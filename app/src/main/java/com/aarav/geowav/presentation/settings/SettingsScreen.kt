package com.aarav.geowav.presentation.settings

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import com.aarav.geowav.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.presentation.components.AboutDialog
import com.aarav.geowav.presentation.theme.manrope
import com.aarav.geowav.presentation.theme.sora
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState


enum class TriggerType { ENTER, EXIT }
enum class ThemeMode { SYSTEM, LIGHT, DARK }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsVM: SettingsVM,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    hasLocationPermission: Boolean,
    notificationsEnabled: Boolean,
    triggerType: TriggerType,
    appVersion: String,
    navigateToHome: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onTriggerTypeChange: (TriggerType) -> Unit,
    onAboutClick: () -> Unit,
    onTermsClick: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit
) {

    val uiState by settingsVM.uiState.collectAsState()

    var showAboutDialog by remember {
        mutableStateOf(false)
    }

    val isPermissionGranted =
        CheckBackgroundPermission() && CheckFineLocationPermission()

    LaunchedEffect(Unit) {
        settingsVM.updateLocationPermission(isPermissionGranted)
    }

    AboutDialog(
        showAboutDialog = showAboutDialog,
        confirmButtonText = "Close",
        onConfirmClick = {
            showAboutDialog = false
        },
        icon = R.drawable.new_logo,
        title = "GeoWav",
        message = "GeoWav is a mobile application that helps users stay connected with their loved ones by sharing meaningful updates in a simple and reliable way. The app focuses on personal communication, supports offline usage, and securely synchronizes data using cloud services, providing a smooth and dependable user experience.",
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontSize = 24.sp,
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navigateToHome()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "back arrow",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                ,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            item {
                Section(title = "Location") {
                    SettingItem(
                        title = "Location Access",
                        subtitle = "Manage location permission",
                        onClick = onOpenLocationSettings
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    TriggerTypeSelector(
                        enabled = hasLocationPermission && notificationsEnabled,
                        selected = triggerType,
                        onSelected = onTriggerTypeChange
                    )
                }
            }

            item {
                Section(title = "Appearance") {
                    ThemeSelector(
                        selected = themeMode,
                        onSelected = onThemeChange
                    )
                }
            }

            item {
                Section(title = "Notifications") {
                    SwitchItem(
                        title = "Enable Notifications",
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = {
                            settingsVM.updateNotificationsEnabled(it)
                        }
                    )
                }
            }

            item {
                Section(title = "About") {
                    SettingItem(
                        title = "App Version",
                        subtitle = uiState.appVersion,
                        enabled = true
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    SettingItem(
                        title = "About GeoWav",
                        onClick = {
                            showAboutDialog = true
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    SettingItem(
                        title = "Terms & Privacy Policy",
                        onClick = onTermsClick
                    )
                }
            }

            item {
                Section(title = "Account") {
                    SettingItem(
                        title = "Logout",
                        onClick = {
                            settingsVM.logout()
                            onLogout()
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    SettingItem(
                        title = "Delete Account",
                        titleColor = MaterialTheme.colorScheme.error,
                        onClick = {
                            settingsVM.showDeleteDialog()
                        }
                    )
                }
            }
        }
    }



    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                settingsVM.dismissDeleteDialog()
            },
            title = { Text("Delete account?", fontFamily = manrope, fontWeight = FontWeight.Bold) },
            text = {
                Text("This action is permanent and cannot be undone.", fontFamily = sora)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        settingsVM.dismissDeleteDialog()
                        onDeleteAccount()
                    }
                ) {
                    Text(
                        "Delete",
                        color = MaterialTheme.colorScheme.error,
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    settingsVM.dismissDeleteDialog()
                }) {
                    Text("Cancel", fontFamily = manrope, fontWeight = FontWeight.SemiBold)
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
            fontFamily = manrope,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column {
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
            .then(
                if (onClick != null && enabled) Modifier.clickable { onClick() } else Modifier
            )
            .padding(vertical = 16.dp, horizontal = 16.dp)
    ) {
        Text(
            text = title,
            color = if (enabled) titleColor else titleColor.copy(alpha = 0.5f),
            fontFamily = manrope,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyLarge
        )
        subtitle?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = sora,
                modifier = Modifier.padding(top = 4.dp)
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontFamily = manrope,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}


@Composable
fun TriggerTypeSelector(
    enabled: Boolean,
    selected: TriggerType,
    onSelected: (TriggerType) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Trigger Type",
            fontFamily = manrope,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        TriggerTypeChipRow(
            enabled = enabled
        )
    }
}

@Composable
fun ThemeSelector(
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Appearance Mode",
            fontFamily = manrope,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeMode.entries.forEach { mode ->
                ThemeChips(
                    label = mode.name.lowercase().replaceFirstChar(Char::uppercase),
                    selected = selected == mode,
                    onClick = { onSelected(mode) }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
//        SettingsScreen(
//            hasLocationPermission = true,
//            notificationsEnabled = true,
//            triggerType = TriggerType.ENTER,
//            themeMode = ThemeMode.SYSTEM,
//            appVersion = "1.0.0",
//            onOpenLocationSettings = {},
//            onToggleNotifications = {},
//            onTriggerTypeChange = {},
//            onThemeChange = {},
//            onAboutClick = {},
//            onTermsClick = {},
//            onLogout = {},
//            onDeleteAccount = {}
//        )
}

@Composable
fun TriggerTypeChips(
    label: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else null,
        label = {
            Text(
                text = label,
                fontFamily = sora,
                fontWeight = FontWeight.Medium
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
fun TriggerTypeChipRow(
    enabled: Boolean
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TriggerTypeChips(
            label = "Enter",
            selected = true,
            enabled = enabled,
            onClick = { }
        )

        TriggerTypeChips(
            label = "Exit",
            selected = true,
            enabled = enabled,
            onClick = { }
        )
    }
}


@Composable
fun ThemeChips(
    label: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else null,
        label = {
            Text(
                text = label,
                fontFamily = sora,
                fontWeight = FontWeight.Medium
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CheckFineLocationPermission(): Boolean {
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    return permissionState.status.isGranted
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun CheckBackgroundPermission(): Boolean {
    val permissionState =
        rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    return permissionState.status.isGranted
}