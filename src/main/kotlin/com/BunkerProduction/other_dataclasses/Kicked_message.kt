package com.BunkerProduction.other_dataclasses

import kotlinx.serialization.Serializable

@Serializable
data class Kicked_message(
    val type: String,
    val id: String
)
