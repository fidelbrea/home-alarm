/*
 * Copyright (C) 2022 Fidel Brea Montilla (fidelbreamontilla@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
