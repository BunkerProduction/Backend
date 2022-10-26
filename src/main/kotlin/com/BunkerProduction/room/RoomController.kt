package com.BunkerProduction.  room

import com.BunkerProduction.enums.GameState
import com.BunkerProduction.other_dataclasses.*
import com.BunkerProduction.session.GenerateRoomCode
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.util.concurrent.ConcurrentHashMap

class RoomController () {

     private val members = ConcurrentHashMap<String, Player>()
     private val gamemodel = ConcurrentHashMap<String, GameModel>()
     private val players_num_votes = ConcurrentHashMap<String, MutableMap<String, Int>>()
     private val players_pla_votes = ConcurrentHashMap<String, MutableMap<String, ArrayList<String>>>()
     private val kicked_members = ConcurrentHashMap<String, Player>()
     private val hosts = ConcurrentHashMap<String, String>()

    fun set_hosts(sessionID: String, hostID: String)
    {
        hosts[sessionID] = hostID
    }
    fun getgameModels(): MutableList<GameModel> { return gamemodel.values.toMutableList() }
    fun clean(): String { gamemodel.values.clear(); members.values.clear(); kicked_members.clear(); return "Success clean"}
    fun getgameModel(sessionID: String): List<GameModel> { return (gamemodel.values.filter { gameModel -> gameModel.sessionID == sessionID  })}
    fun change_game_status(sessionID: String){
        gamemodel[sessionID]?.started = true
    }

