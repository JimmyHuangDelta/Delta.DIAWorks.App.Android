package com.delta.android.WMS.Client.GridAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

public class CarrierQueryLayoutGridAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private DataTable dataTable;

    static class ViewHolder {
        TextView tvCarrierId;
        TextView tvBlockAliasId;
        TextView tvCarrierQty;
    }

    public CarrierQueryLayoutGridAdapter (DataTable dataTable, LayoutInflater inflater) {
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

        CarrierQueryLayoutGridAdapter.ViewHolder holder;

        if (convertView == null) {
            holder = new CarrierQueryLayoutGridAdapter.ViewHolder();
            convertView = this.inflater.inflate(R.layout.activity_wms_carrier_query_layout_listview, null);
            holder.tvCarrierId = convertView.findViewById(R.id.lvTxtCarrierIdVal);
            holder.tvBlockAliasId = convertView.findViewById(R.id.lvTxtBlockAliasIdVal);
            holder.tvCarrierQty = convertView.findViewById(R.id.lvTxtCarrierQtyVal);
            convertView.setTag(holder);
        } else {
            holder = (CarrierQueryLayoutGridAdapter.ViewHolder) convertView.getTag();
        }

        holder.tvCarrierId.setText(dataTable.Rows.get(position).get("CARRIER_ID").toString());
        holder.tvBlockAliasId.setText(dataTable.Rows.get(position).get("BLOCK_ALIAS_ID").toString());
        holder.tvCarrierQty.setText(dataTable.Rows.get(position).get("CARRIED_QTY").toString());

        return convertView;
    }
}
