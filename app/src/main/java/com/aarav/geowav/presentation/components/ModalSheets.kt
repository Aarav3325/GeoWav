package com.aarav.geowav.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

import com.aarav.geowav.R
import com.aarav.geowav.core.utils.SubscriptionHelper
import com.aarav.geowav.data.model.PurchaseResult
import com.aarav.geowav.data.model.UpgradeContext
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.data.model.getPlanContent
import com.aarav.geowav.data.model.getReasonContent
import com.aarav.geowav.presentation.locationsharing.itemShape
import com.aarav.geowav.presentation.paywall.PlanColors
import com.aarav.geowav.presentation.paywall.freePlanColors
import com.aarav.geowav.presentation.paywall.premiumPlanColors
import com.aarav.geowav.presentation.paywall.proPlanColors
import com.aarav.geowav.presentation.theme.manrope
import com.aarav.geowav.presentation.theme.onPrimaryDark
import com.aarav.geowav.presentation.theme.onSecondaryDark
import com.aarav.geowav.presentation.theme.outlineVariantDark
import com.aarav.geowav.presentation.theme.primaryContainerDark
import com.aarav.geowav.presentation.theme.primaryDark
import com.aarav.geowav.presentation.theme.secondaryContainerDark
import com.aarav.geowav.presentation.theme.secondaryDark
import com.aarav.geowav.presentation.theme.surfaceContainerDark
import com.aarav.geowav.presentation.theme.surfaceContainerLight
import com.aarav.geowav.presentation.theme.surfaceDimLight
import com.google.android.libraries.places.api.model.Place
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ObserveSheetEnterEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
private val ObserveSheetExitEasing = CubicBezierEasing(0.4f, 0f, 1f, 1f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceModalSheet(
    place: Place?,
    sheetState: SheetState,
    showSheet: Boolean,
    onDismissRequest: () -> Unit,
    clearSearch: () -> Unit,
    onAddPlaceBtnClick: (String) -> Unit,
    forJourney: Boolean = false,
    onSelectJourneyDestination: ((Double, Double, String, String) -> Unit)? = null
) {
    AnimatedVisibility(showSheet) {
        ModalBottomSheet(
            dragHandle = {
                Surface(
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .width(36.dp)
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                ) {}
            },
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp
        ) {
            SheetContent(
                place = place,
                clearSearch = clearSearch,
                onDismiss = onDismissRequest,
                onAddPlaceBtnClick = onAddPlaceBtnClick,
                forJourney = forJourney,
                onSelectJourneyDestination = onSelectJourneyDestination
            )
        }
    }
}


@Composable
fun SheetContent(
    place: Place?,
    clearSearch: () -> Unit,
    onDismiss: () -> Unit = {},
    onAddPlaceBtnClick: (String) -> Unit,
    forJourney: Boolean = false,
    onSelectJourneyDestination: ((Double, Double, String, String) -> Unit)? = null
) {
    val displayName = place?.displayName?.takeIf { it.isNotBlank() } ?: "Selected Location"
    val addressText = place?.shortFormattedAddress?.takeIf { it.isNotBlank() }
        ?: place?.formattedAddress?.takeIf { it.isNotBlank() }
        ?: "Address unavailable"

    val lat = place?.location?.latitude ?: 0.0
    val lng = place?.location?.longitude ?: 0.0
    val coordinatesText = String.format(Locale.US, "%.5f, %.5f", lat, lng)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                color = if (forJourney)
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                else
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                shape = CircleShape,
                border = BorderStroke(
                    1.dp,
                    if (forJourney)
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                    else
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(
                            if (forJourney) R.drawable.navigation_arrow else R.drawable.map_pin
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = if (forJourney)
                            MaterialTheme.colorScheme.secondary
                        else
                            MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = if (forJourney) "Journey Destination" else "Place Details",
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Details Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Address Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.map_pin),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Address",
                            fontFamily = manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(1.dp))
                        Text(
                            text = addressText,
                            fontFamily = manrope,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                )

                // Coordinates Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.gps),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GPS Coordinates",
                            fontFamily = manrope,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(1.dp))
                        Text(
                            text = coordinatesText,
                            fontFamily = manrope,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Action Button
        Button(
            onClick = {
                if (forJourney && onSelectJourneyDestination != null && place != null) {
                    val placeLat = place.location?.latitude ?: 0.0
                    val placeLng = place.location?.longitude ?: 0.0
                    val name = place.displayName ?: "Selected Location"
                    val address = place.shortFormattedAddress ?: place.formattedAddress ?: ""
                    onSelectJourneyDestination(placeLat, placeLng, name, address)
                    clearSearch()
                } else {
                    onAddPlaceBtnClick(place?.id ?: "0")
                    clearSearch()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(
                        if (forJourney) R.drawable.navigation_arrow else R.drawable.add
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (forJourney) "Set as Destination" else "Add to Places",
                    fontFamily = manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        // Dismiss / Cancel Button
        TextButton(
            onClick = {
                clearSearch()
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Text(
                text = "Cancel",
                fontFamily = manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheet(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        sheetState = sheetState,
        modifier = Modifier.wrapContentHeight(),
        onDismissRequest = onDismissRequest
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheetForObserve(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    val expandedState = sheetState.targetValue

    val expanded = when {
        expandedState.name == "PartiallyExpanded" -> false
        expandedState.name == "Expanded" -> true
        expandedState.name == "Hidden" -> false
        else -> false
    }
//
//    val height = when {
//        expandedState.name == "PartiallyExpanded" -> Modifier.wrapContentHeight()
//        expandedState.name == "Expanded" -> Modifier.fillMaxHeight()
//        else -> Modifier.wrapContentHeight()
//    }



    ModalBottomSheet(
        containerColor = Color(0xEE111820),
        shape = RoundedCornerShape(28.dp),
        sheetState = sheetState,
        modifier = Modifier
            .wrapContentHeight()
            .padding(8.dp)
            .padding(bottom = 24.dp),
        onDismissRequest = onDismissRequest
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 220,
                    easing = ObserveSheetEnterEasing
                )
            ) + slideInVertically(
                animationSpec = tween(
                    durationMillis = 280,
                    easing = ObserveSheetEnterEasing
                ),
                initialOffsetY = { it / 14 }
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = 150,
                    easing = ObserveSheetExitEasing
                )
            ) + slideOutVertically(
                animationSpec = tween(
                    durationMillis = 190,
                    easing = ObserveSheetExitEasing
                ),
                targetOffsetY = { it / 16 }
            )
        ) {
            content()
        }
    }
}

@Composable
fun UpgradeBottomSheetContent(
    context: UpgradeContext,
    onUpgradeClick: () -> Unit,
    onDismiss: () -> Unit
) {

    val reasonContent = remember { getReasonContent(context.reason) }
    val planContent = remember { getPlanContent(context.upgradeTo) }

    val colors = when (context.upgradeTo) {
        UserPlan.PREMIUM -> premiumPlanColors()
        UserPlan.PRO -> proPlanColors()
        UserPlan.FREE -> freePlanColors()
    }

    val gradientColors = when (context.upgradeTo) {
        UserPlan.PREMIUM -> listOf(
            onPrimaryDark,
            primaryContainerDark
        )

        UserPlan.PRO -> listOf(
            onSecondaryDark,
            secondaryContainerDark
        )

        UserPlan.FREE -> listOf(
            surfaceContainerDark,
            outlineVariantDark
        )
    }

    val textColor = when (context.upgradeTo) {
        UserPlan.PREMIUM -> primaryDark
        UserPlan.PRO -> secondaryDark
        UserPlan.FREE -> surfaceContainerDark
    }



    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        gradientColors
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
            ) {

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            textColor.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(reasonContent.icon),
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        text = planContent.title,
                        fontFamily = manrope,
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = reasonContent.title,
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = reasonContent.description,
            fontFamily = manrope,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 20.dp),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Column(
                modifier = Modifier.padding(0.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                planContent.features.forEachIndexed { index, feature ->
                    FeatureItem(feature, colors, index, planContent.features.size)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        FilledTonalButton(
            onClick = onUpgradeClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.buttonBg,
                contentColor = colors.buttonTextColor
            ),
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = planContent.ctaText,
                fontFamily = manrope,
                fontWeight = FontWeight.SemiBold,
                color = colors.buttonTextColor,
                fontSize = 14.sp,
            )
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                "Maybe later",
                fontFamily = manrope,
                color = colors.checkTint,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

//@Composable
//fun UpgradeBottomSheetContent(
//    reason: UpgradeReason,
//    onUpgradeClick: () -> Unit,
//    onDismiss: () -> Unit
//) {
//    val (title, desc) = when (reason) {
//        UpgradeReason.PlaybackLocked -> "Playback is Premium" to
//                "Replay your trips with smooth animation and insights."
//
//        UpgradeReason.HistoryLimit -> "Unlock Full History" to
//                "Access your complete travel timeline anytime."
//
//        UpgradeReason.SpeedControl -> "Control Playback Speed" to
//                "Adjust speed for better analysis of your routes."
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(20.dp)
//    ) {
//
//        Text(
//            text = "Upgrade to Premium",
//            fontFamily = manrope,
//            style = MaterialTheme.typography.titleLarge,
//            fontWeight = FontWeight.Bold
//        )
//
//        Spacer(Modifier.height(8.dp))
//
//        Text(
//            text = title,
//            fontFamily = manrope,
//            style = MaterialTheme.typography.titleMedium
//        )
//
//        Spacer(Modifier.height(6.dp))
//
//        Text(
//            text = desc,
//            fontFamily = manrope,
//            style = MaterialTheme.typography.bodyMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//
//        Spacer(Modifier.height(20.dp))
//
//        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//            FeatureItem("Playback & route animation")
//            FeatureItem("Unlimited history access")
//            FeatureItem("Advanced insights & speed control")
//        }
//
//        Spacer(Modifier.height(24.dp))
//
//        FilledTonalButton(
//            onClick = onUpgradeClick,
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(14.dp)
//        ) {
//            Text("Upgrade Now", fontFamily = manrope)
//        }
//
//        TextButton(
//            onClick = onDismiss,
//            modifier = Modifier.align(Alignment.CenterHorizontally)
//        ) {
//            Text("Maybe later", fontFamily = manrope)
//        }
//    }
//}

@Composable
fun FeatureItem(text: String, colors: PlanColors, index: Int, total: Int) {
    val shape = itemShape(index, total)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(vertical = 12.dp, horizontal = 16.dp)

    ) {
        Icon(
            painter = painterResource(R.drawable.check),
            contentDescription = null,
            tint = colors.checkTint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text, fontFamily = manrope,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseSuccessBottomSheet(
    result: PurchaseResult.Success,
    onExplore: () -> Unit
) {

    val planInfo = getPlanContent(result.plan)

    val colors = when (result.plan) {
        UserPlan.PREMIUM -> premiumPlanColors()
        UserPlan.PRO -> proPlanColors()
        UserPlan.FREE -> freePlanColors()
    }

    val gradientColors = when (result.plan) {
        UserPlan.PREMIUM -> listOf(onPrimaryDark, primaryContainerDark)
        UserPlan.PRO -> listOf(onSecondaryDark, secondaryContainerDark)
        UserPlan.FREE -> listOf(surfaceContainerDark, outlineVariantDark)
    }

    val badge = when(result.plan) {
        UserPlan.PREMIUM -> R.drawable.geowav_premium_badge
        UserPlan.PRO -> R.drawable.geowav_pro_badge
        UserPlan.FREE -> R.drawable.check
    }

    val formattedTime = remember(result.purchaseTime) {
        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            .format(Date(result.purchaseTime))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
        //.padding(bottom = 12.dp)
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            gradientColors
                        )
                    )
                    .padding(20.dp)
            ) {

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                Color.White.copy(alpha = 0.15f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.check),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = if (result.plan == UserPlan.PRO)
                            "Welcome to GeoWav Pro 🚀"
                        else
                            "You're now GeoWav Premium 🎉",
                        fontSize = 18.sp,
                        fontFamily = manrope,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Your upgrade was successful",
                        fontSize = 14.sp,
                        fontFamily = manrope,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .border(1.dp, colors.border, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = colors.bg),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    Text(
                        text = "PLAN ACTIVATED",
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = manrope
                    )

                    val planName = SubscriptionHelper.getPlanName(result.plan)

                    Text(
                        text = "GeoWav $planName",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = manrope
                    )


                    Text(
                        text = "Activated on ${formattedTime}",
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 12.sp,
                        fontFamily = manrope
                    )
                }

                Icon(
                    painter = painterResource(badge),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(0.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                planInfo.features.forEachIndexed { index, feature ->
                    FeatureItem(feature, colors, index, planInfo.features.size)
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        FilledTonalButton(
            onClick = onExplore,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.buttonBg,
                contentColor = colors.buttonTextColor
            )
        ) {
            Text(
                "Start Exploring",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun PurchaseSuccessPremiumBottomSheetPreview() {
    MaterialTheme {
        PurchaseSuccessBottomSheet(
            result = PurchaseResult.Success(
                plan = UserPlan.PREMIUM,
                purchaseToken = "dummy_token",
                orderId = "GPA.1234-5678-9012-34567",
                purchaseTime = System.currentTimeMillis(),
                expiryTime = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000
            ),
            onExplore = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PurchaseSuccessProBottomSheetPreview() {
    MaterialTheme {
        PurchaseSuccessBottomSheet(
            result = PurchaseResult.Success(
                plan = UserPlan.PRO,
                purchaseToken = "dummy_token",
                orderId = "GPA.1234-5678-9012-34568",
                purchaseTime = System.currentTimeMillis(),
                expiryTime = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000
            ),
            onExplore = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeConfirmBottomSheet(
    plan: UserPlan,
    currentPlan: UserPlan = UserPlan.FREE,
    onConfirm: () -> Unit
) {

    val planInfo = getPlanContent(plan)

    val price = when (plan) {
        UserPlan.PREMIUM -> "₹99/month"
        UserPlan.PRO -> if (currentPlan == UserPlan.PREMIUM) "₹149/month" else "₹199/month"
        else -> ""
    }

    val colors = when (plan) {
        UserPlan.PREMIUM -> premiumPlanColors()
        UserPlan.PRO -> proPlanColors()
        UserPlan.FREE -> freePlanColors()
    }

    val badge = when (plan) {
        UserPlan.PREMIUM -> R.drawable.geowav_premium_badge
        UserPlan.PRO -> R.drawable.geowav_pro_badge
        UserPlan.FREE -> R.drawable.check
    }



    val planName = SubscriptionHelper.getPlanName(plan)


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {

            Text(
                text = if (plan == UserPlan.PRO) "Go Pro" else "Upgrade to Premium",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = manrope
            )

            Text(
                text = "Unlock more powerful features",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.outline,
                fontFamily = manrope
            )
        }

        Spacer(Modifier.height(20.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    Text(
                        text = "GeoWav $planName",
                        fontSize = 16.sp,
                        color = colors.text,
                        fontWeight = FontWeight.Medium,
                        fontFamily = manrope
                    )

                    Text(
                        text = price,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = manrope
                    )

                    Text(
                        text = "Cancel anytime",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                        fontFamily = manrope
                    )
                }

                Icon(
                    painter = painterResource(badge),
                    contentDescription = "badge",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterEnd)
                )
            }
        }


        Spacer(Modifier.height(20.dp))

        Column {
            planInfo.features.take(4).forEach { feature ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Icon(
                        painter = painterResource(R.drawable.check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )

                    Text(
                        text = feature,
                        fontSize = 14.sp,
                        fontFamily = manrope
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onConfirm,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.buttonBg,
                contentColor = colors.buttonTextColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Continue to Payment",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                fontFamily = manrope
            )
        }


        Spacer(Modifier.height(6.dp))

        Text(
            text = "Billed monthly · Cancel anytime",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationRequestBottomSheet(
    requesterName: String,
    sheetState: SheetState,
    showSheet: Boolean,
    onDismissRequest: () -> Unit,
    onAccept: (durationMinutes: Int) -> Unit,
    onDecline: () -> Unit
) {
    if (!showSheet) return

    val selectedDuration = remember { mutableStateOf(15) } // 15, 60, -1

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon Badge with layered tonal depth
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.location_sharing),
                        contentDescription = "Location Request Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sheet Title
            Text(
                text = "Location Request",
                fontFamily = manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle with bolded requester name for immediate clarity
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                        append(requesterName)
                    }
                    append(" is asking for your current location.")
                },
                fontFamily = manrope,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section Label
            Text(
                text = "Share for",
                fontFamily = manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Duration options list with animated selection state
            val durations = listOf(
                15 to "15 minutes",
                60 to "1 hour",
                -1 to "Until I stop"
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                durations.forEach { (duration, label) ->
                    val isSelected = selectedDuration.value == duration
                    val animatedContainerColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        animationSpec = tween(durationMillis = 200),
                        label = "durationContainerColor"
                    )
                    val animatedBorderColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        },
                        animationSpec = tween(durationMillis = 200),
                        label = "durationBorderColor"
                    )

                    Surface(
                        onClick = { selectedDuration.value = duration },
                        shape = RoundedCornerShape(16.dp),
                        color = animatedContainerColor,
                        border = BorderStroke(1.dp, animatedBorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.clock),
                                    contentDescription = null,
                                    tint = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = label,
                                    fontFamily = manrope,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }

                            // Selection indicator
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else Color.Transparent
                                    )
                                    .border(
                                        width = if (isSelected) 0.dp else 1.5.dp,
                                        color = if (isSelected) Color.Transparent
                                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
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

            Spacer(modifier = Modifier.height(24.dp))

            // Primary CTA: Share Location
            Button(
                onClick = { onAccept(selectedDuration.value) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.location_sharing),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "Share Location",
                        fontFamily = manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Secondary Action: Not Now
            TextButton(
                onClick = onDecline,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Not Now",
                    fontFamily = manrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun PreviewLocationRequestSheet() {
    LocationRequestBottomSheet(
        "Aarav",
        rememberModalBottomSheetState(),
        true,
        {},
        {},
        {}
    )
}
