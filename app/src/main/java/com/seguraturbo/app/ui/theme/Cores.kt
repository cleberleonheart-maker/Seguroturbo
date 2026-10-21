package com.seguraturbo.app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/** Conjunto de cores de um tema (claro ou escuro). */
data class Paleta(
    val fundo: Color,
    val fundoSuave: Color,
    val superficie: Color,
    val superficieClara: Color,
    val borda: Color,
    val ciano: Color,
    val roxo: Color,
    val verde: Color,
    val amarelo: Color,
    val vermelho: Color,
    val textoClaro: Color,
    val textoCinza: Color
)

private val PALETA_ESCURA = Paleta(
    fundo = Color(0xFF05070A),
    fundoSuave = Color(0xFF0A0F1A),
    superficie = Color(0xFF0F1522),
    superficieClara = Color(0xFF16203A),
    borda = Color(0xFF1E2A3E),
    ciano = Color(0xFF00F2FF),
    roxo = Color(0xFFBC13FE),
    verde = Color(0xFF00FF88),
    amarelo = Color(0xFFFFD600),
    vermelho = Color(0xFFFF0055),
    textoClaro = Color(0xFFE7ECFF),
    textoCinza = Color(0xFFB4C0DE)
)

private val PALETA_CLARA = Paleta(
    fundo = Color(0xFFF3F6FC),
    fundoSuave = Color(0xFFFFFFFF),
    superficie = Color(0xFFFFFFFF),
    superficieClara = Color(0xFFEDF1FA),
    borda = Color(0xFFD6DEEE),
    ciano = Color(0xFF0090A8),
    roxo = Color(0xFF8A1FD1),
    verde = Color(0xFF00874B),
    amarelo = Color(0xFFB07900),
    vermelho = Color(0xFFD10043),
    textoClaro = Color(0xFF101828),
    textoCinza = Color(0xFF4A5568)
)

/**
 * Paleta central do app. As cores são expostas por getters que leem um estado
 * observável (`escuro`), então alternar o tema recompõe automaticamente as telas
 * sem precisar trocar cada `Cores.X` espalhado pelo código.
 */
object Cores {

    var escuro by mutableStateOf(true)
        private set

    private val p: Paleta get() = if (escuro) PALETA_ESCURA else PALETA_CLARA

    fun definirTema(temaEscuro: Boolean) {
        escuro = temaEscuro
    }

    // Fundos
    val Fundo get() = p.fundo
    val FundoSuave get() = p.fundoSuave
    val Superficie get() = p.superficie
    val SuperficieClara get() = p.superficieClara
    val Borda get() = p.borda

    // Neon
    val Ciano get() = p.ciano
    val Roxo get() = p.roxo
    val Verde get() = p.verde
    val Amarelo get() = p.amarelo
    val Vermelho get() = p.vermelho

    // Texto
    val TextoClaro get() = p.textoClaro
    val TextoCinza get() = p.textoCinza

    /** Retorna a cor neon correspondente ao nível de risco da ameaça. */
    fun corPorRisco(risco: String): Color = when (risco) {
        "ALTO" -> Vermelho
        "MÉDIO" -> Amarelo
        else -> Verde
    }

    /** Cor neon correspondente ao nível de risco de um app. */
    fun corPorRiscoApp(risco: com.seguraturbo.app.util.RiscoApp): Color = when (risco) {
        com.seguraturbo.app.util.RiscoApp.ALTO -> Vermelho
        com.seguraturbo.app.util.RiscoApp.MEDIO -> Amarelo
        com.seguraturbo.app.util.RiscoApp.BAIXO -> Ciano
        com.seguraturbo.app.util.RiscoApp.NENHUM -> Verde
    }

    /** Cor de destaque conforme a pontuação de segurança (0–100). */
    fun corPorScore(score: Int): Color = when {
        score >= 80 -> Verde
        score >= 50 -> Amarelo
        else -> Vermelho
    }
}
