package com.delta.android.WMS.Client.GridAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataColumnCollection;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

public class WarehouseStorageDetAdapter extends BaseAdapter {

    private String[][] elementData;
    private boolean blnIsWV;
    private LayoutInflater inflater;
    private DataTable dtWvr = null;

    static class ViewHolder {
        TextView itemId;
        //TextView sheetId;
        TextView itemName;
        TextView storageId;
        TextView lotCode;
        TextView qty;
        //TextView scrapQty, scrapQtyTitle;
        //TextView binId;
        TextView woId;
    }

    public WarehouseStorageDetAdapter(DataTable dt, DataTable dtWvr, boolean blnIsWV, LayoutInflater inflater) {
        this.elementData = new String[dt.Rows.size()][11];
        for (int i = 0; i < dt.Rows.size(); i++) {
            elementData[i][0] = dt.Rows.get(i).getValue("ITEM_ID").toString();
            if (((DataColumnCollection) dt.getColumns()).get("WV_ID") != null)
                elementData[i][1] = dt.Rows.get(i).getValue("WV_ID").toString();
            if (((DataColumnCollection) dt.getColumns()).get("MTL_SHEET_ID") != null)
                elementData[i][1] = dt.Rows.get(i).getValue("MTL_SHEET_ID").toString();
            elementData[i][2] = dt.Rows.get(i).getValue("ITEM_NAME").toString();
            elementData[i][3] = dt.Rows.get(i).getValue("STORAGE_ID").toString();
            elementData[i][4] = dt.Rows.get(i).getValue("LOT_ID").toString();
            elementData[i][5] = dt.Rows.get(i).getValue("QTY").toString();
            elementData[i][6] = dt.Rows.get(i).getValue("SCRAP_QTY").toString();
            //elementData[i][7] = dt.Rows.get(i).getValue("BIN_ID").toString();
            elementData[i][8] = dt.Rows.get(i).getValue("PROC_QTY").toString();
            elementData[i][9] = dt.Rows.get(i).getValue("WO_ID").toString();
            elementData[i][10] = dt.Rows.get(i).getValue("SEQ").toString();
        }
        this.blnIsWV = blnIsWV;
        this.inflater = inflater;
        this.dtWvr = dtWvr;
    }

    public WarehouseStorageDetAdapter(DataTable dt, boolean blnIsWV, LayoutInflater inflater) {
        this.elementData = new String[dt.Rows.size()][10];
        for (int i = 0; i < dt.Rows.size(); i++) {
            elementData[i][0] = dt.Rows.get(i).getValue("ITEM_ID").toString();
            if (((DataColumnCollection) dt.getColumns()).get("WV_ID") != null)
                elementData[i][1] = dt.Rows.get(i).getValue("WV_ID").toString();
            if (((DataColumnCollection) dt.getColumns()).get("MTL_SHEET_ID") != null)
                elementData[i][1] = dt.Rows.get(i).getValue("MTL_SHEET_ID").toString();
            elementData[i][2] = dt.Rows.get(i).getValue("ITEM_NAME").toString();
            elementData[i][3] = dt.Rows.get(i).getValue("STORAGE_ID").toString();
            elementData[i][4] = dt.Rows.get(i).getValue("LOT_ID").toString();
            elementData[i][5] = dt.Rows.get(i).getValue("QTY").toString();
            elementData[i][6] = dt.Rows.get(i).getValue("SCRAP_QTY").toString();
            //elementData[i][7] = dt.Rows.get(i).getValue("BIN_ID").toString();
            elementData[i][8] = dt.Rows.get(i).getValue("PROC_QTY").toString();
            elementData[i][9] = dt.Rows.get(i).getValue("WO_ID").toString();
        }
        this.blnIsWV = blnIsWV;
        this.inflater = inflater;
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
            convertView = this.inflater.inflate(R.layout.activity_wms_warehouse_storage_det_item_listview, null);
            holder.itemId = convertView.findViewById(R.id.tvWvDetGridItemId);
            //holder.sheetId = convertView.findViewById(R.id.tvWvDetGridSheetId);
            holder.itemName = convertView.findViewById(R.id.tvWvDetGridItemName);
            holder.storageId = convertView.findViewById(R.id.tvWvDetGridStorageId);
            holder.lotCode = convertView.findViewById(R.id.tvWvDetGridLotCode);
            holder.qty = convertView.findViewById(R.id.tvWvDetGridQty);
            holder.woId = convertView.findViewById(R.id.tvWvDetGridWoId);
            //holder.scrapQtyTitle = convertView.findViewById(R.id.tvWvDetGridScrapQty2);
            //holder.scrapQty = convertView.findViewById(R.id.tvWvDetGridScrapQty);
            //holder.binId = convertView.findViewById(R.id.tvWvDetGridBinId);

//            if (blnIsWV) {
//                holder.scrapQtyTitle.setVisibility(View.VISIBLE);
//                holder.scrapQty.setVisibility(View.VISIBLE);
//            } else {
//                holder.scrapQtyTitle.setVisibility(View.GONE);
//                holder.scrapQty.setVisibility(View.GONE);
//            }

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.itemId.setText(elementData[pos][0]);
        //holder.sheetId.setText(elementData[pos][1]);
        holder.itemName.setText(elementData[pos][2]);
        holder.storageId.setText(elementData[pos][3]);
        holder.woId.setText(elementData[pos][9]);
        holder.lotCode.setText(elementData[pos][4]);
        if (dtWvr == null)
            holder.qty.setText(elementData[pos][8] + " / " +elementData[pos][5]);
        else {
            Double procQty = 0.0;
            String seq = elementData[pos][10];
            for (DataRow dr : dtWvr.Rows) {
                if (seq.equals(dr.get("SEQ").toString()))
                    procQty += Double.parseDouble(dr.get("QTY").toString());
            }
            holder.qty.setText(procQty + " / " +elementData[pos][5]);
        }
        //holder.scrapQty.setText(elementData[pos][6]);
        //holder.binId.setText(elementData[pos][7]);

        return convertView;
    }
}
