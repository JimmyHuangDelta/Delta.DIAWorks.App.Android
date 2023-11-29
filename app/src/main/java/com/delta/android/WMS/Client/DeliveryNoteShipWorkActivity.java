package com.delta.android.WMS.Client;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.Activity.ScanActivity;
import com.delta.android.Core.DataTable.DataColumn;
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
import com.delta.android.WMS.Param.BDeliveryNoteShipParam;
import com.delta.android.WMS.Param.BIFetchProcessSheetParam;
import com.delta.android.WMS.Param.BIReleaseOrderOnboradCheckParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.BStockOutBaseParam;
import com.delta.android.WMS.Param.ParamObj.CheckCountObj;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.delta.android.WMS.Param.ParamObj.PickDetObj;
import com.delta.android.WMS.Param.ParamObj.ReleaseOrderObj;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.lang.reflect.Parameter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Calendar;

public class DeliveryNoteShipWorkActivity extends BaseFlowActivity {

    //region public param
    private ViewHolder holder = null;
    private String strLicensePlate = "";
    private DataRow drReg = null;

    private DataTable dtDnMst = new DataTable();
    private DataTable dtDnDet = new DataTable();
    private DataTable dtDnPickDet = new DataTable();
    private DataTable dtShtCfg = new DataTable();
    private DataTable dtRegister = new DataTable();

    ArrayList<String> lstSheetId = new ArrayList<>();
    ArrayList<ReleaseOrderObj> alReleaseOrder = new ArrayList<ReleaseOrderObj>();
    ArrayList<String> alRegId = new ArrayList<String>();
    ArrayList<String> alLicensePlate = new ArrayList<String>();
    HashMap<String, String> mapDriver = new HashMap<String, String>();
    HashMap<String, String> mapPhone = new HashMap<String, String>();

    static class ViewHolder{
//        EditText etDNId;
        Spinner cmbDNId;
        ImageButton ibSearch;

        TabHost tabHost;

        //Tab1 DNInfo
        TextView tvShipDate;
        TextView tvDeliveryAddress;
        ListView lvPackInfo;
        //Tab2 Ship
        RadioGroup rgPlate;
        RadioButton rbAdd;
        RadioButton rbSelect;
        EditText etLicensePlate;
        Spinner spinner;
        EditText etPhone;
        EditText etDriver;

        EditText etRegisterID;
        ImageButton ibtnLotIdQRScan;
        //出貨Confirm按鍵
        Button btnConfirm;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_delivery_note_ship_work);

