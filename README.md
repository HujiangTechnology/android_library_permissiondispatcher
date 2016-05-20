android_library_permissiondispatcher
------------------------------------

实现动态权限统一分发的功能，整合了`SYSTEM_ALERT_WINDOW`和`WRITE_SETTINGS`权限判断逻辑

### 依赖

```
com.hujiang.permissiondispatcher:permissiondispatcher:1.0.0
```

[最新版本可以从这个查询到](https://bintray.com/firefly1126/maven/permissiondispatcher/view)

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

用于标识类，方法需要哪些权限

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

### License

```
Copyright (C) 2016 Hujiang, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```