package com.delta.android.WMS.Client;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.delta.android.Core.Activity.BaseFlowActivity;
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
import com.delta.android.WMS.Param.BIFetchProcessSheetParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.ParamObj.Condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class GoodStockOutActivity extends BaseFlowActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_good_stock_out);
        // 取得控制項物件
        initViews();
        // 設定監聽事件
        setListensers();
        // Get Data 取得單據類型
        GetSheetType();
        // 由單據類型取得單據代碼
        spinnerSheetType.setOnItemSelectedListener(GetSheetId);
    }

    // private variable
    private HashMap<String,String> mapSheetTypeKey = new  HashMap<String,String>();
    private HashMap<String,String> mapIdNameSheetTypeKey = new  HashMap<>();
    private ArrayList<String> lstSheetId = new ArrayList<>();
    private DataTable dtDetail = new DataTable();
    private DataTable dtShtDetail = new DataTable();
    private DataTable dtShtCfg = new DataTable();
    private String strActualQtyStatus = "";
    private String strShtTypePolicyId = "";

    // 宣告控制項物件
    private Spinner spinnerSheetType;
    private ImageButton btnSearch;
//    private EditText txtSheetID;
    private Spinner cmbSheetId;
    private ListView listView;

    //取得控制項物件
    private void initViews()
    {
        spinnerSheetType = findViewById(R.id.cmbSheetType);
        btnSearch = findViewById(R.id.btnSearch);
//        txtSheetID = findViewById(R.id.txtSheetID);
        cmbSheetId = findViewById(R.id.cmbSheetId);
        listView = findViewById(R.id.listViewMst);
    }

    //設定監聽事件
    private void setListensers()
    {
        btnSearch.setOnClickListener(GetMaster);
        listView.setOnItemClickListener(GetDetail);
//        txtSheetID.setOnKeyListener(SheetIdOnKey);
    }

    // region 自訂 SimpleArrayAdapter
    private class SimpleArrayAdapter<T> extends ArrayAdapter {
        public SimpleArrayAdapter(Context context, int resource, List<T> objects) {
            super(context, resource, objects);
        }

        // 返回的數據沒有最後一項 (最後一項為提示語)
        @Override
        public int getCount() {
            // don't display last item. It is used as hint.
            int count = super.getCount();
            return count > 0 ? count - 1 : count;
        }
    }
    // endregion

    // region 事件

    private View.OnClickListener GetMaster = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            //region Check Input
            if (spinnerSheetType.getSelectedItemPosition() ==-1)
            {
                ShowMessage(R.string.WAPG005001);//WAPG005001  請選擇單據類型
                return;
            } else if (cmbSheetId.getSelectedItemPosition() == (lstSheetId.size() - 1)) {
                ShowMessage(R.string.WAPG005002);//WAPG005002  請選擇單據代碼
                return;
            }
        /*
        else if (txtSheetID.getText().toString().equals(""))
        {
            ShowMessage(R.string.WAPG005002);//WAPG005002  請輸入單據號碼
            return;
        }
         */
            // endregion

            String strSheetType = mapIdNameSheetTypeKey.get(spinnerSheetType.getSelectedItem().toString());

            String strSheetTypePolicyId = "";

            for (DataRow dr : dtShtCfg.Rows)
            {
                if (mapSheetTypeKey.get(spinnerSheetType.getSelectedItem().toString()).equals(dr.getValue("SHEET_TYPE_KEY").toString()))
                {
                    strSheetTypePolicyId = dr.getValue("SHEET_TYPE_POLICY_ID").toString();
                }
            }

            //PolicyId是Issue、Transfer時才需要去尋找揀料單代碼
            if (strSheetTypePolicyId.equals("Issue") || strSheetTypePolicyId.equals("Transfer"))
            {
                GetPickingID(strSheetType);
            }
            else
            {
                GetMaster();
            }
        }
    };

    private AdapterView.OnItemClickListener GetDetail = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle bundle=new Bundle();
            bundle.putSerializable("DataTable", dtDetail); // 直接傳DataTable -> PageTwo會Crash
            bundle.putSerializable("Sht", dtShtDetail);
            //20201020 Hans 修正傳值方法, 改成用dtDetail去找 Sheet_ID,
            //原本用txtSheetID傳值 txtSheetID在搜尋完後就會被清空, 導致出庫確認時查不到揀貨資訊
            bundle.putString("SHEET_ID", dtDetail.Rows.get(position).getValue("SHEET_ID").toString());
            bundle.putString("SOURCE_SHEET_ID", dtDetail.Rows.get(position).getValue("SOURCE_SHEET_ID").toString());
            bundle.putString("CFG", strActualQtyStatus);
            bundle.putString("SHEET_TYPE", strShtTypePolicyId);
