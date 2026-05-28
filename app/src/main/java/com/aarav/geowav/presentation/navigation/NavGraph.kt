package com.aarav.geowav.presentation.navigation

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.aarav.geowav.core.permissions.GeoPermissionUiState
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.data.model.User
import com.aarav.geowav.presentation.activity.ActivityScreen
import com.aarav.geowav.presentation.addplace.AddPlaceScreen
import com.aarav.geowav.presentation.auth.LoginScreen
import com.aarav.geowav.presentation.auth.SignupScreen
import com.aarav.geowav.presentation.circle.CircleScreen
import com.aarav.geowav.presentation.home.GeoWavHomeScreen
import com.aarav.geowav.presentation.home.HomeScreenVM
import com.aarav.geowav.presentation.locationsharing.LocationSharingScreen
import com.aarav.geowav.presentation.map.MapScreen
import com.aarav.geowav.presentation.observe.ObserveScreen
import com.aarav.geowav.presentation.onboard.OnboardingScreen
import com.aarav.geowav.presentation.paywall.PaywallScreen
import com.aarav.geowav.presentation.profile.ProfileScreen
import com.aarav.geowav.presentation.profile.ThemeMode
import com.aarav.geowav.presentation.subscription.SubscriptionViewModel
import com.aarav.geowav.presentation.timeline.TimelineMapPreview
import com.aarav.geowav.presentation.timeline.TimelineScreen
import com.aarav.geowav.presentation.yourplace.YourPlacesScreen
import com.google.android.gms.maps.model.LatLng

@Composable
fun NavGraph(
    isDarkThemeEnabled: Boolean,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    navHostController: NavHostController,
    subscriptionVM: SubscriptionViewModel,
    sharedPreferences: SharedPreferences,
    location: Pair<Double, Double>?,
    permissionState: GeoPermissionUiState,
    googleSignInClient: GoogleSignInClient,
    modifier: Modifier
) {
    val isLoggedIn = remember { googleSignInClient.isLoggedIn() }
    val isOnboarded = remember { sharedPreferences.getBoolean("isOnboarded", false) }
    val startDestination = when {
        isLoggedIn && isOnboarded -> "home_graph"
        !isOnboarded -> NavRoute.OnBoard.path
        !isLoggedIn && isOnboarded -> NavRoute.Login.path
        else -> NavRoute.SignUp.path
    }

    NavHost(
        modifier = modifier,
        navController = navHostController,
        startDestination = startDestination
    ) {
        AddMapsScreen(
            isDarkThemeEnabled,
            navHostController,
            this,
            location,
            permissionState.foregroundLocationGranted
        )

        AddNewPlaceScreen(
            isDarkThemeEnabled,
            navHostController,
            this,
            subscriptionVM,
            permissionState.locationServicesReady
        )

        AddYourPlacesScreen(
            navHostController,
            this,
            subscriptionVM
        )

        AddSignUpScreen(
            navHostController,
            this
        )

        AddLoginScreen(
            navHostController,
            this
        )

        AddOnBoard(
            navHostController,
            this
        )

//        AddHomeScreen(
//            isDarkThemeEnabled,
//            navHostController,
//            this
//        )

        AddActivityScreen(
            isDarkThemeEnabled,
            navHostController,
            this,
            subscriptionVM
        )

        AddCircleScreen(
            navHostController,
            this,
            subscriptionVM
        )

        AddLocationSharingScreen(
            navHostController,
            this,
            location,
            subscriptionVM,
            permissionState.locationServicesReady
        )

        AddTimelineScreen(
            navHostController,
            this,
            subscriptionVM
        )

        AddTimelinePreviewScreen(
            navHostController,
            this,
            subscriptionVM
        )

        AddPaywallScreen(
            navHostController,
            this,
            subscriptionVM
        )

//        AddObserveScreen(
//            navHostController,
//            this
//        )

        navigation(
            route = "home_graph",
            startDestination = NavRoute.HomeScreen.path
        ) {

            AddHomeScreen(
                isDarkThemeEnabled,
                navHostController,
                this,
                subscriptionVM
            )

            AddObserveScreen(
                navHostController,
                this,
                location
            )
        }


        AddProfileScreen(
            isDarkThemeEnabled,
            subscriptionVM,
            themeMode,
            onThemeChange,
            navHostController,
            this
        )
    }

}

