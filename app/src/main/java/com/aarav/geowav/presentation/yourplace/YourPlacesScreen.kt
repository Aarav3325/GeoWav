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
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Edit Place",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = manrope,
                    fontWeight = FontWeight.Bold
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp)
                ) {
                    PlaceTextField(
                        placeHolder = "e.g., Home, Work, Gym",
                        infoText = "Name",
                        name = placeName,
                        onValueChange = { placeName = it }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
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
                        .height(54.dp),
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
                
                Spacer(modifier = Modifier.height(24.dp))
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

    LaunchedEffect(uiState.placesList) {
        yourPlacesVM.getPlaces()
    }

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {

        Column(
            modifier = Modifier
        ) {

            Column(
                modifier = Modifier.padding(top = 56.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "Your Places",
                    fontSize = 28.sp,
                    fontFamily = manrope,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Awareness zones for circle notifications",
                    fontSize = 13.sp,
                    fontFamily = manrope,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uiState.isLoading && uiState.placesList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading your saved places...",
                            fontFamily = manrope,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            } else if (uiState.placesList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.size(96.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.map_trifold),
                                contentDescription = "Empty places",
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)),
                                modifier = Modifier.padding(24.dp)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "No awareness zones",
                                fontSize = 18.sp,
                                fontFamily = manrope,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.SemiBold,
                            )

                            Text(
                                text = "Add a meaningful place like Home or Work to get started.",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                fontFamily = manrope,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {

                item {
                    PlacesUsageCard(
                        current = uiState.placesList.size,
                        plan = plan,
                        isLoading = uiState.isLoading,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .padding(top = 8.dp, bottom = 8.dp)
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

        AddLocationFAB(
            uiState.placesList,
            yourPlacesVM::onPlaceLimitReached,
            plan,
            Modifier
                .align(Alignment.BottomEnd).navigationBarsPadding().padding(bottom = 80.dp)
                .padding(vertical = 16.dp, horizontal = 12.dp),
            navigateToMap
        )
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
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        elevation = FloatingActionButtonDefaults.elevation(8.dp),
        onClick = {
            if (!isLimitReached) {
                onClick()
            } else {
                onPlaceLimitReached(userPlan)
            }
        },
        icon = {
            Icon(
                painter = painterResource(R.drawable.add),
                contentDescription = "add location",
            )
        },
        text = {
            Text(
                text = "New Place",
                fontFamily = manrope,
                fontWeight = FontWeight.Bold
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
        "$current active places"
    else
        "$current / $max active places"

    val cardBg = if (isLimitReached)
        MaterialTheme.colorScheme.errorContainer
    else
        MaterialTheme.colorScheme.surfaceContainer

    val cardBorder = if (isLimitReached)
        BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
    else
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

    val cardFg = if (isLimitReached)
        MaterialTheme.colorScheme.onErrorContainer
    else
        MaterialTheme.colorScheme.onSurface

    val cardFgMuted = if (isLimitReached)
        cardFg.copy(alpha = 0.65f)
    else
        MaterialTheme.colorScheme.outline

    val badgeBg = if (isLimitReached)
        MaterialTheme.colorScheme.error
    else
        MaterialTheme.colorScheme.surfaceContainerLow

    val badgeFg = if (isLimitReached)
        MaterialTheme.colorScheme.onError
    else
        MaterialTheme.colorScheme.outline

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
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
                            fontWeight = FontWeight.Medium,
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
                    fontSize = textSize ?: 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = manrope,
                    color = cardFg
                )

                if (!isUnlimited) {
                    val targetProgress = current.toFloat() / max
                    var progressAnimatable by remember { mutableStateOf(0f) }
                    LaunchedEffect(targetProgress) {
                        progressAnimatable = targetProgress
                    }
                    val animatedProgress by animateFloatAsState(
                        targetValue = progressAnimatable,
                        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                        label = "PlacesUsageProgress"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(cardFg.copy(alpha = 0.15f))
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
                    Text(
                        text = "Upgrade to add more places",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = manrope,
                        color = cardFgMuted
                    )
                }
            }
        }
    }
}