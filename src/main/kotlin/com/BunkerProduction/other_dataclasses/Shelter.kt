package com.BunkerProduction.other_dataclasses

@kotlinx.serialization.Serializable

data class Shelter(
    val name: String,
    val icon: String,
    val description: String,
    val conditions: ShelterCondition
)