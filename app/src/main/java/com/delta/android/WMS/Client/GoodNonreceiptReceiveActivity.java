package com.delta.android.WMS.Client;

import android.content.Context;
import android.content.Intent;
import android.print.PrinterId;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.Layout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TabHost;
import android.view.View;
import android.widget.TextView;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseActivity;
import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.Activity.ScanActivity;
import com.delta.android.Core.DataTable.DataColumn;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.GenerateJsonNetString.MList;
import com.delta.android.Core.GenerateJsonNetString.MesClass;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionary;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionaryList;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.WMS.Client.GridAdapter.GoodNonreceiptReceiveGridAdapter;
import com.delta.android.WMS.Client.GridAdapter.GoodNonreceiptReceiveSNAdapter;
import com.delta.android.WMS.Client.GridAdapter.GoodNonreceiptReceiveSelectGridAdapter;
import com.delta.android.WMS.Param.BCreateReceiptSheetIDParam;
import com.delta.android.PMS.Client.InsWoSelectActivity;
import com.delta.android.R;
import com.delta.android.WMS.Param.BGoodReceiptReceiveEditParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.ParamObj.CheckCountObj;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.Iterator;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;


public class GoodNonreceiptReceiveActivity extends BaseFlowActivity {

    //region Public param
    private ViewHolder holder = null;
    public DataTable dtLotID;
    public DataTable dtSN;
    public DataTable dtTempSN;
    public DataTable dtStorageItem;
    private String strStorage = "";
    private String strItem = "";
    //For cmbbox use
    HashMap<String, String> mapSheetTypeKey = new HashMap<String, String>();
    HashMap<String, String> mapVendorKey = new HashMap<String, String>();
    HashMap<String, String> mapStorageKey = new HashMap<String, String>();
    HashMap<String, String> mapItemKey = new HashMap<String, String>();
    HashMap<String, String> mapCustomerKey = new HashMap<String, String>();
    HashMap<String, ArrayList<String>> mapStorageBin = new HashMap<String, ArrayList<String>>();
    HashMap<String, String> mapStorageBinResult = new HashMap<String, String>();

    HashMap<String, String> hmSheetTypeQtyStatus = new HashMap<String, String>(); //單據實際數量的狀態
    HashMap<String, String> hmItem = new HashMap<String, String>();
    HashMap<String, String> hmItemName = new HashMap<String, String>();
    HashMap<String, String> hmItemRegisterType = new HashMap<String, String>();
    HashMap<String, String> hmStorage = new HashMap<String, String>();
    HashMap<String, String> hmGrTypeID = new HashMap<String, String>();
    HashMap<String, String> hmVendorID = new HashMap<String, String>();
    HashMap<String, String> hmCustomerID = new HashMap<String, String>();
    ArrayList<String> alLotID = new ArrayList<String>();
    ArrayList<String> alCustomer = new ArrayList<>();
    //endregion

    static class ViewHolder{
        ListView lvReceiveLot;
        TabHost tabHost;

        //Mst
        Spinner cmbSheetType;
        Spinner cmbVendor;
        EditText etVendorShipNo;
        EditText etVendorShipDate;
        Spinner cmbCustomer;

        //Det
        Spinner cmbStorage;
        EditText etPONo;
        EditText etPOSeq;
        Spinner cmbItem;
        EditText etLotID;
        ImageButton ibtnLotIdQRScan; // 220707 Ikea 新增鏡頭掃描輸入
        CheckBox chkFIFOLot;
        EditText etQty;
        EditText etUOM;
        EditText etCMT;
        EditText etMFDDate;
        EditText etExpiryDate;
        Button btnAddLodID;

        Button btnRefresh;
        Button btnReceiveConfirm;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good_nonreceipt_receive);

