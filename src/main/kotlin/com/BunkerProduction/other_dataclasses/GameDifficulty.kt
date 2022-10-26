package com.BunkerProduction.other_dataclasses

@kotlinx.serialization.Serializable
data class GameDifficulty(
    val name: String,
    val icon: String,
    val description: String
)
