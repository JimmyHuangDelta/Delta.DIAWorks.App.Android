package com.delta.android.Core.Common;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;

public class CheckPermission {

    private Context context;
    private ArrayList<String> arrayList; //紀錄缺少驗證的權限。

    public CheckPermission(Context activity) {
        context = activity;
    }

    //傳入 ArrayList<String> 紀錄要檢核的權限。
    public boolean CheckPermission(ArrayList<String> permission) {
        if (Build.VERSION.SDK_INT >= 23) {
            arrayList = new ArrayList<>(); //初始化紀錄未開通權限的清單。
            for (int i = 0; i < permission.size(); i++) {
                int hasWriteContactsPermission = context.checkSelfPermission(permission.get(i));
                if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
                    arrayList.add(permission.get(i));
                }
            }


            //代表有未開通的權限
            if (arrayList.size() > 0)
                return false;
        }
        return true;
    }

    //取得所有未開通的權限
    public String[] GetUnPermission() {
        Object[] objs = arrayList.toArray();
        String[] unPermissions = Arrays.copyOf(objs, objs.length, String[].class);
        return unPermissions;
    }
}
