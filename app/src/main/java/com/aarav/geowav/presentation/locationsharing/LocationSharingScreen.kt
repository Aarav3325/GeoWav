package com.aarav.geowav.presentation.locationsharing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.core.utils.LiveLocationState
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.data.model.UpgradeContext
import com.aarav.geowav.data.model.UpgradeReason
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.presentation.components.EmergencyShareDialog
import com.aarav.geowav.presentation.components.CustomBottomSheet
import com.aarav.geowav.presentation.components.IdentityAvatar
import com.aarav.geowav.presentation.components.PermissionRequiredContent
import com.aarav.geowav.presentation.components.SnackbarManager
import com.aarav.geowav.presentation.components.UpgradeBottomSheetContent
import com.aarav.geowav.presentation.subscription.SubscriptionViewModel
import com.aarav.geowav.presentation.theme.manrope
import com.aarav.geowav.presentation.theme.primaryLight
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
    viewModel: LocationSharingVM,
    navigateToPaywall: () -> Unit,
    navigateToSettings: () -> Unit,
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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Live Location Sharing",
                        fontFamily = manrope,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                    )
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (!locationServicesReady) {
            PermissionRequiredContent(
                title = "Location setup is needed",
                message = "Live sharing and emergency sharing need live and background location access so updates can continue reliably.",
                primaryActionText = "Review setup",
                onPrimaryAction = navigateToSettings,
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }

        LocationSharingContent(
            modifier = Modifier.padding(padding),
            locationUiState = uiState,
            plan,
            cameraPositionState,
            onToggleChange = viewModel::onViewerToggle,
            onStartSharing = viewModel::startLiveLocationSharing,
            onStopSharing = viewModel::stopLiveLocationSharing,
            onStartEmergency = viewModel::startEmergency,
            onStopEmergency = viewModel::stopEmergency
        )

    }
}

