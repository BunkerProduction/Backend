package com.BunkerProduction.plugins

import com.BunkerProduction.di.di
import com.BunkerProduction.room.RoomController
import com.BunkerProduction.routings.gameSocket
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.newInstance

fun Application.configureRouting() {
    val roomController by di.newInstance { RoomController() }

    routing {
        get("/") {
            call.respondText("Hi! it's Bunker")
        }
        gameSocket(roomController)
    }
}