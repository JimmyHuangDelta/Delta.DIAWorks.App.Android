package com.delta.android.WMS.Client;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.Activity.ScanActivity;
import com.delta.android.Core.Activity.ShowMessageEvent;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.GenerateJsonNetString.MesClass;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Param.BGoodReceiptReceiveParam;
import com.delta.android.WMS.Param.ParamObj.CheckCountObj;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class GoodReceiptReceiveLotSnModifyActivity extends BaseFlowActivity {

    // private variable
    private String type;
    private ViewHolder holder = null;
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


    static class ViewHolder {
        // 宣告控制項物件
        EditText LotId;
        EditText Qty;
        EditText Uom;
        EditText Cmt;
        EditText MfgDate;
        EditText ExpDate;
        ListView SnData;
        Button BtnSave;
        Button BtnAddSn;
        Button BtnDelSn;
        ImageButton IbtnGrLotGridLotIdQRScan;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_good_receipt_receive_lot_sn_modify);

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
            } else {
                holder.LotId.setText(result.getContents().trim());
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

        if (holder == null) holder = new ViewHolder();
        // 取得控制項物件
        holder.LotId = findViewById(R.id.tvGrLotGridLotId);
        holder.Qty = findViewById(R.id.tvGrLotGridQty);
        holder.Uom = findViewById(R.id.tvGrLotGridUom);
        holder.Cmt = findViewById(R.id.tvGrLotGridCmt);
        holder.MfgDate = findViewById(R.id.tvGrLotGridMfgDate);
        holder.ExpDate = findViewById(R.id.tvGrLotGridExpDate);
        holder.SnData = findViewById(R.id.lvSnData);
        holder.BtnSave = findViewById(R.id.btnSave);
        holder.BtnAddSn = findViewById(R.id.btnAddSn);
        holder.BtnDelSn = findViewById(R.id.btnDeleteSn);
        holder.IbtnGrLotGridLotIdQRScan = findViewById(R.id.ibtnGrLotGridLotIdQRScan);

        type = getIntent().getStringExtra("Type");
        MstTable = (DataTable) getIntent().getSerializableExtra("MstTable");
        GrLotTable = (DataTable) getIntent().getSerializableExtra("DetLotTable");
        GrLotTableAll = (DataTable) getIntent().getSerializableExtra("DetLotTableAll");
        GrrSnTable = (DataTable) getIntent().getSerializableExtra("DetLotSnTable");
        GrrSnTableall = (DataTable) getIntent().getSerializableExtra("DetLotSnTableAll");
        ShCfg = getIntent().getStringExtra("ActualQtyStatus");
        RegType = getIntent().getStringExtra("RegType");
        DetItemTotalQty = getIntent().getStringExtra("DetItemTotalQty");
        receiveCount = getIntent().getDoubleExtra("DetReceiveItemQty",0);

        // 控制項卡控
        if(RegType.equals("PcsSN"))
        {
            holder.BtnAddSn.setEnabled(true);
        }
        else
        {
            holder.BtnAddSn.setEnabled(true);
        }
        if(RegType.equals("ItemID"))
        {
            holder.LotId.setEnabled(false);
        }
        else
        {
            holder.LotId.setEnabled(true);
        }


        if (type.equals("Modify")) {
            holder.LotId.setText(GrLotTable.Rows.get(0).getValue("LOT_ID").toString());
            holder.Qty.setText(GrLotTable.Rows.get(0).getValue("QTY").toString());
            holder.Uom.setText(GrLotTable.Rows.get(0).getValue("UOM").toString());
            holder.Cmt.setText(GrLotTable.Rows.get(0).getValue("CMT").toString());
            if (!GrLotTable.Rows.get(0).getValue("MFG_DATE").toString().equals(""))
                holder.MfgDate.setText(GrLotTable.Rows.get(0).getValue("MFG_DATE").toString().substring(0, 10));//.replace("-","/"));
            if (!GrLotTable.Rows.get(0).getValue("EXP_DATE").toString().equals(""))
                holder.ExpDate.setText(GrLotTable.Rows.get(0).getValue("EXP_DATE").toString().substring(0, 10));//.replace("-","/"));
            holder.MfgDate.setInputType(InputType.TYPE_NULL);
            holder.ExpDate.setInputType(InputType.TYPE_NULL);

            ArrayList list = new ArrayList();
            for (DataRow dr : GrrSnTable.Rows) {
                list.add(dr.getValue("SN_ID").toString());
            }
            ListAdapter adapter = new ArrayAdapter<>(GoodReceiptReceiveLotSnModifyActivity.this, android.R.layout.simple_list_item_1, list);
            holder.SnData.setAdapter(adapter);
        } else if (type.equals("Add")) {
            if(RegType.equals("ItemID")) holder.LotId.setText("*");
            else holder.LotId.setText("");
            holder.Qty.setText("");
            holder.Uom.setText("");
            holder.Cmt.setText("");
            holder.MfgDate.setText("");
            holder.ExpDate.setText("");
            holder.MfgDate.setInputType(InputType.TYPE_NULL);
            holder.ExpDate.setInputType(InputType.TYPE_NULL);
            Seq = getIntent().getStringExtra("Seq");
            StorageId = getIntent().getStringExtra("StorageId");
            ItemId = getIntent().getStringExtra("ItemId");
            strSkipQC = getIntent().getStringExtra("strSkipQC");
        }
    }

    //設定監聽事件
    private void setListensers() {
        holder.MfgDate.setOnClickListener(MfgDateOnClick);
        holder.ExpDate.setOnClickListener(ExpDateOnClick);
        holder.BtnSave.setOnClickListener(SaveLotSn);
        holder.BtnAddSn.setOnClickListener(AddNewSN);
        holder.BtnDelSn.setOnClickListener(DeleteSn);
        holder.IbtnGrLotGridLotIdQRScan.setOnClickListener(IbtnGrLotGridLotIdQRScanOnClick);
    }

    //region 事件
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

    private View.OnClickListener SaveLotSn = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            double originalQty = 0;
            if (GrLotTable.Rows.size() > 0)
                originalQty = Double.parseDouble(GrLotTable.Rows.get(0).getValue("QTY").toString());

            // 20200731 archie 如果是修改,要記錄舊的修改的數量
            if (type.equals("Modify"))
            {
                if (GrLotTable.Rows.size() > 0)
                {
                    originalQty = Double.parseDouble(GrLotTable.Rows.get(0).getValue("QTY").toString());
                }
            }

            // 檢查物料監控設定
            if (RegType.equals("ItemID"))
            {
                if (holder.LotId.getText().toString().equals("")==false && !(holder.LotId.getText().toString().equals("*")) )
                {
                    ShowMessage("物料[{0}]註冊類別為[{1}],不須輸入批號");//物料[{0}]註冊類別為[{1}],不須輸入批號
                    return;
                }
            }else {
                if (holder.LotId.getText().toString().equals("") || holder.LotId.getText().toString().equals("*") )
                {
                    ShowMessage(R.string.WAPG007005);//物料[{0}]註冊類別為[{1}],必須輸入批號
                    return;
                }else{
                    //指定批號，輸入的批號需要跟單據上的批號一致
                    //String strLotId = GrLotTable.Rows.get(0).getValue("LOT_ID").toString();
                    String strLotId = getIntent().getStringExtra("LotId");
                    if(!strLotId.equals("*") && !holder.LotId.getText().toString().equals(strLotId)){
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
            if (holder.Qty.getText().toString().equals("")) {
                ShowMessage(R.string.WAPG007006);
                return;
            }

            if (holder.MfgDate.getText().toString().equals(""))
            {
                ShowMessage(R.string.WAPG007023);
                return;
            }

            if (holder.ExpDate.getText().toString().equals(""))
            {
                ShowMessage(R.string.WAPG007024);
                return;
            }
            // 檢查單據設定
            switch (ShCfg)
            {
                case "Less":
                case "Equal":
                    if ( Double.parseDouble(holder.Qty.getText().toString()) + receiveCount - originalQty > Double.parseDouble(DetItemTotalQty) )
                    {
                        ShowMessage(R.string.WAPG007013,
                                MstTable.Rows.get(0).getValue("GR_ID").toString(),
                                GrLotTable.Rows.get(0).getValue("SEQ").toString(),
                                (Double.parseDouble(holder.Qty.getText().toString()) + receiveCount - originalQty),
                                DetItemTotalQty);
                        return;
                    }
                    break;

                case "More":
                    break;
            }
            // 批號是否重複
            if (GrLotTableAll != null && GrLotTableAll.Rows.size() > 0) {
                int iMaxLot = 1;
                int count = 0;
                if (type.equals("Add")) iMaxLot = 0;
                for (DataRow dr : GrLotTableAll.Rows) {
                    if (dr.getValue("LOT_ID").toString().equals(holder.LotId.getText().toString().toUpperCase().trim()) //20200729 archie 轉大寫
                            && dr.getValue("SEQ").toString().equals(Seq))
                        count++;
                }
                if (count > iMaxLot) {
                    ShowMessage(R.string.WAPG007007);
                    return;
                }
            }

            if (type.equals("Modify")) {
                for (DataRow drAll : GrLotTableAll.Rows) {
                    if (drAll.getValue("LOT_ID").toString().equals(GrLotTable.Rows.get(0).getValue("LOT_ID").toString())
                        && drAll.getValue("SEQ").toString().equals(GrLotTable.Rows.get(0).getValue("SEQ").toString())
                            && drAll.getValue("GRR_DET_REF_KEY").toString().equals(GrLotTable.Rows.get(0).getValue("GRR_DET_REF_KEY").toString())  )
                    {
                        drAll.setValue("LOT_ID", holder.LotId.getText().toString().toUpperCase().trim()); //20200729 archie 轉大寫
                        drAll.setValue("QTY", holder.Qty.getText().toString());
                        drAll.setValue("UOM", holder.Uom.getText().toString());
                        drAll.setValue("CMT", holder.Cmt.getText().toString());
                        if (!holder.MfgDate.getText().toString().equals(""))
                            drAll.setValue("MFG_DATE", holder.MfgDate.getText().toString() + " 00:00:00");
                        else
                            drAll.setValue("MFG_DATE", "");
                        if (!holder.ExpDate.getText().toString().equals(""))
                            drAll.setValue("EXP_DATE", holder.ExpDate.getText().toString() + " 23:59:59");
                        else
                            drAll.setValue("EXP_DATE", "");
                        break;
                    }
                    /*else
                    {
                        if (!drAll.getValue("MFG_DATE").toString().equals(""))
                        {
                            drAll.setValue("MFG_DATE", drAll.getValue("MFG_DATE").toString().substring(0,10).replace("-","/") + " 00:00:00");
                        }

                        if (!drAll.getValue("EXP_DATE").toString().equals(""))
                        {
                            drAll.setValue("EXP_DATE", drAll.getValue("EXP_DATE").toString().substring(0,10).replace("-","/") + " 23:59:59");
                        }
                    }*/
                }
            } else if (type.equals("Add")) {
                DataRow drNewLot = GrLotTableAll.newRow();
                drNewLot.setValue("STORAGE_ID", StorageId);
                drNewLot.setValue("ITEM_ID", ItemId);
                drNewLot.setValue("SEQ", Seq);

                drNewLot.setValue("LOT_ID", holder.LotId.getText().toString().toUpperCase().trim()); //20200729 archie 轉大寫
                drNewLot.setValue("QTY", holder.Qty.getText().toString());
                drNewLot.setValue("UOM", holder.Uom.getText().toString());
                drNewLot.setValue("CMT", holder.Cmt.getText().toString());
                if (!holder.MfgDate.getText().toString().equals(""))
                    drNewLot.setValue("MFG_DATE", holder.MfgDate.getText().toString() + " 00:00:00");
                else
                    drNewLot.setValue("MFG_DATE", "");
                if (!holder.ExpDate.getText().toString().equals(""))
                    drNewLot.setValue("EXP_DATE", holder.ExpDate.getText().toString() + " 23:59:59");
                else
                    drNewLot.setValue("EXP_DATE", "");
                if (GrrSnTable.Rows.size() > 0)
                    drNewLot.setValue("GRR_DET_SN_REF_KEY", GrrSnTable.Rows.get(0).getValue("GRR_DET_SN_REF_KEY").toString());

                if(!strSkipQC.equals(""))
                {
                    drNewLot.setValue("SKIP_QC", strSkipQC);
                }
                else
                {
                    for (DataRow dr: GrLotTableAll.Rows)
                    {
                        if (dr.getValue("SEQ").toString().equals(Seq))
                        {
                            drNewLot.setValue("SKIP_QC", dr.getValue("SKIP_QC").toString());
                            break;
                        }
                    }
                }

                GrLotTableAll.Rows.add(drNewLot);
            }

            CallBModule();
        }
    };

    private View.OnClickListener AddNewSN = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(RegType.equals("PcsSN")) //20201020 Hans RegType為 PcsSN時進入
            {
                ShowDialog();
            }
            else
            {

            }
        }
    };

    private View.OnClickListener DeleteSn = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            if(RegType.equals("PcsSN") && GrrSnTableall.Rows.size() > 0){
                ShowDeleteSnDialog();
            }
        }
    };

    private View.OnClickListener IbtnGrLotGridLotIdQRScanOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            IntentIntegrator integrator = new IntentIntegrator(GoodReceiptReceiveLotSnModifyActivity.this);
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
    //endregion

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
                holder.MfgDate.setText(sb);
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
                holder.ExpDate.setText(sb);
                dialog.cancel();
            }
        });

    }
    public void onClickMfgDateClear(View v) {
        holder.MfgDate.setText("");
    }
    public void onClickExpDateClear(View v) {
        holder.ExpDate.setText("");
    }

    private void CallBModule() {
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BGoodReceiptReceive");
        bmObj.setModuleID("");
        bmObj.setRequestID("GRR");
        bmObj.params = new Vector<ParameterInfo>();

        BGoodReceiptReceiveParam sheet = new BGoodReceiptReceiveParam();
        BGoodReceiptReceiveParam.GrrMasterObj sheet1 = sheet.new GrrMasterObj();

        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.INV.Parameter.GrrMasterObj", "bmWMS.INV.Param");
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
                            gotoPreviousActivity(GoodReceiptReceiveLotSnActivity.class);
                        }
                    });
                }
            }
        });
    }

    private void ShowDialog() {
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        final View view = inflater.inflate(R.layout.activity_wms_good_receipt_receive_lot_sn_modify_dialog, null);
        final EditText edSnId = view.findViewById(R.id.edSnId);


        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(GoodReceiptReceiveLotSnModifyActivity.this);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);//只允許按下dialog裡的按鍵才能關閉dialog
        dialog.show();

        Button btnConfirmDialog = view.findViewById(R.id.btnConfirm);
        btnConfirmDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edSnId.getText().toString().equals("")) {
                    ShowMessage(R.string.WAPG007008);
                    return;
                }
                if (GrrSnTableall != null && GrrSnTableall.Rows.size() > 0) {
                    for (DataRow dr : GrrSnTableall.Rows) {
                        if (dr.getValue("SN_ID").toString().equals(edSnId.getText().toString().toUpperCase().trim())) { //20200729 archie 轉大寫
                            ShowMessage(R.string.WAPG007009);
                            return;
                        }
                    }
                }

                if (type.equals("Modify")) {
                    // 加 GRR_DET_SN_REF_KEY
                    if (GrLotTable.Rows.get(0).getValue("GRR_DET_SN_REF_KEY") == null
                            || GrLotTable.Rows.get(0).getValue("GRR_DET_SN_REF_KEY").toString().equals("")) {
                        Date date = new Date();
                        String pattern = "MMddHHmmss.S";
                        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                        GrLotTable.Rows.get(0).setValue("GRR_DET_SN_REF_KEY", sdf.format(date));  // 更新被選的lot
                        for (DataRow dr : GrLotTableAll.Rows) {
                            if (dr.getValue("SEQ").toString().equals(GrLotTable.Rows.get(0).getValue("SEQ").toString())
                                    && dr.getValue("LOT_ID").toString().equals(GrLotTable.Rows.get(0).getValue("LOT_ID").toString())) {
                                dr.setValue("GRR_DET_SN_REF_KEY", sdf.format(date));// 更新All lot
                            }
                        }
                    }
                    DataRow drNewLotSn = GrrSnTableall.newRow();
                    drNewLotSn.setValue("GRR_DET_SN_REF_KEY", GrLotTable.Rows.get(0).getValue("GRR_DET_SN_REF_KEY").toString());
                    drNewLotSn.setValue("SN_ID", edSnId.getText().toString().toUpperCase().trim()); //20200729 archie 轉大寫
                    GrrSnTableall.Rows.add(drNewLotSn); // 更新SnAll
                    // update listview
                    GrrSnTable.Rows.add((DataRow) drNewLotSn.clone());
                    ArrayList list = new ArrayList();
                    for (DataRow dr : GrrSnTable.Rows) {
                        list.add(dr.getValue("SN_ID").toString());
                    }
                    ListAdapter adapter = new ArrayAdapter<>(GoodReceiptReceiveLotSnModifyActivity.this, android.R.layout.simple_list_item_1, list);
                    holder.SnData.setAdapter(adapter);
                } else if (type.equals("Add")) {
                    Date date = new Date();
                    String strGrrDetSnRefKey = null;
                    String pattern = "MMddHHmmss.S";
                    SimpleDateFormat sdf = new SimpleDateFormat(pattern);

                    // region Add 會一直新增 Ref Key
                    //20201020 Hans 修正 SN RefKey寫法, 原寫法每點一次就會產生新的 RefKey, 與 Lot的 RefKey不同, 導致沒有新增至object中
                    if (GrrSnTable.Rows.size() <= 0){
                        strGrrDetSnRefKey = sdf.format(date);
                    }else{
                        strGrrDetSnRefKey = GrrSnTable.Rows.get(0).getValue("GRR_DET_SN_REF_KEY").toString();
                    }
                    // endregion

                    DataRow drNewLotSn = GrrSnTableall.newRow();
                    drNewLotSn.setValue("GRR_DET_SN_REF_KEY", strGrrDetSnRefKey);
                    drNewLotSn.setValue("SN_ID", edSnId.getText().toString().toUpperCase().trim()); //20200729 archie 轉大寫
                    GrrSnTableall.Rows.add(drNewLotSn); // 更新SnAll
                    // update listview
                    GrrSnTable.Rows.add((DataRow) drNewLotSn.clone());
                    ArrayList list = new ArrayList();
                    for (DataRow dr : GrrSnTable.Rows) {
                        list.add(dr.getValue("SN_ID").toString());
                    }
                    ListAdapter adapter = new ArrayAdapter<>(GoodReceiptReceiveLotSnModifyActivity.this, android.R.layout.simple_list_item_1, list);
                    holder.SnData.setAdapter(adapter);
                }
                dialog.dismiss();
            }
        });

        Button btnCloseDialog = view.findViewById(R.id.btnCancel);
        btnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void ShowDeleteSnDialog(){
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        final View view = inflater.inflate(R.layout.activity_wms_good_receipt_receive_lot_sn_modify_dialog, null);
        final EditText edSnId = view.findViewById(R.id.edSnId);

        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(GoodReceiptReceiveLotSnModifyActivity.this);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);//只允許按下dialog裡的按鍵才能關閉dialog
        dialog.show();

        Button btnConfirmDialog = view.findViewById(R.id.btnConfirm);
        Button btnCloseDialog = view.findViewById(R.id.btnCancel);

        btnConfirmDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edSnId.getText().toString().equals("")) {
                    ShowMessage(R.string.WAPG007008);
                    return;
                }
                if (GrrSnTableall != null && GrrSnTableall.Rows.size() > 0) {
                    boolean bCheck = false;
                    for (DataRow dr : GrrSnTableall.Rows) {
                        if (dr.getValue("SN_ID").toString().equals(edSnId.getText().toString().toUpperCase().trim())) { //20200729 archie 轉大寫
                            bCheck = true;
                            break;
                        }
                    }
                    if(!bCheck){
                        ShowMessage(R.string.WAPG007022); //WAPG007022 查無欲刪除的序號
                        return;
                    }
                }

                if (type.equals("Modify")) {
                    // 加 GRR_DET_SN_REF_KEY
                    if (GrLotTable.Rows.get(0).getValue("GRR_DET_SN_REF_KEY") == null
                            || GrLotTable.Rows.get(0).getValue("GRR_DET_SN_REF_KEY").toString().equals("")) {
                        return;
                    }

                    int index = -1;
                    for(int i = 0; i < GrrSnTableall.Rows.size(); i++ ){
                        if(GrrSnTableall.Rows.get(i).getValue("GRR_DET_SN_REF_KEY").toString().equals(GrLotTable.Rows.get(0).getValue("GRR_DET_SN_REF_KEY").toString()) &&
                                GrrSnTableall.Rows.get(i).getValue("SN_ID").toString().equals(edSnId.getText().toString().toUpperCase().trim())){
                            index = i;
                        }
                    }
                    if(index != -1) GrrSnTableall.Rows.remove(index);
                    index = -1;
                    for(int i = 0; i < GrrSnTable.Rows.size(); i++){
                        if(GrrSnTable.Rows.get(i).getValue("GRR_DET_SN_REF_KEY").toString().equals(GrLotTable.Rows.get(0).getValue("GRR_DET_SN_REF_KEY").toString()) &&
                                GrrSnTable.Rows.get(i).getValue("SN_ID").toString().equals(edSnId.getText().toString().toUpperCase().trim())){
                            index = i;
                        }
                    }
                    if(index != -1) GrrSnTable.Rows.remove(index);

                    // update listview
                    ArrayList list = new ArrayList();
                    for (DataRow dr : GrrSnTable.Rows) {
                        list.add(dr.getValue("SN_ID").toString());
                    }
                    ListAdapter adapter = new ArrayAdapter<>(GoodReceiptReceiveLotSnModifyActivity.this, android.R.layout.simple_list_item_1, list);
                    holder.SnData.setAdapter(adapter);
                } else if (type.equals("Add")) {
                    String strGrrDetSnRefKey = GrrSnTable.Rows.get(0).getValue("GRR_DET_SN_REF_KEY").toString();

                    int index = -1;
                    for(int i = 0; i < GrrSnTableall.Rows.size(); i++ ){
                        if(GrrSnTableall.Rows.get(i).getValue("GRR_DET_SN_REF_KEY").toString().equals(strGrrDetSnRefKey) &&
                            GrrSnTableall.Rows.get(i).getValue("SN_ID").toString().equals(edSnId.getText().toString().toUpperCase().trim())){
                            index = i;
                        }
                    }
                    if(index != -1) GrrSnTableall.Rows.remove(index);
                    index = -1;
                    for(int i = 0; i < GrrSnTable.Rows.size(); i++){
                        if(GrrSnTable.Rows.get(i).getValue("GRR_DET_SN_REF_KEY").toString().equals(strGrrDetSnRefKey) &&
                            GrrSnTable.Rows.get(i).getValue("SN_ID").toString().equals(edSnId.getText().toString().toUpperCase().trim())){
                            index = i;
                        }
                    }
                    if(index != -1) GrrSnTable.Rows.remove(index);
                    //更新Sn ListView
                    ArrayList list = new ArrayList();
                    for (DataRow dr : GrrSnTable.Rows) {
                        list.add(dr.getValue("SN_ID").toString());
                    }
                    ListAdapter adapter = new ArrayAdapter<>(GoodReceiptReceiveLotSnModifyActivity.this, android.R.layout.simple_list_item_1, list);
                    holder.SnData.setAdapter(adapter);
                }
                dialog.dismiss();
            }
        });

        btnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
}
