package com.delta.android.WMS.Client;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
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
import com.delta.android.WMS.Client.Fragment.DeliveryNoteShipFragment;
import com.delta.android.WMS.Client.Fragment.DeliveryNoteShipInfoFragment;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.ParamObj.Condition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class DeliveryNoteShipWorkNewActivity extends BaseFlowActivity {

    private TabLayout tabLayout;
    private DeliveryNoteShipInfoFragment deliveryNoteShipInfoFragment = new DeliveryNoteShipInfoFragment();; // 第一個頁籤，Position = 0
    private DeliveryNoteShipFragment deliveryNoteShipFragment = new DeliveryNoteShipFragment(); // 第二個頁籤，Position = 1
    private int currentPage = 0; // 記錄目前所在頁籤的位置

    private Spinner cmbPickSheetId;
    private ImageButton ibtnSearch;
    private TextView tvDnId;
    private TextView tvShipDate;
    private TextView tvDeliveryAddress;
    private List<? extends Map<String, Object>> lstPickSheetId;

    private String pickSheetId = null, dnId = null, sheetTypeId = null;

    public DataTable dtMst;
    public DataTable dtDet;

    public DataTable dtPickMst;
    public DataTable dtPickDet;

    public DataTable dtOrderMst;
    public DataTable dtOrderDet;

    public DataTable dtSheetConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_note_ship_work_new);

        cmbPickSheetId = findViewById(R.id.cmbSheetId);
        ibtnSearch = findViewById(R.id.ibtnSearch);
        tvDnId = findViewById(R.id.tvDnId);
        tvShipDate = findViewById(R.id.tvShipDate);
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);
        cmbPickSheetId.setOnItemSelectedListener(cmbPickSheetIdOnClick);
        ibtnSearch.setOnClickListener(ibtnSearchOnClick);

        getPickSheetId();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragmentDeliveryPick, deliveryNoteShipInfoFragment, "DeliveryNoteShipInfo");
        fragmentTransaction.add(R.id.fragmentDeliveryPick, deliveryNoteShipFragment, "DeliveryNoteShip");
        fragmentTransaction.hide(deliveryNoteShipFragment);
        fragmentTransaction.commit();

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                fragmentChange(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void getPickSheetId() {

        // Call BIModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIFetchProcessSheet");
        bmObj.setModuleID("FetchCanShipPickSheet");
        bmObj.setRequestID("FetchCanShipPickSheet");
        bmObj.params = new Vector<ParameterInfo>();

        CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {

                    DataTable dtPickSheetId = bModuleReturn.getReturnJsonTables().get("FetchCanShipPickSheet").get("DATA");

                    DataRow drDefaultItem = dtPickSheetId.newRow();
                    // 下拉選單預設選項依語系調整
                    String strSelectSheetId = getResString(getResources().getString(R.string.SELECT_SHEET_ID));
                    drDefaultItem.setValue("SHEET_ID", strSelectSheetId); // 請選擇單據代碼

                    // region -- 揀料單據代碼 --
                    if (dtPickSheetId != null && dtPickSheetId.Rows.size() > 0)
                        dtPickSheetId.Rows.add(drDefaultItem);
                    lstPickSheetId = (List<? extends Map<String, Object>>) dtPickSheetId.toListHashMap();
                    SimpleAdapter adapterShtId = new SimpleArrayAdapter<>(DeliveryNoteShipWorkNewActivity.this, lstPickSheetId, android.R.layout.simple_spinner_item, new String[]{"SHEET_ID", "DN_ID", "SHEET_TYPE_ID"}, new int[]{android.R.id.text1, 0, 0});
                    adapterShtId.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    cmbPickSheetId.setAdapter(adapterShtId);
                    cmbPickSheetId.setSelection(lstPickSheetId.size()-1, true);
                    // endregion
                }
            }
        });
    }

    private Spinner.OnItemSelectedListener cmbPickSheetIdOnClick = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {

            pickSheetId = "";
            dnId = "";
            sheetTypeId = "";

            if (position != lstPickSheetId.size()-1) {
                Map<String, String> pickSheetIdMap = (Map<String, String>)parent.getItemAtPosition(position);
                pickSheetId = pickSheetIdMap.get("SHEET_ID");
                dnId = pickSheetIdMap.get("DN_ID");
                sheetTypeId = pickSheetIdMap.get("SHEET_TYPE_ID");
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            return;
        }
    };

    public class SimpleArrayAdapter<T> extends SimpleAdapter {

        public SimpleArrayAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        //複寫這個方法，使提示字改為灰色
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = super.getView(position, convertView, parent);

            if ( position == getCount() ){
                HashMap<String, String> rowData = (HashMap) getItem(getCount());
                ((TextView)v.findViewById(android.R.id.text1)).setText("");
                ((TextView)v.findViewById(android.R.id.text1)).setHint(rowData.get("SHEET_ID"));
            }

            return v;
        }

        //複寫這個方法，使返回的數據沒有最後一項
        @Override
        public int getCount() {
            // don't display last item. It is used as hint.
            int count = super.getCount();
            return count > 0 ? count - 1 : count;
        }
    }

    private View.OnClickListener ibtnSearchOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            getPickSheetDet();
        }
    };

    public void getPickSheetDet() {

        if (pickSheetId == null || pickSheetId.length() <= 0) {
            ShowMessage(R.string.WAPG002001); //WAPG002001 請選擇單據代碼
            return;
        }

        //取得出通單資訊
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchDeliveryNoteAndPickSheetMaintain");
        bmObj.setRequestID("BIFetchDeliveryNoteAndPickSheetMaintain");
        bmObj.params = new Vector<ParameterInfo>();

        List<Condition> lstCondition = new ArrayList<Condition>();
        Condition condition = new Condition();
        condition.setAliasTable("P");
        condition.setColumnName("SHEET_ID");
        condition.setValue(pickSheetId);
        condition.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
        lstCondition.add(condition);

        HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
        mapCondition.put(condition.getColumnName(),lstCondition);
        VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vkey, vVal);
        String strCond = msdl.generateFinalCode(mapCondition);

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Condition);
        param1.setNetParameterValue(strCond); // 要用set"Net"ParameterValue

        bmObj.params.add(param1);

        //取得揀貨資訊
        BModuleObject bmPickObj = new BModuleObject();
        bmPickObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmPickObj.setModuleID("BIFetchSheetPickMstAndDet");
        bmPickObj.setRequestID("BIFetchSheetPickMstAndDet");
        bmPickObj.params = new Vector<ParameterInfo>();

        lstCondition = new ArrayList<Condition>();
        condition = new Condition();
        condition.setAliasTable("MST");
        condition.setColumnName("SHEET_ID");
        condition.setValue(pickSheetId);
        condition.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
        lstCondition.add(condition);

        mapCondition = new HashMap<String, List<?>>();
        mapCondition.put(condition.getColumnName(),lstCondition);
        vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
        msdl = new MesSerializableDictionaryList(vkey, vVal);
        strCond = msdl.generateFinalCode(mapCondition);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIWMSFetchInfoParam.Condition);
        param2.setNetParameterValue(strCond); // 要用set"Net"ParameterValue

        bmPickObj.params.add(param2);

        //取得司機相關資訊
        BModuleObject bmDriverObj = new BModuleObject();
        bmDriverObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmDriverObj.setModuleID("BIFetchReleaseOrderMstAndDet");
        bmDriverObj.setRequestID("BIFetchReleaseOrderMstAndDet");
        bmDriverObj.params = new Vector<ParameterInfo>();

        lstCondition = new ArrayList<Condition>();
        condition = new Condition();
        condition.setAliasTable("MST");
        condition.setColumnName("DN_ID");
        condition.setValue(dnId);
        condition.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
        lstCondition.add(condition);

        mapCondition = new HashMap<String, List<?>>();
        mapCondition.put(condition.getColumnName(),lstCondition);
        vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
        msdl = new MesSerializableDictionaryList(vkey, vVal);
        strCond = msdl.generateFinalCode(mapCondition);

        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BIWMSFetchInfoParam.Condition);
        param3.setNetParameterValue(strCond); // 要用set"Net"ParameterValue

        bmDriverObj.params.add(param3);

        //取得Config
        BModuleObject bmConfigObj = new BModuleObject();
        bmConfigObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmConfigObj.setModuleID("BIFetchWmsSheetConfigBySheetId");
        bmConfigObj.setRequestID("BIFetchWmsSheetConfigBySheetId");
        bmConfigObj.params = new Vector<ParameterInfo>();

        ParameterInfo param4 = new ParameterInfo();
        param4.setParameterID(BIWMSFetchInfoParam.Filter);
        param4.setParameterValue(String.format(" AND M.SHEET_ID = '%s'", pickSheetId));

        bmConfigObj.params.add(param4);

        List<BModuleObject> lstBmObj = new ArrayList<BModuleObject>();
        lstBmObj.add(bmObj);
        lstBmObj.add(bmPickObj);
        lstBmObj.add(bmDriverObj);
        lstBmObj.add(bmConfigObj);

        CallBIModule(lstBmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {

                if (CheckBModuleReturnInfo(bModuleReturn)) {

                    dtMst = bModuleReturn.getReturnJsonTables().get("BIFetchDeliveryNoteAndPickSheetMaintain").get("PickMst");
                    dtDet = bModuleReturn.getReturnJsonTables().get("BIFetchDeliveryNoteAndPickSheetMaintain").get("PickDet");

                    dtPickMst = bModuleReturn.getReturnJsonTables().get("BIFetchSheetPickMstAndDet").get("ShtPickMst");
                    dtPickDet = bModuleReturn.getReturnJsonTables().get("BIFetchSheetPickMstAndDet").get("ShtPickDet");

                    for (Iterator<DataRow> iterator = dtPickDet.Rows.iterator(); iterator.hasNext();) {
                        DataRow dr = iterator.next();
                        if (dr.getValue("IS_PICKED").equals("N")) {
                            iterator.remove();
                        }
                    }

                    dtOrderMst = bModuleReturn.getReturnJsonTables().get("BIFetchReleaseOrderMstAndDet").get("OrderMst");
                    dtOrderDet = bModuleReturn.getReturnJsonTables().get("BIFetchReleaseOrderMstAndDet").get("OrderDet");

                    dtSheetConfig = bModuleReturn.getReturnJsonTables().get("BIFetchWmsSheetConfigBySheetId").get("SBRM_WMS_SHEET_CONFIG");

                    if (dtSheetConfig.Rows.size() == 0) {
                        Object[] objs = new Object[1];
                        objs[0] = sheetTypeId;
                        ShowMessage(R.string.WAPG002024, objs); //WAPG002025    單據類型[%s]未設定單據Config設定
                        setDataToFragment(false);
                        return;
                    }

                    if (dtMst.Rows.size() == 0) {
                        ShowMessage(R.string.WAPG002002); //WAPG002002    查無單據資料
                        setDataToFragment(false);
                        return;
                    }

                    if (!dtMst.Rows.get(0).getValue("SHEET_STATUS").equals("Confirmed")) {
                        ShowMessage(R.string.WAPG002003); //WAPG002003    單據未確認
                        setDataToFragment(false);
                        return;
                    }

                    if (dtPickMst.Rows.size() == 0) {
                        ShowMessage(R.string.WAPG002004);//WAPG002004    查無揀貨資料
                        setDataToFragment(false);
                        return;
                    }

                    if (dtMst.Rows.get(0).getValue("SHIP_QC_CONFIRM").equals("Y")) { // 需經QC作確認

                        // 判斷出通單所綁定的揀貨狀態是否為QCConfirmed
                        if (!dtPickMst.Rows.get(0).getValue("PICK_STATUS").equals("QCConfirmed") && !dtPickMst.Rows.get(0).getValue("PICK_STATUS").equals("Delivering")) {
                            ShowMessage(R.string.WAPG002005); //WAPG002005    出通單所綁定的揀貨狀態是不為QCConfirmed
                            setDataToFragment(false);
                            return;
                        }

                    } else { // 不需經QC作確認

                        // 判斷出通單綁定的揀貨狀態是否為Picked
                        if (!dtPickMst.Rows.get(0).getValue("PICK_STATUS").equals("Picked") && !dtPickMst.Rows.get(0).getValue("PICK_STATUS").equals("Delivering")) {
                            ShowMessage(R.string.WAPG002006); //WAPG002006    出通單綁定的揀貨狀態不為Picked
                            setDataToFragment(false);
                            return;
                        }
                    }

                    setDataToFragment(true);

                }
            }
        });
    }

    private void fragmentChange(int position){

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch (currentPage){

            case 0:
                fragmentTransaction.hide(deliveryNoteShipInfoFragment);
                break;

            case 1:
                fragmentTransaction.hide(deliveryNoteShipFragment);
                break;
        }

        switch (position){

            case 0:
                fragmentTransaction.show(deliveryNoteShipInfoFragment);
                break;

            case 1:
                fragmentTransaction.show(deliveryNoteShipFragment);
                break;
        }

        fragmentTransaction.commit();

        currentPage = position;
    }

    private void setDataToFragment(boolean haveData) {

        if (haveData == false) {

            tvDnId.setText("");
            tvShipDate.setText("");
            tvDeliveryAddress.setText("");

            DeliveryNoteShipInfoFragment shipInfoFragment = (DeliveryNoteShipInfoFragment) getSupportFragmentManager().findFragmentByTag("DeliveryNoteShipInfo");
            shipInfoFragment.getPickingInfo(null);

            DeliveryNoteShipFragment shipFragment = (DeliveryNoteShipFragment) getSupportFragmentManager().findFragmentByTag("DeliveryNoteShip");
            shipFragment.getPlateInfo(dtOrderMst);

            return;
        }

        tvDnId.setText(dtMst.Rows.get(0).getValue("DN_ID").toString());
        tvShipDate.setText(dtMst.Rows.get(0).getValue("SHIP_DATE").toString());
        tvDeliveryAddress.setText(dtMst.Rows.get(0).getValue("DELIVERY_ADDRESS").toString());

        DeliveryNoteShipInfoFragment shipInfoFragment = (DeliveryNoteShipInfoFragment) getSupportFragmentManager().findFragmentByTag("DeliveryNoteShipInfo");
        shipInfoFragment.getPickingInfo(dtPickDet);

        DeliveryNoteShipFragment shipFragment = (DeliveryNoteShipFragment) getSupportFragmentManager().findFragmentByTag("DeliveryNoteShip");
        //shipFragment.setData(dtMst, dtDet, dtOrderMst, dtOrderDet, dtPickMst, dtPickDet);
        shipFragment.getPlateInfo(dtOrderMst);

        return;
    }

    public void refreshData() {
        tabLayout.getTabAt(0).select();
        getPickSheetId();
        setDataToFragment(false);
    }
}

