package com.BunkerProduction.other_dataclasses

@kotlinx.serialization.Serializable
data class GamePreferences(
    var votingTime: Int,
    val catastropheId: Int,
    var gameConditions: List<GameCondition>,
    val shelterId: Int,
    val difficultyId: Int
)