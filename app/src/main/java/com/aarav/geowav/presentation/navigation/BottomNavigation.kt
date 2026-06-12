package com.aarav.geowav.presentation.navigation

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.aarav.geowav.R
import com.aarav.geowav.presentation.theme.manrope
import com.aarav.geowav.presentation.theme.sora

@Composable
fun BottomNavigationBar(
    navController: NavController, modifier: Modifier = Modifier
) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val currentRoute = navBackStackEntry?.destination?.route


    val navItems =
        listOf(NavItem.Home, NavItem.LocationSharing, NavItem.Activity, NavItem.YourPlaces)


    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 8.dp,
    ) {


        navItems.forEachIndexed { index, destination ->
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
                        contentDescription = "icon",
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

    val navItems = remember {
        listOf(
            NavItem.Home,
            NavItem.LocationSharing,
            NavItem.Activity,
            NavItem.YourPlaces
        )
    }

    Surface(
        modifier = modifier
            .navigationBarsPadding()
            .wrapContentWidth()
            .height(90.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(40.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            navItems.forEach { item ->

                val selected =
                    currentRoute?.startsWith(item.path) == true


                CustomBottomNavItem(
                    navItem = item,
                    selected = selected,
                    modifier = Modifier.padding(2.dp).weight(1f),
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

@Composable
fun CustomBottomNavItem(
    navItem: NavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val backgroundColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.surfaceBright.copy(0.8f)
        } else {
            Color.Transparent
        },
        label = ""
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = ""
    )

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(32.dp))
            .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(),
            onClick = onClick
        ),
        shape = RoundedCornerShape(32.dp),
        color = backgroundColor
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Icon(
                painter = painterResource(navItem.icon),
                contentDescription = navItem.name,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )

//            AnimatedVisibility(
//                visible = selected,
//                enter = fadeIn() + expandVertically(),
//                exit = fadeOut() + shrinkVertically()
//            ) {

            Spacer(Modifier.height(2.dp))

                Text(
                    text = navItem.name,
                    color = contentColor,
                    fontFamily = manrope,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelMedium
                )
           // }
        }
    }
}