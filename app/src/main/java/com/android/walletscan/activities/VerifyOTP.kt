package com.android.walletscan.activities

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.android.walletscan.R
import com.android.walletscan.databinding.ActivityVerifyOtpBinding
import com.android.walletscan.documents.UserInfo
import com.android.walletscan.supporting.PreferenceManager
import com.android.walletscan.supporting.WalletScanConstants
import com.android.walletscan.util.FirebaseUtil
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.toObject
import java.util.concurrent.TimeUnit

class VerifyOTP : AppCompatActivity() {
    lateinit var binding: ActivityVerifyOtpBinding
    lateinit var progress: ProgressBar
    lateinit var mAuth: FirebaseAuth
    lateinit var collref: CollectionReference
    lateinit var txtEnter: TextView
    lateinit var txtVerify: TextView
    lateinit var tilCode: TextInputLayout
    lateinit var tieCode: TextInputEditText
    lateinit var txtResend: TextView
    lateinit var txtTimer: TextView
    var verificationCode: String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        mAuth = FirebaseAuth.getInstance()
        collref = FirebaseUtil.getUsersReference()

        tilCode = binding.tilCode
        tieCode = tilCode.editText as TextInputEditText
        txtEnter = binding.txtEnter
        txtVerify = binding.txtVerify
        txtResend = binding.txtResend
        txtTimer = binding.txtTimer
        progress = binding.progressHorizontal
        putEverythingEnabled(false)

        val fullPhoneNumber = intent.getStringExtra(WalletScanConstants.INTENT_PARAM_PHONE_NUMBER)!!
        txtEnter.append(" $fullPhoneNumber")
        txtVerify.append(" $fullPhoneNumber")

        sendVerificationCode(fullPhoneNumber)
        tieCode.addTextChangedListener {
            val code = tieCode.text.toString().trim()
            if (code.length == 6) {
                putEverythingEnabled(false)
                verifyVerificationCode(code)
            }
        }
        txtResend.setOnClickListener {
            putEverythingEnabled(false)
            sendVerificationCode(fullPhoneNumber)
        }
        txtTimer.addTextChangedListener {
            val time = txtTimer.text.toString().trim()
            if (time == "0")
                resendEnable(true)
            else
                resendEnable(false)
        }
    }

    private fun resendEnable(b: Boolean) {
        txtResend.isEnabled = b
        if (b) {
            txtResend.setTextColor(ContextCompat.getColor(this, R.color.primary_color))
        } else {
            txtResend.setTextColor(ContextCompat.getColor(this, R.color.greyColor))
        }
    }

    fun startTimeCounter() {
        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                txtTimer.text = (millisUntilFinished / 1000).toInt().toString()
            }

            override fun onFinish() {
                txtResend.isEnabled = true
            }
        }.start()
    }

    private fun sendVerificationCode(fullPhoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(fullPhoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                val code = credential.smsCode
                if (code != null) {
                    tieCode.setText(code)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                putEverythingEnabled(true)
                Log.e(WalletScanConstants.ACTIVITY_VERIFY_OTP, e.message.toString())
                if (e is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(
                        this@VerifyOTP,
                        WalletScanConstants.INVALID_CREDENTIALS,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@VerifyOTP,
                        WalletScanConstants.ERROR_OCCURRED_MESSAGE,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(verificationId, token)
                verificationCode = verificationId
                putEverythingEnabled(true)
                startTimeCounter()
            }
        }


    private fun verifyVerificationCode(code: String) {
        if (verificationCode!=null)
            signInWithPhoneAuthCredential(PhoneAuthProvider.getCredential(verificationCode!!, code))
        else{
            putEverythingEnabled(true)
            Toast.makeText(this, WalletScanConstants.ERROR_OCCURRED_MESSAGE, Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    checkUserCreated(mAuth.currentUser!!.uid, mAuth.currentUser!!.phoneNumber!!)
                } else {
                    putEverythingEnabled(true)
                    txtTimer.text = WalletScanConstants.EMPTY_VALUE
                    Log.e(
                        WalletScanConstants.ACTIVITY_VERIFY_OTP,
                        task.exception?.message.toString()
                    )
                    Toast.makeText(
                        this,
                        WalletScanConstants.ERROR_OCCURRED_MESSAGE,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun checkUserCreated(uid: String, fullPhoneNo: String) {
        collref.document(uid)
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val userInfo = snapshot.toObject<UserInfo>()
                    PreferenceManager.set(
                        true, userInfo!!.name, userInfo.bio,
                        userInfo.phoneNumber, userInfo.uid
                    )
                    tieCode.text = null
                    startActivity(Intent(this, HomeActivity::class.java))
                } else {
                    putEverythingEnabled(true)
                    val intent = Intent(this@VerifyOTP, RegisterUser::class.java)
                    intent.putExtra(WalletScanConstants.INTENT_PARAM_UID, uid)
                    intent.putExtra(WalletScanConstants.INTENT_PARAM_PHONE_NUMBER, fullPhoneNo)
                    startActivity(intent)
                }
            }
            .addOnFailureListener {
                putEverythingEnabled(true)
                Toast.makeText(this, WalletScanConstants.ERROR_OCCURRED_MESSAGE, Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun putEverythingEnabled(b: Boolean) {
        tilCode.isEnabled = b
        txtResend.isEnabled = b
        if (b) {
            progress.visibility = View.INVISIBLE
        } else {
            progress.visibility = View.VISIBLE
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val timerText = txtTimer.text.toString().trim()
            if (progress.visibility == View.INVISIBLE && (timerText == "0" || timerText.isBlank())) {
                finish()
            }
        }
    }
}