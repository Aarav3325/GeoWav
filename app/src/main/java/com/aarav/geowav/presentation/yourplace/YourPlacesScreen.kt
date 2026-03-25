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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
            //.padding(it)
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
                modifier = Modifier.padding(top = 54.dp, start = 16.dp, end = 16.dp)
            )

            PlacesUsageCard(
                uiState.placesList.size,
                plan,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 8.dp)
            )

            if (uiState.placesList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(102.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.tray),
                                contentDescription = "tray",
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer),
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        Text(
                            text = "Add a location to start tracking your places",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = manrope,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp, bottom = 12.dp)
            ) {
                items(uiState.placesList) { place ->
                    GeofencePlaceCard(place) {
                        yourPlacesVM.deletePlace(place)
                    }
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

    val usageText = if (isUnlimited) {
        "$current places"
    } else {
        "$current / $max places used"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isLimitReached)
                    MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                else
                    MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLimitReached)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {

        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            if(showPlanInfo) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = planText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = manrope,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Surface(
                        shape = RoundedCornerShape(25),
                        color = if (isLimitReached)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (isLimitReached) "Limit Reached" else "Active",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = manrope,
                            color = if (isLimitReached)
                                MaterialTheme.colorScheme.onErrorContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = usageText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = manrope,
                color = if (isLimitReached)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onBackground
            )

//            if (plan != UserPlan.FREE) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(current.toFloat() / max)
                            .fillMaxHeight()
                            .background(
                                if (isLimitReached)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.secondary
                            )
                    )
                }
//            }

            if (isLimitReached) {
                Text(
                    text = "Upgrade to add more places",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = manrope,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}