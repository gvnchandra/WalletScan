package com.android.walletscan.supporting

import android.content.Context
import android.content.SharedPreferences
import com.android.walletscan.R
import com.android.walletscan.util.AppUtil

object PreferenceManager{
    private lateinit var sharedPreferences:SharedPreferences
    val LOGGED_IN="loggedIn"
    val NAME="name"
    val BIO="bio"
    val PHONE="phone"
    val UID="uid"

    fun init(context:Context){
       sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name),Context.MODE_PRIVATE)
    }
    fun set(loggedIn:Boolean, name:String, bio:String, phone:String, uId: String){
        sharedPreferences.edit().putBoolean(LOGGED_IN,loggedIn).apply()
        sharedPreferences.edit().putString(NAME,name).apply()
        sharedPreferences.edit().putString(BIO,bio).apply()
        sharedPreferences.edit().putString(PHONE,phone).apply()
        sharedPreferences.edit().putString(UID, uId).apply()
        if(loggedIn) {
            AppUtil.setLoggedInfo(name, bio, phone, uId)
        }
    }

    object Edit{
        fun putBio(bio:String){
            sharedPreferences.edit().putString(BIO,bio).apply()
            LoggedInUserDetails.bio=bio
        }
        fun putName(name:String){
            sharedPreferences.edit().putString(NAME,name).apply()
            LoggedInUserDetails.name=name
        }
    }
    fun getLoggedIn():Boolean{
        return sharedPreferences.getBoolean(LOGGED_IN,false)
    }
    fun getName():String{
       return sharedPreferences.getString(NAME, "")!!
    }
    fun getBio():String{
        return sharedPreferences.getString(BIO, "")!!
    }
    fun getPhone():String{
        return sharedPreferences.getString(PHONE, "")!!
    }
    fun getUId():String{
        return sharedPreferences.getString(UID,"")!!
    }
}