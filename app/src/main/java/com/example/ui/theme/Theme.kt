package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = BluePrimary,
  onPrimary = Color.White,
  primaryContainer = BlueDark,
  onPrimaryContainer = Color(0xFFDBEAFE),
  secondary = BlueSecondary,
  onSecondary = Color.Black,
  secondaryContainer = Color(0xFF1E3A8A),
  onSecondaryContainer = Color(0xFFBFDBFE),
  tertiary = InfoCyan,
  onTertiary = Color.Black,
  background = BackgroundDark,
  onBackground = TextPrimary,
  surface = SurfaceDark,
  onSurface = TextPrimary,
  surfaceVariant = SurfaceVariantDark,
  onSurfaceVariant = TextSecondary,
  outline = SurfaceBorderDark,
  outlineVariant = Color(0xFF1E293B),
  error = ErrorRed,
  onError = Color.White,
  errorContainer = ErrorBg,
  onErrorContainer = Color(0xFFFCA5A5)
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
