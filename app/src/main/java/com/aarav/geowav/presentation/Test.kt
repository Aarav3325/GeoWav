package com.aarav.geowav.presentation


import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import com.aarav.geowav.R
import com.aarav.geowav.domain.authentication.GoogleSignInClient
import com.aarav.geowav.presentation.components.SnackbarManager
import com.aarav.geowav.ui.theme.GeoWavTheme
import com.aarav.geowav.ui.theme.manrope
import com.aarav.geowav.ui.theme.sora
import com.google.maps.android.compose.Circle
import kotlinx.coroutines.launch


// ---------- DATA CLASSES ----------
data class GeoConnection(
    val id: String,
    val name: String,
    val isOnline: Boolean = false
)

data class GeoZone(
    val id: String,
    val name: String,
    val inside: Boolean,
    val radiusMeters: Int
)

data class GeoAlert(
    val id: String,
    val title: String,
    val subtitle: String,
    val time: String,
    val type: String
)

// ---------- MAIN HOME SCREEN ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeoWavHomeScreen(
    navigateToAuth: () -> Unit,
    googleSignInClient: GoogleSignInClient,
    connections: List<GeoConnection>,
    zones: List<GeoZone>,
    alerts: List<GeoAlert>,
    onViewMap: () -> Unit,
    onAddZone: () -> Unit,
    onShareLocation: () -> Unit,
    onOpenAlerts: () -> Unit,
    modifier: Modifier = Modifier
) {


    val scope = rememberCoroutineScope()

    val Primary = MaterialTheme.colorScheme.primary
    val Secondary = MaterialTheme.colorScheme.secondary
    val Accent = MaterialTheme.colorScheme.tertiary
    val Success = MaterialTheme.colorScheme.surfaceTint
    val scroll = rememberScrollState()


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {

                }
            )
        }
    ) {

    }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .background(MaterialTheme.colorScheme.background)
        ) {

            // 🌅 Gradient Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()

                    .background(MaterialTheme.colorScheme.background)
            ) {

                Image(
                    painter = if (isSystemInDarkTheme()) painterResource(R.drawable.dark_bg_geowav_new_2) else painterResource(
                        R.drawable.light_bg_geowav_new
                    ),
                    contentDescription = "bg",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )

                Row(
//                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .padding(start = 12.dp, end = 12.dp, top = 48.dp, bottom = 12.dp)
                ) {
                    Text(
                        text = "GeoWav",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = Color.Black
                        ),
                        fontFamily = manrope,
                        modifier = Modifier.weight(1.0f),
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = {
                            scope.launch {
                                SnackbarManager.showMessage("No Notifications")
                            }
                        }
                    ) {
                        Image(
                            painter = painterResource(R.drawable.bell),
                            contentDescription = "bell",
                            modifier = Modifier
                                .size(28.dp),
                            colorFilter = ColorFilter.tint(Color.Black)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = {
                            scope.launch {
                                googleSignInClient.signOut()
                                navigateToAuth()
                            }
                        }
                    ) {
                        Image(
                            painter = painterResource(R.drawable.gear_six),
                            contentDescription = "setting",
                            modifier = Modifier.size(28.dp),
                            colorFilter = ColorFilter.tint(Color.Black)
                        )
                    }
                    }




                ProfileCard(
                    googleSignInClient,
                    modifier = Modifier.align(Alignment.Center).padding(top = 78.dp)
                )

//                Row(
//                    modifier = Modifier
//                        .align(Alignment.TopEnd)
//                        .padding(16.dp),
//                    horizontalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    IconButton(onClick = {  }) {
//                        Icon(
//                            Icons.Outlined.Notifications,
//                            contentDescription = null,
//                            tint = MaterialTheme.colorScheme.onPrimaryContainer
//                        )
//                    }
//                    IconButton(onClick = {  }) {
//                        Icon(
//                            Icons.Outlined.Settings,
//                            contentDescription = null,
//                            tint = MaterialTheme.colorScheme.onPrimaryContainer
//                        )
//                    }
//                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 12.dp)
                    .background(MaterialTheme.colorScheme.background)
            ) {
//                CurrentLocationCard(
//                    city = "Ahmedabad, Gujarat",
//                    lastUpdated = "2 mins ago",
//                    onViewMap = onViewMap
//                )

                // 👥 Your Circle
                ConnectionsRow(
                    title = "Your Circle",
                    connections = connections,
                    onAdd = onAddZone
                )

                // 🏠 Active Zones
                ActiveZonesSection(
                    zones = zones,
                    onZoneClick = {}
                )

                // ⚡ Quick Actions
                QuickActionsRow(
                    onAddZone = onAddZone,
                    onShare = onShareLocation,
                    onAlerts = onOpenAlerts
                )

                // 🔔 Recent Alerts
                Text(
                    "Recent Alerts",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = manrope
                    ),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                RecentAlertsList(alerts)
            }


            // 📍 Current Location Card

        }
    }
}

