@file:SuppressLint("InlinedApi")

package com.aarav.geowav.presentation.home

import android.annotation.SuppressLint


import android.app.Activity
import android.util.Log
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.ImageLoader
import coil.decode.SvgDecoder
import com.aarav.geowav.R
import com.aarav.geowav.core.permissions.GeoPermissionUiState
import com.aarav.geowav.core.utils.SubscriptionHelper
import com.aarav.geowav.core.utils.ViewerLocationState
import com.aarav.geowav.core.utils.formatRemainingForEmergency
import com.aarav.geowav.core.utils.formatTime
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.data.model.User
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.presentation.components.AvatarImage
import com.aarav.geowav.presentation.subscription.SubscriptionViewModel
import com.aarav.geowav.presentation.theme.manrope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.delay

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
    val activeSharingCount = locations.count { (_, state) ->
        state is ViewerLocationState.NormalSharing ||
                state is ViewerLocationState.EmergencySharing
    }

    val plan by subscriptionViewModel.userPlan.collectAsState()


    val hideTopBar = uiState.currentUser == null


    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                homeScreenVM.refreshPermissionState()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    LaunchedEffect(uiState.lovedOnes) {
        if (uiState.lovedOnes.isNotEmpty()) {
            Log.i("OBSERVE", "observe called")
            homeScreenVM.observeUsers()
        }
        homeScreenVM.cleanupRemovedUsers(
            uiState.lovedOnes.map { it.id }.toSet()
        )
    }



    LaunchedEffect(Unit) {
        homeScreenVM.loadLovedOnes()
    }



    LaunchedEffect(locations) {
        if (locations.isNotEmpty()) {
            homeScreenVM.fetchViewerInfo()
        }
    }


    val scope = rememberCoroutineScope()

    val scroll = rememberScrollState()

    // Switch colors after scrolling 240px
    val useDarkIcons by remember {
        derivedStateOf { scroll.value > 150 }
    }

    // Animate colors smoothly
    val textColor by animateColorAsState(
        targetValue =

            if (hideTopBar) Color.Transparent
            else if (useDarkIcons) {
                if (isDarkThemeEnabled) {
                    Color.White
                } else {
                    Color.Black
                }
            } else {
                Color.Black
            },
        animationSpec = tween(durationMillis = 500), // smooth 0.5s fade
        label = "TextColorAnimation"
    )

    val backgroundColor by animateColorAsState(
        targetValue =
            if (hideTopBar) Color.Transparent
            else if (useDarkIcons)
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
                            onClick = {
                                navigateToSettings()
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


            if (uiState.currentUser == null) {
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
                                .height(220.dp)
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
                            plan,
                            avatar = uiState.userAvatar,
                            currentUser = uiState.currentUser,
                            userName = uiState.username,
                            activeSharingCount = activeSharingCount,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(top = 76.dp),
                            isDarkThemeEnabled = isDarkThemeEnabled
                        )
                    }

                    Column(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .background(MaterialTheme.colorScheme.background)
                    ) {


                        val activeViewerIds = locations
                            .filterValues {
                                it is ViewerLocationState.NormalSharing ||
                                        it is ViewerLocationState.EmergencySharing
                            }
                            .keys

                        val hasAnyLiveSharing = activeViewerIds.isNotEmpty()

                        AnimatedVisibility(uiState.permissionState.shouldShowSetupCard) {
                            LocationSetupReminderCard(
                                permissionState = uiState.permissionState,
                                onReviewClick = navigateToSettings,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }


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
                                onAddZone = onAddZone
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

@Composable
fun LocationSetupReminderCard(
    permissionState: GeoPermissionUiState,
    onReviewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = when {
        !permissionState.foregroundLocationGranted -> "Live location is not set up"
        !permissionState.backgroundLocationGranted -> "Background access is not set up"
        else -> "Permission setup is incomplete"
    }

    val message = when {
        !permissionState.foregroundLocationGranted ->
            "Live movement, emergency sharing, and place alerts need location access before they can run."
        !permissionState.backgroundLocationGranted ->
            "Place alerts and active safety sessions need background access to keep working when GeoWav is not open."
        else ->
            "Some safety alerts may stay paused until permissions are enabled."
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = CircleShape
                ) {
                    Icon(
                        painter = painterResource(R.drawable.map_pin),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier
                            .size(34.dp)
                            .padding(7.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontFamily = manrope,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Features stay paused until you enable access.",
                        fontFamily = manrope,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.78f)
                    )
                }
            }

            Text(
                text = message,
                fontFamily = manrope,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            FilledTonalButton(
                onClick = onReviewClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Review setup",
                    fontFamily = manrope,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
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

    Log.i("HOME", "connection list section recompose")
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

                if (connections.isEmpty()) {
                    QuickActionButton(R.drawable.add, "Add", onManage)
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
    Log.i("HOME", "connection card section recompose")

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
                "Active Places",
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
                        .padding(top = 2.dp, bottom = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
                        contentColor = MaterialTheme.colorScheme.secondary,
                        shape = CircleShape
                    ) {
                        Image(
                            painter = painterResource(R.drawable.navigation_arrow),
                            contentDescription = "empty icon",
                            modifier = Modifier
                                .size(34.dp)
                                .padding(8.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary)
                        )
                    }

                    Text(
                        "No active places yet",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = manrope,
                            fontWeight = FontWeight.SemiBold
                        ),
                        fontSize = 13.sp
                    )

                    Text(
                        "Add a place to start getting calm arrival and exit awareness.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = manrope
                        ),
                        fontSize = 12.sp
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
fun QuickActionsRow(onAddZone: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionButton(R.drawable.add, "Add Place", onAddZone)
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
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = label,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = manrope,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                ),
                fontSize = 13.sp
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f),
                    contentColor = MaterialTheme.colorScheme.tertiary,
                    shape = CircleShape
                ) {
                    Image(
                        painter = painterResource(R.drawable.link_break),
                        contentDescription = "break",
                        modifier = Modifier
                            .size(34.dp)
                            .padding(8.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary)
                    )
                }

                Text(
                    "No movement events today",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold
                    ),
                    fontSize = 13.sp
                )

                Text(
                    "GeoWav will surface arrivals and exits when something changes.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = manrope
                    ),
                    fontSize = 12.sp
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
    val accentColor = if (isEnter) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.error
    }
    val containerColor = if (isDarkThemeEnabled) {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest
    }
    val eventLabel = if (isEnter) "Reached" else "Left"


    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.26f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.map_pin),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = accentColor
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    alert.title,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = manrope
                    )
                )
                Text(
                    relativeTime,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = manrope),
                    maxLines = 2
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    color = accentColor.copy(alpha = 0.10f),
                    contentColor = accentColor,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        eventLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                }

                Text(
                    alert.time,
                    color = MaterialTheme.colorScheme.outline,
                    fontFamily = manrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}


