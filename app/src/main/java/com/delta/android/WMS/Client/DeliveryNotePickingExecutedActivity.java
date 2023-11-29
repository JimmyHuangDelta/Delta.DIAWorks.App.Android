package com.delta.android.WMS.Client;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.Activity.ScanActivity;
import com.delta.android.Core.Activity.ShowMessageEvent;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.GenerateJsonNetString.MList;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionary;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionaryList;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Client.GridAdapter.DeliveryNoteDetPickGridAdapter;
import com.delta.android.WMS.Client.GridAdapter.SheetDetPickGridAdapter;
import com.delta.android.WMS.Param.BDeliveryNotePickingParam;
import com.delta.android.WMS.Param.BGoodReservationParam;
import com.delta.android.WMS.Param.BIFetchPickStrategyParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.BStockOutBaseParam;
import com.delta.android.WMS.Param.ParamObj.CheckCountObj;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.delta.android.WMS.Param.ParamObj.PickDetObj;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class DeliveryNotePickingExecutedActivity extends BaseFlowActivity {

    DataTable pickedData;
    DataTable sheetDetData;
    DataTable sheetMstData;
    DataTable alreadyPickedData;
    double pickedCount = 0;  // 已揀數量
    double totalPickedCount; // 全部應揀數量
    ViewHolder holder;
    String strPickStrategy;  // 挑貨策略
    String strActualQtyStatus; //單據實際數量的狀態
    DataTable dtConfigCond;
    DataTable dtConfigSort;

    static class ViewHolder {
        EditText RegisterId;
        EditText BinId;
        ImageButton IbtnBinIdQRScan;
        TextView ItemId;
        TextView SheetId;
        TextView SheetDetPickQty;
        TextView Seq;
        ListView RegisterData;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_delivery_note_picking_executed);

        this.initialData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, getResources().getText(R.string.QRCODE_SCAN_CANCEL).toString(), Toast.LENGTH_SHORT).show();
            } else {
                holder.BinId.setText(result.getContents().trim());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed()
    {
        //如果要回傳則需要寫此方法
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    private void initialData() {
        sheetDetData = (DataTable) getIntent().getSerializableExtra("DetTable");
        sheetMstData = (DataTable) getIntent().getSerializableExtra("MstTable");
        alreadyPickedData = (DataTable) getIntent().getSerializableExtra("DetPicked");

        DataRow sheetDetRow = sheetDetData.Rows.get(0);
        holder = new ViewHolder();

        holder.IbtnBinIdQRScan = findViewById(R.id.ibtnBinIdQRScan);
        holder.BinId = findViewById(R.id.etSheetDetPickBinId);
        holder.RegisterId = findViewById(R.id.etSheetDetPickRegisterId);
        holder.ItemId = findViewById(R.id.tvSheetDetPickItemId);
        holder.SheetId = findViewById(R.id.tvSheetDetPickSheetId);
        holder.SheetDetPickQty = findViewById(R.id.tvSheetDetPickQty);
        holder.Seq = findViewById(R.id.tvSheetDetPickSeq);
        holder.RegisterData = findViewById(R.id.lvSheetDetPickRegisterData);

        if (sheetDetRow == null) return;

        holder.ItemId.setText(sheetDetRow.getValue("ITEM_ID").toString());
        holder.SheetId.setText(sheetDetRow.getValue("DN_ID").toString());
        holder.Seq.setText(sheetDetRow.getValue("SEQ").toString());
        holder.RegisterId.setText(sheetDetRow.getValue("SEQ").toString());
        holder.RegisterId.setEnabled(false);

        // 檢查是否已經揀貨完成
        this.checkPicked();
        holder.SheetDetPickQty.setText(String.format("%s/%s", String.valueOf(pickedCount), String.valueOf(totalPickedCount)));

        // 先看有無挑貨策略 有責執行 getRegisterData();
        this.getPickStrategy();
        // 看有無設定WMS物料設定檔
        if(sheetDetData.Rows.get(0).getValue("REGISTER_TYPE") == null || sheetDetData.Rows.get(0).getValue("REGISTER_TYPE").toString().equals(""))
        {
            ShowMessage(R.string.WAPG006010,sheetDetData.Rows.get(0).getValue("ITEM_ID"));
        }

        holder.RegisterId.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    filterRegister();
                }
                return false;
            }
        });

        holder.BinId.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    filterBin();
                }
                return false;
            }
        });

        holder.IbtnBinIdQRScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(DeliveryNotePickingExecutedActivity.this);
                // 設置要掃描的條碼類型，ONE_D_CODE_TYPES：一維碼，QR_CODE_TYPES-二維碼
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                integrator.setPrompt("BarCode Scan"); //底部的提示文字
                integrator.setCameraId(0); //前置(1)或者後置(0)攝像頭
                integrator.setBeepEnabled(false); //掃描成功的「嗶嗶」聲
                integrator.setBarcodeImageEnabled(false); //是否保留掃碼成功時候的截圖
                integrator.setCaptureActivity(ScanActivity.class);
                integrator.initiateScan();
            }
        });
    }

    //顯示 目前揀多少貨物/總共要揀多少貨物
    private void checkPicked() {
        totalPickedCount = Double.parseDouble(sheetDetData.Rows.get(0).getValue("QTY").toString());
        if (alreadyPickedData == null || alreadyPickedData.Rows.size() == 0) return;
        for (DataRow dr : alreadyPickedData.Rows) {
            //將非預約揀貨的資料篩選出來
            if (dr.getValue("IS_PICKED").toString().equals(("Y")))
            {
                pickedCount += Double.parseDouble(dr.getValue("QTY").toString());
            }
        }
        if (pickedCount >= totalPickedCount) {
            ShowMessage("此料號已經揀料", new ShowMessageEvent() {
                @Override
                public void onDismiss() {
                    gotoPreviousActivity(DeliveryNotePickingDetailActivity.class);
                }
            });
        }
    }

    private void filterRegister() {
        if (pickedData == null || pickedData.Rows.size() == 0) return;
        String regId = holder.RegisterId.getText().toString().toUpperCase().trim(); //20200729 archie 轉大寫
        DataTable filterTable = new DataTable();
        if (!regId.equals("")) {
            for (DataRow dr : pickedData.Rows) {
                if (dr.getValue("REGISTER_ID").toString().equals(regId)) {
                    filterTable.Rows.add(dr);
                }
            }
        }

        if (filterTable.Rows.size() == 0 && regId.equals(""))
            filterTable = pickedData;

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        DeliveryNoteDetPickGridAdapter adapter = new DeliveryNoteDetPickGridAdapter(filterTable, inflater);
        holder.RegisterData.setAdapter(adapter);
        holder.RegisterData.setOnItemClickListener(onClickListView);
    }

    private void filterBin() {
        if (pickedData == null || pickedData.Rows.size() == 0) return;
        String bnId = holder.BinId.getText().toString().toUpperCase().trim(); //20200729 archie 轉大寫
        DataTable filterTable = new DataTable();
        if (!bnId.equals("")) {
            if (filterTable.Rows.size() == 0) {
                for (DataRow dr : pickedData.Rows) {
                    if (dr.getValue("BIN_ID").toString().equals(bnId)) {
                        filterTable.Rows.add(dr);
                    }
                }
            } else {
                DataTable dt = new DataTable();
                for (DataRow dr : filterTable.Rows) {
                    if (dr.getValue("BIN_ID").toString().equals(bnId)) {
                        dt.Rows.add(dr);
                    }
                }
                filterTable = dt;
            }
        }
        if (filterTable.Rows.size() == 0 && bnId.equals(""))
            filterTable = pickedData;

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        DeliveryNoteDetPickGridAdapter adapter = new DeliveryNoteDetPickGridAdapter(filterTable, inflater);
        holder.RegisterData.setAdapter(adapter);
        holder.RegisterData.setOnItemClickListener(onClickListView);
    }

    private void getPickStrategy()
    {
        // region 揀貨策略 取ACTUAL_QTY_STATUS
        BModuleObject biObj = new BModuleObject();
        biObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        biObj.setModuleID("BIFetchWmsSheetConfig");
        biObj.setRequestID("FetchPickingStrategy");
        biObj.params = new Vector<>();

        List<Condition> conditions = new ArrayList<>();
        HashMap<String, List<?>> dicCondition = new HashMap<>();
        String shtTypeId = sheetMstData.Rows.get(0).getValue("DN_TYPE_ID").toString();

        Condition condition = new Condition();
        condition.setAliasTable("TYP");
        condition.setColumnName("SHEET_TYPE_ID");
        condition.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
        condition.setValue(shtTypeId);
        conditions.add(condition);
        dicCondition.put("SHEET_TYPE_ID", conditions);

        VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vKey, vVal);
        String serializedString = msdl.generateFinalCode(dicCondition);

        ParameterInfo param = new ParameterInfo();
        param.setParameterID(BIWMSFetchInfoParam.Condition);
        param.setNetParameterValue(serializedString);
        biObj.params.add(param);
        //endregion

        //region 單據類型 Config 取 Cond / Sort
        BModuleObject biShtCfgSortAndCond = new BModuleObject();
        biShtCfgSortAndCond.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIDeliveryNotePicking");
        biShtCfgSortAndCond.setModuleID("BIFetchConfigCondAndSort");
        biShtCfgSortAndCond.setRequestID("FetchConfigCondAndSort");
        biShtCfgSortAndCond.params = new Vector<ParameterInfo>();

        String strDnId = sheetMstData.Rows.get(0).getValue("DN_ID").toString();
        ParameterInfo paramId = new ParameterInfo();
        paramId.setParameterID(BDeliveryNotePickingParam.SheetId);
        paramId.setParameterValue(strDnId);
        biShtCfgSortAndCond.params.add(paramId);
        // endregion

        List<BModuleObject> lstBmObj = new ArrayList<>();
        lstBmObj.add(biObj);
        lstBmObj.add(biShtCfgSortAndCond);

        CallBIModule(lstBmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)){
                    DataTable dtPickingStrategy = bModuleReturn.getReturnJsonTables().get("FetchPickingStrategy").get("SBRM_WMS_SHEET_CONFIG");
                    dtConfigCond = bModuleReturn.getReturnJsonTables().get("FetchConfigCondAndSort").get("SheetConfigCond");
                    dtConfigSort = bModuleReturn.getReturnJsonTables().get("FetchConfigCondAndSort").get("SheetConfigSort");

                    /*
                    if (dtPickingStrategy == null || dtPickingStrategy.Rows.size() == 0){
                        //WAPG006015    無設定挑貨策略
                        ShowMessage(R.string.WAPG006015);
                        return;
                    }
                    strPickStrategy = dtPickingStrategy.Rows.get(0).getValue("OUT_PICKING_STRATEGY").toString();
                    if (strPickStrategy.equals(""))
                    {
                        //WAPG006015    無設定挑貨策略
                        ShowMessage(R.string.WAPG006015);
                        return;
                    }
                     */

                    if (dtPickingStrategy != null && dtPickingStrategy.Rows.size() > 0){
                        strActualQtyStatus = dtPickingStrategy.Rows.get(0).getValue("ACTUAL_QTY_STATUS").toString();
                        if (strActualQtyStatus == null || strActualQtyStatus.length() <= 0) {
                            //WAPG006016    無設定實際數量的狀態
                            ShowMessage(R.string.WAPG006016);
                            return;
                        }
                    }

                    getRegisterData();
                }
            }
        });
    }
    private void getRegisterData() {

        // region -- 原揀貨策略(註解) --
        /*
        BModuleObject biObj = new BModuleObject();
        biObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIDeliveryNotePicking");
        switch (strPickStrategy)
        {
            case "FIFO"://先進先出
                biObj.setModuleID("BIFetchItemByFIFOFirst");
                break;

            case "FEFO"://快過期的先出
                biObj.setModuleID("BIFetchItemByFIFODate");
                break;

            case "LOT"://指定批號
                biObj.setModuleID("BIFetchItemByFIFLot");
                break;

            default:
                //無指定挑貨策略
                ShowMessage("無設定挑貨策略");//WCPG009007    無設定挑貨策略
                return;
        }
         */
        // endregion

        BModuleObject biObj = new BModuleObject();
        biObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIDeliveryNotePicking");
        biObj.setModuleID("BIFetchItemByCondition");
        biObj.setRequestID("GetCanPickItem");
        biObj.params = new Vector<>();

        DataTable dtCanpick = new DataTable();

        DataRow drNew = dtCanpick.newRow();
        dtCanpick.Rows.add(drNew);
        dtCanpick.setValue(0, "ITEM_ID", sheetDetData.Rows.get(0).getValue("ITEM_ID").toString());
        dtCanpick.setValue(0, "ITEM_NAME", sheetDetData.Rows.get(0).getValue("ITEM_NAME").toString());
        dtCanpick.setValue(0, "UOM", sheetDetData.Rows.get(0).getValue("UOM").toString());
        dtCanpick.setValue(0, "QTY", sheetDetData.Rows.get(0).getValue("QTY").toString());

        HashMap<String, BigDecimal> mm = new HashMap<String, BigDecimal>();
        mm.put(dtCanpick.Rows.get(0).getValue("ITEM_ID").toString(), new BigDecimal(dtCanpick.Rows.get(0).getValue("QTY").toString()));

        VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create(VirtualClass.VirtualClassType.Decimal);
        MesSerializableDictionary msd = new MesSerializableDictionary(vkey, vVal);
        String serializedObj = msd.generateFinalCode(mm);

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BDeliveryNotePickingParam.ItemIDs);
        param1.setNetParameterValue(serializedObj);
        biObj.params.add(param1);

        String strConfigCond = null;
        if (dtConfigCond != null && dtConfigCond.Rows.size() > 0) {
            for (DataRow drCond : dtConfigCond.Rows) {

                String strCond = null;
                String reqVal = sheetDetData.Rows.get(0).getValue(drCond.getValue("REQ_FIELD").toString()).toString();

                if (!reqVal.equals("*")) {

                    strCond = String.format("REG.%s %s '%s'", drCond.getValue("REG_FIELD").toString(), drCond.get("COND_OPERATOR").toString(), reqVal); // e.g. SR.LOT_CODE = 'LXXX'

                    if (strConfigCond == null || strConfigCond.length() <= 0)
                        strConfigCond = String.format("AND %s", strCond); // e.g. AND SR.LOT_CODE = 'LXXX'
                    else
                        strConfigCond = String.format("%s AND %s", strConfigCond, strCond); // e.g. AND SR.LOT_CODE = 'LXXX' AND R.XXX = 'OOOO'
                }
            }
        }

        List<String> lstConfigSort = new ArrayList<>();
        String strConfigSort = null;
        if (dtConfigSort != null && dtConfigSort.Rows.size() > 0) {
            for (DataRow drSort : dtConfigSort.Rows) {
                lstConfigSort.add(String.format("%s %s", drSort.getValue("REG_FIELD").toString(), drSort.get("SORT_METHOD").toString())); // e.g. MFG_DATE ASC
            }
        }
        if (lstConfigSort.size() > 0) {
            strConfigSort = TextUtils.join(", ", lstConfigSort);
        }

        if (strConfigCond != null && strConfigCond.length() > 0) {
            ParameterInfo paramCond = new ParameterInfo();
            paramCond.setParameterID(BDeliveryNotePickingParam.Filter);
            paramCond.setParameterValue(strConfigCond);
            biObj.params.add(paramCond);
        }

        if (strConfigSort != null && strConfigSort.length() > 0) {
            ParameterInfo paramSort = new ParameterInfo();
            paramSort.setParameterID(BDeliveryNotePickingParam.ConfigSort);
            paramSort.setParameterValue(strConfigSort);
            biObj.params.add(paramSort);
        }

        CallBIModule(biObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    //DataTable dt = bModuleReturn.getReturnJsonTables().get("GetCanPickItem").get("*");
                    DataTable dt = bModuleReturn.getReturnJsonTables().get("GetCanPickItem").get("Register");
                    if (dt == null || dt.Rows.size() == 0) {
                        ShowMessage("目前沒有任何的批號可以揀", new ShowMessageEvent() {
                            @Override
                            public void onDismiss() {
                            }
                        });
                    }
                    pickedData = new DataTable();
                    for (DataRow dr : dt.Rows) {
                        if (dr.getValue("BIN_ID").equals("*")) continue;
                        if (Double.parseDouble(dr.getValue("QTY").toString()) == 0){
                            dr.setValue("SELECTED", "True");
                        }
                        pickedData.Rows.add(dr);
                    }

                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    DeliveryNoteDetPickGridAdapter adapter = new DeliveryNoteDetPickGridAdapter(pickedData, inflater);
                    holder.RegisterData.setAdapter(adapter);
                    holder.RegisterData.setOnItemClickListener(onClickListView);
                }
            }
        });
    }

    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            //CheckBox cb = view.findViewById(R.id.cbPickedGridSelectedRegister);
            final TextView tvQty = view.findViewById(R.id.tvPickedGridQty);
            final TextView tvRsvQty = view.findViewById(R.id.tvPickedGridRsvQty);

            AlertDialog.Builder pickedQtyDialog = new AlertDialog.Builder(DeliveryNotePickingExecutedActivity.this);
            View dialogView = LayoutInflater.from(DeliveryNotePickingExecutedActivity.this).inflate(R.layout.style_wms_dialog_picked_qty, null);
            pickedQtyDialog.setTitle("");
            pickedQtyDialog.setView(dialogView);
            final EditText etQty = dialogView.findViewById(R.id.etPickedQty);
            final Spinner cmbBin = dialogView.findViewById(R.id.cmbTempBin);

            final String qty = tvQty.getText().toString();
            String rsvQty = tvRsvQty.getText().toString();
            rsvQty = (rsvQty != null && rsvQty.length() > 0) ? rsvQty : "0";


            // 依照REGISTER_TYPE 卡數量
            switch (sheetDetData.Rows.get(0).getValue("REGISTER_TYPE").toString())
            {
                case "ItemID":
                case "LotNo":

                    etQty.setEnabled(true);

                    // 20200807 archie 如果有預約帶出預約數量,無預約帶出庫存數量
                    if (Double.parseDouble(rsvQty) == 0.0)
                    {
                        if (Double.parseDouble(qty) > totalPickedCount-pickedCount)
                        {
                            if (strActualQtyStatus.equals("More"))
                            {
                                etQty.setText(qty);
                            }
                            else
                            {
                                etQty.setText(String.valueOf(totalPickedCount-pickedCount));
                            }
                        }
                        else
                        {
                            etQty.setText(qty);
                        }
                    }
                    else
                    {
                        if ((Double.parseDouble(rsvQty)+pickedCount) > totalPickedCount)
                        {
                            if (strActualQtyStatus.equals("More"))
                            {
                                etQty.setText(rsvQty);
                            }
                            else
                            {
                                etQty.setText(String.valueOf(totalPickedCount-pickedCount));
                            }
                        }
                        else
                        {
                            etQty.setText(rsvQty);
                        }
                    }


                    break;
                case "PcsSN":
                case "MinimizePackSN":
                case "BOX":
                    etQty.setText(sheetDetData.Rows.get(0).getValue("QTY").toString());
                    etQty.setEnabled(false);
            }

            BModuleObject bimObj = new BModuleObject();
            bimObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
            bimObj.setModuleID("BIFetchBin");
            bimObj.setRequestID("BIFetchBin");
            bimObj.params = new Vector<ParameterInfo>();

            ParameterInfo param1 = new ParameterInfo();
            param1.setParameterID(BIWMSFetchInfoParam.Filter);
            param1.setParameterValue(String.format(" AND STORAGE_ID ='%s' AND B.BIN_TYPE IN ('OT', 'OS')", pickedData.Rows.get(position).getValue("STORAGE_ID").toString()));
            bimObj.params.add(param1);

            CallBIModule(bimObj, new WebAPIClientEvent(){
                @Override
                public void onPostBack(BModuleReturn bModuleReturn){
                    if(CheckBModuleReturnInfo(bModuleReturn)){
                        DataTable dtBin = bModuleReturn.getReturnJsonTables().get("BIFetchBin").get("BIN");

                        ArrayList<String> alBin = new ArrayList<String>();
                        for(int i = 0; i < dtBin.Rows.size(); i++)
                        {
                            String binId = dtBin.Rows.get(i).getValue("BIN_ID").toString();
                            if(!alBin.contains(binId))
                                alBin.add(binId);
                        }
                        ArrayAdapter<String> adapterBin = new ArrayAdapter<>(DeliveryNotePickingExecutedActivity.this, android.R.layout.simple_spinner_dropdown_item, alBin);
                        cmbBin.setAdapter(adapterBin);
                    }
                }
            });

