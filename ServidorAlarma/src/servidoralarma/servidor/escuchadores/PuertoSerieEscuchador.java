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
package servidoralarma.servidor.escuchadores;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.json.simple.parser.ParseException;
import servidoralarma.servidor.Servidor;
import servidoralarma.servidor.objetos.Mensaje;
import servidoralarma.servidor.objetos.TipoMensaje;
import servidoralarma.servidor.utils.ManejadorJson;

/**
 *
 * @author fidel
 */
public class PuertoSerieEscuchador implements SerialPortDataListener {

    private final String BOL = "{"; // Begining Of Line
    private final String EOL = "}"; // End Of Line           // "\r\n";
    private final Servidor servidor;
    private String mensaje;

    public PuertoSerieEscuchador(Servidor servidor) {
        this.servidor = servidor;
        this.mensaje = "";
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        SerialPort comPort = event.getSerialPort();
        byte[] newData = new byte[comPort.bytesAvailable()];
        comPort.readBytes(newData, newData.length);
        for (byte b : newData) {
            mensaje += (char) b;
        }

        // si no contiene el inicio de un mensaje JSON, no nos vale
        if (!mensaje.contains(BOL)) {
            mensaje = "";
        } else {
            // solamente nos sirve desde el comiendo del mensaje (inclusive)
            mensaje = mensaje.substring(mensaje.indexOf(BOL));

            if (mensaje.contains(EOL)) {
                int posEol = 0;
                while (mensaje.indexOf(EOL, posEol) > posEol) {
                    posEol = mensaje.indexOf(EOL, posEol);
                    String msgJSON = mensaje.substring(0, posEol + EOL.length());
                    if (ManejadorJson.isJsonString(msgJSON)) {
                        mensaje = mensaje.substring(posEol + EOL.length());
                        gestionarJSON(msgJSON);
                    }
                }
            }
        }
    }

    private void gestionarJSON(String msgJSON) {
        try {
            TipoMensaje tipoMensaje = ManejadorJson.detectarTipoMensaje(msgJSON);
            if (tipoMensaje != null) {
                Mensaje msg = new Mensaje(tipoMensaje, ManejadorJson.getDatos(msgJSON));
                servidor.getMensajes().insertar((Mensaje) msg);
                servidor.escribe("Meto un mensaje " + tipoMensaje.toString() + " en la lista.");
            }
        } catch (ParseException e) {
            servidor.escribe(e.toString());
        }
    }
}
