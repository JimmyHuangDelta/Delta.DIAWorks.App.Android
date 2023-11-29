package com.delta.android.WMS.Client;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
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
import com.delta.android.WMS.Client.GridAdapter.SheetDetGridAdapter;
import com.delta.android.WMS.Param.BIFetchPickStrategyParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.BIWMSPickByLightParam;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.delta.android.WMS.Client.GridAdapter.SheetMstGridAdapter;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class GoodPickActivity extends BaseFlowActivity {
    //WAPG001
    ViewHolder holder = null;
    int checkedCount = 0;
    String confirmName = null;
    DataTable SheetMstTable = null;
    DataTable SheetCfgTable = null;
    DataTable SheetTypeTable = null;
    HashMap<String, DataTable> SheetDetTables;
    HashMap<String, String> SheetActStatus;
    ArrayList<String> sheetTypes = null;
    private HashMap<String, String> mapSheetTypeKey = new HashMap<String, String>();
    HashMap<String, String>mapSheet = new HashMap<>();

    static class ViewHolder {
        EditText FromDate;
        EditText ToDate;
        EditText SheetId;
        Spinner MtlSheetType;
        ListView MasterSheetData;
        Button Confirm;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_good_pick);

        this.InitialSetup();

        // 取得單據類型
        GetSheetType();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, getResources().getText(R.string.QRCODE_SCAN_CANCEL).toString(), Toast.LENGTH_SHORT).show();
            } else {
                holder.SheetId.setText(result.getContents().trim());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void InitialSetup() {
        if (holder == null) {
            holder = new ViewHolder();
            holder.FromDate = findViewById(R.id.etFromDate);
            holder.ToDate = findViewById(R.id.etToDate);
            holder.MtlSheetType = findViewById(R.id.cmbMtlSheetType);
            holder.MasterSheetData = findViewById(R.id.lvMasterSheet);
            holder.SheetId = findViewById(R.id.etSheetId);
            holder.Confirm = findViewById(R.id.bConfirm);
        }
        holder.FromDate.setInputType(InputType.TYPE_NULL);
        holder.ToDate.setInputType(InputType.TYPE_NULL);
        SheetDetTables = new HashMap<>();
        //sheetTypes = new ArrayList<>();
        confirmName = holder.Confirm.getText().toString();

        //region 修改用一般的方式搜尋即可
//        holder.SheetId.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_SEARCH){
//                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                    FetchSheetIdInoformation();
//                }
//                return false;
//            }
//        });
        //endregion

        holder.SheetId.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //只有按下Enter才會反映
                if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    String strSheetTypePolicyId = SheetTypeTable.Rows.get(holder.MtlSheetType.getSelectedItemPosition()-1).getValue("SHEET_TYPE_POLICY_ID").toString();

                    //PolicyId是Issue、Transfer時才需要去尋找揀料單代碼
                    if (strSheetTypePolicyId.equals("Issue") || strSheetTypePolicyId.equals("Transfer"))
                    {
                        GetPickingID();
                    }
                    else
                    {
                        FetchSheetIdByPickSht();
                    }

                    return true;
                }
                return false;
            }
        });
    }

    //取得單據類型
    private void GetSheetType() {
        // Call BIModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchWmsSheetConfig");
        bmObj.setRequestID("BIFetchSheetType");
        bmObj.params = new Vector<ParameterInfo>();

        /*條件1->畫面Config設定的單據類型再去做篩選
         * 條件2->直接指定單據類型為Picking
         * 條件1跟2擇一*/
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Filter);
        param1.setParameterValue(" AND ((STP.SHEET_TYPE_POLICY_ID in ('Issue','Transfer','Picking') AND CFG.TO_PICKING_MODE = 'Auto' AND CFG.STORAGE_ACTION_TYPE = 'From') OR STP.SHEET_TYPE_POLICY_ID ='Picking') ");
        bmObj.params.add(param1);

        CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    DataTable dt = bModuleReturn.getReturnJsonTables().get("BIFetchSheetType").get("SBRM_WMS_SHEET_CONFIG");
                    sheetTypes = new ArrayList<String>();

                    // 下拉選單預設選項依語系調整
                    String strSelectSheetType = getResString(getResources().getString(R.string.SELECT_SHEET_TYPE));
                    sheetTypes.add(strSelectSheetType);
                    mapSheetTypeKey.put(strSelectSheetType,"null");

                    for (DataRow dr : dt.Rows) {
                        sheetTypes.add(dr.getValue("SHEET_TYPE_ID").toString());
                        mapSheetTypeKey.put(dr.getValue("SHEET_TYPE_ID").toString(), dr.getValue("SHEET_TYPE_ID").toString());
                    }

                    ArrayAdapter<String> adapterSheetType = new ArrayAdapter<>(GoodPickActivity.this, android.R.layout.simple_spinner_dropdown_item, sheetTypes);
                    holder.MtlSheetType.setAdapter(adapterSheetType);

                    SheetTypeTable = dt;
                }
            }
        });
    }

    public void FromDateOnClick(View v) {

        //region 將原本的日曆顯示方式更改為Dialog跳出轉軸調整日期
//        Calendar c = Calendar.getInstance();
//        new DatePickerDialog(GoodPickActivity.this, new DatePickerDialog.OnDateSetListener() {
//            @Override
//            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//                String strMonth;
//                String strDay;
//                if ((month+1) / 10 == 0) strMonth = "0"+ (month+1);
//                else strMonth = String.valueOf(month+1);
//                if (dayOfMonth / 10 == 0) strDay = "0"+dayOfMonth;
//                else strDay = String.valueOf(dayOfMonth);
//                holder.FromDate.setText((year)+"/" + strMonth +"/"+ strDay);
//            }
//        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
//        ).show();
        //endregion

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.style_wms_dialog_date_picker_spinner, null);
        final DatePicker datePicker = view.findViewById(R.id.date_picker);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);

        builder.setView(view);
        builder.setTitle(R.string.FROM_DATE);
