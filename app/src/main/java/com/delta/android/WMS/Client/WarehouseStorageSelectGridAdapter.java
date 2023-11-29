package com.delta.android.WMS.Client;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

public class WarehouseStorageSelectGridAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private DataTable dt;

    static class ViewHolder{
        TextView itemID;
        TextView itemName;
        TextView storageID;
        TextView binID;
    }

    public WarehouseStorageSelectGridAdapter(DataTable dt, LayoutInflater inflater) {
        this.inflater = inflater;
        this.dt = dt;
    }

    @Override
    public int getCount() {
        return this.dt.Rows.size();
    }

    @Override
    public Object getItem(int pos) {
        return this.dt.Rows.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if(convertView == null){
            holder = new ViewHolder();
            convertView = this.inflater.inflate(R.layout.activity_wms_warehouse_storage_select_listview, null);
            holder.itemID = convertView.findViewById(R.id.tvInStockItemId);
            holder.itemName = convertView.findViewById(R.id.tvInStockItemName);
            holder.storageID = convertView.findViewById(R.id.tvInStockStorageId);
            holder.binID = convertView.findViewById(R.id.tvInStockBinID);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.itemID.setText(dt.Rows.get(pos).getValue("ITEM_ID").toString());
        holder.itemName.setText(dt.Rows.get(pos).getValue("ITEM_NAME").toString());
        holder.storageID.setText(dt.Rows.get(pos).getValue("STORAGE_ID").toString());
        holder.binID.setText(dt.Rows.get(pos).getValue("BIN_ID").toString());

        return convertView;
    }
}
