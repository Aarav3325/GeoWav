package com.aarav.geowav.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


private val LiveGreen = Color(0xFF4CAF50)

@Preview(showBackground = true)
@Composable
fun AwarenessSnapshotCard() {
    Surface(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
        ) {
            GradientBar()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                AwarenessCardLabel()

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Home",
                    style =
                        MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 26.sp,
                            letterSpacing = (-0.5).sp,
                            lineHeight = 30.sp,
                        )
                )

                Spacer(Modifier.height(8.dp))

                VisibilityMembers()

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                )

                LatestActivitySection()
            }
        }
    }
}

@Composable
fun GradientBar() {
//    val topBarBrush = if (isEmergency) {
//        Brush.horizontalGradient(listOf(EmergencyRed, EmergencyAccentBar))
//    } else {
//        Brush.horizontalGradient(listOf(NavyDeep, Periwinkle))
//    }

    val topBarBrush =
        Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.inversePrimary))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .background(topBarBrush)
    )
}

@Composable
fun AwarenessCardLabel() {

    val dotColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)

    val animatedDotColor by animateColorAsState(
        targetValue = LiveGreen,
        animationSpec = tween(400),
        label = "dotColor",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "livePulse")
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dotScale",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size((6 * dotScale).dp)
                .clip(CircleShape)
                .background(animatedDotColor),
        )

        Spacer(Modifier.width(6.dp))

        Text(
            text = "AWARENESS",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 0.08.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            ),
        )
    }
}

@Composable
private fun AvatarStack() {
    Row {
        repeat(3) { idx ->
            MemberAvatar(
                index = idx,
                modifier = Modifier.offset(x = (-6 * idx).dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VisibilityMembers() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(0.dp)) {
        AvatarStack()

        Text(
            text = "Visible to Nitya & Diya",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )

    }
}

@Composable
fun MemberAvatar(
    index: Int,
    size: Int = 26,
    modifier: Modifier = Modifier
) {

    val (avatarBg, avatarFg) = when (index % 3) {
        0 -> Pair(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        1 -> Pair(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        else -> Pair(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .clip(CircleShape)
            .background(avatarBg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "A",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = (size * 0.38f).sp,
                fontWeight = FontWeight.SemiBold,
                color = avatarFg,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LatestActivitySection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MemberAvatar(0)

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                "Latest in your circle",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
            )

            Text(
                text = "Nitya arrived at Home",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }

        Text(
            "5 min ago",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
        )
    }
}
