package com.aarav.geowav.presentation.observe

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.core.utils.ViewerLocationState
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.data.model.User
import com.aarav.geowav.presentation.home.HomeScreenVM
import com.aarav.geowav.presentation.home.ObserveLiveLocationCard
import com.aarav.geowav.presentation.home.ViewerTrayOverlay
import com.aarav.geowav.presentation.theme.manrope

@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObserveScreen(
    viewModel: HomeScreenVM,
    back: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsState()

    Log.i("OBSERVE", uiState.lovedOnes.toString())
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Observe Loved Ones",
                        fontFamily = manrope,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            back()
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.back),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
    ) {
//        LaunchedEffect(Unit) {
//            viewModel.loadLovedOnes()
//        }
//
//        LaunchedEffect(uiState.lovedOnes) {
//            if (uiState.lovedOnes.isNotEmpty()) {
//                Log.i("OBSERVE", "observe called")
//                viewModel.observeUsers()
//                viewModel.cleanupRemovedUsers(
//                    uiState.lovedOnes.map { it.id }.toSet()
//                )
//            }
//        }

        val hasAnyLiveSharing = uiState.locations.values.any {
            it is ViewerLocationState.NormalSharing ||
                    it is ViewerLocationState.EmergencySharing
        }


        /*
        listOf(
//                    CircleMember(
//                        id = "",
//                        profileName = "Aarav",
//                        selected = false,
//                        receiverEmail = "TODO()",
//                        alias = null
//                    )
         */

        val emergencyUser = uiState.locations
            .entries
            .firstOrNull { it.value is ViewerLocationState.EmergencySharing }


        Box(
            modifier = Modifier.fillMaxSize()
                .padding(it)
        ) {

            AnimatedVisibility(hasAnyLiveSharing) {
                ObserveLiveLocationCard(viewModel, uiState, true, navigateToObserve = {}, Modifier
                    .fillMaxSize())
            }

            ViewerTrayOverlay(
                Modifier.align(Alignment.BottomCenter),
                uiState.lovedOnes
            )
        }
    }
}