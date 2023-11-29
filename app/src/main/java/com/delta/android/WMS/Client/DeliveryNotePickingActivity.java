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
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
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
import com.delta.android.WMS.Param.BIFetchProcessSheetParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.delta.android.WMS.Client.GridAdapter.DeliveryNoteMstGridAdapter;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class DeliveryNotePickingActivity extends BaseFlowActivity {

    // private variable
    ViewHolder holder = null;
    int checkedCount = 0;// 計算要執行的單據數量
    DataTable SheetMstTable = null;
    HashMap<String, DataTable> SheetDetTables;// <Mst ID, Detail Table>
    String confirmName = null;// btnName
    String pickSheetId = "";
    String dnId = null;
    List<? extends Map<String, Object>> lstPickSheetId;
    HashMap<String, String> mapSheet = new HashMap<>();

    static class ViewHolder {
        // 宣告控制項物件
        EditText FromDate;
        EditText ToDate;
        ImageButton IbtnSheetIdQRScan;
        Spinner cmbPickSheetId;
        //EditText SheetId;
        ListView MasterSheetData;
        ImageButton IbtnSearch;
        Button BtnConfirm;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_delivery_note_picking);

        this.InitialSetup();
        // 設定監聽事件
        setListensers();
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//        if (result != null) {
//            if (result.getContents() == null) {
//                Toast.makeText(this, getResources().getText(R.string.QRCODE_SCAN_CANCEL).toString(), Toast.LENGTH_SHORT).show();
//            } else {
//                holder.SheetId.setText(result.getContents().trim());
//            }
//        } else {
//            super.onActivityResult(requestCode, resultCode, data);
//        }
//    }

    private void InitialSetup() {
        if (holder == null) {
            holder = new ViewHolder();
            // 取得控制項物件
            holder.FromDate = findViewById(R.id.etFromDate);
            holder.ToDate = findViewById(R.id.etToDate);
            holder.MasterSheetData = findViewById(R.id.lvMasterSheet);
            holder.cmbPickSheetId = findViewById(R.id.cmbSheetId);
//            holder.IbtnSheetIdQRScan = findViewById(R.id.ibtnSheetIdQRScan);
//            holder.SheetId = findViewById(R.id.etSheetId);
            holder.IbtnSearch = findViewById(R.id.ibtnSearch);
            holder.BtnConfirm = findViewById(R.id.btnConfirm);
        }
        holder.FromDate.setInputType(InputType.TYPE_NULL);
        holder.ToDate.setInputType(InputType.TYPE_NULL);
        SheetDetTables = new HashMap<>();
        confirmName = holder.BtnConfirm.getText().toString();

        getPickSheetId();
    }

    //設定監聽事件
    private void setListensers() {
        holder.FromDate.setOnClickListener(FromDateOnClick);
        holder.ToDate.setOnClickListener(ToDateOnClick);

        // 並不是在我們點擊EditText的時候觸發，也不是在我們對EditText進行編輯時觸發，而是在我們編輯完之後點擊軟鍵盤上的各種鍵才會觸發。
        //holder.SheetId.setOnEditorActionListener(SheetIdEdit);
//        holder.SheetId.setOnKeyListener(SheetIdOnKey);
//        holder.IbtnSheetIdQRScan.setOnClickListener(SheetIdQRScanClick);
        holder.IbtnSearch.setOnClickListener(IbtnSearchOnClick);
        holder.MasterSheetData.setOnItemClickListener(ListViewonClick);
        holder.BtnConfirm.setOnClickListener(BtnConfrimClick);
        holder.cmbPickSheetId.setOnItemSelectedListener(cmbPicSheetIdOnClick);
    }

    // region 事件

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
//        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                //日期格式
//                StringBuffer sb = new StringBuffer();
//                sb.append(String.format("%d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth()));
//                holder.FromDate.setText(sb);
//                dialog.cancel();
//            }
//        });
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
//        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                //日期格式
//                StringBuffer sb = new StringBuffer();
//                sb.append(String.format("%d-%02d-%02d", datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth()));
//                holder.ToDate.setText(sb);
//                dialog.cancel();
//            }
//        });
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

    private View.OnClickListener SheetIdQRScanClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            IntentIntegrator integrator = new IntentIntegrator(DeliveryNotePickingActivity.this);
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

    private View.OnClickListener IbtnSearchOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            GetMaster();
        }
    };

    private AdapterView.OnItemClickListener ListViewonClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            int totalCount = holder.MasterSheetData.getCount();
            CheckBox cb = view.findViewById(R.id.cbSheetMstGridSheet);
            cb.setChecked(!cb.isChecked());
            String checkSheet;
            if (cb.isChecked()) {
                checkSheet = "true";
                checkedCount++;
            } else {
                checkSheet = "false";
                checkedCount--;
            }
            SheetMstTable.Rows.get(position).setValue("SELECTED", checkSheet);
            holder.BtnConfirm.setText(String.format("%s(%s)", confirmName, checkedCount + "/" + totalCount));
        }
    };

    private View.OnClickListener BtnConfrimClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (SheetMstTable == null || SheetMstTable.Rows.size() == 0) return;
            ArrayList<String> ids = new ArrayList<String>();
            DataTable dtDNMst = new DataTable();
            for (int i = 0; i < SheetMstTable.Rows.size(); i++) {
                if (SheetMstTable.Rows.get(i).getValue("SELECTED").toString().equals("true")) {
                    ids.add(SheetMstTable.Rows.get(i).getValue("SHEET_ID").toString()); //DN_ID
                    dtDNMst.Rows.add(SheetMstTable.Rows.get(i));
                }
            }

            if (ids.size() == 0) {
                ShowMessage(R.string.WAPG006011);//未選取任何單據資料
                return;
            }

            Bundle sheetData = new Bundle();
            DataTable dtDNDet = new DataTable();
            for (String id : ids) {
                if (!SheetDetTables.containsKey(id)) continue;
                for (DataRow dr : SheetDetTables.get(id).Rows) {
                    dtDNDet.Rows.add(dr);
                }
            }
            sheetData.putStringArrayList("DNIDs", ids);      // 要執行的單據ID
            sheetData.putSerializable("DNDet", dtDNDet);// 要執行的單據Detail
            sheetData.putSerializable("DNMst", dtDNMst);// 要執行的單據Mst
            gotoNextActivity(DeliveryNotePickingDetailNewActivity.class, sheetData); // DeliveryNotePickingDetailActivity
        }
    };

    private Spinner.OnItemSelectedListener cmbPicSheetIdOnClick = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {

            pickSheetId = "";
            dnId = "";

            if (position != lstPickSheetId.size()-1) {
                Map<String, String> pickSheetIdMap = (Map<String, String>)parent.getItemAtPosition(position);
                pickSheetId = pickSheetIdMap.get("SHEET_ID");
                dnId = pickSheetIdMap.get("DN_ID");
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            return;
        }
    };
    // endregion

    private void GetMaster() {

        //region Check Input
        int compare = holder.FromDate.getText().toString().compareTo(holder.ToDate.getText().toString());
        if (holder.FromDate.getText().toString().equals("") && holder.ToDate.getText().toString().equals("") && pickSheetId.length() <= 0) { //holder.SheetId.getText().toString().equals("")
            ShowMessage(R.string.WAPG006001);//請輸入至少一個條件
            return;
        } else if ((holder.FromDate.getText().toString().equals("") || holder.ToDate.getText().toString().equals("")) && pickSheetId.length() <= 0) { //holder.SheetId.getText().toString().equals("")
            ShowMessage(R.string.WAPG006002);//請選擇起始日期和到達日期
            return;
        } else if (compare == 1) {
            ShowMessage(R.string.WAPG006003);//起始日期不能大於到達日期
            return;
        }
        // endregion

        //region Set BIModule
        // BIModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchDeliveryNoteAndPickSheetMaintain");
        bmObj.setRequestID("FetchDeliveryNoteAndPickSheetMaintain");
        bmObj.params = new Vector<ParameterInfo>();

        // 裝Condition的容器
        HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
        List<Condition> lstCondition1 = new ArrayList<Condition>();
        List<Condition> lstCondition2 = new ArrayList<Condition>();
        List<Condition> lstCondition3 = new ArrayList<Condition>();

        // region Set Condition
        // SHEET_ID
        if (pickSheetId.length() > 0) { //!holder.SheetId.getText().toString().equals("")
            Condition conditionSheetId = new Condition();
            conditionSheetId.setAliasTable("P");
            conditionSheetId.setColumnName("SHEET_ID");
            conditionSheetId.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
            conditionSheetId.setValue(pickSheetId); //20200729 archie 轉大寫 // holder.SheetId.getText().toString().toUpperCase().trim()
            lstCondition1.add(conditionSheetId);
            mapCondition.put(conditionSheetId.getColumnName(), lstCondition1);
        }
        // SHEET_STATUS
        Condition conditionSheetStatus = new Condition();
        conditionSheetStatus.setAliasTable("P");
        conditionSheetStatus.setColumnName("SHEET_STATUS");
        conditionSheetStatus.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
        conditionSheetStatus.setValue("Confirmed");
        lstCondition2.add(conditionSheetStatus);
        mapCondition.put(conditionSheetStatus.getColumnName(), lstCondition2);

        if (!holder.FromDate.getText().toString().equals("") && !holder.ToDate.getText().toString().equals("")) {
            // 開立單據時間區間
            Condition conditionCreateDate = new Condition();
            conditionCreateDate.setAliasTable("P");
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
                    DataTable dtMaster = bModuleReturn.getReturnJsonTables().get("FetchDeliveryNoteAndPickSheetMaintain").get("PickMst");
                    DataTable dtDetail = bModuleReturn.getReturnJsonTables().get("FetchDeliveryNoteAndPickSheetMaintain").get("PickDet");
                    Boolean f = true; // 判斷是否Mst已經有輸入的ID

                    if (dtMaster.Rows.size() <= 0) {
                        ShowMessage(R.string.WAPG006004);// 查詢無資料
                        return;
                    }
                    // Input Mst Data
                    if (pickSheetId.length() > 0 && !(SheetMstTable == null || SheetMstTable.Rows.size() == 0))   // 有輸入ID則從原本的MstTable加一列Mst //!holder.SheetId.getText().toString().equals("")
                    {

                        for (DataRow drOld : SheetMstTable.Rows) {
                            if (drOld.getValue("SHEET_ID").toString().equals(dtMaster.Rows.get(0).getValue("SHEET_ID").toString()))
                                f = false;
                        }
                        if (f) SheetMstTable.Rows.add(dtMaster.Rows.get(0));
                    } else {                                                 // 沒有有輸入ID則直接替換Mst
                        SheetMstTable = dtMaster;
                    }

                    // Input Det Data
                    if (pickSheetId.length() > 0 && !(SheetMstTable == null || SheetMstTable.Rows.size() == 0)) //!holder.SheetId.getText().toString().equals("")
                    // 有輸入ID,則從原本的DetTables加一筆(k,v)。v為此單據的items table
                    {
                        if (f)
                            SheetDetTables.put(dtMaster.Rows.get(0).getValue("SHEET_ID").toString(), dtDetail);
                    } else {                                                 // 沒有有輸入ID,則清空後全部加入
                        SheetDetTables.clear();
                        for (DataRow drNew : dtMaster.Rows) {
                            DataTable dt = new DataTable();
                            for (DataRow drNewDet : dtDetail.Rows) {
                                if (drNew.getValue("SHEET_ID").toString().equals(drNewDet.getValue("SHEET_ID").toString())) {
                                    dt.Rows.add(drNewDet);
                                }
                            }
                            SheetDetTables.put(drNew.getValue("SHEET_ID").toString(), dt);
                        }
                    }

                    // checkbox用
                    //for (DataRow dr : SheetMstTable.Rows) dr.setValue("SELECTED", "true");

                    // Input ListView
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE); // 加載layout至此layout上
                    DeliveryNoteMstGridAdapter adapter = new DeliveryNoteMstGridAdapter(SheetMstTable, inflater);
                    holder.MasterSheetData.setAdapter(adapter);
                    checkedCount = SheetMstTable.Rows.size();
                    checkedCount = 0;
                    for (DataRow dr : SheetMstTable.Rows)
                    {
                        if(dr.getValue("SELECTED").toString().equals("true"))
                            checkedCount++;
                    }
                    holder.BtnConfirm.setText(String.format("%s(%s)", confirmName, checkedCount + "/" + SheetMstTable.Rows.size()));
                    holder.cmbPickSheetId.setSelection(lstPickSheetId.size()-1);
                    //holder.SheetId.setText("");
                }
            }
        });
    }

    private void getPickSheetId() {

        // Call BIModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIFetchProcessSheet");
        bmObj.setModuleID("FetchPickSheetBySheetType");
        bmObj.setRequestID("FetchPickSheetBySheetType");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIFetchProcessSheetParam.SheetTypePolicyId);
        param1.setParameterValue("DeliveryNote");
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

                    DataTable dtPickSheetId = bModuleReturn.getReturnJsonTables().get("FetchPickSheetBySheetType").get("DATA");

                    DataRow drDefaultItem = dtPickSheetId.newRow();
                    // 下拉選單預設選項依語系調整
                    String strSelectSheetId = getResString(getResources().getString(R.string.SELECT_SHEET_ID));
                    drDefaultItem.setValue("SHEET_ID", strSelectSheetId); // 請選擇單據代碼

                    // region -- 揀料單據代碼 --
                    if (dtPickSheetId != null && dtPickSheetId.Rows.size() > 0)
                        dtPickSheetId.Rows.add(drDefaultItem);
                    lstPickSheetId = (List<? extends Map<String, Object>>) dtPickSheetId.toListHashMap();
                    SimpleAdapter adapterShtId = new SimpleArrayAdapter<>(DeliveryNotePickingActivity.this, lstPickSheetId, android.R.layout.simple_spinner_item, new String[]{"SHEET_ID", "DN_ID"}, new int[]{android.R.id.text1, 0});
                    adapterShtId.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    holder.cmbPickSheetId.setAdapter(adapterShtId);
                    holder.cmbPickSheetId.setSelection(lstPickSheetId.size()-1, true);
                    // endregion

