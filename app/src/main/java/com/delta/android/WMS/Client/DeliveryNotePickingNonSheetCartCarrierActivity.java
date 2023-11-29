package com.delta.android.WMS.Client;

import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.delta.android.WMS.Client.GridAdapter.GoodPickNonSheetGridAdapter;
import com.delta.android.WMS.Param.BDeliveryNotePickingParam;
import com.delta.android.WMS.Param.BIFetchProcessSheetParam;
import com.delta.android.WMS.Param.BIPDADeliveryNotePickCarrierPortalParam;
import com.delta.android.WMS.Param.BIPDADeliveryNotePickCartPortalParam;
import com.delta.android.WMS.Param.BIPDANoSheetPickCartPortalParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static com.delta.android.Core.Common.Global.getContext;

public class DeliveryNotePickingNonSheetCartCarrierActivity extends BaseFlowActivity {

    //Function WAPG022

//    private EditText etSheetId;
    private Spinner cmbSheetId;
    private EditText etLotId;
    private EditText etItemId;
    private EditText etSelectLotId;
    private EditText etPickQty;
    private Spinner cmbSeq;
    private ListView lvRegister;
    private ImageButton ibtnRegisterSearch;
    private ImageButton ibtnSheetSearch;
    private Button btnConfirm;

    private EditText etNextStep;
    private LinearLayout llCart;
    private LinearLayout llCarrier;
    private LinearLayout llLot;
    private LinearLayout llBlock;
    private LinearLayout llFromBin;
    private LinearLayout llToBin;
    private TextView tvCart;
    private TextView tvCarrier;
    private TextView tvLot;
    private TextView tvBlockID;
    private TextView tvFromBin;
    private TextView tvToBin;

    private int index = 0;
    //private String strStorageId;
    //private String strCarrierId;
    //private String strBlock;
    //private String strFromPort;
    //private String strToPort;
    //private String strObject;
    //private String strCurrentTask;
    //private String strRegLocation = "0";
    //private int iStep;

    public DataTable dtRegister;
    public DataTable dtXfr;
    public DataTable dtDnMst;
    public DataTable dtDnDet;

    private DataTable dtConfigCond = null;
    private DataTable dtConfigSort = null;
    private String strConfigCond = null;
    private String strConfigSort = null;

