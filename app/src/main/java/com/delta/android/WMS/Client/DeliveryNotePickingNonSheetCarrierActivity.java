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
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.PMS.OffLineData.Data;
import com.delta.android.R;
import com.delta.android.WMS.Client.GridAdapter.GoodPickNonSheetGridAdapter;
import com.delta.android.WMS.Param.BDeliveryNotePickingParam;
import com.delta.android.WMS.Param.BIFetchPickStrategyParam;
import com.delta.android.WMS.Param.BIFetchProcessSheetParam;
import com.delta.android.WMS.Param.BIPDADeliveryNotePickCarrierPortalParam;
import com.delta.android.WMS.Param.BIPDANoSheetCarrierPortalParam;
import com.delta.android.WMS.Param.BIPDANoSheetRegisterPortalParam;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class DeliveryNotePickingNonSheetCarrierActivity extends BaseFlowActivity {

    //Function WAPG018

    private TabHost tabHost;
//    private EditText etSheetId;
    private Spinner cmbSheetId;
    private EditText etLotId;
    private EditText etItemId;
    private EditText etSelectLotId;
    private EditText etPickQty;
    private Spinner cmbSeq;
    private ListView lvRegister;
    private ImageButton ibtnRegsiterSearch;
    private ImageButton ibtnSheetSearch;
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
    private int iStep;

    public DataTable dtRegister;
    public DataTable dtXfr;
    public DataTable dtDnMst;
    public DataTable dtDnDet;

    private DataTable dtConfigCond = null;
    private DataTable dtConfigSort = null;
    private String strConfigCond = null;
    private String strConfigSort = null;

    public HashMap<String, String> mapSeqItem = new HashMap<String, String>();
    private ArrayList<String> lstSheetId = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_note_picking_non_sheet_carrier);

        initControl();

        // 載入單據代碼至下拉選單
        GetSheetId();

        setListensers();
    }

    private void initControl(){
        tabHost = findViewById(R.id.tabHost);
//        etSheetId = findViewById(R.id.etSheetId);
        cmbSheetId = findViewById(R.id.cmbSheetId);
        etLotId = findViewById(R.id.etLotId);
        etItemId = findViewById(R.id.etItemId);
        etSelectLotId = findViewById(R.id.etSelectLotId);
        etPickQty = findViewById(R.id.etQty);
        cmbSeq = findViewById(R.id.cmbSeq);
        lvRegister = findViewById(R.id.lvRegisters);
        btnConfirm = findViewById(R.id.btnConfirm);
        ibtnSheetSearch = findViewById(R.id.ibtnSheetSearch);
        ibtnRegsiterSearch = findViewById(R.id.ibtnRegisterSearch);

        //Item ID不可編輯
        etItemId.setEnabled(false);

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
        ibtnSheetSearch.setOnClickListener(ibtnSheetSearchClick);
        cmbSeq.setOnItemSelectedListener(cmbSeqItemSelected);
        ibtnRegsiterSearch.setOnClickListener(ibtnRegisterSearchClick);
        lvRegister.setOnItemClickListener(lvRegisterClick);
        btnConfirm.setOnClickListener(lsConfirm);
        tabHost.setOnTabChangedListener(lsTabHost_OnTabChange);
    }

    private void ClearData(){
//        etSheetId.setText("");
        cmbSheetId.setSelection(lstSheetId.size()-1); // spinner設定回預設選項

        etItemId.setText("");
        etLotId.setText("");
        etPickQty.setText("");

        dtRegister = new DataTable();
        dtDnMst = new DataTable();
        dtDnDet = new DataTable();
        dtXfr = null;

        GetListView();

        etSelectLotId.setText("");
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

        ArrayList<String> alSeq = new ArrayList<String>();
        ArrayAdapter<String> adapterSeq = new ArrayAdapter<String>(DeliveryNotePickingNonSheetCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, alSeq);
        cmbSeq.setAdapter(adapterSeq);
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
        param1.setParameterValue("DN");
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

                    SimpleArrayAdapter adapter = new DeliveryNotePickingNonSheetCarrierActivity.SimpleArrayAdapter<>(DeliveryNotePickingNonSheetCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, lstSheetId);
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

    private void FetchSheetInfo(){

        BModuleObject bmObjSheet = new BModuleObject();
        bmObjSheet.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDADeliveryNotePickCarrierPortal");
        bmObjSheet.setModuleID("BIGetSheetInfo");
        bmObjSheet.setRequestID("BIGetSheetInfo");

        bmObjSheet.params = new Vector<ParameterInfo>();
        ParameterInfo paramSheet = new ParameterInfo();
        paramSheet.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.SheetId);
//        paramSheet.setParameterValue(etSheetId.getText().toString());
        paramSheet.setParameterValue(cmbSheetId.getSelectedItem().toString().toUpperCase().trim());
        bmObjSheet.params.add(paramSheet);

        // region 取得ConfigCond及ConfigSort
        BModuleObject biShtCfgSortAndCond = new BModuleObject();
        biShtCfgSortAndCond.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIDeliveryNotePicking");
        biShtCfgSortAndCond.setModuleID("BIFetchConfigCondAndSort");
        biShtCfgSortAndCond.setRequestID("FetchConfigCondAndSort");
        biShtCfgSortAndCond.params = new Vector<ParameterInfo>();
        ParameterInfo paramShtId = new ParameterInfo();
        paramShtId.setParameterID(BDeliveryNotePickingParam.SheetId);
        paramShtId.setParameterValue(cmbSheetId.getSelectedItem().toString().toUpperCase().trim());
        biShtCfgSortAndCond.params.add(paramShtId);
        // endregion

        List<BModuleObject> lstBmObj = new ArrayList<>();
        lstBmObj.add(bmObjSheet);
        lstBmObj.add(biShtCfgSortAndCond);

        CallBIModule(lstBmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(CheckBModuleReturnInfo(bModuleReturn)){
                    dtDnMst = bModuleReturn.getReturnJsonTables().get("BIGetSheetInfo").get("DnMst");
                    dtDnDet = bModuleReturn.getReturnJsonTables().get("BIGetSheetInfo").get("DnDet");

                    dtConfigCond = bModuleReturn.getReturnJsonTables().get("FetchConfigCondAndSort").get("SheetConfigCond");
                    dtConfigSort = bModuleReturn.getReturnJsonTables().get("FetchConfigCondAndSort").get("SheetConfigSort");

                    ArrayList<String> alSeq = new ArrayList<>();

                    for(DataRow dr : dtDnDet.Rows){
                        String strSeq = Replace(dr.getValue("SEQ").toString());

                        if(mapSeqItem.containsKey(strSeq)) continue;
                        alSeq.add(strSeq);
                        mapSeqItem.put(strSeq, dr.getValue("ITEM_ID").toString());

                        strConfigCond = generateExtendConfigCond(dr, dtConfigCond, "SR");
                        strConfigSort = generateExtendConfigSort(dtConfigSort);
                    }

                    ArrayAdapter<String> adapterSeq = new ArrayAdapter<>(DeliveryNotePickingNonSheetCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, alSeq);

                    cmbSeq.setAdapter(adapterSeq);
                }
            }
        });
    }

    private String Replace(String s){
        if(s != null && s.indexOf(".") > 0){
            s = s.replaceAll("0+?$","");
            s = s.replaceAll("[.]$","");
        }
        return s;
    }

    private void FetchLotInfo(){
        BModuleObject bmObjLot = new BModuleObject();
        bmObjLot.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDADeliveryNotePickCarrierPortal");
        bmObjLot.setModuleID("BIGetPickRegister");
        bmObjLot.setRequestID("BIGetPickRegister");

        bmObjLot.params = new Vector<ParameterInfo>();
        ParameterInfo paramReg = new ParameterInfo();
        paramReg.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.RegisterId);
        paramReg.setParameterValue(etLotId.getText().toString());
        bmObjLot.params.add(paramReg);

        ParameterInfo paramItem = new ParameterInfo();
        paramItem.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.ItemId);
        paramItem.setParameterValue(etItemId.getText().toString());
        bmObjLot.params.add(paramItem);

        ParameterInfo paramQty = new ParameterInfo();
        paramQty.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.PickQty);
        paramQty.setParameterValue(etPickQty.getText().toString());
        bmObjLot.params.add(paramQty);

        if (strConfigCond != null && strConfigCond.length() > 0) {
            ParameterInfo paramCond = new ParameterInfo();
            paramCond.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.ConfigCond);
            paramCond.setParameterValue(strConfigCond);
            bmObjLot.params.add(paramCond);
        }

        if (strConfigSort != null && strConfigSort.length() > 0) {
            ParameterInfo paramSort = new ParameterInfo();
            paramSort.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.ConfigSort);
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

    private void GetListView(){
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        GoodPickNonSheetGridAdapter adapter = new GoodPickNonSheetGridAdapter(dtRegister, inflater);
        lvRegister.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void CarrierSearch(){
        if(etSelectLotId.getText().toString().equals("")){
            ShowMessage(R.string.WAPG018004); //WAPG018004 請選擇批號
            return;
        }

        BModuleObject bmObjDispatch = new BModuleObject();
        bmObjDispatch.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDADeliveryNotePickCarrierPortal");
        bmObjDispatch.setModuleID("BIGetRegisterXfr");
        bmObjDispatch.setRequestID("BIGetRegisterXfr");

        bmObjDispatch.params = new Vector<ParameterInfo>();
        ParameterInfo paramDispatch = new ParameterInfo();
        paramDispatch.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.RegisterId);
        paramDispatch.setParameterValue(etSelectLotId.getText().toString());
        bmObjDispatch.params.add(paramDispatch);

        ParameterInfo paramStock = new ParameterInfo();
        paramStock.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.Stock);
        paramStock.setParameterValue("S");
        bmObjDispatch.params.add(paramStock);

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
                    tvToBin.setText(dtXfr.Rows.get(0).getValue("OS_BIN_ID").toString());

                    tvRegisterId.setText(etSelectLotId.getText().toString());
                    tvBlockID.setText(dtXfr.Rows.get(0).getValue("BEST_BLOCK").toString());
                    tvFromBin2.setText(dtXfr.Rows.get(0).getValue("OS_BIN_ID").toString());
                    tvToBin2.setText(dtXfr.Rows.get(0).getValue("OS_BIN_ID").toString());

                    tvCarrier3.setText(dtXfr.Rows.get(0).getValue("BEST_CARRIER_ID").toString());
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
            ShowMessage(R.string.WAPG018005); //WAPG018005  請選擇物料並輸入揀貨數量
            return;
        }

        BModuleObject bimObj = new BModuleObject();
        bimObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDADeliveryNotePickCarrierPortal");
        bimObj.setModuleID("BIRegisterXfrConfirm");
        bimObj.setRequestID("BIRegisterXfrConfirm");
        bimObj.params = new Vector<ParameterInfo>();

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.RegisterId);
        param1.setParameterValue(etSelectLotId.getText().toString());
        bimObj.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.XfrCase);
        param2.setParameterValue("Carrier");
        bimObj.params.add(param2);

        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.XfrTask);
        param3.setParameterValue(strCurrentTask);
        bimObj.params.add(param3);

        ParameterInfo param4 = new ParameterInfo();
        param4.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.FromPort);
        param4.setParameterValue(strFromPort);
        bimObj.params.add(param4);

        ParameterInfo param5 = new ParameterInfo();
        param5.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.ToPort);
        param5.setParameterValue(strToPort);
        bimObj.params.add(param5);

        ParameterInfo param6 = new ParameterInfo();
        param6.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.ObjectId);
        param6.setParameterValue(strObject);
        bimObj.params.add(param6);

        ParameterInfo param7 = new ParameterInfo();
        param7.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.StorageId);
        param7.setParameterValue(strStorageId);
        bimObj.params.add(param7);

        ParameterInfo param8 = new ParameterInfo();
        param8.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.Carrier);
        param8.setParameterValue(strCarrierId);
        bimObj.params.add(param8);

        ParameterInfo param9 = new ParameterInfo();
        param9.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.Block);
        param9.setParameterValue(strBlock);
        bimObj.params.add(param9);

        ParameterInfo param10 = new ParameterInfo();
        param10.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.BestBin);
        param10.setParameterValue(dtXfr.Rows.get(0).getValue("BEST_BIN"));
        bimObj.params.add(param10);

        ParameterInfo param11 = new ParameterInfo();
        param11.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.Stock);
        param11.setParameterValue("S");
        bimObj.params.add(param11);

        ParameterInfo param12 = new ParameterInfo();
        param12.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.PickQty);
        param12.setParameterValue(etPickQty.getText().toString());
        bimObj.params.add(param12);

        ParameterInfo param13 = new ParameterInfo();
        param13.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.SheetId);
