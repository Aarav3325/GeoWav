package com.aarav.geowav.presentation.yourplace

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.core.utils.FeatureAccess
import com.aarav.geowav.data.model.Place
import com.aarav.geowav.data.model.UpgradeContext
import com.aarav.geowav.data.model.UpgradeEvents
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.presentation.components.GeofencePlaceCard
import com.aarav.geowav.presentation.components.CustomBottomSheet
import com.aarav.geowav.presentation.components.UpgradeBottomSheetContent
import com.aarav.geowav.presentation.components.RadiusChipGroup
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import com.aarav.geowav.presentation.components.PlaceTextField
import com.aarav.geowav.presentation.subscription.SubscriptionViewModel
import com.aarav.geowav.presentation.theme.manrope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.EaseInOut
import androidx.compose.foundation.layout.width

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun YourPlacesScreen(
    yourPlacesVM: YourPlacesVM,
    subscriptionVM: SubscriptionViewModel,
    navigateToPaywall: () -> Unit,
    navigateToMap: () -> Unit,
    navigateToPlaceDetails: (String) -> Unit = {}
) {
    val uiState by yourPlacesVM.uiState.collectAsState()
    val plan by subscriptionVM.userPlan.collectAsState()

    var upgradeContext by remember { mutableStateOf<UpgradeContext?>(null) }
    val placeToEdit = uiState.placeToEdit

    placeToEdit?.let { place ->
        CustomBottomSheet(
            onDismissRequest = {
                yourPlacesVM.setPlaceToEdit(null)
            }
        ) {
            var selectedRadius by remember { mutableStateOf(place.radius) }
            var placeName by remember { mutableStateOf(place.customName.ifBlank { place.placeName }) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        .align(Alignment.CenterHorizontally)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Edit Place",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = manrope,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp)
                ) {
                    PlaceTextField(
                        placeHolder = "e.g., Home, Work, Gym",
                        infoText = "Place Name",
                        name = placeName,
                        onValueChange = { placeName = it }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )

                    RadiusChipGroup(
                        chips = listOf(200f, 300f, 400f, 500f),
                        selectedRadius = selectedRadius,
                        onRadiusSelected = { selectedRadius = it }
                    )
                }

                Button(
                    onClick = {
                        yourPlacesVM.updatePlaceDetails(place, placeName.trim(), selectedRadius)
                        yourPlacesVM.setPlaceToEdit(null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Save Changes",
                        fontFamily = manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

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

    LaunchedEffect(Unit) {
        yourPlacesVM.event.collect { event ->
            if (event is UpgradeEvents.ShowUpgrade) {
                upgradeContext = event.upgradeContext
            }
        }
    }

    LaunchedEffect(Unit) {
        yourPlacesVM.getPlaces()
    }

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Screen Header with status bar padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Your Places",
                            fontSize = 24.sp,
                            fontFamily = manrope,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Awareness zones for circle notifications",
                            fontSize = 13.sp,
                            fontFamily = manrope,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (uiState.placesList.isNotEmpty()) {
                        val max = FeatureAccess.maxSavedPlaces(plan)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Text(
                                text = if (max == Int.MAX_VALUE) "${uiState.placesList.size} Saved" else "${uiState.placesList.size} / $max",
                                fontFamily = manrope,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            if (uiState.isLoading && uiState.placesList.isEmpty()) {
                // Skeleton loading list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp)
                ) {
                    item {
                        PlacesUsageCard(
                            current = 0,
                            plan = plan,
                            isLoading = true,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    items(3) {
                        PlaceCardSkeleton()
                    }
                }
            } else if (uiState.placesList.isEmpty()) {
                // Polished Empty State with Radar Visual
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Concentric Radar circles container
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                            modifier = Modifier.size(110.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    modifier = Modifier.size(76.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            painter = painterResource(R.drawable.map_trifold),
                                            contentDescription = "Empty places",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "No Awareness Zones Yet",
                                fontSize = 19.sp,
                                fontFamily = manrope,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Add meaningful places like Home, Work, or Gym so your circle gets automatic arrival and departure alerts.",
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                fontFamily = manrope,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 19.sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                val isLimitReached = uiState.placesList.size >= FeatureAccess.maxSavedPlaces(plan)
                                if (!isLimitReached) {
                                    navigateToMap()
                                } else {
                                    yourPlacesVM.onPlaceLimitReached(plan)
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.add),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Add First Place",
                                fontFamily = manrope,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp)
                ) {
                    item {
                        PlacesUsageCard(
                            current = uiState.placesList.size,
                            plan = plan,
                            isLoading = uiState.isLoading,
                            onUpgradeClick = {
                                yourPlacesVM.onPlaceLimitReached(plan)
                            },
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    items(uiState.placesList) { place ->
                        GeofencePlaceCard(
                            place = place,
                            onCardClick = { clickedPlace ->
                                navigateToPlaceDetails(clickedPlace.placeId)
                            }
                        )
                    }
                }
            }
        }

        if (uiState.placesList.isNotEmpty()) {
            AddLocationFAB(
                places = uiState.placesList,
                onPlaceLimitReached = yourPlacesVM::onPlaceLimitReached,
                userPlan = plan,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(bottom = 96.dp, end = 16.dp),
                onClick = navigateToMap
            )
        }
    }
}

@Composable
fun PlaceCardSkeleton() {
    val shimmer = shimmerBrush()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(shimmer)
            )

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmer)
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmer)
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(shimmer)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddLocationFAB(
    places: List<Place>,
    onPlaceLimitReached: (UserPlan) -> Unit,
    userPlan: UserPlan,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val isLimitReached = places.size >= FeatureAccess.maxSavedPlaces(userPlan)

    ExtendedFloatingActionButton(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        containerColor = if (isLimitReached) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
        contentColor = if (isLimitReached) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
        elevation = FloatingActionButtonDefaults.elevation(6.dp),
        onClick = {
            if (!isLimitReached) {
                onClick()
            } else {
                onPlaceLimitReached(userPlan)
            }
        },
        icon = {
            Icon(
                painter = painterResource(if (isLimitReached) R.drawable.emergency else R.drawable.add),
                contentDescription = if (isLimitReached) "Upgrade required" else "Add place",
                modifier = Modifier.size(20.dp)
            )
        },
        text = {
            Text(
                text = if (isLimitReached) "Limit Reached" else "New Place",
                fontFamily = manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    )
}

@Composable
fun shimmerBrush(
    showShimmer: Boolean = true,
    targetValue: Float = 1000f
): Brush {
    return if (showShimmer) {
        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnimation by transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = EaseInOut),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerTranslate"
        )
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
            ),
            start = androidx.compose.ui.geometry.Offset(translateAnimation, translateAnimation),
            end = androidx.compose.ui.geometry.Offset(translateAnimation + 300f, translateAnimation + 300f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent)
        )
    }
}

@Composable
fun PlacesUsageCard(
    current: Int,
    plan: UserPlan,
    textSize: TextUnit? = null,
    showPlanInfo: Boolean = true,
    isLoading: Boolean = false,
    onUpgradeClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val max = FeatureAccess.maxSavedPlaces(plan)
    val isUnlimited = max == Int.MAX_VALUE
    val isLimitReached = !isUnlimited && current >= max

    val planText = when (plan) {
        UserPlan.FREE -> "GeoWav Free"
        UserPlan.PREMIUM -> "GeoWav Premium"
        UserPlan.PRO -> "GeoWav Pro"
    }

    val usageText = if (isUnlimited)
        "$current active awareness zones"
    else
        "$current / $max active awareness zones"

    val cardBg = if (isLimitReached)
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
    else
        MaterialTheme.colorScheme.surfaceContainer

    val cardBorder = if (isLimitReached)
        BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.45f))
    else
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

    val cardFg = if (isLimitReached)
        MaterialTheme.colorScheme.onErrorContainer
    else
        MaterialTheme.colorScheme.onSurface

    val cardFgMuted = if (isLimitReached)
        cardFg.copy(alpha = 0.75f)
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    val badgeBg = if (isLimitReached)
        MaterialTheme.colorScheme.error
    else
        MaterialTheme.colorScheme.surfaceContainerHighest

    val badgeFg = if (isLimitReached)
        MaterialTheme.colorScheme.onError
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isLoading) {
                val shimmer = shimmerBrush()
                if (showPlanInfo) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(90.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(shimmer)
                        )
                        Box(
                            modifier = Modifier
                                .width(55.dp)
                                .height(18.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(shimmer)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmer)
                )
                if (!isUnlimited) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(shimmer)
                    )
                }
            } else {
                if (showPlanInfo) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = planText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = manrope,
                            color = cardFgMuted
                        )

                        Surface(
                            shape = RoundedCornerShape(99.dp),
                            color = badgeBg
                        ) {
                            Text(
                                text = if (isLimitReached) "Limit reached" else "Active",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = manrope,
                                color = badgeFg
                            )
                        }
                    }
                }

                Text(
                    text = usageText,
                    fontSize = textSize ?: 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = manrope,
                    color = cardFg
                )

                if (!isUnlimited) {
                    val targetProgress = (current.toFloat() / max).coerceIn(0f, 1f)
                    var progressAnimatable by remember { mutableStateOf(0f) }
                    LaunchedEffect(targetProgress) {
                        progressAnimatable = targetProgress
                    }
                    val animatedProgress by animateFloatAsState(
                        targetValue = progressAnimatable,
                        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                        label = "PlacesUsageProgress"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(cardFg.copy(alpha = 0.12f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(99.dp))
                                .background(
                                    brush = if (isLimitReached) {
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.error,
                                                MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                            )
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    }
                                )
                        )
                    }
                }

                if (isLimitReached) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Upgrade plan for unlimited zones",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = manrope,
                            color = cardFgMuted
                        )

                        if (onUpgradeClick != null) {
                            androidx.compose.material3.TextButton(
                                onClick = onUpgradeClick,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Upgrade",
                                    fontFamily = manrope,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}