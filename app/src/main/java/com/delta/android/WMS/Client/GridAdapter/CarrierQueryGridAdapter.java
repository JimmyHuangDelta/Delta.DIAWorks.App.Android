package com.delta.android.WMS.Client.GridAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

public class CarrierQueryGridAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private DataTable dataTable;

    static class ViewHolder {
        private TextView tvCarrierId;
        private TextView tvCarrierKind;
        private TextView tvFixedPosition;
        private TextView tvLocationId;
        private TextView tvTransportStatus;
    }

    public CarrierQueryGridAdapter (DataTable dataTable, LayoutInflater inflater) {
        this.dataTable = dataTable;
        this.inflater = inflater;
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

        CarrierQueryGridAdapter.ViewHolder holder;

        if(convertView == null) {
            holder = new CarrierQueryGridAdapter.ViewHolder();
            convertView = this.inflater.inflate(R.layout.activity_wms_carrier_query_listview, null);
            holder.tvCarrierId = convertView.findViewById(R.id.lvTxtCarrierIdVal);
            holder.tvCarrierKind = convertView.findViewById(R.id.lvTxtCarrierKindVal);
            holder.tvFixedPosition = convertView.findViewById(R.id.lvTxtFixedPositionVal);
            holder.tvLocationId = convertView.findViewById(R.id.lvTxtLocationVal);
            holder.tvTransportStatus = convertView.findViewById(R.id.lvTxtTransportStatusVal);
            convertView.setTag(holder);
        } else {
            holder = (CarrierQueryGridAdapter.ViewHolder) convertView.getTag();
        }

        holder.tvCarrierId.setText(dataTable.Rows.get(position).get("CARRIER_ID").toString());
        holder.tvCarrierKind.setText(dataTable.Rows.get(position).get("CARRIER_KIND").toString());
        holder.tvFixedPosition.setText(dataTable.Rows.get(position).get("FIXED_LOCATION").toString());
        holder.tvLocationId.setText(dataTable.Rows.get(position).get("LOCATION").toString());
        holder.tvTransportStatus.setText(dataTable.Rows.get(position).get("TRANSPORT_STATUS").toString());

        return convertView;
    }
}
