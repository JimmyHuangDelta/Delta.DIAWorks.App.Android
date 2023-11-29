package com.delta.android.WMS.Client;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.Activity.ScanActivity;
import com.delta.android.Core.Activity.ShowMessageEvent;
import com.delta.android.Core.DataTable.DataColumn;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.GenerateJsonNetString.MesClass;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Client.Fragment.GoodReceiveDataFragment;
import com.delta.android.WMS.Client.GridAdapter.GoodNonreceiptReceiveSNAdapter;
import com.delta.android.WMS.Client.GridAdapter.GoodReceiptReceiveExtendGridAdapter;
import com.delta.android.WMS.Param.BGoodReceiptReceiveParam;
import com.delta.android.WMS.Param.BIGoodReceiptReceivePortalParam;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class GoodReceiptReceiveLotSnModifyNewActivity extends BaseFlowActivity {

    private DataTable MstTable;// get p.3
    private DataTable GrLotTable;// get p.3
    private DataTable GrLotTableAll;// get p.3
    private DataTable GrrSnTable;// get p.3
    private DataTable GrrSnTableall;// get p.3
    private String ShCfg;// get p.3
    private String RegType;// get p.3
    private String DetItemTotalQty;// get p.3
    private String strSkipQC;
    double receiveCount; // 目前已收的數量
    private String Seq;
    private String StorageId;
    private String ItemId;
    private String BarCodeType = ""; //R:代表是掃描存貨代碼；B:代表是掃描收料代碼
    private DataTable dtTempSN;
    private DataTable _dtSize;// get p.3
    private DataTable _dtSkuLevel; // get p.3
    private ArrayList<String> alSize = new ArrayList<>();
    private LinkedHashMap<String, String> mapSizeKey = new LinkedHashMap<String, String>();
    private LinkedHashMap<String, Integer> hmSizeID = new LinkedHashMap<String, Integer>();
    private DataTable _dtBarCodeData;
    private DataTable _dtExtendData;
    private DataTable dtOrgWgrData;
    private DataTable dtWgrDataGroup;
    private String _VendorItemId = "";
    private String _BarCode = "";
    private String _BarCodeType = "";
    private String _Po = "";
    private String _PoSeq = "";
    static String _scanType = ""; //B:BARCODE、R:REGISTERID
    List<? extends Map<String, Object>> lstSkuLevel;
    private String skuLevelId = "";
    private String dataMode = ""; // 判斷此筆資料為中間表(WGR)新增或手動(MANUAL)新增

    // 宣告控制項物件
    LinearLayout llRecCode;
    Spinner cmbSkuLevel;
    EditText LotId; // 存貨編號 (可能是PalletId, BoxId, RegisterId)
    EditText Qty;
    EditText Uom;
    EditText Cmt;
    EditText MfgDate;
    EditText ExpDate;
    ImageButton IbtnGrLotGridLotIdQRScan;
    Button btnMfgDateClear;
    Button btnExpDateClear;
    EditText BarCode;
    ImageButton IbtnGrBarCodeQRScan;
    RadioGroup radioGroup;
    RadioButton radioQRcode;
    RadioButton radioBarcode;
    Button btnSn;
    Button btnModify;
    ListView lvExtent;
    Spinner cmbSize;
    EditText LotCode;
    RelativeLayout rl1;
    TextView tvGrId;
    TextView tvItemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_good_receipt_receive_lot_sn_modify_new);

        this.initialData();
        // 設定監聽事件
        setListensers();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, getResources().getText(R.string.QRCODE_SCAN_CANCEL).toString(), Toast.LENGTH_SHORT).show();
            }
            else
            {
                String content = result.getContents();

                if (_scanType.equals("B"))
                {
                    //content = "&PSN221110002&VNQCD01&2022-11-17&2023-11-17&PSN221110002002_001&5&12&TEST";

                    if (radioBarcode.isChecked())
                    {
//                        CallBarCodeDisassemble(content, true, "BARCODE_TYPE", "ReceiveBarCode");
                    }
                    else if (radioQRcode.isChecked())
                    {
//                        CallBarCodeDisassemble(content, true, "QRCODE_TYPE", "ReceiveQRCode");
                    }
                }
                else if (_scanType.equals("R"))
                {
                    LotId.setText(content);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onBackPressed()
    {
        //如果要回傳則需要寫此方法
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void initialData() {
        // 取得控制項物件

        llRecCode = findViewById(R.id.llRecCode);
        cmbSkuLevel = findViewById(R.id.cmbSkuLevel);
        LotId = findViewById(R.id.tvGrLotGridLotId);
        Qty = findViewById(R.id.tvGrLotGridQty);
        Uom = findViewById(R.id.tvGrLotGridUom);
        Cmt = findViewById(R.id.tvGrLotGridCmt);
        MfgDate = findViewById(R.id.tvGrLotGridMfgDate);
        ExpDate = findViewById(R.id.tvGrLotGridExpDate);
        IbtnGrLotGridLotIdQRScan = findViewById(R.id.ibtnGrLotGridLotIdQRScan);
        btnMfgDateClear = findViewById(R.id.btnMfgDateClear);
        btnExpDateClear = findViewById(R.id.btnExpDateClear);
        radioGroup = findViewById(R.id.radioGroup1);
        BarCode = findViewById(R.id.tvGrBarCode);
        IbtnGrBarCodeQRScan = findViewById(R.id.ibtnGrBarCodeQRScan);
        radioQRcode = findViewById(R.id.radioQRcode);
        radioBarcode = findViewById(R.id.radioBarcode);
        btnSn = findViewById(R.id.btnSn);
        btnModify = findViewById(R.id.btnModify);
        lvExtent = findViewById(R.id.lvExtent);
        cmbSize = findViewById(R.id.cmbSize);
        LotCode = findViewById(R.id.tvGrLotGridLotCode);
        rl1 = findViewById(R.id.rl1);
        tvGrId = findViewById(R.id.tvGrId);
        tvItemId = findViewById(R.id.tvItemId);

        MstTable = (DataTable) getIntent().getSerializableExtra("MstTable");
        GrLotTable = (DataTable) getIntent().getSerializableExtra("DetLotTable");
        GrLotTableAll = (DataTable) getIntent().getSerializableExtra("DetLotTableAll");
        GrrSnTable = (DataTable) getIntent().getSerializableExtra("DetLotSnTable");
        GrrSnTableall = (DataTable) getIntent().getSerializableExtra("DetLotSnTableAll");
        ShCfg = getIntent().getStringExtra("ActualQtyStatus");
        RegType = getIntent().getStringExtra("RegType");
        DetItemTotalQty = getIntent().getStringExtra("DetItemTotalQty");
        receiveCount = getIntent().getDoubleExtra("DetReceiveItemQty",0);
        _dtSize = (DataTable) getIntent().getSerializableExtra("SizeTable");
        _dtSkuLevel = (DataTable) getIntent().getSerializableExtra("SkuLevel");

        btnSn.setText(getResources().getString(R.string.GR_SN) + " (" + GrrSnTable.Rows.size() + ")");

        //region SkuLevel
        lstSkuLevel = (List<? extends Map<String, Object>>) _dtSkuLevel.toListHashMap();
        SimpleAdapter adapterSkuLevel = new AnotherSimpleArrayAdapter<>(this, lstSkuLevel, android.R.layout.simple_spinner_item, new String[]{"DATA_ID", "DATA_NAME"}, new int[]{0, android.R.id.text1});
        adapterSkuLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmbSkuLevel.setAdapter(adapterSkuLevel);
        cmbSkuLevel.setSelection(lstSkuLevel.size()-1, true);
        //endregion

        //region Size
        alSize.clear();
        for (int i = 0; i < _dtSize.Rows.size(); i++) {
            mapSizeKey.put(_dtSize.Rows.get(i).get("IDNAME").toString(), _dtSize.Rows.get(i).get("SIZE_ID").toString());
            alSize.add(i,_dtSize.Rows.get(i).get("SIZE_ID").toString());
        }

        //Collections.sort(alSize, Collections.reverseOrder());
        Collections.sort(alSize); // List依據字母順序排序

        // 下拉選單預設選項依語系調整
        String strSelectSizeId =  "";
        alSize.add(strSelectSizeId);

        for (int i =0; i < alSize.size(); i++)
        {
            hmSizeID.put(alSize.get(i), i);
        }

        SimpleArrayAdapter adapter = new SimpleArrayAdapter<>(GoodReceiptReceiveLotSnModifyNewActivity.this, android.R.layout.simple_spinner_dropdown_item, alSize);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmbSize.setAdapter(adapter);
        cmbSize.setSelection(alSize.size() - 1, true);
        //endregion

        // 控制項卡控
        if(RegType.equals("ItemID"))
        {
            LotId.setEnabled(false);
        }
        else
        {
            LotId.setEnabled(true);
        }

        if (RegType.equals("PcsSN"))
        {
            Qty.setEnabled(false);
        }
        else
        {
            Qty.setEnabled(true);
        }

        rl1.setVisibility(View.GONE);
        BarCode.setEnabled(false);

        // region -- 設置 cmbSkuLevel --
        int levelPosition = 0;
        skuLevelId = GrLotTable.Rows.get(0).getValue("SKU_LEVEL").toString();
        dataMode = GrLotTable.Rows.get(0).getValue("ADD_MODE").toString();

        for (Map<String, Object> levelMap : lstSkuLevel) {
            if (levelMap.get("DATA_ID").toString().equals(skuLevelId))
                break;
            levelPosition++;
        }

        if (levelPosition == lstSkuLevel.size()) {
            ShowMessage(R.string.WAPG009027); //WAPG009027   查無物料代碼[%s]!
            return;
        }

        cmbSkuLevel.setSelection(levelPosition, true);

        // 若 skuLevelId 為實體，才會出現收料條碼供刷讀
        if (skuLevelId.equals("Entity"))
            llRecCode.setVisibility(View.VISIBLE);
        else
            llRecCode.setVisibility(View.GONE);
        // endregion

        tvGrId.setText(GrLotTable.Rows.get(0).getValue("GR_ID").toString());
        tvItemId.setText(GrLotTable.Rows.get(0).getValue("ITEM_ID").toString());
        LotId.setText(GrLotTable.Rows.get(0).getValue("SKU_NUM").toString()); //LOT_ID
        Qty.setText(GrLotTable.Rows.get(0).getValue("QTY").toString());
        Uom.setText(GrLotTable.Rows.get(0).getValue("UOM").toString());
        Cmt.setText(GrLotTable.Rows.get(0).getValue("CMT").toString());
        if (!GrLotTable.Rows.get(0).getValue("MFG_DATE").toString().equals(""))
            MfgDate.setText(GrLotTable.Rows.get(0).getValue("MFG_DATE").toString().substring(0, 10));//.replace("-","/"));
        if (!GrLotTable.Rows.get(0).getValue("EXP_DATE").toString().equals(""))
            ExpDate.setText(GrLotTable.Rows.get(0).getValue("EXP_DATE").toString().substring(0, 10));//.replace("-","/"));
        MfgDate.setInputType(InputType.TYPE_NULL);
        ExpDate.setInputType(InputType.TYPE_NULL);

        if (!GrLotTable.Rows.get(0).getValue("LOT_CODE").toString().equals(""))
            LotCode.setText(GrLotTable.Rows.get(0).getValue("LOT_CODE").toString());

        if (!GrLotTable.Rows.get(0).getValue("SIZE_ID").toString().equals(""))
        {
            Integer i = hmSizeID.get(GrLotTable.Rows.get(0).getValue("SIZE_ID").toString());
            cmbSize.setSelection(i, true);
        }

        if (!GrLotTable.Rows.get(0).getValue("CMT").toString().equals(""))
            Cmt.setText(GrLotTable.Rows.get(0).getValue("CMT").toString());

        String strBarType = "";
        String strBarTypeId = "";
        String strBarCodeValue = "";
        String strSkuNum = LotId.getText().toString().trim();

        if (!GrLotTable.Rows.get(0).getValue("REC_BARCODE").toString().equals("")) {
            radioGroup.check(R.id.radioBarcode);
            strBarType = "BARCODE_TYPE";
            strBarTypeId = "ReceiveBarCode";
            strBarCodeValue = GrLotTable.Rows.get(0).getValue("REC_BARCODE").toString();
            BarCode.setText(strBarCodeValue);
        }

        if (!GrLotTable.Rows.get(0).getValue("REC_QRCODE").toString().equals("")) {
            radioGroup.check(R.id.radioQRcode);
            strBarType = "QRCODE_TYPE";
            strBarTypeId = "ReceiveQRCode";
            strBarCodeValue = GrLotTable.Rows.get(0).getValue("REC_QRCODE").toString();
            BarCode.setText(strBarCodeValue);
        }

        // 資料來自中間表，無法再修改
        if (dataMode.equals("WGR")) {

            cmbSkuLevel.setEnabled(false);

            setEnable(false, false);

        }  else {

            if (strBarType.length() > 0 && strBarTypeId.length() > 0 && strBarCodeValue.length() > 0) {

                cmbSkuLevel.setEnabled(false);
                getSkuNum(tvGrId.getText().toString().trim(), skuLevelId, strBarType,strBarTypeId, strBarCodeValue, "T", false);
            }

        }

//        String content = "";
//        if (!GrLotTable.Rows.get(0).getValue("REC_BARCODE").toString().equals(""))
//        {
//            content = GrLotTable.Rows.get(0).getValue("REC_BARCODE").toString();
//            CallBarCodeDisassemble(content, false, "BARCODE_TYPE", "ReceiveBarCode");
//        }
//        else if (!GrLotTable.Rows.get(0).getValue("REC_QRCODE").toString().equals(""))
//        {
//            content = GrLotTable.Rows.get(0).getValue("REC_QRCODE").toString();
//            CallBarCodeDisassemble(content, false, "QRCODE_TYPE", "ReceiveQRCode");
//        }

    }

    //設定監聽事件
    private void setListensers() {
        cmbSkuLevel.setOnItemSelectedListener(onSelectSkuLevel);
        MfgDate.setOnClickListener(MfgDateOnClick);
        ExpDate.setOnClickListener(ExpDateOnClick);
        btnMfgDateClear.setOnClickListener(onClickMfgDateClear);
        btnExpDateClear.setOnClickListener(onClickExpDateClear);
        radioGroup.setOnCheckedChangeListener(onCheckRadioGroup);
        IbtnGrLotGridLotIdQRScan.setOnClickListener(IbtnGrLotGridLotIdQRScanOnClick);
        IbtnGrBarCodeQRScan.setOnClickListener(IbtnGrBarCodeQRScanQRScanOnClick);
        LotId.setOnKeyListener(onKeyLotId);
        BarCode.setOnKeyListener(onKeyBarCode);
        btnSn.setOnClickListener(AddNewSN);
        btnModify.setOnClickListener(ModifyLotSn);
    }

    private AdapterView.OnClickListener MfgDateOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setDateMfg();
        }
    };

    private AdapterView.OnClickListener ExpDateOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setDateExp();
        }
    };

    private View.OnClickListener onClickMfgDateClear = new View.OnClickListener() {
        @Override
        public void onClick(View view) { MfgDate.setText(""); }
    };

    private View.OnClickListener onClickExpDateClear = new View.OnClickListener() {
        @Override
        public void onClick(View view) { ExpDate.setText(""); }
    };

    private RadioGroup.OnCheckedChangeListener onCheckRadioGroup = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (group.getId() == R.id.radioGroup1) {
                IbtnGrBarCodeQRScan.setEnabled(true);
                BarCode.setEnabled(true);
            }
        }
    };

    private View.OnKeyListener onKeyLotId = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent event) {

            if (keyCode != KeyEvent.KEYCODE_ENTER)
                return false;

            if (event.getAction() == KeyEvent.ACTION_UP) {

                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                getSkuNum(tvGrId.getText().toString().trim(), skuLevelId, LotId.getText().toString().trim(), "N");

                return true;
            }

            return false;
        }
    };

    private View.OnKeyListener onKeyBarCode = new View.OnKeyListener() {

        @Override
        public boolean onKey(View view, int keyCode, KeyEvent event) {

            if (keyCode != KeyEvent.KEYCODE_ENTER)
                return false;

            if (event.getAction() == KeyEvent.ACTION_UP) {

                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(view.getWindowToken(), 0);

//                if (radioBarcode.isChecked())
//                {
////                    CallBarCodeDisassemble(BarCode.getText().toString().trim(), true, "BARCODE_TYPE", "ReceiveBarCode");
//                }
//                else if (radioQRcode.isChecked())
//                {
////                    CallBarCodeDisassemble(BarCode.getText().toString().trim(), true, "QRCODE_TYPE", "ReceiveQRCode");
//                }

                String strBarType = "";
                String strBarTypeId = "";
                String strBarCodeValue = BarCode.getText().toString().trim();
                String strSkuNum = LotId.getText().toString().trim();

                if (radioBarcode.isChecked()) {

                    strBarType = "BARCODE_TYPE";
                    strBarTypeId = "ReceiveBarCode";

                } else if (radioQRcode.isChecked()) {

                    strBarType = "QRCODE_TYPE";
                    strBarTypeId = "ReceiveQRCode";
                }

                if (strBarType.length() > 0 && strBarTypeId.length() > 0 && strBarCodeValue.length() > 0 && strSkuNum.length() > 0) // 條碼+存貨代碼搜尋
                    getSkuNum(tvGrId.getText().toString().trim(), skuLevelId, strSkuNum, strBarType, strBarTypeId, strBarCodeValue, "N");
                else if (strBarType.length() > 0 && strBarTypeId.length() > 0 && strBarCodeValue.length() > 0) // 僅用條碼搜尋
                    getSkuNum(tvGrId.getText().toString().trim(), skuLevelId, strBarType, strBarTypeId, strBarCodeValue, "N", true);

                return true;
            }

            return false;
        }
    };

    private View.OnClickListener ModifyLotSn = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (dataMode.equals("WGR")) {
                ShowMessage("資料來自中間表，無法修改");
                return;
            }

            double originalQty = 0;
            if (GrLotTable.Rows.size() > 0)
                originalQty = Double.parseDouble(GrLotTable.Rows.get(0).getValue("QTY").toString());

            // 20200731 archie 如果是修改,要記錄舊的修改的數量
            if (GrLotTable.Rows.size() > 0)
            {
                originalQty = Double.parseDouble(GrLotTable.Rows.get(0).getValue("QTY").toString());
            }

            // 檢查物料監控設定
            if (RegType.equals("ItemID"))
            {
                if (LotId.getText().toString().equals("")==false && !(LotId.getText().toString().equals("*")) )
                {
                    ShowMessage("物料[{0}]註冊類別為[{1}],不須輸入批號");//物料[{0}]註冊類別為[{1}],不須輸入批號
                    return;
                }
            }else {
                if (LotId.getText().toString().equals("") || LotId.getText().toString().equals("*") )
                {
                    ShowMessage(R.string.WAPG007005);//物料[{0}]註冊類別為[{1}],必須輸入批號
                    return;
                }else{
                    //指定批號，輸入的批號需要跟單據上的批號一致
                    //String strLotId = GrLotTable.Rows.get(0).getValue("LOT_ID").toString();
                    String strLotId = getIntent().getStringExtra("LotId");
                    if(!strLotId.equals("*") && !LotId.getText().toString().equals(strLotId)){
                        Object[] args = new Object[3];
                        args[0] = MstTable.Rows.get(0).getValue("GR_ID").toString();
                        args[1] = GrLotTable.Rows.get(0).getValue("SEQ").toString();
                        args[2] = GrLotTable.Rows.get(0).getValue("LOT_ID").toString();
                        ShowMessage(R.string.WAPG007021); //WAPG007021  單據[%s],項次[%s]有指定批號[%s]，請確認!
                        return;
                    }
                }
            }

            // 檢查數量是否為空
            if (Qty.getText().toString().equals("")) {
                ShowMessage(R.string.WAPG007006);
                return;
            }

            if (MfgDate.getText().toString().equals(""))
            {
                ShowMessage(R.string.WAPG007023);
                return;
            }

            if (ExpDate.getText().toString().equals(""))
            {
                ShowMessage(R.string.WAPG007024);
                return;
            }
            // 檢查單據設定
            /*switch (ShCfg)
            {
                case "Less":
                case "Equal":
                    if ( Double.parseDouble(Qty.getText().toString()) + receiveCount - originalQty > Double.parseDouble(DetItemTotalQty) )
                    {
                        ShowMessage(R.string.WAPG007013,
                                MstTable.Rows.get(0).getValue("GR_ID").toString(),
                                GrLotTable.Rows.get(0).getValue("SEQ").toString(),
                                (Double.parseDouble(Qty.getText().toString()) + receiveCount - originalQty),
                                DetItemTotalQty);
                        return;
                    }
                    break;

                case "More":
                    break;
            }*/
            // 批號是否重複
            if (GrLotTableAll != null && GrLotTableAll.Rows.size() > 0) {
                int iMaxLot = 1;
                int count = 0;
                for (DataRow dr : GrLotTableAll.Rows) {
                    if (dr.getValue("LOT_ID").toString().equals(LotId.getText().toString().toUpperCase().trim()) //20200729 archie 轉大寫
                            && dr.getValue("SEQ").toString().equals(Seq))
                        count++;
                }
                if (count > iMaxLot) {
                    ShowMessage(R.string.WAPG007007);
                    return;
                }
            }

            // 記錄原資料
            String seq = GrLotTable.Rows.get(0).getValue("SEQ").toString();
            String storageId = GrLotTable.Rows.get(0).getValue("STORAGE_ID").toString();
            String skipQc = GrLotTable.Rows.get(0).getValue("SKIP_QC").toString();

            // 先刪除本來的資料
            for (int i = 0; i < GrLotTableAll.Rows.size(); i++) {
                if (GrLotTableAll.Rows.get(i).getValue("LOT_ID").toString().equals(GrLotTable.Rows.get(0).getValue("LOT_ID").toString()) &&
                    GrLotTableAll.Rows.get(i).getValue("SEQ").toString().equals(GrLotTable.Rows.get(0).getValue("SEQ").toString())) {
                    GrLotTableAll.Rows.remove(i);
                }
            }

            // 再增加修改的資料
            if (dtOrgWgrData != null && dtOrgWgrData.Rows.size() > 0) {

                for( DataRow dr : dtOrgWgrData.Rows) {

                    //region GrrDet

                    DataRow drNew = GrLotTableAll.newRow();
                    drNew.setValue("SEQ", seq);
                    drNew.setValue("STORAGE_ID", storageId);
                    drNew.setValue("ITEM_ID", dr.get("ITEM_ID").toString());
                    drNew.setValue("LOT_ID", dr.get("LOT_ID").toString());
                    drNew.setValue("QTY", dr.get("QTY").toString());
                    drNew.setValue("UOM", ""); // 中間表沒有
                    drNew.setValue("CMT", dr.get("CMT").toString());
                    drNew.setValue("SKIP_QC", skipQc);
                    drNew.setValue("MFG_DATE", dr.get("MFG_DATE").toString());
                    drNew.setValue("EXP_DATE", dr.get("EXP_DATE").toString());
                    drNew.setValue("LOT_CODE", dr.get("LOT_CODE").toString());
                    drNew.setValue("SIZE_ID", dr.get("SIZE_ID").toString());
                    drNew.setValue("REC_BARCODE", ""); // 中間表沒有
                    drNew.setValue("REC_QRCODE", ""); // 中間表沒有
                    drNew.setValue("VENDOR_ITEM_ID", ""); // 中間表沒有
                    drNew.setValue("BOX1_ID", dr.get("BOX1_ID").toString());
                    drNew.setValue("BOX2_ID", dr.get("BOX2_ID").toString());
                    drNew.setValue("BOX3_ID", dr.get("BOX3_ID").toString());
                    drNew.setValue("PALLET_ID", dr.get("PALLET_ID").toString());

                    String grrDetSnRefKey = "";
                    if (GrrSnTable.Rows.size() > 0)
                        GrrSnTable.Rows.get(0).getValue("GRR_DET_SN_REF_KEY").toString();

                    drNew.setValue("GRR_DET_SN_REF_KEY", grrDetSnRefKey);

                    GrLotTableAll.Rows.add(drNew);

                    // endregion

                }

            } else {

                //region GrrDet

                DataRow drNew = GrLotTableAll.newRow();
                drNew.setValue("SEQ", seq);
                drNew.setValue("STORAGE_ID", storageId);
                drNew.setValue("ITEM_ID", tvItemId.getText().toString().trim());
                drNew.setValue("LOT_ID", LotId.getText().toString().trim());
                drNew.setValue("QTY", Qty.getText().toString().trim());
                drNew.setValue("UOM", Uom.getText().toString().trim());
                drNew.setValue("CMT", Cmt.getText().toString().trim());
                drNew.setValue("SKIP_QC", skipQc);
                drNew.setValue("MFG_DATE", MfgDate.getText().toString().trim().substring(0,10) + " 00:00:00");
                drNew.setValue("EXP_DATE", ExpDate.getText().toString().trim().substring(0,10) + " 23:59:59");
                drNew.setValue("LOT_CODE", LotCode.getText().toString().trim());
                drNew.setValue("SIZE_ID", cmbSize.getSelectedItem().toString());

                if (radioBarcode.isChecked() && !BarCode.getText().toString().trim().equals(""))
                    drNew.setValue("REC_BARCODE", BarCode.getText().toString().trim());
                else if (radioQRcode.isChecked() && !BarCode.getText().toString().trim().equals(""))
                    drNew.setValue("REC_QRCODE", BarCode.getText().toString().trim());
                else if (!radioBarcode.isChecked() && !radioQRcode.isChecked()) {
                    drNew.setValue("REC_BARCODE", "");
                    drNew.setValue("REC_QRCODE", "");
                }

                drNew.setValue("VENDOR_ITEM_ID", _VendorItemId);
                drNew.setValue("BOX1_ID", "");
                drNew.setValue("BOX2_ID", "");
                drNew.setValue("BOX3_ID", "");
                drNew.setValue("PALLET_ID", "");

                String grrDetSnRefKey = "";
                if (GrrSnTable.Rows.size() > 0)
                    GrrSnTable.Rows.get(0).getValue("GRR_DET_SN_REF_KEY").toString();

                drNew.setValue("GRR_DET_SN_REF_KEY", grrDetSnRefKey);

                GrLotTableAll.Rows.add(drNew);

                // endregion
            }


