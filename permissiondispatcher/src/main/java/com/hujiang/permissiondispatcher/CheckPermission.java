package com.hujiang.permissiondispatcher;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import junit.framework.Assert;

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

        ShadowPermissionActivity.setOnPermissionRequestFinishedListener(mOnPermissionRequestFinishedListener);
    }

    public void check(PermissionItem permissionItem, PermissionListener permissionListener) {
        if (permissionItem == null || permissionListener == null) {
            return;
        }

        if (!PermissionUtils.isOverMarshmallow()) {
            onPermissionGranted(permissionItem, permissionListener);
        } else {
            mPermissionRequestWrappers.add(new PermissionRequestWrapper(permissionItem, permissionListener));
            if (mCurPermissionRequestWrapper == null) {
                mCurPermissionRequestWrapper = mPermissionRequestWrappers.poll();
                requestPermissions(mCurPermissionRequestWrapper);
            }
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private void requestPermissions(PermissionRequestWrapper permissionRequestWrapper) {
        final PermissionItem item = permissionRequestWrapper.permissionItem;
        final PermissionListener listener = permissionRequestWrapper.permissionListener;

        if (PermissionUtils.hasSelfPermissions(mContext, item.permissions)) {
            onPermissionGranted(item, listener);
        } else {
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
