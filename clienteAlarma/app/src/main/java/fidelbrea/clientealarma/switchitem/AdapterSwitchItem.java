package fidelbrea.clientealarma.switchitem;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fidelbrea.clientealarma.R;
import lipermi.handler.CallHandler;
import lipermi.net.Client;
import rmi.ServicioRmiInt;

public class AdapterSwitchItem extends RecyclerView.Adapter<HolderSwitchItem> {

    private final String ON_PREFIX = "on_";
    private final String OFF_PREFIX = "off_";
    private List<SwitchItem> listSwitchItem = new ArrayList<>();
    private Context context;

    public AdapterSwitchItem(Context context) {
        this.context = context;
    }

    public void add(SwitchItem switchItem){
        listSwitchItem.add(switchItem);
        notifyItemInserted(listSwitchItem.size());
    }

    public void remove(int position){
        listSwitchItem.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, listSwitchItem.size());
    }

    public List<SwitchItem> getListButtonApp() {
        return listSwitchItem;
    }

    public void clear(){
        listSwitchItem.clear();
        notifyItemInserted(listSwitchItem.size());
    }



    @Override
    public HolderSwitchItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.card_switch,parent,false);
        return new HolderSwitchItem(v);
    }

    @Override
    public void onBindViewHolder(HolderSwitchItem holder, int position) {
        holder.getImgBackGround().setImageDrawable(context.getDrawable(listSwitchItem.get(position).getBackgroundId()));
        holder.getTxtText().setText(listSwitchItem.get(position).getText());
        holder.getSwitchItem().setChecked(listSwitchItem.get(position).isEnabled());
        holder.getSwitchItem().setTextOn(ON_PREFIX + listSwitchItem.get(position).getText());
        holder.getSwitchItem().setTextOff(OFF_PREFIX + listSwitchItem.get(position).getText());
        holder.getSwitchItem().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String itemText = ((Switch)buttonView).getTextOn().toString().substring(ON_PREFIX.length());
                int position = -1;
                for(int i=0; i<listSwitchItem.size(); i++){
                    if(listSwitchItem.get(i).getText().equals(itemText)){
                        position = i;
                        break;
                    }
                }
                listSwitchItem.get(position).setEnabled(isChecked);
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            CallHandler callHandler = new CallHandler();
                            Client client = new Client(context.getString(R.string.url_server), context.getResources().getInteger(R.integer.server_port), callHandler);
                            ServicioRmiInt servicioRmiInt = (ServicioRmiInt) client.getGlobal(ServicioRmiInt.class);
                            if(itemText.equals(context.getString(R.string.is_admin))) {
                                // User is administrator
                                servicioRmiInt.updateUserAdmin(((TextView) ((Activity) context).findViewById(R.id.textEmail)).getText().toString(), isChecked);
                            }else if(itemText.equals(context.getString(R.string.enabled))) {
                                // Sensor enabled
                                servicioRmiInt.updateSensorEnabled(((TextView) ((Activity) context).findViewById(R.id.pageTitle)).getText().toString(), isChecked);
                            }else if(itemText.equals(context.getString(R.string.delayed))) {
                                // Sensor delayed
                                servicioRmiInt.updateSensorDelayed(((TextView) ((Activity) context).findViewById(R.id.pageTitle)).getText().toString(), isChecked);
                            }else {
                                // It's a camera alias
                                if(isChecked){
                                    // associate sensor-camera
                                    servicioRmiInt.associateSensorCamera(((TextView) ((Activity) context).findViewById(R.id.pageTitle)).getText().toString(), itemText);
                                }else{
                                    // disassociate sensor-camera
                                    servicioRmiInt.disassociateSensorCamera(((TextView) ((Activity) context).findViewById(R.id.pageTitle)).getText().toString(), itemText);
                                }
                            }
                                client.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });
        listSwitchItem.get(position).setSwitchItem(holder.getSwitchItem());
    }

    @Override
    public int getItemCount() {
        return listSwitchItem.size();
    }

}
