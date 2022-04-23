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
package servidoralarma.servidor.hilos;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import servidoralarma.servidor.Servidor;

/**
 *
 * @author fidel
 */
public class MensajeFirebaseThread extends Thread {

    private final Servidor servidor;
    private ArrayList<String> tokens;
    private HashMap<String, Object> mapDataPayload;
    private String titulo;
    private String mensaje;

    private String claveServidor;
    private String urlFCM;
    private String androidChannel;
    private String uriImage;
    private Long timeToLive;

    public MensajeFirebaseThread(Servidor servidor) {
        this.servidor = servidor;
        this.tokens = new ArrayList<>();
        this.mapDataPayload = new HashMap<>();
        this.titulo = "";
        this.mensaje = "";

        String configJson = "";
        try {
            File fi = new File("./config.json");
            if (fi.exists()) {
                configJson = new String(Files.readAllBytes(fi.toPath()));
            }
        } catch (IOException e) {
            servidor.escribe("- error al cargar la configuracion (config.json) desde MensajeFirebaseThread");
            System.exit(-1);
        }

        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject;
            jsonObject = (JSONObject) parser.parse(configJson);
            if(jsonObject != null && jsonObject.containsKey("firebase")){
                JSONObject jsonFirebase = (JSONObject) jsonObject.get("firebase");
                this.claveServidor = (String) jsonFirebase.get("firebase_token");
                this.urlFCM = (String) jsonFirebase.get("url_fcm");
                this.androidChannel = (String) jsonFirebase.get("android_channel");
                this.uriImage = (String) jsonFirebase.get("uri_image");
                this.timeToLive = (Long) jsonFirebase.get("time_to_live");
            }
        } catch (ParseException e) {
            servidor.escribe("- error leyendo los datos de configuracion desde MensajeFirebaseThread.");
            System.exit(-1);
        }
    }

    @Override
    public void run() {
        HashMap<String, Object> mapNotificationPayload = new HashMap<>();
        if (titulo.length() > 0 && mensaje.length() > 0) {
            // ver Tabla 2b de https://firebase.google.com/docs/cloud-messaging/http-server-ref?hl=es
            mapNotificationPayload.put("title", titulo);
            mapNotificationPayload.put("body", mensaje);
            mapNotificationPayload.put("android_channel_id", androidChannel);
            mapNotificationPayload.put("click_action", "OPEN_ACTIVITY_1");
            mapNotificationPayload.put("image", uriImage);
        }
        if (!mapNotificationPayload.isEmpty() || !mapDataPayload.isEmpty()) {
            for (String token : tokens) {
                try {
                    servidor.escribe("enviando mensaje Firebase a usuario...");
                    URL url = new URL(urlFCM);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setUseCaches(false);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Authorization", "key=" + claveServidor);
                    conn.setRequestProperty("Content-Type", "application/json");
                    JSONObject jsonMensaje = new JSONObject();
                    jsonMensaje.put("to", token);
                    jsonMensaje.put("priority", "high");
                    jsonMensaje.put("time_to_live", timeToLive);
                    if (!mapNotificationPayload.isEmpty()) {
                        jsonMensaje.put("notification", new JSONObject(mapNotificationPayload));
                    }
                    if (!mapDataPayload.isEmpty()) {
                        jsonMensaje.put("data", new JSONObject(mapDataPayload));
                    }
                    try ( OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream())) {
                        wr.write(jsonMensaje.toJSONString());
                        wr.flush();
                    }
                    servidor.escribe("respuesta HTTP: " + conn.getResponseCode() + " - " + conn.getResponseMessage());
                    conn.disconnect();
                } catch (IOException e) {
                    servidor.escribe(e.toString());
                }
            }
        }
    }

    public ArrayList<String> getTokens() {
        return tokens;
    }

    public void setTokens(ArrayList<String> tokens) {
        this.tokens = tokens;
    }

    public HashMap<String, Object> getMapDataPayload() {
        return mapDataPayload;
    }

    public void setMapDataPayload(HashMap<String, Object> mapDataPayload) {
        this.mapDataPayload = mapDataPayload;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
