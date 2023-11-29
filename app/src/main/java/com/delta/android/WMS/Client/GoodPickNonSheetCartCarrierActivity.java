package com.delta.android.WMS.Client;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseFlowActivity;
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
import com.delta.android.WMS.Client.GridAdapter.GoodPickNonSheetGridAdapter;
import com.delta.android.WMS.Param.BIFetchPickStrategyParam;
import com.delta.android.WMS.Param.BIFetchProcessSheetParam;
import com.delta.android.WMS.Param.BIPDANoSheetCarrierPortalParam;
import com.delta.android.WMS.Param.BIPDANoSheetCartPortalParam;
import com.delta.android.WMS.Param.BIPDANoSheetPickCartPortalParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.ParamObj.Condition;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import static com.delta.android.Core.Common.Global.getContext;

public class GoodPickNonSheetCartCarrierActivity extends BaseFlowActivity {

    private ViewHolder holder = null;
    private DataTable _dtReg;
    private DataTable dtMaster;
    private DataTable dtDetail;

    private DataTable dtConfigCond = null;
    private DataTable dtConfigSort = null;
    private String strConfigCond = null;
    private String strConfigSort = null;
    private String _strSraechId = ""; //最終處理的單據

    ArrayList<String> lstSeq = null;
    HashMap<String, String> mapSeq = new HashMap<String, String>();
    ArrayList<String> lstSheetId = new ArrayList<>();

    static class ViewHolder
    {
//        EditText EtSheetId;
        Spinner cmbSheetId;
        //EditText EtRegister;
        EditText EtItemID;
        EditText EtPickQty;

        Spinner cmbSeq;

        ListView lvRegister;
        EditText EtSelectLotID;

        EditText EtNextStep;

        ImageButton IbtnSheetId;
        //ImageButton IbtnSearch;
        ImageButton IbtnQtySearch;
        //ImageButton IbtnItemSearch;

        Button BtnConfirm;
        TextView TvCartID;
        TextView TvCarrierID;
        TextView TvLotID;
        TextView TvBlockID;
        TextView TvFromBinID;
        TextView TvToBinID;
        LinearLayout llCart;
        LinearLayout llCarrier;
        LinearLayout llLot;
        LinearLayout llBlock;
        LinearLayout llFromBin;
        LinearLayout llToBin;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_good_pick_non_sheet_cart_carrier);

        InitialSetup();

        // 載入單據代碼至下拉選單
        GetSheetId();

