package fidelbrea.clientealarma.menuitem;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import fidelbrea.clientealarma.R;

public class HolderMenuItem extends RecyclerView.ViewHolder {

    private TextView txtText;
    private ImageView imgIcon;
    private ImageView imgBackGround;

    public HolderMenuItem(@NonNull View itemView) {
        super(itemView);
        txtText = itemView.findViewById(R.id.txtText);
        imgIcon = itemView.findViewById(R.id.imgIcon);
        imgBackGround = itemView.findViewById(R.id.imgBackGround);
    }

    public TextView getTxtText() {
        return txtText;
    }

    public void setTxtText(TextView txtText) {
        this.txtText = txtText;
    }

    public ImageView getImgIcon() {
        return imgIcon;
    }

    public void setImgIcon(ImageView imgIcon) {
        this.imgIcon = imgIcon;
    }

    public ImageView getImgBackGround() {
        return imgBackGround;
    }

    public void setImgBackGround(ImageView imgBackGround) {
        this.imgBackGround = imgBackGround;
    }
}
