package com.aarav.geowav.presentation.locationsharing

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.core.utils.LiveLocationState
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.data.model.UpgradeContext
import com.aarav.geowav.data.model.UpgradeReason
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.presentation.components.CustomBottomSheet
import com.aarav.geowav.presentation.components.EmergencyShareDialog
import com.aarav.geowav.presentation.components.IdentityAvatar
import com.aarav.geowav.presentation.components.PermissionRequiredContent
import com.aarav.geowav.presentation.components.SnackbarManager
import com.aarav.geowav.presentation.components.UpgradeBottomSheetContent
import com.aarav.geowav.presentation.components.openAppDetailsSettings
import com.aarav.geowav.presentation.subscription.SubscriptionViewModel
import com.aarav.geowav.presentation.theme.GeoWavTheme
import com.aarav.geowav.presentation.theme.manrope
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSharingScreen(
    isDarkThemeEnabled: Boolean,
    viewModel: LocationSharingVM,
    navigateToPaywall: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToJourney: () -> Unit = {},
    subscriptionVM: SubscriptionViewModel,
    location: Pair<Double, Double>?,
    locationServicesReady: Boolean
) {

    val uiState by viewModel.uiState.collectAsState()

    val plan by subscriptionVM.userPlan.collectAsState()

    val cameraPositionState = rememberCameraPositionState()

    location?.let { (lat, lng) ->
        LaunchedEffect(lat, lng) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(lat, lng), 16f)
        }
    }



    LaunchedEffect(Unit) {
        viewModel.loadLovedOnes()
        viewModel.loadLocationPermission()

    }

    var upgradeContext by remember { mutableStateOf<UpgradeContext?>(null) }


    upgradeContext?.let {
        CustomBottomSheet(
            onDismissRequest = {
                upgradeContext = null
            }
        ) {
            UpgradeBottomSheetContent(
                context = it,
                onUpgradeClick = {
                    upgradeContext = null
                    navigateToPaywall()
                },
                onDismiss = { upgradeContext = null }
            )
        }
    }


//    LaunchedEffect(uiState.sharingState) {
//        viewModel.refreshState()
//    }


    LaunchedEffect(Unit) {
        viewModel.observeSessionLimit()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LiveLocationUiEvent.ShowError -> {
                    SnackbarManager.showMessage(event.message)
                }

                is LiveLocationUiEvent.SessionLimitReached -> {
                    upgradeContext =
                        UpgradeContext(
                            upgradeTo = UserPlan.PREMIUM,
                            reason = UpgradeReason.SessionLimitReached
                        )
                }
            }
        }
    }
    Scaffold(
        contentWindowInsets = WindowInsets(left = 0, top = 0, right = 0, bottom = 0),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (!locationServicesReady) {
            val context = LocalContext.current
            PermissionRequiredContent(
                title = "Location setup is needed",
                message = "Live sharing and emergency sharing need live and background location access so updates can continue reliably.",
                primaryActionText = "Review setup",
                onPrimaryAction = { openAppDetailsSettings(context) },
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }

        LocationSharingContent(
            modifier = Modifier.padding(padding),
            isDarkThemeEnabled = isDarkThemeEnabled,
            locationUiState = uiState,
            userPlan = plan,
            cameraPosition = cameraPositionState,
            onToggleChange = viewModel::onViewerToggle,
            onStartSharing = viewModel::startLiveLocationSharing,
            onStopSharing = viewModel::stopLiveLocationSharing,
            onStartEmergency = viewModel::startEmergency,
            onStopEmergency = viewModel::stopEmergency,
            navigateToJourney = navigateToJourney
        )

    }
}

