package com.delta.android.WMS.Client;

import android.content.Context;
import android.os.strictmode.CleartextNetworkViolation;
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
import android.widget.Toast;

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
import com.delta.android.WMS.Param.BIPDANoSheetCartPortalParam;
import com.delta.android.WMS.Param.BIPDANoSheetPickCartPortalParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.ParamObj.Condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import static com.delta.android.Core.Common.Global.getContext;

public class GoodPickNonSheetCartActivity extends BaseFlowActivity {

    private ViewHolder holder = null;
    private DataTable _dtFReg; //記錄查詢Register的資料
    private DataTable _dtReg; //記錄欲揀料Register的資料
    private DataTable dtMaster;
    private DataTable dtDetail;

    private DataTable dtConfigCond = null;
    private DataTable dtConfigSort = null;
    private String strConfigCond = null;
    private String strConfigSort = null;
    private String _strSraechId = ""; //最終處理的單據

    ArrayList<String> lstSeq = null;
    HashMap<String, String> mapSeq = new HashMap<String, String>();
    ArrayList<String> lstSheetId = new ArrayList<>();

    static class ViewHolder
    {
        TabHost tabHost;
        //EditText etLotId;
//        EditText etSheetId;
        Spinner cmbSheetId;
        ImageButton ibtnSheetId;
        Spinner cmbSeq;
        EditText etItemId;
        EditText etSelectLotId;
        EditText etQty;
        ImageButton ibtnQtySearch;
        ListView lvRegister;
        //ImageButton ibtnLotSearch;
        //ImageButton ibtnItemSearch;
        Button btnConfirm;

        TextView tvCart;
        TextView tvFromPort;
        TextView tvToPort;
        TextView tvLot2;
        TextView tvFromPort2;
        TextView tvToPort2;
        TextView tvCart3;
        TextView tvFromPort3;
        TextView tvToPort3;
        int index = 0;
        String strStorageId;
        String strIsPort;
        String strOsPort;
        String strBinPort;
        String strCurrentTask;
        int iStep;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_good_pick_non_sheet_cart);

        InitialSetup();

        // 設定監聽事件
        setListensers();

        InitTabHost();

