package com.aarav.geowav.presentation.paywall

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.core.utils.SubscriptionHelper.getSubscriptionStatus
import com.aarav.geowav.data.model.PurchaseResult
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.data.model.UserSubscription
import com.aarav.geowav.presentation.components.CustomBottomSheet
import com.aarav.geowav.presentation.components.PurchaseSuccessBottomSheet
import com.aarav.geowav.presentation.subscription.SubscriptionEvents
import com.aarav.geowav.presentation.subscription.SubscriptionViewModel
import com.aarav.geowav.presentation.theme.manrope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class PlanColors(
    val bg: Color,
    val border: Color,
    val text: Color,
    val buttonBg: Color,
    val buttonTextColor: Color,
    val checkTint: Color,
)


@Composable
fun freePlanColors() = PlanColors(
    bg = MaterialTheme.colorScheme.surfaceContainer,
    border = MaterialTheme.colorScheme.outlineVariant,
    text = MaterialTheme.colorScheme.onSurfaceVariant,
    buttonBg = MaterialTheme.colorScheme.surfaceContainerHigh,
    buttonTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    checkTint = MaterialTheme.colorScheme.outline,
)

@Composable
fun premiumPlanColors() = PlanColors(
    bg = MaterialTheme.colorScheme.onPrimary,
    border = MaterialTheme.colorScheme.primaryContainer,
    text = MaterialTheme.colorScheme.primary,
    buttonBg = MaterialTheme.colorScheme.primary,
    buttonTextColor = MaterialTheme.colorScheme.onPrimary,
    checkTint = MaterialTheme.colorScheme.primary,
)

@Composable
fun proPlanColors() = PlanColors(
    bg = MaterialTheme.colorScheme.onSecondary,
    border = MaterialTheme.colorScheme.secondaryContainer,
    text = MaterialTheme.colorScheme.secondary,
    buttonBg = MaterialTheme.colorScheme.secondary,
    buttonTextColor = MaterialTheme.colorScheme.onSecondary,
    checkTint = MaterialTheme.colorScheme.secondary,
)


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PaywallScreen(
    subscriptionViewModel: SubscriptionViewModel,
    back: () -> Unit,
    onPremiumClick: () -> Unit,
    onProClick: () -> Unit
) {

    val plan by subscriptionViewModel.userPlan.collectAsState()
    val purchaseResult by subscriptionViewModel.purchaseResult.collectAsState()
    val subscriptionState by subscriptionViewModel.subscriptionState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }


    val context = LocalContext.current
    val activity = context as? Activity

