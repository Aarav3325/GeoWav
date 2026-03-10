package com.aarav.geowav

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aarav.geowav.core.managers.KillSwitchManager
import com.aarav.geowav.data.authentication.GoogleSignInClient
import com.aarav.geowav.platform.GeofenceBroadcastReceiver
import com.aarav.geowav.platform.GeofenceForegroundService
import com.aarav.geowav.platform.LiveLocationService
import com.aarav.geowav.platform.LocationManager
import com.aarav.geowav.platform.NotificationService
import com.aarav.geowav.presentation.MainVM
import com.aarav.geowav.presentation.components.AppDisabled
import com.aarav.geowav.presentation.components.LocationPermissionDialog
import com.aarav.geowav.presentation.components.NotificationDisabledDialog
import com.aarav.geowav.presentation.components.SnackbarManager
import com.aarav.geowav.presentation.navigation.BottomNavigationBar
import com.aarav.geowav.presentation.navigation.NavGraph
import com.aarav.geowav.presentation.navigation.NavRoute
import com.aarav.geowav.presentation.settings.ThemeMode
import com.aarav.geowav.presentation.settings.openAppSettings
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
    lateinit var killSwitchManager: KillSwitchManager

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

    @Inject
    lateinit var fusedClient: FusedLocationProviderClient

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var locationManager: LocationManager

    @Inject
    lateinit var geofencingClient: GeofencingClient

    private var isAppEnabled = false

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
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(View(applicationContext)) { v, insets ->
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }


        ViewCompat.setOnApplyWindowInsetsListener(View(applicationContext)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val bottomInset = maxOf(systemBars.bottom, ime.bottom)
            v.setPadding(systemBars.left, 0, systemBars.right, bottomInset)
            insets
        }


        fusedClient = LocationServices.getFusedLocationProviderClient(this)

//        lifecycleScope.launch {
//            killSwitchManager.fetchAndActivate()
//
//            killSwitchManager.observeAppEnabled()
//                .collect { enabled ->
//
//                    Log.i("KILL", "app in kill mode: $enabled")
//
//                    if (!enabled) {
//                        stopAllCriticalServices()
//                        showAppDisabledState = true
//
////                        startActivity(
////                            Intent(this@MainActivity, AppDisabledActivity::class.java)
////                        )
////                        finish()
//                    }
//                }
//        }


