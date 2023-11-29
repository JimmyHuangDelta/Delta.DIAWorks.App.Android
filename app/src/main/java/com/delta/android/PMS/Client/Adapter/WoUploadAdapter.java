package com.delta.android.PMS.Client.Adapter;

import android.app.Activity;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.delta.android.PMS.OffLineData.Data;
import com.delta.android.R;

import java.util.ArrayList;
import java.util.HashMap;

public class WoUploadAdapter extends BaseAdapter {

    private ArrayList mData;//定义数据。
    private LayoutInflater mInflater;//定义Inflater,加载我们自定义的布局。
    Data offData;

    public WoUploadAdapter(LayoutInflater inflater, ArrayList data) {
        mInflater = inflater;
        mData = data;
        offData = new Data(inflater.getContext());
    }

    public ArrayList getUploadData() {
        return mData;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        //获得ListView中的view
        String woType = ((HashMap) mData.get(position)).get("MRO_WO_TYPE").toString();

        if (woType.toUpperCase().contentEquals("PM") ||
                woType.toUpperCase().contentEquals("CHECK")) {
            View viewPm = mInflater.inflate(R.layout.activity_pms_upload_pm_listview, null);
            TextView tvWoid = (TextView) viewPm.findViewById(R.id.tvUploadPmWo);
            TextView tvWoType = (TextView) viewPm.findViewById(R.id.tvUploadPmWoType);
            TextView tvEqpid = (TextView) viewPm.findViewById(R.id.tvUploadPmEqp);
            TextView tvPmid = (TextView) viewPm.findViewById(R.id.tvUploadPmContent);
            TextView tvPmDate = (TextView) viewPm.findViewById(R.id.tvUploadPmDate);
            TextView tvStatus = (TextView) viewPm.findViewById(R.id.tvUploadPmStatus);
            TextView tvIsTrans = (TextView) viewPm.findViewById(R.id.tvIsTrans);
            TextView tvCallFixType = (TextView) viewPm.findViewById(R.id.tvCallFixType);
            TextView tvFailStart = (TextView) viewPm.findViewById(R.id.tvFailStartDate);
            TextView tvFail = (TextView) viewPm.findViewById(R.id.tvFailId);
            Switch swUpload = (Switch) viewPm.findViewById(R.id.swUpload);

            tvWoid.setText(((HashMap) mData.get(position)).get("MRO_WO_ID").toString());
            tvWoType.setText(((HashMap) mData.get(position)).get("MRO_WO_TYPE").toString());
            tvEqpid.setText(((HashMap) mData.get(position)).get("EQP_ID").toString());
            tvPmid.setText(((HashMap) mData.get(position)).get("PM_ID").toString());
            tvPmDate.setText(((HashMap) mData.get(position)).get("PLAN_DT").toString().replace("T", " "));
            tvStatus.setText(((HashMap) mData.get(position)).get("WO_STATUS").toString());

            //檢查是否有轉拋的維修工單
            Cursor csTransRepair = offData.getReadableDatabase().query("TEMP_REPAIR", null, "MRO_WO_ID = ?", new String[]{((HashMap) mData.get(position)).get("MRO_WO_ID").toString()},
                    null, null, null);

            if (csTransRepair.getCount() > 0) {
                csTransRepair.moveToFirst();
                StringBuilder sbFails = new StringBuilder();
                tvIsTrans.setText("Y");
                tvCallFixType.setText(csTransRepair.getString(csTransRepair.getColumnIndex("CALL_FIX_TYPE_ID")));
                tvFailStart.setText(csTransRepair.getString(csTransRepair.getColumnIndex("FAIL_DT")));

                for (int i = 0; i < csTransRepair.getCount(); i++) {
                    sbFails.append("," + csTransRepair.getString(csTransRepair.getColumnIndex("FAIL_ID")));
                    csTransRepair.moveToNext();
                }

                tvFail.setText(sbFails.toString().substring(1));
            } else {
                tvIsTrans.setText("N");
                tvCallFixType.setText("");
                tvFailStart.setText("");
                tvFail.setText("");
            }

            swUpload.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        ((HashMap) mData.get(position)).put("IS_UPLOAD", "Y");
                    } else {
                        ((HashMap) mData.get(position)).put("IS_UPLOAD", "N");
                    }
                }
            });
            return viewPm;
        } else {
            View viewRepair = mInflater.inflate(R.layout.activity_pms_upload_repair_listview, null);
            TextView tvWoid = (TextView) viewRepair.findViewById(R.id.tvUploadRepairWo);
            TextView tvWoType = (TextView) viewRepair.findViewById(R.id.tvUploadRepairWoType);
            TextView tvEqpid = (TextView) viewRepair.findViewById(R.id.tvUploadRepairEqp);
            TextView tvRepairDate = (TextView) viewRepair.findViewById(R.id.tvUploadRepairDate);
            TextView tvStatus = (TextView) viewRepair.findViewById(R.id.tvUploadRepairStatus);
            Switch swUpload = (Switch) viewRepair.findViewById(R.id.swUpload);

            tvWoid.setText(((HashMap) mData.get(position)).get("MRO_WO_ID").toString());
            tvWoType.setText(((HashMap) mData.get(position)).get("MRO_WO_TYPE").toString());
            tvEqpid.setText(((HashMap) mData.get(position)).get("EQP_ID").toString());
            tvRepairDate.setText(((HashMap) mData.get(position)).get("PLAN_DT").toString().replace("T", " "));
            tvStatus.setText(((HashMap) mData.get(position)).get("WO_STATUS").toString());

            swUpload.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        ((HashMap) mData.get(position)).put("IS_UPLOAD", "Y");
                    } else {
                        ((HashMap) mData.get(position)).put("IS_UPLOAD", "N");
                    }
                }
            });
            return viewRepair;
        }
    }
}