//        builder.setPositiveButton(R.string.CONFIRM, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                //日期格式
//                StringBuffer sb = new StringBuffer();
//                sb.append(String.format("%d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth()));
//                holder.FromDate.setText(sb);
//                dialog.cancel();
//            }
//        });
        final AlertDialog dialog = builder.create();
        dialog.show();

        Button bConfirm = view.findViewById(R.id.btnConfirm);
        bConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //日期格式
                StringBuffer sb = new StringBuffer();
                sb.append(String.format("%d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth()));
                holder.FromDate.setText(sb);
                dialog.cancel();
            }
        });


//        Button bConfirm = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
//        if (bConfirm != null) {
//            bConfirm.setTextColor(getResources().getColor(R.color.common_theme_blue1));
//            bConfirm.setGravity(Gravity.CENTER_HORIZONTAL);
//        }
    }

    public void ToDateOnClick(View v) {
        //region 將原本的日曆顯示方式更改為Dialog跳出轉軸調整日期
//        Calendar c = Calendar.getInstance();
//        new DatePickerDialog(GoodPickActivity.this, new DatePickerDialog.OnDateSetListener() {
//            @Override
//            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//                String strMonth;
//                String strDay;
//                if ((month+1) / 10 == 0) strMonth = "0"+ (month+1);
//                else strMonth = String.valueOf(month+1);
//                if (dayOfMonth / 10 == 0) strDay = "0"+dayOfMonth;
//                else strDay = String.valueOf(dayOfMonth);
//                holder.ToDate.setText((year)+"/" + strMonth +"/"+ strDay);
//            }
//        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
//        ).show();
        //endregion

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.style_wms_dialog_date_picker_spinner, null);
        final DatePicker datePicker = view.findViewById(R.id.date_picker);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);

        builder.setView(view);
        builder.setTitle(R.string.TO_DATE);
//        builder.setPositiveButton(R.string.CONFIRM, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                //日期格式
//                StringBuffer sb = new StringBuffer();
//                sb.append(String.format("%d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth()));
//                holder.ToDate.setText(sb);
//                dialog.cancel();
//            }
//        });
        final AlertDialog dialog = builder.create();
        dialog.show();

        Button bConfirm = view.findViewById(R.id.btnConfirm);
        bConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //日期格式
                StringBuffer sb = new StringBuffer();
                sb.append(String.format("%d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth()));
                holder.ToDate.setText(sb);
                dialog.cancel();
            }
        });