//    LaunchedEffect(Unit) {
//        activity?.let {
//            subscriptionViewModel.launchBillingFlow(
//                activity = it,
//                productId = ""
//            )
//        }
//    }

    LaunchedEffect(Unit) {
        subscriptionViewModel.fetchSubscriptionStatus()
        subscriptionViewModel.uiEvents.collect {
            event ->
            when (event) {

                is SubscriptionEvents.PurchaseCancelled -> {
                    snackbarHostState.showSnackbar("Purchase cancelled")
                }

                is SubscriptionEvents.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                else -> Unit
            }
        }
    }

    val successResult = purchaseResult as? PurchaseResult.Success

    if (successResult != null) {
        CustomBottomSheet(
            onDismissRequest = {
                subscriptionViewModel.clearPurchaseResult()
            }
        ) {
            PurchaseSuccessBottomSheet(
                result = successResult,
                onExplore = {
                    subscriptionViewModel.clearPurchaseResult()
                }
            )
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState, Modifier.padding(bottom = 36.dp)) },
        topBar = {
            TopAppBar(
                modifier = Modifier,
                title = {
                    Text(
                        text = "Upgrade your plan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = manrope,
                        lineHeight = 20.sp
                    )
                },
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
                colors = TopAppBarDefaults.topAppBarColors(
                    // containerColor = Color(0xFF0F172A)
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                navigationIcon = {
                    IconButton(onClick = back) {
                        Icon(
                            painter = painterResource(R.drawable.back),
                            contentDescription = "back",
                            modifier = Modifier.size(IconButtonDefaults.smallIconSize)
                        )
                    }
                },
                actions = {
//                    Surface(
//                        shape = CircleShape,
//                        color = MaterialTheme.colorScheme.primary,
//                        modifier = Modifier
//                            .padding(end = 8.dp)
//                            .size(42.dp)
//                    ) {
//                        Icon(
//                            painter = painterResource(R.drawable.credit_card),
//                            contentDescription = "card",
//                            tint = MaterialTheme.colorScheme.onPrimary,
//                            modifier = Modifier
//                                .size(24.dp)
//                                .padding(8.dp)
//                        )
//                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
    ) {

        val isPremiumOrAbove = plan >= UserPlan.PREMIUM

        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .height(8.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp, bottom = 104.dp)
            ) {

                PaywallHeader()

                Spacer(Modifier.height(16.dp))

                CurrentPlanCard(subscriptionState)


                Spacer(Modifier.height(16.dp))

//
//                PlanCard(
//                    title = "GeoWav Free",
//                    price = "₹0",
//                    billingLabel = "/forever",
//                    features = listOf(
//                        "Basic live tracking",
//                        "Today's history only",
//                        "Up to 2 saved places",
//                        "Connect with 1 person"
//                    ),
//                    isFeatured = false,
//                    isCurrentPlan = true,
//                    buttonText = "Current plan",
//                    colors = freePlanColors(),
//                    onClick = {}
//                )
//
//                Spacer(Modifier.height(12.dp))


                if(plan != UserPlan.PREMIUM) {
                    PlanCard(
                        title = "GeoWav Premium",
                        price = "₹99",
                        billingLabel = "/month",
                        features = listOf(
                            "Unlimited live tracking",
                            "Replay your journeys",
                            "Access today & yesterday",
                            "Up to 10 saved places",
                            "Connect with 5 people"
                        ),
                        isFeatured = true,
                        isCurrentPlan = isPremiumOrAbove,
                        buttonText = "Upgrade to Premium",
                        colors = premiumPlanColors(),
                        onClick = onPremiumClick
                    )

                    Spacer(Modifier.height(12.dp))
                }


                if(plan != UserPlan.PRO) {
                    PlanCard(
                        title = "GeoWav Pro",
                        price = "₹199",
                        billingLabel = "/month",
                        features = listOf(
                            "Everything in Premium",
                            "Full location history",
                            "Stay point detection",
                            "Movement insights & analytics",
                            "Export & share trips"
                        ),
                        isFeatured = false,
                        isCurrentPlan = plan == UserPlan.PRO,
                        buttonText = "Go Pro",
                        colors = proPlanColors(),
                        onClick = onProClick
                    )
                }

//                if(plan != UserPlan.FREE) {
//                    subscriptionState?.let {
//                        SubscriptionStatusCard(
//                            data = it
//                        ) {
//
//                        }
//                    }
//                }

                Spacer(Modifier.height(24.dp))

                ComparisonTable()
            }

            StickyUpgradeCTA(
                modifier = Modifier.align(Alignment.BottomCenter),
                onClick = onPremiumClick,
                isEnabled = !isPremiumOrAbove
            )
        }
    }
}

