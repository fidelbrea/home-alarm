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

package servidoralarma;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Scanner;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import servidoralarma.servidor.Servidor;
import servidoralarma.servidor.hilos.MensajeFirebaseThread;
import servidoralarma.servidor.objetos.TipoMensaje;

/**
 *
 * @author fidel
 */
public class Main {

    private static final Date buildDate = getClassBuildTime();
    private static String sqlUrl;
    private static String sqlUserName;
    private static String sqlUserPassword;
    private static String sqlCreateScript;
    private static String sqlInsertScript;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

// ----------------------------------------------------------------------------------------------------------
        System.out.println("Servidor de alarma");
        System.out.println("Trabajo Fin de Grado de Fidel Brea Montilla");
        System.out.println("Compilado el " + buildDate.toString());
        System.out.println("\nTeclee el comando 'help' para obtener ayuda\n");

        String configJson = "";
        try {
            File fi = new File("./config.json");
            if (fi.exists()) {
                configJson = new String(Files.readAllBytes(fi.toPath()));
            }
        } catch (IOException e) {
            System.out.println("- error al cargar la configuracion (config.json)");
            System.exit(-1);
        }

        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject;
            jsonObject = (JSONObject) parser.parse(configJson);
            if(jsonObject != null && jsonObject.containsKey("sql")){
                JSONObject jsonSql = (JSONObject) jsonObject.get("sql");
                sqlUrl = (String) jsonSql.get("url");
                sqlUserName = (String) jsonSql.get("user_name");
                sqlUserPassword = (String) jsonSql.get("user_password");
                sqlCreateScript = (String) jsonSql.get("script_create");
                sqlInsertScript = (String) jsonSql.get("script_insert");
            }
        } catch (ParseException e) {
            System.out.println("- error leyendo los datos de configuracion.");
            System.exit(-1);
        }

        if (!existeBaseDatos()) {
            if (!crearBaseDatos() || !existeBaseDatos()) {
                System.out.println("- error. no se puede acceder ni crear la base de datos");
                System.exit(-1);
            }
        }

        Servidor servidor = new Servidor();

        if (!servidor.iniciarServicioRMI()) {
            servidor.escribe("-error al intentar iniciar el servicio RMI.");
            System.exit(-1);
        }

        if (!servidor.iniciarPuertoSerie()) {
            servidor.escribe("-error al intentar iniciar el puerto serie.");
            servidor.finalizarServicioRMI();
            System.exit(-1);
        }

        servidor.updateAlarmState();

        servidor.getLectorMensajes().start();

        //servidor.getConsultaMensajesSQL().start();
        boolean salir = false;
        String entradaTeclado;
        Scanner entradaEscaner = new Scanner(System.in);
        do {
            if (entradaEscaner.hasNextLine()) {
                // Comando recibido por teclado
                entradaTeclado = entradaEscaner.nextLine().trim();

                if (entradaTeclado.equals("exit")) {
                    salir = true;
                } else if (entradaTeclado.startsWith("getSensors")) {
                    servidor.getSensors();
                } else if (entradaTeclado.startsWith("setSensors")) {
                    servidor.setSensors();
                } else if (entradaTeclado.startsWith("armAlarm")) {
                    servidor.armAlarm();
                } else if (entradaTeclado.startsWith("disarmAlarm")) {
                    servidor.disarmAlarm();
                } else if (entradaTeclado.startsWith("getRam")) {
                    servidor.getRam();
                } else if (entradaTeclado.startsWith("getLoops")) {
                    servidor.getLoops();
                } else if (entradaTeclado.startsWith("msg")) {
                    String msg = "Esta notificacion es una notificacion de prueba.";
                    if (entradaTeclado.trim().indexOf(' ') > -1) {
                        msg = entradaTeclado.substring(3).trim();
                    }
                    try {
                        MensajeFirebaseThread mensajeFirebase = new MensajeFirebaseThread(servidor);
                        mensajeFirebase.setTitulo("Notificacion de prueba");
                        mensajeFirebase.setMensaje(msg);
                        mensajeFirebase.setTokens(servidor.getDatosDB().getTokens());
                        mensajeFirebase.start();
                    } catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                        servidor.escribe(e.toString());
                    }
                } else if (entradaTeclado.equals("help")) {
                    servidor.escribe("help         Muestra esta ayuda");
                    servidor.escribe("msg [texto]  Envia a los smartphones mensaje por defecto o texto especificado");
                    servidor.escribe("armAlarm     Arma el sistema de alarma");
                    servidor.escribe("disarmAlarm  Desarma el sistema de alarma");
                    servidor.escribe("getSensors   Obtiene la configuracion de los sensores");
                    servidor.escribe("setSensors   Establece la configuracion de los sensores con la info de la BD");
                    servidor.escribe("getRam       Muestra la cantidad de RAM en bytes disponibles en la placa Arduino");
                    servidor.escribe("getLoops     Muestra el numero de loops cada 10 segundos (habilitado solamente durante pruebas)");
                    servidor.escribe("exit         Finaliza el servidor");
                } else {
                    servidor.escribe("El comando " + entradaTeclado + " no se reconoce");
                    servidor.escribe("Teclee el comando help para obtener ayuda");
                }
            }
        } while (!salir);

        entradaEscaner.close();
        servidor.getLectorMensajes().setTerminar(true);
        servidor.finalizarPuertoSerie();
        servidor.finalizarServicioRMI();

        System.exit(0); //Salimos correctamente del sistema
