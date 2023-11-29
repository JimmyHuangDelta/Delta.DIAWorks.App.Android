package com.delta.android.WMS.Client;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.Activity.ScanActivity;
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
import com.delta.android.WMS.Client.GridAdapter.SheetCancelDetGridAdapter;
import com.delta.android.WMS.Client.GridAdapter.SheetDetGridAdapter;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.BIWMSPickByLightParam;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GoodCancelPickDetailActivity extends BaseFlowActivity {

    private static int requestCode = 1; //需要接收回傳資訊時使用
    private final int ITEM_ID_QRSCAN_REQUEST_CODE = 11; // 相機掃描回傳資訊使用
    private ViewHolder holder = null;
    private DataTable sheetDetTable;
    private DataTable pickDetTable, pickAndRsvTable;
    DataTable alreadyPickedData;
    private HashMap<String,String> ActualQtyStatus;
    private HashMap<String,String> PickStrategy;
    private int count = 0;
    HashMap<String, String>mapSheet = new HashMap<>(); //紀錄揀料單對應的原始單據

    static class ViewHolder {
        ListView SheetDetData1;
        EditText ItemId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good_cancel_pick_detail);

        count = 1;
        this.initialControl();
        this.initialData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {

        IntentResult result = IntentIntegrator.parseActivityResult(resultCode, dataIntent);
        count = 2;
        if (result != null) {
            if (result.getContents() == null) {
                switch (requestCode) {
                    case 1:
                        holder.ItemId.setText("");
                        this.getPickDet(sheetDetTable);
                        break;
                    case ITEM_ID_QRSCAN_REQUEST_CODE:
                        Toast.makeText(this, getResources().getText(R.string.QRCODE_SCAN_CANCEL).toString(), Toast.LENGTH_SHORT).show();
                        break;
                }
            } else {
                if (requestCode == ITEM_ID_QRSCAN_REQUEST_CODE)
                    holder.ItemId.setText(result.getContents().trim());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, dataIntent);
            //回傳資料
            //String sheetId = sheetDetTable.Rows.get(0).getValue("SHEET_ID").toString();
            //this.getPickDet(sheetId);

//            holder.ItemId.setText("");
//            this.getPickDet(sheetDetTable);

//        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        holder.SheetDetData1.setAdapter(new SheetDetGridAdapter(filterWhetherDetsArePicked(false), inflater, pickDetTable, false));
//        holder.Tabhost.setCurrentTab(0);
        }
    }

    private void initialData() {
        sheetDetTable = (DataTable) getIntent().getSerializableExtra("SheetDet");
        ActualQtyStatus = (HashMap<String, String>) getIntent().getSerializableExtra("actualQtyStatus");
        //PickStrategy = (HashMap<String, String>) getIntent().getSerializableExtra("pickingStrategy");; //pickingStrategy
        mapSheet = (HashMap<String, String>) getIntent().getSerializableExtra("MapSheet");
        this.getPickDet(sheetDetTable);
    }

    private void initialControl() {
        if (holder != null) return;

        holder = new ViewHolder();
        holder.ItemId = findViewById(R.id.etSheetDetItemId);
        holder.SheetDetData1 = findViewById(R.id.lvSheetDetData1);

        holder.ItemId.setOnEditorActionListener(filterSheetDet);
        holder.SheetDetData1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DataRow chooseRow = (DataRow) parent.getAdapter().getItem(position);
                //DataRow chooseRow = sheetDetTable.Rows.get(position);
                //getAlreadyPickedData(chooseRow);

                final DataTable dt = new DataTable();
                HashMap<String, String> actualQtyStatus = new HashMap<>();
                actualQtyStatus.put(chooseRow.getValue("SHEET_ID").toString(), ActualQtyStatus.get(chooseRow.getValue("SHEET_ID").toString()));

                dt.Rows.add(chooseRow);
                Bundle chooseSheetDet = new Bundle();
                chooseSheetDet.putSerializable("DetTable", dt);
                chooseSheetDet.putSerializable("actualQtyStatus", actualQtyStatus);
                chooseSheetDet.putSerializable("PickDetTable", pickDetTable);
                //chooseSheetDet.putSerializable("pickingStrategy", PickStrategy);
                //chooseSheetDet.putSerializable("ConfigCond", dtConfigCond);
                //chooseSheetDet.putSerializable("ConfigSort", dtConfigSort);

                //如果希望使用回傳的資料，則需要用此方法
                Intent intent = new Intent(GoodCancelPickDetailActivity.this, GoodCancelPickExecutedActivity.class);
                intent.putExtras(chooseSheetDet);
                startActivityForResult(intent, requestCode);

            }
        });
    }

    private DataTable filterWhetherDetsArePicked() {
        DataTable showTable = new DataTable();
        HashMap<String, Double> dicShowData = new HashMap<>();
        if (pickDetTable != null && pickDetTable.Rows.size() != 0) {
            for (DataRow drDet : sheetDetTable.Rows) {
                double pickQty = 0.0;
                //將已揀料資訊的數量家總
                for (DataRow drPick : pickDetTable.Rows) {
                    if (drDet.getValue("SHEET_MST_KEY").equals(drPick.getValue("SHEET_MST_KEY"))
                            && drDet.getValue("SEQ").equals(drPick.getValue("SEQ"))) {
                        pickQty += Double.parseDouble(drPick.getValue("QTY").toString());
                    }
                }
                dicShowData.put(drDet.getValue("SHEET_MST_KEY").toString() + drDet.getValue("SEQ").toString(), pickQty);
            }
        } else {
            return showTable;
        }

        for (DataRow dr : sheetDetTable.Rows) {
            String key = dr.getValue("SHEET_MST_KEY").toString() + dr.getValue("SEQ").toString();
            double trxQty = Double.parseDouble(dr.getValue("TRX_QTY").toString());
            if (dicShowData.size() == 0) return showTable;
            if (Double.compare(trxQty, dicShowData.get(key)) <= 0) {
                showTable.Rows.add(dr);
            }
        }
        return showTable;
    }

    private void getPickDet(DataTable sheetDet) {
        BModuleObject biObj = new BModuleObject();
        biObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        biObj.setModuleID("BIFetchSheetPickMstAndDet");
        biObj.setRequestID("BIFetchPickDet");
        biObj.params = new Vector<>();

        VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");

        final List<Condition> conditions = new ArrayList<>();
        HashMap<String, List<?>> dicCondition = new HashMap<>();

        ArrayList<String> ids = new ArrayList<String>();
        for (DataRow dr : sheetDet.Rows) {
            if (ids.contains(dr.getValue("SHEET_ID").toString()) == false) {
                ids.add(dr.getValue("SHEET_ID").toString());
            }
        }

        for (String id : ids) {
            Condition condition = new Condition();
            condition.setAliasTable("MST");
            condition.setColumnName("SHEET_ID");
            condition.setDataType("string");
            condition.setValue(id);
            conditions.add(condition);
        }
        dicCondition.put("SHEET_ID", conditions);

        // 20211005 Hans 增加 SHEET_TYPE_ID與 Winform查詢條件一致
        // 一開始會搜尋同SHEET_TYPE_ID的資料 故直接取第一筆
        //String sheetTypeId = sheetDet.Rows.get(0).getValue("SHEET_TYPE_ID").toString();
//
        //List<Condition> cond = new ArrayList<>();
        //Condition condSheetTypeId = new Condition();
        //condSheetTypeId.setAliasTable("ST");
        //condSheetTypeId.setColumnName("SHEET_TYPE_ID");
        //condSheetTypeId.setDataType("string");
        //condSheetTypeId.setValue(sheetTypeId);
        //cond.add(condSheetTypeId);
        //dicCondition.put("SHEET_TYPE_ID", cond);

        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vKey, vVal);
        String serializedString = msdl.generateFinalCode(dicCondition);
        ParameterInfo param = new ParameterInfo();
        param.setParameterID(BIWMSFetchInfoParam.Condition);
        param.setNetParameterValue(serializedString);
        biObj.params.add(param);

        CallBIModule(biObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    pickAndRsvTable = bModuleReturn.getReturnJsonTables().get("BIFetchPickDet").get("ShtPickDet");

                    //將非預約揀貨的資料篩選出來
                    pickDetTable = new DataTable();

                    if (!pickAndRsvTable.getColumns().contains("SOURCE_SHEET_ID"))
                    {
                        DataColumn dcSource = new DataColumn("SOURCE_SHEET_ID");
                        pickAndRsvTable.addColumn(dcSource);

                        for (DataRow dr : pickAndRsvTable.Rows)
                        {
                            dr.setValue("SOURCE_SHEET_ID", mapSheet.get(dr.getValue("SHEET_ID").toString()));
                        }
                    }

                    if (pickAndRsvTable != null || pickAndRsvTable.Rows.size() > 0) {
                        for (DataRow dr : pickAndRsvTable.Rows) {
                            if (dr.getValue("IS_PICKED").toString().equals(("Y"))) {
                                pickDetTable.Rows.add(dr);
                            }
                        }
                    }

                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    //holder.SheetDetData1.setAdapter(new SheetDetGridAdapter(filterWhetherDetsArePicked(false), inflater, pickDetTable, false, GoodPickDetailActivity.this));
                    holder.SheetDetData1.setAdapter(new SheetCancelDetGridAdapter(sheetDetTable, inflater, pickAndRsvTable,  false, GoodCancelPickDetailActivity.this));
                }
            }
        });
    }

    private TextView.OnEditorActionListener filterSheetDet = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (sheetDetTable == null || sheetDetTable.Rows.size() == 0) return false;
            DataTable dtFilterPicked = null;
            dtFilterPicked = filterWhetherDetsArePicked();

            DataTable filterTable = new DataTable();
            String filterId = holder.ItemId.getText().toString().toUpperCase().trim(); //20200729 archie 轉大寫
            if (filterId == null || filterId.equals("")) {
                filterTable = dtFilterPicked;
            } else {
                for (DataRow dr : dtFilterPicked.Rows) {
                    if (dr.getValue("ITEM_ID").toString().equals(filterId)) {
                        filterTable.Rows.add(dr);
                    }
                }
            }

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            SheetCancelDetGridAdapter adapter = new SheetCancelDetGridAdapter(sheetDetTable, inflater, pickDetTable, false, GoodCancelPickDetailActivity.this);
            holder.SheetDetData1.setAdapter(adapter);
            return false;
        }
    };

    public void OnClickQRScan(View v) {
        IntentIntegrator integrator = new IntentIntegrator(GoodCancelPickDetailActivity.this);
        // 設置要掃描的條碼類型，ONE_D_CODE_TYPES：一維碼，QR_CODE_TYPES-二維碼
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("BarCode Scan"); //底部的提示文字
        integrator.setCameraId(0); //前置(1)或者後置(0)攝像頭
        integrator.setBeepEnabled(false); //掃描成功的「嗶嗶」聲
        integrator.setBarcodeImageEnabled(false); //是否保留掃碼成功時候的截圖
        integrator.setCaptureActivity(ScanActivity.class);
        integrator.setRequestCode(ITEM_ID_QRSCAN_REQUEST_CODE);
        integrator.initiateScan();
    }
}
