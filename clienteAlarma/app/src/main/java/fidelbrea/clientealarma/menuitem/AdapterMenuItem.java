package fidelbrea.clientealarma.menuitem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fidelbrea.clientealarma.R;

public class AdapterMenuItem extends RecyclerView.Adapter<HolderMenuItem> {

    private List<MenuItem> listMenuItem = new ArrayList<>();
    private Context context;

    public AdapterMenuItem(Context context) {
        this.context = context;
    }

    public void add(MenuItem menuItem){
        listMenuItem.add(menuItem);
        notifyItemInserted(listMenuItem.size());
    }

    public void remove(int position){
        listMenuItem.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, listMenuItem.size());
    }

    public List<MenuItem> getListButtonApp() {
        return listMenuItem;
    }

    public void clear(){
        listMenuItem.clear();
        notifyItemInserted(listMenuItem.size());
    }

    @Override
    public HolderMenuItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.card_button_app,parent,false);
        return new HolderMenuItem(v);
    }

    @Override
    public void onBindViewHolder(HolderMenuItem holder, int position) {
        holder.getImgBackGround().setImageDrawable(context.getDrawable(listMenuItem.get(position).getBackgroundId()));
        holder.getImgIcon().setImageDrawable(listMenuItem.get(position).getIcon());
        holder.getTxtText().setText(listMenuItem.get(position).getText());

        float density = context.getResources().getDisplayMetrics().density;
        int width = context.getResources().getDisplayMetrics().widthPixels;
        int widthCorrected = (int) (width / density);
        float scale = (float) ((widthCorrected) / 400.0f);
        switch(listMenuItem.get(position).getBackgroundId()){
            case R.drawable.ic_button_video_mask:
                holder.getImgIcon().setPaddingRelative((int) (scale * 110), 0, 0, 0);
                holder.getTxtText().setTextSize(14);
                break;
            case R.drawable.ic_button_icon_mask:
                holder.getImgIcon().setScaleX(scale);
                holder.getImgIcon().setScaleY(scale);
                holder.getImgIcon().setPaddingRelative((int) (scale * 130), 0, 0, 0);
                break;
            case R.drawable.ic_button_mask:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return listMenuItem.size();
    }

}
