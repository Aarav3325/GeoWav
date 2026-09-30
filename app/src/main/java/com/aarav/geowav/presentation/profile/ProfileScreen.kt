@file:SuppressLint("InlinedApi")

package com.aarav.geowav.presentation.profile

import android.annotation.SuppressLint

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aarav.geowav.R
import com.aarav.geowav.data.model.User
import com.aarav.geowav.presentation.circle.ConnectionUsageCard
import com.aarav.geowav.presentation.components.AboutDialog
import com.aarav.geowav.presentation.components.ProfileCard
import com.aarav.geowav.presentation.components.openAppDetailsSettings
import com.aarav.geowav.presentation.locationsharing.itemShape
import com.aarav.geowav.presentation.paywall.CurrentPlanCard
import com.aarav.geowav.presentation.subscription.SubscriptionViewModel
import com.aarav.geowav.presentation.theme.manrope
import com.aarav.geowav.presentation.yourplace.PlacesUsageCard

enum class ThemeMode { SYSTEM, LIGHT, DARK }


@OptIn(ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun ProfileScreen(
    isDarkThemeEnabled: Boolean,
    profileVM: ProfileVM,
    subscriptionViewModel: SubscriptionViewModel,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    hasLocationPermission: Boolean,
    notificationsEnabled: Boolean,
    navigateToHome: () -> Unit,
    navigateToInsights: () -> Unit,
    navigateToAbout: () -> Unit,
    navigateToReleaseNotes: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit
) {

    val context = LocalContext.current

    val uiState by profileVM.uiState.collectAsState()
    val plan by subscriptionViewModel.userPlan.collectAsState()
    val subscriptionState by subscriptionViewModel.subscriptionState.collectAsState()

    LaunchedEffect(Unit) {
        subscriptionViewModel.fetchSubscriptionStatus()
    }


    var showPermissionEducation by remember {
        mutableStateOf(false)
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                profileVM.refreshPermissionState()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            Log.i("PROFILE", "user avatar : $it")
            profileVM.uploadAvatar(it)
        }
    }






    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Text(
                        text = "Profile",
                        fontSize = 20.sp,
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
                            painter = painterResource(R.drawable.back),
                            contentDescription = "back arrow",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) {

        when {
            uiState.currentUser == null -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ContainedLoadingIndicator()
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(it)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    ProfileCard(
                        isUploading = uiState.isUploading,
                        uploadingProgress = uiState.uploadProgress,
                        isDarkThemeEnabled = isDarkThemeEnabled,
                        plan = plan,
                        currentUser = uiState.currentUser,
                        userAvatar = uiState.userAvatar,
                        onAvatarClick = {
                            launcher.launch("image/*")
                        }
                    )

                    Section("Subscription") {
                        CurrentPlanCard(subscriptionState)
                    }

                    Section(title = "Usage Overview") {
                        ConnectionUsageCard(
                            current = uiState.lovedOnes.size,
                            plan = plan,
                            textSize = MaterialTheme.typography.bodyMedium.fontSize,
                            showPlanInfo = false,
                            isLoading = uiState.isLovedOnesLoading
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        PlacesUsageCard(
                            current = uiState.placesList.size,
                            plan = plan,
                            textSize = MaterialTheme.typography.bodyMedium.fontSize,
                            showPlanInfo = false,
                            isLoading = uiState.isPlacesLoading
                        )
                    }

                    Section(title = "Personal") {
                        SettingItemNew(
                            title = "Insights",
                            subtitle = "Reflect on your place awareness patterns",
                            index = 0,
                            count = 1,
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.activity),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            onClick = navigateToInsights
                        )
                    }

                    Section(title = "Trust & Permissions") {
                        SettingItemNew(
                            title = "Permission Education",
                            subtitle = if (uiState.permissionState.allCorePermissionsGranted) {
                                "Location, background access, and alerts are enabled"
                            } else {
                                "Review how GeoWav uses location, alerts, and background access"
                            },
                            index = 0,
                            count = 1,
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.lock),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            onClick = {
                                showPermissionEducation = true
                            }
                        )
                    }

                    Section(title = "Location") {
                        SettingItemNew(
                            title = "Location Access",
                            subtitle = if (uiState.permissionState.locationServicesReady) {
                                "Live and background location enabled"
                            } else {
                                "Manage live and background location access"
                            },
                            index = 0,
                            count = 2,
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.gps),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            onClick = {
                                openAppSettings(context, Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            }
                        )

                        TriggerTypeSelector(
                            enabled = uiState.hasLocationPermission && uiState.notificationsEnabled,
                            index = 1,
                            count = 2
                        )
                    }

                    Section(title = "Appearance") {
                        ThemeSelector(
                            index = 0,
                            count = 1,
                            selected = themeMode,
                            onSelected = onThemeChange
                        )
                    }

                    Section(title = "Notifications") {
                        SwitchItem(
                            title = "Enable Notifications",
                            subtitle = "Arrival, departure, and emergency alerts",
                            index = 0,
                            count = 1,
                            checked = uiState.notificationsEnabled,
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.bell),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            onCheckedChange = {
                                openAppSettings(context, Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            }
                        )
                    }

                    Section(title = "About") {
                        SettingItemNew(
                            title = "App Version",
                            subtitle = "Installed release build",
                            index = 0,
                            count = 4,
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.info),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Text(
                                        text = "v${uiState.appVersion}",
                                        fontFamily = manrope,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            },
                            enabled = true
                        )

                        SettingItemNew(
                            title = "What's New",
                            subtitle = "Check out the latest features and updates",
                            index = 1,
                            count = 4,
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.timeline),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            onClick = navigateToReleaseNotes
                        )

                        SettingItemNew(
                            title = "About GeoWav",
                            subtitle = "Mission, architecture, and creators",
                            index = 2,
                            count = 4,
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.info),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            onClick = navigateToAbout
                        )

                        SettingItemNew(
                            title = "Privacy Policy",
                            subtitle = "Learn how GeoWav protects your data",
                            index = 3,
                            count = 4,
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.vault),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.redirect),
                                    contentDescription = "Open in browser",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                val url = "https://aarav3325.github.io/geowav-privacy-policy/"
                                val customTabsIntent = CustomTabsIntent.Builder().build()
                                customTabsIntent.launchUrl(context, url.toUri())
                            }
                        )
                    }

                    Section(title = "Account") {
                        SettingItemNew(
                            title = "Logout",
                            subtitle = "Sign out of your GeoWav account",
                            index = 0,
                            count = 1,
                            titleColor = MaterialTheme.colorScheme.error,
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.link_break),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            onClick = {
                                profileVM.logout(onComplete = onLogout)
                            }
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                profileVM.dismissDeleteDialog()
            },
            title = { Text("Delete account?", fontFamily = manrope, fontWeight = FontWeight.Bold) },
            text = {
                Text("This action is permanent and cannot be undone.", fontFamily = manrope)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        profileVM.dismissDeleteDialog()
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
                    profileVM.dismissDeleteDialog()
                }) {
                    Text("Cancel", fontFamily = manrope, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    PermissionEducationDialog(
        showDialog = showPermissionEducation,
        onDismiss = { showPermissionEducation = false },
        onOpenAppSettings = {
            showPermissionEducation = false
            openAppDetailsSettings(context)
        },
        onOpenNotificationSettings = {
            showPermissionEducation = false
            openAppSettings(context, Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        }
    )
}

@Composable
fun Section(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title.uppercase(),
            fontFamily = manrope,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        Column {
            content()
        }
    }
}

fun openAppSettings(
    context: Context,
    appAction: String
) {
    val intent = Intent().apply {
        action = appAction
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        putExtra(Settings.EXTRA_CHANNEL_ID, context.applicationInfo.uid)
    }
    context.startActivity(intent)
}

@Composable
fun PermissionEducationDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit
) {
    if (!showDialog) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "How permissions support safety",
                fontFamily = manrope,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Location powers live movement updates only during active sharing, safety sessions, and place alerts.",
                    fontFamily = manrope,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Background access keeps those active sessions and place alerts working when GeoWav is not open.",
                    fontFamily = manrope,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Notifications let GeoWav tell you about invites, sharing changes, place alerts, and emergency activity.",
                    fontFamily = manrope,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "You stay in control. Android settings can change or remove access anytime.",
                    fontFamily = manrope,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onOpenAppSettings) {
                Text("App settings", fontFamily = manrope)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onOpenNotificationSettings) {
                    Text("Notification settings", fontFamily = manrope)
                }
                TextButton(onClick = onDismiss) {
                    Text("Close", fontFamily = manrope)
                }
            }
        }
    )
}

