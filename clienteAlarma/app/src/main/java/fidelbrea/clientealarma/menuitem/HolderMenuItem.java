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
