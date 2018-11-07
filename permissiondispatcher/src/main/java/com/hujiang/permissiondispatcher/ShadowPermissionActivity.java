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
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

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
    public static final String EXTRA_INSTANCE_TAG = "instance_tag";

    private static final int CHECK_FROM_DEFAULT = 0;
    private static final int CHECK_FROM_SYS_ALERT_WINDOW = 1;
    private static final int CHECK_FROM_WRITE_SETTING = 2;
    private static final int CHECK_FROM_SETTING = 3;

    String rationaleMessage;
    String denyMessage;
    String[] permissions;
    boolean hasRequestedSystemAlertWindow = false;
    String permissionSystemAlertWindow;
    boolean hasRequestedWriteSettings = false;
    String permissionWriteSettings;
    String packageName;
    String tag;

    boolean hasSettingButton;
    String settingButtonText;

    String deniedCloseButtonText;
    String rationaleConfirmText;

    /**
     * start ShadowPermissionActivity self
     * @param context Context
     * @param permissions permission group
     * @param rationalMessage rational message
     * @param rationalButton rational button text
     * @param needSettingButton whether need to shown app setting button or not
     * @param deniedMessage denied message
     * @param deniedButton denied button text
     * @param tag instance tag
     */
    public static void start(Context context, String[] permissions, String rationalMessage, String rationalButton, boolean needSettingButton
        , String settingTxt, String deniedMessage, String deniedButton, String tag) {
        Intent intent = new Intent(context, ShadowPermissionActivity.class);
        intent.putExtra(ShadowPermissionActivity.EXTRA_PERMISSIONS, permissions);
        intent.putExtra(ShadowPermissionActivity.EXTRA_RATIONALE_MESSAGE, rationalMessage);
        intent.putExtra(ShadowPermissionActivity.EXTRA_RATIONALE_CONFIRM_TEXT, rationalButton);
        intent.putExtra(ShadowPermissionActivity.EXTRA_SETTING_BUTTON, needSettingButton);
        intent.putExtra(ShadowPermissionActivity.EXTRA_SETTING_BUTTON_TEXT, settingTxt);
        intent.putExtra(ShadowPermissionActivity.EXTRA_DENY_MESSAGE, deniedMessage);
        intent.putExtra(ShadowPermissionActivity.EXTRA_DENIED_DIALOG_CLOSE_TEXT, deniedButton);
        intent.putExtra(ShadowPermissionActivity.EXTRA_INSTANCE_TAG, tag);
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
        tag = bundle.getString(EXTRA_INSTANCE_TAG, "");

        checkPermissions(CHECK_FROM_DEFAULT);
    }

    private void permissionGranted() {
        if (!TextUtils.isEmpty(tag)) {
            CheckPermissionHolder.CheckPermissionProxy proxy = CheckPermissionHolder.instance().find(tag);
            if (proxy != null && proxy.permissionListener != null) {
                proxy.permissionListener.permissionGranted();
                proxy.permissionListener = null;
            }

            if (proxy != null && proxy.onPermissionRequestFinishedListener != null) {
                proxy.onPermissionRequestFinishedListener.onPermissionRequestFinishedAndCheckNext(permissions);
            }
        }

        finish();
        overridePendingTransition(0, 0);
    }

    private void permissionDenied() {
        if (!TextUtils.isEmpty(tag)) {
            CheckPermissionHolder.CheckPermissionProxy proxy = CheckPermissionHolder.instance().find(tag);
            if (proxy != null && proxy.permissionListener != null) {
                proxy.permissionListener.permissionDenied();
                proxy.permissionListener = null;
            }

            if (proxy != null && proxy.onPermissionRequestFinishedListener != null) {
                proxy.onPermissionRequestFinishedListener.onPermissionRequestFinishedAndCheckNext(permissions);
            }
        }

        finish();
        overridePendingTransition(0, 0);
    }

    private void gotoSetting() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.fromParts("package", packageName, null));
            startActivityForResult(intent, REQ_CODE_REQUEST_SETTING);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
            startActivityForResult(intent, REQ_CODE_REQUEST_SETTING);
        }
    }

    private void checkPermissions(int from) {
        List<String> deniedPermissions = PermissionUtils.findDeniedPermissions(this, permissions);

        boolean showRationale = false;
        for (String permission : deniedPermissions) {
            if(!hasRequestedSystemAlertWindow && permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                permissionSystemAlertWindow = Manifest.permission.SYSTEM_ALERT_WINDOW;
            } else if(!hasRequestedWriteSettings && permission.equals(Manifest.permission.WRITE_SETTINGS)) {
                permissionWriteSettings = Manifest.permission.WRITE_SETTINGS;
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                showRationale = true;
            }
        }

        if (deniedPermissions.isEmpty()) {
            permissionGranted();
        } else {
            String[] deniedPermissionArray = deniedPermissions.toArray(new String[deniedPermissions.size()]);
            if (CHECK_FROM_WRITE_SETTING == from && !PermissionUtils.canWriteSetting(this)) {
                //From Setting Activity
                showPermissionDenyDialog(new String[]{Manifest.permission.WRITE_SETTINGS});
            }  else if (CHECK_FROM_SYS_ALERT_WINDOW == from && !PermissionUtils.canDrawOverlays(this)) {
                showPermissionDenyDialog(new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW});
            } else if (CHECK_FROM_SETTING == from) {
                permissionDenied();
            } else if (showRationale && !TextUtils.isEmpty(rationaleMessage)) {
                //Need Show Rationale
                showRationaleDialog(deniedPermissionArray);
            } else {
                //Need Request Permissions
                requestPermissions(deniedPermissions);
            }
        }
    }

    @TargetApi(value = Build.VERSION_CODES.M)
    public void requestPermissions(List<String> needPermissions) {
        //first SYSTEM_ALERT_WINDOW
        if (!hasRequestedSystemAlertWindow && !TextUtils.isEmpty(permissionSystemAlertWindow)) {
            openSystemAlertWindowSettingPage();
        } else if (!hasRequestedWriteSettings && !TextUtils.isEmpty(permissionWriteSettings)) {
            //second WRITE_SETTINGS
            openWriteSettingPage();
        }else {
            //other permission
            ActivityCompat.requestPermissions(this, needPermissions.toArray(new String[needPermissions.size()]), REQ_CODE_PERMISSION_REQUEST);
        }
    }

    private void openSystemAlertWindowSettingPage() {
        String name = getPackageManager().getApplicationLabel(getApplicationInfo()).toString();
        new AlertDialog.Builder(this)
                .setMessage(name + getString(R.string.permission_need) + getString(R.string.permission_hint_sys_alert_window))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.permission_setting), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.fromParts("package", packageName, null));
                        startActivityForResult(intent, REQ_CODE_REQUEST_SYSTEM_ALERT_WINDOW);
                    }
                }).setNegativeButton(getString(R.string.permission_deny), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkPermissions(CHECK_FROM_SYS_ALERT_WINDOW);
                    }
                }).show();
    }

    private void openWriteSettingPage() {
        String name = getPackageManager().getApplicationLabel(getApplicationInfo()).toString();
        new AlertDialog.Builder(this)
                .setMessage(name + getString(R.string.permission_need) + getString(R.string.permission_hint_write_setting))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.permission_setting), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.fromParts("package", packageName, null));
                        startActivityForResult(intent, REQ_CODE_REQUEST_WRITE_SETTING);
                    }
                }).setNegativeButton(getString(R.string.permission_deny), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkPermissions(CHECK_FROM_WRITE_SETTING);
            }
        }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissions != null && permissions.length > 0 && grantResults != null && grantResults.length > 0) {
            ArrayList<String> deniedPermissions = new ArrayList<>();
            for (String p : permissions) {
                boolean hasPermission = PermissionUtils.hasSelfPermissions(ShadowPermissionActivity.this, p);
                if (!hasPermission) {
                    deniedPermissions.add(p);
                }
            }               

            if (deniedPermissions.isEmpty()) {
                permissionGranted();
            } else {
                showPermissionDenyDialog(deniedPermissions.toArray(new String[deniedPermissions.size()]));
            }
        } else {
            if (PermissionUtils.hasSelfPermissions(ShadowPermissionActivity.this, this.permissions)) {
                permissionGranted();
            } else {
                showPermissionDenyDialog(this.permissions);
            }
        }
    }

    private void showRationaleDialog(final String[] needPermissions) {
        new AlertDialog.Builder(this)
                .setMessage(rationaleMessage)
                .setCancelable(false)
                .setPositiveButton(rationaleConfirmText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(ShadowPermissionActivity.this, needPermissions, REQ_CODE_PERMISSION_REQUEST);
                    }
                }).show();
    }

    public void showPermissionDenyDialog(String[] deniedPermissions) {
        String defaultDenyMsg;
        if (deniedPermissions == null || deniedPermissions.length == 0) {
            defaultDenyMsg = String.format(getString(R.string.permission_denied_msg_default), getString(R.string.permission_name));
        } else {
            StringBuilder pb = new StringBuilder();
            int size = deniedPermissions.length;
            for (int i = 0; i < size; i++) {
                pb.append(CheckPermission.PERMISSION_MAP.get(deniedPermissions[i]));
                if ( i != (size - 1)) {
                    pb.append(',');
                }
            }
            defaultDenyMsg = String.format(getString(R.string.permission_denied_msg_default), pb.toString());
        }
        denyMessage = TextUtils.isEmpty(denyMessage) ? defaultDenyMsg : denyMessage;
        deniedCloseButtonText = TextUtils.isEmpty(deniedCloseButtonText) ? getString(R.string.permission_close) : deniedCloseButtonText;

        if (!hasSettingButton) {
            Toast.makeText(ShadowPermissionActivity.this, denyMessage, Toast.LENGTH_LONG).show();
            permissionDenied();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(denyMessage)
                .setCancelable(false)
                .setNegativeButton(deniedCloseButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        permissionDenied();
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
                checkPermissions(CHECK_FROM_SETTING);
                break;
            }
            case REQ_CODE_REQUEST_SYSTEM_ALERT_WINDOW: {
                hasRequestedSystemAlertWindow = true;
                checkPermissions(CHECK_FROM_SYS_ALERT_WINDOW);
                break;
            }
            case REQ_CODE_REQUEST_WRITE_SETTING: {
                hasRequestedWriteSettings = true;
                checkPermissions(CHECK_FROM_WRITE_SETTING);
                break;
            }
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * 权限请求结束的回调
     */
    public static interface OnPermissionRequestFinishedListener {
        /**
         *
         * @param permissions 已经处理完的权限申请
         * @return 是否还有其他未处理的权限
         */
        public boolean onPermissionRequestFinishedAndCheckNext(String[] permissions);
    }

}
