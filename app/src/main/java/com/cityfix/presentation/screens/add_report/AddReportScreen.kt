package com.cityfix.presentation.screens.add_report

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.cityfix.domain.model.ReportCategory
import com.cityfix.presentation.components.categoryColor
import com.cityfix.presentation.components.categoryIcon
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReportScreen(
    onNavigateUp: () -> Unit,
    viewModel: AddReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) onNavigateUp()
    }

    var showImageSourceSheet by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onEvent(AddReportEvent.ImageSelected(uri))
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.onEvent(AddReportEvent.ImageSelected(pendingCameraUri))
        } else {
            pendingCameraUri = null
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createImageCaptureUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            scope.launch { fetchLocation(context, viewModel) }
        } else {
            viewModel.onEvent(
                AddReportEvent.LocationFetchFailed("Location permission denied")
            )
        }
    }

    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(AddReportEvent.DismissError) },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(AddReportEvent.DismissError) }) {
                    Text("OK")
                }
            }
        )
    }

    if (showImageSourceSheet) {
        ImageSourceBottomSheet(
            onDismiss = { showImageSourceSheet = false },
            onPickGallery = {
                showImageSourceSheet = false
                galleryLauncher.launch("image/*")
            },
            onPickCamera = {
                showImageSourceSheet = false
                if (hasCameraPermission(context)) {
                    val uri = createImageCaptureUri(context)
                    pendingCameraUri = uri
                    cameraLauncher.launch(uri)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report an Issue") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ImagePickerSection(
                    imageUri = uiState.imageUri,
                    onPickImage = { showImageSourceSheet = true },
                    onRemoveImage = { viewModel.onEvent(AddReportEvent.ImageSelected(null)) }
                )

                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.onEvent(AddReportEvent.TitleChanged(it)) },
                    label = { Text("Title") },
                    placeholder = { Text("Brief description of the issue") },
                    isError = uiState.titleError != null,
                    supportingText = uiState.titleError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Title, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.onEvent(AddReportEvent.DescriptionChanged(it)) },
                    label = { Text("Description") },
                    placeholder = { Text("Provide detailed information about the issue") },
                    isError = uiState.descriptionError != null,
                    supportingText = uiState.descriptionError?.let { { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    maxLines = 5,
                    leadingIcon = { Icon(Icons.Filled.Description, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )

                CategorySection(
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = { viewModel.onEvent(AddReportEvent.CategorySelected(it)) }
                )

                LocationSection(
                    latitude = uiState.latitude,
                    longitude = uiState.longitude,
                    hasLocation = uiState.hasLocation,
                    isFetching = uiState.isFetchingLocation,
                    error = uiState.locationError,
                    onRequestLocation = {
                        if (hasLocationPermission(context)) {
                            scope.launch { fetchLocation(context, viewModel) }
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.onEvent(AddReportEvent.Submit) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isSubmitting,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Submit Report",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            if (uiState.isSubmitting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(strokeWidth = 3.dp)
                            Text("Submitting report…", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageSourceBottomSheet(
    onDismiss: () -> Unit,
    onPickGallery: () -> Unit,
    onPickCamera: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Add a Photo",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 8.dp)
            )
            ListItem(
                headlineContent = { Text("Take a photo") },
                supportingContent = { Text("Use the camera") },
                leadingContent = {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                },
                modifier = Modifier.clickable(onClick = onPickCamera)
            )
            ListItem(
                headlineContent = { Text("Choose from gallery") },
                supportingContent = { Text("Pick an existing image") },
                leadingContent = {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                },
                modifier = Modifier.clickable(onClick = onPickGallery)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ImagePickerSection(
    imageUri: Uri?,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        border = if (imageUri == null) BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.outlineVariant
        ) else null
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Selected image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = onPickImage)
                )
                IconButton(
                    onClick = onRemoveImage,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Remove image",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = onPickImage),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add a Photo",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Camera or gallery",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CategorySection(
    selectedCategory: ReportCategory,
    onCategorySelected: (ReportCategory) -> Unit
) {
    Column {
        Text(
            text = "Category",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        val categories = ReportCategory.entries
        val rows = categories.chunked(3)
        rows.forEach { rowCategories ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowCategories.forEach { category ->
                    CategoryChip(
                        category = category,
                        isSelected = selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - rowCategories.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CategoryChip(
    category: ReportCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = categoryColor(category)
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) BorderStroke(2.dp, color) else null
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = categoryIcon(category),
                contentDescription = null,
                tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun LocationSection(
    latitude: Double,
    longitude: Double,
    hasLocation: Boolean,
    isFetching: Boolean,
    error: String?,
    onRequestLocation: () -> Unit
) {
    Column {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ),
            border = if (error != null)
                BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            else null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Location",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = when {
                            isFetching -> "Detecting your location…"
                            hasLocation -> "%.6f, %.6f".format(latitude, longitude)
                            else -> "Location not set"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = onRequestLocation,
                    enabled = !isFetching,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isFetching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Filled.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (hasLocation) "Refresh" else "Get Location")
                    }
                }
            }
        }
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

private fun hasCameraPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

private fun createImageCaptureUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File.createTempFile("report_", ".jpg", imagesDir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

@SuppressLint("MissingPermission")
private suspend fun fetchLocation(
    context: Context,
    viewModel: AddReportViewModel
) {
    viewModel.onEvent(AddReportEvent.LocationFetchStarted)
    val client = LocationServices.getFusedLocationProviderClient(context)
    val cts = CancellationTokenSource()
    try {
        val location = client.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cts.token
        ).await() ?: client.lastLocation.await()

        if (location != null) {
            viewModel.onEvent(
                AddReportEvent.LocationUpdated(location.latitude, location.longitude)
            )
        } else {
            viewModel.onEvent(
                AddReportEvent.LocationFetchFailed(
                    "Couldn't get a location fix. Make sure location is enabled."
                )
            )
        }
    } catch (e: Exception) {
        viewModel.onEvent(
            AddReportEvent.LocationFetchFailed(e.message ?: "Failed to get location")
        )
    }
}
