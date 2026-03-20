package com.aarav.geowav.presentation.home


import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import com.aarav.geowav.R
import com.aarav.geowav.core.utils.ViewerLocationState
import com.aarav.geowav.core.utils.formatRemainingForEmergency
import com.aarav.geowav.core.utils.formatTime
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.presentation.theme.GeoWavTheme
import com.aarav.geowav.presentation.theme.manrope
import com.aarav.geowav.presentation.theme.sora
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import androidx.core.net.toUri
import com.aarav.geowav.presentation.navigation.NavItem
import com.aarav.geowav.presentation.subscription.SubscriptionViewModel

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun GeoWavHomeScreen(
    isDarkThemeEnabled: Boolean,
    navigateToYourPlaces: () -> Unit,
    onAddZone: () -> Unit,
    navigateToObserve: () -> Unit,
    onShareLocation: () -> Unit,
    onOpenAlerts: () -> Unit,
    homeScreenVM: HomeScreenVM,
    subscriptionViewModel: SubscriptionViewModel,
    navigateToSettings: () -> Unit,
    navigateToPaywall: () -> Unit,
    navigateToCircle: () -> Unit,
    navigateToTimeline: (String, String) -> Unit,
    navigateToActivity: () -> Unit,
    modifier: Modifier = Modifier
) {

    val uiState by homeScreenVM.uiState.collectAsState()
    val locations by homeScreenVM.locations.collectAsState()

    val plan by subscriptionViewModel.userPlan.collectAsState()


    val notificationPermission =
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)

    LaunchedEffect(Unit) {
        if (!notificationPermission.status.isGranted) {

            notificationPermission.launchPermissionRequest()
        }
    }


    val context = LocalContext.current
    val activity = context as? Activity


    LaunchedEffect(uiState.lovedOnes) {
        if (uiState.lovedOnes.isNotEmpty()) {
            Log.i("OBSERVE", "observe called")
            homeScreenVM.observeUsers()
        }
        homeScreenVM.cleanupRemovedUsers(
            uiState.lovedOnes.map { it.id }.toSet()
        )
    }

//    Log.i("SESSION", sessionHistory.toString())

    LaunchedEffect(Unit) {
        homeScreenVM.loadLovedOnes()
    }

