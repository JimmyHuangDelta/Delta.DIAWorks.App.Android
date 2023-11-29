package com.delta.android.WMS.Client.GridAdapter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

import java.util.List;

public class SheetCancelPickExecutedGridAdapter extends BaseAdapter {

    private LayoutInflater Inflater; //加載Layout使用
    private DataTable SheetData;
    private DataTable PickData;

    static class ViewHolder {
        TextView LotId;
        TextView BinId;
        TextView Qty;
        TextView Uom;
    }

    public SheetCancelPickExecutedGridAdapter(DataTable pickdet, LayoutInflater inflater) {
        PickData = pickdet;
        this.Inflater = inflater;
    }

    @Override
    public int getCount() {
        return this.PickData.Rows.size();
    }

    @Override
    public Object getItem(int position) {
        return this.PickData.Rows.get(position);
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
            convertView = this.Inflater.inflate(R.layout.activity_wms_good_cancel_pick_executed_grid_listview, null);
            holder.LotId = convertView.findViewById(R.id.tvPickedGridLotId);
            holder.BinId = convertView.findViewById(R.id.tvPickedGridBinId);
            holder.Qty = convertView.findViewById(R.id.tvPickedGridQty);
            holder.Uom = convertView.findViewById(R.id.tvPickedGridUom);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //將資訊放入holder內
        holder.LotId.setText(PickData.Rows.get(position).getValue("LOT_ID").toString());
        holder.BinId.setText(PickData.Rows.get(position).getValue("BIN_ID").toString());
        holder.Qty.setText(PickData.Rows.get(position).getValue("QTY").toString());
        holder.Uom.setText(PickData.Rows.get(position).getValue("UOM").toString());

        return convertView;
    }
}
