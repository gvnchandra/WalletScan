package com.android.walletscan.supporting

import java.text.DecimalFormat

object WalletScanConstants {
    const val ACTIVITY_REGISTER = "RegisterUser"
    const val ACTIVITY_VERIFY_OTP = "VerifyOTP"

    val decFormat = DecimalFormat("0.00")

    const val EMPTY_VALUE = ""
    const val NO_INTERNET_CONNECTON = "Please check your network connection and try again."

    //PhoneNumberLogin
    const val NO_PHONE_NUMBER_ENTERED = "Please enter the phone number"
    const val NO_WALLET_FOUND = "Couldn't find the wallet data"
    const val INVALID_PHONE_NO = "Invalid phone number"

    //VerifyOTP
    const val INVALID_CREDENTIALS = "Invalid Credentials"
    const val ERROR_OCCURRED_MESSAGE = "Something went wrong. Please try again after sometime."

    //Register
    const val NO_NAME_ENTERED = "Please enter the name"

    //Profile
    const val USER_INFO_NAME_FIELD = "name"
    const val USER_INFO_BIO_FIELD = "bio"
    const val USER_INFO_MODIFIED_BY_FIELD = "modifiedBy"
    const val USER_INFO_MODIFIED_ON_FIELD = "modifiedOn"
    const val FAILED_PROFILE_UPDATE_MSG = "Failed to update profile. Please try again later."

    //Wallet fields
    const val WALLET_NAME_FIELD = "name"
    const val WALLET_DESCRIPTION_FIELD = "description"
    const val TRANSACTION_TYPE_ADD = "ADD"
    const val TRANSACTION_TYPE_DEDUCT = "DEDUCT"

    //intent params
    const val INTENT_PARAM_PHONE_NUMBER = "fullPhoneNumber"
    const val INTENT_PARAM_UID = "uid"
    const val INTENT_PARAM_WALLET_INFO = "walletInfo"
    const val INTENT_PARAM_WALLET_ID = "walletId"

    //Firestore
    const val USERS_REF = "users"
    const val WALLETS_REF = "wallets"
    const val WALLETS_HIST_REF = "wallets_transaction_history"
}