package com.delta.android.WMS.Client.GridAdapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.delta.android.Core.Activity.ScanActivity;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;
import com.delta.android.WMS.Client.GoodReceiptReceiveActivity;
import com.google.zxing.integration.android.IntentIntegrator;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class GoodChangeBinGridAdapter extends BaseAdapter {

    private String[][] ElementData;
    private LayoutInflater Inflater; //加載Layout使用

    static class ViewHolder {
        TextView SkuLevel;
        TextView SkuNum;
        TextView ItemId;
        TextView ItemName;
        TextView Qty;
    }

    public GoodChangeBinGridAdapter(DataTable inputData, LayoutInflater inflater) {
        this.ElementData = new String[inputData.Rows.size()][5];
        for (int i = 0; i < inputData.Rows.size(); i++) {
            ElementData[i][0] = inputData.Rows.get(i).getValue("SKU_LEVEL").toString();
            ElementData[i][1] = inputData.Rows.get(i).getValue("SKU_NUM").toString();
            ElementData[i][2] = inputData.Rows.get(i).getValue("ITEM_ID").toString();
            ElementData[i][3] = inputData.Rows.get(i).getValue("ITEM_NAME").toString();
            ElementData[i][4] = new DecimalFormat("0.#").format(inputData.Rows.get(i).getValue("QTY"));
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
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = this.Inflater.inflate(R.layout.activity_wms_good_change_listview, null);
            holder.SkuLevel = convertView.findViewById(R.id.tvSkuLevel);
            holder.SkuNum = convertView.findViewById(R.id.tvSkuNum);
            holder.ItemId = convertView.findViewById(R.id.tvItemId);
            holder.ItemName = convertView.findViewById(R.id.tvItemName);
            holder.Qty = convertView.findViewById(R.id.tvQty);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.SkuLevel.setText(ElementData[position][0]);
        holder.SkuNum.setText(ElementData[position][1]);
        holder.ItemId.setText(ElementData[position][2]);
        holder.ItemName.setText(ElementData[position][3]);
        holder.Qty.setText(ElementData[position][4]);

        return convertView;
    }
}
