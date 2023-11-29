package com.delta.android.WMS.Client.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.Core.Activity.ScanActivity;
import com.delta.android.Core.Activity.ShowMessageEvent;
import com.delta.android.Core.DataTable.DataColumn;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.GenerateJsonNetString.MList;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Client.GoodNonReceiptReceiveDetailNewActivity;
import com.delta.android.WMS.Client.GridAdapter.GoodNonreceiptReceiveSNAdapter;
import com.delta.android.WMS.Client.GridAdapter.GoodReceiptReceiveExtendGridAdapter;
import com.delta.android.WMS.Param.BGoodReceiptReceiveNonSheetWithPackingInfoParam;
import com.delta.android.WMS.Param.BIGoodNonReceiptReceivePortalParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

public class GoodNonReceiptReceiveDataFragment extends Fragment {

    ISendNonReceiptReceiveDataTable iSendNonReceiptReceiveDataTable;

    // region -- DET 收料資訊控制項/DET Receipt information control item --
    private Spinner cmbStorage, cmbItem, cmbSize, cmbSkuLevel, cmbPoNo, cmbPoSeq;
    private EditText etPONo, etPOSeq, etRecScanCode, etLotID, etQty, etUOM, etMFDDate, etExpiryDate, etLotCode, etCmt;
    private RadioGroup rgRecQRCode;
    private RadioButton radioQRcode, radioBarcode;
    private ImageButton ibtnLotIdQRScan, ibtnRecQRScan;
    private Button btMFDDateClear, btExpiryDateClear, btnAddSn, btnAddLodID, btnRefresh;
    //private LinearLayout llBtnReceiveSn;
    private TextView tvRecExtendTitle;
    private ListView lvRecExtend;
    private LinearLayout llRecCode;
    // endregion

    // region -- 鏡頭掃描 Request Code/Lens scan Request Code --
    private final int LOT_ID_QRSCAN_REQUEST_CODE = 1;
    private final int REC_QRSCAN_REQUEST_CODE = 2;
    // endregion

    // region --  全域變數/Global Variables--
    List<? extends Map<String, Object>> lstStorage;
    List<? extends Map<String, Object>> lstItem;
    List<? extends Map<String, Object>> lstSize;
    List<? extends Map<String, Object>> lstSkuLevel;
    List<String> lstPoNo;
    List<? extends Map<String, Object>> lstPoSeq;
    List<String> lstCheckSkuNum = new ArrayList<>();

    String vendorKey, vendorId;
    String storageKey, storageId, itemKey, itemId, itemName, poNo, uom, cmt, mfgDate, expDate, skipQc, regType, tempBin, lotCode, sizeId, sizeKey, vendorItemId, skuLevelId, skuNum;

