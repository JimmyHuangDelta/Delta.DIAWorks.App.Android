package com.delta.android.WMS.Client;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.delta.android.WMS.Param.BIFetchRecoBinParam;
import com.delta.android.WMS.Param.BIGoodInventoryPortalParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.BPDADispatchOrderForInvParam;
import com.delta.android.WMS.Param.ParamObj.CheckCountObj;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.delta.android.WMS.Param.BGoodInventoryParam;
import com.delta.android.WMS.Param.ParamObj.InventoryObj;
import com.delta.android.WMS.Param.ParamObj.RegCarrierBlockObj;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

public class GoodInventoryNewActivity extends BaseFlowActivity {

    //Function ID = 'WAPG004'

    // 宣告控制項物件/Declare control object
    private HashMap<String, String> mapBinKey = new HashMap<String, String>();
    private Spinner cmbStorageId;
    private Spinner cmbSkuLevel;
    private ImageButton btnSearch;
    private EditText BarCode;
    private ImageButton IbtnBarCodeQRScan;
    private RadioButton radioBarcode;
    private RadioButton radioQRcode;
    private ImageButton IbtnItemIdQRScan;
    private EditText etItemId;
    private ImageButton ibtnSkuNumQRScan;
    private EditText etSkuNum;
    //private ImageButton IbtnLotIdQRScan;
    //private EditText etLotId;
    private EditText etSeachBinId;
    private ImageButton IbtnBinIdQRScan;
    private ListView lstDetail;
    private Button btnRefresh;
    //推薦視窗/Recommended window
    private EditText etPutAwayBin; //上架儲位/PutAwayBin
    private EditText etToBinId; //推薦儲位/Recommendation
    private EditText etBin; // 入料口/IS bin
    private EditText txtQty;
    //private Spinner cmbToBin;
    private Spinner cmbCarrier;
    private Spinner cmbBlock;
    private Spinner cmbIsBin;
    private CheckBox chkUnRec;

