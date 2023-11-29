package com.delta.android.PMS.Client;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TextView;

import com.delta.android.Core.Activity.BaseActivity;
import com.delta.android.PMS.OffLineData.Data;
import com.delta.android.R;

public class PmWoSelectActivity extends BaseActivity {

    EditText editText;
    ListView listView;
    Data data;
    SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pms_pm_wo_select);

        editText = (EditText)findViewById(R.id.etWorkId);
        listView = (ListView)findViewById(R.id.lvWorkData);
        data = new Data(PmWoSelectActivity.this);

        BindWoListAndSetFilter();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView textView = (TextView) view.findViewById(R.id.tvSelectWoId);
                Intent intent = new Intent(PmWoSelectActivity.this, WorkPmActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("WO_ID", textView.getText().toString());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                adapter.getFilter().filter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void BindWoListAndSetFilter() {
        String querySql = "SELECT * FROM SEMS_MRO_WO WHERE MRO_WO_TYPE = 'PM' AND MRO_WO_ID IN (SELECT MRO_WO_ID FROM USER_WO WHERE USER_ID =? order by MRO_WO_ID ASC)";
        Cursor cursor = data.getReadableDatabase().rawQuery(querySql,new String[]{PmWoSelectActivity.this.getGlobal().getUserID()});

//        Cursor cursor = db.query("SEMS_MRO_WO", null, "MRO_WO_TYPE = 'PM'", null, null, null, "MRO_WO_ID ASC");
//        cursor.moveToFirst();

        adapter = new SimpleCursorAdapter(this,
                R.layout.activity_pms_select_wo_listview,
                cursor,
                new String[]{"MRO_WO_ID","EQP_ID","PM_ID","WO_STATUS","PLAN_DT"},
                new int[]{R.id.tvSelectWoId,R.id.tvSelectEqpId,R.id.tvSelectPm,R.id.tvSelectWoStatus,R.id.tvSelectPlanDate},
                0);

        adapter.setFilterQueryProvider(new FilterQueryProvider() {

            @Override
            public Cursor runQuery(CharSequence constraint) {
                String partialValue = constraint.toString();
                SQLiteDatabase db = data.getReadableDatabase();
                Cursor cursor = db.query("SEMS_MRO_WO", null
                        , "MRO_WO_TYPE = 'PM' AND ((MRO_WO_ID LIKE '%" + partialValue + "%') OR (EQP_ID LIKE '%" + partialValue + "%'))" +
                                "AND MRO_WO_ID IN (SELECT MRO_WO_ID FROM USER_WO WHERE USER_ID =?)",
                        new String[]{PmWoSelectActivity.this.getGlobal().getUserID()}, null, null, null);

                return cursor;
            }
        });

        listView.setTextFilterEnabled(true);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BindWoListAndSetFilter();
    }
}
