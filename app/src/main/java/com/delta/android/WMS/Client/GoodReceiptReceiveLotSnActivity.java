package com.delta.android.WMS.Client;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;
import com.delta.android.WMS.Client.GridAdapter.GoodReceiptReceiveLotAdapter;

import java.util.HashMap;

public class GoodReceiptReceiveLotSnActivity extends BaseFlowActivity {

    // private variable
    private static int requestCode = 123; //需要接收回傳資訊時使用
    private ViewHolder holder = null;
    private DataTable GrMstTable;// get p.2
    private HashMap GrDetMap;    // get p.2
    private DataTable GrLotTable;// get p.2
    private DataTable GrLotTableAll;// get p.2
    private DataTable GrrSnTable;// get p.2
    private String ShCfg;// get p.2
    private String RegType;// get p.2
    private String DetItemTotalQty;// get p.2
    private String strSkipQC;// get p.2
    double receiveCount;

    static class ViewHolder
    {
        // 宣告控制項物件
        TextView GrId;
        TextView ItemId;
        TextView ItemTotalQty;
        ListView GrLotData;
        Button BtnAddLot;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_good_receipt_receive_lot_sn);

        this.initialData();
        // 設定監聽事件
        setListensers();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent){
        super.onActivityResult(requestCode, resultCode, dataIntent);
        gotoPreviousActivity(GoodReceiptReceiveDetailActivity.class);
    }

    private void initialData() {
        GrMstTable =  (DataTable)getIntent().getSerializableExtra("MstTable");
        GrDetMap = new HashMap();
        GrDetMap = (HashMap) getIntent().getSerializableExtra("DetRow");
        GrLotTable =  (DataTable)getIntent().getSerializableExtra("DetLotTable");
        GrLotTableAll = (DataTable)getIntent().getSerializableExtra("LotTableAll");
        GrrSnTable =  (DataTable)getIntent().getSerializableExtra("DetLotSnTable");
        ShCfg = getIntent().getStringExtra("ActualQtyStatus");
        RegType = getIntent().getStringExtra("RegType");
        DetItemTotalQty = getIntent().getStringExtra("DetItemTotalQty");
        strSkipQC = getIntent().getStringExtra("strSkipQC");

        if (holder==null) holder = new ViewHolder();
        // 取得控制項物件
        holder.GrId = findViewById(R.id.tvGrId);
        holder.ItemId = findViewById(R.id.tvGrDetItemId);
        holder.ItemTotalQty = findViewById(R.id.tvGrDetItemTotalQty);
        holder.GrLotData = findViewById(R.id.lvGrDetLotData);
        holder.BtnAddLot = findViewById(R.id.btnAddLot);

        holder.GrId.setText(GrDetMap.get("GR_ID").toString());
        holder.ItemId.setText(GrDetMap.get("ITEM_ID").toString());
        receiveCount = 0;
        for (DataRow dr : GrLotTable.Rows) receiveCount += Double.parseDouble(dr.getValue("QTY").toString());
        holder.ItemTotalQty.setText(String.format("%s/%s", String.valueOf(receiveCount), DetItemTotalQty));

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        GoodReceiptReceiveLotAdapter adapter = new GoodReceiptReceiveLotAdapter(GrLotTable, inflater);
        holder.GrLotData.setAdapter(adapter);
    }

    //設定監聽事件
    private void setListensers()
    {
        holder.GrLotData.setOnItemClickListener(ModifyLot);
        holder.BtnAddLot.setOnClickListener(AddNewLot);
    }

    // region 事件

    private AdapterView.OnItemClickListener ModifyLot = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            modifyLot(position);
        }
    };

    private View.OnClickListener AddNewLot = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(RegType.equals("ItemID") && GrLotTable.Rows.size() != 0)
            {
                modifyLot(0);
            }
            else
            {
                DataTable LotSn = new DataTable();
                LotSn.getColumns().addAll(GrrSnTable.getColumns());

                Bundle chooseGrLot = new Bundle();
                chooseGrLot.putString("Type","Add");
                chooseGrLot.putString("StorageId",GrDetMap.get("STORAGE_ID").toString());
                chooseGrLot.putString("ItemId",GrDetMap.get("ITEM_ID").toString());
                chooseGrLot.putString("Seq",GrDetMap.get("SEQ").toString());
                chooseGrLot.putString("LotId",GrDetMap.get("LOT_ID").toString());
                chooseGrLot.putSerializable("MstTable", GrMstTable);
                chooseGrLot.putSerializable("DetLotTable", GrLotTable);
                chooseGrLot.putSerializable("DetLotTableAll", GrLotTableAll);
                chooseGrLot.putSerializable("DetLotSnTable", LotSn);
                chooseGrLot.putSerializable("DetLotSnTableAll", GrrSnTable);
                chooseGrLot.putString("ActualQtyStatus",ShCfg);
                chooseGrLot.putString("RegType",RegType);
                chooseGrLot.putString("DetItemTotalQty",DetItemTotalQty);
                chooseGrLot.putDouble("DetReceiveItemQty",receiveCount);
                chooseGrLot.putString("strSkipQC",strSkipQC);
                //gotoNextActivity(GoodReceiptReceiveLotSnModifyActivity.class, chooseGrLot);
                Intent intent = new Intent(GoodReceiptReceiveLotSnActivity.this, GoodReceiptReceiveLotSnModifyActivity.class);
                intent.putExtras(chooseGrLot);
                startActivityForResult(intent, requestCode);
            }
        }
    };

    private void modifyLot(int position)
    {
        DataTable dtChooseLot = new DataTable();
        dtChooseLot.Rows.add(GrLotTable.Rows.get(position));
        dtChooseLot.getColumns().addAll(GrLotTable.getColumns());
        DataTable dtChooseLotSn = new DataTable();
        for (DataRow dr: GrrSnTable.Rows)
        {
            if (dr.getValue("GRR_DET_SN_REF_KEY").toString().equals(GrLotTable.Rows.get(position).getValue("GRR_DET_SN_REF_KEY").toString()))
            {
                dtChooseLotSn.Rows.add(dr);
            }
        }
        dtChooseLotSn.getColumns().addAll(GrrSnTable.getColumns());
        Bundle chooseGrLot = new Bundle();
        chooseGrLot.putString("Type","Modify");
        chooseGrLot.putString("LotId", GrDetMap.get("LOT_ID").toString());
        chooseGrLot.putSerializable("MstTable", GrMstTable);
        chooseGrLot.putSerializable("DetLotTable", dtChooseLot);
        chooseGrLot.putSerializable("DetLotTableAll", GrLotTableAll);
        chooseGrLot.putSerializable("DetLotSnTable", dtChooseLotSn);
        chooseGrLot.putSerializable("DetLotSnTableAll", GrrSnTable);
        chooseGrLot.putString("ActualQtyStatus",ShCfg);
        chooseGrLot.putString("RegType",RegType);
        chooseGrLot.putString("DetItemTotalQty",DetItemTotalQty);
        chooseGrLot.putDouble("DetReceiveItemQty",receiveCount);
        chooseGrLot.putString("strSkipQC",strSkipQC);
        //gotoNextActivity(GoodReceiptReceiveLotSnModifyActivity.class, chooseGrLot);
        Intent intent = new Intent(GoodReceiptReceiveLotSnActivity.this, GoodReceiptReceiveLotSnModifyActivity.class);
        intent.putExtras(chooseGrLot);
        startActivityForResult(intent, requestCode);
    }

    //endregion

}
