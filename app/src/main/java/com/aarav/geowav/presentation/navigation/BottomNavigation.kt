package com.aarav.geowav.presentation.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.aarav.geowav.presentation.theme.manrope

@Composable
fun BottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val navItems = listOf(NavItem.Home, NavItem.LocationSharing, NavItem.Activity, NavItem.YourPlaces)

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 8.dp,
    ) {
        navItems.forEach { destination ->
            val isSelected = currentRoute?.startsWith(destination.path) == true

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != destination.path) {
                        navController.navigate(destination.path) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(destination.icon),
                        contentDescription = destination.name,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(destination.name, fontFamily = manrope)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
fun CustomBottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val colorScheme = MaterialTheme.colorScheme

    val navItems = remember {
        listOf(
            NavItem.Home,
            NavItem.LocationSharing,
            NavItem.Activity,
            NavItem.YourPlaces
        )
    }
    val selectedIndex = navItems.indexOfFirst { item ->
        currentRoute?.startsWith(item.path) == true
    }.coerceAtLeast(0)
    val barShape = RoundedCornerShape(34.dp)

    Surface(
        modifier = modifier
            .navigationBarsPadding()
            .fillMaxWidth()
            .height(96.dp)
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
        shape = barShape,
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(
            width = 0.5.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.46f),
                    colorScheme.outlineVariant.copy(alpha = 0.26f),
                    colorScheme.primary.copy(alpha = 0.16f)
                )
            )
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .clip(barShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorScheme.surfaceBright.copy(alpha = 0.88f),
                            colorScheme.surfaceContainer.copy(alpha = 0.78f),
                            colorScheme.surfaceContainerLow.copy(alpha = 0.70f)
                        )
                    )
                )
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            colorScheme.primary.copy(alpha = 0.10f),
                            Color.Transparent,
                            colorScheme.secondary.copy(alpha = 0.08f)
                        )
                    )
                )
                .padding(4.dp)
        ) {
            val itemWidth = maxWidth / navItems.size
            val indicatorOffset by animateDpAsState(
                targetValue = itemWidth * selectedIndex,
                animationSpec = spring(
                    dampingRatio = 0.76f,
                    stiffness = 420f
                ),
                label = "bottomNavIndicatorOffset"
            )

            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(itemWidth)
                    .fillMaxHeight()
                    .padding(2.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(colorScheme.primaryContainer.copy(alpha = 0.74f))
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    val selected = currentRoute?.startsWith(item.path) == true

                    CustomBottomNavItem(
                        navItem = item,
                        selected = selected,
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f),
                        onClick = {
                            if (currentRoute != item.path) {
                                navController.navigate(item.path) {
                                    launchSingleTop = true
                                    restoreState = true

                                    popUpTo(
                                        navController.graph.findStartDestination().id
                                    ) {
                                        saveState = true
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomBottomNavItem(
    navItem: NavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.86f)
        },
        label = "bottomNavContentColor"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 0.96f,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 520f),
        label = "bottomNavIconScale"
    )
    val itemAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.86f,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 500f),
        label = "bottomNavItemAlpha"
    )
    val labelOffset by animateDpAsState(
        targetValue = if (selected) 0.dp else 2.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 520f),
        label = "bottomNavLabelOffset"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = itemAlpha
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(navItem.icon),
                contentDescription = navItem.name,
                tint = contentColor,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                        translationY = if (selected) -1.5f else 0f
                    }
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = navItem.name,
                color = contentColor,
                fontFamily = manrope,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.offset(y = labelOffset)
            )
        }
    }
}
