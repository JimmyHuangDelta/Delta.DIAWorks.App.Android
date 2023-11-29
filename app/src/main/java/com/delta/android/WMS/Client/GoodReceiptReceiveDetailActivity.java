package com.delta.android.WMS.Client;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.Activity.ScanActivity;
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
import com.delta.android.WMS.Client.GridAdapter.GoodNonreceiptReceiveSelectGridAdapter;
import com.delta.android.WMS.Param.BGoodReceiptReceiveParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.ParamObj.CheckCountObj;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.delta.android.WMS.Client.GridAdapter.GoodReceiptReceiveDetaGridAdapter;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.sql.RowSet;

public class GoodReceiptReceiveDetailActivity extends BaseFlowActivity {

    // private variable
    private static int requestCode = 123; //需要接收回傳資訊時使用
    private ViewHolder holder = null;
    private DataTable GrMstTable;// from p.1
    private DataTable GrDetTable;// from p.1
    private DataTable dtStorageItem;
    private HashMap<String,Double> SeqQtyOfAllLot;// from p.1
    private HashMap<String,String> SeqSkipQcOfAllLot;// from p.1
    private HashMap<String, ArrayList<String>> mapStorageBin = new HashMap<String, ArrayList<String>>();
    private HashMap<String, String> mapStorageBinResult = new HashMap<String, String>();
    private DataTable GrLotTableAll;
    private DataTable GrrSnTableAll;
    private String ShCfg;
    private String strStorage = "";
    private String strItem = "";
    private DataTable filterTable;
    private DataTable _DtQc;

    static class ViewHolder
    {
        // 宣告控制項物件
        ListView GrDetData;
        EditText ItemId;
        Button btnReceiveAll;
        ImageButton ibtnGrDetItemIdQRScan; // 220707 Ikea 新增鏡頭掃描輸入
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_good_receipt_receive_detail);

