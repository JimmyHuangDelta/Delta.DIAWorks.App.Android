package com.delta.android.PMS.Client;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
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

import com.delta.android.Core.Activity.BaseActivity;
import com.delta.android.Core.Adapter.UAdapter;
import com.delta.android.Core.Adapter.UAdapterListener;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.PMS.Client.Fragment.WorkPmFragment;
import com.delta.android.PMS.OffLineData.Data;
import com.delta.android.PMS.Param.BWOStartAndroidParam;
import com.delta.android.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class WorkPmActivity extends BaseActivity {

    String strWoId = "";
    String strEqpId = "";
    private ViewPager pager;
    private TabLayout tabs;
    Data offData;
    private boolean isWork = false; //預設為false ，開始保養後才會改為Y，保養結束後就又會改回N
    boolean isNeedUpdate = false; //按下返回鍵時是否需要彈框詢問是否儲存，預設為不詢問。

    EditText edEqpId, edEqpName, edPmContentId, edPmContentName, edPmStatus, edPmPlanDate, edPmCmt;
    Button btnStartPm, btnAddCmt;
    Data data;
    final String TABLENAME_SEMS_MRO_WO = "SEMS_MRO_WO", TABLENAME_SEMS_MRO_WO_WH = "SEMS_MRO_WO_WH", TABLENAME_SEMS_PM_WO_CHECK = "SEMS_PM_WO_CHECK";
    final String FIELDNAME_EQP_ID = "EQP_ID", FIELDNAME_EQP_NAME = "EQP_NAME", FIELDNAME_WO_STATUS = "WO_STATUS", FIELDNAME_PLAN_DT = "PLAN_DT", FIELDNAME_PM_ID = "PM_ID", FIELDNAME_PM_NAME = "PM_NAME", FIELDNAME_CMT = "CMT", FIELDNAME_TTL_MAN_HOUR = "TTL_MAN_HOUR", FIELDNAME_EXC_MAN_HOUR = "EXC_MAN_HOUR", FIELDNAME_IS_CHANGE = "IS_CHANGE";
    final String FIELDNAME_CHECK_ID = "CHECK_ID", FIELDNAME_CHECK_VALUE = "CHECK_VALUE", FIELDNAME_CHECK_RESULT = "CHECK_RESULT";
    final String FIELDNAME_START_DT = "START_DT", FIELDNAME_END_DT = "END_DT";
    final String STATUS_WAIT = "Wait", STATUS_PROCESS = "Process", STATUS_COMPLETED = "Completed";
    String querySelectionTypeAndId, querySelectionId;

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
        setContentView(R.layout.activity_pms_work_pm);
        pager = (ViewPager) findViewById(R.id.viewpager);
        tabs = (TabLayout) findViewById(R.id.tabs);
        offData = new Data(WorkPmActivity.this);

        tabs.setTabMode(TabLayout.MODE_SCROLLABLE); //由左到右，畫面上看起來會右邊空一塊(適用於Tab數量眾多的時候)
        tabs.setTabGravity(TabLayout.GRAVITY_CENTER); //全部擠在中間
        tabs.setTabTextColors(Color.BLACK, Color.BLACK); //設定字體顏色
        tabs.setupWithViewPager(pager);

        ImageButton btnSelectPage = (ImageButton)findViewById(R.id.ibtnSelectPage);
        btnSelectPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LayoutInflater pageList = LayoutInflater.from(WorkPmActivity.this);
                View vPageList = pageList.inflate(R.layout.activity_pms_listview_dialog,null);
                ListView lsPage = (ListView)vPageList.findViewById(R.id.lsPage);

                //將array中的資料轉成多國語系，讓使用者好辨識
                String[] arPageTitle = {"SOP", "CHECK_ITEM", "EQP_PART", "FILE_ATTACHMENT", "USER_WH", "MODIFY_PART_RECORD"};
                List<HashMap<String, Object>> lstDataList = new ArrayList<HashMap<String, Object>>();
                for (String data : arPageTitle) {
                    HashMap<String, Object> dr = new HashMap<String, Object>();
                    dr.put("ID", data);
                    dr.put("NAME", WorkPmActivity.this.getResString(data));
                    lstDataList.add(dr);
                }

                UAdapter uAdapterDataList = new UAdapter(WorkPmActivity.this, lstDataList, R.layout.listview_download_data, new String[]{"NAME"},
                        new int[]{R.id.tvData});
                uAdapterDataList.addAdapterEvent(new UAdapterListener() {

                    @Override
                    public void onViewRefresh(View view, List<Map<String, ?>> filterData, int position, String[] displayColumns,
                                              int[] viewColumns) {
                        // TODO 自動產生的方法 Stub
                    }
                });

                android.app.AlertDialog.Builder pageDialog = new android.app.AlertDialog.Builder(WorkPmActivity.this);
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

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        strWoId = bundle.getString("WO_ID");
        querySelectionTypeAndId = "MRO_WO_TYPE = 'PM' AND MRO_WO_ID = '" + strWoId + "'";
        querySelectionId = "MRO_WO_ID = '" + strWoId + "'";

        getSupportActionBar().setTitle(getSupportActionBar().getTitle() + "_" + strWoId);

        data = new Data(WorkPmActivity.this);
        SQLiteDatabase db = data.getReadableDatabase();
        Cursor cursor = db.query(TABLENAME_SEMS_MRO_WO, null, querySelectionTypeAndId, null, null, null, null);
        cursor.moveToFirst();
        strEqpId = cursor.getString(cursor.getColumnIndex("EQP_ID"));

        findAllById();
        setAllEditText(cursor);
        setListener();

        btnAddCmt.getBackground().setAlpha(50);

        GetBeforeUpdateData(); //取得變更前的sqlite資料。

        switch (cursor.getString(cursor.getColumnIndex(FIELDNAME_WO_STATUS))) {
            case STATUS_WAIT:
                btnStartPm.setText(R.string.START_PM);
                break;
            case STATUS_PROCESS:
                btnStartPm.setText(R.string.START_PM);
                break;
            case STATUS_COMPLETED:
                btnStartPm.setText(R.string.RESTART_OPEN);
                break;
        }

        db.close();
        GetPmWoInfo();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isNeedUpdate) {
            this.finish();
            return false;
        }

        try {
            android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(WorkPmActivity.this);
            alertDialog.setMessage(getResources().getString(R.string.WHETHER_TO_SAVE_CHANGE));
            alertDialog.setPositiveButton(getResources().getString(R.string.YES), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //檢查是否存在保養NG的項目，如果存在NG項目要詢問是否轉開維修工單。
                    Cursor csCheckNg = offData.getReadableDatabase().query("SEMS_PM_WO_CHECK", null, "MRO_WO_ID = ? AND CHECK_RESULT = 'NG'", new String[]{strWoId}, null, null, null);

                    if (csCheckNg.getCount() > 0) {
                        AlertDialog.Builder alertCreateRepairWo = new AlertDialog.Builder(WorkPmActivity.this);
                        alertCreateRepairWo.setMessage(getResources().getString(R.string.EAPE105026));
                        alertCreateRepairWo.setPositiveButton(getResources().getString(R.string.CONFIRM), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.setClass(WorkPmActivity.this, RepairWoCreate.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("MRO_WO_ID", strWoId);
                                bundle.putString("MRO_WO_TYPE", "PM");
                                intent.putExtras(bundle);
                                startActivity(intent);

                                WorkPmActivity.this.finish();
                            }
                        });

                        alertCreateRepairWo.setNegativeButton(getResources().getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                WorkPmActivity.this.finish();
                            }
                        });

                        alertCreateRepairWo.setCancelable(false);
                        alertCreateRepairWo.show();
                    } else {
                        WorkPmActivity.this.finish();
                    }
                }
            });

            alertDialog.setNegativeButton(getResources().getString(R.string.NO), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    UndoDataToTemp(); //將工單復原回未作業時的樣子
                    WorkPmActivity.this.finish();
                    return;
                }
            });

            alertDialog.show();
        } catch (Exception ex) {
            ex.getMessage();
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        if (!isNeedUpdate) {
            WorkPmActivity.this.finish();
            return;
        }

        android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(WorkPmActivity.this);
        alertDialog.setMessage(getResources().getString(R.string.WHETHER_TO_SAVE_CHANGE));
        alertDialog.setPositiveButton(getResources().getString(R.string.YES), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //檢查是否存在保養NG的項目，如果存在NG項目要詢問是否轉開維修工單。
                Cursor csCheckNg = offData.getReadableDatabase().query("SEMS_PM_WO_CHECK", null, "MRO_WO_ID = ? AND CHECK_RESULT = 'NG'", new String[]{strWoId},
                        null, null, null);

                if (csCheckNg.getCount() > 0) {
                    AlertDialog.Builder alertCreateRepairWo = new AlertDialog.Builder(WorkPmActivity.this);
                    alertCreateRepairWo.setMessage(getResources().getString(R.string.EAPE105026));
                    alertCreateRepairWo.setPositiveButton(getResources().getString(R.string.CONFIRM), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(WorkPmActivity.this, RepairWoCreate.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("MRO_WO_ID", strWoId);
                            bundle.putString("MRO_WO_TYPE", "PM");
                            intent.putExtras(bundle);
                            startActivity(intent);

                            WorkPmActivity.this.finish();
                        }
                    });

                    alertCreateRepairWo.setNegativeButton(getResources().getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            WorkPmActivity.this.finish();
                        }
                    });

                    alertCreateRepairWo.setCancelable(false);
                    alertCreateRepairWo.show();
                } else {
                    WorkPmActivity.this.finish();
                }
            }
        });

        alertDialog.setNegativeButton(getResources().getString(R.string.NO), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UndoDataToTemp(); //將工單復原回未作業時的樣子
                WorkPmActivity.this.finish();
                return;
            }
        });

        alertDialog.show();
    }

    private void setListener() {
        btnAddCmt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isWork) {
                    ShowMessage(getResources().getString(R.string.EAPE105020));
                    return;
                }
                showAlertDialogInAddCmt();
            }
        });

        btnStartPm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SQLiteDatabase dbUpdate = data.getWritableDatabase();
                ContentValues cUpdate = new ContentValues();
                SQLiteDatabase dbQuery = data.getReadableDatabase();

                Cursor csNowWoStatus = offData.getReadableDatabase().query("SEMS_MRO_WO", null, querySelectionId, null, null, null, null);
                csNowWoStatus.moveToFirst();
                String woStatus = csNowWoStatus.getString(csNowWoStatus.getColumnIndex(FIELDNAME_WO_STATUS));

                isNeedUpdate = true;
                if (!woStatus.toUpperCase().contentEquals(STATUS_COMPLETED)) {
                    if (!isWork) {
                        if (woStatus.toUpperCase().contentEquals("WAIT")) {
                            //詢問是否開工，開工後無法透過復原取消開工
                            android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(WorkPmActivity.this);
                            alertDialog.setMessage(getResources().getString(R.string.IS_WORK_START));
                            alertDialog.setPositiveButton(getResources().getString(R.string.YES), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
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
                                    GetPmWoInfo();
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
                            btnStartPm.setText(R.string.COMPLETE);
                            isWork = true;
                            btnAddCmt.getBackground().setAlpha(255);

                            cUpdate.put(FIELDNAME_WO_STATUS, STATUS_PROCESS);
                            cUpdate.put(FIELDNAME_IS_CHANGE, "Y");
                            dbUpdate.update(TABLENAME_SEMS_MRO_WO, cUpdate, querySelectionTypeAndId, null);
                            Cursor cursor = dbQuery.query(TABLENAME_SEMS_MRO_WO, null, querySelectionTypeAndId, null, null, null, null);
                            cursor.moveToFirst();
                            edPmStatus.setText(cursor.getString(cursor.getColumnIndex(FIELDNAME_WO_STATUS)));
                            GetPmWoInfo();
                        }
                    } else {
                        Cursor tmpCursor = checkItemStatus();
                        if (tmpCursor != null) {
                            ShowMessage(R.string.EAPE105001, tmpCursor.getString(tmpCursor.getColumnIndex(FIELDNAME_CHECK_ID)));
                            return;
                        }
                        if (!checkUserStatus()) {
                            ShowMessage(R.string.EAPE105002);
                            return;
                        }

                        showAlertDialogInStartPm();
                        btnAddCmt.getBackground().setAlpha(50);
                        GetPmWoInfo();
                    }
                } else { //如果工單狀態是"已完工"，按下按鈕後重新開工，狀態改為process
                    cUpdate.put(FIELDNAME_WO_STATUS, STATUS_PROCESS);
                    cUpdate.put(FIELDNAME_IS_CHANGE, "Y");
                    dbUpdate.update(TABLENAME_SEMS_MRO_WO, cUpdate, querySelectionTypeAndId, null);
                    Cursor cursor = dbQuery.query(TABLENAME_SEMS_MRO_WO, null, querySelectionTypeAndId, null, null, null, null);
                    cursor.moveToFirst();
                    edPmStatus.setText(cursor.getString(cursor.getColumnIndex(FIELDNAME_WO_STATUS)));

                    btnAddCmt.getBackground().setAlpha(255);
                    isWork = true;
                    btnStartPm.setText(getResources().getString(R.string.COMPLETE));
                    GetPmWoInfo();
                }

                dbUpdate.close();
                dbQuery.close();
            }
        });
    }

    //檢查人員工時是否有保養人員
    private boolean checkUserStatus() {
        SQLiteDatabase db = data.getReadableDatabase();
        Cursor cursor = db.query(TABLENAME_SEMS_MRO_WO_WH, null, querySelectionId, null, null, null, null);
        if (cursor.getCount() != 0) {
            db.close();
            return true;
        } else {
            db.close();
            return false;
        }
    }

    //檢查保養工單檢查項目是否有值
    private Cursor checkItemStatus() {
        SQLiteDatabase db = data.getReadableDatabase();
        Cursor cursor = db.query(TABLENAME_SEMS_PM_WO_CHECK, null, querySelectionId, null, null, null, null);

        while (cursor.moveToNext()) {
            if (cursor.getString(cursor.getColumnIndex(FIELDNAME_CHECK_VALUE)).isEmpty()
                    || cursor.getString(cursor.getColumnIndex(FIELDNAME_CHECK_VALUE)) == null
                    || cursor.getString(cursor.getColumnIndex(FIELDNAME_CHECK_VALUE)).contentEquals("*")
                    || cursor.getString(cursor.getColumnIndex(FIELDNAME_CHECK_RESULT)).isEmpty()
                    || cursor.getString(cursor.getColumnIndex(FIELDNAME_CHECK_RESULT)) == null
                    || cursor.getString(cursor.getColumnIndex(FIELDNAME_CHECK_RESULT)).contentEquals("*")) {
                db.close();
                return cursor;
            }
        }
        db.close();
        return null;
    }

    //顯示自動計算出的人員總工時 額外工時輸入視窗且內容可修改再寫入保養維修工單和變更狀態
    private void showAlertDialogInStartPm() {
        LayoutInflater inflater = LayoutInflater.from(WorkPmActivity.this);
        final View dialogView = inflater.inflate(R.layout.style_pms_dialog_pm_hour, null);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(WorkPmActivity.this);
        alertDialog.setView(dialogView);

        final EditText editTextTtl = (EditText) dialogView.findViewById(R.id.edPmTtlHour);
        editTextTtl.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        final EditText editTextExc = (EditText) dialogView.findViewById(R.id.edPmExcHour);
        editTextExc.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editTextExc.setText("0");

        SQLiteDatabase dbQuery = data.getReadableDatabase();
        Cursor cursor = dbQuery.query(TABLENAME_SEMS_MRO_WO_WH, null, querySelectionId, null, null, null, null);

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Date startDate = null;
        Date endDate = null;
        boolean isFirst = true;
        while (cursor.moveToNext()) {
            try {
                if (isFirst) {
                    startDate = dateFormat.parse(cursor.getString(4));
                    endDate = dateFormat.parse(cursor.getString(5));
                    isFirst = false;
                    continue;
                }

                if (dateFormat.parse(cursor.getString(cursor.getColumnIndex(FIELDNAME_START_DT))).getTime() < startDate.getTime())
                    startDate = dateFormat.parse(cursor.getString(cursor.getColumnIndex(FIELDNAME_START_DT)));
                if (dateFormat.parse(cursor.getString(cursor.getColumnIndex(FIELDNAME_END_DT))).getTime() > endDate.getTime())
                    endDate = dateFormat.parse(cursor.getString(cursor.getColumnIndex(FIELDNAME_END_DT)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        dbQuery.close();

        if (startDate != null && endDate != null)
            editTextTtl.setText(String.valueOf(((endDate.getTime() - startDate.getTime()) / (1000 * 60))));

        final EditText edCmt = (EditText) dialogView.findViewById(R.id.edCmt);
        edCmt.setText(edPmCmt.getText().toString());

        alertDialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SQLiteDatabase dbUpdate = data.getWritableDatabase();
                ContentValues cUpdate = new ContentValues();
                cUpdate.put(FIELDNAME_TTL_MAN_HOUR, editTextTtl.getText().toString());
                cUpdate.put(FIELDNAME_EXC_MAN_HOUR, editTextExc.getText().toString());
                cUpdate.put(FIELDNAME_WO_STATUS, STATUS_COMPLETED);
                cUpdate.put(FIELDNAME_CMT, edCmt.getText().toString());
                dbUpdate.update(TABLENAME_SEMS_MRO_WO, cUpdate, querySelectionTypeAndId, null);
                dbUpdate.close();

                SQLiteDatabase dbQuery = data.getReadableDatabase();
                Cursor cursor = dbQuery.query(TABLENAME_SEMS_MRO_WO, null, querySelectionTypeAndId, null, null, null, null);
                cursor.moveToFirst();
                edPmStatus.setText(cursor.getString(cursor.getColumnIndex(FIELDNAME_WO_STATUS)));
                edPmCmt.setText(edCmt.getText().toString());
                dbQuery.close();

                //檢查是否存在保養NG的項目，如果存在NG項目要詢問是否轉開維修工單。
                Cursor csCheckNg = offData.getReadableDatabase().query("SEMS_PM_WO_CHECK", null, "MRO_WO_ID = ? AND CHECK_RESULT = 'NG'", new String[]{strWoId},
                        null, null, null);

                if (csCheckNg.getCount() > 0) {
                    AlertDialog.Builder alertCreateRepairWo = new AlertDialog.Builder(WorkPmActivity.this);
                    alertCreateRepairWo.setMessage(getResources().getString(R.string.EAPE105026));
                    alertCreateRepairWo.setPositiveButton(getResources().getString(R.string.CONFIRM), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setClass(WorkPmActivity.this, RepairWoCreate.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("MRO_WO_ID", strWoId);
                            bundle.putString("MRO_WO_TYPE", "PM");
                            intent.putExtras(bundle);
                            startActivity(intent);

                            WorkPmActivity.this.finish();
                        }
                    });

                    alertCreateRepairWo.setNegativeButton(getResources().getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            WorkPmActivity.this.finish();
                        }
                    });

                    alertCreateRepairWo.setCancelable(false);
                    alertCreateRepairWo.show();
                } else {
                    WorkPmActivity.this.finish();
                }
            }
        });
        alertDialog.show();
    }

    //顯示新增備註輸入視窗且把內容寫入保養維修工單和變更狀態
    private void showAlertDialogInAddCmt() {
        LayoutInflater inflater = LayoutInflater.from(WorkPmActivity.this);
        final View dialogView = inflater.inflate(R.layout.style_pms_dialog_add_cmt, null);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(WorkPmActivity.this);
        alertDialog.setView(dialogView);

        final EditText editText = (EditText) dialogView.findViewById(R.id.edAddComment);
        editText.setText(edPmCmt.getText().toString());

        alertDialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SQLiteDatabase dbUpdate = data.getWritableDatabase();
                ContentValues cUpdate = new ContentValues();
                cUpdate.put(FIELDNAME_CMT, editText.getText().toString());
                dbUpdate.update(TABLENAME_SEMS_MRO_WO, cUpdate, querySelectionTypeAndId, null);
                dbUpdate.close();

                SQLiteDatabase dbQuery = data.getReadableDatabase();
                Cursor cursor = dbQuery.query(TABLENAME_SEMS_MRO_WO, null, querySelectionTypeAndId, null, null, null, null);
                cursor.moveToFirst();
                edPmCmt.setText(cursor.getString(cursor.getColumnIndex(FIELDNAME_CMT)));
                dbQuery.close();
            }
        });
        alertDialog.show();
    }

    private void findAllById() {
        this.edEqpId = (EditText) findViewById(R.id.edEqpId);
        this.edEqpName = (EditText) findViewById(R.id.edEqpName);
        this.edPmContentId = (EditText) findViewById(R.id.edPmContentId);
        this.edPmContentName = (EditText) findViewById(R.id.edPmContentName);
        this.edPmStatus = (EditText) findViewById(R.id.edPmStatus);
        this.edPmPlanDate = (EditText) findViewById(R.id.edPmPlanDate);
        this.edPmCmt = (EditText) findViewById(R.id.edPmCmt);
        this.btnStartPm = (Button) findViewById(R.id.btnStartPm);
        this.btnAddCmt = (Button) findViewById(R.id.btnAddCmt);
    }

    private void setAllEditText(Cursor cursor) {
        edEqpId.setEnabled(false);
        edEqpName.setEnabled(false);
        edPmContentId.setEnabled(false);
        edPmContentName.setEnabled(false);
        edPmStatus.setEnabled(false);
        edPmPlanDate.setEnabled(false);
        edPmCmt.setEnabled(false);

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = dateFormat.parse(cursor.getString(cursor.getColumnIndex(FIELDNAME_PLAN_DT)).replace("T", " "));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        edEqpId.setText(cursor.getString(cursor.getColumnIndex(FIELDNAME_EQP_ID)));
        edEqpName.setText(cursor.getString(cursor.getColumnIndex(FIELDNAME_EQP_NAME)));
        edPmContentId.setText(cursor.getString(cursor.getColumnIndex(FIELDNAME_PM_ID)));
        edPmContentName.setText(cursor.getString(cursor.getColumnIndex(FIELDNAME_PM_NAME)));
        edPmStatus.setText(cursor.getString(cursor.getColumnIndex(FIELDNAME_WO_STATUS)));
        edPmPlanDate.setText(String.format("%tF", date));
        edPmCmt.setText(cursor.getString(cursor.getColumnIndex(FIELDNAME_CMT)));
    }

    private void GetPmWoInfo() {
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
        public int getCount() {
            return arTitle.length;
        }

        @Override
        public Fragment getItem(int position) {
            return WorkPmFragment.newInstance(arTitle[position], strWoId, strEqpId);
        }
    }

    public boolean GetIsWork() {
        return isWork;
    }
}