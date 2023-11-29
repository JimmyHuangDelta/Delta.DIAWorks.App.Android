package com.delta.android.WMS.Client;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.Activity.ScanActivity;
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
import com.delta.android.WMS.Client.GridAdapter.SheetDetPickGridAdapter;
import com.delta.android.WMS.Param.BGoodReservationParam;
import com.delta.android.WMS.Param.BIFetchPickStrategyParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.BIWMSPickByLightParam;
import com.delta.android.WMS.Param.BStockOutBaseParam;
import com.delta.android.WMS.Param.ParamObj.CheckCountObj;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.delta.android.WMS.Param.ParamObj.PickDetObj;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.function.Function;

public class GoodPickExecutedActivity extends BaseFlowActivity {

    //DataTable pickedData;
    List<RegisterObj> registerData;
    DataTable sheetDetData;
    DataTable alreadyPickedData; // 含預約揀貨及已揀貨
    DataTable rsvPickedData; // 預約揀貨
    DataTable regData;
    boolean isConfirm;
    double pickedCount;
    double totalPickedCount;
    ViewHolder holder;
    private HashMap<String,String> ActualQtyStatus;
    //private HashMap<String,String> PickStrategy;
    private DataTable dtConfigCond;
    private DataTable dtConfigSort;
    private DataTable dtConfig;

    private final int REGISTER_ID_QRSCAN_REQUEST_CODE = 22; // 相機掃描回傳資訊使用
    private final int BIN_ID_QRSCAN_REQUEST_CODE = 33; // 相機掃描回傳資訊使用

    private String strIsRecommend = "true";

    static class ViewHolder {
        EditText RegisterId;
        EditText BinId;
        TextView ItemId;
        TextView SheetId;
        TextView SheetDetPickQty;
        TextView Seq;
        ListView RegisterData;
        TextView PickSheetId;
        TextView tvColor;
        CheckBox chkUseRec;
    }

    public class RegisterObj{
        public String RegisterId;
        public String BinId;
        public String StorageId;
        public String BatchId;
        public String BatchPosition;
        public float Qty; // Reg庫存可揀
        public float ReserveQty; // Reg已被預揀
        public float NeedToPickQty; // Pick已被預揀
        public String ItemUom;
        public boolean Selected;
        public boolean IsReserved;
        public String RegisterType; // Item
        public String MfgDate;
        public String ExpDate;

        public String getRegisterId() {
            return RegisterId;
        }
        public String getQty() { return String.valueOf(Qty); }
        public String getNeedToPickQty() { return String.valueOf(NeedToPickQty); }
        public String getReserveQty() { return String.valueOf(ReserveQty); }
        public String getMfgDate() { return MfgDate; }
        public String getExpDate() { return ExpDate; }
    }

    public static class RegMultiComparator implements Comparator<RegisterObj> {
        protected List<String> fields;

        public RegMultiComparator(DataTable orderedFields) {
            fields = new ArrayList<String>();
            for (DataRow dr : orderedFields.Rows) {
                fields.add(dr.getValue("REG_FIELD").toString() + "-" + dr.getValue("SORT_METHOD").toString());
            }
        }

        @Override
        public int compare(RegisterObj reg1, RegisterObj reg2) {
            Integer score = 0;
            Boolean continueComparison = true;
            Iterator itFields = fields.iterator();

            while (itFields.hasNext() && continueComparison) {
                String field = itFields.next().toString();
                String[] spliter = field.split("-");
                String fieldName = spliter[0];
                String sortMethod = spliter[1];
                Integer currentScore = 0;

                if (fieldName.equalsIgnoreCase("REGISTER_ID")) {
                    currentScore = reg1.getRegisterId().compareTo(reg2.getRegisterId());
                    if (sortMethod.equals("DESC"))
                        currentScore *= (-1);
                } else if (fieldName.equalsIgnoreCase("QTY")) {
                    currentScore = reg1.getQty().compareTo(reg2.getQty());
                    if (sortMethod.equals("DESC"))
                        currentScore *= (-1);
                } else if (fieldName.equalsIgnoreCase("MFG_DATE")) {
                    currentScore = reg1.getMfgDate().compareTo(reg2.getMfgDate());
                    if (sortMethod.equals("DESC"))
                        currentScore *= (-1);
                } else if (fieldName.equalsIgnoreCase("EXP_DATE")) {
                    currentScore = reg1.getExpDate().compareTo(reg2.getExpDate());
                    if (sortMethod.equals("DESC"))
                        currentScore *= (-1);
                }
                if (currentScore != 0) {
                    continueComparison = false;
                }
                score = currentScore;
            }

            return score;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_good_pick_executed);

        this.initialData();
    }

    @Override
    public void onBackPressed() {
        //如果要回傳則需要寫此方法
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, getResources().getText(R.string.QRCODE_SCAN_CANCEL).toString(), Toast.LENGTH_SHORT).show();
            } else {
                switch (requestCode) {
                    case REGISTER_ID_QRSCAN_REQUEST_CODE:
                        holder.RegisterId.setText(result.getContents().trim());
                        break;
                    case BIN_ID_QRSCAN_REQUEST_CODE:
                        holder.BinId.setText(result.getContents().trim());
                        break;
                    default:
                        break;
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initialData() {

        sheetDetData = (DataTable) getIntent().getSerializableExtra("DetTable");
        ActualQtyStatus = (HashMap<String, String>) getIntent().getSerializableExtra("actualQtyStatus");
        //PickStrategy = (HashMap<String, String>) getIntent().getSerializableExtra("pickingStrategy");
        //alreadyPickedData = (DataTable) getIntent().getSerializableExtra("DetPicked");

        DataRow sheetDetRow = sheetDetData.Rows.get(0);
        holder = new ViewHolder();

        holder.BinId = findViewById(R.id.etSheetDetPickBinId);
        holder.RegisterId = findViewById(R.id.etSheetDetPickRegisterId);
        holder.ItemId = findViewById(R.id.tvSheetDetPickItemId);
        holder.SheetId = findViewById(R.id.tvSheetDetPickSheetId);
        holder.SheetDetPickQty = findViewById(R.id.tvSheetDetPickQty);
        holder.Seq = findViewById(R.id.tvSheetDetPickSeq);
        holder.RegisterData = findViewById(R.id.lvSheetDetPickRegisterData);
        holder.PickSheetId = findViewById(R.id.tvSheetDetPickSheetPickId);
        holder.tvColor = findViewById(R.id.tvColor);
        holder.chkUseRec = findViewById(R.id.chkUseRec);

        holder.chkUseRec.setOnClickListener(checkedChange);

        if (sheetDetRow == null) return;

        holder.ItemId.setText(sheetDetRow.getValue("ITEM_ID").toString());
        holder.SheetId.setText(sheetDetRow.getValue("SOURCE_SHEET_ID").toString());
        holder.PickSheetId.setText(sheetDetRow.getValue("SHEET_ID").toString());
        holder.Seq.setText(sheetDetRow.getValue("SEQ").toString());
        //holder.RegisterData.setOnItemClickListener(onClickListView);

        String strArgb = sheetDetRow.getValue("ARGB_LIGHT_COLOR").toString();

        //region 填入燈號顏色
        if (!strArgb.equals("")) {
            holder.tvColor.setVisibility(View.VISIBLE);
            int a = Integer.parseInt(strArgb.split(",")[0]);
            int r = Integer.parseInt(strArgb.split(",")[1]);
            int g = Integer.parseInt(strArgb.split(",")[2]);
            int b = Integer.parseInt(strArgb.split(",")[3]);
            int color = Color.argb(a, r, g, b);
            holder.tvColor.getBackground().setTint(color);
        }
        else {
            holder.tvColor.setVisibility(View.GONE);
        }
        //endregion

        //this.checkPicked();
        //holder.SheetDetPickQty.setText(String.format("%s/%s", String.valueOf(pickedCount), String.valueOf(totalPickedCount)));

        holder.RegisterId.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    filterRegister();
                    return true;
                }
                return false;
            }
        });

        holder.BinId.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    filterBin();
                    return true;
                }
                return false;
            }
        });

        //this.getPickAndRegisterData();

        //this.getPickAndRegister();

        this.getConfigData();

        //region 修改用一般方式搜尋即可
