package com.delta.android.WMS.Client;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.Core.GenerateJsonNetString.MList;
import com.delta.android.Core.GenerateJsonNetString.VirtualClass;
import com.delta.android.Core.WebApiClient.BModuleObject;
import com.delta.android.Core.WebApiClient.BModuleReturn;
import com.delta.android.Core.WebApiClient.ParameterInfo;
import com.delta.android.Core.WebApiClient.WebAPIClientEvent;
import com.delta.android.R;
import com.delta.android.WMS.Param.BDeliveryNoteShipParam;
import com.delta.android.WMS.Param.ParamObj.PickDetObj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class DeliveryNoteShipDetailActivity extends BaseFlowActivity {

    // 宣告控制項物件
    private ListView lstDetail;
    private Button btnOk;
    private Button btnBack;

    // private variable
    DataTable dtDet = new DataTable();
    String ID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_delivery_note_ship_detail);

        // Get Detail table From DeliveryNoteShipDetail.activity
        dtDet = (DataTable) getIntent().getSerializableExtra("DataTable");
        Bundle bundle = getIntent().getExtras();
        ID = bundle.getString("DN_ID");

        // 取得控制項物件
        initViews();
        // 設定監聽事件
        setListensers();
    }

    //取得控制項物件
    private void initViews()
    {
        lstDetail= findViewById(R.id.listViewDet);
        btnOk = findViewById(R.id.btnOk);
        //btnBack = findViewById(R.id.btnBack);
        // 取得物件後才塞入Detail
        GetDetail();
    }

    //設定監聽事件
    private void setListensers()
    {
        btnOk.setOnClickListener(OK);
        btnBack.setOnClickListener(Back);
    }

    // region 事件

    private View.OnClickListener OK = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            //ShowMessage("單據編號: "+"是否確定出庫?");
            ShowDialog("單據編號: "+ID+", 是否確定出庫?");
        }
    };

    private View.OnClickListener Back = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            gotoPreviousActivity(DeliveryNoteShipActivity.class);
        }
    };

    // endregion

    // region Private Function

    private void GetDetail()
    {
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
                DeliveryNoteShipDetailActivity.this,
                list,
                R.layout.activity_wms_delivery_note_ship_detail_listview,
                new String[]{"SEQ","ITEM_ID","ITEM_NAME","LOT_ID","QTY"},
                new int[]{R.id.txtSEQ,R.id.txtItemId,R.id.txtItemName,R.id.txtLotId,R.id.txtQty}
        );
        lstDetail.setAdapter(adapter);
    }

    private void ShowDialog(String Message)
    {
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View view = inflater.inflate(R.layout.style_core_dialog_exit, null);
        TextView tvMessage = view.findViewById(R.id.tvDialogMessage);
        tvMessage.setText(Message);

        final AlertDialog.Builder builder = new AlertDialog.Builder(DeliveryNoteShipDetailActivity.this);
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
                //Toast.makeText(DeliveryNoteShipDetailActivity.this,"Done~~~", Toast.LENGTH_LONG).show();
                ExecutoProcess("DeliveryNoteShip");
            }
        });
    }

    private void ExecutoProcess(String trxType)
    {
        List<PickDetObj> lstPickDetObj = new ArrayList<PickDetObj>();

        for (int i = 0 ;i<dtDet.Rows.size();i++)
        {
            PickDetObj pickDetObj = new PickDetObj();

            pickDetObj.setSheetId(ID);
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
                }
            }
        });
    }
}
