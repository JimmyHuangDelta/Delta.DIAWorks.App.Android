package com.delta.android.WMS.Client.Fragment;

import android.content.Context;
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
import android.widget.TextView;

import com.delta.android.Core.Activity.ShowMessageEvent;
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
import com.delta.android.WMS.Client.GridAdapter.ReceiveSelectTempBinGridAdapter;
import com.delta.android.WMS.Client.GridAdapter.WarehouseStorageNonSheetGridAdapter;
import com.delta.android.WMS.Client.WarehouseStorageNonSheetDetailNewActivity;
import com.delta.android.WMS.Client.WarehouseStorageNonSheetNewActivity;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.BWarehouseStorageNonSheetWithPackingInfoParam;
import com.delta.android.WMS.Param.ParamObj.CheckCountObj;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.delta.android.WMS.Param.ParamObj.WarehouseVoucherDetObj;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class WarehouseStorageNonSheetListFragment extends Fragment {

    IModifyNonSheetData iModifyNonSheetData;

    private ListView lvWarehouseStorageSheet;
    private Button btnConfirm;

    private DataTable dtWvrDetAll = null;
    private DataTable dtWvrDetGroupAll = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_warehouse_storage_non_sheet_list, container, false);

        setInitWidget(view);

        setListeners();

        return view;
    }

    /**
     * 元件初始化
     * Initialize widget
     * @param view
     */
    private void setInitWidget(View view) {
        lvWarehouseStorageSheet = view.findViewById(R.id.lvWarehouseStorageSheet);
        btnConfirm = view.findViewById(R.id.btnConfirm);

        dtWvrDetAll = createWarehouseStorageTable();
        dtWvrDetGroupAll = createWarehouseStorageTable();
    }

    /**
     *設置元件監聽器
     * Setting the listeners of widget
     */
    private void setListeners() {
        lvWarehouseStorageSheet.setOnItemLongClickListener(onLongClickDet);
        btnConfirm.setOnClickListener(onClickConfirm);
    }

    /**
     * 長按入庫明細項次後跳出是否刪除視窗的事件
     * Long press the storage details item to pop up the event of whether to delete the window
     */
    private AdapterView.OnItemLongClickListener onLongClickDet = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long id) {

            final DataRow drSelectedRow = dtWvrDetGroupAll.Rows.get(pos);

            //region 跳出詢問視窗/Jump out of the inquiry window
            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View deleteView = inflater.inflate(R.layout.activity_wms_warehouse_storage_stock_in_delete,null );
            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
            builder.setView(deleteView);

            final android.app.AlertDialog deleteDialog = builder.create();
            deleteDialog.setCancelable(false);//只允許按下dialog裡的按鍵才能關閉dialog/Only the button in the dialog is allowed to close the dialog
            deleteDialog.show();

            TextView tvCurrentSN = deleteDialog.findViewById(R.id.tvDialogMessage);
            // WAPG010025   是否刪除此存貨編號?
            String strErr = getResources().getString(R.string.WAPG010025);
            tvCurrentSN.setText(strErr);
            //endregion

            //region 確認刪除/Confirm to delete
            Button btnDelete = deleteView.findViewById(R.id.btnYes);
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    for (Iterator<DataRow> iterator = dtWvrDetAll.Rows.iterator(); iterator.hasNext();) {
                        DataRow drDelete = iterator.next();
                        if (drDelete.getValue("SKU_NUM").toString().equals(drSelectedRow.getValue("SKU_NUM").toString())) {
                            iterator.remove();
                        }
                    }

                    dtWvrDetGroupAll.Rows.remove(drSelectedRow);

                    List<String> lstExistSkuNum = new ArrayList<>();

                    for (DataRow dr : dtWvrDetGroupAll.Rows) {
                        if (!lstExistSkuNum.contains(dr.get("SKU_NUM").toString()))
                            lstExistSkuNum.add(dr.get("SKU_NUM").toString());
                    }

                    iModifyNonSheetData.modifyDrWvrDet(lstExistSkuNum);

                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    WarehouseStorageNonSheetGridAdapter adapter = new WarehouseStorageNonSheetGridAdapter(dtWvrDetGroupAll, inflater);
                    lvWarehouseStorageSheet.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    deleteDialog.dismiss();
                }
            });
            //endregion

            //region 取消刪除/Cancel to delete
            Button btnCancel = deleteView.findViewById(R.id.btnNo);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteDialog.dismiss();
                }
            });
            //endregion

            return true;
        }
    };

    /**
     * 點選確認後跳出儲位選擇視窗的事件
     * The event that jumps out of the storage location selection window after clicking OK
     */
    private View.OnClickListener onClickConfirm = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (dtWvrDetAll.Rows.size() <= 0) {

                // WAPG010011    尚未新增物料
                ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG010011);
                return;
            }

            // 取得單據類所有倉庫資訊/Obtain all storage information in sheet
            ArrayList<String> lstStorage = new ArrayList<String>();
            for (DataRow drDet : dtWvrDetGroupAll.Rows){
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
        ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(((WarehouseStorageNonSheetDetailNewActivity)getActivity()).CheckBModuleReturnInfo(bModuleReturn)){
                    DataTable dtStorageTempBin = bModuleReturn.getReturnJsonTables().get("BIFetchBin").get("BIN");

                    if (dtStorageTempBin.Rows.size() <= 0) {

                        Object[] args = new Object[1];
                        args[0] = TextUtils.join(", ", lstStorage);

                        // WAPG010022    倉庫[%s]未設定入庫暫存區或入料口
                        ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG010022, args);
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

                            //WAPG010022 倉庫[%s]未設定入庫暫存區或入料口
                            ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG010022, args);
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
        dialogConfirm.setCancelable(true);
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

        for (DataRow dr : dtWvrDetGroupAll.Rows){
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

                        // WAPG010023    倉庫[%s]物料[%s]尚未選擇對應的入庫儲位
                        ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG010023, args);
                        return;
                    }

                    // 將選擇儲位記錄起來/Record the record
                    dtStorageItem.Rows.get(i).setValue("BIN_ID", selectedBin);
                }

                dialogConfirm.dismiss(); // 關閉選擇頁面/Close the selection dialog

                // 將儲位資訊記錄到待入庫Obj/Record the storage location information to the Obj to be stored
                for(DataRow drLot : dtWvrDetAll.Rows) {
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

    /**
     * 執行入庫BModule
     * Execute Warehouse Storage BModule
     */
    private void executeProcess() {

        String sheetTypeId = ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).sheetTypeId;
        String organId = ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).organId;
        String wvSource = ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).wvSource;

        List<WarehouseVoucherDetObj> lstDetObj = CreateDetObj(dtWvrDetAll);

        // region 儲存盤點狀態檢查物件/Store the objects to check the counting status
        List<CheckCountObj> lstChkCountObj = new ArrayList<>();
        for (DataRow dr : dtWvrDetAll.Rows) {
            CheckCountObj chkCountObj = new CheckCountObj();
            chkCountObj.setStorageId(dr.getValue("STORAGE_ID").toString());
            chkCountObj.setItemId(dr.getValue("ITEM_ID").toString());
            chkCountObj.setBinId(dr.getValue("TEMP_BIN").toString());
            lstChkCountObj.add(chkCountObj);
        }
        // endregion

        //Call BModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BWarehouseStorageNonSheetWithPackingInfo");
        bmObj.setModuleID("");
        bmObj.setRequestID("BWarehouseStorageNonSheetWithPackingInfo");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo paramSheetType = new ParameterInfo();
        paramSheetType.setParameterID(BWarehouseStorageNonSheetWithPackingInfoParam.SheetTypeID);
        paramSheetType.setParameterValue(sheetTypeId);
        bmObj.params.add(paramSheetType);

        ParameterInfo paramOrgan = new ParameterInfo();
        paramOrgan.setParameterID(BWarehouseStorageNonSheetWithPackingInfoParam.OrganID);
        paramOrgan.setParameterValue(organId);
        bmObj.params.add(paramOrgan);

        ParameterInfo paramSource = new ParameterInfo();
        paramSource.setParameterID(BWarehouseStorageNonSheetWithPackingInfoParam.WvSource);
        paramSource.setParameterValue(wvSource);
        bmObj.params.add(paramSource);

        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Sheet.Parameter.WarehouseVoucherDetObj", "bmWMS.Sheet.Param");
        MList mListEnum = new MList(vListEnum);
        String strDetObj = mListEnum.generateFinalCode(lstDetObj);

        ParameterInfo paramObj = new ParameterInfo();
        paramObj.setParameterID(BWarehouseStorageNonSheetWithPackingInfoParam.WvDetObj);
        paramObj.setNetParameterValue(strDetObj);
        bmObj.params.add(paramObj);

        ParameterInfo paramExecuteChkStock = new ParameterInfo();
        paramExecuteChkStock.setParameterID(BWarehouseStorageNonSheetWithPackingInfoParam.ExecuteCheckStock); // 20220805 Add by Ikea 是否執行盤點檢查
        paramExecuteChkStock.setNetParameterValue2("true");
        bmObj.params.add(paramExecuteChkStock);

        VirtualClass vListEnum2 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.CheckCountObj", "bmWMS.Library.Param");
        MList mListEnum2 = new MList(vListEnum2);
        String strCheckCountObj = mListEnum2.generateFinalCode(lstChkCountObj);

        ParameterInfo paramChkCountObj = new ParameterInfo();
        paramChkCountObj.setParameterID(BWarehouseStorageNonSheetWithPackingInfoParam.CheckCountObj);
        paramChkCountObj.setNetParameterValue(strCheckCountObj);
        bmObj.params.add(paramChkCountObj);

        ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).CallBModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(!((WarehouseStorageNonSheetDetailNewActivity)getActivity()).CheckBModuleReturnInfo(bModuleReturn)) return;

                Gson gson = new Gson();
                String sheetID = "";
                sheetID = gson.fromJson(bModuleReturn.getReturnList().get("BWarehouseStorageNonSheetWithPackingInfo").get(BWarehouseStorageNonSheetWithPackingInfoParam.WvSheetID).toString(), String.class);

                Object[] args = new Object[1];
                args[0] = sheetID;

                // WAPG010024    作業成功，入庫單號[%s]
                ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG010024, new ShowMessageEvent() {
                    @Override
                    public void onDismiss() {
                        ((WarehouseStorageNonSheetDetailNewActivity)getActivity()).gotoPreviousActivity(WarehouseStorageNonSheetNewActivity.class, true);
                    }
                }, args);

            }
        });
    }

    /**
     * 將欲入庫資料轉為物件
     * Convert the data to be stored into objects
     * @param dtLotID
     * @return
     */
    private List<WarehouseVoucherDetObj> CreateDetObj(DataTable dtLotID){
        List<WarehouseVoucherDetObj> lstDetObj = new ArrayList<WarehouseVoucherDetObj>();

        WarehouseVoucherDetObj det = null;
        for (DataRow dr : dtLotID.Rows){
            det = new WarehouseVoucherDetObj();

            //det.setSeq(Double.parseDouble(dr.getValue("SEQ").toString()));
            det.setStorageId(dr.getValue("STORAGE_ID").toString());
            det.setWoId(dr.getValue("WO_ID").toString());
            det.setItemId(dr.getValue("ITEM_ID").toString());
            det.setLotId(dr.getValue("LOT_ID").toString());
            det.setLotCode(dr.getValue("LOT_CODE").toString());
            det.setQty(Double.parseDouble(dr.getValue("QTY").toString()));
            //det.setScrapQty(Double.parseDouble(dr.getValue("SCRAP_QTY").toString()));
            det.setTempBin(dr.getValue("TEMP_BIN").toString());
            det.setBox1Id(dr.getValue("BOX1_ID").toString());
            det.setBox2Id(dr.getValue("BOX2_ID").toString());
            det.setBox3Id(dr.getValue("BOX3_ID").toString());
            det.setPalletId(dr.getValue("PALLET_ID").toString());

            try{
                if(dr.getValue("MFG_DATE") != null && !dr.getValue("MFG_DATE").toString().equals("")){
                    Date mfg = new SimpleDateFormat("yyyy-MM-dd").parse(dr.getValue("MFG_DATE").toString());
                    det.setMfgDate(mfg);
                }
                if(dr.getValue("EXP_DATE") != null && !dr.get("EXP_DATE").toString().equals("")){
                    Date exp = new SimpleDateFormat("yyyy-MM-dd").parse(dr.getValue("EXP_DATE").toString());
                    det.setExpDate(exp);
                }
            }catch (ParseException e){

            }

            det.setUom(dr.getValue("UOM").toString());
            det.setCmt(dr.getValue("CMT").toString());
            det.setSpecLot(dr.getValue("SPEC_LOT").toString());

            lstDetObj.add(det);
        }
        return lstDetObj;
    }

    /**
     * 新建儲存畫面上呈現的入庫資料與實際儲存的入庫資料
     * The warehouse storage data presented on the new storage screen and the actually stored warehouse storage data
     * @return
     */
    private DataTable createWarehouseStorageTable() {
        DataTable dtWs = new DataTable();
        dtWs.addColumn(new DataColumn("SEQ"));
        dtWs.addColumn(new DataColumn("SKU_LEVEL"));
        dtWs.addColumn(new DataColumn("SKU_NUM"));
        dtWs.addColumn(new DataColumn("STORAGE_KEY"));
        dtWs.addColumn(new DataColumn("STORAGE_ID"));
        dtWs.addColumn(new DataColumn("ITEM_KEY"));
        dtWs.addColumn(new DataColumn("ITEM_ID"));
        dtWs.addColumn(new DataColumn("ITEM_NAME"));
        dtWs.addColumn(new DataColumn("LOT_ID"));
        dtWs.addColumn(new DataColumn("SPEC_LOT"));
        dtWs.addColumn(new DataColumn("QTY"));
        dtWs.addColumn(new DataColumn("UOM"));
        dtWs.addColumn(new DataColumn("CMT"));
        dtWs.addColumn(new DataColumn("MFG_DATE"));
        dtWs.addColumn(new DataColumn("EXP_DATE"));
        dtWs.addColumn(new DataColumn("TEMP_BIN"));
        dtWs.addColumn(new DataColumn("WO_ID"));
        dtWs.addColumn(new DataColumn("LOT_CODE"));
        dtWs.addColumn(new DataColumn("BOX1_ID"));
        dtWs.addColumn(new DataColumn("BOX2_ID"));
        dtWs.addColumn(new DataColumn("BOX3_ID"));
        dtWs.addColumn(new DataColumn("PALLET_ID"));
        return dtWs;
    }

    // region Interface
    public interface IModifyNonSheetData {

        void modifyDrWvrDet(List<String> lstExistSkuNum);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            iModifyNonSheetData = (IModifyNonSheetData) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("Plz implement interface method");
        }
    }

    /**
     * 於入庫頁籤點選新增時，將資料放到入庫明細頁籤
     * When you click Add on the Inbound tab, put the data on the Inbound Details tab
     * @param dtWvrDet
     * @param dtWvrDetGroup
     */
    public void getWvrDet(DataTable dtWvrDet, DataTable dtWvrDetGroup) {

        // 顯示資料用/Display data
        dtWvrDetGroupAll.Rows.add(dtWvrDetGroup.Rows.get(0));

        // 實際存入資料庫用/Actually stored in the database
        for (DataRow drDet : dtWvrDet.Rows) {
            dtWvrDetAll.Rows.add(drDet);
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        WarehouseStorageNonSheetGridAdapter adapter = new WarehouseStorageNonSheetGridAdapter(dtWvrDetGroupAll, inflater);
        lvWarehouseStorageSheet.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
    //endregion
}
// ErrorCode
// WAPG010022    倉庫[%s]未設定入庫暫存區或入料口
// WAPG010023    倉庫[%s]物料[%s]尚未選擇對應的入庫儲位
// WAPG010024    作業成功，入庫單號[%s]
// WAPG010025    是否刪除此存貨編號?