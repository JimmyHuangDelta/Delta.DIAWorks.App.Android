package com.delta.android.Sample.Client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.R;

public class TestFlow2Activity extends BaseFlowActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_test_flow2);

        final Bundle bundle = getBundle();
        Toast.makeText(TestFlow2Activity.this, String.valueOf(bundle.size()) , Toast.LENGTH_SHORT).show();

        Button btnSendParam21 = findViewById(R.id.btnFlow21);
        btnSendParam21.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoNextActivityForResult(TestFlow3Activity.class, new OnActivityResult() {
                    @Override
                    public void onResult(Bundle bundle) {
                        Toast.makeText(TestFlow2Activity.this, String.valueOf(bundle.size()) , Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        Button btnSendParam22 = findViewById(R.id.btnFlow22);
        btnSendParam22.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bundle.putString("id", "010101");
                gotoNextActivityForResult(TestFlow4Activity.class, bundle, new OnActivityResult() {
                    @Override
                    public void onResult(Bundle bundle) {
                        Toast.makeText(TestFlow2Activity.this, String.valueOf(bundle.size()) , Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });

        Button btnSendParam23 = findViewById(R.id.btnFlow23);
        btnSendParam23.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bundle.putString("id", "010101");
                gotoNextActivity(TestFlow3Activity.class, bundle);
            }
        });

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
