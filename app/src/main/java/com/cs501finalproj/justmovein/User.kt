package com.cs501finalproj.justmovein

class User {
    var name:String? = null
    var email:String? = null
    var uid:String? = null
    var profilePic : String? = null
    var zipcode : String? = null
    constructor(){}
    constructor(name:String,email:String,uid:String,profilePic:String,zipcode:String){
        this.name = name;
        this.email = email;
        this.uid = uid;
        this.profilePic = profilePic;
        this.zipcode = zipcode;
    }
    constructor(name:String,email:String,uid:String){
        this.name = name;
        this.email = email;
        this.uid = uid;
    }

}