@Composable
fun LocationSharingContent(
    modifier: Modifier = Modifier,
    isDarkThemeEnabled: Boolean,
    locationUiState: LiveLocationUiState,
    userPlan: UserPlan,
    cameraPosition: CameraPositionState,
    onToggleChange: (String, Boolean) -> Unit,
    onStartSharing: (UserPlan) -> Unit,
    onStopSharing: () -> Unit,
    onStartEmergency: (Int) -> Unit,
    onStopEmergency: () -> Unit,
    navigateToJourney: () -> Unit = {}
) {

    var showCountdownSheet by remember { mutableStateOf(false) }
    var showSetupContactsDialog by remember { mutableStateOf(false) }

    val isEmergencyActive = locationUiState.emergencyEndsAt != null ||
            locationUiState.sharingState is LiveLocationState.EmergencySharing
    val selectedViewerCount = locationUiState.selectedViewerIds.size

    val lazyState = rememberLazyListState()
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(expanded) {
        if (expanded) {
            lazyState.animateScrollToItem(1)
        }
    }

    // SOS Countdown Bottom Sheet
    EmergencyCountdownSheet(
        isVisible = showCountdownSheet,
        onConfirmEmergency = {
            onStartEmergency(15)
            showCountdownSheet = false
        },
        onCancel = { showCountdownSheet = false }
    )

    // Emergency Setup Dialog (if no contacts configured)
    EmergencySetupDialog(
        isVisible = showSetupContactsDialog,
        onDismiss = { showSetupContactsDialog = false },
        onOpenSetup = {
            expanded = true
        }
    )

    Column(
        modifier = modifier.fillMaxSize()
            .navigationBarsPadding()
    ) {
        // Pinned Top Bar with Live Location title and SOS Pill
        LiveLocationTopBar(
            isEmergencyActive = isEmergencyActive,
            onSosClick = {
                if (isEmergencyActive) {
                    onStopEmergency()
                } else if (locationUiState.lovedOnes.isEmpty()) {
                    showSetupContactsDialog = true
                } else {
                    showCountdownSheet = true
                }
            }
        )

        // Pinned Emergency Active Banner (when emergency is active)
        AnimatedVisibility(visible = isEmergencyActive) {
            EmergencyActiveBanner(
                remainingText = locationUiState.remaining,
                onStopEmergency = onStopEmergency
            )
        }

        LazyColumn(
            state = lazyState,
            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            item {
                StatusCard(
                    userPlan,
                    locationUiState.remaining,
                    locationUiState.sharingState,
                    isEmergencyActive,
                    selectedViewerCount,
                    onStartSharing,
                    onStopSharing,
                    onStopEmergency
                )
            }

            item {
                JourneyModeCard(
                    isDark = isDarkThemeEnabled,
                    activeSession = locationUiState.activeSession,
                    savedPlaces = locationUiState.savedPlaces,
                    onStartJourney = navigateToJourney,
                    onStopSharing = onStopSharing
                )
            }

            item {
                LovedOnesCard(
                    expanded,
                    onExpandChange = {
                        expanded = !expanded
                    },
                    locationUiState.lovedOnes,
                    locationUiState.sharingState,
                    locationUiState.selectedViewerIds,
                    locationUiState.updatingViewerId,
                    onToggleChange
                )
            }

            item {
                MapPreviewCard(cameraPosition, locationUiState.sharingState)
            }
        }
    }


}

//@Preview(showBackground = true)
//@Composable
//fun PreviewLocationContent() {
//
//    val locationUiState = LiveLocationUiState(
//        sharingState = LiveLocationState.NotSharing
////        sharingState = LiveLocationState.Sharing(
////            visibleCount = 1,
////            lastUpdatedText = "1s ago"
////        )
////        sharingState = LiveLocationState.EmergencySharing(
////            remainingTime = "12:00"
////        )
//    )
//
//    GeoWavTheme {
//        LocationSharingContent(locationUiState)
//    }
//
////        LocationSharingContent(locationUiState)
//}

