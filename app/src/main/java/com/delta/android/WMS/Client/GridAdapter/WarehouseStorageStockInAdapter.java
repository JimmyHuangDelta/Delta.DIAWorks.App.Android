package com.delta.android.WMS.Client.GridAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class WarehouseStorageStockInAdapter extends BaseAdapter {

    private String[][] elementData;
    private LayoutInflater inflater;

    static class ViewHolder {
        TextView tvSeq;
        TextView tvLotId;
        TextView tvQty;
        TextView tvScrapQty;
        TextView tvMfgDate;
        TextView tvExpDate;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }
}
