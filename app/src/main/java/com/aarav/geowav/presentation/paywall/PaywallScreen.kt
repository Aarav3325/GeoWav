package com.aarav.geowav.presentation.paywall

import android.app.Activity
import android.content.Intent
import android.icu.util.LocaleData
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.aarav.geowav.R
import com.aarav.geowav.core.managers.SubscriptionMapper
import com.aarav.geowav.core.utils.SubscriptionHelper
import com.aarav.geowav.core.utils.SubscriptionHelper.getSubscriptionStatus
import com.aarav.geowav.data.model.PurchaseResult
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.data.model.UserSubscription
import com.aarav.geowav.data.model.getPlanContent
import com.aarav.geowav.presentation.components.CustomBottomSheet
import com.aarav.geowav.presentation.components.PurchaseSuccessBottomSheet
import com.aarav.geowav.presentation.components.SnackbarManager
import com.aarav.geowav.presentation.components.UpgradeConfirmBottomSheet
import com.aarav.geowav.presentation.subscription.SubscriptionEvents
import com.aarav.geowav.presentation.subscription.SubscriptionViewModel
import com.aarav.geowav.presentation.theme.manrope
import com.aarav.geowav.presentation.theme.onSecondaryDark
import com.aarav.geowav.presentation.theme.secondaryContainerDark
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
    back: () -> Unit
) {


    val plan by subscriptionViewModel.userPlan.collectAsState()
    val purchaseResult by subscriptionViewModel.purchaseResult.collectAsState()
    val subscriptionState by subscriptionViewModel.subscriptionState.collectAsState()

    val availablePlans = SubscriptionHelper.getAvailablePlans(plan)

    var showUpgradeConfirm by remember { mutableStateOf(false) }
    var selectedPlan by remember { mutableStateOf<UserPlan?>(null) }


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

    val onPremiumClick = {
        selectedPlan = UserPlan.PREMIUM
        showUpgradeConfirm = true
    }

    val onProClick = {
        selectedPlan = UserPlan.PRO
        showUpgradeConfirm = true
    }

//    val productId = when (selectedPlan) {
//        UserPlan.PREMIUM -> SubscriptionMapper.PREMIUM_ID
//        UserPlan.PRO -> SubscriptionMapper.PRO_ID
//        else -> null
//    }

    if (showUpgradeConfirm && selectedPlan != null) {
        CustomBottomSheet(
            onDismissRequest = {
                showUpgradeConfirm = false
            }
        ) {
            UpgradeConfirmBottomSheet(
                plan = selectedPlan!!,
                onConfirm = {
                    showUpgradeConfirm = false
                    if (selectedPlan != null) {
                        activity?.let {
                            subscriptionViewModel.purchasePlan(
                                activity = it,
                                plan = selectedPlan!!
                            )
                        }
                    }
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        subscriptionViewModel.fetchSubscriptionStatus()
        subscriptionViewModel.uiEvents.collect { event ->
            when (event) {

                is SubscriptionEvents.PurchaseCancelled -> {
                    SnackbarManager.showMessage("Purchase cancelled")
                }

                is SubscriptionEvents.ShowError -> {
                    SnackbarManager.showMessage(event.message)
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
                    .padding(top = 12.dp, bottom = if (availablePlans.isEmpty()) 36.dp else 104.dp)
            ) {

                PaywallHeader()

                Spacer(Modifier.height(16.dp))

                CurrentPlanCard(subscriptionState)

                Spacer(Modifier.height(16.dp))

                if (availablePlans.contains(UserPlan.PREMIUM)) {
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
                        isCurrentPlan = false,
                        buttonText = "Upgrade to Premium",
                        colors = premiumPlanColors(),
                        badge = R.drawable.geowav_premium_badge,
                        onClick = onPremiumClick
                    )

                    Spacer(Modifier.height(12.dp))
                }

                if (availablePlans.contains(UserPlan.PRO)) {
                    PlanCard(
                        title = "GeoWav Pro",
                        price = "₹199",
                        billingLabel = "/month",
                        offerPrice = if (plan == UserPlan.PREMIUM) "₹149" else null,
                        features = listOf(
                            "Everything in Premium",
                            "Full location history",
                            "Stay point detection",
                            "Movement insights & analytics",
                            "Export & share trips"
                        ),
                        isFeatured = false,
                        isCurrentPlan = false,
                        buttonText = "Go Pro",
                        colors = proPlanColors(),
                        badge = R.drawable.geowav_pro_badge,
                        onClick = onProClick
                    )
                }

                if (availablePlans.isEmpty()) {
                    Spacer(Modifier.height(12.dp))

                    ProFeaturesCard()
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

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Restore Purchases",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { subscriptionViewModel.restorePurchases() },
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 13.sp,
                    fontFamily = manrope,
                    fontWeight = FontWeight.Medium
                )
            }

            val nextPlan = when (plan) {
                UserPlan.FREE -> UserPlan.PREMIUM
                UserPlan.PREMIUM -> UserPlan.PRO
                UserPlan.PRO -> null
            }



            nextPlan?.let { target ->

                val label = when (target) {
                    UserPlan.PREMIUM -> "Upgrade to Premium"
                    UserPlan.PRO -> "Go Pro"
                    else -> ""
                }

                val onClickAction = if (target == UserPlan.PREMIUM) {
                    onPremiumClick
                } else {
                    onProClick
                }

                StickyUpgradeCTA(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    onClick = onClickAction,
                    label = label,
                    isEnabled = true,
                    targetPlan = target
                )
            }

        }
    }
}

@Composable
fun ProFeaturesCard() {

    val colors = proPlanColors()

    val plan = getPlanContent(UserPlan.PRO)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.border, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.bg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.text.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.check),
                        contentDescription = null,
                        tint = colors.text,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = "Pro unlocked",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        fontFamily = manrope,
                        color = colors.text
                    )

                    Text(
                        text = "You now have full access",
                        fontSize = 12.sp,
                        fontFamily = manrope,
                        color = colors.text.copy(alpha = 0.7f)
                    )
                }
            }

            HorizontalDivider(
                color = colors.border.copy(alpha = 0.8f),
                thickness = 0.8.dp
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                plan.features.forEach {
                    ProFeatureItem(it, colors)
                }
            }
        }
    }
}

