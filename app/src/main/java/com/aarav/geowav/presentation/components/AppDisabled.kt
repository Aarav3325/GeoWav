package com.aarav.geowav.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.presentation.theme.manrope

@Composable
fun AppDisabled() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                color = Color(0xFFBAC3FF),
                shape = CircleShape
            ) {
                Icon(
                    painter = painterResource(R.drawable.new_logo),
                    contentDescription = "logo",
                    tint = Color(0xFF222C61),
                    modifier = Modifier.size(56.dp),
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "GeoWav",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontFamily = manrope,
                fontWeight = FontWeight.Bold
            )


            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "App Access Disabled",
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = manrope,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Please update the app or try again later.",
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = manrope,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
            )
        }
    }
}