package com.delta.android.WMS.Client.GridAdapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

public class DeliveryNoteDetPickGridNewAdapter extends BaseAdapter {

    private String[][] ElementData;
    private LayoutInflater Inflater;

    static class ViewHolder{
        TextView LotId;
        TextView BinId;
        TextView Qty;
        TextView MfgDate;
        TextView ExpDate;
        //TextView RsvQty;
        TextView UOM;
        //CheckBox SelectedRegister;
    }

    public DeliveryNoteDetPickGridNewAdapter(DataTable elementData, LayoutInflater inflater) {
        this.ElementData = new String[elementData.Rows.size()][8];
        for (int i=0;i<elementData.Rows.size();i++){
            ElementData[i][0] = elementData.Rows.get(i).getValue("REGISTER_ID").toString();
            ElementData[i][1] = elementData.Rows.get(i).getValue("BIN_ID").toString();
            ElementData[i][2] = elementData.Rows.get(i).getValue("QTY").toString();
            //ElementData[i][3] = elementData.Rows.get(i).getValue("RESERVED_QTY").toString();
            ElementData[i][3] = elementData.Rows.get(i).getValue("ITEM_UOM").toString();
            ElementData[i][4] = elementData.Rows.get(i).getValue("RESERVED_QTY").toString();
            ElementData[i][5] = elementData.Rows.get(i).getValue("CAN_PICK_QTY").toString();
            ElementData[i][6] = elementData.Rows.get(i).getValue("MFG_DATE").toString();
            ElementData[i][7] = elementData.Rows.get(i).getValue("EXP_DATE").toString();
            //ElementData[i][4] = "False";
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

            holder = new ViewHolder();
            convertView = this.Inflater.inflate(R.layout.activity_wms_delivery_note_picking_executed_new_listview, null);
            holder.LotId = convertView.findViewById(R.id.tvPickedGridLotId);
            holder.BinId = convertView.findViewById(R.id.tvPickedGridBinId);
            holder.Qty = convertView.findViewById(R.id.tvPickedGridQty);
            //holder.RsvQty = convertView.findViewById(R.id.tvPickedGridRsvQty);
            holder.UOM = convertView.findViewById(R.id.tvPickedGridUom);
            //holder.SelectedRegister = convertView.findViewById(R.id.cbPickedGridSelectedRegister);
            holder.MfgDate = convertView.findViewById(R.id.tvMfgDate);
            holder.ExpDate = convertView.findViewById(R.id.tvExpDate);
            convertView.setTag(holder);

        } else {

            holder = (ViewHolder) convertView.getTag();

        }

        holder.LotId.setText(ElementData[position][0]);
        holder.BinId.setText(ElementData[position][1]);
        holder.UOM.setText(ElementData[position][3]);
        holder.Qty.setText(String.valueOf(ElementData[position][2]) + " / " + ElementData[position][5] + " / " + ElementData[position][4]);
        holder.MfgDate.setText(ElementData[position][6]);
        holder.ExpDate.setText(ElementData[position][7]);

        //holder.RsvQty.setText(ElementData[position][3]);
        //holder.SelectedRegister.setChecked(Boolean.getBoolean(ElementData[position][4]));

        if (Double.parseDouble(ElementData[position][5]) > 0)
            convertView.setBackgroundColor(Color.argb(50, 0, 255, 0));
        else
            convertView.setBackgroundColor(Color.argb(50, 255, 255, 255));

        return convertView;
    }
}