//        holder.RegisterId.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_SEARCH){
//                    InputMethodManager manager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//                    manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                    filterRegister();
//                }
//                return false;
//            }
//        });
        //endregion

        //region 修改用一般方式搜尋即可
//        holder.BinId.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_SEARCH){
//                    InputMethodManager manager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//                    manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                    filterRegister();
//                }
//                return false;
//            }
//        });
        //endregion
    }

    private void getRsvRegAndGetCanPickList() {
        if (rsvPickedData == null || rsvPickedData.Rows.size() == 0) return;
        List<String> lstRsvLotId = new ArrayList<>();
        for (DataRow dr : rsvPickedData.Rows) {
            lstRsvLotId.add(String.format("'%s'", dr.getValue("LOT_ID").toString()));

            RegisterObj reg = new RegisterObj();
            reg.RegisterId = dr.getValue("LOT_ID").toString();
            reg.BinId = dr.getValue("BIN_ID").toString();
            reg.NeedToPickQty = Float.parseFloat(dr.getValue("QTY").toString());
            reg.StorageId = dr.getValue("STORAGE_ID").toString();
            reg.IsReserved = true;
            registerData.add(reg);
        }

        if (lstRsvLotId.size() <= 0) return;
        String strRsvLotId = TextUtils.join(", ", lstRsvLotId);

        // Call BIModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchRegisterByLOT");
        bmObj.setRequestID("BIFetchRegisterByLOT");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo param = new ParameterInfo();
        param.setParameterID(BIWMSFetchInfoParam.Filter);
        param.setParameterValue(String.format(" AND CMB.RESERVE != 'N' AND SR.REGISTER_ID in (%s) ", strRsvLotId));
        bmObj.params.add(param);

        CallBIModule(bmObj, new WebAPIClientEvent() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn))
                {
                    DataTable rsvRegData = bModuleReturn.getReturnJsonTables().get("BIFetchRegisterByLOT").get("Register");

                    for (DataRow dr : rsvRegData.Rows) {

                        for (RegisterObj obj : registerData) {
                            if (obj.RegisterId.equals(dr.getValue("REGISTER_ID").toString())) {
                                obj.Qty = Float.parseFloat(dr.getValue("QTY").toString());
                                obj.ReserveQty = Float.parseFloat(dr.getValue("RESERVED_QTY").toString());
                                obj.ItemUom = dr.getValue("ITEM_UOM").toString();
                                obj.RegisterType = dr.getValue("REGISTER_TYPE").toString();
                                obj.BatchId = dr.getValue("REGISTER_BATCH_ID").toString();
                                obj.BatchPosition = dr.getValue("REGISTER_BATCH_POSITION").toString();
                                obj.MfgDate = dr.getValue("MFG_DATE").toString();
                                obj.ExpDate = dr.getValue("EXP_DATE").toString();
                            }
                        }
                    }

                    for (DataRow dr : regData.Rows) {
                        if (dr.getValue("BIN_ID").equals("*")) continue;

                        RegisterObj reg = new RegisterObj();
                        reg.RegisterId = dr.getValue("REGISTER_ID").toString();
                        reg.BinId = dr.getValue("BIN_ID").toString();
                        reg.Qty = Float.parseFloat(dr.getValue("QTY").toString());
                        reg.ReserveQty = Float.parseFloat(dr.getValue("RESERVED_QTY").toString());
                        reg.ItemUom = dr.getValue("ITEM_UOM").toString();
                        reg.StorageId = dr.getValue("STORAGE_ID").toString();
                        reg.BatchId = dr.getValue("REGISTER_BATCH_ID").toString();
                        reg.BatchPosition = dr.getValue("REGISTER_BATCH_POSITION").toString();
                        reg.IsReserved = false;
                        reg.RegisterType = dr.getValue("REGISTER_TYPE").toString();
                        reg.MfgDate = dr.getValue("MFG_DATE").toString();
                        reg.ExpDate = dr.getValue("EXP_DATE").toString();

                        if (Double.parseDouble(dr.getValue("QTY").toString()) == 0) {
                            dr.setValue("SELECTED", "True");
                            reg.Selected = true;
                        }

                        boolean isExist = false;

                        if (registerData.size() > 0)
                        {
                            for (RegisterObj obj : registerData)
                            {
                                if (obj.RegisterId.equals(reg.RegisterId))
                                {
                                    isExist = true;
                                }
                            }
                        }
                        if (!isExist) {
                            registerData.add(reg);
                        }
                    }

                    if (dtConfigSort != null && dtConfigSort.Rows.size() > 0) {
                            Collections.sort(registerData, new RegMultiComparator(dtConfigSort));
                    }

                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    SheetDetPickGridAdapter adapter = new SheetDetPickGridAdapter(registerData, inflater);
                    holder.RegisterData.setAdapter(adapter);
                }
                CheckPBL();
            }
        });
    }

    private void checkPickedQty() {
        pickedCount = 0;
        totalPickedCount = Double.parseDouble(sheetDetData.Rows.get(0).getValue("TRX_QTY").toString());
        rsvPickedData = new DataTable();
        if (alreadyPickedData == null || alreadyPickedData.Rows.size() == 0) return;
        for (DataRow dr : alreadyPickedData.Rows) {
            if (!dr.getValue("SEQ").toString().equals(holder.Seq.getText().toString()))
                continue;
            if (dr.getValue("IS_PICKED").equals("Y"))
                pickedCount += Double.parseDouble(dr.getValue("QTY").toString());
            else
                rsvPickedData.Rows.add(dr);
        }
        if (pickedCount >= totalPickedCount) {
            gotoPreviousActivity(GoodPickDetailActivity.class);
        }
    }

    private void filterRegister() {
        if (registerData == null || registerData.size() == 0) return;
        String registerId = holder.RegisterId.getText().toString().toUpperCase().trim(); //20200729 archie 轉大寫
        List<RegisterObj> filterReg = new ArrayList<RegisterObj>();
        if (!registerId.equals("")) {
            for (RegisterObj reg : registerData) {
                if (reg.RegisterId.equals(registerId)) {
                    filterReg.add(reg);
                }
            }
        }
        if (filterReg.size() == 0 && registerId.equals(""))
            filterReg = registerData;

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        SheetDetPickGridAdapter adapter = new SheetDetPickGridAdapter(filterReg, inflater);
        holder.RegisterData.setAdapter(adapter);
        //holder.RegisterData.setOnItemClickListener(onClickListView);

        if (filterReg.size() == 1)
        {
            ShowDialog(filterReg);
        }
    }

    private void filterBin() {
        if (registerData == null || registerData.size() == 0) return;
        String bnId = holder.BinId.getText().toString().toUpperCase().trim(); //20200729 archie 轉大寫
        List<RegisterObj> filterReg = new ArrayList<RegisterObj>();
        if (!bnId.equals("")) {
            for (RegisterObj reg : registerData) {
                if (reg.BinId.equals(bnId)) {
                    filterReg.add(reg);
                }
            }
        }
        if (filterReg.size() == 0 && bnId.equals(""))
            filterReg = registerData;

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        SheetDetPickGridAdapter adapter = new SheetDetPickGridAdapter(filterReg, inflater);
        holder.RegisterData.setAdapter(adapter);
        //holder.RegisterData.setOnItemClickListener(onClickListView);

        if (filterReg.size() == 1)
        {
            ShowDialog(filterReg);
        }
    }

    private void getConfigData() {
        List<BModuleObject> biObjs = new ArrayList<BModuleObject>();
        biObjs.add(getConfigCondAndSortBModuleObj());
        biObjs.add(getConfigBModuleObj());

        CallBIModule(biObjs, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    dtConfigCond = bModuleReturn.getReturnJsonTables().get("FetchConfigCondAndSortBySheetId").get("SheetConfigCond");
                    dtConfigSort = bModuleReturn.getReturnJsonTables().get("FetchConfigCondAndSortBySheetId").get("SheetConfigSort");
                    dtConfig = bModuleReturn.getReturnJsonTables().get("FetchWmsSheetConfigBySheetId").get("SBRM_WMS_SHEET_CONFIG");

                    getPickAndRegister(strIsRecommend);
                }
            }
        });
    }

    private void getPickAndRegister(String strIsRecommend) {
        List<BModuleObject> biObjs = new ArrayList<BModuleObject>();
        biObjs.add(getRegisterDataBModuleObj(strIsRecommend));
        biObjs.add(getPickDataBModuleObj());

        CallBIModule(biObjs, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    registerData = new ArrayList<RegisterObj>();
                    alreadyPickedData = bModuleReturn.getReturnJsonTables().get("BIFetchPick").get("ShtPickDet");

                    checkPickedQty();
                    holder.SheetDetPickQty.setText(String.format("%s/%s", String.valueOf(pickedCount), String.valueOf(totalPickedCount)));

                    regData = bModuleReturn.getReturnJsonTables().get("BIFetchRegister").get("Register");
                    if ((regData == null || regData.Rows.size() == 0) && rsvPickedData.Rows.size() < 0) {
                        //目前並未有任何的批號可揀
                        ShowMessage(R.string.WAPG001003);
                        return;
                    }

                    if (rsvPickedData != null && rsvPickedData.Rows.size() > 0) {
                        // 預約 + REG
                        getRsvRegAndGetCanPickList();
                    } else {
                        // 不含預約
                        getCanPickList();
                    }
                }
            }
        });
    }

    private void getCanPickList() {
        for (DataRow dr : regData.Rows) {
            if (dr.getValue("BIN_ID").equals("*")) continue;

            RegisterObj reg = new RegisterObj();
            reg.RegisterId = dr.getValue("REGISTER_ID").toString();
            reg.BinId = dr.getValue("BIN_ID").toString();
            reg.Qty = Float.parseFloat(dr.getValue("QTY").toString());
            reg.ReserveQty = Float.parseFloat(dr.getValue("RESERVED_QTY").toString());
            reg.ItemUom = dr.getValue("ITEM_UOM").toString();
            reg.StorageId = dr.getValue("STORAGE_ID").toString();
            reg.BatchId = dr.getValue("REGISTER_BATCH_ID").toString();
            reg.BatchPosition = dr.getValue("REGISTER_BATCH_POSITION").toString();
            reg.IsReserved = false;
            reg.RegisterType = dr.getValue("REGISTER_TYPE").toString();
            reg.MfgDate = dr.getValue("MFG_DATE").toString();
            reg.ExpDate = dr.getValue("EXP_DATE").toString();

            if (Double.parseDouble(dr.getValue("QTY").toString()) == 0) {
                dr.setValue("SELECTED", "True");
                reg.Selected = true;
            }

            boolean isExist = false;

            if (registerData.size() > 0)
            {
                for (RegisterObj obj : registerData)
                {
                    if (obj.RegisterId.equals(reg.RegisterId))
                    {
                        isExist = true;
                    }
                }
            }

            if (!isExist) {
                registerData.add(reg);
            }
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        SheetDetPickGridAdapter adapter = new SheetDetPickGridAdapter(registerData, inflater);
        holder.RegisterData.setAdapter(adapter);

        CheckPBL();
    }

    private BModuleObject getRegisterDataBModuleObj(String strIsRecommend) {

        //region -- 原揀貨策略(註解) --
        /*
        //20211006 Hans 查詢揀貨條件改成與 Winform相同
        String strBIModuleName = null;

        switch (PickStrategy.get("PickStrategy").toString()){
            case "FIFO":
                strBIModuleName = "BIFetchRegisterByFIFO";
                break;
            case "FEFO":
                strBIModuleName = "BIFetchRegisterByFEFO";
                break;
            case  "LOT":
                strBIModuleName = "BIFetchRegisterByLOT";
                break;
                default:
                    // 無指定挑貨策略
                    break;
        }
        */
        //endregion

        List<String> lstFilter = new ArrayList<String>();
        HashMap<String, Integer> dicItemQty = new  HashMap<String, Integer>();

        for (DataRow dr : sheetDetData.Rows){

            /* winform column name 變更
            SHEET_ID => MTL_SHEET_ID
            FROM_STORAGE_KEY => STORAGE_KEY
            FROM_STORAGE_ID => STORAGE_ID
            FROM_BIN_ID => BIN_ID
            TRX_QTY => QTY
            TRX_CMT => CMT
            ITEM_UOM => UOM
             */

            List<String> lstFilterCol = new ArrayList<String>();

            /* // 改由動態揀貨規則指定
            if (!dr.getValue("LOT_ID").toString().equals("*")){
                lstFilterCol.add(String.format("SR.REGISTER_ID='%s'", dr.getValue("LOT_ID").toString()));
            }
            */

            if (!dr.getValue("FROM_BIN_ID").toString().equals("*")){
                lstFilterCol.add(String.format("SR.BIN_ID='%s'", dr.getValue("FROM_BIN_ID").toString()));
            }else{
                lstFilterCol.add("SR.BIN_ID <> '*'");
            }

            lstFilterCol.add(String.format("SI.ITEM_ID='%s'", dr.getValue("ITEM_ID").toString()));
            lstFilterCol.add("CMB.RESERVE != 'N'");
            lstFilterCol.add(String.format("SS.STORAGE_ID = '%s'", dr.getValue("FROM_STORAGE_ID").toString()));

            // 若單據的 register_id 有值，則不需依設定找庫存，直接顯示對應的 register_id
            if (dr.getValue("REGISTER_ID").toString() != null && dr.getValue("REGISTER_ID").toString().length() > 0) {
                lstFilterCol.add(String.format("SR.REGISTER_ID = '%s'", dr.getValue("REGISTER_ID").toString()));
            } else {
                if (dtConfigCond != null && dtConfigCond.Rows.size() > 0) {
                    for (DataRow drCond : dtConfigCond.Rows) {
                        String reqVal = dr.getValue(drCond.getValue("REQ_FIELD").toString()).toString();
                        if (!reqVal.equals("*"))
                            lstFilterCol.add(String.format("SR.%s %s '%s'", drCond.getValue("REG_FIELD").toString(), drCond.get("COND_OPERATOR").toString(), reqVal)); // e.g. SR.LOT_CODE = 'L...'
                    }
                }
            }

            lstFilter.add(String.format("(%s)", TextUtils.join(" AND ", lstFilterCol)));

            //計算該料號需要揀的總量
            int qty = 0;
            qty = (int) Math.round(Float.parseFloat(dr.getValue("TRX_QTY").toString()));

            String itemId = dr.getValue("ITEM_ID").toString();
            if (dicItemQty.containsKey(itemId)){
                dicItemQty.put(itemId, dicItemQty.get(itemId) + qty);
            }else{
                dicItemQty.put(itemId, qty);
            }
        }

        List<String> lstConfigSort = new ArrayList<>();
        String strConfigSort = null;
        if (dtConfigSort != null && dtConfigSort.Rows.size() > 0) {
            for (DataRow drSort : dtConfigSort.Rows) {
                lstConfigSort.add(String.format("%s %s", drSort.getValue("REG_FIELD").toString(), drSort.get("SORT_METHOD").toString())); // e.g. MFG_DATE ASC
            }
        }
        if (lstConfigSort.size() > 0) {
            strConfigSort = TextUtils.join(", ", lstConfigSort);
        }

        BModuleObject biObj = new BModuleObject();
        biObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        //biObj.setModuleID(strBIModuleName.toString());
        //biObj.setModuleID("BIFetchRegisterByFIFO");
        biObj.setModuleID("BIFetchRegisterByCondition");
        biObj.setRequestID("BIFetchRegister");
        biObj.params = new Vector<>();

        /*
//        List<Condition> conditionSIs = new ArrayList<>();
//        List<Condition> conditionStatus = new ArrayList<>();
//        List<Condition> conditionBins = new ArrayList<>();
//        List<Condition> conditionRegisters = new ArrayList<>();
//
//        HashMap<String, List<?>> dicCondition = new HashMap<>();
//
//        Condition condItem = new Condition();
//        condItem.setAliasTable("SI");
//        condItem.setColumnName("ITEM_ID");
//        condItem.setDataType("string");
//        condItem.setValue(holder.ItemId.getText().toString());
//        conditionSIs.add(condItem);
//        dicCondition.put("ITEM_ID", conditionSIs);
//
//        Condition condStatus = new Condition();
//        condStatus.setAliasTable("SR");
//        condStatus.setColumnName("REGISTER_STATUS");
//        condStatus.setDataType("string");
//        condStatus.setValue("Available");
//        conditionStatus.add(condStatus);
//        dicCondition.put("REGISTER_STATUS", conditionStatus);
//
//        if (!sheetDetData.Rows.get(0).getValue("FROM_BIN_ID").toString().equals("*")) {
//            Condition condBinId = new Condition();
//            condBinId.setAliasTable("SR");
//            condBinId.setColumnName("BIN_ID");
//            condBinId.setDataType("string");
//            condBinId.setValue(sheetDetData.Rows.get(0).getValue("FROM_BIN_ID").toString());
//            conditionBins.add(condBinId);
//            dicCondition.put("BIN_ID", conditionBins);
//        }
//
//        if (!sheetDetData.Rows.get(0).getValue("LOT_ID").toString().equals("*")) {
//            Condition condRegisterId = new Condition();
//            condRegisterId.setAliasTable("SR");
//            condRegisterId.setColumnName("REGISTER_ID");
//            condRegisterId.setDataType("string");
//            condRegisterId.setValue(sheetDetData.Rows.get(0).getValue("LOT_ID").toString());
//            conditionRegisters.add(condRegisterId);
//            dicCondition.put("REGISTER_ID", conditionRegisters);
//        }
//
//        VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
//        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.INV.Parameter.ParameterObj.Condition", "bmWMS.INV.Param");
//
//        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vKey, vVal);
//        String serializedObj = msdl.generateFinalCode(dicCondition);
//        new ParameterInfo().setParameterID(BIWMSFetchInfoParam.Condition);
//        ParameterInfo param1 = new ParameterInfo();
//        param1.setParameterID(BIWMSFetchInfoParam.Condition);
//        param1.setNetParameterValue(serializedObj);
         */

        ParameterInfo paramFilter = new ParameterInfo();
        paramFilter.setParameterID(BIWMSFetchInfoParam.Filter);
        paramFilter.setParameterValue(String.format("  AND SR.REGISTER_STATUS='Available' AND (%s)", TextUtils.join(" OR ", lstFilter)));

        VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create(VirtualClass.VirtualClassType.Decimal);
        MesSerializableDictionary msd = new MesSerializableDictionary(vKey, vVal);
        String strItemQty = msd.generateFinalCode(dicItemQty);

        ParameterInfo paramItemQty = new ParameterInfo();
        paramItemQty.setParameterID(BIWMSFetchInfoParam.ItemQty);
        paramItemQty.setNetParameterValue(strItemQty);

        ParameterInfo paramIsRecommend = new ParameterInfo();
        paramIsRecommend.setParameterID(BIWMSFetchInfoParam.IsRecommend);
        paramIsRecommend.setNetParameterValue(strIsRecommend);


        if (strConfigSort != null && strConfigSort.length() > 0) {
            ParameterInfo paramSort = new ParameterInfo();
            paramSort.setParameterID(BIFetchPickStrategyParam.ConfigSort);
            paramSort.setParameterValue(strConfigSort);
            biObj.params.add(paramSort);
        }

//        biObj.params.add(param1);
        biObj.params.add(paramFilter);
        biObj.params.add(paramItemQty);
        biObj.params.add(paramIsRecommend);

        return biObj;
    }

    private BModuleObject getPickDataBModuleObj() {
        BModuleObject biObj = new BModuleObject();
        biObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        biObj.setModuleID("BIFetchSheetPickMstAndDet");
        biObj.setRequestID("BIFetchPick");
        biObj.params = new Vector<>();
        VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");

        List<Condition> conditionMs = new ArrayList<>();
        List<Condition> conditionSheetType = new ArrayList<>();
        HashMap<String, List<?>> dicCondition = new HashMap<>();

        Condition condSheet = new Condition();
        condSheet.setAliasTable("MST");
        condSheet.setColumnName("SHEET_ID");
        condSheet.setDataType("string");
        condSheet.setValue(sheetDetData.Rows.get(0).getValue("SHEET_ID").toString());
        conditionMs.add(condSheet);
        dicCondition.put("SHEET_ID", conditionMs);

        Condition condSheetType = new Condition();
        condSheetType.setAliasTable("ST");
        condSheetType.setColumnName("SHEET_TYPE_ID");
        condSheetType.setDataType("string");
        condSheetType.setValue(sheetDetData.Rows.get(0).getValue("SHEET_TYPE_ID").toString());
        conditionSheetType.add(condSheetType);
        dicCondition.put("SHEET_TYPE_ID", conditionSheetType);

        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vKey, vVal);
        String serializedObj = msdl.generateFinalCode(dicCondition);

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Condition);
        param1.setNetParameterValue(serializedObj);

        biObj.params.add(param1);
        return biObj;
    }

    private BModuleObject getConfigCondAndSortBModuleObj() {

        // region 取得ConfigCond及ConfigSort
        BModuleObject biShtCfgSortAndCond = new BModuleObject();
        biShtCfgSortAndCond.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        biShtCfgSortAndCond.setModuleID("BIFetchConfigCondAndSortBySheetId");
        biShtCfgSortAndCond.setRequestID("FetchConfigCondAndSortBySheetId");
        biShtCfgSortAndCond.params = new Vector<ParameterInfo>();
        ParameterInfo paramShtTypeKey = new ParameterInfo();
        paramShtTypeKey.setParameterID(BIFetchPickStrategyParam.SheetId);
        paramShtTypeKey.setParameterValue(sheetDetData.Rows.get(0).getValue("SHEET_ID").toString());
        biShtCfgSortAndCond.params.add(paramShtTypeKey);
        // endregion

        return biShtCfgSortAndCond;
    }

    private BModuleObject getConfigBModuleObj() {

        // region 取得Config
        BModuleObject biShtCfg = new BModuleObject();
        biShtCfg.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        biShtCfg.setModuleID("BIFetchWmsSheetConfigBySheetId");
        biShtCfg.setRequestID("FetchWmsSheetConfigBySheetId");
        biShtCfg.params = new Vector<ParameterInfo>();
        ParameterInfo paramFilter = new ParameterInfo();
        paramFilter.setParameterID(BIWMSFetchInfoParam.Filter);
        paramFilter.setParameterValue(String.format("  AND M.SHEET_ID = '%s'", sheetDetData.Rows.get(0).getValue("SHEET_ID").toString()));
        biShtCfg.params.add(paramFilter);
        // endregion

        return biShtCfg;
    }

    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

            //CheckBox cb = view.findViewById(R.id.cbPickedGridSelectedRegister);
            TextView tvQty = view.findViewById(R.id.tvPickedGridQty); // 含可揀/要揀/預揀數量

            AlertDialog.Builder pickedQtyDialog = new AlertDialog.Builder(GoodPickExecutedActivity.this);
            View dialogView = LayoutInflater.from(GoodPickExecutedActivity.this).inflate(R.layout.style_wms_dialog_picked_qty, null);
            pickedQtyDialog.setTitle("");
            pickedQtyDialog.setView(dialogView);
            final EditText etQty = dialogView.findViewById(R.id.etPickedQty);
            final Spinner cmbBin = dialogView.findViewById(R.id.cmbTempBin);

            String[] splitQty = tvQty.getText().toString().split(" \\/ ");
            final String canPickQty = splitQty[0];
            final String needToPickQty = splitQty[1];
            final String rsvPickQty = splitQty[2];

            final String itemType = registerData.get(position).RegisterType;

            //etQty.setText(tvQty.getText());

            double qty = 0.0;

            switch (itemType) {
                case "MinimizePackSN":
                    if (!needToPickQty.equals("0.0")) {
                        qty = Double.parseDouble(needToPickQty); // 若有預約數量，直接顯示
                        etQty.setText(String.valueOf(qty));
                    } else {
                        qty = Double.parseDouble(canPickQty); // 最小包號要整批出
                        etQty.setText(String.valueOf(qty));
                    }
                    break;
                case "LotNo":
                case "ItemID":
                    if (!needToPickQty.equals("0.0")) {
                        qty = Double.parseDouble(needToPickQty); // 若有預約數量，直接顯示
                        etQty.setText(String.valueOf(qty));
                    } else {
                        qty = Double.parseDouble(canPickQty);  // 非最小包號依是否達揀貨數量出
                        if ((qty + pickedCount) <= totalPickedCount) {
                            etQty.setText(String.valueOf(qty));
                        } else {
                            etQty.setText(String.valueOf(totalPickedCount-pickedCount));
                        }
                    }
                    break;
                default:
                    break;
            }

            /*
            if (!needToPickQty.equals("0.0")) {
                qty = Double.parseDouble(needToPickQty);
                etQty.setText(String.valueOf(qty));
            } else {
                qty = Double.parseDouble(canPickQty);
                if ((qty + pickedCount) <= totalPickedCount)
                {
                    //etQty.setText(tvQty.getText());
                    etQty.setText(String.valueOf(qty));
                }
                else
                {
                    etQty.setText(String.valueOf(totalPickedCount-pickedCount));
                }
            }
             */

            //qty = Double.parseDouble(tvQty.getText().toString());

            etQty.setEnabled(true);

            BModuleObject bimObj = new BModuleObject();
            bimObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
            bimObj.setModuleID("BIFetchBin");
            bimObj.setRequestID("BIFetchBin");
            bimObj.params = new Vector<ParameterInfo>();

            ParameterInfo param1 = new ParameterInfo();
            param1.setParameterID(BIWMSFetchInfoParam.Filter);
            param1.setParameterValue(String.format(" AND STORAGE_ID ='%s' AND B.BIN_TYPE IN ('OT', 'OS')", registerData.get(position).StorageId));
            bimObj.params.add(param1);

            CallBIModule(bimObj, new WebAPIClientEvent(){
                @Override
                public void onPostBack(BModuleReturn bModuleReturn){
                    if(CheckBModuleReturnInfo(bModuleReturn)){
                        DataTable dtBin = bModuleReturn.getReturnJsonTables().get("BIFetchBin").get("BIN");

                        boolean bOT = false;
                        for(DataRow dr : dtBin.Rows)
                        {
                            if(dr.getValue("BIN_TYPE").equals("OT"))
                            {
                                bOT = true;
                                break;
                            }
                        }

                        ArrayList<String> alBin = new ArrayList<String>();
                        for(int i = 0; i < dtBin.Rows.size(); i++)
                        {
                            String binId = dtBin.Rows.get(i).getValue("BIN_ID").toString();
                            String binType = dtBin.Rows.get(i).getValue("BIN_TYPE").toString();

                            if(bOT)
                            {
                                if(binType.equals("OT"))
                                    alBin.add(binId);
                            }
                            else
                            {
                                if(!alBin.contains(binId))
                                    alBin.add(binId);
                            }
                        }
                        ArrayAdapter<String> adapterBin = new ArrayAdapter<>(GoodPickExecutedActivity.this, android.R.layout.simple_spinner_dropdown_item, alBin);
                        cmbBin.setAdapter(adapterBin);
                    }
                }
            });

