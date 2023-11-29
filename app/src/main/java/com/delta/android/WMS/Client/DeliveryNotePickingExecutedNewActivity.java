package com.delta.android.WMS.Client;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Client.GridAdapter.DeliveryNoteDetPickGridNewAdapter;
import com.delta.android.WMS.Param.BDeliveryNotePickingParam;
import com.delta.android.WMS.Param.BIDeliveryNotePickingPortalParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.BStockOutBaseParam;
import com.delta.android.WMS.Param.ParamObj.CheckCountObj;
import com.delta.android.WMS.Param.ParamObj.PickDetObj;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class DeliveryNotePickingExecutedNewActivity extends BaseFlowActivity {

    double reqCount = 0; // 需求數量
    double pickedCount = 0;  // 已揀數量
    String actualQtyStatus; //單據實際數量的狀態

    TextView tvDnSheetId;
    TextView tvPickSheetId;
    TextView tvItemId;
    ImageButton ibtnRegIdQRScan;
    ImageButton ibtnBinIdQRScan;
    EditText etRegId;
    EditText etBinId;
    TextView tvSeq;
    TextView tvPickQty;
    ListView lvRegisterData;
    CheckBox chkUseRec;

    DataTable dtCanPickReg;
    DataTable dtNeedToPickSeq;
    String pickShtId;
    double seq;
    String strIsRecommend = "true";
    private final int REG_ID_QRSCAN_REQUEST_CODE = 11;
    private final int BIN_ID_QRSCAN_REQUEST_CODE = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wms_delivery_note_picking_executed);

        initialData();

        pickShtId = getIntent().getStringExtra("pickShtId");
        seq = getIntent().getDoubleExtra("seq", 0);

        getPickAndRegister();

        chkUseRec.setOnClickListener(checkedChange);
        ibtnRegIdQRScan.setOnClickListener(onClickQRScan);
        ibtnBinIdQRScan.setOnClickListener(onClickQRScan);

        etRegId.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {

                if (keyCode != KeyEvent.KEYCODE_ENTER) return false;

                if (event.getAction() == KeyEvent.ACTION_UP) {

                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                    filterRegister(dtCanPickReg);

                    return true;
                }

                return false;
            }
        });

        etBinId.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {

                if (keyCode != KeyEvent.KEYCODE_ENTER) return false;

                if (event.getAction() == KeyEvent.ACTION_UP) {

                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                    String regId = etRegId.getText().toString().trim();
                    if (regId.length() <= 0)
                        ShowMessage(R.string.WAPG001003); // 請輸入存貨代碼
                    else
                        filterBin(dtCanPickReg);

                    return true;
                }

                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {

                if (requestCode == REG_ID_QRSCAN_REQUEST_CODE || requestCode == BIN_ID_QRSCAN_REQUEST_CODE) {
                    Toast.makeText(this, getResources().getText(R.string.QRCODE_SCAN_CANCEL).toString(), Toast.LENGTH_SHORT).show();
                }

            } else {

                switch (requestCode) {
                    case REG_ID_QRSCAN_REQUEST_CODE:
                        etRegId.setText(result.getContents().trim());
                        break;

                    case BIN_ID_QRSCAN_REQUEST_CODE:
                        etBinId.setText(result.getContents().trim());
                        break;

                    default:
                        break;
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        gotoPreviousActivity(DeliveryNotePickingDetailNewActivity.class);
    }

    private View.OnClickListener onClickQRScan = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            int getRequestCode = 0;

            if (view.getId() == R.id.ibtnRegisterIdQRScan)
                getRequestCode = REG_ID_QRSCAN_REQUEST_CODE;
            else if (view.getId() == R.id.ibtnBinIdQRScan)
                getRequestCode = BIN_ID_QRSCAN_REQUEST_CODE;

            IntentIntegrator integrator = new IntentIntegrator(DeliveryNotePickingExecutedNewActivity.this);
            // 設置要掃描的條碼類型，ONE_D_CODE_TYPES：一維碼，QR_CODE_TYPES-二維碼
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("BarCode Scan"); //底部的提示文字
            integrator.setCameraId(0); //前置(1)或者後置(0)攝像頭
            integrator.setBeepEnabled(false); //掃描成功的「嗶嗶」聲
            integrator.setBarcodeImageEnabled(false); //是否保留掃碼成功時候的截圖
            integrator.setCaptureActivity(ScanActivity.class);
            integrator.setRequestCode(getRequestCode);
            integrator.initiateScan();
        }
    };

    private void filterRegister(DataTable dtCanPickReg) {
        if (dtCanPickReg == null || dtCanPickReg.Rows.size() <= 0)
            return;

        String regId = etRegId.getText().toString().trim();
        DataTable dtFilter = new DataTable();
        if (regId.length() > 0) {
            for (DataRow drFind : dtCanPickReg.Rows) {
                if (drFind.getValue("REGISTER_ID").toString().equals(regId))
                    dtFilter.Rows.add(drFind);
            }
        }

        if (dtFilter.Rows.size() == 0 && regId.length() <= 0)
            dtFilter = dtCanPickReg;

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        DeliveryNoteDetPickGridNewAdapter adapter = new DeliveryNoteDetPickGridNewAdapter(dtFilter, inflater);
        lvRegisterData.setAdapter(adapter);

        if (dtFilter.Rows.size() == 1)
        {
            ShowDialog(dtFilter);
        }
    }

    private void filterBin(DataTable dtCanPickReg) {
        if (dtCanPickReg == null || dtCanPickReg.Rows.size() <= 0)
            return;

        String regId = etRegId.getText().toString().trim();
        DataTable dtFilterReg = new DataTable();
        if (regId.length() > 0) {
            for (DataRow drFind : dtCanPickReg.Rows) {
                if (drFind.getValue("REGISTER_ID").toString().equals(regId))
                    dtFilterReg.Rows.add(drFind);
            }
        }

        DataTable dtFilterBin = new DataTable();
        if (dtFilterReg.Rows.size() == 0 ) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            DeliveryNoteDetPickGridNewAdapter adapter = new DeliveryNoteDetPickGridNewAdapter(dtFilterReg, inflater);
            lvRegisterData.setAdapter(adapter);
        } else if (dtFilterReg.Rows.size() > 0) {
            String binId = etBinId.getText().toString().trim();
            if (binId.length() > 0) {
                for (DataRow drFind : dtFilterReg.Rows) {
                    if (drFind.getValue("BIN_ID").toString().equals(binId))
                        dtFilterBin.Rows.add(drFind);
                }
            }

            if (dtFilterBin.Rows.size() == 0 && binId.length() <= 0)
                dtFilterBin = dtFilterReg;

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            DeliveryNoteDetPickGridNewAdapter adapter = new DeliveryNoteDetPickGridNewAdapter(dtFilterBin, inflater);
            lvRegisterData.setAdapter(adapter);

            if (dtFilterBin.Rows.size() == 1)
            {
                ShowDialog(dtFilterBin);
            }
        }
    }

    private View.OnClickListener checkedChange = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if(chkUseRec.isChecked()) {

                strIsRecommend = "true";
                getPickAndRegister();

            } else {

                strIsRecommend = "false";
                getPickAndRegister();

            }
        }
    };

    private void ShowDialog(final DataTable dtFilter) {

        AlertDialog.Builder pickedQtyDialog = new AlertDialog.Builder(DeliveryNotePickingExecutedNewActivity.this);
        View dialogView = LayoutInflater.from(DeliveryNotePickingExecutedNewActivity.this).inflate(R.layout.style_wms_dialog_picked_qty, null);
        pickedQtyDialog.setTitle("");
        pickedQtyDialog.setView(dialogView);
        final EditText etQty = dialogView.findViewById(R.id.etPickedQty);
        final Spinner cmbBin = dialogView.findViewById(R.id.cmbTempBin);

        final double canPickQty = Double.parseDouble(dtFilter.Rows.get(0).get("CAN_PICK_QTY").toString());
        final double needToPickQty = Double.parseDouble(dtFilter.Rows.get(0).get("QTY").toString());
        final double rsvPickQty = Double.parseDouble(dtFilter.Rows.get(0).get("RESERVED_QTY").toString());

        final String itemType = dtFilter.Rows.get(0).get("REGISTER_TYPE").toString();

        double qty = 0.0;

        switch (itemType) {
            case "MinimizePackSN":
                if (canPickQty != 0) {
                    qty = canPickQty; // 若有預約數量，直接顯示
                    etQty.setText(String.valueOf(qty));
                } else {
                    qty = needToPickQty; // 最小包號要整批出
                    etQty.setText(String.valueOf(qty));
                }
                break;
            case "LotNo":
            case "ItemID":
                if (canPickQty != 0) {
                    qty = canPickQty; // 若有預約數量，直接顯示
                    etQty.setText(String.valueOf(qty));
                } else {
                    qty = needToPickQty;  // 非最小包號依是否達揀貨數量出
                    if ((qty + pickedCount) <= reqCount) {
                        etQty.setText(String.valueOf(qty));
                    } else {
                        etQty.setText(String.valueOf(reqCount-pickedCount));
                    }
                }
                break;
            default:
                break;
        }

        etQty.setEnabled(true);

        BModuleObject bimObj = new BModuleObject();
        bimObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bimObj.setModuleID("BIFetchBin");
        bimObj.setRequestID("BIFetchBin");
        bimObj.params = new Vector<ParameterInfo>();

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Filter);
        param1.setParameterValue(String.format(" AND STORAGE_ID ='%s' AND B.BIN_TYPE IN ('OT', 'OS')", dtFilter.Rows.get(0).get("STORAGE_ID").toString()));
        bimObj.params.add(param1);

        CallBIModule(bimObj, new WebAPIClientEvent(){
            @Override
            public void onPostBack(BModuleReturn bModuleReturn){
                if(CheckBModuleReturnInfo(bModuleReturn)){
                    DataTable dtBin = bModuleReturn.getReturnJsonTables().get("BIFetchBin").get("BIN");

                    boolean bOT = false;
                    for(DataRow dr : dtBin.Rows)
                    {
                        if(dr.getValue("BIN_TYPE").equals("OT"))
                        {
                            bOT = true;
                            break;
                        }
                    }

                    ArrayList<String> alBin = new ArrayList<String>();
                    for(int i = 0; i < dtBin.Rows.size(); i++)
                    {
                        String binId = dtBin.Rows.get(i).getValue("BIN_ID").toString();
                        String binType = dtBin.Rows.get(i).getValue("BIN_TYPE").toString();

                        if(bOT)
                        {
                            if(binType.equals("OT"))
                                alBin.add(binId);
                        }
                        else
                        {
                            if(!alBin.contains(binId))
                                alBin.add(binId);
                        }
                    }
                    ArrayAdapter<String> adapterBin = new ArrayAdapter<>(DeliveryNotePickingExecutedNewActivity.this, android.R.layout.simple_spinner_dropdown_item, alBin);
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

                switch (itemType) {
                    case "MinimizePackSN":
                        if (needToPickQty != 0) {
                            if (inputQty != needToPickQty) {
                                Object[] args = new Object[2];
                                args[0] = itemType;
                                args[1] = needToPickQty;
                                ShowMessage(R.string.WAPG001014, args); // WAPG001014   該物料註冊類別為[%s]，輸入數量須為整批數量[%s]
                                return;
                            }
                        } else {
                            if (inputQty != canPickQty) {
                                Object[] args = new Object[2];
                                args[0] = itemType;
                                args[1] = canPickQty;
                                ShowMessage(R.string.WAPG001014, args); // WAPG001014   該物料註冊類別為[%s]，輸入數量須為整批數量[%s]
                                return;
                            }
                        }
                        break;
                    case "LotNo":
                    case "ItemID":
                        if (inputQty > (canPickQty + needToPickQty)) {
                            Object[] args = new Object[1];
                            args[0] = String.valueOf(canPickQty + needToPickQty);
                            ShowMessage(R.string.WAPG001015, args); // WAPG001015   超出剩餘可揀的數量[%s]
                            return;
                        }
                        break;
                    default:
                        break;
                }

                switch (actualQtyStatus)
                {
                    case "Equal":
                    case "Less":
                        if (Double.parseDouble(etQty.getText().toString()) > reqCount-pickedCount)
                        {
                            // WAPG001008   單據[%s],順序[%s]揀貨數量[%s]不可大於單據數量[%s]
                            ShowMessage(R.string.WAPG001008,
                                    tvPickSheetId.getText().toString(),
                                    tvSeq.getText().toString(),
                                    (Double.parseDouble(etQty.getText().toString())),
                                    reqCount - pickedCount);
                            return;
                        }
                        break;

                    default:
                        break;
                }

                String binId = dtFilter.Rows.get(0).getValue("BIN_ID").toString();
                String lotId = dtFilter.Rows.get(0).getValue("REGISTER_ID").toString();
                String qty = dtFilter.Rows.get(0).getValue("QTY").toString();
                String storageId = dtFilter.Rows.get(0).getValue("STORAGE_ID").toString();
                String uom = dtFilter.Rows.get(0).getValue("ITEM_UOM").toString();
                String enterQty= etQty.getText().toString();
                String tempBin = cmbBin.getSelectedItem().toString();

                if (Double.parseDouble( enterQty )  <= 0)
                {
                    //WAPG006005    揀貨數量需大於零
                    ShowMessage(R.string.WAPG006005);
                    return;
                }

                HashMap<String, String> mapBin = new HashMap<>();
                mapBin.put(storageId, tempBin);

                ExecutePicked(binId, lotId, enterQty, storageId, uom, 0, mapBin); // 執行BModule
                if (Double.compare(Double.parseDouble(qty) ,Double.parseDouble(dtFilter.Rows.get(0).getValue("QTY").toString())) == 0){
                    dtFilter.Rows.get(0).setValue("SELECTED", "True");
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

    private void initialData() {

        tvDnSheetId = findViewById(R.id.tvDnSheetId);
        tvPickSheetId = findViewById(R.id.tvSheetDetPickSheetId);
        tvItemId = findViewById(R.id.tvSheetDetPickItemId);
        ibtnRegIdQRScan = findViewById(R.id.ibtnRegisterIdQRScan);
        ibtnBinIdQRScan = findViewById(R.id.ibtnBinIdQRScan);
        etRegId = findViewById(R.id.etSheetDetPickRegisterId);
        etBinId = findViewById(R.id.etSheetDetPickBinId);
        tvSeq = findViewById(R.id.tvSheetDetPickSeq);
        tvPickQty = findViewById(R.id.tvSheetDetPickQty);
        lvRegisterData = findViewById(R.id.lvSheetDetPickRegisterData);
        chkUseRec = findViewById(R.id.chkUseRec);
    }

    private void checkPickedCount() {

        if (dtNeedToPickSeq == null || dtNeedToPickSeq.Rows.size() <= 0) {
            gotoPreviousActivity(DeliveryNotePickingDetailNewActivity.class);
            return;
        }

        double pickedCount = Double.parseDouble(dtNeedToPickSeq.Rows.get(0).getValue("PROC_QTY").toString());
        reqCount = Double.parseDouble(dtNeedToPickSeq.Rows.get(0).getValue("TRX_QTY").toString());
        tvSeq.setText(dtNeedToPickSeq.Rows.get(0).getValue("SEQ").toString());
        tvPickQty.setText(String.format("%s/%s", String.valueOf(pickedCount), String.valueOf(reqCount)));
        if (pickedCount >= reqCount) {
            gotoPreviousActivity(DeliveryNotePickingExecutedNewActivity.class);
        }
    }

    private void getPickAndRegister() {
        BModuleObject bmObjSetData = new BModuleObject();
        bmObjSetData.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIDeliveryNotePickingPortal");
        bmObjSetData.setModuleID("BIFetchDeliveryNoteCanPickData");
        bmObjSetData.setRequestID("BIFetchDeliveryNoteCanPickData");
        bmObjSetData.params = new Vector<>();

        ArrayList<String> lstPickSht = new ArrayList<String>();
        lstPickSht.add(pickShtId);
        VirtualClass vList = VirtualClass.create(VirtualClass.VirtualClassType.String);
        MList mList = new MList(vList);
        String strLstSheetId= mList.generateFinalCode(lstPickSht);
        ParameterInfo param = new ParameterInfo();
        param.setParameterID(BIDeliveryNotePickingPortalParam.SheetId);
        param.setNetParameterValue(strLstSheetId);
        bmObjSetData.params.add(param);

        ParameterInfo paramSeq = new ParameterInfo();
        paramSeq.setParameterID(BIDeliveryNotePickingPortalParam.Seq);
        paramSeq.setParameterValue(seq);
        bmObjSetData.params.add(paramSeq);

        ParameterInfo paramIsRecommend = new ParameterInfo();
        paramIsRecommend.setParameterID(BIDeliveryNotePickingPortalParam.IsRecommend);
        paramIsRecommend.setNetParameterValue(strIsRecommend);
        bmObjSetData.params.add(paramIsRecommend);

        CallBIModule(bmObjSetData, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {

                    dtCanPickReg = new DataTable();
                    dtNeedToPickSeq = new DataTable();
                    actualQtyStatus = "";

                    if (bModuleReturn.getReturnJsonTables().get("BIFetchDeliveryNoteCanPickData").containsKey("Register"))
                        dtCanPickReg = bModuleReturn.getReturnJsonTables().get("BIFetchDeliveryNoteCanPickData").get("Register");
                    if (bModuleReturn.getReturnJsonTables().get("BIFetchDeliveryNoteCanPickData").containsKey("dtNeedToPickSeq"))
                        dtNeedToPickSeq = bModuleReturn.getReturnJsonTables().get("BIFetchDeliveryNoteCanPickData").get("dtNeedToPickSeq");
                    if (bModuleReturn.getReturnList().get("BIFetchDeliveryNoteCanPickData") != null)
                        actualQtyStatus = bModuleReturn.getReturnList().get("BIFetchDeliveryNoteCanPickData").get("ActualQtyStatus").toString();

                    if (dtNeedToPickSeq.Rows.size() > 0) {
                        tvDnSheetId.setText(dtNeedToPickSeq.Rows.get(0).getValue("DN_ID").toString());
                        tvPickSheetId.setText(dtNeedToPickSeq.Rows.get(0).getValue("SHEET_ID").toString());
                        tvItemId.setText(dtNeedToPickSeq.Rows.get(0).getValue("ITEM_ID").toString());
                    }

                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    DeliveryNoteDetPickGridNewAdapter adapter = new DeliveryNoteDetPickGridNewAdapter(dtCanPickReg, inflater);
                    lvRegisterData.setAdapter(adapter);
                    checkPickedCount();
                }
            }
        });
    }

    private void ExecutePicked(String binId, String lotId, String qty, String storageId, String uom, final int position, HashMap<String, String> mapBin) {
        final int pos = position;
        final String fQty = qty;
        List<PickDetObj> lstPickDet = new ArrayList<>();
        PickDetObj detObj = new PickDetObj();
        detObj.setSheetId(tvPickSheetId.getText().toString());
        detObj.setSeq(Double.parseDouble(tvSeq.getText().toString()));
        detObj.setItemId(tvItemId.getText().toString());
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

        ParameterInfo paramMode = new ParameterInfo();
        paramMode.setParameterID(BStockOutBaseParam.Mode);
        paramMode.setParameterValue("DeliveryNotePick");
        bmObj.params.add(paramMode);

        // region 儲存盤點狀態檢查物件
        // 20220805 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中
        List<CheckCountObj> lstChkCountObj = new ArrayList<>();
        CheckCountObj chkCountObjFromBin = new CheckCountObj(); // FROM_BIN
        chkCountObjFromBin.setStorageId(storageId);
        chkCountObjFromBin.setItemId(tvItemId.getText().toString());
        chkCountObjFromBin.setBinId(binId);
        lstChkCountObj.add(chkCountObjFromBin);
        CheckCountObj chkCountObjToBin = new CheckCountObj(); // TO_BIN
        chkCountObjToBin.setStorageId(storageId);
        chkCountObjToBin.setItemId(tvItemId.getText().toString());
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

                    String holdItemId = "";
                    String holdMessage = "";
                    String hintMessage = "";

                    if (bModuleReturn.getReturnList().get("BDeliveryNotePicking") != null) {

                        if (bModuleReturn.getReturnList().get("BDeliveryNotePicking").get(BDeliveryNotePickingParam.HoldItemID) != null) {
                            holdItemId = bModuleReturn.getReturnList().get("BDeliveryNotePicking").get(BDeliveryNotePickingParam.HoldItemID).toString().replaceAll("\"", "");
                        }

                        if (bModuleReturn.getReturnList().get("BDeliveryNotePicking").get(BDeliveryNotePickingParam.HoldMessage) != null) {
                            holdMessage = bModuleReturn.getReturnList().get("BDeliveryNotePicking").get(BDeliveryNotePickingParam.HoldMessage).toString().replaceAll("\"", "");
                            holdMessage = split(holdMessage);
                        }

                        if (bModuleReturn.getReturnList().get("BDeliveryNotePicking").get(BDeliveryNotePickingParam.HintMessage) != null) {
                            hintMessage = bModuleReturn.getReturnList().get("BDeliveryNotePicking").get(BDeliveryNotePickingParam.HintMessage).toString().replaceAll("\"", "");
                            hintMessage = split(hintMessage);
                        }
                    }

                    String showExtraMsg = "";

                    if (hintMessage.length() > 0) {
                        showExtraMsg += getResString(getResources().getString(R.string.ITEM_SPEC_CTRL_HINT_MSG)) + System.getProperty("line.separator");
                        showExtraMsg += hintMessage + System.getProperty("line.separator");
                    }

                    if (holdMessage.length() > 0) {
                        showExtraMsg += getResString(getResources().getString(R.string.ITEM_SPEC_CTRL_HOLD_MSG)) + System.getProperty("line.separator");
                        showExtraMsg += holdMessage + System.getProperty("line.separator");
                    }

                    etRegId.setText("");
                    etBinId.setText("");

                    if (holdItemId.length() > 0) { // 有被鎖定的物料代碼 => 揀貨失敗, 有額外資訊需顯示

                        Object[] args = new Object[1];
                        args[0] = holdItemId;

                        // 揀貨失敗
                        final String finalShowExtraMsg = showExtraMsg;
                        ShowMessage(R.string.WAPG006017, new ShowMessageEvent() {
                            @Override
                            public void onDismiss() {

                                if (finalShowExtraMsg.length() > 0) {

                                    ShowMessage(finalShowExtraMsg, new ShowMessageEvent() {
                                        @Override
                                        public void onDismiss() {
                                            getPickAndRegister();
                                        }
                                    });

                                } else {
                                    getPickAndRegister();
                                }

                            }
                        }, args);

                    } else { // 揀貨成功, 但有額外資訊需顯示

                        final String finalShowExtraMsg = showExtraMsg;
                        if (finalShowExtraMsg.length() > 0) {

                            ShowMessage(finalShowExtraMsg, new ShowMessageEvent() {
                                @Override
                                public void onDismiss() {
                                    getPickAndRegister();
                                }
                            });

                        } else {
                            getPickAndRegister();
                        }
                    }
                }
            }
        });
    }

    // 將字串移除\r\n後，重新加入new line
    private String split(String str) {

        String result = "";

        if (str != null && str.length() > 0) {

            String delim = "\\\\r\\\\n";
            String splitArray[] = str.split(delim);
            for (int i = 0; i < splitArray.length; i++) {
                result += splitArray[i] + System.getProperty("line.separator");
            }

        }

        return result;
    }
}
