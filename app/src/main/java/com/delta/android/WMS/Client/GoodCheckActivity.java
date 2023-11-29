package com.delta.android.WMS.Client;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

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
import com.delta.android.WMS.Param.BIFetchProcessSheetParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.delta.android.WMS.Client.GridAdapter.CheckDetGridAdapter;
import com.delta.android.WMS.Param.BCountMstChangeCheckedStatusParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GoodCheckActivity extends BaseFlowActivity {

    //Function ID = WAPG003

    ViewHolder holder = null;
    DataTable nonCheckMstData = null;
    DataTable nonCheckDetData = null;
    ArrayList<String> lstSheetId = new ArrayList<>();

    static class ViewHolder {
        Spinner SheetId;
//        EditText CheckId;
        //EditText BinId;
        //EditText ItemId;
        ListView NoneCheckList;
        ImageButton IbtnSearch;
        //Button StartChecked;
        //Button EndChecked;
        //TextView CountStatus;
        //FloatingActionButton FabAddNew;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_good_check);
        this.initialData();
        this.GetSheetId();
    }

    private void initialData() {
        if (holder == null) {
            holder = new ViewHolder();
            holder.SheetId = findViewById(R.id.cmbSheetId);
//            holder.CheckId = findViewById(R.id.etCheckId);
            //holder.BinId = findViewById(R.id.etCheckBinId);
            //holder.ItemId = findViewById(R.id.etCheckItemId);
            holder.NoneCheckList = findViewById(R.id.lvNoneCheckedData);
            //holder.StartChecked = findViewById(R.id.btStartCheck);
            //holder.EndChecked = findViewById(R.id.btEndCheck);
//            holder.FabAddNew = findViewById(R.id.fabCheckAddNewItem);
            //holder.CountStatus = findViewById(R.id.tvCheckCountStatus);
            //holder.StartChecked.setEnabled(false);
            //holder.EndChecked.setEnabled(false);
            //holder.FabAddNew.setEnabled(false);
            holder.IbtnSearch = findViewById(R.id.ibtnSearch);
        }

        /*
        holder.CheckId.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    FetchCheckSheetInformation();
                    manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
        */

        holder.IbtnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FetchCheckSheetInformation();
            }
        });

//
//        holder.BinId.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
//                if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    FilterCheckSheetInformation();
//                    manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                    return true;
//                }
//                return false;
//            }
//        });
//        holder.ItemId.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
//                if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    FilterCheckSheetInformation();
//                    manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                    return true;
//                }
//                return false;
//            }
//        });

        //region 用一般的Enter觸發