//            Log.d("SHEET_ID", dtDetail.Rows.get(position).getValue("SHEET_ID").toString());
            gotoNextActivity(GoodStockOutDetailActivity.class, bundle);
        }
    };

    /*private View.OnKeyListener SheetIdOnKey = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            //只有按下Enter才會反映
            if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
            if (event.getAction() == KeyEvent.ACTION_DOWN){
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                GetMaster();
                return true;
            }
            return false;
        }
    };*/
    // endregion

    // region private function
    private void GetSheetType()
    {
        // Call BIModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        //bmObj.setModuleID("BIFetchSheetType");
        bmObj.setModuleID("BIFetchWmsSheetConfig");
        bmObj.setRequestID("SheetTypeAll");

        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Filter);
        //param1.setParameterValue(" AND T.SHEET_TYPE_POLICY_KEY IN ('0000004', '0000005' ,'0000008') "); // Sheet Type Policy = 轉倉 or 發料
        String SheetType = "'Issue','Transfer','Picking'";
        String _POLICY_ID_PICKING = "Picking";
        String paramFilter = String.format(" AND ((STP.SHEET_TYPE_POLICY_ID in (%s) AND CFG.TO_PICKING_MODE = 'Auto' AND CFG.STORAGE_ACTION_TYPE = 'From') OR STP.SHEET_TYPE_POLICY_ID ='%s') ",SheetType,_POLICY_ID_PICKING);
        param1.setParameterValue(paramFilter); //BIFetchWmsSheetConfig
        bmObj.params.add(param1);

        CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    DataTable dt = bModuleReturn.getReturnJsonTables().get("SheetTypeAll").get("SBRM_WMS_SHEET_CONFIG");
                    ArrayList<String> stringArrayList = new ArrayList<String>();
                    Iterator it =  dt.Rows.iterator();
                    int i = 0;
                    while (it.hasNext())
                    {
                        DataRow row = (DataRow) it.next();
                        stringArrayList.add( i,row.getValue("IDNAME").toString());
                        mapSheetTypeKey.put(row.getValue("IDNAME").toString(),row.getValue("SHEET_TYPE_KEY").toString());
                        // 存放SheetType
                        mapIdNameSheetTypeKey.put(row.getValue("IDNAME").toString(),row.getValue("SHEET_TYPE_ID").toString());
                        i++;
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(GoodStockOutActivity.this,android.R.layout.simple_spinner_dropdown_item, stringArrayList);
                    spinnerSheetType.setAdapter(adapter);

                    dtShtCfg = dt;
                }
            }
        });
    }

    // 由單據類別取得單據代碼
    private AdapterView.OnItemSelectedListener GetSheetId = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            String strSheetType = mapIdNameSheetTypeKey.get(spinnerSheetType.getSelectedItem().toString());
            if (!strSheetType.equals("null")) {

                String strSheetTypePolicyId = "";
                String strStatus = "";

                for (DataRow dr : dtShtCfg.Rows)
                {
                    if (mapSheetTypeKey.get(spinnerSheetType.getSelectedItem().toString()).equals(dr.getValue("SHEET_TYPE_KEY").toString()))
                    {
                        strSheetTypePolicyId = dr.getValue("SHEET_TYPE_POLICY_ID").toString();
                    }
                }

                //單據改找Confirmed，如果是Issue、Transfer,狀態要找Closed
                /*if (strSheetTypePolicyId.equals("Issue") || strSheetTypePolicyId.equals("Transfer"))
                    strStatus = "Closed";
                else
                    strStatus = "Confirmed";*/

                strStatus = "Confirmed";

                // Call BIModule
                BModuleObject bmObj = new BModuleObject();
                bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIFetchProcessSheet");
                bmObj.setModuleID("FetchProcessSheetByStatus");
                bmObj.setRequestID("FetchProcessSheetByStatus");
                bmObj.params = new Vector<ParameterInfo>();

                ParameterInfo param1 = new ParameterInfo();
                param1.setParameterID(BIFetchProcessSheetParam.sheetTypeID);
                param1.setParameterValue(strSheetType);
                bmObj.params.add(param1);

                ParameterInfo param2 = new ParameterInfo();
                param2.setParameterID(BIFetchProcessSheetParam.ProcessType);
                param2.setParameterValue("ST");
                bmObj.params.add(param2);

                List<String> lstStatus = new ArrayList<>();
                lstStatus.add(strStatus);
                VirtualClass vList = VirtualClass.create(VirtualClass.VirtualClassType.String);
                MList mList = new MList(vList);
                String strLstStatus = mList.generateFinalCode(lstStatus);
                ParameterInfo param3 = new ParameterInfo();
                param3.setParameterID(BIFetchProcessSheetParam.LstStatus);
                param3.setNetParameterValue(strLstStatus);
                bmObj.params.add(param3);

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

                            SimpleArrayAdapter adapter = new SimpleArrayAdapter<>(GoodStockOutActivity.this, android.R.layout.simple_spinner_dropdown_item, lstSheetId);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            cmbSheetId.setAdapter(adapter);
                            cmbSheetId.setSelection(lstSheetId.size() - 1, true);
                        }
                    }
                });
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            return;
        }
    };

    private void GetMaster()
    {
        //region Check Input
        if (spinnerSheetType.getSelectedItemPosition() ==-1)
        {
            ShowMessage(R.string.WAPG005001);//WAPG005001  請選擇單據類型
            return;
        } else if (cmbSheetId.getSelectedItemPosition() == (lstSheetId.size() - 1)) {
            ShowMessage(R.string.WAPG005002);//WAPG005002  請選擇單據代碼
            return;
        }
        /*
        else if (txtSheetID.getText().toString().equals(""))
        {
            ShowMessage(R.string.WAPG005002);//WAPG005002  請輸入單據號碼
            return;
        }
         */
        // endregion

        String strSheetType = mapIdNameSheetTypeKey.get(spinnerSheetType.getSelectedItem().toString());

        for (DataRow dr : dtShtCfg.Rows)
        {
            if (dr.getValue("SHEET_TYPE_ID").toString().equals(strSheetType))
            {
                strActualQtyStatus = dr.getValue("ACTUAL_QTY_STATUS").toString();
                strShtTypePolicyId = dr.getValue("SHEET_TYPE_POLICY_ID").toString();
            }
        }

        //region Call BIModule
        // BIModule
        List<BModuleObject> lstBmObj = new ArrayList<BModuleObject>();
        BModuleObject bmShtObj = new BModuleObject();
        bmShtObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmShtObj.setModuleID("BIFetchSheetMstAndDet");
        bmShtObj.setRequestID("FetchSheetMstAndDet");

        bmShtObj.params = new Vector<ParameterInfo>();

        // Set Condition
        List<Condition> lstCondition1 = new ArrayList<Condition>();
        Condition condition1 = new Condition();
        condition1.setAliasTable("M");
        condition1.setColumnName("SHEET_ID");
//        condition.setValue(txtSheetID.getText().toString().toUpperCase().trim()); //20200729 archie 轉大寫
        condition1.setValue(cmbSheetId.getSelectedItem().toString().toUpperCase().trim());
        // 用VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName() = "System.String"
        condition1.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
        lstCondition1.add(condition1);

        HashMap<String, List<?>> mapCondition1 = new HashMap<String, List<?>>();
        mapCondition1.put(condition1.getColumnName(),lstCondition1);
        VirtualClass vkey1 = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal1 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
        MesSerializableDictionaryList msdl1 = new MesSerializableDictionaryList(vkey1, vVal1);
        String strCond1 = msdl1.generateFinalCode(mapCondition1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIWMSFetchInfoParam.Condition);
        param2.setNetParameterValue(strCond1); // 要用set"Net"ParameterValue
        bmShtObj.params.add(param2);

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchSheetPickMstAndDet");
        bmObj.setRequestID("FetchPick");

        bmObj.params = new Vector<ParameterInfo>();

        // Set Condition
        List<Condition> lstCondition = new ArrayList<Condition>();
        Condition condition = new Condition();
        condition.setAliasTable("MST");
        condition.setColumnName("SHEET_ID");
//        condition.setValue(txtSheetID.getText().toString().toUpperCase().trim()); //20200729 archie 轉大寫
        condition.setValue(cmbSheetId.getSelectedItem().toString().toUpperCase().trim());
        // 用VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName() = "System.String"
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

        lstBmObj.add((bmShtObj));
        lstBmObj.add((bmObj));

        CallBIModule(lstBmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    DataTable dt = bModuleReturn.getReturnJsonTables().get("FetchPick").get("ShtPickMst");
                    dtDetail = bModuleReturn.getReturnJsonTables().get("FetchPick").get("ShtPickDet");
                    dtShtDetail = bModuleReturn.getReturnJsonTables().get("FetchSheetMstAndDet").get("Det");
                    //check select sheet type
                    if (dt != null && dt.Rows.size() > 0)
                    {
                        if (!mapSheetTypeKey.get(spinnerSheetType.getSelectedItem().toString()).equals(dt.Rows.get(0).getValue("SHEET_TYPE_KEY").toString()))
                        {
                            // 從Spinner取得單據代碼
                            String strSheetId = cmbSheetId.getSelectedItem().toString().toUpperCase().trim();
                            ShowMessage(R.string.WAPG005003, strSheetId, dt.Rows.get(0).getValue("SHEET_TYPE_ID"));//WAPG005003    單據代碼[{0}]單據類型[{1}]與所選單據類型不符
//                            ShowMessage(R.string.WAPG005003,txtSheetID.getText().toString(),dt.Rows.get(0).getValue("SHEET_TYPE_ID"));
                            dt = null;
                            dtDetail =null;
                            return;
                        }

                        if (!dt.Rows.get(0).getValue("SHEET_STATUS").toString().equals("Confirmed") )
                        {
                            // 從Spinner取得單據代碼
                            String strSheetId = cmbSheetId.getSelectedItem().toString().toUpperCase().trim();
                            ShowMessage(R.string.WAPG005004, strSheetId, "Confirmed");//WAPG005004    單據代碼[{0}]狀態須為[{1}]
//                            ShowMessage(R.string.WAPG005004,txtSheetID.getText().toString(),"Confirmed");
                            dt = null;
                            dtDetail =null;
                            return;
                        }

                        if (!dt.Rows.get(0).getValue("PICK_STATUS").toString().equals("Picked") )
                        {
                            ShowMessage(R.string.WAPG005005,dt.Rows.get(0).getValue("PICK_STATUS"),"Picked");//WAPG005005    揀料狀態[{0}]須為[{1}] 才能出庫
                            dt = null;
                            dtDetail =null;
                            return;
                        }
                    }else if (dt.Rows.size() <= 0)
                    {
                        ShowMessage(R.string.WAPG005006);//WAPG005006    查詢無資料
                        listView.setAdapter(null); // 查詢無資料應將listview清空
                        cmbSheetId.setSelection(lstSheetId.size()-1); // spinner設定回預設選項
                        return;
                    }

                    //加入原始單據代碼
                    if (!dt.getColumns().contains("SOURCE_SHEET_ID"))
                    {
                        DataColumn dcSource = new DataColumn("SOURCE_SHEET_ID");
                        dt.addColumn(dcSource);

                        for (DataRow dr : dt.Rows)
                        {
                            dr.setValue("SOURCE_SHEET_ID", cmbSheetId.getSelectedItem().toString().toUpperCase().trim());
                        }
                    }

                    if (!dtDetail.getColumns().contains("SOURCE_SHEET_ID"))
                    {
                        DataColumn dcSource = new DataColumn("SOURCE_SHEET_ID");
                        dtDetail.addColumn(dcSource);

                        for (DataRow dr : dtDetail.Rows)
                        {
                            dr.setValue("SOURCE_SHEET_ID", cmbSheetId.getSelectedItem().toString().toUpperCase().trim());
                        }
                    }
                    // replace T
                    dt.Rows.get(0).setValue("CREATE_DATE",dt.Rows.get(0).getValue("CREATE_DATE").toString().replace("T"," "));
                    ListAdapter adapter = new SimpleAdapter(
                            GoodStockOutActivity.this,
                            dt.toListHashMap(),
                            R.layout.activity_wms_good_stock_out_listview,
                            new String[]{"SHEET_ID","PICK_STATUS","CREATE_DATE"},
                            new int[]{R.id.txtSheetID,R.id.txtSheetStastus,R.id.txtCreateDate}
                    );
                    listView.setAdapter(adapter);

                }
            }
        });
        // endregion
