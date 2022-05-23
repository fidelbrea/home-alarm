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
package servidoralarma.servidor.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import servidoralarma.servidor.objetos.Camara;
import servidoralarma.servidor.objetos.Disparo;
import servidoralarma.servidor.objetos.Sensor;
import servidoralarma.servidor.objetos.Usuario;

/**
 *
 * @author fidel
 */
public class DatosDB {

    //private final String driver = "com.mysql.jdbc.Driver"; // driver viejo
    private final String driver = "com.mysql.cj.jdbc.Driver"; // dirver nuevo
    private final String dbName = "alarma";
    private String url;
    private String userName;
    private String password;

    public DatosDB() {
        String configJson = "";
        try {
            File fi = new File("./config.json");
            if (fi.exists()) {
                configJson = new String(Files.readAllBytes(fi.toPath()));
            }
        } catch (IOException e) {
            System.out.println("- error al cargar la configuracion (config.json) desde DatosDB");
            System.exit(-1);
        }

        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject;
            jsonObject = (JSONObject) parser.parse(configJson);
            if(jsonObject != null && jsonObject.containsKey("sql")){
                JSONObject jsonSql = (JSONObject) jsonObject.get("sql");
                this.url = (String) jsonSql.get("url");
                this.userName = (String) jsonSql.get("user_name");
                this.password = (String) jsonSql.get("user_password");
            }
        } catch (ParseException e) {
            System.out.println("- error leyendo los datos de configuracion desde DatosDB.");
            System.exit(-1);
        }
    }

    private Connection getConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        Class.forName(driver).newInstance();
        return DriverManager.getConnection(url + dbName, userName, password);
    }

    private void executeUpdate(String query) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        Connection connection = getConnection();
        Statement s = connection.createStatement();
        s.executeUpdate(query);
        if (!connection.isClosed()) {
            connection.close();
        }
    }

    private boolean execute(String query) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        boolean res = false;
        Connection connection = getConnection();
        Statement s = connection.createStatement();
        res = s.execute(query);
        if (!connection.isClosed()) {
            connection.close();
        }
        return res;
    }

    public ArrayList<Usuario> getUsers() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        ArrayList<Usuario> users;
        users = new ArrayList<>();
        Connection connection = getConnection();
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery("select email,alias,es_administrador,tag_rfid,codigo,token from Usuario order by alias asc");
        if (rs != null) {
            while (rs.next()) {
                users.add(new Usuario(rs.getString(1), rs.getString(2), rs.getBoolean(3), rs.getLong(4), rs.getString(5), rs.getString(6)));
            }
        }
        if (!connection.isClosed()) {
            connection.close();
        }
        return users;
    }

    public ArrayList<Sensor> getSensors() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
        ArrayList<Sensor> sensores;
        sensores = new ArrayList<>();
        Connection connection = getConnection();
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery("select id,alias,habilitado,retardado from Sensor order by alias asc");
        while (rs.next()) {
            sensores.add(new Sensor(rs.getInt(1), rs.getString(2), rs.getBoolean(3), rs.getBoolean(4)));
        }
        if (!connection.isClosed()) {
            connection.close();
        }
        return sensores;
    }

    public ArrayList<Sensor> getSensors(String orderBy) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        ArrayList<Sensor> sensores;
        sensores = new ArrayList<>();
        Connection connection = getConnection();
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery("select id,alias,habilitado,retardado from Sensor " + orderBy);
        while (rs.next()) {
            sensores.add(new Sensor(rs.getInt(1), rs.getString(2), rs.getBoolean(3), rs.getBoolean(4)));
        }
        if (!connection.isClosed()) {
            connection.close();
        }
        return sensores;
    }

    public Sensor getSensor(int idSensor) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Sensor sensor = null;
        Connection connection = getConnection();
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery("select id,alias,habilitado,retardado from Sensor where id=" + idSensor);
        if (rs.next()) {
            sensor = new Sensor(rs.getInt(1), rs.getString(2), rs.getBoolean(3), rs.getBoolean(4));
        }
        if (!connection.isClosed()) {
            connection.close();
        }
        return sensor;
    }

    public ArrayList<Camara> getCameras() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        ArrayList<Camara> camaras;
        camaras = new ArrayList<>();
        Connection connection = getConnection();
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery("select alias,uri from Camara order by alias asc");
        while (rs.next()) {
            camaras.add(new Camara(rs.getString(1), rs.getString(2)));
        }
        if (!connection.isClosed()) {
            connection.close();
        }
        return camaras;
    }

    public ArrayList<Disparo> getDisparos() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        ArrayList<Disparo> disparos;
        disparos = new ArrayList<>();
        Connection connection = getConnection();
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery("select id_sensor_dispara,id_camara_disparada from Disparo");
        while (rs.next()) {
            disparos.add(new Disparo(rs.getInt(1), rs.getInt(2)));
        }
        if (!connection.isClosed()) {
            connection.close();
        }
        return disparos;
    }

    public HashMap<Integer, String> getLastEventos(int limit) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        HashMap<Integer, String> eventMap = new HashMap<>();
        Connection connection = getConnection();
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery("select timestamp,descripcion from Evento order by id desc limit " + limit);
        int i = 0;
        while (rs.next()) {
            eventMap.put(i++, new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(rs.getTimestamp(1)) + rs.getString(2));
        }
        if (!connection.isClosed()) {
            connection.close();
        }
        return eventMap;
    }

    public void updateUsuario(String email, String alias, boolean administrator, String code, long tag_rfid, String token) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        int administrators = 0;
        for (Usuario u : getUsers()) {
            if (!u.getEmail().equals(email) && u.getAlias().equals(alias)) {
                return;
            }
            if (!u.getEmail().equals(email) && u.getToken().equals(token)) {
                return;
            }
            if (!u.getEmail().equals(email) && u.getCodigo().equals(code)) {
                return;
            }
            if (!u.getEmail().equals(email) && u.isAdministrador()) {
                administrators++;
            }
        }
        if (!administrator && administrators == 0) {
            return;
        }

        executeUpdate("update Usuario set alias='" + alias + "',"
                + "es_administrador=" + ((administrator) ? "true" : "false") + ","
                + "token='" + token + "'"
                + ",tag_rfid=" + tag_rfid + ","
                + "codigo='" + code + "'"
                + "  where email='" + email + "'");
    }

    public void updateUsuario(String email, String token) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        executeUpdate("update Usuario set token='" + token + "' where email='" + email + "'");
    }

    public void updateUserAlias(String oldAlias, String newAlias) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        executeUpdate("update Usuario set alias='" + newAlias + "' where alias='" + oldAlias + "'");
    }

    public void updateUserCode(String email, String code) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        executeUpdate("update Usuario set codigo='" + code + "' where email='" + email + "'");
    }

    public void updateUserTag(String email, String tag) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        executeUpdate("update Usuario set tag_rfid='" + tag + "' where email='" + email + "'");
    }

    public boolean updateUserAdmin(String email, boolean admin) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        int administrators = 0;
        boolean isAdmin = false;
        for (Usuario u : getUsers()) {
            if (u.isAdministrador()) {
                administrators++;
                if (u.getEmail().equals(email)) {
                    isAdmin = true;
                }
            }
        }
        if (isAdmin && administrators == 1) {
            return false;
        }
        executeUpdate("update Usuario set es_administrador=" + (admin ? "true" : "false") + " where email='" + email + "'");
        return true;
    }

    public void updateSensorEnabled(String alias, boolean enabled) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        executeUpdate("update Sensor set habilitado=" + (enabled ? "true" : "false") + " where alias='" + alias + "'");
    }
    
    public void updateSensorDelayed(String alias, boolean delayed) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        executeUpdate("update Sensor set retardado=" + (delayed ? "true" : "false") + " where alias='" + alias + "'");
    }

    public void updateSensorAlias(String aliasFrom, String aliasTo) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        executeUpdate("update Sensor set alias='" + aliasTo + "' where alias='" + aliasFrom + "'");
    }

    public void updateSensor(int id, String alias, boolean enabled, boolean delayed) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        for (Sensor s : getSensors()) {
            if (s.getId() != id && s.getAlias().equals(alias)) {
                return;
            }
        }
        executeUpdate("update Sensor set alias='" + alias + "',"
                + "habilitado=" + ((enabled) ? "true" : "false") + ","
                + "retardado=" + ((delayed) ? "true" : "false") + ","
                + "  where id=" + id);
    }

    public void updateCamara(String aliasFrom, String aliasTo) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        executeUpdate("update Camara set alias='" + aliasTo + "' where alias='" + aliasFrom + "'");
    }

    public void addUsuario(String email, String alias) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        executeUpdate("insert into Usuario (email,alias) values ('" + email + "','" + alias + "')");
    }

    public void addSensor(String alias) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        executeUpdate("insert into Sensor (alias) values ('" + alias + "')");
    }

    public void addCamara(String alias, String uri) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        for (Camara c : getCameras()) {
            if (c.getAlias().equals(alias)) {
                return;
            }
        }
        executeUpdate("insert into Camara (alias,uri) values ('" + alias + "','" + uri + "')");
    }

    public void addDisparo(Integer idSensor, Integer idCamara) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        executeUpdate("insert into Disparo (id_sensor_dispara,id_camara_disparada) values (" + idSensor.toString() + "," + idCamara.toString() + ")");
    }

    public void addEvento(String evento) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        executeUpdate("insert into Evento (descripcion) values ('" + evento + "')");
    }

    public void deleteCamera(String alias) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        executeUpdate("delete from Dispara where alias_camara_disparada='" + alias + "'");
        executeUpdate("delete from Camara where alias='" + alias + "'");
    }

    public boolean deleteUser(String alias) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        int administrators = 0;
        boolean isAdmin = false;
        for (Usuario u : getUsers()) {
            if (u.isAdministrador()) {
                administrators++;
                if (u.getAlias().equals(alias)) {
                    isAdmin = true;
                }
            }
        }
        if (isAdmin && administrators == 1) {
            return false;
        }
        executeUpdate("delete from Usuario where alias='" + alias + "'");
        return true;
    }
    
    public boolean isAdministrator(String email) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        Connection connection = getConnection();
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery("select es_administrador from Usuario where email='" + email + "'");
        if(rs.next()) {
            return rs.getBoolean(1);
        }
        if (!connection.isClosed()) {
            connection.close();
        }
        return false;
    }

    public ArrayList<String> getTokens() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        ArrayList<String> tokens = new ArrayList<>();
        for (Usuario u : getUsers()) {
            if (u.getToken() != null && u.getToken().length() > 20) {
                tokens.add(u.getToken());
            }
        }
        return tokens;
    }

    public ArrayList<String> getNameSensorCameras(int idSensor) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        ArrayList<String> sensorCameras = new ArrayList<>();
        Connection connection = getConnection();
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery("select alias_camara_disparada from Dispara where id_sensor_dispara=" + idSensor + " order by alias_camara_disparada asc");
        while (rs.next()) {
            sensorCameras.add(rs.getString(1));
        }
        if (!connection.isClosed()) {
            connection.close();
        }
        return sensorCameras;
    }

    public void associateSensorCamera(int idSensor, String aliasCamera) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
        executeUpdate("insert into Dispara (id_sensor_dispara,alias_camara_disparada) values (" + idSensor + ",'" + aliasCamera + "')");
    }

    public void disassociateSensorCamera(int idSensor, String aliasCamera) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
        executeUpdate("delete from Dispara where id_sensor_dispara=" + idSensor + " and alias_camara_disparada='" + aliasCamera + "'");
    }
    
    public ArrayList<Camara> getSensorCameras(int idSensor) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        ArrayList<String> aliasPairedUpCameras = getNameSensorCameras(idSensor);
        ArrayList<Camara> sensorCameras = new ArrayList<>();
        for(Camara camera : getCameras()){
            for(String aliasPairedUpCamera : aliasPairedUpCameras){
                if(camera.getAlias().equals(aliasPairedUpCamera)){
                    sensorCameras.add(camera);
                    break;
                }
            }
        }
        return sensorCameras;
    }
    
}