        // 載入單據代號至下拉選單
        GetSheetId();
    }

    //region 事件
    /*
    private View.OnClickListener ibtnLotSearchClick = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            if(holder.etLotId.getText().toString().equals("")){
                ShowMessage(R.string.WAPG016001); //WAPG016001 請輸入批號
                return;
            }

            if(holder.etQty.getText().toString().equals("")){
                ShowMessage(R.string.WAPG016003); //WAPG016003 請輸入數量
                return;
            }

            FetchLotInfo("LOT", holder.etLotId.getText().toString(), holder.etQty.getText().toString());
        }
    };
    */

    /*
    private View.OnClickListener ibtnItemSearchClick = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            if(holder.etItemId.getText().toString().equals("")){
                ShowMessage(R.string.WAPG016002); //WAPG016002 請輸入料號
                return;
            }

            if(holder.etQty.getText().toString().equals("")){
                ShowMessage(R.string.WAPG016003); //WAPG016003 請輸入數量
                return;
            }

            FetchLotInfo("ITEM", holder.etItemId.getText().toString(), holder.etQty.getText().toString());
        }
    };
    */

    private AdapterView.OnItemClickListener lvRegisterClick = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            holder.etSelectLotId.setText(_dtFReg.Rows.get(position).getValue("REGISTER_ID").toString());
            GetData();
        }
    };

    private AdapterView.OnClickListener btnConfirm = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            ProcessExecute();
        }
    };

    private TabHost.OnTabChangeListener TabChange = new TabHost.OnTabChangeListener() {
        @Override
        public void onTabChanged(String tabId)
        {
            if (_dtReg == null || _dtReg.Rows.size() <= 0) return;

            switch (tabId)
            {
                case "1":
                    holder.tvCart.setText(_dtReg.Rows.get(0).getValue("CART_ID").toString());
                    holder.tvFromPort.setText(_dtReg.Rows.get(0).getValue("CART_PORT").toString());
                    holder.tvToPort.setText(_dtReg.Rows.get(0).getValue("OS_PORT").toString());
                    break;

                case "2":
                    holder.tvLot2.setText(_dtReg.Rows.get(0).getValue("LOT_ID").toString());
                    holder.tvFromPort2.setText(_dtReg.Rows.get(0).getValue("CART_BIN_PORT").toString());
                    holder.tvToPort2.setText(_dtReg.Rows.get(0).getValue("OS_PORT").toString());
                    break;

                case "3":
                    holder.tvCart3.setText(_dtReg.Rows.get(0).getValue("CART_ID").toString());
                    holder.tvFromPort3.setText(_dtReg.Rows.get(0).getValue("OS_PORT").toString());
                    holder.tvToPort3.setText(_dtReg.Rows.get(0).getValue("CART_FIX_PORT").toString());
                    break;

                default:
                    break;
            }
        }
    };

    private View.OnClickListener lsSheetIdFetch = new View.OnClickListener(){
        @Override
        public void onClick(View veiw)
        {
//            if(holder.etSheetId.getText().toString().equals("")){
//                //WAPG016006 請輸入單據代碼
//                ShowMessage(R.string.WAPG016006);
//                return;
//            }

            int sheetIdIndex = holder.cmbSheetId.getSelectedItemPosition();
            if (sheetIdIndex == (lstSheetId.size() - 1)) {
                ShowMessage(R.string.WAPG016006); //WAPG021006    請選擇單據代碼
                return;
            }

            String strSheetId = holder.cmbSheetId.getSelectedItem().toString().toUpperCase().trim();

            GetSht(strSheetId);
        }
    };

    private Spinner.OnItemSelectedListener lstSeqSelected = new Spinner.OnItemSelectedListener(){
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l){
            MapSeqItem();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView){}
    };

    private View.OnClickListener lsRegisterFetch = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            if(holder.etItemId.getText().toString().equals("")){
                //WAPG016002 請輸入料號
                ShowMessage(R.string.WAPG016002);
                return;
            }

            String strStorageId = "";

            if (dtDetail.Rows.size() > 0)
            {
                for (DataRow dr : dtDetail.Rows)
                {
                    if (Double.parseDouble(dr.getValue("SEQ").toString()) == Double.parseDouble(holder.cmbSeq.getSelectedItem().toString()))
                    {
                        strStorageId = dr.getValue("FROM_STORAGE_ID").toString();
                    }
                }
            }

            FetchLotInfo("ITEM", holder.etItemId.getText().toString(), holder.etQty.getText().toString(), strStorageId);
        }
    };
    //endregion

    //region '方法
    private void InitialSetup()
    {
        if (holder != null) return;

        holder = new ViewHolder();
        holder.tabHost = findViewById(R.id.tabHost);
//        holder.etSheetId = findViewById(R.id.etSheetId);
        holder.cmbSheetId = findViewById(R.id.cmbSheetId);
        holder.ibtnSheetId = findViewById(R.id.ibtnSheetIdSearch);
        holder.cmbSeq = findViewById(R.id.cmbSeq);
        //holder.etLotId = findViewById(R.id.etLotId);
        holder.etItemId = findViewById(R.id.etItemId);
        holder.etSelectLotId = findViewById(R.id.etSelectLotId);
        holder.lvRegister = findViewById(R.id.lvRegisters);
        holder.btnConfirm = findViewById(R.id.btnConfirm);
        //holder.ibtnItemSearch = findViewById(R.id.ibtnItemSearch);
        //holder.ibtnLotSearch = findViewById(R.id.ibtnLotSearch);
        holder.etQty = findViewById(R.id.etQty);
        holder.ibtnQtySearch = findViewById(R.id.ibtnQtySearch);

        //CartOut
        holder.tvCart = findViewById(R.id.tvCartID);
        holder.tvFromPort = findViewById(R.id.tvFromPortID);
        holder.tvToPort = findViewById(R.id.tvToPortID);

        //CartRegisterUnBind
        holder.tvLot2 = findViewById(R.id.tvLotID2);
        holder.tvFromPort2 = findViewById(R.id.tvFromPortID2);
        holder.tvToPort2 = findViewById(R.id.tvToPortID2);

        //CartIn
        holder.tvCart3 = findViewById(R.id.tvCartID3);
        holder.tvFromPort3 = findViewById(R.id.tvFromPortID3);
        holder.tvToPort3 = findViewById(R.id.tvToPortID3);

        holder.tabHost.setup();
    }

    private void setListensers()
    {
        //holder.ibtnLotSearch.setOnClickListener(ibtnLotSearchClick);
        //holder.ibtnItemSearch.setOnClickListener(ibtnItemSearchClick);
        holder.ibtnSheetId.setOnClickListener(lsSheetIdFetch);
        holder.cmbSeq.setOnItemSelectedListener(lstSeqSelected);
        holder.ibtnQtySearch.setOnClickListener(lsRegisterFetch);
        holder.lvRegister.setOnItemClickListener(lvRegisterClick);
        holder.btnConfirm.setOnClickListener(btnConfirm);
        holder.tabHost.setOnTabChangedListener(TabChange); // 切換tab時觸發事件，load畫面
    }

    private void InitTabHost()
    {
        TabHost.TabSpec spec1 = holder.tabHost.newTabSpec("1");
        View tab1 = LayoutInflater.from(this).inflate(R.layout.activity_wms_good_inventory_tab_widget, null);
        TextView tvTab1 = tab1.findViewById(R.id.tvTabText);
        tvTab1.setText(R.string.CART_MOVE_OUT);
        spec1.setIndicator(tab1);
        spec1.setContent(R.id.tabCartOut);
        holder.tabHost.addTab(spec1);

        TabHost.TabSpec spec2 = holder.tabHost.newTabSpec("2");
        View tab2 = LayoutInflater.from(this).inflate(R.layout.activity_wms_good_inventory_tab_widget, null);
        TextView tvTab2 = tab2.findViewById(R.id.tvTabText);
        tvTab2.setText(R.string.ITEM_CART_UN_BIND);
        spec2.setIndicator(tab2);
        spec2.setContent(R.id.tabCartRegisterBind);
        holder.tabHost.addTab(spec2);

        TabHost.TabSpec spec3 = holder.tabHost.newTabSpec("3");
        View tab3 = LayoutInflater.from(this).inflate(R.layout.activity_wms_good_inventory_tab_widget, null);
        TextView tvTab3 = tab3.findViewById(R.id.tvTabText);
        tvTab3.setText(R.string.CART_MOVE_IN);
        spec3.setIndicator(tab3);
        spec3.setContent(R.id.tabCartIn);
        holder.tabHost.addTab(spec3);

        for(int i = 0; i <3; i++)
        {
            holder.tabHost.getTabWidget().getChildAt(i).setClickable(false);
        }
    }

    private void FetchSheetInfo(String strSheetId){
//        String sheetId = holder.etSheetId.getText().toString();
        if (strSheetId.equals(""))
        {
            _strSraechId = holder.cmbSheetId.getSelectedItem().toString().toUpperCase().trim();
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
//        conditionSheetId.setValue(holder.etSheetId.getText().toString());
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

                    ArrayAdapter<String> adapterSeq = new ArrayAdapter<>(GoodPickNonSheetCartActivity.this, android.R.layout.simple_spinner_dropdown_item, lstSeq);
                    holder.cmbSeq.setAdapter(adapterSeq);
                }
            }
        });
    }

    private void MapSeqItem(){

        DataRow selectedRow = null;

        String seq = mapSeq.get(holder.cmbSeq.getSelectedItem().toString());
        for(DataRow dr: dtDetail.Rows) {
            if(dr.getValue("SEQ").toString().equals(seq)){
                holder.etItemId.setText(dr.getValue("ITEM_ID").toString());
                selectedRow = dr;
                break;
            }
        }
        strConfigCond = generateExtendConfigCond(selectedRow, dtConfigCond);
        strConfigSort = generateExtendConfigSort(dtConfigSort);
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

                    SimpleArrayAdapter adapter = new GoodPickNonSheetCartActivity.SimpleArrayAdapter<>(GoodPickNonSheetCartActivity.this, android.R.layout.simple_spinner_dropdown_item, lstSheetId);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    holder.cmbSheetId.setAdapter(adapter);
                    holder.cmbSheetId.setSelection(lstSheetId.size() - 1, true);
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

    private void FetchLotInfo(String type, String value, String qry, String strStorageId)
    {
        //找出Register
        BModuleObject bmObjLot = new BModuleObject();
        bmObjLot.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDANoSheetPickCartPortal");
        bmObjLot.setModuleID("BIGetRegData");
        bmObjLot.setRequestID("BIGetRegData");

        bmObjLot.params = new Vector<ParameterInfo>();

        if (type.equals("ITEM"))
        {
            ParameterInfo paramItem = new ParameterInfo();
            paramItem.setParameterID(BIPDANoSheetPickCartPortalParam.Item);
            paramItem.setParameterValue(value);
            bmObjLot.params.add(paramItem);
        }
        else
        {
            ParameterInfo paramLot = new ParameterInfo();
            paramLot.setParameterID(BIPDANoSheetPickCartPortalParam.SeachLot);
            paramLot.setParameterValue(value);
            bmObjLot.params.add(paramLot);
        }

        ParameterInfo paramQty = new ParameterInfo();
        paramQty.setParameterID(BIPDANoSheetPickCartPortalParam.Qty);
        paramQty.setParameterValue(qry);
        bmObjLot.params.add(paramQty);

        ParameterInfo paramType = new ParameterInfo();
        paramType.setParameterID(BIPDANoSheetPickCartPortalParam.SeachType);
        paramType.setParameterValue(type);
        bmObjLot.params.add(paramType);

        ParameterInfo paramCase = new ParameterInfo();
        paramCase.setParameterID(BIPDANoSheetPickCartPortalParam.XfrCase);
        paramCase.setParameterValue("Cart");
        bmObjLot.params.add(paramCase);

        ParameterInfo paramStorage = new ParameterInfo();
        paramStorage.setParameterID(BIPDANoSheetPickCartPortalParam.StorageId);
        paramStorage.setParameterValue(strStorageId);
        bmObjLot.params.add(paramStorage);

        if (strConfigCond != null && strConfigCond.length() > 0) {
            ParameterInfo paramCond = new ParameterInfo();
            paramCond.setParameterID(BIPDANoSheetPickCartPortalParam.ConfigCond);
            paramCond.setParameterValue(strConfigCond);
            bmObjLot.params.add(paramCond);
        }

        if (strConfigSort != null && strConfigSort.length() > 0) {
            ParameterInfo paramSort = new ParameterInfo();
            paramSort.setParameterID(BIPDANoSheetPickCartPortalParam.ConfigSort);
            paramSort.setParameterValue(strConfigSort);
            bmObjLot.params.add(paramSort);
        }

        CallBIModule(bmObjLot, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(CheckBModuleReturnInfo(bModuleReturn))
                {
                    _dtFReg = bModuleReturn.getReturnJsonTables().get("BIGetRegData").get("Register");

                    LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                    GoodPickNonSheetGridAdapter adapter = new GoodPickNonSheetGridAdapter(_dtFReg, inflater);
                    holder.lvRegister.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void GetData()
    {
        if (holder.etSelectLotId.getText().toString().equals(""))
        {
            //WAPG016001 請輸入批號
            ShowMessage(R.string.WAPG016001);
            return;
        }

        //region Set BIModule
        // BIModule
        BModuleObject bimObj = new BModuleObject();
        bimObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDANoSheetPickCartPortal");
        bimObj.setModuleID("BIGetRegisterXfr");
        bimObj.setRequestID("BIGetRegisterXfr");
        bimObj.params = new Vector<ParameterInfo>();

        // Input param
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIPDANoSheetPickCartPortalParam.Lot);
        param1.setParameterValue(holder.etSelectLotId.getText().toString());
        bimObj.params.add(param1);
        // endregion

        CallBIModule(bimObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn)
            {
                if (CheckBModuleReturnInfo(bModuleReturn))
                {
                    _dtReg = bModuleReturn.getReturnJsonTables().get("BIGetRegisterXfr").get("PDA");

                    double step = Double.parseDouble(_dtReg.Rows.get(0).getValue("STEP").toString());
                    //指定tab,觸發切換事件
                    if (step == 0)
                    {
                        holder.tabHost.setCurrentTab(1);
                        holder.tabHost.setCurrentTab(0);
                    }
                    else if (step == 1)
                        holder.tabHost.setCurrentTab(1);
                    else if (step == 2)
                        holder.tabHost.setCurrentTab(2);
                    else if (step == 3)
                    {
                        holder.tabHost.setCurrentTab(2);
                        holder.tabHost.setCurrentTab(0);
                    }
                }
            }
        });
    }

    private void ProcessExecute()
    {
        if (_dtReg == null || _dtReg.Rows.size() <= 0) return;

        final int itab = holder.tabHost.getCurrentTab();
        String strSheetId = _strSraechId;
        String strStep = "";
        String strFromPort = "";
        String strToPort = "";
        String strObject = "";
        String strRegId = _dtReg.Rows.get(0).getValue("LOT_ID").toString();
        String strStorageId = _dtReg.Rows.get(0).getValue("STORAGE_ID").toString();
        String strRegLocation = "0";
        String strHaveCarrier = _dtReg.Rows.get(0).getValue("HAVE_CARR").toString();
        String strCarrier = _dtReg.Rows.get(0).getValue("CARRIER_ID").toString();
        String strBlock = _dtReg.Rows.get(0).getValue("BLOCK_ID").toString();
        String strQty = holder.etQty.getText().toString();

        if (itab == 0)
        {
            strStep = "CartStockOut";
            strFromPort = _dtReg.Rows.get(0).getValue("CART_PORT").toString();
            strToPort = _dtReg.Rows.get(0).getValue("OS_PORT").toString();
            strObject = _dtReg.Rows.get(0).getValue("CART_ID").toString();
        }
        else if (itab == 1)
        {
            strStep = "CartRegisterUnBind";
            strFromPort = _dtReg.Rows.get(0).getValue("CART_BIN_PORT").toString();
            strToPort = _dtReg.Rows.get(0).getValue("OS_PORT").toString();
            strRegLocation = strToPort;
            strObject = _dtReg.Rows.get(0).getValue("LOT_ID").toString();
        }
        else
        {
            strStep = "CartStockIn";
            strFromPort = _dtReg.Rows.get(0).getValue("OS_PORT").toString();
            strToPort = _dtReg.Rows.get(0).getValue("CART_FIX_PORT").toString();
            strObject = _dtReg.Rows.get(0).getValue("CART_ID").toString();
        }

        //region Set BIModule
        // BIModule
        BModuleObject bimObj = new BModuleObject();
        bimObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDANoSheetPickCartPortal");
        bimObj.setModuleID("BIRegisterXfrConfirm");
        bimObj.setRequestID("BIRegisterXfrConfirm");
        bimObj.params = new Vector<ParameterInfo>();

        // Input param
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIPDANoSheetPickCartPortalParam.Lot);
        param1.setParameterValue(strRegId);
        bimObj.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIPDANoSheetPickCartPortalParam.XfrCase);
        param2.setParameterValue("Cart");
        bimObj.params.add(param2);

        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BIPDANoSheetPickCartPortalParam.XfrTask);
        param3.setParameterValue(strStep);
        bimObj.params.add(param3);

        ParameterInfo param4 = new ParameterInfo();
        param4.setParameterID(BIPDANoSheetPickCartPortalParam.FromPort);
        param4.setParameterValue(strFromPort);
        bimObj.params.add(param4);

        ParameterInfo param5 = new ParameterInfo();
        param5.setParameterID(BIPDANoSheetPickCartPortalParam.ToPort);
        param5.setParameterValue(strToPort);
        bimObj.params.add(param5);

        ParameterInfo param6 = new ParameterInfo();
        param6.setParameterID(BIPDANoSheetPickCartPortalParam.ObjectId);
        param6.setParameterValue(strObject);
        bimObj.params.add(param6);

        ParameterInfo param7 = new ParameterInfo();
        param7.setParameterID(BIPDANoSheetPickCartPortalParam.StorageId);
        param7.setParameterValue(strStorageId);
        bimObj.params.add(param7);

        ParameterInfo param9 = new ParameterInfo();
        param9.setParameterID(BIPDANoSheetPickCartPortalParam.HaveCarrier);
        param9.setParameterValue(strHaveCarrier);
        bimObj.params.add(param9);

        ParameterInfo param10 = new ParameterInfo();
        param10.setParameterID(BIPDANoSheetPickCartPortalParam.Carrier);
        param10.setParameterValue(strCarrier);
        bimObj.params.add(param10);

        ParameterInfo param11 = new ParameterInfo();
        param11.setParameterID(BIPDANoSheetPickCartPortalParam.Block);
        param11.setParameterValue(strBlock);
        bimObj.params.add(param11);

        ParameterInfo param12 = new ParameterInfo();
        param12.setParameterID(BIPDANoSheetPickCartPortalParam.RegLocation);
        param12.setParameterValue(strRegLocation);
        bimObj.params.add(param12);

        ParameterInfo param13 = new ParameterInfo();
        param13.setParameterID(BIPDANoSheetPickCartPortalParam.Qty);
        param13.setParameterValue(strQty);
        bimObj.params.add(param13);

        ParameterInfo param14 = new ParameterInfo();
        param14.setParameterID(BIPDANoSheetPickCartPortalParam.SheetId);
