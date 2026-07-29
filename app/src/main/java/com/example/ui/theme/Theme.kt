package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = Color(0xFF90CAF9),
  onPrimary = Color(0xFF003258),
  primaryContainer = RoyalBluePrimary,
  onPrimaryContainer = Color(0xFFD1E4FF),
  secondary = Color(0xFFFFB74D),
  onSecondary = Color(0xFF452B00),
  tertiary = SecondaryTeal,
  background = DarkBackground,
  surface = DarkSurface,
  surfaceVariant = DarkSurfaceVariant,
  onBackground = Color(0xFFE2E8F0),
  onSurface = Color(0xFFF1F5F9)
)

private val LightColorScheme = lightColorScheme(
  primary = RoyalBluePrimary,
  onPrimary = Color.White,
  primaryContainer = BlueContainer,
  onPrimaryContainer = OnBlueContainer,
  secondary = RoyalBlueLight,
  onSecondary = Color.White,
  tertiary = AccentGold,
  background = LightBackground,
  surface = LightSurface,
  surfaceVariant = Color(0xFFF1F5F9),
  onBackground = Color(0xFF0F172A),
  onSurface = Color(0xFF1E293B)
)

@Composable
fun WaqarCoachingTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  WaqarCoachingTheme(darkTheme = darkTheme, content = content)
}

