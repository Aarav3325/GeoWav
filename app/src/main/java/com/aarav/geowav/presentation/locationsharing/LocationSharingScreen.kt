package com.aarav.geowav.presentation.locationsharing

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Brush
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
import com.aarav.geowav.presentation.components.EmergencyShareDialog
import com.aarav.geowav.presentation.theme.manrope
import com.aarav.geowav.presentation.theme.sora
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSharingScreen(
    viewModel: LocationSharingVM
) {

    val uiState by viewModel.uiState.collectAsState()


    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadLovedOnes()
        viewModel.loadLocationPermission()

    }

//    LaunchedEffect(uiState.sharingState) {
//        viewModel.refreshState()
//    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LiveLocationUiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
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
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                    )
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LocationSharingContent(
            modifier = Modifier.padding(padding),
            uiState,
            viewModel::onViewerToggle,
            onStartSharing = viewModel::startSharing,
            onStopSharing = viewModel::stopLiveLocationSharing
        )
    }
}

@Composable
fun LocationSharingContent(
    modifier: Modifier = Modifier,
    locationUiState: LiveLocationUiState,
    onToggleChange: (String, Boolean) -> Unit,
    onStartSharing: () -> Unit,
    onStopSharing: () -> Unit
) {

    var showEmergencyDialog by remember {
        mutableStateOf(false)
    }

    var emergencyMode by remember {
        mutableStateOf(false)
    }

    EmergencyShareDialog(
        showEmergencyDialog,
        onConfirm = {
            emergencyMode = true
            showEmergencyDialog = false
        },
        onDismiss = { showEmergencyDialog = false }
    )

    Column(
        modifier = modifier
    ) {
        LazyColumn(
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
                    locationUiState.sharingState,
                    onStartSharing,
                    onStopSharing
                )
            }

            item {
                MapPreviewCard()
            }

            item {
                LovedOnesCard(
                    locationUiState.lovedOnes,
                    locationUiState.sharingState,
                    locationUiState.selectedViewerIds,
                    locationUiState.updatingViewerId,
                    onToggleChange
                )
            }

            item {
                EmergencyShareButton(!emergencyMode) {
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

@Preview
@Composable
fun StatusCard(
    liveLocationState: LiveLocationState,
    onStart: () -> Unit,
    onStop: () -> Unit
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


    val (title, subtitle, color) = when (liveLocationState) {
        LiveLocationState.NotSharing -> {
            Triple(
                "Not sharing location",
                "Tap to start sharing",
                MaterialTheme.colorScheme.inversePrimary
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
                "Sharing live location", "Sharing with: ${liveLocationState.visibleCount} people",
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp)
        ) {
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
                        tint = color,
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

            when (liveLocationState) {
                is LiveLocationState.Sharing -> {
                    StopSharingButton(onStop)
                }

                LiveLocationState.NotSharing -> {
                    StartSharingButton(onStart)
                }

                else -> {}
            }
        }
    }
}

@Composable
fun MapPreviewCard() {
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

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .size(200.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                "Google Map Preview",
                fontFamily = sora,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .padding(vertical = 6.dp, horizontal = 12.dp)
                    .align(Alignment.Center)
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 8.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Text(
                    "Last updated 5s ago",
                    fontFamily = sora,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp)
                )
            }
        }
    }
}

@Composable
fun LovedOnesCard(
    lovedOnesList: List<CircleMember>,
    locationState: LiveLocationState,
    selectedViewerIds: Set<String>,
    updatingViewerId: String? = null,
    onToggleChange: (String, Boolean) -> Unit
) {

    val toggleEnabled =
        locationState is LiveLocationState.NotSharing ||
                locationState is LiveLocationState.Sharing

    val lovedOnes = listOf(
        LovedOneUi(
            "1",
            "Mom",
            true
        ),
        LovedOneUi(
            "2",
            "Dad",
            true
        ),
        LovedOneUi(
            "3",
            "Brother",
            true
        ),
    )

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(24.dp)
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

                TextButton(onClick = { expanded = !expanded }) {
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
                        "You are not sharing your live location with anyone currently",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Normal,
                            fontFamily = manrope,
                            lineHeight = 20.sp
                        ),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp)
                    )
                }

                else -> {}
            }

            if (!expanded) {
                when (locationState) {
                    is LiveLocationState.Sharing -> {
                        CollapsedLovedOnes(
                            lovedOnesList,
                            selectedViewerIds
                        )
                    }

                    else -> {}
                }
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
    selectedViewerIds: Set<String>
) {

    val selected = lovedOnes.filter { it.id in selectedViewerIds }


//    val text = when {
//        selected.isEmpty() ->
//            "No one"
//
//        selected.size <= 2 ->
//            selected.joinToString { it.al }
//
//        else ->
//            "${selected[0].name}, ${selected[1].name} +${selected.size - 2}"
//    }


    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        if (selected.size <= 2) {
            selected.forEach { connection ->
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.onPrimary,
                                    MaterialTheme.colorScheme.inversePrimary
                                )
                            )
                        )
                ) {
                    Text(
                        connection.alias?.take(1) ?: connection.profileName.take(1),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = manrope
                        ),
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        } else {
            selected.take(2).forEach {

                    connection ->
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.onPrimary,
                                    MaterialTheme.colorScheme.inversePrimary
                                )
                            )
                        )
                ) {
                    Text(
                        connection.alias?.take(1) ?: connection.profileName.take(1),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = manrope
                        ),
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                MaterialTheme.colorScheme.onPrimary,
                                MaterialTheme.colorScheme.inversePrimary
                            )
                        )
                    )
            ) {
                Text(
                    "+${selected.size - 2}",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = manrope
                    ),
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

}

data class LovedOneUi(
    val id: String,
    val name: String,
    val selected: Boolean
)

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
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            MaterialTheme.colorScheme.onPrimary,
                            MaterialTheme.colorScheme.inversePrimary
                        )
                    )
                )
        ) {
            Text(
                connection.alias?.take(1) ?: connection.profileName.take(1) ?: "",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = manrope
                ),
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            connection.alias ?: connection.profileName,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = manrope
            ),
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )

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
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 36.dp)
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.5.dp,
            if (enabled) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.outline
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (enabled)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.outline,
            contentColor =
                MaterialTheme.colorScheme.onError
        )
    ) {
        Icon(
            painter = painterResource(R.drawable.emergency),
            contentDescription = null
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Emergency Share",
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
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            "Start Sharing",
            fontFamily = manrope,
            fontWeight = FontWeight.SemiBold
        )
    }
}

