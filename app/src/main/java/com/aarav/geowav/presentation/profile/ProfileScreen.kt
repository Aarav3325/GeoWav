package com.aarav.geowav.presentation.profile

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aarav.geowav.R
import com.aarav.geowav.data.model.User
import com.aarav.geowav.presentation.circle.ConnectionUsageCard
import com.aarav.geowav.presentation.components.AboutDialog
import com.aarav.geowav.presentation.components.ProfileCard
import com.aarav.geowav.presentation.components.TermsAndConditionsDialog
import com.aarav.geowav.presentation.locationsharing.itemShape
import com.aarav.geowav.presentation.paywall.CurrentPlanCard
import com.aarav.geowav.presentation.subscription.SubscriptionViewModel
import com.aarav.geowav.presentation.theme.manrope
import com.aarav.geowav.presentation.yourplace.PlacesUsageCard
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState


enum class ThemeMode { SYSTEM, LIGHT, DARK }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class,
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

    var showAboutDialog by remember {
        mutableStateOf(false)
    }
    var tc by remember {
        mutableStateOf(false)
    }

    val notificationPermission = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)

    val fineLocation = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val background = rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {

                val isGranted =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

                Log.i("NOTI", "granted: " + isGranted)
                profileVM.updateNotificationsEnabled(isGranted)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(fineLocation.status, background.status) {
        if (fineLocation.status.isGranted && background.status.isGranted) {
            profileVM.updateLocationPermission(true)
        } else {
            profileVM.updateLocationPermission(false)
        }
    }

    val isPermissionGranted =
        CheckBackgroundPermission() && CheckFineLocationPermission()

    LaunchedEffect(Unit) {
        profileVM.updateLocationPermission(isPermissionGranted)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            Log.i("PROFILE", "user avatar : $it")
            profileVM.uploadAvatar(it)
        }
    }

    TermsAndConditionsDialog(
        showDialog = tc,
        onAcceptClick = {
            tc = false
        }
    )

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
                        .padding(it)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    ProfileCard(
                        uiState.isUploading,
                        uiState.uploadProgress,
                        isDarkThemeEnabled,
                        plan,
                        uiState.currentUser,
                        uiState.userAvatar,
                        onAvatarClick = {
                            launcher.launch("image/**")
                        }
                    )

                    Section("Subscription") {
                        CurrentPlanCard(subscriptionState)
                    }

                    Section(title = "Usage Overview") {
                        ConnectionUsageCard(
                            uiState.lovedOnes.size,
                            plan,
                            false,
                            Modifier.padding(bottom = 8.dp)
                        )

                        PlacesUsageCard(
                            uiState.placesList.size,
                            plan,
                            false,
                            Modifier.padding(top = 8.dp)
                        )
                    }

                    Section(title = "Location") {
                        SettingItemNew(
                            title = "Location Access",
                            subtitle = "Manage location permission",
                            onClick = {
                                openAppSettings(context, Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            },
                            index = 0,
                            count = 2,
                        )

                        TriggerTypeSelector(
                            enabled = hasLocationPermission && notificationsEnabled,
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
                            index = 0,
                            count = 1,
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = {
                                openAppSettings(context, Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            }
                        )
                    }

                    Section(title = "About") {
                        SettingItemNew(
                            title = "App Version",
                            index = 0,
                            count = 3,
                            subtitle = uiState.appVersion,
                            enabled = true
                        )

                        SettingItemNew(
                            title = "About GeoWav",
                            index = 1,
                            count = 3,
                            onClick = {
                                showAboutDialog = true
                            }
                        )

                        SettingItemNew(
                            title = "Terms & Privacy Policy",
                            index = 2,
                            count = 3,
                            onClick = {
                                tc = true
                            }
                        )
                    }

                    Section(title = "Account") {
                        SettingItemNew(
                            title = "Logout",
                            index = 0,
                            count = 1,
                            onClick = {
                                profileVM.logout(onComplete = onLogout)
                            }
                        )

                        Spacer(modifier = Modifier.height(32.dp))
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
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp)
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
fun SwitchItem(
    title: String,
    index: Int,
    count: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    val shape = itemShape(index, count)

    Row(
        modifier = Modifier
            .padding(vertical = 1.5.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(horizontal = 16.dp, vertical = 8.dp),
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
}


@Composable
fun TriggerTypeSelector(
    enabled: Boolean,
    index: Int,
    count: Int
) {
    val shape = itemShape(index, count)

    Row(
        modifier = Modifier
            .padding(vertical = 1.5.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer),
        verticalAlignment = Alignment.CenterVertically
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
}

@Composable
fun ThemeSelector(
    index: Int,
    count: Int,
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit
) {
    val shape = itemShape(index, count)

    Row(
        modifier = Modifier
            .padding(vertical = 1.5.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer),
        verticalAlignment = Alignment.CenterVertically
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
                    painter = painterResource(R.drawable.check),
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else null,
        label = {
            Text(
                text = label,
                fontFamily = manrope,
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
                    painter = painterResource(R.drawable.check),
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else null,
        label = {
            Text(
                text = label,
                fontFamily = manrope,
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

@Composable
fun SettingItemNew(
    title: String,
    subtitle: String? = null,
    enabled: Boolean = true,
    index: Int,
    count: Int,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null
) {
    val shape = itemShape(index, count)

    Row(
        modifier = Modifier
            .padding(vertical = 1.5.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer),
        verticalAlignment = Alignment.CenterVertically
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
                    fontFamily = manrope,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}