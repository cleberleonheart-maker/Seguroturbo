package com.seguraturbo.app.util

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs

/**
 * Coleta informações REAIS do dispositivo (nada simulado).
 * Usa apenas APIs do próprio Android — sem dependências extras.
 */
object DeviceInfo {

    data class Info(
        val fabricante: String,
        val modelo: String,
        val chipset: String,
        val nucleos: Int,
        val ramTotalMb: Long,
        val ramLivreMb: Long,
        val armazenamentoTotalGb: Double,
        val armazenamentoLivreGb: Double,
        val versaoAndroid: String
    )

    fun coletar(ctx: Context): Info {
        val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoria = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memoria)

        val stat = StatFs(Environment.getDataDirectory().path)
        val bloco = stat.blockSizeLong
        val totalArm = stat.blockCountLong * bloco
        val livreArm = stat.availableBlocksLong * bloco

        return Info(
            fabricante = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            modelo = Build.MODEL,
            chipset = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SOC_MODEL else Build.HARDWARE,
            nucleos = Runtime.getRuntime().availableProcessors(),
            ramTotalMb = memoria.totalMem / (1024L * 1024L),
            ramLivreMb = memoria.availMem / (1024L * 1024L),
            armazenamentoTotalGb = totalArm.toDouble() / (1024.0 * 1024.0 * 1024.0),
            armazenamentoLivreGb = livreArm.toDouble() / (1024.0 * 1024.0 * 1024.0),
            versaoAndroid = "Android ${Build.VERSION.RELEASE}"
        )
    }
}
