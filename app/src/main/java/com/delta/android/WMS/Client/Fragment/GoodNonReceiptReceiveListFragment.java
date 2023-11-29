package com.delta.android.WMS.Client.Fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.delta.android.Core.Activity.ShowMessageEvent;
import com.delta.android.Core.DataTable.DataColumn;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.GenerateJsonNetString.MList;
import com.delta.android.Core.GenerateJsonNetString.MesClass;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionary;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionaryList;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Client.GoodNonReceiptReceiveDetailNewActivity;
import com.delta.android.WMS.Client.GoodNonReceiptReceiveNewActivity;
import com.delta.android.WMS.Client.GridAdapter.GoodNonreceiptReceiveGridAdapter;
import com.delta.android.WMS.Client.GridAdapter.GoodNonreceiptReceiveSelectGridAdapter;
import com.delta.android.WMS.Param.BGoodReceiptReceiveNonSheetWithPackingInfoParam;
import com.delta.android.WMS.Param.BIGoodNonReceiptReceivePortalParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.ParamObj.CheckCountObj;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class GoodNonReceiptReceiveListFragment extends Fragment {
    private DataTable dtLotWithSkuLevelAll, dtLotAll, dtSnAll, dtReceiptMst;

    IModifyNonReceiptReceiveDataTable iModifyNonReceiptReceiveDataTable;

    // region -- 收料明細控制項 --
    private ListView lvReceiveSheet;
    private Button btnConfirm;
    // endregion

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_good_non_receipt_receive_list, container, false);

        lvReceiveSheet = view.findViewById(R.id.lvReceiveSheet);
        btnConfirm = view.findViewById(R.id.bConfirm);

        dtReceiptMst = (DataTable)getFragmentManager().findFragmentByTag("ReceiveList").getArguments().getSerializable("dtReceiptMst");

        dtLotWithSkuLevelAll = createDataTableLotID();
        dtLotAll = createDataTableLotID();
        dtSnAll = createDataTableSN();

        setListeners();

        return view;
    }

    // region -- Private Method --

    private void setListeners() {
        lvReceiveSheet.setOnItemClickListener(onClickReceiveSheet);
        btnConfirm.setOnClickListener(onClickConfirm);
    }

    private AdapterView.OnItemClickListener onClickReceiveSheet = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            DataRow selectedRow = (DataRow) parent.getItemAtPosition(position);

            String findSkuLevel = selectedRow.getValue("SKU_LEVEL").toString();
            String findSkuNum = selectedRow.getValue("SKU_NUM").toString();

            DataTable dtLotTemp = createDataTableLotID();
            for (DataRow drFind : dtLotAll.Rows) {
                if (drFind.getValue("SKU_LEVEL").toString().equals(findSkuLevel) && drFind.getValue("SKU_NUM").toString().equals(findSkuNum))
                    dtLotTemp.Rows.add(drFind);
            }


            DataTable dtSnTemp = createDataTableSN();

            if (selectedRow.getValue("REGISTER_TYPE").equals("PcsSN")) {

                List<String> lstRefKeys = new ArrayList<>();
                for (DataRow drFind : dtLotTemp.Rows) {
                    if (drFind.getValue("SKU_LEVEL").equals(findSkuLevel) && drFind.getValue("SKU_NUM").equals(findSkuNum)) {

                        String refKey = drFind.getValue("GRR_DET_SN_REF_KEY").toString();

                        if (!lstRefKeys.contains(refKey))
                            lstRefKeys.add(refKey);
                    }
                }

//                String[] aryRefKeys = new String[lstRefKeys.size()];
//                lstRefKeys.toArray(aryRefKeys);
                        //grrDetRefKey = selectedRow.getValue("GRR_DET_SN_REF_KEY").toString();

                for (Iterator<DataRow> iterator = dtSnAll.Rows.iterator(); iterator.hasNext();) {
                    DataRow drSn = iterator.next();
                    String snRefKey = drSn.getValue("GRR_DET_SN_REF_KEY").toString();
                    if (lstRefKeys.contains(snRefKey))
                        dtSnTemp.Rows.add(drSn);
//                    if (drSn.getValue("GRR_DET_SN_REF_KEY").equals(grrDetRefKey)) {
//                        dtSnTemp.Rows.add(drSn);
//                        //iterator.remove();
//                    }
                }
            }

