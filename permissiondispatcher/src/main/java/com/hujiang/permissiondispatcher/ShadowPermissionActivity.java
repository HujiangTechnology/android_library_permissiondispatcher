package com.hujiang.permissiondispatcher;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * to dispatcher permission
 * <br>
 * modify from https://github.com/ParkSangGwon/TedPermission
 *
 * @author simon
 * @since 2016-05-15
 */
public class ShadowPermissionActivity extends FragmentActivity {

    public static final int REQ_CODE_PERMISSION_REQUEST = 110;
    public static final int REQ_CODE_REQUEST_SETTING = 119;
    public static final int REQ_CODE_REQUEST_SYSTEM_ALERT_WINDOW = 120;
    public static final int REQ_CODE_REQUEST_WRITE_SETTING = 121;

    public static final String EXTRA_PERMISSIONS = "permissions";
    public static final String EXTRA_RATIONALE_MESSAGE = "rationale_message";
    public static final String EXTRA_DENY_MESSAGE = "deny_message";
    public static final String EXTRA_SETTING_BUTTON = "setting_button";
    public static final String EXTRA_SETTING_BUTTON_TEXT = "setting_button_text";
    public static final String EXTRA_RATIONALE_CONFIRM_TEXT = "rationale_confirm_text";
    public static final String EXTRA_DENIED_DIALOG_CLOSE_TEXT = "denied_dialog_close_text";

    String rationaleMessage;
    String denyMessage;
    String[] permissions;
    boolean hasRequestedSystemAlertWindow = false;
    String permissionSystemAlertWindow;
    boolean hasRequestedWriteSettings = false;
    String permissionWriteSettings;
    String packageName;

    boolean hasSettingButton;
    String settingButtonText;

    String deniedCloseButtonText;
    String rationaleConfirmText;

    private static onPermissionRequestFinishedListener sOnPermissionRequestFinishedListener;
    private static PermissionListener sPermissionListener;

    public static void setOnPermissionRequestFinishedListener(onPermissionRequestFinishedListener listener) {
        sOnPermissionRequestFinishedListener = listener;
    }

    private static void setPermissionListener(PermissionListener permissionListener) {
        sPermissionListener = permissionListener;
    }

    /**
     * start ShadowPermissionActivity self
     * @param context Context
     * @param permissions permission group
     * @param rationalMessage rational message
     * @param rationalButton rational button text
     * @param needSettingButton whether need to shown app setting button or not
     * @param deniedMessage denied message
     * @param deniedButton denied button text
     * @param permissionListener permission listener
     */
    public static void start(Context context, String[] permissions, String rationalMessage, String rationalButton, boolean needSettingButton
        , String settingTxt, String deniedMessage, String deniedButton, PermissionListener permissionListener) {
        setPermissionListener(permissionListener);

        Intent intent = new Intent(context, ShadowPermissionActivity.class);
        intent.putExtra(ShadowPermissionActivity.EXTRA_PERMISSIONS, permissions);
        intent.putExtra(ShadowPermissionActivity.EXTRA_RATIONALE_MESSAGE, rationalMessage);
        intent.putExtra(ShadowPermissionActivity.EXTRA_RATIONALE_CONFIRM_TEXT, rationalButton);
        intent.putExtra(ShadowPermissionActivity.EXTRA_SETTING_BUTTON, needSettingButton);
        intent.putExtra(ShadowPermissionActivity.EXTRA_SETTING_BUTTON_TEXT, settingTxt);
        intent.putExtra(ShadowPermissionActivity.EXTRA_DENY_MESSAGE, deniedMessage);
        intent.putExtra(ShadowPermissionActivity.EXTRA_DENIED_DIALOG_CLOSE_TEXT, deniedButton);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            setIntent(intent);
        }

        packageName = getPackageName();

        Bundle bundle = getIntent().getExtras();
        permissions = bundle.getStringArray(EXTRA_PERMISSIONS);
        rationaleMessage = bundle.getString(EXTRA_RATIONALE_MESSAGE);
        denyMessage = bundle.getString(EXTRA_DENY_MESSAGE);
        hasSettingButton = bundle.getBoolean(EXTRA_SETTING_BUTTON, false);
        settingButtonText = bundle.getString(EXTRA_SETTING_BUTTON_TEXT, getString(R.string.permission_setting));
        rationaleConfirmText = bundle.getString(EXTRA_RATIONALE_CONFIRM_TEXT, getString(R.string.permission_ok));
        deniedCloseButtonText = bundle.getString(EXTRA_DENIED_DIALOG_CLOSE_TEXT, getString(R.string.permission_close));

