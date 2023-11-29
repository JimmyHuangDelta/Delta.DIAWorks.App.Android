package com.delta.android.WMS.Client;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.Activity.ScanActivity;
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
import com.delta.android.WMS.Client.GridAdapter.SheetCancelDetGridAdapter;
import com.delta.android.WMS.Client.GridAdapter.SheetCancelPickExecutedGridAdapter;
import com.delta.android.WMS.Client.GridAdapter.SheetDetPickGridAdapter;
import com.delta.android.WMS.Param.BGoodReservationParam;
import com.delta.android.WMS.Param.BIFetchPickStrategyParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.BStockOutBaseParam;
import com.delta.android.WMS.Param.ParamObj.CheckCountObj;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.delta.android.WMS.Param.ParamObj.PickDetObj;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class GoodCancelPickExecutedActivity extends BaseFlowActivity {

    //DataTable pickedData;
    DataTable sheetDetData;
    DataTable alreadyPickedData; // 含預約揀貨及已揀貨
    DataTable rsvPickedData; // 預約揀貨
    DataTable regData;
    boolean isConfirm;
    double pickedCount;
    double totalPickedCount;
    ViewHolder holder;
    private HashMap<String,String> ActualQtyStatus;
    //private HashMap<String,String> PickStrategy;
    private DataTable dtConfigCond;
    private DataTable dtConfigSort;
    private DataTable dtConfig;

    private final int REGISTER_ID_QRSCAN_REQUEST_CODE = 22; // 相機掃描回傳資訊使用
    private final int BIN_ID_QRSCAN_REQUEST_CODE = 33; // 相機掃描回傳資訊使用


    static class ViewHolder {
        EditText RegisterId;
        EditText BinId;
        TextView ItemId;
        TextView SheetId;
        TextView SheetDetPickQty;
        TextView Seq;
        ListView RegisterData;
        TextView PickSheetId;
        TextView tvColor;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good_cancel_pick_executed);

        this.initialData();
    }

    @Override
    public void onBackPressed() {
        //如果要回傳則需要寫此方法
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, getResources().getText(R.string.QRCODE_SCAN_CANCEL).toString(), Toast.LENGTH_SHORT).show();
            } else {
                switch (requestCode) {
                    case REGISTER_ID_QRSCAN_REQUEST_CODE:
                        holder.RegisterId.setText(result.getContents().trim());
                        break;
                    case BIN_ID_QRSCAN_REQUEST_CODE:
                        holder.BinId.setText(result.getContents().trim());
                        break;
                    default:
                        break;
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initialData() {

        sheetDetData = (DataTable) getIntent().getSerializableExtra("DetTable");
        ActualQtyStatus = (HashMap<String, String>) getIntent().getSerializableExtra("actualQtyStatus");
        //PickStrategy = (HashMap<String, String>) getIntent().getSerializableExtra("pickingStrategy");
        //alreadyPickedData = (DataTable) getIntent().getSerializableExtra("DetPicked");

        DataRow sheetDetRow = sheetDetData.Rows.get(0);
        holder = new ViewHolder();

        holder.BinId = findViewById(R.id.etSheetDetPickBinId);
        holder.RegisterId = findViewById(R.id.etSheetDetPickRegisterId);
        holder.ItemId = findViewById(R.id.tvSheetDetPickItemId);
        holder.SheetId = findViewById(R.id.tvSheetDetPickSheetId);
        holder.SheetDetPickQty = findViewById(R.id.tvSheetDetPickQty);
        holder.Seq = findViewById(R.id.tvSheetDetPickSeq);
        holder.RegisterData = findViewById(R.id.lvSheetDetPickRegisterData);
        holder.PickSheetId = findViewById(R.id.tvSheetDetPickSheetPickId);
        holder.tvColor = findViewById(R.id.tvColor);

        if (sheetDetRow == null) return;

        holder.ItemId.setText(sheetDetRow.getValue("ITEM_ID").toString());
        holder.SheetId.setText(sheetDetRow.getValue("SOURCE_SHEET_ID").toString());
        holder.PickSheetId.setText(sheetDetRow.getValue("SHEET_ID").toString());
        holder.Seq.setText(sheetDetRow.getValue("SEQ").toString());
        holder.RegisterData.setOnItemClickListener(onClickListView);

        //this.checkPicked();
        //holder.SheetDetPickQty.setText(String.format("%s/%s", String.valueOf(pickedCount), String.valueOf(totalPickedCount)));

        holder.RegisterId.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    filterRegister();
                    return true;
                }
                return false;
            }
        });

        holder.BinId.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    filterBin();
                    return true;
                }
                return false;
            }
        });

        //this.getPickAndRegisterData();

        //this.getPickAndRegister();

        this.getConfigData();

        //region 修改用一般方式搜尋即可
