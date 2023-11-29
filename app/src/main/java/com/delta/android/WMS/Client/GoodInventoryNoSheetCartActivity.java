package com.delta.android.WMS.Client;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionaryList;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.PMS.OffLineData.Data;
import com.delta.android.R;
import com.delta.android.WMS.Param.BIPDANoSheetCartPortalParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.BPDADispatchOrderForInvParam;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import static com.delta.android.Core.Common.Global.getContext;

public class GoodInventoryNoSheetCartActivity extends BaseFlowActivity {

    private ViewHolder holder = null;
    //private DataTable _dtReg; //記錄刷批號取得的相關資料
    private String strStorageId;
    private String strFromPort;
    private String strToPort;
    private String strObject;
    private String strCurrentTask;
    private int iStep;

    public DataTable dtReg;
    public DataTable dtRegisterXfr;
    public DataTable dtBinID;
    public DataTable dtCarrierBlockID;
    public DataTable dtCartInfo;

    static class ViewHolder
    {
        EditText EtRegister;
        EditText EtIsBin;
        EditText EtBIN;
        EditText EtItemName;
        EditText EtItem;
        EditText EtQty;
        ImageButton IbtnSearch;

        Spinner CmbRecommendBin;
        Button BtnRecommend;

        Button BtnConfirm;
        TabHost Tabhost;
        TextView TvCartID1;
        TextView TvFromBinID1;
        TextView TvToBinID1;
        TextView TvLotID2;
        TextView TvFromBinID2;
        TextView TvToBinID2;
        TextView TvCartID3;
        TextView TvFromBinID3;
        TextView TvToBinID3;
    }

