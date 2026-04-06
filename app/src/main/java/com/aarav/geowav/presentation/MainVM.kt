package com.aarav.geowav.presentation

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import com.aarav.geowav.presentation.profile.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.model.User
import com.aarav.geowav.presentation.components.SnackbarManager
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MainVM @Inject constructor(
    private val prefs: SharedPreferences,
    private val googleSignInClient: GoogleSignInClient
) : ViewModel() {

    private val _themeMode = MutableStateFlow(loadTheme())
    val themeMode = _themeMode.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

//
//    init {
//        if(currentUser != null) {
//            fetchUser()
//        }
//    }

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

    fun fetchUser() {
        viewModelScope.launch {
            val user = googleSignInClient.fetchCurrentUser()
            _currentUser.value = user
        }
    }
}
