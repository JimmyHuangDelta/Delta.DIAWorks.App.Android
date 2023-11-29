package com.delta.android.PMS.Client.Adapter;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.PMS.Client.Fragment.WorkPmInfoFragment;
import com.delta.android.PMS.Client.WorkPmActivity;
import com.delta.android.PMS.OffLineData.Data;
import com.delta.android.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static com.delta.android.Core.Common.Global.getContext;

public class CheckItemAdapter extends BaseAdapter {

    private Cursor dtData;//定义数据。
    private LayoutInflater mInflater;//定义Inflater,加载我们自定义的布局。
    ArrayList<HashMap<String, String>> arData = new ArrayList<>();
    private FragmentActivity conText;
    private String woId;

    public CheckItemAdapter(FragmentActivity context, LayoutInflater inflater, Cursor data, String woid) {
        mInflater = inflater;
        conText = context;
        woId = woid;
        if (data.getCount() != 0) {
            dtData = data;
            dtData.moveToFirst();
            arData = new ArrayList<>();
            do {// 逐筆讀出資料
                HashMap<String, String> checkItem = new HashMap<>();
                checkItem.put("MRO_WO_ID", dtData.getString(dtData.getColumnIndex("MRO_WO_ID")));//MRO_WO_ID
                checkItem.put("CHECK_ID", dtData.getString(dtData.getColumnIndex("CHECK_ID")));//CHECK_ID
                checkItem.put("CHECK_NAME", dtData.getString(dtData.getColumnIndex("CHECK_NAME")));//CHECK_NAME
                checkItem.put("CHECK_TYPE", dtData.getString(dtData.getColumnIndex("CHECK_TYPE")));//CHECK_TYPE
                checkItem.put("USL", dtData.getString(dtData.getColumnIndex("USL")));//USL
                checkItem.put("LSL", dtData.getString(dtData.getColumnIndex("LSL")));//LSL
                checkItem.put("TARGET", dtData.getString(dtData.getColumnIndex("TARGET")));//TARGET
                checkItem.put("UOM", dtData.getString(dtData.getColumnIndex("UOM")));//UOM
                checkItem.put("CHECK_VALUE", dtData.getString(dtData.getColumnIndex("CHECK_VALUE")));//CHECK_VALUE
                checkItem.put("CHECK_RESULT", dtData.getString(dtData.getColumnIndex("CHECK_RESULT")));//CHECK_RESULT
                checkItem.put("CHECK_USER_KEY", dtData.getString(dtData.getColumnIndex("CHECK_USER_KEY")));//CHECK_USER_KEY
                checkItem.put("STD_HOUR", dtData.getString(dtData.getColumnIndex("STD_HOUR")));//STD_HOUR
                checkItem.put("CMT", dtData.getString(dtData.getColumnIndex("CMT")));//CMT
                checkItem.put("DESC_TYPE", dtData.getString(dtData.getColumnIndex("DESC_TYPE")));//DESC_TYPE
                arData.add(checkItem);
            } while (dtData.moveToNext());    // 有一下筆就繼續迴圈
        }
    }

    @Override
    public int getCount() {
        return arData.size();
    }

