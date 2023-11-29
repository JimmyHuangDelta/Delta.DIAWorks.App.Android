package com.delta.android.WMS.Client.GridAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

public class DeliveryNotePickingGridAdapter extends BaseAdapter {

    private LayoutInflater Inflater; //加載Layout時使用
    private DataTable SheetData;//紀錄每一個GridView所需要使用的項目

    public DeliveryNotePickingGridAdapter(DataTable sheetData, LayoutInflater inflater) {
        this.SheetData = sheetData;
        this.Inflater = inflater;
    }

    static class ViewHolder {
        TextView tvSeq;
        TextView tvItemId;
        TextView tvItemName;
        TextView tvLotId;
        TextView tvQty;
    }

    @Override
    public int getCount() {
        return this.SheetData.Rows.size();
    }

    @Override
    public Object getItem(int position) {
        return this.SheetData.Rows.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = this.Inflater.inflate(R.layout.style_wms_listview_dn_picking_list, null);
            holder.tvSeq = convertView.findViewById(R.id.tvSeq);
            holder.tvItemId = convertView.findViewById(R.id.tvItemId);
            holder.tvItemName = convertView.findViewById(R.id.tvItemName);
            holder.tvLotId = convertView.findViewById(R.id.tvLotId);
            holder.tvQty = convertView.findViewById(R.id.tvQty);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //將資訊放入holder內
        holder.tvSeq.setText(SheetData.Rows.get(position).getValue("SEQ").toString());
        holder.tvItemId.setText(SheetData.Rows.get(position).getValue("ITEM_ID").toString());
        holder.tvItemName.setText(SheetData.Rows.get(position).getValue("ITEM_NAME").toString());
        holder.tvLotId.setText(SheetData.Rows.get(position).getValue("LOT_ID").toString());
        holder.tvQty.setText(SheetData.Rows.get(position).getValue("QTY").toString());

        return convertView;
    }
}
