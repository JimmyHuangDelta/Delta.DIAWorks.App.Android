package com.delta.android.Sample.Client;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.delta.android.Core.Activity.BaseActivity;
import com.delta.android.Core.Activity.BaseFlowActivity;
import com.delta.android.R;

public class Page3Activity extends BaseFlowActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_page3);


        Button btn = findViewById(R.id.btnGoTo);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoNextActivity(Page4Activity.class);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
