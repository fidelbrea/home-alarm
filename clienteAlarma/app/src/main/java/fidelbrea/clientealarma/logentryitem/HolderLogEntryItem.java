package fidelbrea.clientealarma.logentryitem;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import fidelbrea.clientealarma.R;

public class HolderLogEntryItem extends RecyclerView.ViewHolder {

    private TextView txtTimestamp;
    private TextView txtEvent;

    public HolderLogEntryItem(@NonNull View itemView) {
        super(itemView);
        txtTimestamp = itemView.findViewById(R.id.txtTimestamp);
        txtEvent = itemView.findViewById(R.id.txtEvent);
    }

    public TextView getTxtTimestamp() {
        return txtTimestamp;
    }

    public void setTxtTimestamp(TextView txtTimestamp) {
        this.txtTimestamp = txtTimestamp;
    }

    public TextView getTxtEvent() {
        return txtEvent;
    }

    public void setTxtEvent(TextView txtEvent) {
        this.txtEvent = txtEvent;
    }
}
