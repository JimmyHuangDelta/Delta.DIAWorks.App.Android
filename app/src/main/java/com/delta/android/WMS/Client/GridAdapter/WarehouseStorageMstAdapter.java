package com.delta.android.WMS.Client.GridAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

public class WarehouseStorageMstAdapter extends BaseAdapter {

    private String[][] elementData; // 記錄每一個 GridView 所需要使用的項目
    private LayoutInflater inflater; // 加載 Layout 時使用

    public WarehouseStorageMstAdapter(DataTable sheetData, LayoutInflater inflater){
        this.elementData = new String[sheetData.Rows.size()][4];
        for (int i=0;i<sheetData.Rows.size();i++){
            elementData[i][0] = sheetData.Rows.get(i).getValue("MTL_SHEET_ID").toString();
            elementData[i][1] = sheetData.Rows.get(i).getValue("MTL_SHEET_STATUS").toString();
            elementData[i][2] = sheetData.Rows.get(i).getValue("CREATE_DATE").toString().substring(0,10);
        }
        this.inflater = inflater;
    }

    static class ViewHolder {
        TextView sheetId;
        TextView sheetStatus;
        TextView sheetDate;
    }

    @Override
    public int getCount() {
        return this.elementData.length;
    }

    @Override
    public Object getItem(int pos) {
        return this.elementData[pos];
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = this.inflater.inflate(R.layout.activity_wms_warehouse_storage_mst_listview, null);
            holder.sheetId = convertView.findViewById(R.id.tvSheetMstGridId);
            holder.sheetDate = convertView.findViewById(R.id.tvSheetMstGridDate);
            holder.sheetStatus = convertView.findViewById(R.id.tvSheetMstGridStatus);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.sheetId.setText(elementData[pos][0]);
        holder.sheetStatus.setText(elementData[pos][1]);
        holder.sheetDate.setText(elementData[pos][2]);

        return convertView;
    }
}
