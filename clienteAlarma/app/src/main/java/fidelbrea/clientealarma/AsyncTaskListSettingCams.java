package fidelbrea.clientealarma;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fidelbrea.clientealarma.menuitem.AdapterMenuItem;
import fidelbrea.clientealarma.menuitem.MenuItem;
import lipermi.handler.CallHandler;
import lipermi.net.Client;
import rmi.ServicioRmiInt;

public class AsyncTaskListSettingCams extends AsyncTask<String, Void, String> {

    private AdapterMenuItem adapter;
    private Context context;

    public AsyncTaskListSettingCams(Context context, AdapterMenuItem adapter) {
        this.context = context;
        this.adapter = adapter;
    }

    protected String doInBackground(String... data) {
        String res = "";
        try {
            CallHandler callHandler = new CallHandler();
            Client client = new Client((data[0]), context.getResources().getInteger(R.integer.server_port), callHandler);
            ServicioRmiInt servicioRmiInt = (ServicioRmiInt) client.getGlobal(ServicioRmiInt.class);
            res = servicioRmiInt.getCamsList();
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
                JSONArray jsonArray = jsonObject.getJSONArray("cameras");
                for (int i = 0; i < jsonArray.length(); i++) {
                    MenuItem menuItem = new MenuItem(jsonArray.getString(i), context.getDrawable(R.drawable.ic_camera_mask), R.drawable.ic_button_icon_mask);
                    adapter.add(menuItem);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter.add(new MenuItem((context.getString(R.string.add_cam)), context.getDrawable(R.drawable.ic_add_mask), R.drawable.ic_button_icon_mask));
        adapter.add(new MenuItem(context.getString(R.string.back), context.getDrawable(R.drawable.ic_back_mask), R.drawable.ic_button_icon_mask));
    }
}


