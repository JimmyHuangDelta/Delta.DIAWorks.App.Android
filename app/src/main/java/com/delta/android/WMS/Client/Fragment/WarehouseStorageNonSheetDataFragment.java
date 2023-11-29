package com.delta.android.WMS.Client.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.delta.android.Core.Activity.ScanActivity;
import com.delta.android.Core.Activity.ShowMessageEvent;
import com.delta.android.Core.DataTable.DataColumn;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Client.WarehouseStorageNonSheetDetailNewActivity;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.BIWarehouseStorageNonSheetPortalParam;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class WarehouseStorageNonSheetDataFragment extends Fragment {

    // region 元件/Widget
    private Spinner cmbStorage;
    private EditText etWoId;
    private Spinner cmbItem;
    private Spinner cmbSkuLevel;
    private ImageButton ibtnSkuNumQRScan;
    private EditText etSkuNum;
    private EditText etLotCode;
    private EditText etQty;
    private EditText etScrapQty;
    private EditText etUom;
    private EditText etCmt;
    private EditText etMfgDate;
    private EditText etExpDate;
    private Button btnMfgDateClear;
    private Button btnExpDateClear;
    private Button btnAdd;
    private Button btnClear;
    // endregion

    //region 全域變數/global variables
    ISendNonSheetData iSendNonSheetData;

    private List<? extends Map<String, Object>> lstSkuLevel;
    private List<? extends Map<String, Object>> lstStorage;
    private List<? extends Map<String, Object>> lstItem;

    private String storageId = "", skuLevel = "", itemId = "", itemName = "";
    private boolean fromWwvCont = false; // 判斷此筆資料是否來自中間表/Determine whether this data comes from the intermediate table
    private List<String> lstExistSkuNum = new ArrayList<>();

    private DataTable dtWvrDet = null;
    private DataTable dtWvrDetGroup = null;
    private DataTable dtWwvDet = new DataTable();
    private DataTable dtWwvDetGroup = new DataTable();
    private final int SKU_NUM_QRSCAN_REQUEST_CODE = 1301;
    //endregion

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_warehouse_storage_non_sheet_data, container, false);

        setInitWidget(view);

        getSpinnerData();

        setListeners();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);

        if (result != null) {

            if (result.getContents() == null) {
                Toast.makeText((WarehouseStorageNonSheetDetailNewActivity)getActivity(), getResources().getText(R.string.QRCODE_SCAN_CANCEL).toString(), Toast.LENGTH_SHORT).show();
            } else {

                switch (requestCode) {
                    case SKU_NUM_QRSCAN_REQUEST_CODE:
                        etSkuNum.setText(result.getContents().trim());
                        getWwvCont(skuLevel, result.getContents().trim());
                        break;

                    default:
                        break;
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    /**
     * 元件初始化
     * Component initialization
     */
    private void setInitWidget(View view) {
        cmbStorage = view.findViewById(R.id.cmbStorage);
        etWoId = view.findViewById(R.id.etWoId);
        cmbItem = view.findViewById(R.id.cmbItem);
        cmbSkuLevel = view.findViewById(R.id.cmbSkuLevel);
        ibtnSkuNumQRScan = view.findViewById(R.id.ibtnSkuNumQRScan);
        etSkuNum = view.findViewById(R.id.etSkuNum);
        etLotCode = view.findViewById(R.id.etLotCode);
        etQty = view.findViewById(R.id.etQty);
        etScrapQty = view.findViewById(R.id.etScrapQty);
        etUom = view.findViewById(R.id.etUom);
        etCmt = view.findViewById(R.id.etCmt);
        etMfgDate = view.findViewById(R.id.etMfgDate);
        etMfgDate.setInputType(InputType.TYPE_NULL);
        etExpDate = view.findViewById(R.id.etExpDate);
        etExpDate.setInputType(InputType.TYPE_NULL);
        btnMfgDateClear = view.findViewById(R.id.btnMfgDateClear);
        btnExpDateClear = view.findViewById(R.id.btnExpDateClear);
        btnAdd = view.findViewById(R.id.btnAdd);
        btnClear = view.findViewById(R.id.btnClear);

        dtWvrDet = createWarehouseStorageTable();
        dtWvrDetGroup = createWarehouseStorageTable();
    }

    /**
     * 取得下拉選單資料並放入Spinner內
     * Get drop-down menu data and put it into Spinner
     */
    private void getSpinnerData() {

        //region Set Param
        ArrayList<BModuleObject> lsBmObj = new ArrayList<>();

        // 倉庫/Storage
        BModuleObject bmObjStorage = new BModuleObject();
        bmObjStorage.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObjStorage.setModuleID("BIFetchStorage");
        bmObjStorage.setRequestID("BIFetchStorage");

        bmObjStorage.params = new Vector<ParameterInfo>();
        ParameterInfo paramStorage = new ParameterInfo();
        paramStorage.setParameterID(BIWMSFetchInfoParam.Filter);
        paramStorage.setParameterValue(" AND S.STORAGE_TYPE ='WMS' ");
        bmObjStorage.params.add(paramStorage);
        lsBmObj.add(bmObjStorage);

        // 存貨層級/SkuLevel
        BModuleObject bmObjSkuLevel = new BModuleObject();
        bmObjSkuLevel.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSPackingInfo");
        bmObjSkuLevel.setModuleID("BIFetchSkuLevel");
        bmObjSkuLevel.setRequestID("BIFetchSkuLevel");
        lsBmObj.add(bmObjSkuLevel);

        // 物料/Item
        BModuleObject bmObjItem = new BModuleObject();
        bmObjItem.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObjItem.setModuleID("BIFetchItem");
        bmObjItem.setRequestID("BIFetchItem");
        lsBmObj.add(bmObjItem);

        ((WarehouseStorageNonSheetDetailNewActivity) getActivity()).CallBIModule(lsBmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (((WarehouseStorageNonSheetDetailNewActivity) getActivity()).CheckBModuleReturnInfo(bModuleReturn)) {

                    DataTable dtSkuLevel = bModuleReturn.getReturnJsonTables().get("BIFetchSkuLevel").get("WmsSkuLevel");
                    DataTable dtStorage = bModuleReturn.getReturnJsonTables().get("BIFetchStorage").get("STORAGE");
                    DataTable dtItem = bModuleReturn.getReturnJsonTables().get("BIFetchItem").get("ITEM");

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

                    // region -- 物料/Item --
                    if (dtItem != null && dtItem.Rows.size() > 0)
                        dtItem.Rows.add(drDefaultItem);
                    lstItem = (List<? extends Map<String, Object>>) dtItem.toListHashMap();
                    SimpleAdapter adapterItem = new SimpleArrayAdapter<>(getContext(), lstItem, android.R.layout.simple_spinner_item, new String[]{"ITEM_KEY", "ITEM_ID", "ITEM_NAME", "REGISTER_TYPE", "IDNAME"}, new int[]{0, android.R.id.text1, 0, 0, 0});
                    adapterItem.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    cmbItem.setAdapter(adapterItem);
                    cmbItem.setSelection(lstItem.size()-1, true);
                    if (dtItem.Rows.size() > 2)
                        cmbItem.setSelection(lstItem.size()-1, true);
                    else if (dtItem.Rows.size() == 2)
                        cmbItem.setSelection(0, true);
                    // endregion

                }
            }
        });
    }

    /**
     * 設置選擇製造日期視窗
     * Set Select Date of Manufacture window
     */
    private void setMfgDate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getLayoutInflater().inflate(R.layout.style_wms_dialog_date_picker_spinner, null);//layout
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);//物件
        //右邊簡化/Simplify on the right
        datePicker.setCalendarViewShown(false);
        //初始化/Initialize
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
        //設置Dialog/Setting Dialog
        builder.setView(view);
        //標頭/header
        builder.setTitle(R.string.MFD_DATE);
        final AlertDialog dialog = builder.create();
        dialog.show();

        Button bConfirm = view.findViewById(R.id.btnConfirm);
        bConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //日期格式/Date Format
                StringBuffer sb = new StringBuffer();
                sb.append(String.format("%d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth()));
                etMfgDate.setText(sb);
                dialog.cancel();
            }
        });
    }

    /**
     * 設置選擇有效期限日期視窗
     * Set select expiry date window
     */
    private void setExpDate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getLayoutInflater().inflate(R.layout.style_wms_dialog_date_picker_spinner, null);//layout
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);//物件
        //右邊簡化/Simplify on the right
        datePicker.setCalendarViewShown(false);
        //初始化/Initialize
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
        //設置Dialog/Setting Dialog
        builder.setView(view);
        //標頭/Header
        builder.setTitle(R.string.EXPIRY_DATE);
        final AlertDialog dialog = builder.create();
        dialog.show();

        Button bConfirm = view.findViewById(R.id.btnConfirm);
        bConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //日期格式/Date format
                StringBuffer sb = new StringBuffer();
                sb.append(String.format("%d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth()));
                etExpDate.setText(sb);
                dialog.cancel();
            }
        });
    }

    /**
     * 新增入庫資料前，確認是否有填入必填資料
     * Before adding data into the database, confirm whether the required information is filled in
     * @return true/false
     */
    private boolean chkRequiredData() {

        if (storageId.length() <= 0) {

            // WAPG010001    請選擇倉庫!
            ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG010001);
            cmbStorage.requestFocus();
            return false;
        }

        if (itemId.length() <= 0) {

            // WAPG010002    請選擇物料!
            ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG010002);
            cmbItem.requestFocus();
            return false;
        }

        if (etQty.length() <= 0) {

            // WAPG010003    請輸入數量!
            ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG010003);
            etQty.requestFocus();
            return false;
        }

        if (etSkuNum.getText().length() <= 0) {

            // WAPG010014    請輸入存貨編號!
            ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG010014);
            etSkuNum.requestFocus();
            return false;
        }

        if (lstExistSkuNum.contains(etSkuNum.getText().toString().trim())) {
            // WAPG010017    存貨編號已存在!
            ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG010017);
            etSkuNum.requestFocus();
            return false;
        }

        if (etMfgDate.getText().length() <= 0) {

            // WAPG010015    請輸入製造日期!
            ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG010015);
            etMfgDate.requestFocus();
            return false;
        }

        if (etExpDate.getText().length() <= 0) {

            // WAPG010016    請輸入有效期限!
            ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG010016);
            etExpDate.requestFocus();
            return false;
        }

        int compare = etMfgDate.getText().toString().compareTo(etExpDate.getText().toString());
        if (compare > 0) {
            // WAPG010026   製造日期不可大於有效期限
            ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG010026);
            etMfgDate.requestFocus();
            return false;
        }

        switch (skuLevel) {

            case "Pallet":
            case "Box3":
            case "Box2":
            case "Box1":
                if (!fromWwvCont) {

                    // WAPG010018    請確認存貨編號存在於中間表(SWMS_CONT_WWV)!
                    ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG010018);
                    return false;

                } else if (fromWwvCont && dtWwvDetGroup.Rows.size() == 0) {

                    // WAPG010018    請確認存貨編號存在於中間表(SWMS_CONT_WWV)!
                    ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG010018);
                    return false;
                }
                break;

            case "Entity":
                break;

            default:
                // WAPG010019    請選擇存貨層級!
                ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG010019);
                return false;
        }

        return true;
    }

    /**
     * 鎖定元件
     * Set enabled or disabled to widget
     * @param isEnabled true/false
     */
    private void setWidgetEnable(boolean isEnabled) {
        etWoId.setEnabled(isEnabled);
        etLotCode.setEnabled(isEnabled);
        cmbItem.setEnabled(isEnabled);
        etQty.setEnabled(isEnabled);
        etUom.setEnabled(isEnabled);
        etCmt.setEnabled(isEnabled);
        etMfgDate.setEnabled(isEnabled);
        etExpDate.setEnabled(isEnabled);

        btnMfgDateClear.setEnabled(isEnabled);
        btnExpDateClear.setEnabled(isEnabled);
    }

    /**
     * 設置監聽事件
     * Set up listening events
     */
    private void setListeners() {

        cmbStorage.setOnItemSelectedListener(onSelectStorage);
        cmbSkuLevel.setOnItemSelectedListener(onSelectSkuLevel);
        cmbItem.setOnItemSelectedListener(onSelectItem);
        etSkuNum.setOnKeyListener(onKeySkuNum);
        ibtnSkuNumQRScan.setOnClickListener(onClickSkuNumQRScan);
        etMfgDate.setOnClickListener(onClickSetMfgDate);
        etExpDate.setOnClickListener(onClickSetExpDate);
        btnMfgDateClear.setOnClickListener(onClickClearMfgDate);
        btnExpDateClear.setOnClickListener(onClickClearExpDate);
        btnAdd.setOnClickListener(onClickAdd);
        btnClear.setOnClickListener(onClickClear);
    }

    /**
     * 選取倉庫下拉選單，取得StorageId等資訊
     * Select the warehouse drop-down menu to obtain Storage Id and other information
     */
    private Spinner.OnItemSelectedListener onSelectStorage = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            storageId = "";

            if (position != lstStorage.size() - 1) {
                Map<String, String> storageMap = (Map<String, String>) parent.getItemAtPosition(position);
                storageId = storageMap.get("STORAGE_ID");
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    /**
     * 選取存貨層級下拉選單，取得SkuLevel等資訊
     * Select the inventory level drop-down menu to obtain information such as Sku Level
     */
    private Spinner.OnItemSelectedListener onSelectSkuLevel = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            skuLevel = "";

            if (position != lstSkuLevel.size() - 1) {
                Map<String, String> skuLevelMap = (Map<String, String>) parent.getItemAtPosition(position);
                skuLevel = skuLevelMap.get("DATA_ID");
            }

            // 除了存貨層級為Entity可手動建帳外，其餘的資料應從中間表來，所以當切換至非Entity層級，只剩存貨編號可輸入
            // Except that the inventory level is Entity and accounts can be created manually, the rest of the data should come from the intermediate table, so when switching to a non-Entity level, only the inventory number can be entered
            switch (skuLevel) {

                case "Pallet":
                case "Box3":
                case "Box2":
                case "Box1":
                    setWidgetEnable(false);
                    break;

                case "Entity":
                default:
                    setWidgetEnable(true);
                    break;
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    /**
     * 選取物料下拉選單，取得ItemId等資訊
     * Select the item drop-down menu to obtain Item Id and other information
     */
    private Spinner.OnItemSelectedListener onSelectItem = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            itemId = "";
            itemName = "";

            if (position != lstItem.size() - 1) {
                Map<String, String> itemMap = (Map<String, String>) parent.getItemAtPosition(position);
                itemId = itemMap.get("ITEM_ID");
                itemName = itemMap.get("ITEM_NAME");
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    /**
     * 點選存貨編號QRCode按鈕，開啟鏡頭掃描
     * Click the QR Code button of the sku num to start the lens scanning
     */
    private View.OnClickListener onClickSkuNumQRScan = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            IntentIntegrator integrator = IntentIntegrator.forSupportFragment(WarehouseStorageNonSheetDataFragment.this);
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
     * 點選製造日期，彈出日期選擇視窗
     * Click on the date of manufacture, and a date selection window will pop up
     */
    private View.OnClickListener onClickSetMfgDate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setMfgDate();
        }
    };

    /**
     * 點選有效日期，彈出日期選擇視窗
     * Click on the effective date, and the date selection window will pop up
     */
    private View.OnClickListener onClickSetExpDate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setExpDate();
        }
    };

    /**
     * 點選新增按鈕，將入庫明細加入庫明細頁籤內
     * Click the Add button to add the storage details to the storage details tab
     */
    private View.OnClickListener onClickAdd = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (!chkRequiredData())
                return;

            // 存貨層級為 Entity，入庫資料可能來自手動建帳或中間表/The sku level is Entity, and the warehousing data may come from manual account creation or intermediate tables
            if (skuLevel.equals("Entity")) {

                if (fromWwvCont == true) { // 來自中間表/from the intermediate table

                    for (DataRow dr : dtWwvDet.Rows) {
                        DataRow drNew = dtWvrDet.newRow();
                        drNew.setValue("SKU_LEVEL", dr.getValue("SKU_LEVEL").toString());
                        drNew.setValue("SKU_NUM", dr.getValue("SKU_NUM").toString());
                        drNew.setValue("STORAGE_ID", storageId);
                        drNew.setValue("ITEM_ID", itemId);
                        drNew.setValue("ITEM_NAME", itemName);
                        drNew.setValue("LOT_ID", dr.getValue("ENTITY_ID").toString());
                        drNew.setValue("SPEC_LOT", "Y");
                        drNew.setValue("QTY", dr.getValue("QTY").toString());
                        drNew.setValue("UOM", ""); // 中間表沒有 UOM
                        drNew.setValue("CMT", dr.getValue("CMT").toString());
                        drNew.setValue("MFG_DATE", dr.getValue("MFG_DATE").toString());
                        drNew.setValue("EXP_DATE", dr.getValue("EXP_DATE").toString());
                        drNew.setValue("TEMP_BIN", ""); // 之後確認入庫要填入的入庫儲位
                        String lotCode = dr.getValue("LOT_CODE").toString().equals("") ? "*" : dr.getValue("LOT_CODE").toString();
                        drNew.setValue("LOT_CODE", lotCode);
                        String woId = dr.getValue("WO_ID").toString().equals("") ? "*" : dr.getValue("WO_ID").toString();
                        drNew.setValue("WO_ID", woId);
                        drNew.setValue("BOX1_ID", dr.getValue("BOX1_ID").toString());
                        drNew.setValue("BOX2_ID", dr.getValue("BOX2_ID").toString());
                        drNew.setValue("BOX3_ID", dr.getValue("BOX3_ID").toString());
                        drNew.setValue("PALLET_ID", dr.getValue("PALLET_ID").toString());
                        dtWvrDet.Rows.add(drNew);
                    }

                    for (DataRow dr : dtWwvDetGroup.Rows) {
                        DataRow drNew = dtWvrDetGroup.newRow();
                        drNew.setValue("SKU_LEVEL", dr.getValue("SKU_LEVEL").toString());
                        drNew.setValue("SKU_NUM", dr.getValue("SKU_NUM").toString());
                        drNew.setValue("STORAGE_ID", storageId);
                        drNew.setValue("ITEM_ID", itemId);
                        drNew.setValue("ITEM_NAME", itemName);
                        drNew.setValue("LOT_ID", dr.getValue("ENTITY_ID").toString());
                        drNew.setValue("SPEC_LOT", "Y");
                        drNew.setValue("QTY", dr.getValue("QTY").toString());
                        drNew.setValue("UOM", ""); // 中間表沒有 UOM
                        drNew.setValue("CMT", dr.getValue("CMT").toString());
                        drNew.setValue("MFG_DATE", dr.getValue("MFG_DATE").toString());
                        drNew.setValue("EXP_DATE", dr.getValue("EXP_DATE").toString());
                        drNew.setValue("TEMP_BIN", ""); // 之後確認入庫要填入的入庫儲位
                        String lotCode = dr.getValue("LOT_CODE").toString().equals("") ? "*" : dr.getValue("LOT_CODE").toString();
                        drNew.setValue("LOT_CODE", lotCode);
                        String woId = dr.getValue("WO_ID").toString().equals("") ? "*" : dr.getValue("WO_ID").toString();
                        drNew.setValue("WO_ID", woId);
                        drNew.setValue("BOX1_ID", dr.getValue("BOX1_ID").toString());
                        drNew.setValue("BOX2_ID", dr.getValue("BOX2_ID").toString());
                        drNew.setValue("BOX3_ID", dr.getValue("BOX3_ID").toString());
                        drNew.setValue("PALLET_ID", dr.getValue("PALLET_ID").toString());
                        dtWvrDetGroup.Rows.add(drNew);
                    }

                } else { // 來自手動建帳/From manual account creation

                    DataRow drNew = dtWvrDet.newRow();
                    drNew.setValue("SKU_LEVEL", skuLevel);
                    drNew.setValue("SKU_NUM", etSkuNum.getText().toString().trim());
                    drNew.setValue("STORAGE_ID", storageId);
                    drNew.setValue("ITEM_ID", itemId);
                    drNew.setValue("ITEM_NAME", itemName);
                    String lotCode = etLotCode.getText().toString().trim().equals("") ? "*" : etLotCode.getText().toString().trim();
                    drNew.setValue("LOT_CODE", lotCode);
                    String woId = etWoId.getText().toString().trim().equals("") ? "*" : etWoId.getText().toString().trim();
                    drNew.setValue("WO_ID", woId);
                    drNew.setValue("LOT_ID", etSkuNum.getText().toString().trim());
                    drNew.setValue("SPEC_LOT", "Y");
                    drNew.setValue("QTY", etQty.getText().toString().trim());
                    drNew.setValue("UOM", etUom.getText().toString().trim());
                    drNew.setValue("CMT", etCmt.getText().toString().trim());
                    drNew.setValue("MFG_DATE", etMfgDate.getText().toString().trim());
                    drNew.setValue("EXP_DATE", etExpDate.getText().toString().trim());
                    drNew.setValue("TEMP_BIN", ""); // 之後確認入庫要填入的入庫儲位
                    drNew.setValue("BOX1_ID", ""); // 畫面沒有 BOX1_ID
                    drNew.setValue("BOX2_ID", ""); // 畫面沒有 BOX2_ID
                    drNew.setValue("BOX3_ID", ""); // 畫面沒有 BOX3_ID
                    drNew.setValue("PALLET_ID", ""); // 畫面沒有 PALLET_ID
                    dtWvrDet.Rows.add(drNew);
                    dtWvrDetGroup.Rows.add(drNew);

                }

            } else { // 其他的存貨層級資料必定來自中間表/Other sku level data must come from the intermediate table

                for (DataRow dr : dtWwvDet.Rows) {
                    DataRow drNew = dtWvrDet.newRow();
                    drNew.setValue("SKU_LEVEL", dr.getValue("SKU_LEVEL").toString());
                    drNew.setValue("SKU_NUM", dr.getValue("SKU_NUM").toString());
                    drNew.setValue("STORAGE_ID", storageId);
                    drNew.setValue("ITEM_ID", itemId);
                    drNew.setValue("ITEM_NAME", itemName);
                    drNew.setValue("LOT_ID", dr.getValue("ENTITY_ID").toString());
                    drNew.setValue("SPEC_LOT", "Y");
                    drNew.setValue("QTY", dr.getValue("QTY").toString());
                    drNew.setValue("UOM", ""); // 中間表沒有 UOM
                    drNew.setValue("CMT", dr.getValue("CMT").toString());
                    drNew.setValue("MFG_DATE", dr.getValue("MFG_DATE").toString());
                    drNew.setValue("EXP_DATE", dr.getValue("EXP_DATE").toString());
                    drNew.setValue("TEMP_BIN", ""); // 之後確認入庫要填入的入庫儲位
                    String lotCode = dr.getValue("LOT_CODE").toString().equals("") ? "*" : dr.getValue("LOT_CODE").toString();
                    drNew.setValue("LOT_CODE", lotCode);
                    String woId = dr.getValue("WO_ID").toString().equals("") ? "*" : dr.getValue("WO_ID").toString();
                    drNew.setValue("WO_ID", woId);
                    drNew.setValue("BOX1_ID", dr.getValue("BOX1_ID").toString());
                    drNew.setValue("BOX2_ID", dr.getValue("BOX2_ID").toString());
                    drNew.setValue("BOX3_ID", dr.getValue("BOX3_ID").toString());
                    drNew.setValue("PALLET_ID", dr.getValue("PALLET_ID").toString());
                    dtWvrDet.Rows.add(drNew);
                }

                for (DataRow dr : dtWwvDetGroup.Rows) {
                    DataRow drNew = dtWvrDetGroup.newRow();
                    drNew.setValue("SKU_LEVEL", dr.getValue("SKU_LEVEL").toString());
                    drNew.setValue("SKU_NUM", dr.getValue("SKU_NUM").toString());
                    drNew.setValue("STORAGE_ID", storageId);
                    drNew.setValue("ITEM_ID", itemId);
                    drNew.setValue("ITEM_NAME", itemName);
                    drNew.setValue("LOT_ID", dr.getValue("ENTITY_ID").toString());
                    drNew.setValue("SPEC_LOT", "Y");
                    drNew.setValue("QTY", dr.getValue("QTY").toString());
                    drNew.setValue("UOM", ""); // 中間表沒有 UOM
                    drNew.setValue("CMT", dr.getValue("CMT").toString());
                    drNew.setValue("MFG_DATE", dr.getValue("MFG_DATE").toString());
                    drNew.setValue("EXP_DATE", dr.getValue("EXP_DATE").toString());
                    drNew.setValue("TEMP_BIN", ""); // 之後確認入庫要填入的入庫儲位
                    String lotCode = dr.getValue("LOT_CODE").toString().equals("") ? "*" : dr.getValue("LOT_CODE").toString();
                    drNew.setValue("LOT_CODE", lotCode);
                    String woId = dr.getValue("WO_ID").toString().equals("") ? "*" : dr.getValue("WO_ID").toString();
                    drNew.setValue("WO_ID", woId);
                    drNew.setValue("BOX1_ID", dr.getValue("BOX1_ID").toString());
                    drNew.setValue("BOX2_ID", dr.getValue("BOX2_ID").toString());
                    drNew.setValue("BOX3_ID", dr.getValue("BOX3_ID").toString());
                    drNew.setValue("PALLET_ID", dr.getValue("PALLET_ID").toString());
                    dtWvrDetGroup.Rows.add(drNew);

                }

            }

            // region Add WvrDet

            iSendNonSheetData.sendWvrDet(dtWvrDet, dtWvrDetGroup);
            lstExistSkuNum.add(etSkuNum.getText().toString().trim());

            // endregion

            refresh();
        }
    };

    /**
     * 將畫面資料清空
     * Clear screen data
     */
    private void clearData() {

        //region -- 存貨層級設置回預設空白/SkuLevel Set to Default --
        SimpleAdapter adapterSkuLevel = new SimpleArrayAdapter<>(getContext(), lstSkuLevel, android.R.layout.simple_spinner_item, new String[]{"DATA_ID", "DATA_NAME", "IDNAME"}, new int[]{0, android.R.id.text1, 0});
        adapterSkuLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmbSkuLevel.setAdapter(adapterSkuLevel);
        cmbSkuLevel.setSelection(lstSkuLevel.size()-1, true);
        // endregion

        //region -- 物料設置回預設空白/Item Set to Default --
        SimpleAdapter adapterItem = new SimpleArrayAdapter<>(getContext(), lstItem, android.R.layout.simple_spinner_item, new String[]{"ITEM_KEY", "ITEM_ID", "ITEM_NAME", "REGISTER_TYPE", "IDNAME"}, new int[]{0, android.R.id.text1, 0, 0, 0});
        adapterItem.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmbItem.setAdapter(adapterItem);
        if (lstItem.size() > 2)
            cmbItem.setSelection(lstItem.size()-1, true);
        else if (lstItem.size() == 2)
            cmbItem.setSelection(0, true);
        // endregion

        cmbSkuLevel.setEnabled(true);
        etWoId.getText().clear();
        etSkuNum.getText().clear();
        etLotCode.getText().clear();
        etQty.getText().clear();
        etUom.getText().clear();
        etCmt.getText().clear();
        etMfgDate.getText().clear();
        etExpDate.getText().clear();
    }

    /**
     *新增完畢後清空畫面及復原全域變數
     * Clear the screen and restore global variables after adding
     */
    private void refresh() {

        clearData();
        etSkuNum.requestFocus();
        fromWwvCont = false;
        dtWvrDet.Rows.removeAll(dtWvrDet.Rows);
        dtWvrDetGroup.Rows.removeAll(dtWvrDetGroup.Rows);
    }

    private View.OnKeyListener onKeySkuNum = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent event) {

            if (keyCode != KeyEvent.KEYCODE_ENTER)
                return false;

            if (event.getAction() == KeyEvent.ACTION_UP) {

                InputMethodManager manager = (InputMethodManager) ((WarehouseStorageNonSheetDetailNewActivity) getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                String skuNum = etSkuNum.getText().toString();
                getWwvCont(skuLevel, skuNum);

                return true;
            }

            return false;
        }
    };

    /**
     * Call BIModule 取得中間表資訊
     * Call BIModule to obtain intermediate table information
     * @param skuLevel
     * @param skuNum
     */
    private void getWwvCont(String skuLevel, String skuNum) {

        String wvId = "*";

        if (skuNum.length() <= 0) {

            // WAPG010014    請輸入存貨編號!
            ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG010014);
            etSkuNum.requestFocus();
            return;
        }

        if (lstExistSkuNum.contains(skuNum)){

            // WAPG010017    存貨編號已存在!
            ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG010017);
            clearData();
            etSkuNum.requestFocus();
            return;
        }

        BModuleObject biObj1 = new BModuleObject();
        biObj1.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIWarehouseStorageNonSheetPortal");
        biObj1.setModuleID("BIGetWwvCont");
        biObj1.setRequestID("BIGetWwvCont");
        biObj1.params = new Vector<>();

        //Input Param
        ParameterInfo paramWvId = new ParameterInfo();
        paramWvId.setParameterID(BIWarehouseStorageNonSheetPortalParam.WvId);
        paramWvId.setParameterValue(wvId);
        biObj1.params.add(paramWvId);

        if (skuLevel.length() > 0) {
            ParameterInfo paramSkuLevel = new ParameterInfo();
            paramSkuLevel.setParameterID(BIWarehouseStorageNonSheetPortalParam.SkuLevel);
            paramSkuLevel.setParameterValue(skuLevel);
            biObj1.params.add(paramSkuLevel);
        }

        ParameterInfo paramSkuNum = new ParameterInfo();
        paramSkuNum.setParameterID(BIWarehouseStorageNonSheetPortalParam.SkuNum);
        paramSkuNum.setParameterValue(skuNum);
        biObj1.params.add(paramSkuNum);

        ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).CallBIModule(biObj1, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {

                if (bModuleReturn.getAckError().containsKey("WBPG091004")) {

                } else if (bModuleReturn.getAckError().size() > 0) {
                    clearData();
                }

                if (((WarehouseStorageNonSheetDetailNewActivity)getActivity()).CheckBModuleReturnInfo(bModuleReturn)) {

                    String askChangeSkuLevel = "";

                    // 查詢發現還有外層包/The query found that there are outer packages
                    if (bModuleReturn.getReturnList().size() > 0) {

                        if (bModuleReturn.getReturnList().get("BIGetWwvCont").get(BIWarehouseStorageNonSheetPortalParam.ChangeSkuLevel) != null) {
                            askChangeSkuLevel = bModuleReturn.getReturnList().get("BIGetWwvCont").get(BIWarehouseStorageNonSheetPortalParam.ChangeSkuLevel).toString().replaceAll("\"", "");
                        }

                        if (!askChangeSkuLevel.equals("")) {

                            final String [] skuData = askChangeSkuLevel.split(":");
                            Object[] args = new Object[2];
                            args[0] = skuData[0];
                            args[1] = skuData[1];

                            String showMsg = String.format(((WarehouseStorageNonSheetDetailNewActivity) getActivity()).getResString(getResources().getString(R.string.CHANGE_SKU_LEVEL)), args);

                            ((WarehouseStorageNonSheetDetailNewActivity) getActivity()).ShowMessage(showMsg, new ShowMessageEvent() {
                                @Override
                                public void onDismiss() {

                                    // region -- 設置 cmbSkuLevel/Setting cmbSkuLevel --
                                    int levelPosition = 0;

                                    for (Map<String, Object> levelMap : lstSkuLevel) {
                                        if (levelMap.get("DATA_ID").toString().equals(skuData[0]))
                                            break;
                                        levelPosition++;
                                    }

                                    if (levelPosition == lstSkuLevel.size()) {
                                        // WAPG010020    查無存貨層級[%s]!
                                        ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG010020);
                                        return;
                                    }

                                    SimpleAdapter adapterSkuLevel = new SimpleArrayAdapter<>(getContext(), lstSkuLevel, android.R.layout.simple_spinner_item, new String[]{"DATA_ID", "DATA_NAME", "IDNAME"}, new int[]{0, android.R.id.text1, 0});
                                    adapterSkuLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    cmbSkuLevel.setAdapter(adapterSkuLevel);
                                    cmbSkuLevel.setSelection(levelPosition, true);
                                    // endregion

                                    etSkuNum.setText(skuData[1]);

                                    getWwvCont(skuData[0], skuData[1]);
                                    return;
                                }
                            });
                        }
                    }

//                    // 查詢成功回傳中間表可入庫資訊/If the query succeeds, return the storage information of the intermediate table
                    else if (bModuleReturn.getReturnJsonTables().size() > 0) {

                        dtWwvDet = bModuleReturn.getReturnJsonTables().get("BIGetWwvCont").get("dtWwvCont");
                        dtWwvDetGroup = bModuleReturn.getReturnJsonTables().get("BIGetWwvCont").get("dtWwvWithSkuLevel");

                        for (DataRow dr : dtWwvDetGroup.Rows) {

                            fromWwvCont = true;
                            cmbSkuLevel.setEnabled(false);

                            // region -- 設置 cmbSkuLevel/Setting cmbSkuLevel --
                            int levelPosition = 0;

                            for (Map<String, Object> levelMap : lstSkuLevel) {
                                if (levelMap.get("DATA_ID").toString().equals(dr.getValue("SKU_LEVEL").toString()))
                                    break;
                                levelPosition++;
                            }

                            if (levelPosition == lstSkuLevel.size()) {
                                // WAPG010020    查無存貨層級[%s]!
                                ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG010020);
                                return;
                            }

                            SimpleAdapter adapterSkuLevel = new SimpleArrayAdapter<>(getContext(), lstSkuLevel, android.R.layout.simple_spinner_item, new String[]{"DATA_ID", "DATA_NAME", "IDNAME"}, new int[]{0, android.R.id.text1, 0});
                            adapterSkuLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            cmbSkuLevel.setAdapter(adapterSkuLevel);
                            cmbSkuLevel.setSelection(levelPosition, true);
                            // endregion

                            // region -- 設置物料/Setting item --
                            int itemPosition = 0;

                            for (Map<String, Object> itemMap : lstItem) {
                                if (itemMap.get("ITEM_ID").toString().equals(dr.getValue("ITEM_ID").toString()))
                                    break;
                                itemPosition++;
                            }

                            if (itemPosition == lstItem.size()) {
                                // WAPG010021   查無物料代碼[%s]!
                                ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG010021);
                                return;
                            }

                            SimpleAdapter adapterItem = new SimpleArrayAdapter<>(getContext(), lstItem, android.R.layout.simple_spinner_item, new String[]{"ITEM_KEY", "ITEM_ID", "ITEM_NAME", "REGISTER_TYPE", "IDNAME"}, new int[]{0, android.R.id.text1, 0, 0, 0});
                            adapterItem.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            cmbItem.setAdapter(adapterItem);
                            cmbItem.setSelection(itemPosition, true);
                            // endregion

                            etWoId.setText(dr.getValue("WO_ID").toString());
                            etQty.setText(dr.getValue("QTY").toString());
                            etLotCode.setText(dr.getValue("LOT_CODE").toString());
                            //etUom.setText(dr.getValue("UOM").toString()); // 中間表沒有UOM/Intermediate tables have no UOM
                            etCmt.setText(dr.getValue("CMT").toString());
                            etMfgDate.setText(dr.getValue("MFG_DATE").toString());
                            etExpDate.setText(dr.getValue("EXP_DATE").toString());
                        }
                    }
                }
            }
        });
    }

    /**
     * 新建儲存畫面上呈現的入庫資料與實際儲存的入庫資料
     * The warehouse storage data presented on the new storage screen and the actually stored warehouse storage data
     * @return
     */
    private DataTable createWarehouseStorageTable() {
        DataTable dtWs = new DataTable();
        dtWs.addColumn(new DataColumn("SEQ"));
        dtWs.addColumn(new DataColumn("SKU_LEVEL"));
        dtWs.addColumn(new DataColumn("SKU_NUM"));
        dtWs.addColumn(new DataColumn("STORAGE_KEY"));
        dtWs.addColumn(new DataColumn("STORAGE_ID"));
        dtWs.addColumn(new DataColumn("ITEM_KEY"));
        dtWs.addColumn(new DataColumn("ITEM_ID"));
        dtWs.addColumn(new DataColumn("ITEM_NAME"));
        dtWs.addColumn(new DataColumn("LOT_ID"));
        dtWs.addColumn(new DataColumn("SPEC_LOT"));
        dtWs.addColumn(new DataColumn("QTY"));
        dtWs.addColumn(new DataColumn("UOM"));
        dtWs.addColumn(new DataColumn("CMT"));
        dtWs.addColumn(new DataColumn("MFG_DATE"));
        dtWs.addColumn(new DataColumn("EXP_DATE"));
        dtWs.addColumn(new DataColumn("TEMP_BIN"));
        dtWs.addColumn(new DataColumn("WO_ID"));
        dtWs.addColumn(new DataColumn("LOT_CODE"));
        dtWs.addColumn(new DataColumn("BOX1_ID"));
        dtWs.addColumn(new DataColumn("BOX2_ID"));
        dtWs.addColumn(new DataColumn("BOX3_ID"));
        dtWs.addColumn(new DataColumn("PALLET_ID"));
        return dtWs;
    }

    /**
     * 點選清除按鈕，將畫面上的資料清空
     */
    private View.OnClickListener onClickClear = new View.OnClickListener() {
        @Override
        public void onClick(View v) { clearData(); }
    };

    /**
     * 點選製造日期的x，清除製造日期
     * Click the x of the manufacturing date to clear the manufacturing date
     */
    private View.OnClickListener onClickClearMfgDate = new View.OnClickListener() {
        @Override
        public void onClick(View v) { etMfgDate.getText().clear();
        }
    };

    /**
     * 點選有效日期的x，清除有效日期
     * Click the x of the expiry date to clear the expiry date
     */
    private View.OnClickListener onClickClearExpDate = new View.OnClickListener() {
        @Override
        public void onClick(View v) { etExpDate.getText().clear();
        }
    };

    /**
     * 複寫 SimpleAdapter ，點選下拉選單時不顯示最後一項
     * Owrite SimpleAdapter,  the last item is not displayed when the drop-down menu is clicked
     * @param <T>
     */
    private class SimpleArrayAdapter<T> extends SimpleAdapter {
        public SimpleArrayAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        @Override
        public int getCount() {
            // don't display last item. It is used as hint.
            int count = super.getCount();
            return count > 0 ? count - 1 : count;
        }
    }

    // region Interface
    public interface ISendNonSheetData {

        void sendWvrDet(DataTable dtWvrDet, DataTable dtWvrDetGroup);
    }

    /**
     * 入庫明細頁籤有刪除資料時，將剩下的存貨編號傳回入庫頁籤
     * When data is deleted on the Inbound Details tab, the remaining inventory number will be sent back to the Inbound tab
     * @param existSkuNum
     */
    public void setExistSkuNum(List<String> existSkuNum) {
        lstExistSkuNum = existSkuNum;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            iSendNonSheetData = (ISendNonSheetData) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("Plz implement interface method");
        }
    }
    // endregion
}

// ERROR CODE
// WAPG010001    請選擇倉庫!
// WAPG010002    請選擇物料!
// WAPG010003    請輸入數量!
// WAPG010014    請輸入存貨編號!
// WAPG010015    請輸入製造日期!
// WAPG010016    請輸入有效期限!
// WAPG010017    存貨編號已存在!
// WAPG010018    請確認存貨編號存在於中間表(SWMS_CONT_WWV)!
// WAPG010019    請選擇存貨層級!
// WAPG010020    查無存貨層級[%s]!
// WAPG010021    查無物料代碼[%s]!
// WAPG010026    製造日期不可大於有效期限!