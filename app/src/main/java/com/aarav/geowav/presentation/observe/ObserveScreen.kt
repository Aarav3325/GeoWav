package com.aarav.geowav.presentation.observe

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.core.utils.ViewerLocationState
import com.aarav.geowav.core.utils.formatTime
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.data.model.toLatLng
import com.aarav.geowav.presentation.components.AvatarImage
import com.aarav.geowav.presentation.components.MyAlertDialog
import com.aarav.geowav.presentation.home.HomeScreenVM
import com.aarav.geowav.presentation.home.ObserveLiveLocationCard
import com.aarav.geowav.presentation.theme.manrope
import com.aarav.geowav.presentation.theme.onSurfaceDark
import com.aarav.geowav.presentation.theme.onSurfaceLight
import com.aarav.geowav.presentation.theme.surfaceContainerHighDark
import com.aarav.geowav.presentation.theme.surfaceContainerHighLight
import com.aarav.geowav.presentation.timeline.SessionPreviewTopBar
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import androidx.core.net.toUri


@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObserveScreen(
    viewModel: HomeScreenVM,
    userLocation: Pair<Double, Double>?,
    back: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsState()
    val locations by viewModel.locations.collectAsState()


    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) {


        var showStopDialog by remember {
            mutableStateOf(false)
        }

        var showTray by remember {
            mutableStateOf(true)
        }

        MyAlertDialog(
            shouldShowDialog = showStopDialog,
            onDismissRequest = {
                showStopDialog = false
                back()
            },
            icon = R.drawable.new_logo,
            title = "Sharing Inactive",
            message = "Currently nobody is sharing their live location with you. You will be redirected to Home on clicking Return to Home button.",
            confirmButtonText = "Return to Home"

        ) {
            showStopDialog = false
            back()
        }

        val hasAnyLiveSharing = locations.values.any {
            it is ViewerLocationState.NormalSharing ||
                    it is ViewerLocationState.EmergencySharing
        }

        LaunchedEffect(hasAnyLiveSharing) {
            if (!hasAnyLiveSharing) {
                showStopDialog = true
            }
        }


        val emergencyUser = locations
            .entries
            .firstOrNull { it.value is ViewerLocationState.EmergencySharing }


        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {

            AnimatedVisibility(hasAnyLiveSharing) {
                ObserveLiveLocationCard(
                    viewModel, uiState, true,
                    showTray,
                    onHideClick = {
                        showTray = false
                    },
                    onShowTray = {
                        showTray = true
                    },
                    navigateToObserve = {},
                    Modifier.fillMaxSize(),
                    userLocation = userLocation
                )
            }

            SessionPreviewTopBar(
                screenTitle = "Observe Loved Ones",
                onBack = back,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 16.dp, top = 10.dp)
            )


        }
    }
}

