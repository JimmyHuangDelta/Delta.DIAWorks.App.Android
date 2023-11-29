package com.delta.android.PMS.Client.Fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.DateTimeKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.delta.android.Core.Activity.ShowMessageEvent;
import com.delta.android.Core.Common.CheckPermission;
import com.delta.android.Core.Common.Global;
import com.delta.android.PMS.Client.Adapter.WoUserWhAdapter;
import com.delta.android.PMS.Client.WorkPmActivity;
import com.delta.android.PMS.Client.WorkRepairActivity;
import com.delta.android.PMS.Common.FileUtils;
import com.delta.android.PMS.Common.PreventButtonMultiClick;
import com.delta.android.PMS.OffLineData.Data;
import com.delta.android.R;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class WorkRepairFragment extends Fragment {

    //    private String title;
    private String woId;
    private String eqpId;
    SimpleDateFormat df; //紀錄日期格試
    Data offData;
    SimpleCursorAdapter adapter;
    SimpleCursorAdapter partAdapter; //零件adapter
    SimpleCursorAdapter storageAdapter; //倉庫adapter
    SimpleCursorAdapter binAdapter; //儲位adapter
    String uploadFileLocalPath; //紀錄上傳的檔案來源
    EditText edFileName; //紀錄檔案名稱
    ListView lsFileUpload;

    public static WorkRepairFragment newInstance(String pageTitle, String woId, String eqpId) {
        WorkRepairFragment fragment = new WorkRepairFragment();
        Bundle args = new Bundle();
        args.putString("TITLE", pageTitle);
        args.putString("WO_ID", woId);
        args.putString("EQP_ID", eqpId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        df = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        FrameLayout frameLayout = new FrameLayout(getActivity());
        frameLayout.setLayoutParams(params);
        frameLayout.setEnabled(false);

        final String title = getArguments().getString("TITLE");
        woId = getArguments().getString("WO_ID");
        eqpId = getArguments().getString("EQP_ID");
        offData = new Data(getContext());
        final WorkRepairActivity activity = (WorkRepairActivity) getActivity();

        switch (title) {

            case "FAIL":
                //region
                View viewFail = getLayoutInflater().inflate(R.layout.activity_pms_fail, null);
                final ListView lsFail = (ListView) viewFail.findViewById(R.id.lvFail);
                SQLiteDatabase dbQueryFail = offData.getReadableDatabase();
                Cursor csFail = dbQueryFail.query("SEMS_REPAIR_FAIL", null, " MRO_WO_ID =? ", new String[]{woId}, null, null, null);
                csFail.moveToFirst();
                SimpleCursorAdapter failAdapter = new SimpleCursorAdapter(
                        getContext(),
                        R.layout.activity_pms_repair_fail_listview,
                        csFail,
                        new String[]{"FAIL_ID", "FAIL_NAME", "FAIL_CMT"},
                        new int[]{R.id.tvIdValue,
                                R.id.tvNameValue,
                                R.id.tvCmtValue},
                        0
                );
                lsFail.setAdapter(failAdapter);
                dbQueryFail.close();

                frameLayout.addView(viewFail);
                //endregion
                break;

            case "PART_TRX":
                //region
                View viewPartTrx = getLayoutInflater().inflate(R.layout.activity_pms_part_trx, null);

                final ListView lsPartTrx = (ListView) viewPartTrx.findViewById(R.id.lvPartTrx);
                SQLiteDatabase dbQueryPartTrx = offData.getReadableDatabase();
                Cursor csPartTrx = dbQueryPartTrx.query("SEMS_MRO_PART_TRX", null, " MRO_WO_ID =? ", new String[]{woId}, null, null, "TRX_DATE DESC");
                csPartTrx.moveToFirst();
                SimpleCursorAdapter partTrxAdapter = new SimpleCursorAdapter(
                        getContext(),
                        R.layout.activity_pms_part_trx_listview,
                        csPartTrx,
                        new String[]{"TRX_DATE", "TRX_MODE", "PART_ID", "PART_LOT_ID", "PART_QTY", "STORAGE_ID", "BIN_ID", "CMT"},
                        new int[]{R.id.tvTrxPartDate, R.id.tvtrxPartMode, R.id.tvTrxPartId, R.id.tvtrxPartLot, R.id.tvTrxPartQty, R.id.tvTrxPartStorage, R.id.tvTrxPartBin, R.id.tvTrxPartCmt},
                        0
                );
                lsPartTrx.setAdapter(partTrxAdapter);
                dbQueryPartTrx.close();

                Button btnUndo = (Button) viewPartTrx.findViewById(R.id.btnUndoPreTrx);
                if (!activity.GetIsWork()) {
                    btnUndo.getBackground().setAlpha(50);
                }

                btnUndo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!activity.GetIsWork()) {
                            ((WorkRepairActivity) getActivity()).ShowMessage(getResources().getString(R.string.EAPE106023));
                            return;
                        }

                        if (PreventButtonMultiClick.isFastDoubleClick()) {
                            Toast.makeText(getContext(), getResources().getString(R.string.NOT_ALLOW_MUTI_CLICK), Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(getContext(), 0)
                                    .setCancelable(false)
                                    .setMessage(getResources().getString(R.string.UNDO_PRE_TRX) + "?")
                                    .setPositiveButton(getResources().getString(R.string.CONFIRM), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            //找出最新一筆資料
                                            Cursor csLatest = offData.getReadableDatabase().query("SEMS_MRO_PART_TRX", null, "IS_NEW = 'Y'", null, null, null, "TRX_DATE DESC");
                                            csLatest.moveToFirst();

                                            if (csLatest.getCount() <= 0) {
                                                ((WorkRepairActivity) getActivity()).ShowMessage(getResources().getString(R.string.EAPE106028));
                                                return;
                                            }

                                            String undoPartId = csLatest.getString(csLatest.getColumnIndex("PART_ID"));
                                            String undoPartLot = csLatest.getString(csLatest.getColumnIndex("PART_LOT_ID"));
                                            String undoStorage = csLatest.getString(csLatest.getColumnIndex("STORAGE_ID"));
                                            String undoBin = csLatest.getString(csLatest.getColumnIndex("BIN_ID"));
                                            int undoPartQty = csLatest.getInt(csLatest.getColumnIndex("PART_QTY"));
                                            String undoTrxDate = csLatest.getString(csLatest.getColumnIndex("TRX_DATE"));
                                            String undoTrxMode = csLatest.getString(csLatest.getColumnIndex("TRX_MODE"));

                                            //修改上機零件資訊
                                            Cursor csEqpPart = offData.getReadableDatabase().query("SEMS_EQP_PART", null, "PART_ID = ? AND PART_LOT_ID = ? AND EQP_ID = ?",
                                                    new String[]{undoPartId, undoPartLot, eqpId}, null, null, null);
                                            csEqpPart.moveToFirst();

                                            if (undoTrxMode.toUpperCase().contentEquals("MOUNT")) { //如果是Mount 要把上機的資料下機
                                                if (csEqpPart.getInt(csEqpPart.getColumnIndex("PART_QTY")) == undoPartQty) {//如果要復原的數量 = 現在，砍掉這一筆資料
                                                    offData.getWritableDatabase().delete("SEMS_EQP_PART", "EQP_ID = ? AND PART_ID = ? AND PART_LOT_ID = ?"
                                                            , new String[]{eqpId, undoPartId, undoPartLot});
                                                } else { //如果復原上機紀錄後，數量 > 0 ，只做更新的動作
                                                    ContentValues cvUpdate = new ContentValues();
                                                    cvUpdate.put("PART_QTY", csEqpPart.getInt(csEqpPart.getColumnIndex("PART_QTY")) - undoPartQty);
                                                    offData.getWritableDatabase().update("SEMS_EQP_PART", cvUpdate, "EQP_ID = ? AND PART_ID = ? AND PART_LOT_ID = ?"
                                                            , new String[]{eqpId, undoPartId, undoPartLot});
                                                }
                                            } else { //要把下機紀錄復原
                                                if (csEqpPart.getCount() <= 0) { //如果機台零件的table內沒有這個零件的資料，要新增一筆紀錄
                                                    ContentValues cvInsert = new ContentValues();
                                                    cvInsert.put("EQP_ID", eqpId);
                                                    cvInsert.put("PART_ID", undoPartId);
                                                    cvInsert.put("PART_LOT_ID", undoPartLot);
                                                    cvInsert.put("PART_QTY", undoPartQty);
                                                    offData.getWritableDatabase().insert("SEMS_EQP_PART", null, cvInsert);
                                                } else { //更新數量
                                                    ContentValues cvUpdate = new ContentValues();
                                                    cvUpdate.put("PART_QTY", csEqpPart.getInt(csEqpPart.getColumnIndex("PART_QTY")) + undoPartQty);
                                                    offData.getWritableDatabase().update("SEMS_EQP_PART", cvUpdate, "EQP_ID = ? AND PART_ID = ? AND PART_LOT_ID = ? ",
                                                            new String[]{eqpId, undoPartId, undoPartLot});
                                                }
                                            }

                                            //刪除最新一筆交易紀錄
                                            offData.getWritableDatabase().delete("SEMS_MRO_PART_TRX"
                                                    , "IS_NEW = 'Y' AND PART_ID = ? AND PART_LOT_ID = ? AND STORAGE_ID = ? AND BIN_ID = ? AND PART_QTY = ? AND TRX_DATE = ?"
                                                    , new String[]{undoPartId, undoPartLot, undoStorage, undoBin, Integer.toString(undoPartQty), undoTrxDate});


                                            //重新Binding畫面資訊
                                            SQLiteDatabase dbQueryPartTrx = offData.getReadableDatabase();
                                            Cursor csPartTrx = dbQueryPartTrx.query("SEMS_MRO_PART_TRX", null, " MRO_WO_ID =? ", new String[]{woId}, null, null, "TRX_DATE DESC");
                                            csPartTrx.moveToFirst();
                                            SimpleCursorAdapter partTrxAdapter = new SimpleCursorAdapter(
                                                    getContext(),
                                                    R.layout.activity_pms_part_trx_listview,
                                                    csPartTrx,
                                                    new String[]{"TRX_DATE", "TRX_MODE", "PART_ID", "PART_LOT_ID", "PART_QTY", "STORAGE_ID", "BIN_ID", "CMT"},
                                                    new int[]{R.id.tvTrxPartDate, R.id.tvtrxPartMode, R.id.tvTrxPartId, R.id.tvtrxPartLot, R.id.tvTrxPartQty, R.id.tvTrxPartStorage, R.id.tvTrxPartBin, R.id.tvTrxPartCmt},
                                                    0
                                            );
                                            lsPartTrx.setAdapter(partTrxAdapter);
                                            dbQueryPartTrx.close();
                                        }
                                    }).setNegativeButton(getResources().getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            return;
                                        }
                                    });

                            android.support.v7.app.AlertDialog alertDialog = alert.create();
                            alertDialog.show();
                        }
                    }
                });

                frameLayout.addView(viewPartTrx);
                //endregion
                break;

            case "USER_WH":
                //region
                View viewUserWh = getLayoutInflater().inflate(R.layout.activity_pms_work_user, null);

                final ListView lsUserWh = (ListView) viewUserWh.findViewById(R.id.lvUserWH);
                BindUserWh(lsUserWh);

                //region 刪除維修人員
                /*lsUserWh.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        if (!activity.GetIsWork()) {
                            ((WorkRepairActivity) getActivity()).ShowMessage(getResources().getString(R.string.EAPE106023));
                            return false;
                        }

                        final String whUserId = ((Cursor) lsUserWh.getAdapter().getItem(position)).getString(((Cursor) lsUserWh.getAdapter().getItem(position)).getColumnIndex("USER_ID"));
                        android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(getContext(), 0)
                                .setTitle(getResources().getString(R.string.DELETE_USER))
                                .setCancelable(false)
                                .setMessage(getResources().getString(R.string.DELETE_THIS_FILE))
                                .setPositiveButton(getResources().getString(R.string.CONFIRM), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        offData.getWritableDatabase().delete("SEMS_MRO_WO_WH"
                                                , "MRO_WO_ID = ? AND USER_ID = ?"
                                                , new String[]{woId, whUserId});

                                        BindUserWh(lsUserWh);
                                    }
                                }).setNegativeButton(getResources().getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        return;
                                    }
                                });

                        android.support.v7.app.AlertDialog alertDialog = alert.create();
                        alertDialog.show();

                        return false;
                    }
                });*/
                //endregion

                //region 新增維修人員
                ImageButton btnAddUser = (ImageButton) viewUserWh.findViewById(R.id.btnAddUserWh);
                btnAddUser.setImageResource(R.mipmap.add);
                if (!activity.GetIsWork()) {
                    btnAddUser.getBackground().setAlpha(50);
                }

                btnAddUser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!activity.GetIsWork()) {
                            ((WorkRepairActivity) getActivity()).ShowMessage(getResources().getString(R.string.EAPE106023));
                            return;
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        View view = getLayoutInflater().inflate(R.layout.style_pms_dialog_user_wh, null);
                        builder.setView(view);
                        final AlertDialog dialog = builder.create();
                        dialog.show();
                        dialog.setCancelable(false);

                        final EditText edFailWhUser = (EditText) view.findViewById(R.id.edUserWhCmt);

                        final Spinner spUserId = (Spinner) view.findViewById(R.id.spWorkUserId);
                        SQLiteDatabase dbQueryUser = offData.getReadableDatabase();
                        Cursor csUser = dbQueryUser.query("SBRM_EMS_EQP_SUB_ROLE", new String[]{"USER_ID,USER_NAME,USER_ID || '_' || USER_NAME as IDNAME,_id"}, "EQP_ID = ? and TECH_TYPE = 'Repair'", new String[]{eqpId}, null, null, null);
                        csUser.moveToFirst();
                        adapter = new SimpleCursorAdapter(
                                getContext(),
                                android.R.layout.simple_list_item_1,
                                csUser,
                                new String[]{"USER_ID", "IDNAME"},
                                new int[]{0, android.R.id.text1},
                                0
                        );
                        spUserId.setAdapter(adapter);
                        dbQueryUser.close();

                        final String[] strUserId = {""};
                        final String[] strUserName = {""};
                        spUserId.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                strUserId[0] = adapter.getCursor().getString(adapter.getCursor().getColumnIndex("USER_ID"));
                                strUserName[0] = adapter.getCursor().getString(adapter.getCursor().getColumnIndex("USER_NAME"));
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        //取得當前日期時間
                        final Calendar c = Calendar.getInstance();
                        StringBuffer sbNowDate = new StringBuffer();
                        sbNowDate.append(String.format("%d/%02d/%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)));
                        StringBuffer sbNowTime = new StringBuffer();
                        sbNowTime.append(String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)));

                        final EditText edUserEndDate = (EditText) view.findViewById(R.id.edUserSEndDate);
                        edUserEndDate.setText(sbNowDate);
                        edUserEndDate.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                View view = getLayoutInflater().inflate(R.layout.style_wms_dialog_date_picker_spinner, null);
                                final DatePicker datePicker = view.findViewById(R.id.date_picker);

                                Calendar cal = Calendar.getInstance();
                                cal.setTimeInMillis(System.currentTimeMillis());
                                datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);

                                builder.setView(view);
                                builder.setTitle(R.string.SELECT_WORK_END_DATE);

                                final AlertDialog dialog = builder.create();
                                dialog.show();

                                Button bConfirm = view.findViewById(R.id.btnConfirm);
                                bConfirm.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //日期格式
                                        StringBuffer sb = new StringBuffer();
                                        sb.append(String.format("%d/%02d/%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth()));
                                        edUserEndDate.setText(sb);
                                        dialog.cancel();
                                    }
                                });
                            }
                        });

                        final EditText edUserEndTime = (EditText) view.findViewById(R.id.edUserSEndTime);
                        edUserEndTime.setText(sbNowTime);
                        edUserEndTime.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final Calendar c = Calendar.getInstance();
                                int mHour = c.get(Calendar.HOUR_OF_DAY);
                                int mMinute = c.get(Calendar.MINUTE);

                                // Launch Time Picker Dialog
                                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                                        if (hourOfDay < 10 && minute < 10) {
                                            edUserEndTime.setText("0" + hourOfDay + ":0" + minute );
                                        } else if (hourOfDay < 10 && minute >= 10) {
                                            edUserEndTime.setText("0" + hourOfDay + ":" + minute);
                                        } else if (hourOfDay >= 10 && minute < 10) {
                                            edUserEndTime.setText(hourOfDay + ":0" + minute );
                                        } else {
                                            edUserEndTime.setText(hourOfDay + ":" + minute);
                                        }
                                    }
                                }, mHour, mMinute, true);
                                timePickerDialog.show();
                            }
                        });

                        //起始時間預設為當前時間 往前30分鐘
                        if (c.get(Calendar.MINUTE) < 30 && c.get(Calendar.HOUR_OF_DAY) == 0) {
                            //日期往前一天，小時23，分鐘+30
                            c.set(Calendar.DATE, c.get(Calendar.DATE) - 1);

                            sbNowDate = new StringBuffer();
                            sbNowDate.append(String.format("%d/%02d/%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)));

                            //時間+30分鐘
                            sbNowTime = new StringBuffer();
                            sbNowTime.append(String.format("%02d:%02d", 23, c.get(Calendar.MINUTE) + 30));
                        } else if (c.get(Calendar.MINUTE) < 30) {
                            //時間 +30分鐘，小時 -1
                            sbNowTime = new StringBuffer();
                            sbNowTime.append(String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY) - 1, c.get(Calendar.MINUTE) + 30));
                        } else {
                            //時間 -30分鐘
                            sbNowTime = new StringBuffer();
                            sbNowTime.append(String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE) - 30));
                        }

                        final EditText edUserStartDate = (EditText) view.findViewById(R.id.edUserStartDate);
                        edUserStartDate.setText(sbNowDate);
                        edUserStartDate.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
                                        edUserStartDate.setText(sb);
                                        dialog.cancel();
                                    }
                                });
                            }
                        });

                        final EditText edUserStartTime = (EditText) view.findViewById(R.id.edUserStartTime);
                        edUserStartTime.setText(sbNowTime);
                        edUserStartTime.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int mHour = c.get(Calendar.HOUR_OF_DAY);
                                int mMinute = c.get(Calendar.MINUTE);

                                // Launch Time Picker Dialog
                                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                                        if (hourOfDay < 10 && minute < 10) {
                                            edUserStartTime.setText("0" + hourOfDay + ":0" + minute);
                                        } else if (hourOfDay < 10 && minute >= 10) {
                                            edUserStartTime.setText("0" + hourOfDay + ":" + minute);
                                        } else if (hourOfDay >= 10 && minute < 10) {
                                            edUserStartTime.setText(hourOfDay + ":0" + minute);
                                        } else {
                                            edUserStartTime.setText(hourOfDay + ":" + minute);
                                        }
                                    }
                                }, mHour, mMinute, true);
                                timePickerDialog.show();
                            }
                        });


                        setEditTextReadOnly(edUserStartDate);
                        setEditTextReadOnly(edUserStartTime);
                        setEditTextReadOnly(edUserEndDate);
                        setEditTextReadOnly(edUserEndTime);

                        //按下彈框的確認紐
                        Button btnConfirm = (Button) view.findViewById(R.id.btnConfirm);
                        btnConfirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //檢查人員代碼是否有輸入
                                if (strUserId[0].contentEquals("")) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE106001), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (edUserStartDate.getText().toString().contentEquals("")) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE106002), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (edUserStartTime.getText().toString().contentEquals("")) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE106003), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (edUserEndDate.getText().toString().contentEquals("")) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE106004), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (edUserEndTime.getText().toString().contentEquals("")) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE106005), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                //檢查輸入的時間結束時間是否大於起始時間
                                Date start = new Date();
                                try {
                                    start = df.parse(edUserStartDate.getText().toString() + " " + edUserStartTime.getText().toString() + ":00");
                                } catch (Exception ex) {
                                    ex.getCause();
                                    return;
                                }

                                Date end = new Date();
                                try {
                                    end = df.parse(edUserEndDate.getText().toString() + " " + edUserEndTime.getText().toString() + ":00");
                                } catch (Exception ex) {
                                    ex.getCause();
                                    return;
                                }

                                int comResult = start.compareTo(end);
                                if (comResult >= 0) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE106006), Toast.LENGTH_LONG).show();
                                    edUserEndDate.setText("");
                                    edUserEndTime.setText("");
                                    return;
                                }

                                //將輸入的資料寫道sqlIte
                                boolean isExists = false; //預設不存在
                                SQLiteDatabase dbRead = offData.getReadableDatabase();
                                Cursor cursor = dbRead.query("SEMS_MRO_WO_WH", null, "USER_ID = ? AND MRO_WO_ID = ?", new String[]{strUserId[0], woId}, null, null, null);
                                if (cursor.getCount() > 0) {
                                    isExists = true;
                                } else {
                                    isExists = false;
                                }
                                dbRead.close();

                                SQLiteDatabase dbUpdate = offData.getWritableDatabase();
                                if (isExists) {
                                    //資料庫已經存在這一筆資料。Update資料
                                    ContentValues cValues = new ContentValues();
                                    cValues.put("START_DT", edUserStartDate.getText().toString() + " " + edUserStartTime.getText().toString() + ":00");
                                    cValues.put("END_DT", edUserEndDate.getText().toString() + " " + edUserEndTime.getText().toString() + ":00");
                                    cValues.put("CMT", edFailWhUser.getText().toString());
                                    dbUpdate.update("SEMS_MRO_WO_WH", cValues, "USER_ID =? AND MRO_WO_ID = ?", new String[]{strUserId[0], woId});
                                } else {
                                    //Insert資料
                                    ContentValues cValues = new ContentValues();
                                    cValues.put("MRO_WO_ID", woId);
                                    cValues.put("USER_ID", strUserId[0]);
                                    cValues.put("USER_NAME", strUserName[0]);
                                    cValues.put("START_DT", edUserStartDate.getText().toString() + " " + edUserStartTime.getText().toString() + ":00");
                                    cValues.put("END_DT", edUserEndDate.getText().toString() + " " + edUserEndTime.getText().toString() + ":00");
                                    cValues.put("CMT", edFailWhUser.getText().toString());
                                    dbUpdate.insert("SEMS_MRO_WO_WH", null, cValues);
                                }
                                dbUpdate.close();
                                BindUserWh(lsUserWh); //重新綁定listView

                                //關閉視窗
                                dialog.hide();
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
                    }
                });
                //endregion

                frameLayout.addView(viewUserWh);
                //endregion
                break;

            case "FAIL_REASON":
                //region
                View viewReason = getLayoutInflater().inflate(R.layout.activity_pms_work_fail_reason, null);

                final ListView lsFailRsn = (ListView) viewReason.findViewById(R.id.lvFailReason);
                BindFailRsn(lsFailRsn);

                //region 刪除故障原因
                lsFailRsn.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        if (!activity.GetIsWork()) {
                            ((WorkRepairActivity) getActivity()).ShowMessage(getResources().getString(R.string.EAPE106023));
                            return false;
                        }

                        final String failRsnId = ((Cursor) lsFailRsn.getAdapter().getItem(position)).getString(((Cursor) lsFailRsn.getAdapter().getItem(position)).getColumnIndex("FAIL_REASON_ID"));
                        android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(getContext(), 0)
                                .setTitle(getResources().getString(R.string.DELETE_FAIL_REASON))
                                .setCancelable(false)
                                .setMessage(getResources().getString(R.string.DELETE_THIS_FILE))
                                .setPositiveButton(getResources().getString(R.string.CONFIRM), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        offData.getWritableDatabase().delete("SEMS_REPAIR_WO_RSN"
                                                , "MRO_WO_ID = ? AND FAIL_REASON_ID = ?"
                                                , new String[]{woId, failRsnId});

                                        BindFailRsn(lsFailRsn);
                                    }
                                }).setNegativeButton(getResources().getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        return;
                                    }
                                });

                        android.support.v7.app.AlertDialog alertDialog = alert.create();
                        alertDialog.show();

                        return false;
                    }
                });
                //endregion

                //region 新增故障原因
                ImageButton btnAddReason = (ImageButton) viewReason.findViewById(R.id.btnAddFailReason);
                btnAddReason.setImageResource(R.mipmap.add);
                if (!activity.GetIsWork()) {
                    btnAddReason.getBackground().setAlpha(50);
                }

                btnAddReason.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!activity.GetIsWork()) {
                            ((WorkRepairActivity) getActivity()).ShowMessage(getResources().getString(R.string.EAPE106023));
                            return;
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        View view = getLayoutInflater().inflate(R.layout.style_pms_dialog_fail_reason, null);
                        builder.setView(view);
                        final AlertDialog dialog = builder.create();
                        dialog.show();
                        dialog.setCancelable(false);

                        final EditText edFailRsnCmt = (EditText) view.findViewById(R.id.edFailRsnCmt);

                        final Spinner spFailRsn = (Spinner) view.findViewById(R.id.spFailRsn);
                        SQLiteDatabase dbQueryUser = offData.getReadableDatabase();
                        Cursor csFailRsn = dbQueryUser.query("SBRM_EMS_FAIL_REASON", new String[]{"FAIL_REASON_ID as _id", "FAIL_REASON_NAME"}, null, null, null, null, null);
                        csFailRsn.moveToFirst();
                        adapter = new SimpleCursorAdapter(
                                getContext(),
                                android.R.layout.simple_list_item_2,
                                csFailRsn,
                                new String[]{"_id", "FAIL_REASON_NAME"},
                                new int[]{android.R.id.text2, android.R.id.text1},
                                0
                        );
                        spFailRsn.setAdapter(adapter);
                        dbQueryUser.close();

                        final String[] strRsnId = {""};
                        final String[] strRsnName = {""};
                        spFailRsn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                strRsnId[0] = adapter.getCursor().getString(0);
                                strRsnName[0] = adapter.getCursor().getString(1);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        //按下彈框的確認紐
                        Button btnConfirm = (Button) view.findViewById(R.id.btnConfirm);
                        btnConfirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (strRsnId[0].contentEquals("")) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE106007), Toast.LENGTH_LONG).show();
                                    return;
                                }

