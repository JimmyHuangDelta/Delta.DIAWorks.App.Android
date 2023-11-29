package com.delta.android.WMS.Client;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.Activity.ScanActivity;
import com.delta.android.Core.DataTable.DataColumn;
import com.delta.android.Core.DataTable.DataColumnCollection;
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
import com.delta.android.WMS.Client.GridAdapter.WarehouseStorageMstAdapter;
import com.delta.android.WMS.Param.BIFetchProcessSheetParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class WarehouseStorageActivity extends BaseFlowActivity {

    // FunctionId = WAPG027

    ViewHolder holder = null;
    DataTable dtMstInLv = null;
    HashMap<String, DataTable> mapMstDet  = null;
    String strSheetPolicyId, strSheetId, strSheetTypeId, strProcessType;
    boolean blnIsWV = false;

    private static class ViewHolder {
        // 宣告控制項物件
        EditText etFromDate;
        EditText etToDate;
        ListView lvMstData;
        Spinner cmbSheetType;
        ImageButton ibtnSheetIdQRScan;
        EditText etSheetId;
        ImageButton ibtnSearch;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_warehouse_storage);

        initialSetUp();

        setListeners();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, getResources().getText(R.string.QRCODE_SCAN_CANCEL).toString(), Toast.LENGTH_SHORT).show();
            } else {
                holder.etSheetId.setText(result.getContents().trim());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initialSetUp() {
        if (holder == null) {
            holder = new ViewHolder();
            holder.etFromDate = findViewById(R.id.etFromDate);
            holder.etToDate = findViewById(R.id.etToDate);
            holder.cmbSheetType = findViewById(R.id.cmbSheetType);
            holder.ibtnSheetIdQRScan = findViewById(R.id.ibtnSheetIdQRScan);
            holder.etSheetId = findViewById(R.id.etSheetId);
            holder.lvMstData = findViewById(R.id.lvMasterSheet);
            holder.ibtnSearch = findViewById(R.id.ibtnSearch);
        }
        holder.etFromDate.setInputType(InputType.TYPE_NULL);
        holder.etToDate.setInputType(InputType.TYPE_NULL);
        mapMstDet = new HashMap<>();
        strSheetPolicyId = "";
        getSheetType();
    }

    // 設置監聽事件
    private void setListeners() {
        holder.etSheetId.setOnKeyListener(onKeyDownSheetId);
        holder.etFromDate.setOnClickListener(fromDateOnClick);
        holder.etToDate.setOnClickListener(toDateOnClick);
        holder.cmbSheetType.setOnItemSelectedListener(cmbSheetTypeOnItemSelected);
        holder.ibtnSheetIdQRScan.setOnClickListener(ibtnSheetIdQRScanOnClick);
        holder.ibtnSearch.setOnClickListener(ibtnSearchOnClick);
        holder.lvMstData.setOnItemClickListener(lvOnClick);
    }

    // 選擇起日
    public void setDateFrom() {
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
        builder.setTitle(R.string.FROM_DATE);

        final AlertDialog dialog = builder.create();
        dialog.show();

        Button bConfirm = view.findViewById(R.id.btnConfirm);
        bConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //日期格式
                StringBuffer sb = new StringBuffer();
                sb.append(String.format("%d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth()));
                holder.etFromDate.setText(sb);
                dialog.cancel();
            }
        });
    }

    // 選擇訖日
    public void setDateTo() {
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
        builder.setTitle(R.string.TO_DATE);

        final AlertDialog dialog = builder.create();
        dialog.show();

        Button bConfirm = view.findViewById(R.id.btnConfirm);
        bConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //日期格式
                StringBuffer sb = new StringBuffer();
                sb.append(String.format("%d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth()));
                holder.etToDate.setText(sb);
                dialog.cancel();
            }
        });
    }

    // 清除起日
    public void onClickFromDateClear(View v) {
        holder.etFromDate.setText("");
    }

    // 清除訖日
    public void onClickToDateClear(View v) { holder.etToDate.setText(""); }

    // 清除單據代碼
    public void onClearSheetId(View v) {
        holder.etSheetId.setText("");
    }

    // 取得單據類型下拉選單
    private void getSheetType() {

        // Call BIModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchSheetType");
        bmObj.setRequestID("BIFetchSheetType");
        bmObj.params = new Vector<>();

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Filter);
        param1.setParameterValue(" AND P.SHEET_TYPE_POLICY_ID in ('Warehouse', 'Return')"); //" AND P.SHEET_TYPE_POLICY_ID in ('Warehouse', 'Return')"
        bmObj.params.add(param1);

        CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    DataTable dtSheetType = bModuleReturn.getReturnJsonTables().get("BIFetchSheetType").get("SHEET_TYPE");
                    DataRow drDefaultItem = dtSheetType.newRow();
                    drDefaultItem.setValue("IDNAME", getResString(getResources().getString(R.string.SELECT_SHEET_TYPE))); // 請選擇單據類型
                    dtSheetType.Rows.add(0, drDefaultItem);
                    List<Map<String, Object>> lstSheetType = (List<Map<String, Object>>) dtSheetType.toListHashMap();

                    SimpleAdapter adapter = new SimpleAdapter(WarehouseStorageActivity.this, lstSheetType, android.R.layout.simple_spinner_item, new String[]{"IDNAME"}, new int[]{android.R.id.text1});
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    holder.cmbSheetType.setAdapter(adapter);
                    holder.cmbSheetType.setSelection(0, true);
                }
            }
        });
    }

    // 取得 Mst & Det
    private void getMstAndDet() {

        // 檢查輸入值
        int compare = holder.etFromDate.getText().toString().compareTo(holder.etToDate.getText().toString());
        if (holder.etFromDate.getText().toString().equals("") && holder.etToDate.getText().toString().equals("") && holder.etSheetId.getText().toString().equals("")) { // holder.SheetId.getText().toString().equals("")
            ShowMessage(R.string.WAPG027001); // WAPG027001 請輸入至少一個條件
            return;
        } else if ((holder.etFromDate.getText().toString().equals("") || holder.etToDate.getText().toString().equals("")) && holder.etSheetId.getText().toString().equals("")) { // holder.SheetId.getText().toString().equals("")
            ShowMessage(R.string.WAPG027002); // WAPG027002 請選擇開單日期(起)和開單日期(迄)
            return;
        } else if (compare == 1) {
            ShowMessage(R.string.WAPG027003); // WAPG027003 開單日期(起)不能大於開單日期(迄)
            return;
        } else if (holder.cmbSheetType.getSelectedItemPosition() == 0) { // 只選日期的話至少也要選單據類型
            ShowMessage(R.string.WAPG027004); // WAPG027004 請選擇單據類型
            return;
        }

        if (!holder.etSheetId.getText().toString().equals(""))
            strSheetId = holder.etSheetId.getText().toString();

        // 裝 Condition 的容器
        HashMap<String, List<?>> mapCon = new HashMap<>();
        List<Condition> lstConSheetId = new ArrayList<>();
        List<Condition> lstConDate = new ArrayList<>();
        List<Condition> lstConStatus = new ArrayList<>();

        // Set BIModule
        List<BModuleObject> lstBmObj = new ArrayList<>();
        BModuleObject bmSheetObj = new BModuleObject();
        BModuleObject bmConfigObj = new BModuleObject();

        switch (strSheetPolicyId) {
            case "Warehouse":
                mapCon.clear();
                bmSheetObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
                bmSheetObj.setModuleID("BIFetchWarehouseVoucherMstAndDet");
                bmSheetObj.setRequestID("BIFetchWarehouseVoucherMstAndDet");
                bmSheetObj.params = new Vector<>();

                // 單據代碼
                if (strSheetId != null && !strSheetId.equals("")) {
                    Condition conSheetId = new Condition();
                    conSheetId.setAliasTable("M");
                    conSheetId.setColumnName("WV_ID");
                    conSheetId.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
                    conSheetId.setValue(strSheetId);
                    lstConSheetId.add(conSheetId);
                    mapCon.put(conSheetId.getColumnName(), lstConSheetId);

                    Condition conStatus = new Condition();
                    conStatus.setAliasTable("M");
                    conStatus.setColumnName("WV_STATUS");
                    conStatus.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
                    conStatus.setValue("Confirmed");
                    lstConStatus.add(conStatus);

                    mapCon.put(conStatus.getColumnName(), lstConStatus);
                }

                // 開立單據時間區間 & 單據狀態為 Confirmed
                if (!holder.etFromDate.getText().toString().equals("") && !holder.etToDate.getText().toString().equals("")) {

                    Condition conCreateDate = new Condition();
                    conCreateDate.setAliasTable("M");
                    conCreateDate.setColumnName("CREATE_DATE");
                    conCreateDate.setDataType("System.DateTime");
                    conCreateDate.setValue(holder.etFromDate.getText().toString() + " 00:00:00");
                    conCreateDate.setValueBetween(holder.etToDate.getText().toString() + " 23:59:59");
                    lstConDate.add(conCreateDate);

                    Condition conStatus = new Condition();
                    conStatus.setAliasTable("M");
                    conStatus.setColumnName("WV_STATUS");
                    conStatus.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
                    conStatus.setValue("Confirmed");
                    lstConStatus.add(conStatus);

                    mapCon.put(conCreateDate.getColumnName(), lstConDate);
                    mapCon.put(conStatus.getColumnName(), lstConStatus);
                }

                // Serialize 序列化
                VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
                VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
                MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vkey, vVal);
                String strCond = msdl.generateFinalCode(mapCon);

                // Input param
                ParameterInfo param1 = new ParameterInfo();
                param1.setParameterID(BIWMSFetchInfoParam.Condition);
                param1.setNetParameterValue(strCond); // 要用set"Net"ParameterValue
                bmSheetObj.params.add(param1);
                lstBmObj.add(bmSheetObj);
                break;

            case "Return":
                mapCon.clear();
                bmConfigObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
                bmConfigObj.setModuleID("BIFetchSheetConfigByID");
                bmConfigObj.setRequestID("BIFetchSheetConfigByID");
                bmConfigObj.params = new Vector<>();

                Condition conConfigId = new Condition();
                conConfigId.setAliasTable("M");
                conConfigId.setColumnName("SHEET_ID");
                conConfigId.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
                conConfigId.setValue(strSheetId);
                lstConSheetId.add(conConfigId);
                mapCon.put(conConfigId.getColumnName(), lstConSheetId);

                // Serialize 序列化
                VirtualClass vkey1 = VirtualClass.create(VirtualClass.VirtualClassType.String);
                VirtualClass vVal1 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
                MesSerializableDictionaryList msdl1 = new MesSerializableDictionaryList(vkey1, vVal1);
                String strCond1 = msdl1.generateFinalCode(mapCon);

                // Input param
                ParameterInfo paramConfigObj = new ParameterInfo();
                paramConfigObj.setParameterID(BIWMSFetchInfoParam.Condition);
                paramConfigObj.setNetParameterValue(strCond1); // 要用set"Net"ParameterValue
                bmConfigObj.params.add(paramConfigObj);
                lstBmObj.add(bmConfigObj);

                mapCon.clear();
                bmSheetObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
                bmSheetObj.setModuleID("BIFetchSheetMstAndDet");
                bmSheetObj.setRequestID("BIFetchSheetMstAndDet");
                bmSheetObj.params = new Vector<>();

                // 單據代碼 & 單據狀態為 Confirmed
                if (strSheetId != null && !strSheetId.equals("")) {
                    Condition conSheetId = new Condition();
                    conSheetId.setAliasTable("M");
                    conSheetId.setColumnName("SHEET_ID");
                    conSheetId.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
                    conSheetId.setValue(strSheetId);
                    lstConSheetId.add(conSheetId);
                    mapCon.put(conSheetId.getColumnName(), lstConSheetId);

                    Condition conStatus = new Condition();
                    conStatus.setAliasTable("M");
                    conStatus.setColumnName("SHEET_STATUS");
                    conStatus.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
                    conStatus.setValue("Confirmed");
                    lstConStatus.add(conStatus);
                    mapCon.put(conStatus.getColumnName(), lstConStatus);
                }

                // 開立單據時間區間 & 單據狀態為 Confirmed
                if (!holder.etFromDate.getText().toString().equals("") && !holder.etToDate.getText().toString().equals("")) {

                    Condition conCreateDate = new Condition();
                    conCreateDate.setAliasTable("M");
                    conCreateDate.setColumnName("CREATE_DATE");
                    conCreateDate.setDataType("System.DateTime");
                    conCreateDate.setValue(holder.etFromDate.getText().toString() + " 00:00:00");
                    conCreateDate.setValueBetween(holder.etToDate.getText().toString() + " 23:59:59");
                    lstConDate.add(conCreateDate);
                    mapCon.put(conCreateDate.getColumnName(), lstConDate);

                    Condition conStatus = new Condition();
                    conStatus.setAliasTable("M");
                    conStatus.setColumnName("SHEET_STATUS");
                    conStatus.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
                    conStatus.setValue("Confirmed");
                    lstConStatus.add(conStatus);

                    mapCon.put(conStatus.getColumnName(), lstConStatus);
                }
                // Serialize 序列化
                VirtualClass vkey2 = VirtualClass.create(VirtualClass.VirtualClassType.String);
                VirtualClass vVal2 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
                MesSerializableDictionaryList msdl2 = new MesSerializableDictionaryList(vkey2, vVal2);
                String strCond2 = msdl2.generateFinalCode(mapCon);

                // Input param
                ParameterInfo paramSheetObj = new ParameterInfo();
                paramSheetObj.setParameterID(BIWMSFetchInfoParam.Condition);
                paramSheetObj.setNetParameterValue(strCond2); // 要用set"Net"ParameterValue
                bmSheetObj.params.add(paramSheetObj);

                lstBmObj.add(bmSheetObj);
                break;
        }


        // Call BIModule
        CallBIModule(lstBmObj, new WebAPIClientEvent() {

            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {

                if (!CheckBModuleReturnInfo(bModuleReturn))
                    return;

                DataTable dtMst = null, dtDet = null, dtConfig = null;

                switch (strSheetPolicyId) {
                    case "Warehouse":
                        dtMst = bModuleReturn.getReturnJsonTables().get("BIFetchWarehouseVoucherMstAndDet").get("WvMst");
                        if (dtMst.Rows.size() == 0) {
                            ShowMessage(R.string.WAPG027005); // WAPG027004 查詢無資料
                            return;
                        }
                        ((DataColumnCollection) dtMst.getColumns()).get("WV_ID").setColumnName("MTL_SHEET_ID");
                        ((DataColumnCollection) dtMst.getColumns()).get("WV_STATUS").setColumnName("MTL_SHEET_STATUS");
                        ((DataColumnCollection) dtMst.getColumns()).get("WV_SOURCE").setColumnName("SOURCE");
                        ((DataColumnCollection) dtMst.getColumns()).get("WV_DET_REF_KEY").setColumnName("SHEET_REF_KEY");
                        dtDet = bModuleReturn.getReturnJsonTables().get("BIFetchWarehouseVoucherMstAndDet").get("WvDet");
                        ((DataColumnCollection) dtDet.getColumns()).get("WV_ID").setColumnName("MTL_SHEET_ID");
                        ((DataColumnCollection) dtDet.getColumns()).get("WV_DET_REF_KEY").setColumnName("SHEET_REF_KEY");
                        dtDet.getColumns().add(new DataColumn("BIN_ID"));
                        for (int pos = 0; pos < dtDet.Rows.size(); pos++)
                            dtDet.Rows.get(pos).setValue("BIN_ID", "");
                        break;
                    case "Return":
                        dtMst = bModuleReturn.getReturnJsonTables().get("BIFetchSheetMstAndDet").get("Mst");
                        if (dtMst.Rows.size() == 0) {
                            ShowMessage(R.string.WAPG027005); // WAPG027004 查詢無資料
                            return;
                        }
                        ((DataColumnCollection) dtMst.getColumns()).get("SHEET_ID").setColumnName("MTL_SHEET_ID");
                        ((DataColumnCollection) dtMst.getColumns()).get("SHEET_DATE").setColumnName("MTL_SHEET_DATE");
                        ((DataColumnCollection) dtMst.getColumns()).get("SHEET_STATUS").setColumnName("MTL_SHEET_STATUS");
                        ((DataColumnCollection) dtMst.getColumns()).get("SHEET_DATA_SOURCE").setColumnName("SOURCE");
                        dtDet = bModuleReturn.getReturnJsonTables().get("BIFetchSheetMstAndDet").get("Det");
                        ((DataColumnCollection) dtDet.getColumns()).get("SHEET_ID").setColumnName("MTL_SHEET_ID");
                        ((DataColumnCollection) dtDet.getColumns()).get("TO_STORAGE_KEY").setColumnName("STORAGE_KEY");
                        ((DataColumnCollection) dtDet.getColumns()).get("TO_STORAGE_ID").setColumnName("STORAGE_ID");
                        ((DataColumnCollection) dtDet.getColumns()).get("TO_BIN_ID").setColumnName("BIN_ID");
                        ((DataColumnCollection) dtDet.getColumns()).get("TRX_QTY").setColumnName("QTY");
                        ((DataColumnCollection) dtDet.getColumns()).get("ITEM_UOM").setColumnName("UOM");
                        ((DataColumnCollection) dtDet.getColumns()).get("TRX_CMT").setColumnName("CMT");
                        dtDet.getColumns().add(new DataColumn("SCRAP_QTY"));
                        dtDet.getColumns().add(new DataColumn("ACTUAL_QTY_STATUS"));

                        dtConfig = bModuleReturn.getReturnJsonTables().get("BIFetchSheetConfigByID").get("Config");
                        String strActualQtyStatus = "";
                        for (DataRow drConfig : dtConfig.Rows) {
                            if (drConfig.get("SHEET_ID").equals(strSheetId)) {
                                strActualQtyStatus = drConfig.get("ACTUAL_QTY_STATUS").toString();
                                break;
                            }
                        }
                        for (int pos = 0; pos < dtDet.Rows.size(); pos++) {
                            dtDet.Rows.get(pos).setValue("SCRAP_QTY", "");
                            dtDet.Rows.get(pos).setValue("ACTUAL_QTY_STATUS", strActualQtyStatus);
                        }

                        break;
                }

                Boolean existed = true; // 判斷是否已有取得 Mst 的紀錄

//                if (dtMst.Rows.size() == 0) {
//                    ShowMessage(R.string.WAPG007004); // 查詢無資料
//                    holder.cmbSheetId.setSelection(0); // 單據代碼回到預設值
//                    return;
//                }

                // 加入 lvMst 的資料
                if (!holder.etSheetId.getText().toString().equals("") && !(dtMstInLv == null || dtMstInLv.Rows.size() == 0)) { // 有選擇 ID 從原本的 dtMstInLv 加一列 Mst
                    for (DataRow drOld : dtMstInLv.Rows) {
                        if (drOld.getValue("MTL_SHEET_ID").toString().equals(dtMst.Rows.get(0).getValue("MTL_SHEET_ID").toString()))
                            existed = false;
                    }
                    if (existed)
                        dtMstInLv.Rows.add(dtMst.Rows.get(0));
                } else { // 沒有有選擇 ID 則直接替換 dtMstInLv
                    dtMstInLv = dtMst;
                }

                // 加入 lv
                if (!holder.etSheetId.getText().toString().equals("") && !(dtMstInLv == null || dtMstInLv.Rows.size() == 0)) { // 有選擇 ID 從原本的 mapMstDet 加一筆(k,v)，v 為此單據的 items table
                    if (existed)
                        mapMstDet.put(dtMst.Rows.get(0).getValue("MTL_SHEET_ID").toString(), dtDet);
                } else {
                    mapMstDet.clear();
                    for (DataRow drNew : dtMst.Rows) {
                        DataTable dt = new DataTable();
                        for (DataRow drNewDet : dtDet.Rows) {
                            if (drNew.getValue("MTL_SHEET_ID").toString().equals(drNewDet.get("MTL_SHEET_ID").toString())) {
                                dt.Rows.add(drNewDet);
                            }
                        }
                        mapMstDet.put(drNew.getValue("MTL_SHEET_ID").toString(), dt);
                        mapMstDet.get(drNew.getValue("MTL_SHEET_ID").toString()).getColumns().addAll(dtDet.getColumns()); // add columns
                    }
                }

                // 顯示在 ListView
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); // 加載layout至此layout上
                WarehouseStorageMstAdapter adapter = new WarehouseStorageMstAdapter(dtMstInLv, inflater);
                holder.lvMstData.setAdapter(adapter);
                holder.etSheetId.setText("");

            }
        });
    }

    // endregion

    //region Event

    private View.OnKeyListener onKeyDownSheetId = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent event) {
            if (keyCode != KeyEvent.KEYCODE_ENTER)
                return false;

            if (event.getAction() == KeyEvent.ACTION_UP) {

                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                getMstAndDet();

                return true;
            }

            return false;
        }
    };

    // 選擇單據類型 -> 取得單據代碼下拉選單內容
    private AdapterView.OnItemSelectedListener cmbSheetTypeOnItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            strSheetTypeId = "";
            strProcessType = "";

            if (position == 0) {
                // 單據類型選擇 pos = 0 為請選擇單據類型
                strSheetPolicyId = "";
            } else {
                Map<String, String> mapSheetType = (Map<String, String>)parent.getItemAtPosition(position);
                strSheetPolicyId = mapSheetType.get("SHEET_TYPE_POLICY_ID");
                strSheetTypeId = mapSheetType.get("SHEET_TYPE_ID");
                if (strSheetPolicyId.equals("Warehouse"))
                    strProcessType = "WV";
                else
                    strProcessType = "PK";
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Auto-generated method stub
        }
    };

    // 點選QRCode掃描
    private View.OnClickListener ibtnSheetIdQRScanOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            IntentIntegrator integrator = new IntentIntegrator(WarehouseStorageActivity.this);
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

    // 點選設置起日
    private AdapterView.OnClickListener fromDateOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setDateFrom();
        }
    };

    // 點選設置訖日
    private AdapterView.OnClickListener toDateOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setDateTo();
        }
    };

    // 查詢取得 Mst 和 Det
    private View.OnClickListener ibtnSearchOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            getMstAndDet();
        }

    };

    // 點選其中一個 Mst 傳送相關資料至下個畫面
    private AdapterView.OnItemClickListener lvOnClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {

            DataRow selectedRow = dtMstInLv.Rows.get(pos);

            if (selectedRow.getValue("SHEET_TYPE_POLICY_ID").equals("Warehouse")) {
                blnIsWV = true;
                strSheetPolicyId = "Warehouse";
            } else {
                blnIsWV = false;
                strSheetPolicyId = "Return";
            }

            // 存放選中的單據
            final DataTable dtMstResult = new DataTable();
            dtMstResult.Rows.add(selectedRow);
            dtMstResult.getColumns().addAll(dtMstInLv.getColumns());

            // 存放選中的單據detail
            final DataTable dtDetResult = new DataTable();
            for (DataRow dr : mapMstDet.get(selectedRow.getValue("MTL_SHEET_ID").toString()).Rows) {
                dtDetResult.Rows.add(dr);
            }
            if (dtDetResult.getColumns() == null || dtDetResult.getColumns().size() == 0) {
                dtDetResult.getColumns().addAll(mapMstDet.get(selectedRow.getValue("MTL_SHEET_ID").toString()).getColumns());
            }

            List<BModuleObject> lstBmObj = new ArrayList<>();
            HashMap<String, List<?>> mapCon1 = new HashMap<>();
            List<Condition> lstConWvId = new ArrayList<>();

            // region Set BIModule
            BModuleObject bmWvrObj = new BModuleObject();
            bmWvrObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
            bmWvrObj.setModuleID("BIFetchWarehouseVoucherReceiveDet");
            bmWvrObj.setRequestID("BIFetchWarehouseVoucherReceiveDet");
            bmWvrObj.params = new Vector<>();

            BModuleObject bmWvrGroupBySkuLevelObj = new BModuleObject();
            bmWvrGroupBySkuLevelObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
            bmWvrGroupBySkuLevelObj.setModuleID("BIFetchWarehouseVoucherReceiveDetGroupBySkuLevel");
            bmWvrGroupBySkuLevelObj.setRequestID("BIFetchWarehouseVoucherReceiveDetGroupBySkuLevel");
            bmWvrGroupBySkuLevelObj.params = new Vector<>();

            BModuleObject bmWvrWithPackingInfo = new BModuleObject();
            bmWvrWithPackingInfo.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
            bmWvrWithPackingInfo.setModuleID("BIFetchWarehouseVoucherReceiveDetWithPackingInfo");
            bmWvrWithPackingInfo.setRequestID("BIFetchWarehouseVoucherReceiveDetWithPackingInfo");
            bmWvrWithPackingInfo.params = new Vector<>();

            Condition conWvId = new Condition();
            conWvId.setAliasTable("M");
            conWvId.setColumnName("WV_ID");
            conWvId.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
            conWvId.setValue(selectedRow.getValue("MTL_SHEET_ID").toString());
            lstConWvId.add(conWvId);
            mapCon1.put(conWvId.getColumnName(), lstConWvId);

            // Serialize 序列化
            VirtualClass vkey3 = VirtualClass.create(VirtualClass.VirtualClassType.String);
            VirtualClass vVal3 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
            MesSerializableDictionaryList msdl3 = new MesSerializableDictionaryList(vkey3, vVal3);
            String strCond3 = msdl3.generateFinalCode(mapCon1);

            ParameterInfo paramWvIdObj = new ParameterInfo();
            paramWvIdObj.setParameterID(BIWMSFetchInfoParam.Condition);
            paramWvIdObj.setNetParameterValue(strCond3); // 要用set"Net"ParameterValue
            bmWvrObj.params.add(paramWvIdObj);
            lstBmObj.add(bmWvrObj);

            ParameterInfo paramWvIdGroupBySkuLevelObj = new ParameterInfo();
            paramWvIdGroupBySkuLevelObj.setParameterID(BIWMSFetchInfoParam.Condition);
            paramWvIdGroupBySkuLevelObj.setNetParameterValue(strCond3); // 要用set"Net"ParameterValue
            bmWvrGroupBySkuLevelObj.params.add(paramWvIdGroupBySkuLevelObj);
            lstBmObj.add(bmWvrGroupBySkuLevelObj);

            ParameterInfo paramWvIdWithPackingInfoObj = new ParameterInfo();
            paramWvIdWithPackingInfoObj.setParameterID(BIWMSFetchInfoParam.Condition);
            paramWvIdWithPackingInfoObj.setNetParameterValue(strCond3); // 要用set"Net"ParameterValue
            bmWvrWithPackingInfo.params.add(paramWvIdWithPackingInfoObj);
            lstBmObj.add(bmWvrWithPackingInfo);
            // endregion

            CallBIModule(lstBmObj, new WebAPIClientEvent() {

                @Override
                public void onPostBack(BModuleReturn bModuleReturn) {

                    if (!CheckBModuleReturnInfo(bModuleReturn))
                        return;

                    DataTable dtWvrDet = bModuleReturn.getReturnJsonTables().get("BIFetchWarehouseVoucherReceiveDet").get("WvrDet");
                    DataTable dtWvrDetGroup = bModuleReturn.getReturnJsonTables().get("BIFetchWarehouseVoucherReceiveDetGroupBySkuLevel").get("WvrDetGroupBySkuLevel");
                    DataTable dtWvrDetWithPackingInfo = bModuleReturn.getReturnJsonTables().get("BIFetchWarehouseVoucherReceiveDetWithPackingInfo").get("WvrDetWithPackingInfo");

                    // 打包選中的 Mst 及 Det 資料至下個畫面
                    Bundle sheetInfo = new Bundle();
                    sheetInfo.putString("sheetTypeId", strSheetTypeId);
                    sheetInfo.putString("sheetPolicyId", strSheetPolicyId);
                    sheetInfo.putBoolean("blnIsWV", blnIsWV);
                    sheetInfo.putSerializable("dtMstResult", dtMstResult);
                    sheetInfo.putSerializable("dtDetResult", dtDetResult);
                    sheetInfo.putSerializable("dtWvrDet", dtWvrDet);
                    sheetInfo.putSerializable("dtWvrDetGroup", dtWvrDetGroup);
                    sheetInfo.putSerializable("dtWvrDetWithPackingInfo", dtWvrDetWithPackingInfo);
                    gotoNextActivity(WarehouseStorageDetailNewActivity.class, sheetInfo);
                }
            });


        }
    };
    // endregion
}

