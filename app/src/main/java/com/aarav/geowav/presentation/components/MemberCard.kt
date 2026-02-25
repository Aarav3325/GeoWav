package com.aarav.geowav.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.core.utils.formatTime
import com.aarav.geowav.data.model.CircleMember
import com.aarav.geowav.presentation.theme.manrope
import com.aarav.geowav.presentation.theme.sora
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MemberInfoCard(
    member: CircleMember
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                MaterialTheme.colorScheme.onPrimary,
                                MaterialTheme.colorScheme.inversePrimary
                            )
                        )
                    ),
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    member.alias?.take(1) ?: member.profileName.take(1),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.W600,
                        fontFamily = sora
                    ),
                    fontSize = 24.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    member.alias ?: member.profileName,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = manrope,
                    fontSize = 16.sp
                )

                Text(
                    member.receiverEmail ?: "",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = manrope,
                    fontSize = 14.sp
                )


                val dateFormatter = remember {
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                }

                Text(
                    "Added at ${dateFormatter.format(Date(member.addedAt))}",
                    color = MaterialTheme.colorScheme.outlineVariant,
                    fontFamily = manrope,
                    fontSize = 10.sp
                )

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MemberInfoCardPreview() {
    val member = CircleMember(
        id = "1",
        alias = "Test",
        selected = false,
        receiverEmail = "test@gmail.com",
        addedAt = System.currentTimeMillis(),
        profileName = "Test"
    )

    MemberInfoCard(
        member
    )
}