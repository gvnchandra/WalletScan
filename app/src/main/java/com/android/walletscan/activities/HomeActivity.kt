package com.android.walletscan.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.android.walletscan.R
import com.android.walletscan.databinding.ActivityHomeBinding
import com.android.walletscan.documents.WalletInfo
import com.android.walletscan.fragments.CreatedWalletsFragment
import com.android.walletscan.network.ConnectionManager
import com.android.walletscan.supporting.LoggedInUserDetails
import com.android.walletscan.supporting.PreferenceManager
import com.android.walletscan.supporting.WalletScanConstants
import com.android.walletscan.util.AppUtil
import com.android.walletscan.util.FirebaseUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import java.util.Date


class HomeActivity : AppCompatActivity() {

    private lateinit var fab: FloatingActionButton
    private lateinit var toolbar: Toolbar
    private lateinit var binding: ActivityHomeBinding
    private lateinit var txtShortName: TextView
    //add wallet dialog
    lateinit var tilWalletName: TextInputLayout
    lateinit var tilWalletDesc: TextInputLayout
    lateinit var tilWalletBal: TextInputLayout
    lateinit var progressBar: ProgressBar
    lateinit var dialogbtnCreate: Button
    lateinit var dialogbtnCancel: Button
    lateinit var addWalletDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toolbar = binding.toolbar
        txtShortName = binding.txtShortName
        fab = binding.fabAddAccount
        setSupportActionBar(toolbar)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        PreferenceManager.init(this)
        Log.i("uid", PreferenceManager.getUId())
        Log.i("uid log", LoggedInUserDetails.uid)

        txtShortName.text = AppUtil.findShortName(LoggedInUserDetails.name)
        val resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                txtShortName.text = AppUtil.findShortName(LoggedInUserDetails.name)
            }
        txtShortName.setOnClickListener {
            resultLauncher.launch(Intent(this, Profile::class.java))
        }
        fab.setOnClickListener {
            showAddWalletDialog()
        }
        initData()
    }

    private fun initData() {
        supportFragmentManager.beginTransaction().replace(R.id.container, CreatedWalletsFragment())
            .commit()
    }

    private fun showAddWalletDialog() {
        addWalletDialog = Dialog(this)
        val addWalletLayout = layoutInflater.inflate(R.layout.add_wallet_dialog, null, false)
        addWalletDialog.setContentView(addWalletLayout)
        addWalletDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        addWalletDialog.show()
        tilWalletName = addWalletLayout.findViewById(R.id.tilName)
        tilWalletDesc = addWalletLayout.findViewById(R.id.tilDescription)
        tilWalletBal = addWalletLayout.findViewById(R.id.tilBalance)
        progressBar = addWalletLayout.findViewById(R.id.progress_horizontal)
        dialogbtnCreate = addWalletLayout.findViewById(R.id.btnCreate)
        dialogbtnCancel = addWalletLayout.findViewById(R.id.btnCancel)
        val tieName = tilWalletName.editText
        val tieDescription = tilWalletDesc.editText
        val tieBalance = tilWalletBal.editText
        dialogbtnCreate.setOnClickListener {
            val strName = tieName?.text.toString().trim()
            val strDescription = tieDescription?.text.toString().trim()
            val strBalance = tieBalance?.text.toString().trim()
            if (strName.isEmpty()) {
                tilWalletName.error = "Please enter the name"
                return@setOnClickListener
            }
            if (strDescription.isEmpty()) {
                tilWalletDesc.error = "Please enter the description"
                return@setOnClickListener
            }
            if (strBalance.isEmpty()) {
                tilWalletBal.error = "Please enter the amount"
                return@setOnClickListener
            }
            try {
                strBalance.toDouble()
            } catch (e: NumberFormatException) {
                tilWalletBal.error = "Please enter the valid amount"
                return@setOnClickListener
            }
            if (ConnectionManager.isNetworkAvailable(this)) {
                addWalletDialog.setCancelable(false)
                putEverythingEnabled(false)
                saveWalletInfo(strName, strDescription, strBalance.toDouble())
            } else {
                Toast.makeText(this, WalletScanConstants.NO_INTERNET_CONNECTON, Toast.LENGTH_SHORT)
                    .show()
            }
        }
        dialogbtnCancel.setOnClickListener {
            addWalletDialog.dismiss()
        }
    }

    private fun saveWalletInfo(strName: String, strDescription: String, balance: Double) {
        val ref = FirebaseUtil.getWalletsReference().document()
        val walletInfo = WalletInfo(
            ref.id,
            strName,
            strDescription,
            balance,
            balance,
            LoggedInUserDetails.uid,
            LoggedInUserDetails.uid,
            Date(),
            null,
            null
        )
        ref.set(walletInfo).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                addWalletDialog.cancel()
                Toast.makeText(this, "Wallet $strName added successfully!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                putEverythingEnabled(true)
                Toast.makeText(this, WalletScanConstants.ERROR_OCCURRED_MESSAGE, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun putEverythingEnabled(b: Boolean) {
        if (b) {
            progressBar.visibility = View.INVISIBLE
            dialogbtnCreate.isEnabled = true
            dialogbtnCancel.isEnabled = true
            tilWalletDesc.isEnabled = true
            tilWalletName.isEnabled = true
            tilWalletBal.isEnabled = true
        } else {
            progressBar.visibility = View.VISIBLE
            dialogbtnCreate.isEnabled = false
            dialogbtnCancel.isEnabled = false
            tilWalletDesc.isEnabled = false
            tilWalletName.isEnabled = false
            tilWalletBal.isEnabled = false
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            ActivityCompat.finishAffinity(this@HomeActivity)
        }
    }
}