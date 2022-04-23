/*
 * LipeRMI - a light weight Internet approach for remote method invocation
 * Copyright (C) 2006  Felipe Santos Andrade
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * For more information, see http://lipermi.sourceforge.net/license.php
 * You can also contact author through lipeandrade@users.sourceforge.net
 */
package lipermi.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import lipermi.handler.CallHandler;
import lipermi.handler.ConnectionHandler;
import lipermi.handler.IConnectionHandlerListener;
import lipermi.handler.filter.DefaultFilter;
import lipermi.handler.filter.IProtocolFilter;

/**
 * The LipeRMI server. This object listen to a specific port and when a client
 * connects it delegates the connection to a
 * {@link lipermi.handler.ConnectionHandler ConnectionHandler}.
 *
 * @author lipe
 * @date 05/10/2006
 *
 * @see lipermi.handler.CallHandler
 * @see lipermi.net.Client
 */
public class Server {

    private ServerSocket serverSocket;
    private boolean enabled;
    private List<IServerListener> listeners = new LinkedList<IServerListener>();

    public void addServerListener(IServerListener listener) {
        listeners.add(listener);
    }

    public void removeServerListener(IServerListener listener) {
        listeners.remove(listener);
    }

    public void close() {
        enabled = false;
    }

    public void bind(int port, CallHandler callHandler) throws IOException {
        bind(port, callHandler, new DefaultFilter());
    }

    public void bind(final int port, final CallHandler callHandler, final IProtocolFilter filter) throws IOException {
        enabled = true;
        serverSocket = new ServerSocket();
        serverSocket.setPerformancePreferences(1, 0, 2); // indicate the relative importance of (short connection time, low latency, and high bandwidth)
        serverSocket.bind(new InetSocketAddress(port));

        Thread bindThread = new Thread(new Runnable() {
            public void run() {
                while (enabled) {
                    Socket acceptSocket = null;
                    try {
                        acceptSocket = serverSocket.accept(); // accion bloqueante

                        final Socket clientSocket = acceptSocket;
                        ConnectionHandler.createConnectionHandler(clientSocket, callHandler, filter, new IConnectionHandlerListener() {
                            @Override
                            public void connectionClosed() {
                                for (IServerListener listener : listeners) {
                                    listener.clientDisconnected(clientSocket);
                                }
                            }
                        });
                        for (IServerListener listener : listeners) {
                            listener.clientConnected(clientSocket);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, String.format("alarma::ServicioRMI (%d)", port));
        bindThread.start();
    }

}
