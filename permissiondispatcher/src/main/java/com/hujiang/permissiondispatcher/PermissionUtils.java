package com.hujiang.permissiondispatcher;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.text.TextUtils;

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
        if (permissions == null || permissions.length == 0) {
            return false;
        }

        for (String permission : permissions) {
            if (permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                if (!canDrawOverlays(context)) {
                    return false;
                }
            } else if (permission.equals(Manifest.permission.WRITE_SETTINGS)) {
                if (!canWriteSetting(context)) {
                    return false;
                }
            } else if (PermissionChecker.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static List<String> findDeniedPermissions(Activity activity, String... permission) {
        List<String> denyPermissions = new ArrayList<>();
        if (!isOverMarshmallow()) {
            return denyPermissions;
        }

        for (String value : permission) {
            if (value.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                if(!canDrawOverlays(activity)) {
                    denyPermissions.add(value);
                }
            } else if(value.equals(Manifest.permission.WRITE_SETTINGS)) {
                if(!canWriteSetting(activity)) {
                    denyPermissions.add(value);
                }
            } else if(PermissionChecker.checkSelfPermission(activity, value) != PackageManager.PERMISSION_GRANTED) {
                denyPermissions.add(value);
            }
        }
        return denyPermissions;
    }

    public static boolean canDrawOverlays(Context context) {
        if (isOverMarshmallow()) {
            return Settings.canDrawOverlays(context);
        }

        return true;
    }

    public static boolean canWriteSetting(Context context) {
        if (isOverMarshmallow()) {
            return Settings.System.canWrite(context);
        }

        return true;
    }

    public static boolean containsSystemAlertWindowPermission(String[] permissions) {
        return contains(permissions, Manifest.permission.SYSTEM_ALERT_WINDOW);
    }

    public static boolean containsWriteSettingPermission(String[] permissions) {
        return contains(permissions, Manifest.permission.WRITE_SETTINGS);
    }

    private static boolean contains(String[] sources, String target) {
        if (sources == null || sources.length <= 0 || TextUtils.isEmpty(target)) {
            return false;
        }

        for (String s : sources) {
            if (target.equals(s)) {
                return true;
            }
        }

        return false;
    }

    public static boolean shouldShowRequestPermissionRationale(Activity activity, String[] permissions) {
        if (permissions == null || permissions.length <= 0 || activity == null) {
            return false;
        }

        for (String p : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, p)) {
                return true;
            }
        }

        return false;
    }
}
