package com.example.chat.models

class Chats {
    var image : String = ""
    var names : String = ""
    var keyChat : String = ""
    var uidReceiver: String = ""
    var idMessage : String = ""
    var typeMessage : String = ""
    var message : String = ""
    var transmittorUid  : String = ""
    var receiverUid : String = ""
    var time : Long = 0

    constructor()
    constructor(
        time: Long,
        receiverUid: String,
        transmittorUid: String,
        message: String,
        typeMessage: String,
        idMessage: String,
        uidReceiver: String,
        keyChat: String,
        names: String,
        image: String
    ) {
        this.time = time
        this.receiverUid = receiverUid
        this.transmittorUid = transmittorUid
        this.message = message
        this.typeMessage = typeMessage
        this.idMessage = idMessage
        this.uidReceiver = uidReceiver
        this.keyChat = keyChat
        this.names = names
        this.image = image
    }


}