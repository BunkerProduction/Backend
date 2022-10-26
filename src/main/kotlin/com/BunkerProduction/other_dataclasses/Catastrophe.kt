package com.BunkerProduction.other_dataclasses

@kotlinx.serialization.Serializable

data class Catastrophe(
    val name: String,
    val icon: String,
    val shortDescription: String,
    val fullDescription: String
)