@Composable
fun LocationSharingContent(
    modifier: Modifier = Modifier,
    locationUiState: LiveLocationUiState,
    userPlan: UserPlan,
    cameraPosition: CameraPositionState,
    onToggleChange: (String, Boolean) -> Unit,
    onStartSharing: (UserPlan) -> Unit,
    onStopSharing: () -> Unit,
    onStartEmergency: (Int) -> Unit,
    onStopEmergency: () -> Unit
) {

    var showEmergencyDialog by remember {
        mutableStateOf(false)
    }


    val isEmergencyActive = locationUiState.emergencyEndsAt != null
    val selectedViewerCount = locationUiState.selectedViewerIds.size

    EmergencyShareDialog(
        showEmergencyDialog,
        onConfirm = {
            onStartEmergency(15)
            showEmergencyDialog = false
        },
        onDismiss = { showEmergencyDialog = false }
    )

    val lazyState = rememberLazyListState()

    var expanded by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(expanded) {
        if (expanded) {
            lazyState.animateScrollToItem(2)
        }
    }


    Column(
        modifier = modifier
    ) {
        LazyColumn(
            state = lazyState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
//            item {
//                Text(
//                    text = "Live Location Sharing",
//                    fontSize = 24.sp,
//                    fontFamily = manrope,
//                    fontWeight = FontWeight.Bold,
//                    color = MaterialTheme.colorScheme.onBackground,
//                    modifier = Modifier.padding(top = 54.dp, start = 16.dp, end = 16.dp)
//                )
//            }

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

            item {
                EmergencyShareButton(
                    enabled = !isEmergencyActive &&
                            !locationUiState.isEmergencyLoading &&
                            locationUiState.sharingState !is LiveLocationState.EmergencySharing
                ) {
                    showEmergencyDialog = true
                }
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
            MaterialTheme.colorScheme.surfaceContainerHigh

        LiveLocationState.Starting,
        is LiveLocationState.Sharing ->
            MaterialTheme.colorScheme.tertiaryContainer

        is LiveLocationState.EmergencySharing ->
            MaterialTheme.colorScheme.errorContainer

        is LiveLocationState.Error ->
            MaterialTheme.colorScheme.surfaceContainerHigh
    }


    val selectedAudienceText = when (selectedViewerCount) {
        0 -> "No one selected yet"
        1 -> "Ready to share with 1 person"
        else -> "Ready to share with $selectedViewerCount people"
    }

    val (title, subtitle, color) = when (liveLocationState) {
        LiveLocationState.NotSharing -> {
            Triple(
                "Not sharing location",
                selectedAudienceText,
                MaterialTheme.colorScheme.primary
            )
        }

        LiveLocationState.Starting -> {
            Triple(
                "Starting live location sharing...",
                "Please wait",
                MaterialTheme.colorScheme.primary
            )
        }

        is LiveLocationState.Sharing -> {
            Triple(
                "Sharing live location",
                if (liveLocationState.visibleCount > 1) "Sharing with: ${liveLocationState.visibleCount} people" else "Sharing with: 1 person",
                Color(0xFF2E7D32)
            )
        }

        is LiveLocationState.Error -> {
            Triple(
                "Location sharing stopped",
                liveLocationState.message,
                MaterialTheme.colorScheme.error
            )
        }

        is LiveLocationState.EmergencySharing ->
            Triple(
                "Emergency sharing active",
                "Ends in ${liveLocationState.remainingTime}",
                Color.Red
            )


        else -> {
            Triple("", "", Color.Transparent)
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp)
        ) {
            AnimatedVisibility(!isEmergencyActive) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Surface(
                        color = Color.White,
                        shape = CircleShape,
                        shadowElevation = 2.dp
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.new_logo),
                            contentDescription = null,
                            tint = primaryLight,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))


                    Column(
                        Modifier.weight(1f),
                        //verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            title, fontWeight = FontWeight.SemiBold,
                            fontFamily = manrope,
                            fontSize = 12.sp,
                        )
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodySmall, fontFamily = manrope
                        )
                    }


                    Spacer(Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(color, CircleShape)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.error,
                    shape = CircleShape,
                    shadowElevation = 2.dp,

                    ) {
                    Icon(
                        painter = painterResource(R.drawable.emergency),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(4.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))


                Column(
                    Modifier.weight(1f),
                    //verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        if (isEmergencyActive)
                            "Emergency location sharing is active"
                        else
                            "Emergency location sharing is disabled",
                        fontFamily = manrope,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "Share live location to all loved ones in case of an emergency",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = manrope
                    )
                }

                Spacer(Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(MaterialTheme.colorScheme.error, CircleShape)
                )
            }

            AnimatedVisibility(isEmergencyActive) {

                val sharingState = liveLocationState as? LiveLocationState.EmergencySharing

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Emergency ends in",
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    )

                    Text(
                        remainingTime ?: "00:00",
                        fontFamily = manrope,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                    )
                }
            }

            when (liveLocationState) {
                is LiveLocationState.Sharing -> {
                    StopSharingButton(onStop)
                }

                LiveLocationState.NotSharing -> {
                    StartSharingButton(
                        userPlan = userPlan,
                        enabled = selectedViewerCount > 0,
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MapPreviewCard(
    cameraPosition: CameraPositionState,
    liveLocationState: LiveLocationState
) {
//    GoogleMap(
//        modifier = Modifier
//            .fillMaxWidth()
//            .border(
//                1.dp,
//                MaterialTheme.colorScheme.outline,
//                RoundedCornerShape(16.dp)
//            )
//            .height(220.dp)
//            .clip(RoundedCornerShape(24.dp)),
//    )

    var mapLoad by remember {
        mutableStateOf(false)
    }


    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .size(200.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
//            Text(
//                "Google Map Preview",
//                fontFamily = manrope,
//                fontWeight = FontWeight.SemiBold,
//                color = MaterialTheme.colorScheme.onBackground,
//                style = MaterialTheme.typography.labelLarge,
//                modifier = Modifier
//                    .padding(vertical = 6.dp, horizontal = 12.dp)
//                    .align(Alignment.Center)
//            )
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

            if(!mapLoad) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ContainedLoadingIndicator()
                }
            }


            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                cameraPositionState = cameraPosition,
                uiSettings = uiSettings,
                properties = mapProperties,
                onMapLoaded = {
                    mapLoad = true
                }
            )


            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 8.dp)
                    .align(Alignment.BottomEnd)
            ) {

                if (liveLocationState !is LiveLocationState.NotSharing) {

                    val state = liveLocationState as? LiveLocationState.Sharing
                    if (state != null) {
                        LastUpdatedText(state.lastUpdatedText)
                    }
                }
            }
        }
    }
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

    val toggleEnabled =
        locationState is LiveLocationState.NotSharing

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(16.dp)
    ) {

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .animateContentSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Visible to",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = manrope
                    ),
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )

                TextButton(onClick = onExpandChange) {
                    Text(
                        if (expanded) "Collapse" else "Edit",
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }

            when (locationState) {
                LiveLocationState.NotSharing -> {
                    Text(
                        "When you start sharing, these people will be able to see your live location.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Normal,
                            fontFamily = manrope,
                            lineHeight = 20.sp
                        ),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }

                else -> {}
            }

            if (!expanded) {
                CollapsedLovedOnes(
                    lovedOnes = lovedOnesList,
                    selectedViewerIds = selectedViewerIds,
                    locationState = locationState
                )
            } else {
                ExpandedLovedOnes(
                    lovedOnesList,
                    selectedViewerIds,
                    updatingViewerId,
                    onToggleChange,
                    toggleEnabled
                )
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
    toggleEnabled: Boolean
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        lovedOnes.forEachIndexed { index, connection ->
            LovedOneCard(
                connection,
                index,
                lovedOnes.size,
                selectedViewerIds,
                updatingViewerId,
                onToggleChange,
                toggleEnabled
            )
        }
    }
}

