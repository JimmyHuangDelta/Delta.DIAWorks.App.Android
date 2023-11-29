package com.delta.android.WMS.Client.GridAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReceiveSelectTempBinGridAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater Inflater;
    private DataTable dtSelect;
    private HashMap<String, ArrayList<String>> mapItemStorageBin = new HashMap<>();

    static class ViewHolder{
        TextView ItemID;
        TextView ItemName;
        TextView StorageID;
        //TextView BinID;
        Spinner cmbBinID;
    }

    public ReceiveSelectTempBinGridAdapter(Context context, DataTable dtSelect, HashMap<String, ArrayList<String>> mapItemStorageBin, LayoutInflater inflater){
        this.context = context;
        this.Inflater = inflater;
        this.dtSelect = dtSelect;
        this.mapItemStorageBin = mapItemStorageBin;
    }

    @Override
    public int getCount() {return this.dtSelect.Rows.size();}

    @Override
    public Object getItem(int position) {return this.dtSelect.Rows.get(position);}

    @Override
    public long getItemId(int position) {return position;}

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        ViewHolder holder;

        if(convertView == null){
            holder = new ViewHolder();
            convertView = this.Inflater.inflate(R.layout.activity_receive_select_temp_bin_listview, null);
            holder.ItemID = convertView.findViewById(R.id.tvReceiveItemId);
            holder.ItemName = convertView.findViewById(R.id.tvReceiveItemName);
            holder.StorageID = convertView.findViewById(R.id.tvReceiveStorageId);
            holder.cmbBinID = convertView.findViewById(R.id.cmbBinID);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder)convertView.getTag();
        }

        //將資訊放入holder內
        holder.ItemID.setText(dtSelect.Rows.get(position).getValue("ITEM_ID").toString());
        holder.ItemName.setText(dtSelect.Rows.get(position).getValue("ITEM_NAME").toString());
        holder.StorageID.setText(dtSelect.Rows.get(position).getValue("STORAGE_ID").toString());

        final String strStorage = dtSelect.getValue(position, "STORAGE_ID").toString();
        final String strItem = dtSelect.getValue(position, "ITEM_ID").toString();
        ArrayAdapter<String> adapterBin = new SimpleArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, mapItemStorageBin.get(strStorage + "_" + strItem));
        holder.cmbBinID.setAdapter(adapterBin);
        if (mapItemStorageBin.get(strStorage + "_" + strItem).size() > 2)
            holder.cmbBinID.setSelection(mapItemStorageBin.get(strStorage + "_" + strItem).size() - 1, true);

        return convertView;
    }

    private class SimpleArrayAdapter<T> extends ArrayAdapter {
        public SimpleArrayAdapter(Context context, int resource, List<T> objects) {
            super(context, resource, objects);
        }

        //複寫這個方法，使返回的數據沒有最後一項
        @Override
        public int getCount() {
            // don't display last item. It is used as hint.
            int count = super.getCount();
            return count > 0 ? count - 1 : count;
        }

    }
}
