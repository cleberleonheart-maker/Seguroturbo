package com.seguraturbo.app.util

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import org.json.JSONArray
import org.json.JSONObject

object TesteConsumoBateria {
    private const val CHAVE = "bateria_teste_json"

    data class Resultado(
        val inicio: Long,
        val fim: Long,
        val nivelInicio: Int,
        val nivelFim: Int,
        val drenagem: Int,
        val duracaoMin: Long
    )

    fun nivelAtual(ctx: Context): Int {
        val intent = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val nivel = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val escala = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (nivel >= 0 && escala > 0) (nivel * 100 / escala) else -1
    }

    fun salvar(ctx: Context, r: Resultado) {
        val arr = JSONArray(Armazenamento.lerTexto(ctx, CHAVE, "[]") ?: "[]")
        val o = JSONObject().apply {
            put("inicio", r.inicio)
            put("fim", r.fim)
            put("nivelInicio", r.nivelInicio)
            put("nivelFim", r.nivelFim)
            put("drenagem", r.drenagem)
            put("duracaoMin", r.duracaoMin)
        }
        arr.put(o)
        if (arr.length() > 10) {
            val arr2 = JSONArray()
            for (i in arr.length() - 10 until arr.length()) arr2.put(arr.get(i))
            Armazenamento.gravarTexto(ctx, CHAVE, arr2.toString())
        } else {
            Armazenamento.gravarTexto(ctx, CHAVE, arr.toString())
        }
    }

    fun listar(ctx: Context): List<Resultado> {
        val arr = JSONArray(Armazenamento.lerTexto(ctx, CHAVE, "[]") ?: "[]")
        val lista = mutableListOf<Resultado>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            lista.add(
                Resultado(
                    inicio = o.optLong("inicio"),
                    fim = o.optLong("fim"),
                    nivelInicio = o.optInt("nivelInicio"),
                    nivelFim = o.optInt("nivelFim"),
                    drenagem = o.optInt("drenagem"),
                    duracaoMin = o.optLong("duracaoMin")
                )
            )
        }
        return lista
    }
}