//    Log.i("HOME", uiState.lovedOnes.toString())

    LaunchedEffect(locations) {
        if (locations.isNotEmpty()) {
            homeScreenVM.fetchViewerInfo()
        }
    }


    val scope = rememberCoroutineScope()

    val scroll = rememberScrollState()
    val scrollOffset = scroll.value

    // Switch colors after scrolling 240px
    val useDarkIcons = scrollOffset > 150

    // Animate colors smoothly
    val textColor by animateColorAsState(
        targetValue = if (useDarkIcons) {
            if (isDarkThemeEnabled) {
                Color.White
            } else {
                Color.Black
            }
        } else {
            Color.Black
        },
        animationSpec = tween(durationMillis = 500), // smooth 0.8s fade
        label = "TextColorAnimation"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (useDarkIcons)
            MaterialTheme.colorScheme.primaryContainer
        else
            Color.Transparent,
        animationSpec = tween(durationMillis = 500),
        label = "BackgroundColorAnimation"
    )



    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            if (!uiState.username.isNullOrEmpty()) {
                TopAppBar(
                    title = {
                        Text(
                            text = "GeoWav",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = textColor
                            ),
                            fontFamily = manrope,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                    IconButton(
                        onClick = {
                            navigateToPaywall()
//                            activity?.let {
//                                homeScreenVM.launchBillingFlow(it, "")
//                            }
                        }
                    ) {
                        Image(
                            painter = painterResource(R.drawable.payment),
                            contentDescription = "payment",
                            modifier = Modifier.size(28.dp),
                            colorFilter = ColorFilter.tint(textColor)
                        )
                    }

                        IconButton(
//                        onClick = onThemeChange
                            onClick = {
                                navigateToSettings()
//                            homeScreenVM.signOut()
//                            navigateToAuth()
                            }
                        ) {
                            Image(
                                painter = painterResource(R.drawable.gear_six),
                                contentDescription = "setting",
                                modifier = Modifier.size(28.dp),
                                colorFilter = ColorFilter.tint(textColor)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = backgroundColor
                    )
                )
            }
        }

    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {

//            locations.forEach { (ownerId, state) ->
//                when (state) {
//                    is ViewerLocationState.NormalSharing -> {
//                        Log.i("OBSERVE", "user: $ownerId, location: ${state.location}")
//                    }
//
//
//                    is ViewerLocationState.EmergencySharing -> {
//                        Log.i(
//                            "OBSERVE",
//                            "user: $ownerId, location: ${state.location}, remaining: ${state.endsAt}"
//                        )
//                    }
//
//                    ViewerLocationState.Blocked -> Unit
//                }
//            }

            if (uiState.username.isNullOrEmpty()) {
                ContainedLoadingIndicator(
                    Modifier.align(Alignment.Center)
                )
            } else {

                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .verticalScroll(scroll)
                        .background(MaterialTheme.colorScheme.background)
                ) {


                    Box(
                        modifier = Modifier
                            .fillMaxWidth()

                            .background(MaterialTheme.colorScheme.background)
                    ) {


                        Image(
                            painter = if (isDarkThemeEnabled) painterResource(R.drawable.dark_bg_geowav_new_2) else painterResource(
                                R.drawable.light_bg_geowav_new
                            ),
                            contentDescription = "bg",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        )



                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.background.copy(alpha = 0.1f),
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                                        )
                                    )
                                )
                        )


                        ProfileCard(
                            avatar = uiState.userAvatar,
                            userName = uiState.username,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(top = 92.dp),
                            isDarkThemeEnabled = isDarkThemeEnabled
                        )
                    }

                    Column(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .background(MaterialTheme.colorScheme.background)
                    ) {

//                    val hasAnyLiveSharing = uiState.locations.values.any {
//                        it is ViewerLocationState.NormalSharing ||
//                                it is ViewerLocationState.EmergencySharing
//                    }
//
//                    val viewerInfo = uiState.currentViewers

                        val activeViewerIds = locations
                            .filterValues {
                                it is ViewerLocationState.NormalSharing ||
                                        it is ViewerLocationState.EmergencySharing
                            }
                            .keys

                        val hasAnyLiveSharing = activeViewerIds.isNotEmpty()


                        AnimatedVisibility(hasAnyLiveSharing) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                            ) {

                                val viewers = uiState.lovedOnes.filter {
                                    it.id in activeViewerIds
                                }

                                ViewerCardHome(viewers, navigateToObserve)

                                ObserveLiveLocationCard(
                                    homeScreenVM,
                                    uiState,
                                    false,
                                    false,
                                    onHideClick = {},
                                    onShowTray = {},
                                    navigateToObserve,
                                    Modifier
                                        .height(220.dp)
                                )
                            }
                        }

//                    ObserveLiveLocationCard(
//                        homeScreenVM, uiState, false, navigateToObserve, Modifier
//                            .height(220.dp)
//                    )

//                    ObserveLiveLocationCard(
//                        uiState.locations
//                    )


