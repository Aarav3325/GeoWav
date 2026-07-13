package com.aarav.geowav.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.presentation.theme.manrope

@Composable
fun AppStateView(
    modifier: Modifier = Modifier,
    iconRes: Int? = null,
    title: String,
    description: String,
    primaryCtaText: String? = null,
    onPrimaryCtaClick: (() -> Unit)? = null,
    secondaryCtaText: String? = null,
    onSecondaryCtaClick: (() -> Unit)? = null,
    isLoading: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        } else if (iconRes != null) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = CircleShape
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .size(72.dp)
                        .padding(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            fontFamily = manrope,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            fontFamily = manrope,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (primaryCtaText != null && onPrimaryCtaClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onPrimaryCtaClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = primaryCtaText,
                    fontFamily = manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        if (secondaryCtaText != null && onSecondaryCtaClick != null) {
            Spacer(modifier = Modifier.height(if (primaryCtaText == null) 24.dp else 8.dp))
            OutlinedButton(
                onClick = onSecondaryCtaClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = secondaryCtaText,
                    fontFamily = manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

/**
 * Standard factory mapping from AppScreenState to AppStateView configuration.
 */
@Composable
fun AppStateScreen(
    state: AppScreenState<*>,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    onSettings: (() -> Unit)? = null,
    onUpgrade: (() -> Unit)? = null,
    customEmptyContent: (@Composable () -> Unit)? = null
) {
    when (state) {
        is AppScreenState.Loading -> {
            AppStateView(
                modifier = modifier,
                isLoading = true,
                title = "Please wait",
                description = "Preparing your information..."
            )
        }
        is AppScreenState.Empty -> {
            if (customEmptyContent != null) {
                customEmptyContent()
            } else {
                AppStateView(
                    modifier = modifier,
                    iconRes = R.drawable.empty,
                    title = "Nothing to display",
                    description = "No items or activity found here yet."
                )
            }
        }
        is AppScreenState.NoInternet -> {
            AppStateView(
                modifier = modifier,
                iconRes = R.drawable.link_break,
                title = "Connection unavailable",
                description = "Please check your network settings and try again.",
                primaryCtaText = "Try Again",
                onPrimaryCtaClick = onRetry
            )
        }
        is AppScreenState.Timeout -> {
            AppStateView(
                modifier = modifier,
                iconRes = R.drawable.restart,
                title = "Request timed out",
                description = "Taking longer than expected. Please try again.",
                primaryCtaText = "Retry",
                onPrimaryCtaClick = onRetry
            )
        }
        is AppScreenState.ServerError -> {
            AppStateView(
                modifier = modifier,
                iconRes = R.drawable.lock,
                title = "Server connection lost",
                description = "We couldn't reach our systems. Please try again in a moment.",
                primaryCtaText = "Try Again",
                onPrimaryCtaClick = onRetry
            )
        }
        is AppScreenState.UnknownError -> {
            AppStateView(
                modifier = modifier,
                iconRes = R.drawable.info,
                title = "Something went wrong",
                description = "Please try again or restart the application.",
                primaryCtaText = "Retry",
                onPrimaryCtaClick = onRetry
            )
        }
        is AppScreenState.PermissionRequired -> {
            val (icon, title, desc, btn) = when (state.type) {
                PermissionType.LOCATION -> Quadruple(
                    R.drawable.map_pin,
                    "Location permission needed",
                    "We need location access to notify your circle when you arrive at saved places.",
                    "Grant Access"
                )
                PermissionType.BACKGROUND_LOCATION -> Quadruple(
                    R.drawable.map_pin_area,
                    "Background location needed",
                    "Continuous sharing requires background location access so family can see you even when the app is closed.",
                    "Open Settings"
                )
                PermissionType.NOTIFICATIONS -> Quadruple(
                    R.drawable.bell,
                    "Notifications disabled",
                    "Turn on notifications to receive alerts when your loved ones arrive or depart.",
                    "Enable Notifications"
                )
                PermissionType.GPS -> Quadruple(
                    R.drawable.gps,
                    "GPS is disabled",
                    "Turn on device GPS location services to pinpoint your coordinates on the map.",
                    "Turn On GPS"
                )
                PermissionType.BATTERY_OPTIMIZATION -> Quadruple(
                    R.drawable.info,
                    "Disable battery saver",
                    "To ensure accurate and timely location sharing, please exclude GeoWav from battery optimizations.",
                    "Configure"
                )
                PermissionType.FOREGROUND_SERVICE -> Quadruple(
                    R.drawable.bug_droid,
                    "Service permission needed",
                    "Foreground service permissions are required for accurate tracking and sharing.",
                    "Grant Permission"
                )
            }
            AppStateView(
                modifier = modifier,
                iconRes = icon,
                title = title,
                description = desc,
                primaryCtaText = btn,
                onPrimaryCtaClick = onSettings
            )
        }
        is AppScreenState.PremiumRequired -> {
            AppStateView(
                modifier = modifier,
                iconRes = R.drawable.geowav_premium_badge,
                title = "GeoWav Premium feature",
                description = "Upgrade to unlock ${state.featureName} and experience full connectivity.",
                primaryCtaText = "View Plans",
                onPrimaryCtaClick = onUpgrade
            )
        }
        is AppScreenState.NoSearchResults -> {
            AppStateView(
                modifier = modifier,
                iconRes = R.drawable.search,
                title = "No search results",
                description = "Try searching with a different place or address.",
                primaryCtaText = "Search Again",
                onPrimaryCtaClick = onRetry
            )
        }
        is AppScreenState.FeatureUnavailable -> {
            AppStateView(
                modifier = modifier,
                iconRes = R.drawable.info,
                title = "Feature unavailable",
                description = "This option is currently not available in your region.",
                primaryCtaText = "Go Back",
                onPrimaryCtaClick = onRetry
            )
        }
        is AppScreenState.Success -> {
            // Success state is rendered inside the screen's main content, not here.
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
