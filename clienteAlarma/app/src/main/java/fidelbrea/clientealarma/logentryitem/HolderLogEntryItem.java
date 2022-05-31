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
