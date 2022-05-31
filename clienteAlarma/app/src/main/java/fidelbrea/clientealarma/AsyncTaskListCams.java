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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import fidelbrea.clientealarma.menuitem.AdapterMenuItem;
import fidelbrea.clientealarma.menuitem.MenuItem;
import lipermi.handler.CallHandler;
import lipermi.net.Client;
import rmi.ServicioRmiInt;

public class AsyncTaskListCams extends AsyncTask<String, Void, String> {

    private final AdapterMenuItem adapter;
    private final Context context;
    private String eventName;
    private String cameraName;

    public AsyncTaskListCams(Context context, AdapterMenuItem adapter) {
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
            if (data.length == 1) {
                cameraName = data[0];
                eventName = "";
                res = servicioRmiInt.getEventsList(data[0]);
            } else if (data.length == 2) {
                cameraName = data[0];
                eventName = data[1];
                res = servicioRmiInt.getEventPictures(data[0], data[1]);
            } else {
                res = servicioRmiInt.getCamsList();
            }
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    protected void onPostExecute(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);

            String[] arrays = {"cameras", "pictures", "events"};
            for (String arrayName : arrays) {
                if (jsonObject.has(arrayName)) {
                    JSONArray jsonArray = jsonObject.getJSONArray(arrayName);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        MenuItem menuItem = new MenuItem(jsonArray.getString(i), context.getDrawable(R.drawable.ic_camera_mask), R.drawable.ic_button_icon_mask);
                        if (arrayName.equals("events")) {
                            menuItem.setIcon(context.getDrawable(R.drawable.ic_folder_mask));
                            String menuText = jsonArray.getString(i);
                            try {
                                Date date;
                                SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                                date = df.parse(String.valueOf(jsonArray.getString(i)));
                                df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                                menuText = df.format(date);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            menuItem.setTextId(menuText);
                        }
                        if (arrayName.equals("pictures")) {
                            menuItem.setBackgroundId(R.drawable.ic_button_video_mask);
                            AsyncTaskGetCamPicture asyncTaskGetCamPicture = new AsyncTaskGetCamPicture(context, menuItem, adapter);
                            asyncTaskGetCamPicture.execute(cameraName, eventName, jsonArray.getString(i));
                        } else {
                            adapter.add(menuItem);
                        }
                    }
                }
            }
            if (jsonObject.has("events"))
                adapter.add(new MenuItem((context.getString(R.string.shoot)), context.getDrawable(R.drawable.ic_shoot_mask), R.drawable.ic_button_icon_mask));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter.add(new MenuItem(context.getString(R.string.back), context.getDrawable(R.drawable.ic_back_mask), R.drawable.ic_button_icon_mask));

    }
}


