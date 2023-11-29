package com.delta.android.WMS.Client;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
import com.delta.android.WMS.Param.BIWMSFetchInfoParam;
import com.delta.android.WMS.Param.ParamObj.Condition;
import com.delta.android.WMS.Param.BDeliveryNoteShipParam;
import com.delta.android.WMS.Param.ParamObj.PickDetObj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class DeliveryNoteShipActivity extends BaseFlowActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_delivery_note_ship_detail);

        // 取得控制項物件
        initViews();

        // 設定監聽事件
        setListensers();
    }

    // 宣告控制項物件
    private ImageButton btnSearch;
    private EditText txtDnID;
    private ListView listView;
    private Button btnOk;
    private DataTable dtDet = new DataTable();

    //取得控制項物件
    private void initViews()
    {
        btnSearch = findViewById(R.id.btnSearch);
        txtDnID = findViewById(R.id.txtDnID);
        listView = findViewById(R.id.listViewDet);
        btnOk = findViewById(R.id.btnOk);
    }

    //設定監聽事件
    private void setListensers()
    {
        btnSearch.setOnClickListener(GetDetail);
        btnOk.setOnClickListener(OK);
        txtDnID.setOnKeyListener(DnIDOnKey);  //20200729 archie 按下Enter也可以做搜尋
        //btnSearch.setOnClickListener(GetMaster);
        //listView.setOnItemClickListener(GetDetail);
    }

    private View.OnKeyListener DnIDOnKey = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            //只有按下Enter才會反映
            if (keyCode != KeyEvent.KEYCODE_ENTER) return false;
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                GetDetail();
                return true;
            }
            return false;
        }
    };

    private View.OnClickListener GetDetail = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            GetDetail();
        }
    };

    /*private AdapterView.OnItemClickListener GetDetail = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent();
                intent.setClass(DeliveryNoteShipActivity.this,DeliveryNoteShipDetailActivity.class);
                Bundle bundle=new Bundle();
                bundle.putSerializable("DataTable", dtDet); // 直接傳DataTable -> PageTwo會Crash
                bundle.putString("DN_ID",txtDnID.getText().toString().trim());
                intent.putExtras(bundle);
                startActivity(intent);
            }
    };

    private void GetMasterInfo()
    {
        if (txtDnID.getText().toString().trim().equals(""))
        {
            ShowMessage("WCPG005001");
            return;
        }

        //region Call BIModule
        // BIModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchDeliveryNoteMaintain");
        bmObj.setRequestID("FetchDeliveryNoteMaintain");
        bmObj.params = new Vector<ParameterInfo>();

        BModuleObject bmPickObj = new BModuleObject();
        bmPickObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmPickObj.setModuleID("BIFetchDnPickMstAndDet");
        bmPickObj.setRequestID("FetchDnPickDet");
        bmPickObj.params = new Vector<ParameterInfo>();

        // Set Condition
        List<Condition> lstCondition = new ArrayList<Condition>();
        Condition condition = new Condition();
        condition.setAliasTable("MST");
        condition.setColumnName("DN_ID");
        condition.setValue(txtDnID.getText().toString().trim());
        condition.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
        lstCondition.add(condition);

        HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
        mapCondition.put(condition.getColumnName(),lstCondition);
        VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.INV.Parameter.ParameterObj.Condition", "bmWMS.INV.Param");
        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vkey, vVal);
        String strCond = msdl.generateFinalCode(mapCondition);

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Condition);
        param1.setNetParameterValue(strCond); // 要用set"Net"ParameterValue
        bmObj.params.add(param1);
        bmPickObj.params.add(param1);

        List<BModuleObject> lstBmObj = new ArrayList<BModuleObject>();
        lstBmObj.add(bmObj);
        lstBmObj.add(bmPickObj);

        CallBIModule(lstBmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    DataTable dt = bModuleReturn.getReturnJsonTables().get("FetchDeliveryNoteMaintain").get("DnMst");
                    DataTable dtPickMst = bModuleReturn.getReturnJsonTables().get("FetchDnPickDet").get("DnMst");
                    dtDet = bModuleReturn.getReturnJsonTables().get("FetchDnPickDet").get("DnPick");

                    //check select sheet type
                    if (dt == null || dt.Rows.size() ==0)
                    {
                        ShowMessage("WCPG011002");//WCPG011002查無單據資料
                        return;
                    }

                    if (!dt.Rows.get(0).getValue("DN_STATUS").toString().equals("Confirmed"))
                    {
                        ShowMessage("WCPG011003");//WCPG011003  單據未確認
                        return;
                    }

                    if (dtPickMst == null || dtPickMst.Rows.size() ==0)
                    {
                        ShowMessage("WCPG011004");//WCPG011004  查無揀貨資料
                        return;
                    }

                    if (dt.Rows.get(0).getValue("SHIP_QC_CONFIRM").toString().equals("Y"))
                    {
                        if (!dtPickMst.Rows.get(0).getValue("DN_STATUS").toString().equals("QCConfirmed"))
                        {
                            ShowMessage("WCPG011005");//WCPG011005  出通單所綁定的揀貨狀態是不為QCConfirmed
                            return;
                        }
                    }
                    else//不需經QC作確認
                    {
                        //則判斷出通單綁定的揀貨狀態是否為Picked
                        if (!dtPickMst.Rows.get(0).getValue("PICK_STATUS").toString().equals("Picked"))
                        {
                            ShowMessage("WCPG011006");//WCPG011006  出通單綁定的揀貨狀態不為Picked
                            return;
                        }
                    }

                    List<HashMap<String , String>> list = new ArrayList<>();
                    ArrayList<String> listSheetId = new ArrayList<String>();
                    ArrayList<String> listCreateDate = new ArrayList<String>();
                    Iterator it =  dt.Rows.iterator();
                    int i = 0;
                    while (it.hasNext())
                    {
                        DataRow row = (DataRow) it.next();
                        listSheetId.add( i,row.getValue("DN_ID").toString());
                        listCreateDate.add( i,row.getValue("CREATE_DATE").toString());
                        i++;
                    }
                    for (int j =0; j<listSheetId.size();j++)
                    {
                        HashMap<String , String> hashMap = new HashMap<>();
                        hashMap.put("DN_ID" , listSheetId.get(j));
                        hashMap.put("CREATE_DATE" , listCreateDate.get(j));
                        //把title , text存入HashMap之中
                        list.add(hashMap);
                    }
                    //Toast.makeText(TestActivity_YenChen.this,"Done~~~", Toast.LENGTH_LONG).show();
                    ListAdapter adapter = new SimpleAdapter(
                            DeliveryNoteShipActivity.this,
                            list,
                            android.R.layout.simple_list_item_2,
                            new String[]{"DN_ID","CREATE_DATE"},
                            new int[]{android.R.id.text1,android.R.id.text2}
                    );
                    listView.setAdapter(adapter);
                }
            }
        });
    }*/

    private void GetDetail()
    {
        if (txtDnID.getText().toString().trim().equals(""))
        {
            ShowMessage(R.string.WAPG002001);//WAPG002001  請輸入單據代碼
            return;
        }

        //Call BIModule
        //取得通貨通資單資訊
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmObj.setModuleID("BIFetchDeliveryNoteMaintain");
        bmObj.setRequestID("FetchDeliveryNoteMaintain");
        bmObj.params = new Vector<ParameterInfo>();

        //取得揀貨資訊
        BModuleObject bmPickObj = new BModuleObject();
        bmPickObj.setBModuleName("Unicom.Uniworks.BModule.WMS.Library.BIWMSFetchInfo");
        bmPickObj.setModuleID("BIFetchDnPickMstAndDet");
        bmPickObj.setRequestID("FetchDnPickDet");
        bmPickObj.params = new Vector<ParameterInfo>();

        // Set Condition
        List<Condition> lstCondition = new ArrayList<Condition>();
        Condition condition = new Condition();
        condition.setAliasTable("MST");
        condition.setColumnName("DN_ID");
        condition.setValue(txtDnID.getText().toString().toUpperCase().trim()); //20200729 archie 轉大寫
        condition.setDataType(VirtualClass.create(VirtualClass.VirtualClassType.String).getClassName());
        lstCondition.add(condition);

        HashMap<String, List<?>> mapCondition = new HashMap<String, List<?>>();
        mapCondition.put(condition.getColumnName(),lstCondition);
        VirtualClass vkey = VirtualClass.create(VirtualClass.VirtualClassType.String);
        VirtualClass vVal = VirtualClass.create("Unicom.Uniworks.BModule.WMS.Library.Parameter.ParameterObj.Condition", "bmWMS.Library.Param");
        MesSerializableDictionaryList msdl = new MesSerializableDictionaryList(vkey, vVal);
        String strCond = msdl.generateFinalCode(mapCondition);

        ParameterInfo param1 = new ParameterInfo();
        param1.setParameterID(BIWMSFetchInfoParam.Condition);
        param1.setNetParameterValue(strCond); // 要用set"Net"ParameterValue
        bmObj.params.add(param1);
        bmPickObj.params.add(param1);

        List<BModuleObject> lstBmObj = new ArrayList<BModuleObject>();
        lstBmObj.add(bmObj);
        lstBmObj.add(bmPickObj);

        CallBIModule(lstBmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    DataTable dt = bModuleReturn.getReturnJsonTables().get("FetchDeliveryNoteMaintain").get("DnMst");
                    DataTable dtPickMst = bModuleReturn.getReturnJsonTables().get("FetchDnPickDet").get("DnMst");
                    dtDet = bModuleReturn.getReturnJsonTables().get("FetchDnPickDet").get("DnPick");

                    //Check Data
                    if (dt == null || dt.Rows.size() ==0)
                    {
                        ShowMessage(R.string.WAPG002002);//WAPG002002  查無單據資料
                        txtDnID.selectAll();
                        listView.setAdapter(null);
                        return;
                    }

                    if (!dt.Rows.get(0).getValue("DN_STATUS").toString().equals("Confirmed"))
                    {
                        ShowMessage(R.string.WAPG002003);//WAPG002003  單據未確認
                        txtDnID.selectAll();
                        listView.setAdapter(null);
                        return;
                    }

                    if (dtPickMst == null || dtPickMst.Rows.size() ==0)
                    {
                        ShowMessage(R.string.WAPG002004);//WAPG002004  查無揀貨資料
                        txtDnID.selectAll();
                        listView.setAdapter(null);
                        return;
                    }

                    if (dt.Rows.get(0).getValue("SHIP_QC_CONFIRM").toString().equals("Y"))
                    {
                        if (!dtPickMst.Rows.get(0).getValue("PICK_STATUS").toString().equals("QCConfirmed"))
                        {
                            ShowMessage(R.string.WAPG002005);//WAPG002005  出通單所綁定的揀貨狀態是不為QCConfirmed
                            txtDnID.selectAll();
                            listView.setAdapter(null);
                            return;
                        }
                    }
                    else//不需經QC作確認
                    {
                        //則判斷出通單綁定的揀貨狀態是否為Picked
                        if (!dtPickMst.Rows.get(0).getValue("PICK_STATUS").toString().equals("Picked"))
                        {
                            ShowMessage(R.string.WAPG002006);//WAPG002006  出通單綁定的揀貨狀態不為Picked
                            txtDnID.selectAll();
                            listView.setAdapter(null);
                            return;
                        }
                    }

                    List<HashMap<String , String>> list = new ArrayList<>();
                    ArrayList<String> listSeq = new ArrayList<String>();
                    ArrayList<String> listItemId = new ArrayList<String>();
                    ArrayList<String> listItemName = new ArrayList<String>();
                    ArrayList<String> listLotId = new ArrayList<String>();
                    ArrayList<String> listQty = new ArrayList<String>();
                    Iterator it =  dtDet.Rows.iterator();
                    int i = 0;
                    while (it.hasNext())
                    {
                        DataRow row = (DataRow) it.next();
                        listSeq.add( i,row.getValue("SEQ").toString());
                        listItemId.add( i,row.getValue("ITEM_ID").toString());
                        listItemName.add( i,row.getValue("ITEM_NAME").toString());
                        listLotId.add( i,row.getValue("LOT_ID").toString());
                        listQty.add( i,row.getValue("QTY").toString());
                        i++;
                    }
                    for (int j =0; j<listItemId.size();j++)
                    {
                        HashMap<String , String> hashMap = new HashMap<>();
                        hashMap.put("SEQ" , listSeq.get(j));
                        hashMap.put("ITEM_ID" , listItemId.get(j));
                        hashMap.put("ITEM_NAME" , listItemName.get(j));
                        hashMap.put("LOT_ID" , listLotId.get(j));
                        hashMap.put("QTY" , listQty.get(j));
                        //把title , text存入HashMap之中
                        list.add(hashMap);
                    }

                    ListAdapter adapter = new SimpleAdapter(
                            DeliveryNoteShipActivity.this,
                            list,
                            R.layout.activity_wms_delivery_note_ship_detail_listview,
                            new String[]{"SEQ","ITEM_ID","ITEM_NAME","LOT_ID","QTY"},
                            new int[]{R.id.txtSEQ,R.id.txtItemId,R.id.txtItemName,R.id.txtLotId,R.id.txtQty}
                    );
                    listView.setAdapter(adapter);
                }
            }
        });
    }

    private View.OnClickListener OK = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            if (listView.getCount() <= 0) return;
            //ShowMessage("單據編號: "+"是否確定出庫?");
            ShowDialog("單據編號: "+txtDnID.getText().toString().trim()+", 是否確定出庫?");
        }
    };

    private void ShowDialog(String Message)
    {
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View view = inflater.inflate(R.layout.style_core_dialog_exit, null);
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);
        tvMessage.setText(Message);

        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(DeliveryNoteShipActivity.this);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);//只允許按下dialog裡的按鍵才能關閉dialog
        dialog.show();

        Button btnCloseDialog = view.findViewById(R.id.btnCancel);
        btnCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button btnConfirm = view.findViewById(R.id.btnOk);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                ExecutoProcess("DeliveryNoteShip");
            }
        });
    }

    //執行單據變更(出貨)
    private void ExecutoProcess(String trxType)
    {
        List<PickDetObj> lstPickDetObj = new ArrayList<PickDetObj>();

        for (int i = 0 ;i<dtDet.Rows.size();i++)
        {
            PickDetObj pickDetObj = new PickDetObj();

            pickDetObj.setSheetId(txtDnID.getText().toString().trim());
            pickDetObj.setSeq(Double.valueOf(dtDet.Rows.get(i).getValue("SEQ").toString()));
            pickDetObj.setItemId(dtDet.Rows.get(i).getValue("ITEM_ID").toString());
            pickDetObj.setLotId(dtDet.Rows.get(i).getValue("LOT_ID").toString());
            pickDetObj.setQty(Double.valueOf(dtDet.Rows.get(i).getValue("QTY").toString()));
            pickDetObj.setStorageId(dtDet.Rows.get(i).getValue("STORAGE_ID").toString());
            pickDetObj.setBinId(dtDet.Rows.get(i).getValue("BIN_ID").toString());
            pickDetObj.setUom(dtDet.Rows.get(i).getValue("UOM").toString());
            lstPickDetObj.add(pickDetObj);
        }

        // Add param
        VirtualClass vListEnum = VirtualClass.create("Unicom.Uniworks.BModule.WMS.INV.Parameter.ParameterObj.PickDetObj", "bmWMS.INV.Param");
        MList mListEnum = new MList(vListEnum);
        String strLsRelatData = mListEnum.generateFinalCode(lstPickDetObj);

        // Call BModule
        BModuleObject bmObj = new BModuleObject();
        bmObj.setBModuleName("Unicom.Uniworks.BModule.WMS.INV.BDeliveryNoteShip");
        bmObj.setModuleID("");
        bmObj.setRequestID("BDeliveryNoteShip");
        bmObj.params = new Vector<ParameterInfo>();

        ParameterInfo param = new ParameterInfo();
        param.setParameterID(BDeliveryNoteShipParam.PickDetObj);
        param.setNetParameterValue(strLsRelatData);
        bmObj.params.add(param);

        CallBModule(bmObj, new WebAPIClientEvent() {
            @Override
            public void onPostBack(BModuleReturn bModuleReturn) {
                if (CheckBModuleReturnInfo(bModuleReturn)) {
                    ShowMessage(R.string.WAPG002007);//WAPG002007  作業成功
                    txtDnID.selectAll();
                    listView.setAdapter(null);
                }
            }
        });
    }
}

//Error Code
//WAPG002001    請輸入單據代碼
//WAPG002002    查無單據資料
//WAPG002003    單據未確認
//WAPG002004    查無揀貨資料
//WAPG002005    出通單所綁定的揀貨狀態是不為QCConfirmed
//WAPG002006    出通單綁定的揀貨狀態不為Picked
//WAPG002007    作業成功