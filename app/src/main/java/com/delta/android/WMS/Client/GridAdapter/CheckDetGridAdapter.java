package com.delta.android.WMS.Client.GridAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

public class CheckDetGridAdapter extends BaseAdapter {
    private String[][] ElementData;
    private LayoutInflater Inflater; //加載Layout使用

    static class ViewHolder{
        TextView ItemId;
        TextView BinId;
    }

    public CheckDetGridAdapter(DataTable sheetData, LayoutInflater inflater){
        this.ElementData = new String[sheetData.Rows.size()][2];
        for (int i=0;i<sheetData.Rows.size();i++){
            ElementData[i][0] = sheetData.Rows.get(i).getValue("ITEM_ID").toString();
            ElementData[i][1] = sheetData.Rows.get(i).getValue("BIN_ID").toString();
        }
        this.Inflater = inflater;
    }
    @Override
    public int getCount() {
        return this.ElementData.length;
    }

    @Override
    public Object getItem(int position) {
        return this.ElementData[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null){
            holder = new CheckDetGridAdapter.ViewHolder();
            convertView = this.Inflater.inflate(R.layout.activity_wms_good_check_listview,null);
            holder.ItemId = convertView.findViewById(R.id.tvCheckGridItemId);
            holder.BinId = convertView.findViewById(R.id.tvCheckGridBinId);
            convertView.setTag(holder);
        }else{
            holder = (CheckDetGridAdapter.ViewHolder) convertView.getTag();
        }
        //將資訊放入holder內
        holder.ItemId.setText(ElementData[position][0]);
        holder.BinId.setText(ElementData[position][1]);
        return convertView;
    }
}
