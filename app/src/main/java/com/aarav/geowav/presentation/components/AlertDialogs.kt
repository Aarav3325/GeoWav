package com.aarav.geowav.presentation.components

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
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
import com.aarav.geowav.presentation.theme.manrope
import com.aarav.geowav.presentation.theme.sora
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
            Icon(
                painter = painterResource(R.drawable.navigation_arrow),
                contentDescription = "Location icon",
                tint = MaterialTheme.colorScheme.tertiary
            )
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
                    fontFamily = sora
                )
                Text(
                    text = "\nPermissions requested:",
                    fontFamily = sora
                )
                Text(
                    text = "• Precise location (while using the app)\n" +
                            "• Background location (even when the app is closed or not in use)",
                    fontFamily = sora
                )
                Text(
                    text = "\nWhen asked, please choose “Allow all the time” to enable background location access.",
                    fontFamily = sora
                )
                Text(
                    text = "\nYou can change these permissions anytime in your device settings.",
                    fontFamily = sora
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
fun MyAlertDialog(
    modifier: Modifier = Modifier,
    shouldShowDialog: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
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
                    painter = painterResource(id = R.drawable.bug_droid),
                    contentDescription = "Error icon",
                    tint = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = message,
                    fontFamily = sora,
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
            containerColor = MaterialTheme.colorScheme.surfaceTint,
            modifier = modifier,
            onDismissRequest = onConfirmClick,
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
            text = {
                Text(
                    text = message,
                    fontFamily = sora,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            title = {
                Text(
                    text = title,
                    fontFamily = manrope,
                    color = MaterialTheme.colorScheme.background,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            icon = {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = "info icon",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(56.dp)
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
            containerColor = MaterialTheme.colorScheme.surfaceTint,
            modifier = modifier,
            onDismissRequest = onConfirmClick,
            confirmButton = {
                FilledTonalButton(
                    onClick = onConfirmClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(
                        text = "Enable Location",
                        fontFamily = manrope
                    )
                }
            },
            title = {
                Text(
                    text = "Location access required",
                    fontFamily = manrope,
                    color = MaterialTheme.colorScheme.background,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "GeoWav needs location access to detect entry, exit, and safety alerts even when the app is not open.",
                    fontFamily = sora,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            icon = {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "location icon",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(56.dp)
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
            containerColor = MaterialTheme.colorScheme.surfaceTint,
            modifier = modifier,
            onDismissRequest = onAcceptClick,
            confirmButton = {
                FilledTonalButton(
                    onClick = onAcceptClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(
                        text = "Accept & Continue",
                        fontFamily = manrope
                    )
                }
            },
            title = {
                Text(
                    text = "Terms & Conditions",
                    fontFamily = manrope,
                    color = MaterialTheme.colorScheme.background,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "By continuing, you agree to GeoWav’s Terms & Conditions and Privacy Policy. GeoWav uses location data to provide safety alerts and location-based features.",
                    fontFamily = sora,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            icon = {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Image(
                        painter = painterResource(R.drawable.files),
                        contentDescription = "terms icon",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                        modifier = Modifier.size(56.dp)
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
                    fontFamily = sora,
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
                        modifier = Modifier.size(34.dp)
                            .padding(4.dp)
                    )
                }
            }
        )
    }
}


