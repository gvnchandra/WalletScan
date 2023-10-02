package com.android.walletscan.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.walletscan.databinding.ActivityPhoneNumberLoginBinding
import com.android.walletscan.network.ConnectionManager
import com.android.walletscan.supporting.WalletScanConstants
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.rilixtech.widget.countrycodepicker.CountryCodePicker

class PhoneNumberLogin : AppCompatActivity() {
    private lateinit var binding: ActivityPhoneNumberLoginBinding
    private lateinit var tilPhone: TextInputLayout
    private lateinit var tiePhone: TextInputEditText
    private lateinit var btnSend: Button
    private lateinit var picker: CountryCodePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneNumberLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this,onBackPressedCallback)

        tilPhone = binding.tilPhone
        tiePhone = tilPhone.editText as TextInputEditText
        picker = binding.ccp
        btnSend = binding.btnSend
        picker.registerPhoneNumberTextView(tiePhone)
        picker.enablePhoneAutoFormatter(true)
        btnSend.setOnClickListener {
            onBtnSendClicked()
        }
    }

    private fun onBtnSendClicked() {
        tilPhone.error = null
        val stPhone = tiePhone.text.toString().trim()
        if (TextUtils.isEmpty(stPhone)) {
            tilPhone.error = WalletScanConstants.NO_PHONE_NUMBER_ENTERED
            tilPhone.requestFocus()
            return
        }
        if (!picker.isValid) {
            tilPhone.error = WalletScanConstants.INVALID_PHONE_NO
            tilPhone.requestFocus()
            return
        }
        if (ConnectionManager.isNetworkAvailable(this)) {
            val intent = Intent(this, VerifyOTP::class.java)
            Log.i("countryCode", "${picker.phoneNumber.countryCode}")
            Log.i("nationalPhoneNo", "${picker.phoneNumber.nationalNumber}")
            intent.putExtra(WalletScanConstants.INTENT_PARAM_PHONE_NUMBER, "+${picker.phoneNumber.countryCode}" +
                    "${picker.phoneNumber.nationalNumber}")
            startActivity(intent)
        }
        else {
            Snackbar.make(btnSend, WalletScanConstants.NO_INTERNET_CONNECTON, Snackbar.LENGTH_SHORT).show()
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            ActivityCompat.finishAffinity(this@PhoneNumberLogin)
        }
    }
}