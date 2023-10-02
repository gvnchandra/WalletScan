package com.android.walletscan.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.airbnb.lottie.LottieAnimationView
import com.android.walletscan.R
import com.android.walletscan.adapters.WalletHistoryAdapter
import com.android.walletscan.databinding.ActivityWalletHistoryBinding
import com.android.walletscan.documents.WalletHistInfo
import com.android.walletscan.documents.WalletInfo
import com.android.walletscan.interfaces.TransactionEditDeleteListener
import com.android.walletscan.network.ConnectionManager
import com.android.walletscan.supporting.LoggedInUserDetails
import com.android.walletscan.supporting.WalletScanConstants
import com.android.walletscan.util.FirebaseUtil
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale
import java.util.Objects

class WalletHistoryActivity : AppCompatActivity(), TransactionEditDeleteListener {
    lateinit var binding: ActivityWalletHistoryBinding
    private lateinit var toolbar: Toolbar
    private lateinit var txtWalletName: TextView

    lateinit var rvHist: RecyclerView
    var walletHistAdapter: WalletHistoryAdapter? = null
    var walletHistInfoList= mutableListOf<WalletHistInfo>()
    lateinit var walletInfo:WalletInfo

    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var txtNoRecords: TextView
    lateinit var noDataAnim: LottieAnimationView
    lateinit var noInternetAnim: LottieAnimationView
    lateinit var loadingAnim: LottieAnimationView

    lateinit var tilAmount: TextInputLayout
    lateinit var tilPurpose: TextInputLayout
    lateinit var tilDate: TextInputLayout
    lateinit var tilTime: TextInputLayout
    lateinit var tieDate: EditText
    lateinit var tieTime: EditText
    lateinit var txtToRetOrRec: TextView
    lateinit var swchDateTime: SwitchCompat
    lateinit var swchToRetOrRec: SwitchCompat
    lateinit var progressBar: ProgressBar
    lateinit var dialogbtnCreate: Button
    lateinit var dialogbtnCancel: Button
    lateinit var addTransDialog: Dialog

    //filter dialog
    lateinit var btnApply: Button
    lateinit var rgFilter: RadioGroup
    lateinit var rbNoFilter: RadioButton
    lateinit var rbNotRec: RadioButton
    lateinit var rbNotRet: RadioButton

    // Settings Dialog
    lateinit var tilName: TextInputLayout
    lateinit var tilDescription: TextInputLayout
    lateinit var progressBarSettings: ProgressBar
    lateinit var btnSave: Button
    lateinit var btnCancel: Button
    lateinit var txtBalance: TextView
    lateinit var settingsDialog: Dialog
    lateinit var lnDelete: LinearLayout

    var strDateFormat = "yyyy-MM-dd"
    var strTimeFormat = "HH:mm"
    var strDate = ""
    var strTime = ""
    var FILTER_TYPE="Clear Filter"

    var dateComparator = Comparator<WalletHistInfo> { one, two ->
        two.transactionDoneOn!!.compareTo(one.transactionDoneOn)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toolbar = binding.toolbar
        txtWalletName = binding.txtWalletName
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        txtNoRecords = binding.txtNoRecords
        noDataAnim = binding.noDataAnim
        loadingAnim = binding.loadingAnim
        noInternetAnim = binding.noInternetAnim
        swipeRefreshLayout = binding.swipeRefreshLayout
        rvHist = binding.rvHist

        if (intent != null) {
            walletInfo = intent.getSerializableExtra(WalletScanConstants.INTENT_PARAM_WALLET_INFO)
                    as WalletInfo
            txtWalletName.text=walletInfo.name
        }
        initData()
    }

