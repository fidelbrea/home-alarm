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

public class AsyncTaskGetUser extends AsyncTask<String, Void, String> {

    private Context context;
    private AdapterSwitchItem adapterSwitches;
    private AdapterMenuItem adapterButtons;
    private TextView title;
    private TextView email;
    private TextView code;
    private TextView tag;

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
            CallHandler callHandler = new CallHandler();
            Client client = new Client(context.getString(R.string.url_server), context.getResources().getInteger(R.integer.server_port), callHandler);
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
            if(jsonObject.has("alias")) {
                title.setText(jsonObject.getString("alias"));
            }
            if(jsonObject.has("email")) {
                email.setText(jsonObject.getString("email"));
            }
            if(jsonObject.has("is_admin")){
                SwitchItem switchItem = new SwitchItem(context.getString(R.string.is_admin), jsonObject.getBoolean("is_admin"), R.drawable.ic_button_mask);
                adapterSwitches.add(switchItem);
            }
            if(jsonObject.has("code")) {
                code.setText(jsonObject.getString("code"));
            }
            if(jsonObject.has("tag")) {
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


