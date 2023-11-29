package com.delta.android.Sample.Client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestFlow1Activity extends BaseFlowActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_test_flow1);

        Button btnSendParam11 = findViewById(R.id.btnFlow11);
        btnSendParam11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("name", "puma");
                bundle.putInt("age", 35);
                gotoNextActivity(TestFlow2Activity.class, bundle);
            }
        });

        Button btnSendParam12 = findViewById(R.id.btnFlow12);
        btnSendParam12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ListView listview = findViewById(R.id.lvData);
                //ListView 要顯示的內容
                String[] str = {"A", "B", "C", "D", "E", "F", "G", "H"};
                ArrayAdapter adapter = new ArrayAdapter(TestFlow1Activity.this, android.R.layout.simple_list_item_activated_1, str);
                listview.setAdapter(adapter);
            }
        });


        Button btnSendParam13 = findViewById(R.id.btnFlow13);
        btnSendParam13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ListView listview = findViewById(R.id.lvData);
                //ListView 要顯示的內容
                String[] str = {"A", "B", "C", "D", "E", "F", "G", "H"};

                List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("level", "1");
                item.put("name", "aaa");
                items.add(item);

                SimpleAdapter adapter = new SimpleAdapter(
                        TestFlow1Activity.this,
                        items,
                        android.R.layout.simple_list_item_activated_2,
                        new String[]{"level", "name"},
                        new int[]{android.R.id.text1, android.R.id.text2}
                );
                listview.setAdapter(adapter);
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }
}
