package com.delta.android.WMS.Client;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.Activity.ScanActivity;
import com.delta.android.Core.Activity.ShowMessageEvent;
import com.delta.android.Core.DataTable.DataColumn;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.GenerateJsonNetString.MList;
import com.delta.android.Core.GenerateJsonNetString.MesClass;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionaryList;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Client.GridAdapter.WarehouseStorageDetAdapter;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.BWarehouseStorageParam;
import com.delta.android.WMS.Param.ParamObj.CheckCountObj;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class WarehouseStorageDetailActivity extends BaseFlowActivity {

    // 變數
    private final int ITEM_CLICK_REQUEST_CODE = 11;

    private ViewHolder holder = null;
    private DataTable dtMst; // 前一個頁面選中的 Mst
    private DataTable dtDet; // 前一個頁面選中的 Det
    private DataTable dtLotAll; // 各料號已暫存的批號資訊
    private DataTable dtStorageTempBin;
    private DataTable dtFilter;
    private String strSheetPolicyId;
    private boolean blnIsWV;

    private HashMap<String, ArrayList<String>> mapStorageBin;
    private HashMap<String, String> mapStorageBinResult;

    static class ViewHolder {
        ListView lvDetData;
        ImageButton ibtnItemIdQRScan;
        EditText etItemId;
        Button btnInStockFinish;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_warehouse_storage_det);

        this.initialSetUp();

        this.setListeners();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, getResources().getText(R.string.QRCODE_SCAN_CANCEL).toString(), Toast.LENGTH_SHORT).show();
            } else {
                holder.etItemId.setText(result.getContents().trim());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == ITEM_CLICK_REQUEST_CODE && resultCode == 1) {
                Bundle bundle = data.getExtras();
                DataTable dtLotData = (DataTable) bundle.getSerializable("dtLotData");
                dtLotAll = dtLotData;
            }
        }
    }

    // region Method

    // 初始設定
    private void initialSetUp() {

        strSheetPolicyId = getIntent().getStringExtra("sheetPolicyId");
        blnIsWV = getIntent().getBooleanExtra("blnIsWV", false);
        dtMst = (DataTable) getIntent().getSerializableExtra("dtMstResult");
        dtDet = (DataTable) getIntent().getSerializableExtra("dtDetResult");
        dtStorageTempBin = new DataTable();
        mapStorageBin = new HashMap<String, ArrayList<String>>();
        mapStorageBinResult = new HashMap<>();

        createLotDataTable();

        if (holder == null)
            holder = new ViewHolder();

        holder.etItemId = findViewById(R.id.etItemId);
        holder.ibtnItemIdQRScan = findViewById(R.id.ibtnItemIdQRScan);
        holder.lvDetData = findViewById(R.id.lvWvDetData);
        holder.btnInStockFinish = findViewById(R.id.btnInStockFinish);

        getDetData(dtDet);
    }

    // 監聽事件
    private void setListeners() {
        holder.ibtnItemIdQRScan.setOnClickListener(ibtnQRScanOnClick);
        holder.etItemId.setOnEditorActionListener(filterSheetDet);
        holder.lvDetData.setOnItemClickListener(getLotData);
        holder.btnInStockFinish.setOnClickListener(stockInFinish);
    }

    // 設置 ListView
    private void getDetData(DataTable dt) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        WarehouseStorageDetAdapter adapter = new WarehouseStorageDetAdapter(dt, blnIsWV, inflater);
        holder.lvDetData.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    // 建立空的DataTable存放各料號已暫存的批號資訊
    private void createLotDataTable() {
        dtLotAll = new DataTable();
        dtLotAll.addColumn(new DataColumn("SEQ"));
        dtLotAll.addColumn(new DataColumn("STORAGE_ID"));
        dtLotAll.addColumn(new DataColumn("ITEM_ID"));
        dtLotAll.addColumn(new DataColumn("LOT_ID"));
        dtLotAll.addColumn(new DataColumn("QTY"));
        dtLotAll.addColumn(new DataColumn("SCRAP_QTY"));
        dtLotAll.addColumn(new DataColumn("MFG_DATE"));
        dtLotAll.addColumn(new DataColumn("EXP_DATE"));
        dtLotAll.addColumn(new DataColumn("TEMP_BIN"));
    }
    // endregion


    // region 事件
    private View.OnClickListener ibtnQRScanOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            IntentIntegrator integrator = new IntentIntegrator(WarehouseStorageDetailActivity.this);
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

    private TextView.OnEditorActionListener filterSheetDet = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (dtDet == null || dtDet.Rows.size() == 0)
                return  false;
            dtFilter = new DataTable();
            String filterId = holder.etItemId.getText().toString().toUpperCase().trim();
            if (filterId == null || filterId.equals("")) {
                dtFilter = dtDet;
            } else {
                for (DataRow dr : dtDet.Rows) {
                    if (dr.getValue("ITEM_ID").toString().equals(filterId))
                        dtFilter.Rows.add(dr);
                }
            }
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            WarehouseStorageDetAdapter adapter = new WarehouseStorageDetAdapter(dtFilter, blnIsWV, inflater);
            holder.lvDetData.setAdapter(adapter);
            return false;
        }
    };

    private AdapterView.OnItemClickListener getLotData = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
            if (dtFilter == null)
                dtFilter = dtDet;
            final DataRow drSelectedRow = dtFilter.Rows.get(pos);

            Bundle selectedInfo = new Bundle();
            selectedInfo.putString("sheetPolicyId", strSheetPolicyId);
            selectedInfo.putBoolean("blnIsWV", blnIsWV);
            selectedInfo.putSerializable("dtDet", dtDet);
            selectedInfo.putSerializable("dtLotAll", dtLotAll);
            selectedInfo.putInt("lotPos", pos);
            selectedInfo.putSerializable("sheetId", drSelectedRow.getValue("MTL_SHEET_ID").toString());
            selectedInfo.putSerializable("itemId", drSelectedRow.getValue("ITEM_ID").toString());
            selectedInfo.putSerializable("qty", drSelectedRow.getValue("QTY").toString());
            selectedInfo.putSerializable("scrapQty", drSelectedRow.getValue("SCRAP_QTY").toString());

            Intent intent = new Intent(WarehouseStorageDetailActivity.this, WarehouseStorageStockInActivity.class);
            intent.putExtras(selectedInfo);
            startActivityForResult(intent, ITEM_CLICK_REQUEST_CODE);

