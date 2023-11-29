package com.delta.android.Core.Activity;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.delta.android.R;

import java.util.Timer;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core_main);

        if (Build.VERSION.SDK_INT >= 26) {//8.0以上,要自行打開權限(app更新通知)
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 0);
            }
        }

        //檢查是否有下載檔儲存檔案的權限
        if(hasExternalStoragePermission()==false)
        {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    101);
        }
        else
        {
            Intent intent;
            if (getResources().getBoolean(R.bool.DEBUG_MODE)) {
                intent = new Intent();
                intent.setClassName(getApplicationContext(), getResources().getString(R.string.DEBUG_CLASS));
            } else {
                intent = new Intent(MainActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }
    }

    private boolean hasExternalStoragePermission() {
        return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);
        Intent intent;
        if (getResources().getBoolean(R.bool.DEBUG_MODE)) {
            intent = new Intent();
            intent.setClassName(getApplicationContext(), getResources().getString(R.string.DEBUG_CLASS));
        } else {
            intent = new Intent(MainActivity.this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Intent intent;
        if (getResources().getBoolean(R.bool.DEBUG_MODE)) {
            intent = new Intent();
            intent.setClassName(getApplicationContext(), getResources().getString(R.string.DEBUG_CLASS));
        } else {
            intent = new Intent(MainActivity.this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