@Composable
fun SwitchItem(
    title: String,
    subtitle: String? = null,
    index: Int,
    count: Int,
    checked: Boolean,
    leadingIcon: (@Composable () -> Unit)? = null,
    onCheckedChange: (Boolean) -> Unit
) {
    val shape = itemShape(index, count)

    Surface(
        onClick = { onCheckedChange(!checked) },
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .padding(vertical = 1.5.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (leadingIcon != null) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        leadingIcon()
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontFamily = manrope,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = manrope,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )
        }
    }
}

@Composable
fun TriggerTypeSelector(
    enabled: Boolean,
    index: Int,
    count: Int
) {
    val shape = itemShape(index, count)

    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .padding(vertical = 1.5.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.bell),
                        contentDescription = null,
                        tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "Awareness Triggers",
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Automatic place notifications",
                        fontFamily = manrope,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Arrivals (Enter)", "Departures (Exit)").forEach { label ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (enabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (enabled) {
                                Icon(
                                    painter = painterResource(R.drawable.check),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = label,
                                fontFamily = manrope,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeSelector(
    index: Int,
    count: Int,
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit
) {
    val shape = itemShape(index, count)

    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .padding(vertical = 1.5.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.night),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "Appearance Mode",
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Customize your visual theme",
                        fontFamily = manrope,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeMode.entries.forEach { mode ->
                    val isSelected = selected == mode
                    val modeLabel = when (mode) {
                        ThemeMode.SYSTEM -> "System"
                        ThemeMode.LIGHT -> "Light"
                        ThemeMode.DARK -> "Dark"
                    }
                    val containerColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        label = "themeChipContainer"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "themeChipContent"
                    )

                    Surface(
                        onClick = { onSelected(mode) },
                        shape = RoundedCornerShape(12.dp),
                        color = containerColor,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            if (isSelected) {
                                Icon(
                                    painter = painterResource(R.drawable.check),
                                    contentDescription = null,
                                    tint = contentColor,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .padding(end = 4.dp)
                                )
                            }
                            Text(
                                text = modeLabel,
                                fontFamily = manrope,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = contentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingItemNew(
    title: String,
    subtitle: String? = null,
    enabled: Boolean = true,
    index: Int,
    count: Int,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val shape = itemShape(index, count)

    Surface(
        onClick = { if (enabled && onClick != null) onClick() },
        enabled = enabled && onClick != null,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .padding(vertical = 1.5.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (leadingIcon != null) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        leadingIcon()
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = if (enabled) titleColor else titleColor.copy(alpha = 0.5f),
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = manrope,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailingIcon()
            } else if (onClick != null && enabled) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(R.drawable.caret_right_fill),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
