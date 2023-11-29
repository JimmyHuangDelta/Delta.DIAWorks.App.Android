package com.delta.android.Core.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.delta.android.R;

public class ConfigSettingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core_config_setting);

        final EditText etUrl = findViewById(R.id.etUrl);
        etUrl.setText(getGlobal().getUrl());

        final EditText etPortalUrl = findViewById(R.id.etPortalUrl);
        etPortalUrl.setText(getGlobal().get_PortalUrl());

        //20201020 archie SSO新增
        final EditText etSSOUrl = findViewById(R.id.etSSOUrl);
        etSSOUrl.setText(getGlobal().getSSOUrl());

        Button btnConfirm = findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGlobal().setUrl(etUrl.getText().toString());
                getGlobal().setPortalUrl(etPortalUrl.getText().toString());
                //20201020 archie SSO新增
                getGlobal().setSSOUrl(etSSOUrl.getText().toString());
                ShowMessage("Update Success");
            }
        });
    }
}
