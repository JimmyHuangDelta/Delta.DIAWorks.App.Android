package com.delta.android.WMS.Client;

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
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionaryList;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Client.GridAdapter.WarehouseQueryRegistorGridAdapter;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class WarehouseQueryRegisterActivity extends BaseFlowActivity {

    // Function ID = WAPG025

    private final int BIN_QRSCAN_REQUEST_CODE = 1;
    private final int ITEM_QRSCAN_REQUEST_CODE = 2;
    private final int LOT_QRSCAN_REQUEST_CODE = 3;

    private CheckBox chkStorage, chkBin, chkItem, chkLot;
    private Spinner cmbStorage;
    private EditText etBin, etItem, etLot;
    private ImageButton ibtnBinQRScan, ibtnItemQRScan, ibtnLotQRScan, ibtnSearch;
    private TextView tvResultCountVal;
    private ListView lvQueryResult;
    private Button btnRefresh;

    // 存放倉庫 IDNAME
    private ArrayList<String> lstStorage;

    // [Key, Value] = [IDNAME, STORAGE_ID]
    private HashMap<String, String> mapStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_warehouse_query_register);

        setInitData();

        getStorage();

        setListeners();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "您取消了掃描", Toast.LENGTH_SHORT).show();
            } else {
                switch (requestCode) {
                    case BIN_QRSCAN_REQUEST_CODE:
                        etBin.setText(result.getContents().trim());
                        break;
                    case ITEM_QRSCAN_REQUEST_CODE:
                        etItem.setText(result.getContents().trim());
                        break;
                    case LOT_QRSCAN_REQUEST_CODE:
                        etLot.setText(result.getContents().trim());
                        break;
                    default:
                        break;
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setInitData() {

        chkStorage = findViewById(R.id.chkStorage);
        chkBin = findViewById(R.id.chkBin);
        chkItem = findViewById(R.id.chkItem);
        chkLot = findViewById(R.id.chkLot);
        cmbStorage = findViewById(R.id.cmbStorage);
        etBin = findViewById(R.id.etBin);
        etItem = findViewById(R.id.etItem);
        etLot = findViewById(R.id.etLot);
        ibtnBinQRScan = findViewById(R.id.ibtnBinQRScan);
        ibtnItemQRScan = findViewById(R.id.ibtnItemQRScan);
        ibtnLotQRScan = findViewById(R.id.ibtnLotQRScan);
        ibtnSearch = findViewById(R.id.ibtnSearch);
        tvResultCountVal = findViewById(R.id.tvResultCountVal);
        lvQueryResult = findViewById(R.id.lvQueryResult);
        btnRefresh = findViewById(R.id.btnRefresh);

        // Spinner, EditText 預設為 disable
        cmbStorage.setEnabled(false);
        ibtnBinQRScan.setEnabled(false);
        ibtnItemQRScan.setEnabled(false);
        ibtnLotQRScan.setEnabled(false);
        etBin.setEnabled(false);
        etItem.setEnabled(false);
        etLot.setEnabled(false);

        // CheckBox 預設不勾選
        chkStorage.setChecked(false);
        chkBin.setChecked(false);
        chkItem.setChecked(false);
        chkLot.setChecked(false);

    }

    private void getStorage() {

        // Call BIModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchStorage");
        bmObj.setRequestID("BIFetchStorage");

        CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {

                if(!CheckBModuleReturnInfo(bModuleReturn)) return;

                lstStorage = new ArrayList<>();
                mapStorage = new HashMap<>();
                String strSelectStorage = getResString(getResources().getString(R.string.SELECT_STORAGE));
                lstStorage.add(strSelectStorage);
                DataTable dt = bModuleReturn.getReturnJsonTables().get("BIFetchStorage").get("STORAGE");
                int pos = 0;
                for (DataRow dr : dt.Rows) {
                    lstStorage.add(pos, dr.getValue("IDNAME").toString().trim());
                    mapStorage.put(dr.getValue("IDNAME").toString().trim(), dr.get("STORAGE_ID").toString().trim());
                    pos++;
                }
                SimpleArrayAdapter adapter = new SimpleArrayAdapter<>(WarehouseQueryRegisterActivity.this, android.R.layout.simple_spinner_dropdown_item, lstStorage);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                cmbStorage.setAdapter(adapter);
                cmbStorage.setSelection(lstStorage.size() - 1, true);
            }
        });
    }

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

    private void getQueryResult() {

        String strStorageId = "";

        if (mapStorage.size() > 0)
            strStorageId = mapStorage.get(cmbStorage.getSelectedItem().toString().toUpperCase().trim());

        String strBinId = etBin.getText().toString().toUpperCase().trim();
        String strItemId = etItem.getText().toString().toUpperCase().trim();
        String strLotId = etLot.getText().toString().toUpperCase().trim();

        // region Check
        if (!chkStorage.isChecked() && !chkBin.isChecked() && !chkItem.isChecked() && !chkLot.isChecked()) {
            ShowMessage(R.string.WAPG025006); // WAPG025006    至少選擇一個查詢條件
            return;
        }
        if (chkStorage.isChecked() && cmbStorage.getSelectedItemPosition() == (lstStorage.size() - 1)) {
            ShowMessage(R.string.WAPG025001); // WAPG025001    請選擇倉庫
            return;
        }
        if (chkBin.isChecked() && strBinId.equals("")) {
            ShowMessage(R.string.WAPG025002); // WAPG025002    請輸入儲位
            return;
        }
        if (chkItem.isChecked() && strItemId.equals("")) {
            ShowMessage(R.string.WAPG025003); // WAPG025003    請輸入物料
            return;
        }
        if (chkLot.isChecked() && strLotId.equals("")) {
            ShowMessage(R.string.WAPG025004); // WAPG025004    請輸入批號
            return;
        }
        // endregion

        // region Set Parameter
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIWareHouseQueryRegistor");
        bmObj.setRequestID("BIWareHouseQueryRegistor");
        bmObj.params = new Vector<ParameterInfo>();

        HashMap<String, List<?>> mapCondition = new HashMap<>();
        ArrayList<Condition> lstCondition;

        if (chkStorage.isChecked()) {
            // STORAGE_ID
            Condition cdtStorageId = new Condition();
            cdtStorageId.setAliasTable("S");
            cdtStorageId.setColumnName("STORAGE_ID");
            cdtStorageId.setValue(strStorageId);
            cdtStorageId.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
            lstCondition = new ArrayList<>();
            lstCondition.add(cdtStorageId);
            mapCondition.put(cdtStorageId.getColumnName(), lstCondition);
        }

        if (chkBin.isChecked()) {
            // BIN_ID
            Condition cdtBinId = new Condition();
            cdtBinId.setAliasTable("B");
            cdtBinId.setColumnName("BIN_ID");
            cdtBinId.setValue(strBinId);
            cdtBinId.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
            lstCondition = new ArrayList<>();
            lstCondition.add(cdtBinId);
            mapCondition.put(cdtBinId.getColumnName(), lstCondition);
        }

        if (chkItem.isChecked()) {
            // ITEM_ID
            Condition cdtItemId = new Condition();
            cdtItemId.setAliasTable("IT");
            cdtItemId.setColumnName("ITEM_ID");
            cdtItemId.setValue(strItemId);
            cdtItemId.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
            lstCondition = new ArrayList<>();
            lstCondition.add(cdtItemId);
            mapCondition.put(cdtItemId.getColumnName(), lstCondition);
        }

        if(chkLot.isChecked()) {
            ParameterInfo param1 = new ParameterInfo();
            param1.setParameterID(BIWMSFetchInfoParam.Filter);
            param1.setParameterValue("AND R.REGISTER_ID LIKE '" + strLotId + "'");
            bmObj.params.add(param1);
        }

        if (mapCondition.size() > 0) {
            VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);

            // Unicom.Uniworks.BModule.WMS.INV.Parameter.ParameterObj.Condition, bmWMS.INV.Param
            VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
            MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vkey, vVal);
            String strCon = msdl.generateFinalCode(mapCondition);

            ParameterInfo param2 = new ParameterInfo();
            param2.setParameterID(BIWMSFetchInfoParam.Condition);
            param2.setNetParameterValue(strCon);
            bmObj.params.add(param2);
        }
        // endregion

        // region Call BIModule. Get ListView and Set Count
        CallBIModule(bmObj, new WebAPIClientEvent() {

            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {

                if(!CheckBModuleReturnInfo(bModuleReturn)) return;

                DataTable dtMst = bModuleReturn.getReturnJsonTables().get("BIWareHouseQueryRegistor").get("GridMaster");

                /* // 220705 Ikea 目前不在WMS的scope
                DataTable dtSmtlInventory = bModuleReturn.getReturnJsonTables().get("BIWareHouseQueryRegistor").get("GridSmtlInventory");

                if (!chkLot.isChecked()) {
                    for (DataRow dr: dtSmtlInventory.Rows) {
                        DataRow drNew = dtMst.newRow();
                        drNew.setValue("STORAGE_ID", dr.getValue("STORAGE_ID"));
                        drNew.setValue("STORAGE_NAME", dr.getValue("STORAGE_NAME"));
                        drNew.setValue("BIN_ID", dr.getValue("BIN_ID"));
                        drNew.setValue("BIN_NAME", dr.getValue("BIN_NAME"));
                        drNew.setValue("ITEM_ID", dr.getValue("ITEM_ID"));
                        drNew.setValue("ITEM_NAME", dr.getValue("ITEM_NAME"));
                        drNew.setValue("QTY", dr.getValue("QTY"));
                        drNew.setValue("EXP_DATE", "");
                        drNew.setValue("DISPLAY_REGISTER_ID", dr.getValue("DISPLAY_REGISTER_ID"));
                        dtMst.Rows.add(drNew);
                    }
                }
                */

                if (dtMst == null || dtMst.Rows.size() <= 0) {
                    ShowMessage(R.string.WAPG025005); // WAPG025005    查詢無資料
                    return;
                }

                // 查詢結果顯示於 ListView
                getListView(dtMst);

                // 設定查詢筆數
                tvResultCountVal.setText(String.valueOf(dtMst.Rows.size()));
            }
        });
    }

    private void getListView (DataTable dt) {

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        WarehouseQueryRegistorGridAdapter adapter = new WarehouseQueryRegistorGridAdapter(dt, inflater);
        lvQueryResult.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    private void setListeners() {
        chkStorage.setOnClickListener(checkedChange);
        chkBin.setOnClickListener(checkedChange);
        chkItem.setOnClickListener(checkedChange);
        chkLot.setOnClickListener(checkedChange);
        etBin.setOnKeyListener(fetchDataByEnter);
        etItem.setOnKeyListener(fetchDataByEnter);
        etLot.setOnKeyListener(fetchDataByEnter);
        ibtnBinQRScan.setOnClickListener(inputDataByQRScan);
        ibtnItemQRScan.setOnClickListener(inputDataByQRScan);
        ibtnLotQRScan.setOnClickListener(inputDataByQRScan);
        ibtnSearch.setOnClickListener(fetchDataByButton);
        btnRefresh.setOnClickListener(refreshView);
    }

    private View.OnClickListener checkedChange = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.chkStorage:
                    if(chkStorage.isChecked()) {
                        cmbStorage.setEnabled(true);
                    } else {
                        cmbStorage.setSelection(lstStorage.size()-1);
                        cmbStorage.setEnabled(false);
                    }
                    break;
                case R.id.chkBin:
                    if (chkBin.isChecked()) {
                        etBin.setEnabled(true);
                        ibtnBinQRScan.setEnabled(true);
                    } else {
                        etBin.setText("");
                        etBin.setEnabled(false);
                        ibtnBinQRScan.setEnabled(false);
                    }
                    break;
                case R.id.chkItem:
                    if (chkItem.isChecked()) {
                        etItem.setEnabled(true);
                        ibtnItemQRScan.setEnabled(true);
                    } else {
                        etItem.setText("");
                        etItem.setEnabled(false);
                        ibtnItemQRScan.setEnabled(false);
                    }
                    break;
                case R.id.chkLot:
                    if (chkLot.isChecked()) {
                        etLot.setEnabled(true);
                        ibtnLotQRScan.setEnabled(true);
                    } else {
                        etLot.setText("");
                        etLot.setEnabled(false);
                        ibtnLotQRScan.setEnabled(false);
                    }
                    break;
            }
        }
    };

    private View.OnClickListener fetchDataByButton = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            // 清空查詢結果
            tvResultCountVal.setText("0");
            lvQueryResult.setAdapter(null);

            getQueryResult();
        }
    };

    private View.OnKeyListener fetchDataByEnter = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            // 只有按下 Enter 才會反應
            if (keyCode != KeyEvent.KEYCODE_ENTER)
                return false;
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                // 清空查詢結果
                tvResultCountVal.setText("0");
                lvQueryResult.setAdapter(null);

                getQueryResult();
                return true;
            }
            return false;
        }
    };

    private View.OnClickListener inputDataByQRScan = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            IntentIntegrator integrator = new IntentIntegrator(WarehouseQueryRegisterActivity.this);
            // 設置要掃描的條碼類型，ONE_D_CODE_TYPES：一維碼，QR_CODE_TYPES-二維碼
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("BarCode Scan"); //底部的提示文字
            integrator.setCameraId(0); //前置(1)或者後置(0)攝像頭
            integrator.setBeepEnabled(false); //掃描成功的「嗶嗶」聲
            integrator.setBarcodeImageEnabled(false); //是否保留掃碼成功時候的截圖
            integrator.setCaptureActivity(ScanActivity.class);
            switch (view.getId()) {
                case R.id.ibtnBinQRScan:
                    integrator.setRequestCode(BIN_QRSCAN_REQUEST_CODE);
                    break;
                case R.id.ibtnItemQRScan:
                    integrator.setRequestCode(ITEM_QRSCAN_REQUEST_CODE);
                    break;
                case R.id.ibtnLotQRScan:
                    integrator.setRequestCode(LOT_QRSCAN_REQUEST_CODE);
                    break;
                default:
                    integrator.setRequestCode(0);
                    break;
            }
            integrator.initiateScan();
        }
    };

    private View.OnClickListener refreshView = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            // 重新取得Storage資料, EditText 清空資料, 並 disable
            getStorage();
            etBin.setText("");
            etItem.setText("");
            etLot.setText("");
            cmbStorage.setEnabled(false);
            etBin.setEnabled(false);
            etItem.setEnabled(false);
            etLot.setEnabled(false);
            ibtnBinQRScan.setEnabled(false);
            ibtnItemQRScan.setEnabled(false);
            ibtnLotQRScan.setEnabled(false);

            // CheckBox 取消勾選
            chkStorage.setChecked(false);
            chkBin.setChecked(false);
            chkItem.setChecked(false);
            chkLot.setChecked(false);

            // 清空 ListView
            lvQueryResult.setAdapter(null);

            // 查詢數量設為 0
            tvResultCountVal.setText("0");
        }
    };
}

// Error Code
// WAPG025001 請選擇倉庫
// WAPG025002 請選擇儲位
// WAPG025003 請選擇物料
// WAPG025004 請輸入批號
// WAPG025005 查詢無資料
// WAPG025006 至少選擇一個查詢條件
