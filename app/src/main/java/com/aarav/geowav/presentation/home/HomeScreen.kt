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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
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
import com.aarav.geowav.data.model.ActivityTransition
import com.aarav.geowav.data.model.CircleActivityItem
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.data.model.User
import com.aarav.geowav.data.model.UserPlan
import androidx.compose.runtime.saveable.rememberSaveable
import com.aarav.geowav.presentation.components.AvatarImage
import com.aarav.geowav.presentation.components.TrialOfferDialog
import com.aarav.geowav.presentation.components.AwarenessSnapshotCard
import com.aarav.geowav.presentation.components.IdentityAvatar
import com.aarav.geowav.presentation.components.InsightPreviewCard
import com.aarav.geowav.presentation.components.openAppDetailsSettings
import com.aarav.geowav.presentation.insights.PersonalInsightsViewModel
import com.aarav.geowav.presentation.navigation.NavRoute
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
    personalInsightsVM: PersonalInsightsViewModel,
    subscriptionViewModel: SubscriptionViewModel,
    navigateToSettings: () -> Unit,
    navigateToPaywall: () -> Unit,
    navigateToCircle: () -> Unit,
    navigateToTimeline: (String, String) -> Unit,
    navigateToActivity: () -> Unit,
    navigateToInsights: () -> Unit,
    modifier: Modifier = Modifier
) {

    val uiState by homeScreenVM.uiState.collectAsState()
    val awarenessSnapshotState by homeScreenVM.awarenessSnapshotUiState.collectAsState()
    val locations by homeScreenVM.locations.collectAsState()
    val personalInsightsState by personalInsightsVM.uiState.collectAsState()
    val activeSharingCount = locations.count { (_, state) ->
        state is ViewerLocationState.NormalSharing ||
                state is ViewerLocationState.EmergencySharing
    }

    val plan by subscriptionViewModel.userPlan.collectAsState()
    val paywallConfig by subscriptionViewModel.paywallConfig.collectAsState()
    val hasShownPromoPopup by subscriptionViewModel.hasShownPromoPopup.collectAsState()


    val hideTopBar = uiState.currentUser == null


    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
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

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 4 }
    )

    // Switch colors after scrolling
    val useDarkIcons by remember {
        derivedStateOf { scroll.value > 60 }
    }

    val showLocationText by remember {
        derivedStateOf { scroll.value > 150 }
    }

    // Animate colors smoothly
    val textColor by animateColorAsState(
        targetValue =

            if (hideTopBar) Color.Transparent
            else if (useDarkIcons) {
               Color.White
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
                Color(0xFF5654A2)
            else
                Color.Transparent,
        animationSpec = tween(durationMillis = 500),
        label = "BackgroundColorAnimation"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        topBar = {
                ProfileCardV2(
                    currentUser = uiState.currentUser,
                    userName = uiState.username,
                    contentColor = textColor,
                    navigateToPaywall,
                    navigateToSettings,
                    showLocationText,
                    locationAddress = awarenessSnapshotState.currentPlace,
                    modifier = Modifier
                        .background(backgroundColor)
                        .windowInsetsPadding(TopAppBarDefaults.windowInsets)
                        .padding(TopAppBarDefaults.ContentPadding)
                        .height(TopAppBarDefaults.TopAppBarExpandedHeight)
                )
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
                            painter = painterResource(R.drawable.haze),
                            contentDescription = "bg",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .fillMaxWidth()
                                .matchParentSize()
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

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(
                                modifier = Modifier
                                    .windowInsetsPadding(TopAppBarDefaults.windowInsets)
                                    .height(TopAppBarDefaults.TopAppBarExpandedHeight)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxWidth()
                            ) { page ->
                                when (page) {
                                    0 -> AwarenessSnapshotCard(
                                        uiState = awarenessSnapshotState,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    1 -> InsightPreviewCard(
                                        insight = personalInsightsState.mostVisitedPlaceInsight,
                                        scope = personalInsightsState.mostVisitedPlaceInsight?.scope,
                                        heroText = "MOST VISITED PLACE",
                                        ctaText = "See Insights",
                                        onClick = navigateToInsights,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    2 -> InsightPreviewCard(
                                        insight = personalInsightsState.averageVisitDurationInsight,
                                        scope = personalInsightsState.averageVisitDurationInsight?.scope,
                                        heroText = "AVERAGE TIME SPENT",
                                        ctaText = "See Insights",
                                        onClick = navigateToInsights,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    3 -> InsightPreviewCard(
                                        insight = personalInsightsState.weeklyAwarenessSummaryInsight,
                                        scope = personalInsightsState.weeklyAwarenessSummaryInsight?.scope,
                                        heroText = "WEEKLY SUMMARY",
                                        ctaText = "See Insights",
                                        onClick = navigateToInsights,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                repeat(pagerState.pageCount) { index ->
                                    val isSelected = pagerState.currentPage == index
                                    val width = if (isSelected) 16.dp else 6.dp
                                    val alpha = if (isSelected) 1f else 0.4f
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 3.dp)
                                            .size(width = width, height = 6.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = alpha),
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                        }
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

                        val context = LocalContext.current
                        AnimatedVisibility(uiState.permissionState.shouldShowSetupCard) {
                            LocationSetupReminderCard(
                                permissionState = uiState.permissionState,
                                onReviewClick = { openAppDetailsSettings(context) },
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }


                        AnimatedVisibility(hasAnyLiveSharing) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp)
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
                                        .height(190.dp)
                                )
                            }
                        }


                        ConnectionsList(
                            title = "People",
                            connections = uiState.lovedOnes,
                            isLoading = uiState.isLovedOnesLoading,
                            error = uiState.lovedOnesError,
                            locationStates = locations,
                            onManage = navigateToCircle,
                            navigateToTimeline = navigateToTimeline
                        )

                        ActiveZonesSection(
                            zones = uiState.placesList,
                            isLoading = uiState.isPlacesLoading,
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
                                .padding(top = 10.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Awareness",
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = manrope,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                fontSize = 15.sp,
                            )

                            TextButton(onClick = navigateToActivity) {
                                Text(
                                    "History",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontFamily = manrope,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        RecentAlertsList(
                            activities = uiState.awarenessItems,
                            isLoading = uiState.isAwarenessLoading,
                            error = uiState.awarenessError,
                            currentUserId = uiState.currentUser?.userId ?: homeScreenVM.viewerId,
                            isDarkThemeEnabled = isDarkThemeEnabled
                        )


                        Spacer(modifier = Modifier.height(142.dp))
                    }
                }
            }
        }
    }

    TrialOfferDialog(
        showDialog = paywallConfig.launchOfferEnabled && plan != UserPlan.PRO && !hasShownPromoPopup,
        config = paywallConfig,
        onDismiss = { subscriptionViewModel.setPromoPopupShown(true) },
        onClaim = {
            subscriptionViewModel.setPromoPopupShown(true)
            navigateToPaywall()
        }
    )
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConnectionsList(
    title: String,
    connections: List<CircleMember>,
    isLoading: Boolean = false,
    error: String? = null,
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
                fontSize = 15.sp,
                modifier = Modifier.weight(1.0f)
            )

            TextButton(onClick = onManage) {
                Text(
                    "Manage",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = manrope,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ContainedLoadingIndicator()
                }
            } else if (error != null) {
                val isNoInternet = error.contains("internet", ignoreCase = true) ||
                                   error.contains("connection", ignoreCase = true)
                val msg = if (isNoInternet) "Couldn't load circle. Check connection." else "Circle details currently unavailable."
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = manrope
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else if (connections.isEmpty()) {
                val infiniteTransition = rememberInfiniteTransition()
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.92f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1800, easing = EaseInOut),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .scale(scale)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.user),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "No connections added",
                            fontFamily = manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Add friends or family to see their real-time locations here.",
                            fontFamily = manrope,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onManage,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.add),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Add Connections",
                                fontFamily = manrope,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
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

private data class ConnectionStatusDetails(
    val label: String,
    val detail: String,
    val color: Color,
    val isSharing: Boolean
)

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

    val status = when (locationState) {
        is ViewerLocationState.EmergencySharing ->
            ConnectionStatusDetails(
                label = "Emergency",
                detail = "Live emergency sharing",
                color = MaterialTheme.colorScheme.error,
                isSharing = true
            )

        is ViewerLocationState.NormalSharing ->
            ConnectionStatusDetails(
                label = "Live now",
                detail = "Location is updating",
                color = MaterialTheme.colorScheme.primary,
                isSharing = true
            )

        else ->
            ConnectionStatusDetails(
                label = "Quiet",
                detail = "Not sharing right now",
                color = MaterialTheme.colorScheme.outline,
                isSharing = false
            )
    }

    val statusText = status.label
    val statusDetail = status.detail
    val statusColor = status.color
    val isSharing = status.isSharing

    Card(
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(
            1.dp,
            if (emergencyState != null) {
                statusColor.copy(alpha = 0.42f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
            }
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (emergencyState != null) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.18f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        ),
        shape = RoundedCornerShape(14.dp)
    ) {

        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                IdentityAvatar(
                    avatarUrl = member.avatarUrl,
                    displayName = name,
                    backgroundColor = statusColor.copy(alpha = 0.12f),
                    contentColor = statusColor,
                    borderColor = statusColor.copy(alpha = 0.24f),
                    modifier = Modifier
                        .size(46.dp)
                )

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
                        text = statusDetail,
                        fontFamily = manrope,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(25),
                    color = statusColor.copy(alpha = if (emergencyState != null) 0.18f else 0.10f)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 4.dp
                        ),
                        fontFamily = manrope,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }

                Spacer(Modifier.width(10.dp))

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
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
                        tint = MaterialTheme.colorScheme.tertiary,
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ActiveZonesSection(
    zones: List<Place>,
    isLoading: Boolean = false,
    onZoneClick: (com.aarav.geowav.data.model.Place) -> Unit,
    onViewAllClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(top = 2.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Places",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = manrope,
                    fontWeight = FontWeight.SemiBold
                )
            )

            TextButton(onClick = {
                onViewAllClick()
            }) {
                Text(
                    "View",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = manrope,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ContainedLoadingIndicator()
                }
            } else if (zones.isNotEmpty()) {
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
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(14.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.map_pin),
                    contentDescription = "zone",
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    zone.customName.ifEmpty { zone.placeName },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = manrope,
                        lineHeight = 17.sp
                    )
                )

                Text(
                    "${zone.radius.toInt()}m awareness radius",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = manrope
                    ),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.width(6.dp))

            Surface(
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f),
                contentColor = MaterialTheme.colorScheme.tertiary,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    "Active",
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontFamily = manrope,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.tertiary
                )
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RecentAlertsList(
    activities: List<CircleActivityItem>,
    isLoading: Boolean = false,
    error: String? = null,
    currentUserId: String,
    isDarkThemeEnabled: Boolean
) {


    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                ContainedLoadingIndicator()
            }
        } else if (error != null) {
            val isNoInternet = error.contains("internet", ignoreCase = true) ||
                               error.contains("connection", ignoreCase = true)
            val msg = if (isNoInternet) "Couldn't load updates. Check connection." else "Activity log currently unavailable."
            Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = manrope
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else if (activities.isEmpty()) {
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
                    "No recent movement updates",
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
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
            }
        } else {
            activities.forEach { activity ->
                AwarenessItem(
                    activity = activity,
                    currentUserId = currentUserId,
                    isDarkThemeEnabled = isDarkThemeEnabled
                )
            }
        }
    }
}