    public HashMap<String, String> mapSeqItem = new HashMap<>();
    private ArrayList<String> lstSheetId = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_note_picking_non_sheet_cart_carrier);

        initControl();

        // 載入單據代碼至下拉選單
        GetSheetId();

        setListensers();
    }

    private void initControl(){
//        etSheetId = findViewById(R.id.etSheetId);
        cmbSheetId = findViewById(R.id.cmbSheetId);
        etLotId = findViewById(R.id.etLotId);
        etItemId = findViewById(R.id.etItemId);
        etSelectLotId = findViewById(R.id.etSelectLotId);
        etPickQty = findViewById(R.id.etQty);
        cmbSeq = findViewById(R.id.cmbSeq);
        lvRegister = findViewById(R.id.lvRegisters);
        btnConfirm = findViewById(R.id.btnConfirm);
        ibtnSheetSearch = findViewById(R.id.ibtnSheetSearch);
        ibtnRegisterSearch = findViewById(R.id.ibtnRegisterSearch);

        //Item ID不可編輯
        etItemId.setEnabled(false);

        etNextStep = findViewById(R.id.etNextStep);
        tvCart = findViewById(R.id.tvCartID);
        tvCarrier = findViewById(R.id.tvCarrierID);
        tvLot = findViewById(R.id.tvLotID);
        tvBlockID = findViewById(R.id.tvBlockID);
        tvFromBin = findViewById(R.id.tvFromBinID);
        tvToBin = findViewById(R.id.tvToBinID);
        llCart = findViewById(R.id.llCart);
        llCarrier = findViewById(R.id.llCarrier);
        llLot = findViewById(R.id.llLot);
        llBlock = findViewById(R.id.llBlock);
        llFromBin = findViewById(R.id.llFromBin);
        llToBin = findViewById(R.id.llToBin);
    }

    private void setListensers(){
        cmbSeq.setOnItemSelectedListener(cmbSeqItemSelected);
        ibtnSheetSearch.setOnClickListener(ibtnSheetSearchClick);
        ibtnRegisterSearch.setOnClickListener(ibtnRegisterSearchClick);
        lvRegister.setOnItemClickListener(lvRegisterClick);
        btnConfirm.setOnClickListener(lsConfirm);
    }

    private void ClearData(){
//        etSheetId.setText("");
        cmbSheetId.setSelection(lstSheetId.size()-1); // spinner設定回預設選項
        etItemId.setText("");
        etLotId.setText("");
        etPickQty.setText("");
        etSelectLotId.setText("");

        dtDnMst = new DataTable();
        dtDnDet = new DataTable();
        dtRegister = new DataTable();
        dtXfr = new DataTable();
        mapSeqItem = new HashMap<>();

        ArrayList<String> alSeq = new ArrayList<>();
        ArrayAdapter<String> adapterSeq = new ArrayAdapter<String>(DeliveryNotePickingNonSheetCartCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, alSeq);
        cmbSeq.setAdapter(adapterSeq);

        GetListView();

        etNextStep.setText("");
        tvCart.setText("");
        tvCarrier.setText("");
        tvLot.setText("");
        tvBlockID.setText("");
        tvFromBin.setText("");
        tvToBin.setText("");

        strConfigCond = null;
        strConfigSort = null;
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
        param1.setParameterValue("DN");
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

                    SimpleArrayAdapter adapter = new DeliveryNotePickingNonSheetCartCarrierActivity.SimpleArrayAdapter<>(DeliveryNotePickingNonSheetCartCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, lstSheetId);
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

    private void FetchSheetInfo(){
        BModuleObject bmObjSheet = new BModuleObject();
        bmObjSheet.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDADeliveryNotePickCartPortal");
        bmObjSheet.setModuleID("BIGetSheetInfo");
        bmObjSheet.setRequestID("BIGetSheetInfo");

        bmObjSheet.params = new Vector<ParameterInfo>();
        ParameterInfo paramSheet = new ParameterInfo();
        paramSheet.setParameterID(BIPDADeliveryNotePickCartPortalParam.SheetId);
//        paramSheet.setParameterValue(etSheetId.getText().toString());
        paramSheet.setParameterValue(cmbSheetId.getSelectedItem().toString().toUpperCase().trim());
        bmObjSheet.params.add(paramSheet);

        // region 取得ConfigCond及ConfigSort
        BModuleObject biShtCfgSortAndCond = new BModuleObject();
        biShtCfgSortAndCond.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIDeliveryNotePicking");
        biShtCfgSortAndCond.setModuleID("BIFetchConfigCondAndSort");
        biShtCfgSortAndCond.setRequestID("FetchConfigCondAndSort");
        biShtCfgSortAndCond.params = new Vector<ParameterInfo>();
        ParameterInfo paramShtId = new ParameterInfo();
        paramShtId.setParameterID(BDeliveryNotePickingParam.SheetId);
        paramShtId.setParameterValue(cmbSheetId.getSelectedItem().toString().toUpperCase().trim());
        biShtCfgSortAndCond.params.add(paramShtId);
        // endregion

        List<BModuleObject> lstBmObj = new ArrayList<>();
        lstBmObj.add(bmObjSheet);
        lstBmObj.add(biShtCfgSortAndCond);

        CallBIModule(lstBmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(CheckBModuleReturnInfo(bModuleReturn)){
                    dtDnMst = bModuleReturn.getReturnJsonTables().get("BIGetSheetInfo").get("DnMst");
                    dtDnDet = bModuleReturn.getReturnJsonTables().get("BIGetSheetInfo").get("DnDet");

                    dtConfigCond = bModuleReturn.getReturnJsonTables().get("FetchConfigCondAndSort").get("SheetConfigCond");
                    dtConfigSort = bModuleReturn.getReturnJsonTables().get("FetchConfigCondAndSort").get("SheetConfigSort");

                    ArrayList<String> alSeq = new ArrayList<>();

                    for(DataRow dr : dtDnDet.Rows){
                        String strSeq = Replace(dr.getValue("SEQ").toString());

                        if(mapSeqItem.containsKey(strSeq)) continue;
                        alSeq.add(strSeq);
                        mapSeqItem.put(strSeq, dr.getValue("ITEM_ID").toString());
                    }

                    ArrayAdapter<String> adapterSeq = new ArrayAdapter<>(DeliveryNotePickingNonSheetCartCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, alSeq);

                    cmbSeq.setAdapter(adapterSeq);
                }
            }
        });
    }

    private String Replace(String s){
        if(s != null && s.indexOf(".") > 0){
            s = s.replaceAll("0+?$","");
            s = s.replaceAll("[.]$","");
        }
        return s;
    }

    private void FetchLotInfo(){
        BModuleObject bmObjLot = new BModuleObject();
        bmObjLot.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDADeliveryNotePickCartPortal");
        bmObjLot.setModuleID("BIGetPickRegister");
        bmObjLot.setRequestID("BIGetPickRegister");

        bmObjLot.params = new Vector<ParameterInfo>();
        ParameterInfo paramReg = new ParameterInfo();
        paramReg.setParameterID(BIPDADeliveryNotePickCartPortalParam.RegisterId);
        paramReg.setParameterValue(etLotId.getText().toString());
        bmObjLot.params.add(paramReg);

        ParameterInfo paramItem = new ParameterInfo();
        paramItem.setParameterID(BIPDADeliveryNotePickCartPortalParam.ItemId);
        paramItem.setParameterValue(etItemId.getText().toString());
        bmObjLot.params.add(paramItem);

        ParameterInfo paramQty = new ParameterInfo();
        paramQty.setParameterID(BIPDADeliveryNotePickCartPortalParam.PickQty);
        paramQty.setParameterValue(etPickQty.getText().toString());
        bmObjLot.params.add(paramQty);

        if (strConfigCond != null && strConfigCond.length() > 0) {
            ParameterInfo paramCond = new ParameterInfo();
            paramCond.setParameterID(BIPDADeliveryNotePickCartPortalParam.ConfigCond);
            paramCond.setParameterValue(strConfigCond);
            bmObjLot.params.add(paramCond);
        }

        if (strConfigSort != null && strConfigSort.length() > 0) {
            ParameterInfo paramSort = new ParameterInfo();
            paramSort.setParameterID(BIPDADeliveryNotePickCartPortalParam.ConfigSort);
            paramSort.setParameterValue(strConfigSort);
            bmObjLot.params.add(paramSort);
        }

        CallBIModule(bmObjLot, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(CheckBModuleReturnInfo(bModuleReturn)){
                    dtRegister = bModuleReturn.getReturnJsonTables().get("BIGetPickRegister").get("Register");
                    GetListView();
                }
            }
        });
    }

    private void GetListView(){
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        GoodPickNonSheetGridAdapter adapter = new GoodPickNonSheetGridAdapter(dtRegister, inflater);
        lvRegister.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void CartSearch(){
        if(etSelectLotId.getText().toString().equals("")){
            ShowMessage(R.string.WAPG019004); //WAPG019004 請選擇批號
            return;
        }

        BModuleObject bmObjDispatch = new BModuleObject();
        bmObjDispatch.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDADeliveryNotePickCartPortal");
        bmObjDispatch.setModuleID("BIGetRegisterXfr");
        bmObjDispatch.setRequestID("BIGetRegisterXfr");

        bmObjDispatch.params = new Vector<ParameterInfo>();
        ParameterInfo paramDispatch = new ParameterInfo();
        paramDispatch.setParameterID(BIPDADeliveryNotePickCartPortalParam.RegisterId);
        paramDispatch.setParameterValue(etSelectLotId.getText().toString());
        bmObjDispatch.params.add(paramDispatch);

        ParameterInfo paramStock = new ParameterInfo();
        paramStock.setParameterID(BIPDADeliveryNotePickCartPortalParam.Stock);
        paramStock.setParameterValue("S");
        bmObjDispatch.params.add(paramStock);

        CallBIModule(bmObjDispatch, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(CheckBModuleReturnInfo(bModuleReturn)){
                    dtXfr = bModuleReturn.getReturnJsonTables().get("BIGetRegisterXfr").get("PDA");

                    //strStorageId = dtXfr.Rows.get(0).getValue("STORAGE_ID").toString();
                    //strCarrierId = dtXfr.Rows.get(0).getValue("CARRIER_ID").toString();
                    //strBlock = dtXfr.Rows.get(0).getValue("BLOCK_ID").toString();
                    //strCurrentTask = dtXfr.Rows.get(0).getValue("CURRENT_TASK").toString();
                    String strCaseComp = dtXfr.Rows.get(0).getValue("XFR_CASE_COMP").toString();

                    etNextStep.setText(dtXfr.Rows.get(0).getValue("NEXT_STEP").toString());

                    switch (etNextStep.getText().toString())
                    {
                        case "CartStockOut":
                            llCart.setVisibility(View.VISIBLE);
                            llCarrier.setVisibility(View.GONE);
                            llLot.setVisibility(View.GONE);
                            llBlock.setVisibility(View.GONE);
                            llFromBin.setVisibility(View.VISIBLE);
                            llToBin.setVisibility(View.VISIBLE);

                            tvCart.setText(dtXfr.Rows.get(0).getValue("CART_ID").toString());
                            tvFromBin.setText(dtXfr.Rows.get(0).getValue("CART_PORT").toString());
                            tvToBin.setText(dtXfr.Rows.get(0).getValue("OS_PORT").toString());
                            break;

                        case "CarrierStockOut":
                            llCart.setVisibility(View.GONE);
                            llCarrier.setVisibility(View.VISIBLE);
                            llLot.setVisibility(View.GONE);
                            llBlock.setVisibility(View.GONE);
                            llFromBin.setVisibility(View.VISIBLE);
                            llToBin.setVisibility(View.VISIBLE);

                            tvCarrier.setText(dtXfr.Rows.get(0).getValue("CARRIER_ID").toString());
                            tvFromBin.setText(dtXfr.Rows.get(0).getValue("CARRIER_PORT").toString());
                            tvToBin.setText(dtXfr.Rows.get(0).getValue("OS_PORT").toString());
                            break;

                        case "CarrierRegisterUnBind":
                            llCart.setVisibility(View.GONE);
                            llCarrier.setVisibility(View.GONE);
                            llLot.setVisibility(View.VISIBLE);
                            llBlock.setVisibility(View.VISIBLE);
                            llFromBin.setVisibility(View.VISIBLE);
                            llToBin.setVisibility(View.VISIBLE);

                            tvLot.setText(etSelectLotId.getText().toString());
                            tvBlockID.setText(dtXfr.Rows.get(0).getValue("BLOCK_ID").toString());
                            tvFromBin.setText(dtXfr.Rows.get(0).getValue("CARRIER_PORT").toString());
                            tvToBin.setText(dtXfr.Rows.get(0).getValue("OS_PORT").toString());
                            break;

                        case "CarrierStockIn":
                            llCart.setVisibility(View.GONE);
                            llCarrier.setVisibility(View.VISIBLE);
                            llLot.setVisibility(View.GONE);
                            llBlock.setVisibility(View.GONE);
                            llFromBin.setVisibility(View.VISIBLE);
                            llToBin.setVisibility(View.VISIBLE);

                            tvCarrier.setText(dtXfr.Rows.get(0).getValue("CARRIER_ID").toString());
                            tvFromBin.setText(dtXfr.Rows.get(0).getValue("OS_PORT").toString());
                            tvToBin.setText(dtXfr.Rows.get(0).getValue("CART_BIN_PORT").toString());
                            break;

                        case "CartStockIn":
                            llCart.setVisibility(View.VISIBLE);
                            llCarrier.setVisibility(View.GONE);
                            llLot.setVisibility(View.GONE);
                            llBlock.setVisibility(View.GONE);
                            llFromBin.setVisibility(View.VISIBLE);
                            llToBin.setVisibility(View.VISIBLE);

                            tvCarrier.setText(dtXfr.Rows.get(0).getValue("CART_ID").toString());
                            tvFromBin.setText(dtXfr.Rows.get(0).getValue("OS_PORT").toString());
                            tvToBin.setText(dtXfr.Rows.get(0).getValue("CART_FIX_PORT").toString());
                            break;
                    }
                }
            }
        });
    }

    private void PickConfirm(){
        if(dtXfr == null || dtXfr.Rows.size() <= 0) return;

        if(etSelectLotId.getText().toString().equals("") || etPickQty.getText().toString().equals("")){
            ShowMessage(R.string.WAPG019005); //WAPG019005  請選擇物料並輸入揀貨數量
            return;
        }

        boolean bCart = false;
        final String strStep = etNextStep.getText().toString();
        String strHaveCarrier = dtXfr.Rows.get(0).getValue("HAVE_CARR").toString();
        String strFromPort = "";
        String strToPort = "";
        String strObject = "";
        String strRegId = etSelectLotId.getText().toString();
        String strStorageId = dtXfr.Rows.get(0).getValue("STORAGE_ID").toString();
        String strCarrier = dtXfr.Rows.get(0).getValue("CARRIER_ID").toString();
        String strBlock = dtXfr.Rows.get(0).getValue("BLOCK_ID").toString();
        String strRegLoc = dtXfr.Rows.get(0).getValue("LOCATION").toString();

        BModuleObject bimCartObj = new BModuleObject();
        bimCartObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDADeliveryNotePickCartPortal");
        bimCartObj.setModuleID("BIRegisterXfrConfirm");
        bimCartObj.setRequestID("BIRegisterXfrConfirm");
        bimCartObj.params = new Vector<ParameterInfo>();

        BModuleObject bimCarrierObj = new BModuleObject();
        bimCarrierObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDADeliveryNotePickCarrierPortal");
        bimCarrierObj.setModuleID("BIRegisterXfrConfirm");
        bimCarrierObj.setRequestID("BIRegisterXfrConfirm");
        bimCarrierObj.params = new Vector<ParameterInfo>();

        switch (strStep)
        {
            case "CartStockOut":
                strFromPort = dtXfr.Rows.get(0).getValue("CART_PORT").toString();
                strToPort = dtXfr.Rows.get(0).getValue("OS_PORT").toString();
                strObject = dtXfr.Rows.get(0).getValue("CART_ID").toString();
                bCart = true;
                break;

            case "CarrierStockOut":
                strFromPort = dtXfr.Rows.get(0).getValue("CARRIER_PORT").toString();
                strToPort = dtXfr.Rows.get(0).getValue("OS_PORT").toString();
                strObject = dtXfr.Rows.get(0).getValue("CARRIER_ID").toString();
                break;

            case "CarrierRegisterUnBind":
                strFromPort = dtXfr.Rows.get(0).getValue("OS_PORT").toString();
                strToPort = dtXfr.Rows.get(0).getValue("OS_PORT").toString();
                strObject = strRegId;
                break;

            case "CarrierStockIn":
                strFromPort = dtXfr.Rows.get(0).getValue("OS_PORT").toString();
                strToPort = dtXfr.Rows.get(0).getValue("CART_BIN_PORT").toString();
                strObject = dtXfr.Rows.get(0).getValue("CARRIER_ID").toString();
                break;

            case "CartStockIn":
                strFromPort = dtXfr.Rows.get(0).getValue("OS_PORT").toString();
                strToPort = dtXfr.Rows.get(0).getValue("CART_FIX_PORT").toString();
                strObject = dtXfr.Rows.get(0).getValue("CART_ID").toString();
                bCart = true;
                break;
        }

        if(bCart)
        {
            ParameterInfo paramReg = new ParameterInfo();
            paramReg.setParameterID(BIPDADeliveryNotePickCartPortalParam.RegisterId);
            paramReg.setParameterValue(strRegId);
            bimCartObj.params.add(paramReg);

            ParameterInfo paramXfrCase = new ParameterInfo();
            paramXfrCase.setParameterID(BIPDADeliveryNotePickCartPortalParam.XfrCase);
            paramXfrCase.setParameterValue("CartCarrier");
            bimCartObj.params.add(paramXfrCase);

            ParameterInfo paramXfrTask = new ParameterInfo();
            paramXfrTask.setParameterID(BIPDADeliveryNotePickCartPortalParam.XfrTask);
            paramXfrTask.setParameterValue(strStep);
            bimCartObj.params.add(paramXfrTask);

            ParameterInfo paramFromPort = new ParameterInfo();
            paramFromPort.setParameterID(BIPDADeliveryNotePickCartPortalParam.FromPort);
            paramFromPort.setParameterValue(strFromPort);
            bimCartObj.params.add(paramFromPort);

            ParameterInfo paramToPort = new ParameterInfo();
            paramToPort.setParameterID(BIPDADeliveryNotePickCartPortalParam.ToPort);
            paramToPort.setParameterValue(strToPort);
            bimCartObj.params.add(paramToPort);

            ParameterInfo paramObjId = new ParameterInfo();
            paramObjId.setParameterID(BIPDADeliveryNotePickCartPortalParam.ObjectId);
            paramObjId.setParameterValue(strObject);
            bimCartObj.params.add(paramObjId);

            ParameterInfo paramStorageId = new ParameterInfo();
            paramStorageId.setParameterID(BIPDADeliveryNotePickCartPortalParam.StorageId);
            paramStorageId.setParameterValue(strStorageId);
            bimCartObj.params.add(paramStorageId);

            ParameterInfo paramCarrier = new ParameterInfo();
            paramCarrier.setParameterID(BIPDADeliveryNotePickCartPortalParam.Carrier);
            paramCarrier.setParameterValue(strCarrier);
            bimCartObj.params.add(paramCarrier);

            ParameterInfo paramBlock = new ParameterInfo();
            paramBlock.setParameterID(BIPDADeliveryNotePickCartPortalParam.Block);
            paramBlock.setParameterValue(strBlock);
            bimCartObj.params.add(paramBlock);

            ParameterInfo paramStock = new ParameterInfo();
            paramStock.setParameterID(BIPDADeliveryNotePickCartPortalParam.Stock);
            paramStock.setParameterValue("S");
            bimCartObj.params.add(paramStock);

            ParameterInfo paramPickQty = new ParameterInfo();
            paramPickQty.setParameterID(BIPDADeliveryNotePickCartPortalParam.PickQty);
            paramPickQty.setParameterValue(etPickQty.getText().toString());
            bimCartObj.params.add(paramPickQty);

            ParameterInfo paramSheetId = new ParameterInfo();
            paramSheetId.setParameterID(BIPDADeliveryNotePickCartPortalParam.SheetId);
//            paramSheetId.setParameterValue(etSheetId.getText().toString());
            paramSheetId.setParameterValue(cmbSheetId.getSelectedItem().toString().toUpperCase().trim());
            bimCartObj.params.add(paramSheetId);

            ParameterInfo paramSeq = new ParameterInfo();
            paramSeq.setParameterID(BIPDADeliveryNotePickCartPortalParam.Seq);
            paramSeq.setParameterValue(cmbSeq.getSelectedItem().toString());
            bimCartObj.params.add(paramSeq);

            ParameterInfo paramRegLoc = new ParameterInfo();
            paramRegLoc.setParameterID(BIPDADeliveryNotePickCartPortalParam.RegLocation);
            paramRegLoc.setParameterValue(strRegLoc);
            bimCartObj.params.add(paramRegLoc);

            ParameterInfo paramHaveCarr = new ParameterInfo();
            paramHaveCarr.setParameterID(BIPDADeliveryNotePickCartPortalParam.HaveCarrier);
            paramHaveCarr.setParameterValue(strHaveCarrier);
            bimCartObj.params.add(paramHaveCarr);

            ParameterInfo paramOsBin = new ParameterInfo(); // 20220812 Ikea 傳入 BinId
            paramOsBin.setParameterID(BIPDADeliveryNotePickCartPortalParam.OsBinId);
            paramOsBin.setParameterValue(dtXfr.Rows.get(0).getValue("OS_BIN_ID").toString());
            bimCartObj.params.add(paramOsBin);

            CallBIModule(bimCartObj, new WebAPIClientEvent() {
                @Override
                public void onPostBack(BModuleReturn bModuleReturn) {
                    if(CheckBModuleReturnInfo(bModuleReturn))
                    {
                        //WAPG014003 作業成功
                        Toast.makeText(getContext(), R.string.WAPG014003, Toast.LENGTH_SHORT).show();

                        if(strStep.equals("CartStockIn"))
                            ClearData();
                        else
                        {
                            switch (strStep)
                            {
                                case "CartStockOut":
                                    etNextStep.setText("CarrierStockOut");
                                    llCart.setVisibility(View.GONE);
                                    llCarrier.setVisibility(View.VISIBLE);
                                    llLot.setVisibility(View.GONE);
                                    llBlock.setVisibility(View.GONE);
                                    llFromBin.setVisibility(View.VISIBLE);
                                    llToBin.setVisibility(View.VISIBLE);

                                    tvCarrier.setText(dtXfr.Rows.get(0).getValue("CARRIER_ID").toString());
                                    tvFromBin.setText(dtXfr.Rows.get(0).getValue("CARRIER_PORT").toString());
                                    tvToBin.setText(dtXfr.Rows.get(0).getValue("OS_PORT").toString());
                                    break;

                                case "CarrierStockOut":
                                    etNextStep.setText("CarrierRegisterBind");
                                    llCart.setVisibility(View.GONE);
                                    llCarrier.setVisibility(View.GONE);
                                    llLot.setVisibility(View.VISIBLE);
                                    llBlock.setVisibility(View.VISIBLE);
                                    llFromBin.setVisibility(View.VISIBLE);
                                    llToBin.setVisibility(View.VISIBLE);

                                    tvLot.setText(etSelectLotId.getText().toString());
                                    tvBlockID.setText(dtXfr.Rows.get(0).getValue("BLOCK_ID").toString());
                                    tvFromBin.setText(dtXfr.Rows.get(0).getValue("OS_PORT").toString());
                                    tvToBin.setText(dtXfr.Rows.get(0).getValue("OS_PORT").toString());
                                    break;

                                case "CarrierRegisterUnBind":
                                    etNextStep.setText("CarrierStockIn");
                                    llCart.setVisibility(View.GONE);
                                    llCarrier.setVisibility(View.VISIBLE);
                                    llLot.setVisibility(View.GONE);
                                    llBlock.setVisibility(View.GONE);
                                    llFromBin.setVisibility(View.VISIBLE);
                                    llToBin.setVisibility(View.VISIBLE);

                                    tvCarrier.setText(dtXfr.Rows.get(0).getValue("CARRIER_ID").toString());
                                    tvFromBin.setText(dtXfr.Rows.get(0).getValue("OS_PORT").toString());
                                    tvToBin.setText(dtXfr.Rows.get(0).getValue("CART_BIN_PORT").toString());
                                    break;

                                case "CarrierStockIn":
                                    etNextStep.setText("CartStockIn");
                                    llCart.setVisibility(View.VISIBLE);
                                    llCarrier.setVisibility(View.GONE);
                                    llLot.setVisibility(View.GONE);
                                    llBlock.setVisibility(View.GONE);
                                    llFromBin.setVisibility(View.VISIBLE);
                                    llToBin.setVisibility(View.VISIBLE);

                                    tvCarrier.setText(dtXfr.Rows.get(0).getValue("CART_ID").toString());
                                    tvFromBin.setText(dtXfr.Rows.get(0).getValue("OS_PORT").toString());
                                    tvToBin.setText(dtXfr.Rows.get(0).getValue("CART_FIX_PORT").toString());
                                    break;
                            }
                        }
                    }
                }
            });
        }
        else
        {
            ParameterInfo paramReg = new ParameterInfo();
            paramReg.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.RegisterId);
            paramReg.setParameterValue(strRegId);
            bimCarrierObj.params.add(paramReg);

            ParameterInfo paramXfrCase = new ParameterInfo();
            paramXfrCase.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.XfrCase);
            paramXfrCase.setParameterValue("CartCarrier");
            bimCarrierObj.params.add(paramXfrCase);

            ParameterInfo paramXfrTask = new ParameterInfo();
            paramXfrTask.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.XfrTask);
            paramXfrTask.setParameterValue(strStep);
            bimCarrierObj.params.add(paramXfrTask);

            ParameterInfo paramFromPort = new ParameterInfo();
            paramFromPort.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.FromPort);
            paramFromPort.setParameterValue(strFromPort);
            bimCarrierObj.params.add(paramFromPort);

            ParameterInfo paramToPort = new ParameterInfo();
            paramToPort.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.ToPort);
            paramToPort.setParameterValue(strToPort);
            bimCarrierObj.params.add(paramToPort);

            ParameterInfo paramObj = new ParameterInfo();
            paramObj.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.ObjectId);
            paramObj.setParameterValue(strObject);
            bimCarrierObj.params.add(paramObj);

            ParameterInfo paramStorage = new ParameterInfo();
            paramStorage.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.StorageId);
            paramStorage.setParameterValue(strStorageId);
            bimCarrierObj.params.add(paramStorage);

            ParameterInfo paramCarrier = new ParameterInfo();
            paramCarrier.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.Carrier);
            paramCarrier.setParameterValue(strCarrier);
            bimCarrierObj.params.add(paramCarrier);

            ParameterInfo paramBlock = new ParameterInfo();
            paramBlock.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.Block);
            paramBlock.setParameterValue(strBlock);
            bimCarrierObj.params.add(paramBlock);

            ParameterInfo paramBestBin = new ParameterInfo();
            paramBestBin.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.BestBin);
            paramBestBin.setParameterValue(dtXfr.Rows.get(0).getValue("OS_PORT").toString());
            bimCarrierObj.params.add(paramBestBin);

            ParameterInfo paramStock = new ParameterInfo();
            paramStock.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.Stock);
            paramStock.setParameterValue("S");
            bimCarrierObj.params.add(paramStock);

            ParameterInfo paramPickQty = new ParameterInfo();
            paramPickQty.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.PickQty);
            paramPickQty.setParameterValue(etPickQty.getText().toString());
            bimCarrierObj.params.add(paramPickQty);

            ParameterInfo paramSheetId = new ParameterInfo();
            paramSheetId.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.SheetId);
