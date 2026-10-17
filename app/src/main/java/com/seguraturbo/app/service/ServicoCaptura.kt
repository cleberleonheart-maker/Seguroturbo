package com.seguraturbo.app.service

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import com.seguraturbo.app.R
import com.seguraturbo.app.util.Notificacoes

/** Serviço de primeiro plano que captura a tela ou grava o vídeo do jogo. */
class ServicoCaptura : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val acao = try {
            AcaoCaptura.valueOf(intent?.getStringExtra(EXTRA_ACAO) ?: AcaoCaptura.NENHUMA.name)
        } catch (e: Exception) {
            AcaoCaptura.NENHUMA
        }

        iniciarForeground()

        val codigo = intent?.getIntExtra(EXTRA_CODIGO, 0) ?: 0
        val dados: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(EXTRA_DADOS, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(EXTRA_DADOS)
        }

        if (!BarraJogo.temProjecao() && dados != null) {
            try {
                val mgr = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                val projecao = mgr.getMediaProjection(codigo, dados)
                if (projecao != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    projecao.registerCallback(object : android.media.projection.MediaProjection.Callback() {
                        override fun onStop() { }
                    }, null)
                }
                BarraJogo.definirProjecao(projecao)
            } catch (e: Exception) {
                BarraJogo.definirProjecao(null)
            }
        }

        when (acao) {
            AcaoCaptura.CAPTURA -> {
                BarraJogo.capturar(this) { arquivo ->
                    avisar(if (arquivo != null) getString(R.string.barra_captura_ok) else getString(R.string.barra_captura_erro))
                }
                pararDepois()
            }
            AcaoCaptura.INICIAR_GRAVACAO -> {
                if (!BarraJogo.iniciarGravacao(this)) {
                    avisar(getString(R.string.barra_gravacao_erro))
                }
            }
            AcaoCaptura.PARAR_GRAVACAO -> {
                val arquivo = BarraJogo.pararGravacao()
                avisar(if (arquivo != null) getString(R.string.barra_gravacao_ok) else getString(R.string.barra_gravacao_erro))
                pararDepois()
            }
            AcaoCaptura.NENHUMA -> pararDepois()
        }
        return START_NOT_STICKY
    }

    private fun iniciarForeground() {
        val notif = criarNotificacao()
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                startForeground(ID_NOTIF, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
            else -> startForeground(ID_NOTIF, notif)
        }
    }

    /** Dá um tempo curto antes de encerrar, para a captura ser concluída. */
    private fun pararDepois() {
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (!BarraJogo.gravando) stopSelf()
        }, 1500L)
    }

    private fun avisar(texto: String) {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            try {
                android.widget.Toast.makeText(applicationContext, texto, android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) { }
        }
    }

    private fun criarNotificacao(): Notification =
        Notificacoes.notificacaoJogo(this, getString(R.string.barra_gravando))

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        const val ID_NOTIF = 1004
        const val EXTRA_ACAO = "acao"
        const val EXTRA_CODIGO = "codigo"
        const val EXTRA_DADOS = "dados"

        fun executar(ctx: Context, acao: AcaoCaptura, codigo: Int, dados: Intent) {
            if (acao == AcaoCaptura.NENHUMA) return
            val i = Intent(ctx, ServicoCaptura::class.java)
                .putExtra(EXTRA_ACAO, acao.name)
                .putExtra(EXTRA_CODIGO, codigo)
                .putExtra(EXTRA_DADOS, dados)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ctx.startForegroundService(i)
                } else {
                    ctx.startService(i)
                }
            } catch (e: Exception) { }
        }

        fun iniciar(ctx: Context, acao: AcaoCaptura) {
            if (acao == AcaoCaptura.NENHUMA) return
            val i = Intent(ctx, ServicoCaptura::class.java).putExtra(EXTRA_ACAO, acao.name)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ctx.startForegroundService(i)
                } else {
                    ctx.startService(i)
                }
            } catch (e: Exception) { }
        }
    }
}
