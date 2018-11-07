package com.hujiang.permissiondispatcher;

import java.util.HashMap;
import java.util.UUID;

/**
 * @author chenfei
 * @since 2018-11-05
 */
public class CheckPermissionHolder {

    private static CheckPermissionHolder sCheckPermissionHolder;

    static CheckPermissionHolder instance() {
        if (sCheckPermissionHolder == null) {
            synchronized (CheckPermissionHolder.class) {
                if (sCheckPermissionHolder == null) {
                    sCheckPermissionHolder = new CheckPermissionHolder();
                }
            }
        }
        return sCheckPermissionHolder;
    }

    public HashMap<String, CheckPermissionProxy> mPermissionCacheHashMap = new HashMap<>();

    CheckPermissionHolder() {}

    public CheckPermissionProxy find(String tag) {
        return mPermissionCacheHashMap.get(tag);
    }

    public CheckPermissionProxy find(CheckPermission checkPermission) {
        for (String key : mPermissionCacheHashMap.keySet()) {
            CheckPermissionProxy proxy = mPermissionCacheHashMap.get(key);
            if (proxy != null
                    && proxy.checkPermission != null
                    && proxy.checkPermission == checkPermission) {
                return proxy;
            }
        }
        return null;
    }

    public void remove(CheckPermission checkPermission) {
        CheckPermissionProxy proxy = find(checkPermission);
        if (proxy != null) {
            proxy.checkPermission = null;
            proxy.onPermissionRequestFinishedListener = null;
            proxy.permissionListener = null;
            mPermissionCacheHashMap.remove(proxy.tag);
        }
    }

    public CheckPermissionProxy add(CheckPermission checkPermission, ShadowPermissionActivity.OnPermissionRequestFinishedListener onPermissionRequestFinishedListener
            , PermissionListener permissionListener) {
        if (checkPermission != null) {
            CheckPermissionProxy findProxy = find(checkPermission);
            if (findProxy != null) {
                return findProxy;
            }

            CheckPermissionProxy newProxy = new CheckPermissionProxy(checkPermission, onPermissionRequestFinishedListener, permissionListener);
            mPermissionCacheHashMap.put(newProxy.tag, newProxy);
            return newProxy;
        }
        return null;
    }

    static class CheckPermissionProxy {

        CheckPermission checkPermission;
        String tag;
        volatile ShadowPermissionActivity.OnPermissionRequestFinishedListener onPermissionRequestFinishedListener;
        volatile PermissionListener permissionListener;

        CheckPermissionProxy(CheckPermission checkPermission, ShadowPermissionActivity.OnPermissionRequestFinishedListener onPermissionRequestFinishedListener
                , PermissionListener permissionListener) {
            this.tag = String.valueOf(UUID.randomUUID()) + String.valueOf(System.currentTimeMillis());
            this.checkPermission = checkPermission;
            this.onPermissionRequestFinishedListener = onPermissionRequestFinishedListener;
            this.permissionListener = permissionListener;
        }
    }
}