@Composable
fun ProFeatureItem(
    text: String,
    colors: PlanColors
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(50))
                .background(colors.checkTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.check),
                contentDescription = null,
                tint = colors.checkTint,
                modifier = Modifier.size(12.dp)
            )
        }

        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = manrope,
            color = colors.text.copy(alpha = 0.9f)
        )
    }
}

@Composable
fun CurrentPlanCard(
    subscription: UserSubscription?,
    modifier: Modifier = Modifier
) {

    val plan = UserPlan.valueOf(subscription?.plan ?: "FREE")

    Log.i("PLAN", "auto renew : " + subscription?.autoRenewing.toString())
    Log.i("PLAN", "token : " + subscription?.purchaseToken.toString())
    Log.i("PLAN", "active : " + subscription?.active.toString())

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

    val status = subscription?.let { getSubscriptionStatus(it) }

    val formattedPurchaseDate = remember(subscription?.purchaseTime) {
        subscription?.purchaseTime?.let {
            SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                .format(Date(it))
        } ?: ""
    }

    val formattedExpiryDate = remember(subscription?.expiryTime) {
        subscription?.expiryTime?.let {
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
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, colors.border, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.bg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

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

                Spacer(Modifier.height(20.dp))

                val context = LocalContext.current

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Manage Subscription",
                        fontFamily = manrope,
                        color = colors.text,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = "https://play.google.com/store/account/subscriptions".toUri()
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            if (plan != UserPlan.FREE) {

                val label = when {
                    status?.isExpired == true -> "Expired"
                    status?.isCancelled == true -> "Ending"
                    status?.active == true -> "Active"
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

@Composable
fun PaywallHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
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

        Spacer(Modifier.width(12.dp))

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
    offerPrice: String? = null,
    features: List<String>,
    isFeatured: Boolean,
    isCurrentPlan: Boolean = false,
    buttonText: String,
    colors: PlanColors,
    badge: Int,
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


//            if (isFeatured) {
//                Surface(
//                    shape = RoundedCornerShape(6.dp),
//                    color = MaterialTheme.colorScheme.primaryContainer
//                ) {
//                    Row(
//                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(4.dp)
//                    ) {
//                        Icon(
//                            painter = painterResource(R.drawable.heart_fill),
//                            contentDescription = null,
//                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
//                            modifier = Modifier.size(11.dp)
//                        )
//                        Text(
//                            text = "Most popular",
//                            color = MaterialTheme.colorScheme.onPrimaryContainer,
//                            fontSize = 12.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            fontFamily = manrope,
//                        )
//                    }
//                }
//                Spacer(Modifier.height(12.dp))
//            }

            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .padding(top = 6.dp, bottom = 0.dp)
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        color = colors.text,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = manrope,
                    )

                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = if (offerPrice != null) {
                                buildAnnotatedString {
                                    withStyle(
                                        SpanStyle(
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                    ) {
                                        append(price)
                                    }
                                    append(" ")
                                    append(offerPrice)
                                }
                            } else {
                                buildAnnotatedString {
                                    append(price)
                                }
                            },
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
                            modifier = Modifier
                                .padding(bottom = 4.dp, start = 3.dp)
                        )
                    }
                }


                Icon(
                    painter = painterResource(badge),
                    contentDescription = "badge",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(48.dp)
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
        CompareRow("Live Location Sharing", "up to 30 minutes", "Unlimited", "Unlimited"),
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
    label: String = "Upgrade",
    isEnabled: Boolean,
    targetPlan: UserPlan
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (targetPlan == UserPlan.PRO) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                onSecondaryDark,
                                secondaryContainerDark
                            )
                        )
                    )
            ) {
                Button(
                    onClick = onClick,
                    enabled = isEnabled,
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        text = label,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        fontFamily = manrope,
                        color = Color.White
                    )
                }
            }

        } else {

            val colors = premiumPlanColors()

            Button(
                onClick = onClick,
                enabled = isEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.buttonBg,
                    contentColor = colors.buttonTextColor
                )
            ) {
                Text(
                    text = label,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    fontFamily = manrope,
                )
            }
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