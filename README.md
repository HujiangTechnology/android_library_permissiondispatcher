android_library_permissiondispatcher
------------------------------------

实现动态权限统一分发的功能，整合了`SYSTEM_ALERT_WINDOW`和`WRITE_SETTINGS`权限判断逻辑

### 依赖

```
com.hujiang.permissiondispatcher:permissiondispatcher:0.0.1
```

[最新版本可以从这个查询到](http://192.168.156.142:8081/nexus/#nexus-search;quick~com.hujiang.permissiondispatcher)

### 使用

* CheckPermission

在需要动态权限逻辑判断的是否使用该接口

```

 CheckPermission.from(this)
                .setPermissions(dangerousPermissionCamera)
                .setRationaleMsg("I need your camera to xxx")
                .setRationaleConfirmText("Request Camera Permission")
                .setDeniedMsg("The Camera Permission Denied")
                .check(new PermissionListener() {
                            @Override
                            public void permissionGranted() {
                                Toast.makeText(MainActivity.this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();
                                updatePermissionStatus();
                            }

                            @Override
                            public void permissionDenied() {
                                Toast.makeText(MainActivity.this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
                                updatePermissionStatus();
                            }
                        });
```

* @NeedPermission

这是一个用于标识类及方法所需权限的注解，用于Activity及Fragment，用在其他类不生效，需要配合AOPlib使用才有效

```

//用户Activity 类
@NeedPermission(permissions = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS})
public class MainActivity extents Activity {
}

//用户Activity 或者Fragment的内部方法
 @NeedPermission(permissions = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS})
    private void startBActivity(String name, long id) {
    }

```