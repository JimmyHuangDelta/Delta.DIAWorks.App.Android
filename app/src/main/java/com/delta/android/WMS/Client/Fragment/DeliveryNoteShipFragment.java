package com.delta.android.WMS.Client.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.delta.android.Core.Activity.ScanActivity;
import com.delta.android.Core.Activity.ShowMessageEvent;
import com.delta.android.Core.DataTable.DataColumn;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.GenerateJsonNetString.MList;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionary;
import com.delta.android.Core.GenerateJsonNetString.MesSerializableDictionaryList;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Client.DeliveryNoteShipWorkNewActivity;
import com.delta.android.WMS.Client.GridAdapter.DeliveryNoteShipSelectGridAdapter;
import com.delta.android.WMS.Param.BDeliveryNoteShipParam;
import com.delta.android.WMS.Param.BIReleaseOrderOnboradCheckParam;
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.BStockOutBaseParam;
import com.delta.android.WMS.Param.ParamObj.CheckCountObj;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.delta.android.WMS.Param.ParamObj.PickDetObj;
import com.delta.android.WMS.Param.ParamObj.ReleaseOrderObj;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class DeliveryNoteShipFragment extends Fragment {

    private final int LOT_ID_QRSCAN_REQUEST_CODE = 11;

    private TextView tvAddPlate;
    private TextView tvSelectPlate;
    private TextView tvPhone;
    private TextView tvDriver;
    private RadioGroup rgPlate;
    private RadioButton rbAdd, rbSelect;
    private EditText etLicensePlate;
    private Spinner cmbPlate;
    private EditText etPhone;
    private EditText etDriver;
    private ImageButton ibtnLotIdQRScan;
    private EditText etLotId;
    private Button btnConfirm;
    private Button btnLotIdClear;

    private DataTable dtPlateInfo;
    private List<? extends Map<String, Object>> lstPlateInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_delivery_note_ship, container, false);

        tvAddPlate = view.findViewById(R.id.tvAddPlate);
        tvSelectPlate = view.findViewById(R.id.tvSelectPlate);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvDriver = view.findViewById(R.id.tvDriver);
        rgPlate = view.findViewById(R.id.rgPlate);
        rbAdd = view.findViewById(R.id.rbAdd);
        rbSelect = view.findViewById(R.id.rbSelect);
        etLicensePlate = view.findViewById(R.id.etLicensePlate);
        cmbPlate = view.findViewById(R.id.cmbPlate);
        etPhone = view.findViewById(R.id.etPhone);
        etDriver = view.findViewById(R.id.etDriver);
        ibtnLotIdQRScan = view.findViewById(R.id.ibtnLotIdQRScan);
        etLotId = view.findViewById(R.id.etLotId);
        btnLotIdClear = view.findViewById(R.id.btnLotIdClear);
        btnConfirm = view.findViewById(R.id.btnConfirm);

        tvAddPlate.setEnabled(false);
        tvSelectPlate.setEnabled(false);
        tvPhone.setEnabled(false);
        tvDriver.setEnabled(false);
        etLicensePlate.setEnabled(false);
        cmbPlate.setEnabled(false);
        etDriver.setEnabled(false);
        etPhone.setEnabled(false);

        etLotId.setOnKeyListener(etLotIdOnKey);
        btnLotIdClear.setOnClickListener(btnLotIdClearOnClick);
        ibtnLotIdQRScan.setOnClickListener(ibtnLotIdQRScanOnClick);
        btnConfirm.setOnClickListener(btnConfirmOnClick);
        rgPlate.setOnCheckedChangeListener(rgPlateOnCheckedChange);
        cmbPlate.setOnItemSelectedListener(cmbPlateOnItemSelected);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {

                Toast.makeText((DeliveryNoteShipWorkNewActivity) getActivity(), getResources().getText(R.string.QRCODE_SCAN_CANCEL).toString(), Toast.LENGTH_SHORT).show();

            } else {

                switch (requestCode) {

                    case LOT_ID_QRSCAN_REQUEST_CODE:
                        etLotId.setText(result.getContents().trim());
                        break;

                    default:
                        break;
                }
            }
        } else {

            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private View.OnKeyListener etLotIdOnKey = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {

            if (keyCode != KeyEvent.KEYCODE_ENTER) return false;

            if (keyEvent.getAction() == KeyEvent.ACTION_UP) {

                final DataTable dtMst, dtDet, dtOrderMst, dtOrderDet, dtPickMst, dtPickDet, dtSheetConfig;
                dtMst = ((DeliveryNoteShipWorkNewActivity) getActivity()).dtMst;
                dtDet = ((DeliveryNoteShipWorkNewActivity) getActivity()).dtDet;
                dtOrderMst = ((DeliveryNoteShipWorkNewActivity) getActivity()).dtOrderMst;
                dtOrderDet = ((DeliveryNoteShipWorkNewActivity) getActivity()).dtOrderDet;
                dtPickMst = ((DeliveryNoteShipWorkNewActivity) getActivity()).dtPickMst;
                dtPickDet = ((DeliveryNoteShipWorkNewActivity) getActivity()).dtPickDet;
                dtSheetConfig = ((DeliveryNoteShipWorkNewActivity) getActivity()).dtSheetConfig;

                // 檢查是否有選擇單據
                if (dtPickDet == null || dtPickDet.Rows.size() == 0) {
                    ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG002001); //WAPG002001    請輸入單據代碼
                }

                String strLisencePlate = "";
                final String strDriverName = etDriver.getText().toString().trim();
                final String strDriverPhone = etPhone.getText().toString().trim();

                if (rbAdd.isChecked()) {
                    strLisencePlate = etLicensePlate.getText().toString().trim();
                } else if (rbSelect.isChecked()) {
                    if (cmbPlate.getSelectedItem() == null || cmbPlate.getSelectedItem().toString().trim().length() <= 0) {
                        ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG002026); //WAPG002026    請新增或選擇車牌號碼
                        return false;
                    } else {
                        strLisencePlate = ((DataRow) cmbPlate.getSelectedItem()).getValue("LICENSE_PLATE_NUM").toString();
                    }
                } else {
                    ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG002026); //WAPG002026    請新增或選擇車牌號碼
                    return false;
                }

                if (strLisencePlate.length() <= 0) {
                    ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG002010); //WAPG002010    車牌輸入錯誤
                    return false;
                }

                if (strDriverName.length() <= 0) {
                    ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG002009); //WAPG002009    請輸入司機
                    return false;
                }

                if (strDriverPhone.length() <= 0) {
                    ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG002008); //WAPG002008    請輸入電話
                    return false;
                }

                final String strLotId = etLotId.getText().toString().trim();
                if (strLotId.length() <= 0) {
                    ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG002028); //WAPG002028    請輸入存貨代碼
                    return false;
                }

                if (dtDet == null || dtDet.Rows.size() <= 0 || dtPickDet == null || dtPickDet.Rows.size() <= 0) {
                    ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG002004); //WAPG002004 查無揀貨資料
                    return false;
                }

                // 檢查批號是否已存在
                for (DataRow drOd : dtOrderDet.Rows) {
                    if (drOd.getValue("LOT_ID").equals(strLotId)) {
                        Object[] args = new Object[1];
                        args[0] = strLotId;
                        ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG002029, args); //WAPG002029    存貨代碼[%s]已存在
                        return false;
                    }
                }

                DataRow drPickDet = null;
                for (DataRow drPk : dtPickDet.Rows) {
                    if (drPk.getValue("LOT_ID").equals(strLotId)) {
                        drPickDet = drPk;
                        break;
                    }
                }
                if (drPickDet == null) {
                    Object[] args = new Object[1];
                    args[0] = strLotId;
                    ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG002030, args); //WAPG002030    存貨代碼[%s]不存在揀料清單中
                    return false;
                }

                for (DataRow drOm : dtOrderMst.Rows) {

                    if (drOm.getValue("RELEASE_ORDER_STATUS").toString().equals("Open"))
                        continue;

                    String drPlateNum = drOm.getValue("LICENSE_PLATE_NUM").toString().trim();
                    String drDriverName = drOm.getValue("DRIVER_NAME").toString().trim();

                    if (drPlateNum.equals(strLisencePlate) && drDriverName.equals(strDriverName)) {
                        Object[] args = new Object[2];
                        args[0] = strLisencePlate;
                        args[1] = strDriverName;
                        ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG002031, args); //WAPG002031    車牌號碼[%s]司機名稱[%s]已存在
                        return false;
                    }
                }

                //檢查該車牌目前要出貨的貨 客戶、送貨地址、出貨日期是否相同
                BModuleObject bmObj = new BModuleObject();
                bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BIReleaseOrderOnboradCheck");
                bmObj.setModuleID("BICheckLicense");
                bmObj.setRequestID("BICheckLicense");

                bmObj.params = new Vector<ParameterInfo>();
                ParameterInfo param1 = new ParameterInfo();
                param1.setParameterID(BIReleaseOrderOnboradCheckParam.LicensePlateNum);
                param1.setParameterValue(strLisencePlate);
                bmObj.params.add(param1);

                ParameterInfo param2 = new ParameterInfo();
                param2.setParameterID(BIReleaseOrderOnboradCheckParam.CustomerKey);
                param2.setParameterValue(dtMst.Rows.get(0).getValue("CUSTOMER_KEY").toString());
                bmObj.params.add(param2);

                ParameterInfo param3 = new ParameterInfo();
                param3.setParameterID(BIReleaseOrderOnboradCheckParam.ShipDate);
                String strDate = dtMst.Rows.get(0).getValue("SHIP_DATE").toString().substring(0, 10).replace("-", "/");
                param3.setParameterValue(strDate);
                bmObj.params.add(param3);

                ParameterInfo param4 = new ParameterInfo();
                param4.setParameterID(BIReleaseOrderOnboradCheckParam.DeliveryAddress);
                param4.setParameterValue(dtMst.Rows.get(0).getValue("DELIVERY_ADDRESS").toString());
                bmObj.params.add(param4);

                final DataRow finalDrPickDet = drPickDet;
                final String finalStrLisencePlate = strLisencePlate;
                ((DeliveryNoteShipWorkNewActivity) getActivity()).CallBIModule(bmObj, new WebAPIClientEvent() {
                    @Override
                    public void onPostBack(BModuleReturn bModuleReturn) {
                        if (((DeliveryNoteShipWorkNewActivity) getActivity()).CheckBModuleReturnInfo(bModuleReturn)) {

                            DataTable dtCheck = bModuleReturn.getReturnJsonTables().get("BICheckLicense").get("Result");
                            if (!dtCheck.Rows.get(0).getValue("ENABLE").equals("Y"))
                                return;

                            List<ReleaseOrderObj> alReleaseOrder = new ArrayList<>();
                            ReleaseOrderObj ro = new ReleaseOrderObj();
                            ro.setDnId(dtMst.Rows.get(0).getValue("DN_ID").toString());
                            ro.setLicensePlateNum(finalStrLisencePlate);
                            ro.setDriverName(strDriverName);
                            ro.setDriverPhone(strDriverPhone);
                            ro.setCustomerKey(dtMst.Rows.get(0).getValue("CUSTOMER_KEY").toString());
                            ro.setDeliveryAddress(dtMst.Rows.get(0).getValue("DELIVERY_ADDRESS").toString());
                            ro.setShipDate(dtMst.Rows.get(0).getValue("SHIP_DATE").toString());
                            ro.setSeq(finalDrPickDet.getValue("SEQ").toString());
                            ro.setItemKey(finalDrPickDet.getValue("ITEM_KEY").toString());
                            ro.setLotId(strLotId);
                            ro.setStorageKey(finalDrPickDet.getValue("STORAGE_KEY").toString());
                            ro.setQty(finalDrPickDet.getValue("QTY").toString());
                            ro.setCmt(finalDrPickDet.getValue("CMT").toString());
                            alReleaseOrder.add(ro);

                            List<PickDetObj> lstPickDetObj = new ArrayList<>();
                            PickDetObj pickDetObj = new PickDetObj();
                            pickDetObj.setSheetId(finalDrPickDet.getValue("SHEET_ID").toString());
                            pickDetObj.setSeq(Double.parseDouble(finalDrPickDet.getValue("SEQ").toString()));
                            pickDetObj.setItemId(finalDrPickDet.getValue("ITEM_ID").toString());
                            pickDetObj.setLotId(finalDrPickDet.getValue("LOT_ID").toString());
                            pickDetObj.setQty(Double.parseDouble(finalDrPickDet.getValue("QTY").toString()));
                            pickDetObj.setStorageId(finalDrPickDet.getValue("STORAGE_ID").toString());
                            pickDetObj.setBinId(finalDrPickDet.getValue("BIN_ID").toString());
                            pickDetObj.setUom(finalDrPickDet.getValue("UOM").toString());
                            lstPickDetObj.add(pickDetObj);

                            VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.PickDetObj", "bmWMS.Library.Param");
                            MList mListEnum = new MList(vListEnum);
                            String strLsRelatData = mListEnum.generateFinalCode(lstPickDetObj);

                            VirtualClass vListEnum2 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.INV.Parameter.ParameterObj.ReleaseOrderObj", "bmWMS.INV.Param");
                            MList mListEnum2 = new MList(vListEnum2);
                            String strLsReleaseOrder = mListEnum2.generateFinalCode(alReleaseOrder);

                            BModuleObject bmObj = new BModuleObject();
                            bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BDeliveryNoteShip");
                            bmObj.setModuleID("BDeliveryNoteShip");
                            bmObj.setRequestID("BDeliveryNoteShip");
                            bmObj.params = new Vector<ParameterInfo>();

                            ParameterInfo param1 = new ParameterInfo();
                            param1.setParameterID(BStockOutBaseParam.CheckStockOutByObjs); //20220829 archie 將可共用的Param改為出庫Base的Param
                            param1.setNetParameterValue(strLsRelatData);
                            bmObj.params.add(param1);

                            ParameterInfo param2 = new ParameterInfo();
                            param2.setParameterID(BDeliveryNoteShipParam.ReleaseOrderObj);
                            param2.setNetParameterValue(strLsReleaseOrder);
                            bmObj.params.add(param2);

                            ParameterInfo param4 = new ParameterInfo();
                            param4.setParameterID(BDeliveryNoteShipParam.Action);
                            param4.setParameterValue("SAVE");
                            bmObj.params.add(param4);
                            ParameterInfo param5 = new ParameterInfo();
                            param5.setParameterID(BDeliveryNoteShipParam.DnId);
                            param5.setParameterValue(dtMst.Rows.get(0).getValue("DN_ID").toString());
                            bmObj.params.add(param5);

                            ParameterInfo paramExecuteChkStock = new ParameterInfo();
                            paramExecuteChkStock.setParameterID(BDeliveryNoteShipParam.ExecuteCheckStock); // 20220805 Add by Ikea 是否執行盤點檢查
                            paramExecuteChkStock.setNetParameterValue2("false");
                            bmObj.params.add(paramExecuteChkStock);

                            ParameterInfo paramMode = new ParameterInfo();
                            paramMode.setParameterID(BStockOutBaseParam.Mode);
                            paramMode.setParameterValue("Ship");
                            bmObj.params.add(paramMode);

                            ((DeliveryNoteShipWorkNewActivity) getActivity()).CallBModule(bmObj, new WebAPIClientEvent() {
                                @Override
                                public void onPostBack(BModuleReturn bModuleReturn) {
                                    if (((DeliveryNoteShipWorkNewActivity) getActivity()).CheckBModuleReturnInfo(bModuleReturn)) {

                                        String holdItemId = "";
                                        String holdMessage = "";
                                        String hintMessage = "";

                                        if (bModuleReturn.getReturnList().get("BDeliveryNoteShip") != null) {

                                            if (bModuleReturn.getReturnList().get("BDeliveryNoteShip").get(BDeliveryNoteShipParam.HoldItemID) != null) {
                                                holdItemId = bModuleReturn.getReturnList().get("BDeliveryNoteShip").get(BDeliveryNoteShipParam.HoldItemID).toString();
                                            }

                                            if (bModuleReturn.getReturnList().get("BDeliveryNoteShip").get(BDeliveryNoteShipParam.HoldMessage) != null) {
                                                holdMessage = bModuleReturn.getReturnList().get("BDeliveryNoteShip").get(BDeliveryNoteShipParam.HoldMessage).toString();
                                                holdMessage = split(holdMessage);
                                            }

                                            if (bModuleReturn.getReturnList().get("BDeliveryNoteShip").get(BDeliveryNoteShipParam.HintMessage) != null) {
                                                hintMessage = bModuleReturn.getReturnList().get("BDeliveryNoteShip").get(BDeliveryNoteShipParam.HintMessage).toString();
                                                hintMessage = split(hintMessage);
                                            }
                                        }

                                        String showExtraMsg = "";

                                        if (hintMessage.length() > 0) {
                                            showExtraMsg += ((DeliveryNoteShipWorkNewActivity) getActivity()).getResString(getResources().getString(R.string.ITEM_SPEC_CTRL_HINT_MSG)) + System.getProperty("line.separator");
                                            showExtraMsg += hintMessage + System.getProperty("line.separator");
                                        }

                                        if (holdMessage.length() > 0) {
                                            showExtraMsg += ((DeliveryNoteShipWorkNewActivity) getActivity()).getResString(getResources().getString(R.string.ITEM_SPEC_CTRL_HOLD_MSG)) + System.getProperty("line.separator");
                                            showExtraMsg += holdMessage + System.getProperty("line.separator");
                                        }

                                        if (holdItemId.length() > 0) { // 有被鎖定的物料代碼 => 揀貨失敗, 有額外資訊需顯示

                                            Object[] args = new Object[1];
                                            args[0] = holdItemId;

                                            // 揀貨失敗
                                            final String finalShowExtraMsg = showExtraMsg;
                                            ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG002033, new ShowMessageEvent() {
                                                @Override
                                                public void onDismiss() {

                                                    if (finalShowExtraMsg.length() > 0) {

                                                        ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(finalShowExtraMsg, new ShowMessageEvent() {
                                                            @Override
                                                            public void onDismiss() {
                                                                etLicensePlate.getText().clear();
                                                                etDriver.getText().clear();
                                                                etLotId.getText().clear();
                                                                ((DeliveryNoteShipWorkNewActivity) getActivity()).getPickSheetDet();
                                                            }
                                                        });

                                                    } else {
                                                        etLicensePlate.getText().clear();
                                                        etDriver.getText().clear();
                                                        etLotId.getText().clear();
                                                        ((DeliveryNoteShipWorkNewActivity) getActivity()).getPickSheetDet();
                                                    }

                                                }
                                            }, args);
                                        } else { // 揀貨成功, 但有額外資訊需顯示

                                            final String finalShowExtraMsg = showExtraMsg;
                                            if (finalShowExtraMsg.length() > 0) {

                                                ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(finalShowExtraMsg, new ShowMessageEvent() {
                                                    @Override
                                                    public void onDismiss() {
                                                        etLicensePlate.getText().clear();
                                                        etDriver.getText().clear();
                                                        etLotId.getText().clear();
                                                        ((DeliveryNoteShipWorkNewActivity) getActivity()).getPickSheetDet();
                                                    }
                                                });

                                            } else {
                                                Toast.makeText(getContext(), R.string.WAPG002007, Toast.LENGTH_SHORT).show();
                                                etLicensePlate.getText().clear();
                                                etDriver.getText().clear();
                                                etLotId.getText().clear();
                                                ((DeliveryNoteShipWorkNewActivity) getActivity()).getPickSheetDet();
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }
                });

                return true;
            }

            return false;
        }
    };

    private Spinner.OnItemSelectedListener cmbPlateOnItemSelected = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {

            String strLicensePlateNum = "";
            String strDriveName = "";
            String strDriverPhone = "";

            if (position != lstPlateInfo.size() - 1) {
                Map<String, String> infoMap = (Map<String, String>) parent.getItemAtPosition(position);
                strLicensePlateNum = infoMap.get("LICENSE_PLATE_NUM");
                strDriveName = infoMap.get("DRIVER_NAME");
                strDriverPhone = infoMap.get("DRIVER_PHONE");
            }

            etDriver.setText(strDriveName);
            etPhone.setText(strDriverPhone);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            return;
        }
    };

    public class SimpleArrayAdapter<T> extends SimpleAdapter {

        public SimpleArrayAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        //複寫這個方法，使提示字改為灰色
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = super.getView(position, convertView, parent);

            if (position == getCount()) {
                HashMap<String, String> rowData = (HashMap) getItem(getCount());
                ((TextView) v.findViewById(android.R.id.text1)).setText("");
                ((TextView) v.findViewById(android.R.id.text1)).setHint(rowData.get("SHEET_ID"));
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

    private RadioGroup.OnCheckedChangeListener rgPlateOnCheckedChange = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (group.getId() == R.id.rgPlate) {

                switch (checkedId) {
                    case R.id.rbAdd:

                        //disable 下拉選單
                        tvSelectPlate.setEnabled(false);
                        cmbPlate.setEnabled(false);

                        tvDriver.setEnabled(true);
                        tvAddPlate.setEnabled(true);
                        tvPhone.setEnabled(true);
                        tvDriver.setEnabled(true);
                        etDriver.setEnabled(true);
                        etPhone.setEnabled(true);
                        etLicensePlate.setEnabled(true);

                        //清除資訊
                        if (lstPlateInfo != null && lstPlateInfo.size() > 0)
                            cmbPlate.setSelection(lstPlateInfo.size() - 1, true);
                        etDriver.setText("");
                        etPhone.setText("");
                        break;

                    case R.id.rbSelect:
                        //disable 相關輸入
                        tvAddPlate.setEnabled(false);
                        tvDriver.setEnabled(false);
                        tvPhone.setEnabled(false);
                        etDriver.setEnabled(false);
                        etPhone.setEnabled(false);
                        etLicensePlate.setEnabled(false);

                        tvSelectPlate.setEnabled(true);
                        cmbPlate.setEnabled(true);
                        if (lstPlateInfo != null && lstPlateInfo.size() > 0)
                            cmbPlate.setSelection(lstPlateInfo.size() - 1, true);
                        //清除資訊
                        etDriver.setText("");
                        etPhone.setText("");
                        etLicensePlate.setText("");
                        break;
                }
            }
        }
    };

    private View.OnClickListener ibtnLotIdQRScanOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            IntentIntegrator integrator = IntentIntegrator.forSupportFragment(DeliveryNoteShipFragment.this);
            // 設置要掃描的條碼類型，ONE_D_CODE_TYPES：一維碼，QR_CODE_TYPES-二維碼
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("BarCode Scan"); //底部的提示文字
            integrator.setCameraId(0); //前置(1)或者後置(0)攝像頭
            integrator.setBeepEnabled(false); //掃描成功的「嗶嗶」聲
            integrator.setBarcodeImageEnabled(false); //是否保留掃碼成功時候的截圖
            integrator.setCaptureActivity(ScanActivity.class);
            switch (view.getId()) {

                case R.id.ibtnLotIdQRScan:

                    integrator.setRequestCode(LOT_ID_QRSCAN_REQUEST_CODE);
                    break;

                default:

                    integrator.setRequestCode(0);
                    break;
            }
            integrator.initiateScan();
        }
    };

    private View.OnClickListener btnLotIdClearOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            etLotId.setText("");
        }
    };

    private View.OnClickListener btnConfirmOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            selectTempBin();
        }
    };

    private void selectTempBin() {
        final DataTable dtMst, dtDet, dtOrderMst, dtOrderDet, dtPickMst, dtPickDet, dtSheetConfig;
        dtMst = ((DeliveryNoteShipWorkNewActivity) getActivity()).dtMst;
        dtDet = ((DeliveryNoteShipWorkNewActivity) getActivity()).dtDet;
        dtOrderMst = ((DeliveryNoteShipWorkNewActivity) getActivity()).dtOrderMst;
        dtOrderDet = ((DeliveryNoteShipWorkNewActivity) getActivity()).dtOrderDet;
        dtPickMst = ((DeliveryNoteShipWorkNewActivity) getActivity()).dtPickMst;
        dtPickDet = ((DeliveryNoteShipWorkNewActivity) getActivity()).dtPickDet;
        dtSheetConfig = ((DeliveryNoteShipWorkNewActivity) getActivity()).dtSheetConfig;

        // 檢查是否有選擇單據
        if (dtPickDet == null || dtPickDet.Rows.size() == 0) {
            ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG002001); //WAPG002001    請輸入單據代碼
        }

        // 檢查批號是否過期
        for (DataRow drCheck : dtPickDet.Rows) {
            if (!drCheck.getValue("EXP_DATE").equals("")) {
                String strExpDate = drCheck.getValue("EXP_DATE").toString();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                try {
                    Date date = sdf.parse(strExpDate);
                    Date currDate = Calendar.getInstance().getTime();
                    if (date.compareTo(currDate) < 0) {
                        Object[] args = new Object[1];
                        args[0] = drCheck.getValue("LOT_ID").toString();
                        ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG002015, args); //WAPG002015 批號[%s]已過期
                        return;
                    }
                } catch (ParseException e) {
                    Object[] args = new Object[1];
                    args[0] = drCheck.getValue("LOT_ID").toString();
                    ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG002014, args); //WAPG002014 批號[%s]無法正常轉換時間
                }
            }

        }

        // 檢查是否需要確認庫存 - 暫時拿掉

        // 檢查是否有出貨派車資訊
        if (dtOrderMst == null || dtOrderMst.Rows.size() == 0 || dtOrderDet == null || dtOrderDet.Rows.size() == 0) {

            ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG002026); //WAPG002026     請新增或選擇車牌號碼
            return;

        }

        String strLisencePlate = "";
        final String strDriverName = etDriver.getText().toString().trim();
        final String strDriverPhone = etPhone.getText().toString().trim();

        if (rbAdd.isChecked()) {

            strLisencePlate = etLicensePlate.getText().toString().trim();

            if (strLisencePlate.length() <= 0) {
                ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG002026); //WAPG002026    請新增或選擇車牌號碼
                return;
            }

        } else if (rbSelect.isChecked()) {

            if (cmbPlate.getSelectedItem() == null || cmbPlate.getSelectedItem().toString().trim().length() <= 0) {
                ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG002026); //WAPG002026    請新增或選擇車牌號碼
                return;
            } else {
                strLisencePlate = ((DataRow) cmbPlate.getSelectedItem()).getValue("LICENSE_PLATE_NUM").toString();
            }

        } else {

            ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG002026); //WAPG002026    請新增或選擇車牌號碼
            return;

        }

        // 檢查車牌的 RELEASE_ORDER_STATUS
        DataTable dtCanShipOrderMst = new DataTable();
        DataTable dtCanShipOrderDet = new DataTable();
        String dnId = dtMst.Rows.get(0).getValue("DN_ID").toString();
        for (DataRow drOm : dtOrderMst.Rows) {

            if (drOm.getValue("DN_ID").toString().equals(dnId) && drOm.getValue("LICENSE_PLATE_NUM").toString().equals(strLisencePlate) && drOm.getValue("RELEASE_ORDER_STATUS").toString().equals("Closed")) {
                Object[] args = new Object[1];
                args[0] = strLisencePlate;
                ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG002027, args); //WAPG002027    車牌[%s]的車門已關閉，無法修改
                return;
            }

            if (drOm.getValue("DN_ID").toString().equals(dnId) && drOm.getValue("LICENSE_PLATE_NUM").toString().equals(strLisencePlate)) {
                dtCanShipOrderMst.Rows.add(drOm);
                break;
            }
        }

        for (DataRow drOd : dtOrderDet.Rows) {

            if (drOd.getValue("DN_ID").toString().equals(dnId) && drOd.getValue("LICENSE_PLATE_NUM").toString().equals(strLisencePlate)) {
                dtCanShipOrderDet.Rows.add(drOd);
            }
        }

        if (dtCanShipOrderDet.Rows.size() == 0) {
            ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG002032); //WAPG002032    查無準備出貨資訊
            return;
        }

        List<PickDetObj> lstPickDetObj = new ArrayList<>();
        for (DataRow drShip : dtCanShipOrderDet.Rows) {
            for (DataRow drPick : dtPickDet.Rows) {

                if (drShip.getValue("SEQ").toString().equals(drPick.getValue("SEQ").toString()) &&
                        drShip.getValue("ITEM_KEY").toString().equals(drPick.getValue("ITEM_KEY").toString()) &&
                        drShip.getValue("LOT_ID").toString().equals(drPick.getValue("LOT_ID").toString()) &&
                        drShip.getValue("STORAGE_KEY").toString().equals(drPick.getValue("STORAGE_KEY").toString())) {

                    PickDetObj pDet = new PickDetObj();
                    pDet.setSheetId(drPick.getValue("SHEET_ID").toString());
                    pDet.setSeq(Double.parseDouble(drPick.getValue("SEQ").toString()));
                    pDet.setItemId(drPick.getValue("ITEM_ID").toString());
                    pDet.setLotId(drPick.getValue("LOT_ID").toString());
                    pDet.setStorageId(drPick.getValue("STORAGE_ID").toString());
                    pDet.setBinId(drPick.getValue("BIN_ID").toString());
                    pDet.setUom(drPick.getValue("UOM").toString());
                    pDet.setQty(Double.parseDouble(drPick.getValue("QTY").toString()));
                    lstPickDetObj.add(pDet);
                }
            }
        }

        ArrayList<String> lstStorage = new ArrayList<String>();
        for (PickDetObj pDet : lstPickDetObj) {
            String storageId = pDet.getStorageId();
            if (!lstStorage.contains(storageId)) {
                lstStorage.add(storageId);
            }
        }

        checkStorageInTempBin(lstStorage, lstPickDetObj, dtCanShipOrderMst, dtCanShipOrderDet);
    }

    private void checkStorageInTempBin(final List<String> lstStorage, final List<PickDetObj> lstPickDetObj, final DataTable dtCanShipOrderMst, final DataTable dtCanShipOrderDet) {

        final HashMap<String, ArrayList<String>> mapStorageBin = new HashMap<String, ArrayList<String>>();

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchBin");
        bmObj.setRequestID("BIFetchBin");

        bmObj.params = new Vector<ParameterInfo>();
        //裝Condition的容器
        final HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
        List<Condition> lstCondition = new ArrayList<>();
        for (int i = 0; i < lstStorage.size(); i++) {
            Condition cond = new Condition();
            cond.setAliasTable("S");
            cond.setColumnName("STORAGE_ID");
            cond.setValue(lstStorage.get(i));
            cond.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
            lstCondition.add(cond);
        }
        mapCondition.put("STORAGE_ID", lstCondition);

        //Serialize序列化
        VirtualClass vKey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vKey, vVal);
        String strCond = msdl.generateFinalCode(mapCondition);

        //Input param
        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Condition);
        param1.setNetParameterValue(strCond);
        bmObj.params.add(param1);

        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BIWMSFetchInfoParam.Filter);
        param2.setParameterValue("AND B.BIN_TYPE IN ('OT', 'OS')");
        bmObj.params.add(param2);

        //Call BIModule
        ((DeliveryNoteShipWorkNewActivity) getActivity()).CallBIModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (((DeliveryNoteShipWorkNewActivity) getActivity()).CheckBModuleReturnInfo(bModuleReturn)) {
                    DataTable dtStorageTempBin = bModuleReturn.getReturnJsonTables().get("BIFetchBin").get("BIN");

                    //有暫存區，就直接到暫存區
                    for (final String storageID : lstStorage) {
                        for (DataRow dr : dtStorageTempBin.Rows) {
                            if (dr.getValue("STORAGE_ID").toString().equals(storageID)) {
                                if (!mapStorageBin.containsKey(storageID)) {
                                    ArrayList<String> lstBin = new ArrayList<>();

                                    if (dr.getValue("BIN_TYPE").toString().equals("OT"))
                                        lstBin.add(dr.getValue("BIN_ID").toString());

                                    mapStorageBin.put(storageID, lstBin);
                                } else {
                                    if (dr.getValue("BIN_TYPE").toString().equals("OT"))
                                        mapStorageBin.get(storageID).add(dr.getValue("BIN_ID").toString());
                                }
                            }
                        }
                    }

                    for (final String storageID : lstStorage) {
                        for (DataRow dr : dtStorageTempBin.Rows) {
                            if (dr.getValue("STORAGE_ID").toString().equals(storageID)) {
                                if (mapStorageBin.get(storageID).size() <= 0) {
                                    if (dr.getValue("BIN_TYPE").toString().equals("OS"))
                                        mapStorageBin.get(storageID).add(dr.getValue("BIN_ID").toString());
                                }
                            }
                        }
                    }
                    showConfirmDialog(mapStorageBin, lstPickDetObj, dtCanShipOrderMst, dtCanShipOrderDet);
                }
            }
        });
    }

    private void showConfirmDialog(final HashMap<String, ArrayList<String>> mapStorageBin, final List<PickDetObj> lstPickDet, final DataTable dtCanShipOrderMst, final DataTable dtCanShipOrderDet) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View viewConfirm = inflater.inflate(R.layout.activity_delivery_note_ship_confirm_dialog, null);
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setView(viewConfirm);

        final android.app.AlertDialog dialogConfirm = builder.create();
        dialogConfirm.setCancelable(false);
        dialogConfirm.show();

        //取得控制項
        final ListView lvStorageItem = dialogConfirm.findViewById(R.id.lvShipItem);
        Button btnSelectConfirm = dialogConfirm.findViewById(R.id.btnBinConfirm);

        //整理資料(by 倉庫+物料)
        ArrayList<String> lstTemp = new ArrayList<String>();
        final DataTable dtStorage = new DataTable();
