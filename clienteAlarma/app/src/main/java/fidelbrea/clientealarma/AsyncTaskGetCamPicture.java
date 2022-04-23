package fidelbrea.clientealarma;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import fidelbrea.clientealarma.menuitem.AdapterMenuItem;
import fidelbrea.clientealarma.menuitem.MenuItem;
import lipermi.handler.CallHandler;
import lipermi.net.Client;
import rmi.ServicioRmiInt;

public class AsyncTaskGetCamPicture extends AsyncTask<String, Void, Bitmap> {
    private Context context;
    private MenuItem menuItem;
    private AdapterMenuItem adapter;

    public AsyncTaskGetCamPicture(Context context, MenuItem menuItem, AdapterMenuItem adapter) {
        this.context = context;
        this.menuItem = menuItem;
        this.adapter = adapter;
    }

    protected Bitmap doInBackground(String... data) {
        Bitmap bitmap = null;
        try {
            CallHandler callHandler = new CallHandler();
            Client client = new Client(((String)data[0]), context.getResources().getInteger(R.integer.server_port), callHandler);
            ServicioRmiInt servicioRmiInt = (ServicioRmiInt) client.getGlobal(ServicioRmiInt.class);
            byte[] res = servicioRmiInt.getPicture(data[1], data[2], data[3]);
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


