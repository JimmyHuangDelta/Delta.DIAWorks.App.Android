package com.delta.android.WMS.Client;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseActivity;
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
import com.delta.android.WMS.Client.GridAdapter.GoodChangeBinGridAdapter;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.BWmsChangeBinParam;
import com.delta.android.WMS.Param.ParamObj.CheckCountObj;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GoodChangeBinActivity extends BaseFlowActivity {

    private ViewHolder _holder = null;
    private DataTable _registerTable = new DataTable();
    private String _selectedRegister = null;
    private String _selectedRegisterBin = null;

    //控制項放置區域
    static class ViewHolder {
        EditText SkuNum;
        ImageButton IbtnSkuNumQRScan; //QR Code Scan Button
        EditText ItemId;
        EditText FromBinId;
        ListView RegisterData;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_good_change_bin);

        InitialSetup();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, getResources().getText(R.string.QRCODE_SCAN_CANCEL).toString(), Toast.LENGTH_SHORT).show();
            } else {
                _holder.SkuNum.setText(result.getContents().trim());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //初始化控制項
    private void InitialSetup() {
        //初始化控制項
        if (_holder == null) {
            _holder = new ViewHolder();
            _holder.SkuNum = findViewById(R.id.etSkuNum);
            _holder.ItemId = findViewById(R.id.etItemId);
            _holder.FromBinId = findViewById(R.id.etFromBinId);
            _holder.RegisterData = findViewById(R.id.lvRegisters);
            _holder.IbtnSkuNumQRScan = findViewById(R.id.ibtnSkuNumQRScan);

            _holder.RegisterData.setOnItemClickListener(onClickListView);
            _holder.IbtnSkuNumQRScan.setOnClickListener(ibtnSkuNumQRScanOnClick);
        }

        //新增批號代碼輸入時的觸發事件
//        _holder.RegisterId.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
//                if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                    //按下Enter後觸發撈取資料
//                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
//
//                    return true;
//                }
//                return false;
//            }
//        });
//
//        _holder.ItemId.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
//                if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                    //按下Enter後觸發撈取資料
//                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
//
//                    return true;
//                }
//                return false;
//            }
//        });

        _holder.FromBinId.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    //按下Enter後觸發撈取資料
                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    FetchRegisterData();
                    return true;
                }
                return false;
            }
        });
    }

    //撈取批號資料
    private void FetchRegisterData() {
        if (_holder.FromBinId.getText().toString().equals("")) {
            ShowMessage(R.string.WAPG008005, new ShowMessageEvent() {
                @Override
                public void onDismiss() {
                    _holder.FromBinId.requestFocus();
                }
            });
            return;
        }

        BModuleObject biObj = new BModuleObject();
        biObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWmsChangeBin");
        biObj.setModuleID("BIFetchSkuDet");
        biObj.setRequestID("BIFetchSkuDet");
        biObj.params = new Vector<>();
        //設定要撈取的條件Condition物件
        VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");

        List<Condition> condItemIds = new ArrayList<>();
        List<Condition> condBinIds = new ArrayList<>();
        List<Condition> condRegisterStatus = new ArrayList<>();

        HashMap<String, List<?>> dicCondition = new HashMap<>();

        if (!_holder.SkuNum.getText().toString().equals("")) {
            //設定要塞入的參數
            ParameterInfo paramSkuNum = new ParameterInfo();
            paramSkuNum.setParameterID(BIWMSFetchInfoParam.Filter);
            paramSkuNum.setParameterValue(String.format("WHERE SKU_NUM = '%s'", _holder.SkuNum.getText().toString().trim()));
            biObj.params.add(paramSkuNum);
        }

        if (!_holder.ItemId.getText().toString().equals("")) {
            //設定要塞入的參數
            Condition condItemId = new Condition();
            condItemId.setAliasTable("SI");
            condItemId.setColumnName("ITEM_ID");
            condItemId.setDataType("string");
            condItemId.setValue(_holder.ItemId.getText().toString().trim());
            condItemIds.add(condItemId);
            dicCondition.put("ITEM_ID", condItemIds);
        }

        //設定要塞入的參數
        Condition condBinId = new Condition();
        condBinId.setAliasTable("SB");
        condBinId.setColumnName("BIN_ID");
        condBinId.setDataType("string");
        condBinId.setValue(_holder.FromBinId.getText().toString().trim());
        condBinIds.add(condBinId);
        dicCondition.put("BIN_ID", condBinIds);

        //設定要塞入的參數
        Condition condStatus = new Condition();
        condStatus.setAliasTable("SR");
        condStatus.setColumnName("REGISTER_STATUS");
        condStatus.setDataType("string");
        condStatus.setValue("Available");
        condRegisterStatus.add(condStatus);
        dicCondition.put("REGISTER_STATUS", condRegisterStatus);

        //序列化參數
        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vKey, vVal);
        String serializeString = msdl.generateFinalCode(dicCondition);
        ParameterInfo param = new ParameterInfo();
        param.setParameterID(BIWMSFetchInfoParam.Condition);
        param.setNetParameterValue(serializeString);

        biObj.params.add(param);

        CallBIModule(biObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    //目前的表格名稱為SWMS_INVENTORY應該要修改為SWMS_REGISTER
                    DataTable dtReg = bModuleReturn.getReturnJsonTables().get("BIFetchSkuDet").get("SWMS_REGISTER");
                    if (dtReg == null || dtReg.Rows.size() == 0) {
                        //WAPG008001    批號[%s]狀態為[Available]並未有資料
                        //Object[] args = new Object[1];
                        //args[0] = _holder.RegisterId.getText().toString();
                        //ShowMessage(R.string.WAPG008001, args);
                        ShowMessage(R.string.WAPG008003);
                        return;
                    }

                    boolean isAdd;
                    for (DataRow drNew : dtReg.Rows) {
                        isAdd = true;
                        for (DataRow drTemp : _registerTable.Rows) {
                            if (drTemp.getValue("ITEM_ID").toString().equals(drNew.getValue("ITEM_ID").toString())
                                    && drTemp.getValue("REGISTER_ID").toString().equals(drNew.getValue("REGISTER_ID").toString())
                                    && drTemp.getValue("BIN_ID").toString().equals(drNew.getValue("BIN_ID").toString())) {
                                isAdd = false;
                                break;
                            }
                        }
                        if (isAdd)
                            _registerTable.Rows.add(drNew);
                    }

                    _holder.SkuNum.getText().clear();
                    _holder.ItemId.getText().clear();
                    _holder.FromBinId.getText().clear();
                    _holder.SkuNum.requestFocus();

                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    GoodChangeBinGridAdapter adapter = new GoodChangeBinGridAdapter(_registerTable, inflater);
                    _holder.RegisterData.setAdapter(adapter);

                }
            }
        });
    }

    //執行儲位交換
    private void ExecuteChangingBin(final String registerId, final String binId, final String orgBinId) {
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BWmsChangeBin");
        bmObj.setModuleID("");
        bmObj.setRequestID("BChangeBin");
        bmObj.params = new Vector<>();

        VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create(VirtualClass.VirtualClassType.String);

        HashMap<String, String> dicRegisterChangeBin = new HashMap<>();
        HashMap<String, String> dicRegisterOrgBin = new HashMap<>();
        dicRegisterChangeBin.put(registerId, binId);
        dicRegisterOrgBin.put(registerId, orgBinId);

        MesSerializableDictionary msdl1 = new MesSerializableDictionary(vKey, vVal);
        String serializedString1 = msdl1.generateFinalCode(dicRegisterChangeBin);

        MesSerializableDictionary msdl2 = new MesSerializableDictionary(vKey, vVal);
        String serializedString2 = msdl2.generateFinalCode(dicRegisterOrgBin);

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BWmsChangeBinParam.RegisterChangeBins);
        param1.setNetParameterValue(serializedString1);
        bmObj.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BWmsChangeBinParam.TrxCmt);
        param2.setParameterValue("");
        bmObj.params.add(param2);

        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BWmsChangeBinParam.RegisterOrgBins);
        param3.setNetParameterValue(serializedString2);
        bmObj.params.add(param3);

        CallBModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    //FetchRegisterData();
                    //WAPG008002    作業成功
                    ShowMessage(R.string.WAPG008002);

                    //成功清除改變那筆資料
                    for (DataRow dr : _registerTable.Rows) {
                        if (dr.getValue("REGISTER_ID").toString().equals(registerId)
                                && dr.getValue("BIN_ID").toString().equals(orgBinId)) {
                            _registerTable.Rows.remove(dr);
                            break;
                        }
                    }

                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    GoodChangeBinGridAdapter adapter = new GoodChangeBinGridAdapter(_registerTable, inflater);
                    _holder.RegisterData.setAdapter(adapter);
                }
            }
        });
    }

    //執行儲位交換
    private void ExecuteChangingBin(final String registerId, final String binId, final String orgBinId, final String storageId, final String itemId) {
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BWmsChangeBin");
        bmObj.setModuleID("");
        bmObj.setRequestID("BChangeBin");
        bmObj.params = new Vector<>();

        VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create(VirtualClass.VirtualClassType.String);

        HashMap<String, String> dicRegisterChangeBin = new HashMap<>();
        HashMap<String, String> dicRegisterOrgBin = new HashMap<>();
        dicRegisterChangeBin.put(registerId, binId);
        dicRegisterOrgBin.put(registerId, orgBinId);

        MesSerializableDictionary msdl1 = new MesSerializableDictionary(vKey, vVal);
        String serializedString1 = msdl1.generateFinalCode(dicRegisterChangeBin);

        MesSerializableDictionary msdl2 = new MesSerializableDictionary(vKey, vVal);
        String serializedString2 = msdl2.generateFinalCode(dicRegisterOrgBin);

        // region 儲存盤點狀態檢查物件
        // 20220805 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中
        List<CheckCountObj> lstChkCountObj = new ArrayList<>();
        // region 儲存盤點狀態檢查物件
        CheckCountObj chkCountObjFromBin = new CheckCountObj(); // FROM_BIN
        chkCountObjFromBin.setStorageId(storageId);
        chkCountObjFromBin.setItemId(itemId);
        chkCountObjFromBin.setBinId(orgBinId);
        lstChkCountObj.add(chkCountObjFromBin);
        CheckCountObj chkCountObjToBin = new CheckCountObj(); // TO_BIN
        chkCountObjToBin.setStorageId(storageId);
        chkCountObjToBin.setItemId(itemId);
        chkCountObjToBin.setBinId(binId);
        lstChkCountObj.add(chkCountObjToBin);
        // endregion

        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.CheckCountObj", "bmWMS.Library.Param");
        MList mListEnum = new MList(vListEnum);
        String strCheckCountObj = mListEnum.generateFinalCode(lstChkCountObj);

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BWmsChangeBinParam.RegisterChangeBins);
        param1.setNetParameterValue(serializedString1);
        bmObj.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BWmsChangeBinParam.TrxCmt);
        param2.setParameterValue("");
        bmObj.params.add(param2);

        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BWmsChangeBinParam.RegisterOrgBins);
        param3.setNetParameterValue(serializedString2);
        bmObj.params.add(param3);

        ParameterInfo paramExecuteChkStock = new ParameterInfo();
        paramExecuteChkStock.setParameterID(BWmsChangeBinParam.ExecuteCheckStock); // 20220805 Add by Ikea 是否執行盤點檢查
        paramExecuteChkStock.setNetParameterValue2("true");
        bmObj.params.add(paramExecuteChkStock);

        ParameterInfo paramChkCountObj = new ParameterInfo(); // 20220805 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中
        paramChkCountObj.setParameterID(BWmsChangeBinParam.CheckCountObj);
        paramChkCountObj.setNetParameterValue(strCheckCountObj);
        bmObj.params.add(paramChkCountObj);

        CallBModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    //FetchRegisterData();
                    //WAPG008002    作業成功
                    ShowMessage(R.string.WAPG008002);

                    //成功清除改變那筆資料
                    for (DataRow dr : _registerTable.Rows) {
                        if (dr.getValue("REGISTER_ID").toString().equals(registerId)
                                && dr.getValue("BIN_ID").toString().equals(orgBinId)) {
                            _registerTable.Rows.remove(dr);
                            break;
                        }
                    }

                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    GoodChangeBinGridAdapter adapter = new GoodChangeBinGridAdapter(_registerTable, inflater);
                    _holder.RegisterData.setAdapter(adapter);
                }
            }
        });
    }

    //點選Grid當下觸發的事件
    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            final TextView tvSkuLevel = view.findViewById(R.id.tvSkuLevel);
            final TextView tvSkuNum = view.findViewById(R.id.tvSkuNum);
            final TextView tvItemId = view.findViewById(R.id.tvItemId);
            final TextView tvItemName = view.findViewById(R.id.tvItemName);
            final TextView tvQty = view.findViewById(R.id.tvQty);

            final String itemId = _registerTable.Rows.get(position).getValue("ITEM_ID").toString();
            _selectedRegister = _registerTable.Rows.get(position).getValue("REGISTER_ID").toString();
            _selectedRegisterBin = _registerTable.Rows.get(position).getValue("BIN_ID").toString();

            //確認是否為IQC儲位，IQC儲位不可調整儲位
            BModuleObject bmObj = new BModuleObject();
            bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
            bmObj.setModuleID("BIFetchBin");
            bmObj.setRequestID("BIFetchBin");
            bmObj.params = new Vector<ParameterInfo>();

            String strBin = _registerTable.Rows.get(position).getValue("BIN_ID").toString();
            String strFilter = String.format(" AND B.BIN_ID = '%s'", strBin);

            ParameterInfo param1 = new ParameterInfo();
            param1.setParameterID(BIWMSFetchInfoParam.Filter);
            param1.setParameterValue(strFilter);
            bmObj.params.add(param1);

            CallBIModule(bmObj, new WebAPIClientEvent() {
                @Override
                public void onPostBack(BModuleReturn bModuleReturn) {
                    if(CheckBModuleReturnInfo(bModuleReturn))
                    {
                        final DataTable dt = bModuleReturn.getReturnJsonTables().get("BIFetchBin").get("BIN");
                        String strBinType = dt.Rows.get(0).getValue("BIN_TYPE").toString();
                        if(strBinType.equals("IQC"))
                        {
                            //WAPG008006    IQC儲位不可調整
                            ShowMessage(R.string.WAPG008006);
                            return;
                        }
                        else
                        {
                            AlertDialog.Builder binChangedDialog = new AlertDialog.Builder(GoodChangeBinActivity.this);
                            View dialogVIew = LayoutInflater.from(GoodChangeBinActivity.this).inflate(R.layout.style_wms_dialog_change_bin, null);
                            binChangedDialog.setTitle("");
                            binChangedDialog.setView(dialogVIew);

                            final AlertDialog dialog = binChangedDialog.create();
                            dialog.show();

                            TextView tvBinSkuLevel = dialog.findViewById(R.id.tvBinSkuLevel);
                            tvBinSkuLevel.setText(tvSkuLevel.getText());

                            TextView tvBinSkuNum = dialog.findViewById(R.id.tvBinSkuNum);
                            tvBinSkuNum.setText(tvSkuNum.getText());

                            TextView tvBinItemId = dialog.findViewById(R.id.tvBinItemId);
                            tvBinItemId.setText(tvItemId.getText());

                            TextView tvBinItemName = dialog.findViewById(R.id.tvBinItemName);
                            tvBinItemName.setText(tvItemName.getText());

                            TextView tvBinQty = dialog.findViewById(R.id.tvBinQty);
                            tvBinQty.setText(tvQty.getText());

                            Button bConfirm = dialog.findViewById(R.id.btnConfirm);
                            bConfirm.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    EditText etBinId = dialog.findViewById(R.id.etChangeBinId);
                                    String storageId = dt.Rows.get(0).getValue("STORAGE_ID").toString();
                                    ExecuteChangingBin(_selectedRegister, etBinId.getText().toString().trim(), _selectedRegisterBin, storageId, itemId);

                                    dialog.dismiss();

                                }
                            });

                            Button bCancel = dialog.findViewById(R.id.btnCancel);
                            bCancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });
                        }
                    }
                }
            });
        }
    };

    //Scan SkuNum QRCode Button Click
    private View.OnClickListener ibtnSkuNumQRScanOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            IntentIntegrator integrator = new IntentIntegrator(GoodChangeBinActivity.this);
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
}
