package com.hujiang.permissiondispatcher;

import android.Manifest;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import junit.framework.Assert;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * check permission
 * <br>
 * modify from https://github.com/ParkSangGwon/TedPermission
 *
 * @author simon
 * @since 2016-05-15
 */
public class CheckPermission {
    private static final String TAG = "CheckPermission";

    public static final HashMap<String, String> PERMISSION_MAP = new HashMap<>();

    private static CheckPermission sInstance;
    private final Context mContext;
    private PermissionRequestWrapper mCurPermissionRequestWrapper;
    private Queue<PermissionRequestWrapper> mPermissionRequestWrappers = new ConcurrentLinkedQueue<>();
    private ShadowPermissionActivity.OnPermissionRequestFinishedListener mOnPermissionRequestFinishedListener = new ShadowPermissionActivity.OnPermissionRequestFinishedListener() {
        @Override
        public boolean onPermissionRequestFinishedAndCheckNext(String[] permissions) {
            mCurPermissionRequestWrapper = mPermissionRequestWrappers.poll();
            if (mCurPermissionRequestWrapper != null) {
                requestPermissions(mCurPermissionRequestWrapper);
            }

            return mCurPermissionRequestWrapper != null;
        }
    };

    public static CheckPermission instance(Context context) {
        if (sInstance == null) {
            synchronized (CheckPermission.class) {
                if (sInstance == null) {
                    sInstance = new CheckPermission(context);
                }
            }
        }

        return sInstance;
    }

    private CheckPermission(Context context) {
        this.mContext = context.getApplicationContext();

        PERMISSION_MAP.put(Manifest.permission.READ_CALENDAR, context.getString(R.string.permission_hint_calendar));
        PERMISSION_MAP.put(Manifest.permission.WRITE_CALENDAR, context.getString(R.string.permission_hint_calendar));
        PERMISSION_MAP.put(Manifest.permission.CAMERA, context.getString(R.string.permission_hint_camera));
        PERMISSION_MAP.put(Manifest.permission.READ_CONTACTS, context.getString(R.string.permission_hint_contacts));
        PERMISSION_MAP.put(Manifest.permission.WRITE_CONTACTS, context.getString(R.string.permission_hint_contacts));
        PERMISSION_MAP.put(Manifest.permission.GET_ACCOUNTS, context.getString(R.string.permission_hint_contacts));
        PERMISSION_MAP.put(Manifest.permission.ACCESS_FINE_LOCATION, context.getString(R.string.permission_hint_location));
        PERMISSION_MAP.put(Manifest.permission.ACCESS_COARSE_LOCATION, context.getString(R.string.permission_hint_location));
        PERMISSION_MAP.put(Manifest.permission.RECORD_AUDIO, context.getString(R.string.permission_hint_microphone));
        PERMISSION_MAP.put(Manifest.permission.READ_PHONE_STATE, context.getString(R.string.permission_hint_phone));
        PERMISSION_MAP.put(Manifest.permission.CALL_PHONE, context.getString(R.string.permission_hint_phone));
        PERMISSION_MAP.put(Manifest.permission.READ_CALL_LOG, context.getString(R.string.permission_hint_phone));
        PERMISSION_MAP.put(Manifest.permission.WRITE_CALL_LOG, context.getString(R.string.permission_hint_phone));
        PERMISSION_MAP.put(Manifest.permission.ADD_VOICEMAIL, context.getString(R.string.permission_hint_phone));
        PERMISSION_MAP.put(Manifest.permission.USE_SIP, context.getString(R.string.permission_hint_phone));
        PERMISSION_MAP.put(Manifest.permission.PROCESS_OUTGOING_CALLS, context.getString(R.string.permission_hint_phone));
        PERMISSION_MAP.put(Manifest.permission.BODY_SENSORS, context.getString(R.string.permission_hint_sensors));
        PERMISSION_MAP.put(Manifest.permission.SEND_SMS, context.getString(R.string.permission_hint_sms));
        PERMISSION_MAP.put(Manifest.permission.RECEIVE_SMS, context.getString(R.string.permission_hint_sms));
        PERMISSION_MAP.put(Manifest.permission.READ_SMS, context.getString(R.string.permission_hint_sms));
        PERMISSION_MAP.put(Manifest.permission.RECEIVE_WAP_PUSH, context.getString(R.string.permission_hint_sms));
        PERMISSION_MAP.put(Manifest.permission.RECEIVE_SMS, context.getString(R.string.permission_hint_sms));
        PERMISSION_MAP.put(Manifest.permission.READ_EXTERNAL_STORAGE, context.getString(R.string.permission_hint_storage));
        PERMISSION_MAP.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, context.getString(R.string.permission_hint_storage));
        PERMISSION_MAP.put(Manifest.permission.WRITE_SETTINGS, context.getString(R.string.permission_hint_write_setting));
        PERMISSION_MAP.put(Manifest.permission.SYSTEM_ALERT_WINDOW, context.getString(R.string.permission_hint_sys_alert_window));

        ShadowPermissionActivity.setOnPermissionRequestFinishedListener(mOnPermissionRequestFinishedListener);
    }

    public void check(PermissionItem permissionItem, PermissionListener permissionListener) {
        if (permissionItem == null || permissionListener == null) {
            return;
        }

        if (!PermissionUtils.isOverMarshmallow() || PermissionUtils.hasSelfPermissions(mContext, permissionItem.permissions)) {
            //permission granted
            onPermissionGranted(permissionItem, permissionListener);
        } else {
            //permission denied
            mPermissionRequestWrappers.add(new PermissionRequestWrapper(permissionItem, permissionListener));
            if (mCurPermissionRequestWrapper == null) {
                mCurPermissionRequestWrapper = mPermissionRequestWrappers.poll();
                requestPermissions(mCurPermissionRequestWrapper);
            }
        }
    }

    private void requestPermissions(PermissionRequestWrapper permissionRequestWrapper) {
        final PermissionItem item = permissionRequestWrapper.permissionItem;
        final PermissionListener listener = permissionRequestWrapper.permissionListener;
        ShadowPermissionActivity.start(mContext
                , item.permissions
                , item.rationalMessage
                , item.rationalButton
                , item.needGotoSetting
                , item.settingText
                , item.deniedMessage
                , item.deniedButton
                , listener);
    }

    private void onPermissionGranted(PermissionItem item, PermissionListener listener) {
        Assert.assertNotNull(item);

        if (listener != null) {
            listener.permissionGranted();
        }

        mOnPermissionRequestFinishedListener.onPermissionRequestFinishedAndCheckNext(item.permissions);
    }

    private void onPermissionDenied(PermissionItem item, PermissionListener listener) {
        Assert.assertNotNull(item);

        if (listener != null) {
            listener.permissionDenied();
        }

        mOnPermissionRequestFinishedListener.onPermissionRequestFinishedAndCheckNext(item.permissions);
    }

    class PermissionRequestWrapper {
        PermissionItem permissionItem;
        PermissionListener permissionListener;

        public PermissionRequestWrapper(PermissionItem item, PermissionListener listener) {
            this.permissionItem = item;
            this.permissionListener = listener;
        }
    }

}
