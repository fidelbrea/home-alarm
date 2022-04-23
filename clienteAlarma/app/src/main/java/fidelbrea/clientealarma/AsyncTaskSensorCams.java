package fidelbrea.clientealarma;

import android.content.Context;
import android.os.AsyncTask;
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

    private Context context;
    private AdapterSwitchItem adapterSwitches;
    private AdapterMenuItem adapterButtons;
    private TextView title;

    public AsyncTaskSensorCams(Context context, AdapterSwitchItem adapterSwitches, AdapterMenuItem adapterButtons) {
        this.context = context;
        this.adapterSwitches = adapterSwitches;
        this.adapterButtons = adapterButtons;
    }

    protected String doInBackground(String... data) {
        String res = "";
        try {
            CallHandler callHandler = new CallHandler();
            Client client = new Client(context.getString(R.string.url_server), context.getResources().getInteger(R.integer.server_port), callHandler);
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
            if(jsonObject.has("cameras")) {
                JSONArray aCams = jsonObject.getJSONArray("cameras");
                for(int i=0; i<aCams.length(); i++) {
                    JSONObject jsonCam = aCams.getJSONObject(i);
                    if(jsonCam.has("alias") && jsonCam.has("paired_up")){
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


