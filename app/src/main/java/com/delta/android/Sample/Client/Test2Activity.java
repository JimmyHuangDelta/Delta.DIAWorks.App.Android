package com.delta.android.Sample.Client;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.delta.android.Core.Activity.BaseActivity;
import com.delta.android.R;

public class Test2Activity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_test2);

        Button btnShowMessage1 = findViewById(R.id.btnShowMessage1);
        Button btnShowMessage2 = findViewById(R.id.btnShowMessage2);
        Button btnShowMessage3 = findViewById(R.id.btnShowMessage3);
        Button btnShowMessage4 = findViewById(R.id.btnShowMessage4);

        btnShowMessage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowMessage("AAAdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
                        "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
                        "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
                        "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
                        "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
                        "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
                        "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
                        "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
                        "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
                        "dsdsdsdeeeeeeeBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBCCCCCCCCCCCCCCCCcc");
            }
        });

        btnShowMessage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowMessage(R.string.C001001);
            }
        });

        btnShowMessage3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowMessage(R.string.C001002, "A02", "Open");
            }
        });

        btnShowMessage4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowMessage(getResString("USER_ID"));
            }
        });

    }
}
