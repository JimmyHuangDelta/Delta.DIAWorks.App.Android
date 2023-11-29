package com.delta.android.Core.Activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.delta.android.BuildConfig;
import com.delta.android.R;

public class VersionUpdateActivity extends BaseActivity {

    TextView tvAppVersion;
    Button btnCheckUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core_version_update);

        tvAppVersion = findViewById(R.id.tvAppVersion);
        //btnCheckUpdate = findViewById(R.id.btnCheckUpdate);

        //取得目前App取版
        tvAppVersion.setText(String.format("%s：%s", "目前版本", BuildConfig.VERSION_NAME));

    }
}
