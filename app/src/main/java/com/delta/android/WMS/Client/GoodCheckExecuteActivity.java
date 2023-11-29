package com.delta.android.WMS.Client;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.Activity.ShowMessageEvent;
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
import com.delta.android.WMS.Client.GridAdapter.CheckHisGridAdapter;
import com.delta.android.WMS.Param.BCountMstChangeCheckedStatusParam;
import com.delta.android.WMS.Param.BExecuteCountParam;
import com.delta.android.WMS.Param.BExecuteCountDetCloseParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.delta.android.WMS.Param.ParamObj.CountInvObj;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class GoodCheckExecuteActivity extends BaseFlowActivity {

    ViewHolder holder = null;
    DataRow chooseDetRow;
    DataRow mstRow;
    DataTable alreadyCountedDatatable;
    boolean isCycle;
    boolean displayInv;
    boolean isItemID = false;//註冊類別,是否為ItemID

    static class ViewHolder {
        //EditText ItemId;
        EditText BinId;
        EditText RegisterId;
        EditText RegisterQty;
        EditText Qty;
        //Button Confirm;
        TextView CheckId;
        TextView CheckCountStatus;
        TextView DetItemId;
        TextView DetBinId;
        Button StartCheck;
        Button EndCheck;
        ListView ShowHisCount;
        TabHost CountTabHost;
        Button CheckExecute;
        Button CreateCount;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_good_check_execute);
        this.initialData();
    }

    private void initialData() {
        if (holder == null) {
            holder = new ViewHolder();
            //holder.ItemId = findViewById(R.id.etCheckExecuteItemId);
            holder.RegisterId = findViewById(R.id.etCheckExecuteRegisterId);
            holder.RegisterQty = findViewById(R.id.etCheckRegisterQty);
            holder.Qty = findViewById(R.id.etCheckExecuteQty);
            //holder.Confirm = findViewById(R.id.btCheckExecuteConfirm);
            holder.CheckId = findViewById(R.id.tvCheckId);
            holder.CheckCountStatus = findViewById(R.id.tvCheckCountStatus);
            holder.StartCheck = findViewById(R.id.btStartCheck);
            holder.EndCheck = findViewById(R.id.btEndCheck);
            holder.BinId = findViewById(R.id.etCheckExecuteBinId);
            holder.ShowHisCount = findViewById((R.id.lvShowHisCount));
            holder.CountTabHost = findViewById(R.id.tabCountHost);
            holder.CheckExecute = findViewById(R.id.btCheckExecute);
            holder.DetItemId = findViewById(R.id.tvDetItemId);
            holder.DetBinId = findViewById(R.id.tvDetBinId);
            holder.CreateCount = findViewById(R.id.btCreateCount);
        }
        DataTable chooseTable = (DataTable) getIntent().getSerializableExtra("CheckDet");
        DataTable mstTable = (DataTable) getIntent().getSerializableExtra("CheckMst");
        holder.CountTabHost.setup();

        TabHost.TabSpec spec1 = holder.CountTabHost.newTabSpec("TabCount");
        View tab1 = LayoutInflater.from(this).inflate(R.layout.activity_wms_good_pick_tab_widget, null);
        TextView tvTab1 = tab1.findViewById(R.id.tvTabText);
        tvTab1.setText(R.string.EXECUTE_COUNT);
        spec1.setIndicator(tab1);
        spec1.setContent(R.id.llEnterCheckCount);
        holder.CountTabHost.addTab(spec1);

        TabHost.TabSpec spec2 = holder.CountTabHost.newTabSpec("TabHisCount");
        View tab2 = LayoutInflater.from(this).inflate(R.layout.activity_wms_good_pick_tab_widget, null);
        TextView tvTab2 = tab2.findViewById(R.id.tvTabText);
        tvTab2.setText(R.string.COUNT_HISTORY);
        spec2.setIndicator(tab2);
        spec2.setContent(R.id.llShowHisCount);
        holder.CountTabHost.addTab(spec2);

        holder.CountTabHost.setCurrentTab(0);

        holder.CountTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (tabId.equals("TabHisCount")) {
                    FetchCountInv();
                }
            }
        });

        mstRow = mstTable.Rows.get(0);
        isCycle = mstRow.getValue("COUNT_KIND").toString().equals("Cycle");
        displayInv = mstRow.getValue("DISPLAY_INVENTORY").toString().equals("Y");
        holder.CheckId.setText(mstRow.getValue("COUNT_ID").toString());
        chooseDetRow = chooseTable.Rows.get(0);
        holder.DetItemId.setText(chooseDetRow.getValue("ITEM_ID").toString());
        holder.DetBinId.setText(chooseDetRow.getValue("BIN_ID").toString());

        //SBRM_WMS_ITEM_PROFILE.REGISTER_TYPE='ItemID' =>只管料號
        if (chooseDetRow.getValue("REGISTER_TYPE").toString().equals("ItemID")) {
            isItemID = true;
            //移除批號控制項
            ((LinearLayout) findViewById(R.id.llEnterCheckCount)).removeView(findViewById(R.id.etCheckExecuteRegisterId));
        }

        //移除控制項
        if (!displayInv) {
            ((LinearLayout) findViewById(R.id.llEnterCheckCount)).removeView(findViewById(R.id.etCheckRegisterQty));
        } else {
            if (isItemID) {
                holder.BinId.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        //只有按下Enter才會反映
                        if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                            getRegisterQty();
                            return true;
                        }
                        return false;
                    }
                });
            } else {
                holder.RegisterId.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        //只有按下Enter才會反映
                        if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                            getRegisterQty();
                            return true;
                        }
                        return false;
                    }
                });
            }

        }

        if (chooseDetRow.getValue("BIN_ID").toString().equals("*")) {
            holder.BinId.setEnabled(true);
        } else {
            holder.BinId.setEnabled(false);
            holder.BinId.setText(chooseDetRow.getValue("BIN_ID").toString());
        }


