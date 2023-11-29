package com.delta.android.PMS.Client;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseActivity;
import com.delta.android.Core.Common.Global;
import com.delta.android.Core.DataTable.DataColumn;
import com.delta.android.Core.DataTable.DataColumnCollection;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.PMS.Client.Adapter.WoDownloadAdapter;
import com.delta.android.PMS.Common.PreventButtonMultiClick;
import com.delta.android.PMS.OffLineData.Data;
import com.delta.android.PMS.Param.BIGetEqpAndroidParam;
import com.delta.android.PMS.Param.BIGetWoAndroidParam;
import com.delta.android.PMS.Param.BWoUploadAndroidParam;
import com.delta.android.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

public class DownloadWoActivity extends BaseActivity {

    DataTable dtEqp;
    DataTable dtPm;
    String downloadType;
    ArrayList<String> arExistWo = new ArrayList<>(); //本機判斷是否存在用
    StringBuilder sbExistWo = new StringBuilder();
    //    String[] existWos; //工單上傳用
    Spinner spEqp;
    Spinner spPm;
    EditText edStartDate;
    EditText edEndDate;
    ListView lsWo;
    WoDownloadAdapter adapter;
    Data offData;
    ProgressDialog progressDialog;

    //保養(點檢) .維修共用table
    DataTable dtWo;
    DataTable dtWH;
    DataTable dtEqpPart;
    DataTable dtFile;
    DataTable dtPartTrx;

    //保養(點檢)用table
    DataTable dtCheckMethod;
    DataTable dtCheckConsume;
    DataTable dtCheckTool;
    DataTable dtSop;
    DataTable dtcheck;

