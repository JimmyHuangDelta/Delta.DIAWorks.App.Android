package com.delta.android.Sample.Client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.delta.android.Core.Activity.AccountActivity;
import com.delta.android.Core.Activity.BaseActivity;
import com.delta.android.R;

public class Test4Activity extends AccountActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_test4);


        Button btnTest = findViewById(R.id.btnTest);


        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int a = 0;
                int b = 0;
                double aa = a / b;
                //gotoHomeActivity();
                //Toast.makeText(Test4Activity.this, "test", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(Intent.ACTION_MAIN);
//                PackageManager managerclock = getPackageManager();
//                i = managerclock.getLaunchIntentForPackage("com.delta.android.Uniworks");
//                i.addCategory(Intent.CATEGORY_LAUNCHER);
//                startActivity(i);

            }
        });

    }
}
