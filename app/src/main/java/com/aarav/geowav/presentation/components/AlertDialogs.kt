@file:SuppressLint("InlinedApi")

package com.aarav.geowav.presentation.components

import android.annotation.SuppressLint

import android.Manifest
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.aarav.geowav.R
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.data.model.PaywallConfig
import com.aarav.geowav.presentation.theme.manrope
import com.aarav.geowav.presentation.theme.GeoWavTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PermissionAlertDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onPermissionsGranted: () -> Unit
) {
    if (!showDialog) return

    val context = LocalContext.current
    val fineLocationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val backgroundLocationPermission =
        rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    var requestingBackground by remember { mutableStateOf(false) }

    fun requestFineLocation() {
        fineLocationPermission.launchPermissionRequest()
    }

    LaunchedEffect(fineLocationPermission.status) {
        if (fineLocationPermission.status.isGranted) {
            requestingBackground = true
        }
    }

    LaunchedEffect(requestingBackground) {
        if (requestingBackground && !backgroundLocationPermission.status.isGranted) {
            backgroundLocationPermission.launchPermissionRequest()
        }
    }

    // Observe final result
    LaunchedEffect(fineLocationPermission.status, backgroundLocationPermission.status) {
        if (fineLocationPermission.status.isGranted && backgroundLocationPermission.status.isGranted) {
            Toast.makeText(context, "Permissions granted", Toast.LENGTH_SHORT).show()
            onDismiss()
            onPermissionsGranted()
        }
    }

    AlertDialog(
        icon = {
            Surface(
                color = Color(0xFFBAC3FF),
                shape = CircleShape
            ) {
                Icon(
                    painter = painterResource(R.drawable.new_logo),
                    contentDescription = "logo",
                    tint = Color(0xFF222C61),
                    modifier = Modifier.size(56.dp),
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        title = {
            Text(
                text = "Location Access Required",
                fontFamily = manrope,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Text(
                    text = "GeoWav needs access to your location to provide accurate tracking and nearby service updates.",
                    fontFamily = manrope
                )
                Text(
                    text = "\nPermissions requested:",
                    fontFamily = manrope
                )
                Text(
                    text = "• Precise location (while using the app)\n" +
                            "• Background location (even when the app is closed or not in use)",
                    fontFamily = manrope
                )
                Text(
                    text = "\nWhen asked, please choose “Allow all the time” to enable background location access.",
                    fontFamily = manrope
                )
                Text(
                    text = "\nYou can change these permissions anytime in your device settings.",
                    fontFamily = manrope
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { requestFineLocation() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Continue", fontFamily = manrope)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(
                    "Cancel",
                    fontFamily = manrope,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        shape = RoundedCornerShape(12.dp),
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        ),
        onDismissRequest = { onDismiss() }
    )
}

@Composable
fun DeleteDialog(
    modifier: Modifier = Modifier,
    shouldShowDialog: Boolean,
    onDismissRequest: () -> Unit,
    dismissButtonText: String?,
    onDismissClick: () -> Unit,
    title: String,
    icon: Int = R.drawable.trash,
    message: String,
    confirmButtonText: String,
    onConfirmClick: () -> Unit,
) {
    if (shouldShowDialog) {
        AlertDialog(
            modifier = modifier,
            onDismissRequest = onDismissRequest,
            confirmButton = {
                FilledTonalButton(
                    onClick = onConfirmClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(confirmButtonText, fontFamily = manrope)
                }
            },
            dismissButton = {
                dismissButtonText?.let {
                    FilledTonalButton(
                        onClick = onDismissClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(dismissButtonText, fontFamily = manrope)
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    text = title,
                    fontFamily = manrope,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            icon = {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = "Error icon",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = message,
                    fontFamily = manrope,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        )
    }
}

@Composable
fun MyAlertDialog(
    modifier: Modifier = Modifier,
    shouldShowDialog: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    icon: Int = R.drawable.bug_droid,
    message: String,
    confirmButtonText: String,
    onConfirmClick: () -> Unit,
) {
    if (shouldShowDialog) {
        AlertDialog(
            modifier = modifier,
            onDismissRequest = onDismissRequest,
            confirmButton = {
                FilledTonalButton(
                    onClick = onConfirmClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(confirmButtonText, fontFamily = manrope)
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    text = title,
                    fontFamily = manrope,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            icon = {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = "Error icon",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = message,
                    fontFamily = manrope,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AboutDialog(
    showAboutDialog: Boolean,
    confirmButtonText: String,
    onConfirmClick: () -> Unit,
    title: String,
    icon: Int,
    message: String,
    modifier: Modifier = Modifier
) {
    if (showAboutDialog) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            modifier = modifier,
            onDismissRequest = onConfirmClick,
            confirmButton = {
                FilledTonalButton(
                    onClick = onConfirmClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(confirmButtonText, fontFamily = manrope)
                }
            },
            text = {
                Text(
                    text = message,
                    fontFamily = manrope,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            title = {
                Text(
                    text = title,
                    fontFamily = manrope,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            icon = {
                Surface(
                    color = Color(0xFFBAC3FF),
                    shape = CircleShape
                ) {
                    Icon(
                        painter = painterResource(R.drawable.new_logo),
                        contentDescription = "logo",
                        tint = Color(0xFF222C61),
                        modifier = Modifier.size(56.dp),
                    )
                }
            }
        )
    }
}

@Composable
fun LocationPermissionDialog(
    showDialog: Boolean,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (showDialog) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            modifier = modifier,
            onDismissRequest = onConfirmClick,
            confirmButton = {
                FilledTonalButton(
                    onClick = onConfirmClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text(
                        text = "Enable Location",
                        fontFamily = manrope,
                    )
                }
            },
            title = {
                Text(
                    text = "Location access required",
                    fontFamily = manrope,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "GeoWav needs location access to detect entry, exit, and safety alerts even when the app is not open.",
                    fontFamily = manrope,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            icon = {
                Surface(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = CircleShape
                ) {
                    Icon(
                        painter = painterResource(R.drawable.map_pin),
                        contentDescription = "location icon",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier
                            .size(56.dp)
                            .padding(8.dp)
                    )
                }
            }
        )
    }
}

@Composable
fun TermsAndConditionsDialog(
    showDialog: Boolean,
    onAcceptClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (showDialog) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = modifier,
            onDismissRequest = onAcceptClick,
            confirmButton = {
                FilledTonalButton(
                    onClick = onAcceptClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Text(
                        text = "Accept & Continue",
                        fontFamily = manrope,
                    )
                }
            },
            title = {
                Text(
                    text = "Terms & Conditions",
                    fontFamily = manrope,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "By continuing, you agree to GeoWav's Terms & Conditions and Privacy Policy. GeoWav uses location data to provide safety alerts and location-based features.",
                    fontFamily = manrope,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            icon = {
                Surface(
                    color = MaterialTheme.colorScheme.tertiary,
                    shape = CircleShape
                ) {
                    Image(
                        painter = painterResource(R.drawable.files),
                        contentDescription = "terms icon",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onTertiary),
                        modifier = Modifier
                            .size(56.dp)
                            .padding(8.dp)
                    )
                }
            }
        )
    }
}

@Composable
fun EmergencyShareDialog(
    showDialog: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Emergency Live Location Sharing",
                    fontFamily = manrope,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "This will share your live location with all loved ones for 15 minutes.",
                    fontFamily = manrope,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(
                        text = "Start Emergency Share",
                        fontFamily = manrope,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = "Cancel",
                        fontFamily = manrope,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            icon = {
                Surface(
                    color = MaterialTheme.colorScheme.error,
                    shape = CircleShape
                ) {
                    Image(
                        painter = painterResource(R.drawable.emergency),
                        contentDescription = "emergency icon",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onError),
                        modifier = Modifier
                            .size(34.dp)
                            .padding(4.dp)
                    )
                }
            }
        )
    }
}

@Composable
fun NotificationDisabledDialog(onConfirmClick: () -> Unit, onDismiss: () -> Unit) {

    AlertDialog(
        onDismissRequest = {},
        title = { Text("Enable Notifications") },
        text = {
            Text(
                "GeoWav needs notifications to alert you about circle updates, location sharing and emergencies.",
                fontFamily = manrope,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    "Close",
                    fontFamily = manrope
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(
                    "Open Settings",
                    fontFamily = manrope
                )
            }
        }
    )
}

@Composable
fun TrialOfferDialogContent(
    config: PaywallConfig,
    onDismiss: () -> Unit,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                ) {
                    Image(
                        painter = painterResource(R.drawable.prism),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    ),
                                    startY = 60f
                                )
                            )
                    )
                }

                val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.12f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_scale"
                )
                val glowAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.15f,
                    targetValue = 0.35f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glow_alpha"
                )

                Box(
                    modifier = Modifier
                        .offset(y = (-32).dp)
                        .size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .scale(pulseScale)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha),
                                CircleShape
                            )
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(64.dp)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                CircleShape
                            )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.geowav_pro_badge),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .offset(y = (-24).dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = config.trialMessage.ifBlank { "7-Day Free Trial" },
                        fontFamily = manrope,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        fontSize = 22.sp,
                        letterSpacing = (-0.3).sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = config.title,
                        fontFamily = manrope,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = config.subtitle,
                        fontFamily = manrope,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onClaim()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(percent = 50),
                        contentPadding = PaddingValues(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(percent = 50)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = config.launchBadgeText.ifBlank { "Claim Offer" },
                                fontFamily = manrope,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Maybe Later",
                            fontFamily = manrope,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(32.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                        CircleShape
                    )
            ) {
                Icon(
                    painter = painterResource(R.drawable.close),
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun TrialOfferDialog(
    showDialog: Boolean,
    config: PaywallConfig,
    onDismiss: () -> Unit,
    onClaim: () -> Unit
) {
    if (!showDialog) return

    AnimatedVisibility(
        visible = true,
        enter = scaleIn(
            initialScale = 0.9f,
            animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(durationMillis = 220)),
        exit = scaleOut(
            targetScale = 0.9f,
            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(durationMillis = 150)),
    ) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                TrialOfferDialogContent(
                    config = config,
                    onDismiss = onDismiss,
                    onClaim = onClaim
                )
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun TrialOfferDialogPreview() {
    GeoWavTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            TrialOfferDialogContent(
                config = PaywallConfig(
                    offeringId = "default",
                    title = "Claim Free Trial Now",
                    subtitle = "Claim GeoWav Pro celebrate our launch",
                    launchOfferEnabled = true,
                    showLaunchBadge = true,
                    launchBadgeText = "Claim Now",
                    trialMessage = "7-Day Free Trial. Cancel Anytime."
                ),
                onDismiss = {},
                onClaim = {}
            )
        }
    }
}
