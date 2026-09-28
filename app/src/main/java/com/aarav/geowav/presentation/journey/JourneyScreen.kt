package com.aarav.geowav.presentation.journey

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.data.model.DestinationLocation
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.data.model.SharingSession
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.presentation.components.IdentityAvatar
import com.aarav.geowav.presentation.theme.manrope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyScreen(
    viewModel: JourneyViewModel,
    userPlan: UserPlan = UserPlan.FREE,
    onBack: () -> Unit,
    onNavigateToMapPicker: () -> Unit,
    onNavigateToObserve: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is JourneyUiEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                JourneyUiEvent.JourneyStarted -> {}
                JourneyUiEvent.JourneyCompleted -> {}
                JourneyUiEvent.JourneyCancelled -> onBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (uiState.step) {
                            JourneyStep.DESTINATION_SELECTION -> "I'm On My Way"
                            JourneyStep.MEMBER_SELECTION -> "Who Should Know?"
                            JourneyStep.CONFIRMATION -> "Confirm Journey"
                            JourneyStep.ACTIVE_JOURNEY -> "Active Journey"
                            JourneyStep.ARRIVED -> "Journey Complete"
                            JourneyStep.NOT_REACHED -> "Journey Status"
                        },
                        fontFamily = manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when (uiState.step) {
                            JourneyStep.DESTINATION_SELECTION -> onBack()
                            JourneyStep.MEMBER_SELECTION -> viewModel.backToStep(JourneyStep.DESTINATION_SELECTION)
                            JourneyStep.CONFIRMATION -> viewModel.backToStep(JourneyStep.MEMBER_SELECTION)
                            JourneyStep.ACTIVE_JOURNEY -> onBack()
                            JourneyStep.ARRIVED -> onBack()
                            JourneyStep.NOT_REACHED -> viewModel.backToStep(JourneyStep.ACTIVE_JOURNEY)
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            if (uiState.step == JourneyStep.DESTINATION_SELECTION ||
                uiState.step == JourneyStep.MEMBER_SELECTION ||
                uiState.step == JourneyStep.CONFIRMATION
            ) {
                JourneyStepHeader(step = uiState.step)
                Spacer(modifier = Modifier.height(14.dp))
            }

            when (uiState.step) {
                JourneyStep.DESTINATION_SELECTION -> DestinationSelectionContent(
                    places = uiState.savedPlaces,
                    userLocation = uiState.userLocation,
                    isLoading = uiState.isLoadingPlaces,
                    onSelectPlace = { viewModel.selectPlace(it) },
                    onSelectCustomDestination = { dest -> viewModel.selectCustomDestination(dest) },
                    onNavigateToMapPicker = onNavigateToMapPicker
                )

                JourneyStep.MEMBER_SELECTION -> MemberSelectionContent(
                    members = uiState.circleMembers,
                    selectedIds = uiState.selectedMemberIds,
                    isLoading = uiState.isLoadingMembers,
                    onToggleMember = { viewModel.toggleMemberSelection(it) },
                    onSelectAll = {
                        val allIds = uiState.circleMembers.map { m -> m.id }.toSet()
                        allIds.forEach { id -> viewModel.toggleMemberSelection(id) }
                    },
                    onContinue = { viewModel.proceedToConfirmation() }
                )

                JourneyStep.CONFIRMATION -> ConfirmationContent(
                    destinationName = uiState.customDestination?.name ?: "Selected Location",
                    destinationAddress = uiState.customDestination?.address ?: "",
                    destinationLat = uiState.customDestination?.latitude ?: 0.0,
                    destinationLng = uiState.customDestination?.longitude ?: 0.0,
                    selectedMembers = uiState.circleMembers.filter { it.id in uiState.selectedMemberIds },
                    isLoading = uiState.isActionLoading,
                    onStartJourney = { viewModel.startJourney(userPlan) },
                    onCancel = { viewModel.backToStep(JourneyStep.DESTINATION_SELECTION) }
                )

                JourneyStep.ACTIVE_JOURNEY -> ActiveJourneyContent(
                    destinationName = uiState.customDestination?.name ?: uiState.activeSession?.destinationLocation?.name ?: "Destination",
                    selectedMembers = uiState.circleMembers.filter { it.id in uiState.selectedMemberIds || it.id in (uiState.activeSession?.sharedWith ?: emptyList()) },
                    distanceMeters = uiState.distanceToDestinationMeters,
                    activeSession = uiState.activeSession,
                    onEndJourney = { viewModel.endJourneyManually() }
                )

                JourneyStep.ARRIVED -> ArrivedContent(
                    destinationName = uiState.customDestination?.name ?: "Destination",
                    onDone = onBack
                )

                JourneyStep.NOT_REACHED -> NotReachedContent(
                    destinationName = uiState.customDestination?.name ?: "Destination",
                    onContinue = { viewModel.continueJourney() },
                    onChangeDestination = { viewModel.changeDestination() },
                    onEndJourney = { viewModel.endJourneyManually() }
                )
            }
        }
    }
}

