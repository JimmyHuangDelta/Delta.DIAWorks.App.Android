package com.delta.android.WMS.Client.Fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.Core.Activity.ScanActivity;
import com.delta.android.Core.Activity.ShowMessageEvent;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.GenerateJsonNetString.MesClass;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Client.GridAdapter.WarehouseStorageDetAdapter;
import com.delta.android.WMS.Client.GridAdapter.WarehouseStorageLotAdapter;
import com.delta.android.WMS.Client.WarehouseStorageDetailNewActivity;
import com.delta.android.WMS.Client.WarehouseStorageReceivedActivity;
import com.delta.android.WMS.Param.BIGoodReceiptReceivePortalParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.BIWarehouseStoragePortalParam;
import com.delta.android.WMS.Param.BWarehouseStorageWithPackingInfoParam;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;


public class WarehouseStorageDataFragment extends Fragment {

    private DataTable dtMst, dtDet, dtWvrDet, dtWvrDetGroup;
    private DataTable dtInputWvrDet, dtInputWvrDetGroup;
    private String sheetPolicyId;

    private Spinner cmbStorage;
    private Spinner cmbSkuLevel;
    private ImageButton ibtnSkuNumQRScan;
    private EditText etSkuNum;
    private Button btnClearSkuNum;
    private ListView lvWvDetLotData;
    private Button btnWarehouseStorage;

    private List<? extends Map<String, Object>> lstSkuLevel;
    private List<? extends Map<String, Object>> lstStorage;
    private String wvId;
    private String skuLevelId;
    private String storageId;
    private List<String> lstExistSkuNum = null;