//        holder.CheckId.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (event == null || event.getAction() != KeyEvent.ACTION_DOWN) return false;
//                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                FetchCheckSheetInformation();
//                manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                return false;
//            }
//        });
//        holder.BinId.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (event == null || event.getAction() != KeyEvent.ACTION_DOWN) return false;
//                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                FilterCheckSheetInformation();
//                return false;
//            }
//        });
//        holder.ItemId.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (event == null || event.getAction() != KeyEvent.ACTION_DOWN) return false;
//                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
//                FilterCheckSheetInformation();
//                return false;
//            }
//        });
        //endregion

        //holder.NoneCheckList.setEnabled(false);
    }

    private void GetSheetId() {
        // Call BIModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIFetchProcessSheet");
        bmObj.setModuleID("FetchProcessSheetByStatus");
        bmObj.setRequestID("FetchProcessSheetByStatus");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIFetchProcessSheetParam.ProcessType);
        param1.setParameterValue("COUNT");
        bmObj.params.add(param1);

        List<String> lstStatus = new ArrayList<>();
        lstStatus.add("Confirmed");
        lstStatus.add("FirstChecked");
        lstStatus.add("SecondChecked");
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
                    String strSelectCountId = getResString(getResources().getString(R.string.SELECT_COUNT_ID));
                    lstSheetId.add(strSelectCountId);

                    SimpleArrayAdapter adapter = new GoodCheckActivity.SimpleArrayAdapter<>(GoodCheckActivity.this, android.R.layout.simple_spinner_dropdown_item, lstSheetId);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    holder.SheetId.setAdapter(adapter);
                    holder.SheetId.setSelection(lstSheetId.size() - 1, true);
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

    private void FetchCheckSheetInformation() {

        /*
        if (holder.CheckId.getText().toString().equals("")) {
            ShowMessage(R.string.WAPG003008);//請輸入盤點單代碼
            return;
        }
        */

        int sheetIdIndex = holder.SheetId.getSelectedItemPosition();
        if (sheetIdIndex == (lstSheetId.size() - 1)) {
            ShowMessage(R.string.WAPG003008); //WAPG003008 請選擇盤點單代碼
            return;
        }

        BModuleObject biObj = new BModuleObject();
        biObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        biObj.setModuleID("BIFetchCountMstAndDet");
        biObj.setRequestID("BIFetchCount");
        biObj.params = new Vector<>();
        VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");

        List<Condition> conditionM1s = new ArrayList<>();
        List<Condition> conditionM2s = new ArrayList<>();
        HashMap<String, List<?>> dicCondition = new HashMap<>();

        Condition condId = new Condition();
        condId.setAliasTable("M");
        condId.setColumnName("COUNT_ID");
        condId.setDataType("string");
//        condId.setValue(holder.CheckId.getText().toString().toUpperCase().trim()); //20200729 archie 轉大寫
        condId.setValue(holder.SheetId.getSelectedItem().toString().toUpperCase().trim());
        conditionM1s.add(condId);
        dicCondition.put("COUNT_ID", conditionM1s);

        Condition condConfirmStatus = new Condition();
        condConfirmStatus.setAliasTable("M");
        condConfirmStatus.setColumnName("COUNT_STATUS");
        condConfirmStatus.setDataType("string");
        condConfirmStatus.setValue("Confirmed");
        conditionM2s.add(condConfirmStatus);
        dicCondition.put("COUNT_STATUS", conditionM2s);

        Condition condFirstCheckStatus = new Condition();
        condFirstCheckStatus.setAliasTable("M");
        condFirstCheckStatus.setColumnName("COUNT_STATUS");
        condFirstCheckStatus.setDataType("string");
        condFirstCheckStatus.setValue("FirstChecked");
        conditionM2s.add(condFirstCheckStatus);
        dicCondition.put("COUNT_STATUS", conditionM2s);

        Condition condSecondCheckStatus = new Condition();
        condSecondCheckStatus.setAliasTable("M");
        condSecondCheckStatus.setColumnName("COUNT_STATUS");
        condSecondCheckStatus.setDataType("string");
        condSecondCheckStatus.setValue("SecondChecked");
        conditionM2s.add(condSecondCheckStatus);
        dicCondition.put("COUNT_STATUS", conditionM2s);

        Condition condClosedStatus = new Condition();
        condClosedStatus.setAliasTable("M");
        condClosedStatus.setColumnName("COUNT_STATUS");
        condClosedStatus.setDataType("string");
        condClosedStatus.setValue("Closed");
        conditionM2s.add(condClosedStatus);
        dicCondition.put("COUNT_STATUS", conditionM2s);

        if (dicCondition.size() != 0) {
            MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vKey, vVal);
            String serializedObj = msdl.generateFinalCode(dicCondition);
            ParameterInfo param1 = new ParameterInfo();
            param1.setParameterID(BIWMSFetchInfoParam.Condition);
            param1.setNetParameterValue(serializedObj);
            biObj.params.add(param1);
        }

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIWMSFetchInfoParam.OnlyTableColumn);
        param2.setParameterValue("N");
        biObj.params.add(param2);

        CallBIModule(biObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    nonCheckMstData = bModuleReturn.getReturnJsonTables().get("BIFetchCount").get("Mst");
                    nonCheckDetData = bModuleReturn.getReturnJsonTables().get("BIFetchCount").get("Det");
                    if (nonCheckMstData == null || nonCheckMstData.Rows.size() == 0) {
                        Object[] args = new Object[1];
//                        args[0] = holder.CheckId.getText().toString();
                        args[0] = holder.SheetId.getSelectedItem().toString().toUpperCase().trim();
                        //單據[%s]不為Confirmed狀態!
                        ShowMessage(R.string.WAPG003001, args);

                        //holder.StartChecked.setEnabled(false);
                        //holder.CountStatus.setText("");
                        holder.NoneCheckList.setAdapter(null);
                        return;
                    }
                    if (nonCheckDetData == null || nonCheckDetData.Rows.size() == 0) {
                        Object[] args = new Object[1];
//                        args[0] = holder.CheckId.getText().toString();
                        args[0] = holder.SheetId.getSelectedItem().toString().toUpperCase().trim();
                        //單據[%s]目前沒有任何盤點項目!
                        ShowMessage(R.string.WAPG003002, args);
                        //holder.StartChecked.setEnabled(false);
                        return;
                    }
                    if (nonCheckMstData.Rows.get(0).getValue("COUNT_STATUS").toString().equals("FirstChecked") ||
                            nonCheckMstData.Rows.get(0).getValue("COUNT_STATUS").toString().equals("SecondChecked")) {
                        //holder.StartChecked.setEnabled(false);
                        //holder.EndChecked.setEnabled(true);
                        //holder.NoneCheckList.setEnabled(true);
                        //holder.FabAddNew.setEnabled(true);
                    } else {
                        //holder.StartChecked.setEnabled(true);
                        //holder.FabAddNew.setEnabled(false);
                    }
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    CheckDetGridAdapter adapter = new CheckDetGridAdapter(nonCheckDetData, inflater);
                    holder.NoneCheckList.setAdapter(adapter);
                    holder.NoneCheckList.setOnItemClickListener(onClickListView);
                    //holder.CountStatus.setText(nonCheckMstData.Rows.get(0).getValue("COUNT_STATUS").toString());
                    return;
                }
                //holder.StartChecked.setEnabled(false);
            }
        });
    }
