package com.aarav.geowav.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
@Preview(showBackground = true)
@Composable
fun PlaceModalSheet(
    place: Place?,
    sheetState: SheetState,
    showSheet: Boolean,
    onDismissRequest: () -> Unit,
    clearSearch: () -> Unit,
    onAddPlaceBtnClick: (String) -> Unit
) {

    AnimatedVisibility(showSheet) {
        ModalBottomSheet(
            dragHandle = {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .width(60.dp)
                        .height(3.dp),
                    color = MaterialTheme.colorScheme.secondary,
                ) {

                }
            },
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            SheetContent(
                place,
                clearSearch,
                onAddPlaceBtnClick
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SheetContent(
    place: Place?,
    clearSearch: () -> Unit,
    onAddPlaceBtnClick: (String) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            Surface(
                modifier = Modifier.size(32.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape,
            ) {
                Icon(
                    painter = painterResource(R.drawable.navigation_arrow),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(6.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = place?.displayName ?: "Invalid Place",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
        }

        Text(
            text = place?.shortFormattedAddress ?: "Address not available",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            fontFamily = manrope,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Lat: ${
                place?.location?.latitude?.toString()?.take(7)
            }, Lng: ${place?.location?.longitude?.toString()?.take(7)}",
            style = MaterialTheme.typography.labelMedium,
            fontSize = 14.sp,
            fontFamily = manrope,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FilledTonalButton(
            onClick = {
                onAddPlaceBtnClick(place?.id ?: "0")
                clearSearch()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),

            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Add Place", fontFamily = manrope, fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    painter = painterResource(R.drawable.caret_circle_right),
                    contentDescription = "caret circle right",
                    modifier = Modifier.size(28.dp)
                )
            }
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
