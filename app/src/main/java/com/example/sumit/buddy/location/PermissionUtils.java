package com.example.sumit.buddy.location;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class PermissionUtils {
    private static final String TAG = "PermissionUtils";

    private static final String SHARED_PREFERENCE_FILENAME = "permission_location";
    private static final String SHARED_PREFERENCE_KEY = "is_app_launched_first_time";

    public interface PermissionAskListener {
        public void onPermissionGranted();

        public void onPermissionRequest();

        public void onPermissionPreviouslyDenied();

        public void onPermissionDisabled();


    }

    private static int getTargetSDK(Activity activity) {
        int targetSdkVersion = 0;
        try {
            final PackageInfo info = activity.getPackageManager().getPackageInfo(
                    activity.getPackageName(), 0);
            targetSdkVersion = info.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "TargetSDK is " + targetSdkVersion);
        return targetSdkVersion;
    }

    private static boolean isRuntimePermissionRequired(Activity activity) {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getTargetSDK(activity) >= Build.VERSION_CODES.M);
    }

    private static boolean getApplicationLaunchedFirstTime(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(SHARED_PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(SHARED_PREFERENCE_KEY, true);
    }

    private static void setApplicationLaunchedFirstTime(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(SHARED_PREFERENCE_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SHARED_PREFERENCE_KEY, false);
        editor.commit();
    }


    public static void checkPermission(Activity activity, String permission, PermissionAskListener permissionAskListener) {

        if (!isRuntimePermissionRequired(activity)) {
            Log.v(TAG, "Runtime permission not required.");
            permissionAskListener.onPermissionGranted();
        } else {

            Log.v(TAG, "Runtime permission required");

            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission not granted");

                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    Log.v(TAG, "Permission denied previously. Show custom dialog and then request permission");
                    permissionAskListener.onPermissionPreviouslyDenied();
                } else {

                    if (getApplicationLaunchedFirstTime(activity)) {

                        Log.v(TAG, "Activity asking location permission first time. No custom dialog required, just ask for permission");

                        setApplicationLaunchedFirstTime(activity);
                        permissionAskListener.onPermissionRequest();
                    } else {
                        Log.v(TAG, "Permission disabled. Re-enable permission by going to settings screen");

                        permissionAskListener.onPermissionDisabled();
                    }

                }

            } else {
                Log.v(TAG, "Permission already granted");
                permissionAskListener.onPermissionGranted();

            }

        }


    }
}
