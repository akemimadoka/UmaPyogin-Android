package moe.madoka.umapyogin_android

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import moe.madoka.umapyogin_android.databinding.ActivityMainBinding

data class PreferenceKey<T>(val name: String, val defaultValue: T)

fun SharedPreferences.get(key: PreferenceKey<Boolean>): Boolean {
    return getBoolean(key.name, key.defaultValue)
}

fun SharedPreferences.get(key: PreferenceKey<Int>): Int {
    return getInt(key.name, key.defaultValue)
}

fun SharedPreferences.get(key: PreferenceKey<Long>): Long {
    return getLong(key.name, key.defaultValue)
}

fun SharedPreferences.get(key: PreferenceKey<Float>): Float {
    return getFloat(key.name, key.defaultValue)
}

fun SharedPreferences.get(key: PreferenceKey<String?>): String? {
    return getString(key.name, key.defaultValue)
}

fun SharedPreferences.get(key: PreferenceKey<Set<String?>?>): Set<String?>? {
    return getStringSet(key.name, key.defaultValue)
}

fun SharedPreferences.Editor.put(key: PreferenceKey<Boolean>, value: Boolean) {
    putBoolean(key.name, value)
}

fun SharedPreferences.Editor.put(key: PreferenceKey<Int>, value: Int) {
    putInt(key.name, value)
}

fun SharedPreferences.Editor.put(key: PreferenceKey<Long>, value: Long) {
    putLong(key.name, value)
}

fun SharedPreferences.Editor.put(key: PreferenceKey<Float>, value: Float) {
    putFloat(key.name, value)
}

fun SharedPreferences.Editor.put(key: PreferenceKey<String?>, value: String?) {
    putString(key.name, value)
}

fun SharedPreferences.Editor.put(key: PreferenceKey<Set<String?>?>, value: Set<String?>?) {
    putStringSet(key.name, value)
}

data class UmaPyoginConfig(var enabled: Boolean, var unlockFPS: Boolean)

interface ConfigListener {
    fun onEnabledChanged(value: Boolean)
    fun onUnlockFPSChanged(value: Boolean)
}

class MainActivity : AppCompatActivity(), ConfigListener {
    lateinit var config: UmaPyoginConfig
    lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        @Suppress("DEPRECATION")
        @SuppressLint("WorldReadableFiles")
        preferences = applicationContext.getSharedPreferences(
            BuildConfig.APPLICATION_ID,
            Context.MODE_WORLD_READABLE
        )

        val binding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        config = UmaPyoginConfig(
            enabled = preferences.get(PREF_ENABLED),
            unlockFPS = preferences.get(PREF_UNLOCK_FPS),
        )
        binding.config = config
        binding.listener = this
    }

    override fun onEnabledChanged(value: Boolean) {
        val editor = preferences.edit()
        editor.put(PREF_ENABLED, value)
        editor.apply()
    }

    override fun onUnlockFPSChanged(value: Boolean) {
        val editor = preferences.edit()
        editor.put(PREF_UNLOCK_FPS, value)
        editor.apply()
    }

    companion object {
        val PREF_ENABLED = PreferenceKey("Enabled", true)
        val PREF_UNLOCK_FPS = PreferenceKey("UnlockFPS", true)
    }
}
