/*
 * Permissions      2016-05-12
 * Copyright (c) 2016 hujiang Co.Ltd. All right reserved(http://www.hujiang.com).
 * 
 */
package com.hujiang.permissiondispatcher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * to mark the class type or method that needs runtime permissions.Just used on Activity class, and all void returned methods of any class.
 *
 * @author simon
 * @version 1.0.0
 * @since 2016-05-12
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NeedPermission {
    /**
     * 你所申请的权限列表，例如 {@link android.Manifest.permission#READ_CONTACTS}
     * @return 权限列表
     * @see android.Manifest.permission
     */
    String[] permissions() default "";

    /**
     * 合理性解释内容
     * @return 合理性解释内容
     */
    String rationalMessage() default "";

    /**
     * 合理性解释文本资源ID
     * @return
     */
    int rationalMsgResId() default 0;


    /**
     * 合理性解释按钮文本
     * @return 合理性解释按钮文本
     */
    String rationalButton() default "";

    /**
     * 合理性解释按钮文本资源ID
     * @return
     */
    int rationalBtnResId() default 0;

    /**
     * 权限禁止文本内容
     * @return 权限禁止文本内容
     */
    String deniedMessage() default "";

    /**
     * 权限禁止文本资源ID
     * @return
     */
    int deniedMsgResId() default 0;

    /**
     * 权限禁止按钮文本
     * @return 权限禁止按钮文本
     */
    String deniedButton() default "";

    /**
     * 权限禁止按钮文本资源ID
     * @return
     */
    int deniedBtnResId() default 0;

    /**
     * app设置按钮文本
     * @return
     */
    String settingText() default "";

    /**
     * app设置按钮文本资源ID
     * @return
     */
    int settingResId() default 0;

    /**
     * 是否显示跳转到应用权限设置界面
     * @return 是否显示跳转到应用权限设置界面
     */
    boolean needGotoSetting() default false;

    /**
     * 是否无视权限，程序正常往下走
     * @return 是否无视权限，程序正常往下走
     */
    boolean runIgnorePermission() default false;
}
