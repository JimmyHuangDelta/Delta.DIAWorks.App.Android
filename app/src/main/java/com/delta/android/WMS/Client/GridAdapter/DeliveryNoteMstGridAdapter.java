package com.delta.android.WMS.Client.GridAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

public class DeliveryNoteMstGridAdapter extends BaseAdapter {

    private LayoutInflater Inflater; //加載Layout時使用
    private DataTable SheetData;//紀錄每一個GridView所需要使用的項目

    public DeliveryNoteMstGridAdapter(DataTable sheetData, LayoutInflater inflater) {
        this.SheetData = sheetData;
        this.Inflater = inflater;
    }

    static class ViewHolder {
        TextView PickShtId;
        TextView DnId;
        TextView SheetStatus;
        TextView SheetDate;
        CheckBox CheckSheet;
    }

    @Override
    public int getCount() {
        return this.SheetData.Rows.size();
    }

    @Override
    public Object getItem(int position) {
        return this.SheetData.Rows.get(position);
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
            convertView = this.Inflater.inflate(R.layout.style_wms_listview_dn_sheet_master, null);
            holder.PickShtId = convertView.findViewById(R.id.tvSheetMstGridPickId);
            holder.DnId = convertView.findViewById(R.id.tvSheetMstGridId);
            holder.SheetDate = convertView.findViewById(R.id.tvSheetMstGridDate);
            holder.SheetStatus = convertView.findViewById(R.id.tvSheetMstGridStatus);
            holder.CheckSheet = convertView.findViewById(R.id.cbSheetMstGridSheet);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //將資訊放入holder內
        holder.PickShtId.setText(SheetData.Rows.get(position).getValue("SHEET_ID").toString());
        holder.DnId.setText(SheetData.Rows.get(position).getValue("DN_ID").toString());
        holder.SheetStatus.setText(SheetData.Rows.get(position).getValue("SHEET_STATUS").toString());
        holder.SheetDate.setText(SheetData.Rows.get(position).getValue("CREATE_DATE").toString());
        holder.CheckSheet.setChecked(Boolean.parseBoolean(SheetData.Rows.get(position).getValue("SELECTED").toString()));

        return convertView;
    }
}
