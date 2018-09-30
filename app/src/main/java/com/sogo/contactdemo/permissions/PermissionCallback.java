package com.sogo.contactdemo.permissions;

/**
 * Created by zhengchen on 28/11/2017.
 */

public interface PermissionCallback {
    void onPermissionGranted();

    void onPermissionDenied();
}
