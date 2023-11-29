package com.delta.android.PMS.Client.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

import java.util.ArrayList;
import java.util.HashMap;

public class SopAdapter extends BaseAdapter {

    private DataTable dtData;//定义数据。
    private LayoutInflater mInflater;//定义Inflater,加载我们自定义的布局。
    ArrayList<HashMap<String, String>> arData = new ArrayList<>();

    public SopAdapter(LayoutInflater inflater, DataTable data) {
        mInflater = inflater;
        dtData = data;

        arData = new ArrayList<>();
        for (int i = 0; i < dtData.Rows.size(); i++) {
            HashMap<String, String> sop = new HashMap<>();
            sop.put("IDNAME", dtData.Rows.get(i).get("IDNAME").toString());
            sop.put("URL",dtData.Rows.get(i).get("URL").toString());
            sop.put("IS_EXIST",dtData.Rows.get(i).get("is_exist").toString());
            sop.put("IS_DOWNLOAD",dtData.Rows.get(i).get("is_download").toString());

//            if (dtData.Rows.get(i).get("is_exist").toString().toUpperCase().contentEquals("Y")) {
//                sop.put("IS_DOWNLOAD", "N");
//            }else {
//                sop.put("IS_DOWNLOAD", "Y");
//            }

            arData.add(sop);
        }
    }

    public DataTable getDtData() {
        return dtData;
    }

    public ArrayList GetData(){
        return arData;
    }

    public int getDownloadCount(){
        int cnt = 0;

        for (int i =0;i<arData.size();i++){
            if (arData.get(i).get("IS_DOWNLOAD").toUpperCase().contentEquals("Y")){
                cnt++;
            }
        }

        return cnt;
    }

    @Override
    public int getCount() {
        return arData.size();
    }

    @Override
    public Object getItem(int position) {
        return dtData.Rows.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View vDownload = mInflater.inflate(R.layout.activity_pms_sop_listview, null);

        TextView tvDocNo = (TextView) vDownload.findViewById(R.id.tvLsDocNo);
        Switch swIsDownload = (Switch) vDownload.findViewById(R.id.swSopDownload);

        swIsDownload.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    arData.get(position).put("IS_DOWNLOAD", "Y");
                } else {
                    arData.get(position).put("IS_DOWNLOAD", "N");
                }
            }
        });

        tvDocNo.setText(arData.get(position).get("IDNAME"));
        if (arData.get(position).get("IS_EXIST").toUpperCase().contentEquals("N")) {
            vDownload.setBackgroundColor(Color.argb(255, 255, 255, 156));
        } else {
            vDownload.setBackgroundColor(Color.argb(255, 197, 234, 179));
        }

        if (arData.get(position).get("IS_DOWNLOAD").toUpperCase().contentEquals("Y")) {
            swIsDownload.setChecked(true);
        } else {
            swIsDownload.setChecked(false);
        }

        return vDownload;
    }
}
