UmaPyogin-Android
====

[UmaPyogin](https://github.com/akemimadoka/UmaPyogin) 的 Android 实现

构建
----
需要安装 conan，并事先配置好 [conan-android_ndk](https://github.com/akemimadoka/conan-android_ndk)

注意
----
Android 11 下，因系统安全限制，仅有 edxposed 和 lsposed 能够读取应用配置，因此若你的设备在 Android 11 以上，并且正在使用如太极等非必须 root 的 xposed 框架时，你将可能无法通过插件配置禁用插件或禁用解锁 FPS 功能

当前不支持 armv7 设备，请等待适配

程序会尝试在游戏的目录下解压本地化数据，因此会导致游戏占用空间略微变大，并且不会随着插件的卸载而删除，这个问题预计在将来解决

解锁 FPS 功能会导致剧情的 AUTO 失效，若需要使用 AUTO，请不要使用解锁 FPS 功能