//
//                    AnimatedVisibility(hasAnyLiveSharing && viewerInfo.isNotEmpty()) {
//                        ViewerCardHome(viewerInfo, navigateToObserve)
//                    }


                        ConnectionsList(
                            title = "Your Circle",
                            connections = uiState.lovedOnes,
                            locationStates = locations,
                            onManage = navigateToCircle,
                            navigateToTimeline = navigateToTimeline
                        )

                        ActiveZonesSection(
                            zones = uiState.placesList,
                            onZoneClick = {},
                            onViewAllClick = {
                                navigateToYourPlaces()
                            }
                        )

                        if (uiState.placesList.isEmpty()) {
                            QuickActionsRow(
                                onAddZone = onAddZone,
                                onShare = onShareLocation,
                                onAlerts = onOpenAlerts
                            )
                        }

                        Row(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Your Activity",
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = manrope,
                                ),
                                fontSize = 16.sp,
                            )

                            TextButton(onClick = navigateToActivity) {
                                Text(
                                    "View All",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontFamily = manrope
                                )
                            }
                        }

                        RecentAlertsList(uiState.alertsList.take(5), isDarkThemeEnabled)


                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                color = Color(0xFFBAC3FF),
                                shape = CircleShape
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.new_logo),
                                    contentDescription = "logo",
                                    tint = Color(0xFF222C61),
                                    modifier = Modifier.size(56.dp),
                                )
                            }

                            Text(
                                text = "GeoWav",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 24.sp,
                                fontFamily = manrope,
                                fontWeight = FontWeight.Bold
                            )

//                        Row(
//                            horizontalArrangement = Arrangement.spacedBy(8.dp)
//                        ) {
//                            Text(
//                                text = "Made With Love",
//                                color = MaterialTheme.colorScheme.onBackground,
//                                fontSize = 16.sp,
//                                fontFamily = manrope,
//                                fontWeight = FontWeight.Bold
//                            )
//
//                            Icon(
//                                painter = painterResource(R.drawable.heart_fill),
//                                contentDescription = "logo",
//                                modifier = Modifier.size(24.dp),
//                                tint = MaterialTheme.colorScheme.error
//                            )
//                        }
                        }
                    }
                }
            }
        }
    }


}

@Composable
fun UserMarkerUi(
    isEmergency: Boolean
) {
    val pulse by rememberInfiniteTransition().animateFloat(
        initialValue = 0.9f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(48.dp)
    ) {
        // Emergency ring
        if (isEmergency) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .scale(pulse)
                    .background(
                        Color.Red.copy(alpha = 0.25f),
                        CircleShape
                    )
            )
        }

        // Main marker
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    if (isEmergency) Color.Red else MaterialTheme.colorScheme.primary,
                    CircleShape
                )
                .border(2.dp, Color.White, CircleShape)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewLocationCard() {

}

@Composable
fun CurrentLocationCard(city: String, lastUpdated: String, onViewMap: () -> Unit) {

    val Primary = MaterialTheme.colorScheme.primary
    val Accent = MaterialTheme.colorScheme.tertiary
    Card(
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFE9F2FF), Color(0xFFF6FBFF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.map_pin),
                    contentDescription = "map",
                    tint = Accent,
                    modifier = Modifier.size(36.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    city,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = manrope,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Last updated • $lastUpdated",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Accent,
                        fontFamily = manrope
                    )
                )
            }

            Button(
                onClick = onViewMap,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("View", color = Color.White, fontFamily = manrope)
            }
        }
    }
}

