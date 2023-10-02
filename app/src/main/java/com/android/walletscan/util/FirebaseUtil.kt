package com.android.walletscan.util

import com.android.walletscan.documents.UserInfo
import com.android.walletscan.supporting.WalletScanConstants
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

object FirebaseUtil {
    private val usersRef = Firebase.firestore.collection(WalletScanConstants.USERS_REF)
    private val walletsRef = Firebase.firestore.collection(WalletScanConstants.WALLETS_REF)
    private val walletsHistRef = Firebase.firestore.collection(WalletScanConstants.WALLETS_HIST_REF)

    fun getUsersReference(): CollectionReference {
        return usersRef
    }

    fun getWalletsReference(): CollectionReference {
        return walletsRef
    }

    fun getWalletsHistReference(): CollectionReference {
        return walletsHistRef
    }

    fun getUserDocReference(id: String): DocumentReference {
        return usersRef.document(id)
    }

    suspend fun getUserInfo(id: String): UserInfo? {
        val userInfoSnapshot = usersRef.document(id).get().await()
        return if (userInfoSnapshot.exists()) {
            userInfoSnapshot.toObject<UserInfo>()
        } else {
            null
        }
    }
}