//            pickedQtyDialog.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    String binId = pickedData.Rows.get(position).getValue("BIN_ID").toString();
//                    String lotId = pickedData.Rows.get(position).getValue("REGISTER_ID").toString();
//                    String qty = pickedData.Rows.get(position).getValue("QTY").toString();
//                    String storageId = pickedData.Rows.get(position).getValue("STORAGE_ID").toString();
//                    String uom = sheetDetData.Rows.get(0).getValue("UOM").toString();
//                    ExecutePicked(binId,lotId,qty,storageId,uom,position); // 執行BModule
//
//                }
//            });
//            pickedQtyDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    isConfirm = false;
//                }
//            });
            final AlertDialog dialog = pickedQtyDialog.create();
            dialog.show();

            Button bConfirm = dialogView.findViewById(R.id.btnConfirm);
            bConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    switch (strActualQtyStatus)
                    {
                        case "More":
                            if ((Double.parseDouble(etQty.getText().toString()) + pickedCount < totalPickedCount))
                            {
                                ShowMessage(R.string.WAPG006013,
                                        holder.SheetId.getText().toString(),
                                        holder.Seq.getText().toString(),
                                        etQty.getText().toString(),
                                        totalPickedCount-pickedCount);
                                return;
                            }

                            break;

                        case "Less":
                        case "Equal":
                            if ((Double.parseDouble(etQty.getText().toString()) + pickedCount > totalPickedCount))
                            {
                                ShowMessage(R.string.WAPG006012,
                                        holder.SheetId.getText().toString(),
                                        holder.Seq.getText().toString(),
                                        etQty.getText().toString(),
                                        totalPickedCount-pickedCount);
                                return;
                            }

                            break;
                    }

                    String binId = pickedData.Rows.get(position).getValue("BIN_ID").toString();
                    String lotId = pickedData.Rows.get(position).getValue("REGISTER_ID").toString();
                    String qty = pickedData.Rows.get(position).getValue("QTY").toString();
                    String storageId = pickedData.Rows.get(position).getValue("STORAGE_ID").toString();
                    String uom = sheetDetData.Rows.get(0).getValue("UOM").toString();
                    String enterQty= etQty.getText().toString();
                    String tempBin = cmbBin.getSelectedItem().toString();
                    if (Double.parseDouble( enterQty )  <= 0)
                    {
                        //WAPG006005    揀貨數量需大於零
                        ShowMessage(R.string.WAPG006005);
                        return;
                    }
                    /*if (Double.parseDouble( enterQty )  > Double.parseDouble( qty ))
                    {
                        //WAPG006006    揀貨數量不能超過庫存數量
                        ShowMessage(R.string.WAPG006006);
                        return;
                    }*/

                    HashMap<String, String> mapBin = new HashMap<>();
                    mapBin.put(storageId, tempBin);

                    ExecutePicked(binId, lotId, enterQty, storageId, uom, position, mapBin); // 執行BModule
                    if (Double.compare(Double.parseDouble(qty) ,Double.parseDouble(pickedData.Rows.get(position).getValue("QTY").toString())) == 0){
                        pickedData.Rows.get(position).setValue("SELECTED", "True");
                    }
                    dialog.dismiss();
                }
            });

            Button bCancel = dialogView.findViewById(R.id.btnCancel);
            bCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

        }
    };

    private void ExecutePicked(String binId, String lotId, String qty, String storageId, String uom, final int position, HashMap<String, String> mapBin) {
        final int pos = position;
        final String fQty = qty;
        List<PickDetObj> lstPickDet = new ArrayList<>();
        PickDetObj detObj = new PickDetObj();
        detObj.setSheetId(holder.SheetId.getText().toString());
        detObj.setSeq(Double.parseDouble(holder.Seq.getText().toString()));
        detObj.setItemId(holder.ItemId.getText().toString());
        detObj.setLotId(lotId);
        detObj.setStorageId(storageId);
        detObj.setBinId(binId);
        detObj.setQty(Double.parseDouble(qty));
        detObj.setUom(uom);
        lstPickDet.add(detObj);

        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.PickDetObj", "bmWMS.Library.Param");
        MList mListEnum = new MList(vListEnum);
        String strLsRelatData = mListEnum.generateFinalCode(lstPickDet);

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BDeliveryNotePicking");
        bmObj.setModuleID("");
        bmObj.setRequestID("BDeliveryNotePicking");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo param = new ParameterInfo();
        param.setParameterID(BStockOutBaseParam.CheckStockOutByObjs);
        param.setNetParameterValue(strLsRelatData);
        bmObj.params.add(param);

        // region 儲存盤點狀態檢查物件
        // 20220805 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中
        List<CheckCountObj> lstChkCountObj = new ArrayList<>();
        CheckCountObj chkCountObjFromBin = new CheckCountObj(); // FROM_BIN
        chkCountObjFromBin.setStorageId(storageId);
        chkCountObjFromBin.setItemId(holder.ItemId.getText().toString());
        chkCountObjFromBin.setBinId(binId);
        lstChkCountObj.add(chkCountObjFromBin);
        CheckCountObj chkCountObjToBin = new CheckCountObj(); // TO_BIN
        chkCountObjToBin.setStorageId(storageId);
        chkCountObjToBin.setItemId(holder.ItemId.getText().toString());
        chkCountObjToBin.setBinId(mapBin.get(storageId));
        lstChkCountObj.add(chkCountObjToBin);
        // endregion

        VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create(VirtualClass.VirtualClassType.String);
        MesSerializableDictionary msd = new MesSerializableDictionary(vKey, vVal);
        String serializedObj = msd.generateFinalCode(mapBin);

        ParameterInfo paramBin = new ParameterInfo();
        paramBin.setParameterID(BDeliveryNotePickingParam.StorageTempBin);
        paramBin.setNetParameterValue(serializedObj);
        bmObj.params.add(paramBin);

        VirtualClass vListEnum2 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.CheckCountObj", "bmWMS.Library.Param");
        MList mListEnum2 = new MList(vListEnum2);
        String strCheckCountObj = mListEnum2.generateFinalCode(lstChkCountObj);

        ParameterInfo paramExecuteChkStock = new ParameterInfo();
        paramExecuteChkStock.setParameterID(BDeliveryNotePickingParam.ExecuteCheckStock); // 20220805 Add by Ikea 是否執行盤點檢查
        paramExecuteChkStock.setNetParameterValue2("true");
        bmObj.params.add(paramExecuteChkStock);

        ParameterInfo paramChkCountObj = new ParameterInfo(); // 20220805 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中
        paramChkCountObj.setParameterID(BDeliveryNotePickingParam.CheckCountObj);
        paramChkCountObj.setNetParameterValue(strCheckCountObj);
        bmObj.params.add(paramChkCountObj);

        CallBModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    pickedCount += Double.parseDouble(fQty);
                    holder.SheetDetPickQty.setText(String.format("%s/%s", String.valueOf(pickedCount), String.valueOf(totalPickedCount)));

                    /*Double registerQty = Double.parseDouble(pickedData.Rows.get(position).getValue("QTY").toString())
                           - Double.parseDouble(fQty);

                    pickedData.Rows.get(position).setValue("QTY", String.valueOf(registerQty));

                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    DeliveryNoteDetPickGridAdapter adapter = new DeliveryNoteDetPickGridAdapter(pickedData, inflater);
                    holder.RegisterData.setAdapter(adapter);
                    holder.RegisterData.setOnItemClickListener(onClickListView);*/
                    ShowMessage(R.string.WAPG006007);
                    getRegisterData();
                }else
                {
                    ShowMessage(R.string.WAPG006008);
                }
            }
        });
    }
}
