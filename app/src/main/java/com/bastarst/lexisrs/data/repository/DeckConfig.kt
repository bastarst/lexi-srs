package com.bastarst.lexisrs.data.repository

data class DeckConfig(
    val id: Long,
    val name: String,
    val assetName: String
) {
    companion object {
        val OXFORD_3000 = DeckConfig(
            id = 0L,
            name = "Oxford 3000",
            assetName = "Oxford_3000_English_Russian.csv"
        )

        val OXFORD_5000 = DeckConfig(
            id = 1L,
            name = "Oxford 5000",
            assetName = "Oxford_5000_English_Russian.csv"
        )
        val PHRASAL_VERBS = DeckConfig(
            id = 2L,
            name = "Phrasal Verbs",
            assetName = "English_Russian_Phrasal_Verbs.csv"
        )

        val ALL: List<DeckConfig> = listOf(OXFORD_3000, OXFORD_5000, PHRASAL_VERBS)

        fun byId(id: Long): DeckConfig =
            ALL.firstOrNull { it.id == id } ?: OXFORD_3000
    }
}