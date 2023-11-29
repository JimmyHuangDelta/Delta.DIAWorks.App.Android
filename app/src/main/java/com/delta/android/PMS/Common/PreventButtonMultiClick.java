package com.delta.android.PMS.Common;

public class PreventButtonMultiClick {
    private static long lastClickTime;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 3000) {
            return true;
        }

        lastClickTime = time;
        return false;
    }
}