    BigDecimal poSeq = null, qty = null;
    String[] grrDetRefKey;
    DataTable dtLot, dtLotWithSkuLevel, dtSn, dtReceiptMst, dtVendorQcInfo;
    DataTable dtWgrDataGroup, dtOrgWgrData, dtOrgSn;
    String _detailMode = "Add", _addMode = "MGR";
    int _modifyPos = -1;
    // endregion

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_good_non_receipt_receive_data, container, false);

        setInitWidget(view);

        getSpinnerInitData();

        setListeners();

        dtReceiptMst = (DataTable)getFragmentManager().findFragmentByTag("ReceiveData").getArguments().getSerializable("dtReceiptMst");
        vendorKey = dtReceiptMst.Rows.get(0).getValue("VENDOR_KEY").toString();
        vendorId = dtReceiptMst.Rows.get(0).getValue("VENDOR_ID").toString();

        dtVendorQcInfo = (DataTable)getFragmentManager().findFragmentByTag("ReceiveData").getArguments().getSerializable("dtVendorQcInfo");

        dtLotWithSkuLevel = createDataTableLotID(); // 儲存最外層資訊
        dtLot = createDataTableLotID(); // 儲存最細的資訊
        dtSn = createDataTableSN();

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                InputMethodManager manager = (InputMethodManager) ((GoodNonReceiptReceiveDetailNewActivity) getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                return true;
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText((GoodNonReceiptReceiveDetailNewActivity)getActivity(), getResources().getText(R.string.QRCODE_SCAN_CANCEL).toString(), Toast.LENGTH_SHORT).show();
            } else {
                switch (requestCode) {

                    case LOT_ID_QRSCAN_REQUEST_CODE:
                        etLotID.setText(result.getContents().trim());
                        break;

                    case REC_QRSCAN_REQUEST_CODE:

                        int selectedId = rgRecQRCode.getCheckedRadioButtonId();
                        String barCodeType = "", barCodeTypeId = "";

                        if (selectedId == R.id.radioQRcode) {
                            barCodeType = "QRCODE_TYPE";
                            barCodeTypeId = "ReceiveQRCode";

                        } else if (selectedId == R.id.radioBarcode){
                            barCodeType = "BARCODE_TYPE";
                            barCodeTypeId = "ReceiveBarCode";
                        }

                        getRecCode(barCodeType, barCodeTypeId, result.getContents().trim(), dtReceiptMst, true);

                        break;

                    default:
                        break;
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // region -- Private Method --

    private void setInitWidget(View view) {
        cmbStorage = view.findViewById(R.id.cmbStorage);
        etPONo = view.findViewById(R.id.etPONo);
        etPOSeq = view.findViewById(R.id.etPOSeq);
        cmbItem = view.findViewById(R.id.cmbItem);
        cmbSize = view.findViewById(R.id.cmbSize);
        cmbSkuLevel = view.findViewById(R.id.cmbSkuLevel);
        cmbPoNo = view.findViewById(R.id.cmbPoNo);
        cmbPoSeq = view.findViewById(R.id.cmbPoSeq);

        etRecScanCode = view.findViewById(R.id.etRecScanCode);
        etLotID = view.findViewById(R.id.etLot);
        etLotCode = view.findViewById(R.id.etLotCode);
        etCmt = view.findViewById(R.id.etCmt);
        rgRecQRCode = view.findViewById(R.id.rgRecQRCode);
        radioBarcode = view.findViewById(R.id.radioBarcode);
        radioQRcode = view.findViewById(R.id.radioQRcode);
        ibtnLotIdQRScan = view.findViewById(R.id.ibtnLotIdQRScan);
        ibtnRecQRScan = view.findViewById(R.id.ibtnRecQRScan);
        etQty = view.findViewById(R.id.etQty);
        etUOM = view.findViewById(R.id.etUOM);
        etMFDDate= view.findViewById(R.id.etMFDDate);
        etMFDDate.setInputType(InputType.TYPE_NULL);
        etExpiryDate= view.findViewById(R.id.etExpiryDate);
        etExpiryDate.setInputType(InputType.TYPE_NULL);
        btMFDDateClear = view.findViewById(R.id.btMFDDateClear);
        btExpiryDateClear = view.findViewById(R.id.btExpiryDateClear);
        btnAddSn = view.findViewById(R.id.btnAddSn);
        btnAddLodID = view.findViewById(R.id.btnAddLot);
        btnRefresh = view.findViewById(R.id.btnRefresh);
        tvRecExtendTitle = view.findViewById(R.id.tvRecExtendTitle);
        tvRecExtendTitle = view.findViewById(R.id.tvRecExtendTitle);
        lvRecExtend = view.findViewById(R.id.lvRecExtend);
        llRecCode = view.findViewById(R.id.llRecCode);

        etRecScanCode.setEnabled(false);
        btnAddSn.setText(getResources().getString(R.string.GR_SN) + " (0)");
        btnAddSn.setEnabled(false);
    }

    private class AnotherSimpleArrayAdapter<T> extends SimpleAdapter {

        public AnotherSimpleArrayAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
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

    private class AnotherArrayAdapter<T> extends ArrayAdapter {
        public AnotherArrayAdapter(Context context, int resource, List<T> objects) {
            super(context, resource, objects);
        }

        //複寫這個方法，使返回的數據沒有最後一項/Override this method so that the returned data does not have the last item
        @Override
        public int getCount() {
            // don't display last item. It is used as hint.
            int count = super.getCount();
            return count > 0 ? count - 1 : count;
        }
    }

    private void getSpinnerInitData() {

        //region Set Param
        ArrayList<BModuleObject> lsBObj = new ArrayList<>();

        // 倉庫/Storage
        BModuleObject bmObjStorage = new BModuleObject();
        bmObjStorage.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObjStorage.setModuleID("BIFetchStorage");
        bmObjStorage.setRequestID("GetStorage");

        bmObjStorage.params = new Vector<ParameterInfo>();
        ParameterInfo paramStorage = new ParameterInfo();
        paramStorage.setParameterID(BIWMSFetchInfoParam.Filter);
        paramStorage.setParameterValue("  AND S.STORAGE_TYPE ='WMS' ");
        bmObjStorage.params.add(paramStorage);
        lsBObj.add(bmObjStorage);

        // 物料/Item
        BModuleObject bmObjItem = new BModuleObject();
        bmObjItem.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObjItem.setModuleID("BIFetchItem");
        bmObjItem.setRequestID("GetItem");
        lsBObj.add(bmObjItem);

        // 尺寸/Size
        BModuleObject bmObjSize = new BModuleObject();
        bmObjSize.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObjSize.setModuleID("BIFetchSize");
        bmObjSize.setRequestID("GetSize");
        lsBObj.add(bmObjSize);

        // 存貨層級/SkuLevel
        BModuleObject bmObjSkuLevel = new BModuleObject();
        bmObjSkuLevel.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSPackingInfo");
        bmObjSkuLevel.setModuleID("BIFetchSkuLevel");
        bmObjSkuLevel.setRequestID("GetSkuLevel");
        lsBObj.add(bmObjSkuLevel);

        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).CallBIModule(lsBObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (((GoodNonReceiptReceiveDetailNewActivity)getActivity()).CheckBModuleReturnInfo(bModuleReturn)) {
                    DataTable dtStorage = bModuleReturn.getReturnJsonTables().get("GetStorage").get("STORAGE");
                    DataTable dtItem = bModuleReturn.getReturnJsonTables().get("GetItem").get("ITEM");
                    DataTable dtSize = bModuleReturn.getReturnJsonTables().get("GetSize").get("WMS_SIZE");
                    DataTable dtSkuLevel = bModuleReturn.getReturnJsonTables().get("GetSkuLevel").get("WmsSkuLevel");

                    DataRow drDefaultItem = dtStorage.newRow();
                    drDefaultItem.setValue("IDNAME", ""); // 下拉式選單default空白/drop-down menu default blank

                    // region -- 倉庫/Storage --
                    if (dtStorage != null && dtStorage.Rows.size() > 0)
                        dtStorage.Rows.add(drDefaultItem);
                    lstStorage = (List<? extends Map<String, Object>>) dtStorage.toListHashMap();
                    SimpleAdapter adapterStorage = new AnotherSimpleArrayAdapter<>(getContext(), lstStorage, android.R.layout.simple_spinner_item, new String[]{"STORAGE_KEY", "STORAGE_ID", "IDNAME"}, new int[]{0, android.R.id.text1, 0});
                    adapterStorage.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    cmbStorage.setAdapter(adapterStorage);
                    cmbStorage.setSelection(lstStorage.size()-1, true);
                    // endregion

                    // region -- 物料/Item --
                    if (dtItem != null && dtItem.Rows.size() > 0)
                        dtItem.Rows.add(drDefaultItem);
                    lstItem = (List<? extends Map<String, Object>>) dtItem.toListHashMap();
                    SimpleAdapter adapterItem = new AnotherSimpleArrayAdapter<>(getContext(), lstItem, android.R.layout.simple_spinner_item, new String[]{"ITEM_KEY", "ITEM_ID", "ITEM_NAME", "REGISTER_TYPE", "IDNAME"}, new int[]{0, android.R.id.text1, 0, 0, 0});
                    adapterItem.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    cmbItem.setAdapter(adapterItem);
                    cmbItem.setSelection(lstItem.size()-1, true);
                    // endregion

                    // region -- 尺寸/Size --
                    if (dtSize != null && dtSize.Rows.size() > 0)
                        dtSize.Rows.add(drDefaultItem);
                    lstSize = (List<? extends Map<String, Object>>) dtSize.toListHashMap();
                    SimpleAdapter adapterSize = new AnotherSimpleArrayAdapter<>(getContext(), lstSize, android.R.layout.simple_spinner_item, new String[]{"SIZE_KEY", "SIZE_ID", "SIZE_NAME", "IDNAME"}, new int[]{0, android.R.id.text1, 0, 0});
                    adapterSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    cmbSize.setAdapter(adapterSize);
                    cmbSize.setSelection(lstSize.size()-1, true);
                    // endregion

                    //region -- 存貨層級/SkuLevel --
                    if (dtSkuLevel != null && dtSkuLevel.Rows.size() > 0)
                        dtSkuLevel.Rows.add(drDefaultItem);
                    lstSkuLevel = (List<? extends Map<String, Object>>) dtSkuLevel.toListHashMap();
                    SimpleAdapter adapterSkuLevel = new AnotherSimpleArrayAdapter<>(getContext(), lstSkuLevel, android.R.layout.simple_spinner_item, new String[]{"DATA_ID", "DATA_NAME", "IDNAME"}, new int[]{0, android.R.id.text1, 0});
                    adapterSkuLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    cmbSkuLevel.setAdapter(adapterSkuLevel);
                    cmbSkuLevel.setSelection(lstSkuLevel.size()-1, true);
                    // endregion
                }
            }
        });

        //endregion
    }

    private void setListeners() {
        etMFDDate.setOnClickListener(onClickMfgDate);
        etExpiryDate.setOnClickListener(onClickExpDate);
        btMFDDateClear.setOnClickListener(onClickMFDDateClear);
        btExpiryDateClear.setOnClickListener(onClickExpiryDateClear);
        cmbStorage.setOnItemSelectedListener(onClickStorage);
        cmbItem.setOnItemSelectedListener(onClickItem);
        cmbSize.setOnItemSelectedListener(onClickSize);
        cmbSkuLevel.setOnItemSelectedListener(onClickSkuLevel);
        cmbPoNo.setOnItemSelectedListener(onClickPoNo);
        cmbPoSeq.setOnItemSelectedListener(onClickPoSeq);
        rgRecQRCode.setOnCheckedChangeListener(onCheckRadioGroup);
        etRecScanCode.setOnKeyListener(onKeyRecScanCode);
        etLotID.setOnKeyListener(onKeyLotID);
        ibtnLotIdQRScan.setOnClickListener(onClickQRScan);
        ibtnRecQRScan.setOnClickListener(onClickQRScan);
        btnAddSn.setOnClickListener(onClickAddSn);
        btnAddLodID.setOnClickListener(onClickAddLotId);
        btnRefresh.setOnClickListener(onClickRefresh);
    }

    private View.OnClickListener onClickMFDDateClear = new View.OnClickListener() {
        @Override
        public void onClick(View view) { etMFDDate.setText(""); }
    };

    private View.OnClickListener onClickExpiryDateClear = new View.OnClickListener() {
        @Override
        public void onClick(View view) { etExpiryDate.setText(""); }
    };

    private View.OnClickListener onClickMfgDate = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setMFDDate();
        }
    };

    private View.OnClickListener onClickExpDate = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setExpiryDate();
        }
    };

    public void setMFDDate(){
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
                etMFDDate.setText(sb);
                dialog.cancel();
            }
        });
    }

    public void setExpiryDate(){
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
                etExpiryDate.setText(sb);
                dialog.cancel();
            }
        });
    }

    private RadioGroup.OnCheckedChangeListener onCheckRadioGroup = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (group.getId() == R.id.rgRecQRCode) {
                etRecScanCode.setEnabled(true);
            }
        }
    };

    private View.OnKeyListener onKeyRecScanCode = new View.OnKeyListener() {

        @Override
        public boolean onKey(View view, int keyCode, KeyEvent event) {

            if (keyCode != KeyEvent.KEYCODE_ENTER)
                return false;

            if (event.getAction() == KeyEvent.ACTION_UP) {

                InputMethodManager manager = (InputMethodManager) ((GoodNonReceiptReceiveDetailNewActivity) getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                String strBarCodeType = "";
                String strBarCodeTypeId = "";
                String strBarCodeValue = etRecScanCode.getText().toString().trim();
                String strSkuNum = etLotID.getText().toString().trim();

                if (etRecScanCode.length() == 0) {
                    ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009032); //WAPG009031   請輸入收料條碼 !!
                    etRecScanCode.requestFocus();
                    return true;
                }

                if (radioQRcode.isChecked()) {

                    strBarCodeType = "QRCODE_TYPE";
                    strBarCodeTypeId = "ReceiveQRCode";


                } else if (radioBarcode.isChecked()){

                    strBarCodeType = "BARCODE_TYPE";
                    strBarCodeTypeId = "ReceiveBarCode";

                }

                if (strBarCodeType.length() > 0 && strBarCodeTypeId.length() > 0 && strBarCodeValue.length() > 0 && strSkuNum.length() > 0) {
                    // 同時有存貨編號及收料條碼/At the same time, there are inventory numbers and receipt barcodes
                    getSkuNum("*", skuLevelId, strSkuNum, strBarCodeType, strBarCodeTypeId, strBarCodeValue);
                } else if (strBarCodeType.length() > 0 && strBarCodeTypeId.length() > 0 && strBarCodeValue.length() > 0) {
                    // 只有收料條碼/Only receipt barcode
                    getSkuNum("*", skuLevelId, strBarCodeType, strBarCodeTypeId, strBarCodeValue);
                }

                return true;
            }

            return false;
        }
    };

    private View.OnKeyListener onKeyLotID = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent event) {

            if (keyCode != KeyEvent.KEYCODE_ENTER)
                return false;

            if (event.getAction() == KeyEvent.ACTION_UP) {

                if (etLotID.getText().toString().trim().length() <= 0) {
                    ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009033); //WAPG009033   請輸入存貨編碼 !!
                    etLotID.requestFocus();
                    return true;
                }

                InputMethodManager manager = (InputMethodManager) ((GoodNonReceiptReceiveDetailNewActivity) getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                getSkuNum("*", skuLevelId, etLotID.getText().toString().trim());

                return true;
            }

            return false;
        }
    };

    // 只有輸入條碼/Only enter the barcode
    private void getSkuNum(String strGrId, String strSkuLevel, String strBarType, String strBarTypeId, final String strBarCodeValue) {

        // 條碼拆解/Barcode dismantling
        BModuleObject biObj1 = new BModuleObject();
        biObj1.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIGoodNonReceiptReceivePortal");
        biObj1.setModuleID("BIFetchNonReceiptReceiveByBarCode");
        biObj1.setRequestID("BIFetchNonReceiptReceiveByBarCode");
        biObj1.params = new Vector<>();

        // Input param
        ParameterInfo paramGrId = new ParameterInfo();
        paramGrId.setParameterID(BIGoodNonReceiptReceivePortalParam.GrId);
        paramGrId.setParameterValue(strGrId);
        biObj1.params.add(paramGrId);

        ParameterInfo paramSkuLevel = new ParameterInfo();
        paramSkuLevel.setParameterID(BIGoodNonReceiptReceivePortalParam.SkuLevel);
        paramSkuLevel.setParameterValue(strSkuLevel);
        biObj1.params.add(paramSkuLevel);

        ParameterInfo paramIsReceived = new ParameterInfo();
        paramIsReceived.setParameterID(BIGoodNonReceiptReceivePortalParam.IsReceived);
        paramIsReceived.setParameterValue("N");
        biObj1.params.add(paramIsReceived);

        ParameterInfo paramBarCodeType = new ParameterInfo();
        paramBarCodeType.setParameterID(BIGoodNonReceiptReceivePortalParam.BarCodeType);
        paramBarCodeType.setParameterValue(strBarType);
        biObj1.params.add(paramBarCodeType);

        ParameterInfo paramBarCodeTypeId = new ParameterInfo();
        paramBarCodeTypeId.setParameterID(BIGoodNonReceiptReceivePortalParam.BarCodeTypeId);
        paramBarCodeTypeId.setParameterValue(strBarTypeId);
        biObj1.params.add(paramBarCodeTypeId);

        ParameterInfo paramBarCodeValue = new ParameterInfo();
        paramBarCodeValue.setParameterID(BIGoodNonReceiptReceivePortalParam.BarCodeValue);
        paramBarCodeValue.setParameterValue(strBarCodeValue);
        biObj1.params.add(paramBarCodeValue);

        List<BGoodReceiptReceiveNonSheetWithPackingInfoParam.GrNonSheetWithPackingInfoMasterObj> lstGrMstObj = new ArrayList<>();
        BGoodReceiptReceiveNonSheetWithPackingInfoParam sheet = new BGoodReceiptReceiveNonSheetWithPackingInfoParam();

        // region -- 設置GrrMstObj/Setting GrrMstObj --
        for (DataRow dr : dtReceiptMst.Rows) {
            BGoodReceiptReceiveNonSheetWithPackingInfoParam.GrNonSheetWithPackingInfoMasterObj sheetMst = sheet.new GrNonSheetWithPackingInfoMasterObj();
            sheetMst.setGrTypeID(dr.getValue("GR_TYPE_ID").toString());
            sheetMst.setGrTypeKey(dr.getValue("GR_TYPE_KEY").toString());
            sheetMst.setGrSource(dr.getValue("GR_SOURCE").toString());
            sheetMst.setVendorID(dr.getValue("VENDOR_ID").toString());
            sheetMst.setVendorKey(dr.getValue("VENDOR_KEY").toString());
            sheetMst.setVendorShipNo(dr.getValue("VENDOR_SHIP_NO").toString());
            sheetMst.setVendorShipDate(dr.getValue("VENDOR_SHIP_DATE").toString());
            sheetMst.setCustomerID(dr.getValue("CUSTOMER_ID").toString());
            sheetMst.setCustomerKey(dr.getValue("CUSTOMER_KEY").toString());
            lstGrMstObj.add(sheetMst);
        }

        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.INV.Parameter.GrNonSheetWithPackingInfoMasterObj", "bmWMS.INV.Param");
        MList mListEnum = new MList(vListEnum);
        String strGrrMstObj = mListEnum.generateFinalCode(lstGrMstObj);
        // endregion

        ParameterInfo paramGrMstObj = new ParameterInfo();
        paramGrMstObj.setParameterID(BIGoodNonReceiptReceivePortalParam.GrrMasterObj);
        paramGrMstObj.setNetParameterValue(strGrrMstObj);
        biObj1.params.add(paramGrMstObj);

        etRecScanCode.getText().clear(); // 先清除，若拆解成功再回填/Clear first, then backfill if dismantled successfully

        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).CallBIModule(biObj1, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {

                if (((GoodNonReceiptReceiveDetailNewActivity)getActivity()).CheckBModuleReturnInfo(bModuleReturn)) {

                    String askChangeSkuLevel = "";

                    if (bModuleReturn.getReturnList().get("BIFetchNonReceiptReceiveByBarCode") != null) {

                        if (bModuleReturn.getReturnList().get("BIFetchNonReceiptReceiveByBarCode").get(BIGoodNonReceiptReceivePortalParam.ChangeSkuLevel) != null) {
                            askChangeSkuLevel = bModuleReturn.getReturnList().get("BIFetchNonReceiptReceiveByBarCode").get(BIGoodNonReceiptReceivePortalParam.ChangeSkuLevel).toString().replaceAll("\"", "");
                        }

                        if (!askChangeSkuLevel.equals("")) {

                            final String [] skuData = askChangeSkuLevel.split(";");
                            Object[] args = new Object[2];
                            args[0] = skuData[0];
                            args[1] = skuData[1];

                            String showMsg = String.format(((GoodNonReceiptReceiveDetailNewActivity) getActivity()).getResString(getResources().getString(R.string.CHANGE_SKU_LEVEL)), args);

                            ((GoodNonReceiptReceiveDetailNewActivity) getActivity()).ShowMessage(showMsg, new ShowMessageEvent() {
                                @Override
                                public void onDismiss() {

                                    // region -- 設置 cmbSkuLevel --
                                    int levelPosition = 0;
                                    skuLevelId = skuData[0];

                                    for (Map<String, Object> levelMap : lstSkuLevel) {
                                        if (levelMap.get("DATA_ID").toString().equals(skuLevelId))
                                            break;
                                        levelPosition++;
                                    }

                                    if (levelPosition == lstSkuLevel.size()) {
                                        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009027); //WAPG009027   查無物料代碼[%s]!
                                        return;
                                    }

                                    SimpleAdapter adapterSkuLevel = new AnotherSimpleArrayAdapter<>(getContext(), lstSkuLevel, android.R.layout.simple_spinner_item, new String[]{"DATA_ID", "DATA_NAME", "IDNAME"}, new int[]{0, android.R.id.text1, 0});
                                    adapterSkuLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    cmbSkuLevel.setAdapter(adapterSkuLevel);
                                    cmbSkuLevel.setSelection(levelPosition, true);
                                    // endregion

                                    etLotID.setText(skuData[1]);

                                    getSkuNum("*", skuData[0], skuData[1]);
                                }
                            });
                        }

                    } else {

                        refreshInsertData(true, true);

                        DataTable dtBarCodeData = bModuleReturn.getReturnJsonTables().get("BIFetchNonReceiptReceiveByBarCode").get("BARCODE_DATA");
                        DataTable dtExtendData = bModuleReturn.getReturnJsonTables().get("BIFetchNonReceiptReceiveByBarCode").get("EXTEND_DATA");
                        dtOrgWgrData = bModuleReturn.getReturnJsonTables().get("BIFetchNonReceiptReceiveByBarCode").get("dtOrgWgrData");
                        dtOrgSn = bModuleReturn.getReturnJsonTables().get("BIFetchNonReceiptReceiveByBarCode").get("dtSn");

                        _addMode = (dtOrgWgrData != null && dtOrgWgrData.Rows.size() > 0) ? "WGR" : "MGR";

                        String itemId = "";

                        if (dtOrgWgrData == null || dtOrgWgrData.Rows.size() == 0) {

                            //region 主要條碼資料填值/Fill in the main barcode data
                            for (DataRow row : dtBarCodeData.Rows) {

                                switch (row.getValue("BARCODE_VARIABLE_ID").toString()) {

                                    case "ITEM_ID":
                                        itemId = row.getValue("BARCODE_VALUE").toString();
                                        // region -- 設置 cmbItem/Setting cmbItem --
                                        int itemPosition = 0;

                                        for (Map<String, Object> itemMap : lstItem) {
                                            if (itemMap.get("ITEM_ID").toString().equals(itemId))
                                                break;
                                            itemPosition++;
                                        }

                                        if (itemPosition == lstItem.size()) {
                                            ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009027); //WAPG009027   查無物料代碼[%s]!
                                            return;
                                        }

                                        SimpleAdapter adapterItem = new AnotherSimpleArrayAdapter<>(getContext(), lstItem, android.R.layout.simple_spinner_item, new String[]{"ITEM_KEY", "ITEM_ID", "ITEM_NAME", "REGISTER_TYPE", "IDNAME"}, new int[]{0, android.R.id.text1, 0, 0, 0});
                                        adapterItem.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        cmbItem.setAdapter(adapterItem);
                                        cmbItem.setSelection(itemPosition, true);
                                        // endregion
                                        cmbItem.setEnabled(false);
                                        break;

                                    case "REGISTER_ID":
                                        etLotID.setText(row.getValue("BARCODE_VALUE").toString());
                                        etLotID.setEnabled(false);
                                        ibtnLotIdQRScan.setEnabled(false);
                                        break;

                                    case "QTY":
                                        etQty.setText(row.getValue("BARCODE_VALUE").toString());
                                        etQty.setEnabled(false);
                                        break;

                                    case "LOT_CODE":
                                        etLotCode.setText(row.getValue("BARCODE_VALUE").toString());
                                        etLotCode.setEnabled(false);
                                        break;

                                    case "MFG_DATE":
                                        etMFDDate.setText(row.getValue("BARCODE_VALUE").toString());
                                        etMFDDate.setEnabled(false);
                                        btMFDDateClear.setEnabled(false);
                                        break;

                                    case "EXP_DATE":
                                        etExpiryDate.setText(row.getValue("BARCODE_VALUE").toString());
                                        etExpiryDate.setEnabled(false);
                                        btExpiryDateClear.setEnabled(false);
                                        break;

                                    case "SIZE_ID":
                                        sizeId = row.getValue("BARCODE_VALUE").toString();
                                        // region -- 設置 cmbSize/Setting cmbSize --
                                        int sizePosition = 0;

                                        if (sizeId.length() <= 0) {
                                            sizePosition = lstSize.size() - 1;
                                        } else {
                                            for (Map<String, Object> sizeMap : lstSize) {
                                                if (sizeMap.get("SIZE_ID").toString().equals(sizeId))
                                                    break;
                                                sizePosition++;
                                            }
                                        }

                                        if (sizePosition == lstSize.size()) {
                                            ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009028); //WAPG009028   查無尺寸代碼[%s]!
                                            return;
                                        }

                                        SimpleAdapter adapterSize = new AnotherSimpleArrayAdapter<>(getContext(), lstSize, android.R.layout.simple_spinner_item, new String[]{"SIZE_KEY", "SIZE_ID", "SIZE_NAME", "IDNAME"}, new int[]{0, android.R.id.text1, 0, 0});
                                        adapterSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        cmbSize.setAdapter(adapterSize);
                                        cmbSize.setSelection(sizePosition, true);
                                        // endregion
                                        cmbSize.setEnabled(false);
                                        break;

                                    case "VENDOR_ITEM_ID":
                                        vendorItemId = row.getValue("BARCODE_VALUE").toString();
                                        break;

                                    case "CMT":
                                        etCmt.setText(row.getValue("BARCODE_VALUE").toString());
                                        etCmt.setEnabled(false);
                                        break;

                                    case "PO_NO":
                                        etPONo.setText(row.getValue("BARCODE_VALUE").toString());
                                        etPONo.setEnabled(false);
                                        break;

                                    case "PO_SEQ":
                                        etPOSeq.setText(row.getValue("BARCODE_VALUE").toString());
                                        etPOSeq.setEnabled(false);
                                        break;

                                    default:
                                        break;
                                }
                            }
                            //endregion

                        }
                        else {

                            setEnable(false, false, true);

                            if (skuLevelId.equals("Entity")) {

                                changePoWidget(true);

                                // region -- 設置 PoNo/Setting PoNo --
                                lstPoNo = new ArrayList<>();
                                for (DataRow dr : dtOrgWgrData.Rows) {
                                    String poNo = dr.getValue("PO_NO").toString();
                                    if (!lstPoNo.contains(poNo))
                                        lstPoNo.add(poNo);
                                }

                                ArrayAdapter adapterPoNo = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, lstPoNo);
                                adapterPoNo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                cmbPoNo.setAdapter(adapterPoNo);
                                cmbPoNo.setSelection(0, true);
                                // endregion

                            } else {

                                changePoWidget(false);

                                //String skuNum = dtWgrDataGroup.Rows.get(0).getValue("SKU_NUM").toString().trim();
                                String poNo = dtWgrDataGroup.Rows.get(0).getValue("PO_NO").toString().trim();
                                String poSeqDisplay = dtWgrDataGroup.Rows.get(0).getValue("PO_SEQ_DISPLAY").toString().trim();
                                itemId = dtWgrDataGroup.Rows.get(0).getValue("ITEM_ID").toString().trim();
                                String lotId = dtWgrDataGroup.Rows.get(0).getValue("LOT_ID").toString().trim();
                                String qty = dtWgrDataGroup.Rows.get(0).getValue("QTY").toString().trim();
                                String mfgDate = dtWgrDataGroup.Rows.get(0).getValue("MFG_DATE").toString().trim().substring(0,10) + " 00:00:00";
                                String expDate = dtWgrDataGroup.Rows.get(0).getValue("EXP_DATE").toString().trim().substring(0,10) + " 23:59:59";
                                String lotCode = dtWgrDataGroup.Rows.get(0).getValue("LOT_CODE").toString().trim();
                                String sizeId = dtWgrDataGroup.Rows.get(0).getValue("SIZE_ID").toString().trim();
                                String cmt = dtWgrDataGroup.Rows.get(0).getValue("CMT").toString().trim();
                                String box1Id = dtWgrDataGroup.Rows.get(0).getValue("BOX1_ID").toString().trim();
                                String box2Id = dtWgrDataGroup.Rows.get(0).getValue("BOX2_ID").toString().trim();
                                String box3Id = dtWgrDataGroup.Rows.get(0).getValue("BOX3_ID").toString().trim();
                                String palletId = dtWgrDataGroup.Rows.get(0).getValue("PALLET_ID").toString().trim();

                                //region 顯示資料 - 中間表 (已 group by) 填值/Display data - intermediate table (group by) filled value
                                if (itemId.length() > 0) {

                                    // region -- 設置 cmbItem/Setting cmbItem --
                                    int itemPosition = 0;

                                    for (Map<String, Object> itemMap : lstItem) {
                                        if (itemMap.get("ITEM_ID").toString().equals(itemId))
                                            break;
                                        itemPosition++;
                                    }

                                    if (itemPosition == lstItem.size()) {
                                        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009027); //WAPG009027   查無物料代碼[%s]!
                                        return;
                                    }

                                    SimpleAdapter adapterItem = new AnotherSimpleArrayAdapter<>(getContext(), lstItem, android.R.layout.simple_spinner_item, new String[]{"ITEM_KEY", "ITEM_ID", "ITEM_NAME", "REGISTER_TYPE", "IDNAME"}, new int[]{0, android.R.id.text1, 0, 0, 0});
                                    adapterItem.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    cmbItem.setAdapter(adapterItem);
                                    cmbItem.setSelection(itemPosition, true);
                                    regType = lstItem.get(itemPosition).get("REGISTER_TYPE").toString();
                                    // endregion

                                }

                                if (poNo.length() > 0) {
                                    etPONo.setText(poNo);
                                }

                                if (poSeqDisplay.length() > 0) {
                                    etPOSeq.setText(poSeqDisplay);
                                }

                                if (qty.length() > 0) {
                                    etQty.setText(qty);
                                }

                                if (lotCode.length() > 0) {
                                    etLotCode.setText(lotCode);
                                }

                                if (mfgDate.length() > 0) {
                                    etMFDDate.setText(mfgDate);
                                }

                                if (expDate.length() > 0) {
                                    etExpiryDate.setText(expDate);
                                }

                                if (sizeId.length() > 0) {

                                    // region -- 設置 cmbSize/Setting cmbSize --
                                    int sizePosition = 0;

                                    if (sizeId.length() <= 0) {
                                        sizePosition = lstSize.size() - 1;
                                    } else {
                                        for (Map<String, Object> sizeMap : lstSize) {
                                            if (sizeMap.get("SIZE_ID").toString().equals(sizeId))
                                                break;
                                            sizePosition++;
                                        }
                                    }

                                    if (sizePosition == lstSize.size()) {
                                        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009028); //WAPG009028   查無尺寸代碼[%s]!
                                        return;
                                    }

                                    SimpleAdapter adapterSize = new AnotherSimpleArrayAdapter<>(getContext(), lstSize, android.R.layout.simple_spinner_item, new String[]{"SIZE_KEY", "SIZE_ID", "SIZE_NAME", "IDNAME"}, new int[]{0, android.R.id.text1, 0, 0});
                                    adapterSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    cmbSize.setAdapter(adapterSize);
                                    cmbSize.setSelection(sizePosition, true);
                                    // endregion

                                }

                                if (cmt.length() > 0) {
                                    etCmt.setText(cmt);
                                }

                                //endregion

                            }
                        }

                        //region Extend
                        if (dtExtendData != null && dtExtendData.Rows.size() > 0) {

                            LayoutInflater inflater = getActivity().getLayoutInflater();
                            GoodReceiptReceiveExtendGridAdapter adapter = new GoodReceiptReceiveExtendGridAdapter(dtExtendData, inflater);
                            lvRecExtend.setAdapter(adapter);

                            tvRecExtendTitle.setVisibility(View.VISIBLE);
                            lvRecExtend.setVisibility(View.VISIBLE);
                        } else {
                            tvRecExtendTitle.setVisibility(View.GONE);
                            lvRecExtend.setVisibility(View.GONE);
                        }
                        //endregion

                        // region 回填條碼資料回EditText/Backfill barcode information back to Edit Text
                        etRecScanCode.setText(strBarCodeValue);
                        // endregion

                        //region 同步 PcsSN 的表/Synchronize the table of PcsSN
                        if (itemId.equals(""))
                            return;

                        if (regType.equals("PcsSN")) {

                            List<String> lstRefKeys = new ArrayList<>();

                            for (int i = 0; i < dtOrgWgrData.Rows.size(); i++) {
                                String refKey = dtOrgWgrData.Rows.get(i).getValue("GRR_DET_SN_REF_KEY").toString().trim();

                                if (refKey.equals(""))
                                    continue;

                                if (!lstRefKeys.contains(refKey))
                                    lstRefKeys.add(refKey);
                            }

                            if (lstRefKeys.size() == 0)
                                return;

                            String[] aryRefKeys = new String[lstRefKeys.size()];
                            lstRefKeys.toArray(aryRefKeys);

                            for(DataRow dr : dtOrgSn.Rows) {
                                DataRow drNew = dtSn.newRow();
                                drNew.setValue("GRR_DET_SN_REF_KEY", dr.get("GRR_DET_SN_REF_KEY").toString().trim());
                                drNew.setValue("SN_ID", dr.get("SN_ID").toString().trim());
                                dtSn.Rows.add(drNew);
                            }

                            grrDetRefKey = aryRefKeys;
                            btnAddSn.setText(getResources().getString(R.string.GR_SN) + " (" + dtSn.Rows.size() + ")");

                        }
                        //endregion

                    }
                }
            }
        });
    }

    // 只有輸入存貨編碼/Only enter the stock code
    private void getSkuNum(String strGrId, String strSkuLevel, String strSkuNum) {

        BModuleObject biObj1 = new BModuleObject();
        biObj1.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIGoodNonReceiptReceivePortal");
        biObj1.setModuleID("BIFetchNonReceiptReceiveBySkuNum");
        biObj1.setRequestID("BIFetchNonReceiptReceiveBySkuNum");
        biObj1.params = new Vector<>();

        // Input param
        ParameterInfo paramGrId = new ParameterInfo();
        paramGrId.setParameterID(BIGoodNonReceiptReceivePortalParam.GrId);
        paramGrId.setParameterValue(strGrId);
        biObj1.params.add(paramGrId);

        ParameterInfo paramSkuLevel = new ParameterInfo();
        paramSkuLevel.setParameterID(BIGoodNonReceiptReceivePortalParam.SkuLevel);
        paramSkuLevel.setParameterValue(strSkuLevel);
        biObj1.params.add(paramSkuLevel);

        ParameterInfo paramSkuNum = new ParameterInfo();
        paramSkuNum.setParameterID(BIGoodNonReceiptReceivePortalParam.SkuNum);
        paramSkuNum.setParameterValue(strSkuNum);
        biObj1.params.add(paramSkuNum);

        ParameterInfo paramIsReceived = new ParameterInfo();
        paramIsReceived.setParameterID(BIGoodNonReceiptReceivePortalParam.IsReceived);
        paramIsReceived.setParameterValue("N");
        biObj1.params.add(paramIsReceived);

        List<BGoodReceiptReceiveNonSheetWithPackingInfoParam.GrNonSheetWithPackingInfoMasterObj> lstGrMstObj = new ArrayList<>();
        BGoodReceiptReceiveNonSheetWithPackingInfoParam sheet = new BGoodReceiptReceiveNonSheetWithPackingInfoParam();

        // region -- 設置GrrMstObj/Setting GrrMstObj --
        for (DataRow dr : dtReceiptMst.Rows) {
            BGoodReceiptReceiveNonSheetWithPackingInfoParam.GrNonSheetWithPackingInfoMasterObj sheetMst = sheet.new GrNonSheetWithPackingInfoMasterObj();
            sheetMst.setGrTypeID(dr.getValue("GR_TYPE_ID").toString());
            sheetMst.setGrTypeKey(dr.getValue("GR_TYPE_KEY").toString());
            sheetMst.setGrSource(dr.getValue("GR_SOURCE").toString());
            sheetMst.setVendorID(dr.getValue("VENDOR_ID").toString());
            sheetMst.setVendorKey(dr.getValue("VENDOR_KEY").toString());
            sheetMst.setVendorShipNo(dr.getValue("VENDOR_SHIP_NO").toString());
            sheetMst.setVendorShipDate(dr.getValue("VENDOR_SHIP_DATE").toString());
            sheetMst.setCustomerID(dr.getValue("CUSTOMER_ID").toString());
            sheetMst.setCustomerKey(dr.getValue("CUSTOMER_KEY").toString());
            lstGrMstObj.add(sheetMst);
        }

        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.INV.Parameter.GrNonSheetWithPackingInfoMasterObj", "bmWMS.INV.Param");
        MList mListEnum = new MList(vListEnum);
        String strGrrMstObj = mListEnum.generateFinalCode(lstGrMstObj);
        // endregion

        ParameterInfo paramGrMstObj = new ParameterInfo();
        paramGrMstObj.setParameterID(BIGoodNonReceiptReceivePortalParam.GrrMasterObj);
        paramGrMstObj.setNetParameterValue(strGrrMstObj);
        biObj1.params.add(paramGrMstObj);

        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).CallBIModule(biObj1, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {

                if (((GoodNonReceiptReceiveDetailNewActivity)getActivity()).CheckBModuleReturnInfo(bModuleReturn)) {

                    String askChangeSkuLevel = "";

                    if (bModuleReturn.getReturnList().get("BIFetchNonReceiptReceiveBySkuNum") != null) {

                        if (bModuleReturn.getReturnList().get("BIFetchNonReceiptReceiveBySkuNum").get(BIGoodNonReceiptReceivePortalParam.ChangeSkuLevel) != null) {
                            askChangeSkuLevel = bModuleReturn.getReturnList().get("BIFetchNonReceiptReceiveBySkuNum").get(BIGoodNonReceiptReceivePortalParam.ChangeSkuLevel).toString().replaceAll("\"", "");
                        }

                        if (!askChangeSkuLevel.equals("")) {

                            final String [] skuData = askChangeSkuLevel.split(";");
                            Object[] args = new Object[2];
                            args[0] = skuData[0];
                            args[1] = skuData[1];

                            String showMsg = String.format(((GoodNonReceiptReceiveDetailNewActivity) getActivity()).getResString(getResources().getString(R.string.CHANGE_SKU_LEVEL)), args);

                            ((GoodNonReceiptReceiveDetailNewActivity) getActivity()).ShowMessage(showMsg, new ShowMessageEvent() {
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
                                        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009027); //WAPG009027   查無物料代碼[%s]!
                                        return;
                                    }

                                    SimpleAdapter adapterSkuLevel = new AnotherSimpleArrayAdapter<>(getContext(), lstSkuLevel, android.R.layout.simple_spinner_item, new String[]{"DATA_ID", "DATA_NAME", "IDNAME"}, new int[]{0, android.R.id.text1, 0});
                                    adapterSkuLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    cmbSkuLevel.setAdapter(adapterSkuLevel);
                                    cmbSkuLevel.setSelection(levelPosition, true);
                                    // endregion

                                    etLotID.setText(skuData[1]);

                                    getSkuNum("*", skuData[0], skuData[1]);
                                }
                            });
                        }

                    } else {

                        refreshInsertData(true, true);
                        setEnable(false, false, true);

                        dtWgrDataGroup = bModuleReturn.getReturnJsonTables().get("BIFetchNonReceiptReceiveBySkuNum").get("dtWgrWithSkuLevel");
                        dtOrgWgrData = bModuleReturn.getReturnJsonTables().get("BIFetchNonReceiptReceiveBySkuNum").get("dtOrgWgrData");
                        dtOrgSn = bModuleReturn.getReturnJsonTables().get("BIFetchNonReceiptReceiveBySkuNum").get("dtSn");

                        _addMode = (dtOrgWgrData != null && dtOrgWgrData.Rows.size() > 0) ? "WGR" : "MGR";

                        String skuLevel = dtWgrDataGroup.Rows.get(0).getValue("SKU_LEVEL").toString().trim();
                        String skuNum = dtWgrDataGroup.Rows.get(0).getValue("SKU_NUM").toString().trim();

                        if (skuNum.length() > 0) {
                            etLotID.setText(skuNum);
                        }

                        if (skuLevel.equals("Entity")) {

                            changePoWidget(true);

                            // region -- 設置 PoNo/Setting PoNo --
                            lstPoNo = new ArrayList<>();
                            for (DataRow dr : dtOrgWgrData.Rows) {
                                String poNo = dr.getValue("PO_NO").toString();
                                if (!lstPoNo.contains(poNo))
                                    lstPoNo.add(poNo);
                            }

                            ArrayAdapter adapterPoNo = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, lstPoNo);
                            adapterPoNo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            cmbPoNo.setAdapter(adapterPoNo);
                            cmbPoNo.setSelection(0, true);
                            // endregion

                        } else {

                            changePoWidget(false);

                            //String skuNum = dtWgrDataGroup.Rows.get(0).getValue("SKU_NUM").toString().trim();
                            String poNo = dtWgrDataGroup.Rows.get(0).getValue("PO_NO").toString().trim();
                            String poSeqDisplay = dtWgrDataGroup.Rows.get(0).getValue("PO_SEQ_DISPLAY").toString().trim();
                            String itemId = dtWgrDataGroup.Rows.get(0).getValue("ITEM_ID").toString().trim();
                            String lotId = dtWgrDataGroup.Rows.get(0).getValue("LOT_ID").toString().trim();
                            String qty = dtWgrDataGroup.Rows.get(0).getValue("QTY").toString().trim();
                            String mfgDate = dtWgrDataGroup.Rows.get(0).getValue("MFG_DATE").toString().trim().substring(0,10) + " 00:00:00";
                            String expDate = dtWgrDataGroup.Rows.get(0).getValue("EXP_DATE").toString().trim().substring(0,10) + " 23:59:59";
                            String lotCode = dtWgrDataGroup.Rows.get(0).getValue("LOT_CODE").toString().trim();
                            String sizeId = dtWgrDataGroup.Rows.get(0).getValue("SIZE_ID").toString().trim();
                            String cmt = dtWgrDataGroup.Rows.get(0).getValue("CMT").toString().trim();
                            String box1Id = dtWgrDataGroup.Rows.get(0).getValue("BOX1_ID").toString().trim();
                            String box2Id = dtWgrDataGroup.Rows.get(0).getValue("BOX2_ID").toString().trim();
                            String box3Id = dtWgrDataGroup.Rows.get(0).getValue("BOX3_ID").toString().trim();
                            String palletId = dtWgrDataGroup.Rows.get(0).getValue("PALLET_ID").toString().trim();

                            //region 顯示資料 - 中間表 (已 group by) 填值/Display data - intermediate table (group by) filled value
                            if (itemId.length() > 0) {

                                // region -- 設置 cmbItem --
                                int itemPosition = 0;

                                for (Map<String, Object> itemMap : lstItem) {
                                    if (itemMap.get("ITEM_ID").toString().equals(itemId))
                                        break;
                                    itemPosition++;
                                }

                                if (itemPosition == lstItem.size()) {
                                    ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009027); //WAPG009027   查無物料代碼[%s]!
                                    return;
                                }

                                SimpleAdapter adapterItem = new AnotherSimpleArrayAdapter<>(getContext(), lstItem, android.R.layout.simple_spinner_item, new String[]{"ITEM_KEY", "ITEM_ID", "ITEM_NAME", "REGISTER_TYPE", "IDNAME"}, new int[]{0, android.R.id.text1, 0, 0, 0});
                                adapterItem.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                cmbItem.setAdapter(adapterItem);
                                cmbItem.setSelection(itemPosition, true);
                                regType = lstItem.get(itemPosition).get("REGISTER_TYPE").toString();
                                // endregion

                            }

                            if (poNo.length() > 0) {
                                etPONo.setText(poNo);
                            }

                            if (poSeqDisplay.length() > 0) {
                                etPOSeq.setText(poSeqDisplay);
                            }

                            if (qty.length() > 0) {
                                etQty.setText(qty);
                            }

                            if (lotCode.length() > 0) {
                                etLotCode.setText(lotCode);
                            }

                            if (mfgDate.length() > 0) {
                                etMFDDate.setText(mfgDate);
                            }

                            if (expDate.length() > 0) {
                                etExpiryDate.setText(expDate);
                            }

                            if (sizeId.length() > 0) {

                                // region -- 設置 cmbSize --
                                int sizePosition = 0;

                                if (sizeId.length() <= 0) {
                                    sizePosition = lstSize.size() - 1;
                                } else {
                                    for (Map<String, Object> sizeMap : lstSize) {
                                        if (sizeMap.get("SIZE_ID").toString().equals(sizeId))
                                            break;
                                        sizePosition++;
                                    }
                                }

                                if (sizePosition == lstSize.size()) {
                                    ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009028); //WAPG009028   查無尺寸代碼[%s]!
                                    return;
                                }

                                SimpleAdapter adapterSize = new AnotherSimpleArrayAdapter<>(getContext(), lstSize, android.R.layout.simple_spinner_item, new String[]{"SIZE_KEY", "SIZE_ID", "SIZE_NAME", "IDNAME"}, new int[]{0, android.R.id.text1, 0, 0});
                                adapterSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                cmbSize.setAdapter(adapterSize);
                                cmbSize.setSelection(sizePosition, true);
                                // endregion

                            }

                            if (cmt.length() > 0) {
                                etCmt.setText(cmt);
                            }

                            //endregion

                            //region 同步 PcsSN 的表/Synchronize the table of PcsSN
                            if (itemId.equals(""))
                                return;

                            if (regType.equals("PcsSN")) {

                                List<String> lstRefKeys = new ArrayList<>();

                                for (int i = 0; i < dtOrgWgrData.Rows.size(); i++) {
                                    String refKey = dtOrgWgrData.Rows.get(i).getValue("GRR_DET_SN_REF_KEY").toString().trim();

                                    if (refKey.equals(""))
                                        continue;

                                    if (!lstRefKeys.contains(refKey))
                                        lstRefKeys.add(refKey);
                                }

                                if (lstRefKeys.size() == 0)
                                    return;

                                String[] aryRefKeys = new String[lstRefKeys.size()];
                                lstRefKeys.toArray(aryRefKeys);

                                for(DataRow dr : dtOrgSn.Rows) {
                                    DataRow drNew = dtSn.newRow();
                                    drNew.setValue("GRR_DET_SN_REF_KEY", dr.get("GRR_DET_SN_REF_KEY").toString().trim());
                                    drNew.setValue("SN_ID", dr.get("SN_ID").toString().trim());
                                    dtSn.Rows.add(drNew);
                                }

                                grrDetRefKey = aryRefKeys;
                                btnAddSn.setText(getResources().getString(R.string.GR_SN) + " (" + dtSn.Rows.size() + ")");

                            }
                            //endregion
                        }
                    }
                }
            }
        });

    }

    // 輸入條碼 & 存貨編碼/Enter barcode & inventory code
    private void getSkuNum(String strGrId, String strSkuLevel, String strSkuNum, String strBarType, String strBarTypeId, final String strBarCodeValue) {

        BModuleObject biObj1 = new BModuleObject();
        biObj1.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIGoodNonReceiptReceivePortal");
        biObj1.setModuleID("BIFetchNonReceiptReceiveBySkuNumAndBarCode");
        biObj1.setRequestID("BIFetchNonReceiptReceiveBySkuNumAndBarCode");
        biObj1.params = new Vector<>();

        // Input param
        ParameterInfo paramGrId = new ParameterInfo();
        paramGrId.setParameterID(BIGoodNonReceiptReceivePortalParam.GrId);
        paramGrId.setParameterValue(strGrId);
        biObj1.params.add(paramGrId);

        ParameterInfo paramSkuLevel = new ParameterInfo();
        paramSkuLevel.setParameterID(BIGoodNonReceiptReceivePortalParam.SkuLevel);
        paramSkuLevel.setParameterValue(strSkuLevel);
        biObj1.params.add(paramSkuLevel);

        ParameterInfo paramSkuNum = new ParameterInfo();
        paramSkuNum.setParameterID(BIGoodNonReceiptReceivePortalParam.SkuNum);
        paramSkuNum.setParameterValue(strSkuNum);
        biObj1.params.add(paramSkuNum);

        ParameterInfo paramIsReceived = new ParameterInfo();
        paramIsReceived.setParameterID(BIGoodNonReceiptReceivePortalParam.IsReceived);
        paramIsReceived.setParameterValue("N");
        biObj1.params.add(paramIsReceived);

        ParameterInfo paramBarCodeType = new ParameterInfo();
        paramBarCodeType.setParameterID(BIGoodNonReceiptReceivePortalParam.BarCodeType);
        paramBarCodeType.setParameterValue(strBarType);
        biObj1.params.add(paramBarCodeType);

        ParameterInfo paramBarCodeTypeId = new ParameterInfo();
        paramBarCodeTypeId.setParameterID(BIGoodNonReceiptReceivePortalParam.BarCodeTypeId);
        paramBarCodeTypeId.setParameterValue(strBarTypeId);
        biObj1.params.add(paramBarCodeTypeId);

        ParameterInfo paramBarCodeValue = new ParameterInfo();
        paramBarCodeValue.setParameterID(BIGoodNonReceiptReceivePortalParam.BarCodeValue);
        paramBarCodeValue.setParameterValue(strBarCodeValue);
        biObj1.params.add(paramBarCodeValue);

        List<BGoodReceiptReceiveNonSheetWithPackingInfoParam.GrNonSheetWithPackingInfoMasterObj> lstGrMstObj = new ArrayList<>();
        BGoodReceiptReceiveNonSheetWithPackingInfoParam sheet = new BGoodReceiptReceiveNonSheetWithPackingInfoParam();

        // region -- 設置GrrMstObj --
        for (DataRow dr : dtReceiptMst.Rows) {
            BGoodReceiptReceiveNonSheetWithPackingInfoParam.GrNonSheetWithPackingInfoMasterObj sheetMst = sheet.new GrNonSheetWithPackingInfoMasterObj();
            sheetMst.setGrTypeID(dr.getValue("GR_TYPE_ID").toString());
            sheetMst.setGrTypeKey(dr.getValue("GR_TYPE_KEY").toString());
            sheetMst.setGrSource(dr.getValue("GR_SOURCE").toString());
            sheetMst.setVendorID(dr.getValue("VENDOR_ID").toString());
            sheetMst.setVendorKey(dr.getValue("VENDOR_KEY").toString());
            sheetMst.setVendorShipNo(dr.getValue("VENDOR_SHIP_NO").toString());
            sheetMst.setVendorShipDate(dr.getValue("VENDOR_SHIP_DATE").toString());
            sheetMst.setCustomerID(dr.getValue("CUSTOMER_ID").toString());
            sheetMst.setCustomerKey(dr.getValue("CUSTOMER_KEY").toString());
            lstGrMstObj.add(sheetMst);
        }

        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.INV.Parameter.GrNonSheetWithPackingInfoMasterObj", "bmWMS.INV.Param");
        MList mListEnum = new MList(vListEnum);
        String strGrrMstObj = mListEnum.generateFinalCode(lstGrMstObj);
        // endregion

        ParameterInfo paramGrMstObj = new ParameterInfo();
        paramGrMstObj.setParameterID(BIGoodNonReceiptReceivePortalParam.GrrMasterObj);
        paramGrMstObj.setNetParameterValue(strGrrMstObj);
        biObj1.params.add(paramGrMstObj);

        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).CallBIModule(biObj1, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {

                if (((GoodNonReceiptReceiveDetailNewActivity)getActivity()).CheckBModuleReturnInfo(bModuleReturn)) {

                    String askChangeSkuLevel = "";

                    if (bModuleReturn.getReturnList().get("BIFetchNonReceiptReceiveBySkuNumAndBarCode") != null) {

                        if (bModuleReturn.getReturnList().get("BIFetchNonReceiptReceiveBySkuNumAndBarCode").get(BIGoodNonReceiptReceivePortalParam.ChangeSkuLevel) != null) {
                            askChangeSkuLevel = bModuleReturn.getReturnList().get("BIFetchNonReceiptReceiveBySkuNumAndBarCode").get(BIGoodNonReceiptReceivePortalParam.ChangeSkuLevel).toString().replaceAll("\"", "");
                        }

                        if (!askChangeSkuLevel.equals("")) {

                            final String [] skuData = askChangeSkuLevel.split(";");
                            Object[] args = new Object[2];
                            args[0] = skuData[0];
                            args[1] = skuData[1];

                            String showMsg = String.format(((GoodNonReceiptReceiveDetailNewActivity) getActivity()).getResString(getResources().getString(R.string.CHANGE_SKU_LEVEL)), args);

                            ((GoodNonReceiptReceiveDetailNewActivity) getActivity()).ShowMessage(showMsg, new ShowMessageEvent() {
                                @Override
                                public void onDismiss() {

                                    // region -- 設置 cmbSkuLevel --
                                    int levelPosition = 0;
                                    skuLevelId = skuData[0];

                                    for (Map<String, Object> levelMap : lstSkuLevel) {
                                        if (levelMap.get("DATA_ID").toString().equals(skuLevelId))
                                            break;
                                        levelPosition++;
                                    }

                                    if (levelPosition == lstSkuLevel.size()) {
                                        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009027); //WAPG009027   查無物料代碼[%s]!
                                        return;
                                    }

                                    SimpleAdapter adapterSkuLevel = new AnotherSimpleArrayAdapter<>(getContext(), lstSkuLevel, android.R.layout.simple_spinner_item, new String[]{"DATA_ID", "DATA_NAME", "IDNAME"}, new int[]{0, android.R.id.text1, 0});
                                    adapterSkuLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    cmbSkuLevel.setAdapter(adapterSkuLevel);
                                    cmbSkuLevel.setSelection(levelPosition, true);
                                    // endregion

                                    etLotID.setText(skuData[1]);

                                    getSkuNum("*", skuData[0], skuData[1]);
                                }
                            });
                        }

                    } else {

                        refreshInsertData(true, true);
                        setEnable(false, false, false);

                        dtWgrDataGroup = bModuleReturn.getReturnJsonTables().get("BIFetchNonReceiptReceiveBySkuNumAndBarCode").get("dtWgrWithSkuLevel");
                        dtOrgWgrData = bModuleReturn.getReturnJsonTables().get("BIFetchNonReceiptReceiveBySkuNumAndBarCode").get("dtOrgWgrData");
                        dtOrgSn = bModuleReturn.getReturnJsonTables().get("BIFetchNonReceiptReceiveBySkuNumAndBarCode").get("dtSn");

                        DataTable dtBarCodeData = bModuleReturn.getReturnJsonTables().get("BIFetchNonReceiptReceiveBySkuNumAndBarCode").get("BARCODE_DATA");
                        DataTable dtExtendData = bModuleReturn.getReturnJsonTables().get("BIFetchNonReceiptReceiveBySkuNumAndBarCode").get("EXTEND_DATA");

                        _addMode = (dtOrgWgrData != null && dtOrgWgrData.Rows.size() > 0) ? "WGR" : "MGR";

                        String itemId = "";

                        if (dtOrgWgrData == null || dtOrgWgrData.Rows.size() == 0) {

                            //region 主要條碼資料填值/Fill in the main barcode data
                            for (DataRow row : dtBarCodeData.Rows) {

                                switch (row.getValue("BARCODE_VARIABLE_ID").toString()) {

                                    case "ITEM_ID":
                                        itemId = row.getValue("BARCODE_VALUE").toString();
                                        // region -- 設置 cmbItem --
                                        int itemPosition = 0;

                                        for (Map<String, Object> itemMap : lstItem) {
                                            if (itemMap.get("ITEM_ID").toString().equals(itemId))
                                                break;
                                            itemPosition++;
                                        }

                                        if (itemPosition == lstItem.size()) {
                                            ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009027); //WAPG009027   查無物料代碼[%s]!
                                            return;
                                        }

                                        SimpleAdapter adapterItem = new AnotherSimpleArrayAdapter<>(getContext(), lstItem, android.R.layout.simple_spinner_item, new String[]{"ITEM_KEY", "ITEM_ID", "ITEM_NAME", "REGISTER_TYPE", "IDNAME"}, new int[]{0, android.R.id.text1, 0, 0, 0});
                                        adapterItem.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        cmbItem.setAdapter(adapterItem);
                                        cmbItem.setSelection(itemPosition, true);
                                        // endregion
                                        cmbItem.setEnabled(false);
                                        break;

                                    case "REGISTER_ID":
                                        etLotID.setText(row.getValue("BARCODE_VALUE").toString());
                                        etLotID.setEnabled(false);
                                        ibtnLotIdQRScan.setEnabled(false);
                                        break;

                                    case "QTY":
                                        etQty.setText(row.getValue("BARCODE_VALUE").toString());
                                        etQty.setEnabled(false);
                                        break;

                                    case "LOT_CODE":
                                        etLotCode.setText(row.getValue("BARCODE_VALUE").toString());
                                        etLotCode.setEnabled(false);
                                        break;

                                    case "MFG_DATE":
                                        etMFDDate.setText(row.getValue("BARCODE_VALUE").toString());
                                        etMFDDate.setEnabled(false);
                                        btMFDDateClear.setEnabled(false);
                                        break;

                                    case "EXP_DATE":
                                        etExpiryDate.setText(row.getValue("BARCODE_VALUE").toString());
                                        etExpiryDate.setEnabled(false);
                                        btExpiryDateClear.setEnabled(false);
                                        break;

                                    case "SIZE_ID":
                                        sizeId = row.getValue("BARCODE_VALUE").toString();
                                        // region -- 設置 cmbSize --
                                        int sizePosition = 0;

                                        if (sizeId.length() <= 0) {
                                            sizePosition = lstSize.size() - 1;
                                        } else {
                                            for (Map<String, Object> sizeMap : lstSize) {
                                                if (sizeMap.get("SIZE_ID").toString().equals(sizeId))
                                                    break;
                                                sizePosition++;
                                            }
                                        }

                                        if (sizePosition == lstSize.size()) {
                                            ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009028); //WAPG009028   查無尺寸代碼[%s]!
                                            return;
                                        }

                                        SimpleAdapter adapterSize = new AnotherSimpleArrayAdapter<>(getContext(), lstSize, android.R.layout.simple_spinner_item, new String[]{"SIZE_KEY", "SIZE_ID", "SIZE_NAME", "IDNAME"}, new int[]{0, android.R.id.text1, 0, 0});
                                        adapterSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        cmbSize.setAdapter(adapterSize);
                                        cmbSize.setSelection(sizePosition, true);
                                        // endregion
                                        cmbSize.setEnabled(false);
                                        break;

                                    case "VENDOR_ITEM_ID":
                                        vendorItemId = row.getValue("BARCODE_VALUE").toString();
                                        break;

                                    case "CMT":
                                        etCmt.setText(row.getValue("BARCODE_VALUE").toString());
                                        etCmt.setEnabled(false);
                                        break;

                                    case "PO_NO":
                                        etPONo.setText(row.getValue("BARCODE_VALUE").toString());
                                        etPONo.setEnabled(false);
                                        break;

                                    case "PO_SEQ":
                                        etPOSeq.setText(row.getValue("BARCODE_VALUE").toString());
                                        etPOSeq.setEnabled(false);
                                        break;

                                    default:
                                        break;
                                }
                            }
                            //endregion

                        } else {

                            setEnable(false, false, true);

                            if (skuLevelId.equals("Entity")) {

                                changePoWidget(true);

                                // region -- 設置 PoNo/Setting PoNo --
                                lstPoNo = new ArrayList<>();
                                for (DataRow dr : dtOrgWgrData.Rows) {
                                    String poNo = dr.getValue("PO_NO").toString();
                                    if (!lstPoNo.contains(poNo))
                                        lstPoNo.add(poNo);
                                }

                                ArrayAdapter adapterPoNo = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, lstPoNo);
                                adapterPoNo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                cmbPoNo.setAdapter(adapterPoNo);
                                cmbPoNo.setSelection(0, true);
                                // endregion

                            } else {

                                changePoWidget(false);

                                //String skuNum = dtWgrDataGroup.Rows.get(0).getValue("SKU_NUM").toString().trim();
                                String poNo = dtWgrDataGroup.Rows.get(0).getValue("PO_NO").toString().trim();
                                String poSeqDisplay = dtWgrDataGroup.Rows.get(0).getValue("PO_SEQ_DISPLAY").toString().trim();
                                itemId = dtWgrDataGroup.Rows.get(0).getValue("ITEM_ID").toString().trim();
                                String lotId = dtWgrDataGroup.Rows.get(0).getValue("LOT_ID").toString().trim();
                                String qty = dtWgrDataGroup.Rows.get(0).getValue("QTY").toString().trim();
                                String mfgDate = dtWgrDataGroup.Rows.get(0).getValue("MFG_DATE").toString().trim().substring(0,10) + " 00:00:00";
                                String expDate = dtWgrDataGroup.Rows.get(0).getValue("EXP_DATE").toString().trim().substring(0,10) + " 23:59:59";
                                String lotCode = dtWgrDataGroup.Rows.get(0).getValue("LOT_CODE").toString().trim();
                                String sizeId = dtWgrDataGroup.Rows.get(0).getValue("SIZE_ID").toString().trim();
                                String cmt = dtWgrDataGroup.Rows.get(0).getValue("CMT").toString().trim();
                                String box1Id = dtWgrDataGroup.Rows.get(0).getValue("BOX1_ID").toString().trim();
                                String box2Id = dtWgrDataGroup.Rows.get(0).getValue("BOX2_ID").toString().trim();
                                String box3Id = dtWgrDataGroup.Rows.get(0).getValue("BOX3_ID").toString().trim();
                                String palletId = dtWgrDataGroup.Rows.get(0).getValue("PALLET_ID").toString().trim();

                                //region 顯示資料 - 中間表 (已 group by) 填值/Display data - intermediate table (group by) filled value
                                if (itemId.length() > 0) {

                                    // region -- 設置 cmbItem/Setting cmbItem --
                                    int itemPosition = 0;

                                    for (Map<String, Object> itemMap : lstItem) {
                                        if (itemMap.get("ITEM_ID").toString().equals(itemId))
                                            break;
                                        itemPosition++;
                                    }

                                    if (itemPosition == lstItem.size()) {
                                        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009027); //WAPG009027   查無物料代碼[%s]!
                                        return;
                                    }

                                    SimpleAdapter adapterItem = new AnotherSimpleArrayAdapter<>(getContext(), lstItem, android.R.layout.simple_spinner_item, new String[]{"ITEM_KEY", "ITEM_ID", "ITEM_NAME", "REGISTER_TYPE", "IDNAME"}, new int[]{0, android.R.id.text1, 0, 0, 0});
                                    adapterItem.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    cmbItem.setAdapter(adapterItem);
                                    cmbItem.setSelection(itemPosition, true);
                                    regType = lstItem.get(itemPosition).get("REGISTER_TYPE").toString();
                                    // endregion

                                }

                                if (poNo.length() > 0) {
                                    etPONo.setText(poNo);
                                }

                                if (poSeqDisplay.length() > 0) {
                                    etPOSeq.setText(poSeqDisplay);
                                }

                                if (qty.length() > 0) {
                                    etQty.setText(qty);
                                }

                                if (lotCode.length() > 0) {
                                    etLotCode.setText(lotCode);
                                }

                                if (mfgDate.length() > 0) {
                                    etMFDDate.setText(mfgDate);
                                }

                                if (expDate.length() > 0) {
                                    etExpiryDate.setText(expDate);
                                }

                                if (sizeId.length() > 0) {

                                    // region -- 設置 cmbSize/Setting cmbSize --
                                    int sizePosition = 0;

                                    if (sizeId.length() <= 0) {
                                        sizePosition = lstSize.size() - 1;
                                    } else {
                                        for (Map<String, Object> sizeMap : lstSize) {
                                            if (sizeMap.get("SIZE_ID").toString().equals(sizeId))
                                                break;
                                            sizePosition++;
                                        }
                                    }

                                    if (sizePosition == lstSize.size()) {
                                        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009028); //WAPG009028   查無尺寸代碼[%s]!
                                        return;
                                    }

                                    SimpleAdapter adapterSize = new AnotherSimpleArrayAdapter<>(getContext(), lstSize, android.R.layout.simple_spinner_item, new String[]{"SIZE_KEY", "SIZE_ID", "SIZE_NAME", "IDNAME"}, new int[]{0, android.R.id.text1, 0, 0});
                                    adapterSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    cmbSize.setAdapter(adapterSize);
                                    cmbSize.setSelection(sizePosition, true);
                                    // endregion

                                }

                                if (cmt.length() > 0) {
                                    etCmt.setText(cmt);
                                }

                                //endregion

                            }
                        }


                        //region Extend
                        if (dtExtendData != null && dtExtendData.Rows.size() > 0) {

                            LayoutInflater inflater = getActivity().getLayoutInflater();
                            GoodReceiptReceiveExtendGridAdapter adapter = new GoodReceiptReceiveExtendGridAdapter(dtExtendData, inflater);
                            lvRecExtend.setAdapter(adapter);

                            tvRecExtendTitle.setVisibility(View.VISIBLE);
                            lvRecExtend.setVisibility(View.VISIBLE);
                        } else {
                            tvRecExtendTitle.setVisibility(View.GONE);
                            lvRecExtend.setVisibility(View.GONE);
                        }
                        //endregion

                        // region 回填條碼資料回EditText/Backfill barcode information back to Edit Text
                        etRecScanCode.setText(strBarCodeValue);
                        // endregion

                        //region 同步 PcsSN 的表/Synchronize the table of PcsSN
                        if (itemId.equals(""))
                            return;

                        if (regType.equals("PcsSN")) {

                            List<String> lstRefKeys = new ArrayList<>();

                            for (int i = 0; i < dtOrgWgrData.Rows.size(); i++) {
                                String refKey = dtOrgWgrData.Rows.get(i).getValue("GRR_DET_SN_REF_KEY").toString().trim();

                                if (refKey.equals(""))
                                    continue;

                                if (!lstRefKeys.contains(refKey))
                                    lstRefKeys.add(refKey);
                            }

                            if (lstRefKeys.size() == 0)
                                return;

                            String[] aryRefKeys = new String[lstRefKeys.size()];
                            lstRefKeys.toArray(aryRefKeys);

                            for(DataRow dr : dtOrgSn.Rows) {
                                DataRow drNew = dtSn.newRow();
                                drNew.setValue("GRR_DET_SN_REF_KEY", dr.get("GRR_DET_SN_REF_KEY").toString().trim());
                                drNew.setValue("SN_ID", dr.get("SN_ID").toString().trim());
                                dtSn.Rows.add(drNew);
                            }

                            grrDetRefKey = aryRefKeys;
                            btnAddSn.setText(getResources().getString(R.string.GR_SN) + " (" + dtSn.Rows.size() + ")");

                        }
                        //endregion

                    }
                }
            }
        });
    }

    private Spinner.OnItemSelectedListener onClickStorage = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {

            storageId = "";
            storageKey = "";

            if (position != lstStorage.size()-1) {
                Map<String, String> storageMap = (Map<String, String>)parent.getItemAtPosition(position);
                storageId = storageMap.get("STORAGE_ID");
                storageKey = storageMap.get("STORAGE_KEY");
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            return;
        }
    };

    private Spinner.OnItemSelectedListener onClickItem = new AdapterView.OnItemSelectedListener() {

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {

            itemId = "";
            itemName = "";
            itemKey = "";
            regType = "";

            if (position != lstItem.size()-1) {
                Map<String, String> itemMap = (Map<String, String>)parent.getItemAtPosition(position);
                itemId = itemMap.get("ITEM_ID");
                itemName = itemMap.get("ITEM_NAME");
                itemKey = itemMap.get("ITEM_KEY");
                regType = itemMap.get("REGISTER_TYPE");


                switch (regType) {

                    case "PcsSN":
                        btnAddSn.setEnabled(true);
                        break;

                    default:
                        btnAddSn.setEnabled(false);
                        break;
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            return;
        }
    };

    private Spinner.OnItemSelectedListener onClickSize = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
            sizeKey = "";

            if (position != lstItem.size()-1) {
                Map<String, String> sizeMap = (Map<String, String>)parent.getItemAtPosition(position);
                sizeKey = sizeMap.get("SIZE_KEY");
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private Spinner.OnItemSelectedListener onClickSkuLevel = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
            skuLevelId = "";

            if (position != lstSkuLevel.size()-1) {
                Map<String, String> skuLevelMap = (Map<String, String>)parent.getItemAtPosition(position);
                skuLevelId = skuLevelMap.get("DATA_ID");
            }

            // 若 skuLevelId 為實體，才會出現收料條碼供刷讀/If the skuLevelId is an entity, the receiving barcode will appear for scanning
            if (skuLevelId.equals("Entity"))
                llRecCode.setVisibility(View.VISIBLE);
            else
                llRecCode.setVisibility(View.GONE);

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private Spinner.OnItemSelectedListener onClickPoNo = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {

            poNo = (String) parent.getItemAtPosition(position);

            // region -- 設置 PoSeq/Setting PoSeq --

            DataTable dtSetPoSeq = new DataTable();

            for (int i = 0; i < dtOrgWgrData.Rows.size(); i++) {
                if (dtOrgWgrData.Rows.get(i).getValue("PO_NO").equals(poNo))
                    dtSetPoSeq.Rows.add(dtOrgWgrData.Rows.get(i));
            }

            lstPoSeq = (List<? extends Map<String, Object>>) dtSetPoSeq.toListHashMap();
            SimpleAdapter adapterPoSeq = new SimpleAdapter(getContext(), lstPoSeq, android.R.layout.simple_spinner_dropdown_item, new String[]{"PO_SEQ"}, new int[]{android.R.id.text1});
            adapterPoSeq.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cmbPoSeq.setAdapter(adapterPoSeq);
            cmbPoSeq.setSelection(0, true);
            // endregion
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private Spinner.OnItemSelectedListener onClickPoSeq = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {

            Map<String, String> poSeqMap = (Map<String, String>)parent.getItemAtPosition(position);

            //String skuNum = poSeqMap.get("SKU_NUM").toString().trim();
            String poNo = poSeqMap.get("PO_NO");
            String poSeqDisplay = String.valueOf(poSeqMap.get("PO_SEQ"));
            String itemId = poSeqMap.get("ITEM_ID");
            String lotId = poSeqMap.get("LOT_ID");
            String qty = String.valueOf(poSeqMap.get("QTY"));
            String mfgDate = poSeqMap.get("MFG_DATE").substring(0,10) + " 00:00:00";
            String expDate = poSeqMap.get("EXP_DATE").substring(0,10) + " 23:59:59";
            String lotCode = poSeqMap.get("LOT_CODE");
            String sizeId = poSeqMap.get("SIZE_ID");
            String cmt = poSeqMap.get("CMT");

            //region 顯示資料
            if (itemId.length() > 0) {

                // region -- 設置 cmbItem/Setting cmbItem --
                int itemPosition = 0;

                for (Map<String, Object> itemMap : lstItem) {
                    if (itemMap.get("ITEM_ID").toString().equals(itemId))
                        break;
                    itemPosition++;
                }

                if (itemPosition == lstItem.size()) {
                    ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009027); //WAPG009027   查無物料代碼[%s]!
                    return;
                }

                SimpleAdapter adapterItem = new AnotherSimpleArrayAdapter<>(getContext(), lstItem, android.R.layout.simple_spinner_item, new String[]{"ITEM_KEY", "ITEM_ID", "ITEM_NAME", "REGISTER_TYPE", "IDNAME"}, new int[]{0, android.R.id.text1, 0, 0, 0});
                adapterItem.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                cmbItem.setAdapter(adapterItem);
                cmbItem.setSelection(itemPosition, true);
                regType = lstItem.get(itemPosition).get("REGISTER_TYPE").toString();
                // endregion

            }

            if (lotId.length() > 0) {
                etLotID.setText(lotId);
            }

            if (poNo.length() > 0) {
                etPONo.setText(poNo);
            }

            if (poSeqDisplay.length() > 0) {
                etPOSeq.setText(poSeqDisplay);
            }

            if (qty.length() > 0) {
                etQty.setText(qty);
            }

            if (lotCode.length() > 0) {
                etLotCode.setText(lotCode);
            }

            if (mfgDate.length() > 0) {
                etMFDDate.setText(mfgDate);
            }

            if (expDate.length() > 0) {
                etExpiryDate.setText(expDate);
            }

            if (sizeId.length() > 0) {

                // region -- 設置 cmbSize/Setting cmbSize --
                int sizePosition = 0;

                if (sizeId.length() <= 0) {
                    sizePosition = lstSize.size() - 1;
                } else {
                    for (Map<String, Object> sizeMap : lstSize) {
                        if (sizeMap.get("SIZE_ID").toString().equals(sizeId))
                            break;
                        sizePosition++;
                    }
                }

                if (sizePosition == lstSize.size()) {
                    ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009028); //WAPG009028   查無尺寸代碼[%s]!
                    return;
                }

                SimpleAdapter adapterSize = new AnotherSimpleArrayAdapter<>(getContext(), lstSize, android.R.layout.simple_spinner_item, new String[]{"SIZE_KEY", "SIZE_ID", "SIZE_NAME", "IDNAME"}, new int[]{0, android.R.id.text1, 0, 0});
                adapterSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                cmbSize.setAdapter(adapterSize);
                cmbSize.setSelection(sizePosition, true);
                // endregion

            }

            if (cmt.length() > 0) {
                etCmt.setText(cmt);
            }

            // endregion
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private View.OnClickListener onClickQRScan = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            IntentIntegrator integrator = IntentIntegrator.forSupportFragment(GoodNonReceiptReceiveDataFragment.this);
            // 設置要掃描的條碼類型，ONE_D_CODE_TYPES：一維碼，QR_CODE_TYPES-二維碼/Set the barcode type to be scanned, ONE D_CODE TYPES: one-dimensional code, QR CODE TYPES-two-dimensional code
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("BarCode Scan"); //底部的提示文字/Hint text at the bottom
            integrator.setCameraId(0); //前置(1)或者後置(0)攝像頭/Front (1) or rear (0) camera
            integrator.setBeepEnabled(false); //掃描成功的「嗶嗶」聲/"Beep" sound for successful scan
            integrator.setBarcodeImageEnabled(false); //是否保留掃碼成功時候的截圖/Whether to keep the screenshot when the scan code is successful
            integrator.setCaptureActivity(ScanActivity.class);
            switch (view.getId()) {

                case R.id.ibtnLotIdQRScan:

                    integrator.setRequestCode(LOT_ID_QRSCAN_REQUEST_CODE);
                    break;

                case R.id.ibtnRecQRScan:

                    if (rgRecQRCode.getCheckedRadioButtonId() == -1) {
                        // 沒有選擇使用一維條碼或二維條碼/Do not select to use 1D or 2D barcodes
                        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009029); // WAPG009029    請選擇收料條碼類型
                        return;
                    }

                    integrator.setRequestCode(REC_QRSCAN_REQUEST_CODE);
                    break;

                default:

                    integrator.setRequestCode(0);
                    break;
            }
            integrator.initiateScan();
        }
    };

    private View.OnClickListener onClickAddSn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (etQty.length() <= 0) {
                ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009005); // WAPG009005    請輸入數量
                etQty.requestFocus();
                return;
            }

            String[] strGrrDetSNRerKey;

            if (grrDetRefKey == null || grrDetRefKey.length == 0) {
                Date date = new Date();
                String pattern = "MMddHHmmss.S";
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                strGrrDetSNRerKey = new String[] { sdf.format(date)};
            }
            else
                strGrrDetSNRerKey = grrDetRefKey;

            ShowSNDialog(getContext(), strGrrDetSNRerKey, dtSn, Integer.valueOf(etQty.getText().toString().trim().split("\\.")[0]));

        }
    };

    private void ShowSNDialog(final Context context, final String[] strGrrDetSNRefKey, final DataTable dtSn, int snQty){

        LayoutInflater inflater = LayoutInflater.from(context);
        final View viewDialog = inflater.inflate(R.layout.activity_good_nonreceipt_receive_lot_sn,null );
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setView(viewDialog);

        final android.app.AlertDialog dialog = builder.create();
        dialog.setCancelable(false);//只允許按下dialog裡的按鍵才能關閉dialog/Only the button in the dialog is allowed to close the dialog
        dialog.show();

        final int intSNQty = snQty;

        //region create dtTempSN
        final ArrayList<String> alTempSN = new ArrayList<String>();
        final DataTable dtSnTemp = createDataTableSN();

        if (dtSn.Rows.size() > 0) {
            for (DataRow drClone : dtSn.Rows) {

                String refKey = drClone.getValue("GRR_DET_SN_REF_KEY").toString();
                String snID = drClone.getValue("SN_ID").toString();

                for (String strKey : strGrrDetSNRefKey) {

                    //把 dtSn 符合 GRR_DET_SN_REF_KEY 的資料搬到 dtSnTemp/Move the data of dtSn conforming to GRR_DET_SN_REF_KEY to dtSnTemp
                    if (refKey.equals(strKey)) {
                        DataRow drNew = dtSnTemp.newRow();
                        drNew.setValue("GRR_DET_SN_REF_KEY", drClone.getValue("GRR_DET_SN_REF_KEY").toString());
                        drNew.setValue("SN_ID", drClone.getValue("SN_ID").toString());
                        dtSnTemp.Rows.add(drNew);
                        alTempSN.add(drClone.getValue("SN_ID").toString());
                    }
                }
            }
        }

        LayoutInflater inflaterSNTemp = LayoutInflater.from(context);
        GoodNonreceiptReceiveSNAdapter adapter = new GoodNonreceiptReceiveSNAdapter(dtSnTemp, inflaterSNTemp);
        ListView lsSN = viewDialog.findViewById(R.id.lvReceiveSN);
        lsSN.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        //endregion

        //region 刷入SN/Enter SN
        EditText etSN = viewDialog.findViewById(R.id.edSnId);
        etSN.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
                if (event.getAction() == KeyEvent.ACTION_DOWN){

                    if (_addMode.equals("WGR")) {
                        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009034); //WAPG009034  資料來自中間表，無法修改 !!
                        return false;
                    }

                    EditText etSN = view.findViewById(R.id.edSnId);
                    String snID = etSN.getText().toString().trim();

                    // 判斷SN是否重複/Determine whether the SN is repeated
                    if (alTempSN.contains(snID)){
                        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009015);//WAPG009015    序號重複!
                        return false;
                    }

                    DataRow drNew = dtSnTemp.newRow();
                    drNew.setValue("GRR_DET_SN_REF_KEY",strGrrDetSNRefKey[0]);
                    drNew.setValue("SN_ID",snID);
                    dtSnTemp.Rows.add(drNew);
                    alTempSN.add(snID);

                    // ListView 顯示/Show on ListView
                    LayoutInflater inflater = LayoutInflater.from(context);
                    GoodNonreceiptReceiveSNAdapter adapter = new GoodNonreceiptReceiveSNAdapter(dtSnTemp, inflater);
                    ListView lsSN = viewDialog.findViewById(R.id.lvReceiveSN);
                    lsSN.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    // 清空刷入的欄位/Clear the enter field
                    etSN.getText().clear();
                    return true;
                }
                return false;
            }
        });
        //endregion

        //region ListView 事件/ Event on ListView
        ListView lvSNs = viewDialog.findViewById(R.id.lvReceiveSN);
        lvSNs.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (_addMode.equals("WGR")) {
                    ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009034);  //WAPG009034  資料來自中間表，無法修改 !!
                    return false;
                }

                if (dtSnTemp.Rows.size() > 0){
                    final DataRow chooseRow = dtSnTemp.Rows.get(position);

                    //region 跳出詢問視窗/Jump out of the inquiry window
                    LayoutInflater inflater = LayoutInflater.from(context);
                    final View viewSN = inflater.inflate(R.layout.activity_good_nonreceipt_receive_lot_sn_delete,null );
                    final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                    builder.setView(viewSN);

                    final android.app.AlertDialog dialogSN = builder.create();
                    dialogSN.setCancelable(false);//只允許按下dialog裡的按鍵才能關閉dialog
                    dialogSN.show();

                    TextView tvCurrentSN = dialogSN.findViewById(R.id.tvCurrentSN);
                    String strErr = getResources().getString(R.string.WAPG009008); //WAPG009008    是否刪除此序號?
                    tvCurrentSN.setText(strErr);
                    //endregion

                    //region 確認清除/Confirm Clear
                    Button btnDelete = viewSN.findViewById(R.id.btnYes);
                    btnDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dtSnTemp.Rows.remove(chooseRow);
                            alTempSN.remove(chooseRow.getValue("SN_ID").toString());

                            // Refresh ListView
                            LayoutInflater inflater = LayoutInflater.from(context);
                            GoodNonreceiptReceiveSNAdapter adapter = new GoodNonreceiptReceiveSNAdapter(dtSnTemp, inflater);
                            ListView lsSN = viewDialog.findViewById(R.id.lvReceiveSN);
                            lsSN.setAdapter(adapter);
                            adapter.notifyDataSetChanged();

                            dialogSN.dismiss();
                        }
                    });
                    //endregion

                    //region 取消清除/Clear Cancel
                    Button btnCancel = viewSN.findViewById(R.id.btnNo);
                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialogSN.dismiss();
                        }
                    });
                    //endregion
                }
                return false;
            }
        });
        //endregion

        //region Confirm 按鈕事件/Event on Confirm Button
        Button btnConfirmDialog = viewDialog.findViewById(R.id.btnSNConfirm);
        btnConfirmDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (_addMode.equals("WGR")) {
                    ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009034); //WAPG009034  資料來自中間表，無法修改 !!
                    return;
                }

                if (dtSnTemp.Rows.size() < intSNQty){
                    ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009009);//WAPG009009    未滿需求量!
                    return;
                }
                if(dtSnTemp.Rows.size() > intSNQty){
                    ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009012); //WAPG009012    已超出需求量!
                    return;
                }

                // 先清空dtSn 再把移 dtSnTemp 進去/First clear dtSn and then move dtSnTemp in
                dtSn.Rows.removeAll(dtSn.Rows);

                for (DataRow drClone : dtSnTemp.Rows){
                    DataRow drSnNew = dtSn.newRow();
                    drSnNew.setValue("GRR_DET_SN_REF_KEY", drClone.get("GRR_DET_SN_REF_KEY").toString());
                    drSnNew.setValue("SN_ID",drClone.get("SN_ID").toString());
                    dtSn.Rows.add(drSnNew);
                }