//        param13.setParameterValue(etSheetId.getText().toString());
        param13.setParameterValue(cmbSheetId.getSelectedItem().toString().toUpperCase().trim());
        bimObj.params.add(param13);

        ParameterInfo param14 = new ParameterInfo();
        param14.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.Seq);
        param14.setParameterValue(cmbSeq.getSelectedItem().toString());
        bimObj.params.add(param14);

        ParameterInfo param15 = new ParameterInfo(); // 20220812 Ikea 傳入 BinId
        param15.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.OsBinId);
        param15.setParameterValue(dtXfr.Rows.get(0).getValue("OS_BIN_ID").toString());
        bimObj.params.add(param15);

        CallBIModule(bimObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(CheckBModuleReturnInfo(bModuleReturn)){
                    Object[] args = new Object[1];
                    args[0] = strCurrentTask;
                    ShowMessage(R.string.WAPG018006, args); //WAPG018006 揀貨任務[%s]完成

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

    private View.OnClickListener ibtnSheetSearchClick = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            /*
            if(etSheetId.getText().toString().equals("")){
                ShowMessage(R.string.WAPG018001); //WAPG018001 請輸入單據代碼
                return;
            }
             */

            int sheetIdIndex = cmbSheetId.getSelectedItemPosition();
            if (sheetIdIndex == (lstSheetId.size() - 1)) {
                ShowMessage(R.string.WAPG018001); //WAPG018001 請選擇單據代碼
                return;
            }

            FetchSheetInfo();
        }
    };

    private AdapterView.OnItemSelectedListener cmbSeqItemSelected = new AdapterView.OnItemSelectedListener(){
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l){
            if(mapSeqItem.containsKey(cmbSeq.getSelectedItem().toString())){
                String strItem = mapSeqItem.get(cmbSeq.getSelectedItem().toString());
                etItemId.setText(strItem);
            }

            DataRow drDet = dtDnDet.Rows.get(i);
            strConfigCond = generateExtendConfigCond(drDet, dtConfigCond, "SR");
            strConfigSort = generateExtendConfigSort(dtConfigSort);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView){
            //do nothing
        }
    };

    private View.OnClickListener ibtnRegisterSearchClick = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            if(etPickQty.getText().toString().equals("")){
                ShowMessage(R.string.WAPG018002); //WAPG018002 請輸入揀貨數量
                return;
            }
            if(cmbSeq.getSelectedItem().toString().equals("") || etItemId.getText().toString().equals("")){
                ShowMessage(R.string.WAPG018003); //WAPG018003 請確認項次與物料代碼是否正確
                return;
            }
            FetchLotInfo();
        }
    };

    private AdapterView.OnItemClickListener lvRegisterClick = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            etSelectLotId.setText(dtRegister.Rows.get(position).getValue("REGISTER_ID").toString());
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

    public void OnClickLotClear(View v) { etSelectLotId.setText(""); }

    //擴充挑貨規則: 條件串成 SQL Where 條件
    private String generateExtendConfigCond(DataRow drDet, DataTable dtCond, String strAlias) {

        String strExtendCond = null;

        if (dtCond != null && dtCond.Rows.size() > 0) {
            for (DataRow drCond : dtCond.Rows) {
                String reqVal = drDet.getValue(drCond.getValue("REQ_FIELD").toString()).toString();
                String strCond = null;

                if (!reqVal.equals("*")) {
                    strCond = String.format("%s.%s %s '%s'",strAlias, drCond.getValue("REG_FIELD").toString(), drCond.get("COND_OPERATOR").toString(), reqVal); // e.g. SR.LOT_CODE = 'L...'

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

//ERROR CODE WAPG018
//WAPG018001    請選擇單據代碼
//WAPG018002    請輸入揀貨數量
//WAPG018003    請確認項次與物料代碼是否正確
//WAPG018004    請選擇批號
//WAPG018005    請選擇物料並輸入揀貨數量
//WAPG018006    揀貨任務[%s]完成