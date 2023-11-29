package com.delta.android.WMS.Client;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.Activity.ScanActivity;
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
import com.delta.android.WMS.Client.GridAdapter.CarrierQueryGridAdapter;
import com.delta.android.WMS.Client.GridAdapter.CarrierQueryLayoutGridAdapter;
import com.delta.android.WMS.Client.GridAdapter.CarrierQueryRegisterGridAdapter;
import com.delta.android.WMS.Param.BIFetchProcessSheetParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class CarrierQueryActivity extends BaseFlowActivity {

    // Function ID = WAGP026

    private String strCarrierId;
    private String strCarrierTypeKey;
    private String strCarrierKindId;

    private CheckBox chkCarrierId;
    private CheckBox chkCarrierType;
    private CheckBox chkCarrierKind;
    private ImageButton ibtnCarrierIdQRScan;
    private EditText etCarrierId;
    private Spinner cmbCarrierType;
    private Spinner cmbCarrierKind;
    private ImageButton ibtnSearch;

    private ListView lvCarrierQueryResult;
    private ListView lvCarrierLayout;
    private ListView lvCarrierRegister;
    private TextView tvCarrierCountVal;
    private TextView tvLayoutCountVal;
    private TextView tvRegCountVal;

    private Button btnRefresh;

    // 存放載具類型至Spinner
    //private ArrayList<String> lstCarrierType;
    //private HashMap<String, String> mapCarrierType;

    // 存放載具種類至Spinner
    //private ArrayList<String> lstCarrierKind;
    //private HashMap<String, String> mapCarrierKind;

    private DataTable dtCarrier = null;
    private DataTable dtLayout = null;
    private DataTable selectedCarrierLayout = null;
    private HashMap<String, DataTable> mapCarrierLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_carrier_query);

        setInitData();

        setListeners();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "您取消了掃描", Toast.LENGTH_SHORT).show();
            } else {
                etCarrierId.setText(result.getContents().trim());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setInitData() {

        strCarrierId = "";
        strCarrierTypeKey = "";
        strCarrierKindId = "";

        chkCarrierId = findViewById(R.id.chkCarrierId);
        chkCarrierType = findViewById(R.id.chkCarrierType);
        chkCarrierKind = findViewById(R.id.chkCarrierKind);
        ibtnCarrierIdQRScan = findViewById(R.id.ibtnCarrierIdQRScan);
        etCarrierId = findViewById(R.id.etCarrierId);
        cmbCarrierType = findViewById(R.id.cmbCarrierType);
        cmbCarrierKind = findViewById(R.id.cmbCarrierKind);

        ibtnSearch = findViewById(R.id.ibtnSearch);
        tvCarrierCountVal = findViewById(R.id.tvCarrierCountVal);
        tvLayoutCountVal = findViewById(R.id.tvLayoutCountVal);
        tvRegCountVal = findViewById(R.id.tvRegCountVal);
        lvCarrierQueryResult = findViewById(R.id.lvCarrierQueryResult);
        lvCarrierLayout = findViewById(R.id.lvLayout);
        lvCarrierRegister = findViewById(R.id.lvRegister);
        btnRefresh = findViewById(R.id.btnRefresh);

        // Spinner, EditText 預設為 disable
        etCarrierId.setEnabled(false);
        ibtnCarrierIdQRScan.setEnabled(false);
        cmbCarrierType.setEnabled(false);
        cmbCarrierKind.setEnabled(false);

        // CheckBox 預設不勾選
        chkCarrierId.setChecked(false);
        chkCarrierType.setChecked(false);
        chkCarrierKind.setChecked(false);

        // Spinner 取得載具類型及載具種類
        getCarrierTypeAndCarrierKind();
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

    private void setListeners() {

        chkCarrierId.setOnClickListener(checkedChange);
        chkCarrierType.setOnClickListener(checkedChange);
        chkCarrierKind.setOnClickListener(checkedChange);

        lvCarrierQueryResult.setOnItemClickListener(lvCarrierItemClick);
        lvCarrierLayout.setOnItemClickListener(lvCarrierLayoutItemClick);

        cmbCarrierType.setOnItemSelectedListener(selectedItem);
        cmbCarrierKind.setOnItemSelectedListener(selectedItem);
        etCarrierId.setOnKeyListener(fetchDataByEnter);
        ibtnCarrierIdQRScan.setOnClickListener(inputDataByQRScan);
        ibtnSearch.setOnClickListener(fetchDataByButton);

        btnRefresh.setOnClickListener(refreshView);

    }

    private void getCarrierTypeAndCarrierKind() {

        ArrayList<BModuleObject> lstBObj = new ArrayList<>();

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchCarrierType");
        bmObj.setRequestID("BIFetchCarrierType");
        lstBObj.add(bmObj);

        BModuleObject bmObj2 = new BModuleObject();
        bmObj2.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj2.setModuleID("BIFetchCarrierKind");
        bmObj2.setRequestID("BIFetchCarrierKind");
        lstBObj.add(bmObj2);

        CallBIModule(lstBObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {

                if(!CheckBModuleReturnInfo(bModuleReturn)) return;

                // region Spinner 取得載具類型
                //lstCarrierType = new ArrayList<>();
                //mapCarrierType = new HashMap<>();
                DataTable dtCarrierType = bModuleReturn.getReturnJsonTables().get("BIFetchCarrierType").get("WMS_CARRIER_TYPE");
                List<? extends Map<String, Object>> lstCarrierType = (List<? extends Map<String, Object>>) dtCarrierType.toListHashMap();
                SimpleAdapter adapterCT = new SimpleAdapter(CarrierQueryActivity.this, lstCarrierType, android.R.layout.simple_spinner_item, new String[]{"CARRIER_TYPE_KEY", "CARRIER_TYPE_ID", "CARRIER_TYPE_NAME", "IDNAME"}, new int[]{0, 0, 0, android.R.id.text1});
                adapterCT.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                cmbCarrierType.setAdapter(adapterCT);
                cmbCarrierType.setSelection(0, true);
                // endregion

                // region Spinner 取得載具種類
                //lstCarrierKind = new ArrayList<>();
                //mapCarrierKind = new HashMap<>();
                DataTable dtCarrierKind = bModuleReturn.getReturnJsonTables().get("BIFetchCarrierKind").get("WMS_CARRIER_KIND");
                List<? extends Map<String, Object>> lstCarrierKind = (List<? extends Map<String, Object>>) dtCarrierKind.toListHashMap();
                SimpleAdapter adapterCK = new SimpleAdapter(CarrierQueryActivity.this, lstCarrierKind, android.R.layout.simple_spinner_item, new String[]{"COMBOBOX_KEY", "DATA_ID", "DATA_NAME", "IDNAME"}, new int[]{0, 0, 0, android.R.id.text1});
                adapterCK.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                cmbCarrierKind.setAdapter(adapterCK);
                cmbCarrierKind.setSelection(0, true);
//                int posCK = 0;
//                for (DataRow dr : dtCarrierKind.Rows) {
//                    lstCarrierKind.add(posCK, dr.getValue("IDNAME").toString().trim());
//                    mapCarrierKind.put(dr.getValue("IDNAME").toString().trim(), dr.get("DATA_ID").toString().trim());
//                    posCK++;
//                }
//
//                String strSelectCarrierKind = getResString(getResources().getString(R.string.SELECT_CARRIER_KIND));
//                lstCarrierKind.add(strSelectCarrierKind);


                // endregion
            }
        });
    }

    private void getQueryResult() {

        strCarrierId = etCarrierId.getText().toString().toUpperCase().trim();
        //String strCarrierType = mapCarrierType.get(cmbCarrierType.getSelectedItem().toString());
        //String strCarrierKind = mapCarrierKind.get(cmbCarrierKind.getSelectedItem().toString());

        // region Check
        if (chkCarrierId.isChecked() && strCarrierId.equals("")) {
            ShowMessage(R.string.WAPG026001); // WAPG026001    請選擇載具代碼
            return;
        }

//        if (chkCarrierType.isChecked() && cmbCarrierType.getSelectedItemPosition() == (lstCarrierType.size() - 1)) {
//            ShowMessage(R.string.WAPG026002); // WAPG026002    請選擇載具類型
//            return;
//        }
//
//        if (chkCarrierKind.isChecked() && cmbCarrierKind.getSelectedItemPosition() == (lstCarrierKind.size() - 1)) {
//            ShowMessage(R.string.WAPG026003); // WAPG026003    請選擇載具種類
//            return;
//        }
        // endregion

        // region Set Parameter

        HashMap<String, List<?>> mapCondition = new HashMap<>();
        ArrayList<Condition> lstCondition;

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchCarrier");
        bmObj.setRequestID("BIFetchCarrier");
        bmObj.params = new Vector<ParameterInfo>();

        if(chkCarrierId.isChecked()) {
            // CARRIER_ID
            ParameterInfo param1 = new ParameterInfo();
            param1.setParameterID(BIWMSFetchInfoParam.Filter);
            param1.setParameterValue("AND C.CARRIER_ID LIKE '" + strCarrierId + "'");
            bmObj.params.add(param1);
        }

        if (chkCarrierType.isChecked()) {
            // CARRIER_TYPE
            Condition cdtCarrierType = new Condition();
            cdtCarrierType.setAliasTable("CT");
            cdtCarrierType.setColumnName("CARRIER_TYPE_KEY");
            cdtCarrierType.setValue(strCarrierTypeKey);
            cdtCarrierType.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
            lstCondition = new ArrayList<>();
            lstCondition.add(cdtCarrierType);
            mapCondition.put(cdtCarrierType.getColumnName(), lstCondition);
        }

        if (chkCarrierKind.isChecked()) {
            // CARRIER_KIND
            Condition cdtCarrierKind = new Condition();
            cdtCarrierKind.setAliasTable("WC");
            cdtCarrierKind.setColumnName("CARRIER_KIND");
            cdtCarrierKind.setValue(strCarrierKindId);
            cdtCarrierKind.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
            lstCondition = new ArrayList<>();
            lstCondition.add(cdtCarrierKind);
            mapCondition.put(cdtCarrierKind.getColumnName(), lstCondition);
        }

        if (mapCondition.size() > 0) {
            VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
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

                dtCarrier = bModuleReturn.getReturnJsonTables().get("BIFetchCarrier").get("Carrier");
                dtLayout = bModuleReturn.getReturnJsonTables().get("BIFetchCarrier").get("Layout");

                if (dtCarrier == null || dtCarrier.Rows.size() <= 0) {
                    ShowMessage(R.string.WAPG026004); // WAPG026004    查詢無資料
                    return;
                }

                mapCarrierLayout = getCarrierLayout(dtCarrier, dtLayout);

                // 查詢結果顯示於 ListView
                LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                CarrierQueryGridAdapter adapter = new CarrierQueryGridAdapter(dtCarrier, inflater);
                lvCarrierQueryResult.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                // 設定查詢筆數
                tvCarrierCountVal.setText(String.valueOf(dtCarrier.Rows.size()));
            }
        });
        // endregion
    }

    private View.OnClickListener checkedChange = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.chkCarrierId:
                    if (chkCarrierId.isChecked()) {
                        etCarrierId.setEnabled(true);
                        ibtnCarrierIdQRScan.setEnabled(true);

                    } else {
                        etCarrierId.setText("");
                        etCarrierId.setEnabled(false);
                        ibtnCarrierIdQRScan.setEnabled(false);
                    }
                    break;
                case R.id.chkCarrierType:
                    if(chkCarrierType.isChecked()) {
                        cmbCarrierType.setEnabled(true);
                    } else {
                        cmbCarrierType.setEnabled(false);
                    }
                    break;
                case R.id.chkCarrierKind:
                    if(chkCarrierKind.isChecked()) {
                        cmbCarrierKind.setEnabled(true);
                    } else {
                        cmbCarrierKind.setEnabled(false);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private View.OnClickListener inputDataByQRScan = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            IntentIntegrator integrator = new IntentIntegrator(CarrierQueryActivity.this);
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

    private View.OnClickListener fetchDataByButton = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            // 清空查詢結果區 (ListView, TextView)
            resetQueryResult();

            // 依條件查詢
            getQueryResult();
        }
    };

    private AdapterView.OnItemSelectedListener selectedItem = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            //String strSheetType = mapIdNameSheetTypeKey.get(spinnerSheetType.getSelectedItem().toString());
            switch (parent.getId()) {
                case R.id.cmbCarrierType:
                    Map<String, String> typeMap = (Map<String, String>)parent.getItemAtPosition(position);
                    strCarrierTypeKey = typeMap.get("CARRIER_TYPE_KEY");
                    break;
                case R.id.cmbCarrierKind:
                    Map<String, String> kindMap = (Map<String, String>)parent.getItemAtPosition(position);
                    strCarrierKindId = kindMap.get("DATA_ID");
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            return;
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

                // 清空查詢結果區 (ListView, TextView)
                resetQueryResult();

                // 依條件查詢
                getQueryResult();

                return true;
            }
            return false;
        }
    };

    private View.OnClickListener refreshView = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            // 重新取得載具類型及載具種類, EditText 清空資料, 並 disable
            getCarrierTypeAndCarrierKind();
            etCarrierId.setText("");
            ibtnCarrierIdQRScan.setEnabled(false);
            etCarrierId.setEnabled(false);
            cmbCarrierType.setEnabled(false);
            cmbCarrierKind.setEnabled(false);

            // CheckBox 取消勾選
            chkCarrierId.setChecked(false);
            chkCarrierType.setChecked(false);
            chkCarrierKind.setChecked(false);

            // 清空查詢結果區 (ListView, TextView)
            resetQueryResult();
        }
    };

    private AdapterView.OnItemClickListener lvCarrierItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            String strCarrierId = dtCarrier.Rows.get(position).get("CARRIER_ID").toString();
            selectedCarrierLayout = mapCarrierLayout.get(strCarrierId);

            // 查詢結果顯示於 ListView
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            CarrierQueryLayoutGridAdapter adapter = new CarrierQueryLayoutGridAdapter(selectedCarrierLayout, inflater);
            lvCarrierLayout.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            // 設定查詢筆數
            tvLayoutCountVal.setText(String.valueOf(selectedCarrierLayout.Rows.size()));
        }
    };

    private AdapterView.OnItemClickListener lvCarrierLayoutItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            final String strCarrierId = selectedCarrierLayout.Rows.get(position).get("CARRIER_ID").toString();
            final String strBlockId = selectedCarrierLayout.Rows.get(position).get("BLOCK_ALIAS_ID").toString();

            BModuleObject bmObj = new BModuleObject();
            bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
            bmObj.setModuleID("BIFetchCarrierRegister");
            bmObj.setRequestID("BIFetchCarrierRegister");
            bmObj.params = new Vector<ParameterInfo>();

            HashMap<String, List<?>> mapCondition = new HashMap<>();
            ArrayList<Condition> lstCondition;

            Condition cdtBlockId = new Condition();
            cdtBlockId.setAliasTable("R");
            cdtBlockId.setColumnName("REGISTER_BATCH_POSITION");
            cdtBlockId.setValue(strBlockId);
            cdtBlockId.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
            lstCondition = new ArrayList<>();
            lstCondition.add(cdtBlockId);
            mapCondition.put(cdtBlockId.getColumnName(), lstCondition);

            Condition cdtCarrierId = new Condition();
            cdtCarrierId.setAliasTable("R");
            cdtCarrierId.setColumnName("REGISTER_BATCH_ID");
            cdtCarrierId.setValue(strCarrierId);
            cdtCarrierId.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
            lstCondition = new ArrayList<>();
            lstCondition.add(cdtCarrierId);
            mapCondition.put(cdtCarrierId.getColumnName(), lstCondition);

            if (mapCondition.size() > 0) {
                VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
                VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
                MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vkey, vVal);
                String strCon = msdl.generateFinalCode(mapCondition);

                ParameterInfo param = new ParameterInfo();
                param.setParameterID(BIWMSFetchInfoParam.Condition);
                param.setNetParameterValue(strCon);
                bmObj.params.add(param);
            }

            CallBIModule(bmObj, new WebAPIClientEvent() {

                @Override
                public void onPostBack(BModuleReturn bModuleReturn) {

                    if(!CheckBModuleReturnInfo(bModuleReturn)) return;

                    DataTable dtCarrierReg = bModuleReturn.getReturnJsonTables().get("BIFetchCarrierRegister").get("CarrierReg");

                    if (dtCarrierReg == null || dtCarrierReg.Rows.size() <= 0) {
                        ShowMessage(R.string.WAPG026005); // WAPG026005    查詢無承載物料
                        lvCarrierRegister.setAdapter(null);
                        tvRegCountVal.setText("0");
                        return;
                    }

                    // 查詢結果顯示於 ListView
                    LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                    CarrierQueryRegisterGridAdapter adapter = new CarrierQueryRegisterGridAdapter(strCarrierId, strBlockId, dtCarrierReg, inflater);
                    lvCarrierRegister.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    // 設定查詢筆數
                    tvCarrierCountVal.setText(String.valueOf(dtCarrierReg.Rows.size()));
                }
            });
        }
    };

    private void resetQueryResult() {

        // 查詢筆數設為0
        tvCarrierCountVal.setText("0");
        tvLayoutCountVal.setText("0");
        tvRegCountVal.setText("0");

        // 清空ListView
        lvCarrierQueryResult.setAdapter(null);
        lvCarrierLayout.setAdapter(null);
        lvCarrierRegister.setAdapter(null);

    }

    private HashMap<String, DataTable> getCarrierLayout(DataTable dtCarrier, DataTable dtLayout) {
        HashMap<String, DataTable> mapLayout = new HashMap<>();
        for(int pos = 0; pos < dtCarrier.Rows.size(); pos++) {
            String strCarrierId = dtCarrier.getValue(pos, "CARRIER_ID").toString();
            DataTable dtNew = new DataTable();
            for(DataRow dr: dtLayout.Rows) {
                if(dr.getValue("CARRIER_ID").toString().equals(strCarrierId)) {
                    DataRow drNew = new DataRow(dtNew);
                    drNew.setValue("CARRIER_ID", dr.getValue("CARRIER_ID"));
                    drNew.setValue("BLOCK_ALIAS_ID", dr.getValue("BLOCK_ALIAS_ID"));
                    drNew.setValue("CARRIED_QTY", dr.getValue("CARRIED_QTY"));
                    dtNew.Rows.add(drNew);
                }
            }
            mapLayout.put(strCarrierId, dtNew);
        }
        return mapLayout;
    }
}

// Error Code
// WAPG026001   請選擇載具代碼
// WAPG026002   請選擇載具類型
// WAPG026003   請選擇載具種類
// WAPG026004   查詢無載具資料
// WAPG026005   查詢無承載物料