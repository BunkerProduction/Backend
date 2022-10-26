package com.BunkerProduction.other_dataclasses
@kotlinx.serialization.Serializable
data class Player(
    val id: String,
    var isCreator: Boolean,
    var username: String,
    var attributes: Array<Attribute>
)