fun AddMapsScreen(
    isDarkThemeEnabled: Boolean,
    navController: NavController,
    navGraphBuilder: NavGraphBuilder,
    location: Pair<Double, Double>?,
    hasForegroundLocationPermission: Boolean
) {
    navGraphBuilder.composable(
        route = NavRoute.MapScreen.path
    ) {
        MapScreen(
            isDarkThemeEnabled,
            mapViewModel = hiltViewModel(),
            location,
            hasForegroundLocationPermission = hasForegroundLocationPermission,
            navigateToAddPlace = { id ->
                navController.navigate(NavRoute.AddPlace.createRoute(id))
            },
            navigateToManualAddPlace = { lat, lng, address ->
                navController.navigate(NavRoute.ManualAddPlace.createRoute(lat, lng, address))
            },
            navigateToSettings = {
                navController.navigate(NavRoute.Settings.path)
            },
            navigateToHome = {
                navController.navigateUp()
            }
        )
    }
}

fun AddNewPlaceScreen(
    isDarkThemeEnabled: Boolean,
    navController: NavController,
    navGraphBuilder: NavGraphBuilder,
    subscriptionVM: SubscriptionViewModel,
    locationServicesReady: Boolean
) {
    navGraphBuilder.composable(
        route = NavRoute.ManualAddPlace.path.plus("/{lat}/{lng}?address={address}"),
        arguments = listOf(
            navArgument("lat") {
                type = NavType.StringType
            },
            navArgument("lng") {
                type = NavType.StringType
            },
            navArgument("address") {
                type = NavType.StringType
                defaultValue = "Approximate location"
            }
        )
    ) {
        val lat = it.arguments?.getString("lat")?.toDoubleOrNull()
        val lng = it.arguments?.getString("lng")?.toDoubleOrNull()
        val address = it.arguments?.getString("address") ?: "Approximate location"
        val manualLatLng = if (lat != null && lng != null) LatLng(lat, lng) else null

        AddPlaceScreen(
            isDarkThemeEnabled,
            placeId = null,
            manualLatLng = manualLatLng,
            manualAddress = address,
            navigateToMaps = {
                navController.navigateUp()
            },
            navigateToPaywall = {
                navController.navigate(NavRoute.Paywall.path)
            },
            navigateToYourPlaces = {
                navController.navigate(NavRoute.YourPlaces.path) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            },
            navigateToSettings = {
                navController.navigate(NavRoute.Settings.path)
            },
            locationServicesReady = locationServicesReady,
            subscriptionVM = subscriptionVM,
            placeViewModel = hiltViewModel()
        )
    }

    navGraphBuilder.composable(
        route = NavRoute.AddPlace.path.plus("/{placeId}"),
        arguments = listOf(
            navArgument("placeId") {
                type = NavType.StringType
            }
        )
    ) {
        val placeId = it.arguments?.getString("placeId").orEmpty()

        AddPlaceScreen(
            isDarkThemeEnabled,
            placeId = placeId,
            navigateToMaps = {
                navController.navigateUp()
            },
            navigateToPaywall = {
                navController.navigate(NavRoute.Paywall.path)
            },
            navigateToYourPlaces = {
                navController.navigate(NavRoute.YourPlaces.path) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            },
            navigateToSettings = {
                navController.navigate(NavRoute.Settings.path)
            },
            locationServicesReady = locationServicesReady,
            subscriptionVM = subscriptionVM,
            placeViewModel = hiltViewModel()
        )

    }
}

fun AddYourPlacesScreen(
    navController: NavController,
    navGraphBuilder: NavGraphBuilder,
    subscriptionVM: SubscriptionViewModel
) {
    navGraphBuilder.composable(
        route = NavRoute.YourPlaces.path
    ) {
        YourPlacesScreen(
            yourPlacesVM = hiltViewModel(),
            subscriptionVM = subscriptionVM,
            navigateToPaywall = {
                navController.navigate(NavRoute.Paywall.path)
            },
            navigateToMap = {
                navController.navigate(NavRoute.MapScreen.path)
            }
        )
    }
}