@Composable
fun ConnectionsList(
    title: String,
    connections: List<CircleMember>,
    locationStates: Map<String, ViewerLocationState>,
    onManage: () -> Unit,
    navigateToTimeline: (String, String) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, top = 8.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = manrope
                ),
                fontSize = 16.sp,
                modifier = Modifier.weight(1.0f)
            )

            TextButton(onClick = onManage) {
                Text(
                    "Manage",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = manrope
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (connections.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.empty),
                        contentDescription = "empty icon",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary)
                    )

                    Text(
                        "No connections yet",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = manrope
                        )
                    )
                }

            } else {

                connections.forEach { conn ->
                    val state = locationStates[conn.id]

                    ConnectionStatusCard(
                        member = conn,
                        locationState = state,
                        navigateToTimeline
                    )
                }

                if(connections.isEmpty()) {
                    QuickActionButton(R.drawable.plus_circle__1_, "Add", onManage)
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusCard(
    member: CircleMember,
    locationState: ViewerLocationState?,
    navigateToTimeline: (String, String) -> Unit
) {

    val name = member.alias ?: member.profileName

    val finalName = name.split(" ").first()
    val emergencyState = locationState as? ViewerLocationState.EmergencySharing
    val isSharingActive = locationState as? ViewerLocationState.NormalSharing

    // Emergency count down
    val remaining by produceState(
        initialValue = formatRemainingForEmergency(emergencyState?.endsAt ?: 0L),
        key1 = emergencyState?.endsAt
    ) {
        val endsAt = emergencyState?.endsAt ?: return@produceState

        while (true) {
            value = formatRemainingForEmergency(endsAt)
            delay(1000)

            if (formatRemainingForEmergency(endsAt) == "00:00") break
        }
    }

    val (statusText, statusColor, isSharing) = when (locationState) {
        is ViewerLocationState.EmergencySharing ->
            Triple("Emergency", MaterialTheme.colorScheme.error, true)

        is ViewerLocationState.NormalSharing ->
            Triple("Live", MaterialTheme.colorScheme.primary, true)

        else ->
            Triple("Not Sharing", MaterialTheme.colorScheme.outline, false)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {

        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            statusColor.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.new_logo),
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = finalName,
                        fontFamily = manrope,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(0.dp))

                    Text(
                        text = statusText,
                        fontFamily = manrope,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }

                // Status label on right
                if (isSharingActive != null || emergencyState != null) {
                    Surface(
                        shape = RoundedCornerShape(25),
                        color = statusColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = statusText,
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 4.dp
                            ),
                            fontFamily = manrope,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    }
                }

                Spacer(Modifier.width(10.dp))

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            navigateToTimeline(member.id, name)
                        }
                        .size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.timeline),
                        contentDescription = "View Timeline",
                        tint = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(6.dp)
                    )
                }
            }


            // Emergency count down section
            if (emergencyState != null) {


                Spacer(Modifier.height(12.dp))

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
                        remaining,
                        fontFamily = manrope,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                    )
                }
            }

            if (isSharing && locationState != null) {

                val location = when (locationState) {
                    is ViewerLocationState.NormalSharing -> locationState.location
                    is ViewerLocationState.EmergencySharing -> locationState.location
                    else -> null
                }

                location?.let {

                    Spacer(Modifier.height(12.dp))

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Updated ${formatTime(it.timestamp)}",
                            fontFamily = manrope,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Started ${formatTime(it.startedAt)}",
                            fontFamily = manrope,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun InfoColumn(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontFamily = manrope,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(Modifier.height(2.dp))

        Text(
            text = value,
            fontFamily = manrope,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ActiveZonesSection(
    zones: List<Place>,
    onZoneClick: (com.aarav.geowav.data.model.Place) -> Unit,
    onViewAllClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 0.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Active Zones",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = manrope
                )
            )

            TextButton(onClick = {
                onViewAllClick()
            }) {
                Text(
                    "View All",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = manrope
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (zones.isNotEmpty()) {
                zones.forEach { zone ->
                    ZoneCard(
                        zone = zone,
                        onClick = { onZoneClick(zone) })
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.navigation_arrow),
                        contentDescription = "empty icon",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary)
                    )

                    Text(
                        "No zones are added yet",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = manrope
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ZoneCard(zone: Place, onClick: () -> Unit) {

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.map_pin),
                        contentDescription = "zone",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        zone.customName.ifEmpty {
                            zone.placeName
                        },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = manrope,
                            lineHeight = 17.sp
                        )
                    )

                    Text(
                        "${zone.radius.toInt()}m • Enter/Exit Trigger",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontFamily = manrope
                        ),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.width(6.dp))

                TextButton(onClick = { }) {
                    Text(
                        "Active",
                        fontSize = 14.sp,
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

        }
    }
}

@Composable
fun QuickActionsRow(onAddZone: () -> Unit, onShare: () -> Unit, onAlerts: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionButton(R.drawable.plus_circle__1_, "Add Zone", onAddZone)
    }
}

