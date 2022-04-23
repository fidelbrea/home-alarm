package fidelbrea.clientealarma;

import android.content.Context;
import android.os.AsyncTask;

import fidelbrea.clientealarma.menuitem.AdapterMenuItem;
import fidelbrea.clientealarma.menuitem.MenuItem;
import lipermi.handler.CallHandler;
import lipermi.net.Client;
import rmi.ServicioRmiInt;

public class AsyncTaskCheckIfAdministratorUser extends AsyncTask<String, Void, Boolean> {

    private Context context;
    private AdapterMenuItem adapterButtons;

    public AsyncTaskCheckIfAdministratorUser(
            Context context,
            AdapterMenuItem adapterButtons
    ) {
        this.context = context;
        this.adapterButtons = adapterButtons;
    }

    protected Boolean doInBackground(String... data) {
        Boolean res = false;
        try {
            CallHandler callHandler = new CallHandler();
            Client client = new Client(context.getString(R.string.url_server), context.getResources().getInteger(R.integer.server_port), callHandler);
            ServicioRmiInt servicioRmiInt = (ServicioRmiInt) client.getGlobal(ServicioRmiInt.class);
            res = servicioRmiInt.isAdministrator(data[0]);
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    protected void onPostExecute(Boolean res) {
        adapterButtons.add(new MenuItem(context.getString(R.string.alarm), context.getDrawable(R.drawable.ic_alarm_mask), R.drawable.ic_button_icon_mask));
        adapterButtons.add(new MenuItem(context.getString(R.string.cameras), context.getDrawable(R.drawable.ic_camera_mask), R.drawable.ic_button_icon_mask));
        adapterButtons.add(new MenuItem(context.getString(R.string.log), context.getDrawable(R.drawable.ic_log_mask), R.drawable.ic_button_icon_mask));
        if(res){
            adapterButtons.add(new MenuItem(context.getString(R.string.settings), context.getDrawable(R.drawable.ic_setting_mask), R.drawable.ic_button_icon_mask));
        }
        adapterButtons.add(new MenuItem(context.getString(R.string.exit), context.getDrawable(R.drawable.ic_exit_mask), R.drawable.ic_button_icon_mask));
    }
}


