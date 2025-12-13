package org.example.data

enum class LLM(val key: String, val tableName: String) {
    NOMIC("nomic-embed-text:v1.5","nomic_games"),
    SNOWFLAKE("snowflake-arctic-embed2", "snowflake_games"),
    MXBAI("mxbai-embed-large", "mxbai_games")
}