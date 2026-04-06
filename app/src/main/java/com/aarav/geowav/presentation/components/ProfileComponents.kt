package com.aarav.geowav.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import coil.decode.SvgDecoder
import com.aarav.geowav.R
import com.aarav.geowav.data.model.User
import com.aarav.geowav.data.model.UserPlan
import com.aarav.geowav.presentation.theme.manrope

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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

    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }

    val imageUrl =
        currentUser?.avatar?.takeIf { it.isNotBlank() }
            ?: userAvatar.takeIf { !it.isNullOrBlank() }

    val stableAvatar = remember(currentUser?.avatar) {
        currentUser?.avatar
    }


    val badge = when (plan) {
        UserPlan.FREE -> null
        UserPlan.PREMIUM -> R.drawable.geowav_premium_badge
        UserPlan.PRO -> R.drawable.geowav_pro_badge
    }


    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
            ) {

                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .clickable(
                            enabled = !isUploading
                        ) {
                            onAvatarClick()
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White)
                            .size(84.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!imageUrl.isNullOrBlank()) {
                            AvatarImage(
                                avatarUrl = imageUrl,
                                isUploading = isUploading,
                                modifier = Modifier.size(84.dp)
                            )
                        } else {
//
                            Text(
                                text = currentUser?.username?.take(1) ?: "",
                                color = Color.Black,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier

                            )
                        }
                    }

                }

                if (!isUploading) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .clickable {
                                onAvatarClick()
                            }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.camera),
                            contentDescription = "camera",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }

                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(84.dp)
                    )
                }


            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(
                    horizontal = 16.dp
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = currentUser?.username ?: "User",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = manrope
                    )

                    badge?.let {
                        Icon(
                            painter = painterResource(it),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = currentUser?.email ?: "email@gmail.com",
                    style = MaterialTheme.typography.titleSmall,
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