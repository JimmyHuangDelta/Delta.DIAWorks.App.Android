package com.delta.android.WMS.Client;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionaryList;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Client.Fragment.WarehouseStorageDataFragment;
import com.delta.android.WMS.Client.Fragment.WarehouseStorageListFragment;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.ParamObj.Condition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class WarehouseStorageDetailNewActivity extends BaseFlowActivity {

    private WarehouseStorageListFragment warehouseStorageListFragment = new WarehouseStorageListFragment(); // 入庫明細頁籤/Inbound Details tab
    private WarehouseStorageDataFragment warehouseStorageDataFragment = new WarehouseStorageDataFragment(); // (欲)批次入庫資料頁籤/(Desired) batch warehousing data tab
    private int currentPage = 0; // 紀錄目前所在頁籤的位置/Record the position of the current tab
    private TabLayout tabLayout;

    String sheetTypeId; // 前一頁傳來/From first page
    String sheetPolicyId; // 前一頁傳來/From first page
    DataTable dtMst; // 前一頁傳來/From first page
    DataTable dtDet; // 前一頁傳來/From first page
    DataTable dtWvrDet; // 前一頁傳來/From first page
    DataTable dtWvrDetWithPackingInfo; // 前一頁傳來/From first page
    DataTable dtWvDet; // 第一個頁籤Call BIModule取得/From the first tab Call BIModule
    DataTable dtWvrDetGroup; // 第一個頁籤Call BIModule取得/From the first tab Call BIModule

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_warehouse_storage_detail_new);

        sheetTypeId = getIntent().getStringExtra("sheetTypeId");
        sheetPolicyId = getIntent().getStringExtra("sheetPolicyId");
        dtMst = (DataTable) getIntent().getSerializableExtra("dtMstResult");
        dtDet = (DataTable) getIntent().getSerializableExtra("dtDetResult");
        dtWvrDet = (DataTable) getIntent().getSerializableExtra("dtWvrDet");
        dtWvrDetGroup = (DataTable) getIntent().getSerializableExtra("dtWvrDetGroup");
        dtWvrDetWithPackingInfo = (DataTable) getIntent().getSerializableExtra("dtWvrDetWithPackingInfo");

        TextView tvWvId = findViewById(R.id.tvWvId);
        tvWvId.setText(dtMst.Rows.get(0).getValue("MTL_SHEET_ID").toString());

        // 將資料放入Bundle準備傳到下一頁/Put the data into the Bundle and prepare to pass it to the next page
        Bundle bundle = new Bundle();
        bundle.putString("sheetTypeId", sheetTypeId);
        bundle.putString("sheetPolicyId", sheetPolicyId);
        bundle.putSerializable("dtMst", dtMst);
        bundle.putSerializable("dtDet", dtDet);
        bundle.putSerializable("dtWvrDet", dtWvrDet);
        bundle.putSerializable("dtWvrDetGroup", dtWvrDetGroup);
        bundle.putSerializable("dtWvrDetWithPackingInfo", dtWvrDetWithPackingInfo);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragmentWarehouseStorage, warehouseStorageListFragment, "WarehouseStorageList");
        fragmentTransaction.add(R.id.fragmentWarehouseStorage, warehouseStorageDataFragment, "WarehouseStorageData");
        warehouseStorageListFragment.setArguments(bundle);
        warehouseStorageDataFragment.setArguments(bundle);
        fragmentTransaction.hide(warehouseStorageDataFragment);
        fragmentTransaction.commit();

        tabLayout = findViewById(R.id.tabLayout);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                fragmentChange(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1)
            GetWvDet();
    }

    private void fragmentChange(int position){

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch (currentPage){

            case 0:
                fragmentTransaction.hide(warehouseStorageListFragment);
                break;

            case 1:
                GetWvDet(); // 當切回入庫頁籤時，重新取得已入庫資訊/When switching back to the storage tab, retrieve the storage information
                fragmentTransaction.hide(warehouseStorageDataFragment);
                break;
        }

        switch (position){

            case 0:
                fragmentTransaction.show(warehouseStorageListFragment);
                break;

            case 1:
                fragmentTransaction.show(warehouseStorageDataFragment);
                break;
        }

        fragmentTransaction.commit();

        currentPage = position;
    }

    public void GetWvDet() {

        List<BModuleObject> bmObjs = new ArrayList<BModuleObject>();

        // region Get WV_DET
        BModuleObject biObj1 = new BModuleObject();
        biObj1.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        biObj1.setModuleID("BIFetchWarehouseVoucherMstAndDet");
        biObj1.setRequestID("BIFetchWarehouseVoucherMstAndDet");
        biObj1.params = new Vector<>();

        HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
        List<Condition> lstCondition1 = new ArrayList<Condition>();
        List<Condition> lstCondition2 = new ArrayList<Condition>();

        // SHEET_TYPE_ID
        Condition conShtTypeId = new Condition();
        conShtTypeId.setAliasTable("ST");
        conShtTypeId.setColumnName("SHEET_TYPE_ID");
        conShtTypeId.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
        conShtTypeId.setValue(dtMst.Rows.get(0).getValue("SHEET_TYPE_ID").toString());
        lstCondition1.add(conShtTypeId);
        mapCondition.put(conShtTypeId.getColumnName(),lstCondition1);

        // WV_ID
        Condition conWvId = new Condition();
        conWvId.setAliasTable("M");
        conWvId.setColumnName("WV_ID");
        conWvId.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
        conWvId.setValue(dtMst.Rows.get(0).getValue("MTL_SHEET_ID").toString());
        lstCondition2.add(conWvId);
        mapCondition.put(conWvId.getColumnName(),lstCondition2);

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

        bmObjs.add(biObj1);
        // endregion

        // region Get WVR_DET
        HashMap<String, List<?>> mapCon1 = new HashMap<>();

        BModuleObject bmWvrObj = new BModuleObject();
        bmWvrObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmWvrObj.setModuleID("BIFetchWarehouseVoucherReceiveDet");
        bmWvrObj.setRequestID("BIFetchWarehouseVoucherReceiveDet");
        bmWvrObj.params = new Vector<>();

        mapCon1.put(conWvId.getColumnName(),lstCondition2);

        VirtualClass vkey3 = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal3 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
        MesSerializableDictionaryList msdl3 = new MesSerializableDictionaryList(vkey3, vVal3);
        String strCond3 = msdl3.generateFinalCode(mapCon1);

        ParameterInfo paramWvIdObj = new ParameterInfo();
        paramWvIdObj.setParameterID(BIWMSFetchInfoParam.Condition);
        paramWvIdObj.setNetParameterValue(strCond3); // 要用set"Net"ParameterValue
        bmWvrObj.params.add(paramWvIdObj);
        bmObjs.add(bmWvrObj);
        // endregion

        // region Get WVR_DET_GROUP_BY_SKU_LEVEL
        BModuleObject bmWvrGroupBySkuLevelObj = new BModuleObject();
        bmWvrGroupBySkuLevelObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmWvrGroupBySkuLevelObj.setModuleID("BIFetchWarehouseVoucherReceiveDetGroupBySkuLevel");
        bmWvrGroupBySkuLevelObj.setRequestID("BIFetchWarehouseVoucherReceiveDetGroupBySkuLevel");
        bmWvrGroupBySkuLevelObj.params = new Vector<>();

        ParameterInfo paramWvIdGroupBySkuLevelObj = new ParameterInfo();
        paramWvIdGroupBySkuLevelObj.setParameterID(BIWMSFetchInfoParam.Condition);
        paramWvIdGroupBySkuLevelObj.setNetParameterValue(strCond3); // 要用set"Net"ParameterValue
        bmWvrGroupBySkuLevelObj.params.add(paramWvIdGroupBySkuLevelObj);
        bmObjs.add(bmWvrGroupBySkuLevelObj);
        // endregion

        // region WVR_DET_WITH_PACKING_INFO
        BModuleObject bmWvrWithPackingInfo = new BModuleObject();
        bmWvrWithPackingInfo.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmWvrWithPackingInfo.setModuleID("BIFetchWarehouseVoucherReceiveDetWithPackingInfo");
        bmWvrWithPackingInfo.setRequestID("BIFetchWarehouseVoucherReceiveDetWithPackingInfo");
        bmWvrWithPackingInfo.params = new Vector<>();

        ParameterInfo paramWvIdWithPackingInfoObj = new ParameterInfo();
        paramWvIdWithPackingInfoObj.setParameterID(BIWMSFetchInfoParam.Condition);
        paramWvIdWithPackingInfoObj.setNetParameterValue(strCond3); // 要用set"Net"ParameterValue
        bmWvrWithPackingInfo.params.add(paramWvIdWithPackingInfoObj);
        bmObjs.add(bmWvrWithPackingInfo);
        // endregion

        CallBIModule(bmObjs, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {

                if (CheckBModuleReturnInfo(bModuleReturn)) {

                    dtWvDet = bModuleReturn.getReturnJsonTables().get("BIFetchWarehouseVoucherMstAndDet").get("WvDet");
                    DataTable dtWvrDetGroup = bModuleReturn.getReturnJsonTables().get("BIFetchWarehouseVoucherReceiveDetGroupBySkuLevel").get("WvrDetGroupBySkuLevel");
                    DataTable dtWvrDetWithPackingInfo = bModuleReturn.getReturnJsonTables().get("BIFetchWarehouseVoucherReceiveDetWithPackingInfo").get("WvrDetWithPackingInfo");

                    warehouseStorageListFragment.setWvLotData(dtWvDet);
                    warehouseStorageListFragment.setWvrData(dtWvrDetWithPackingInfo, dtWvrDetGroup);
                    warehouseStorageDataFragment.setWvrData(dtWvrDetWithPackingInfo, dtWvrDetGroup);
                }
            }
        });
    }
}
