package com.delta.android.WMS.Client;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.Activity.ShowMessageEvent;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.GenerateJsonNetString.MList;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionary;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionaryList;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Param.BGoodStockOutParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.BStockOutBaseParam;
import com.delta.android.WMS.Param.ParamObj.CheckCountObj;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoodStockOutDetailActivity extends BaseFlowActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_good_stock_out_detail);

        // Get Detail table From GoodStockOutActivity.activity
        dtDetail = (DataTable) getIntent().getSerializableExtra("DataTable");
        dtShtDetail = (DataTable) getIntent().getSerializableExtra("Sht");
        Bundle bundle = getIntent().getExtras();
        ID = bundle.getString("SHEET_ID");
        SourceID = bundle.getString("SOURCE_SHEET_ID");
        strActualQtyStatus = bundle.getString("CFG");
        strShtTypePolicyId = bundle.getString("SHEET_TYPE");

        // 取得控制項物件
        initViews();
        // 設定監聽事件
        setListensers();
    }

    // private variable
    DataTable dtDetail = new DataTable();
    DataTable dtShtDetail = new DataTable();
    String ID = "";
    String SourceID = "";
    String strActualQtyStatus = "";
    String strShtTypePolicyId = "";
    String strTargetStorage = "";
    HashMap<String, String> dicTempBin = new HashMap<>();

    // 宣告控制項物件
    private ListView lstDetail;
    private Button btnOk;

    //取得控制項物件
    private void initViews()
    {
        lstDetail= findViewById(R.id.listViewDet);
        btnOk = findViewById(R.id.GoodStockOut_btnOk);
        // 取得物件後才塞入Detail
        GetDetail();
    }

    //設定監聽事件
    private void setListensers()
    {
        btnOk.setOnClickListener(OK);
    }

    // region 事件

    private View.OnClickListener OK = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            //region 檢查 ACTUAL_QTY_STATUS  實際數量的狀態
            for(DataRow dr : dtShtDetail.Rows){
                double sum = 0;
                for(DataRow drPick : dtDetail.Rows){
                    // 只看已揀貨
                    if(drPick.getValue("IS_PICKED").toString().equals("Y") &&
                            drPick.getValue("SEQ").toString().equals(dr.getValue("SEQ").toString())){
                        double qty = Double.parseDouble(drPick.getValue("QTY").toString());
                        sum += qty;
                    }
                }
                double detQty = Double.parseDouble(dr.getValue("TRX_QTY").toString());

                if (sum == 0)
                {
                    Object[] objs = new Object[2];
                    objs[0] = SourceID;
                    objs[1] = dr.getValue("SEQ").toString();

                    //WAPG005008 單據[%s],項次[%s]沒有揀料
                    ShowMessage(R.string.WAPG005008, objs);
                    return;
                }

                switch (strActualQtyStatus){
                    case "More":
                        if(sum < detQty){
                            Object[] objs = new Object[4];
                            objs[0] = SourceID;
                            objs[1] = dr.getValue("SEQ").toString();
                            objs[2] = sum;
                            objs[3] = detQty;

                            //WAPG005009 單據[%s],項次[%s],出庫數量[%s]不可小於單據數量[%s]
                            ShowMessage(R.string.WAPG005009, objs);
                            return;
                        }
                        break;
                    case "Less":
                        if(sum > detQty){
                            Object[] objs = new Object[4];
                            objs[0] = SourceID;
                            objs[1] = dr.getValue("SEQ").toString();
                            objs[2] = sum;
                            objs[3] = detQty;

                            //WAPG005010 單據[%s],項次[%s],出庫數量[%s]不可大於單據數量[%s]
                            ShowMessage(R.string.WAPG005010, objs);
                            return;
                        }

                        break;
                    case "Equal":
                        if(sum != detQty){
                            Object[] objs = new Object[4];
                            objs[0] = SourceID;
                            objs[1] = dr.getValue("SEQ").toString();
                            objs[2] = sum;
                            objs[3] = detQty;

                            //WAPG005011 單據[%s],項次[%s],出庫數量[%s]必須等於單據數量[%s]
                            ShowMessage(R.string.WAPG005011, objs);
                            return;
                        }
                        break;
                }
            }
            //endregion

            BModuleObject bimObj = new BModuleObject();
            bimObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
            bimObj.setModuleID("BIFetchBin");
            bimObj.setRequestID("BIFetchBin");
            bimObj.params = new Vector<ParameterInfo>();

            ParameterInfo param1 = new ParameterInfo();
            param1.setParameterID(BIWMSFetchInfoParam.Filter);
            //param1.setParameterValue(String.format(" AND STORAGE_ID = '%s' AND B.BIN_TYPE ='OS'", dtDetail.Rows.get(0).getValue("STORAGE_ID").toString()));
            if(!strShtTypePolicyId.equals("Transfer"))
                param1.setParameterValue(String.format(" AND STORAGE_ID = '%s' AND B.BIN_TYPE IN ('OT', 'OS')", dtDetail.Rows.get(0).getValue("STORAGE_ID").toString()));
            else
                param1.setParameterValue(String.format(" AND STORAGE_ID = '%s' AND B.BIN_TYPE IN ('IT', 'IS')", dtShtDetail.Rows.get(0).getValue("TO_STORAGE_ID").toString()));

            bimObj.params.add(param1);

            CallBIModule(bimObj, new WebAPIClientEvent() {
                @Override
                public void onPostBack(BModuleReturn bModuleReturn) {
                    if(CheckBModuleReturnInfo(bModuleReturn)){
                        DataTable dtBin = bModuleReturn.getReturnJsonTables().get("BIFetchBin").get("BIN");

                        ArrayList<String> alBin = new ArrayList<String>();
                        if(strShtTypePolicyId.equals("Transfer"))
                            strTargetStorage = dtShtDetail.Rows.get(0).getValue("TO_STORAGE_ID").toString();
                        else
                            strTargetStorage = dtDetail.Rows.get(0).getValue("STORAGE_ID").toString();
                        //for(int i = 0; i < dtBin.Rows.size(); i++)
                        //{
                        //    String binId = dtBin.Rows.get(i).getValue("BIN_ID").toString();
                        //    if(!alBin.contains(binId))
                        //        alBin.add(binId);
                        //}

                        for(int i = 0; i < dtBin.Rows.size(); i++)
                        {
                            String binType = dtBin.Rows.get(i).getValue("BIN_TYPE").toString();
                            String binId = dtBin.Rows.get(i).getValue("BIN_ID").toString();
                            if(strShtTypePolicyId.equals("Transfer"))
                            {
                                if(binType.equals("IT"))
                                {
                                    alBin.add(binId);
                                    break;
                                }
                            }
                            else
                            {
                                if(binType.equals("OT"))
                                {
                                    alBin.add(binId);
                                    break;
                                }
                            }
                        }

                        if(alBin.size() == 0)
                        {
                            for(int i = 0; i < dtBin.Rows.size(); i++)
                            {
                                String binType = dtBin.Rows.get(i).getValue("BIN_TYPE").toString();
                                String binId = dtBin.Rows.get(i).getValue("BIN_ID").toString();
                                if(strShtTypePolicyId.equals("Transfer"))
                                {
                                    if(binType.equals("IS"))
                                    {
                                        if(!alBin.contains(binId))
                                            alBin.add(binId);
                                    }
                                }
                                else
                                {
                                    if(binType.equals("OS"))
                                    {
                                        if(!alBin.contains(binId))
                                            alBin.add(binId);
                                    }
                                }
                            }
                        }

                        //ShowMessage("單據編號: "+"是否確定出庫?");
                        ShowDialog("單據編號: "+SourceID+", 目的倉庫: " + strTargetStorage + ", 是否確定出庫?", alBin);
                    }
                }
            });
        }
    };

    // endregion

    // region Private Function

    private void GetDetail()
    {
        List<HashMap<String , String>> list = new ArrayList<>();
        ArrayList<String> listItemId = new ArrayList<String>();
        ArrayList<String> listItemName = new ArrayList<String>();
        ArrayList<String> listLotId = new ArrayList<String>();
        ArrayList<String> listStorageId = new ArrayList<String>();
        ArrayList<String> listBinId = new ArrayList<String>();
        ArrayList<String> listQty = new ArrayList<String>();
        ArrayList<String> listUom = new ArrayList<String>();
        ArrayList<String> listCmt = new ArrayList<String>();
        Iterator it =  dtDetail.Rows.iterator();
        int i = 0;
        while (it.hasNext())
        {
            DataRow row = (DataRow) it.next();
            if (row.getValue("IS_PICKED").toString().equals("N")) // 不顯示已預約但未揀貨的項目
                continue;
            listItemId.add( i,row.getValue("ITEM_ID").toString());
            listItemName.add( i,row.getValue("ITEM_NAME").toString());
            listLotId.add( i,row.getValue("LOT_ID").toString());
            listStorageId.add( i,row.getValue("STORAGE_ID").toString());
            listBinId.add( i,row.getValue("BIN_ID").toString());
            listQty.add( i,row.getValue("QTY").toString());
            listUom.add( i,row.getValue("UOM").toString());
            listCmt.add( i,row.getValue("CMT").toString());
            i++;
        }
        for (int j =0; j<listItemId.size();j++)
        {
            HashMap<String , String> hashMap = new HashMap<>();
            hashMap.put("ITEM_ID" , listItemId.get(j));
            hashMap.put("ITEM_NAME" , listItemName.get(j));
            hashMap.put("LOT_ID" , listLotId.get(j));
            hashMap.put("STORAGE_ID" , listStorageId.get(j));
            hashMap.put("BIN_ID" , listBinId.get(j));
            hashMap.put("QTY" , listQty.get(j));
            hashMap.put("UOM" , listUom.get(j));
            hashMap.put("CMT" , listCmt.get(j));
            //把title , text存入HashMap之中
            list.add(hashMap);
        }

        ListAdapter adapter = new SimpleAdapter(
                GoodStockOutDetailActivity.this,
                list,
                R.layout.activity_wms_good_stock_out_detail_listview,
                new String[]{"ITEM_ID","ITEM_NAME","LOT_ID","BIN_ID","QTY","UOM"},
                new int[]{R.id.tvItemId,R.id.tvItemName,R.id.tvLotId,R.id.tvBinId,R.id.tvQty,R.id.tvUom}
        );
        lstDetail.setAdapter(adapter);
    }

    private void ShowDialog(String Message, ArrayList<String> alBin)
    {
        //LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        LayoutInflater inflater = LayoutInflater.from(GoodStockOutDetailActivity.this);
        //View view = inflater.inflate(R.layout.style_core_dialog_exit, null);
        View view = inflater.inflate(R.layout.style_wms_dialog_stock_out, null);
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);
        final Spinner cmbBin = view.findViewById(R.id.cmbTempBin);
        tvMessage.setText(Message);

        ArrayAdapter<String> adapterBin = new ArrayAdapter<>(GoodStockOutDetailActivity.this, android.R.layout.simple_spinner_dropdown_item, alBin);
        cmbBin.setAdapter(adapterBin);

        final AlertDialog.Builder builder = new AlertDialog.Builder(GoodStockOutDetailActivity.this);
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
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cmbBin.getSelectedItem().toString().equals("")) {
                    ShowMessage("請選擇目的儲位");
                }
                dialog.dismiss();
                //Toast.makeText(GoodStockOutDetailActivity.this,"Done~~~", Toast.LENGTH_LONG).show();
                dicTempBin.put(strTargetStorage, cmbBin.getSelectedItem().toString());
                ExecuteProcess("GoodStockOut", cmbBin.getSelectedItem().toString());
            }
        });
    }

    private void ExecuteProcess(String trxType, String tempBin)
    {
        // region 儲存盤點狀態檢查物件
        // 20220805 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中
        List<CheckCountObj> lstChkCountObj = new ArrayList<>();
        for (DataRow dr : dtDetail.Rows) {
            CheckCountObj chkCountObjFromBin = new CheckCountObj(); // FROM_BIN
            chkCountObjFromBin.setStorageId(dr.getValue("STORAGE_ID").toString());
            chkCountObjFromBin.setItemId(dr.getValue("ITEM_ID").toString());
            chkCountObjFromBin.setBinId(dr.getValue("BIN_ID").toString());
            lstChkCountObj.add(chkCountObjFromBin);
            CheckCountObj chkCountObjToBin = new CheckCountObj(); // TO_BIN
            chkCountObjToBin.setStorageId(dr.getValue("STORAGE_ID").toString());
            chkCountObjToBin.setItemId(dr.getValue("ITEM_ID").toString());
            chkCountObjToBin.setBinId(tempBin);
            lstChkCountObj.add(chkCountObjToBin);
        }
        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.CheckCountObj", "bmWMS.Library.Param");
        MList mListEnum = new MList(vListEnum);
        String strCheckCountObj = mListEnum.generateFinalCode(lstChkCountObj);
        // endregion

        // Call BModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BGoodStockOut");
        bmObj.setModuleID("");
        bmObj.setRequestID("GoodStockOut");
        bmObj.params = new Vector<ParameterInfo>();
        // Add param
        ParameterInfo paramTrxType = new ParameterInfo();
        paramTrxType.setParameterID(BGoodStockOutParam.TrxType);
        paramTrxType.setParameterValue(trxType);
        ParameterInfo paramSheetIds = new ParameterInfo();
        paramSheetIds.setParameterID(BStockOutBaseParam.CheckStockOutBySheetIds); //20220829 archie 將可共用的Param改為出庫Base的Param
        paramSheetIds.setParameterValue(ID);
        ParameterInfo paramMode = new ParameterInfo();
        paramMode.setParameterID(BStockOutBaseParam.Mode);
        paramMode.setParameterValue("StockOut");
        ParameterInfo paramBin = new ParameterInfo();
        paramBin.setParameterID(BGoodStockOutParam.TempBin);
        //paramBin.setParameterValue(tempBin);

        VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create(VirtualClass.VirtualClassType.String);

        MesSerializableDictionary msd = new MesSerializableDictionary(vKey, vVal);
        String serializedString = msd.generateFinalCode(dicTempBin);
        paramBin.setNetParameterValue(serializedString);

        ParameterInfo paramExecuteChkStock = new ParameterInfo();
        paramExecuteChkStock.setParameterID(BGoodStockOutParam.ExecuteCheckStock); // 20220805 Add by Ikea 是否執行盤點檢查
        paramExecuteChkStock.setNetParameterValue2("true");
        ParameterInfo paramChkCountObj = new ParameterInfo(); // 20220805 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中
        paramChkCountObj.setParameterID(BGoodStockOutParam.CheckCountObj);
        paramChkCountObj.setNetParameterValue(strCheckCountObj);

        bmObj.params.add(paramTrxType);
        bmObj.params.add(paramMode);
        bmObj.params.add(paramSheetIds);
        bmObj.params.add(paramBin);
        bmObj.params.add(paramExecuteChkStock);
        bmObj.params.add(paramChkCountObj);

        CallBModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    //ExecuteSheetReceive("SheetReceive");->BModule整合 call一次就好

                    String holdItemId = "";
                    String holdMessage = "";
                    String hintMessage = "";

                    if (bModuleReturn.getReturnList().get("GoodStockOut") != null) {

                        if (bModuleReturn.getReturnList().get("GoodStockOut").get(BGoodStockOutParam.HoldItemID) != null) {
                            holdItemId = bModuleReturn.getReturnList().get("GoodStockOut").get(BGoodStockOutParam.HoldItemID).toString().replaceAll("\"", "");
                        }

                        if (bModuleReturn.getReturnList().get("GoodStockOut").get(BGoodStockOutParam.HoldMessage) != null) {
                            holdMessage = bModuleReturn.getReturnList().get("GoodStockOut").get(BGoodStockOutParam.HoldMessage).toString().replaceAll("\"", "");
                            holdMessage = split(holdMessage);
                        }

                        if (bModuleReturn.getReturnList().get("GoodStockOut").get(BGoodStockOutParam.HintMessage) != null) {
                            hintMessage = bModuleReturn.getReturnList().get("GoodStockOut").get(BGoodStockOutParam.HintMessage).toString().replaceAll("\"", "");
                            hintMessage = split(hintMessage);
                        }
                    }

                    String showExtraMsg = "";

                    if (hintMessage.length() > 0) {
                        showExtraMsg += getResString(getResources().getString(R.string.ITEM_SPEC_CTRL_HINT_MSG)) + System.getProperty("line.separator") ;
                        showExtraMsg += hintMessage + System.getProperty("line.separator");
                    }

                    if (holdMessage.length() > 0) {
                        showExtraMsg += getResString(getResources().getString(R.string.ITEM_SPEC_CTRL_HOLD_MSG)) + System.getProperty("line.separator");
                        showExtraMsg += holdMessage + System.getProperty("line.separator");
                    }

                    if (holdItemId.length() > 0) { // 有被鎖定的物料代碼 => 揀貨失敗, 有額外資訊需顯示

                        Object[] args = new Object[1];
                        args[0] = holdItemId;

                        final String finalShowExtraMsg = showExtraMsg;
                        // 出庫失敗
                        ShowMessage(R.string.WAPG005013, new ShowMessageEvent() {
                            @Override
                            public void onDismiss() {

                                if (finalShowExtraMsg.length() > 0) {

                                    // 超規資訊
                                    ShowMessage(finalShowExtraMsg, new ShowMessageEvent() {
                                        @Override
                                        public void onDismiss() {
                                            gotoPreviousActivity(GoodStockOutActivity.class, true);
                                        }
                                    });

                                }

                            }
                        }, args);

                    } else { // 出庫成功. 但有額外資訊需顯示

                        final String finalShowExtraMsg = showExtraMsg;
                        if (finalShowExtraMsg.length() > 0) {

                            // 出庫成功
                            ShowMessage(R.string.WAPG005007, new ShowMessageEvent() {
                                @Override
                                public void onDismiss() {
                                    // 超規資訊
                                    ShowMessage(finalShowExtraMsg, new ShowMessageEvent() {
                                        @Override
                                        public void onDismiss() {
                                            gotoPreviousActivity(GoodStockOutActivity.class, true);
                                        }
                                    });

                                }
                            });

                        } else {

                            // 出庫成功
                            ShowMessage(R.string.WAPG005007, new ShowMessageEvent() {
                                @Override
                                public void onDismiss() {

                                    gotoPreviousActivity(GoodStockOutActivity.class, true);

                                }
                            });
                        }
                    }

                    //ShowMessage(R.string.WAPG005007);
                    //gotoPreviousActivity(GoodStockOutActivity.class, true);
                }
            }
        });
    }

    private void ExecuteSheetReceive(String trxType)
    {
        // Call BModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BGoodStockOut");
        bmObj.setModuleID("");
        bmObj.setRequestID("SheetReceive");
        bmObj.params = new Vector<ParameterInfo>();
        // Add param
        ParameterInfo paramTrxType = new ParameterInfo();
        paramTrxType.setParameterID(BGoodStockOutParam.TrxType);
        paramTrxType.setParameterValue(trxType);
        ParameterInfo paramSheetIds = new ParameterInfo();
        paramSheetIds.setParameterID(BGoodStockOutParam.SheetIds);
        paramSheetIds.setParameterValue(ID);
        bmObj.params.add(paramTrxType);
        bmObj.params.add(paramSheetIds);

        CallBModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (!CheckBModuleReturnInfo(bModuleReturn)) {
                    ShowMessage("未將實際收的料填入入庫表", new ShowMessageEvent() {
                        @Override
                        public void onDismiss() {
                            gotoPreviousActivity(GoodStockOutActivity.class);
                        }
                    });

                }
            }
        });
    }

    // endregion

    // 將字串移除\r\n後，重新加入new line
    private String split(String str) {

        String result = "";

        if (str != null && str.length() > 0) {

            String delim = "\\\\r\\\\n";
            String splitArray[] = str.split(delim);
            for (int i = 0; i < splitArray.length; i++) {
                result += splitArray[i] + System.getProperty("line.separator");
            }

        }

        return result;
    }

}
