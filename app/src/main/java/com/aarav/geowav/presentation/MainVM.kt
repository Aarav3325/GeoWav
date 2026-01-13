package com.aarav.geowav.presentation

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.aarav.geowav.presentation.settings.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class MainVM @Inject constructor(
    private val prefs: SharedPreferences
) : ViewModel() {

    private val _themeMode = MutableStateFlow(loadTheme())
    val themeMode = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _themeMode.value = mode
    }

    fun loadTheme(): ThemeMode =
        prefs.getString("theme_mode", ThemeMode.SYSTEM.name)
            ?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM
}
