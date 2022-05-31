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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fidelbrea.clientealarma.R;

public class AdapterLogEntryItem extends RecyclerView.Adapter<HolderLogEntryItem> {

    private final List<LogEntryItem> listLogEntries = new ArrayList<>();
    private final Context context;

    public AdapterLogEntryItem(Context context) {
        this.context = context;
    }

    public void add(LogEntryItem logEntryItem) {
        listLogEntries.add(logEntryItem);
        notifyItemInserted(listLogEntries.size());
    }

    public void remove(int position) {
        listLogEntries.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, listLogEntries.size());
    }

    public void clear() {
        listLogEntries.clear();
        notifyItemInserted(listLogEntries.size());
    }

    @Override
    public HolderLogEntryItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.card_log_entry, parent, false);
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
