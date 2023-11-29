package com.delta.android.WMS.Client.GridAdapter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

public class GoodReceiptReceiveExtendGridAdapter extends BaseAdapter {

    private LayoutInflater Inflater;
    private DataTable dtExtend;

    static class ViewHolder {
        TextView tvExtentName;
        TextView tvExtentValue;
    }

    public GoodReceiptReceiveExtendGridAdapter(DataTable dt, LayoutInflater inflater){
        this.Inflater = inflater;
        this.dtExtend = dt;
    }

    @Override
    public int getCount() {return this.dtExtend.Rows.size();}

    @Override
    public Object getItem(int position) {return this.dtExtend.Rows.get(position);}

    @Override
    public long getItemId(int position) {return position;}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GoodReceiptReceiveExtendGridAdapter.ViewHolder holder;

        if(convertView == null){
            holder = new GoodReceiptReceiveExtendGridAdapter.ViewHolder();
            convertView = this.Inflater.inflate(R.layout.activity_wms_good_receipt_receive_extend_listview, null);
            holder.tvExtentName = convertView.findViewById(R.id.tvExtentName);
            holder.tvExtentValue = convertView.findViewById(R.id.tvExtentValue);
            convertView.setTag(holder);
        }else{
            holder = (GoodReceiptReceiveExtendGridAdapter.ViewHolder)convertView.getTag();
        }

        //將資訊放入holder內
        holder.tvExtentName.setText(dtExtend.Rows.get(position).getValue("BARCODE_VARIABLE_ID").toString());
        holder.tvExtentValue.setText(dtExtend.Rows.get(position).getValue("BARCODE_VALUE").toString());

        return convertView;
    }
}
