package com.aarav.geowav.presentation


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.R
import com.aarav.geowav.ui.theme.GeoWavTheme
import com.aarav.geowav.ui.theme.nunito

// ---------- THEME COLORS ----------
//val Primary = MaterialTheme.colorScheme.primary
//val Accent = Color(0xFF007AFF)
//val Tertiary = Color(0xFF7A869A)
//val Success = Color(0xFF00B894)

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
    connections: List<GeoConnection>,
    zones: List<GeoZone>,
    alerts: List<GeoAlert>,
    onViewMap: () -> Unit,
    onAddZone: () -> Unit,
    onShareLocation: () -> Unit,
    onOpenAlerts: () -> Unit,
    modifier: Modifier = Modifier
) {


    val Primary = MaterialTheme.colorScheme.primary
    val Secondary = MaterialTheme.colorScheme.secondary
    val Accent = MaterialTheme.colorScheme.tertiary
    val Success = MaterialTheme.colorScheme.surfaceTint
    val scroll = rememberScrollState()

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
                    painter = if (isSystemInDarkTheme()) painterResource(R.drawable.dark_bg_geowav) else painterResource(
                        R.drawable.light_bg_geowav
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
                        .padding(start = 12.dp, end = 12.dp, top = 48.dp)
                ) {
                    Text(
                        text = "GeoWav",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = MaterialTheme.colorScheme.background
                        ),
                        fontFamily = nunito,
                        modifier = Modifier.weight(1.0f),
                        fontWeight = FontWeight.Bold
                    )

                    Image(
                        painter = painterResource(R.drawable.bell),
                        contentDescription = "bell",
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(28.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.background)
                    )

                    Image(
                        painter = painterResource(R.drawable.gear_six),
                        contentDescription = "setting",
                        modifier = Modifier.size(28.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.background)
                    )
                }

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
                modifier = Modifier.padding(12.dp)
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
                        fontFamily = nunito
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
                        fontFamily = nunito,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Last updated • $lastUpdated",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Accent,
                        fontFamily = nunito
                    )
                )
            }

            Button(
                onClick = onViewMap,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("View", color = Color.White, fontFamily = nunito)
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
                    fontFamily = nunito
                ),
                fontSize = 16.sp,
                modifier = Modifier.weight(1.0f)
            )
            TextButton(onClick = { }) {
                Text(
                    "Manage",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = nunito
                )
            }
        }

        LazyRow(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                    fontWeight = FontWeight.Bold,
                    fontFamily = nunito
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
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = nunito),
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
                    fontFamily = nunito
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
                fontFamily = nunito
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
            .height(72.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
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
                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = nunito)
                    )
                    Text(
                        "${zone.radiusMeters} m • ${if (zone.inside) "Inside" else "Outside"}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontFamily = nunito
                        ),
                        fontSize = 12.sp
                    )
                }
            }
            TextButton(onClick = onClick) {
                Text(
                    "Edit",
                    fontSize = 14.sp,
                    fontFamily = nunito,
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
        onClick = {}
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
                    fontFamily = nunito,
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
                    fontFamily = nunito
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
                .padding(12.dp),
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
                        fontFamily = nunito
                    )
                )
                Text(
                    alert.subtitle,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = nunito),
                    maxLines = 2
                )
            }
            Text(alert.time,
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
        GeoConnection("3", "Aayush", false)
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

    GeoWavTheme {
        GeoWavHomeScreen(
            connections = sampleConnections,
            zones = sampleZones,
            alerts = sampleAlerts,
            onViewMap = {},
            onAddZone = {},
            onShareLocation = {},
            onOpenAlerts = {}
        )
    }
}
