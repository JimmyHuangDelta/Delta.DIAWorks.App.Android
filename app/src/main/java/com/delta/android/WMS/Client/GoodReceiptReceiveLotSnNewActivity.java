package com.delta.android.WMS.Client;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

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
import com.delta.android.WMS.Client.GridAdapter.GoodReceiptReceiveLotAdapter;
import com.delta.android.WMS.Param.BIGoodReceiptReceivePortalParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.ParamObj.Condition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GoodReceiptReceiveLotSnNewActivity extends BaseFlowActivity {

    private static int requestCode = 123; //需要接收回傳資訊時使用
    private DataTable GrMstTable;// get p.2
    private HashMap GrDetMap;    // get p.2
    private DataTable GrLotTable;// get p.2
    private DataTable GrLotTableAll;// get p.2
    private DataTable GrrSnTable;// get p.2
    private String ShCfg;// get p.2
    private String RegType;// get p.2
    private String DetItemTotalQty;// get p.2
    private String strSkipQC;// get p.2
    double receiveCount;
    private DataTable _dtSize;// get p.2
    private DataTable _dtSkuLevel; // get p.2
    private double chooseSeq; // get p.2

    // 宣告控制項物件
    TextView GrId;
    TextView ItemId;
    TextView ItemTotalQty;
    ListView GrLotData;
    Button BtnAddLot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_good_receipt_receive_lot_sn_new);

        this.initialData();
        // 設定監聽事件
        setListensers();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent){
        super.onActivityResult(requestCode, resultCode, dataIntent);
        GetGrrData();

        //gotoPreviousActivity(GoodReceiptReceiveDetailActivity.class);
    }

    private void initialData() {
        GrMstTable =  (DataTable)getIntent().getSerializableExtra("MstTable");
        GrDetMap = new HashMap();
        GrDetMap = (HashMap) getIntent().getSerializableExtra("DetRow");
        GrLotTable =  (DataTable)getIntent().getSerializableExtra("DetLotTable");
        GrLotTableAll = (DataTable)getIntent().getSerializableExtra("LotTableAll");
        GrrSnTable =  (DataTable)getIntent().getSerializableExtra("DetLotSnTable");
        ShCfg = getIntent().getStringExtra("ActualQtyStatus");
        RegType = getIntent().getStringExtra("RegType");
        DetItemTotalQty = getIntent().getStringExtra("DetItemTotalQty");
        strSkipQC = getIntent().getStringExtra("strSkipQC");
        _dtSize =  (DataTable)getIntent().getSerializableExtra("SizeTable");
        _dtSkuLevel = (DataTable)getIntent().getSerializableExtra("SkuLevel");
        chooseSeq = getIntent().getDoubleExtra("chooseSeq", 0);

        // 取得控制項物件
        GrId = findViewById(R.id.tvGrId);
        ItemId = findViewById(R.id.tvGrDetItemId);
        ItemTotalQty = findViewById(R.id.tvGrDetItemTotalQty);
        GrLotData = findViewById(R.id.lvGrDetLotData);

        GrId.setText(GrDetMap.get("GR_ID").toString());
        ItemId.setText(GrDetMap.get("ITEM_ID").toString());
        receiveCount = 0;
        for (DataRow dr : GrLotTable.Rows) receiveCount += Double.parseDouble(dr.getValue("QTY").toString());
        ItemTotalQty.setText(String.format("%s/%s", String.valueOf(receiveCount), DetItemTotalQty));

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        GoodReceiptReceiveLotAdapter adapter = new GoodReceiptReceiveLotAdapter(GrLotTable, inflater);
        GrLotData.setAdapter(adapter);
    }

    //設定監聽事件
    private void setListensers()
    {
        GrLotData.setOnItemClickListener(ModifyLot);
    }

    private AdapterView.OnItemClickListener ModifyLot = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            modifyLot(position);
        }
    };

    private void modifyLot(int position)
    {
        DataTable dtChooseLot = new DataTable();
        dtChooseLot.Rows.add(GrLotTable.Rows.get(position));
        dtChooseLot.getColumns().addAll(GrLotTable.getColumns());

        DataTable dtChooseLotDetail = new DataTable();
        String seq = GrLotTable.Rows.get(0).getValue("SEQ").toString();
        for (DataRow dr : GrLotTableAll.Rows) {
            if (seq.equals(dr.getValue("SEQ").toString())) {
                dtChooseLotDetail.Rows.add(dr);
            }
        }
        dtChooseLotDetail.getColumns().addAll(GrLotTableAll.getColumns());

        DataTable dtChooseLotSn = new DataTable();
        for (DataRow dr: GrrSnTable.Rows) {

            for (DataRow drLot : dtChooseLotDetail.Rows) {

                if (drLot.get("GRR_DET_SN_REF_KEY").toString().equals(dr.getValue("GRR_DET_SN_REF_KEY").toString()))
                    dtChooseLotSn.Rows.add(dr);
            }

//            if (dr.getValue("GRR_DET_SN_REF_KEY").toString().equals(GrLotTable.Rows.get(position).getValue("GRR_DET_SN_REF_KEY").toString()))
//            {
//                dtChooseLotSn.Rows.add(dr);
//            }
        }
        dtChooseLotSn.getColumns().addAll(GrrSnTable.getColumns());

        Bundle chooseGrLot = new Bundle();
        chooseGrLot.putString("Type","Modify");
        chooseGrLot.putString("LotId", GrDetMap.get("LOT_ID").toString());
        chooseGrLot.putSerializable("MstTable", GrMstTable);
        chooseGrLot.putSerializable("DetLotTable", dtChooseLot);
        chooseGrLot.putSerializable("DetLotTableAll", GrLotTableAll);
        chooseGrLot.putSerializable("DetLotSnTable", dtChooseLotSn);
        chooseGrLot.putSerializable("DetLotSnTableAll", GrrSnTable);
        chooseGrLot.putString("ActualQtyStatus",ShCfg);
        chooseGrLot.putString("RegType",RegType);
        chooseGrLot.putString("DetItemTotalQty",DetItemTotalQty);
        chooseGrLot.putDouble("DetReceiveItemQty",receiveCount);
        chooseGrLot.putString("strSkipQC",strSkipQC);
        chooseGrLot.putSerializable("SizeTable", _dtSize);
        chooseGrLot.putSerializable("SkuLevel", _dtSkuLevel);
        //gotoNextActivity(GoodReceiptReceiveLotSnModifyActivity.class, chooseGrLot);
        Intent intent = new Intent(GoodReceiptReceiveLotSnNewActivity.this, GoodReceiptReceiveLotSnModifyNewActivity.class);
        intent.putExtras(chooseGrLot);
        startActivityForResult(intent, requestCode);
    }

    private void GetGrrData() {
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
        conditionSheetId.setValue(GrLotTable.Rows.get(0).getValue("GR_ID").toString());
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

        CallBIModule(bmObjs, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    DataTable dtLot = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceiptReceiveDet").get("GrrDet");
                    DataTable dtSN = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceiptReceiveSN").get("GrrSn");
                    DataTable dtShtCfg = bModuleReturn.getReturnJsonTables().get("FetchWmsSheetConfig").get("SBRM_WMS_SHEET_CONFIG");
                    DataTable dtMerge = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceiptReceiveInfo").get("MERGE");
                    DataTable dtWgr = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceiptReceiveInfo").get("WGR");
                    DataTable dtMgr = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceiptReceiveInfo").get("MGR");

                    if (dtShtCfg.Rows.size()<=0)
                    {
                        ShowMessage(R.string.WAPG007011,GrMstTable.Rows.get(0).getValue("GR_ID").toString());
                        return;
                    }
                    if (GrLotTable.Rows.get(0).getValue("REGISTER_TYPE").toString().equals(""))
                    {
                        ShowMessage(R.string.WAPG007012,GrLotTable.Rows.get(0).getValue("ITEM_ID").toString());
                        return;
                    }

                    DataTable dtChooseRowsMst = new DataTable();
                    for (DataRow dr: GrMstTable.Rows)
                    {
                        if (dr.getValue("GR_ID").toString().equals(GrLotTable.Rows.get(0).getValue("GR_ID").toString()))
                        {
                            dtChooseRowsMst.Rows.add(dr);
                            break;
                        }
                    }
                    dtChooseRowsMst.getColumns().addAll(GrMstTable.getColumns());

                    DataTable dtChooseRowsLot = new DataTable();
//                    for (DataRow dr : dtLot.Rows){
//                        if (dr.getValue("SEQ").toString().equals(String.valueOf(chooseSeq))){
//                            dtChooseRowsLot.Rows.add(dr);
//                        }
//                    }
                    for (DataRow dr : dtMerge.Rows){
                        if (dr.getValue("SEQ").toString().equals(String.valueOf(chooseSeq))){
                            dtChooseRowsLot.Rows.add(dr);
                        }
                    }
                    dtChooseRowsLot.getColumns().addAll( dtLot.getColumns() );

                    ShCfg = dtShtCfg.Rows.get(0).getValue("ACTUAL_QTY_STATUS").toString();

                    // Set SkipQC by checkbox
                    String strSkipQC = GrLotTable.Rows.get(0).getValue("SKIP_QC").toString();
                    Boolean flag =false;
                    for (DataRow dr: dtLot.Rows)
                    {
                        if (dr.getValue("SEQ").toString().equals(GrLotTable.Rows.get(0).getValue("SEQ").toString()))
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

                    GrLotTableAll = dtLot;
                    GrLotTable = dtChooseRowsLot;

                    receiveCount = 0;
                    for (DataRow dr : GrLotTable.Rows) receiveCount += Double.parseDouble(dr.getValue("QTY").toString());
                    ItemTotalQty.setText(String.format("%s/%s", String.valueOf(receiveCount), DetItemTotalQty));

                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    GoodReceiptReceiveLotAdapter adapter = new GoodReceiptReceiveLotAdapter(GrLotTable, inflater);
                    GrLotData.setAdapter(adapter);
                }
            }
        });
    }
}