    private ArrayList<String> lstBin = null;
    private ArrayList<String> lstCarrier = null;
    private ArrayAdapter<String> adapterBin = null;
    private ArrayAdapter<String> adapterCarrier = null;
    private ArrayAdapter<String> adapterBlock = null;
    private DataTable _dtBin = new DataTable();
    private DataTable dtBestBin = new DataTable();
    private DataTable dtBestCarrierBlock = new DataTable();
    private DataTable _dtGroupReg = new DataTable();
    private DataTable _dtReg = new DataTable();
    //private HashMap<String, String> map = new HashMap<String, String>();//ListView點到的那筆
    private String strStorageId = "";
    ArrayList<String> stringArrayStorageList = new ArrayList<>();
    ArrayList<String> stringArraySkuLevelList = new ArrayList<>();
    private HashMap<String, String> mapStorageKey = new HashMap<String, String>();
    private HashMap<String, String> mapSkuLevelKey = new HashMap<String, String>();
    private String _scanType; //記錄此次點選的掃描類型/Record the scan type selected this time

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_good_inventory_new);

        // 取得控制項物件/Get control object
        initViews();

        // 設定監聽事件/Set up monitoring events
        setListensers();

        // 取得倉庫/Get Storage
        //GetStorage();

        // 取得下拉選單資料(倉庫, 存貨層級)/Get drop-down menu data (warehouse, inventory level)
        getSpinnerData();
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
                String strItem = "";
                String strBin = "";
                String strSkuLevel = "";
                String strSkuNum = "";

                if (_scanType.equals("B"))
                {
                    if (radioBarcode.isChecked())
                    {
                        CallBarCodeDisassemble(content, "BARCODE_TYPE", "ReceiveBarCode");
                    }
                    else if (radioQRcode.isChecked())
                    {
                        CallBarCodeDisassemble(content, "QRCODE_TYPE", "ReceiveQRCode");
                    }

                    BarCode.setText(content);
                }
                else if (_scanType.equals("S"))
                {
                    etSkuNum.setText(content);

                    if (!etItemId.getText().toString().equals(""))
                        strItem = etItemId.getText().toString().trim();

                    if (!etSeachBinId.getText().toString().equals(""))
                        strBin = etSeachBinId.getText().toString().trim();

                    strSkuLevel = mapSkuLevelKey.get(cmbSkuLevel.getSelectedItem().toString());
                    strSkuLevel = strSkuLevel.equals("null") ? "" : strSkuLevel;

                    GetDetail(strStorageId, strItem, strSkuLevel, content, strBin);
                }
                else if (_scanType.equals("I"))
                {
                    etItemId.setText(content);

                    if (!etSkuNum.getText().toString().equals(""))
                        strSkuNum = etSkuNum.getText().toString().trim();

                    if (!etSeachBinId.getText().toString().equals(""))
                        strBin = etSeachBinId.getText().toString().trim();

                    strSkuLevel = mapSkuLevelKey.get(cmbSkuLevel.getSelectedItem().toString());
                    strSkuLevel = strSkuLevel.equals("null") ? "" : strSkuLevel;

                    GetDetail(strStorageId, content, strSkuLevel, strSkuNum, strBin);
                }
                else if (_scanType.equals("BIN"))
                {
                    etSeachBinId.setText(content);

                    if (!etSkuNum.getText().toString().equals(""))
                        strSkuNum = etSkuNum.getText().toString().trim();

                    if (!etItemId.getText().toString().equals(""))
                        strItem = etItemId.getText().toString().trim();

                    strSkuLevel = mapSkuLevelKey.get(cmbSkuLevel.getSelectedItem().toString());
                    strSkuLevel = strSkuLevel.equals("null") ? "" : strSkuLevel;

                    GetDetail(strStorageId, strItem, strSkuLevel, strSkuNum, content);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //取得控制項物件/Get control object
    private void initViews() {
        cmbStorageId = findViewById(R.id.cmbStorageId);
        cmbSkuLevel = findViewById(R.id.cmbSkuLevel);
        btnSearch = findViewById(R.id.btnSearch);
        BarCode = findViewById(R.id.tvGrBarCode);
        IbtnBarCodeQRScan = findViewById(R.id.ibtnBarCodeQRScan);;
        radioBarcode = findViewById(R.id.radioBarcode);
        radioQRcode = findViewById(R.id.radioQRcode);
        IbtnItemIdQRScan = findViewById(R.id.ibtnItemIdQRScan);
        etItemId = findViewById(R.id.etItemId);
        ibtnSkuNumQRScan = findViewById(R.id.ibtnSkuNumQRScan);
        etSkuNum = findViewById(R.id.etSkuNum);
        //IbtnLotIdQRScan = findViewById(R.id.ibtnLotIdQRScan);
        //etLotId = findViewById(R.id.etLotId);
        IbtnBinIdQRScan = findViewById(R.id.ibtnBinIdQRScan);
        etSeachBinId = findViewById(R.id.etBinId);
        lstDetail = findViewById(R.id.listViewDet);
        btnRefresh = findViewById(R.id.btnRefresh);
    }

    //設定監聽事件/Set up monitoring events
    private void setListensers() {
        btnSearch.setOnClickListener(GetDetail);
        //lstDetail.setOnItemClickListener(OnBin);
        IbtnBarCodeQRScan.setOnClickListener(IbtnBarCodeQRScanOnClick);
        BarCode.setOnKeyListener(BarCodeOnKey);
        IbtnItemIdQRScan.setOnClickListener(IbtnItemIdQRScanOnClick);
        //IbtnLotIdQRScan.setOnClickListener(IbtnLotIdQRScanOnClick);
        ibtnSkuNumQRScan.setOnClickListener(ibtnSkuNumQRScanOnClick);
        IbtnBinIdQRScan.setOnClickListener(IbtnBinIdQRScanOnClick);
        etItemId.setOnKeyListener(ItemOnKey);
        //etLotId.setOnKeyListener(LotOnKey);
        etSkuNum.setOnKeyListener(SkuNumOnKey);
        etSeachBinId.setOnKeyListener(SeachBinOnKey);
        btnRefresh.setOnClickListener(ClearData);
    }

    //region 監聽事件/Listening events
    private View.OnClickListener GetDetail = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CheckInput();
        }
    };

    private View.OnClickListener IbtnBarCodeQRScanOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (strStorageId.equals("")) return;

            if (!radioQRcode.isChecked() && !radioBarcode.isChecked())
            {
                //WAPG009022 請選擇一維條碼或二維條碼
                ShowMessage(R.string.WAPG009022);
                return;
            }

            IntentIntegrator integrator = new IntentIntegrator(GoodInventoryNewActivity.this);
            // 設置要掃描的條碼類型，ONE_D_CODE_TYPES：一維碼，QR_CODE_TYPES-二維碼
            // Set the barcode type to be scanned, ONE D_CODE TYPES: one-dimensional code, QR CODE TYPES-two-dimensional code
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("BarCode Scan"); //底部的提示文字/Hint text at the bottom
            integrator.setCameraId(0); //前置(1)或者後置(0)攝像頭/Front (1) or rear (0) camera
            integrator.setBeepEnabled(false); //掃描成功的「嗶嗶」聲/"Beep" sound for successful scan
            integrator.setBarcodeImageEnabled(false); //是否保留掃碼成功時候的截圖/Whether to keep the screenshot when the scan code is successful
            integrator.setCaptureActivity(ScanActivity.class);
            integrator.initiateScan();

            _scanType  = "B";
        }
    };

    private View.OnClickListener IbtnItemIdQRScanOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (strStorageId.equals("")) return;

            IntentIntegrator integrator = new IntentIntegrator(GoodInventoryNewActivity.this);
            // 設置要掃描的條碼類型，ONE_D_CODE_TYPES：一維碼，QR_CODE_TYPES-二維碼
            // Set the barcode type to be scanned, ONE D_CODE TYPES: one-dimensional code, QR CODE TYPES-two-dimensional code
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("BarCode Scan"); //底部的提示文字/Hint text at the bottom
            integrator.setCameraId(0); //前置(1)或者後置(0)攝像頭/Front (1) or rear (0) camera
            integrator.setBeepEnabled(false); //掃描成功的「嗶嗶」聲/"Beep" sound for successful scan
            integrator.setBarcodeImageEnabled(false); //是否保留掃碼成功時候的截圖/Whether to keep the screenshot when the scan code is successful
            integrator.setCaptureActivity(ScanActivity.class);
            integrator.initiateScan();

            _scanType  = "I";
        }
    };

    private View.OnClickListener ibtnSkuNumQRScanOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (strStorageId.equals("")) return;

            IntentIntegrator integrator = new IntentIntegrator(GoodInventoryNewActivity.this);
            // 設置要掃描的條碼類型，ONE_D_CODE_TYPES：一維碼，QR_CODE_TYPES-二維碼
            // Set the barcode type to be scanned, ONE D_CODE TYPES: one-dimensional code, QR CODE TYPES-two-dimensional code
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("BarCode Scan"); //底部的提示文字/Hint text at the bottom
            integrator.setCameraId(0); //前置(1)或者後置(0)攝像頭/Front (1) or rear (0) camera
            integrator.setBeepEnabled(false); //掃描成功的「嗶嗶」聲/"Beep" sound for successful scan
            integrator.setBarcodeImageEnabled(false); //是否保留掃碼成功時候的截圖/Whether to keep the screenshot when the scan code is successful
            integrator.setCaptureActivity(ScanActivity.class);
            integrator.initiateScan();

            _scanType  = "S";
        }
    };

    private View.OnClickListener IbtnLotIdQRScanOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (strStorageId.equals("")) return;

            IntentIntegrator integrator = new IntentIntegrator(GoodInventoryNewActivity.this);
            // 設置要掃描的條碼類型，ONE_D_CODE_TYPES：一維碼，QR_CODE_TYPES-二維碼
            // Set the barcode type to be scanned, ONE D_CODE TYPES: one-dimensional code, QR CODE TYPES-two-dimensional code
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("BarCode Scan"); //底部的提示文字/Hint text at the bottom
            integrator.setCameraId(0); //前置(1)或者後置(0)攝像頭/Front (1) or rear (0) camera
            integrator.setBeepEnabled(false); //掃描成功的「嗶嗶」聲/"Beep" sound for successful scan
            integrator.setBarcodeImageEnabled(false); //是否保留掃碼成功時候的截圖/Whether to keep the screenshot when the scan code is successful
            integrator.setCaptureActivity(ScanActivity.class);
            integrator.initiateScan();

            _scanType  = "R";
        }
    };

    private View.OnClickListener IbtnBinIdQRScanOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (strStorageId.equals("")) return;

            IntentIntegrator integrator = new IntentIntegrator(GoodInventoryNewActivity.this);
            // 設置要掃描的條碼類型，ONE_D_CODE_TYPES：一維碼，QR_CODE_TYPES-二維碼
            // Set the barcode type to be scanned, ONE D_CODE TYPES: one-dimensional code, QR CODE TYPES-two-dimensional code
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("BarCode Scan"); //底部的提示文字/Hint text at the bottom
            integrator.setCameraId(0); //前置(1)或者後置(0)攝像頭/Front (1) or rear (0) camera
            integrator.setBeepEnabled(false); //掃描成功的「嗶嗶」聲/"Beep" sound for successful scan
            integrator.setBarcodeImageEnabled(false); //是否保留掃碼成功時候的截圖/Whether to keep the screenshot when the scan code is successful
            integrator.setCaptureActivity(ScanActivity.class);
            integrator.initiateScan();

            _scanType  = "BIN";
        }
    };

    private View.OnKeyListener BarCodeOnKey = new View.OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            if (keyCode != KeyEvent.KEYCODE_ENTER) return false;

            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                if (strStorageId.equals("")) return false;

                String strBarcode = BarCode.getText().toString().trim();

                if (strBarcode.equals(""))
                {
                    //WAPG004016    請輸入[%s]
                    ShowMessage(R.string.WAPG004016, getResources().getString(R.string.RECEIVE_BARCODE));
                    return false;
                }

                if (radioBarcode.isChecked())
                {
                    CallBarCodeDisassemble(strBarcode, "BARCODE_TYPE", "ReceiveBarCode");
                }
                else if (radioQRcode.isChecked())
                {
                    CallBarCodeDisassemble(strBarcode, "QRCODE_TYPE", "ReceiveQRCode");
                }
                return true;
            }
            return false;
        }
    };

    private View.OnKeyListener ItemOnKey = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            //只有按下Enter才會反映/It will only be reflected if Enter is pressed
            if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                if (strStorageId.equals("")) return false;

                String strItemId = etItemId.getText().toString().trim();

                if (strItemId.equals(""))
                {
                    //WAPG004016    請輸入[%s]
                    ShowMessage(R.string.WAPG004016, getResources().getString(R.string.ITEM_ID));
                    return false;
                }

                String strSkuLevel = "";
                String strSkuNum = "";
                String strBin = "";

                if (!etSkuNum.getText().toString().equals(""))
                    strSkuNum = etSkuNum.getText().toString().trim();

                if (!etSeachBinId.getText().toString().equals(""))
                    strBin = etSeachBinId.getText().toString().trim();

                strSkuLevel = mapSkuLevelKey.get(cmbSkuLevel.getSelectedItem().toString());
                strSkuLevel = strSkuLevel.equals("null") ? "" : strSkuLevel;

                GetDetail(strStorageId, strItemId, strSkuLevel, strSkuNum, strBin);
                return true;
            }
            return false;
        }
    };

    private View.OnKeyListener SkuNumOnKey = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            //只有按下Enter才會反映/It will only be reflected if Enter is pressed
            if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                if (strStorageId.equals("")) return false;

                String strSkuNum = etSkuNum.getText().toString().trim();

                if (strSkuNum.equals(""))
                {
                    //WAPG004016    請輸入[%s]
                    ShowMessage(R.string.WAPG004016, getResources().getString(R.string.SKU_NUM));
                    return false;
                }

                String strSkuLevel = "";
                String strItem = "";
                String strBin = "";

                if (!etItemId.getText().toString().equals(""))
                    strItem = etItemId.getText().toString().trim();

                if (!etSeachBinId.getText().toString().equals(""))
                    strBin = etSeachBinId.getText().toString().trim();

                strSkuLevel = mapSkuLevelKey.get(cmbSkuLevel.getSelectedItem().toString());
                strSkuLevel = strSkuLevel.equals("null") ? "" : strSkuLevel;

                GetDetail(strStorageId, strItem, strSkuLevel, strSkuNum, strBin);
                return true;
            }
            return false;
        }
    };

    private View.OnKeyListener SeachBinOnKey = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            //只有按下Enter才會反映/It will only be reflected if Enter is pressed
            if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                if (strStorageId.equals("")) return false;

                String strBinId = etSeachBinId.getText().toString().trim();

                if (strBinId.equals(""))
                {
                    //WAPG004016    請輸入[%s]
                    ShowMessage(R.string.WAPG004016, getResources().getString(R.string.BIN_ID));
                    return false;
                }

                String strSkuLevel = "";
                String strSkuNum = "";
                String strItem = "";

                if (!etSkuNum.getText().toString().equals(""))
                    strSkuNum = etSkuNum.getText().toString().trim();

                if (!etItemId.getText().toString().equals(""))
                    strItem = etItemId.getText().toString().trim();

                strSkuLevel = mapSkuLevelKey.get(cmbSkuLevel.getSelectedItem().toString());
                strSkuLevel = strSkuLevel.equals("null") ? "" : strSkuLevel;

                GetDetail(strStorageId, strItem, strSkuLevel, strSkuNum, strBinId);
                return true;
            }
            return false;
        }
    };

    private View.OnClickListener ClearData = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Clear();
        }
    };
    //endregion

    //region 方法/Methods

    private void getSpinnerData() {
        //region Set Param
        ArrayList<BModuleObject> lsBObj = new ArrayList<>();

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchStorage");
        bmObj.setRequestID("FetchStorage");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Filter);
        param1.setParameterValue(" AND S.STORAGE_TYPE = 'WMS'");
        bmObj.params.add(param1);
        lsBObj.add(bmObj);

        // 存貨層級/SkuLevel
        BModuleObject bmObjSkuLevel = new BModuleObject();
        bmObjSkuLevel.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSPackingInfo");
        bmObjSkuLevel.setModuleID("BIFetchSkuLevel");
        bmObjSkuLevel.setRequestID("GetSkuLevel");
        lsBObj.add(bmObjSkuLevel);

        CallBIModule(lsBObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    DataTable dtStorage = bModuleReturn.getReturnJsonTables().get("FetchStorage").get("STORAGE");
                    DataTable dtSkuLevel = bModuleReturn.getReturnJsonTables().get("GetSkuLevel").get("WmsSkuLevel");

                    // region Set Storage Spinner
                    // 下拉選單預設選項依語系調整/The default options of the drop-down menu are adjusted according to the language
                    String strSelectSheetType = getResString(getResources().getString(R.string.SELECT_STORAGE));
                    stringArrayStorageList.add(strSelectSheetType);

                    mapStorageKey.put(strSelectSheetType, "null");
                    Iterator it = dtStorage.Rows.iterator();
                    int i = 0;
                    while (it.hasNext()) {
                        DataRow row = (DataRow) it.next();
                        stringArrayStorageList.add(i, row.getValue("IDNAME").toString());
                        mapStorageKey.put(row.getValue("IDNAME").toString(), row.getValue("STORAGE_ID").toString());
                        i++;
                    }
                    simpleArrayAdapter adapter = new simpleArrayAdapter<>(GoodInventoryNewActivity.this, android.R.layout.simple_spinner_dropdown_item, stringArrayStorageList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    cmbStorageId.setAdapter(adapter);
                    cmbStorageId.setSelection(stringArrayStorageList.size() - 1, true);
                    // endregion

                    // region Set Sku Level Spinner
                    // 下拉選單預設選項依語系調整/The default options of the drop-down menu are adjusted according to the language
                    String strSelectSkuLevel = "";
                    stringArraySkuLevelList.add(strSelectSkuLevel);

                    mapSkuLevelKey.put(strSelectSkuLevel, "null");
                    Iterator itSl = dtSkuLevel.Rows.iterator();
                    int idx = 0;
                    while (itSl.hasNext()) {
                        DataRow row = (DataRow) itSl.next();
                        stringArraySkuLevelList.add(idx, row.getValue("DATA_NAME").toString());
                        mapSkuLevelKey.put(row.getValue("DATA_NAME").toString(), row.getValue("DATA_ID").toString());
                        idx++;
                    }
                    simpleArrayAdapter adapterSl = new simpleArrayAdapter<>(GoodInventoryNewActivity.this, android.R.layout.simple_spinner_dropdown_item, stringArraySkuLevelList);
                    adapterSl.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    cmbSkuLevel.setAdapter(adapterSl);
                    cmbSkuLevel.setSelection(stringArraySkuLevelList.size() - 1, true);
                    // endregion
                }
            }
        });
    }

    //取得倉庫/Get Storage
    private void GetStorage() {
        // Call BIModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchStorage");
        bmObj.setRequestID("FetchStorage");
        bmObj.params = new Vector<ParameterInfo>();

		ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Filter);
        param1.setParameterValue(" AND S.STORAGE_TYPE = 'WMS'");
        bmObj.params.add(param1);

        CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    DataTable dt = bModuleReturn.getReturnJsonTables().get("FetchStorage").get("STORAGE");
                    ArrayList<String> stringArrayList = new ArrayList<>();

                        // 下拉選單預設選項依語系調整/The default options of the drop-down menu are adjusted according to the language
                    String strSelectSheetType = getResString(getResources().getString(R.string.SELECT_STORAGE));
                    stringArrayList.add(strSelectSheetType);

                    mapStorageKey.put(strSelectSheetType, "null");
                    Iterator it = dt.Rows.iterator();
                    int i = 0;
                    while (it.hasNext()) {
                        DataRow row = (DataRow) it.next();
                        stringArrayList.add(i, row.getValue("IDNAME").toString());
                        mapStorageKey.put(row.getValue("IDNAME").toString(), row.getValue("STORAGE_ID").toString());
                        i++;
                    }
                    simpleArrayAdapter adapter = new simpleArrayAdapter<>(GoodInventoryNewActivity.this, android.R.layout.simple_spinner_dropdown_item, stringArrayList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    cmbStorageId.setAdapter(adapter);
                    cmbStorageId.setSelection(stringArrayList.size() - 1, true);
                }
            }
        });

    }

    private void CheckInput() {
        //檢查倉庫是否選擇/Check if the warehouse is selected
        strStorageId = mapStorageKey.get(cmbStorageId.getSelectedItem().toString());

        if (strStorageId.equals("null")) {
            //WAPG004015    請選擇倉庫
            ShowMessage(R.string.WAPG004015);
            return;
        }

        GetDetail(strStorageId,  "", "", "", "");
    }

    private void GetDetail(String strStorageId, String strItemId, String strSkuLevel, String strSkuNum, String strBinId) {

        String strFilter = "";

        strFilter = String.format(" AND R.REGISTER_STATUS = 'Available' AND B.BIN_TYPE IN ('IT','IS') AND S.STORAGE_ID = '%s'", strStorageId);

        if (!strItemId.equals(""))
        {
            strFilter = String.format("%s AND I.ITEM_ID = '%s'", strFilter, strItemId);
        }

        if (!strSkuNum.equals(""))
        {
            strFilter = String.format("%s AND '%s' IN (PALLET_ID, BOX3_ID, BOX2_ID, BOX1_ID, REGISTER_ID)", strFilter, strSkuNum);
        }

        if (!strBinId.equals(""))
        {
            strFilter = String.format("%s AND R.BIN_ID = '%s'", strFilter, strBinId);
        }

        // Call BIModule
        ArrayList<BModuleObject> lstBObj = new ArrayList<BModuleObject>();

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIGoodInventoryPortal");
        bmObj.setModuleID("BIFetchRegisterByConditions");
        bmObj.setRequestID("FetchWmsRegister");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIGoodInventoryPortalParam.Filter);
        param1.setParameterValue(strFilter);
        bmObj.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIGoodInventoryPortalParam.SkuLevel);
        param2.setParameterValue(strSkuLevel);
        bmObj.params.add(param2);

        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BIGoodInventoryPortalParam.SkuNum);
        param3.setParameterValue(strSkuNum);
        bmObj.params.add(param3);

        lstBObj.add(bmObj);

        //if (_dtBin.Rows.size() <= 0)
        //{
            BModuleObject bmObjBin = new BModuleObject();
            bmObjBin.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
            bmObjBin.setModuleID("BIFetchBin");
            bmObjBin.setRequestID("FetchBin");
            bmObjBin.params = new Vector<ParameterInfo>();

            // Set Condition
            List<Condition> lstCondition = new ArrayList<Condition>();
            Condition condition = new Condition();
            condition.setAliasTable("S");
            condition.setColumnName("STORAGE_ID");
            condition.setValue(strStorageId);
            condition.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
            lstCondition.add(condition);

            HashMap<String, List<?>> mapCondition = new HashMap<String,List<?>>();
            mapCondition.put(condition.getColumnName(),lstCondition);
            VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
            VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
            MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vkey,vVal);
            String strCond = msdl.generateFinalCode(mapCondition);

            ParameterInfo param = new ParameterInfo();
            param.setParameterID(BIWMSFetchInfoParam.Condition);
            param.setNetParameterValue(strCond);
            bmObjBin.params.add(param);
            lstBObj.add(bmObjBin);
        //}

        CallBIModule(lstBObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn))
                {
                    DataTable dtGroup = bModuleReturn.getReturnJsonTables().get("FetchWmsRegister").get("GROUP_SWMS_REGISTER_WITH_SKU_INFO");
                    DataTable dt = bModuleReturn.getReturnJsonTables().get("FetchWmsRegister").get("SWMS_REGISTER_WITH_SKU_INFO");

                    if (dtGroup.Rows.size() <= 0)
                    {
                        //WAPG004017    查無資料
                        ShowMessage(R.string.WAPG004017);
                        return;
                    }

                    _dtGroupReg = dtGroup;
                    _dtReg = dt;

                    //if (_dtBin.Rows.size() <= 0)
                    //{
                        DataTable dtBin = bModuleReturn.getReturnJsonTables().get("FetchBin").get("BIN");
                        _dtBin = dtBin;

                        ArrayList<String> arrListBin = new ArrayList<String>();
                        for (DataRow dr : _dtBin.Rows) {
                            arrListBin.add(dr.getValue("IDNAME").toString());
                            mapBinKey.put(dr.getValue("IDNAME").toString(), dr.getValue("BIN_ID").toString());
                        }
                    //}

                    List<HashMap<String, String>> list = new ArrayList<>();

                    for (DataRow dr : _dtGroupReg.Rows)
                    {
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("SKU_LEVEL", dr.getValue("SKU_LEVEL").toString());
                        hashMap.put("SKU_NUM", dr.getValue("SKU_NUM").toString());
                        hashMap.put("ITEM_ID", dr.getValue("ITEM_ID").toString());
                        hashMap.put("ITEM_NAME", dr.getValue("ITEM_NAME").toString());
                        hashMap.put("BIN_ID", dr.getValue("BIN_ID").toString());
                        list.add(hashMap);
                    }

                    ListAdapter adapter = new SimpleAdapter(
                            GoodInventoryNewActivity.this,
                            list,
                            R.layout.activity_wms_good_inventory_new_detail_listview,
                            new String[]{"SKU_LEVEL", "SKU_NUM", "ITEM_ID", "ITEM_NAME", "BIN_ID"},
                            new int[]{ R.id.tvSkuLevel, R.id.tvSkuNum, R.id.txtItemId, R.id.txtItemName, R.id.tvBinId}
                    );
                    lstDetail.setAdapter(adapter);

                    if (_dtGroupReg.Rows.size() == 1)
                    {
                        ShowDialog();
                    }
                }
            }
        });
    }

    private void GetDetail(String strStorageId, String strItemId, String strSkuId, String strBinId) {

        String strFilter = "";

        strFilter = String.format(" AND R.REGISTER_STATUS = 'Available' AND B.BIN_TYPE IN ('IT','IS') AND S.STORAGE_ID = '%s'", strStorageId);

        if (!strItemId.equals(""))
        {
            strFilter = String.format("%s AND I.ITEM_ID = '%s'", strFilter, strItemId);
        }

        if (!strSkuId.equals(""))
        {
            strFilter = String.format("%s AND R.REGISTER_ID = '%s'", strFilter, strSkuId);
        }

        if (!strBinId.equals(""))
        {
            strFilter = String.format("%s AND R.BIN_ID = '%s'", strFilter, strBinId);
        }
        // Call BIModule
        ArrayList<BModuleObject> lstBObj = new ArrayList<BModuleObject>();

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchWmsRegister");
        bmObj.setRequestID("FetchWmsRegister");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Filter);
        param1.setParameterValue(strFilter);
        bmObj.params.add(param1);
        lstBObj.add(bmObj);

        if (_dtBin.Rows.size() <= 0)
        {
            BModuleObject bmObjBin = new BModuleObject();
            bmObjBin.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
            bmObjBin.setModuleID("BIFetchBin");
            bmObjBin.setRequestID("FetchBin");
            bmObjBin.params = new Vector<ParameterInfo>();

            // Set Condition
            List<Condition> lstCondition = new ArrayList<Condition>();
            Condition condition = new Condition();
            condition.setAliasTable("S");
            condition.setColumnName("STORAGE_ID");
            condition.setValue(strStorageId);
            condition.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
            lstCondition.add(condition);

            HashMap<String, List<?>> mapCondition = new HashMap<String,List<?>>();
            mapCondition.put(condition.getColumnName(),lstCondition);
            VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
            VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
            MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vkey,vVal);
            String strCond = msdl.generateFinalCode(mapCondition);

            ParameterInfo param = new ParameterInfo();
            param.setParameterID(BIWMSFetchInfoParam.Condition);
            param.setNetParameterValue(strCond);
            bmObjBin.params.add(param);
            lstBObj.add(bmObjBin);
        }

        CallBIModule(lstBObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn))
                {
                    DataTable dt = bModuleReturn.getReturnJsonTables().get("FetchWmsRegister").get("SWMS_REGISTER");

                    if (dt.Rows.size() <= 0)
                    {
                        //WAPG004017    查無資料
                        ShowMessage(R.string.WAPG004017);
                        return;
                    }

                    _dtReg = dt;

                    if (_dtBin.Rows.size() <= 0)
                    {
                        DataTable dtBin = bModuleReturn.getReturnJsonTables().get("FetchBin").get("BIN");
                        _dtBin = dtBin;

                        ArrayList<String> arrListBin = new ArrayList<String>();
                        for (DataRow dr : _dtBin.Rows) {
                            arrListBin.add(dr.getValue("IDNAME").toString());
                            mapBinKey.put(dr.getValue("IDNAME").toString(), dr.getValue("BIN_ID").toString());
                        }
                    }

                    List<HashMap<String, String>> list = new ArrayList<>();

                    for (DataRow dr : _dtReg.Rows)
                    {
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("ITEM_ID", dr.getValue("ITEM_ID").toString());
                        hashMap.put("ITEM_NAME", dr.getValue("ITEM_NAME").toString());
                        hashMap.put("REGISTER_ID", dr.getValue("REGISTER_ID").toString());
                        list.add(hashMap);
                    }

                    ListAdapter adapter = new SimpleAdapter(
                            GoodInventoryNewActivity.this,
                            list,
                            R.layout.activity_wms_good_inventory_new_detail_listview,
                            new String[]{"ITEM_ID", "ITEM_NAME", "REGISTER_ID"},
                            new int[]{ R.id.txtItemId, R.id.txtItemName, R.id.txtLotId}
                    );
                    lstDetail.setAdapter(adapter);

                    if (_dtReg.Rows.size() == 1)
                    {
                        ShowDialog();
                    }
                }
            }
        });
    }

    private void CallBarCodeDisassemble(String content, String barcodeType, String barcodeTypeId)
    {
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIGoodInventoryPortal");
        bmObj.setModuleID("BIFetchRegisterByBarCode");
        bmObj.setRequestID("FetchRegisterByBarCode");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIGoodInventoryPortalParam.StorageId);
        param1.setParameterValue(strStorageId);
        bmObj.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIGoodInventoryPortalParam.BarCodeType);
        param2.setParameterValue(barcodeType);
        bmObj.params.add(param2);

        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BIGoodInventoryPortalParam.BarCodeTypeId);
        param3.setParameterValue(barcodeTypeId);
        bmObj.params.add(param3);

        ParameterInfo param4 = new ParameterInfo();
        param4.setParameterID(BIGoodInventoryPortalParam.BarCodeValue);
        param4.setParameterValue(content);
        bmObj.params.add(param4);

        CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn))
                {
                    DataTable dtGroup = bModuleReturn.getReturnJsonTables().get("FetchRegisterByBarCode").get("GROUP_SWMS_REGISTER_WITH_SKU_INFO");
                    DataTable dt = bModuleReturn.getReturnJsonTables().get("FetchRegisterByBarCode").get("SWMS_REGISTER_WITH_SKU_INFO");

                    if (dtGroup.Rows.size() <= 0)
                    {
                        //WAPG004017    查無資料
                        ShowMessage(R.string.WAPG004017);
                        return;
                    }

                    _dtGroupReg = dtGroup;
                    _dtReg = dt;

                    List<HashMap<String, String>> list = new ArrayList<>();

                    for (DataRow dr : _dtGroupReg.Rows)
                    {
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("SKU_LEVEL", dr.getValue("SKU_LEVEL").toString());
                        hashMap.put("SKU_NUM", dr.getValue("SKU_NUM").toString());
                        hashMap.put("ITEM_ID", dr.getValue("ITEM_ID").toString());
                        hashMap.put("ITEM_NAME", dr.getValue("ITEM_NAME").toString());
                        hashMap.put("BIN_ID", dr.getValue("BIN_ID").toString());
                        list.add(hashMap);
                    }

                    ListAdapter adapter = new SimpleAdapter(
                            GoodInventoryNewActivity.this,
                            list,
                            R.layout.activity_wms_good_inventory_new_detail_listview,
                            new String[]{"SKU_LEVEL", "SKU_NUM", "ITEM_ID", "ITEM_NAME", "BIN_ID"},
                            new int[]{ R.id.tvSkuLevel, R.id.tvSkuNum, R.id.txtItemId, R.id.txtItemName, R.id.tvBinId}
                    );
                    lstDetail.setAdapter(adapter);

                    if (_dtReg.Rows.size() == 1)
                    {
                        ShowDialog();
                    }
                }
            }
        });
    }

    private void ShowDialog() {
        LayoutInflater inflater = LayoutInflater.from(GoodInventoryNewActivity.this);
        View view = inflater.inflate(R.layout.activity_wms_good_inventory_detail_listview_dialog, null);

        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(GoodInventoryNewActivity.this);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);//只允許按下dialog裡的按鍵才能關閉dialog/Only the button in the dialog is allowed to close the dialog
        dialog.show();

        Button btnCloseDialog = view.findViewById(R.id.btnCancel);
        btnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button btnConfirm = view.findViewById(R.id.btnOk);
        final EditText etSkuLevel = view.findViewById(R.id.etSkuLevel);
        EditText etSkuNum = view.findViewById(R.id.etSkuNum);
        etPutAwayBin = view.findViewById(R.id.etPutAwayBin);
        etToBinId = view.findViewById(R.id.etToBinId);
        etBin = view.findViewById(R.id.txtBinID);
        //etBin.setEnabled(false);
        //cmbToBin = view.findViewById(R.id.cmbToBinId);
        cmbCarrier = view.findViewById(R.id.cmbToCarrierId);
        cmbBlock = view.findViewById(R.id.cmbToBlockId);
        cmbIsBin = view.findViewById(R.id.cmbIsBin);
        chkUnRec = view.findViewById(R.id.chkUnRec);
        txtQty = view.findViewById(R.id.txtQty);
        etSkuLevel.setText(_dtGroupReg.Rows.get(0).getValue("SKU_LEVEL").toString());
        etSkuNum.setText(_dtGroupReg.Rows.get(0).getValue("SKU_NUM").toString());
        txtQty.setText(_dtGroupReg.Rows.get(0).getValue("QTY").toString());
        chkUnRec.setChecked(true);

        if (_dtGroupReg.Rows.get(0).getValue("REGISTER_TYPE").toString().equals("MinimizePackSN") || _dtGroupReg.Rows.get(0).getValue("REGISTER_TYPE").toString().equals("PcsSN"))
        {
            txtQty.setEnabled(false);
        }
        else
        {
            txtQty.setEnabled(true);
        }

        if(_dtGroupReg != null && _dtGroupReg.Rows.size() > 0)
            etBin.setText(_dtGroupReg.Rows.get(0).getValue("BIN_ID").toString());

        boolean bCheck = false;
        ArrayList<String> alIs = new ArrayList<String>();
        for(DataRow dr : _dtBin.Rows){
            if(dr.getValue("BIN_TYPE").toString().equals("IS")) {
                if(!alIs.contains(dr.getValue("BIN_ID").toString())){
                    alIs.add(dr.getValue("BIN_ID").toString());
                }
            }
            if(dr.getValue("BIN_ID").toString().equals(etBin.getText().toString())){
                if(dr.getValue("BIN_TYPE").toString().equals("IT"))
                {
                    bCheck = true;
                }
                else if(dr.getValue("BIN_TYPE").toString().equals("IS"))
                {
                    alIs = new ArrayList<String>();
                    alIs.add(etBin.getText().toString());
                }
            }
        }

        if(bCheck)
        {
            if (alIs == null || alIs.size() == 0) {

                String storageId = _dtGroupReg.Rows.get(0).getValue("STORAGE_ID").toString();
                Object[] args = new Object[1];
                args[0] = storageId;

                //WAPG004018    倉庫[%s]未設定入料口
                ShowMessage(R.string.WAPG004018, args);
                return;
            }

            alIs.add("");
            simpleArrayAdapter adapter = new simpleArrayAdapter<>(GoodInventoryNewActivity.this, android.R.layout.simple_spinner_dropdown_item, alIs);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cmbIsBin.setAdapter(adapter);

            if (alIs.size() > 2) // 超過兩個入料口，顯示空白讓使用者選擇
                cmbIsBin.setSelection(alIs.size() - 1, true);
            else if (alIs.size() == 2) // 只有一個入料口，直接顯示即可
                cmbIsBin.setSelection(0, true);

            cmbIsBin.setEnabled(true);
        }
        else
        {
            // 收料或入庫原本就收到入料口，不需讓使用者選擇
            ArrayAdapter<String> adapterIs = new ArrayAdapter<String>(GoodInventoryNewActivity.this, android.R.layout.simple_spinner_dropdown_item, alIs);
            cmbIsBin.setAdapter(adapterIs);
            cmbIsBin.setSelection(0);
            cmbIsBin.setEnabled(false);
        }

        //GetBin("N");
        GetDispatchOrderForInv("Y");


        chkUnRec.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                if(isChecked){
                    //使用推薦/Use recommendation
                    //GetBin();
                    GetDispatchOrderForInv("Y");
                }
                else{
                    //不使用推薦/Do not use recommendation
                    //GetBin();
                    GetDispatchOrderForInv("N");
                }
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //檢查是否選擇入料口/Check if the IS bin is selected
                if(cmbIsBin.getSelectedItem() != null && cmbIsBin.getAdapter().getCount() > 0){
                    if(cmbIsBin.getSelectedItem().toString().equals("")){
                        ShowMessage(R.string.WAPG004013); //WAPG004013    請選擇入料口
                        return;
                    }
                }

                //檢查儲位代碼是否輸入/Check whether the storage code is entered
                //String strBin = cmbToBin.getSelectedItem().toString().trim();
                //if (txtBin.getText().toString().trim().equals("")) {
                String strBin = etPutAwayBin.getText().toString().trim();
                if(strBin.equals("")){
                    //WAPG004019    請輸入上架儲位
                    ShowMessage(R.string.WAPG004019);
                    return;
                }

                if (!strBin.equals(etToBinId.getText().toString().trim())) {
                    //WAPG004020    上架儲位與推薦儲位不符
                    ShowMessage(R.string.WAPG004020);
                    return;
                }

                // 檢查倉庫裡是否有此儲位/Check if there is this storage slot in the warehouse
                //if (!CheckBin(txtBin.getText().toString().trim())){
                if(!CheckBin(strBin)){
                    ShowMessage(R.string.WAPG004012);//WAPG004012   非該倉庫儲位
                    return;
                }

                //檢查數量是否輸入/Check if the quantity is entered
                if (txtQty.getText().toString().trim().equals("")) {
                    ShowMessage(R.string.WAPG004009);//WAPG004009    請輸入數量
                    return;
                }
                
                ExecutoProcess();

                dialog.dismiss();
            }
        });
    }

    private void GetDispatchOrderForInv(final String strRecommand)
    {
        HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
        List<Condition> lstCondition1 = new ArrayList<Condition>();
        List<Condition> lstCondition2 = new ArrayList<Condition>();
        List<Condition> lstCondition3 = new ArrayList<Condition>();

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchHaveCarrier");
        bmObj.setRequestID("BIFetchHaveCarrier");
        bmObj.params = new Vector<ParameterInfo>();

        // ITEM_ID
        Condition conditionItemId = new Condition();
        conditionItemId.setAliasTable("I");
        conditionItemId.setColumnName("ITEM_ID");
        conditionItemId.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
        conditionItemId.setValue(_dtReg.Rows.get(0).getValue("ITEM_ID").toString());
        lstCondition1.add(conditionItemId);
        mapCondition.put(conditionItemId.getColumnName(), lstCondition1);

        // SHEET_STATUS
        Condition conditionFactoryKey = new Condition();
        conditionFactoryKey.setAliasTable("CL");
        conditionFactoryKey.setColumnName("FACTORY_KEY");
        conditionFactoryKey.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
        conditionFactoryKey.setValue(getGlobal().getFactoryKey());
        lstCondition2.add(conditionFactoryKey);
        mapCondition.put(conditionFactoryKey.getColumnName(), lstCondition2);

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

        CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn))
                {
                    DataTable dtMaster = bModuleReturn.getReturnJsonTables().get("BIFetchHaveCarrier").get("CARRIER_BLOCK");

                    if (dtMaster.Rows.size() > 0)
                    {
                        //有載具,走原本的上架規則/If there is a vehicle, follow the original put away rules
                        CarrierRco(strRecommand);
                    }
                    else
                    {
                        //無載具,走動態上架規則/No vehicle, go dynamic put away rules
                        BinRco(strRecommand);
                    }
                }
            }
        });
    }

    private void CarrierRco(String strRecommand) {
        //找出推薦儲位/Find recommended bins
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BPDADispatchOrderForInv");
        bmObj.setModuleID("");
        bmObj.setRequestID("BPDADispatchOrderForInv");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BPDADispatchOrderForInvParam.DispatchLotId);
        param1.setParameterValue(_dtReg.Rows.get(0).getValue("REGISTER_ID").toString());
        bmObj.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BPDADispatchOrderForInvParam.IsRecommend);
        param2.setParameterValue(strRecommand);
        bmObj.params.add(param2);

        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BPDADispatchOrderForInvParam.FetchBinId);
        param3.setParameterValue(_dtReg.Rows.get(0).getValue("BIN_ID").toString());
        bmObj.params.add(param3);

        CallBModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(CheckBModuleReturnInfo(bModuleReturn)){
                    dtBestBin = bModuleReturn.getReturnJsonTables().get("BPDADispatchOrderForInv").get("BinID");
                    dtBestCarrierBlock = bModuleReturn.getReturnJsonTables().get("BPDADispatchOrderForInv").get("CarrierBlockID");

//                    lstBin = new ArrayList<String>();
//                    for(DataRow dr : dtBestBin.Rows){
//                        String strBin = dr.getValue("BIN_ID").toString();
//                        lstBin.add(strBin);
//                    }
//                    adapterBin = new ArrayAdapter<>(GoodInventoryNewActivity.this, android.R.layout.simple_spinner_dropdown_item, lstBin);
//                    cmbToBin.setAdapter(adapterBin);

                    String strBin = dtBestBin.Rows.get(0).getValue("BIN_ID").toString();
                    etToBinId.setText(strBin);

                    lstCarrier = new ArrayList<String>();
                    final HashMap<String, ArrayList<String>> mapBlock = new HashMap<>();
                    for(DataRow dr : dtBestCarrierBlock.Rows){
                        String strCarrier = dr.getValue("CARRIER_ID").toString();
                        String strBlock = dr.getValue("BLOCK_ID").toString();
                        if(!lstCarrier.contains(strCarrier)) {
                            lstCarrier.add(strCarrier);
                        }
                        if(!mapBlock.containsKey(strCarrier)){
                            ArrayList<String> lstBlock = new ArrayList<String>();
                            lstBlock.add(strBlock);
                            mapBlock.put(strCarrier, lstBlock);
                        }
                        else{
                            mapBlock.get(strCarrier).add(strBlock);
                        }
                    }
                    adapterCarrier = new ArrayAdapter<>(GoodInventoryNewActivity.this, android.R.layout.simple_spinner_dropdown_item, lstCarrier);
                    cmbCarrier.setAdapter(adapterCarrier);

                    cmbCarrier.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l){
                            ArrayAdapter<String> adapterBlock = new ArrayAdapter<>(GoodInventoryNewActivity.this, android.R.layout.simple_spinner_dropdown_item, mapBlock.get(cmbCarrier.getSelectedItem().toString()));
                            cmbBlock.setAdapter(adapterBlock);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView){
                            ArrayAdapter<String> adapterBlock = new ArrayAdapter<>(GoodInventoryNewActivity.this, android.R.layout.simple_spinner_dropdown_item, mapBlock.get(cmbCarrier.getSelectedItem().toString()));
                            cmbBlock.setAdapter(adapterBlock);
                        }
                    });

                    if (cmbCarrier.getSelectedItem() != null) {
                        adapterBlock = new ArrayAdapter<>(GoodInventoryNewActivity.this, android.R.layout.simple_spinner_dropdown_item, mapBlock.get(cmbCarrier.getSelectedItem().toString()));
                        cmbBlock.setAdapter(adapterBlock);
                    }
                }
            }
        });
    }

    private void BinRco(final String strRecommand) {
        //找出推薦儲位/Find recommended bins
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIFetchRecoBin");
        bmObj.setModuleID("BIFetchRecoBinByRegisters");
        bmObj.setRequestID("BIFetchRecoBinByRegisters");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo param0 = new ParameterInfo();
        param0.setParameterID(BIFetchRecoBinParam.SkuLevel);
        param0.setParameterValue(_dtGroupReg.Rows.get(0).getValue("SKU_LEVEL").toString());
        bmObj.params.add(param0);

//        ParameterInfo param1 = new ParameterInfo();
//        param1.setParameterID(BIFetchRecoBinParam.RegisterId);
//        param1.setParameterValue(_dtReg.Rows.get(0).getValue("REGISTER_ID").toString());
//        bmObj.params.add(param1);

        List<String> lstRegIds = new ArrayList<>();
        for (DataRow dr : _dtReg.Rows) {
            if (!lstRegIds.contains(dr.get("REGISTER_ID").toString()))
                lstRegIds.add(dr.get("REGISTER_ID").toString());
        }

        VirtualClass vList = VirtualClass.create(VirtualClass.VirtualClassType.String);
        MList mList = new MList(vList);
        String strLstRegIds = mList.generateFinalCode(lstRegIds);

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIFetchRecoBinParam.RegisterIds);
        param1.setNetParameterValue(strLstRegIds);
        bmObj.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIFetchRecoBinParam.ItemId);
        param2.setParameterValue(_dtGroupReg.Rows.get(0).getValue("ITEM_ID").toString());
        bmObj.params.add(param2);

        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BIFetchRecoBinParam.StorageId);
        param3.setParameterValue(_dtGroupReg.Rows.get(0).getValue("STORAGE_ID").toString());
        bmObj.params.add(param3);

        ParameterInfo param4 = new ParameterInfo();
        param4.setParameterID(BIFetchRecoBinParam.Qty);
        param4.setParameterValue(_dtGroupReg.Rows.get(0).getValue("QTY").toString());
        bmObj.params.add(param4);

        ParameterInfo param5 = new ParameterInfo();
        param5.setParameterID(BIFetchRecoBinParam.BinId);
        param5.setParameterValue(_dtGroupReg.Rows.get(0).getValue("BIN_ID").toString());
        bmObj.params.add(param5);

        CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if(CheckBModuleReturnInfo(bModuleReturn)){
                    dtBestBin = bModuleReturn.getReturnJsonTables().get("BIFetchRecoBinByRegisters").get("BinID");
                    dtBestCarrierBlock = bModuleReturn.getReturnJsonTables().get("BIFetchRecoBinByRegisters").get("CarrierBlockID");

                    etBin.setText(dtBestBin.Rows.get(0).getValue("BIN_ID").toString());

                    //region 推薦儲位下拉選單寫法
//                    lstBin = new ArrayList<String>();
//                    if (strRecommand.equals("Y"))
//                    {
//                        lstBin.add(dtBestBin.Rows.get(0).getValue("BIN_ID").toString());
//                    }
//                    else
//                    {
//                        for(DataRow dr : dtBestBin.Rows){
//                            String strBin = dr.getValue("BIN_ID").toString();
//                            lstBin.add(strBin);
//                        }
//                    }
//
//                    adapterBin = new ArrayAdapter<>(GoodInventoryNewActivity.this, android.R.layout.simple_spinner_dropdown_item, lstBin);
//                    cmbToBin.setAdapter(adapterBin);
                    //endregion

                    if (strRecommand.equals("Y"))
                    {
                        String strBin = dtBestBin.Rows.get(0).getValue("BIN_ID").toString();
                        etToBinId.setText(strBin);
                    }

                    lstCarrier = new ArrayList<String>();
                    final HashMap<String, ArrayList<String>> mapBlock = new HashMap<>();
                    for(DataRow dr : dtBestCarrierBlock.Rows){
                        String strCarrier = dr.getValue("CARRIER_ID").toString();
                        String strBlock = dr.getValue("BLOCK_ID").toString();
                        if(!lstCarrier.contains(strCarrier)) {
                            lstCarrier.add(strCarrier);
                        }
                        if(!mapBlock.containsKey(strCarrier)){
                            ArrayList<String> lstBlock = new ArrayList<String>();
                            lstBlock.add(strBlock);
                            mapBlock.put(strCarrier, lstBlock);
                        }
                        else{
                            mapBlock.get(strCarrier).add(strBlock);
                        }
                    }
                    adapterCarrier = new ArrayAdapter<>(GoodInventoryNewActivity.this, android.R.layout.simple_spinner_dropdown_item, lstCarrier);
                    cmbCarrier.setAdapter(adapterCarrier);

                    cmbCarrier.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l){
                            ArrayAdapter<String> adapterBlock = new ArrayAdapter<>(GoodInventoryNewActivity.this, android.R.layout.simple_spinner_dropdown_item, mapBlock.get(cmbCarrier.getSelectedItem().toString()));
                            cmbBlock.setAdapter(adapterBlock);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView){
                            ArrayAdapter<String> adapterBlock = new ArrayAdapter<>(GoodInventoryNewActivity.this, android.R.layout.simple_spinner_dropdown_item, mapBlock.get(cmbCarrier.getSelectedItem().toString()));
                            cmbBlock.setAdapter(adapterBlock);
                        }
                    });

                    if (cmbCarrier.getSelectedItem() != null) {
                        adapterBlock = new ArrayAdapter<>(GoodInventoryNewActivity.this, android.R.layout.simple_spinner_dropdown_item, mapBlock.get(cmbCarrier.getSelectedItem().toString()));
                        cmbBlock.setAdapter(adapterBlock);
                    }
                }
            }
        });
    }

    private boolean CheckBin(String binID){
        for ( Map.Entry<String, String> entry: mapBinKey.entrySet()){
            if(Objects.equals(binID,entry.getValue())){
                return true;
            }
        }

        return false;
    }

    //執行上架/Execute put away
    private void ExecutoProcess() {

        String putAwaySkuLevel = _dtGroupReg.Rows.get(0).getValue("SKU_LEVEL").toString();
        String putAwaySkuNum = _dtGroupReg.Rows.get(0).getValue("SKU_NUM").toString();
        List<InventoryObj> lstInventoryObj = new ArrayList<InventoryObj>();
        List<CheckCountObj> lstChkCountObj = new ArrayList<>();

        for (DataRow dr : _dtGroupReg.Rows) {

            for (DataRow drLot : _dtReg.Rows) {

                double qty = 0.0;

                if (!putAwaySkuLevel.equals("Entity")) // 存貨層級非Entity，僅能依據包裝數量上架/The sku level is not Entity, and can only be put on the shelf according to the packaging quantity
                    qty = Double.valueOf(drLot.get("QTY").toString());
                else // 存貨層級為Entity，且為LotNo或Item可依據畫面上指定數量上架/The sku level is Entity, and it is LotNo or Item, which can be put on the shelves according to the specified quantity on the screen
                    qty = Double.valueOf(txtQty.getText().toString().trim());

                InventoryObj inventoryObj = new InventoryObj();
                inventoryObj.setItemId(_dtGroupReg.Rows.get(0).getValue("ITEM_ID").toString());
                inventoryObj.setStorageId(_dtGroupReg.Rows.get(0).getValue("STORAGE_ID").toString());
                inventoryObj.setLotId(drLot.getValue("REGISTER_ID").toString());
                inventoryObj.setFromBinId(_dtGroupReg.Rows.get(0).getValue("BIN_ID").toString());
                inventoryObj.setBinId(etToBinId.getText().toString().trim()); //cmbToBin.getSelectedItem().toString().trim()
                inventoryObj.setInventoryQty(qty); //Double.valueOf(txtQty.getText().toString().trim())
                inventoryObj.setPortId(etToBinId.getText().toString().trim()); //cmbToBin.getSelectedItem().toString().trim()
                inventoryObj.setFromBinType(_dtGroupReg.Rows.get(0).getValue("BIN_TYPE").toString());
                inventoryObj.setTempBin(cmbIsBin.getSelectedItem().toString().trim());
                inventoryObj.setRegisterSerialKey(Double.valueOf(drLot.getValue("REGISTER_SERIAL_KEY").toString()));
                lstInventoryObj.add(inventoryObj);
            }

            // region 儲存盤點狀態檢查物件 / Store object of checking checking status
            // 20220812 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中/Pass in items, bins, and warehouse data to check whether the inventory status is in inventory
            CheckCountObj chkCountObjFormBin = new CheckCountObj(); // FROM_BIN
            chkCountObjFormBin.setStorageId(dr.getValue("STORAGE_ID").toString());
            chkCountObjFormBin.setItemId(dr.getValue("ITEM_ID").toString());
            chkCountObjFormBin.setBinId(dr.getValue("BIN_ID").toString());
            lstChkCountObj.add(chkCountObjFormBin);

            CheckCountObj chkCountObjToBin = new CheckCountObj(); // TO_BIN
            chkCountObjToBin.setStorageId(dr.getValue("STORAGE_ID").toString());
            chkCountObjToBin.setItemId(dr.getValue("ITEM_ID").toString());
            chkCountObjToBin.setBinId(etToBinId.getText().toString().trim()); //cmbToBin.getSelectedItem().toString().trim()
            lstChkCountObj.add(chkCountObjToBin);
            // endregion
        }

        // Add paramBPDAGoodInventory
        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.INV.Parameter.ParameterObj.InventoryObj", "bmWMS.INV.Param");
        MList mListEnum = new MList(vListEnum);
        String strLsRelatData = mListEnum.generateFinalCode(lstInventoryObj);

        // Call BModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BGoodInventory");
        bmObj.setModuleID("");
        bmObj.setRequestID("BGoodInventory");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo paramInvItemObj = new ParameterInfo();
        paramInvItemObj.setParameterID(BGoodInventoryParam.InvItemObj);
        paramInvItemObj.setNetParameterValue(strLsRelatData);
        bmObj.params.add(paramInvItemObj);

        if(cmbCarrier.getSelectedItem() != null && !cmbCarrier.getSelectedItem().toString().equals("")){

            List<RegCarrierBlockObj> lstRegCarrBlockObj = new ArrayList<>();
            RegCarrierBlockObj carrBlockObj = new RegCarrierBlockObj();
            carrBlockObj.setRegId(_dtReg.Rows.get(0).getValue("REGISTER_ID").toString());
            carrBlockObj.setQty(Double.parseDouble(txtQty.getText().toString().trim()));
            carrBlockObj.setCarrierId(cmbCarrier.getSelectedItem().toString());
            carrBlockObj.setBlockId(cmbBlock.getSelectedItem().toString());

            lstRegCarrBlockObj.add(carrBlockObj);

            //Add param
            VirtualClass vLisEnum2 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
            MList mListEnum2 = new MList(vLisEnum2);
            String strLsRelatData2 = mListEnum2.generateFinalCode(lstRegCarrBlockObj);

            ParameterInfo paramRegCarrBlock = new ParameterInfo();
            paramRegCarrBlock.setParameterID(BGoodInventoryParam.RegCarrierBlockParam);
            paramRegCarrBlock.setNetParameterValue(strLsRelatData2);
            bmObj.params.add(paramRegCarrBlock);
        }

        VirtualClass vListEnum3 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.CheckCountObj", "bmWMS.Library.Param");
        MList mListEnum3 = new MList(vListEnum3);
        String strCheckCountObj = mListEnum3.generateFinalCode(lstChkCountObj);

        ParameterInfo paramExecuteChkStock = new ParameterInfo();
        paramExecuteChkStock.setParameterID(BGoodInventoryParam.ExecuteCheckStock); // 20220812 Add by Ikea 是否執行盤點檢查/Whether to perform an inventory check
        paramExecuteChkStock.setNetParameterValue2("true");
        bmObj.params.add(paramExecuteChkStock);

        ParameterInfo paramChkCountObj = new ParameterInfo(); // 20220812 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中/Pass in items, bins, and warehouse data to check whether the inventory status is in inventory
        paramChkCountObj.setParameterID(BGoodInventoryParam.CheckCountObj);
        paramChkCountObj.setNetParameterValue(strCheckCountObj);
        bmObj.params.add(paramChkCountObj);

        CallBModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    ShowMessage(R.string.WAPG004007);//WAPG004007  作業成功
                    Clear();
                }
            }
        });
    }

    private void Clear() {
        strStorageId = "";
        cmbStorageId.setSelection(stringArrayStorageList.size() - 1, true);
        cmbStorageId.setEnabled(true);

        BarCode.setText("");
        etItemId.setText("");
        cmbSkuLevel.setSelection(stringArraySkuLevelList.size() - 1, true);
        etSkuNum.setText("");
        //etLotId.setText("");
        etSeachBinId.setText("");

        List<HashMap<String, String>> list = new ArrayList<>();

        ListAdapter adapter = new SimpleAdapter(
                GoodInventoryNewActivity.this,
                list,
                R.layout.activity_wms_good_inventory_new_detail_listview,
                new String[]{"SKU_LEVEL", "SKU_NUM", "ITEM_ID", "ITEM_NAME", "BIN_ID"},
                new int[]{ R.id.tvSkuLevel, R.id.tvSkuNum, R.id.txtItemId, R.id.txtItemName, R.id.tvBinId}
        );
        lstDetail.setAdapter(adapter);
    }

    public class simpleArrayAdapter<T> extends ArrayAdapter {
        public simpleArrayAdapter(Context context, int resource, List<T> objects) {
            super(context, resource, objects);
        }

        //複寫這個方法，使提示字改為灰色/Override this method to change the hint to gray
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = super.getView(position, convertView, parent);

            if ( position == getCount() ){
                String hintData = (String) getItem(getCount());
                ((TextView)v.findViewById(android.R.id.text1)).setText("");
                ((TextView)v.findViewById(android.R.id.text1)).setHint(hintData);
            }

            return v;
        }

        //複寫這個方法，使返回的數據沒有最後一項
        //Override this method so that the returned data does not have the last item
        @Override
        public int getCount() {
            // don't display last item. It is used as hint.
            int count = super.getCount();
            return count > 0 ? count - 1 : count;
        }

    }
    //endregion
}

//Error Code
//WAPG004001    請選擇單據代碼
//WAPG004002    查無單據資料
//WAPG004003    單據未確認
//WAPG004004    查無揀貨資料
//WAPG004005    揀貨狀態不為Closed
//WAPG004006    請選擇單據類型
//WAPG004007    作業成功
//WAPG004008    請輸入儲位代碼
//WAPG004009    請輸入數量
//WAPG004010    上架數量超出剩餘可處理數量
//WAPG004011    此單據已完全上架
//WAPG004013    請選擇入料口
//WAPG004014    調整儲位至入料口失敗
//WAPG004015    請選擇倉庫
//WAPG004016    請輸入[%s]
//WAPG004017    查無資料

//WAPG004018    倉庫[%s]未設定入料口
//WAPG004019    請輸入上架儲位
//WAPG004020    上架儲位與推薦儲位不符