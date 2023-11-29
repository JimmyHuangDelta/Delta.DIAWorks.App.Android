package com.delta.android.WMS.Client;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.Activity.ScanActivity;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Vector;

public class WarehouseStorageStockInModifyActivity extends BaseFlowActivity {

    private String strSheetPolicyId, detailMode, strLotId, strMfgDate, strExpDate;
    private BigDecimal decQty, decScrapQty;
    private boolean blnIsWV;
    private DataTable dtDet, dtLotAll;
    private int lotPos;

    private ViewHolder holder = null;
    private String drSeq, drStorageId, drItemId;
    private String inputLotId;
    private BigDecimal inputQty;
    private BigDecimal inputScrapQty;
    private String inputMfgDate;
    private String inputExpDate;

    static class ViewHolder {
        // 宣告控制項物件
        ImageButton ibtnLotIdQRScan;
        EditText etLotId;
        EditText etQty;
        EditText etScrapQty;
        EditText etMfgDate;
        EditText etExpDate;
        Button btnSave;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_warehouse_storage_stock_in_modify);

        this.initialData();

        this.setListensers();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, getResources().getText(R.string.QRCODE_SCAN_CANCEL).toString(), Toast.LENGTH_SHORT).show();
            } else {
                holder.etLotId.setText(result.getContents().trim());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
//            DataTable dtLotData = (DataTable) data.getSerializableExtra("dtLotData");
//            dtLotAll = dtLotData;
        }
    }

    private void initialData() {

        detailMode = getIntent().getStringExtra("detailMode");
        strSheetPolicyId = getIntent().getStringExtra("sheetPolicyId");
        blnIsWV = getIntent().getBooleanExtra("blnIsWV", false);
        dtDet = (DataTable) getIntent().getSerializableExtra("dtDet");
        dtLotAll = (DataTable) getIntent().getSerializableExtra("dtLotAll");
        lotPos = getIntent().getIntExtra("lotPos", 0);

        if (holder == null)
            holder = new ViewHolder();

        holder.ibtnLotIdQRScan = findViewById(R.id.ibtnLotIdQRScan);
        holder.etLotId = findViewById(R.id.etLotGridLotId);
        holder.etQty = findViewById(R.id.etLotGridQty);
        holder.etScrapQty = findViewById(R.id.etLotGridScrapQty);
        holder.etMfgDate = findViewById(R.id.etLotGridMfgDate);
        holder.etExpDate = findViewById(R.id.etLotGridExpDate);
        holder.btnSave = findViewById(R.id.btnSave);

        if (!blnIsWV) {
            holder.etScrapQty.setVisibility(View.GONE);
        }

        if (detailMode.equals("Modify")) {
            strLotId = getIntent().getStringExtra("lotId");
            decQty = new BigDecimal(getIntent().getSerializableExtra("qty").toString()).setScale(1);
            decScrapQty = new BigDecimal(getIntent().getSerializableExtra("scrapQty").toString()).setScale(1);
            strMfgDate = getIntent().getStringExtra("mfgDate");
            strExpDate = getIntent().getStringExtra("expDate");
            holder.etLotId.setText(strLotId);
            holder.etQty.setText(decQty.toString());
            holder.etScrapQty.setText(decScrapQty.toString());
            holder.etMfgDate.setText(strMfgDate);
            holder.etExpDate.setText(strExpDate);
        }

    }

    private void setListensers() {
        holder.ibtnLotIdQRScan.setOnClickListener(ibtnQRScanOnClick);
        holder.etMfgDate.setOnClickListener(clickMfgDate);
        holder.etExpDate.setOnClickListener(clickExpDate);
        holder.btnSave.setOnClickListener(saveLotData);
    }

    private View.OnClickListener ibtnQRScanOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            IntentIntegrator integrator = new IntentIntegrator(WarehouseStorageStockInModifyActivity.this);
            // 設置要掃描的條碼類型，ONE_D_CODE_TYPES：一維碼，QR_CODE_TYPES-二維碼
            integrator.setDesiredBarcodeFormats(com.google.zxing.integration.android.IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("BarCode Scan"); //底部的提示文字
            integrator.setCameraId(0); //前置(1)或者後置(0)攝像頭
            integrator.setBeepEnabled(false); //掃描成功的「嗶嗶」聲
            integrator.setBarcodeImageEnabled(false); //是否保留掃碼成功時候的截圖
            integrator.setCaptureActivity(ScanActivity .class);
            integrator.initiateScan();
        }
    };

    private AdapterView.OnClickListener clickMfgDate = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setDateMfg();
        }
    };

    private AdapterView.OnClickListener clickExpDate = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            setDateExp();
        }
    };

    private View.OnClickListener saveLotData = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

        if (checkInput()) {
            Bundle inputData = new Bundle();
            inputData.putString("drSeq", drSeq);
            inputData.putString("drStorageId", drStorageId);
            inputData.putString("drItemId", drItemId);
            inputData.putString("inputLotId", inputLotId);
            inputData.putSerializable("inputQty", inputQty);
            inputData.putSerializable("inputScrapQty", inputScrapQty);
            inputData.putString("inputMfgDate", inputMfgDate);
            inputData.putString("inputExpDate", inputExpDate);
            setActivityResult(inputData);
        }

        }
    };

    public void onClickMfgDateClear(View v) {
        holder.etMfgDate.setText("");
    }
    public void onClickExpDateClear(View v) {
        holder.etExpDate.setText("");
    }
    public void setDateMfg() {
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
        builder.setTitle("設定製造日期");

        final AlertDialog dialog = builder.create();
        dialog.show();

        Button bConfirm = view.findViewById(R.id.btnConfirm);
        bConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //日期格式
                StringBuffer sb = new StringBuffer();
                sb.append(String.format("%d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth()));
                holder.etMfgDate.setText(sb);
                dialog.cancel();
            }
        });
    }
    public void setDateExp() {
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
        builder.setTitle("設定有效期限");

        final AlertDialog dialog = builder.create();
        dialog.show();

        Button bConfirm = view.findViewById(R.id.btnConfirm);
        bConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //日期格式
                StringBuffer sb = new StringBuffer();
                sb.append(String.format("%d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth()));
                holder.etExpDate.setText(sb);
                dialog.cancel();
            }
        });
    }

    private boolean checkInput() {

        // region 檢查輸入值
        if (holder.etQty.getText().toString().trim().equals("")) {
            ShowMessage(R.string.WAPG027007); // WAPG027007   請輸入數量
            return false;
        }
        if (holder.etScrapQty.getText().toString().trim().equals("")) {
            if (blnIsWV) {
                // 入庫需要輸入報廢數量
                ShowMessage(R.string.WAPG027008); // WAPG027008   請輸入報廢數量
                return false;
            } else {
                // 退料不需要輸入報廢數量
                holder.etScrapQty.setText("0");
            }
        }
        if (holder.etQty.getText().toString().trim().equals("0") && holder.etScrapQty.getText().toString().trim().equals("0")) {
            if (blnIsWV) {
                ShowMessage(R.string.WAPG027009); // WAPG027009   數量、報廢數量不可同時為0！
                return false;
            }
        }

        if (holder.etMfgDate.getText().toString().trim().equals("")) {
            ShowMessage(R.string.WAPG027010); // WAPG027010   請輸入製造日期
            return false;
        }

        if (holder.etExpDate.getText().toString().trim().equals("")) {
            ShowMessage(R.string.WAPG027011); // WAPG027011   請輸入有效期限
            return false;
        }

        int compare = holder.etMfgDate.getText().toString().compareTo(holder.etExpDate.getText().toString());
        if (compare == 1) {
            ShowMessage(R.string.WAPG027012); // WAPG027012   製造日期不可大於有效期限
            return false;
        }
        // endregion

        // region 判斷是否有設定實際數量的狀態
        String drActualQtyStatus = dtDet.getValue(lotPos, "ACTUAL_QTY_STATUS").toString();
        if (drActualQtyStatus.equals("")) {
            Object[] args = new Object[1];
            args[0] = strSheetPolicyId;
            ShowMessage(R.string.WAPG027013, args); //WAPG027013    單據類型「%s」未設定「單據Config設定」內的【實際數量的狀態】，請先設定!
            return false;
        }
        //endregion

        // region 取得輸入值: LotId, Qty, ScrapQty, MfgDate, ExpDate -> 經過以下判斷沒問題會在saveLotData傳回上一頁顯示出來
        inputLotId = holder.etLotId.getText().toString().trim();
        inputQty = new BigDecimal(holder.etQty.getText().toString().trim()).setScale(1);
        inputScrapQty = new BigDecimal(holder.etScrapQty.getText().toString().trim()).setScale(1);
        inputMfgDate = holder.etMfgDate.getText().toString().trim();
        inputExpDate = holder.etExpDate.getText().toString().trim();
        // endregion

        // region 由上一頁點選的資料取得: Seq, StorageId, ItemId -> 經過以下判斷沒問題會在saveLotData傳回上一頁顯示出來
        drSeq = dtDet.Rows.get(lotPos).getValue("SEQ").toString();
        drStorageId = dtDet.Rows.get(lotPos).get("STORAGE_ID").toString();
        drItemId = dtDet.Rows.get(lotPos).get("ITEM_ID").toString();
        // endregion

        // region 由上一頁點選的資料取得: LotId, RegType, Qty, ScrapQty
        String drLotId = dtDet.Rows.get(lotPos).getValue("LOT_ID").toString();
        String drRegType = dtDet.Rows.get(lotPos).getValue("REGISTER_TYPE").toString();
        BigDecimal drQty = new BigDecimal(dtDet.getValue(lotPos, "QTY").toString());
        BigDecimal drScrapQty = new BigDecimal(dtDet.getValue(lotPos, "SCRAP_QTY").toString().equals("") ? "0.0" : dtDet.getValue(lotPos, "SCRAP_QTY").toString());
        // endregion


        // 物料管控批號時，如果單據項次有指定批號(非*號)，只能新增同批號的物料。
        if (!drRegType.equals("ItemID")) {

            if (!drLotId.equals("*")) {

                // 單據項次內，批號非*號，表示要卡控批號一致
                if (!drLotId.equals(inputLotId)) {
                    Object[] args = new Object[3];
                    args[0] = inputLotId;
                    args[1] = drSeq;
                    args[2] = drLotId;
                    ShowMessage(R.string.WAPG027014, args); // WAPG027014    輸入批號「%s」與單據項次「%s」指定的批號「%s」不一致!
                    holder.etLotId.requestFocus();
                    return false;
                }

                // 依單據Config的【實際數量的狀態】檢查數量
                if (drActualQtyStatus.equals("Equal")) { // 數量要等於單據數量

                    if (drQty.compareTo(inputQty) != 0) { // drQty != inputQty
                        Object[] args = new Object[2];
                        args[0] = strSheetPolicyId;
                        args[1] = drActualQtyStatus;
                        ShowMessage(R.string.WAPG027015, args); // WAPG027015    單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，批號數量需相等!
                        holder.etQty.requestFocus();
                        return false;
                    }
                    if (drScrapQty.compareTo(inputScrapQty) != 0) { // drScrapQty != inputScrapQty
                        Object[] args = new Object[2];
                        args[0] = strSheetPolicyId;
                        args[1] = drActualQtyStatus;
                        ShowMessage(R.string.WAPG027016, args); // WAPG027016    單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，批號報廢數量需相等!
                        holder.etScrapQty.requestFocus();
                        return false;
                    }

                } else if (drActualQtyStatus.equals("More")) { // 數量可以大於單據數量

                    if (drQty.compareTo(inputQty) == -1) { // drQty < inputQty
                        Object[] args = new Object[4];
                        args[0] = strSheetPolicyId;
                        args[1] = drActualQtyStatus;
                        args[2] = inputQty;
                        args[3] = drQty;
                        ShowMessage(R.string.WAPG027017, args); // WAPG027017   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號數量「%s」不可小於單據數量「%s」!
                        holder.etQty.requestFocus();
                        return false;
                    }

                    if (drScrapQty.compareTo(inputScrapQty) == -1) { //drScrapQty < inputScrapQty
                        Object[] args = new Object[4];
                        args[0] = strSheetPolicyId;
                        args[1] = drActualQtyStatus;
                        args[2] = inputScrapQty;
                        args[3] = drScrapQty;
                        ShowMessage(R.string.WAPG027018, args); // WAPG027018   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號報廢數量「%s」不可小於單據報廢數量「%s」!
                        holder.etScrapQty.requestFocus();
                        return false;
                    }

                } else if (drActualQtyStatus.equals("Less")) {
                    // 數量可以小於單據數量

                    if (drQty.compareTo(inputQty) == 1) { //drQty > inputQty
                        Object[] args = new Object[4];
                        args[0] = strSheetPolicyId;
                        args[1] = drActualQtyStatus;
                        args[2] = inputQty;
                        args[3] = drQty;
                        ShowMessage(R.string.WAPG027019, args); // WAPG027019   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號數量「%s」不可大於單據數量「%s」!
                        holder.etQty.requestFocus();
                        return false;
                    }

                    if (drScrapQty.compareTo(inputScrapQty) == 1) { // drScrapQty > inputScrapQty
                        Object[] args = new Object[4];
                        args[0] = strSheetPolicyId;
                        args[1] = drActualQtyStatus;
                        args[2] = inputScrapQty;
                        args[3] = drScrapQty;
                        ShowMessage(R.string.WAPG027020, args); // WAPG027020   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號報廢數量「%s」不可大於單據報廢數量「%s」!
                        holder.etScrapQty.requestFocus();
                        return false;
                    }
                }

            } else {
                // 單據項次內，批號為*號，表示不需要卡控批號

                // 新增數量需加上已新增數量387
                BigDecimal decTotalAddQty = new BigDecimal("0.0");
                BigDecimal decTotalAddScrapQty = new BigDecimal("0.0");

                if (dtLotAll != null && dtLotAll.Rows.size() > 0) {
                    // 如果是修改要先扣除選擇列的數量 <= 大概這附近

                    for (DataRow drLot : dtLotAll.Rows) {
                        if (drLot.getValue("SEQ").equals(drSeq)) {
                            decTotalAddQty = decTotalAddQty.add(new BigDecimal(drLot.getValue("QTY").toString()).setScale(1));
                            decTotalAddScrapQty = decTotalAddScrapQty.add(new BigDecimal(drLot.getValue("SCRAP_QTY").toString()).setScale(1));
                        }
                    }

                    //如果是修改要先扣除選擇列的數量
                    if (detailMode.equals("Modify")) {
                        decTotalAddQty = decTotalAddQty.subtract(decQty);
                        decTotalAddScrapQty = decTotalAddScrapQty.subtract(decScrapQty);
                    }
                }

                decTotalAddQty = decTotalAddQty.add(inputQty);
                decTotalAddScrapQty = decTotalAddScrapQty.add(inputScrapQty);

                //依單據Config的【實際數量的狀態】檢查數量
                if (drActualQtyStatus.equals("More")) {
                    // 數量可以大於單據數量

                    if (decTotalAddQty.compareTo(inputQty) == -1) { //totalQty < inputQty
                        Object[] args = new Object[4];
                        args[0] = strSheetPolicyId;
                        args[1] = drActualQtyStatus;
                        args[2] = decTotalAddQty;
                        args[3] = drQty;
                        ShowMessage(R.string.WAPG027017, args); // WAPG027017   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號數量「%s」不可小於單據數量「%s」!
                        holder.etQty.requestFocus();
                        return false;
                    }
                    if (decTotalAddScrapQty.compareTo(inputScrapQty) == -1 ) { //totalScrapQty < inputScrapQty
                        // WAPG strSheetPolicyId drActualQtyStatus inputScrapQty drScrapQty
                        Object[] args = new Object[4];
                        args[0] = strSheetPolicyId;
                        args[1] = drActualQtyStatus;
                        args[2] = decTotalAddScrapQty;
                        args[3] = drScrapQty;
                        ShowMessage(R.string.WAPG027018, args); // WAPG027018   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號報廢數量「%s」不可小於單據報廢數量「%s」!
                        holder.etScrapQty.requestFocus();
                        return false;
                    }
                } else if (drActualQtyStatus.equals("Less")) {
                    // 數量可以小於單據數量

                    if (decTotalAddQty.compareTo(inputQty) == 1) { //totalQty > inputQty
                        Object[] args = new Object[4];
                        args[0] = strSheetPolicyId;
                        args[1] = drActualQtyStatus;
                        args[2] = decTotalAddQty;
                        args[3] = drQty;
                        ShowMessage(R.string.WAPG027019, args); // WAPG027019   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號數量「%s」不可大於單據數量「%s」!
                        holder.etQty.requestFocus();
                        return false;
                    }
                    if (decTotalAddScrapQty.compareTo(inputScrapQty) == 1) { //totalScrapQty > inputScrapQty
                        // WAPG strSheetPolicyId drActualQtyStatus inputScrapQty drScrapQty
                        Object[] args = new Object[4];
                        args[0] = strSheetPolicyId;
                        args[1] = drActualQtyStatus;
                        args[2] = decTotalAddScrapQty;
                        args[3] = drScrapQty;
                        ShowMessage(R.string.WAPG027020, args); // WAPG027020   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號報廢數量「%s」不可大於單據報廢數量「%s」!
                        holder.etScrapQty.requestFocus();
                        return false;
                    }
                }
            }
        } else {
            // 物料管控批號時，如果單據開立批號為*號，表示不指定批號，則不需要卡控批號(但需要輸入批號)。
            // 單據項次內，批號為*號，表示不需要卡控批號

            // 新增數量需要加上已新增數量
            BigDecimal decTotalAddQty = new BigDecimal("0.0");
            BigDecimal decTotalAddScrapQty = new BigDecimal("0.0");

            if (dtLotAll != null && dtLotAll.Rows.size() > 0) {
                // 如果是修改要先扣除選擇列的數量 <= 大概這附近

                for (DataRow drLot : dtLotAll.Rows) {
                    if (drLot.getValue("SEQ").equals(drSeq)) {
                        decTotalAddQty = decTotalAddQty.add(new BigDecimal(drLot.getValue("QTY").toString()).setScale(1));
                        decTotalAddScrapQty = decTotalAddScrapQty.add(new BigDecimal(drLot.getValue("SCRAP_QTY").toString()).setScale(1));
                    }
                }

                //如果是修改要先扣除選擇列的數量
                if (detailMode.equals("Modify")) {
                    decTotalAddQty = decTotalAddQty.subtract(decQty);
                    decTotalAddScrapQty = decTotalAddScrapQty.subtract(decScrapQty);
                }
            }

            decTotalAddQty = decTotalAddQty.add(inputQty);
            decTotalAddScrapQty = decTotalAddScrapQty.add(inputScrapQty);

            // 依單據Config的【實際數量的狀態】檢查數量
            if (drActualQtyStatus.equals("Equal")) {
                if (drQty.compareTo(inputQty) != 0) { //drQty != inputQty
                    Object[] args = new Object[2];
                    args[0] = strSheetPolicyId;
                    args[1] = drActualQtyStatus;
                    ShowMessage(R.string.WAPG027015, args); // WAPG027015   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，批號數量需相等!
                    holder.etQty.requestFocus();
                    return false;
                }
                if (drScrapQty.compareTo(inputQty) != 0) { //drScrapQty != inputQty
                    // WAPG strSheetPolicyId drActualQtyStatus
                    Object[] args = new Object[2];
                    args[0] = strSheetPolicyId;
                    args[1] = drActualQtyStatus;
                    ShowMessage(R.string.WAPG027016, args); // WAPG027016   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，批號報廢數量需相等!
                    holder.etScrapQty.requestFocus();
                    return false;
                }
            } else if (drActualQtyStatus.equals("More")) {
                if (decTotalAddQty.compareTo(inputQty) == -1) { //drQty < inputQty
                    Object[] args = new Object[4];
                    args[0] = strSheetPolicyId;
                    args[1] = drActualQtyStatus;
                    args[2] = decTotalAddQty;
                    args[3] = drQty;
                    ShowMessage(R.string.WAPG027017, args); // WAPG027017   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號數量「%s」不可小於單據數量「%s」!
                    holder.etQty.requestFocus();
                    return false;
                }
                if (decTotalAddScrapQty.compareTo(inputQty) == -1) { //drScrapQty < inputQty
                    Object[] args = new Object[4];
                    args[0] = strSheetPolicyId;
                    args[1] = drActualQtyStatus;
                    args[2] = decTotalAddScrapQty;
                    args[3] = drScrapQty;
                    ShowMessage(R.string.WAPG027018, args); // WAPG027018   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號報廢數量「%s」不可小於單據報廢數量「%s」!
                    holder.etScrapQty.requestFocus();
                    return false;
                }
            } else if (drActualQtyStatus.equals("Less")) {
                if (decTotalAddQty.compareTo(inputQty) == 1) { //drQty > inputQty
                    Object[] args = new Object[4];
                    args[0] = strSheetPolicyId;
                    args[1] = drActualQtyStatus;
                    args[2] = decTotalAddQty;
                    args[3] = drQty;
                    ShowMessage(R.string.WAPG027019, args); // WAPG027019   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號數量「%s」不可大於單據數量「%s」!
                    holder.etQty.requestFocus();
                    return false;
                }
                if (decTotalAddScrapQty.compareTo(inputScrapQty) == 1) { // drScrapQty > inputScrapQty
                    Object[] args = new Object[4];
                    args[0] = strSheetPolicyId;
                    args[1] = drActualQtyStatus;
                    args[2] = decTotalAddScrapQty;
                    args[3] = drScrapQty;
                    ShowMessage(R.string.WAPG027020, args); // WAPG027020   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號報廢數量「%s」不可大於單據報廢數量「%s」!
                    holder.etScrapQty.requestFocus();
                    return false;
                }
            }
        }

        int iMaxLot = 1, sameLotIdCounter = 0;
        for (DataRow dr : dtLotAll.Rows) {
            if (dr.getValue("LOT_ID").toString().equals(inputLotId) && dr.getValue("SEQ").toString().equals(drSeq))
                sameLotIdCounter++;
        }
        if (detailMode.equals("Add"))
            iMaxLot = 0;
        else {
            if (!inputLotId.equals(strLotId))
                iMaxLot = 0;
        }
        if (sameLotIdCounter > iMaxLot) {
            ShowMessage(R.string.WAPG027021); // WAPG027021   批號已存在!
            return false;
        }

        if (drRegType.equals("")) {
            Object[] args = new Object[1];
            args[0] = drItemId;
            ShowMessage(R.string.WAPG027022, args); // WAPG027022   物料[%s]未設定WMS物料設定檔
            return false;
        }

        if (drRegType.equals("ItemID")) {
            // By 料號
            if (inputLotId != null && !inputLotId.equals("*")) {
                Object[] args = new Object[1];
                args[0] = drItemId;
                ShowMessage(R.string.WAPG027023, args); // WAPG027023   物料[%s]不管控批號，批號需為空白
                holder.etLotId.requestFocus();
                return false;
            }
        } else {
            if (inputLotId == null && inputLotId.equals("*")) {
                Object[] args = new Object[1];
                args[0] = drItemId;
                ShowMessage(R.string.WAPG027006, args); // WAPG027006   物料[%s]管控批號，需要輸入批號
                holder.etLotId.requestFocus();
                return false;
            }
        }
        /*
        if (blnIsWV) {
            if (String.valueOf(inputScrapQty) == null) {
                ShowMessage(R.string.WAPG027008); // WAPG027008   請輸入報廢數量
                holder.etScrapQty.requestFocus();
                return false;
            }

            // 入庫 check box data
            if (dtDet.Rows.get(lotPos).getValue("WO_ID").toString().trim() != null && !dtDet.Rows.get(lotPos).getValue("WO_ID").toString().trim().equals("*")) {
                if (!drRegType.equals("ItemID")) {
                    checkBoxInfo(inputLotId);
                }
            }
        }
        */
        return true;
    }

    /*
    private void checkBoxInfo(final String strLotId) {

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchBoxLevelByID");
        bmObj.setRequestID("BIFetchBoxLevelByID");
        bmObj.params = new Vector<>();
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Filter);
        param1.setParameterValue("AND R.REGISTER_ID LIKE '" + strLotId + "'");
        bmObj.params.add(param1);

        CallBIModule(bmObj, new WebAPIClientEvent() {

            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(!CheckBModuleReturnInfo(bModuleReturn)) return;

                DataTable dtBox = bModuleReturn.getReturnJsonTables().get("BIFetchBoxLevelByID").get("Box");
                if (dtBox.Rows.size() == 0) {
                    Object[] args = new Object[1];
                    args[0] = strLotId;
                    ShowMessage(R.string.WAPG027024, args); // WAPG027024   批號[%s]不存在於MES
                    return;
                } else {
                    DataRow drFirstRow = dtBox.Rows.get(0);
                    if (!drFirstRow.getValue("IS_OUTERMOST").equals("Y")) {
                        Object[] args = new Object[2];
                        args[0] = strLotId;
                        args[1] = dtBox.Rows.get(0).getValue("UP_ID").toString();
                        ShowMessage(R.string.WAPG027025, args); // WAPG027025   批號[%s]非最外箱,需輸入最外層箱號,上層ID[%s]
                        return;
                    } else {
                        BigDecimal dQty = new BigDecimal("0.0");
                        for (DataRow dr : dtBox.Rows) {
                            dQty = dQty.add(new BigDecimal(dr.getValue("QTY").toString()));
                        }
                        if (decQty.compareTo(dQty) != 0) {
                            Object[] args = new Object[3];
                            args[0] = inputLotId;
                            args[1] = decQty;
                            args[2] = dQty;
                            ShowMessage(R.string.WAPG027026, args); // WAPG027026   批號[%s]數量[%s]需等於[%s]
                            return;
                        }
                    }
                }
            }
        });
    }
     */
}

