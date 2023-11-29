package com.delta.android.WMS.Client.GridAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

public class GoodPickNonSheetGridAdapter extends BaseAdapter {
    private LayoutInflater Inflater;
    private DataTable dtRegister;

    static class ViewHolder{
        TextView ItemID;
        TextView ItemName;
        TextView LotID;
        TextView Qty;
        TextView BinID;
    }

    public GoodPickNonSheetGridAdapter(DataTable dtRegister, LayoutInflater inflater){
        this.Inflater = inflater;
        this.dtRegister = dtRegister;
    }

    @Override
    public int getCount() {return this.dtRegister.Rows.size();}

    @Override
    public Object getItem(int position) {return this.dtRegister.Rows.get(position);}

    @Override
    public long getItemId(int position) {return position;}

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        GoodPickNonSheetGridAdapter.ViewHolder holder;

        if(convertView == null){
            holder = new GoodPickNonSheetGridAdapter.ViewHolder();
            convertView = this.Inflater.inflate(R.layout.activity_good_pick_non_sheet_listview, null);
            holder.ItemID = convertView.findViewById(R.id.tvPickItemId);
            holder.ItemName = convertView.findViewById(R.id.tvPickItemName);
            holder.LotID = convertView.findViewById(R.id.tvPickLotId);
            holder.Qty = convertView.findViewById(R.id.tvPickQty);
            holder.BinID = convertView.findViewById(R.id.tvBinID);
            convertView.setTag(holder);
        }else{
            holder = (GoodPickNonSheetGridAdapter.ViewHolder)convertView.getTag();
        }

        //將資訊放入holder內
        holder.ItemName.setText(dtRegister.Rows.get(position).getValue("ITEM_NAME").toString());
        holder.ItemID.setText(dtRegister.Rows.get(position).getValue("ITEM_ID").toString());
        holder.LotID.setText(dtRegister.Rows.get(position).getValue("REGISTER_ID").toString());
        holder.Qty.setText(dtRegister.Rows.get(position).getValue("QTY").toString());
        holder.BinID.setText(dtRegister.Rows.get(position).getValue("BIN_ID").toString());

        return convertView;
    }
}
