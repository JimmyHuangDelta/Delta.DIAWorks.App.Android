package com.delta.android.Core.AppUpdate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;

import com.delta.android.R;

import java.io.File;
import java.lang.reflect.Method;

public class DownloadCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String strPkgName = context.getPackageName();

        if (!intent.getAction().equals("android.intent.action.DOWNLOAD_COMPLETE")) {
            return;
        }

        if (!intent.getPackage().equals(strPkgName)) {
            return;
        }

        try {

            if (Build.VERSION.SDK_INT >= 24) {//android 7.0以上會有問題,要多加這段語法
                try {
                    Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                    m.invoke(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            File apkFile = new File(String.format("%s/%s.apk", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), context.getResources().getString(R.string.app_name)));
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        } catch (Exception e) {
            e.getStackTrace();
        }
    }
}