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

/**
 *
 * @author fidel
 */
public class Usuario {

    private final String email;
    private String alias;
    private boolean administrador;
    private long tagRFID;
    private String codigo;
    private String token;

    public Usuario(String email, String alias, boolean administrador, long tagRFID, String codigo, String token) {
        this.email = email;
        this.alias = alias;
        this.administrador = administrador;
        this.tagRFID = tagRFID;
        this.codigo = codigo;
        this.token = token;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean isAdministrador() {
        return administrador;
    }

    public void setAdministrador(boolean administrador) {
        this.administrador = administrador;
    }

    public long getTagRFID() {
        return tagRFID;
    }

    public void setTagRFID(long tagRFID) {
        this.tagRFID = tagRFID;
    }

    public String getEmail() {
        return email;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
