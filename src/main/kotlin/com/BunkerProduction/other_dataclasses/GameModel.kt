package com.BunkerProduction.other_dataclasses

import com.BunkerProduction.enums.GameState

@kotlinx.serialization.Serializable
data class GameModel(
    var started: Boolean,
    val type: Type_Model,
    var sessionID: String,
    var preferences: GamePreferences,
    var players: MutableList<Player>?,
    var gameState: GameState,
    var initialNumberOfPlayers: Int,
    var turn: String,
    var round: Int,
    var votes: MutableMap<String, Int>?,
    var set_of_voters: MutableList<String>?
)
