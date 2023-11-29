package com.delta.android.Core.AppUpdate;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.v4.content.FileProvider;
import android.view.WindowManager;

import com.delta.android.Core.Common.Global;
import com.delta.android.R;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class CheckUpdateService extends Service {

    private Binder binder;
    private HandlerThread thread = null;
    private Handler handler;
    private Handler handlerUI = null;

    private int interval = 10000;
    private boolean checkVersion = true;
    private Gson gson = null;
    private String version = null;
    private AppVersion appVer = null;

    long downloadID;

    //建立服務
    @Override
    public void onCreate() {
        super.onCreate();
        binder = new Binder();
        interval = getResources().getInteger(R.integer.DEFAULT_UPDATE_APP_INTERVAL);
        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    //綁定服務
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    //啟動服務
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        thread = new HandlerThread("CheckUpdate");
        handler = new Handler();
        handlerUI = new Handler();
        gson = new Gson();

        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (!thread.isAlive())//表示Thread尚未被開啟
        {
            thread.start();
            handler = new Handler(thread.getLooper());
        }

        handler.post(timer);

        return super.onStartCommand(intent, flags, startId);
    }

    //解除綁定服務
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    //銷毀服務
    @Override
    public void onDestroy() {
        unregisterReceiver(onDownloadComplete);
        handler.removeCallbacks(timer);
        super.onDestroy();
        thread.quit();
    }

    final Runnable timer = new Runnable() {
        @Override
        public void run() {
            try {
                CheckUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
            handler.postDelayed(this, interval);
        }
    };

    private void CheckUpdate() {
        if (version == null) {
            return;
        }

        //有顯示提示更新的視窗，在尚未關閉視窗前不繼續執行版本檢查
        if (!checkVersion) {
            return;
        }

        // 版本檢查
        boolean bSuccess = false;

        HttpURLConnection conn = null;
        StringBuffer sb = new StringBuffer();

        try {
            URL url = new URL(Global.getContext().getApiUpdateUrl());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
            conn.setRequestMethod("POST");

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append('\r');
            }
            reader.close();

            conn.connect();
            bSuccess = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            bSuccess = false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        appVer = gson.fromJson(sb.toString(), AppVersion.class);

        if (appVer == null)
            return;

        if (!appVer.getVersion().equals(version) && bSuccess) {
            try {
                handlerUI.post(new Runnable() {
                    @Override
                    public void run() {
                        ShowDownloadCheckDialog(appVer.getVersion(), appVer.getDownloadUrl());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return;
    }

    private void ShowDownloadCheckDialog(String version, final String url) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getResources().getText(R.string.TIP));//提示
        dialog.setMessage(String.format(getResources().getText(R.string.A000001).toString(), version));//版本有更新，是否更新至最新版本?( %s 版)
        dialog.setPositiveButton(getResources().getText(R.string.CONFIRM), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DownloadUpdateApk(url);
            }
        });

        dialog.setNegativeButton(getResources().getText(R.string.CANCEL), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkVersion = true;
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });

        checkVersion = false;
        AlertDialog alert = dialog.create();
        alert.setCancelable(false);//只允許按下dialog裡的按鍵才能關閉dialog

        if (Build.VERSION.SDK_INT >= 26) {//8.0以上
            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        }
        else
        {
            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
        }

        alert.show();
    }

    BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadID == id) {
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

                checkVersion=true;
            }
        }
    };

    private void DownloadUpdateApk(String apkUrl) {

        String apkName = String.format("%s.apk", getResources().getString(R.string.app_name));

        //先刪除舊有的apk
//        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());
//        if (dir.isDirectory()) {
//            String[] children = dir.list();
//            if (children != null) {
//                for (int i = 0; i < children.length; i++) {
//                    if (children[i].equals(apkName)) {
//                        new File(dir, children[i]).delete();
//                        break;
//                    }
//                }
//            }
//        }

        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + apkName);
        if (dir.exists()){
            dir.delete();
        }

        /*DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
        request.setTitle(getResources().getString(R.string.app_name));
        request.setDescription(getResources().getText(R.string.A000002));//更新檔下載中
        //在下載過程中通知欄會一直顯示該下載的通知，下載完成後該Notification會繼續顯示，直到用戶點擊該Notification或消除該Notification。
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        //设置文件存放路徑=>根目>sdcard>Download>xxx.apk
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkName);
        downloadID = downloadManager.enqueue(request);*/

        //20201006 archie 因使用DownloadManager會無法下載apk,故改用DownloadTask的方式
        String destPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .getAbsolutePath() + File.separator + apkName;
        new DownloadTask().execute(apkUrl, destPath);
    }

    private class DownloadTask extends AsyncTask<String, Void, Void>
    {
        // 传递两个参数：URL 和 目标路径
        private String url;
        private String destPath;

        @Override
        protected void onPreExecute()
        {
            String apkName = String.format("%s.apk", getResources().getString(R.string.app_name));


            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + apkName);
            if (dir.exists())
            {
                dir.delete();
            }
        }

        @Override
        protected Void doInBackground(String... params)
        {
            //LogUtil.i("doInBackground. url:{}, dest:{}", params[0], params[1]);
            url = params[0];
            destPath = params[1];
            OutputStream out = null;
            HttpURLConnection urlConnection = null;
            try
            {
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(15000);
                urlConnection.setReadTimeout(15000);
                InputStream in = urlConnection.getInputStream();
                out = new FileOutputStream(params[1]);
                byte[] buffer = new byte[10 * 1024];
                int len;
                while ((len = in.read(buffer)) != -1)
                {
                    out.write(buffer, 0, len);
                }
                in.close();
            } catch (IOException e)
            {
                final String strMsg = e.getMessage();

            } finally
            {
                if (urlConnection != null)
                {
                    urlConnection.disconnect();
                }
                if (out != null)
                {
                    try
                    {
                        out.close();
                    } catch (IOException e)
                    {

                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            if (Build.VERSION.SDK_INT >= 24)
            {//android 7.0以上會有問題,要多加這段語法
                try
                {
                    Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                    m.invoke(null);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            File apkFile = new File(String.format("%s/%s.apk", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), getResources().getString(R.string.app_name)));

            if (apkFile.exists())
            {
                /*Intent i = new Intent(Intent.ACTION_VIEW);
                i.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(i);*/

                try
                {

//                    Intent i = new Intent(Intent.ACTION_VIEW);
//                    i.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
//                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    getApplicationContext().startActivity(i);


                    //20210603 Winnie 修改 for Android10 可以更新
                    //因Android10安全防護層級提高,故Intent需要更改寫法才不會出錯
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setAction(Intent.ACTION_VIEW);
                    if (Build.VERSION.SDK_INT >= 24) {
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //設置程式擁有讀取限權

                        //intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");

                        //FileProvider的前置設定在AndroidManifest.xml中,authority內容值需跟設定的一致
                        Uri apkUri = FileProvider.getUriForFile(getApplicationContext(), "com.delta.android.PMS.Client",
                                apkFile);
                        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    } else {
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                    }


                /*
                if (getApplicationContext() != null &&
                        intent.resolveActivity(getApplicationContext().getPackageManager())!=null)
                {
                    getApplicationContext().startActivity(intent);
                }
                */

                    //intent.addCategory(Intent.CATEGORY_DEFAULT);
                    getApplicationContext().startActivity(intent);
                }
                catch (ActivityNotFoundException e)
                {
                    e.getMessage();
                    e.printStackTrace();
                }
            }
            else
            {
                //Toast.makeText(getApplicationContext(), "下載檔案不存在！", Toast.LENGTH_SHORT).show();
            }
        }
    }
}