package fidelbrea.clientealarma.menuitem;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class MenuItem {

    private String text;
    private Drawable icon;
    private Bitmap picture;
    private int backgroundId;

    public MenuItem(String text, Drawable icon, int backgroundId){
        this.text = text;
        this.icon = icon;
        this.backgroundId = backgroundId;
        this.picture = null;
    }

    public String getText() {
        return text;
    }

    public void setTextId(String text) {
        this.text = text;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public int getBackgroundId() {
        return backgroundId;
    }

    public void setBackgroundId(int backgroundId) {
        this.backgroundId = backgroundId;
    }

    public Bitmap getPicture() {
        return picture;
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }

}
