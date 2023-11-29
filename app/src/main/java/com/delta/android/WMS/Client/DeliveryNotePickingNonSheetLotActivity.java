package com.delta.android.WMS.Client;

import android.content.Context;
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

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.GenerateJsonNetString.MList;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionary;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Client.GridAdapter.GoodPickNonSheetGridAdapter;
import com.delta.android.WMS.Param.BDeliveryNotePickingParam;
import com.delta.android.WMS.Param.BIFetchPickStrategyParam;
import com.delta.android.WMS.Param.BIFetchProcessSheetParam;
import com.delta.android.WMS.Param.BIPDANoSheetRegisterPortalParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class DeliveryNotePickingNonSheetLotActivity extends BaseFlowActivity {

//    private EditText etSheetId;
    private Spinner cmbSheetId;
    private ImageButton ibtnSheetId;
    private Spinner cmbSeq;
    private EditText etItemId;
    private EditText etQty;
    private ImageButton ibtnQty;
    private ListView lvReg;

    private EditText etLotId;
    private Button btnConfirm;

    private DataTable dtMst;
    private DataTable dtDet;
    private DataTable dtPick;
    private DataTable dtReg;

    private DataTable dtConfigCond = null;
    private DataTable dtConfigSort = null;
    private String strConfigCond = null;
    private String strConfigSort = null;

    ArrayList<String> lstSeq = null;
    HashMap<String, String> mapSeq = new HashMap<String, String>();
    ArrayList<String> lstSheetId = new ArrayList<>();
    String storageId = "";
    String binId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_note_picking_non_sheet_lot);

        initViews();

        // 載入單據代碼至下拉選單
        GetSheetId();

        setListeners();
    }

    private void initViews(){
//        etSheetId = findViewById(R.id.etSheetId);
        cmbSheetId = findViewById(R.id.cmbSheetId);
        ibtnSheetId = findViewById(R.id.ibtnSheetIdSearch);
        cmbSeq = findViewById(R.id.cmbSeq);
        etItemId = findViewById(R.id.etItemId);
        etQty = findViewById(R.id.etQty);
        ibtnQty = findViewById(R.id.ibtnRegisterSearch);
        lvReg = findViewById(R.id.lvRegisters);

        etLotId = findViewById(R.id.etSelectLotId);
        btnConfirm = findViewById(R.id.btnConfirm);
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
                    String strSelectDeliveryNoteId = getResString(getResources().getString(R.string.SELECT_DELIVERY_NOTE_ID));
                    lstSheetId.add(strSelectDeliveryNoteId);

                    SimpleArrayAdapter adapter = new DeliveryNotePickingNonSheetLotActivity.SimpleArrayAdapter<>(DeliveryNotePickingNonSheetLotActivity.this, android.R.layout.simple_spinner_dropdown_item, lstSheetId);
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

    private void setListeners(){
        ibtnSheetId.setOnClickListener(lsSheetIdFetch);
        cmbSeq.setOnItemSelectedListener(lstSeqSelected);
        ibtnQty.setOnClickListener(lsRegisterFetch);
        lvReg.setOnItemClickListener(lvRegClick);
        btnConfirm.setOnClickListener(lsConfirm);
    }

    private View.OnClickListener lsSheetIdFetch =  new View.OnClickListener(){
        @Override
        public void onClick(View view) {

            /*
            if (etSheetId.getText().toString().equals("")){
                //WAPG017001    請輸入出通單單號
                ShowMessage(R.string.WAPG017001);
                return;
            }
             */

            int sheetIdIndex = cmbSheetId.getSelectedItemPosition();
            if (sheetIdIndex == (lstSheetId.size() - 1)) {
                ShowMessage(R.string.WAPG017001); //WAPG017001    請選擇出通單單號
                return;
            }

            FetchDeliveryNote();
        }
    };

    private Spinner.OnItemSelectedListener lstSeqSelected = new Spinner.OnItemSelectedListener(){

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            MapSeqItem();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private View.OnClickListener lsRegisterFetch = new View.OnClickListener(){

        @Override
        public void onClick(View view) {

            if(etItemId.getText().toString().equals("")){
                //WAPG017001    請輸入出通單單號
                ShowMessage(R.string.WAPG017001);
                return;
            }

            FetchRegister();
        }
    };

    private AdapterView.OnItemClickListener lvRegClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            etLotId.setText(dtReg.Rows.get(position).getValue("REGISTER_ID").toString());
            storageId = dtReg.Rows.get(position).getValue("STORAGE_ID").toString();
            binId =  dtReg.Rows.get(position).getValue("BIN_ID").toString();
        }
    };

    private View.OnClickListener lsConfirm = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            ConfirmProcess();
        }
    };

    private void FetchDeliveryNote(){

//        String dnId = etSheetId.getText().toString();
        String dnId = cmbSheetId.getSelectedItem().toString().toUpperCase().trim();

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDANoSheetRegisterPortal");
        bmObj.setModuleID("BIDeliveryNoteInfo");
        bmObj.setRequestID("BIDeliveryNoteInfo");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo param = new ParameterInfo();
        param.setParameterID(BIPDANoSheetRegisterPortalParam.DnID);
        param.setParameterValue(dnId);
        bmObj.params.add(param);

        // region 取得ConfigCond及ConfigSort
        BModuleObject biShtCfgSortAndCond = new BModuleObject();
        biShtCfgSortAndCond.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIDeliveryNotePicking");
        biShtCfgSortAndCond.setModuleID("BIFetchConfigCondAndSort");
        biShtCfgSortAndCond.setRequestID("FetchConfigCondAndSort");
        biShtCfgSortAndCond.params = new Vector<>();
        ParameterInfo paramShtId = new ParameterInfo();
        paramShtId.setParameterID(BDeliveryNotePickingParam.SheetId);
        paramShtId.setParameterValue(dnId);
        biShtCfgSortAndCond.params.add(paramShtId);
        // endregion

        List<BModuleObject> lstBmObj = new ArrayList<>();
        lstBmObj.add(bmObj);
        lstBmObj.add(biShtCfgSortAndCond);

        CallBIModule(lstBmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(!CheckBModuleReturnInfo(bModuleReturn)) return;

                dtMst = bModuleReturn.getReturnJsonTables().get("BIDeliveryNoteInfo").get("DnMst");
                dtDet = bModuleReturn.getReturnJsonTables().get("BIDeliveryNoteInfo").get("DnDet");
                dtPick = bModuleReturn.getReturnJsonTables().get("BIDeliveryNoteInfo").get("DnPicked");

                dtConfigCond = bModuleReturn.getReturnJsonTables().get("FetchConfigCondAndSort").get("SheetConfigCond");
                dtConfigSort = bModuleReturn.getReturnJsonTables().get("FetchConfigCondAndSort").get("SheetConfigSort");

                lstSeq = new ArrayList<String>();
                for(DataRow dr : dtDet.Rows){

                    Double doubleSeq =Double.parseDouble(dr.getValue("SEQ").toString());
                    Integer intSeq = Integer.valueOf(doubleSeq.intValue());

                    lstSeq.add(intSeq.toString());
                    mapSeq.put(intSeq.toString(), dr.getValue("SEQ").toString());
                }

                ArrayAdapter<String> adapterSeq = new ArrayAdapter<>(DeliveryNotePickingNonSheetLotActivity.this, android.R.layout.simple_spinner_dropdown_item, lstSeq);
                cmbSeq.setAdapter(adapterSeq);

            }
        });
    }

    private void MapSeqItem(){

        DataRow selectedRow = null;

        String seq = mapSeq.get(cmbSeq.getSelectedItem().toString());
        for(DataRow dr: dtDet.Rows){
            if(dr.getValue("SEQ").toString().equals(seq)){

                etItemId.setText(dr.getValue("ITEM_ID").toString());

                Double doubleQty =Double.parseDouble(dr.getValue("CAN_PICK_QTY").toString());
                Integer intQty = Integer.valueOf(doubleQty.intValue());
                etQty.setText(intQty.toString());
                selectedRow = dr;
                break;
            }
        }

        strConfigCond = generateExtendConfigCond(selectedRow, dtConfigCond, "REG");
        strConfigSort = generateExtendConfigSort(dtConfigSort);
    }

    private void FetchRegister(){

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDANoSheetRegisterPortal");
        bmObj.setModuleID("BIDelieveryNoteGetPickRegister");
        bmObj.setRequestID("BIDelieveryNoteGetPickRegister");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo paramReg = new ParameterInfo();
        paramReg.setParameterID(BIPDANoSheetRegisterPortalParam.RegisterId);
        paramReg.setParameterValue("");
        bmObj.params.add(paramReg);

        ParameterInfo paramItemId = new ParameterInfo();
        paramItemId.setParameterID(BIPDANoSheetRegisterPortalParam.ItemId);
        paramItemId.setParameterValue(etItemId.getText().toString());
        bmObj.params.add(paramItemId);

        ParameterInfo paramQty = new ParameterInfo();
        paramQty.setParameterID(BIPDANoSheetRegisterPortalParam.RegQty);
        Double doubleQty =Double.parseDouble(etQty.getText().toString());
        Integer intQty = Integer.valueOf(doubleQty.intValue());
        paramQty.setParameterValue(intQty.toString());
        bmObj.params.add(paramQty);

        if (strConfigCond != null && strConfigCond.length() > 0) {
            ParameterInfo paramCond = new ParameterInfo();
            paramCond.setParameterID(BIPDANoSheetRegisterPortalParam.ConfigCond);
            paramCond.setParameterValue(strConfigCond);
            bmObj.params.add(paramCond);
        }

        if (strConfigSort != null && strConfigSort.length() > 0) {
            ParameterInfo paramSort = new ParameterInfo();
            paramSort.setParameterID(BIPDANoSheetRegisterPortalParam.ConfigSort);
            paramSort.setParameterValue(strConfigSort);
            bmObj.params.add(paramSort);
        }

        CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(!CheckBModuleReturnInfo(bModuleReturn)) return;

                dtReg = bModuleReturn.getReturnJsonTables().get("BIDelieveryNoteGetPickRegister").get("Register");

                if (dtReg == null || dtReg.Rows.size() <= 0) {
                    //WAPG017003    查無可揀資料
                    ShowMessage(R.string.WAPG017003);
                    return;
                }
                GetListView();
            }
        });
    }

    private void GetListView(){
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        GoodPickNonSheetGridAdapter adapter = new GoodPickNonSheetGridAdapter(dtReg, inflater);
        lvReg.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void ConfirmProcess(){

        //region Check
        if(etLotId.getText().toString().equals("")){
            //WAPG017002    請選擇欲檢貨出庫物料
            ShowMessage(R.string.WAPG017002);
            return;
        }

        if (etQty.getText().toString().equals("")){
            //WAPG017004    請輸入揀貨數量
            ShowMessage(R.string.WAPG017004);
            return;
        }
        //endregion

        BModuleObject bimObj = new BModuleObject();
        bimObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDANoSheetRegisterPortal");
        bimObj.setModuleID("BIRegisterXfrConfirm");
        bimObj.setRequestID("BIRegisterXfrConfirm");
        bimObj.params = new Vector<ParameterInfo>();

        //region Input param
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIPDANoSheetRegisterPortalParam.RegisterId);
        param1.setParameterValue(etLotId.getText().toString());
        bimObj.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIPDANoSheetRegisterPortalParam.XfrCase);
        param2.setParameterValue("Lot");
        bimObj.params.add(param2);

        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BIPDANoSheetRegisterPortalParam.XfrTask);
        param3.setParameterValue("RegisterStockOut");
        bimObj.params.add(param3);

        ParameterInfo param5 = new ParameterInfo();
        param5.setParameterID(BIPDANoSheetRegisterPortalParam.ObjectId);
        param5.setParameterValue(etLotId.getText().toString());
        bimObj.params.add(param5);

        ParameterInfo param6 = new ParameterInfo();
        param6.setParameterID(BIPDANoSheetRegisterPortalParam.StorageId);
        param6.setParameterValue(storageId);
        bimObj.params.add(param6);

        ParameterInfo param7 = new ParameterInfo();
        param7.setParameterID(BIPDANoSheetRegisterPortalParam.BestBin);
        param7.setParameterValue(binId);
        bimObj.params.add(param7);

        ParameterInfo param8 = new ParameterInfo();
        param8.setParameterID(BIPDANoSheetRegisterPortalParam.Stock);
        param8.setParameterValue("S");
        bimObj.params.add(param8);

        ParameterInfo param9 = new ParameterInfo();
        param9.setParameterID(BIPDANoSheetRegisterPortalParam.RegQty);
        param9.setParameterValue(etQty.getText().toString());
        bimObj.params.add(param9);

        HashMap<String, Integer> dicDnSeq = new HashMap<String, Integer>();
