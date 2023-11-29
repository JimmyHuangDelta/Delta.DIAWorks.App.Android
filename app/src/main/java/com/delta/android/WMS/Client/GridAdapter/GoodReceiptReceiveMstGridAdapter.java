package com.delta.android.WMS.Client.GridAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

public class GoodReceiptReceiveMstGridAdapter extends BaseAdapter {

    private String[][] ElementData; //紀錄每一個GridView所需要使用的項目
    private LayoutInflater Inflater; //加載Layout時使用

    public GoodReceiptReceiveMstGridAdapter(DataTable sheetData, LayoutInflater inflater){
        this.ElementData = new String[sheetData.Rows.size()][6];
        for (int i=0;i<sheetData.Rows.size();i++){
            ElementData[i][0] = sheetData.Rows.get(i).getValue("GR_ID").toString();
            ElementData[i][1] = sheetData.Rows.get(i).getValue("GR_STATUS").toString();
            ElementData[i][2] = sheetData.Rows.get(i).getValue("CREATE_DATE").toString().substring(0,10);
            ElementData[i][3] = sheetData.Rows.get(i).getValue("CUSTOMER_NAME").toString();
            ElementData[i][4] = sheetData.Rows.get(i).getValue("CUSTOMER_ID").toString();
        }
        this.Inflater = inflater;
    }

    static class ViewHolder{
        TextView SheetId;
        TextView SheetStatus;
        TextView SheetDate;
        TextView CustomerName;
        TextView CustomerID;
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
            holder = new ViewHolder();
            convertView = this.Inflater.inflate(R.layout.activity_wms_good_receipt_receive_listview,null);
            holder.SheetId = convertView.findViewById(R.id.tvSheetMstGridId);
            holder.SheetDate = convertView.findViewById(R.id.tvSheetMstGridDate);
            holder.SheetStatus = convertView.findViewById(R.id.tvSheetMstGridStatus);
            holder.CustomerName = convertView.findViewById(R.id.tvCustomerName);
            holder.CustomerID = convertView.findViewById(R.id.tvCustomerID);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        //將資訊放入holder內
        holder.SheetId.setText(ElementData[position][0]);
        holder.SheetStatus.setText(ElementData[position][1]);
        holder.SheetDate.setText(ElementData[position][2]);
        holder.CustomerName.setText(ElementData[position][3]);
        holder.CustomerID.setText(ElementData[position][4]);

        return convertView;
    }
}