@Composable
fun SubscriptionStatusCard(
    data: UserSubscription,
    modifier: Modifier = Modifier,
    onManageClick: () -> Unit
) {

    val status = getSubscriptionStatus(data)
    val plan = UserPlan.valueOf(data.plan)

    val accentColor = when (plan) {
        UserPlan.PREMIUM -> MaterialTheme.colorScheme.primary
        UserPlan.PRO -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {

        val formattedPurchaseDate = remember(data.purchaseTime) {
            SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                .format(Date(data.purchaseTime))
        }

        val formattedExpiryDate = remember(data.expiryTime) {
            SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                .format(Date(data.expiryTime))
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // 🔹 Top Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {

                Column {
                    Text(
                        text = "GeoWav ${plan.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = when {
                            status.isExpired -> "Subscription expired"
                            status.isCancelled -> "Expires on $formattedExpiryDate"
                            else -> "Active since $formattedPurchaseDate"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    painter = painterResource(R.drawable.check),
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 🔹 Divider
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // 🔹 Status Row
            Text(
                text = when {
                    status.isExpired -> "Your subscription has ended"
                    status.isCancelled -> "Expires in ${status.daysRemaining} days"
                    else -> "Renews on $formattedExpiryDate"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (status.isExpired)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurface
            )

            // 🔹 Action Button
            TextButton(
                onClick = onManageClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Manage Subscription")
            }
        }
    }
}

@Composable
fun CurrentPlanCard(
    data: UserSubscription?,
) {

    val plan = UserPlan.valueOf(data?.plan ?: "FREE")

    Log.i("PLAN", "auto renew : " + data?.autoRenewing.toString())
    Log.i("PLAN", "token : " + data?.purchaseToken.toString())
    Log.i("PLAN", "active : " + data?.active.toString())

    val planText = when (plan) {
        UserPlan.FREE -> "GeoWav Free"
        UserPlan.PREMIUM -> "GeoWav Premium"
        UserPlan.PRO -> "GeoWav Pro"
    }

    val colors = when (plan) {
        UserPlan.FREE -> freePlanColors()
        UserPlan.PREMIUM -> premiumPlanColors()
        UserPlan.PRO -> proPlanColors()
    }

    val status = data?.let { getSubscriptionStatus(it) }

    val formattedPurchaseDate = remember(data?.purchaseTime) {
        data?.purchaseTime?.let {
            SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                .format(Date(it))
        } ?: ""
    }

    val formattedExpiryDate = remember(data?.expiryTime) {
        data?.expiryTime?.let {
            SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                .format(Date(it))
        } ?: ""
    }

    val subtitle = when {
        plan == UserPlan.FREE -> "Basic tracking · Limited history"

        status?.isExpired == true -> "Your subscription has ended"

        status?.isCancelled == true ->
            "Expires in ${status.daysRemaining} days"

        status != null ->
            "Renews on $formattedExpiryDate"

        else -> ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.border, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.bg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // 🔹 LEFT CONTENT
            Column {
                Text(
                    text = "Current Plan",
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = manrope,
                    letterSpacing = 0.8.sp
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = planText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = manrope,
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    color = if (status?.isExpired == true)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.outline,
                    fontSize = 12.sp,
                    fontFamily = manrope,
                )
            }

            // 🔥 RIGHT SIDE (better than "Active")
            if (plan != UserPlan.FREE) {

                val label = when {
                    status?.isExpired == true -> "Expired"
                    status?.isCancelled == true -> "Ending"
                    status?.isActive == true -> "Active"
                    else -> "Free"
                }

                Surface(
                    shape = RoundedCornerShape(25),
                    color = colors.text
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {

                        // Dot indicator
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(colors.buttonTextColor)
                        )

                        Text(
                            text = label,
                            color = colors.buttonTextColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = manrope,
                        )
                    }
                }
            }
        }
    }
}

//
//@Composable
//fun CurrentPlanCard(
//    plan: UserPlan
//) {
//
//    val planText = when (plan) {
//        UserPlan.FREE -> "GeoWav Free"
//        UserPlan.PREMIUM -> "GeoWav Premium"
//        UserPlan.PRO -> "GeoWav Pro"
//    }
//
//    val colors = when (plan) {
//        UserPlan.FREE -> freePlanColors()
//        UserPlan.PREMIUM -> premiumPlanColors()
//        UserPlan.PRO -> proPlanColors()
//    }
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .border(1.dp, colors.border, RoundedCornerShape(16.dp)),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(containerColor = colors.bg),
//        elevation = CardDefaults.cardElevation(0.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 12.dp, vertical = 14.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Column {
//                Text(
//                    text = "Current Plan",
//                    color = MaterialTheme.colorScheme.outline,
//                    fontSize = 12.sp,
//                    fontWeight = FontWeight.Medium,
//                    fontFamily = manrope,
//                    letterSpacing = 0.8.sp
//                )
//                Spacer(Modifier.height(4.dp))
//                Text(
//                    text = planText,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    fontFamily = manrope,
//                )
//                Spacer(Modifier.height(2.dp))
//                Text(
//                    text = "Basic tracking · Limited history",
//                    color = MaterialTheme.colorScheme.outline,
//                    fontSize = 12.sp,
//                    fontFamily = manrope,
//                )
//            }
//
//
//            if (plan != UserPlan.FREE) {
//                Surface(
//                    shape = RoundedCornerShape(25),
//                    color = colors.text
//                ) {
//                    Row(
//                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(5.dp)
//                    ) {
//                        Box(
//                            modifier = Modifier
//                                .size(6.dp)
//                                .clip(CircleShape)
//                                .background(colors.buttonTextColor)
//                        )
//
//                        Text(
//                            text = "Active",
//                            color = colors.buttonTextColor,
//                            fontSize = 12.sp,
//                            fontWeight = FontWeight.Medium,
//                            fontFamily = manrope,
//                        )
//                    }
//                }
//            }
//        }
//    }
//}


enum class BillingCycle { Monthly, Yearly }

@Composable
fun BillingToggle(
    selected: BillingCycle,
    onSelect: (BillingCycle) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        BillingCycle.entries.forEach { cycle ->
            val isSelected = selected == cycle
            TextButton(
                onClick = { onSelect(cycle) },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    ),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text(
                    text = cycle.name,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    fontFamily = manrope,
                )
                if (cycle == BillingCycle.Yearly) {
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)
                        else
                            MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Save 20%",
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                            fontFamily = manrope,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun PaywallHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column() {
            Text(
                text = "Unlock smarter\ntracking",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = manrope,
                lineHeight = 32.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Replay journeys, track longer, share with your people.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = manrope,
                lineHeight = 20.sp
            )
        }

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(42.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.credit_card),
                contentDescription = "card",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(24.dp)
                    .padding(8.dp)
            )
        }

    }
}


