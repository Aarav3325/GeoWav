package com.aarav.geowav.core.permissions

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

object PermissionPrefs {
    const val IS_ONBOARDED = "isOnboarded"
    const val PERMISSION_SETUP_SKIPPED = "permissionSetupSkipped"
    const val LOCATION_EDUCATION_SEEN = "locationEducationSeen"
    const val NOTIFICATION_EDUCATION_SEEN = "notificationEducationSeen"
    const val BACKGROUND_EDUCATION_SEEN = "backgroundLocationEducationSeen"
}

enum class GeoPermissionKind {
    Notifications,
    ForegroundLocation,
    BackgroundLocation
}

data class GeoPermissionUiState(
    val notificationsGranted: Boolean = false,
    val foregroundLocationGranted: Boolean = false,
    val backgroundLocationGranted: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val permissionSetupSkipped: Boolean = false,
    val locationEducationSeen: Boolean = false,
    val notificationEducationSeen: Boolean = false,
    val backgroundEducationSeen: Boolean = false
) {
    val locationServicesReady: Boolean
        get() = foregroundLocationGranted && backgroundLocationGranted

    val allCorePermissionsGranted: Boolean
        get() = notificationsGranted && locationServicesReady

    val shouldShowSetupCard: Boolean
        get() = onboardingCompleted && !allCorePermissionsGranted

    val canRequestBackgroundLocation: Boolean
        get() = foregroundLocationGranted

    val nextRecommendedPermission: GeoPermissionKind?
        get() = when {
            !notificationsGranted -> GeoPermissionKind.Notifications
            !foregroundLocationGranted -> GeoPermissionKind.ForegroundLocation
            !backgroundLocationGranted -> GeoPermissionKind.BackgroundLocation
            else -> null
        }
}

@Singleton
class GeoPermissionCoordinator @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val prefs: SharedPreferences
) {
    private val _state = MutableStateFlow(readState())
    val state: StateFlow<GeoPermissionUiState> = _state.asStateFlow()

    fun refresh(): GeoPermissionUiState {
        val current = readState()
        _state.value = current
        return current
    }

    fun markOnboardingEducationComplete(skippedPermissionSetup: Boolean) {
        prefs.edit(commit = true) {
            putBoolean(PermissionPrefs.IS_ONBOARDED, true)
            putBoolean(PermissionPrefs.PERMISSION_SETUP_SKIPPED, skippedPermissionSetup)
            putBoolean(PermissionPrefs.LOCATION_EDUCATION_SEEN, true)
            putBoolean(PermissionPrefs.NOTIFICATION_EDUCATION_SEEN, true)
            putBoolean(PermissionPrefs.BACKGROUND_EDUCATION_SEEN, true)
        }
        refresh()
    }

    fun markPermissionSetupSkipped(skipped: Boolean) {
        prefs.edit {
            putBoolean(PermissionPrefs.PERMISSION_SETUP_SKIPPED, skipped)
        }
        refresh()
    }

    private fun readState(): GeoPermissionUiState {
        return GeoPermissionUiState(
            notificationsGranted = areNotificationsGranted(),
            foregroundLocationGranted = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION),
            backgroundLocationGranted = hasBackgroundLocation(),
            onboardingCompleted = prefs.getBoolean(PermissionPrefs.IS_ONBOARDED, false),
            permissionSetupSkipped = prefs.getBoolean(PermissionPrefs.PERMISSION_SETUP_SKIPPED, false),
            locationEducationSeen = prefs.getBoolean(PermissionPrefs.LOCATION_EDUCATION_SEEN, false),
            notificationEducationSeen = prefs.getBoolean(PermissionPrefs.NOTIFICATION_EDUCATION_SEEN, false),
            backgroundEducationSeen = prefs.getBoolean(PermissionPrefs.BACKGROUND_EDUCATION_SEEN, false)
        )
    }

    private fun areNotificationsGranted(): Boolean {
        val notificationToggleEnabled = NotificationManagerCompat
            .from(context)
            .areNotificationsEnabled()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return notificationToggleEnabled
        }

        return notificationToggleEnabled &&
            hasPermission(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun hasBackgroundLocation(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
        return hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}
