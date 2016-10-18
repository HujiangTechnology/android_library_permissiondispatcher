package com.hujiang.permissiondispatcher.demo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hujiang.permissiondispatcher.CheckPermission;
import com.hujiang.permissiondispatcher.NeedPermission;
import com.hujiang.permissiondispatcher.PermissionItem;
import com.hujiang.permissiondispatcher.PermissionListener;
import com.hujiang.permissiondispatcher.PermissionUtils;


@NeedPermission(permissions = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS})
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "MainActivity";

    String[] normalPermission = new String[]{Manifest.permission.INTERNET};

    String[] dangerousPermissionCamera = new String[]{Manifest.permission.CAMERA};

    String[] dangerousPermissionLocation = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};

    //SYSTEM_ALERT_WINDOW write permission
    String[] systemAlertWindowPermission = new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW};
        //system settings write permission
    String[] settingsPermission = new String[]{Manifest.permission.WRITE_SETTINGS};

    TextView mTvPermissionInternet;
    TextView mTvPermissionCamera;
    TextView mTvPermissionLocation;
    TextView mTvPermissionSystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTvPermissionInternet = (TextView) findViewById(R.id.tv_permission_internet);
        mTvPermissionCamera = (TextView) findViewById(R.id.tv_permission_camera);
        mTvPermissionLocation = (TextView) findViewById(R.id.tv_permission_location);
        mTvPermissionSystem = (TextView) findViewById(R.id.tv_permission_system);
        findViewById(R.id.btn_request_permission_all).setOnClickListener(this);
        findViewById(R.id.btn_request_permission_internet).setOnClickListener(this);
        findViewById(R.id.btn_request_permission_camera).setOnClickListener(this);
        findViewById(R.id.btn_request_permission_location).setOnClickListener(this);
        findViewById(R.id.btn_request_permission_system).setOnClickListener(this);
        findViewById(R.id.btn_open_permission_setting).setOnClickListener(this);
        findViewById(R.id.btn_open_battery_optimization_settings).setOnClickListener(this);
        findViewById(R.id.btn_start_b_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startB();
            }
        });

        updatePermissionStatus();

    }

    private void startB() {
        CheckPermission.instance(this).check(new PermissionItem(Manifest.permission.READ_CALL_LOG), new PermissionListener() {
            @Override
            public void permissionGranted() {
                startActivity(new Intent(MainActivity.this, BActivity.class));
            }

            @Override
            public void permissionDenied() {

            }
        });
    }

    @SuppressWarnings("deprecation")
    private void updatePermissionStatus() {
        boolean isGranted = PermissionUtils.hasSelfPermissions(this, normalPermission);
        mTvPermissionInternet.setText(getString(R.string.normal_permission_status, isGranted ? "Granted" : "Denied"));
        mTvPermissionInternet.setTextColor(getResources().getColor(isGranted ? R.color.green : R.color.red));

        isGranted = PermissionUtils.hasSelfPermissions(this, dangerousPermissionCamera);
        mTvPermissionCamera.setText(getString(R.string.dangerous_permission_status, isGranted ? "Granted" : "Denied"));
        mTvPermissionCamera.setTextColor(getResources().getColor(isGranted ? R.color.green : R.color.red));

        isGranted = PermissionUtils.hasSelfPermissions(this, dangerousPermissionLocation);
        mTvPermissionLocation.setText(getString(R.string.dangerous_permission_status, isGranted ? "Granted" : "Denied"));
        mTvPermissionLocation.setTextColor(getResources().getColor(isGranted ? R.color.green : R.color.red));

        isGranted = PermissionUtils.hasSelfPermissions(this, systemAlertWindowPermission);
        mTvPermissionSystem.setText(getString(R.string.system_permission_status, isGranted ? "Granted" : "Denied"));
        mTvPermissionSystem.setTextColor(getResources().getColor(isGranted ? R.color.green : R.color.red));
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_request_permission_all:
                break;
            case R.id.btn_request_permission_camera:
                break;
            case R.id.btn_request_permission_location:
                break;
            case R.id.btn_request_permission_system:
                break;
            case R.id.btn_request_permission_internet:
                break;
            case R.id.btn_open_permission_setting:
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                break;
            case R.id.btn_open_battery_optimization_settings:
                if(PermissionUtils.isOverMarshmallow()) {
                    Intent batteryIntent = new Intent();
                    String packageName = getPackageName();
                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    //http://developer.android.com/intl/ko/training/monitoring-device-state/doze-standby.html#support_for_other_use_cases
                    if (pm.isIgnoringBatteryOptimizations(packageName))
                        batteryIntent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    else {
                        batteryIntent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        batteryIntent.setData(Uri.parse("package:" + packageName));
                    }

                    startActivity(batteryIntent);
                } else {
                    Toast.makeText(MainActivity.this, "battery optimizations on Android6.0", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
