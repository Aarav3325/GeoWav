package com.aarav.geowav.presentation.settings

import android.app.Application
import android.content.SharedPreferences
import android.health.connect.datatypes.AppInfo
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.platform.AppVersionInfo
import com.aarav.geowav.platform.getAppVersionInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher

@HiltViewModel
class SettingsVM @Inject constructor(
    private val prefs: SharedPreferences,
    application: Application,
    private val googleSignInClient: GoogleSignInClient
) : AndroidViewModel(application) {

    private val _uiState: MutableStateFlow<SettingsUiState> = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _themeMode: MutableStateFlow<ThemeMode> = MutableStateFlow(loadTheme())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()



    init {
        updateAppVersion()

    }

    fun updateLocationPermission(hasLocationPermission: Boolean) {
        _uiState.update {
            it.copy(
                hasLocationPermission = hasLocationPermission
            )
        }
    }

    fun updateNotificationsEnabled(notificationsEnabled: Boolean) {
        _uiState.update {
            it.copy(
                notificationsEnabled = notificationsEnabled
            )
        }
    }

    fun updateAppVersion(){
        _uiState.update {
            it.copy(
                appVersion = application.getAppVersionInfo().versionName
            )
        }
    }

    fun showDeleteDialog() {
        _uiState.update {
            it.copy(
                showDeleteDialog = true
            )
        }
    }

    fun dismissDeleteDialog() {
        _uiState.update {
            it.copy(
                showDeleteDialog = false
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                googleSignInClient.signOut()
            }
        }
    }

    fun loadTheme(): ThemeMode {
        val theme =  prefs.getString("theme_mode", ThemeMode.SYSTEM.name)?.let {
            ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM



        return theme
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().apply {
            putString("theme_mode", mode.name)
            apply()
        }

        _themeMode.value = mode


        Log.i("SETTINGS", "theme ${_themeMode.value}")
    }
}

data class SettingsUiState(
    val hasLocationPermission: Boolean = false,
    val notificationsEnabled: Boolean = false,
    val appVersion: String = "",
    val showDeleteDialog: Boolean = false
)

