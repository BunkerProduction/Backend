package com.BunkerProduction.other_dataclasses

@kotlinx.serialization.Serializable
data class Attribute(
    var id: Int,
    var isExposed: Boolean,
)
