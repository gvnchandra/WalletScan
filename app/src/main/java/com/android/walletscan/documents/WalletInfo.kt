package com.android.walletscan.documents

import java.io.Serializable
import java.util.Date

class WalletInfo : Serializable {
    var id: String = ""
    var name: String = ""
    var description: String = ""
    var openingBalance: Double = 0.0
    var currentBalance: Double = 0.0
    var ownerId: String = ""
    var createdBy: String = ""
    var createdOn: Date? = null
    var modifiedBy: String? = null
    var modifiedOn: Date? = null

    constructor() {}
    constructor(
        id: String,
        name: String,
        description: String,
        openingBalance: Double,
        currentBalance: Double,
        ownerId: String,
        createdBy: String,
        createdOn: Date?,
        modifiedBy: String?,
        modifiedOn: Date?
    ) {
        this.id = id
        this.name = name
        this.description = description
        this.openingBalance = openingBalance
        this.currentBalance = currentBalance
        this.ownerId = ownerId
        this.createdBy = createdBy
        this.createdOn = createdOn
        this.modifiedBy = modifiedBy
        this.modifiedOn = modifiedOn
    }


}