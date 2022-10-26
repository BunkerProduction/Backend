package com.BunkerProduction.other_dataclasses

import java.util.StringJoiner
@kotlinx.serialization.Serializable

data class AttributeOpening(
    val attributeNumber: Int,
    val playerID: String,
)