//        lifecycleScope.launch {
//            killSwitchManager.fetchAndActivate()
//
//            if(!killSwitchManager.isAppEnabled()) {
//
//                startActivity(Intent(this@MainActivity, AppDisabledActivity::class.java))
//                finish()
//                Log.e("KILL", "App is in kill mode")
//            }
//
//        }

        setContent {

            var showAppDisabledState by remember {
                mutableStateOf(false)
            }

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

//            val check = ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.POST_NOTIFICATIONS
//            ) == PackageManager.PERMISSION_GRANTED
//
//            val notificationPermission =
//                rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
//

            NotificationServiceInitializer(
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


//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//
//
//                if (!check) {
//                    showNotificationPermissionDialog()
//                }
//            } else {
//                var notificationPermission =
//                    rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
//            }

            LaunchedEffect(Unit) {
                if (googleSignInClient.isLoggedIn()) {
                    killSwitchManager.fetchAndActivate()
                    killSwitchManager.observeAppEnabled()
                        .collect { enabled ->
                            if (!enabled) {

                                Log.i("KILL", "app in kill mode")
                                stopAllCriticalServices()
                                showAppDisabledState = true
                            } else {
                                showAppDisabledState = false
                                Log.i("KILL", "app not in kill mode")
                            }
                        }
                }
            }

            if (showAppDisabledState) {
                AppDisabled()
                return@setContent
            }

            var location by remember {
                mutableStateOf<Location?>(null)
            }

            LaunchedEffect(Unit) {
                locationManager.getLocationUpdates().distinctUntilChanged().collectLatest {
                    location = it

                    Log.i("LOCATION", "lat: ${location?.latitude}, long: ${location?.longitude}")

                }


//                if (location == null) {
//                    Log.i("LOCATION", "null")
//
//                } else {
//                    Log.i("LOCATION", "lat: ${location?.latitude}, long: ${location?.longitude}")
//                }
            }

            val mainVM: MainVM = hiltViewModel()
            val themeMode by mainVM.themeMode.collectAsState()

            Log.i("MYTAG", "theme $themeMode")


            val isDarkTheme = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }


            Crossfade(
                targetState = isDarkTheme, animationSpec = tween(800), label = "ThemeFade"
            ) { isDark ->
                GeoWavTheme(
                    darkTheme = isDark
                ) {

                    val controller = WindowInsetsControllerCompat(
                        window,
                        View(applicationContext)
                    )
                    controller.isAppearanceLightStatusBars = !isDark


                    val isOnboarded = sharedPreferences.getBoolean("isOnboarded", false)

                    val fineLocationPermission =
                        rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
                    val backgroundLocationPermission =
                        rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)


                    val permissionsGranted =
                        fineLocationPermission.status.isGranted && backgroundLocationPermission.status.isGranted

                    Log.i("MYTAG", "permissions $permissionsGranted")


//                    if (permissionsGranted && isAppEnabled) {
//                        val intent = Intent(context, GeofenceForegroundService::class.java)
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            context.startForegroundService(intent)
//                        } else {
//                            context.startService(intent)
//                        }
//                    } else {
//                        if (isOnboarded) {
//                            LocationPermissionDialog(
//                                true,
//                                onConfirmClick = {
//                                    openAppSettings(
//                                        context,
//                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS
//                                    )
//                                }
//                            )
//                        }
//                    }

//                    LaunchedEffect(Unit) {
//                        GeoNotificationHelper.show(
//                            context,
//                            "circle_channel",
//                            "New Invite",
//                            "Aarav invited you"
//                        )
//                    }

                    LaunchedEffect(permissionsGranted) {

                        val enabled = killSwitchManager.isAppEnabled()

                        if (permissionsGranted && enabled) {

                            val intent = Intent(context, GeofenceForegroundService::class.java)

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(intent)
                            } else {
                                context.startService(intent)
                            }
                        }

                    }

                    LocationPermissionDialog(
                        isOnboarded && !permissionsGranted,
                        onConfirmClick = {
                            openAppSettings(
                                context,
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS
                            )
                        }
                    )


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

                    LaunchedEffect(snackbarHostState) {
                        SnackbarManager.bind(snackbarHostState)
                    }


                    Scaffold(
                        snackbarHost = {
                            //SnackbarManager.bind(snackbarHostState)
                            SnackbarHost(snackbarHostState)
                        },
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets(0),
                        containerColor = Color.Transparent,
                        bottomBar = {
                            AnimatedVisibility(isBottomBarVisible) {
                                BottomNavigationBar(navController)
                            }
                        }) {
                        val location1 =
                            location?.let { it.latitude to it.longitude } ?: (0.0 to 0.0)


                        NavGraph(
                            isDarkThemeEnabled = isDark,
                            themeMode = themeMode,
                            onThemeChange = { newMode ->
                                mainVM.setThemeMode(newMode)
                            },
                            navController,
                            sharedPreferences,
                            location1,
                            googleSignInClient,
                            modifier = Modifier.padding(it)
                        )
                    }
                }
            }


        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun NotificationServiceInitializer(
        showSettingsDialog: () -> Unit,
        dismissDialog: () -> Unit
    ) {

        val context = LocalContext.current

        // Get lifecycle of activity
        val lifecycleOwner = LocalLifecycleOwner.current

        val notificationPermission = rememberPermissionState(
            Manifest.permission.POST_NOTIFICATIONS
        )

        // Prevent duplicate service start
        var serviceStarted by remember { mutableStateOf(false) }

        fun startServiceIfNeeded() {
            if (serviceStarted) return

            val intent = Intent(context, NotificationService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }

            serviceStarted = true
        }

        // Initial check
        LaunchedEffect(Unit) {


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val runtimeGranted = notificationPermission.status.isGranted
                val enabled = NotificationManagerCompat
                    .from(context)
                    .areNotificationsEnabled()


                when {

                    // Both runtime permission AND toggle enabled
                    runtimeGranted && enabled -> {
                        startServiceIfNeeded()
                    }

                    // Runtime denied - request permission (first launch case)
                    !runtimeGranted -> {
                        notificationPermission.launchPermissionRequest()
                    }

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
                        if (notificationPermission.status.isGranted) {
                            startServiceIfNeeded()
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
        stopService(Intent(this, GeofenceForegroundService::class.java))
        stopService(Intent(this, NotificationService::class.java))

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(this, GeofenceBroadcastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        geofencingClient.removeGeofences(pendingIntent)
    }

}


