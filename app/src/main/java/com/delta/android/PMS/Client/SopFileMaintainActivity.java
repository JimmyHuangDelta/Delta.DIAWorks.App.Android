package com.delta.android.PMS.Client;

import android.Manifest;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseActivity;
import com.delta.android.Core.Common.CheckPermission;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.PMS.Client.Adapter.SopAdapter;
import com.delta.android.PMS.Common.PreventButtonMultiClick;
import com.delta.android.PMS.Common.UploadUtil;
import com.delta.android.PMS.Param.BIGetEqpAndroidParam;
import com.delta.android.PMS.Param.BIGetSopAndroidParam;
import com.delta.android.R;
import com.google.gson.Gson;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

public class SopFileMaintainActivity extends BaseActivity {

    DataTable dtEqp;
    DataTable dtPm;
    String downloadType;
    ArrayList<String> arExistWo = new ArrayList<>();
    Spinner spEqp;
    Spinner spPm;
    ListView lsSop;
    SopAdapter sopAdapter;
    ArrayList arData = new ArrayList();
    ArrayList<String> arErrorPath = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pms_sop_file_maintain);

        spEqp = (Spinner) findViewById(R.id.spSopEqp);
        spPm = (Spinner) findViewById(R.id.spSopPm);
        lsSop = (ListView) findViewById(R.id.lvSop);

        ImageButton btnQuery = (ImageButton) findViewById(R.id.btnSopQuery);
        btnQuery.setImageResource(R.mipmap.query_wo);
        btnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QuerySopFile();
            }
        });

        Button btnShowLocalFile = (Button) findViewById(R.id.btnSopFileShow);
        btnShowLocalFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(SopFileMaintainActivity.this, SopLocalFileShowActivity.class);
                startActivity(intent);
            }
        });

        Button btnDownloadAll = (Button) findViewById(R.id.btnSopDownloadAll);
        btnDownloadAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    DataTable dtSource = sopAdapter.getDtData();
                    for (int i = 0; i < dtSource.Rows.size(); i++) {
                        dtSource.setValue(i, "is_download", "Y");
                    }

                    sopAdapter = new SopAdapter(getLayoutInflater(), dtSource);
                    lsSop.setAdapter(sopAdapter);
                } catch (Exception ex) {
                    ex.getMessage();
                }
            }
        });

        Button btnUnDownloadAll = (Button) findViewById(R.id.btnSopUnDownloadAll);
        btnUnDownloadAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    DataTable dtSource = sopAdapter.getDtData();
                    for (int i = 0; i < dtSource.Rows.size(); i++) {
                        dtSource.setValue(i, "is_download", "N");
                    }

                    sopAdapter = new SopAdapter(getLayoutInflater(), dtSource);
                    lsSop.setAdapter(sopAdapter);
                } catch (Exception ex) {
                    ex.getMessage();
                }
            }
        });

        Button btnExec = (Button) findViewById(R.id.btnSopExec);
        btnExec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PreventButtonMultiClick.isFastDoubleClick()) {
                    // 进行点击事件后的逻辑操作
                    Toast.makeText(SopFileMaintainActivity.this, getResources().getString(R.string.NOT_ALLOW_MUTI_CLICK), Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    if (sopAdapter == null || sopAdapter.getDownloadCount() <= 0) {
                        ShowMessage(getResources().getString(R.string.EAPE104001));
                        return;
                    }

                    ArrayList<String> arReqPermission = new ArrayList<String>(); //紀錄所需的權限
                    arReqPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    arReqPermission.add(Manifest.permission.READ_EXTERNAL_STORAGE);

                    CheckPermission checkPermission = new CheckPermission(SopFileMaintainActivity.this);
                    if (!checkPermission.CheckPermission(arReqPermission)) { //r檢核是否有缺少開通的權限
                        String[] unPermission = checkPermission.GetUnPermission();
                        if (unPermission != null && unPermission.length > 0) { //如果有缺少要開通的權限，談框詢問是否需要開啟
                            if (Build.VERSION.SDK_INT >= 23) { //建置版本 >23 (android 6.0以上)才需要檢查
                                requestPermissions(unPermission, 0);
                            }
                        }
                    } else {
                        ExecDownload();
                    }
                }
            }
        });
    }

    private void QuerySopFile() {
        String strEqpKey = ((HashMap) spEqp.getSelectedItem()).get("EQP_KEY").toString();
        String strPmKey = ((HashMap) spPm.getSelectedItem()).get("PM_KEY").toString();

        BModuleObject biObjSop = new BModuleObject();
        biObjSop.setBModuleName("Unicom.Uniworks.BModule.EMS.BIGetSopAndroid");
        biObjSop.setModuleID("GetSopFile");
        biObjSop.setRequestID("GetSopFile");

        biObjSop.params = new Vector<ParameterInfo>();

        ParameterInfo eqpParam = new ParameterInfo();
        eqpParam.setParameterID(BIGetSopAndroidParam.EqpKey);
        eqpParam.setParameterValue(strEqpKey);
        biObjSop.params.add(eqpParam);

        ParameterInfo pmParam = new ParameterInfo();
        pmParam.setParameterID(BIGetSopAndroidParam.PMKey);
        pmParam.setParameterValue(strPmKey);
        biObjSop.params.add(pmParam);

        CallBIModule(biObjSop, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                //檢查SOP檔案是否存在本機
                DataTable dtSopFile = bModuleReturn.getReturnJsonTables().get("GetSopFile").get("SBRM_EMS_SOP");
                for (int i = 0; i < dtSopFile.Rows.size(); i++) {
                    //比對檔案是否存在本機，如果不存在要把這筆資料從sqlite內砍掉
                    String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                    final File checkFile = new File(path + getResources().getString(R.string.DEFAULT_LOCAL_SOP_DIR) + dtSopFile.Rows.get(i).get("IDNAME") + "."
                            + dtSopFile.Rows.get(i).get("URL").toString().split("\\.")[dtSopFile.Rows.get(i).get("URL").toString().split("\\.").length - 1]);
                    if (checkFile.exists()) {
                        dtSopFile.Rows.get(i).setValue("is_exist", "Y");
                        dtSopFile.Rows.get(i).setValue("is_download", "N");
                    } else {
                        dtSopFile.Rows.get(i).setValue("is_exist", "N");
                        dtSopFile.Rows.get(i).setValue("is_download", "Y");
                    }
                }

                //BindSop
                try {
                    LayoutInflater layoutInflater = getLayoutInflater();
                    sopAdapter = new SopAdapter(layoutInflater, dtSopFile);
                    lsSop.setAdapter(sopAdapter);
                } catch (Exception ex) {
                    ex.getMessage();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        BindSpinner();
        sopAdapter = null;
        lsSop.setAdapter(sopAdapter);
    }

    //權限檢查後觸發的事件
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) { //等於 - 1代表有不許允開啟的權限。直接return`.
                    return;
                }
            }

            ExecDownload();
        }
    }

    private void ExecDownload() {
        if (sopAdapter == null) {
            ShowMessage(getResources().getString(R.string.PLEASE_QUERY_BEFORE_EXECUTE));
            return;
        }

        new CheckUrlThread().start();
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            try {
                if (msg.what == 1) {
                    for (int i = 0; i < arData.size(); i++) {
                        if (((HashMap) arData.get(i)).get("IS_DOWNLOAD").toString().contentEquals("Y")) {
                            DownloadSopFile(((HashMap) arData.get(i)).get("IDNAME").toString()
                                    // + "." + ((HashMap) arData.get(i)).get("URL").toString().split("\\.")[((HashMap) arData.get(i)).get("URL").toString().split("\\.").length - 1]
                                    , ((HashMap) arData.get(i)).get("URL").toString());
                        }
                    }

                    if (arErrorPath.size() > 0) {
                        ShowMessage(Arrays.toString(arErrorPath.toArray()) + "\n \n \n" + getResources().getString(R.string.FILE_PATH_ERROR));
                        arErrorPath = new ArrayList<>();
                    }

                    //下載完畢，將暫存的資料恢復。
                    QuerySopFile();
                }
            } catch (Exception ex) {
                ex.getMessage();
            }
        }
    };

    private void DownloadSopFile(final String fileName, final String url) {
        try {
            //檢查檔案室否已經存在
            final File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + getResources().getString(R.string.DEFAULT_LOCAL_SOP_DIR) + fileName
                    + "." + url.split("\\.")[url.split("\\.").length - 1]);
            if (dir.exists()) {
                AlertDialog.Builder alert = new AlertDialog.Builder(SopFileMaintainActivity.this, 0)
                        .setTitle(getResources().getString(R.string.FILE_COVER))
                        .setCancelable(false)
                        .setMessage(fileName)
                        .setPositiveButton(getResources().getString(R.string.CONFIRM), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (url.contentEquals(""))
                                    return;

                                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                Uri uri = Uri.parse(url);
                                DownloadManager.Request request = new DownloadManager.Request(uri);
                                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                                request.setAllowedOverRoaming(false);
                                request.setTitle(fileName);

                                dir.delete(); //如果檔案存在要重新下載的話，要砍掉原本的檔案

                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS + getResources().getString(R.string.DEFAULT_LOCAL_SOP_DIR),
                                        fileName + "." + uri.getPath().split("/")[uri.getPath().split("/").length - 1].split("\\.")[1]);
                                dm.enqueue(request);

                            }
                        }).setNegativeButton(getResources().getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });

                AlertDialog alertDialog = alert.create();
                alertDialog.show();
            } else {
                if (url.contentEquals(""))
                    return;

                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                Uri uri = Uri.parse(url);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                request.setAllowedOverRoaming(false);
                request.setTitle(fileName);

                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS + getResources().getString(R.string.DEFAULT_LOCAL_SOP_DIR),
                        fileName + "." + uri.getPath().split("/")[uri.getPath().split("/").length - 1].split("\\.")[1]);
                dm.enqueue(request);

            }
        } catch (Exception ex) {
            Toast.makeText(SopFileMaintainActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
    }

    private class CheckUrlThread extends Thread {
        @Override
        public void run() {
            //......处理比较耗时的操作
            Looper.prepare();
            arData = sopAdapter.GetData();

            for (int i = arData.size() - 1; i >= 0; i--) {
                if (((HashMap) arData.get(i)).get("IS_DOWNLOAD").toString().toUpperCase().contentEquals("Y")) {
                    URL httpurl = null;
                    try {
                        String httpPath = ((HashMap) arData.get(i)).get("URL").toString();
                        httpurl = new URL(new URI(httpPath).toASCIIString());
                        URLConnection urlConnection = httpurl.openConnection();
                        urlConnection.setConnectTimeout(1000); // 1秒
                        // urlConnection.getInputStream();
                        Long TotalSize = Long.parseLong(urlConnection.getHeaderField("Content-Length"));
                        if (TotalSize <= 0) {
                            arErrorPath.add(((HashMap) arData.get(i)).get("IDNAME").toString());
                            arData.remove(i);
                        }
                    } catch (Exception e) {
                        arErrorPath.add(((HashMap) arData.get(i)).get("IDNAME").toString());
                        arData.remove(i);
                    }
                } else {
                    arData.remove(i);
                }
            }

            Message msg = new Message();
            msg.what = 1;
            handler.sendMessage(msg);
        }
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
        paramUser.setParameterValue(SopFileMaintainActivity.this.getGlobal().getUserKey());
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

                    SimpleAdapter eqpAdapter = new SimpleAdapter(SopFileMaintainActivity.this, lsEqp, android.R.layout.simple_list_item_1, new String[]{"EQP_KEY", "IDNAME"}, new int[]{0, android.R.id.text1});
                    SimpleAdapter pmAdapter = new SimpleAdapter(SopFileMaintainActivity.this, lsPm, android.R.layout.simple_list_item_1, new String[]{"PM_KEY", "IDNAME"}, new int[]{0, android.R.id.text1});

                    spEqp.setAdapter(eqpAdapter);
                    spPm.setAdapter(pmAdapter);
                }
            }
        });
    }
}