        this.initialData();
        // 設定監聽事件
        setListensers();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent){

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, dataIntent);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, getResources().getText(R.string.QRCODE_SCAN_CANCEL).toString(), Toast.LENGTH_SHORT).show();
            } else {
                holder.ItemId.setText(result.getContents().trim());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, dataIntent);
            // 回傳資料
            GetReceiveQtyAndSkipQc();
        }
    }

    private void initialData() {

        GrMstTable =  (DataTable)getIntent().getSerializableExtra("GrMst");
        GrDetTable =  (DataTable)getIntent().getSerializableExtra("GrDet");
        SeqQtyOfAllLot = (HashMap)getIntent().getSerializableExtra("GrrQty");
        SeqSkipQcOfAllLot = (HashMap)getIntent().getSerializableExtra("GrrSkipQc");

        if (holder==null) holder = new ViewHolder();
        // 取得控制項物件
        holder.ItemId = findViewById(R.id.etGrDetItemId);
        holder.GrDetData = findViewById(R.id.lvGrDetData);
        holder.btnReceiveAll = findViewById(R.id.btnReceiveAll);
        holder.ibtnGrDetItemIdQRScan = findViewById(R.id.ibtnGrDetItemIdQRScan); // 220707 Ikea 新增鏡頭掃描輸入

        GetQcData();
    }

    //設定監聽事件
    private void setListensers()
    {
        holder.ItemId.setOnEditorActionListener(filterSheetDet);
        holder.GrDetData.setOnItemClickListener(getLotData);
        holder.btnReceiveAll.setOnClickListener(ReceiveAll);
        holder.ibtnGrDetItemIdQRScan.setOnClickListener(IbtnGridDetItemIdQRScanOnClick);
    }

    //region 事件
    private TextView.OnEditorActionListener filterSheetDet = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (GrDetTable == null || GrDetTable.Rows.size() == 0) return false;
            // 讓使用者用搜尋 ITEM_ID

            filterTable = new DataTable();
            String filterId = holder.ItemId.getText().toString().toUpperCase().trim(); //20200729 archie 轉大寫
            if (filterId == null || filterId.equals("")){
                filterTable = GrDetTable;
            }else{
                for (DataRow dr : GrDetTable.Rows){
                    if (dr.getValue("ITEM_ID").toString().equals(filterId)){
                        filterTable.Rows.add(dr);
                    }
                }
            }
            // Input ListView
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            GoodReceiptReceiveDetaGridAdapter adapter = new GoodReceiptReceiveDetaGridAdapter(filterTable, SeqQtyOfAllLot, SeqSkipQcOfAllLot, inflater);
            holder.GrDetData.setAdapter(adapter);
            return false;
        }
    };

    private AdapterView.OnItemClickListener getLotData = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

            if (filterTable == null) filterTable = GrDetTable;
            final DataRow chooseRow = filterTable.Rows.get(position);

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

            bmObjs.add(biObj1);
            bmObjs.add(biObj2);
            bmObjs.add(biObj3);
            // endregion

            final DataTable dtChooseDetItem = new DataTable();
            dtChooseDetItem.Rows.add(chooseRow);

            CallBIModule(bmObjs, new WebAPIClientEvent() {
                @Override
                public void onPostBack(BModuleReturn bModuleReturn) {
                    if (CheckBModuleReturnInfo(bModuleReturn)) {
                        DataTable dtLot = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceiptReceiveDet").get("GrrDet");
                        DataTable dtSN = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceiptReceiveSN").get("GrrSn");
                        DataTable dtShtCfg = bModuleReturn.getReturnJsonTables().get("FetchWmsSheetConfig").get("SBRM_WMS_SHEET_CONFIG");

                        if (dtShtCfg.Rows.size()<=0)
                        {
                            ShowMessage(R.string.WAPG007011,GrMstTable.Rows.get(0).getValue("GR_ID").toString());
                            return;
                        }
                        if (chooseRow.getValue("REGISTER_TYPE").toString().equals(""))
                        {
                            ShowMessage(R.string.WAPG007012,chooseRow.getValue("ITEM_ID").toString());
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
                        for (DataRow dr : dtLot.Rows){
                            if (dr.getValue("ITEM_ID").toString().equals(chooseRow.getValue("ITEM_ID").toString())){
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
                        //gotoNextActivity(GoodReceiptReceiveLotSnActivity.class, chooseGrDet);
                        Intent intent = new Intent(GoodReceiptReceiveDetailActivity.this, GoodReceiptReceiveLotSnActivity.class);
                        intent.putExtras(chooseGrDet);
                        startActivityForResult(intent, requestCode);
                    }
                }
            });
        }
    };

    private View.OnClickListener ReceiveAll = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Get -> Check -> ExecuteProcess
            GetLotAndSn();
        }
    };

    // 220707 Ikea 新增鏡頭掃描輸入
    private View.OnClickListener IbtnGridDetItemIdQRScanOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            IntentIntegrator integrator = new IntentIntegrator(GoodReceiptReceiveDetailActivity.this);
            // 設置要掃描的條碼類型，ONE_D_CODE_TYPES：一維碼，QR_CODE_TYPES-二維碼
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("BarCode Scan"); //底部的提示文字
            integrator.setCameraId(0); //前置(1)或者後置(0)攝像頭
            integrator.setBeepEnabled(false); //掃描成功的「嗶嗶」聲
            integrator.setBarcodeImageEnabled(false); //是否保留掃碼成功時候的截圖
            integrator.setCaptureActivity(ScanActivity.class);
            integrator.initiateScan();
        }
    };

    //endregion

    // region 方法
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

        mapStorageBin = new HashMap<String, ArrayList<String>>();
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
            if(strQC.equals("Y")){
                bSkipQC = true;
                break;
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

        CallBIModule(bmObjs, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    GrLotTableAll = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceiptReceiveDet").get("GrrDet");
                    GrrSnTableAll = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceiptReceiveSN").get("GrrSn");
                    DataTable dtShtCfg = bModuleReturn.getReturnJsonTables().get("FetchWmsSheetConfig").get("SBRM_WMS_SHEET_CONFIG");
                    DataTable dtStorageTempBin = bModuleReturn.getReturnJsonTables().get("BIFetchBin").get("BIN");

                    if (GrLotTableAll.Rows.size()<=0)
                    {
                        ShowMessage(R.string.WAPG007016);
                        return;
                    }
                    if (dtShtCfg.Rows.size()<=0)
                    {
                        ShowMessage(R.string.WAPG007011,GrMstTable.Rows.get(0).getValue("GR_ID").toString());
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
                        ShowMessage(R.string.WAPG007018); //WAPG007018 查無對應的入庫儲位
                        return;
                    }

                    //有暫存區，就直接到暫存區
                    for (DataRow dr : dtStorageTempBin.Rows)
                    {
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

                    for (DataRow dr : dtStorageTempBin.Rows)
                    {
                        if (mapStorageBin.get(dr.getValue("STORAGE_ID").toString()).size() <= 0)
                        {
                            if(dr.getValue("BIN_TYPE").toString().equals("IS"))
                                mapStorageBin.get(dr.getValue("STORAGE_ID").toString()).add(dr.getValue("BIN_ID").toString());
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
        LayoutInflater inflater = LayoutInflater.from(GoodReceiptReceiveDetailActivity.this);
        final View viewConfirm = inflater.inflate(R.layout.activity_good_nonreceipt_receive_confirm, null);
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(GoodReceiptReceiveDetailActivity.this);
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

        for(DataRow dr : GrDetTable.Rows){
            String strSelectKey = dr.getValue("STORAGE_ID").toString() + "_" + dr.getValue("ITEM_ID").toString();
            //當取得的儲位不只一個時才加入
            /*if(mapStorageBin.containsKey(dr.getValue("STORAGE_ID").toString())){
                if(mapStorageBin.get(dr.getValue("STORAGE_ID").toString()).size() < 1) // 220708 Ikea 原為<=1會使只有一個儲位的情況顯示不出來，無法進行後續作業
                    continue;
            }*/

            if(!lstTemp.contains(strSelectKey)){
                lstTemp.add(strSelectKey);
                DataRow drNew = dtStorageItem.newRow();
                drNew.setValue("ITEM_ID", dr.getValue("ITEM_ID").toString());
                drNew.setValue("ITEM_NAME", dr.getValue("ITEM_NAME").toString());
                drNew.setValue("STORAGE_ID", dr.getValue("STORAGE_ID").toString());
                drNew.setValue("BIN_ID", "");
                dtStorageItem.Rows.add(drNew);
            }
        }

        LayoutInflater inflaterSelect = LayoutInflater.from(getApplicationContext());
        GoodNonreceiptReceiveSelectGridAdapter adapterSelect = new GoodNonreceiptReceiveSelectGridAdapter(dtStorageItem, inflaterSelect);
        lvStorageItem.setAdapter(adapterSelect);
        adapterSelect.notifyDataSetChanged();

        lvStorageItem.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                LayoutInflater inflaterBin = LayoutInflater.from(GoodReceiptReceiveDetailActivity.this);
                final View viewBin = inflaterBin.inflate(R.layout.activity_good_nonreceipt_receive_confirm_select_bin, null);
                final android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.Builder(GoodReceiptReceiveDetailActivity.this);
                builder1.setView(viewBin);

                final android.app.AlertDialog dialogBin = builder1.create();
                dialogBin.setCancelable(false);
                dialogBin.show();

                final Spinner cmbBin = dialogBin.findViewById(R.id.cmbBinID);
                final Button btnBinConfirm = dialogBin.findViewById(R.id.btnBinConfirm);

                strStorage = dtStorageItem.getValue(position, "STORAGE_ID").toString();
                strItem = dtStorageItem.getValue(position, "ITEM_ID").toString();
                ArrayAdapter<String> adapterBin = new ArrayAdapter<>(GoodReceiptReceiveDetailActivity.this, android.R.layout.simple_spinner_dropdown_item, mapStorageBin.get(strStorage));
                cmbBin.setAdapter(adapterBin);

                cmbBin.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l){
                        //dtStorageItem.setValue(position, "BIN_ID", mapStorageBin.get(strStorage).get(position));
                        for(DataRow dr : dtStorageItem.Rows){
                            if(dr.getValue("STORAGE_ID").toString().equals(strStorage) &&
                                    dr.getValue("ITEM_ID").toString().equals(strItem)){
                                dr.setValue("BIN_ID", mapStorageBin.get(strStorage).get(position));
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
                        LayoutInflater inflaterSelect = LayoutInflater.from(getApplicationContext());
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
                        ShowMessage(R.string.WAPG009018, args); //WAPG009018    尚未選擇倉庫[%s]物料[%s]對應儲位
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
                            if(!mapStorageBinResult.containsKey(storage)){
                                mapStorageBinResult.put(storage, dr.getValue("BIN_ID").toString());
                            }
                        }
                    }
                }

                //CreateSheetID();
                CheckAllConstraint();
            }
        });
    }

    private void CheckAllConstraint()
    {
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
                ShowMessage(str);
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
                        ShowMessage(R.string.WAPG007014,
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
                        ShowMessage(R.string.WAPG007013,
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
                        ShowMessage(R.string.WAPG007015,
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
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BGoodReceiptReceive");
        bmObj.setModuleID("");
        bmObj.setRequestID("GRR");
        bmObj.params = new Vector<ParameterInfo>();

        BGoodReceiptReceiveParam sheet = new BGoodReceiptReceiveParam();
        BGoodReceiptReceiveParam.GrrMasterObj sheet1 = sheet.new GrrMasterObj();
        BGoodReceiptReceiveParam.GrrMasterObj sheetTemp = sheet1.GetGrrSheet(GrMstTable, GrLotTableAll, GrrSnTableAll);

        //如果物料為PcsSN類別的，在收料及收料完成時需要檢查是否有批號及序號。
        for(BGoodReceiptReceiveParam.GrrDetObj obj : sheetTemp.getGrDetails()){
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
                ShowMessage(R.string.WAPG007019, args); //WAPG007019 查無物料[%s]明細
                return;
            }
            if(!drTemp.getValue("REGISTER_TYPE").toString().equals("PcsSN")) continue;

            if(obj.getGrSns().size() <= 0){
                Object[] args = new Object[2];
                args[0] = itemId;
                args[1] = "PcsSN";
                ShowMessage(R.string.WAPG007020, args); //WAPG007020 物料[%s]為[%s]管控，未輸入SN
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

        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.INV.Parameter.GrrMasterObj", "bmWMS.INV.Param");
        MesClass mesClassEnum = new MesClass(vListEnum);
        String strGrrMstObj = mesClassEnum.generateFinalCode(sheet1.GetGrrSheet(GrMstTable, GrLotTableAll, GrrSnTableAll));

        VirtualClass vListEnum2 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.CheckCountObj", "bmWMS.Library.Param");
        MList mListEnum2 = new MList(vListEnum2);
        String strCheckCountObj = mListEnum2.generateFinalCode(lstChkCountObj);

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BGoodReceiptReceiveParam.TrxType);
        param1.setParameterValue("Confirmed");
        bmObj.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BGoodReceiptReceiveParam.GrrMasterObj);
        param2.setNetParameterValue(strGrrMstObj);// setNetParameterValue2?
        bmObj.params.add(param2);

        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BGoodReceiptReceiveParam.ExecuteCheckStock); // 20220804 Add by Ikea 是否執行盤點檢查
        param3.setNetParameterValue2("true");
        bmObj.params.add(param3);

        ParameterInfo param4 = new ParameterInfo(); // 20220804 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中
        param4.setParameterID(BGoodReceiptReceiveParam.CheckCountObj);
        param4.setNetParameterValue(strCheckCountObj);
        bmObj.params.add(param4);

        CallBModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    ShowMessage(R.string.WAPG007010, new ShowMessageEvent() {
                        @Override
                        public void onDismiss() {
                            gotoPreviousActivity(GoodReceiptReceiveActivity.class, true);
                        }
                    });
                }
            }
        });
    }

    // endregion

    private void GetReceiveQtyAndSkipQc()
    {
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

        CallBIModule(biObj1, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                GrLotTableAll = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceiptReceiveDet").get("GrrDet");
                SeqQtyOfAllLot = new HashMap();
                SeqSkipQcOfAllLot = new HashMap();

                // 初始化SeqQtyOfAllLot
                for (DataRow dr: GrDetTable.Rows)
                {
                    SeqQtyOfAllLot.put(dr.getValue("SEQ").toString(), 0.0);
                }
                // 得到各項次已收料的數量總和
                for (DataRow dr: GrLotTableAll.Rows)
                {
                    double temp = 0.0;
                    temp = SeqQtyOfAllLot.get(dr.getValue("SEQ").toString()) + Double.parseDouble(dr.getValue("QTY").toString());
                    SeqQtyOfAllLot.put(dr.getValue("SEQ").toString(),temp);
                    SeqSkipQcOfAllLot.put(dr.getValue("SEQ").toString(),dr.getValue("SKIP_QC").toString());

                    if (!dr.getValue("MFG_DATE").toString().equals(""))
                    {

                        dr.setValue("MFG_DATE", dr.getValue("MFG_DATE").toString().substring(0,10) + " 00:00:00");
                    }

                    if (!dr.getValue("EXP_DATE").toString().equals(""))
                    {

                        dr.setValue("EXP_DATE", dr.getValue("EXP_DATE").toString().substring(0,10) + " 23:59:59");
                    }
                }


                GetQcData();
                // Input ListView
                /*LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                GoodReceiptReceiveDetaGridAdapter adapter = new GoodReceiptReceiveDetaGridAdapter(GrDetTable, SeqQtyOfAllLot, SeqSkipQcOfAllLot,inflater);
                holder.GrDetData.setAdapter(adapter);*/
            }
        });
    }

    private void GetQcData()
    {
        final String strVendorKey = GrMstTable.Rows.get(0).getValue("VENDOR_KEY").toString();
        String strVKey = String.format("%s','%s", strVendorKey, "*");
        List<String>lstItem = new ArrayList<>();

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

        CallBIModule(bmObj, new WebAPIClientEvent() {
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

                    // Input ListView
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    GoodReceiptReceiveDetaGridAdapter adapter = new GoodReceiptReceiveDetaGridAdapter(GrDetTable, SeqQtyOfAllLot, SeqSkipQcOfAllLot,inflater);
                    holder.GrDetData.setAdapter(adapter);
                }
            }
        });
    }
}
