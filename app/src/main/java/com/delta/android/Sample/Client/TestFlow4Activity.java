package com.delta.android.Sample.Client;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.R;

public class TestFlow4Activity extends BaseFlowActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_test_flow4);

        final Bundle bundle = getBundle();
        Toast.makeText(TestFlow4Activity.this, bundle == null ? "0" : String.valueOf(bundle.size()), Toast.LENGTH_SHORT).show();


        Button btnSendParam41 = findViewById(R.id.btnFlow41);
        btnSendParam41.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("test4", "ABCDE");
                setActivityResult(bundle);
            }
        });


        Button btnSendParam42 = findViewById(R.id.btnFlow42);
        btnSendParam42.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //back TestFlow1

                gotoPreviousActivity(TestFlow1Activity.class);
            }
        });

        Button btnSendParam43 = findViewById(R.id.btnFlow43);
        btnSendParam43.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //back TestFlow2

                bundle.putString("qty", "123");
                gotoPreviousActivity(TestFlow2Activity.class, bundle);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
