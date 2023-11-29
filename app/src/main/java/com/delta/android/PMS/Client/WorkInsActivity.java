package com.delta.android.PMS.Client;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseActivity;
import com.delta.android.Core.Adapter.UAdapter;
import com.delta.android.Core.Adapter.UAdapterListener;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionary;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionaryList;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.PMS.Client.Fragment.WorkInsFragment;
import com.delta.android.PMS.Common.UploadUtil;
import com.delta.android.PMS.OffLineData.Data;
import com.delta.android.PMS.Param.BWOStartAndroidParam;
import com.delta.android.PMS.Param.BWoUploadAndroidParam;
import com.delta.android.PMS.Param.ParamObj.PartTrxObj;
import com.delta.android.PMS.Param.ParamObj.PmCheckObj;
import com.delta.android.PMS.Param.ParamObj.UploadObj;
import com.delta.android.PMS.Param.ParamObj.UserDataObj;
import com.delta.android.R;
import com.google.gson.Gson;
import com.google.gson.internal.StringMap;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class WorkInsActivity extends BaseActivity {

    String strWoId = "";
    String strEqpId = "";
    private ViewPager pager;
    private TabLayout tabs;
    SimpleDateFormat df;
    private boolean isWork = false; //預設為false ，開始保養後才會改為Y，保養結束後就又會改回N
    Data offData;
    ArrayList<HashMap<String, String>> uploadData = new ArrayList<>(); //紀錄工單要上傳的檔案資訊
    ArrayList uploadRtnData = new ArrayList(); //紀錄上傳後回傳的資訊
    boolean isNeedUpdate = false; //按下返回鍵時是否需要彈框詢問是否儲存，預設為不詢問。

    EditText edEqpId, edEqpName, edPmContentId, edPmContentName, edPmStatus, edPmPlanDate, edPmCmt;
    Button btnStartPm, btnAddCmt;

    //紀錄工單開始作業前的所有資料 (只記錄會有異動的)
    ArrayList<HashMap<String, String>> arTempWo = new ArrayList<>();
    ArrayList<HashMap<String, String>> arTempWoWh = new ArrayList<>();
    ArrayList<HashMap<String, String>> arTempWoPartTrx = new ArrayList<>();
    ArrayList<HashMap<String, String>> arTempWoEqpPart = new ArrayList<>();
    ArrayList<HashMap<String, String>> arTempWoFile = new ArrayList<>();
    ArrayList<HashMap<String, String>> arTempWoCheck = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pms_work_ins);
        pager = (ViewPager) findViewById(R.id.viewpager);
        tabs = (TabLayout) findViewById(R.id.tabs);
        offData = new Data(WorkInsActivity.this);
        df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        tabs.setTabMode(TabLayout.MODE_SCROLLABLE); //由左到右，畫面上看起來會右邊空一塊(適用於Tab數量眾多的時候)
        tabs.setTabGravity(TabLayout.GRAVITY_CENTER); //全部擠在中間
        tabs.setTabTextColors(Color.BLACK, Color.BLACK); //設定字體顏色
        tabs.setupWithViewPager(pager);

        ImageButton btnSelectPage = (ImageButton) findViewById(R.id.ibtnSelectPage);
        btnSelectPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LayoutInflater pageList = LayoutInflater.from(WorkInsActivity.this);
                View vPageList = pageList.inflate(R.layout.activity_pms_listview_dialog, null);
                ListView lsPage = (ListView) vPageList.findViewById(R.id.lsPage);

                //將array中的資料轉成多國語系，讓使用者好辨識
                String[] arPageTitle = {"SOP", "CHECK_ITEM", "EQP_PART", "FILE_ATTACHMENT", "USER_WH", "MODIFY_PART_RECORD"};
                List<HashMap<String, Object>> lstDataList = new ArrayList<HashMap<String, Object>>();
                for (String data : arPageTitle) {
                    HashMap<String, Object> dr = new HashMap<String, Object>();
                    dr.put("ID", data);
                    dr.put("NAME", WorkInsActivity.this.getResString(data));
                    lstDataList.add(dr);
                }

                UAdapter uAdapterDataList = new UAdapter(WorkInsActivity.this, lstDataList, R.layout.listview_download_data, new String[]{"NAME"},
                        new int[]{R.id.tvData});
                uAdapterDataList.addAdapterEvent(new UAdapterListener() {

                    @Override
                    public void onViewRefresh(View view, List<Map<String, ?>> filterData, int position, String[] displayColumns,
                                              int[] viewColumns) {
                        // TODO 自動產生的方法 Stub
                    }
                });

                android.app.AlertDialog.Builder pageDialog = new android.app.AlertDialog.Builder(WorkInsActivity.this);
                pageDialog.setView(vPageList);
                final android.app.AlertDialog page = pageDialog.create();

                lsPage.setAdapter(uAdapterDataList);
                lsPage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        pager.setCurrentItem(position);
                        tabs.getTabAt(position).select();
                        page.dismiss();
                    }
                });

                page.show();
            }
        });

        this.edEqpId = (EditText) findViewById(R.id.edEqpId);
        setEditTextReadOnly(edEqpId);
        this.edEqpName = (EditText) findViewById(R.id.edEqpName);
        setEditTextReadOnly(edEqpName);
        this.edPmContentId = (EditText) findViewById(R.id.edPmContentId);
        setEditTextReadOnly(edPmContentId);
        this.edPmContentName = (EditText) findViewById(R.id.edPmContentName);
        setEditTextReadOnly(edPmContentName);
        this.edPmStatus = (EditText) findViewById(R.id.edPmStatus);
        setEditTextReadOnly(edPmStatus);
        this.edPmPlanDate = (EditText) findViewById(R.id.edPmPlanDate);
        setEditTextReadOnly(edPmPlanDate);
        this.edPmCmt = (EditText) findViewById(R.id.edPmCmt);
        setEditTextReadOnly(edPmCmt);

        this.btnStartPm = (Button) findViewById(R.id.btnStartPm);
        this.btnAddCmt = (Button) findViewById(R.id.btnAddCmt);

        Bundle bundle = getIntent().getExtras();
        strWoId = bundle.getString("WO_ID");
        getSupportActionBar().setTitle(getSupportActionBar().getTitle() + "_" + strWoId);

        GetBeforeUpdateData(); //取得變更前的sqlite資料。

        Cursor csWoInfo = offData.getReadableDatabase().query("SEMS_MRO_WO", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
        csWoInfo.moveToFirst();
        strEqpId = csWoInfo.getString(csWoInfo.getColumnIndex("EQP_ID"));

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = dateFormat.parse(csWoInfo.getString(csWoInfo.getColumnIndex("PLAN_DT")).replace("T", " "));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (csWoInfo.getString(csWoInfo.getColumnIndex("WO_STATUS")).toUpperCase().contentEquals("COMPLETED")) {
            btnStartPm.setText(getResources().getString(R.string.RESTART_OPEN));
        } else {
            btnStartPm.setText(getResources().getString(R.string.START_REPAIR));
        }

        edEqpId.setText(csWoInfo.getString(csWoInfo.getColumnIndex("EQP_ID")));
        edEqpName.setText(csWoInfo.getString(csWoInfo.getColumnIndex("EQP_NAME")));
        edPmContentId.setText(csWoInfo.getString(csWoInfo.getColumnIndex("PM_ID")));
        edPmContentName.setText(csWoInfo.getString(csWoInfo.getColumnIndex("PM_NAME")));
        edPmStatus.setText(csWoInfo.getString(csWoInfo.getColumnIndex("WO_STATUS")));
        edPmPlanDate.setText(String.format("%tF", date));
        edPmCmt.setText(csWoInfo.getString(csWoInfo.getColumnIndex("CMT")));
        btnAddCmt.getBackground().setAlpha(50); //按下開始作業前，無法編輯備註。

        btnAddCmt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isWork)
                    return;

                AlertDialog.Builder builder = new AlertDialog.Builder(WorkInsActivity.this);
                View view = getLayoutInflater().inflate(R.layout.style_pms_dialog_add_cmt, null);

                final EditText edCmt = (EditText) view.findViewById(R.id.edAddComment);
                Cursor csWoInfo = offData.getReadableDatabase().query("SEMS_MRO_WO", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
                csWoInfo.moveToFirst();
                edCmt.setText(csWoInfo.getString(csWoInfo.getColumnIndex("CMT")));

                builder.setView(view);
                builder.setPositiveButton(getResources().getString(R.string.CONFIRM), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ContentValues cvUpdate = new ContentValues();
                        cvUpdate.put("CMT", edCmt.getText().toString());
                        offData.getWritableDatabase().update("SEMS_MRO_WO", cvUpdate, "MRO_WO_ID = ?", new String[]{strWoId});

                        edPmCmt.setText(edCmt.getText().toString());
                    }
                });

                final AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        btnStartPm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //取的當前工單狀態
                SQLiteDatabase queryWo = offData.getReadableDatabase();
                final Cursor csWo = queryWo.query("SEMS_MRO_WO", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
                csWo.moveToFirst();
                String woStatus = csWo.getString(csWo.getColumnIndex("WO_STATUS"));
                String cmt = csWo.getString(csWo.getColumnIndex("CMT"));
                queryWo.close();

                isNeedUpdate = true;

                if (!isWork) { //開工或是重新開工
                    if (woStatus.toUpperCase().contentEquals("WAIT")) {
                        //詢問是否開工，開工後無法透過復原取消開工
                        android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(WorkInsActivity.this);
                        alertDialog.setMessage(getResources().getString(R.string.IS_WORK_START));
                        alertDialog.setPositiveButton(getResources().getString(R.string.YES), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //call BModule 開工
                                boolean isConn = CheckConnection();
                                if (isConn) { //如果當前沒有網路，先update本機的工單狀態，之後完工在一起更新就好(或是透過工單上傳)
                                    BModuleObject bObjUpload = new BModuleObject();
                                    bObjUpload.setBModuleName("Unicom.Uniworks.BModule.EMS.BWOStartAndroid");
                                    bObjUpload.setModuleID("");
                                    bObjUpload.setRequestID("BWOStart");
                                    bObjUpload.params = new Vector<ParameterInfo>();

                                    ParameterInfo paramWoKey = new ParameterInfo();
                                    paramWoKey.setParameterID(BWOStartAndroidParam.WoId);
                                    paramWoKey.setParameterValue(strWoId);
                                    bObjUpload.params.add(paramWoKey);

                                    ParameterInfo paramWoSerialKey = new ParameterInfo();
                                    paramWoSerialKey.setParameterID(BWOStartAndroidParam.WoSerialKey);
                                    paramWoSerialKey.setParameterValue(csWo.getInt(csWo.getColumnIndex("MRO_WO_SERIAL_KEY")));
                                    bObjUpload.params.add(paramWoSerialKey);

                                    CallBModule(bObjUpload, new WebAPIClientEvent() {
                                        @Override
                                        public void onPostBack(BModuleReturn bModuleReturn) {
                                            if (bModuleReturn.getSuccess()) {
                                                btnStartPm.setText(getResources().getString(R.string.COMPLETE));
                                                isWork = true;

                                                ContentValues cvUpdateWoStatus = new ContentValues();
                                                cvUpdateWoStatus.put("IS_CHANGE", "Y");
                                                cvUpdateWoStatus.put("WO_STATUS", "Process");
                                                cvUpdateWoStatus.put("NEED_UPLOAD", "Y");
                                                cvUpdateWoStatus.put("MRO_WO_SERIAL_KEY", csWo.getInt(csWo.getColumnIndex("MRO_WO_SERIAL_KEY")) + 1);
                                                offData.getWritableDatabase().update("SEMS_MRO_WO", cvUpdateWoStatus, "MRO_WO_ID = ?", new String[]{strWoId});

                                                //更新畫面上顯示的工單狀態
                                                Cursor cursor = offData.getReadableDatabase().query("SEMS_MRO_WO", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
                                                cursor.moveToFirst();
                                                edPmStatus.setText(cursor.getString(cursor.getColumnIndex("WO_STATUS")));
                                                btnAddCmt.getBackground().setAlpha(255);
                                                GetBeforeUpdateData(); //更新全部暫存資料
                                                isNeedUpdate = false;
                                                GetInsWoInfo();
                                            } else {
                                                //紀錄工單與錯誤訊息
                                                ShowMessage(bModuleReturn.getAckError().entrySet().iterator().next().getValue().toString());
                                                return;
                                            }
                                        }
                                    });
                                } else {
                                    //先異動本機工單資訊，之後完工時再一起更新或是透過工單上傳更新
                                    btnStartPm.setText(getResources().getString(R.string.COMPLETE));
                                    isWork = true;

                                    ContentValues cvUpdateWoStatus = new ContentValues();
                                    cvUpdateWoStatus.put("IS_CHANGE", "Y");
                                    cvUpdateWoStatus.put("WO_STATUS", "Process");
                                    cvUpdateWoStatus.put("NEED_UPLOAD", "Y");
                                    offData.getWritableDatabase().update("SEMS_MRO_WO", cvUpdateWoStatus, "MRO_WO_ID = ?", new String[]{strWoId});

                                    //更新畫面上顯示的工單狀態
                                    Cursor cursor = offData.getReadableDatabase().query("SEMS_MRO_WO", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
                                    cursor.moveToFirst();
                                    edPmStatus.setText(cursor.getString(cursor.getColumnIndex("WO_STATUS")));

                                    btnAddCmt.getBackground().setAlpha(255);
                                    ShowMessage(getResources().getString(R.string.EAPE107001));
                                    GetInsWoInfo();
                                }
                            }
                        });

                        alertDialog.setNegativeButton(getResources().getString(R.string.NO), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });

                        alertDialog.show();
                    } else {
                        btnStartPm.setText(getResources().getString(R.string.COMPLETE));
                        isWork = true;

                        ContentValues cvUpdateWoStatus = new ContentValues();
                        cvUpdateWoStatus.put("IS_CHANGE", "Y");
                        cvUpdateWoStatus.put("WO_STATUS", "Process");
                        cvUpdateWoStatus.put("NEED_UPLOAD", "Y");
                        offData.getWritableDatabase().update("SEMS_MRO_WO", cvUpdateWoStatus, "MRO_WO_ID = ?", new String[]{strWoId});

                        //更新畫面上顯示的工單狀態
                        Cursor cursor = offData.getReadableDatabase().query("SEMS_MRO_WO", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
                        cursor.moveToFirst();
                        edPmStatus.setText(cursor.getString(cursor.getColumnIndex("WO_STATUS")));
                        btnAddCmt.getBackground().setAlpha(255);
                        GetInsWoInfo();
                    }
                } else { //工單完工，檢查必要資訊是否都有輸入
                    Cursor cursorItem = offData.getReadableDatabase().query("SEMS_PM_WO_CHECK", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);

                    boolean isNeedCreateRepairWo = false;
                    while (cursorItem.moveToNext()) {
                        if (cursorItem.getString(cursorItem.getColumnIndex("CHECK_VALUE")).isEmpty()
                                || cursorItem.getString(cursorItem.getColumnIndex("CHECK_VALUE")) == null
                                || cursorItem.getString(cursorItem.getColumnIndex("CHECK_VALUE")).contentEquals("*")
                                || cursorItem.getString(cursorItem.getColumnIndex("CHECK_RESULT")).isEmpty()
                                || cursorItem.getString(cursorItem.getColumnIndex("CHECK_RESULT")) == null
                                || cursorItem.getString(cursorItem.getColumnIndex("CHECK_RESULT")).contentEquals("*")) {
                            ShowMessage(R.string.EAPE105001, cursorItem.getString(cursorItem.getColumnIndex("CHECK_ID")));
                            return;
                        }

                        if (cursorItem.getString(cursorItem.getColumnIndex("CHECK_VALUE")).toUpperCase().contentEquals("NG")) {
                            isNeedCreateRepairWo = true;
                        }
                    }

                    Cursor cursor = offData.getReadableDatabase().query("SEMS_MRO_WO_WH", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
                    if (cursor.getCount() == 0) {
                        ShowMessage(getResources().getString(R.string.EAPE107002));
                        return;
                    }

                    //取得工單最早開工人員的時間
                    Cursor csWorkStart = offData.getReadableDatabase().query("SEMS_MRO_WO_WH", new String[]{"MIN(START_DT)"}, "MRO_WO_ID = ?", new String[]{strWoId},
                            null, null, null);
                    csWorkStart.moveToFirst();

                    //取得工單最晚完工人員的時間
                    Cursor csWorkEnd = offData.getReadableDatabase().query("SEMS_MRO_WO_WH", new String[]{"MAX(END_DT)"}, "MRO_WO_ID = ?", new String[]{strWoId},
                            null, null, null);
                    csWorkEnd.moveToFirst();

                    DateFormat dfUpdate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    int workMin = 0;
                    try {
                        workMin = (int) ((dfUpdate.parse(csWorkEnd.getString(0).replace("T", " ")).getTime() -
                                dfUpdate.parse(csWorkStart.getString(0).replace("T", " ")).getTime()) / 1000 / 60);
                    } catch (Exception ex) {
                        ShowMessage(ex.getMessage());
                        return;
                    }

                    //彈框輸入維修工時與除外工時
                    LayoutInflater woCompInflat = LayoutInflater.from(WorkInsActivity.this);
                    View woCompView = woCompInflat.inflate(R.layout.style_pms_dialog_repair_hour, null);
                    android.support.v7.app.AlertDialog.Builder woCompBuilder = new android.support.v7.app.AlertDialog.Builder(WorkInsActivity.this);
                    woCompBuilder.setView(woCompView);

                    final EditText edTtlMin = (EditText) woCompView.findViewById(R.id.edTtlHour);
                    edTtlMin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    edTtlMin.setText(Integer.toString(workMin));

                    final EditText edExcMin = (EditText) woCompView.findViewById(R.id.edExcHour);
                    edExcMin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    edExcMin.setText("0");

                    final EditText edCmt = (EditText) woCompView.findViewById(R.id.edCmt);
                    edCmt.setText(cmt);

                    woCompBuilder.setPositiveButton(getResources().getString(R.string.CONFIRM), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int TtlMin = 0;
                            if (!edTtlMin.getText().toString().contentEquals("")) {
                                TtlMin = Integer.parseInt(edTtlMin.getText().toString());
                            }

                            int ExcMin = 0;
                            if (!edExcMin.getText().toString().contentEquals("")) {
                                ExcMin = Integer.parseInt(edExcMin.getText().toString());
                            }

                            if (TtlMin <= 0) {
                                Toast.makeText(WorkInsActivity.this, getResources().getString(R.string.CONFIRM), Toast.LENGTH_LONG).show();/////////////////////////////////////////////////////////
                                return;
                            }

                            isWork = false;
                            GetInsWoInfo();

                            Date nd = new Date(System.currentTimeMillis());
                            String strDate = df.format(nd); //紀錄當前時間

                            ContentValues cvUpdateWoStatus = new ContentValues();
                            cvUpdateWoStatus.put("WO_STATUS", "Completed");
                            cvUpdateWoStatus.put("END_DT", strDate);
                            cvUpdateWoStatus.put("TTL_MAN_HOUR", TtlMin);
                            cvUpdateWoStatus.put("EXC_MAN_HOUR", ExcMin);
                            cvUpdateWoStatus.put("CMT", edCmt.getText().toString());
                            offData.getWritableDatabase().update("SEMS_MRO_WO", cvUpdateWoStatus, "MRO_WO_ID = ?", new String[]{strWoId});

                            //call BModule update 工單狀態與工單相關資訊
                            if (CheckConnection()) {
                                //檢查是否有檢察NG的項目，彈框詢問是否開立維修單
                                Cursor csCheckNg = offData.getReadableDatabase().query("SEMS_PM_WO_CHECK", null, "MRO_WO_ID = ? AND CHECK_RESULT = 'NG'", new String[]{strWoId}, null, null, null);

                                if (csCheckNg.getCount() > 0) {
                                    final android.support.v7.app.AlertDialog.Builder alertCreateRepairWo = new android.support.v7.app.AlertDialog.Builder(WorkInsActivity.this);
                                    alertCreateRepairWo.setMessage(getResources().getString(R.string.EAPE107025));
                                    alertCreateRepairWo.setPositiveButton(getResources().getString(R.string.CONFIRM), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent();
                                            intent.setClass(WorkInsActivity.this, RepairWoCreate.class);
                                            Bundle bundle = new Bundle();
                                            bundle.putString("MRO_WO_ID", strWoId);
                                            bundle.putString("MRO_WO_TYPE", "CHECK");
                                            intent.putExtras(bundle);
                                            startActivity(intent);
                                            WorkInsActivity.this.finish();
                                        }
                                    });

                                    alertCreateRepairWo.setNegativeButton(getResources().getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            boolean uploadResult = UploadWoInfo();
//                                            if (uploadResult) {
//                                                edCmt.setText(edCmt.getText().toString());
//                                                btnAddCmt.getBackground().setAlpha(50);
//                                                isNeedUpdate = false;
//                                                GetBeforeUpdateData();
//                                            } else {
//                                                return;
//                                            }
                                        }
                                    });

                                    alertCreateRepairWo.setCancelable(false);
                                    alertCreateRepairWo.show();
                                } else {
                                    boolean uploadResult = UploadWoInfo();
//                                    if (uploadResult) {
//                                        edCmt.setText(edCmt.getText().toString());
//                                        btnAddCmt.getBackground().setAlpha(50);
//                                        isNeedUpdate = false;
////                                        GetBeforeUpdateData();
//                                    } else {
//                                        return;
//                                    }
                                }
                            } else {
                                Cursor csCheckNg = offData.getReadableDatabase().query("SEMS_PM_WO_CHECK", null, "MRO_WO_ID = ? AND CHECK_RESULT = 'NG'", new String[]{strWoId}, null, null, null);

                                if (csCheckNg.getCount() > 0) {
                                    final android.support.v7.app.AlertDialog.Builder alertCreateRepairWo = new android.support.v7.app.AlertDialog.Builder(WorkInsActivity.this);
                                    alertCreateRepairWo.setMessage(getResources().getString(R.string.EAPE107025));
                                    alertCreateRepairWo.setPositiveButton(getResources().getString(R.string.CONFIRM), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent();
                                            intent.setClass(WorkInsActivity.this, RepairWoCreate.class);
                                            Bundle bundle = new Bundle();
                                            bundle.putString("MRO_WO_ID", strWoId);
                                            bundle.putString("MRO_WO_TYPE", "CHECK");
                                            intent.putExtras(bundle);
                                            startActivity(intent);
                                            WorkInsActivity.this.finish();
                                        }
                                    });

                                    alertCreateRepairWo.setNegativeButton(getResources().getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            btnStartPm.setText(getResources().getString(R.string.RESTART_OPEN));
                                            ShowMessage(getResources().getString(R.string.EAPE107001));
                                            edCmt.setText(edCmt.getText().toString());
                                            btnAddCmt.getBackground().setAlpha(50);
                                            isNeedUpdate = false;
                                        }
                                    });

                                    alertCreateRepairWo.setCancelable(false);
                                    alertCreateRepairWo.show();
                                }
                            }

                        }
                    });

                    woCompBuilder.show();
                }
            }
        });

        GetInsWoInfo();
    }

    //工單完工、暫存
    private boolean UploadWoInfo() {
        try {
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
            } else { //如果沒有要上傳的檔案就直接將所有資訊上傳server
                GetUploadWoData();
            }
        } catch (Exception ex) {
            ShowMessage(ex.getMessage());
            return false;
        }

        return true;
    }

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

        CallBModule(bObjUpload, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (bModuleReturn.getSuccess()) {
                    try {
                        DeleteWo(woId); //刪除上傳成功的工單相關資訊
                        WorkInsActivity.this.finish();
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
        //檢查是否有其他人下載相同的工，如果有工單相關資訊不刪除，只刪除人員對應的工單
//        Cursor csUserWo = offData.getReadableDatabase().query("USER_WO", null, "MRO_WO_ID = ? AND USER_ID <> ?", new String[]{woId, WorkInsActivity.this.getGlobal().getUserID()}, null, null, null);

        try {
//            if (csUserWo.getCount() <= 0) {
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
//            }

            //刪除暫存的轉拋維修單
            offData.getWritableDatabase().delete("TEMP_REPAIR", "MRO_WO_ID = ?", new String[]{woId});

            //刪除人員對應工單紀錄
            offData.getWritableDatabase().delete("USER_WO", "MRO_WO_ID = ?", new String[]{woId});
        } catch (Exception ex) {
            ex.getMessage();
        }
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

    private void GetBeforeUpdateData() {
        Cursor csWo = offData.getReadableDatabase().query("SEMS_MRO_WO", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
        csWo.moveToFirst();
        for (int i = 0; i < csWo.getCount(); i++) {
            HashMap<String, String> woData = new HashMap<>();
            woData.put("MRO_WO_ID", strWoId);
            woData.put("WO_STATUS", csWo.getString(csWo.getColumnIndex("WO_STATUS")));
            woData.put("PLAN_DT", csWo.getString(csWo.getColumnIndex("PLAN_DT")));
            woData.put("START_DT", csWo.getString(csWo.getColumnIndex("START_DT")));
            woData.put("END_DT", csWo.getString(csWo.getColumnIndex("END_DT")));
            woData.put("IS_CHANGE", csWo.getString(csWo.getColumnIndex("IS_CHANGE")));
            woData.put("NEED_UPLOAD", csWo.getString(csWo.getColumnIndex("NEED_UPLOAD")));
            woData.put("CMT", csWo.getString(csWo.getColumnIndex("CMT")));
            woData.put("TTL_MAN_HOUR", csWo.getString(csWo.getColumnIndex("TTL_MAN_HOUR")));
            woData.put("EXC_MAN_HOUR", csWo.getString(csWo.getColumnIndex("EXC_MAN_HOUR")));

            arTempWo.add(woData);
            csWo.moveToNext();
        }

        Cursor csWoWh = offData.getReadableDatabase().query("SEMS_MRO_WO_WH", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
        csWoWh.moveToFirst();
        for (int i = 0; i < csWoWh.getCount(); i++) {
            HashMap<String, String> woData = new HashMap<>();
            woData.put("MRO_WO_ID", strWoId);
            woData.put("USER_ID", csWoWh.getString(csWoWh.getColumnIndex("USER_ID")));
            woData.put("USER_NAME", csWoWh.getString(csWoWh.getColumnIndex("USER_NAME")));
            woData.put("START_DT", csWoWh.getString(csWoWh.getColumnIndex("START_DT")));
            woData.put("END_DT", csWoWh.getString(csWoWh.getColumnIndex("END_DT")));
            woData.put("CMT", csWoWh.getString(csWoWh.getColumnIndex("CMT")));

            arTempWoWh.add(woData);
            csWoWh.moveToNext();
        }

        Cursor csWoPartTrx = offData.getReadableDatabase().query("SEMS_MRO_PART_TRX", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
        csWoPartTrx.moveToFirst();
        for (int i = 0; i < csWoPartTrx.getCount(); i++) {
            HashMap<String, String> woData = new HashMap<>();
            woData.put("MRO_WO_ID", strWoId);
            woData.put("TRX_DATE", csWoPartTrx.getString(csWoPartTrx.getColumnIndex("TRX_DATE")));
            woData.put("PART_ID", csWoPartTrx.getString(csWoPartTrx.getColumnIndex("PART_ID")));
            woData.put("PART_LOT_ID", csWoPartTrx.getString(csWoPartTrx.getColumnIndex("PART_LOT_ID")));
            woData.put("PART_QTY", csWoPartTrx.getString(csWoPartTrx.getColumnIndex("PART_QTY")));
            woData.put("IS_NEW", csWoPartTrx.getString(csWoPartTrx.getColumnIndex("IS_NEW")));
            woData.put("STORAGE_ID", csWoPartTrx.getString(csWoPartTrx.getColumnIndex("STORAGE_ID")));
            woData.put("BIN_ID", csWoPartTrx.getString(csWoPartTrx.getColumnIndex("BIN_ID")));
            woData.put("TRX_MODE", csWoPartTrx.getString(csWoPartTrx.getColumnIndex("TRX_MODE")));
            woData.put("CMT", csWoPartTrx.getString(csWoPartTrx.getColumnIndex("CMT")));

            arTempWoPartTrx.add(woData);
            csWoPartTrx.moveToNext();
        }

        Cursor csWoEqpPart = offData.getReadableDatabase().query("SEMS_EQP_PART", null, "EQP_ID = ?", new String[]{strEqpId}, null, null, null);
        csWoEqpPart.moveToFirst();
        for (int i = 0; i < csWoEqpPart.getCount(); i++) {
            HashMap<String, String> woData = new HashMap<>();
            woData.put("EQP_ID", strEqpId);
            woData.put("PART_ID", csWoEqpPart.getString(csWoEqpPart.getColumnIndex("PART_ID")));
            woData.put("PART_LOT_ID", csWoEqpPart.getString(csWoEqpPart.getColumnIndex("PART_LOT_ID")));
            woData.put("PART_QTY", csWoEqpPart.getString(csWoEqpPart.getColumnIndex("PART_QTY")));
            woData.put("CMT", csWoEqpPart.getString(csWoEqpPart.getColumnIndex("CMT")));

            arTempWoEqpPart.add(woData);
            csWoEqpPart.moveToNext();
        }

        Cursor csWoFile = offData.getReadableDatabase().query("SEMS_FILE", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
        csWoFile.moveToFirst();
        for (int i = 0; i < csWoFile.getCount(); i++) {
            HashMap<String, String> woData = new HashMap<>();
            woData.put("MRO_WO_ID", strWoId);
            woData.put("FILE_NAME", csWoFile.getString(csWoFile.getColumnIndex("FILE_NAME")));
            woData.put("FILE_DESC", csWoFile.getString(csWoFile.getColumnIndex("FILE_DESC")));
            woData.put("LOCAL_FILE_PATH", csWoFile.getString(csWoFile.getColumnIndex("LOCAL_FILE_PATH")));
            woData.put("UPLOAD_USER_ID", csWoFile.getString(csWoFile.getColumnIndex("UPLOAD_USER_ID")));
            woData.put("UPLOAD_DATE", csWoFile.getString(csWoFile.getColumnIndex("UPLOAD_DATE")));
            woData.put("ERROR_MSG", csWoFile.getString(csWoFile.getColumnIndex("ERROR_MSG")));

            arTempWoFile.add(woData);
            csWoFile.moveToNext();
        }

        Cursor csWoCheckItem = offData.getReadableDatabase().query("SEMS_PM_WO_CHECK", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
        csWoCheckItem.moveToFirst();
        for (int i = 0; i < csWoCheckItem.getCount(); i++) {
            HashMap<String, String> woData = new HashMap<>();
            woData.put("MRO_WO_ID", strWoId);
            woData.put("CHECK_ID", csWoCheckItem.getString(csWoCheckItem.getColumnIndex("CHECK_ID")));
            woData.put("CHECK_NAME", csWoCheckItem.getString(csWoCheckItem.getColumnIndex("CHECK_NAME")));
            woData.put("CHECK_TYPE", csWoCheckItem.getString(csWoCheckItem.getColumnIndex("CHECK_TYPE")));
            woData.put("USL", csWoCheckItem.getString(csWoCheckItem.getColumnIndex("USL")));
            woData.put("LSL", csWoCheckItem.getString(csWoCheckItem.getColumnIndex("LSL")));
            woData.put("TARGET", csWoCheckItem.getString(csWoCheckItem.getColumnIndex("TARGET")));
            woData.put("UOM", csWoCheckItem.getString(csWoCheckItem.getColumnIndex("UOM")));
            woData.put("CHECK_VALUE", csWoCheckItem.getString(csWoCheckItem.getColumnIndex("CHECK_VALUE")));
            woData.put("CHECK_RESULT", csWoCheckItem.getString(csWoCheckItem.getColumnIndex("CHECK_RESULT")));
            woData.put("CHECK_USER_KEY", csWoCheckItem.getString(csWoCheckItem.getColumnIndex("CHECK_USER_KEY")));
            woData.put("STD_HOUR", csWoCheckItem.getString(csWoCheckItem.getColumnIndex("STD_HOUR")));
            woData.put("CMT", csWoCheckItem.getString(csWoCheckItem.getColumnIndex("CMT")));

            arTempWoCheck.add(woData);
            csWoCheckItem.moveToNext();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        return super.onOptionsItemSelected(item);
        if (!isNeedUpdate) {
            WorkInsActivity.this.finish();
            return false;
        }

        try {
            android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(WorkInsActivity.this);
            alertDialog.setMessage(getResources().getString(R.string.WHETHER_TO_SAVE_CHANGE));
            alertDialog.setPositiveButton(getResources().getString(R.string.YES), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //call BModule update 工單狀態與工單相關資訊
                    if (CheckConnection()) {
                        //檢查是否有檢察NG的項目，彈框詢問是否開立維修單
                        Cursor csCheckNg = offData.getReadableDatabase().query("SEMS_PM_WO_CHECK", null, "MRO_WO_ID = ? AND CHECK_RESULT = 'NG'", new String[]{strWoId}, null, null, null);

                        if (csCheckNg.getCount() > 0) {
                            final android.support.v7.app.AlertDialog.Builder alertCreateRepairWo = new android.support.v7.app.AlertDialog.Builder(WorkInsActivity.this);
                            alertCreateRepairWo.setMessage(getResources().getString(R.string.EAPE107025));
                            alertCreateRepairWo.setPositiveButton(getResources().getString(R.string.CONFIRM), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent();
                                    intent.setClass(WorkInsActivity.this, RepairWoCreate.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("MRO_WO_ID", strWoId);
                                    bundle.putString("MRO_WO_TYPE", "CHECK");
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                    WorkInsActivity.this.finish();
                                }
                            });

                            alertCreateRepairWo.setNegativeButton(getResources().getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    boolean uploadResult = UploadWoInfo();
//                                    if (uploadResult) {
//                                        Message msg = new Message();
//                                        msg.what = 1;
//                                        handler.sendMessage(msg);
//                                    } else {
//                                        return;
//                                    }
                                }
                            });

                            alertCreateRepairWo.setCancelable(false);
                            alertCreateRepairWo.show();
                        } else {
                            boolean uploadResult = UploadWoInfo();
//                            if (uploadResult) {
//                                Message msg = new Message();
//                                msg.what = 1;
//                                handler.sendMessage(msg);
//                            } else {
//                                return;
//                            }
                        }
                    } else {
                        Cursor csCheckNg = offData.getReadableDatabase().query("SEMS_PM_WO_CHECK", null, "MRO_WO_ID = ? AND CHECK_RESULT = 'NG'", new String[]{strWoId}, null, null, null);

                        if (csCheckNg.getCount() > 0) {
                            final android.support.v7.app.AlertDialog.Builder alertCreateRepairWo = new android.support.v7.app.AlertDialog.Builder(WorkInsActivity.this);
                            alertCreateRepairWo.setMessage(getResources().getString(R.string.EAPE107025));
                            alertCreateRepairWo.setPositiveButton(getResources().getString(R.string.CONFIRM), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent();
                                    intent.setClass(WorkInsActivity.this, RepairWoCreate.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("MRO_WO_ID", strWoId);
                                    bundle.putString("MRO_WO_TYPE", "CHECK");
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                    WorkInsActivity.this.finish();
                                }
                            });

                            alertCreateRepairWo.setNegativeButton(getResources().getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ShowMessage(getResources().getString(R.string.EAPE107001));
                                    Message msg = new Message();
                                    msg.what = 1;
                                    handler.sendMessage(msg);
                                    WorkInsActivity.this.finish();
                                }
                            });

                            alertCreateRepairWo.setCancelable(false);
                            alertCreateRepairWo.show();
                        } else {
                            WorkInsActivity.this.finish();
                        }
                    }
                }
            });

            alertDialog.setNegativeButton(getResources().getString(R.string.NO), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    UndoDataToTemp(); //將工單復原回未作業時的樣子
                    WorkInsActivity.this.finish();
                    return;
                }
            });

            alertDialog.show();
        } catch (Exception ex) {
            ex.getMessage();
        }

        return true;
    }

    //工單相關資訊復原回開工前的樣子。
    private void UndoDataToTemp() {
        for (int i = 0; i < arTempWo.size(); i++) {
            ContentValues cvWo = new ContentValues();
            cvWo.put("WO_STATUS", arTempWo.get(i).get("WO_STATUS"));
            cvWo.put("PLAN_DT", arTempWo.get(i).get("PLAN_DT"));
            cvWo.put("START_DT", arTempWo.get(i).get("START_DT"));
            cvWo.put("END_DT", arTempWo.get(i).get("END_DT"));
            cvWo.put("IS_CHANGE", arTempWo.get(i).get("IS_CHANGE"));
            cvWo.put("NEED_UPLOAD", arTempWo.get(i).get("NEED_UPLOAD"));
            cvWo.put("CMT", arTempWo.get(i).get("CMT"));
            cvWo.put("TTL_MAN_HOUR", arTempWo.get(i).get("TTL_MAN_HOUR"));
            cvWo.put("EXC_MAN_HOUR", arTempWo.get(i).get("EXC_MAN_HOUR"));

            offData.getWritableDatabase().update("SEMS_MRO_WO", cvWo, "MRO_WO_ID = ?", new String[]{strWoId});
        }

        offData.getWritableDatabase().delete("SEMS_MRO_PART_TRX", "MRO_WO_ID = ?", new String[]{strWoId});
        offData.getWritableDatabase().delete("SEMS_MRO_WO_WH", "MRO_WO_ID = ?", new String[]{strWoId});
        offData.getWritableDatabase().delete("SEMS_EQP_PART", "EQP_ID = ?", new String[]{strEqpId});
        offData.getWritableDatabase().delete("SEMS_FILE", "MRO_WO_ID = ?", new String[]{strWoId});
        offData.getWritableDatabase().delete("SEMS_PM_WO_CHECK", "MRO_WO_ID = ?", new String[]{strWoId});

        for (int i = 0; i < arTempWoEqpPart.size(); i++) {
            ContentValues cvInsert = new ContentValues();
            cvInsert.put("EQP_ID", arTempWoEqpPart.get(i).get("EQP_ID"));
            cvInsert.put("PART_ID", arTempWoEqpPart.get(i).get("PART_ID"));
            cvInsert.put("PART_LOT_ID", arTempWoEqpPart.get(i).get("PART_LOT_ID"));
            cvInsert.put("PART_QTY", arTempWoEqpPart.get(i).get("PART_QTY"));
            cvInsert.put("CMT", arTempWoEqpPart.get(i).get("CMT"));

            offData.getWritableDatabase().insert("SEMS_EQP_PART", null, cvInsert);
        }

        for (int i = 0; i < arTempWoPartTrx.size(); i++) {
            ContentValues cvInsert = new ContentValues();
            cvInsert.put("TRX_DATE", arTempWoPartTrx.get(i).get("TRX_DATE"));
            cvInsert.put("MRO_WO_ID", arTempWoPartTrx.get(i).get("MRO_WO_ID"));
            cvInsert.put("PART_ID", arTempWoPartTrx.get(i).get("PART_ID"));
            cvInsert.put("PART_LOT_ID", arTempWoPartTrx.get(i).get("PART_LOT_ID"));
            cvInsert.put("PART_QTY", arTempWoPartTrx.get(i).get("PART_QTY"));
            cvInsert.put("IS_NEW", arTempWoPartTrx.get(i).get("IS_NEW"));
            cvInsert.put("STORAGE_ID", arTempWoPartTrx.get(i).get("STORAGE_ID"));
            cvInsert.put("BIN_ID", arTempWoPartTrx.get(i).get("BIN_ID"));
            cvInsert.put("TRX_MODE", arTempWoPartTrx.get(i).get("TRX_MODE"));
            cvInsert.put("CMT", arTempWoPartTrx.get(i).get("CMT"));

            offData.getWritableDatabase().insert("SEMS_MRO_PART_TRX", null, cvInsert);
        }

        for (int i = 0; i < arTempWoWh.size(); i++) {
            ContentValues cvInsert = new ContentValues();
            cvInsert.put("MRO_WO_ID", arTempWoWh.get(i).get("MRO_WO_ID"));
            cvInsert.put("USER_ID", arTempWoWh.get(i).get("USER_ID"));
            cvInsert.put("USER_NAME", arTempWoWh.get(i).get("USER_NAME"));
            cvInsert.put("START_DT", arTempWoWh.get(i).get("START_DT"));
            cvInsert.put("END_DT", arTempWoWh.get(i).get("END_DT"));
            cvInsert.put("CMT", arTempWoWh.get(i).get("CMT"));

            offData.getWritableDatabase().insert("SEMS_MRO_WO_WH", null, cvInsert);
        }

        for (int i = 0; i < arTempWoFile.size(); i++) {
            ContentValues cvInsert = new ContentValues();
            cvInsert.put("MRO_WO_ID", arTempWoFile.get(i).get("MRO_WO_ID"));
            cvInsert.put("FILE_NAME", arTempWoFile.get(i).get("FILE_NAME"));
            cvInsert.put("FILE_DESC", arTempWoFile.get(i).get("FILE_DESC"));
            cvInsert.put("LOCAL_FILE_PATH", arTempWoFile.get(i).get("LOCAL_FILE_PATH"));
            cvInsert.put("UPLOAD_USER_ID", arTempWoFile.get(i).get("UPLOAD_USER_ID"));
            cvInsert.put("UPLOAD_DATE", arTempWoFile.get(i).get("UPLOAD_DATE"));
            cvInsert.put("ERROR_MSG", arTempWoFile.get(i).get("ERROR_MSG"));

            offData.getWritableDatabase().insert("SEMS_FILE", null, cvInsert);
        }

        for (int i = 0; i < arTempWoCheck.size(); i++) {
            ContentValues cvInsert = new ContentValues();
            cvInsert.put("MRO_WO_ID", arTempWoCheck.get(i).get("MRO_WO_ID"));
            cvInsert.put("CHECK_ID", arTempWoCheck.get(i).get("CHECK_ID"));
            cvInsert.put("CHECK_NAME", arTempWoCheck.get(i).get("CHECK_NAME"));
            cvInsert.put("CHECK_TYPE", arTempWoCheck.get(i).get("CHECK_TYPE"));
            cvInsert.put("USL", arTempWoCheck.get(i).get("USL"));
            cvInsert.put("LSL", arTempWoCheck.get(i).get("LSL"));
            cvInsert.put("TARGET", arTempWoCheck.get(i).get("TARGET"));
            cvInsert.put("UOM", arTempWoCheck.get(i).get("UOM"));
            cvInsert.put("CHECK_VALUE", arTempWoCheck.get(i).get("CHECK_VALUE"));
            cvInsert.put("CHECK_RESULT", arTempWoCheck.get(i).get("CHECK_RESULT"));
            cvInsert.put("CHECK_USER_KEY", arTempWoCheck.get(i).get("CHECK_USER_KEY"));
            cvInsert.put("STD_HOUR", arTempWoCheck.get(i).get("STD_HOUR"));
            cvInsert.put("CMT", arTempWoCheck.get(i).get("CMT"));

            offData.getWritableDatabase().insert("SEMS_PM_WO_CHECK", null, cvInsert);
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if (!isNeedUpdate) {
            WorkInsActivity.this.finish();
            return;
        }

        android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(WorkInsActivity.this);
        alertDialog.setMessage(getResources().getString(R.string.WHETHER_TO_SAVE_CHANGE));
        alertDialog.setPositiveButton(getResources().getString(R.string.YES), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //call BModule update 工單狀態與工單相關資訊
                if (CheckConnection()) {
                    //檢查是否有檢察NG的項目，彈框詢問是否開立維修單
                    Cursor csCheckNg = offData.getReadableDatabase().query("SEMS_PM_WO_CHECK", null, "MRO_WO_ID = ? AND CHECK_RESULT = 'NG'", new String[]{strWoId}, null, null, null);

                    if (csCheckNg.getCount() > 0) {
                        final android.support.v7.app.AlertDialog.Builder alertCreateRepairWo = new android.support.v7.app.AlertDialog.Builder(WorkInsActivity.this);
                        alertCreateRepairWo.setMessage(getResources().getString(R.string.EAPE107025));
                        alertCreateRepairWo.setPositiveButton(getResources().getString(R.string.CONFIRM), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.setClass(WorkInsActivity.this, RepairWoCreate.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("MRO_WO_ID", strWoId);
                                bundle.putString("MRO_WO_TYPE", "CHECK");
                                intent.putExtras(bundle);
                                startActivity(intent);
                                WorkInsActivity.this.finish();
                            }
                        });

                        alertCreateRepairWo.setNegativeButton(getResources().getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean uploadResult = UploadWoInfo();
//                                if (uploadResult) {
//                                    Message msg = new Message();
//                                    msg.what = 1;
//                                    handler.sendMessage(msg);
//                                } else {
//                                    return;
//                                }
                            }
                        });

                        alertCreateRepairWo.setCancelable(false);
                        alertCreateRepairWo.show();
                    } else {
                        boolean uploadResult = UploadWoInfo();
//                        if (uploadResult) {
//                            Message msg = new Message();
//                            msg.what = 1;
//                            handler.sendMessage(msg);
//                        } else {
//                            return;
//                        }
                    }
                } else {
                    Cursor csCheckNg = offData.getReadableDatabase().query("SEMS_PM_WO_CHECK", null, "MRO_WO_ID = ? AND CHECK_RESULT = 'NG'", new String[]{strWoId}, null, null, null);

                    if (csCheckNg.getCount() > 0) {
                        final android.support.v7.app.AlertDialog.Builder alertCreateRepairWo = new android.support.v7.app.AlertDialog.Builder(WorkInsActivity.this);
                        alertCreateRepairWo.setMessage(getResources().getString(R.string.EAPE107025));
                        alertCreateRepairWo.setPositiveButton(getResources().getString(R.string.CONFIRM), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.setClass(WorkInsActivity.this, RepairWoCreate.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("MRO_WO_ID", strWoId);
                                bundle.putString("MRO_WO_TYPE", "CHECK");
                                intent.putExtras(bundle);
                                startActivity(intent);
                                WorkInsActivity.this.finish();
                            }
                        });

                        alertCreateRepairWo.setNegativeButton(getResources().getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ShowMessage(getResources().getString(R.string.EAPE107001));
                                Message msg = new Message();
                                msg.what = 1;
                                handler.sendMessage(msg);
                                WorkInsActivity.this.finish();
                            }
                        });

                        alertCreateRepairWo.setCancelable(false);
                        alertCreateRepairWo.show();
                    } else {
                        WorkInsActivity.this.finish();
                    }
                }
            }
        });

        alertDialog.setNegativeButton(getResources().getString(R.string.NO), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UndoDataToTemp(); //將工單復原回未作業時的樣子
                WorkInsActivity.this.finish();
                return;
            }
        });

        alertDialog.show();
    }

    private void GetInsWoInfo() {
        //Binding 畫面上的資訊
        pager.setAdapter(new WorkFragment(getSupportFragmentManager()));
        pager.getAdapter().notifyDataSetChanged();
    }

    public class WorkFragment extends FragmentPagerAdapter {
        public WorkFragment(FragmentManager fm) {
            super(fm);
        }

        String[] arTitle = {"SOP", "CHECK_ITEM", "EQP_PART", "FILE_UPLOAD", "USER_WH", "PART_TRX"};

        public CharSequence getPageTitle(int position) {
            String title = "";
            switch (position) {
                case 0:
                    title = getResources().getString(R.string.SOP);
                    break;
                case 1:
                    title = getResources().getString(R.string.CHECK_ITEM);
                    break;
                case 2:
                    title = getResources().getString(R.string.EQP_PART);
                    break;
                case 3:
                    title = getResources().getString(R.string.FILE_ATTACHMENT);
                    break;
                case 4:
                    title = getResources().getString(R.string.USER_WH);
                    break;
                case 5:
                    title = getResources().getString(R.string.MODIFY_PART_RECORD);
                    break;
            }

            return title;
        }

        @Override
        public Fragment getItem(int position) {
            return WorkInsFragment.newInstance(arTitle[position], strWoId, strEqpId);
        }

        @Override
        public int getCount() {
            return arTitle.length;
        }
    }

    public boolean GetIsWork() {
        return isWork;
    }

    public void UpdateNeedUpdateFlag(boolean needUpdate) {
        isNeedUpdate = needUpdate;
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
}
