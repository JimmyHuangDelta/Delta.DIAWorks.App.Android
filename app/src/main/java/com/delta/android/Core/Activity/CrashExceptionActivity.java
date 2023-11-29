package com.delta.android.Core.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.delta.android.R;

public class CrashExceptionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core_crash_exception);

        TextView tvMessage = findViewById(R.id.tvDialogMessage);
        if (getIntent().getStringExtra("CRASH_MESSAGE") != null) {
            tvMessage.setText(getIntent().getStringExtra("CRASH_MESSAGE"));
        }

        Button btnStartApp = findViewById(R.id.btnReStartApp);
        btnStartApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReStartApp();
            }
        });

    }

    public void ReStartApp() {
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        startActivity(i);
        finish();
    }
}
