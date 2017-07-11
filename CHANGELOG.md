### v1.0.11（2017-07-11）
* 修复ZUK手机上PermissionChecker check权限返回-2，却在onRequestPermissionsResult返回0的Bug

### v1.0.10(2017-06-27)
* optimize the work flow of the permission check
* friendly and clear toast when permission denied

### v1.0.9(2016-10-18)
* optimize the logic flow with permission request queue

### v1.0.5(2016-07-15)
* @NeedPermission supports ResId
* @NeedPermission#needGotoSetting default true

### v1.0.4(2016-07-12)
* fix app label shown on the transparent activity

### v1.0.3(2016-07-11)
* add NeedPermission.runIgnorePermission


### v1.0.2

* 当系统版本低于6.0时，自动授予所有权限。