fun AddSignUpScreen(
    navController: NavController,
    navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.composable(
        route = NavRoute.SignUp.path
    ) {
        SignupScreen(
            signUpVM = hiltViewModel(),
            navigateToHome = {
                navController.navigate(NavRoute.HomeScreen.path) {
                    popUpTo(NavRoute.SignUp.path) {
                        inclusive = true
                    }
                }
            },
            navigateToLogin = {
                navController.navigate(NavRoute.Login.path) {
                    popUpTo(NavRoute.SignUp.path) {
                        inclusive = true
                    }
                }
            }
        )
    }
}

fun AddLoginScreen(
    navController: NavController,
    navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.composable(
        route = NavRoute.Login.path
    ) {
        LoginScreen(
            loginVM = hiltViewModel(),
            navigateToHome = {
                navController.navigate(NavRoute.HomeScreen.path) {
                    popUpTo(NavRoute.Login.path) {
                        inclusive = true
                    }
                }
            },
            navigateToSignUp = {
                navController.navigate(NavRoute.SignUp.path) {
                    popUpTo(NavRoute.Login.path) {
                        inclusive = true
                    }
                }
            }
        )
    }
}

fun AddOnBoard(
    navController: NavController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.composable(
        route = NavRoute.OnBoard.path
    ) {
        OnboardingScreen(
            navigateToAuth = {
                navController.navigate(NavRoute.SignUp.path) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = true
                    }
                }
            },
            onBoardVM = hiltViewModel()
        )
    }
}

