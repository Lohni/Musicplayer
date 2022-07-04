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
        //if (Build.VERSION.SDK_INT >= 23) {
        //Check whether your app has access to the READ permission//
        if (checkPermission(activity, permission)) {
            //If your app has access to the device’s storage, then print the following message to Android Studio’s Logcat//
            Log.e("Permission", permission + "-Permission already granted.");
            return true;
        } else {
            //If your app doesn’t have permission to access external storage, then call requestPermission//
            requestPermission(activity, permission);
            return false;
        }
        //}
    }

    private static boolean checkPermission(Activity activity, String permission) {
        //Check for READ_EXTERNAL_STORAGE access, using ContextCompat.checkSelfPermission()//
        int result = ContextCompat.checkSelfPermission(activity, permission);
        //If the app does have this permission, then return true//
        //If the app doesn’t have this permission, then return false//
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private static void requestPermission(Activity activity, String permission) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, PERMISSION_REQUEST_CODE);
    }


    public static boolean permission(Activity activity, Fragment fragment, String permission) {
        if (checkPermission(activity, permission)) {
            //If your app has access to the device’s storage, then print the following message to Android Studio’s Logcat//
            Log.e("Permission", permission + "-Permission already granted.");
            return true;
        } else {
            //If your app doesn’t have permission to access external storage, then call requestPermission//
            requestPermission(fragment, permission);
            return false;
        }
    }

    private static void requestPermission(Fragment fragment, String permission) {
        fragment.requestPermissions(new String[]{permission}, PERMISSION_REQUEST_CODE);
    }
}
