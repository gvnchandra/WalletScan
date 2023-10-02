package com.android.walletscan.documents

class WalletHistInfoTemp {
    var startingBalance: Double = 0.0
    var endingBalance: Double = 0.0
    lateinit var walletHistInfo: WalletHistInfo

    constructor() {}
    constructor(startingBalance: Double, endingBalance: Double, walletHistInfo: WalletHistInfo) {
        this.startingBalance = startingBalance
        this.endingBalance = endingBalance
        this.walletHistInfo = walletHistInfo
    }

}