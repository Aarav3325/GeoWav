package com.aarav.geowav.presentation


import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import com.aarav.geowav.R
import com.aarav.geowav.data.model.GeoConnection
import com.aarav.geowav.data.place.Place
import com.aarav.geowav.domain.authentication.GoogleSignInClient
import com.aarav.geowav.presentation.components.SnackbarManager
import com.aarav.geowav.presentation.home.HomeScreenVM
import com.aarav.geowav.ui.theme.GeoWavTheme
import com.aarav.geowav.ui.theme.manrope
import com.aarav.geowav.ui.theme.sora
import kotlinx.coroutines.launch
import kotlin.text.equals


// ---------- DATA CLASSES ----------

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
    zones: List<GeoZone>,
    alerts: List<GeoAlert>,
    onViewMap: () -> Unit,
    onAddZone: () -> Unit,
    onShareLocation: () -> Unit,
    onOpenAlerts: () -> Unit,
    homeScreenVM: HomeScreenVM,
    modifier: Modifier = Modifier
) {

    val allConnection by homeScreenVM.allConnections.collectAsState()

    val recentAlerts by homeScreenVM.alerts.collectAsState()

    val finalRecentAlerts = recentAlerts.take(4)


    val allPlaces by homeScreenVM.allPlaces.collectAsState()

    val scope = rememberCoroutineScope()

    val scroll = rememberScrollState()
    val scrollOffset = scroll.value

// Switch colors after scrolling 240px
    val useDarkIcons = scrollOffset > 150

// Animate colors smoothly
    val textColor by animateColorAsState(
        targetValue = if (useDarkIcons) {
            if (isSystemInDarkTheme()) {
                Color.White
            } else {
                Color.Black
            }
        } else {
            if (isSystemInDarkTheme()) {
                Color.Black
            } else {
                Color.Black
            }
        },
        animationSpec = tween(durationMillis = 800), // smooth 0.5s fade
        label = "TextColorAnimation"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (useDarkIcons)
            MaterialTheme.colorScheme.primaryContainer
        else
            Color.Transparent,
        animationSpec = tween(durationMillis = 800),
        label = "BackgroundColorAnimation"
    )

    val context = LocalContext.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "GeoWav",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = textColor
                        ),
                        fontFamily = manrope,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                               // Toast.makeText(context, "Welcome to GeoWav", Toast.LENGTH_LONG).show()
                                SnackbarManager.showMessage("No Notifications")
                            }
                        }
                    ) {
                        Image(
                            painter = painterResource(R.drawable.bell),
                            contentDescription = "bell",
                            modifier = Modifier.size(28.dp),
                            colorFilter = ColorFilter.tint(textColor)
                        )
                    }

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
                            colorFilter = ColorFilter.tint(textColor)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
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

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.background.copy(alpha = 0.1f),
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                                    )
                                )
                            )
                    )

//                    Row(
////                    horizontalArrangement = Arrangement.SpaceBetween,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .align(Alignment.TopStart)
//                            .padding(start = 12.dp, end = 12.dp, top = 48.dp, bottom = 12.dp)
//                    ) {
//                        Text(
//                            text = "GeoWav",
//                            style = MaterialTheme.typography.headlineLarge.copy(
//                                color = Color.Black
//                            ),
//                            fontFamily = manrope,
//                            modifier = Modifier.weight(1.0f),
//                            fontWeight = FontWeight.Bold
//                        )
//
//                        IconButton(
//                            onClick = {
//                                scope.launch {
//                                    SnackbarManager.showMessage("No Notifications")
//                                }
//                            }
//                        ) {
//                            Image(
//                                painter = painterResource(R.drawable.bell),
//                                contentDescription = "bell",
//                                modifier = Modifier
//                                    .size(28.dp),
//                                colorFilter = ColorFilter.tint(Color.Black)
//                            )
//                        }
//
//                        Spacer(modifier = Modifier.width(10.dp))
//
//                        IconButton(
//                            onClick = {
//                                scope.launch {
//                                    googleSignInClient.signOut()
//                                    navigateToAuth()
//                                }
//                            }
//                        ) {
//                            Image(
//                                painter = painterResource(R.drawable.gear_six),
//                                contentDescription = "setting",
//                                modifier = Modifier.size(28.dp),
//                                colorFilter = ColorFilter.tint(Color.Black)
//                            )
//                        }
//                    }


                    ProfileCard(
                        googleSignInClient,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 92.dp)
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
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
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
                        connections = allConnection,
                        onAdd = onAddZone
                    )

                    // 🏠 Active Zones
                    ActiveZonesSection(
                        zones = allPlaces,
                        onZoneClick = {}
                    )

                    // ⚡ Quick Actions
                    if(allPlaces.isEmpty()){
                        QuickActionsRow(
                            onAddZone = onAddZone,
                            onShare = onShareLocation,
                            onAlerts = onOpenAlerts
                        )
                    }

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
                    RecentAlertsList(finalRecentAlerts)
                }


                // 📍 Current Location Card

            }
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
fun ConnectionsRow(title: String, connections: List<GeoConnection>?, onAdd: () -> Unit) {

    val Primary = MaterialTheme.colorScheme.primary
    val Secondary = MaterialTheme.colorScheme.secondary
    val Accent = MaterialTheme.colorScheme.tertiary
    val Success = MaterialTheme.colorScheme.surfaceTint
    Column(
        modifier = Modifier.padding(top = 6.dp)
    ) {
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
            if (connections.isNullOrEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .height(50.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.empty),
                            contentDescription = "empty icon",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary)
                        )

                        Text("No connections yet",
                            fontFamily = sora,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 16.sp)
                    }
                }
            } else {
                items(connections) { conn ->
                    ConnectionCard(conn)
                }
            }
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
                conn.name?.take(1).toString(),
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
            conn.name.toString(),
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .width(84.dp)
            .padding(start = 8.dp)) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.tertiary, CircleShape)
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = MaterialTheme.colorScheme.tertiary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add",
            fontFamily = sora,
            fontSize = 14.sp,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = sora),
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