@Composable
fun PlanCard(
    title: String,
    price: String,
    billingLabel: String,
    features: List<String>,
    isFeatured: Boolean,
    isCurrentPlan: Boolean = false,
    buttonText: String,
    colors: PlanColors,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isFeatured) 2.dp else 1.dp,
                color = colors.border,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.bg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {


            if (isFeatured) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.heart_fill),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "Most popular",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = manrope,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }


            Text(
                text = title,
                color = colors.text,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = manrope,
            )


            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
            ) {
                Text(
                    text = price,
                    color = colors.text,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = manrope,
                )
                Text(
                    text = billingLabel,
                    color = colors.text.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    fontFamily = manrope,
                    modifier = Modifier.padding(bottom = 4.dp, start = 3.dp)
                )
            }


            features.forEach { feature ->
                FeatureRow(
                    text = feature,
                    checkTint = colors.checkTint,
                    textColor = colors.text.copy(alpha = 0.8f)
                )
            }

            Spacer(Modifier.height(16.dp))


            Button(
                onClick = onClick,
                enabled = !isCurrentPlan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.buttonBg,
                    contentColor = colors.buttonTextColor,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    disabledContentColor = MaterialTheme.colorScheme.outline
                )
            ) {
                Text(
                    text = buttonText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = manrope,
                )
            }
        }
    }
}


@Composable
fun FeatureRow(text: String, checkTint: Color, textColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(50))
                .background(checkTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.check),
                contentDescription = null,
                tint = checkTint,
                modifier = Modifier.size(11.dp)
            )
        }

        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            fontFamily = manrope,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 18.sp
        )
    }
}


private data class CompareRow(
    val feature: String, val free: String, val premium: String, val pro: String
)

@Composable
fun ComparisonTable() {
    val rows = listOf(
        CompareRow("Playback", "–", "✓", "✓"),
        CompareRow("Live Location Sharing", "up to 30 mins", "Unlimited", "Unlimited"),
        CompareRow("Session History", "Today", "2 days", "Full"),
        CompareRow("Places", "2", "10", "Unlimited"),
        CompareRow("Connections", "1", "5", "Unlimited"),
        CompareRow("Insights", "–", "–", "✓"),
        CompareRow("Stay point", "-", "–", "✓"),
    )

    Column(
        modifier = Modifier.padding(
            bottom = 16.dp
        )
    ) {
        Text(
            text = "Compare Plans",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            letterSpacing = 0.8.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = manrope,
            modifier = Modifier.padding(bottom = 12.dp)
        )


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text(
                "Features",
                modifier = Modifier.weight(1f),
                fontFamily = manrope,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            listOf(
                "Free" to MaterialTheme.colorScheme.onSurfaceVariant,
                "Premium" to MaterialTheme.colorScheme.primary,
                "Pro" to MaterialTheme.colorScheme.secondary
            ).forEach { (label, color) ->
                Text(
                    text = label,
                    color = color,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = manrope,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.3.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 0.5.dp
        )

        rows.forEachIndexed { index, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = row.feature,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = manrope,
                    modifier = Modifier.weight(1f)
                )
                listOf(
                    row.free to MaterialTheme.colorScheme.onSurfaceVariant,
                    row.premium to MaterialTheme.colorScheme.primary,
                    row.pro to MaterialTheme.colorScheme.secondary
                ).forEach { (value, color) ->
                    if (value == "✓") {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .size(24.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.check),
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    } else {
                        Text(
                            text = value,
                            color = color,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = manrope,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (index != rows.size - 1) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp
                )
            }
        }
    }
}


@Composable
fun StickyUpgradeCTA(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isEnabled: Boolean
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onClick,
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = "Upgrade to Premium",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                fontFamily = manrope,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cancel anytime ",
                color = MaterialTheme.colorScheme.outline,
                fontSize = 11.sp,
                fontFamily = manrope,
                textAlign = TextAlign.Center
            )

            Text(
                text = "· Secure payment",
                color = MaterialTheme.colorScheme.outline,
                fontSize = 11.sp,
                fontFamily = manrope,
                textAlign = TextAlign.Center
            )

            Icon(
                painter = painterResource(R.drawable.secure_payment),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(14.dp)
            )

        }
    }
}

//
//@Preview(showBackground = true)
//@Composable
//fun PaywallScreenPreview() {
//    GeoWavTheme(
//        darkTheme = isSystemInDarkTheme()
//    ) {
//        PaywallScreen(back = {}, onPremiumClick = {}, onProClick = {})
//    }
//}
