package com.aarav.geowav.presentation.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Typography
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aarav.geowav.ui.theme.sora
import kotlinx.coroutines.delay

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.aarav.geowav.domain.authentication.GoogleSignInClient
import com.aarav.geowav.presentation.components.SnackbarManager
import com.aarav.geowav.ui.theme.GeoWavTheme
import com.aarav.geowav.ui.theme.manrope
import com.aarav.geowav.ui.theme.sora
import kotlinx.coroutines.launch

// ---------- DATA CLASSES (Unchanged) ----------
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
    val type: String // "enter" or "exit"
)

// ---------- MAIN HOME SCREEN ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeoWavHomeScreen(
    navigateToAuth: () -> Unit,
    //googleSignInClient: GoogleSignInClient,
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
    val scroll = rememberScrollState()
    val scrollOffset = scroll.value


     // Threshold for switching header colors
    val headerTransitionThreshold = 180
    val isScrolledPastHeader = scrollOffset > headerTransitionThreshold
    val isDarkTheme = isSystemInDarkTheme()

    // Animate TopBar Colors
    val topBarContentColor by animateColorAsState(
        targetValue = if (isScrolledPastHeader) MaterialTheme.colorScheme.onSurface else Color.White,
        animationSpec = tween(durationMillis = 500),
        label = "TextColorAnimation"
    )

    val topBarContainerColor by animateColorAsState(
        targetValue = if (isScrolledPastHeader) MaterialTheme.colorScheme.surface else Color.Transparent,
        animationSpec = tween(durationMillis = 500),
        label = "BackgroundColorAnimation"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isScrolledPastHeader) "GeoWav" else "",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = manrope,
                        fontWeight = FontWeight.Bold,
                        color = topBarContentColor
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch { SnackbarManager.showMessage("No Notifications") }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = topBarContentColor
                        )
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                               // googleSignInClient.signOut()
                                navigateToAuth()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = topBarContentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarContainerColor,
                    scrolledContainerColor = topBarContainerColor
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Scrollable Content
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // 1. Hero / Header Section
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Background Image
                    Image(
                        painter = if (isDarkTheme)
                            painterResource(R.drawable.dark_bg_geowav_new_2)
                        else
                            painterResource(R.drawable.light_bg_geowav_new),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    )

                    // Gradient Overlay for text readability
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.3f),
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                            )
                    )

                    // Profile Card Overlay
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(top = 100.dp, bottom = 20.dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        ProfileCard()
                    }
                }

                // 2. Main Content Body
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .offset(y = (-10).dp) // Slight pull up to bridge gap
                ) {

                    // Quick Actions Grid
                    QuickActionsRow(
                        onAddZone = onAddZone,
                        onShare = onShareLocation,
                        onAlerts = onOpenAlerts
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Connections
                    SectionHeader(title = "Your Circle", actionText = "Manage", onAction = {})
                    ConnectionsRow(
                        connections = connections,
                        onAdd = onAddZone
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Zones
                    SectionHeader(title = "Active Zones")
                    ActiveZonesSection(
                        zones = zones,
                        onZoneClick = {}
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Alerts
                    SectionHeader(title = "Recent Activity", actionText = "View All", onAction = onOpenAlerts)
                    RecentAlertsList(alerts)

                    // Bottom Padding for scroll
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

// ---------- SUB-COMPONENTS ----------

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = manrope,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        if (actionText != null && onAction != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = sora,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onAction() }
            )
        }
    }
}

@Composable
fun ProfileCard(
    //googleSignInClient: GoogleSignInClient
) {
    val context = LocalContext.current
    //val userName = googleSignInClient.getUserName()

    // Image Loader Logic
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }

//    //val userPhoto = googleSignInClient.getUserProfile().toString()
//    val isDark = isSystemInDarkTheme()
//    val avatarUrl = remember(userPhoto, isDark) {
//        if (userPhoto.isBlank()) {
//            if (isDark) "https://storage.googleapis.com/geowav-bucket-1/user_dark_theme.svg"
//            else "https://storage.googleapis.com/geowav-bucket-1/user_light_theme.svg"
//        } else {
//            userPhoto
//        }
//    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            Text(
                text = "Welcome back,",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = sora,
                color = Color.White.copy(alpha = 0.9f)
            )
            Text(
                text = "Aarav",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = manrope
                ),
                color = Color.White
            )
        }

        Surface(
            shape = CircleShape,
            shadowElevation = 8.dp,
            modifier = Modifier.size(80.dp),
            border = androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.background)
        ) {
            AsyncImage(
                model = "https://storage.googleapis.com/geowav-bucket-1/user.svg",
                contentDescription = "Profile",
                imageLoader = imageLoader,
                placeholder = painterResource(R.drawable.user), // Ensure this resource exists or remove
                error = painterResource(R.drawable.user),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun QuickActionsRow(onAddZone: () -> Unit, onShare: () -> Unit, onAlerts: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // We use weight to make them distribute evenly
        QuickActionButton(
            icon = Icons.Default.Add,
            label = "New Zone",
            onClick = onAddZone,
            modifier = Modifier.weight(1f),
            highlight = true
        )
        QuickActionButton(
            icon = Icons.Default.LocationOn,
            label = "Share Loc",
            onClick = onShare,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            icon = Icons.Default.Notifications,
            label = "Alerts",
            onClick = onAlerts,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    val containerColor = if (highlight) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
    val contentColor = if (highlight) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    Card(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = sora),
                color = contentColor
            )
        }
    }
}

@Composable
fun ConnectionsRow(connections: List<GeoConnection>, onAdd: () -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        item { AddConnectionButton(onClick = onAdd) }
        items(connections) { conn -> ConnectionItem(conn) }
    }
}

@Composable
fun ConnectionItem(conn: GeoConnection) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp)) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = conn.name.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = sora,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            // Online Indicator
            if (conn.isOnline) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(16.dp)
                        .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50)) // Green
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = conn.name,
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = sora),
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AddConnectionButton(onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp)) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                .clickable { onClick() }
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = MaterialTheme.colorScheme.outline
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add",
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = sora),
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun ActiveZonesSection(zones: List<GeoZone>, onZoneClick: (GeoZone) -> Unit) {
    if (zones.isEmpty()) {
        Text(
            text = "No active zones set.",
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = sora),
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            zones.forEach { zone -> ZoneCard(zone, onClick = { onZoneClick(zone) }) }
        }
    }
}

@Composable
fun ZoneCard(zone: GeoZone, onClick: () -> Unit) {
    val isActive = zone.inside
    val containerColor = if (isActive) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer
    val iconColor = if (isActive) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = iconColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = zone.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = sora,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${zone.radiusMeters}m radius",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = manrope),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status Badge
            Surface(
                color = if(isActive) Color(0xFFE7F8ED) else Color(0xFFF5F5F5), // Light Green / Grey
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isActive) "Inside" else "Outside",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if(isActive) Color(0xFF1E8E3E) else Color.Gray
                )
            }
        }
    }
}

@Composable
fun RecentAlertsList(alerts: List<GeoAlert>) {
    if (alerts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No recent activity.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = sora,
                    color = MaterialTheme.colorScheme.outline
                )
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            alerts.forEach { alert -> AlertItem(alert) }
        }
    }
}

@Composable
fun AlertItem(alert: GeoAlert) {
    val isEntry = alert.type == "enter"

    // Semantic styling based on logic
   // val icon = if (isEntry) Icons.Rounded.Login else Icons.Rounded.Logout
    val (bgColor, iconTint) = if (isSystemInDarkTheme()) {
        if (isEntry) 0xFF0F291E to 0xFFA3F2D6 else 0xFF2C1517 to 0xFFFFDAD6
    } else {
        if (isEntry) 0xFFECFDF5 to 0xFF059669 else 0xFFFFF1F2 to 0xFFE11D48
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Box
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(bgColor)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(iconTint),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = alert.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = sora
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = alert.subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = manrope
                ),
                maxLines = 1
            )
        }

        Text(
            text = alert.time,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.outline,
                fontFamily = sora
            )
        )
    }
}

// ---------- PREVIEW & MOCK DATA ----------

// Mock implementation for Preview


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GeoWavHomePreview() {
    val sampleConnections = listOf(
        GeoConnection("1", "Anushree", true),
        GeoConnection("2", "Akshat", true),
        GeoConnection("3", "Mom", false),
        GeoConnection("4", "Dad", false)
    )

    val sampleZones = listOf(
        GeoZone("z1", "Home", true, 200),
        GeoZone("z2", "Office", false, 300)
    )

    val sampleAlerts = listOf(
        GeoAlert("a1", "Entered Home", "Tracking active", "5:42 PM", "enter"),
        GeoAlert("a2", "Left Office", "Duration: 4h 20m", "3:12 PM", "exit"),
        GeoAlert("a3", "Entered Gym", "Tracking active", "9:00 AM", "enter")
    )

    GeoWavTheme {
        GeoWavHomeScreen(
            navigateToAuth = {},
           // googleSignInClient = MockGoogleSignInClient(),
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
