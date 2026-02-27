package com.aarav.geowav.presentation.components

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R

@Composable
fun AppDisabled() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
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

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "App Access Disabled",
                color = Color.White,
                fontSize = 22.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Please update the app or try again later.",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    }
}