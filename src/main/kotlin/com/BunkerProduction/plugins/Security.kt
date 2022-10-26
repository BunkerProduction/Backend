package com.BunkerProduction.plugins

import com.BunkerProduction.di.di
import com.BunkerProduction.enums.GameState
import com.BunkerProduction.other_dataclasses.*
import com.BunkerProduction.room.RoomController
import com.BunkerProduction.session.GameSession
import io.ktor.server.application.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import org.kodein.di.newInstance


fun Application.configureSecurity() {
    val roomController by di.newInstance { RoomController() }
    install(Sessions) {
        cookie<GameSession>("GAME_SESSION")
    }
    intercept(ApplicationCallPipeline.Plugins) {

        if((call.sessions.get<GameSession>() == null)) //Проверка были ли уже сессии
        {
            var username = call.parameters["username"] ?: "Player"
            var sessionID = call.parameters["sessionID"] ?: "None"
            var isCreator = call.parameters["isCreator"] ?: "true"
            var voitingTime = call.parameters["voitingTime"] ?: 0
            var initialNumberOfPlayers = call.parameters["initialNumberOfPlayers"] ?: 1
            var turn = call.parameters["turn"] ?: "_"
            var round = call.parameters["round"] ?: 0

            var gamePreferences = GamePreferences(
                votingTime = 4,
                catastropheId = 1,
                gameConditions = roomController.get_conditions(),
                shelterId = 2,
                difficultyId = 3
            )
            var gameState = GameState.normal //по умолчанию стоит normal


            if((sessionID == "None")&&(isCreator == "true")) {
                sessionID = roomController.is_exist(sessionID)
            }

                var gameModel = GameModel(
                    started = false,
                    type = Type_Model.game_model,
                    sessionID = sessionID,
                    preferences = gamePreferences,
                    players = null,
                    gameState = gameState,
                    initialNumberOfPlayers = initialNumberOfPlayers as Int,
                    turn = turn,
                    round = round as Int,
                    votes = null,
                    set_of_voters = null
                )

                val id = generateNonce()
                call.sessions.set(GameSession(id, username, sessionID, isCreator, gameModel))

        }

        }
    }