        initControl();
        setListensers();
        initData();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, getResources().getText(R.string.QRCODE_SCAN_CANCEL).toString(), Toast.LENGTH_SHORT).show();
            } else {
                holder.etRegisterID.setText(result.getContents().trim());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initControl(){
        if(holder != null) return;

        holder = new ViewHolder();

//        holder.etDNId = findViewById(R.id.etDNId);
        holder.cmbDNId = findViewById(R.id.cmbDNId);
        holder.ibSearch = findViewById(R.id.ibSearch);

        holder.tabHost = findViewById(R.id.tabHost);
        holder.tabHost.setup();
        TabHost.TabSpec spec1 = holder.tabHost.newTabSpec("DNInfo");
        View tab1 = LayoutInflater.from(this).inflate(R.layout.activity_wms_good_inventory_tab_widget, null);
        TextView tvTab1 = tab1.findViewById(R.id.tvTabText);
        tvTab1.setText(R.string.DN_INFO);
        spec1.setIndicator(tab1);
        spec1.setContent(R.id.Tab1);
        holder.tabHost.addTab(spec1);

        TabHost.TabSpec spec2 = holder.tabHost.newTabSpec("Ship");
        View tab2 = LayoutInflater.from(this).inflate(R.layout.activity_wms_good_inventory_tab_widget, null);
        TextView tvTab2 = tab2.findViewById(R.id.tvTabText);
        tvTab2.setText(R.string.SHIP);
        spec2.setIndicator(tab2);
        spec2.setContent(R.id.Tab2);
        holder.tabHost.addTab(spec2);

        holder.tvShipDate = findViewById(R.id.tvShipDate);
        holder.tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);
        holder.lvPackInfo = findViewById(R.id.lvPackingInfo);

        holder.rgPlate = findViewById(R.id.rgPlate);
        holder.rbAdd = findViewById(R.id.rbAdd);
        holder.rbSelect = findViewById(R.id.rbSelect);
        holder.rgPlate.check(R.id.rbAdd);

        holder.etLicensePlate = findViewById(R.id.etLicensePlate);
        holder.etDriver = findViewById(R.id.etDriver);
        holder.etPhone = findViewById(R.id.etPhone);
        holder.spinner = findViewById(R.id.spinner);
        holder.spinner.setEnabled(false);
        holder.spinner.setSelection(0, true);

        holder.ibtnLotIdQRScan = findViewById(R.id.ibtnLotIdQRScan);
        holder.etRegisterID = findViewById(R.id.etLotId);

        holder.btnConfirm = findViewById(R.id.btConfirm);
    }

    private void initData(){

        ArrayList<BModuleObject> lstBObj = new ArrayList<>();
        //取得單據設定檔
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchWmsSheetConfig");
        bmObj.setRequestID("FetchWmsShtCfg");
        bmObj.params = new Vector<ParameterInfo>();

        // Set Condition
        List<Condition> lstCondition = new ArrayList<Condition>();
        Condition condition = new Condition();
        condition.setAliasTable("STP");
        condition.setColumnName("SHEET_TYPE_POLICY_ID");
        condition.setValue("DeliveryNote");
        condition.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
        lstCondition.add(condition);

        HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
        mapCondition.put(condition.getColumnName(),lstCondition);
        VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vkey, vVal);
        String strCond = msdl.generateFinalCode(mapCondition);

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Condition);
        param1.setNetParameterValue(strCond); // 要用set"Net"ParameterValue

        bmObj.params.add(param1);
        lstBObj.add(bmObj);

        //取得倉庫物料資訊
        BModuleObject bmObj2 = new BModuleObject();
        bmObj2.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj2.setModuleID("BIFetchWmsRegister");
        bmObj2.setRequestID("FetchWmsRegister");
        bmObj2.params = new Vector<ParameterInfo>();
        lstBObj.add(bmObj2);

        CallBIModule(lstBObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(!CheckBModuleReturnInfo(bModuleReturn)) return;

                dtShtCfg = bModuleReturn.getReturnJsonTables().get("FetchWmsShtCfg").get("SBRM_WMS_SHEET_CONFIG");

                if(dtShtCfg == null || dtShtCfg.Rows.size() <= 0){
                    //WAPG002016    查無出通單設定檔
                    ShowMessage(R.string.WAPG002016);
                }

                dtRegister = bModuleReturn.getReturnJsonTables().get("FetchWmsRegister").get("SWMS_REGISTER");

                if(dtRegister == null || dtRegister.Rows.size() <= 0){
                    ShowMessage(R.string.WAPG002019); //WAPG002019 無法取得倉庫物料資訊
                }

                // 載入單據代碼至下拉選單
                GetDNId();
            }
        });

    }

    private void setListensers(){
        holder.ibSearch.setOnClickListener(GetDetail);
        holder.rgPlate.setOnCheckedChangeListener(radGrpOnCheckedChange);
        holder.spinner.setOnItemSelectedListener(LicensePlateOnItemSelected);

        holder.etRegisterID.setOnKeyListener(RegIdOnKey);
        holder.ibtnLotIdQRScan.setOnClickListener(LotIdQRScanOnClick);
        holder.btnConfirm.setOnClickListener(lsConfirm);
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

    private void GetDNId() {
        // Call BIModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIFetchProcessSheet");
        bmObj.setModuleID("FetchProcessSheetByStatus");
        bmObj.setRequestID("FetchProcessSheetByStatus");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIFetchProcessSheetParam.ProcessType);
        param1.setParameterValue("SHIP");
        bmObj.params.add(param1);

        List<String> lstStatus = new ArrayList<>();
        lstStatus.add("Confirmed");
        VirtualClass vList = VirtualClass.create(VirtualClass.VirtualClassType.String);
        MList mList = new MList(vList);
        String strLstStatus = mList.generateFinalCode(lstStatus);
        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIFetchProcessSheetParam.LstStatus);
        param2.setNetParameterValue(strLstStatus);
        bmObj.params.add(param2);

        CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    lstSheetId.clear();
                    DataTable dt = bModuleReturn.getReturnJsonTables().get("FetchProcessSheetByStatus").get("DATA");
                    int count = 0;
                    for (DataRow dr : dt.Rows) {
                        lstSheetId.add(count, dr.getValue("SHEET_ID").toString());
                        count++;
                    }
                    Collections.sort(lstSheetId); // List依據字母順序排序

                    // 下拉選單預設選項依語系調整
                    String strSelectSheetId = getResString(getResources().getString(R.string.SELECT_SHEET_ID));
                    lstSheetId.add(strSelectSheetId);

                    SimpleArrayAdapter adapter = new DeliveryNoteShipWorkActivity.SimpleArrayAdapter<>(DeliveryNoteShipWorkActivity.this, android.R.layout.simple_spinner_dropdown_item, lstSheetId);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    holder.cmbDNId.setAdapter(adapter);
                    holder.cmbDNId.setSelection(lstSheetId.size() - 1, true);
                }
            }
        });
    }

    private void RefreshData(){
//        holder.etDNId.setText("");
        holder.tvDeliveryAddress.setText("");
        holder.tvShipDate.setText("");

        holder.etLicensePlate.setText("");
        holder.etDriver.setText("");
        holder.etPhone.setText("");

        holder.etRegisterID.setText("");

        alLicensePlate.clear();
        alRegId.clear();
        mapDriver.clear();
        mapPhone.clear();
        dtDnMst = new DataTable();
        dtDnPickDet = new DataTable();

        holder.lvPackInfo.setAdapter(null);

        ArrayAdapter<String> adapterLP = new ArrayAdapter<>(DeliveryNoteShipWorkActivity.this, android.R.layout.simple_spinner_dropdown_item, alLicensePlate);
        holder.spinner.setAdapter(adapterLP);
    }

    private View.OnClickListener GetDetail = new View.OnClickListener(){
        @Override
        public void onClick(View v)
        {
            GetDetail();
        }
    };

    private RadioGroup.OnCheckedChangeListener radGrpOnCheckedChange = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if(group.getId() == R.id.rgPlate){
                RadioButton rb = findViewById(checkedId);

                switch (checkedId)
                {
                    case R.id.rbAdd:
                        //disable 下拉選單
                        holder.spinner.setEnabled(false);

                        holder.etDriver.setEnabled(true);
                        holder.etPhone.setEnabled(true);
                        holder.etLicensePlate.setEnabled(true);
                        //清除資訊
                        holder.spinner.setSelection(0);
                        holder.etDriver.setText("");
                        holder.etPhone.setText("");
                        break;

                    case R.id.rbSelect:
                        //disable 相關輸入
                        holder.etDriver.setEnabled(false);
                        holder.etPhone.setEnabled(false);
                        holder.etLicensePlate.setEnabled(false);

                        holder.spinner.setEnabled(true);
                        //清除資訊
                        holder.etDriver.setText("");
                        holder.etPhone.setText("");
                        holder.etLicensePlate.setText("");
                        break;
                }
            }
        }
    };

   private Spinner.OnItemSelectedListener LicensePlateOnItemSelected = new Spinner.OnItemSelectedListener(){
       @Override
       public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l){
           //變更車牌資訊
           String strLicensePalate = holder.spinner.getSelectedItem().toString();

           if(mapDriver.containsKey(strLicensePalate)){
               holder.etDriver.setText(mapDriver.get(strLicensePalate).toString());
           }
           if(mapPhone.containsKey(strLicensePalate)){
               holder.etPhone.setText(mapPhone.get(strLicensePalate).toString());
           }
       }

       @Override
       public void onNothingSelected(AdapterView<?> adapterView){
           //do nothing
       }
   };

    private View.OnKeyListener RegIdOnKey = new View.OnKeyListener(){
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event){
            if(keyCode != KeyEvent.KEYCODE_ENTER) return false;
            if(event.getAction() == KeyEvent.ACTION_DOWN){

                strLicensePlate = "";
                if(holder.rbAdd.isChecked())
                    strLicensePlate = holder.etLicensePlate.getText().toString();
                if(holder.rbSelect.isChecked())
                    strLicensePlate = holder.spinner.getSelectedItem().toString();

                if(strLicensePlate.equals("")){
                    ShowMessage(R.string.WAPG002010); //WAPG002010 車牌輸入錯誤
                    return false;
                }

                if(holder.etPhone.getText().toString().equals("")){
                    ShowMessage(R.string.WAPG002008); //WAPG002008  請輸入電話
                    return false;
                }
                if(holder.etDriver.getText().toString().equals("")){
                    ShowMessage(R.string.WAPG002009); //WAPG002009  請輸入司機
                    return false;
                }

                if(dtDnMst == null || dtDnMst.Rows.size() <= 0 || dtDnPickDet == null || dtDnPickDet.Rows.size() <= 0){
                    ShowMessage(R.string.WAPG002004); //WAPG002004 查無揀貨資料
                    return false;
                }

                if(alRegId.contains(holder.etRegisterID.getText().toString())){
                    ShowMessage(R.string.WAPG002011); //WAPG002011 該批號重複
                    return false;
                }

                //確認是否包含該批號
                boolean bCheck = false;
                drReg = null;
                for(DataRow dr : dtDnPickDet.Rows){
                    if(dr.getValue("LOT_ID").toString().equals(holder.etRegisterID.getText().toString())){
                        drReg = dr;
                        bCheck = true;
                        break;
                    }
                    else
                        bCheck = false;
                }
                if(!bCheck)
                {
                    ShowMessage(R.string.WAPG002012); //WAPG002012 查無批號相關資訊
                    return false;
                }

                //檢查該車牌目前要出貨的貨 客戶、送貨地址、出貨日期是否相同
                BModuleObject bmObj = new BModuleObject();
                bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIReleaseOrderOnboradCheck");
                bmObj.setModuleID("BICheckLicense");
                bmObj.setRequestID("BICheckLicense");

                bmObj.params = new Vector<ParameterInfo>();
                ParameterInfo param1 = new ParameterInfo();
                param1.setParameterID(BIReleaseOrderOnboradCheckParam.LicensePlateNum);
                param1.setParameterValue(strLicensePlate);
                bmObj.params.add(param1);

                ParameterInfo param2 = new ParameterInfo();
                param2.setParameterID(BIReleaseOrderOnboradCheckParam.CustomerKey);
                param2.setParameterValue(dtDnMst.Rows.get(0).getValue("CUSTOMER_KEY").toString());
                bmObj.params.add(param2);

                ParameterInfo param3 = new ParameterInfo();
                param3.setParameterID(BIReleaseOrderOnboradCheckParam.ShipDate);
                String strDate = dtDnMst.Rows.get(0).getValue("SHIP_DATE").toString().substring(0, 10);
                param3.setParameterValue(strDate);
                bmObj.params.add(param3);

                ParameterInfo param4 = new ParameterInfo();
                param4.setParameterID(BIReleaseOrderOnboradCheckParam.DeliveryAddress);
                param4.setParameterValue(dtDnMst.Rows.get(0).getValue("DELIVERY_ADDRESS").toString());
                bmObj.params.add(param4);

                CallBIModule(bmObj, new WebAPIClientEvent() {
                    @Override
                    public void onPostBack(BModuleReturn bModuleReturn) {
                        if(CheckBModuleReturnInfo(bModuleReturn)){
                            //記錄相關資訊
                            alRegId.add(holder.etRegisterID.getText().toString());
                            if(!alLicensePlate.contains(holder.etLicensePlate.getText().toString())){
                                if(alLicensePlate.size() == 0){
                                    //新增一筆空值作為預設值
                                    alLicensePlate.add("");
                                }
                                alLicensePlate.add(holder.etLicensePlate.getText().toString());
                            }
                            if(!mapDriver.containsKey(strLicensePlate)){
                                mapDriver.put(strLicensePlate, holder.etDriver.getText().toString());
                            }
                            if(!mapPhone.containsKey(strLicensePlate)){
                                mapPhone.put(strLicensePlate, holder.etPhone.getText().toString());
                            }

                            ArrayAdapter<String> adapterLP = new ArrayAdapter<>(DeliveryNoteShipWorkActivity.this, android.R.layout.simple_spinner_dropdown_item, alLicensePlate);
                            holder.spinner.setAdapter(adapterLP);

                            ReleaseOrderObj ro = new ReleaseOrderObj();
                            ro.setDnId(dtDnMst.Rows.get(0).getValue("DN_ID").toString());
                            ro.setLicensePlateNum(strLicensePlate);
                            ro.setDriverName(holder.etDriver.getText().toString());
                            ro.setDriverPhone(holder.etPhone.getText().toString());
                            ro.setCustomerKey(dtDnMst.Rows.get(0).getValue("CUSTOMER_KEY").toString());
                            ro.setDeliveryAddress(dtDnMst.Rows.get(0).getValue("DELIVERY_ADDRESS").toString());
                            ro.setShipDate(dtDnMst.Rows.get(0).getValue("SHIP_DATE").toString());
                            ro.setSeq(drReg.getValue("SEQ").toString());
                            ro.setItemKey(drReg.getValue("ITEM_KEY").toString());
                            ro.setLotId(holder.etRegisterID.getText().toString());
                            ro.setStorageKey(drReg.getValue("STORAGE_KEY").toString());
                            ro.setQty(drReg.getValue("QTY").toString());
                            ro.setCmt(drReg.getValue("CMT").toString());
                            alReleaseOrder.add(ro);

                            Toast.makeText(DeliveryNoteShipWorkActivity.this, R.string.WAPG002007, Toast.LENGTH_SHORT).show();
                            holder.etRegisterID.setText("");
                        }
                    }
                });

                return true;
            }
            return false;
        }
    };

    private View.OnClickListener LotIdQRScanOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            IntentIntegrator integrator = new IntentIntegrator(DeliveryNoteShipWorkActivity.this);
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

    private AdapterView.OnClickListener lsConfirm = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            //ExceteDeliveryNoteShip();
            ShowDialog();
        }
    };

    private void ShowDialog(){
//        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        // 由getApplicationContext()修改為指定的Activity
        LayoutInflater inflater = LayoutInflater.from(DeliveryNoteShipWorkActivity.this);
        View view = inflater.inflate(R.layout.activity_wms_delivery_note_ship_work_select_bin, null);
        final Spinner cmbBin = view.findViewById(R.id.cmbBinID);
        Button btnConfirm = view.findViewById(R.id.btnBinConfirm);

        final AlertDialog.Builder builder = new AlertDialog.Builder(DeliveryNoteShipWorkActivity.this);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        BModuleObject bimObj = new BModuleObject();
        bimObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bimObj.setModuleID("BIFetchBin");
        bimObj.setRequestID("BIFetchBin");
        bimObj.params = new Vector<ParameterInfo>();

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Filter);
        param1.setParameterValue(String.format(" AND STORAGE_ID = '%s' AND B.BIN_TYPE = 'OS'", dtDnPickDet.Rows.get(0).getValue("STORAGE_ID").toString()));
        bimObj.params.add(param1);

        CallBIModule(bimObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(CheckBModuleReturnInfo(bModuleReturn)){
                    DataTable dtBin = bModuleReturn.getReturnJsonTables().get("BIFetchBin").get("BIN");

                    ArrayList<String> alBin = new ArrayList<String>();
                    for(int i = 0; i < dtBin.Rows.size(); i++)
                    {
                        String binId = dtBin.Rows.get(i).getValue("BIN_ID").toString();
                        if(!alBin.contains(binId))
                            alBin.add(binId);
                    }
                    ArrayAdapter<String> adapterBin = new ArrayAdapter<>(DeliveryNoteShipWorkActivity.this, android.R.layout.simple_spinner_dropdown_item, alBin);
                    cmbBin.setAdapter(adapterBin);
                }
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cmbBin.getSelectedItem().toString().equals("")){
                    ShowMessage(R.string.WAPG002023); //WAPG002023    請選擇儲位
                    return;
                }
                dialog.dismiss();
                ExceteDeliveryNoteShip(cmbBin.getSelectedItem().toString());
            }
        });
    }