    // region -- 鏡頭掃描 Request Code/Lens scan Request Code --
    private final int SKU_NUM_QRSCAN_REQUEST_CODE = 2701;
    // endregion

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_warehouse_storage_data, container, false);

        // region 取得前一頁的資訊 (從WarehouseStorageDetailNewActivity)/Get previous page information (from WarehouseStorageDetailNewActivity)
        sheetPolicyId = (String) getFragmentManager().findFragmentByTag("WarehouseStorageData").getArguments().getSerializable("sheetPolicyId");
        dtMst = (DataTable)getFragmentManager().findFragmentByTag("WarehouseStorageData").getArguments().getSerializable("dtMst");
        dtDet = (DataTable)getFragmentManager().findFragmentByTag("WarehouseStorageData").getArguments().getSerializable("dtDet");
        dtWvrDet = (DataTable)getFragmentManager().findFragmentByTag("WarehouseStorageData").getArguments().getSerializable("dtWvrDetWithPackingInfo");
        dtWvrDetGroup = (DataTable)getFragmentManager().findFragmentByTag("WarehouseStorageData").getArguments().getSerializable("dtWvrDetGroup");
        // endregion

        // region 建立空Table(儲存使用者輸入帶入庫的資料)/Create an empty Table (storing the data entered by the user into the database)
        dtInputWvrDetGroup = new DataTable();
        dtInputWvrDetGroup.getColumns().addAll(dtWvrDetGroup.getColumns());

        dtInputWvrDet = new DataTable();
        dtInputWvrDet.getColumns().addAll(dtWvrDet.getColumns());
        //endregion

        wvId = dtMst.Rows.get(0).getValue("MTL_SHEET_ID").toString();

        initWidget(view);
        getSpinnerData();
        showListView();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText((WarehouseStorageDetailNewActivity)getActivity(), getResources().getText(R.string.QRCODE_SCAN_CANCEL).toString(), Toast.LENGTH_SHORT).show();
            } else {
                switch (requestCode) {

                    case SKU_NUM_QRSCAN_REQUEST_CODE:
                        etSkuNum.setText(result.getContents().trim());
                        getWwvCont(storageId, skuLevelId, result.getContents().trim());
                        break;

                    default:
                        break;
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initWidget(View view) {
        cmbStorage = view.findViewById(R.id.cmbStorage);
        cmbSkuLevel = view.findViewById(R.id.cmbSkuLevel);
        etSkuNum = view.findViewById(R.id.etSkuNum);

        btnClearSkuNum = view.findViewById(R.id.btnClearSkuNum);
        ibtnSkuNumQRScan = view.findViewById(R.id.ibtnSkuNumQRScan);
        lvWvDetLotData = view.findViewById(R.id.lvWvDetLotData);
        btnWarehouseStorage = view.findViewById(R.id.btnWarehouseStorage);
        etSkuNum.setOnKeyListener(onKeySkuNum);
        cmbSkuLevel.setOnItemSelectedListener(onSelectSkuLevel);
        cmbStorage.setOnItemSelectedListener(onSelectStorage);

        btnClearSkuNum.setOnClickListener(onClickClearSkuNum);
        lvWvDetLotData.setOnItemLongClickListener(onClickLotData);
        ibtnSkuNumQRScan.setOnClickListener(onClickSkuNumQRCode);
        btnWarehouseStorage.setOnClickListener(onClickSave);
    }

    private void getSpinnerData() {

        List<BModuleObject> bmObjs = new ArrayList<BModuleObject>();

        // SkuLevel
        BModuleObject bmObj2 = new BModuleObject();
        bmObj2.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSPackingInfo");
        bmObj2.setModuleID("BIFetchSkuLevel");
        bmObj2.setRequestID("BIFetchSkuLevel");
        bmObjs.add(bmObj2);

        //由單據取得Storage資訊/Obtain Storage information from the sheet
        List<String> lstStorageId = new ArrayList<>();
        for (DataRow drDet : dtDet.Rows) {
            String storageId = drDet.getValue("STORAGE_ID").toString();

            if (!lstStorageId.contains(storageId))
                lstStorageId.add(storageId);
        }

        if (lstStorageId.size() == 0) {
            // WAPG027039    請選擇倉庫
            ((WarehouseStorageDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG027039);
            clearData();
            cmbStorage.requestFocus();
            return;
        }

        String strConStorage = TextUtils.join("','", lstStorageId);

        //Storage
        BModuleObject bmObj3 = new BModuleObject();
        bmObj3.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj3.setModuleID("BIFetchStorage");
        bmObj3.setRequestID("BIFetchStorage");

        bmObj3.params = new Vector<ParameterInfo>();
        ParameterInfo param = new ParameterInfo();
        param.setParameterID(BIWMSFetchInfoParam.Filter);
        String strCon = String.format(" AND S.STORAGE_ID IN ('%s') ", strConStorage);
        param.setParameterValue(strCon);
        bmObj3.params.add(param);
        bmObjs.add(bmObj3);

        ((WarehouseStorageDetailNewActivity) getActivity()).CallBIModule(bmObjs, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (((WarehouseStorageDetailNewActivity) getActivity()).CheckBModuleReturnInfo(bModuleReturn)) {

                    DataTable dtSkuLevel = bModuleReturn.getReturnJsonTables().get("BIFetchSkuLevel").get("WmsSkuLevel");
                    DataTable dtStorage = bModuleReturn.getReturnJsonTables().get("BIFetchStorage").get("STORAGE");

                    //region -- 存貨層級/SkuLevel --
                    DataRow drDefaultItem = dtSkuLevel.newRow();
                    if (dtSkuLevel != null && dtSkuLevel.Rows.size() > 0)
                        dtSkuLevel.Rows.add(drDefaultItem);
                    lstSkuLevel = (List<? extends Map<String, Object>>) dtSkuLevel.toListHashMap();
                    SimpleAdapter adapterSkuLevel = new SimpleArrayAdapter<>(getContext(), lstSkuLevel, android.R.layout.simple_spinner_item, new String[]{"DATA_ID", "DATA_NAME", "IDNAME"}, new int[]{0, android.R.id.text1, 0});
                    adapterSkuLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    cmbSkuLevel.setAdapter(adapterSkuLevel);
                    cmbSkuLevel.setSelection(lstSkuLevel.size()-1, true);
                    // endregion

                    // region -- 倉庫/Storage --
                    if (dtStorage != null && dtStorage.Rows.size() > 0)
                        dtStorage.Rows.add(drDefaultItem);
                    lstStorage = (List<? extends Map<String, Object>>) dtStorage.toListHashMap();
                    SimpleAdapter adapterStorage = new SimpleArrayAdapter<>(getContext(), lstStorage, android.R.layout.simple_spinner_item, new String[]{"STORAGE_KEY", "STORAGE_ID", "IDNAME"}, new int[]{0, android.R.id.text1, 0});
                    adapterStorage.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    cmbStorage.setAdapter(adapterStorage);
                    if (dtStorage.Rows.size() > 2)
                        cmbStorage.setSelection(lstStorage.size()-1, true);
                    else if (dtStorage.Rows.size() == 2)
                        cmbStorage.setSelection(0, true);
                    // endregion

                }
            }
        });
    }

    /**
     * 顯示使用者刷入的待入庫資料
     * Display the data brought into the library by the user
     */
    private void showListView() {
        LayoutInflater lvInflater = getActivity().getLayoutInflater();
        WarehouseStorageLotAdapter adapter = new WarehouseStorageLotAdapter(dtInputWvrDetGroup, true, lvInflater);
        lvWvDetLotData.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private View.OnKeyListener onKeySkuNum = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent event) {

            if (keyCode != KeyEvent.KEYCODE_ENTER)
                return false;

            if (event.getAction() == KeyEvent.ACTION_UP) {

                InputMethodManager manager = (InputMethodManager) ((WarehouseStorageDetailNewActivity) getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                String skuNum = etSkuNum.getText().toString();
                getWwvCont(storageId, skuLevelId, skuNum);

                return true;
            }

            return false;
        }
    };

    private View.OnClickListener onClickSkuNumQRCode = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            IntentIntegrator integrator = IntentIntegrator.forSupportFragment(WarehouseStorageDataFragment.this);
            // 設置要掃描的條碼類型，ONE_D_CODE_TYPES：一維碼，QR_CODE_TYPES-二維碼/Set the barcode type to be scanned, ONE D_CODE TYPES: one-dimensional code, QR CODE TYPES-two-dimensional code
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("BarCode Scan"); //底部的提示文字/Hint text at the bottom
            integrator.setCameraId(0); //前置(1)或者後置(0)攝像頭/Front (1) or rear (0) camera
            integrator.setBeepEnabled(false); //掃描成功的「嗶嗶」聲/"Beep" sound for successful scan
            integrator.setBarcodeImageEnabled(false); //是否保留掃碼成功時候的截圖/Whether to keep the screenshot when the scan code is successful
            integrator.setCaptureActivity(ScanActivity.class);
            integrator.setRequestCode(SKU_NUM_QRSCAN_REQUEST_CODE);
            integrator.initiateScan();
        }
    };

    /**
     *  將使用者選擇的倉庫代碼、存貨層級(可不選)、存貨編號至中間表查詢，若有查到資料將回傳詳細資料及GroupBy存貨層級的資料
     *  Query the warehouse code, inventory level (optional), and inventory number selected by the user to the intermediate table. If the information is found, it will return detailed information and Group By inventory level information
     * @param storageId
     * @param skuLevel
     * @param skuNum
     */
    private void getWwvCont(final String storageId, String skuLevel, String skuNum) {

        if (storageId.length() <= 0) {

            // WAPG027039    請選擇倉庫
            ((WarehouseStorageDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG027039);
            clearWidgetData();
            cmbStorage.requestFocus();
            return;
        }

        if (skuNum.length() <= 0) {

            // WAPG027040    請輸入存貨編號
            ((WarehouseStorageDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG027040);
            clearWidgetData();
            etSkuNum.requestFocus();
            return;
        }

        if (lstExistSkuNum == null) {

            lstExistSkuNum = new ArrayList<>();

        } else if (lstExistSkuNum.contains(skuNum)){

            // WAPG027041    存貨編號已存在
            ((WarehouseStorageDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG027041);
            clearWidgetData();
            etSkuNum.requestFocus();
            return;
        }

        String actualQtyStatus = dtDet.Rows.get(0).getValue("ACTUAL_QTY_STATUS").toString();
        if (actualQtyStatus.equals("")) {

            Object[] args = new Object[1];
            args[0] = sheetPolicyId;

            //WAPG027013    單據類型「%s」未設定「單據Config設定」內的【實際數量的狀態】，請先設定!
            ((WarehouseStorageDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG027013, args);
            return;
        }

        BModuleObject biObj1 = new BModuleObject();
        biObj1.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIWarehouseStoragePortal");
        biObj1.setModuleID("BIGetWwvCont");
        biObj1.setRequestID("BIGetWwvCont");
        biObj1.params = new Vector<>();

        //Input Param
        ParameterInfo paramWvId = new ParameterInfo();
        paramWvId.setParameterID(BIWarehouseStoragePortalParam.WvId);
        paramWvId.setParameterValue(wvId);
        biObj1.params.add(paramWvId);

        if (skuLevel.length() > 0) {
            ParameterInfo paramSkuLevel = new ParameterInfo();
            paramSkuLevel.setParameterID(BIWarehouseStoragePortalParam.SkuLevel);
            paramSkuLevel.setParameterValue(skuLevel);
            biObj1.params.add(paramSkuLevel);
        }

        ParameterInfo paramSkuNum = new ParameterInfo();
        paramSkuNum.setParameterID(BIWarehouseStoragePortalParam.SkuNum);
        paramSkuNum.setParameterValue(skuNum);
        biObj1.params.add(paramSkuNum);

        ParameterInfo paramStg = new ParameterInfo();
        paramStg.setParameterID(BIWarehouseStoragePortalParam.StorageId);
        paramStg.setParameterValue(storageId);
        biObj1.params.add(paramStg);

        ParameterInfo paramActQtySts = new ParameterInfo();
        paramActQtySts.setParameterID(BIWarehouseStoragePortalParam.ActualQtyStatus);
        paramActQtySts.setParameterValue(actualQtyStatus);
        biObj1.params.add(paramActQtySts);

        BWarehouseStorageWithPackingInfoParam.WarehouseStorageWithPackingInfoMasterObj wvvObj = new BWarehouseStorageWithPackingInfoParam().new WarehouseStorageWithPackingInfoMasterObj().getWsSheet(dtMst.Rows.get(0), dtInputWvrDet);

        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.INV.Parameter.WarehouseStorageWithPackingInfoMasterObj", "bmWMS.INV.Param");
        MesClass mesClassEnum = new MesClass(vListEnum);
        String strWvMstObj = mesClassEnum.generateFinalCode(wvvObj);

        ParameterInfo paramWsObj = new ParameterInfo();
        paramWsObj.setParameterID(BIWarehouseStoragePortalParam.WsObj);
        paramWsObj.setNetParameterValue(strWvMstObj);
        biObj1.params.add(paramWsObj);

        ((WarehouseStorageDetailNewActivity)getActivity()).CallBIModule(biObj1, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {

                if (bModuleReturn.getAckError().size() > 0) {
                    clearWidgetData();
                    etSkuNum.requestFocus();
                }

                if (((WarehouseStorageDetailNewActivity)getActivity()).CheckBModuleReturnInfo(bModuleReturn)) {

                    String askChangeSkuLevel = "";

                    // 查詢發現還有外層包/The query found that there are outer packages
                    if (bModuleReturn.getReturnList().size() > 0) {

                        if (bModuleReturn.getReturnList().get("BIGetWwvCont").get(BIWarehouseStoragePortalParam.ChangeSkuLevel) != null) {
                            askChangeSkuLevel = bModuleReturn.getReturnList().get("BIGetWwvCont").get(BIWarehouseStoragePortalParam.ChangeSkuLevel).toString().replaceAll("\"", "");
                        }

                        if (!askChangeSkuLevel.equals("")) {

                            final String [] skuData = askChangeSkuLevel.split(":");
                            Object[] args = new Object[2];
                            args[0] = skuData[0];
                            args[1] = skuData[1];

                            String showMsg = String.format(((WarehouseStorageDetailNewActivity) getActivity()).getResString(getResources().getString(R.string.CHANGE_SKU_LEVEL)), args);

                            ((WarehouseStorageDetailNewActivity) getActivity()).ShowMessage(showMsg, new ShowMessageEvent() {
                                @Override
                                public void onDismiss() {

                                    // region -- 設置 cmbSkuLevel/Setting cmbSkuLevel --
                                    int levelPosition = 0;
                                    skuLevelId = skuData[0];

                                    for (Map<String, Object> levelMap : lstSkuLevel) {
                                        if (levelMap.get("DATA_ID").toString().equals(skuLevelId))
                                            break;
                                        levelPosition++;
                                    }

                                    if (levelPosition == lstSkuLevel.size()) {
                                        //WAPG027042   查無存貨層級[%s]!
                                        ((WarehouseStorageDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG027042);
                                        return;
                                    }

                                    SimpleAdapter adapterSkuLevel = new SimpleArrayAdapter<>(getContext(), lstSkuLevel, android.R.layout.simple_spinner_item, new String[]{"DATA_ID", "DATA_NAME", "IDNAME"}, new int[]{0, android.R.id.text1, 0});
                                    adapterSkuLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    cmbSkuLevel.setAdapter(adapterSkuLevel);
                                    cmbSkuLevel.setSelection(levelPosition, true);
                                    // endregion

                                    etSkuNum.setText(skuData[1]);

                                    getWwvCont(storageId, skuData[0], skuData[1]);
                                    return;
                                }
                            });
                        }
                    }

                    // 查詢成功回傳中間表可入庫資訊/If the query succeeds, return the storage information of the intermediate table
                    else if (bModuleReturn.getReturnJsonTables().size() > 0) {

                        DataTable dt = bModuleReturn.getReturnJsonTables().get("BIGetWwvCont").get("dtWwvCont");
                        DataTable dtWithSkuLevel = bModuleReturn.getReturnJsonTables().get("BIGetWwvCont").get("dtWwvWithSkuLevel");

                        for (DataRow dr : dt.Rows) {
                            DataRow drNew = dtInputWvrDet.newRow();
                            drNew.setValue("SEQ", dr.getValue("SEQ").toString());
                            drNew.setValue("WO_ID", dr.getValue("WO_ID").toString());
                            drNew.setValue("LOT_CODE", dr.getValue("LOT_CODE").toString());
                            drNew.setValue("STORAGE_ID", dr.getValue("STORAGE_ID").toString());
                            drNew.setValue("ITEM_ID", dr.getValue("ITEM_ID").toString());
                            drNew.setValue("LOT_ID", dr.getValue("ENTITY_ID").toString());
                            drNew.setValue("QTY", dr.getValue("QTY").toString());
                            //drNew.setValue("PARENT_LOT_ID", dr.getValue("PARENT_LOT_ID").toString());
                            //drNew.setValue("ROOT_LOT_ID", dr.getValue("ROOT_LOT_ID").toString());
                            drNew.setValue("CMT", dr.getValue("CMT").toString());
                            //drNew.setValue("EXPOSURE_TIME", dr.getValue("EXPOSURE_TIME").toString());
                            drNew.setValue("MFG_DATE", dr.getValue("MFG_DATE").toString());
                            drNew.setValue("EXP_DATE", dr.getValue("EXP_DATE").toString());
                            drNew.setValue("PALLET_ID", dr.getValue("PALLET_ID").toString());
                            drNew.setValue("BOX3_ID", dr.getValue("BOX3_ID").toString());
                            drNew.setValue("BOX2_ID", dr.getValue("BOX2_ID").toString());
                            drNew.setValue("BOX1_ID", dr.getValue("BOX1_ID").toString());
                            drNew.setValue("SKU_LEVEL", dr.get("SKU_LEVEL").toString());
                            drNew.setValue("SKU_NUM", dr.get("SKU_NUM").toString());
                            dtInputWvrDet.Rows.add(drNew);
                        }

                        for (DataRow dr : dtWithSkuLevel.Rows) {
                            DataRow drNew = dtInputWvrDetGroup.newRow();
                            //drNew.setValue("SEQ", dr.get("SEQ").toString());
                            drNew.setValue("WO_ID", dr.get("WO_ID").toString());
                            drNew.setValue("LOT_CODE", dr.get("LOT_CODE").toString());
                            drNew.setValue("STORAGE_ID", storageId);
                            drNew.setValue("ITEM_ID", dr.get("ITEM_ID").toString());
                            //drNew.setValue("LOT_ID", dr.get("LOT_ID").toString());
                            drNew.setValue("QTY", dr.get("QTY").toString());
                            drNew.setValue("MFG_DATE", dr.get("MFG_DATE").toString());
                            drNew.setValue("EXP_DATE", dr.get("EXP_DATE").toString());
                            drNew.setValue("SKU_LEVEL", dr.get("SKU_LEVEL").toString());
                            drNew.setValue("SKU_NUM", dr.get("SKU_NUM").toString());
                            dtInputWvrDetGroup.Rows.add(drNew);

                            lstExistSkuNum.add(dr.get("SKU_NUM").toString());
                        }
                    }

                    clearWidgetData();
                    etSkuNum.requestFocus();
                    showListView();

                }
            }
        });
    }

    /**
     * 批次入庫前的資料檢查
     * @return
     */
    private boolean chkBeforeSave() {

        if (dtInputWvrDet.Rows.size() == 0) {

            //WAPG027043   請新增待入庫資料
            ((WarehouseStorageDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG027043);
            return false;
        }

        // 將待入庫資料加入已暫存的入庫資料中/Add the data to be stored into the temporarily stored dat
        for(DataRow dr : dtInputWvrDet.Rows) {
            DataRow drNew = dtWvrDet.newRow();
            drNew.setValue("SEQ", dr.getValue("SEQ").toString());
            drNew.setValue("WO_ID", dr.getValue("WO_ID").toString());
            drNew.setValue("LOT_CODE", dr.getValue("LOT_CODE").toString());
            drNew.setValue("STORAGE_ID", dr.getValue("STORAGE_ID").toString());
            drNew.setValue("ITEM_ID", dr.getValue("ITEM_ID").toString());
            drNew.setValue("LOT_ID", dr.getValue("LOT_ID").toString());
            drNew.setValue("QTY", dr.getValue("QTY").toString());
            //drNew.setValue("PARENT_LOT_ID", dr.getValue("PARENT_LOT_ID").toString());
            //drNew.setValue("ROOT_LOT_ID", dr.getValue("ROOT_LOT_ID").toString());
            drNew.setValue("CMT", dr.getValue("CMT").toString());
            //drNew.setValue("EXPOSURE_TIME", dr.getValue("EXPOSURE_TIME").toString());
            drNew.setValue("MFG_DATE", dr.getValue("MFG_DATE").toString());
            drNew.setValue("EXP_DATE", dr.getValue("EXP_DATE").toString());
            drNew.setValue("PALLET_ID", dr.getValue("PALLET_ID").toString());
            drNew.setValue("BOX3_ID", dr.getValue("BOX3_ID").toString());
            drNew.setValue("BOX2_ID", dr.getValue("BOX2_ID").toString());
            drNew.setValue("BOX1_ID", dr.getValue("BOX1_ID").toString());
            dtWvrDet.Rows.add(drNew);
        }

        return true;
    }

    private View.OnClickListener onClickClearSkuNum = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            etSkuNum.setText("");
        }
    };

    private Spinner.OnItemSelectedListener onSelectSkuLevel = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
            skuLevelId = "";

            if (position != lstSkuLevel.size()-1) {
                Map<String, String> skuLevelMap = (Map<String, String>)parent.getItemAtPosition(position);
                skuLevelId = skuLevelMap.get("DATA_ID");
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private Spinner.OnItemSelectedListener onSelectStorage = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
            storageId = "";

            if (position != lstStorage.size()-1) {
                Map<String, String> lstorageMap = (Map<String, String>)parent.getItemAtPosition(position);
                storageId = lstorageMap.get("STORAGE_ID");
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private AdapterView.OnItemLongClickListener onClickLotData = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long id) {

            final DataRow drSelectedRow = dtInputWvrDetGroup.Rows.get(pos);

            //region 跳出詢問視窗
            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View deleteView = inflater.inflate(R.layout.activity_wms_warehouse_storage_stock_in_delete,null );
            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
            builder.setView(deleteView);

            final android.app.AlertDialog deleteDialog = builder.create();
            deleteDialog.setCancelable(false);//只允許按下dialog裡的按鍵才能關閉dialog
            deleteDialog.show();

            TextView tvCurrentSN = deleteDialog.findViewById(R.id.tvDialogMessage);
            // WAPG027044   是否刪除此存貨編號?
            String strErr = getResources().getString(R.string.WAPG027044);
            tvCurrentSN.setText(strErr);
            //endregion

            //region 確認清除
            Button btnDelete = deleteView.findViewById(R.id.btnYes);
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    for (Iterator<DataRow> iterator = dtInputWvrDet.Rows.iterator(); iterator.hasNext();) {
                        DataRow drDelete = iterator.next();
                        if (drDelete.getValue("SKU_NUM").toString().equals(drSelectedRow.getValue("SKU_NUM").toString())) {
                            iterator.remove();
                        }
                    }

                    lstExistSkuNum.remove(drSelectedRow.getValue("SKU_NUM").toString());

                    dtInputWvrDetGroup.Rows.remove(drSelectedRow);

                    showListView();

                    deleteDialog.dismiss();
                }
            });
            //endregion

            //region 取消清除
            Button btnCancel = deleteView.findViewById(R.id.btnNo);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteDialog.dismiss();
                }
            });
            //endregion

            return true;
        }
    };

    private View.OnClickListener onClickSave = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            if (!chkBeforeSave()) {
                return;
            }

            BModuleObject biObj1 = new BModuleObject();
            biObj1.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BWarehouseStorageWithPackingInfo");
            biObj1.setModuleID("BWarehouseStorageWithPackingInfo");
            biObj1.setRequestID("BWarehouseStorageWithPackingInfo");
            biObj1.params = new Vector<>();

            ParameterInfo param1 = new ParameterInfo();
            param1.setParameterID(BWarehouseStorageWithPackingInfoParam.SheetTypePolicyId);
            param1.setParameterValue(sheetPolicyId);
            biObj1.params.add(param1);

            ParameterInfo param2 = new ParameterInfo();
            param2.setParameterID(BWarehouseStorageWithPackingInfoParam.TrxType);
            param2.setParameterValue("WarehouseStorage");
            biObj1.params.add(param2);

            ParameterInfo param3 = new ParameterInfo();
            param3.setParameterID(BWarehouseStorageWithPackingInfoParam.SheetTypePolicyId);
            param3.setParameterValue(sheetPolicyId);
            biObj1.params.add(param3);

            BWarehouseStorageWithPackingInfoParam.WarehouseStorageWithPackingInfoMasterObj wvvObj = new BWarehouseStorageWithPackingInfoParam().new WarehouseStorageWithPackingInfoMasterObj().getWsSheet(dtMst.Rows.get(0), dtWvrDet);

            VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.INV.Parameter.WarehouseStorageWithPackingInfoMasterObj", "bmWMS.INV.Param");
            MesClass mesClassEnum = new MesClass(vListEnum);
            String strWvMstObj = mesClassEnum.generateFinalCode(wvvObj);

            ParameterInfo param4 = new ParameterInfo();
            param4.setParameterID(BWarehouseStorageWithPackingInfoParam.WsObj);
            param4.setNetParameterValue(strWvMstObj);
            biObj1.params.add(param4);

            ParameterInfo param5 = new ParameterInfo();
            param5.setParameterID(BWarehouseStorageWithPackingInfoParam.TrxMode);
            param5.setParameterValue("Modified");
            biObj1.params.add(param5);

            ParameterInfo paramExecuteChkStock = new ParameterInfo();
            paramExecuteChkStock.setParameterID(BIWarehouseStoragePortalParam.ExecuteCheckStock);
            paramExecuteChkStock.setNetParameterValue2("false");
            biObj1.params.add(paramExecuteChkStock);

            ((WarehouseStorageDetailNewActivity)getActivity()).CallBModule(biObj1, new WebAPIClientEvent() {
                @Override
                public void onPostBack(BModuleReturn bModuleReturn) {
                    if (((WarehouseStorageDetailNewActivity)getActivity()).CheckBModuleReturnInfo(bModuleReturn)) {

                        //WAPG027045    入庫暫存成功!
                        ((WarehouseStorageDetailNewActivity) getActivity()).ShowMessage(R.string.WAPG027045);

                        clearWidgetData();
                        clearData();
                        showListView();

                    }
                }
            });
        }
    };

    private void clearWidgetData() {
        etSkuNum.getText().clear();

        //region -- 存貨層級設置回預設空白/SkuLevel Set to Default --
        SimpleAdapter adapterSkuLevel = new SimpleArrayAdapter<>(getContext(), lstSkuLevel, android.R.layout.simple_spinner_item, new String[]{"DATA_ID", "DATA_NAME", "IDNAME"}, new int[]{0, android.R.id.text1, 0});
        adapterSkuLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmbSkuLevel.setAdapter(adapterSkuLevel);
        cmbSkuLevel.setSelection(lstSkuLevel.size()-1, true);
        // endregion
    }

    private void clearData() {
        dtInputWvrDet.Rows.removeAll(dtInputWvrDet.Rows);
        dtInputWvrDetGroup.Rows.removeAll(dtInputWvrDetGroup.Rows);
        lstExistSkuNum =  null;
    }

    public void setWvrData(DataTable dtWvr, DataTable dtWvrGroup) {
        dtWvrDetGroup = dtWvrGroup;
        dtWvrDet = dtWvr;
    }

    private class SimpleArrayAdapter<T> extends SimpleAdapter {

        public SimpleArrayAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        //複寫這個方法，使返回的數據沒有最後一項/Override this method so that the returned data does not have the last item
        @Override
        public int getCount() {
            // don't display last item. It is used as hint.
            int count = super.getCount();
            return count > 0 ? count - 1 : count;
        }
    }
}
