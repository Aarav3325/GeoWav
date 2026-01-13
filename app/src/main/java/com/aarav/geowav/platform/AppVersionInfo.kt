package com.aarav.geowav.platform

import android.content.Context
import android.content.pm.PackageManager

data class AppVersionInfo(
    val versionName: String,
    val major: Int,
    val minor: Int,
    val patch: Int
)

fun Context.getAppVersionInfo(): AppVersionInfo {
    val packageManager = packageManager
    val packageName = packageName

    val packageInfo = if (android.os.Build.VERSION.SDK_INT >= 33) {
        packageManager.getPackageInfo(
            packageName,
            PackageManager.PackageInfoFlags.of(0)
        )
    } else {
        packageManager.getPackageInfo(packageName, 0)
    }

    val versionName = packageInfo.versionName ?: "0.0.0"

    val parts = versionName.split(".")
    val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0



    return AppVersionInfo(
        versionName = versionName,
        major = major,
        minor = minor,
        patch = patch
    )
}
