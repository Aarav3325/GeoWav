package com.aarav.geowav.presentation.settings

import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


enum class TriggerType { ENTER, EXIT }
enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Composable
fun SettingsScreen(
    hasLocationPermission: Boolean,
    notificationsEnabled: Boolean,
    triggerType: TriggerType,
    themeMode: ThemeMode,
    appVersion: String,
    onOpenLocationSettings: () -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onTriggerTypeChange: (TriggerType) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onAboutClick: () -> Unit,
    onTermsClick: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // 🧭 Location
        item {
            Section(title = "Location") {
                SettingItem(
                    title = "Location Access",
                    subtitle = "Manage location permission",
                    onClick = onOpenLocationSettings
                )

                TriggerTypeSelector(
                    enabled = hasLocationPermission && notificationsEnabled,
                    selected = triggerType,
                    onSelected = onTriggerTypeChange
                )
            }
        }

        // 🎨 Appearance
        item {
            Section(title = "Appearance") {
                ThemeSelector(
                    selected = themeMode,
                    onSelected = onThemeChange
                )
            }
        }

        // 🔔 Notifications
        item {
            Section(title = "Notifications") {
                SwitchItem(
                    title = "Enable Notifications",
                    checked = notificationsEnabled,
                    onCheckedChange = onToggleNotifications
                )
            }
        }

        // ℹ️ About
        item {
            Section(title = "About") {
                SettingItem(
                    title = "App Version",
                    subtitle = appVersion,
                    enabled = false
                )
                SettingItem(
                    title = "About GeoWav",
                    onClick = onAboutClick
                )
                SettingItem(
                    title = "Terms & Privacy Policy",
                    onClick = onTermsClick
                )
            }
        }

        // 👤 Account
        item {
            Section(title = "Account") {
                SettingItem(
                    title = "Logout",
                    onClick = onLogout
                )

                SettingItem(
                    title = "Delete Account",
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = { showDeleteDialog = true }
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete account?") },
            text = {
                Text("This action is permanent and cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteAccount()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun Section(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    subtitle: String? = null,
    enabled: Boolean = true,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .let {
                if (onClick != null && enabled)
                    it.clickable { onClick() }
                else it
            }
            .padding(vertical = 10.dp)
    ) {
        Text(title, color = titleColor)
        subtitle?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}


@Composable
fun TriggerTypeSelector(
    enabled: Boolean,
    selected: TriggerType,
    onSelected: (TriggerType) -> Unit
) {
    Column {
        Text(
            "Trigger Type",
            color = if (enabled)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RadioOption("Enter", selected == TriggerType.ENTER, enabled) {
                onSelected(TriggerType.ENTER)
            }
            RadioOption("Exit", selected == TriggerType.EXIT, enabled) {
                onSelected(TriggerType.EXIT)
            }
        }
    }
}

@Composable
fun ThemeSelector(
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit
) {
    Column {
        Text("Dark Mode")
        ThemeMode.values().forEach {
            RadioOption(
                text = it.name.lowercase().replaceFirstChar(Char::uppercase),
                selected = selected == it,
                enabled = true
            ) { onSelected(it) }
        }
    }
}

@Composable
fun RadioOption(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled) { onClick() }
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            enabled = enabled
        )
        Text(text)
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {

    MaterialTheme {

        val context = LocalContext.current
        val packageManager = context.packageManager
        val packageName = context.packageName

        val packageInfo = if (Build.VERSION.SDK_INT >= 33) {
            packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0)
        }

        val versionName = packageInfo.versionName ?: "0.0.0"


        val parts = versionName.split(".")
        val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0

        SettingsScreen(
            hasLocationPermission = true,
            notificationsEnabled = true,
            triggerType = TriggerType.ENTER,
            themeMode = ThemeMode.SYSTEM,
            appVersion = versionName,
            onOpenLocationSettings = {},
            onToggleNotifications = {},
            onTriggerTypeChange = {},
            onThemeChange = {},
            onAboutClick = {},
            onTermsClick = {},
            onLogout = {},
            onDeleteAccount = {}
        )
    }
}