@Composable
private fun CollapsedLovedOnes(
    lovedOnes: List<CircleMember>,
    selectedViewerIds: Set<String>,
    locationState: LiveLocationState
) {

    val selected = lovedOnes.filter { it.id in selectedViewerIds }
    val contextText = when (locationState) {
        is LiveLocationState.Sharing,
        is LiveLocationState.EmergencySharing -> "Seeing your live location"
        else -> "Will see your location"
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        if (selected.isEmpty()) {
            Text(
                "No one is selected yet",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = manrope,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )
            return@Column
        }

        selected.take(3).forEach { connection ->
            VisibleToPersonRow(
                connection = connection,
                contextText = contextText
            )
        }

        if (selected.size > 3) {
            Text(
                "+${selected.size - 3} more included",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = manrope,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 54.dp, top = 2.dp)
            )
        }
    }

}

@Composable
private fun VisibleToPersonRow(
    connection: CircleMember,
    contextText: String,
    modifier: Modifier = Modifier
) {
    val displayName = connection.alias?.takeIf { it.isNotBlank() } ?: connection.profileName

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IdentityAvatar(
            avatarUrl = connection.avatarUrl,
            displayName = displayName,
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(38.dp)
        )

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                displayName,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                contextText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = manrope,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LovedOneCard(
    connection: CircleMember,
    index: Int,
    count: Int,
    selectedViewerIds: Set<String>,
    updatingViewerId: String? = null,
    onToggleChange: (String, Boolean) -> Unit,
    toggleEnabled: Boolean
) {

    val shape = itemShape(index, count)

    val isSelected = selectedViewerIds.contains(connection.id)
    val isUpdating = updatingViewerId == connection.id


    Row(
        modifier = Modifier
            .padding(vertical = 1.5.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val displayName = connection.alias?.takeIf { it.isNotBlank() } ?: connection.profileName

        IdentityAvatar(
            avatarUrl = connection.avatarUrl,
            displayName = displayName,
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(42.dp)
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                displayName,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = manrope
                ),
                fontSize = 14.sp
            )
            Text(
                if (isSelected) "Included in live sharing" else "Not included",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = manrope,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
        }

        Switch(
            checked = isSelected,
            enabled = toggleEnabled,
            onCheckedChange = {
                onToggleChange(connection.id, it)
            },
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
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
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .padding(bottom = 36.dp)
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
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
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
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
    onClick: (UserPlan) -> Unit
) {
    FilledTonalButton(
        onClick = {
            onClick(userPlan)
        },
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Text(
            if (enabled) "Start Sharing" else "Select someone to share with",
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
        modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp)

    )
}
