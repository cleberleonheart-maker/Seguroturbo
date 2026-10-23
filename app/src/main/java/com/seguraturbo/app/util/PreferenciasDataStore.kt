package com.seguraturbo.app.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "seguraturbo_datastore")

/**
 * DataStore paralelo às SharedPreferences. Mantém API compatível e faz fallback
 * para os valores antigos (migração gradual, sem quebra).
 */
object PreferenciasDataStore {

    private val K_TEMA_ESCURO = booleanPreferencesKey("tema_escuro")
    private val K_TEMA_MODO = stringPreferencesKey("tema_modo")
    private val K_ESCALA_FONTE = floatPreferencesKey("escala_fonte")
    private val K_JOGOS_OTIMIZADOS = stringSetPreferencesKey("jogos_otimizados")
    private val K_LEMBRETE = booleanPreferencesKey("lembrete_ativo")
    private val K_ULTIMO_SCORE = intPreferencesKey("ultimo_score")
    private val K_VERSAO_IGNORADA = stringPreferencesKey("versao_ignorada")
    private val K_DND_AUTO = booleanPreferencesKey("dnd_auto")
    private val K_TRAVA_TELA_JOGO = booleanPreferencesKey("trava_tela_jogo")
    private val K_SILENCIAR_MIDIA = booleanPreferencesKey("silenciar_midia_jogo")
    private val K_VOLUME_MIDIA = intPreferencesKey("volume_midia_anterior")
    private val K_FILTRO_ANTERIOR = intPreferencesKey("filtro_anterior")
    private val K_PERFIL_DESEMPENHO = stringPreferencesKey("perfil_desempenho")
    private val K_PERFIL_USO = stringPreferencesKey("perfil_uso")
    private val K_ONBOARDING_VISTO = booleanPreferencesKey("onboarding_visto")
    private val K_ATUALIZACAO_AUTO = booleanPreferencesKey("atualizacao_automatica")
    private val K_ULTIMA_VERSAO_VISTA = stringPreferencesKey("ultima_versao_vista")
    private val K_AUTO_LIMPAR_RAM_JOGO = booleanPreferencesKey("auto_limpar_ram_jogo")
    private val K_SESSAO_FOCO_ATIVO = booleanPreferencesKey("sessao_foco_ativo")
    private val K_SESSAO_FOCO_FIM = longPreferencesKey("sessao_foco_fim")

    fun getBoolean(ctx: Context, key: Preferences.Key<Boolean>, def: Boolean): Boolean = runBlocking {
        val ds = ctx.dataStore.data.first()
        if (ds.contains(key)) ds[key]!! else def
    }

    fun getInt(ctx: Context, key: Preferences.Key<Int>, def: Int): Int = runBlocking {
        val ds = ctx.dataStore.data.first()
        if (ds.contains(key)) ds[key]!! else def
    }

    fun getLong(ctx: Context, key: Preferences.Key<Long>, def: Long): Long = runBlocking {
        val ds = ctx.dataStore.data.first()
        if (ds.contains(key)) ds[key]!! else def
    }

    fun getFloat(ctx: Context, key: Preferences.Key<Float>, def: Float): Float = runBlocking {
        val ds = ctx.dataStore.data.first()
        if (ds.contains(key)) ds[key]!! else def
    }

    fun getString(ctx: Context, key: Preferences.Key<String>, def: String): String = runBlocking {
        val ds = ctx.dataStore.data.first()
        if (ds.contains(key)) ds[key]!! else def
    }

    fun getStringSet(ctx: Context, key: Preferences.Key<Set<String>>, def: Set<String>): Set<String> = runBlocking {
        val ds = ctx.dataStore.data.first()
        ds[key] ?: def
    }

    fun putBoolean(ctx: Context, key: Preferences.Key<Boolean>, value: Boolean) {
        runBlocking { ctx.dataStore.edit { it[key] = value } }
    }

    fun putInt(ctx: Context, key: Preferences.Key<Int>, value: Int) {
        runBlocking { ctx.dataStore.edit { it[key] = value } }
    }

    fun putLong(ctx: Context, key: Preferences.Key<Long>, value: Long) {
        runBlocking { ctx.dataStore.edit { it[key] = value } }
    }

    fun putFloat(ctx: Context, key: Preferences.Key<Float>, value: Float) {
        runBlocking { ctx.dataStore.edit { it[key] = value } }
    }

    fun putString(ctx: Context, key: Preferences.Key<String>, value: String) {
        runBlocking { ctx.dataStore.edit { it[key] = value } }
    }

    fun putStringSet(ctx: Context, key: Preferences.Key<Set<String>>, value: Set<String>) {
        runBlocking { ctx.dataStore.edit { it[key] = value } }
    }
}
