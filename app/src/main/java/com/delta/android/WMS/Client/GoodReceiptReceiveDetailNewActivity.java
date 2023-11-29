package com.delta.android.WMS.Client;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.DataTable.DataColumn;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionaryList;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Client.Fragment.GoodReceiveDataFragment;
import com.delta.android.WMS.Client.Fragment.GoodReceiveListFragment;
import com.delta.android.WMS.Client.GridAdapter.GoodReceiptReceiveDetaGridAdapter;
import com.delta.android.WMS.Param.BIGoodReceiptReceivePortalParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.ParamObj.Condition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GoodReceiptReceiveDetailNewActivity extends BaseFlowActivity implements GoodReceiveListFragment.OnFragmentInteractionListener,
        GoodReceiveDataFragment.OnFragmentInteractionListener{

    private GoodReceiveListFragment receiveListFragment = new GoodReceiveListFragment();
    private GoodReceiveDataFragment receiveDataFragment = new GoodReceiveDataFragment();;
    TextView GrId;
    TextView ItemId;
    TextView ItemTotalQty;
    private TabLayout tabLayout;
    int now = 0;
    private DataTable GrMstTable;// get p.1
    private DataTable GrDetTable;// get p.1
    private HashMap SeqQtyOfAllLot;    // get p.1
    private HashMap SeqSkipQcOfAllLot;    // get p.1
    private DataTable _DtQc;
    private DataTable _dtSize;
    private DataTable _dtItem;
    private DataTable _dtSkuLevel;
    private HashMap<String, String> ItemSkipQcOfAllLot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_good_receipt_receive_detail_new);

        //region -- 控制項初始化 --
        GrId = findViewById(R.id.tvGrId);
        ItemId = findViewById(R.id.tvGrDetItemId);
        ItemTotalQty = findViewById(R.id.tvGrDetItemTotalQty);
        tabLayout = findViewById(R.id.tabLayout);
        //endregion

        InitialData();
        // Step02-新增TabLayout的按下按鈕的監聽器:

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            //按下要做的事
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Step03-寫一個方法，tab.getPosition是按下哪個按鈕，將之傳入fragmentChange方法內:
                fragmentChange(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Step04-切換顯示方法撰寫:
    public void fragmentChange(int position){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Step05-先判斷目前的Fragment，並將之隱藏:
        switch (now){
            case 0:
                //if (count == 1) GoodReceiveDataFragment.setData(GrMstTable, GrDetTable, SeqQtyOfAllLot, SeqSkipQcOfAllLot, _dtSize, getApplicationContext(), _dtItem, ItemSkipQcOfAllLot);
                fragmentTransaction.hide(receiveListFragment);
                break;
            case 1:
                GetGrrDet();
                fragmentTransaction.hide(receiveDataFragment);
                break;
        }

        // Step06-判斷使用者點選的選單按鈕，顯示相對應的Fragment:
        switch (position){
            case 0:
                fragmentTransaction.show(receiveListFragment);
                break;
            case 1:
                fragmentTransaction.show(receiveDataFragment);
                break;
        }
        fragmentTransaction.commit();
        // Step06-更新目前所在的Fragment:
        now = position;
    }

    private void InitialData()
    {
        GrMstTable =  (DataTable)getIntent().getSerializableExtra("GrMst");
        GrDetTable =  (DataTable)getIntent().getSerializableExtra("GrDet");
        SeqQtyOfAllLot = new HashMap();
        SeqQtyOfAllLot = (HashMap) getIntent().getSerializableExtra("GrrQty");
        SeqSkipQcOfAllLot = new HashMap();
        SeqSkipQcOfAllLot = (HashMap) getIntent().getSerializableExtra("GrrSkipQc");
        _dtSize =  (DataTable)getIntent().getSerializableExtra("Size");
        _dtItem =  (DataTable)getIntent().getSerializableExtra("Item");
        _dtSkuLevel =  (DataTable)getIntent().getSerializableExtra("SkuLevel");

        GrId.setText(GrMstTable.Rows.get(0).getValue("GR_ID").toString());

        GetQcData();
    }

    private void GetQcData()
    {
        final String strVendorKey = GrMstTable.Rows.get(0).getValue("VENDOR_KEY").toString();
        String strVKey = String.format("%s','%s", strVendorKey, "*");
        List<String> lstItem = new ArrayList<>();

        for (DataRow dr : GrDetTable.Rows)
        {
            if (!lstItem.contains(dr.getValue("ITEM_KEY").toString()))
                lstItem.add(dr.getValue("ITEM_KEY").toString());
        }

        lstItem.add("*");

        final String str = TextUtils.join("','", lstItem);
        String strFilter = String.format(" AND A.VENDOR_KEY IN ('%s') AND A.ITEM_KEY IN ('%s')", strVKey, str);

        //region Set BIModule
        // BIModule
        List<BModuleObject> bmObjs = new ArrayList<BModuleObject>();

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchVendorItemIQC");
        bmObj.setRequestID("BIFetchVendorItemIQC");
        bmObj.params = new Vector<ParameterInfo>();

        // Input param
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Filter);
        param1.setParameterValue(strFilter); // 要用set"Net"ParameterValue
        bmObj.params.add(param1);
        // endregion

        bmObjs.add(bmObj);

        CallBIModule(bmObjs, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(CheckBModuleReturnInfo(bModuleReturn))
                {
                    _DtQc = bModuleReturn.getReturnJsonTables().get("BIFetchVendorItemIQC").get("VENDOR_ITEM_IQC");

                    GrDetTable.addColumn(new DataColumn("SKIP_QC"));

                    for (DataRow dr : GrDetTable.Rows)
                    {
                        dr.setValue("SKIP_QC", "");

                        //region 本身供應商+本身物料
                        for (DataRow d : _DtQc.Rows)
                        {
                            if (d.getValue("VENDOR_KEY").toString().equals(strVendorKey) && d.getValue("ITEM_KEY").toString().equals(dr.getValue("ITEM_KEY").toString()))
                            {
                                dr.setValue("SKIP_QC", d.getValue("SKIP_QC").toString());
                                break;
                            }
                        }
                        //endregion

                        //region 本身供應商+*物料
                        if (dr.getValue("SKIP_QC").toString().equals(""))
                        {
                            for (DataRow d : _DtQc.Rows)
                            {
                                if (d.getValue("VENDOR_KEY").toString().equals(strVendorKey) && d.getValue("ITEM_KEY").toString().equals("*"))
                                {
                                    dr.setValue("SKIP_QC", d.getValue("SKIP_QC").toString());
                                    break;
                                }
                            }
                        }
                        //endregion

                        //region *供應商+本身物料
                        if (dr.getValue("SKIP_QC").toString().equals(""))
                        {
                            for (DataRow d : _DtQc.Rows)
                            {
                                if (d.getValue("VENDOR_KEY").toString().equals("*") && d.getValue("ITEM_KEY").toString().equals(dr.getValue("ITEM_KEY").toString()))
                                {
                                    dr.setValue("SKIP_QC", d.getValue("SKIP_QC").toString());
                                    break;
                                }
                            }
                        }
                        //endregion

                        //region *供應商+*物料
                        if (dr.getValue("SKIP_QC").toString().equals(""))
                        {
                            for (DataRow d : _DtQc.Rows)
                            {
                                if (d.getValue("VENDOR_KEY").toString().equals("*") && d.getValue("ITEM_KEY").toString().equals("*"))
                                {
                                    dr.setValue("SKIP_QC", d.getValue("SKIP_QC").toString());
                                    break;
                                }
                            }
                        }
                        //endregion

                        if (dr.getValue("SKIP_QC").toString().equals(""))
                        {
                            ShowMessage(R.string.WAPG007017, GrMstTable.Rows.get(0).getValue("VENDOR_ID").toString());
                            return;
                        }
                    }

                    ItemSkipQcOfAllLot = new HashMap<>();

                    for (DataRow dr : GrDetTable.Rows)
                    {
                        if (!ItemSkipQcOfAllLot.containsKey(dr.getValue("ITEM_ID").toString()))
                        {
                            ItemSkipQcOfAllLot.put(dr.getValue("ITEM_ID").toString(), dr.getValue("SKIP_QC").toString());
                        }
                    }

                    // Input ListView
                    /*LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    GoodReceiveListFragment.setGrLotData(GrDetTable, SeqQtyOfAllLot, SeqSkipQcOfAllLot, inflater, GrMstTable, _dtSize);

                    GrMstTable =  (DataTable)getIntent().getSerializableExtra("GrMst");
                    GrDetTable =  (DataTable)getIntent().getSerializableExtra("GrDet");
                    SeqQtyOfAllLot = new HashMap();
                    SeqQtyOfAllLot = (HashMap) getIntent().getSerializableExtra("GrrQty");
                    SeqSkipQcOfAllLot = new HashMap();
                    SeqSkipQcOfAllLot = (HashMap) getIntent().getSerializableExtra("GrrSkipQc");
                    _dtSize =  (DataTable)getIntent().getSerializableExtra("Size");
                    _dtItem =  (DataTable)getIntent().getSerializableExtra("Item");
                    GoodReceiveDataFragment.setData(GrMstTable, GrDetTable, SeqQtyOfAllLot, SeqSkipQcOfAllLot, _dtSize, getApplicationContext(), _dtItem, ItemSkipQcOfAllLot);*/

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("GrMst", GrMstTable);
                    bundle.putSerializable("GrDet", GrDetTable);
                    bundle.putSerializable("GrrQty", SeqQtyOfAllLot);
                    bundle.putSerializable("GrrSkipQc", SeqSkipQcOfAllLot);
                    bundle.putSerializable("Size", _dtSize);
                    bundle.putSerializable("Item", _dtItem);
                    bundle.putSerializable("SkuLevel", _dtSkuLevel);
                    bundle.putSerializable("ItemSkipQcOfAllLot", ItemSkipQcOfAllLot);

                    // Step01-FragmentTransaction加入兩個Fragment，暫時先隱藏另外一張Fragment:
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.add(R.id.framLayout,receiveListFragment,"ReceiveList");
                    fragmentTransaction.add(R.id.framLayout,receiveDataFragment,"ReceiveData");
                    receiveDataFragment.setArguments(bundle);
                    receiveListFragment.setArguments(bundle);
                    fragmentTransaction.hide(receiveDataFragment);
                    fragmentTransaction.commit();
                }
            }
        });
    }

    private void GetGrrDet()
    {
        List<BModuleObject> bmObjs = new ArrayList<BModuleObject>();

        // GRR DET
        BModuleObject biObj1 = new BModuleObject();
        biObj1.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        biObj1.setModuleID("BIFetchGoodReceiptReceiveDet");
        biObj1.setRequestID("BIFetchGoodReceiptReceiveDet");
        biObj1.params = new Vector<>();

        // region Set Condition
        // 裝Condition的容器
        HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
        List<Condition> lstCondition1 = new ArrayList<Condition>();

        // SHEET_ID
        Condition conditionSheetId = new Condition();
        conditionSheetId.setAliasTable("M");
        conditionSheetId.setColumnName("GR_ID");
        conditionSheetId.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
        conditionSheetId.setValue(GrMstTable.Rows.get(0).getValue("GR_ID").toString());
        lstCondition1.add(conditionSheetId);
        mapCondition.put(conditionSheetId.getColumnName(),lstCondition1);
        // endregion

        // Serialize序列化
        VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vkey, vVal);
        String strCond = msdl.generateFinalCode(mapCondition);

        // Input param
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Condition);
        param1.setNetParameterValue(strCond); // 要用set"Net"ParameterValue
        biObj1.params.add(param1);

        bmObjs.add(biObj1);

        CallBIModule(bmObjs, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn))
                {
                    DataTable GrLotTableAll = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceiptReceiveDet").get("GrrDet");

                    HashMap<String,Double> seqQtyOfAllLot = new HashMap();
                    HashMap<String,String> SeqSkipQcOfAllLot = new HashMap();

                    // 初始化seqQtyOfAllLot
                    for (DataRow dr: GrDetTable.Rows)
                    {
                        seqQtyOfAllLot.put(dr.getValue("SEQ").toString(), 0.0);
                    }
                    // 得到各項次已收料的數量總和
                    for (DataRow dr: GrLotTableAll.Rows)
                    {
                        double temp = 0.0;
                        temp = seqQtyOfAllLot.get(dr.getValue("SEQ").toString()) + Double.parseDouble(dr.getValue("QTY").toString());
                        seqQtyOfAllLot.put(dr.getValue("SEQ").toString(),temp);
                        SeqSkipQcOfAllLot.put(dr.getValue("SEQ").toString(),dr.getValue("SKIP_QC").toString());
                    }

                    SeqQtyOfAllLot = seqQtyOfAllLot;

                    // Input ListView
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    GoodReceiveListFragment.setGrLotData(GrDetTable, SeqQtyOfAllLot, SeqSkipQcOfAllLot, inflater, GrMstTable, _dtSize);
                }
            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
