package com.nh.cowauction.utility

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.pm.PackageInfoCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Description : Device Provider Class
 *
 * Created by hmju on 2021-05-18
 */
interface DeviceProvider {
    fun getContext(): Context
    fun isNavigationBar(): Boolean
    fun getVersionName(): String
    fun getVersionCode(): Int
    fun getDeviceWidth(): Int
    fun getDeviceHeight(): Int
    fun getStatusBarHeight(): Int
    fun getNavigationBarHeight(): Int
    fun getMaxVolume(): Int
    fun getVolume(): Int
    fun setVolume(volume: Int)
    fun isPermissionsCheck(permissions: String): Boolean
}

@Suppress("DEPRECATION")
class DeviceProviderImpl @Inject constructor(
    @ApplicationContext private val ctx: Context
) : DeviceProvider {
    private val res by lazy { ctx.resources }
    private val audioManager: AudioManager by lazy { ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    override fun getContext() = ctx

    override fun isNavigationBar(): Boolean {
        val id = ctx.resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return id > 0 && res.getBoolean(id)
    }

    override fun getVersionName(): String {
        val packageInfo = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
        return packageInfo.versionName
    }

    override fun getVersionCode(): Int {
        val packageInfo = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageInfoCompat.getLongVersionCode(packageInfo).toInt()
        } else {
            packageInfo.versionCode
        }
    }

    override fun getDeviceWidth(): Int {
        return res.displayMetrics.widthPixels
    }

    override fun getDeviceHeight(): Int {
        return res.displayMetrics.heightPixels
    }

    override fun getStatusBarHeight(): Int {
        val id = res.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) res.getDimensionPixelSize(id) else -1
    }

    override fun getNavigationBarHeight(): Int {
        val id = res.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (id > 0) res.getDimensionPixelSize(id) else -1
    }

    override fun getMaxVolume() = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    override fun getVolume() = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

    override fun setVolume(volume: Int) {
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            volume,
            AudioManager.FLAG_SHOW_UI
        )
    }

    override fun isPermissionsCheck(permissions: String) =
        ContextCompat.checkSelfPermission(ctx, permissions) == PackageManager.PERMISSION_GRANTED
}