//                                if (edFailRsnCmt.getText().toString().contentEquals("")) {
//                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE106025), Toast.LENGTH_SHORT).show();
//                                    return;
//                                }

                                //將輸入的資料寫道sqlIte
                                boolean isExists = false; //預設不存在
                                SQLiteDatabase dbRead = offData.getReadableDatabase();
                                Cursor cursor = dbRead.query("SEMS_REPAIR_WO_RSN", null, "FAIL_REASON_ID = ? AND MRO_WO_ID = ?", new String[]{strRsnId[0], woId}, null, null, null);
                                if (cursor.getCount() > 0) {
                                    isExists = true;
                                } else {
                                    isExists = false;
                                }
                                dbRead.close();

                                SQLiteDatabase dbUpdate = offData.getWritableDatabase();
                                if (isExists) {
                                    //資料庫已經存在這一筆資料。Update資料
                                    ContentValues cValues = new ContentValues();
                                    cValues.put("FAIL_REASON_CMT", edFailRsnCmt.getText().toString());
                                    dbUpdate.update("SEMS_REPAIR_WO_RSN", cValues, "FAIL_REASON_ID =? AND MRO_WO_ID = ?", new String[]{strRsnId[0], woId});
                                } else {
                                    //Insert資料
                                    ContentValues cValues = new ContentValues();
                                    cValues.put("MRO_WO_ID", woId);
                                    cValues.put("FAIL_REASON_ID", strRsnId[0]);
                                    cValues.put("FAIL_REASON_NAME", strRsnName[0]);
                                    cValues.put("FAIL_REASON_CMT", edFailRsnCmt.getText().toString());
                                    dbUpdate.insert("SEMS_REPAIR_WO_RSN", null, cValues);
                                }
                                dbUpdate.close();
                                BindFailRsn(lsFailRsn);
                                //關閉視窗
                                dialog.hide();
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
                    }
                });
                //endregion

                frameLayout.addView(viewReason);
                //endregion
                break;

            case "FAIL_STRATEGY":
                //region
                View viewStrategy = getLayoutInflater().inflate(R.layout.activity_pms_work_fail_strategy, null);

                final ListView lsFailSty = (ListView) viewStrategy.findViewById(R.id.lvFailStrategy);
                BindFailSty(lsFailSty);
                ImageButton btnAddStrategy = (ImageButton) viewStrategy.findViewById(R.id.btnAddFailStrategy);
                btnAddStrategy.setImageResource(R.mipmap.add);
                if (!activity.GetIsWork()) {
                    btnAddStrategy.getBackground().setAlpha(50);
                }

                //region 刪除故障處置
                lsFailSty.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        if (!activity.GetIsWork()) {
                            ((WorkRepairActivity) getActivity()).ShowMessage(getResources().getString(R.string.EAPE106023));
                            return false;
                        }

                        final String failStyId = ((Cursor) lsFailSty.getAdapter().getItem(position)).getString(((Cursor) lsFailSty.getAdapter().getItem(position)).getColumnIndex("FAIL_STRATEGY_ID"));
                        android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(getContext(), 0)
                                .setTitle(getResources().getString(R.string.DELETE_FAIL_STRATEGY))
                                .setCancelable(false)
                                .setMessage(getResources().getString(R.string.DELETE_THIS_FILE))
                                .setPositiveButton(getResources().getString(R.string.CONFIRM), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        offData.getWritableDatabase().delete("SEMS_REPAIR_WO_STY"
                                                , "MRO_WO_ID = ? AND FAIL_STRATEGY_ID = ?"
                                                , new String[]{woId, failStyId});

                                        BindFailSty(lsFailSty);
                                    }
                                }).setNegativeButton(getResources().getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        return;
                                    }
                                });

                        android.support.v7.app.AlertDialog alertDialog = alert.create();
                        alertDialog.show();

                        return false;
                    }
                });
                //endregion

                //region 新增不良處置
                btnAddStrategy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!activity.GetIsWork()) {
                            ((WorkRepairActivity) getActivity()).ShowMessage(getResources().getString(R.string.EAPE106023));
                            return;
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        View view = getLayoutInflater().inflate(R.layout.style_pms_dialog_fail_strategy, null);
                        builder.setView(view);

                        final EditText edFailStyCmt = (EditText) view.findViewById(R.id.edFailStyCmt);
                        final Spinner spFailSty = (Spinner) view.findViewById(R.id.spFailSty);

                        SQLiteDatabase dbQuerySty = offData.getReadableDatabase();
                        Cursor csFailSty = dbQuerySty.query("SBRM_EMS_FAIL_STRATEGY", new String[]{"FAIL_STRATEGY_ID as _id", "FAIL_STRATEGY_NAME"}, null, null, null, null, null);
                        csFailSty.moveToFirst();
                        adapter = new SimpleCursorAdapter(
                                getContext(),
                                android.R.layout.simple_list_item_2,
                                csFailSty,
                                new String[]{"_id", "FAIL_STRATEGY_NAME"},
                                new int[]{android.R.id.text2, android.R.id.text1},
                                0
                        );
                        spFailSty.setAdapter(adapter);
                        dbQuerySty.close();

                        final String[] strStyId = {""};
                        final String[] strStyName = {""};
                        spFailSty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                strStyId[0] = adapter.getCursor().getString(0);
                                strStyName[0] = adapter.getCursor().getString(1);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        final AlertDialog dialog = builder.create();
                        dialog.show();
                        dialog.setCancelable(false);

                        //按下彈框的確認紐
                        Button btnConfirm = (Button) view.findViewById(R.id.btnConfirm);
                        btnConfirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (strStyId[0].contentEquals("")) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE106008), Toast.LENGTH_LONG).show();
                                    return;
                                }

