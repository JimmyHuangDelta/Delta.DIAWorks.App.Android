package com.delta.android.WMS.Client.GridAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

import java.util.HashMap;

public class GoodReceiptReceiveDetaGridAdapter extends BaseAdapter {

    private String[][] ElementData;
    private LayoutInflater Inflater; //加載Layout使用

    static class ViewHolder{
        TextView ItemId;
        TextView SheetId;
        TextView ItemName;
        TextView StorageId;
        TextView LotId;
        TextView PoNo;
        TextView PoSeq;
        TextView Qty;
        TextView Uom;
        TextView MfgDate;
        TextView ExpDate;
        TextView SkipQc;
    }

    public GoodReceiptReceiveDetaGridAdapter(DataTable sheetData, HashMap<String,Double> SeqQtyOfAllLot, HashMap<String,String> SeqSkipQcOfAllLot,LayoutInflater inflater){
        this.ElementData = new String[sheetData.Rows.size()][12];
        for (int i=0;i<sheetData.Rows.size();i++){
            ElementData[i][0] = sheetData.Rows.get(i).getValue("ITEM_ID").toString();
            ElementData[i][1] = sheetData.Rows.get(i).getValue("GR_ID").toString();
            ElementData[i][2] = sheetData.Rows.get(i).getValue("ITEM_NAME").toString();
            ElementData[i][3] = sheetData.Rows.get(i).getValue("STORAGE_ID").toString();
            //ElementData[i][4] = sheetData.Rows.get(i).getValue("PO_NO").toString();
            //ElementData[i][5] = sheetData.Rows.get(i).getValue("PO_SEQ").toString();
            ElementData[i][6] = sheetData.Rows.get(i).getValue("LOT_ID").toString();
            ElementData[i][7] = sheetData.Rows.get(i).getValue("QTY").toString();
            ElementData[i][8] = sheetData.Rows.get(i).getValue("UOM").toString();
//            if (!sheetData.Rows.get(i).getValue("MFG_DATE").toString().equals(""))
//                ElementData[i][9] = sheetData.Rows.get(i).getValue("MFG_DATE").toString().substring(0,10).replace("-","/");
//            else ElementData[i][9] ="";
//            if (!sheetData.Rows.get(i).getValue("EXP_DATE").toString().equals(""))
//                ElementData[i][10] = sheetData.Rows.get(i).getValue("EXP_DATE").toString().substring(0,10).replace("-","/");
//            else ElementData[i][10] ="";
            ElementData[i][4] = "false";
            ElementData[i][5] = String.valueOf(SeqQtyOfAllLot.get( sheetData.Rows.get(i).getValue("SEQ").toString()));
            ElementData[i][9] = SeqSkipQcOfAllLot.get( sheetData.Rows.get(i).getValue("SEQ").toString());
            ElementData[i][10] = sheetData.Rows.get(i).getValue("SKIP_QC").toString();
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
        //return this.CheckSkipQc[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null){
            holder = new ViewHolder();
            convertView = this.Inflater.inflate(R.layout.activity_wms_good_receipt_receive_detail_listview,null);
            holder.ItemId = convertView.findViewById(R.id.tvGrDetGridItemId);
            holder.SheetId = convertView.findViewById(R.id.tvGrDetGridSheetId);
            holder.ItemName = convertView.findViewById(R.id.tvGrDetGridItemName);
            holder.StorageId = convertView.findViewById(R.id.tvGrDetGridStorageId);
            //holder.PoNo = convertView.findViewById(R.id.tvGrDetGridPoNo);
            //holder.PoSeq = convertView.findViewById(R.id.tvGrDetGridPoSeq);
            holder.LotId = convertView.findViewById(R.id.tvGrDetGridLotId);
            holder.Qty = convertView.findViewById(R.id.tvGrDetGridQty);
            holder.Uom = convertView.findViewById(R.id.tvGrDetGridUom);
            //holder.MfgDate = convertView.findViewById(R.id.tvGrDetGridMfgDate);
            ///holder.ExpDate = convertView.findViewById(R.id.tvGrDetGridExpDate);
            holder.SkipQc = convertView.findViewById(R.id.tvGrDetGridSkipQc);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        //將資訊放入holder內
        holder.ItemId.setText(ElementData[position][0]);
        holder.SheetId.setText(ElementData[position][1]);
        holder.ItemName.setText(ElementData[position][2]);
        holder.StorageId.setText(ElementData[position][3]);
        //holder.PoNo.setText(ElementData[position][4]);
        //holder.PoSeq.setText(ElementData[position][5]);
        holder.LotId.setText(ElementData[position][6]);
        holder.Qty.setText(ElementData[position][5]+"/"+ElementData[position][7]);
        holder.Uom.setText(ElementData[position][8]);
        //holder.MfgDate.setText(ElementData[position][9]);
        //holder.ExpDate.setText(ElementData[position][10]);

        //holder.CheckShipQc.setOnClickListener(click);
        holder.SkipQc.setText(ElementData[position][10]);

        return convertView;
    }

    public View.OnClickListener click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //CheckBox c = v.findViewById(R.id.cbGrDetGridSkipQc);
        }
    };
}
