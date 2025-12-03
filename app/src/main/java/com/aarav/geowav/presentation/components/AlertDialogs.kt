package com.aarav.geowav.presentation.components

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.aarav.geowav.R
import com.aarav.geowav.ui.theme.manrope
import com.aarav.geowav.ui.theme.sora
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay

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
    val backgroundLocationPermission = rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

    var requestingBackground by remember { mutableStateOf(false) }

    // Request flow
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