@Composable
fun ViewerInfoSheetContent(
    viewerList: List<CircleMember>,
    locations: Map<String, ViewerLocationState>,
    onClick: (String) -> Unit,
    selectedUserLocationState: ViewerLocationState? = null,
    selectedUserDetails: CircleMember? = null,
    userLocation: Pair<Double, Double>? = null,
    onDismiss: () -> Unit
) {

    val isSelected = selectedUserDetails != null
            && selectedUserLocationState != null

    val titleText = when {
        viewerList.isEmpty() ->
            "No one"

        viewerList.size == 1 -> {
            viewerList.first().profileName
        }

        viewerList.size == 2 ->
            viewerList.joinToString { it.profileName }

        else ->
            "${viewerList[0].profileName}, ${viewerList[1].profileName} + ${viewerList.size - 2}"
    }

    AnimatedVisibility(
        !isSelected,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 260
            )
        ) + slideInHorizontally(
            animationSpec = tween(
                durationMillis = 320
            ),
            initialOffsetX = { -it / 12 }
        ),

        exit = fadeOut(
            animationSpec = tween(
                durationMillis = 180
            )
        ) + slideOutHorizontally(
            animationSpec = tween(
                durationMillis = 240
            ),
            targetOffsetX = { -it / 18 }
        )
    ) {
        LazyColumn(
            modifier = Modifier
                .wrapContentHeight()
                .animateContentSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "$titleText is sharing",
                    color = Color.White,
                    fontFamily = manrope,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            items(viewerList) { viewer ->
                val state = locations[viewer.id] is ViewerLocationState.EmergencySharing
                val index = viewerList.indexOf(viewer)
                val isLast = index == viewerList.size - 1
                ViewerInfoRow(
                    isLast,
                    viewer,
                    state,
                    locations[viewer.id],
                    onClick
                )
            }
        }
    }

    AnimatedVisibility(
        isSelected,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 280
            )
        ) + slideInHorizontally(
            animationSpec = tween(
                durationMillis = 340
            ),
            initialOffsetX = { it / 14 }
        ),

        exit = fadeOut(
            animationSpec = tween(
                durationMillis = 180
            )
        ) + slideOutHorizontally(
            animationSpec = tween(
                durationMillis = 240
            ),
            targetOffsetX = { it / 18 }
        )
    ) {
        ViewerDetailContent(
            modifier = Modifier.padding(horizontal = 12.dp),
            viewer = selectedUserDetails,
            locationState = selectedUserLocationState,
            userLocation = userLocation,
            onDismiss = onDismiss
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ViewerInfoRow(
    isLast: Boolean,
    conn: CircleMember,
    isEmergency: Boolean,
    viewerState: ViewerLocationState?,
    onClick: (String) -> Unit
) {

    val displayName = conn.alias?.takeIf { it.isNotBlank() } ?: conn.profileName
    val avatarUrl = conn.avatarUrl?.takeIf { it.isNotBlank() }
    val location = when (viewerState) {
        is ViewerLocationState.NormalSharing -> viewerState.location
        is ViewerLocationState.EmergencySharing -> viewerState.location
        else -> null
    }

    Column(
        modifier = Modifier
//            .background(Color(0xEE111820))
            .clickable {
                onClick(conn.id)
            }
            .padding(
                horizontal = 12.dp
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Box {

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUrl != null) {
                        AvatarImage(
                            avatarUrl = avatarUrl,
                            isUploading = false,
                            modifier = Modifier
                                .matchParentSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.onPrimary,
                                            MaterialTheme.colorScheme.inversePrimary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayName.take(1),
                                fontFamily = manrope,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (isEmergency)
                                MaterialTheme.colorScheme.error
                            else
                                Color(0xFF34C759)
                        )
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.surface,
                            CircleShape
                        )
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = manrope,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Last Updated: ${formatTime(location?.timestamp ?: System.currentTimeMillis())}",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = manrope,
                    color = Color.White.copy(alpha = 0.62f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

            }

            Surface(
                color = Color(0xFF34C759).copy(0.08f),
                shape = RoundedCornerShape(99.dp),
            ) {
                Text(
                    text = "Live",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = manrope,
                    color = Color(0xFF34C759).copy(alpha = 0.86f),
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (!isLast) {
            HorizontalDivider(
                Modifier
                    .background(Color.Transparent),
                thickness = 1.dp
            )
        }

    }
}

@Composable
fun CollapsedViewerTray(
    viewerInfo: List<CircleMember>,
    showDetail: () -> Unit,
    modifier: Modifier = Modifier
) {

    val titleText = when {
        viewerInfo.isEmpty() ->
            "No one"

        viewerInfo.size == 1 -> {
            viewerInfo.first().profileName
        }

        viewerInfo.size == 2 ->
            viewerInfo.joinToString { it.profileName }

        else ->
            "${viewerInfo[0].profileName}, ${viewerInfo[1].profileName} + ${viewerInfo.size - 2}"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(28.dp)
            )
            .clickable {
                showDetail()
            }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color(0xEE111820)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = manrope,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Viewing live locations",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = manrope,
                    color = Color.White.copy(alpha = 0.62f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

            }

            IconButton(
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = surfaceContainerHighLight,
                    contentColor = onSurfaceLight
                ), onClick = showDetail
            ) {
                Icon(
                    painter = painterResource(R.drawable.right_arrow),
                    contentDescription = "Expand",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun CollapsedViewerInfo(
    memberName: String,
    lastTimestamp: Long,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color(0xEE111820)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = memberName,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = manrope,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Last Updated: ${formatTime(lastTimestamp)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = manrope,
                    color = Color.White.copy(alpha = 0.62f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

            }

            IconButton(
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ), onClick = onDismiss
            ) {
                Icon(
                    painter = painterResource(R.drawable.clear),
                    contentDescription = "Close help",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun CollapsedViewerInfoV2(
    memberName: String,
    lastTimestamp: Long,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            Text(
                text = memberName,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = manrope,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Last Updated: ${formatTime(lastTimestamp)}",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = manrope,
                color = Color.White.copy(alpha = 0.62f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

        }

        IconButton(
            modifier = Modifier.size(36.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            ), onClick = onDismiss
        ) {
            Icon(
                painter = painterResource(R.drawable.clear),
                contentDescription = "Close help",
                modifier = Modifier.size(18.dp)
            )
        }
    }

}

@Composable
fun CompactActionMenu(
    resetCameraPosition: () -> Unit,
    changeMapType: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xCC111820),
        shape = RoundedCornerShape(99.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            IconButton(
                modifier = Modifier.size(42.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Color.White.copy(alpha = 0.88f)
                ),
                onClick = resetCameraPosition
            ) {
                Icon(
                    painter = painterResource(R.drawable.gps),
                    contentDescription = "reset camera position",
                    tint = Color.White.copy(alpha = 0.84f),
                    modifier = Modifier.size(21.dp)
                )
            }

            IconButton(
                modifier = Modifier.size(42.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Color.White.copy(alpha = 0.88f)
                ),
                onClick = {
                    changeMapType()
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.map_trifold),
                    contentDescription = "map",
                    tint = Color.White.copy(alpha = 0.84f),
                    modifier = Modifier.size(21.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ViewerDetailContent(
    modifier: Modifier = Modifier,
    viewer: CircleMember?,
    locationState: ViewerLocationState?,
    userLocation: Pair<Double, Double>? = null,
    onDismiss: () -> Unit
) {
    val isEmergency = locationState is ViewerLocationState.EmergencySharing

    val context = LocalContext.current

    var distanceBetween by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(locationState, userLocation) {

        if (userLocation == null) {
            distanceBetween = null
            return@LaunchedEffect
        }

        val current = when (locationState) {
            is ViewerLocationState.NormalSharing -> locationState.location
            is ViewerLocationState.EmergencySharing -> locationState.location
            else -> null
        }

        current?.let {
            val memberLatLng = current.toLatLng()
            val viewerLatLng = LatLng(userLocation.first, userLocation.second)

            val distance = SphericalUtil.computeDistanceBetween(
                viewerLatLng,
                memberLatLng
            )

            distanceBetween = if (distance >= 1000) {
                "${(distance / 1000).toInt()} km"
            } else {
                "${distance.toInt()} m"
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                viewer?.alias ?: viewer?.profileName ?: "",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.W900
            )

            IconButton(
                onClick = onDismiss,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = surfaceContainerHighDark,
                    contentColor = onSurfaceDark
                ),
            ) {
                Icon(
                    painter = painterResource(R.drawable.clear),
                    contentDescription = null
                )
            }
        }

        Text(
            if (isEmergency) "Emergency" else "Live",
            color = if (isEmergency) MaterialTheme.colorScheme.error else Color(0xFF34C759),
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .height(108.dp)
                    .weight(1f),
                shape = RoundedCornerShape(22.dp),
                color = surfaceContainerHighDark
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .padding(start = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFd7d978)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ruler),
                                contentDescription = "ruler",
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(6.dp),
                                tint = Color.White
                            )
                        }

                        distanceBetween?.let {
                            Text(
                                "${viewer?.alias ?: viewer?.profileName ?: ""} is $it away from you",
                                color = Color.White.copy(alpha = 0.84f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .height(108.dp)
                    .weight(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable {
                        val current = when (locationState) {
                            is ViewerLocationState.NormalSharing -> locationState.location
                            is ViewerLocationState.EmergencySharing -> locationState.location
                            else -> null
                        }


                        current?.let {
                            val uri = "google.navigation:q=${it.lat},${it.lng}".toUri()
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            intent.setPackage("com.google.android.apps.maps")
                            context.startActivity(intent)
                        }
                    },
                shape = RoundedCornerShape(22.dp),
                color = surfaceContainerHighDark
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .padding(start = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF5b95e1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.arrow_bend_direction),
                                contentDescription = "ruler",
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(6.dp),
                                tint = Color.White
                            )
                        }

                        Text(
                            "Directions",
                            color = Color.White.copy(alpha = 0.84f),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.W700
                        )
                    }
                }
            }
        }
    }
}