//            iModifyNonReceiptReceiveDataTable.modifyDrLotData(selectedRow, dtSnTemp, "Modify", position);
            iModifyNonReceiptReceiveDataTable.modifyDrLotData(selectedRow, dtLotTemp, dtSnTemp, "Modify", position);

            TabLayout tb = getActivity().findViewById(R.id.tabLayout);
            tb.getTabAt(0).select();
        }
    };

    private View.OnClickListener onClickConfirm = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            ArrayList<String> lstStorage = new ArrayList<String>();
            for (DataRow drLot : dtLotAll.Rows){
                String storageId = drLot.getValue("STORAGE_ID").toString();
                if(!lstStorage.contains(storageId)){
                    lstStorage.add(storageId);
                }
            }

            checkStorageInTempBin(lstStorage); // 依據料倉儲選擇收料暫存儲位
        }
    };

    private void checkStorageInTempBin(final List<String> lstStorage) {

        final HashMap<String, ArrayList<String>> mapStorageBin = new HashMap<String, ArrayList<String>>(); // 存放 IT 或 IS
        final HashMap<String, ArrayList<String>> mapStorageIqcBin = new HashMap<String, ArrayList<String>>(); // 存放 IQC
        HashMap<String, ArrayList<String>> mapItemStorageBin = new HashMap<String, ArrayList<String>>(); // 存放物料倉儲儲位

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchBin");
        bmObj.setRequestID("BIFetchBin");

        bmObj.params = new Vector<ParameterInfo>();
        //裝Condition的容器
        final HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
        List<Condition> lstCondition = new ArrayList<Condition>();
        for (int i = 0; i < lstStorage.size(); i++)
        {
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

        boolean bSkipQC = true;
        for(DataRow lot : dtLotAll.Rows){
            String strQC = lot.getValue("SKIP_QC").toString();
            if(strQC.equals("N")){
                bSkipQC = false;
                break;
            }
        }

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIWMSFetchInfoParam.Filter);
        if(bSkipQC)
            param2.setParameterValue("AND B.BIN_TYPE IN ('IT', 'IS')");
        else
            param2.setParameterValue("AND B.BIN_TYPE IN ('IT', 'IS', 'IQC')");
        bmObj.params.add(param2);

        //Call BIModule
        final boolean finalBSkipQC = bSkipQC;
        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(((GoodNonReceiptReceiveDetailNewActivity)getActivity()).CheckBModuleReturnInfo(bModuleReturn)){
                    DataTable dtStorageTempBin = bModuleReturn.getReturnJsonTables().get("BIFetchBin").get("BIN");

                    if (dtStorageTempBin.Rows.size() <= 0) {

                        Object[] args = new Object[1];
                        args[0] = TextUtils.join(", ", lstStorage);

                        if (finalBSkipQC) {

                            ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009030, args); //WAPG009030    倉庫[%s]未設定入庫暫存區或入料口
                            return;

                        } else {

                            ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009031, args); //WAPG009031    倉庫[%s]未設定入庫暫存區, 入料口, 或 IQC 儲位
                            return;

                        }
                    }

                    // 有IQC，存放IQC/Have IQC, store IQC
                    for (DataRow dr : dtStorageTempBin.Rows) {
                        if(!mapStorageIqcBin.containsKey(dr.getValue("STORAGE_ID").toString()))
                        {
                            ArrayList<String> lstBin = new ArrayList<>();

                            if(dr.getValue("BIN_TYPE").toString().equals("IQC"))
                                lstBin.add(dr.getValue("BIN_ID").toString());

                            mapStorageIqcBin.put(dr.getValue("STORAGE_ID").toString(), lstBin);

                        }
                        else {

                            if(dr.getValue("BIN_TYPE").toString().equals("IQC"))
                                mapStorageIqcBin.get(dr.getValue("STORAGE_ID").toString()).add(dr.getValue("BIN_ID").toString());
                        }
                    }

                    if (finalBSkipQC == false) {

                        for (String key: mapStorageIqcBin.keySet()) {

                            List<String> lstBin = mapStorageIqcBin.get(key);
                            if (lstBin.size() == 0) {

                                Object[] args = new Object[1];
                                args[0] = key;

                                ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009031, args); //WAPG009031 倉庫[%s]未設定進料檢驗儲位
                                return;
                            }
                        }
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

                            ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009030, args); //WAPG009030 倉庫[%s]未設定入庫暫存區或入料口
                            return;
                        }
                    }


                    showConfirmDialog(mapStorageBin, mapStorageIqcBin);
                }
            }
        });
    }

    private void showConfirmDialog(final HashMap<String, ArrayList<String>> mapStorageBin, final HashMap<String, ArrayList<String>> mapStorageIqcBin) {

        final HashMap<String, ArrayList<String>> mapItemStorageBin = new HashMap<>();

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View viewConfirm = inflater.inflate(R.layout.activity_good_nonreceipt_receive_confirm, null);
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setView(viewConfirm);

        final android.app.AlertDialog dialogConfirm = builder.create();
        dialogConfirm.setCancelable(false);
        dialogConfirm.show();

        //取得控制項
        final ListView lvStorageItem = dialogConfirm.findViewById(R.id.lvReceiveLot);
        Button btnSelectConfirm = dialogConfirm.findViewById(R.id.btnBinConfirm);

        //整理資料(by 倉庫+物料)
        ArrayList<String> lstTemp = new ArrayList<String>();
        final DataTable dtStorageItem = new DataTable();
        dtStorageItem.addColumn(new DataColumn("ITEM_ID"));
        dtStorageItem.addColumn(new DataColumn("ITEM_NAME"));
        dtStorageItem.addColumn(new DataColumn("STORAGE_ID"));
        dtStorageItem.addColumn(new DataColumn("BIN_ID"));

        for (DataRow dr : dtLotAll.Rows){
            String strStorageId = dr.getValue("STORAGE_ID").toString();
            String strItemId = dr.getValue("ITEM_ID").toString();
            String strQC = dr.getValue("SKIP_QC").toString();
            String strSelectKey = strStorageId + "_" + strItemId;

            //當取得的儲位不只一個時才加入
            if(!lstTemp.contains(strSelectKey)){
                lstTemp.add(strSelectKey);
                DataRow drNew = dtStorageItem.newRow();
                drNew.setValue("ITEM_ID", dr.getValue("ITEM_ID").toString());
                drNew.setValue("ITEM_NAME", dr.getValue("ITEM_NAME").toString());
                drNew.setValue("STORAGE_ID", dr.getValue("STORAGE_ID").toString());
                drNew.setValue("BIN_ID", "");
                dtStorageItem.Rows.add(drNew);

                if (strQC.equals("Y"))
                    mapItemStorageBin.put(strSelectKey, mapStorageBin.get(strStorageId));
                else
                    mapItemStorageBin.put(strSelectKey, mapStorageIqcBin.get(strStorageId));
            }
        }

        LayoutInflater inflaterSelect = LayoutInflater.from(getContext());
        GoodNonreceiptReceiveSelectGridAdapter adapterSelect = new GoodNonreceiptReceiveSelectGridAdapter(dtStorageItem, inflaterSelect);
        lvStorageItem.setAdapter(adapterSelect);
        adapterSelect.notifyDataSetChanged();

        lvStorageItem.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                LayoutInflater inflaterBin = LayoutInflater.from(getContext());
                final View viewBin = inflaterBin.inflate(R.layout.activity_good_nonreceipt_receive_confirm_select_bin, null);
                final android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.Builder(getContext());
                builder1.setView(viewBin);

                final android.app.AlertDialog dialogBin = builder1.create();
                dialogBin.setCancelable(false);
                dialogBin.show();

                final Spinner cmbBin = dialogBin.findViewById(R.id.cmbBinID);
                final Button btnBinConfirm = dialogBin.findViewById(R.id.btnBinConfirm);

                final String strStorage = dtStorageItem.getValue(position, "STORAGE_ID").toString();
                final String strItem = dtStorageItem.getValue(position, "ITEM_ID").toString();
                ArrayAdapter<String> adapterBin = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, mapItemStorageBin.get(strStorage + "_" + strItem));
                cmbBin.setAdapter(adapterBin);

                cmbBin.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l){
                        for(DataRow dr : dtStorageItem.Rows){
                            if(dr.getValue("STORAGE_ID").toString().equals(strStorage) &&
                                    dr.getValue("ITEM_ID").toString().equals(strItem)){
                                dr.setValue("BIN_ID", mapItemStorageBin.get(strStorage + "_" + strItem).get(position)); //mapStorageBin.get(strStorage).get(position)
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView){
                        //do nothing
                    }
                });

                btnBinConfirm.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        //refreash listview
                        LayoutInflater inflaterSelect = LayoutInflater.from(getContext());
                        GoodNonreceiptReceiveSelectGridAdapter adapterSelect = new GoodNonreceiptReceiveSelectGridAdapter(dtStorageItem, inflaterSelect);
                        lvStorageItem.setAdapter(adapterSelect);

                        dialogBin.dismiss();
                    }
                });
            }
        });

        btnSelectConfirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                for(DataRow dr : dtStorageItem.Rows){
                    if(dr.getValue("BIN_ID").toString().equals("")){
                        Object[] args = new Object[2];
                        args[0] = dr.getValue("STORAGE_ID").toString();
                        args[1] = dr.getValue("ITEM_ID").toString();
                        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009018, args); //WAPG009018    尚未選擇倉庫[%s]物料[%s]對應儲位
                        return;
                    }
                }
                dialogConfirm.dismiss();