        // 設定監聽事件
        setListensers();
    }

    private void InitialSetup() {
        if(holder != null) return;

        holder = new ViewHolder();
        //holder.EtRegister = findViewById(R.id.etLotId);
//        holder.EtSheetId = findViewById(R.id.etSheetId);
        holder.cmbSheetId = findViewById(R.id.cmbSheetId);
        holder.EtItemID = findViewById(R.id.etItemID);
        holder.EtPickQty = findViewById(R.id.etPickQty);

        holder.cmbSeq = findViewById(R.id.cmbSeq);

        holder.lvRegister = findViewById(R.id.lvRegisters);
        holder.EtSelectLotID = findViewById(R.id.etSelectLotId);

        //holder.IbtnSearch = findViewById(R.id.IbtnSearch);
        holder.IbtnSheetId = findViewById(R.id.ibtnSheetIdSearch);
        holder.IbtnQtySearch = findViewById(R.id.IbtnQtySearch);
        //holder.IbtnItemSearch = findViewById(R.id.IbtnItemSearch);
        holder.BtnConfirm = findViewById(R.id.btnConfirm);
        holder.TvCartID = findViewById(R.id.tvCartID);
        holder.TvFromBinID = findViewById(R.id.tvFromBinID);
        holder.TvToBinID = findViewById(R.id.tvToBinID);
        holder.TvCarrierID = findViewById(R.id.tvCarrierID);
        holder.TvLotID = findViewById(R.id.tvLotID);
        holder.TvBlockID = findViewById(R.id.tvBlockID);
        holder.EtNextStep = findViewById(R.id.etNextStep);
        holder.llCart = findViewById(R.id.llCart);
        holder.llCarrier = findViewById(R.id.llCarrier);
        holder.llLot = findViewById(R.id.llLot);
        holder.llBlock = findViewById(R.id.llBlock);
        holder.llFromBin = findViewById(R.id.llFromBin);
        holder.llToBin = findViewById(R.id.llToBin);
    }

    private void setListensers() {
        //holder.EtRegister.setOnKeyListener(LotIdOnKey);
        //holder.IbtnSearch.setOnClickListener(IbtnSearchOnClick);
        //holder.IbtnItemSearch.setOnClickListener(IbtnItemSearchClick);
        holder.IbtnSheetId.setOnClickListener(lsSheetIdFetch);
        holder.cmbSeq.setOnItemSelectedListener(lstSeqSelected);
        holder.IbtnQtySearch.setOnClickListener(lsRegisterFetch);
        holder.lvRegister.setOnItemClickListener(lvRegisterClick);
        holder.BtnConfirm.setOnClickListener(BtnConfirmOnClick);
    }

    //region 事件
    /*
    private View.OnKeyListener LotIdOnKey = new View.OnKeyListener(){
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event){
            //只有按下Enter才會反映
            if(keyCode != KeyEvent.KEYCODE_ENTER) return false;
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(v.getWindowToken(), 0);// 隱藏虛擬鍵盤
                GetData();
                return true;
            }
            return  false;
        }
    };
     */

    /*
    private View.OnClickListener IbtnSearchOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v){
            if(holder.EtRegister.getText().toString().equals("")){
                ShowMessage(R.string.WAPG016001); //WAPG016001 請輸入批號
                return;
            }

            if(holder.EtPickQty.getText().toString().equals("")){
                ShowMessage(R.string.WAPG016003); //WAPG016003 請輸入數量
                return;
            }

            FetchLotInfo("LOT", holder.EtRegister.getText().toString(), holder.EtPickQty.getText().toString());
        }
    };
    */

    /*
    private View.OnClickListener IbtnItemSearchClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(holder.EtItemID.getText().toString().equals("")){
                ShowMessage(R.string.WAPG016002); //WAPG016002 請輸入料號
                return;
            }

            if(holder.EtPickQty.getText().toString().equals("")){
                ShowMessage(R.string.WAPG016003); //WAPG016003 請輸入數量
                return;
            }

            FetchLotInfo("ITEM", holder.EtItemID.getText().toString(), holder.EtPickQty.getText().toString());
        }
    };
    */

    private View.OnClickListener lsSheetIdFetch = new View.OnClickListener(){
        @Override
        public void onClick(View veiw)
        {
            /*
            if(holder.EtSheetId.getText().toString().equals("")){
                //WAPG016006    請輸入單據代碼
                ShowMessage(R.string.WAPG016006);
                return;
            }
             */

            int sheetIdIndex = holder.cmbSheetId.getSelectedItemPosition();
            if (sheetIdIndex == (lstSheetId.size() - 1)) {
                ShowMessage(R.string.WAPG016006); //WAPG016006    請選擇單據代碼
                return;
            }

            String strSheetId = holder.cmbSheetId.getSelectedItem().toString().toUpperCase().trim();

            GetSht(strSheetId);
        }
    };

    private void GetSheetId() {
        // Call BIModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIFetchProcessSheet");
        bmObj.setModuleID("FetchProcessSheetByStatus");
        bmObj.setRequestID("FetchProcessSheetByStatus");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIFetchProcessSheetParam.ProcessType);
        param1.setParameterValue("PK");
        bmObj.params.add(param1);

        List<String> lstStatus = new ArrayList<>();
        lstStatus.add("Confirmed");
        VirtualClass vList = VirtualClass.create(VirtualClass.VirtualClassType.String);
        MList mList = new MList(vList);
        String strLstStatus = mList.generateFinalCode(lstStatus);
        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIFetchProcessSheetParam.LstStatus);
        param2.setNetParameterValue(strLstStatus);
        bmObj.params.add(param2);

        CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    lstSheetId.clear();
                    DataTable dt = bModuleReturn.getReturnJsonTables().get("FetchProcessSheetByStatus").get("DATA");
                    int pos = 0;
                    for (DataRow dr : dt.Rows) {
                        lstSheetId.add(pos, dr.getValue("SHEET_ID").toString());
                        pos++;
                    }
                    Collections.sort(lstSheetId); // List依據字母順序排序

                    // 下拉選單預設選項依語系調整
                    String strSelectSheetId = getResString(getResources().getString(R.string.SELECT_SHEET_ID));
                    lstSheetId.add(strSelectSheetId);

                    SimpleArrayAdapter adapter = new GoodPickNonSheetCartCarrierActivity.SimpleArrayAdapter<>(GoodPickNonSheetCartCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, lstSheetId);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    holder.cmbSheetId.setAdapter(adapter);
                    holder.cmbSheetId.setSelection(lstSheetId.size() - 1, true);
                }
            }
        });
    }

    private class SimpleArrayAdapter<T> extends ArrayAdapter {
        public SimpleArrayAdapter(Context context, int resource, List<T> objects) {
            super(context, resource, objects);
        }

        //複寫這個方法，使返回的數據沒有最後一項
        @Override
        public int getCount() {
            // don't display last item. It is used as hint.
            int count = super.getCount();
            return count > 0 ? count - 1 : count;
        }
    }

    private void FetchSheetInfo(String strSheetId){
//        String sheetId = holder.EtSheetId.getText().toString();
        if (strSheetId.equals(""))
        {
            _strSraechId = holder.cmbSheetId.getSelectedItem().toString().toUpperCase().trim();
        }
        else
        {
            _strSraechId = strSheetId;
        }

        BModuleObject bmObjSheet = new BModuleObject();
        bmObjSheet.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObjSheet.setModuleID("BIFetchSheetMstAndDet");
        bmObjSheet.setRequestID("BIFetchSheetMstAndDet");

        bmObjSheet.params = new Vector<ParameterInfo>();

        HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
        List<Condition> lstCondition1 = new ArrayList<Condition>();

        //SHEET_ID
        Condition conditionSheetId = new Condition();
        conditionSheetId.setAliasTable("M");
        conditionSheetId.setColumnName("SHEET_ID");
        conditionSheetId.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
//        conditionSheetId.setValue(holder.EtSheetId.getText().toString());
        conditionSheetId.setValue(strSheetId);
        lstCondition1.add(conditionSheetId);
        mapCondition.put(conditionSheetId.getColumnName(), lstCondition1);

        //Serialize序列化
        VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param" );
        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vkey, vVal);
        String strCond = msdl.generateFinalCode(mapCondition);

        //Input param
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Condition);
        param1.setNetParameterValue(strCond);
        bmObjSheet.params.add(param1);

        // region 取得ConfigCond及ConfigSort
        BModuleObject biShtCfgSortAndCond = new BModuleObject();
        biShtCfgSortAndCond.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        biShtCfgSortAndCond.setModuleID("BIFetchConfigCondAndSortBySheetId");
        biShtCfgSortAndCond.setRequestID("FetchConfigCondAndSort");
        biShtCfgSortAndCond.params = new Vector<ParameterInfo>();
        ParameterInfo paramShtId = new ParameterInfo();
        paramShtId.setParameterID(BIFetchPickStrategyParam.SheetId);
        paramShtId.setParameterValue(strSheetId);
        biShtCfgSortAndCond.params.add(paramShtId);
        // endregion

        List<BModuleObject> lstBmObj = new ArrayList<>();
        lstBmObj.add(bmObjSheet);
        lstBmObj.add(biShtCfgSortAndCond);

        CallBIModule(lstBmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(CheckBModuleReturnInfo(bModuleReturn)){
                    dtMaster = bModuleReturn.getReturnJsonTables().get("BIFetchSheetMstAndDet").get("Mst");
                    dtDetail = bModuleReturn.getReturnJsonTables().get("BIFetchSheetMstAndDet").get("Det");

                    dtConfigCond = bModuleReturn.getReturnJsonTables().get("FetchConfigCondAndSort").get("SheetConfigCond");
                    dtConfigSort = bModuleReturn.getReturnJsonTables().get("FetchConfigCondAndSort").get("SheetConfigSort");

                    if(dtMaster == null || dtMaster.Rows.size() <= 0 || dtDetail == null || dtDetail.Rows.size() <= 0)
                    {
                        //WAPG016007    查詢單據資料錯誤
                        ShowMessage(R.string.WAPG016007);
                        return;
                    }

                    lstSeq = new ArrayList<String>();
                    for(DataRow dr : dtDetail.Rows){
                        Double doubleSeq = Double.parseDouble(dr.getValue("SEQ").toString());
                        Integer intSeq = Integer.valueOf(doubleSeq.intValue());

                        lstSeq.add(intSeq.toString());
                        mapSeq.put(intSeq.toString(), dr.getValue("SEQ").toString());
                    }

                    ArrayAdapter<String> adapterSeq = new ArrayAdapter<>(GoodPickNonSheetCartCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, lstSeq);
                    holder.cmbSeq.setAdapter(adapterSeq);
                }
            }
        });
    }

    private Spinner.OnItemSelectedListener lstSeqSelected = new Spinner.OnItemSelectedListener(){
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l){
            MapSeqItem();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView){}
    };

    private void MapSeqItem(){

        DataRow selectedRow = null;

        String seq = mapSeq.get(holder.cmbSeq.getSelectedItem().toString());
        for(DataRow dr: dtDetail.Rows) {
            if(dr.getValue("SEQ").toString().equals(seq)){
                holder.EtItemID.setText(dr.getValue("ITEM_ID").toString());
                selectedRow = dr;
                break;
            }
        }

        strConfigCond = generateExtendConfigCond(selectedRow, dtConfigCond);
        strConfigSort = generateExtendConfigSort(dtConfigSort);
    }

    private View.OnClickListener lsRegisterFetch = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            if(holder.EtItemID.getText().toString().equals("")){
                //WAPG016002    請輸入料號
                ShowMessage(R.string.WAPG016002);
                return;
            }

            String strStorageId = "";

            if (dtDetail.Rows.size() > 0)
            {
                for (DataRow dr : dtDetail.Rows)
                {
                    if (Double.parseDouble(dr.getValue("SEQ").toString()) == Double.parseDouble(holder.cmbSeq.getSelectedItem().toString()))
                    {
                        strStorageId = dr.getValue("FROM_STORAGE_ID").toString();
                    }
                }
            }

            FetchLotInfo("ITEM", holder.EtItemID.getText().toString(), holder.EtPickQty.getText().toString(), strStorageId);
        }
    };

    private AdapterView.OnItemClickListener lvRegisterClick = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int positon, long id)
        {
            holder.EtSelectLotID.setText(_dtReg.Rows.get(positon).getValue("REGISTER_ID").toString());
            GetData(positon);
        }
    };

    private View.OnClickListener BtnConfirmOnClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            ProcessExecute();
        }
    };
    //endregion

   private void FetchLotInfo(String type, String value, String qty, String strStorageId)
   {
       //找出Register
       BModuleObject bmObjLot = new BModuleObject();
       bmObjLot.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDANoSheetPickCartPortal");
       bmObjLot.setModuleID("BIGetRegData");
       bmObjLot.setRequestID("BIGetRegData");

       bmObjLot.params = new Vector<ParameterInfo>();

       if (type.equals("ITEM"))
       {
           ParameterInfo paramItem = new ParameterInfo();
           paramItem.setParameterID(BIPDANoSheetPickCartPortalParam.Item);
           paramItem.setParameterValue(value);
           bmObjLot.params.add(paramItem);
       }
       else
       {
           ParameterInfo paramLot = new ParameterInfo();
           paramLot.setParameterID(BIPDANoSheetPickCartPortalParam.SeachLot);
           paramLot.setParameterValue(value);
           bmObjLot.params.add(paramLot);
       }

       ParameterInfo paramQty = new ParameterInfo();
       paramQty.setParameterID(BIPDANoSheetPickCartPortalParam.Qty);
       paramQty.setParameterValue(qty);
       bmObjLot.params.add(paramQty);

       ParameterInfo paramType = new ParameterInfo();
       paramType.setParameterID(BIPDANoSheetPickCartPortalParam.SeachType);
       paramType.setParameterValue(type);
       bmObjLot.params.add(paramType);

       ParameterInfo paramStorage = new ParameterInfo();
       paramStorage.setParameterID(BIPDANoSheetPickCartPortalParam.StorageId);
       paramStorage.setParameterValue(strStorageId);
       bmObjLot.params.add(paramStorage);

       if (strConfigCond != null && strConfigCond.length() > 0) {
           ParameterInfo paramCond = new ParameterInfo();
           paramCond.setParameterID(BIPDANoSheetPickCartPortalParam.ConfigCond);
           paramCond.setParameterValue(strConfigCond);
           bmObjLot.params.add(paramCond);
       }

       if (strConfigSort != null && strConfigSort.length() > 0) {
           ParameterInfo paramSort = new ParameterInfo();
           paramSort.setParameterID(BIPDANoSheetPickCartPortalParam.ConfigSort);
           paramSort.setParameterValue(strConfigSort);
           bmObjLot.params.add(paramSort);
       }

       CallBIModule(bmObjLot, new WebAPIClientEvent() {
           @Override
           public void onPostBack(BModuleReturn bModuleReturn) {
               if(CheckBModuleReturnInfo(bModuleReturn))
               {
                   _dtReg = bModuleReturn.getReturnJsonTables().get("BIGetRegData").get("Register");

                   LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                   GoodPickNonSheetGridAdapter adapter = new GoodPickNonSheetGridAdapter(_dtReg, inflater);
                   holder.lvRegister.setAdapter(adapter);
                   adapter.notifyDataSetChanged();
               }
           }
       });
   }

   private void GetData()
   {
        if(holder.EtSelectLotID.getText().toString().equals("")){
            //WAPG016001 請輸入批號
            ShowMessage(R.string.WAPG016001);
            return;
        }

       // BIModule
       BModuleObject bimObj = new BModuleObject();
       bimObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDANoSheetPickCartPortal");
       bimObj.setModuleID("BIGetRegisterXfr");
       bimObj.setRequestID("BIGetRegisterXfr");
       bimObj.params = new Vector<ParameterInfo>();

       // Input param
       ParameterInfo param1 = new ParameterInfo();
       param1.setParameterID(BIPDANoSheetPickCartPortalParam.Lot);
       param1.setParameterValue(holder.EtSelectLotID.getText().toString());
       bimObj.params.add(param1);
       // endregion

       CallBIModule(bimObj, new WebAPIClientEvent() {
           @Override
           public void onPostBack(BModuleReturn bModuleReturn)
           {
               if (CheckBModuleReturnInfo(bModuleReturn))
               {
                   _dtReg = bModuleReturn.getReturnJsonTables().get("BIGetRegisterXfr").get("PDA");

                   double step = Double.parseDouble(_dtReg.Rows.get(0).getValue("STEP").toString());
                   //指定tab,觸發切換事件

                   holder.EtNextStep.setText(_dtReg.Rows.get(0).getValue("NEXT_STEP").toString());

                   switch(_dtReg.Rows.get(0).getValue("NEXT_STEP").toString())
                   {
                       case "CartStockOut":
                           holder.llCart.setVisibility(View.VISIBLE);
                           holder.llCarrier.setVisibility(View.GONE);
                           holder.llLot.setVisibility(View.GONE);
                           holder.llBlock.setVisibility(View.GONE);
                           holder.llFromBin.setVisibility(View.VISIBLE);
                           holder.llToBin.setVisibility(View.VISIBLE);

                           holder.TvCartID.setText(_dtReg.Rows.get(0).getValue("CART_ID").toString());
                           holder.TvFromBinID.setText(_dtReg.Rows.get(0).getValue("CART_PORT").toString());
                           holder.TvToBinID.setText(_dtReg.Rows.get(0).getValue("OS_PORT").toString());
                           break;

                       case "CarrierStockOut":
                           holder.llCart.setVisibility(View.GONE);
                           holder.llCarrier.setVisibility(View.VISIBLE);
                           holder.llLot.setVisibility(View.GONE);
                           holder.llBlock.setVisibility(View.GONE);
                           holder.llFromBin.setVisibility(View.VISIBLE);
                           holder.llToBin.setVisibility(View.VISIBLE);

                           holder.TvCarrierID.setText(_dtReg.Rows.get(0).getValue("CARRIER_ID").toString());
                           holder.TvFromBinID.setText(_dtReg.Rows.get(0).getValue("CARRIER_PORT").toString());
                           holder.TvToBinID.setText(_dtReg.Rows.get(0).getValue("OS_PORT").toString());
                           break;

                       case "CarrierRegisterUnBind":
                           holder.llCart.setVisibility(View.GONE);
                           holder.llCarrier.setVisibility(View.GONE);
                           holder.llLot.setVisibility(View.VISIBLE);
                           holder.llBlock.setVisibility(View.VISIBLE);
                           holder.llFromBin.setVisibility(View.VISIBLE);
                           holder.llToBin.setVisibility(View.VISIBLE);

                           holder.TvLotID.setText(_dtReg.Rows.get(0).getValue("LOT_ID").toString());
                           holder.TvBlockID.setText(_dtReg.Rows.get(0).getValue("BLOCK_ID").toString());
                           holder.TvFromBinID.setText(_dtReg.Rows.get(0).getValue("OS_PORT").toString());
                           holder.TvToBinID.setText(_dtReg.Rows.get(0).getValue("OS_PORT").toString());
                           break;

                       case "CarrierStockIn":
                           holder.llCart.setVisibility(View.GONE);
                           holder.llCarrier.setVisibility(View.VISIBLE);
                           holder.llLot.setVisibility(View.GONE);
                           holder.llBlock.setVisibility(View.GONE);
                           holder.llFromBin.setVisibility(View.VISIBLE);
                           holder.llToBin.setVisibility(View.VISIBLE);

                           holder.TvCarrierID.setText(_dtReg.Rows.get(0).getValue("CARRIER_ID").toString());
                           holder.TvFromBinID.setText(_dtReg.Rows.get(0).getValue("OS_PORT").toString());
                           holder.TvToBinID.setText(_dtReg.Rows.get(0).getValue("CARR_FIX_PORT").toString());
                           break;

                       case "CartStockIn":
                           holder.llCart.setVisibility(View.VISIBLE);
                           holder.llCarrier.setVisibility(View.GONE);
                           holder.llLot.setVisibility(View.GONE);
                           holder.llBlock.setVisibility(View.GONE);
                           holder.llFromBin.setVisibility(View.VISIBLE);
                           holder.llToBin.setVisibility(View.VISIBLE);

                           holder.TvCarrierID.setText(_dtReg.Rows.get(0).getValue("CART_ID").toString());
                           holder.TvFromBinID.setText(_dtReg.Rows.get(0).getValue("OS_PORT").toString());
                           holder.TvToBinID.setText(_dtReg.Rows.get(0).getValue("CART_FIX_PORT").toString());
                           break;
                   }
               }
           }
       });
   }

    private void GetData(int pos)
    {
        if(_dtReg == null || _dtReg.Rows.size() <= 0) return;

        if(holder.EtSelectLotID.getText().toString().equals("")){
            //WAPG016001 請輸入批號
            ShowMessage(R.string.WAPG016001);
            return;
        }

        String strStorageId = _dtReg.Rows.get(pos).getValue("STORAGE_ID").toString();

        // BIModule
        BModuleObject bimObj = new BModuleObject();
        bimObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDANoSheetPickCartPortal");
        bimObj.setModuleID("BIGetRegisterXfr");
        bimObj.setRequestID("BIGetRegisterXfr");
        bimObj.params = new Vector<ParameterInfo>();

        // Input param
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIPDANoSheetPickCartPortalParam.Lot);
        param1.setParameterValue(holder.EtSelectLotID.getText().toString());
        bimObj.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIPDANoSheetPickCartPortalParam.Stock);
        param2.setParameterValue("OUT");
        bimObj.params.add(param2);

        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BIPDANoSheetPickCartPortalParam.StorageId);
        param3.setParameterValue(strStorageId);
        bimObj.params.add(param3);
        // endregion

        CallBIModule(bimObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn)
            {
                if (CheckBModuleReturnInfo(bModuleReturn))
                {
                    _dtReg = bModuleReturn.getReturnJsonTables().get("BIGetRegisterXfr").get("PDA");

                    double step = Double.parseDouble(_dtReg.Rows.get(0).getValue("STEP").toString());
                    //指定tab,觸發切換事件

                    holder.EtNextStep.setText(_dtReg.Rows.get(0).getValue("NEXT_STEP").toString());

                    switch(_dtReg.Rows.get(0).getValue("NEXT_STEP").toString())
                    {
                        case "CartStockOut":
                            holder.llCart.setVisibility(View.VISIBLE);
                            holder.llCarrier.setVisibility(View.GONE);
                            holder.llLot.setVisibility(View.GONE);
                            holder.llBlock.setVisibility(View.GONE);
                            holder.llFromBin.setVisibility(View.VISIBLE);
                            holder.llToBin.setVisibility(View.VISIBLE);

                            holder.TvCartID.setText(_dtReg.Rows.get(0).getValue("CART_ID").toString());
                            holder.TvFromBinID.setText(_dtReg.Rows.get(0).getValue("CART_PORT").toString());
                            holder.TvToBinID.setText(_dtReg.Rows.get(0).getValue("OS_PORT").toString());
                            break;

                        case "CarrierStockOut":
                            holder.llCart.setVisibility(View.GONE);
                            holder.llCarrier.setVisibility(View.VISIBLE);
                            holder.llLot.setVisibility(View.GONE);
                            holder.llBlock.setVisibility(View.GONE);
                            holder.llFromBin.setVisibility(View.VISIBLE);
                            holder.llToBin.setVisibility(View.VISIBLE);

                            holder.TvCarrierID.setText(_dtReg.Rows.get(0).getValue("CARRIER_ID").toString());
                            holder.TvFromBinID.setText(_dtReg.Rows.get(0).getValue("CARRIER_PORT").toString());
                            holder.TvToBinID.setText(_dtReg.Rows.get(0).getValue("OS_PORT").toString());
                            break;

                        case "CarrierRegisterUnBind":
                            holder.llCart.setVisibility(View.GONE);
                            holder.llCarrier.setVisibility(View.GONE);
                            holder.llLot.setVisibility(View.VISIBLE);
                            holder.llBlock.setVisibility(View.VISIBLE);
                            holder.llFromBin.setVisibility(View.VISIBLE);
                            holder.llToBin.setVisibility(View.VISIBLE);

                            holder.TvLotID.setText(_dtReg.Rows.get(0).getValue("LOT_ID").toString());
                            holder.TvBlockID.setText(_dtReg.Rows.get(0).getValue("BLOCK_ID").toString());
                            holder.TvFromBinID.setText(_dtReg.Rows.get(0).getValue("OS_PORT").toString());
                            holder.TvToBinID.setText(_dtReg.Rows.get(0).getValue("OS_PORT").toString());
                            break;

                        case "CarrierStockIn":
                            holder.llCart.setVisibility(View.GONE);
                            holder.llCarrier.setVisibility(View.VISIBLE);
                            holder.llLot.setVisibility(View.GONE);
                            holder.llBlock.setVisibility(View.GONE);
                            holder.llFromBin.setVisibility(View.VISIBLE);
                            holder.llToBin.setVisibility(View.VISIBLE);

                            holder.TvCarrierID.setText(_dtReg.Rows.get(0).getValue("CARRIER_ID").toString());
                            holder.TvFromBinID.setText(_dtReg.Rows.get(0).getValue("OS_PORT").toString());
                            holder.TvToBinID.setText(_dtReg.Rows.get(0).getValue("CARRIER_PORT").toString());
                            break;

                        case "CartStockIn":
                            holder.llCart.setVisibility(View.VISIBLE);
                            holder.llCarrier.setVisibility(View.GONE);
                            holder.llLot.setVisibility(View.GONE);
                            holder.llBlock.setVisibility(View.GONE);
                            holder.llFromBin.setVisibility(View.VISIBLE);
                            holder.llToBin.setVisibility(View.VISIBLE);

                            holder.TvCarrierID.setText(_dtReg.Rows.get(0).getValue("CART_ID").toString());
                            holder.TvFromBinID.setText(_dtReg.Rows.get(0).getValue("OS_PORT").toString());
                            holder.TvToBinID.setText(_dtReg.Rows.get(0).getValue("CART_FIX_PORT").toString());
                            break;
                    }
                }
            }
        });
    }

    private void ProcessExecute(){
        if(_dtReg == null || _dtReg.Rows.size() <= 0) return;

        boolean bCart = false;
        final String strStep = holder.EtNextStep.getText().toString();
        String strSheetId = _strSraechId;
        String strFromPort = "";
        String strToPort = "";
        String strObject = "";
        String strRegId = _dtReg.Rows.get(0).getValue("LOT_ID").toString();
        String strStorageId = _dtReg.Rows.get(0).getValue("STORAGE_ID").toString();
        String strHaveCarrier = _dtReg.Rows.get(0).getValue("HAVE_CARR").toString();
        String strCarrier = _dtReg.Rows.get(0).getValue("CARRIER_ID").toString();
        String strBlock = _dtReg.Rows.get(0).getValue("BLOCK_ID").toString();
        String strRegLoc = _dtReg.Rows.get(0).getValue("LOCATION").toString();

        BModuleObject bimCartObj = new BModuleObject();
        bimCartObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDANoSheetPickCartPortal");
        bimCartObj.setModuleID("BIRegisterXfrConfirm");
        bimCartObj.setRequestID("BIRegisterXfrConfirm");
        bimCartObj.params = new Vector<ParameterInfo>();

        BModuleObject bimCarrierObj = new BModuleObject();
        bimCarrierObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDANoSheetCarrierPortal");
        bimCarrierObj.setModuleID("BIRegisterXfrConfirm");
        bimCarrierObj.setRequestID("BIRegisterXfrConfirm");
        bimCarrierObj.params = new Vector<ParameterInfo>();

        switch (strStep)
        {
            case "CartStockOut":
                strFromPort = _dtReg.Rows.get(0).getValue("CART_PORT").toString();
                strToPort = _dtReg.Rows.get(0).getValue("OS_PORT").toString();
                strObject = _dtReg.Rows.get(0).getValue("CART_ID").toString();
                bCart = true;
                break;

            case "CarrierStockOut":
                strFromPort = _dtReg.Rows.get(0).getValue("CARRIER_PORT").toString();
                strToPort = _dtReg.Rows.get(0).getValue("OS_PORT").toString();
                strObject = _dtReg.Rows.get(0).getValue("CARRIER_ID").toString();
                break;

            case "CarrierRegisterUnBind":
                strFromPort = _dtReg.Rows.get(0).getValue("OS_PORT").toString();
                strToPort = _dtReg.Rows.get(0).getValue("OS_PORT").toString();
                strObject = _dtReg.Rows.get(0).getValue("LOT_ID").toString();
                break;

            case "CarrierStockIn":
                strFromPort = _dtReg.Rows.get(0).getValue("OS_PORT").toString();
                strToPort = _dtReg.Rows.get(0).getValue("CARRIER_PORT").toString();
                strObject = _dtReg.Rows.get(0).getValue("CARRIER_ID").toString();
                break;

            case "CartStockIn":
                strFromPort = _dtReg.Rows.get(0).getValue("OS_PORT").toString();
                strToPort = _dtReg.Rows.get(0).getValue("CART_FIX_PORT").toString();
                strObject = _dtReg.Rows.get(0).getValue("CART_ID").toString();
                bCart = true;
                break;
        }

        if(bCart)
        {
            ParameterInfo paramReg = new ParameterInfo();
            paramReg.setParameterID(BIPDANoSheetPickCartPortalParam.Lot);
            paramReg.setParameterValue(strRegId);
            bimCartObj.params.add(paramReg);

            ParameterInfo paramXfrCase = new ParameterInfo();
            paramXfrCase.setParameterID(BIPDANoSheetPickCartPortalParam.XfrCase);
            paramXfrCase.setParameterValue("CartCarrier");
            bimCartObj.params.add(paramXfrCase);

            ParameterInfo paramXfrTask = new ParameterInfo();
            paramXfrTask.setParameterID(BIPDANoSheetPickCartPortalParam.XfrTask);
            paramXfrTask.setParameterValue(strStep);
            bimCartObj.params.add(paramXfrTask);

            ParameterInfo paramFromPort = new ParameterInfo();
            paramFromPort.setParameterID(BIPDANoSheetPickCartPortalParam.FromPort);
            paramFromPort.setParameterValue(strFromPort);
            bimCartObj.params.add(paramFromPort);

            ParameterInfo paramToPort = new ParameterInfo();
            paramToPort.setParameterID(BIPDANoSheetPickCartPortalParam.ToPort);
            paramToPort.setParameterValue(strToPort);
            bimCartObj.params.add(paramToPort);

            ParameterInfo paramObjId = new ParameterInfo();
            paramObjId.setParameterID(BIPDANoSheetPickCartPortalParam.ObjectId);
            paramObjId.setParameterValue(strObject);
            bimCartObj.params.add(paramObjId);

            ParameterInfo paramStorage = new ParameterInfo();
            paramStorage.setParameterID(BIPDANoSheetPickCartPortalParam.StorageId);
            paramStorage.setParameterValue(strStorageId);
            bimCartObj.params.add(paramStorage);

            ParameterInfo paramLocation = new ParameterInfo();
            paramLocation.setParameterID(BIPDANoSheetPickCartPortalParam.RegLocation);
            paramLocation.setParameterValue(strRegLoc);
            bimCartObj.params.add(paramLocation);

            ParameterInfo paramQty = new ParameterInfo();
            paramQty.setParameterID(BIPDANoSheetPickCartPortalParam.Qty);
            paramQty.setParameterValue(holder.EtPickQty.getText().toString());
            bimCartObj.params.add(paramQty);

            ParameterInfo paramCarrier = new ParameterInfo();
            //paramCarrier.setParameterID(BIPDANoSheetCartPortalParam.Carrier);
            paramCarrier.setParameterID(BIPDANoSheetPickCartPortalParam.Carrier);
            paramCarrier.setParameterValue(strCarrier);
            bimCartObj.params.add(paramCarrier);

            ParameterInfo paramBlock = new ParameterInfo();
            //paramBlock.setParameterID(BIPDANoSheetCartPortalParam.Block);
            paramBlock.setParameterID(BIPDANoSheetPickCartPortalParam.Block);
            paramBlock.setParameterValue(strBlock);
            bimCartObj.params.add(paramBlock);

            ParameterInfo paramHaveCarrier = new ParameterInfo();
            //paramHaveCarrier.setParameterID(BIPDANoSheetCartPortalParam.HaveCarrier);
            paramHaveCarrier.setParameterID(BIPDANoSheetPickCartPortalParam.HaveCarrier);
            paramHaveCarrier.setParameterValue(strHaveCarrier);
            bimCartObj.params.add(paramHaveCarrier);

            ParameterInfo paramSheet = new ParameterInfo();
            paramSheet.setParameterID(BIPDANoSheetPickCartPortalParam.SheetId);
//            paramSheet.setParameterValue(holder.EtSheetId.getText().toString());
            paramSheet.setParameterValue(strSheetId);
            bimCartObj.params.add(paramSheet);

            ParameterInfo paramSeq = new ParameterInfo();
            paramSeq.setParameterID(BIPDANoSheetPickCartPortalParam.Seq);
            paramSeq.setParameterValue(holder.cmbSeq.getSelectedItem().toString());
            bimCartObj.params.add(paramSeq);

            ParameterInfo paramBestBin = new ParameterInfo();
            paramBestBin.setParameterID(BIPDANoSheetPickCartPortalParam.BestBin);
            paramBestBin.setParameterValue(_dtReg.Rows.get(0).getValue("OS_BIN_ID").toString());
            bimCartObj.params.add(paramBestBin);

            ParameterInfo paramItemId = new ParameterInfo();  // 20220812 Ikea 傳入 ItemId
            paramItemId.setParameterID(BIPDANoSheetPickCartPortalParam.Item);
            paramItemId.setParameterValue(holder.EtItemID.getText().toString());
            bimCartObj.params.add(paramItemId);

            ParameterInfo paramBinId = new ParameterInfo(); // 20220812 Ikea 傳入 BinId
            paramBinId.setParameterID(BIPDANoSheetPickCartPortalParam.OsBinId);
            paramBinId.setParameterValue(_dtReg.Rows.get(0).getValue("OS_BIN_ID").toString());
            bimCartObj.params.add(paramBinId);

            CallBIModule(bimCartObj, new WebAPIClientEvent(){
                @Override
                public void onPostBack(BModuleReturn bModuleReturn){
                    if (CheckBModuleReturnInfo(bModuleReturn))
                    {
                        //WAPG014003 作業成功
                        Toast.makeText(getContext(), R.string.WAPG014003, Toast.LENGTH_SHORT).show();

                        if(strStep.equals("CartStockIn"))
                            Clear();
                        else
                        {
                            switch (strStep)
                            {
                                case "CartStockOut":
                                    holder.EtNextStep.setText("CarrierStockOut");
                                    holder.llCart.setVisibility(View.GONE);
                                    holder.llCarrier.setVisibility(View.VISIBLE);
                                    holder.llLot.setVisibility(View.GONE);
                                    holder.llBlock.setVisibility(View.GONE);
                                    holder.llFromBin.setVisibility(View.VISIBLE);
                                    holder.llToBin.setVisibility(View.VISIBLE);

                                    holder.TvCarrierID.setText(_dtReg.Rows.get(0).getValue("CARRIER_ID").toString());
                                    holder.TvFromBinID.setText(_dtReg.Rows.get(0).getValue("CARRIER_PORT").toString());
                                    holder.TvToBinID.setText(_dtReg.Rows.get(0).getValue("OS_PORT").toString());
                                    break;

                                case "CarrierStockOut":
                                    holder.EtNextStep.setText("CarrierRegisterBind");
                                    holder.llCart.setVisibility(View.GONE);
                                    holder.llCarrier.setVisibility(View.GONE);
                                    holder.llLot.setVisibility(View.VISIBLE);
                                    holder.llBlock.setVisibility(View.VISIBLE);
                                    holder.llFromBin.setVisibility(View.VISIBLE);
                                    holder.llToBin.setVisibility(View.VISIBLE);

                                    holder.TvLotID.setText(_dtReg.Rows.get(0).getValue("LOT_ID").toString());
                                    holder.TvBlockID.setText(_dtReg.Rows.get(0).getValue("BLOCK_ID").toString());
                                    holder.TvFromBinID.setText(_dtReg.Rows.get(0).getValue("OS_PORT").toString());
                                    holder.TvToBinID.setText(_dtReg.Rows.get(0).getValue("OS_PORT").toString());
                                    break;

                                case "CarrierRegisterUnBind":
                                    holder.EtNextStep.setText("CarrierStockIn");
                                    holder.llCart.setVisibility(View.GONE);
                                    holder.llCarrier.setVisibility(View.VISIBLE);
                                    holder.llLot.setVisibility(View.GONE);
                                    holder.llBlock.setVisibility(View.GONE);
                                    holder.llFromBin.setVisibility(View.VISIBLE);
                                    holder.llToBin.setVisibility(View.VISIBLE);

                                    holder.TvCarrierID.setText(_dtReg.Rows.get(0).getValue("CARRIER_ID").toString());
                                    holder.TvFromBinID.setText(_dtReg.Rows.get(0).getValue("OS_PORT").toString());
                                    holder.TvToBinID.setText(_dtReg.Rows.get(0).getValue("CARR_FIX_PORT").toString());
                                    break;

                                case "CarrierStockIn":
                                    holder.EtNextStep.setText("CartStockIn");
                                    holder.llCart.setVisibility(View.VISIBLE);
                                    holder.llCarrier.setVisibility(View.GONE);
                                    holder.llLot.setVisibility(View.GONE);
                                    holder.llBlock.setVisibility(View.GONE);
                                    holder.llFromBin.setVisibility(View.VISIBLE);
                                    holder.llToBin.setVisibility(View.VISIBLE);

                                    holder.TvCarrierID.setText(_dtReg.Rows.get(0).getValue("CART_ID").toString());
                                    holder.TvFromBinID.setText(_dtReg.Rows.get(0).getValue("OS_PORT").toString());
                                    holder.TvToBinID.setText(_dtReg.Rows.get(0).getValue("CART_FIX_PORT").toString());
                                    break;
                            }
                        }
                    }
                }
            });
        }
        else
        {
            ParameterInfo paramReg = new ParameterInfo();
            paramReg.setParameterID(BIPDANoSheetCarrierPortalParam.RegisterId);
            paramReg.setParameterValue(strRegId);
            bimCarrierObj.params.add(paramReg);

            ParameterInfo paramXfrCase = new ParameterInfo();
            paramXfrCase.setParameterID(BIPDANoSheetCarrierPortalParam.XfrCase);
            paramXfrCase.setParameterValue("CartCarrier");
            bimCarrierObj.params.add(paramXfrCase);

            ParameterInfo paramXfrTask = new ParameterInfo();
            paramXfrTask.setParameterID(BIPDANoSheetCarrierPortalParam.XfrTask);
            paramXfrTask.setParameterValue(strStep);
            bimCarrierObj.params.add(paramXfrTask);

            ParameterInfo paramFromPort = new ParameterInfo();
            paramFromPort.setParameterID(BIPDANoSheetCarrierPortalParam.FromPort);
            paramFromPort.setParameterValue(strFromPort);
            bimCarrierObj.params.add(paramFromPort);

            ParameterInfo paramToPort = new ParameterInfo();
            paramToPort.setParameterID(BIPDANoSheetCarrierPortalParam.ToPort);
            paramToPort.setParameterValue(strToPort);
            bimCarrierObj.params.add(paramToPort);

            ParameterInfo paramObjId = new ParameterInfo();
            paramObjId.setParameterID(BIPDANoSheetCarrierPortalParam.ObjectId);
            paramObjId.setParameterValue(strObject);
            bimCarrierObj.params.add(paramObjId);

            ParameterInfo paramStorage = new ParameterInfo();
            paramStorage.setParameterID(BIPDANoSheetCarrierPortalParam.StorageId);
            paramStorage.setParameterValue(strStorageId);
            bimCarrierObj.params.add(paramStorage);

            ParameterInfo paramCarrier = new ParameterInfo();
            paramCarrier.setParameterID(BIPDANoSheetCarrierPortalParam.Carrier);
            paramCarrier.setParameterValue(strCarrier);
            bimCarrierObj.params.add(paramCarrier);

            ParameterInfo paramBlock = new ParameterInfo();
            paramBlock.setParameterID(BIPDANoSheetCarrierPortalParam.Block);
            paramBlock.setParameterValue(strBlock);
            bimCarrierObj.params.add(paramBlock);

            ParameterInfo paramBestBin = new ParameterInfo();
            paramBestBin.setParameterID(BIPDANoSheetCarrierPortalParam.BestBin);
            paramBestBin.setParameterValue(_dtReg.Rows.get(0).getValue("OS_BIN_ID").toString());
            bimCarrierObj.params.add(paramBestBin);

            ParameterInfo paramStock = new ParameterInfo();
            paramStock.setParameterID(BIPDANoSheetCarrierPortalParam.stock);
            paramStock.setParameterValue("O");
            bimCarrierObj.params.add(paramStock);

            ParameterInfo paramRegQty = new ParameterInfo();
            paramRegQty.setParameterID(BIPDANoSheetCarrierPortalParam.RegQty);
            paramRegQty.setParameterValue(holder.EtPickQty.getText().toString());
            bimCarrierObj.params.add(paramRegQty);

            ParameterInfo paramSheet = new ParameterInfo();
            paramSheet.setParameterID(BIPDANoSheetCarrierPortalParam.SheetId);
//            paramSheet.setParameterValue(holder.EtSheetId.getText().toString());
            paramSheet.setParameterValue(strSheetId);
            bimCarrierObj.params.add(paramSheet);

            ParameterInfo paramSeq = new ParameterInfo();
            paramSeq.setParameterID(BIPDANoSheetCarrierPortalParam.Seq);
            paramSeq.setParameterValue(holder.cmbSeq.getSelectedItem().toString());
            bimCarrierObj.params.add(paramSeq);

            ParameterInfo paramItemId = new ParameterInfo();  // 20220812 Ikea 傳入 ItemId
            paramItemId.setParameterID(BIPDANoSheetCarrierPortalParam.ItemId);
            paramItemId.setParameterValue(holder.EtItemID.getText().toString());
            bimCarrierObj.params.add(paramItemId);

            ParameterInfo paramBinId = new ParameterInfo(); // 20220812 Ikea 傳入 BinId
            paramBinId.setParameterID(BIPDANoSheetCarrierPortalParam.OsBinId);
            paramBinId.setParameterValue(_dtReg.Rows.get(0).getValue("OS_BIN_ID").toString());
            bimCarrierObj.params.add(paramBinId);

            CallBIModule(bimCarrierObj, new WebAPIClientEvent(){
                @Override
                public void onPostBack(BModuleReturn bModuleReturn){
                    if (CheckBModuleReturnInfo(bModuleReturn))
                    {
                        //WAPG014003 作業成功
                        Toast.makeText(getContext(), R.string.WAPG014003, Toast.LENGTH_SHORT).show();

                        if(strStep.equals("CartStockIn"))
                            Clear();
                        else
                        {
                            switch (strStep)
                            {
                                case "CartStockOut":
                                    holder.EtNextStep.setText("CarrierStockOut");
                                    holder.llCart.setVisibility(View.GONE);
                                    holder.llCarrier.setVisibility(View.VISIBLE);
                                    holder.llLot.setVisibility(View.GONE);
                                    holder.llBlock.setVisibility(View.GONE);
                                    holder.llFromBin.setVisibility(View.VISIBLE);
                                    holder.llToBin.setVisibility(View.VISIBLE);

                                    holder.TvCarrierID.setText(_dtReg.Rows.get(0).getValue("CARRIER_ID").toString());
                                    holder.TvFromBinID.setText(_dtReg.Rows.get(0).getValue("CARRIER_PORT").toString());
                                    holder.TvToBinID.setText(_dtReg.Rows.get(0).getValue("OS_PORT").toString());
                                    break;

                                case "CarrierStockOut":
                                    holder.EtNextStep.setText("CarrierRegisterUnBind");
                                    holder.llCart.setVisibility(View.GONE);
                                    holder.llCarrier.setVisibility(View.GONE);
                                    holder.llLot.setVisibility(View.VISIBLE);
                                    holder.llBlock.setVisibility(View.VISIBLE);
                                    holder.llFromBin.setVisibility(View.VISIBLE);
                                    holder.llToBin.setVisibility(View.VISIBLE);

                                    holder.TvLotID.setText(_dtReg.Rows.get(0).getValue("LOT_ID").toString());
                                    holder.TvBlockID.setText(_dtReg.Rows.get(0).getValue("BLOCK_ID").toString());
                                    holder.TvFromBinID.setText(_dtReg.Rows.get(0).getValue("OS_PORT").toString());
                                    holder.TvToBinID.setText(_dtReg.Rows.get(0).getValue("OS_PORT").toString());
                                    break;

                                case "CarrierRegisterUnBind":
                                    holder.EtNextStep.setText("CarrierStockIn");
                                    holder.llCart.setVisibility(View.GONE);
                                    holder.llCarrier.setVisibility(View.VISIBLE);
                                    holder.llLot.setVisibility(View.GONE);
                                    holder.llBlock.setVisibility(View.GONE);
                                    holder.llFromBin.setVisibility(View.VISIBLE);
                                    holder.llToBin.setVisibility(View.VISIBLE);

                                    holder.TvCarrierID.setText(_dtReg.Rows.get(0).getValue("CARRIER_ID").toString());
                                    holder.TvFromBinID.setText(_dtReg.Rows.get(0).getValue("OS_PORT").toString());
                                    holder.TvToBinID.setText(_dtReg.Rows.get(0).getValue("CARRIER_PORT").toString());
                                    break;

                                case "CarrierStockIn":
                                    holder.EtNextStep.setText("CartStockIn");
                                    holder.llCart.setVisibility(View.VISIBLE);
                                    holder.llCarrier.setVisibility(View.GONE);
                                    holder.llLot.setVisibility(View.GONE);
                                    holder.llBlock.setVisibility(View.GONE);
                                    holder.llFromBin.setVisibility(View.VISIBLE);
                                    holder.llToBin.setVisibility(View.VISIBLE);

                                    holder.TvCarrierID.setText(_dtReg.Rows.get(0).getValue("CART_ID").toString());
                                    holder.TvFromBinID.setText(_dtReg.Rows.get(0).getValue("OS_PORT").toString());
                                    holder.TvToBinID.setText(_dtReg.Rows.get(0).getValue("CART_FIX_PORT").toString());
                                    break;
                            }
                        }
                    }
                }
            });
        }
    }

    private void Clear(){
        //holder.EtRegister.setText("");
//        holder.EtSheetId.setText("");
        holder.cmbSheetId.setSelection(lstSheetId.size()-1); // spinner設定回預設選項
        holder.EtItemID.setText("");
        holder.EtPickQty.setText("");

        holder.EtNextStep.setText("");
        holder.TvCartID.setText("");
        holder.TvCarrierID.setText("");
        holder.TvLotID.setText("");
        holder.TvBlockID.setText("");
        holder.TvFromBinID.setText("");
        holder.TvToBinID.setText("");

        holder.EtSelectLotID.setText("");

        holder.llCart.setVisibility(View.VISIBLE);
        holder.llCarrier.setVisibility(View.VISIBLE);
        holder.llLot.setVisibility(View.VISIBLE);
        holder.llBlock.setVisibility(View.VISIBLE);
        holder.llFromBin.setVisibility(View.VISIBLE);
        holder.llToBin.setVisibility(View.VISIBLE);

        _dtReg = new DataTable();
        dtMaster = new DataTable();
        dtDetail = new DataTable();
        lstSeq = new ArrayList<>();
        mapSeq = new HashMap<String, String>();

        ArrayAdapter<String> adapterSeq = new ArrayAdapter<>(GoodPickNonSheetCartCarrierActivity.this, android.R.layout.simple_spinner_dropdown_item, lstSeq);
        holder.cmbSeq.setAdapter(adapterSeq);

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        GoodPickNonSheetGridAdapter adapter = new GoodPickNonSheetGridAdapter(_dtReg, inflater);
        holder.lvRegister.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void GetSht(final String strSheetId) {

        BModuleObject bmObjSheet = new BModuleObject();
        bmObjSheet.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObjSheet.setModuleID("BIFetchSheetMst");
        bmObjSheet.setRequestID("BIFetchSheetMst");

        bmObjSheet.params = new Vector<ParameterInfo>();

        HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
        List<Condition> lstCondition1 = new ArrayList<Condition>();

        //SHEET_ID
        Condition conditionSheetId = new Condition();
        conditionSheetId.setAliasTable("M");
        conditionSheetId.setColumnName("SHEET_ID");
        conditionSheetId.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
        conditionSheetId.setValue(strSheetId);
        lstCondition1.add(conditionSheetId);
        mapCondition.put(conditionSheetId.getColumnName(), lstCondition1);

        //Serialize序列化
        VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vkey, vVal);
        String strCond = msdl.generateFinalCode(mapCondition);

        //Input param
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Condition);
        param1.setNetParameterValue(strCond);
        bmObjSheet.params.add(param1);

        CallBIModule(bmObjSheet, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn))
                {
                    DataTable dtMst = bModuleReturn.getReturnJsonTables().get("BIFetchSheetMst").get("Mst");

                    if (dtMst.Rows.get(0).getValue("SHEET_TYPE_POLICY_ID").toString().equals("Issue") ||
                            dtMst.Rows.get(0).getValue("SHEET_TYPE_POLICY_ID").toString().equals("Transfer"))
                    {
                        GetPickingID(strSheetId);
                    }
                    else
                    {
                        FetchSheetInfo("");
                    }
                }
            }
        });
    }

    private void GetPickingID(final String strSheetId) {
        String strPickingID = "";

        //region Call BIModule
        // BIModule
        List<BModuleObject> lstBmObj = new ArrayList<BModuleObject>();
        BModuleObject bmShtObj = new BModuleObject();
        bmShtObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmShtObj.setModuleID("BIFetchPickShtID");
        bmShtObj.setRequestID("FetchPickShtID");

        bmShtObj.params = new Vector<ParameterInfo>();

        // Set Condition
        List<Condition> lstCondition1 = new ArrayList<Condition>();
        Condition condition1 = new Condition();
        condition1.setAliasTable("M");
        condition1.setColumnName("SHEET_ID");
        condition1.setValue(strSheetId);
        // 用VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName() = "System.String"
        condition1.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
        lstCondition1.add(condition1);

        HashMap<String, List<?>> mapCondition1 = new HashMap<String, List<?>>();
        mapCondition1.put(condition1.getColumnName(),lstCondition1);
        VirtualClass vkey1 = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal1 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
        MesSerializableDictionaryList msdl1 = new MesSerializableDictionaryList(vkey1, vVal1);
        String strCond1 = msdl1.generateFinalCode(mapCondition1);

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Condition);
        param1.setNetParameterValue(strCond1); // 要用set"Net"ParameterValue
        bmShtObj.params.add(param1);

        CallBIModule(bmShtObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {

                    DataTable dt = bModuleReturn.getReturnJsonTables().get("FetchPickShtID").get("PICKING_ID");

                    if (dt.Rows.size() <= 0)
                    {
                        //WAPG016008    單據代碼[{0}]查無轉揀料單資料
                        ShowMessage(R.string.WAPG016008, strSheetId);
                        return;
                    }

                    String pickingID = dt.Rows.get(0).getValue("SHEET_ID").toString();

                    FetchSheetInfo(pickingID);
                }
            }
        });
        // endregion
    }

    //擴充挑貨規則: 條件串成 SQL Where 條件
    private String generateExtendConfigCond(DataRow drDet, DataTable dtCond) {

        String strExtendCond = null;

        if (dtCond != null && dtCond.Rows.size() > 0) {
            for (DataRow drCond : dtCond.Rows) {
                String reqVal = drDet.getValue(drCond.getValue("REQ_FIELD").toString()).toString();
                String strCond = null;

                if (!reqVal.equals("*")) {
                    strCond = String.format("SR.%s %s '%s'", drCond.getValue("REG_FIELD").toString(), drCond.get("COND_OPERATOR").toString(), reqVal); // e.g. SR.LOT_CODE = 'L...'

                    if (strExtendCond == null || strExtendCond.length() <= 0) {
                        strExtendCond = String.format("AND %s", strCond); // e.g. AND SR.LOT_CODE = 'LXXXX'
                    } else {
                        strExtendCond = String.format("%s AND %s", strExtendCond, strCond); // e.g. AND SR.LOT_CODE = 'LXXXX' AND R.XXX = 'OOOO'
                    }
                }
            }
        }

        return strExtendCond;
    }

    //擴充挑貨規則: 排序串成 SQL Order By 條件
    private String generateExtendConfigSort(DataTable dtSort) {

        List<String> lstConfigSort = new ArrayList<>();

        String strConfigSort = null;
        if (dtSort != null && dtSort.Rows.size() > 0) {
            for (DataRow drSort : dtSort.Rows) {
                lstConfigSort.add(String.format("%s %s", drSort.getValue("REG_FIELD").toString(), drSort.get("SORT_METHOD").toString())); // e.g. MFG_DATE ASC
            }
        }

        if (lstConfigSort.size() > 0) {
            strConfigSort = TextUtils.join(", ", lstConfigSort);
        }

        return strConfigSort;
    }
}

//ERROR CODE WAPG016
//WAPG016001    請輸入批號
//WAPG016002    請輸入料號
//WAPG016003    請輸入數量
//WAPG016004    作業成功
//WAPG016005    請按確認執行下一個步驟
//WAPG016006    請選擇單據代碼
//WAPG016007    查詢單據資料錯誤
//WAPG016008    單據代碼[{0}]查無轉揀料單資料