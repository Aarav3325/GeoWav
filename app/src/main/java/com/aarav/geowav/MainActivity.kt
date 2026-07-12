@file:SuppressLint("InlinedApi")

package com.aarav.geowav

import android.annotation.SuppressLint

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aarav.geowav.core.permissions.GeoPermissionCoordinator
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.platform.GeofenceBroadcastReceiver
import com.aarav.geowav.platform.GeofenceForegroundService
import com.aarav.geowav.platform.LiveLocationService
import com.aarav.geowav.platform.LocationManager
import com.aarav.geowav.platform.NotificationService
import com.aarav.geowav.presentation.MainVM
import com.aarav.geowav.presentation.components.AppDisabled
import com.aarav.geowav.presentation.components.NotificationDisabledDialog
import com.aarav.geowav.presentation.components.SnackbarManager
import com.aarav.geowav.presentation.locationsharing.LocationSharingVM
import com.aarav.geowav.presentation.navigation.BottomNavigationBar
import com.aarav.geowav.presentation.navigation.CustomBottomNavigationBar
import com.aarav.geowav.presentation.navigation.NavGraph
import com.aarav.geowav.presentation.navigation.NavRoute
import com.aarav.geowav.presentation.profile.ThemeMode
import com.aarav.geowav.presentation.profile.openAppSettings
import com.aarav.geowav.presentation.subscription.SubscriptionViewModel
import com.aarav.geowav.presentation.theme.GeoWavTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

    @Inject
    lateinit var fusedClient: FusedLocationProviderClient

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var locationManager: LocationManager

    @Inject
    lateinit var permissionCoordinator: GeoPermissionCoordinator

    @Inject
    lateinit var geofencingClient: GeofencingClient

    private var notificationIntent by mutableStateOf<String?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        notificationIntent = intent.getStringExtra("type")

        Log.i("NOTI", "intent: $notificationIntent")
    }


    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        fusedClient = LocationServices.getFusedLocationProviderClient(this)


        setContent {

            var showDialog by remember {
                mutableStateOf(false)
            }

            val context = LocalContext.current


            LaunchedEffect(Unit) {
                val check =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    )

                Log.i("MYTAG", "notification permissions: $check")
            }

            val userIdFlow = remember {
                googleSignInClient.getUserIdFlow()
            }
            val userId by userIdFlow.collectAsState(initial = googleSignInClient.getUserId())
            val isLoggedIn = userId.isNotBlank()

            LaunchedEffect(userId) {
                Log.i("SERVICE", "logged in : $isLoggedIn")
                if (!isLoggedIn) {
                    stopAllCriticalServices()
                    showDialog = false
                }
            }

            NotificationServiceInitializer(
                isLoggedIn = isLoggedIn,
                showSettingsDialog = {
                    showDialog = true
                }
            ) {
                showDialog = false
            }

            if (showDialog) {
                NotificationDisabledDialog(
                    onConfirmClick = {
                        openAppSettings(
                            context,
                            Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        )
                    },
                    onDismiss = {
                        showDialog = false
                    }
                )
            }

            var location by remember {
                mutableStateOf<Location?>(null)
            }

            val permissionUiState by permissionCoordinator.state.collectAsState()

            LaunchedEffect(userId, permissionUiState.foregroundLocationGranted) {
                if (isLoggedIn && permissionUiState.foregroundLocationGranted) {
                    locationManager.getLocationUpdates().distinctUntilChanged().collectLatest {
                        location = it
                    }
                } else {
                    location = null
                }
            }


            val mainVM: MainVM = hiltViewModel()
            val themeMode by mainVM.themeMode.collectAsState()
            val currentUser by mainVM.currentUser.collectAsState()

            Log.i("MYTAG", "theme $themeMode")

            LaunchedEffect(currentUser) {
                currentUser?.let {
                    mainVM.showMessage()
                }
            }


            LaunchedEffect(userId) {
                if (isLoggedIn) {
                    mainVM.initializeUserSession()
                } else {
                    mainVM.clearCurrentUser()
                    mainVM.stopPlaceSync()
                }
            }




            val isDarkTheme = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            val subscriptionVM: SubscriptionViewModel = hiltViewModel()

            val plan by subscriptionVM.userPlan.collectAsState()


            Log.i("SUBSCRIPTION", "plan $plan")


            val view = LocalView.current