@Composable
fun StatusCard(
    userPlan: UserPlan,
    remainingTime: String? = null,
    liveLocationState: LiveLocationState,
    isEmergencyActive: Boolean,
    selectedViewerCount: Int,
    onStart: (UserPlan) -> Unit,
    onStop: () -> Unit,
    onEmergencyStop: () -> Unit,
) {
    val containerColor = when (liveLocationState) {
        LiveLocationState.NotSharing ->
            MaterialTheme.colorScheme.surfaceContainer

        LiveLocationState.Starting,
        is LiveLocationState.Sharing ->
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)

        is LiveLocationState.EmergencySharing ->
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)

        is LiveLocationState.Error ->
            MaterialTheme.colorScheme.surfaceContainer
    }

    val selectedAudienceText = when (selectedViewerCount) {
        0 -> "No audience selected"
        1 -> "Ready for 1 person"
        else -> "Ready for $selectedViewerCount people"
    }

    val (title, subtitle, statusColor, statusLabel) = when (liveLocationState) {
        LiveLocationState.NotSharing -> {
            Quad(
                "Not Sharing",
                selectedAudienceText,
                MaterialTheme.colorScheme.outline,
                "Inactive"
            )
        }

        LiveLocationState.Starting -> {
            Quad(
                "Starting Live Sharing",
                "Connecting to circle…",
                MaterialTheme.colorScheme.primary,
                "Starting"
            )
        }

        is LiveLocationState.Sharing -> {
            Quad(
                "Live Sharing Active",
                if (liveLocationState.visibleCount > 1) "Visible to ${liveLocationState.visibleCount} people" else "Visible to 1 person",
                Color(0xFF2E7D32),
                "Live"
            )
        }

        is LiveLocationState.Error -> {
            Quad(
                "Sharing Stopped",
                liveLocationState.message,
                MaterialTheme.colorScheme.error,
                "Error"
            )
        }

        is LiveLocationState.EmergencySharing ->
            Quad(
                "Emergency SOS Active",
                "Broadcasting live coordinates to circle",
                MaterialTheme.colorScheme.error,
                "SOS Active"
            )
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = BorderStroke(
            1.dp,
            if (isEmergencyActive) MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(animationSpec = tween(250, easing = FastOutSlowInEasing))
        ) {
            // Main Status Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (isEmergencyActive) MaterialTheme.colorScheme.error else statusColor.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(if (isEmergencyActive) R.drawable.emergency else R.drawable.location_sharing),
                            contentDescription = null,
                            tint = if (isEmergencyActive) MaterialTheme.colorScheme.onError else statusColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = manrope,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = manrope,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Status pill badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusColor.copy(alpha = 0.14f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(statusColor, CircleShape)
                        )
                        Text(
                            text = statusLabel,
                            fontFamily = manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = statusColor
                        )
                    }
                }
            }

            // Emergency Active Countdown Callout
            AnimatedVisibility(visible = isEmergencyActive) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Emergency broadcast ends in",
                            fontFamily = manrope,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = remainingTime ?: "00:00",
                            fontFamily = manrope,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button
            when (liveLocationState) {
                is LiveLocationState.Sharing -> {
                    StopSharingButton(onStop)
                }

                LiveLocationState.NotSharing -> {
                    StartSharingButton(
                        userPlan = userPlan,
                        enabled = selectedViewerCount > 0,
                        selectedViewerCount = selectedViewerCount,
                        onClick = onStart
                    )
                }

                is LiveLocationState.EmergencySharing -> {
                    StopEmergencyButton(onEmergencyStop)
                }

                else -> {}
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MapPreviewCard(
    cameraPosition: CameraPositionState,
    liveLocationState: LiveLocationState
) {
    var mapLoad by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Map View",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = manrope
                    ),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when (liveLocationState) {
                        LiveLocationState.NotSharing -> MaterialTheme.colorScheme.surfaceContainerHighest
                        is LiveLocationState.EmergencySharing -> MaterialTheme.colorScheme.errorContainer
                        is LiveLocationState.Sharing -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.surfaceContainerHighest
                    }
                ) {
                    when (liveLocationState) {
                        LiveLocationState.NotSharing -> MapStatusText(
                            text = "Preview only",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        is LiveLocationState.Sharing -> LastUpdatedText(liveLocationState.lastUpdatedText)
                        is LiveLocationState.EmergencySharing -> MapStatusText(
                            text = "Emergency active",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        LiveLocationState.Starting -> MapStatusText(
                            text = "Starting…",
                            color = MaterialTheme.colorScheme.primary
                        )
                        is LiveLocationState.Error -> MapStatusText(
                            text = "Not sharing",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Map View Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                var uiSettings by remember {
                    mutableStateOf(
                        MapUiSettings(
                            myLocationButtonEnabled = true,
                            zoomControlsEnabled = false,
                            compassEnabled = true,
                            mapToolbarEnabled = false
                        )
                    )
                }

                var mapProperties by remember {
                    mutableStateOf(MapProperties(isMyLocationEnabled = true))
                }

                if (!mapLoad) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ContainedLoadingIndicator()
                    }
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPosition,
                    uiSettings = uiSettings,
                    properties = mapProperties,
                    onMapLoaded = {
                        mapLoad = true
                    }
                )
            }
        }
    }
}

