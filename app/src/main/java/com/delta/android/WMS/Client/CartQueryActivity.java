package com.delta.android.WMS.Client;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.Activity.ScanActivity;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Client.GridAdapter.CartQueryGridAdapter;
import com.delta.android.WMS.Param.BICartMaintainParam;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Vector;

public class CartQueryActivity extends BaseFlowActivity {

    // Function ID = WAPG024

    private final int CART_ID_QRSCAN_REQUEST_CODE = 1;
    private final int VEHICLE_ID_QRSCAN_REQUEST_CODE = 2;
    private final int LOCATION_ID_QRSCAN_REQUEST_CODE = 3;

    private CheckBox chkCartId;
    private CheckBox chkVehicleId;
    private CheckBox chkLocationId;
    private EditText etCartId;
    private EditText etVehicleId;
    private EditText etLocationId;
    private ImageButton ibtnCardIdQRScan, ibtnVehicleIdQRScan, ibtntLocationIdQRScan;
    private ImageButton ibtnSearch;
    private TextView tvResultCountVal;
    private ListView lvQueryResult;
    private Button btnRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_cart_query);

        setInitData();

        setListeners();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, getResources().getText(R.string.QRCODE_SCAN_CANCEL).toString(), Toast.LENGTH_SHORT).show();
            } else {
                switch (requestCode) {
                    case CART_ID_QRSCAN_REQUEST_CODE:
                        etCartId.setText(result.getContents().trim().toUpperCase());
                        break;
                    case VEHICLE_ID_QRSCAN_REQUEST_CODE:
                        etVehicleId.setText(result.getContents().trim().toUpperCase());
                        break;
                    case LOCATION_ID_QRSCAN_REQUEST_CODE:
                        etLocationId.setText(result.getContents().trim().toUpperCase());
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

        chkCartId = findViewById(R.id.chkCartId);
        chkVehicleId = findViewById(R.id.chkVehicleId);
        chkLocationId = findViewById(R.id.chkLocationId);
        etCartId = findViewById(R.id.etCartId);
        etVehicleId = findViewById(R.id.etVehicleId);
        etLocationId = findViewById(R.id.etLocationId);
        ibtnCardIdQRScan = findViewById(R.id.ibtnCartIdQRScan);
        ibtnVehicleIdQRScan = findViewById(R.id.ibtnVehicleIdQRScan);
        ibtntLocationIdQRScan = findViewById(R.id.ibtnLocationIdQRScan);
        ibtnSearch = findViewById(R.id.ibtnSearch);
        tvResultCountVal = findViewById(R.id.tvResultCountVal);
        lvQueryResult = findViewById(R.id.lvQueryResult);
        btnRefresh = findViewById(R.id.btnRefresh);

        // EditText, CheckBox 預設為 disable
        etCartId.setEnabled(false);
        etVehicleId.setEnabled(false);
        etLocationId.setEnabled(false);
        ibtnCardIdQRScan.setEnabled(false);
        ibtnVehicleIdQRScan.setEnabled(false);
        ibtntLocationIdQRScan.setEnabled(false);
        chkCartId.setChecked(false);
        chkVehicleId.setChecked(false);
        chkLocationId.setChecked(false);

    }

    private void setListeners() {
        chkCartId.setOnClickListener(checkedChange);
        chkVehicleId.setOnClickListener(checkedChange);
        chkLocationId.setOnClickListener(checkedChange);
        etCartId.setOnKeyListener(fetchCartByEnter);
        etVehicleId.setOnKeyListener(fetchCartByEnter);
        etLocationId.setOnKeyListener(fetchCartByEnter);
        ibtnCardIdQRScan.setOnClickListener(inputDataByQRScan);
        ibtnVehicleIdQRScan.setOnClickListener(inputDataByQRScan);
        ibtntLocationIdQRScan.setOnClickListener(inputDataByQRScan);
        ibtnSearch.setOnClickListener(fetchCartByButton);
        btnRefresh.setOnClickListener(refreshView);
    }

    private void fetchCart() {

        String strCartId = etCartId.getText().toString().toUpperCase().trim();
        String strVehicleId = etVehicleId.getText().toString().toUpperCase().trim();
        String strLocationId = etLocationId.getText().toString().toUpperCase().trim();

        // region Check
        if (chkCartId.isChecked() && strCartId.equals("")) {
            ShowMessage(R.string.WAPG024001); // WAPG024001    請輸入料架代碼
            return;
        }
        if (chkVehicleId.isChecked() && strVehicleId.equals("")) {
            ShowMessage(R.string.WAPG024002); // WAPG024002    請輸入AGV車代碼
            return;
        }
        if (chkLocationId.isChecked() && strLocationId.equals("")) {
            ShowMessage(R.string.WAPG024003); // WAPG024003    請輸入位置代碼
            return;
        }
        // endregion

        // region Set Parameter
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.CAR.BICartMaintain");
        bmObj.setModuleID("BIFetchCart");
        bmObj.setRequestID("BIFetchCart");
        bmObj.params = new Vector<ParameterInfo>();

        if (chkCartId.isChecked()) {
            ParameterInfo param1 = new ParameterInfo();
            param1.setParameterID(BICartMaintainParam.CartID);
            param1.setParameterValue(strCartId);
            bmObj.params.add(param1);
        }

        if (chkVehicleId.isChecked()) {
            ParameterInfo param2 = new ParameterInfo();
            param2.setParameterID(BICartMaintainParam.VehicleID);
            param2.setParameterValue(strVehicleId);
            bmObj.params.add(param2);
        }

        if (chkLocationId.isChecked()) {
            ParameterInfo param3 = new ParameterInfo();
            param3.setParameterID(BICartMaintainParam.Location);
            param3.setParameterValue(strLocationId);
            bmObj.params.add(param3);
        }
        // endregion

        // region Call BIModule. GetListView and Set Count
        CallBIModule(bmObj, new WebAPIClientEvent() {

            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {

                if(!CheckBModuleReturnInfo(bModuleReturn)) return;

                DataTable dtResult = bModuleReturn.getReturnJsonTables().get("BIFetchCart").get("SWMS_CART");

                if (dtResult == null || dtResult.Rows.size() <= 0) {
                    ShowMessage(R.string.WAPG024004); // WAPG0024004    查詢無資料
                    lvQueryResult.setAdapter(null); // 查詢無資料將 ListView 清空
                    tvResultCountVal.setText("0");
                    return;
                }

                // 查詢結果顯示於 ListView
                getListView(dtResult);

                // 設定查詢筆數
                tvResultCountVal.setText(String.valueOf(dtResult.Rows.size()));
            }
        });
        // endregion
    }

    private void getListView (DataTable dt) {

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        CartQueryGridAdapter  adapter = new CartQueryGridAdapter(dt, inflater);
        lvQueryResult.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    private View.OnClickListener checkedChange = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.chkCartId:
                    if (chkCartId.isChecked()) {
                        etCartId.setEnabled(true);
                        ibtnCardIdQRScan.setEnabled(true);
                    } else {
                        etCartId.setText("");
                        etCartId.setEnabled(false);
                        ibtnCardIdQRScan.setEnabled(false);
                    }
                    break;
                case R.id.chkVehicleId:
                    if (chkVehicleId.isChecked()) {
                        etVehicleId.setEnabled(true);
                        ibtnVehicleIdQRScan.setEnabled(true);
                    } else {
                        etVehicleId.setText("");
                        etVehicleId.setEnabled(false);
                        ibtnVehicleIdQRScan.setEnabled(false);
                    }
                    break;
                case R.id.chkLocationId:
                    if (chkLocationId.isChecked()) {
                        etLocationId.setEnabled(true);
                        ibtntLocationIdQRScan.setEnabled(true);
                    } else {
                        etLocationId.setText("");
                        etLocationId.setEnabled(false);
                        ibtntLocationIdQRScan.setEnabled(false);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private View.OnKeyListener fetchCartByEnter = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            // 只有按下 Enter 才會反應
            if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                fetchCart();
                return true;
            }
            return false;
        }
    };

    private View.OnClickListener inputDataByQRScan = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            IntentIntegrator integrator = new IntentIntegrator(CartQueryActivity.this);
            // 設置要掃描的條碼類型，ONE_D_CODE_TYPES：一維碼，QR_CODE_TYPES-二維碼
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("BarCode Scan"); //底部的提示文字
            integrator.setCameraId(0); //前置(1)或者後置(0)攝像頭
            integrator.setBeepEnabled(false); //掃描成功的「嗶嗶」聲
            integrator.setBarcodeImageEnabled(false); //是否保留掃碼成功時候的截圖
            integrator.setCaptureActivity(ScanActivity.class);
            switch (view.getId()) {
                case R.id.ibtnCartIdQRScan:
                    integrator.setRequestCode(CART_ID_QRSCAN_REQUEST_CODE);
                    break;
                case R.id.ibtnVehicleIdQRScan:
                    integrator.setRequestCode(VEHICLE_ID_QRSCAN_REQUEST_CODE);
                    break;
                case R.id.ibtnLocationIdQRScan:
                    integrator.setRequestCode(LOCATION_ID_QRSCAN_REQUEST_CODE);
                    break;
                default:
                    integrator.setRequestCode(0);
                    break;
            }
            integrator.initiateScan();
        }
    };

    private View.OnClickListener fetchCartByButton = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            fetchCart();
        }
    };

    private View.OnClickListener refreshView = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            // EditText 清空資料並 disable
            etCartId.setText("");
            etVehicleId.setText("");
            etLocationId.setText("");
            etCartId.setEnabled(false);
            etVehicleId.setEnabled(false);
            etLocationId.setEnabled(false);
            ibtnCardIdQRScan.setEnabled(false);
            ibtnVehicleIdQRScan.setEnabled(false);
            ibtntLocationIdQRScan.setEnabled(false);

            // CheckBox 取消勾選
            chkCartId.setChecked(false);
            chkVehicleId.setChecked(false);
            chkLocationId.setChecked(false);

            // 清空 ListView
            lvQueryResult.setAdapter(null);

            // 查詢數量設為 0
            tvResultCountVal.setText("0");

        }
    };
}

// ERROR CODE
// WCRG024001    請輸入料架代碼!
// WCRG024002    請輸入AGV車!
// WCRG024003    請選擇位置!
// WCRG024004    查詢無資料!
