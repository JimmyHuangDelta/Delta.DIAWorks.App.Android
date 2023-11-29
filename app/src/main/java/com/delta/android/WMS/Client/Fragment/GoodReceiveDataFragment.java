package com.delta.android.WMS.Client.Fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

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
import com.delta.android.WMS.Client.GoodReceiptReceiveDetailNewActivity;
import com.delta.android.WMS.Client.GridAdapter.GoodNonreceiptReceiveSNAdapter;
import com.delta.android.WMS.Client.GridAdapter.GoodReceiptReceiveExtendGridAdapter;
import com.delta.android.WMS.Param.BGoodReceiptReceiveParam;
import com.delta.android.WMS.Param.BGoodReceiptReceiveWithPackingInfoParam;
import com.delta.android.WMS.Param.BIGoodReceiptReceivePortalParam;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class GoodReceiveDataFragment extends Fragment {

    private DataTable GrMstTable;// get p.1
    private DataTable GrDetTable;// get p.1
    private HashMap SeqQtyOfAllLot;    // get p.1
    private HashMap SeqSkipQcOfAllLot;    // get p.1
    String _scanType = ""; //B:BARCODE、R:REGISTERID
    DataTable dtSN;
    DataTable dtTempSN;
    private DataTable _dtBarCodeData;
    private DataTable _dtExtendData = new DataTable();
    DataTable _dtSize; // get Parent
    DataTable _dtItem; // get Parent
    DataTable _dtSkuLevel;
    ArrayList<String> alSize = new ArrayList<>();
    LinkedHashMap<String, String> mapSizeKey = new LinkedHashMap<String, String>();
    LinkedHashMap<String, Integer> hmSizeID = new LinkedHashMap<String, Integer>();
    ArrayList<String> alItem = new ArrayList<>();
    LinkedHashMap<String, String> mapItemKey = new LinkedHashMap<String, String>();
    LinkedHashMap<String, Integer> hmItemID = new LinkedHashMap<String, Integer>();
    List<? extends Map<String, Object>> lstSkuLevel;
    String skuLevelId = "";
    LinkedHashMap<String, String> hmItemRegisterType = new LinkedHashMap<String, String>();
    String[] _GrrDetSNRerKey;
    String _BarCode = "";
    String _BarCodeType = "";
    String _VendorItemId = "";
    String _Po = "";
    String _PoSeq = "0";
    private HashMap<String, String> ItemSkipQcOfAllLot; // get Parent

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
    Button btnAddSn;
    Button btnSave;
    ListView lvExtent;
    Spinner cmbSize;
    EditText LotCode;
    Spinner cmbItem;
    RelativeLayout rl1;
    LinearLayout llRecCode;

    DataTable dtWgrData = null;
    DataTable dtOrgWgrData = null;
    DataTable dtSn = null;
    DataTable dtWgrDataGroup = null;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,@Nullable  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_wms_good_receive_data_fragment, container, false);

        // region -- 控制項初始化 --
        cmbSkuLevel = view.findViewById(R.id.cmbSkuLevel);
        LotId = view.findViewById(R.id.tvGrLotGridLotId);
        Qty = view.findViewById(R.id.tvGrLotGridQty);
        Uom = view.findViewById(R.id.tvGrLotGridUom);
        Cmt = view.findViewById(R.id.tvGrLotGridCmt);
        MfgDate = view.findViewById(R.id.tvGrLotGridMfgDate);
        MfgDate.setInputType(InputType.TYPE_NULL);
        ExpDate = view.findViewById(R.id.tvGrLotGridExpDate);
        ExpDate.setInputType(InputType.TYPE_NULL);
        IbtnGrLotGridLotIdQRScan = view.findViewById(R.id.ibtnGrLotGridLotIdQRScan);
        btnMfgDateClear = view.findViewById(R.id.btnMfgDateClear);
        btnExpDateClear = view.findViewById(R.id.btnExpDateClear);
        BarCode = view.findViewById(R.id.tvGrBarCode);
        IbtnGrBarCodeQRScan = view.findViewById(R.id.ibtnGrBarCodeQRScan);
        radioGroup = view.findViewById(R.id.radioGroup1);
        radioQRcode = view.findViewById(R.id.radioQRcode);
        radioBarcode = view.findViewById(R.id.radioBarcode);
        btnAddSn = view.findViewById(R.id.btnAddSn);
        btnSave = view.findViewById(R.id.btnSave);
        lvExtent = view.findViewById(R.id.lvExtent);
        cmbSize = view.findViewById(R.id.cmbSize);
        LotCode = view.findViewById(R.id.tvGrLotGridLotCode);
        cmbItem = view.findViewById(R.id.cmbItem);
        rl1 = view.findViewById(R.id.rl1);
        llRecCode = view.findViewById(R.id.llRecCode);
        // endregion

        // region -- 監聽事件 --
        IbtnGrLotGridLotIdQRScan.setOnClickListener(IbtnGrLotGridLotIdQRScanOnClick);
        IbtnGrBarCodeQRScan.setOnClickListener(IbtnGrLBarCodeQRScanOnClick);
        radioGroup.setOnCheckedChangeListener(onCheckRadioGroup);
        LotId.setOnKeyListener(onKeyLotId);
        BarCode.setOnKeyListener(onKeyBarCode);
        MfgDate.setOnClickListener(lsMFDDate);
        ExpDate.setOnClickListener(lsExpiryDate);
        btnMfgDateClear.setOnClickListener(onClickMfgDateClear);
        btnExpDateClear.setOnClickListener(onClickExpDateClear);
        btnAddSn.setOnClickListener(btnAddSnOnClick);
        btnSave.setOnClickListener(btnSaveOnClick);
        cmbSkuLevel.setOnItemSelectedListener(onSelectSkuLevel);
        // endregion

        //region dtSN
        dtSN = new DataTable();
        DataColumn dcSNGrrDetSNRefKey = new DataColumn("GRR_DET_SN_REF_KEY");
        DataColumn dcSN = new DataColumn("SN_ID");
        dtSN.addColumn(dcSNGrrDetSNRefKey);
        dtSN.addColumn(dcSN);
        //endregion

        GrMstTable = (DataTable)getFragmentManager().findFragmentByTag("ReceiveData").getArguments().getSerializable("GrMst");
        GrDetTable = (DataTable)getFragmentManager().findFragmentByTag("ReceiveData").getArguments().getSerializable("GrDet");
        SeqQtyOfAllLot = (HashMap)getFragmentManager().findFragmentByTag("ReceiveData").getArguments().getSerializable("GrrQty");
        SeqSkipQcOfAllLot = (HashMap)getFragmentManager().findFragmentByTag("ReceiveData").getArguments().getSerializable("GrrSkipQc");
        _dtSize = (DataTable)getFragmentManager().findFragmentByTag("ReceiveData").getArguments().getSerializable("Size");
        _dtItem = (DataTable)getFragmentManager().findFragmentByTag("ReceiveData").getArguments().getSerializable("Item");
        _dtSkuLevel = (DataTable)getFragmentManager().findFragmentByTag("ReceiveData").getArguments().getSerializable("SkuLevel");
        ItemSkipQcOfAllLot = (HashMap)getFragmentManager().findFragmentByTag("ReceiveData").getArguments().getSerializable("ItemSkipQcOfAllLot");

        rl1.setVisibility(View.GONE);
        BarCode.setEnabled(false);
        btnAddSn.setText(getResources().getString(R.string.GR_SN) + " (0)");
        setCombobox(getContext());

        return view;
    }

    //region 監聽事件

    //製造日期
    private AdapterView.OnClickListener lsMFDDate = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            setMFDDate();
        }
    };

    //有效日期
    private AdapterView.OnClickListener lsExpiryDate = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            setExpiryDate();
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

    private View.OnClickListener IbtnGrLotGridLotIdQRScanOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            _scanType = "R";
            IntentIntegrator integrator = IntentIntegrator.forSupportFragment(GoodReceiveDataFragment.this);
            //IntentIntegrator integrator = new IntentIntegrator(getActivity());
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

    private View.OnClickListener IbtnGrLBarCodeQRScanOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (!radioQRcode.isChecked() && !radioBarcode.isChecked())
            {
                //WAPG009022 請選擇一維條碼或二維條碼
                ((GoodReceiptReceiveDetailNewActivity) getActivity()).ShowMessage(R.string.WAPG009022);
                return;
            }

            _scanType = "B";
            IntentIntegrator integrator = IntentIntegrator.forSupportFragment(GoodReceiveDataFragment.this);
            //IntentIntegrator integrator = new IntentIntegrator(getActivity());
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

    private View.OnClickListener btnAddSnOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (!CheckRequiredData()) return;

            if (!hmItemRegisterType.get(cmbItem.getSelectedItem().toString()).equals("PcsSN")) return;

            String[] strGrrDetSNRerKey;

            if (_GrrDetSNRerKey == null || _GrrDetSNRerKey.length == 0) { //_GrrDetSNRerKey.equals("")
                Date date = new Date();
                String pattern = "MMddHHmmss.S";
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                strGrrDetSNRerKey = new String[] {sdf.format(date)};
            }
            else
                strGrrDetSNRerKey = _GrrDetSNRerKey;

            ShowSnDialog(strGrrDetSNRerKey, Integer.parseInt(Qty.getText().toString().trim().split("\\.")[0]));
        }
    };

    private View.OnClickListener btnSaveOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!CheckRequiredData()) return;
            SaveGrrData("Add");
        }
    };

    private AdapterView.OnItemLongClickListener lvSnDataOnItemLongClick = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

            return true;
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);

        if (result!= null)
        {
            if (result.getContents() == null)
            {
                Toast.makeText(getActivity(), "result : null", Toast.LENGTH_LONG).show();
            }
            else
            {
                String content = result.getContents();

                if (_scanType.equals("B"))
                {
                    //BarCode.setText(content);
                    //CallBarCodeDisassemble(content);
                    //getSkuNum(GrMstTable.Rows.get(0).getValue("GR_ID").toString(), "", true, "N");

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
                        getSkuNum(GrMstTable.Rows.get(0).getValue("GR_ID").toString(), skuLevelId, strSkuNum, strBarType, strBarTypeId, strBarCodeValue);
                    else if (strBarType.length() > 0 && strBarTypeId.length() > 0 && strBarCodeValue.length() > 0) // 僅用條碼搜尋
                        getSkuNum(GrMstTable.Rows.get(0).getValue("GR_ID").toString(), skuLevelId, strBarType, strBarTypeId, strBarCodeValue);

                }
                else if (_scanType.equals("R"))
                {
                    LotId.setText(content);
                    //getSkuNum(GrMstTable.Rows.get(0).getValue("GR_ID").toString(), LotId.getText().toString().trim(), false, "N");
                    getSkuNum(GrMstTable.Rows.get(0).getValue("GR_ID").toString(), skuLevelId, LotId.getText().toString().trim());
                }
            }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

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

                InputMethodManager manager = (InputMethodManager) ((GoodReceiptReceiveDetailNewActivity) getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                //getSkuNum(GrMstTable.Rows.get(0).getValue("GR_ID").toString(), LotId.getText().toString().trim(), false, "N");
                getSkuNum(GrMstTable.Rows.get(0).getValue("GR_ID").toString(), skuLevelId, LotId.getText().toString().trim());

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

                InputMethodManager manager = (InputMethodManager) ((GoodReceiptReceiveDetailNewActivity) getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(view.getWindowToken(), 0);

                //CallBarCodeDisassemble(BarCode.getText().toString().trim());

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
                    getSkuNum(GrMstTable.Rows.get(0).getValue("GR_ID").toString(), skuLevelId, strSkuNum, strBarType, strBarTypeId, strBarCodeValue);
                else if (strBarType.length() > 0 && strBarTypeId.length() > 0 && strBarCodeValue.length() > 0) // 僅用條碼搜尋
                    getSkuNum(GrMstTable.Rows.get(0).getValue("GR_ID").toString(), skuLevelId, strBarType, strBarTypeId, strBarCodeValue);

                return true;
            }

            return false;
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

    //endregion

    private void setCombobox(Context context) {

        //region Item
        alItem.clear();
        for (int i = 0; i < _dtItem.Rows.size(); i++) {
            mapItemKey.put(_dtItem.Rows.get(i).get("IDNAME").toString(), _dtItem.Rows.get(i).get("ITEM_ID").toString());
            alItem.add(i,_dtItem.Rows.get(i).get("ITEM_ID").toString());

            hmItemRegisterType.put(_dtItem.Rows.get(i).get("ITEM_ID").toString(),_dtItem.Rows.get(i).get("REGISTER_TYPE").toString());
        }

        Collections.sort(alItem); // List依據字母順序排序

        // 下拉選單預設選項依語系調整
        String strSelectItemId =  "";
        alItem.add(strSelectItemId);

        for (int i =0; i < alItem.size(); i++) {
            hmItemID.put(alItem.get(i), i);
        }

        // 下拉選單預設選項依語系調整
        SimpleArrayAdapter adapterItem = new SimpleArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, alItem);
        adapterItem.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmbItem.setAdapter(adapterItem);
        cmbItem.setSelection(alItem.size() - 1, true);
        //endregion

        //region Size
        alSize.clear();
        for (int i = 0; i < _dtSize.Rows.size(); i++) {
            mapSizeKey.put(_dtSize.Rows.get(i).get("IDNAME").toString(), _dtSize.Rows.get(i).get("SIZE_ID").toString());
            alSize.add(i,_dtSize.Rows.get(i).get("SIZE_ID").toString());
        }
        alSize.add("*"); // 多加入 * ，代表包裝箱內有多種尺寸
        //Collections.sort(alSize, Collections.reverseOrder());
        Collections.sort(alSize); // List依據字母順序排序

        // 下拉選單預設選項依語系調整
        String strSelectSizeId =  "";
        alSize.add(strSelectSizeId);

        for (int i =0; i < alSize.size(); i++)
        {
            hmSizeID.put(alSize.get(i), i);
        }

        // 下拉選單預設選項依語系調整
        SimpleArrayAdapter adapter = new SimpleArrayAdapter<>(context, android.R.layout.simple_spinner_item, alSize);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmbSize.setAdapter(adapter);
        cmbSize.setSelection(alSize.size() - 1, true);
        //endregion

        //region SkuLevel
        DataRow drDefaultItem = _dtSkuLevel.newRow();
        drDefaultItem.setValue("DATA_ID", ""); // 下拉式選單default空白

        if (_dtSkuLevel != null && _dtSkuLevel.Rows.size() > 0)
            _dtSkuLevel.Rows.add(drDefaultItem);
        lstSkuLevel = (List<? extends Map<String, Object>>) _dtSkuLevel.toListHashMap();
        SimpleAdapter adapterSkuLevel = new AnotherSimpleArrayAdapter<>(getContext(), lstSkuLevel, android.R.layout.simple_spinner_item, new String[]{"DATA_ID", "DATA_NAME"}, new int[]{0, android.R.id.text1});
        adapterSkuLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmbSkuLevel.setAdapter(adapterSkuLevel);
        cmbSkuLevel.setSelection(lstSkuLevel.size()-1, true);
        //endregion
    }

    public void setMFDDate(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
        //標頭
        builder.setTitle(R.string.MFD_DATE);
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

    public void setExpiryDate(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
        //標頭
        builder.setTitle(R.string.EXPIRY_DATE);
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

    // 只有輸入條碼
    private void getSkuNum(String strGrId, String strSkuLevel, String strBarType, String strBarTypeId, final String strBarCodeValue) {

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

        ParameterInfo paramIsReceived = new ParameterInfo();
        paramIsReceived.setParameterID(BIGoodReceiptReceivePortalParam.IsReceived);
        paramIsReceived.setParameterValue("N");
        biObj1.params.add(paramIsReceived);

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

        BarCode.getText().clear(); // 先清除，若拆解成功再回填

        ((GoodReceiptReceiveDetailNewActivity)getActivity()).CallBIModule(biObj1, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {

                if (((GoodReceiptReceiveDetailNewActivity)getActivity()).CheckBModuleReturnInfo(bModuleReturn)) {

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

                            String showMsg = String.format(((GoodReceiptReceiveDetailNewActivity) getActivity()).getResString(getResources().getString(R.string.CHANGE_SKU_LEVEL)), args);

                            ((GoodReceiptReceiveDetailNewActivity) getActivity()).ShowMessage(showMsg, new ShowMessageEvent() {
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
                                        ((GoodReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009027); //WAPG009027   查無物料代碼[%s]!
                                        return;
                                    }

                                    SimpleAdapter adapterSkuLevel = new AnotherSimpleArrayAdapter<>(getContext(), lstSkuLevel, android.R.layout.simple_spinner_item, new String[]{"DATA_ID", "DATA_NAME"}, new int[]{0, android.R.id.text1});
                                    adapterSkuLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    cmbSkuLevel.setAdapter(adapterSkuLevel);
                                    cmbSkuLevel.setSelection(levelPosition, true);
                                    // endregion

                                    LotId.setText(skuData[1]);

                                    //getSkuNum(GrMstTable.Rows.get(0).getValue("GR_ID").toString(), LotId.getText().toString().trim(), false, "N");
                                }
                            });
                        }

                    } else {

                        ClearData(true);

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
                                    Integer item = hmItemID.get(row.getValue("BARCODE_VALUE").toString());
                                    cmbItem.setSelection(item, true);
                                    cmbItem.setEnabled(false);
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
                            LayoutInflater inflater = getActivity().getLayoutInflater();
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

                        if (hmItemRegisterType.get(itemId).equals("PcsSN")) {

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
                                DataRow drNew = dtSN.newRow();
                                drNew.setValue("GRR_DET_SN_REF_KEY", dr.get("GRR_DET_SN_REF_KEY").toString().trim());
                                drNew.setValue("SN_ID", dr.get("SN_ID").toString().trim());
                                dtSN.Rows.add(drNew);
                            }

                            _GrrDetSNRerKey = aryRefKeys;
                            btnAddSn.setText(getResources().getString(R.string.GR_SN) + " (" + dtSN.Rows.size() + ")");

                        }
                        //endregion
                    }
                }
            }
        });
    }

    // 只有輸入存貨編碼
    private void getSkuNum(String strGrId, String strSkuLevel, String strSkuNum) {

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
        paramIsReceived.setParameterValue("N");
        biObj1.params.add(paramIsReceived);

        ((GoodReceiptReceiveDetailNewActivity)getActivity()).CallBIModule(biObj1, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {

                if (((GoodReceiptReceiveDetailNewActivity)getActivity()).CheckBModuleReturnInfo(bModuleReturn)) {

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

                            String showMsg = String.format(((GoodReceiptReceiveDetailNewActivity) getActivity()).getResString(getResources().getString(R.string.CHANGE_SKU_LEVEL)), args);

                            ((GoodReceiptReceiveDetailNewActivity) getActivity()).ShowMessage(showMsg, new ShowMessageEvent() {
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
                                        ((GoodReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009027); //WAPG009027   查無物料代碼[%s]!
                                        return;
                                    }

                                    SimpleAdapter adapterSkuLevel = new AnotherSimpleArrayAdapter<>(getContext(), lstSkuLevel, android.R.layout.simple_spinner_item, new String[]{"DATA_ID", "DATA_NAME"}, new int[]{0, android.R.id.text1});
                                    adapterSkuLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    cmbSkuLevel.setAdapter(adapterSkuLevel);
                                    cmbSkuLevel.setSelection(levelPosition, true);
                                    // endregion

                                    LotId.setText(skuData[1]);

                                    getSkuNum(GrMstTable.Rows.get(0).getValue("GR_ID").toString(), skuData[0], skuData[1]);
                                }
                            });
                        }

                    } else {

                        ClearData(true);
                        setEnable(false, true);

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
                            Integer item = hmItemID.get(itemId);
                            cmbItem.setSelection(item, true);
                        }

                        if (lotId.length() > 0) {
                            LotId.setText(skuNum);
                        }

                        if (qty.length() > 0) {
                            Qty.setText(qty);
                        }

                        if (lotCode.length() > 0) {
                            LotCode.setText(qty);
                        }

                        if (mfgDate.length() > 0) {
                            MfgDate.setText(mfgDate);
                        }

                        if (expDate.length() > 0) {
                            ExpDate.setText(expDate);
                        }

                        if (sizeId.length() > 0) {
                            Integer i = hmSizeID.get(sizeId);
                            cmbSize.setSelection(i, true);
                        }

                        if (cmt.length() > 0) {
                            Cmt.setText(cmt);
                        }
                        //endregion

                        //region 同步 PcsSN 的表
                        if (itemId.equals(""))
                            return;

                        if (hmItemRegisterType.get(itemId).equals("PcsSN")) {

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
                                DataRow drNew = dtSN.newRow();
                                drNew.setValue("GRR_DET_SN_REF_KEY", dr.get("GRR_DET_SN_REF_KEY").toString().trim());
                                drNew.setValue("SN_ID", dr.get("SN_ID").toString().trim());
                                dtSN.Rows.add(drNew);
                            }

                            _GrrDetSNRerKey = aryRefKeys;
                            btnAddSn.setText(getResources().getString(R.string.GR_SN) + " (" + dtSN.Rows.size() + ")");

                        }
                        //endregion
                    }
                }
            }
        });

    }

    // 輸入條碼 & 存貨編碼
    private void getSkuNum(String strGrId, String strSkuLevel, String strSkuNum, String strBarType, String strBarTypeId, final String strBarCodeValue) {

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
        paramSkuLevel.setParameterValue(strSkuLevel);
        biObj1.params.add(paramSkuLevel);

        ParameterInfo paramSkuNum = new ParameterInfo();
        paramSkuNum.setParameterID(BIGoodReceiptReceivePortalParam.SkuNum);
        paramSkuNum.setParameterValue(strSkuNum);
        biObj1.params.add(paramSkuNum);

        ParameterInfo paramIsReceived = new ParameterInfo();
        paramIsReceived.setParameterID(BIGoodReceiptReceivePortalParam.IsReceived);
        paramIsReceived.setParameterValue("N");
        biObj1.params.add(paramIsReceived);

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

        BarCode.getText().clear(); // 先清除，若拆解成功再回填

        ((GoodReceiptReceiveDetailNewActivity)getActivity()).CallBIModule(biObj1, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {

            if (((GoodReceiptReceiveDetailNewActivity)getActivity()).CheckBModuleReturnInfo(bModuleReturn)) {

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

                            String showMsg = String.format(((GoodReceiptReceiveDetailNewActivity) getActivity()).getResString(getResources().getString(R.string.CHANGE_SKU_LEVEL)), args);

                            ((GoodReceiptReceiveDetailNewActivity) getActivity()).ShowMessage(showMsg, new ShowMessageEvent() {
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
                                        ((GoodReceiptReceiveDetailNewActivity)getActivity()).ShowMessage(R.string.WAPG009027); //WAPG009027   查無物料代碼[%s]!
                                        return;
                                    }

                                    SimpleAdapter adapterSkuLevel = new AnotherSimpleArrayAdapter<>(getContext(), lstSkuLevel, android.R.layout.simple_spinner_item, new String[]{"DATA_ID", "DATA_NAME"}, new int[]{0, android.R.id.text1});
                                    adapterSkuLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    cmbSkuLevel.setAdapter(adapterSkuLevel);
                                    cmbSkuLevel.setSelection(levelPosition, true);
                                    // endregion

                                    LotId.setText(skuData[1]);

                                    getSkuNum(GrMstTable.Rows.get(0).getValue("GR_ID").toString(), skuData[0], skuData[1]);
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
                                    itemId = row.getValue("BARCODE_VALUE").toString();
                                    Integer item = hmItemID.get(row.getValue("BARCODE_VALUE").toString());
                                    cmbItem.setSelection(item, true);
                                    cmbItem.setEnabled(false);
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
                            LayoutInflater inflater = getActivity().getLayoutInflater();
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

                        if (hmItemRegisterType.get(itemId).equals("PcsSN")) {

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
                                DataRow drNew = dtSN.newRow();
                                drNew.setValue("GRR_DET_SN_REF_KEY", dr.get("GRR_DET_SN_REF_KEY").toString().trim());
                                drNew.setValue("SN_ID", dr.get("SN_ID").toString().trim());
                                dtSN.Rows.add(drNew);
                            }

                            _GrrDetSNRerKey = aryRefKeys;
                            btnAddSn.setText(getResources().getString(R.string.GR_SN) + " (" + dtSN.Rows.size() + ")");

                        }
                        //endregion

                        //region
//                        if (bModuleReturn.getReturnJsonTables().get("BIFetchReceiptBySkuNumAndBarCode").get("BARCODE_DATA") != null) {
//
//                            _dtBarCodeData = bModuleReturn.getReturnJsonTables().get("BIFetchSkuNumData").get("BARCODE_DATA");
//                            _dtExtendData = bModuleReturn.getReturnJsonTables().get("BIFetchSkuNumData").get("EXTEND_DATA");
//
//                            ClearData(true);
//
//                            //region 主要條碼資料填值
//                            for (DataRow row : _dtBarCodeData.Rows)
//                            {
//                                switch (row.getValue("BARCODE_VARIABLE_ID").toString())
//                                {
//                                    case "ITEM_ID":
//                                        Integer item = hmItemID.get(row.getValue("BARCODE_VALUE").toString());
//                                        cmbItem.setSelection(item, true);
//                                        cmbItem.setEnabled(false);
//                                        break;
//
//                                    case "REGISTER_ID":
//                                        LotId.setText(row.getValue("BARCODE_VALUE").toString());
//                                        LotId.setEnabled(false);
//                                        IbtnGrLotGridLotIdQRScan.setEnabled(false);
//                                        break;
//
//                                    case "QTY":
//                                        Qty.setText(row.getValue("BARCODE_VALUE").toString());
//                                        Qty.setEnabled(false);
//                                        break;
//
//                                    case "LOT_CODE":
//                                        LotCode.setText(row.getValue("BARCODE_VALUE").toString());
//                                        LotCode.setEnabled(false);
//                                        break;
//
//                                    case "MFG_DATE":
//                                        MfgDate.setText(row.getValue("BARCODE_VALUE").toString());
//                                        MfgDate.setEnabled(false);
//                                        btnMfgDateClear.setEnabled(false);
//                                        break;
//
//                                    case "EXP_DATE":
//                                        ExpDate.setText(row.getValue("BARCODE_VALUE").toString());
//                                        ExpDate.setEnabled(false);
//                                        btnExpDateClear.setEnabled(false);
//                                        break;
//
//                                    case "SIZE_ID":
//                                        Integer i = hmSizeID.get(row.getValue("BARCODE_VALUE").toString());
//                                        cmbSize.setSelection(i, true);
//                                        cmbSize.setEnabled(false);
//                                        break;
//
//                                    case "VENDOR_ITEM_ID":
//                                        _VendorItemId = row.getValue("BARCODE_VALUE").toString();
//                                        break;
//
//                                    case "CMT":
//                                        Cmt.setText(row.getValue("BARCODE_VALUE").toString());
//                                        Cmt.setEnabled(false);
//                                        break;
//
//                                    case "PO":
//                                        _Po = row.getValue("PO").toString();
//                                        break;
//
//                                    case "PO_ITEM":
//                                        _PoSeq = row.getValue("PO_ITEM").toString();
//                                        break;
//
//                                    case "PO_SEQ":
//                                        _PoSeq = row.getValue("PO_SEQ").toString();
//                                        break;
//
//                                    default:
//                                        break;
//                                }
//                            }
//                            //endregion
//
//                            //region Extend
//                            if (_dtExtendData.Rows.size() > 0)
//                            {
//                                rl1.setVisibility(View.VISIBLE);
//                                LayoutInflater inflater = getActivity().getLayoutInflater();
//                                GoodReceiptReceiveExtendGridAdapter adapter = new GoodReceiptReceiveExtendGridAdapter(_dtExtendData, inflater);
//                                lvExtent.setAdapter(adapter);
//                            }
//                            else
//                            {
//                                rl1.setVisibility(View.GONE);
//                            }
//                            //endregion
//
//                            if (radioBarcode.isChecked())
//                                _BarCodeType = "BARCODE";
//                            else if (radioQRcode.isChecked())
//                                _BarCodeType = "QRCODE";
//
//                            BarCode.setText(strBarCodeValue);
//                        }
//                        else {
//
//                            // 從中間表來
//
//                            dtWgrData = bModuleReturn.getReturnJsonTables().get("BIFetchSkuNumData").get("SWMS_WGR_CONT");
//                            dtOrgWgrData = bModuleReturn.getReturnJsonTables().get("BIFetchSkuNumData").get("dtOrgWgrData");
//                            DataTable dtOrgSn = bModuleReturn.getReturnJsonTables().get("BIFetchSkuNumData").get("dtSn");
//                            dtWgrDataGroup = bModuleReturn.getReturnJsonTables().get("BIFetchSkuNumData").get("dtWgrWithSkuLevel");
//
//                            // 這邊要確認如果是條碼拆解出來的東西需要接到 dtBarCode, dtExtend
//
//                            String skuNum = dtWgrDataGroup.Rows.get(0).getValue("SKU_NUM").toString().trim();
//                            String itemId = dtWgrDataGroup.Rows.get(0).getValue("ITEM_ID").toString().trim();
//                            String lotId = dtWgrDataGroup.Rows.get(0).getValue("LOT_ID").toString().trim();
//                            double qty = Double.parseDouble(dtWgrDataGroup.Rows.get(0).getValue("QTY").toString().trim());
//                            String mfgDate = dtWgrDataGroup.Rows.get(0).getValue("MFG_DATE").toString().trim().substring(0,10) + " 00:00:00";
//                            String expDate = dtWgrDataGroup.Rows.get(0).getValue("EXP_DATE").toString().trim().substring(0,10) + " 23:59:59";
//                            String lotCode = dtWgrDataGroup.Rows.get(0).getValue("LOT_CODE").toString().trim();
//                            String sizeId = dtWgrDataGroup.Rows.get(0).getValue("SIZE_ID").toString().trim();
//                            String cmt = dtWgrDataGroup.Rows.get(0).getValue("CMT").toString().trim();
//
//                            LotId.setText(skuNum);
//                            Qty.setText(String.valueOf(qty));
//                            MfgDate.setText(mfgDate);
//                            ExpDate.setText(expDate);
//                            LotCode.setText(lotCode);
//                            Cmt.setText(cmt);
//                            //LotId.setEnabled(false);
//                            Qty.setEnabled(false);
//                            MfgDate.setEnabled(false);
//                            ExpDate.setEnabled(false);
//                            LotCode.setEnabled(false);
//                            Cmt.setEnabled(false);
//                            cmbItem.setEnabled(false);
//
//
//                            Integer item = hmItemID.get(itemId);
//                            cmbItem.setSelection(item, true);
//
//                            if (sizeId.length() > 0) {
//                                Integer sizeItem = hmSizeID.get(sizeId);
//                                cmbSize.setSelection(sizeItem, true);
//                            }
//
//                            if (hmItemRegisterType.get(itemId).equals("PcsSN")) {
//
//                                List<String> lstRefKeys = new ArrayList<>();
//
//                                for (int i = 0; i < dtOrgWgrData.Rows.size(); i++) {
//                                    String refKey = dtOrgWgrData.Rows.get(i).getValue("GRR_DET_SN_REF_KEY").toString().trim();
//                                    if (!lstRefKeys.contains(refKey))
//                                        lstRefKeys.add(refKey);
//                                }
//
//                                String[] aryRefKeys = new String[lstRefKeys.size()];
//                                lstRefKeys.toArray(aryRefKeys);
//
//                                for(DataRow dr : dtOrgSn.Rows) {
//                                    DataRow drNew = dtSN.newRow();
//                                    drNew.setValue("GRR_DET_SN_REF_KEY", dr.get("GRR_DET_SN_REF_KEY").toString().trim());
//                                    drNew.setValue("SN_ID", dr.get("SN_ID").toString().trim());
//                                    dtSN.Rows.add(drNew);
//                                }
//
//                                _GrrDetSNRerKey = aryRefKeys;
//                                btnAddSn.setText(getResources().getString(R.string.GR_SN) + " (" + dtSN.Rows.size() + ")");
//
//                            }
//
//                        }
                        //endregion
                    }
                }
            }
        });
    }

    private void CallBarCodeDisassemble(final String content) {
        String strBarType = "";
        String strBarTypeId = "";

        if (radioBarcode.isChecked()) {

            strBarType = "BARCODE_TYPE";
            strBarTypeId = "ReceiveBarCode";

        } else if (radioQRcode.isChecked()) {

            strBarType = "QRCODE_TYPE";
            strBarTypeId = "ReceiveQRCode";

        }

        // region Set BIModule
        // List of BIModules
        List<BModuleObject> bmObjs = new ArrayList<BModuleObject>();

        // 條碼拆解
        BModuleObject biObj1 = new BModuleObject();
        biObj1.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIGoodReceiptReceivePortal");
        biObj1.setModuleID("BIFetchReceiptBarCodeResult");
        biObj1.setRequestID("BIFetchReceiptBarCodeResult");
        biObj1.params = new Vector<>();

        // Input param
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIGoodReceiptReceivePortalParam.BarCodeType);
        param1.setParameterValue(strBarType);
        biObj1.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIGoodReceiptReceivePortalParam.BarCodeTypeId);
        param2.setParameterValue(strBarTypeId);
        biObj1.params.add(param2);

        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BIGoodReceiptReceivePortalParam.BarCodeValue);
        param3.setParameterValue(content);
        biObj1.params.add(param3);

        ParameterInfo param4 = new ParameterInfo();
        param4.setParameterID(BIGoodReceiptReceivePortalParam.GrId);
        param4.setParameterValue(GrMstTable.Rows.get(0).getValue("GR_ID").toString());
        biObj1.params.add(param4);

        bmObjs.add(biObj1);
        // endregion

        ((GoodReceiptReceiveDetailNewActivity)getActivity()).CallBIModule(bmObjs, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (((GoodReceiptReceiveDetailNewActivity)getActivity()).CheckBModuleReturnInfo(bModuleReturn)) {

                    _dtBarCodeData = bModuleReturn.getReturnJsonTables().get("BIFetchReceiptBarCodeResult").get("BARCODE_DATA");
                    _dtExtendData = bModuleReturn.getReturnJsonTables().get("BIFetchReceiptBarCodeResult").get("EXTEND_DATA");

                    ClearData(true);

                    //region 主要條碼資料填值
                    for (DataRow row : _dtBarCodeData.Rows)
                    {
                        switch (row.getValue("BARCODE_VARIABLE_ID").toString())
                        {
                            case "ITEM_ID":
                                Integer item = hmItemID.get(row.getValue("BARCODE_VALUE").toString());
                                cmbItem.setSelection(item, true);
                                cmbItem.setEnabled(false);
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

                                default:
                                    break;
                        }
                    }
                    //endregion

                    //region Extend
                    if (_dtExtendData.Rows.size() > 0)
                    {
                        rl1.setVisibility(View.VISIBLE);
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        GoodReceiptReceiveExtendGridAdapter adapter = new GoodReceiptReceiveExtendGridAdapter(_dtExtendData, inflater);
                        lvExtent.setAdapter(adapter);
                    }
                    else
                    {
                        rl1.setVisibility(View.GONE);
                    }
                    //endregion

                    BarCode.setText(content);
                    _BarCode = content;

                    if (radioBarcode.isChecked())
                        _BarCodeType = "BARCODE";
                    else if (radioQRcode.isChecked())
                        _BarCodeType = "QRCODE";

                } else {
                    BarCode.getText().clear();
                }
            }
        });
    }

    private void ShowSnDialog(final String[] strGrrDetSNRefKey, int snQty) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View viewDialog = inflater.inflate(R.layout.activity_good_nonreceipt_receive_lot_sn,null );
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
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

        if (dtSN.Rows.size() > 0) {
            for (int i = dtSN.Rows.size() -1; i >= 0; i--) {

                String refKey = dtSN.Rows.get(i).getValue("GRR_DET_SN_REF_KEY").toString();
                String snID = dtSN.Rows.get(i).getValue("SN_ID").toString();

                for (String strKey : strGrrDetSNRefKey) {

                    if (refKey.equals(strKey)) { //strGrrDetSNRefKey

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

        LayoutInflater inflaterSNTemp = LayoutInflater.from(getContext());
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
                        ((GoodReceiptReceiveDetailNewActivity) getActivity()).ShowMessage(R.string.WAPG009015);//WAPG009015    序號重複!
                        return false;
                    }

                    DataRow drNew = dtTempSN.newRow();
                    drNew.setValue("GRR_DET_SN_REF_KEY",strGrrDetSNRefKey);
                    drNew.setValue("SN_ID",snID);
                    dtTempSN.Rows.add(drNew);
                    alTempSN.add(snID);

                    // ListView 顯示
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    GoodNonreceiptReceiveSNAdapter adapter = new GoodNonreceiptReceiveSNAdapter(dtTempSN, inflater);
                    ListView lsSN = viewDialog.findViewById(R.id.lvReceiveSN);
                    lsSN.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    // 清空刷入的欄位
                    etSN.getText().clear();
                    etSN.requestFocus();
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
                    ((GoodReceiptReceiveDetailNewActivity) getActivity()).ShowMessage(R.string.WAPG009009);//WAPG009009    未滿需求量!
                    return;
                }
                if(dtTempSN.Rows.size() > intSNQty){
                    ((GoodReceiptReceiveDetailNewActivity) getActivity()).ShowMessage(R.string.WAPG009012); //WAPG009012    已超出需求量!
                    return;
                }

                //移動 dtTempSN到 dtSN
                dtSN = new DataTable();
                for (int i = 0 ; i < dtTempSN.Rows.size(); i++){
                    DataRow drNew = dtSN.newRow();
                    drNew.setValue("GRR_DET_SN_REF_KEY",dtTempSN.Rows.get(i).get("GRR_DET_SN_REF_KEY").toString());
                    drNew.setValue("SN_ID",dtTempSN.Rows.get(i).get("SN_ID").toString());
                    dtSN.Rows.add(drNew);
                }

                btnAddSn.setText(getResources().getString(R.string.GR_SN) + " (" + dtSN.Rows.size() + ")");
                dtTempSN = null;
                alTempSN.clear();
                dialog.dismiss();
                _GrrDetSNRerKey = strGrrDetSNRefKey;
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

                btnAddSn.setText(getResources().getString(R.string.GR_SN) + " (" + dtSN.Rows.size() + ")");
                dtTempSN = null;
                dialog.dismiss();
            }
        });
        //endregion
    }

    private boolean CheckRequiredData() {
        if (LotId.getText().toString().equals(""))
        {
            //WAPG009023 請輸入批號
            ((GoodReceiptReceiveDetailNewActivity) getActivity()).ShowMessage(R.string.WAPG009023);
            LotId.requestFocus();
            return false;
        }

        String strItem = mapItemKey.get(cmbItem.getSelectedItem().toString());
        if (cmbItem.getSelectedItemPosition() == -1){
            //WAPG009004 請選擇物料
            ((GoodReceiptReceiveDetailNewActivity) getActivity()).ShowMessage(R.string.WAPG009004);
            return false;
        }

        if (Qty.getText().toString().equals("")) {
            //WAPG009005 請輸入數量
            ((GoodReceiptReceiveDetailNewActivity) getActivity()).ShowMessage(R.string.WAPG009005);
            Qty.requestFocus();
            return false;
        }

        if (MfgDate.getText().toString().equals("")) {
            //WAPG009019 請輸入製造日期
            ((GoodReceiptReceiveDetailNewActivity) getActivity()).ShowMessage(R.string.WAPG009019);
            MfgDate.requestFocus();
            return false;
        }

        if (ExpDate.getText().toString().equals("")) {
            //WAPG009020 請輸入有效期限
            ((GoodReceiptReceiveDetailNewActivity) getActivity()).ShowMessage(R.string.WAPG009020);
            ExpDate.requestFocus();
            return false;
        }

        if ( !(skuLevelId.equals("") || skuLevelId.equals("Entity")) ) {

            if (dtOrgWgrData == null || dtOrgWgrData.Rows.size() <= 0) {

                //WAPG009020 請輸入有效期限
                ((GoodReceiptReceiveDetailNewActivity) getActivity()).ShowMessage("請選擇存貨層級並刷入存貨編號由中間表(SWMS_WGR_CONT)帶入資料");
                cmbSkuLevel.requestFocus();
                return false;
            }
        }

        return true;
    }

    private void SaveGrrData(String type) {

        //region set param
        BGoodReceiptReceiveWithPackingInfoParam sheet = new BGoodReceiptReceiveWithPackingInfoParam();
        BGoodReceiptReceiveWithPackingInfoParam.GrrWithPackingInfoMasterObj sheet1 = sheet.new GrrWithPackingInfoMasterObj();

        sheet1.setGrID(GrMstTable.Rows.get(0).getValue("GR_ID").toString());

        ArrayList<BGoodReceiptReceiveWithPackingInfoParam.GrrWithPackingInfoDetObj> detList = new ArrayList<>();

        if (dtOrgWgrData != null && dtOrgWgrData.Rows.size() > 0) {

            for( DataRow dr : dtOrgWgrData.Rows) {

                //region GrrDet

                BGoodReceiptReceiveWithPackingInfoParam.GrrWithPackingInfoDetObj det = sheet.new GrrWithPackingInfoDetObj();
                det.setLotId(dr.getValue("LOT_ID").toString().trim());
                det.setItemId(dr.getValue("ITEM_ID").toString().trim());
                det.setQty(dr.getValue("QTY").toString().trim());
                //det.setUom("");
                det.setMfgDate(dr.getValue("MFG_DATE").toString().trim().substring(0,10) + " 00:00:00");
                det.setExpDate(dr.getValue("EXP_DATE").toString().trim().substring(0,10) + " 23:59:59");
                det.setLotCode(dr.getValue("LOT_CODE").toString().trim());
                det.setSizeId(dr.getValue("SIZE_ID").toString().trim());
                det.setSkipQc(ItemSkipQcOfAllLot.get(dr.getValue("ITEM_ID").toString().trim()));
                det.setPo(dr.getValue("PO_NO").toString().trim());
                det.setPoSeq(dr.getValue("PO_SEQ").toString().trim());
                det.setBox1Id(dr.getValue("BOX1_ID").toString().trim());
                det.setBox2Id(dr.getValue("BOX2_ID").toString().trim());
                det.setBox3Id(dr.getValue("BOX3_ID").toString().trim());
                det.setPalletId(dr.getValue("PALLET_ID").toString().trim());

                det.setCmt(dr.getValue("CMT").toString().trim());
                //det.setVendorItemId("");

                if (radioBarcode.isChecked() && !BarCode.getText().toString().trim().equals(""))
                    det.setRecBarCode(BarCode.getText().toString().trim());
                else if (radioQRcode.isChecked() && !BarCode.getText().toString().trim().equals(""))
                    det.setRecQRCode(BarCode.getText().toString().trim());

                //region GrrDetSn
                if (dtSN != null && _GrrDetSNRerKey != null && _GrrDetSNRerKey.length > 0) // !_GrrDetSNRerKey.equals("")
                {
                    ArrayList<BGoodReceiptReceiveWithPackingInfoParam.GrrWithPackingInfoSnObj> lstSn = new ArrayList<BGoodReceiptReceiveWithPackingInfoParam.GrrWithPackingInfoSnObj>();
                    for (DataRow sn : dtSN.Rows)
                    {
                        if (sn.getValue("GRR_DET_SN_REF_KEY").toString().equals(dr.getValue("GRR_DET_SN_REF_KEY").toString())) {
                            BGoodReceiptReceiveWithPackingInfoParam.GrrWithPackingInfoSnObj grSn = sheet.new GrrWithPackingInfoSnObj();
                            grSn.setSnId(sn.getValue("SN_ID").toString());
                            lstSn.add(grSn);
                        }
                    }
                    det.setGrSns(lstSn);
                }
                //endregion

                detList.add(det);
                // endregion

            }

        } else {

            //region GrrDet

            BGoodReceiptReceiveWithPackingInfoParam.GrrWithPackingInfoDetObj det = sheet.new GrrWithPackingInfoDetObj();
            det.setLotId(LotId.getText().toString().trim());
            det.setItemId(cmbItem.getSelectedItem().toString());
            det.setQty(Qty.getText().toString().trim());
            det.setUom(Uom.getText().toString().trim());
            det.setMfgDate(MfgDate.getText().toString().trim().substring(0,10) + " 00:00:00");
            det.setExpDate(ExpDate.getText().toString().trim().substring(0,10) + " 23:59:59");
            det.setLotCode(LotCode.getText().toString().trim());
            det.setSizeId(cmbSize.getSelectedItem().toString());
            det.setSkipQc(ItemSkipQcOfAllLot.get(cmbItem.getSelectedItem().toString()));

            det.setCmt(Cmt.getText().toString().trim());
            det.setVendorItemId(_VendorItemId);

            if (radioBarcode.isChecked() && !BarCode.getText().toString().trim().equals(""))
                det.setRecBarCode(BarCode.getText().toString().trim());
            else if (radioQRcode.isChecked() && !BarCode.getText().toString().trim().equals(""))
                det.setRecQRCode(BarCode.getText().toString().trim());

            //region GrrDetSn
            if (dtSN != null && _GrrDetSNRerKey != null && _GrrDetSNRerKey.length > 0) // !_GrrDetSNRerKey.equals("")
            {
                ArrayList<BGoodReceiptReceiveWithPackingInfoParam.GrrWithPackingInfoSnObj> lstSn = new ArrayList<BGoodReceiptReceiveWithPackingInfoParam.GrrWithPackingInfoSnObj>();
                for (DataRow sn : dtSN.Rows)
                {
                    BGoodReceiptReceiveWithPackingInfoParam.GrrWithPackingInfoSnObj grSn = sheet.new GrrWithPackingInfoSnObj();
                    grSn.setSnId(sn.getValue("SN_ID").toString());
                    lstSn.add(grSn);
                }
                det.setGrSns(lstSn);
            }
            //endregion

            detList.add(det);
            // endregion
        }

        sheet1.setGrDetails(detList);
        //endregion

        // region Set BIModule
        // List of BIModules
        List<BModuleObject> bmObjs = new ArrayList<BModuleObject>();

        // 條碼拆解
        BModuleObject biObj1 = new BModuleObject();
        biObj1.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIGoodReceiptReceivePortal");
        biObj1.setModuleID("BIDistributeReceipt");
        biObj1.setRequestID("BIDistributeReceipt");
        biObj1.params = new Vector<>();

        // Input param
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIGoodReceiptReceivePortalParam.GrId);
        param1.setParameterValue(GrMstTable.Rows.get(0).getValue("GR_ID").toString());
        biObj1.params.add(param1);

        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.INV.Parameter.GrrWithPackingInfoMasterObj","bmWMS.INV.Param"); // BGoodReceiptReceiveEditParam.GrMasterObj
        MesClass mesClassEnum = new MesClass(vListEnum);
        String strGrrMstObj = mesClassEnum.generateFinalCode(sheet1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIGoodReceiptReceivePortalParam.GrrMasterObj);
        param2.setNetParameterValue(strGrrMstObj);
        biObj1.params.add(param2);

        bmObjs.add(biObj1);
        // endregion

        ((GoodReceiptReceiveDetailNewActivity)getActivity()).CallBIModule(bmObjs, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (((GoodReceiptReceiveDetailNewActivity)getActivity()).CheckBModuleReturnInfo(bModuleReturn)) {
                    //WAPG009007    作業成功!
                    ((GoodReceiptReceiveDetailNewActivity) getActivity()).ShowMessage(R.string.WAPG009007);

                    ClearData(true);
                }
            }
        });
    }

    private void ClearData(boolean remainSkuLevel) {
        BarCode.setText("");
        LotId.setText("");
        cmbItem.setSelection(alItem.size() - 1, true);
        Qty.setText("");
        Uom.setText("");
        MfgDate.setText("");
        ExpDate.setText("");
        LotCode.setText("");
        cmbSize.setSelection(alSize.size() - 1, true);
        Cmt.setText("");
        _GrrDetSNRerKey = null;
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
        dtSN = new DataTable();
        //dtWgrData = new DataTable();
        dtWgrDataGroup = new DataTable();
        dtOrgWgrData = new DataTable();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        GoodReceiptReceiveExtendGridAdapter adapter = new GoodReceiptReceiveExtendGridAdapter(_dtExtendData, inflater);
        lvExtent.setAdapter(adapter);
    }

    private void setEnable(boolean enable, boolean qrscanEnable) {
        LotId.setEnabled(enable);
        IbtnGrLotGridLotIdQRScan.setEnabled(enable);
        cmbItem.setEnabled(enable);
        Qty.setEnabled(enable);
        Uom.setEnabled(enable);
        MfgDate.setEnabled(enable);
        ExpDate.setEnabled(enable);
        LotCode.setEnabled(enable);
        cmbSize.setEnabled(enable);
        Cmt.setEnabled(enable);
        btnMfgDateClear.setEnabled(enable);
        btnExpDateClear.setEnabled(enable);

        radioBarcode.setEnabled(qrscanEnable);
        radioQRcode.setEnabled(qrscanEnable);
        IbtnGrBarCodeQRScan.setEnabled(qrscanEnable);

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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
