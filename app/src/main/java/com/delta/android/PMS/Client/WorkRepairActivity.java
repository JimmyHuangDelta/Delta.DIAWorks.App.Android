package com.delta.android.PMS.Client;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseActivity;
import com.delta.android.Core.Adapter.UAdapter;
import com.delta.android.Core.Adapter.UAdapterListener;
import com.delta.android.PMS.Client.Fragment.WorkRepairFragment;
import com.delta.android.PMS.OffLineData.Data;
import com.delta.android.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkRepairActivity extends BaseActivity {

    String strWoId = "";
    String strEqpId = "";
    private ViewPager pager;
    private TabLayout tabs;
    SimpleDateFormat df;
    private boolean isWork = false; //預設為false ，開始保養後才會改為Y，保養結束後就又會改回N

    EditText edEqpID;
    EditText edEqpName;
    EditText edCallRepairId;
    EditText edCallRepairName;
    EditText edFailStartDate;
    EditText edFailCmt;
    EditText edFailEndDate;
    EditText edFailEndTime;
    EditText edFailStatus;
    Data offData;
    Button btnWorkStatus;

    //紀錄工單開始作業前的所有資料 (只記錄會有異動的)
    ArrayList<HashMap<String, String>> arTempWo = new ArrayList<>();
    ArrayList<HashMap<String, String>> arTempWoWh = new ArrayList<>();
    ArrayList<HashMap<String, String>> arTempWoPartTrx = new ArrayList<>();
    ArrayList<HashMap<String, String>> arTempWoEqpPart = new ArrayList<>();
    ArrayList<HashMap<String, String>> arTempWoFile = new ArrayList<>();
    ArrayList<HashMap<String, String>> arTempWoRsn = new ArrayList<>();
    ArrayList<HashMap<String, String>> arTempWoSty = new ArrayList<>();

    boolean isNeedUpdate = false; //按下返回鍵時是否需要彈框詢問是否儲存，預設為不詢問。

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pms_work_repair);

        pager = (ViewPager) findViewById(R.id.viewpager);
        tabs = (TabLayout) findViewById(R.id.tabs);
        offData = new Data(WorkRepairActivity.this);
        df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        Bundle bundle = getIntent().getExtras();
        strWoId = bundle.getString("WO_ID");
        getSupportActionBar().setTitle(getSupportActionBar().getTitle() + "_" + strWoId);

        //取得工單對應的機台
        SQLiteDatabase dbQueryWo = offData.getReadableDatabase();
        Cursor csWo = dbQueryWo.query("SEMS_MRO_WO", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
        csWo.moveToFirst();
        strEqpId = csWo.getString(csWo.getColumnIndex("EQP_ID"));
        dbQueryWo.close();

        tabs.setTabMode(TabLayout.MODE_SCROLLABLE); //由左到右，畫面上看起來會右邊空一塊(適用於Tab數量眾多的時候)
        tabs.setTabGravity(TabLayout.GRAVITY_CENTER); //全部擠在中間
        tabs.setTabTextColors(Color.BLACK, Color.BLACK); //設定字體顏色
        tabs.setupWithViewPager(pager);

        ImageButton btnSelectPage = (ImageButton)findViewById(R.id.ibtnSelectPage);
        btnSelectPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LayoutInflater pageList = LayoutInflater.from(WorkRepairActivity.this);
                View vPageList = pageList.inflate(R.layout.activity_pms_listview_dialog,null);
                ListView lsPage = (ListView)vPageList.findViewById(R.id.lsPage);

                //將array中的資料轉成多國語系，讓使用者好辨識
                String[] arPageTitle = {"FAIL", "FAIL_REASON", "FAIL_STRATEGY", "EQP_PART", "FILE_ATTACHMENT", "USER_WH", "MODIFY_PART_RECORD"};
                List<HashMap<String, Object>> lstDataList = new ArrayList<HashMap<String, Object>>();
                for (String data : arPageTitle) {
                    HashMap<String, Object> dr = new HashMap<String, Object>();
                    dr.put("ID", data);
                    dr.put("NAME", WorkRepairActivity.this.getResString(data));
                    lstDataList.add(dr);
                }

                UAdapter uAdapterDataList = new UAdapter(WorkRepairActivity.this, lstDataList, R.layout.listview_download_data, new String[]{"NAME"},
                        new int[]{R.id.tvData});
                uAdapterDataList.addAdapterEvent(new UAdapterListener() {

                    @Override
                    public void onViewRefresh(View view, List<Map<String, ?>> filterData, int position, String[] displayColumns,
                                              int[] viewColumns) {
                        // TODO 自動產生的方法 Stub
                    }
                });

                AlertDialog.Builder pageDialog = new AlertDialog.Builder(WorkRepairActivity.this);
                pageDialog.setView(vPageList);
                final AlertDialog page = pageDialog.create();

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

        final Button btnAddCmt = (Button) findViewById(R.id.btnAddFailCmt);
        btnWorkStatus = (Button) findViewById(R.id.btnStartEndRepait);

        if (csWo.getString(csWo.getColumnIndex("WO_STATUS")).toUpperCase().contentEquals("COMPLETED")) {
            btnWorkStatus.setText(getResources().getString(R.string.RESTART_OPEN));
        } else {
            btnWorkStatus.setText(getResources().getString(R.string.START_REPAIR));
        }

        edEqpID = (EditText) findViewById(R.id.edRepairEqpId);
        setEditTextReadOnly(edEqpID);
        edEqpName = (EditText) findViewById(R.id.edRepairEqpName);
        setEditTextReadOnly(edEqpName);
        edCallRepairId = (EditText) findViewById(R.id.edCallRepairId);
        setEditTextReadOnly(edCallRepairId);
        edCallRepairName = (EditText) findViewById(R.id.edCallRepairName);
        setEditTextReadOnly(edCallRepairName);
        edFailStartDate = (EditText) findViewById(R.id.edDefectStart);
        setEditTextReadOnly(edFailStartDate);
        edFailCmt = (EditText) findViewById(R.id.edFailCmt);
        setEditTextReadOnly(edFailCmt);
        edFailStatus = (EditText) findViewById(R.id.edRepairStatus);
        setEditTextReadOnly(edFailStatus);

        edEqpID.setText(csWo.getString(csWo.getColumnIndex("EQP_ID")));
        edEqpName.setText(csWo.getString(csWo.getColumnIndex("EQP_NAME")));
        edCallRepairId.setText(csWo.getString(csWo.getColumnIndex("CALL_FIX_TYPE_ID")));
        edCallRepairName.setText(csWo.getString(csWo.getColumnIndex("CALL_FIX_TYPE_NAME")));
        edFailStartDate.setText(csWo.getString(csWo.getColumnIndex("PLAN_DT")).replace("T", " "));
        edFailStatus.setText(csWo.getString(csWo.getColumnIndex("WO_STATUS")).replace("T", " "));
        edFailCmt.setText(csWo.getString(csWo.getColumnIndex("CMT")));

        GetRepairWoInfo();
        GetBeforeUpdateData(); //取得工單開始變更前的資料

        btnAddCmt.getBackground().setAlpha(50);
        btnAddCmt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isWork)
                    return;

                AlertDialog.Builder builder = new AlertDialog.Builder(WorkRepairActivity.this);
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

                        edFailCmt.setText(edCmt.getText().toString());
                    }
                });

                final AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        edFailEndDate = (EditText) findViewById(R.id.edRepairEndDate);
        edFailEndDate.setText(csWo.getString(csWo.getColumnIndex("FAIL_END_DT")).replace("T", " ").split(" ")[0]);
        edFailEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isWork)
                    return;

                AlertDialog.Builder builder = new AlertDialog.Builder(WorkRepairActivity.this);
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
                        edFailEndDate.setText(sb);
                        dialog.cancel();
                    }
                });
            }
        });
        setEditTextReadOnly(edFailEndDate);

        edFailEndTime = (EditText) findViewById(R.id.edRepairEndTime);
        try {
            edFailEndTime.setText(csWo.getString(csWo.getColumnIndex("FAIL_END_DT")).replace("T", " ").split(" ")[1]);
        } catch (Exception ex) {
            edFailEndTime.setText("");
        }

        edFailEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isWork)
                    return;

                final Calendar c = Calendar.getInstance();
                int mHour = c.get(Calendar.HOUR_OF_DAY);
                int mMinute = c.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(WorkRepairActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        if (hourOfDay < 10 && minute < 10) {
                            edFailEndTime.setText("0" + hourOfDay + ":0" + minute);
                        } else if (hourOfDay < 10 && minute >= 10) {
                            edFailEndTime.setText("0" + hourOfDay + ":" + minute);
                        } else if (hourOfDay >= 10 && minute < 10) {
                            edFailEndTime.setText(hourOfDay + ":0" + minute);
                        } else {
                            edFailEndTime.setText(hourOfDay + ":" + minute);
                        }
                    }
                }, mHour, mMinute, true);
                timePickerDialog.show();
            }
        });
        setEditTextReadOnly(edFailEndTime);

        //如果工單已完工，按鈕鎖住不可以再按。
        btnWorkStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //取的當前工單狀態
                SQLiteDatabase queryWo = offData.getReadableDatabase();
                Cursor csWo = queryWo.query("SEMS_MRO_WO", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
                csWo.moveToFirst();
                String woStatus = csWo.getString(csWo.getColumnIndex("WO_STATUS"));
                String cmt = csWo.getString(csWo.getColumnIndex("CMT"));
                queryWo.close();

                isNeedUpdate = true;

                if (!isWork) {
                    //檢查工單狀態，工單狀態只要不是"已完工" 就可以繼續作業
                    if (!woStatus.toUpperCase().contentEquals("COMPLETED")) {
                        final SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        final Date nd = new Date(System.currentTimeMillis());
                        final String strDate = df.format(nd); //紀錄當前時間

                        //如果工單狀態是wait ，彈框出來輸入計畫結束時間，再將公單狀態改成process
                        if (woStatus.toUpperCase().contentEquals("WAIT")) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(WorkRepairActivity.this);
                            View view = getLayoutInflater().inflate(R.layout.style_pms_dialog_date_time, null);
                            builder.setView(view);
                            final AlertDialog dialog = builder.create();
                            dialog.show();
                            dialog.setCancelable(false);

                            final EditText edPlanEndDate = (EditText) view.findViewById(R.id.edPlanFailEndDate);
                            setEditTextReadOnly(edPlanEndDate);
                            edPlanEndDate.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(WorkRepairActivity.this);
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
                                            edPlanEndDate.setText(sb);
                                            dialog.cancel();
                                        }
                                    });
                                }
                            });

                            final EditText edPlanEndTime = (EditText) view.findViewById(R.id.edPlanFailEndTime);
                            setEditTextReadOnly(edPlanEndTime);
                            edPlanEndTime.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final Calendar c = Calendar.getInstance();
                                    int mHour = c.get(Calendar.HOUR_OF_DAY);
                                    int mMinute = c.get(Calendar.MINUTE);

                                    // Launch Time Picker Dialog
                                    TimePickerDialog timePickerDialog = new TimePickerDialog(WorkRepairActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                        @Override
                                        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                                            if (hourOfDay < 10 && minute < 10) {
                                                edPlanEndTime.setText("0" + hourOfDay + ":0" + minute);
                                            } else if (hourOfDay < 10 && minute >= 10) {
                                                edPlanEndTime.setText("0" + hourOfDay + ":" + minute);
                                            } else if (hourOfDay >= 10 && minute < 10) {
                                                edPlanEndTime.setText(hourOfDay + ":0" + minute);
                                            } else {
                                                edPlanEndTime.setText(hourOfDay + ":" + minute);
                                            }
                                        }
                                    }, mHour, mMinute, true);
                                    timePickerDialog.show();
                                }
                            });

                            //按下彈框的確認紐
                            Button btnConfirm = (Button) view.findViewById(R.id.btnConfirm);
                            btnConfirm.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (edPlanEndDate.getText().toString().contentEquals("")) {
                                        Toast.makeText(WorkRepairActivity.this, getResources().getString(R.string.EAPE106002), Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    if (edPlanEndTime.getText().toString().contentEquals("")) {
                                        Toast.makeText(WorkRepairActivity.this, getResources().getString(R.string.EAPE106003), Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    //檢查輸入的時間結束時間是否大於當前時間
                                    Date end = new Date();
                                    try {
                                        end = df.parse(edPlanEndDate.getText().toString() + " " + edPlanEndTime.getText().toString() + ":00");
                                    } catch (Exception ex) {
                                        ex.getCause();
                                        return;
                                    }

                                    int comResult = nd.compareTo(end);
                                    if (comResult >= 0) {
                                        Toast.makeText(WorkRepairActivity.this, getResources().getString(R.string.EAPE106027), Toast.LENGTH_LONG).show();
                                        edPlanEndDate.setText("");
                                        edPlanEndTime.setText("");
                                        return;
                                    }

                                    btnWorkStatus.setText(getResources().getString(R.string.COMPLETE));
                                    isWork = true;

                                    ContentValues cvUpdateWoStatus = new ContentValues();
                                    cvUpdateWoStatus.put("WO_STATUS", "Process");
                                    cvUpdateWoStatus.put("IS_CHANGE", "Y");
                                    cvUpdateWoStatus.put("START_DT", strDate);
                                    cvUpdateWoStatus.put("PLAN_END_DT", edPlanEndDate.getText().toString() + " " + edPlanEndTime.getText().toString() + ":00");
                                    offData.getWritableDatabase().update("SEMS_MRO_WO", cvUpdateWoStatus, "MRO_WO_ID = ?", new String[]{strWoId});

                                    edFailStatus.setText("Process");
                                    btnAddCmt.getBackground().setAlpha(255);

                                    //關閉視窗
                                    dialog.hide();
                                    GetRepairWoInfo();
                                }
                            });

                            //按下彈框的取消紐
                            Button btnCancel = (Button) view.findViewById(R.id.btnCancel);
                            btnCancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.hide();
                                }
                            });
                        } else {
                            btnWorkStatus.setText(getResources().getString(R.string.COMPLETE));
                            isWork = true;
                            GetRepairWoInfo();

                            ContentValues cvUpdateWoStatus = new ContentValues();
                            cvUpdateWoStatus.put("IS_CHANGE", "Y");
                            offData.getWritableDatabase().update("SEMS_MRO_WO", cvUpdateWoStatus, "MRO_WO_ID = ?", new String[]{strWoId});
                            btnAddCmt.getBackground().setAlpha(255);
                        }
                    } else {
                        //將工單重新開工。
                        ContentValues cvUpdateWoStatus = new ContentValues();
                        cvUpdateWoStatus.put("WO_STATUS", "Process");
                        cvUpdateWoStatus.put("IS_CHANGE", "Y");
                        offData.getWritableDatabase().update("SEMS_MRO_WO", cvUpdateWoStatus, "MRO_WO_ID = ?", new String[]{strWoId});

                        edFailStatus.setText("Process");
                        btnWorkStatus.setText(getResources().getString(R.string.COMPLETE));
                        isWork = true;
                        GetRepairWoInfo();

                        btnAddCmt.getBackground().setAlpha(255);
                    }
                } else { //工單編輯狀態，檢查項目是否輸入完成
                    //檢查是否有輸入維修結束時間
                    if (edFailEndDate.getText().toString().contentEquals("") || edFailEndTime.getText().toString().contentEquals("")) {
                        ShowMessage(getResources().getString(R.string.EAPE106019));
                        return;
                    }

                    //檢查維修人員是否有資料
                    Cursor csUser = offData.getReadableDatabase().query("SEMS_MRO_WO_WH", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
                    csUser.moveToFirst();
                    if (csUser.getCount() <= 0) {
                        ShowMessage(getResources().getString(R.string.EAPE106020));
                        return;
                    }

                    //檢查故障原因是否有資料
                    Cursor csFailRsn = offData.getReadableDatabase().query("SEMS_REPAIR_WO_RSN", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
                    csFailRsn.moveToFirst();
                    if (csFailRsn.getCount() <= 0) {
                        ShowMessage(getResources().getString(R.string.EAPE106021));
                        return;
                    }

                    //檢查故障處置處置是否有資料
                    Cursor csFailSty = offData.getReadableDatabase().query("SEMS_REPAIR_WO_STY", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
                    csFailSty.moveToFirst();
                    if (csFailSty.getCount() <= 0) {
                        ShowMessage(getResources().getString(R.string.EAPE106022));
                        return;
                    }

                    //彈框輸入維修工時與除外工時
                    LayoutInflater woCompInflat = LayoutInflater.from(WorkRepairActivity.this);
                    View woCompView = woCompInflat.inflate(R.layout.style_pms_dialog_repair_hour, null);
                    android.support.v7.app.AlertDialog.Builder woCompBuilder = new android.support.v7.app.AlertDialog.Builder(WorkRepairActivity.this);
                    woCompBuilder.setView(woCompView);

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
                                Toast.makeText(WorkRepairActivity.this, getResources().getString(R.string.CONFIRM), Toast.LENGTH_LONG).show();/////////////////////////////////////////////////////////
                                return;
                            }

                            Date nd = new Date(System.currentTimeMillis());
                            String strDate = df.format(nd); //紀錄當前時間

                            ContentValues cvUpdateWoStatus = new ContentValues();
                            cvUpdateWoStatus.put("WO_STATUS", "Completed");
                            cvUpdateWoStatus.put("FAIL_END_DT", edFailEndDate.getText().toString() + " " + edFailEndTime.getText().toString());
                            cvUpdateWoStatus.put("END_DT", strDate);
                            cvUpdateWoStatus.put("TTL_MAN_HOUR", TtlMin);
                            cvUpdateWoStatus.put("EXC_MAN_HOUR", ExcMin);
                            cvUpdateWoStatus.put("CMT", edCmt.getText().toString());
                            offData.getWritableDatabase().update("SEMS_MRO_WO", cvUpdateWoStatus, "MRO_WO_ID = ?", new String[]{strWoId});

                            WorkRepairActivity.this.finish();
                        }
                    });

                    woCompBuilder.show();
                }
            }
        });
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

        Cursor csWoRsn = offData.getReadableDatabase().query("SEMS_REPAIR_WO_RSN", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
        csWoRsn.moveToFirst();
        for (int i = 0; i < csWoRsn.getCount(); i++) {
            HashMap<String, String> woData = new HashMap<>();
            woData.put("MRO_WO_ID", strWoId);
            woData.put("FAIL_REASON_ID", csWoRsn.getString(csWoRsn.getColumnIndex("FAIL_REASON_ID")));
            woData.put("FAIL_REASON_NAME", csWoRsn.getString(csWoRsn.getColumnIndex("FAIL_REASON_NAME")));
            woData.put("FAIL_REASON_CMT", csWoRsn.getString(csWoRsn.getColumnIndex("FAIL_REASON_CMT")));

            arTempWoRsn.add(woData);
            csWoRsn.moveToNext();
        }

        Cursor csWoSty = offData.getReadableDatabase().query("SEMS_REPAIR_WO_STY", null, "MRO_WO_ID = ?", new String[]{strWoId}, null, null, null);
        csWoSty.moveToFirst();
        for (int i = 0; i < csWoSty.getCount(); i++) {
            HashMap<String, String> woData = new HashMap<>();
            woData.put("MRO_WO_ID", strWoId);
            woData.put("FAIL_STRATEGY_ID", csWoSty.getString(csWoSty.getColumnIndex("FAIL_STRATEGY_ID")));
            woData.put("FAIL_STRATEGY_NAME", csWoSty.getString(csWoSty.getColumnIndex("FAIL_STRATEGY_NAME")));
            woData.put("FAIL_STRATEGY_CMT", csWoSty.getString(csWoSty.getColumnIndex("FAIL_STRATEGY_CMT")));

            arTempWoSty.add(woData);
            csWoSty.moveToNext();
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
        offData.getWritableDatabase().delete("SEMS_REPAIR_WO_RSN", "MRO_WO_ID = ?", new String[]{strWoId});
        offData.getWritableDatabase().delete("SEMS_REPAIR_WO_STY", "MRO_WO_ID = ?", new String[]{strWoId});

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

        for (int i = 0; i < arTempWoRsn.size(); i++) {
            ContentValues cvInsert = new ContentValues();
            cvInsert.put("MRO_WO_ID", arTempWoRsn.get(i).get("MRO_WO_ID"));
            cvInsert.put("FAIL_REASON_ID", arTempWoRsn.get(i).get("FAIL_REASON_ID"));
            cvInsert.put("FAIL_REASON_NAME", arTempWoRsn.get(i).get("FAIL_REASON_NAME"));
            cvInsert.put("FAIL_REASON_CMT", arTempWoRsn.get(i).get("FAIL_REASON_CMT"));

            offData.getWritableDatabase().insert("SEMS_REPAIR_WO_RSN", null, cvInsert);
        }

        for (int i = 0; i < arTempWoSty.size(); i++) {
            ContentValues cvInsert = new ContentValues();
            cvInsert.put("MRO_WO_ID", arTempWoSty.get(i).get("MRO_WO_ID"));
            cvInsert.put("FAIL_STRATEGY_ID", arTempWoSty.get(i).get("FAIL_STRATEGY_ID"));
            cvInsert.put("FAIL_STRATEGY_NAME", arTempWoSty.get(i).get("FAIL_STRATEGY_NAME"));
            cvInsert.put("FAIL_STRATEGY_CMT", arTempWoSty.get(i).get("FAIL_STRATEGY_CMT"));

            offData.getWritableDatabase().insert("SEMS_REPAIR_WO_STY", null, cvInsert);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isNeedUpdate) {
            this.finish();
            return false;
        }

        try {
            android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(WorkRepairActivity.this);
            alertDialog.setMessage(getResources().getString(R.string.WHETHER_TO_SAVE_CHANGE));
            alertDialog.setPositiveButton(getResources().getString(R.string.YES), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    WorkRepairActivity.this.finish();
                }
            });

            alertDialog.setNegativeButton(getResources().getString(R.string.NO), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    UndoDataToTemp(); //將工單復原回未作業時的樣子
                    WorkRepairActivity.this.finish();
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
            WorkRepairActivity.this.finish();
            return;
        }

        android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(WorkRepairActivity.this);
        alertDialog.setMessage(getResources().getString(R.string.WHETHER_TO_SAVE_CHANGE));
        alertDialog.setPositiveButton(getResources().getString(R.string.YES), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                WorkRepairActivity.this.finish();
            }
        });

        alertDialog.setNegativeButton(getResources().getString(R.string.NO), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UndoDataToTemp(); //將工單復原回未作業時的樣子
                WorkRepairActivity.this.finish();
                return;
            }
        });

        alertDialog.show();
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

    private void GetRepairWoInfo() {
        //Binding 畫面上的資訊
        pager.setAdapter(new WorkFragment(getSupportFragmentManager()));
        pager.getAdapter().notifyDataSetChanged();
    }

    public class WorkFragment extends FragmentPagerAdapter {
        public WorkFragment(FragmentManager fm) {
            super(fm);
        }

        String[] arTitle = {"FAIL", "FAIL_REASON", "FAIL_STRATEGY", "EQP_PART", "FILE_UPLOAD", "USER_WH", "PART_TRX"};

        public CharSequence getPageTitle(int position) {
            String title = "";
            switch (position) {
                case 0:
                    title = getResources().getString(R.string.FAIL);
                    break;
                case 1:
                    title = getResources().getString(R.string.FAIL_REASON);
                    break;
                case 2:
                    title = getResources().getString(R.string.FAIL_STRATEGY);
                    break;
                case 3:
                    title = getResources().getString(R.string.EQP_PART);
                    break;
                case 4:
                    title = getResources().getString(R.string.FILE_ATTACHMENT);
                    break;
                case 5:
                    title = getResources().getString(R.string.USER_WH);
                    break;
                case 6:
                    title = getResources().getString(R.string.MODIFY_PART_RECORD);
                    break;
            }

            return title;
        }

        @Override
        public Fragment getItem(int position) {
            return WorkRepairFragment.newInstance(arTitle[position], strWoId, strEqpId);
        }

        @Override
        public int getCount() {
            return arTitle.length;
        }
    }

    public boolean GetIsWork() {
        return isWork;
    }
}
