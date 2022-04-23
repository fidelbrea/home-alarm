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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import servidoralarma.servidor.objetos.Camara;

/**
 *
 * @author fidel
 */
public class CapturadorImagenesThread extends Thread {

    private final Camara cam;
    private final String nombreEvento;
    private final int cantidadDeImagenes;

    public CapturadorImagenesThread(Camara cam, int amountOfPictures) {
        this.cam = cam;
        this.cantidadDeImagenes = (amountOfPictures < 10) ? amountOfPictures : 10;
        nombreEvento = getTimeStamp();
    }

    @Override
    public synchronized void run() {
        try {
            if (cam.getUri().startsWith("rtsp://")) {
                String path = cam.getCamPath() + ((nombreEvento.length() > 0) ? nombreEvento.toLowerCase() + "/" : "");
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                // ffmpeg -y -i rtsp://fidelbrea:udima22@192.168.1.87/stream1 -r 1 -frames:v 10 -strftime 1 %Y%m%d%H%M%S.jpg
                List<String> commands = new ArrayList<>();
                commands.add("ffmpeg");
                commands.add("-y");
                commands.add("-i");
                commands.add(cam.getUri());
                commands.add("-r");
                commands.add("1");
                commands.add("-frames:v");
                commands.add("10");
                commands.add("-strftime");
                commands.add("1");
                commands.add(path + "%Y%m%d%H%M%S.jpg");
                ProcessBuilder builder = new ProcessBuilder();
                builder.command(commands);
                Process process = builder.start();
                process.waitFor(30, TimeUnit.SECONDS);
            } else {
                for (int idPicture = 1; idPicture <= cantidadDeImagenes; idPicture++) {
                    long ts = System.currentTimeMillis();
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                    String path = cam.getCamPath() + ((nombreEvento.length() > 0) ? nombreEvento.toLowerCase() + "/" : "");
                    String file = ((nombreEvento.length() > 0) ? idPicture + "_" : "") + dtf.format(LocalDateTime.now()) + ".jpg";
                    File dir = new File(path);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    InputStream is = new URL(cam.getUri()).openStream();
                    OutputStream os = new FileOutputStream(path + file, false);
                    byte[] buffer = new byte[2048];
                    int length;
                    while ((length = is.read(buffer)) != -1) {
                        os.write(buffer, 0, length);
                    }
                    is.close();
                    os.close();
                    while ((System.currentTimeMillis() - ts) < 1000) {
                    }
                }
            }
        } catch (InterruptedException | IOException e) {
            System.out.println(e.toString());
        }
    }

    private String getTimeStamp() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return dtf.format(LocalDateTime.now());
    }

}
