package com.delta.android.WMS.Client;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.Activity.ScanActivity;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.GenerateJsonNetString.MList;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Client.Fragment.DeliveryNotePickFragment;
import com.delta.android.WMS.Client.Fragment.DeliveryNoteUnpickFragment;
import com.delta.android.WMS.Param.BIDeliveryNotePickingPortalParam;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Vector;

public class DeliveryNotePickingDetailNewActivity extends BaseFlowActivity {

    private DeliveryNoteUnpickFragment deliveryNoteUnpickFragment = new DeliveryNoteUnpickFragment();; // 第一個頁籤，Position = 0
    private DeliveryNotePickFragment deliveryNotePickFragment = new DeliveryNotePickFragment(); // 第二個頁籤，Position = 1
    private int currentPage = 0; // 記錄目前所在頁籤的位置
    //HashMap<String, String>mapSheet = new HashMap<>(); //紀錄揀料單對應的原始單據
    private TabLayout tabLayout;
    private EditText etItemId;
    private ImageButton ibtnItemIdQRScan;

    // private variable
    public static int requestCode = 1; //需要接收回傳資訊時使用
    private final int ITEM_ID_QRSCAN_REQUEST_CODE = 11;
    private DataTable sheetMstTable, sheetDetTable;
    private DataTable showTable; // tab上顯示的detail資訊
    private DataTable pickDetTable;// 全部的揀貨資訊
    private DataTable alreadyPickedData;// 某一筆detail的揀貨資訊
    private DataTable dtPickedFinish, dtNeedToPick;
    public ArrayList<String> DNIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_note_picking_detail_new);

        DNIDs = getIntent().getStringArrayListExtra("DNIDs");
        sheetMstTable = (DataTable) getIntent().getSerializableExtra("DNMst");
        sheetDetTable = (DataTable) getIntent().getSerializableExtra("DNDet");

        etItemId = findViewById(R.id.etDnDetItemId);
        ibtnItemIdQRScan = findViewById(R.id.ibtnItemIdQRScan);

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                fragmentChange(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        ibtnItemIdQRScan.setOnClickListener(ItemQRScan);

        getPickDet(false);

        etItemId.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode != KeyEvent.KEYCODE_ENTER) return false;

                if (event.getAction() == KeyEvent.ACTION_UP) {

                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    if (currentPage == 0) { // 未揀頁籤

                        if (dtNeedToPick == null || dtNeedToPick.Rows.size() == 0)
                            return false;

                        DataTable dtFilter = new DataTable();
                        String filterId = etItemId.getText().toString().trim();

                        if (filterId.length() <= 0)
                            dtFilter = dtNeedToPick;
                        else {
                            for (DataRow dr : dtNeedToPick.Rows) {
                                if (dr.getValue("ITEM_ID").toString().equals(filterId)) {
                                    dtFilter.Rows.add(dr);
                                }
                            }
                        }

                        DeliveryNoteUnpickFragment unpickFragment = (DeliveryNoteUnpickFragment) getSupportFragmentManager().findFragmentByTag("DeliveryUnpick");
                        unpickFragment.getUnpickTable(dtFilter);

                    } else { // 已揀頁籤
                        if (dtPickedFinish == null || dtPickedFinish.Rows.size() == 0)
                            return false;

                        DataTable dtFilter = new DataTable();
                        String filterId = etItemId.getText().toString().trim();

                        if (filterId.length() <= 0)
                            dtFilter = dtPickedFinish;
                        else {
                            for (DataRow dr : dtPickedFinish.Rows) {
                                if (dr.getValue("ITEM_ID").toString().equals(filterId)) {
                                    dtFilter.Rows.add(dr);
                                }
                            }
                        }

                        DeliveryNotePickFragment pickFragment = (DeliveryNotePickFragment) getSupportFragmentManager().findFragmentByTag("DeliveryPick");
                        pickFragment.getPickTable(dtFilter);
                    }

                    return true;
                }

                return false;
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);

        IntentResult result = IntentIntegrator.parseActivityResult(resultCode, dataIntent);
        if (result != null) {
            if (result.getContents() == null) {
                if (requestCode == 100) {
                    // 回傳資料
                    // 取得已揀資訊放tab
                    this.getPickDet(true);
                } else if (requestCode == ITEM_ID_QRSCAN_REQUEST_CODE) {
                    Toast.makeText(this, getResources().getText(R.string.QRCODE_SCAN_CANCEL).toString(), Toast.LENGTH_SHORT).show();
                }
            } else {
                switch (requestCode) {
                    case ITEM_ID_QRSCAN_REQUEST_CODE:
                        etItemId.setText(result.getContents().trim());
                        break;
                    default:
                        break;
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, dataIntent);
        }
    }

    private void getPickDet(final boolean afterPicking) {

        BModuleObject bmObjSetData = new BModuleObject();
        bmObjSetData.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIDeliveryNotePickingPortal");
        bmObjSetData.setModuleID("BIFetchDeliveryNotePickingSheetDet");
        bmObjSetData.setRequestID("BIFetchDeliveryNotePickingSheetDet");
        bmObjSetData.params = new Vector<>();

        VirtualClass vList = VirtualClass.create(VirtualClass.VirtualClassType.String);
        MList mList = new MList(vList);
        String strLstSheetId= mList.generateFinalCode(DNIDs);
        ParameterInfo param = new ParameterInfo();
        param.setParameterID(BIDeliveryNotePickingPortalParam.SheetId);
        param.setNetParameterValue(strLstSheetId);
        bmObjSetData.params.add(param);

        CallBIModule(bmObjSetData, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {

                    dtPickedFinish = bModuleReturn.getReturnJsonTables().get("BIFetchDeliveryNotePickingSheetDet").get("dtPickedFinish");
                    dtNeedToPick = bModuleReturn.getReturnJsonTables().get("BIFetchDeliveryNotePickingSheetDet").get("dtNeedToPick");

                    if (afterPicking == false) {
                        // 將 Mst 的資料傳到下一頁的第一個頁籤
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("dtPickedFinish", dtPickedFinish);
                        bundle.putSerializable("dtNeedToPick", dtNeedToPick);

                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.add(R.id.fragmentDeliveryPick, deliveryNoteUnpickFragment, "DeliveryUnpick");
                        fragmentTransaction.add(R.id.fragmentDeliveryPick, deliveryNotePickFragment, "DeliveryPick");
                        deliveryNoteUnpickFragment.setArguments(bundle);
                        deliveryNotePickFragment.setArguments(bundle);
                        fragmentTransaction.hide(deliveryNotePickFragment);
                        fragmentTransaction.commit();

                    } else {

                        DeliveryNoteUnpickFragment unpickFragment = (DeliveryNoteUnpickFragment) getSupportFragmentManager().findFragmentByTag("DeliveryUnpick");
                        unpickFragment.getUnpickTable(dtNeedToPick);

                        DeliveryNotePickFragment pickFragment = (DeliveryNotePickFragment) getSupportFragmentManager().findFragmentByTag("DeliveryPick");
                        pickFragment.getPickTable(dtPickedFinish);

                    }
                }
            }
        });
    }

    private View.OnClickListener ItemQRScan = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            IntentIntegrator integrator = new IntentIntegrator(DeliveryNotePickingDetailNewActivity.this);
            // 設置要掃描的條碼類型，ONE_D_CODE_TYPES：一維碼，QR_CODE_TYPES-二維碼
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("BarCode Scan"); //底部的提示文字
            integrator.setCameraId(0); //前置(1)或者後置(0)攝像頭
            integrator.setBeepEnabled(false); //掃描成功的「嗶嗶」聲
            integrator.setBarcodeImageEnabled(false); //是否保留掃碼成功時候的截圖
            integrator.setCaptureActivity(ScanActivity.class);
            integrator.setRequestCode(ITEM_ID_QRSCAN_REQUEST_CODE);
            integrator.initiateScan();
        }
    };

    private void fragmentChange(int position){

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch (currentPage){

            case 0:
                fragmentTransaction.hide(deliveryNoteUnpickFragment);
                break;

            case 1:
                fragmentTransaction.hide(deliveryNotePickFragment);
                break;
        }

        switch (position){

            case 0:
                fragmentTransaction.show(deliveryNoteUnpickFragment);
                break;

            case 1:
                fragmentTransaction.show(deliveryNotePickFragment);
                break;
        }

        fragmentTransaction.commit();

        currentPage = position;
    }

}