//                                if (edFailStyCmt.getText().toString().contentEquals("")) {
//                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE106026), Toast.LENGTH_SHORT).show();
//                                    return;
//                                }

                                //將輸入的資料寫道sqlIte
                                boolean isExists = false; //預設不存在
                                SQLiteDatabase dbRead = offData.getReadableDatabase();
                                Cursor cursor = dbRead.query("SEMS_REPAIR_WO_STY", null, "FAIL_STRATEGY_ID = ? AND MRO_WO_ID = ?", new String[]{strStyId[0], woId}, null, null, null);
                                if (cursor.getCount() > 0) {
                                    isExists = true;
                                } else {
                                    isExists = false;
                                }
                                dbRead.close();

                                SQLiteDatabase dbUpdate = offData.getWritableDatabase();
                                if (isExists) {
                                    //資料庫已經存在這一筆資料。Update資料
                                    ContentValues cValues = new ContentValues();
                                    cValues.put("FAIL_STRATEGY_CMT", edFailStyCmt.getText().toString());
                                    dbUpdate.update("SEMS_REPAIR_WO_STY", cValues, "FAIL_STRATEGY_ID =? AND MRO_WO_ID = ?", new String[]{strStyId[0], woId});
                                } else {
                                    //Insert資料
                                    ContentValues cValues = new ContentValues();
                                    cValues.put("MRO_WO_ID", woId);
                                    cValues.put("FAIL_STRATEGY_ID", strStyId[0]);
                                    cValues.put("FAIL_STRATEGY_NAME", strStyName[0]);
                                    cValues.put("FAIL_STRATEGY_CMT", edFailStyCmt.getText().toString());
                                    dbUpdate.insert("SEMS_REPAIR_WO_STY", null, cValues);
                                }
                                dbUpdate.close();
                                BindFailSty(lsFailSty);
                                //關閉視窗
                                dialog.hide();
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
                    }
                });
                //endregion

                frameLayout.addView(viewStrategy);
                //endregion
                break;

            case "EQP_PART":
                //region
                View viewEqpPart = getLayoutInflater().inflate(R.layout.activity_pms_work_eqp_part, null);

                final ListView lsEqpPart = (ListView) viewEqpPart.findViewById(R.id.lvEqpPart); //機台上的零件
                BindEqpPart(lsEqpPart);

                //region 零件下機
                lsEqpPart.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        if (!activity.GetIsWork()) {
                            ((WorkRepairActivity) getActivity()).ShowMessage(getResources().getString(R.string.EAPE106023));
                            return false;
                        }

                        lsEqpPart.getAdapter();

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        View viewUnMount = getLayoutInflater().inflate(R.layout.style_pms_dialog_unmount_eqp_part, null);
                        builder.setView(viewUnMount);
                        builder.setTitle(getResources().getString(R.string.UNMOUNT_PART));///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////零件下機
                        final AlertDialog dialog = builder.create();
                        dialog.show();
                        dialog.setCancelable(false);

                        final String partId = ((Cursor) lsEqpPart.getAdapter().getItem(position)).getString(((Cursor) lsEqpPart.getAdapter().getItem(position)).getColumnIndex("PART_ID"));
                        final String partLotId = ((Cursor) lsEqpPart.getAdapter().getItem(position)).getString(((Cursor) lsEqpPart.getAdapter().getItem(position)).getColumnIndex("PART_LOT_ID"));
                        String partQty = ((Cursor) lsEqpPart.getAdapter().getItem(position)).getString(((Cursor) lsEqpPart.getAdapter().getItem(position)).getColumnIndex("PART_QTY"));
                        final EditText edPartId = (EditText) viewUnMount.findViewById(R.id.edPartId);
                        edPartId.setText(partId);
                        setEditTextReadOnly(edPartId);
                        final EditText edPartLot = (EditText) viewUnMount.findViewById(R.id.edPartLot);
                        edPartLot.setText(partLotId);
                        setEditTextReadOnly(edPartLot);
                        final EditText edPartQty = (EditText) viewUnMount.findViewById(R.id.edPartQty);
                        edPartQty.setText(partQty);
                        setEditTextReadOnly(edPartQty);
                        final EditText edTrxQty = (EditText) viewUnMount.findViewById(R.id.edTrxQty);

                        Spinner spDisStorage = (Spinner) viewUnMount.findViewById(R.id.spSourceStorage);
                        SQLiteDatabase dbQueryStorage = offData.getReadableDatabase();
                        Cursor csStorage = dbQueryStorage.query("SBRM_STORAGE", new String[]{"STORAGE_ID as _id", "STORAGE_NAME"},
                                "STORAGE_TYPE = 'PART_PENDING' AND USER_ID = ?", new String[]{activity.getGlobal().getUserID()},
                                null, null, null);
                        csStorage.moveToFirst();
                        storageAdapter = new SimpleCursorAdapter(
                                getContext(),
                                android.R.layout.simple_list_item_1,
                                csStorage,
                                new String[]{"_id"},
                                new int[]{android.R.id.text1},
                                0
                        );
                        spDisStorage.setAdapter(storageAdapter);
                        dbQueryStorage.close();

                        final Spinner spDisBin = (Spinner) viewUnMount.findViewById(R.id.spSourceBin);
                        final String[] strStorageId = {""};
                        spDisStorage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                strStorageId[0] = storageAdapter.getCursor().getString(0);
                                SQLiteDatabase dbQueryBin = offData.getReadableDatabase();
                                Cursor csBin = dbQueryBin.query("SBRM_BIN", new String[]{"BIN_ID as _id", "BIN_NAME"}, "STORAGE_ID = ?", new String[]{strStorageId[0]}, null, null, null);
                                csBin.moveToFirst();
                                binAdapter = new SimpleCursorAdapter(
                                        getContext(),
                                        android.R.layout.simple_list_item_1,
                                        csBin,
                                        new String[]{"_id"},
                                        new int[]{android.R.id.text1},
                                        0
                                );
                                spDisBin.setAdapter(binAdapter);
                                dbQueryBin.close();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        final String[] strBinId = {""};
                        final String[] strBinName = {""};
                        spDisBin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                strBinId[0] = binAdapter.getCursor().getString(0);
                                strBinName[0] = binAdapter.getCursor().getString(1);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        final EditText edCmt = (EditText) viewUnMount.findViewById(R.id.edEqpPartCmt);
                        Date nd = new Date(System.currentTimeMillis());
                        final String strDate = df.format(nd); //紀錄當前時間

                        //按下彈框的確認紐
                        Button btnConfirm = (Button) viewUnMount.findViewById(R.id.btnConfirm);
                        btnConfirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //檢查是否有輸入下機零件數
                                int trxQty = 0;
                                try {
                                    trxQty = Integer.parseInt(edPartQty.getText().toString());
                                } catch (Exception ex) {
                                    ex.getMessage();
                                }

                                if (edTrxQty.getText().toString().contentEquals("") ||
                                        trxQty <= 0) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE106009), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                //比較下機零件是否超過線在機上數量
                                int eqpPartQty = 0; //紀錄機上的零件剩餘數
                                if (Integer.parseInt(edPartQty.getText().toString()) < Integer.parseInt(edTrxQty.getText().toString())) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE106010), Toast.LENGTH_LONG).show();
                                    return;
                                } else {
                                    eqpPartQty = Integer.parseInt(edPartQty.getText().toString()) - Integer.parseInt(edTrxQty.getText().toString());
                                }

                                //檢查倉庫、儲位是否有選擇
                                if (strStorageId[0].contentEquals("")) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE106011), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (strBinId[0].contentEquals("")) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE106012), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                //將輸入的資料寫道sqlIte
                                // 將輸入的資料寫道sqlIte (PART_TRX 、 EQP_PART)
                                SQLiteDatabase dbInsertTrx = offData.getWritableDatabase();
                                ContentValues cValues = new ContentValues();
                                cValues.put("MRO_WO_ID", woId);
                                cValues.put("PART_ID", edPartId.getText().toString());
                                cValues.put("PART_LOT_ID", edPartLot.getText().toString());
                                cValues.put("PART_QTY", edTrxQty.getText().toString());
                                cValues.put("IS_NEW", "Y");
                                cValues.put("STORAGE_ID", strStorageId[0]);
                                cValues.put("BIN_ID", strBinId[0]);
                                cValues.put("CMT", edCmt.getText().toString());
                                cValues.put("TRX_MODE", "UnMount");
                                cValues.put("TRX_DATE", strDate);
                                dbInsertTrx.insert("SEMS_MRO_PART_TRX", null, cValues);
                                dbInsertTrx.close();

                                if (eqpPartQty == 0) {
                                    offData.getWritableDatabase().delete("SEMS_EQP_PART"
                                            , "EQP_ID = ? AND PART_ID = ? AND PART_LOT_ID = ?"
                                            , new String[]{eqpId, edPartId.getText().toString(), edPartLot.getText().toString()});
                                } else {
                                    ContentValues cvUpdate = new ContentValues();
                                    cvUpdate.put("PART_QTY", eqpPartQty);
                                    offData.getWritableDatabase().update("SEMS_EQP_PART", cvUpdate,
                                            "EQP_ID = ? AND PART_ID = ? AND PART_LOT_ID = ?",
                                            new String[]{eqpId, edPartId.getText().toString(), edPartLot.getText().toString()});
                                }

                                BindEqpPart(lsEqpPart);
                                //關閉視窗
                                dialog.hide();
                            }
                        });

                        //按下彈框的取消紐
                        Button btnCancel = (Button) viewUnMount.findViewById(R.id.btnCancel);
                        btnCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.hide();
                            }
                        });

                        return true;
                    }
                });
                //endregion

                //region 零件上機
                ImageButton btnAddMountPart = (ImageButton) viewEqpPart.findViewById(R.id.btnAddEqpMountPart);
                btnAddMountPart.setImageResource(R.mipmap.add);
                if (!activity.GetIsWork()) {
                    btnAddMountPart.getBackground().setAlpha(50);
                }

                btnAddMountPart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!activity.GetIsWork()) {
                            ((WorkRepairActivity) getActivity()).ShowMessage(getResources().getString(R.string.EAPE106023));
                            return;
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        View view = getLayoutInflater().inflate(R.layout.style_pms_dialog_eqp_part, null);
                        builder.setView(view);
                        builder.setTitle(getResources().getString(R.string.ADD_MONUT_PART));
                        final AlertDialog dialog = builder.create();
                        dialog.show();
                        dialog.setCancelable(false);

                        Spinner spPartGroup = (Spinner) view.findViewById(R.id.spPartGroup);
                        SQLiteDatabase dbQueryPartGroup = offData.getReadableDatabase();
                        Cursor csPartGroup = dbQueryPartGroup.query("SBRM_PART_GROUP", new String[]{"PART_GROUP_ID as _id", "PART_GROUP_NAME"}, null, null, null, null, null);
                        csPartGroup.moveToFirst();
                        final SimpleCursorAdapter groupAdapter = new SimpleCursorAdapter(
                                getContext(),
                                android.R.layout.simple_list_item_1,
                                csPartGroup,
                                new String[]{"_id"},
                                new int[]{android.R.id.text1},
                                0
                        );
                        spPartGroup.setAdapter(groupAdapter);
                        dbQueryPartGroup.close();

                        final String[] strPartGroupId = {""};
                        final Spinner spPart = (Spinner) view.findViewById(R.id.spPart);
                        spPartGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                strPartGroupId[0] = groupAdapter.getCursor().getString(0);
                                SQLiteDatabase dbQueryPart = offData.getReadableDatabase();
                                Cursor csPart = dbQueryPart.query("SBRM_PART", new String[]{"PART_ID as _id", "PART_NAME"}, "PART_GROUP_ID = ?", new String[]{strPartGroupId[0]}, null, null, null);
                                csPart.moveToFirst();
                                partAdapter = new SimpleCursorAdapter(
                                        getContext(),
                                        android.R.layout.simple_list_item_1,
                                        csPart,
                                        new String[]{"_id"},
                                        new int[]{android.R.id.text1},
                                        0
                                );
                                spPart.setAdapter(partAdapter);
                                dbQueryPart.close();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        final String[] strPartId = {""};
                        final String[] strPartName = {""};
                        spPart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                strPartId[0] = partAdapter.getCursor().getString(0);
                                strPartName[0] = partAdapter.getCursor().getString(1);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        final EditText edPartLot = (EditText) view.findViewById(R.id.edPartLotNo);
                        final EditText edPartQty = (EditText) view.findViewById(R.id.edPartQty);

                        Spinner spSourceStorage = (Spinner) view.findViewById(R.id.spSourceStorage);
                        SQLiteDatabase dbQueryStorage = offData.getReadableDatabase();
                        Cursor csStorage = dbQueryStorage.query("SBRM_STORAGE", new String[]{"STORAGE_ID as _id", "STORAGE_NAME"},
                                "STORAGE_TYPE = 'PART' AND USER_ID = ?", new String[]{activity.getGlobal().getUserID()},
                                null, null, null);
                        csStorage.moveToFirst();
                        storageAdapter = new SimpleCursorAdapter(
                                getContext(),
                                android.R.layout.simple_list_item_1,
                                csStorage,
                                new String[]{"_id"},
                                new int[]{android.R.id.text1},
                                0
                        );
                        spSourceStorage.setAdapter(storageAdapter);
                        dbQueryStorage.close();

                        final Spinner spSourceBin = (Spinner) view.findViewById(R.id.spSourceBin);
                        final String[] strStorageId = {""};
                        spSourceStorage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                strStorageId[0] = storageAdapter.getCursor().getString(0);
                                SQLiteDatabase dbQueryBin = offData.getReadableDatabase();
                                Cursor csBin = dbQueryBin.query("SBRM_BIN", new String[]{"BIN_ID as _id", "BIN_NAME"}, "STORAGE_ID = ?", new String[]{strStorageId[0]}, null, null, null);
                                csBin.moveToFirst();
                                binAdapter = new SimpleCursorAdapter(
                                        getContext(),
                                        android.R.layout.simple_list_item_1,
                                        csBin,
                                        new String[]{"_id"},
                                        new int[]{android.R.id.text1},
                                        0
                                );
                                spSourceBin.setAdapter(binAdapter);
                                dbQueryBin.close();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        final String[] strBinId = {""};
                        final String[] strBinName = {""};
                        spSourceBin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                strBinId[0] = binAdapter.getCursor().getString(0);
                                strBinName[0] = binAdapter.getCursor().getString(1);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        final EditText edCmt = (EditText) view.findViewById(R.id.edEqpPartCmt);
                        Date nd = new Date(System.currentTimeMillis());
                        final String strDate = df.format(nd); //紀錄當前時間

                        //按下彈框的確認紐
                        Button btnConfirm = (Button) view.findViewById(R.id.btnConfirm);
                        btnConfirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (strPartId[0].contentEquals("")) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE106013), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (edPartLot.getText().toString().contentEquals("")) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE106014), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                int trxQty = 0;
                                try {
                                    trxQty = Integer.parseInt(edPartQty.getText().toString());
                                } catch (Exception ex) {
                                    ex.getMessage();
                                }

                                if (edPartQty.getText().toString().contentEquals("") ||
                                        trxQty <= 0) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE106015), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (strStorageId[0].contentEquals("")) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE106016), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (strBinId[0].contentEquals("")) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE106017), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                //檢查庫存是否存在這個批號的零件，並且數量足夠
                                Cursor csInv = offData.getReadableDatabase().query("SMTL_INVENTORY", null
                                        , "ITEM_ID = ? AND MTL_LOT_ID = ? AND STORAGE_ID = ? AND BIN_ID = ?"
                                        , new String[]{strPartId[0], edPartLot.getText().toString(), strStorageId[0], strBinId[0]}
                                        , null, null, null);
                                csInv.moveToFirst();
                                if (csInv.getCount() <= 0) {
                                    ((WorkRepairActivity) getActivity()).ShowMessage(getResources().getString(R.string.EAPE106029));
                                    return;
                                }

                                if (csInv.getInt(csInv.getColumnIndex("QTY")) < Integer.parseInt(edPartQty.getText().toString())) {
                                    ((WorkRepairActivity) getActivity()).ShowMessage(getResources().getString(R.string.EAPE106030));
                                    return;
                                }

                                //將輸入的資料寫道sqlIte (PART_TRX 、 EQP_PART)
                                SQLiteDatabase dbInsertTrx = offData.getWritableDatabase();
                                ContentValues cValues = new ContentValues();
                                cValues.put("MRO_WO_ID", woId);
                                cValues.put("PART_ID", strPartId[0]);
                                cValues.put("PART_LOT_ID", edPartLot.getText().toString());
                                cValues.put("PART_QTY", edPartQty.getText().toString());
                                cValues.put("IS_NEW", "Y");
                                cValues.put("STORAGE_ID", strStorageId[0]);
                                cValues.put("BIN_ID", strBinId[0]);
                                cValues.put("CMT", edCmt.getText().toString());
                                cValues.put("TRX_MODE", "Mount");
                                cValues.put("TRX_DATE", strDate);
                                dbInsertTrx.insert("SEMS_MRO_PART_TRX", null, cValues);
                                dbInsertTrx.close();

                                boolean isExists = false; //預設不存在
                                int partQty = 0;
                                SQLiteDatabase dbRead = offData.getReadableDatabase();
                                Cursor cursor = dbRead.query("SEMS_EQP_PART", null,
                                        "EQP_ID = ? AND PART_ID = ? AND PART_LOT_ID = ?",
                                        new String[]{eqpId, strPartId[0], edPartLot.getText().toString()},
                                        null, null, null);
                                cursor.moveToFirst();
                                if (cursor.getCount() > 0) {
                                    isExists = true;
                                    partQty = cursor.getInt(cursor.getColumnIndex("PART_QTY"));
                                } else {
                                    isExists = false;
                                }
                                dbRead.close();

                                SQLiteDatabase dbUpdate = offData.getWritableDatabase();
                                if (isExists) {
                                    partQty = partQty + Integer.parseInt(edPartQty.getText().toString());
                                    ContentValues cValuesUpdate = new ContentValues();
                                    cValuesUpdate.put("PART_QTY", partQty);
                                    cValuesUpdate.put("CMT", edCmt.getText().toString());
                                    dbUpdate.update("SEMS_EQP_PART", cValuesUpdate,
                                            "EQP_ID =? AND PART_ID = ? AND PART_LOT_ID =?",
                                            new String[]{eqpId, strPartId[0], edPartLot.getText().toString()});
                                } else {
                                    ContentValues cValuesUpdate = new ContentValues();
                                    cValuesUpdate.put("EQP_ID", eqpId);
                                    cValuesUpdate.put("PART_ID", strPartId[0]);
                                    cValuesUpdate.put("PART_LOT_ID", edPartLot.getText().toString());
                                    cValuesUpdate.put("PART_QTY", edPartQty.getText().toString());
                                    cValuesUpdate.put("CMT", edCmt.getText().toString());
                                    dbUpdate.insert("SEMS_EQP_PART", null, cValuesUpdate);
                                }

                                BindEqpPart(lsEqpPart);

                                //關閉視窗
                                dialog.hide();
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
                    }
                });
                //endregion

                frameLayout.addView(viewEqpPart);
                //endregion
                break;

            case "FILE_UPLOAD":
                View viewFileUpload = getLayoutInflater().inflate(R.layout.activity_pms_work_file_upload, null);

                lsFileUpload = (ListView) viewFileUpload.findViewById(R.id.lvFileUpload);
                ImageButton btnUpload = (ImageButton) viewFileUpload.findViewById(R.id.btnUploadFile);
                btnUpload.setImageResource(R.mipmap.add);
                if (!activity.GetIsWork()) {
                    btnUpload.getBackground().setAlpha(50);
                }

                BindUploadFileList(lsFileUpload);

                //region 刪除上傳檔案
                lsFileUpload.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        if (!activity.GetIsWork()) {
                            ((WorkRepairActivity) getActivity()).ShowMessage(getResources().getString(R.string.EAPE106023));
                            return false;
                        }

                        final String fileName = ((Cursor) lsFileUpload.getAdapter().getItem(position)).getString(((Cursor) lsFileUpload.getAdapter().getItem(position)).getColumnIndex("FILE_NAME"));
                        android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(getContext(), 0)
                                .setTitle(getResources().getString(R.string.DELETE_UPLOAD_FILE))
                                .setCancelable(false)
                                .setMessage(getResources().getString(R.string.DELETE_THIS_FILE))
                                .setPositiveButton(getResources().getString(R.string.CONFIRM), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        offData.getWritableDatabase().delete("SEMS_FILE"
                                                , "MRO_WO_ID = ? AND FILE_NAME = ?"
                                                , new String[]{woId, fileName});

                                        BindUploadFileList(lsFileUpload);
                                    }
                                }).setNegativeButton(getResources().getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        return;
                                    }
                                });

                        android.support.v7.app.AlertDialog alertDialog = alert.create();
                        alertDialog.show();

                        return false;
                    }
                });

                //endregion

                //region 新增上傳檔案
                btnUpload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!activity.GetIsWork()) {
                            ((WorkRepairActivity) getActivity()).ShowMessage(getResources().getString(R.string.EAPE106023));
                            return;
                        }

                        ArrayList<String> arReqPermission = new ArrayList<String>(); //紀錄所需的權限
                        arReqPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        arReqPermission.add(Manifest.permission.READ_EXTERNAL_STORAGE);

                        CheckPermission checkPermission = new CheckPermission(getActivity());
                        if (!checkPermission.CheckPermission(arReqPermission)) { //r檢核是否有缺少開通的權限
                            String[] unPermission = checkPermission.GetUnPermission();
                            if (unPermission != null && unPermission.length > 0) { //如果有缺少要開通的權限，談框詢問是否需要開啟
                                if (Build.VERSION.SDK_INT >= 23) { //建置版本 >23 (android 6.0以上)才需要檢查
                                    requestPermissions(unPermission, 0);
                                }
                            }
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            View view = getLayoutInflater().inflate(R.layout.style_pms_dialog_file_upload, null);
                            builder.setView(view);
                            final AlertDialog dialog = builder.create();
                            dialog.show();
                            dialog.setCancelable(false);

                            edFileName = (EditText) view.findViewById(R.id.edFileName);
                            setEditTextReadOnly(edFileName);
                            final EditText edFileDesc = (EditText) view.findViewById(R.id.edFileUploadCmt);

                            Button btnFile = (Button) view.findViewById(R.id.btnSelectFile);
                            btnFile.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent();
                                    intent.setType("*/*");
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(Intent.createChooser(intent, "Complete action using"), 1);
                                }
                            });

                            //按下彈框的確認紐
                            Button btnConfirm = (Button) view.findViewById(R.id.btnConfirm);
                            btnConfirm.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (edFileName.getText().toString().contentEquals("")) {
                                        Toast.makeText(getContext(), getResources().getString(R.string.EAPE106018), Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    boolean isExists = false; //預設不存在
                                    SQLiteDatabase dbRead = offData.getReadableDatabase();
                                    Cursor cursor = dbRead.query("SEMS_FILE", null, "FILE_NAME = ? AND MRO_WO_ID = ?", new String[]{edFileName.getText().toString(), woId}, null, null, null);
                                    if (cursor.getCount() > 0) {
                                        isExists = true;
                                    } else {
                                        isExists = false;
                                    }
                                    dbRead.close();

                                    //將輸入的資料寫道sqlIte
                                    SQLiteDatabase dbUpdate = offData.getWritableDatabase();
                                    Date nd = new Date(System.currentTimeMillis());
                                    String strDate = df.format(nd); //紀錄當前時間

                                    if (isExists) {
                                        ContentValues cValues = new ContentValues();
                                        cValues.put("FILE_DESC", edFileDesc.getText().toString());
                                        cValues.put("LOCAL_FILE_PATH", uploadFileLocalPath);
                                        cValues.put("UPLOAD_USER_ID", Global.getContext().getUserID());
                                        cValues.put("UPLOAD_DATE", strDate);
                                        dbUpdate.update("SEMS_FILE", cValues, "FILE_NAME =? AND MRO_WO_ID = ?", new String[]{edFileName.getText().toString(), woId});
                                    } else {
                                        ContentValues cValues = new ContentValues();
                                        cValues.put("MRO_WO_ID", woId);
                                        cValues.put("FILE_NAME", edFileName.getText().toString());
                                        cValues.put("FILE_DESC", edFileDesc.getText().toString());
                                        cValues.put("LOCAL_FILE_PATH", uploadFileLocalPath);
                                        cValues.put("UPLOAD_USER_ID", Global.getContext().getUserID());
                                        cValues.put("UPLOAD_DATE", strDate);
                                        dbUpdate.insert("SEMS_FILE", null, cValues);
                                    }

                                    dbUpdate.close();
                                    BindUploadFileList(lsFileUpload);

                                    //關閉視窗
                                    dialog.hide();
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
                        }
                    }
                });
                //endregion

                frameLayout.addView(viewFileUpload);
                break;
        }

        return frameLayout;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) { //等於 - 1代表有不許允開啟的權限。直接return`.
                    return;
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            View view = getLayoutInflater().inflate(R.layout.style_pms_dialog_file_upload, null);
            builder.setView(view);
            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.setCancelable(false);

            edFileName = (EditText) view.findViewById(R.id.edFileName);
            final EditText edFileDesc = (EditText) view.findViewById(R.id.edFileUploadCmt);

            Button btnFile = (Button) view.findViewById(R.id.btnSelectFile);
            btnFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setType("*/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Complete action using"), 1);
                }
            });

            //按下彈框的確認紐
            Button btnConfirm = (Button) view.findViewById(R.id.btnConfirm);
            btnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (edFileName.getText().toString().contentEquals("")) {
                        Toast.makeText(getContext(), getResources().getString(R.string.EAPE106018), Toast.LENGTH_LONG).show();
                        return;
                    }

                    boolean isExists = false; //預設不存在
                    SQLiteDatabase dbRead = offData.getReadableDatabase();
                    Cursor cursor = dbRead.query("SEMS_FILE", null, "FILE_NAME = ? AND MRO_WO_ID = ?", new String[]{edFileName.getText().toString(), woId}, null, null, null);
                    if (cursor.getCount() > 0) {
                        isExists = true;
                    } else {
                        isExists = false;
                    }
                    dbRead.close();

                    //將輸入的資料寫道sqlIte
                    SQLiteDatabase dbUpdate = offData.getWritableDatabase();
                    Date nd = new Date(System.currentTimeMillis());
                    String strDate = df.format(nd); //紀錄當前時間

                    if (isExists) {
                        ContentValues cValues = new ContentValues();
                        cValues.put("FILE_DESC", edFileDesc.getText().toString());
                        cValues.put("LOCAL_FILE_PATH", uploadFileLocalPath);
                        cValues.put("UPLOAD_USER_ID", Global.getContext().getUserID());
                        cValues.put("UPLOAD_DATE", strDate);
                        dbUpdate.update("SEMS_FILE", cValues, "FILE_NAME =? AND MRO_WO_ID = ?", new String[]{edFileName.getText().toString(), woId});
                    } else {
                        ContentValues cValues = new ContentValues();
                        cValues.put("MRO_WO_ID", woId);
                        cValues.put("FILE_NAME", edFileName.getText().toString());
                        cValues.put("FILE_DESC", edFileDesc.getText().toString());
                        cValues.put("LOCAL_FILE_PATH", uploadFileLocalPath);
                        cValues.put("UPLOAD_USER_ID", Global.getContext().getUserID());
                        cValues.put("UPLOAD_DATE", strDate);
                        dbUpdate.insert("SEMS_FILE", null, cValues);
                    }

                    dbUpdate.close();
                    BindUploadFileList(lsFileUpload);

                    //關閉視窗
                    dialog.hide();
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
        }
    }

    @Override
    //檔案上傳選擇檔案完壁觸發用
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0 || data == null)
            return;

        Uri uri = data.getData();
        try {
            if (Build.VERSION.SDK_INT >= 19) {
                uploadFileLocalPath = FileUtils.getPath(getContext(), uri);
            } else {
                uploadFileLocalPath = uri.getPath();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(uploadFileLocalPath);
        edFileName.setText(file.getName());
    }

    private void BindEqpPart(ListView lsEqpPart) {
        SQLiteDatabase dbQuery = offData.getReadableDatabase();
        Cursor csEqpPart = dbQuery.query("SEMS_EQP_PART", null, " EQP_ID =?    AND PART_QTY>0", new String[]{eqpId}, null, null, null);
        csEqpPart.moveToFirst();
        SimpleCursorAdapter eqpPartAdapter = new SimpleCursorAdapter(
                getContext(),
                R.layout.activity_pms_eqp_part_listview,
                csEqpPart,
                new String[]{"PART_ID", "PART_LOT_ID", "PART_QTY", "CMT"},
                new int[]{R.id.tvPart,
                        R.id.tvPartLot,
                        R.id.tvPartQty,
                        R.id.tvPartCmt},
                0
        );
        lsEqpPart.setAdapter(eqpPartAdapter);
        dbQuery.close();
    }

    private void BindUploadFileList(ListView lsUploadFile) {
        SQLiteDatabase dbQuery = offData.getReadableDatabase();
        Cursor csFailSty = dbQuery.query("SEMS_FILE", null, " MRO_WO_ID =? ", new String[]{woId}, null, null, null);
        csFailSty.moveToFirst();
        SimpleCursorAdapter styAdapter = new SimpleCursorAdapter(
                getContext(),
                R.layout.activity_pms_upload_file_listview,
                csFailSty,
                new String[]{"FILE_NAME", "FILE_DESC", "UPLOAD_USER_ID", "UPLOAD_DATE", "ERROR_MSG"},
                new int[]{R.id.tvFileName,
                        R.id.tvFileDesc,
                        R.id.tvUploadUser,
                        R.id.tvUploadDate,
                        R.id.tvUploadError},
                0
        );
        lsUploadFile.setAdapter(styAdapter);
        dbQuery.close();
    }

    private void BindUserWh(ListView lsUserWh) {
        SQLiteDatabase dbQuery = offData.getReadableDatabase();
        Cursor csUserWH = dbQuery.query("SEMS_MRO_WO_WH", new String[]{"USER_ID,USER_NAME,USER_ID || '_' || USER_NAME as IDNAME,_id,strftime('%Y/%m/%d %H:%M',replace(START_DT,'/','-')) as START_DT,strftime('%Y/%m/%d %H:%M',replace(END_DT,'/','-'))  as END_DT,CMT"},
                " MRO_WO_ID =? ", new String[]{woId}, null, null, null);
        csUserWH.moveToFirst();

        boolean isWork = ((WorkRepairActivity) getActivity()).GetIsWork();
        WoUserWhAdapter userAdapter = new WoUserWhAdapter(
                getContext(),
                R.layout.activity_pms_user_wh_listview,
                csUserWH,
                new String[]{"IDNAME", "START_DT", "END_DT", "CMT"},
                new int[]{R.id.tvWhUserId, R.id.tvWhStart, R.id.tvWhEnd, R.id.tvWhCmt},
                0,
                isWork,
                offData,
                woId,
                lsUserWh
        );
        lsUserWh.setAdapter(userAdapter);
        dbQuery.close();
    }

    private void BindFailSty(ListView lsSty) {
        SQLiteDatabase dbQuery = offData.getReadableDatabase();
        Cursor csFailSty = dbQuery.query("SEMS_REPAIR_WO_STY", null, " MRO_WO_ID =? ", new String[]{woId}, null, null, null);
        csFailSty.moveToFirst();
        SimpleCursorAdapter styAdapter = new SimpleCursorAdapter(
                getContext(),
                R.layout.activity_pms_repair_fail_strategy_listview,
                csFailSty,
                new String[]{"FAIL_STRATEGY_ID", "FAIL_STRATEGY_NAME", "FAIL_STRATEGY_CMT"},
                new int[]{R.id.tvIdValue,
                        R.id.tvNameValue,
                        R.id.tvCmtValue},
                0
        );
        lsSty.setAdapter(styAdapter);
        dbQuery.close();
    }

    private void BindFailRsn(ListView lsRsn) {
        SQLiteDatabase dbQuery = offData.getReadableDatabase();
        Cursor csFailRsn = dbQuery.query("SEMS_REPAIR_WO_RSN", null, " MRO_WO_ID =? ", new String[]{woId}, null, null, null);
        csFailRsn.moveToFirst();
        SimpleCursorAdapter rsnAdapter = new SimpleCursorAdapter(
                getContext(),
                R.layout.activity_pms_repair_fail_reason_listview,
                csFailRsn,
                new String[]{"FAIL_REASON_ID",
                        "FAIL_REASON_NAME",
                        "FAIL_REASON_CMT"},
                new int[]{R.id.tvIdValue,
                        R.id.tvNameValue,
                        R.id.tvCmtValue},
                0
        );
        lsRsn.setAdapter(rsnAdapter);
        dbQuery.close();
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