//    public void onClickDNClear(View v) { holder.etDNId.setText(""); }

    public void onClickLotIdClear(View v) { holder.etRegisterID.setText(""); }

    private void GetDetail(){

        /*
        if(holder.etDNId.getText().toString().trim().equals("")){
            ShowMessage(R.string.WAPG002001); //WAPG002001 請輸入單據代碼
            return;
        }
         */

        int sheetIdIndex = holder.cmbDNId.getSelectedItemPosition();
        if (sheetIdIndex == (lstSheetId.size() - 1)) {
            ShowMessage(R.string.WAPG002001); //WAPG002001 請選擇單據代碼
            return;
        }

        //取得出通單資訊
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchDeliveryNoteMaintain");
        bmObj.setRequestID("FetchDeliveryNoteMaintain");
        bmObj.params = new Vector<ParameterInfo>();

        //取得揀貨資訊
        BModuleObject bmPickObj = new BModuleObject();
        bmPickObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmPickObj.setModuleID("BIFetchDnPickMstAndDet");
        bmPickObj.setRequestID("FetchDnPickDet");
        bmPickObj.params = new Vector<ParameterInfo>();

        //取得司機相關資訊
        BModuleObject bmDriverObj = new BModuleObject();
        bmDriverObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmDriverObj.setModuleID("BIFetchReleaseOrderMstAndDet");
        bmDriverObj.setRequestID("FetchReleaseOrder");
        bmDriverObj.params = new Vector<ParameterInfo>();

        // Set Condition
        List<Condition> lstCondition = new ArrayList<Condition>();
        Condition condition = new Condition();
        condition.setAliasTable("MST");
        condition.setColumnName("DN_ID");
//        condition.setValue(holder.etDNId.getText().toString().toUpperCase().trim());
        condition.setValue(holder.cmbDNId.getSelectedItem().toString().toUpperCase().trim());
        condition.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
        lstCondition.add(condition);

        HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
        mapCondition.put(condition.getColumnName(),lstCondition);
        VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vkey, vVal);
        String strCond = msdl.generateFinalCode(mapCondition);

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Condition);
        param1.setNetParameterValue(strCond); // 要用set"Net"ParameterValue

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIWMSFetchInfoParam.OnlyTableColumn);
        param2.setParameterValue("Y");

        bmObj.params.add(param1);
        bmPickObj.params.add(param1);
        //bmDriverObj.params.add(param2);
        bmDriverObj.params.add(param1);

        List<BModuleObject> lstBmObj = new ArrayList<BModuleObject>();
        lstBmObj.add(bmObj);
        lstBmObj.add(bmPickObj);
        lstBmObj.add(bmDriverObj);

        CallBIModule(lstBmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    dtDnMst = bModuleReturn.getReturnJsonTables().get("FetchDeliveryNoteMaintain").get("DnMst");
                    dtDnDet = bModuleReturn.getReturnJsonTables().get("FetchDeliveryNoteMaintain").get("DnDet");
                    DataTable dtPickMst = bModuleReturn.getReturnJsonTables().get("FetchDnPickDet").get("DnMst");
                    dtDnPickDet = bModuleReturn.getReturnJsonTables().get("FetchDnPickDet").get("DnPick");
                    //dtReleaseOrderMst = bModuleReturn.getReturnJsonTables().get("FetchReleaseOrder").get("OrderMst");
                    //dtReleaseOrderDet = bModuleReturn.getReturnJsonTables().get("FetchReleaseOrder").get("OrderDet");

                    //Check Data
                    if (dtDnMst == null || dtDnMst.Rows.size() ==0)
                    {
                        ShowMessage(R.string.WAPG002002);//WAPG002002  查無單據資料
                        holder.lvPackInfo.setAdapter(null);
                        return;
                    }

                    if (!dtDnMst.Rows.get(0).getValue("DN_STATUS").toString().equals("Confirmed"))
                    {
                        ShowMessage(R.string.WAPG002003);//WAPG002003  單據未確認
                        holder.lvPackInfo.setAdapter(null);
                        return;
                    }

                    if (dtPickMst == null || dtPickMst.Rows.size() ==0)
                    {
                        ShowMessage(R.string.WAPG002004);//WAPG002004  查無揀貨資料
                        holder.lvPackInfo.setAdapter(null);
                        return;
                    }

                    if (dtDnMst.Rows.get(0).getValue("SHIP_QC_CONFIRM").toString().equals("Y"))
                    {
                        if (!dtPickMst.Rows.get(0).getValue("PICK_STATUS").toString().equals("QCConfirmed"))
                        {
                            ShowMessage(R.string.WAPG002005);//WAPG002005  出通單所綁定的揀貨狀態是不為QCConfirmed
                            holder.lvPackInfo.setAdapter(null);
                            return;
                        }
                    }
                    else//不需經QC作確認
                    {
                        //則判斷出通單綁定的揀貨狀態是否為Picked
                        if (!dtPickMst.Rows.get(0).getValue("PICK_STATUS").toString().equals("Picked"))
                        {
                            ShowMessage(R.string.WAPG002006);//WAPG002006  出通單綁定的揀貨狀態不為Picked
                            holder.lvPackInfo.setAdapter(null);
                            return;
                        }
                    }

                    holder.tvShipDate.setText(dtDnMst.Rows.get(0).getValue("SHIP_DATE").toString());
                    holder.tvDeliveryAddress.setText(dtDnMst.Rows.get(0).getValue("DELIVERY_ADDRESS").toString());

                    List<HashMap<String , String>> list = new ArrayList<>();
                    ArrayList<String> listSeq = new ArrayList<String>();
                    ArrayList<String> listItemId = new ArrayList<String>();
                    ArrayList<String> listItemName = new ArrayList<String>();
                    ArrayList<String> listLotId = new ArrayList<String>();
                    ArrayList<String> listQty = new ArrayList<String>();

                    //出通單揀貨資訊顯示
                    Iterator it =  dtDnPickDet.Rows.iterator();
                    int i = 0;
                    while (it.hasNext())
                    {
                        DataRow row = (DataRow) it.next();
                        listSeq.add( i,row.getValue("SEQ").toString());
                        listItemId.add( i,row.getValue("ITEM_ID").toString());
                        listItemName.add( i,row.getValue("ITEM_NAME").toString());
                        listLotId.add( i,row.getValue("LOT_ID").toString());
                        listQty.add( i,row.getValue("QTY").toString());
                        i++;
                    }
                    for (int j =0; j<listItemId.size();j++)
                    {
                        HashMap<String , String> hashMap = new HashMap<>();
                        hashMap.put("SEQ" , listSeq.get(j));
                        hashMap.put("ITEM_ID" , listItemId.get(j));
                        hashMap.put("ITEM_NAME" , listItemName.get(j));
                        hashMap.put("LOT_ID" , listLotId.get(j));
                        hashMap.put("QTY" , listQty.get(j));
                        //把title , text存入HashMap之中
                        list.add(hashMap);
                    }

                    ListAdapter adapter = new SimpleAdapter(
                            DeliveryNoteShipWorkActivity.this,
                            list,
                            R.layout.activity_wms_delivery_note_ship_detail_listview,
                            new String[]{"SEQ","ITEM_ID","ITEM_NAME","LOT_ID","QTY"},
                            new int[]{R.id.txtSEQ,R.id.txtItemId,R.id.txtItemName,R.id.txtLotId,R.id.txtQty}
                    );
                    holder.lvPackInfo.setAdapter(adapter);
                }
            }
        });
    }

    private void ExceteDeliveryNoteShip(String tempBin){
        if(dtDnPickDet == null || dtDnPickDet.Rows.size() == 0){
            ShowMessage(R.string.WAPG002004); //WAPG002004 查無揀貨資料
            return;
        }
        if(alRegId == null || alRegId.size() == 0){
            ShowMessage(R.string.WAPG002012); //WAPG002012 無批號相關資訊
            return;
        }
        if(mapDriver == null || mapPhone == null || mapDriver.size() == 0 || mapPhone.size() == 0){
            ShowMessage(R.string.WAPG002013); //WAPG002013 查無司機相關資訊
            return;
        }

        //檢查是否過期
        for(DataRow dr : dtDnPickDet.Rows){
            if(!dr.getValue("EXP_DATE").toString().equals("")){
                String strExpDate = dr.getValue("EXP_DATE").toString();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                try{
                    Date date = sdf.parse(strExpDate);
                    Date currDate = Calendar.getInstance().getTime();
                    if(date.compareTo(currDate) < 0){
                        Object[] args = new Object[1];
                        args[0] = dr.getValue("LOT_ID").toString();
                        ShowMessage(R.string.WAPG002015, args); //WAPG002015 批號[%s]已過期
                        return;
                    }
                }catch (ParseException e){
                    Object[] args = new Object[1];
                    args[0] = dr.getValue("LOT_ID").toString();
                    ShowMessage(R.string.WAPG002014, args); //WAPG002014 批號[%s]無法正常轉換時間
                }
            }
        }

        //檢查是否需要確認庫存
        if(dtShtCfg.Rows.get(0).getValue("OUT_CHECK_INVENTORY").toString().equals("Y")){
            DataRow drTemp = null;
            boolean bCheck = false;
            for(DataRow dr : dtDnPickDet.Rows){
                for(DataRow drReg : dtRegister.Rows){
                    if(drReg.getValue("REGISTER_ID").toString().equals(dr.getValue("LOT_ID").toString())){
                        bCheck = true;
                        drTemp = drReg;
                        break;
                       }
                    }
                if(bCheck){
                    double regQty = Double.parseDouble(drTemp.getValue("QTY").toString());
                    double qty = Double.parseDouble(dr.getValue("QTY").toString());
                    if(regQty < qty){
                        ShowMessage(R.string.WAPG002017); //WAPG002017 批號[%s]庫存數量不足
                        return;
                    }
                }
                else{
                    ShowMessage(R.string.WAPG002018); //WAPG002018 批號[%s]不存在註冊資料中
                    return;
                }
            }
        }

        //檢查 ACTUAL_QTY_STATUS  實際數量的狀態
        for(DataRow dr : dtDnDet.Rows){
            double sum = 0;
            for(DataRow drPick : dtDnPickDet.Rows){
                if(drPick.getValue("SEQ").toString().equals(dr.getValue("SEQ").toString())){
                    double qty = Double.parseDouble(drPick.getValue("SEQ").toString());
                    sum += qty;
                }
            }
            double detQty = Double.parseDouble(dr.getValue("SEQ").toString());

            if (sum == 0)
            {
                Object[] objs = new Object[2];
                objs[0] = holder.cmbDNId.getSelectedItem().toString().toUpperCase().trim();
                objs[1] = dr.getValue("SEQ").toString();

                //WAPG002024 單據[%s],項次[%s]沒有揀料
                ShowMessage(R.string.WAPG002024, objs);
                return;
            }

            switch (dtShtCfg.Rows.get(0).getValue("ACTUAL_QTY_STATUS").toString()){
                case "More":
                    if(sum < detQty){
                        Object[] objs = new Object[4];
//                        objs[0] = holder.etDNId.getText().toString();
                        objs[0] = holder.cmbDNId.getSelectedItem().toString().toUpperCase().trim();
                        objs[1] = dr.getValue("SEQ").toString();
                        objs[2] = sum;
                        objs[3] = detQty;

                        //WAPG002020 單據[%s],項次[%s],出貨數量[%s]不可小於單據數量[%s]
                        ShowMessage(R.string.WAPG002020, objs);
                        return;
                    }
                    break;
                case "Less":
                    if(sum > detQty){
                        Object[] objs = new Object[4];
//                        objs[0] = holder.etDNId.getText().toString();
                        objs[0] = holder.cmbDNId.getSelectedItem().toString().toUpperCase().trim();
                        objs[1] = dr.getValue("SEQ").toString();
                        objs[2] = sum;
                        objs[3] = detQty;

                        //WAPG002021 單據[%s],項次[%s],出貨數量[%s]不可大於單據數量[%s]
                        ShowMessage(R.string.WAPG002021, objs);
                        return;
                    }

                    break;
                case "Equal":
                    if(sum != detQty){
                        Object[] objs = new Object[4];
//                        objs[0] = holder.etDNId.getText().toString();
                        objs[0] = holder.cmbDNId.getSelectedItem().toString().toUpperCase().trim();
                        objs[1] = dr.getValue("SEQ").toString();
                        objs[2] = sum;
                        objs[3] = detQty;

                        //WAPG002022 單據[%s],項次[%s],出貨數量[%s]必須等於單據數量[%s]
                        ShowMessage(R.string.WAPG002022, objs);
                        return;
                    }
                    break;
            }
        }

        List<PickDetObj> lstPickDet = new ArrayList<>();
        List<CheckCountObj>  lstChkCountObj  = new ArrayList<>(); // 20220805 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中
        for(DataRow dr : dtDnPickDet.Rows)
        {
            PickDetObj detObj = new PickDetObj();
            detObj.setSheetId(dr.getValue("DN_ID").toString());
            detObj.setSeq(Double.parseDouble(dr.getValue("SEQ").toString()));
            detObj.setItemId(dr.getValue("ITEM_ID").toString());
            detObj.setLotId(dr.getValue("LOT_ID").toString());
            detObj.setQty(Double.parseDouble(dr.getValue("QTY").toString()));
            detObj.setStorageId(dr.getValue("STORAGE_ID").toString());
            detObj.setBinId(dr.getValue("BIN_ID").toString());
            detObj.setUom(dr.getValue("UOM").toString());
            lstPickDet.add(detObj);

            // region 儲存盤點狀態檢查物件
            CheckCountObj chkCountObjFromBin = new CheckCountObj(); // FROM_BIN
            chkCountObjFromBin.setStorageId(dr.getValue("STORAGE_ID").toString());
            chkCountObjFromBin.setItemId(dr.getValue("ITEM_ID").toString());
            chkCountObjFromBin.setBinId(dr.getValue("BIN_ID").toString());
            lstChkCountObj.add(chkCountObjFromBin);
            CheckCountObj chkCountObjToBin = new CheckCountObj(); // TO_BIN
            chkCountObjToBin.setStorageId(dr.getValue("STORAGE_ID").toString());
            chkCountObjToBin.setItemId(dr.getValue("ITEM_ID").toString());
            chkCountObjToBin.setBinId(tempBin);
            lstChkCountObj.add(chkCountObjToBin);
            // endregion
        }

        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.PickDetObj", "bmWMS.Library.Param");
        MList mListEnum = new MList(vListEnum);
        String strLsRelatData = mListEnum.generateFinalCode(lstPickDet);

        VirtualClass vListEnum2 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.INV.Parameter.ParameterObj.ReleaseOrderObj", "bmWMS.INV.Param");
        MList mListEnum2 = new MList(vListEnum2);
        String strLsReleaseOrder = mListEnum2.generateFinalCode(alReleaseOrder);

        VirtualClass vListEnum3 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.CheckCountObj", "bmWMS.Library.Param");
        MList mListEnum3 = new MList(vListEnum3);
        String strCheckCountObj = mListEnum3.generateFinalCode(lstChkCountObj);

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BDeliveryNoteShip");
        bmObj.setModuleID("BDeliveryNoteShip");
        bmObj.setRequestID("BDeliveryNoteShip");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BStockOutBaseParam.CheckStockOutByObjs); //20220829 archie 將可共用的Param改為出庫Base的Param
        param1.setNetParameterValue(strLsRelatData);
        bmObj.params.add(param1);
        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BDeliveryNoteShipParam.ReleaseOrderObj);
        param2.setNetParameterValue(strLsReleaseOrder);
        bmObj.params.add(param2);
        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BDeliveryNoteShipParam.TempBin);
        param3.setParameterValue(tempBin);
        bmObj.params.add(param3);
        ParameterInfo param4 = new ParameterInfo();
        param4.setParameterID(BDeliveryNoteShipParam.Action);
        param4.setParameterValue("CONFIRM");
        bmObj.params.add(param4);

        ParameterInfo paramExecuteChkStock = new ParameterInfo();
        paramExecuteChkStock.setParameterID(BDeliveryNoteShipParam.ExecuteCheckStock); // 20220805 Add by Ikea 是否執行盤點檢查
        paramExecuteChkStock.setNetParameterValue2("true");
        bmObj.params.add(paramExecuteChkStock);
        ParameterInfo paramChkCountObj = new ParameterInfo(); // 20220805 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中
        paramChkCountObj.setParameterID(BDeliveryNoteShipParam.CheckCountObj);
        paramChkCountObj.setNetParameterValue(strCheckCountObj);
        bmObj.params.add(paramChkCountObj);


        CallBModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(!CheckBModuleReturnInfo(bModuleReturn)) return;

                ShowMessage(R.string.WAPG002007); //WAPG002007 作業成功
                RefreshData();
            }
        });
    }
}

