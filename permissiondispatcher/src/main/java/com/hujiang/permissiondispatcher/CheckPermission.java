package com.hujiang.permissiondispatcher;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;

import java.security.acl.Permission;
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

        ShadowPermissionActivity.setOnPermissionRequestFinishedListener(new ShadowPermissionActivity.onPermissionRequestFinishedListener() {
            @Override
            public boolean onPermissionRequestFinishedAndCheckNext(String[] permissions) {
                mCurPermissionRequestWrapper = mPermissionRequestWrappers.poll();

                if (mCurPermissionRequestWrapper != null) {
                    requestPermissions(mCurPermissionRequestWrapper);
                }

                return mCurPermissionRequestWrapper != null;
            }
        });
    }

    public void check(PermissionItem permissionItem, PermissionListener permissionListener) {
        if (permissionItem == null || permissionListener == null) {
            return;
        }

        if (!PermissionUtils.isOverMarshmallow()) {
            if (permissionListener != null) {
                permissionListener.permissionGranted();
            }
        } else if (PermissionUtils.hasSelfPermissions(mContext, permissionItem.permissions)) {
            if (permissionListener != null) {
                permissionListener.permissionGranted();
            }
        } else {

            mPermissionRequestWrappers.add(new PermissionRequestWrapper(permissionItem, permissionListener));

            if (mCurPermissionRequestWrapper == null) {
                mCurPermissionRequestWrapper = mPermissionRequestWrappers.poll();
                requestPermissions(mCurPermissionRequestWrapper);
            }
        }
    }

    private void requestPermissions(PermissionRequestWrapper permissionRequestWrapper) {
        PermissionItem item = permissionRequestWrapper.permissionItem;
        PermissionListener listener = permissionRequestWrapper.permissionListener;

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

    class PermissionRequestWrapper {
        PermissionItem permissionItem;
        PermissionListener permissionListener;

        public PermissionRequestWrapper(PermissionItem item, PermissionListener listener) {
            this.permissionItem = item;
            this.permissionListener = listener;
        }
    }

}
