package com.delta.android.PMS.Client.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.datatype.DatatypeConfigurationException;

public class WoDownloadAdapter extends BaseAdapter {

    private DataTable dtData;//定义数据。
    private LayoutInflater mInflater;//定义Inflater,加载我们自定义的布局。
    ArrayList<HashMap<String, String>> arData = new ArrayList<>();

    public WoDownloadAdapter(LayoutInflater inflater, DataTable data) {
        mInflater = inflater;
        dtData = data;

        for (int i = 0; i < dtData.Rows.size(); i++) {
            HashMap<String, String> woIsDownload = new HashMap<>();
            woIsDownload.put("WO_ID", dtData.Rows.get(i).get("MRO_WO_ID").toString());
            woIsDownload.put("EQP_ID", dtData.Rows.get(i).get("EQP_ID").toString());
            woIsDownload.put("PM_ID", dtData.Rows.get(i).get("PM_ID").toString());
            woIsDownload.put("MRO_WO_TYPE", dtData.Rows.get(i).get("MRO_WO_TYPE").toString());
            woIsDownload.put("CALL_FIX_TYPE_ID", dtData.Rows.get(i).get("CALL_FIX_TYPE_ID").toString());
            woIsDownload.put("IS_DOWNLOAD", dtData.Rows.get(i).get("IS_DOWNLOAD").toString());
            woIsDownload.put("IS_EXIST", dtData.Rows.get(i).get("IS_EXIST").toString());
            woIsDownload.put("PLAN_DATE", dtData.Rows.get(i).get("PLAN_DATE").toString());
            arData.add(woIsDownload);
        }
    }

    public ArrayList<String> getDownloadData() {
        ArrayList<String> arDownloadWo = new ArrayList<>();
        for (int i = 0; i < arData.size(); i++) {
            HashMap<String, String> data = arData.get(i);
            if (data.get("IS_DOWNLOAD").toUpperCase().contentEquals("Y")) {
                arDownloadWo.add(data.get("WO_ID"));
            }
        }

        return arDownloadWo;
    }

    public ArrayList<String> getUnDownloadData() {
        ArrayList<String> arUnDownloadWo = new ArrayList<>();
        for (int i = 0; i < arData.size(); i++) {
            HashMap<String, String> data = arData.get(i);
            if (data.get("IS_DOWNLOAD").toUpperCase().contentEquals("N")) {
                arUnDownloadWo.add(data.get("WO_ID"));
            }
        }

        return arUnDownloadWo;
    }

    @Override
    public int getCount() {
        return dtData.Rows.size();
    }

    @Override
    public Object getItem(int position) {
        return arData.get(position).get("WO_ID").toString();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vDownload = null;
        if (arData.get(position).get("MRO_WO_TYPE").toUpperCase().contentEquals("REPAIR")) {
            vDownload = mInflater.inflate(R.layout.activity_pms_download_repair_listview, null);
            vDownload.setLongClickable(true);

            TextView tvIsExist = (TextView) vDownload.findViewById(R.id.tvDownloadIsExist);
            TextView tvWoId = (TextView) vDownload.findViewById(R.id.tvLsWoId);
            TextView tvEqp = (TextView) vDownload.findViewById(R.id.tvLsEqp);
            TextView tvPm = (TextView) vDownload.findViewById(R.id.tvLsPm);
            TextView tvItem = (TextView) vDownload.findViewById(R.id.textView10);
            TextView tvFailDt = (TextView) vDownload.findViewById(R.id.tvLsRepairDt);
            Switch swIsDownload = (Switch) vDownload.findViewById(R.id.swExist);

            tvEqp.setText(arData.get(position).get("EQP_ID"));
            tvPm.setText(arData.get(position).get("CALL_FIX_TYPE_ID"));
            tvWoId.setText(arData.get(position).get("WO_ID"));
            tvFailDt.setText(arData.get(position).get("PLAN_DATE").replace("T", " "));

            final HashMap<String, String> data = arData.get(position);

            swIsDownload.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        data.put("IS_DOWNLOAD", "Y");
                    } else {
                        data.put("IS_DOWNLOAD", "N");
                    }
                }
            });

            if (arData.get(position).get("IS_DOWNLOAD").toUpperCase().contentEquals("Y")) {
                swIsDownload.setChecked(true);
            } else {
                swIsDownload.setChecked(false);
            }

            if (arData.get(position).get("IS_EXIST").toUpperCase().contentEquals("N")) {
                tvIsExist.setText(mInflater.getContext().getResources().getText(R.string.NON_EXIST)); //如果存在
            } else {
                tvIsExist.setText(mInflater.getContext().getResources().getText(R.string.EXIST));
            }
        } else {
            vDownload = mInflater.inflate(R.layout.activity_pms_download_wo_listview, null);
            vDownload.setLongClickable(true);

            TextView tvIsExist = (TextView) vDownload.findViewById(R.id.tvDownloadIsExist);
            TextView tvWoId = (TextView) vDownload.findViewById(R.id.tvLsWoId);
            TextView tvEqp = (TextView) vDownload.findViewById(R.id.tvLsEqp);
            TextView tvPm = (TextView) vDownload.findViewById(R.id.tvLsPm);
            TextView tvItem = (TextView) vDownload.findViewById(R.id.textView10);
            TextView tvPlanDate = (TextView) vDownload.findViewById(R.id.tvLsPmPlanDt);
            Switch swIsDownload = (Switch) vDownload.findViewById(R.id.swExist);

            tvEqp.setText(arData.get(position).get("EQP_ID"));
            tvWoId.setText(arData.get(position).get("WO_ID"));
            tvPm.setText(arData.get(position).get("PM_ID"));
            tvPlanDate.setText(arData.get(position).get("PLAN_DATE").replace("T", " "));

            final HashMap<String, String> data = arData.get(position);

            swIsDownload.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        data.put("IS_DOWNLOAD", "Y");
                    } else {
                        data.put("IS_DOWNLOAD", "N");
                    }
                }
            });

            if (arData.get(position).get("IS_DOWNLOAD").toUpperCase().contentEquals("Y")) {
                swIsDownload.setChecked(true);
            } else {
                swIsDownload.setChecked(false);
            }

            if (arData.get(position).get("IS_EXIST").toUpperCase().contentEquals("N")) {
                tvIsExist.setText(mInflater.getContext().getResources().getText(R.string.NON_EXIST)); //如果存在
            } else {
                tvIsExist.setText(mInflater.getContext().getResources().getText(R.string.EXIST));
            }
        }
        return vDownload;
    }
}
