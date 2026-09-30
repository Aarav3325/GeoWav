package com.aarav.geowav.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.decode.SvgDecoder
import com.aarav.geowav.R
import com.aarav.geowav.data.model.User
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.presentation.theme.manrope

@Composable
fun ProfileCard(
    isUploading: Boolean,
    uploadingProgress: Float,
    isDarkThemeEnabled: Boolean,
    plan: UserPlan,
    currentUser: User?,
    userAvatar: String?,
    onAvatarClick: () -> Unit
) {
    val imageUrl = currentUser?.avatar?.takeIf { it.isNotBlank() }
        ?: userAvatar?.takeIf { !it.isNullOrBlank() }

    val badge = when (plan) {
        UserPlan.FREE -> null
        UserPlan.PREMIUM -> R.drawable.geowav_premium_badge
        UserPlan.PRO -> R.drawable.geowav_pro_badge
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                // Avatar container
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .clickable(enabled = !isUploading) {
                            onAvatarClick()
                        }
                ) {
                    if (!imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "${currentUser?.username ?: "User"} avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser?.username?.take(1)?.uppercase() ?: "U",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = manrope
                            )
                        }
                    }
                }

                if (isUploading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    }
                } else {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            MaterialTheme.colorScheme.surfaceContainer
                        ),
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .clickable {
                                onAvatarClick()
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.camera),
                                contentDescription = "Change profile picture",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = currentUser?.username ?: "User",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = manrope
                    )

                    badge?.let {
                        Icon(
                            painter = painterResource(it),
                            contentDescription = "Subscription Badge",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = currentUser?.email ?: "email@gmail.com",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = manrope
                )
            }
        }
    }
}

@Composable
fun AvatarImage(
    avatarUrl: String?,
    isUploading: Boolean,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = avatarUrl,
        contentDescription = null,
//        placeholder = painterResource(R.drawable.user),
//        error = painterResource(R.drawable.user),
//        fallback = painterResource(R.drawable.user),
        contentScale = ContentScale.Crop,
        modifier = modifier
    )


    if (isUploading) {
        CircularProgressIndicator()
    }


}

@Composable
fun IdentityAvatar(
    avatarUrl: String?,
    displayName: String,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    borderColor: Color? = null
) {
    val initial = displayName.take(1).ifBlank { "?" }.uppercase()
    val cleanAvatarUrl = avatarUrl?.takeIf { it.isNotBlank() }
    val avatarModifier = modifier
        .clip(CircleShape)
        .then(
            if (borderColor != null) {
                Modifier.border(1.dp, borderColor, CircleShape)
            } else {
                Modifier
            }
        )

    Box(
        modifier = avatarModifier,
        contentAlignment = Alignment.Center
    ) {
        if (cleanAvatarUrl == null) {
            IdentityAvatarInitials(
                initial = initial,
                backgroundColor = backgroundColor,
                contentColor = contentColor,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            SubcomposeAsyncImage(
                model = cleanAvatarUrl,
                contentDescription = "$displayName profile photo",
                contentScale = ContentScale.Crop,
                loading = {
                    IdentityAvatarInitials(
                        initial = initial,
                        backgroundColor = backgroundColor,
                        contentColor = contentColor,
                        modifier = Modifier.fillMaxSize()
                    )
                },
                error = {
                    IdentityAvatarInitials(
                        initial = initial,
                        backgroundColor = backgroundColor,
                        contentColor = contentColor,
                        modifier = Modifier.fillMaxSize()
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
private fun IdentityAvatarInitials(
    initial: String,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = contentColor,
            fontFamily = manrope,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}
