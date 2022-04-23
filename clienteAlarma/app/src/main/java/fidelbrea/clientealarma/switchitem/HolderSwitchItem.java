package fidelbrea.clientealarma.switchitem;

import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import fidelbrea.clientealarma.R;

public class HolderSwitchItem extends RecyclerView.ViewHolder {

    private TextView txtText;
    private Switch switchItem;
    private ImageView imgBackGround;

    public HolderSwitchItem(@NonNull View itemView) {
        super(itemView);
        txtText = itemView.findViewById(R.id.txtText);
        switchItem = itemView.findViewById(R.id.switchItem);
        imgBackGround = itemView.findViewById(R.id.imgBackGround);
    }

    public TextView getTxtText() {
        return txtText;
    }

    public void setTxtText(TextView txtText) {
        this.txtText = txtText;
    }

    public Switch getSwitchItem() {
        return switchItem;
    }

    public void setSwitchItem(Switch switchItem) {
        this.switchItem = switchItem;
    }

    public ImageView getImgBackGround() {
        return imgBackGround;
    }

    public void setImgBackGround(ImageView imgBackGround) {
        this.imgBackGround = imgBackGround;
    }
}
