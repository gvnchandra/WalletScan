package com.android.walletscan.interfaces

import com.android.walletscan.documents.WalletHistInfo

interface TransactionEditDeleteListener {
    fun editTransaction(
        walletId: String,
        walletHistId: String,
        transactionType: String,
        origAmt: Double,
        updatedAmt: Double,
        updatedPurpose: String,
        toRetOrRec: Boolean
    )

    fun saveStatus(
        walletId: String,
        walletHistId: String,
    )

    fun deleteTransaction(
        walletHistInfo: WalletHistInfo
    )
}