// ---------- CURRENT LOCATION CARD ----------
@Composable
fun CurrentLocationCard(city: String, lastUpdated: String, onViewMap: () -> Unit) {

    val Primary = MaterialTheme.colorScheme.primary
    val Secondary = MaterialTheme.colorScheme.secondary
    val Accent = MaterialTheme.colorScheme.tertiary
    val Success = MaterialTheme.colorScheme.surfaceTint
    Card(
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFE9F2FF), Color(0xFFF6FBFF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "map",
                    tint = Accent,
                    modifier = Modifier.size(36.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    city,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = sora,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Last updated • $lastUpdated",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Accent,
                        fontFamily = sora
                    )
                )
            }

            Button(
                onClick = onViewMap,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("View", color = Color.White, fontFamily = sora)
            }
        }
    }
}

// ---------- CONNECTIONS ----------
@Composable
fun ConnectionsRow(title: String, connections: List<GeoConnection>, onAdd: () -> Unit) {

    val Primary = MaterialTheme.colorScheme.primary
    val Secondary = MaterialTheme.colorScheme.secondary
    val Accent = MaterialTheme.colorScheme.tertiary
    val Success = MaterialTheme.colorScheme.surfaceTint
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = manrope
                ),
                fontSize = 16.sp,
                modifier = Modifier.weight(1.0f)
            )
            TextButton(onClick = { }) {
                Text(
                    "Manage",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = manrope
                )
            }
        }

        LazyRow(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            items(connections) { conn -> ConnectionCard(conn) }
            item { AddConnectionCard(onClick = onAdd) }
        }
    }
}

@Composable
fun ConnectionCard(conn: GeoConnection) {

    val Primary = MaterialTheme.colorScheme.primary
    val Secondary = MaterialTheme.colorScheme.secondary
    val Accent = MaterialTheme.colorScheme.tertiary
    val Success = MaterialTheme.colorScheme.surfaceTint
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(88.dp)) {
        Box(
            modifier = Modifier
                .size(64.dp)
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
                conn.name.take(1),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.W600,
                    fontFamily = sora
                ),
                fontSize = 24.sp,
                modifier = Modifier.align(Alignment.Center)
            )
//            Box(
//                modifier = Modifier
//                    .size(14.dp)
//                    .clip(CircleShape)
//                    .background(if (conn.isOnline) Success else Color.Gray)
//                    .align(Alignment.BottomEnd)
//            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            conn.name,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = sora),
            maxLines = 1
        )
    }
}

@Composable
fun AddConnectionCard(onClick: () -> Unit) {

    val Primary = MaterialTheme.colorScheme.primary
    val Accent = MaterialTheme.colorScheme.secondary
    val Tertiary = MaterialTheme.colorScheme.tertiary
    val Success = MaterialTheme.colorScheme.surfaceTint
    FilledTonalButton(
        shape = CircleShape,
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight(),
        onClick = {},
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
                //.clickable { onClick() }
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add",
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Add",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.tertiary,
                    fontFamily = sora
                ),
                fontSize = 16.sp
            )
        }
    }
}