    private fun initData() {
        rvHist.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvHist.setHasFixedSize(true)
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                this,
                R.color.white
            )
        )
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.primary_color))
        swipeRefreshLayout.setOnRefreshListener {
            retrieveHistory(walletInfo.id)
        }
        retrieveHistory(walletInfo.id)
    }

    private fun retrieveHistory(walletId: String) {
        swipeRefreshLayout.isRefreshing = false
        if (ConnectionManager.isNetworkAvailable(this)) {
            setVisible(1)
            FirebaseUtil.getWalletsHistReference().whereEqualTo("walletId", walletId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.size() != 0) {
                        walletHistInfoList = mutableListOf()
                        snapshot.documents.forEach {
                            if (it.exists()) {
                                val walletInfo = it.toObject<WalletHistInfo>()!!
                                walletHistInfoList.add(walletInfo)
                            }
                        }
                        Collections.sort(walletHistInfoList, dateComparator)
                        walletHistAdapter = WalletHistoryAdapter(this, walletHistInfoList, this@WalletHistoryActivity)
                        rvHist.adapter = walletHistAdapter
                        setVisible(2)
                    } else
                        setVisible(3)
                }
        } else
            setVisible(4)
    }


    private fun setVisible(case: Int) {
        when (case) {
            1 -> {
                rvHist.visibility = View.GONE
                noDataAnim.visibility = View.GONE
                noInternetAnim.visibility = View.GONE
                loadingAnim.visibility = View.VISIBLE
                txtNoRecords.visibility = View.GONE
            }

            2 -> {
                loadingAnim.visibility = View.GONE
                noDataAnim.visibility = View.GONE
                noInternetAnim.visibility = View.GONE
                txtNoRecords.visibility = View.GONE
                rvHist.visibility = View.VISIBLE
            }

            3 -> {
                loadingAnim.visibility = View.GONE
                noInternetAnim.visibility = View.GONE
                noDataAnim.visibility = View.VISIBLE
                txtNoRecords.visibility = View.VISIBLE
                rvHist.visibility = View.GONE
            }

            4 -> {
                loadingAnim.visibility = View.GONE
                noDataAnim.visibility = View.GONE
                noInternetAnim.visibility = View.VISIBLE
                txtNoRecords.visibility = View.GONE
                rvHist.visibility = View.GONE
            }
        }
    }

    private fun showAddTransactionDialog(transactionType: String) {
        addTransDialog = Dialog(this)
        val addWalletLayout = layoutInflater.inflate(R.layout.add_transaction_dialog, null, false)
        addTransDialog.setContentView(addWalletLayout)
        addTransDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        addTransDialog.show()
        tilAmount = addWalletLayout.findViewById(R.id.tilAmount)
        tilPurpose = addWalletLayout.findViewById(R.id.tilPurpose)
        tilDate = addWalletLayout.findViewById(R.id.tilDate)
        tilTime = addWalletLayout.findViewById(R.id.tilTime)
        txtToRetOrRec = addWalletLayout.findViewById(R.id.txtToRetOrRec)
        swchToRetOrRec = addWalletLayout.findViewById(R.id.schToRetOrRec)
        swchDateTime = addWalletLayout.findViewById(R.id.schDateTime)
        progressBar = addWalletLayout.findViewById(R.id.progress_horizontal)
        dialogbtnCreate = addWalletLayout.findViewById(R.id.btnSave)
        dialogbtnCancel = addWalletLayout.findViewById(R.id.btnCancel)
        val tieAmount = tilAmount.editText
        tieDate = tilDate.editText!!
        tieTime = tilTime.editText!!
        if (transactionType == WalletScanConstants.TRANSACTION_TYPE_ADD) {
            tieAmount?.hint = "Amount to be added"
            txtToRetOrRec.text="Money to be returned?"
        } else {
            tieAmount?.hint = "Amount to be deducted"
            txtToRetOrRec.text="Money to be recovered?"
        }
        val tiePurpose = tilPurpose.editText
        swchDateTime.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                tilDate.visibility = View.GONE
                tilTime.visibility = View.GONE
            } else {
                tilDate.visibility = View.VISIBLE
                tilTime.visibility = View.VISIBLE
            }
        }
        tieDate.setOnClickListener {
            showDatePicker()
        }
        tieTime.setOnClickListener {
            showTimePicker()
        }
        dialogbtnCreate.setOnClickListener {
            val strPurpose = tiePurpose?.text.toString().trim()
            val strAmount = tieAmount?.text.toString().trim()
            if (strAmount.isEmpty()) {
                tilAmount.error = "Please enter the amount"
                return@setOnClickListener
            }
            try {
                strAmount.toDouble()
            } catch (e: NumberFormatException) {
                tilAmount.error = "Please enter the valid amount"
                return@setOnClickListener
            }
            val date = getDateObj()
            if (Objects.isNull(date)) {
                Toast.makeText(this, "Invalid date or time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (ConnectionManager.isNetworkAvailable(this)) {
                addTransDialog.setCancelable(false)
                putEverythingEnabled(false)
                saveTransaction(transactionType, strPurpose, strAmount.toDouble(), swchToRetOrRec.isChecked, date!!)
            } else {
                Toast.makeText(this, WalletScanConstants.NO_INTERNET_CONNECTON, Toast.LENGTH_SHORT)
                    .show()
            }
        }
        dialogbtnCancel.setOnClickListener {
            addTransDialog.dismiss()
        }
    }

    private fun getDateObj(): Date? {
        var date = Date()
        if (!swchDateTime.isChecked) {
            val sdf = SimpleDateFormat("$strDateFormat $strTimeFormat", Locale.getDefault())
            try {
                date = sdf.parse("$strDate $strTime")!!
            } catch (e: Exception) {
                Log.e("getDateObj", "${e.message}")
                return null
            }
        }
        return date
    }

    private fun showTimePicker() {
        val timePicker = MaterialTimePicker
            .Builder()
            .setTitleText("Select time")
            .build()
        timePicker.show(supportFragmentManager, "TIME_PICKER")
        timePicker.addOnPositiveButtonClickListener {
            strTime = timePicker.hour.toString() + ":" + timePicker.minute.toString()
            tieTime.setText(strTime)
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker
            .Builder
            .datePicker()
            .setTitleText("Select date")
            .build()
        datePicker.show(supportFragmentManager, "DATE_PICKER")
        datePicker.addOnPositiveButtonClickListener {
            strDate = SimpleDateFormat(strDateFormat, Locale.getDefault()).format(it)
            tieDate.setText(strDate)
        }
    }

    private fun saveTransaction(
        transactionType: String,
        strPurpose: String,
        amount: Double,
        toRetOrRec: Boolean,
        date: Date
    ) {
        var amtAdd: Double? = null
        var amtDeduct: Double? = null
        val message: String

        val walletRef = FirebaseUtil.getWalletsReference().document(walletInfo.id)
        val updates = hashMapOf<String, Any>(
            WalletScanConstants.USER_INFO_MODIFIED_BY_FIELD to LoggedInUserDetails.uid,
            WalletScanConstants.USER_INFO_MODIFIED_ON_FIELD to Date()
        )
        if (transactionType == WalletScanConstants.TRANSACTION_TYPE_ADD) {
            amtAdd = amount
            updates["currentBalance"] = walletInfo.currentBalance + amount
            walletRef.set(updates, SetOptions.merge())
            message = "Amount Rs.$amount/- added"
        } else {
            amtDeduct = amount
            updates["currentBalance"] = walletInfo.currentBalance - amount
            walletRef.set(updates, SetOptions.merge())
            message = "Amount Rs.$amount/- deducted"
        }
        val ref = FirebaseUtil.getWalletsHistReference().document()
        val walletHistInfo = WalletHistInfo(
            ref.id,
            walletInfo.id,
            transactionType,
            amtAdd,
            amtDeduct,
            strPurpose,
            toRetOrRec,
            if(toRetOrRec) false else null,
            LoggedInUserDetails.uid,
            date,
            null,
            null
        )
        ref.set(walletHistInfo).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                putEverythingEnabled(true)
                Log.e("saveTransaction()", "${task.exception?.message}")
                Toast.makeText(this, WalletScanConstants.ERROR_OCCURRED_MESSAGE, Toast.LENGTH_SHORT)
                    .show()
            } else {
                addTransDialog.cancel()
                Toast.makeText(this, message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun putEverythingEnabled(b: Boolean) {
        if (b) {
            progressBar.visibility = View.INVISIBLE
            dialogbtnCreate.isEnabled = true
            dialogbtnCancel.isEnabled = true
            tilAmount.isEnabled = true
            tilPurpose.isEnabled = true
        } else {
            progressBar.visibility = View.VISIBLE
            dialogbtnCreate.isEnabled = false
            dialogbtnCancel.isEnabled = false
            tilAmount.isEnabled = false
            tilPurpose.isEnabled = false
        }
    }

    private fun putEverythingEnabledSettingsDialog(b: Boolean) {
        if (b) {
            progressBarSettings.visibility = View.INVISIBLE
            btnSave.isEnabled = true
            btnCancel.isEnabled = true
            tilName.isEnabled = true
            tilDescription.isEnabled = true
            lnDelete.isEnabled = true
        } else {
            progressBarSettings.visibility = View.VISIBLE
            btnSave.isEnabled = false
            btnCancel.isEnabled = false
            tilName.isEnabled = false
            tilDescription.isEnabled = false
            lnDelete.isEnabled = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.wallet_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }

            R.id.itmFilter -> {
                showFilterDialog()
            }

            R.id.itmDetailHist -> {
                val intent = Intent(this, WalletDetailHistoryActivity::class.java)
                intent.putExtra(WalletScanConstants.INTENT_PARAM_WALLET_INFO, walletInfo)
                startActivity(intent)
            }

            R.id.itmSettings -> {
                showSettingsDialog()
            }

            R.id.itmAdd -> {
                showAddTransactionDialog(WalletScanConstants.TRANSACTION_TYPE_ADD)
            }

            R.id.itmDeduct -> {
                showAddTransactionDialog(WalletScanConstants.TRANSACTION_TYPE_DEDUCT)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showFilterDialog() {
        if (walletHistInfoList.isEmpty())
            return
        val filterDialog = Dialog(this)
        val filterLayout = layoutInflater.inflate(R.layout.filter_dialog, null, false)
        filterDialog.setContentView(filterLayout)
        filterDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        filterDialog.show()
        btnApply = filterLayout.findViewById(R.id.btnApply)
        rgFilter = filterLayout.findViewById(R.id.rgFilter)
        rbNoFilter = filterLayout.findViewById(R.id.rbNoFilter)
        rbNotRec = filterLayout.findViewById(R.id.rbNotRec)
        rbNotRet = filterLayout.findViewById(R.id.rbNotRet)

        var tobeRec=0
        var tobeRet=0
        walletHistInfoList.forEach {
            if (it.toRecoverOrReturn==true){
                if (it.transactionType==WalletScanConstants.TRANSACTION_TYPE_ADD){
                    tobeRet++;
                }
                else if (it.transactionType==WalletScanConstants.TRANSACTION_TYPE_DEDUCT){
                    tobeRec++;
                }
            }
        }
        rbNotRet.text=getString(R.string.not_returned)+" ($tobeRet)"
        rbNotRec.text=getString(R.string.not_recovered)+" ($tobeRec)"

        btnApply.setOnClickListener {
            val selectedId = rgFilter.checkedRadioButtonId
            when (selectedId) {
                -1, R.id.rbNoFilter -> {
                    FILTER_TYPE=getString(R.string.clear_filter)
                }
                R.id.rbNotRec -> {
                    FILTER_TYPE=getString(R.string.not_recovered)
                }
                R.id.rbNotRet -> {
                    FILTER_TYPE=getString(R.string.not_returned)
                }
            }
            walletHistAdapter?.filter?.filter(FILTER_TYPE)
            filterDialog.dismiss()
        }
    }

    private fun showSettingsDialog() {
        settingsDialog = Dialog(this)
        val settingsLayout = layoutInflater.inflate(R.layout.wallet_settings_dialog, null, false)
        settingsDialog.setContentView(settingsLayout)
        settingsDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        settingsDialog.show()
        tilName = settingsLayout.findViewById(R.id.tilWalletName)
        tilDescription = settingsLayout.findViewById(R.id.tilDescription)
        txtBalance = settingsLayout.findViewById(R.id.txtBalance)
        progressBarSettings = settingsLayout.findViewById(R.id.progress)
        btnSave = settingsLayout.findViewById(R.id.btnSave)
        btnCancel = settingsLayout.findViewById(R.id.btnCancel)
        lnDelete = settingsLayout.findViewById(R.id.lnDelete)

        lnDelete.setOnClickListener {
            if (ConnectionManager.isNetworkAvailable(this)) {
                settingsDialog.setCancelable(false)
                putEverythingEnabledSettingsDialog(false)
                deleteWalletInfo(walletInfo.id)
            } else {
                Toast.makeText(this, WalletScanConstants.NO_INTERNET_CONNECTON, Toast.LENGTH_SHORT)
                    .show()
            }
        }
        val tieName = tilName.editText
        val tieDescription = tilDescription.editText
        txtBalance.text =
            "Current balance: Rs.${WalletScanConstants.decFormat.format(walletInfo.currentBalance)}/-"
        tieName?.setText(walletInfo.name)
        tieDescription?.setText(walletInfo.description)

        btnSave.setOnClickListener {
            val strName = tieName?.text.toString().trim()
            val strDescription = tieDescription?.text.toString().trim()
            if (strName.isEmpty()) {
                tilName.error = "Please enter the name"
                return@setOnClickListener
            }
            if (strDescription.isEmpty()) {
                tilDescription.error = "Please enter the description"
                return@setOnClickListener
            }
            if (ConnectionManager.isNetworkAvailable(this)) {
                settingsDialog.setCancelable(false)
                putEverythingEnabledSettingsDialog(false)
                saveUpdateWalletInfo(walletInfo.id, strName, strDescription)
            } else {
                Toast.makeText(this, WalletScanConstants.NO_INTERNET_CONNECTON, Toast.LENGTH_SHORT)
                    .show()
            }
        }
        btnCancel.setOnClickListener {
            settingsDialog.dismiss()
        }
    }

    private fun deleteWalletInfo(walletId: String) {
        FirebaseUtil.getWalletsReference().document(walletId).delete()
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    putEverythingEnabledSettingsDialog(true)
                    Log.e("deleteWalletInfo()", "${task.exception?.message}")
                    Toast.makeText(
                        this,
                        WalletScanConstants.ERROR_OCCURRED_MESSAGE,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    FirebaseUtil.getWalletsHistReference().whereEqualTo("walletId", walletId).get()
                        .addOnCompleteListener {
                            if (!it.isSuccessful) {
                                putEverythingEnabledSettingsDialog(true)
                                Log.e("deleteWalletInfo:Hist", "${it.exception?.message}")
                                Toast.makeText(
                                    this,
                                    WalletScanConstants.ERROR_OCCURRED_MESSAGE,
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            } else {
                                it.result.documents.forEach { doc ->
                                    FirebaseUtil.getWalletsHistReference().document(doc.id).delete()
                                }
                                settingsDialog.cancel()
                                Toast.makeText(this, "Wallet deleted!", Toast.LENGTH_SHORT)
                                    .show()
                                finish()
                            }
                        }
                }
            }
    }

    private fun saveUpdateWalletInfo(walletId: String, strWalletName: String, strDesc: String) {
        val updateData = hashMapOf(
            WalletScanConstants.WALLET_NAME_FIELD to strWalletName,
            WalletScanConstants.WALLET_DESCRIPTION_FIELD to strDesc,
            WalletScanConstants.USER_INFO_MODIFIED_BY_FIELD to LoggedInUserDetails.uid,
            WalletScanConstants.USER_INFO_MODIFIED_ON_FIELD to Date()
        )
        FirebaseUtil.getWalletsReference().document(walletId).set(updateData, SetOptions.merge())
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    putEverythingEnabledSettingsDialog(true)
                    Log.e("saveUpdateWalletInfo()", "${task.exception?.message}")
                    Toast.makeText(
                        this,
                        WalletScanConstants.ERROR_OCCURRED_MESSAGE,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    settingsDialog.cancel()
                    Toast.makeText(this, "Wallet info updated!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    override fun editTransaction(
        walletId:String,
        walletHistId: String,
        transactionType: String,
        origAmt: Double,
        updatedAmt: Double,
        updatedPurpose: String,
        toRetOrRec:Boolean)
    {
        val isRecoveredOrReturned:Any?=if(toRetOrRec) false else null
        val histUpdates = hashMapOf(
            "purpose" to updatedPurpose,
            "toRecoverOrReturn" to toRetOrRec,
            "isRecoveredOrReturned" to isRecoveredOrReturned,
            WalletScanConstants.USER_INFO_MODIFIED_BY_FIELD to LoggedInUserDetails.uid,
            WalletScanConstants.USER_INFO_MODIFIED_ON_FIELD to Date()
        )
        val amt: Double = if (transactionType==WalletScanConstants.TRANSACTION_TYPE_ADD){
            histUpdates["amtAdded"] = updatedAmt
            updatedAmt-origAmt
        }
        else{
            histUpdates["amtDeducted"] = updatedAmt
            origAmt-updatedAmt
        }
        FirebaseUtil.getWalletsHistReference().document(walletHistId)
            .set(histUpdates, SetOptions.merge())
        //wallet update
        val updates = hashMapOf (
            "currentBalance" to FieldValue.increment(amt),
            WalletScanConstants.USER_INFO_MODIFIED_BY_FIELD to LoggedInUserDetails.uid,
            WalletScanConstants.USER_INFO_MODIFIED_ON_FIELD to Date()
        )
        FirebaseUtil.getWalletsReference().document(walletId).set(updates, SetOptions.merge())
    }

    override fun saveStatus(
        walletId: String,
        walletHistId: String,
    ) {
        val histUpdates = hashMapOf<String, Any>(
            "isRecoveredOrReturned" to true,
            WalletScanConstants.USER_INFO_MODIFIED_BY_FIELD to LoggedInUserDetails.uid,
            WalletScanConstants.USER_INFO_MODIFIED_ON_FIELD to Date(),
            "toRecoverOrReturn" to false
        )
        FirebaseUtil.getWalletsHistReference().document(walletHistId)
            .set(histUpdates, SetOptions.merge())
    }

    override fun deleteTransaction(walletHistInfo: WalletHistInfo) {
        FirebaseUtil.getWalletsHistReference().document(walletHistInfo.id).delete()
        val amt = if (walletHistInfo.transactionType==WalletScanConstants.TRANSACTION_TYPE_ADD){
            -(walletHistInfo.amtAdded)!!
        } else{
            (walletHistInfo.amtDeducted)!!
        }
        //wallet update
        val updates = hashMapOf (
            "currentBalance" to FieldValue.increment(amt),
            WalletScanConstants.USER_INFO_MODIFIED_BY_FIELD to LoggedInUserDetails.uid,
            WalletScanConstants.USER_INFO_MODIFIED_ON_FIELD to Date()
        )
        FirebaseUtil.getWalletsReference().document(walletHistInfo.walletId).set(updates, SetOptions.merge())
    }
}