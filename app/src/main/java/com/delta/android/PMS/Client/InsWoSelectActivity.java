package com.delta.android.PMS.Client;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.delta.android.Core.Activity.BaseActivity;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.PMS.OffLineData.Data;
import com.delta.android.PMS.Param.BIGetEqpAndroidParam;
import com.delta.android.PMS.Param.BIGetWoAndroidParam;
import com.delta.android.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class InsWoSelectActivity extends BaseActivity {

    Spinner spEqp;
    Spinner spPm;
    EditText edFilter;
    ListView lsWo;
    Data offData;
    ProgressDialog progressDialog;
    SimpleCursorAdapter adapter;

    DataTable dtWo;
    DataTable dtFile;
    DataTable dtcheck;
    DataTable dtEqpPart;
    DataTable dtCheckMethod;
    DataTable dtCheckTool;
    DataTable dtPartTrx;
    DataTable dtWH;
    DataTable dtSop;
    DataTable dtCheckConsume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pms_ins_wo_select);

        spEqp = (Spinner) findViewById(R.id.spDownloadEqp);
        spPm = (Spinner) findViewById(R.id.spDownloadPm);
        edFilter = (EditText) findViewById(R.id.edPepairId);
        lsWo = (ListView) findViewById(R.id.lvPairQueryWo);
        lsWo.setTextFilterEnabled(true); //開啟過濾功能
        offData = new Data(InsWoSelectActivity.this);

        BindInsWoList(); //Binding 點檢工單

        edFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                String partialValue = constraint.toString();
                SQLiteDatabase db = offData.getReadableDatabase();
                Cursor cursor = db.query("SEMS_MRO_WO", null,
                        "MRO_WO_TYPE = 'Check' AND ((MRO_WO_ID LIKE '%" + partialValue + "%') OR (EQP_ID LIKE '%" + partialValue + "%') OR (PM_ID LIKE '%" + partialValue + "%') )",
                        null, null, null, null);
                return cursor;
            }
        });

        Button btnQuery = (Button) findViewById(R.id.btnQueryInsWo);
        btnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isConn = CheckConnection(); //檢查網路連線狀況
                if (!isConn) {
                    ShowMessage("Connection error");
                    return;
                }

                edFilter.setText("");
                progressDialog = new ProgressDialog(InsWoSelectActivity.this);
                DeleteAllInsWo(); //砍掉本機的全部"點檢工單"
                String strEqpKey = ((HashMap) spEqp.getSelectedItem()).get("EQP_KEY").toString();
                String strPmKey = ((HashMap) spPm.getSelectedItem()).get("PM_KEY").toString();

                BModuleObject biObjWo = new BModuleObject();
                biObjWo.setBModuleName("Unicom.Uniworks.BModule.EMS.BIGetWoAndroid");
                biObjWo.setModuleID("GetCheckInfo");
                biObjWo.setRequestID("GetCheckInfo");
                biObjWo.params = new Vector<ParameterInfo>();

                ParameterInfo userParam = new ParameterInfo();
                userParam.setParameterID(BIGetWoAndroidParam.UserKey);
                userParam.setParameterValue(InsWoSelectActivity.this.getGlobal().getUserKey());
                biObjWo.params.add(userParam);

                ParameterInfo eqpParam = new ParameterInfo();
                eqpParam.setParameterID(BIGetWoAndroidParam.EqpKey);
                eqpParam.setParameterValue(strEqpKey);
                biObjWo.params.add(eqpParam);

                ParameterInfo pmParam = new ParameterInfo();
                pmParam.setParameterID(BIGetWoAndroidParam.PMKey);
                pmParam.setParameterValue(strPmKey);
                biObjWo.params.add(pmParam);

                CallBIModule(biObjWo, new WebAPIClientEvent() {
                    @Override
                    public void onPostBack(BModuleReturn bModuleReturn) {
                        dtWo = bModuleReturn.getReturnJsonTables().get("GetCheckInfo").get("SEMS_MRO_WO");
                        dtFile = bModuleReturn.getReturnJsonTables().get("GetCheckInfo").get("SEMS_FILE");
                        dtcheck = bModuleReturn.getReturnJsonTables().get("GetCheckInfo").get("SEMS_PM_WO_CHECK");
                        dtEqpPart = bModuleReturn.getReturnJsonTables().get("GetCheckInfo").get("SEMS_EQP_PART");
                        dtCheckMethod = bModuleReturn.getReturnJsonTables().get("GetCheckInfo").get("SBRM_EMS_CHECK_METHOD");
                        dtCheckTool = bModuleReturn.getReturnJsonTables().get("GetCheckInfo").get("SBRM_EMS_CHECK_FIX_TOOL");
                        dtCheckConsume = bModuleReturn.getReturnJsonTables().get("GetCheckInfo").get("SBRM_EMS_CHECK_CONSUMABLE");
                        dtPartTrx = bModuleReturn.getReturnJsonTables().get("GetCheckInfo").get("SEMS_MRO_PART_TRX");
                        dtWH = bModuleReturn.getReturnJsonTables().get("GetCheckInfo").get("SEMS_MRO_WO_WH");
                        dtSop = bModuleReturn.getReturnJsonTables().get("GetCheckInfo").get("SBRM_EMS_PM_SOP");

                        if (dtWo != null && dtWo.Rows.size() > 0) {
                            //將查詢出來的工單資訊寫到sqlite
                            DownloadWo();
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
                            //endregion

                            lsWo.setAdapter(null);
                            ShowMessage(getResources().getString(R.string.QUERY_NO_DATA));
                            return;
                        }
                    }
                });
            }
        });

        lsWo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectWoId = ((SQLiteCursor) adapter.getItem(position)).getString(((SQLiteCursor) adapter.getItem(position)).getColumnIndex("MRO_WO_ID"));
                Intent intent = new Intent();
                intent.setClass(InsWoSelectActivity.this, WorkInsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("WO_ID", selectWoId);
                intent.putExtras(bundle);
                edFilter.setText("");
                startActivity(intent);
            }
        });
    }

    private void BindInsWoList() {
        String querySql = "SELECT * FROM SEMS_MRO_WO WHERE MRO_WO_TYPE = 'Check' COLLATE NOCASE AND MRO_WO_ID IN (SELECT MRO_WO_ID FROM USER_WO WHERE USER_ID =?)";
        Cursor csInsWo = offData.getReadableDatabase().rawQuery(querySql,new String[]{InsWoSelectActivity.this.getGlobal().getUserID()});

        adapter = new SimpleCursorAdapter(this,
                R.layout.activity_pms_ins_wo_listview,
                csInsWo,
                new String[]{"MRO_WO_ID", "EQP_ID", "PM_ID", "PLAN_DT", "WO_STATUS"},
                new int[]{R.id.tvLsWoId, R.id.tvLsEqp, R.id.tvLsPm, R.id.tvLsPmPlanDate, R.id.tvlsWoStatus},
                0);

        lsWo.setAdapter(adapter);
        lsWo.setTextFilterEnabled(true); //開啟過濾功能
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                String partialValue = constraint.toString();
                SQLiteDatabase db = offData.getReadableDatabase();
                Cursor cursor = db.query("SEMS_MRO_WO", null,
                        "MRO_WO_TYPE = 'Check' AND ((MRO_WO_ID LIKE '%" + partialValue + "%') OR (EQP_ID LIKE '%" + partialValue + "%') OR (PM_ID LIKE '%" + partialValue + "%') ) " +
                                "AND MRO_WO_ID IN (SELECT MRO_WO_ID FROM USER_WO WHERE USER_ID =?)",
                        new String[]{InsWoSelectActivity.this.getGlobal().getUserID()}, null, null, null);
                return cursor;
            }
        });
    }

    private void DownloadWo() {
        SQLiteDatabase dbWrite = offData.getWritableDatabase();

        try {
            dbWrite.beginTransaction();

            for (int i = 0; i < dtWo.Rows.size(); i++) {
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
                dbWrite.insert("SEMS_MRO_WO", null, woValues);

                //紀錄工單與人員對應資訊到sqlite
                ContentValues woUserValues = new ContentValues();
                woUserValues.put("USER_ID", InsWoSelectActivity.this.getGlobal().getUserID());
                woUserValues.put("MRO_WO_ID", dtWo.Rows.get(i).get("MRO_WO_ID").toString());
                woUserValues.put("MRO_WO_TYPE", dtWo.Rows.get(i).get("MRO_WO_TYPE").toString());
                dbWrite.insert("USER_WO", null, woUserValues);
            }

            for (int i = 0; i < dtWH.Rows.size(); i++) {
                ContentValues userValues = new ContentValues();
                userValues.put("MRO_WO_ID", dtWH.Rows.get(i).get("MRO_WO_ID").toString());
                userValues.put("USER_ID", dtWH.Rows.get(i).get("USER_ID").toString());
                userValues.put("USER_NAME", dtWH.Rows.get(i).get("USER_NAME").toString());
                userValues.put("START_DT", dtWH.Rows.get(i).get("START_DT").toString());
                userValues.put("END_DT", dtWH.Rows.get(i).get("END_DT").toString());
                userValues.put("CMT", dtWH.Rows.get(i).get("CMT").toString());
                dbWrite.insert("SEMS_MRO_WO_WH", null, userValues);
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
                dbWrite.insert("SEMS_EQP_PART", null, eqpPartValues);
            }

            for (int i = 0; i < dtPartTrx.Rows.size(); i++) {
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
                dbWrite.insert("SEMS_MRO_PART_TRX", null, partTrxValues);
            }

            for (int i = 0; i < dtFile.Rows.size(); i++) {
                ContentValues fileValues = new ContentValues();
                fileValues.put("MRO_WO_ID", dtFile.Rows.get(i).get("MRO_WO_ID").toString());
                fileValues.put("FILE_ID", dtFile.Rows.get(i).get("FILE_NAME").toString());
                fileValues.put("FILE_DESC", dtFile.Rows.get(i).get("FILE_DESC").toString());
                fileValues.put("UPLOAD_USER_ID", dtFile.Rows.get(i).get("USER_ID").toString());
                fileValues.put("UPLOAD_DATE", dtFile.Rows.get(i).get("TRX_DATE").toString());
                dbWrite.insert("SEMS_FILE", null, fileValues);
            }

            for (int i = 0; i < dtCheckMethod.Rows.size(); i++) {
                ContentValues methodValues = new ContentValues();
                methodValues.put("PM_METHOD_ID", dtCheckMethod.Rows.get(i).get("PM_METHOD_ID").toString());
                methodValues.put("PM_METHOD_NAME", dtCheckMethod.Rows.get(i).get("PM_METHOD_NAME").toString());
                methodValues.put("CHECK_ID", dtCheckMethod.Rows.get(i).get("CHECK_ID").toString());
                methodValues.put("CHECK_NAME", dtCheckMethod.Rows.get(i).get("CHECK_NAME").toString());
                methodValues.put("MRO_WO_ID", dtCheckMethod.Rows.get(i).get("MRO_WO_ID").toString());
                dbWrite.insert("SBRM_EMS_CHECK_METHOD", null, methodValues);
            }

            for (int i = 0; i < dtCheckConsume.Rows.size(); i++) {
                ContentValues consumeValues = new ContentValues();
                consumeValues.put("CONSUMABLE_LIST_ID", dtCheckConsume.Rows.get(i).get("CONSUMABLE_LIST_ID").toString());
                consumeValues.put("CONSUMABLE_LIST_NAME", dtCheckConsume.Rows.get(i).get("CONSUMABLE_LIST_NAME").toString());
                consumeValues.put("CONSUMABLE_TYPE_ID", dtCheckConsume.Rows.get(i).get("CONSUMABLE_TYPE_ID").toString());
                consumeValues.put("CONSUMABLE_TYPE_NAME", dtCheckConsume.Rows.get(i).get("CONSUMABLE_TYPE_NAME").toString());
                consumeValues.put("CHECK_ID", dtCheckConsume.Rows.get(i).get("CHECK_ID").toString());
                consumeValues.put("CHECK_NAME", dtCheckConsume.Rows.get(i).get("CHECK_NAME").toString());
                consumeValues.put("MRO_WO_ID", dtCheckConsume.Rows.get(i).get("MRO_WO_ID").toString());
                dbWrite.insert("SBRM_EMS_CHECK_CONSUMABLE", null, consumeValues);
            }

            for (int i = 0; i < dtCheckTool.Rows.size(); i++) {
                ContentValues toolValues = new ContentValues();
                toolValues.put("FIX_TOOL_ID", dtCheckTool.Rows.get(i).get("FIX_TOOL_ID").toString());
                toolValues.put("FIX_TOOL_NAME", dtCheckTool.Rows.get(i).get("FIX_TOOL_NAME").toString());
                toolValues.put("CHECK_ID", dtCheckTool.Rows.get(i).get("CHECK_ID").toString());
                toolValues.put("CHECK_NAME", dtCheckTool.Rows.get(i).get("CHECK_NAME").toString());
                toolValues.put("MRO_WO_ID", dtCheckTool.Rows.get(i).get("MRO_WO_ID").toString());
                dbWrite.insert("SBRM_EMS_CHECK_FIX_TOOL", null, toolValues);
            }

            for (int i = 0; i < dtSop.Rows.size(); i++) {
                ContentValues sopValues = new ContentValues();
                sopValues.put("SOP_ID", dtSop.Rows.get(i).get("SOP_ID").toString());
                sopValues.put("SOP_NAME", dtSop.Rows.get(i).get("SOP_NAME").toString());
                sopValues.put("DOC_NO", dtSop.Rows.get(i).get("DOC_NO").toString());
                sopValues.put("URL", dtSop.Rows.get(i).get("URL").toString());
                sopValues.put("FULL_FILE_NAME", dtSop.Rows.get(i).get("FULL_FILE_NAME").toString());
                sopValues.put("MRO_WO_ID", dtSop.Rows.get(i).get("MRO_WO_ID").toString());
                dbWrite.insert("SBRM_EMS_PM_SOP", null, sopValues);
            }

            for (int i = 0; i < dtcheck.Rows.size(); i++) {
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
//                checkValues.put("STD_HOUR", dtcheck.Rows.get(i).get("STD_HOUR").toString());
                checkValues.put("CMT", dtcheck.Rows.get(i).get("CMT").toString());
                checkValues.put("DESC_TYPE", dtcheck.Rows.get(i).get("DESC_TYPE").toString());
                dbWrite.insert("SEMS_PM_WO_CHECK", null, checkValues);
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

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                progressDialog.dismiss();
                BindInsWoList();
            }
        }
    };

    private boolean CheckConnection() {
        boolean checkConn = true;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo == null) {
            checkConn = false;
        } else {
            if (networkInfo.isFailover()) {
                checkConn = false;
            }

            if (!networkInfo.isConnected()) {
                checkConn = false;
            }
        }

        return checkConn;
    }

    private void DeleteAllInsWo() {
        Cursor csInsWo = offData.getReadableDatabase().query("SEMS_MRO_WO", null, "MRO_WO_TYPE = 'Check' COLLATE NOCASE AND MRO_WO_ID IN (SELECT MRO_WO_ID FROM USER_WO WHERE USER_ID = ?)"
                , new String[]{InsWoSelectActivity.this.getGlobal().getUserID()}, null, null, null);
        csInsWo.moveToFirst();

        //先砍掉對應的資料
        for (int i = 0; i < csInsWo.getCount(); i++) {
            String strWoId = csInsWo.getString(csInsWo.getColumnIndex("MRO_WO_ID"));
            offData.getWritableDatabase().delete("SEMS_FILE", "MRO_WO_ID = ?", new String[]{strWoId});
            offData.getWritableDatabase().delete("SEMS_MRO_WO_WH", "MRO_WO_ID = ?", new String[]{strWoId});
            offData.getWritableDatabase().delete("SEMS_MRO_PART_TRX", "MRO_WO_ID = ?", new String[]{strWoId});
            offData.getWritableDatabase().delete("SEMS_PM_WO_CHECK", "MRO_WO_ID = ?", new String[]{strWoId});
            offData.getWritableDatabase().delete("SBRM_EMS_PM_SOP", "MRO_WO_ID = ?", new String[]{strWoId});
            offData.getWritableDatabase().delete("SBRM_EMS_CHECK_METHOD", "MRO_WO_ID = ?", new String[]{strWoId});
            offData.getWritableDatabase().delete("SBRM_EMS_CHECK_FIX_TOOL", "MRO_WO_ID = ?", new String[]{strWoId});
            offData.getWritableDatabase().delete("SBRM_EMS_CHECK_CONSUMABLE", "MRO_WO_ID = ?", new String[]{strWoId});

            csInsWo.moveToNext();
        }

        //砍掉全部工單
        offData.getWritableDatabase().delete("SEMS_MRO_WO",
                "MRO_WO_TYPE = 'Check' COLLATE NOCASE AND MRO_WO_ID IN (SELECT MRO_WO_ID FROM USER_WO WHERE USER_ID = ?)",
                new String[]{InsWoSelectActivity.this.getGlobal().getUserID()});

        //砍掉全部工單對應人員紀錄
        offData.getWritableDatabase().delete("USER_WO","USER_ID = ? AND MRO_WO_TYPE = 'CHECK' COLLATE NOCASE",new String[]{InsWoSelectActivity.this.getGlobal().getUserID()});
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
        paramUser.setParameterValue(InsWoSelectActivity.this.getGlobal().getUserKey());
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
                    DataTable dtEqp = bModuleReturn.getReturnJsonTables().get("GetEqp").get("SBRM_EQP");
                    DataTable dtPm = bModuleReturn.getReturnJsonTables().get("GetPMContent").get("SBRM_EQP");

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

                    SimpleAdapter eqpAdapter = new SimpleAdapter(InsWoSelectActivity.this, lsEqp, android.R.layout.simple_list_item_1, new String[]{"EQP_KEY", "IDNAME"}, new int[]{0, android.R.id.text1});
                    SimpleAdapter pmAdapter = new SimpleAdapter(InsWoSelectActivity.this, lsPm, android.R.layout.simple_list_item_1, new String[]{"PM_KEY", "IDNAME"}, new int[]{0, android.R.id.text1});

                    spEqp.setAdapter(eqpAdapter);
                    spPm.setAdapter(pmAdapter);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BindSpinner();
        BindInsWoList();
        lsWo.setTextFilterEnabled(true); //開啟過濾功能
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                String partialValue = constraint.toString();
                SQLiteDatabase db = offData.getReadableDatabase();
                Cursor cursor = db.query("SEMS_MRO_WO", null,
                        "MRO_WO_TYPE = 'Check' AND ((MRO_WO_ID LIKE '%" + partialValue + "%') OR (EQP_ID LIKE '%" + partialValue + "%') OR (PM_ID LIKE '%" + partialValue + "%') ) " +
                                "AND MRO_WO_ID IN (SELECT MRO_WO_ID FROM USER_WO WHERE USER_ID =?)",
                        new String[]{InsWoSelectActivity.this.getGlobal().getUserID()}, null, null, null);
                return cursor;
            }
        });
    }
}
