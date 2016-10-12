android_library_permissiondispatcher
------------------------------------

实现动态权限统一分发的功能，整合了`SYSTEM_ALERT_WINDOW`和`WRITE_SETTINGS`权限判断逻辑

### 依赖

```
com.hujiang.permissiondispatcher:permissiondispatcher:1.0.2
```

[最新版本可以从这个查询到](https://bintray.com/firefly1126/maven/permissiondispatcher/view)

### 使用

* CheckPermission

在需要动态权限逻辑判断的是否使用该接口

```

 CheckPermission.instance(context)
                .check(permissionItem, new PermissionListener() {
                            @Override
                            public void permissionGranted() {
                                //授权成功，执行正常的业务逻辑
                            }

                            @Override
                            public void permissionDenied() {
                                //授权失败，给用户友好提示
                            }
                        });
```

* @NeedPermission

`待完善...`

### [CHANGELOG](CHANGELOG.md)

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


