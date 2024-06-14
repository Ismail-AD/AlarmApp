package com.appdev.alarmapp.ui.MissionDemos

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.VectorDrawable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.appdev.alarmapp.R
import com.appdev.alarmapp.ui.CustomButton
import com.appdev.alarmapp.ui.MainScreen.MainViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun showMap(mainViewModel: MainViewModel, navController: NavHostController) {
    val contextFromCompose = LocalContext.current
    val fusedLocationClient: FusedLocationProviderClient by remember {
        mutableStateOf(LocationServices.getFusedLocationProviderClient(contextFromCompose))
    }
    val scope = rememberCoroutineScope()
    val permissionState =
        rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)
    var grantedOrNot by remember {
        mutableStateOf(permissionState.status.isGranted)
    }
    val singapore = LatLng(1.35, 103.87)
    var cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 15f)
    }
    var uiSettings by remember { mutableStateOf(MapUiSettings()) }
    var mapProperties by remember { mutableStateOf(MapProperties()) }
    val sheetState = rememberModalBottomSheetState()

    var showDialog by remember {
        mutableStateOf(false)
    }
    var clickedLocCoordinates by remember {
        mutableStateOf<LatLng?>(null)
    }

    var locationName by remember { mutableStateOf("Unknown Location") }
    val currentLocation by mainViewModel.currentLocation.collectAsStateWithLifecycle()
    val loaderState by mainViewModel.isFetchingLocation.collectAsStateWithLifecycle()
    val nameLoaderState by mainViewModel.isFetchingLocationName.collectAsStateWithLifecycle()
    LaunchedEffect(permissionState.status, key2 = loaderState) {
        if (permissionState.status.isGranted) {
            grantedOrNot = permissionState.status.isGranted
        }
        if (!loaderState && currentLocation != null) {
            currentLocation?.let {
                cameraPositionState.position =
                    CameraPosition.fromLatLngZoom(LatLng(it.latitude, it.longitude), 15f)
            }
        }
    }
    var showRationale by remember(permissionState) {
        mutableStateOf(false)
    }
    LaunchedEffect(key1 = Unit) {
        mainViewModel.startLocationUpdates(fusedLocationClient)
    }
    BackHandler {
        navController.popBackStack()
    }
    Box(
        modifier = Modifier
            .fillMaxSize(), contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.onBackground)
                .fillMaxHeight()
        ) {
            if (!grantedOrNot) {
                if (permissionState.status.shouldShowRationale) {
                    showRationale = true
                } else {
                    permissionState.launchPermissionRequest()
                }
            } else {
                if (loaderState) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Dialog(onDismissRequest = { /*TODO*/ }) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                } else {
                    currentLocation?.let {
                        Box(modifier = Modifier.fillMaxSize()) {

                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                uiSettings = uiSettings.copy(
                                    scrollGesturesEnabled = true,
                                    zoomControlsEnabled = true,
                                    mapToolbarEnabled = true,
                                    tiltGesturesEnabled = true
                                ), properties = mapProperties.copy(isMyLocationEnabled = true),
                                onMapClick = { latLng ->
                                    clickedLocCoordinates = latLng
                                    showDialog = true
                                    scope.launch(Dispatchers.IO) {
                                        locationName = mainViewModel.getLocationName(
                                            latLng.latitude,
                                            latLng.longitude,
                                            contextFromCompose
                                        )
                                    }
                                },
                                cameraPositionState = cameraPositionState
                            ) {
                                Marker(
                                    state = MarkerState(
                                        position = LatLng(
                                            it.latitude,
                                            it.longitude
                                        )
                                    ),
                                    title = "Current Location"
                                )
                                clickedLocCoordinates?.let { cLc ->
                                    Marker(
                                        state = MarkerState(
                                            position = LatLng(
                                                cLc.latitude,
                                                cLc.longitude
                                            )
                                        ),
                                        title = "Tapped Location", icon = bitmapDescriptorFromVector()
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .padding(vertical = 10.dp, horizontal = 15.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Card(
                                    onClick = {
                                        navController.popBackStack()
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.surfaceTint
                                    ),
                                    shape = CircleShape,
                                    colors = CardDefaults.cardColors(containerColor = Color.Black)
                                ) {
                                    Box(
                                        modifier = Modifier.size(27.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.KeyboardArrowLeft,
                                            contentDescription = "",
                                            tint = Color.White
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(
                                    modifier = Modifier
                                        .background(Color.Black, shape = RoundedCornerShape(20.dp))
                                        .padding(
                                            start = 10.dp,
                                            top = 5.dp,
                                            bottom = 5.dp,
                                            end = 10.dp
                                        )
                                ) {
                                    Text(
                                        text = "tap on map to add location",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        textAlign = TextAlign.Center, fontWeight = FontWeight.W500
                                    )
                                }
                            }
                        }

                    }
//                    AndroidView(
//                        factory = { context ->
//                            MapView(context).apply {
//                                // Initialize Google Map
//                                onCreate(null)
//                                getMapAsync { googleMap ->
//                                    googleMap.isMyLocationEnabled = true // Show current location
//                                    googleMap.uiSettings.isZoomControlsEnabled = true
//
//
//                                    // Move camera to current location
//                                    currentLocation?.let {
//                                        // Add marker for current location
//                                        googleMap.addMarker(
//                                            MarkerOptions().position(it)
//                                                .title("Current Location")
//                                        )
//
//                                        googleMap.moveCamera(
//                                            CameraUpdateFactory.newLatLngZoom(it, 15f)
//                                        )
//                                    }
//
//                                    // Set long click listener
//                                    googleMap.setOnMapLongClickListener { latLng ->
//                                        clickedLocCoordinates = latLng
//                                        showDialog = true
//                                        scope.launch(Dispatchers.IO) {
//                                            locationName = mainViewModel.getLocationName(
//                                                latLng.latitude,
//                                                latLng.longitude,
//                                                context
//                                            )
//                                        }
//                                    }
//                                }
//                            }
//                        },
//                        modifier = Modifier.fillMaxSize()
//                    )
                }
            }
            if (showDialog) {
                ModalBottomSheet(
                    onDismissRequest = { showDialog = false },
                    sheetState = sheetState
                ) {
                    if (nameLoaderState) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Color(0xFFF57C00))
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.onBackground,
                                    shape = RoundedCornerShape(5.dp)
                                )
                                .padding(horizontal = 15.dp, vertical = 10.dp)
                        ) {
                            if ((locationName == "Unknown Location" && clickedLocCoordinates != null) || locationName != "Unknown Location") {
                                Text(
                                    text = if (locationName == "Unknown Location") "Do you want to save location on following Coordinates?" else "Do you want to save following location ?",
                                    color = MaterialTheme.colorScheme.surfaceTint,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 20.dp), fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = if (locationName == "Unknown Location" && clickedLocCoordinates != null) "Latitude :${
                                    clickedLocCoordinates!!.latitude.toString().take(10)
                                } \n Longitude :${
                                    clickedLocCoordinates!!.longitude.toString().take(10)
                                }" else if (locationName != "Unknown Location") locationName else "Location not found ! \n Make sure your location is turned on and then try again",
                                color = MaterialTheme.colorScheme.surfaceTint,
                                fontSize = 17.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 20.dp, start = 8.dp, end = 8.dp),
                                fontWeight = FontWeight.Normal
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(bottom = 20.dp)
                                ) {
                                    CustomButton(
                                        onClick = {
                                            scope.launch {
                                                sheetState.hide()
                                            }
                                            showDialog = false
                                        },
                                        text = "No",
                                        width = 0.40f,
                                        backgroundColor = Color(0xff3F434F)
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    CustomButton(
                                        onClick = {
                                            scope.launch {
                                                sheetState.hide()
                                            }
                                            showDialog = false
                                            clickedLocCoordinates?.let {
                                                mainViewModel.updateLocationName(locationName,it.longitude,it.latitude)
                                            }
                                            navController.popBackStack()
                                        },
                                        text = "Yes",
                                        width = 0.75f,
                                    )
                                }
                            }
                        }
                    }
                }
//                Box(
//                    modifier = Modifier.fillMaxSize()
//                ) {
//                    Dialog(onDismissRequest = {
//                        showDialog = false
//                    }) {
//
//                            }
//                        }
//                    }
//                }

            }
        }
    }
}
@Composable
fun bitmapDescriptorFromVector(): BitmapDescriptor {
    val vectorDrawable = ContextCompat.getDrawable(
        LocalContext.current,
        R.drawable.marker // Replace with your vector drawable
    ) as VectorDrawable


    val bitmap = Bitmap.createBitmap(
        vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
    vectorDrawable.draw(canvas)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}