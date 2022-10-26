package com.BunkerProduction.routings

import com.BunkerProduction.ios_client_version
import com.BunkerProduction.other_dataclasses.*
import com.BunkerProduction.room.RoomController
import com.BunkerProduction.session.GameSession
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class Connection(val session: DefaultWebSocketSession) {
    companion object {
        var lastId = AtomicInteger(0)
    }

    val name = "user${lastId.getAndIncrement()}"
}

fun Control_Version(oldVersionName: String?, newVersionName: String): Boolean {
    var oldNumbers = oldVersionName?.split(".")
    var newNumbers = newVersionName.split(".")
    var oldVersion: String = ""
    var newVersion: String = ""
    var res: Boolean = true
    if (oldNumbers != null) {
        for (number in oldNumbers) {
            oldVersion += number
        }
    }
    for (number in newNumbers) {
        newVersion += number
    }
    return oldVersion.toInt() < newVersion.toInt()
}

fun Route.gameSocket(roomController: RoomController) {
    val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
    webSocket("/game") {
        val thisConnection = Connection(this)
        connections += thisConnection

//            send("You've logged in as [${thisConnection.name}]")
//            send("This_connection_session: [${thisConnection.session}]")
//            send("Players_in_Hash_Map_before_connect: [${roomController.get_members()}]")
        val session = call.sessions.get<GameSession>()

        if (session == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session."))
            return@webSocket
        }
        try {
            if (session.isCreator == "true") {
                roomController.onCreateGame(
                    id = session.id,
                    username = session.username,
                    sessionID = session.sessionID,
                    socket = this,
                    gameModel = session.gameModel
                )

                if (Control_Version(call.request.headers["version_client"].toString(), ios_client_version)) {
                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "TheСlientMustBeUpdated"))
                    return@webSocket
                }
                val handshake = Handshake(
                    type = Type_Model.handshake,
                    id = session.id
                )
                send(Json.encodeToJsonElement(handshake).toString())

//                   send("gameModel: ${roomController.getgameModels()}")
//                   send("isCreator: ${session.isCreator}")
            }
            if ((session.isCreator == "false") && (roomController.roomisExist(session.sessionID))) {
                val handshake = Handshake(
                    type = Type_Model.handshake,
                    id = session.id
                )
                send(Json.encodeToJsonElement(handshake).toString())

                roomController.onJoin(
                    id = session.id,
                    username = session.username,
                    sessionID = session.sessionID,
                    socket = this
                )
            }

//                        close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "The game has already started, you can not join"))
//                        return@webSocket

//                    send(Json.encodeToJsonElement(roomController.get_waitingRoom(session.sessionID)).toString())
//                    send("gameModels: ${roomController.getgameModels()}")
//                   send("isCreator : ${session.isCreator}")
//                     send(roomController.getPlayers(session.sessionID).toString())


            if ((session.isCreator == "false") && (!roomController.roomisExist(session.sessionID))) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "SessionNotExistsError"))
                return@webSocket
//                    send("gameModels: ${roomController.getgameModels()}")
//                    send("isCreator : ${session.isCreator}")
            }

            incoming.consumeEach { frame ->

                if ((frame is Frame.Text) && (frame.readText() != "waiting_room") && (frame.readText() != "game") && (frame.readText() != "game_models") && (frame.readText() != "clean") && (frame.readText() != "turn")) {
                    if (frame.readText().contains("catastropheId")) {
                        val input_json = Json.decodeFromString<GamePreferences>(frame.readText())
                        input_json.gameConditions = roomController.get_conditions()
                        roomController.GetDataShatus(
                            gamePreferences = input_json,
                            sessionID = session.sessionID
                        )

                    }
                    if (frame.readText().contains("attributeNumber")) {
                        val input_json = Json.decodeFromString<AttributeOpening>(frame.readText())
                        if (roomController.opening_attribute(input_json, session.sessionID)) {
//                               print("Attribute succes open");
                        } else {
//                               print("Attribute don't open");
                        }

                    }
                    if (frame.readText().contains("votedFor")) {
                        val input_json = Json.decodeFromString<Vote>(frame.readText())
                            if(roomController.check_voting(
                                session.sessionID,
                                roomController.getPlayers_NONEsocket(sessionID = session.sessionID),
                                roomController.get_set_of_votes(session.sessionID)
                            ))
                            {
                                print("\n-------\nГолосование закончилось\n--------\n")
                                roomController.make_set_of_votes(input_json.votedFor, session.sessionID)
                                roomController.add_last_vote(input_json, session.sessionID)
                                roomController.get_gamemodel_after_voting(session.sessionID)
                            }
                            else
                            {
                                print("\n-------\nГолосование НЕ закончилось\n--------\n")
                                roomController.new_vote(input_json, session.sessionID)
                                roomController.gamemodelToMembers(session.sessionID)
                            }

                    }
//                        send("gameModels: ${roomController.getgameModels()}")
//                        send("roomisexist: ${roomController.roomisExist(session.sessionID)}")
//                        send("Players_in_Hash_Map_after_connect: [${roomController.get_members()}]")
                }

                if ((frame is Frame.Text) && (frame.readText() == "waiting_room")) {
                    send(Json.encodeToJsonElement(roomController.get_waitingRoom(session.sessionID)).toString())
                }
                if ((frame is Frame.Text) && (frame.readText() == "game")) {
                    roomController.change_game_status(session.sessionID)
                    roomController.gamemodelToMembers(session.sessionID)
                }
                if ((frame is Frame.Text) && (frame.readText() == "game_models")) {
                    send(Json.encodeToJsonElement(roomController.getgameModels()).toString())
                }
                if ((frame is Frame.Text) && (frame.readText() == "clean")) {
                    send(roomController.clean())
                }
                if ((frame is Frame.Text) && (frame.readText() == "voting")) {
                    var players = roomController.getPlayers_NONEsocket(sessionID = session.sessionID)

                }

            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            roomController.tryDissconect(this, session.sessionID, closeReason.await()?.message.toString())
            connections.remove(thisConnection)

        }
    }

}








