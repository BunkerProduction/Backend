package com.BunkerProduction.other_dataclasses

import kotlinx.serialization.Serializable

@Serializable
data class Vote(
    var player: String,
    var votedFor: String
)
//{
//    "player":"12a833c4f6dca2d2",
//    "votedFor":"3c7e43425e2e704d"
//}