//                HashMap<String, String> mapStorageBinResult = new HashMap<String, String>();

                //帶入TEMP_BIN
                for(DataRow drLot : dtLotAll.Rows){
                    String storage = drLot.getValue("STORAGE_ID").toString();
                    String itemid = drLot.getValue("ITEM_ID").toString();
                    for (DataRow dr : dtStorageItem.Rows){
                        if(dr.getValue("STORAGE_ID").toString().equals(storage) &&
                                dr.getValue("ITEM_ID").toString().equals(itemid)){
                            drLot.setValue("TEMP_BIN", dr.getValue("BIN_ID").toString());
//                                if(!mapStorageBinResult.containsKey(storage)){
//                                mapStorageBinResult.put(storage, dr.getValue("BIN_ID").toString());
//                            }
                        }
                    }
                }

                //createSheetID(mapStorageBinResult);
                callBmCreateSheet();
            }
        });
    }

    private void callBmCreateSheet() {

        if(dtLotAll.Rows.size() <= 0) {
            ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009016);// WAPG009016  尚未新增任何物料代碼!
            return;
        }
        if(!checkSN()) return;

        BModuleObject bmObjSetData = new BModuleObject();
        bmObjSetData.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIGoodNonReceiptReceivePortal");
        bmObjSetData.setModuleID("BISetNonReceiptReceiveData");
        bmObjSetData.setRequestID("BISetNonReceiptReceiveData");
        bmObjSetData.params = new Vector<>();

        BGoodReceiptReceiveNonSheetWithPackingInfoParam sheet = new BGoodReceiptReceiveNonSheetWithPackingInfoParam();
        BGoodReceiptReceiveNonSheetWithPackingInfoParam.GrNonSheetWithPackingInfoMasterObj sheetMstObj = sheet.new GrNonSheetWithPackingInfoMasterObj();

        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.INV.Parameter.GrNonSheetWithPackingInfoMasterObj","bmWMS.INV.Param"); // BGoodReceiptReceiveEditParam.GrMasterObj
        MesClass mesClassEnum = new MesClass(vListEnum);
        String strGrrMstObj = mesClassEnum.generateFinalCode(sheetMstObj.GetGrrSheet(dtReceiptMst, dtLotAll, dtSnAll));

        ParameterInfo paramGrrMstObj = new ParameterInfo();
        paramGrrMstObj.setParameterID(BIGoodNonReceiptReceivePortalParam.GrrMasterObj);
        paramGrrMstObj.setNetParameterValue(strGrrMstObj);
        bmObjSetData.params.add(paramGrrMstObj);

        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).CallBIModule(bmObjSetData, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (!((GoodNonReceiptReceiveDetailNewActivity)getActivity()).CheckBModuleReturnInfo(bModuleReturn))
                    return;

                Gson gson = new Gson();
                String sheetId = gson.fromJson(bModuleReturn.getReturnList().get("BISetNonReceiptReceiveData").get("PDASysKey").toString(), String.class);

                Object[] args = new Object[1];
                args[0] = sheetId;

                //WAPG009024    作業成功，收料單號[%s]
                ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009024, new ShowMessageEvent() {
                    @Override
                    public void onDismiss() {
                        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).gotoPreviousActivity(GoodNonReceiptReceiveNewActivity.class, true);
                    }
                }, args);

            }
        });

    }

    private boolean checkSN (){

        List<String> lsUnfulfillLot = new ArrayList<String>();

        //region 1.先檢查by SN收料是否有 refKey，沒有代表沒有收SN
        for (int i = 0; i < dtLotAll.Rows.size(); i++){
            if (dtLotAll.Rows.get(i).getValue("REGISTER_TYPE").toString().equals("PcsSN") &&
                    dtLotAll.Rows.get(i).getValue("GRR_DET_SN_REF_KEY").toString().length() <= 0){

                lsUnfulfillLot.add(dtLotAll.Rows.get(i).getValue("LOT_ID").toString());
            }
        }
        //endregion

        //region 2.有 refKey，確認是否收齊
        //取出有 refKey的 LotID，和該 LotID所需的SN個數
        DataTable dtLotSNQty = new DataTable();
        dtLotSNQty.addColumn(new DataColumn("LOT_ID"));
        dtLotSNQty.addColumn(new DataColumn("GRR_DET_SN_REF_KEY"));
        dtLotSNQty.addColumn(new DataColumn("QTY"));

        for(int i = 0; i < dtLotAll.Rows.size(); i++){
            if (dtLotAll.Rows.get(i).getValue("GRR_DET_SN_REF_KEY").toString().length() > 0){

                DataRow drNew = dtLotSNQty.newRow();
                drNew.setValue("LOT_ID", dtLotAll.Rows.get(i).getValue("LOT_ID").toString());
                drNew.setValue("GRR_DET_SN_REF_KEY", dtLotAll.Rows.get(i).getValue("GRR_DET_SN_REF_KEY").toString());
                drNew.setValue("QTY", dtLotAll.Rows.get(i).getValue("QTY").toString());
                dtLotSNQty.Rows.add(drNew);
            }
        }

        //計算所收的SN數量是否滿足需求量
        for (int i = 0; i < dtLotSNQty.Rows.size(); i++){

            //找出所收的SN數量
            int receiveQty = 0;
            String lotID = dtLotSNQty.Rows.get(i).getValue("LOT_ID").toString();
            String refKey = dtLotSNQty.Rows.get(i).getValue("GRR_DET_SN_REF_KEY").toString();
            String qty = dtLotSNQty.Rows.get(i).getValue("QTY").toString().split("\\.")[0];
            int needQty = Integer.parseInt(qty);
            for (int j = 0; j < dtSnAll.Rows.size(); j++){
                if (refKey == dtSnAll.Rows.get(j).getValue("GRR_DET_SN_REF_KEY").toString()){
                    receiveQty ++;
                }
            }

            //比對需求量和收的數量
            if (needQty > receiveQty){
                lsUnfulfillLot.add(lotID);
            }
        }
        //endregion

        if (lsUnfulfillLot.size() > 0){
            Object[] args = new Object[1];
            args[0] = TextUtils.join(", ", lsUnfulfillLot);
            ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009038, args); //WAPG009038    存貨編號[%s]序號尚未收滿 !
            return false;
        }
        return true;
    }

    private void createSheetID(final HashMap<String, String> mapStorageBinResult) {

        final String sheetTypeKey = dtReceiptMst.Rows.get(0).getValue("GR_TYPE_KEY").toString();
        if (sheetTypeKey.length() <= 0) {
            ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009001); //WAPG009001    請選擇單據類型
            return;
        }

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchGRSheetID");
        bmObj.setRequestID("BIFetchGRSheetID");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo parameterInfo = new ParameterInfo();
        parameterInfo.setParameterID(BIWMSFetchInfoParam.SysKeyCount);
        int iCount = 1;
        parameterInfo.setParameterValue(iCount);
        bmObj.params.add(parameterInfo);

        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(!((GoodNonReceiptReceiveDetailNewActivity)getActivity()).CheckBModuleReturnInfo(bModuleReturn)) return;

                Gson gson = new Gson();
                String sheetID = gson.fromJson(bModuleReturn.getReturnList().get("BIFetchGRSheetID").get("PDASysKey").toString(), String.class);

                if (sheetID.length() > 0) {
                    dtReceiptMst.Rows.get(0).setValue("GR_ID", sheetID);
                }

                receiveConfirm(mapStorageBinResult);
            }
        });
    }

    private void receiveConfirm(HashMap<String, String> mapStorageBinResult) {

        if(dtLotAll.Rows.size() <= 0) {
            ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009016);
            return;
        }
        if(!checkSN()) return;

        // region 儲存盤點狀態檢查物件
        // 20220805 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中
        List<CheckCountObj> lstChkCountObj = new ArrayList<>();
        for (DataRow dr : dtLotAll.Rows) {
            CheckCountObj chkCountObj = new CheckCountObj();
            chkCountObj.setStorageId(dr.getValue("STORAGE_ID").toString());
            chkCountObj.setItemId(dr.getValue("ITEM_ID").toString());
            chkCountObj.setBinId(dr.getValue("TEMP_BIN").toString());
            lstChkCountObj.add(chkCountObj);
        }
        // endregion

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BGoodReceiptReceiveNonSheet");
        bmObj.setModuleID("");
        bmObj.setRequestID("GRR");
        bmObj.params = new Vector<ParameterInfo>();

        BGoodReceiptReceiveNonSheetWithPackingInfoParam sheet = new BGoodReceiptReceiveNonSheetWithPackingInfoParam();
        BGoodReceiptReceiveNonSheetWithPackingInfoParam.GrNonSheetWithPackingInfoMasterObj sheetMstObj = sheet.new GrNonSheetWithPackingInfoMasterObj();

        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.INV.Parameter.GrMasterObj","bmWMS.INV.Param"); // BGoodReceiptReceiveEditParam.GrMasterObj
        MesClass mesClassEnum = new MesClass(vListEnum);
        String strGrrMstObj = mesClassEnum.generateFinalCode(sheetMstObj.GetGrrSheet(dtReceiptMst, dtLotAll, dtSnAll));

        VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create(VirtualClass.VirtualClassType.String);
        MesSerializableDictionary msd = new MesSerializableDictionary(vkey, vVal);
        String strTempBin = msd.generateFinalCode(mapStorageBinResult);

        VirtualClass vListEnum2 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.CheckCountObj", "bmWMS.Library.Param");
        MList mListEnum = new MList(vListEnum2);
        String strCheckCountObj = mListEnum.generateFinalCode(lstChkCountObj);

        ParameterInfo param = new ParameterInfo();
        param.setParameterID(BGoodReceiptReceiveNonSheetWithPackingInfoParam.GrMasterObj);
        param.setNetParameterValue(strGrrMstObj);
        bmObj.params.add(param);

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BGoodReceiptReceiveNonSheetWithPackingInfoParam.StorageInTempBin);
        param1.setNetParameterValue(strTempBin);
        bmObj.params.add(param1);

        ParameterInfo paramExecuteChkStock = new ParameterInfo();
        paramExecuteChkStock.setParameterID(BGoodReceiptReceiveNonSheetWithPackingInfoParam.ExecuteCheckStock); // 20220805 Add by Ikea 是否執行盤點檢查
        paramExecuteChkStock.setNetParameterValue2("true");
        bmObj.params.add(paramExecuteChkStock);

        ParameterInfo paramChkCountObj = new ParameterInfo(); // 20220805 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中
        paramChkCountObj.setParameterID(BGoodReceiptReceiveNonSheetWithPackingInfoParam.CheckCountObj);
        paramChkCountObj.setNetParameterValue(strCheckCountObj);
        bmObj.params.add(paramChkCountObj);

        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).CallBModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (!((GoodNonReceiptReceiveDetailNewActivity)getActivity()).CheckBModuleReturnInfo(bModuleReturn)) return;

                //WAPG009007    作業成功
                ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009007, new ShowMessageEvent() {
                    @Override
                    public void onDismiss() {
                        ((GoodNonReceiptReceiveDetailNewActivity)getActivity()).gotoPreviousActivity(GoodNonReceiptReceiveNewActivity.class, true);
                    }
                });
            }
        });
    }

    private DataTable createDataTableLotID() {
        DataTable dtLot = new DataTable();
        dtLot.addColumn(new DataColumn("ADD_MODE"));
        dtLot.addColumn(new DataColumn("SKU_LEVEL"));
        dtLot.addColumn(new DataColumn("SKU_NUM"));
        dtLot.addColumn(new DataColumn("STORAGE_KEY"));
        dtLot.addColumn(new DataColumn("STORAGE_ID"));
        dtLot.addColumn(new DataColumn("ITEM_KEY"));
        dtLot.addColumn(new DataColumn("ITEM_ID"));
        dtLot.addColumn(new DataColumn("ITEM_NAME"));
        dtLot.addColumn(new DataColumn("PO_NO"));
        dtLot.addColumn(new DataColumn("PO_SEQ"));
        dtLot.addColumn(new DataColumn("PO_SEQ_DISPLAY"));
        dtLot.addColumn(new DataColumn("LOT_ID"));
        dtLot.addColumn(new DataColumn("SPEC_LOT"));
        dtLot.addColumn(new DataColumn("QTY"));
        dtLot.addColumn(new DataColumn("UOM"));
        dtLot.addColumn(new DataColumn("CMT"));
        dtLot.addColumn(new DataColumn("MFG_DATE"));
        dtLot.addColumn(new DataColumn("EXP_DATE"));
        dtLot.addColumn(new DataColumn("SKIP_QC"));
        dtLot.addColumn(new DataColumn("GRR_DET_SN_REF_KEY"));
        dtLot.addColumn(new DataColumn("REGISTER_TYPE"));
        dtLot.addColumn(new DataColumn("TEMP_BIN"));
        dtLot.addColumn(new DataColumn("LOT_CODE"));
        dtLot.addColumn(new DataColumn("SIZE_KEY"));
        dtLot.addColumn(new DataColumn("VENDOR_ITEM_ID"));
        dtLot.addColumn(new DataColumn("REC_BARCODE"));
        dtLot.addColumn(new DataColumn("REC_QRCODE"));
        dtLot.addColumn(new DataColumn("BOX1_ID"));
        dtLot.addColumn(new DataColumn("BOX2_ID"));
        dtLot.addColumn(new DataColumn("BOX3_ID"));
        dtLot.addColumn(new DataColumn("PALLET_ID"));
        return dtLot;
    }

    private DataTable createDataTableSN() {
        DataTable dtSN = new DataTable();
        dtSN.addColumn(new DataColumn("GRR_DET_SN_REF_KEY"));
        dtSN.addColumn(new DataColumn("SN_ID"));
        dtSN.addColumn(new DataColumn("LOT_POS"));
        return dtSN;
    }

    // endregion

    // region -- Interface --

    public interface IModifyNonReceiptReceiveDataTable {

        //public void modifyDrLotData(DataRow drLot, DataTable dtSn, String mode, int pos);

        public void modifyDrLotData(DataRow drLotWithSkuLevel, DataTable dtLot, DataTable dtSn, String mode, int pos);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            iModifyNonReceiptReceiveDataTable = (IModifyNonReceiptReceiveDataTable) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Plz implement interface method");
        }
    }