    fun new_vote(players: Vote, sessionID: String){
//        var players_in_room: ArrayList<String> = ArrayList()
//
//        members.values.forEach { member ->
//            if (member.sessionID == sessionID) {
//                    players_in_room.add(member.id)
//            }
//        }
        if(gamemodel[sessionID]?.votes?.get(players.player) == null)
        {

            print("-----\n\nСписок голосов пуст! \n\n--------")
            var playersArray: MutableMap<String, Int>
            var playersArray_pla: MutableMap<String, ArrayList<String>>
            if(players_num_votes[sessionID] == null)
            {
                playersArray = mutableMapOf()
                playersArray_pla = mutableMapOf()
            }
            else {
                playersArray = players_num_votes[sessionID]!!
                playersArray_pla = players_pla_votes[sessionID]!!
            }
            playersArray.put(players.player, 1)
            playersArray_pla.put(players.player, arrayListOf(players.votedFor))
            players_num_votes[sessionID] = playersArray
            players_pla_votes[sessionID] = playersArray_pla
            gamemodel[sessionID]?.votes = players_num_votes[sessionID]

//            print(players_pla_votes[sessionID].toString())

        }
        else
        {
            print("-----\n\nСписок голосов НЕе пуст! \n\n--------")
            var countVotes = gamemodel[sessionID]?.votes?.get(players.player)
                countVotes = countVotes!! + 1
            var playerVoted = players_pla_votes[sessionID]?.get(players.player)
            playerVoted?.add(players.votedFor)
            if (playerVoted != null) {
                players_pla_votes[sessionID]?.put(players.player, playerVoted)
            }
//            print(players_pla_votes[sessionID].toString())
                gamemodel[sessionID]?.votes?.put(players.player, countVotes)
        }

        make_set_of_votes(players.votedFor, sessionID)
    }
    fun add_last_vote(players: Vote, sessionID: String){
        var countVotes = gamemodel[sessionID]?.votes?.get(players.player)
        countVotes = countVotes!! + 1
        var playerVoted = players_pla_votes[sessionID]?.get(players.player)
        playerVoted?.add(players.votedFor)
        if (playerVoted != null) {
            players_pla_votes[sessionID]?.put(players.player, playerVoted)
        }
//            print(players_pla_votes[sessionID].toString())
        gamemodel[sessionID]?.votes?.put(players.player, countVotes)
    }
    fun change_gamestate(state: Enum<GameState>, sessionID: String){
        gamemodel[sessionID]?.gameState = state as GameState
    }
    fun remove_votes(sessionID: String){

    }
    fun make_set_of_votes(player: String, sessionID: String)
    {
        if(gamemodel[sessionID]?.set_of_voters == null)
        {
            gamemodel[sessionID]?.set_of_voters = mutableListOf()
        }
        if(!gamemodel[sessionID]?.set_of_voters?.contains(player)!!) {
            gamemodel[sessionID]?.set_of_voters?.add(player)
        }
    }
    suspend fun gamemodelToMembers(sessionID: String){
        members.values.forEach{ member ->
            if(member.sessionID == sessionID)
                member.socket?.send(Frame.Text(Json.encodeToJsonElement(getgameModel(sessionID)).toString()))

        }
        kicked_members.values.forEach{ kicked_member ->
            if(kicked_member.sessionID == sessionID)
            {
                kicked_member.socket?.send(Frame.Text(Json.encodeToJsonElement(getgameModel(sessionID)).toString()))
            }
        }

    }
    fun roomisExist(sessionID: String): Boolean {return gamemodel.containsKey(sessionID)}
    suspend fun opening_attribute(params: AttributeOpening, sessionID: String): Boolean{
        var num_atr = params.attributeNumber
        var playerID = params.playerID
        var opened = false
        var trigerForLoop = true

        members.values.forEach{ member ->
            if (member.sessionID == sessionID){
                for (attribute in member.attributes) {
                    if (attribute.isExposed) {
                        trigerForLoop = false
                    }
                }
            }
        }
        members.values.forEach{ member ->
            if ((member.sessionID == sessionID)&&(member.id == playerID)){
                member.attributes[num_atr].isExposed = true
                opened = true
            }
        }

            change_turn(sessionID, isFirstLoop = trigerForLoop, for_status = false)

        members.values.forEach{ member ->
            if(member.sessionID == sessionID)
                member.socket?.send(Frame.Text(Json.encodeToJsonElement(getgameModel(sessionID)).toString()))
        }
        kicked_members.values.forEach{ kicked_member ->
            if(kicked_member.sessionID == sessionID)
            {
                kicked_member.socket?.send(Frame.Text(Json.encodeToJsonElement(getgameModel(sessionID)).toString()))
            }
        }

        return opened
    }
    suspend fun opening_conditions(params: List<GameCondition>, sessionID: String, round: Int) {
        params[round].isExposed = true

//        members.values.forEach{ member ->
//            if(member.sessionID == sessionID)
//                member.socket?.send(Frame.Text(Json.encodeToJsonElement(getgameModel(sessionID)).toString()))
//        }
    }
    fun get_conditions(): List<GameCondition> {
        var condition1 = GameCondition(Condition = (0..29).random(), isExposed = false)
        var condition2 = GameCondition(Condition = (0..29).random(), isExposed = false)
        while(condition1.Condition == condition2.Condition) {
            condition2 = GameCondition(Condition = (0..29).random(), isExposed = false)
        }
        var condition3 = GameCondition(Condition = (0..29).random(), isExposed = false)
        while ((condition3.Condition == condition2.Condition) || (condition3.Condition == condition1.Condition)) {
            condition3 = GameCondition(Condition = (0..29).random(), isExposed = false)
        }
        var condition4 = GameCondition(Condition = (0..29).random(), isExposed = false)
        while ((condition4.Condition == condition1.Condition) || (condition4.Condition == condition2.Condition) || (condition4.Condition == condition3.Condition)) {
            condition4 = GameCondition(Condition = (0..29).random(), isExposed = false)
        }
        var condition5 = GameCondition(Condition = (0..29).random(), isExposed = false)
        while ((condition5.Condition == condition1.Condition) || (condition5.Condition == condition2.Condition) || (condition5.Condition == condition3.Condition) || (condition5.Condition == condition4.Condition)) {
            condition5 = GameCondition(Condition = (0..29).random(), isExposed = false)
        }
        var gameConditions = listOf(condition1, condition2, condition3, condition4, condition5)
        return gameConditions
    }
    fun gen_attributes(sessionID: String, isfirst: Boolean): Array<Attribute> {
        var profession = Attribute(id = (0..198).random(), isExposed = false)
        var health = Attribute(id = (0..93).random(), isExposed = false)
        var biology = Attribute(id = (0..102).random(), isExposed = false)
        var hobby = Attribute(id = (0..64).random(), isExposed = false)
        var luggage = Attribute(id = (0..61).random(), isExposed = false)
        var fact = Attribute(id = (0..53).random(), isExposed = false)

        if( isfirst == false) {

            var professions:List<Attribute> = listOf()
            var healths:List<Attribute> = listOf()
            var biologys:List<Attribute> = listOf()
            var hobbys:List<Attribute> = listOf()
            var luggages:List<Attribute> = listOf()
            var facts:List<Attribute> = listOf()

            members.values.forEach { member ->
                if (member.sessionID == sessionID) {
                    professions += member.attributes[0]
                    healths += member.attributes[1]
                    biologys += member.attributes[2]
                    hobbys += member.attributes[3]
                    luggages += member.attributes[4]
                    facts += member.attributes[5]
                }
            }

            while(professions.contains(profession)) {
                profession  = Attribute(id= (0..198).random(), isExposed = false)
            }
            while(healths.contains(health)) {
                Attribute(id = (0..93).random(), isExposed = false)
            }
            while(biologys.contains(biology))
            {
                Attribute(id = (0..102).random(), isExposed = false)
            }
            while(hobbys.contains(hobby))
            {
                hobby  = Attribute(id = (0..64).random(), isExposed = false)
            }
            while(luggages.contains(luggage))
            {
                luggage  = Attribute(id = (0..61).random(), isExposed = false)
            }
            while(facts.contains(fact))
            {
                fact  = Attribute(id = (0..53).random(), isExposed = false)
            }
        }
        val res = arrayOf(profession, health, biology, hobby, luggage, fact)
        return res
    }
    fun get_set_of_votes(sessionID: String): MutableList<String>? {return gamemodel[sessionID]?.set_of_voters }
    fun check_voting(sessionID: String, players: MutableList<com.BunkerProduction.other_dataclasses.Player>, set_of_votes: MutableList<String>?): Boolean {
    var result:Boolean = false
        if (set_of_votes != null) {
            result = (players.count() == (set_of_votes.count()+1))
        }
        print("\nПроверка на проверку voting прошла $result\n")
        return result
    }

