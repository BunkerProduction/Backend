package com.BunkerProduction.other_dataclasses

interface DataSource {
    suspend fun getData()
    suspend fun insertData()
}