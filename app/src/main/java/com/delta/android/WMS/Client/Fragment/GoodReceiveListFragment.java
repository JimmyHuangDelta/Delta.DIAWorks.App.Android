package com.delta.android.WMS.Client.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.delta.android.Core.Activity.ShowMessageEvent;
import com.delta.android.Core.DataTable.DataColumn;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.GenerateJsonNetString.MList;
import com.delta.android.Core.GenerateJsonNetString.MesClass;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionaryList;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Client.GoodReceiptReceiveActivity;
import com.delta.android.WMS.Client.GoodReceiptReceiveDetailActivity;
import com.delta.android.WMS.Client.GoodReceiptReceiveDetailNewActivity;
import com.delta.android.WMS.Client.GoodReceiptReceiveLotSnActivity;
import com.delta.android.WMS.Client.GoodReceiptReceiveLotSnNewActivity;
import com.delta.android.WMS.Client.GridAdapter.GoodNonreceiptReceiveSelectGridAdapter;
import com.delta.android.WMS.Client.GridAdapter.GoodReceiptReceiveDetaGridAdapter;
import com.delta.android.WMS.Client.GridAdapter.GoodReceiptReceiveLotAdapter;
import com.delta.android.WMS.Param.BGoodReceiptReceiveParam;
import com.delta.android.WMS.Param.BGoodReceiptReceiveWithPackingInfoParam;
import com.delta.android.WMS.Param.BIGoodReceiptReceivePortalParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.ParamObj.CheckCountObj;
import com.delta.android.WMS.Param.ParamObj.Condition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

public class GoodReceiveListFragment extends Fragment {

    static ListView GrLotData;
    private Button btnReceiveAll;

