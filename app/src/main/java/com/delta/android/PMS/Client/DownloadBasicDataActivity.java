package com.delta.android.PMS.Client;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.delta.android.Core.Adapter.UAdapter;
import com.delta.android.Core.Adapter.UAdapterListener;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.PMS.Common.PreventButtonMultiClick;
import com.delta.android.PMS.OffLineData.Data;
import com.delta.android.PMS.Param.BIGetBRMDatabyPDAParam;
import com.delta.android.PMS.Param.BIGetWoAndroidParam;
import com.google.gson.Gson;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseActivity;
import com.delta.android.R;


public class DownloadBasicDataActivity extends BaseActivity {
    ProgressDialog progressDialog;
    String strConnFailReason = ""; //紀錄連線失敗的錯誤訊息。
    String strServerError = ""; //紀錄call server回傳的錯誤訊息。
    Data offData;
    boolean isUpdateInv = true; //一鍵下載是否要更新庫存資料用的flag

    //region 所有基本資料的table
    DataTable dtUserInfo = new DataTable();
    DataTable dtSopFile = new DataTable();
    DataTable dtFail = new DataTable();
    DataTable dtFailSty = new DataTable();
    DataTable dtFailRsn = new DataTable();
    DataTable dtPmTool = new DataTable();
    DataTable dtPmMethod = new DataTable();
    DataTable dtPmConsumable = new DataTable();
    DataTable dtPartGroup = new DataTable();
    DataTable dtPart = new DataTable();
    DataTable dtStorage = new DataTable();
    DataTable dtBin = new DataTable();
    DataTable dtUserEqp = new DataTable();
    DataTable dtInv = new DataTable();
    DataTable dtCallFixType = new DataTable();
    //endregion

    String[] arrDataList = {
            //"USER",//使用者
            //"SOP_FILE",//SOP檔案
            "FAIL",//故障現象
            "FAIL_REASON",//故障原因
            "FAIL_STRATEGY",//故障處置
            //"PM_FIXT_TOOL",//保養工具
            //"PM_METHOD",//保養方法
            //"PM_CONSUMABLE_LIST",//保養耗材
            "COMPONENTS",//零件
            "COMPONENTS_GROUP",//零件群組
            "STORAGE",//倉庫
            "BIN",//儲位
            "USER_EQP", //人員對應機台
            "SMTL_INVENTORY", //庫存資料
            "CALL_FIX_TYPE" //叫修類別
    };
    List<HashMap<String, Object>> lstDataList = new ArrayList<HashMap<String, Object>>();
    UAdapter uAdapterDataList = null;

    //UI
    ListView lvDataList = null;// 下載資料列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pms_download_basic_data);

        progressDialog = new ProgressDialog(DownloadBasicDataActivity.this);
        offData = new Data(DownloadBasicDataActivity.this);
        lvDataList = findViewById(R.id.lvDataList);