//            gotoNextActivityForResult(WarehouseStorageStockInActivity.class, selectedInfo, new OnActivityResult() {
//                @Override
//                public void onResult(Bundle bundle) {
//                    DataTable dtLotData = (DataTable) bundle.getSerializable("dtLotData");
//                    dtLotAll = dtLotData;
//                }
//            });
        }
    };

    private View.OnClickListener stockInFinish = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            beforeStockInCheck();
        }
    };
    // endregion

    private void beforeStockInCheck() {

        final ArrayList<String> lstStorageId = new ArrayList<>();

        if (dtLotAll == null || dtLotAll.Rows.size() == 0) {
            ShowMessage(R.string.WAPG027028); // WAPG027028   請新增批號
            return;
        }

        // 檢查單據內每個項次是否都有新增明細資料
        // 只需要檢查物料卡控批號，但單據項次的批號為*號時，數量是否滿足單據Config的【實際數量的狀態】，只需檢查相等的狀態(因為more跟less已經在新增時檢查完畢)
        for (DataRow drDet : dtDet.Rows) {
            String seq = drDet.getValue("SEQ").toString();
            String storage = drDet.getValue("STORAGE_ID").toString();
            lstStorageId.add(storage);
            int counter = 0;
            DataTable dtLotForCheck = new DataTable();
            for (DataRow drLot : dtLotAll.Rows) {
                if (drLot.getValue("SEQ").toString().equals(seq)) {
                    dtLotForCheck.Rows.add(drLot);
                    counter++;
                }
            }
            if (counter == 0) {
                Object[] args = new Object[1];
                args[0] = seq;
                ShowMessage(R.string.WAPG027029, args); // WAPG027029   單據項次「%s」沒有明細!
                return;
            }
            if (!drDet.getValue("REGISTER_TYPE").toString().equals("ItemID") && drDet.getValue("LOT_ID").equals("*")) {
                String strActualQtyStatus = drDet.getValue("ACTUAL_QTY_STATUS").toString();
                BigDecimal decTotalAddQty = new BigDecimal("0.0");
                BigDecimal decTotalScrapAddQty = new BigDecimal("0.0");
                BigDecimal decQty = new BigDecimal(drDet.getValue("QTY").toString());
                BigDecimal decScrapQty = new BigDecimal(drDet.getValue("SCRAP_QTY").toString().equals("") ? "0.0" : drDet.getValue("SCRAP_QTY").toString());
                for (DataRow drLotForCheck : dtLotForCheck.Rows) {
                    decTotalAddQty = decTotalAddQty.add(new BigDecimal(drLotForCheck.get("QTY").toString()).setScale(1));
                    decTotalScrapAddQty = decTotalScrapAddQty.add(new BigDecimal(drLotForCheck.getValue("SCRAP_QTY").toString()).setScale(1));
                }
                //依單據Config的【實際數量的狀態】來檢查數量
                if (strActualQtyStatus.equals("Equal")) {
                    if (decTotalAddQty.compareTo(decQty) != 0) {
                        Object[] args = new Object[3];
                        args[0] = strSheetPolicyId;
                        args[1] = strActualQtyStatus;
                        args[2] = seq;
                        ShowMessage(R.string.WAPG027030, args); // WAPG027030   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，項次「%s」批號數量需相等!
                        return;
                    }
                    if (decTotalScrapAddQty.compareTo(decScrapQty) != 0) {
                        Object[] args = new Object[3];
                        args[0] = strSheetPolicyId;
                        args[1] = strActualQtyStatus;
                        args[2] = seq;
                        ShowMessage(R.string.WAPG027031, args); // WAPG027031   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，項次「%s」批號報廢數量需相等!
                        return;
                    }
                }
            }
        }

        BModuleObject bmObj2 = new BModuleObject();
        bmObj2.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj2.setModuleID("BIFetchBin");
        bmObj2.setRequestID("BIFetchBin");

        bmObj2.params = new Vector<ParameterInfo>();
        //裝Condition的容器
        final HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
        List<Condition> lstCondition = new ArrayList<Condition>();
        for (int i = 0; i < lstStorageId.size(); i++)
        {
            Condition cond = new Condition();
            cond.setAliasTable("S");
            cond.setColumnName("STORAGE_ID");
            cond.setValue(lstStorageId.get(i));
            cond.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
            lstCondition.add(cond);
        }
        mapCondition.put("STORAGE_ID", lstCondition);

        //Serialize序列化
        VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Libray.Param");
        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vKey, vVal);
        String strCond = msdl.generateFinalCode(mapCondition);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIWMSFetchInfoParam.Filter);
        param2.setParameterValue("AND B.BIN_TYPE IN ('IT', 'IS')");
        bmObj2.params.add(param2);

        //Call BIModule
        CallBIModule(bmObj2, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(CheckBModuleReturnInfo(bModuleReturn)){
                    dtStorageTempBin = bModuleReturn.getReturnJsonTables().get("BIFetchBin").get("BIN");

                    if(dtStorageTempBin.Rows.size() <= 0){
                        ShowMessage(R.string.WAPG027032); // WAPG027032   查無對應的入庫儲位
                        return;
                    }

                    mapStorageBin.clear();
                    //有暫存區，就直接到暫存區
                    for (DataRow dr : dtStorageTempBin.Rows)
                    {
                        if(!mapStorageBin.containsKey(dr.getValue("STORAGE_ID").toString()))
                        {
                            ArrayList<String> lstBin = new ArrayList<>();

                            if(dr.getValue("BIN_TYPE").toString().equals("IT"))
                                lstBin.add(dr.getValue("BIN_ID").toString());

                            mapStorageBin.put(dr.getValue("STORAGE_ID").toString(), lstBin);
                        }
                        else
                        {
                            if(dr.getValue("BIN_TYPE").toString().equals("IT"))
                                mapStorageBin.get(dr.getValue("STORAGE_ID").toString()).add(dr.getValue("BIN_ID").toString());
                        }
                    }

                    for (DataRow dr : dtStorageTempBin.Rows)
                    {
                        if (mapStorageBin.get(dr.getValue("STORAGE_ID").toString()).size() <= 0)
                        {
                            if(dr.getValue("BIN_TYPE").toString().equals("IS"))
                                mapStorageBin.get(dr.getValue("STORAGE_ID").toString()).add(dr.getValue("BIN_ID").toString());
                        }
                    }
                }
            }
        });
        showConfirmDialog();
    }

    private void showConfirmDialog() {

        LayoutInflater inflater = LayoutInflater.from(WarehouseStorageDetailActivity.this);
        final View viewConfirm = inflater.inflate(R.layout.activity_wms_warehouse_storage_stock_in_confirm, null);
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(WarehouseStorageDetailActivity.this);
        builder.setView(viewConfirm);

        final android.app.AlertDialog dialogConfirm = builder.create();
        dialogConfirm.setCancelable(false);
        dialogConfirm.show();

        final ListView lvStorageItem = dialogConfirm.findViewById(R.id.lvStockInLot);
        Button btnSelectConfirm = dialogConfirm.findViewById(R.id.btnBinConfirm);

        final DataTable dtStorageItem = new DataTable();
        dtStorageItem.addColumn(new DataColumn("ITEM_ID"));
        dtStorageItem.addColumn(new DataColumn("ITEM_NAME"));
        dtStorageItem.addColumn(new DataColumn("STORAGE_ID"));
        dtStorageItem.addColumn(new DataColumn("BIN_ID"));

        for (DataRow drdet : dtDet.Rows) {
            DataRow drStorageItem = dtStorageItem.newRow();
            drStorageItem.setValue("ITEM_ID", drdet.getValue("ITEM_ID").toString());
            drStorageItem.setValue("ITEM_NAME", drdet.getValue("ITEM_NAME").toString());
            drStorageItem.setValue("STORAGE_ID", drdet.getValue("STORAGE_ID").toString());
            drStorageItem.setValue("BIN_ID", drdet.getValue("BIN_ID").toString());
            dtStorageItem.Rows.add(drStorageItem);
        }

        LayoutInflater inflaterSelect = LayoutInflater.from(getApplicationContext());
        WarehouseStorageSelectGridAdapter adapterSelect = new WarehouseStorageSelectGridAdapter(dtStorageItem, inflaterSelect);
        lvStorageItem.setAdapter(adapterSelect);
        adapterSelect.notifyDataSetChanged();

        lvStorageItem.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                LayoutInflater inflaterBin = LayoutInflater.from(WarehouseStorageDetailActivity.this);
                final View viewBin = inflaterBin.inflate(R.layout.activity_wms_warehouse_storage_confirm_select_bin, null);
                final android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.Builder(WarehouseStorageDetailActivity.this);
                builder1.setView(viewBin);

                final android.app.AlertDialog dialogBin = builder1.create();
                dialogBin.setCancelable(false);
                dialogBin.show();

                final Spinner cmbBin = dialogBin.findViewById(R.id.cmbBinID);
                final Button btnBinConfirm = dialogBin.findViewById(R.id.btnBinConfirm);

                final String strStorage = dtStorageItem.getValue(position, "STORAGE_ID").toString();
                final String strItem = dtStorageItem.getValue(position, "ITEM_ID").toString();
                ArrayAdapter<String> adapterBin = new ArrayAdapter<>(WarehouseStorageDetailActivity.this, android.R.layout.simple_spinner_dropdown_item, mapStorageBin.get(strStorage));
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
                        WarehouseStorageSelectGridAdapter adapterSelect = new WarehouseStorageSelectGridAdapter(dtStorageItem, inflaterSelect);
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
                        ShowMessage(R.string.WAPG027033, args); // WAPG027033   尚未選擇倉庫[%s]物料[%s]對應儲位
                        return;
                    }
                }
                dialogConfirm.dismiss();

                //帶入TEMP_BIN
                if(!dtLotAll.getColumns().contains("TEMP_BIN")){
                    DataColumn dcTempBin = new DataColumn("TEMP_BIN");
                    dtLotAll.addColumn(dcTempBin);
                }

                for(DataRow drLot : dtLotAll.Rows){
                    String storage = drLot.getValue("STORAGE_ID").toString();
                    String itemid = drLot.getValue("ITEM_ID").toString();
                    mapStorageBinResult.clear();
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

                ExecuteProcess("WarehouseStorage");
            }
        });

    }

    private void ExecuteProcess(String trxType) {

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BWarehouseStorage");
        bmObj.setModuleID("");
        bmObj.setRequestID("BWarehouseStorage");
        bmObj.params = new Vector<ParameterInfo>();

        //Input param
        ParameterInfo paramTrxType = new ParameterInfo();
        paramTrxType.setParameterID(BWarehouseStorageParam.TrxType);
        paramTrxType.setParameterValue(trxType);
        bmObj.params.add(paramTrxType);

        ParameterInfo paramPolicyId = new ParameterInfo();
        paramPolicyId.setParameterID(BWarehouseStorageParam.SheetTypePolicyId);
        paramPolicyId.setParameterValue(strSheetPolicyId);
        bmObj.params.add(paramPolicyId);

        BWarehouseStorageParam.WarehouseStorageMasterObj sheet = new BWarehouseStorageParam()
                .new WarehouseStorageMasterObj().getWsSheet(dtMst.Rows.get(0), dtLotAll);

        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.INV.Parameter.WarehouseStorageMasterObj", "bmWMS.INV.Param");
        MesClass mesClassEnum = new MesClass(vListEnum);
        String strWvMstObj = mesClassEnum.generateFinalCode(sheet);

        ParameterInfo paramWsObj = new ParameterInfo();
        paramWsObj.setParameterID(BWarehouseStorageParam.WsObj);
        paramWsObj.setNetParameterValue(strWvMstObj);
        bmObj.params.add(paramWsObj);

        // region 儲存盤點狀態檢查物件
        // 20220805 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中
        List<CheckCountObj> lstChkCountObj = new ArrayList<>();
        for (DataRow dr : dtLotAll.Rows) {
            CheckCountObj chkCountObj = new CheckCountObj();
            chkCountObj.setStorageId(dr.getValue("STORAGE_ID").toString());
            chkCountObj.setItemId(dr.getValue("ITEM_ID").toString());
            chkCountObj.setBinId(dr.getValue("TEMP_BIN").toString());
            lstChkCountObj.add(chkCountObj);
        }
        // endregion

        VirtualClass vListEnum2 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.CheckCountObj", "bmWMS.Library.Param");
        MList mListEnum = new MList(vListEnum2);
        String strCheckCountObj = mListEnum.generateFinalCode(lstChkCountObj);

        ParameterInfo paramExecuteChkStock = new ParameterInfo();
        paramExecuteChkStock.setParameterID(BWarehouseStorageParam.ExecuteCheckStock); // 20220805 Add by Ikea 是否執行盤點檢查
        paramExecuteChkStock.setNetParameterValue2("true");
        bmObj.params.add(paramExecuteChkStock);

        ParameterInfo paramChkCountObj = new ParameterInfo(); // 20220805 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中
        paramChkCountObj.setParameterID(BWarehouseStorageParam.CheckCountObj);
        paramChkCountObj.setNetParameterValue(strCheckCountObj);
        bmObj.params.add(paramChkCountObj);

        CallBModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    // WAPG027034   作業成功
                    ShowMessage(R.string.WAPG027034, new ShowMessageEvent() {
                        @Override
                        public void onDismiss() {
                            gotoPreviousActivity(WarehouseStorageActivity.class, true);
                        }
                    });
                }
            }
        });
    }
}

