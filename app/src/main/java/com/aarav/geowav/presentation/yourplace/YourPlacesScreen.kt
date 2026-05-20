package com.aarav.geowav.presentation.yourplace

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.Spacer
import com.aarav.geowav.presentation.subscription.SubscriptionViewModel
import com.aarav.geowav.presentation.theme.manrope

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun YourPlacesScreen(
    yourPlacesVM: YourPlacesVM,
    subscriptionVM: SubscriptionViewModel,
    navigateToPaywall: () -> Unit,
    navigateToMap: () -> Unit
) {
    val uiState by yourPlacesVM.uiState.collectAsState()

    val plan by subscriptionVM.userPlan.collectAsState()



    var upgradeContext by remember { mutableStateOf<UpgradeContext?>(null) }
    var placeToEditRadius by remember { mutableStateOf<Place?>(null) }

    placeToEditRadius?.let { place ->
        CustomBottomSheet(
            onDismissRequest = {
                placeToEditRadius = null
            }
        ) {
            var selectedRadius by remember { mutableStateOf(place.radius) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Edit Radius",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = manrope,
                    fontWeight = FontWeight.Bold
                )

                RadiusChipGroup(
                    chips = listOf(200f, 300f, 400f, 500f),
                    selectedRadius = selectedRadius,
                    onRadiusSelected = { selectedRadius = it }
                )

                Button(
                    onClick = {
                        yourPlacesVM.updatePlaceRadius(place, selectedRadius)
                        placeToEditRadius = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Changes", fontFamily = manrope)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
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

            Text(
                text = "Your Places",
                fontSize = 20.sp,
                fontFamily = manrope,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 54.dp, start = 12.dp, end = 12.dp)
            )

            PlacesUsageCard(
                uiState.placesList.size,
                plan,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(top = 16.dp, bottom = 8.dp)
            )

            if (uiState.placesList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
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
                    .padding(top = 8.dp, bottom = 12.dp)
            ) {
                items(uiState.placesList) { place ->
                    GeofencePlaceCard(
                        place = place,
                        onDeleteClick = {
                            yourPlacesVM.deletePlace(place)
                        },
                        onEditRadiusClick = {
                            placeToEditRadius = place
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
                .align(Alignment.BottomEnd)
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

    FloatingActionButton(
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
        }
    ) {
        Icon(
            painter = painterResource(R.drawable.add),
            contentDescription = "add location",
        )
    }
}
@Composable
fun PlacesUsageCard(
    current: Int,
    plan: UserPlan,
    textSize: TextUnit? = null,
    showPlanInfo: Boolean = true,
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
        "$current places"
    else
        "$current / $max places used"

    val cardBg = if (isLimitReached)
        MaterialTheme.colorScheme.errorContainer
    else
        MaterialTheme.colorScheme.primaryContainer

    val cardFg = if (isLimitReached)
        MaterialTheme.colorScheme.onErrorContainer
    else
        MaterialTheme.colorScheme.onPrimaryContainer

    val cardFgMuted = cardFg.copy(alpha = 0.65f)

    val badgeBg = if (isLimitReached)
        MaterialTheme.colorScheme.error
    else
        MaterialTheme.colorScheme.primary

    val badgeFg = if (isLimitReached)
        MaterialTheme.colorScheme.onError
    else
        MaterialTheme.colorScheme.onPrimary

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

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
                fontSize = textSize ?: 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = manrope,
                color = cardFg
            )

            if (!isUnlimited) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(cardFg.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(current.toFloat() / max)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(99.dp))
                            .background(cardFg)
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