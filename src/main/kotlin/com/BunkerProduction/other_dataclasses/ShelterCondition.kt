package com.BunkerProduction.other_dataclasses

@kotlinx.serialization.Serializable

data class ShelterCondition(
    var isExposed: Boolean,
    val icon: String,
    val name: String,
    val description: String
)
