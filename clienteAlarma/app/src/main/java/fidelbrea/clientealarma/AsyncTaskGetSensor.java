package fidelbrea.clientealarma;

import android.content.Context;
import android.os.AsyncTask;
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

public class AsyncTaskGetSensor extends AsyncTask<String, Void, String> {

    private Context context;
    private AdapterSwitchItem adapterSwitches;
    private AdapterMenuItem adapterButtons;
    private TextView title;

    public AsyncTaskGetSensor(Context context, AdapterSwitchItem adapterSwitches, AdapterMenuItem adapterButtons, TextView title) {
        this.context = context;
        this.adapterSwitches = adapterSwitches;
        this.adapterButtons = adapterButtons;
        this.title = title;
    }

    protected String doInBackground(String... data) {
        String res = "";
        try {
            CallHandler callHandler = new CallHandler();
            Client client = new Client((data[0]), context.getResources().getInteger(R.integer.server_port), callHandler);
            ServicioRmiInt servicioRmiInt = (ServicioRmiInt) client.getGlobal(ServicioRmiInt.class);
            res = servicioRmiInt.getSensor(data[1]);
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    protected void onPostExecute(String s) {
        try {
            JSONObject jsonObject = new JSONObject(s);
            if(jsonObject.has("alias")) {
                title.setText(jsonObject.getString("alias"));
            }
            if(jsonObject.has("enabled")){
                SwitchItem switchItem = new SwitchItem(context.getString(R.string.enabled), jsonObject.getBoolean("enabled"), R.drawable.ic_button_mask);
                adapterSwitches.add(switchItem);
            }
            if(jsonObject.has("delayed")){
                SwitchItem switchItem = new SwitchItem(context.getString(R.string.delayed), jsonObject.getBoolean("delayed"), R.drawable.ic_button_mask);
                adapterSwitches.add(switchItem);
            }

            adapterButtons.add(new MenuItem(context.getString(R.string.edit_alias), null, R.drawable.ic_button_mask));
            adapterButtons.add(new MenuItem(context.getString(R.string.associate_with_cams), null, R.drawable.ic_button_mask));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapterButtons.add(new MenuItem(context.getString(R.string.back), context.getDrawable(R.drawable.ic_back_mask), R.drawable.ic_button_icon_mask));

    }
}


