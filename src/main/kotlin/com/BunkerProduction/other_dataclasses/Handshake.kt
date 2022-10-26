package com.BunkerProduction.other_dataclasses

@kotlinx.serialization.Serializable
data class Handshake(
    val type: Type_Model,
    val id: String
)
