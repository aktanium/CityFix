package com.cityfix.presentation.screens.add_report

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.cityfix.domain.model.ReportCategory
import com.cityfix.presentation.components.CategoryIcon
import com.cityfix.presentation.components.categoryColor
import com.cityfix.presentation.components.categoryIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReportScreen(
    onNavigateUp: () -> Unit,
    viewModel: AddReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) onNavigateUp()
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onEvent(AddReportEvent.ImageSelected(uri))
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.onEvent(AddReportEvent.LocationUpdated(41.2995, 69.2401))
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report an Issue") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ImagePickerSection(
                imageUri = uiState.imageUri,
                onPickImage = { imagePickerLauncher.launch("image/*") },
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
                onRequestLocation = {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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
                    Icon(Icons.Filled.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Submit Report",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
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
                    modifier = Modifier.fillMaxSize()
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
                        text = "Tap to upload from gallery",
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
    onRequestLocation: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
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
                    text = if (latitude == 0.0 && longitude == 0.0)
                        "Location not set"
                    else "%.6f, %.6f".format(latitude, longitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(
                onClick = onRequestLocation,
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Get Location")
            }
        }
    }
}
