package com.aarav.geowav.presentation.navigation

import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.aarav.geowav.ui.theme.sora

@Composable
fun BottomNavigationBar(
    navController: NavController, modifier: Modifier = Modifier
) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val currentRoute = navBackStackEntry?.destination?.route

    //val currentRoute = navBackStackEntry?.destination?.route?.substringBefore("/")

    val navItems = listOf(NavItem.Home, NavItem.Activity, NavItem.YourPlaces)

//    Log.i("NAV", navController.currentBackStack.value.toString())
//    Log.i("NAV", navController.currentDestination.toString())

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 8.dp,
    ) {
        navItems.forEachIndexed { index, destination ->
            val isSelected = currentRoute?.startsWith(destination.path) == true

            NavigationBarItem(
                selected = isSelected,
                //selected = if(destination.name == "Home") true else false,
                onClick = {
                    Log.i("NAV", "BottomNavigationBar: $currentRoute, dest : ${destination.path}")
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
                    Text(destination.name, fontFamily = sora)
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