// ---------- ZONES ----------
@Composable
fun ActiveZonesSection(zones: List<GeoZone>, onZoneClick: (GeoZone) -> Unit) {
    Column(
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        Text(
            "Active Zones",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontFamily = manrope
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            zones.forEach { zone -> ZoneCard(zone = zone, onClick = { onZoneClick(zone) }) }
        }
    }
}

@Composable
fun ZoneCard(zone: GeoZone, onClick: () -> Unit) {

    val Primary = MaterialTheme.colorScheme.primary
    val Secondary = MaterialTheme.colorScheme.secondary
    val Tertiary = MaterialTheme.colorScheme.tertiary
    val Success = MaterialTheme.colorScheme.surfaceTint
    val statusColor = if (zone.inside) Secondary else Tertiary
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(statusColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = "zone",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        zone.name,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontFamily = sora)
                    )
                    Text(
                        "${zone.radiusMeters}m • ${if (zone.inside) "Inside" else "Outside"}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontFamily = sora
                        ),
                        fontSize = 12.sp
                    )
                }
            }
            TextButton(onClick = onClick) {
                Text(
                    "Edit",
                    fontSize = 14.sp,
                    fontFamily = sora,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

// ---------- QUICK ACTIONS ----------
@Composable
fun QuickActionsRow(onAddZone: () -> Unit, onShare: () -> Unit, onAlerts: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        QuickActionButton(Icons.Default.Add, "Add Zone", onAddZone)
//        QuickActionButton(Icons.Default.Share, "Share", onShare)
//        QuickActionButton(Icons.Default.Notifications, "Alerts", onAlerts)
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {

    val Primary = MaterialTheme.colorScheme.primary
    val Secondary = MaterialTheme.colorScheme.secondary
    val Accent = MaterialTheme.colorScheme.tertiary
    val Success = MaterialTheme.colorScheme.surfaceTint
    FilledTonalButton(
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        onClick = onClick
        //modifier = Modifier.clickable { onClick() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = sora,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                fontSize = 14.sp
            )
        }
    }
}

// ---------- ALERTS ----------
@Composable
fun RecentAlertsList(alerts: List<GeoAlert>) {

    val Primary = MaterialTheme.colorScheme.primary
    val Secondary = MaterialTheme.colorScheme.secondary
    val Accent = MaterialTheme.colorScheme.tertiary
    val Success = MaterialTheme.colorScheme.surfaceTint
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(bottom = 24.dp)
    ) {
        if (alerts.isEmpty()) {
            Text(
                "No recent alerts. You’re all clear!",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Accent,
                    fontFamily = sora
                )
            )
        } else {
            alerts.forEach { alert -> AlertItem(alert) }
        }
    }
}

@Composable
fun AlertItem(alert: GeoAlert) {


    val enter = Color(0xFF00513f)

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSystemInDarkTheme()) {
                if (alert.type.equals("enter")) Color(0xFF00513f) else Color(0xFF723339)
            } else {
                if (alert.type.equals("enter")) Color(0xFFa3f2d6) else Color(0xFFffdadb)
            },
            contentColor =
                if (isSystemInDarkTheme()) {
                    if (alert.type.equals("enter")) Color(0XFFa3f2d6) else Color(0xFFffdadb)
                } else {
                    if (alert.type.equals("enter")) Color(0xFF00513f) else Color(0xFF723339)
                }
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    //.background(Color(0xFFBAFFDF)),
                    .background(MaterialTheme.colorScheme.inverseSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    alert.title,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = sora
                    )
                )
                Text(
                    alert.subtitle,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = sora),
                    maxLines = 2
                )
            }
            Text(alert.time,
                fontFamily = sora,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp)
        }
    }
}