//        if (!chooseDetRow.getValue("ITEM_ID").toString().equals("")) {
//            holder.ItemId.setText(chooseDetRow.getValue("ITEM_ID").toString());
//            holder.BinId.setText(chooseDetRow.getValue("BIN_ID").toString());
//            holder.ItemId.setEnabled(false);
//            holder.BinId.setEnabled(false);
//        }

        holder.StartCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCountStatus("Y");//開始盤點
            }
        });


        holder.EndCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCountStatus("N");//結束盤點
            }
        });

        holder.CreateCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowDialog();
            }
        });

        changeCountStatus("S");//取得盤點狀態
    }

    private void getRegisterQty() {
        String binId = holder.BinId.getText().toString().trim();
        String regId;

        if (binId.equals("")) {
            ShowMessage(R.string.WAPG003005, new ShowMessageEvent() {
                @Override
                public void onDismiss() {
                    holder.BinId.requestFocus();
                }
            }, getResources().getString(R.string.BIN_ID));
            return;
        }

        if (isItemID) {
            regId = chooseDetRow.getValue("ITEM_ID").toString();//料號
        } else {
            regId = holder.RegisterId.getText().toString().trim();
            if (regId.equals("")) {
                ShowMessage(R.string.WAPG003005, new ShowMessageEvent() {
                    @Override
                    public void onDismiss() {
                        holder.RegisterId.requestFocus();
                    }
                }, getResources().getString(R.string.REGISTER_ID));
                return;
            }
        }

        // Call BIModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchWmsRegister");
        bmObj.setRequestID("BIFetchWmsRegister");

        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Filter);
        if (holder.DetItemId.getText().equals("*"))
            param1.setParameterValue(String.format(" AND R.REGISTER_ID='%s' AND R.BIN_ID='%s' ", regId, binId));
        else
            param1.setParameterValue(String.format(" AND R.REGISTER_ID='%s' AND R.BIN_ID='%s' AND I.ITEM_ID='%s' ", regId, binId, holder.DetItemId.getText().toString()));
        bmObj.params.add(param1);

        CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    DataTable dt = bModuleReturn.getReturnJsonTables().get("BIFetchWmsRegister").get("SWMS_REGISTER");
                    if (dt.Rows.size() == 0) {
                        ShowMessage(R.string.WAPG003009);
                        holder.RegisterId.getText().clear();
                        if (holder.BinId.isEnabled()) {
                            holder.BinId.getText().clear();
                            holder.BinId.requestFocus();
                        } else {
                            holder.RegisterId.requestFocus();
                        }
                    } else {
                        holder.RegisterQty.setText(dt.Rows.get(0).getValue("QTY").toString());
                    }
                }
            }
        });
    }

    private void changeCountStatus(final String status) {
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BCountMstChangeCheckedStatus");
        bmObj.setModuleID("");
        bmObj.setRequestID("BGoodCount");
        bmObj.params = new Vector<>();

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BCountMstChangeCheckedStatusParam.CountId);
        param1.setParameterValue(mstRow.getValue("COUNT_ID").toString());
        bmObj.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BCountMstChangeCheckedStatusParam.CountStatus);
        param2.setParameterValue(status);
        bmObj.params.add(param2);

        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BCountMstChangeCheckedStatusParam.ItemId);
        param3.setParameterValue(chooseDetRow.getValue("ITEM_ID").toString());
        bmObj.params.add(param3);

        ParameterInfo param4 = new ParameterInfo();
        param4.setParameterID(BCountMstChangeCheckedStatusParam.BinId);
        param4.setParameterValue(chooseDetRow.getValue("BIN_ID").toString());
        bmObj.params.add(param4);

        CallBModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    Gson gson = new Gson();
                    String mstStatus = gson.fromJson(bModuleReturn.getReturnList().get("BGoodCount").get(BCountMstChangeCheckedStatusParam.ReturnMstCountStatus).toString(), String.class);
                    String detStatus = gson.fromJson(bModuleReturn.getReturnList().get("BGoodCount").get(BCountMstChangeCheckedStatusParam.ReturnDetCountStatus).toString(), String.class);
                    holder.CheckCountStatus.setText(mstStatus);

                    switch (detStatus) {
                        case "None":
                            holder.StartCheck.setEnabled(true);
                            holder.StartCheck.setAlpha(1);
                            holder.EndCheck.setEnabled(false);
                            holder.EndCheck.setAlpha(0.5f);
                            holder.CheckExecute.setEnabled(false);
                            holder.CheckExecute.setAlpha(0.5f);
                            holder.CreateCount.setEnabled(false);
                            holder.CreateCount.setAlpha(0.5f);
                            break;
                        case "Checked":
                            holder.StartCheck.setEnabled(false);
                            holder.StartCheck.setAlpha(0.5f);
                            holder.EndCheck.setEnabled(true);
                            holder.EndCheck.setAlpha(1);
                            holder.CheckExecute.setEnabled(true);
                            holder.CheckExecute.setAlpha(1);
                            holder.CreateCount.setEnabled(true);
                            holder.CreateCount.setAlpha(1);
                            break;
                        case "Closed":
                            holder.StartCheck.setEnabled(false);
                            holder.StartCheck.setAlpha(0.5f);
                            holder.EndCheck.setEnabled(false);
                            holder.EndCheck.setAlpha(0.5f);
                            holder.CheckExecute.setEnabled(false);
                            holder.CheckExecute.setAlpha(0.5f);
                            holder.CreateCount.setEnabled(false);
                            holder.CreateCount.setAlpha(0.5f);
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

    private void FetchCountInv() {
        String countKey = getIntent().getStringExtra("CountKey");

        BModuleObject biObj = new BModuleObject();
        biObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        biObj.setModuleID("BIFetchCountInv");
        biObj.setRequestID("BIFetchCountInv");
        VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
        biObj.params = new Vector<>();

        List<Condition> conditionCi = new ArrayList<>();
        List<Condition> conditionItem = new ArrayList<>();
        List<Condition> conditionBin = new ArrayList<>();
        HashMap<String, List<?>> dicCondition = new HashMap<>();

        Condition condition = new Condition();
        condition.setAliasTable("CI");
        condition.setColumnName("COUNT_KEY");
        condition.setDataType("string");
        condition.setValue(countKey);
        conditionCi.add(condition);
        dicCondition.put("COUNT_KEY", conditionCi);

        if (!holder.DetItemId.getText().toString().equals("") && !holder.DetItemId.getText().toString().equals("*"))
        {
            Condition condition1 = new Condition();
            condition1.setAliasTable("SI");
            condition1.setColumnName("ITEM_ID");
            condition1.setDataType("string");
            condition1.setValue(holder.DetItemId.getText().toString());
            conditionItem.add(condition1);
            dicCondition.put("ITEM_ID", conditionItem);
        }

        if (!holder.DetBinId.getText().toString().equals("") && !holder.DetBinId.getText().toString().equals("*"))
        {
            Condition condition1 = new Condition();
            condition1.setAliasTable("CI");
            condition1.setColumnName("BIN_ID");
            condition1.setDataType("string");
            condition1.setValue(holder.DetBinId.getText().toString());
            conditionBin.add(condition1);
            dicCondition.put("BIN_ID", conditionBin);
        }

        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vKey, vVal);
        String serializedObj = msdl.generateFinalCode(dicCondition);
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Condition);
        param1.setNetParameterValue(serializedObj);
        biObj.params.add(param1);

        CallBIModule(biObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    alreadyCountedDatatable = bModuleReturn.getReturnJsonTables().get("BIFetchCountInv").get("SWMS_COUNT_INV");
                    if (alreadyCountedDatatable == null || alreadyCountedDatatable.Rows.size() == 0) {
                        holder.ShowHisCount.setAdapter(null);
                    } else {
                        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        holder.ShowHisCount.setAdapter(new CheckHisGridAdapter(alreadyCountedDatatable, isCycle, inflater));
                    }
                }
                holder.ShowHisCount.setClickable(false);
            }
        });
    }

    private void ShowDialog() {
        AlertDialog.Builder countDialog = new AlertDialog.Builder(GoodCheckExecuteActivity.this);
        View dialogView = LayoutInflater.from(GoodCheckExecuteActivity.this).inflate(R.layout.style_wms_dialog_good_check_add, null);
        countDialog.setTitle("");
        countDialog.setView(dialogView);

        final EditText etItem = dialogView.findViewById(R.id.etCheckExecuteItemId);
        final EditText etBin = dialogView.findViewById(R.id.etCheckExecuteBinId);
        final EditText etReg = dialogView.findViewById(R.id.etCheckExecuteRegisterId);
        final EditText etRegQty = dialogView.findViewById(R.id.etCheckRegisterQty);
        final EditText etQty = dialogView.findViewById(R.id.etCheckExecuteQty);
        final Button btCreate = dialogView.findViewById(R.id.btCreate);
        final Button btCancel = dialogView.findViewById(R.id.btCancel);

        if (holder.DetItemId.getText().toString().equals("*"))
        {
            etItem.setText("");
            etItem.setEnabled(true);
        }
        else
        {
            etItem.setText(holder.DetItemId.getText().toString());
            etItem.setEnabled(false);
        }

        if (holder.DetBinId.getText().toString().equals(""))
        {
            etBin.setText("");
            etBin.setEnabled(true);
        }
        else
        {
            etBin.setText(holder.DetBinId.getText().toString());
            etBin.setEnabled(false);
        }

        final AlertDialog dialog = countDialog.create();
        dialog.show();

        btCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //region check
                if (etItem.getText().toString().equals(""))
                {
                    //請輸入[%s]!
                    etItem.requestFocus();
                    Object[] args = new Object[1];
                    args[0] = getResources().getString(R.string.ITEM_ID);
                    ShowMessage(R.string.WAPG003005, args);
                    return;
                }

                if (etBin.getText().toString().equals(""))
                {
                    //請輸入[%s]!
                    etBin.requestFocus();
                    Object[] args = new Object[1];
                    args[0] = getResources().getString(R.string.BIN_ID);
                    ShowMessage(R.string.WAPG003005, args);
                    return;
                }

                if (etReg.getText().toString().equals(""))
                {
                    //請輸入[%s]!
                    etReg.requestFocus();
                    Object[] args = new Object[1];
                    args[0] = getResources().getString(R.string.REGISTER_ID);
                    ShowMessage(R.string.WAPG003005, args);
                    return;
                }

                if (etQty.getText().toString().equals("")) {
                    //請輸入[%s]!
                    etQty.requestFocus();
                    Object[] args = new Object[1];
                    args[0] = getResources().getString(R.string.QTY);
                    ShowMessage(R.string.WAPG003005, args);
                    return;
                }
                //endregion

                String itemId = etItem.getText().toString().trim();

                String binId = etBin.getText().toString().trim();

                String registerId;
                if (isItemID) {
                    registerId = chooseDetRow.getValue("ITEM_ID").toString();
                } else {
                    registerId = etReg.getText().toString().equals("") ? "*" : etReg.getText().toString().trim();
                }

                double qty = Double.parseDouble(etQty.getText().toString());
                double inventoryQty = chooseDetRow.getValue("INVENTORY_QTY").toString().equals("") ? 0 : Double.parseDouble(chooseDetRow.getValue("INVENTORY_QTY").toString());

                ExecuteCount(itemId, binId, registerId, qty, inventoryQty, "Run", "Add");
                dialog.dismiss();
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

//    public void onClickConfirm(View v){
//        BModuleObject bmObj = new BModuleObject();
//        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.BExecuteCountDetClose");
//        bmObj.setModuleID("");
//        bmObj.setRequestID("BCountExecuteClose");
//        bmObj.params = new Vector<>();
//
//        ParameterInfo param1 = new ParameterInfo();
//        param1.setParameterID(BExecuteCountDetCloseParam.BinId);
//        param1.setParameterValue(holder.BinId.getText().toString());
//        bmObj.params.add(param1);
//
//        ParameterInfo param2 = new ParameterInfo();
//        param2.setParameterID(BExecuteCountDetCloseParam.ItemId);
//        param2.setParameterValue(holder.ItemId.getText().toString());
//        bmObj.params.add(param2);
//
//        ParameterInfo param3 = new ParameterInfo();
//        param3.setParameterID(BExecuteCountDetCloseParam.CountId);
//        param3.setParameterValue(getIntent().getStringExtra("CountId"));
//        bmObj.params.add(param3);
//
//        CallBModule(bmObj, new WebAPIClientEvent() {
//            @Override
//            public void onPostBack(BModuleReturn bModuleReturn) {
//                if (CheckBModuleReturnInfo(bModuleReturn)) {
//                    finish();
//                }
//            }
//        });
//    }

    public void onClickExecuteCount(View v) {
//        if (holder.ItemId.getText().toString().equals("")) {
//            //請輸入料號!
//            Object[] args = new Object[1];
//            args[0] = getResources().getString(R.string.ITEM_ID);
//            ShowMessage(R.string.WAPG003005, args);
//            return;
//        }
        if (holder.BinId.getText().toString().equals("")) {
            //請輸入儲位!
            holder.BinId.requestFocus();
            Object[] args = new Object[1];
            args[0] = getResources().getString(R.string.BIN_ID);
            ShowMessage(R.string.WAPG003005, args);
            return;
        }
        if (!isItemID) {
            if (holder.RegisterId.getText().toString().equals("")) {
                //請輸入批號!
                holder.RegisterId.requestFocus();
                Object[] args = new Object[1];
                args[0] = getResources().getString(R.string.REGISTER_ID);
                ShowMessage(R.string.WAPG003005, args);
                return;
            }
        }

        if (holder.Qty.getText().toString().equals("")) {
            //請輸入數量!
            holder.Qty.requestFocus();
            Object[] args = new Object[1];
            args[0] = getResources().getString(R.string.QTY);
            ShowMessage(R.string.WAPG003005, args);
            return;
        }

        String itemId = holder.DetItemId.getText().toString().trim();
        String binId = holder.BinId.getText().toString().trim();

        String registerId;
        if (isItemID) {
            registerId = chooseDetRow.getValue("ITEM_ID").toString();
        } else {
            registerId = holder.RegisterId.getText().toString().equals("") ? "*" : holder.RegisterId.getText().toString().trim();
        }
        double qty = Double.parseDouble(holder.Qty.getText().toString());
        double inventoryQty = chooseDetRow.getValue("INVENTORY_QTY").toString().equals("") ? 0 : Double.parseDouble(chooseDetRow.getValue("INVENTORY_QTY").toString());

        ExecuteCount(itemId, binId, registerId, qty, inventoryQty, "Checked", "Record");
    }

    private void ExecuteCount(String itemId, String binId, String registerId, double qty, double inventoryQty, final String status, final String countType)
    {
        List<CountInvObj> lstCountInvObjs = new ArrayList<>();
        String countId = getIntent().getStringExtra("CountId");

        CountInvObj ciObj = new CountInvObj();
        ciObj.setCountId(countId);
        ciObj.setItemId(itemId);
        ciObj.setLotId(registerId);
        ciObj.setInventoryStatus(status);
        ciObj.setCustStatus("*");
        ciObj.setInventoryQty(inventoryQty);
        ciObj.setFirstCountQty(qty);
        ciObj.setSecondCountQty(qty);
        ciObj.setUserAdjustFlg("N");
        ciObj.setBinId(binId);
        lstCountInvObjs.add(ciObj);

        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.INV.Parameter.ParameterObj.CountInvObj", "bmWMS.INV.Param");
        MList mListEnum = new MList(vListEnum);
        String strLsRelatData = mListEnum.generateFinalCode(lstCountInvObjs);

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BExecuteCount");
        bmObj.setModuleID("");
        bmObj.setRequestID("BCountExecute");
        bmObj.params = new Vector<>();

        ParameterInfo param = new ParameterInfo();
        param.setParameterID(BExecuteCountParam.CountInvObjs);
        param.setNetParameterValue(strLsRelatData);
        bmObj.params.add(param);

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BExecuteCountParam.CountType);
        param1.setParameterValue(countType);
        bmObj.params.add(param1);

        CallBModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {

                    if (status.equals("Checked"))
                    {
                        holder.Qty.getText().clear();
                        holder.RegisterId.getText().clear();
                        holder.RegisterQty.getText().clear();

                        if (chooseDetRow.getValue("BIN_ID").toString().equals("*"))
                        {
                            holder.BinId.getText().clear();
                            holder.BinId.requestFocus();
                        }
                        else
                        {
                            holder.RegisterId.requestFocus();
                        }

                        //輸入成功!
                        ShowMessage(R.string.WAPG003007);
                    }
                    else
                    {
                        //手動新增資料成功!
                        ShowMessage(R.string.WAPG003010);
                    }
                }
            }
        });
    }
}
