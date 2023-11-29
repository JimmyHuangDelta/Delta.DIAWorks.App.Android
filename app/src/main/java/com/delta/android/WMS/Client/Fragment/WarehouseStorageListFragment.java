package com.delta.android.WMS.Client.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.delta.android.Core.Activity.ShowMessageEvent;
import com.delta.android.Core.DataTable.DataColumn;
import com.delta.android.Core.DataTable.DataColumnCollection;
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
import com.delta.android.WMS.Client.GridAdapter.ReceiveSelectTempBinGridAdapter;
import com.delta.android.WMS.Client.GridAdapter.WarehouseStorageDetAdapter;
import com.delta.android.WMS.Client.WarehouseStorageActivity;
import com.delta.android.WMS.Client.WarehouseStorageDetailNewActivity;
import com.delta.android.WMS.Client.WarehouseStorageReceivedActivity;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.BIWarehouseStoragePortalParam;
import com.delta.android.WMS.Param.BWarehouseStorageWithPackingInfoParam;
import com.delta.android.WMS.Param.ParamObj.CheckCountObj;
import com.delta.android.WMS.Param.ParamObj.Condition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class WarehouseStorageListFragment extends Fragment {

    private DataTable dtMst, dtDet, dtWvrDetGroup, dtWvrDetWithPackingInfo;
    private ListView lvWvLotData;
    private Button btnConfirmAll;
    private String sheetPolicyId, sheetTypeId;
    private String wvId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_warehouse_storage_list, container, false);

        initWidget(view);

        // Get Info from WarehouseStorageDetailNewActivity
        sheetPolicyId = (String) getFragmentManager().findFragmentByTag("WarehouseStorageList").getArguments().getSerializable("sheetPolicyId");
        sheetTypeId = (String) getFragmentManager().findFragmentByTag("WarehouseStorageList").getArguments().getSerializable("sheetTypeId");
        dtMst = (DataTable)getFragmentManager().findFragmentByTag("WarehouseStorageList").getArguments().getSerializable("dtMst");
        dtDet = (DataTable)getFragmentManager().findFragmentByTag("WarehouseStorageList").getArguments().getSerializable("dtDet");
        dtWvrDetGroup = (DataTable)getFragmentManager().findFragmentByTag("WarehouseStorageList").getArguments().getSerializable("dtWvrDetGroup");
        dtWvrDetWithPackingInfo = (DataTable)getFragmentManager().findFragmentByTag("WarehouseStorageList").getArguments().getSerializable("dtWvrDetWithPackingInfo");

        // Get wvId
        if (((DataColumnCollection) dtMst.getColumns()).get("WV_ID") != null)
            wvId = dtMst.Rows.get(0).getValue("WV_ID").toString();
        if (((DataColumnCollection) dtMst.getColumns()).get("MTL_SHEET_ID") != null)
            wvId = dtMst.Rows.get(0).getValue("MTL_SHEET_ID").toString();

        // Put Det in ListView and Show Data
        showListView();

        // Set Listener on Confirm
        btnConfirmAll.setOnClickListener(onClickConfirm);

        return view;
    }

    /**
     *  Initialize Witget
     * @param view
     */
    private void initWidget(View view) {
        lvWvLotData = view.findViewById(R.id.lvWvLotData);
        btnConfirmAll = view.findViewById(R.id.btnConfirmAll);
        lvWvLotData.setOnItemClickListener(lvOnClick);
    }

    /**
     * 在 ListView 中顯示 WV_DET 資料
     * Show WV_DET Data
     */
    private void showListView() {

        LayoutInflater lvInflater = getActivity().getLayoutInflater();
        WarehouseStorageDetAdapter adapter = new WarehouseStorageDetAdapter(dtDet, dtWvrDetWithPackingInfo, true, lvInflater);
        lvWvLotData.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    /**
     * 從 WarehouseStorageDetailNewActivity 設置 WV_DET (當點擊入庫明細頁籤) 並顯示資料至 ListView
     *  Set  WV_DET from WarehouseStorageDetailNewActivity (When click warehouseStorageListFragment) and show data in ListView
     * @param dtWvDet
     */
    public void setWvLotData(DataTable dtWvDet) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        WarehouseStorageDetAdapter adapter = new WarehouseStorageDetAdapter(dtWvDet, true, inflater);
        lvWvLotData.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    /**
     * 從 WarehouseStorageDetailNewActivity 設置 WVR_DET (GroupBySkuLevel)、WVR_DET (包含SKU_LEVEL, SKU_NUM)
     * Sset WVR_DET(GroupBySkuLevel), WVR_DET(With SKU_LEVEL, SKU_NUM) from WarehouseStorageDetailNewActivity
     * @param dtWvr WVR_DET(With SKU_LEVEL, SKU_NUM)
     * @param dtWvrGroup WVR_DET(GroupBySkuLevel)
     */
    public void setWvrData(DataTable dtWvr, DataTable dtWvrGroup) {

        dtWvrDetGroup = dtWvrGroup;
        dtWvrDetWithPackingInfo = dtWvr;

    }

    /**
     * 執行入庫完成前的檢查: 實際數量狀態
     * Check before executing confirm: check actual qty status
     * @return true/false
     */
    private boolean checkBeforeConfirm() {

        for (DataRow drDet : dtDet.Rows) {

            String actualQtyStutas = drDet.getValue("ACTUAL_QTY_STATUS").toString();

            if (actualQtyStutas.equals("")) {

                Object[] args = new Object[1];
                args[0] = sheetPolicyId;

                //WAPG027013    單據類型「%s」未設定「單據Config設定」內的【實際數量的狀態】，請先設定!
                ((WarehouseStorageDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG027013, args);
                return false;
            }

            String seq = drDet.getValue("SEQ").toString();
            Double qty = Double.parseDouble(drDet.getValue("QTY").toString());
            Double procQty = 0.0;

            for (DataRow dr : dtWvrDetWithPackingInfo.Rows) {
                if (seq.equals(dr.get("SEQ").toString())) {
                    procQty += Double.parseDouble(dr.get("QTY").toString());
                }
            }

            Object[] args = new Object[3];
            args[0] = sheetTypeId;
            args[1] = actualQtyStutas;
            args[2] = seq;

            switch (actualQtyStutas) {

                case "More":
                    if (qty > procQty) {
                        //WAPG027035    單據類型「%s」在「單據類型Config設定」內的【實際數量的狀態】設定為「%s」，項次「%s」欲入庫數量不可小於單據數量!
                        ((WarehouseStorageDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG027035, args);
                        return false;
                    }
                    break;

                case "Equal":
                    if (!qty.equals(procQty)) {
                        //WAPG027036    單據類型「%s」在「單據類型Config設定」內的【實際數量的狀態】設定為「%s」，項次「%s」欲入庫數量應等於單據數量!
                        ((WarehouseStorageDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG027036, args);
                        return false;
                    }
                    break;

                case "Less":
                    if (procQty <= 0) {
                        //WAPG027037    單據類型「%s」在「單據類型Config設定」內的【實際數量的狀態】設定為「%s」，項次「%s」欲入庫數量不可等於0!
                        ((WarehouseStorageDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG027037, args);
                        return false;
                    }
                    break;
            }
        }

        return true;
    }

    /**
     * 入庫完成點擊事件
     * Event of Click
     */
    private View.OnClickListener onClickConfirm = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            if (!checkBeforeConfirm())
                return;

            // 取得單據類所有倉庫資訊/Obtain all storage information in sheet
            ArrayList<String> lstStorage = new ArrayList<String>();
            for (DataRow drDet : dtDet.Rows){
                String storageId = drDet.getValue("STORAGE_ID").toString();
                if(!lstStorage.contains(storageId)){
                    lstStorage.add(storageId);
                }
            }

            checkStorageInTempBin(lstStorage);
        }
    };

    /**
     * 取得倉庫內的暫存儲位及入料口
     * Get IT/IS Bin in Storage
     * @param lstStorage 單據內記錄的倉庫/Storage recorded in sheet
     */
    private void checkStorageInTempBin(final List<String> lstStorage) {
        final HashMap<String, ArrayList<String>> mapStorageBin = new HashMap<String, ArrayList<String>>(); // 存放 IT 或 IS 儲位 / Store IT or IS Bin

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchBin");
        bmObj.setRequestID("BIFetchBin");

        bmObj.params = new Vector<ParameterInfo>();
        //裝Condition的容器
        final HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
        List<Condition> lstCondition = new ArrayList<Condition>();
        for (int i = 0; i < lstStorage.size(); i++) {
            Condition cond = new Condition();
            cond.setAliasTable("S");
            cond.setColumnName("STORAGE_ID");
            cond.setValue(lstStorage.get(i));
            cond.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
            lstCondition.add(cond);
        }
        mapCondition.put("STORAGE_ID", lstCondition);

        //Serialize序列化
        VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vKey, vVal);
        String strCond = msdl.generateFinalCode(mapCondition);

        //Input param
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Condition);
        param1.setNetParameterValue(strCond);
        bmObj.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIWMSFetchInfoParam.Filter);
        param2.setParameterValue("AND B.BIN_TYPE IN ('IT', 'IS')");
        bmObj.params.add(param2);

        //Call BIModule
        ((WarehouseStorageDetailNewActivity)getActivity()).CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(((WarehouseStorageDetailNewActivity)getActivity()).CheckBModuleReturnInfo(bModuleReturn)){
                    DataTable dtStorageTempBin = bModuleReturn.getReturnJsonTables().get("BIFetchBin").get("BIN");

                    if (dtStorageTempBin.Rows.size() <= 0) {

                        Object[] args = new Object[1];
                        args[0] = TextUtils.join(", ", lstStorage);

                        //WAPG009030    倉庫[%s]未設定入庫暫存區或入料口
                        ((WarehouseStorageDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009030, args);
                        return;
                    }

                    // 有暫存區，就直接到暫存區/If there is a temporary storage area, go directly to the temporary storage area
                    for (DataRow dr : dtStorageTempBin.Rows) {

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

                    for (String key: mapStorageBin.keySet()) {

                        if (mapStorageBin.get(key).size() <= 0) {

                            for (DataRow dr : dtStorageTempBin.Rows) {

                                if (dr.getValue("STORAGE_ID").toString().equals(key)) {

                                    if(dr.getValue("BIN_TYPE").toString().equals("IS"))
                                        mapStorageBin.get(key).add(dr.getValue("BIN_ID").toString());
                                }
                            }
                        }
                    }

                    for (String key: mapStorageBin.keySet()) {

                        List<String> lstBin = mapStorageBin.get(key);
                        if (lstBin.size() == 0) {

                            Object[] args = new Object[1];
                            args[0] = key;

                            //WAPG009030 倉庫[%s]未設定入庫暫存區或入料口
                            ((WarehouseStorageDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009030, args);
                            return;
                        }
                    }

                    // 在每個倉庫指定的儲位List新增一筆空資料，若儲位超過一筆資料時，下拉選單預設至空白的位置，讓使用者知道要選
                    // Add an empty data in the storage location List specified in each warehouse. If the storage location exceeds one data, the drop-down list will be defaulted to a blank location to let the user know to select
                    for(String key : mapStorageBin.keySet()) {
                        mapStorageBin.get(key).add("");
                    }

                    showSelectTempBin(mapStorageBin);
                }
            }
        });
    }

    /**
     * 彈出視窗，顯示物料、倉庫、儲位(下拉選單)(若儲位選項只有一個直接顯示，兩個以上則顯示空白讓user選擇)
     * A pop-up window displays material, warehouse, and storage location (drop-down menu) (if there is only one storage location option, it will be displayed directly, and if there are more than two storage location options, it will be blank for the user to choose)
     * @param mapStorageBin 倉庫對應儲位的Map/Map of Storage and Bin
     */
    private void showSelectTempBin(HashMap<String, ArrayList<String>> mapStorageBin) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View viewConfirm = inflater.inflate(R.layout.activity_select_receive_temp_bin, null);
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setView(viewConfirm);

        final android.app.AlertDialog dialogConfirm = builder.create();
        dialogConfirm.setCancelable(false);
        dialogConfirm.show();

        //取得彈出視窗內的控制項/Get the controls in the popup window
        final ListView lvEntryLot = dialogConfirm.findViewById(R.id.lvEntryData);
        Button btnSelectConfirm = dialogConfirm.findViewById(R.id.btnBinConfirm);

        //整理資料(by 倉庫+物料)/Organize data (By StorageID + ItemID)
        ArrayList<String> lstTemp = new ArrayList<String>();
        final DataTable dtStorageItem = new DataTable();
        dtStorageItem.addColumn(new DataColumn("ITEM_ID"));
        dtStorageItem.addColumn(new DataColumn("ITEM_NAME"));
        dtStorageItem.addColumn(new DataColumn("STORAGE_ID"));
        dtStorageItem.addColumn(new DataColumn("BIN_ID"));

        //整理下拉選單資料(倉庫＋物料 對應 儲位)/Organize the data in drop down spinner (warehouse + material corresponding storage location)
        final HashMap<String, ArrayList<String>> mapItemStorageBin = new HashMap<>();

        for (DataRow dr : dtDet.Rows){
            String strStorageId = dr.getValue("STORAGE_ID").toString();
            String strItemId = dr.getValue("ITEM_ID").toString();
            String strSelectKey = strStorageId + "_" + strItemId;

            if(!lstTemp.contains(strSelectKey)){
                lstTemp.add(strSelectKey);
                DataRow drNew = dtStorageItem.newRow();
                drNew.setValue("ITEM_ID", dr.getValue("ITEM_ID").toString());
                drNew.setValue("ITEM_NAME", dr.getValue("ITEM_NAME").toString());
                drNew.setValue("STORAGE_ID", dr.getValue("STORAGE_ID").toString());
                drNew.setValue("BIN_ID", "");
                dtStorageItem.Rows.add(drNew);
                mapItemStorageBin.put(strSelectKey, mapStorageBin.get(strStorageId));
            }
        }

        LayoutInflater inflaterSelect = LayoutInflater.from(getContext());
        ReceiveSelectTempBinGridAdapter adapterSelect = new ReceiveSelectTempBinGridAdapter(getContext(), dtStorageItem, mapItemStorageBin, inflaterSelect);
        lvEntryLot.setAdapter(adapterSelect);
        adapterSelect.notifyDataSetChanged();

        btnSelectConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 將 ListView 內每一列資料做迴圈/Make a iterator for each row of data in the ListView
                for (int i = 0; i < lvEntryLot.getChildCount(); i++) {

                    View listItem = lvEntryLot.getChildAt(i); // 取得該列的 ListView item / Get the ListView item of the column
                    Spinner spinner = listItem.findViewById(R.id.cmbBinID); // 進一步取得 item 內的 spinner / Obtain the spinner in the item

                    String itemId = dtStorageItem.Rows.get(i).getValue("ITEM_ID").toString();
                    String storageId = dtStorageItem.Rows.get(i).getValue("STORAGE_ID").toString();

                    String selectedBin = (String) spinner.getSelectedItem();

                    if (selectedBin.equals("")){
                        Object[] args = new Object[2];
                        args[0] = storageId;
                        args[1] = itemId;

                        // WAPG027038    倉庫[%s]物料[%s]尚未選擇對應的入庫儲位
                        ((WarehouseStorageDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG027038, args);
                        return;
                    }

                    // 將選擇儲位記錄起來
                    dtStorageItem.Rows.get(i).setValue("BIN_ID", selectedBin);
                }

                dialogConfirm.dismiss(); // 關閉選擇頁面

                // 將儲位資訊記錄到待入庫Obj
                for(DataRow drLot : dtWvrDetWithPackingInfo.Rows) {
                    String storage = drLot.getValue("STORAGE_ID").toString();
                    String item = drLot.getValue("ITEM_ID").toString();

                    for (DataRow dr : dtStorageItem.Rows) {
                        if (dr.getValue("STORAGE_ID").equals(storage) && dr.getValue("ITEM_ID").equals(item)) {
                            drLot.setValue("TEMP_BIN", dr.getValue("BIN_ID").toString());
                            break;
                        }
                    }
                }

                executeProcess();
            }
        });
    }

    private void executeProcess() {

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
        param5.setParameterValue("Confirmed");
        biObj1.params.add(param5);

        ParameterInfo paramExecuteChkStock = new ParameterInfo();
        paramExecuteChkStock.setParameterID(BIWarehouseStoragePortalParam.ExecuteCheckStock);
        paramExecuteChkStock.setNetParameterValue2("true");
        biObj1.params.add(paramExecuteChkStock);

        List<CheckCountObj>  lstChkCountObj  = new ArrayList<>(); // 20220805 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中
        for(DataRow dr : dtWvrDetWithPackingInfo.Rows)
        {
            // region 儲存盤點狀態檢查物件
            CheckCountObj chkCountObjToBin = new CheckCountObj(); // TO_BIN
            chkCountObjToBin.setStorageId(dr.getValue("STORAGE_ID").toString());
            chkCountObjToBin.setItemId(dr.getValue("ITEM_ID").toString());
            chkCountObjToBin.setBinId(dr.getValue("TEMP_BIN").toString());
            lstChkCountObj.add(chkCountObjToBin);
            // endregion
        }

        VirtualClass vListEnum3 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.CheckCountObj", "bmWMS.Library.Param");
        MList mListEnum3 = new MList(vListEnum3);
        String strCheckCountObj = mListEnum3.generateFinalCode(lstChkCountObj);

        ParameterInfo paramChkCountObj = new ParameterInfo(); // 20220805 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中
        paramChkCountObj.setParameterID(BIWarehouseStoragePortalParam.CheckCountObj);
        paramChkCountObj.setNetParameterValue(strCheckCountObj);
        biObj1.params.add(paramChkCountObj);

        ((WarehouseStorageDetailNewActivity)getActivity()).CallBModule(biObj1, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (((WarehouseStorageDetailNewActivity)getActivity()).CheckBModuleReturnInfo(bModuleReturn)) {

                    //WAPG027034    作業成功!
                    ((WarehouseStorageDetailNewActivity) getActivity()).ShowMessage(R.string.WAPG027034, new ShowMessageEvent() {
                        @Override
                        public void onDismiss() {
                            ((WarehouseStorageDetailNewActivity)getActivity()).gotoPreviousActivity(WarehouseStorageActivity.class, true);
                        }
                    });
                }
            }
        });
    }

    private AdapterView.OnItemClickListener lvOnClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {

            DataRow selectedRow = dtDet.Rows.get(pos);

            DataTable selectedWvrGroup = new DataTable();
            selectedWvrGroup.getColumns().addAll(dtWvrDetGroup.getColumns());
            for (DataRow drWvrGroup : dtWvrDetGroup.Rows) {
                if (drWvrGroup.get("SEQ").toString().equals(selectedRow.get("SEQ").toString()))
                    selectedWvrGroup.Rows.add(drWvrGroup);
            }

            DataTable selectedWvrWithPackingInfo = new DataTable();
            selectedWvrWithPackingInfo.getColumns().addAll(dtWvrDetWithPackingInfo.getColumns());
            for (DataRow drWvrWithPackingInfo : dtWvrDetWithPackingInfo.Rows) {
                if (drWvrWithPackingInfo.get("SEQ").toString().equals(selectedRow.get("SEQ").toString()))
                    selectedWvrWithPackingInfo.Rows.add(drWvrWithPackingInfo);
            }

            Bundle itemInfo = new Bundle();
            itemInfo.putString("sheetPolicyId", sheetPolicyId);
            itemInfo.putString("wvId", wvId);
            itemInfo.putSerializable("dtMst", dtMst);
            itemInfo.putSerializable("dtDetRow", selectedRow);
            itemInfo.putSerializable("selectedWvrGroup", selectedWvrGroup);
            itemInfo.putSerializable("selectedWvrWithPackingInfo", selectedWvrWithPackingInfo);
            itemInfo.putSerializable("dtWvrDet", dtWvrDetWithPackingInfo);

            ((WarehouseStorageDetailNewActivity)getActivity()).gotoNextActivityForResult(WarehouseStorageReceivedActivity.class, itemInfo, new WarehouseStorageDetailNewActivity.OnActivityResult() {
                @Override
                public void onResult(Bundle bundle) {
                    // do noting
                    // 會返回到 WarehouseStorageDetailNewActivity 的 onActivityResult 執行
                }
            });
        }
    };


}