    suspend fun get_gamemodel_after_voting(sessionID: String)
    {
        change_turn(sessionID, isFirstLoop = false, for_status = true)
        if(gamemodel[sessionID]?.initialNumberOfPlayers  == 1)
        {
            change_gamestate(state = GameState.fishing, sessionID)
        }
//        members.values.forEach{ member ->
//            if(member.sessionID == sessionID)
//                member.socket?.send(Frame.Text(Json.encodeToJsonElement(getgameModel(sessionID)).toString()))
//        }
//        kicked_members.values.forEach{ kicked_member ->
//            if(kicked_member.sessionID == sessionID)
//            {
//                kicked_member.socket?.send(Frame.Text(Json.encodeToJsonElement(getgameModel(sessionID)).toString()))
//            }
//        }


    }
    suspend fun change_turn(sessionID: String, isFirstLoop: Boolean, for_status:Boolean): String? {
        print("isFirstLoop $isFirstLoop\n")
        var players = getPlayers_NONEsocket(sessionID = sessionID)
        var ids:MutableList<String> = mutableListOf()
        var MaxValue: Int = 0
        var MaxValueID: String = ""
        var memberSocket: DefaultWebSocketServerSession? = null
        var gamemodel_turn = ""
        var index_id: Int
        var playerID = hosts[sessionID]
        var indexHOST = 0
        print("ids1 $ids\n\n")

        for (player in players) {
            ids += player.id
        }

        print("ids2 $ids\n\n")
        var count_ids = ids.count()

        var m = ids[ids.count()-1]
        print("id_posledn $m\n")
        print("playerID $playerID\n")

            if(ids.indexOf(playerID)!=0) {
                indexHOST = ids.indexOf(playerID)
                var element = ids[0]
                ids[indexHOST] = element
                if (playerID != null) {
                    ids[0] = playerID
                }
            }
        gamemodel[sessionID]?.initialNumberOfPlayers  = ids.count()
        print("ids_relocated $ids\n\n")

        if(gamemodel[sessionID]?.turn  == "")
        {
            index_id = 0

        }

        if(isFirstLoop) {
            index_id = 1
            gamemodel[sessionID]?.turn =  ids[index_id]
        }
        else {
            if(gamemodel[sessionID]?.turn !="")
            index_id = ids.indexOf(gamemodel[sessionID]?.turn)
            else
                index_id = 0
        }
        print("Сейчас на терне чел по счету $index_id\n")


        if((index_id == 0) && !isFirstLoop)
        {

            if(gamemodel[sessionID]?.votes != null) {
                for (value in gamemodel[sessionID]?.votes!!) {
                    if(value.value > MaxValue)
                    {
                        MaxValue = value.value
                        MaxValueID = value.key
                    }
                }
                print("Максимальное число проголосвало за $MaxValueID голосов $MaxValue")
//                удаление игрока из модели, за которого проголосовало больше всего людей
                    members.values.forEach { member ->
                        if (member.id == MaxValueID){
                            memberSocket = member.socket as DefaultWebSocketServerSession
                            kicked_members[memberSocket.toString()] = member
                        }
                    }
                print("порядко игроков в списке $ids\n")
                var trig = 0
                members.values.forEach{ member ->
                    if(member.sessionID == sessionID)
                        member.socket?.send(Frame.Text(Json.encodeToJsonElement(getgameModel(sessionID)).toString()))
                }
                members.remove(memberSocket.toString())//удаление из хэш карты Members
                players_num_votes[sessionID]?.clear()//Очистка карт голосования
                players_pla_votes[sessionID]?.clear()
                if(ids.count() < 2)
                {
                    gamemodel[sessionID]?.gameState  = GameState.fishing
                }
                print("порядко игроков в списке $ids\n")
//                index_id = 0;
                //перерасчет
                ids = mutableListOf()
                var players = getPlayers_NONEsocket(sessionID = sessionID)
                for(player in players)
                {
                    ids+=player.id
                }
                if(ids.indexOf(playerID)!=0) {
                    indexHOST = ids.indexOf(playerID)
                    var element = ids[0]
                    ids[indexHOST] = element
                    if (playerID != null) {
                        ids[0] = playerID
                    }
                }
                if(ids.count() == 1)
                {
                    gamemodel[sessionID]?.gameState  = GameState.fishing
                }
                print("порядко игроков в списке $ids ПОСЛЕ УДАЛЕНИЯ\n")
                var kicked_message = kicked_members[memberSocket.toString()]?.id?.let { Kicked_message(type = "kickedPlayer", id = it) }
                members.values.forEach{ member ->
                    if(member.sessionID == sessionID)
                        member.socket?.send(Json.encodeToJsonElement(kicked_message).toString())
                }
                kicked_members.values.forEach{ kicked_member ->
                    if(kicked_member.sessionID == sessionID)
                    {
                        kicked_member.socket?.send(Json.encodeToJsonElement(kicked_message).toString())
                    }
                }

                print("порядко игроков в списке $ids\n")
                gamemodel[sessionID]?.set_of_voters = null
                gamemodel[sessionID]?.votes = null
                gamemodel[sessionID]?.turn = ids[index_id]
                if(ids.count() > 1) {
                    gamemodel[sessionID]?.gameState = GameState.normal
                }
                else
                {
                    gamemodel[sessionID]?.gameState = GameState.fishing
                }
                print("порядко игроков в списке $ids\n")
                Thread.sleep(2000);
                gamemodel[sessionID]?.initialNumberOfPlayers = ids.count()
                gamemodel[sessionID]?.players = getPlayers_NONEsocket(sessionID = sessionID)
                members.values.forEach{ member ->
                    if(member.sessionID == sessionID)
                        member.socket?.send(Frame.Text(Json.encodeToJsonElement(getgameModel(sessionID)).toString()))
                }
                kicked_members.forEach{ kicked ->
                    if(kicked.value.sessionID == sessionID)
                        kicked.value.socket?.send(Frame.Text(Json.encodeToJsonElement(getgameModel(sessionID)).toString()))
                }
                print("порядко игроков в списке $ids\n")


                val hosts = members.values.filter { player -> player.isCreator } //проверка на хоста в комнате

                if(hosts.isEmpty())
                {
                    members.values.forEach{ member ->
                        if((member.sessionID == sessionID)&&(trig == 0)) {
                            trig++
                            member.isCreator = true //Заменяем хоста
                        }

                    }
                }
                gamemodel[sessionID]?.initialNumberOfPlayers = ids.count()
                gamemodel[sessionID]?.players = getPlayers_NONEsocket(sessionID) // Обновление модели игры
                if (gamemodel[sessionID]?.players?.isEmpty() == true)
                {
                    gamemodel.remove(sessionID)
                }
                print("Игрок $MaxValueID удален\n")
                print("Список кикнутых\n")
                print(kicked_members)

                if(ids.count() < 2)
                {
                    change_gamestate(state = GameState.fishing, sessionID)
                }
            }
        }
        if(ids.count() < 2)
        {
            change_gamestate(state = GameState.fishing, sessionID)
        }
        if((!isFirstLoop)&&(gamemodel[sessionID]?.gameState != GameState.voting)) {
            if((index_id + 1 <= ids.count())&&(!for_status))
            index_id += 1
        }
        if(ids.count() > 1) {
            change_gamestate(state = GameState.normal, sessionID)
        }
        print("index_id $index_id\n")
        print("count_ids ${ids.count()}\n")
        print("порядко игроков в списке $ids\n")
        print(members.values)
        if(ids.count() == 1)
        {
            gamemodel[sessionID]?.gameState  = GameState.fishing
        }
        if((index_id == ids.count())&&(gamemodel[sessionID]?.gameState  != GameState.fishing))
        {
            if((gamemodel[sessionID]?.round!! >= 1)&&(gamemodel[sessionID]?.round!! <= 5)) {
                change_gamestate(state = GameState.voting, sessionID)
                print("\nTRIG\n")
            }
            else {
                if(ids.count() > 1) {
                    change_gamestate(state = GameState.normal, sessionID)
                }
                else {
                    change_gamestate(state = GameState.fishing, sessionID)
                }
            }
            index_id = 0
            gamemodel[sessionID]?.turn = ids[index_id]
            gamemodel[sessionID]?.round = gamemodel[sessionID]?.round?.plus(1)!!
            var gameConditions = gamemodel[sessionID]?.preferences?.gameConditions
            var round =  gamemodel[sessionID]?.round
            if (gameConditions != null) {
                if ((round != null) && (round <=5)){
                    opening_conditions(gameConditions, sessionID, round-1)
                }
            }

        }
        else {
            print("index_id $index_id\n")
            print("ids $ids\n")
            if(gamemodel[sessionID]?.gameState  != GameState.fishing)
            gamemodel[sessionID]?.turn = ids[index_id]
        }

        gamemodel[sessionID]?.set_of_voters = null
        gamemodel[sessionID]?.votes = null
        if(gamemodel[sessionID]?.gameState == GameState.voting)
        {
            gamemodel_turn = gamemodel[sessionID]?.turn.toString()
            gamemodel[sessionID]?.turn = ""

        }
        return gamemodel[sessionID]?.turn
    }

