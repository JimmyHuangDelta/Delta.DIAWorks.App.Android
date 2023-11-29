package com.delta.android.PMS.Client.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.delta.android.R;

import java.util.ArrayList;
import java.util.HashMap;

public class FailWoAdapter extends BaseAdapter {

    private ArrayList mData;//定义数据。
    private LayoutInflater mInflater;//定义Inflater,加载我们自定义的布局。

    public FailWoAdapter(LayoutInflater inflater, ArrayList data) {
        mInflater = inflater;
        mData = data;
    }

    //回傳要覆蓋的工單
    public ArrayList<HashMap<String,String>> getCoverData(){
        ArrayList<HashMap<String,String>> coverData = new ArrayList<>();
        for (int i =0;i<mData.size();i++){
            if (((HashMap)mData.get(i)).get("IS_COVER").toString().toUpperCase().contentEquals("Y")){
                coverData.add((HashMap<String, String>) mData.get(i));
            }
        }

        return  coverData;
    }

    public ArrayList<HashMap<String,String>> getIgnoreData(){
        ArrayList<HashMap<String,String>> ignoreData = new ArrayList<>();
        for (int i =0;i<mData.size();i++){
            if (((HashMap)mData.get(i)).get("IS_COVER").toString().toUpperCase().contentEquals("N")){
                ignoreData.add((HashMap<String, String>) mData.get(i));
            }
        }

        return  ignoreData;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View viewChangeWo = mInflater.inflate(R.layout.activity_pms_fail_wo_listview, null);

        TextView txtWoType = (TextView) viewChangeWo.findViewById(R.id.tvFailWoType);
        TextView txtWoId = (TextView) viewChangeWo.findViewById(R.id.tvFailWoId);
        TextView txtMsg = (TextView) viewChangeWo.findViewById(R.id.tvFailErrorMsg);
        Switch swAction = (Switch) viewChangeWo.findViewById(R.id.swCover);
        final HashMap<String,String> data = (HashMap<String, String>) mData.get(position);

        if (data.get("IS_COVER").toUpperCase().contentEquals("Y")){
            swAction.setChecked(true);
        }else {
            swAction.setChecked(false);
        }

        swAction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    data.put("IS_COVER", "Y");
                } else {
                    data.put("IS_COVER", "N");
                }
            }
        });

        txtWoType.setText(data.get("WO_TYPE"));
        txtWoId.setText(data.get("WO_ID"));
        txtMsg.setText(data.get("ERROR_MSG"));
        return viewChangeWo;
    }
}