        initControl();
        initData();
        setListensers();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, getResources().getText(R.string.QRCODE_SCAN_CANCEL).toString(), Toast.LENGTH_SHORT).show();
            } else {
                holder.etLotID.setText(result.getContents().trim());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //region Private Method
    private void initControl(){
        if (holder != null) return;

        holder = new ViewHolder();
        holder.tabHost = findViewById(R.id.tabHost);

        //Mst
        holder.cmbSheetType = findViewById(R.id.cmbReceiveSheetType);
        holder.cmbVendor = findViewById(R.id.cmbVendor);
        holder.etVendorShipNo = findViewById(R.id.etVendorShipNo);
        holder.etVendorShipDate = findViewById(R.id.etVendorShipDate);
        holder.etVendorShipDate.setInputType(InputType.TYPE_NULL);
        holder.cmbCustomer = findViewById(R.id.cmbCustomer);

        //Det
        holder.cmbStorage = findViewById(R.id.cmbStorage);
        holder.etPONo = findViewById(R.id.etPONo);
        holder.etPOSeq = findViewById(R.id.etPOSeq);
        holder.cmbItem = findViewById(R.id.cmbItem);
        holder.etLotID = findViewById(R.id.etLot);
        holder.ibtnLotIdQRScan = findViewById(R.id.ibtnLotIdQRScan); // 220707 Ikea 新增鏡頭掃描輸入
        holder.etQty = findViewById(R.id.etQty);
        holder.etUOM = findViewById(R.id.etUOM);
        holder.etCMT = findViewById(R.id.etCMT);
        holder.etMFDDate = findViewById(R.id.etMFDDate);
        holder.etMFDDate.setInputType(InputType.TYPE_NULL);
        holder.etExpiryDate = findViewById(R.id.etExpiryDate);
        holder.etExpiryDate.setInputType(InputType.TYPE_NULL);
        holder.btnAddLodID = findViewById(R.id.btnAddLot);
        holder.chkFIFOLot = findViewById(R.id.cbFIFOLot);

        holder.btnRefresh = findViewById(R.id.btnRefresh);
        holder.btnReceiveConfirm = findViewById(R.id.btnConfirm);
        holder.lvReceiveLot = findViewById(R.id.lvReceiveLot);
        holder.tabHost.setup();

        TabHost.TabSpec spec1 = holder.tabHost.newTabSpec("Mst");
        View tab1 = LayoutInflater.from(this).inflate(R.layout.activity_wms_good_pick_tab_widget,null);
        TextView tvTab1 = tab1.findViewById(R.id.tvTabText);
        tvTab1.setText(R.string.RECEIVE_INFO);
        spec1.setIndicator(tab1);
        spec1.setContent(R.id.Tab1);
        holder.tabHost.addTab(spec1);

        TabHost.TabSpec spec2 = holder.tabHost.newTabSpec("Det");
        View tab2 = LayoutInflater.from(this).inflate(R.layout.activity_wms_good_pick_tab_widget,null);
        TextView tvTab2 = tab2.findViewById(R.id.tvTabText);
        tvTab2.setText(R.string.RECEIVE_DET);
        spec2.setIndicator(tab2);
        spec2.setContent(R.id.Tab2);
        holder.tabHost.addTab(spec2);
    }

    private void initData(){
        GetInitCmbBox();
        CreateDatatableLotID();
    }

    private void Refresh(boolean refreshAll){

        if (refreshAll) {
            CreateDatatableLotID();

            //Mst
            holder.cmbSheetType.setSelection(0);
            holder.cmbVendor.setSelection(0);
            holder.etVendorShipNo.getText().clear();
            holder.etVendorShipDate.getText().clear();
            holder.cmbCustomer.setSelection(alCustomer.size()-1); // spinner設定回預設選項

            //Det
            holder.cmbStorage.setSelection(0);
            holder.etPONo.getText().clear();
            holder.etPOSeq.getText().clear();
            holder.cmbItem.setSelection(0);

            GetListView();
        }

        holder.etLotID.getText().clear();
        holder.etQty.getText().clear();
        holder.etUOM.getText().clear();
        holder.etCMT.getText().clear();
        holder.etMFDDate.getText().clear();
        holder.etExpiryDate.getText().clear();
        holder.chkFIFOLot.setChecked(false);
    }

    private void setListensers(){
        holder.tabHost.setOnTabChangedListener(lsTabHost_OnTabChange);
        holder.ibtnLotIdQRScan.setOnClickListener(ibtnOnClick); // 220707 Ikea 新增鏡頭掃描輸入
        holder.etVendorShipDate.setOnClickListener(lsVendorShipDate);
        holder.etMFDDate.setOnClickListener(lsMFDDate);
        holder.etExpiryDate.setOnClickListener(lsExpiryDate);
        holder.btnAddLodID.setOnClickListener(lsAddLotID);
        holder.btnReceiveConfirm.setOnClickListener(lsConfirm);
        holder.btnRefresh.setOnClickListener(lsRefresh);
        holder.lvReceiveLot.setOnItemClickListener(lsListViewOnClick);
        holder.lvReceiveLot.setOnItemLongClickListener(lsLotListView);
        holder.cmbItem.setOnItemSelectedListener(lsItemSelected);
    }

    private void GetInitCmbBox(){

        //region Set Param
        ArrayList<BModuleObject> lsBObj = new ArrayList<>();

        //單據類型
        BModuleObject bmObjSheetType = new BModuleObject();
        bmObjSheetType.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObjSheetType.setModuleID("BIFetchWmsSheetConfig");
        bmObjSheetType.setRequestID("GetSheetType");

        bmObjSheetType.params = new Vector<ParameterInfo>();
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Filter);
        param1.setParameterValue("  AND STP.SHEET_TYPE_POLICY_ID ='Receipt' AND TYP.SHEET_TYPE_ID = 'SysReceipt' ");
        bmObjSheetType.params.add(param1);
        lsBObj.add(bmObjSheetType);

        //廠商代碼
        BModuleObject bmObjVendor = new BModuleObject();
        bmObjVendor.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObjVendor.setModuleID("BIFetchVendor");
        bmObjVendor.setRequestID("GetVendor");
        lsBObj.add(bmObjVendor);

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

        //客戶
        BModuleObject bmObjCustomer = new BModuleObject();
        bmObjCustomer.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObjCustomer.setModuleID("BIFetchCustomer");
        bmObjCustomer.setRequestID("GetCustomer");
        lsBObj.add(bmObjCustomer);
        //endregion

        CallBIModule(lsBObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    DataTable dtSheetType = bModuleReturn.getReturnJsonTables().get("GetSheetType").get("SBRM_WMS_SHEET_CONFIG");
                    DataTable dtVendor = bModuleReturn.getReturnJsonTables().get("GetVendor").get("VENDOR");
                    DataTable dtStorage = bModuleReturn.getReturnJsonTables().get("GetStorage").get("STORAGE");
                    DataTable dtItem = bModuleReturn.getReturnJsonTables().get("GetItem").get("ITEM");
                    DataTable dtCustomer = bModuleReturn.getReturnJsonTables().get("GetCustomer").get("CUSTOMER");

                    ArrayList<String> alSheetType = new ArrayList<>();
                    ArrayList<String> alVendor = new ArrayList<>();
                    ArrayList<String> alStorage = new ArrayList<>();
                    ArrayList<String> alItem = new ArrayList<>();

                    if (dtSheetType.Rows.size() <= 0)
                    {
                        ShowMessage(R.string.WAPG009021); //WAPG009021    未設定收料類型單據，無法產生系統的收料單據
                        return;
                    }

                    for (int i = 0; i < dtSheetType.Rows.size(); i++) {
                        mapSheetTypeKey.put(dtSheetType.Rows.get(i).get("IDNAME").toString(), dtSheetType.Rows.get(i).get("SHEET_TYPE_KEY").toString());
                        alSheetType.add(i, dtSheetType.Rows.get(i).get("IDNAME").toString());

                        hmGrTypeID.put(dtSheetType.Rows.get(i).get("SHEET_TYPE_KEY").toString(), dtSheetType.Rows.get(i).get("SHEET_TYPE_ID").toString());
                        hmSheetTypeQtyStatus.put(dtSheetType.Rows.get(i).get("SHEET_TYPE_KEY").toString(), dtSheetType.Rows.get(i).get("ACTUAL_QTY_STATUS").toString());
                    }

                    for (int i = 0; i < dtVendor.Rows.size(); i++) {
                        mapVendorKey.put(dtVendor.Rows.get(i).get("IDNAME").toString(),dtVendor.Rows.get(i).get("VENDOR_KEY").toString());
                        alVendor.add(i,dtVendor.Rows.get(i).get("IDNAME").toString());

                        hmVendorID.put(dtVendor.Rows.get(i).get("VENDOR_KEY").toString(), dtVendor.Rows.get(i).get("VENDOR_ID").toString());
                    }

                    for (int i = 0; i < dtStorage.Rows.size(); i++) {
                        mapStorageKey.put(dtStorage.Rows.get(i).get("IDNAME").toString(),dtStorage.Rows.get(i).get("STORAGE_KEY").toString());
                        alStorage.add(i,dtStorage.Rows.get(i).get("IDNAME").toString());

                        hmStorage.put(dtStorage.Rows.get(i).get("STORAGE_KEY").toString(),dtStorage.Rows.get(i).get("STORAGE_ID").toString());
                    }

                    for (int i = 0; i < dtItem.Rows.size(); i++) {
                        mapItemKey.put(dtItem.Rows.get(i).get("IDNAME").toString(), dtItem.Rows.get(i).get("ITEM_KEY").toString());
                        alItem.add(i,dtItem.Rows.get(i).get("IDNAME").toString());

                        hmItem.put(dtItem.Rows.get(i).get("ITEM_KEY").toString(),dtItem.Rows.get(i).get("ITEM_ID").toString());
                        hmItemName.put(dtItem.Rows.get(i).get("ITEM_KEY").toString(),dtItem.Rows.get(i).get("ITEM_NAME").toString());
                        hmItemRegisterType.put(dtItem.Rows.get(i).get("ITEM_KEY").toString(),dtItem.Rows.get(i).get("REGISTER_TYPE").toString());
                    }

                    ArrayAdapter<String> adapterSheetType = new ArrayAdapter<>(GoodNonreceiptReceiveActivity.this, android.R.layout.simple_spinner_dropdown_item, alSheetType);
                    ArrayAdapter<String> adapterVendor = new ArrayAdapter<>(GoodNonreceiptReceiveActivity.this, android.R.layout.simple_spinner_dropdown_item, alVendor);
                    ArrayAdapter<String> adapterStorage = new ArrayAdapter<>(GoodNonreceiptReceiveActivity.this, android.R.layout.simple_spinner_dropdown_item, alStorage);
                    ArrayAdapter<String> adapterItem = new ArrayAdapter<>(GoodNonreceiptReceiveActivity.this, android.R.layout.simple_spinner_dropdown_item, alItem);

                    holder.cmbSheetType.setAdapter(adapterSheetType);
                    holder.cmbVendor.setAdapter(adapterVendor);
                    holder.cmbStorage.setAdapter(adapterStorage);
                    holder.cmbItem.setAdapter(adapterItem);

                    //20220719 archie add customerKey
                    alCustomer.clear();
                    for (int i = 0; i < dtCustomer.Rows.size(); i++) {
                        mapCustomerKey.put(dtCustomer.Rows.get(i).get("IDNAME").toString(), dtCustomer.Rows.get(i).get("CUSTOMER_KEY").toString());
                        alCustomer.add(i,dtCustomer.Rows.get(i).get("IDNAME").toString());

                        hmCustomerID.put(dtCustomer.Rows.get(i).get("CUSTOMER_KEY").toString(),dtCustomer.Rows.get(i).get("CUSTOMER_ID").toString());
                    }

                    Collections.sort(alCustomer); // List依據字母順序排序

                    // 下拉選單預設選項依語系調整
                    String strSelectCustomerId = getResString(getResources().getString(R.string.SELECT_CUSTOMER_ID));
                    alCustomer.add(strSelectCustomerId);

                    SimpleArrayAdapter adapter = new GoodNonreceiptReceiveActivity.SimpleArrayAdapter<>(GoodNonreceiptReceiveActivity.this, android.R.layout.simple_spinner_dropdown_item, alCustomer);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    holder.cmbCustomer.setAdapter(adapter);
                    holder.cmbCustomer.setSelection(alCustomer.size() - 1, true);
                }
            }
        });
    }

    private void CreateDatatableLotID() {

        //region dtLotID
        dtLotID = new DataTable();

        DataColumn dcStorageKey = new DataColumn("STORAGE_KEY");
        DataColumn dcStorageID = new DataColumn("STORAGE_ID");
        DataColumn dcItemKey = new DataColumn("ITEM_KEY");
        DataColumn dcItemID = new DataColumn("ITEM_ID");
        DataColumn dcItemName = new DataColumn("ITEM_NAME");
        DataColumn dcPONO = new DataColumn("PONO");
        DataColumn dcPOSeq = new DataColumn("PO_SEQ");
        DataColumn dcLotID = new DataColumn("LOT_ID");
        DataColumn dcFIFOLot = new DataColumn("SPEC_LOT");
        DataColumn dcQty = new DataColumn("QTY");
        DataColumn dcUOM = new DataColumn("UOM");
        DataColumn dcCMT = new DataColumn("CMT");
        DataColumn dcMFDDate = new DataColumn("MFG_DATE");
        DataColumn dcExpiryDate = new DataColumn("EXP_DATE");
        DataColumn dcSkipFlag = new DataColumn("SKIP_QC");
        DataColumn dcGrrDetSNRefKey = new DataColumn("GRR_DET_SN_REF_KEY");
        DataColumn dcRegisterType = new DataColumn("REGISTER_TYPE");
        DataColumn dcTempBin = new DataColumn("TEMP_BIN");

        dtLotID.addColumn(dcStorageKey);
        dtLotID.addColumn(dcStorageID);
        dtLotID.addColumn(dcItemKey);
        dtLotID.addColumn(dcItemID);
        dtLotID.addColumn(dcItemName);
        dtLotID.addColumn(dcPONO);
        dtLotID.addColumn(dcPOSeq);
        dtLotID.addColumn(dcLotID);
        dtLotID.addColumn(dcFIFOLot);
        dtLotID.addColumn(dcQty);
        dtLotID.addColumn(dcUOM);
        dtLotID.addColumn(dcCMT);
        dtLotID.addColumn(dcMFDDate);
        dtLotID.addColumn(dcExpiryDate);
        dtLotID.addColumn(dcSkipFlag);
        dtLotID.addColumn(dcGrrDetSNRefKey);
        dtLotID.addColumn(dcRegisterType);
        dtLotID.addColumn(dcTempBin);
        //endregion

        //region dtSN
        dtSN = new DataTable();
        DataColumn dcSNGrrDetSNRefKey = new DataColumn("GRR_DET_SN_REF_KEY");
        DataColumn dcSN = new DataColumn("SN_ID");
        dtSN.addColumn(dcSNGrrDetSNRefKey);
        dtSN.addColumn(dcSN);
        //endregion
    }

    public void setVendorShipDate() {
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
        builder.setTitle(R.string.VENDOR_SHIP_DATE);
        final AlertDialog dialog = builder.create();
        dialog.show();

        Button bConfirm = view.findViewById(R.id.btnConfirm);
        bConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //日期格式
                StringBuffer sb = new StringBuffer();
                sb.append(String.format("%d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth()));
                holder.etVendorShipDate.setText(sb);
                dialog.cancel();
            }
        });
    }

    public void setMFDDate(){
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

    public void setExpiryDate(){
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

    public void onClickVendorDateClear(View v) {
        holder.etVendorShipDate.setText("");
    }

    public void onClickMFDDateClear(View v) {
        holder.etMFDDate.setText("");
    }

    public void onClickExpiryDateClear(View v) {
        holder.etExpiryDate.setText("");
    }

    public void addLotID(){

        //region param
        //Det
        final String strStorageKey;
        final String strPONo;
        final int poSeq;
        final String strItemKey;
        final String strLotID;
        final boolean bFIFOLot;
        final int qty;
        final String strUOM;
        final String strCMT;
        final String strMFDDate;
        final String strExpiryDate;
        //endregion

        //region check param
        strStorageKey = mapStorageKey.get(holder.cmbStorage.getSelectedItem().toString());
        if (holder.cmbStorage.getSelectedItemPosition() == -1){
            ShowMessage(R.string.WAPG009003); //WAPG009003    請選擇倉庫
            return;
        }

        strItemKey = mapItemKey.get(holder.cmbItem.getSelectedItem().toString());
        if (holder.cmbItem.getSelectedItemPosition() == -1){
            ShowMessage(R.string.WAPG009004); //WAPG009004    請選擇物料
            return;
        }


        if (holder.etQty.getText().toString().equals("")){
            ShowMessage(R.string.WAPG009005); //WAPG009005    請輸入數量
            return;
        }else{
            qty = Integer.parseInt(holder.etQty.getText().toString());
        }

        if (holder.etMFDDate.getText().toString().equals("")){
            ShowMessage(R.string.WAPG009019); //WAPG009019    請輸入製造日期
            return;
        }else{
            strMFDDate = holder.etMFDDate.getText().toString().trim();
        }

        if (holder.etExpiryDate.getText().toString().equals("")){
            ShowMessage(R.string.WAPG009020); //WAPG009020    請輸入有效期限
            return;
        }else{
            strExpiryDate = holder.etExpiryDate.getText().toString().trim();
        }

        //region 檢查批號
        strLotID = holder.etLotID.getText().toString().trim();
        Object[] args = new Object[2];
        args[0] = hmItem.get(strItemKey);
        args[1] = hmItemRegisterType.get(strItemKey);

        if (hmItemRegisterType.get(strItemKey).equals("ItemID")){
            if (!strLotID.equals("*")){
                ShowMessage(R.string.WAPG009010, args); //WAPG009010   物料[%s]註冊類別為[%s]，不需要輸入批號!
                return;
            }
        }else{
            if (strLotID.equals("")){
                ShowMessage(R.string.WAPG009011, args); //WAPG009011   物料[%s]註冊類別為[%s]，需要輸入批號!
                return;
            }

            if (alLotID.contains(strLotID)){
                ShowMessage(R.string.WAPG009006); //WAPG009006    批號已存在
                return;
            }
        }
        //endregion

        strPONo = holder.etPONo.getText().toString().trim();
        if (!holder.etPOSeq.getText().toString().equals("")){
            poSeq = Integer.parseInt(holder.etPOSeq.getText().toString());
        }else {
            poSeq = 0;
        }
        strUOM = holder.etUOM.getText().toString().trim();
        strCMT = holder.etCMT.getText().toString().trim();
        if (holder.chkFIFOLot.isChecked()){
            bFIFOLot = true;
        }else{
            bFIFOLot = false;
        }

        alLotID.add(strLotID);
        //endregion

        final String strVendorKey = mapVendorKey.get(holder.cmbVendor.getSelectedItem().toString());
        String strVKey = String.format("%s','%s", strVendorKey, "*");
        String strKey = String.format("%s','%s", strItemKey, "*");

        String strFilter = String.format(" AND A.VENDOR_KEY IN ('%s') AND A.ITEM_KEY IN ('%s')", strVKey, strKey);

        //region Set BIModule
        // BIModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchVendorItemIQC");
        bmObj.setRequestID("BIFetchVendorItemIQC");
        bmObj.params = new Vector<ParameterInfo>();


        // Input param
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Filter);
        param1.setParameterValue(strFilter); // 要用set"Net"ParameterValue
        bmObj.params.add(param1);

        // endregion

        CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(CheckBModuleReturnInfo(bModuleReturn))
                {
                    DataTable dt = bModuleReturn.getReturnJsonTables().get("BIFetchVendorItemIQC").get("VENDOR_ITEM_IQC");
                    String strSkipQc ="";

                    //region 本身供應商+本身物料
                    for (DataRow d : dt.Rows)
                    {
                        if (d.getValue("VENDOR_KEY").toString().equals(strVendorKey) && d.getValue("ITEM_KEY").toString().equals(strItemKey))
                        {
                            strSkipQc = d.getValue("SKIP_QC").toString();
                            break;
                        }
                    }
                    //endregion

                    //region 本身供應商+*物料
                    if (strSkipQc.equals(""))
                    {
                        for (DataRow d : dt.Rows)
                        {
                            if (d.getValue("VENDOR_KEY").toString().equals(strVendorKey) && d.getValue("ITEM_KEY").toString().equals("*"))
                            {
                                strSkipQc = d.getValue("SKIP_QC").toString();
                                break;
                            }
                        }
                    }
                    //endregion

                    //region *供應商+本身物料
                    if (strSkipQc.equals(""))
                    {
                        for (DataRow d : dt.Rows)
                        {
                            if (d.getValue("VENDOR_KEY").toString().equals("*") && d.getValue("ITEM_KEY").toString().equals(strItemKey))
                            {
                                strSkipQc = d.getValue("SKIP_QC").toString();
                                break;
                            }
                        }
                    }
                    //endregion

                    //region *供應商+*物料
                    if (strSkipQc.equals(""))
                    {
                        for (DataRow d : dt.Rows)
                        {
                            if (d.getValue("VENDOR_KEY").toString().equals("*") && d.getValue("ITEM_KEY").toString().equals("*"))
                            {
                                strSkipQc = d.getValue("SKIP_QC").toString();
                                break;
                            }
                        }
                    }
                    //endregion

                    if (strSkipQc.equals(""))
                    {
                        ShowMessage(R.string.WAPG009017, hmVendorID.get(strVendorKey));
                        return;
                    }

                    //region Add New LotID
                    DataRow drNew = dtLotID.newRow();
                    drNew.setValue("STORAGE_KEY",strStorageKey);
                    drNew.setValue("STORAGE_ID",hmStorage.get(strStorageKey));
                    drNew.setValue("ITEM_KEY",strItemKey);
                    drNew.setValue("ITEM_ID",hmItem.get(strItemKey));
                    drNew.setValue("ITEM_NAME",hmItemName.get(strItemKey));
                    drNew.setValue("PONO",strPONo);
                    drNew.setValue("PO_SEQ",poSeq);
                    drNew.setValue("LOT_ID",strLotID);
                    drNew.setValue("QTY",qty);
                    drNew.setValue("UOM",strUOM);
                    drNew.setValue("CMT",strCMT);
                    drNew.setValue("MFG_DATE",strMFDDate);
                    drNew.setValue("EXP_DATE",strExpiryDate);
                    if (bFIFOLot){
                        drNew.setValue("SPEC_LOT","Y");
                    }else{
                        drNew.setValue("SPEC_LOT","N");
                    }

                    drNew.setValue("SKIP_QC",strSkipQc);
                    drNew.setValue("REGISTER_TYPE", hmItemRegisterType.get(strItemKey));

                    dtLotID.Rows.add(drNew);
                    //endregion

                    GetListView();
                    Refresh(false);
                    holder.cmbVendor.setEnabled(false);
                }
            }
        });
    }

    private void GetListView(){

        if (dtLotID.Rows.size() <= 0)
            holder.cmbVendor.setEnabled(true);

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        GoodNonreceiptReceiveGridAdapter adapter = new GoodNonreceiptReceiveGridAdapter(dtLotID, inflater);
        holder.lvReceiveLot.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void CreateSheetID()
    {
        final String strSheetType;

        strSheetType = mapSheetTypeKey.get(holder.cmbSheetType.getSelectedItem().toString());
        if (holder.cmbSheetType.getSelectedItemPosition() == -1) {
            ShowMessage(R.string.WAPG009001); //WAPG009001    請選擇單據類型
            return;
        }

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchGRSheetID");
        bmObj.setRequestID("BIFetchGRSheetID");
        bmObj.params = new Vector<ParameterInfo>();

        //ParameterInfo parameterInfo = new ParameterInfo();
        //parameterInfo.setParameterID(BCreateReceiptSheetIDParam.SheetTypeID);
        //parameterInfo.setParameterValue(strSheetType);
        //bmObj.params.add(parameterInfo);
        ParameterInfo parameterInfo = new ParameterInfo();
        parameterInfo.setParameterID(BIWMSFetchInfoParam.SysKeyCount);
        int iCount = 1;
        parameterInfo.setParameterValue(iCount);
        bmObj.params.add(parameterInfo);

        CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(!CheckBModuleReturnInfo(bModuleReturn)) return;

                Gson gson = new Gson();
                String strSheetID;
                strSheetID = gson.fromJson(bModuleReturn.getReturnList().get("BIFetchGRSheetID").get("PDASysKey").toString(), String.class);

                ReceiveConfirm(strSheetID, strSheetType);
            }
        });
    }

    private void CheckStorageInTempBin(List<String> lstStorage)
    {
        final List<String> lstStrageID = lstStorage;
        mapStorageBin = new HashMap<String, ArrayList<String>>();

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchBin");
        bmObj.setRequestID("BIFetchBin");

        bmObj.params = new Vector<ParameterInfo>();
        //裝Condition的容器
        final HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
        List<Condition> lstCondition = new ArrayList<Condition>();
        for (int i = 0; i < lstStrageID.size(); i++)
        {
            Condition cond = new Condition();
            cond.setAliasTable("S");
            cond.setColumnName("STORAGE_ID");
            cond.setValue(lstStrageID.get(i));
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

        boolean bSkipQC = false;
        for(DataRow lot : dtLotID.Rows){
            String strQC = lot.getValue("SKIP_QC").toString();
            if(strQC.equals("Y")){
                bSkipQC = true;
                break;
            }
        }

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIWMSFetchInfoParam.Filter);
        if(bSkipQC)
            param2.setParameterValue("AND B.BIN_TYPE IN ('IT', 'IS')");
        else
            param2.setParameterValue("AND B.BIN_TYPE IN ('IT', 'IS', 'IQC')");
        bmObj.params.add(param2);

        //Call BIModule
        CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(CheckBModuleReturnInfo(bModuleReturn)){
                    DataTable dtStorageTempBin = bModuleReturn.getReturnJsonTables().get("BIFetchBin").get("BIN");

                    //有暫存區，就直接到暫存區
                    for (final String storageID: lstStrageID)
                    {
                        for (DataRow dr : dtStorageTempBin.Rows)
                        {
                            if(dr.getValue("STORAGE_ID").toString().equals(storageID))
                            {
                                if(!mapStorageBin.containsKey(storageID))
                                {
                                    ArrayList<String> lstBin = new ArrayList<>();

                                    if(dr.getValue("BIN_TYPE").toString().equals("IT"))
                                        lstBin.add(dr.getValue("BIN_ID").toString());

                                    mapStorageBin.put(storageID, lstBin);
                                }
                                else
                                {
                                    if(dr.getValue("BIN_TYPE").toString().equals("IT"))
                                        mapStorageBin.get(storageID).add(dr.getValue("BIN_ID").toString());
                                }
                            }
                        }
                    }

                    for (final String storageID: lstStrageID)
                    {
                        for (DataRow dr : dtStorageTempBin.Rows)
                        {
                            if(dr.getValue("STORAGE_ID").toString().equals(storageID))
                            {
                                if (mapStorageBin.get(storageID).size() <= 0)
                                {
                                    if(dr.getValue("BIN_TYPE").toString().equals("IS"))
                                        mapStorageBin.get(storageID).add(dr.getValue("BIN_ID").toString());
                                }
                            }
                        }
                    }
                    ShowConfirmDialog();
                }
            }
        });
    }

    private void ShowConfirmDialog(){
        LayoutInflater inflater = LayoutInflater.from(GoodNonreceiptReceiveActivity.this);
        final View viewConfirm = inflater.inflate(R.layout.activity_good_nonreceipt_receive_confirm, null);
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(GoodNonreceiptReceiveActivity.this);
        builder.setView(viewConfirm);

        final android.app.AlertDialog dialogConfirm = builder.create();
        dialogConfirm.setCancelable(false);
        dialogConfirm.show();

        //取得控制項
        final ListView lvStorageItem = dialogConfirm.findViewById(R.id.lvReceiveLot);
        Button btnSelectConfirm = dialogConfirm.findViewById(R.id.btnBinConfirm);

        //整理資料(by 倉庫+物料)
        ArrayList<String> lstTemp = new ArrayList<String>();
        dtStorageItem = new DataTable();
        DataColumn dcItemId = new DataColumn("ITEM_ID");
        DataColumn dcItemName = new DataColumn("ITEM_NAME");
        DataColumn dcStorage = new DataColumn("STORAGE_ID");
        DataColumn dcBin = new DataColumn("BIN_ID");

        dtStorageItem.addColumn(dcItemId);
        dtStorageItem.addColumn(dcItemName);
        dtStorageItem.addColumn(dcStorage);
        dtStorageItem.addColumn(dcBin);

        for (DataRow dr : dtLotID.Rows){
            String strSelectKey = dr.getValue("STORAGE_ID").toString() + "_" + dr.getValue("ITEM_ID").toString();
            //當取得的儲位不只一個時才加入
            /*if(mapStorageBin.containsKey(dr.getValue("STORAGE_ID").toString())){
                if(mapStorageBin.get(dr.getValue("STORAGE_ID").toString()).size() < 1) // 220708 Ikea 原為<=1會使只有一個儲位的情況顯示不出來，無法進行後續作業
                    continue;
            }*/

            if(!lstTemp.contains(strSelectKey)){
                lstTemp.add(strSelectKey);
                DataRow drNew = dtStorageItem.newRow();
                drNew.setValue("ITEM_ID", dr.getValue("ITEM_ID").toString());
                drNew.setValue("ITEM_NAME", dr.getValue("ITEM_NAME").toString());
                drNew.setValue("STORAGE_ID", dr.getValue("STORAGE_ID").toString());
                drNew.setValue("BIN_ID", "");
                dtStorageItem.Rows.add(drNew);
            }
        }

        LayoutInflater inflaterSelect = LayoutInflater.from(getApplicationContext());
        GoodNonreceiptReceiveSelectGridAdapter adapterSelect = new GoodNonreceiptReceiveSelectGridAdapter(dtStorageItem, inflaterSelect);
        lvStorageItem.setAdapter(adapterSelect);
        adapterSelect.notifyDataSetChanged();

        lvStorageItem.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
               LayoutInflater inflaterBin = LayoutInflater.from(GoodNonreceiptReceiveActivity.this);
               final View viewBin = inflaterBin.inflate(R.layout.activity_good_nonreceipt_receive_confirm_select_bin, null);
               final android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.Builder(GoodNonreceiptReceiveActivity.this);
               builder1.setView(viewBin);

               final android.app.AlertDialog dialogBin = builder1.create();
               dialogBin.setCancelable(false);
               dialogBin.show();

               final Spinner cmbBin = dialogBin.findViewById(R.id.cmbBinID);
               final Button btnBinConfirm = dialogBin.findViewById(R.id.btnBinConfirm);

               strStorage = dtStorageItem.getValue(position, "STORAGE_ID").toString();
               strItem = dtStorageItem.getValue(position, "ITEM_ID").toString();
               ArrayAdapter<String> adapterBin = new ArrayAdapter<>(GoodNonreceiptReceiveActivity.this, android.R.layout.simple_spinner_dropdown_item, mapStorageBin.get(strStorage));
                cmbBin.setAdapter(adapterBin);

                cmbBin.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l){
                        //dtStorageItem.setValue(position, "BIN_ID", mapStorageBin.get(strStorage).get(position));
                        for(DataRow dr : dtStorageItem.Rows){
                            if(dr.getValue("STORAGE_ID").toString().equals(strStorage) &&
                                    dr.getValue("ITEM_ID").toString().equals(strItem)){
                                dr.setValue("BIN_ID", mapStorageBin.get(strStorage).get(position));
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView){
                        //do nothing
                    }
                });

                btnBinConfirm.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        //refreash listview
                        LayoutInflater inflaterSelect = LayoutInflater.from(getApplicationContext());
                        GoodNonreceiptReceiveSelectGridAdapter adapterSelect = new GoodNonreceiptReceiveSelectGridAdapter(dtStorageItem, inflaterSelect);
                        lvStorageItem.setAdapter(adapterSelect);

                        dialogBin.dismiss();
                    }
                });
            }
        });

        btnSelectConfirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                for(DataRow dr : dtStorageItem.Rows){
                    if(dr.getValue("BIN_ID").toString().equals("")){
                        Object[] args = new Object[2];
                        args[0] = dr.getValue("STORAGE_ID").toString();
                        args[1] = dr.getValue("ITEM_ID").toString();
                        ShowMessage(R.string.WAPG009018, args); //WAPG009018    尚未選擇倉庫[%s]物料[%s]對應儲位
                        return;
                    }
                }
                dialogConfirm.dismiss();

                //帶入TEMP_BIN
                for(DataRow drLot : dtLotID.Rows){
                    String storage = drLot.getValue("STORAGE_ID").toString();
                    String itemid = drLot.getValue("ITEM_ID").toString();
                    for (DataRow dr : dtStorageItem.Rows){
                        if(dr.getValue("STORAGE_ID").toString().equals(storage) &&
                        dr.getValue("ITEM_ID").toString().equals(itemid)){
                            drLot.setValue("TEMP_BIN", dr.getValue("BIN_ID").toString());
                            if(!mapStorageBinResult.containsKey(storage)){
                                mapStorageBinResult.put(storage, dr.getValue("BIN_ID").toString());
                            }
                        }
                    }
                }

                CreateSheetID();
            }
        });
    }

    private void ReceiveConfirm(String strSheetID, String strSheetType){
        if(strSheetID.equals("")) return;
        if(dtLotID.Rows.size() <= 0) {
            ShowMessage(R.string.WAPG009016);
            return;
        }
        if(!CheckSN()) return;

        String strVendor;
        String strVendorShipNo;
        String strVendorShipDate;

        strVendor = mapVendorKey.get(holder.cmbVendor.getSelectedItem().toString());
        if (holder.cmbVendor.getSelectedItemPosition() == -1){
            ShowMessage(R.string.WAPG009002); //WAPG009002    請選擇廠商
            return;
        }

        strVendorShipNo = holder.etVendorShipNo.getText().toString().trim();
        strVendorShipDate = holder.etVendorShipDate.getText().toString().trim();

        String strCustomer = "";
        if (holder.cmbVendor.getSelectedItemPosition() > -1){
            strCustomer = mapCustomerKey.get(holder.cmbCustomer.getSelectedItem().toString());
        }

        //region  Create Mst Table
        DataTable dtMst = new DataTable();
        DataColumn dcGrID = new DataColumn("GR_ID");
        DataColumn dcGrTypeID = new DataColumn("GR_TYPE_ID");
        DataColumn dcGrTypeKey = new DataColumn("GR_TYPE_KEY");
        DataColumn dcGrSource = new DataColumn("GR_SOURCE");
        DataColumn dcVendorID = new DataColumn("VENDOR_ID");
        DataColumn dcVendorKey = new DataColumn("VENDOR_KEY");
        DataColumn dcVendorShipNo = new DataColumn("VENDOR_SHIP_NO");
        DataColumn dcVendorShipDate = new DataColumn("VENDOR_SHIP_DATE");
        DataColumn dcCustomerID = new DataColumn("CUSTOMER_ID");
        DataColumn dcCustomerKey = new DataColumn("CUSTOMER_KEY");
        dtMst.addColumn(dcGrID);
        dtMst.addColumn(dcGrTypeID);
        dtMst.addColumn(dcGrTypeKey);
        dtMst.addColumn(dcGrSource);
        dtMst.addColumn(dcVendorID);
        dtMst.addColumn(dcVendorKey);
        dtMst.addColumn(dcVendorShipNo);
        dtMst.addColumn(dcVendorShipDate);
        dtMst.addColumn(dcCustomerID);
        dtMst.addColumn(dcCustomerKey);

        DataRow drNewMst = dtMst.newRow();
        drNewMst.setValue("GR_ID", strSheetID);
        drNewMst.setValue("GR_TYPE_ID", hmGrTypeID.get(strSheetType));
        drNewMst.setValue("GR_TYPE_KEY", strSheetType);
        drNewMst.setValue("GR_SOURCE", "WMS");
        drNewMst.setValue("VENDOR_ID", hmVendorID.get(strVendor));
        drNewMst.setValue("VENDOR_KEY", strVendor);
        drNewMst.setValue("VENDOR_SHIP_NO", strVendorShipNo);
        drNewMst.setValue("VENDOR_SHIP_DATE", strVendorShipDate);
        drNewMst.setValue("CUSTOMER_ID", hmCustomerID.get(strCustomer));
        drNewMst.setValue("CUSTOMER_KEY", strCustomer);

        dtMst.Rows.add(drNewMst);
        //endregion

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

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BGoodReceiptReceiveNonSheet");
        bmObj.setModuleID("");
        bmObj.setRequestID("GRR");
        bmObj.params = new Vector<ParameterInfo>();

        BGoodReceiptReceiveEditParam sheet = new BGoodReceiptReceiveEditParam();
        BGoodReceiptReceiveEditParam.GrEditMasterObj sheetMstObj = sheet.new GrEditMasterObj();

        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.INV.Parameter.GrEditMasterObj","bmWMS.INV.Param"); // BGoodReceiptReceiveEditParam.GrMasterObj
        MesClass mesClassEnum = new MesClass(vListEnum);
        String strGrrMstObj = mesClassEnum.generateFinalCode(sheetMstObj.GetGrrSheet(dtMst, dtLotID, dtSN));

        VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create(VirtualClass.VirtualClassType.String);
        MesSerializableDictionary msd = new MesSerializableDictionary(vkey, vVal);
        String strTempBin = msd.generateFinalCode(mapStorageBinResult);

        VirtualClass vListEnum2 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.CheckCountObj", "bmWMS.Library.Param");
        MList mListEnum = new MList(vListEnum2);
        String strCheckCountObj = mListEnum.generateFinalCode(lstChkCountObj);

        ParameterInfo param = new ParameterInfo();
        param.setParameterID(BGoodReceiptReceiveEditParam.GrMasterObj);
        param.setNetParameterValue(strGrrMstObj);
        bmObj.params.add(param);

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BGoodReceiptReceiveEditParam.StorageInTempBin);
        param1.setNetParameterValue(strTempBin);
        bmObj.params.add(param1);

        ParameterInfo paramExecuteChkStock = new ParameterInfo();
        paramExecuteChkStock.setParameterID(BGoodReceiptReceiveEditParam.ExecuteCheckStock); // 20220805 Add by Ikea 是否執行盤點檢查
        paramExecuteChkStock.setNetParameterValue2("true");
        bmObj.params.add(paramExecuteChkStock);

        ParameterInfo paramChkCountObj = new ParameterInfo(); // 20220805 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中
        paramChkCountObj.setParameterID(BGoodReceiptReceiveEditParam.CheckCountObj);
        paramChkCountObj.setNetParameterValue(strCheckCountObj);
        bmObj.params.add(paramChkCountObj);

        CallBModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (!CheckBModuleReturnInfo(bModuleReturn)) return;

                ShowMessage(R.string.WAPG009007); //WAPG009007    作業成功
                Refresh(true);
            }
        });
    }

    private void ShowLotDialog(int position){

        final int intLotPosition = position;

        //region init dialog
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        final View viewLot = inflater.inflate(R.layout.activity_good_nonreceipt_receive_lot_delete, null);
        final android.app.AlertDialog.Builder builder =  new android.app.AlertDialog.Builder(GoodNonreceiptReceiveActivity.this);
        builder.setView(viewLot);

        final android.app.AlertDialog dialogLot = builder.create();
        dialogLot.setCancelable(false);//只允許按下dialog裡的按鍵才能關閉dialog
        dialogLot.show();

        TextView tvCurrentSN = dialogLot.findViewById(R.id.tvCurrentLot);
        String strErr = getResources().getString(R.string.WAPG009014); //WAPG009014    是否刪除此批號?
        tvCurrentSN.setText(strErr);
        //endregion

        //region 確認清除
        Button btnDelete = viewLot.findViewById(R.id.btnYes);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dtLotID.Rows.get(intLotPosition).getValue("GRR_DET_SN_REF_KEY") != null){
                    //移除該 Lot已收的SN
                    String strRefKey = dtLotID.Rows.get(intLotPosition).getValue("GRR_DET_SN_REF_KEY").toString();
                    for (int i = dtSN.Rows.size() -1; i >= 0; i--){
                        if (dtSN.Rows.get(i).getValue("GRR_DET_SN_REF_KEY").toString().equals(strRefKey)) {
                            dtSN.Rows.remove(i);
                        }
                    }
                }

                //移除該 Lot
                alLotID.remove(dtLotID.Rows.get(intLotPosition).getValue("LOT_ID").toString());
                dtLotID.Rows.remove(intLotPosition);

                dialogLot.dismiss();
                GetListView();
            }
        });
        //endregion

        //region 取消清除
        Button btnCancel = viewLot.findViewById(R.id.btnNo);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogLot.dismiss();
            }
        });
        //endregion
    }

    private void ShowSNDialog(final String strGrrDetSNRefKey, int snQty, int position){
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        final View viewDialog = inflater.inflate(R.layout.activity_good_nonreceipt_receive_lot_sn,null );
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(GoodNonreceiptReceiveActivity.this);
        builder.setView(viewDialog);

        final android.app.AlertDialog dialog = builder.create();
        dialog.setCancelable(false);//只允許按下dialog裡的按鍵才能關閉dialog
        dialog.show();

        final int intSNQty = snQty;
        final int intLotNoPosition = position;

        //region create dtTempSN
        final ArrayList<String> alTempSN = new ArrayList<String>();
        dtTempSN = new DataTable();
        DataColumn dcGrrDetSNRefKey = new DataColumn("GRR_DET_SN_REF_KEY");
        DataColumn dcSN = new DataColumn("SN_ID");
        dtTempSN.addColumn(dcGrrDetSNRefKey);
        dtTempSN.addColumn(dcSN);

        if (dtSN.Rows.size() > 0) {
            for (int i = dtSN.Rows.size() -1; i >= 0; i--) {

                String refKey = dtSN.Rows.get(i).getValue("GRR_DET_SN_REF_KEY").toString();
                String snID = dtSN.Rows.get(i).getValue("SN_ID").toString();
                if (refKey.equals(strGrrDetSNRefKey)) {

                    //把 dtSN符合 Grr_Det_SN_Ref_Key的資料搬到 dtTempSN
                    DataRow drNew = dtTempSN.newRow();
                    drNew.setValue("GRR_DET_SN_REF_KEY", refKey);
                    drNew.setValue("SN_ID", snID);
                    dtTempSN.Rows.add(drNew);
                    alTempSN.add(snID);

                    dtSN.Rows.remove(i);
                }
            }
        }

        LayoutInflater inflaterSNTemp = LayoutInflater.from(getApplicationContext());
        GoodNonreceiptReceiveSNAdapter adapter = new GoodNonreceiptReceiveSNAdapter(dtTempSN, inflaterSNTemp);
        ListView lsSN = viewDialog.findViewById(R.id.lvReceiveSN);
        lsSN.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        //endregion

        //region 刷入SN
        EditText etSN = viewDialog.findViewById(R.id.edSnId);
        etSN.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
                if (event.getAction() == KeyEvent.ACTION_DOWN){

                    EditText etSN = view.findViewById(R.id.edSnId);
                    String snID = etSN.getText().toString().trim();

                    // 判斷SN是否重複
                    if (alTempSN.contains(snID)){
                        ShowMessage(R.string.WAPG009015);//WAPG009015    序號重複!
                        return false;
                    }

                    DataRow drNew = dtTempSN.newRow();
                    drNew.setValue("GRR_DET_SN_REF_KEY",strGrrDetSNRefKey);
                    drNew.setValue("SN_ID",snID);
                    dtTempSN.Rows.add(drNew);
                    alTempSN.add(snID);

                    // ListView 顯示
                    LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                    GoodNonreceiptReceiveSNAdapter adapter = new GoodNonreceiptReceiveSNAdapter(dtTempSN, inflater);
                    ListView lsSN = viewDialog.findViewById(R.id.lvReceiveSN);
                    lsSN.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    // 清空刷入的欄位
                    etSN.getText().clear();
                    return true;
                }
                return false;
            }
        });
        //endregion

        //region ListView 事件
        ListView lvSNs = viewDialog.findViewById(R.id.lvReceiveSN);
        lvSNs.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (dtTempSN.Rows.size() > 0){
                    final DataRow chooseRow = dtTempSN.Rows.get(position);

                    //region 跳出詢問視窗
                    LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                    final View viewSN = inflater.inflate(R.layout.activity_good_nonreceipt_receive_lot_sn_delete,null );
                    final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(GoodNonreceiptReceiveActivity.this);
                    builder.setView(viewSN);

                    final android.app.AlertDialog dialogSN = builder.create();
                    dialogSN.setCancelable(false);//只允許按下dialog裡的按鍵才能關閉dialog
                    dialogSN.show();

                    TextView tvCurrentSN = dialogSN.findViewById(R.id.tvCurrentSN);
                    String strErr = getResources().getString(R.string.WAPG009008); //WAPG009008    是否刪除此序號?
                    tvCurrentSN.setText(strErr);
                    //endregion

                    //region 確認清除
                    Button btnDelete = viewSN.findViewById(R.id.btnYes);
                    btnDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dtTempSN.Rows.remove(chooseRow);
                            alTempSN.remove(chooseRow.getValue("SN_ID").toString());

                            // Refresh ListView
                            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                            GoodNonreceiptReceiveSNAdapter adapter = new GoodNonreceiptReceiveSNAdapter(dtTempSN, inflater);
                            ListView lsSN = viewDialog.findViewById(R.id.lvReceiveSN);
                            lsSN.setAdapter(adapter);
                            adapter.notifyDataSetChanged();

                            dialogSN.dismiss();
                        }
                    });
                    //endregion

                    //region 取消清除
                    Button btnCancel = viewSN.findViewById(R.id.btnNo);
                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialogSN.dismiss();
                        }
                    });
                    //endregion
                }
                return false;
            }
        });
        //endregion

        //region Confirm 按鈕事件
        Button btnConfirmDialog = viewDialog.findViewById(R.id.btnSNConfirm);
        btnConfirmDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (dtTempSN.Rows.size() < intSNQty){
                    ShowMessage(R.string.WAPG009009);//WAPG009009    未滿需求量!
                    return;
                }
                if(dtTempSN.Rows.size() > intSNQty){
                    ShowMessage(R.string.WAPG009012); //WAPG009012    已超出需求量!
                    return;
                }

                //移動 dtTempSN到 dtSN
                for (int i = 0 ; i < dtTempSN.Rows.size(); i++){
                    DataRow drNew = dtSN.newRow();
                    drNew.setValue("GRR_DET_SN_REF_KEY",dtTempSN.Rows.get(i).get("GRR_DET_SN_REF_KEY").toString());
                    drNew.setValue("SN_ID",dtTempSN.Rows.get(i).get("SN_ID").toString());
                    dtSN.Rows.add(drNew);
                }

                dtLotID.Rows.get(intLotNoPosition).setValue("GRR_DET_SN_REF_KEY",dtTempSN.Rows.get(0).get("GRR_DET_SN_REF_KEY").toString());

                dtTempSN = null;
                alTempSN.clear();
                dialog.dismiss();
            }
        });
        //endregion

        //region Cancel 按鈕事件
        Button btnCloseDialog = viewDialog.findViewById(R.id.btnSNCancel);
        btnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //移動 dtTempSN到 dtSN
                for (int i = 0 ; i < dtTempSN.Rows.size(); i++){
                    DataRow drNew = dtSN.newRow();
                    drNew.setValue("GRR_DET_SN_REF_KEY",dtTempSN.Rows.get(i).get("GRR_DET_SN_REF_KEY").toString());
                    drNew.setValue("SN_ID",dtTempSN.Rows.get(i).get("SN_ID").toString());
                    dtSN.Rows.add(drNew);
                }

                dtLotID.Rows.get(intLotNoPosition).setValue("GRR_DET_SN_REF_KEY",dtTempSN.Rows.get(0).get("GRR_DET_SN_REF_KEY").toString());

                dtTempSN = null;
                dialog.dismiss();
            }
        });
        //endregion
    }

    private boolean CheckSN(){

        List<String> lsUnfulfillLot = new ArrayList<String>();

        //region 1.先檢查by SN收料是否有 refKey，沒有代表沒有收SN
        for (int i = 0; i < dtLotID.Rows.size(); i++){
            if (dtLotID.Rows.get(i).getValue("REGISTER_TYPE").toString().equals("PcsSN") &&
                    dtLotID.Rows.get(i).getValue("GRR_DET_SN_REF_KEY") == null){

                lsUnfulfillLot.add(dtLotID.Rows.get(i).getValue("LOT_ID").toString());
            }
        }
        //endregion

        //region 2.有 refKey，確認是否收齊
        //取出有 refKey的 LotID，和該 LotID所需的SN個數
        DataTable dtLotSNQty = new DataTable();
        DataColumn dcLotID = new DataColumn("LOT_ID");
        DataColumn dcGrrDetSNRefKey = new DataColumn("GRR_DET_SN_REF_KEY");
        DataColumn dcQty = new DataColumn("QTY");

        dtLotSNQty.addColumn(dcLotID);
        dtLotSNQty.addColumn(dcGrrDetSNRefKey);
        dtLotSNQty.addColumn(dcQty);

        for(int i = 0; i < dtLotID.Rows.size(); i++){
            if (dtLotID.Rows.get(i).getValue("GRR_DET_SN_REF_KEY") != null){

                DataRow drNew = dtLotSNQty.newRow();
                drNew.setValue("LOT_ID", dtLotID.Rows.get(i).getValue("LOT_ID").toString());
                drNew.setValue("GRR_DET_SN_REF_KEY", dtLotID.Rows.get(i).getValue("GRR_DET_SN_REF_KEY").toString());
                drNew.setValue("QTY", dtLotID.Rows.get(i).getValue("QTY").toString());
                dtLotSNQty.Rows.add(drNew);
            }
        }

        //計算所收的SN數量是否滿足需求量
        for (int i = 0; i < dtLotSNQty.Rows.size(); i++){

            //找出所收的SN數量
            int receiveQty = 0;
            String lotID = dtLotSNQty.Rows.get(i).getValue("LOT_ID").toString();
            String refKey = dtLotSNQty.Rows.get(i).getValue("GRR_DET_SN_REF_KEY").toString();
            int needQty = Integer.parseInt( dtLotSNQty.Rows.get(i).getValue("QTY").toString());
            for (int j = 0; j < dtSN.Rows.size(); j++){
                if (refKey == dtSN.Rows.get(j).getValue("GRR_DET_SN_REF_KEY").toString()){
                    receiveQty ++;
                }
            }

            //比對需求量和收的數量
            if (needQty > receiveQty){
                lsUnfulfillLot.add(lotID);
            }
        }
        //endregion

        if (lsUnfulfillLot.size() > 0){
            Object[] args = new Object[1];
            args[0] = TextUtils.join(", ", lsUnfulfillLot);
            ShowMessage(R.string.WAPG009013, args); //WAPG009013    批號[%s]序號尚未收滿!
            return false;
        }
        return true;
    }

    private void CheckItemRegType(){
        String strItemKey = mapItemKey.get(holder.cmbItem.getSelectedItem().toString());
        if (hmItemRegisterType.get(strItemKey).equals("ItemID")){
            holder.etLotID.setEnabled(false);
            holder.etLotID.setText("*");
            holder.chkFIFOLot.setEnabled(false);
            holder.chkFIFOLot.setChecked(false);
        }else{
            holder.etLotID.setEnabled(true);
            holder.etLotID.setText("");
            holder.chkFIFOLot.setEnabled(true);
        }
    }
    //endregion

    //region Listener Method

    //頁簽監聽事件
    private TabHost.OnTabChangeListener lsTabHost_OnTabChange = new TabHost.OnTabChangeListener() {
        @Override
        public void onTabChanged(String tabID) {

        }
    };

    // 220707 Ikea 新增鏡頭掃描輸入
    private View.OnClickListener ibtnOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            IntentIntegrator integrator = new IntentIntegrator(GoodNonreceiptReceiveActivity.this);
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

    //廠商出貨日期
    private AdapterView.OnClickListener lsVendorShipDate = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            setVendorShipDate();
        }
    };

    //製造日期
    private AdapterView.OnClickListener lsMFDDate = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            setMFDDate();
        }
    };

    //有效日期
    private AdapterView.OnClickListener lsExpiryDate = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            setExpiryDate();
        }
    };

    //新增料號
    private AdapterView.OnClickListener lsAddLotID = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            addLotID();
        }
    };

    //更新
    private AdapterView.OnClickListener lsRefresh = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            initControl();
            initData();
            Refresh(true);
        }
    };

    //收料確認
    private AdapterView.OnClickListener lsConfirm = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ArrayList<String> lstStorage = new ArrayList<String>();
            for (DataRow dr : dtLotID.Rows){
                String temp = dr.getValue("STORAGE_ID").toString();
                if(!lstStorage.contains(temp)){
                    lstStorage.add(temp);
                }
            }
            CheckStorageInTempBin(lstStorage);

        }
    };

    //新增SN
    private AdapterView.OnItemClickListener lsListViewOnClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final DataRow chooseRow = dtLotID.Rows.get(position);
            if (chooseRow.getValue("REGISTER_TYPE").equals("PcsSN")) {

                String strGrrDetSNRerKey;
                String strQty = chooseRow.getValue("QTY").toString();

                if (chooseRow.getValue("GRR_DET_SN_REF_KEY") == null) {
                    Date date = new Date();
                    String pattern = "MMddHHmmss.S";
                    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                    strGrrDetSNRerKey = sdf.format(date);
                }else{
                    strGrrDetSNRerKey = chooseRow.getValue("GRR_DET_SN_REF_KEY").toString();
                }

                ShowSNDialog(strGrrDetSNRerKey, Integer.parseInt(strQty), position);
            }
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

    private AdapterView.OnItemSelectedListener lsItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            CheckItemRegType();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private class SimpleArrayAdapter<T> extends ArrayAdapter {
        public SimpleArrayAdapter(Context context, int resource, List<T> objects) {
            super(context, resource, objects);
        }

        //複寫這個方法，使返回的數據沒有最後一項
        @Override
        public int getCount() {
            // don't display last item. It is used as hint.
            int count = super.getCount();
            return count > 0 ? count - 1 : count;
        }
    }
    //endregion
}
//Error Code WAPG009
//WAPG009001    請選擇單據類型
//WAPG009002    請選擇廠商
//WAPG009003    請選擇倉庫
//WAPG009004    請選擇物料
//WAPG009005    請輸入數量
//WAPG009006    批號已存在
//WAPG009007    作業成功
//WAPG009008    是否刪除此序號?
//WAPG009009    未滿需求量!
//WAPG009010    物料[%s]註冊類別為[%s]，不需要輸入批號!
//WAPG009011    物料[%s]註冊類別為[%s]，需要輸入批號!
//WAPG009012    已滿足需求量
//WAPG009013    物料[%s]序號尚未收滿!
//WAPG009014    是否刪除此批號?
//WAPG009015    序號重複!
//WAPG009016    尚未新增任何料號!
//WAPG009017    供應商[%s]未設定供應商對應物料檢驗設定
//WAPG009018    尚未選擇倉庫[%s]物料[%s]對應儲位
//WAPG009019    請輸入製造日期
//WAPG009020    請輸入有效期限
//WAPG009021    未設定收料類型單據，無法產生系統的收料單據