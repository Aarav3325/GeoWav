package com.aarav.geowav.presentation

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.model.User
import com.aarav.geowav.domain.repository.PaymentRepository
import com.aarav.geowav.domain.repository.PlaceRepository
import com.aarav.geowav.presentation.components.SnackbarManager
import com.aarav.geowav.presentation.profile.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainVM @Inject constructor(
    private val prefs: SharedPreferences,
    private val placeRepository: PlaceRepository,
    private val googleSignInClient: GoogleSignInClient,
    private val paymentRepository: PaymentRepository,

    ) : ViewModel() {

    private val _themeMode = MutableStateFlow(loadTheme())
    val themeMode = _themeMode.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private var restored = false
    private var isSyncStarted = false

//
//    init {
//        if(currentUser != null) {
//            fetchUser()
//        }
//    }

    init {
        viewModelScope.launch {
            googleSignInClient.getUserIdFlow()
                .distinctUntilChanged()
                .collectLatest { uid ->
                    if (uid.isNotEmpty()) {
                        paymentRepository.initializeUser(uid)
                    }
//                else {
//                    paymentRepository.clear()
//                }
                }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit { putString("theme_mode", mode.name) }
        _themeMode.value = mode
    }

    fun loadTheme(): ThemeMode =
        prefs.getString("theme_mode", ThemeMode.SYSTEM.name)
            ?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM

    fun showMessage() {
        viewModelScope.launch {
            SnackbarManager.showMessage("Welcome ${currentUser.value?.username}")
        }
    }

    fun initializeUserSession() {
        viewModelScope.launch {

            launch {
                startPlaceSync()
            }

            launch {
                fetchUser()
            }

        }
    }

    fun fetchUser() {
        viewModelScope.launch {
            val user = googleSignInClient.fetchCurrentUser()
            _currentUser.value = user
        }
    }

    fun clearCurrentUser() {
        _currentUser.value = null
    }

    fun startPlaceSync() {

        if (isSyncStarted) return

        isSyncStarted = true

        viewModelScope.launch {

            placeRepository.migratePlacesIfNeeded()

            placeRepository.startRealtimeSync(viewModelScope)

        }
    }
    fun stopPlaceSync() {
        placeRepository.stopRealtimeSync()

        isSyncStarted = false
    }
}
