package com.delta.android.WMS.Client;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.Activity.ScanActivity;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.GenerateJsonNetString.MList;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Client.GridAdapter.DeliveryNoteDetGridAdapter;
import com.delta.android.WMS.Client.GridAdapter.SheetDetGridAdapter;
import com.delta.android.WMS.Param.BDeliveryNotePickingParam;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class DeliveryNotePickingDetailActivity extends BaseFlowActivity {

    // private variable
    private static int requestCode = 1; //需要接收回傳資訊時使用
    private final int ITEM_ID_QRSCAN_REQUEST_CODE = 11;
    private ViewHolder holder = null;
    private ArrayList<String> DNIDs = new ArrayList<String>();
    private DataTable sheetDetTable;
    private DataTable sheetMstTable;
    private DataTable showTable; // tab上顯示的detail資訊
    private DataTable pickDetTable;// 全部的揀貨資訊
    private DataTable alreadyPickedData;// 某一筆detail的揀貨資訊

    static class ViewHolder {
        // 宣告控制項物件
        ListView SheetDetData1;
        ListView SheetDetData2;
        EditText ItemId;
        ImageButton IbtnItemIdQRScan;
        TabHost Tabhost;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_delivery_note_picking_detail);

        this.initialControl();
        this.initialData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);

        IntentResult result = IntentIntegrator.parseActivityResult(resultCode, dataIntent);
        if (result != null) {
            if (result.getContents() == null) {
                if (requestCode == 1) {
                    super.onActivityResult(requestCode, resultCode, dataIntent);
                    // 回傳資料
                    // 取得已揀資訊放tab
                    String sheetIds = "";
                    for (String id : DNIDs) // 將選取的MST DNIDs丟到BIModule找其已揀資訊
                    {
                        sheetIds += "," + id; // 用','隔開，BIModule在用String.Split(',')轉成 String[]
                    }
                    sheetIds = sheetIds.substring(1);// 去掉第一個','
                    holder.ItemId.setText("");
                    this.getPickDet(sheetIds);
                } else if (requestCode == ITEM_ID_QRSCAN_REQUEST_CODE) {
                    Toast.makeText(this, getResources().getText(R.string.QRCODE_SCAN_CANCEL).toString(), Toast.LENGTH_SHORT).show();
                }
            } else {
                switch (requestCode) {
                    case ITEM_ID_QRSCAN_REQUEST_CODE:
                        holder.ItemId.setText(result.getContents().trim().toUpperCase());
                        break;
                    default:
                        break;
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, dataIntent);
        }
    }

    private void initialData() {
        DNIDs = getIntent().getStringArrayListExtra("DNIDs");
        sheetDetTable = (DataTable) getIntent().getSerializableExtra("DNDet");
        sheetMstTable = (DataTable) getIntent().getSerializableExtra("DNMst");
        // 取得已揀資訊放tab
        String sheetIds = "";
        for (String id : DNIDs) // 將選取的MST DNIDs丟到BIModule找其已揀資訊
        {
            sheetIds += "," + id; // 用','隔開，BIModule在用String.Split(',')轉成 String[]
        }
        sheetIds = sheetIds.substring(1);// 去掉第一個','
        this.getPickDet(sheetIds);

    }

    private void initialControl() {
        if (holder != null) return;

        holder = new ViewHolder();
        holder.ItemId = findViewById(R.id.etDnDetItemId);// 取得控制項物件
        holder.IbtnItemIdQRScan = findViewById(R.id.ibtnItemIdQRScan);
        holder.SheetDetData1 = findViewById(R.id.lvDnDetData1);
        holder.SheetDetData2 = findViewById(R.id.lvDnDetData2);
        holder.Tabhost = findViewById(R.id.tabHost);
        holder.Tabhost.setup();

        TabHost.TabSpec spec1 = holder.Tabhost.newTabSpec("TabUnPicked");
        View tab1 = LayoutInflater.from(this).inflate(R.layout.activity_wms_good_pick_tab_widget, null);
        TextView tvTab1 = tab1.findViewById(R.id.tvTabText);// 取得activity_wms_good_pick_tab_widget內的物件
        tvTab1.setText(R.string.REGISTER_UNPICKED); // 設tab1的標題
        spec1.setIndicator(tab1); // 設定Tab的圖示以及顯示的文字，用View的方式
        spec1.setContent(R.id.llPresentData1);// 把想要加入的Intent加入到這個Tab(det linerlayout)
        holder.Tabhost.addTab(spec1);

        TabHost.TabSpec spec2 = holder.Tabhost.newTabSpec("TabPicked");
        View tab2 = LayoutInflater.from(this).inflate(R.layout.activity_wms_good_pick_tab_widget, null);
        TextView tvTab2 = tab2.findViewById(R.id.tvTabText);
        tvTab2.setText(R.string.REGISTER_PICKED);
        spec2.setIndicator(tab2);
        spec2.setContent(R.id.llPresentData2);
        holder.Tabhost.addTab(spec2);
        holder.Tabhost.setOnTabChangedListener(TabChange); // 切換tab時觸發事件，load畫面

        holder.ItemId.setOnEditorActionListener(filterSheetDet);
        holder.SheetDetData1.setOnItemClickListener(getAlreadyPickedData);
        holder.IbtnItemIdQRScan.setOnClickListener(ItemQRScan);
    }


    //region 事件
    private TextView.OnEditorActionListener filterSheetDet = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (sheetDetTable == null || sheetDetTable.Rows.size() == 0) return false;
            DataTable dtFilterPicked = null;
            if (holder.Tabhost.getCurrentTabTag() == "TabUnPicked")
                dtFilterPicked = filterWhetherDetsArePicked(false);
            else
                dtFilterPicked = filterWhetherDetsArePicked(true);

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
            if (holder.Tabhost.getCurrentTabTag() == "TabUnPicked") {
                DeliveryNoteDetGridAdapter adapter = new DeliveryNoteDetGridAdapter(filterTable, inflater, pickDetTable, false, DeliveryNotePickingDetailActivity.this);
                holder.SheetDetData1.setAdapter(adapter);
            } else {
                DeliveryNoteDetGridAdapter adapter = new DeliveryNoteDetGridAdapter(filterTable, inflater, pickDetTable, true,DeliveryNotePickingDetailActivity.this);
                holder.SheetDetData2.setAdapter(adapter);
            }

            return false;
        }
    };

    private AdapterView.OnItemClickListener getAlreadyPickedData = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final DataRow chooseRow = (DataRow) parent.getAdapter().getItem(position);
            BModuleObject biObj = new BModuleObject();
            biObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIDeliveryNotePicking");
            biObj.setModuleID("BIGetDeliveryNotePicked");
            biObj.setRequestID("GetPickedItem");
            biObj.params = new Vector<>();

            //BModule 原本吃String[]參數，這邊virtual class沒有-> 新增一個BDeliveryNotePickingParam.DNIDandroid
            List<String> lstCondition = new ArrayList<String>();
            lstCondition.add(chooseRow.getValue("DN_ID").toString());
            VirtualClass vList = VirtualClass.create(VirtualClass.VirtualClassType.String);
            MList mList = new MList(vList);
            String strLsManuSN = mList.generateFinalCode(lstCondition);

            //這樣應該傳一個參數就好?->因為BModule會檢查
            ParameterInfo param1 = new ParameterInfo();
            param1.setParameterID(BDeliveryNotePickingParam.DNIDs);
            param1.setNetParameterValue(strLsManuSN);

            ParameterInfo param2 = new ParameterInfo();
            param2.setParameterID(BDeliveryNotePickingParam.DNIDandroid);
            param2.setParameterValue(chooseRow.getValue("DN_ID").toString());

            biObj.params.add(param1);
            biObj.params.add(param2);
            final DataTable dt = new DataTable();
            dt.Rows.add(chooseRow);

            final DataTable dtM = new DataTable();
            for (DataRow dr : sheetMstTable.Rows)
            {
                if (dr.getValue("DN_ID").toString().equals(chooseRow.getValue("DN_ID").toString()))
                {
                    dtM.Rows.add(dr);
                    break;
                }
            }

            CallBIModule(biObj, new WebAPIClientEvent() {
                @Override
                public void onPostBack(BModuleReturn bModuleReturn) {
                    if (CheckBModuleReturnInfo(bModuleReturn)) {
                        alreadyPickedData = bModuleReturn.getReturnJsonTables().get("GetPickedItem").get("*");
                        if (alreadyPickedData == null || alreadyPickedData.Rows.size() == 0) {
                            alreadyPickedData = new DataTable();
                        }
                        DataTable dtPicked = new DataTable();
                        for (DataRow dr : alreadyPickedData.Rows) {
                            if (dr.getValue("ITEM_ID").toString().equals(chooseRow.getValue("ITEM_ID").toString())
                                    && dr.getValue("SEQ").toString().equals(chooseRow.getValue("SEQ").toString())) {
                                dtPicked.Rows.add(dr);
                            }
                        }
                        Bundle chooseSheetDet = new Bundle();
                        chooseSheetDet.putSerializable("DetTable", dt);
                        chooseSheetDet.putSerializable("DetPicked", dtPicked);
                        chooseSheetDet.putSerializable("MstTable", dtM);
                        //gotoNextActivity(DeliveryNotePickingExecutedActivity.class, chooseSheetDet);

                        //如果希望使用回傳的資料，則需要用此方法
                        Intent intent = new Intent(DeliveryNotePickingDetailActivity.this, DeliveryNotePickingExecutedActivity.class);
                        intent.putExtras(chooseSheetDet);
                        startActivityForResult(intent, requestCode);
                    }
                }
            });
        }
    };

    private View.OnClickListener ItemQRScan = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            IntentIntegrator integrator = new IntentIntegrator(DeliveryNotePickingDetailActivity.this);
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
    };

    private TabHost.OnTabChangeListener TabChange = new TabHost.OnTabChangeListener() {
        @Override
        public void onTabChanged(String tabId) {
            DataTable dtFilterPicked;
            holder.ItemId.setText("");
            if (tabId.equals("TabUnPicked")) {
                holder.SheetDetData1.setEnabled(true);
                holder.SheetDetData2.setEnabled(false);
                dtFilterPicked = filterWhetherDetsArePicked(false);//取得未揀的資訊
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                DeliveryNoteDetGridAdapter adapter = new DeliveryNoteDetGridAdapter(dtFilterPicked, inflater, pickDetTable, false,DeliveryNotePickingDetailActivity.this);
                holder.SheetDetData1.setAdapter(adapter);
            } else {
                holder.SheetDetData2.setEnabled(true);
                holder.SheetDetData1.setEnabled(false);
                dtFilterPicked = filterWhetherDetsArePicked(true);//取得已揀的資訊
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                DeliveryNoteDetGridAdapter adapter = new DeliveryNoteDetGridAdapter(dtFilterPicked, inflater, pickDetTable, true,DeliveryNotePickingDetailActivity.this
                );
                holder.SheetDetData2.setAdapter(adapter);
            }
        }
    };
    //endregion

    // region private Function

    // 取得揀貨資訊
    private void getPickDet(String sheetId) {
        BModuleObject biObj = new BModuleObject();
        biObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIDeliveryNotePicking");
        biObj.setModuleID("BIGetDeliveryNotePicked");
        biObj.setRequestID("GetPickedItem");
        biObj.params = new Vector<>();

        //BModule 原本吃String[]參數，這邊virtual class沒有-> 新增一個BDeliveryNotePickingParam.DNIDandroid
        List<String> lstCondition = new ArrayList<String>();
        lstCondition.add("");
        VirtualClass vList = VirtualClass.create(VirtualClass.VirtualClassType.String);
        MList mList = new MList(vList);
        String strLsManuSN = mList.generateFinalCode(lstCondition);

        //這樣應該傳一個參數就好?->因為BModule會檢查原本的那個參數
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BDeliveryNotePickingParam.DNIDs);
        param1.setNetParameterValue(strLsManuSN);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BDeliveryNotePickingParam.DNIDandroid);
        param2.setParameterValue(sheetId); // sheetId在外面處理

        biObj.params.add(param1);
        biObj.params.add(param2);

        CallBIModule(biObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    DataTable dt = bModuleReturn.getReturnJsonTables().get("GetPickedItem").get("*");

                    pickDetTable = new DataTable();

                    //將非預約揀貨的資料篩選出來
                    if (dt != null && dt.Rows.size() >= 0) {
                        for (DataRow dr : dt.Rows) {
                            if (dr.getValue("IS_PICKED").toString().equals(("Y"))) {
                                pickDetTable.Rows.add(dr);
                            }
                        }
                    }
                }
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                holder.SheetDetData1.setAdapter(new DeliveryNoteDetGridAdapter(filterWhetherDetsArePicked(false), inflater, pickDetTable, false,DeliveryNotePickingDetailActivity.this));
                //holder.Tabhost.setCurrentTab(0);// 設Tab為未揀料頁面
            }
        });
    }

    private DataTable filterWhetherDetsArePicked(boolean showPickedData) {
        showTable = null;
        showTable = new DataTable();
        HashMap<String, Double> dicShowData = new HashMap<>();
        //region 判斷是否有已揀的
        if (pickDetTable != null && pickDetTable.Rows.size() != 0) {
            for (DataRow drDet : sheetDetTable.Rows) {
                double pickQty = 0.0;
                //將已揀料資訊的數量加總
                for (DataRow drPick : pickDetTable.Rows) { //SEQ還要再加DNID? 多筆單據
                    if (drDet.getValue("DN_ID").equals(drPick.getValue("DN_ID"))
                            && drDet.getValue("SEQ").equals(drPick.getValue("SEQ"))) {
                        pickQty += Double.parseDouble(drPick.getValue("QTY").toString());
                    }
                    dicShowData.put(drDet.getValue("DN_ID").toString() + drDet.getValue("SEQ").toString(), pickQty);
                }
            }
        } else {
            if (showPickedData)
                return showTable;
            else
                return sheetDetTable;
        }
        //endregion

        // 有已揀且要顯示已揀資訊
        if (showPickedData) {
            for (DataRow dr : sheetDetTable.Rows) {
                String DN_ID = dr.getValue("DN_ID").toString();
                String seqName = dr.getValue("SEQ").toString();
                double trxQty = Double.parseDouble(dr.getValue("QTY").toString());
                if (dicShowData.size() == 0) return showTable;
                if (Double.compare(trxQty, dicShowData.get(DN_ID + seqName)) <= 0) { // trxQty <= dicShowData.get(seqName)
                    showTable.Rows.add(dr);
                }
            }
        } else { // 有已揀但要顯示未揀資訊
            for (DataRow dr : sheetDetTable.Rows) {
                String DN_ID = dr.getValue("DN_ID").toString();
                String seqName = dr.getValue("SEQ").toString();
                double trxQty = Double.parseDouble(dr.getValue("QTY").toString());
                if (dicShowData.size() == 0) return showTable;
                if (Double.compare(trxQty, dicShowData.get(DN_ID + seqName)) > 0) { // trxQty > dicShowData.get(seqName)
                    showTable.Rows.add(dr);
                }
            }
        }
        return showTable;
    }

    // endregion


}
