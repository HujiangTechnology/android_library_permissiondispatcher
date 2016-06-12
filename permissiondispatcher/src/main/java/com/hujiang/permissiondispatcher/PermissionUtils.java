package com.hujiang.permissiondispatcher;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.PermissionChecker;

import java.util.ArrayList;
import java.util.List;

/**
 * permission utils
 *  <br>
 * modify from https://github.com/ParkSangGwon/TedPermission
 *
 * @author simon
 * @since 2016-05-15
 */
final public class PermissionUtils {
    private PermissionUtils() {
    }

    public static boolean isOverMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * Returns true if <code>Activity</code> or <code>Fragment</code> has access to all given permissions.
     *
     * @param context     context
     * @param permissions permissions
     * @return returns true if <code>Activity</code> or <code>Fragment</code> has access to all given permissions.
     */
    @TargetApi(value = Build.VERSION_CODES.M)
    public static boolean hasSelfPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (isOverMarshmallow() && permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                if (!Settings.canDrawOverlays(context)) {
                    return false;
                }
            } else if (isOverMarshmallow() && permission.equals(Manifest.permission.WRITE_SETTINGS)) {
                if (!Settings.System.canWrite(context)) {
                    return false;
                }
            } else if (PermissionChecker.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }



    @TargetApi(value = Build.VERSION_CODES.M)
    public static List<String> findDeniedPermissions(Activity activity, String... permission) {
        List<String> denyPermissions = new ArrayList<>();
        if (!isOverMarshmallow()) {
            return denyPermissions;
        }

        for (String value : permission) {
            // CommonsWare's blog post:https://commonsware.com/blog/2015/08/17/random-musings-android-6p0-sdk.html
            //support SYSTEM_ALERT_WINDOW,WRITE_SETTINGS
            if (isOverMarshmallow() && value.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                if(!Settings.canDrawOverlays(activity)) {
                    denyPermissions.add(value);
                }
            } else if(isOverMarshmallow() && value.equals(Manifest.permission.WRITE_SETTINGS)) {
                if(!Settings.System.canWrite(activity)) {
                    denyPermissions.add(value);
                }
            } else if(PermissionChecker.checkSelfPermission(activity, value) != PackageManager.PERMISSION_GRANTED) {
                denyPermissions.add(value);
            }
        }
        return denyPermissions;
    }
}