@Composable
fun AwarenessItem(
    activity: CircleActivityItem,
    currentUserId: String,
    isDarkThemeEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val isArrival = activity.normalizedTransitionType == ActivityTransition.ARRIVED.name
    val actorLabel = if (activity.actorId == currentUserId) "You" else activity.actorName
    val title = if (isArrival) {
        "$actorLabel arrived at ${activity.placeName}"
    } else {
        "$actorLabel left ${activity.placeName}"
    }
    val relativeTime = buildRelativeSubtitle(
        type = if (isArrival) "enter" else "exit",
        timestamp = activity.timestamp
    )
    val accentColor = if (isArrival) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.error
    }
    val containerColor = if (isDarkThemeEnabled) {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth(),
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

            IdentityAvatar(
                avatarUrl = activity.actorAvatar,
                displayName = activity.actorName,
                backgroundColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                contentColor = MaterialTheme.colorScheme.outline,
                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f),
                modifier = Modifier
                    .size(46.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    title,
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
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = manrope)
                )
            }

            Surface(
                color = accentColor.copy(alpha = 0.10f),
                contentColor = accentColor,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    if (isArrival) "Arrived" else "Left",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    fontFamily = manrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun AlertItem(
    alert: com.aarav.geowav.data.model.GeoAlert,
    isDarkThemeEnabled: Boolean,
    modifier: Modifier = Modifier
) {

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
        modifier = modifier
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

@Preview(showBackground = true)
@Composable
fun ProfileCardV2(
    currentUser: User?,
    userName: String?,
    contentColor: Color,
    navigateToPaywall: () -> Unit,
    navigateToProfile: () -> Unit,
    showLocationText: Boolean,
    locationAddress: String?,
    modifier: Modifier = Modifier
) {
    val imageUrl =
        currentUser?.avatar?.takeIf { it.isNotBlank() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f),
        ) {

            val address = locationAddress.takeIf {
                locationAddress != "Location access needed"
            } ?: "GeoWav"

            AnimatedVisibility(showLocationText) {
                Text(
                    text = address,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = contentColor,
                        fontSize = 20.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = manrope,
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedVisibility(!showLocationText) {
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    if(!userName.isNullOrEmpty()) {
                        Text(
                            text = buildAnnotatedString {
                                append("Hello ")
                                withStyle(
                                    SpanStyle(
                                        color = contentColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                ) {
                                    append("$userName,")
                                }
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = contentColor
                        )
                    }

                    Text(
                        text = "GeoWav",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = contentColor
                        ),
                        fontFamily = manrope,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        IconButton(
            onClick = navigateToPaywall
        ) {
            Image(
                painter = painterResource(R.drawable.payment),
                contentDescription = "payment",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(contentColor)
            )
        }

        Spacer(Modifier.width(8.dp))

        IdentityAvatar(
            avatarUrl = imageUrl,
            displayName = userName ?: "",
            backgroundColor = MaterialTheme.colorScheme.outline,
            contentColor = contentColor,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f),
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable {
                    navigateToProfile()
                }
        )
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