// ---------- ZONES ----------
@Composable
fun ActiveZonesSection(zones: List<Place>, onZoneClick: (_root_ide_package_.com.aarav.geowav.data.place.Place) -> Unit) {
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
            if(zones.isNotEmpty()){
                zones.forEach { zone -> ZoneCard(zone = zone, onClick = { onZoneClick(zone) }) }
            }
            else{
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.navigation_arrow),
                        contentDescription = "empty icon",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary)
                    )

                    Text("No Zones are addded yet",
                        fontFamily = sora,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ZoneCard(zone: Place, onClick: () -> Unit) {

    val Primary = MaterialTheme.colorScheme.primary
    val Secondary = MaterialTheme.colorScheme.secondary
    val Tertiary = MaterialTheme.colorScheme.tertiary
    val Success = MaterialTheme.colorScheme.surfaceTint
    //val statusColor = if (zone.inside) Secondary else Tertiary
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
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
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
                        if(zone.customName.toString().isNotEmpty()){
                            zone.customName
                        }else{
                            zone.placeName
                        },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = sora
                        )
                    )
                    Text(
                        "${zone.radius.toInt()}m • ENTER/EXIT Trigger",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.secondary,
                            fontFamily = sora
                        ),
                        fontSize = 12.sp
                    )
                }
            }
            TextButton(onClick = {  }) {
                Text(
                    "Active",
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
        elevation = ButtonDefaults.elevatedButtonElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
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
fun RecentAlertsList(alerts: List<com.aarav.geowav.data.model.GeoAlert>) {

    val Primary = MaterialTheme.colorScheme.primary
    val Secondary = MaterialTheme.colorScheme.secondary
    val Accent = MaterialTheme.colorScheme.tertiary
    val Success = MaterialTheme.colorScheme.surfaceTint
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
        .fillMaxWidth().padding(bottom = 24.dp)
    ) {
        if (alerts.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                Image(
                    painter = painterResource(R.drawable.link_break),
                    contentDescription = "break",
                    modifier = Modifier.size(48.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "No recent alerts. You’re all clear!",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = sora
                    )
                )
            }
        } else {
            alerts.forEach { alert -> AlertItem(alert) }
        }
    }
}

@Composable
fun AlertItem(alert: com.aarav.geowav.data.model.GeoAlert) {

    val type = if (alert.type.equals("ENTER", ignoreCase = true)) "enter" else "exit"

    val enter = Color(0xFF00513f)

    val relativeTime = buildRelativeSubtitle(type, alert.readableTime)

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
                    relativeTime,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = sora),
                    maxLines = 2
                )
            }
            Text(
                alert.time,
                fontFamily = sora,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

// ---------- PREVIEW ----------
@Preview(showBackground = true)
@Composable
fun GeoWavHomePreview() {
//    val sampleConnections = listOf(
//        GeoConnection("1", "Anushree"),
//        GeoConnection("2", "Akshat"),
//        GeoConnection("3", "Mummy")
//    )

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
        GeoAlert(
            "a2",
            "Entered Office",
            "300m • Sending auto updates to your circle",
            "9:15 AM",
            "enter"
        ),
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
fun ProfileCard(googleSignInClient: GoogleSignInClient, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp),
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


            val uri by remember {
                mutableStateOf(
                    googleSignInClient.getUserProfile()
                )
            }

            val avatar by remember {
                mutableStateOf(uri.toString().isBlank())
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
                    uri
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
                modifier = Modifier.size(84.dp),
                color = Color.White
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "User avatar",
                    imageLoader = imageLoader,
                    placeholder = painterResource(R.drawable.user),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(84.dp)
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun CurrentLocationCardPreview() {
    GeoWavTheme {
        CurrentLocationCard(
            "Ahmedabad",
            "3 min ago"
        ) { }
    }
}

fun buildRelativeSubtitle(type: String, timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val minutes = diff / 60000
    val hours = diff / (60000 * 60)

    val verb = when (type) {
        "enter" -> "Reached"
        else    -> "Left"
    }

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min${if (minutes > 1) "s" else ""} ago"
        hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
        else -> {
            val df = java.text.SimpleDateFormat("dd MMM, h:mm a", java.util.Locale.getDefault())
            "$verb on ${df.format(java.util.Date(timestamp))}"
        }
    }
}