//            paramSheetId.setParameterValue(etSheetId.getText().toString());
            paramSheetId.setParameterValue(cmbSheetId.getSelectedItem().toString().toUpperCase().trim());
            bimCarrierObj.params.add(paramSheetId);

            ParameterInfo paramSeq = new ParameterInfo();
            paramSeq.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.Seq);
            paramSeq.setParameterValue(cmbSeq.getSelectedItem().toString());
            bimCarrierObj.params.add(paramSeq);

            ParameterInfo paramOsBin = new ParameterInfo(); // 20220812 Ikea 傳入 BinId
            paramOsBin.setParameterID(BIPDADeliveryNotePickCarrierPortalParam.OsBinId);
            paramOsBin.setParameterValue(dtXfr.Rows.get(0).getValue("OS_BIN_ID").toString());
            bimCarrierObj.params.add(paramOsBin);

            CallBIModule(bimCarrierObj, new WebAPIClientEvent() {
                @Override
                public void onPostBack(BModuleReturn bModuleReturn) {
                    if (CheckBModuleReturnInfo(bModuleReturn))
                    {
                        //WAPG014003 作業成功
                        Toast.makeText(getContext(), R.string.WAPG014003, Toast.LENGTH_SHORT).show();

                        if(strStep.equals("CartStockIn"))
                            ClearData();
                        else
                        {
                            switch (strStep)
                            {
                                case "CartStockOut":
                                    etNextStep.setText("CarrierStockOut");
                                    llCart.setVisibility(View.GONE);
                                    llCarrier.setVisibility(View.VISIBLE);
                                    llLot.setVisibility(View.GONE);
                                    llBlock.setVisibility(View.GONE);
                                    llFromBin.setVisibility(View.VISIBLE);
                                    llToBin.setVisibility(View.VISIBLE);

                                    tvCarrier.setText(dtXfr.Rows.get(0).getValue("CARRIER_ID").toString());
                                    tvFromBin.setText(dtXfr.Rows.get(0).getValue("CARRIER_PORT").toString());
                                    tvToBin.setText(dtXfr.Rows.get(0).getValue("OS_PORT").toString());
                                    break;

                                case "CarrierStockOut":
                                    etNextStep.setText("CarrierRegisterUnBind");
                                    llCart.setVisibility(View.GONE);
                                    llCarrier.setVisibility(View.GONE);
                                    llLot.setVisibility(View.VISIBLE);
                                    llBlock.setVisibility(View.VISIBLE);
                                    llFromBin.setVisibility(View.VISIBLE);
                                    llToBin.setVisibility(View.VISIBLE);

                                    tvLot.setText(etSelectLotId.getText().toString());
                                    tvBlockID.setText(dtXfr.Rows.get(0).getValue("BLOCK_ID").toString());
                                    tvFromBin.setText(dtXfr.Rows.get(0).getValue("OS_PORT").toString());
                                    tvToBin.setText(dtXfr.Rows.get(0).getValue("OS_PORT").toString());
                                    break;

                                case "CarrierRegisterUnBind":
                                    etNextStep.setText("CarrierStockIn");
                                    llCart.setVisibility(View.GONE);
                                    llCarrier.setVisibility(View.VISIBLE);
                                    llLot.setVisibility(View.GONE);
                                    llBlock.setVisibility(View.GONE);
                                    llFromBin.setVisibility(View.VISIBLE);
                                    llToBin.setVisibility(View.VISIBLE);

                                    tvCarrier.setText(dtXfr.Rows.get(0).getValue("CARRIER_ID").toString());
                                    tvFromBin.setText(dtXfr.Rows.get(0).getValue("OS_PORT").toString());
                                    tvToBin.setText(dtXfr.Rows.get(0).getValue("CART_BIN_PORT").toString());
                                    break;

                                case "CarrierStockIn":
                                    etNextStep.setText("CartStockIn");
                                    llCart.setVisibility(View.VISIBLE);
                                    llCarrier.setVisibility(View.GONE);
                                    llLot.setVisibility(View.GONE);
                                    llBlock.setVisibility(View.GONE);
                                    llFromBin.setVisibility(View.VISIBLE);
                                    llToBin.setVisibility(View.VISIBLE);

                                    tvCarrier.setText(dtXfr.Rows.get(0).getValue("CART_ID").toString());
                                    tvFromBin.setText(dtXfr.Rows.get(0).getValue("OS_PORT").toString());
                                    tvToBin.setText(dtXfr.Rows.get(0).getValue("CART_FIX_PORT").toString());
                                    break;
                            }
                        }
                    }
                }
            });
        }
    }

    private View.OnClickListener ibtnSheetSearchClick = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            /*
            if(etSheetId.getText().toString().equals("")){
                ShowMessage(R.string.WAPG019001); //WAPG019001 請輸入單據代碼
                return;
            }
             */

            int sheetIdIndex = cmbSheetId.getSelectedItemPosition();
            if (sheetIdIndex == (lstSheetId.size() - 1)) {
                ShowMessage(R.string.WAPG019001); //WAPG019001 請選擇單據代碼
                return;
            }

            FetchSheetInfo();
        }
    };

    private AdapterView.OnItemSelectedListener cmbSeqItemSelected = new AdapterView.OnItemSelectedListener(){
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l){
            if(mapSeqItem.containsKey(cmbSeq.getSelectedItem().toString())){
                String strItem = mapSeqItem.get(cmbSeq.getSelectedItem().toString());
                etItemId.setText(strItem);
            }

            DataRow drDet = dtDnDet.Rows.get(i);
            strConfigCond = generateExtendConfigCond(drDet, dtConfigCond, "SR");
            strConfigSort = generateExtendConfigSort(dtConfigSort);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView){
            //do nothing
        }
    };

    private View.OnClickListener ibtnRegisterSearchClick = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            if(etPickQty.getText().toString().equals("")){
                ShowMessage(R.string.WAPG019002); //WAPG019002 請輸入揀貨數量
                return;
            }
            if(cmbSeq.getSelectedItem().toString().equals("") || etItemId.getText().toString().equals("")){
                ShowMessage(R.string.WAPG019003); //WAPG019003 請確認項次與物料代碼是否正確
                return;
            }
            FetchLotInfo();
        }
    };

    private AdapterView.OnItemClickListener lvRegisterClick = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            etSelectLotId.setText(dtRegister.Rows.get(position).getValue("REGISTER_ID").toString());
            index = position;
            CartSearch();
        }
    };

    private AdapterView.OnClickListener lsConfirm = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            PickConfirm();
        }
    };

    public void OnClickLotClear(View v) {etSelectLotId.setText("");}

    //擴充挑貨規則: 條件串成 SQL Where 條件
    private String generateExtendConfigCond(DataRow drDet, DataTable dtCond, String strAlias) {

        String strExtendCond = null;

        if (dtCond != null && dtCond.Rows.size() > 0) {
            for (DataRow drCond : dtCond.Rows) {
                String reqVal = drDet.getValue(drCond.getValue("REQ_FIELD").toString()).toString();
                String strCond = null;

                if (!reqVal.equals("*")) {
                    strCond = String.format("%s.%s %s '%s'",strAlias, drCond.getValue("REG_FIELD").toString(), drCond.get("COND_OPERATOR").toString(), reqVal); // e.g. SR.LOT_CODE = 'L...'

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