// ERROR CODE
// WAPG027001   請輸入至少一個條件
// WAPG027002   請選擇開單日期(起)和開單日期(迄)
// WAPG027003   開單日期(起)不能大於開單日期(迄)
// WAPG027004   請選擇單據類型
// WAPG027005   查詢無資料
// WAPG027006   物料[%s]管控批號，需要輸入批號
// WAPG027007   請輸入數量
// WAPG027008   請輸入報廢數量
// WAPG027009   數量、報廢數量不可同時為0！
// WAPG027010   請輸入製造日期
// WAPG027011   請輸入有效期限
// WAPG027012   製造日期不可大於有效期限
// WAPG027013   單據類型「%s」未設定「單據Config設定」內的【實際數量的狀態】，請先設定!
// WAPG027014   輸入批號「%s」與單據項次「%s」指定的批號「%s」不一致!
// WAPG027015   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，批號數量需相等!
// WAPG027016   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，批號報廢數量需相等!
// WAPG027017   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號數量「%s」不可小於單據數量「%s」!
// WAPG027018   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號報廢數量「%s」不可小於單據報廢數量「%s」!
// WAPG027019   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號數量「%s」不可大於單據數量「%s」!
// WAPG027020   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號報廢數量「%s」不可大於單據報廢數量「%s」!
// WAPG027021   批號已存在!
// WAPG027022   物料[%s]未設定WMS物料設定檔
// WAPG027023   物料[%s]不管控批號，批號需為空白
// WAPG027024   批號[%s]不存在於MES
// WAPG027025   批號[%s]非最外箱,需輸入最外層箱號,上層ID[%s]
// WAPG027026   批號[%s]數量[%s]需等於[%s]
// WAPG027027   是否刪除此批號?
// WAPG027028   請新增批號
// WAPG027029   單據項次「%s」沒有明細!
// WAPG027030   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，項次「%s」批號數量需相等!
// WAPG027031   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，項次「%s」批號報廢數量需相等!
// WAPG027032   查無對應的入庫儲位
// WAPG027033   尚未選擇倉庫[%s]物料[%s]對應儲位
// WAPG027034   作業成功