    //維修用table
    DataTable dtFail;
    DataTable dtFailRsn;
    DataTable dtFailSty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pms_download_wo);

        Bundle bundle = getIntent().getExtras();
        downloadType = bundle.getString("downloadType"); //要下載的工單類型

        switch (downloadType.toUpperCase()){
            case "PM":
                getSupportActionBar().setTitle(getSupportActionBar().getTitle() + "_" + getResources().getString(R.string.PM_WO));
                break;

            case "REPAIR":
                getSupportActionBar().setTitle(getSupportActionBar().getTitle() + "_" + getResources().getString(R.string.REPAIR_WO));
                break;

            case "CHECK":
                getSupportActionBar().setTitle(getSupportActionBar().getTitle() + "_" + getResources().getString(R.string.INSPECTION_WO));
                break;
        }

        TextView tvEqp = (TextView) findViewById(R.id.tvDownloadEqp);
        TextView tvPm = (TextView) findViewById(R.id.tvDownloadPm);

        ImageButton btnQuery = (ImageButton) findViewById(R.id.btnQueryDownloadWo);
        btnQuery.setImageResource(R.mipmap.query_wo);
        Button btnDownload = (Button) findViewById(R.id.btnDownloadWo);

        spEqp = (Spinner) findViewById(R.id.spDownloadEqp);
        spPm = (Spinner) findViewById(R.id.spDownloadPm);

        edStartDate = (EditText) findViewById(R.id.edDownloadStartDate);
        setEditTextReadOnly(edStartDate);
        edEndDate = (EditText) findViewById(R.id.edDownloadEndDate);
        setEditTextReadOnly(edEndDate);

        lsWo = (ListView) findViewById(R.id.lvQueryWo);
        lsWo.setEnabled(true);
        lsWo.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return true;
            }
        });

        if (downloadType.toUpperCase().contentEquals("REPAIR")) { //如果是維修單下載，不顯示保養內容選擇的原件。
            tvPm.setVisibility(View.INVISIBLE);
            spPm.setVisibility(View.INVISIBLE);
        }

        offData = new Data(DownloadWoActivity.this);

        btnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strEqpKey = ((HashMap) spEqp.getSelectedItem()).get("EQP_KEY").toString();
                String strPmKey = ((HashMap) spPm.getSelectedItem()).get("PM_KEY").toString();
                String strStartDate = edStartDate.getText().toString();
                String strEndDate = edEndDate.getText().toString();

                if (downloadType.toUpperCase().contentEquals("REPAIR")) {
                    BindRepairData(strEqpKey, strStartDate, strEndDate);
                } else if (downloadType.toUpperCase().contentEquals("PM")) {
                    BindPmData(strEqpKey, strPmKey, strStartDate, strEndDate);
                } else {
                    BindCheckData(strEqpKey, strPmKey, strStartDate, strEndDate);
                }
            }
        });

        Button btnDownloadAll = (Button) findViewById(R.id.btnQueryWoDownloadAll);
        btnDownloadAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    for (int i = 0; i < dtWo.Rows.size(); i++) {
                        DataColumn dc = new DataColumn("IS_DOWNLOAD");
                        dtWo.Rows.get(i).put("IS_DOWNLOAD", "Y");
                    }

                    adapter = new WoDownloadAdapter(getLayoutInflater(), dtWo);
                    lsWo.setAdapter(adapter);
                } catch (Exception ex) {
                    ex.getMessage();
                }
            }
        });

        Button btnUnDownloadAll = (Button) findViewById(R.id.btnQueryWoUnDownloadAll);
        btnUnDownloadAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    for (int i = 0; i < dtWo.Rows.size(); i++) {
                        DataColumn dc = new DataColumn("IS_DOWNLOAD");
                        dtWo.Rows.get(i).put("IS_DOWNLOAD", "N");
                    }

                    adapter = new WoDownloadAdapter(getLayoutInflater(), dtWo);
                    lsWo.setAdapter(adapter);
                } catch (Exception ex) {
                    ex.getMessage();
                }
            }
        });

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(DownloadWoActivity.this);
                try {
                    if (PreventButtonMultiClick.isFastDoubleClick()) {
                        // 进行点击事件后的逻辑操作
                        Toast.makeText(DownloadWoActivity.this, getResources().getString(R.string.NOT_ALLOW_MUTI_CLICK), Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        if (adapter != null && adapter.getDownloadData().size() > 0) {

                            //將資料insert到保養單相關table
                            if (dtWo.Rows.size() > 0) {
                                progressDialog.setMessage(getResources().getString(R.string.DATA_DOWNLOAD_INPR));
                                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                progressDialog.setCancelable(false);
                                progressDialog.show();

                                DownloadWo(downloadType);
                            }
                        }
                    }
                } catch (Exception ex) {

                } finally {
                    progressDialog.dismiss();
                }
            }
        });
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                progressDialog.dismiss();
                ShowMessage(getResources().getString(R.string.DOWNLOAD_SUCCESS));

                String strEqpKey = ((HashMap) spEqp.getSelectedItem()).get("EQP_KEY").toString();
                String strPmKey = ((HashMap) spPm.getSelectedItem()).get("PM_KEY").toString();
                String strStartDate = edStartDate.getText().toString();
                String strEndDate = edEndDate.getText().toString();
                if (downloadType.toUpperCase().contentEquals("REPAIR")) {
                    BindRepairData(strEqpKey, strStartDate, strEndDate);
                } else if (downloadType.toUpperCase().contentEquals("PM")) {
                    BindPmData(strEqpKey, strPmKey, strStartDate, strEndDate);
                } else {
                    BindCheckData(strEqpKey, strPmKey, strStartDate, strEndDate);
                }
            }
        }
    };

    private void DownloadWo(String downloadType) {
        SQLiteDatabase dbWrite = offData.getWritableDatabase();

        try {
            dbWrite.beginTransaction();
            ArrayList<String> arUnDownload = adapter.getUnDownloadData(); //取得不下載的工單

            //取得目前工單資料
            SQLiteDatabase dbQuery = offData.getReadableDatabase();
            Cursor cursor;

            if (downloadType.toUpperCase().contentEquals("PM")) {
                cursor = dbQuery.query("SEMS_MRO_WO", null, "MRO_WO_TYPE = ?", new String[]{"PM"}, null, null, null);
            } else if (downloadType.toUpperCase().contentEquals("REPAIR")) {
                cursor = dbQuery.query("SEMS_MRO_WO", null, "MRO_WO_TYPE = ?", new String[]{"Repair"}, null, null, null);
            } else {
                cursor = dbQuery.query("SEMS_MRO_WO", null, "MRO_WO_TYPE = ?", new String[]{"Check"}, null, null, null);
            }
            cursor.moveToFirst();

            sbExistWo = new StringBuilder();
            for (int i = 0; i < cursor.getCount(); i++) {
                arExistWo.add(cursor.getString(cursor.getColumnIndex("MRO_WO_ID")));
                sbExistWo.append("," + cursor.getString(cursor.getColumnIndex("MRO_WO_ID")));
                cursor.moveToNext();
            }

            for (int i = 0; i < dtWo.Rows.size(); i++) {
                //如果工單存在不下載的工單清單內，直接跳下一筆
                if (arUnDownload.contains(dtWo.Rows.get(i).get("MRO_WO_ID").toString()))
                    continue;

                //檢查要下載的工單是否存在，如果存在相關數據要先砍掉
                if (arExistWo.contains(dtWo.Rows.get(i).get("MRO_WO_ID").toString())) {
                    DeleteWo(downloadType, dtWo.Rows.get(i).get("MRO_WO_ID").toString());
                }

                //新增工單資訊到SQLITE內
                ContentValues woValues = new ContentValues();
                woValues.put("MRO_WO_SERIAL_KEY", Double.parseDouble(dtWo.Rows.get(i).get("MRO_WO_SERIAL_KEY").toString()));
                woValues.put("MRO_WO_ID", dtWo.Rows.get(i).get("MRO_WO_ID").toString());
                woValues.put("MRO_WO_TYPE", dtWo.Rows.get(i).get("MRO_WO_TYPE").toString());
                woValues.put("WO_STATUS", dtWo.Rows.get(i).get("WO_STATUS").toString());
                woValues.put("EQP_ID", dtWo.Rows.get(i).get("EQP_ID").toString());
                woValues.put("EQP_NAME", dtWo.Rows.get(i).get("EQP_NAME").toString());
                woValues.put("PLAN_DT", dtWo.Rows.get(i).get("PLAN_DATE").toString().split("T")[0]);
                woValues.put("START_DT", dtWo.Rows.get(i).get("START_DT").toString());
                woValues.put("END_DT", dtWo.Rows.get(i).get("END_DT").toString());
                woValues.put("FAIL_END_DT", dtWo.Rows.get(i).get("FAIL_END_DT").toString());
                woValues.put("PM_ID", dtWo.Rows.get(i).get("PM_ID").toString());
                woValues.put("PM_NAME", dtWo.Rows.get(i).get("PM_NAME").toString());
                woValues.put("CALL_FIX_TYPE_ID", dtWo.Rows.get(i).get("CALL_FIX_TYPE_ID").toString());
                woValues.put("CALL_FIX_TYPE_NAME", dtWo.Rows.get(i).get("CALL_FIX_TYPE_NAME").toString());
                woValues.put("CMT", dtWo.Rows.get(i).get("CMT").toString());
                woValues.put("TTL_MAN_HOUR", Double.parseDouble(dtWo.Rows.get(i).get("TTL_MAN_HOUR").toString()));
                woValues.put("EXC_MAN_HOUR", Double.parseDouble(dtWo.Rows.get(i).get("EXC_MAN_HOUR").toString()));
                dbWrite.insertOrThrow("SEMS_MRO_WO", null, woValues);

                //紀錄工單與人員對應資訊到sqlite
                ContentValues woUserValues = new ContentValues();
                woUserValues.put("USER_ID", DownloadWoActivity.this.getGlobal().getUserID());
                woUserValues.put("MRO_WO_ID", dtWo.Rows.get(i).get("MRO_WO_ID").toString());
                woUserValues.put("MRO_WO_TYPE", dtWo.Rows.get(i).get("MRO_WO_TYPE").toString());
                dbWrite.insertOrThrow("USER_WO", null, woUserValues);
            }

            for (int i = 0; i < dtWH.Rows.size(); i++) {
                //如果工單存在不下載的工單清單內，直接跳下一筆
                if (arUnDownload.contains(dtWH.Rows.get(i).get("MRO_WO_ID").toString()))
                    continue;

                ContentValues userValues = new ContentValues();
                userValues.put("MRO_WO_ID", dtWH.Rows.get(i).get("MRO_WO_ID").toString());
                userValues.put("USER_ID", dtWH.Rows.get(i).get("USER_ID").toString());
                userValues.put("USER_NAME", dtWH.Rows.get(i).get("USER_NAME").toString());
                userValues.put("START_DT", dtWH.Rows.get(i).get("START_DT").toString());
                userValues.put("END_DT", dtWH.Rows.get(i).get("END_DT").toString());
                userValues.put("CMT", dtWH.Rows.get(i).get("CMT").toString());
                dbWrite.insertOrThrow("SEMS_MRO_WO_WH", null, userValues);
            }

            for (int i = 0; i < dtEqpPart.Rows.size(); i++) {
                //如果已經有相同機台、零件、零件批號的資料，要先刪除原有資料
                offData.getWritableDatabase().delete("SEMS_EQP_PART", "EQP_ID = ? AND PART_ID = ? AND PART_LOT_ID = ?"
                        , new String[]{dtEqpPart.Rows.get(i).get("EQP_ID").toString(), dtEqpPart.Rows.get(i).get("ITEM_ID").toString(), dtEqpPart.Rows.get(i).get("PART_LOT_ID").toString()});

                ContentValues eqpPartValues = new ContentValues();
                eqpPartValues.put("EQP_ID", dtEqpPart.Rows.get(i).get("EQP_ID").toString());
                eqpPartValues.put("PART_ID", dtEqpPart.Rows.get(i).get("ITEM_ID").toString());
                eqpPartValues.put("PART_LOT_ID", dtEqpPart.Rows.get(i).get("PART_LOT_ID").toString());
                eqpPartValues.put("PART_QTY", dtEqpPart.Rows.get(i).get("PART_QTY").toString());
                eqpPartValues.put("CMT", dtEqpPart.Rows.get(i).get("CMT").toString());
                dbWrite.insertOrThrow("SEMS_EQP_PART", null, eqpPartValues);
            }

            for (int i = 0; i < dtPartTrx.Rows.size(); i++) {
                if (arUnDownload.contains(dtPartTrx.Rows.get(i).get("MRO_WO_ID").toString()))
                    continue;

                ContentValues partTrxValues = new ContentValues();
                partTrxValues.put("TRX_DATE", dtPartTrx.Rows.get(i).get("TRX_DATE").toString());
                partTrxValues.put("MRO_WO_ID", dtPartTrx.Rows.get(i).get("MRO_WO_ID").toString());
                partTrxValues.put("PART_ID", dtPartTrx.Rows.get(i).get("PART_ID").toString());
                partTrxValues.put("PART_LOT_ID", dtPartTrx.Rows.get(i).get("PART_LOT_ID").toString());
                partTrxValues.put("PART_QTY", dtPartTrx.Rows.get(i).get("PART_QTY").toString());
                partTrxValues.put("STORAGE_ID", dtPartTrx.Rows.get(i).get("CURR_LOCATION_TYPE").toString());
                partTrxValues.put("BIN_ID", "");
                partTrxValues.put("CMT", dtPartTrx.Rows.get(i).get("CMT").toString());
                partTrxValues.put("TRX_MODE", dtPartTrx.Rows.get(i).get("TRX_MODE").toString());
                dbWrite.insertOrThrow("SEMS_MRO_PART_TRX", null, partTrxValues);
            }

            for (int i = 0; i < dtFile.Rows.size(); i++) {
                if (arUnDownload.contains(dtFile.Rows.get(i).get("MRO_WO_ID").toString()))
                    continue;

                ContentValues fileValues = new ContentValues();
                fileValues.put("MRO_WO_ID", dtFile.Rows.get(i).get("MRO_WO_ID").toString());
                fileValues.put("FILE_NAME", dtFile.Rows.get(i).get("FILE_NAME").toString());
                fileValues.put("FILE_DESC", dtFile.Rows.get(i).get("FILE_DESC").toString());
                fileValues.put("UPLOAD_USER_ID", dtFile.Rows.get(i).get("USER_ID").toString());
                fileValues.put("UPLOAD_DATE", dtFile.Rows.get(i).get("TRX_DATE").toString());
                dbWrite.insertOrThrow("SEMS_FILE", null, fileValues);

            }

            if (downloadType.toUpperCase().contentEquals("REPAIR")) {
                for (int i = 0; i < dtFail.Rows.size(); i++) {
                    if (arUnDownload.contains(dtFail.Rows.get(i).get("MRO_WO_ID").toString()))
                        continue;

                    ContentValues failValues = new ContentValues();
                    failValues.put("MRO_WO_ID", dtFail.Rows.get(i).get("MRO_WO_ID").toString());
                    failValues.put("FAIL_ID", dtFail.Rows.get(i).get("FAIL_ID").toString());
                    failValues.put("FAIL_NAME", dtFail.Rows.get(i).get("FAIL_NAME").toString());
                    failValues.put("FAIL_CMT", dtFail.Rows.get(i).get("CMT").toString());
                    dbWrite.insertOrThrow("SEMS_REPAIR_FAIL", null, failValues);
                }

                for (int i = 0; i < dtFailSty.Rows.size(); i++) {
                    if (arUnDownload.contains(dtFailSty.Rows.get(i).get("MRO_WO_ID").toString()))
                        continue;

                    ContentValues failStyValues = new ContentValues();
                    failStyValues.put("MRO_WO_ID", dtFailSty.Rows.get(i).get("MRO_WO_ID").toString());
                    failStyValues.put("FAIL_STRATEGY_ID", dtFailSty.Rows.get(i).get("FAIL_STRATEGY_ID").toString());
                    failStyValues.put("FAIL_STRATEGY_NAME", dtFailSty.Rows.get(i).get("FAIL_STRATEGY_NAME").toString());
                    failStyValues.put("FAIL_STRATEGY_CMT", dtFailSty.Rows.get(i).get("CMT").toString());
                    dbWrite.insertOrThrow("SEMS_REPAIR_WO_STY", null, failStyValues);
                }

                for (int i = 0; i < dtFailRsn.Rows.size(); i++) {
                    if (arUnDownload.contains(dtFailRsn.Rows.get(i).get("MRO_WO_ID").toString()))
                        continue;

                    ContentValues failRsnValues = new ContentValues();
                    failRsnValues.put("MRO_WO_ID", dtFailRsn.Rows.get(i).get("MRO_WO_ID").toString());
                    failRsnValues.put("FAIL_REASON_ID", dtFailRsn.Rows.get(i).get("FAIL_REASON_ID").toString());
                    failRsnValues.put("FAIL_REASON_NAME", dtFailRsn.Rows.get(i).get("FAIL_REASON_NAME").toString());
                    failRsnValues.put("FAIL_REASON_CMT", dtFailRsn.Rows.get(i).get("CMT").toString());
                    dbWrite.insertOrThrow("SEMS_REPAIR_WO_RSN", null, failRsnValues);
                }
            } else {
                for (int i = 0; i < dtCheckMethod.Rows.size(); i++) {
                    if (arUnDownload.contains(dtCheckMethod.Rows.get(i).get("MRO_WO_ID").toString()))
                        continue;

                    ContentValues methodValues = new ContentValues();
                    methodValues.put("PM_METHOD_ID", dtCheckMethod.Rows.get(i).get("PM_METHOD_ID").toString());
                    methodValues.put("PM_METHOD_NAME", dtCheckMethod.Rows.get(i).get("PM_METHOD_NAME").toString());
                    methodValues.put("CHECK_ID", dtCheckMethod.Rows.get(i).get("CHECK_ID").toString());
                    methodValues.put("CHECK_NAME", dtCheckMethod.Rows.get(i).get("CHECK_NAME").toString());
                    methodValues.put("MRO_WO_ID", dtCheckMethod.Rows.get(i).get("MRO_WO_ID").toString());
                    dbWrite.insertOrThrow("SBRM_EMS_CHECK_METHOD", null, methodValues);
                }

                for (int i = 0; i < dtCheckConsume.Rows.size(); i++) {
                    if (arUnDownload.contains(dtCheckConsume.Rows.get(i).get("MRO_WO_ID").toString()))
                        continue;

                    ContentValues consumeValues = new ContentValues();
                    consumeValues.put("CONSUMABLE_LIST_ID", dtCheckConsume.Rows.get(i).get("CONSUMABLE_LIST_ID").toString());
                    consumeValues.put("CONSUMABLE_LIST_NAME", dtCheckConsume.Rows.get(i).get("CONSUMABLE_LIST_NAME").toString());
                    consumeValues.put("CONSUMABLE_TYPE_ID", dtCheckConsume.Rows.get(i).get("CONSUMABLE_TYPE_ID").toString());
                    consumeValues.put("CONSUMABLE_TYPE_NAME", dtCheckConsume.Rows.get(i).get("CONSUMABLE_TYPE_NAME").toString());
                    consumeValues.put("CHECK_ID", dtCheckConsume.Rows.get(i).get("CHECK_ID").toString());
                    consumeValues.put("CHECK_NAME", dtCheckConsume.Rows.get(i).get("CHECK_NAME").toString());
                    consumeValues.put("MRO_WO_ID", dtCheckConsume.Rows.get(i).get("MRO_WO_ID").toString());
                    dbWrite.insertOrThrow("SBRM_EMS_CHECK_CONSUMABLE", null, consumeValues);
                }

                for (int i = 0; i < dtCheckTool.Rows.size(); i++) {
                    if (arUnDownload.contains(dtCheckTool.Rows.get(i).get("MRO_WO_ID").toString()))
                        continue;

                    ContentValues toolValues = new ContentValues();
                    toolValues.put("FIX_TOOL_ID", dtCheckTool.Rows.get(i).get("FIX_TOOL_ID").toString());
                    toolValues.put("FIX_TOOL_NAME", dtCheckTool.Rows.get(i).get("FIX_TOOL_NAME").toString());
                    toolValues.put("CHECK_ID", dtCheckTool.Rows.get(i).get("CHECK_ID").toString());
                    toolValues.put("CHECK_NAME", dtCheckTool.Rows.get(i).get("CHECK_NAME").toString());
                    toolValues.put("MRO_WO_ID", dtCheckTool.Rows.get(i).get("MRO_WO_ID").toString());
                    dbWrite.insertOrThrow("SBRM_EMS_CHECK_FIX_TOOL", null, toolValues);
                }

                for (int i = 0; i < dtSop.Rows.size(); i++) {
                    if (arUnDownload.contains(dtSop.Rows.get(i).get("MRO_WO_ID").toString()))
                        continue;

                    ContentValues sopValues = new ContentValues();
                    sopValues.put("SOP_ID", dtSop.Rows.get(i).get("SOP_ID").toString());
                    sopValues.put("SOP_NAME", dtSop.Rows.get(i).get("SOP_NAME").toString());
                    sopValues.put("DOC_NO", dtSop.Rows.get(i).get("DOC_NO").toString());
                    sopValues.put("URL", dtSop.Rows.get(i).get("URL").toString());
                    sopValues.put("FULL_FILE_NAME", dtSop.Rows.get(i).get("FULL_FILE_NAME").toString());
                    sopValues.put("MRO_WO_ID", dtSop.Rows.get(i).get("MRO_WO_ID").toString());
                    dbWrite.insertOrThrow("SBRM_EMS_PM_SOP", null, sopValues);
                }

                for (int i = 0; i < dtcheck.Rows.size(); i++) {
                    if (arUnDownload.contains(dtcheck.Rows.get(i).get("MRO_WO_ID").toString()))
                        continue;

                    ContentValues checkValues = new ContentValues();
                    checkValues.put("MRO_WO_ID", dtcheck.Rows.get(i).get("MRO_WO_ID").toString());
                    checkValues.put("CHECK_ID", dtcheck.Rows.get(i).get("CHECK_ID").toString());
                    checkValues.put("CHECK_NAME", dtcheck.Rows.get(i).get("CHECK_NAME").toString());
                    checkValues.put("CHECK_TYPE", dtcheck.Rows.get(i).get("CHECK_TYPE").toString());
                    checkValues.put("USL", dtcheck.Rows.get(i).get("USL").toString());
                    checkValues.put("LSL", dtcheck.Rows.get(i).get("LSL").toString());
                    checkValues.put("TARGET", dtcheck.Rows.get(i).get("TARGET").toString());
                    checkValues.put("UOM", dtcheck.Rows.get(i).get("UOM").toString());
                    checkValues.put("CHECK_VALUE", dtcheck.Rows.get(i).get("CHECK_VALUE").toString());
                    checkValues.put("CHECK_RESULT", dtcheck.Rows.get(i).get("CHECK_RESULT").toString());
//                    checkValues.put("STD_HOUR", dtcheck.Rows.get(i).get("STD_HOUR").toString());
                    checkValues.put("CMT", dtcheck.Rows.get(i).get("CMT").toString());
                    checkValues.put("DESC_TYPE", dtcheck.Rows.get(i).get("DESC_TYPE").toString());
                    dbWrite.insertOrThrow("SEMS_PM_WO_CHECK", null, checkValues);
                }
            }

            dbWrite.setTransactionSuccessful();
            Message msg = new Message();
            msg.what = 1;
            handler.sendMessage(msg);
        } catch (Exception ex) {
            ex.getMessage();
            ShowMessage(ex.getMessage());
        } finally {
            dbWrite.endTransaction();
        }
    }

    private void DeleteWo(String downloadType, String woId) {
        offData.getWritableDatabase().delete("SEMS_MRO_WO", "MRO_WO_ID = ?", new String[]{woId});
        offData.getWritableDatabase().delete("SEMS_MRO_PART_TRX", "MRO_WO_ID = ?", new String[]{woId});
        offData.getWritableDatabase().delete("SEMS_MRO_WO_WH", "MRO_WO_ID = ?", new String[]{woId});
        offData.getWritableDatabase().delete("SEMS_FILE", "MRO_WO_ID = ?", new String[]{woId});

        if (downloadType.toUpperCase().contentEquals("REPAIR")) {
            offData.getWritableDatabase().delete("SEMS_REPAIR_FAIL", "MRO_WO_ID = ?", new String[]{woId});
            offData.getWritableDatabase().delete("SEMS_REPAIR_WO_RSN", "MRO_WO_ID = ?", new String[]{woId});
            offData.getWritableDatabase().delete("SEMS_REPAIR_WO_STY", "MRO_WO_ID = ?", new String[]{woId});
        } else {
            offData.getWritableDatabase().delete("SBRM_EMS_CHECK_CONSUMABLE", "MRO_WO_ID = ?", new String[]{woId});
            offData.getWritableDatabase().delete("SBRM_EMS_CHECK_FIX_TOOL", "MRO_WO_ID = ?", new String[]{woId});
            offData.getWritableDatabase().delete("SBRM_EMS_CHECK_METHOD", "MRO_WO_ID = ?", new String[]{woId});
            offData.getWritableDatabase().delete("SBRM_EMS_PM_SOP", "MRO_WO_ID = ?", new String[]{woId});
            offData.getWritableDatabase().delete("SEMS_PM_WO_CHECK", "MRO_WO_ID = ?", new String[]{woId});
        }
    }

    private void BindCheckData(String eqpKey, String pmKey, String startDate, String endDate) {
        try {
            //取得目前工單資料
            SQLiteDatabase dbQuery = offData.getReadableDatabase();
            Cursor cursor;

            if (downloadType.toUpperCase().contentEquals("PM")) {
                cursor = dbQuery.query("USER_WO", null, "MRO_WO_TYPE = ? COLLATE NOCASE AND USER_ID = ?", new String[]{"PM",DownloadWoActivity.this.getGlobal().getUserID()}, null, null, null);
            } else if (downloadType.toUpperCase().contentEquals("REPAIR")) {
                cursor = dbQuery.query("USER_WO", null, "MRO_WO_TYPE = ? COLLATE NOCASE AND USER_ID = ?", new String[]{"Repair",DownloadWoActivity.this.getGlobal().getUserID()}, null, null, null);
            } else {
                cursor = dbQuery.query("USER_WO", null, "MRO_WO_TYPE = ? COLLATE NOCASE AND USER_ID = ?", new String[]{"Check",DownloadWoActivity.this.getGlobal().getUserID()}, null, null, null);
            }
            cursor.moveToFirst();

            sbExistWo = new StringBuilder();
            for (int i = 0; i < cursor.getCount(); i++) {
                arExistWo.add(cursor.getString(cursor.getColumnIndex("MRO_WO_ID")));
                sbExistWo.append("," + cursor.getString(cursor.getColumnIndex("MRO_WO_ID")));
                cursor.moveToNext();
            }

            BModuleObject biObjWo = new BModuleObject();
            biObjWo.setBModuleName("Unicom.Uniworks.BModule.EMS.BIGetWoAndroid");
            biObjWo.setModuleID("GetCheckInfo");
            biObjWo.setRequestID("GetCheckInfo");
            biObjWo.params = new Vector<ParameterInfo>();

            ParameterInfo userParam = new ParameterInfo();
            userParam.setParameterID(BIGetWoAndroidParam.UserKey);
            userParam.setParameterValue(DownloadWoActivity.this.getGlobal().getUserKey());
            biObjWo.params.add(userParam);

            ParameterInfo eqpParam = new ParameterInfo();
            eqpParam.setParameterID(BIGetWoAndroidParam.EqpKey);
            eqpParam.setParameterValue(eqpKey);
            biObjWo.params.add(eqpParam);

            ParameterInfo pmParam = new ParameterInfo();
            pmParam.setParameterID(BIGetWoAndroidParam.PMKey);
            pmParam.setParameterValue(pmKey);
            biObjWo.params.add(pmParam);

            ParameterInfo startDateParam = new ParameterInfo();
            startDateParam.setParameterID(BIGetWoAndroidParam.StartDate);
            startDateParam.setParameterValue(startDate);
            biObjWo.params.add(startDateParam);

            ParameterInfo endDateParam = new ParameterInfo();
            endDateParam.setParameterID(BIGetWoAndroidParam.EndDate);
            endDateParam.setParameterValue(endDate);
            biObjWo.params.add(endDateParam);

            ParameterInfo existWoParam = new ParameterInfo();
            existWoParam.setParameterID(BIGetWoAndroidParam.ExistWo);
            existWoParam.setParameterValue(sbExistWo.toString());
            biObjWo.params.add(existWoParam);

            CallBIModule(biObjWo, new WebAPIClientEvent() {
                @Override
                public void onPostBack(BModuleReturn bModuleReturn) {
                    dtWo = bModuleReturn.getReturnJsonTables().get("GetCheckInfo").get("SEMS_MRO_WO");
                    dtFile = bModuleReturn.getReturnJsonTables().get("GetCheckInfo").get("SEMS_FILE");
                    dtcheck = bModuleReturn.getReturnJsonTables().get("GetCheckInfo").get("SEMS_PM_WO_CHECK");
                    dtEqpPart = bModuleReturn.getReturnJsonTables().get("GetCheckInfo").get("SEMS_EQP_PART");
                    dtCheckMethod = bModuleReturn.getReturnJsonTables().get("GetCheckInfo").get("SBRM_EMS_CHECK_METHOD");
                    dtCheckConsume = bModuleReturn.getReturnJsonTables().get("GetCheckInfo").get("SBRM_EMS_CHECK_CONSUMABLE");
                    dtCheckTool = bModuleReturn.getReturnJsonTables().get("GetCheckInfo").get("SBRM_EMS_CHECK_FIX_TOOL");
                    dtPartTrx = bModuleReturn.getReturnJsonTables().get("GetCheckInfo").get("SEMS_MRO_PART_TRX");
                    dtWH = bModuleReturn.getReturnJsonTables().get("GetCheckInfo").get("SEMS_MRO_WO_WH");
                    dtSop = bModuleReturn.getReturnJsonTables().get("GetCheckInfo").get("SBRM_EMS_PM_SOP");

                    if (dtWo != null && dtWo.Rows.size() > 0) {
                        final LayoutInflater layoutInflater = getLayoutInflater();
                        adapter = new WoDownloadAdapter(layoutInflater, dtWo);
                        lsWo.setAdapter(adapter);
                    } else {
                        //region 清空前面查詢出來的table
                        dtWo = new DataTable();
                        dtWH = new DataTable();
                        dtEqpPart = new DataTable();
                        dtFile = new DataTable();
                        dtPartTrx = new DataTable();
                        dtCheckMethod = new DataTable();
                        dtCheckConsume = new DataTable();
                        dtCheckTool = new DataTable();
                        dtSop = new DataTable();
                        dtcheck = new DataTable();
                        dtFail = new DataTable();
                        dtFailRsn = new DataTable();
                        dtFailSty = new DataTable();
                        //endregion

                        lsWo.setAdapter(null);
                        ShowMessage(getResources().getString(R.string.QUERY_NO_DATA));
                        return;
                    }
                }
            });
        } catch (Exception ex) {
            ex.getMessage();
        }
    }

    private void BindPmData(String eqpKey, String pmKey, String startDate, String endDate) {
        try {
            //取得目前工單資料
            SQLiteDatabase dbQuery = offData.getReadableDatabase();
            Cursor cursor;

            if (downloadType.toUpperCase().contentEquals("PM")) {
                cursor = dbQuery.query("USER_WO", null, "MRO_WO_TYPE = ? COLLATE NOCASE AND USER_ID = ?", new String[]{"PM",DownloadWoActivity.this.getGlobal().getUserID()}, null, null, null);
            } else if (downloadType.toUpperCase().contentEquals("REPAIR")) {
                cursor = dbQuery.query("USER_WO", null, "MRO_WO_TYPE = ? COLLATE NOCASE AND USER_ID = ?", new String[]{"Repair",DownloadWoActivity.this.getGlobal().getUserID()}, null, null, null);
            } else {
                cursor = dbQuery.query("USER_WO", null, "MRO_WO_TYPE = ? COLLATE NOCASE AND USER_ID = ?", new String[]{"Check",DownloadWoActivity.this.getGlobal().getUserID()}, null, null, null);
            }
            cursor.moveToFirst();

            sbExistWo = new StringBuilder();
            for (int i = 0; i < cursor.getCount(); i++) {
                arExistWo.add(cursor.getString(cursor.getColumnIndex("MRO_WO_ID")));
                sbExistWo.append("," + cursor.getString(cursor.getColumnIndex("MRO_WO_ID")));
                cursor.moveToNext();
            }

            BModuleObject biObjWo = new BModuleObject();
            biObjWo.setBModuleName("Unicom.Uniworks.BModule.EMS.BIGetWoAndroid");
            biObjWo.setModuleID("GetPmInfo");
            biObjWo.setRequestID("GetPmInfo");
            biObjWo.params = new Vector<ParameterInfo>();

            ParameterInfo userParam = new ParameterInfo();
            userParam.setParameterID(BIGetWoAndroidParam.UserKey);
            userParam.setParameterValue(DownloadWoActivity.this.getGlobal().getUserKey());
            biObjWo.params.add(userParam);

            ParameterInfo eqpParam = new ParameterInfo();
            eqpParam.setParameterID(BIGetWoAndroidParam.EqpKey);
            eqpParam.setParameterValue(eqpKey);
            biObjWo.params.add(eqpParam);

            ParameterInfo pmParam = new ParameterInfo();
            pmParam.setParameterID(BIGetWoAndroidParam.PMKey);
            pmParam.setParameterValue(pmKey);
            biObjWo.params.add(pmParam);

            ParameterInfo startDateParam = new ParameterInfo();
            startDateParam.setParameterID(BIGetWoAndroidParam.StartDate);
            startDateParam.setParameterValue(startDate);
            biObjWo.params.add(startDateParam);

            ParameterInfo endDateParam = new ParameterInfo();
            endDateParam.setParameterID(BIGetWoAndroidParam.EndDate);
            endDateParam.setParameterValue(endDate);
            biObjWo.params.add(endDateParam);

            ParameterInfo existWoParam = new ParameterInfo();
            existWoParam.setParameterID(BIGetWoAndroidParam.ExistWo);
            existWoParam.setParameterValue(sbExistWo.toString());
            biObjWo.params.add(existWoParam);

            CallBIModule(biObjWo, new WebAPIClientEvent() {
                @Override
                public void onPostBack(BModuleReturn bModuleReturn) {
                    dtWo = bModuleReturn.getReturnJsonTables().get("GetPmInfo").get("SEMS_MRO_WO");
                    dtFile = bModuleReturn.getReturnJsonTables().get("GetPmInfo").get("SEMS_FILE");
                    dtcheck = bModuleReturn.getReturnJsonTables().get("GetPmInfo").get("SEMS_PM_WO_CHECK");
                    dtEqpPart = bModuleReturn.getReturnJsonTables().get("GetPmInfo").get("SEMS_EQP_PART");
                    dtCheckMethod = bModuleReturn.getReturnJsonTables().get("GetPmInfo").get("SBRM_EMS_CHECK_METHOD");
                    dtCheckConsume = bModuleReturn.getReturnJsonTables().get("GetPmInfo").get("SBRM_EMS_CHECK_CONSUMABLE");
                    dtCheckTool = bModuleReturn.getReturnJsonTables().get("GetPmInfo").get("SBRM_EMS_CHECK_FIX_TOOL");
                    dtPartTrx = bModuleReturn.getReturnJsonTables().get("GetPmInfo").get("SEMS_MRO_PART_TRX");
                    dtWH = bModuleReturn.getReturnJsonTables().get("GetPmInfo").get("SEMS_MRO_WO_WH");
                    dtSop = bModuleReturn.getReturnJsonTables().get("GetPmInfo").get("SBRM_EMS_PM_SOP");

                    if (dtWo != null && dtWo.Rows.size() > 0) {
                        final LayoutInflater layoutInflater = getLayoutInflater();
                        adapter = new WoDownloadAdapter(layoutInflater, dtWo);
                        lsWo.setAdapter(adapter);
                    } else {
                        //region 清空前面查詢出來的table
                        dtWo = new DataTable();
                        dtWH = new DataTable();
                        dtEqpPart = new DataTable();
                        dtFile = new DataTable();
                        dtPartTrx = new DataTable();
                        dtCheckMethod = new DataTable();
                        dtCheckConsume = new DataTable();
                        dtCheckTool = new DataTable();
                        dtSop = new DataTable();
                        dtcheck = new DataTable();
                        dtFail = new DataTable();
                        dtFailRsn = new DataTable();
                        dtFailSty = new DataTable();
                        //endregion

                        lsWo.setAdapter(null);
                        ShowMessage(getResources().getString(R.string.QUERY_NO_DATA));
                        return;
                    }
                }
            });
        } catch (Exception ex) {
            ex.getMessage();
        }
    }

    private void BindRepairData(String eqpKey, String startDate, String endDate) {
        SQLiteDatabase dbQuery = offData.getReadableDatabase();
        Cursor cursor;

        if (downloadType.toUpperCase().contentEquals("PM")) {
            cursor = dbQuery.query("USER_WO", null, "MRO_WO_TYPE = ? COLLATE NOCASE AND USER_ID = ?", new String[]{"PM",DownloadWoActivity.this.getGlobal().getUserID()}, null, null, null);
        } else if (downloadType.toUpperCase().contentEquals("REPAIR")) {
            cursor = dbQuery.query("USER_WO", null, "MRO_WO_TYPE = ? COLLATE NOCASE AND USER_ID = ?", new String[]{"Repair",DownloadWoActivity.this.getGlobal().getUserID()}, null, null, null);
        } else {
            cursor = dbQuery.query("USER_WO", null, "MRO_WO_TYPE = ? COLLATE NOCASE AND USER_ID = ?", new String[]{"Check",DownloadWoActivity.this.getGlobal().getUserID()}, null, null, null);
        }
        cursor.moveToFirst();

        sbExistWo = new StringBuilder();
        for (int i = 0; i < cursor.getCount(); i++) {
            arExistWo.add(cursor.getString(cursor.getColumnIndex("MRO_WO_ID")));
            sbExistWo.append("," + cursor.getString(cursor.getColumnIndex("MRO_WO_ID")));
            cursor.moveToNext();
        }

        BModuleObject biObjWo = new BModuleObject();
        biObjWo.setBModuleName("Unicom.Uniworks.BModule.EMS.BIGetWoAndroid");
        biObjWo.setModuleID("GetRepairInfo");
        biObjWo.setRequestID("GetRepairInfo");
        biObjWo.params = new Vector<ParameterInfo>();

        ParameterInfo userParam = new ParameterInfo();
        userParam.setParameterID(BIGetWoAndroidParam.UserKey);
        userParam.setParameterValue(DownloadWoActivity.this.getGlobal().getUserKey());
        biObjWo.params.add(userParam);

        ParameterInfo eqpParam = new ParameterInfo();
        eqpParam.setParameterID(BIGetWoAndroidParam.EqpKey);
        eqpParam.setParameterValue(eqpKey);
        biObjWo.params.add(eqpParam);

        ParameterInfo startDateParam = new ParameterInfo();
        startDateParam.setParameterID(BIGetWoAndroidParam.StartDate);
        startDateParam.setParameterValue(startDate);
        biObjWo.params.add(startDateParam);

        ParameterInfo endDateParam = new ParameterInfo();
        endDateParam.setParameterID(BIGetWoAndroidParam.EndDate);
        endDateParam.setParameterValue(endDate);
        biObjWo.params.add(endDateParam);

        ParameterInfo existWoParam = new ParameterInfo();
        existWoParam.setParameterID(BIGetWoAndroidParam.ExistWo);
        existWoParam.setParameterValue(sbExistWo.toString());
        biObjWo.params.add(existWoParam);

        CallBIModule(biObjWo, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                dtWo = bModuleReturn.getReturnJsonTables().get("GetRepairInfo").get("SEMS_MRO_WO");
                dtFile = bModuleReturn.getReturnJsonTables().get("GetRepairInfo").get("SEMS_FILE");
                dtEqpPart = bModuleReturn.getReturnJsonTables().get("GetRepairInfo").get("SEMS_EQP_PART");
                dtPartTrx = bModuleReturn.getReturnJsonTables().get("GetRepairInfo").get("SEMS_MRO_PART_TRX");
                dtWH = bModuleReturn.getReturnJsonTables().get("GetRepairInfo").get("SEMS_MRO_WO_WH");
                dtFailSty = bModuleReturn.getReturnJsonTables().get("GetRepairInfo").get("SEMS_REPAIR_WO_STY");
                dtFail = bModuleReturn.getReturnJsonTables().get("GetRepairInfo").get("SEMS_REPAIR_FAIL");
                dtFailRsn = bModuleReturn.getReturnJsonTables().get("GetRepairInfo").get("SEMS_REPAIR_WO_RSN");

                if (dtWo != null && dtWo.Rows.size() > 0) {
                    final LayoutInflater layoutInflater = getLayoutInflater();
                    adapter = new WoDownloadAdapter(layoutInflater, dtWo);
                    lsWo.setAdapter(adapter);
                } else {
                    //region 清空前面查詢出來的table
                    dtWo = new DataTable();
                    dtWH = new DataTable();
                    dtEqpPart = new DataTable();
                    dtFile = new DataTable();
                    dtPartTrx = new DataTable();
                    dtCheckMethod = new DataTable();
                    dtCheckConsume = new DataTable();
                    dtCheckTool = new DataTable();
                    dtSop = new DataTable();
                    dtcheck = new DataTable();
                    dtFail = new DataTable();
                    dtFailRsn = new DataTable();
                    dtFailSty = new DataTable();
                    //endregion

                    lsWo.setAdapter(null);
                    ShowMessage(getResources().getString(R.string.QUERY_NO_DATA));
                    return;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BindSpinner();
    }

    private void BindSpinner() {
        ArrayList<BModuleObject> lsBObj = new ArrayList<>();
        BModuleObject biObjEqp = new BModuleObject();
        biObjEqp.setBModuleName("Unicom.Uniworks.BModule.EMS.BIGetEqpAndroid");
        biObjEqp.setModuleID("GetEqp");
        biObjEqp.setRequestID("GetEqp");
        biObjEqp.params = new Vector<ParameterInfo>();
        ParameterInfo paramUser = new ParameterInfo();
        paramUser.setParameterID(BIGetEqpAndroidParam.UserKey);
        paramUser.setParameterValue(DownloadWoActivity.this.getGlobal().getUserKey());
        biObjEqp.params.add(paramUser);
        lsBObj.add(biObjEqp);

        BModuleObject biObjPm = new BModuleObject();
        biObjPm.setBModuleName("Unicom.Uniworks.BModule.EMS.BIGetPMAndroid");
        biObjPm.setModuleID("GetPMContent");
        biObjPm.setRequestID("GetPMContent");
        lsBObj.add(biObjPm);

        CallBIModule(lsBObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    dtEqp = bModuleReturn.getReturnJsonTables().get("GetEqp").get("SBRM_EQP");
                    dtPm = bModuleReturn.getReturnJsonTables().get("GetPMContent").get("SBRM_EQP");

                    ArrayList lsEqp = new ArrayList();
                    ArrayList lsPm = new ArrayList();

                    for (int i = 0; i < dtEqp.Rows.size(); i++) {
                        HashMap<String, String> eqp = new HashMap<>();
                        eqp.put("EQP_KEY", dtEqp.Rows.get(i).get("EQP_KEY").toString());
                        eqp.put("IDNAME", dtEqp.Rows.get(i).get("IDNAME").toString());
                        lsEqp.add(eqp);
                    }

                    for (int i = 0; i < dtPm.Rows.size(); i++) {
                        HashMap<String, String> pm = new HashMap<>();
                        pm.put("PM_KEY", dtPm.Rows.get(i).get("PM_KEY").toString());
                        pm.put("IDNAME", dtPm.Rows.get(i).get("IDNAME").toString());
                        lsPm.add(pm);
                    }

                    SimpleAdapter eqpAdapter = new SimpleAdapter(DownloadWoActivity.this, lsEqp, android.R.layout.simple_list_item_1, new String[]{"EQP_KEY", "IDNAME"}, new int[]{0, android.R.id.text1});
                    SimpleAdapter pmAdapter = new SimpleAdapter(DownloadWoActivity.this, lsPm, android.R.layout.simple_list_item_1, new String[]{"PM_KEY", "IDNAME"}, new int[]{0, android.R.id.text1});

                    spEqp.setAdapter(eqpAdapter);
                    spPm.setAdapter(pmAdapter);
                }
            }
        });
    }

    //設定EditText不可修改
    public static void setEditTextReadOnly(EditText text) {
        text.setTextColor(Color.BLACK);   //設置顏色，使其看起來像只讀模式
        if (text instanceof android.widget.EditText) {
            text.setCursorVisible(false);      //設置光標不可見
            text.setFocusable(false);           //無焦點
            text.setFocusableInTouchMode(false);     //觸摸時也得不到焦點
        }
    }

    public void FromDateOnClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.style_wms_dialog_date_picker_spinner, null);
        final DatePicker datePicker = view.findViewById(R.id.date_picker);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);

        builder.setView(view);
        builder.setTitle(R.string.FROM_DATE);

        final AlertDialog dialog = builder.create();
        dialog.show();

        Button bConfirm = view.findViewById(R.id.btnConfirm);
        bConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //日期格式
                StringBuffer sb = new StringBuffer();
                sb.append(String.format("%d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth()));
//                holder.FromDate.setText(sb);
                edStartDate.setText(sb);
                dialog.cancel();
            }
        });
    }

    public void ToDateOnClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.style_wms_dialog_date_picker_spinner, null);
        final DatePicker datePicker = view.findViewById(R.id.date_picker);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);

        builder.setView(view);
        builder.setTitle(R.string.TO_DATE);

        final AlertDialog dialog = builder.create();
        dialog.show();

        Button bConfirm = view.findViewById(R.id.btnConfirm);
        bConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //日期格式
                StringBuffer sb = new StringBuffer();
                sb.append(String.format("%d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth()));
//                holder.ToDate.setText(sb);
                edEndDate.setText(sb);
                dialog.cancel();
            }
        });
    }

    public void onClickFromDateClear(View v) {
        edStartDate.setText("");
    }

    public void onClickToDateClear(View v) {
        edEndDate.setText("");
    }
}
