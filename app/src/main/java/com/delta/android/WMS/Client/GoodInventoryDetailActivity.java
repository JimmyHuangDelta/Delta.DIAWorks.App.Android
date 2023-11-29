package com.delta.android.WMS.Client;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
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
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.BGoodInventoryParam;
import com.delta.android.WMS.Param.ParamObj.InventoryObj;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class GoodInventoryDetailActivity extends BaseFlowActivity {

    // 宣告控制項物件
    private EditText txtLotID;
    private Spinner cmbBin;
    private EditText txtQty;
    private ListView lstDetail;
    private ImageButton btnConfirm;
    private DataTable dtBin = new DataTable();
    HashMap<String, String> map = new HashMap<String, String>();//ListView點到的那筆
    DataTable dtDet = new DataTable();
    String SheetID;
    String SheetTypePolicyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_good_inventory_detail);

        // Get Detail table From DeliveryNoteShipDetail.activity
        dtDet = (DataTable) getIntent().getSerializableExtra("DataTable");
        Bundle bundle = getIntent().getExtras();
        SheetID = bundle.getString("SHEET_ID");
        SheetTypePolicyId = bundle.getString("SHEET_TYPE_POLICY_ID");


        // 取得控制項物件
        //txtLotID = findViewById(R.id.txtLotId);
        lstDetail = findViewById(R.id.listViewDet);
        //btnConfirm = findViewById(R.id.btnConfirm);

        // 取得物件後才塞入Detail
        GetDetail();

        // 設定監聽事件
        setListensers();
    }

    //設定監聽事件
    private void setListensers() {
        lstDetail.setOnItemClickListener(OnBin);
    }

    /*public class simpleArrayAdapter<T> extends ArrayAdapter {
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

    }*/

    private AdapterView.OnItemClickListener OnBin = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (lstDetail.getCount() <= 0) return;
            map = (HashMap<String, String>) lstDetail.getItemAtPosition(position);
            GetBin();
        }
    };

    private void GetBin() {
        // Call BIModule
        BModuleObject bmObjBin = new BModuleObject();
        bmObjBin.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObjBin.setModuleID("BIFetchBin");
        bmObjBin.setRequestID("BIFetchBin");
        bmObjBin.params = new Vector<ParameterInfo>();

        ParameterInfo paramBin = new ParameterInfo();
        paramBin.setParameterID(BIWMSFetchInfoParam.Filter);
        paramBin.setParameterValue("AND STORAGE_ID = '" + map.get("STORAGE_ID") + "'"); // 倉庫代碼為查詢條件
        bmObjBin.params.add(paramBin);

        CallBIModule(bmObjBin, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {

                    dtBin = null;
                    dtBin = bModuleReturn.getReturnJsonTables().get("BIFetchBin").get("BIN");
                    HashMap<String, String> mapBin = new HashMap<String, String>();
                    ArrayList<String> arrListBin = new ArrayList<String>();
                    //arrListBin.add("請選擇儲位");
                    //mapBin.put("請選擇儲位", "null");
                    Iterator it = dtBin.Rows.iterator();
                    int i = 0;
                    while (it.hasNext()) {
                        DataRow row = (DataRow) it.next();
                        arrListBin.add(i, row.getValue("IDNAME").toString());
                        mapBin.put(row.getValue("IDNAME").toString(), row.getValue("BIN_ID").toString());
                        i++;
                    }

                    ShowDialog(arrListBin);
                }
            }
        });
    }

    private void GetDetail() {
        List<HashMap<String, String>> list = new ArrayList<>();

        //region 暫時隱藏之前塞資料的Code
        /*ArrayList<String> listSeq = new ArrayList<String>();
        ArrayList<String> listItemId = new ArrayList<String>();
        ArrayList<String> listItemName = new ArrayList<String>();
        ArrayList<String> listLotId = new ArrayList<String>();
        ArrayList<String> listQty = new ArrayList<String>();
        ArrayList<String> lisStorageId = new ArrayList<String>();
        ArrayList<String> listProcQty = new ArrayList<String>();
        Iterator it = dtDet.Rows.iterator();
        int i = 0;
        while (it.hasNext()) {
            DataRow row = (DataRow) it.next();
            listSeq.add(i, row.getValue("SEQ").toString().replace(".0",""));
            listItemId.add(i, row.getValue("ITEM_ID").toString());
            listItemName.add(i, row.getValue("ITEM_NAME").toString());
            lisStorageId.add(i, row.getValue("STORAGE_ID").toString());
            listLotId.add(i, row.getValue("LOT_ID").toString());
            listQty.add(i, row.getValue("QTY").toString().replace(".0",""));
            listProcQty.add(i, row.getValue("PROC_QTY").toString().replace(".0",""));
            i++;
        }
        for (int j = 0; j < listItemId.size(); j++) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("SEQ", listSeq.get(j));
            hashMap.put("ITEM_ID", listItemId.get(j));
            hashMap.put("ITEM_NAME", listItemName.get(j));
            hashMap.put("STORAGE_ID", lisStorageId.get(j));
            hashMap.put("LOT_ID", listLotId.get(j));
            hashMap.put("QTY", listQty.get(j));
            hashMap.put("PROC_QTY", listProcQty.get(j));
            //把title , text存入HashMap之中
            list.add(hashMap);
        }*/
        //endregion

        for (DataRow dr : dtDet.Rows
        ) {
            //上架數量-已處理數量 不等於0才顯示在畫面，表示未完成上架動作
            if (Double.valueOf(dr.getValue("QTY").toString()) - Double.valueOf(dr.getValue("PROC_QTY").toString()) != 0 )
            {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("SEQ", dr.getValue("SEQ").toString().replace(".0", ""));
                hashMap.put("ITEM_ID", dr.getValue("ITEM_ID").toString());
                hashMap.put("ITEM_NAME", dr.getValue("ITEM_NAME").toString());
                hashMap.put("STORAGE_ID", dr.getValue("STORAGE_ID").toString());
                hashMap.put("LOT_ID", dr.getValue("LOT_ID").toString());
                hashMap.put("QTY", dr.getValue("QTY").toString().replace(".0", ""));
                hashMap.put("PROC_QTY", dr.getValue("PROC_QTY").toString().replace(".0", ""));
                list.add(hashMap);
            }
        }

        ListAdapter adapter = new SimpleAdapter(
                GoodInventoryDetailActivity.this,
                list,
                R.layout.activity_wms_good_inventory_detail_listview,
                new String[]{"SEQ", "ITEM_ID", "ITEM_NAME", "STORAGE_ID", "LOT_ID", "QTY", "PROC_QTY"},
                new int[]{R.id.txtSEQ, R.id.txtItemId, R.id.txtItemName, R.id.txtStorageId, R.id.txtLotId, R.id.txtQty, R.id.txtProcQty}
        );
        lstDetail.setAdapter(adapter);
    }

    private void ShowDialog(ArrayList<String> arrListBin) {
        LayoutInflater inflater = LayoutInflater.from(GoodInventoryDetailActivity.this);
        View view = inflater.inflate(R.layout.activity_wms_good_inventory_detail_listview_dialog, null);

        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(GoodInventoryDetailActivity.this);
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

        Button btnConfirm = view.findViewById(R.id.btnOk);
        //cmbBin = view.findViewById(R.id.txtBinID);
        txtQty = view.findViewById(R.id.txtQty);
        txtQty.setText(map.get("QTY"));

        //txtQty.setEnabled(false);
        //simpleArrayAdapter adapter = new simpleArrayAdapter<>(GoodInventoryDetailActivity.this, android.R.layout.simple_spinner_dropdown_item, arrListBin);
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //ArrayAdapter<String> adapter = new ArrayAdapter<>(GoodInventoryDetailActivity.this, android.R.layout.simple_spinner_dropdown_item, arrListBin);
        //cmbBin.setAdapter(adapter);
        //cmbBin.setSelection(arrListBin.size() - 1, true);

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //檢查儲位代碼是否輸入
                //if (cmbBin.getSelectedItem().toString().equals("")) {
                //    ShowMessage(R.string.WAPG004008);//WAPG004008    請輸入儲位代碼
                //    return;
                //}

                //檢查數量是否輸入
                if (txtQty.getText().toString().trim().equals("")) {
                    ShowMessage(R.string.WAPG004009);//WAPG004009    請輸入數量
                    return;
                }

                //檢查輸入數量是否超出可上架數量
                if (Double.valueOf(txtQty.getText().toString().trim()) > Double.valueOf(map.get("QTY")) - Double.valueOf(map.get("PROC_QTY"))) {
                    ShowMessage(R.string.WAPG004010);//WAPG004010    上架數量超出剩餘可處理數量
                    return;
                }
                ExecutoProcess();
                dialog.dismiss();
            }
        });
    }

    //執行單據變更(出貨)
    private void ExecutoProcess() {

        List<InventoryObj> lstInventoryObj = new ArrayList<InventoryObj>();

        InventoryObj inventoryObj = new InventoryObj();
        inventoryObj.setSheetId(SheetID);
        inventoryObj.setSeq(Integer.valueOf(map.get("SEQ")));
        inventoryObj.setItemId(map.get("ITEM_ID"));
        inventoryObj.setStorageId(map.get("STORAGE_ID"));
        inventoryObj.setLotId(map.get("LOT_ID"));
        //inventoryObj.setBinId(cmbBin.getSelectedItem().toString());
        inventoryObj.setInventoryQty(Integer.valueOf(txtQty.getText().toString().trim()));

        lstInventoryObj.add(inventoryObj);

        // Add param
        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.INV.Parameter.ParameterObj.InventoryObj", "bmWMS.INV.Param");
        MList mListEnum = new MList(vListEnum);
        String strLsRelatData = mListEnum.generateFinalCode(lstInventoryObj);

        // Call BModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BGoodInventory");
        bmObj.setModuleID("");
        bmObj.setRequestID("BGoodInventory");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo paramInvItemObj = new ParameterInfo();
        paramInvItemObj.setParameterID(BGoodInventoryParam.InvItemObj);
        paramInvItemObj.setNetParameterValue(strLsRelatData);
        bmObj.params.add(paramInvItemObj);

        ParameterInfo paramSheetTypePolicyId = new ParameterInfo();
        paramSheetTypePolicyId.setParameterID(BGoodInventoryParam.SheetTypePolicyId);
        paramSheetTypePolicyId.setParameterValue(SheetTypePolicyId);
        bmObj.params.add(paramSheetTypePolicyId);

        CallBModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    ShowMessage(R.string.WAPG004007);//WAPG004007  作業成功
                    txtLotID.selectAll();
                }
            }
        });
    }
}
//Error Code
//WAPG004001    請輸入單據代碼
//WAPG004002    查無單據資料
//WAPG004003    單據未確認
//WAPG004004    查無揀貨資料
//WAPG004005    出通單所綁定的揀貨狀態不為Closed
//WAPG004006    請選擇單據類型
//WAPG004007    作業成功
//WAPG004008    請輸入儲位代碼
//WAPG004009    請輸入數量
//WAPG004009    上架數量超出剩餘可處理數量
