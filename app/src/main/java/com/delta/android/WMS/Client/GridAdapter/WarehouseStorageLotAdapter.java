package com.delta.android.WMS.Client.GridAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

public class WarehouseStorageLotAdapter extends BaseAdapter {

    private String[][] elementData;
    private boolean blnIsWV;
    private LayoutInflater inflater;

    static class ViewHolder {
        TextView tvSkuLevel;
        TextView tvSkuNum;
//        TextView tvLotId;
        TextView tvQty;
//        TextView tvScrapQty, tvScrapQtyTitle;
        TextView tvMfgDate;
        TextView tvExpDate;
        TextView tvLotCode;
        TextView tvWoId;
    }

    public WarehouseStorageLotAdapter(DataTable dt, boolean blnIsWV, LayoutInflater inflater) {
        this.elementData = new String[dt.Rows.size()][8];
        for (int i = 0; i < dt.Rows.size(); i++) {
            elementData[i][0] = dt.Rows.get(i).getValue("SKU_NUM").toString();
            //elementData[i][0] = dt.Rows.get(i).getValue("LOT_ID").toString();
            elementData[i][1] = dt.Rows.get(i).getValue("QTY").toString();
            //elementData[i][2] = dt.Rows.get(i).getValue("SCRAP_QTY").toString();
            elementData[i][3] = dt.Rows.get(i).getValue("MFG_DATE").toString();
            elementData[i][4] = dt.Rows.get(i).getValue("EXP_DATE").toString();
            elementData[i][5] = dt.Rows.get(i).getValue("LOT_CODE").toString();
            elementData[i][6] = dt.Rows.get(i).getValue("WO_ID").toString();
            elementData[i][7] = dt.Rows.get(i).get("SKU_LEVEL").toString();
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
            convertView = this.inflater.inflate(R.layout.activity_wms_warehouse_storage_lot, null);

            holder.tvSkuLevel = convertView.findViewById(R.id.tvSkuLevel);
            holder.tvSkuNum = convertView.findViewById(R.id.tvSkuNum);
//            holder.tvLotId = convertView.findViewById(R.id.tvLotID);
            holder.tvQty = convertView.findViewById(R.id.tvQty);
//            holder.tvScrapQtyTitle = convertView.findViewById(R.id.tvScrapQty2);
//            holder.tvScrapQty = convertView.findViewById(R.id.tvScrapQty);
            holder.tvMfgDate = convertView.findViewById(R.id.tvMfgDate);
            holder.tvExpDate = convertView.findViewById(R.id.tvExpDate);
//            if (blnIsWV) {
//                holder.tvScrapQtyTitle.setVisibility(View.VISIBLE);
//                holder.tvScrapQty.setVisibility(View.VISIBLE);
//            } else {
//                holder.tvScrapQtyTitle.setVisibility(View.GONE);
//                holder.tvScrapQty.setVisibility(View.GONE);
//            }
            holder.tvLotCode = convertView.findViewById(R.id.tvLotCode);
            holder.tvWoId = convertView.findViewById(R.id.tvWoId);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvSkuLevel.setText(elementData[pos][7]);
        holder.tvSkuNum.setText(elementData[pos][0]);
//        holder.tvLotId.setText(elementData[pos][0]);
        holder.tvQty.setText(elementData[pos][1]);
//        holder.tvScrapQty.setText(elementData[pos][2]);
        holder.tvMfgDate.setText(elementData[pos][3]);
        holder.tvExpDate.setText(elementData[pos][4]);
        holder.tvLotCode.setText(elementData[pos][5]);
        holder.tvWoId.setText(elementData[pos][6]);

        return convertView;
    }
}
