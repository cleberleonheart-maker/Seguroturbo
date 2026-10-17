package com.seguraturbo.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seguraturbo.app.R
import com.seguraturbo.app.ui.components.BotaoNeon
import com.seguraturbo.app.ui.components.CartaoFuturista
import com.seguraturbo.app.ui.components.TituloSecao
import com.seguraturbo.app.ui.theme.Cores

/**
 * Tela de boas-vindas exibida apenas no primeiro uso. Apresenta os três módulos
 * e explica que as permissões só são pedidas quando o recurso é usado.
 */
@Composable
fun OnboardingScreen(onConcluir: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cores.Fundo)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))
        Text("🛡️", fontSize = 56.sp)
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.app_name),
            color = Cores.Ciano,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Text(
            stringResource(R.string.onboarding_subtitulo),
            color = Cores.TextoCinza,
            fontSize = 13.sp
        )
        Spacer(Modifier.height(26.dp))

        ModuloCard("🛡️", stringResource(R.string.nav_seguranca), stringResource(R.string.onboarding_seg))
        Spacer(Modifier.height(12.dp))
        ModuloCard("⚡", stringResource(R.string.nav_game_turbo), stringResource(R.string.onboarding_gt))
        Spacer(Modifier.height(12.dp))
        ModuloCard("📊", stringResource(R.string.nav_benchmark), stringResource(R.string.onboarding_bm))
        Spacer(Modifier.height(18.dp))

        CartaoFuturista {
            TituloSecao(stringResource(R.string.onboarding_perm_titulo))
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(R.string.onboarding_perm_desc),
                color = Cores.TextoCinza,
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.height(24.dp))
        BotaoNeon(
            texto = stringResource(R.string.onboarding_comecar),
            onClick = onConcluir,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ModuloCard(emoji: String, titulo: String, descricao: String) {
    CartaoFuturista {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 26.sp)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(titulo, color = Cores.TextoClaro, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(descricao, color = Cores.TextoCinza, fontSize = 13.sp)
            }
        }
    }
}
