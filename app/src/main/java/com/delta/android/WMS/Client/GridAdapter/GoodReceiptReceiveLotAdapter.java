package com.delta.android.WMS.Client.GridAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

public class GoodReceiptReceiveLotAdapter extends BaseAdapter {

    private LayoutInflater Inflater; //加載Layout使用
    private DataTable dtLot;

    static class ViewHolder{
        TextView SkuLevel;
        TextView SkuNum;
        //TextView LotId;
        TextView Qty;
        TextView Uom;
        TextView Cmt;
        TextView MfgDate;
        TextView ExpDate;
    }

    public GoodReceiptReceiveLotAdapter(DataTable sheetData, LayoutInflater inflater){
//        this.ElementData = new String[sheetData.Rows.size()][6];
//        for (int i=0;i<sheetData.Rows.size();i++){
//            ElementData[i][0] = sheetData.Rows.get(i).getValue("LOT_ID").toString();
//            ElementData[i][1] = sheetData.Rows.get(i).getValue("QTY").toString();
//            ElementData[i][2] = sheetData.Rows.get(i).getValue("UOM").toString();
//            //ElementData[i][3] = sheetData.Rows.get(i).getValue("CMT").toString();
//            if (!sheetData.Rows.get(i).getValue("MFG_DATE").toString().equals(""))
//                ElementData[i][4] = sheetData.Rows.get(i).getValue("MFG_DATE").toString().substring(0,10);//.replace("-","/");
//            else ElementData[i][4] ="";
//            if (!sheetData.Rows.get(i).getValue("EXP_DATE").toString().equals(""))
//                ElementData[i][5] = sheetData.Rows.get(i).getValue("EXP_DATE").toString().substring(0,10);//.replace("-","/");
//            else ElementData[i][5] ="";
//        }
        this.Inflater = inflater;
        this.dtLot = sheetData;
    }
    @Override
    public int getCount() {
        return this.dtLot.Rows.size();
    }

    @Override
    public Object getItem(int position) { return this.dtLot.Rows.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null){

            holder = new ViewHolder();
            convertView = this.Inflater.inflate(R.layout.activity_wms_good_receipt_receive_lot_sn_listview,null);
            holder.SkuLevel = convertView.findViewById(R.id.tvGrLotGridSkuLevel);
            holder.SkuNum = convertView.findViewById(R.id.tvGrLotGridSkuNum);
            //holder.LotId = convertView.findViewById(R.id.tvGrLotGridSkuNum);
            holder.Qty = convertView.findViewById(R.id.tvGrLotGridQty);
            holder.Uom = convertView.findViewById(R.id.tvGrLotGridUom);
            //holder.Cmt = convertView.findViewById(R.id.tvGrLotGridCmt);
            holder.MfgDate = convertView.findViewById(R.id.tvGrDetGridMfgDate);
            holder.ExpDate = convertView.findViewById(R.id.tvGrDetGridExpDate);
            convertView.setTag(holder);

        }else{

            holder = (ViewHolder) convertView.getTag();

        }
        //將資訊放入holder內

        holder.SkuLevel.setText(dtLot.Rows.get(position).get("SKU_LEVEL").toString());
        holder.SkuNum.setText(dtLot.Rows.get(position).get("SKU_NUM").toString());
        holder.Qty.setText(dtLot.Rows.get(position).get("QTY").toString());
        holder.Uom.setText(dtLot.Rows.get(position).get("UOM").toString());
        //holder.Cmt.setText(ElementData[position][3]);
        holder.MfgDate.setText(dtLot.Rows.get(position).get("MFG_DATE").toString());
        holder.ExpDate.setText(dtLot.Rows.get(position).get("EXP_DATE").toString());

        return convertView;
    }
}
