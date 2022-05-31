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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import fidelbrea.clientealarma.menuitem.AdapterMenuItem;
import fidelbrea.clientealarma.menuitem.MenuItem;
import lipermi.handler.CallHandler;
import lipermi.net.Client;
import rmi.ServicioRmiInt;

public class AsyncTaskListSettingUsers extends AsyncTask<String, Void, String> {

    private final AdapterMenuItem adapter;
    private final Context context;

    public AsyncTaskListSettingUsers(Context context, AdapterMenuItem adapter) {
        this.context = context;
        this.adapter = adapter;
    }

    protected String doInBackground(String... data) {
        String res = "";
        try {
            SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String confServerUrl = myPreferences.getString("URL_SERVER", "");
            Integer confServerPort = myPreferences.getInt("PORT_SERVER", 28803);
            CallHandler callHandler = new CallHandler();
            Client client = new Client(confServerUrl, confServerPort, callHandler);
            ServicioRmiInt servicioRmiInt = (ServicioRmiInt) client.getGlobal(ServicioRmiInt.class);
            res = servicioRmiInt.getUsersList();
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    protected void onPostExecute(String s) {
        try {
            ArrayList<String> sensorsList = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(s);
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                sensorsList.add(key);
            }
            Collections.sort(sensorsList);
            for (String sensor : sensorsList) {
                boolean admin = jsonObject.getBoolean(sensor);
                MenuItem menuItem = new MenuItem(sensor, context.getDrawable((admin) ? R.drawable.ic_user_admin : R.drawable.ic_user), R.drawable.ic_button_icon_mask);
                adapter.add(menuItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter.add(new MenuItem((context.getString(R.string.add_user)), context.getDrawable(R.drawable.ic_add_mask), R.drawable.ic_button_icon_mask));
        adapter.add(new MenuItem(context.getString(R.string.back), context.getDrawable(R.drawable.ic_back_mask), R.drawable.ic_button_icon_mask));
    }
}


