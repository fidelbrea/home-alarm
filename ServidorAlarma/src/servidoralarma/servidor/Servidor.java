/* 
 * Copyright (C) 2022 Fidel Brea Montilla (fidelbreamontilla@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package servidoralarma.servidor;

import servidoralarma.servidor.objetos.Camara;
import servidoralarma.servidor.objetos.Usuario;
import servidoralarma.servidor.objetos.Sensor;
import com.fazecast.jSerialComm.SerialPort;
import estructuras.listas.Lista;
import estructuras.listas.ListaEnlazadaOrdenada;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import lipermi.exception.LipeRMIException;
import lipermi.handler.CallHandler;
import lipermi.net.Server;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import servidoralarma.servidor.escuchadores.ServicioRmiEscuchador;
import rmi.ServicioRmiImp;
import rmi.ServicioRmiInt;
import servidoralarma.servidor.hilos.CapturadorImagenesThread;
import servidoralarma.servidor.hilos.MensajeFirebaseThread;
import servidoralarma.servidor.escuchadores.PuertoSerieEscuchador;
import servidoralarma.servidor.hilos.ServicioLectorMensajesThread;
import servidoralarma.servidor.utils.DatosDB;
import servidoralarma.servidor.utils.ManejadorJson;

/**
 *
 * @author fidel
 */
public final class Servidor {

    private int puertoServidorRMI;
    private String puertoSerie;
    private String codigoPrearmado;

    private final DatosDB datosDB;
    private SerialPort rs232;
    private final ServicioLectorMensajesThread lectorMensajes;
    private Server rmiServer;

    private int alarmState;
    private Lista mensajes;
    private ArrayList<Integer> sensoresDisparados;

    public Servidor() {
        String configJson = "";
        try {
            File fi = new File("./config.json");
            if (fi.exists()) {
                configJson = new String(Files.readAllBytes(fi.toPath()));
            }
        } catch (IOException e) {
            System.out.println("- error al cargar la configuracion (config.json) desde Servidor");
            System.exit(-1);
        }

        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject;
            jsonObject = (JSONObject) parser.parse(configJson);
            if(jsonObject != null && jsonObject.containsKey("server")){
                JSONObject jsonServer = (JSONObject) jsonObject.get("server");
                this.puertoServidorRMI = ((Long) jsonServer.get("rmi_port")).intValue();
                this.puertoSerie = (String) jsonServer.get("serial_port");
                this.codigoPrearmado = (String) jsonServer.get("prearm_code");
            }
        } catch (ParseException e) {
            System.out.println("- error leyendo los datos de configuracion desde Servidor.");
            System.exit(-1);
        }
        
        alarmState = -1;
        datosDB = new DatosDB();
        mensajes = new ListaEnlazadaOrdenada();
        sensoresDisparados = new ArrayList<>();

