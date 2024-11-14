package com.example.chat.models

class Chat {
    var idMessage : String = ""
    var typeMessage : String = ""
    var message : String = ""
    var transmitterUid : String = ""
    var receiverUid : String = ""
    var time : Long = 0

    constructor()

    constructor(
        time: Long,
        receiverUid: String,
        transmitterUid: String,
        message: String,
        typeMessage: String,
        idMessage: String
    ) {
        this.time = time
        this.receiverUid = receiverUid
        this.transmitterUid = transmitterUid
        this.message = message
        this.typeMessage = typeMessage
        this.idMessage = idMessage
    }


}