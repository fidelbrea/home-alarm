package fidelbrea.clientealarma.switchitem;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.Switch;

public class SwitchItem {

    private String text;
    private boolean enabled;
    private int backgroundId;
    private Switch switchItem;

    public SwitchItem(String text, boolean enabled, int backgroundId){
        this.text = text;
        this.enabled = enabled;
        this.backgroundId = backgroundId;
        switchItem = null;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getBackgroundId() {
        return backgroundId;
    }

    public void setBackgroundId(int backgroundId) {
        this.backgroundId = backgroundId;
    }

    public Switch getSwitchItem() {
        return switchItem;
    }

    public void setSwitchItem(Switch switchItem) {
        this.switchItem = switchItem;
    }
}
