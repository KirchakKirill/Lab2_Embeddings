package org.core.dto

enum class LLM(val key: String, val tableName: String) {
    NOMIC("nomic-embed-text:v1.5","nomic_games"),
    SNOWFLAKE("snowflake-arctic-embed2", "snowflake_games"),
    MXBAI("mxbai-embed-large:335m", "mxbai_games"),
    QWEN3VL("qwen3-vl:235b-instruct-cloud", "qwen3vl_games_chat_story"),
    QWEN3NEXT("qwen3-next:80b-cloud", "qwen3next_games_chat_story"),
    NEMOTRON3("nemotron-3-nano:30b-cloud", "nemotron_chat_story"),
}