package com.seguraturbo.app.model

import com.seguraturbo.app.R

/** Uma versão do changelog com os itens "novo" e "correção" (arrays de strings). */
data class VersaoChangelog(
    val versao: String,
    val novidades: Int,
    val correcoes: Int
)

/**
 * Changelog do app. Cada release nova adiciona um item no topo com o que foi
 * adicionado e corrigido, traduzido nos quatro idiomas (pt/en/es/fr).
 */
object Changelog {

    /** Versões mais recentes primeiro. */
    val versoes: List<VersaoChangelog> = listOf(
        VersaoChangelog("1.27.1", R.array.changelog_1_27_1_novidades, R.array.changelog_1_27_1_correcoes),
        VersaoChangelog("1.27", R.array.changelog_1_27_novidades, R.array.changelog_1_27_correcoes),
        VersaoChangelog("1.26", R.array.changelog_1_26_novidades, R.array.changelog_1_26_correcoes),
        VersaoChangelog("1.25", R.array.changelog_1_25_novidades, R.array.changelog_1_25_correcoes),
        VersaoChangelog("1.24", R.array.changelog_1_24_novidades, R.array.changelog_1_24_correcoes),
        VersaoChangelog("1.23", R.array.changelog_1_23_novidades, R.array.changelog_1_23_correcoes),
        VersaoChangelog("1.22", R.array.changelog_1_22_novidades, R.array.changelog_1_22_correcoes),
        VersaoChangelog("1.21", R.array.changelog_1_21_novidades, R.array.changelog_1_21_correcoes),
        VersaoChangelog("1.20", R.array.changelog_1_20_novidades, R.array.changelog_1_20_correcoes),
        VersaoChangelog("1.19", R.array.changelog_1_19_novidades, R.array.changelog_1_19_correcoes),
        VersaoChangelog("1.18", R.array.changelog_1_18_novidades, R.array.changelog_1_18_correcoes),
        VersaoChangelog("1.17", R.array.changelog_1_17_novidades, R.array.changelog_1_17_correcoes),
        VersaoChangelog("1.16", R.array.changelog_1_16_novidades, R.array.changelog_1_16_correcoes)
    )

    /** Changelog de uma versão específica, ou null se não houver registro. */
    fun para(versao: String): VersaoChangelog? =
        versoes.firstOrNull { it.versao == versao }
}