// ---------- PREVIEW ----------
@Preview(showBackground = true)
@Composable
fun GeoWavHomePreview() {
    val sampleConnections = listOf(
        GeoConnection("1", "Anushree", true),
        GeoConnection("2", "Akshat", true),
        GeoConnection("3", "Mummy", false)
    )

    val sampleZones = listOf(
        GeoZone("z1", "Home", true, 200),
        GeoZone("z2", "Office", false, 300)
    )

    val sampleAlerts = listOf(
        GeoAlert(
            "a1",
            "Entered Home",
            "200m • Sending auto updates to your circle",
            "5:42 PM",
            "enter"
        ),
        GeoAlert("a2", "Left Office", "You left Office • 3 mins ago", "3:12 PM", "exit"),
        GeoAlert("a2", "Entered Office", "300m • Sending auto updates to your circle", "9:15 AM", "enter"),
        GeoAlert("a2", "Left Home", "10 mins ago", "8:46 AM", "exit")
    )


//    GeoWavTheme {
//        GeoWavHomeScreen(
//            connections = sampleConnections,
//            zones = sampleZones,
//            alerts = sampleAlerts,
//            onViewMap = {},
//            onAddZone = {},
//            onShareLocation = {},
//            onOpenAlerts = {}
//        )
//    }
}

@Composable
fun ProfileCard(googleSignInClient: GoogleSignInClient, modifier: Modifier = Modifier){
    Card(
        modifier = modifier.fillMaxWidth().wrapContentHeight().padding(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
//                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Welcome,",
                    fontFamily = sora,
                    fontSize = 18.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                )

                Text(
                    text = googleSignInClient.getUserName(),
                    fontFamily = manrope,
                    fontSize = 32.sp,
                    color = Color.Black,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                )
            }

            val avatar by remember {
                mutableStateOf(googleSignInClient.getUserProfile().toString().isBlank())
            }

            val isDark = isSystemInDarkTheme()

            val imageUrl = remember(avatar, isDark) {
                if (avatar) {
                    if (isDark) {
                        "https://storage.googleapis.com/geowav-bucket-1/user_dark_theme.svg"
                    } else {
                        "https://storage.googleapis.com/geowav-bucket-1/user_light_theme.svg"
                    }
                } else {
                    googleSignInClient.getUserProfile() // ⚠️ Must return a *stable* String (not recompute every frame)
                }
            }

            val context = LocalContext.current
            val imageLoader = ImageLoader.Builder(context)
                .components {
                    add(SvgDecoder.Factory())
                }
                .build()

            Surface(
                shape = CircleShape,
                modifier = Modifier.size(96.dp),
                color = Color.White
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "User avatar",
                    imageLoader = imageLoader,
                    placeholder = painterResource(R.drawable.user),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(96.dp)
                )
            }

            //data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIzMiIgaGVpZ2h0PSIzMiIgZmlsbD0iI2JhYzNmZiIgdmlld0JveD0iMCAwIDI1NiAyNTYiPjxwYXRoIGQ9Ik0yMzAuOTIsMjEyYy0xNS4yMy0yNi4zMy0zOC43LTQ1LjIxLTY2LjA5LTU0LjE2YTcyLDcyLDAsMSwwLTczLjY2LDBDNjMuNzgsMTY2Ljc4LDQwLjMxLDE4NS42NiwyNS4wOCwyMTJhOCw4LDAsMSwwLDEzLjg1LDhjMTguODQtMzIuNTYsNTIuMTQtNTIsODkuMDctNTJzNzAuMjMsMTkuNDQsODkuMDcsNTJhOCw4LDAsMSwwLDEzLjg1LThaTTcyLDk2YTU2LDU2LDAsMSwxLDU2LDU2QTU2LjA2LDU2LjA2LDAsMCwxLDcyLDk2WiI+PC9wYXRoPjwvc3ZnPg==

//            Surface(
//                shape = CircleShape,
//                //color = MaterialTheme.colorScheme.background,
//                modifier = Modifier
//                    .size(96.dp)
//            ) {

          //  }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CurrentLocationCardPreview(){
    GeoWavTheme {
        CurrentLocationCard(
            "Ahmedabad",
            "3 min ago"
        ) { }
    }
}
