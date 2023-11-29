package com.delta.android.WMS.Client;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.DataTable.DataColumn;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class GoodNonReceiptReceiveNewActivity extends BaseFlowActivity {

    // region -- MST 控制項 --
    private Spinner cmbSheetType, cmbVendor, cmbCustomer;
    private EditText etVendorShipNo, etVendorShipDate;
    private Button btVendorShipDateClear, btnAddReceiptDet;
    // endregion

    // region -- MST 變數 --
    private String sheetTypeId, sheetTypeKey, vendorId, vendorKey, customerId, customerKey, vendorShipNo, vendorShipDate;
    List<? extends Map<String, Object>> lstSheetType;
    List<? extends Map<String, Object>> lstVendor;
    List<? extends Map<String, Object>> lstCustomer;
    DataTable dtVendorItemQc, dtVendorQcInfo;
    // endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good_non_receipt_receive_new);

        setInitWidget();

        getSpinnerInitData();

        setListeners();
    }

    // region -- Private Method --

    private void setInitWidget() {
        cmbSheetType = findViewById(R.id.cmbReceiveSheetType);
        cmbVendor = findViewById(R.id.cmbVendor);
        etVendorShipNo = findViewById(R.id.etVendorShipNo);
        etVendorShipDate = findViewById(R.id.etVendorShipDate);
        etVendorShipDate.setInputType(InputType.TYPE_NULL);
        cmbCustomer = findViewById(R.id.cmbCustomer);
        btVendorShipDateClear = findViewById(R.id.btVendorShipDateClear);
        btnAddReceiptDet = findViewById(R.id.btnAddReceiptDet);
    }

    private class SimpleArrayAdapter<T> extends SimpleAdapter {
        public SimpleArrayAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        //複寫這個方法，使返回的數據沒有最後一項
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

        //單據類型
        BModuleObject bmObjSheetType = new BModuleObject();
        bmObjSheetType.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObjSheetType.setModuleID("BIFetchSheetType"); // 原 BIFetchWmsSheetConfig => 如果沒有設定 Config 就會抓不到 Sheet Type
        bmObjSheetType.setRequestID("GetSheetType");

        bmObjSheetType.params = new Vector<>();
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Filter);
        param1.setParameterValue("  AND P.SHEET_TYPE_POLICY_ID = 'Receipt' AND T.SHEET_TYPE_ID = 'SysReceipt' ");
        bmObjSheetType.params.add(param1);
        lsBObj.add(bmObjSheetType);

        //廠商代碼
        BModuleObject bmObjVendor = new BModuleObject();
        bmObjVendor.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObjVendor.setModuleID("BIFetchVendor");
        bmObjVendor.setRequestID("GetVendor");
        lsBObj.add(bmObjVendor);

        // 廠商物料檢驗
        // VendroItemQC
        BModuleObject bmObjVI = new BModuleObject();
        bmObjVI.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObjVI.setModuleID("BIFetchVendorItemIQC");
        bmObjVI.setRequestID("BIFetchVendorItemIQC");
        lsBObj.add(bmObjVI);

        //客戶
        BModuleObject bmObjCustomer = new BModuleObject();
        bmObjCustomer.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObjCustomer.setModuleID("BIFetchCustomer");
        bmObjCustomer.setRequestID("GetCustomer");
        lsBObj.add(bmObjCustomer);
        //endregion

        CallBIModule(lsBObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    DataTable dtSheetType = bModuleReturn.getReturnJsonTables().get("GetSheetType").get("SHEET_TYPE");
                    DataTable dtVendor = bModuleReturn.getReturnJsonTables().get("GetVendor").get("VENDOR");
                    DataTable dtCustomer = bModuleReturn.getReturnJsonTables().get("GetCustomer").get("CUSTOMER");
                    dtVendorItemQc = bModuleReturn.getReturnJsonTables().get("BIFetchVendorItemIQC").get("VENDOR_ITEM_IQC");

                    DataRow drDefaultItem = dtSheetType.newRow();
                    drDefaultItem.setValue("IDNAME", ""); // 下拉式選單default空白

                    // region -- 收料類型 --
                    if (dtSheetType != null && dtSheetType.Rows.size() > 0)
                        dtSheetType.Rows.add(drDefaultItem);
                    lstSheetType = (List<? extends Map<String, Object>>) dtSheetType.toListHashMap();
                    SimpleAdapter adapterShtType = new SimpleArrayAdapter<>(GoodNonReceiptReceiveNewActivity.this, lstSheetType, android.R.layout.simple_spinner_item, new String[]{"SHEET_TYPE_KEY", "SHEET_TYPE_ID", "IDNAME"}, new int[]{0, android.R.id.text1, 0});
                    adapterShtType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    cmbSheetType.setAdapter(adapterShtType);
                    cmbSheetType.setSelection(lstSheetType.size()-1, true);
                    // endregion

                    // region -- 廠商 --
                    if (dtVendor != null && dtVendor.Rows.size() > 0)
                        dtVendor.Rows.add(drDefaultItem);
                    lstVendor = (List<? extends Map<String, Object>>) dtVendor.toListHashMap();
                    SimpleAdapter adapterVendor = new SimpleArrayAdapter<>(GoodNonReceiptReceiveNewActivity.this, lstVendor, android.R.layout.simple_spinner_item, new String[]{"VENDOR_KEY", "VENDOR_ID", "IDNAME"}, new int[]{0, android.R.id.text1, 0});
                    adapterVendor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    cmbVendor.setAdapter(adapterVendor);
                    cmbVendor.setSelection(lstVendor.size()-1, true);
                    // endregion

                    // region -- 客戶 --
                    if (dtCustomer != null && dtCustomer.Rows.size() > 0)
                        dtCustomer.Rows.add(drDefaultItem);
                    lstCustomer = (List<? extends Map<String, Object>>) dtCustomer.toListHashMap();
                    SimpleAdapter adapterCustomer = new SimpleArrayAdapter<>(GoodNonReceiptReceiveNewActivity.this, lstCustomer, android.R.layout.simple_spinner_item, new String[]{"CUSTOMER_KEY", "CUSTOMER_ID", "IDNAME"}, new int[]{0, android.R.id.text1, 0});
                    adapterCustomer.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    cmbCustomer.setAdapter(adapterCustomer);
                    cmbCustomer.setSelection(lstCustomer.size()-1, true);
                    // endregion
                }
            }
        });
    }

    private void setListeners() {
        cmbSheetType.setOnItemSelectedListener(cmbSheetTypeOnClick);
        cmbVendor.setOnItemSelectedListener(cmbVendorOnClick);
        cmbCustomer.setOnItemSelectedListener(cmbCustomerOnClick);
        etVendorShipDate.setOnClickListener(vendorShipDateOnClick);
        btVendorShipDateClear.setOnClickListener(onClickVendorShipDateClear);
        btnAddReceiptDet.setOnClickListener(btnReceiptDetOnClick);
    }

    private Spinner.OnItemSelectedListener cmbSheetTypeOnClick = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {

            sheetTypeId = "";
            sheetTypeKey = "";

            if (position != lstSheetType.size()-1) {
                Map<String, String> sheetTypeMap = (Map<String, String>)parent.getItemAtPosition(position);
                sheetTypeId = sheetTypeMap.get("SHEET_TYPE_ID");
                sheetTypeKey = sheetTypeMap.get("SHEET_TYPE_KEY");
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            return;
        }
    };

    private Spinner.OnItemSelectedListener cmbVendorOnClick = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {

            vendorId = "";
            vendorKey = "";

            if (position != lstVendor.size()-1) {
                Map<String, String> sheetTypeMap = (Map<String, String>)parent.getItemAtPosition(position);
                vendorId = sheetTypeMap.get("VENDOR_ID");
                vendorKey = sheetTypeMap.get("VENDOR_KEY");

                if (dtVendorItemQc != null && dtVendorItemQc.Rows.size() > 0) {
                    dtVendorQcInfo = new DataTable();

                    for (DataRow dr : dtVendorItemQc.Rows) {
                        if (dr.getValue("VENDOR_KEY").equals(vendorKey) || dr.getValue("VENDOR_KEY").equals("*"))
                            dtVendorQcInfo.Rows.add(dr); //lstVendorItemQc.add(dr);
                    }
                }

            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            return;
        }
    };

    private Spinner.OnItemSelectedListener cmbCustomerOnClick = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {

            customerId = "";
            customerKey = "";

            if (position != lstCustomer.size()-1) {
                Map<String, String> sheetTypeMap = (Map<String, String>)parent.getItemAtPosition(position);
                customerId = sheetTypeMap.get("CUSTOMER_ID");
                customerKey = sheetTypeMap.get("CUSTOMER_KEY");
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            return;
        }
    };

    private View.OnClickListener vendorShipDateOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) { setVendorShipDate();
        }
    };

    private View.OnClickListener onClickVendorShipDateClear = new View.OnClickListener() {
        @Override
        public void onClick(View view) { etVendorShipDate.setText(""); }
    };

    public void setVendorShipDate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.style_wms_dialog_date_picker_spinner, null);//layout
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);//物件
        //右邊簡化
        datePicker.setCalendarViewShown(false);
        //初始化
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
        //設置Dialog
        builder.setView(view);
        //標頭
        builder.setTitle(R.string.VENDOR_SHIP_DATE);
        final AlertDialog dialog = builder.create();
        dialog.show();

        Button bConfirm = view.findViewById(R.id.btnConfirm);
        bConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //日期格式
                StringBuffer sb = new StringBuffer();
                sb.append(String.format("%d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth()));
                etVendorShipDate.setText(sb);
                dialog.cancel();
            }
        });
    }

    private View.OnClickListener btnReceiptDetOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (sheetTypeKey.length() <= 0) {
                ShowMessage(R.string.WAPG009001); // WAPG009001 請選擇單據類型
                return;
            }

            if (vendorKey.length() <= 0) {
                ShowMessage(R.string.WAPG009002); //WAPG009002    請選擇廠商
                return;
            }

            if (dtVendorQcInfo == null || dtVendorQcInfo.Rows.size() <= 0) {
                ShowMessage(R.string.WAPG009017, vendorId); // WAPG009017    供應商[%s]未設定供應商對應物料檢驗設定
                return;
            }

            vendorShipNo = etVendorShipNo.getText().toString().trim();
            vendorShipDate = etVendorShipDate.getText().toString().trim();

            DataTable dtReceiptMst = createReceiptMst();
            DataRow drNewMst = dtReceiptMst.newRow();
            drNewMst.setValue("GR_ID", "");
            drNewMst.setValue("GR_TYPE_ID", sheetTypeId);
            drNewMst.setValue("GR_TYPE_KEY", sheetTypeKey);
            drNewMst.setValue("GR_SOURCE", "WMS");
            drNewMst.setValue("VENDOR_ID", vendorId);
            drNewMst.setValue("VENDOR_KEY", vendorKey);
            drNewMst.setValue("VENDOR_SHIP_NO", vendorShipNo);
            drNewMst.setValue("VENDOR_SHIP_DATE", vendorShipDate);
            drNewMst.setValue("CUSTOMER_ID", customerId);
            drNewMst.setValue("CUSTOMER_KEY", customerKey);
            dtReceiptMst.Rows.add(drNewMst);

            Bundle mstInfo = new Bundle();
            mstInfo.putSerializable("dtReceiptMst", dtReceiptMst);
            mstInfo.putSerializable("dtVendorQcInfo", dtVendorQcInfo);
            gotoNextActivity(GoodNonReceiptReceiveDetailNewActivity.class, mstInfo);
        }
    };

    private DataTable createReceiptMst() {
        DataTable dtMst = new DataTable();
        dtMst.addColumn(new DataColumn("GR_ID"));
        dtMst.addColumn(new DataColumn("GR_TYPE_ID"));
        dtMst.addColumn(new DataColumn("GR_TYPE_KEY"));
        dtMst.addColumn(new DataColumn("GR_SOURCE"));
        dtMst.addColumn(new DataColumn("VENDOR_ID"));
        dtMst.addColumn(new DataColumn("VENDOR_KEY"));
        dtMst.addColumn(new DataColumn("VENDOR_SHIP_NO"));
        dtMst.addColumn(new DataColumn("VENDOR_SHIP_DATE"));
        dtMst.addColumn(new DataColumn("CUSTOMER_ID"));
        dtMst.addColumn(new DataColumn("CUSTOMER_KEY"));
        return dtMst;
    }

    // endregion
}

