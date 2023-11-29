package com.delta.android.WMS.Client.GridAdapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

public class SheetCancelDetGridAdapter extends BaseAdapter {

    private LayoutInflater Inflater; //加載Layout使用
    private DataTable SheetData;
    private DataTable PickData;
    private DataTable RsvData;
    private boolean IsPick;
    private android.content.Context Context;

    static class ViewHolder {
        TextView ItemId;
        TextView SheetId;
        TextView ItemName;
        TextView LotId;
        TextView BinId;
        TextView Qty;
        TextView QtyCaption;
        TextView Uom;
        ImageView Next;
        TextView PickSheetId;
    }

    public SheetCancelDetGridAdapter(DataTable sheetData, LayoutInflater inflater, DataTable pickData, boolean isPick, Context context) {
        this.SheetData = sheetData;
        this.PickData = pickData;
        this.IsPick = isPick;
        this.Inflater = inflater;
        this.Context = context;
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
            convertView = this.Inflater.inflate(R.layout.activity_wms_good_cancel_pick_detail_listview, null);
            holder.ItemId = convertView.findViewById(R.id.tvSheetDetGridItemId);
            holder.SheetId = convertView.findViewById(R.id.tvSheetDetGridSheetId);
            holder.ItemName = convertView.findViewById(R.id.tvSheetDetGridItemName);
            holder.LotId = convertView.findViewById(R.id.tvSheetDetGridLotId);
            holder.BinId = convertView.findViewById(R.id.tvSheetDetGridBinId);
            holder.Qty = convertView.findViewById(R.id.tvSheetDetGridQty);
            holder.QtyCaption = convertView.findViewById((R.id.tvSheetDetGridQty3));
            holder.Uom = convertView.findViewById(R.id.tvSheetDetGridUom);
            holder.Next = convertView.findViewById(R.id.ivNext);
            holder.PickSheetId = convertView.findViewById(R.id.tvSheetDetGridSheetPickId);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //將資訊放入holder內
        holder.ItemId.setText(SheetData.Rows.get(position).getValue("ITEM_ID").toString());
        holder.SheetId.setText(SheetData.Rows.get(position).getValue("SOURCE_SHEET_ID").toString());
        holder.ItemName.setText(SheetData.Rows.get(position).getValue("ITEM_NAME").toString());
        holder.LotId.setText(SheetData.Rows.get(position).getValue("LOT_ID").toString());
        holder.BinId.setText(SheetData.Rows.get(position).getValue("FROM_BIN_ID").toString());
        //holder.Qty.setText(SheetData.Rows.get(position).getValue("TRX_QTY").toString());
        holder.Uom.setText(SheetData.Rows.get(position).getValue("ITEM_UOM").toString());
        holder.PickSheetId.setText(SheetData.Rows.get(position).getValue("SHEET_ID").toString());

        double pickQty = 0.0;
        double rsvPickQty = 0.0;

        //將已揀料資訊的數量家總

        for (DataRow drPick : PickData.Rows) {
            if (SheetData.Rows.get(position).getValue("SHEET_MST_KEY").equals(drPick.getValue("SHEET_MST_KEY"))
                    && SheetData.Rows.get(position).getValue("SEQ").equals(drPick.getValue("SEQ"))) {
                if (drPick.getValue("PICK_STATUS").equals("Picked")) {
                    if (drPick.getValue("IS_PICKED").toString().equals(("Y")))
                        pickQty += Double.parseDouble(drPick.getValue("QTY").toString());
                    else
                        rsvPickQty += Double.parseDouble(drPick.getValue("QTY").toString());
                } else if (drPick.getValue("PICK_STATUS").equals("Reserved")) {
                    rsvPickQty += Double.parseDouble(drPick.getValue("QTY").toString());
                }
            }
        }

        // 將預揀資訊的數量加總
        if (RsvData != null && RsvData.Rows.size() > 0) {
            for (DataRow drRsv : RsvData.Rows) {
                if (SheetData.Rows.get(position).getValue("SHEET_MST_KEY").equals(drRsv.getValue("SHEET_MST_KEY"))
                        && SheetData.Rows.get(position).getValue("SEQ").equals(drRsv.getValue("SEQ"))) {
                    rsvPickQty += Double.parseDouble(drRsv.getValue("QTY").toString());
                }
            }
        }

        if (IsPick)//已捒完項次
        {
            holder.Next.setVisibility(View.INVISIBLE);
            holder.Qty.setText(SheetData.Rows.get(position).getValue("TRX_QTY").toString());
            holder.QtyCaption.setText((Context.getResources().getString(R.string.QTY)));
        } else//未捒項次
        {
            holder.Next.setVisibility(View.VISIBLE);
            // holder.Qty.setText(String.format("%s/%s", String.valueOf(pickQty), String.valueOf(SheetData.Rows.get(position).getValue("TRX_QTY").toString())));
            holder.Qty.setText(String.format("%s / %s / %s", String.valueOf(rsvPickQty), String.valueOf(pickQty), String.valueOf(SheetData.Rows.get(position).getValue("TRX_QTY").toString())));
            holder.QtyCaption.setText((Context.getResources().getString(R.string.RSV_PICK_REQUIRE_QTY)));
        }

        return convertView;
    }
}