//        txtSheetID.setText("");
    }

    private void GetMasterByPickId(final String pickingID, String strSheetType, final String strSheetTypeKey)
    {
        for (DataRow dr : dtShtCfg.Rows)
        {
            if (dr.getValue("SHEET_TYPE_ID").toString().equals(strSheetType))
            {
                strActualQtyStatus = dr.getValue("ACTUAL_QTY_STATUS").toString();
                strShtTypePolicyId = dr.getValue("SHEET_TYPE_POLICY_ID").toString();
            }
        }

        //region Call BIModule
        // BIModule
        List<BModuleObject> lstBmObj = new ArrayList<BModuleObject>();
        BModuleObject bmShtObj = new BModuleObject();
        bmShtObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmShtObj.setModuleID("BIFetchSheetMstAndDet");
        bmShtObj.setRequestID("FetchSheetMstAndDet");

        bmShtObj.params = new Vector<ParameterInfo>();

        // Set Condition
        List<Condition> lstCondition1 = new ArrayList<Condition>();
        Condition condition1 = new Condition();
        condition1.setAliasTable("M");
        condition1.setColumnName("SHEET_ID");
//        condition.setValue(txtSheetID.getText().toString().toUpperCase().trim()); //20200729 archie 轉大寫
        condition1.setValue(pickingID);
        // 用VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName() = "System.String"
        condition1.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
        lstCondition1.add(condition1);

        HashMap<String, List<?>> mapCondition1 = new HashMap<String, List<?>>();
        mapCondition1.put(condition1.getColumnName(),lstCondition1);
        VirtualClass vkey1 = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal1 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
        MesSerializableDictionaryList msdl1 = new MesSerializableDictionaryList(vkey1, vVal1);
        String strCond1 = msdl1.generateFinalCode(mapCondition1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIWMSFetchInfoParam.Condition);
        param2.setNetParameterValue(strCond1); // 要用set"Net"ParameterValue
        bmShtObj.params.add(param2);

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchSheetPickMstAndDet");
        bmObj.setRequestID("FetchPick");

        bmObj.params = new Vector<ParameterInfo>();

        // Set Condition
        List<Condition> lstCondition = new ArrayList<Condition>();
        Condition condition = new Condition();
        condition.setAliasTable("MST");
        condition.setColumnName("SHEET_ID");
//        condition.setValue(txtSheetID.getText().toString().toUpperCase().trim()); //20200729 archie 轉大寫
        condition.setValue(pickingID);
        // 用VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName() = "System.String"
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

        lstBmObj.add((bmShtObj));
        lstBmObj.add((bmObj));

        CallBIModule(lstBmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    DataTable dt = bModuleReturn.getReturnJsonTables().get("FetchPick").get("ShtPickMst");
                    dtDetail = bModuleReturn.getReturnJsonTables().get("FetchPick").get("ShtPickDet");
                    dtShtDetail = bModuleReturn.getReturnJsonTables().get("FetchSheetMstAndDet").get("Det");
                    //check select sheet type
                    if (dt != null && dt.Rows.size() > 0)
                    {
                        if (!strSheetTypeKey.equals(dt.Rows.get(0).getValue("SHEET_TYPE_KEY").toString()))
                        {
                            // 從Spinner取得單據代碼
                            String strSheetId = pickingID;
                            ShowMessage(R.string.WAPG005003, strSheetId, dt.Rows.get(0).getValue("SHEET_TYPE_ID"));//WAPG005003    單據代碼[{0}]單據類型[{1}]與所選單據類型不符
//                            ShowMessage(R.string.WAPG005003,txtSheetID.getText().toString(),dt.Rows.get(0).getValue("SHEET_TYPE_ID"));
                            dt = null;
                            dtDetail =null;
                            return;
                        }

                        if (!dt.Rows.get(0).getValue("SHEET_STATUS").toString().equals("Confirmed") )
                        {
                            // 從Spinner取得單據代碼
                            String strSheetId = pickingID;
                            ShowMessage(R.string.WAPG005004, strSheetId, "Confirmed");//WAPG005004    單據代碼[{0}]狀態須為[{1}]
//                            ShowMessage(R.string.WAPG005004,txtSheetID.getText().toString(),"Confirmed");
                            dt = null;
                            dtDetail =null;
                            return;
                        }

                        if (!dt.Rows.get(0).getValue("PICK_STATUS").toString().equals("Picked") )
                        {
                            ShowMessage(R.string.WAPG005005,dt.Rows.get(0).getValue("PICK_STATUS"),"Picked");//WAPG005005    揀料狀態[{0}]須為[{1}] 才能出庫
                            dt = null;
                            dtDetail =null;
                            return;
                        }
                    }else if (dt.Rows.size() <= 0)
                    {
                        ShowMessage(R.string.WAPG005006);//WAPG005006    查詢無資料
                        listView.setAdapter(null); // 查詢無資料應將listview清空
                        cmbSheetId.setSelection(lstSheetId.size()-1); // spinner設定回預設選項
                        return;
                    }

                    //加入原始單據代碼
                    if (!dt.getColumns().contains("SOURCE_SHEET_ID"))
                    {
                        DataColumn dcSource = new DataColumn("SOURCE_SHEET_ID");
                        dt.addColumn(dcSource);

                        for (DataRow dr : dt.Rows)
                        {
                            dr.setValue("SOURCE_SHEET_ID", cmbSheetId.getSelectedItem().toString().toUpperCase().trim());
                        }
                    }

                    if (!dtDetail.getColumns().contains("SOURCE_SHEET_ID"))
                    {
                        DataColumn dcSource = new DataColumn("SOURCE_SHEET_ID");
                        dtDetail.addColumn(dcSource);

                        for (DataRow dr : dtDetail.Rows)
                        {
                            dr.setValue("SOURCE_SHEET_ID", cmbSheetId.getSelectedItem().toString().toUpperCase().trim());
                        }
                    }
                    // replace T
                    dt.Rows.get(0).setValue("CREATE_DATE",dt.Rows.get(0).getValue("CREATE_DATE").toString().replace("T"," "));
                    ListAdapter adapter = new SimpleAdapter(
                            GoodStockOutActivity.this,
                            dt.toListHashMap(),
                            R.layout.activity_wms_good_stock_out_listview,
                            new String[]{"SOURCE_SHEET_ID", "SHEET_ID","PICK_STATUS","CREATE_DATE"},
                            new int[]{R.id.txtSheetID, R.id.txtPickSheet,R.id.txtSheetStastus,R.id.txtCreateDate}
                    );
                    listView.setAdapter(adapter);

                }
            }
        });
        // endregion
