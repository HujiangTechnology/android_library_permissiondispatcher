package com.hujiang.permissiondispatcher;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.StringRes;

import java.security.acl.Permission;

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
    private final Context mContext;

    private String[] mPermissions;
    private String   mRationaleConfirmText;
    private String   mRationaleMessage;

    private String   mDenyMessage;
    private String   mDeniedCloseButtonText;

    private boolean  mHasSettingBtn = false;

    public CheckPermission(Context context) {
        this.mContext = context;
    }

    public static CheckPermission from(Context context) {
        return new CheckPermission(context);
    }

    /**
     * ask for permissions
     * @param permissions
     * @return
     */
    public CheckPermission setPermissions(String... permissions) {
        this.mPermissions = permissions;
        return this;
    }

    /**
     * explain to the user why your app wants the permissions
     * @param rationaleMessage
     * @return
     */

    public CheckPermission setRationaleMsg(String rationaleMessage) {
        this.mRationaleMessage = rationaleMessage;
        return this;
    }

    /**
     * explain to the user why your app wants the permissions
     * @param stringRes
     * @return
     */
    public CheckPermission setRationaleMsg(@StringRes int stringRes) {
        if (stringRes <= 0) {
            throw new IllegalArgumentException("Invalid value for RationaleMessage");
        }
        this.mRationaleMessage = mContext.getString(stringRes);
        return this;
    }

    /**
     * The text to display in the positive button of rationale message dialog
     * @param rationaleConfirmText
     * @return
     */
    public CheckPermission setRationaleConfirmText(String rationaleConfirmText) {

        this.mRationaleConfirmText = rationaleConfirmText;
        return this;
    }
    /**
     * The text to display in the positive button of rationale message dialog
     * @param stringRes
     * @return
     */
    public CheckPermission setRationaleConfirmText(@StringRes int stringRes) {

        if (stringRes <= 0) {
            throw new IllegalArgumentException("Invalid value for RationaleConfirmText");
        }
        this.mRationaleConfirmText = mContext.getString(stringRes);

        return this;
    }


    /**
     * when user deny permission, show deny message
     * @param denyMessage
     * @return
     */
    public CheckPermission setDeniedMsg(String denyMessage) {
        this.mDenyMessage = denyMessage;
        return this;
    }
    /**
     * when user deny permission, show deny message
     * @param stringRes
     * @return
     */
    public CheckPermission setDeniedMsg(@StringRes int stringRes) {
        if (stringRes <= 0) {
            throw new IllegalArgumentException("Invalid value for DeniedMessage");
        }
        this.mDenyMessage = mContext.getString(stringRes);
        return this;
    }


    /**
     * The text to display in the close button of deny message dialog
     * @param stringRes
     * @return
     */
    public CheckPermission setDeniedCloseButtonText(@StringRes int stringRes) {

        if (stringRes <= 0) {
            throw new IllegalArgumentException("Invalid value for DeniedCloseButtonText");
        }
        this.mDeniedCloseButtonText = mContext.getString(stringRes);

        return this;
    }
    /**
     * The text to display in the close button of deny message dialog
     * @param deniedCloseButtonText
     * @return
     */
    public CheckPermission setDeniedCloseButtonText(String deniedCloseButtonText) {

        this.mDeniedCloseButtonText = deniedCloseButtonText;
        return this;
    }

    /**
     * when user deny permission,show the setting button
     * @param hasSettingBtn
     * @return
     */
    public CheckPermission setGotoSettingButton(boolean hasSettingBtn) {

        this.mHasSettingBtn = hasSettingBtn;
        return this;
    }


    // requestPermissions
    public void check(PermissionListener permissionListener) {

        if (permissionListener == null) {
            throw new NullPointerException("You must setPermissionListener() on CheckPermission");
        }

        if (mPermissions == null || mPermissions.length == 0) {
            throw new NullPointerException("You must setPermissions() on CheckPermission");
        }

        if (!PermissionUtils.isOverMarshmallow()) {
            if (permissionListener != null) {
                permissionListener.permissionGranted();
            }
        } else if (!PermissionUtils.hasSelfPermissions(mContext, mPermissions)) {
            requestPermissions(permissionListener);
        } else {
            if (permissionListener != null) {
                permissionListener.permissionGranted();
            }
        }
    }

    private void requestPermissions(PermissionListener permissionListener) {
        ShadowPermissionActivity.start(mContext
                , mPermissions
                , mRationaleMessage
                , mRationaleConfirmText
                , mHasSettingBtn
                , mDenyMessage
                , mDeniedCloseButtonText
                , permissionListener);
    }

}
