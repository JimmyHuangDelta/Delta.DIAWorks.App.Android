package com.delta.android.PMS.Client;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;


import com.delta.android.Core.Activity.BaseActivity;
import com.delta.android.PMS.Common.PreventButtonMultiClick;
import com.delta.android.R;
import com.delta.android.PMS.OffLineData.Data;

public class DownloadBRMProccessActivity extends BaseActivity {

    Data offData;
    Button btnDownload;
    ProgressDialog progressDialog;
    String strConnFailReason; //紀錄連線失敗的錯誤訊息。
    String strServerError; //紀錄call server回傳的錯誤訊息。
    DataTable dtData; //r紀錄BModule return回來的table

    String requestId;
    String strTableName;
    String idCol;
    String nameCol;
    String serialKeyCol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_brm_proccess);
        offData = new Data(DownloadBRMProccessActivity.this);

        Bundle bundle = getIntent().getExtras();
        requestId = bundle.getString("RequestId"); //BModule name
        strTableName = bundle.getString("TableName"); //sqlite table name
        idCol = bundle.getString("ID_COL"); // id_col
        nameCol = bundle.getString("NAME_COL"); // name_col
        serialKeyCol = bundle.getString("SERIAL_KEY_COL"); // serial_key_col

        btnDownload = findViewById(R.id.btnStartDownload);

        BindListData();
        progressDialog = new ProgressDialog(DownloadBRMProccessActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.DATA_DOWNLOAD));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PreventButtonMultiClick.isFastDoubleClick()) {
                    Toast.makeText(DownloadBRMProccessActivity.this, getResources().getString(R.string.NOT_ALLOW_MUTI_CLICK), Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    boolean isConn = CheckConnect();
                    if (isConn) {
                        DownloadData();
                    } else {
                        ShowMessage(strConnFailReason);
                    }
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        DownloadBRMProccessActivity.this.finish();
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

    //r接收thread回傳的訊息，並做處理
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                BindListData();
                ShowMessage(getResources().getString(R.string.DATA_DOWNLOAD_SUCCESS));
                progressDialog.dismiss();
            }

            if (msg.what == 2) {
                progressDialog.incrementProgressBy(1);
            }

            if (msg.what == 3) {
                progressDialog.dismiss();
                ShowMessage(strServerError);
            }
        }
    };

    private boolean DownloadData() {
        offData.getWritableDatabase().delete(strTableName, null, null);
        BModuleObject biObj1 = new BModuleObject();
        biObj1.setBModuleName("Unicom.Uniworks.BModule.EMS.BIGetBRMDatabyPDA");
        biObj1.setModuleID(requestId);
        biObj1.setRequestID(requestId);

        CallBIModule(biObj1, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    try {
                        dtData = bModuleReturn.getReturnJsonTables().get(requestId).get(strTableName);
                        progressDialog.setMax(dtData.Rows.size());
                        progressDialog.show();

                        new InertThread().start();
                    } catch (Exception ex) {
                        ex.getMessage();
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
            boolean isSuccess = InsertDataToSqlite(dtData);

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

    private boolean InsertDataToSqlite(DataTable dtData) {
        try {
            offData.getWritableDatabase().beginTransaction();
            for (int i = 0; i < dtData.Rows.size(); i++) {
                ContentValues cValue = new ContentValues();
                for (int j = 0; j < dtData.getColumns().size(); j++) {
                    cValue.put(dtData.getColumns().get(j).ColumnName, dtData.Rows.get(i).getValue(dtData.getColumns().get(j).ColumnName).toString());
                }

                offData.getWritableDatabase().insert(strTableName, null, cValue);
                Message msg = new Message();
                msg.what = 2;
                handler.sendMessage(msg);
            }

            offData.getWritableDatabase().setTransactionSuccessful();
        } catch (Exception ex) {
            ex.getMessage();
            ShowMessage(ex.getMessage());
            return false;
        } finally {
            offData.getWritableDatabase().endTransaction();
        }

        return true;
    }

    private void BindListData() {
        SQLiteDatabase db = offData.getReadableDatabase();

        if (strTableName.toUpperCase().contentEquals("SMTL_INVENTORY")) {
            Cursor cursor = db.query(strTableName, null, null, null, null, null, null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                ListView lsBRMData = findViewById(R.id.lsBRMData);
                SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                        R.layout.activity_pms_inv_data_listview,
                        cursor,
                        new String[]{"ITEM_ID", "MTL_LOT_ID", "QTY","STORAGE_ID","BIN_ID"},
                        new int[]{R.id.tvInvItemId, R.id.tvInvMtlLotId, R.id.tvInvQty,R.id.tvInvStorage,R.id.tvInvBin},
                        0);
                lsBRMData.setAdapter(adapter);
            }
        } else {
            Cursor cursor = db.query(strTableName, new String[]{nameCol, idCol + " as _id"}, null, null, null, null, null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                ListView lsBRMData = findViewById(R.id.lsBRMData);
                SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                        android.R.layout.simple_list_item_2,
                        cursor,
                        new String[]{nameCol, "_id"},
                        new int[]{android.R.id.text1, android.R.id.text2},
                        0);
                lsBRMData.setAdapter(adapter);
            }
        }
    }
}
