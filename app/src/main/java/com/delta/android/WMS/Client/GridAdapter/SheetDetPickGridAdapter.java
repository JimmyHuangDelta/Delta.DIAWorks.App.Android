package com.delta.android.WMS.Client.GridAdapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;
import com.delta.android.WMS.Client.GoodPickExecutedActivity;

import java.util.List;

public class SheetDetPickGridAdapter extends BaseAdapter {
    //private String[][] ElementData;
    private List<GoodPickExecutedActivity.RegisterObj> RegisterData;
    private LayoutInflater Inflater;

    static class ViewHolder {
        TextView LotId;
        TextView BinId;
        TextView Qty;
        TextView BatchId;
        TextView BatchPosition;
        TextView Uom;
        TextView MfgDate;
        TextView ExpDate;
        //CheckBox SelectedRegister;
    }

    public SheetDetPickGridAdapter(List<GoodPickExecutedActivity.RegisterObj> elementData, LayoutInflater inflater) {
        RegisterData = elementData;
        this.Inflater = inflater;
    }

    @Override
    public int getCount() {
        return this.RegisterData.size();
    }

    @Override
    public Object getItem(int position) {
        return this.RegisterData.get(position);
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
            convertView = this.Inflater.inflate(R.layout.activity_wms_good_pick_executed_listview, null);
            holder.LotId = convertView.findViewById(R.id.tvPickedGridLotId);
            holder.BinId = convertView.findViewById(R.id.tvPickedGridBinId);
            holder.Qty = convertView.findViewById(R.id.tvPickedGridQty);
            holder.BatchId = convertView.findViewById(R.id.tvPickedGridBatchId);
            holder.BatchPosition = convertView.findViewById(R.id.tvPickedGridBatchPosition);
            holder.Uom = convertView.findViewById(R.id.tvPickedGridUom);
            holder.MfgDate = convertView.findViewById(R.id.tvPickedGridMfgDate);
            holder.ExpDate = convertView.findViewById(R.id.tvPickedGridExpDate);
            //holder.SelectedRegister = convertView.findViewById(R.id.cbPickedGridSelectedRegister);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        GoodPickExecutedActivity.RegisterObj reg = RegisterData.get(position);
        holder.LotId.setText(reg.RegisterId);
        holder.BinId.setText(reg.BinId);
        holder.Qty.setText(String.valueOf(reg.Qty) + " / " + reg.NeedToPickQty + " / " + reg.ReserveQty);
        holder.BatchId.setText(reg.BatchId);
        holder.BatchPosition.setText(reg.BatchPosition);
        holder.Uom.setText(reg.ItemUom);
        holder.MfgDate.setText(reg.MfgDate);
        holder.ExpDate.setText(reg.ExpDate);
        //holder.SelectedRegister.setChecked(Boolean.getBoolean(ElementData[position][4]));
        if (reg.IsReserved)
            convertView.setBackgroundColor(Color.argb(50, 0, 255, 0));
        else
            convertView.setBackgroundColor(Color.WHITE);

        return convertView;
    }
}