//
//                dtLotAll.Rows.get(intLotNoPosition).setValue("GRR_DET_SN_REF_KEY", dtSnTemp.Rows.get(0).get("GRR_DET_SN_REF_KEY").toString());

                //dtSnTemp = null;

                //dtSnTemp.Rows.removeAll(dtSnTemp.Rows);
                //btnAddSn.setText("收料序號 (" + dtSnTemp.Rows.size() + ")");
                btnAddSn.setText(getResources().getString(R.string.GR_SN) + " (" + dtSnTemp.Rows.size() + ")");

                alTempSN.clear();
                dialog.dismiss();
            }
        });
        //endregion

        //region Cancel 按鈕事件/Event on Cancl Button
        Button btnCloseDialog = viewDialog.findViewById(R.id.btnSNCancel);
        btnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnAddSn.setText(getResources().getString(R.string.GR_SN) + " (" + dtSn.Rows.size() + ")");

                dtSnTemp.Rows.removeAll(dtSnTemp.Rows);
                dialog.dismiss();
            }
        });
        //endregion
    }

    private View.OnClickListener onClickAddLotId = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            //_addMode = (dtOrgWgrData != null && dtOrgWgrData.Rows.size() > 0) ? "WGR" : "MGR";

            String recBarCode, recQrCode;

            // region -- Check input and get input val --

            if (skuLevelId == null || skuLevelId.length() <= 0) {
                ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009035); // WAPG009035    請選擇存貨層級
                cmbSkuLevel.requestFocus();
                return;
            }

            if (storageKey == null || storageKey.length() <= 0) {
                ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009003); // WAPG009003    請選擇倉庫
                cmbStorage.requestFocus();
                return;
            }

            if (itemKey == null || itemKey.length() <= 0) {
                ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009004); // WAPG009004    請選擇物料
                cmbItem.requestFocus();
                return;
            } else {

                skipQc = "";

                //region -- Check QC: 本身供應商+本身物料/Check QC: own supplier + own material --
                for (DataRow drItemQc : dtVendorQcInfo.Rows)
                {
                    if (drItemQc.getValue("VENDOR_KEY").toString().equals(vendorKey) && drItemQc.getValue("ITEM_KEY").toString().equals(itemKey))
                    {
                        skipQc = drItemQc.getValue("SKIP_QC").toString();
                        break;
                    }
                }
                //endregion

                //region -- Check QC: 本身供應商+*物料/Check QC: own supplier+*material --
                if (skipQc.equals(""))
                {
                    for (DataRow drItemQc : dtVendorQcInfo.Rows)
                    {
                        if (drItemQc.getValue("VENDOR_KEY").toString().equals(vendorKey) && drItemQc.getValue("ITEM_KEY").toString().equals("*"))
                        {
                            skipQc = drItemQc.getValue("SKIP_QC").toString();
                            break;
                        }
                    }
                }
                //endregion

                //region -- Check QC: *供應商+本身物料/Check QC: *Supplier + own material --
                if (skipQc.equals(""))
                {
                    for (DataRow drItemQc : dtVendorQcInfo.Rows)
                    {
                        if (drItemQc.getValue("VENDOR_KEY").toString().equals("*") && drItemQc.getValue("ITEM_KEY").toString().equals(itemKey))
                        {
                            skipQc = drItemQc.getValue("SKIP_QC").toString();
                            break;
                        }
                    }
                }
                //endregion

                //region -- Check QC: *供應商+*物料/Check QC: *Supplier+*Material --
                if (skipQc.equals(""))
                {
                    for (DataRow drItemQc : dtVendorQcInfo.Rows)
                    {
                        if (drItemQc.getValue("VENDOR_KEY").toString().equals("*") && drItemQc.getValue("ITEM_KEY").toString().equals("*"))
                        {
                            skipQc = drItemQc.getValue("SKIP_QC").toString();
                            break;
                        }
                    }
                }
                //endregion
            }

            if (etLotID.length() <= 0) {
                ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009036); // WAPG009036    請輸入存貨編號
                etLotID.requestFocus();
                return;
            } else {

                skuNum = etLotID.getText().toString().trim();
                String chkSkuNumExist = skuNum;

                if (regType.equals("ItemID")) {
                    chkSkuNumExist = skuNum + "_" + etPONo.getText().toString().trim() + "_" + etPOSeq.getText().toString().trim();
                }

                if (_detailMode.equals("Add")) {

                    if (lstCheckSkuNum.contains(chkSkuNumExist)) {
                        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009037); // WAPG009037    存貨編號已存在
                        return;
                    }
                }
                else {
                    if (lstCheckSkuNum.contains(chkSkuNumExist) && lstCheckSkuNum.indexOf(chkSkuNumExist) != _modifyPos) {
                        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009037); // WAPG009037    存貨代碼已存在
                        lstCheckSkuNum.add(_modifyPos, chkSkuNumExist);
                        return;
                    }
                }
            }

            if (etQty.length() <= 0) {
                ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009005); // WAPG009005    請輸入數量
                etQty.requestFocus();
                return;
            } else {
                qty = new BigDecimal(etQty.getText().toString().trim());
            }

            if (etMFDDate.length() <= 0) {
                ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009019); // WAPG009019    請輸入製造日期
                etMFDDate.requestFocus();
                return;
            } else {
                mfgDate = etMFDDate.getText().toString().trim();
            }

            if (etExpiryDate.length() <= 0) {
                ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009020); // WAPG009020    請輸入有效期限
                etExpiryDate.requestFocus();
                return;
            } else {
                expDate = etExpiryDate.getText().toString().trim();
            }

            if (etPONo.length() > 0)
                poNo = etPONo.getText().toString().trim();
            else
                poNo = "";

            if (etPOSeq.length() > 0 ) {
                if (!etPOSeq.getText().toString().trim().equals("*"))
                    poSeq = new BigDecimal((etPOSeq.getText().toString().trim()));
            }
            else
                poSeq = null;

            if (etUOM.length() > 0)
                uom = etUOM.getText().toString().trim();
            else
                uom = "";

            if (etCmt.length() > 0)
                cmt = etCmt.getText().toString().trim();
            else
                cmt = "";

            if (etLotCode.length() > 0)
                lotCode = etLotCode.getText().toString().trim();
            else
                lotCode = "";

            if (sizeKey == null || sizeKey.length() <= 0)
                sizeKey = "";

            if (radioQRcode.isChecked() && etRecScanCode.length() > 0)
                recQrCode = etRecScanCode.getText().toString().trim();
            else
                recQrCode = "";

            if (radioBarcode.isChecked() && etRecScanCode.length() > 0)
                recBarCode = etRecScanCode.getText().toString().trim();
            else
                recBarCode = "";
            // endregion

            // region -- Check SkuNum--
            Object[] args = new Object[2];
            args[0] = itemId;
            args[1] = regType;

            if (skuLevelId.equals("Entity")) {
                if (regType.equals("ItemID")){

                    if (!skuNum.equals(itemId)){
                        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009010, args); //WAPG009010   物料[%s]註冊類別為[%s]，不需要輸入批號!
                        return;
                    }

                } else {

                    if (skuNum.length() <= 0){
                        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009011, args); //WAPG009011   物料[%s]註冊類別為[%s]，需要輸入批號!
                        return;
                    }
                }
            }

            //endregion

            // region -- Check SN --
            if (regType.equals("PcsSN")) {

                if (dtSn != null && dtSn.Rows.size() <= 0) {

                    ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009025); //WAPG009022   請新增收料序號!
                    return;

                } else if (dtSn.Rows.size() != Integer.valueOf(etQty.getText().toString().split("\\.")[0])) {

                    Object[] args1 = new Object[2];
                    args1[0] = dtSn.Rows.size();
                    args1[1] = Integer.valueOf(etQty.getText().toString());
                    ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009026); //WAPG009026   收料數量[%s]與序號數量[%s]不符!
                    return;

                }

                if (dtSn.Rows.size() > 0) {

                    List<String> lstRefKeys = new ArrayList<>();

                    for(DataRow dr : dtSn.Rows) {

                        String refKey = dr.getValue("GRR_DET_SN_REF_KEY").toString();
                        if (!lstRefKeys.contains(refKey)) {
                            lstRefKeys.add(refKey);
                        }
                    }

                    String[] aryRefKeys = new String[lstRefKeys.size()];
                    lstRefKeys.toArray(aryRefKeys);

                    grrDetRefKey = aryRefKeys;
                }

            } else {

                if (dtSn != null && dtSn.Rows.size() > 0) {
                    dtSn.Rows.removeAll(dtSn.Rows);
                    grrDetRefKey = null;
                }
            }
            // endregion

            if (dtWgrDataGroup != null && dtWgrDataGroup.Rows.size() > 0 && !skuLevelId.equals("Entity")) {

                //region 記錄顯示資料至收料清單/Record and display data to the material receipt list
                DataRow drWithSkuLevel = dtLotWithSkuLevel.newRow();
                drWithSkuLevel.setValue("ADD_MODE", _addMode);
                drWithSkuLevel.setValue("SKU_LEVEL", skuLevelId);
                drWithSkuLevel.setValue("SKU_NUM", skuNum);
                drWithSkuLevel.setValue("STORAGE_KEY", storageKey);
                drWithSkuLevel.setValue("STORAGE_ID", storageId);
                drWithSkuLevel.setValue("ITEM_KEY", itemKey);
                drWithSkuLevel.setValue("ITEM_ID", itemId);
                drWithSkuLevel.setValue("ITEM_NAME", itemName);
                drWithSkuLevel.setValue("PO_NO", poNo);
                drWithSkuLevel.setValue("PO_SEQ_DISPLAY", dtWgrDataGroup.Rows.get(0).getValue("PO_SEQ_DISPLAY").toString().trim());
                drWithSkuLevel.setValue("PO_SEQ", dtWgrDataGroup.Rows.get(0).getValue("PO_SEQ_DISPLAY").toString().trim()); // 不重要
                drWithSkuLevel.setValue("LOT_ID", dtWgrDataGroup.Rows.get(0).getValue("LOT_ID").toString().trim());
                drWithSkuLevel.setValue("QTY", qty);
                drWithSkuLevel.setValue("UOM", ""); // 中間表沒有
                drWithSkuLevel.setValue("CMT", cmt);
                drWithSkuLevel.setValue("MFG_DATE", mfgDate);
                drWithSkuLevel.setValue("EXP_DATE", expDate);
                drWithSkuLevel.setValue("SKIP_QC", skipQc); // addLot 才會填入
                drWithSkuLevel.setValue("GRR_DET_SN_REF_KEY", ""); // 中間表沒有
                drWithSkuLevel.setValue("REGISTER_TYPE", regType);
                drWithSkuLevel.setValue("TEMP_BIN", ""); // 中間表沒有
                drWithSkuLevel.setValue("LOT_CODE", lotCode);
                drWithSkuLevel.setValue("SIZE_ID", sizeId);
                drWithSkuLevel.setValue("SIZE_KEY", sizeKey);
                drWithSkuLevel.setValue("VENDOR_ITEM_ID", ""); // 中間表沒有
                drWithSkuLevel.setValue("SPEC_LOT", "N");
                drWithSkuLevel.setValue("REC_BARCODE", ""); // 中間表沒有
                drWithSkuLevel.setValue("REC_QRCODE", ""); // 中間表沒有
                drWithSkuLevel.setValue("BOX1_ID", dtWgrDataGroup.Rows.get(0).getValue("BOX1_ID").toString().trim());
                drWithSkuLevel.setValue("BOX2_ID", dtWgrDataGroup.Rows.get(0).getValue("BOX2_ID").toString().trim());
                drWithSkuLevel.setValue("BOX3_ID", dtWgrDataGroup.Rows.get(0).getValue("BOX3_ID").toString().trim());
                drWithSkuLevel.setValue("PALLET_ID", dtWgrDataGroup.Rows.get(0).getValue("PALLET_ID").toString().trim());
                dtLotWithSkuLevel.Rows.add(drWithSkuLevel);
                //endregion

                //region 記錄實際資料/record actual data
                for (DataRow drWgr : dtOrgWgrData.Rows) {
                    DataRow drNew = dtLot.newRow();
                    drNew.setValue("SKU_LEVEL", skuLevelId);
                    drNew.setValue("SKU_NUM", skuNum);
                    drNew.setValue("ADD_MODE", _addMode);
                    drNew.setValue("STORAGE_KEY", storageKey);
                    drNew.setValue("STORAGE_ID", storageId);
                    drNew.setValue("ITEM_KEY", itemKey);
                    drNew.setValue("ITEM_ID", itemId);
                    drNew.setValue("ITEM_NAME", itemName);
                    drNew.setValue("PO_NO", drWgr.getValue("PO_NO").toString().trim());
                    drNew.setValue("PO_SEQ_DISPLAY", drWgr.getValue("PO_NO").toString().trim()); // 顯示用
                    drNew.setValue("PO_SEQ", drWgr.getValue("PO_SEQ").toString().trim());
                    drNew.setValue("LOT_ID", drWgr.getValue("LOT_ID").toString().trim());
                    drNew.setValue("QTY", drWgr.getValue("QTY").toString().trim());
                    drNew.setValue("UOM", ""); // 中間表沒有
                    drNew.setValue("CMT", drWgr.getValue("CMT").toString().trim());
                    drNew.setValue("MFG_DATE", drWgr.getValue("MFG_DATE").toString().trim());
                    drNew.setValue("EXP_DATE", drWgr.getValue("EXP_DATE").toString().trim());
                    drNew.setValue("SKIP_QC", skipQc); // addLot 才會填入
                    drNew.setValue("GRR_DET_SN_REF_KEY", drWgr.getValue("GRR_DET_SN_REF_KEY").toString());
                    drNew.setValue("REGISTER_TYPE", regType);
                    drNew.setValue("TEMP_BIN", ""); // 中間表沒有
                    drNew.setValue("LOT_CODE", drWgr.getValue("LOT_CODE").toString().trim());
                    drNew.setValue("SIZE_KEY", sizeKey);
                    drNew.setValue("SIZE_ID", sizeId);
                    drNew.setValue("VENDOR_ITEM_ID", ""); // 中間表沒有
                    drNew.setValue("SPEC_LOT", "N");
                    drNew.setValue("REC_BARCODE", ""); // 中間表沒有
                    drNew.setValue("REC_QRCODE", ""); // 中間表沒有
                    drNew.setValue("BOX1_ID", drWgr.getValue("BOX1_ID").toString().trim());
                    drNew.setValue("BOX2_ID", drWgr.getValue("BOX2_ID").toString().trim());
                    drNew.setValue("BOX3_ID", drWgr.getValue("BOX3_ID").toString().trim());
                    drNew.setValue("PALLET_ID", drWgr.getValue("PALLET_ID").toString().trim());
                    dtLot.Rows.add(drNew);
                }
                //endregion

            } else {

                DataRow drLotNew = dtLotWithSkuLevel.newRow();
                drLotNew.setValue("SKU_LEVEL", skuLevelId);
                drLotNew.setValue("SKU_NUM", skuNum);
                drLotNew.setValue("ADD_MODE", _addMode);
                drLotNew.setValue("STORAGE_KEY", storageKey);
                drLotNew.setValue("STORAGE_ID", storageId);
                drLotNew.setValue("ITEM_KEY", itemKey);
                drLotNew.setValue("ITEM_ID", itemId);
                drLotNew.setValue("ITEM_NAME", itemName);
                drLotNew.setValue("PO_NO", poNo);
                drLotNew.setValue("PO_SEQ", poSeq);
                drLotNew.setValue("PO_SEQ_DISPLAY", poSeq);
                drLotNew.setValue("LOT_ID", skuNum);
                drLotNew.setValue("QTY", qty);
                drLotNew.setValue("UOM", uom);
                drLotNew.setValue("CMT", cmt);
                drLotNew.setValue("MFG_DATE", mfgDate);
                drLotNew.setValue("EXP_DATE", expDate);
                drLotNew.setValue("SKIP_QC", skipQc);
                drLotNew.setValue("REGISTER_TYPE", regType);
                if (grrDetRefKey != null && grrDetRefKey.length > 0)
                    drLotNew.setValue("GRR_DET_SN_REF_KEY", grrDetRefKey[0]);
                else
                    drLotNew.setValue("GRR_DET_SN_REF_KEY", grrDetRefKey);
                drLotNew.setValue("LOT_CODE", lotCode);
                drLotNew.setValue("SIZE_KEY", sizeKey);
                drLotNew.setValue("SIZE_ID", sizeId);
                drLotNew.setValue("VENDOR_ITEM_ID", vendorItemId);
                drLotNew.setValue("SPEC_LOT", "N");
                drLotNew.setValue("REC_QRCODE", recQrCode);
                drLotNew.setValue("REC_BARCODE", recBarCode);
                drLotNew.setValue("BOX1_ID", "");
                drLotNew.setValue("BOX2_ID", "");
                drLotNew.setValue("BOX3_ID", "");
                drLotNew.setValue("PALLET_ID", "");
                dtLotWithSkuLevel.Rows.add(drLotNew);
                dtLot.Rows.add(drLotNew);
            }

            // region -- Add Lot or Modify Lot--
            if (_detailMode.equals("Modify")) {
                lstCheckSkuNum.set(_modifyPos, skuNum);
                iSendNonReceiptReceiveDataTable.sendDrLotToList(dtLotWithSkuLevel, dtLot, dtSn,"Modify", _modifyPos);
//                iSendNonReceiptReceiveDataTable.sendDrLotToList(drLotNew, dtSn,"Modify", _modifyPos);
                setDetailMode("Add", -1);
            }
            else {

                if (regType.equals("ItemID")) {
                    skuNum = skuNum + "_" + etPONo.getText().toString().trim() + "_" + etPOSeq.getText().toString().trim();
                }

                lstCheckSkuNum.add(skuNum);
//                iSendNonReceiptReceiveDataTable.sendDrLotToList(drLotNew, dtSn, "Add", -1);
                iSendNonReceiptReceiveDataTable.sendDrLotToList(dtLotWithSkuLevel, dtLot, dtSn, "Add", -1);
            }

            // endregion

            refreshInsertData(true, true);
        }
    };

    private View.OnClickListener onClickRefresh = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (_detailMode.equals("Add"))
                refreshInsertData(true, true);
            else {
                refreshInsertData(true, true);
                setDetailMode("Add", -1);
                TabLayout tb = getActivity().findViewById(R.id.tabLayout);
                tb.getTabAt(1).select();
            }
        }
    };

    private void refreshInsertData(boolean remainSkuLevel, boolean remainStorage) {

        if (remainSkuLevel == false) {
            cmbSkuLevel.setSelection(lstSkuLevel.size()-1, true);
            skuLevelId = "";
        }

        if (remainStorage == false) {
            cmbStorage.setSelection(lstStorage.size()-1, true);
            storageId = "";
            storageKey = "";
        }

        etRecScanCode.getText().clear();
        etPONo.getText().clear();
        etPOSeq.getText().clear();
        cmbItem.setSelection(lstItem.size()-1, true);
        cmbSize.setSelection(lstSize.size()-1, true);
        etLotID.getText().clear();
        etLotCode.getText().clear();
        etQty.getText().clear();
        etUOM.getText().clear();
        etCmt.getText().clear();
        etMFDDate.getText().clear();
        etExpiryDate.getText().clear();
        //btnAddSn.setText("收料序號 (" + 0 + ")");
        btnAddSn.setText(getResources().getString(R.string.GR_SN) + " (0)");
        btnAddSn.setEnabled(false);
        //btnAddSn.setVisibility(View.GONE);
        tvRecExtendTitle.setVisibility(View.GONE);
        lvRecExtend.setVisibility(View.GONE);

        itemKey = ""; itemId = ""; itemName = ""; poNo = ""; skuNum = "";
        uom = ""; cmt = "";  mfgDate = ""; expDate = ""; skipQc = ""; regType = ""; tempBin = "";
        lotCode = ""; sizeKey = ""; vendorItemId = "";
        poSeq = null; qty = null;

        dtLot.Rows.removeAll(dtLot.Rows);
        dtSn.Rows.removeAll(dtSn.Rows);
        dtLotWithSkuLevel.Rows.removeAll(dtLotWithSkuLevel.Rows);

        dtWgrDataGroup = null;
        dtOrgWgrData = null;
        dtOrgSn = null;

        grrDetRefKey = null;

        changePoWidget(false);

        setEnable(true, true, true);
    }

    private void setEnable(boolean enable, boolean skuLevelEnable, boolean qrScanEnable) {

        cmbStorage.setEnabled(true); // 無論如何倉庫都可以選擇/Whatever warehouse can choose

        cmbItem.setEnabled(enable);
        etPONo.setEnabled(enable);
        etPOSeq.setEnabled(enable);
        etLotCode.setEnabled(enable);
        etMFDDate.setEnabled(enable);
        btMFDDateClear.setEnabled(enable);
        etExpiryDate.setEnabled(enable);
        btExpiryDateClear.setEnabled(enable);
        etLotID.setEnabled(enable);
        ibtnLotIdQRScan.setEnabled(enable);
        etQty.setEnabled(enable);
        etUOM.setEnabled(enable);
        etCmt.setEnabled(enable);
        cmbSize.setEnabled(enable);

        cmbSkuLevel.setEnabled(skuLevelEnable);

        etRecScanCode.setEnabled(qrScanEnable);
        radioBarcode.setEnabled(qrScanEnable);
        radioQRcode.setEnabled(qrScanEnable);
        ibtnRecQRScan.setEnabled(qrScanEnable);
    }

    private void changePoWidget(boolean isEntity) {

        if (isEntity == true) {
            cmbPoNo.setVisibility(View.VISIBLE);
            cmbPoSeq.setVisibility(View.VISIBLE);
            etPONo.setVisibility(View.GONE);
            etPOSeq.setVisibility(View.GONE);
        } else {
            lstPoNo = null;
            lstPoSeq = null;
            cmbPoNo.setVisibility(View.GONE);
            cmbPoSeq.setVisibility(View.GONE);
            etPONo.setVisibility(View.VISIBLE);
            etPOSeq.setVisibility(View.VISIBLE);
        }

    }

    private DataTable createDataTableLotID() {
        DataTable dtLot = new DataTable();
        dtLot.addColumn(new DataColumn("ADD_MODE"));
        dtLot.addColumn(new DataColumn("SKU_LEVEL"));
        dtLot.addColumn(new DataColumn("SKU_NUM"));
        dtLot.addColumn(new DataColumn("STORAGE_KEY"));
        dtLot.addColumn(new DataColumn("STORAGE_ID"));
        dtLot.addColumn(new DataColumn("ITEM_KEY"));
        dtLot.addColumn(new DataColumn("ITEM_ID"));
        dtLot.addColumn(new DataColumn("ITEM_NAME"));
        dtLot.addColumn(new DataColumn("PO_NO"));
        dtLot.addColumn(new DataColumn("PO_SEQ"));
        dtLot.addColumn(new DataColumn("PO_SEQ_DISPLAY"));
        dtLot.addColumn(new DataColumn("LOT_ID"));
        dtLot.addColumn(new DataColumn("SPEC_LOT"));
        dtLot.addColumn(new DataColumn("QTY"));
        dtLot.addColumn(new DataColumn("UOM"));
        dtLot.addColumn(new DataColumn("CMT"));
        dtLot.addColumn(new DataColumn("MFG_DATE"));
        dtLot.addColumn(new DataColumn("EXP_DATE"));
        dtLot.addColumn(new DataColumn("SKIP_QC"));
        dtLot.addColumn(new DataColumn("GRR_DET_SN_REF_KEY"));
        dtLot.addColumn(new DataColumn("REGISTER_TYPE"));
        dtLot.addColumn(new DataColumn("TEMP_BIN"));
        dtLot.addColumn(new DataColumn("LOT_CODE"));
        dtLot.addColumn(new DataColumn("SIZE_ID"));
        dtLot.addColumn(new DataColumn("SIZE_KEY"));
        dtLot.addColumn(new DataColumn("VENDOR_ITEM_ID"));
        dtLot.addColumn(new DataColumn("REC_BARCODE"));
        dtLot.addColumn(new DataColumn("REC_QRCODE"));
        dtLot.addColumn(new DataColumn("BOX1_ID"));
        dtLot.addColumn(new DataColumn("BOX2_ID"));
        dtLot.addColumn(new DataColumn("BOX3_ID"));
        dtLot.addColumn(new DataColumn("PALLET_ID"));
        return dtLot;
    }

    private DataTable createDataTableSN() {
        DataTable dtSN = new DataTable();
        dtSN.addColumn(new DataColumn("GRR_DET_SN_REF_KEY"));
        dtSN.addColumn(new DataColumn("SN_ID"));
        return dtSN;
    }

    private void setDetailMode(String mode, int pos) {

        switch (mode) {

            case "Add":
                _detailMode = mode; // Add
                _modifyPos = -1;
                btnAddLodID.setText(getResources().getString(R.string.ADD_SKU_ID));
                btnRefresh.setText(R.string.CLEAR);
                break;

            case "Modify":
                _detailMode = mode; // Modify
                _modifyPos = pos;
                btnAddLodID.setText(R.string.MODIFY_SKU_ID);
                btnRefresh.setText(R.string.CANCEL);
                break;
        }
    }

    // endregion

    // region -- Interface --
    public interface ISendNonReceiptReceiveDataTable {

//        public void sendDrLotToList(DataRow drLot, String mode, int pos);

//        public void sendDrLotToList(DataRow drLot, DataTable dtSn, String mode, int pos);

        public void sendDrLotToList(DataTable dtLotWithSkuLevel, DataTable dtLot, DataTable dtSn, String mode, int pos);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            iSendNonReceiptReceiveDataTable = (ISendNonReceiptReceiveDataTable) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Plz implement interface method");
        }
    }

    public void setLotTable(DataRow drLotWithSkuLevel, DataTable dtLot, DataTable dtSelectedSn, String mode, int pos) {

        setDetailMode(mode, pos);
        refreshInsertData(true, true);

        dtWgrDataGroup = new DataTable();
        dtOrgWgrData = new DataTable();
        dtSn = new DataTable();

        dtWgrDataGroup.Rows.add(drLotWithSkuLevel);
        dtOrgWgrData = dtLot;
        dtSn = dtSelectedSn;

        _addMode = drLotWithSkuLevel.getValue("ADD_MODE").toString();

        // region -- 設置 cmbStorage --
        int storagePosition = 0;
        storageKey = drLotWithSkuLevel.getValue("STORAGE_KEY").toString();

        for (Map<String, Object> storageMap : lstStorage) {
            if (storageMap.get("STORAGE_KEY").toString().equals(storageKey))
                break;
            storagePosition++;
        }

        SimpleAdapter adapterStorage = new AnotherSimpleArrayAdapter<>(getContext(), lstStorage, android.R.layout.simple_spinner_item, new String[]{"STORAGE_KEY", "STORAGE_ID", "IDNAME"}, new int[]{0, android.R.id.text1, 0});
        adapterStorage.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmbStorage.setAdapter(adapterStorage);
        cmbStorage.setSelection(storagePosition, true);
        // endregion

        // region -- 設置 cmbSkuLevel --
        int levelPosition = 0;
        skuLevelId = drLotWithSkuLevel.getValue("SKU_LEVEL").toString();

        for (Map<String, Object> levelMap : lstSkuLevel) {
            if (levelMap.get("DATA_ID").toString().equals(skuLevelId))
                break;
            levelPosition++;
        }

        SimpleAdapter adapterSkuLevel = new AnotherSimpleArrayAdapter<>(getContext(), lstSkuLevel, android.R.layout.simple_spinner_item, new String[]{"DATA_ID", "DATA_NAME", "IDNAME"}, new int[]{0, android.R.id.text1, 0});
        adapterSkuLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmbSkuLevel.setAdapter(adapterSkuLevel);
        cmbSkuLevel.setSelection(levelPosition, true);
        // endregion

        // region -- 設置 cmbItem --
        int itemPosition = 0;
        itemKey = drLotWithSkuLevel.getValue("ITEM_KEY").toString();

        for (Map<String, Object> itemMap : lstItem) {
            if (itemMap.get("ITEM_KEY").toString().equals(itemKey))
                break;
            itemPosition++;
        }

        SimpleAdapter adapterItem = new AnotherSimpleArrayAdapter<>(getContext(), lstItem, android.R.layout.simple_spinner_item, new String[]{"ITEM_KEY", "ITEM_ID", "ITEM_NAME", "REGISTER_TYPE", "IDNAME"}, new int[]{0, android.R.id.text1, 0, 0, 0});
        adapterItem.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmbItem.setAdapter(adapterItem);
        cmbItem.setSelection(itemPosition, true);
        // endregion

        // region -- 設置 cmbSize --
        int sizePosition = 0;
        sizeKey = drLotWithSkuLevel.getValue("SIZE_KEY").toString();

        if (sizeKey.length() <= 0) {
            sizePosition = lstSize.size() - 1;
        } else {
            for (Map<String, Object> sizeMap : lstSize) {
                if (sizeMap.get("SIZE_KEY").toString().equals(sizeKey))
                    break;
                sizePosition++;
            }
        }

        SimpleAdapter adapterSize = new AnotherSimpleArrayAdapter<>(getContext(), lstSize, android.R.layout.simple_spinner_item, new String[]{"SIZE_KEY", "SIZE_ID", "SIZE_NAME", "IDNAME"}, new int[]{0, android.R.id.text1, 0, 0});
        adapterSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmbSize.setAdapter(adapterSize);
        cmbSize.setSelection(sizePosition, true);
        // endregion

        if (_addMode.equals("WGR")) {

            setEnable(false, false,false);

            // region -- 設置 EditText --
            etPONo.setText(drLotWithSkuLevel.getValue("PO_NO").toString());
            etPOSeq.setText(drLotWithSkuLevel.getValue("PO_SEQ_DISPLAY").toString());
            etLotID.setText(drLotWithSkuLevel.getValue("SKU_NUM").toString()); // LOT_ID
            etQty.setText(drLotWithSkuLevel.getValue("QTY").toString());
            etUOM.setText(drLotWithSkuLevel.getValue("UOM").toString());
            etCmt.setText(drLotWithSkuLevel.getValue("CMT").toString());
            etLotCode.setText(drLotWithSkuLevel.getValue("LOT_CODE").toString());
            etMFDDate.setText(drLotWithSkuLevel.getValue("MFG_DATE").toString());
            etExpiryDate.setText(drLotWithSkuLevel.getValue("EXP_DATE").toString());
            // endregion

            //region -- 拆解 QRCode/BarCode --
            String strBarCodeType, strBarCodeTypeId, strBarCodeValue;
            String strBarCode = drLotWithSkuLevel.getValue("REC_BARCODE").toString().trim();
            String strQRCode = drLotWithSkuLevel.getValue("REC_QRCODE").toString().trim();

            if (strBarCode.length() > 0) {

                strBarCodeType = "BARCODE_TYPE";
                strBarCodeTypeId = "ReceiveBarCode";
                strBarCodeValue = strBarCode;

                rgRecQRCode.check(R.id.radioBarcode);

                getRecCode(strBarCodeType, strBarCodeTypeId, strBarCodeValue, dtReceiptMst, false); // 拆解條碼

            } else if (strQRCode.length() > 0) {

                strBarCodeType = "QRCODE_TYPE";
                strBarCodeTypeId = "ReceiveQRCode";
                strBarCodeValue = strQRCode;

                rgRecQRCode.check(R.id.radioQRcode);

                getRecCode(strBarCodeType, strBarCodeTypeId, strBarCodeValue, dtReceiptMst, false); // 拆解條碼
            }
            //endregion

        } else {

            setEnable(true, true, true);

            // region -- 設置 EditText --
            etPONo.setText(drLotWithSkuLevel.getValue("PO_NO").toString());
            etPOSeq.setText(drLotWithSkuLevel.getValue("PO_SEQ").toString());
            etLotID.setText(drLotWithSkuLevel.getValue("SKU_NUM").toString()); // LOT_ID
            etQty.setText(drLotWithSkuLevel.getValue("QTY").toString());
            etUOM.setText(drLotWithSkuLevel.getValue("UOM").toString());
            etCmt.setText(drLotWithSkuLevel.getValue("CMT").toString());
            etLotCode.setText(drLotWithSkuLevel.getValue("LOT_CODE").toString());
            etMFDDate.setText(drLotWithSkuLevel.getValue("MFG_DATE").toString());
            etExpiryDate.setText(drLotWithSkuLevel.getValue("EXP_DATE").toString());
            // endregion

            //region -- 拆解並設置 QRCode/BarCode --
            String strBarCodeType, strBarCodeTypeId, strBarCodeValue;
            String strBarCode = drLotWithSkuLevel.getValue("REC_BARCODE").toString().trim();
            String strQRCode = drLotWithSkuLevel.getValue("REC_QRCODE").toString().trim();

            if (strBarCode.length() > 0) {

                strBarCodeType = "BARCODE_TYPE";
                strBarCodeTypeId = "ReceiveBarCode";
                strBarCodeValue = strBarCode;

                rgRecQRCode.check(R.id.radioBarcode);

                getRecCode(strBarCodeType, strBarCodeTypeId, strBarCodeValue, dtReceiptMst, true); // 拆解條碼填入對應欄位

            } else if (strQRCode.length() > 0) {

                strBarCodeType = "QRCODE_TYPE";
                strBarCodeTypeId = "ReceiveQRCode";
                strBarCodeValue = strQRCode;

                rgRecQRCode.check(R.id.radioQRcode);

                getRecCode(strBarCodeType, strBarCodeTypeId, strBarCodeValue, dtReceiptMst, true); // 拆解條碼填入對應欄位
            }
            //endregion
        }

        //region -- 設置 Sn --
        if (drLotWithSkuLevel.getValue("REGISTER_TYPE").equals("PcsSN")) {
            if (dtSelectedSn != null && dtSelectedSn.Rows.size() > 0) {
                dtSn = dtSelectedSn;

                List<String> lstRefKeys = new ArrayList<>();
                for (DataRow dr : dtSn.Rows) {

                    String refKey = dr.getValue("GRR_DET_SN_REF_KEY").toString();
                    if (!lstRefKeys.contains(refKey))
                        lstRefKeys.add(refKey);
                }

                String[] aryRefKeys = new String[lstRefKeys.size()];
                lstRefKeys.toArray(aryRefKeys);

                grrDetRefKey = aryRefKeys;

                btnAddSn.setText(getResources().getString(R.string.GR_SN) + " (" + dtSn.Rows.size() + ")");
            }
        } else {
            dtSn.Rows.removeAll(dtSn.Rows);
            btnAddSn.setText(getResources().getString(R.string.GR_SN) + " (0)");
        }
        //endregion
    }

    public void setLotTable(DataRow drLot, DataTable dtSelectedSn, String mode, int pos) {

        //lstCheckLotId.remove(drLot.getValue("LOT_ID").toString());

//        String barCodeType = "", barCodeTypeId = "";
//        if (drLot.getValue("REC_BARCODE").toString().length() > 0) {
//            etRecScanCode.setText(drLot.getValue("REC_BARCODE").toString());
//            barCodeType = "BARCODE_TYPE";
//            barCodeTypeId = "ReceiveBarCode";
//        } else if (drLot.getValue("REC_QRCODE").toString().length() > 0) {
//            etRecScanCode.setText(drLot.getValue("REC_QRCODE").toString());
//            barCodeType = "QRCODE_TYPE";
//            barCodeTypeId = "ReceiveQRCode";
//        } else {
//            barCodeType = "";
//            barCodeTypeId = "";
//            etRecScanCode.getText().clear();
//        }
//         再填入收料條碼資訊
//        if (etRecScanCode.length() > 0) {
//            getRecCode(barCodeType, barCodeTypeId, etRecScanCode.getText().toString().trim(), dtReceiptMst, true);
//        }
    }

    private void getRecCode(String barCodeType, String barCodeTypeId, final String content, DataTable dtReceiptMst, final boolean getMainInfo) {
        BModuleObject bmObjRecCode = new BModuleObject();
        bmObjRecCode.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIGoodNonReceiptReceivePortal");
        bmObjRecCode.setModuleID("BIFetchNonReceiptReceiveBarCodeResult");
        bmObjRecCode.setRequestID("BIFetchNonReceiptReceiveBarCodeResult");
        bmObjRecCode.params = new Vector<>();

        // Input param
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIGoodNonReceiptReceivePortalParam.BarCodeType);
        param1.setParameterValue(barCodeType);
        bmObjRecCode.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIGoodNonReceiptReceivePortalParam.BarCodeTypeId);
        param2.setParameterValue(barCodeTypeId);
        bmObjRecCode.params.add(param2);

        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BIGoodNonReceiptReceivePortalParam.BarCodeValue);
        param3.setParameterValue(content);
        bmObjRecCode.params.add(param3);

        List<BGoodReceiptReceiveNonSheetWithPackingInfoParam.GrNonSheetWithPackingInfoMasterObj> lstGrMstObj = new ArrayList<>();
        BGoodReceiptReceiveNonSheetWithPackingInfoParam sheet = new BGoodReceiptReceiveNonSheetWithPackingInfoParam();

        // region -- 設置GrrMstObj --
        for (DataRow dr : dtReceiptMst.Rows) {
            BGoodReceiptReceiveNonSheetWithPackingInfoParam.GrNonSheetWithPackingInfoMasterObj sheetMst = sheet.new GrNonSheetWithPackingInfoMasterObj();
            sheetMst.setGrTypeID(dr.getValue("GR_TYPE_ID").toString());
            sheetMst.setGrTypeKey(dr.getValue("GR_TYPE_KEY").toString());
            sheetMst.setGrSource(dr.getValue("GR_SOURCE").toString());
            sheetMst.setVendorID(dr.getValue("VENDOR_ID").toString());
            sheetMst.setVendorKey(dr.getValue("VENDOR_KEY").toString());
            sheetMst.setVendorShipNo(dr.getValue("VENDOR_SHIP_NO").toString());
            sheetMst.setVendorShipDate(dr.getValue("VENDOR_SHIP_DATE").toString());
            sheetMst.setCustomerID(dr.getValue("CUSTOMER_ID").toString());
            sheetMst.setCustomerKey(dr.getValue("CUSTOMER_KEY").toString());
            lstGrMstObj.add(sheetMst);
        }

        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.INV.Parameter.GrNonSheetWithPackingInfoMasterObj", "bmWMS.INV.Param");
        MList mListEnum = new MList(vListEnum);
        String strGrrMstObj = mListEnum.generateFinalCode(lstGrMstObj);
        // endregion

        ParameterInfo param4 = new ParameterInfo();
        param4.setParameterID(BIGoodNonReceiptReceivePortalParam.GrrMasterObj);
        param4.setNetParameterValue(strGrrMstObj);
        bmObjRecCode.params.add(param4);

        //refreshInsertData(true, true);

        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).CallBIModule(bmObjRecCode, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (((GoodNonReceiptReceiveDetailNewActivity)getActivity()).CheckBModuleReturnInfo(bModuleReturn)) {

                    etRecScanCode.setText(content);
                    //etRecScanCode.setEnabled(false);

                    DataTable dtBarCodeData = bModuleReturn.getReturnJsonTables().get("BIFetchNonReceiptReceiveBarCodeResult").get("BARCODE_DATA");
                    DataTable dtExtendData = bModuleReturn.getReturnJsonTables().get("BIFetchNonReceiptReceiveBarCodeResult").get("EXTEND_DATA");

                    // region -- 收料資訊塞值/Received information plug value --
                    if (getMainInfo == true) {
                        if (dtBarCodeData != null && dtBarCodeData.Rows.size() > 0) {
                            for (DataRow drData : dtBarCodeData.Rows) {

                                String variableId = drData.get("BARCODE_VARIABLE_ID").toString().trim().toUpperCase();
                                String value = drData.get("BARCODE_VALUE").toString();

                                switch (variableId) {
                                    case "ITEM_ID":
                                        // region -- 設置 cmbItem/Setting cmbItem --
                                        int itemPosition = 0;
                                        itemId = value;

                                        for (Map<String, Object> itemMap : lstItem) {
                                            if (itemMap.get("ITEM_ID").toString().equals(itemId))
                                                break;
                                            itemPosition++;
                                        }

                                        if (itemPosition == lstItem.size()) {
                                            ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009027); //WAPG009027   查無物料代碼[%s]!
                                            return;
                                        }

                                        SimpleAdapter adapterItem = new AnotherSimpleArrayAdapter<>(getContext(), lstItem, android.R.layout.simple_spinner_item, new String[]{"ITEM_KEY", "ITEM_ID", "ITEM_NAME", "REGISTER_TYPE", "IDNAME"}, new int[]{0, android.R.id.text1, 0, 0, 0});
                                        adapterItem.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        cmbItem.setAdapter(adapterItem);
                                        cmbItem.setSelection(itemPosition, true);
                                        // endregion

                                        cmbItem.setEnabled(false);

                                        break;

                                    case "VENDOR_ITEM_ID":
                                        vendorItemId = value;
                                        break;

                                    case "PO_NO":
                                        etPONo.setText(value);
                                        etPONo.setEnabled(false);
                                        break;

                                    case "PO_SEQ":
                                        etPOSeq.setText(value);
                                        etPOSeq.setEnabled(false);
                                        break;

                                    case "LOT_CODE":
                                        etLotCode.setText(value);
                                        etLotCode.setEnabled(false);
                                        break;

                                    case "MFG_DATE":
                                        etMFDDate.setText(value);
                                        etMFDDate.setEnabled(false);
                                        btMFDDateClear.setEnabled(false);
                                        break;

                                    case "EXP_DATE":
                                        etExpiryDate.setText(value);
                                        etExpiryDate.setEnabled(false);
                                        btExpiryDateClear.setEnabled(false);
                                        break;

                                    case "REGISTER_ID":
                                        etLotID.setText(value);
                                        etLotID.setEnabled(false);
                                        ibtnLotIdQRScan.setEnabled(false);
                                        break;

                                    case "QTY":
                                        etQty.setText(value);
                                        etQty.setEnabled(false);
                                        break;

                                    case "UOM":
                                        etUOM.setText(value);
                                        etUOM.setEnabled(false);
                                        break;

                                    case "SIZE_ID":
                                        // region -- 設置 cmbSize/Setting cmbSize --
                                        int sizePosition = 0;
                                        sizeId = value;

                                        if (sizeId.length() <= 0) {
                                            sizePosition = lstSize.size() - 1;
                                        } else {
                                            for (Map<String, Object> sizeMap : lstSize) {
                                                if (sizeMap.get("SIZE_ID").toString().equals(sizeId))
                                                    break;
                                                sizePosition++;
                                            }
                                        }

                                        if (sizePosition == lstSize.size()) {
                                            ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009028); //WAPG009028   查無尺寸代碼[%s]!
                                            return;
                                        }

                                        SimpleAdapter adapterSize = new AnotherSimpleArrayAdapter<>(getContext(), lstSize, android.R.layout.simple_spinner_item, new String[]{"SIZE_KEY", "SIZE_ID", "SIZE_NAME", "IDNAME"}, new int[]{0, android.R.id.text1, 0, 0});
                                        adapterSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        cmbSize.setAdapter(adapterSize);
                                        cmbSize.setSelection(sizePosition, true);
                                        // endregion

                                        cmbSize.setEnabled(false);

                                        break;

                                    default:
                                        break;
                                }
                            }
                        }
                    }
                    // endregion

                    // region -- Extend欄位 --
                    if (dtExtendData != null && dtExtendData.Rows.size() > 0) {
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        GoodReceiptReceiveExtendGridAdapter adapter = new GoodReceiptReceiveExtendGridAdapter(dtExtendData, inflater);
                        lvRecExtend.setAdapter(adapter);

                        tvRecExtendTitle.setVisibility(View.VISIBLE);
                        lvRecExtend.setVisibility(View.VISIBLE);
                    }
                    // endregion

                } else {
                    etRecScanCode.getText().clear();
                }
            }
        });
    }
    // endregion
}
