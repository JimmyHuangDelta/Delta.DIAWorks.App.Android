package com.delta.android.WMS.Client.GridAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

public class DeliveryNoteDetGridAdapter extends BaseAdapter {

    //private String[][] ElementData;
    private LayoutInflater Inflater; //加載Layout使用
    private DataTable SheetData;
    private DataTable PickData;
    private boolean IsPick;
    private Context Context;

    static class ViewHolder {
        TextView ItemId;
        TextView SheetId;
        TextView ItemName;
        TextView OrderId;
        TextView BinId;
        TextView Qty;
        TextView QtyCaption;
        TextView Uom;
        ImageView Next;
    }

    public DeliveryNoteDetGridAdapter(DataTable sheetData, LayoutInflater inflater, DataTable pickData, boolean isPick, Context context) {
        this.SheetData = sheetData;
        this.Inflater = inflater;
        this.PickData = pickData;
        this.IsPick = isPick;
        Context = context;
    }

    public DeliveryNoteDetGridAdapter(DataTable sheetData, LayoutInflater inflater, boolean isPick, Context context) {
        this.SheetData = sheetData;
        this.Inflater = inflater;
        this.IsPick = isPick;
        Context = context;
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
            convertView = this.Inflater.inflate(R.layout.activity_wms_delivery_note_picking_detail_listview, null);
            holder.ItemId = convertView.findViewById(R.id.tvDnDetGridItemId);
            holder.SheetId = convertView.findViewById(R.id.tvDnDetGridSheetId);
            holder.ItemName = convertView.findViewById(R.id.tvDnDetGridItemName);
            holder.OrderId = convertView.findViewById(R.id.tvPickedGridOrderId);
            //holder.BinId = convertView.findViewById(R.id.tvSheetDetGridBinId);
            holder.Qty = convertView.findViewById(R.id.tvDnDetGridQty);
            holder.QtyCaption = convertView.findViewById(R.id.tvDnDetGridQty2);
            holder.Uom = convertView.findViewById(R.id.tvDnDetGridUom);
            holder.Next =  convertView.findViewById(R.id.ivNext);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //將資訊放入holder內
        holder.ItemId.setText(SheetData.Rows.get(position).getValue("ITEM_ID").toString());
        holder.SheetId.setText(SheetData.Rows.get(position).getValue("SHEET_ID").toString());
        holder.ItemName.setText(SheetData.Rows.get(position).getValue("ITEM_NAME").toString());
        holder.OrderId.setText(SheetData.Rows.get(position).getValue("ORDER_ID").toString());
        //holder.BinId.setText(SheetData.Rows.get(position).getValue("FROM_BIN_ID").toString());
        holder.Qty.setText(SheetData.Rows.get(position).getValue("TRX_QTY").toString());
        holder.Uom.setText(SheetData.Rows.get(position).getValue("UOM").toString());

//        double pickQty = 0.0;
//        //將已揀料資訊的數量家總
//        for (DataRow drPick : PickData.Rows) {
//            if (SheetData.Rows.get(position).getValue("SHEET_ID").equals(drPick.getValue("SHEET_ID"))
//                    && SheetData.Rows.get(position).getValue("SEQ").equals(drPick.getValue("SEQ"))) {
//                pickQty += Double.parseDouble(drPick.getValue("PROC_QTY").toString());
//            }
//        }

        if(IsPick)//已捒完項次
        {
            holder.Next.setVisibility(View.INVISIBLE);
            holder.Qty.setText(SheetData.Rows.get(position).getValue("PICKED_QTY").toString());
            holder.QtyCaption.setText((Context.getResources().getString(R.string.QTY)));
        }
        else//未捒項次
        {
            holder.Next.setVisibility(View.VISIBLE);
            holder.Qty.setText(String.format("%s / %s / %s", SheetData.Rows.get(position).getValue("RESERVED_QTY").toString(), SheetData.Rows.get(position).getValue("PICKED_QTY").toString(), SheetData.Rows.get(position).getValue("TRX_QTY").toString()));
            holder.QtyCaption.setText((Context.getResources().getString(R.string.RSV_PICK_REQUIRE_QTY)));
        }

        return convertView;
    }
}
