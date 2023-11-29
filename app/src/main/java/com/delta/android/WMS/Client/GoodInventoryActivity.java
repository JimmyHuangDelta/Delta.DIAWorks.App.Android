package com.delta.android.WMS.Client;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.DataTable.DataColumnCollection;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionaryList;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.ParamObj.Condition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class GoodInventoryActivity extends BaseFlowActivity {

    //Function ID = 'WAPG004'
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_good_inventory);

        // 取得控制項物件
        initViews();

        // 設定監聽事件
        setListensers();

        // 取得單據類型
        GetSheetType();
    }

    // 宣告控制項物件
    private HashMap<String, String> mapSheetTypeKey = new HashMap<String, String>();
    private Spinner cmbSheetType;
    private ImageButton btnSearch;
    private EditText txtSheetID;
    private ListView listView;
    private DataTable dtDet = new DataTable();
    private DataTable dtBin = new DataTable();
    String strSheetTypePolicyId;

    //取得控制項物件
    private void initViews() {
        cmbSheetType = findViewById(R.id.cmbSheetType);
        btnSearch = findViewById(R.id.btnSearch);
        txtSheetID = findViewById(R.id.txtSheetID);
        //listView = findViewById(R.id.listViewDet);
    }

    //設定監聽事件
    private void setListensers() {
        btnSearch.setOnClickListener(GetDetail);
        //btnOk.setOnClickListener(OK);
        //listView.setOnItemClickListener(OnBin);
    }

    private View.OnClickListener GetDetail = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            GetDetail();
        }
    };

    public class simpleArrayAdapter<T> extends ArrayAdapter {
        public simpleArrayAdapter(Context context, int resource, List<T> objects) {
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

    private void GetSheetType() {
        // Call BIModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchSheetType");
        bmObj.setRequestID("SheetTypeAll");

        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Filter);
        param1.setParameterValue(" AND P.SHEET_TYPE_POLICY_ID IN ('Warehouse','Receipt','Return','Transfer') "); // Sheet Type Policy = 轉倉 or 發料
        bmObj.params.add(param1);

        CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    DataTable dt = bModuleReturn.getReturnJsonTables().get("SheetTypeAll").get("SHEET_TYPE");
                    ArrayList<String> stringArrayList = new ArrayList<String>();
                    stringArrayList.add("請選擇單據類型");
                    mapSheetTypeKey.put("請選擇單據類型", "null");
                    Iterator it = dt.Rows.iterator();
                    int i = 0;
                    while (it.hasNext()) {
                        DataRow row = (DataRow) it.next();
                        stringArrayList.add(i, row.getValue("IDNAME").toString());
                        mapSheetTypeKey.put(row.getValue("IDNAME").toString(), row.getValue("SHEET_TYPE_ID").toString());
                        i++;
                    }

                    simpleArrayAdapter adapter = new simpleArrayAdapter<>(GoodInventoryActivity.this, android.R.layout.simple_spinner_dropdown_item, stringArrayList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    cmbSheetType.setAdapter(adapter);
                    cmbSheetType.setSelection(stringArrayList.size() - 1, true);
                }
            }
        });
    }


    private void GetDetail() {
        //檢查單據類型是否選擇
        strSheetTypePolicyId = mapSheetTypeKey.get(cmbSheetType.getSelectedItem().toString());
        if (strSheetTypePolicyId.equals("null")) {
            ShowMessage(R.string.WAPG004006);//WAPG004006  請選擇單據類型
            return;
        }

        //檢查單據代碼是否輸入
        if (txtSheetID.getText().toString().trim().equals("")) {
            ShowMessage(R.string.WAPG004001);//WAPG004001  請輸入單據代碼
            return;
        }

        BModuleObject bmObj = new BModuleObject();
        BModuleObject bmObj2 = new BModuleObject();
        Condition conditionSheetID = new Condition();
        switch (strSheetTypePolicyId) {
            case "Warehouse"://入庫
                bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
                bmObj.setModuleID("BIFetchWarehouseVoucherMstAndDet");
                bmObj.setRequestID("BIFetchWarehouseVoucherMstAndDet");
                bmObj.params = new Vector<ParameterInfo>();

                conditionSheetID.setAliasTable("M");
                conditionSheetID.setColumnName("WV_ID");
                break;
            case "Receipt"://收料
                bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
                bmObj.setModuleID("BIFetchGoodReceipt");
                bmObj.setRequestID("BIFetchGoodReceipt");
                bmObj.params = new Vector<ParameterInfo>();

                bmObj2.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
                bmObj2.setModuleID("BIFetchGoodReceiptAndReceive");
                bmObj2.setRequestID("BIFetchGoodReceiptAndReceive");
                bmObj2.params = new Vector<ParameterInfo>();

                conditionSheetID.setAliasTable("M");
                conditionSheetID.setColumnName("GR_ID");
                break;
            case "Return"://退料
            case "Transfer"://轉倉
                bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
                bmObj.setModuleID("BIFetchSheetMstAndDet");
                bmObj.setRequestID("BIFetchSheetMstAndDet");
                bmObj.params = new Vector<ParameterInfo>();

                conditionSheetID.setAliasTable("M");
                conditionSheetID.setColumnName("SHEET_ID");
                break;
            default:
                break;
        }


        // Set conditionSheetID
        conditionSheetID.setValue(txtSheetID.getText().toString().trim());
        conditionSheetID.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());

        // Set conditionSheetType
        Condition conditionSheetType = new Condition();
        conditionSheetType.setAliasTable("ST");
        conditionSheetType.setColumnName("SHEET_TYPE_ID");
        conditionSheetType.setValue(strSheetTypePolicyId);
        conditionSheetType.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());

        List<Condition> lstCondition = new ArrayList<Condition>();
        lstCondition.add(conditionSheetID);
        List<Condition> lstCondition2 = new ArrayList<Condition>();
        lstCondition2.add(conditionSheetType);

        HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
        mapCondition.put(conditionSheetID.getColumnName(), lstCondition);
        mapCondition.put(conditionSheetType.getColumnName(), lstCondition2);
        VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vkey, vVal);
        String strCond = msdl.generateFinalCode(mapCondition);

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Condition);
        param1.setNetParameterValue(strCond); // 要用set"Net"ParameterValue
        bmObj.params.add(param1);
        if (strSheetTypePolicyId.equals("Receipt")) {
            bmObj2.params.add(param1);
        }

        List<BModuleObject> lstBmObj = new ArrayList<BModuleObject>();
        lstBmObj.add(bmObj);
        if (strSheetTypePolicyId.equals("Receipt")) {
            lstBmObj.add(bmObj2);
        }

        BModuleObject bmObjBin = new BModuleObject();
        bmObjBin.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObjBin.setModuleID("BIFetchBin");
        bmObjBin.setRequestID("BIFetchBin");
        lstBmObj.add(bmObjBin);

        CallBIModule(lstBmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {

                    DataTable dtMst = null;
                    dtDet = null;

                    dtBin = bModuleReturn.getReturnJsonTables().get("BIFetchBin").get("BIN");

                    switch (strSheetTypePolicyId) {
                        case "Warehouse"://入庫
                            dtMst = bModuleReturn.getReturnJsonTables().get("BIFetchWarehouseVoucherMstAndDet").get("WvMst");
                            dtDet = bModuleReturn.getReturnJsonTables().get("BIFetchWarehouseVoucherMstAndDet").get("WvDet");
                            break;
                        case "Receipt"://收料
                            dtMst = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceipt").get("GrMst");
                            dtDet = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceiptAndReceive").get("GrrDet");
                            break;
                        case "Return"://退料
                        case "Transfer"://轉倉
                            dtMst = bModuleReturn.getReturnJsonTables().get("BIFetchSheetMstAndDet").get("Mst");
                            dtDet = bModuleReturn.getReturnJsonTables().get("BIFetchSheetMstAndDet").get("Det");
                            break;
                    }

                    //Check Data
                    if (dtMst == null || dtMst.Rows.size() == 0) {
                        ShowMessage(R.string.WAPG004002);//WAPG004002  查無單據資料
                        txtSheetID.selectAll();
                        //listView.setAdapter(null);
                        return;
                    }

                    switch (strSheetTypePolicyId) {
                        case "Warehouse"://入庫
                            ((DataColumnCollection) dtMst.getColumns()).get("WV_ID").setColumnName("MTL_SHEET_ID");
                            ((DataColumnCollection) dtMst.getColumns()).get("WV_STATUS").setColumnName("MTL_SHEET_STATUS");
                            ((DataColumnCollection) dtMst.getColumns()).get("WV_SOURCE").setColumnName("SOURCE");
                            ((DataColumnCollection) dtMst.getColumns()).get("WV_DET_REF_KEY").setColumnName("SHEET_REF_KEY");
                            ((DataColumnCollection) dtDet.getColumns()).get("WV_ID").setColumnName("MTL_SHEET_ID");
                            ((DataColumnCollection) dtDet.getColumns()).get("WV_DET_REF_KEY").setColumnName("SHEET_REF_KEY");
                            break;
                        case "Receipt"://收料
                            ((DataColumnCollection) dtMst.getColumns()).get("GR_ID").setColumnName("MTL_SHEET_ID");
                            ((DataColumnCollection) dtMst.getColumns()).get("GR_STATUS").setColumnName("MTL_SHEET_STATUS");
                            ((DataColumnCollection) dtMst.getColumns()).get("GR_SOURCE").setColumnName("SOURCE");
                            ((DataColumnCollection) dtMst.getColumns()).get("GR_MST_KEY").setColumnName("SHEET_REF_KEY");
                            ((DataColumnCollection) dtDet.getColumns()).get("GR_ID").setColumnName("MTL_SHEET_ID");
                            ((DataColumnCollection) dtDet.getColumns()).get("GR_MST_KEY").setColumnName("SHEET_REF_KEY");
                            break;
                        case "Return"://退料
                        case "Transfer"://轉倉
                            ((DataColumnCollection) dtMst.getColumns()).get("SHEET_ID").setColumnName("MTL_SHEET_ID");
                            ((DataColumnCollection) dtMst.getColumns()).get("SHEET_DATE").setColumnName("MTL_SHEET_DATE");
                            ((DataColumnCollection) dtMst.getColumns()).get("SHEET_STATUS").setColumnName("MTL_SHEET_STATUS");
                            ((DataColumnCollection) dtMst.getColumns()).get("SHEET_DATA_SOURCE").setColumnName("SOURCE");
                            ((DataColumnCollection) dtDet.getColumns()).get("SHEET_ID").setColumnName("MTL_SHEET_ID");
                            ((DataColumnCollection) dtDet.getColumns()).get("TO_STORAGE_KEY").setColumnName("STORAGE_KEY");
                            ((DataColumnCollection) dtDet.getColumns()).get("TO_STORAGE_ID").setColumnName("STORAGE_ID");
                            ((DataColumnCollection) dtDet.getColumns()).get("TRX_QTY").setColumnName("QTY");
                            ((DataColumnCollection) dtDet.getColumns()).get("TRX_CMT").setColumnName("CMT");
                            ((DataColumnCollection) dtDet.getColumns()).get("ITEM_UOM").setColumnName("UOM");
                            break;
                    }

                    if (!dtMst.Rows.get(0).getValue("MTL_SHEET_STATUS").toString().equals("Closed")) {
                        ShowMessage(R.string.WAPG004005);//WAPG004005    揀貨狀態不為Closed
                        txtSheetID.selectAll();
                        //listView.setAdapter(null);
                        return;
                    }

                    /*List<HashMap<String, String>> list = new ArrayList<>();
                    ArrayList<String> listSeq = new ArrayList<String>();
                    ArrayList<String> listItemId = new ArrayList<String>();
                    ArrayList<String> listItemName = new ArrayList<String>();
                    ArrayList<String> listLotId = new ArrayList<String>();
                    ArrayList<String> listQty = new ArrayList<String>();
                    Iterator it = dtDet.Rows.iterator();
                    int i = 0;
                    while (it.hasNext()) {
                        DataRow row = (DataRow) it.next();
                        listSeq.add(i, row.getValue("SEQ").toString());
                        listItemId.add(i, row.getValue("ITEM_ID").toString());
                        listItemName.add(i, row.getValue("ITEM_NAME").toString());
                        listLotId.add(i, row.getValue("LOT_ID").toString());
                        listQty.add(i, row.getValue("QTY").toString());
                        i++;
                    }
                    for (int j = 0; j < listItemId.size(); j++) {
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("SEQ", listSeq.get(j));
                        hashMap.put("ITEM_ID", listItemId.get(j));
                        hashMap.put("ITEM_NAME", listItemName.get(j));
                        hashMap.put("LOT_ID", listLotId.get(j));
                        hashMap.put("QTY", listQty.get(j));
                        //把title , text存入HashMap之中
                        list.add(hashMap);
                    }

                    ListAdapter adapter = new SimpleAdapter(
                            GoodInventoryActivity.this,
                            list,
                            R.layout.activity_wms_delivery_note_ship_detail_listview,
                            new String[]{"SEQ", "ITEM_ID", "ITEM_NAME", "LOT_ID", "QTY"},
                            new int[]{R.id.txtSEQ, R.id.txtItemId, R.id.txtItemName, R.id.txtLotId, R.id.txtQty}
                    );
                    listView.setAdapter(adapter);*/
                }


                Intent intent = new Intent();
                intent.setClass(GoodInventoryActivity.this, GoodInventoryDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("DataTable", dtDet); // 直接傳DataTable -> PageTwo會Crash
                //bundle.putSerializable("DataTable", dtBin); // 直接傳DataTable -> PageTwo會Crash
                bundle.putString("SHEET_ID", txtSheetID.getText().toString().trim());
                bundle.putString("SHEET_TYPE_POLICY_ID", strSheetTypePolicyId);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

    }

    /*private AdapterView.OnItemClickListener OnBin = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (listView.getCount() <= 0) return;
            ShowDialog("單據編號: " + txtSheetID.getText().toString().trim() + ", 是否確定出庫?");
        }
    };

    private void ShowDialog(String Message) {
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View view = inflater.inflate(R.layout.activity_wms_good_inventory_detail_listview_dialog, null);

        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(GoodInventoryActivity.this);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);//只允許按下dialog裡的按鍵才能關閉dialog
        dialog.show();

        Button btnCloseDialog = view.findViewById(R.id.btnCancel);
        btnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }*/
}

//Error Code
//WAPG004001    請輸入單據代碼
//WAPG004002    查無單據資料
//WAPG004003    單據未確認
//WAPG004004    查無揀貨資料
//WAPG004005    揀貨狀態不為Closed
//WAPG004006    請選擇單據類型
//WAPG004007    作業成功
