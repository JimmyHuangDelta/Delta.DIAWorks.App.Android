package com.delta.android.PMS.Client;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.delta.android.Core.Activity.BaseActivity;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionary;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionaryList;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.PMS.Client.Adapter.FailAdapter;
import com.delta.android.PMS.Common.UploadUtil;
import com.delta.android.PMS.OffLineData.Data;
import com.delta.android.PMS.Param.BCallRepairAndroidParam;
import com.delta.android.PMS.Param.BWoUploadAndroidParam;
import com.delta.android.PMS.Param.ParamObj.PartTrxObj;
import com.delta.android.PMS.Param.ParamObj.PmCheckObj;
import com.delta.android.PMS.Param.ParamObj.UploadObj;
import com.delta.android.PMS.Param.ParamObj.UserDataObj;
import com.delta.android.R;
import com.google.gson.Gson;
import com.google.gson.internal.StringMap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class RepairWoCreate extends BaseActivity {

    String strWoId = "";
    String strEqpId = "";
    Data offData;
    DataTable dtDataFail = new DataTable();
    DataTable dtDataCallFixType = new DataTable();
    EditText edFailDate;
    EditText edFailTime;
    ArrayList arFail = new ArrayList();
    ArrayList arCallType = new ArrayList();
    FailAdapter failAdapter;
    Spinner spCallFixType;
    ListView lsFail;
    ArrayList<HashMap<String, String>> uploadData = new ArrayList<>(); //紀錄工單要上傳的檔案資訊
    ArrayList uploadRtnData = new ArrayList(); //紀錄上傳後回傳的資訊
    String strWoType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pms_repair_wo_create);
        offData = new Data(RepairWoCreate.this);

        Bundle bundle = getIntent().getExtras();
        strWoId = bundle.getString("MRO_WO_ID");
        strWoType = bundle.getString("MRO_WO_TYPE");

        Cursor csWoInfo = offData.getReadableDatabase().query("SEMS_MRO_WO", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
        csWoInfo.moveToFirst();
        strEqpId = csWoInfo.getString(csWoInfo.getColumnIndex("EQP_ID"));

        Button btnConfirm = (Button) findViewById(R.id.btnConfirm);
        lsFail = (ListView) findViewById(R.id.lvFail);
        edFailDate = (EditText) findViewById(R.id.edFailDate);
        edFailTime = (EditText) findViewById(R.id.edFailTime);
        spCallFixType = (Spinner) findViewById(R.id.spCallFixType);

        edFailDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RepairWoCreate.this);
                View view = getLayoutInflater().inflate(R.layout.style_wms_dialog_date_picker_spinner, null);
                final DatePicker datePicker = view.findViewById(R.id.date_picker);

                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(System.currentTimeMillis());
                datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);

                builder.setView(view);
                builder.setTitle(R.string.SELECT_WORK_START_DATE);

                final AlertDialog dialog = builder.create();
                dialog.show();

                Button bConfirm = view.findViewById(R.id.btnConfirm);
                bConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //日期格式
                        StringBuffer sb = new StringBuffer();
                        sb.append(String.format("%d/%02d/%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth()));
                        edFailDate.setText(sb);
                        dialog.cancel();
                    }
                });
            }
        });
        setEditTextReadOnly(edFailDate);

        edFailTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int mHour = c.get(Calendar.HOUR_OF_DAY);
                int mMinute = c.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(RepairWoCreate.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        if (hourOfDay < 10 && minute < 10) {
                            edFailTime.setText("0" + hourOfDay + ":0" + minute);
                        } else if (hourOfDay < 10 && minute >= 10) {
                            edFailTime.setText("0" + hourOfDay + ":" + minute);
                        } else if (hourOfDay >= 10 && minute < 10) {
                            edFailTime.setText(hourOfDay + ":0" + minute);
                        } else {
                            edFailTime.setText(hourOfDay + ":" + minute);
                        }
                    }
                }, mHour, mMinute, true);
                timePickerDialog.show();
            }
        });
        setEditTextReadOnly(edFailTime);

        if (!CheckConnection()) {
            Cursor csFail = offData.getReadableDatabase().query("SBRM_EMS_FAIL", null, null, null, null, null, null);
            csFail.moveToFirst();

            for (int i = 0; i < csFail.getCount(); i++) {
                HashMap<String, String> failData = new HashMap<>();
                failData.put("FAIL_ID", csFail.getString(csFail.getColumnIndex("FAIL_ID")));
                failData.put("FAIL_NAME", csFail.getString(csFail.getColumnIndex("FAIL_NAME")));
                failData.put("CHECKED", "N");
                arFail.add(failData);
                csFail.moveToNext();
            }

            Cursor csCallFixType = offData.getReadableDatabase().query("SBRM_EMS_CALL_FIX_TYPE", null, null, null, null, null, null);
            csCallFixType.moveToFirst();

            for (int i = 0; i < csCallFixType.getCount(); i++) {
                HashMap<String, String> callFixTypeData = new HashMap<>();
                callFixTypeData.put("CALL_FIX_TYPE_ID", csFail.getString(csFail.getColumnIndex("CALL_FIX_TYPE_ID")));
                callFixTypeData.put("CALL_FIX_TYPE_NAME", csFail.getString(csFail.getColumnIndex("CALL_FIX_TYPE_NAME")));
                arCallType.add(callFixTypeData);
                csCallFixType.moveToNext();
            }

            failAdapter = new FailAdapter(getLayoutInflater(), arFail, strWoId);
            lsFail.setAdapter(failAdapter);

            SimpleAdapter callFixTypeAdapter = new SimpleAdapter(RepairWoCreate.this, arCallType, android.R.layout.simple_list_item_1, new String[]{"CALL_FIX_TYPE_ID", "CALL_FIX_TYPE_NAME"}, new int[]{0, android.R.id.text1});
            spCallFixType.setAdapter(callFixTypeAdapter);
        } else {
            GetOnLineData();
        }

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList arFail = failAdapter.getmData();

                //檢查必輸資訊是否有輸入
                if (edFailDate.getText().toString().trim().contentEquals("") || edFailTime.getText().toString().trim().contentEquals("")) {
                    ShowMessage(getResources().getString(R.string.EAPE108001));
                    return;
                }

                if (spCallFixType.getSelectedItem() == null) {
                    ShowMessage(getResources().getString(R.string.EAPE108002));
                    return;
                }

                //檢查是否有勾選故障現象
                if (failAdapter.getCheckCount() <= 0) {
                    ShowMessage(getResources().getString(R.string.EAPE108004));
                    return;
                }

                //如果是點檢工單，並且是online 才直接call server 轉拋。
                if (CheckConnection() && strWoType.toUpperCase().contentEquals("CHECK")) {
                    UploadData();
                } else {
                    //檢查sqlite是否存在相同保養單的暫存資料，如果存在先刪除，在重新新增
                    Cursor csTemp = offData.getReadableDatabase().query("TEMP_REPAIR", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
                    if (csTemp.getCount() > 0) {
                        offData.getWritableDatabase().delete("TEMP_REPAIR", "MRO_WO_ID = ?", new String[]{strWoId});
                    }

                    for (int i = 0; i < arFail.size(); i++) {
                        if (((HashMap) arFail.get(i)).get("CHECKED").toString().contentEquals("Y")) {
//                            //檢查是否有輸入備註
//                            if (((HashMap) arFail.get(i)).get("CMT").toString().trim().contentEquals("")) {
//                                ShowMessage(((HashMap) arFail.get(i)).get("FAIL_ID").toString() + getResources().getString(R.string.EAPE108003));
//                                return;
//                            }

                            HashMap failData = (HashMap) arFail.get(i);
                            ContentValues woValues = new ContentValues();
                            woValues.put("MRO_WO_ID", strWoId);
                            woValues.put("CALL_FIX_TYPE_ID", ((HashMap) spCallFixType.getSelectedItem()).get("CALL_FIX_TYPE_ID").toString());
                            woValues.put("FAIL_DT", edFailDate.getText().toString() + " " + edFailTime.getText().toString());
                            woValues.put("CMT", failData.get("CMT").toString());
                            woValues.put("FAIL_ID", failData.get("FAIL_ID").toString());
                            offData.getWritableDatabase().insert("TEMP_REPAIR", null, woValues);
                        }
                    }

                    RepairWoCreate.this.finish();
                }
            }
        });
    }

    //起一條新的thread 將檔案上傳到指定位置
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
                    msg.what = 1;
                    handler.sendMessage(msg);
                } else {
                    ShowMessage("Upload Error");
                    return;
                }
            } else {
                ShowMessage(getResources().getString(R.string.EAPE102001));
                return;
            }
        }
    }

    //r接收thread回傳的訊息，並做處理
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            try {
                if (msg.what == 1) {
                    GetUploadWoData(); //檔案上傳完畢後，執行工單上傳
                }
            } catch (Exception ex) {
                ex.getMessage();
            }
        }
    };

    private void GetUploadWoData() {
        ArrayList<PmCheckObj> arPmCheck = new ArrayList<>(); //紀錄點檢資訊

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

        //region 取得工單資訊
        Cursor csWo = offData.getReadableDatabase().query("SEMS_MRO_WO", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
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

        String strWoType = csWo.getString(csWo.getColumnIndex("MRO_WO_TYPE"));

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

        UploadWo(strWoId, strWoType, woData, arUser, arPartTrx, arUpload, arPmCheck);
    }

    private void UploadWo(final String woId, final String strWoType,
                          final HashMap<String, String> woData,
                          ArrayList<UserDataObj> arUser,
                          ArrayList<PartTrxObj> arPartTrx,
                          ArrayList<UploadObj> arUpload,
                          ArrayList<PmCheckObj> arPmCheck) {
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
        paramCreateCallFixType.setParameterValue(((HashMap) spCallFixType.getSelectedItem()).get("CALL_FIX_TYPE_ID"));
        bObjCreateRepair.params.add(paramCreateCallFixType);

        ParameterInfo paramCreateFailDt = new ParameterInfo();
        paramCreateFailDt.setParameterID(BCallRepairAndroidParam.FailDt);
        paramCreateFailDt.setParameterValue(edFailDate.getText().toString() + " " + edFailTime.getText().toString());
        bObjCreateRepair.params.add(paramCreateFailDt);

        //找出對應的故障現象
        ArrayList arFail = failAdapter.getmData();
        StringBuilder sbFail = new StringBuilder();

        for (int i = 0; i < arFail.size(); i++) {
            if (((HashMap) arFail.get(i)).get("CHECKED").toString().toUpperCase().contentEquals("Y")) {
                sbFail.append(((HashMap) arFail.get(i)).get("FAIL_ID").toString() + "|" + ((HashMap) arFail.get(i)).get("CMT").toString() + ",");
            }
        }

        ParameterInfo paramCreateFailList = new ParameterInfo();
        paramCreateFailList.setParameterID(BCallRepairAndroidParam.FailId);
        paramCreateFailList.setParameterValue(sbFail.toString());
        bObjCreateRepair.params.add(paramCreateFailList);

        lsBObj.add(bObjCreateRepair);

        CallBModule(lsBObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (bModuleReturn.getSuccess()) {
                    try {
                        DeleteWo(woId); //刪除上傳成功的工單相關資訊
                        RepairWoCreate.this.finish();
                    } catch (Exception ex) {
                        ex.getMessage();
                    }
                } else {
                    //紀錄工單與錯誤訊息
                    try {
                        ShowMessage(bModuleReturn.getAckError().entrySet().iterator().next().getValue().toString());
                        new deleteUploadThread().start(); //產生一條新的執行緒，去刪除剛剛上傳的檔案
                        return;
                    } catch (Exception ex) {
                        ShowMessage(ex.getMessage());
                        new deleteUploadThread().start(); //產生一條新的執行緒，去刪除剛剛上傳的檔案
                        return;
                    }
                }
            }
        });
    }

    private void DeleteWo(String woId) {
        try {
            //工單基本資訊
            offData.getWritableDatabase().delete("SEMS_MRO_WO", "MRO_WO_ID = ?", new String[]{woId});
            offData.getWritableDatabase().delete("SEMS_FILE", "MRO_WO_ID = ?", new String[]{woId});
            offData.getWritableDatabase().delete("SEMS_MRO_WO_WH", "MRO_WO_ID = ?", new String[]{woId});
            offData.getWritableDatabase().delete("SEMS_MRO_PART_TRX", "MRO_WO_ID = ?", new String[]{woId});

            //點檢工單相關
            offData.getWritableDatabase().delete("SBRM_EMS_CHECK_CONSUMABLE", "MRO_WO_ID = ?", new String[]{woId});
            offData.getWritableDatabase().delete("SBRM_EMS_CHECK_FIX_TOOL", "MRO_WO_ID = ?", new String[]{woId});
            offData.getWritableDatabase().delete("SBRM_EMS_CHECK_METHOD", "MRO_WO_ID = ?", new String[]{woId});
            offData.getWritableDatabase().delete("SBRM_EMS_PM_SOP", "MRO_WO_ID = ?", new String[]{woId});
            offData.getWritableDatabase().delete("SEMS_PM_WO_CHECK", "MRO_WO_ID = ?", new String[]{woId});
        } catch (Exception ex) {
            ex.getMessage();
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

    private void UploadData() {
        Cursor csWo = offData.getReadableDatabase().query("SEMS_MRO_WO", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
        csWo.moveToFirst();
        Cursor csFailUpload = offData.getReadableDatabase().query("SEMS_FILE", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
        uploadData = new ArrayList<>(); //紀錄要上傳的檔案資訊
        if (csFailUpload.getCount() > 0) {
            csFailUpload.moveToFirst();
            for (int i = 0; i < csFailUpload.getCount(); i++) {
                HashMap<String, String> data = new HashMap<>();
                String strPmId = csWo.getString(csWo.getColumnIndex("PM_ID"));
                data.put("FILE_NAME", csFailUpload.getString(csFailUpload.getColumnIndex("FILE_NAME")));
                data.put("FILE_DESC", csFailUpload.getString(csFailUpload.getColumnIndex("FILE_DESC")));
                data.put("LOCAL_FILE_PATH", csFailUpload.getString(csFailUpload.getColumnIndex("LOCAL_FILE_PATH")));
                data.put("WO_ID", strWoId);
                data.put("EQP_ID", strEqpId);
                data.put("PM_ID", strPmId);
                uploadData.add(data);

                csFailUpload.moveToNext();
            }

            new uploadThread().start(); //先執行檔案上傳，上傳完畢後在直營工單內容上傳。
        } else {
            GetUploadWoData();
        }
    }

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

    private void GetOnLineData() {
        ArrayList<BModuleObject> lsBObj = new ArrayList<>();
        BModuleObject biObjFail = new BModuleObject();
        biObjFail.setBModuleName("Unicom.Uniworks.BModule.EMS.BIGetBRMDatabyPDA");
        biObjFail.setModuleID("GetFail");
        biObjFail.setRequestID("GetFail");
        lsBObj.add(biObjFail);

        BModuleObject biObjCallFixType = new BModuleObject();
        biObjCallFixType.setBModuleName("Unicom.Uniworks.BModule.EMS.BIGetBRMDatabyPDA");
        biObjCallFixType.setModuleID("GetCallFixType");
        biObjCallFixType.setRequestID("GetCallFixType");
        lsBObj.add(biObjCallFixType);

        CallBIModule(lsBObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    dtDataFail = bModuleReturn.getReturnJsonTables().get("GetFail").get("SBRM_EMS_FAIL");
                    dtDataCallFixType = bModuleReturn.getReturnJsonTables().get("GetCallFixType").get("SBRM_EMS_CALL_FIX_TYPE");

                    for (int i = 0; i < dtDataFail.Rows.size(); i++) {
                        HashMap<String, String> failData = new HashMap<>();
                        failData.put("FAIL_ID", dtDataFail.Rows.get(i).get("FAIL_ID").toString());
                        failData.put("FAIL_NAME", dtDataFail.Rows.get(i).get("FAIL_NAME").toString());
                        failData.put("CMT", "");
                        failData.put("CHECKED", "N");
                        arFail.add(failData);
                    }

                    for (int i = 0; i < dtDataCallFixType.Rows.size(); i++) {
                        HashMap<String, String> callFixType = new HashMap<>();
                        callFixType.put("CALL_FIX_TYPE_ID", dtDataCallFixType.Rows.get(i).get("CALL_FIX_TYPE_ID").toString());
                        callFixType.put("CALL_FIX_TYPE_NAME", dtDataCallFixType.Rows.get(i).get("CALL_FIX_TYPE_NAME").toString());
                        arCallType.add(callFixType);
                    }

                    failAdapter = new FailAdapter(getLayoutInflater(), arFail, strWoId);
                    lsFail.setAdapter(failAdapter);

                    SimpleAdapter callFixTypeAdapter = new SimpleAdapter(RepairWoCreate.this, arCallType, android.R.layout.simple_list_item_1, new String[]{"CALL_FIX_TYPE_ID", "CALL_FIX_TYPE_NAME"}, new int[]{0, android.R.id.text1});
                    spCallFixType.setAdapter(callFixTypeAdapter);
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

    public void onClickDateClear(View v) {
        edFailDate.setText("");
    }

    public void onClickTimeClear(View v) {
        edFailTime.setText("");
    }
}
