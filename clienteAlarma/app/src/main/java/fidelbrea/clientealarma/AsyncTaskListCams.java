package fidelbrea.clientealarma;

import android.content.Context;
import android.os.AsyncTask;

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

    private AdapterMenuItem adapter;
    private Context context;
    private String eventName;
    private String cameraName;

    public AsyncTaskListCams(Context context, AdapterMenuItem adapter) {
        this.context = context;
        this.adapter = adapter;
    }

    protected String doInBackground(String... data) {
        String res = "";
        try {
            CallHandler callHandler = new CallHandler();
            Client client = new Client((data[0]), context.getResources().getInteger(R.integer.server_port), callHandler);
            ServicioRmiInt servicioRmiInt = (ServicioRmiInt) client.getGlobal(ServicioRmiInt.class);
            if(data.length == 3){
                cameraName = data[2];
                eventName = "";
                res = servicioRmiInt.getEventsList(data[2]);
            }else if(data.length == 4){
                cameraName = data[2];
                eventName = data[3];
                res = servicioRmiInt.getEventPictures(data[2], data[3]);
            }else {
                res = servicioRmiInt.getCamsList();
            }
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    protected void onPostExecute(String s){
        try {
            JSONObject jsonObject = new JSONObject(s);

            String[] arrays = {"cameras", "pictures", "events"};
            for(String arrayName : arrays){
                if(jsonObject.has(arrayName)) {
                    JSONArray jsonArray = jsonObject.getJSONArray(arrayName);
                    for(int i=0; i<jsonArray.length(); i++){
                        MenuItem menuItem = new MenuItem(jsonArray.getString(i), context.getDrawable(R.drawable.ic_camera_mask), R.drawable.ic_button_icon_mask);
                        if(arrayName.equals("events")) {
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
                        if(arrayName.equals("pictures")){
                            menuItem.setBackgroundId(R.drawable.ic_button_video_mask);
                            AsyncTaskGetCamPicture asyncTaskGetCamPicture = new AsyncTaskGetCamPicture(context, menuItem, adapter);
                            asyncTaskGetCamPicture.execute(context.getString(R.string.url_server), cameraName, eventName, jsonArray.getString(i));
                        }else {
                            adapter.add(menuItem);
                        }
                    }
                }
            }
            if(jsonObject.has("events"))
                adapter.add(new MenuItem((context.getString(R.string.shoot)), context.getDrawable(R.drawable.ic_shoot_mask), R.drawable.ic_button_icon_mask));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        adapter.add(new MenuItem(context.getString(R.string.back), context.getDrawable(R.drawable.ic_back_mask), R.drawable.ic_button_icon_mask));

    }
}


