package com.BunkerProduction.room

import com.BunkerProduction.other_dataclasses.Attribute
import io.ktor.websocket.*

@kotlinx.serialization.Serializable
data class Player(
    val id: String,
    val username: String,
    var sessionID: String,
    var socket: WebSocketSession?,
    var isCreator: Boolean,
    var attributes: Array<Attribute>
)