//        Button bConfirm = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
//        if (bConfirm != null) {
//            bConfirm.setTextColor(getResources().getColor(R.color.common_theme_blue1));
//        }

    }

    public void ClickConfirm(View v) {
        if (SheetMstTable == null || SheetMstTable.Rows.size() == 0) return;

        ArrayList<String> ids = new ArrayList<String>();
        String strSheetTypePolicyId = SheetMstTable.Rows.get(0).getValue("SHEET_TYPE_POLICY_ID").toString();

        for (int i = 0; i < SheetMstTable.Rows.size(); i++) {
            if (SheetMstTable.Rows.get(i).getValue("SELECTED").toString().equals("true")) {
                ids.add(SheetMstTable.Rows.get(i).getValue("SHEET_ID").toString());
            }
        }

        if(ids.size()==0)
        {
            ShowMessage(R.string.WAPG001007);
            return;
        }

        CheckPBL(ids, strSheetTypePolicyId);
    }

    public void OnClickIbSearch(View v) {
        //region Check Input
        int compare = holder.FromDate.getText().toString().compareTo(holder.ToDate.getText().toString());
        if (holder.FromDate.getText().toString().equals("") && holder.ToDate.getText().toString().equals("") && holder.SheetId.getText().toString().equals("")) {
            ShowMessage(R.string.WAPG001004);
            return;
        } else if ((holder.FromDate.getText().toString().equals("") || holder.ToDate.getText().toString().equals("")) && holder.SheetId.getText().toString().equals("")) {
            ShowMessage(R.string.WAPG001005);
            return;
        } else if (compare == 1) {
            ShowMessage(R.string.WAPG001006);
            return;
        }
        // endregion

        this.FetchSheetIdInoformation();
    }

    public void OnClickQRScan(View v) {
        IntentIntegrator integrator = new IntentIntegrator(GoodPickActivity.this);
        // 設置要掃描的條碼類型，ONE_D_CODE_TYPES：一維碼，QR_CODE_TYPES-二維碼
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("BarCode Scan"); //底部的提示文字
        integrator.setCameraId(0); //前置(1)或者後置(0)攝像頭
        integrator.setBeepEnabled(false); //掃描成功的「嗶嗶」聲
        integrator.setBarcodeImageEnabled(false); //是否保留掃碼成功時候的截圖
        integrator.setCaptureActivity(ScanActivity.class);
        integrator.initiateScan();
    }

    private void FetchSheetIdInoformation() {

        if (holder.FromDate.getText().toString().equals("") &&
                holder.ToDate.getText().toString().equals("") &&
                holder.SheetId.getText().toString().equals("")) return;

        String strSheetTypePolicyId = SheetTypeTable.Rows.get(holder.MtlSheetType.getSelectedItemPosition()-1).getValue("SHEET_TYPE_POLICY_ID").toString();

        //PolicyId是Issue、Transfer時才需要去尋找揀料單代碼
        if (strSheetTypePolicyId.equals("Issue") || strSheetTypePolicyId.equals("Transfer"))
        {
            GetPickingID();
        }
        else
        {
            FetchSheetIdByPickSht();
        }
    }

    //根據原始單據取得對應的揀料單
    private void GetPickingID() {
        BModuleObject biObj = new BModuleObject();
        biObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        biObj.setModuleID("BIFetchPickShtID");
        biObj.setRequestID("BIFetchPickShtID");
        biObj.params = new Vector<>();
        VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");

        List<Condition> conditionIds = new ArrayList<>();
        List<Condition> conditionStatuss = new ArrayList<>();
        List<Condition> conditionSheetTypes = new ArrayList<>();
        List<Condition> conditionCreateDate = new ArrayList<>();

        HashMap<String, List<?>> dicCondition = new HashMap<>();

        if (!holder.SheetId.getText().toString().equals("")) {
            Condition condition = new Condition();
            condition.setAliasTable("M");
            condition.setColumnName("SHEET_ID");
            condition.setDataType("string");
            condition.setValue(holder.SheetId.getText().toString().toUpperCase().trim()); //20200729 archie 轉大寫
            conditionIds.add(condition);
            dicCondition.put("SHEET_ID", conditionIds);
        }

        Condition condSheetStatus = new Condition();
        condSheetStatus.setAliasTable("M");
        condSheetStatus.setColumnName("SHEET_STATUS");
        condSheetStatus.setDataType("string");
        condSheetStatus.setValue("Confirmed");
        conditionStatuss.add(condSheetStatus);
        dicCondition.put("SHEET_STATUS", conditionStatuss);

        final String strSheetTypeId = mapSheetTypeKey.get(holder.MtlSheetType.getSelectedItem().toString());
        if (strSheetTypeId.equals("null")){
            ShowMessage(R.string.WAPG001012); //請選擇單據類型
            return;
        }

        Condition condSheetType = new Condition();
        condSheetType.setAliasTable("ST");
        condSheetType.setColumnName("SHEET_TYPE_ID");
        condSheetType.setDataType("string");
        condSheetType.setValue(strSheetTypeId);
        conditionSheetTypes.add(condSheetType);
        dicCondition.put("SHEET_TYPE_ID", conditionSheetTypes);

        if (!holder.FromDate.getText().toString().equals("") && !holder.ToDate.getText().toString().equals("")) {
            Condition condDate = new Condition();
            condDate.setAliasTable("M");
            condDate.setColumnName("CREATE_DATE");
            condDate.setDataType("System.DateTime");
            condDate.setValue(holder.FromDate.getText().toString() + " 00:00:00");
            condDate.setValueBetween(holder.ToDate.getText().toString() + " 23:59:59");
            conditionCreateDate.add(condDate);
            dicCondition.put("CREATE_DATE", conditionCreateDate);
        }

        if (dicCondition.size() != 0) {
            MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vKey, vVal);
            String serializedObj = msdl.generateFinalCode(dicCondition);
            ParameterInfo param1 = new ParameterInfo();
            param1.setParameterID(BIWMSFetchInfoParam.Condition);
            param1.setNetParameterValue(serializedObj);
            biObj.params.add(param1);
        }

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIWMSFetchInfoParam.OnlyTableColumn);
        param2.setParameterValue("N");
        biObj.params.add(param2);

        List<BModuleObject> lstBmObj = new ArrayList<BModuleObject>();
        lstBmObj.add(biObj);

        CallBIModule(lstBmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    DataTable dtPick = bModuleReturn.getReturnJsonTables().get("BIFetchPickShtID").get("PICKING_ID");

                    if (dtPick.Rows.size() <= 0)
                    {
                        //WAPG001013 單據類型[{0}]查無轉揀料單資料
                        ShowMessage(R.string.WAPG001013, strSheetTypeId);
                        return ;
                    }

                    FetchSheetId(dtPick);
                }
            }
        });
    }

    //用原始單據找到的揀料單資料去找揀料單的Mst、Det
    private void FetchSheetId(DataTable dtPick) {

        if (holder.FromDate.getText().toString().equals("") &&
                holder.ToDate.getText().toString().equals("") &&
                holder.SheetId.getText().toString().equals("")) return;

        final String strSourceShtId = holder.SheetId.getText().toString().trim();

        BModuleObject biObj = new BModuleObject();
        biObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        biObj.setModuleID("BIFetchSheetMstAndDet");
        biObj.setRequestID("BIFetchSheetMst");
        biObj.params = new Vector<>();
        VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");

        List<Condition> conditionIds = new ArrayList<>();
        List<Condition> conditionStatuss = new ArrayList<>();
        List<Condition> conditionSheetTypes = new ArrayList<>();
        List<Condition> conditionCreateDate = new ArrayList<>();
        String strShtId = "";
        List<String>lstShtId = new ArrayList<>();
        mapSheet = new HashMap<>(); //key:PickSht, Value:SourceSht

        for (DataRow dr : dtPick.Rows)
        {
            if (!lstShtId.equals(dr.getValue("SHEET_ID").toString()))
                lstShtId.add(dr.getValue("SHEET_ID").toString());

            if (!mapSheet.containsKey(dr.getValue("SHEET_ID").toString()))
                mapSheet.put(dr.getValue("SHEET_ID").toString(), dr.getValue("SOURCE_SHT_ID").toString());
        }

        strShtId = TextUtils.join("','", lstShtId);

        HashMap<String, List<?>> dicCondition = new HashMap<>();

        Condition condSheetStatus = new Condition();
        condSheetStatus.setAliasTable("M");
        condSheetStatus.setColumnName("SHEET_STATUS");
        condSheetStatus.setDataType("string");
        condSheetStatus.setValue("Confirmed");
        conditionStatuss.add(condSheetStatus);
        dicCondition.put("SHEET_STATUS", conditionStatuss);

        /*
        //for (String type: sheetTypes)
        //{
        //    Condition condSheetType = new Condition();
        //    condSheetType.setAliasTable("ST");
        //    condSheetType.setColumnName("SHEET_TYPE_ID");
        //    condSheetType.setDataType("string");
        //    condSheetType.setValue(type);
        //    conditionSheetTypes.add(condSheetType);
        //}
         */

        String strSheetTypeKey = dtPick.Rows.get(0).getValue("SHEET_TYPE_KEY").toString();
        if (strSheetTypeKey.equals("null")){
            ShowMessage(R.string.WAPG001012); //請選擇單據類型
            return;
        }

        Condition condSheetType = new Condition();
        condSheetType.setAliasTable("ST");
        condSheetType.setColumnName("SHEET_TYPE_KEY");
        condSheetType.setDataType("string");
        condSheetType.setValue(strSheetTypeKey);
        conditionSheetTypes.add(condSheetType);
        dicCondition.put("SHEET_TYPE_KEY", conditionSheetTypes);

        if (!holder.FromDate.getText().toString().equals("") && !holder.ToDate.getText().toString().equals("")) {
            Condition condDate = new Condition();
            condDate.setAliasTable("M");
            condDate.setColumnName("CREATE_DATE");
            condDate.setDataType("System.DateTime");
            condDate.setValue(holder.FromDate.getText().toString() + " 00:00:00");
            condDate.setValueBetween(holder.ToDate.getText().toString() + " 23:59:59");
            conditionCreateDate.add(condDate);
            dicCondition.put("CREATE_DATE", conditionCreateDate);
        }

        if (dicCondition.size() != 0) {
            MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vKey, vVal);
            String serializedObj = msdl.generateFinalCode(dicCondition);
            ParameterInfo param1 = new ParameterInfo();
            param1.setParameterID(BIWMSFetchInfoParam.Condition);
            param1.setNetParameterValue(serializedObj);
            biObj.params.add(param1);
        }

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIWMSFetchInfoParam.OnlyTableColumn);
        param2.setParameterValue("N");
        biObj.params.add(param2);

        ParameterInfo paramShtId = new ParameterInfo();
        paramShtId.setParameterID(BIWMSFetchInfoParam.Filter);
        paramShtId.setParameterValue(String.format(" AND M.SHEET_ID IN ('%s') ", strShtId));
        biObj.params.add(paramShtId);

        // region 取得該Sheet的Config設定
        BModuleObject bmCfgObj = new BModuleObject();
        bmCfgObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmCfgObj.setModuleID("BIFetchSheetConfigByID");
        bmCfgObj.setRequestID("FetchSheetConfigByID");
        bmCfgObj.params =  new Vector<ParameterInfo>();
        String strFilterCfg = "";

        HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
        List<Condition> lstCondition = new ArrayList<Condition>();
        Condition conditionSheetID = null;
        if(!holder.FromDate.getText().toString().equals("") && !holder.ToDate.getText().toString().equals("")){
            conditionSheetID = new Condition();
            conditionSheetID.setAliasTable("M");
            conditionSheetID.setColumnName("CREATE_DATE");
            conditionSheetID.setDataType("System.DateTime");
            conditionSheetID.setValue(holder.FromDate.getText().toString() + " 00:00:00");
            conditionSheetID.setValueBetween(holder.ToDate.getText().toString() + " 23:59:59");
            lstCondition.add(conditionSheetID);
            strFilterCfg = "AND SWSC.STORAGE_ACTION_TYPE = 'From'";
        }
        else if (!holder.SheetId.getText().toString().equals(""))
        {
            strFilterCfg = String.format(" AND SWSC.STORAGE_ACTION_TYPE = 'From' AND M.SHEET_ID IN ('%s') ", strShtId);
        }
        else {
            ShowMessage(R.string.WAPG001010);//WAPG001010   請輸入起訖時間
            return;
        }

        if (mapCondition.size() > 0)
        {
            mapCondition.put(conditionSheetID.getColumnName(),lstCondition);

            // Serialize序列化
            VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
            VirtualClass vVa2 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
            MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vkey, vVa2);
            String strCond = msdl.generateFinalCode(mapCondition);

            ParameterInfo param3 = new ParameterInfo();
            param3.setParameterID(BIWMSFetchInfoParam.Condition);
            param3.setNetParameterValue(strCond);
            bmCfgObj.params.add(param3);
        }

        // Input param

        ParameterInfo param4 = new ParameterInfo();
        param4.setParameterID(BIWMSFetchInfoParam.Filter);
        param4.setParameterValue( strFilterCfg);
        bmCfgObj.params.add(param4);
        // endregion

        // region 取得ConfigCond及ConfigSort
        BModuleObject biShtCfgSortAndCond = new BModuleObject();
        biShtCfgSortAndCond.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        biShtCfgSortAndCond.setModuleID("BIFetchConfigCondAndSort");
        biShtCfgSortAndCond.setRequestID("FetchConfigCondAndSort");
        biShtCfgSortAndCond.params = new Vector<ParameterInfo>();
        ParameterInfo paramShtTypeKey = new ParameterInfo();
        paramShtTypeKey.setParameterID(BIFetchPickStrategyParam.SheetTypeKey);
        paramShtTypeKey.setParameterValue(strSheetTypeKey);
        biShtCfgSortAndCond.params.add(paramShtTypeKey);
        // endregion

        List<BModuleObject> lstBmObj = new ArrayList<BModuleObject>();
        lstBmObj.add(biObj);
        lstBmObj.add(bmCfgObj);
        lstBmObj.add(biShtCfgSortAndCond);

        CallBIModule(lstBmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    DataTable dtMst = bModuleReturn.getReturnJsonTables().get("BIFetchSheetMst").get("Mst");
                    DataTable dtDet = bModuleReturn.getReturnJsonTables().get("BIFetchSheetMst").get("Det");

                    //20201019 Hans DetTable新增 ACTUAL_QTY_STATUS欄位
                    SheetActStatus = new HashMap<>();
                    DataColumn dc = new DataColumn("ACTUAL_QTY_STATUS");
                    dtDet.addColumn(dc);
                    SheetCfgTable = bModuleReturn.getReturnJsonTables().get("FetchSheetConfigByID").get("Config");

                    if (dtMst == null || dtMst.Rows.size() == 0) {
                        //目前篩選條件無Conifrmed的單據資料!
                        ShowMessage(R.string.WAPG001001);
                    }else{
                        //region 檢查單據類型, 單據Config設定[實際數量的狀態]是否設定
                        List<String> unsetActualQtyStatusSheet = new ArrayList<String>();
                        for (DataRow dr : SheetCfgTable.Rows){
                            String sheetTypeName = dr.getValue("SHEET_TYPE_NAME").toString();
                            if (dr.getValue("ACTUAL_QTY_STATUS").toString() == "" && !unsetActualQtyStatusSheet.contains(sheetTypeName)){
                                unsetActualQtyStatusSheet.add(sheetTypeName);
                            }
                            SheetActStatus.put(dr.getValue("SHEET_ID").toString(),dr.getValue("ACTUAL_QTY_STATUS").toString());
                        }

                        if(unsetActualQtyStatusSheet.size() > 0){
                            ShowMessage(R.string.WAPG001009, TextUtils.join(" , ", unsetActualQtyStatusSheet));
                        }
                        //endregion
                    }

                    if (!dtMst.getColumns().contains("SOURCE_SHEET_ID"))
                    {
                        DataColumn dcSource = new DataColumn("SOURCE_SHEET_ID");
                        dtMst.addColumn(dcSource);

                        for (DataRow dr : dtMst.Rows)
                        {
                            dr.setValue("SOURCE_SHEET_ID", mapSheet.get(dr.getValue("SHEET_ID").toString()));
                        }
                    }

                    if (!dtDet.getColumns().contains("SOURCE_SHEET_ID"))
                    {
                        DataColumn dcSource = new DataColumn("SOURCE_SHEET_ID");
                        dtDet.addColumn(dcSource);

                        for (DataRow dr : dtDet.Rows)
                        {
                            dr.setValue("SOURCE_SHEET_ID", mapSheet.get(dr.getValue("SHEET_ID").toString()));
                        }
                    }

                    if (!dtMst.getColumns().contains("ARGB_LIGHT_COLOR"))
                    {
                        DataColumn dcSource = new DataColumn("ARGB_LIGHT_COLOR");
                        dtMst.addColumn(dcSource);

                        for (DataRow dr : dtMst.Rows)
                        {
                            dr.setValue("ARGB_LIGHT_COLOR", "");
                        }
                    }

                    if (!dtDet.getColumns().contains("ARGB_LIGHT_COLOR"))
                    {
                        DataColumn dcSource = new DataColumn("ARGB_LIGHT_COLOR");
                        dtDet.addColumn(dcSource);

                        for (DataRow dr : dtDet.Rows)
                        {
                            dr.setValue("ARGB_LIGHT_COLOR", "");
                        }
                    }


                    if (SheetMstTable == null || SheetMstTable.Rows.size() == 0) {
                        SheetMstTable = dtMst;
                    } else {
                        for (DataRow drNew : dtMst.Rows) {
                            boolean needAdded = true;
                            for (DataRow drOld : SheetMstTable.Rows) {
                                if (drNew.getValue("SHEET_ID").equals(drOld.getValue("SHEET_ID"))) {
                                    needAdded=false;
                                    break;
                                }
                            }
                            if (needAdded) SheetMstTable.Rows.add(drNew);
                        }
                    }

                    //for (DataRow dr : SheetMstTable.Rows) dr.setValue("SELECTED", "true");

                    if (SheetDetTables == null || SheetDetTables.size() == 0) {
                        for (DataRow drNew : dtMst.Rows) {
                            DataTable dt = new DataTable();
                            for (DataRow drNewDet : dtDet.Rows) {
                                if (drNew.getValue("SHEET_ID").toString().equals(drNewDet.getValue("SHEET_ID").toString())) {
                                    //填入單據ACTUAL_QTY_STATUS
                                    drNewDet.setValue("ACTUAL_QTY_STATUS",SheetActStatus.get(drNewDet.getValue("SHEET_ID").toString()));
                                    dt.Rows.add(drNewDet);
                                }
                            }
                            SheetDetTables.put(drNew.getValue("SHEET_ID").toString(), dt);
                        }
                    } else {
                        List<String> detIds = new ArrayList<>();
                        for (DataRow dr : dtDet.Rows) {
                            if (SheetDetTables.containsKey(dr.getValue("SHEET_ID").toString())) {
                                continue;
                            }
                            //填入單據ACTUAL_QTY_STATUS
                            dr.setValue("ACTUAL_QTY_STATUS",SheetActStatus.get(dr.getValue("SHEET_ID").toString()));
                            detIds.add(dr.getValue("SHEET_ID").toString());
                        }

                        for (String str : detIds) {
                            DataTable dt = new DataTable();
                            for (DataRow dr : dtDet.Rows) {
                                if (dr.getValue("SHEET_ID").toString().equals(str)) {
                                    dt.Rows.add(dr);
                                }
                            }
                            SheetDetTables.put(str, dt);
                        }
                    }
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    SheetMstGridAdapter adapter = new SheetMstGridAdapter(SheetMstTable, inflater);
                    holder.MasterSheetData.setAdapter(adapter);
                    holder.MasterSheetData.setOnItemClickListener(onClickListView);
                    checkedCount = 0;
                    for (DataRow dr : SheetMstTable.Rows)
                    {
                        if(dr.getValue("SELECTED").toString().equals("true"))
                            checkedCount++;
                    }
                    //checkedCount = SheetMstTable.Rows.size();
                    holder.Confirm.setText(String.format("%s(%s)", confirmName, checkedCount + "/" + SheetMstTable.Rows.size()));

                    if (SheetMstTable.Rows.size() > 0)
                        holder.MtlSheetType.setEnabled(false);
                }
            }
        });
        holder.SheetId.setText("");
    }


    private void FetchSheetIdByPickSht() {

        if (holder.FromDate.getText().toString().equals("") &&
                holder.ToDate.getText().toString().equals("") &&
                holder.SheetId.getText().toString().equals("")) return;

        final String strSourceShtId = holder.SheetId.getText().toString().trim();

        BModuleObject biObj = new BModuleObject();
        biObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        biObj.setModuleID("BIFetchSheetMstAndDet");
        biObj.setRequestID("BIFetchSheetMst");
        biObj.params = new Vector<>();
        VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");

        List<Condition> conditionIds = new ArrayList<>();
        List<Condition> conditionStatuss = new ArrayList<>();
        List<Condition> conditionSheetTypes = new ArrayList<>();
        List<Condition> conditionCreateDate = new ArrayList<>();

        HashMap<String, List<?>> dicCondition = new HashMap<>();

        if (!holder.SheetId.getText().toString().equals("")) {
            Condition condition = new Condition();
            condition.setAliasTable("M");
            condition.setColumnName("SHEET_ID");
            condition.setDataType("string");
            condition.setValue(holder.SheetId.getText().toString().toUpperCase().trim()); //20200729 archie 轉大寫
            conditionIds.add(condition);
            dicCondition.put("SHEET_ID", conditionIds);
        }

        Condition condSheetStatus = new Condition();
        condSheetStatus.setAliasTable("M");
        condSheetStatus.setColumnName("SHEET_STATUS");
        condSheetStatus.setDataType("string");
        condSheetStatus.setValue("Confirmed");
        conditionStatuss.add(condSheetStatus);
        dicCondition.put("SHEET_STATUS", conditionStatuss);

        /*
        //for (String type: sheetTypes)
        //{
        //    Condition condSheetType = new Condition();
        //    condSheetType.setAliasTable("ST");
        //    condSheetType.setColumnName("SHEET_TYPE_ID");
        //    condSheetType.setDataType("string");
        //    condSheetType.setValue(type);
        //    conditionSheetTypes.add(condSheetType);
        //}
         */

        String strSheetTypeId = mapSheetTypeKey.get(holder.MtlSheetType.getSelectedItem().toString());
        if (strSheetTypeId.equals("null")){
            ShowMessage(R.string.WAPG001012); //請選擇單據類型
            return;
        }

        Condition condSheetType = new Condition();
        condSheetType.setAliasTable("ST");
        condSheetType.setColumnName("SHEET_TYPE_ID");
        condSheetType.setDataType("string");
        condSheetType.setValue(strSheetTypeId);
        conditionSheetTypes.add(condSheetType);
        dicCondition.put("SHEET_TYPE_ID", conditionSheetTypes);

        if (!holder.FromDate.getText().toString().equals("") && !holder.ToDate.getText().toString().equals("")) {
            Condition condDate = new Condition();
            condDate.setAliasTable("M");
            condDate.setColumnName("CREATE_DATE");
            condDate.setDataType("System.DateTime");
            condDate.setValue(holder.FromDate.getText().toString() + " 00:00:00");
            condDate.setValueBetween(holder.ToDate.getText().toString() + " 23:59:59");
            conditionCreateDate.add(condDate);
            dicCondition.put("CREATE_DATE", conditionCreateDate);
        }

        if (dicCondition.size() != 0) {
            MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vKey, vVal);
            String serializedObj = msdl.generateFinalCode(dicCondition);
            ParameterInfo param1 = new ParameterInfo();
            param1.setParameterID(BIWMSFetchInfoParam.Condition);
            param1.setNetParameterValue(serializedObj);
            biObj.params.add(param1);
        }

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIWMSFetchInfoParam.OnlyTableColumn);
        param2.setParameterValue("N");
        biObj.params.add(param2);

        // region 取得該Sheet的Config設定
        BModuleObject bmCfgObj = new BModuleObject();
        bmCfgObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmCfgObj.setModuleID("BIFetchSheetConfigByID");
        bmCfgObj.setRequestID("FetchSheetConfigByID");
        bmCfgObj.params =  new Vector<ParameterInfo>();

        HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
        List<Condition> lstCondition = new ArrayList<Condition>();
        Condition conditionSheetID = null;
        if(!holder.FromDate.getText().toString().equals("") && !holder.ToDate.getText().toString().equals("")){
            conditionSheetID = new Condition();
            conditionSheetID.setAliasTable("M");
            conditionSheetID.setColumnName("CREATE_DATE");
            conditionSheetID.setDataType("System.DateTime");
            conditionSheetID.setValue(holder.FromDate.getText().toString() + " 00:00:00");
            conditionSheetID.setValueBetween(holder.ToDate.getText().toString() + " 23:59:59");
            lstCondition.add(conditionSheetID);
        }
        else if (!holder.SheetId.getText().toString().equals(""))
        {
            conditionSheetID = new Condition();
            conditionSheetID.setAliasTable("M");
            conditionSheetID.setColumnName("SHEET_ID");
            conditionSheetID.setDataType("string");
            conditionSheetID.setValue(holder.SheetId.getText().toString().toUpperCase().trim());
            lstCondition.add(conditionSheetID);
        }
        else {
            ShowMessage(R.string.WAPG001010);//WAPG001010   請輸入起訖時間
            return;
        }
        mapCondition.put(conditionSheetID.getColumnName(),lstCondition);

        // Serialize序列化
        VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVa2 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vkey, vVa2);
        String strCond = msdl.generateFinalCode(mapCondition);

        // Input param
        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BIWMSFetchInfoParam.Condition);
        param3.setNetParameterValue(strCond);

        ParameterInfo param4 = new ParameterInfo();
        param4.setParameterID(BIWMSFetchInfoParam.Filter);
        param4.setParameterValue( "AND SWSC.STORAGE_ACTION_TYPE = 'From'");
        bmCfgObj.params.add(param3);
        bmCfgObj.params.add(param4);
        // endregion

        // region 取得ConfigCond及ConfigSort
        String strSheetTypeKey = SheetTypeTable.Rows.get(holder.MtlSheetType.getSelectedItemPosition()-1).getValue("SHEET_TYPE_KEY").toString();
        BModuleObject biShtCfgSortAndCond = new BModuleObject();
        biShtCfgSortAndCond.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        biShtCfgSortAndCond.setModuleID("BIFetchConfigCondAndSort");
        biShtCfgSortAndCond.setRequestID("FetchConfigCondAndSort");
        biShtCfgSortAndCond.params = new Vector<ParameterInfo>();
        ParameterInfo paramShtTypeKey = new ParameterInfo();
        paramShtTypeKey.setParameterID(BIFetchPickStrategyParam.SheetTypeKey);
        paramShtTypeKey.setParameterValue(strSheetTypeKey);
        biShtCfgSortAndCond.params.add(paramShtTypeKey);
        // endregion

        List<BModuleObject> lstBmObj = new ArrayList<BModuleObject>();
        lstBmObj.add(biObj);
        lstBmObj.add(bmCfgObj);
        lstBmObj.add(biShtCfgSortAndCond);

        CallBIModule(lstBmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    DataTable dtMst = bModuleReturn.getReturnJsonTables().get("BIFetchSheetMst").get("Mst");
                    DataTable dtDet = bModuleReturn.getReturnJsonTables().get("BIFetchSheetMst").get("Det");

                    //20201019 Hans DetTable新增 ACTUAL_QTY_STATUS欄位
                    SheetActStatus = new HashMap<>();
                    DataColumn dc = new DataColumn("ACTUAL_QTY_STATUS");
                    dtDet.addColumn(dc);
                    SheetCfgTable = bModuleReturn.getReturnJsonTables().get("FetchSheetConfigByID").get("Config");

                    if (dtMst == null || dtMst.Rows.size() == 0) {
                        //目前篩選條件無Conifrmed的單據資料!
                        ShowMessage(R.string.WAPG001001);
                    }else{
                        //region 檢查單據類型, 單據Config設定[實際數量的狀態]是否設定
                        List<String> unsetActualQtyStatusSheet = new ArrayList<String>();
                        for (DataRow dr : SheetCfgTable.Rows){
                            String sheetTypeName = dr.getValue("SHEET_TYPE_NAME").toString();
                            if (dr.getValue("ACTUAL_QTY_STATUS").toString() == "" && !unsetActualQtyStatusSheet.contains(sheetTypeName)){
                                unsetActualQtyStatusSheet.add(sheetTypeName);
                            }
                            SheetActStatus.put(dr.getValue("SHEET_ID").toString(),dr.getValue("ACTUAL_QTY_STATUS").toString());
                        }

                        if(unsetActualQtyStatusSheet.size() > 0){
                            ShowMessage(R.string.WAPG001009, TextUtils.join(" , ", unsetActualQtyStatusSheet));
                        }
                        //endregion
                    }

                    if (SheetMstTable == null || SheetMstTable.Rows.size() == 0) {
                        SheetMstTable = dtMst;
                    } else {
                        for (DataRow drNew : dtMst.Rows) {
                            boolean needAdded = true;
                            for (DataRow drOld : SheetMstTable.Rows) {
                                if (drNew.getValue("SHEET_ID").equals(drOld.getValue("SHEET_ID"))) {
                                    needAdded=false;
                                    break;
                                }
                            }
                            if (needAdded) SheetMstTable.Rows.add(drNew);
                        }
                    }

                    //for (DataRow dr : SheetMstTable.Rows) dr.setValue("SELECTED", "true");

                    if (((DataColumnCollection) dtMst.getColumns()).get("SOURCE_SHEET_ID") == null) // !dtMst.getColumns().contains("SOURCE_SHEET_ID") 原方式抓不到是否已有SOURCE_SHEET_ID
                    {
                        DataColumn dcSource = new DataColumn("SOURCE_SHEET_ID");
                        dtMst.addColumn(dcSource);

                        for (DataRow dr : dtMst.Rows)
                        {
                            dr.setValue("SOURCE_SHEET_ID", strSourceShtId);
                        }
                    }

                    if (((DataColumnCollection) dtDet.getColumns()).get("SOURCE_SHEET_ID") == null) //!dtDet.getColumns().contains("SOURCE_SHEET_ID")
                    {
                        DataColumn dcSource = new DataColumn("SOURCE_SHEET_ID");
                        dtDet.addColumn(dcSource);

                        for (DataRow dr : dtDet.Rows)
                        {
                            dr.setValue("SOURCE_SHEET_ID", strSourceShtId);
                        }
                    }

                    if (SheetDetTables == null || SheetDetTables.size() == 0) {
                        for (DataRow drNew : dtMst.Rows) {
                            DataTable dt = new DataTable();
                            for (DataRow drNewDet : dtDet.Rows) {
                                if (drNew.getValue("SHEET_ID").toString().equals(drNewDet.getValue("SHEET_ID").toString())) {
                                    //填入單據ACTUAL_QTY_STATUS
                                    drNewDet.setValue("ACTUAL_QTY_STATUS",SheetActStatus.get(drNewDet.getValue("SHEET_ID").toString()));
                                    dt.Rows.add(drNewDet);
                                }
                            }
                            SheetDetTables.put(drNew.getValue("SHEET_ID").toString(), dt);
                        }
                    } else {
                        List<String> detIds = new ArrayList<>();
                        for (DataRow dr : dtDet.Rows) {
                            if (SheetDetTables.containsKey(dr.getValue("SHEET_ID").toString())) {
                                continue;
                            }
                            //填入單據ACTUAL_QTY_STATUS
                            dr.setValue("ACTUAL_QTY_STATUS",SheetActStatus.get(dr.getValue("SHEET_ID").toString()));
                            detIds.add(dr.getValue("SHEET_ID").toString());
                        }

                        for (String str : detIds) {
                            DataTable dt = new DataTable();
                            for (DataRow dr : dtDet.Rows) {
                                if (dr.getValue("SHEET_ID").toString().equals(str)) {
                                    dt.Rows.add(dr);
                                }
                            }
                            SheetDetTables.put(str, dt);
                        }
                    }
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    SheetMstGridAdapter adapter = new SheetMstGridAdapter(SheetMstTable, inflater);
                    holder.MasterSheetData.setAdapter(adapter);
                    holder.MasterSheetData.setOnItemClickListener(onClickListView);
                    checkedCount = 0;
                    for (DataRow dr : SheetMstTable.Rows)
                    {
                        if(dr.getValue("SELECTED").toString().equals("true"))
                            checkedCount++;
                    }
                    //checkedCount = SheetMstTable.Rows.size();
                    holder.Confirm.setText(String.format("%s(%s)", confirmName, checkedCount + "/" + SheetMstTable.Rows.size()));

                    if (SheetMstTable.Rows.size() > 0)
                        holder.MtlSheetType.setEnabled(false);
                }
            }
        });
        holder.SheetId.setText("");
    }

    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener() {
        @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            int totalCount = holder.MasterSheetData.getCount();
            CheckBox cb = view.findViewById(R.id.cbSheetMstGridSheet);
            cb.setChecked(!cb.isChecked());
            String checkSheet;
            if (cb.isChecked()) {
                checkSheet = "true";
                checkedCount++;
            } else {
                checkSheet = "false";
                checkedCount--;
            }
            SheetMstTable.Rows.get(position).setValue("SELECTED", checkSheet);
            holder.Confirm.setText(String.format("%s(%s)", confirmName, checkedCount + "/" + totalCount));
        }
    };

    public void onClickFromDateClear(View v) {
        holder.FromDate.setText("");
    }

    public void onClickToDateClear(View v) {
        holder.ToDate.setText("");
    }

    private  void CheckPBL(ArrayList<String> ids, String strSheetTypePolicyId)
    {
        // Call BIModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSPickByLight");
        bmObj.setModuleID("BIGetPKLBIn");
        bmObj.setRequestID("BIGetPKLBIn");
        bmObj.params = new Vector<ParameterInfo>();

        VirtualClass vList = VirtualClass.create(VirtualClass.VirtualClassType.String);
        MList mList = new MList(vList);
        String strLstStatus = mList.generateFinalCode(ids);

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSPickByLightParam.LstSheetID);
        param1.setNetParameterValue(strLstStatus);
        bmObj.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIWMSPickByLightParam.SheetTypePolicyID);
        param2.setParameterValue(strSheetTypePolicyId);
        bmObj.params.add(param2);

        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BIWMSPickByLightParam.IsCheck);
        param3.setParameterValue("");
        bmObj.params.add(param3);

        ParameterInfo param4 = new ParameterInfo();
        param4.setParameterID(BIWMSPickByLightParam.LightSignal);
        param4.setParameterValue("ON");
        bmObj.params.add(param4);

        CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn))
                {
                    DataTable dtPbl = bModuleReturn.getReturnJsonTables().get("BIGetPKLBIn").get("SHT_COLOR");

                    ArrayList<String> ids = new ArrayList<String>();
                    String strSheetTypePolicyId = SheetMstTable.Rows.get(0).getValue("SHEET_TYPE_POLICY_ID").toString();
                    HashMap<String, String> sheetTypeMap = new HashMap<>();
                    for (int i = 0; i < SheetMstTable.Rows.size(); i++) {
                        if (SheetMstTable.Rows.get(i).getValue("SELECTED").toString().equals("true")) {
                            ids.add(SheetMstTable.Rows.get(i).getValue("SHEET_ID").toString());
                            sheetTypeMap.put(SheetMstTable.Rows.get(i).getValue("SHEET_ID").toString(), SheetMstTable.Rows.get(i).getValue("SHEET_TYPE_ID").toString());
                        }
                    }

                    Bundle sheetData = new Bundle();
                    DataTable resultTable = new DataTable();
                    HashMap<String, String> actualQtyStatus = new HashMap<>();
                    for (String str : ids)
                    {
                        if (!SheetDetTables.containsKey(str)) continue;

                        for (DataRow dr : SheetDetTables.get(str).Rows)
                        {
                            DataRow drNew = dr;
                            dr.put("SHEET_TYPE_ID", sheetTypeMap.get(str));
                            resultTable.Rows.add(dr);

                            if (!actualQtyStatus.containsKey(dr.getValue("SHEET_ID").toString()))
                            {
                                actualQtyStatus.put(dr.getValue("SHEET_ID").toString(),
                                        dr.getValue("ACTUAL_QTY_STATUS").toString());
                                //dr.put("ACTUAL_QTY_STATUS",SheetActStatus.get(str)).toString());
                            }
                        }
                    }

                    if (dtPbl.Rows.size() > 0)
                    {
                        boolean status = false; //如果是有下達亮燈,為true,反之則為false

                        if (dtPbl.Rows.get(0).getValue("SEQ").toString().equals(""))
                            status = true;

                        for (DataRow dr : dtPbl.Rows)
                        {
                            for (DataRow row : resultTable.Rows)
                            {
                                if (row.getValue("SHEET_ID").toString().equals(dr.getValue("SHEET_ID").toString()))
                                {
                                    if (status)
                                    {
                                        row.setValue("ARGB_LIGHT_COLOR", dr.getValue("ARGB_LIGHT_COLOR").toString());
                                    }
                                    else
                                    {
                                        if (Double.parseDouble(row.getValue("SEQ").toString()) == Double.parseDouble(dr.getValue("SEQ").toString()))
                                        {
                                            row.setValue("ARGB_LIGHT_COLOR", dr.getValue("ARGB_LIGHT_COLOR").toString());
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // region -- 原挑貨規則 --
        /*
        HashMap<String,String> pickStrategy = new HashMap<>();
        for (DataRow dr : SheetTypeTable.Rows){
            String sheetTypeId = dr.getValue("SHEET_TYPE_ID").toString();

            if(sheetTypeId.equals(holder.MtlSheetType.getSelectedItem().toString())){
                pickStrategy.put("PickStrategy", dr.getValue("OUT_PICKING_STRATEGY").toString());
                break;
            }
        }
         */
                    // endregion

                    for (DataRow drCheck : resultTable.Rows) {
                        if (!(drCheck.getValue("PICK_STATUS").toString().equals("Reserved") ||
                            drCheck.getValue("PICK_STATUS").toString().equals("Picking")) ) {

                            Object[] args = new Object[2];
                            args[0] = drCheck.getValue("PICK_STATUS").toString();
                            args[1] = "Reserved, Picking";
                            ShowMessage(R.string.WAPG001016, args); // 揀料狀態為[%s]須為[%s]才能進行揀料作業
                            return;
                        }
                    }

                    sheetData.putSerializable("SheetDet", resultTable);
                    sheetData.putSerializable("actualQtyStatus", actualQtyStatus);
                    //sheetData.putSerializable("pickingStrategy", pickStrategy);
                    sheetData.putStringArrayList("ShtIds", ids);
                    sheetData.putString("ShtTypePolicyId", strSheetTypePolicyId);
                    sheetData.putSerializable("MapSheet", mapSheet);
                    gotoNextActivity(GoodPickDetailActivity.class, sheetData);
                }
            }
        });
    }
}
