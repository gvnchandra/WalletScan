package com.android.walletscan.util

import com.android.walletscan.supporting.LoggedInUserDetails
import java.util.Locale

object AppUtil {
    fun setLoggedInfo(name:String, bio:String, phoneNumber:String, uid:String){
        LoggedInUserDetails.name=name
        LoggedInUserDetails.bio=bio
        LoggedInUserDetails.phoneNumber=phoneNumber
        LoggedInUserDetails.uid=uid
    }

    fun findShortName(strName:String?):String{
        return if(!strName.isNullOrBlank()) {
            val strList = strName.split(" ")
            val strShortName: String = if (strList.size == 1)
                strList[0][0].toString()
            else
                strList[0][0].toString() + strList[strList.size - 1][0]
                    .toString()
            strShortName.uppercase(Locale.ENGLISH)
        } else{
            ""
        }
    }
}