package com.example.musicplayer.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class Permissions {

    private static final int PERMISSION_REQUEST_CODE = 0x03;

    public static boolean permission(Activity activity, String permission) {
        if (checkPermission(activity, permission)) {
            Log.e("Permission", permission + "-Permission already granted.");
            return true;
        } else {
            requestPermission(activity, permission);
            return false;
        }
    }

    private static boolean checkPermission(Activity activity, String permission) {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private static void requestPermission(Activity activity, String permission) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, PERMISSION_REQUEST_CODE);
    }

    public static boolean permission(Activity activity, Fragment fragment, String permission) {
        if (checkPermission(activity, permission)) {
            Log.e("Permission", permission + "-Permission already granted.");
            return true;
        } else {
            requestPermission(fragment, permission);
            return false;
        }
    }

    private static void requestPermission(Fragment fragment, String permission) {
        fragment.shouldShowRequestPermissionRationale(permission);
    }
}
