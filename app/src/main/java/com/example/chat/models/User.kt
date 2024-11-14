package com.example.chat.models

class User {
    var uid : String = ""
    var email : String = ""
    var username : String = ""
    var imgProfile : String = ""

    constructor()

    constructor(uid: String, email: String, username: String, imgProfile: String) {
        this.uid = uid
        this.email = email
        this.username = username
        this.imgProfile = imgProfile
    }


}