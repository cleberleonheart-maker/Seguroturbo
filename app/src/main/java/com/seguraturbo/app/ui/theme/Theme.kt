package com.seguraturbo.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import com.seguraturbo.app.model.TemaModo

/** Estado global de aparência (tamanho da fonte e modo de tema). */
object Aparencia {
    var escalaFonte by mutableStateOf(1f)
    var temaModo by mutableStateOf(TemaModo.ESCURO)
}

private fun esquemaEscuro() = darkColorScheme(
    background = Cores.Fundo,
    surface = Cores.Superficie,
    surfaceVariant = Cores.SuperficieClara,
    primary = Cores.Ciano,
    onPrimary = Cores.Fundo,
    secondary = Cores.Roxo,
    onSecondary = Cores.TextoClaro,
    error = Cores.Vermelho,
    onError = Cores.TextoClaro,
    onBackground = Cores.TextoClaro,
    onSurface = Cores.TextoClaro,
    onSurfaceVariant = Cores.TextoCinza
)

private fun esquemaClaro() = lightColorScheme(
    background = Cores.Fundo,
    surface = Cores.Superficie,
    surfaceVariant = Cores.SuperficieClara,
    primary = Cores.Ciano,
    onPrimary = Cores.TextoClaro,
    secondary = Cores.Roxo,
    onSecondary = Cores.TextoClaro,
    error = Cores.Vermelho,
    onError = Cores.TextoClaro,
    onBackground = Cores.TextoClaro,
    onSurface = Cores.TextoClaro,
    onSurfaceVariant = Cores.TextoCinza
)

private fun tipografia() = Typography(
    displayLarge = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Cores.TextoClaro, letterSpacing = 0.5.sp),
    headlineMedium = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Cores.TextoClaro, letterSpacing = 0.2.sp),
    titleLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Cores.Ciano, letterSpacing = 0.4.sp),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Cores.TextoClaro),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, color = Cores.TextoClaro),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Cores.TextoClaro),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Cores.TextoCinza),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Cores.Ciano, letterSpacing = 0.5.sp)
)

/**
 * Tema único do SeguroTurbo, aplicado UMA vez no MainActivity.
 *
 * O esquema de cores vem de [Cores] (que alterna claro/escuro por estado) e a
 * escala de fonte vem de [Aparencia]; ambos recompõem automaticamente ao mudar.
 */
@Composable
fun SeguroTurboTheme(content: @Composable () -> Unit) {
    val densidadeAtual = LocalDensity.current
    val densidadeEscalada = Density(
        density = densidadeAtual.density,
        fontScale = densidadeAtual.fontScale * Aparencia.escalaFonte
    )
    val sistemaEscuro = isSystemInDarkTheme()
    val modo = Aparencia.temaModo
    LaunchedEffect(modo, sistemaEscuro) {
        Cores.aplicarModo(modo, sistemaEscuro)
    }
    CompositionLocalProvider(LocalDensity provides densidadeEscalada) {
        MaterialTheme(
            colorScheme = if (Cores.escuro) esquemaEscuro() else esquemaClaro(),
            typography = tipografia(),
            content = content
        )
    }
}