@Composable
fun QuickActionButton(
    icon: Int,
    label: String,
    onClick: () -> Unit
) {

    FilledTonalButton(
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = label,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = manrope,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun RecentAlertsList(
    alerts: List<com.aarav.geowav.data.model.GeoAlert>,
    isDarkThemeEnabled: Boolean
) {


    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        if (alerts.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.link_break),
                    contentDescription = "break",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    "No recent alerts. You’re all clear!",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = manrope
                    )
                )
            }
        } else {
            alerts.forEach { alert -> AlertItem(alert, isDarkThemeEnabled) }
        }
    }
}

@Composable
fun AlertItem(alert: com.aarav.geowav.data.model.GeoAlert, isDarkThemeEnabled: Boolean) {

    val type = if (alert.type.equals("ENTER", ignoreCase = true)) "enter" else "exit"

    val isEnter = alert.type.equals("ENTER", true)
    val relativeTime = buildRelativeSubtitle(type, alert.readableTime)


    val boxColor = Color(0xFFEDEDED)
    val iconColor = Color(0xFF4A4A4A)

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkThemeEnabled) {
                if (isEnter) Color(0xFF00513f) else Color(0xFF723339)
            } else {
                if (isEnter) Color(0xFFa3f2d6) else Color(0xFFffdadb)
            },
            contentColor =
                if (isDarkThemeEnabled) {
                    if (isEnter) Color(0XFFa3f2d6) else Color(0xFFffdadb)
                } else {
                    if (isEnter) Color(0xFF00513f) else Color(0xFF723339)
                }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    //.background(Color(0xFFBAFFDF)),
                    .background(
                        boxColor
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.map_pin),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = iconColor
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    alert.title,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = manrope
                    )
                )
                Text(
                    relativeTime,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = manrope),
                    maxLines = 2
                )
            }
            Text(
                alert.time,
                fontFamily = manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}


@Composable
fun ProfileCard(
    avatar: String?,
    userName: String?,
    modifier: Modifier = Modifier,
    isDarkThemeEnabled: Boolean
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome,",
                    fontFamily = manrope,
                    fontSize = 18.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                )

                Text(
                    text = userName ?: "",
                    fontFamily = manrope,
                    fontSize = 32.sp,
                    color = Color.Black,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                )
            }


            val imageUrl = remember(avatar, isDarkThemeEnabled) {
                if (avatar.isNullOrBlank()) {
                    if (isDarkThemeEnabled) {
                        "https://storage.googleapis.com/geowav-bucket-1/user_dark_theme.svg"
                    } else {
                        "https://storage.googleapis.com/geowav-bucket-1/user_light_theme.svg"
                    }
                } else {
                    avatar
                }
            }

            val context = LocalContext.current
            val imageLoader = ImageLoader.Builder(context)
                .components {
                    add(SvgDecoder.Factory())
                }
                .build()

            Surface(
                shape = CircleShape,
                modifier = Modifier.size(84.dp),
                color = Color.White
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "User avatar",
                    imageLoader = imageLoader,
                    placeholder = painterResource(R.drawable.user),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(84.dp)
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun CurrentLocationCardPreview() {
    GeoWavTheme {
        CurrentLocationCard(
            "Ahmedabad",
            "3 min ago"
        ) { }
    }
}

fun buildRelativeSubtitle(type: String, timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val minutes = diff / 60000
    val hours = diff / (60000 * 60)

    val verb = when (type) {
        "enter" -> "Reached"
        else -> "Left"
    }

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min${if (minutes > 1) "s" else ""} ago"
        hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
        else -> {
            val df = java.text.SimpleDateFormat("dd MMMM y", java.util.Locale.getDefault())
            "${df.format(java.util.Date(timestamp))}"
        }
    }
}