//        txtSheetID.setText("");
    }

    private void GetPickingID(final String strSheetType)
    {
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
//        condition.setValue(txtSheetID.getText().toString().toUpperCase().trim()); //20200729 archie 轉大寫
        condition1.setValue(cmbSheetId.getSelectedItem().toString().toUpperCase().trim());
        // 用VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName() = "System.String"
        condition1.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
        lstCondition1.add(condition1);

        List<Condition> lstCondition2 = new ArrayList<Condition>();
        Condition condition2 = new Condition();
        condition2.setAliasTable("ST");
        condition2.setColumnName("SHEET_TYPE_ID");
//        condition.setValue(txtSheetID.getText().toString().toUpperCase().trim()); //20200729 archie 轉大寫
        condition2.setValue(strSheetType);
        // 用VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName() = "System.String"
        condition2.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
        lstCondition2.add(condition2);

        HashMap<String, List<?>> mapCondition1 = new HashMap<String, List<?>>();
        mapCondition1.put(condition1.getColumnName(),lstCondition1);
        mapCondition1.put(condition2.getColumnName(),lstCondition2);
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
                        //WAPG005012    單據類型[{0}]查無轉揀料單資料
                        ShowMessage(R.string.WAPG005012, strSheetType);
                        return;
                    }

                    String pickingID = dt.Rows.get(0).getValue("SHEET_ID").toString();
                    String strSheetTypeKey = dt.Rows.get(0).getValue("SHEET_TYPE_KEY").toString();
                    GetMasterByPickId(pickingID, strSheetType, strSheetTypeKey);
                }
            }
        });
        // endregion


    }
    //endregion
}

//Error Code
//WAPG005001    請選擇單據類型
//WAPG005002    請選擇單據代碼
//WAPG005003    單據代碼[{0}]單據類型[{1}]與所選單據類型不符
//WAPG005004    單據代碼[{0}]狀態須為[{1}]
//WAPG005005    揀料狀態[{0}]須為[{1}] 才能出庫
//WAPG005006    查詢無資料
//WAPG005012    單據類型[{0}]查無轉揀料單資料