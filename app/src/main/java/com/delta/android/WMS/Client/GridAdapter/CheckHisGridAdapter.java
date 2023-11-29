package com.delta.android.WMS.Client.GridAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

public class CheckHisGridAdapter extends BaseAdapter {
    private String[][] ElementData;
    private LayoutInflater Inflater; //加載Layout使用
    private boolean IsCycle;

    static class ViewHolder {
        TextView ItemId;
        TextView BinId;
        TextView LotId;
        TextView FirstCountQty;
        TextView FirstCountLabel;
        TextView SecondCountQty;
        TextView SecondCountLabel;
    }

    public CheckHisGridAdapter(DataTable sheetData, boolean isCycle, LayoutInflater inflater) {
        this.ElementData = new String[sheetData.Rows.size()][5];
        this.IsCycle = isCycle;
        for (int i = 0; i < sheetData.Rows.size(); i++) {
            ElementData[i][0] = sheetData.Rows.get(i).getValue("ITEM_ID").toString();
            ElementData[i][1] = sheetData.Rows.get(i).getValue("BIN_ID").toString();
            ElementData[i][2] = sheetData.Rows.get(i).getValue("LOT_ID").toString();
            ElementData[i][3] = sheetData.Rows.get(i).getValue("FIRST_COUNT_QTY").toString();
            ElementData[i][4] = sheetData.Rows.get(i).getValue("SECOND_COUNT_QTY").toString();
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
            convertView = this.Inflater.inflate(R.layout.activity_wms_good_check_his_list_view, null);
            holder.ItemId = convertView.findViewById(R.id.tvCheckHisItemId);
            holder.BinId = convertView.findViewById(R.id.tvCheckHisBinId);
            holder.LotId = convertView.findViewById(R.id.tvCheckHisLotId);
            holder.FirstCountQty = convertView.findViewById(R.id.tvCheckHisFirstCountQty);
            holder.FirstCountLabel = convertView.findViewById(R.id.tvFirstCountLabel);
            holder.SecondCountQty = convertView.findViewById(R.id.tvCheckHisSecondCountQty);
            holder.SecondCountLabel = convertView.findViewById(R.id.tvSecondCountLabel);
            convertView.setTag(holder);

            if (this.IsCycle) {
                holder.FirstCountLabel.setText(R.string.COUNT_QTY);
                holder.SecondCountLabel.setVisibility(View.INVISIBLE);
                holder.SecondCountQty.setVisibility(View.INVISIBLE);
            }

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //將資訊放入holder內
        holder.ItemId.setText(ElementData[position][0]);
        holder.BinId.setText(ElementData[position][1]);
        holder.LotId.setText(ElementData[position][2]);
        holder.FirstCountQty.setText(ElementData[position][3]);
        holder.SecondCountQty.setText(ElementData[position][4]);

        return convertView;
    }
}