@Composable
private fun MapStatusText(
    text: String,
    color: Color
) {
    Text(
        text = text,
        fontFamily = manrope,
        fontWeight = FontWeight.SemiBold,
        color = color,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 10.dp)
    )
}

@Composable
fun LovedOnesCard(
    expanded: Boolean,
    onExpandChange: () -> Unit,
    lovedOnesList: List<CircleMember>,
    locationState: LiveLocationState,
    selectedViewerIds: Set<String>,
    updatingViewerId: String? = null,
    onToggleChange: (String, Boolean) -> Unit
) {
    val toggleEnabled = locationState is LiveLocationState.NotSharing
    val selectedViewerCount = lovedOnesList.count { it.id in selectedViewerIds }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "chevronRotation"
    )

    val notSharingHelperText = when {
        lovedOnesList.isEmpty() -> "Add people to your circle before starting live sharing."
        selectedViewerCount == 0 -> "Choose who can see your live location before you start sharing."
        else -> "When you start sharing, these people will be able to see your live location."
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(animationSpec = tween(250, easing = FastOutSlowInEasing))
        ) {
            // Header Row: Title + Count Badge + Expand/Collapse Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Visible to",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = manrope
                        ),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Counter badge pill
                    val badgeContainerColor = if (selectedViewerCount > 0) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    }
                    val badgeContentColor = if (selectedViewerCount > 0) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = badgeContainerColor,
                        contentColor = badgeContentColor
                    ) {
                        Text(
                            text = when {
                                lovedOnesList.isEmpty() -> "0"
                                selectedViewerCount == lovedOnesList.size -> "All (${selectedViewerCount})"
                                else -> "$selectedViewerCount / ${lovedOnesList.size}"
                            },
                            fontFamily = manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                if (lovedOnesList.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onExpandChange)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = when {
                                    expanded -> "Done"
                                    toggleEnabled -> "Edit"
                                    else -> "View"
                                },
                                fontFamily = manrope,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                painter = painterResource(R.drawable.down_arrow),
                                contentDescription = if (expanded) "Collapse audience list" else "Expand audience list",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .rotate(chevronRotation)
                            )
                        }
                    }
                }
            }

            if (locationState is LiveLocationState.NotSharing) {
                Text(
                    text = notSharingHelperText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = manrope,
                        lineHeight = 18.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 12.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            AnimatedContent(
                targetState = expanded,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) togetherWith
                            fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
                },
                label = "LovedOnesExpansion"
            ) { isExpanded ->
                if (!isExpanded) {
                    CollapsedLovedOnes(
                        lovedOnes = lovedOnesList,
                        selectedViewerIds = selectedViewerIds,
                        locationState = locationState,
                        onExpandChange = onExpandChange,
                        toggleEnabled = toggleEnabled
                    )
                } else {
                    ExpandedLovedOnes(
                        lovedOnes = lovedOnesList,
                        selectedViewerIds = selectedViewerIds,
                        updatingViewerId = updatingViewerId,
                        onToggleChange = onToggleChange,
                        toggleEnabled = toggleEnabled,
                        locationState = locationState
                    )
                }
            }
        }
    }
}

