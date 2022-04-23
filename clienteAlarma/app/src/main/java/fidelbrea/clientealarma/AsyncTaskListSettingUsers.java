package fidelbrea.clientealarma;

import android.content.Context;
import android.os.AsyncTask;

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

    private AdapterMenuItem adapter;
    private Context context;

    public AsyncTaskListSettingUsers(Context context, AdapterMenuItem adapter) {
        this.context = context;
        this.adapter = adapter;
    }

    protected String doInBackground(String... data) {
        String res = "";
        try {
            CallHandler callHandler = new CallHandler();
            Client client = new Client((data[0]), context.getResources().getInteger(R.integer.server_port), callHandler);
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
            while(keys.hasNext()) {
                String key = keys.next();
                sensorsList.add(key);
            }
            Collections.sort(sensorsList);
            for(String sensor : sensorsList) {
                boolean admin = jsonObject.getBoolean(sensor);
                MenuItem menuItem = new MenuItem(sensor, context.getDrawable((admin)?R.drawable.ic_user_admin:R.drawable.ic_user), R.drawable.ic_button_icon_mask);
                adapter.add(menuItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter.add(new MenuItem((context.getString(R.string.add_user)), context.getDrawable(R.drawable.ic_add_mask), R.drawable.ic_button_icon_mask));
        adapter.add(new MenuItem(context.getString(R.string.back), context.getDrawable(R.drawable.ic_back_mask), R.drawable.ic_button_icon_mask));
    }
}