//            for (DataRow drAll : GrLotTableAll.Rows) {
//                if (drAll.getValue("LOT_ID").toString().equals(GrLotTable.Rows.get(0).getValue("LOT_ID").toString())
//                        && drAll.getValue("SEQ").toString().equals(GrLotTable.Rows.get(0).getValue("SEQ").toString())
//                        //&& drAll.getValue("GRR_DET_REF_KEY").toString().equals(GrLotTable.Rows.get(0).getValue("GRR_DET_REF_KEY").toString())
//                )
//                {
//                    drAll.setValue("LOT_ID", LotId.getText().toString().toUpperCase().trim()); //20200729 archie 轉大寫
//                    drAll.setValue("QTY", Qty.getText().toString());
//                    drAll.setValue("UOM", Uom.getText().toString());
//                    drAll.setValue("CMT", Cmt.getText().toString());
//                    if (!MfgDate.getText().toString().equals(""))
//                        drAll.setValue("MFG_DATE", MfgDate.getText().toString() + " 00:00:00");
//                    else
//                        drAll.setValue("MFG_DATE", "");
//                    if (!ExpDate.getText().toString().equals(""))
//                        drAll.setValue("EXP_DATE", ExpDate.getText().toString() + " 23:59:59");
//                    else
//                        drAll.setValue("EXP_DATE", "");
//
//                    drAll.setValue("LOT_CODE", LotCode.getText().toString());
//                    drAll.setValue("SIZE_ID", cmbSize.getSelectedItem().toString());
//
//                    if (!_BarCodeType.equals(""))
//                    {
//                        if (_BarCodeType.equals("BARCODE_TYPE"))
//                        {
//                            drAll.setValue("REC_BARCODE", _BarCode);
//                        }
//                        else if (_BarCodeType.equals("QRCODE_TYPE"))
//                        {
//                            drAll.setValue("REC_QRCODE", _BarCode);
//                        }
//                    }
//
//                    if (!_VendorItemId.equals(""))
//                    {
//                        drAll.setValue("VENDOR_ITEM_ID", _VendorItemId);
//                    }
//
//                    break;
//                }
//                    /*else
//                    {
//                        if (!drAll.getValue("MFG_DATE").toString().equals(""))
//                        {
//                            drAll.setValue("MFG_DATE", drAll.getValue("MFG_DATE").toString().substring(0,10).replace("-","/") + " 00:00:00");
//                        }
//
//                        if (!drAll.getValue("EXP_DATE").toString().equals(""))
//                        {
//                            drAll.setValue("EXP_DATE", drAll.getValue("EXP_DATE").toString().substring(0,10).replace("-","/") + " 23:59:59");
//                        }
//                    }*/
//            }

            CallBModule();
        }
    };

    private View.OnClickListener AddNewSN = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if(RegType.equals("PcsSN")) //20201020 Hans RegType為 PcsSN時進入
            {
                List<String> lstRefKeys = new ArrayList<>();

                for (int i = 0; i < GrrSnTable.Rows.size(); i++) {

                    String refKey = GrrSnTable.Rows.get(i).getValue("GRR_DET_SN_REF_KEY").toString();
                    if (!lstRefKeys.contains(refKey))
                        lstRefKeys.add(refKey);
                }

                String[] aryRefKeys = new String[lstRefKeys.size()];
                lstRefKeys.toArray(aryRefKeys);

                int snQty = (int) Double.parseDouble(Qty.getText().toString().trim());
                ShowSnDialog(aryRefKeys, snQty);
            }
        }
    };

    private View.OnClickListener IbtnGrLotGridLotIdQRScanOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            IntentIntegrator integrator = new IntentIntegrator(GoodReceiptReceiveLotSnModifyNewActivity.this);
            // 設置要掃描的條碼類型，ONE_D_CODE_TYPES：一維碼，QR_CODE_TYPES-二維碼
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("BarCode Scan"); //底部的提示文字
            integrator.setCameraId(0); //前置(1)或者後置(0)攝像頭
            integrator.setBeepEnabled(false); //掃描成功的「嗶嗶」聲
            integrator.setBarcodeImageEnabled(false); //是否保留掃碼成功時候的截圖
            integrator.setCaptureActivity(ScanActivity.class);
            integrator.initiateScan();

            _scanType  = "R";
        }
    };

    private View.OnClickListener IbtnGrBarCodeQRScanQRScanOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!radioQRcode.isChecked() && !radioBarcode.isChecked())
            {
                //WAPG009022 請選擇一維條碼或二維條碼
                ShowMessage(R.string.WAPG009022);
                return;
            }

            IntentIntegrator integrator = new IntentIntegrator(GoodReceiptReceiveLotSnModifyNewActivity.this);
            // 設置要掃描的條碼類型，ONE_D_CODE_TYPES：一維碼，QR_CODE_TYPES-二維碼
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("BarCode Scan"); //底部的提示文字
            integrator.setCameraId(0); //前置(1)或者後置(0)攝像頭
            integrator.setBeepEnabled(false); //掃描成功的「嗶嗶」聲
            integrator.setBarcodeImageEnabled(false); //是否保留掃碼成功時候的截圖
            integrator.setCaptureActivity(ScanActivity.class);
            integrator.initiateScan();

            _scanType  = "B";
        }
    };

    private Spinner.OnItemSelectedListener onSelectSkuLevel = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {

            skuLevelId = "";

            if (position != lstSkuLevel.size()-1) {
                Map<String, String> skuLevelMap = (Map<String, String>)parent.getItemAtPosition(position);
                skuLevelId = skuLevelMap.get("DATA_ID");
            }

            // 若 skuLevelId 為實體，才會出現收料條碼供刷讀
            if (skuLevelId.equals("Entity"))
                llRecCode.setVisibility(View.VISIBLE);
            else
                llRecCode.setVisibility(View.GONE);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            return;
        }
    };

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
                MfgDate.setText(sb);
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
                ExpDate.setText(sb);
                dialog.cancel();
            }
        });

    }

    public void onClickMfgDateClear(View v) {
        MfgDate.setText("");
    }

    public void onClickExpDateClear(View v) {
        ExpDate.setText("");
    }

    private void CallBModule() {
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BGoodReceiptReceive");
        bmObj.setModuleID("");
        bmObj.setRequestID("GRR");
        bmObj.params = new Vector<ParameterInfo>();

        BGoodReceiptReceiveParam sheet = new BGoodReceiptReceiveParam();
        BGoodReceiptReceiveParam.GrrMasterObj sheet1 = sheet.new GrrMasterObj();

        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.INV.Parameter.GrrWithPackingInfoMasterObj", "bmWMS.INV.Param");
        MesClass mesClassEnum = new MesClass(vListEnum);
        String strGrrMstObj = mesClassEnum.generateFinalCode(sheet1.GetGrrSheet(MstTable, GrLotTableAll, GrrSnTableall));

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BGoodReceiptReceiveParam.TrxType);
        param1.setParameterValue("Modify");
        bmObj.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BGoodReceiptReceiveParam.GrrMasterObj);
        param2.setNetParameterValue(strGrrMstObj);// setNetParameterValue2?
        bmObj.params.add(param2);

        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BGoodReceiptReceiveParam.ExecuteCheckStock); // 20220804 Add by Ikea 是否執行盤點檢查
        param3.setNetParameterValue2("false"); // 因為「收料」並無儲位資訊，「收料完成」才有
        bmObj.params.add(param3);

        CallBModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    ShowMessage(R.string.WAPG007010, new ShowMessageEvent() {
                        @Override
                        public void onDismiss() {
                            finish();
                        }
                    });
                }
            }
        });
    }

    private void ShowSnDialog(final String[] strGrrDetSNRefKey, int snQty) {
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        final View viewDialog = inflater.inflate(R.layout.activity_good_nonreceipt_receive_lot_sn,null );
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(GoodReceiptReceiveLotSnModifyNewActivity.this);
        builder.setView(viewDialog);

        final android.app.AlertDialog dialog = builder.create();
        dialog.setCancelable(false);//只允許按下dialog裡的按鍵才能關閉dialog
        dialog.show();

        final int intSNQty = snQty;

        //region create dtTempSN
        final ArrayList<String> alTempSN = new ArrayList<String>();
        dtTempSN = new DataTable();
        DataColumn dcGrrDetSNRefKey = new DataColumn("GRR_DET_SN_REF_KEY");
        DataColumn dcSN = new DataColumn("SN_ID");
        dtTempSN.addColumn(dcGrrDetSNRefKey);
        dtTempSN.addColumn(dcSN);

        if (GrrSnTable.Rows.size() > 0) {
            for (int i = GrrSnTable.Rows.size() -1; i >= 0; i--) {

                String refKey = GrrSnTable.Rows.get(i).getValue("GRR_DET_SN_REF_KEY").toString();
                String snID = GrrSnTable.Rows.get(i).getValue("SN_ID").toString();

                for (String strKey : strGrrDetSNRefKey) {

                    if (refKey.equals(strKey)) {

                        //把 dtSN符合 Grr_Det_SN_Ref_Key的資料搬到 dtTempSN
                        DataRow drNew = dtTempSN.newRow();
                        drNew.setValue("GRR_DET_SN_REF_KEY", refKey);
                        drNew.setValue("SN_ID", snID);
                        dtTempSN.Rows.add(drNew);
                        alTempSN.add(snID);

                        //dtSN.Rows.remove(i);
                    }
                }
            }
        }

        LayoutInflater inflaterSNTemp = LayoutInflater.from(getApplicationContext());
        GoodNonreceiptReceiveSNAdapter adapter = new GoodNonreceiptReceiveSNAdapter(dtTempSN, inflaterSNTemp);
        ListView lsSN = viewDialog.findViewById(R.id.lvReceiveSN);
        lsSN.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        //endregion

        //region 刷入SN
        EditText etSN = viewDialog.findViewById(R.id.edSnId);
        etSN.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
                if (event.getAction() == KeyEvent.ACTION_DOWN){

                    EditText etSN = view.findViewById(R.id.edSnId);
                    String snID = etSN.getText().toString().trim();

                    // 判斷SN是否重複
                    if (alTempSN.contains(snID)){
                        ShowMessage(R.string.WAPG009015);//WAPG009015    序號重複!
                        return false;
                    }

                    DataRow drNew = dtTempSN.newRow();
                    drNew.setValue("GRR_DET_SN_REF_KEY",strGrrDetSNRefKey);
                    drNew.setValue("SN_ID",snID);
                    dtTempSN.Rows.add(drNew);
                    alTempSN.add(snID);

                    // ListView 顯示
                    LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
                    GoodNonreceiptReceiveSNAdapter adapter = new GoodNonreceiptReceiveSNAdapter(dtTempSN, inflater);
                    ListView lsSN = viewDialog.findViewById(R.id.lvReceiveSN);
                    lsSN.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    // 清空刷入的欄位
                    etSN.getText().clear();
                    return true;
                }
                return false;
            }
        });
        //endregion

        //region Confirm 按鈕事件
        Button btnConfirmDialog = viewDialog.findViewById(R.id.btnSNConfirm);
        btnConfirmDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (dtTempSN.Rows.size() < intSNQty){
                    ShowMessage(R.string.WAPG009009);//WAPG009009    未滿需求量!
                    return;
                }
                if(dtTempSN.Rows.size() > intSNQty){
                    ShowMessage(R.string.WAPG009012); //WAPG009012    已超出需求量!
                    return;
                }

                //移動 dtTempSN到 dtSN
                GrrSnTable = new DataTable();
                for (int i = 0 ; i < dtTempSN.Rows.size(); i++){
                    DataRow drNew = GrrSnTable.newRow();
                    drNew.setValue("GRR_DET_SN_REF_KEY",dtTempSN.Rows.get(i).get("GRR_DET_SN_REF_KEY").toString());
                    drNew.setValue("SN_ID",dtTempSN.Rows.get(i).get("SN_ID").toString());
                    GrrSnTable.Rows.add(drNew);
                }

                //刪除舊的
                for (int i = GrrSnTableall.Rows.size()-1; i >= 0; i--) {

                    if (!GrrSnTableall.Rows.get(i).getValue("GRR_DET_SN_REF_KEY").toString().equals(strGrrDetSNRefKey)) continue;

                    GrrSnTableall.Rows.remove(i);
                }