// ----------------------------------------------------------------------------------------------------------

    }

    private static boolean existeBaseDatos() {
        System.out.println("comprobando si existe la base de datos...");
        String driver = "com.mysql.cj.jdbc.Driver"; // dirver nuevo
        String dbName = "alarma";

        try {
            Class.forName(driver).newInstance();
            Connection conexion = DriverManager.getConnection(sqlUrl + dbName, sqlUserName, sqlUserPassword);
            Statement s = conexion.createStatement();
            s.executeQuery("select Id_Sensor_Dispara from Dispara");
            if (!conexion.isClosed()) {
                conexion.close();
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            System.out.println(e.toString());
            return false;
        }
        return true;
    }

    private static boolean crearBaseDatos() {
        System.out.println("creando la base de datos...");
        String driver = "com.mysql.cj.jdbc.Driver"; // dirver nuevo
        String queryCreate = leerFichero(sqlCreateScript);
        String queryInsert = leerFichero(sqlInsertScript);

        if (queryCreate == null) {
            System.out.println("el archivo " + sqlCreateScript + " no se ha leido bien");
            return false;
        }

        if (queryInsert == null) {
            System.out.println("el archivo " + sqlInsertScript + " no se ha leido bien");
            return false;
        }

        try {
            Class.forName(driver).newInstance();
            Connection conexion = DriverManager.getConnection(sqlUrl, sqlUserName, sqlUserPassword);

            Statement s = conexion.createStatement();
            for (String command : queryCreate.split(";")) {
                if (command.trim().length() > 0) {
                    System.out.println(command + ";");
                    s.executeUpdate(command + ";");
                }
            }
            for (String command : queryInsert.split(";")) {
                if (command.trim().length() > 0) {
                    System.out.println(command + ";");
                    s.executeUpdate(command + ";");
                }
            }

            if (!conexion.isClosed()) {
                conexion.close();
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
            System.out.println(e.toString());
            return false;
        }
        return true;
    }

    /**
     *
     * @param fileName
     * @return
     */
    public static String leerFichero(String fileName) {
        File file = new File(fileName);
        String sCadena;
        String res = "";

        if (!file.exists()) {
            return null;
        }

        try {
            BufferedReader bf = new BufferedReader(new FileReader(file));
            while ((sCadena = bf.readLine()) != null) {
                res += sCadena;
            }
            bf.close();
        } catch (IOException e) {
            return null;
        }

        while (res.contains("  ")) {
            res = res.replace("  ", " ");
        }
        res = res.replace(";\n", ";");
        return res;
    }

    /**
     * Handles files, jar entries, and deployed jar entries in a zip file (EAR).
     *
     * @return The date if it can be determined, or null if not.
     */
    private static Date getClassBuildTime() {
        Date d = null;
        Class<?> currentClass = new Object() {
        }.getClass().getEnclosingClass();
        URL resource = currentClass.getResource(currentClass.getSimpleName() + ".class");
        if (resource != null) {
            if (resource.getProtocol().equals("file")) {
                try {
                    d = new Date(new File(resource.toURI()).lastModified());
                } catch (URISyntaxException ignored) {
                }
            } else if (resource.getProtocol().equals("jar")) {
                String path = resource.getPath();
                d = new Date(new File(path.substring(5, path.indexOf("!"))).lastModified());
            } else if (resource.getProtocol().equals("zip")) {
                String path = resource.getPath();
                File jarFileOnDisk = new File(path.substring(0, path.indexOf("!")));
                //long jfodLastModifiedLong = jarFileOnDisk.lastModified ();
                //Date jfodLasModifiedDate = new Date(jfodLastModifiedLong);
                try ( JarFile jf = new JarFile(jarFileOnDisk)) {
                    ZipEntry ze = jf.getEntry(path.substring(path.indexOf("!") + 2));//Skip the ! and the /
                    long zeTimeLong = ze.getTime();
                    Date zeTimeDate = new Date(zeTimeLong);
                    d = zeTimeDate;
                } catch (IOException | RuntimeException ignored) {
                }
            }
        }
        return d;
    }
}
