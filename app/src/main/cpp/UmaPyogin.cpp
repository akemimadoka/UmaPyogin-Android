#include <UmaPyogin/Plugin.h>

#include <jni.h>
#include <dlfcn.h>
#include <android/log.h>
#include <android/permission_manager.h>

#include <And64InlineHook.hpp>

extern "C"
{
void *fake_dlopen(const char *libpath, int flags);
void *fake_dlsym(void *handle, const char *name);
int fake_dlclose(void *handle);
}

namespace
{
class AndroidHookInstaller : public UmaPyogin::HookInstaller
{
public:
    explicit AndroidHookInstaller(const std::string& il2cppLibraryPath)
        : m_Il2CppLibrary(fake_dlopen(il2cppLibraryPath.c_str(), RTLD_LAZY))
    {
    }

    ~AndroidHookInstaller() override {
        fake_dlclose(m_Il2CppLibrary);
    }

    void InstallHook(UmaPyogin::OpaqueFunctionPointer addr, UmaPyogin::OpaqueFunctionPointer hook,
                     UmaPyogin::OpaqueFunctionPointer* orig) override
    {
        A64HookFunction(reinterpret_cast<void*>(addr), reinterpret_cast<void*>(hook), reinterpret_cast<void**>(orig));
    }

    UmaPyogin::OpaqueFunctionPointer LookupSymbol(const char* name) override
    {
        return reinterpret_cast<UmaPyogin::OpaqueFunctionPointer>(fake_dlsym(m_Il2CppLibrary, name));
    }

private:
    void* m_Il2CppLibrary;
};
}

extern "C"
JNIEXPORT void JNICALL
Java_moe_madoka_umapyogin_1android_UmaPyogin_initHook(JNIEnv *env, jclass clazz, jstring basePath, jstring targetLibraryPath,
                   jboolean unlock_fps) {
    const auto targetLibraryPathChars = env->GetStringUTFChars(targetLibraryPath, nullptr);
    const std::string targetLibraryPathStr = targetLibraryPathChars;
    env->ReleaseStringUTFChars(targetLibraryPath, targetLibraryPathChars);
    const auto basePathChars = env->GetStringUTFChars(basePath, nullptr);
    const std::string basePathStr = basePathChars;
    env->ReleaseStringUTFChars(basePath, basePathChars);

    auto& plugin = UmaPyogin::Plugin::GetInstance();
    plugin.SetLogHandler([](UmaPyogin::Log::Level level, const char* msg) {
        __android_log_write(level == UmaPyogin::Log::Level::Error ? ANDROID_LOG_ERROR : ANDROID_LOG_INFO, "UmaPyogin", msg);
    });
    plugin.LoadConfig(UmaPyogin::Config{
            .StaticLocalizationFilePath = basePathStr + "/static.json",
            .StoryLocalizationDirPath = basePathStr + "/stories",
            .TextDataDictPath = basePathStr + "/database/text_data.json",
            .CharacterSystemTextDataDictPath = basePathStr + "/database/character_system_text.json",
            .RaceJikkyoCommentDataDictPath = basePathStr + "/database/race_jikkyo_comment.json",
            .RaceJikkyoMessageDataDictPath = basePathStr + "/database/race_jikkyo_message.json",
            .ExtraAssetBundlePath = basePathStr + "/resources/umamusumelocalify",
            .ReplaceFontPath = "assets/bundledassets/umamusumelocalify/fonts/MSYH.TTC",
            .OverrideFPS = unlock_fps ? 60 : 0,
    });
    plugin.InstallHook(std::make_unique<AndroidHookInstaller>(targetLibraryPathStr));
}