//        dtStorageItem.addColumn(new DataColumn("ITEM_ID"));
//        dtStorageItem.addColumn(new DataColumn("ITEM_NAME"));
        dtStorage.addColumn(new DataColumn("STORAGE_ID"));
        dtStorage.addColumn(new DataColumn("BIN_ID"));

        for (PickDetObj pDet : lstPickDet) {
            String strSelectKey = pDet.getStorageId();
            //當取得的儲位不只一個時才加入

            if (!lstTemp.contains(strSelectKey)) {
                lstTemp.add(strSelectKey);
                DataRow drNew = dtStorage.newRow();
//                drNew.setValue("ITEM_ID", pDet.getItemId());
//                drNew.setValue("ITEM_NAME", dr.getValue("ITEM_NAME").toString());
                drNew.setValue("STORAGE_ID", pDet.getStorageId());
                drNew.setValue("BIN_ID", "");
                dtStorage.Rows.add(drNew);
            }
        }

        LayoutInflater inflaterSelect = LayoutInflater.from(getContext());
        DeliveryNoteShipSelectGridAdapter adapterSelect = new DeliveryNoteShipSelectGridAdapter(dtStorage, inflaterSelect);
        lvStorageItem.setAdapter(adapterSelect);
        adapterSelect.notifyDataSetChanged();

        lvStorageItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LayoutInflater inflaterBin = LayoutInflater.from(getContext());
                final View viewBin = inflaterBin.inflate(R.layout.activity_delivery_not_ship_confirm_select_bin, null);
                final android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.Builder(getContext());
                builder1.setView(viewBin);

                final android.app.AlertDialog dialogBin = builder1.create();
                dialogBin.setCancelable(false);
                dialogBin.show();

                final Spinner cmbBin = dialogBin.findViewById(R.id.cmbBinID);
                final Button btnBinConfirm = dialogBin.findViewById(R.id.btnBinConfirm);

                final String strStorage = dtStorage.getValue(position, "STORAGE_ID").toString();
                ArrayAdapter<String> adapterBin = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, mapStorageBin.get(strStorage));
                cmbBin.setAdapter(adapterBin);

                cmbBin.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                        for (DataRow dr : dtStorage.Rows) {
                            if (dr.getValue("STORAGE_ID").toString().equals(strStorage)) {
                                dr.setValue("BIN_ID", mapStorageBin.get(strStorage).get(position));
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        //do nothing
                    }
                });

                btnBinConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //refreash listview
                        LayoutInflater inflaterSelect = LayoutInflater.from(getContext());
                        DeliveryNoteShipSelectGridAdapter adapterSelect = new DeliveryNoteShipSelectGridAdapter(dtStorage, inflaterSelect);
                        lvStorageItem.setAdapter(adapterSelect);

                        dialogBin.dismiss();
                    }
                });
            }
        });

        btnSelectConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (DataRow dr : dtStorage.Rows) {
                    if (dr.getValue("BIN_ID").toString().equals("")) {
                        Object[] args = new Object[2];
                        args[0] = dr.getValue("STORAGE_ID").toString();
                        ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG009018, args); //WAPG009018    尚未選擇倉庫[%s]對應儲位
                        return;
                    }
                }
                dialogConfirm.dismiss();

                HashMap<String, String> mapStorageBinResult = new HashMap<String, String>();
                HashMap<String, String> mapSeqTempBin = new HashMap<>();

                //帶入TEMP_BIN
                for (PickDetObj pDet : lstPickDet) {
                    String storage = pDet.getStorageId();
                    String itemid = pDet.getItemId();
                    for (DataRow dr : dtStorage.Rows) {
                        if (dr.getValue("STORAGE_ID").toString().equals(storage)) {
//                            drLot.setValue("TEMP_BIN", dr.getValue("BIN_ID").toString());
                            if (!mapStorageBinResult.containsKey(storage)) {
                                mapStorageBinResult.put(storage, dr.getValue("BIN_ID").toString());
                            }
                        }
                    }
                }

                for (PickDetObj pDet : lstPickDet) {
                    if (!mapSeqTempBin.containsKey(pDet.getSeq()))
                        mapSeqTempBin.put(String.valueOf(pDet.getSeq()), mapStorageBinResult.get(pDet.getStorageId()));
                }

                executeShip(lstPickDet, mapSeqTempBin, dtCanShipOrderMst, dtCanShipOrderDet);
            }
        });
    }

    private void executeShip(List<PickDetObj> lstPickDet, HashMap<String, String> mapSeqTempBin, DataTable dtCanShipOrderMst, DataTable dtCanShipOrderDet) {

        final DataTable dtMst, dtDet, dtOrderMst, dtOrderDet, dtPickMst, dtPickDet, dtSheetConfig;
        dtMst = ((DeliveryNoteShipWorkNewActivity) getActivity()).dtMst;
        dtDet = ((DeliveryNoteShipWorkNewActivity) getActivity()).dtDet;
        dtOrderMst = ((DeliveryNoteShipWorkNewActivity) getActivity()).dtOrderMst;
        dtOrderDet = ((DeliveryNoteShipWorkNewActivity) getActivity()).dtOrderDet;
        dtPickMst = ((DeliveryNoteShipWorkNewActivity) getActivity()).dtPickMst;
        dtPickDet = ((DeliveryNoteShipWorkNewActivity) getActivity()).dtPickDet;
        dtSheetConfig = ((DeliveryNoteShipWorkNewActivity) getActivity()).dtSheetConfig;

        //檢查 ACTUAL_QTY_STATUS  實際數量的狀態
        String pickSheetId = dtMst.Rows.get(0).getValue("SHEET_ID").toString();

        double sum = 0;
        double trxQty = 0;
        String seq = "0";
        for (DataRow drChkQty : dtDet.Rows) {

            sum = 0;
            seq = drChkQty.getValue("SEQ").toString();
            trxQty = Double.parseDouble(drChkQty.getValue("TRX_QTY").toString());

            for (DataRow drPick : dtPickDet.Rows) {
                if (drPick.getValue("SEQ").toString().equals(drChkQty.getValue("SEQ").toString()) &&
                        drPick.getValue("IS_PICKED").equals("Y")) {
                    sum += Double.parseDouble(drPick.getValue("QTY").toString());
                }
            }

            if (sum == 0) {
                Object[] objs = new Object[2];
                objs[0] = pickSheetId;
                objs[1] = seq;

                ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG002024, objs); //WAPG002024 單據[%s],項次[%s]沒有揀料
                return;
            }

            Object[] objs = new Object[4];
            objs[0] = pickSheetId;
            objs[1] = seq;
            objs[2] = sum;
            objs[3] = trxQty;

            switch (dtSheetConfig.Rows.get(0).getValue("ACTUAL_QTY_STATUS").toString()) {

                case "Less":
                case "Equal":
                    if (sum > trxQty) {
                        //WAPG002021 單據[%s],項次[%s],出貨數量[%s]不可大於單據數量[%s]
                        ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG002021, objs);
                        return;
                    }
                    break;

                default:
                    break;

            }
        }

        List<ReleaseOrderObj> alReleaseOrder = new ArrayList<>();
        for (DataRow dr : dtCanShipOrderDet.Rows) {
            ReleaseOrderObj ro = new ReleaseOrderObj();

            ro.setReleaseOrderMstKey(dtCanShipOrderMst.Rows.get(0).getValue("RELEASE_ORDER_MST_KEY").toString());
            ro.setDnId(dtCanShipOrderMst.Rows.get(0).getValue("DN_ID").toString());
            ro.setLicensePlateNum(dtCanShipOrderMst.Rows.get(0).getValue("LICENSE_PLATE_NUM").toString());
            ro.setDriverName(dtCanShipOrderMst.Rows.get(0).getValue("DRIVER_NAME").toString());
            ro.setDriverPhone(dtCanShipOrderMst.Rows.get(0).getValue("DRIVER_PHONE").toString());
            ro.setCustomerKey(dtCanShipOrderMst.Rows.get(0).getValue("CUSTOMER_KEY").toString());
            ro.setDeliveryAddress(dtCanShipOrderMst.Rows.get(0).getValue("DELIVERY_ADDRESS").toString());
            ro.setShipDate(dtCanShipOrderMst.Rows.get(0).getValue("SHIP_DATE").toString());

            ro.setReleaseOrderDetKey(dr.getValue("RELEASE_ORDER_DET_KEY").toString());
            ro.setSeq(dr.getValue("SEQ").toString());
            ro.setItemKey(dr.getValue("ITEM_KEY").toString());
            ro.setLotId(dr.getValue("LOT_ID").toString());
            ro.setStorageKey(dr.getValue("STORAGE_KEY").toString());
            ro.setQty(dr.getValue("QTY").toString());
            ro.setCmt(dr.getValue("CMT").toString());

            alReleaseOrder.add(ro);
        }

        List<CheckCountObj> lstChkCountObj = new ArrayList<>(); // 20220805 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中
        for (PickDetObj pDet : lstPickDet) {
            // region 儲存盤點狀態檢查物件
            CheckCountObj chkCountObjFromBin = new CheckCountObj(); // FROM_BIN
            chkCountObjFromBin.setStorageId(pDet.getStorageId());
            chkCountObjFromBin.setItemId(pDet.getItemId());
            chkCountObjFromBin.setBinId(pDet.getBinId());
            lstChkCountObj.add(chkCountObjFromBin);
            CheckCountObj chkCountObjToBin = new CheckCountObj(); // TO_BIN
            chkCountObjToBin.setStorageId(pDet.getStorageId());
            chkCountObjToBin.setItemId(pDet.getItemId());
            chkCountObjToBin.setBinId(mapSeqTempBin.get(String.valueOf(pDet.getSeq())));
            lstChkCountObj.add(chkCountObjToBin);
            // endregion
        }

        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.PickDetObj", "bmWMS.Library.Param");
        MList mListEnum = new MList(vListEnum);
        String strLsRelatData = mListEnum.generateFinalCode(lstPickDet);

        VirtualClass vListEnum2 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.INV.Parameter.ParameterObj.ReleaseOrderObj", "bmWMS.INV.Param");
        MList mListEnum2 = new MList(vListEnum2);
        String strLsReleaseOrder = mListEnum2.generateFinalCode(alReleaseOrder);

        VirtualClass vListEnum3 = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.CheckCountObj", "bmWMS.Library.Param");
        MList mListEnum3 = new MList(vListEnum3);
        String strCheckCountObj = mListEnum3.generateFinalCode(lstChkCountObj);

        VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create(VirtualClass.VirtualClassType.String);
        MesSerializableDictionary msd = new MesSerializableDictionary(vkey, vVal);
        String strMapSeqTempBin = msd.generateFinalCode(mapSeqTempBin);

        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BDeliveryNoteShip");
        bmObj.setModuleID("BDeliveryNoteShip");
        bmObj.setRequestID("BDeliveryNoteShip");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BStockOutBaseParam.CheckStockOutByObjs); //20220829 archie 將可共用的Param改為出庫Base的Param
        param1.setNetParameterValue(strLsRelatData);
        bmObj.params.add(param1);
        ParameterInfo param2 = new ParameterInfo();
        param2.setParameterID(BDeliveryNoteShipParam.ReleaseOrderObj);
        param2.setNetParameterValue(strLsReleaseOrder);
        bmObj.params.add(param2);
        ParameterInfo param3 = new ParameterInfo();
        param3.setParameterID(BDeliveryNoteShipParam.TempBin);
        param3.setNetParameterValue(strMapSeqTempBin);
        bmObj.params.add(param3);
        ParameterInfo param4 = new ParameterInfo();
        param4.setParameterID(BDeliveryNoteShipParam.Action);
        param4.setParameterValue("CONFIRM");
        bmObj.params.add(param4);
        ParameterInfo param5 = new ParameterInfo();
        param5.setParameterID(BDeliveryNoteShipParam.DnId);
        param5.setParameterValue(dtCanShipOrderMst.Rows.get(0).getValue("DN_ID").toString());
        bmObj.params.add(param5);

        ParameterInfo paramChkFromPda = new ParameterInfo();
        paramChkFromPda.setParameterID(BDeliveryNoteShipParam.FromPda);
        paramChkFromPda.setNetParameterValue2("true");
        bmObj.params.add(paramChkFromPda);

        ParameterInfo paramExecuteChkStock = new ParameterInfo();
        paramExecuteChkStock.setParameterID(BDeliveryNoteShipParam.ExecuteCheckStock); // 20220805 Add by Ikea 是否執行盤點檢查
        paramExecuteChkStock.setNetParameterValue2("true");
        bmObj.params.add(paramExecuteChkStock);
        ParameterInfo paramChkCountObj = new ParameterInfo(); // 20220805 Add by Ikea 傳入物料、儲位、倉庫資料查詢盤點狀態是否為盤點中
        paramChkCountObj.setParameterID(BDeliveryNoteShipParam.CheckCountObj);
        paramChkCountObj.setNetParameterValue(strCheckCountObj);
        bmObj.params.add(paramChkCountObj);

        ParameterInfo paramMode = new ParameterInfo();
        paramMode.setParameterID(BStockOutBaseParam.Mode);
        paramMode.setParameterValue("Ship");
        bmObj.params.add(paramMode);

        ((DeliveryNoteShipWorkNewActivity) getActivity()).CallBModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (!((DeliveryNoteShipWorkNewActivity) getActivity()).CheckBModuleReturnInfo(bModuleReturn))
                    return;

                String holdItemId = "";
                String holdMessage = "";
                String hintMessage = "";

                if (bModuleReturn.getReturnList().get("BDeliveryNoteShip") != null) {

                    if (bModuleReturn.getReturnList().get("BDeliveryNoteShip").get(BDeliveryNoteShipParam.HoldItemID) != null) {
                        holdItemId = bModuleReturn.getReturnList().get("BDeliveryNoteShip").get(BDeliveryNoteShipParam.HoldItemID).toString().replaceAll("\"", "");
                    }

                    if (bModuleReturn.getReturnList().get("BDeliveryNoteShip").get(BDeliveryNoteShipParam.HoldMessage) != null) {
                        holdMessage = bModuleReturn.getReturnList().get("BDeliveryNoteShip").get(BDeliveryNoteShipParam.HoldMessage).toString().replaceAll("\"", "");
                    }

                    if (bModuleReturn.getReturnList().get("BDeliveryNoteShip").get(BDeliveryNoteShipParam.HintMessage) != null) {
                        hintMessage = bModuleReturn.getReturnList().get("BDeliveryNoteShip").get(BDeliveryNoteShipParam.HintMessage).toString().replaceAll("\"", "");
                    }
                }

                String showExtraMsg = "";

                if (hintMessage.length() > 0) {
                    showExtraMsg += ((DeliveryNoteShipWorkNewActivity) getActivity()).getResString(getResources().getString(R.string.ITEM_SPEC_CTRL_HINT_MSG)) + "\n";
                    showExtraMsg += hintMessage + "\n";
                }

                if (holdMessage.length() > 0) {
                    showExtraMsg += ((DeliveryNoteShipWorkNewActivity) getActivity()).getResString(getResources().getString(R.string.ITEM_SPEC_CTRL_HOLD_MSG)) + "\n";
                    showExtraMsg += holdMessage + "\n";
                }

                if (holdItemId.length() > 0) { // 有被鎖定的物料代碼 => 揀貨失敗, 有額外資訊需顯示

                    Object[] args = new Object[1];
                    args[0] = holdItemId;

                    // 揀貨失敗
                    final String finalShowExtraMsg = showExtraMsg;
                    ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG001017, new ShowMessageEvent() {
                        @Override
                        public void onDismiss() {

                            if (finalShowExtraMsg.length() > 0) {

                                ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(finalShowExtraMsg, new ShowMessageEvent() {
                                    @Override
                                    public void onDismiss() {
                                        ((DeliveryNoteShipWorkNewActivity) getActivity()).refreshData();
                                    }
                                });

                            } else {
                                ((DeliveryNoteShipWorkNewActivity) getActivity()).refreshData();
                            }

                        }
                    }, args);

                } else { // 揀貨成功, 但有額外資訊需顯示

                    final String finalShowExtraMsg = showExtraMsg;
                    if (finalShowExtraMsg.length() > 0) {

                        ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG002007, new ShowMessageEvent() {
                            @Override
                            public void onDismiss() {

                                ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(finalShowExtraMsg, new ShowMessageEvent() {
                                    @Override
                                    public void onDismiss() {
                                        ((DeliveryNoteShipWorkNewActivity) getActivity()).refreshData();
                                    }
                                });
                            }
                        });

                    } else {
                        //WAPG002007 作業成功
                        ((DeliveryNoteShipWorkNewActivity) getActivity()).ShowMessage(R.string.WAPG002007, new ShowMessageEvent() {
                            @Override
                            public void onDismiss() {
                                ((DeliveryNoteShipWorkNewActivity) getActivity()).refreshData();
                            }
                        });
                    }
                }
            }
        });

    }

    public void getPlateInfo(DataTable dtOrderMst) {

        dtPlateInfo = null;
        dtPlateInfo = new DataTable();
        dtPlateInfo.addColumn(new DataColumn("LICENSE_PLATE_NUM"));
        dtPlateInfo.addColumn(new DataColumn("DRIVER_NAME"));
        dtPlateInfo.addColumn(new DataColumn("DRIVER_PHONE"));

        for (DataRow drOm : dtOrderMst.Rows) {

            if (drOm.getValue("RELEASE_ORDER_STATUS").toString().equals("Closed"))
                continue;

            DataRow drNew = dtPlateInfo.newRow();
            drNew.setValue("LICENSE_PLATE_NUM", drOm.getValue("LICENSE_PLATE_NUM").toString());
            drNew.setValue("DRIVER_NAME", drOm.getValue("DRIVER_NAME").toString());
            drNew.setValue("DRIVER_PHONE", drOm.getValue("DRIVER_PHONE").toString());
            dtPlateInfo.Rows.add(drNew);
        }

        DataRow drDefaultItem = dtPlateInfo.newRow();
        drDefaultItem.setValue("LICENSE_PLATE_NUM", ""); // 下拉式選單default空白

        if (dtPlateInfo != null && dtPlateInfo.Rows.size() > 0) {
            dtPlateInfo.Rows.add(drDefaultItem);
        }

        lstPlateInfo = (List<? extends Map<String, Object>>) dtPlateInfo.toListHashMap();
        SimpleAdapter adapter = new SimpleArrayAdapter<>(getContext(), lstPlateInfo, android.R.layout.simple_spinner_item, new String[]{"LICENSE_PLATE_NUM", "DRIVER_NAME", "DRIVER_PHONE"}, new int[]{android.R.id.text1, 0, 0});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmbPlate.setAdapter(adapter);
        cmbPlate.setSelection(lstPlateInfo.size() - 1, true);

        return;
    }

    // 將字串移除\r\n後，重新加入new line
    private String split(String str) {

        String result = "";

        if (str != null && str.length() > 0) {

            String delim = "\\\\r\\\\n";
            String splitArray[] = str.split(delim);
            for (int i = 0; i < splitArray.length; i++) {
                result += splitArray[i] + System.getProperty("line.separator");
            }

        }

        return result;
    }
