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

import com.fazecast.jSerialComm.SerialPort;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import servidoralarma.servidor.objetos.TipoMensaje;

/**
 *
 * @author fidel
 */
public class ManejadorJson {

    public ManejadorJson() {
    }

    public static TipoMensaje detectarTipoMensaje(String json) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject;
        jsonObject = (JSONObject) parser.parse(json);
        if(jsonObject != null && jsonObject.containsKey("tipo")){
            String tipo = (String) jsonObject.get("tipo");
            for (TipoMensaje tipoMensaje : TipoMensaje.values()) {
                if (tipoMensaje.toString().equals(tipo)) {
                    return TipoMensaje.valueOf(tipo);
                }
            }
        }
        return null;
    }

    public static String getDatosTexto(String json) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject;
        jsonObject = (JSONObject) parser.parse(json);
        return (String) jsonObject.get("datos");
    }

    public static long getDatosNumero(String json) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject;
        jsonObject = (JSONObject) parser.parse(json);
        return (long) jsonObject.get("datos");
    }

    public static Object getDatos(String json) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject;
        jsonObject = (JSONObject) parser.parse(json);
        return jsonObject.get("datos");
    }

    /**
     * Envia una cadena JSON a traves del puerto serie
     *
     * @param s puerto serie
     * @param json mensaje a enviar
     */
    public static void enviarJSON(SerialPort s, String json) {
        String[] mensajeDividido = json.split("(?<=\\G.{60})");
        /*
        java.util.regex (Regular Expression)
        
        Fuente: https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html
        
        Los parentesis permiten definir grupos con dos objetivos, uno para extraer
        la parte del input compatible y ademas poderle aplicar un operador de
        recurrencia sobre el grupo que se indique.
        
        (?<=X) X, a traves de una busqueda posterior positiva de ancho cero
        
        \G coincide con el punto donde finalizo la ultima coincidencia
        
        . cuarlquier simbolo (caracter), pero solamente uno.
        
        {60} exactamente 60 ocurrencias
        
         */
        for (String parte : mensajeDividido) {
            s.writeBytes(parte.getBytes(), parte.length());
            s.flushIOBuffers();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {}
        }
    }

    public static boolean isJsonString(String json) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(json);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

}
