package com.android.walletscan.documents

import java.util.*

class UserInfo {

    var uid: String = ""
    var name: String = ""
    var bio: String = ""
    var phoneNumber: String = ""
    var createdBy: String? = null
    var createdOn: Date? = null
    var modifiedBy: String? = null
    var modifiedOn: Date? = null

    constructor() {}
    constructor(
        name: String, bio: String, fullPhoneNo: String, uid: String,
        createdBy: String?, createdTime: Date?,
        modifiedBy: String?, modifiedTime: Date?
    ) {
        this.name = name
        this.phoneNumber = fullPhoneNo
        this.bio = bio
        this.uid = uid
        this.createdBy = createdBy
        this.createdOn = createdTime
        this.modifiedBy = modifiedBy
        this.modifiedOn = modifiedTime
    }
}