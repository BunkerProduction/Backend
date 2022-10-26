package com.BunkerProduction.room

@kotlinx.serialization.Serializable
class SessionIsNone: Exception (
    "Такой комнаты не сущевствует"
)