//    public void getLotTable(DataRow drLot, DataTable dtSn, String mode, int pos) {
//
//        switch (mode) {
//
//            case "Add":
//                dtLotAll.Rows.add(drLot);
//                if (dtSn != null && dtSn.Rows.size() > 0) {
//                    for (DataRow drClone : dtSn.Rows) {
//                        DataRow drSnNew = dtSnAll.newRow();
//                        drSnNew.setValue("GRR_DET_SN_REF_KEY", drClone.getValue("GRR_DET_SN_REF_KEY").toString());
//                        drSnNew.setValue("SN_ID", drClone.getValue("SN_ID").toString());
//                        drSnNew.setValue("LOT_POS", dtLotAll.Rows.size()-1);
//                        dtSnAll.Rows.add(drSnNew);
//                    }
//                }
//                break;
//
//            case "Modify":
//                DataRow drModifyLot = dtLotAll.Rows.get(pos);
//                drModifyLot.setValue("STORAGE_KEY", drLot.getValue("STORAGE_KEY"));
//                drModifyLot.setValue("STORAGE_ID", drLot.getValue("STORAGE_ID"));
//                drModifyLot.setValue("ITEM_KEY", drLot.getValue("ITEM_KEY"));
//                drModifyLot.setValue("ITEM_ID", drLot.getValue("ITEM_ID"));
//                drModifyLot.setValue("ITEM_NAME", drLot.getValue("ITEM_NAME"));
//                drModifyLot.setValue("PO_NO", drLot.getValue("PO_NO"));
//                drModifyLot.setValue("PO_SEQ", drLot.getValue("PO_SEQ"));
//                drModifyLot.setValue("LOT_ID", drLot.getValue("LOT_ID"));
//                drModifyLot.setValue("QTY", drLot.getValue("QTY"));
//                drModifyLot.setValue("UOM", drLot.getValue("UOM"));
//                drModifyLot.setValue("CMT", drLot.getValue("CMT"));
//                drModifyLot.setValue("MFG_DATE", drLot.getValue("MFG_DATE"));
//                drModifyLot.setValue("EXP_DATE", drLot.getValue("EXP_DATE"));
//                drModifyLot.setValue("SKIP_QC", drLot.getValue("SKIP_QC"));
//                drModifyLot.setValue("REGISTER_TYPE", drLot.getValue("REGISTER_TYPE"));
//                drModifyLot.setValue("GRR_DET_SN_REF_KEY", drLot.getValue("GRR_DET_SN_REF_KEY"));
//                drModifyLot.setValue("LOT_CODE", drLot.getValue("LOT_CODE"));
//                drModifyLot.setValue("SIZE_KEY", drLot.getValue("SIZE_KEY"));
//                drModifyLot.setValue("SIZE_ID", drLot.getValue("SIZE_ID"));
//                drModifyLot.setValue("VENDOR_ITEM_ID", drLot.getValue("VENDOR_ITEM_ID"));
//                drModifyLot.setValue("SPEC_LOT", drLot.getValue("SPEC_LOT"));
//                drModifyLot.setValue("REC_QRCODE", drLot.getValue("REC_QRCODE"));
//                drModifyLot.setValue("REC_BARCODE", drLot.getValue("REC_BARCODE"));
//                drModifyLot.setValue("BOX1_ID", drLot.getValue("BOX1_ID"));
//                drModifyLot.setValue("BOX2_ID", drLot.getValue("BOX2_ID"));
//                drModifyLot.setValue("BOX3_ID", drLot.getValue("BOX3_ID"));
//                drModifyLot.setValue("PALLET_ID", drLot.getValue("PALLET_ID"));
//
//                for (Iterator<DataRow> iterator = dtSnAll.Rows.iterator(); iterator.hasNext();) {
//                    DataRow drFind = iterator.next();
//                    if (drFind.getValue("LOT_POS").equals(pos)) {
//                        iterator.remove();
//                    }
//                }
//
//                if (dtSn != null && dtSn.Rows.size() > 0) {
//
//                    for (DataRow drClone : dtSn.Rows) {
//                        DataRow drSnNew = dtSnAll.newRow();
//                        drSnNew.setValue("GRR_DET_SN_REF_KEY", drClone.getValue("GRR_DET_SN_REF_KEY").toString());
//                        drSnNew.setValue("SN_ID", drClone.getValue("SN_ID").toString());
//                        drSnNew.setValue("LOT_POS", pos);
//                        dtSnAll.Rows.add(drSnNew);
//                    }
//                }
//
//                break;
//
//            default:
//                break;
//        }
//
//        LayoutInflater inflater = LayoutInflater.from(getContext());
//        GoodNonreceiptReceiveGridAdapter adapter = new GoodNonreceiptReceiveGridAdapter(dtLotAll, inflater);
//        lvReceiveSheet.setAdapter(adapter);
//        adapter.notifyDataSetChanged();
//    }

    public void getLotTable(DataTable dtLotWithSkuLevel, DataTable dtLot, DataTable dtSn, String mode, int pos) {

        switch (mode) {

            case "Add":

                dtLotWithSkuLevelAll.Rows.add(dtLotWithSkuLevel.Rows.get(0));

                for (DataRow drLot : dtLot.Rows) {
                    dtLotAll.Rows.add(drLot);
                }

                if (dtSn != null && dtSn.Rows.size() > 0) {
                    for (DataRow drClone : dtSn.Rows) {
                        DataRow drSnNew = dtSnAll.newRow();
                        drSnNew.setValue("GRR_DET_SN_REF_KEY", drClone.getValue("GRR_DET_SN_REF_KEY").toString());
                        drSnNew.setValue("SN_ID", drClone.getValue("SN_ID").toString());
                        drSnNew.setValue("LOT_POS", dtLotAll.Rows.size()-1);
                        dtSnAll.Rows.add(drSnNew);
                    }
                }
                break;

            case "Modify":

                DataRow drModifyLot = dtLotWithSkuLevelAll.Rows.get(pos);
                String modifySkuLevel = drModifyLot.getValue("SKU_LEVEL").toString();
                String modifySkuNum = drModifyLot.getValue("SKU_NUM").toString();
                List<String> lstRefKey = new ArrayList<>();

                // region 先記錄 GRR_DET_SN_REF_KEY 資訊，並刪除實際資料

                for (Iterator<DataRow> iterator = dtLotAll.Rows.iterator(); iterator.hasNext();) {
                    DataRow drFindLot = iterator.next();
                    if (drFindLot.getValue("SKU_LEVEL").equals(modifySkuLevel) &&
                        drFindLot.getValue("SKU_NUM").equals(modifySkuNum)) {

                        if (!drFindLot.getValue("GRR_DET_SN_REF_KEY").toString().equals("")) {

                            String ref = drFindLot.getValue("GRR_DET_SN_REF_KEY").toString();

                            if (!lstRefKey.contains(ref))
                                lstRefKey.add(ref);

                        }

                        iterator.remove();
                    }
                }
                // endregion

                // region 後刪除顯示資料

                dtLotWithSkuLevelAll.Rows.remove(pos);

                // endregion

                // region 再刪除 SN 資料

                if (dtSnAll.Rows.size() > 0) {

                    for (int i = dtSn.Rows.size() - 1; i >= 0; i--) {

                        String refKey = dtSn.Rows.get(i).get("GRR_DET_SN_REF_KEY").toString();

                        for (Iterator<DataRow> iterator = dtSnAll.Rows.iterator(); iterator.hasNext();) {
                            DataRow drFindSn = iterator.next();
                            if (drFindSn.getValue("GRR_DET_SN_REF_KEY").equals(refKey)) {
                                iterator.remove();
                            }
                        }

                    }

                }

                // endregion

                // region 新增顯示資料

                dtLotWithSkuLevelAll.Rows.add(dtLotWithSkuLevel.Rows.get(0));

                // endregion

                // region 新增實際資料

                for (DataRow dr : dtLot.Rows) {

                    dtLotAll.Rows.add(dr);

                }

                // endregion

                // region 新增序號

                for (DataRow dr : dtSn.Rows) {
                    dtSnAll.Rows.add(dr);
                }

                // endregion
//                drModifyLot.setValue("STORAGE_KEY", drLot.getValue("STORAGE_KEY"));
//                drModifyLot.setValue("STORAGE_ID", drLot.getValue("STORAGE_ID"));
//                drModifyLot.setValue("ITEM_KEY", drLot.getValue("ITEM_KEY"));
//                drModifyLot.setValue("ITEM_ID", drLot.getValue("ITEM_ID"));
//                drModifyLot.setValue("ITEM_NAME", drLot.getValue("ITEM_NAME"));
//                drModifyLot.setValue("PO_NO", drLot.getValue("PO_NO"));
//                drModifyLot.setValue("PO_SEQ", drLot.getValue("PO_SEQ"));
//                drModifyLot.setValue("LOT_ID", drLot.getValue("LOT_ID"));
//                drModifyLot.setValue("QTY", drLot.getValue("QTY"));
//                drModifyLot.setValue("UOM", drLot.getValue("UOM"));
//                drModifyLot.setValue("CMT", drLot.getValue("CMT"));
//                drModifyLot.setValue("MFG_DATE", drLot.getValue("MFG_DATE"));
//                drModifyLot.setValue("EXP_DATE", drLot.getValue("EXP_DATE"));
//                drModifyLot.setValue("SKIP_QC", drLot.getValue("SKIP_QC"));
//                drModifyLot.setValue("REGISTER_TYPE", drLot.getValue("REGISTER_TYPE"));
//                drModifyLot.setValue("GRR_DET_SN_REF_KEY", drLot.getValue("GRR_DET_SN_REF_KEY"));
//                drModifyLot.setValue("LOT_CODE", drLot.getValue("LOT_CODE"));
//                drModifyLot.setValue("SIZE_KEY", drLot.getValue("SIZE_KEY"));
//                drModifyLot.setValue("SIZE_ID", drLot.getValue("SIZE_ID"));
//                drModifyLot.setValue("VENDOR_ITEM_ID", drLot.getValue("VENDOR_ITEM_ID"));
//                drModifyLot.setValue("SPEC_LOG", drLot.getValue("SPEC_LOG"));
//                drModifyLot.setValue("REC_QRCODE", drLot.getValue("REC_QRCODE"));
//                drModifyLot.setValue("REC_BARCODE", drLot.getValue("REC_BARCODE"));
//
//                for (Iterator<DataRow> iterator = dtSnAll.Rows.iterator(); iterator.hasNext();) {
//                    DataRow drFind = iterator.next();
//                    if (drFind.getValue("LOT_POS").equals(pos)) {
//                        iterator.remove();
//                    }
//                }
//
//                if (dtSn != null && dtSn.Rows.size() > 0) {
//
//                    for (DataRow drClone : dtSn.Rows) {
//                        DataRow drSnNew = dtSnAll.newRow();
//                        drSnNew.setValue("GRR_DET_SN_REF_KEY", drClone.getValue("GRR_DET_SN_REF_KEY").toString());
//                        drSnNew.setValue("SN_ID", drClone.getValue("SN_ID").toString());
//                        drSnNew.setValue("LOT_POS", pos);
//                        dtSnAll.Rows.add(drSnNew);
//                    }
//                }

                break;

            default:
                break;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        GoodNonreceiptReceiveGridAdapter adapter = new GoodNonreceiptReceiveGridAdapter(dtLotWithSkuLevelAll, inflater);
        lvReceiveSheet.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    // endregion
}
