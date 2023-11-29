package com.delta.android.WMS.Client;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseActivity;
import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.Activity.ScanActivity;
import com.delta.android.Core.DataTable.DataColumn;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.GenerateJsonNetString.MList;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionaryList;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Client.GridAdapter.WarehouseStorageNonSheetGridAdapter;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.BWarehouseStorageNonSheetParam;
import com.delta.android.WMS.Param.ParamObj.CheckCountObj;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.delta.android.WMS.Param.ParamObj.WarehouseVoucherDetObj;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.lang.reflect.Array;
import java.lang.reflect.Parameter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class WarehouseStorageNonSheetActivity extends BaseFlowActivity {

    //region Public param
    private ViewHolder holder = null;
    public DataTable dtLotID;

    private int iMaxSeq = 0;
    HashMap<String, String> mapStorage = new HashMap<String, String>();

    //for cmbbox
    HashMap<String, String> mapSheetTypeKey = new HashMap<String, String>();
    HashMap<String, String> mapStorageKey = new HashMap<String, String>();
    HashMap<String, String> mapItemKey = new HashMap<String, String>();
    HashMap<String, String> mapOrgan = new HashMap<String, String>();

    HashMap<String, String> hmItem = new HashMap<String, String>();
    HashMap<String, String> hmItemName = new HashMap<String, String>();
    HashMap<String, String> hmItemRegisterType = new HashMap<String, String>();
    HashMap<String, String> hmStorage = new HashMap<String, String>();
    HashMap<String, String> hmGrTypeID = new HashMap<String, String>();

    ArrayList<String> alLotID = new ArrayList<String>();
    //endregion

    static class ViewHolder {
        ListView lvWarehouseLot;
        TabHost tabHost;
        //Mst
        Spinner cmbSheetType;
        Spinner cmbOrgan;
        //EditText etSource;

        //Det
        Spinner cmbStorage;
        Spinner cmbItem;
        EditText etWoId;
        ImageButton ibtnLotQRScan;  // 220708 Ikea 新增鏡頭掃描輸入
        EditText etLot;
        CheckBox chkFIFOLot;
        EditText etQty;
        EditText etScrapQty;
        EditText etUom;
        EditText etCmt;
        EditText etMFDDate;
        EditText etExpiryDate;
        Button btnAddLodID;

        Button btnRefresh;
        Button btnConfirm;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warehouse_storage_non_sheet);

        initControl();
        initData();
        setListensers();
    }

    // 220708 Ikea 新增鏡頭掃描輸入
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, getResources().getText(R.string.QRCODE_SCAN_CANCEL).toString(), Toast.LENGTH_SHORT).show();
            } else {
                holder.etLot.setText(result.getContents().trim());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initControl() {
        if (holder != null) return;

        holder = new ViewHolder();
        holder.tabHost = findViewById(R.id.tabHost);

        //Mst
        holder.cmbSheetType = findViewById(R.id.cmbSheetType);
        holder.cmbOrgan = findViewById(R.id.cmbOrgan);
        //Det
        holder.cmbStorage = findViewById(R.id.cmbStorage);
        holder.cmbItem = findViewById(R.id.cmbItem);
        holder.etWoId = findViewById(R.id.etWoId);
        holder.ibtnLotQRScan = findViewById(R.id.ibtnLotQRScan); // 220708 Ikea 新增鏡頭掃描輸入
        holder.etLot = findViewById(R.id.etLot);
        holder.chkFIFOLot = findViewById(R.id.cbFIFOLot);
        holder.etQty = findViewById(R.id.etQty);
        holder.etScrapQty = findViewById(R.id.etScrapQty);
        holder.etUom = findViewById(R.id.etUOM);
        holder.etCmt = findViewById(R.id.etCMT);
        holder.etMFDDate = findViewById(R.id.etMFDDate);
        holder.etExpiryDate = findViewById(R.id.etExpiryDate);
        holder.btnAddLodID = findViewById(R.id.btnAddLot);
        holder.chkFIFOLot = findViewById(R.id.cbFIFOLot);

        holder.btnRefresh = findViewById(R.id.btnRefresh);
        holder.btnConfirm = findViewById(R.id.btnConfirm);
        holder.lvWarehouseLot = findViewById(R.id.lvWarehouseLot);

        holder.tabHost.setup();

        TabHost.TabSpec spec1 = holder.tabHost.newTabSpec("Mst");
        View tab1 = LayoutInflater.from(this).inflate(R.layout.activity_wms_good_pick_tab_widget, null);
        TextView tvTab1 = tab1.findViewById(R.id.tvTabText);
        tvTab1.setText(R.string.WAREHOUSE_INFO);
        spec1.setIndicator(tab1);
        spec1.setContent(R.id.Tab1);
        holder.tabHost.addTab(spec1);

        TabHost.TabSpec spec2 = holder.tabHost.newTabSpec("Det");
        View tab2 = LayoutInflater.from(this).inflate(R.layout.activity_wms_good_pick_tab_widget, null);
        TextView tvTab2 = tab2.findViewById(R.id.tvTabText);
        tvTab2.setText(R.string.WAREHOUSE_DET);
        spec2.setIndicator(tab2);
        spec2.setContent(R.id.Tab2);
        holder.tabHost.addTab(spec2);
    }

    private void initData() {
        GetInitCmbBox();
        CreateDatatableLotID();
    }

    private void Refresh(boolean refreshAll) {
        if (refreshAll) {
            CreateDatatableLotID();

            //Mst
            holder.cmbSheetType.setSelection(0);
            holder.cmbOrgan.setSelection(0);
            //holder.etSource.getText().clear();

            //Det
            holder.cmbStorage.setSelection(0);
            holder.cmbItem.setSelection(0);

            GetListView();
        }

        holder.etWoId.getText().clear();
        holder.etLot.getText().clear();
        holder.etQty.getText().clear();
        holder.etScrapQty.getText().clear();
        holder.etUom.getText().clear();
        holder.etCmt.getText().clear();
        holder.etMFDDate.getText().clear();
        holder.etExpiryDate.getText().clear();
        holder.chkFIFOLot.setChecked(false);
    }

    private void setListensers() {
        holder.tabHost.setOnTabChangedListener(lsTabHost_OnTabChange);
        holder.ibtnLotQRScan.setOnClickListener(ibtnOnClick); // 220708 Ikea 新增鏡頭掃描輸入
        holder.etMFDDate.setOnClickListener(lsMFDDate);
        holder.etExpiryDate.setOnClickListener(lsExpiryDate);
        holder.btnAddLodID.setOnClickListener(lsAddLotID);
        holder.btnRefresh.setOnClickListener(lsRefresh);
        holder.btnConfirm.setOnClickListener(lsConfirm);
        holder.lvWarehouseLot.setOnItemClickListener(lsLotOnClick);
        holder.lvWarehouseLot.setOnItemLongClickListener(lsLotListView);
    }

    private void GetInitCmbBox() {
        ArrayList<BModuleObject> lsBObj = new ArrayList<>();

        //單據類型
        BModuleObject bmObjSheetType = new BModuleObject();
        bmObjSheetType.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObjSheetType.setModuleID("BIFetchSheetType");
        bmObjSheetType.setRequestID("GetSheetType");

        bmObjSheetType.params = new Vector<ParameterInfo>();
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Filter);
        param1.setParameterValue(" AND P.SHEET_TYPE_POLICY_ID in ('Warehouse') AND T.SHEET_TYPE_ID = 'SysWarehouse' ");
        bmObjSheetType.params.add(param1);
        lsBObj.add(bmObjSheetType);

        //部門
        BModuleObject bmObjOrgan = new BModuleObject();
        bmObjOrgan.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObjOrgan.setModuleID("BIFetchOrgan");
        bmObjOrgan.setRequestID("GetOrgan");
        lsBObj.add(bmObjOrgan);

        //倉庫
        BModuleObject bmObjStorage = new BModuleObject();
        bmObjStorage.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObjStorage.setModuleID("BIFetchStorage");
        bmObjStorage.setRequestID("GetStorage");

        bmObjStorage.params = new Vector<ParameterInfo>();
        ParameterInfo paramStorage = new ParameterInfo();
        paramStorage.setParameterID(BIWMSFetchInfoParam.Filter);
        paramStorage.setParameterValue("  AND S.STORAGE_TYPE ='WMS' ");
        bmObjStorage.params.add(paramStorage);
        lsBObj.add(bmObjStorage);

        //物料
        BModuleObject bmObjItem = new BModuleObject();
        bmObjItem.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObjItem.setModuleID("BIFetchItem");
        bmObjItem.setRequestID("GetItem");
        lsBObj.add(bmObjItem);

        //工單
        // 220708 Ikea 工單僅提供輸入，不用BM取資料
        /*
        BModuleObject bmObjWo = new BModuleObject();
        bmObjWo.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObjWo.setModuleID("BIFetchWo");
        bmObjWo.setRequestID("GetWo");
        lsBObj.add(bmObjWo);
         */

        CallBIModule(lsBObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    DataTable dtSheetType = bModuleReturn.getReturnJsonTables().get("GetSheetType").get("SHEET_TYPE");
                    DataTable dtStorage = bModuleReturn.getReturnJsonTables().get("GetStorage").get("STORAGE");
                    DataTable dtItem = bModuleReturn.getReturnJsonTables().get("GetItem").get("ITEM");
                    DataTable dtOrgan = bModuleReturn.getReturnJsonTables().get("GetOrgan").get("ORGAN");

                    if (dtSheetType.Rows.size() <= 0)
                    {
                        ShowMessage(R.string.WAPG010013); //WAPG010013    未設定入庫類型單據，無法產生系統的入庫單據
                        return;
                    }

                    ArrayList<String> alSheetType = new ArrayList<>();
                    ArrayList<String> alStorage = new ArrayList<>();
                    ArrayList<String> alItem = new ArrayList<>();
                    ArrayList<String> alOrgan = new ArrayList<>();

                    for (int i = 0; i < dtSheetType.Rows.size(); i++) {
                        //mapSheetTypeKey.put(dtSheetType.Rows.get(i).get("IDNAME").toString(), dtSheetType.Rows.get(i).get("SHEET_TYPE_POLICY_ID").toString());
                        mapSheetTypeKey.put(dtSheetType.Rows.get(i).get("IDNAME").toString(), dtSheetType.Rows.get(i).get("SHEET_TYPE_ID").toString());
                        alSheetType.add(i, dtSheetType.Rows.get(i).get("IDNAME").toString());

                        hmGrTypeID.put(dtSheetType.Rows.get(i).get("SHEET_TYPE_KEY").toString(), dtSheetType.Rows.get(i).get("SHEET_TYPE_ID").toString());
                    }

                    for (int i = 0; i < dtStorage.Rows.size(); i++) {
                        mapStorageKey.put(dtStorage.Rows.get(i).get("IDNAME").toString(), dtStorage.Rows.get(i).get("STORAGE_KEY").toString());
                        alStorage.add(i, dtStorage.Rows.get(i).get("IDNAME").toString());

                        hmStorage.put(dtStorage.Rows.get(i).get("STORAGE_KEY").toString(), dtStorage.Rows.get(i).get("STORAGE_ID").toString());
                    }

                    for (int i = 0; i < dtItem.Rows.size(); i++) {
                        mapItemKey.put(dtItem.Rows.get(i).get("IDNAME").toString(), dtItem.Rows.get(i).get("ITEM_KEY").toString());
                        alItem.add(i, dtItem.Rows.get(i).get("IDNAME").toString());

                        hmItem.put(dtItem.Rows.get(i).get("ITEM_KEY").toString(), dtItem.Rows.get(i).get("ITEM_ID").toString());
                        hmItemName.put(dtItem.Rows.get(i).get("ITEM_KEY").toString(), dtItem.Rows.get(i).get("ITEM_NAME").toString());
                        hmItemRegisterType.put(dtItem.Rows.get(i).get("ITEM_KEY").toString(), dtItem.Rows.get(i).get("REGISTER_TYPE").toString());
                    }

                    for (int i = 0; i < dtOrgan.Rows.size(); i++) {
                        mapOrgan.put(dtOrgan.Rows.get(i).get("IDNAME").toString(), dtOrgan.Rows.get(i).get("ORGAN_ID").toString());
                        alOrgan.add(i, dtOrgan.Rows.get(i).get("IDNAME").toString());
                    }

                    ArrayAdapter<String> adapterSheetType = new ArrayAdapter<>(WarehouseStorageNonSheetActivity.this, android.R.layout.simple_spinner_dropdown_item, alSheetType);
                    ArrayAdapter<String> adapterStorage = new ArrayAdapter<>(WarehouseStorageNonSheetActivity.this, android.R.layout.simple_spinner_dropdown_item, alStorage);
                    ArrayAdapter<String> adapterItem = new ArrayAdapter<>(WarehouseStorageNonSheetActivity.this, android.R.layout.simple_spinner_dropdown_item, alItem);
                    ArrayAdapter<String> adapterOrgan = new ArrayAdapter<>(WarehouseStorageNonSheetActivity.this, android.R.layout.simple_spinner_dropdown_item, alOrgan);

                    holder.cmbSheetType.setAdapter(adapterSheetType);
                    holder.cmbStorage.setAdapter(adapterStorage);
                    holder.cmbItem.setAdapter(adapterItem);
                    holder.cmbOrgan.setAdapter(adapterOrgan);
                }
            }
        });
    }

    private void CreateDatatableLotID() {
        dtLotID = new DataTable();

        DataColumn dcStorageKey = new DataColumn("STORAGE_KEY");
        DataColumn dcStorageID = new DataColumn("STORAGE_ID");
        DataColumn dcItemKey = new DataColumn("ITEM_KEY");
        DataColumn dcItemID = new DataColumn("ITEM_ID");
        DataColumn dcItemName = new DataColumn("ITEM_NAME");
        DataColumn dcWoID = new DataColumn("WO_ID");
        DataColumn dcLotID = new DataColumn("LOT_ID");
        DataColumn dcFIFOLot = new DataColumn("SPEC_LOT");
        DataColumn dcQty = new DataColumn("QTY");
        DataColumn dcScrapQty = new DataColumn("SCRAP_QTY");
        DataColumn dcUOM = new DataColumn("UOM");
        DataColumn dcCMT = new DataColumn("CMT");
        DataColumn dcMFDDate = new DataColumn("MFG_DATE");
        DataColumn dcExpiryDate = new DataColumn("EXP_DATE");
        DataColumn dcRegisterType = new DataColumn("REGISTER_TYPE");
        DataColumn dcTempBin = new DataColumn("TEMP_BIN");

        dtLotID.addColumn(dcStorageKey);
        dtLotID.addColumn(dcStorageID);
        dtLotID.addColumn(dcItemKey);
        dtLotID.addColumn(dcItemID);
        dtLotID.addColumn(dcWoID);
        dtLotID.addColumn(dcItemName);
        dtLotID.addColumn(dcLotID);
        dtLotID.addColumn(dcFIFOLot);
        dtLotID.addColumn(dcQty);
        dtLotID.addColumn(dcScrapQty);
        dtLotID.addColumn(dcUOM);
        dtLotID.addColumn(dcCMT);
        dtLotID.addColumn(dcMFDDate);
        dtLotID.addColumn(dcExpiryDate);
        dtLotID.addColumn(dcRegisterType);
        dtLotID.addColumn(dcTempBin);
    }

    public void setMFDDate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.style_wms_dialog_date_picker_spinner, null);
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);
        //右邊簡化
        datePicker.setCalendarViewShown(false);
        //初始化
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
        //設置Dialog
        builder.setView(view);
        //標頭
        builder.setTitle(R.string.MFD_DATE);
        final AlertDialog dialog = builder.create();
        dialog.show();

        Button bConfirm = view.findViewById(R.id.btnConfirm);
        bConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //日期格式
                StringBuffer sb = new StringBuffer();
                sb.append(String.format("%d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth()));
                holder.etMFDDate.setText(sb);
                dialog.cancel();
            }
        });
    }

    public void addLotID() {
        //region param
        //Det
        String strStorageKey;
        String strItemKey;
        String strWoID;
        String strLotID;
        boolean bFIFOLot;
        int qty;
        int scrapQty;
        String strUOM;
        String strCMT;
        String strMFDDate;
        String strExpiryDate;
        //endregion

        strStorageKey = mapStorageKey.get(holder.cmbStorage.getSelectedItem().toString());
        if (holder.cmbStorage.getSelectedItemPosition() == -1) {
            ShowMessage(R.string.WAPG010001); //WAPG010001 請選擇倉庫
            return;
        }

        strItemKey = mapItemKey.get(holder.cmbItem.getSelectedItem().toString());
        if (holder.cmbItem.getSelectedItemPosition() == -1) {
            ShowMessage(R.string.WAPG010002); //WAPG010002 請選擇物料
            return;
        }

        if(holder.etLot.getText().toString().equals("")){
            ShowMessage(R.string.WAPG010012); //WAPG010012 請輸入批號
            return;
        }

        if (holder.etQty.getText().toString().equals("")) {
            ShowMessage(R.string.WAPG010003); //WAPG010003 請輸入數量
            return;
        } else {
            qty = Integer.parseInt(holder.etQty.getText().toString());
        }

        if (holder.etScrapQty.getText().toString().equals("")) {
            ShowMessage(R.string.WAPG010004); //WAPG010004 請輸入報廢數量
            return;
        } else {
            scrapQty = Integer.parseInt(holder.etScrapQty.getText().toString());
        }

        //region 檢查批號
        strLotID = holder.etLot.getText().toString().trim();
        Object[] args = new Object[2];
        args[0] = hmItem.get(strItemKey);
        args[1] = hmItemRegisterType.get(strItemKey);

        if (hmItemRegisterType.get(strItemKey).equals("ItemID")) {
            if (!strLotID.equals("*")) {
                ShowMessage(R.string.WAPG009010, args); //WAPG009010   物料[%s]註冊類別為[%s]，不需要輸入批號!
                return;
            }
        } else {
            if (strLotID.equals("")) {
                ShowMessage(R.string.WAPG009011, args); //WAPG009011   物料[%s]註冊類別為[%s]，需要輸入批號!
                return;
            }

            if (alLotID.contains(strLotID)) {
                ShowMessage(R.string.WAPG009006); //WAPG009006    批號已存在
                return;
            }
        }
        //endregion

        strWoID = holder.etWoId.getText().toString().trim();
        strUOM = holder.etUom.getText().toString().trim();
        strCMT = holder.etCmt.getText().toString().trim();
        strMFDDate = holder.etMFDDate.getText().toString().trim();
        strExpiryDate = holder.etExpiryDate.getText().toString().trim();
        if (holder.chkFIFOLot.isChecked()) {
            bFIFOLot = true;
        } else {
            bFIFOLot = false;
        }

        alLotID.add(strLotID);

        //region Add New LotID
        iMaxSeq++;
        DataRow drNew = dtLotID.newRow();
        drNew.setValue("SEQ", iMaxSeq);
        drNew.setValue("STORAGE_KEY", strStorageKey);
        drNew.setValue("STORAGE_ID", hmStorage.get(strStorageKey));
        drNew.setValue("ITEM_KEY", strItemKey);
        drNew.setValue("ITEM_ID", hmItem.get(strItemKey));
        drNew.setValue("ITEM_NAME", hmItemName.get(strItemKey));
        drNew.setValue("WO_ID", strWoID);
        drNew.setValue("LOT_ID", strLotID);
        drNew.setValue("QTY", qty);
        drNew.setValue("SCRAP_QTY", scrapQty);
        drNew.setValue("UOM", strUOM);
        drNew.setValue("CMT", strCMT);
        drNew.setValue("MFG_DATE", strMFDDate);
        drNew.setValue("EXP_DATE", strExpiryDate);
        if (bFIFOLot) {
            drNew.setValue("SPEC_LOT", "Y");
        } else {
            drNew.setValue("SPEC_LOT", "N");
        }
        drNew.setValue("REGISTER_TYPE", hmItemRegisterType.get(strItemKey));

        dtLotID.Rows.add(drNew);
        //endregion

        GetListView();
        Refresh(false);
    }

    private void ShowLotDialog(int position)
    {
        final int intLotPos = position;

        //region init dialog
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        final View viewLot = inflater.inflate(R.layout.activity_warehouse_storage_non_sheet_lot_delete, null);
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(WarehouseStorageNonSheetActivity.this);
        builder.setView(viewLot);

        final android.app.AlertDialog dialogLot = builder.create();
        dialogLot.setCancelable(false);
        dialogLot.show();

        TextView tvCurrentLot = dialogLot.findViewById(R.id.tvCurrentLot);
        String strErr = getResources().getString(R.string.WAPG010008); //WAPG010008 是否刪除此批號?
        tvCurrentLot.setText(strErr);
        //endregion

        //region 確認清除
        Button btnDelete = viewLot.findViewById(R.id.btnYes);
        btnDelete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                alLotID.remove(dtLotID.Rows.get(intLotPos).getValue("LOT_ID").toString());
                dtLotID.Rows.remove(intLotPos);

                dialogLot.dismiss();
                GetListView();
            }
        });
        //endregion

        //region 取消清除
        Button btnCancel = viewLot.findViewById(R.id.btnNo);
        btnCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                dialogLot.dismiss();
            }
        });
        //endregion
    }

    private void ShowBinDialog(String storageID, List<String> lstBin)
    {
        final String strStorage = storageID;
        //region init dialog
        LayoutInflater inflater = LayoutInflater.from(WarehouseStorageNonSheetActivity.this);
        final View viewBin = inflater.inflate(R.layout.activity_warehouse_storage_non_sheet_select_bin, null);
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(WarehouseStorageNonSheetActivity.this);
        builder.setView(viewBin);

        final android.app.AlertDialog dialogBin = builder.create();
        dialogBin.setCancelable(false);
        dialogBin.show();

        ArrayList<String> alBin = new ArrayList<>();

        TextView tvStorage = dialogBin.findViewById(R.id.tvStorageId);
        final Spinner cmbBin = dialogBin.findViewById(R.id.cmbBinID);

        tvStorage.setText(String.format("Storage ID: %s",storageID));
        for(int i = 0; i < lstBin.size(); i++){
            alBin.add(lstBin.get(i));
        }

        ArrayAdapter<String> adapterBin = new ArrayAdapter<>(WarehouseStorageNonSheetActivity.this, android.R.layout.simple_spinner_dropdown_item, alBin);
        cmbBin.setAdapter(adapterBin);
        //endregion

        //region 確認
        Button btnConfirm = viewBin.findViewById(R.id.btnBinConfirm);
        btnConfirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(cmbBin.getSelectedItemPosition() == -1) return;

                mapStorage.put(strStorage, cmbBin.getSelectedItem().toString());

                dialogBin.dismiss();
            }
        });
        //endregion
    }

    private void WarehouseStorageConfirm()
    {
        //單據類型
        if(holder.cmbSheetType.getSelectedItemPosition() == -1){
            ShowMessage(R.string.WAPG010005); //WAPG010005 請選擇單據類型
            return;
        }

        //部門
        String strOrgan = "";
        if(holder.cmbOrgan.getSelectedItemPosition() != -1){
            strOrgan = mapOrgan.get(holder.cmbOrgan.getSelectedItem().toString());
            //ShowMessage(R.string.WAPG010006); //WAPG010006 請選擇部門
            return;
        }

        if(dtLotID.Rows.size() <= 0){
            ShowMessage(R.string.WAPG010011); //WAPG010011 尚未新增物料
            return;
        }

        List<String> lstStorageId = new ArrayList<String>();
        for (DataRow dr : dtLotID.Rows){
            if(lstStorageId.contains(dr.getValue("STORAGE_ID").toString()))
                continue;
            else {
                lstStorageId.add(dr.getValue("STORAGE_ID").toString());
            }
        }

        for (DataRow dr : dtLotID.Rows){
            String storageId = dr.getValue("STORAGE_ID").toString();
            if(!mapStorage.containsKey(storageId)){
                Object[] args = new Object[1];
                args[0] = storageId;
                ShowMessage(R.string.WAPG010010, args); //WAPG010010    倉庫[%s]尚未設定入庫儲位
                return;
            }
            dr.setValue("TEMP_BIN", mapStorage.get(storageId));
        }

        List<WarehouseVoucherDetObj> lstDetObj = CreateDetObj(dtLotID);

        // region 儲存盤點狀態檢查物件
        // 20220805 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中
        List<CheckCountObj> lstChkCountObj = new ArrayList<>();
        for (DataRow dr : dtLotID.Rows) {
            CheckCountObj chkCountObj = new CheckCountObj();
            chkCountObj.setStorageId(dr.getValue("STORAGE_ID").toString());
            chkCountObj.setItemId(dr.getValue("ITEM_ID").toString());
            chkCountObj.setBinId(dr.getValue("TEMP_BIN").toString());
            lstChkCountObj.add(chkCountObj);
        }
        // endregion

        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Sheet.Parameter.WarehouseVoucherDetObj", "bmWMS.Sheet.Param");
        MList mListEnum = new MList(vListEnum);
        String strDetObj = mListEnum.generateFinalCode(lstDetObj);

        String strSheetType = mapSheetTypeKey.get(holder.cmbSheetType.getSelectedItem().toString());
        //String strOrgan = mapOrgan.get(holder.cmbOrgan.getSelectedItem().toString());

        VirtualClass vListEnum2 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.CheckCountObj", "bmWMS.Library.Param");
        MList mListEnum2 = new MList(vListEnum2);
        String strCheckCountObj = mListEnum2.generateFinalCode(lstChkCountObj);

        //Call BModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BWarehouseStorageNonSheet");
        bmObj.setModuleID("");
        bmObj.setRequestID("WarehouseStorageActivity");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo paramSheetType = new ParameterInfo();
        paramSheetType.setParameterID(BWarehouseStorageNonSheetParam.SheetTypeID);
        paramSheetType.setParameterValue(strSheetType);
        bmObj.params.add(paramSheetType);

        ParameterInfo paramOrgan = new ParameterInfo();
        paramOrgan.setParameterID(BWarehouseStorageNonSheetParam.OrganID);
        paramOrgan.setParameterValue(strOrgan);
        bmObj.params.add(paramOrgan);

        ParameterInfo paramSource = new ParameterInfo();
        paramSource.setParameterID(BWarehouseStorageNonSheetParam.WvSource);
        //PDA無單據入庫 來源固定為WMS
        paramSource.setParameterValue("WMS");
        bmObj.params.add(paramSource);

        ParameterInfo paramObj = new ParameterInfo();
        paramObj.setParameterID(BWarehouseStorageNonSheetParam.WvDetObj);
        paramObj.setNetParameterValue(strDetObj);
        bmObj.params.add(paramObj);

        ParameterInfo paramExecuteChkStock = new ParameterInfo();
        paramExecuteChkStock.setParameterID(BWarehouseStorageNonSheetParam.ExecuteCheckStock); // 20220805 Add by Ikea 是否執行盤點檢查
        paramExecuteChkStock.setNetParameterValue2("true");
        bmObj.params.add(paramExecuteChkStock);

        ParameterInfo paramChkCountObj = new ParameterInfo(); // 20220805 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中
        paramChkCountObj.setParameterID(BWarehouseStorageNonSheetParam.CheckCountObj);
        paramChkCountObj.setNetParameterValue(strCheckCountObj);
        bmObj.params.add(paramChkCountObj);

        CallBModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(!CheckBModuleReturnInfo(bModuleReturn)) return;

                Gson gson = new Gson();
                String sheetID = "";
                sheetID = gson.fromJson(bModuleReturn.getReturnList().get("WarehouseStorageActivity").get(BWarehouseStorageNonSheetParam.WvSheetID).toString(), String.class);

                ShowMessage(R.string.WAPG010009); //WAPG010009    儲存成功

                Refresh(true);
                mapStorage.clear();
                iMaxSeq = 0;
            }
        });
    }

    private void CheckStorageInTempBin(List<String> lstStorageId)
    {
        final List<String> lstStorageID = lstStorageId;

        mapStorage = new HashMap<String, String>();

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchBin");
        bmObj.setRequestID("BIFetchBin");

        bmObj.params = new Vector<ParameterInfo>();
        //裝Condition的容器
        final HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
        List<Condition> lstCondition = new ArrayList<Condition>();
        for (int i = 0; i < lstStorageID.size(); i++)
        {
            Condition cond = new Condition();
            cond.setAliasTable("S");
            cond.setColumnName("STORAGE_ID");
            cond.setValue(lstStorageID.get(i));
            cond.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
            lstCondition.add(cond);
        }
        mapCondition.put("STORAGE_ID", lstCondition);

        //Serialize序列化
        VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vKey, vVal);
        String strCond = msdl.generateFinalCode(mapCondition);

        //Input param
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Condition);
        param1.setNetParameterValue(strCond);
        bmObj.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIWMSFetchInfoParam.Filter);
        param2.setParameterValue("AND B.BIN_TYPE IN ('IT', 'IS')");
        bmObj.params.add(param2);

        //Call BIModule
        CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(CheckBModuleReturnInfo(bModuleReturn)){
                    DataTable dtStorageTempBin = bModuleReturn.getReturnJsonTables().get("BIFetchBin").get("BIN");

                    for (final String storageID: lstStorageID) {
                        List<String> lstBin = new ArrayList<String>();
                        for (DataRow dr : dtStorageTempBin.Rows){
                            if(dr.getValue("STORAGE_ID").toString().equals(storageID)){
                                //有暫存區，直接進暫存區
                                if (dr.getValue("BIN_TYPE").toString().equals("IT"))
                                    lstBin.add(dr.getValue("BIN_ID").toString());
                            }
                        }

                        if (lstBin.size() <= 0)
                        {
                            for (DataRow dr : dtStorageTempBin.Rows){
                                if(dr.getValue("STORAGE_ID").toString().equals(storageID)){
                                    //有暫存區，直接進暫存區
                                    if (dr.getValue("BIN_TYPE").toString().equals("IS"))
                                        lstBin.add(dr.getValue("BIN_ID").toString());
                                }
                            }
                        }

                        ShowBinDialog(storageID, lstBin);
                    }
                }
            }
        });
    }

    private List<WarehouseVoucherDetObj> CreateDetObj(DataTable dtLotID){
        List<WarehouseVoucherDetObj> lstDetObj = new ArrayList<WarehouseVoucherDetObj>();

        WarehouseVoucherDetObj det = null;
        for (DataRow dr : dtLotID.Rows){
            det = new WarehouseVoucherDetObj();

            det.setSeq(Double.parseDouble(dr.getValue("SEQ").toString()));
            det.setStorageId(dr.getValue("STORAGE_ID").toString());
            if(dr.getValue("WO_ID").toString().equals("*"))
                det.setWoId("");
            else
                det.setWoId(dr.getValue("WO_ID").toString());
            det.setItemId(dr.getValue("ITEM_ID").toString());
            det.setLotId(dr.getValue("LOT_ID").toString());
            det.setQty(Double.parseDouble(dr.getValue("QTY").toString()));
            det.setScrapQty(Double.parseDouble(dr.getValue("SCRAP_QTY").toString()));
            det.setTempBin(dr.getValue("TEMP_BIN").toString());
            try{
                if(dr.getValue("MFG_DATE") != null && !dr.getValue("MFG_DATE").toString().equals("")){
                    Date mfg = new SimpleDateFormat("yyyy-MM-dd").parse(dr.getValue("MFG_DATE").toString());
                    det.setMfgDate(mfg);
                }
                if(dr.getValue("EXP_DATE") != null && !dr.get("EXP_DATE").toString().equals("")){
                    Date exp = (new SimpleDateFormat("yyyy-MM-dd")).parse(dr.getValue("EXP_DATE").toString());
                    det.setExpDate(exp);
                }
            }catch (ParseException e){

            }
            det.setUom(dr.getValue("UOM").toString());
            det.setCmt(dr.getValue("CMT").toString());
            det.setSpecLot(dr.getValue("SPEC_LOT").toString());

            lstDetObj.add(det);
        }
        return lstDetObj;
    }

    public void setExpiryDate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.style_wms_dialog_date_picker_spinner, null);//layout
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);//物件
        //右邊簡化
        datePicker.setCalendarViewShown(false);
        //初始化
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
        //設置Dialog
        builder.setView(view);
        //標頭
        builder.setTitle(R.string.EXPIRY_DATE);
        final AlertDialog dialog = builder.create();
        dialog.show();

        Button bConfirm = view.findViewById(R.id.btnConfirm);
        bConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //日期格式
                StringBuffer sb = new StringBuffer();
                sb.append(String.format("%d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth()));
                holder.etExpiryDate.setText(sb);
                dialog.cancel();
            }
        });
    }

    private void GetListView() {
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        WarehouseStorageNonSheetGridAdapter adapter = new WarehouseStorageNonSheetGridAdapter(dtLotID, inflater);
        holder.lvWarehouseLot.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void onClickMFDDateClear(View v)
    {
        holder.etMFDDate.setText("");
    }

    public void onClickExpiryDateClear(View v)
    {
        holder.etExpiryDate.setText("");
    }

    private TabHost.OnTabChangeListener lsTabHost_OnTabChange = new TabHost.OnTabChangeListener(){
        @Override
        public void onTabChanged(String tabID){

        }
    };

    // 220708 Ikea 新增鏡頭掃描輸入
    private View.OnClickListener ibtnOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            IntentIntegrator integrator = new IntentIntegrator(WarehouseStorageNonSheetActivity.this);
            // 設置要掃描的條碼類型，ONE_D_CODE_TYPES：一維碼，QR_CODE_TYPES-二維碼
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("BarCode Scan"); //底部的提示文字
            integrator.setCameraId(0); //前置(1)或者後置(0)攝像頭
            integrator.setBeepEnabled(false); //掃描成功的「嗶嗶」聲
            integrator.setBarcodeImageEnabled(false); //是否保留掃碼成功時候的截圖
            integrator.setCaptureActivity(ScanActivity.class);
            integrator.initiateScan();
        }
    };

    //製造日期
    private AdapterView.OnClickListener lsMFDDate = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            setMFDDate();
        }
    };

    //有效日期
    private AdapterView.OnClickListener lsExpiryDate = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            setExpiryDate();
        }
    };

    //新增料號
    private AdapterView.OnClickListener lsAddLotID = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            addLotID();
        }
    };

    //更新
    private AdapterView.OnClickListener lsRefresh = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            initControl();
            initData();
            Refresh(true);
        }
    };

    //確認BIN
    private AdapterView.OnItemClickListener lsLotOnClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            List<String> lstStorageId = new ArrayList<String>();
            lstStorageId.add(dtLotID.getValue(position, "STORAGE_ID").toString());

            CheckStorageInTempBin(lstStorageId);
        }
    };

    //刪除Lot
    private AdapterView.OnItemLongClickListener lsLotListView = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            ShowLotDialog(position);
            return true;
        }
    };

    //入庫確認
    private AdapterView.OnClickListener lsConfirm = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            WarehouseStorageConfirm();
        }
    };

}

//Error Code WAPG010
//WAPG010001    請選擇倉庫
//WAPG010002    請選擇物料
//WAPG010003    請輸入數量
//WAPG010004    請輸入報廢數量
//WAPG010005    請選擇單據類型
//WAPG010006    請選擇部門
//WAPG010007    請輸入來源
//WAPG010008    是否刪除此批號?
//WAPG010009    儲存成功
//WAPG010010    倉庫[%s]尚未設定入庫儲位
//WAPG010011    尚未新增物料
//WAPG010012    請輸入批號
//WAPG010013    未設定入庫類型單據，無法產生系統的入庫單據