//
//    private void FilterCheckSheetInformation() {
//        if (nonCheckDetData == null || nonCheckDetData.Rows.size() == 0) return;
//        boolean haveFilter = false;
//        DataTable dtFilter = new DataTable();
//        if (!holder.BinId.getText().toString().equals("")) {
//            haveFilter = true;
//            for (DataRow dr : nonCheckDetData.Rows) {
//                if (dr.getValue("BIN_ID").toString().equals(holder.BinId.getText().toString())) {
//                    dtFilter.Rows.add(dr);
//                }
//            }
//        }
//        if (!holder.ItemId.getText().toString().equals("")) {
//            haveFilter = true;
//            if (dtFilter.Rows.size() == 0) {
//                for (DataRow dr : nonCheckDetData.Rows) {
//                    if (dr.getValue("ITEM_ID").toString().equals(holder.ItemId.getText().toString())) {
//                        dtFilter.Rows.add(dr);
//                    }
//                }
//            } else {
//                for (DataRow dr : dtFilter.Rows) {
//                    if (dr.getValue("ITEM_ID").toString().equals(holder.ItemId.getText().toString())) {
//                        dtFilter.Rows.add(dr);
//                    }
//                }
//            }
//        }
//
//        if (!haveFilter) dtFilter = nonCheckDetData;
//        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        CheckDetGridAdapter adapter = new CheckDetGridAdapter(dtFilter, inflater);
//        holder.NoneCheckList.setAdapter(adapter);
//        holder.NoneCheckList.setOnItemClickListener(onClickListView);
//    }

    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Bundle checkData = new Bundle();
            DataTable chooseData = new DataTable();
            chooseData.Rows.add(nonCheckDetData.Rows.get(position));
            checkData.putSerializable("CheckDet", chooseData);
            checkData.putSerializable("CheckMst", nonCheckMstData);
            checkData.putString("CountId", nonCheckMstData.Rows.get(0).getValue("COUNT_ID").toString());
            checkData.putString("CountKey", nonCheckMstData.Rows.get(0).getValue("COUNT_KEY").toString());
            gotoNextActivity(GoodCheckExecuteActivity.class, checkData);
        }
    };

    public void onClickAddNewItem(View v) {
        Bundle checkData = new Bundle();
        DataTable chooseData = new DataTable();
        DataRow selectRow = chooseData.newRow();
        selectRow.put("BIN_ID", "");
        selectRow.put("ITEM_ID", "");
        selectRow.put("INVENTORY_QTY", "");
        chooseData.Rows.add(selectRow);
        checkData.putSerializable("CheckDet", chooseData);
        checkData.putString("CountId", nonCheckMstData.Rows.get(0).getValue("COUNT_ID").toString());
        gotoNextActivity(GoodCheckExecuteActivity.class, checkData);
    }

