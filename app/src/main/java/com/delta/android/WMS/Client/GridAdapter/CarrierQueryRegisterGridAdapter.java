package com.delta.android.WMS.Client.GridAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

import org.w3c.dom.Text;


public class CarrierQueryRegisterGridAdapter extends BaseAdapter {

    private String carrierId;
    private String blockId;
    private LayoutInflater inflater;
    private DataTable dataTable;

    static class ViewHolder {
        TextView tvCarrierId;
        TextView tvBlockId;
        TextView tvItemId;
        TextView tvRegisterId;
        TextView tvQty;
    }

    public CarrierQueryRegisterGridAdapter(DataTable dataTable, LayoutInflater inflater) {
        this.inflater = inflater;
        this.dataTable = dataTable;
    }

    public CarrierQueryRegisterGridAdapter(String carrierId, String blockId, DataTable dataTable, LayoutInflater inflater) {
        this.carrierId = carrierId;
        this.blockId = blockId;
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

        CarrierQueryRegisterGridAdapter.ViewHolder holder;

        if (convertView == null) {
            holder = new CarrierQueryRegisterGridAdapter.ViewHolder();
            convertView = this.inflater.inflate(R.layout.activity_wms_carrier_query_layout_listview, null);
            holder.tvCarrierId = convertView.findViewById(R.id.lvTxtCarrierIdVal);
            holder.tvBlockId = convertView.findViewById(R.id.lvTxtBlockIdVal);
            holder.tvItemId = convertView.findViewById(R.id.lvTxtItemIdVal);
            holder.tvRegisterId = convertView.findViewById(R.id.lvTxtRegisterIdVal);
            holder.tvQty = convertView.findViewById(R.id.lvTxtQtyVal);
            convertView.setTag(holder);
        } else {
            holder = (CarrierQueryRegisterGridAdapter.ViewHolder) convertView.getTag();
        }

        holder.tvCarrierId.setText(this.carrierId);
        holder.tvBlockId.setText(this.blockId);
        holder.tvItemId.setText(dataTable.Rows.get(position).get("ITEM_ID").toString());
        holder.tvRegisterId.setText(dataTable.Rows.get(position).get("REGISTER_ID").toString());
        holder.tvQty.setText((dataTable.Rows.get(position).get("QTY").toString()));
        return convertView;
    }
}