        lvDataList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (PreventButtonMultiClick.isFastDoubleClick()) {
                    // 进行点击事件后的逻辑操作
                    Toast.makeText(DownloadBasicDataActivity.this, getResources().getString(R.string.NOT_ALLOW_MUTI_CLICK), Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    //取得點選的項目
                    HashMap<String, Object> dr = lstDataList.get(position);
                    Intent intent = new Intent(DownloadBasicDataActivity.this, DownloadBRMProccessActivity.class);
                    Bundle bundle = new Bundle();

                    switch (dr.get("ID").toString()) {
                        case "USER":
                            bundle.putString("RequestId", "GetUserInfo");
                            bundle.putString("TableName", "SBRM_USER");
                            bundle.putString("ID_COL", "USER_ID");
                            bundle.putString("NAME_COL", "USER_NAME");
                            bundle.putString("SERIAL_KEY_COL", "USER_SERIAL_KEY");
                            break;

                        case "SOP_FILE":
                            bundle.putString("RequestId", "GetSopFile");
                            bundle.putString("TableName", "SBRM_EMS_SOP");
                            bundle.putString("ID_COL", "SOP_ID");
                            bundle.putString("NAME_COL", "SOP_NAME");
                            bundle.putString("SERIAL_KEY_COL", "SOP_SERIAL_KEY");
                            break;

                        case "FAIL":
                            bundle.putString("RequestId", "GetFail");
                            bundle.putString("TableName", "SBRM_EMS_FAIL");
                            bundle.putString("ID_COL", "FAIL_ID");
                            bundle.putString("NAME_COL", "FAIL_NAME");
                            bundle.putString("SERIAL_KEY_COL", "FAIL_SERIAL_KEY");
                            break;

                        case "FAIL_REASON":
                            bundle.putString("RequestId", "GetFailRsn");
                            bundle.putString("TableName", "SBRM_EMS_FAIL_REASON");
                            bundle.putString("ID_COL", "FAIL_REASON_ID");
                            bundle.putString("NAME_COL", "FAIL_REASON_NAME");
                            bundle.putString("SERIAL_KEY_COL", "FAIL_REASON_SERIAL_KEY");
                            break;

                        case "FAIL_STRATEGY":
                            bundle.putString("RequestId", "GetFailSty");
                            bundle.putString("TableName", "SBRM_EMS_FAIL_STRATEGY");
                            bundle.putString("ID_COL", "FAIL_STRATEGY_ID");
                            bundle.putString("NAME_COL", "FAIL_STRATEGY_NAME");
                            bundle.putString("SERIAL_KEY_COL", "FAIL_STRATEGY_SERIAL_KEY");
                            break;

                        case "PM_FIXT_TOOL":
                            bundle.putString("RequestId", "GetPmTool");
                            bundle.putString("TableName", "SBRM_EMS_FIX_TOOL");
                            bundle.putString("ID_COL", "FIX_TOOL_ID");
                            bundle.putString("NAME_COL", "FIX_TOOL_NAME");
                            bundle.putString("SERIAL_KEY_COL", "FIX_TOOL_SERIAL_KEY");
                            break;

                        case "PM_METHOD":
                            bundle.putString("RequestId", "GetPmMethod");
                            bundle.putString("TableName", "SBRM_EMS_PM_METHOD");
                            bundle.putString("ID_COL", "PM_METHOD_ID");
                            bundle.putString("NAME_COL", "PM_METHOD_NAME");
                            bundle.putString("SERIAL_KEY_COL", "PM_METHOD_SERIAL_KEY");
                            break;

                        case "PM_CONSUMABLE_LIST":
                            bundle.putString("RequestId", "GetPmConsumable");
                            bundle.putString("TableName", "SBRM_EMS_CONSUMABLE_LIST");
                            bundle.putString("ID_COL", "CONSUMABLE_LIST_ID");
                            bundle.putString("NAME_COL", "CONSUMABLE_LIST_NAME");
                            bundle.putString("SERIAL_KEY_COL", "CONSUMABLE_LIST_SERIAL_KEY");
                            break;

                        case "COMPONENTS":
                            bundle.putString("RequestId", "GetPart");
                            bundle.putString("TableName", "SBRM_PART");
                            bundle.putString("ID_COL", "PART_ID");
                            bundle.putString("NAME_COL", "PART_NAME");
                            bundle.putString("SERIAL_KEY_COL", "PART_SERIAL_KEY");
                            break;

                        case "COMPONENTS_GROUP":
                            bundle.putString("RequestId", "GetPartGroup");
                            bundle.putString("TableName", "SBRM_PART_GROUP");
                            bundle.putString("ID_COL", "PART_GROUP_ID");
                            bundle.putString("NAME_COL", "PART_GROUP_NAME");
                            bundle.putString("SERIAL_KEY_COL", "PART_GROUP_SERIAL_KEY");
                            break;

                        case "STORAGE":
                            bundle.putString("RequestId", "GetStorage");
                            bundle.putString("TableName", "SBRM_STORAGE");
                            bundle.putString("ID_COL", "USER_ID");
                            bundle.putString("NAME_COL", "STORAGE_ID");
                            bundle.putString("SERIAL_KEY_COL", "STORAGE_SERIAL_KEY");
                            break;

                        case "BIN":
                            bundle.putString("RequestId", "GetBin");
                            bundle.putString("TableName", "SBRM_BIN");
                            bundle.putString("ID_COL", "BIN_ID");
                            bundle.putString("NAME_COL", "BIN_NAME");
                            bundle.putString("SERIAL_KEY_COL", "BIN_SERIAL_KEY");
                            break;

                        case "USER_EQP":
                            bundle.putString("RequestId", "GetUserEqp");
                            bundle.putString("TableName", "SBRM_EMS_EQP_SUB_ROLE");
                            bundle.putString("ID_COL", "USER_ID");
                            bundle.putString("NAME_COL", "EQP_ID");
                            bundle.putString("SERIAL_KEY_COL", "EQP_SUB_ROLE_SERIAL_KEY");
                            break;

                        case "SMTL_INVENTORY":
                            bundle.putString("RequestId", "GetInvData");
                            bundle.putString("TableName", "SMTL_INVENTORY");
                            bundle.putString("ID_COL", "ITEM_ID");
                            bundle.putString("NAME_COL", "QTY");
                            bundle.putString("SERIAL_KEY_COL", "");
                            break;

                        case "CALL_FIX_TYPE":
                            bundle.putString("RequestId", "GetCallFixType");
                            bundle.putString("TableName", "SBRM_EMS_CALL_FIX_TYPE");
                            bundle.putString("ID_COL", "CALL_FIX_TYPE_ID");
                            bundle.putString("NAME_COL", "CALL_FIX_TYPE_NAME");
                            bundle.putString("SERIAL_KEY_COL", "CALL_FIX_TYPE_SERIAL_KEY");
                            break;
                    }

                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });
        bindDataList();

