package com.BunkerProduction.other_dataclasses

import kotlinx.serialization.Serializable

@Serializable
data class Error(
    var type: Type_Model,
    var message:String
)
