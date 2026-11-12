package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme =
  lightColorScheme(
    primary = ChampagneGold,
    onPrimary = Color.White,
    primaryContainer = SoftGoldContainer,
    onPrimaryContainer = DeepGoldText,
    secondary = WarmBronze,
    onSecondary = Color.White,
    secondaryContainer = BronzeContainer,
    onSecondaryContainer = EspressoInk,
    tertiary = EmeraldRoyal,
    onTertiary = Color.White,
    background = IvoryPaper,
    onBackground = EspressoInk,
    surface = CardCream,
    onSurface = EspressoInk,
    surfaceVariant = LinenSurface,
    onSurfaceVariant = WarmTaupeText,
    outline = SubtleGoldBorder,
    error = RoseTerracotta,
    onError = Color.White,
  )

private val DarkColorScheme =
  darkColorScheme(
    primary = GoldLightAccent,
    onPrimary = ObsidianBackground,
    primaryContainer = GoldDarkContainer,
    onPrimaryContainer = WarmParchmentText,
    secondary = SubtleGoldBorder,
    onSecondary = ObsidianBackground,
    secondaryContainer = ObsidianCard,
    onSecondaryContainer = WarmParchmentText,
    tertiary = EmeraldStatus,
    onTertiary = Color.White,
    background = ObsidianBackground,
    onBackground = WarmParchmentText,
    surface = ObsidianSurface,
    onSurface = WarmParchmentText,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = SubtleGoldBorder,
    outline = WarmBronze,
    error = RoseTerracotta,
    onError = Color.White,
  )

val AppShapes =
  Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
  )

@Composable
fun SeriKadTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = AppShapes,
    content = content,
  )
}