@Composable
fun ExpandedLovedOnes(
    lovedOnes: List<CircleMember>,
    selectedViewerIds: Set<String>,
    updatingViewerId: String? = null,
    onToggleChange: (String, Boolean) -> Unit,
    toggleEnabled: Boolean,
    locationState: LiveLocationState
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (toggleEnabled && lovedOnes.size > 1) {
            val allSelected = lovedOnes.all { it.id in selectedViewerIds }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select who can see your location",
                    fontFamily = manrope,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextButton(
                    onClick = {
                        val newTarget = !allSelected
                        lovedOnes.forEach { member ->
                            onToggleChange(member.id, newTarget)
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (allSelected) "Deselect All" else "Select All",
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        lovedOnes.forEachIndexed { index, connection ->
            LovedOneCard(
                connection = connection,
                index = index,
                count = lovedOnes.size,
                selectedViewerIds = selectedViewerIds,
                updatingViewerId = updatingViewerId,
                onToggleChange = onToggleChange,
                toggleEnabled = toggleEnabled,
                locationState = locationState
            )
        }
    }
}

@Composable
private fun CollapsedLovedOnes(
    lovedOnes: List<CircleMember>,
    selectedViewerIds: Set<String>,
    locationState: LiveLocationState,
    onExpandChange: () -> Unit,
    toggleEnabled: Boolean
) {
    val selected = lovedOnes.filter { it.id in selectedViewerIds }
    val contextText = when (locationState) {
        is LiveLocationState.Sharing,
        is LiveLocationState.EmergencySharing -> "Seeing your live location"
        else -> "Will see your live location"
    }

    if (selected.isEmpty()) {
        val emptyText = if (lovedOnes.isEmpty()) {
            "No one in your circle yet. Add circle members to share location."
        } else {
            "No one selected. Tap Edit to choose who sees your location."
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .then(
                    if (lovedOnes.isNotEmpty()) Modifier.clickable(onClick = onExpandChange)
                    else Modifier
                )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.location_sharing),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = emptyText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = manrope,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )

                if (lovedOnes.isNotEmpty()) {
                    Text(
                        text = if (toggleEnabled) "Edit" else "View",
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    } else {
        // Glanceable summary card with overlapping avatar stack
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onExpandChange)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Overlapping avatar cluster
                val maxVisibleAvatars = 3
                val visibleMembers = selected.take(maxVisibleAvatars)
                val remainingCount = selected.size - visibleMembers.size
                val stackWidth = ((visibleMembers.size - 1) * 20 + 36 + (if (remainingCount > 0) 20 else 0)).dp

                Box(
                    modifier = Modifier
                        .size(width = stackWidth, height = 36.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    visibleMembers.forEachIndexed { idx, connection ->
                        val memberName = connection.alias?.takeIf { it.isNotBlank() } ?: connection.profileName
                        IdentityAvatar(
                            avatarUrl = connection.avatarUrl,
                            displayName = memberName,
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            borderColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier
                                .offset(x = (idx * 20).dp)
                                .size(36.dp)
                        )
                    }

                    if (remainingCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.surfaceContainerLow),
                            modifier = Modifier
                                .offset(x = (visibleMembers.size * 20).dp)
                                .size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "+$remainingCount",
                                    fontFamily = manrope,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Names summary + context
                Column(modifier = Modifier.weight(1f)) {
                    val summaryTitle = if (selected.size == 1) {
                        val single = selected.first()
                        single.alias?.takeIf { it.isNotBlank() } ?: single.profileName
                    } else if (selected.size == 2) {
                        val first = selected[0].alias?.takeIf { it.isNotBlank() } ?: selected[0].profileName
                        val second = selected[1].alias?.takeIf { it.isNotBlank() } ?: selected[1].profileName
                        "$first, $second"
                    } else {
                        val first = selected[0].alias?.takeIf { it.isNotBlank() } ?: selected[0].profileName
                        val second = selected[1].alias?.takeIf { it.isNotBlank() } ?: selected[1].profileName
                        "$first, $second +${selected.size - 2} more"
                    }

                    Text(
                        text = summaryTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${selected.size} ${if (selected.size == 1) "person" else "people"} • $contextText",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = manrope,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    painter = painterResource(R.drawable.right_arrow),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LovedOneCard(
    connection: CircleMember,
    index: Int = 0,
    count: Int = 1,
    selectedViewerIds: Set<String> = emptySet(),
    updatingViewerId: String? = null,
    onToggleChange: (String, Boolean) -> Unit = { _, _ -> },
    toggleEnabled: Boolean = true,
    locationState: LiveLocationState = LiveLocationState.NotSharing
) {
    val isSelected = selectedViewerIds.contains(connection.id)
    val isUpdating = updatingViewerId == connection.id
    val displayName = connection.alias?.takeIf { it.isNotBlank() } ?: connection.profileName

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = tween(200),
        label = "lovedOneBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        },
        animationSpec = tween(200),
        label = "lovedOneBorder"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = toggleEnabled && !isUpdating) {
                onToggleChange(connection.id, !isSelected)
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IdentityAvatar(
                avatarUrl = connection.avatarUrl,
                displayName = displayName,
                backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(42.dp)
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = manrope
                    ),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = viewerContextText(
                        isSelected = isSelected,
                        locationState = locationState
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = manrope,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isUpdating) {
                Box(
                    modifier = Modifier.size(48.dp, 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Switch(
                    checked = isSelected,
                    enabled = toggleEnabled && !isUpdating,
                    onCheckedChange = {
                        onToggleChange(connection.id, it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        uncheckedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}

private fun viewerContextText(
    isSelected: Boolean,
    locationState: LiveLocationState
): String {
    return when {
        locationState is LiveLocationState.NotSharing && isSelected -> "Will see your location"
        locationState is LiveLocationState.NotSharing -> "Will not see your location"
        isSelected -> "Seeing your live location"
        else -> "Not included in this session"
    }
}

@Composable
fun itemShape(index: Int, count: Int): Shape {
    val largeCorner = 16.dp
    val smallCorner = 8.dp

    return when {
        count == 1 -> {
            RoundedCornerShape(largeCorner)
        }

        index == 0 -> {
            RoundedCornerShape(
                topStart = largeCorner,
                topEnd = largeCorner,
                bottomStart = smallCorner,
                bottomEnd = smallCorner
            )
        }

        index == count - 1 -> {
            RoundedCornerShape(
                bottomStart = largeCorner,
                bottomEnd = largeCorner,
                topStart = smallCorner,
                topEnd = smallCorner
            )
        }

        else -> {
            RoundedCornerShape(smallCorner)
        }


    }
}

@Composable
fun EmergencyShareButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.5.dp,
            if (enabled)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.outline
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    ) {
        Icon(
            painter = painterResource(R.drawable.emergency),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Emergency Share",
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun StopEmergencyButton(
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )
    ) {
        Icon(
            painter = painterResource(R.drawable.emergency),
            contentDescription = null
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Stop Emergency",
            fontWeight = FontWeight.SemiBold
        )
    }
}


@Composable
fun StopSharingButton(
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
        )
    ) {
        Text(
            text = "Stop Sharing",
            fontWeight = FontWeight.SemiBold
        )
    }
}


@Composable
fun StartSharingButton(
    userPlan: UserPlan,
    enabled: Boolean = true,
    selectedViewerCount: Int = 0,
    onClick: (UserPlan) -> Unit
) {
    val label = when {
        !enabled -> "Select someone to share with"
        selectedViewerCount == 1 -> "Start Sharing with 1 Person"
        else -> "Start Sharing with $selectedViewerCount People"
    }

    FilledTonalButton(
        onClick = {
            onClick(userPlan)
        },
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(
            label,
            fontFamily = manrope,
            fontWeight = FontWeight.SemiBold
        )
    }
}


fun timeAgo(lastUpdatedAt: Long): String {
    val now = System.currentTimeMillis()
    val diffMillis = now - lastUpdatedAt

    val seconds = diffMillis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60

    return when {
        seconds < 60 -> "${seconds}s ago"
        minutes < 60 -> "${minutes} mins ago"
        hours < 24 -> "${hours} hrs ago"
        else -> "${hours / 24} days ago"
    }
}

@Composable
fun LastUpdatedText(lastUpdatedAt: Long) {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(lastUpdatedAt) {
        while (true) {
            delay(1_000)
            now = System.currentTimeMillis()
        }
    }
    val text = remember(lastUpdatedAt, now) {
        "Last updated ${timeAgo(lastUpdatedAt)}"
    }
    Text(
        text,
        fontFamily = manrope,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSecondary,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 10.dp)

    )
}

@Composable
fun JourneyModeCard(
    isDark: Boolean,
    activeSession: com.aarav.geowav.data.model.SharingSession? = null,
    savedPlaces: List<com.aarav.geowav.data.model.Place> = emptyList(),
    onStartJourney: () -> Unit = {},
    onStartJourneyWithPlace: ((com.aarav.geowav.data.model.Place) -> Unit)? = null,
    onStopSharing: () -> Unit = {}
) {
    val isJourneyActive = activeSession != null &&
            activeSession.mode == com.aarav.geowav.data.model.SessionMode.JOURNEY &&
            (activeSession.status == com.aarav.geowav.data.model.SessionStatus.ACTIVE ||
             activeSession.status == com.aarav.geowav.data.model.SessionStatus.COMPLETED)

    val isOtherSharingActive = activeSession != null &&
            activeSession.mode != com.aarav.geowav.data.model.SessionMode.JOURNEY &&
            (activeSession.status == com.aarav.geowav.data.model.SessionStatus.ACTIVE ||
             activeSession.status == com.aarav.geowav.data.model.SessionStatus.PAUSED)

    if (isJourneyActive && activeSession != null) {
        com.aarav.geowav.presentation.home.JourneyInProgressCard(
            session = activeSession,
            onCardClick = onStartJourney,
            onEndJourney = onStopSharing,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
        return
    }


    // Surface styling: Tinted primaryContainer (soft lavender in light, deep navy #222C61 in dark), no outline, 24dp radius
    val containerColor = if (isDark) {
        Color(0xFF222C61)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
    }

    val titleColor = if (isDark) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
    val subtitleColor = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(24.dp),
        border = null,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header Row: Title & Subtitle on Left, Decorative Canvas & Affordance on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (isJourneyActive) "On the way to ${activeSession?.destinationLocation?.name ?: "Destination"}" else "Journey Mode",
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = titleColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when {
                            isOtherSharingActive -> "Stop live sharing to start a journey"
                            isJourneyActive -> "Sharing location until you arrive"
                            else -> "Share your location until you arrive"
                        },
                        fontFamily = manrope,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = subtitleColor,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                if (!isJourneyActive) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Decorative Canvas illustration (Start dot, dashed curved path, destination pin)
                        val accentColor = (if (isDark) Color(0xFF90CAF9) else MaterialTheme.colorScheme.primary).copy(alpha = 0.45f)
                        Canvas(
                            modifier = Modifier.size(width = 54.dp, height = 40.dp)
                        ) {
                            val startX = 6.dp.toPx()
                            val startY = 32.dp.toPx()
                            val endX = 46.dp.toPx()
                            val endY = 10.dp.toPx()

                            // Path
                            val path = Path().apply {
                                moveTo(startX, startY)
                                cubicTo(
                                    18.dp.toPx(), 40.dp.toPx(),
                                    32.dp.toPx(), 6.dp.toPx(),
                                    endX, endY
                                )
                            }
                            drawPath(
                                path = path,
                                color = accentColor,
                                style = Stroke(
                                    width = 2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                                )
                            )
                            // Start dot
                            drawCircle(
                                color = accentColor,
                                radius = 3.5.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(startX, startY)
                            )
                            // Destination pin outer & inner circle
                            drawCircle(
                                color = accentColor,
                                radius = 5.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(endX, endY)
                            )
                            drawCircle(
                                color = containerColor,
                                radius = 2.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(endX, endY)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Single primary affordance circular button
                        Surface(
                            onClick = onStartJourney,
                            shape = CircleShape,
                            color = if (isDark) Color(0xFF3B488C) else Color(0xFF1B2348),
                            contentColor = Color.White,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.path),
                                    contentDescription = "Start Journey",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (isJourneyActive) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onStopSharing,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(
                        text = "End Journey",
                        fontFamily = manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))

                // Bottom row of saved-place chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val chipContainerColor = if (isDark) Color(0xFF2C3775) else MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    val chipContentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

                    val displayPlaces = if (savedPlaces.isNotEmpty()) {
                        savedPlaces
                    } else {
                        // Fallback quick chips if no places saved yet
                        listOf(
                            com.aarav.geowav.data.model.Place(placeName = "Work", customName = "Office"),
                            com.aarav.geowav.data.model.Place(placeName = "Home", customName = "Home")
                        )
                    }

                    displayPlaces.take(3).forEach { place ->
                        val label = place.customName.ifBlank { place.placeName }
                        Surface(
                            onClick = {
                                if (onStartJourneyWithPlace != null && savedPlaces.isNotEmpty()) {
                                    onStartJourneyWithPlace(place)
                                } else {
                                    onStartJourney()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = chipContainerColor,
                            modifier = Modifier.height(40.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.map_pin),
                                    contentDescription = null,
                                    tint = chipContentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = label,
                                    fontFamily = manrope,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = chipContentColor
                                )
                            }
                        }
                    }

                    // Trailing "Other…" chip
                    Surface(
                        onClick = onStartJourney,
                        shape = RoundedCornerShape(12.dp),
                        color = chipContainerColor,
                        modifier = Modifier.height(40.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.search),
                                contentDescription = null,
                                tint = chipContentColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Other…",
                                fontFamily = manrope,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = chipContentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "JourneyModeCard - Light", showBackground = true)
@Composable
private fun JourneyModeCardLightPreview() {
    MaterialTheme {
        JourneyModeCard(
            isDark = false,
            activeSession = null,
            onStartJourney = {},
            onStopSharing = {}
        )
    }
}

@Preview(name = "JourneyModeCard - Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun JourneyModeCardDarkPreview() {
    MaterialTheme {
        JourneyModeCard(
            isDark = true,
            activeSession = null,
            onStartJourney = {},
            onStopSharing = {}
        )
    }
}

@Composable
fun LiveLocationTopBar(
    isEmergencyActive: Boolean,
    onSosClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Live Location",
                        fontFamily = manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Real-time sharing with loved ones",
                        fontFamily = manrope,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Surface(
                    onClick = onSosClick,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.emergency),
                            contentDescription = "SOS Emergency",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isEmergencyActive) "ACTIVE" else "SOS",
                            fontFamily = manrope,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyActiveBanner(
    remainingText: String?,
    onStopEmergency: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.emergency),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Emergency active",
                        fontFamily = manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = if (!remainingText.isNullOrBlank()) "Ends in $remainingText" else "Broadcasting location to circle",
                        fontFamily = manrope,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onStopEmergency,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Stop sharing",
                    fontFamily = manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun EmergencyCountdownSheet(
    isVisible: Boolean,
    onConfirmEmergency: () -> Unit,
    onCancel: () -> Unit
) {
    if (!isVisible) return

    var secondsRemaining by remember { mutableStateOf(5) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(isVisible) {
        if (isVisible) {
            secondsRemaining = 5
            while (secondsRemaining > 0) {
                try {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                } catch (e: Exception) {
                    // Fallback
                }
                delay(1000L)
                secondsRemaining--
            }
            onConfirmEmergency()
        }
    }

    CustomBottomSheet(
        onDismissRequest = onCancel
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.emergency),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Emergency Alert",
                fontFamily = manrope,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Sending in ${secondsRemaining}s…",
                fontFamily = manrope,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "An emergency location broadcast will be sent to your emergency contacts in $secondsRemaining seconds.",
                fontFamily = manrope,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = "Cancel Emergency",
                    fontFamily = manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun EmergencySetupDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onOpenSetup: () -> Unit
) {
    if (!isVisible) return

    CustomBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.user),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Setup Emergency Contacts",
                fontFamily = manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "No emergency contacts found. Add trusted members to your Circle first so emergency SOS alerts can reach them.",
                fontFamily = manrope,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onDismiss()
                    onOpenSetup()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Add Contacts",
                    fontFamily = manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Preview(name = "LiveLocationTopBar - Light", showBackground = true)
@Composable
private fun LiveLocationTopBarLightPreview() {
    GeoWavTheme {
        LiveLocationTopBar(
            isEmergencyActive = false,
            onSosClick = {}
        )
    }
}

@Preview(name = "LiveLocationTopBar - Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun LiveLocationTopBarDarkPreview() {
    GeoWavTheme {
        LiveLocationTopBar(
            isEmergencyActive = false,
            onSosClick = {}
        )
    }
}

@Preview(name = "EmergencyActiveBanner - Light", showBackground = true)
@Composable
private fun EmergencyActiveBannerLightPreview() {
    GeoWavTheme {
        EmergencyActiveBanner(
            remainingText = "14m 30s",
            onStopEmergency = {}
        )
    }
}

@Preview(name = "EmergencyActiveBanner - Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun EmergencyActiveBannerDarkPreview() {
    GeoWavTheme {
        EmergencyActiveBanner(
            remainingText = "14m 30s",
            onStopEmergency = {}
        )
    }
}

@Preview(name = "EmergencyCountdownSheet - Light", showBackground = true)
@Composable
private fun EmergencyCountdownSheetLightPreview() {
    GeoWavTheme {
        EmergencyCountdownSheet(
            isVisible = true,
            onConfirmEmergency = {},
            onCancel = {}
        )
    }
}

@Preview(name = "EmergencyCountdownSheet - Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun EmergencyCountdownSheetDarkPreview() {
    GeoWavTheme {
        EmergencyCountdownSheet(
            isVisible = true,
            onConfirmEmergency = {},
            onCancel = {}
        )
    }
}