        checkPermissions(false);
    }

    private void permissionGranted() {
        if(sPermissionListener != null) {
            sPermissionListener.permissionGranted();
            sPermissionListener = null;
        }

        if (sOnPermissionRequestFinishedListener == null || !sOnPermissionRequestFinishedListener.onPermissionRequestFinishedAndCheckNext(permissions)) {
            finish();
            overridePendingTransition(0, 0);
        }
    }

    private void permissionDenied(List<String> deniedpermissions) {
        if(sPermissionListener != null){
            sPermissionListener.permissionDenied();
            sPermissionListener = null;
        }

        if (sOnPermissionRequestFinishedListener == null || !sOnPermissionRequestFinishedListener.onPermissionRequestFinishedAndCheckNext(permissions)) {
            finish();
            overridePendingTransition(0, 0);
        }
    }

    private void gotoSetting() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + packageName));
            startActivityForResult(intent, REQ_CODE_REQUEST_SETTING);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
            startActivityForResult(intent, REQ_CODE_REQUEST_SETTING);
        }
    }

    private void checkPermissions(boolean isAllRequested) {

        List<String> needPermissions = PermissionUtils.findDeniedPermissions(this, permissions);

        boolean showRationale = false;
        for (String permission : needPermissions) {
            if(!hasRequestedSystemAlertWindow && permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                permissionSystemAlertWindow = Manifest.permission.SYSTEM_ALERT_WINDOW;
            } else if(!hasRequestedWriteSettings && permission.equals(Manifest.permission.WRITE_SETTINGS)) {
                permissionWriteSettings = Manifest.permission.WRITE_SETTINGS;
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                showRationale = true;
            }
        }

        if (!PermissionUtils.isOverMarshmallow()) {
            permissionGranted();
        } else if (needPermissions.isEmpty()) {
            permissionGranted();
        } else if (isAllRequested) {
            //From Setting Activity
            permissionDenied(needPermissions);
        } else if (showRationale && !TextUtils.isEmpty(rationaleMessage)) {
            //Need Show Rationale
            showRationaleDialog(needPermissions);
        } else {
            //Need Request Permissions
            requestPermissions(needPermissions);
        }
    }

    @TargetApi(value = Build.VERSION_CODES.M)
    public void requestPermissions(List<String> needPermissions) {
        //first SYSTEM_ALERT_WINDOW
        if (!hasRequestedSystemAlertWindow && !TextUtils.isEmpty(permissionSystemAlertWindow)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName));
            startActivityForResult(intent, REQ_CODE_REQUEST_SYSTEM_ALERT_WINDOW);
        } else if (!hasRequestedWriteSettings && !TextUtils.isEmpty(permissionWriteSettings)) {
            //second WRITE_SETTINGS
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + packageName));
            startActivityForResult(intent, REQ_CODE_REQUEST_WRITE_SETTING);
        }else{
            //other permission
            ActivityCompat.requestPermissions(this, needPermissions.toArray(new String[needPermissions.size()]), REQ_CODE_PERMISSION_REQUEST);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        ArrayList<String> deniedPermissions = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];

            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permission);
            }
        }

        if (deniedPermissions.isEmpty()) {
            permissionGranted();
        } else {
            showPermissionDenyDialog(deniedPermissions);
        }
    }

    private void showRationaleDialog(final List<String> needPermissions) {
        new AlertDialog.Builder(this)
                .setMessage(rationaleMessage)
                .setCancelable(false)
                .setNegativeButton(rationaleConfirmText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermissions(needPermissions);
                    }
                }).show();
    }

    public void showPermissionDenyDialog(final ArrayList<String> deniedPermissions) {
        denyMessage = TextUtils.isEmpty(denyMessage) ? getString(R.string.permission_denied_msg_default) : denyMessage;
        deniedCloseButtonText = TextUtils.isEmpty(deniedCloseButtonText) ? getString(R.string.permission_close) : deniedCloseButtonText;

        if (!hasSettingButton) {
            // denyMessage
            Toast.makeText(ShadowPermissionActivity.this, denyMessage, Toast.LENGTH_LONG).show();
            permissionDenied(deniedPermissions);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(denyMessage)
                .setCancelable(false)
                .setNegativeButton(deniedCloseButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        permissionDenied(deniedPermissions);
                    }
                }).setPositiveButton(settingButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              gotoSetting();
            }
        });


        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_CODE_REQUEST_SETTING: {
                checkPermissions(true);
                break;
            }
            case REQ_CODE_REQUEST_SYSTEM_ALERT_WINDOW: {
                hasRequestedSystemAlertWindow = true;
                checkPermissions(false);
                break;
            }
            case REQ_CODE_REQUEST_WRITE_SETTING: {
                hasRequestedWriteSettings = true;
                checkPermissions(false);
                break;
            }
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private boolean isPermissionsEqual(String[] permissions1, String[] permissions2) {
        if (permissions1 == null || permissions2 == null) {
            return false;
        }

        if (permissions1.length != permissions2.length) {
            return false;
        }

        boolean isEqual;
        for (String p1 : permissions1) {
            boolean isp1Found = false;
            for (String p2 : permissions2) {
                if (TextUtils.equals(p1, p2)) {
                    isp1Found = true;
                    break;
                }
            }
            isEqual = isp1Found;
            if (!isEqual) {
                return false;
            }
        }

        return true;
    }

    /**
     * 权限请求结束的回调
     */
    static interface onPermissionRequestFinishedListener {
        /**
         *
         * @param permissions 已经处理完的权限申请
         * @return 是否还有其他未处理的权限
         */
        public boolean onPermissionRequestFinishedAndCheckNext(String[] permissions);
    }

}
