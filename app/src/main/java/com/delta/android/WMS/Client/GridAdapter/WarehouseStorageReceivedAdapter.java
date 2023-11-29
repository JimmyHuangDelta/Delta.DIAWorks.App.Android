package com.delta.android.WMS.Client.GridAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

public class WarehouseStorageReceivedAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private DataTable dt;

    static class ViewHolder{
        TextView tvSkuLevel;
        TextView tvSkuNum;
        TextView tvQty;
        TextView tvMfgDate;
        TextView tvExpDate;
        TextView tvLotCode;
        TextView tvWoId;
        CheckBox cbSelected;
    }

    public WarehouseStorageReceivedAdapter(DataTable dt, LayoutInflater inflater) {
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
            convertView = this.inflater.inflate(R.layout.activity_wms_warehouse_storage_received_listview, null);
            holder.tvSkuLevel = convertView.findViewById(R.id.tvSkuLevel);
            holder.tvSkuNum = convertView.findViewById(R.id.tvSkuNum);
            holder.tvQty = convertView.findViewById(R.id.tvQty);
            holder.tvMfgDate = convertView.findViewById(R.id.tvMfgDate);
            holder.tvExpDate = convertView.findViewById(R.id.tvExpDate);
            holder.tvLotCode = convertView.findViewById(R.id.tvLotCode);
            holder.tvWoId = convertView.findViewById(R.id.tvWoId);
            holder.cbSelected = convertView.findViewById(R.id.cbSelected);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvSkuLevel.setText(dt.Rows.get(pos).getValue("SKU_LEVEL").toString());
        holder.tvSkuNum.setText(dt.Rows.get(pos).getValue("SKU_NUM").toString());
        holder.tvQty.setText(dt.Rows.get(pos).getValue("QTY").toString());
        holder.tvMfgDate.setText(dt.Rows.get(pos).getValue("MFG_DATE").toString());
        holder.tvExpDate.setText(dt.Rows.get(pos).getValue("EXP_DATE").toString());
        holder.tvLotCode.setText(dt.Rows.get(pos).getValue("LOT_CODE").toString());
        holder.tvWoId.setText(dt.Rows.get(pos).getValue("WO_ID").toString());
        holder.cbSelected.setChecked(Boolean.parseBoolean(dt.Rows.get(pos).getValue("SELECTED").toString()));

        return convertView;
    }
}
