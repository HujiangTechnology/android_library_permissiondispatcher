package com.hujiang.permissiondispatcher;

/**
 * permissionlistener
 * <br>
 *
 * @author simon
 * @since 2016-05-15
 */
public interface PermissionListener {
    public void permissionGranted();
    public void permissionDenied();
}