//    public void onClickStartCheck(View v) {
//        changeCountStatus(true);
//    }
//
//    public void onClickEndCheck(View v) {
//        changeCountStatus(false);
//        holder.CheckId.setText("");
//        holder.BinId.setText("");
//        holder.ItemId.setText("");
//        holder.CountStatus.setText("");
//        holder.StartChecked.setEnabled(false);
//        holder.EndChecked.setEnabled(false);
//        holder.NoneCheckList.setAdapter(null);
//        holder.NoneCheckList.setEnabled(false);
//    }

//    private void changeCountStatus(final boolean isStart) {
//        if (nonCheckMstData == null || nonCheckMstData.Rows.size() == 0) return;
//        BModuleObject bmObj = new BModuleObject();
//        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.BCountMstChangeCheckedStatus");
//        bmObj.setModuleID("");
//        bmObj.setRequestID("BGoodCount");
//        bmObj.params = new Vector<>();
//
//        ParameterInfo param1 = new ParameterInfo();
//        param1.setParameterID(BCountMstChangeCheckedStatusParam.CountId);
//        param1.setParameterValue(nonCheckMstData.Rows.get(0).getValue("COUNT_ID").toString());
//        bmObj.params.add(param1);
//
//        ParameterInfo param2 = new ParameterInfo();
//        param2.setParameterID(BCountMstChangeCheckedStatusParam.CountStatus);
//        if (isStart) {
//            param2.setParameterValue("Y");
//        } else {
//            param2.setParameterValue("N");
//        }
//        bmObj.params.add(param2);
//        CallBModule(bmObj, new WebAPIClientEvent() {
//            @Override
//            public void onPostBack(BModuleReturn bModuleReturn) {
//                if (CheckBModuleReturnInfo(bModuleReturn)) {
//                    if (isStart) {
//                        //開始盤點
//                        ShowMessage(R.string.WAPG003003);
//                        if (nonCheckMstData.Rows.get(0).getValue("FIRST_COUNT_CLOSE_DATE").toString().equals("")) {
//                            holder.CountStatus.setText("FirstChecked");
//                        } else {
//                            holder.CountStatus.setText("SecondChecked");
//                        }
//                        holder.NoneCheckList.setEnabled(true);
//                        holder.StartChecked.setEnabled(false);
//                        holder.EndChecked.setEnabled(true);
//                        //holder.FabAddNew.setEnabled(true);
//                    } else {
//                        //結束盤點
//                        ShowMessage(R.string.WAPG003004);
//                        holder.NoneCheckList.setEnabled(false);
//                        holder.EndChecked.setEnabled(false);
//                        holder.StartChecked.setEnabled(true);
//                        //holder.FabAddNew.setEnabled(false);
//                    }
//                }
//            }
//        });
//    }
}
