@file:Suppress("InlinedApi")

package com.aarav.geowav.presentation.onboard

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.presentation.theme.manrope
import com.aarav.geowav.presentation.theme.onPrimaryDark
import com.aarav.geowav.presentation.theme.primaryDark
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OnboardingScreen(
    navigateToAuth: () -> Unit,
    onBoardVM: OnBoardVM,
) {
    val uiState by onBoardVM.uiState.collectAsState()
    val pages = uiState.pages
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pages.size }
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        onBoardVM.onPageChanged(pagerState.currentPage)
    }

    LaunchedEffect(uiState.isOnboardingComplete) {
        if (uiState.isOnboardingComplete) {
            navigateToAuth()
        }
    }

    if (uiState.showPermissionSetup) {
        PermissionSetupSheet(
            onDismiss = onBoardVM::onPermissionSetupDismiss,
            onFineLocationChanged = onBoardVM::onFineLocationResult,
            onBackgroundLocationChanged = onBoardVM::onBackgroundLocationResult,
            onContinue = { skipped -> onBoardVM.completeOnboarding(skipped) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPageContent(index = page, page = pages[page])
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(uiState.currentPage != pages.lastIndex) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    pages.lastIndex,
                                    animationSpec = TweenSpec(durationMillis = 400)
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Skip setup",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = manrope,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                AnimatedVisibility(
                    modifier = Modifier.weight(1f),
                    visible = uiState.currentPage != pages.lastIndex
                ) {
                    DotsIndicator(
                        modifier = Modifier.weight(1f),
                        currentPage = pagerState.currentPage,
                        totalDots = pages.size
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                FilledTonalButton(
                    onClick = {
                        if (uiState.currentPage == pages.lastIndex) {
                            onBoardVM.onContinueClicked()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    pagerState.currentPage + 1,
                                    animationSpec = TweenSpec(durationMillis = 350)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = if (uiState.currentPage == pages.lastIndex) {
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    } else {
                        Modifier.height(48.dp)
                    }
                ) {
                    Text(
                        text = if (uiState.currentPage == pages.lastIndex) {
                            "Set up access"
                        } else {
                            "Next"
                        },
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PermissionSetupSheet(
    onDismiss: () -> Unit,
    onFineLocationChanged: (Boolean) -> Unit,
    onBackgroundLocationChanged: (Boolean) -> Unit,
    onContinue: (skipped: Boolean) -> Unit
) {
    val notificationPermission = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    val fineLocationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val backgroundLocationPermission =
        rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    val notificationsGranted =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || notificationPermission.status.isGranted
    val fineLocationGranted = fineLocationPermission.status.isGranted
    val backgroundLocationGranted = backgroundLocationPermission.status.isGranted
    val anyMissing = !notificationsGranted || !fineLocationGranted || !backgroundLocationGranted
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(fineLocationGranted) {
        onFineLocationChanged(fineLocationGranted)
    }

    LaunchedEffect(backgroundLocationGranted) {
        onBackgroundLocationChanged(backgroundLocationGranted)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 18.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Choose what GeoWav can use",
                fontFamily = manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Each permission supports a specific safety feature. You can continue without enabling everything.",
                fontFamily = manrope,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            PermissionSetupRow(
                title = "Notifications",
                description = "Invites, place alerts, sharing changes, and emergency activity.",
                granted = notificationsGranted,
                icon = R.drawable.bell,
                actionText = "Enable alerts",
                onAction = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermission.launchPermissionRequest()
                    }
                }
            )

            PermissionSetupRow(
                title = "Live location",
                description = "Shows realtime movement during active sharing and safety sessions.",
                granted = fineLocationGranted,
                icon = R.drawable.map_pin,
                actionText = "Enable live updates",
                onAction = { fineLocationPermission.launchPermissionRequest() }
            )

            PermissionSetupRow(
                title = "Background access",
                description = "Keeps active sessions and place alerts working when GeoWav is not open.",
                granted = backgroundLocationGranted,
                enabled = fineLocationGranted,
                icon = R.drawable.gps,
                actionText = "Set up background access",
                disabledText = "Enable live location first",
                onAction = { backgroundLocationPermission.launchPermissionRequest() }
            )

            FilledTonalButton(
                onClick = { onContinue(anyMissing) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = if (anyMissing) "Continue for now" else "Continue",
                    fontFamily = manrope
                )
            }

            TextButton(
                onClick = { onContinue(true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text(
                    text = "Review later",
                    fontFamily = manrope,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PermissionSetupRow(
    title: String,
    description: String,
    granted: Boolean,
    icon: Int,
    actionText: String,
    onAction: () -> Unit,
    enabled: Boolean = true,
    disabledText: String = actionText
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp)
            ),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = if (granted) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    },
                    shape = CircleShape
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = if (granted) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .padding(7.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontFamily = manrope,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (granted) "Enabled" else "Optional",
                        fontFamily = manrope,
                        fontSize = 11.sp,
                        color = if (granted) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Text(
                text = description,
                fontFamily = manrope,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!granted) {
                OutlinedButton(
                    onClick = onAction,
                    enabled = enabled,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (enabled) actionText else disabledText,
                        fontFamily = manrope,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingPageContent(
    index: Int,
    page: OnBoardingPage
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "GeoWav",
            fontSize = 34.sp,
            fontFamily = manrope,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier
                    .size(128.dp)
                    .align(Alignment.Center),
                color = if (index == 0) primaryDark else MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape,
            ) {
                Image(
                    painter = painterResource(id = page.imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(if(index == 0) 14.dp else 28.dp),
                    colorFilter = ColorFilter.tint(
                        if (index == 0) onPrimaryDark else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = page.title,
            fontFamily = manrope,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            fontFamily = manrope,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp
        )

        page.reassurance?.let {
            Spacer(modifier = Modifier.height(18.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    fontFamily = manrope,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DotsIndicator(
    modifier: Modifier = Modifier,
    currentPage: Int,
    totalDots: Int
) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalDots) { index ->
            val width = if (index == currentPage) 25.dp else 8.dp
            val color =
                if (index == currentPage) MaterialTheme.colorScheme.onBackground
                else MaterialTheme.colorScheme.outlineVariant
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(width = width, height = 5.dp)
                    .background(color, RoundedCornerShape(16.dp))
            )
        }
    }
}