//                for (DataRow row : GrrSnTableall.Rows)
//                {
//                    if (!row.getValue("GRR_DET_SN_REF_KEY").toString().equals(strGrrDetSNRefKey)) continue;
//
//                    GrrSnTableall.Rows.remove(row);
//                }

                for (DataRow row : GrrSnTable.Rows)
                {
                    DataRow drNew = GrrSnTableall.newRow();
                    drNew.setValue("GRR_DET_SN_REF_KEY",row.getValue("GRR_DET_SN_REF_KEY").toString());
                    drNew.setValue("SN_ID",row.getValue("SN_ID").toString());
                    GrrSnTableall.Rows.add(drNew);
                }

                btnSn.setText(getResources().getString(R.string.GR_SN) + " (" + GrrSnTable.Rows.size() + ")");
                dtTempSN = null;
                alTempSN.clear();
                dialog.dismiss();
            }
        });
        //endregion

        //region Cancel 按鈕事件
        Button btnCloseDialog = viewDialog.findViewById(R.id.btnSNCancel);
        btnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*//移動 dtTempSN到 dtSN
                for (int i = 0 ; i < dtTempSN.Rows.size(); i++){
                    DataRow drNew = dtSN.newRow();
                    drNew.setValue("GRR_DET_SN_REF_KEY",dtTempSN.Rows.get(i).get("GRR_DET_SN_REF_KEY").toString());
                    drNew.setValue("SN_ID",dtTempSN.Rows.get(i).get("SN_ID").toString());
                    dtSN.Rows.add(drNew);
                }*/

                btnSn.setText(getResources().getString(R.string.GR_SN) + " (" + GrrSnTable.Rows.size() + ")");
                dtTempSN = null;
                dialog.dismiss();
            }
        });
        //endregion
    }

    private void ClearData(boolean remainSkuLevel) {
        BarCode.setText("");
        LotId.setText("");
        Qty.setText("");
        Uom.setText("");
        MfgDate.setText("");
        ExpDate.setText("");
        LotCode.setText("");
        cmbSize.setSelection(alSize.size() - 1, true);
        Cmt.setText("");
        _BarCode = "";
        _VendorItemId = "";
        _scanType = "";
        _Po = "";
        _PoSeq = "";

        if (remainSkuLevel == false) {
            cmbSkuLevel.setSelection(lstSkuLevel.size()-1, true);
            skuLevelId = "";
        }

        setEnable(true, true);

        _dtBarCodeData = new DataTable();
        _dtExtendData = new DataTable();
        dtWgrDataGroup = new DataTable();
        dtOrgWgrData = new DataTable();

        if (GrrSnTable.Rows.size() > 0) {
            for (int i = GrrSnTable.Rows.size() -1; i >= 0; i--){
                GrrSnTable.Rows.remove(i);
            }
        }

        LayoutInflater inflater = getLayoutInflater();
        GoodReceiptReceiveExtendGridAdapter adapter = new GoodReceiptReceiveExtendGridAdapter(_dtExtendData, inflater);
        lvExtent.setAdapter(adapter);
    }

    private void setEnable(boolean enable, boolean qrScanEnable) {
        LotId.setEnabled(enable);
        IbtnGrLotGridLotIdQRScan.setEnabled(enable);
        Qty.setEnabled(enable);
        Uom.setEnabled(enable);
        MfgDate.setEnabled(enable);
        ExpDate.setEnabled(enable);
        LotCode.setEnabled(enable);
        cmbSize.setEnabled(enable);
        Cmt.setEnabled(enable);
        btnMfgDateClear.setEnabled(enable);
        btnExpDateClear.setEnabled(enable);

        radioQRcode.setEnabled(qrScanEnable);
        radioBarcode.setEnabled(qrScanEnable);
        IbtnGrBarCodeQRScan.setEnabled(qrScanEnable);
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

    private class AnotherSimpleArrayAdapter<T> extends SimpleAdapter {
        public AnotherSimpleArrayAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        //複寫這個方法，使返回的數據沒有最後一項
        @Override
        public int getCount() {
            // don't display last item. It is used as hint.
            int count = super.getCount();
            return count > 0 ? count - 1 : count;
        }
    }

    // 只有輸入條碼
    private void getSkuNum(String strGrId, String strSkuLevel, String strBarType, String strBarTypeId, final String strBarCodeValue, String isReceived, final boolean isNew) {

        // 條碼拆解
        BModuleObject biObj1 = new BModuleObject();
        biObj1.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIGoodReceiptReceivePortal");
        biObj1.setModuleID("BIFetchReceiptBarCodeResult");
        biObj1.setRequestID("BIFetchReceiptBarCodeResult");
        biObj1.params = new Vector<>();

        // Input param
        ParameterInfo paramGrId = new ParameterInfo();
        paramGrId.setParameterID(BIGoodReceiptReceivePortalParam.GrId);
        paramGrId.setParameterValue(strGrId);
        biObj1.params.add(paramGrId);

        ParameterInfo paramSkuLevel = new ParameterInfo();
        paramSkuLevel.setParameterID(BIGoodReceiptReceivePortalParam.SkuLevel);
        paramSkuLevel.setParameterValue(strSkuLevel);
        biObj1.params.add(paramSkuLevel);

        ParameterInfo paramBarCodeType = new ParameterInfo();
        paramBarCodeType.setParameterID(BIGoodReceiptReceivePortalParam.BarCodeType);
        paramBarCodeType.setParameterValue(strBarType);
        biObj1.params.add(paramBarCodeType);

        ParameterInfo paramBarCodeTypeId = new ParameterInfo();
        paramBarCodeTypeId.setParameterID(BIGoodReceiptReceivePortalParam.BarCodeTypeId);
        paramBarCodeTypeId.setParameterValue(strBarTypeId);
        biObj1.params.add(paramBarCodeTypeId);

        ParameterInfo paramBarCodeValue = new ParameterInfo();
        paramBarCodeValue.setParameterID(BIGoodReceiptReceivePortalParam.BarCodeValue);
        paramBarCodeValue.setParameterValue(strBarCodeValue);
        biObj1.params.add(paramBarCodeValue);

        ParameterInfo paramIsReceived = new ParameterInfo();
        paramIsReceived.setParameterID(BIGoodReceiptReceivePortalParam.IsReceived);
        paramIsReceived.setParameterValue(isReceived);
        biObj1.params.add(paramIsReceived);

        BarCode.getText().clear(); // 先清除，若拆解成功再回填

        CallBIModule(biObj1, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {

                if (CheckBModuleReturnInfo(bModuleReturn)) {

                    String askChangeSkuLevel = "";

                    if (bModuleReturn.getReturnList().get("BIFetchReceiptBarCodeResult") != null) {

                        if (bModuleReturn.getReturnList().get("BIFetchReceiptBarCodeResult").get(BIGoodReceiptReceivePortalParam.ChangeSkuLevel) != null) {
                            askChangeSkuLevel = bModuleReturn.getReturnList().get("BIFetchReceiptBarCodeResult").get(BIGoodReceiptReceivePortalParam.ChangeSkuLevel).toString().replaceAll("\"", "");
                        }

                        if (!askChangeSkuLevel.equals("")) {

                            final String [] skuData = askChangeSkuLevel.split(";");
                            Object[] args = new Object[2];
                            args[0] = skuData[0];
                            args[1] = skuData[1];

                            String showMsg = String.format(getResString(getResources().getString(R.string.CHANGE_SKU_LEVEL)), args);

                            ShowMessage(showMsg, new ShowMessageEvent() {
                                @Override
                                public void onDismiss() {

                                    // region -- 設置 cmbSkuLevel --
                                    int levelPosition = 0;
                                    skuLevelId = skuData[0];

                                    for (Map<String, Object> levelMap : lstSkuLevel) {
                                        if (levelMap.get("DATA_ID").toString().equals(skuLevelId))
                                            break;
                                        levelPosition++;
                                    }

                                    if (levelPosition == lstSkuLevel.size()) {
                                        ShowMessage(R.string.WAPG009027); //WAPG009027   查無物料代碼[%s]!
                                        return;
                                    }

                                    SimpleAdapter adapterSkuLevel = new AnotherSimpleArrayAdapter<>(getApplicationContext(), lstSkuLevel, android.R.layout.simple_spinner_item, new String[]{"DATA_ID", "DATA_NAME"}, new int[]{0, android.R.id.text1});
                                    adapterSkuLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    cmbSkuLevel.setAdapter(adapterSkuLevel);
                                    cmbSkuLevel.setSelection(levelPosition, true);
                                    // endregion

                                    LotId.setText(skuData[1]);
                                }
                            });
                        }

                    } else {

                        if (isNew == true) {
                            ClearData(true);
                        }

                        _dtBarCodeData = bModuleReturn.getReturnJsonTables().get("BIFetchReceiptBarCodeResult").get("BARCODE_DATA");
                        _dtExtendData = bModuleReturn.getReturnJsonTables().get("BIFetchReceiptBarCodeResult").get("EXTEND_DATA");
                        dtOrgWgrData = bModuleReturn.getReturnJsonTables().get("BIFetchReceiptBarCodeResult").get("dtOrgWgrData");
                        DataTable dtOrgSn = bModuleReturn.getReturnJsonTables().get("BIFetchReceiptBarCodeResult").get("dtSn");

                        String itemId = "";

                        //region 主要條碼資料填值
                        for (DataRow row : _dtBarCodeData.Rows)
                        {
                            switch (row.getValue("BARCODE_VARIABLE_ID").toString())
                            {
                                case "ITEM_ID":
                                    itemId = row.getValue("BARCODE_VALUE").toString();
                                    if (!itemId.equals(tvItemId.getText().toString().trim())) {
                                        ShowMessage("條碼物料代碼[]與項次物料代碼[]不符，請檢查條碼");
                                    }
                                    break;

                                case "REGISTER_ID":
                                    LotId.setText(row.getValue("BARCODE_VALUE").toString());
                                    LotId.setEnabled(false);
                                    IbtnGrLotGridLotIdQRScan.setEnabled(false);
                                    break;

                                case "QTY":
                                    Qty.setText(row.getValue("BARCODE_VALUE").toString());
                                    Qty.setEnabled(false);
                                    break;

                                case "LOT_CODE":
                                    LotCode.setText(row.getValue("BARCODE_VALUE").toString());
                                    LotCode.setEnabled(false);
                                    break;

                                case "MFG_DATE":
                                    MfgDate.setText(row.getValue("BARCODE_VALUE").toString());
                                    MfgDate.setEnabled(false);
                                    btnMfgDateClear.setEnabled(false);
                                    break;

                                case "EXP_DATE":
                                    ExpDate.setText(row.getValue("BARCODE_VALUE").toString());
                                    ExpDate.setEnabled(false);
                                    btnExpDateClear.setEnabled(false);
                                    break;

                                case "SIZE_ID":
                                    Integer i = hmSizeID.get(row.getValue("BARCODE_VALUE").toString());
                                    cmbSize.setSelection(i, true);
                                    cmbSize.setEnabled(false);
                                    break;

                                case "VENDOR_ITEM_ID":
                                    _VendorItemId = row.getValue("BARCODE_VALUE").toString();
                                    break;

                                case "CMT":
                                    Cmt.setText(row.getValue("BARCODE_VALUE").toString());
                                    Cmt.setEnabled(false);
                                    break;

                                case "PO":
                                    _Po = row.getValue("BARCODE_VALUE").toString();
                                    break;

                                case "PO_ITEM":
                                case "PO_SEQ":
                                    _PoSeq = row.getValue("BARCODE_VALUE").toString();
                                    break;

                                default:
                                    break;
                            }
                        }
                        //endregion

                        //region Extend
                        if (_dtExtendData.Rows.size() > 0)
                        {
                            rl1.setVisibility(View.VISIBLE);
                            LayoutInflater inflater = getLayoutInflater();
                            GoodReceiptReceiveExtendGridAdapter adapter = new GoodReceiptReceiveExtendGridAdapter(_dtExtendData, inflater);
                            lvExtent.setAdapter(adapter);
                        }
                        else
                        {
                            rl1.setVisibility(View.GONE);
                        }
                        //endregion

                        if (radioBarcode.isChecked())
                            _BarCodeType = "BARCODE";
                        else if (radioQRcode.isChecked())
                            _BarCodeType = "QRCODE";

                        BarCode.setText(strBarCodeValue);

                        //region 同步 PcsSN 的表
                        if (itemId.equals(""))
                            return;

                        if (RegType.equals("PcsSN")) {

                            List<String> lstRefKeys = new ArrayList<>();

                            for (int i = 0; i < dtOrgWgrData.Rows.size(); i++) {
                                String refKey = dtOrgWgrData.Rows.get(i).getValue("GRR_DET_SN_REF_KEY").toString().trim();

                                if (refKey.equals(""))
                                    continue;

                                if (!lstRefKeys.contains(refKey))
                                    lstRefKeys.add(refKey);
                            }

                            if (lstRefKeys.size() == 0)
                                return;

                            String[] aryRefKeys = new String[lstRefKeys.size()];
                            lstRefKeys.toArray(aryRefKeys);

                            for(DataRow dr : dtOrgSn.Rows) {
                                DataRow drNew = GrrSnTable.newRow();
                                drNew.setValue("GRR_DET_SN_REF_KEY", dr.get("GRR_DET_SN_REF_KEY").toString().trim());
                                drNew.setValue("SN_ID", dr.get("SN_ID").toString().trim());
                                GrrSnTable.Rows.add(drNew);
                            }

                            //_GrrDetSNRerKey = aryRefKeys;
                            btnSn.setText(getResources().getString(R.string.GR_SN) + " (" + GrrSnTable.Rows.size() + ")");

                        }
                        //endregion
                    }
                }
            }
        });
    }

    // 只有輸入存貨編碼
    private void getSkuNum(String strGrId, String strSkuLevel, String strSkuNum, final String isReceived) {

        // 條碼拆解
        BModuleObject biObj1 = new BModuleObject();
        biObj1.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIGoodReceiptReceivePortal");
        biObj1.setModuleID("BIFetchReceiptBySkuNum");
        biObj1.setRequestID("BIFetchReceiptBySkuNum");
        biObj1.params = new Vector<>();

        // Input param
        ParameterInfo paramGrId = new ParameterInfo();
        paramGrId.setParameterID(BIGoodReceiptReceivePortalParam.GrId);
        paramGrId.setParameterValue(strGrId);
        biObj1.params.add(paramGrId);

        ParameterInfo paramSkuLevel = new ParameterInfo();
        paramSkuLevel.setParameterID(BIGoodReceiptReceivePortalParam.SkuLevel);
        paramSkuLevel.setParameterValue(strSkuLevel);
        biObj1.params.add(paramSkuLevel);

        ParameterInfo paramSkuNum = new ParameterInfo();
        paramSkuNum.setParameterID(BIGoodReceiptReceivePortalParam.SkuNum);
        paramSkuNum.setParameterValue(strSkuNum);
        biObj1.params.add(paramSkuNum);

        ParameterInfo paramIsReceived = new ParameterInfo();
        paramIsReceived.setParameterID(BIGoodReceiptReceivePortalParam.IsReceived);
        paramIsReceived.setParameterValue(isReceived);
        biObj1.params.add(paramIsReceived);

        CallBIModule(biObj1, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {

            if (CheckBModuleReturnInfo(bModuleReturn)) {

                    String askChangeSkuLevel = "";

                    if (bModuleReturn.getReturnList().get("BIFetchReceiptBySkuNum") != null) {

                        if (bModuleReturn.getReturnList().get("BIFetchReceiptBySkuNum").get(BIGoodReceiptReceivePortalParam.ChangeSkuLevel) != null) {
                            askChangeSkuLevel = bModuleReturn.getReturnList().get("BIFetchReceiptBySkuNum").get(BIGoodReceiptReceivePortalParam.ChangeSkuLevel).toString().replaceAll("\"", "");
                        }

                        if (!askChangeSkuLevel.equals("")) {

                            final String [] skuData = askChangeSkuLevel.split(";");
                            Object[] args = new Object[2];
                            args[0] = skuData[0];
                            args[1] = skuData[1];

                            String showMsg = String.format(getResString(getResources().getString(R.string.CHANGE_SKU_LEVEL)), args);

                            ShowMessage(showMsg, new ShowMessageEvent() {
                                @Override
                                public void onDismiss() {

                                    // region -- 設置 cmbSkuLevel --
                                    int levelPosition = 0;
                                    skuLevelId = skuData[0];

                                    for (Map<String, Object> levelMap : lstSkuLevel) {
                                        if (levelMap.get("DATA_ID").toString().equals(skuLevelId))
                                            break;
                                        levelPosition++;
                                    }

                                    if (levelPosition == lstSkuLevel.size()) {
                                        ShowMessage(R.string.WAPG009027); //WAPG009027   查無物料代碼[%s]!
                                        return;
                                    }

                                    SimpleAdapter adapterSkuLevel = new AnotherSimpleArrayAdapter<>(getApplicationContext(), lstSkuLevel, android.R.layout.simple_spinner_item, new String[]{"DATA_ID", "DATA_NAME"}, new int[]{0, android.R.id.text1});
                                    adapterSkuLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    cmbSkuLevel.setAdapter(adapterSkuLevel);
                                    cmbSkuLevel.setSelection(levelPosition, true);
                                    // endregion

                                    LotId.setText(skuData[1]);

                                    getSkuNum(tvGrId.getText().toString().trim(), skuData[0], skuData[1], isReceived);
                                }
                            });
                        }

                    } else {

                        ClearData(true);
                        setEnable(true, false);

                        dtWgrDataGroup = bModuleReturn.getReturnJsonTables().get("BIFetchReceiptBySkuNum").get("dtWgrWithSkuLevel");
                        dtOrgWgrData = bModuleReturn.getReturnJsonTables().get("BIFetchReceiptBySkuNum").get("dtOrgWgrData");
                        DataTable dtOrgSn = bModuleReturn.getReturnJsonTables().get("BIFetchReceiptBySkuNum").get("dtSn");

                        String skuNum = dtWgrDataGroup.Rows.get(0).getValue("SKU_NUM").toString().trim();
                        String itemId = dtWgrDataGroup.Rows.get(0).getValue("ITEM_ID").toString().trim();
                        String lotId = dtWgrDataGroup.Rows.get(0).getValue("LOT_ID").toString().trim();
//                        double qty = Double.parseDouble(dtWgrDataGroup.Rows.get(0).getValue("QTY").toString().trim());
                        String qty = dtWgrDataGroup.Rows.get(0).getValue("QTY").toString().trim();
                        String mfgDate = dtWgrDataGroup.Rows.get(0).getValue("MFG_DATE").toString().trim().substring(0,10) + " 00:00:00";
                        String expDate = dtWgrDataGroup.Rows.get(0).getValue("EXP_DATE").toString().trim().substring(0,10) + " 23:59:59";
                        String lotCode = dtWgrDataGroup.Rows.get(0).getValue("LOT_CODE").toString().trim();
                        String sizeId = dtWgrDataGroup.Rows.get(0).getValue("SIZE_ID").toString().trim();
                        String cmt = dtWgrDataGroup.Rows.get(0).getValue("CMT").toString().trim();

                        //region 中間表(已 group by)填值
                        if (itemId.length() > 0) {

                            if (!itemId.equals(tvItemId.getText().toString().trim())) {
                                ShowMessage("");
                                return;
                            }
                        }

                        if (lotId.length() > 0) {
                            LotId.setText(skuNum);
                            //LotId.setEnabled(false);
                        }

                        if (qty.length() > 0) {
                            Qty.setText(qty);
                            Qty.setEnabled(false);
                        }

                        if (lotCode.length() > 0) {
                            LotCode.setText(qty);
                            LotCode.setEnabled(false);
                        }

                        if (mfgDate.length() > 0) {
                            MfgDate.setText(mfgDate);
                            MfgDate.setEnabled(false);
                            btnMfgDateClear.setEnabled(false);
                        }

                        if (expDate.length() > 0) {
                            ExpDate.setText(expDate);
                            ExpDate.setEnabled(false);
                            btnExpDateClear.setEnabled(false);
                        }

                        if (sizeId.length() > 0) {
                            Integer i = hmSizeID.get(sizeId);
                            cmbSize.setSelection(i, true);
                            cmbSize.setEnabled(false);
                        }

                        if (cmt.length() > 0) {
                            Cmt.setText(cmt);
                            Cmt.setEnabled(false);
                        }
                        //endregion

                        //region 同步 PcsSN 的表
                        if (itemId.equals(""))
                            return;

                        if (RegType.equals("PcsSN")) {

                            List<String> lstRefKeys = new ArrayList<>();

                            for (int i = 0; i < dtOrgWgrData.Rows.size(); i++) {
                                String refKey = dtOrgWgrData.Rows.get(i).getValue("GRR_DET_SN_REF_KEY").toString().trim();

                                if (refKey.equals(""))
                                    continue;

                                if (!lstRefKeys.contains(refKey))
                                    lstRefKeys.add(refKey);
                            }

                            if (lstRefKeys.size() == 0)
                                return;

                            String[] aryRefKeys = new String[lstRefKeys.size()];
                            lstRefKeys.toArray(aryRefKeys);

                            String grrDetSnRefKey = GrrSnTable.Rows.get(0).get("GRR_DET_SN_REF_KEY").toString();

                            for (int i = GrrSnTable.Rows.size() - 1; i >= 0; i--) {
                                if (GrrSnTable.Rows.get(i).getValue("GRR_DET_SN_REF_KEY").toString().equals(grrDetSnRefKey))
                                    GrrSnTable.Rows.remove(i);
                            }

                            for (int i = GrrSnTableall.Rows.size() - 1; i >= 0; i--) {
                                if (GrrSnTableall.Rows.get(i).getValue("GRR_DET_SN_REF_KEY").toString().equals(grrDetSnRefKey))
                                    GrrSnTableall.Rows.remove(i);
                            }

                            for(DataRow dr : dtOrgSn.Rows) {
                                DataRow drNew = GrrSnTable.newRow();
                                drNew.setValue("GRR_DET_SN_REF_KEY", dr.get("GRR_DET_SN_REF_KEY").toString().trim());
                                drNew.setValue("SN_ID", dr.get("SN_ID").toString().trim());
                                GrrSnTable.Rows.add(drNew);

                                DataRow drNewAll = GrrSnTableall.newRow();
                                drNewAll.setValue("GRR_DET_SN_REF_KEY", dr.get("GRR_DET_SN_REF_KEY").toString().trim());
                                drNewAll.setValue("SN_ID", dr.get("SN_ID").toString().trim());
                                GrrSnTableall.Rows.add(drNew);
                            }

//                            _GrrDetSNRerKey = aryRefKeys;
                            btnSn.setText(getResources().getString(R.string.GR_SN) + " (" + GrrSnTable.Rows.size() + ")");

                        }
                        //endregion
                    }
                }
            }
        });

    }

    // 輸入條碼 & 存貨編碼
    private void getSkuNum(String strGrId, String strSkuLevel, String strSkuNum, String strBarType, String strBarTypeId, final String strBarCodeValue, final String isReceived) {

        // 條碼拆解
        BModuleObject biObj1 = new BModuleObject();
        biObj1.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIGoodReceiptReceivePortal");
        biObj1.setModuleID("BIFetchReceiptBySkuNumAndBarCode");
        biObj1.setRequestID("BIFetchReceiptBySkuNumAndBarCode");
        biObj1.params = new Vector<>();

        // Input param
        ParameterInfo paramGrId = new ParameterInfo();
        paramGrId.setParameterID(BIGoodReceiptReceivePortalParam.GrId);
        paramGrId.setParameterValue(strGrId);
        biObj1.params.add(paramGrId);

        ParameterInfo paramSkuLevel = new ParameterInfo();
        paramSkuLevel.setParameterID(BIGoodReceiptReceivePortalParam.SkuLevel);
        paramSkuLevel.setParameterValue(skuLevelId);
        biObj1.params.add(paramSkuLevel);

        ParameterInfo paramSkuNum = new ParameterInfo();
        paramSkuNum.setParameterID(BIGoodReceiptReceivePortalParam.SkuNum);
        paramSkuNum.setParameterValue(strSkuNum);
        biObj1.params.add(paramSkuNum);

        ParameterInfo paramBarCodeType = new ParameterInfo();
        paramBarCodeType.setParameterID(BIGoodReceiptReceivePortalParam.BarCodeType);
        paramBarCodeType.setParameterValue(strBarType);
        biObj1.params.add(paramBarCodeType);

        ParameterInfo paramBarCodeTypeId = new ParameterInfo();
        paramBarCodeTypeId.setParameterID(BIGoodReceiptReceivePortalParam.BarCodeTypeId);
        paramBarCodeTypeId.setParameterValue(strBarTypeId);
        biObj1.params.add(paramBarCodeTypeId);

        ParameterInfo paramBarCodeValue = new ParameterInfo();
        paramBarCodeValue.setParameterID(BIGoodReceiptReceivePortalParam.BarCodeValue);
        paramBarCodeValue.setParameterValue(strBarCodeValue);
        biObj1.params.add(paramBarCodeValue);

        ParameterInfo paramIsReceived = new ParameterInfo();
        paramIsReceived.setParameterID(BIGoodReceiptReceivePortalParam.IsReceived);
        paramIsReceived.setParameterValue(isReceived);
        biObj1.params.add(paramIsReceived);

        BarCode.getText().clear(); // 先清除，若拆解成功再回填

        CallBIModule(biObj1, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {

            if (CheckBModuleReturnInfo(bModuleReturn)) {

                    String askChangeSkuLevel = "";

                    if (bModuleReturn.getReturnList().get("BIFetchReceiptBySkuNumAndBarCode") != null) {

                        if (bModuleReturn.getReturnList().get("BIFetchReceiptBySkuNumAndBarCode").get(BIGoodReceiptReceivePortalParam.ChangeSkuLevel) != null) {
                            askChangeSkuLevel = bModuleReturn.getReturnList().get("BIFetchReceiptBySkuNumAndBarCode").get(BIGoodReceiptReceivePortalParam.ChangeSkuLevel).toString().replaceAll("\"", "");
                        }

                        if (!askChangeSkuLevel.equals("")) {

                            final String [] skuData = askChangeSkuLevel.split(";");
                            Object[] args = new Object[2];
                            args[0] = skuData[0];
                            args[1] = skuData[1];

                            String showMsg = String.format(getResString(getResources().getString(R.string.CHANGE_SKU_LEVEL)), args);

                            ShowMessage(showMsg, new ShowMessageEvent() {
                                @Override
                                public void onDismiss() {

                                    // region -- 設置 cmbSkuLevel --
                                    int levelPosition = 0;
                                    skuLevelId = skuData[0];

                                    for (Map<String, Object> levelMap : lstSkuLevel) {
                                        if (levelMap.get("DATA_ID").toString().equals(skuLevelId))
                                            break;
                                        levelPosition++;
                                    }

                                    if (levelPosition == lstSkuLevel.size()) {
                                        ShowMessage(R.string.WAPG009027); //WAPG009027   查無物料代碼[%s]!
                                        return;
                                    }

                                    SimpleAdapter adapterSkuLevel = new AnotherSimpleArrayAdapter<>(getApplicationContext(), lstSkuLevel, android.R.layout.simple_spinner_item, new String[]{"DATA_ID", "DATA_NAME"}, new int[]{0, android.R.id.text1});
                                    adapterSkuLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    cmbSkuLevel.setAdapter(adapterSkuLevel);
                                    cmbSkuLevel.setSelection(levelPosition, true);
                                    // endregion

                                    LotId.setText(skuData[1]);

                                    getSkuNum(tvGrId.getText().toString().trim(), skuData[0], skuData[1], isReceived);
                                }
                            });
                        }

                    } else {

                        ClearData(true);
                        setEnable(false, false);

                        _dtBarCodeData = bModuleReturn.getReturnJsonTables().get("BIFetchReceiptBySkuNumAndBarCode").get("BARCODE_DATA");
                        _dtExtendData = bModuleReturn.getReturnJsonTables().get("BIFetchReceiptBySkuNumAndBarCode").get("EXTEND_DATA");
                        dtOrgWgrData = bModuleReturn.getReturnJsonTables().get("BIFetchReceiptBySkuNumAndBarCode").get("dtOrgWgrData");
                        DataTable dtOrgSn = bModuleReturn.getReturnJsonTables().get("BIFetchReceiptBySkuNumAndBarCode").get("dtSn");

                        String itemId = "";

                        //region 主要條碼資料填值
                        for (DataRow row : _dtBarCodeData.Rows)
                        {
                            switch (row.getValue("BARCODE_VARIABLE_ID").toString())
                            {
                                case "ITEM_ID":
                                    if (!itemId.equals(tvItemId.getText().toString().trim())) {
                                        ShowMessage("");
                                        return;
                                    }
                                    break;

                                case "REGISTER_ID":
                                    LotId.setText(row.getValue("BARCODE_VALUE").toString());
                                    LotId.setEnabled(false);
                                    IbtnGrLotGridLotIdQRScan.setEnabled(false);
                                    break;

                                case "QTY":
                                    Qty.setText(row.getValue("BARCODE_VALUE").toString());
                                    Qty.setEnabled(false);
                                    break;

                                case "LOT_CODE":
                                    LotCode.setText(row.getValue("BARCODE_VALUE").toString());
                                    LotCode.setEnabled(false);
                                    break;

                                case "MFG_DATE":
                                    MfgDate.setText(row.getValue("BARCODE_VALUE").toString());
                                    MfgDate.setEnabled(false);
                                    btnMfgDateClear.setEnabled(false);
                                    break;

                                case "EXP_DATE":
                                    ExpDate.setText(row.getValue("BARCODE_VALUE").toString());
                                    ExpDate.setEnabled(false);
                                    btnExpDateClear.setEnabled(false);
                                    break;

                                case "SIZE_ID":
                                    Integer i = hmSizeID.get(row.getValue("BARCODE_VALUE").toString());
                                    cmbSize.setSelection(i, true);
                                    cmbSize.setEnabled(false);
                                    break;

                                case "VENDOR_ITEM_ID":
                                    _VendorItemId = row.getValue("BARCODE_VALUE").toString();
                                    break;

                                case "CMT":
                                    Cmt.setText(row.getValue("BARCODE_VALUE").toString());
                                    Cmt.setEnabled(false);
                                    break;

                                case "PO":
                                    _Po = row.getValue("BARCODE_VALUE").toString();
                                    break;

                                case "PO_ITEM":
                                case "PO_SEQ":
                                    _PoSeq = row.getValue("BARCODE_VALUE").toString();
                                    break;

                                default:
                                    break;
                            }
                        }
                        //endregion

                        //region Extend
                        if (_dtExtendData.Rows.size() > 0)
                        {
                            rl1.setVisibility(View.VISIBLE);
                            LayoutInflater inflater = getLayoutInflater();
                            GoodReceiptReceiveExtendGridAdapter adapter = new GoodReceiptReceiveExtendGridAdapter(_dtExtendData, inflater);
                            lvExtent.setAdapter(adapter);
                        }
                        else
                        {
                            rl1.setVisibility(View.GONE);
                        }
                        //endregion

                        if (radioBarcode.isChecked())
                            _BarCodeType = "BARCODE";
                        else if (radioQRcode.isChecked())
                            _BarCodeType = "QRCODE";

                        BarCode.setText(strBarCodeValue);

                        //region 同步 PcsSN 的表
                        if (itemId.equals(""))
                            return;

                        if (RegType.equals("PcsSN")) {

                            List<String> lstRefKeys = new ArrayList<>();

                            for (int i = 0; i < dtOrgWgrData.Rows.size(); i++) {
                                String refKey = dtOrgWgrData.Rows.get(i).getValue("GRR_DET_SN_REF_KEY").toString().trim();

                                if (refKey.equals(""))
                                    continue;

                                if (!lstRefKeys.contains(refKey))
                                    lstRefKeys.add(refKey);
                            }

                            if (lstRefKeys.size() == 0)
                                return;

                            String[] aryRefKeys = new String[lstRefKeys.size()];
                            lstRefKeys.toArray(aryRefKeys);

                            for(DataRow dr : dtOrgSn.Rows) {
                                DataRow drNew = GrrSnTable.newRow();
                                drNew.setValue("GRR_DET_SN_REF_KEY", dr.get("GRR_DET_SN_REF_KEY").toString().trim());
                                drNew.setValue("SN_ID", dr.get("SN_ID").toString().trim());
                                GrrSnTable.Rows.add(drNew);
                            }

//                            _GrrDetSNRerKey = aryRefKeys;
                            btnSn.setText(getResources().getString(R.string.GR_SN) + " (" + GrrSnTable.Rows.size() + ")");

                        }
                        //endregion

                    }
                }
            }
        });
    }

