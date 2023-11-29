package com.delta.android.WMS.Client;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import com.delta.android.Core.Activity.BaseFlowActivity;
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
import com.delta.android.WMS.Client.GridAdapter.GoodPickNonSheetGridAdapter;
import com.delta.android.WMS.Param.BIFetchPickStrategyParam;
import com.delta.android.WMS.Param.BIFetchProcessSheetParam;
import com.delta.android.WMS.Param.BIPDANoSheetCarrierPortalParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.ParamObj.Condition;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GoodPickNonSheetCarrierActivity extends BaseFlowActivity {

    //Function WAPG015

    private TabHost tabHost;
//    private EditText etSheetId;
    private Spinner cmbSheetId;
    private ImageButton ibtnSheetId;
    //private EditText etLotId;
    private Spinner cmbSeq;
    private EditText etItemId;
    private EditText etSelectLotId;
    private EditText etPickQty;
    private ListView lvRegister;
    //private ImageButton ibtnLotSearch;
    //private ImageButton ibtnItemSearch;
    private ImageButton ibtnQtySearch;
    private Button btnConfirm;

    private TextView tvCarrier;
    private TextView tvFromBin;
    private TextView tvToBin;
    private TextView tvRegisterId;
    private TextView tvBlockID;
    private TextView tvFromBin2;
    private TextView tvToBin2;
    private TextView tvCarrier3;
    private TextView tvFromBin3;
    private TextView tvToBin3;

    private int index = 0;
    private String strStorageId;
    private String strCarrierId;
    private String strBlock;
    private String strFromPort;
    private String strToPort;
    private String strObject;
    private String strCurrentTask;
    private String strSelectBinId;
    private String _strSraechId = ""; //最終處理的單據
    private int iStep;

    public DataTable dtRegister;
    public DataTable dtXfr;
    public DataTable dtMaster;
    public DataTable dtDetail;

    private DataTable dtConfigCond = null;
    private DataTable dtConfigSort = null;
    private String strConfigCond = null;
    private String strConfigSort = null;

    ArrayList<String> lstSeq = null;
    HashMap<String, String> mapSeq = new HashMap<String, String>();
    ArrayList<String> lstSheetId = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good_pick_non_sheet_carrier);

        initControl();
        setListensers();

        // 載入單據代碼至下拉選單
        GetSheetId();
    }

    private void initControl(){
        tabHost = findViewById(R.id.tabHost);
//        etSheetId = findViewById(R.id.etSheetId);
        cmbSheetId = findViewById(R.id.cmbSheetId);
        ibtnSheetId = findViewById(R.id.ibtnSheetIdSearch);
        //etLotId = findViewById(R.id.etLotId);
        cmbSeq = findViewById(R.id.cmbSeq);
        etItemId = findViewById(R.id.etItemId);
        etSelectLotId = findViewById(R.id.etSelectLotId);
        etPickQty = findViewById(R.id.etPickQty);
        lvRegister = findViewById(R.id.lvRegisters);
        btnConfirm = findViewById(R.id.btnConfirm);
        //ibtnItemSearch = findViewById(R.id.ibtnItemSearch);
        //ibtnLotSearch = findViewById(R.id.ibtnLotSearch);
        ibtnQtySearch = findViewById(R.id.ibtnQtySearch);

        //CarrierOut
        tvCarrier = findViewById(R.id.tvCarrierID);
        tvFromBin = findViewById(R.id.tvFromBinID);
        tvToBin = findViewById(R.id.tvToBinID);

        //CarrierRegisterUnBind
        tvRegisterId = findViewById(R.id.tvRegisterID);
        tvBlockID = findViewById(R.id.tvBlockID);
        tvFromBin2 = findViewById(R.id.tvFromBinID2);
        tvToBin2 = findViewById(R.id.tvToBinID2);

        //CarrierIn
        tvCarrier3 = findViewById(R.id.tvCarrierID3);
        tvFromBin3 = findViewById(R.id.tvFromBinID3);
        tvToBin3 = findViewById(R.id.tvToBinID3);

        tabHost.setup();

        TabHost.TabSpec spec1 = tabHost.newTabSpec("CarrierStockOut");
        View tab1 = LayoutInflater.from(this).inflate(R.layout.activity_wms_good_inventory_tab_widget, null);
        TextView tvTab1 = tab1.findViewById(R.id.tvTabText);
        tvTab1.setText(R.string.CARRIER_STOCK_OUT);
        spec1.setIndicator(tab1);
        spec1.setContent(R.id.tabCarrierOut);
        tabHost.addTab(spec1);

        TabHost.TabSpec spec2 = tabHost.newTabSpec("CarrierRegisterUnBind");
        View tab2 = LayoutInflater.from(this).inflate(R.layout.activity_wms_good_inventory_tab_widget, null);
        TextView tvTab2 = tab2.findViewById(R.id.tvTabText);
        tvTab2.setText(R.string.CARRIER_REGISTER_UNBIND);
        spec2.setIndicator(tab2);
        spec2.setContent(R.id.tabCarrierRegisterBind);
        tabHost.addTab(spec2);

        TabHost.TabSpec spec3 = tabHost.newTabSpec("CarrierStockIn");
        View tab3 = LayoutInflater.from(this).inflate(R.layout.activity_wms_good_inventory_tab_widget, null);
        TextView tvTab3 = tab3.findViewById(R.id.tvTabText);
        tvTab3.setText(R.string.CARRIER_STOCK_IN);
        spec3.setIndicator(tab3);
        spec3.setContent(R.id.tabCarrierIn);
        tabHost.addTab(spec3);

        for(int i = 0; i < 3; i++)
        {
            tabHost.getTabWidget().getChildAt(i).setClickable(false);
        }
    }

    private void setListensers(){
        //ibtnLotSearch.setOnClickListener(ibtnLotSearchClick);
        //ibtnItemSearch.setOnClickListener(ibtnItemSearchClick);
        ibtnSheetId.setOnClickListener(lsSheetIdFetch);
        cmbSeq.setOnItemSelectedListener(lstSeqSelected);
        ibtnQtySearch.setOnClickListener(lsRegisterFetch);
        lvRegister.setOnItemClickListener(lvRegisterClick);
        btnConfirm.setOnClickListener(lsConfirm);
        tabHost.setOnTabChangedListener(lsTabHost_OnTabChange);
    }

    private void GetListView(){
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        GoodPickNonSheetGridAdapter adapter = new GoodPickNonSheetGridAdapter(dtRegister, inflater);
        lvRegister.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void ClearData(){
        //etLotId.setText("");
//        etSheetId.setText("");
        cmbSheetId.setSelection(lstSheetId.size()-1); // spinner設定回預設選項
        etItemId.setText("");
        etPickQty.setText("");
        etSelectLotId.setText("");

        dtRegister = new DataTable();
        dtXfr = null;
        dtMaster = new DataTable();
        dtDetail = new DataTable();

        GetListView();

        tvRegisterId.setText("");
        tvBlockID.setText("");
        tvCarrier.setText("");
        tvFromBin.setText("");
        tvToBin.setText("");
        tvFromBin2.setText("");
        tvToBin2.setText("");
        tvCarrier3.setText("");
        tvFromBin3.setText("");
        tvToBin3.setText("");

        lstSeq = new ArrayList<>();
        mapSeq = new HashMap<String, String>();

        ArrayAdapter<String> adapterSeq = new ArrayAdapter<>(GoodPickNonSheetCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, lstSeq);
        cmbSeq.setAdapter(adapterSeq);

        strConfigCond = null;
        strConfigSort = null;
    }

    private void GetSheetId() {
        // Call BIModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIFetchProcessSheet");
        bmObj.setModuleID("FetchProcessSheetByStatus");
        bmObj.setRequestID("FetchProcessSheetByStatus");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIFetchProcessSheetParam.ProcessType);
        param1.setParameterValue("PK");
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
                    int pos = 0;
                    for (DataRow dr : dt.Rows) {
                        lstSheetId.add(pos, dr.getValue("SHEET_ID").toString());
                        pos++;
                    }
                    Collections.sort(lstSheetId); // List依據字母順序排序

                    // 下拉選單預設選項依語系調整
                    String strSelectSheetId = getResString(getResources().getString(R.string.SELECT_SHEET_ID));
                    lstSheetId.add(strSelectSheetId);

                    SimpleArrayAdapter adapter = new GoodPickNonSheetCarrierActivity.SimpleArrayAdapter<>(GoodPickNonSheetCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, lstSheetId);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    cmbSheetId.setAdapter(adapter);
                    cmbSheetId.setSelection(lstSheetId.size() - 1, true);
                }
            }
        });
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

    private void FetchLotInfo(){

        //找出Register
        BModuleObject bmObjLot = new BModuleObject();
        bmObjLot.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDANoSheetCarrierPortal");
        bmObjLot.setModuleID("BIGetPickRegister");
        bmObjLot.setRequestID("BIGetPickRegister");

        bmObjLot.params = new Vector<ParameterInfo>();
        ParameterInfo paramReg = new ParameterInfo();
        paramReg.setParameterID(BIPDANoSheetCarrierPortalParam.RegisterId);
        paramReg.setParameterValue("");
        bmObjLot.params.add(paramReg);

        ParameterInfo paramItem = new ParameterInfo();
        paramItem.setParameterID(BIPDANoSheetCarrierPortalParam.ItemId);
        paramItem.setParameterValue(etItemId.getText().toString());
        bmObjLot.params.add(paramItem);

        ParameterInfo paramQty = new ParameterInfo();
        paramQty.setParameterID(BIPDANoSheetCarrierPortalParam.RegQty);
        paramQty.setParameterValue(etPickQty.getText().toString());
        bmObjLot.params.add(paramQty);

        ParameterInfo paramSheetId = new ParameterInfo();
        paramSheetId.setParameterID(BIPDANoSheetCarrierPortalParam.SheetId);
        paramSheetId.setParameterValue(_strSraechId);
        bmObjLot.params.add(paramSheetId);

        if (strConfigCond != null && strConfigCond.length() > 0) {
            ParameterInfo paramCond = new ParameterInfo();
            paramCond.setParameterID(BIPDANoSheetCarrierPortalParam.ConfigCond);
            paramCond.setParameterValue(strConfigCond);
            bmObjLot.params.add(paramCond);
        }

        if (strConfigSort != null && strConfigSort.length() > 0) {
            ParameterInfo paramSort = new ParameterInfo();
            paramSort.setParameterID(BIPDANoSheetCarrierPortalParam.ConfigSort);
            paramSort.setParameterValue(strConfigSort);
            bmObjLot.params.add(paramSort);
        }

        CallBIModule(bmObjLot, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(CheckBModuleReturnInfo(bModuleReturn)){
                    dtRegister = bModuleReturn.getReturnJsonTables().get("BIGetPickRegister").get("Register");
                    GetListView();
                }
            }
        });
    }

    private Spinner.OnItemSelectedListener lstSeqSelected = new Spinner.OnItemSelectedListener(){
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l){
            MapSeqItem();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView){}
    };

    private void MapSeqItem(){

        DataRow selectedRow = null;

        String seq = mapSeq.get(cmbSeq.getSelectedItem().toString());
        for(DataRow dr: dtDetail.Rows) {
            if(dr.getValue("SEQ").toString().equals(seq)){
                etItemId.setText(dr.getValue("ITEM_ID").toString());
                selectedRow = dr;
                break;
            }
        }

        strConfigCond = generateExtendConfigCond(selectedRow, dtConfigCond);
        strConfigSort = generateExtendConfigSort(dtConfigSort);
    }

    private void CarrierSearch(){
        if(etSelectLotId.getText().toString().equals("")){
            ShowMessage(R.string.WAPG015001); //WAPG015001    請輸入批號
        }

        //BIPDANoSheetCarrierPortal
        BModuleObject bmObjDispatch = new BModuleObject();
        bmObjDispatch.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDANoSheetCarrierPortal");
        bmObjDispatch.setModuleID("BIGetRegisterXfr");
        bmObjDispatch.setRequestID("BIGetRegisterXfr");

        bmObjDispatch.params = new Vector<ParameterInfo>();
        ParameterInfo paramDispatch = new ParameterInfo();
        paramDispatch.setParameterID(BIPDANoSheetCarrierPortalParam.RegisterId);
        paramDispatch.setParameterValue(etSelectLotId.getText().toString());
        bmObjDispatch.params.add(paramDispatch);

        ParameterInfo paramStock = new ParameterInfo();
        paramStock.setParameterID(BIPDANoSheetCarrierPortalParam.stock);
        paramStock.setParameterValue("OUT");
        bmObjDispatch.params.add(paramStock);

        ParameterInfo parambin = new ParameterInfo();
        parambin.setParameterID(BIPDANoSheetCarrierPortalParam.FetchBinId);
        parambin.setParameterValue(strSelectBinId);
        bmObjDispatch.params.add(parambin);

        CallBIModule(bmObjDispatch, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(CheckBModuleReturnInfo(bModuleReturn)){
                    dtXfr = bModuleReturn.getReturnJsonTables().get("BIGetRegisterXfr").get("PDA");

                    strStorageId = dtXfr.Rows.get(0).getValue("STORAGE_ID").toString();
                    strCarrierId = dtXfr.Rows.get(0).getValue("BEST_CARRIER_ID").toString();
                    strBlock = dtXfr.Rows.get(0).getValue("BEST_BLOCK").toString();
                    strCurrentTask = dtXfr.Rows.get(0).getValue("CURRENT_TASK").toString();
                    String strCaseComp = dtXfr.Rows.get(0).getValue("XFR_CASE_COMP").toString();

                    //顯示Tab資訊
                    tvCarrier.setText(dtXfr.Rows.get(0).getValue("BEST_CARRIER_ID").toString());
                    tvFromBin.setText(dtXfr.Rows.get(0).getValue("BEST_BIN").toString());
                    //tvToBin.setText(dtXfr.Rows.get(0).getValue("IS_BIN_ID").toString());
                    tvToBin.setText(dtXfr.Rows.get(0).getValue("OS_BIN_ID").toString());

                    tvRegisterId.setText(etSelectLotId.getText().toString());
                    tvBlockID.setText(dtXfr.Rows.get(0).getValue("BEST_BLOCK").toString());
                    //tvFromBin2.setText(dtXfr.Rows.get(0).getValue("IS_BIN_ID").toString());
                    //tvToBin2.setText(dtXfr.Rows.get(0).getValue("IS_BIN_ID").toString());
                    tvFromBin2.setText(dtXfr.Rows.get(0).getValue("OS_BIN_ID").toString());
                    tvToBin2.setText(dtXfr.Rows.get(0).getValue("OS_BIN_ID").toString());

                    tvCarrier3.setText(dtXfr.Rows.get(0).getValue("BEST_CARRIER_ID").toString());
                    //tvFromBin3.setText(dtXfr.Rows.get(0).getValue("IS_BIN_ID").toString());
                    tvFromBin3.setText(dtXfr.Rows.get(0).getValue("OS_BIN_ID").toString());
                    tvToBin3.setText(dtXfr.Rows.get(0).getValue("FIX_LOCATION").toString());

                    //跳至下一個流程
                    iStep = 0;
                    switch (strCurrentTask){
                        case "CarrierStockOut":
                            iStep = 1;
                            strToPort = tvToBin.getText().toString();
                            strFromPort = tvToBin.getText().toString();
                            strObject = etSelectLotId.getText().toString();
                            break;
                        case "CarrierRegisterUnBind":
                            iStep = 2;
                            strFromPort = tvFromBin2.getText().toString();
                            strToPort = tvToBin2.getText().toString();
                            strObject = tvRegisterId.getText().toString();
                            break;
                        case "CarrierStockIn":
                            if(strCaseComp.equals("Y"))
                                strCurrentTask = "CarrierStockOut";

                            iStep = 0;
                            strFromPort = tvFromBin.getText().toString();
                            strToPort = tvToBin.getText().toString();
                            strObject = tvCarrier.getText().toString();

                            break;
                        default:
                            iStep = 0;
                            strCurrentTask = "CarrierStockOut";
                            strFromPort = tvFromBin.getText().toString();
                            strToPort = tvToBin.getText().toString();
                            strObject = tvCarrier.getText().toString();
                            break;
                    }

                    tabHost.setCurrentTab(iStep);
                }
            }
        });
    }

    private void PickConfirm(){

        if(dtXfr == null || dtXfr.Rows.size() <= 0) return;

        if(etSelectLotId.getText().toString().equals("") || etPickQty.getText().toString().equals("")){
            ShowMessage(R.string.WAPG015004); //WAPG015004    請選擇物料並輸入檢貨數量
            return;
        }

        //BI
        BModuleObject bimObj = new BModuleObject();
        bimObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDANoSheetCarrierPortal");
        bimObj.setModuleID("BIRegisterXfrConfirm");
        bimObj.setRequestID("BIRegisterXfrConfirm");
        bimObj.params = new Vector<ParameterInfo>();

        //Input param
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIPDANoSheetCarrierPortalParam.RegisterId);
        //param1.setParameterValue(etLotId.getText().toString());
        param1.setParameterValue(etSelectLotId.getText().toString());
        bimObj.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIPDANoSheetCarrierPortalParam.XfrCase);
        param2.setParameterValue("Carrier");
        bimObj.params.add(param2);

        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BIPDANoSheetCarrierPortalParam.XfrTask);
        param3.setParameterValue(strCurrentTask);
        bimObj.params.add(param3);

        ParameterInfo param4 = new ParameterInfo();
        param4.setParameterID(BIPDANoSheetCarrierPortalParam.FromPort);
        param4.setParameterValue(strFromPort);
        bimObj.params.add(param4);

        ParameterInfo param5 = new ParameterInfo();
        param5.setParameterID(BIPDANoSheetCarrierPortalParam.ToPort);
        param5.setParameterValue(strToPort);
        bimObj.params.add(param5);

        ParameterInfo param6 = new ParameterInfo();
        param6.setParameterID(BIPDANoSheetCarrierPortalParam.ObjectId);
        param6.setParameterValue(strObject);
        bimObj.params.add(param6);

        ParameterInfo param7 = new ParameterInfo();
        param7.setParameterID(BIPDANoSheetCarrierPortalParam.StorageId);
        param7.setParameterValue(strStorageId);
        bimObj.params.add(param7);

        ParameterInfo param8 = new ParameterInfo();
        param8.setParameterID(BIPDANoSheetCarrierPortalParam.Carrier);
        param8.setParameterValue(strCarrierId);
        bimObj.params.add(param8);

        ParameterInfo param9 = new ParameterInfo();
        param9.setParameterID(BIPDANoSheetCarrierPortalParam.Block);
        param9.setParameterValue(strBlock);
        bimObj.params.add(param9);

        ParameterInfo param10 = new ParameterInfo();
        param10.setParameterID(BIPDANoSheetCarrierPortalParam.BestBin);
        param10.setParameterValue(dtXfr.Rows.get(0).getValue("BEST_BIN"));
        bimObj.params.add(param10);

        ParameterInfo param11 = new ParameterInfo();
        param11.setParameterID(BIPDANoSheetCarrierPortalParam.stock);
        param11.setParameterValue("O");
        bimObj.params.add(param11);

        ParameterInfo param12 = new ParameterInfo();
        param12.setParameterID(BIPDANoSheetCarrierPortalParam.RegQty);
        param12.setParameterValue(etPickQty.getText().toString());
        bimObj.params.add(param12);

        ParameterInfo param13 = new ParameterInfo();
        param13.setParameterID(BIPDANoSheetCarrierPortalParam.SheetId);
