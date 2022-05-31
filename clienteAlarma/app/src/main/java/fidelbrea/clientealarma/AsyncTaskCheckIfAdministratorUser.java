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

package fidelbrea.clientealarma;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import fidelbrea.clientealarma.menuitem.AdapterMenuItem;
import fidelbrea.clientealarma.menuitem.MenuItem;
import lipermi.handler.CallHandler;
import lipermi.net.Client;
import rmi.ServicioRmiInt;

public class AsyncTaskCheckIfAdministratorUser extends AsyncTask<String, Void, Boolean> {

    private final Context context;
    private final AdapterMenuItem adapterButtons;
    private String confServerUrl;
    private Integer confServerPort;

    public AsyncTaskCheckIfAdministratorUser(
            Context context,
            AdapterMenuItem adapterButtons
    ) {
        this.context = context;
        this.adapterButtons = adapterButtons;
    }

    protected Boolean doInBackground(String... data) {
        Boolean res = false;
        try {
            SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            confServerUrl = myPreferences.getString("URL_SERVER", "");
            confServerPort = myPreferences.getInt("PORT_SERVER", 28803);
            if(confServerUrl.equals("")){
                res = true;
            }else {
                CallHandler callHandler = new CallHandler();
                Client client = new Client(confServerUrl, confServerPort, callHandler);
                ServicioRmiInt servicioRmiInt = (ServicioRmiInt) client.getGlobal(ServicioRmiInt.class);
                res = servicioRmiInt.isAdministrator(data[0]);
                client.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    protected void onPostExecute(Boolean res) {
        if(!confServerUrl.equals("")) {
            adapterButtons.add(new MenuItem(context.getString(R.string.alarm), context.getDrawable(R.drawable.ic_alarm_mask), R.drawable.ic_button_icon_mask));
            adapterButtons.add(new MenuItem(context.getString(R.string.cameras), context.getDrawable(R.drawable.ic_camera_mask), R.drawable.ic_button_icon_mask));
            adapterButtons.add(new MenuItem(context.getString(R.string.log), context.getDrawable(R.drawable.ic_log_mask), R.drawable.ic_button_icon_mask));
            if (res) {
                adapterButtons.add(new MenuItem(context.getString(R.string.settings), context.getDrawable(R.drawable.ic_setting_mask), R.drawable.ic_button_icon_mask));
            }
        }else{
            adapterButtons.add(new MenuItem(context.getString(R.string.settings), context.getDrawable(R.drawable.ic_setting_mask), R.drawable.ic_button_icon_mask));
        }
        adapterButtons.add(new MenuItem(context.getString(R.string.exit), context.getDrawable(R.drawable.ic_exit_mask), R.drawable.ic_button_icon_mask));
    }
}


