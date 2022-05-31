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
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fidelbrea.clientealarma.menuitem.AdapterMenuItem;
import fidelbrea.clientealarma.menuitem.MenuItem;
import fidelbrea.clientealarma.switchitem.AdapterSwitchItem;
import fidelbrea.clientealarma.switchitem.SwitchItem;
import lipermi.handler.CallHandler;
import lipermi.net.Client;
import rmi.ServicioRmiInt;

public class AsyncTaskSensorCams extends AsyncTask<String, Void, String> {

    private final Context context;
    private final AdapterSwitchItem adapterSwitches;
    private final AdapterMenuItem adapterButtons;
    private TextView title;

    public AsyncTaskSensorCams(Context context, AdapterSwitchItem adapterSwitches, AdapterMenuItem adapterButtons) {
        this.context = context;
        this.adapterSwitches = adapterSwitches;
        this.adapterButtons = adapterButtons;
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
            res = servicioRmiInt.getSensorCams(data[0]);
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    protected void onPostExecute(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            if (jsonObject.has("cameras")) {
                JSONArray aCams = jsonObject.getJSONArray("cameras");
                for (int i = 0; i < aCams.length(); i++) {
                    JSONObject jsonCam = aCams.getJSONObject(i);
                    if (jsonCam.has("alias") && jsonCam.has("paired_up")) {
                        SwitchItem switchItem = new SwitchItem(jsonCam.getString("alias"), jsonCam.getBoolean("paired_up"), R.drawable.ic_button_mask);
                        adapterSwitches.add(switchItem);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapterButtons.add(new MenuItem(context.getString(R.string.back), context.getDrawable(R.drawable.ic_back_mask), R.drawable.ic_button_icon_mask));
    }
}


