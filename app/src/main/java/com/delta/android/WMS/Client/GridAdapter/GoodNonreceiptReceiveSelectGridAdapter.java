package com.delta.android.WMS.Client.GridAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

public class GoodNonreceiptReceiveSelectGridAdapter extends BaseAdapter {

    private LayoutInflater Inflater;
    private DataTable dtSelect;

    static class ViewHolder{
        TextView ItemID;
        TextView ItemName;
        TextView StorageID;
        TextView BinID;
    }

    public GoodNonreceiptReceiveSelectGridAdapter(DataTable dtSelect, LayoutInflater inflater){
        this.Inflater = inflater;
        this.dtSelect = dtSelect;
    }

    @Override
    public int getCount() { return this.dtSelect.Rows.size(); }

    @Override
    public Object getItem(int position) { return this.dtSelect.Rows.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder holder;

        if(convertView == null){
            holder = new ViewHolder();
            convertView = this.Inflater.inflate(R.layout.activity_good_nonreceipt_receive_confirm_listview, null);
            holder.ItemID = convertView.findViewById(R.id.tvReceiveItemId);
            holder.ItemName = convertView.findViewById(R.id.tvReceiveItemName);
            holder.StorageID = convertView.findViewById(R.id.tvReceiveStorageId);
            holder.BinID = convertView.findViewById(R.id.tvReceiveBinID);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        //將資訊放入holder內
        holder.ItemID.setText(dtSelect.Rows.get(position).getValue("ITEM_ID").toString());
        holder.ItemName.setText(dtSelect.Rows.get(position).getValue("ITEM_NAME").toString());
        holder.StorageID.setText(dtSelect.Rows.get(position).getValue("STORAGE_ID").toString());
        holder.BinID.setText(dtSelect.Rows.get(position).getValue("BIN_ID").toString());

        return convertView;
    }
}
