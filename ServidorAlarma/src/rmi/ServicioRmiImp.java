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
package rmi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import servidoralarma.servidor.Servidor;
import servidoralarma.servidor.hilos.CapturadorImagenesThread;
import servidoralarma.servidor.objetos.Camara;
import servidoralarma.servidor.objetos.Sensor;
import servidoralarma.servidor.objetos.Usuario;

/**
 *
 * @author fidel
 */
public class ServicioRmiImp implements ServicioRmiInt {

    private final Servidor servidor;

    public ServicioRmiImp(Servidor servidor) {
        this.servidor = servidor;
    }

    @Override
    public boolean checkEmail(String email) {
        servidor.escribe("check email " + email);
        try {
            for (Usuario usuario : servidor.getDatosDB().getUsers()) {
                if (usuario.getEmail().equals(email)) {
                    return true;
                }
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
        }
        return false;
    }

    @Override
    public int getAlarmState() {
        return servidor.getAlarmState();
    }

    @Override
    public boolean registerUser(String email, String token) {
        boolean usuarioRegistrado = false;
        try {
            for (Usuario usuario : servidor.getDatosDB().getUsers()) {
                if (usuario.getEmail().equals(email)) {
                    usuarioRegistrado = true;
                    servidor.escribe("acceso del usuario " + usuario.getAlias());
                    if (usuario.getToken() == null || !usuario.getToken().equals(token)) {
                        servidor.getDatosDB().updateUsuario(email, token);
                        servidor.escribe("token actualizado");
                    }
                    break;
                }
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
        }
        return usuarioRegistrado;
    }

    @Override
    public String getCamsList() {
        servidor.escribe("entregando lista de camaras");
        ArrayList<Object> aliasCam = new ArrayList<>();
        try{
            for (Camara cam : servidor.getDatosDB().getCameras()) {
                aliasCam.add(cam.getAlias());
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
        }
        JSONArray jsonCameras = new JSONArray(aliasCam);
        HashMap<Object, Object> json = new HashMap<>();
        json.put("cameras", jsonCameras);
        JSONObject jsonObject = new JSONObject(json);
        return jsonObject.toJSONString();
    }

    @Override
    public String getEventsList(String camera) {
        servidor.escribe("entregando lista de eventos");
        Camara cam = getCamera(camera);
        JSONArray jsonPictures = new JSONArray();
        JSONArray jsonEvents = new JSONArray();
        for (String s : cam.getEventsList()) {
            if (s.endsWith(".jpg")) {
                jsonPictures.add(s);
            } else {
                jsonEvents.add(s);
            }
        }
        JSONObject jsonRes = new JSONObject();
        jsonRes.put("pictures", jsonPictures);
        jsonRes.put("events", jsonEvents);
        return jsonRes.toJSONString();
    }

    @Override
    public String getEventPictures(String camera, String event) {
        servidor.escribe("entregando imagenes de evento");
        Camara cam = getCamera(camera);
        JSONArray jsonPictures = new JSONArray();
        for (String s : cam.getEventPictures(event)) {
            if (s.endsWith(".jpg")) {
                jsonPictures.add(s);
            }
        }
        JSONObject jsonRes = new JSONObject();
        jsonRes.put("pictures", jsonPictures);
        return jsonRes.toJSONString();
    }

    @Override
    public byte[] getPicture(String camera, String event, String picture) {
        servidor.escribe("entregando imagen");
        Camara cam = getCamera(camera);
        String pathToPicture = cam.getCamPath() + ((event.length() > 0) ? event + "/" : "") + picture;
        byte[] res = {};
        try {
            File fi = new File(pathToPicture);
            if (fi.exists() && fi.isFile()) {
                res = Files.readAllBytes(fi.toPath());
            }
        } catch (IOException e) {
            //ToDo
        }
        return res;
    }

    @Override
    public void shoot(String camera, int shoots) {
        servidor.escribe("tomando imagenes de camara " + camera);
        Camara cam = getCamera(camera);
        CapturadorImagenesThread camEvent = new CapturadorImagenesThread(cam, shoots);
        camEvent.start();
    }

    private Camara getCamera(String camera) {
        servidor.escribe("entregando camara " + camera);
        try{
            for (Camara c : servidor.getDatosDB().getCameras()) {
                if (c.getAlias().equals(camera)) {
                    return c;
                }
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
        }
        return null;
    }

    @Override
    public boolean deleteEvent(String camera, String event) {
        servidor.escribe("eliminando evento capturado " + event);
        Camara cam = getCamera(camera);
        String pathToEvent = cam.getCamPath() + event + "/";
        File dir = new File(pathToEvent);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                file.delete();
            }
            return dir.delete();
        }
        return false;
    }

    @Override
    public boolean deletePicture(String camera, String event, String picture) {
        servidor.escribe("eliminando imagen " + picture + " del evento " + event);
        Camara cam = getCamera(camera);
        String pathToPicture = cam.getCamPath() + ((event.length() > 0) ? event + "/" : "") + picture;
        File fi = new File(pathToPicture);
        if (fi.exists() && fi.isFile()) {
            return fi.delete();
        }
        return false;
    }

    @Override
    public String getSensorsList() {
        servidor.escribe("entregando lista de sensores");
        JSONObject jsonRes = new JSONObject();
        try{
            for (Sensor sensor : servidor.getDatosDB().getSensors()) {
                jsonRes.put(sensor.getAlias(), sensor.isEnabled());
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
        }
        return jsonRes.toJSONString();
    }

    @Override
    public String getSensor(String alias) {
        servidor.escribe("entregando sensor " + alias);
        JSONObject jsonRes = new JSONObject();
        try{
            for (Sensor sensor : servidor.getDatosDB().getSensors()) {
                if (sensor.getAlias().equals(alias)) {
                    jsonRes.put("id", sensor.getId());
                    jsonRes.put("alias", sensor.getAlias());
                    jsonRes.put("enabled", sensor.isEnabled());
                    jsonRes.put("delayed", sensor.isDelayed());
                    break;
                }
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
        }
        return jsonRes.toJSONString();
    }

    @Override
    public void addCamera(String alias, String uri) {
        try{
            servidor.getDatosDB().addCamara(alias, uri);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
        }
    }

    @Override
    public void deleteCamera(String alias) {
        try{
            servidor.getDatosDB().deleteCamera(alias);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
        }
    }

    @Override
    public void updateSensor(int id, String alias, boolean enabled, boolean delayed) {
        try{
            servidor.getDatosDB().updateSensor(id, alias, enabled, delayed);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
        }
    }

    @Override
    public String getUsersList() {
        servidor.escribe("entregando lista de usurios");
        JSONObject jsonRes = new JSONObject();
        try{
            for (Usuario user : servidor.getDatosDB().getUsers()) {
                jsonRes.put(user.getAlias(), user.isAdministrador());
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
        }
        return jsonRes.toJSONString();
    }

    @Override
    public String getUser(String alias) {
        servidor.escribe("entregando usurio " + alias);
        JSONObject jsonRes = new JSONObject();
        try{
            for (Usuario user : servidor.getDatosDB().getUsers()) {
                if (user.getAlias().equals(alias)) {
                    jsonRes.put("email", user.getEmail());
                    jsonRes.put("alias", user.getAlias());
                    jsonRes.put("is_admin", user.isAdministrador());
                    jsonRes.put("code", user.getCodigo());
                    jsonRes.put("tag", user.getTagRFID());
                    break;
                }
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
        }
        return jsonRes.toJSONString();
    }

    @Override
    public boolean addUser(String email, String alias) {
        try{
            for (Usuario usuario : servidor.getDatosDB().getUsers()) {
                if (usuario.getEmail().equals(email) || usuario.getAlias().equals(alias)) {
                    return false;
                }
            }
            servidor.getDatosDB().addUsuario(email, alias);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
        }
        return true;
    }

    @Override
    public boolean deleteUser(String alias) {
        boolean res = false;
        try{
            res = servidor.getDatosDB().deleteUser(alias);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
        }
        return res;
    }

    @Override
    public void updateUserTag(String email, String tag) {
        try{
            servidor.getDatosDB().updateUserTag(email, tag);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
        }
    }

    @Override
    public void updateUserCode(String email, String code) {
        try{
            servidor.getDatosDB().updateUserCode(email, code);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
        }
    }

    @Override
    public boolean updateUserAdmin(String email, boolean admin) {
        boolean res = false;
        try{
            res = servidor.getDatosDB().updateUserAdmin(email, admin);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
        }
        return res;
    }

    @Override
    public boolean isAdministrator(String email) {
        boolean res = false;
        try{
            res = servidor.getDatosDB().isAdministrator(email);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
        }
        return res;
    }

    @Override
    public void armAlarm() {
        servidor.armAlarm();
    }

    @Override
    public void disarmAlarm() {
        servidor.disarmAlarm();
    }
    
    @Override
    public String getLastEvents(int limit){
        servidor.escribe("entregando ultimas " + limit + " entradas del historial");
        JSONObject jsonRes = new JSONObject();
        try{
            JSONArray aEventos = new JSONArray();
            HashMap<Integer, String> eventMap = servidor.getDatosDB().getLastEventos(limit);
            for(int i=0; i<eventMap.size(); i++){
                JSONObject jsonEvent = new JSONObject();
                jsonEvent.put("id", i);
                jsonEvent.put("timestamp", eventMap.get(i).substring(0,19));
                jsonEvent.put("event", eventMap.get(i).substring(19));
                aEventos.add(jsonEvent);
            }
            jsonRes.put("events", aEventos);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
        }
        return jsonRes.toJSONString();
    }
    
    @Override
    public String getSensorCams(String aliasSensor){
        JSONObject jsonRes = new JSONObject();
        servidor.escribe("entregando lista de camaras asociadas a " + aliasSensor);
        int id = -1;
        try{
            // Obtenemos el id del sensor
            for (Sensor sensor : servidor.getDatosDB().getSensors()) {
                if (sensor.getAlias().equals(aliasSensor)) {
                    id = sensor.getId();
                    break;
                }
            }

            if(id>-1){
                JSONArray aCameras = new JSONArray();
                ArrayList<String> aSensorCameras = servidor.getDatosDB().getNameSensorCameras(id);
                for(Camara camera : servidor.getDatosDB().getCameras()){
                    JSONObject jsonCameraItem = new JSONObject();
                    jsonCameraItem.put("alias", camera.getAlias());
                    boolean pairedUp = false;
                    for(String aliasCamera : aSensorCameras){
                        if(aliasCamera.equals(camera.getAlias())){
                            pairedUp = true;
                            break;
                        }
                    }
                    jsonCameraItem.put("paired_up", pairedUp);
                    //jsonCameraItem.put("paired_up", ((boolean)(aSensorCameras.contains(camera.getAlias()))));
                    aCameras.add(jsonCameraItem);
                }
                jsonRes.put("cameras", aCameras);
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
        }
        return jsonRes.toJSONString();
    }

    @Override
    public void updateSensorEnabled(String alias, boolean enabled){
        try{
            servidor.getDatosDB().updateSensorEnabled(alias, enabled);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
        }
    }

    @Override
    public void updateSensorDelayed(String alias, boolean delayed){
        try{
            servidor.getDatosDB().updateSensorDelayed(alias, delayed);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
        }
    }

    @Override
    public void associateSensorCamera(String aliasSensor, String aliasCamera){
        try{
            for (Sensor sensor : servidor.getDatosDB().getSensors()) {
                if (sensor.getAlias().equals(aliasSensor)) {
                    servidor.getDatosDB().associateSensorCamera(sensor.getId(), aliasCamera);
                    break;
                }
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
        }
    }

    @Override
    public void disassociateSensorCamera(String aliasSensor, String aliasCamera){
        try{
            for (Sensor sensor : servidor.getDatosDB().getSensors()) {
                if (sensor.getAlias().equals(aliasSensor)) {
                    servidor.getDatosDB().disassociateSensorCamera(sensor.getId(), aliasCamera);
                    break;
                }
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
        }
    }
    
    @Override
    public boolean modifyUserAlias(String oldAlias, String newAlias){
        boolean res = true;
        try {
            for(Usuario usuario : servidor.getDatosDB().getUsers()){
                if(usuario.getAlias().equals(newAlias)){
                    res = false;
                    break;
                }
            }
            if(res){
                servidor.getDatosDB().updateUserAlias(oldAlias, newAlias);
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
            res = false;
        }
        return res;
    }

    @Override
    public boolean modifyCameraAlias(String oldAlias, String newAlias){
        boolean res = true;
        try {
            for(Camara camara : servidor.getDatosDB().getCameras()){
                if(camara.getAlias().equals(newAlias)){
                    res = false;
                    break;
                }
            }
            if(res){
                servidor.getDatosDB().updateCamara(oldAlias, newAlias);
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
            res = false;
        }
        return res;
    }
    
    @Override
    public boolean modifySensorAlias(String oldAlias, String newAlias){
        boolean res = true;
        try {
            for(Sensor sensor : servidor.getDatosDB().getSensors()){
                if(sensor.getAlias().equals(newAlias)){
                    res = false;
                    break;
                }
            }
            if(res){
                servidor.getDatosDB().updateSensorAlias(oldAlias, newAlias);
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            servidor.escribe(e.toString());
            res = false;
        }
        return res;
    }
}
