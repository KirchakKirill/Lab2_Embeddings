package org.core.dto

enum class LLM(val key: String, val tableName: String) {
    NOMIC("nomic-embed-text:v1.5","nomic_games"),
    SNOWFLAKE("snowflake-arctic-embed2", "snowflake_games"),
    MXBAI("mxbai-embed-large:335m", "mxbai_games"),
    QWEN3("qwen3-vl:235b-instruct-cloud", "qwen3_games_chat_story"),
    NEMOTRON3("nemotron-3-nano:30b-cloud", "nemotron_chat_story")
}