//            pickedQtyDialog.setPositiveButton(R.string.CONFIRM, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    String binId = pickedData.Rows.get(position).getValue("BIN_ID").toString();
//                    String lotId = pickedData.Rows.get(position).getValue("REGISTER_ID").toString();
//                    String qty = pickedData.Rows.get(position).getValue("QTY").toString();
//                    String storageId = pickedData.Rows.get(position).getValue("STORAGE_ID").toString();
//                    String uom = pickedData.Rows.get(position).getValue("ITEM_UOM").toString();
//                    ExecutePicked(binId, lotId, qty, storageId, uom, position);
//                }
//            });
//            pickedQtyDialog.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    isConfirm = false;
//                }
//            });

            final AlertDialog dialog = pickedQtyDialog.create();
            dialog.show();

//            Button bConfirm = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
//            Button bCancel = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
//            if (bConfirm != null)
//                bConfirm.setTextColor(getResources().getColor(R.color.common_theme_blue1));
//            if (bCancel != null)
//                bCancel.setTextColor(getResources().getColor(R.color.common_theme_blue1));

            Button bConfirm = dialogView.findViewById(R.id.btnConfirm);
            bConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Double inputQty = Double.parseDouble(etQty.getText().toString());

                    switch (itemType) {
                        case "MinimizePackSN":
                            if (!needToPickQty.equals("0.0")) {
                                if (inputQty != Double.parseDouble(needToPickQty)) {
                                    Object[] args = new Object[2];
                                    args[0] = itemType;
                                    args[1] = needToPickQty;
                                    ShowMessage(R.string.WAPG001014, args); // WAPG001014   該物料註冊類別為[%s]，輸入數量須為整批數量[%s]
                                    return;
                                }
                            } else {
                                if (inputQty != Double.parseDouble(canPickQty)) {
                                    Object[] args = new Object[2];
                                    args[0] = itemType;
                                    args[1] = canPickQty;
                                    ShowMessage(R.string.WAPG001014, args); // WAPG001014   該物料註冊類別為[%s]，輸入數量須為整批數量[%s]
                                    return;
                                }
                            }
                            break;
                        case "LotNo":
                        case "ItemID":
                            if (inputQty > (Double.parseDouble(canPickQty) + Double.parseDouble(needToPickQty))) {
                                Object[] args = new Object[1];
                                args[0] = String.valueOf((Double.parseDouble(canPickQty) + Double.parseDouble(needToPickQty)));
                                ShowMessage(R.string.WAPG001015, args); // WAPG001015   超出剩餘可揀的數量[%s]
                                return;
                            }
                            break;
                        default:
                            break;
                    }

                    if (dtConfig.Rows.get(0).getValue("ACTUAL_QTY_STATUS").toString().equals("")) {
                        // WAPG001009 單據類型[%s], 單據Config設定 [實際數量的狀態]未設置
                        ShowMessage(R.string.WAPG001009,
                                dtConfig.Rows.get(0).getValue("SHEET_TYPE_ID").toString());
                        return;
                    }

                    switch (dtConfig.Rows.get(0).getValue("ACTUAL_QTY_STATUS").toString()) //ActualQtyStatus.get(holder.PickSheetId.getText().toString())
                    {
                        case "Equal":
                        case "Less":
                            if (Double.parseDouble(etQty.getText().toString()) > totalPickedCount-pickedCount)
                            {
                                // WAPG001008   單據[%s],順序[%s]揀貨數量[%s]不可大於單據數量[%s]
                                ShowMessage(R.string.WAPG001008,
                                        holder.SheetId.getText().toString(),
                                        holder.Seq.getText().toString(),
                                        (Double.parseDouble(etQty.getText().toString())),
                                        totalPickedCount - pickedCount);
                                return;
                            }
                            break;

                            default:
                                break;
                    }

                    RegisterObj reg = registerData.get(position);
                    String binId = reg.BinId;
                    String lotId = reg.RegisterId;
                    String qty = etQty.getText().toString();
                    String storageId = reg.StorageId;
                    String uom = registerData.get(position).ItemUom;
                    String tempBin = cmbBin.getSelectedItem().toString();

                    HashMap<String, String> mapBin = new HashMap<>();
                    mapBin.put(storageId, tempBin);

                    ExecutePicked(binId, lotId, qty, storageId, uom, position, mapBin);

                    if (Double.compare(Double.parseDouble(qty), Double.parseDouble(String.valueOf(reg.Qty))) == 0) {
                        registerData.get(position).Selected = true;
                    }
                    dialog.dismiss();
                }
            });

            Button bCancel = dialogView.findViewById(R.id.btnCancel);
            bCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }
    };

    private void ExecutePicked(String binId, String lotId, String qty, String storageId, String uom, final int position, HashMap<String, String> mapBin) {
        ArrayList<PickDetObj> lstPickDet = new ArrayList<>();
        final int pos = position;
        final String fQty = qty;
        PickDetObj detObj = new PickDetObj();
        detObj.setSheetId(holder.PickSheetId.getText().toString());
        detObj.setItemId(holder.ItemId.getText().toString());
        detObj.setSeq(Double.parseDouble(holder.Seq.getText().toString()));
        detObj.setBinId(binId);
        detObj.setLotId(lotId);
        detObj.setQty(Double.parseDouble(qty));
        detObj.setStorageId(storageId);
        detObj.setUom(uom);
        lstPickDet.add(detObj);

        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.PickDetObj", "bmWMS.Library.Param");
        MList mListEnum = new MList(vListEnum);
        String strLsRelatData = mListEnum.generateFinalCode(lstPickDet);

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BGoodPick");
        bmObj.setModuleID("");
        bmObj.setRequestID("BGoodPick");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo param = new ParameterInfo();
        param.setParameterID(BStockOutBaseParam.CheckStockOutByObjs); //20220829 archie 將可共用的Param改為出庫Base的Param
        param.setNetParameterValue(strLsRelatData);
        bmObj.params.add(param);

        ParameterInfo paramMode = new ParameterInfo();
        paramMode.setParameterID(BStockOutBaseParam.Mode);
        paramMode.setParameterValue("Pick");
        bmObj.params.add(paramMode);

        // region 儲存盤點狀態檢查物件
        // 20220805 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中
        List<CheckCountObj> lstChkCountObj = new ArrayList<>();
        CheckCountObj chkCountObjFromBin = new CheckCountObj(); // FROM_BIN
        chkCountObjFromBin.setStorageId(storageId);
        chkCountObjFromBin.setItemId(holder.ItemId.getText().toString());
        chkCountObjFromBin.setBinId(binId);
        lstChkCountObj.add(chkCountObjFromBin);
        CheckCountObj chkCountObjToBin = new CheckCountObj(); // TO_BIN
        chkCountObjToBin.setStorageId(storageId);
        chkCountObjToBin.setItemId(holder.ItemId.getText().toString());
        chkCountObjToBin.setBinId(mapBin.get(storageId));
        lstChkCountObj.add(chkCountObjToBin);
        // endregion

        VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create(VirtualClass.VirtualClassType.String);
        MesSerializableDictionary msd = new MesSerializableDictionary(vKey, vVal);
        String serializedObj = msd.generateFinalCode(mapBin);

        ParameterInfo paramBin = new ParameterInfo();
        paramBin.setParameterID(BGoodReservationParam.StorageTempBin);
        paramBin.setNetParameterValue(serializedObj);
        bmObj.params.add(paramBin);

        VirtualClass vListEnum2 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.CheckCountObj", "bmWMS.Library.Param");
        MList mListEnum2 = new MList(vListEnum2);
        String strCheckCountObj = mListEnum2.generateFinalCode(lstChkCountObj);

        ParameterInfo paramExecuteChkStock = new ParameterInfo();
        paramExecuteChkStock.setParameterID(BGoodReservationParam.ExecuteCheckStock); // 20220805 Add by Ikea 是否執行盤點檢查
        paramExecuteChkStock.setNetParameterValue2("true");
        bmObj.params.add(paramExecuteChkStock);

        ParameterInfo paramChkCountObj = new ParameterInfo(); // 20220805 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中
        paramChkCountObj.setParameterID(BGoodReservationParam.CheckCountObj);
        paramChkCountObj.setNetParameterValue(strCheckCountObj);
        bmObj.params.add(paramChkCountObj);

        CallBModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {

                    String holdItemId = "";
                    String holdMessage = "";
                    String hintMessage = "";

                    if (bModuleReturn.getReturnList().get("BGoodPick") != null) {

                        if (bModuleReturn.getReturnList().get("BGoodPick").get(BGoodReservationParam.HoldItemID) != null) {
                            holdItemId = bModuleReturn.getReturnList().get("BGoodPick").get(BGoodReservationParam.HoldItemID).toString().replaceAll("\"", "");
                        }

                        if (bModuleReturn.getReturnList().get("BGoodPick").get(BGoodReservationParam.HoldMessage) != null) {
                            holdMessage = bModuleReturn.getReturnList().get("BGoodPick").get(BGoodReservationParam.HoldMessage).toString().replaceAll("\"", "");
                            holdMessage = split(holdMessage);
                        }

                        if (bModuleReturn.getReturnList().get("BGoodPick").get(BGoodReservationParam.HintMessage) != null) {
                            hintMessage = bModuleReturn.getReturnList().get("BGoodPick").get(BGoodReservationParam.HintMessage).toString().replaceAll("\"", "");
                            hintMessage = split(hintMessage);
                        }
                    }

                    String showExtraMsg = "";

                    if (hintMessage.length() > 0) {
                        showExtraMsg += getResString(getResources().getString(R.string.ITEM_SPEC_CTRL_HINT_MSG)) + System.getProperty("line.separator");
                        showExtraMsg += hintMessage + System.getProperty("line.separator");
                    }

                    if (holdMessage.length() > 0) {
                        showExtraMsg += getResString(getResources().getString(R.string.ITEM_SPEC_CTRL_HOLD_MSG)) + System.getProperty("line.separator");
                        showExtraMsg += holdMessage + System.getProperty("line.separator");
                    }

                    holder.RegisterId.setText("");
                    holder.BinId.setText("");

                    if (holdItemId.length() > 0) { // 有被鎖定的物料代碼 => 揀貨失敗, 有額外資訊需顯示

                        Object[] args = new Object[1];
                        args[0] = holdItemId;

                        // 揀貨失敗
                        final String finalShowExtraMsg = showExtraMsg;
                        ShowMessage(R.string.WAPG001017, new ShowMessageEvent() {
                            @Override
                            public void onDismiss() {

                                if (finalShowExtraMsg.length() > 0) {

                                    ShowMessage(finalShowExtraMsg, new ShowMessageEvent() {
                                        @Override
                                        public void onDismiss() {
                                            getPickAndRegister(strIsRecommend);
                                        }
                                    });

                                } else {
                                    getPickAndRegister(strIsRecommend);
                                }

                            }
                        }, args);

                    } else { // 揀貨成功, 但有額外資訊需顯示

                        final String finalShowExtraMsg = showExtraMsg;
                        if (finalShowExtraMsg.length() > 0) {

                            ShowMessage(finalShowExtraMsg, new ShowMessageEvent() {
                                @Override
                                public void onDismiss() {
                                    getPickAndRegister(strIsRecommend);
                                }
                            });

                        } else {
                            getPickAndRegister(strIsRecommend);
                        }
                    }
                }
            }
        });
    }

    public void OnClickQRScan(View v) {
        IntentIntegrator integrator = new IntentIntegrator(GoodPickExecutedActivity.this);
        // 設置要掃描的條碼類型，ONE_D_CODE_TYPES：一維碼，QR_CODE_TYPES-二維碼
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("BarCode Scan"); //底部的提示文字
        integrator.setCameraId(0); //前置(1)或者後置(0)攝像頭
        integrator.setBeepEnabled(false); //掃描成功的「嗶嗶」聲
        integrator.setBarcodeImageEnabled(false); //是否保留掃碼成功時候的截圖
        integrator.setCaptureActivity(ScanActivity.class);
        switch (v.getId()) {
            case R.id.ibtnRegisterIdQRScan:
                integrator.setRequestCode(REGISTER_ID_QRSCAN_REQUEST_CODE);
                break;
            case R.id.ibtnPickBinQRScan:
                integrator.setRequestCode(BIN_ID_QRSCAN_REQUEST_CODE);
                break;
            default:
                integrator.setRequestCode(0);
                break;
        }
        integrator.initiateScan();
    }

    private void CheckPBL() {
        ArrayList<String> ids = new ArrayList<String>();
        ids.add(sheetDetData.Rows.get(0).getValue("SHEET_ID").toString());

        String strSheetTypePolicyId = sheetDetData.Rows.get(0).getValue("SHEET_TYPE_POLICY_ID").toString();

        // Call BIModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSPickByLight");
        bmObj.setModuleID("BIGetPKLBIn");
        bmObj.setRequestID("BIGetPKLBIn");
        bmObj.params = new Vector<ParameterInfo>();

        VirtualClass vList = VirtualClass.create(VirtualClass.VirtualClassType.String);
        MList mList = new MList(vList);
        String strLstStatus = mList.generateFinalCode(ids);

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSPickByLightParam.LstSheetID);
        param1.setNetParameterValue(strLstStatus);
        bmObj.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIWMSPickByLightParam.SheetTypePolicyID);
        param2.setParameterValue(strSheetTypePolicyId);
        bmObj.params.add(param2);

        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BIWMSPickByLightParam.IsCheck);
        param3.setParameterValue("Y");
        bmObj.params.add(param3);

        ParameterInfo param4 = new ParameterInfo();
        param4.setParameterID(BIWMSPickByLightParam.LightSignal);
        param4.setParameterValue("OFF");
        bmObj.params.add(param4);

        CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn))
                {
                    
                }
            }
        });
    }

    private void getPickAndRegisterData() {
        List<BModuleObject> biObjs = new ArrayList<BModuleObject>();
        biObjs.add(getRegisterDataBModuleObj("true"));
        biObjs.add(getPickDataBModuleObj());

        CallBIModule(biObjs, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    registerData = new ArrayList<RegisterObj>();
                    alreadyPickedData = bModuleReturn.getReturnJsonTables().get("BIFetchPick").get("ShtPickDet");

                    checkPicked();

                    holder.SheetDetPickQty.setText(String.format("%s/%s", String.valueOf(pickedCount), String.valueOf(totalPickedCount)));

                    DataTable dt = bModuleReturn.getReturnJsonTables().get("BIFetchRegister").get("Register");
                    if (dt == null || dt.Rows.size() == 0) {
                        //目前並未有任何的批號可揀
                        ShowMessage(R.string.WAPG001003);
                    }

                    for (DataRow dr : dt.Rows) {
                        if (dr.getValue("BIN_ID").equals("*")) continue;

                        RegisterObj reg = new RegisterObj();
                        reg.RegisterId = dr.getValue("REGISTER_ID").toString();
                        reg.BinId = dr.getValue("BIN_ID").toString();
                        reg.Qty = Float.parseFloat(dr.getValue("QTY").toString());
                        reg.ReserveQty = Float.parseFloat(dr.getValue("RESERVED_QTY").toString());
                        reg.ItemUom = dr.getValue("ITEM_UOM").toString();
                        reg.StorageId = dr.getValue("STORAGE_ID").toString();
                        reg.BatchId = dr.getValue("REGISTER_BATCH_ID").toString();
                        reg.BatchPosition = dr.getValue("REGISTER_BATCH_POSITION").toString();
                        reg.IsReserved = false;

                        if (Double.parseDouble(dr.getValue("QTY").toString()) == 0) {
                            dr.setValue("SELECTED", "True");
                            reg.Selected = true;
                        }

                        boolean isExist = false;

                        if (registerData.size() > 0)
                        {
                            for (RegisterObj obj : registerData)
                            {
                                if (obj.RegisterId.equals(reg.RegisterId))
                                {
                                    isExist = true;
                                }
                            }
                        }

                        if (!isExist) {
                            registerData.add(reg);
                        }
                    }

                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    SheetDetPickGridAdapter adapter = new SheetDetPickGridAdapter(registerData, inflater);
                    holder.RegisterData.setAdapter(adapter);
                }
                CheckPBL();
            }
        });
    }

    private void checkPicked() {
        pickedCount = 0;
        totalPickedCount = Double.parseDouble(sheetDetData.Rows.get(0).getValue("TRX_QTY").toString());
        if (alreadyPickedData == null || alreadyPickedData.Rows.size() == 0) return;
        for (DataRow dr : alreadyPickedData.Rows) {
            if (!dr.getValue("SEQ").toString().equals(holder.Seq.getText().toString())) continue;
            if (dr.getValue("IS_PICKED").equals("N")) {
                RegisterObj reg = new RegisterObj();
                reg.RegisterId = dr.getValue("LOT_ID").toString();
                reg.BinId = dr.getValue("BIN_ID").toString();
                reg.Qty = Float.parseFloat(dr.getValue("QTY").toString());
                reg.ItemUom = dr.getValue("UOM").toString();
                reg.StorageId = dr.getValue("STORAGE_ID").toString();
                reg.IsReserved = true;

                registerData.add(reg);
                continue;
            }
            pickedCount += Double.parseDouble(dr.getValue("QTY").toString());
        }
//        if (pickedCount >= totalPickedCount) {
//            //此料號已經揀料了
//            ShowMessage(R.string.WAPG001002, new ShowMessageEvent() {
//                @Override
//                public void onDismiss() {
//                    gotoPreviousActivity(GoodPickDetailActivity.class);
//                }
//            });
//        }

        if (pickedCount >= totalPickedCount) {
            gotoPreviousActivity(GoodPickDetailActivity.class);
        }
    }

    private View.OnClickListener checkedChange = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if(holder.chkUseRec.isChecked()) {
                strIsRecommend = "true";
                getPickAndRegister(strIsRecommend);
            } else {
                strIsRecommend = "false";
                getPickAndRegister(strIsRecommend);
            }
        }
    };

    private void ShowDialog(final List<RegisterObj> filterReg) {
        //TextView tvQty = view.findViewById(R.id.tvPickedGridQty); // 含可揀/要揀/預揀數量

        AlertDialog.Builder pickedQtyDialog = new AlertDialog.Builder(GoodPickExecutedActivity.this);
        View dialogView = LayoutInflater.from(GoodPickExecutedActivity.this).inflate(R.layout.style_wms_dialog_picked_qty, null);
        pickedQtyDialog.setTitle("");
        pickedQtyDialog.setView(dialogView);
        final EditText etQty = dialogView.findViewById(R.id.etPickedQty);
        final Spinner cmbBin = dialogView.findViewById(R.id.cmbTempBin);

        final String canPickQty = filterReg.get(0).getQty();
        final String needToPickQty = filterReg.get(0).getNeedToPickQty();
        final String rsvPickQty = filterReg.get(0).getReserveQty();

        final String itemType = filterReg.get(0).RegisterType;

        //etQty.setText(tvQty.getText());

        double qty = 0.0;

        switch (itemType) {
            case "MinimizePackSN":
                if (!needToPickQty.equals("0.0")) {
                    qty = Double.parseDouble(needToPickQty); // 若有預約數量，直接顯示
                    etQty.setText(String.valueOf(qty));
                } else {
                    qty = Double.parseDouble(canPickQty); // 最小包號要整批出
                    etQty.setText(String.valueOf(qty));
                }
                break;
            case "LotNo":
            case "ItemID":
                if (!needToPickQty.equals("0.0")) {
                    qty = Double.parseDouble(needToPickQty); // 若有預約數量，直接顯示
                    etQty.setText(String.valueOf(qty));
                } else {
                    qty = Double.parseDouble(canPickQty);  // 非最小包號依是否達揀貨數量出
                    if ((qty + pickedCount) <= totalPickedCount) {
                        etQty.setText(String.valueOf(qty));
                    } else {
                        etQty.setText(String.valueOf(totalPickedCount-pickedCount));
                    }
                }
                break;
            default:
                break;
        }

            /*
            if (!needToPickQty.equals("0.0")) {
                qty = Double.parseDouble(needToPickQty);
                etQty.setText(String.valueOf(qty));
            } else {
                qty = Double.parseDouble(canPickQty);
                if ((qty + pickedCount) <= totalPickedCount)
                {
                    //etQty.setText(tvQty.getText());
                    etQty.setText(String.valueOf(qty));
                }
                else
                {
                    etQty.setText(String.valueOf(totalPickedCount-pickedCount));
                }
            }
             */

        //qty = Double.parseDouble(tvQty.getText().toString());

        etQty.setEnabled(true);

        BModuleObject bimObj = new BModuleObject();
        bimObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bimObj.setModuleID("BIFetchBin");
        bimObj.setRequestID("BIFetchBin");
        bimObj.params = new Vector<ParameterInfo>();

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Filter);
        param1.setParameterValue(String.format(" AND STORAGE_ID ='%s' AND B.BIN_TYPE IN ('OT', 'OS')", filterReg.get(0).StorageId));
        bimObj.params.add(param1);

        CallBIModule(bimObj, new WebAPIClientEvent(){
            @Override
            public void onPostBack(BModuleReturn bModuleReturn){
                if(CheckBModuleReturnInfo(bModuleReturn)){
                    DataTable dtBin = bModuleReturn.getReturnJsonTables().get("BIFetchBin").get("BIN");

                    boolean bOT = false;
                    for(DataRow dr : dtBin.Rows)
                    {
                        if(dr.getValue("BIN_TYPE").equals("OT"))
                        {
                            bOT = true;
                            break;
                        }
                    }

                    ArrayList<String> alBin = new ArrayList<String>();
                    for(int i = 0; i < dtBin.Rows.size(); i++)
                    {
                        String binId = dtBin.Rows.get(i).getValue("BIN_ID").toString();
                        String binType = dtBin.Rows.get(i).getValue("BIN_TYPE").toString();

                        if(bOT)
                        {
                            if(binType.equals("OT"))
                                alBin.add(binId);
                        }
                        else
                        {
                            if(!alBin.contains(binId))
                                alBin.add(binId);
                        }
                    }
                    ArrayAdapter<String> adapterBin = new ArrayAdapter<>(GoodPickExecutedActivity.this, android.R.layout.simple_spinner_dropdown_item, alBin);
                    cmbBin.setAdapter(adapterBin);
                }
            }
        });

