package com.cityfix.presentation.screens.splash

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "splash_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(600),
        label = "splash_alpha"
    )

    LaunchedEffect(Unit) {
        isVisible = true
        delay(2000)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.scale(scale)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationCity,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(64.dp)
                )
            }

            Text(
                text = "CityFix",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Text(
                text = "Report. Track. Resolve.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }

        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 2.dp
        )
    }
}