//Error Code WAPG009
//WAPG009001    請選擇單據類型
//WAPG009002    請選擇廠商
//WAPG009003    請選擇倉庫
//WAPG009004    請選擇物料
//WAPG009005    請輸入數量
//WAPG009006    存貨代碼已存在
//WAPG009007    作業成功
//WAPG009008    是否刪除此序號?
//WAPG009009    未滿需求量!
//WAPG009010    物料[%s]註冊類別為[%s]，不需要輸入批號!
//WAPG009011    物料[%s]註冊類別為[%s]，需要輸入批號!
//WAPG009012    已滿足需求量
//WAPG009013    物料[%s]序號尚未收滿!
//WAPG009014    是否刪除此批號?
//WAPG009015    序號重複!
//WAPG009016    尚未新增任何料號!
//WAPG009017    供應商[%s]未設定供應商對應物料檢驗設定
//WAPG009018    尚未選擇倉庫[%s]物料[%s]對應儲位
//WAPG009019    請輸入製造日期
//WAPG009020    請輸入有效期限
//WAPG009021    未設定收料類型單據，無法產生系統的收料單據

//WAPG009022    請選擇一維條碼或二維條碼
//WAPG009023    請輸入存貨代碼
//WAPG009024    作業成功，收料單號[%s]

//WAPG009025    請新增收料序號!
//WAPG009026    收料數量[%s]與序號數量[%s]不符!
//WAPG009027    查無物料代碼[%s]!
//WAPG009028    查無尺寸代碼[%s]!
//WAPG009029    請選擇收料條碼類型

//WAPG009030    倉庫[%s]未設定入庫暫存區或入料口
//WAPG009031    倉庫[%s]未設定IQC儲位