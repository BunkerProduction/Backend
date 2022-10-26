package com.BunkerProduction.other_dataclasses

@kotlinx.serialization.Serializable
data class GameCondition(
    val Condition: Int,
    var isExposed: Boolean
)
