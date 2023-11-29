package com.delta.android.WMS.Client;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.Activity.ScanActivity;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionaryList;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.delta.android.WMS.Client.GridAdapter.GoodReceiptReceiveMstGridAdapter;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GoodReceiptReceiveActivity extends BaseFlowActivity {

    // private variable
    ViewHolder holder = null;
    DataTable GrMstTable = null;
    HashMap<String, DataTable> GrDetTables;

    static class ViewHolder {
        // 宣告控制項物件
        EditText FromDate;
        EditText ToDate;
        EditText SheetId;
        ListView MasterGrData;
        ImageButton IbtnSearch;
        ImageButton IbtnSheetIdQRScan; // 220707 Ikea 新增鏡頭掃描輸入
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_good_receipt_receive);

        this.InitialSetup();
        // 設定監聽事件
        setListensers();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, getResources().getText(R.string.QRCODE_SCAN_CANCEL).toString(), Toast.LENGTH_SHORT).show();
            } else {
                holder.SheetId.setText(result.getContents().trim());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void InitialSetup() {
        if (holder == null) {
            holder = new ViewHolder();
            // 取得控制項物件
            holder.FromDate = findViewById(R.id.etFromDate);
            holder.ToDate = findViewById(R.id.etToDate);
            holder.MasterGrData = findViewById(R.id.lvMasterSheet);
            holder.SheetId = findViewById(R.id.etSheetId);
            holder.IbtnSearch = findViewById(R.id.ibtnSearch);
            holder.IbtnSheetIdQRScan = findViewById(R.id.ibtnSheetIdQRScan); // 220707 Ikea 新增鏡頭掃描輸入
        }
        holder.FromDate.setInputType(InputType.TYPE_NULL);
        holder.ToDate.setInputType(InputType.TYPE_NULL);
        GrDetTables = new HashMap<>();
    }

    //設定監聽事件
    private void setListensers() {
        holder.FromDate.setOnClickListener(FromDateOnClick);
        holder.ToDate.setOnClickListener(ToDateOnClick);

        holder.SheetId.setOnKeyListener(SheetIdOnKey);
        holder.IbtnSearch.setOnClickListener(IbtnSearchOnClick);
        holder.IbtnSheetIdQRScan.setOnClickListener(IbtnSheetIdQRScanOnClick); // 220707 Ikea 新增鏡頭掃描輸入
        holder.MasterGrData.setOnItemClickListener(ListViewOnClick);
    }

    // region 事件

    private AdapterView.OnClickListener FromDateOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setDateFrom();
        }
    };

    private AdapterView.OnClickListener ToDateOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setDateTo();
        }
    };

    private View.OnKeyListener SheetIdOnKey = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            //只有按下Enter才會反映
            if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(v.getWindowToken(), 0);// 隱藏虛擬鍵盤
                GetMaster();
                return true;
            }
            return false;
        }
    };

    private View.OnClickListener IbtnSearchOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            GetMaster();
        }
    };

    // 220707 Ikea 新增鏡頭掃描輸入
    private View.OnClickListener IbtnSheetIdQRScanOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            IntentIntegrator integrator = new IntentIntegrator(GoodReceiptReceiveActivity.this);
            // 設置要掃描的條碼類型，ONE_D_CODE_TYPES：一維碼，QR_CODE_TYPES-二維碼
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("BarCode Scan"); //底部的提示文字
            integrator.setCameraId(0); //前置(1)或者後置(0)攝像頭
            integrator.setBeepEnabled(false); //掃描成功的「嗶嗶」聲
            integrator.setBarcodeImageEnabled(false); //是否保留掃碼成功時候的截圖
            integrator.setCaptureActivity(ScanActivity.class);
            integrator.initiateScan();
        }
    };

    private AdapterView.OnItemClickListener ListViewOnClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final DataRow chooseRow = GrMstTable.Rows.get(position);

            // 將被選中的單據丟到 MstResultTable
            DataTable MstResultTable = new DataTable();
            MstResultTable.Rows.add(chooseRow);// add row
            MstResultTable.getColumns().addAll(GrMstTable.getColumns()); // add columns -> 好像不會用到

            // 將選中單據的detail丟到 DetResultTable
            DataTable DetResultTable = new DataTable();
            for (DataRow dr : GrDetTables.get(chooseRow.getValue("GR_ID").toString()).Rows)
            {
                DetResultTable.Rows.add(dr); // add row
            }
            if (DetResultTable.getColumns() == null || DetResultTable.getColumns().size() == 0)
                DetResultTable.getColumns().addAll(GrDetTables.get(chooseRow.getValue("GR_ID").toString()).getColumns()); // add columns

            GetReceiveQtyAndSkipQcAndGotoNextActivity(MstResultTable, DetResultTable);
        }
    };

    // endregion

    // choose date
    public void setDateFrom() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.style_wms_dialog_date_picker_spinner, null);//layout
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);//物件
        //右邊簡化
        datePicker.setCalendarViewShown(false);
        //初始化
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
        //設置Dialog
        builder.setView(view);
        builder.setTitle(R.string.FROM_DATE);

        final AlertDialog dialog = builder.create();
        dialog.show();

        Button bConfirm = view.findViewById(R.id.btnConfirm);
        bConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //日期格式
                StringBuffer sb = new StringBuffer();
                sb.append(String.format("%d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth()));
                holder.FromDate.setText(sb);
                dialog.cancel();
            }
        });
    }
    public void setDateTo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.style_wms_dialog_date_picker_spinner, null);//layout
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);//物件
        //右邊簡化
        datePicker.setCalendarViewShown(false);
        //初始化
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
        //設置Dialog
        builder.setView(view);
        builder.setTitle(R.string.TO_DATE);

        final AlertDialog dialog = builder.create();
        dialog.show();

        Button bConfirm = view.findViewById(R.id.btnConfirm);
        bConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //日期格式
                StringBuffer sb = new StringBuffer();
                sb.append(String.format("%d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth()));
                holder.ToDate.setText(sb);
                dialog.cancel();
            }
        });
    }
    // clear date
    public void onClickFromDateClear(View v) {
        holder.FromDate.setText("");
    }
    public void onClickToDateClear(View v) {
        holder.ToDate.setText("");
    }
    // Get Master and Detail
    private void GetMaster() {

        //region Check Input
        int compare = holder.FromDate.getText().toString().compareTo(holder.ToDate.getText().toString());
        if (holder.FromDate.getText().toString().equals("") && holder.ToDate.getText().toString().equals("") && holder.SheetId.getText().toString().equals("")) {
            ShowMessage(R.string.WAPG007001);
            return;
        } else if ((holder.FromDate.getText().toString().equals("") || holder.ToDate.getText().toString().equals("")) && holder.SheetId.getText().toString().equals("")) {
            ShowMessage(R.string.WAPG007002);
            return;
        } else if (compare == 1) {
            ShowMessage(R.string.WAPG007003);
            return;
        }
        // endregion

        //region Set BIModule
        // BIModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchGoodReceipt");
        bmObj.setRequestID("BIFetchGoodReceipt");
        bmObj.params = new Vector<ParameterInfo>();

        // 裝Condition的容器
        HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
        List<Condition> lstCondition1 = new ArrayList<Condition>();
        List<Condition> lstCondition2 = new ArrayList<Condition>();
        List<Condition> lstCondition3 = new ArrayList<Condition>();

        // region Set Condition
        // SHEET_ID
        if (!holder.SheetId.getText().toString().equals("")) {
            Condition conditionSheetId = new Condition();
            conditionSheetId.setAliasTable("M");
            conditionSheetId.setColumnName("GR_ID");
            conditionSheetId.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
            conditionSheetId.setValue(holder.SheetId.getText().toString().toUpperCase().trim()); //20200729 archie 轉大寫
            lstCondition1.add(conditionSheetId);
            mapCondition.put(conditionSheetId.getColumnName(), lstCondition1);
        }
        // SHEET_STATUS
        Condition conditionSheetStatus = new Condition();
        conditionSheetStatus.setAliasTable("M");
        conditionSheetStatus.setColumnName("GR_STATUS");
        conditionSheetStatus.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
        conditionSheetStatus.setValue("Confirmed");
        lstCondition2.add(conditionSheetStatus);
        mapCondition.put(conditionSheetStatus.getColumnName(), lstCondition2);

        if (!holder.FromDate.getText().toString().equals("") && !holder.ToDate.getText().toString().equals("")) {
            // 開立單據時間區間
            Condition conditionCreateDate = new Condition();
            conditionCreateDate.setAliasTable("M");
            conditionCreateDate.setColumnName("CREATE_DATE");
            conditionCreateDate.setDataType("System.DateTime");
            conditionCreateDate.setValue(holder.FromDate.getText().toString() + " 00:00:00");
            conditionCreateDate.setValueBetween(holder.ToDate.getText().toString() + " 23:59:59");
            lstCondition3.add(conditionCreateDate);
            mapCondition.put(conditionCreateDate.getColumnName(), lstCondition3);
        }
        // endregion

        // Serialize序列化
        VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vkey, vVal);
        String strCond = msdl.generateFinalCode(mapCondition);

        // Input param
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Condition);
        param1.setNetParameterValue(strCond); // 要用set"Net"ParameterValue
        bmObj.params.add(param1);

        // endregion

        // Call BIModule
        CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    DataTable dtMaster = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceipt").get("GrMst");
                    DataTable dtDetail = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceipt").get("GrDet");
                    Boolean f = true; // 判斷是否Mst已經有輸入的ID

                    if (dtMaster.Rows.size() <= 0) {
                        //    查詢無資料
                        ShowMessage(R.string.WAPG007004);
                        return;
                    }
                    // Input Mst Data
                    if (!holder.SheetId.getText().toString().equals("") && !(GrMstTable == null || GrMstTable.Rows.size() == 0))   // 有輸入ID則從原本的MstTable加一列Mst
                    {
                        for (DataRow drOld : GrMstTable.Rows) {
                            if (drOld.getValue("GR_ID").toString().equals(dtMaster.Rows.get(0).getValue("GR_ID").toString()))
                                f = false;
                        }
                        if (f) GrMstTable.Rows.add(dtMaster.Rows.get(0));
                    } else {                                                 // 沒有有輸入ID則直接替換Mst
                        GrMstTable = dtMaster;
                    }

                    // Input Det Data
                    if (!holder.SheetId.getText().toString().equals("") && !(GrMstTable == null || GrMstTable.Rows.size() == 0))
                    // 有輸入ID,則從原本的DetTables加一筆(k,v)。v為此單據的items table
                    {
                        if (f)
                            GrDetTables.put(dtMaster.Rows.get(0).getValue("GR_ID").toString(), dtDetail);
                    } else {                                                 // 沒有有輸入ID,則清空後全部加入
                        GrDetTables.clear();
                        for (DataRow drNew : dtMaster.Rows) {
                            DataTable dt = new DataTable();
                            for (DataRow drNewDet : dtDetail.Rows) {
                                if (drNew.getValue("GR_ID").toString().equals(drNewDet.getValue("GR_ID").toString())) {
                                    dt.Rows.add(drNewDet);
                                }
                            }
                            GrDetTables.put(drNew.getValue("GR_ID").toString(), dt);
                            GrDetTables.get(drNew.getValue("GR_ID").toString()).getColumns().addAll(dtDetail.getColumns()); // add columns
                        }
                    }

                    // Input ListView
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); // 加載layout至此layout上
                    GoodReceiptReceiveMstGridAdapter adapter = new GoodReceiptReceiveMstGridAdapter(GrMstTable, inflater);
                    holder.MasterGrData.setAdapter(adapter);
                    holder.SheetId.setText("");
                }
            }
        });
    }

    private void GetReceiveQtyAndSkipQcAndGotoNextActivity(final DataTable MstResultTable, final DataTable DetResultTable)
    {
        List<BModuleObject> bmObjs = new ArrayList<BModuleObject>();

        // GRR DET
        BModuleObject biObj1 = new BModuleObject();
        biObj1.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        biObj1.setModuleID("BIFetchGoodReceiptReceiveDet");
        biObj1.setRequestID("BIFetchGoodReceiptReceiveDet");
        biObj1.params = new Vector<>();

        BModuleObject bmObj2 = new BModuleObject();
        bmObj2.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj2.setModuleID("BIFetchSize");
        bmObj2.setRequestID("BIFetchSize");
        bmObj2.params = new Vector<ParameterInfo>();

        BModuleObject bmObj3 = new BModuleObject();
        bmObj3.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj3.setModuleID("BIFetchItem");
        bmObj3.setRequestID("BIFetchItem");
        bmObj3.params = new Vector<ParameterInfo>();

        BModuleObject bmObj4 = new BModuleObject();
        bmObj4.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSPackingInfo");
        bmObj4.setModuleID("BIFetchSkuLevel");
        bmObj4.setRequestID("BIFetchSkuLevel");
        bmObj4.params = new Vector<ParameterInfo>();

        // region Set Condition 1
        // 裝Condition的容器
        HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
        List<Condition> lstCondition1 = new ArrayList<Condition>();

        // SHEET_ID
        Condition conditionSheetId = new Condition();
        conditionSheetId.setAliasTable("M");
        conditionSheetId.setColumnName("GR_ID");
        conditionSheetId.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
        conditionSheetId.setValue(MstResultTable.Rows.get(0).getValue("GR_ID").toString());
        lstCondition1.add(conditionSheetId);
        mapCondition.put(conditionSheetId.getColumnName(),lstCondition1);

        // Serialize序列化
        VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vkey, vVal);
        String strCond = msdl.generateFinalCode(mapCondition);
        // endregion

        // Input param
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Condition);
        param1.setNetParameterValue(strCond); // 要用set"Net"ParameterValue
        biObj1.params.add(param1);

        bmObjs.add(biObj1);
        bmObjs.add(bmObj2);
        bmObjs.add(bmObj3);
        bmObjs.add(bmObj4);

        CallBIModule(bmObjs, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn))
                {
                    DataTable GrLotTableAll = bModuleReturn.getReturnJsonTables().get("BIFetchGoodReceiptReceiveDet").get("GrrDet");
                    DataTable dtSize = bModuleReturn.getReturnJsonTables().get("BIFetchSize").get("WMS_SIZE");
                    DataTable dtItem = bModuleReturn.getReturnJsonTables().get("BIFetchItem").get("ITEM");
                    DataTable dtSkuLevel = bModuleReturn.getReturnJsonTables().get("BIFetchSkuLevel").get("WmsSkuLevel");

                    HashMap<String,Double> SeqQtyOfAllLot = new HashMap();
                    HashMap<String,String> SeqSkipQcOfAllLot = new HashMap();

                    // 初始化SeqQtyOfAllLot
                    for (DataRow dr: DetResultTable.Rows)
                    {
                        SeqQtyOfAllLot.put(dr.getValue("SEQ").toString(), 0.0);
                    }
                    // 得到各項次已收料的數量總和
                    for (DataRow dr: GrLotTableAll.Rows)
                    {
                        double temp = 0.0;
                        temp = SeqQtyOfAllLot.get(dr.getValue("SEQ").toString()) + Double.parseDouble(dr.getValue("QTY").toString());
                        SeqQtyOfAllLot.put(dr.getValue("SEQ").toString(),temp);
                        SeqSkipQcOfAllLot.put(dr.getValue("SEQ").toString(),dr.getValue("SKIP_QC").toString());
                    }

                    Bundle GrData = new Bundle();
                    GrData.putSerializable("GrMst", MstResultTable);
                    GrData.putSerializable("GrDet", DetResultTable);
                    GrData.putSerializable("GrrQty", SeqQtyOfAllLot);
                    GrData.putSerializable("GrrSkipQc", SeqSkipQcOfAllLot);
                    GrData.putSerializable("Size", dtSize);
                    GrData.putSerializable("Item", dtItem);
                    GrData.putSerializable("SkuLevel", dtSkuLevel);
                    gotoNextActivity(GoodReceiptReceiveDetailNewActivity.class, GrData);
                }
            }
        });
    }

}
