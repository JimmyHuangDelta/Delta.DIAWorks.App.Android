package com.delta.android.Core.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.delta.android.R;

public class LaunchActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core_launch);


        //轉到登入頁
        Intent intent = new Intent();
        intent.setClass(this, LoginActivity.class);
        startActivity(intent);


    }
}
