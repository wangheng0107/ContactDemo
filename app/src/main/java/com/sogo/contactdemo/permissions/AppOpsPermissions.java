package com.sogo.contactdemo.permissions;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Binder;
import android.os.Build;
import android.util.Log;

/**
 * Created by chenzheng on 2017/6/5.
 */

public class AppOpsPermissions {
    private static final String TAG = AppOpsPermissions.class.getSimpleName();

    public static boolean hasContactPermission(Context context, String ops) {
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                int checkResult = appOpsManager.checkOpNoThrow(
                        ops, Binder.getCallingUid(), context.getPackageName());
                if (checkResult == AppOpsManager.MODE_ALLOWED) {
                    Log.d(TAG, ops + ":有权限");
                } else if (checkResult == AppOpsManager.MODE_IGNORED) {
                    Log.d(TAG, ops + ":被禁止了");
                    return false;
                } else if (checkResult == AppOpsManager.MODE_ERRORED) {
                    Log.d(TAG, ops + ":出错了");
                } else if (checkResult == 4) {
                    Log.d(TAG, ops + ":权限需要询问");
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return true;
            }

        }

        return true;
    }
}