//        dicDnSeq.put(etSheetId.getText().toString(), Integer.parseInt(cmbSeq.getSelectedItem().toString()));
        dicDnSeq.put(cmbSheetId.getSelectedItem().toString().toUpperCase().trim(), Integer.parseInt(cmbSeq.getSelectedItem().toString()));

        VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create(VirtualClass.VirtualClassType.Decimal);
        MesSerializableDictionary msd = new MesSerializableDictionary(vKey, vVal);
        String strDnSeq = msd.generateFinalCode(dicDnSeq);

        ParameterInfo param10 = new ParameterInfo();
        param10.setParameterID(BIPDANoSheetRegisterPortalParam.DicDeliveryNote);
        param10.setNetParameterValue(strDnSeq);
        bimObj.params.add(param10);

        ParameterInfo param11 = new ParameterInfo();
        param11.setParameterID(BIPDANoSheetRegisterPortalParam.ItemId);
        param11.setParameterValue(etItemId.getText().toString());
        bimObj.params.add(param11);
        //endregion

        CallBIModule(bimObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (!CheckBModuleReturnInfo(bModuleReturn)) return;
                //WAPG017005    檢貨完成
                ShowMessage(R.string.WAPG017005);
                initData();
            }
        });
    }

    private void initData(){
        etQty.setText("");
        etLotId.setText("");
        etItemId.setText("");
//        etSheetId.setText("");
        cmbSheetId.setSelection(lstSheetId.size()-1); // spinner設定回預設選項

        dtMst = new DataTable();
        dtDet = new DataTable();
        dtPick = new DataTable();
        dtReg = new DataTable();

        lstSeq = new ArrayList<>();
        mapSeq = new HashMap<String, String>();
        storageId = "";
        binId = "";

        GetListView();

        ArrayAdapter<String> adapterSeq = new ArrayAdapter<>(DeliveryNotePickingNonSheetLotActivity.this, android.R.layout.simple_spinner_dropdown_item, lstSeq);
        cmbSeq.setAdapter(adapterSeq);
    }

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
// Error Code WAPG017
//WAPG017001    請選擇出通單單號
//WAPG017002    請選擇欲檢貨出庫物料
//WAPG017003    查無可揀資料
//WAPG017004    請輸入揀貨數量
//WAPG017005    揀貨完成