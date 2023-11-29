package com.delta.android.WMS.Client;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.GenerateJsonNetString.MList;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionary;
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
import com.delta.android.WMS.Param.BIFetchRecoBinParam;
import com.delta.android.WMS.Param.BIPDANoSheetCarrierPortalParam;
import com.delta.android.WMS.Param.BIPDANoSheetRegisterPortalParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.BPDADispatchOrderForInvParam;
import com.delta.android.WMS.Param.BPDAGoodPickParam;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.delta.android.WMS.Param.ParamObj.PickDetObj;
import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GoodPickNonSheetActivity extends BaseFlowActivity {

    //Function ID = WAPG012

    //    private EditText etSheetId;
    private Spinner cmbSheetId;
    private ImageButton ibtnSheetId;
    private Spinner cmbSeq;
    //private EditText etLotId;
    private EditText etItemId;
    private EditText etSelectLotId;
    private EditText etQty;
    private ListView lvRegister;
    //private ImageButton ibtnLotSearch;
    //private ImageButton ibtnItemSearch;
    private ImageButton ibtnQty;
    private Button btnConfirm;

    private int index = 0;
    private String strStorage = "";
    private String strFetchBin = "";
    private String strBestBin = "";
//    private String strFromTo = "";
    private String strIptQty = ""; //Ikea
    private String _strSraechId = ""; //最終處理的單據

    public DataTable dtRegister;
    public DataTable dtMaster;
    public DataTable dtDetail;

    private DataTable dtConfigCond = null;
    private DataTable dtConfigSort = null;
    private String strConfigCond = null;
    private String strConfigSort = null;

    ArrayList<String> lstSeq = null;
    HashMap<String, String> mapSeq = new HashMap<String, String>();
    ArrayList<String> lstSheetId = new ArrayList<>();
    HashMap<String, String> mapSheetInfo = new HashMap<>(); // Ikea

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good_pick_non_sheet);

        initView();
        setListensers();
        GetSheetId();
    }

    private void initView() {
        //etLotId = findViewById(R.id.etLotId);
//        etSheetId = findViewById(R.id.etSheetId);
        cmbSheetId = findViewById(R.id.cmbSheetId);
        ibtnSheetId = findViewById(R.id.ibtnSheetIdSearch);
        cmbSeq = findViewById(R.id.cmbSeq);
        etItemId = findViewById(R.id.etItemId);
        etSelectLotId = findViewById(R.id.etSelectLotId);
        etQty = findViewById(R.id.etPickQty);
        lvRegister = findViewById(R.id.lvRegisters);
        btnConfirm = findViewById(R.id.btnConfirm);
        ibtnQty = findViewById(R.id.ibtnQtySearch);
        //ibtnLotSearch = findViewById(R.id.ibtnLotSearch);
        //ibtnItemSearch = findViewById(R.id.ibtnItemSearch);
    }

    private void initData() {
        dtRegister = new DataTable();
        GetListView();

        //etLotId.setText("");
        //etSheetId.setText("");
        GetSheetId();

        etItemId.setText("");
        etSelectLotId.setText("");
        etQty.setText("");

        dtMaster = new DataTable();
        dtDetail = new DataTable();
        lstSeq = new ArrayList<>();
        mapSeq = new HashMap<String, String>();
        mapSheetInfo = new HashMap<>(); //Ikea

        index = 0;
        strStorage = "";

        ArrayAdapter<String> adapterSeq = new ArrayAdapter<>(GoodPickNonSheetActivity.this, android.R.layout.simple_spinner_dropdown_item, lstSeq);
        cmbSeq.setAdapter(adapterSeq);

        strConfigCond = null;
        strConfigSort = null;
    }

    private void setListensers() {
        btnConfirm.setOnClickListener(lsConfirm);
        //ibtnLotSearch.setOnClickListener(ibtnLotSearchClick);
        cmbSeq.setOnItemSelectedListener(lstSeqSelected);
        ibtnSheetId.setOnClickListener(lsSheetIdFetch);
        ibtnQty.setOnClickListener(lsRegisterFetch);
        //ibtnItemSearch.setOnClickListener(ibtnItemSearchClick);
        lvRegister.setOnItemClickListener(lvRegisterClick);
    }

    private void FetchSheetInfo(String strSheetId) {
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
        conditionSheetId.setValue(_strSraechId);
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

        // region 取得ConfigCond及ConfigSort
        BModuleObject biShtCfgSortAndCond = new BModuleObject();
        biShtCfgSortAndCond.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        biShtCfgSortAndCond.setModuleID("BIFetchConfigCondAndSortBySheetId");
        biShtCfgSortAndCond.setRequestID("FetchConfigCondAndSort");
        biShtCfgSortAndCond.params = new Vector<ParameterInfo>();
        ParameterInfo paramShtId = new ParameterInfo();
        paramShtId.setParameterID(BIFetchPickStrategyParam.SheetId);
        paramShtId.setParameterValue(_strSraechId);
        biShtCfgSortAndCond.params.add(paramShtId);
        // endregion

        List<BModuleObject> lstBmObj = new ArrayList<>();
        lstBmObj.add(bmObjSheet);
        lstBmObj.add(biShtCfgSortAndCond);

        CallBIModule(lstBmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    dtMaster = bModuleReturn.getReturnJsonTables().get("BIFetchSheetMstAndDet").get("Mst");
                    dtDetail = bModuleReturn.getReturnJsonTables().get("BIFetchSheetMstAndDet").get("Det");

                    dtConfigCond = bModuleReturn.getReturnJsonTables().get("FetchConfigCondAndSort").get("SheetConfigCond");
                    dtConfigSort = bModuleReturn.getReturnJsonTables().get("FetchConfigCondAndSort").get("SheetConfigSort");

                    if (dtMaster == null || dtMaster.Rows.size() <= 0 || dtDetail == null || dtDetail.Rows.size() <= 0) {
                        //WAPG012010    查詢單據資料錯誤
                        ShowMessage(R.string.WAPG012010);
                        return;
                    }

                    lstSeq = new ArrayList<String>();
                    for (DataRow dr : dtDetail.Rows) {
                        Double doubleSeq = Double.parseDouble(dr.getValue("SEQ").toString());
                        Integer intSeq = Integer.valueOf(doubleSeq.intValue());

                        lstSeq.add(intSeq.toString());
                        mapSeq.put(intSeq.toString(), dr.getValue("SEQ").toString());

                    }

                    ArrayAdapter<String> adapterSeq = new ArrayAdapter<>(GoodPickNonSheetActivity.this, android.R.layout.simple_spinner_dropdown_item, lstSeq);
                    cmbSeq.setAdapter(adapterSeq);
                }
            }
        });
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

                    SimpleArrayAdapter adapter = new GoodPickNonSheetActivity.SimpleArrayAdapter<>(GoodPickNonSheetActivity.this, android.R.layout.simple_spinner_dropdown_item, lstSheetId);
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

    private Spinner.OnItemSelectedListener lstSeqSelected = new Spinner.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            MapSeqItem();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    };

    private void MapSeqItem() {

        DataRow selectedRow = null;

        String seq = mapSeq.get(cmbSeq.getSelectedItem().toString());
        for (DataRow dr : dtDetail.Rows) {
            if (dr.getValue("SEQ").toString().equals(seq)) {
                etItemId.setText(dr.getValue("ITEM_ID").toString());
                selectedRow = dr;
                break;
            }
        }

        strConfigCond = generateExtendConfigCond(selectedRow, dtConfigCond);
        strConfigSort = generateExtendConfigSort(dtConfigSort);
    }

    private void FetchLotInfo() {

        // region 找出無單據揀貨策略，再依策略找出 Register
        BModuleObject bmObjLot = new BModuleObject();
        bmObjLot.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDANoSheetRegisterPortal");
        bmObjLot.setModuleID("BIGetPickRegister");
        bmObjLot.setRequestID("BIGetPickRegister");

        bmObjLot.params = new Vector<ParameterInfo>();
        ParameterInfo paramReg = new ParameterInfo();
        paramReg.setParameterID(BIPDANoSheetRegisterPortalParam.RegisterId);
        paramReg.setParameterValue("");
        bmObjLot.params.add(paramReg);

        ParameterInfo paramItem = new ParameterInfo();
        paramItem.setParameterID(BIPDANoSheetRegisterPortalParam.ItemId);
        paramItem.setParameterValue(etItemId.getText().toString());
        bmObjLot.params.add(paramItem);

        ParameterInfo paramQty = new ParameterInfo();
        paramQty.setParameterID(BIPDANoSheetRegisterPortalParam.RegQty);
        paramQty.setParameterValue(etQty.getText().toString());
        bmObjLot.params.add(paramQty);

        if (strConfigCond != null && strConfigCond.length() > 0) {
            ParameterInfo paramCond = new ParameterInfo();
            paramCond.setParameterID(BIPDANoSheetRegisterPortalParam.ConfigCond);
            paramCond.setParameterValue(strConfigCond);
            bmObjLot.params.add(paramCond);
        }

        if (strConfigSort != null && strConfigSort.length() > 0) {
            ParameterInfo paramSort = new ParameterInfo();
            paramSort.setParameterID(BIPDANoSheetRegisterPortalParam.ConfigSort);
            paramSort.setParameterValue(strConfigSort);
            bmObjLot.params.add(paramSort);
        }
        // endregion

        CallBIModule(bmObjLot, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (!CheckBModuleReturnInfo(bModuleReturn))
                    return;

                dtRegister = bModuleReturn.getReturnJsonTables().get("BIGetPickRegister").get("Register");
                GetListView();

            }
        });
    }

    private void Confirm() {
        if (etSelectLotId.getText().toString().equals("")) {
            ShowMessage(R.string.WAPG012006); //WAPG012006 請選擇欲檢貨出庫物料
            return;
        }
        if (etQty.getText().toString().equals("")) {
            ShowMessage(R.string.WAPG012002); //WAPG012002 請輸入數量
            return;
        }
        double regQty = Double.parseDouble(dtRegister.Rows.get(index).getValue("QTY").toString());
        double pickQty = Double.parseDouble(etQty.getText().toString());

        if (pickQty > regQty) {
            ShowMessage(R.string.WAPG012008); //WAPG012008 檢貨數量不可大於物料數量
            return;
        }

        //BI
        BModuleObject bimObj = new BModuleObject();
        bimObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDANoSheetRegisterPortal");
        bimObj.setModuleID("BIRegisterXfrConfirm");
        bimObj.setRequestID("BIRegisterXfrConfirm");
        bimObj.params = new Vector<ParameterInfo>();

        // Input param
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIPDANoSheetRegisterPortalParam.RegisterId);
        param1.setParameterValue(etSelectLotId.getText().toString());
        bimObj.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIPDANoSheetRegisterPortalParam.XfrCase);
        param2.setParameterValue("Lot");
        bimObj.params.add(param2);

        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BIPDANoSheetRegisterPortalParam.XfrTask);
        param3.setParameterValue("RegisterStockOut");
        bimObj.params.add(param3);

        //ParameterInfo param4 = new ParameterInfo();
        //param4.setParameterID(BIPDANoSheetRegisterPortalParam.FromPort);
        //param4.setParameterValue(strFromTo);
        //bimObj.params.add(param4);

        ParameterInfo param5 = new ParameterInfo();
        param5.setParameterID(BIPDANoSheetRegisterPortalParam.ObjectId);
        param5.setParameterValue(etSelectLotId.getText().toString());
        bimObj.params.add(param5);

        ParameterInfo param6 = new ParameterInfo();
        param6.setParameterID(BIPDANoSheetRegisterPortalParam.StorageId);
        param6.setParameterValue(strStorage);
        bimObj.params.add(param6);

        ParameterInfo param7 = new ParameterInfo();
        param7.setParameterID(BIPDANoSheetRegisterPortalParam.BestBin);
        param7.setParameterValue(strBestBin);
        bimObj.params.add(param7);

        ParameterInfo param8 = new ParameterInfo();
        param8.setParameterID(BIPDANoSheetRegisterPortalParam.Stock);
        param8.setParameterValue("O");
        bimObj.params.add(param8);

        ParameterInfo param9 = new ParameterInfo();
        param9.setParameterID(BIPDANoSheetRegisterPortalParam.RegQty);
        param9.setParameterValue(etQty.getText().toString());
        bimObj.params.add(param9);

        HashMap<String, Integer> dicSheet = new HashMap<String, Integer>();