// ERROR CODE
// WAPG027001   請輸入至少一個條件
// WAPG027002   請選擇開單日期(起)和開單日期(迄)
// WAPG027003   開單日期(起)不能大於開單日期(迄)
// WAPG027004   請選擇單據類型
// WAPG027005   查詢無資料
// WAPG027006   物料[%s]管控批號，需要輸入批號
// WAPG027007   請輸入數量
// WAPG027008   請輸入報廢數量
// WAPG027009   數量、報廢數量不可同時為0！
// WAPG027010   請輸入製造日期
// WAPG027011   請輸入有效期限
// WAPG027012   製造日期不可大於有效期限
// WAPG027013   單據類型「%s」未設定「單據Config設定」內的【實際數量的狀態】，請先設定!
// WAPG027014   輸入批號「%s」與單據項次「%s」指定的批號「%s」不一致!
// WAPG027015   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，批號數量需相等!
// WAPG027016   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，批號報廢數量需相等!
// WAPG027017   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號數量「%s」不可小於單據數量「%s」!
// WAPG027018   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號報廢數量「%s」不可小於單據報廢數量「%s」!
// WAPG027019   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號數量「%s」不可大於單據數量「%s」!
// WAPG027020   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號報廢數量「%s」不可大於單據報廢數量「%s」!
// WAPG027021   批號已存在!
// WAPG027022   物料[%s]未設定WMS物料設定檔
// WAPG027023   物料[%s]不管控批號，批號需為空白
// WAPG027024   批號[%s]不存在於MES
// WAPG027025   批號[%s]非最外箱,需輸入最外層箱號,上層ID[%s]
// WAPG027026   批號[%s]數量[%s]需等於[%s]
// WAPG027027   是否刪除此批號?
// WAPG027028   請新增批號
// WAPG027029   單據項次「%s」沒有明細!
// WAPG027030   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，項次「%s」批號數量需相等!
// WAPG027031   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，項次「%s」批號報廢數量需相等!
// WAPG027032   查無對應的入庫儲位
// WAPG027033   尚未選擇倉庫[%s]物料[%s]對應儲位
// WAPG027034   作業成功