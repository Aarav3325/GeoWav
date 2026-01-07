import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aarav.geowav.domain.authentication.GoogleSignInClient
import com.aarav.geowav.presentation.GeoWavHomeScreen
import com.aarav.geowav.presentation.activity.ActivityScreen
import com.aarav.geowav.presentation.auth.LoginScreen
import com.aarav.geowav.presentation.auth.SignupScreen
import com.aarav.geowav.presentation.map.MapScreen
import com.aarav.geowav.presentation.map.MapViewModel
import com.aarav.geowav.presentation.navigation.NavRoute
import com.aarav.geowav.presentation.onboard.OnboardingScreen
import com.aarav.geowav.presentation.place.AddPlaceScreen
import com.aarav.geowav.presentation.place.PlaceViewModel
import com.aarav.geowav.presentation.place.YourPlacesScreen

@Composable
fun NavGraph(
    navHostController: NavHostController,
    sharedPreferences: SharedPreferences,
    location: Pair<Double, Double>?,
    googleSignInClient: GoogleSignInClient,
    mapViewModel: MapViewModel,
    placesViewModel: PlaceViewModel,
    modifier: Modifier
) {
    val isLoggedIn = googleSignInClient.isLoggedIn()
    val isOnboarded = sharedPreferences.getBoolean("isOnboarded", false)

    NavHost(
        modifier = modifier,
        navController = navHostController,
        startDestination = if (isLoggedIn && isOnboarded) NavRoute.HomeScreen.path else if (!isOnboarded) NavRoute.OnBoard.path
        else if (!isLoggedIn && isOnboarded) NavRoute.Login.path else NavRoute.SignUp.path
    ) {
        AddMapsScreen(
            navHostController,
            this,
            location,
            mapViewModel,
            placesViewModel
        )

        AddNewPlaceScreen(
            navHostController,
            this
        )

        AddYourPlacesScreen(
            navHostController,
            this,
            placesViewModel
        )

        AddSignUpScreen(
            navHostController,
            this,
            googleSignInClient
        )

        AddLoginScreen(
            navHostController,
            this,
            googleSignInClient
        )

        AddOnBoard(
            navHostController,
            this,
            sharedPreferences
        )

        AddHomeScreen(
            navHostController,
            this,
            googleSignInClient
        )

        AddActivityScreen(
            navHostController,
            this
        )
    }

}

fun AddMapsScreen(
    navController: NavController,
    navGraphBuilder: NavGraphBuilder,
    location: Pair<Double, Double>?,
    mapViewModel: MapViewModel, placesViewModel: PlaceViewModel
) {
    navGraphBuilder.composable(
        route = NavRoute.MapScreen.path
    ) {
        MapScreen(
            mapViewModel,
            location,
            placesViewModel,
            navigateToAddPlace = { id ->
                navController.navigate(NavRoute.AddPlace.createRoute(id)) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            navigateToHome = {
                navController.navigateUp()
            }
        )
    }
}

fun AddNewPlaceScreen(
    navController: NavController,
    navGraphBuilder: NavGraphBuilder,
) {
    navGraphBuilder.composable(
        route = NavRoute.AddPlace.path.plus("/{placeId}"),
        arguments = listOf(
            navArgument("placeId") {
                type = NavType.StringType
            }
        )
    ) {
        val placeId = it.arguments?.get("placeId").toString()

        AddPlaceScreen(
            placeId,
            navigateToMaps = {
                navController.navigateUp()
            },
            navigateToYourPlaces = {
                navController.navigate(NavRoute.YourPlaces.path) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            placeViewModel = hiltViewModel()
        )

    }
}

fun AddYourPlacesScreen(
    navController: NavController, navGraphBuilder: NavGraphBuilder, placesViewModel: PlaceViewModel
) {
    navGraphBuilder.composable(
        route = NavRoute.YourPlaces.path
    ) {
        YourPlacesScreen(
            placesViewModel,
            navigateToMap = {
                navController.navigate(NavRoute.MapScreen.path) {
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

fun AddSignUpScreen(
    navController: NavController,
    navGraphBuilder: NavGraphBuilder,
    googleSignInClient: GoogleSignInClient
) {
    navGraphBuilder.composable(
        route = NavRoute.SignUp.path
    ) {
        SignupScreen(
            signUpVM = hiltViewModel(),
            navigateToHome = {
                navController.navigate(NavRoute.HomeScreen.path)
            },
            navigateToLogin = {
                navController.navigate(NavRoute.Login.path)
            }
        )
    }
}

fun AddLoginScreen(
    navController: NavController,
    navGraphBuilder: NavGraphBuilder,
    googleSignInClient: GoogleSignInClient
) {
    navGraphBuilder.composable(
        route = NavRoute.Login.path
    ) {
        LoginScreen(
            loginVM = hiltViewModel(),
            navigateToMap = {
                navController.navigate(NavRoute.HomeScreen.path)
            },
            navigateToSignUp = {
                navController.navigate(NavRoute.SignUp.path)
            }
        )
    }
}

fun AddOnBoard(
    navController: NavController, navGraphBuilder: NavGraphBuilder,
    sharedPreferences: SharedPreferences
) {
    navGraphBuilder.composable(
        route = NavRoute.OnBoard.path
    ) {
        OnboardingScreen(
            navigateToAuth = {
                navController.navigate(NavRoute.SignUp.path)
            },
            sharedPreferences,
            onBoardVM = hiltViewModel()
        )
    }
}

fun AddHomeScreen(
    navController: NavController, navGraphBuilder: NavGraphBuilder,
    googleSignInClient: GoogleSignInClient
) {
    navGraphBuilder.composable(
        route = NavRoute.HomeScreen.path
    ) {

        GeoWavHomeScreen(
            navigateToAuth = {
                navController.navigate(NavRoute.Login.path) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            googleSignInClient,
            onAddZone = {
                navController.navigate(NavRoute.MapScreen.path)
            },
            onShareLocation = {},
            onOpenAlerts = {},
            homeScreenVM = hiltViewModel(),
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
    navController: NavController, navGraphBuilder: NavGraphBuilder
) {
    navGraphBuilder.composable(
        route = NavRoute.ActivityScreen.path
    ) {
        ActivityScreen(
            activityViewModel = hiltViewModel()
        )
    }
}