//        dicSheet.put(etSheetId.getText().toString(), Integer.parseInt(cmbSeq.getSelectedItem().toString()));
        String strSheetId = _strSraechId;
        dicSheet.put(strSheetId, Integer.parseInt(cmbSeq.getSelectedItem().toString()));

        VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create(VirtualClass.VirtualClassType.Decimal);
        MesSerializableDictionary msd = new MesSerializableDictionary(vKey, vVal);
        String strSheet = msd.generateFinalCode(dicSheet);

        ParameterInfo param10 = new ParameterInfo();
        param10.setParameterID(BIPDANoSheetRegisterPortalParam.DicPick);
        param10.setNetParameterValue(strSheet);
        bimObj.params.add(param10);

        ParameterInfo param11 = new ParameterInfo();  // 20220812 Ikea 傳入 ItemId
        param11.setParameterID(BIPDANoSheetCarrierPortalParam.ItemId);
        param11.setParameterValue(etItemId.getText().toString());
        bimObj.params.add(param11);

        ParameterInfo param12 = new ParameterInfo(); // 20220812 Ikea 傳入 BinId
        param12.setParameterID(BIPDANoSheetCarrierPortalParam.FetchBinId);
        param12.setParameterValue(dtRegister.Rows.get(index).getValue("BIN_ID").toString());
        bimObj.params.add(param12);

        CallBIModule(bimObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (!CheckBModuleReturnInfo(bModuleReturn)) return;

                ShowMessage(R.string.WAPG012007); //WAPG012007 檢貨出庫完成
                initData();
            }
        });
    }

    private void GetListView() {
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        GoodPickNonSheetGridAdapter adapter = new GoodPickNonSheetGridAdapter(dtRegister, inflater);
        lvRegister.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private View.OnKeyListener lsLotIdKey = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                FetchLotInfo();
            }
            return false;
        }
    };

    private View.OnClickListener lsSheetIdFetch = new View.OnClickListener() {
        @Override
        public void onClick(View veiw) {
//            if(etSheetId.getText().toString().equals("")){
//                //WAPG012009    請輸入單據代碼
//                ShowMessage(R.string.WAPG012009);
//                return;
//            }

            int sheetIdIndex = cmbSheetId.getSelectedItemPosition();
            if (sheetIdIndex == (lstSheetId.size() - 1)) {
                ShowMessage(R.string.WAPG012009); //WAPG012009    請選擇單據代碼
                return;
            }

            String strSheetId = cmbSheetId.getSelectedItem().toString().toUpperCase().trim();

            GetSht(strSheetId);
        }
    };

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
                        //WAPG012011    單據代碼[{0}]查無轉揀料單資料
                        ShowMessage(R.string.WAPG012011, strSheetId);
                        return;
                    }

                    String pickingID = dt.Rows.get(0).getValue("SHEET_ID").toString();

                    FetchSheetInfo(pickingID);
                }
            }
        });
        // endregion
    }

    private View.OnClickListener lsRegisterFetch = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (etItemId.getText().toString().equals("")) {
                ShowMessage(R.string.WAPG012005); //WAPG012005    請輸入料號
                return;
            }

            FetchLotInfo();
        }
    };

    /*
    private View.OnClickListener ibtnLotSearchClick = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            if(etLotId.getText().toString().equals("")){
                ShowMessage(R.string.WAPG012001); //WAPG012001 請輸入批號
                return;
            }
            if(etQty.getText().toString().equals("")){
                ShowMessage(R.string.WAPG012002); //WAPG012002  請輸入數量
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
              ShowMessage(R.string.WAPG012005); //WAPG012005 請輸入料號
              return;
          }
          if(etQty.getText().toString().equals("")){
              ShowMessage(R.string.WAPG012002); //WAPG012002  請輸入數量
              return;
          }
          FetchLotInfo();
      }
    };
    */

    public void OnClickLotClear(View v) {
        etSelectLotId.setText("");
    }

    public void OnClickQtyClear(View v) {
        etQty.setText("");
    }

    private AdapterView.OnClickListener lsConfirm = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Confirm();
        }
    };

    private AdapterView.OnItemClickListener lvRegisterClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            etSelectLotId.setText(dtRegister.Rows.get(position).getValue("REGISTER_ID").toString());
            strStorage = dtRegister.Rows.get(position).getValue("STORAGE_ID").toString();
            strFetchBin = dtRegister.Rows.get(position).getValue("BIN_ID").toString();

            BModuleObject bmObjBest = new BModuleObject();
            bmObjBest.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDANoSheetRegisterPortal");
            bmObjBest.setModuleID("BIGetBestBin");
            bmObjBest.setRequestID("BIGetBestBin");

            bmObjBest.params = new Vector<ParameterInfo>();
            ParameterInfo paramReg = new ParameterInfo();
            paramReg.setParameterID(BIPDANoSheetRegisterPortalParam.RegisterId);
            paramReg.setParameterValue(etSelectLotId.getText().toString());
            bmObjBest.params.add(paramReg);

            ParameterInfo paramStock = new ParameterInfo();
            paramStock.setParameterID(BIPDANoSheetRegisterPortalParam.Stock);
            paramStock.setParameterValue("O");
            bmObjBest.params.add(paramStock);

            ParameterInfo paramStorage = new ParameterInfo();
            paramStorage.setParameterID(BIPDANoSheetRegisterPortalParam.StorageId);
            paramStorage.setParameterValue(strStorage);
            bmObjBest.params.add(paramStorage);

            ParameterInfo paramBin = new ParameterInfo();
            paramBin.setParameterID(BIPDANoSheetRegisterPortalParam.FetchBin);
            paramBin.setParameterValue(strFetchBin);
            bmObjBest.params.add(paramBin);

            CallBIModule(bmObjBest, new WebAPIClientEvent() {
                @Override
                public void onPostBack(BModuleReturn bModuleReturn) {
                    if (CheckBModuleReturnInfo(bModuleReturn)) {
                        Gson gson = new Gson();
                        strBestBin = gson.fromJson(bModuleReturn.getReturnList().get("BIGetBestBin").get(BIFetchRecoBinParam.BestBin).toString(), String.class);
//                        strFromTo = gson.fromJson(bModuleReturn.getReturnList().get("BIGetBestBin").get(BIPDANoSheetRegisterPortalParam.ToPort).toString(), String.class);
                    }
                }
            });
            index = position;
        }
    };

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

//ERROR CODE WAPG012
//WAPG012001    請輸入批號
//WAPG012002    請輸入數量
//WAPG012003    輸入的數量與物料數量不符
//WAPG012004    檢貨出庫完成
//WAPG012005    請輸入料號
//WAPG012006    請選擇欲檢貨出庫物料
//WAPG012007    檢貨出庫完成
//WAPG012008    檢貨數量不可大於物料數量
//WAPG012009    請選擇單據代碼
//WAPG012010    查詢單據資料錯誤
//WAPG012011    單據代碼[{0}]查無轉揀料單資料