        lectorMensajes = new ServicioLectorMensajesThread(this);
        lectorMensajes.setPriority(Thread.NORM_PRIORITY);
        lectorMensajes.setName("alarma::ServicioLectorMensajes");
    }

    /**
     * Inicializa el servidor RMI (biblioteca LipeRMI)
     *
     * @return true si conexion ok
     */
    public boolean iniciarServicioRMI() {
        escribe("iniciando servicio RMI en puerto " + puertoServidorRMI + "...");
        rmiServer = new Server();
        CallHandler callHandler = new CallHandler();
        try {
            callHandler.registerGlobal(ServicioRmiInt.class, new ServicioRmiImp(this));
            rmiServer.bind(puertoServidorRMI, callHandler);
        } catch (IOException | LipeRMIException e) {
            escribe(e.toString());
            return false;
        }
        rmiServer.addServerListener(new ServicioRmiEscuchador(this));
        escribe("servicio RMI iniciado");
        return true;
    }

    public void finalizarServicioRMI() {
        rmiServer.close();
        escribe("finalizado el servicio RMI");
    }

    public boolean iniciarPuertoSerie() {
        escribe("iniciando puerto serie...");
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort s : ports) {
            if (s.getSystemPortPath().equals(puertoSerie)){
                s.setBaudRate(9600);
                s.setNumDataBits(8);
                s.setParity(SerialPort.NO_PARITY);
                s.setNumStopBits(0);
                boolean puertoAbierto = s.openPort(0);
                if (puertoAbierto) {
                    s.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
                    s.addDataListener(new PuertoSerieEscuchador(this));
                    setRs232(s);
                    escribe("puerto serie " + s.getSystemPortPath() + " iniciado");
                    return true;
                } else {
                    escribe("error al abrir puerto serie");
                    return false;
                }
            }
        }
        escribe("no se ha encontrado el puerto serie");
        return false;
    }

    public void finalizarPuertoSerie() {
        if (rs232 != null) {
            rs232.removeDataListener();
            if (rs232.isOpen()) {
                rs232.closePort();
                escribe("se ha cerrado el puerto serie");
            }
        }
    }

    /**
     * Escribe una linea de texto en la consola
     *
     * @param texto
     */
    public void escribe(String texto) {
        System.out.println(texto);
    }

    public void escribe(String texto, boolean toLog) {
        System.out.println(texto);
        if (toLog) {
            try {
                datosDB.addEvento(texto);
            } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                System.out.println(e.toString());
            }
        }
    }

    public void handleCode(String code) {
        if(code.equals(codigoPrearmado)){
            try {
                JSONArray aSensores = new JSONArray();
                for (Sensor sensor : datosDB.getSensors()) {
                    HashMap<String,Object>aSensor = new HashMap<>();
                    aSensor.put("id", sensor.getId());
                    aSensor.put("habilitado", sensor.isEnabled());
                    aSensor.put("retardado", sensor.isDelayed());
                    aSensores.add(new JSONObject(aSensor));
                }
                JSONArray aCodigos = new JSONArray();
                JSONArray aEtiquetas = new JSONArray();
                for(Usuario u : datosDB.getUsers()){
                    aCodigos.add(u.getCodigo());
                    aEtiquetas.add(u.getTagRFID());
                }
                HashMap<String,Object> datosArmado = new HashMap<>();
                datosArmado.put("sensores", aSensores);
                datosArmado.put("codigos", aCodigos);
                datosArmado.put("etiquetas", aEtiquetas);
                JSONObject obj = new JSONObject();
                obj.put("tipo","prearmar");
                obj.put("datos",new JSONObject(datosArmado));
                ManejadorJson.enviarJSON(rs232, obj.toJSONString());
            } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                escribe(e.toString());
            }        
        }else{
            try {
                MensajeFirebaseThread mensajeFirebase = new MensajeFirebaseThread(this);
                mensajeFirebase.getMapDataPayload().put("code", code);
                mensajeFirebase.setTokens(datosDB.getTokens());
                mensajeFirebase.start();
// -- el desarmado se hace en la centralita con los datos enviados al armar o prearmar
//                for (Usuario user : datosDB.getUsers()) {
//                    if (code.equals(user.getCodigo())) {
//                        disarmAlarm();
//                    }
//                }
            } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                escribe(e.toString());
            }
        }
    }

    public void handleTag(String tag) {
        try {
            MensajeFirebaseThread mensajeFirebase = new MensajeFirebaseThread(this);
            mensajeFirebase.getMapDataPayload().put("tag", tag);
            mensajeFirebase.setTokens(datosDB.getTokens());
            mensajeFirebase.start();
// -- el desarmado se hace en la centralita con los datos enviados al armar o prearmar
//            for (Usuario user : datosDB.getUsers()) {
//                if (tag.equals(String.valueOf(user.getTagRFID()))) {
//                    disarmAlarm();
//                }
//            }
        } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            escribe(e.toString());
        }

    }

    public void armAlarm() {
        try {
            JSONArray aSensores = new JSONArray();
            for (Sensor sensor : datosDB.getSensors()) {
                HashMap<String,Object>aSensor = new HashMap<>();
                aSensor.put("id", sensor.getId());
                aSensor.put("habilitado", sensor.isEnabled());
                aSensor.put("retardado", sensor.isDelayed());
                aSensores.add(new JSONObject(aSensor));
            }
            JSONArray aCodigos = new JSONArray();
            JSONArray aEtiquetas = new JSONArray();
            for(Usuario u : datosDB.getUsers()){
                aCodigos.add(u.getCodigo());
                aEtiquetas.add(u.getTagRFID());
            }
            HashMap<String,Object> datosArmado = new HashMap<>();
            datosArmado.put("sensores", aSensores);
            datosArmado.put("codigos", aCodigos);
            datosArmado.put("etiquetas", aEtiquetas);
            JSONObject obj = new JSONObject();
            obj.put("tipo","armar");
            obj.put("datos",new JSONObject(datosArmado));
            ManejadorJson.enviarJSON(rs232, obj.toJSONString());
        } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            escribe(e.toString());
        }
    }

    public void disarmAlarm() {
        JSONObject obj = new JSONObject();
        obj.put("tipo", "desarmar");
        obj.put("datos", "");
        ManejadorJson.enviarJSON(rs232, obj.toJSONString());
    }

    public void updateAlarmState() {
        JSONObject obj = new JSONObject();
        obj.put("tipo", "getAlarmState");
        obj.put("datos", "");
        ManejadorJson.enviarJSON(rs232, obj.toJSONString());
    }

    public void handleAlarmState(String alarmState) {
        this.alarmState = Integer.parseInt(alarmState);
        try{
            MensajeFirebaseThread mensajeFirebase = new MensajeFirebaseThread(this);
            mensajeFirebase.getMapDataPayload().put("alarm_state", alarmState);
            mensajeFirebase.setTokens(datosDB.getTokens());
            mensajeFirebase.start();
        } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            escribe(e.toString());
        }

        if (alarmState.equals("0")) {
            System.out.println("--> borro lista de sensores disparados");
            sensoresDisparados.clear();
        }
    }

    public void handleTrigger() {
        escribe("--> alarma disparada");
        String txtSensoresDisparados;
        if (!sensoresDisparados.isEmpty()) {
            try {
                boolean hayCaptura = false;
                txtSensoresDisparados = (sensoresDisparados.size() == 1) ? "El sensor " : "Los sensores ";
                for(Sensor sensor : getDatosDB().getSensors()){
                    for(Integer nSensor : sensoresDisparados){
                        if(sensor.getId() == nSensor){
                            txtSensoresDisparados += sensor.getAlias() + ", ";
                            for (Camara camera : getDatosDB().getSensorCameras(nSensor)) {
                                hayCaptura = true;
                                CapturadorImagenesThread camEvent = new CapturadorImagenesThread(camera, 3);
                                camEvent.start();
                            }
                            break;
                        }
                    }
                }
                txtSensoresDisparados = txtSensoresDisparados.substring(0, txtSensoresDisparados.length() - 2);
                txtSensoresDisparados += (sensoresDisparados.size() == 1) ? " ha sido disparado." : " han sido disparados.";
                txtSensoresDisparados += " Verifique la vivienda" + ((hayCaptura) ? " y las capturas de imagenes." : ".");
                MensajeFirebaseThread mensajeFirebase = new MensajeFirebaseThread(this);
                mensajeFirebase.setTitulo("Alarma disparada");
                mensajeFirebase.setMensaje(txtSensoresDisparados);
                mensajeFirebase.setTokens(datosDB.getTokens());
                mensajeFirebase.start();
            } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                escribe(e.toString());
            }
        }
    }

    /*
     * Getters and Setters
     */
    public void getSensors() {
        JSONObject obj = new JSONObject();
        obj.put("tipo", "getSensors");
        obj.put("datos", "");
        ManejadorJson.enviarJSON(rs232, obj.toJSONString());
    }

    public void setSensors() {
        try {
            JSONArray aSensors = new JSONArray();
            for (Sensor sensor : datosDB.getSensors("order by id asc")) {
                aSensors.add(sensor.isEnabled());
                aSensors.add(sensor.isDelayed());
            }
            JSONObject obj = new JSONObject();
            obj.put("tipo", "setSensors");
            obj.put("datos", aSensors);
            ManejadorJson.enviarJSON(rs232, obj.toJSONString());
        } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            escribe(e.toString());
        }
    }

    public void getRam() {
        JSONObject obj = new JSONObject();
        obj.put("tipo", "getRam");
        obj.put("datos", "");
        ManejadorJson.enviarJSON(rs232, obj.toJSONString());
    }

    public void getLoops() {
        JSONObject obj = new JSONObject();
        obj.put("tipo", "getLoops");
        obj.put("datos", "");
        ManejadorJson.enviarJSON(rs232, obj.toJSONString());
    }

    public int getAlarmState() {
        return alarmState;
    }

    public ArrayList<Integer> getSensoresDisparados() {
        return sensoresDisparados;
    }

    public void setSensoresDisparados(ArrayList<Integer> sensoresDisparados) {
        this.sensoresDisparados = sensoresDisparados;
    }

    public SerialPort getRs232() {
        return rs232;
    }

    public void setRs232(SerialPort rs232) {
        this.rs232 = rs232;
    }

    public Lista getMensajes() {
        return mensajes;
    }

    public void setMensajes(Lista mensajes) {
        this.mensajes = mensajes;
    }

    public ServicioLectorMensajesThread getLectorMensajes() {
        return lectorMensajes;
    }

    public DatosDB getDatosDB() {
        return datosDB;
    }

}