    fun get_waitingRoom(sessionID: String): WaitingRoom {
        var players = mutableListOf<com.BunkerProduction.other_dataclasses.Player>()
        members.values.forEach { member ->
            if (member.sessionID == sessionID)
                players += Player(
                    id = member.id,
                    username = member.username,
                    isCreator = member.isCreator,
                    attributes =  member.attributes
                )
        }
        return WaitingRoom(
            type = Type_Model.waiting_room,
            players = players,
            sessionID = sessionID
        )
    }
    fun getPlayers_NONEsocket(sessionID: String): MutableList<com.BunkerProduction.other_dataclasses.Player> {
        var players = mutableListOf<com.BunkerProduction.other_dataclasses.Player>()
        members.values.forEach{ member ->
            if(member.sessionID == sessionID) {
                players += Player(
                    id = member.id,
                    username = member.username,
                    isCreator = member.isCreator,
                    attributes = member.attributes
                )
            }
        }
        return players
    }
    fun getPlayers(sessionID: String): MutableList<Player> {
        val res = (members.values.filter { player -> player.sessionID == sessionID }).toMutableList()
        return res
    }

    fun is_exist(sessionID: String): String { // Для нового подключения
        var sessionIDmoded = sessionID
        if (sessionIDmoded == "None") {
            sessionIDmoded = GenerateRoomCode()
        }
        members.values.forEach { member ->
            while (member.sessionID != sessionID) {
                sessionIDmoded = GenerateRoomCode()
            }
        }
            return sessionIDmoded
    }


