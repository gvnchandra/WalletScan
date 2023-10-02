package com.android.walletscan.documents

import java.util.Date

class WalletHistInfo {
    var id: String = ""
    var walletId: String = ""
    var transactionType: String = ""
    var amtAdded: Double? = null
    var amtDeducted: Double? = null
    var purpose: String = ""
    var toRecoverOrReturn: Boolean? = null
    var isRecoveredOrReturned: Boolean? = null
    var transactionDoneBy: String = ""
    var transactionDoneOn: Date? = null
    var modifiedBy: String? = null
    var modifiedOn: Date? = null

    constructor() {}
    constructor(
        id: String,
        walletId: String,
        transactionType: String,
        amtAdded: Double?,
        amtDeducted: Double?,
        purpose: String,
        toBeRecoveredOrReturned: Boolean?,
        isRecoveredOrReturned: Boolean?,
        transactionDoneBy: String,
        transactionDoneOn: Date?,
        modifiedBy: String?,
        modifiedOn: Date?
    ) {
        this.id = id
        this.walletId = walletId
        this.transactionType = transactionType
        this.amtAdded = amtAdded
        this.amtDeducted = amtDeducted
        this.purpose = purpose
        this.toRecoverOrReturn = toBeRecoveredOrReturned
        this.isRecoveredOrReturned = isRecoveredOrReturned
        this.transactionDoneBy = transactionDoneBy
        this.transactionDoneOn = transactionDoneOn
        this.modifiedBy = modifiedBy
        this.modifiedOn = modifiedOn
    }


}