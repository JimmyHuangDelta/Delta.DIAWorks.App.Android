package com.delta.android.WMS.Client.GridAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

public class GoodNonreceiptReceiveGridAdapter extends BaseAdapter {

    private LayoutInflater Inflater;
    private DataTable dtSheetReceive;

    static class ViewHolder{
        TextView ItemID;
//        TextView ItemName;
        TextView StorageID;
        TextView SkuLevel;
        TextView SkuNum;
//        TextView LotID;
        TextView SkipFlag;
        TextView Qty;
        //Button btnAddSN;
    }

    public GoodNonreceiptReceiveGridAdapter(DataTable dtSheetReceive, LayoutInflater inflater){

        this.Inflater = inflater;
        this.dtSheetReceive = dtSheetReceive;
        /*this.ElementData = new String[sheetData.Rows.size()][11];
        for (int i = 0; i < sheetData.Rows.size(); i++){
            ElementData[i][0] = sheetData.Rows.get(i).getValue("ITEM_ID").toString();
            ElementData[i][1] = sheetData.Rows.get(i).getValue("ITEM_NAME").toString();
            ElementData[i][2] = sheetData.Rows.get(i).getValue("STORAGE_ID").toString();
            ElementData[i][3] = sheetData.Rows.get(i).getValue("LOT_ID").toString();
            ElementData[i][4] = sheetData.Rows.get(i).getValue("SKIP_FLAG").toString();
        }
        this.Inflater = inflater;*/
    }


    @Override
    public int getCount() {
        return this.dtSheetReceive.Rows.size();
    }

    @Override
    public Object getItem(int position) {
        return this.dtSheetReceive.Rows.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        //final  ViewHolder holder;
        if (convertView == null){
            holder = new ViewHolder();
            convertView = this.Inflater.inflate(R.layout.activity_good_nonreceipt_receive_listview, null);
            holder.ItemID = convertView.findViewById(R.id.tvReceiveItemId);
//            holder.ItemName = convertView.findViewById(R.id.tvReceiveItemName);
            holder.StorageID = convertView.findViewById(R.id.tvReceiveStorageId);
            holder.SkuLevel = convertView.findViewById(R.id.tvReceiveSkuLevel);
            holder.SkuNum = convertView.findViewById(R.id.tvReceiveSkuNum);
//            holder.LotID = convertView.findViewById(R.id.tvReceiveLotId);
            holder.SkipFlag = convertView.findViewById(R.id.tvReceiveSkipFlag);
            holder.Qty = convertView.findViewById(R.id.tvReceiveQty);
            //holder.btnAddSN = convertView.findViewById(R.id.btnAddSN);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        //將資訊放入holder內
        holder.ItemID.setText(dtSheetReceive.Rows.get(position).getValue("ITEM_ID").toString());
//        holder.ItemName.setText(dtSheetReceive.Rows.get(position).getValue("ITEM_NAME").toString());
        holder.StorageID.setText(dtSheetReceive.Rows.get(position).getValue("STORAGE_ID").toString());
        holder.SkuLevel.setText(dtSheetReceive.Rows.get(position).getValue("SKU_LEVEL").toString());
        holder.SkuNum.setText(dtSheetReceive.Rows.get(position).getValue("SKU_NUM").toString());
//        holder.LotID.setText(dtSheetReceive.Rows.get(position).getValue("LOT_ID").toString());
        holder.SkipFlag.setText(dtSheetReceive.Rows.get(position).getValue("SKIP_QC").toString());
        holder.Qty.setText(dtSheetReceive.Rows.get(position).getValue("QTY").toString());

/*
        //將資訊放入holder內
        holder.ItemID.setText(ElementData[position][0]);
        holder.ItemName.setText(ElementData[position][1]);
        holder.StorageID.setText(ElementData[position][2]);
        holder.LotID.setText(ElementData[position][3]);
        holder.SkipFlag.setText(ElementData[position][4]);*/

        return convertView;
    }
}
