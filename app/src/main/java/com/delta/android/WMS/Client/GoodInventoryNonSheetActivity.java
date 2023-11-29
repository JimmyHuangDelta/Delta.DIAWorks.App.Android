package com.delta.android.WMS.Client;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.Activity.ScanActivity;
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
import com.delta.android.WMS.Client.GridAdapter.GoodInventoryNonSheetGridAdapter;
import com.delta.android.WMS.Param.BAutoJudgeXfrTaskGenerateParam;
import com.delta.android.WMS.Param.BIFetchRecoBinParam;
import com.delta.android.WMS.Param.BIPDANoSheetCarrierPortalParam;
import com.delta.android.WMS.Param.BIPDANoSheetRegisterPortalParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.BPDADispatchOrderForInvParam;
import com.delta.android.WMS.Param.BPDAGoodInventoryParam;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.delta.android.WMS.Param.ParamObj.InventoryObj;
import com.delta.android.WMS.Param.ParamObj.RegCarrierBlockObj;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GoodInventoryNonSheetActivity extends BaseFlowActivity {

    final private int IS_BIN_QRSCAN_REQUEST_CODE = 11;
    final private int BIN_QRSCAN_REQUEST_CODE = 12;

//    private EditText etLotId;
    private Spinner cmbLotId;
    private ImageButton ibtnIsBinQRScan;
    private EditText etIsBin;
    private ImageButton ibtnBinQRScan;
    private EditText etBin;
    private EditText etItemName;
    private EditText etItem;
    private EditText etQty;

    private Spinner cmbRecommendBin;
    private Spinner cmbRecommendCarrier;
    private Spinner cmbRecommendBlock;
    private Button btnRecommend;

    private Button btnConfirm;
//    private Button btnRefresh;
    private ImageButton ibtnLotSearch;
    //private EditText etFetchBinId;

    private String strTempBin;
    private String strStorageId;

    public DataTable dtRegister;
    public DataTable dtBinID;
    public DataTable dtCarrierBlockID;

    ArrayList<String> lstRecommendBin = null;
    ArrayList<String> lstRecommendCarrier = null;
    ArrayList<String> lstRecommendBlock = null;
    ArrayList<String> lstLotId = null;

    //Function ID = WAPG011
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good_inventory_non_sheet);

        initViews();
        getLotId();
        setListensers();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, getResources().getText(R.string.QRCODE_SCAN_CANCEL).toString(), Toast.LENGTH_SHORT).show();
            } else {
                switch (requestCode) {
                    case IS_BIN_QRSCAN_REQUEST_CODE:
                        etIsBin.setText(result.getContents().trim().toUpperCase());
                        break;
                    case BIN_QRSCAN_REQUEST_CODE:
                        etBin.setText(result.getContents().trim().toUpperCase());
                        break;
                    default:
                        break;
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initViews() {
//        etLotId = findViewById(R.id.etLotId);
        cmbLotId = findViewById(R.id.cmbLotId);
        ibtnIsBinQRScan = findViewById(R.id.ibtnIsBinQRScan);
        etIsBin = findViewById(R.id.etIsBin);
        ibtnBinQRScan = findViewById(R.id.ibtnBinQRScan);
        etBin = findViewById(R.id.etBin);
        etItemName = findViewById(R.id.etItemName);
        etItem = findViewById(R.id.etItem);
        etQty = findViewById(R.id.etQty);
//        btnRefresh = findViewById(R.id.btnRefresh);
        cmbRecommendBin = findViewById(R.id.cmbRecommendBin);
        cmbRecommendCarrier = findViewById(R.id.cmbRecommendCarrier);
        cmbRecommendBlock = findViewById(R.id.cmbRecommendBlock);
        btnRecommend = findViewById(R.id.btnRecommend);
        btnConfirm = findViewById(R.id.btnBinConfirm);
        ibtnLotSearch = findViewById(R.id.ibtnSearch);
        //etFetchBinId = findViewById(R.id.etFetchBinId);
    }

    private void initData() {
        strTempBin = "";
        strStorageId = "";
        dtRegister = new DataTable();

//        etLotId.setText("");
        getLotId();
        etIsBin.setText("");
        etBin.setText("");
        etItem.setText("");
        etItem.setFocusableInTouchMode(false);
        etItem.setFocusable(false);
        etItemName.setText("");
        etItemName.setFocusableInTouchMode(false);
        etItemName.setFocusable(false);
        etQty.setText("");
        etQty.setFocusableInTouchMode(false);
        etQty.setFocusable(false);

        lstRecommendBin = new ArrayList<>();
        lstRecommendCarrier = new ArrayList<>();
        lstRecommendBlock = new ArrayList<>();

        ArrayAdapter<String> adapterBin = new ArrayAdapter<>(GoodInventoryNonSheetActivity.this, android.R.layout.simple_spinner_dropdown_item, lstRecommendBin);
        cmbRecommendBin.setAdapter(adapterBin);
        ArrayAdapter<String> adapterCarrier = new ArrayAdapter<>(GoodInventoryNonSheetActivity.this, android.R.layout.simple_spinner_dropdown_item, lstRecommendCarrier);
        cmbRecommendCarrier.setAdapter(adapterCarrier);
        ArrayAdapter<String> adapterBlock = new ArrayAdapter<>(GoodInventoryNonSheetActivity.this, android.R.layout.simple_spinner_dropdown_item, lstRecommendBlock);
        cmbRecommendBlock.setAdapter(adapterBlock);
    }

    private void setListensers() {
        //etLotId.setOnKeyListener(lsLotIdKey);
//        btnRefresh.setOnClickListener(lsRefresh);
        ibtnIsBinQRScan.setOnClickListener(ibtnQRScan);
        ibtnBinQRScan.setOnClickListener(ibtnQRScan);
        btnRecommend.setOnClickListener(lsRecommend);
        btnConfirm.setOnClickListener(lsConfirm);
        //ibtnSearch.setOnClickListener(ibtnSearchClick);
        ibtnLotSearch.setOnClickListener(ibtnLotSearchClick);

    }

    private void FetchLotInfo() {
        /*
        if (etFetchBinId.getText().toString().equals(""))
        {
            ShowMessage(R.string.WAPG011006); //WAPG011006 請輸入入料口
            return;
        }
        */

        /*
        if (etLotId.getText().toString().equals("")) {
            ShowMessage(R.string.WAPG011001); //WAPG011001 請輸入批號
            return;
        }
        */

        int lotIdIndex = cmbLotId.getSelectedItemPosition();
        if (lotIdIndex == (lstLotId.size() - 1)) {
            ShowMessage(R.string.WAPG011001); //WAPG011001 請選擇批號
            return;
        }

        if (etIsBin.getText().toString().equals("")) {
            ShowMessage(R.string.WAPG011006); //WAPG011006    請輸入入料口
            return;
        }

        //找出Best Bin
        BModuleObject bmObjDispatch = new BModuleObject();
        bmObjDispatch.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIFetchRecoBin");
        bmObjDispatch.setModuleID("BIFetchRecoBinByRegister");
        bmObjDispatch.setRequestID("BIFetchRecoBinByRegister");

        bmObjDispatch.params = new Vector<ParameterInfo>();
        ParameterInfo paramDispatch = new ParameterInfo();
        paramDispatch.setParameterID(BIFetchRecoBinParam.RegisterId);
//        paramDispatch.setParameterValue(etLotId.getText().toString());
        paramDispatch.setParameterValue(cmbLotId.getSelectedItem().toString());
        bmObjDispatch.params.add(paramDispatch);

        ParameterInfo paramFetchBinId = new ParameterInfo();
        paramFetchBinId.setParameterID(BIFetchRecoBinParam.BinId);
        paramFetchBinId.setParameterValue(etIsBin.getText().toString());
        bmObjDispatch.params.add(paramFetchBinId);


        ParameterInfo paramFetchBinType = new ParameterInfo();
        paramFetchBinType.setParameterID(BIFetchRecoBinParam.BinType);
        paramFetchBinType.setParameterValue("GL");
        bmObjDispatch.params.add(paramFetchBinType);

        //ParameterInfo paramFetchBin = new ParameterInfo();
        //paramFetchBin.setParameterID(BPDADispatchOrderForInvParam.FetchBinId);
        //paramFetchBin.setParameterValue(etFetchBinId.getText().toString());

        CallBIModule(bmObjDispatch, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    Gson gson = new Gson();

                    dtRegister = bModuleReturn.getReturnJsonTables().get("BIFetchRecoBinByRegister").get("SWMS_REGISTER");
                    strStorageId = gson.fromJson(bModuleReturn.getReturnList().get("BIFetchRecoBinByRegister").get(BIFetchRecoBinParam.StorageId).toString(), String.class);

                    dtBinID = bModuleReturn.getReturnJsonTables().get("BIFetchRecoBinByRegister").get("BinID");
                    dtCarrierBlockID = bModuleReturn.getReturnJsonTables().get("BIFetchRecoBinByRegister").get("CarrierBlockID");
                    etItemName.setText(dtRegister.Rows.get(0).getValue("ITEM_NAME").toString());
                    etItem.setText(dtRegister.Rows.get(0).getValue("ITEM_ID").toString());
                    etQty.setText(dtRegister.Rows.get(0).getValue("QTY").toString());

                    lstRecommendBin = new ArrayList<String>();
                    for (DataRow dr : dtBinID.Rows) {
                        String strBin = dr.getValue("BIN_ID").toString();
                        lstRecommendBin.add(strBin);
                    }
                    ArrayAdapter<String> adapterBin = new ArrayAdapter<>(GoodInventoryNonSheetActivity.this, android.R.layout.simple_spinner_dropdown_item, lstRecommendBin);
                    cmbRecommendBin.setAdapter(adapterBin);

                    if (dtCarrierBlockID.Rows.size() > 0) {
                        lstRecommendCarrier = new ArrayList<String>();
                        final HashMap<String, ArrayList<String>> mapBlock = new HashMap<>();
                        for (DataRow dr : dtCarrierBlockID.Rows) {
                            String strCarrier = dr.getValue("CARRIER_ID").toString();
                            String strBlock = dr.getValue("BLOCK_ID").toString();
                            if (!lstRecommendCarrier.contains(strCarrier)) {
                                lstRecommendCarrier.add(strCarrier);
                            }
                            if (!mapBlock.containsKey(strCarrier)) {
                                ArrayList<String> lstBlock = new ArrayList<String>();
                                lstBlock.add(strBlock);
                                mapBlock.put(strCarrier, lstBlock);
                            } else {
                                mapBlock.get(strCarrier).add(strBlock);
                            }
                        }
                        ArrayAdapter<String> adapterCarrier = new ArrayAdapter<>(GoodInventoryNonSheetActivity.this, android.R.layout.simple_spinner_dropdown_item, lstRecommendCarrier);
                        cmbRecommendCarrier.setAdapter(adapterCarrier);

                        cmbRecommendCarrier.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                                ArrayAdapter<String> adapterBlock = new ArrayAdapter<>(GoodInventoryNonSheetActivity.this, android.R.layout.simple_spinner_dropdown_item, mapBlock.get(cmbRecommendCarrier.getSelectedItem().toString()));
                                cmbRecommendBlock.setAdapter(adapterBlock);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {
                                ArrayAdapter<String> adapterBlock = new ArrayAdapter<>(GoodInventoryNonSheetActivity.this, android.R.layout.simple_spinner_dropdown_item, mapBlock.get(cmbRecommendCarrier.getSelectedItem().toString()));
                                cmbRecommendBlock.setAdapter(adapterBlock);
                            }
                        });

                        ArrayAdapter<String> adapterBlock = new ArrayAdapter<>(GoodInventoryNonSheetActivity.this, android.R.layout.simple_spinner_dropdown_item, mapBlock.get(cmbRecommendCarrier.getSelectedItem().toString()));
                        cmbRecommendBlock.setAdapter(adapterBlock);
                    }
                }
            }
        });
    }

    private void Confirm() {
        /*
        if (etLotId.getText().toString().equals("")) {
            ShowMessage(R.string.WAPG011001); //WAPG011001 請輸入批號
            return;
        }
        */

        int lotIdIndex = cmbLotId.getSelectedItemPosition();
        if (lotIdIndex == (lstLotId.size() - 1)) {
            ShowMessage(R.string.WAPG011001); //WAPG011001 請選擇批號
            return;
        }

        String tempBin1 = cmbRecommendBin.getSelectedItem().toString();
        String tempBin2 = etBin.getText().toString();
        if (!tempBin1.equals(tempBin2)) {
            ShowMessage(R.string.WAPG011005); //WAPG011005 請確認儲位代碼是否與推薦儲位相同
            return;
        }

        //BI
        BModuleObject bimObj = new BModuleObject();
        bimObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIPDANoSheetRegisterPortal");
        bimObj.setModuleID("BIRegisterXfrConfirm");
        bimObj.setRequestID("BIRegisterXfrConfirm");
        bimObj.params = new Vector<ParameterInfo>();

        // Input param
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIPDANoSheetRegisterPortalParam.RegisterId);
//        param1.setParameterValue(etLotId.getText().toString());
        param1.setParameterValue(cmbLotId.getSelectedItem().toString());
        bimObj.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIPDANoSheetRegisterPortalParam.XfrCase);
        param2.setParameterValue("Lot");
        bimObj.params.add(param2);

        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BIPDANoSheetRegisterPortalParam.XfrTask);
        param3.setParameterValue("RegisterStockIn");
        bimObj.params.add(param3);


        ParameterInfo param6 = new ParameterInfo();
        param6.setParameterID(BIPDANoSheetRegisterPortalParam.ObjectId);
