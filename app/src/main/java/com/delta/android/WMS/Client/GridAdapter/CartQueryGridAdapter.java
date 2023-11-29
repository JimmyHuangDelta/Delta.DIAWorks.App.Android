package com.delta.android.WMS.Client.GridAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

public class CartQueryGridAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private DataTable dataTable;

    static class ViewHolder {
        TextView tvCartId;
        TextView tvVehicleId;
        TextView tvFixedLocation;
        TextView tvLocation;
        TextView tvTransportStatus;
    }

    public CartQueryGridAdapter (DataTable dataTable, LayoutInflater inflater) {
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

        CartQueryGridAdapter.ViewHolder holder;

        if (convertView == null) {
            holder = new CartQueryGridAdapter.ViewHolder();
            convertView = this.inflater.inflate(R.layout.activity_wms_cart_query_listview, null);
            holder.tvCartId = convertView.findViewById(R.id.lvTxtCartIdVal);
            holder.tvVehicleId = convertView.findViewById(R.id.lvTxtVehicleIdVal);
            holder.tvFixedLocation = convertView.findViewById(R.id.lvTxtFixedLocationVal);
            holder.tvLocation = convertView.findViewById(R.id.lvTxtLocationVal);
            holder.tvTransportStatus = convertView.findViewById(R.id.lvTxtTransportStatusVal);
            convertView.setTag(holder);
        } else {
            holder = (CartQueryGridAdapter.ViewHolder) convertView.getTag();
        }

        holder.tvCartId.setText(dataTable.Rows.get(position).getValue("CART_ID").toString());
        holder.tvVehicleId.setText(dataTable.Rows.get(position).getValue("VEHICLE_ID").toString());
        holder.tvFixedLocation.setText(dataTable.Rows.get(position).getValue("FIXED_LOCATION").toString());
        holder.tvLocation.setText(dataTable.Rows.get(position).getValue("LOCATION").toString());
        holder.tvTransportStatus.setText(dataTable.Rows.get(position).getValue("TRANSPORT_STATUS").toString());

        return convertView;
    }
}