    TabLayout tabLayout;
    private String ShCfg;
    private static DataTable GrMstTable;// get p.1
    private static DataTable GrDetTable;// get p.1
    private static HashMap SeqQtyOfAllLot;    // get p.1
    private static HashMap SeqSkipQcOfAllLot;    // get p.1
    private static DataTable _dtSize;// get Parent
    private DataTable _dtSkuLevel;// get Parent
    private LinkedHashMap<String, ArrayList<String>> mapStorageBin = new LinkedHashMap<String, ArrayList<String>>(); // 有暫存區先到暫存區，沒有放入料口/If there is a temporary storage area, it goes to the temporary storage area first, and does not put it into the material port
    private LinkedHashMap<String, ArrayList<String>> mapStorageIqcBin = new LinkedHashMap<String, ArrayList<String>>(); // 存放 IQC/Deposit IQC
    private LinkedHashMap<String, ArrayList<String>> mapItemStorageBin = new LinkedHashMap<String, ArrayList<String>>();
    //private LinkedHashMap<String, String> mapStorageBinResult = new LinkedHashMap<String, String>();
    private DataTable GrLotTableAll;
    private DataTable GrrSnTableAll;
    private DataTable dtStorageItem;
    private String strStorage = "";
    private String strItem = "";

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_wms_good_receive_list_fragment, container, false);

        tabLayout = getActivity().findViewById(R.id.tabLayout);

        //region -- 控制項初始化 --
        GrLotData = view.findViewById(R.id.lvGrDetLotData);
        btnReceiveAll = view.findViewById(R.id.btnReceiveAll);
        //endregion

        //region -- 監聽事件 --
        GrLotData.setOnItemClickListener(lvSnDataOnItemClick);
        btnReceiveAll.setOnClickListener(ReceiveAll);
        //endregion

        GrMstTable = (DataTable)getFragmentManager().findFragmentByTag("ReceiveList").getArguments().getSerializable("GrMst");
        GrDetTable = (DataTable)getFragmentManager().findFragmentByTag("ReceiveList").getArguments().getSerializable("GrDet");
        SeqQtyOfAllLot = (HashMap)getFragmentManager().findFragmentByTag("ReceiveList").getArguments().getSerializable("GrrQty");
        SeqSkipQcOfAllLot = (HashMap)getFragmentManager().findFragmentByTag("ReceiveList").getArguments().getSerializable("GrrSkipQc");
        _dtSize = (DataTable)getFragmentManager().findFragmentByTag("ReceiveList").getArguments().getSerializable("Size");
        _dtSkuLevel = (DataTable)getFragmentManager().findFragmentByTag("ReceiveList").getArguments().getSerializable("SkuLevel");

        GoodReceiptReceiveDetaGridAdapter adapter = new GoodReceiptReceiveDetaGridAdapter(GrDetTable, SeqQtyOfAllLot, SeqSkipQcOfAllLot, inflater);
        GrLotData.setAdapter(adapter);

        /*GrLotTable =  new DataTable();
        DataColumn dcLotID = new DataColumn("LOT_ID");
        DataColumn dcQty = new DataColumn("QTY");
        DataColumn dcUom = new DataColumn("UOM");
        DataColumn dcCmt = new DataColumn("CMT");
        DataColumn dcMfgDate = new DataColumn("MFG_DATE");
        DataColumn dcExpDAte = new DataColumn("EXP_DATE");

        GrLotTable.addColumn(dcLotID);
        GrLotTable.addColumn(dcQty);
        GrLotTable.addColumn(dcUom);
        GrLotTable.addColumn(dcMfgDate);
        GrLotTable.addColumn(dcExpDAte);

        DataRow row = GrLotTable.newRow();
        row.setValue("LOT_ID", "AAA");
        row.setValue("QTY", "1000");
        row.setValue("UOM", "");
        row.setValue("CMT", "");
        row.setValue("MFG_DATE", "2022-11-08");
        row.setValue("EXP_DATE", "2023-11-08");
        GrLotTable.Rows.add(row);

        row = GrLotTable.newRow();
        row.setValue("LOT_ID", "BBB");
        row.setValue("QTY", "500");
        row.setValue("UOM", "");
        row.setValue("CMT", "");
        row.setValue("MFG_DATE", "2022-11-08");
        row.setValue("EXP_DATE", "2023-11-08");
        GrLotTable.Rows.add(row);

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        GoodReceiptReceiveLotAdapter adapter = new GoodReceiptReceiveLotAdapter(GrLotTable, layoutInflater);
        GrLotData.setAdapter(adapter);*/

        return view;
    }

    private AdapterView.OnItemClickListener lvSnDataOnItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final DataRow chooseRow = GrDetTable.Rows.get(position);

            final double chooseSeq = Double.parseDouble(chooseRow.getValue("SEQ").toString());

            // region Set BIModule
            // List of BIModules
            List<BModuleObject> bmObjs = new ArrayList<BModuleObject>();

            // GRR DET
            BModuleObject biObj1 = new BModuleObject();
            biObj1.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
            biObj1.setModuleID("BIFetchGoodReceiptReceiveDet");
            biObj1.setRequestID("BIFetchGoodReceiptReceiveDet");
            biObj1.params = new Vector<>();
            // GRR SN
            BModuleObject biObj2 = new BModuleObject();
            biObj2.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
            biObj2.setModuleID("BIFetchGoodReceiptReceiveSN");
            biObj2.setRequestID("BIFetchGoodReceiptReceiveSN");
            biObj2.params = new Vector<>();
            // WmsConfug
            BModuleObject biObj3 = new BModuleObject();
            biObj3.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
            biObj3.setModuleID("BIFetchWmsSheetConfig");
            biObj3.setRequestID("FetchWmsSheetConfig");
            biObj3.params = new Vector<>();

            // region Set Condition
            // 裝Condition的容器
            HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
            List<Condition> lstCondition1 = new ArrayList<Condition>();

            // SHEET_ID
            Condition conditionSheetId = new Condition();
            conditionSheetId.setAliasTable("M");
            conditionSheetId.setColumnName("GR_ID");
            conditionSheetId.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
            conditionSheetId.setValue(chooseRow.getValue("GR_ID").toString());
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
            biObj2.params.add(param1);

            ParameterInfo param2 = new ParameterInfo();
            param2.setParameterID(BIWMSFetchInfoParam.Filter);
            String str = String.format(" AND CFG.STORAGE_ACTION_TYPE ='To' AND TYP.SHEET_TYPE_KEY = '%s' ",GrMstTable.Rows.get(0).getValue("GR_TYPE_KEY").toString());
            param2.setParameterValue(str);
            biObj3.params.add(param2);

            // GRR DET
            BModuleObject biObj4 = new BModuleObject();
            biObj4.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIGoodReceiptReceivePortal");
            biObj4.setModuleID("BIFetchGoodReceiptReceiveInfo");
            biObj4.setRequestID("BIFetchGoodReceiptReceiveInfo");
            biObj4.params = new Vector<>();

            // Input param
            ParameterInfo paramGrId = new ParameterInfo();
            paramGrId.setParameterID(BIGoodReceiptReceivePortalParam.GrId);
            paramGrId.setParameterValue(GrMstTable.Rows.get(0).getValue("GR_ID").toString());
            biObj4.params.add(paramGrId);

            bmObjs.add(biObj1);
            bmObjs.add(biObj2);
            bmObjs.add(biObj3);
            bmObjs.add(biObj4);
            // endregion

            final DataTable dtChooseDetItem = new DataTable();
            dtChooseDetItem.Rows.add(chooseRow);

            ((GoodReceiptReceiveDetailNewActivity)getActivity()).CallBIModule(bmObjs, new WebAPIClientEvent() {
                @Override
                public void onPostBack(BModuleReturn bModuleReturn) {
                    if (((GoodReceiptReceiveDetailNewActivity)getActivity()).CheckBModuleReturnInfo(bModuleReturn)) {
                        DataTable dtLot = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceiptReceiveDet").get("GrrDet");
                        DataTable dtSN = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceiptReceiveSN").get("GrrSn");
                        DataTable dtShtCfg = bModuleReturn.getReturnJsonTables().get("FetchWmsSheetConfig").get("SBRM_WMS_SHEET_CONFIG");
                        DataTable dtMerge = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceiptReceiveInfo").get("MERGE");
                        DataTable dtWgr = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceiptReceiveInfo").get("WGR");
                        DataTable dtMgr = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceiptReceiveInfo").get("MGR");

                        if (dtShtCfg.Rows.size()<=0)
                        {
                            ((GoodReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG007011,GrMstTable.Rows.get(0).getValue("GR_ID").toString());
                            return;
                        }
                        if (chooseRow.getValue("REGISTER_TYPE").toString().equals(""))
                        {
                            ((GoodReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG007012,chooseRow.getValue("ITEM_ID").toString());
                            return;
                        }

                        DataTable dtChooseRowsMst = new DataTable();
                        for (DataRow dr: GrMstTable.Rows)
                        {
                            if (dr.getValue("GR_ID").toString().equals(chooseRow.getValue("GR_ID").toString()))
                            {
                                dtChooseRowsMst.Rows.add(dr);
                                break;
                            }
                        }
                        dtChooseRowsMst.getColumns().addAll(GrMstTable.getColumns());

                        DataTable dtChooseRowsLot = new DataTable();
//                        for (DataRow dr : dtLot.Rows){
//                            if (dr.getValue("ITEM_ID").toString().equals(chooseRow.getValue("ITEM_ID").toString()) &&
//                                    Double.parseDouble(dr.getValue("SEQ").toString()) == Double.parseDouble(chooseRow.getValue("SEQ").toString())){
//                                dtChooseRowsLot.Rows.add(dr);
//                            }
//                        }
                        for (DataRow dr : dtMerge.Rows){
                            if (dr.getValue("ITEM_ID").toString().equals(chooseRow.getValue("ITEM_ID").toString()) &&
                                    Double.parseDouble(dr.getValue("SEQ").toString()) == Double.parseDouble(chooseRow.getValue("SEQ").toString())){
                                dtChooseRowsLot.Rows.add(dr);
                            }
                        }
                        dtChooseRowsLot.getColumns().addAll( dtLot.getColumns() );

                        ShCfg = dtShtCfg.Rows.get(0).getValue("ACTUAL_QTY_STATUS").toString();

                        // Set SkipQC by checkbox
                        String strSkipQC = chooseRow.getValue("SKIP_QC").toString();
                        Boolean flag =false;
                        for (DataRow dr: dtLot.Rows)
                        {
                            if (dr.getValue("SEQ").toString().equals(chooseRow.getValue("SEQ").toString()))
                            {
                                if (strSkipQC.equals("Y"))
                                {
                                    dr.setValue("SKIP_QC","Y");
                                    flag =true;
                                }
                                else if (!strSkipQC.equals("Y"))
                                {
                                    dr.setValue("SKIP_QC","N");
                                    flag =true;
                                }
                            }
                        }
                        // 若某一筆(某一SEQ)的GRDet完全沒有lot(GRRDet)的話，先記錄skipQc
                        if(!flag)
                        {
                            if (strSkipQC.equals("Y"))
                            {
                                strSkipQC = "Y";
                            }
                            else if (!strSkipQC.equals("Y"))
                            {
                                strSkipQC = "N";
                            }
                        }

                        Bundle chooseGrDet = new Bundle();
                        chooseGrDet.putSerializable("MstTable", dtChooseRowsMst);
                        chooseGrDet.putSerializable("DetRow", chooseRow);
                        chooseGrDet.putSerializable("DetLotTable", dtChooseRowsLot);
                        chooseGrDet.putSerializable("LotTableAll", dtLot);
                        chooseGrDet.putSerializable("DetLotSnTable", dtSN);
                        chooseGrDet.putString("ActualQtyStatus",ShCfg);
                        chooseGrDet.putString("RegType",chooseRow.getValue("REGISTER_TYPE").toString());
                        chooseGrDet.putString("DetItemTotalQty",chooseRow.getValue("QTY").toString());
                        chooseGrDet.putString("strSkipQC", strSkipQC);
                        chooseGrDet.putSerializable("SizeTable", _dtSize);
                        chooseGrDet.putSerializable("SkuLevel", _dtSkuLevel);
                        chooseGrDet.putDouble("chooseSeq", chooseSeq);
                        //gotoNextActivity(GoodReceiptReceiveLotSnActivity.class, chooseGrDet);
                        Intent intent = new Intent(getActivity(), GoodReceiptReceiveLotSnNewActivity.class);
                        intent.putExtras(chooseGrDet);
                        startActivity(intent);
                    }
                }
            });
            /*((GoodReceiptReceiveDetailNewActivity) getActivity()).changeFragment(0, GrLotTable.Rows.get(position), GrLotSnTable);
            tabLayout.getTabAt(0).select();*/
        }
    };

    private View.OnClickListener ReceiveAll = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Get -> Check -> ExecuteProcess
            GetLotAndSn();
        }
    };

    public static void setGrLotData(DataTable filterTable, HashMap<String,Double> mapSeqQtyOfAllLot, HashMap<String,String> mapSeqSkipQcOfAllLot, LayoutInflater inflater,
                                    DataTable GrMst, DataTable dtSize)
    {
        GrMstTable = GrMst;
        GrDetTable = filterTable;
        SeqQtyOfAllLot = mapSeqQtyOfAllLot;
        SeqSkipQcOfAllLot = mapSeqSkipQcOfAllLot;
        _dtSize = dtSize;
        GoodReceiptReceiveDetaGridAdapter adapter = new GoodReceiptReceiveDetaGridAdapter(filterTable, mapSeqQtyOfAllLot, mapSeqSkipQcOfAllLot, inflater);
        GrLotData.setAdapter(adapter);
    }

    private void GetLotAndSn()
    {
        // region Set BIModule
        // List of BIModules
        List<BModuleObject> bmObjs = new ArrayList<BModuleObject>();

        // GRR DET
        BModuleObject biObj1 = new BModuleObject();
        biObj1.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        biObj1.setModuleID("BIFetchGoodReceiptReceiveDet");
        biObj1.setRequestID("BIFetchGoodReceiptReceiveDet");
        biObj1.params = new Vector<>();
        // GRR SN
        BModuleObject biObj2 = new BModuleObject();
        biObj2.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        biObj2.setModuleID("BIFetchGoodReceiptReceiveSN");
        biObj2.setRequestID("BIFetchGoodReceiptReceiveSN");
        biObj2.params = new Vector<>();
        // WmsConfug
        BModuleObject biObj3 = new BModuleObject();
        biObj3.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        biObj3.setModuleID("BIFetchWmsSheetConfig");
        biObj3.setRequestID("FetchWmsSheetConfig");
        biObj3.params = new Vector<>();
        // Bin
        BModuleObject biObj4 = new BModuleObject();
        biObj4.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        biObj4.setModuleID("BIFetchBin");
        biObj4.setRequestID("BIFetchBin");
        biObj4.params = new Vector<>();
        // GRR WGR
//        BModuleObject biObj5 = new BModuleObject();
//        biObj5.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIGoodReceiptReceivePortal");
//        biObj5.setModuleID("BIFetchGoodReceiptReceiveInfo");
//        biObj5.setRequestID("BIFetchGoodReceiptReceiveInfo");
//        biObj5.params = new Vector<>();

//        bmObjs.add(biObj1);
//        bmObjs.add(biObj2);
//        bmObjs.add(biObj3);
//        bmObjs.add(biObj4);
//        bmObjs.add(biObj5);

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
        biObj2.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIWMSFetchInfoParam.Filter);
        String str = String.format(" AND CFG.STORAGE_ACTION_TYPE ='To' AND TYP.SHEET_TYPE_KEY = '%s' ",GrMstTable.Rows.get(0).getValue("GR_TYPE_KEY").toString());
        param2.setParameterValue(str);
        biObj3.params.add(param2);

        mapStorageBin = new LinkedHashMap<String, ArrayList<String>>();
        mapStorageIqcBin = new LinkedHashMap<String, ArrayList<String>>();
        final ArrayList<String> lstStorage = new ArrayList<String>();
        for(DataRow dr : GrDetTable.Rows){
            String temp = dr.getValue("STORAGE_ID").toString();
            if(!lstStorage.contains(temp)){
                lstStorage.add(temp);
            }
        }
        //裝Condition的容器
        final HashMap<String, List<?>> mapCondition2 = new HashMap<String, List<?>>();
        List<Condition> lstCondition = new ArrayList<Condition>();
        for(int i = 0; i < lstStorage.size(); i++)
        {
            Condition cond = new Condition();
            cond.setAliasTable("S");
            cond.setColumnName("STORAGE_ID");
            cond.setValue(lstStorage.get(i));
            cond.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
            lstCondition.add(cond);
        }
        mapCondition2.put("STORAGE_ID", lstCondition);

        //Serialize序列化
        VirtualClass vKey2 = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal2 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
        MesSerializableDictionaryList msdl2 = new MesSerializableDictionaryList(vKey2, vVal2);
        String strCond2 = msdl2.generateFinalCode(mapCondition2);

        //Input param
        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BIWMSFetchInfoParam.Condition);
        param3.setNetParameterValue(strCond2);
        biObj4.params.add(param3);

        boolean bSkipQC = false;
        for(DataRow dr : GrDetTable.Rows){
            String strQC = dr.getValue("SKIP_QC").toString();
            if(strQC.equals("N")){
                bSkipQC = false;
                break;
            } else if (strQC.equals("Y")) {
                bSkipQC = true;
            }
        }

        ParameterInfo param4 = new ParameterInfo();
        param4.setParameterID(BIWMSFetchInfoParam.Filter);
        if(bSkipQC)
            param4.setParameterValue("AND B.BIN_TYPE IN ('IT', 'IS')");
        else
            param4.setParameterValue("AND B.BIN_TYPE IN ('IT', 'IS', 'IQC')");
        biObj4.params.add(param4);

        bmObjs.add(biObj1);
        bmObjs.add(biObj2);
        bmObjs.add(biObj3);
        bmObjs.add(biObj4);
        // endregion

        final boolean finalBSkipQC = bSkipQC;
        ((GoodReceiptReceiveDetailNewActivity)getActivity()).CallBIModule(bmObjs, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (((GoodReceiptReceiveDetailNewActivity)getActivity()).CheckBModuleReturnInfo(bModuleReturn)) {
                    GrLotTableAll = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceiptReceiveDet").get("GrrDet");
                    GrrSnTableAll = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceiptReceiveSN").get("GrrSn");
                    DataTable dtShtCfg = bModuleReturn.getReturnJsonTables().get("FetchWmsSheetConfig").get("SBRM_WMS_SHEET_CONFIG");
                    DataTable dtStorageTempBin = bModuleReturn.getReturnJsonTables().get("BIFetchBin").get("BIN");
//                    DataTable dtMerge = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceiptReceiveInfo").get("MERGE");
//                    DataTable dtWgr = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceiptReceiveInfo").get("WGR");
//                    DataTable dtMgr = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceiptReceiveInfo").get("MGR");

                    if (GrLotTableAll.Rows.size()<=0)
                    {
                        ((GoodReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG007016);
                        return;
                    }
                    if (dtShtCfg.Rows.size()<=0)
                    {
                        ((GoodReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG007011,GrMstTable.Rows.get(0).getValue("GR_ID").toString());
                        return;
                    }
                    ShCfg = dtShtCfg.Rows.get(0).getValue("ACTUAL_QTY_STATUS").toString();

                    for (DataRow dr : GrLotTableAll.Rows)
                    {
                        if (!dr.getValue("MFG_DATE").toString().equals(""))
                        {
                            dr.setValue("MFG_DATE", dr.getValue("MFG_DATE").toString().substring(0,10) + " 00:00:00");
                        }

                        if (!dr.getValue("EXP_DATE").toString().equals(""))
                        {
                            dr.setValue("EXP_DATE", dr.getValue("EXP_DATE").toString().substring(0,10) + " 23:59:59");
                        }
                    }

                    if(dtStorageTempBin.Rows.size() <= 0){
                        ((GoodReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG007018); //WAPG007018 查無對應的入庫儲位
                        return;
                    }

                    // 有IQC，存放IQC/Have IQC, store IQC
                    for (DataRow dr : dtStorageTempBin.Rows) {
                        if(!mapStorageIqcBin.containsKey(dr.getValue("STORAGE_ID").toString()))
                        {
                            ArrayList<String> lstBin = new ArrayList<>();

                            if(dr.getValue("BIN_TYPE").toString().equals("IQC"))
                                lstBin.add(dr.getValue("BIN_ID").toString());

                            mapStorageIqcBin.put(dr.getValue("STORAGE_ID").toString(), lstBin);

                        }
                        else {

                            if(dr.getValue("BIN_TYPE").toString().equals("IQC"))
                                mapStorageIqcBin.get(dr.getValue("STORAGE_ID").toString()).add(dr.getValue("BIN_ID").toString());
                        }
                    }

                    if (finalBSkipQC == false) {

                        for (String key: mapStorageIqcBin.keySet()) {

                            List<String> lstBin = mapStorageIqcBin.get(key);
                            if (lstBin.size() == 0) {

                                Object[] args = new Object[1];
                                args[0] = key;

                                ((GoodReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009031, args); //WAPG009031 倉庫[%s]未設定進料檢驗儲位
                                return;
                            }
                        }
                    }

                    // 有暫存區，就直接到暫存區/If there is a temporary storage area, go directly to the temporary storage area
                    for (DataRow dr : dtStorageTempBin.Rows) {

                        if(!mapStorageBin.containsKey(dr.getValue("STORAGE_ID").toString()))
                        {
                            ArrayList<String> lstBin = new ArrayList<>();

                            if(dr.getValue("BIN_TYPE").toString().equals("IT"))
                                lstBin.add(dr.getValue("BIN_ID").toString());

                            mapStorageBin.put(dr.getValue("STORAGE_ID").toString(), lstBin);
                        }
                        else
                        {
                            if(dr.getValue("BIN_TYPE").toString().equals("IT"))
                                mapStorageBin.get(dr.getValue("STORAGE_ID").toString()).add(dr.getValue("BIN_ID").toString());
                        }
                    }

                    for (String key: mapStorageBin.keySet()) {

                        if (mapStorageBin.get(key).size() <= 0) {

                            for (DataRow dr : dtStorageTempBin.Rows) {

                                if (dr.getValue("STORAGE_ID").toString().equals(key)) {

                                    if(dr.getValue("BIN_TYPE").toString().equals("IS"))
                                        mapStorageBin.get(key).add(dr.getValue("BIN_ID").toString());
                                }
                            }
                        }
                    }

                    for (String key: mapStorageBin.keySet()) {

                        List<String> lstBin = mapStorageBin.get(key);
                        if (lstBin.size() == 0) {

                            Object[] args = new Object[1];
                            args[0] = key;

                            ((GoodReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009030, args); //WAPG009030 倉庫[%s]未設定入庫暫存區或入料口
                            return;
                        }
                    }

                    ShowConfirmDialog();
                    //Check
                    //CheckAllConstraint();
                }
            }
        });
    }

    private void ShowConfirmDialog(){
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View viewConfirm = inflater.inflate(R.layout.activity_good_nonreceipt_receive_confirm, null);
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setView(viewConfirm);

        final android.app.AlertDialog dialogConfirm = builder.create();
        dialogConfirm.setCancelable(false);
        dialogConfirm.show();

        //取得控制項
        final ListView lvStorageItem = dialogConfirm.findViewById(R.id.lvReceiveLot);
        Button btnSelectConfirm = dialogConfirm.findViewById(R.id.btnBinConfirm);

        //整理資料(by 倉庫+物料)
        ArrayList<String> lstTemp = new ArrayList<String>();
        dtStorageItem = new DataTable();
        DataColumn dcItemId = new DataColumn("ITEM_ID");
        DataColumn dcItemName = new DataColumn("ITEM_NAME");
        DataColumn dcStorage = new DataColumn("STORAGE_ID");
        DataColumn dcBin = new DataColumn("BIN_ID");

        dtStorageItem.addColumn(dcItemId);
        dtStorageItem.addColumn(dcItemName);
        dtStorageItem.addColumn(dcStorage);
        dtStorageItem.addColumn(dcBin);

        mapItemStorageBin = new LinkedHashMap<>();

        for(DataRow dr : GrDetTable.Rows) {
            String strStorageId = dr.getValue("STORAGE_ID").toString();
            String strItemId = dr.getValue("ITEM_ID").toString();
            String strQC = dr.getValue("SKIP_QC").toString();
            String strSelectKey = strStorageId + "_" + strItemId;

            if(!lstTemp.contains(strSelectKey)){
                lstTemp.add(strSelectKey);
                DataRow drNew = dtStorageItem.newRow();
                drNew.setValue("ITEM_ID", dr.getValue("ITEM_ID").toString());
                drNew.setValue("ITEM_NAME", dr.getValue("ITEM_NAME").toString());
                drNew.setValue("STORAGE_ID", dr.getValue("STORAGE_ID").toString());
                drNew.setValue("BIN_ID", "");
                dtStorageItem.Rows.add(drNew);

                if (strQC.equals("Y"))
                    mapItemStorageBin.put(strSelectKey, mapStorageBin.get(strStorageId));
                else
                    mapItemStorageBin.put(strSelectKey, mapStorageIqcBin.get(strStorageId));
            }

        }

        LayoutInflater inflaterSelect = LayoutInflater.from(getContext());
        GoodNonreceiptReceiveSelectGridAdapter adapterSelect = new GoodNonreceiptReceiveSelectGridAdapter(dtStorageItem, inflaterSelect);
        lvStorageItem.setAdapter(adapterSelect);
        adapterSelect.notifyDataSetChanged();

        lvStorageItem.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                LayoutInflater inflaterBin = LayoutInflater.from(getActivity());
                final View viewBin = inflaterBin.inflate(R.layout.activity_good_nonreceipt_receive_confirm_select_bin, null);
                final android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.Builder(getContext());
                builder1.setView(viewBin);

                final android.app.AlertDialog dialogBin = builder1.create();
                dialogBin.setCancelable(false);
                dialogBin.show();

                final Spinner cmbBin = dialogBin.findViewById(R.id.cmbBinID);
                final Button btnBinConfirm = dialogBin.findViewById(R.id.btnBinConfirm);

                strStorage = dtStorageItem.getValue(position, "STORAGE_ID").toString();
                strItem = dtStorageItem.getValue(position, "ITEM_ID").toString();
                ArrayAdapter<String> adapterBin = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, mapItemStorageBin.get(strStorage + "_" + strItem)); //mapStorageBin.get(strStorage)
                cmbBin.setAdapter(adapterBin);

                cmbBin.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l){
                        //dtStorageItem.setValue(position, "BIN_ID", mapStorageBin.get(strStorage).get(position));
                        for(DataRow dr : dtStorageItem.Rows){
                            if(dr.getValue("STORAGE_ID").toString().equals(strStorage) &&
                                    dr.getValue("ITEM_ID").toString().equals(strItem)){
                                dr.setValue("BIN_ID", mapItemStorageBin.get(strStorage + "_" + strItem).get(position)); //mapStorageBin.get(strStorage).get(position)
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView){
                        //do nothing
                    }
                });

                btnBinConfirm.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        //refreash listview
                        LayoutInflater inflaterSelect = LayoutInflater.from(getContext());
                        GoodNonreceiptReceiveSelectGridAdapter adapterSelect = new GoodNonreceiptReceiveSelectGridAdapter(dtStorageItem, inflaterSelect);
                        lvStorageItem.setAdapter(adapterSelect);

                        dialogBin.dismiss();
                    }
                });
            }
        });

        btnSelectConfirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                for(DataRow dr : dtStorageItem.Rows){
                    if(dr.getValue("BIN_ID").toString().equals("")){
                        Object[] args = new Object[2];
                        args[0] = dr.getValue("STORAGE_ID").toString();
                        args[1] = dr.getValue("ITEM_ID").toString();
                        ((GoodReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009018, args); //WAPG009018    尚未選擇倉庫[%s]物料[%s]對應儲位
                        return;
                    }
                }
                dialogConfirm.dismiss();

                //帶入TEMP_BIN
                if(!GrLotTableAll.getColumns().contains("TEMP_BIN")){
                    DataColumn dcTempBin = new DataColumn("TEMP_BIN");
                    GrLotTableAll.addColumn(dcTempBin);
                }

                for(DataRow drLot : GrLotTableAll.Rows){
                    String storage = drLot.getValue("STORAGE_ID").toString();
                    String itemid = drLot.getValue("ITEM_ID").toString();
                    for (DataRow dr : dtStorageItem.Rows){
                        if(dr.getValue("STORAGE_ID").toString().equals(storage) &&
                                dr.getValue("ITEM_ID").toString().equals(itemid)){
                            drLot.setValue("TEMP_BIN", dr.getValue("BIN_ID").toString());
//                            if(!mapStorageBinResult.containsKey(storage)){
//                                mapStorageBinResult.put(storage, dr.getValue("BIN_ID").toString());
//                            }
                        }
                    }
                }

                //CreateSheetID();
                CheckAllConstraint();
            }
        });
    }

    private void CheckAllConstraint() {
        // 得到各項次已收料的數量總和
        HashMap<String,Double> SeqQtyOfAllLot = new HashMap();
        for (DataRow dr: GrLotTableAll.Rows)
        {
            if(SeqQtyOfAllLot.get(dr.getValue("SEQ").toString()) == null)
            {
                double temp = 0;
                temp+=Double.parseDouble(dr.getValue("QTY").toString());
                SeqQtyOfAllLot.put(dr.getValue("SEQ").toString(),temp);
            }
            else {
                double temp = 0;
                temp = SeqQtyOfAllLot.get(dr.getValue("SEQ").toString()) + Double.parseDouble(dr.getValue("QTY").toString());
                SeqQtyOfAllLot.put(dr.getValue("SEQ").toString(),temp);
            }
        }

        // 20200804 archie 檢查每個項次是否都有收料
        for (DataRow d : GrDetTable.Rows)
        {
            if (!SeqQtyOfAllLot.containsKey(d.getValue("SEQ").toString()))
            {
                String str = String.format("%s[%s]%s", this.getResources().getString(R.string.SEQ), d.getValue("SEQ").toString(),this.getResources().getString(R.string.WAPG007016));
                ((GoodReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(str);
                return;
            }
        }

        // 檢查單據設定
        for (DataRow dr: GrDetTable.Rows)
        {
            switch (ShCfg)
            {
                case "More":
                    if(Double.parseDouble(dr.getValue("QTY").toString()) > SeqQtyOfAllLot.get(dr.getValue("SEQ").toString()))
                    {
                        ((GoodReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG007014,
                                GrMstTable.Rows.get(0).getValue("GR_ID").toString(),
                                dr.getValue("SEQ").toString(),
                                SeqQtyOfAllLot.get(dr.getValue("SEQ").toString()).toString(),
                                dr.getValue("QTY").toString());
                        return;
                    }
                    break;
                case "Less":
                    if (Double.parseDouble(dr.getValue("QTY").toString()) < SeqQtyOfAllLot.get(dr.getValue("SEQ").toString()))
                    {
                        ((GoodReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG007013,
                                GrMstTable.Rows.get(0).getValue("GR_ID").toString(),
                                dr.getValue("SEQ").toString(),
                                SeqQtyOfAllLot.get(dr.getValue("SEQ").toString()).toString(),
                                dr.getValue("QTY").toString());
                        return;
                    }
                    break;

                case "Equal":
                    if (Double.parseDouble(dr.getValue("QTY").toString()) != SeqQtyOfAllLot.get(dr.getValue("SEQ").toString()))
                    {
                        ((GoodReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG007015,
                                GrMstTable.Rows.get(0).getValue("GR_ID").toString(),
                                dr.getValue("SEQ").toString(),
                                SeqQtyOfAllLot.get(dr.getValue("SEQ").toString()).toString(),
                                dr.getValue("QTY").toString());
                        return;
                    }
                    break;
            }
        }

        // Set SkipQC by checkbox
        String seq = "1.0";
        for (int i =0;i<GrDetTable.Rows.size();i++)
        {
            String strSkipQC = GrDetTable.Rows.get(i).getValue("SKIP_QC").toString();
            //Boolean skipQc = ((CheckBox)holder.GrDetData.getAdapter().getItem(i)).isChecked();
            for (DataRow dr: GrLotTableAll.Rows)
            {
                if (!dr.getValue("SEQ").toString().equals(seq))
                {
                    continue;
                }

                dr.setValue("SKIP_QC",strSkipQC);
            }
            if(i+1<GrDetTable.Rows.size()) seq = GrDetTable.Rows.get(i+1).getValue("SEQ").toString();
        }

        ExecuteProcess();
    }

    private void ExecuteProcess()
    {
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIGoodReceiptReceivePortal");
        bmObj.setModuleID("BISetReceiptExtend");
        bmObj.setRequestID("BISetReceiptExtend");
        bmObj.params = new Vector<ParameterInfo>();

        BGoodReceiptReceiveWithPackingInfoParam sheet = new BGoodReceiptReceiveWithPackingInfoParam();
        BGoodReceiptReceiveWithPackingInfoParam.GrrWithPackingInfoMasterObj sheet1 = sheet.new GrrWithPackingInfoMasterObj();
        BGoodReceiptReceiveWithPackingInfoParam.GrrWithPackingInfoMasterObj sheetTemp = sheet1.GetGrrSheet(GrMstTable, GrLotTableAll, GrrSnTableAll);

        //如果物料為PcsSN類別的，在收料及收料完成時需要檢查是否有批號及序號。
        for(BGoodReceiptReceiveWithPackingInfoParam.GrrWithPackingInfoDetObj obj : sheetTemp.getGrDetails()){
            String itemId = obj.getItemId();
            DataRow drTemp = null;
            for(DataRow dr : GrDetTable.Rows){
                if(dr.getValue("ITEM_ID").toString().equals(itemId)) {
                    drTemp = dr;
                    break;
                }
            }
            if(drTemp == null){
                Object[] args = new Object[1];
                args[0] = itemId;
                ((GoodReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG007019, args); //WAPG007019 查無物料[%s]明細
                return;
            }
            if(!drTemp.getValue("REGISTER_TYPE").toString().equals("PcsSN")) continue;

            if(obj.getGrSns().size() <= 0){
                Object[] args = new Object[2];
                args[0] = itemId;
                args[1] = "PcsSN";
                ((GoodReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG007020, args); //WAPG007020 物料[%s]為[%s]管控，未輸入SN
                return;
            }
        }

        // region 儲存盤點狀態檢查物件
        List<CheckCountObj> lstChkCountObj = new ArrayList<>();
        for (DataRow dr : GrLotTableAll.Rows) {
            CheckCountObj chkCountObj = new CheckCountObj();
            chkCountObj.setStorageId(dr.getValue("STORAGE_ID").toString());
            chkCountObj.setItemId(dr.getValue("ITEM_ID").toString());
            chkCountObj.setBinId(dr.getValue("TEMP_BIN").toString());
            lstChkCountObj.add(chkCountObj);
        }
        // endregion

        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.INV.Parameter.GrrWithPackingInfoMasterObj", "bmWMS.INV.Param");
        MesClass mesClassEnum = new MesClass(vListEnum);
        String strGrrMstObj = mesClassEnum.generateFinalCode(sheet1.GetGrrSheet(GrMstTable, GrLotTableAll, GrrSnTableAll));

        VirtualClass vListEnum2 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.CheckCountObj", "bmWMS.Library.Param");
        MList mListEnum2 = new MList(vListEnum2);
        String strCheckCountObj = mListEnum2.generateFinalCode(lstChkCountObj);

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BGoodReceiptReceiveWithPackingInfoParam.TrxType);
        param1.setParameterValue("Confirmed");
        bmObj.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BGoodReceiptReceiveWithPackingInfoParam.GrrMasterObj);
        param2.setNetParameterValue(strGrrMstObj);// setNetParameterValue2?
        bmObj.params.add(param2);

        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BGoodReceiptReceiveWithPackingInfoParam.ExecuteCheckStock); // 20220804 Add by Ikea 是否執行盤點檢查
        param3.setParameterValue("true");
        bmObj.params.add(param3);

        ParameterInfo param4 = new ParameterInfo(); // 20220804 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中
        param4.setParameterID(BGoodReceiptReceiveWithPackingInfoParam.CheckCountObj);
        param4.setNetParameterValue(strCheckCountObj);
        bmObj.params.add(param4);

        ParameterInfo param5 = new ParameterInfo();
        param5.setParameterID(BIGoodReceiptReceivePortalParam.GrId);
        param5.setParameterValue(GrMstTable.Rows.get(0).getValue("GR_ID").toString());
        bmObj.params.add(param5);

        ((GoodReceiptReceiveDetailNewActivity)getActivity()).CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (((GoodReceiptReceiveDetailNewActivity)getActivity()).CheckBModuleReturnInfo(bModuleReturn)) {
                    ((GoodReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG007010, new ShowMessageEvent() {
                        @Override
                        public void onDismiss() {
                            //getActivity().gotoPreviousActivity(GoodReceiptReceiveActivity.class, true);
                            Intent intent = new Intent(getActivity(), GoodReceiptReceiveActivity.class);
                            startActivity(intent);
                        }
                    });
                }
            }
        });
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
