package com.BunkerProduction.other_dataclasses

import io.ktor.network.sockets.*

@kotlinx.serialization.Serializable
data class Status(
    var username: String,
    var sessionID: String,
    var text: String,
    var connection: String, //connection [user0......n]
    var isCreator: Boolean,
    var gameModel: GameModel
)