//                    int count = 0;
//                    for (DataRow dr : dt.Rows) {
//                        lstSheetId.add(count, dr.getValue("SHEET_ID").toString());
//                        count++;
//                    }
//                    Collections.sort(lstSheetId); // List依據字母順序排序
//
//                    // 下拉選單預設選項依語系調整
//                    String strSelectSheetId = getResString(getResources().getString(R.string.SELECT_SHEET_ID));
//                    lstSheetId.add(strSelectSheetId);
//
//                    SimpleArrayAdapter adapter = new DeliveryNoteShipWorkActivity.SimpleArrayAdapter<>(DeliveryNoteShipWorkActivity.this, android.R.layout.simple_spinner_dropdown_item, lstSheetId);
//                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                    holder.cmbDNId.setAdapter(adapter);
//                    holder.cmbDNId.setSelection(lstSheetId.size() - 1, true);
                }
            }
        });
    }

    public void onClickFromDateClear(View v) {
        holder.FromDate.setText("");
    }

    public void onClickToDateClear(View v) {
        holder.ToDate.setText("");
    }

    private class SimpleArrayAdapter<T> extends SimpleAdapter {

        public SimpleArrayAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        //複寫這個方法，使提示字改為灰色
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = super.getView(position, convertView, parent);

            if ( position == getCount() ){
                HashMap<String, String> rowData = (HashMap) getItem(getCount());
                ((TextView)v.findViewById(android.R.id.text1)).setText("");
                ((TextView)v.findViewById(android.R.id.text1)).setHint(rowData.get("SHEET_ID"));
            }

            return v;
        }

        //複寫這個方法，使返回的數據沒有最後一項
        @Override
        public int getCount() {
            // don't display last item. It is used as hint.
            int count = super.getCount();
            return count > 0 ? count - 1 : count;
        }
    }
}
