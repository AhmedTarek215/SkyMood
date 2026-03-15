package com.example.skymood.presentation.map.view

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.ui.zIndex
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skymood.R
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.skymood.presentation.map.viewmodel.MapPickerViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    viewModel: MapPickerViewModel,
    onLocationSelected: (lat: Double, lon: Double) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    var searchActive by remember { mutableStateOf(false) }
    var selectedLat by remember { mutableDoubleStateOf(30.0444) } 
    var selectedLon by remember { mutableDoubleStateOf(31.2357) }
    var hasSelection by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val mapViewRef = remember { mutableStateOf<MapView?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapViewRef.value?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapViewRef.value?.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapViewRef.value = null
        }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF1D3349), Color(0xFF0C1623))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(R.string.map_pick_location),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Text(
                text = if (hasSelection) "Lat: ${"%.4f".format(selectedLat)}, Lon: ${"%.4f".format(selectedLon)}"
                else stringResource(R.string.map_tap_instruction),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .zIndex(1f)
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChanged(it) },
                    onSearch = { searchActive = false },
                    active = searchActive,
                    onActiveChange = { searchActive = it },
                    placeholder = { Text(stringResource(R.string.map_search_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                    trailingIcon = {
                        if (searchActive || searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                if (searchQuery.isNotEmpty()) {
                                    viewModel.clearSearch()
                                } else {
                                    searchActive = false
                                }
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(searchResults) { result ->
                            ListItem(
                                headlineContent = { Text(result.name) },
                                supportingContent = { Text("${result.state ?: ""} ${result.country}") },
                                modifier = Modifier
                                    .clickable {
                                        selectedLat = result.lat
                                        selectedLon = result.lon
                                        hasSelection = true
                                        viewModel.clearSearch()
                                        searchActive = false
                                        mapViewRef.value?.controller?.setCenter(GeoPoint(result.lat, result.lon))
                                    }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // MAP
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .background(Color.DarkGray, RoundedCornerShape(16.dp))
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(6.0)
                            controller.setCenter(GeoPoint(30.0444, 31.2357))

                            val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                    selectedLat = p.latitude
                                    selectedLon = p.longitude
                                    hasSelection = true
                                    return true
                                }
                                override fun longPressHelper(p: GeoPoint): Boolean = false
                            })
                            overlays.add(0, eventsOverlay)
                            mapViewRef.value = this
                        }
                    },
                    update = { mapView ->
                        if (hasSelection) {
                            val markersToRemove = mapView.overlays.filterIsInstance<Marker>()
                            mapView.overlays.removeAll(markersToRemove.toSet())

                            // Add new marker
                            val marker = Marker(mapView).apply {
                                position = GeoPoint(selectedLat, selectedLon)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = "Selected Location"
                            }
                            mapView.overlays.add(marker)
                            mapView.invalidate()
                        }
                    },
                    onRelease = { mapView ->
                        mapView.onPause()
                        mapView.onDetach()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm button
            Button(
                onClick = {
                    onLocationSelected(selectedLat, selectedLon)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E88E5),
                    disabledContainerColor = Color(0xFF1E88E5).copy(alpha = 0.3f)
                ),
                enabled = hasSelection
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.map_confirm_location), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
