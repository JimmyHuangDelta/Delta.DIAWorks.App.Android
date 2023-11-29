package com.delta.android.PMS.Client;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.delta.android.Core.Activity.BaseActivity;
import com.delta.android.Core.Activity.ShowMessageEvent;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionary;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionaryList;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.PMS.Client.Adapter.WoUploadAdapter;
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

public class WoUploadActivity extends BaseActivity {

    ArrayList<HashMap<String, String>> arWo;
    Data offData;
    ArrayList<HashMap<String, String>> errorWo = new ArrayList<>(); //紀錄上傳失敗的工單與對應的錯誤訊息
    ProgressDialog progressDialog;
    int totalCount = 0;
    int nowCount = 0;
    Button btnUpload;
    ArrayList<HashMap<String, String>> uploadData = new ArrayList<>(); //紀錄工單要上傳的檔案
    ArrayList<String> errorWoId = new ArrayList<>();
    ArrayList uploadRtnData = new ArrayList(); //紀錄上傳成功的檔案相關資訊

    String uploadWoId = "";
    String uploadWoType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pms_wo_upload);
    }

    private void GetUploadData() { //如果有要上傳的檔案，就先上傳
        if (arWo.get(nowCount).get("IS_UPLOAD").toUpperCase().contentEquals("N")) { //如果公單的flag是不上傳，就不上傳，而且不記錄失敗錯誤訊息
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
        } else {
            String strWoId = arWo.get(nowCount).get("MRO_WO_ID");
            String strEqpId = arWo.get(nowCount).get("EQP_ID");
            String strPmId = arWo.get(nowCount).get("PM_ID");
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

                uploadWoId = arWo.get(nowCount).get("MRO_WO_ID");
                uploadWoType = arWo.get(nowCount).get("MRO_WO_TYPE");
                new uploadThread().start(); //先執行檔案上傳，上傳完畢後在直營工單內容上傳。

            } else { //如果沒有要上傳的檔案就直接將所有資訊上傳server
                GetUploadWoData();
            }
        }
    }

    private void GetUploadWoData() {
        String strWoId = arWo.get(nowCount).get("MRO_WO_ID");
        String strWoType = arWo.get(nowCount).get("MRO_WO_TYPE");
        ArrayList<FailReasonObj> arRepairFailRsn = new ArrayList<>(); //紀錄維修的故障原因
        ArrayList<FailStrategyObj> arRepairFailSty = new ArrayList<>(); //紀錄維修的故障處置
        ArrayList<PmCheckObj> arPmCheck = new ArrayList<>(); //紀錄保養的保養資訊

        switch (strWoType.toUpperCase()) {
            case "PM":
            case "CHECK":
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

        UploadWo(strWoId, strWoType, woData, arUser, arPartTrx, arUpload, arRepairFailRsn, arRepairFailSty, arPmCheck);
    }

    private class uploadThread extends Thread {
        @Override
        public void run() {
            //......处理比较耗时的操作
            Looper.prepare();

            //先把檔案丟到webserver
            UploadUtil upload = new UploadUtil(getGlobal());

            if (!getGlobal().get_PortalUrl().contentEquals("")) {
                String res = upload.uploadFile(getGlobal().get_PortalUrl() + "/UploadFile/UploadFile", uploadData);

                //处理完成后给handler发送消息
                if (!res.toUpperCase().contentEquals("ERROR")) { //代表上傳成功
                    uploadWoId = "";
                    uploadWoType = "";

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
                uploadWoId = "";
                uploadWoType = "";

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

    private void UploadWo(final String woId, final String strWoType,
                          final HashMap<String, String> woData,
                          ArrayList<UserDataObj> arUser,
                          ArrayList<PartTrxObj> arPartTrx,
                          ArrayList<UploadObj> arUpload,
                          ArrayList<FailReasonObj> arRepairFailRsn,
                          ArrayList<FailStrategyObj> arRepairFailSty,
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
        Cursor csTranSRepair = offData.getReadableDatabase().query("TEMP_REPAIR", null, "MRO_WO_ID = ?", new String[]{woId}, null, null, null);
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
            paramCreateWoId.setParameterValue(woId);
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
                if (bModuleReturn.getSuccess()) {
                    try {
                        DeleteWo(woId); //先刪除上傳成功的工單資訊
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
                        errorWoId.add(woId);
                        HashMap<String, String> errorData = new HashMap<>();
                        errorData.put("WO_TYPE", strWoType);
                        errorData.put("WO_ID", woId);
                        errorData.put("ERROR_MSG", bModuleReturn.getAckError().entrySet().iterator().next().getValue().toString());
                        errorWo.add(errorData);

                        nowCount++;
                        if (nowCount == totalCount) {
                            Message msg = new Message();
                            msg.what = 1;
                            msg.obj = errorData;
                            handler.sendMessage(msg);
                        } else {
                            Message msg = new Message();
                            msg.what = 2;
                            msg.obj = errorData;
                            handler.sendMessage(msg);
                        }

                        new deleteUploadThread().start(); //產生一條新的執行緒，去刪除剛剛上傳的檔案
                    } catch (Exception ex) {
                        ex.getMessage();
                        new deleteUploadThread().start(); //產生一條新的執行緒，去刪除剛剛上傳的檔案
                    }
                }
            }
        });
    }

    private void UpdateEqpPart() {
//        StringBuilder sbWoId = new StringBuilder(); //紀錄不刪除公單相關資訊的工單
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

        try {
            offData.getWritableDatabase().delete("SEMS_EQP_PART", "EQP_ID NOT IN  (" + sbEqpId.toString() + ")", null);
        } catch (Exception ex) {
            ex.getMessage();
        }
    }

    private void DeleteWo(String woId) {
        try {
            //檢查是否有其他人下載相同的工，如果有工單相關資訊不刪除，只刪除人員對應的工單
            Cursor csUserWo = offData.getReadableDatabase().query("USER_WO", null, "MRO_WO_ID = ? AND USER_ID <> ?", new String[]{woId, WoUploadActivity.this.getGlobal().getUserID()}, null, null, null);

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

            //刪除轉拋資訊
            offData.getWritableDatabase().delete("TEMP_REPAIR", "MRO_WO_ID = ?", new String[]{woId});

            //刪除人員對應公單紀錄
            offData.getWritableDatabase().delete("USER_WO", "MRO_WO_ID = ? AND USER_ID = ?", new String[]{woId, WoUploadActivity.this.getGlobal().getUserID()});

        } catch (Exception ex) {
            ex.getMessage();
        }
    }

    //r接收thread回傳的訊息，並做處理
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            try {
                if (msg.what == 1) {
                    progressDialog.dismiss();

                    UpdateEqpPart(); //刪除上傳失敗外的其他公單與相關資訊
                    if (errorWo.size() > 0) {
                        Intent intent = new Intent();
                        intent.setClass(WoUploadActivity.this, UploadFailWoActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("ERROR_DATA", errorWo);
                        intent.putExtras(bundle);
                        WoUploadActivity.this.finish();
                        startActivity(intent);
                    } else {
                        final AlertDialog.Builder alert = new AlertDialog.Builder(WoUploadActivity.this);
                        alert.setMessage(R.string.EAPE102002);
                        alert.setCancelable(false);
                        alert.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //檢查是否有其他人下載相同的工，如果有工單相關資訊不刪除，只刪除人員對應的工單
                                Cursor csUserWo = offData.getReadableDatabase().query("USER_WO", null, "USER_ID = ?", new String[]{WoUploadActivity.this.getGlobal().getUserID()}, null, null, null);
                                csUserWo.moveToFirst();
                                for (int i = 0; i < csUserWo.getCount(); i++) {
                                    //檢查是否有其他人員也下載相同工單，如果有，就不刪除工單資訊，只刪除人員對應工單資訊
                                    Cursor csUserWoUnLogin = offData.getReadableDatabase().query("USER_WO", null,
                                            "MRO_WO_ID = ? AND USER_ID <> ?",
                                            new String[]{csUserWo.getString(csUserWo.getColumnIndex("MRO_WO_ID")), WoUploadActivity.this.getGlobal().getUserID()},
                                            null, null, null);
                                    if (csUserWoUnLogin.getCount() <= 0) {
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

                                offData.getWritableDatabase().delete("USER_WO", "USER_ID = ?", new String[]{WoUploadActivity.this.getGlobal().getUserID()});
                                UpdateEqpPart(); //更新機台，因為機台身上沒有紀錄工單
                                onResume();
                            }
                        }).setNegativeButton(R.string.NO, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onResume();
                                return;
                            }
                        });

                        alert.show();
                    }
                }

                if (msg.what == 2) {
                    progressDialog.incrementProgressBy(1);
                    GetUploadData();
                }

                if (msg.what == 3) {
                    GetUploadWoData(); //檔案上傳完畢後，執行工單上傳
                }

                if (msg.what == 4) {
                    errorWoId.add(uploadWoId);
                    HashMap<String, String> errorData = new HashMap<>();
                    errorData.put("WO_TYPE", uploadWoType);
                    errorData.put("WO_ID", uploadWoId);
                    errorData.put("ERROR_MSG", getResources().getString(R.string.EAPE102003));
                    errorWo.add(errorData);
                    nowCount++;

                    if (nowCount == totalCount) {
                        progressDialog.dismiss();
                        Intent intent = new Intent();
                        intent.setClass(WoUploadActivity.this, UploadFailWoActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("ERROR_DATA", errorWo);
                        intent.putExtras(bundle);
                        WoUploadActivity.this.finish();
                        startActivity(intent);
                    } else {
                        progressDialog.incrementProgressBy(1);
                        GetUploadData();
                    }
                }
            } catch (Exception ex) {
                ex.getMessage();
            }
        }
    };

    //設定EditText不可修改
    public static void setEditTextReadOnly(EditText text) {
        text.setTextColor(Color.BLACK);   //設置顏色，使其看起來像只讀模式
        if (text instanceof android.widget.EditText) {
            text.setCursorVisible(false);      //設置光標不可見
            text.setFocusable(false);           //無焦點
            text.setFocusableInTouchMode(false);     //觸摸時也得不到焦點
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        offData = new Data(WoUploadActivity.this);
        arWo = new ArrayList<>();

        EditText edTotal = (EditText) findViewById(R.id.edUploadTotal);
        EditText edPmCount = (EditText) findViewById(R.id.edUploadPm);
        EditText edRepairCount = (EditText) findViewById(R.id.edUploadRepair);
        EditText edInsCount = (EditText) findViewById(R.id.edUploadIns);
        setEditTextReadOnly(edTotal);
        setEditTextReadOnly(edPmCount);
        setEditTextReadOnly(edRepairCount);
        setEditTextReadOnly(edInsCount);

        ListView lsWo = (ListView) findViewById(R.id.lvUploadWo);
        totalCount = 0;
        nowCount = 0;
        int pmCount = 0;
        int repairCount = 0;
        int insCount = 0;

        //取得所有已變更的工單
        String querySql = "SELECT * FROM SEMS_MRO_WO WHERE IS_CHANGE = 'Y' AND MRO_WO_ID IN (SELECT MRO_WO_ID FROM USER_WO WHERE USER_ID =?)";
        Cursor csChangeWo = offData.getReadableDatabase().rawQuery(querySql, new String[]{WoUploadActivity.this.getGlobal().getUserID()});

//        final Cursor csChangeWo = offData.getReadableDatabase().query("SEMS_MRO_WO", null, "IS_CHANGE = 'Y'", null, null, null, null);
        csChangeWo.moveToFirst();
        for (int i = 0; i < csChangeWo.getCount(); i++) {
            HashMap<String, String> data = new HashMap<>();
            data.put("MRO_WO_ID", csChangeWo.getString(csChangeWo.getColumnIndex("MRO_WO_ID")));
            data.put("MRO_WO_TYPE", csChangeWo.getString(csChangeWo.getColumnIndex("MRO_WO_TYPE")));
            data.put("EQP_ID", csChangeWo.getString(csChangeWo.getColumnIndex("EQP_ID")));
            data.put("PLAN_DT", csChangeWo.getString(csChangeWo.getColumnIndex("PLAN_DT")));
            data.put("PM_ID", csChangeWo.getString(csChangeWo.getColumnIndex("PM_ID")));
            data.put("WO_STATUS", csChangeWo.getString(csChangeWo.getColumnIndex("WO_STATUS")));
            data.put("IS_UPLOAD", "Y");
            arWo.add(data);

            if (csChangeWo.getString(csChangeWo.getColumnIndex("MRO_WO_TYPE")).toUpperCase().contentEquals("PM")) {
                pmCount++;
            } else if (csChangeWo.getString(csChangeWo.getColumnIndex("MRO_WO_TYPE")).toUpperCase().contentEquals("REPAIR")) {
                repairCount++;
            } else {
                insCount++;
            }

            totalCount++;
            csChangeWo.moveToNext();
        }

        edTotal.setText(Integer.toString(totalCount));
        edPmCount.setText(Integer.toString(pmCount));
        edRepairCount.setText(Integer.toString(repairCount));
        edInsCount.setText(Integer.toString(insCount));

        btnUpload = (Button) findViewById(R.id.btnWoUpload);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (arWo.size() > 0) {
                    GetUploadData();
                    progressDialog.show();
                }
            }
        });

        WoUploadAdapter adapter = new WoUploadAdapter(getLayoutInflater(), arWo);
        lsWo.setAdapter(adapter);

        progressDialog = new ProgressDialog(WoUploadActivity.this);
        progressDialog.setMessage(getResources().getString(R.string.DATA_UPLOAD));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setMax(totalCount);
    }
}