//        param6.setParameterValue(etLotId.getText().toString());
        param6.setParameterValue(cmbLotId.getSelectedItem().toString());
        bimObj.params.add(param6);

        ParameterInfo param7 = new ParameterInfo();
        param7.setParameterID(BIPDANoSheetRegisterPortalParam.StorageId);
        param7.setParameterValue(strStorageId);
        bimObj.params.add(param7);

        ParameterInfo param8 = new ParameterInfo();
        param8.setParameterID(BIPDANoSheetRegisterPortalParam.BestBin);
        param8.setParameterValue(etBin.getText().toString());
        bimObj.params.add(param8);

        ParameterInfo param9 = new ParameterInfo();
        param9.setParameterID(BIPDANoSheetRegisterPortalParam.Stock);
        param9.setParameterValue("I");
        bimObj.params.add(param9);

        ParameterInfo param10 = new ParameterInfo();
        param10.setParameterID(BIPDANoSheetRegisterPortalParam.RegQty);
        param10.setParameterValue(etQty.getText().toString());
        bimObj.params.add(param10);

        ParameterInfo param11 = new ParameterInfo(); // 20220812 Ikea 傳入 ItemId
        param11.setParameterID(BIPDANoSheetRegisterPortalParam.ItemId);
        param11.setParameterValue(etItem.getText().toString());
        bimObj.params.add(param11);

        CallBIModule(bimObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (!CheckBModuleReturnInfo(bModuleReturn)) return;

                ShowMessage(R.string.WAPG011002); //WAPG011002    上架完成
                initData();
            }
        });
    }

    private void getLotId() {

        BModuleObject bmObj = new BModuleObject();

        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchRegisterByLOT");
        bmObj.setRequestID("BIFetchRegisterByLOT");

        bmObj.params = new Vector<ParameterInfo>();

        Condition cdtRegStatus = new Condition();
        cdtRegStatus.setAliasTable("SR");
        cdtRegStatus.setColumnName("REGISTER_STATUS");
        cdtRegStatus.setValue("Available");
        cdtRegStatus.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
        ArrayList<Condition> lstCondition = new ArrayList<>();
        lstCondition.add(cdtRegStatus);
        HashMap<String, List<?>> mapCondition = new HashMap<>();
        mapCondition.put(cdtRegStatus.getColumnName(), lstCondition);

        VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vkey, vVal);
        String strCon = msdl.generateFinalCode(mapCondition);

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Condition);
        param1.setNetParameterValue(strCon);
        bmObj.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIWMSFetchInfoParam.Filter);
        param2.setParameterValue(" AND SB.BIN_TYPE IN ('IO', 'IS', 'IT')");
        bmObj.params.add(param2);

        CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (!CheckBModuleReturnInfo(bModuleReturn))
                    return;
                lstLotId = new ArrayList<>();
                DataTable dt = bModuleReturn.getReturnJsonTables().get("BIFetchRegisterByLOT").get("Register");
                int pos = 0;
                for (DataRow dr : dt.Rows) {
                    lstLotId.add(pos, dr.getValue("REGISTER_ID").toString());
                    pos++;
                }
                Collections.sort(lstLotId); // List依據字母順序排序

                // 下拉選單預設選項依語系調整
                String strSelectLotId = getResString(getResources().getString(R.string.SELECT_SKU_ID));
                lstLotId.add(strSelectLotId);

                SimpleArrayAdapter adapter = new SimpleArrayAdapter<>(GoodInventoryNonSheetActivity.this, android.R.layout.simple_spinner_dropdown_item, lstLotId);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                cmbLotId.setAdapter(adapter);
                cmbLotId.setSelection(lstLotId.size() - 1, true);

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

    private View.OnKeyListener lsLotIdKey = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                FetchLotInfo();
            }
            return false;
        }
    };

    private View.OnClickListener ibtnLotSearchClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            /*
            if (etLotId.getText().equals("")) {
                ShowMessage(R.string.WAPG011001); //WAPG011001 請輸入批號
                return;
            }
             */

            int lotIdIndex = cmbLotId.getSelectedItemPosition();
            if (lotIdIndex == (lstLotId.size() - 1)) {
                ShowMessage(R.string.WAPG011001); //WAPG011001 請選擇批號
                return;
            }

            FetchLotInfo();
        }
    };

    private View.OnClickListener ibtnSearchClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            /*
            if (etLotId.getText().equals("")) {
                ShowMessage(R.string.WAPG011001); //WAPG011001 請輸入批號
                return;
            }
             */

            int lotIdIndex = cmbLotId.getSelectedItemPosition();
            if (lotIdIndex == (lstLotId.size() - 1)) {
                ShowMessage(R.string.WAPG011001); //WAPG011001 請選擇批號
                return;
            }

            //找出Best Bin
            BModuleObject bmObjDispatch = new BModuleObject();
            bmObjDispatch.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BPDADispatchOrderForInv");
            bmObjDispatch.setModuleID("");
            bmObjDispatch.setRequestID("BPDADispatchOrderForInv");

            bmObjDispatch.params = new Vector<ParameterInfo>();
            ParameterInfo paramDispatch = new ParameterInfo();
            paramDispatch.setParameterID(BPDADispatchOrderForInvParam.DispatchLotId);
