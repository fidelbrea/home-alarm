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
package rmi;

public interface ServicioRmiInt {

    public boolean checkEmail(String email);

    public int getAlarmState();

    public boolean registerUser(String email, String token);

    public String getCamsList();

    public String getEventsList(String camera);

    public String getEventPictures(String camera, String event);

    public byte[] getPicture(String camera, String event, String picture);

    public boolean deleteEvent(String camera, String event);

    public boolean deletePicture(String camera, String event, String picture);

    public void shoot(String camera, int shoots);

    public String getSensorsList();

    public String getSensor(String alias);

    public void addCamera(String alias, String uri);

    public void deleteCamera(String alias);

    public void updateSensor(int id, String alias, boolean enabled, boolean delayed);

    public String getUsersList();

    public String getUser(String alias);

    public boolean addUser(String email, String alias);

    public boolean deleteUser(String alias);

    public void updateUserTag(String email, String tag);

    public void updateUserCode(String email, String code);

    public boolean updateUserAdmin(String email, boolean admin);

    public boolean isAdministrator(String email);

    public void armAlarm();

    public void disarmAlarm();

    public String getLastEvents(int limit);

    public String getSensorCams(String alias);

    public void updateSensorEnabled(String alias, boolean enabled);

    public void updateSensorDelayed(String alias, boolean delayed);

    public void associateSensorCamera(String aliasSensor, String aliasCamera);

    public void disassociateSensorCamera(String aliasSensor, String aliasCamera);

    public boolean modifyUserAlias(String oldAlias, String newAlias);

    public boolean modifyCameraAlias(String oldAlias, String newAlias);

    public boolean modifySensorAlias(String oldAlias, String newAlias);
}
