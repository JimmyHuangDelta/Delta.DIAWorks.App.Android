package com.delta.android.WMS.Client;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.Core.DataTable.DataRow;
import com.delta.android.Core.DataTable.DataTable;
import com.delta.android.R;
import com.delta.android.WMS.Client.GridAdapter.WarehouseStorageLotAdapter;

import java.math.BigDecimal;

public class WarehouseStorageStockInActivity extends BaseFlowActivity {

    private ViewHolder holder = null;
    private String strSheetPolicyId;
    private DataTable dtDet, dtLotAll;
    private int lotPos;
    private String strSheetId, strItemId, strQty, strScrapQty;
    private boolean blnIsWV;
    private String detailMode;

    static class ViewHolder
    {
        // 宣告控制項物件
        TextView tvSheetId;
        TextView tvItemId;
        TextView tvQty;
        TextView tvScrapQty, tvScrapQtyTitle;
        ListView lvLotData;
        Button btnAddLot;
        Button btnConfirm;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wms_warehouse_storage_stock_in);

        this.initialData();

        this.setListeners();
    }

    @Override
    public void onBackPressed() {
        //如果要回傳則需要寫此方法
        Bundle inputData = new Bundle();
        inputData.putSerializable("dtLotData", dtLotAll);

        Intent intent = new Intent();
        intent.putExtras(inputData);
        setResult(1, intent);

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Bundle inputData = new Bundle();
                inputData.putSerializable("dtLotData", dtLotAll);

                Intent intent = new Intent();
                intent.putExtras(inputData);
                setResult(1, intent);

                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initialData() {

        detailMode = "";
        strSheetPolicyId = getIntent().getStringExtra("sheetPolicyId");
        dtDet = (DataTable) getIntent().getSerializableExtra("dtDet");
        dtLotAll = (DataTable) getIntent().getSerializableExtra("dtLotAll");
        lotPos = getIntent().getIntExtra("lotPos", 0);
        blnIsWV = getIntent().getBooleanExtra("blnIsWV", false);
        strSheetId = (String) getIntent().getSerializableExtra("sheetId");
        strItemId = (String) getIntent().getSerializableExtra("itemId");
        strQty = (String) getIntent().getSerializableExtra("qty");
        strScrapQty = (String) getIntent().getSerializableExtra("scrapQty");

        if (holder == null)
            holder = new ViewHolder();
        holder.tvSheetId = findViewById(R.id.tvSheetId);
        holder.tvItemId = findViewById(R.id.tvDetItemId);
        holder.tvQty = findViewById(R.id.tvDetItemTotalQty);
        holder.tvScrapQtyTitle = findViewById(R.id.tvDetItemScrapQty2);
        holder.tvScrapQty = findViewById(R.id.tvDetItemScrapQty);
        holder.lvLotData = findViewById(R.id.lvDetLotData);
        holder.btnAddLot = findViewById(R.id.btnAddLot);
        holder.btnConfirm = findViewById(R.id.btnConfirm);

        holder.tvSheetId.setText(strSheetId);
        holder.tvItemId.setText(strItemId);
        holder.tvQty.setText(strQty);

        if (blnIsWV) {
            holder.tvScrapQtyTitle.setVisibility(View.VISIBLE);
            holder.tvScrapQty.setVisibility(View.VISIBLE);
            holder.tvScrapQty.setText(strScrapQty);
        } else {
            holder.tvScrapQtyTitle.setVisibility(View.GONE);
            holder.tvScrapQty.setVisibility(View.GONE);
        }

        getLotData(dtLotAll);

    }

    private void setListeners() {
        holder.lvLotData.setOnItemClickListener(modifyLot);
        holder.lvLotData.setOnItemLongClickListener(deleteLot);
        holder.btnAddLot.setOnClickListener(addNewLot);
        holder.btnConfirm.setOnClickListener(confirmLot);
    }

    // region 事件

    private AdapterView.OnItemLongClickListener deleteLot = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long id) {

            final DataTable dtCurrentLot = new DataTable();
            if (dtLotAll != null && dtLotAll.Rows.size() > 0) {
                String detSeq = dtDet.Rows.get(lotPos).getValue("SEQ").toString();
                for (DataRow dr : dtLotAll.Rows) {
                    if (detSeq.equals(dr.getValue("SEQ"))) {
                        DataRow drLot = dr;
                        dtCurrentLot.Rows.add(drLot);
                    }
                }
            }
            final DataRow drSelectedRow = dtCurrentLot.Rows.get(pos);

            //region 跳出詢問視窗
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            final View deleteView = inflater.inflate(R.layout.activity_wms_warehouse_storage_stock_in_delete,null );
            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(WarehouseStorageStockInActivity.this);
            builder.setView(deleteView);

            final android.app.AlertDialog deleteDialog = builder.create();
            deleteDialog.setCancelable(false);//只允許按下dialog裡的按鍵才能關閉dialog
            deleteDialog.show();

            TextView tvCurrentSN = deleteDialog.findViewById(R.id.tvDialogMessage);
            String strErr = getResources().getString(R.string.WAPG027027); // WAPG027027   是否刪除此批號?
            tvCurrentSN.setText(strErr);
            //endregion

            //region 確認清除
            Button btnDelete = deleteView.findViewById(R.id.btnYes);
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dtLotAll.Rows.remove(drSelectedRow);
                    getLotData(dtLotAll);
                    deleteDialog.dismiss();
                }
            });
            //endregion

            //region 取消清除
            Button btnCancel = deleteView.findViewById(R.id.btnNo);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteDialog.dismiss();
                }
            });
            //endregion

            return true;
        }
    };

    private AdapterView.OnItemClickListener modifyLot = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {

            detailMode = "Modify";

            DataTable dtCurrentLot = new DataTable();
            if (dtLotAll != null && dtLotAll.Rows.size() > 0) {
                String detSeq = dtDet.Rows.get(lotPos).getValue("SEQ").toString();
                for (DataRow dr : dtLotAll.Rows) {
                    if (detSeq.equals(dr.getValue("SEQ"))) {
                        DataRow drLot = dr;
                        dtCurrentLot.Rows.add(drLot);
                    }
                }
            }

            final DataRow drSelectedRow = dtCurrentLot.Rows.get(pos);

            Bundle sheetInfo = new Bundle();
            sheetInfo.putString("detailMode", detailMode);
            sheetInfo.putBoolean("blnIsWV", blnIsWV);
            sheetInfo.putString("sheetPolicyId", strSheetPolicyId);
            sheetInfo.putSerializable("dtDet", dtDet);
            sheetInfo.putInt("lotPos", lotPos);
            sheetInfo.putSerializable("dtLotAll", dtLotAll);
            sheetInfo.putString("lotId", dtCurrentLot.Rows.get(pos).getValue("LOT_ID").toString());
            sheetInfo.putSerializable("qty", dtCurrentLot.Rows.get(pos).getValue("QTY").toString());
            sheetInfo.putSerializable("scrapQty", dtCurrentLot.Rows.get(pos).getValue("SCRAP_QTY").toString());
            sheetInfo.putSerializable("mfgDate", dtCurrentLot.Rows.get(pos).getValue("MFG_DATE").toString());
            sheetInfo.putSerializable("expDate", dtCurrentLot.Rows.get(pos).getValue("EXP_DATE").toString());

            gotoNextActivityForResult(WarehouseStorageStockInModifyActivity.class, sheetInfo, new OnActivityResult(){
                @Override
                public void onResult(Bundle bundle) {

                    String drSeq = bundle.getString("drSeq"); //getIntent().getStringExtra("drSeq");
                    String drStorageId = bundle.getString("drStorageId");
                    String drItemId = bundle.getString("drItemId");
                    String inputLotId = bundle.getString("inputLotId");
                    BigDecimal inputQty = (BigDecimal) bundle.getSerializable("inputQty");
                    BigDecimal inputScrapQty = (BigDecimal) bundle.getSerializable("inputScrapQty");
                    String inputMfgDate = bundle.getString("inputMfgDate");
                    String inputExpDate = bundle.getString("inputExpDate");

                    drSelectedRow.setValue("SEQ", drSeq);
                    drSelectedRow.setValue("STORAGE_ID", drStorageId);
                    drSelectedRow.setValue("ITEM_ID", drItemId);
                    drSelectedRow.setValue("LOT_ID", inputLotId);
                    drSelectedRow.setValue("QTY", inputQty);
                    drSelectedRow.setValue("SCRAP_QTY", inputScrapQty);
                    drSelectedRow.setValue("MFG_DATE", inputMfgDate);
                    drSelectedRow.setValue("EXP_DATE", inputExpDate);

                    getLotData(dtLotAll);
                }
            });
        }
    };

    private View.OnClickListener addNewLot = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            detailMode = "Add";
            Bundle sheetInfo = new Bundle();
            sheetInfo.putString("detailMode", detailMode);
            sheetInfo.putBoolean("blnIsWV", blnIsWV);
            sheetInfo.putString("sheetPolicyId", strSheetPolicyId);
            sheetInfo.putSerializable("dtDet", dtDet);
            sheetInfo.putInt("lotPos", lotPos);
            sheetInfo.putSerializable("dtLotAll", dtLotAll);

            gotoNextActivityForResult(WarehouseStorageStockInModifyActivity.class, sheetInfo, new OnActivityResult() {
                @Override
                public void onResult(Bundle bundle) {
                    String drSeq = bundle.getString("drSeq"); //getIntent().getStringExtra("drSeq");
                    String drStorageId = bundle.getString("drStorageId");
                    String drItemId = bundle.getString("drItemId");
                    String inputLotId = bundle.getString("inputLotId");
                    BigDecimal inputQty = (BigDecimal) bundle.getSerializable("inputQty");
                    BigDecimal inputScrapQty = (BigDecimal) bundle.getSerializable("inputScrapQty");
                    String inputMfgDate = bundle.getString("inputMfgDate");
                    String inputExpDate = bundle.getString("inputExpDate");

                    DataRow drLot = dtLotAll.newRow();
                    drLot.setValue("SEQ", drSeq);
                    drLot.setValue("STORAGE_ID", drStorageId);
                    drLot.setValue("ITEM_ID", drItemId);
                    drLot.setValue("LOT_ID", inputLotId);
                    drLot.setValue("QTY", inputQty);
                    drLot.setValue("SCRAP_QTY", inputScrapQty);
                    drLot.setValue("MFG_DATE", inputMfgDate);
                    drLot.setValue("EXP_DATE", inputExpDate);
                    dtLotAll.Rows.add(drLot);

                    getLotData(dtLotAll);

                }
            });

        }
    };
    // end region

    private View.OnClickListener confirmLot = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Bundle inputData = new Bundle();
            inputData.putSerializable("dtLotData", dtLotAll);
            setActivityResult(inputData);
        }
    };

    private void getLotData(DataTable dt) {

        DataTable dtCurrentLot = new DataTable();
        if (dt != null && dt.Rows.size() > 0) {
            String detSeq = dtDet.Rows.get(lotPos).getValue("SEQ").toString();
            for (DataRow dr : dt.Rows) {
                if (detSeq.equals(dr.getValue("SEQ"))) {
                    DataRow drLot = dr;
                    dtCurrentLot.Rows.add(drLot);
                }
            }
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        WarehouseStorageLotAdapter adapter = new WarehouseStorageLotAdapter(dtCurrentLot, blnIsWV, inflater);
        holder.lvLotData.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}

// ERROR CODE
// WAPG027001   請輸入至少一個條件
// WAPG027002   請選擇開單日期(起)和開單日期(迄)
// WAPG027003   開單日期(起)不能大於開單日期(迄)
// WAPG027004   請選擇單據類型
// WAPG027005   查詢無資料
// WAPG027006   物料[%s]管控批號，需要輸入批號
// WAPG027007   請輸入數量
// WAPG027008   請輸入報廢數量
// WAPG027009   數量、報廢數量不可同時為0！
// WAPG027010   請輸入製造日期
// WAPG027011   請輸入有效期限
// WAPG027012   製造日期不可大於有效期限
// WAPG027013   單據類型「%s」未設定「單據Config設定」內的【實際數量的狀態】，請先設定!
// WAPG027014   輸入批號「%s」與單據項次「%s」指定的批號「%s」不一致!
// WAPG027015   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，批號數量需相等!
// WAPG027016   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，批號報廢數量需相等!
// WAPG027017   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號數量「%s」不可小於單據數量「%s」!
// WAPG027018   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號報廢數量「%s」不可小於單據報廢數量「%s」!
// WAPG027019   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號數量「%s」不可大於單據數量「%s」!
// WAPG027020   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，新增批號報廢數量「%s」不可大於單據報廢數量「%s」!
// WAPG027021   批號已存在!
// WAPG027022   物料[%s]未設定WMS物料設定檔
// WAPG027023   物料[%s]不管控批號，批號需為空白
// WAPG027024   批號[%s]不存在於MES
// WAPG027025   批號[%s]非最外箱,需輸入最外層箱號,上層ID[%s]
// WAPG027026   批號[%s]數量[%s]需等於[%s]
// WAPG027027   是否刪除此批號?
// WAPG027028   請新增批號
// WAPG027029   單據項次「%s」沒有明細!
// WAPG027030   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，項次「%s」批號數量需相等!
// WAPG027031   單據類型「%s」在「單據Config設定」內的【實際數量的狀態】設定為「%s」，項次「%s」批號報廢數量需相等!
// WAPG027032   查無對應的入庫儲位
// WAPG027033   尚未選擇倉庫[%s]物料[%s]對應儲位
// WAPG027034   作業成功