package com.epsilon.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = DarkBlue10,
    primaryContainer = DarkBlue30,
    onPrimaryContainer = Blue90,
    
    secondary = Cyan70,
    onSecondary = DarkBlue20,
    secondaryContainer = DarkBlue40,
    onSecondaryContainer = Cyan90,
    
    tertiary = Teal70,
    onTertiary = DarkBlue20,
    tertiaryContainer = DarkBlue40,
    onTertiaryContainer = Teal90,
    
    error = Error,
    onError = Color.White,
    errorContainer = ErrorContainer,
    onErrorContainer = Color(0xFF8B0000),
    
    background = DarkBlue10,
    onBackground = Gray95,
    
    surface = DarkBlue20,
    onSurface = Gray95,
    surfaceVariant = DarkBlue30,
    onSurfaceVariant = Gray80,
    
    outline = Gray60,
    outlineVariant = Gray40,
    
    inverseSurface = Gray90,
    inverseOnSurface = DarkBlue20,
    inversePrimary = Blue40,
    
    surfaceTint = Blue80,
)

private val LightColorScheme = lightColorScheme(
    primary = Blue50,
    onPrimary = Color.White,
    primaryContainer = Blue90,
    onPrimaryContainer = Blue10,
    
    secondary = Cyan50,
    onSecondary = Color.White,
    secondaryContainer = Cyan90,
    onSecondaryContainer = Blue20,
    
    tertiary = Teal50,
    onTertiary = Color.White,
    tertiaryContainer = Teal90,
    onTertiaryContainer = Blue20,
    
    error = Error,
    onError = Color.White,
    errorContainer = ErrorContainer,
    onErrorContainer = Color(0xFF8B0000),
    
    background = Gray99,
    onBackground = Gray10,
    
    surface = Color.White,
    onSurface = Gray10,
    surfaceVariant = Blue95,
    onSurfaceVariant = Gray40,
    
    outline = Gray50,
    outlineVariant = Gray80,
    
    inverseSurface = Gray20,
    inverseOnSurface = Gray95,
    inversePrimary = Blue80,
    
    surfaceTint = Blue50,
)

@Composable
fun EpsilonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disabled to use our blue theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}