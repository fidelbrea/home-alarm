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
package servidoralarma.servidor.objetos;

import java.io.File;
import java.util.Arrays;

/**
 *
 * @author fidel
 */
public class Camara {

    private String alias;
    private String uri;
    private String camPath;

    public Camara(String alias, String uri) {
        this.alias = alias.trim();
        this.uri = uri;
        this.camPath = "./cam/" + this.alias.replace(' ', '_').toLowerCase() + "/";
        File dir = new File(this.camPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public String[] getEventsList() {
        File folder = new File(camPath);
        if (folder.exists()) {
            String[] res = folder.list();
            Arrays.sort(res);
            return res;
        }
        return null;
    }

    public String[] getEventPictures(String event) {
        File folder = new File(camPath + event.toLowerCase() + "/");
        if (folder.exists()) {
            String[] res = folder.list();
            Arrays.sort(res);
            return res;
        }
        return null;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
        updateCamPath();
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getCamPath() {
        return camPath;
    }

    public void updateCamPath() {
        this.camPath = "./cam/" + this.alias.replace(' ', '_').toLowerCase() + "/";
        File dir = new File(this.camPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

}
