package com.delta.android.WMS.Client.GridAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

public class GoodInventoryNonSheetGridAdapter extends BaseAdapter {
    private LayoutInflater Inflater;
    private DataTable dtRegister;

    static class ViewHolder{
        TextView ItemID;
        TextView ItemName;
        TextView LotID;
        TextView Qty;
    }

    public GoodInventoryNonSheetGridAdapter(DataTable dtRegister, LayoutInflater inflater){
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
        ViewHolder holder;

        if(convertView == null){
            holder = new ViewHolder();
            convertView = this.Inflater.inflate(R.layout.activity_good_inventory_non_sheet_listview, null);
            holder.ItemID = convertView.findViewById(R.id.tvInventoryItemId);
            holder.ItemName = convertView.findViewById(R.id.tvInventoryItemName);
            holder.LotID = convertView.findViewById(R.id.tvInventoryLotId);
            holder.Qty = convertView.findViewById(R.id.tvInventoryQty);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }

        //將資訊放入holder內
        holder.ItemName.setText(dtRegister.Rows.get(position).getValue("ITEM_NAME").toString());
        holder.ItemID.setText(dtRegister.Rows.get(position).getValue("ITEM_ID").toString());
        holder.LotID.setText(dtRegister.Rows.get(position).getValue("REGISTER_ID").toString());
        holder.Qty.setText(dtRegister.Rows.get(position).getValue("QTY").toString());

        return convertView;
    }
}
