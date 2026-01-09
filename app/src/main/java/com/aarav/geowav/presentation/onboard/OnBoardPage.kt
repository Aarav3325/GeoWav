package com.aarav.geowav.presentation.onboard

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.aarav.geowav.presentation.components.PermissionAlertDialog
import com.aarav.geowav.presentation.theme.manrope
import com.aarav.geowav.presentation.theme.sora
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OnboardingScreen(
    navigateToAuth: () -> Unit,
    sharedPreferences: SharedPreferences,
    onBoardVM: OnBoardVM,
) {

    val uiState by onBoardVM.uiState.collectAsState()


    val showLocationDialog = uiState.showPermissionDialog


//    var permissionsGranted by remember { mutableStateOf(false) }
//
//    var isOnboarded by remember {
//        mutableStateOf(false)
//    }

    val pages = uiState.pages

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pages.size }
    )

    LaunchedEffect(pagerState.currentPage) {
        onBoardVM.onPageChanged(pagerState.currentPage)
    }


    val scope = rememberCoroutineScope()

    AnimatedVisibility(uiState.showPermissionDialog) {
        PermissionAlertDialog(
            showDialog = uiState.showPermissionDialog,
            onDismiss = { onBoardVM.onPermissionDialogDismiss() },
            onPermissionsGranted = {
                onBoardVM.onFineLocationResult(true)
                onBoardVM.onBackgroundLocationResult(true)
            }
        )
    }

    LaunchedEffect(uiState.allPermissionsGranted) {
        if (uiState.allPermissionsGranted) {
            sharedPreferences.edit(commit = true) {
                putBoolean("isOnboarded", true)
                //editor.apply()
            }
            delay(2000)
            navigateToAuth()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.SpaceBetween
    ) {


        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(0.75f)
        ) { page ->
            OnboardingPageContent(page = pages[page])

            Log.i("MYTAG", "page : " + pages[page])

        }

        // Indicator + Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Skip",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = manrope,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                }

                AnimatedVisibility(
                    modifier = Modifier.weight(1.0f),
                    visible = uiState.currentPage != pages.lastIndex
                ) {
                    DotsIndicator(
                        modifier = Modifier.weight(1.0f),
                        pagerState.currentPage,
                        pages.size
                    )

                }


                Log.i("MYTAG", "Current page : " + pagerState.currentPage)

                Spacer(modifier = Modifier.height(16.dp))

                var clickState by remember {
                    mutableStateOf(false)
                }

                FilledTonalButton(
                    onClick = {
                        if (uiState.currentPage == pages.lastIndex) {
                            clickState = !clickState
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
                    modifier = if (uiState.currentPage == pages.lastIndex) Modifier
                        .fillMaxWidth()
                        .height(48.dp) else Modifier.height(
                        48.dp
                    )
                ) {
                    AnimatedVisibility(!clickState) {
                        Text(
                            text = if (uiState.currentPage == pages.lastIndex) "Grant Permissions" else "Next",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = manrope,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }


                    AnimatedVisibility(clickState) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingPageContent(
    page: OnBoardingPage
) {

    Column(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "GeoWav",
            fontSize = 36.sp,
            fontFamily = manrope,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier
                    .size(154.dp)
                    .align(Alignment.Center),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = CircleShape,
            ) {
                Image(
                    painter = painterResource(id = page.imageRes),
                    contentDescription = "navigation arrow",
                    modifier = Modifier
                        .size(24.dp)
                        .padding(24.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onTertiaryContainer)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = page.title,
            fontFamily = manrope,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            fontFamily = sora,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DotsIndicator(
    modifier: Modifier = Modifier,
    currentPage: Int, totalDots: Int
) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalDots) { index ->

            val width = if (index == currentPage) 25.dp else 15.dp
            val color =
                if (index == currentPage) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.inversePrimary
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(width = width, height = 5.dp)
                    .background(color, RoundedCornerShape(16.dp))
            )
        }
    }
}