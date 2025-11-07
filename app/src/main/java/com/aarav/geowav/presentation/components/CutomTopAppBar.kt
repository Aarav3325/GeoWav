package com.aarav.geowav.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aarav.geowav.R
import com.aarav.geowav.ui.theme.manrope
import kotlinx.coroutines.launch
//
//@Composable
//fun DefualtTopAppBar(){
//    TopAppBar(
//        title = {
//            Text(
//                text = "GeoWav",
//                style = MaterialTheme.typography.headlineLarge.copy(
//                    color = textColor.value
//                ),
//                fontFamily = manrope,
//                fontWeight = FontWeight.Bold
//            )
//        },
//        actions = {
//            IconButton(
//                onClick = {
//                    scope.launch {
//                        SnackbarManager.showMessage("No Notifications")
//                    }
//                }
//            ) {
//                Image(
//                    colorFilter = ColorFilter.tint(textColor.value),
//                    painter = painterResource(R.drawable.bell),
//                    contentDescription = "bell",
//                    modifier = Modifier
//                        .size(28.dp),
//                )
//            }
//
//            // Spacer(modifier = Modifier.width(10.dp))
//
//            IconButton(
//                onClick = {
//                    scope.launch {
//                        googleSignInClient.signOut()
//                        navigateToAuth()
//                    }
//                }
//            ) {
//                Image(
//                    colorFilter = ColorFilter.tint(textColor.value),
//                    painter = painterResource(R.drawable.gear_six),
//                    contentDescription = "setting",
//                    modifier = Modifier.size(28.dp),
//                )
//            }
//        },
//        colors = TopAppBarDefaults.topAppBarColors(
//            containerColor = Color.Transparent,
//            scrolledContainerColor = Color.Transparent
//        )
//    )
//}