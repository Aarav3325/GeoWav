package com.aarav.geowav.presentation.paywall

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.TextStyle
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
import com.aarav.geowav.data.model.PaywallConfig
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
import com.aarav.geowav.presentation.theme.GeoWavThemeExtras
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
    bg = MaterialTheme.colorScheme.surfaceContainerLow,
    border = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    text = MaterialTheme.colorScheme.onSurfaceVariant,
    buttonBg = MaterialTheme.colorScheme.surfaceContainerHigh,
    buttonTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    checkTint = MaterialTheme.colorScheme.outline,
)

@Composable
fun premiumPlanColors() = PlanColors(
    bg = GeoWavThemeExtras.colors.periwinkleTintedSurface,
    border = GeoWavThemeExtras.colors.periwinkle.copy(alpha = 0.5f),
    text = GeoWavThemeExtras.colors.periwinkle,
    buttonBg = GeoWavThemeExtras.colors.periwinkle,
    buttonTextColor = GeoWavThemeExtras.colors.periwinkleTintedSurface,
    checkTint = GeoWavThemeExtras.colors.periwinkle,
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
    val paywallConfig by subscriptionViewModel.paywallConfig.collectAsState()
    val offeringState by subscriptionViewModel.offeringState.collectAsState()

    val availablePlans = SubscriptionHelper.getAvailablePlans(plan)

    val premiumPackage = remember(offeringState.allPackages) {
        offeringState.allPackages.find {
            it.identifier == "premium_monthly" || 
            it.identifier.contains("premium", ignoreCase = true) || 
            it.product.id.contains("premium", ignoreCase = true)
        }
    }
    val proPackage = remember(offeringState.allPackages) {
        offeringState.allPackages.find {
            it.identifier == "pro_monthly" || 
            it.identifier.contains("pro", ignoreCase = true) || 
            it.product.id.contains("pro", ignoreCase = true)
        }
    }

    val premiumPrice = premiumPackage?.product?.price?.formatted ?: "₹99"
    val proPrice = proPackage?.product?.price?.formatted ?: "₹199"

    val proOfferPrice = if (plan == UserPlan.PREMIUM) {
            if (proPrice == "₹199" || proPrice.contains("199")) "₹149" else null
        } else null

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
                currentPlan = plan,
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
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontFamily = manrope,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    )
                },
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                navigationIcon = {
                    IconButton(onClick = back) {
                        Icon(
                            painter = painterResource(R.drawable.back),
                            contentDescription = "back",
                            modifier = Modifier.size(IconButtonDefaults.smallIconSize),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .height(8.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = if (availablePlans.isEmpty()) 48.dp else 140.dp)
            ) {

                PaywallHeader(config = paywallConfig, showTrialMessage = plan != UserPlan.PRO)

                Spacer(Modifier.height(8.dp))

                CurrentPlanCard(subscriptionState)

                Spacer(Modifier.height(20.dp))

                if (availablePlans.contains(UserPlan.PREMIUM)) {
                    PlanCard(
                        title = "GeoWav Premium",
                        price = premiumPrice,
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

                    Spacer(Modifier.height(16.dp))
                }

                if (availablePlans.contains(UserPlan.PRO)) {
                    PlanCard(
                        title = "GeoWav Pro",
                        price = proPrice,
                        billingLabel = "/month",
                        offerPrice = proOfferPrice,
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
                    Spacer(Modifier.height(16.dp))

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

                Spacer(Modifier.height(28.dp))

                ComparisonTable()

                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { subscriptionViewModel.restorePurchases() },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Restore Purchases",
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        letterSpacing = 0.25.sp
                    )
                }

                Spacer(Modifier.height(20.dp))
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
    val plan = getPlanContent(UserPlan.PRO)
    val colors = proPlanColors()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = colors.border,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.bg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.checkTint.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.geowav_pro_badge),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.matchParentSize()
                    )
                }

                Column {
                    Text(
                        text = "GeoWav Pro is active",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = manrope,
                        color = colors.text
                    )

                    Text(
                        text = "You now have full access to all features",
                        fontSize = 12.sp,
                        fontFamily = manrope,
                        color = colors.text.copy(alpha = 0.7f)
                    )
                }
            }

            HorizontalDivider(
                color = colors.border.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                plan.features.forEach {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(colors.checkTint.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.check),
                                contentDescription = null,
                                tint = colors.checkTint,
                                modifier = Modifier.size(10.dp)
                            )
                        }

                        Text(
                            text = it,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = manrope,
                            color = colors.text.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
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

    val status = subscription?.let { getSubscriptionStatus(it) }

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
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CURRENT PLAN",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = manrope,
                    letterSpacing = 1.5.sp
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = planText,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = manrope,
                    )

                    if (plan != UserPlan.FREE) {
                        val label = when {
                            status?.isExpired == true -> "Expired"
                            status?.isCancelled == true -> "Ending"
                            status?.active == true -> "Active"
                            else -> "Free"
                        }

                        val badgeColor = when {
                            status?.isExpired == true -> MaterialTheme.colorScheme.error
                            status?.isCancelled == true -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.primary
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = badgeColor.copy(alpha = 0.08f)
                        ) {
                            Text(
                                text = label.uppercase(Locale.getDefault()),
                                color = badgeColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = manrope,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    color = if (status?.isExpired == true)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    fontFamily = manrope,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(16.dp))

                val context = LocalContext.current

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = "https://play.google.com/store/account/subscriptions".toUri()
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        text = "Manage Subscription",
                        fontFamily = manrope,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(R.drawable.right_arrow),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PaywallHeader(config: PaywallConfig, showTrialMessage: Boolean) {
    val title = if (config.launchOfferEnabled) config.title else "Unlock smarter tracking"
    val subtitle = if (config.launchOfferEnabled) config.subtitle else "Replay journeys, track longer, and share with your inner circle."
    val displayTrial = showTrialMessage && config.launchOfferEnabled && config.trialMessage.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (config.launchOfferEnabled && config.showLaunchBadge && config.launchBadgeText.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    )
                )),
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                Text(
                    text = config.launchBadgeText.uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = manrope,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    letterSpacing = 1.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.04f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.credit_card),
                    contentDescription = "Premium Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (config.launchOfferEnabled) {
            Text(
                text = title,
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                ),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = manrope,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp,
                modifier = Modifier.fillMaxWidth(0.95f)
            )
        } else {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = manrope,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp,
                modifier = Modifier.fillMaxWidth(0.95f)
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            fontSize = 14.sp,
            fontFamily = manrope,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.fillMaxWidth(0.9f)
        )

        if (displayTrial) {
            Spacer(Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.geowav_pro_badge),
                        contentDescription = "Trial Badge",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = config.trialMessage,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = manrope,
                        textAlign = TextAlign.Center
                    )
                }
            }
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
    val isPro = title.contains("Pro", ignoreCase = true)

    val cardBgColor = colors.bg

    val cardBorderColor = if (isFeatured || isPro) {
        colors.border
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    }

    val titleColor = colors.text
    val priceColor = colors.text
    val featureTextColor = colors.text.copy(alpha = 0.85f)
    val checkIconColor = colors.checkTint
    val checkBgColor = colors.checkTint.copy(alpha = 0.08f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isPro || isFeatured) 1.5.dp else 1.dp,
                color = cardBorderColor,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            if (isPro) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = colors.checkTint.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Text(
                            text = "MOST VALUE",
                            color = colors.text,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = manrope,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            letterSpacing = 1.sp
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            } else if (isFeatured) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = colors.checkTint.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = "POPULAR",
                            color = colors.text,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = manrope,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            letterSpacing = 1.sp
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = titleColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = manrope,
                    )

                    Spacer(Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = if (offerPrice != null) {
                                buildAnnotatedString {
                                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
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
                            color = priceColor,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = manrope,
                        )
                        Text(
                            text = billingLabel,
                            color = colors.text.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            fontFamily = manrope,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                        )
                    }
                }

                Icon(
                    painter = painterResource(badge),
                    contentDescription = "badge",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(
                color = colors.border.copy(alpha = 0.2f),
                thickness = 1.dp
            )
            Spacer(Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                features.forEach { feature ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(checkBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.check),
                                contentDescription = null,
                                tint = checkIconColor,
                                modifier = Modifier.size(10.dp)
                            )
                        }

                        Text(
                            text = feature,
                            color = featureTextColor,
                            fontSize = 14.sp,
                            fontFamily = manrope,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onClick,
                enabled = !isCurrentPlan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
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
                    fontSize = 15.sp,
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
        modifier = Modifier.padding(bottom = 24.dp)
    ) {
        Text(
            text = "Compare Plans",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = manrope,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Features",
                        modifier = Modifier.weight(1.3f),
                        fontFamily = manrope,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    listOf(
                        "Free" to MaterialTheme.colorScheme.onSurfaceVariant,
                        "Premium" to GeoWavThemeExtras.colors.periwinkle,
                        "Pro" to MaterialTheme.colorScheme.secondary
                    ).forEach { (label, color) ->
                        Text(
                            text = label,
                            color = color,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = manrope,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    thickness = 1.dp
                )

                rows.forEachIndexed { index, row ->
                    val rowBg = if (index % 2 == 0) {
                        Color.Transparent
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.3f)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(rowBg)
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = row.feature,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = manrope,
                            modifier = Modifier.weight(1.3f)
                        )

                        listOf(
                            row.free to MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            row.premium to GeoWavThemeExtras.colors.periwinkle,
                            row.pro to MaterialTheme.colorScheme.secondary
                        ).forEach { (value, color) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                if (value == "✓") {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(color.copy(alpha = 0.08f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.check),
                                            contentDescription = null,
                                            tint = color,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = value,
                                        color = color,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = manrope,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    if (index != rows.size - 1) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                            thickness = 0.8.dp
                        )
                    }
                }
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
    val surfaceColor = MaterialTheme.colorScheme.surface
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(surfaceColor)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
            thickness = 0.8.dp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val buttonBgColor = if (targetPlan == UserPlan.PRO) {
            proPlanColors().buttonBg
        } else {
            premiumPlanColors().buttonBg
        }

        val buttonTextColor = if (targetPlan == UserPlan.PRO) {
            proPlanColors().buttonTextColor
        } else {
            premiumPlanColors().buttonTextColor
        }

        Button(
            onClick = onClick,
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonBgColor,
                contentColor = buttonTextColor
            )
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                fontFamily = manrope,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cancel anytime · Secure payment",
                color = MaterialTheme.colorScheme.outline,
                fontSize = 11.sp,
                fontFamily = manrope,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Icon(
                painter = painterResource(R.drawable.secure_payment),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(13.dp)
            )
        }
    }
}