// ERROR CODE
// WAPG027001   請輸入至少一個條件
// WAPG027002   請選擇開單日期(起)和開單日期(迄)
// WAPG027003   開單日期(起)不能大於開單日期(迄)
// WAPG027004   請選擇單據類型
// WAPG027005   查詢無資料
// WAPG027006   物料[%s]管控批號，需要輸入批號
// WAPG027007   請輸入數量
// WAPG027008   請輸入報廢數量
// WAPG027009   數量、報廢數量不可同時為0！
// WAPG027010   請輸入製造日期
// WAPG027011   請輸入有效期限
// WAPG027012   製造日期不可大於有效期限
// WAPG027013   單據類型「%s」未設定「單據Config設定」內的【實際數量的狀態】，請先設定!
// WAPG027014   輸入批號「%s」與單據項次「%s」指定的批號「%s」不一致!
// WAPG027015   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，批號數量需相等!
// WAPG027016   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，批號報廢數量需相等!
// WAPG027017   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號數量「%s」不可小於單據數量「%s」!
// WAPG027018   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號報廢數量「%s」不可小於單據報廢數量「%s」!
// WAPG027019   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號數量「%s」不可大於單據數量「%s」!
// WAPG027020   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號報廢數量「%s」不可大於單據報廢數量「%s」!
// WAPG027021   批號已存在!
// WAPG027022   物料[%s]未設定WMS物料設定檔
// WAPG027023   物料[%s]不管控批號，批號需為空白
// WAPG027024   批號[%s]不存在於MES
// WAPG027025   批號[%s]非最外箱,需輸入最外層箱號,上層ID[%s]
// WAPG027026   批號[%s]數量[%s]需等於[%s]
// WAPG027027   是否刪除此批號?
// WAPG027028   請新增批號
// WAPG027029   單據項次「%s」沒有明細!
// WAPG027030   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，項次「%s」批號數量需相等!
// WAPG027031   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，項次「%s」批號報廢數量需相等!
// WAPG027032   查無對應的入庫儲位
// WAPG027033   尚未選擇倉庫[%s]物料[%s]對應儲位
// WAPG027034   作業成功