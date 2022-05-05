package moe.madoka.umapyogin_android

import android.annotation.SuppressLint
import android.app.AndroidAppHelper
import android.content.res.XModuleResources
import android.util.Log
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File
import java.io.IOException
import java.io.InputStream

class UmaPyogin : IXposedHookLoadPackage, IXposedHookZygoteInit {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "jp.co.cygames.umamusume") {
            return
        }

        val cls = lpparam.classLoader.loadClass("com.unity3d.player.UnityPlayer")
        XposedHelpers.findAndHookMethod(
            cls,
            "loadNative",
            String::class.java,
            object : XC_MethodHook() {
                @SuppressLint("UnsafeDynamicallyLoadedCode")
                override fun afterHookedMethod(param: MethodHookParam) {
                    super.afterHookedMethod(param)

                    Log.i(TAG, "UnityPlayer.loadNative")

                    if (alreadyInitialized) {
                        return
                    }

                    val pref =
                        XSharedPreferences(BuildConfig.APPLICATION_ID, BuildConfig.APPLICATION_ID)
                    if (!pref.file.canRead() && !pref.makeWorldReadable()) {
                        Log.e(TAG, "Cannot load preference, all config will use default value")
                    }
                    val enabled = pref.get(MainActivity.PREF_ENABLED)
                    val unlockFPS = pref.get(MainActivity.PREF_UNLOCK_FPS)

                    if (!enabled) {
                        return
                    }

                    val app = AndroidAppHelper.currentApplication()
                    val filesDir = app.applicationContext.filesDir
                    val umaPyoginFiles = File(filesDir, "UmaPyoginFiles")
                    val versionFile = File(umaPyoginFiles, "version.txt")
                    val shouldCopyAssets = if (!umaPyoginFiles.exists()) {
                        umaPyoginFiles.mkdirs()
                        versionFile.writeText(BuildConfig.VERSION_NAME)
                        true
                    } else {
                        if (!versionFile.exists()) {
                            versionFile.writeText(BuildConfig.VERSION_NAME)
                            true
                        } else {
                            versionFile.readText() == BuildConfig.VERSION_NAME
                        }
                    }

                    if (shouldCopyAssets) {
                        val assets = XModuleResources.createInstance(modulePath, null).assets
                        fun forAllAssetFiles(
                            basePath: String,
                            action: (String, InputStream?) -> Unit
                        ) {
                            val assetFiles = assets.list(basePath)!!
                            for (file in assetFiles) {
                                try {
                                    assets.open("$basePath/$file")
                                } catch (e: IOException) {
                                    action("$basePath/$file", null)
                                    forAllAssetFiles("$basePath/$file", action)
                                    return
                                }.use {
                                    action("$basePath/$file", it)
                                }
                            }
                        }
                        forAllAssetFiles("UmaPyoginFiles") { path, file ->
                            val outFile = File(filesDir, path)
                            if (file == null) {
                                outFile.mkdirs()
                            } else {
                                outFile.outputStream().use { out ->
                                    file.copyTo(out)
                                }
                            }
                        }
                    }

                    // TODO: 解决硬编码，兼容 armv7
                    System.load("${File(modulePath).parent}/lib/arm64/libUmaPyogin.so")
                    initHook(
                        umaPyoginFiles.absolutePath,
                        "${app.applicationInfo.nativeLibraryDir}/libil2cpp.so",
                        unlockFPS
                    )

                    alreadyInitialized = true
                }
            })
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        modulePath = startupParam.modulePath
    }

    private lateinit var modulePath: String
    private var alreadyInitialized = false

    companion object {
        @JvmStatic
        external fun initHook(basePath: String, targetLibraryPath: String, unlockFPS: Boolean)

        const val TAG = "UmaPyogin"
    }
}
