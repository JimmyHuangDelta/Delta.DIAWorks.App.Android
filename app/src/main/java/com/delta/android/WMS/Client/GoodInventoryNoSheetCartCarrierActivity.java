package com.delta.android.WMS.Client;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
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

public class GoodInventoryNoSheetCartCarrierActivity extends BaseFlowActivity {

    private ViewHolder holder = null;
    private String strStorageId;
    private String strFromPort;
    private String strToPort;
    private String strObject;
    private String strRegLocation;
    private String strCurrentTask;

    private DataTable dtReg;
    private DataTable dtRegisterXfr;
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
        EditText EtNextStep;
        ImageButton IbtnSearch;

        Spinner CmbRecommendBin;
        Spinner CmbRecommendCarrier;
        Spinner CmbRecommendBlock;
        Button BtnRecommend;

        Button BtnConfirm;
        TextView TvCartID;
        TextView TvCarrierID;
        TextView TvLotID;
        TextView TvBlockID;
        TextView TvFromBinID;
        TextView TvToBinID;
        LinearLayout llCart;
        LinearLayout llCarrier;
        LinearLayout llLot;
        LinearLayout llBlock;
        LinearLayout llFromBin;
        LinearLayout llToBin;
    }

    ArrayList<String> lstRecommendBin = null;
    ArrayList<String> lstRecommendCarrier = null;
    ArrayList<String> lstRecommendBlock = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_good_inventory_no_sheet_cart_carrier);

        InitialSetup();

        // 設定監聽事件
        setListensers();
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

            if(holder.CmbRecommendBin.getSelectedItem().toString().equals("") || holder.CmbRecommendCarrier.getSelectedItem().toString().equals("") || holder.CmbRecommendBlock.getSelectedItem().toString().equals(""))
                return;

            holder.CmbRecommendBin.setEnabled(false);
            holder.CmbRecommendCarrier.setEnabled(false);
            holder.CmbRecommendBlock.setEnabled(false);

            //Cart Info
            BModuleObject bimObj = new BModuleObject();
            bimObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
            bimObj.setModuleID("BIFetchCarrierToCart");
            bimObj.setRequestID("BIFetchCarrierToCart");
            bimObj.params = new Vector<ParameterInfo>();

            final HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
            List<Condition> lstCondition = new ArrayList<Condition>();
            Condition cond = new Condition();
            cond.setAliasTable("C");
            cond.setColumnName("LOCATION");
            cond.setValue(holder.CmbRecommendBin.getSelectedItem().toString());
            cond.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
            lstCondition.add(cond);
            mapCondition.put("LOCATION", lstCondition);

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
                        dtCartInfo = bModuleReturn.getReturnJsonTables().get("BIFetchCarrierToCart").get("CART");
                        String strCartId = dtCartInfo.Rows.get(0).getValue("CART_ID").toString();
                        String strCartBinPort = dtCartInfo.Rows.get(0).getValue("CART_BIN_PORT").toString();
                        String strCartFixPort = dtCartInfo.Rows.get(0).getValue("CART_FIX_PORT").toString();
                        String strCarrFixPort = dtCartInfo.Rows.get(0).getValue("CARR_FIX_PORT").toString();

                        //跳至下一個流程任務
                        switch (strCurrentTask){
                            case "CartStockOut":
                                strCurrentTask = "CarrierStockOut";

                                holder.EtNextStep.setText(strCurrentTask);
                                holder.llCart.setVisibility(View.GONE);
                                holder.llCarrier.setVisibility(View.VISIBLE);
                                holder.llLot.setVisibility(View.GONE);
                                holder.llBlock.setVisibility(View.GONE);
                                holder.llFromBin.setVisibility(View.VISIBLE);
                                holder.llToBin.setVisibility(View.VISIBLE);

                                holder.TvCarrierID.setText(holder.CmbRecommendCarrier.getSelectedItem().toString());
                                holder.TvFromBinID.setText(strCarrFixPort);
                                holder.TvToBinID.setText(holder.EtIsBin.getText().toString());

                                strToPort = holder.TvToBinID.getText().toString();
                                strFromPort = holder.TvFromBinID.getText().toString();
                                strObject = holder.TvCarrierID.getText().toString();
                                break;
                            case "CarrierStockOut":
                                strCurrentTask = "CarrierRegisterBind";

                                holder.EtNextStep.setText(strCurrentTask);
                                holder.llCart.setVisibility(View.GONE);
                                holder.llCarrier.setVisibility(View.GONE);
                                holder.llLot.setVisibility(View.VISIBLE);
                                holder.llBlock.setVisibility(View.VISIBLE);
                                holder.llFromBin.setVisibility(View.VISIBLE);
                                holder.llToBin.setVisibility(View.VISIBLE);

                                holder.TvLotID.setText(holder.EtRegister.getText().toString());
                                holder.TvBlockID.setText(holder.CmbRecommendBlock.getSelectedItem().toString());
                                holder.TvFromBinID.setText(holder.EtIsBin.getText().toString());
                                holder.TvToBinID.setText(holder.EtIsBin.getText().toString());

                                strToPort = holder.TvToBinID.getText().toString();
                                strFromPort = holder.TvFromBinID.getText().toString();
                                strObject = holder.TvLotID.getText().toString();
                                strRegLocation = holder.TvBlockID.getText().toString();
                                break;
                            case "CarrierRegisterBind":
                                strCurrentTask = "CarrierStockIn";

                                holder.EtNextStep.setText(strCurrentTask);
                                holder.llCart.setVisibility(View.GONE);
                                holder.llCarrier.setVisibility(View.VISIBLE);
                                holder.llLot.setVisibility(View.GONE);
                                holder.llBlock.setVisibility(View.GONE);
                                holder.llFromBin.setVisibility(View.VISIBLE);
                                holder.llToBin.setVisibility(View.VISIBLE);

                                holder.TvCarrierID.setText(holder.CmbRecommendCarrier.getSelectedItem().toString());
                                holder.TvFromBinID.setText(holder.EtIsBin.getText().toString());
                                holder.TvToBinID.setText(strCarrFixPort);

                                strFromPort = holder.TvFromBinID.getText().toString();
                                strToPort = holder.TvToBinID.getText().toString();
                                strObject = holder.TvCarrierID.getText().toString();
                                break;
                            case "CarrierStockIn":
                                strCurrentTask = "CartStockIn";

                                holder.EtNextStep.setText(strCurrentTask);
                                holder.llCart.setVisibility(View.VISIBLE);
                                holder.llCarrier.setVisibility(View.GONE);
                                holder.llLot.setVisibility(View.GONE);
                                holder.llBlock.setVisibility(View.GONE);
                                holder.llFromBin.setVisibility(View.VISIBLE);
                                holder.llToBin.setVisibility(View.VISIBLE);

                                holder.TvCartID.setText(strCartId);
                                holder.TvFromBinID.setText(holder.EtIsBin.getText().toString());
                                holder.TvToBinID.setText(strCarrFixPort);

                                strFromPort = holder.TvFromBinID.getText().toString();
                                strToPort = holder.TvToBinID.getText().toString();
                                strObject = holder.TvCartID.getText().toString();
                                break;
                            case "CartStockIn":
                                break;
                            default:
                                strCurrentTask = "CartStockOut";

                                holder.EtNextStep.setText(strCurrentTask);
                                holder.llCart.setVisibility(View.VISIBLE);
                                holder.llCarrier.setVisibility(View.GONE);
                                holder.llLot.setVisibility(View.GONE);
                                holder.llBlock.setVisibility(View.GONE);
                                holder.llFromBin.setVisibility(View.VISIBLE);
                                holder.llToBin.setVisibility(View.VISIBLE);

                                holder.TvCartID.setText(strCartId);
                                holder.TvFromBinID.setText(strCartFixPort);
                                holder.TvToBinID.setText(holder.EtIsBin.getText().toString());

                                strFromPort = holder.TvFromBinID.getText().toString();
                                strToPort = holder.TvToBinID.getText().toString();
                                strObject = holder.TvCartID.getText().toString();
                                break;
                        }
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
    //endregion

    //region 方法
    private void InitialSetup() {
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
        holder.CmbRecommendCarrier = findViewById(R.id.cmbRecommendCarrier);
        holder.CmbRecommendBlock = findViewById(R.id.cmbRecommendBlock);
        holder.BtnRecommend = findViewById(R.id.btnRecommend);

        holder.BtnConfirm = findViewById(R.id.btnConfirm);
        holder.TvCartID = findViewById(R.id.tvCartID);
        holder.TvFromBinID = findViewById(R.id.tvFromBinID);
        holder.TvToBinID = findViewById(R.id.tvToBinID);
        holder.TvCarrierID = findViewById(R.id.tvCarrierID);
        holder.TvLotID = findViewById(R.id.tvLotID);
        holder.TvBlockID = findViewById(R.id.tvBlockID);
        holder.EtNextStep = findViewById(R.id.etNextStep);
        holder.llCart = findViewById(R.id.llCart);
        holder.llCarrier = findViewById(R.id.llCarrier);
        holder.llLot = findViewById(R.id.llLot);
        holder.llBlock = findViewById(R.id.llBlock);
        holder.llFromBin = findViewById(R.id.llFromBin);
        holder.llToBin = findViewById(R.id.llToBin);
    }

    private void setListensers() {
        holder.IbtnSearch.setOnClickListener(IbtnSearchOnClick);
        holder.BtnRecommend.setOnClickListener(lsRecommend);
        holder.BtnConfirm.setOnClickListener(BtnConfirmOnClick);
    }

    private void GetData() {
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
        param2.setParameterValue("CartCarrier");
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
                    //_dtReg = bModuleReturn.getReturnJsonTables().get("BIGetRegisterXfr").get("PDA");
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
                        ArrayAdapter<String> adapterBin = new ArrayAdapter<>(GoodInventoryNoSheetCartCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, lstRecommendBin);
                        holder.CmbRecommendBin.setAdapter(adapterBin);

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
                            ArrayAdapter<String> adapterCarrier = new ArrayAdapter<>(GoodInventoryNoSheetCartCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, lstRecommendCarrier);
                            holder.CmbRecommendCarrier.setAdapter(adapterCarrier);

                            holder.CmbRecommendCarrier.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l){
                                    ArrayAdapter<String> adapterBlock = new ArrayAdapter<>(GoodInventoryNoSheetCartCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, mapBlock.get(holder.CmbRecommendCarrier.getSelectedItem().toString()));
                                    holder.CmbRecommendBlock.setAdapter(adapterBlock);
                                }
                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView){
                                    ArrayAdapter<String> adapterBlock = new ArrayAdapter<>(GoodInventoryNoSheetCartCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, mapBlock.get(holder.CmbRecommendCarrier.getSelectedItem().toString()));
                                    holder.CmbRecommendBlock.setAdapter(adapterBlock);
                                }
                            });

                            ArrayAdapter<String> adapterBlock = new ArrayAdapter<>(GoodInventoryNoSheetCartCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, mapBlock.get(holder.CmbRecommendCarrier.getSelectedItem().toString()));
                            holder.CmbRecommendBlock.setAdapter(adapterBlock);
                        }
                    }
                    else
                    {
                        lstRecommendBin = new ArrayList<String>();
                        lstRecommendBin.add(strTmpBestBin);
                        ArrayAdapter<String> adapterBin = new ArrayAdapter<>(GoodInventoryNoSheetCartCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, lstRecommendBin);
                        holder.CmbRecommendBin.setAdapter(adapterBin);
                        holder.CmbRecommendBin.setSelection(0);

                        lstRecommendCarrier = new ArrayList<String>();
                        lstRecommendCarrier.add(strTmpCarrier);
                        ArrayAdapter<String> adapterCarrier = new ArrayAdapter<>(GoodInventoryNoSheetCartCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, lstRecommendCarrier);
                        holder.CmbRecommendCarrier.setAdapter(adapterCarrier);
                        holder.CmbRecommendCarrier.setSelection(0);

                        lstRecommendBlock = new ArrayList<String>();
                        lstRecommendBlock.add(strTmpBlock);
                        ArrayAdapter<String> adapterBlock = new ArrayAdapter<>(GoodInventoryNoSheetCartCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, lstRecommendBlock);
                        holder.CmbRecommendBlock.setAdapter(adapterBlock);
                        holder.CmbRecommendCarrier.setSelection(0);

                        holder.CmbRecommendBin.setEnabled(false);
                        holder.CmbRecommendCarrier.setEnabled(false);
                        holder.CmbRecommendBlock.setEnabled(false);

                        //Cart Info
                        BModuleObject bimObj = new BModuleObject();
                        bimObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
                        bimObj.setModuleID("BIFetchCarrierToCart");
                        bimObj.setRequestID("BIFetchCarrierToCart");
                        bimObj.params = new Vector<ParameterInfo>();

                        final HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
                        List<Condition> lstCondition = new ArrayList<Condition>();
                        Condition cond = new Condition();

                        if (!strTmpCarrier.isEmpty()) {
                            cond.setAliasTable("C");
                            cond.setColumnName("CARRIER_ID");
                            cond.setValue(strTmpCarrier);
                        }
                        else
                        {
                            cond.setAliasTable("C");
                            cond.setColumnName("LOCATION");
                            cond.setValue(holder.CmbRecommendBin.getSelectedItem().toString());
                        }

                        cond.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
                        lstCondition.add(cond);
                        mapCondition.put(cond.getColumnName(), lstCondition);

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
                                    dtCartInfo = bModuleReturn.getReturnJsonTables().get("BIFetchCarrierToCart").get("CART");
                                    String strCartId = dtCartInfo.Rows.get(0).getValue("CART_ID").toString();
                                    String strCartBinPort = dtCartInfo.Rows.get(0).getValue("CART_BIN_PORT").toString();
                                    String strCartFixPort = dtCartInfo.Rows.get(0).getValue("CART_FIX_PORT").toString();
                                    String strCarrFixPort = dtCartInfo.Rows.get(0).getValue("CARR_FIX_PORT").toString();

                                    //跳至下一個流程任務
                                    switch (strCurrentTask){
                                        case "CartStockOut":
                                            strCurrentTask = "CarrierStockOut";

                                            holder.EtNextStep.setText(strCurrentTask);
                                            holder.llCart.setVisibility(View.GONE);
                                            holder.llCarrier.setVisibility(View.VISIBLE);
                                            holder.llLot.setVisibility(View.GONE);
                                            holder.llBlock.setVisibility(View.GONE);
                                            holder.llFromBin.setVisibility(View.VISIBLE);
                                            holder.llToBin.setVisibility(View.VISIBLE);

                                            holder.TvCarrierID.setText(holder.CmbRecommendCarrier.getSelectedItem().toString());
                                            holder.TvFromBinID.setText(strCarrFixPort);
                                            holder.TvToBinID.setText(holder.EtIsBin.getText().toString());

                                            strToPort = holder.TvToBinID.getText().toString();
                                            strFromPort = holder.TvFromBinID.getText().toString();
                                            strObject = holder.TvCarrierID.getText().toString();
                                            break;
                                        case "CarrierStockOut":
                                            strCurrentTask = "CarrierRegisterBind";

                                            holder.EtNextStep.setText(strCurrentTask);
                                            holder.llCart.setVisibility(View.GONE);
                                            holder.llCarrier.setVisibility(View.GONE);
                                            holder.llLot.setVisibility(View.VISIBLE);
                                            holder.llBlock.setVisibility(View.VISIBLE);
                                            holder.llFromBin.setVisibility(View.VISIBLE);
                                            holder.llToBin.setVisibility(View.VISIBLE);

                                            holder.TvLotID.setText(holder.EtRegister.getText().toString());
                                            holder.TvBlockID.setText(holder.CmbRecommendBlock.getSelectedItem().toString());
                                            holder.TvFromBinID.setText(holder.EtIsBin.getText().toString());
                                            holder.TvToBinID.setText(holder.EtIsBin.getText().toString());

                                            strToPort = holder.TvToBinID.getText().toString();
                                            strFromPort = holder.TvFromBinID.getText().toString();
                                            strObject = holder.TvLotID.getText().toString();
                                            strRegLocation = holder.TvBlockID.getText().toString();
                                            break;
                                        case "CarrierRegisterBind":
                                            strCurrentTask = "CarrierStockIn";

                                            holder.EtNextStep.setText(strCurrentTask);
                                            holder.llCart.setVisibility(View.GONE);
                                            holder.llCarrier.setVisibility(View.VISIBLE);
                                            holder.llLot.setVisibility(View.GONE);
                                            holder.llBlock.setVisibility(View.GONE);
                                            holder.llFromBin.setVisibility(View.VISIBLE);
                                            holder.llToBin.setVisibility(View.VISIBLE);

                                            holder.TvCarrierID.setText(holder.CmbRecommendCarrier.getSelectedItem().toString());
                                            holder.TvFromBinID.setText(holder.EtIsBin.getText().toString());
                                            holder.TvToBinID.setText(strCarrFixPort);

                                            strFromPort = holder.TvFromBinID.getText().toString();
                                            strToPort = holder.TvToBinID.getText().toString();
                                            strObject = holder.TvCarrierID.getText().toString();
                                            break;
                                        case "CarrierStockIn":
                                            strCurrentTask = "CartStockIn";

                                            holder.EtNextStep.setText(strCurrentTask);
                                            holder.llCart.setVisibility(View.VISIBLE);
                                            holder.llCarrier.setVisibility(View.GONE);
                                            holder.llLot.setVisibility(View.GONE);
                                            holder.llBlock.setVisibility(View.GONE);
                                            holder.llFromBin.setVisibility(View.VISIBLE);
                                            holder.llToBin.setVisibility(View.VISIBLE);

                                            holder.TvCartID.setText(strCartId);
                                            holder.TvFromBinID.setText(holder.EtIsBin.getText().toString());
                                            holder.TvToBinID.setText(strCartFixPort);

                                            strFromPort = holder.TvFromBinID.getText().toString();
                                            strToPort = holder.TvToBinID.getText().toString();
                                            strObject = holder.TvCartID.getText().toString();
                                            break;
                                        case "CartStockIn":
                                            break;
                                        default:
                                            strCurrentTask = "CartStockOut";

                                            holder.EtNextStep.setText(strCurrentTask);
                                            holder.llCart.setVisibility(View.VISIBLE);
                                            holder.llCarrier.setVisibility(View.GONE);
                                            holder.llLot.setVisibility(View.GONE);
                                            holder.llBlock.setVisibility(View.GONE);
                                            holder.llFromBin.setVisibility(View.VISIBLE);
                                            holder.llToBin.setVisibility(View.VISIBLE);

                                            holder.TvCartID.setText(strCartId);
                                            holder.TvFromBinID.setText(strCarrFixPort);
                                            holder.TvToBinID.setText(holder.EtIsBin.getText().toString());

                                            strFromPort = holder.TvFromBinID.getText().toString();
                                            strToPort = holder.TvToBinID.getText().toString();
                                            strObject = holder.TvCartID.getText().toString();
                                            break;
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void ProcessExecute() {
        if(dtReg == null || dtReg.Rows.size() <= 0) return;

        if (holder.EtBIN.getText().toString().equals(""))
        {
            //WAPG014002 請輸入儲位
            ShowMessage(R.string.WAPG014002);
            return;
        }

        final String strStep = holder.EtNextStep.getText().toString();
        String strRegId = holder.EtRegister.getText().toString();
        String strBestBin = holder.CmbRecommendBin.getSelectedItem().toString();
        String strHaveCarrier = "Y";
        String strCarrier = holder.CmbRecommendCarrier.getSelectedItem().toString();
        String strBlock = holder.CmbRecommendBlock.getSelectedItem().toString();
        strRegLocation = "0";


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
        param2.setParameterValue("CartCarrier");
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
        param8.setParameterValue(strBestBin);
        bimObj.params.add(param8);

        ParameterInfo param9 = new ParameterInfo();
        param9.setParameterID(BIPDANoSheetCartPortalParam.HaveCarrier);
        param9.setParameterValue(strHaveCarrier);
        bimObj.params.add(param9);

        ParameterInfo param10 = new ParameterInfo();
        param10.setParameterID(BIPDANoSheetCartPortalParam.Carrier);
        param10.setParameterValue(strCarrier);
        bimObj.params.add(param10);

        ParameterInfo param11 = new ParameterInfo();
        param11.setParameterID(BIPDANoSheetCartPortalParam.Block);
        param11.setParameterValue(strBlock);
        bimObj.params.add(param11);

        ParameterInfo param12 = new ParameterInfo();
        param12.setParameterID(BIPDANoSheetCartPortalParam.RegLocation);
        param12.setParameterValue(strRegLocation);
        bimObj.params.add(param12);

        ParameterInfo param13 = new ParameterInfo();  // 20220812 Ikea 傳入 ItemId
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

                    String strCartId = dtCartInfo.Rows.get(0).getValue("CART_ID").toString();
                    String strCartBinPort = dtCartInfo.Rows.get(0).getValue("CART_BIN_PORT").toString();
                    String strCartFixPort = dtCartInfo.Rows.get(0).getValue("CART_FIX_PORT").toString();
                    String strCarrFixPort = dtCartInfo.Rows.get(0).getValue("CARR_FIX_PORT").toString();

                    if (dt.Rows.size() <= 0)
                    {
                        //WAPG014003 作業成功
                        Toast.makeText(getContext(), R.string.WAPG014003, Toast.LENGTH_SHORT).show();

                        if(strCurrentTask.equals("CartStockIn"))
                        {
                            Clear();
                        }
                        else
                        {
                            switch(strCurrentTask)
                            {
                                case "CartStockOut":
                                    strCurrentTask = "CarrierStockOut";
                                    holder.EtNextStep.setText(strCurrentTask);
                                    holder.llCart.setVisibility(View.GONE);
                                    holder.llCarrier.setVisibility(View.VISIBLE);
                                    holder.llLot.setVisibility(View.GONE);
                                    holder.llBlock.setVisibility(View.GONE);
                                    holder.llFromBin.setVisibility(View.VISIBLE);
                                    holder.llToBin.setVisibility(View.VISIBLE);

                                    holder.TvCarrierID.setText(holder.CmbRecommendCarrier.getSelectedItem().toString());
                                    holder.TvFromBinID.setText(strCarrFixPort);
                                    holder.TvToBinID.setText(holder.EtIsBin.getText().toString());

                                    strFromPort = holder.TvFromBinID.getText().toString();
                                    strToPort = holder.TvToBinID.getText().toString();
                                    strObject = holder.TvCarrierID.getText().toString();
                                    break;
                                case "CarrierStockOut":
                                    strCurrentTask = "CarrierRegisterBind";
                                    holder.EtNextStep.setText(strCurrentTask);
                                    holder.llCart.setVisibility(View.GONE);
                                    holder.llCarrier.setVisibility(View.GONE);
                                    holder.llLot.setVisibility(View.VISIBLE);
                                    holder.llBlock.setVisibility(View.VISIBLE);
                                    holder.llFromBin.setVisibility(View.VISIBLE);
                                    holder.llToBin.setVisibility(View.VISIBLE);

                                    holder.TvLotID.setText(holder.EtRegister.getText().toString());
                                    holder.TvBlockID.setText(holder.CmbRecommendBlock.getSelectedItem().toString());
                                    holder.TvFromBinID.setText(holder.EtIsBin.getText().toString());
                                    holder.TvToBinID.setText(holder.EtIsBin.getText().toString());

                                    strFromPort = holder.TvFromBinID.getText().toString();
                                    strToPort = holder.TvToBinID.getText().toString();
                                    strObject = holder.TvLotID.getText().toString();
                                    strRegLocation = holder.TvBlockID.getText().toString();
                                    break;
                                case "CarrierRegisterBind":
                                    strCurrentTask = "CarrierStockIn";
                                    holder.EtNextStep.setText(strCurrentTask);
                                    holder.llCart.setVisibility(View.GONE);
                                    holder.llCarrier.setVisibility(View.VISIBLE);
                                    holder.llLot.setVisibility(View.GONE);
                                    holder.llBlock.setVisibility(View.GONE);
                                    holder.llFromBin.setVisibility(View.VISIBLE);
                                    holder.llToBin.setVisibility(View.VISIBLE);

                                    holder.TvCarrierID.setText(holder.CmbRecommendCarrier.getSelectedItem().toString());
                                    holder.TvFromBinID.setText(holder.EtIsBin.getText().toString());
                                    holder.TvToBinID.setText(strCarrFixPort);

                                    strFromPort = holder.TvFromBinID.getText().toString();
                                    strToPort = holder.TvToBinID.getText().toString();
                                    strObject = holder.TvCarrierID.getText().toString();
                                    break;

                                case "CarrierStockIn":
                                    strCurrentTask = "CartStockIn";
                                    holder.EtNextStep.setText(strCurrentTask);
                                    holder.llCart.setVisibility(View.VISIBLE);
                                    holder.llCarrier.setVisibility(View.GONE);
                                    holder.llLot.setVisibility(View.GONE);
                                    holder.llBlock.setVisibility(View.GONE);
                                    holder.llFromBin.setVisibility(View.VISIBLE);
                                    holder.llToBin.setVisibility(View.VISIBLE);

                                    holder.TvCartID.setText(strCartId);
                                    holder.TvFromBinID.setText(holder.EtIsBin.getText().toString());
                                    holder.TvToBinID.setText(strCartFixPort);

                                    strFromPort = holder.TvFromBinID.getText().toString();
                                    strToPort = holder.TvToBinID.getText().toString();
                                    strObject = holder.TvCartID.getText().toString();
                                    break;
                            }
                        }
                    }
                    else
                    {
                        String Next = dt.Rows.get(0).getValue("NEXT").toString();

                        switch (Next)
                        {
                            case "CartStockOut":
                                strCurrentTask = "CartStockOut";
                                holder.EtNextStep.setText(strCurrentTask);
                                holder.llCart.setVisibility(View.VISIBLE);
                                holder.llCarrier.setVisibility(View.GONE);
                                holder.llLot.setVisibility(View.GONE);
                                holder.llBlock.setVisibility(View.GONE);
                                holder.llFromBin.setVisibility(View.VISIBLE);
                                holder.llToBin.setVisibility(View.VISIBLE);

                                holder.TvCartID.setText(strCartId);
                                holder.TvFromBinID.setText(strCartFixPort);
                                holder.TvToBinID.setText(holder.EtIsBin.getText().toString());

                                strFromPort = holder.TvFromBinID.getText().toString();
                                strToPort = holder.TvToBinID.getText().toString();
                                strObject = holder.TvCartID.getText().toString();
                                break;
                            case "CarrierStockOut":
                                strCurrentTask = "CarrierStockOut";
                                holder.EtNextStep.setText(strCurrentTask);
                                holder.llCart.setVisibility(View.GONE);
                                holder.llCarrier.setVisibility(View.VISIBLE);
                                holder.llLot.setVisibility(View.GONE);
                                holder.llBlock.setVisibility(View.GONE);
                                holder.llFromBin.setVisibility(View.VISIBLE);
                                holder.llToBin.setVisibility(View.VISIBLE);

                                holder.TvCarrierID.setText(holder.CmbRecommendCarrier.getSelectedItem().toString());
                                holder.TvFromBinID.setText(strCarrFixPort);
                                holder.TvToBinID.setText(holder.EtIsBin.getText().toString());

                                strFromPort = holder.TvFromBinID.getText().toString();
                                strToPort = holder.TvToBinID.getText().toString();
                                strObject = holder.TvCarrierID.getText().toString();
                                break;
                            case "CarrierRegisterBind":
                                strCurrentTask = "CarrierRegisterBind";
                                holder.EtNextStep.setText(strCurrentTask);
                                holder.llCart.setVisibility(View.GONE);
                                holder.llCarrier.setVisibility(View.GONE);
                                holder.llLot.setVisibility(View.VISIBLE);
                                holder.llBlock.setVisibility(View.VISIBLE);
                                holder.llFromBin.setVisibility(View.VISIBLE);
                                holder.llToBin.setVisibility(View.VISIBLE);

                                holder.TvLotID.setText(holder.EtRegister.getText().toString());
                                holder.TvBlockID.setText(holder.CmbRecommendBlock.getSelectedItem().toString());
                                holder.TvFromBinID.setText(holder.EtIsBin.getText().toString());
                                holder.TvToBinID.setText(holder.EtIsBin.getText().toString());

                                strFromPort = holder.TvFromBinID.getText().toString();
                                strToPort = holder.TvToBinID.getText().toString();
                                strObject = holder.TvLotID.getText().toString();
                                strRegLocation = holder.TvBlockID.getText().toString();
                                break;
                            case "CarrierStockIn":
                                strCurrentTask = "CarrierStockIn";
                                holder.EtNextStep.setText(strCurrentTask);
                                holder.llCart.setVisibility(View.GONE);
                                holder.llCarrier.setVisibility(View.VISIBLE);
                                holder.llLot.setVisibility(View.GONE);
                                holder.llBlock.setVisibility(View.GONE);
                                holder.llFromBin.setVisibility(View.VISIBLE);
                                holder.llToBin.setVisibility(View.VISIBLE);

                                holder.TvCarrierID.setText(holder.CmbRecommendCarrier.getSelectedItem().toString());
                                holder.TvFromBinID.setText(holder.EtIsBin.getText().toString());
                                holder.TvToBinID.setText(strCarrFixPort);

                                strFromPort = holder.TvFromBinID.getText().toString();
                                strToPort = holder.TvToBinID.getText().toString();
                                strObject = holder.TvCarrierID.getText().toString();
                                break;
                            case "CartStockIn":
                                strCurrentTask = "CartStockIn";
                                holder.EtNextStep.setText(strCurrentTask);
                                holder.llCart.setVisibility(View.VISIBLE);
                                holder.llCarrier.setVisibility(View.GONE);
                                holder.llLot.setVisibility(View.GONE);
                                holder.llBlock.setVisibility(View.GONE);
                                holder.llFromBin.setVisibility(View.VISIBLE);
                                holder.llToBin.setVisibility(View.VISIBLE);

                                holder.TvCartID.setText(strCartId);
                                holder.TvFromBinID.setText(holder.EtIsBin.getText().toString());
                                holder.TvToBinID.setText(strCartFixPort);

                                strFromPort = holder.TvFromBinID.getText().toString();
                                strToPort = holder.TvToBinID.getText().toString();
                                strObject = holder.TvCartID.getText().toString();
                                break;
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
        lstRecommendCarrier = new ArrayList<>();
        lstRecommendBlock = new ArrayList<>();

        ArrayAdapter<String> adapterBin = new ArrayAdapter<>(GoodInventoryNoSheetCartCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, lstRecommendBin);
        holder.CmbRecommendBin.setAdapter(adapterBin);
        ArrayAdapter<String> adapterCarrier = new ArrayAdapter<>(GoodInventoryNoSheetCartCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, lstRecommendCarrier);
        holder.CmbRecommendCarrier.setAdapter(adapterCarrier);
        ArrayAdapter<String> adapterBlock = new ArrayAdapter<>(GoodInventoryNoSheetCartCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, lstRecommendBlock);
        holder.CmbRecommendBlock.setAdapter(adapterBlock);

        holder.EtNextStep.setText("");
        holder.TvCartID.setText("");
        holder.TvCarrierID.setText("");
        holder.TvLotID.setText("");
        holder.TvBlockID.setText("");
        holder.TvFromBinID.setText("");
        holder.TvToBinID.setText("");

        holder.llCart.setVisibility(View.VISIBLE);
        holder.llCarrier.setVisibility(View.VISIBLE);
        holder.llLot.setVisibility(View.VISIBLE);
        holder.llBlock.setVisibility(View.VISIBLE);
        holder.llFromBin.setVisibility(View.VISIBLE);
        holder.llToBin.setVisibility(View.VISIBLE);

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
