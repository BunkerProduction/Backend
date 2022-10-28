package com.BunkerProduction.other_dataclasses

import kotlinx.serialization.Serializable

@Serializable
data class Error(
    var typeModel: Type_Model,
    var message:String
)
