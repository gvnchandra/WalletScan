package com.android.walletscan.activities

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.android.walletscan.R
import com.android.walletscan.databinding.ActivityProfileBinding
import com.android.walletscan.network.ConnectionManager
import com.android.walletscan.supporting.LoggedInUserDetails
import com.android.walletscan.supporting.PreferenceManager
import com.android.walletscan.supporting.WalletScanConstants
import com.android.walletscan.util.AppUtil
import com.android.walletscan.util.FirebaseUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.SetOptions
import java.util.Date

class Profile : AppCompatActivity() {
    lateinit var binding: ActivityProfileBinding
    lateinit var imgBack: ImageView
    lateinit var txtShortName: TextView
    lateinit var txtName: TextView
    lateinit var txtBio: TextView
    lateinit var txtPhone: TextView
    lateinit var txtLogout:TextView
    lateinit var toolbar: Toolbar
    lateinit var edName:EditText
    lateinit var edBio:EditText
    lateinit var collref:CollectionReference
    lateinit var progressBar: ProgressBar
    var strName=""
    var strBio=""
    lateinit var sheetDialog: BottomSheetDialog
    lateinit var txtCancel:TextView
    lateinit var txtSave:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this,onBackPressedCallback)

        toolbar=binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title=""
        imgBack=binding.imgBackButton
        txtName=binding.txtName
        txtShortName=binding.txtShortName
        txtBio=binding.txtBio
        txtPhone=binding.txtPhone

        initBottomSheetDialog()
        txtLogout=binding.txtLogout
        txtLogout.setOnClickListener {
            //removeToken()
            PreferenceManager.set(false,"","","","")
            val intent=Intent(this,PhoneNumberLogin::class.java)
            intent.flags= FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        progressBar=binding.progressHorizontal
        collref= FirebaseUtil.getUsersReference()
        txtName.text= LoggedInUserDetails.name
        txtBio.text= LoggedInUserDetails.bio
        txtPhone.text = LoggedInUserDetails.phoneNumber
        txtShortName.text= AppUtil.findShortName(LoggedInUserDetails.name)
        imgBack.setOnClickListener {
            onPressedBackBtn()
        }
    }

    private fun updateProfile() {
        val view = this.currentFocus
        if (view != null) {
            val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        if(strName==LoggedInUserDetails.name && strBio==LoggedInUserDetails.bio){
            txtName.text=strName
            txtBio.text=strBio
            sheetDialog.dismiss()
        }
        else{
            if(ConnectionManager.isNetworkAvailable(this)) {
                val updateData = hashMapOf(
                    WalletScanConstants.USER_INFO_NAME_FIELD to strName,
                    WalletScanConstants.USER_INFO_BIO_FIELD to strBio,
                    WalletScanConstants.USER_INFO_MODIFIED_BY_FIELD to LoggedInUserDetails.uid,
                    WalletScanConstants.USER_INFO_MODIFIED_ON_FIELD to Date()
                )
                setEnabled(false)
                collref.document(LoggedInUserDetails.uid).set(updateData, SetOptions.merge())
                    .addOnCompleteListener {
                        setEnabled(true)
                        sheetDialog.dismiss()
                        if(it.isSuccessful){
                            PreferenceManager.Edit.putName(strName)
                            PreferenceManager.Edit.putBio(strBio)
                            txtShortName.text=AppUtil.findShortName(strName)
                            txtBio.text=strBio
                            txtName.text=strName
                        }
                        else{
                            txtName.text=LoggedInUserDetails.name
                            txtBio.text=LoggedInUserDetails.bio
                            Toast.makeText(this,WalletScanConstants.FAILED_PROFILE_UPDATE_MSG,
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            else {
                Toast.makeText(this, WalletScanConstants.NO_INTERNET_CONNECTON, Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun onPressedBackBtn() {
        if(progressBar.visibility== View.INVISIBLE){
            setResult(1,null)
            finish()
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onPressedBackBtn()
        }
    }

    private fun setEnabled(b:Boolean){
        txtSave.isEnabled=b
        txtCancel.isEnabled=b
        edName.isEnabled=b
        if(b)
            progressBar.visibility= View.INVISIBLE
        else
            progressBar.visibility= View.VISIBLE
    }

    private fun initBottomSheetDialog() {
        sheetDialog= BottomSheetDialog(this, R.style.BottomSheetDialog)
        val bottomSheet=layoutInflater.inflate(R.layout.profile_updation_dialog,null)
        edName=bottomSheet.findViewById(R.id.edName)
        edBio = bottomSheet.findViewById(R.id.edBio)
        txtSave=bottomSheet.findViewById(R.id.txtSave)
        txtCancel=bottomSheet.findViewById(R.id.txtCancel)
        sheetDialog.setContentView(bottomSheet)
        edName.setText(LoggedInUserDetails.name)
        edBio.setText(LoggedInUserDetails.bio)

        txtSave.setOnClickListener {
            strName=edName.text.toString().trim()
            strBio = edBio.text.toString().trim()
            when {
                strName.isEmpty() -> {
                    Toast.makeText(this,WalletScanConstants.NO_NAME_ENTERED,Toast.LENGTH_SHORT).show()
                    edName.requestFocus()
                }
                else -> updateProfile()
            }
        }
        txtCancel.setOnClickListener {
            sheetDialog.dismiss()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_profile_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.itmEdit -> {
                if(!sheetDialog.isShowing)
                    sheetDialog.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}