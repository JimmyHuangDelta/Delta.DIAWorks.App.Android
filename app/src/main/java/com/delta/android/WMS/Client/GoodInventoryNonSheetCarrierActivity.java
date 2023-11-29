package com.delta.android.WMS.Client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.delta.android.R;
import com.delta.android.WMS.Client.GridAdapter.GoodInventoryNonSheetGridAdapter;
import com.delta.android.WMS.Param.BAutoJudgeXfrTaskGenerateParam;
import com.delta.android.WMS.Param.BIPDANoSheetCarrierPortalParam;
import com.delta.android.WMS.Param.BIPDANoSheetCartPortalParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.BPDADispatchOrderForInvParam;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.delta.android.WMS.Param.ParamObj.WMSTasklistParam;
import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GoodInventoryNonSheetCarrierActivity extends BaseFlowActivity {

    //Function WAPG013

    private TabHost tabHost;
    private EditText etLotId;
    private EditText etIsBin;
    private EditText etBin;
    private EditText etItemName;
    private EditText etItem;
    private EditText etQty;

    private Spinner cmbRecommendBin;
    private Spinner cmbRecommendCarrier;
    private Spinner cmbRecommendBlock;
    private Button btnRecommend;

    private TextView tvCarrier;
    private TextView tvFromBin;
    private TextView tvToBin;
    private TextView tvRegisterID;
    private TextView tvBlockID;
    private TextView tvFromBin2;
    private TextView tvToBin2;
    private TextView tvCarrier3;
    private TextView tvFromBin3;
    private TextView tvToBin3;
    private ImageButton ibtnSearch;
    private Button btnConfirm;

    private String strStorageId;
    private String strFromPort;
    private String strToPort;
    private String strObject;
    private String strCurrentTask;
    private int iStep;

    //public DataTable dtRegister;
    public DataTable dtReg;
    public DataTable dtRegisterXfr;
    public DataTable dtBinID;
    public DataTable dtCarrierBlockID;

    ArrayList<String> lstRecommendBin = null;
    ArrayList<String> lstRecommendCarrier = null;
    ArrayList<String> lstRecommendBlock = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good_inventory_non_sheet_carrier);

        initControl();
        setListensers();
    }

    private void initControl(){
        tabHost = findViewById(R.id.tabHost);
        etLotId = findViewById(R.id.etLotId);
        etIsBin = findViewById(R.id.etIsBin);
        etBin = findViewById(R.id.etBin);
        etItemName = findViewById(R.id.etItemName);
        etItem = findViewById(R.id.etItem);
        etQty = findViewById(R.id.etQty);
        cmbRecommendBin = findViewById(R.id.cmbRecommendBin);
        cmbRecommendCarrier = findViewById(R.id.cmbRecommendCarrier);
        cmbRecommendBlock = findViewById(R.id.cmbRecommendBlock);
        btnRecommend = findViewById(R.id.btnRecommend);
        ibtnSearch = findViewById(R.id.ibtnSearch);
        btnConfirm = findViewById(R.id.btnConfirm);

        //CarrierOut
        tvCarrier = findViewById(R.id.tvCarrierID);
        tvFromBin = findViewById(R.id.tvFromBinID);
        tvToBin = findViewById(R.id.tvToBinID);

        //CarrierRegisterBind
        tvRegisterID = findViewById(R.id.tvRegisterID);
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

        TabHost.TabSpec spec2 = tabHost.newTabSpec("CarrierRegisterBind");
        View tab2 = LayoutInflater.from(this).inflate(R.layout.activity_wms_good_inventory_tab_widget, null);
        TextView tvTab2 = tab2.findViewById(R.id.tvTabText);
        tvTab2.setText(R.string.CARRIER_REGISTER_BIND);
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
        tabHost.setOnTabChangedListener(lsTabHost_OnTabChange);
        ibtnSearch.setOnClickListener(ibtnLotSearchClick);
        btnRecommend.setOnClickListener(lsRecommend);
        btnConfirm.setOnClickListener(lsConfirm);
    }

    private void ClearData(){
        etLotId.setText("");
        etIsBin.setText("");
        etQty.setText("");
        etItem.setText("");
        etItemName.setText("");
        etBin.setText("");

        lstRecommendBin = new ArrayList<>();
        lstRecommendCarrier = new ArrayList<>();
        lstRecommendBlock = new ArrayList<>();

        ArrayAdapter<String> adapterBin = new ArrayAdapter<>(GoodInventoryNonSheetCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, lstRecommendBin);
        cmbRecommendBin.setAdapter(adapterBin);
        ArrayAdapter<String> adapterCarrier = new ArrayAdapter<>(GoodInventoryNonSheetCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, lstRecommendCarrier);
        cmbRecommendCarrier.setAdapter(adapterCarrier);
        ArrayAdapter<String> adapterBlock = new ArrayAdapter<>(GoodInventoryNonSheetCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, lstRecommendBlock);
        cmbRecommendBlock.setAdapter(adapterBlock);

        tvCarrier.setText("");
        tvFromBin.setText("");
        tvToBin.setText("");
        tvRegisterID.setText("");
        tvBlockID.setText("");
        tvFromBin2.setText("");
        tvToBin2.setText("");
        tvCarrier3.setText("");
        tvFromBin3.setText("");
        tvToBin3.setText("");
    }

    private void CarrierSearch(){
        if(etLotId.getText().toString().equals("")){
            ShowMessage(R.string.WAPG013001); //WAPG013001 請輸入批號
            return;
        }
        if(etIsBin.getText().toString().equals("")){
            ShowMessage(R.string.WAPG013006); //WAPG013006    請輸入入料口
            return;
        }

        //BIPDANoSheetCarrierPortal
        BModuleObject bmObjDispatch = new BModuleObject();
        bmObjDispatch.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDANoSheetCarrierPortal");
        bmObjDispatch.setModuleID("BIGetBestBin");
        bmObjDispatch.setRequestID("BIGetBestBin");

        bmObjDispatch.params = new Vector<ParameterInfo>();
        ParameterInfo paramDispatch = new ParameterInfo();
        paramDispatch.setParameterID(BIPDANoSheetCarrierPortalParam.RegisterId);
        paramDispatch.setParameterValue(etLotId.getText().toString());
        bmObjDispatch.params.add(paramDispatch);

        ParameterInfo paramStock = new ParameterInfo();
        paramStock.setParameterID(BIPDANoSheetCarrierPortalParam.stock);
        paramStock.setParameterValue("IN");
        bmObjDispatch.params.add(paramStock);

        ParameterInfo paramFetchBin = new ParameterInfo();
        paramFetchBin.setParameterID(BIPDANoSheetCarrierPortalParam.FetchBinId);
        paramFetchBin.setParameterValue(etIsBin.getText().toString());
        bmObjDispatch.params.add(paramFetchBin);

        CallBIModule(bmObjDispatch, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(CheckBModuleReturnInfo(bModuleReturn)){
                    
                    Gson gson = new Gson();
                    dtReg = bModuleReturn.getReturnJsonTables().get("BIGetBestBin").get("RegisterInfo");
                    dtBinID = bModuleReturn.getReturnJsonTables().get("BIGetBestBin").get("RecommendBin");
                    dtCarrierBlockID = bModuleReturn.getReturnJsonTables().get("BIGetBestBin").get("RecommendCarrierBlock");
                    dtRegisterXfr = bModuleReturn.getReturnJsonTables().get("BIGetBestBin").get("RegisterXfrInfo");
                    strStorageId = gson.fromJson(bModuleReturn.getReturnList().get("BIGetBestBin").get(BPDADispatchOrderForInvParam.StorageId).toString(), String.class);

                    etItemName.setText(dtReg.Rows.get(0).getValue("ITEM_NAME").toString());
                    etItem.setText(dtReg.Rows.get(0).getValue("ITEM_ID").toString());
                    etQty.setText(dtReg.Rows.get(0).getValue("QTY").toString());

                    strCurrentTask = "";
                    String strTmpBestBin = "";
                    String strTmpCarrier = "";
                    String strTmpBlock = "";

                    if(dtRegisterXfr.Rows.size() > 0) {
                        strCurrentTask = dtRegisterXfr.Rows.get(0).getValue("XFR_TASK").toString();
                        strTmpBestBin = dtRegisterXfr.Rows.get(0).getValue("TEMP_BEST_BIN_ID").toString();
                        strTmpCarrier = dtRegisterXfr.Rows.get(0).getValue("TEMP_CARRIER_ID").toString();
                        strTmpBlock = dtRegisterXfr.Rows.get(0).getValue("TEMP_BLOCK_ID").toString();
                    }

                    if(strCurrentTask.equals(""))
                    {
                        lstRecommendBin = new ArrayList<String>();
                        for(DataRow dr : dtBinID.Rows){
                            String strBin = dr.getValue("BIN_ID").toString();
                            lstRecommendBin.add(strBin);
                        }
                        ArrayAdapter<String> adapterBin = new ArrayAdapter<>(GoodInventoryNonSheetCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, lstRecommendBin);
                        cmbRecommendBin.setAdapter(adapterBin);

                        if(dtCarrierBlockID.Rows.size() > 0)
                        {
                            lstRecommendCarrier = new ArrayList<String>();
                            final HashMap<String, ArrayList<String>> mapBlock = new HashMap<>();
                            for(DataRow dr : dtCarrierBlockID.Rows){
                                String strCarrier = dr.getValue("CARRIER_ID").toString();
                                String strBlock = dr.getValue("BLOCK_ID").toString();
                                if(!lstRecommendCarrier.contains(strCarrier)){
                                    lstRecommendCarrier.add(strCarrier);
                                }
                                if(!mapBlock.containsKey(strCarrier)){
                                    ArrayList<String> lstBlock = new ArrayList<String>();
                                    lstBlock.add(strBlock);
                                    mapBlock.put(strCarrier, lstBlock);
                                }
                                else{
                                    mapBlock.get(strCarrier).add(strBlock);
                                }
                            }
                            ArrayAdapter<String> adapterCarrier = new ArrayAdapter<>(GoodInventoryNonSheetCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, lstRecommendCarrier);
                            cmbRecommendCarrier.setAdapter(adapterCarrier);

                            cmbRecommendCarrier.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l){
                                    ArrayAdapter<String> adapterBlock = new ArrayAdapter<>(GoodInventoryNonSheetCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, mapBlock.get(cmbRecommendCarrier.getSelectedItem().toString()));
                                    cmbRecommendBlock.setAdapter(adapterBlock);
                                }
                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView){
                                    ArrayAdapter<String> adapterBlock = new ArrayAdapter<>(GoodInventoryNonSheetCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, mapBlock.get(cmbRecommendCarrier.getSelectedItem().toString()));
                                    cmbRecommendBlock.setAdapter(adapterBlock);
                                }
                            });

                            ArrayAdapter<String> adapterBlock = new ArrayAdapter<>(GoodInventoryNonSheetCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, mapBlock.get(cmbRecommendCarrier.getSelectedItem().toString()));
                            cmbRecommendBlock.setAdapter(adapterBlock);
                        }
                    }
                    else
                    {
                        lstRecommendBin = new ArrayList<String>();
                        lstRecommendBin.add(strTmpBestBin);
                        ArrayAdapter<String> adapterBin = new ArrayAdapter<>(GoodInventoryNonSheetCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, lstRecommendBin);
                        cmbRecommendBin.setAdapter(adapterBin);
                        cmbRecommendBin.setSelection(0);

                        lstRecommendCarrier = new ArrayList<String>();
                        lstRecommendCarrier.add(strTmpCarrier);
                        ArrayAdapter<String> adapterCarrier = new ArrayAdapter<>(GoodInventoryNonSheetCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, lstRecommendCarrier);
                        cmbRecommendCarrier.setAdapter(adapterCarrier);
                        cmbRecommendCarrier.setSelection(0);

                        lstRecommendBlock = new ArrayList<String>();
                        lstRecommendBlock.add(strTmpBlock);
                        ArrayAdapter<String> adapterBlock = new ArrayAdapter<>(GoodInventoryNonSheetCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, lstRecommendBlock);
                        cmbRecommendBlock.setAdapter(adapterBlock);
                        cmbRecommendCarrier.setSelection(0);

                        cmbRecommendBin.setEnabled(false);
                        cmbRecommendCarrier.setEnabled(false);
                        cmbRecommendBlock.setEnabled(false);

                        //顯示Tab資訊
                        tvCarrier.setText(cmbRecommendCarrier.getSelectedItem().toString());
                        tvFromBin.setText(etIsBin.getText().toString());
                        tvToBin.setText(cmbRecommendBin.getSelectedItem().toString());

                        tvRegisterID.setText(etLotId.getText().toString());
                        tvBlockID.setText(cmbRecommendBlock.getSelectedItem().toString());
                        tvFromBin2.setText(etIsBin.getText().toString());
                        tvToBin2.setText(etIsBin.getText().toString());

                        tvCarrier3.setText(cmbRecommendCarrier.getSelectedItem().toString());
                        tvFromBin3.setText(etIsBin.getText().toString());
                        tvToBin3.setText(cmbRecommendBin.getSelectedItem().toString());

                        //跳至下一個流程任務
                        iStep = 0;
                        switch (strCurrentTask){
                            case "CarrierStockOut":
                                iStep = 1;
                                strToPort = tvToBin.getText().toString();
                                strFromPort = tvToBin.getText().toString();
                                strObject = tvCarrier.getText().toString();
                                break;
                            case "CarrierRegisterBind":
                                iStep = 2;
                                strFromPort = tvFromBin2.getText().toString();
                                strToPort = tvToBin2.getText().toString();
                                strObject = tvRegisterID.getText().toString();
                                break;
                            case "CarrierStockIn":
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
            }
        });
    }

    private void InventoryConfirm(){

        if(dtReg == null || dtReg.Rows.size() <= 0) return;

        String tempBin1 = cmbRecommendBin.getSelectedItem().toString();
        String tempBin2 = etBin.getText().toString();

        if(!tempBin1.equals(tempBin2)){
            ShowMessage(R.string.WAPG013005); //WAPG013005    請確認儲位代碼是否與推薦儲位相同
            return;
        }

        //BI
        BModuleObject bimObj = new BModuleObject();
        bimObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDANoSheetCarrierPortal");
        bimObj.setModuleID("BIRegisterXfrConfirm");
        bimObj.setRequestID("BIRegisterXfrConfirm");
        bimObj.params = new Vector<ParameterInfo>();

        // Input param
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIPDANoSheetCarrierPortalParam.RegisterId);
        param1.setParameterValue(etLotId.getText().toString());
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
        param8.setParameterValue(cmbRecommendCarrier.getSelectedItem().toString());
        bimObj.params.add(param8);

        ParameterInfo param9 = new ParameterInfo();
        param9.setParameterID(BIPDANoSheetCarrierPortalParam.Block);
        param9.setParameterValue(cmbRecommendBlock.getSelectedItem().toString());
        bimObj.params.add(param9);

        ParameterInfo param10 = new ParameterInfo();
        param10.setParameterID(BIPDANoSheetCarrierPortalParam.BestBin);
        param10.setParameterValue(cmbRecommendBin.getSelectedItem().toString());
        bimObj.params.add(param10);

        ParameterInfo param11 = new ParameterInfo();
        param11.setParameterID(BIPDANoSheetCarrierPortalParam.stock);
        param11.setParameterValue("I");
        bimObj.params.add(param11);

        ParameterInfo param12 = new ParameterInfo();
        param12.setParameterID(BIPDANoSheetCarrierPortalParam.RegQty);
        double dQty = Double.parseDouble(etQty.getText().toString());
        param12.setParameterValue(Double.toString(dQty));
        bimObj.params.add(param12);

        ParameterInfo param13 = new ParameterInfo(); // 20220812 Ikea 傳入 ItemId
        param13.setParameterID(BIPDANoSheetCarrierPortalParam.ItemId);
        param13.setParameterValue(etItem.getText().toString());
        bimObj.params.add(param13);

        ParameterInfo param14 = new ParameterInfo(); // 20220812 Ikea 傳入 BinId
        param14.setParameterID(BIPDANoSheetCarrierPortalParam.FetchBinId);
        param14.setParameterValue(etBin.getText().toString());
        bimObj.params.add(param14);

        CallBIModule(bimObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(CheckBModuleReturnInfo(bModuleReturn)){
                    Object[] args = new Object[1];
                    args[0] = strCurrentTask;
                    ShowMessage(R.string.WAPG013004, args); //WAPG013004 上架任務[%s]完成

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

    private View.OnClickListener ibtnLotSearchClick = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            CarrierSearch();
        }
    };


    private TabHost.OnTabChangeListener lsTabHost_OnTabChange = new TabHost.OnTabChangeListener(){
        @Override
        public void onTabChanged(String tabID){
            if(dtReg == null || dtReg.Rows.size() <= 0) return;

            strCurrentTask = tabHost.getCurrentTabTag();

            switch (tabID)
            {
                case "CarrierStockOut":
                    strFromPort = tvFromBin.getText().toString();
                    strToPort = tvToBin.getText().toString();
                    strObject = tvCarrier.getText().toString();
                    break;
                case "CarrierRegisterBind":
                    strObject = tvRegisterID.getText().toString();
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

    private AdapterView.OnClickListener lsRecommend = new View.OnClickListener(){
        @Override
        public void onClick(View view){

            if(etLotId.getText().toString().equals("")){
                ShowMessage(R.string.WAPG013001); //WAPG013001 請輸入批號
                return;
            }
            if(etIsBin.getText().toString().equals("")){
                ShowMessage(R.string.WAPG013006); //WAPG013006    請輸入入料口
                return;
            }
            if(cmbRecommendBin.getSelectedItem().toString().equals("") || cmbRecommendCarrier.getSelectedItem().toString().equals("") || cmbRecommendBlock.getSelectedItem().toString().equals(""))
                return;

            cmbRecommendBin.setEnabled(false);
            cmbRecommendCarrier.setEnabled(false);
            cmbRecommendBlock.setEnabled(false);

            //顯示Tab資訊
            tvCarrier.setText(cmbRecommendCarrier.getSelectedItem().toString());
            tvFromBin.setText(cmbRecommendBin.getSelectedItem().toString());
            tvToBin.setText(etIsBin.getText().toString());

            tvRegisterID.setText(etLotId.getText().toString());
            tvBlockID.setText(cmbRecommendBlock.getSelectedItem().toString());
            tvFromBin2.setText(etIsBin.getText().toString());
            tvToBin2.setText(etIsBin.getText().toString());

            tvCarrier3.setText(cmbRecommendCarrier.getSelectedItem().toString());
            tvFromBin3.setText(etIsBin.getText().toString());
            tvToBin3.setText(cmbRecommendBin.getSelectedItem().toString());

            //跳至下一個流程任務
            iStep = 0;
            switch (strCurrentTask){
                case "CarrierStockOut":
                    iStep = 1;
                    strToPort = tvToBin.getText().toString();
                    strFromPort = tvToBin.getText().toString();
                    strObject = tvCarrier.getText().toString();
                    break;
                case "CarrierRegisterBind":
                    iStep = 2;
                    strFromPort = tvFromBin2.getText().toString();
                    strToPort = tvToBin2.getText().toString();
                    strObject = tvRegisterID.getText().toString();
                    break;
                case "CarrierStockIn":
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
    };

    private AdapterView.OnClickListener lsConfirm = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            InventoryConfirm();
        }
    };
}

//ERROR CODE WAPG013
//WAPG013001    請輸入批號
//WAPG013002    該批號不屬於載具上架
//WAPG013003    上架流程完成
//WAPG013004    上架任務[%s]完成
//WAPG013005    請確認儲位代碼是否與推薦儲位相同
//WAPG013006    請輸入入料口