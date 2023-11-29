package com.delta.android.Sample.Client;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.R;

public class TestFlow3Activity extends BaseFlowActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_test_flow3);

        final Bundle bundle = getBundle();
        Toast.makeText(TestFlow3Activity.this, bundle == null ? "0" : String.valueOf(bundle.size()), Toast.LENGTH_SHORT).show();


        Button btnSendParam31 = findViewById(R.id.btnFlow31);
        btnSendParam31.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("test3", "12345");
                setActivityResult(bundle);
            }
        });


        Button btnSendParam32 = findViewById(R.id.btnFlow32);
        btnSendParam32.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bundle.putString("key", "AA001");
                gotoNextActivity(TestFlow4Activity.class, bundle);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
