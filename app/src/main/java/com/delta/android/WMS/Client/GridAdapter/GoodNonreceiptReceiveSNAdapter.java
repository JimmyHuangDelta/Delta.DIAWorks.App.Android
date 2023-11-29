package com.delta.android.WMS.Client.GridAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

import org.w3c.dom.Text;

public class GoodNonreceiptReceiveSNAdapter extends BaseAdapter {

    private LayoutInflater Inflater;
    private DataTable dtSN;

    static class ViewHolder{
        TextView SN;
    }

    public GoodNonreceiptReceiveSNAdapter(DataTable dtSN, LayoutInflater inflater){
        this.Inflater = inflater;
        this.dtSN = dtSN;
    }

    @Override
    public int getCount() {
        return this.dtSN.Rows.size();
    }

    @Override
    public Object getItem(int position) {
        return this.dtSN.Rows.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null){
            holder = new ViewHolder();
            convertView = this.Inflater.inflate(R.layout.activity_good_nonreceipt_receive_lot_sn_listview, null);
            holder.SN = convertView.findViewById(R.id.tvReceiveSN);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.SN.setText(dtSN.Rows.get(position).get("SN_ID").toString());

        return convertView;
    }
}