//Error Code
//WAPG002001    請選擇單據代碼
//WAPG002002    查無單據資料
//WAPG002003    單據未確認
//WAPG002004    查無揀貨資料
//WAPG002005    出通單所綁定的揀貨狀態是不為QCConfirmed
//WAPG002006    出通單綁定的揀貨狀態不為Picked
//WAPG002007    作業成功
//WAPG002008    請輸入電話
//WAPG002009    請輸入司機
//WAPG002010    車牌輸入錯誤
//WAPG002011    該批號重複
//WAPG002012    查無批號相關資訊
//WAPG002013    查無司機相關資訊
//WAPG002014    批號[%s]無法正常轉換時間
//WAPG002015    批號[%s]已過期
//WAPG002016    查無出通單設定檔
//WAPG002017    批號[%s]庫存數量不足
//WAPG002018    批號[%s]不存在註冊資料中
//WAPG002019    無法取得倉庫物料資訊
//WAPG002020    單據[%s],項次[%s],出貨數量[%s]不可小於單據數量[%s]
//WAPG002021    單據[%s],項次[%s],出貨數量[%s]不可大於單據數量[%s]
//WAPG002022    單據[%s],項次[%s],出貨數量[%s]必須等於單據數量[%s]
//WAPG002023    請選擇儲位