//            pickedQtyDialog.setPositiveButton(R.string.CONFIRM, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    String binId = pickedData.Rows.get(position).getValue("BIN_ID").toString();
//                    String lotId = pickedData.Rows.get(position).getValue("REGISTER_ID").toString();
//                    String qty = pickedData.Rows.get(position).getValue("QTY").toString();
//                    String storageId = pickedData.Rows.get(position).getValue("STORAGE_ID").toString();
//                    String uom = pickedData.Rows.get(position).getValue("ITEM_UOM").toString();
//                    ExecutePicked(binId, lotId, qty, storageId, uom, position);
//                }
//            });
//            pickedQtyDialog.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    isConfirm = false;
//                }
//            });

        final AlertDialog dialog = pickedQtyDialog.create();
        dialog.show();

//            Button bConfirm = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
//            Button bCancel = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
//            if (bConfirm != null)
//                bConfirm.setTextColor(getResources().getColor(R.color.common_theme_blue1));
//            if (bCancel != null)
//                bCancel.setTextColor(getResources().getColor(R.color.common_theme_blue1));

        Button bConfirm = dialogView.findViewById(R.id.btnConfirm);
        bConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Double inputQty = Double.parseDouble(etQty.getText().toString());

                switch (itemType) {
                    case "MinimizePackSN":
                        if (!needToPickQty.equals("0.0")) {
                            if (inputQty != Double.parseDouble(needToPickQty)) {
                                Object[] args = new Object[2];
                                args[0] = itemType;
                                args[1] = needToPickQty;
                                ShowMessage(R.string.WAPG001014, args); // WAPG001014   該物料註冊類別為[%s]，輸入數量須為整批數量[%s]
                                return;
                            }
                        } else {
                            if (inputQty != Double.parseDouble(canPickQty)) {
                                Object[] args = new Object[2];
                                args[0] = itemType;
                                args[1] = canPickQty;
                                ShowMessage(R.string.WAPG001014, args); // WAPG001014   該物料註冊類別為[%s]，輸入數量須為整批數量[%s]
                                return;
                            }
                        }
                        break;
                    case "LotNo":
                    case "ItemID":
                        if (inputQty > (Double.parseDouble(canPickQty) + Double.parseDouble(needToPickQty))) {
                            Object[] args = new Object[1];
                            args[0] = String.valueOf((Double.parseDouble(canPickQty) + Double.parseDouble(needToPickQty)));
                            ShowMessage(R.string.WAPG001015, args); // WAPG001015   超出剩餘可揀的數量[%s]
                            return;
                        }
                        break;
                    default:
                        break;
                }

                if (dtConfig.Rows.get(0).getValue("ACTUAL_QTY_STATUS").toString().equals("")) {
                    // WAPG001009 單據類型[%s], 單據Config設定 [實際數量的狀態]未設置
                    ShowMessage(R.string.WAPG001009,
                            dtConfig.Rows.get(0).getValue("SHEET_TYPE_ID").toString());
                    return;
                }

                switch (dtConfig.Rows.get(0).getValue("ACTUAL_QTY_STATUS").toString()) //ActualQtyStatus.get(holder.PickSheetId.getText().toString())
                {
                    case "Equal":
                    case "Less":
                        if (Double.parseDouble(etQty.getText().toString()) > totalPickedCount-pickedCount)
                        {
                            // WAPG001008   單據[%s],順序[%s]揀貨數量[%s]不可大於單據數量[%s]
                            ShowMessage(R.string.WAPG001008,
                                    holder.SheetId.getText().toString(),
                                    holder.Seq.getText().toString(),
                                    (Double.parseDouble(etQty.getText().toString())),
                                    totalPickedCount - pickedCount);
                            return;
                        }
                        break;

                    default:
                        break;
                }

                RegisterObj reg = filterReg.get(0);
                String binId = reg.BinId;
                String lotId = reg.RegisterId;
                String qty = etQty.getText().toString();
                String storageId = reg.StorageId;
                String uom = filterReg.get(0).ItemUom;
                String tempBin = cmbBin.getSelectedItem().toString();

                HashMap<String, String> mapBin = new HashMap<>();
                mapBin.put(storageId, tempBin);

                ExecutePicked(binId, lotId, qty, storageId, uom, 1, mapBin);

                if (Double.compare(Double.parseDouble(qty), Double.parseDouble(String.valueOf(reg.Qty))) == 0) {
                    for (RegisterObj obj : registerData)
                    {
                        if (obj.RegisterId.equals(filterReg.get(0).RegisterId))
                        {
                            obj.Selected = true;
                        }
                    }
                }
                dialog.dismiss();
            }
        });

        Button bCancel = dialogView.findViewById(R.id.btnCancel);
        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

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