@Composable
private fun JourneyStepHeader(step: JourneyStep) {
    val stepNumber = when (step) {
        JourneyStep.DESTINATION_SELECTION -> 1
        JourneyStep.MEMBER_SELECTION -> 2
        JourneyStep.CONFIRMATION -> 3
        else -> 3
    }
    val stepLabel = when (step) {
        JourneyStep.DESTINATION_SELECTION -> "Step 1 of 3: Destination"
        JourneyStep.MEMBER_SELECTION -> "Step 2 of 3: People"
        JourneyStep.CONFIRMATION -> "Step 3 of 3: Confirm"
        else -> "Step 3 of 3"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stepLabel,
            fontFamily = manrope,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..3).forEach { index ->
                val isActive = index <= stepNumber
                val color by animateColorAsState(
                    targetValue = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                    label = "stepColor"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

@Composable
private fun DestinationSelectionContent(
    places: List<Place>,
    userLocation: LatLng?,
    isLoading: Boolean,
    onSelectPlace: (Place) -> Unit,
    onSelectCustomDestination: (DestinationLocation) -> Unit,
    onNavigateToMapPicker: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredPlaces = remember(searchQuery, places) {
        if (searchQuery.isBlank()) places
        else places.filter {
            it.placeName.contains(searchQuery, ignoreCase = true) ||
                    it.customName.contains(searchQuery, ignoreCase = true) ||
                    (it.address?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    val defaultLatLng = remember(userLocation, places) {
        userLocation ?: places.firstOrNull()?.let { LatLng(it.latitude, it.longitude) } ?: LatLng(37.7749, -122.4194)
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLatLng, 15f)
    }

    LaunchedEffect(userLocation) {
        userLocation?.let { loc ->
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(loc, 15f),
                1000
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = "Search from your saved places...",
                    fontFamily = manrope,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Options List (Top Half)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row for "Choose on map"
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToMapPicker() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.map_trifold),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Choose on map",
                            fontFamily = manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.caret_right_fill),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else if (filteredPlaces.isEmpty()) {
                item {
                    Text(
                        text = "No places found matching search.",
                        fontFamily = manrope,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(filteredPlaces) { place ->
                    PlainPlaceRow(place = place, onClick = { onSelectPlace(place) })
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Live Interactive Location Map Preview (Lower Half)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable { onNavigateToMapPicker() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = true),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        mapToolbarEnabled = false,
                        compassEnabled = false,
                        scrollGesturesEnabled = true,
                        zoomGesturesEnabled = true,
                        tiltGesturesEnabled = false,
                        rotationGesturesEnabled = true
                    ),
                    onMapClick = { onNavigateToMapPicker() }
                ) {
                    places.forEach { place ->
                        Marker(
                            state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                            title = place.customName.ifEmpty { place.placeName }
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(6.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "YOUR CURRENT LOCATION",
                            fontFamily = manrope,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .clickable { onNavigateToMapPicker() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.map_trifold),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tap to search map or pick pin",
                            fontFamily = manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlainPlaceRow(place: Place, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.map_pin),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place.customName.ifEmpty { place.placeName },
                    fontFamily = manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val addr = place.address.orEmpty()
                if (addr.isNotEmpty()) {
                    Text(
                        text = addr,
                        fontFamily = manrope,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                painter = painterResource(id = R.drawable.caret_right_fill),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun MemberSelectionContent(
    members: List<CircleMember>,
    selectedIds: Set<String>,
    isLoading: Boolean,
    onToggleMember: (String) -> Unit,
    onSelectAll: () -> Unit,
    onContinue: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Who should know?",
                    fontFamily = manrope,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Circle members notified upon departure and arrival.",
                    fontFamily = manrope,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (members.isNotEmpty()) {
                TextButton(onClick = onSelectAll) {
                    Text(
                        text = if (selectedIds.size == members.size) "Deselect" else "Select All",
                        fontFamily = manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (members.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No circle members found. Add members in Circle screen.",
                    fontFamily = manrope,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(members) { member ->
                    val isSelected = selectedIds.contains(member.id)
                    DistinctMemberCard(
                        member = member,
                        isSelected = isSelected,
                        onToggle = { onToggleMember(member.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onContinue,
            enabled = selectedIds.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            val countText = if (selectedIds.isNotEmpty()) " (${selectedIds.size})" else ""
            Text(
                text = "Continue$countText",
                fontFamily = manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun DistinctMemberCard(
    member: CircleMember,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val cardBorder = if (isSelected) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }

    val containerColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    else
        MaterialTheme.colorScheme.surfaceContainerHigh

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onToggle,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val primaryName = member.profileName.orEmpty().ifBlank {
                member.alias.orEmpty().ifBlank { member.receiverEmail.orEmpty() }
            }
            val subtitleText = when {
                member.profileName.orEmpty().isNotBlank() && member.alias.orEmpty().isNotBlank() &&
                        !member.alias.orEmpty().equals(member.profileName.orEmpty(), ignoreCase = true) -> member.alias.orEmpty()
                member.profileName.orEmpty().isNotBlank() && member.receiverEmail.orEmpty().isNotBlank() &&
                        !member.receiverEmail.orEmpty().equals(member.profileName.orEmpty(), ignoreCase = true) -> member.receiverEmail.orEmpty()
                else -> null
            }

            IdentityAvatar(
                avatarUrl = member.avatarUrl,
                displayName = primaryName,
                backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                borderColor = if (isSelected) MaterialTheme.colorScheme.primary else null,
                modifier = Modifier.size(42.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = primaryName,
                    fontFamily = manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                subtitleText?.let { sub ->
                    Text(
                        text = sub,
                        fontFamily = manrope,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Surface(
                shape = CircleShape,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isSelected) {
                        Icon(
                            painter = painterResource(id = R.drawable.check),
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmationContent(
    destinationName: String,
    destinationAddress: String,
    destinationLat: Double,
    destinationLng: Double,
    selectedMembers: List<CircleMember>,
    isLoading: Boolean,
    onStartJourney: () -> Unit,
    onCancel: () -> Unit
) {
    val destLatLng = remember(destinationLat, destinationLng) {
        if (destinationLat != 0.0 && destinationLng != 0.0) LatLng(destinationLat, destinationLng)
        else LatLng(37.7749, -122.4194)
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(destLatLng, 13f)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // Route Snapshot Mini-Map (Top Half)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = true),
                        uiSettings = MapUiSettings(zoomControlsEnabled = false)
                    ) {
                        Marker(
                            state = MarkerState(position = destLatLng),
                            title = destinationName
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "ROUTE SNAPSHOT",
                            fontFamily = manrope,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = destinationName,
                    fontFamily = manrope,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (destinationAddress.isNotEmpty()) {
                    Text(
                        text = destinationAddress,
                        fontFamily = manrope,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notified Members Overlapping Avatar Stack
            Text(
                text = "NOTIFYING MEMBERS",
                fontFamily = manrope,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OverlappingAvatarStack(members = selectedMembers)
                Spacer(modifier = Modifier.width(12.dp))
                val memberNames = selectedMembers.take(2).joinToString(", ") { m ->
                    m.profileName.orEmpty().ifEmpty { m.receiverEmail.orEmpty() }
                }
                val extraCount = if (selectedMembers.size > 2) " +${selectedMembers.size - 2} more" else ""
                Text(
                    text = "$memberNames$extraCount",
                    fontFamily = manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lightweight inline caption (not boxed card)
            Text(
                text = "Live location updates will stop automatically upon arrival.",
                fontFamily = manrope,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }

        // Action Buttons at bottom
        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            Button(
                onClick = onStartJourney,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Start Journey",
                        fontFamily = manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Change Details",
                    fontFamily = manrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun OverlappingAvatarStack(members: List<CircleMember>) {
    val displayMembers = members.take(4)
    Row(horizontalArrangement = Arrangement.spacedBy((-10).dp)) {
        displayMembers.forEachIndexed { index, member ->
            val profileName = member.profileName.orEmpty().ifEmpty { member.receiverEmail.orEmpty() }
            IdentityAvatar(
                avatarUrl = member.avatarUrl,
                displayName = profileName,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                borderColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
private fun ActiveJourneyContent(
    destinationName: String,
    selectedMembers: List<CircleMember>,
    distanceMeters: Double?,
    activeSession: SharingSession?,
    onEndJourney: () -> Unit
) {
    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val startedAt = activeSession?.startedAt ?: 0L
    val elapsedMillis = if (startedAt > 0L) (currentTimeMillis - startedAt).coerceAtLeast(0L) else 0L
    val elapsedMins = (elapsedMillis / 60000L).toInt()
    val elapsedSecs = ((elapsedMillis % 60000L) / 1000L).toInt()
    val elapsedText = if (startedAt > 0L) {
        if (elapsedMins > 0) "${elapsedMins}m ${elapsedSecs}s" else "${elapsedSecs}s"
    } else {
        "Active"
    }

    val lastTimestamp = activeSession?.timestamp ?: 0L
    val syncDiffMillis = if (lastTimestamp > 0L) (currentTimeMillis - lastTimestamp).coerceAtLeast(0L) else 0L
    val syncSecs = (syncDiffMillis / 1000L).toInt()
    val syncText = when {
        lastTimestamp <= 0L -> "Live location active"
        syncSecs < 10 -> "Updated just now"
        syncSecs < 60 -> "Updated ${syncSecs}s ago"
        else -> "Updated ${syncSecs / 60}m ago"
    }

    val startTimeText = if (startedAt > 0L) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(startedAt))
    } else {
        "Just now"
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // 1. Status Card with real elapsed time & real last-sync text
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "JOURNEY IN PROGRESS",
                                fontFamily = manrope,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.8.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Sharing for $elapsedText",
                                fontFamily = manrope,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = destinationName,
                        fontFamily = manrope,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$syncText · Real-time location active",
                        fontFamily = manrope,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (distanceMeters != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val distKm = distanceMeters / 1000.0
                        val distText = if (distKm >= 1.0) "%.1f km remaining".format(distKm) else "%d m remaining".format(distanceMeters.toInt())
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = distText,
                                fontFamily = manrope,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Recipient Avatar Chip Row + Status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OverlappingAvatarStack(members = selectedMembers)
                            Spacer(modifier = Modifier.width(10.dp))
                            val memberNames = selectedMembers.take(2).joinToString(", ") { m ->
                                m.profileName.orEmpty().ifEmpty { m.receiverEmail.orEmpty() }
                            }
                            val extraCount = if (selectedMembers.size > 2) " +${selectedMembers.size - 2}" else ""
                            Column {
                                Text(
                                    text = if (selectedMembers.isNotEmpty()) memberNames + extraCount else "Circle Members",
                                    fontFamily = manrope,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Notified upon arrival",
                                    fontFamily = manrope,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "Notified",
                                fontFamily = manrope,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 2. Real Data Live Activity Feed Timeline
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "LIVE ACTIVITY FEED",
                        fontFamily = manrope,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    ActivityTimelineRow(
                        dotColor = MaterialTheme.colorScheme.primary,
                        title = "Last location sync",
                        timestamp = syncText,
                        isFirst = true,
                        isLast = false
                    )
                    ActivityTimelineRow(
                        dotColor = MaterialTheme.colorScheme.secondary,
                        title = "Circle members notified",
                        timestamp = startTimeText,
                        isFirst = false,
                        isLast = false
                    )
                    ActivityTimelineRow(
                        dotColor = MaterialTheme.colorScheme.outline,
                        title = "Journey started",
                        timestamp = startTimeText,
                        isFirst = false,
                        isLast = true
                    )
                }
            }
        }

        // 3. Sole Unambiguous CTA
        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            Button(
                onClick = onEndJourney,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(
                    text = "End Journey",
                    fontFamily = manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ActivityTimelineRow(
    dotColor: Color,
    title: String,
    timestamp: String,
    isFirst: Boolean,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (!isLast) 10.dp else 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontFamily = manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = timestamp,
                fontFamily = manrope,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ArrivedContent(
    destinationName: String,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.check),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "You Arrived!",
            fontFamily = manrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Safely reached $destinationName.",
            fontFamily = manrope,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Live sharing session completed automatically.",
            fontFamily = manrope,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(0.6f),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Done",
                fontFamily = manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun NotReachedContent(
    destinationName: String,
    onContinue: () -> Unit,
    onChangeDestination: () -> Unit,
    onEndJourney: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Still on the way",
            fontFamily = manrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Destination ($destinationName) has not been reached yet.",
            fontFamily = manrope,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(0.85f),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Continue Journey",
                fontFamily = manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onChangeDestination,
            modifier = Modifier.fillMaxWidth(0.85f),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Change Destination",
                fontFamily = manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onEndJourney,
            modifier = Modifier.fillMaxWidth(0.85f),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "End Journey",
                fontFamily = manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}
