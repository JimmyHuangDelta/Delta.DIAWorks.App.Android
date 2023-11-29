package com.delta.android.PMS.Client.Fragment;

import android.Manifest;
import android.app.DownloadManager;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
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
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.delta.android.Core.Common.CheckPermission;
import com.delta.android.Core.Common.Global;
import com.delta.android.PMS.Client.Adapter.CheckItemAdapter;
import com.delta.android.PMS.Client.Adapter.InsCheckItemAdapter;
import com.delta.android.PMS.Client.Adapter.WoUserWhAdapter;
import com.delta.android.PMS.Client.WorkInsActivity;
import com.delta.android.PMS.Client.WorkRepairActivity;
import com.delta.android.PMS.Common.DownloadReceiver;
import com.delta.android.PMS.Common.FileUtils;
import com.delta.android.PMS.Common.PreventButtonMultiClick;
import com.delta.android.PMS.OffLineData.Data;
import com.delta.android.R;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class WorkInsFragment extends Fragment {
    private String title;
    private String woId;
    private String eqpId;
    SimpleDateFormat df;
    Data offData;
    private DownloadReceiver downloadReceiver;

    SimpleCursorAdapter storageAdapter; //倉庫adapte
    SimpleCursorAdapter partAdapter; //零件adapter
    SimpleCursorAdapter binAdapter; //儲位adapter
    String uploadFileLocalPath; //紀錄上傳的檔案來源
    EditText edFileName; //紀錄檔案名稱
    ListView lsFileUpload;

    public static WorkInsFragment newInstance(String pageTitle, String woId, String eqpId) {
        WorkInsFragment fragment = new WorkInsFragment();
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

        title = getArguments().getString("TITLE");
        woId = getArguments().getString("WO_ID");
        eqpId = getArguments().getString("EQP_ID");
        df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        offData = new Data(getContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        FrameLayout frameLayout = new FrameLayout(getActivity());
        frameLayout.setLayoutParams(params);
        frameLayout.setEnabled(false);
        final WorkInsActivity activity = (WorkInsActivity) getActivity();

        switch (title) {
            case "SOP":
                //region
                View viewSop = getLayoutInflater().inflate(R.layout.activity_pms_sop, null);
                ListView lsSop = (ListView) viewSop.findViewById(R.id.lvPmSop);
                SQLiteDatabase dbQuery = offData.getReadableDatabase();
                Cursor csSop = dbQuery.query("SBRM_EMS_PM_SOP", null, "MRO_WO_ID = ?", new String[]{woId}, null, null, null);
                csSop.moveToFirst();

                SimpleCursorAdapter sopAdapter = new SimpleCursorAdapter(
                        getContext(),
                        R.layout.activity_pms_pm_sop_listview,
                        csSop,
                        new String[]{"SOP_ID", "SOP_NAME", "DOC_NO", "FULL_FILE_NAME"},
                        new int[]{R.id.tvSopId, R.id.tvSopName, R.id.tvDocNo, R.id.tvFullFileName},
                        0
                );
                lsSop.setAdapter(sopAdapter);
                dbQuery.close();

                frameLayout.addView(viewSop);

                lsSop.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        final WorkInsActivity activity = (WorkInsActivity) getActivity();
                        if (!activity.GetIsWork()) {
                            ((WorkInsActivity) getActivity()).ShowMessage(getResources().getString(R.string.EAPE107004));
                            return;
                        }

                        ArrayList<String> arReqPermission = new ArrayList<String>(); //紀錄所需的權限
                        arReqPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        arReqPermission.add(Manifest.permission.READ_EXTERNAL_STORAGE);

                        CheckPermission checkPermission = new CheckPermission(getActivity());
                        if (!checkPermission.CheckPermission(arReqPermission)) { //檢核是否有缺少開通的權限
                            String[] unPermission = checkPermission.GetUnPermission();
                            if (unPermission != null && unPermission.length > 0) { //如果有缺少要開通的權限，談框詢問是否需要開啟
                                if (Build.VERSION.SDK_INT >= 23) { //建置版本 >23 (android 6.0以上)才需要檢查
                                    requestPermissions(unPermission, 1);
                                }
                            }
                        } else {
                            TextView tvSopId = (TextView) view.findViewById(R.id.tvSopId);
                            TextView tvSopName = (TextView) view.findViewById(R.id.tvSopName);
                            TextView tvDocNo = (TextView) view.findViewById(R.id.tvDocNo);

                            final String sopId = tvSopId.getText().toString();
                            final String sopName = tvSopName.getText().toString();
                            final String docNo = tvDocNo.getText().toString();
                            final String fileName = String.format("%s_%s", sopId, sopName);
                            String extension = "";

                            final HashMap<String, ArrayList<String>> fileNames = new HashMap<String, ArrayList<String>>();
                            StringBuilder sbMsg = new StringBuilder();
                            sbMsg.append(woId + "\n");
//                            String msg = woId + "\n";
                            SQLiteDatabase dbQuery = offData.getReadableDatabase();
                            Cursor cursor = dbQuery.query("SBRM_EMS_PM_SOP", null, "MRO_WO_ID = ?", new String[]{woId}, null, null, null);

                            //取工單所對應Sop的資訊
                            while (cursor.moveToNext()) {
                                String curSopId = cursor.getString(cursor.getColumnIndex("SOP_ID"));
                                String curSopName = cursor.getString(cursor.getColumnIndex("SOP_NAME"));
                                String curUrl = cursor.getString(cursor.getColumnIndex("URL"));

                                //定義檔名的規範SOPID_SOPNAME
                                String curFileName = String.format("%s_%s", curSopId, curSopName);
                                ArrayList<String> tmp = new ArrayList<String>();
                                tmp.add(curUrl);
                                tmp.add(curUrl.substring(curUrl.lastIndexOf(".")));
                                fileNames.put(curFileName, tmp);
                                sbMsg.append(curFileName);
//                                msg += curFileName;
                                if (cursor.getPosition() != cursor.getCount() - 1)
                                    sbMsg.append("\n");
//                                    msg += "\n";
                            }
                            //取選擇項目副檔名
                            extension = fileNames.containsKey(fileName) ? fileNames.get(fileName).get(1) : "";

                            dbQuery.close();

                            final String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                            final String defaultLocalSopDir = getResources().getString(R.string.DEFAULT_LOCAL_SOP_DIR);
                            final File checkFile = new File(path + defaultLocalSopDir + fileName + extension);

                            File file = new File(defaultLocalSopDir);
                            if (!file.exists()) file.mkdir();

                            try {
                                //是否有檔案
                                if (checkFile.exists()) {
                                    //開啟檔案
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    Uri uri;
                                    if (Build.VERSION.SDK_INT < 24) {
                                        uri = Uri.fromFile(checkFile);
                                    } else {
                                        uri = FileProvider.getUriForFile(getActivity(), "com.delta.android.PMS.Client", checkFile);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    }

                                    intent.setDataAndType(uri, "*/*");
                                    getActivity().startActivity(intent);
                                } else {
                                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity(), 0)
                                            .setTitle(getResources().getString(R.string.FILE_DOWNLOAD))
                                            .setCancelable(false)
                                            .setMessage(sbMsg.toString())
                                            .setPositiveButton(getResources().getString(R.string.CONFIRM), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                    ConnectivityManager cManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                                                    NetworkInfo networkInfo = cManager.getActiveNetworkInfo();

                                                    //是否有網路
                                                    if (networkInfo == null || !networkInfo.isConnected()) {
                                                        ((WorkInsActivity) getActivity()).ShowMessage(R.string.EAPE107005);
                                                    } else {
                                                        //註銷BroadcastReceiver
                                                        if (downloadReceiver != null)
                                                            getActivity().unregisterReceiver(downloadReceiver);

                                                        //註冊BroadcastReceiver
                                                        downloadReceiver = new DownloadReceiver(getActivity(), fileNames.size());
                                                        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
                                                        getActivity().registerReceiver(downloadReceiver, intentFilter);

                                                        //下載對應工單項目全部Sop
                                                        for (HashMap.Entry<String, ArrayList<String>> fileName : fileNames.entrySet()) {

                                                            //已存在Sop不下載
                                                            File checkFile = new File(path + defaultLocalSopDir + fileName.getKey() + fileName.getValue().get(1));
                                                            if (checkFile.exists()) continue;

                                                            DownloadManager dm = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                                                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileName.getValue().get(0)));
                                                            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                                                            request.setAllowedOverRoaming(false);
                                                            request.setTitle(fileName.getKey());
                                                            request.setDestinationInExternalPublicDir(defaultLocalSopDir, fileName.getKey() + fileName.getValue().get(1));
                                                            dm.enqueue(request);
                                                        }
                                                    }
                                                }
                                            }).setNegativeButton(getResources().getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            });

                                    AlertDialog alertDialog = alert.create();
                                    alertDialog.show();
                                }
                            } catch (Exception ex) {
                                StringWriter sw = new StringWriter();
                                ex.printStackTrace(new PrintWriter(sw));
                                ((WorkInsActivity) getActivity()).ShowMessage(sw.toString());
                            }
                        }
                    }
                });
                //endregion
                break;
            case "PART_TRX":
                //region
                View viewPartTrx = getLayoutInflater().inflate(R.layout.activity_pms_part_trx, null);

                final ListView lsPartTrx = (ListView) viewPartTrx.findViewById(R.id.lvPartTrx);
                SQLiteDatabase dbQueryPartTrx = offData.getReadableDatabase();
                Cursor csPartTrx = dbQueryPartTrx.query("SEMS_MRO_PART_TRX", null, " MRO_WO_ID =? ", new String[]{woId}, null, null, null);
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
                            ((WorkInsActivity) getActivity()).ShowMessage(getResources().getString(R.string.EAPE107004));
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
                                                ((WorkInsActivity) getActivity()).ShowMessage(getResources().getString(R.string.EAPE107022));
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
                                                } else {
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
                        final WorkInsActivity activity = (WorkInsActivity) getActivity();
                        if (!activity.GetIsWork()) {
                            ((WorkInsActivity) getActivity()).ShowMessage(getResources().getString(R.string.EAPE107004));
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

                                        activity.UpdateNeedUpdateFlag(true);
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
                        final WorkInsActivity activity = (WorkInsActivity) getActivity();
                        if (!activity.GetIsWork()) {
                            ((WorkInsActivity) getActivity()).ShowMessage(getResources().getString(R.string.EAPE107004));
                            return;
                        }

                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
                        View view = getLayoutInflater().inflate(R.layout.style_pms_dialog_user_wh, null);
                        builder.setView(view);
                        final android.app.AlertDialog dialog = builder.create();
                        dialog.show();
                        dialog.setCancelable(false);

                        final EditText edFailWhUser = (EditText) view.findViewById(R.id.edUserWhCmt);

                        final Spinner spUserId = (Spinner) view.findViewById(R.id.spWorkUserId);
                        SQLiteDatabase dbQueryUser = offData.getReadableDatabase();
                        Cursor csUser = dbQueryUser.query("SBRM_EMS_EQP_SUB_ROLE", new String[]{"USER_ID,USER_NAME,USER_ID || '_' || USER_NAME as IDNAME,_id"}, "EQP_ID = ? AND TECH_TYPE = 'PM'", new String[]{eqpId}, null, null, null);
                        csUser.moveToFirst();
                        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(
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
                        edUserEndDate.setText(sbNowDate.toString());
                        edUserEndDate.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
                                View view = getLayoutInflater().inflate(R.layout.style_wms_dialog_date_picker_spinner, null);
                                final DatePicker datePicker = view.findViewById(R.id.date_picker);

                                Calendar cal = Calendar.getInstance();
                                cal.setTimeInMillis(System.currentTimeMillis());
                                datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);

                                builder.setView(view);
                                builder.setTitle(R.string.SELECT_WORK_END_DATE);

                                final android.app.AlertDialog dialog = builder.create();
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
                        edUserEndTime.setText(sbNowTime.toString());
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
                                            edUserEndTime.setText("0" + hourOfDay + ":0" + minute);
                                        } else if (hourOfDay < 10 && minute >= 10) {
                                            edUserEndTime.setText("0" + hourOfDay + ":" + minute);
                                        } else if (hourOfDay >= 10 && minute < 10) {
                                            edUserEndTime.setText(hourOfDay + ":0" + minute);
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
                        edUserStartDate.setText(sbNowDate.toString());
                        edUserStartDate.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
                                View view = getLayoutInflater().inflate(R.layout.style_wms_dialog_date_picker_spinner, null);
                                final DatePicker datePicker = view.findViewById(R.id.date_picker);

                                Calendar cal = Calendar.getInstance();
                                cal.setTimeInMillis(System.currentTimeMillis());
                                datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);

                                builder.setView(view);
                                builder.setTitle(R.string.SELECT_WORK_START_DATE);

                                final android.app.AlertDialog dialog = builder.create();
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
                        edUserStartTime.setText(sbNowTime.toString());
                        edUserStartTime.setOnClickListener(new View.OnClickListener() {
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
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE107006), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (edUserStartDate.getText().toString().contentEquals("")) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE107007), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (edUserStartTime.getText().toString().contentEquals("")) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE107008), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (edUserEndDate.getText().toString().contentEquals("")) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE107009), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (edUserEndTime.getText().toString().contentEquals("")) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE107010), Toast.LENGTH_LONG).show();
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
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE107011), Toast.LENGTH_LONG).show();
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
                                    cValues.put("START_DT", edUserStartDate.getText().toString() + " " + edUserStartTime.getText().toString()+":00");
                                    cValues.put("END_DT", edUserEndDate.getText().toString() + " " + edUserEndTime.getText().toString()+":00");
                                    cValues.put("CMT", edFailWhUser.getText().toString());
                                    dbUpdate.insert("SEMS_MRO_WO_WH", null, cValues);
                                }
                                dbUpdate.close();
                                activity.UpdateNeedUpdateFlag(true); //更新主畫面上的flag
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
            case "CHECK_ITEM":
                //設定ListView清單
                View viewCheckItem = getLayoutInflater().inflate(R.layout.activity_pms_pm_check_item, null);
                ListView lsCheckItem = (ListView) viewCheckItem.findViewById(R.id.lvWorkCheckItem);
                SQLiteDatabase db = offData.getReadableDatabase();
                Cursor CursorCheckItem = db.query("SEMS_PM_WO_CHECK", null, "MRO_WO_ID = '" + woId + "'", null, null, null, null);
                LayoutInflater layoutInflater = getLayoutInflater();
                InsCheckItemAdapter insCheckItemAdapter = new InsCheckItemAdapter(getActivity(), layoutInflater, CursorCheckItem, woId);
                lsCheckItem.setAdapter(insCheckItemAdapter);
                frameLayout.addView(viewCheckItem);
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
                        final WorkInsActivity activity = (WorkInsActivity) getActivity();
                        if (!activity.GetIsWork()) {
                            ((WorkInsActivity) getActivity()).ShowMessage(getResources().getString(R.string.EAPE107004));
                            return false;
                        }

                        lsEqpPart.getAdapter();

                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
                        View viewUnMount = getLayoutInflater().inflate(R.layout.style_pms_dialog_unmount_eqp_part, null);
                        builder.setView(viewUnMount);
                        builder.setTitle(getResources().getString(R.string.UNMOUNT_PART));///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////零件下機
                        final android.app.AlertDialog dialog = builder.create();
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
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE107012), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                //比較下機零件是否超過線在機上數量
                                int eqpPartQty = 0; //紀錄機上的零件剩餘數
                                if (Integer.parseInt(edPartQty.getText().toString()) < Integer.parseInt(edTrxQty.getText().toString())) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE107013), Toast.LENGTH_LONG).show();
                                    return;
                                } else {
                                    eqpPartQty = Integer.parseInt(edPartQty.getText().toString()) - Integer.parseInt(edTrxQty.getText().toString());
                                }

                                //檢查倉庫、儲位是否有選擇
                                if (strStorageId[0].contentEquals("")) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE107014), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (strBinId[0].contentEquals("")) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE107015), Toast.LENGTH_LONG).show();
                                    return;
                                }

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

                                activity.UpdateNeedUpdateFlag(true); //更新主畫面上的flag
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
                            ((WorkInsActivity) getActivity()).ShowMessage(getResources().getString(R.string.EAPE107004));
                            return;
                        }

                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
                        View view = getLayoutInflater().inflate(R.layout.style_pms_dialog_eqp_part, null);
                        builder.setView(view);
                        builder.setTitle(getResources().getString(R.string.ADD_MONUT_PART));
                        final android.app.AlertDialog dialog = builder.create();
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
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE107016), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (edPartLot.getText().toString().contentEquals("")) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE107017), Toast.LENGTH_LONG).show();
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
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE107018), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (strStorageId[0].contentEquals("")) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE107019), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                if (strBinId[0].contentEquals("")) {
                                    Toast.makeText(getContext(), getResources().getString(R.string.EAPE107020), Toast.LENGTH_LONG).show();
                                    return;
                                }

                                //檢查庫存是否存在這個批號的零件，並且數量足夠
                                Cursor csInv = offData.getReadableDatabase().query("SMTL_INVENTORY", null
                                        , "ITEM_ID = ? AND MTL_LOT_ID = ? AND STORAGE_ID = ? AND BIN_ID = ?"
                                        , new String[]{strPartId[0], edPartLot.getText().toString(), strStorageId[0], strBinId[0]}
                                        , null, null, null);
                                csInv.moveToFirst();
                                if (csInv.getCount() <= 0) {
                                    ((WorkInsActivity) getActivity()).ShowMessage(getResources().getString(R.string.EAPE107023));
                                    return;
                                }

                                if (csInv.getInt(csInv.getColumnIndex("QTY")) < Integer.parseInt(edPartQty.getText().toString())) {
                                    ((WorkInsActivity) getActivity()).ShowMessage(getResources().getString(R.string.EAPE107024));
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

                                activity.UpdateNeedUpdateFlag(true); //更新主畫面上的flag
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
                        final WorkInsActivity activity = (WorkInsActivity) getActivity();
                        if (!activity.GetIsWork()) {
                            ((WorkInsActivity) getActivity()).ShowMessage(getResources().getString(R.string.EAPE107004));
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

                                        activity.UpdateNeedUpdateFlag(true); //更新主畫面上的flag
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
                        final WorkInsActivity activity = (WorkInsActivity) getActivity();
                        if (!activity.GetIsWork()) {
                            ((WorkInsActivity) getActivity()).ShowMessage(getResources().getString(R.string.EAPE107004));
                            return;
                        }

                        ArrayList<String> arReqPermission = new ArrayList<String>(); //紀錄所需的權限
                        arReqPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        arReqPermission.add(Manifest.permission.READ_EXTERNAL_STORAGE);

                        CheckPermission checkPermission = new CheckPermission(getActivity());
                        if (!checkPermission.CheckPermission(arReqPermission)) { //檢查是否有缺少開通的權限
                            String[] unPermission = checkPermission.GetUnPermission();
                            if (unPermission != null && unPermission.length > 0) { //如果有缺少要開通的權限，彈框詢問是否需要開啟
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
                                        Toast.makeText(getContext(), getResources().getString(R.string.EAPE107021), Toast.LENGTH_LONG).show();
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
                                    activity.UpdateNeedUpdateFlag(true); //更新主畫面上的flag
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

    //使用權限檢查
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) { //等於 - 1代表有不許允開啟的權限。直接return`.
                    return;
                }
            }

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
            View view = getLayoutInflater().inflate(R.layout.style_pms_dialog_file_upload, null);
            builder.setView(view);
            final android.app.AlertDialog dialog = builder.create();
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
                        Toast.makeText(getContext(), getResources().getString(R.string.EAPE107021), Toast.LENGTH_LONG).show();
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
        } else {
            return;
        }
    }

    //檔案上傳_選擇檔案
    @Override
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
        Cursor csEqpPart = dbQuery.query("SEMS_EQP_PART", null, " EQP_ID =?  AND PART_QTY>0", new String[]{eqpId}, null, null, null);
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

        boolean isWork = ((WorkInsActivity) getActivity()).GetIsWork();
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