//        param13.setParameterValue(etSheetId.getText().toString());
        param13.setParameterValue(_strSraechId);
        bimObj.params.add(param13);

        ParameterInfo param14 = new ParameterInfo();
        param14.setParameterID(BIPDANoSheetCarrierPortalParam.Seq);
        param14.setParameterValue(cmbSeq.getSelectedItem().toString());
        bimObj.params.add(param14);

        ParameterInfo param15 = new ParameterInfo();  // 20220812 Ikea 傳入 ItemId
        param15.setParameterID(BIPDANoSheetCarrierPortalParam.ItemId);
        param15.setParameterValue(etItemId.getText().toString());
        bimObj.params.add(param15);

        ParameterInfo param16 = new ParameterInfo(); // 20220812 Ikea 傳入 BinId
        param16.setParameterID(BIPDANoSheetCarrierPortalParam.OsBinId);
        param16.setParameterValue(dtXfr.Rows.get(0).getValue("OS_BIN_ID").toString());
        bimObj.params.add(param16);

        CallBIModule(bimObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(CheckBModuleReturnInfo(bModuleReturn)){
                    Object[] args = new Object[1];
                    args[0] = strCurrentTask;
                    ShowMessage(R.string.WAPG015005, args); //WAPG015005 上架任務[%s]完成

                    if(iStep < 2)
                        iStep++;
                    else{
                        iStep = 0;
                        ClearData();
                    }

                    tabHost.setCurrentTab(iStep);
                    strCurrentTask = tabHost.getCurrentTabTag();
                }
            }
        });
    }

    private void GetSht(final String strSheetId) {

        BModuleObject bmObjSheet = new BModuleObject();
        bmObjSheet.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObjSheet.setModuleID("BIFetchSheetMst");
        bmObjSheet.setRequestID("BIFetchSheetMst");

        bmObjSheet.params = new Vector<ParameterInfo>();

        HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
        List<Condition> lstCondition1 = new ArrayList<Condition>();

        //SHEET_ID
        Condition conditionSheetId = new Condition();
        conditionSheetId.setAliasTable("M");
        conditionSheetId.setColumnName("SHEET_ID");
        conditionSheetId.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
        conditionSheetId.setValue(strSheetId);
        lstCondition1.add(conditionSheetId);
        mapCondition.put(conditionSheetId.getColumnName(), lstCondition1);

        //Serialize序列化
        VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vkey, vVal);
        String strCond = msdl.generateFinalCode(mapCondition);

        //Input param
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Condition);
        param1.setNetParameterValue(strCond);
        bmObjSheet.params.add(param1);

        CallBIModule(bmObjSheet, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn))
                {
                    DataTable dtMst = bModuleReturn.getReturnJsonTables().get("BIFetchSheetMst").get("Mst");

                    if (dtMst.Rows.get(0).getValue("SHEET_TYPE_POLICY_ID").toString().equals("Issue") ||
                            dtMst.Rows.get(0).getValue("SHEET_TYPE_POLICY_ID").toString().equals("Transfer"))
                    {
                        GetPickingID(strSheetId);
                    }
                    else
                    {
                        FetchSheetInfo("");
                    }
                }
            }
        });
    }

    private void GetPickingID(final String strSheetId) {
        String strPickingID = "";

        //region Call BIModule
        // BIModule
        List<BModuleObject> lstBmObj = new ArrayList<BModuleObject>();
        BModuleObject bmShtObj = new BModuleObject();
        bmShtObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmShtObj.setModuleID("BIFetchPickShtID");
        bmShtObj.setRequestID("FetchPickShtID");

        bmShtObj.params = new Vector<ParameterInfo>();

        // Set Condition
        List<Condition> lstCondition1 = new ArrayList<Condition>();
        Condition condition1 = new Condition();
        condition1.setAliasTable("M");
        condition1.setColumnName("SHEET_ID");
        condition1.setValue(strSheetId);
        // 用VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName() = "System.String"
        condition1.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
        lstCondition1.add(condition1);

        HashMap<String, List<?>> mapCondition1 = new HashMap<String, List<?>>();
        mapCondition1.put(condition1.getColumnName(),lstCondition1);
        VirtualClass vkey1 = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal1 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
        MesSerializableDictionaryList msdl1 = new MesSerializableDictionaryList(vkey1, vVal1);
        String strCond1 = msdl1.generateFinalCode(mapCondition1);

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Condition);
        param1.setNetParameterValue(strCond1); // 要用set"Net"ParameterValue
        bmShtObj.params.add(param1);

        CallBIModule(bmShtObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {

                    DataTable dt = bModuleReturn.getReturnJsonTables().get("FetchPickShtID").get("PICKING_ID");

                    if (dt.Rows.size() <= 0)
                    {
                        //WAPG015009    單據代碼[{0}]查無轉揀料單資料
                        ShowMessage(R.string.WAPG015009, strSheetId);
                        return;
                    }

                    String pickingID = dt.Rows.get(0).getValue("SHEET_ID").toString();

                    FetchSheetInfo(pickingID);
                }
            }
        });
        // endregion
    }

    /*
    private View.OnClickListener ibtnLotSearchClick = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            if(etLotId.getText().toString().equals("")){
                ShowMessage(R.string.WAPG015001); //WAPG015001 請輸入批號
                return;
            }
            if(etPickQty.getText().toString().equals("")){
                ShowMessage(R.string.WAPG015006); //WAPG015006 請輸入數量
                return;
            }
            FetchLotInfo();
        }
    };
    */

    /*
    private View.OnClickListener ibtnItemSearchClick = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            if(etItemId.getText().toString().equals("")){
                ShowMessage(R.string.WAPG015002); //WAPG015002 請輸入料號
                return;
            }
            if(etPickQty.getText().toString().equals("")){
                ShowMessage(R.string.WAPG015006); //WAPG015006 請輸入數量
                return;
            }
            FetchLotInfo();
        }
    };
    */

    private View.OnClickListener lsSheetIdFetch = new View.OnClickListener(){
        @Override
        public void onClick(View veiw)
        {

//            if(etSheetId.getText().toString().equals("")){
//                //WAPG015007    請輸入單據代碼
//                ShowMessage(R.string.WAPG015007);
//                return;
//            }

            int sheetIdIndex = cmbSheetId.getSelectedItemPosition();
            if (sheetIdIndex == (lstSheetId.size() - 1)) {
                ShowMessage(R.string.WAPG015007); //WAPG015007    請選擇單據代碼
                return;
            }

            String strSheetId = cmbSheetId.getSelectedItem().toString().toUpperCase().trim();

            GetSht(strSheetId);
        }
    };

    private View.OnClickListener lsRegisterFetch = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            if(etItemId.getText().toString().equals("")){
                //WAPG015002    請輸入料號
                ShowMessage(R.string.WAPG015002);
                return;
            }
            FetchLotInfo();
        }
    };

    private void FetchSheetInfo(String strSheetId){
//        String sheetId = etSheetId.getText().toString();
        if (strSheetId.equals(""))
        {
            _strSraechId = cmbSheetId.getSelectedItem().toString().toUpperCase().trim();
        }
        else
        {
            _strSraechId = strSheetId;
        }

        BModuleObject bmObjSheet = new BModuleObject();
        bmObjSheet.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObjSheet.setModuleID("BIFetchSheetMstAndDet");
        bmObjSheet.setRequestID("BIFetchSheetMstAndDet");

        bmObjSheet.params = new Vector<ParameterInfo>();

        HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
        List<Condition> lstCondition1 = new ArrayList<Condition>();

        //SHEET_ID
        Condition conditionSheetId = new Condition();
        conditionSheetId.setAliasTable("M");
        conditionSheetId.setColumnName("SHEET_ID");
        conditionSheetId.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
//        conditionSheetId.setValue(etSheetId.getText().toString());
        conditionSheetId.setValue(strSheetId);
        lstCondition1.add(conditionSheetId);
        mapCondition.put(conditionSheetId.getColumnName(), lstCondition1);

        //Serialize序列化
        VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param" );
        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vkey, vVal);
        String strCond = msdl.generateFinalCode(mapCondition);

        //Input param
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Condition);
        param1.setNetParameterValue(strCond);
        bmObjSheet.params.add(param1);

        // region 取得ConfigCond及ConfigSort
        BModuleObject biShtCfgSortAndCond = new BModuleObject();
        biShtCfgSortAndCond.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        biShtCfgSortAndCond.setModuleID("BIFetchConfigCondAndSortBySheetId");
        biShtCfgSortAndCond.setRequestID("FetchConfigCondAndSort");
        biShtCfgSortAndCond.params = new Vector<ParameterInfo>();
        ParameterInfo paramShtId = new ParameterInfo();
        paramShtId.setParameterID(BIFetchPickStrategyParam.SheetId);
        paramShtId.setParameterValue(strSheetId);
        biShtCfgSortAndCond.params.add(paramShtId);
        // endregion

        List<BModuleObject> lstBmObj = new ArrayList<>();
        lstBmObj.add(bmObjSheet);
        lstBmObj.add(biShtCfgSortAndCond);

        CallBIModule(lstBmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(CheckBModuleReturnInfo(bModuleReturn)){
                    dtMaster = bModuleReturn.getReturnJsonTables().get("BIFetchSheetMstAndDet").get("Mst");
                    dtDetail = bModuleReturn.getReturnJsonTables().get("BIFetchSheetMstAndDet").get("Det");

                    dtConfigCond = bModuleReturn.getReturnJsonTables().get("FetchConfigCondAndSort").get("SheetConfigCond");
                    dtConfigSort = bModuleReturn.getReturnJsonTables().get("FetchConfigCondAndSort").get("SheetConfigSort");

                    if(dtMaster == null || dtMaster.Rows.size() <= 0 || dtDetail == null || dtDetail.Rows.size() <= 0)
                    {
                        //WAPG015008    查詢單據資料錯誤
                        ShowMessage(R.string.WAPG015008);
                        return;
                    }

                    lstSeq = new ArrayList<String>();
                    for(DataRow dr : dtDetail.Rows){
                        Double doubleSeq = Double.parseDouble(dr.getValue("SEQ").toString());
                        Integer intSeq = Integer.valueOf(doubleSeq.intValue());

                        lstSeq.add(intSeq.toString());
                        mapSeq.put(intSeq.toString(), dr.getValue("SEQ").toString());
                    }

                    ArrayAdapter<String> adapterSeq = new ArrayAdapter<>(GoodPickNonSheetCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, lstSeq);
                    cmbSeq.setAdapter(adapterSeq);
                }
            }
        });
    }

    private AdapterView.OnItemClickListener lvRegisterClick = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            etSelectLotId.setText(dtRegister.Rows.get(position).getValue("REGISTER_ID").toString());
            strSelectBinId = dtRegister.Rows.get(position).getValue("BIN_ID").toString();
            index = position;
            CarrierSearch();
        }
    };

    private AdapterView.OnClickListener lsConfirm = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            PickConfirm();
        }
    };

    private TabHost.OnTabChangeListener lsTabHost_OnTabChange = new TabHost.OnTabChangeListener(){
        @Override
        public void onTabChanged(String tabID){
            if(dtXfr == null || dtXfr.Rows.size() <= 0) return;

            strCurrentTask = tabHost.getCurrentTabTag();

            switch (tabID)
            {
                case "CarrierStockOut":
                    strFromPort = tvFromBin.getText().toString();
                    strToPort = tvToBin.getText().toString();
                    strObject = tvCarrier.getText().toString();
                    break;
                case "CarrierRegisterUnBind":
                    strObject = tvRegisterId.getText().toString();
                    strFromPort = tvFromBin2.getText().toString();
                    strToPort = tvToBin2.getText().toString();
                    break;
                case "CarrierStockIn":
                    strFromPort = tvFromBin3.getText().toString();
                    strToPort = tvToBin3.getText().toString();
                    strObject = tvCarrier3.getText().toString();
                    break;
            }
        }
    };

    public void OnClickLotClear(View v){
        etSelectLotId.setText("");
    }

    public void OnClickQtyClear(View v){
        etPickQty.setText("");
    }

    //擴充挑貨規則: 條件串成 SQL Where 條件
    private String generateExtendConfigCond(DataRow drDet, DataTable dtCond) {

        String strExtendCond = null;

        if (dtCond != null && dtCond.Rows.size() > 0) {
            for (DataRow drCond : dtCond.Rows) {
                String reqVal = drDet.getValue(drCond.getValue("REQ_FIELD").toString()).toString();
                String strCond = null;

                if (!reqVal.equals("*")) {
                    strCond = String.format("SR.%s %s '%s'", drCond.getValue("REG_FIELD").toString(), drCond.get("COND_OPERATOR").toString(), reqVal); // e.g. SR.LOT_CODE = 'L...'

                    if (strExtendCond == null || strExtendCond.length() <= 0) {
                        strExtendCond = String.format("AND %s", strCond); // e.g. AND SR.LOT_CODE = 'LXXXX'
                    } else {
                        strExtendCond = String.format("%s AND %s", strExtendCond, strCond); // e.g. AND SR.LOT_CODE = 'LXXXX' AND R.XXX = 'OOOO'
                    }
                }
            }
        }

        return strExtendCond;
    }

    //擴充挑貨規則: 排序串成 SQL Order By 條件
    private String generateExtendConfigSort(DataTable dtSort) {

        List<String> lstConfigSort = new ArrayList<>();

        String strConfigSort = null;
        if (dtSort != null && dtSort.Rows.size() > 0) {
            for (DataRow drSort : dtSort.Rows) {
                lstConfigSort.add(String.format("%s %s", drSort.getValue("REG_FIELD").toString(), drSort.get("SORT_METHOD").toString())); // e.g. MFG_DATE ASC
            }
        }

        if (lstConfigSort.size() > 0) {
            strConfigSort = TextUtils.join(", ", lstConfigSort);
        }

        return strConfigSort;
    }
}

//ERROR CODE WAPG015
//WAPG015001    請輸入批號
//WAPG015002    請輸入料號
//WAPG015003    請選擇物料
//WAPG015004    請選擇物料並輸入檢貨數量
//WAPG015005    檢貨出庫任務[%s]完成
//WAPG015006    請輸入數量
//WAPG015007    請選擇單據代碼
//WAPG015008    查詢單據資料錯誤
//WAPG015009    單據代碼[{0}]查無轉揀料單資料

