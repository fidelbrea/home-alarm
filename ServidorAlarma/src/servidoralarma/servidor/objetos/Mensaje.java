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

import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.simple.parser.ParseException;
import servidoralarma.servidor.utils.ManejadorJson;

/**
 *
 * @author fidel
 */
public class Mensaje implements Comparable {

    private long tsRecibido;
    private TipoMensaje tipo;
    private Object datos;

    public Mensaje(TipoMensaje tipo, Object datos) {
        tsRecibido = System.currentTimeMillis();
        this.tipo = tipo;
        this.datos = datos;
    }

    public Mensaje(String msgJSON) throws ParseException {
        tsRecibido = System.currentTimeMillis();
        tipo = ManejadorJson.detectarTipoMensaje(msgJSON);
        if (tipo != null) {
            datos = ManejadorJson.getDatos(msgJSON);
        } else {
            datos = null;
        }
    }

    public long getTsRecibido() {
        return tsRecibido;
    }

    public void setTsRecibido(long tsRecibido) {
        this.tsRecibido = tsRecibido;
    }

    public TipoMensaje getTipo() {
        return tipo;
    }

    public void setTipo(TipoMensaje tipo) {
        this.tipo = tipo;
    }

    public Object getDatos() {
        return datos;
    }

    public void setDatos(Object datos) {
        this.datos = datos;
    }

    @Override
    public String toString() {
        String res = "";
        Date ts = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        res += "Tipo msg..: " + tipo.name();
        ts.setTime(tsRecibido);
        res += "\nRecibido..: " + sdf.format(ts);
        res += "\nDatos.....: " + ((datos != null)?datos.toString():"<null>");
        return res;
    }

    @Override
    public int hashCode() {
        return this.tipo.ordinal();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Mensaje other = (Mensaje) obj;
        return this.tipo == other.tipo;
    }
    
    @Override
    public int compareTo(Object o) {
        return (this.hashCode()-o.hashCode());
    }

}