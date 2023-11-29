package com.delta.android.WMS.Client;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.Activity.ShowMessageEvent;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.GenerateJsonNetString.MesClass;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Client.GridAdapter.WarehouseStorageReceivedAdapter;
import com.delta.android.WMS.Param.BIWarehouseStoragePortalParam;
import com.delta.android.WMS.Param.BWarehouseStorageWithPackingInfoParam;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

public class WarehouseStorageReceivedActivity extends BaseFlowActivity {

    private TextView tvWvId;
    private TextView tvWvrItemId;
    private TextView tvWvrQty;
    private ListView lvWvrDetGroup;
    private Button btnDelete;

    private String sheetPolicyId;
    private String wvId;
    private HashMap drSelectedDet;
    private DataTable dtMst;
    private DataTable dtSelectedWvrGroup;
    private DataTable dtSelectedWvrWithPackingInfo;
    private DataTable dtWvrDetWithPackingInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warehouse_storage_received);

        initWidget();

        sheetPolicyId = getIntent().getStringExtra("sheetPolicyId");
        wvId = getIntent().getStringExtra("wvId");
        drSelectedDet = (HashMap) getIntent().getSerializableExtra("dtDetRow");
        dtMst = (DataTable) getIntent().getSerializableExtra("dtMst");
        dtSelectedWvrGroup = (DataTable) getIntent().getSerializableExtra("selectedWvrGroup");
        dtSelectedWvrWithPackingInfo = (DataTable) getIntent().getSerializableExtra("selectedWvrWithPackingInfo");
        dtWvrDetWithPackingInfo = (DataTable) getIntent().getSerializableExtra("dtWvrDet");

        Double procQty = 0.0;
        for(DataRow dr : dtSelectedWvrWithPackingInfo.Rows) {
            procQty += Double.parseDouble(dr.get("QTY").toString());
        }

        tvWvId.setText(wvId);
        tvWvrItemId.setText(drSelectedDet.get("ITEM_ID").toString());
        tvWvrQty.setText(procQty + " / " + drSelectedDet.get("QTY").toString());

        LayoutInflater lvInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        WarehouseStorageReceivedAdapter adapter = new WarehouseStorageReceivedAdapter(dtSelectedWvrGroup, lvInflater);
        lvWvrDetGroup.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        lvWvrDetGroup.setOnItemClickListener(onClickListView);
        btnDelete.setOnClickListener(onClickDelete);
    }

    private void initWidget() {
        tvWvId = findViewById(R.id.tvWvId);
        tvWvrItemId = findViewById(R.id.tvWvrItemId);
        tvWvrQty = findViewById(R.id.tvWvrQty);
        lvWvrDetGroup = findViewById(R.id.lvWvrDetGroup);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private View.OnClickListener onClickDelete = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Map<String, String> mapSkuNumSeq = new HashMap<>();

            for(DataRow dr : dtSelectedWvrGroup.Rows) {
                if (dr.getValue("SELECTED").toString().equals("true"))
                    mapSkuNumSeq.put(dr.getValue("SKU_NUM").toString(), dr.getValue("SEQ").toString());
            }

            // 刪除包裝內資料
            for (Iterator<DataRow> iterator = dtWvrDetWithPackingInfo.Rows.iterator(); iterator.hasNext();) {
                DataRow drDelete = iterator.next();
                for (String skuNum : mapSkuNumSeq.keySet()) {
                    if (drDelete.getValue("SKU_NUM").toString().equals(skuNum) && drDelete.getValue("SEQ").toString().equals(mapSkuNumSeq.get(skuNum))) {
                        iterator.remove();
                    }
                }
            }

            BModuleObject biObj1 = new BModuleObject();
            biObj1.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BWarehouseStorageWithPackingInfo");
            biObj1.setModuleID("BWarehouseStorageWithPackingInfo");
            biObj1.setRequestID("BWarehouseStorageWithPackingInfo");
            biObj1.params = new Vector<>();

            ParameterInfo param1 = new ParameterInfo();
            param1.setParameterID(BWarehouseStorageWithPackingInfoParam.SheetTypePolicyId);
            param1.setParameterValue(sheetPolicyId);
            biObj1.params.add(param1);

            ParameterInfo param2 = new ParameterInfo();
            param2.setParameterID(BWarehouseStorageWithPackingInfoParam.TrxType);
            param2.setParameterValue("WarehouseStorage");
            biObj1.params.add(param2);

            ParameterInfo param3 = new ParameterInfo();
            param3.setParameterID(BWarehouseStorageWithPackingInfoParam.SheetTypePolicyId);
            param3.setParameterValue(sheetPolicyId);
            biObj1.params.add(param3);

            BWarehouseStorageWithPackingInfoParam.WarehouseStorageWithPackingInfoMasterObj wvvObj = new BWarehouseStorageWithPackingInfoParam().new WarehouseStorageWithPackingInfoMasterObj().getWsSheet(dtMst.Rows.get(0), dtWvrDetWithPackingInfo);

            VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.INV.Parameter.WarehouseStorageWithPackingInfoMasterObj", "bmWMS.INV.Param");
            MesClass mesClassEnum = new MesClass(vListEnum);
            String strWvMstObj = mesClassEnum.generateFinalCode(wvvObj);

            ParameterInfo param4 = new ParameterInfo();
            param4.setParameterID(BWarehouseStorageWithPackingInfoParam.WsObj);
            param4.setNetParameterValue(strWvMstObj);
            biObj1.params.add(param4);

            ParameterInfo param5 = new ParameterInfo();
            param5.setParameterID(BWarehouseStorageWithPackingInfoParam.TrxMode);
            param5.setParameterValue("Modified");
            biObj1.params.add(param5);

            ParameterInfo paramExecuteChkStock = new ParameterInfo();
            paramExecuteChkStock.setParameterID(BIWarehouseStoragePortalParam.ExecuteCheckStock);
            paramExecuteChkStock.setNetParameterValue2("false");
            biObj1.params.add(paramExecuteChkStock);

            CallBModule(biObj1, new WebAPIClientEvent() {
                @Override
                public void onPostBack(BModuleReturn bModuleReturn) {
                    if (CheckBModuleReturnInfo(bModuleReturn)) {

                        //WAPG027046    入庫暫存資料刪除成功
                        ShowMessage(R.string.WAPG027046, new ShowMessageEvent() {
                            @Override
                            public void onDismiss() {

                                // 關閉頁面，返回 WarehouseStorageDetailNewActivity
                                Bundle bundle = new Bundle();
                                setActivityResult(bundle);

                            }
                        });

                    }
                }
            });
        }
    };


    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {

            CheckBox cbSelected = view.findViewById(R.id.cbSelected);
            cbSelected.setChecked(!cbSelected.isChecked());

            String checked = cbSelected.isChecked() ? "true" : "false";

            dtSelectedWvrGroup.Rows.get(pos).setValue("SELECTED", checked);
        }
    };
}
