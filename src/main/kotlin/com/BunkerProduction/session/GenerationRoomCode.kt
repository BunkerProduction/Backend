package com.BunkerProduction.session

fun GenerateRoomCode(): String {
    var sessionID = (1..999999).random()
    var count = 0
    var num = sessionID.toInt()
    var strcount = sessionID.toString()
    while (num != 0) {
        num /= 10
        ++count
    }
    for (i in 1..6-count)
    {
        strcount = "0"+strcount
    }
    return strcount
}