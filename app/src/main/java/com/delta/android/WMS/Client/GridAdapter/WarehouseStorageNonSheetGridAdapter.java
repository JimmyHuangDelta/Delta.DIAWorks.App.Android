package com.delta.android.WMS.Client.GridAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

import org.w3c.dom.Text;

public class WarehouseStorageNonSheetGridAdapter extends BaseAdapter {

    private LayoutInflater Inflater;
    private DataTable dtSheetWarehouse;

    static class ViewHolder{
        TextView tvItemId;
        TextView tvItemName;
        TextView tvStorageId;
        TextView tvSkuLevel;
        TextView tvSkuNum;
        TextView tvQty;
        TextView tvMfgDate;
        TextView tvExpDate;
        TextView tvLotCode;
    }

    public WarehouseStorageNonSheetGridAdapter(DataTable dtSheetWarehouse, LayoutInflater inflater){
        this.Inflater = inflater;
        this.dtSheetWarehouse = dtSheetWarehouse;
    }

    @Override
    public int getCount() { return this.dtSheetWarehouse.Rows.size(); }

    @Override
    public Object getItem(int position) { return this.dtSheetWarehouse.Rows.get(position); }

    @Override
    public long getItemId(int positon) { return positon; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder holder;

        if(convertView == null){
            holder = new ViewHolder();
            convertView = this.Inflater.inflate(R.layout.activity_warehouse_storage_non_sheet_listview, null);
            holder.tvItemId = convertView.findViewById(R.id.tvItemId);
            holder.tvItemName = convertView.findViewById(R.id.tvItemName);
            holder.tvStorageId = convertView.findViewById(R.id.tvStorageId);
            holder.tvSkuLevel = convertView.findViewById(R.id.tvSkuLevel);
            holder.tvSkuNum = convertView.findViewById(R.id.tvSkuNum);
            holder.tvQty = convertView.findViewById(R.id.tvQty);
            holder.tvMfgDate = convertView.findViewById(R.id.tvMfgDate);
            holder.tvExpDate = convertView.findViewById(R.id.tvExpDate);
            holder.tvLotCode = convertView.findViewById(R.id.tvLotCode);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        //將資訊放入holder內
        holder.tvItemId.setText(dtSheetWarehouse.Rows.get(position).getValue("ITEM_ID").toString());
        holder.tvItemName.setText(dtSheetWarehouse.Rows.get(position).getValue("ITEM_NAME").toString());
        holder.tvStorageId.setText(dtSheetWarehouse.Rows.get(position).getValue("STORAGE_ID").toString());
        holder.tvSkuLevel.setText(dtSheetWarehouse.Rows.get(position).getValue("SKU_LEVEL").toString());
        holder.tvSkuNum.setText(dtSheetWarehouse.Rows.get(position).getValue("SKU_NUM").toString());
        holder.tvQty.setText(dtSheetWarehouse.Rows.get(position).getValue("QTY").toString());
        holder.tvMfgDate.setText(dtSheetWarehouse.Rows.get(position).getValue("MFG_DATE").toString());
        holder.tvExpDate.setText(dtSheetWarehouse.Rows.get(position).getValue("EXP_DATE").toString());
        holder.tvLotCode.setText(dtSheetWarehouse.Rows.get(position).getValue("LOT_CODE").toString());

        return convertView;
    }
}
