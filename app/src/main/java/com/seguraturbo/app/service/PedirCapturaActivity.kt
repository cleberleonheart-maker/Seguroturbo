package com.seguraturbo.app.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.seguraturbo.app.R

/** Activity transparente que pede o consentimento de captura de tela ao usuário. */
class PedirCapturaActivity : ComponentActivity() {

    private val pedido = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultado ->
        val acao = BarraJogo.pendente
        BarraJogo.pendente = AcaoCaptura.NENHUMA
        if (resultado.resultCode == Activity.RESULT_OK && resultado.data != null) {
            ServicoCaptura.executar(this, acao, resultado.resultCode, resultado.data!!)
        } else {
            Toast.makeText(
                this,
                R.string.barra_captura_negada,
                Toast.LENGTH_SHORT
            ).show()
        }
        finish()
        overridePendingTransition(0, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mgr = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        try {
            pedido.launch(mgr.createScreenCaptureIntent())
        } catch (e: Exception) {
            BarraJogo.pendente = AcaoCaptura.NENHUMA
            finish()
        }
    }
}