fun AddHomeScreen(
    isDarkThemeEnabled: Boolean,
    navController: NavController,
    navGraphBuilder: NavGraphBuilder,
    subscriptionVM: SubscriptionViewModel
) {
    navGraphBuilder.composable(
        route = NavRoute.HomeScreen.path
    ) { backStackEntry ->

        val parentEntry = remember(backStackEntry) {
            navController.getBackStackEntry("home_graph")
        }

        val sharedVM: HomeScreenVM = hiltViewModel(parentEntry)

        GeoWavHomeScreen(
            isDarkThemeEnabled = isDarkThemeEnabled,
            navigateToYourPlaces = {
                navController.navigate(NavRoute.YourPlaces.path) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onAddZone = {
                navController.navigate(NavRoute.MapScreen.path)
            },
            navigateToObserve = {
                navController.navigate(NavRoute.ObserveUsers.path)
            },
            homeScreenVM = sharedVM,
            subscriptionViewModel = subscriptionVM,
            navigateToSettings = {
                navController.navigate(NavRoute.Settings.path)
            },
            navigateToPaywall = {
                navController.navigate(NavRoute.Paywall.path)
            },
            navigateToCircle = {
                navController.navigate(NavRoute.Circle.path)
            },
            navigateToTimeline = { userId, name ->
                navController.navigate(NavRoute.TimeLine.createRoute(userId, name))
            },
            navigateToActivity = {
                navController.navigate(NavRoute.ActivityScreen.path) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}

fun AddActivityScreen(
    isDarkThemeEnabled: Boolean,
    navController: NavController,
    navGraphBuilder: NavGraphBuilder,
    subscriptionVM: SubscriptionViewModel
) {
    navGraphBuilder.composable(
        route = NavRoute.ActivityScreen.path
    ) {
        ActivityScreen(
            isDarkThemeEnabled,
            activityViewModel = hiltViewModel(),
            subscriptionViewModel = subscriptionVM,
            navigateToPaywall = {
                navController.navigate(NavRoute.Paywall.path)
            }
        )
    }
}

fun AddCircleScreen(
    navController: NavController,
    navGraphBuilder: NavGraphBuilder,
    subscriptionViewModel: SubscriptionViewModel
) {
    navGraphBuilder.composable(
        route = NavRoute.Circle.path
    ) {
        CircleScreen(
            viewModel = hiltViewModel(),
            subscriptionVM = subscriptionViewModel,
            navigateToPaywall = {
                navController.navigate(NavRoute.Paywall.path)
            },
            back = {
                navController.popBackStack()
            }
        )
    }
}

fun AddLocationSharingScreen(
    navController: NavController,
    navGraphBuilder: NavGraphBuilder,
    location: Pair<Double, Double>?,
    subscriptionVM: SubscriptionViewModel,
    locationServicesReady: Boolean
) {
    navGraphBuilder.composable(
        route = NavRoute.LocationSharing.path
    ) {
        LocationSharingScreen(
            viewModel = hiltViewModel(),
            navigateToPaywall = {
                navController.navigate(NavRoute.Paywall.path)
            },
            navigateToSettings = {
                navController.navigate(NavRoute.Settings.path)
            },
            subscriptionVM = subscriptionVM,
            location = location,
            locationServicesReady = locationServicesReady
        )
    }
}

fun AddObserveScreen(
    navController: NavController,
    navGraphBuilder: NavGraphBuilder,
    location: Pair<Double, Double>?,
) {
    navGraphBuilder.composable(
        route = NavRoute.ObserveUsers.path
    ) { backStackEntry ->

        val parentEntry = remember(backStackEntry) {
            navController.getBackStackEntry("home_graph")
        }

        val sharedVM: HomeScreenVM = hiltViewModel(parentEntry)


        ObserveScreen(
            viewModel = sharedVM,
            userLocation = location,
            back = {
                navController.popBackStack()
            }
        )
    }
}

fun AddTimelineScreen(
    navController: NavController,
    navGraphBuilder: NavGraphBuilder,
    subscriptionVM: SubscriptionViewModel
) {
    navGraphBuilder.composable(
        route = NavRoute.TimeLine.path.plus("/{userId}/{name}"),
        arguments = listOf(
            navArgument(
                "userId"
            ) {
                type = NavType.StringType
            },
            navArgument(
                "name"
            ) {
                type = NavType.StringType
            }
        )
    ) {
        val userId = it.arguments?.getString("userId") ?: ""
        val name = it.arguments?.getString("name") ?: ""

        TimelineScreen(
            timelineViewModel = hiltViewModel(),
            subscriptionViewModel = subscriptionVM,
            back = {
                navController.popBackStack()
            },
            navigateToPaywall = {
                navController.navigate(NavRoute.Paywall.path)
            },
            navigateToPreview = { sessionId, name, userId ->
                navController.navigate(NavRoute.TimelinePreview.createRoute(sessionId, userId))
            },
            userId = userId,
            name = name
        )
    }
}

fun AddTimelinePreviewScreen(
    navController: NavController,
    navGraphBuilder: NavGraphBuilder,
    subscriptionViewModel: SubscriptionViewModel
) {
    navGraphBuilder.composable(
        route = NavRoute.TimelinePreview.path.plus("/{sessionId}/{userId}"),
        arguments = listOf(
            navArgument(
                "sessionId"
            ) {
                type = NavType.StringType
            },
            navArgument(
                "userId"
            ) {
                type = NavType.StringType
            }
        )
    ) {
        val sessionId = it.arguments?.getString("sessionId") ?: ""
        val userId = it.arguments?.getString("userId") ?: ""

        TimelineMapPreview(
            subscriptionViewModel = subscriptionViewModel,
            viewModel = hiltViewModel(),
            back = {
                navController.popBackStack()
            },
            navigateToPaywall = {
                navController.navigate(NavRoute.Paywall.path)
            },
            sessionId,
            userId
        )
    }
}

fun AddPaywallScreen(
    navController: NavController,
    navGraphBuilder: NavGraphBuilder,
    subscriptionVM: SubscriptionViewModel
) {
    navGraphBuilder.composable(
        route = NavRoute.Paywall.path,
    ) {
        PaywallScreen(
            subscriptionVM,
            back = {
                navController.popBackStack()
            }
        )
    }
}

fun AddProfileScreen(
    isDarkThemeEnabled: Boolean,
    subscriptionViewModel: SubscriptionViewModel,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    navController: NavController,
    navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.composable(
        route = NavRoute.Settings.path
    ) {
        ProfileScreen(
            isDarkThemeEnabled = isDarkThemeEnabled,
            profileVM = hiltViewModel(),
            subscriptionViewModel = subscriptionViewModel,
            themeMode = themeMode,
            onThemeChange = onThemeChange,
            hasLocationPermission = true,
            notificationsEnabled = true,
            navigateToHome = {
                navController.navigateUp()
            },
            onLogout = {
                navController.navigate(NavRoute.Login.path) {
                    popUpTo(0)
                }
            },
//            onLogout = {
//                navController.navigate(NavRoute.Login.path) {
//                    popUpTo(navController.graph.findStartDestination().id) {
//                        inclusive = true
//                    }
//                }
//            },
            onDeleteAccount = {}
        )
    }
}