//            paramDispatch.setParameterValue(etLotId.getText().toString());
            paramDispatch.setParameterValue(cmbLotId.getSelectedItem().toString());
            bmObjDispatch.params.add(paramDispatch);

            CallBModule(bmObjDispatch, new WebAPIClientEvent() {
                @Override
                public void onPostBack(BModuleReturn bModuleReturn) {
                    if (CheckBModuleReturnInfo(bModuleReturn)) {
                        Gson gson = new Gson();
                        String strBestBin = gson.fromJson(bModuleReturn.getReturnList().get("BPDADispatchOrderForInv").get(BPDADispatchOrderForInvParam.BestBin).toString(), String.class);
                        strStorageId = gson.fromJson(bModuleReturn.getReturnList().get("BPDADispatchOrderForInv").get(BPDADispatchOrderForInvParam.StorageId).toString(), String.class);
                    }
                }
            });
        }
    };

    private View.OnClickListener ibtnQRScan = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            IntentIntegrator integrator = new IntentIntegrator(GoodInventoryNonSheetActivity.this);
            // 設置要掃描的條碼類型，ONE_D_CODE_TYPES：一維碼，QR_CODE_TYPES-二維碼
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("BarCode Scan"); //底部的提示文字
            integrator.setCameraId(0); //前置(1)或者後置(0)攝像頭
            integrator.setBeepEnabled(false); //掃描成功的「嗶嗶」聲
            integrator.setBarcodeImageEnabled(false); //是否保留掃碼成功時候的截圖
            integrator.setCaptureActivity(ScanActivity.class);
            switch (view.getId()) {
                case R.id.ibtnIsBinQRScan:
                    integrator.setRequestCode(IS_BIN_QRSCAN_REQUEST_CODE);
                    break;
                case R.id.ibtnBinQRScan:
                    integrator.setRequestCode(BIN_QRSCAN_REQUEST_CODE);
                    break;
                default:
                    integrator.setRequestCode(0);
                    break;
            }
            integrator.initiateScan();
        }
    };

    private AdapterView.OnClickListener lsRecommend = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            cmbRecommendBin.setEnabled(false);
            cmbRecommendCarrier.setEnabled(false);
            cmbRecommendBlock.setEnabled(false);
        }
    };

    private AdapterView.OnClickListener lsConfirm = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Confirm();
        }
    };

    private AdapterView.OnClickListener lsRefresh = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            initData();
        }
    };
}

//ERROR CODE WAPG011
//WAPG011001    請選擇批號
//WAPG011002    上架完成
//WAPG011003    請確認推薦儲位
//WAPG011004    該批號不屬於批號上架
//WAPG011005    請確認儲位代碼是否與推薦儲位相同
//WAPG011006    請輸入入料口