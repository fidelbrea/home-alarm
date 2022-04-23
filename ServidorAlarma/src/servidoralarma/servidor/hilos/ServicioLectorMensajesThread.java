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

import servidoralarma.servidor.Servidor;
import servidoralarma.servidor.objetos.Mensaje;

/**
 *
 * @author fidel
 */
public class ServicioLectorMensajesThread extends Thread {

    private final Servidor servidor;
    private boolean terminar;

    public ServicioLectorMensajesThread(Servidor servidor) {
        this.servidor = servidor;
        terminar = false;
    }

    @Override
    public synchronized void run() {
        servidor.escribe("servicio lector mensajes iniciado");
        Mensaje m;
        while (!terminar) {
            do {
                servidor.getMensajes().primero();
                m = (Mensaje) servidor.getMensajes().recuperar();
                if (m != null) {
                    servidor.getMensajes().eliminar(m);
                    servidor.escribe(m.toString(), true);
                    switch (m.getTipo()) {
                        case INICIO:
                            servidor.updateAlarmState();
                            servidor.setSensors();
                            break;
                        case ALARM_STATE:
                            servidor.handleAlarmState(m.getDatos().toString());
                            break;
                        case PREDISPARO:
                            servidor.getSensoresDisparados().add(Integer.parseInt(m.getDatos().toString()) + 1);
                            servidor.escribe("sensor " + (Integer.parseInt(m.getDatos().toString()) + 1) + " predisparado");
                            break;
                        case DISPARO:
                            if (Integer.parseInt(m.getDatos().toString()) != 999) {
                                servidor.getSensoresDisparados().add(Integer.parseInt(m.getDatos().toString()) + 1);
                                servidor.escribe("sensor " + (Integer.parseInt(m.getDatos().toString()) + 1) + " disparado");
                            }
                            servidor.handleTrigger();
                            break;
                        case CODIGO:
                            servidor.handleCode(m.getDatos().toString());
                            break;
                        case TAG:
                            servidor.handleTag(m.getDatos().toString());
                            break;
                        case FALLO_230:
                            //ToDo send notification
                            break;
                        case FALLO_BAT:
                            //ToDo send notification
                            break;
                        case SENSORS:
                            //Nothing to do, it is just info
                            break;
                        case RAM:
                            //Nothing to do, it is just info
                            break;
                        case LOOPS:
                            //Nothing to do, it is just info
                            break;
                    }
                }
            } while (m != null);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                servidor.escribe("- error LectorMensajes::" + ex.toString());
            }
        }
        servidor.escribe("servicio lector mensajes finalizado");
    }

    public void setTerminar(boolean terminar) {
        this.terminar = terminar;
    }

}