@Composable
fun ProfileCard(
    plan: UserPlan,
    avatar: String?,
    currentUser: User?,
    userName: String?,
    activeSharingCount: Int,
    modifier: Modifier = Modifier,
    isDarkThemeEnabled: Boolean
) {
    Log.i("USER", "home user: $currentUser")
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Welcome,",
                    fontFamily = manrope,
                    fontSize = 14.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                )

                Text(
                    text = userName ?: "",
                    fontFamily = manrope,
                    fontSize = 28.sp,
                    color = Color.Black,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                )

                Text(
                    text = when (activeSharingCount) {
                        0 -> "All quiet right now"
                        1 -> "1 person sharing live"
                        else -> "$activeSharingCount people sharing live"
                    },
                    fontFamily = manrope,
                    fontSize = 13.sp,
                    color = Color.Black.copy(alpha = 0.68f),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }


            val imageUrl =
                currentUser?.avatar?.takeIf { it.isNotBlank() }

            val context = LocalContext.current
            val imageLoader = ImageLoader.Builder(context)
                .components {
                    add(SvgDecoder.Factory())
                }
                .build()

            val badge = SubscriptionHelper.getPlanBadge(plan)

            Box() {

                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White)
                            .size(72.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!imageUrl.isNullOrBlank()) {
                            AvatarImage(
                                avatarUrl = imageUrl,
                                isUploading = false,
                                modifier = Modifier.size(72.dp)
                            )
                        } else {
//
                            Log.d("USER", "name: ${currentUser?.username}")

                            Text(
                                text = currentUser?.username?.take(1) ?: "",
                                color = Color.Black,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier

                            )
                        }
                    }

                }

//                AsyncImage(
//                    model = imageUrl,
//                    contentDescription = "User avatar",
//                    imageLoader = imageLoader,
//                    placeholder = painterResource(R.drawable.user),
//                    contentScale = ContentScale.Crop,
//                    modifier = Modifier.size(84.dp)
//                )
//                Box(
//                    modifier = Modifier
//                        .clip(CircleShape)
//                        .background(Color.White)
//                        .size(84.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    if (!imageUrl.isNullOrBlank()) {
//                        Log.d("USER", "avatar: async")
//                        AvatarImage(
//                            avatarUrl = imageUrl,
//                            isUploading = false,
//                            modifier = Modifier
//                                .size(84.dp)
//                                .clip(CircleShape)
//                                .background(Color.White)
//                        )
//                    } else {
//                        Text(
//                            text = currentUser?.username?.take(1) ?: "",
//                            color = Color.Black,
//                            fontSize = 42.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            modifier = Modifier
//
//                        )
//                    }
//                }
                badge?.let {
                    Icon(
                        painter = painterResource(badge),
                        contentDescription = "badge",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.BottomEnd)
                    )
                }


            }
        }
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