//    private void CallBarCodeDisassemble(final String content, final boolean isNew, final String strBarType, String strBarTypeId) {
//        // region Set BIModule
//        // List of BIModules
//        List<BModuleObject> bmObjs = new ArrayList<BModuleObject>();
//
//        // 條碼拆解
//        BModuleObject biObj1 = new BModuleObject();
//        biObj1.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIGoodReceiptReceivePortal");
//        biObj1.setModuleID("BIFetchReceiptBarCodeResult");
//        biObj1.setRequestID("BIFetchReceiptBarCodeResult");
//        biObj1.params = new Vector<>();
//
//        // Input param
//        ParameterInfo param1 = new ParameterInfo();
//        param1.setParameterID(BIGoodReceiptReceivePortalParam.BarCodeType);
//        param1.setParameterValue(strBarType);
//        biObj1.params.add(param1);
//
//        ParameterInfo param2 = new ParameterInfo();
//        param2.setParameterID(BIGoodReceiptReceivePortalParam.BarCodeTypeId);
//        param2.setParameterValue(strBarTypeId);
//        biObj1.params.add(param2);
//
//        ParameterInfo param3 = new ParameterInfo();
//        param3.setParameterID(BIGoodReceiptReceivePortalParam.BarCodeValue);
//        param3.setParameterValue(content);
//        biObj1.params.add(param3);
//
//        ParameterInfo param4 = new ParameterInfo();
//        param4.setParameterID(BIGoodReceiptReceivePortalParam.GrId);
//        param4.setParameterValue(tvGrId.getText().toString().trim());
//        biObj1.params.add(param4);
//
//        bmObjs.add(biObj1);
//        // endregion
//
//        CallBIModule(bmObjs, new WebAPIClientEvent() {
//            @Override
//            public void onPostBack(BModuleReturn bModuleReturn) {
//                if (CheckBModuleReturnInfo(bModuleReturn)) {
//                    _dtBarCodeData = bModuleReturn.getReturnJsonTables().get("BIFetchReceiptBarCodeResult").get("BARCODE_DATA");
//                    _dtExtendData = bModuleReturn.getReturnJsonTables().get("BIFetchReceiptBarCodeResult").get("EXTEND_DATA");
//
//                    ClearData();
//
//                    if (isNew)
//                    {
//                        if (RegType.equals("PcsSN"))
//                        {
//                            for (DataRow row : _dtBarCodeData.Rows)
//                            {
//                                if (!row.getValue("BARCODE_VARIABLE_ID").toString().equals("QTY")) continue;
//
//                                Double qty = Double.parseDouble(Qty.getText().toString().trim());
//                                Double BQty = Double.parseDouble(row.getValue("BARCODE_VALUE").toString());
//
//                                if (!qty.equals(BQty))
//                                {
//                                    ShowMessage(R.string.WAPG007025,RegType);
//                                    return;
//                                }
//                            }
//                        }
//                    }
//
//                    //region 主要條碼資料填值
//                    for (DataRow row : _dtBarCodeData.Rows)
//                    {
//                        switch (row.getValue("BARCODE_VARIABLE_ID").toString())
//                        {
//                            case "REGISTER_ID":
//                                LotId.setText(row.getValue("BARCODE_VALUE").toString());
//                                LotId.setEnabled(false);
//                                IbtnGrLotGridLotIdQRScan.setEnabled(false);
//                                break;
//
//                            case "QTY":
//                                Qty.setText(row.getValue("BARCODE_VALUE").toString());
//                                Qty.setEnabled(false);
//                                break;
//
//                            case "LOT_CODE":
//                                LotCode.setText(row.getValue("BARCODE_VALUE").toString());
//                                LotCode.setEnabled(false);
//                                break;
//
//                            case "MFG_DATE":
//                                MfgDate.setText(row.getValue("BARCODE_VALUE").toString());
//                                MfgDate.setEnabled(false);
//                                btnMfgDateClear.setEnabled(false);
//                                break;
//
//                            case "EXP_DATE":
//                                ExpDate.setText(row.getValue("BARCODE_VALUE").toString());
//                                ExpDate.setEnabled(false);
//                                btnExpDateClear.setEnabled(false);
//                                break;
//
//                            case "SIZE_ID":
//                                Integer i = hmSizeID.get(row.getValue("BARCODE_VALUE").toString());
//                                cmbSize.setSelection(i, true);
//                                cmbSize.setEnabled(false);
//                                break;
//
//                            case "VENDOR_ITEM_ID":
//                                _VendorItemId = row.getValue("BARCODE_VALUE").toString();
//                                break;
//
//                            default:
//                                break;
//                        }
//                    }
//                    //endregion
//
//                    //region Extend
//                    if (_dtExtendData.Rows.size() > 0)
//                    {
//                        rl1.setVisibility(View.VISIBLE);
//                        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
//                        GoodReceiptReceiveExtendGridAdapter adapter = new GoodReceiptReceiveExtendGridAdapter(_dtExtendData, inflater);
//                        lvExtent.setAdapter(adapter);
//                    }
//                    else
//                    {
//                        rl1.setVisibility(View.GONE);
//                    }
//                    //endregion
//
//                    BarCode.setText(content);
//                    _BarCode = content;
//                    _BarCodeType = strBarType;
//                } else {
//                    BarCode.getText().clear();
//                }
//            }
//        });
//    }
}
