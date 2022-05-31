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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import fidelbrea.clientealarma.menuitem.AdapterMenuItem;
import fidelbrea.clientealarma.menuitem.MenuItem;
import lipermi.handler.CallHandler;
import lipermi.net.Client;
import rmi.ServicioRmiInt;

public class AsyncTaskGetCamPicture extends AsyncTask<String, Void, Bitmap> {
    private final Context context;
    private final MenuItem menuItem;
    private final AdapterMenuItem adapter;

    public AsyncTaskGetCamPicture(Context context, MenuItem menuItem, AdapterMenuItem adapter) {
        this.context = context;
        this.menuItem = menuItem;
        this.adapter = adapter;
    }

    protected Bitmap doInBackground(String... data) {
        Bitmap bitmap = null;
        try {
            SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String confServerUrl = myPreferences.getString("URL_SERVER", "");
            Integer confServerPort = myPreferences.getInt("PORT_SERVER", 28803);
            CallHandler callHandler = new CallHandler();
            Client client = new Client(confServerUrl, confServerPort, callHandler);
            ServicioRmiInt servicioRmiInt = (ServicioRmiInt) client.getGlobal(ServicioRmiInt.class);
            byte[] res = servicioRmiInt.getPicture(data[0], data[1], data[2]);
            client.close();
            bitmap = BitmapFactory.decodeByteArray(res, 0, res.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    protected void onPostExecute(Bitmap result) {
        menuItem.setPicture(result);
        Bitmap bitMapResized = Bitmap.createScaledBitmap(result, 320, 180, false);
        Drawable d = new BitmapDrawable(context.getResources(), bitMapResized);
        menuItem.setIcon(d);
        adapter.add(menuItem);
    }

}


