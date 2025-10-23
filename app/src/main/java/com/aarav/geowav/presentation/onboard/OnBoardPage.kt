package com.aarav.geowav.presentation.onboard

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.ui.theme.nunito
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    navigateToAuth: () -> Unit
    //modifier: Modifier,
                     //navigateToSignUp : () -> Unit
)
{

    val pages = listOf(
        OnBoardingPage(
            "Stay Connected in Real-Time",
            "GeoWav keeps you updated about your friends and loved ones with live location sharing. See who’s nearby and never miss a moment.",
            R.drawable.gps
        ),
        OnBoardingPage(
            "Smart Geofences & Alerts",
            "Set places that matter — home, school, or office. Get notified when someone arrives or leaves, automatically and effortlessly.",
            R.drawable.navigation_arrow
        ),
        OnBoardingPage(
            "Your Safety, Our Priority",
            "GeoWav values privacy and security. Your location is shared only with people you trust. Stay safe while staying connected.",
            R.drawable.vault
        ),
    )

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pages.size }
    )
    val scope = rememberCoroutineScope()

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

        val context = LocalContext.current

        // Indicator + Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp).padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                val last = pagerState.currentPage == pages.lastIndex

               AnimatedVisibility(pagerState.currentPage != pages.lastIndex) {

                   TextButton(
                       onClick = {
                               scope.launch { pagerState.animateScrollToPage(pagerState.pageCount - 1, animationSpec = TweenSpec(durationMillis = 400)) }
                       },
                       shape = RoundedCornerShape(12.dp),
                       colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
                       modifier = Modifier.wrapContentWidth().height(56.dp)
                   ) {
                       Text(
                           text = "Skip",
                           fontSize = 18.sp,
                           textAlign = TextAlign.Center,
                           fontFamily = nunito,
                           color = MaterialTheme.colorScheme.onBackground,
                           fontWeight = FontWeight.SemiBold
                       )
                   }
                   
               }

                AnimatedVisibility(modifier = Modifier.weight(1.0f),
                    visible = pagerState.currentPage != pages.lastIndex) {
                     DotsIndicator(modifier = Modifier.weight(1.0f),pagerState.currentPage, pages.size)

                }


                Log.i("MYTAG", "Current page : " + pagerState.currentPage)

                Spacer(modifier = Modifier.height(16.dp))

                var clickState by remember {
                    mutableStateOf(false)
                }

                FilledTonalButton(
                    onClick = {
                        if (pagerState.currentPage == pages.lastIndex) {
                            //navigateToSignUp()
                            clickState = !clickState
                            navigateToAuth()
//                        val intent = Intent(context, HomeScreenActivity::class.java)
//                        context.startActivity(intent)
//                        Toast.makeText(context, "OnBoardCompleted", Toast.LENGTH_LONG).show()
                        } else {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1, animationSpec = TweenSpec(durationMillis = 350)) }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                    modifier = if(last) Modifier.fillMaxWidth().height(56.dp) else Modifier.height(56.dp)
                ) {
                    AnimatedVisibility(!clickState) {
                        Text(
                            text = if (pagerState.currentPage == pages.lastIndex) "Grant Permissions" else "Next",
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = nunito,
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
){

//    Row(
//        modifier = Modifier.padding(12.dp)
//    ) {
//        Text(
//            text = "GeoWav",
//            fontSize = 36.sp,
//            fontFamily = nunito,
//            color = MaterialTheme.colorScheme.onBackground,
//            fontWeight = FontWeight.Bold
//        )
//    }

    Column(
        modifier = Modifier.padding(12.dp).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
                Text(
            text = "GeoWav",
            fontSize = 36.sp,
            fontFamily = nunito,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier.fillMaxWidth()
        ){
            Surface(
                modifier = Modifier.size(196.dp).align(Alignment.Center),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = CircleShape,
            ) {
                Image(
                    painter = painterResource(id = page.imageRes),
                    contentDescription = "navigation arrow",
                    modifier = Modifier.size(24.dp).padding(24.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onTertiaryContainer)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = page.title,
            fontSize = 24.sp,
            fontFamily = nunito,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            fontFamily = nunito,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
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
            val color = if (index == currentPage) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.inversePrimary
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(width = width, height = 5.dp)
                    .background(color, RoundedCornerShape(16.dp))
            )
        }
    }
}