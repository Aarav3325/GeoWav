package com.aarav.geowav.presentation.profile

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.core.utils.Resource
import com.aarav.geowav.core.utils.UploadResult
import com.aarav.geowav.core.permissions.GeoPermissionCoordinator
import com.aarav.geowav.core.permissions.GeoPermissionUiState
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.data.model.User
import com.aarav.geowav.data.model.UserSubscription
import com.aarav.geowav.domain.repository.CircleRepository
import com.aarav.geowav.domain.repository.PlaceRepository
import com.aarav.geowav.domain.repository.SubscriptionRepository
import com.aarav.geowav.platform.GeofenceForegroundService
import com.aarav.geowav.platform.LiveLocationService
import com.aarav.geowav.platform.NotificationService
import com.aarav.geowav.platform.getAppVersionInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.net.toUri
import kotlinx.coroutines.flow.distinctUntilChanged

@HiltViewModel
class ProfileVM @Inject constructor(
    private val prefs: SharedPreferences,
    application: Application,
    private val subscriptionRepository: SubscriptionRepository,
    private val circleRepository: CircleRepository,
    private val placeRepository: PlaceRepository,
    private val googleSignInClient: GoogleSignInClient,
    private val permissionCoordinator: GeoPermissionCoordinator
) : AndroidViewModel(application) {

    private val _uiState: MutableStateFlow<SettingsUiState> = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _themeMode: MutableStateFlow<ThemeMode> = MutableStateFlow(loadTheme())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _subscriptionState = MutableStateFlow<UserSubscription?>(null)
    val subscriptionState: StateFlow<UserSubscription?> = _subscriptionState.asStateFlow()

    private val currentUserId: String
        get() = googleSignInClient.getUserId()

    init {
        fetchUser()
        loadLovedOnes()
        getPlaces()
        updateAppVersion()
        observePermissionState()
    }

    fun loadLovedOnes() {

        if (currentUserId.isEmpty()) return

        viewModelScope.launch {
            when (val result =
                circleRepository.getAcceptedLovedOnes(currentUserId)
            ) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            lovedOnes = result.data ?: emptyList()
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            lovedOnesError = result.message
                        )
                    }
                }

                else -> {}
            }
        }
    }

    fun getPlaces() {
        viewModelScope.launch {
            placeRepository.getPlaces()
                .distinctUntilChanged()
                .collectLatest { list ->
                    _uiState.update {
                        it.copy(
                            placesList = list
                        )
                    }
                }
        }
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

    fun refreshPermissionState() {
        val permissionState = permissionCoordinator.refresh()
        _uiState.update {
            it.copy(
                permissionState = permissionState,
                hasLocationPermission = permissionState.locationServicesReady,
                notificationsEnabled = permissionState.notificationsGranted
            )
        }
    }

    private fun observePermissionState() {
        refreshPermissionState()
        viewModelScope.launch {
            permissionCoordinator.state.collectLatest { permissionState ->
                _uiState.update {
                    it.copy(
                        permissionState = permissionState,
                        hasLocationPermission = permissionState.locationServicesReady,
                        notificationsEnabled = permissionState.notificationsGranted
                    )
                }
            }
        }
    }

    fun fetchUser() {
        viewModelScope.launch {
            val user = googleSignInClient.fetchCurrentUser()
//            val uri = googleSignInClient.getUserProfile()?.toString()
            _uiState.update {
                it.copy(
                    currentUser = user,
//                    userAvatar = uri
                )
            }
        }
    }

    fun updateAppVersion() {
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

    fun uploadAvatar(uri: Uri) {
        viewModelScope.launch {
            googleSignInClient.uploadUserAvatar(uri)
                .collect {
                    result ->
                    when(result) {
                        is UploadResult.Progress -> {
                            _uiState.update {
                                it.copy(
                                    isUploading = true,
                                    uploadProgress = result.progress
                                )
                            }
                        }

                        is UploadResult.Success -> {
                            _uiState.update {
                                it.copy(
                                    isUploading = false,
                                    userAvatar = result.downloadUrl.toUri().toString(),
                                )
                            }
                        }

                        is UploadResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    isUploading = false
                                )
                            }
                        }
                    }
                }
        }
    }

    fun logout(onComplete: () -> Unit = {}) {
        val context = getApplication<Application>()

        // Stop all foreground services before signing out
        context.stopService(Intent(context, LiveLocationService::class.java))
        context.stopService(Intent(context, GeofenceForegroundService::class.java))
        context.stopService(Intent(context, NotificationService::class.java))

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                googleSignInClient.signOut()
            }
            // Navigate only after sign-out is complete
            onComplete()
        }
    }

    fun loadTheme(): ThemeMode {
        val theme = prefs.getString("theme_mode", ThemeMode.SYSTEM.name)?.let {
            ThemeMode.valueOf(it)
        } ?: ThemeMode.SYSTEM

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
    val currentUser: User? = null,
    val userAvatar: String? = null,
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val hasLocationPermission: Boolean = false,
    val notificationsEnabled: Boolean = false,
    val appVersion: String = "",
    val lovedOnesError: String? = null,
    val placesError: String? = null,
    val lovedOnes: List<CircleMember> = emptyList(),
    val placesList: List<Place> = emptyList(),
    val showDeleteDialog: Boolean = false,
    val permissionState: GeoPermissionUiState = GeoPermissionUiState()
)

