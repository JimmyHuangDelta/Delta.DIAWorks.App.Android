package com.delta.android.PMS.Client.Adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.delta.android.PMS.Client.WorkInsActivity;
import com.delta.android.PMS.OffLineData.Data;
import com.delta.android.R;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class FailAdapter extends BaseAdapter {

    private ArrayList mData;//定义数据。
    private LayoutInflater mInflater;//定义Inflater,加载我们自定义的布局。
    private String strWoId;

    public FailAdapter(LayoutInflater inflater, ArrayList data, String woId) {
        mInflater = inflater;
        mData = data;
        strWoId = woId;
    }

    public int getCheckCount() {
        int iCheckCount = 0;
        for (int i = 0; i < mData.size(); i++) {
            if (((HashMap) mData.get(i)).get("CHECKED").toString().contentEquals("Y")) {
                iCheckCount++;
            }
        }

        return iCheckCount;
    }

    public ArrayList getmData() {
        return mData;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        View vFail = mInflater.inflate(R.layout.activity_pms_select_fail_listview, null);
        final HashMap<String, String> fail = (HashMap<String, String>) mData.get(position);
        final Data offData = new Data(mInflater.getContext());

        CheckBox chkFailId = (CheckBox) vFail.findViewById(R.id.chkFail);
        chkFailId.setText(fail.get("FAIL_ID") + "_" + fail.get("FAIL_NAME"));

        if (fail.get("CHECKED").toUpperCase().contentEquals("N")) {
            chkFailId.setChecked(false);
        } else {
            chkFailId.setChecked(true);
        }

        Button btnAddCmt = (Button) vFail.findViewById(R.id.btnAddCmt);
        btnAddCmt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mInflater.getContext());
                View view = mInflater.inflate(R.layout.style_pms_dialog_add_cmt, null);

                final EditText edCmt = (EditText) view.findViewById(R.id.edAddComment);
                edCmt.setText(fail.get("CMT"));

                builder.setView(view);
                builder.setPositiveButton(mInflater.getContext().getResources().getString(R.string.CONFIRM), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fail.put("CMT", edCmt.getText().toString());
                    }
                });

                final AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        chkFailId.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    fail.put("CHECKED", "Y");
                } else {
                    fail.put("CHECKED", "N");
                }
            }
        });

        return vFail;
    }
}
