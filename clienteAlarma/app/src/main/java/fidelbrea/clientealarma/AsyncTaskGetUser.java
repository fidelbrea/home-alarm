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

import org.json.JSONException;
import org.json.JSONObject;

import fidelbrea.clientealarma.menuitem.AdapterMenuItem;
import fidelbrea.clientealarma.menuitem.MenuItem;
import fidelbrea.clientealarma.switchitem.AdapterSwitchItem;
import fidelbrea.clientealarma.switchitem.SwitchItem;
import lipermi.handler.CallHandler;
import lipermi.net.Client;
import rmi.ServicioRmiInt;

public class AsyncTaskGetUser extends AsyncTask<String, Void, String> {

    private final Context context;
    private final AdapterSwitchItem adapterSwitches;
    private final AdapterMenuItem adapterButtons;
    private final TextView title;
    private final TextView email;
    private final TextView code;
    private final TextView tag;

    public AsyncTaskGetUser(
            Context context,
            AdapterSwitchItem adapterSwitches,
            AdapterMenuItem adapterButtons,
            TextView title,
            TextView email,
            TextView code,
            TextView tag
    ) {
        this.context = context;
        this.adapterSwitches = adapterSwitches;
        this.adapterButtons = adapterButtons;
        this.title = title;
        this.email = email;
        this.code = code;
        this.tag = tag;
        email.setText(R.string.empty);
        code.setText(R.string.empty);
        tag.setText(R.string.empty);
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
            res = servicioRmiInt.getUser(data[0]);
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    protected void onPostExecute(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            if (jsonObject.has("alias")) {
                title.setText(jsonObject.getString("alias"));
            }
            if (jsonObject.has("email")) {
                email.setText(jsonObject.getString("email"));
            }
            if (jsonObject.has("is_admin")) {
                SwitchItem switchItem = new SwitchItem(context.getString(R.string.is_admin), jsonObject.getBoolean("is_admin"), R.drawable.ic_button_mask);
                adapterSwitches.add(switchItem);
            }
            if (jsonObject.has("code")) {
                code.setText(jsonObject.getString("code"));
            }
            if (jsonObject.has("tag")) {
                tag.setText(jsonObject.getString("tag"));
            }

            adapterButtons.add(new MenuItem(context.getString(R.string.edit_alias), null, R.drawable.ic_button_mask));
            adapterButtons.add(new MenuItem(context.getString(R.string.modify_code), context.getDrawable(R.drawable.ic_code), R.drawable.ic_button_icon_mask));
            adapterButtons.add(new MenuItem(context.getString(R.string.modify_tag), context.getDrawable(R.drawable.ic_tag), R.drawable.ic_button_icon_mask));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapterButtons.add(new MenuItem(context.getString(R.string.back), context.getDrawable(R.drawable.ic_back_mask), R.drawable.ic_button_icon_mask));

    }
}


