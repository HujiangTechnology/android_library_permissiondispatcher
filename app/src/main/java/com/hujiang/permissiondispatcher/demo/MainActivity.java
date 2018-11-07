package com.hujiang.permissiondispatcher.demo;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hujiang.permissiondispatcher.CheckPermission;
import com.hujiang.permissiondispatcher.CheckPermissionHolder;
import com.hujiang.permissiondispatcher.PermissionItem;
import com.hujiang.permissiondispatcher.PermissionListener;
import com.hujiang.permissiondispatcher.PermissionUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "MainActivity";

    private static final String PERMISSION_FORMAT = "%s %s";

    Button mButtonCalendar;
    TextView mTextCalendar;
    Button mButtonCamera;
    TextView mTextCamera;
    Button mButtonContact;
    TextView mTextContact;
    Button mButtonLocation;
    TextView mTextLocation;
    Button mButtonMicPhone;
    TextView mTextMicPhone;
    Button mButtonBodySensor;
    TextView mTextBodySensor;
    Button mButtonSMS;
    TextView mTextSMS;
    Button mButtonExtStorage;
    TextView mTextExtStorage;
    Button mButtonWriteSetting;
    TextView mTextWriteSetting;
    Button mButtonSysWindow;
    TextView mTextSysWindow;
    Button mButtonMix;
    TextView mTextMix;

    void onCalendarClick(View view) {
        grantPermission(mTextCalendar, Manifest.permission.WRITE_CALENDAR);
    }

    void onCameraClick(View view) {
        grantPermission(mTextCamera, Manifest.permission.CAMERA);
    }

    void onContactClick(View view) {
        grantPermission(mTextContact, Manifest.permission.WRITE_CONTACTS);
    }

    void onLocationClick(View view) {
        grantPermission(mTextLocation, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    void onMicPhoneClick(View view) {
        grantPermission(mTextMicPhone, Manifest.permission.RECORD_AUDIO);
    }

    void onBodySensorClick(View view) {
        grantPermission(mTextBodySensor, Manifest.permission.BODY_SENSORS);
    }

    void onSMSClick(View view) {
        grantPermission(mTextSMS, Manifest.permission.SEND_SMS);
    }

    void onExtStorageClick(View view) {
        grantPermission(mTextExtStorage, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    void onWriteSettingClick(View view) {
        grantPermission(mTextWriteSetting, Manifest.permission.WRITE_SETTINGS);
    }

    void onSysWindowClick(View view) {
        grantPermission(mTextSysWindow, Manifest.permission.SYSTEM_ALERT_WINDOW);
    }

    CheckPermission mCheckPermission;

   void grantPermission(final TextView textView, final String permission) {
       if (mCheckPermission == null) {
           mCheckPermission = CheckPermission.create(this);
       }
       mCheckPermission.check(new PermissionItem(permission).needGotoSetting(true), new PermissionListener() {
            @Override
            public void permissionGranted() {
                updatePermissionItemInfo(textView, permission);
                startBActivity();
            }

            @Override
            public void permissionDenied() {
                updatePermissionItemInfo(textView, permission);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mButtonCalendar = (Button)findViewById(R.id.btn_calendar_req);
        mTextCalendar = (TextView)findViewById(R.id.txt_calendar_per);
        mButtonCamera = (Button)findViewById(R.id.btn_camera_req);
        mTextCamera = (TextView)findViewById(R.id.txt_camera_per);
        mButtonContact = (Button)findViewById(R.id.btn_contact_req);
        mTextContact = (TextView)findViewById(R.id.txt_contact_per);
        mButtonBodySensor = (Button)findViewById(R.id.btn_bodysensor_req);
        mTextBodySensor = (TextView)findViewById(R.id.txt_bodysensor_per);
        mButtonExtStorage = (Button)findViewById(R.id.btn_extstorage_req);
        mTextExtStorage = (TextView)findViewById(R.id.txt_extstorage_per);
        mButtonLocation = (Button)findViewById(R.id.btn_location_req);
        mTextLocation = (TextView)findViewById(R.id.txt_location_per);
        mButtonMicPhone = (Button)findViewById(R.id.btn_micphone_req);
        mTextMicPhone = (TextView)findViewById(R.id.txt_micphone_per);
        mButtonSMS = (Button)findViewById(R.id.btn_sms_req);
        mTextSMS = (TextView)findViewById(R.id.txt_sms_per);
        mButtonSysWindow = (Button)findViewById(R.id.btn_sysalertwindow_req);
        mTextSysWindow = (TextView)findViewById(R.id.txt_sysalertwindow_per);
        mButtonWriteSetting = (Button)findViewById(R.id.btn_writesetting_req);
        mTextWriteSetting = (TextView)findViewById(R.id.txt_writesetting_per);
        mButtonMix = (Button)findViewById(R.id.btn_mix_permission);
        mTextMix = (TextView)findViewById(R.id.txt_mix_permission);

        mButtonWriteSetting.setOnClickListener(this);
        mButtonContact.setOnClickListener(this);
        mButtonSysWindow.setOnClickListener(this);
        mButtonSMS.setOnClickListener(this);
        mButtonMicPhone.setOnClickListener(this);
        mButtonBodySensor.setOnClickListener(this);
        mButtonLocation.setOnClickListener(this);
        mButtonCalendar.setOnClickListener(this);
        mButtonCamera.setOnClickListener(this);
        mButtonExtStorage.setOnClickListener(this);
        mButtonMix.setOnClickListener(this);

        updatePermissionStatus();
    }

    private void startBActivity() {
        startActivity(new Intent(MainActivity.this, BActivity.class));
    }

    @SuppressWarnings("deprecation")
    private void updatePermissionStatus() {
        updatePermissionItemInfo(mTextCalendar, Manifest.permission.WRITE_CALENDAR);
        updatePermissionItemInfo(mTextCamera, Manifest.permission.CAMERA);
        updatePermissionItemInfo(mTextContact, Manifest.permission.WRITE_CONTACTS);
        updatePermissionItemInfo(mTextLocation, Manifest.permission.ACCESS_FINE_LOCATION);
        updatePermissionItemInfo(mTextMicPhone, Manifest.permission.RECORD_AUDIO);
        updatePermissionItemInfo(mTextBodySensor, Manifest.permission.BODY_SENSORS);
        updatePermissionItemInfo(mTextSMS, Manifest.permission.SEND_SMS);
        updatePermissionItemInfo(mTextExtStorage, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        updatePermissionItemInfo(mTextSysWindow, Manifest.permission.SYSTEM_ALERT_WINDOW);
        updatePermissionItemInfo(mTextWriteSetting, Manifest.permission.WRITE_SETTINGS);

        //mix permission init

    }

    private void updatePermissionItemInfo(TextView textView, String permission) {
        boolean hasPermission = PermissionUtils.hasSelfPermissions(this, permission);
        textView.setText(String.format(PERMISSION_FORMAT, permission,  hasPermission ? "granted" : "Denied"));
        textView.setTextColor(hasPermission ? Color.GREEN : Color.RED);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_calendar_req) {
            onCalendarClick(v);
        } else if (id == R.id.btn_writesetting_req) {
            onWriteSettingClick(v);
        } else if (id == R.id.btn_sysalertwindow_req) {
            onSysWindowClick(v);
        } else if (id == R.id.btn_sms_req) {
            onSMSClick(v);
        } else if (id == R.id.btn_bodysensor_req) {
            onBodySensorClick(v);
        } else if (id == R.id.btn_camera_req) {
            onCameraClick(v);
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    startBActivity();
//                }
//            }, 500);
        } else if (id == R.id.btn_contact_req) {
            onContactClick(v);
        } else if (id == R.id.btn_extstorage_req) {
            onExtStorageClick(v);
        } else if (id == R.id.btn_location_req) {
            onLocationClick(v);
        } else if (id == R.id.btn_micphone_req) {
            onMicPhoneClick(v);
        } else if (id == R.id.btn_mix_permission) {
            CheckPermission.instance(this).check(new PermissionItem(Manifest.permission.CAMERA
                    , Manifest.permission.READ_CONTACTS
                    , Manifest.permission.WRITE_SETTINGS
                    , Manifest.permission.SYSTEM_ALERT_WINDOW)
                    .needGotoSetting(true), new PermissionListener() {
                @Override
                public void permissionGranted() {
                    startBActivity();
                }

                @Override
                public void permissionDenied() {

                }
            });
        }
    }
}
