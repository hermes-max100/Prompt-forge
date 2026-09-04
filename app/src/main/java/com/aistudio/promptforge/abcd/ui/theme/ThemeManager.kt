package com.aistudio.promptforge.abcd.ui.theme

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadInitialTheme())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _glowEffectsEnabled = MutableStateFlow(prefs.getBoolean(KEY_GLOW_ENABLED, true))
    val glowEffectsEnabled: StateFlow<Boolean> = _glowEffectsEnabled.asStateFlow()

    private fun loadInitialTheme(): AppThemeMode {
        val savedName = prefs.getString(KEY_THEME_MODE, null)
        return if (savedName != null) {
            try {
                AppThemeMode.valueOf(savedName)
            } catch (_: Exception) {
                AppThemeMode.DARK
            }
        } else {
            // Default to AutoFlow Dark ("AI control room") with system option available
            AppThemeMode.DARK
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    fun setGlowEffectsEnabled(enabled: Boolean) {
        _glowEffectsEnabled.value = enabled
        prefs.edit().putBoolean(KEY_GLOW_ENABLED, enabled).apply()
    }

    fun resetToSystemDefault() {
        setThemeMode(AppThemeMode.SYSTEM)
    }

    companion object {
        private const val PREFS_NAME = "autoflow_theme_preferences"
        private const val KEY_THEME_MODE = "app_theme_mode"
        private const val KEY_GLOW_ENABLED = "app_glow_enabled"

        fun resolveTokens(mode: AppThemeMode, isSystemDark: Boolean, allowGlow: Boolean = true): AppThemeTokens {
            val baseTokens = when (mode) {
                AppThemeMode.SYSTEM -> if (isSystemDark) DarkThemeTokens else LightThemeTokens
                AppThemeMode.DARK -> DarkThemeTokens
                AppThemeMode.LIGHT -> LightThemeTokens
                AppThemeMode.NEON -> NeonThemeTokens
                AppThemeMode.CYBERPUNK -> CyberpunkThemeTokens
            }
            return if (!allowGlow && baseTokens.isGlowEnabled) {
                baseTokens.copy(isGlowEnabled = false)
            } else {
                baseTokens
            }
        }
    }
}
