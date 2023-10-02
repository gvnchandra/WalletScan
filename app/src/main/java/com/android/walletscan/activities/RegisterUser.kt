package com.android.walletscan.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.walletscan.R
import com.android.walletscan.databinding.ActivityRegisterUserBinding
import com.android.walletscan.documents.UserInfo
import com.android.walletscan.network.ConnectionManager
import com.android.walletscan.supporting.PreferenceManager
import com.android.walletscan.supporting.WalletScanConstants
import com.android.walletscan.util.FirebaseUtil
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.CollectionReference
import java.util.*

class RegisterUser : AppCompatActivity() {
    lateinit var binding: ActivityRegisterUserBinding
    lateinit var progress: ProgressBar
    private lateinit var tilName: TextInputLayout
    private lateinit var tieName: TextInputEditText
    private lateinit var tilBio: TextInputLayout
    private lateinit var tieBio: TextInputEditText
    lateinit var btnSignUp: Button
    lateinit var fullPhoneNumber:String
    lateinit var uid: String
    lateinit var collref: CollectionReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityRegisterUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this,onBackPressedCallback)

        collref= FirebaseUtil.getUsersReference()
        progress=binding.progressHorizontal
        tilName = binding.tilName
        tieName = tilName.editText as TextInputEditText
        tilBio = binding.tilBio
        tieBio = tilBio.editText as TextInputEditText
        btnSignUp=binding.btnSignup
        fullPhoneNumber = intent.getStringExtra(WalletScanConstants.INTENT_PARAM_PHONE_NUMBER)!!
        uid=intent.getStringExtra(WalletScanConstants.INTENT_PARAM_UID)!!

        btnSignUp.setOnClickListener {
            tilName.error=null
            val stName:String=tieName.text.toString().trim()
            if (stName.isEmpty()) {
                tilName.error = WalletScanConstants.NO_NAME_ENTERED
                tilName.requestFocus()
                return@setOnClickListener
            }
            val stBio:String=tieBio.text.toString().trim()
            saveUserInfo(UserInfo(stName, stBio, fullPhoneNumber, uid, uid, Date(), null, null))
        }
    }

    private fun saveUserInfo(userInfo: UserInfo) {
        if (ConnectionManager.isNetworkAvailable(this)) {
            putEverythingEnabled(false)
            collref.document(userInfo.uid).set(userInfo)
                .addOnCompleteListener {task->
                    putEverythingEnabled(true)
                    if(task.isSuccessful){
                        PreferenceManager.set(true,userInfo.name, userInfo.bio,
                            userInfo.phoneNumber, userInfo.uid)
                        startActivity(Intent(this,HomeActivity::class.java))
                    }
                    else {
                        PreferenceManager.set(false,"","","","")
                        Toast.makeText(this, WalletScanConstants.ERROR_OCCURRED_MESSAGE, Toast.LENGTH_SHORT).show()
                        Log.e(WalletScanConstants.ACTIVITY_REGISTER, task.exception?.message.toString())
                    }
                }
        }
        else {
            Snackbar.make(btnSignUp, WalletScanConstants.NO_INTERNET_CONNECTON, Snackbar.LENGTH_SHORT).show()
        }
    }
    private fun putEverythingEnabled(enable:Boolean) {
        tilName.isEnabled = enable
        tilBio.isEnabled = enable
        btnSignUp.isEnabled =enable
        if(enable) {
            progress.visibility = View.INVISIBLE
            btnSignUp.text = getString(R.string.sign_up)
        }
        else{
            progress.visibility = View.VISIBLE
            btnSignUp.text = getString(R.string.signingup)
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            ActivityCompat.finishAffinity(this@RegisterUser)
        }
    }
}