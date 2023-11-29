package com.delta.android.WMS.Client.GridAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

public class WarehouseQueryRegistorGridAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private DataTable dataTable;

    static class ViewHolder {
        TextView tvLotId;
        TextView tvStorageName;
        TextView tvBinId;
        TextView tvBinName;
        TextView tvItemId;
        TextView tvItemName;
        TextView tvQty;
        TextView tvLocation;
        TextView tvExpDate;
    }

    public WarehouseQueryRegistorGridAdapter (DataTable dataTable, LayoutInflater inflater) {
        this.inflater = inflater;
        this.dataTable = dataTable;
    }

    @Override
    public int getCount() {
        return this.dataTable.Rows.size();
    }

    @Override
    public Object getItem(int position) {
        return this.dataTable.Rows.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        WarehouseQueryRegistorGridAdapter.ViewHolder holder;

        if (convertView == null) {
            holder = new WarehouseQueryRegistorGridAdapter.ViewHolder();
            convertView = this.inflater.inflate(R.layout.activity_wms_warehouse_query_register_listview, null);
            holder.tvLotId = convertView.findViewById(R.id.lvTxtLotIdVal);
            holder.tvStorageName = convertView.findViewById(R.id.lvTxtStorageNameVal);
            holder.tvBinId = convertView.findViewById(R.id.lvTxtBinIdVal);
            holder.tvBinName = convertView.findViewById(R.id.lvTxtBinNameVal);
            holder.tvItemId = convertView.findViewById(R.id.lvTxtItemIdVal);
            holder.tvItemName = convertView.findViewById(R.id.lvTxtItemNameVal);
            holder.tvQty = convertView.findViewById(R.id.lvTxtQtyVal);
            holder.tvLocation = convertView.findViewById(R.id.lvTxtLocationVal);
            holder.tvExpDate = convertView.findViewById(R.id.lvTxtExpDateVal);
            convertView.setTag(holder);
        } else {
            holder = (WarehouseQueryRegistorGridAdapter.ViewHolder) convertView.getTag();
        }

        holder.tvLotId.setText(dataTable.Rows.get(position).get("DISPLAY_REGISTER_ID").toString());
        holder.tvStorageName.setText(dataTable.Rows.get(position).get("STORAGE_NAME").toString());
        holder.tvBinId.setText(dataTable.Rows.get(position).get("BIN_ID").toString());
        holder.tvBinName.setText(dataTable.Rows.get(position).get("BIN_NAME").toString());
        holder.tvItemId.setText(dataTable.Rows.get(position).get("ITEM_ID").toString());
        holder.tvItemName.setText(dataTable.Rows.get(position).get("ITEM_NAME").toString());
        holder.tvQty.setText(dataTable.Rows.get(position).get("QTY").toString());
//        holder.tvLocation.setText(dataTable.Rows.get(position).get("").toString());
        holder.tvExpDate.setText(dataTable.Rows.get(position).get("EXP_DATE").toString());

        return convertView;
    }
}
