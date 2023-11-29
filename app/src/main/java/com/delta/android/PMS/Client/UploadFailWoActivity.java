package com.delta.android.PMS.Client;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseActivity;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionary;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionaryList;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.PMS.Client.Adapter.FailWoAdapter;
import com.delta.android.PMS.Client.Adapter.WoUploadAdapter;
import com.delta.android.PMS.Common.PreventButtonMultiClick;
import com.delta.android.PMS.Common.UploadUtil;
import com.delta.android.PMS.OffLineData.Data;
import com.delta.android.PMS.Param.BCallRepairAndroidParam;
import com.delta.android.PMS.Param.BWoUploadAndroidParam;
import com.delta.android.PMS.Param.ParamObj.FailReasonObj;
import com.delta.android.PMS.Param.ParamObj.FailStrategyObj;
import com.delta.android.PMS.Param.ParamObj.PartTrxObj;
import com.delta.android.PMS.Param.ParamObj.PmCheckObj;
import com.delta.android.PMS.Param.ParamObj.UploadObj;
import com.delta.android.PMS.Param.ParamObj.UserDataObj;
import com.delta.android.R;
import com.google.gson.Gson;
import com.google.gson.internal.StringMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class UploadFailWoActivity extends BaseActivity {

    ListView lsFailWo;
    ProgressDialog progressDialog;
    int totalCount = 0;
    int nowCount = 0;
    FailWoAdapter adapter;
    Data offData;
    ArrayList<HashMap<String, String>> reUploadWo;
    ArrayList<HashMap<String, String>> errorData; //畫面binding用的
    ArrayList<HashMap<String, String>> uploadErrorData; //真正重新上傳的
    ArrayList<String> errorWoId = new ArrayList<>(); //紀錄重新上傳失敗的工單代碼
    ArrayList uploadRtnData = new ArrayList(); //紀錄上傳成功的檔案相關資訊
    ArrayList<HashMap<String, String>> uploadData = new ArrayList<>(); //紀錄工單要上傳的檔案

    String uploadWoId = ""; //紀錄檔案上傳的工單代碼
    String uploadWoType = ""; //記錄檔案上傳的工單類型

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pms_upload_fail_wo);

        Bundle bundle = getIntent().getExtras();
        errorData = (ArrayList<HashMap<String, String>>) bundle.getSerializable("ERROR_DATA"); //要下載的工單類型
        uploadErrorData = (ArrayList<HashMap<String, String>>) bundle.getSerializable("ERROR_DATA"); //要下載的工單類型

        for (int i = 0; i < errorData.size(); i++) {
            errorData.get(i).put("IS_COVER", "N");
        }

        offData = new Data(UploadFailWoActivity.this);
        lsFailWo = (ListView) findViewById(R.id.lsErrorWo);
        adapter = new FailWoAdapter(getLayoutInflater(), errorData);
        lsFailWo.setAdapter(adapter);

        Button btnExec = (Button) findViewById(R.id.btnExe);
        Button btnAllCover = (Button) findViewById(R.id.btnAllCover);
        Button btnAllIgnore = (Button) findViewById(R.id.btnAllIgnore);

        btnAllCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < errorData.size(); i++) {
                    errorData.get(i).put("IS_COVER", "Y");
                }

                lsFailWo = (ListView) findViewById(R.id.lsErrorWo);
                adapter = new FailWoAdapter(getLayoutInflater(), errorData);
                lsFailWo.setAdapter(adapter);
            }
        });

        btnAllIgnore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < errorData.size(); i++) {
                    errorData.get(i).put("IS_COVER", "N");
                }

                lsFailWo = (ListView) findViewById(R.id.lsErrorWo);
                adapter = new FailWoAdapter(getLayoutInflater(), errorData);
                lsFailWo.setAdapter(adapter);
            }
        });

        btnExec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorData = new ArrayList<>(); //清空畫面上bindign的arrayList ，如果上傳有錯誤會再重新加資料進去
                if (PreventButtonMultiClick.isFastDoubleClick()) {
                    // 进行点击事件后的逻辑操作
                    Toast.makeText(UploadFailWoActivity.this, getResources().getString(R.string.NOT_ALLOW_MUTI_CLICK), Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    if (adapter.getCoverData().size() > 0) {  //如果有要覆蓋的工單
                        reUploadWo = adapter.getCoverData();
                        totalCount = adapter.getCoverData().size();
                        progressDialog.show();
                        progressDialog.setMax(totalCount);
                        GetReUploadData();
                    } else { //忽略全部公單，彈框詢問是否刪除本機所有工單，如果選擇否，有異動的公單依舊存在本機端。
                        final AlertDialog.Builder alert = new AlertDialog.Builder(UploadFailWoActivity.this);
                        alert.setMessage(R.string.EAPE102002);
                        alert.setCancelable(false);
                        alert.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //檢查是否有其他人下載相同的工，如果有工單相關資訊不刪除，只刪除人員對應的工單
                                Cursor csUserWo = offData.getReadableDatabase().query("USER_WO", null, "USER_ID = ?", new String[]{UploadFailWoActivity.this.getGlobal().getUserID()}, null, null, null);
                                csUserWo.moveToFirst();
                                for (int i = 0; i < csUserWo.getCount(); i++) {
                                    //檢查是否有其他人員也下載相同工單，如果有，就不刪除工單資訊，只刪除人員對應工單資訊
                                    Cursor csUserWoUnLogin = offData.getReadableDatabase().query("USER_WO", null,
                                            "MRO_WO_ID = ? AND USER_ID <> ?",
                                            new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID")), UploadFailWoActivity.this.getGlobal().getUserID()},
                                            null, null, null);

                                    if (csUserWoUnLogin.getCount() <= 0) { //沒有其他人也下載這張工單要作業
                                        offData.getWritableDatabase().delete("SEMS_MRO_WO", "MRO_WO_ID = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                        offData.getWritableDatabase().delete("SEMS_FILE", "MRO_WO_ID = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                        offData.getWritableDatabase().delete("SEMS_MRO_WO_WH", "MRO_WO_ID = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                        offData.getWritableDatabase().delete("SEMS_MRO_PART_TRX", "MRO_WO_ID  = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                        offData.getWritableDatabase().delete("SBRM_EMS_CHECK_CONSUMABLE", "MRO_WO_ID  = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                        offData.getWritableDatabase().delete("SBRM_EMS_CHECK_FIX_TOOL", "MRO_WO_ID  = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                        offData.getWritableDatabase().delete("SBRM_EMS_CHECK_METHOD", "MRO_WO_ID  = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                        offData.getWritableDatabase().delete("SBRM_EMS_PM_SOP", "MRO_WO_ID  = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                        offData.getWritableDatabase().delete("SEMS_PM_WO_CHECK", "MRO_WO_ID  = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                        offData.getWritableDatabase().delete("SEMS_REPAIR_WO_STY", "MRO_WO_ID  = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                        offData.getWritableDatabase().delete("SEMS_REPAIR_WO_RSN", "MRO_WO_ID  = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                        offData.getWritableDatabase().delete("SEMS_REPAIR_FAIL", "MRO_WO_ID  = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                        offData.getWritableDatabase().delete("TEMP_REPAIR", "MRO_WO_ID = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                    }

                                    csUserWo.moveToNext();
                                }

                                offData.getWritableDatabase().delete("USER_WO", "USER_ID = ?", new String[]{UploadFailWoActivity.this.getGlobal().getUserID()});
                                UpdateEqpPart(); //更新機台，因為機台身上沒有紀錄工單
                                UploadFailWoActivity.this.finish();
                            }
                        }).setNegativeButton(R.string.NO, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                UploadFailWoActivity.this.finish();
                                return;
                            }
                        });

                        alert.show();
                    }
                }
            }
        });

        progressDialog = new ProgressDialog(UploadFailWoActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.DATA_UPLOAD));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
    }

    //r接收thread回傳的訊息，並做處理
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            try {
                if (msg.what == 1) {
                    progressDialog.dismiss();

                    UpdateEqpPart(); //刪除上傳失敗外的其他公單與相關資訊
                    if (errorData.size() > 0) {
                        uploadErrorData = errorData; //把重新上傳錯誤的檔案指定到 uploadErrorData
                        adapter = new FailWoAdapter(getLayoutInflater(), errorData);
                        lsFailWo.setAdapter(adapter);
                        nowCount = 0;
                        totalCount = errorData.size();
                    } else {
                        final AlertDialog.Builder alert = new AlertDialog.Builder(UploadFailWoActivity.this);
                        alert.setMessage(R.string.EAPE102002);
                        alert.setCancelable(false);
                        alert.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //檢查是否有其他人下載相同的工，如果有工單相關資訊不刪除，只刪除人員對應的工單
                                Cursor csUserWo = offData.getReadableDatabase().query("USER_WO", null, "USER_ID = ?", new String[]{UploadFailWoActivity.this.getGlobal().getUserID()}, null, null, null);
                                csUserWo.moveToFirst();
                                for (int i = 0; i < csUserWo.getCount(); i++) {
                                    //檢查是否有其他人員也下載相同工單，如果有，就不刪除工單資訊，只刪除人員對應工單資訊
                                    Cursor csUserWoUnLogin = offData.getReadableDatabase().query("USER_WO", null,
                                            "MRO_WO_ID = ? AND USER_ID <> ?",
                                            new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID")), UploadFailWoActivity.this.getGlobal().getUserID()},
                                            null, null, null);

                                    if (csUserWoUnLogin.getCount() <= 0) { //沒有其他人也下載這張工單要作業
                                        offData.getWritableDatabase().delete("SEMS_MRO_WO", "MRO_WO_ID = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                        offData.getWritableDatabase().delete("SEMS_FILE", "MRO_WO_ID = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                        offData.getWritableDatabase().delete("SEMS_MRO_WO_WH", "MRO_WO_ID = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                        offData.getWritableDatabase().delete("SEMS_MRO_PART_TRX", "MRO_WO_ID  = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                        offData.getWritableDatabase().delete("SBRM_EMS_CHECK_CONSUMABLE", "MRO_WO_ID  = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                        offData.getWritableDatabase().delete("SBRM_EMS_CHECK_FIX_TOOL", "MRO_WO_ID  = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                        offData.getWritableDatabase().delete("SBRM_EMS_CHECK_METHOD", "MRO_WO_ID  = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                        offData.getWritableDatabase().delete("SBRM_EMS_PM_SOP", "MRO_WO_ID  = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                        offData.getWritableDatabase().delete("SEMS_PM_WO_CHECK", "MRO_WO_ID  = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                        offData.getWritableDatabase().delete("SEMS_REPAIR_WO_STY", "MRO_WO_ID  = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                        offData.getWritableDatabase().delete("SEMS_REPAIR_WO_RSN", "MRO_WO_ID  = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                        offData.getWritableDatabase().delete("SEMS_REPAIR_FAIL", "MRO_WO_ID  = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                        offData.getWritableDatabase().delete("TEMP_REPAIR", "MRO_WO_ID = ?", new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID"))});
                                    }

                                    csUserWo.moveToNext();
                                }

                                offData.getWritableDatabase().delete("USER_WO", "USER_ID = ?", new String[]{UploadFailWoActivity.this.getGlobal().getUserID()});
                                UpdateEqpPart(); //更新機台，因為機台身上沒有紀錄工單
                            }
                        }).setNegativeButton(R.string.NO, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });

                        alert.show();
                    }
                }

                if (msg.what == 2) {
                    progressDialog.incrementProgressBy(1);
                    GetReUploadData();
                }

                if (msg.what == 3) {
                    GetReUploadWoData(); //檔案上傳完畢後，執行工單上傳
                }

                if (msg.what == 4) {
                    HashMap<String, String> error = new HashMap<>();
                    error.put("WO_TYPE", uploadWoType);
                    error.put("WO_ID", uploadWoId);
                    error.put("ERROR_MSG", getResources().getString(R.string.EAPE102003));
                    error.put("IS_COVER", "N");

                    errorData.add(error);
                    errorWoId.add(uploadWoId);
                    nowCount++;

                    uploadWoId = "";
                    uploadWoType = "";

                    if (nowCount == totalCount) {
                        progressDialog.dismiss();

                        uploadErrorData = errorData; //把重新上傳錯誤的檔案指定到 uploadErrorData
                        adapter = new FailWoAdapter(getLayoutInflater(), errorData);
                        lsFailWo.setAdapter(adapter);
                        nowCount = 0;
                        totalCount = errorData.size();
                    } else {
                        progressDialog.incrementProgressBy(1);
                        GetReUploadData();
                    }
                }
            } catch (Exception ex) {
                ex.getMessage();
            }
        }
    };

    private void GetReUploadData() {
        Cursor csWo = offData.getReadableDatabase().query("SEMS_MRO_WO", null, "MRO_WO_ID = ?", new String[]{uploadErrorData.get(nowCount).get("WO_ID")}, null, null, null);
        csWo.moveToFirst();
        String strWoId = csWo.getString(csWo.getColumnIndex("MRO_WO_ID"));
        String strEqpId = csWo.getString(csWo.getColumnIndex("EQP_ID"));
        String strPmId = csWo.getString(csWo.getColumnIndex("PM_ID"));
        uploadData = new ArrayList<>(); //紀錄要上傳的檔案資訊

        //判斷是否有要上傳的檔案
        Cursor csFailUpload = offData.getReadableDatabase().query("SEMS_FILE", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
        if (csFailUpload.getCount() > 0) {
            csFailUpload.moveToFirst();
            for (int i = 0; i < csFailUpload.getCount(); i++) {
                HashMap<String, String> data = new HashMap<>();
                data.put("FILE_NAME", csFailUpload.getString(csFailUpload.getColumnIndex("FILE_NAME")));
                data.put("FILE_DESC", csFailUpload.getString(csFailUpload.getColumnIndex("FILE_DESC")));
                data.put("LOCAL_FILE_PATH", csFailUpload.getString(csFailUpload.getColumnIndex("LOCAL_FILE_PATH")));
                data.put("WO_ID", strWoId);
                data.put("EQP_ID", strEqpId);
                data.put("PM_ID", strPmId);
                uploadData.add(data);

                csFailUpload.moveToNext();
            }

            uploadWoId = csWo.getString(csWo.getColumnIndex("MRO_WO_ID"));
            uploadWoType = csWo.getString(csWo.getColumnIndex("MRO_WO_TYPE"));
            new uploadThread().start(); //先執行檔案上傳，上傳完畢後在直營工單內容上傳。

        } else { //如果沒有要上傳的檔案就直接將所有資訊上傳server
            GetReUploadWoData();
        }
    }

    private void GetReUploadWoData() {
        Cursor csWo = offData.getReadableDatabase().query("SEMS_MRO_WO", null, "MRO_WO_ID = ?", new String[]{uploadErrorData.get(nowCount).get("WO_ID")}, null, null, null);
        csWo.moveToFirst();
        String strWoId = csWo.getString(csWo.getColumnIndex("MRO_WO_ID"));
        String strWoType = csWo.getString(csWo.getColumnIndex("MRO_WO_TYPE"));
        ArrayList<FailReasonObj> arRepairFailRsn = new ArrayList<>(); //紀錄維修的故障原因
        ArrayList<FailStrategyObj> arRepairFailSty = new ArrayList<>(); //紀錄維修的故障處置
        ArrayList<PmCheckObj> arPmCheck = new ArrayList<>(); //紀錄保養的保養資訊

        switch (strWoType.toUpperCase()) {
            case "PM":
            case "INS":
                //regopn 取得保養結果
                Cursor csCheckItem = offData.getReadableDatabase().query("SEMS_PM_WO_CHECK", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
                csCheckItem.moveToFirst();
                for (int j = 0; j < csCheckItem.getCount(); j++) {
                    PmCheckObj item = new PmCheckObj();
                    item.setCheckId(csCheckItem.getString(csCheckItem.getColumnIndex("CHECK_ID")));
                    item.setCheckValue(csCheckItem.getString(csCheckItem.getColumnIndex("CHECK_VALUE")));
                    item.setCheckResult(csCheckItem.getString(csCheckItem.getColumnIndex("CHECK_RESULT")));
                    item.setCheckUserKey(csCheckItem.getString(csCheckItem.getColumnIndex("CHECK_USER_KEY")));
                    item.setCmt(csCheckItem.getString(csCheckItem.getColumnIndex("CMT")));
                    arPmCheck.add(item);

                    csCheckItem.moveToNext();
                }
                //endregion
                break;

            case "REPAIR":
                //regopn 取得保維修故障原因
                Cursor csFailRsn = offData.getReadableDatabase().query("SEMS_REPAIR_WO_RSN", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
                csFailRsn.moveToFirst();
                for (int j = 0; j < csFailRsn.getCount(); j++) {
                    FailReasonObj rsn = new FailReasonObj();
                    rsn.setFailRsnId(csFailRsn.getString(csFailRsn.getColumnIndex("FAIL_REASON_ID")));
                    rsn.setFailRsnCmt(csFailRsn.getString(csFailRsn.getColumnIndex("FAIL_REASON_CMT")));
                    arRepairFailRsn.add(rsn);

                    csFailRsn.moveToNext();
                }
                //endregion

                //regopn 取得維修故障處置
                Cursor csFailSty = offData.getReadableDatabase().query("SEMS_REPAIR_WO_STY", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
                csFailSty.moveToFirst();
                for (int j = 0; j < csFailSty.getCount(); j++) {
                    FailStrategyObj sty = new FailStrategyObj();
                    sty.setFailStyId(csFailSty.getString(csFailSty.getColumnIndex("FAIL_STRATEGY_ID")));
                    sty.setFailStyCmt(csFailSty.getString(csFailSty.getColumnIndex("FAIL_STRATEGY_CMT")));
                    arRepairFailSty.add(sty);

                    csFailSty.moveToNext();
                }
                //endregion
                break;
        }

        //region 取得維修工單資訊
        csWo.moveToFirst();
        HashMap<String, String> woData = new HashMap<>();
        woData.put("MRO_WO_SERIAL_KEY", csWo.getString(csWo.getColumnIndex("MRO_WO_SERIAL_KEY")));
        woData.put("MRO_WO_ID", strWoId);
        woData.put("WO_STATUS", csWo.getString(csWo.getColumnIndex("WO_STATUS")));
        woData.put("START_DT", csWo.getString(csWo.getColumnIndex("START_DT")));
        woData.put("END_DT", csWo.getString(csWo.getColumnIndex("END_DT")));
        woData.put("FAIL_END_DT", csWo.getString(csWo.getColumnIndex("FAIL_END_DT")));
        woData.put("CMT", csWo.getString(csWo.getColumnIndex("CMT")));
        woData.put("TTL_MAN_HOUR", csWo.getString(csWo.getColumnIndex("TTL_MAN_HOUR")));
        woData.put("EXC_MAN_HOUR", csWo.getString(csWo.getColumnIndex("EXC_MAN_HOUR")));
        woData.put("PLAN_END_DATE", csWo.getString(csWo.getColumnIndex("PLAN_END_DT")));
        //endregion

        //region 取得工單機台零件交易紀錄 (只抓出本次的)
        ArrayList<PartTrxObj> arPartTrx = new ArrayList<>();
        Cursor csPartTrx = offData.getReadableDatabase().query("SEMS_MRO_PART_TRX", null, "MRO_WO_ID = ? AND IS_NEW = 'Y'", new String[]{strWoId}, null, null, null);
        csPartTrx.moveToFirst();
        for (int j = 0; j < csPartTrx.getCount(); j++) {
            PartTrxObj partTrx = new PartTrxObj();
            partTrx.setTrxDate(csPartTrx.getString(csPartTrx.getColumnIndex("TRX_DATE")));
            partTrx.setPartId(csPartTrx.getString(csPartTrx.getColumnIndex("PART_ID")));
            partTrx.setPartLotId(csPartTrx.getString(csPartTrx.getColumnIndex("PART_LOT_ID")));
            partTrx.setTrxQty(Integer.parseInt(csPartTrx.getString(csPartTrx.getColumnIndex("PART_QTY"))));
            partTrx.setStorageId(csPartTrx.getString(csPartTrx.getColumnIndex("STORAGE_ID")));
            partTrx.setBinId(csPartTrx.getString(csPartTrx.getColumnIndex("BIN_ID")));
            partTrx.setTrxMode(csPartTrx.getString(csPartTrx.getColumnIndex("TRX_MODE")));
            partTrx.setCmt(csPartTrx.getString(csPartTrx.getColumnIndex("CMT")));
            arPartTrx.add(partTrx);

            csPartTrx.moveToNext();
        }
        //endregion

        //region 取得人員資訊
        ArrayList<UserDataObj> arUser = new ArrayList<>();
        Cursor csRepairUser = offData.getReadableDatabase().query("SEMS_MRO_WO_WH", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
        csRepairUser.moveToFirst();
        for (int j = 0; j < csRepairUser.getCount(); j++) {
            UserDataObj user = new UserDataObj();
            user.setUserId(csRepairUser.getString(csRepairUser.getColumnIndex("USER_ID")));
            user.setStartDt(csRepairUser.getString(csRepairUser.getColumnIndex("START_DT")));
            user.setEndDt(csRepairUser.getString(csRepairUser.getColumnIndex("END_DT")));
            user.setUserCmt(csRepairUser.getString(csRepairUser.getColumnIndex("CMT")));
            arUser.add(user);

            csRepairUser.moveToNext();
        }
        //endregion

        //region 取得上傳成功的檔案
        ArrayList<UploadObj> arUpload = new ArrayList<>();
        for (int i = 0; i < uploadRtnData.size(); i++) { //根據上傳到server的檔案，上傳對應的工單資訊。
            //找出sqlite內檔案紀錄的 檔案描述、上傳人員
            Cursor csUploadInfo = offData.getReadableDatabase().query("SEMS_FILE", new String[]{"FILE_DESC", "UPLOAD_USER_ID"},
                    "MRO_WO_ID = ? AND FILE_NAME = ?",
                    new String[]{strWoId, ((StringMap) uploadRtnData.get(i)).get("FILE_NAME").toString()},
                    null, null, null);
            csUploadInfo.moveToFirst();

            UploadObj uploadObj = new UploadObj();
            uploadObj.setFileGuid(((StringMap) uploadRtnData.get(i)).get("FILE_GUID").toString());
            uploadObj.setFileType(strWoType);
            uploadObj.setUploadDate(((StringMap) uploadRtnData.get(i)).get("UPLOAD_DATE").toString());
            uploadObj.setUploadFileName(((StringMap) uploadRtnData.get(i)).get("FILE_NAME").toString());
            uploadObj.setMroWoId(((StringMap) uploadRtnData.get(i)).get("WO_ID").toString());
            uploadObj.setFilePath(((StringMap) uploadRtnData.get(i)).get("PATH").toString());

            if (csUploadInfo.getCount() > 0) {
                uploadObj.setUploadFileDesc(csUploadInfo.getString(csUploadInfo.getColumnIndex("FILE_DESC")));
                uploadObj.setUploadUserId(csUploadInfo.getString(csUploadInfo.getColumnIndex("UPLOAD_USER_ID")));
            }

            arUpload.add(uploadObj);
        }

        //endregion

        ReUploadWo(strWoId, strWoType, woData, arUser, arPartTrx, arUpload, arRepairFailRsn, arRepairFailSty, arPmCheck);
    }

    private void ReUploadWo(final String strWoId, final String strWoType, HashMap<String, String> woData, ArrayList<UserDataObj> arUser, ArrayList<PartTrxObj> arPartTrx, ArrayList<UploadObj> arUpload, ArrayList<FailReasonObj> arRepairFailRsn, ArrayList<FailStrategyObj> arRepairFailSty, ArrayList<PmCheckObj> arPmCheck) {
        ArrayList<BModuleObject> lsBObj = new ArrayList<>();
        BModuleObject bObjUpload = new BModuleObject();
        bObjUpload.setBModuleName("Unicom.Uniworks.BModule.EMS.BWoUploadAndroid");
        bObjUpload.setModuleID("");
        bObjUpload.setRequestID("WoUploadAndroid");
        bObjUpload.params = new Vector<ParameterInfo>();

        VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create(VirtualClass.VirtualClassType.String);
        MesSerializableDictionary msdl1 = new MesSerializableDictionary(vKey, vVal);
        String serializedString1 = msdl1.generateFinalCode(woData);

        ParameterInfo paramWoType = new ParameterInfo();
        paramWoType.setParameterID(BWoUploadAndroidParam.WoType);
        paramWoType.setParameterValue(strWoType);
        bObjUpload.params.add(paramWoType);

        ParameterInfo paramReUploadFlag = new ParameterInfo();
        paramReUploadFlag.setParameterID(BWoUploadAndroidParam.ReUpload);
        paramReUploadFlag.setParameterValue("Y");
        bObjUpload.params.add(paramReUploadFlag);

        ParameterInfo paramWoData = new ParameterInfo();
        paramWoData.setParameterID(BWoUploadAndroidParam.WoData);
        paramWoData.setParameterValue(serializedString1);
        bObjUpload.params.add(paramWoData);

        VirtualClass vValUser = VirtualClass.create("Unicom.Uniworks.BModule.EMS.Parameter.EmsAndroidObj.AndroidUserDataObj", "bmEMS.Param");
        MesSerializableDictionaryList msdl1User = new MesSerializableDictionaryList(vKey, vValUser);
        HashMap<String, List<?>> mapUser = new HashMap<String, List<?>>();
        mapUser.put("USER", arUser);
        ParameterInfo paramWoUser = new ParameterInfo();
        paramWoUser.setParameterID(BWoUploadAndroidParam.UserData);
        paramWoUser.setNetParameterValue(msdl1User.generateFinalCode(mapUser));
        bObjUpload.params.add(paramWoUser);

        if (arPartTrx.size() > 0) {
            VirtualClass vValPartTrx = VirtualClass.create("Unicom.Uniworks.BModule.EMS.Parameter.EmsAndroidObj.AndroidPartTrxObj", "bmEMS.Param");
            MesSerializableDictionaryList msdl1PartTrx = new MesSerializableDictionaryList(vKey, vValPartTrx);
            HashMap<String, List<?>> mapPartTrx = new HashMap<String, List<?>>();
            mapPartTrx.put("PART_TRX", arPartTrx);
            ParameterInfo paramWoPartTrx = new ParameterInfo();
            paramWoPartTrx.setParameterID(BWoUploadAndroidParam.PartTrx);
            paramWoPartTrx.setNetParameterValue(msdl1PartTrx.generateFinalCode(mapPartTrx));
            bObjUpload.params.add(paramWoPartTrx);
        }

        if (arUpload.size() > 0) {
            VirtualClass vValUpload = VirtualClass.create("Unicom.Uniworks.BModule.EMS.Parameter.EmsAndroidObj.AndroidUploadObj", "bmEMS.Param");
            MesSerializableDictionaryList msdl1Upload = new MesSerializableDictionaryList(vKey, vValUpload);
            HashMap<String, List<?>> mapUpload = new HashMap<String, List<?>>();
            mapUpload.put("UPLOAD", arUpload);
            ParameterInfo paramWoUpload = new ParameterInfo();
            paramWoUpload.setParameterID(BWoUploadAndroidParam.UploadData);
            paramWoUpload.setNetParameterValue(msdl1Upload.generateFinalCode(mapUpload));
            bObjUpload.params.add(paramWoUpload);
        }

        if (arRepairFailRsn.size() > 0) {
            VirtualClass vValFailRsn = VirtualClass.create("Unicom.Uniworks.BModule.EMS.Parameter.EmsAndroidObj.AndroidFailReasonObj", "bmEMS.Param");
            MesSerializableDictionaryList msdl1FailRsn = new MesSerializableDictionaryList(vKey, vValFailRsn);
            HashMap<String, List<?>> mapFailRsn = new HashMap<String, List<?>>();
            mapFailRsn.put("FAIL_RSN", arRepairFailRsn);
            ParameterInfo paramFailRsn = new ParameterInfo();
            paramFailRsn.setParameterID(BWoUploadAndroidParam.FailRsn);
            paramFailRsn.setNetParameterValue(msdl1FailRsn.generateFinalCode(mapFailRsn));
            bObjUpload.params.add(paramFailRsn);
        }

        if (arRepairFailSty.size() > 0) {
            VirtualClass vValFailSty = VirtualClass.create("Unicom.Uniworks.BModule.EMS.Parameter.EmsAndroidObj.AndroidFailStrategyObj", "bmEMS.Param");
            MesSerializableDictionaryList msdl1FailSty = new MesSerializableDictionaryList(vKey, vValFailSty);
            HashMap<String, List<?>> mapFailSty = new HashMap<String, List<?>>();
            mapFailSty.put("FAIL_STY", arRepairFailSty);
            ParameterInfo paramFailSty = new ParameterInfo();
            paramFailSty.setParameterID(BWoUploadAndroidParam.FailSty);
            paramFailSty.setNetParameterValue(msdl1FailSty.generateFinalCode(mapFailSty));
            bObjUpload.params.add(paramFailSty);
        }

        if (arPmCheck.size() > 0) {
            VirtualClass vValPmCheck = VirtualClass.create("Unicom.Uniworks.BModule.EMS.Parameter.EmsAndroidObj.AndroidPmCheckObj", "bmEMS.Param");
            MesSerializableDictionaryList msdl1PmCheck = new MesSerializableDictionaryList(vKey, vValPmCheck);
            HashMap<String, List<?>> mapPmCheck = new HashMap<String, List<?>>();
            mapPmCheck.put("PM_CHECK", arPmCheck);
            ParameterInfo paramPmCheck = new ParameterInfo();
            paramPmCheck.setParameterID(BWoUploadAndroidParam.PmCheck);
            paramPmCheck.setNetParameterValue(msdl1PmCheck.generateFinalCode(mapPmCheck));
            bObjUpload.params.add(paramPmCheck);
        }

        lsBObj.add(bObjUpload);

        //找出這張保養單對應的轉拋維修單資訊
        Cursor csTranSRepair = offData.getReadableDatabase().query("TEMP_REPAIR", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
        csTranSRepair.moveToFirst();

        if (csTranSRepair.getCount() > 0) {
            String strCallFixType = csTranSRepair.getString(csTranSRepair.getColumnIndex("CALL_FIX_TYPE_ID"));
            String strFailDt = csTranSRepair.getString(csTranSRepair.getColumnIndex("FAIL_DT"));
            StringBuilder sbFail = new StringBuilder();

            for (int i = 0; i < csTranSRepair.getCount(); i++) {
                sbFail.append(csTranSRepair.getString(csTranSRepair.getColumnIndex("FAIL_ID")) + "|" + csTranSRepair.getString(csTranSRepair.getColumnIndex("CMT")) + ",");
                csTranSRepair.moveToNext();
            }

            BModuleObject bObjCreateRepair = new BModuleObject();
            bObjCreateRepair.setBModuleName("Unicom.Uniworks.BModule.EMS.BCallRepairAndroid");
            bObjCreateRepair.setModuleID("");
            bObjCreateRepair.setRequestID("BCallRepairAndroid");
            bObjCreateRepair.params = new Vector<ParameterInfo>();

            ParameterInfo paramCreateWoId = new ParameterInfo();
            paramCreateWoId.setParameterID(BCallRepairAndroidParam.WoId);
            paramCreateWoId.setParameterValue(strWoId);
            bObjCreateRepair.params.add(paramCreateWoId);

            ParameterInfo paramCreateCallFixType = new ParameterInfo();
            paramCreateCallFixType.setParameterID(BCallRepairAndroidParam.CallFixTypeId);
            paramCreateCallFixType.setParameterValue(strCallFixType);
            bObjCreateRepair.params.add(paramCreateCallFixType);

            ParameterInfo paramCreateFailDt = new ParameterInfo();
            paramCreateFailDt.setParameterID(BCallRepairAndroidParam.FailDt);
            paramCreateFailDt.setParameterValue(strFailDt);
            bObjCreateRepair.params.add(paramCreateFailDt);

            ParameterInfo paramCreateFailList = new ParameterInfo();
            paramCreateFailList.setParameterID(BCallRepairAndroidParam.FailId);
            paramCreateFailList.setParameterValue(sbFail.toString());
            bObjCreateRepair.params.add(paramCreateFailList);

            lsBObj.add(bObjCreateRepair);
        }


        CallBModule(lsBObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                try {
                    if (bModuleReturn.getSuccess()) {
                        try {
                            DeleteWo(strWoId);
                            nowCount++;

                            if (nowCount == totalCount) {
                                Message msg = new Message();
                                msg.what = 1;
                                handler.sendMessage(msg);
                            } else {
                                Message msg = new Message();
                                msg.what = 2;
                                handler.sendMessage(msg);
                            }
                        } catch (Exception ex) {
                            ex.getMessage();
                        }
                    } else {
                        //紀錄工單與錯誤訊息
                        try {
                            HashMap<String, String> error = new HashMap<>();
                            error.put("WO_TYPE", strWoType);
                            error.put("WO_ID", strWoId);
                            error.put("ERROR_MSG", bModuleReturn.getAckError().entrySet().iterator().next().getValue().toString());
                            error.put("IS_COVER", "N");

                            if (bModuleReturn.getReturnList() != null &&
                                    bModuleReturn.getReturnList().size() > 0 &&
                                    bModuleReturn.getReturnList().get("WoUploadAndroid").get("WO_SERIAL_KEY") != null) {
                                error.put("WO_SERIAL_KEY", bModuleReturn.getReturnList().get("WoUploadAndroid").get("WO_SERIAL_KEY").toString());
                            }

                            errorData.add(error);
                            errorWoId.add(strWoId);

                            nowCount++;
                            if (nowCount == totalCount) {
                                Message msg = new Message();
                                msg.what = 1;
                                handler.sendMessage(msg);
                            } else {
                                Message msg = new Message();
                                msg.what = 2;
                                handler.sendMessage(msg);
                            }
                            new deleteUploadThread().start(); //產生一條新的執行緒，去刪除剛剛上傳的檔案
                        } catch (Exception ex) {
                            ex.getMessage();
                            new deleteUploadThread().start(); //產生一條新的執行緒，去刪除剛剛上傳的檔案
                        }
                    }
                } catch (Exception ex) {
                    ex.getMessage();
                }
            }
        });
    }

    private class uploadThread extends Thread {
        @Override
        public void run() {
            //......处理比较耗时的操作
            Looper.prepare();

            //先把檔案丟到webserver
            if (!getGlobal().get_PortalUrl().contentEquals("")) {
                UploadUtil upload = new UploadUtil(getGlobal());
                String res = upload.uploadFile(getGlobal().get_PortalUrl() + "/UploadFile/UploadFile", uploadData);

                //处理完成后给handler发送消息
                if (!res.toUpperCase().contentEquals("ERROR")) { //代表上傳成功
                    uploadRtnData = ((ArrayList) new Gson().fromJson(res, Object.class));
                    Message msg = new Message();
                    msg.what = 3;
                    handler.sendMessage(msg);
                } else {
                    Message msg = new Message();
                    msg.what = 4;
                    handler.sendMessage(msg);
                }
            } else {
                ShowMessage(getResources().getString(R.string.EAPE102001));
                return;
            }
        }
    }

    private class deleteUploadThread extends Thread {
        @Override
        public void run() {
            //......处理比较耗时的操作
            Looper.prepare();

            //先把檔案丟到webserver
            if (!getGlobal().get_PortalUrl().contentEquals("")) {
                UploadUtil upload = new UploadUtil(getGlobal());
                upload.deleteFile(getGlobal().get_PortalUrl() + "/UploadFile/DeleteUpLoadFile", uploadRtnData); //上傳失敗，刪除上傳的檔案
            } else {
                ShowMessage(getResources().getString(R.string.EAPE102001));
                return;
            }
        }
    }

    private void UpdateEqpPart() {
        StringBuilder sbEqpId = new StringBuilder(); //紀錄不刪除機台零件的機台

        for (int i = 0; i < errorWoId.size(); i++) {
            Cursor csEqp = offData.getReadableDatabase().query("SEMS_MRO_WO", null, "MRO_WO_ID = ?", new String[]{errorWoId.get(i)}, null, null, null);
            csEqp.moveToFirst();
            if (sbEqpId.toString().contentEquals("")) {
                sbEqpId.append("'").append(csEqp.getString(csEqp.getColumnIndex("EQP_ID"))).append("'");
            } else {
                sbEqpId.append(",'").append(errorWoId.get(i)).append("'");
            }
        }

        offData.getWritableDatabase().delete("SEMS_EQP_PART", "EQP_ID NOT IN (" + sbEqpId.toString() + ")", null);
    }

    private void DeleteWo(String woId) {
        //檢查是否有其他人下載相同的工，如果有工單相關資訊不刪除，只刪除人員對應的工單
        Cursor csUserWo = offData.getReadableDatabase().query("USER_WO", null, "MRO_WO_ID = ? AND USER_ID <> ?", new String[]{woId, UploadFailWoActivity.this.getGlobal().getUserID()}, null, null, null);

        try {
            if (csUserWo.getCount() <= 0) {
                //工單基本資訊
                offData.getWritableDatabase().delete("SEMS_MRO_WO", "MRO_WO_ID = ?", new String[]{woId});
                offData.getWritableDatabase().delete("SEMS_FILE", "MRO_WO_ID = ?", new String[]{woId});
                offData.getWritableDatabase().delete("SEMS_MRO_WO_WH", "MRO_WO_ID = ?", new String[]{woId});
                offData.getWritableDatabase().delete("SEMS_MRO_PART_TRX", "MRO_WO_ID = ?", new String[]{woId});

                //保養工單相關
                offData.getWritableDatabase().delete("SBRM_EMS_CHECK_CONSUMABLE", "MRO_WO_ID = ?", new String[]{woId});
                offData.getWritableDatabase().delete("SBRM_EMS_CHECK_FIX_TOOL", "MRO_WO_ID = ?", new String[]{woId});
                offData.getWritableDatabase().delete("SBRM_EMS_CHECK_METHOD", "MRO_WO_ID = ?", new String[]{woId});
                offData.getWritableDatabase().delete("SBRM_EMS_PM_SOP", "MRO_WO_ID = ?", new String[]{woId});
                offData.getWritableDatabase().delete("SEMS_PM_WO_CHECK", "MRO_WO_ID = ?", new String[]{woId});

                //維修工單相關
                offData.getWritableDatabase().delete("SEMS_REPAIR_WO_STY", "MRO_WO_ID = ?", new String[]{woId});
                offData.getWritableDatabase().delete("SEMS_REPAIR_WO_RSN", "MRO_WO_ID = ?", new String[]{woId});
                offData.getWritableDatabase().delete("SEMS_REPAIR_FAIL", "MRO_WO_ID = ?", new String[]{woId});
            }

            //刪除暫存的轉拋維修單
            offData.getWritableDatabase().delete("TEMP_REPAIR", "MRO_WO_ID = ?", new String[]{woId});

            //刪除人員對應工單紀錄
            offData.getWritableDatabase().delete("USER_WO", "MRO_WO_ID = ? AND USER_ID = ?", new String[]{woId, UploadFailWoActivity.this.getGlobal().getUserID()});
        } catch (Exception ex) {
            ex.getMessage();
        }
    }
}