//        param14.setParameterValue(holder.etSheetId.getText().toString());
        param14.setParameterValue(strSheetId);
        bimObj.params.add(param14);

        ParameterInfo param15 = new ParameterInfo();
        param15.setParameterID(BIPDANoSheetPickCartPortalParam.Seq);
        param15.setParameterValue(holder.cmbSeq.getSelectedItem().toString());
        bimObj.params.add(param15);

        ParameterInfo paramBestBin = new ParameterInfo();
        paramBestBin.setParameterID(BIPDANoSheetPickCartPortalParam.BestBin);
        paramBestBin.setParameterValue(_dtReg.Rows.get(0).getValue("OS_BIN_ID").toString());
        bimObj.params.add(paramBestBin);

        ParameterInfo param16 = new ParameterInfo();  // 20220812 Ikea 傳入 ItemId
        param16.setParameterID(BIPDANoSheetPickCartPortalParam.Item);
        param16.setParameterValue(holder.etItemId.getText().toString());
        bimObj.params.add(param16);

        ParameterInfo param17 = new ParameterInfo(); // 20220812 Ikea 傳入 BinId
        param17.setParameterID(BIPDANoSheetPickCartPortalParam.OsBinId);
        param17.setParameterValue(_dtReg.Rows.get(0).getValue("OS_BIN_ID").toString());
        bimObj.params.add(param17);

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

                        if (holder.tabHost.getCurrentTab() == 2)
                        {
                            Clear();
                        }
                        else
                        {
                            holder.tabHost.setCurrentTab(itab+1);
                        }
                    }
                    else
                    {
                        String Next = dt.Rows.get(0).getValue("NEXT").toString();

                        if (Next.equals("CartRegisterUnBind"))
                        {

                            holder.tabHost.setCurrentTab(1);
                        }
                        else if (Next.equals("CartStockIn"))
                        {
                            holder.tabHost.setCurrentTab(2);
                        }

                        //WAPG016005 請按確認執行下一個步驟
                        ShowMessage(R.string.WAPG016005);
                    }
                }
            }
        });
    }

    private void Clear()
    {
//        holder.etSheetId.setText("");
        holder.cmbSheetId.setSelection(lstSheetId.size()-1); // spinner設定回預設選項
        holder.etItemId.setText("");
        holder.etSelectLotId.setText("");
        holder.etQty.setText("");
        //holder.etLotId.setText("");

        //CartOut
        holder.tvCart.setText("");
        holder.tvFromPort.setText("");
        holder.tvToPort.setText("");

        //CartRegisterUnBind
        holder.tvLot2.setText("");
        holder.tvFromPort2.setText("");
        holder.tvToPort2.setText("");

        //CartIn
        holder.tvCart3.setText("");
        holder.tvFromPort3.setText("");
        holder.tvToPort3.setText("");

        _dtFReg = new DataTable();
        _dtReg = new DataTable();
        dtMaster = new DataTable();
        dtDetail = new DataTable();

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        GoodPickNonSheetGridAdapter adapter = new GoodPickNonSheetGridAdapter(_dtFReg, inflater);
        holder.lvRegister.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        lstSeq = new ArrayList<>();
        mapSeq = new HashMap<String, String>();

        ArrayAdapter<String> adapterSeq = new ArrayAdapter<>(GoodPickNonSheetCartActivity.this, android.R.layout.simple_spinner_dropdown_item, lstSeq);
        holder.cmbSeq.setAdapter(adapterSeq);
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
                        //WAPG016008 單據代碼[{0}]查無轉揀料單資料
                        ShowMessage(R.string.WAPG016008, strSheetId);
                        return;
                    }

                    String pickingID = dt.Rows.get(0).getValue("SHEET_ID").toString();

                    FetchSheetInfo(pickingID);
                }
            }
        });
        // endregion
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
    //endregion
}

//ERROR CODE
//WAPG016001 請輸入批號
//WAPG016002 請輸入料號
//WAPG016003 請輸入數量
//WAPG016004 作業成功
//WAPG016005 請按確認執行下一個步驟
//WAPG016006 請選擇單據代碼
//WAPG016008 單據代碼[{0}]查無轉揀料單資料