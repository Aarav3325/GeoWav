package com.aarav.geowav.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.data.model.UpgradeContext
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.data.model.getPlanContent
import com.aarav.geowav.data.model.getReasonContent
import com.aarav.geowav.presentation.paywall.freePlanColors
import com.aarav.geowav.presentation.paywall.premiumPlanColors
import com.aarav.geowav.presentation.paywall.proPlanColors
import com.aarav.geowav.presentation.theme.manrope
import com.aarav.geowav.presentation.theme.*
import com.aarav.geowav.presentation.theme.onPrimaryLight
import com.google.android.libraries.places.api.model.Place

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun PlaceModalSheet(
    place: Place?,
    sheetState: SheetState,
    showSheet: Boolean,
    onDismissRequest: () -> Unit,
    clearSearch: () -> Unit,
    onAddPlaceBtnClick: (String) -> Unit
) {

    AnimatedVisibility(showSheet) {
        ModalBottomSheet(
            dragHandle = {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .width(60.dp)
                        .height(3.dp),
                    color = MaterialTheme.colorScheme.secondary,
                ) {

                }
            },
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            SheetContent(
                place,
                clearSearch,
                onAddPlaceBtnClick
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SheetContent(
    place: Place?,
    clearSearch: () -> Unit,
    onAddPlaceBtnClick: (String) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            Surface(
                modifier = Modifier.size(32.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape,
            ) {
                Icon(
                    painter = painterResource(R.drawable.navigation_arrow),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(4.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = place?.displayName ?: "Invalid Place",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = manrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
        }

        Text(
            text = place?.shortFormattedAddress ?: "Address not available",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            fontFamily = manrope,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Lat: ${
                place?.location?.latitude?.toString()?.take(7)
            }, Lng: ${place?.location?.longitude?.toString()?.take(7)}",
            style = MaterialTheme.typography.labelMedium,
            fontSize = 14.sp,
            fontFamily = manrope,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FilledTonalButton(
            onClick = {
                onAddPlaceBtnClick(place?.id ?: "0")
                clearSearch()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),

            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Add Place", fontFamily = manrope, fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    painter = painterResource(R.drawable.caret_circle_right),
                    contentDescription = "caret circle right",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeBottomSheet(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest
    ) {
        content()
    }
}

@Composable
fun UpgradeBottomSheetContent(
    context: UpgradeContext,
    onUpgradeClick: () -> Unit,
    onDismiss: () -> Unit
) {


    val reasonContent = remember { getReasonContent(context.reason) }
    val planContent = remember { getPlanContent(context.upgradeTo) }

    val colors = when (context.upgradeTo) {
        UserPlan.PREMIUM -> premiumPlanColors()
        UserPlan.PRO -> proPlanColors()
        UserPlan.FREE -> freePlanColors()
    }

    val gradientColors = when (context.upgradeTo) {
        UserPlan.PREMIUM -> listOf(
            onPrimaryDark,
            primaryContainerDark
        )

        UserPlan.PRO -> listOf(
            onSecondaryDark,
            secondaryContainerDark
        )
        UserPlan.FREE -> listOf(
            surfaceContainerDark,
            outlineVariantDark
        )
    }

    val textColor = when (context.upgradeTo) {
        UserPlan.PREMIUM -> primaryDark
        UserPlan.PRO -> secondaryDark
        UserPlan.FREE -> surfaceContainerDark
    }



    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        gradientColors
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
            ) {

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            textColor.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(reasonContent.icon),
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        text = planContent.title,
                        fontFamily = manrope,
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = reasonContent.title,
                        fontFamily = manrope,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = reasonContent.description,
            fontFamily = manrope,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 20.dp),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                planContent.features.forEach {
                    FeatureItem(it)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        FilledTonalButton(
            onClick = onUpgradeClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.buttonBg,
                contentColor = colors.buttonTextColor
            ),
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = planContent.ctaText,
                fontFamily = manrope,
                fontWeight = FontWeight.SemiBold,
                color = colors.buttonTextColor,
                fontSize = 14.sp,
            )
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                "Maybe later",
                fontFamily = manrope,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

//@Composable
//fun UpgradeBottomSheetContent(
//    reason: UpgradeReason,
//    onUpgradeClick: () -> Unit,
//    onDismiss: () -> Unit
//) {
//    val (title, desc) = when (reason) {
//        UpgradeReason.PlaybackLocked -> "Playback is Premium" to
//                "Replay your trips with smooth animation and insights."
//
//        UpgradeReason.HistoryLimit -> "Unlock Full History" to
//                "Access your complete travel timeline anytime."
//
//        UpgradeReason.SpeedControl -> "Control Playback Speed" to
//                "Adjust speed for better analysis of your routes."
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(20.dp)
//    ) {
//
//        Text(
//            text = "Upgrade to Premium",
//            fontFamily = manrope,
//            style = MaterialTheme.typography.titleLarge,
//            fontWeight = FontWeight.Bold
//        )
//
//        Spacer(Modifier.height(8.dp))
//
//        Text(
//            text = title,
//            fontFamily = manrope,
//            style = MaterialTheme.typography.titleMedium
//        )
//
//        Spacer(Modifier.height(6.dp))
//
//        Text(
//            text = desc,
//            fontFamily = manrope,
//            style = MaterialTheme.typography.bodyMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//
//        Spacer(Modifier.height(20.dp))
//
//        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//            FeatureItem("Playback & route animation")
//            FeatureItem("Unlimited history access")
//            FeatureItem("Advanced insights & speed control")
//        }
//
//        Spacer(Modifier.height(24.dp))
//
//        FilledTonalButton(
//            onClick = onUpgradeClick,
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(14.dp)
//        ) {
//            Text("Upgrade Now", fontFamily = manrope)
//        }
//
//        TextButton(
//            onClick = onDismiss,
//            modifier = Modifier.align(Alignment.CenterHorizontally)
//        ) {
//            Text("Maybe later", fontFamily = manrope)
//        }
//    }
//}

@Composable
fun FeatureItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(R.drawable.check),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text, fontFamily = manrope, fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
    }
}