//    private void showDialog() {
//        LayoutInflater inflater = LayoutInflater.from(getActivity());
//        View view = inflater.inflate(R.layout.activity_wms_delivery_note_ship_work_select_bin, null);
//        final Spinner cmbBin = view.findViewById(R.id.cmbBinID);
//        Button btnConfirm = view.findViewById(R.id.btnBinConfirm);
//
//        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setView(view);
//
//        final AlertDialog dialog = builder.create();
//        dialog.setCancelable(false);
//        dialog.show();
//
//        BModuleObject bimObj = new BModuleObject();
//        bimObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
//        bimObj.setModuleID("BIFetchBin");
//        bimObj.setRequestID("BIFetchBin");
//        bimObj.params = new Vector<ParameterInfo>();
//
//        ParameterInfo param1 = new ParameterInfo();
//        param1.setParameterID(BIWMSFetchInfoParam.Filter);
//        param1.setParameterValue(String.format(" AND STORAGE_ID = '%s' AND B.BIN_TYPE = 'OS'", dtDnPickDet.Rows.get(0).getValue("STORAGE_ID").toString()));
//        bimObj.params.add(param1);
//
//        ((DeliveryNoteShipWorkNewActivity)getActivity()).CallBIModule(bimObj, new WebAPIClientEvent() {
//            @Override
//            public void onPostBack(BModuleReturn bModuleReturn) {
//                if(((DeliveryNoteShipWorkNewActivity)getActivity()).CheckBModuleReturnInfo(bModuleReturn)){
//                    DataTable dtBin = bModuleReturn.getReturnJsonTables().get("BIFetchBin").get("BIN");
//
//                    ArrayList<String> alBin = new ArrayList<String>();
//                    for(int i = 0; i < dtBin.Rows.size(); i++)
//                    {
//                        String binId = dtBin.Rows.get(i).getValue("BIN_ID").toString();
//                        if(!alBin.contains(binId))
//                            alBin.add(binId);
//                    }
//                    ArrayAdapter<String> adapterBin = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, alBin);
//                    cmbBin.setAdapter(adapterBin);
//                }
//            }
//        });
//
//        btnConfirm.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(cmbBin.getSelectedItem().toString().equals("")){
//                    ((DeliveryNoteShipWorkNewActivity)getActivity()).ShowMessage(R.string.WAPG002023); //WAPG002023    請選擇儲位
//                    return;
//                }
//                dialog.dismiss();
//                ExceteDeliveryNoteShip(cmbBin.getSelectedItem().toString());
//            }
//        });
//    }

}