        Button btnDownloadAll = (Button) findViewById(R.id.btnAllDownload);
        btnDownloadAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PreventButtonMultiClick.isFastDoubleClick()) {
                    Toast.makeText(DownloadBasicDataActivity.this, getResources().getString(R.string.NOT_ALLOW_MUTI_CLICK), Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    boolean isConn = CheckConnect();
                    if (isConn) {
                        //先砍掉全部資料
                        offData.getWritableDatabase().delete("SBRM_USER",null,null);
                        offData.getWritableDatabase().delete("SBRM_EMS_SOP",null,null);
                        offData.getWritableDatabase().delete("SBRM_EMS_FAIL",null,null);
                        offData.getWritableDatabase().delete("SBRM_EMS_FAIL_REASON",null,null);
                        offData.getWritableDatabase().delete("SBRM_EMS_FAIL_STRATEGY",null,null);
                        offData.getWritableDatabase().delete("SBRM_EMS_FIX_TOOL",null,null);
                        offData.getWritableDatabase().delete("SBRM_EMS_PM_METHOD",null,null);
                        offData.getWritableDatabase().delete("SBRM_EMS_CONSUMABLE_LIST",null,null);
                        offData.getWritableDatabase().delete("SBRM_PART",null,null);
                        offData.getWritableDatabase().delete("SBRM_PART_GROUP",null,null);
                        offData.getWritableDatabase().delete("SBRM_STORAGE",null,null);
                        offData.getWritableDatabase().delete("SBRM_BIN",null,null);
                        offData.getWritableDatabase().delete("SBRM_EMS_EQP_SUB_ROLE",null,null);
                        offData.getWritableDatabase().delete("SBRM_EMS_CALL_FIX_TYPE",null,null);

                        android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(DownloadBasicDataActivity.this, 0)
                                .setCancelable(false)
                                .setMessage(getResources().getString(R.string.IS_UPDATE_DATA))
                                .setPositiveButton(getResources().getString(R.string.YES), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        isUpdateInv = true;
                                        offData.getWritableDatabase().delete("SMTL_INVENTORY",null,null);
                                        progressDialog.setMax(arrDataList.length);
                                        progressDialog.show();
                                        DownloadData();
                                    }
                                }).setNegativeButton(getResources().getString(R.string.NO), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        isUpdateInv = false;

                                        progressDialog.setMax(arrDataList.length);
                                        progressDialog.show();
                                        DownloadData();
                                    }
                                });

                        AlertDialog alertDialog = alert.create();
                        alertDialog.show();
                    } else {
                        ShowMessage(strConnFailReason);
                    }
                }
            }
        });
    }

    //r接收thread回傳的訊息，並做處理
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                ShowMessage(getResources().getString(R.string.DATA_DOWNLOAD_SUCCESS));
                progressDialog.dismiss();
            }

            if (msg.what == 3) {
                progressDialog.dismiss();
                ShowMessage(strServerError);
            }
        }
    };

    private boolean DownloadData() {
        BModuleObject biObj1 = new BModuleObject();
        biObj1.setBModuleName("Unicom.Uniworks.BModule.EMS.BIGetBRMDatabyPDA");
        biObj1.setModuleID("GetAllBRMData");
        biObj1.setRequestID("GetAllBRMData");
        biObj1.params = new Vector<ParameterInfo>();

        ParameterInfo updateFlagParam = new ParameterInfo();
        updateFlagParam.setParameterID(BIGetBRMDatabyPDAParam.IsUpdateInv);
        updateFlagParam.setParameterValue(isUpdateInv);
        biObj1.params.add(updateFlagParam);

        CallBIModule(biObj1, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    try {
//                        dtUserInfo = bModuleReturn.getReturnJsonTables().get("GetAllBRMData").get("SBRM_USER");
//                        dtSopFile = bModuleReturn.getReturnJsonTables().get("GetAllBRMData").get("SBRM_EMS_SOP");
                        dtFail = bModuleReturn.getReturnJsonTables().get("GetAllBRMData").get("SBRM_EMS_FAIL");
                        dtFailSty = bModuleReturn.getReturnJsonTables().get("GetAllBRMData").get("SBRM_EMS_FAIL_STRATEGY");
                        dtFailRsn = bModuleReturn.getReturnJsonTables().get("GetAllBRMData").get("SBRM_EMS_FAIL_REASON");
//                        dtPmTool = bModuleReturn.getReturnJsonTables().get("GetAllBRMData").get("SBRM_EMS_FIX_TOOL");
//                        dtPmMethod = bModuleReturn.getReturnJsonTables().get("GetAllBRMData").get("SBRM_EMS_PM_METHOD");
//                        dtPmConsumable = bModuleReturn.getReturnJsonTables().get("GetAllBRMData").get("SBRM_EMS_CONSUMABLE_LIST");
                        dtPartGroup = bModuleReturn.getReturnJsonTables().get("GetAllBRMData").get("SBRM_PART_GROUP");
                        dtPart = bModuleReturn.getReturnJsonTables().get("GetAllBRMData").get("SBRM_PART");
                        dtStorage = bModuleReturn.getReturnJsonTables().get("GetAllBRMData").get("SBRM_STORAGE");
                        dtBin = bModuleReturn.getReturnJsonTables().get("GetAllBRMData").get("SBRM_BIN");
                        dtUserEqp = bModuleReturn.getReturnJsonTables().get("GetAllBRMData").get("SBRM_EMS_EQP_SUB_ROLE");
                        dtCallFixType = bModuleReturn.getReturnJsonTables().get("GetAllBRMData").get("SBRM_EMS_CALL_FIX_TYPE");

                        if(isUpdateInv){
                            dtInv = bModuleReturn.getReturnJsonTables().get("GetAllBRMData").get("SMTL_INVENTORY");
                        }

                        new InertThread().start();
                    } catch (Exception ex) {
                        ShowMessage(ex.getMessage());
                        progressDialog.dismiss();
                    }
                } else {
                    strServerError = "Call BModule Return Error";
                    Message msg = new Message();
                    msg.what = 3;
                    handler.sendMessage(msg);
                }
            }
        });

        return true;
    }

    private class InertThread extends Thread {
        @Override
        public void run() {
            //......处理比较耗时的操作
            Looper.prepare();
            boolean isSuccess = InsertDataToSqlite();

            //处理完成后给handler发送消息
            if (isSuccess) {
                Message msg = new Message();
                msg.what = 1;
                handler.sendMessage(msg);
            } else {
                Message msg = new Message();
                msg.what = 3;
                handler.sendMessage(msg);
            }
        }
    }

    private boolean InsertDataToSqlite() {
        try {
            offData.getWritableDatabase().beginTransaction();

            for (int i = 0; i < dtFail.Rows.size(); i++) {
                ContentValues cValue = new ContentValues();
                for (int j = 0; j < dtFail.getColumns().size(); j++) {
                    cValue.put(dtFail.getColumns().get(j).ColumnName, dtFail.Rows.get(i).getValue(dtFail.getColumns().get(j).ColumnName).toString());
                }

                offData.getWritableDatabase().insert("SBRM_EMS_FAIL", null, cValue);
            }
            progressDialog.incrementProgressBy(1);

            for (int i = 0; i < dtFailSty.Rows.size(); i++) {
                ContentValues cValue = new ContentValues();
                for (int j = 0; j < dtFailSty.getColumns().size(); j++) {
                    cValue.put(dtFailSty.getColumns().get(j).ColumnName, dtFailSty.Rows.get(i).getValue(dtFailSty.getColumns().get(j).ColumnName).toString());
                }

                offData.getWritableDatabase().insert("SBRM_EMS_FAIL_STRATEGY", null, cValue);
            }
            progressDialog.incrementProgressBy(1);

            for (int i = 0; i < dtFailRsn.Rows.size(); i++) {
                ContentValues cValue = new ContentValues();
                for (int j = 0; j < dtFailRsn.getColumns().size(); j++) {
                    cValue.put(dtFailRsn.getColumns().get(j).ColumnName, dtFailRsn.Rows.get(i).getValue(dtFailRsn.getColumns().get(j).ColumnName).toString());
                }

                offData.getWritableDatabase().insert("SBRM_EMS_FAIL_REASON", null, cValue);
            }
            progressDialog.incrementProgressBy(1);

            for (int i = 0; i < dtPartGroup.Rows.size(); i++) {
                ContentValues cValue = new ContentValues();
                for (int j = 0; j < dtPartGroup.getColumns().size(); j++) {
                    cValue.put(dtPartGroup.getColumns().get(j).ColumnName, dtPartGroup.Rows.get(i).getValue(dtPartGroup.getColumns().get(j).ColumnName).toString());
                }

                offData.getWritableDatabase().insert("SBRM_PART_GROUP", null, cValue);
            }
            progressDialog.incrementProgressBy(1);

            for (int i = 0; i < dtPart.Rows.size(); i++) {
                ContentValues cValue = new ContentValues();
                for (int j = 0; j < dtPart.getColumns().size(); j++) {
                    cValue.put(dtPart.getColumns().get(j).ColumnName, dtPart.Rows.get(i).getValue(dtPart.getColumns().get(j).ColumnName).toString());
                }

                offData.getWritableDatabase().insert("SBRM_PART", null, cValue);
            }
            progressDialog.incrementProgressBy(1);

            for (int i = 0; i < dtStorage.Rows.size(); i++) {
                ContentValues cValue = new ContentValues();
                for (int j = 0; j < dtStorage.getColumns().size(); j++) {
                    cValue.put(dtStorage.getColumns().get(j).ColumnName, dtStorage.Rows.get(i).getValue(dtStorage.getColumns().get(j).ColumnName).toString());
                }

                offData.getWritableDatabase().insert("SBRM_STORAGE", null, cValue);
            }
            progressDialog.incrementProgressBy(1);

            for (int i = 0; i < dtBin.Rows.size(); i++) {
                ContentValues cValue = new ContentValues();
                for (int j = 0; j < dtBin.getColumns().size(); j++) {
                    cValue.put(dtBin.getColumns().get(j).ColumnName, dtBin.Rows.get(i).getValue(dtBin.getColumns().get(j).ColumnName).toString());
                }

                offData.getWritableDatabase().insert("SBRM_BIN", null, cValue);
            }
            progressDialog.incrementProgressBy(1);

            for (int i = 0; i < dtUserEqp.Rows.size(); i++) {
                ContentValues cValue = new ContentValues();
                for (int j = 0; j < dtUserEqp.getColumns().size(); j++) {
                    cValue.put(dtUserEqp.getColumns().get(j).ColumnName, dtUserEqp.Rows.get(i).getValue(dtUserEqp.getColumns().get(j).ColumnName).toString());
                }

                offData.getWritableDatabase().insert("SBRM_EMS_EQP_SUB_ROLE", null, cValue);
            }
            progressDialog.incrementProgressBy(1);

            for (int i = 0; i < dtInv.Rows.size(); i++) {
                ContentValues cValue = new ContentValues();
                for (int j = 0; j < dtInv.getColumns().size(); j++) {
                    cValue.put(dtInv.getColumns().get(j).ColumnName, dtInv.Rows.get(i).getValue(dtInv.getColumns().get(j).ColumnName).toString());
                }

                offData.getWritableDatabase().insert("SMTL_INVENTORY", null, cValue);
            }
            progressDialog.incrementProgressBy(1);

            for (int i = 0; i < dtCallFixType.Rows.size(); i++) {
                ContentValues cValue = new ContentValues();
                for (int j = 0; j < dtCallFixType.getColumns().size(); j++) {
                    cValue.put(dtCallFixType.getColumns().get(j).ColumnName, dtCallFixType.Rows.get(i).getValue(dtCallFixType.getColumns().get(j).ColumnName).toString());
                }

                offData.getWritableDatabase().insert("SBRM_EMS_CALL_FIX_TYPE", null, cValue);
            }
            progressDialog.incrementProgressBy(1);

            offData.getWritableDatabase().setTransactionSuccessful();
        } catch (Exception ex) {
            ex.getMessage();
            progressDialog.dismiss();
            return false;
        } finally {
            offData.getWritableDatabase().endTransaction();
            progressDialog.dismiss();
        }

        return true;
    }

    private boolean CheckConnect() {
        boolean checkConn = true;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo == null) {
            strConnFailReason = "網路未連線";
            checkConn = false;
        } else {
            if (networkInfo.isFailover()) {
                strConnFailReason = "網路異常";
                checkConn = false;
            }

            if (!networkInfo.isConnected()) {
                strConnFailReason = "網路未連線";
                checkConn = false;
            }
        }

        return checkConn;
    }

    private void bindDataList() {

        //將array中的資料轉成多國語系，讓使用者好辨識
        for (String data : arrDataList) {
            HashMap<String, Object> dr = new HashMap<String, Object>();
            dr.put("ID", data);
            dr.put("NAME", this.getResString(data));
            lstDataList.add(dr);
        }

        uAdapterDataList = new UAdapter(this, lstDataList, R.layout.listview_download_data, new String[]{"NAME"},
                new int[]{R.id.tvData});
        uAdapterDataList.addAdapterEvent(new UAdapterListener() {

            @Override
            public void onViewRefresh(View view, List<Map<String, ?>> filterData, int position, String[] displayColumns,
                                      int[] viewColumns) {
                // TODO 自動產生的方法 Stub
            }
        });

        lvDataList.setAdapter(uAdapterDataList);
    }
}
