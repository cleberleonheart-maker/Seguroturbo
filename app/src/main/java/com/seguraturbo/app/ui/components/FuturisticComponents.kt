package com.seguraturbo.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seguraturbo.app.R
import com.seguraturbo.app.model.HistoricoEntrada
import com.seguraturbo.app.ui.theme.Cores
import com.seguraturbo.app.util.Formato

/** Cartão com aparência de vidro (glassmorphism) e gradiente sutil. */
@Composable
fun CartaoFuturista(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    conteudo: @Composable ColumnScope.() -> Unit
) {
    val base = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(18.dp))
        .background(Brush.verticalGradient(listOf(Cores.Superficie, Cores.SuperficieClara)))
        .border(1.dp, Cores.Borda, RoundedCornerShape(18.dp))
    val comClique = if (onClick != null) base.clickable { onClick() } else base
    Box(modifier = comClique) {
        Column(modifier = Modifier.padding(18.dp), content = conteudo)
    }
}

/**
 * Botão com contorno neon: fundo escuro, borda e RÓTULO na cor de destaque.
 * O texto é o elemento mais forte (fácil de ler), em vez do bloco de cor cheio.
 */
@Composable
fun BotaoNeon(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    cor: Color = Cores.Ciano
) {
    val corAtiva = if (habilitado) cor else Cores.TextoCinza
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(corAtiva.copy(alpha = if (habilitado) 0.14f else 0.06f))
            .border(
                width = 1.5.dp,
                color = corAtiva.copy(alpha = if (habilitado) 0.9f else 0.35f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(enabled = habilitado) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            texto,
            color = corAtiva,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

/** Barra de progresso com gradiente neon. */
@Composable
fun BarraProgressoNeon(
    progresso: Float,
    modifier: Modifier = Modifier,
    cor: Color = Cores.Ciano
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Cores.Fundo)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progresso.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(Brush.horizontalGradient(listOf(cor, Cores.Roxo)))
        )
    }
}

/** Pequeno selo de status colorido. */
@Composable
fun SeloStatus(texto: String, cor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(cor.copy(alpha = 0.15f))
            .border(1.dp, cor.copy(alpha = 0.6f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(texto, color = cor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

/** Título de seção com barrinha neon. */
@Composable
fun TituloSecao(texto: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(4.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Cores.Ciano)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            texto,
            color = Cores.Ciano,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

/** Linha rótulo/valor usada nas fichas de dispositivo. */
@Composable
fun LinhaInfo(rotulo: String, valor: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(rotulo, color = Cores.TextoCinza, fontSize = 13.sp)
        Text(valor, color = Cores.TextoClaro, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Gráfico de linha (sparkline) usado para mostrar a evolução das pontuações. */
@Composable
fun GraficoLinha(
    valores: List<Int>,
    modifier: Modifier = Modifier,
    cor: Color = Cores.Ciano
) {
    if (valores.size < 2) return
    val maximo = valores.max().toFloat()
    val minimo = valores.min().toFloat()
    val faixa = (maximo - minimo).coerceAtLeast(1f)

    Canvas(modifier = modifier.fillMaxWidth().height(96.dp)) {
        val largura = size.width
        val altura = size.height
        val passo = largura / (valores.size - 1)

        val linha = Path()
        valores.forEachIndexed { i, v ->
            val x = i * passo
            val y = altura - ((v - minimo) / faixa) * altura
            if (i == 0) linha.moveTo(x, y) else linha.lineTo(x, y)
        }

        // Área preenchida sob a linha, com desvanecimento neon.
        val area = Path().apply {
            addPath(linha)
            lineTo(largura, altura)
            lineTo(0f, altura)
            close()
        }
        drawPath(
            path = area,
            brush = Brush.verticalGradient(listOf(cor.copy(alpha = 0.35f), Color.Transparent))
        )
        drawPath(
            path = linha,
            color = cor,
            style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/** Bloco de histórico compartilhado pelas telas. */
@Composable
fun HistoricoSecao(
    historico: List<HistoricoEntrada>,
    onLimpar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TituloSecao(stringResource(R.string.hist_title))
            if (historico.isNotEmpty()) {
                TextButton(onClick = onLimpar) {
                    Text(stringResource(R.string.hist_clear), color = Cores.TextoCinza, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (historico.isEmpty()) {
            Text(
                stringResource(R.string.hist_empty),
                color = Cores.TextoCinza,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            historico.take(5).forEach { entrada ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(iconeTipo(entrada.tipo), fontSize = 18.sp)
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entrada.titulo, color = Cores.TextoClaro, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(entrada.detalhe, color = Cores.TextoCinza, fontSize = 11.sp)
                    }
                    Text(Formato.data(entrada.data), color = Cores.TextoCinza, fontSize = 10.sp)
                }
            }
        }
    }
}

private fun iconeTipo(tipo: HistoricoEntrada.Tipo): String = when (tipo) {
    HistoricoEntrada.Tipo.SCAN -> "🛡️"
    HistoricoEntrada.Tipo.LIMPEZA -> "🧹"
    HistoricoEntrada.Tipo.BENCHMARK -> "📊"
    HistoricoEntrada.Tipo.TURBO -> "⚡"
}
