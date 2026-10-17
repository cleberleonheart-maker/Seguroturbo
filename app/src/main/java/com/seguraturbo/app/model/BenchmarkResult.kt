package com.seguraturbo.app.model

data class BenchmarkResult(
    val cpu: Int = 0,
    val gpu: Int = 0,
    val mem: Int = 0,
    val ux: Int = 0,
    /** FPS medidos no teste de GPU (OpenGL ES). 0 = estimativa usada. */
    val gpuFps: Int = 0,
    /** Velocidade medida do armazenamento em MB/s (0 = não medido). */
    val armazenamentoMbs: Double = 0.0
) {
    val total: Int
        get() = cpu + gpu + mem + ux
}