    ArrayList<String> lstRecommendBin = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_good_inventory_no_sheet_cart);

        InitialSetup();

        // 設定監聽事件
        setListensers();

        InitTabHost();
    }

    //region 事件
    private View.OnKeyListener LotIdOnKey = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            //只有按下Enter才會反映
            if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(v.getWindowToken(), 0);// 隱藏虛擬鍵盤
                GetData();
                return true;
            }
            return false;
        }
    };

    private View.OnClickListener IbtnSearchOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            GetData();
        }
    };

    private View.OnClickListener lsRecommend = new View.OnClickListener()
    {
        @Override
        public void onClick(View view){

            if (holder.EtRegister.getText().toString().equals(""))
            {
                //WAPG014 請輸入批號
                ShowMessage(R.string.WAPG014001);
                return;
            }
            if(holder.EtIsBin.getText().toString().equals(""))
            {
                //WAPG014005 請輸入入料口
                ShowMessage(R.string.WAPG014005);
                return;
            }
            if(holder.CmbRecommendBin.getSelectedItem().toString().equals(""))
                return;

            holder.CmbRecommendBin.setEnabled(false);

            //Cart Info
            BModuleObject bimObj = new BModuleObject();
            bimObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
            bimObj.setModuleID("BIFetchBinToCart");
            bimObj.setRequestID("BIFetchBinToCart");
            bimObj.params = new Vector<ParameterInfo>();

            final HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
            List<Condition> lstCondition = new ArrayList<Condition>();
            Condition cond = new Condition();
            cond.setAliasTable("CB");
            cond.setColumnName("BIN_ID");
            cond.setValue(holder.CmbRecommendBin.getSelectedItem().toString());
            cond.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
            lstCondition.add(cond);
            mapCondition.put("BIN_ID", lstCondition);

            //Serialize序列化
            VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
            VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition","bmWMS.Library.Param");
            MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vKey, vVal);
            String strCond = msdl.generateFinalCode(mapCondition);

            //Input param
            ParameterInfo param1 = new ParameterInfo();
            param1.setParameterID(BIWMSFetchInfoParam.Condition);
            param1.setNetParameterValue(strCond);
            bimObj.params.add(param1);

            //Call BIModule
            CallBIModule(bimObj, new WebAPIClientEvent() {
                @Override
                public void onPostBack(BModuleReturn bModuleReturn) {
                    if(CheckBModuleReturnInfo(bModuleReturn)){
                        dtCartInfo = bModuleReturn.getReturnJsonTables().get("BIFetchBinToCart").get("CART");
                        String strCartId = dtCartInfo.Rows.get(0).getValue("CART_ID").toString();
                        String strCartBinPort = dtCartInfo.Rows.get(0).getValue("CART_BIN_PORT").toString();

                        holder.TvCartID1.setText(strCartId);
                        holder.TvFromBinID1.setText(dtCartInfo.Rows.get(0).getValue("CART_PORT").toString());
                        holder.TvToBinID1.setText(holder.EtIsBin.getText().toString());

                        holder.TvLotID2.setText(holder.EtRegister.getText().toString());
                        holder.TvFromBinID2.setText(holder.EtIsBin.getText().toString());
                        holder.TvToBinID2.setText(strCartBinPort);

                        holder.TvCartID3.setText(strCartId);
                        holder.TvFromBinID3.setText(holder.EtIsBin.getText().toString());
                        holder.TvToBinID3.setText(dtCartInfo.Rows.get(0).getValue("CART_FIX_PORT").toString());

                        //跳至下一個流程任務
                        iStep = 0;
                        switch (strCurrentTask){
                            case "CartStockOut":
                                iStep = 1;
                                strToPort = holder.TvToBinID1.getText().toString();
                                strFromPort = holder.TvFromBinID1.getText().toString();
                                strObject = holder.TvCartID1.getText().toString();
                                break;
                            case "CartRegisterBind":
                                iStep = 2;
                                strFromPort = holder.TvFromBinID2.getText().toString();
                                strToPort = holder.TvToBinID2.getText().toString();
                                strObject = holder.TvLotID2.getText().toString();
                                break;
                            case "CartStockIn":
                                iStep = 0;
                                strFromPort = holder.TvFromBinID3.getText().toString();
                                strToPort = holder.TvToBinID3.getText().toString();
                                strObject = holder.TvCartID3.getText().toString();
                                break;
                            default:
                                iStep = 0;
                                strCurrentTask = "CartStockOut";
                                strFromPort = holder.TvFromBinID1.getText().toString();
                                strToPort = holder.TvToBinID1.getText().toString();
                                strObject = holder.TvCartID1.getText().toString();
                                break;
                        }

                        holder.Tabhost.setCurrentTab(iStep);
                    }
                }
            });
        }
    };

    private View.OnClickListener BtnConfirmOnClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            ProcessExecute();
        }
    };

    private TabHost.OnTabChangeListener TabChange = new TabHost.OnTabChangeListener() {
        @Override
        public void onTabChanged(String tabId)
        {
            if (dtReg == null || dtReg.Rows.size() <= 0) return;

            switch (tabId)
            {
                case "1":
                    strFromPort = holder.TvFromBinID1.getText().toString();
                    strToPort = holder.TvToBinID1.getText().toString();
                    strObject = holder.TvCartID1.getText().toString();
                    break;

                case "2":
                    strObject = holder.TvLotID2.getText().toString();
                    strFromPort = holder.TvFromBinID2.getText().toString();
                    strToPort = holder.TvToBinID2.getText().toString();
                    break;

                case "3":
                    strFromPort = holder.TvFromBinID3.getText().toString();
                    strToPort = holder.TvToBinID3.getText().toString();
                    strObject = holder.TvCartID3.getText().toString();
                    break;

                default:
                    break;
            }
        }
    };
    //endregion

    //region 方法
    private void InitialSetup()
    {
        if (holder != null) return;

        holder = new ViewHolder();
        holder.EtRegister = findViewById(R.id.etLotId);
        holder.EtIsBin = findViewById(R.id.etIsBin);
        holder.EtBIN = findViewById(R.id.etBin);
        holder.EtItemName = findViewById(R.id.etItemName);
        holder.EtItem = findViewById(R.id.etItem);
        holder.EtQty = findViewById(R.id.etQty);
        holder.IbtnSearch = findViewById(R.id.ibtnSearch);

        holder.CmbRecommendBin = findViewById(R.id.cmbRecommendBin);
        holder.BtnRecommend = findViewById(R.id.btnRecommend);

        holder.BtnConfirm = findViewById(R.id.btnConfirm);
        holder.TvCartID1 = findViewById(R.id.tvCartID1);
        holder.TvFromBinID1 = findViewById(R.id.tvFromBinID1);
        holder.TvToBinID1 = findViewById(R.id.tvToBinID1);
        holder.TvLotID2 = findViewById(R.id.tvLotID2);
        holder.TvFromBinID2 = findViewById(R.id.tvFromBinID2);
        holder.TvToBinID2 = findViewById(R.id.tvToBinID2);
        holder.TvCartID3 = findViewById(R.id.tvCartID3);
        holder.TvFromBinID3 = findViewById(R.id.tvFromBinID3);
        holder.TvToBinID3 = findViewById(R.id.tvToBinID3);
        holder.Tabhost = findViewById(R.id.tabHost);
        holder.Tabhost.setup();
    }

    private void setListensers()
    {
        holder.IbtnSearch.setOnClickListener(IbtnSearchOnClick);
        holder.BtnRecommend.setOnClickListener(lsRecommend);
        holder.BtnConfirm.setOnClickListener(BtnConfirmOnClick);
        holder.Tabhost.setOnTabChangedListener(TabChange); // 切換tab時觸發事件，load畫面
    }

    private void InitTabHost()
    {
        TabHost.TabSpec spec2_1 = holder.Tabhost.newTabSpec("1");
        View tab2_1 = LayoutInflater.from(this).inflate(R.layout.activity_wms_good_inventory_tab_widget, null);
        TextView tvTab2_1 = tab2_1.findViewById(R.id.tvTabText);// 取得activity_wms_good_pick_tab_widget內的物件
        tvTab2_1.setText(R.string.CART_MOVE_OUT);
        spec2_1.setIndicator(tab2_1); // 設定Tab的圖示以及顯示的文字，用View的方式
        spec2_1.setContent(R.id.tab2_1);// 把想要加入的Intent加入到這個Tab(det linerlayout)
        holder.Tabhost.addTab(spec2_1);

        TabHost.TabSpec spec2_2 = holder.Tabhost.newTabSpec("2");
        View tab2_2 = LayoutInflater.from(this).inflate(R.layout.activity_wms_good_inventory_tab_widget, null);
        TextView tvTab2_2 = tab2_2.findViewById(R.id.tvTabText);// 取得activity_wms_good_pick_tab_widget內的物件
        tvTab2_2.setText(R.string.ITEM_CART_BIND);
        spec2_2.setIndicator(tab2_2); // 設定Tab的圖示以及顯示的文字，用View的方式
        spec2_2.setContent(R.id.tab2_2);// 把想要加入的Intent加入到這個Tab(det linerlayout)
        holder.Tabhost.addTab(spec2_2);

        TabHost.TabSpec spec2_3 = holder.Tabhost.newTabSpec("3");
        View tab2_3 = LayoutInflater.from(this).inflate(R.layout.activity_wms_good_inventory_tab_widget, null);
        TextView tvTab2_3 = tab2_3.findViewById(R.id.tvTabText);// 取得activity_wms_good_pick_tab_widget內的物件
        tvTab2_3.setText(R.string.CART_MOVE_IN);
        spec2_3.setIndicator(tab2_3); // 設定Tab的圖示以及顯示的文字，用View的方式
        spec2_3.setContent(R.id.tab2_3);// 把想要加入的Intent加入到這個Tab(det linerlayout)
        holder.Tabhost.addTab(spec2_3);

        for(int i = 0; i < 3; i++)
        {
            holder.Tabhost.getTabWidget().getChildAt(i).setClickable(false);
        }
    }

    private void GetData()
    {
        if (holder.EtRegister.getText().toString().equals(""))
        {
            //WAPG014 請輸入批號
            ShowMessage(R.string.WAPG014001);
            return;
        }
        if(holder.EtIsBin.getText().toString().equals(""))
        {
            //WAPG014005 請輸入入料口
            ShowMessage(R.string.WAPG014005);
            return;
        }

        //region Set BIModule
        // BIModule
        BModuleObject bimObj = new BModuleObject();
        bimObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDANoSheetCartPortal");
        bimObj.setModuleID("BIGetBestBin");
        bimObj.setRequestID("BIGetBestBin");
        bimObj.params = new Vector<ParameterInfo>();

        // Input param
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIPDANoSheetCartPortalParam.RegisterId);
        param1.setParameterValue(holder.EtRegister.getText().toString());
        bimObj.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIPDANoSheetCartPortalParam.XfrCase);
        param2.setParameterValue("Cart");
        bimObj.params.add(param2);

        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BIPDANoSheetCartPortalParam.Abnormal);
        param3.setParameterValue("Y");
        bimObj.params.add(param3);

        ParameterInfo param4 = new ParameterInfo();
        param4.setParameterID(BIPDANoSheetCartPortalParam.FetchBinId);
        param4.setParameterValue(holder.EtIsBin.getText().toString());
        bimObj.params.add(param4);
        // endregion

        CallBIModule(bimObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn)
            {
                if (CheckBModuleReturnInfo(bModuleReturn))
                {
                    Gson gson = new Gson();
                    dtReg = bModuleReturn.getReturnJsonTables().get("BIGetBestBin").get("RegisterInfo");
                    dtBinID = bModuleReturn.getReturnJsonTables().get("BIGetBestBin").get("RecommendBin");
                    dtCarrierBlockID = bModuleReturn.getReturnJsonTables().get("BIGetBestBin").get("RecommendCarrierBlock");
                    dtRegisterXfr = bModuleReturn.getReturnJsonTables().get("BIGetBestBin").get("RegisterXfrInfo");
                    dtCartInfo = bModuleReturn.getReturnJsonTables().get("BIGetBestBin").get("CartInfo");

                    strStorageId = gson.fromJson(bModuleReturn.getReturnList().get("BIGetBestBin").get(BIPDANoSheetCartPortalParam.StorageId).toString(), String.class);
                    holder.EtItemName.setText(dtReg.Rows.get(0).getValue("ITEM_NAME").toString());
                    holder.EtItem.setText(dtReg.Rows.get(0).getValue("ITEM_ID").toString());
                    holder.EtQty.setText(dtReg.Rows.get(0).getValue("QTY").toString());

                    strCurrentTask = "";
                    String strTmpBestBin = "";
                    String strTmpCarrier = "";
                    String strTmpBlock = "";

                    if(dtRegisterXfr.Rows.size() > 0){
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
                        ArrayAdapter<String> adapterBin = new ArrayAdapter<>(GoodInventoryNoSheetCartActivity.this, android.R.layout.simple_spinner_dropdown_item, lstRecommendBin);
                        holder.CmbRecommendBin.setAdapter(adapterBin);
                    }
                    else
                    {
                        lstRecommendBin = new ArrayList<String>();
                        lstRecommendBin.add(strTmpBestBin);
                        ArrayAdapter<String> adapterBin = new ArrayAdapter<>(GoodInventoryNoSheetCartActivity.this, android.R.layout.simple_spinner_dropdown_item, lstRecommendBin);
                        holder.CmbRecommendBin.setAdapter(adapterBin);
                        holder.CmbRecommendBin.setSelection(0);

                        holder.CmbRecommendBin.setEnabled(false);

                        //Cart Info
                        BModuleObject bimObj = new BModuleObject();
                        bimObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
                        bimObj.setModuleID("BIFetchBinToCart");
                        bimObj.setRequestID("BIFetchBinToCart");
                        bimObj.params = new Vector<ParameterInfo>();

                        final HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
                        List<Condition> lstCondition = new ArrayList<Condition>();
                        Condition cond = new Condition();
                        cond.setAliasTable("CB");
                        cond.setColumnName("BIN_ID");
                        cond.setValue(holder.CmbRecommendBin.getSelectedItem().toString());
                        cond.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
                        lstCondition.add(cond);
                        mapCondition.put("BIN_ID", lstCondition);

                        //Serialize序列化
                        VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
                        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition","bmWMS.Library.Param");
                        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vKey, vVal);
                        String strCond = msdl.generateFinalCode(mapCondition);

                        //Input param
                        ParameterInfo param1 = new ParameterInfo();
                        param1.setParameterID(BIWMSFetchInfoParam.Condition);
                        param1.setNetParameterValue(strCond);
                        bimObj.params.add(param1);

                        //Call BIModule
                        CallBIModule(bimObj, new WebAPIClientEvent() {
                            @Override
                            public void onPostBack(BModuleReturn bModuleReturn) {
                                if(CheckBModuleReturnInfo(bModuleReturn)){
                                    dtCartInfo = bModuleReturn.getReturnJsonTables().get("BIFetchBinToCart").get("CART");
                                    String strCartId = dtCartInfo.Rows.get(0).getValue("CART_ID").toString();
                                    String strCartBinPort = dtCartInfo.Rows.get(0).getValue("CART_BIN_PORT").toString();

                                    holder.TvCartID1.setText(strCartId);
                                    holder.TvFromBinID1.setText(dtCartInfo.Rows.get(0).getValue("CART_PORT").toString());
                                    holder.TvToBinID1.setText(holder.EtIsBin.getText().toString());

                                    holder.TvLotID2.setText(holder.EtRegister.getText().toString());
                                    holder.TvFromBinID2.setText(holder.EtIsBin.getText().toString());
                                    holder.TvToBinID2.setText(strCartBinPort);

                                    holder.TvCartID3.setText(strCartId);
                                    holder.TvFromBinID3.setText(holder.EtIsBin.getText().toString());
                                    holder.TvToBinID3.setText(dtCartInfo.Rows.get(0).getValue("CART_FIX_PORT").toString());

                                    //跳至下一個流程任務
                                    iStep = 0;
                                    switch (strCurrentTask){
                                        case "CartStockOut":
                                            iStep = 1;
                                            strToPort = holder.TvToBinID1.getText().toString();
                                            strFromPort = holder.TvFromBinID1.getText().toString();
                                            strObject = holder.TvCartID1.getText().toString();
                                            break;
                                        case "CartRegisterBind":
                                            iStep = 2;
                                            strFromPort = holder.TvFromBinID2.getText().toString();
                                            strToPort = holder.TvToBinID2.getText().toString();
                                            strObject = holder.TvLotID2.getText().toString();
                                            break;
                                        case "CartStockIn":
                                            iStep = 0;
                                            strFromPort = holder.TvFromBinID3.getText().toString();
                                            strToPort = holder.TvToBinID3.getText().toString();
                                            strObject = holder.TvCartID3.getText().toString();
                                            break;
                                        default:
                                            iStep = 0;
                                            strCurrentTask = "CartStockOut";
                                            strFromPort = holder.TvFromBinID1.getText().toString();
                                            strToPort = holder.TvToBinID1.getText().toString();
                                            strObject = holder.TvCartID1.getText().toString();
                                            break;
                                    }

                                    holder.Tabhost.setCurrentTab(iStep);
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void ProcessExecute()
    {
        if (dtReg == null || dtReg.Rows.size() <= 0) return;

        if (holder.EtBIN.getText().toString().equals(""))
        {
            //WAPG014002 請輸入儲位
            ShowMessage(R.string.WAPG014002);
            return;
        }

        final int itab = holder.Tabhost.getCurrentTab();
        String strRegId = holder.EtRegister.getText().toString();
        String strBestBin = holder.CmbRecommendBin.getSelectedItem().toString();
        String strHaveCarrier = "N";
        String strRegLocation = strBestBin;

        if (itab == 0)
            strCurrentTask = "CartStockOut";
        else if (itab == 1)
            strCurrentTask = "CartRegisterBind";
        else
            strCurrentTask = "CartStockIn";

        //region Set BIModule
        // BIModule
        BModuleObject bimObj = new BModuleObject();
        bimObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDANoSheetCartPortal");
        bimObj.setModuleID("BIRegisterXfrConfirm");
        bimObj.setRequestID("BIRegisterXfrConfirm");
        bimObj.params = new Vector<ParameterInfo>();

        // Input param
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIPDANoSheetCartPortalParam.RegisterId);
        param1.setParameterValue(strRegId);
        bimObj.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIPDANoSheetCartPortalParam.XfrCase);
        param2.setParameterValue("Cart");
        bimObj.params.add(param2);

        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BIPDANoSheetCartPortalParam.XfrTask);
        param3.setParameterValue(strCurrentTask);
        bimObj.params.add(param3);

        ParameterInfo param4 = new ParameterInfo();
        param4.setParameterID(BIPDANoSheetCartPortalParam.FromPort);
        param4.setParameterValue(strFromPort);
        bimObj.params.add(param4);

        ParameterInfo param5 = new ParameterInfo();
        param5.setParameterID(BIPDANoSheetCartPortalParam.ToPort);
        param5.setParameterValue(strToPort);
        bimObj.params.add(param5);

        ParameterInfo param6 = new ParameterInfo();
        param6.setParameterID(BIPDANoSheetCartPortalParam.ObjectId);
        param6.setParameterValue(strObject);
        bimObj.params.add(param6);

        ParameterInfo param7 = new ParameterInfo();
        param7.setParameterID(BIPDANoSheetCartPortalParam.StorageId);
        param7.setParameterValue(strStorageId);
        bimObj.params.add(param7);

        ParameterInfo param8 = new ParameterInfo();
        param8.setParameterID(BIPDANoSheetCartPortalParam.BestBin);
        param8.setParameterValue(holder.CmbRecommendBin.getSelectedItem().toString());
        bimObj.params.add(param8);

        ParameterInfo param9 = new ParameterInfo();
        param9.setParameterID(BIPDANoSheetCartPortalParam.HaveCarrier);
        param9.setParameterValue(strHaveCarrier);
        bimObj.params.add(param9);

        ParameterInfo param10 = new ParameterInfo();
        param10.setParameterID(BIPDANoSheetCartPortalParam.Carrier);
        param10.setParameterValue("");
        bimObj.params.add(param10);

        ParameterInfo param11 = new ParameterInfo();
        param11.setParameterID(BIPDANoSheetCartPortalParam.Block);
        param11.setParameterValue("");
        bimObj.params.add(param11);

        ParameterInfo param12 = new ParameterInfo();
        param12.setParameterID(BIPDANoSheetCartPortalParam.RegLocation);
        param12.setParameterValue(strRegLocation);
        bimObj.params.add(param12);

        ParameterInfo param13 = new ParameterInfo();
        param13.setParameterID(BIPDANoSheetCartPortalParam.ItemId);
        param13.setParameterValue(holder.EtItem.getText().toString());
        bimObj.params.add(param13);

        ParameterInfo param14 = new ParameterInfo(); // 20220812 Ikea 傳入 BinId
        param14.setParameterID(BIPDANoSheetCartPortalParam.FetchBinId);
        param14.setParameterValue(holder.EtIsBin.getText().toString());
        bimObj.params.add(param14);
        // endregion

        CallBIModule(bimObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn)
            {
                if (CheckBModuleReturnInfo(bModuleReturn))
                {
                    DataTable dt = bModuleReturn.getReturnJsonTables().get("BIRegisterXfrConfirm").get("NEXT");

                    if (dt.Rows.size() <= 0)
                    {
                        //WAPG014003 作業成功
                        Toast.makeText(getContext(), R.string.WAPG014003, Toast.LENGTH_SHORT).show();

                        if (holder.Tabhost.getCurrentTab() == 2)
                        {
                            Clear();
                        }
                        else
                        {
                            holder.Tabhost.setCurrentTab(itab+1);
                        }
                    }
                    else
                    {
                        String Next = dt.Rows.get(0).getValue("NEXT").toString();

                        if (Next.equals("CartRegisterBind"))
                        {
                            holder.Tabhost.setCurrentTab(1);
                        }
                        else if (Next.equals("CartStockIn"))
                        {
                            holder.Tabhost.setCurrentTab(2);
                        }

                        //WAPG014004 請按確認執行下一個步驟
                        ShowMessage(R.string.WAPG014004);
                    }
                }
            }
        });
    }

    private void Clear()
    {
        holder.EtRegister.setText("");
        holder.EtIsBin.setText("");
        holder.EtBIN.setText("");
        holder.EtItemName.setText("");
        holder.EtItem.setText("");
        holder.EtQty.setText("");

        lstRecommendBin = new ArrayList<>();

        ArrayAdapter<String> adapterBin = new ArrayAdapter<>(GoodInventoryNoSheetCartActivity.this, android.R.layout.simple_spinner_dropdown_item, lstRecommendBin);
        holder.CmbRecommendBin.setAdapter(adapterBin);

        holder.TvCartID1.setText("");
        holder.TvFromBinID1.setText("");
        holder.TvToBinID1.setText("");
        holder.TvLotID2.setText("");
        holder.TvFromBinID2.setText("");
        holder.TvToBinID2.setText("");
        holder.TvCartID3.setText("");
        holder.TvFromBinID3.setText("");
        holder.TvToBinID3.setText("");

        strStorageId = "";
        strFromPort = "";
        strToPort = "";
        strObject = "";
        strCurrentTask = "";

        //_dtReg = new DataTable();
        dtReg = new DataTable();
        dtRegisterXfr = new DataTable();
        dtBinID = new DataTable();
        dtCarrierBlockID = new DataTable();
        dtCartInfo = new DataTable();
    }
    //endregion
}

//ERROR CODE
//WAPG014001 請輸入批號
//WAPG014002 請輸入儲位
//WAPG014003 作業成功
//WAPG014004 請按確認執行下一個步驟
//WAPG014005 請輸入入料口