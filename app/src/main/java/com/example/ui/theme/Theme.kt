package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

val LocalReducedMotion = staticCompositionLocalOf { false }

private val LightColors = lightColorScheme(
    primary = ForestSagePrimary,
    onPrimary = OnForestSagePrimary,
    primaryContainer = ForestSageContainer,
    onPrimaryContainer = OnForestSageContainer,
    secondary = TerracottaSecondary,
    onSecondary = OnTerracottaSecondary,
    secondaryContainer = TerracottaContainer,
    onSecondaryContainer = OnTerracottaContainer,
    tertiary = SlateMistTertiary,
    onTertiary = OnSlateMistTertiary,
    tertiaryContainer = SlateMistContainer,
    onTertiaryContainer = OnSlateMistContainer,
    background = WarmLinenBackground,
    onBackground = OnWarmLinenBackground,
    surface = AlabasterSurface,
    onSurface = OnAlabasterSurface,
    surfaceVariant = SoftOatSurfaceVariant,
    onSurfaceVariant = OnSoftOatSurfaceVariant,
    outline = BotanicalOutline,
    outlineVariant = BotanicalOutlineVariant,
    error = GentleError,
    onError = OnGentleError,
    errorContainer = GentleErrorContainer,
    onErrorContainer = OnGentleErrorContainer
)

private val DarkColors = darkColorScheme(
    primary = DarkSagePrimary,
    onPrimary = OnDarkSagePrimary,
    primaryContainer = DarkSageContainer,
    onPrimaryContainer = OnDarkSageContainer,
    secondary = DarkClaySecondary,
    onSecondary = OnDarkClaySecondary,
    secondaryContainer = DarkClayContainer,
    onSecondaryContainer = OnDarkClayContainer,
    tertiary = DarkMistTertiary,
    onTertiary = OnDarkMistTertiary,
    tertiaryContainer = DarkMistContainer,
    onTertiaryContainer = OnDarkMistContainer,
    background = EveningPineBackground,
    onBackground = OnEveningPineBackground,
    surface = DeepMossSurface,
    onSurface = OnDeepMossSurface,
    surfaceVariant = ElevatedMossSurfaceVariant,
    onSurfaceVariant = OnElevatedMossSurfaceVariant,
    outline = DarkBotanicalOutline,
    outlineVariant = DarkBotanicalOutlineVariant,
    error = GentleError,
    onError = OnGentleError,
    errorContainer = GentleErrorContainer,
    onErrorContainer = OnGentleErrorContainer
)

val BotanicalShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MindBodyTheme(
    themeMode: String = "SYSTEM",
    reducedMotion: Boolean = false,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (useDarkTheme) DarkColors else LightColors

    CompositionLocalProvider(LocalReducedMotion provides reducedMotion) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = BotanicalShapes,
            content = content
        )
    }
}
