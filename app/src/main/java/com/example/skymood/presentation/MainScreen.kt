package com.example.skymood.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.skymood.R
import com.example.skymood.data.weather.WeatherRepository
import com.example.skymood.data.database.WeatherDatabase
import com.example.skymood.data.weather.datasource.local.WeatherLocalDataSource
import com.example.skymood.data.weather.datasource.remote.WeatherRemoteDataSource
import com.example.skymood.presentation.home.HomeScreen
import com.example.skymood.presentation.home.viewmodel.HomeViewModel
import com.example.skymood.presentation.home.viewmodel.HomeViewModelFactory
import com.example.skymood.presentation.map.MapPickerScreen
import com.example.skymood.presentation.map.MapPickerViewModel
import com.example.skymood.presentation.map.MapPickerViewModelFactory
import com.example.skymood.presentation.favorites.view.FavoritesScreen
import com.example.skymood.presentation.favorites.view.FavoriteForecastScreen
import com.example.skymood.presentation.favorites.viewmodel.FavoritesViewModel
import com.example.skymood.presentation.favorites.viewmodel.FavoritesViewModelFactory
import com.example.skymood.presentation.favorites.viewmodel.FavoriteForecastViewModel
import com.example.skymood.presentation.favorites.viewmodel.FavoriteForecastViewModelFactory
import com.example.skymood.presentation.settings.view.SettingsScreen
import com.example.skymood.presentation.settings.viewmodel.SettingsViewModel
import com.example.skymood.presentation.settings.viewmodel.SettingsViewModelFactory
import com.example.skymood.data.settings.SettingsPreferencesManager

sealed class Screen(val route: String, val title: String, val selectedIcon: Int, val unselectedIcon: Int) {
    object Home : Screen("home", "HOME", R.drawable.ic_home_blue, R.drawable.ic_home_grey)
    object Alerts : Screen("alerts", "ALERTS", R.drawable.ic_bell_blue, R.drawable.ic_bell_grey)
    object Favorites : Screen("favorites", "FAVORITES", R.drawable.ic_heart_blue, R.drawable.ic_heart_grey)
    object Settings : Screen("settings", "SETTINGS", R.drawable.ic_settings_blue, R.drawable.ic_settings_grey)
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    
    // Singletons & Managers
    val preferencesManager = remember { SettingsPreferencesManager(context) }
    
    val repository = remember { 
        val db = WeatherDatabase.getDatabase(context)
        val localDataSource = WeatherLocalDataSource(db.weatherDao())
        val remoteDataSource = WeatherRemoteDataSource()
        WeatherRepository(remoteDataSource, localDataSource) 
    }
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(repository, application, preferencesManager))
    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(preferencesManager))
    val favoritesViewModel: FavoritesViewModel = viewModel(factory = FavoritesViewModelFactory(repository, preferencesManager))
    val mapPickerViewModel: MapPickerViewModel = viewModel(factory = MapPickerViewModelFactory(repository))
    val favoriteForecastViewModel: FavoriteForecastViewModel = viewModel(factory = FavoriteForecastViewModelFactory(repository, preferencesManager))

    var mapPickerDestination by remember { mutableStateOf("home") }

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute != "map_picker" && currentRoute?.startsWith("favorite_forecast") != true) {
                BottomNavigationBar(navController = navController)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToMap = {
                        mapPickerDestination = "home"
                        navController.navigate("map_picker")
                    }
                )
            }
            composable("map_picker") {
                MapPickerScreen(
                    viewModel = mapPickerViewModel,
                    onLocationSelected = { lat, lon ->
                        if (mapPickerDestination == "favorites") {
                            favoritesViewModel.fetchAndAddFavorite(lat, lon)
                        } else {
                            homeViewModel.setLocation(lat, lon, isHomeLocation = true)
                        }
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Alerts.route) { Text("Alerts Screen") }
            composable(Screen.Favorites.route) { 
                FavoritesScreen(
                    viewModel = favoritesViewModel,
                    onNavigateToForecast = { lat, lon ->
                        navController.navigate("favorite_forecast/$lat/$lon")
                    },
                    onNavigateToAddFavorite = {
                        mapPickerDestination = "favorites"
                        navController.navigate("map_picker")
                    }
                )
            }
            composable(
                route = "favorite_forecast/{lat}/{lon}",
                arguments = listOf(
                    navArgument("lat") { type = NavType.FloatType },
                    navArgument("lon") { type = NavType.FloatType }
                )
            ) { backStackEntry ->
                val lat = backStackEntry.arguments?.getFloat("lat")?.toDouble() ?: 0.0
                val lon = backStackEntry.arguments?.getFloat("lon")?.toDouble() ?: 0.0
                FavoriteForecastScreen(
                    lat = lat,
                    lon = lon,
                    viewModel = favoriteForecastViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Settings.route) { 
                SettingsScreen(viewModel = settingsViewModel) 
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(Screen.Home, Screen.Alerts, Screen.Favorites, Screen.Settings)

    NavigationBar(
        containerColor = Color(0xFF0C1623) 
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
                icon = { 
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = if (isSelected) screen.selectedIcon else screen.unselectedIcon), 
                        contentDescription = screen.title,
                        tint = Color.Unspecified
                    ) 
                },
                label = { Text(text = screen.title, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedTextColor = Color(0xFF4FC3F7),
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
