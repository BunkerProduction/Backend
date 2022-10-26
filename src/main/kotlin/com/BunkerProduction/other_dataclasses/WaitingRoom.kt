package com.BunkerProduction.other_dataclasses
@kotlinx.serialization.Serializable
data class WaitingRoom(
    val type: Type_Model,
    var players: List<Player>,
    var sessionID: String
)