//Error Code
//WAPG002001    請輸入單據代碼
//WAPG002002    查無單據資料
//WAPG002003    單據未確認
//WAPG002004    查無揀貨資料
//WAPG002005    出通單所綁定的揀貨狀態是不為QCConfirmed
//WAPG002006    出通單綁定的揀貨狀態不為Picked
//WAPG002007    作業成功
//WAPG002008    請輸入電話
//WAPG002009    請輸入司機
//WAPG002010    車牌輸入錯誤
//WAPG002011    該批號重複
//WAPG002012    查無批號相關資訊
//WAPG002013    查無司機相關資訊
//WAPG002014    批號[%s]無法正常轉換時間
//WAPG002015    批號[%s]已過期
//WAPG002016    查無出通單設定檔
//WAPG002017    批號[%s]儲位[%s]庫存數量不足
//WAPG002018    批號[%s]儲位[%s]不存在註冊資料中
//WAPG002019    無法取得倉庫物料資訊
//WAPG002020    單據[%s],項次[%s],出貨數量[%s]不可小於單據數量[%s]
//WAPG002021    單據[%s],項次[%s],出貨數量[%s]不可大於單據數量[%s]
//WAPG002022    單據[%s],項次[%s],出貨數量[%s]必須等於單據數量[%s]
//WAPG002023    請選擇儲位
//WAPG002024    單據[%s],項次[%s]沒有揀料

//WAPG002025    單據類型[%s]未設定單據Config設定
//WAPG002026    請新增或選擇車牌號碼
//WAPG002027    車牌[%s]的車門已關閉，無法修改
//WAPG002028    請輸入存貨代碼
//WAPG002029    存貨代碼[%s]已存在
//WAPG002030    存貨代碼[%s]不存在於揀貨清單
//WAPG002031    車牌號碼[%s]司機名稱[%s]已存在
//WAPG002032    查無準備出貨資訊