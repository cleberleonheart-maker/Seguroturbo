package com.seguraturbo.app.service

import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.seguraturbo.app.R
import com.seguraturbo.app.util.ModoConcentracao
import com.seguraturbo.app.util.Notificacoes
import com.seguraturbo.app.util.Preferencias
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Serviço em primeiro plano que desenha a barra flutuante do Game Turbo por
 * cima do jogo: relógio, bateria e RAM, com atalhos rápidos.
 */
class ServicoBarraJogo : Service() {

    private var janela: android.view.WindowManager? = null
    private var barra: android.view.View? = null
    private var params: android.view.WindowManager.LayoutParams? = null

    private var txtRelogio: android.widget.TextView? = null
    private var txtBateria: android.widget.TextView? = null
    private var txtRam: android.widget.TextView? = null
    private var txtTimer: android.widget.TextView? = null
    private var btnGravar: android.widget.TextView? = null

    private val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    private var nivelBateria = 0
    private var carregando = false

    private var timerAtivo = false
    private var timerInicio = 0L

    private var pacoteJogo: String? = null

    private val receptorBateria = object : android.content.BroadcastReceiver() {
        override fun onReceive(c: android.content.Context?, i: android.content.Intent?) {
            i ?: return
            val nivel = i.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
            val escala = i.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
            if (nivel >= 0 && escala > 0) nivelBateria = nivel * 100 / escala
            val st = i.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1)
            carregando = st == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                    st == android.os.BatteryManager.BATTERY_STATUS_FULL
        }
    }

    override fun onBind(intent: android.content.Intent?): android.os.IBinder? = null

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_OCULTAR) {
            pararTudo()
            return START_NOT_STICKY
        }
        pacoteJogo = intent?.getStringExtra(EXTRA_PACOTE) ?: pacoteJogo
        iniciarForeground()
        if (!PermissaoSobreposicao.podeDesenhar(this)) {
            pararTudo()
            return START_NOT_STICKY
        }
        registrarBateria()
        barra?.let { return START_STICKY }
        montarBarra()
        loopAtualizar()
        return START_STICKY
    }

    private fun registrarBateria() {
        try {
            registerReceiver(receptorBateria, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
        } catch (e: Exception) { }
    }

    private fun iniciarForeground() {
        val notif = Notificacoes.notificacaoJogo(this, getString(R.string.notif_barra_texto))
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(ID_NOTIF, notif, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            startForeground(ID_NOTIF, notif, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE)
        } else {
            startForeground(ID_NOTIF, notif)
        }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun fundo(cor: Int, raio: Int, borda: Int = 0, corBorda: Int = 0): android.graphics.drawable.GradientDrawable =
        android.graphics.drawable.GradientDrawable().apply {
            setColor(cor)
            cornerRadius = raio.toFloat()
            if (borda > 0) setStroke(borda, corBorda)
        }

    private fun uniforme(cor: Int, raio: Int, borda: Int, corBorda: Int): android.graphics.drawable.GradientDrawable =
        fundo(cor, raio, borda, corBorda)

    private fun criarBotao(emoji: String, onClick: () -> Unit): android.widget.TextView =
        android.widget.TextView(this).apply {
            text = emoji
            textSize = 15f
            setTextColor(android.graphics.Color.WHITE)
            gravity = android.view.Gravity.CENTER
            setPadding(dp(8), dp(5), dp(8), dp(5))
            background = fundo(0x33FFFFFF, dp(12))
            setOnClickListener { onClick() }
        }

    private fun criarInfo(): android.widget.TextView =
        android.widget.TextView(this).apply {
            textSize = 13f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(dp(6), 0, dp(6), 0)
        }

    private fun montarBarra() {
        val wm = getSystemService(android.content.Context.WINDOW_SERVICE) as android.view.WindowManager
        janela = wm

        val linha = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(dp(10), dp(6), dp(10), dp(6))
            background = fundo(0xF2101018.toInt(), dp(22), dp(1), 0xFF00E5FF.toInt())
            elevation = dp(6).toFloat()
        }

        txtRelogio = criarInfo()
        txtBateria = criarInfo()
        txtRam = criarInfo()
        txtTimer = criarInfo().apply { setTextColor(0xFF39FF14.toInt()); visibility = android.view.View.GONE }
        linha.addView(txtRelogio)
        linha.addView(txtBateria)
        linha.addView(txtRam)
        linha.addView(txtTimer)

        linha.addView(criarBotao("\uD83D\uDCF8") { capturar() })
        btnGravar = criarBotao("\u23FA") { alternarGravacao() }
        linha.addView(btnGravar)
        linha.addView(criarBotao("\uD83D\uDD15") { alternarDnd() })
        linha.addView(criarBotao("\uD83D\uDCF6") { abrirWifi() })
        linha.addView(criarBotao("\uD83D\uDD06") { alternarBrilho() })
        linha.addView(criarBotao("\u23F1") { alternarTimer() })
        linha.addView(criarBotao("\u2699") { abrirAjustes() })
        linha.addView(criarBotao("\u2715") { pararTudo() })

        val tipo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") android.view.WindowManager.LayoutParams.TYPE_PHONE

        val p = android.view.WindowManager.LayoutParams(
            android.view.WindowManager.LayoutParams.WRAP_CONTENT,
            android.view.WindowManager.LayoutParams.WRAP_CONTENT,
            tipo,
            android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            android.graphics.PixelFormat.TRANSLUCENT
        ).apply {
            gravity = android.view.Gravity.TOP or android.view.Gravity.START
            x = dp(12)
            y = dp(60)
        }
        params = p

        arrastavel(txtRelogio!!, linha)
        arrastavel(txtBateria!!, linha)
        arrastavel(txtRam!!, linha)
        try {
            wm.addView(linha, p)
            barra = linha
        } catch (e: Exception) {
            pararTudo()
        }

        BarraJogo.aoAtualizar = { atualizarBotaoGravacao() }
        atualizarBotaoGravacao()
    }

    private fun arrastavel(alca: android.view.View, alvo: android.view.View) {
        var iniX = 0
        var iniY = 0
        var toqueX = 0f
        var toqueY = 0f
        alca.setOnTouchListener { _, e ->
            when (e.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    iniX = params?.x ?: 0
                    iniY = params?.y ?: 0
                    toqueX = e.rawX
                    toqueY = e.rawY
                    true
                }
                android.view.MotionEvent.ACTION_MOVE -> {
                    val dx = (e.rawX - toqueX).toInt()
                    val dy = (e.rawY - toqueY).toInt()
                    params?.let {
                        it.x = iniX + dx
                        it.y = iniY + dy
                        try { janela?.updateViewLayout(alvo, it) } catch (ex: Exception) { }
                    }
                    true
                }
                else -> true
            }
        }
    }

    private fun loopAtualizar() {
        handler.post(object : Runnable {
            override fun run() {
                atualizarInfos()
                handler.postDelayed(this, 1000L)
            }
        })
    }

    private fun atualizarInfos() {
        txtRelogio?.text = formatoHora.format(Date())
        txtBateria?.text = "${if (carregando) "\u26A1" else "\uD83D\uDD0B"} $nivelBateria%"
        txtRam?.text = "\uD83E\uDDE0 ${ramLivre()}"
        if (timerAtivo) {
            val seg = ((System.currentTimeMillis() - timerInicio) / 1000L).toInt()
            txtTimer?.text = String.format(Locale.getDefault(), "\u23F1 %02d:%02d", seg / 60, seg % 60)
        }
        if (BarraJogo.gravando) {
            val seg = BarraJogo.duracaoGravacao()
            txtTimer?.visibility = android.view.View.VISIBLE
            txtTimer?.text = String.format(Locale.getDefault(), "\u23FA %02d:%02d", seg / 60, seg % 60)
        } else if (!timerAtivo) {
            txtTimer?.visibility = android.view.View.GONE
        }
    }

    private fun ramLivre(): String {
        return try {
            val am = getSystemService(android.content.Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val mi = android.app.ActivityManager.MemoryInfo()
            am.getMemoryInfo(mi)
            val gb = mi.availMem.toDouble() / (1024.0 * 1024.0 * 1024.0)
            if (gb >= 1.0) String.format(Locale.getDefault(), "%.1fGB", gb)
            else String.format(Locale.getDefault(), "%dMB", mi.availMem / (1024 * 1024))
        } catch (e: Exception) {
            "-"
        }
    }

    private fun atualizarBotaoGravacao() {
        btnGravar?.text = if (BarraJogo.gravando) "\u23F9" else "\u23FA"
    }

    private fun capturar() {
        PedirCaptura()
    }

    private fun PedirCaptura() {
        if (BarraJogo.temProjecao()) {
            ServicoCaptura.iniciar(this, AcaoCaptura.CAPTURA)
            return
        }
        BarraJogo.pendente = AcaoCaptura.CAPTURA
        try {
            startActivity(android.content.Intent(this, PedirCapturaActivity::class.java)
                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) { }
    }

    private fun alternarGravacao() {
        if (BarraJogo.gravando) {
            ServicoCaptura.iniciar(this, AcaoCaptura.PARAR_GRAVACAO)
            return
        }
        if (BarraJogo.temProjecao()) {
            ServicoCaptura.iniciar(this, AcaoCaptura.INICIAR_GRAVACAO)
            return
        }
        BarraJogo.pendente = AcaoCaptura.INICIAR_GRAVACAO
        try {
            startActivity(android.content.Intent(this, PedirCapturaActivity::class.java)
                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) { }
    }

    private fun alternarDnd() {
        try {
            if (ModoConcentracao.estaAtivo(this)) ModoConcentracao.restaurar(this)
            else ModoConcentracao.ativar(this)
        } catch (e: Exception) { }
    }

    private fun abrirWifi() {
        try {
            val i = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q)
                android.content.Intent(android.provider.Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
            else
                android.content.Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
            i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
        } catch (e: Exception) {
            try {
                startActivity(android.content.Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (ex: Exception) { }
        }
    }

    private fun alternarBrilho() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&
                !android.provider.Settings.System.canWrite(this)
            ) {
                startActivity(android.content.Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS)
                    .setData(android.net.Uri.parse("package:$packageName"))
                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                return
            }
            val atual = android.provider.Settings.System.getInt(
                contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS, 128
            )
            val proximo = when {
                atual < 85 -> 160
                atual < 170 -> 255
                else -> 40
            }
            android.provider.Settings.System.putInt(contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS, proximo)
            try {
                android.provider.Settings.System.putInt(
                    contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
                    android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                )
            } catch (e: Exception) { }
        } catch (e: Exception) { }
    }

    private fun alternarTimer() {
        if (timerAtivo) {
            timerAtivo = false
            txtTimer?.visibility = android.view.View.GONE
        } else {
            timerAtivo = true
            timerInicio = System.currentTimeMillis()
            txtTimer?.visibility = android.view.View.VISIBLE
        }
    }

    private fun abrirAjustes() {
        try {
            startActivity(android.content.Intent(android.provider.Settings.ACTION_SETTINGS)
                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) { }
    }

    private fun pararTudo() {
        try { handler.removeCallbacksAndMessages(null) } catch (e: Exception) { }
        try { unregisterReceiver(receptorBateria) } catch (e: Exception) { }
        try { BarraJogo.pararGravacao() } catch (e: Exception) { }
        try { BarraJogo.liberar() } catch (e: Exception) { }
        BarraJogo.aoAtualizar = null
        try {
            barra?.let { janela?.removeView(it) }
        } catch (e: Exception) { }
        barra = null
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        try { handler.removeCallbacksAndMessages(null) } catch (e: Exception) { }
        try { unregisterReceiver(receptorBateria) } catch (e: Exception) { }
        try { (getSystemService(android.content.Context.WINDOW_SERVICE) as android.view.WindowManager).removeView(barra) } catch (e: Exception) { }
        BarraJogo.aoAtualizar = null
        super.onDestroy()
    }

    companion object {
        const val CANAL = "seguraturbo_jogo"
        const val ID_NOTIF = 1003
        const val ACTION_MOSTRAR = "com.seguraturbo.app.MOSTRAR_BARRA"
        const val ACTION_OCULTAR = "com.seguraturbo.app.OCULTAR_BARRA"
        const val EXTRA_PACOTE = "pacote"

        fun iniciar(ctx: android.content.Context, pacote: String?) {
            val i = android.content.Intent(ctx, ServicoBarraJogo::class.java)
                .setAction(ACTION_MOSTRAR)
                .putExtra(EXTRA_PACOTE, pacote)
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                    ctx.startForegroundService(i)
                else ctx.startService(i)
            } catch (e: Exception) { }
        }

        fun parar(ctx: android.content.Context) {
            val i = android.content.Intent(ctx, ServicoBarraJogo::class.java).setAction(ACTION_OCULTAR)
            try { ctx.startService(i) } catch (e: Exception) { }
        }
    }
}
