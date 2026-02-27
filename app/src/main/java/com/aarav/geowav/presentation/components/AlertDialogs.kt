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
                    fontFamily = sora,
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
            containerColor = MaterialTheme.colorScheme.primaryFixed,
            modifier = modifier,
            onDismissRequest = onConfirmClick,
            confirmButton = {
                FilledTonalButton(
                    onClick = onConfirmClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimaryFixed,
                        contentColor = MaterialTheme.colorScheme.primaryFixed
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
                    color = MaterialTheme.colorScheme.onPrimaryFixed,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            title = {
                Text(
                    text = title,
                    fontFamily = manrope,
                    color = MaterialTheme.colorScheme.onPrimaryFixed,
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
            containerColor = MaterialTheme.colorScheme.primaryFixed,
            modifier = modifier,
            onDismissRequest = onConfirmClick,
            confirmButton = {
                FilledTonalButton(
                    onClick = onConfirmClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimaryFixed,
                        contentColor = MaterialTheme.colorScheme.primaryFixed
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
                    color = MaterialTheme.colorScheme.onPrimaryFixed,
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
                    color = MaterialTheme.colorScheme.onPrimaryFixed,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            icon = {
                Surface(
                    color = MaterialTheme.colorScheme.onPrimaryFixed,
                    shape = CircleShape
                ) {
                    Icon(
                        painter = painterResource(R.drawable.map_pin),
                        contentDescription = "location icon",
                        tint = MaterialTheme.colorScheme.primaryFixed,
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
            containerColor = MaterialTheme.colorScheme.primaryFixed,
            modifier = modifier,
            onDismissRequest = onAcceptClick,
            confirmButton = {
                FilledTonalButton(
                    onClick = onAcceptClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimaryFixed,
                        contentColor = MaterialTheme.colorScheme.primaryFixed
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
                    color = MaterialTheme.colorScheme.onPrimaryFixed,
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
                    color = MaterialTheme.colorScheme.onPrimaryFixed,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            icon = {
                Surface(
                    color = MaterialTheme.colorScheme.onPrimaryFixed,
                    shape = CircleShape
                ) {
                    Image(
                        painter = painterResource(R.drawable.files),
                        contentDescription = "terms icon",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primaryFixed),
                        modifier = Modifier.size(56.dp).padding(8.dp)
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

@Preview(showBackground = true)
@Composable
fun MemberAlertPreview() {
    val member = CircleMember(
        id = "1",
        alias = "Test",
        selected = false,
        receiverEmail = "test@gmail.com",
        addedAt = System.currentTimeMillis(),
        profileName = "Test"
    )

    MemberAlertDialog(
        true,
        onDismiss = {},
        member
    )
}

@Composable
fun MemberAlertDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    member: CircleMember
) {
    if (showDialog) {
        AlertDialog(
            modifier = Modifier.fillMaxWidth(),
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
                MemberInfoCard(member)
            },
            confirmButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text(
                        text = "Close",
                        fontFamily = manrope,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            icon = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = CircleShape
                ) {
                    Image(
                        painter = painterResource(R.drawable.info),
                        contentDescription = "emergency icon",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
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