    fun onCreateGame(
        id: String,
        username: String,
        sessionID: String,
        socket: WebSocketSession,
        gameModel: GameModel
    )
    {
        members[socket.toString()] = Player(
            id = id,
            username = username,
            sessionID = sessionID,
            socket = socket,
            isCreator = true,
            attributes = gen_attributes(sessionID, isfirst = true)
        )

        gamemodel[sessionID] = GameModel(
            started = false,
            type = Type_Model.game_model,
            sessionID = sessionID,
            preferences = gameModel.preferences,
            players = getPlayers_NONEsocket(sessionID), //Берет всех играков из сессии, хотя она только что создалась (не нужно )
            gameState = gameModel.gameState,
            initialNumberOfPlayers = gameModel.initialNumberOfPlayers,
            turn = id,
            round = gameModel.round,
            votes = gameModel.votes,
            set_of_voters = gameModel.set_of_voters
        )
            set_hosts(sessionID, id)

    }
    suspend fun onJoin(
        id: String,
        username: String,
        sessionID: String,
        socket: WebSocketSession,
    ) {
        if (gamemodel[sessionID]?.started  == false) {
            var attributes = gen_attributes(sessionID, isfirst = false)
            members[socket.toString()] = Player(
                id = id,
                username = username,
                sessionID = sessionID,
                socket = socket,
                isCreator = false,
                attributes = attributes
            )
            var player_NONEsocket = Player(
                id = id,
                isCreator = false,
                username = username,
                attributes = attributes
            )

            gamemodel[sessionID]?.initialNumberOfPlayers = gamemodel[sessionID]?.initialNumberOfPlayers!! + 1
            gamemodel[sessionID]?.players?.add(player_NONEsocket)

            members.values.forEach { member ->
                if (member.sessionID == sessionID)
                    member.socket?.send(Frame.Text(Json.encodeToJsonElement(get_waitingRoom(sessionID)).toString()))
            }
            kicked_members.values.forEach{ kicked_member ->
                if(kicked_member.sessionID == sessionID)
                {
                    kicked_member.socket?.send(Frame.Text(Json.encodeToJsonElement(get_waitingRoom(sessionID)).toString()))
                }
            }
        }
        else
        {
            socket.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "GameStartedError"))
        }
    }

    suspend fun GetDataShatus(gamePreferences: GamePreferences, sessionID: String)
    {
//     print(members.values.toMutableList()) //Хэш-карта играков
//        print(gamemodel.values) //Хэш-карта игровых моделей
        members.values.forEach{ member ->
            gamemodel[sessionID]?.preferences = gamePreferences
            if(member.sessionID == sessionID)
            member.socket?.send(Frame.Text(Json.encodeToJsonElement(get_waitingRoom(sessionID)).toString()))
        }
        kicked_members.values.forEach{ kicked_member ->
            if(kicked_member.sessionID == sessionID)
            {
                kicked_member.socket?.send(Frame.Text(Json.encodeToJsonElement(get_waitingRoom(sessionID)).toString()))
            }
        }
    }

    suspend fun tryDissconect(socket: DefaultWebSocketServerSession, sessionID: String, closeReason:String){
        var trig = 0
        if(members.containsKey(socket.toString())){

            members.remove(socket.toString()) //удаление из хэш карты Members
            members[socket.toString()]?.socket?.close() // Закрываем сессию для user
        }

        val hosts = members.values.filter { player -> player.isCreator } //проверка на хоста в комнате

        if(hosts.isEmpty())
        {
            members.values.forEach{ member ->
                if((member.sessionID == sessionID)&&(trig == 0)) {
                    trig++
                    member.isCreator = true //Заменяем хоста
                }

            }
        }
        gamemodel[sessionID]?.initialNumberOfPlayers = gamemodel[sessionID]?.initialNumberOfPlayers!! - 1
        gamemodel[sessionID]?.players = getPlayers_NONEsocket(sessionID) // Обновление модели игры
        if (gamemodel[sessionID]?.players?.isEmpty() == true)
        {
            gamemodel.remove(sessionID)
        }
        if(closeReason != "GameStartedError") {
            if (gamemodel[sessionID]?.started == true) {
                gamemodelToMembers(sessionID)
            } else {
                members.values.forEach { member ->
                    if (member.sessionID == sessionID)
                        member.socket?.send(Frame.Text(Json.encodeToJsonElement(get_waitingRoom(sessionID)).toString()))
                }
            }
        }
    }
}




