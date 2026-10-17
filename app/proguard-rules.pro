# Regras ProGuard/R8 do SeguroTurbo

# Modelos de dados (não usam reflexão, mas mantemos por segurança).
-keep class com.seguraturbo.app.model.** { *; }

# Mantém chamadas de métodos de anotação/compose que o R8 pode remover sem querer.
-dontwarn org.jetbrains.annotations.**
-dontwarn kotlinx.coroutines.**

# Preserva os nomes das classes de ViewModel usadas pelo AndroidX.
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