//            SideEffect {
//                val window = (view.context as Activity).window
//
//                WindowCompat.getInsetsController(window, view).apply {
//                    isAppearanceLightStatusBars = if(isDarkTheme) true else false
//                    isAppearanceLightNavigationBars = if(isDarkTheme) false else true
//                }
//            }



            Crossfade(
                targetState = isDarkTheme, animationSpec = tween(800), label = "ThemeFade"
            ) { isDark ->
                GeoWavTheme(
                    darkTheme = isDark
                ) {


//                    val controller = WindowInsetsControllerCompat(
//                        window,
//                        View(applicationContext)
//                    )
//                    controller.isAppearanceLightStatusBars = !isDark


                    val fineLocationPermission =
                        rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
                    val backgroundLocationPermission =
                        rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)


                    val permissionState = permissionCoordinator.refresh()
                    val permissionsGranted =
                        fineLocationPermission.status.isGranted &&
                            backgroundLocationPermission.status.isGranted &&
                            permissionState.locationServicesReady

                    Log.i("MYTAG", "permissions $permissionsGranted")


                    LaunchedEffect(permissionsGranted && isLoggedIn) {

                        if (permissionsGranted && isLoggedIn) {

                            val intent = Intent(context, GeofenceForegroundService::class.java)

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(intent)
                            } else {
                                context.startService(intent)
                            }
                        }

                    }

                    val navController = rememberNavController()

                    LaunchedEffect(notificationIntent) {
                        notificationIntent?.let {
                            Log.i("NOTI", "routing")

                            navController.navigate(it)

                            notificationIntent = null
                        }
                    }

                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    val showBottomRoutes = listOf(
                        NavRoute.HomeScreen.path,
                        NavRoute.ActivityScreen.path,
                        NavRoute.YourPlaces.path,
                        NavRoute.LocationSharing.path
                    )


                    val isBottomBarVisible = currentRoute in showBottomRoutes


                    val snackbarHostState = remember {
                        SnackbarHostState()
                    }


                    SideEffect {
                        WindowCompat.getInsetsController(window, view).apply {

                            isAppearanceLightStatusBars = if(currentRoute == NavRoute.HomeScreen.path) true else !isDark
                            isAppearanceLightNavigationBars = !isDark
                        }
                    }

                    LaunchedEffect(snackbarHostState) {
                        SnackbarManager.bind(snackbarHostState)
                    }


                    Scaffold(
                        snackbarHost = {
                            SnackbarHost(snackbarHostState)
                        },
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        containerColor = Color.Transparent,
//                        bottomBar = {
//
//                        }
                    ) {
                        val location1 =
                            location?.let { it.latitude to it.longitude }


                        Box(
                            modifier = Modifier.fillMaxSize()
                                .padding(it)
                        ) {
                            NavGraph(
                                isDarkThemeEnabled = isDark,
                                themeMode = themeMode,
                                onThemeChange = { newMode ->
                                    mainVM.setThemeMode(newMode)
                                },
                                navHostController = navController,
                                subscriptionVM = subscriptionVM,
                                sharedPreferences = sharedPreferences,
                                location = location1,
                                permissionState = permissionUiState,
                                googleSignInClient = googleSignInClient,
                                modifier = Modifier
                            )

                            AnimatedVisibility(isBottomBarVisible, Modifier.align(Alignment.BottomCenter)) {
                                CustomBottomNavigationBar(navController, Modifier)
                            }
                        }
                    }
                }
            }


        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun NotificationServiceInitializer(
        isLoggedIn: Boolean,
        showSettingsDialog: () -> Unit,
        dismissDialog: () -> Unit
    ) {

        val context = LocalContext.current

        // Get lifecycle of activity
        val lifecycleOwner = LocalLifecycleOwner.current

        val notificationPermission = rememberPermissionState(
            Manifest.permission.POST_NOTIFICATIONS
        )

        fun startServiceIfNeeded() {
            val intent = Intent(context, NotificationService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }

            Log.i("SERVICE", "NOTIFICATION SERVICE STARTED")
        }

        val runtimeGranted = notificationPermission.status.isGranted
        val enabled = NotificationManagerCompat
            .from(context)
            .areNotificationsEnabled()


        // Initial check
        LaunchedEffect(isLoggedIn, runtimeGranted, enabled) {

            if (!isLoggedIn) return@LaunchedEffect

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                when {

                    // Both runtime permission AND toggle enabled
                    runtimeGranted && enabled -> {
                        startServiceIfNeeded()
                    }

                    // Runtime denied: onboarding/profile explain this before the system prompt.
                    !runtimeGranted -> Unit

                    // Runtime granted but toggle disabled - user turned off in settings
                    runtimeGranted && !enabled -> {
                        showSettingsDialog()
                    }
                }

            } else {

                if (NotificationManagerCompat
                        .from(context)
                        .areNotificationsEnabled()
                ) {
                    startServiceIfNeeded()
                } else {
                    showSettingsDialog()
                }
            }
        }

        // Re-check when returning from settings (Android 12 toggle case)
        DisposableEffect(lifecycleOwner) {

            val observer = LifecycleEventObserver { _, event ->

                if (event == Lifecycle.Event.ON_RESUME) {


                    if (!isLoggedIn) return@LifecycleEventObserver

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {

                        if (!NotificationManagerCompat
                                .from(context)
                                .areNotificationsEnabled()
                        ) {
                            showSettingsDialog()
                        } else {
                            dismissDialog()
                            startServiceIfNeeded()
                        }

                    } else {
                        // Android 13+
                        if (
                            notificationPermission.status.isGranted &&
                            NotificationManagerCompat.from(context).areNotificationsEnabled()
                        ) {
                            dismissDialog()
                            startServiceIfNeeded()
                        } else if (notificationPermission.status.isGranted) {
                            showSettingsDialog()
                        }
                    }
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }

    private fun stopAllCriticalServices() {

        stopService(Intent(this, LiveLocationService::class.java))
        Log.d("SERVICE", "Live Location services stopped - 1/3")
        stopService(Intent(this, GeofenceForegroundService::class.java))
        Log.d("SERVICE", "Geofence services stopped - 2/3")
        stopService(Intent(this, NotificationService::class.java))
        Log.d("SERVICE", "Notification service stopped - 3/3")

        Log.d("SERVICE", "All services stopped")

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(this, GeofenceBroadcastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        geofencingClient.removeGeofences(pendingIntent)
        Log.d("SERVICE", "Geofence pending intent removed")
    }

}


//fun startNotificationService(context: Context) {
//    val intent = Intent(context, NotificationService::class.java)
//
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        context.startForegroundService(intent)
//    } else {
//        context.startService(intent)
//    }
//}          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
//        )
//
//        geofencingClient.removeGeofences(pendingIntent)
//    }

//}


//fun startNotificationService(context: Context) {
//    val intent = Intent(context, NotificationService::class.java)
//
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        context.startForegroundService(intent)
//    } else {
//        context.startService(intent)
//    }
//}