//        holder.RegisterId.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_SEARCH){
//                    InputMethodManager manager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//                    manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                    filterRegister();
//                }
//                return false;
//            }
//        });
        //endregion

        //region 修改用一般方式搜尋即可
//        holder.BinId.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_SEARCH){
//                    InputMethodManager manager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//                    manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                    filterRegister();
//                }
//                return false;
//            }
//        });
        //endregion
    }

    private void checkPickedQty() {
        pickedCount = 0;
        totalPickedCount = Double.parseDouble(sheetDetData.Rows.get(0).getValue("TRX_QTY").toString());
        rsvPickedData = new DataTable();
        if (alreadyPickedData == null || alreadyPickedData.Rows.size() == 0) return;
        for (DataRow dr : alreadyPickedData.Rows) {
            if (!dr.getValue("SEQ").toString().equals(holder.Seq.getText().toString()))
                continue;
            if (dr.getValue("IS_PICKED").equals("Y"))
                pickedCount += Double.parseDouble(dr.getValue("QTY").toString());
            else
                rsvPickedData.Rows.add(dr);
        }
        if (pickedCount >= totalPickedCount) {
            //gotoPreviousActivity(GoodCancelPickDetailActivity.class);
        }
    }

    private void filterRegister() {
        if (alreadyPickedData == null || alreadyPickedData.Rows.size() == 0) return;
        String registerId = holder.RegisterId.getText().toString().toUpperCase().trim(); //20200729 archie 轉大寫
        DataTable dt = new DataTable();
        if (!registerId.equals("")) {
            for (DataRow reg : alreadyPickedData.Rows) {
                if (reg.getValue("LOT_ID").toString().equals(registerId)) {
                    dt.Rows.add(reg);
                }
            }
        }
        if (dt.Rows.size() == 0 && registerId.equals(""))
            dt = alreadyPickedData;

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        SheetCancelPickExecutedGridAdapter adapter = new SheetCancelPickExecutedGridAdapter(dt, inflater);
        holder.RegisterData.setAdapter(adapter);
        holder.RegisterData.setOnItemClickListener(onClickListView);
    }

    private void filterBin() {
        if (alreadyPickedData == null || alreadyPickedData.Rows.size() == 0) return;
        String bnId = holder.BinId.getText().toString().toUpperCase().trim(); //20200729 archie 轉大寫
        DataTable dt = new DataTable();
        if (!bnId.equals("")) {
            for (DataRow reg : alreadyPickedData.Rows) {
                if (reg.getValue("BIN_ID").toString().equals(bnId)) {
                    dt.Rows.add(reg);
                }
            }
        }
        if (dt.Rows.size() == 0 && bnId.equals(""))
            dt = alreadyPickedData;

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        SheetCancelPickExecutedGridAdapter adapter = new SheetCancelPickExecutedGridAdapter(dt, inflater);
        holder.RegisterData.setAdapter(adapter);
        holder.RegisterData.setOnItemClickListener(onClickListView);
    }

    private void getConfigData() {
        List<BModuleObject> biObjs = new ArrayList<BModuleObject>();
        biObjs.add(getConfigCondAndSortBModuleObj());
        biObjs.add(getConfigBModuleObj());

        CallBIModule(biObjs, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    dtConfigCond = bModuleReturn.getReturnJsonTables().get("FetchConfigCondAndSortBySheetId").get("SheetConfigCond");
                    dtConfigSort = bModuleReturn.getReturnJsonTables().get("FetchConfigCondAndSortBySheetId").get("SheetConfigSort");
                    dtConfig = bModuleReturn.getReturnJsonTables().get("FetchWmsSheetConfigBySheetId").get("SBRM_WMS_SHEET_CONFIG");

                    getPickAndRegister();
                }
            }
        });
    }

    private void getPickAndRegister() {
        List<BModuleObject> biObjs = new ArrayList<BModuleObject>();
        //biObjs.add(getRegisterDataBModuleObj(strIsRecommend));
        biObjs.add(getPickDataBModuleObj());

        CallBIModule(biObjs, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    DataTable dt = bModuleReturn.getReturnJsonTables().get("BIFetchPick").get("ShtPickDet");

                    alreadyPickedData = new DataTable();

                    if (dt != null && dt.Rows.size() > 0)
                    {
                        for (DataRow d : dt.Rows)
                        {
                            if (d.getValue("SEQ").toString().equals(holder.Seq.getText().toString()))
                            {
                                alreadyPickedData.Rows.add(d);
                            }
                        }
                    }

                    checkPickedQty();
                    holder.SheetDetPickQty.setText(String.format("%s/%s", String.valueOf(pickedCount), String.valueOf(totalPickedCount)));

                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    holder.RegisterData.setAdapter(new SheetCancelPickExecutedGridAdapter(alreadyPickedData, inflater));
                }
            }
        });
    }

    private BModuleObject getPickDataBModuleObj() {
        BModuleObject biObj = new BModuleObject();
        biObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        biObj.setModuleID("BIFetchSheetPickMstAndDet");
        biObj.setRequestID("BIFetchPick");
        biObj.params = new Vector<>();
        VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");

        List<Condition> conditionMs = new ArrayList<>();
        List<Condition> conditionSheetType = new ArrayList<>();
        HashMap<String, List<?>> dicCondition = new HashMap<>();

        Condition condSheet = new Condition();
        condSheet.setAliasTable("MST");
        condSheet.setColumnName("SHEET_ID");
        condSheet.setDataType("string");
        condSheet.setValue(sheetDetData.Rows.get(0).getValue("SHEET_ID").toString());
        conditionMs.add(condSheet);
        dicCondition.put("SHEET_ID", conditionMs);

        Condition condSheetType = new Condition();
        condSheetType.setAliasTable("ST");
        condSheetType.setColumnName("SHEET_TYPE_ID");
        condSheetType.setDataType("string");
        condSheetType.setValue(sheetDetData.Rows.get(0).getValue("SHEET_TYPE_ID").toString());
        conditionSheetType.add(condSheetType);
        dicCondition.put("SHEET_TYPE_ID", conditionSheetType);

        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vKey, vVal);
        String serializedObj = msdl.generateFinalCode(dicCondition);

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Condition);
        param1.setNetParameterValue(serializedObj);

        biObj.params.add(param1);
        return biObj;
    }

    private BModuleObject getConfigCondAndSortBModuleObj() {

        // region 取得ConfigCond及ConfigSort
        BModuleObject biShtCfgSortAndCond = new BModuleObject();
        biShtCfgSortAndCond.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        biShtCfgSortAndCond.setModuleID("BIFetchConfigCondAndSortBySheetId");
        biShtCfgSortAndCond.setRequestID("FetchConfigCondAndSortBySheetId");
        biShtCfgSortAndCond.params = new Vector<ParameterInfo>();
        ParameterInfo paramShtTypeKey = new ParameterInfo();
        paramShtTypeKey.setParameterID(BIFetchPickStrategyParam.SheetId);
        paramShtTypeKey.setParameterValue(sheetDetData.Rows.get(0).getValue("SHEET_ID").toString());
        biShtCfgSortAndCond.params.add(paramShtTypeKey);
        // endregion

        return biShtCfgSortAndCond;
    }

    private BModuleObject getConfigBModuleObj() {

        // region 取得Config
        BModuleObject biShtCfg = new BModuleObject();
        biShtCfg.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        biShtCfg.setModuleID("BIFetchWmsSheetConfigBySheetId");
        biShtCfg.setRequestID("FetchWmsSheetConfigBySheetId");
        biShtCfg.params = new Vector<ParameterInfo>();
        ParameterInfo paramFilter = new ParameterInfo();
        paramFilter.setParameterID(BIWMSFetchInfoParam.Filter);
        paramFilter.setParameterValue(String.format("  AND M.SHEET_ID = '%s'", sheetDetData.Rows.get(0).getValue("SHEET_ID").toString()));
        biShtCfg.params.add(paramFilter);
        // endregion

        return biShtCfg;
    }

    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

            //CheckBox cb = view.findViewById(R.id.cbPickedGridSelectedRegister);

            if (!sheetDetData.Rows.get(0).getValue("REGISTER_ID").toString().equals(""))
            {
                //WAPG028002 單據[%s],項次[%s]為指定揀料批序號,故不能取消
                ShowMessage(R.string.WAPG028002,
                        sheetDetData.Rows.get(0).getValue("SHEET_ID").toString(), sheetDetData.Rows.get(0).getValue("SEQ").toString());
                return;
            }

            TextView tvQty = view.findViewById(R.id.tvPickedGridQty); // 含可揀/要揀/預揀數量

            AlertDialog.Builder pickedQtyDialog = new AlertDialog.Builder(GoodCancelPickExecutedActivity.this);
            View dialogView = LayoutInflater.from(GoodCancelPickExecutedActivity.this).inflate(R.layout.style_wms_dialog_cancel_picked_qty, null);
            pickedQtyDialog.setTitle("");
            pickedQtyDialog.setView(dialogView);
            final EditText etQty = dialogView.findViewById(R.id.etPickedQty);
            final Spinner cmbBin = dialogView.findViewById(R.id.cmbTempBin);

            final String canPickQty = tvQty.getText().toString();

            final String itemType = sheetDetData.Rows.get(0).getValue("REGISTER_TYPE").toString();//registerData.get(position).RegisterType;

            //etQty.setText(tvQty.getText());

            double qty = 0.0;

            qty = Double.parseDouble(canPickQty); // 最小包號要整批出
            etQty.setText(String.valueOf(qty));

            switch (itemType) {
                case "MinimizePackSN":
                    etQty.setEnabled(false);
                    break;
                case "LotNo":
                case "ItemID":
                    etQty.setEnabled(true);
                    break;
                default:
                    break;
            }

            /*
            if (!needToPickQty.equals("0.0")) {
                qty = Double.parseDouble(needToPickQty);
                etQty.setText(String.valueOf(qty));
            } else {
                qty = Double.parseDouble(canPickQty);
                if ((qty + pickedCount) <= totalPickedCount)
                {
                    //etQty.setText(tvQty.getText());
                    etQty.setText(String.valueOf(qty));
                }
                else
                {
                    etQty.setText(String.valueOf(totalPickedCount-pickedCount));
                }
            }
             */

            //qty = Double.parseDouble(tvQty.getText().toString());

            BModuleObject bimObj = new BModuleObject();
            bimObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
            bimObj.setModuleID("BIFetchBin");
            bimObj.setRequestID("BIFetchBin");
            bimObj.params = new Vector<ParameterInfo>();

            ParameterInfo param1 = new ParameterInfo();
            param1.setParameterID(BIWMSFetchInfoParam.Filter);
            param1.setParameterValue(String.format(" AND STORAGE_ID ='%s' AND CMB.RESERVE != 'N'", alreadyPickedData.Rows.get(position).getValue("STORAGE_ID").toString()));
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
                        ArrayAdapter<String> adapterBin = new ArrayAdapter<>(GoodCancelPickExecutedActivity.this, android.R.layout.simple_spinner_dropdown_item, alBin);
                        cmbBin.setAdapter(adapterBin);
                    }
                }
            });


            final AlertDialog dialog = pickedQtyDialog.create();
            dialog.show();

            Button bConfirm = dialogView.findViewById(R.id.btnConfirm);
            bConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Double inputQty = Double.parseDouble(etQty.getText().toString());

                    if (inputQty > (Double.parseDouble(canPickQty)))
                    {
                        // WAPG028001   超出可取消的數量[%s]
                        ShowMessage(R.string.WAPG028001, canPickQty);
                        return;
                    }

                    if (dtConfig.Rows.get(0).getValue("ACTUAL_QTY_STATUS").toString().equals("")) {
                        // WAPG001009 單據類型[%s], 單據Config設定 [實際數量的狀態]未設置
                        ShowMessage(R.string.WAPG001009,
                                dtConfig.Rows.get(0).getValue("SHEET_TYPE_ID").toString());
                        return;
                    }

                    String binId = alreadyPickedData.Rows.get(position).getValue("BIN_ID").toString();
                    String lotId = alreadyPickedData.Rows.get(position).getValue("LOT_ID").toString();
                    String qty = etQty.getText().toString();
                    String storageId =alreadyPickedData.Rows.get(position).getValue("STORAGE_ID").toString();
                    String uom = alreadyPickedData.Rows.get(position).getValue("UOM").toString();
                    String tempBin = cmbBin.getSelectedItem().toString();

                    HashMap<String, String> mapBin = new HashMap<>();
                    mapBin.put(storageId, tempBin);

                    ExecutePicked(binId, lotId, qty, storageId, uom, position, mapBin);

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
        ArrayList<PickDetObj> lstPickDet = new ArrayList<>();
        final int pos = position;
        final String fQty = qty;
        PickDetObj detObj = new PickDetObj();
        detObj.setSheetId(holder.PickSheetId.getText().toString());
        detObj.setItemId(holder.ItemId.getText().toString());
        detObj.setSeq(Double.parseDouble(holder.Seq.getText().toString()));
        detObj.setBinId(mapBin.get(storageId));
        detObj.setLotId(lotId);
        detObj.setQty(Double.parseDouble(qty));
        detObj.setStorageId(storageId);
        detObj.setUom(uom);
        lstPickDet.add(detObj);

        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.PickDetObj", "bmWMS.Library.Param");
        MList mListEnum = new MList(vListEnum);
        String strLsRelatData = mListEnum.generateFinalCode(lstPickDet);

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BGoodCancelPicking");
        bmObj.setModuleID("");
        bmObj.setRequestID("BGoodCancelPicking");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo param = new ParameterInfo();
        param.setParameterID(BStockOutBaseParam.CheckStockOutByObjs); //20220829 archie 將可共用的Param改為出庫Base的Param
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

        VirtualClass vListEnum2 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.CheckCountObj", "bmWMS.Library.Param");
        MList mListEnum2 = new MList(vListEnum2);
        String strCheckCountObj = mListEnum2.generateFinalCode(lstChkCountObj);

        ParameterInfo paramExecuteChkStock = new ParameterInfo();
        paramExecuteChkStock.setParameterID(BGoodReservationParam.ExecuteCheckStock); // 20220805 Add by Ikea 是否執行盤點檢查
        paramExecuteChkStock.setNetParameterValue2("true");
        bmObj.params.add(paramExecuteChkStock);

        ParameterInfo paramChkCountObj = new ParameterInfo(); // 20220805 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中
        paramChkCountObj.setParameterID(BGoodReservationParam.CheckCountObj);
        paramChkCountObj.setNetParameterValue(strCheckCountObj);
        bmObj.params.add(paramChkCountObj);

        CallBModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    /*
//                    pickedCount += Double.parseDouble(fQty);
//                    holder.SheetDetPickQty.setText(String.format("%s/%s", String.valueOf(pickedCount), String.valueOf(totalPickedCount)));
//
//                    Double registerQty = Double.parseDouble(pickedData.Rows.get(position).getValue("QTY").toString())
//                            - Double.parseDouble(fQty);
//
//                    pickedData.Rows.get(position).setValue("QTY", String.valueOf(registerQty));
//
//                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                    SheetDetPickGridAdapter adapter = new SheetDetPickGridAdapter(registerData, inflater);
//                    holder.RegisterData.setAdapter(adapter);
//                    holder.RegisterData.setOnItemClickListener(onClickListView);
                    */
                    //getPickAndRegisterData();
                    getPickAndRegister();
                }
            }
        });
    }

    public void OnClickQRScan(View v) {
        IntentIntegrator integrator = new IntentIntegrator(GoodCancelPickExecutedActivity.this);
        // 設置要掃描的條碼類型，ONE_D_CODE_TYPES：一維碼，QR_CODE_TYPES-二維碼
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("BarCode Scan"); //底部的提示文字
        integrator.setCameraId(0); //前置(1)或者後置(0)攝像頭
        integrator.setBeepEnabled(false); //掃描成功的「嗶嗶」聲
        integrator.setBarcodeImageEnabled(false); //是否保留掃碼成功時候的截圖
        integrator.setCaptureActivity(ScanActivity.class);
        switch (v.getId()) {
            case R.id.ibtnRegisterIdQRScan:
                integrator.setRequestCode(REGISTER_ID_QRSCAN_REQUEST_CODE);
                break;
            case R.id.ibtnPickBinQRScan:
                integrator.setRequestCode(BIN_ID_QRSCAN_REQUEST_CODE);
                break;
            default:
                integrator.setRequestCode(0);
                break;
        }
        integrator.initiateScan();
    }
}
