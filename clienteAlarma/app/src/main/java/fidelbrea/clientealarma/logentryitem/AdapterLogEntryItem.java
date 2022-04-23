package fidelbrea.clientealarma.logentryitem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fidelbrea.clientealarma.R;

public class AdapterLogEntryItem extends RecyclerView.Adapter<HolderLogEntryItem> {

    private List<LogEntryItem> listLogEntries = new ArrayList<>();
    private Context context;

    public AdapterLogEntryItem(Context context) {
        this.context = context;
    }

    public void add(LogEntryItem logEntryItem){
        listLogEntries.add(logEntryItem);
        notifyItemInserted(listLogEntries.size());
    }

    public void remove(int position){
        listLogEntries.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, listLogEntries.size());
    }

    public void clear(){
        listLogEntries.clear();
        notifyItemInserted(listLogEntries.size());
    }

    @Override
    public HolderLogEntryItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.card_log_entry,parent,false);
        return new HolderLogEntryItem(v);
    }

    @Override
    public void onBindViewHolder(HolderLogEntryItem holder, int position) {
        holder.getTxtTimestamp().setText(listLogEntries.get(position).getTimestamp());
        holder.getTxtEvent().setText(listLogEntries.get(position).getEvent());
    }

    @Override
    public int getItemCount() {
        return listLogEntries.size();
    }

}
