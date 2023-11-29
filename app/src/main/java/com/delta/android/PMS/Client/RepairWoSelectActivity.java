package com.delta.android.PMS.Client;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.delta.android.Core.Activity.BaseActivity;
import com.delta.android.PMS.OffLineData.Data;
import com.delta.android.R;

public class RepairWoSelectActivity extends BaseActivity {

    ListView lsWo;
    EditText edPepairId;
    SimpleCursorAdapter adapter;
    Data data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pms_repair_wo_select);

        lsWo = (ListView) findViewById(R.id.lvPairQueryWo);
        edPepairId = (EditText) findViewById(R.id.edPepairId);
        data = new Data(RepairWoSelectActivity.this);

        //設定ListView資料
        setListViewData();

        //設定EditText監聽事件
        setEditTextListensers();

        //設定ListView監聽事件
        setListViewListensers();

        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                String partialValue = constraint.toString();
                SQLiteDatabase db = data.getReadableDatabase();
                Cursor cursor = db.query("SEMS_MRO_WO", null,
                        "MRO_WO_TYPE = 'Repair' AND ((MRO_WO_ID LIKE '%" + partialValue + "%') OR (EQP_ID LIKE '%" + partialValue + "%'))"+
                                "AND MRO_WO_ID IN (SELECT MRO_WO_ID FROM USER_WO WHERE USER_ID =?)",
                        new String[]{RepairWoSelectActivity.this.getGlobal().getUserID()}, null, null, null);
                return cursor;
            }
        });
    }

    private void setListViewData()
    {
        String querySql = "SELECT * FROM SEMS_MRO_WO WHERE MRO_WO_TYPE = 'Repair' AND MRO_WO_ID IN (SELECT MRO_WO_ID FROM USER_WO WHERE USER_ID =? order by MRO_WO_ID ASC)";
        Cursor cursor = data.getReadableDatabase().rawQuery(querySql,new String[]{RepairWoSelectActivity.this.getGlobal().getUserID()});

        adapter = new SimpleCursorAdapter(this,
                R.layout.activity_pms_select_repair_listview,
                cursor,
                new String[]{"MRO_WO_ID","EQP_ID","WO_STATUS","PLAN_DT"},
                new int[]{R.id.tvSelectWoId,R.id.tvSelectEqpId,R.id.tvSelectWoStatus,R.id.tvSelectPlanDate},
                0);
        lsWo.setAdapter(adapter);
        lsWo.setTextFilterEnabled(true);
    }

    private void setEditTextListensers()
    {
        edPepairId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setListViewListensers()
    {
        lsWo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view.findViewById(R.id.tvSelectWoId);
                Intent intent = new Intent();
                intent.setClass(RepairWoSelectActivity.this, WorkRepairActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("WO_ID", textView.getText().toString());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //設定ListView資料
        setListViewData();

        //設定EditText監聽事件
        setEditTextListensers();

        //設定ListView監聽事件
        setListViewListensers();

        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                String partialValue = constraint.toString();
                SQLiteDatabase db = data.getReadableDatabase();
                Cursor cursor = db.query("SEMS_MRO_WO", null,
                        "MRO_WO_TYPE = 'Repair' AND ((MRO_WO_ID LIKE '%" + partialValue + "%') OR (EQP_ID LIKE '%" + partialValue + "%'))"+
                                "AND MRO_WO_ID IN (SELECT MRO_WO_ID FROM USER_WO WHERE USER_ID =?)",
                        new String[]{RepairWoSelectActivity.this.getGlobal().getUserID()}, null, null, null);
                return cursor;
            }
        });
    }
}