    @Override
    public Object getItem(int position) {
        return dtData.moveToPosition(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final View vCheckItem = mInflater.inflate(R.layout.activity_pms_pm_check_listview, null);

        TextView tvcheckID = (TextView) vCheckItem.findViewById(R.id.tvcheckID);
        TextView tvcheckItem = (TextView) vCheckItem.findViewById(R.id.tvcheckItem);
        final TextView tvcheckResult = (TextView) vCheckItem.findViewById(R.id.tvcheckResult);
        TextView tvcheckType = (TextView) vCheckItem.findViewById(R.id.tvcheckType);
        TextView tvcheckValue = (TextView) vCheckItem.findViewById(R.id.tvcheckValue);
        TextView tvUpperLimit = (TextView) vCheckItem.findViewById(R.id.tvUpperLimit);
        TextView tvTargetValue = (TextView) vCheckItem.findViewById(R.id.tvTargetValue);
        TextView tvLowerLimit = (TextView) vCheckItem.findViewById(R.id.tvLowerLimit);
        TextView tvUnit = (TextView) vCheckItem.findViewById(R.id.tvUnit);
//        TextView tvStandardWorkingHour = (TextView) vCheckItem.findViewById(R.id.tvStandardWorkingHour);
        TextView tvCMT = (TextView) vCheckItem.findViewById(R.id.tvCMT);


        tvcheckID.setText(arData.get(position).get("CHECK_ID"));
        tvcheckItem.setText(arData.get(position).get("CHECK_NAME"));
        tvcheckResult.setText(arData.get(position).get("CHECK_RESULT"));
        tvcheckType.setText(arData.get(position).get("CHECK_TYPE"));
        tvcheckValue.setText(arData.get(position).get("CHECK_VALUE"));
        tvUpperLimit.setText(arData.get(position).get("USL"));
        tvTargetValue.setText(arData.get(position).get("TARGET"));
        tvLowerLimit.setText(arData.get(position).get("LSL"));
        tvUnit.setText(arData.get(position).get("UOM"));
//        tvStandardWorkingHour.setText(arData.get(position).get("STD_HOUR"));
        tvCMT.setText(arData.get(position).get("CMT"));
        if (arData.get(position).get("CHECK_RESULT").equals("NG")) {
            vCheckItem.setBackgroundColor(Color.argb(255, 255, 81, 81));
        }

        //查看保養資訊
        Button btnPMINFO = (Button) vCheckItem.findViewById(R.id.btnPMINFO);
        btnPMINFO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final WorkPmActivity activity = (WorkPmActivity) conText;
                if (!activity.GetIsWork()) {
                    ((WorkPmActivity) conText).ShowMessage(conText.getResources().getString(R.string.EAPE105020));
                    return;
                }
                WorkPmInfoFragment newFragment = WorkPmInfoFragment.newInstance();
                Bundle args = new Bundle();
                args.putString("WO_ID", woId);
                newFragment.setArguments(args);
                newFragment.show(conText.getSupportFragmentManager(), "dialog");
            }
        });

        vCheckItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final WorkPmActivity activity = (WorkPmActivity) conText;
                if (!activity.GetIsWork()) {
                    ((WorkPmActivity) conText).ShowMessage(conText.getResources().getString(R.string.EAPE105020));
                    return;
                }
                final View textEntryView = mInflater.inflate(R.layout.activity_pms_pm_check_work, null);

                //View資料
                final TextView tvcheckID_S = (TextView) v.findViewById(R.id.tvcheckID);
                final TextView tvcheckItem_S = (TextView) v.findViewById(R.id.tvcheckItem);//TODO delete
                final TextView tvcheckType_S = (TextView) v.findViewById(R.id.tvcheckType);
                TextView tvcheckValue_S = (TextView) v.findViewById(R.id.tvcheckValue);
                final TextView tvUpperLimit_S = (TextView) v.findViewById(R.id.tvUpperLimit);
                final TextView tvTargetValue_S = (TextView) v.findViewById(R.id.tvTargetValue);
                final TextView tvLowerLimit_S = (TextView) v.findViewById(R.id.tvLowerLimit);
                TextView tvUnit_S = (TextView) v.findViewById(R.id.tvUnit);
                TextView tvCMT_S = (TextView) v.findViewById(R.id.tvCMT);

                //Dialog資料
                final TextView tvcheckItem = (TextView) textEntryView.findViewById(R.id.tvcheckItem);//TODO delete
                TextView tvcheckType = (TextView) textEntryView.findViewById(R.id.tvcheckType);
                final EditText edcheckValue = (EditText) textEntryView.findViewById(R.id.edcheckValue);
                final RadioGroup radioCheck = (RadioGroup) textEntryView.findViewById(R.id.radioCheck);
                RadioButton rdbTrue = (RadioButton) textEntryView.findViewById(R.id.rdbTrue);
                RadioButton rdbFalse = (RadioButton) textEntryView.findViewById(R.id.rdbFalse);
                TextView tvUnit = (TextView) textEntryView.findViewById(R.id.tvUnit);
                final EditText edcmt = (EditText) textEntryView.findViewById(R.id.edCMT);
                final Spinner spDescType = (Spinner) textEntryView.findViewById(R.id.spDescType);
                SimpleAdapter spAdapter;

                tvcheckItem.setText(tvcheckItem_S.getText());
                tvcheckType.setText(tvcheckType_S.getText());
                edcheckValue.setText(tvcheckValue_S.getText());
                edcmt.setText(tvCMT_S.getText());
                if (tvcheckValue_S.getText().toString().equals("True")) {
                    rdbTrue.setChecked(true);
                } else {
                    rdbFalse.setChecked(true);
                }
                tvUnit.setText(tvUnit_S.getText());

                switch (tvcheckType_S.getText().toString()) {
                    case "Standard":
                        edcheckValue.setVisibility(View.GONE);
                        edcheckValue.setInputType(InputType.TYPE_CLASS_TEXT);
                        spDescType.setVisibility(View.GONE);
                        break;
                    case "Variable":
                        radioCheck.setVisibility(View.GONE);
                        edcheckValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                        spDescType.setVisibility(View.GONE);
                        break;
                    case "Description":
                        radioCheck.setVisibility(View.GONE);
                        edcheckValue.setVisibility(View.GONE);

                        ArrayList arDescData = new ArrayList();
                        String[] type = arData.get(position).get("DESC_TYPE").split(",");
                        for (int i = 0; i < type.length; i++) {
                            HashMap<String, String> desc = new HashMap();
                            desc.put("TYPE", type[i]);
                            arDescData.add(desc);
                        }

                        spAdapter = new SimpleAdapter(mInflater.getContext(), arDescData, android.R.layout.simple_list_item_1, new String[]{"TYPE"}, new int[]{android.R.id.text1});
                        spDescType.setAdapter(spAdapter);
                        break;
                }

                final AlertDialog dialog = new AlertDialog.Builder(conText)
                        .setTitle(R.string.CHECK_ITEM_WORK)
                        .setView(textEntryView)
                        .setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(R.string.CONFIRM, null)
                        .setCancelable(true)
                        .create();
                dialog.show();
                //為了避免點選 positive 按鈕後直接關閉 dialog,把點選事件拿出來設定
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View dialogView) {

                                String checkResult = "NG";
                                String CHECK_VALUE = "*";

                                switch (tvcheckType_S.getText().toString()) {
                                    case "Standard":
                                        int selectedId = radioCheck.getCheckedRadioButtonId();
                                        RadioButton radioButton = textEntryView.findViewById(selectedId);
                                        CHECK_VALUE = radioButton.getText().toString();
                                        if (CHECK_VALUE.equals("True")) {
                                            checkResult = "OK";
                                        }
                                        break;
                                    case "Variable":
                                        CHECK_VALUE = edcheckValue.getText().toString();
                                        if (edcheckValue.getText().toString().contentEquals("")) {
                                            Toast.makeText(getContext(), conText.getResources().getString(R.string.EAPE105001, tvcheckItem.getText()), Toast.LENGTH_LONG).show();
                                            return;
                                        }

                                        try {
                                            Float.parseFloat(edcheckValue.getText().toString());
                                            Float.parseFloat(tvUpperLimit_S.getText().toString());
                                            Float.parseFloat(tvLowerLimit_S.getText().toString());
                                            if (Float.parseFloat(edcheckValue.getText().toString()) <= Float.parseFloat(tvUpperLimit_S.getText().toString()) &&
                                                    Float.parseFloat(edcheckValue.getText().toString()) >= Float.parseFloat(tvLowerLimit_S.getText().toString())) {
                                                checkResult = "OK";
                                            }
                                        } catch (NumberFormatException e) {
                                            Toast.makeText(getContext(), conText.getResources().getString(R.string.EAPE105022), Toast.LENGTH_LONG).show();
                                            return;
                                        }

                                        break;
                                    case "Description":
                                        CHECK_VALUE = ((HashMap) spDescType.getSelectedItem()).get("TYPE").toString();
                                        if (CHECK_VALUE.equals(tvTargetValue_S.getText().toString())) {
                                            checkResult = "OK";
                                        }
                                        break;
                                }

                                if (checkResult.equals("NG")) {
                                    if (edcmt.getText().toString().contentEquals("")) {
                                        Toast.makeText(getContext(), conText.getResources().getString(R.string.EAPE105021), Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                }

                                Data offData = new Data(getContext());
                                SQLiteDatabase dbUpdate = offData.getWritableDatabase();
                                ContentValues cUpdate = new ContentValues();
                                cUpdate.put("CHECK_VALUE", CHECK_VALUE);
                                cUpdate.put("CHECK_RESULT", checkResult);
                                cUpdate.put("CHECK_USER_KEY", getContext().getUserKey()); //紀錄檢測人員。2019/12/17 新增。
                                cUpdate.put("CMT", edcmt.getText().toString());
                                dbUpdate.update("SEMS_PM_WO_CHECK", cUpdate, "MRO_WO_ID = '" + woId + "' AND CHECK_ID = '" + tvcheckID_S.getText().toString() + "'", null);

                                //update list
                                TextView tvcheckValue_list = (TextView) v.findViewById(R.id.tvcheckValue);
                                TextView tvcheckResult_list = (TextView) v.findViewById(R.id.tvcheckResult);
                                TextView tvCMT_list = (TextView) v.findViewById(R.id.tvCMT);
                                tvcheckValue_list.setText(CHECK_VALUE);
                                tvcheckResult_list.setText(checkResult);
                                tvCMT_list.setText(edcmt.getText().toString());
                                if (checkResult.equals("NG")) {
                                    vCheckItem.setBackgroundColor(Color.argb(255, 255, 81, 81));
                                } else {
                                    vCheckItem.setBackgroundColor(Color.TRANSPARENT);
                                }

                                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {

                                        InputMethodManager inputMgr = (InputMethodManager) mInflater.getContext()
                                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                                        inputMgr.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
                                    }
                                });

                                dialog.dismiss();
                            }
                        });
            }
        });

        return vCheckItem;
    }
}
