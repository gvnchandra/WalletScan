package com.android.walletscan.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.airbnb.lottie.LottieAnimationView
import com.android.walletscan.R
import com.android.walletscan.adapters.WalletDetailHistoryAdapter
import com.android.walletscan.databinding.FragmentTransactionHistoryBinding
import com.android.walletscan.documents.WalletHistInfo
import com.android.walletscan.documents.WalletHistInfoTemp
import com.android.walletscan.documents.WalletInfo
import com.android.walletscan.interfaces.TransactionEditDeleteListener
import com.android.walletscan.network.ConnectionManager
import com.android.walletscan.supporting.LoggedInUserDetails
import com.android.walletscan.supporting.WalletScanConstants
import com.android.walletscan.util.FirebaseUtil
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import java.util.Collections
import java.util.Date

class TransactionHistoryFragment : Fragment() {
    lateinit var binding: FragmentTransactionHistoryBinding
    lateinit var con: Activity
    lateinit var rvHist: RecyclerView
    var walletHistAdapter: WalletDetailHistoryAdapter? = null
    lateinit var walletHistInfoList: MutableList<WalletHistInfo>
    lateinit var walletInfo:WalletInfo

    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var txtNoRecords: TextView
    lateinit var noDataAnim: LottieAnimationView
    lateinit var noInternetAnim: LottieAnimationView
    lateinit var loadingAnim: LottieAnimationView

    var dateComparator = Comparator<WalletHistInfo> { one, two ->
        one.transactionDoneOn!!.compareTo(two.transactionDoneOn)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTransactionHistoryBinding.inflate(inflater, container, false)
        txtNoRecords = binding.txtNoRecords
        noDataAnim = binding.noDataAnim
        loadingAnim = binding.loadingAnim
        noInternetAnim = binding.noInternetAnim
        swipeRefreshLayout = binding.swipeRefreshLayout
        con = activity as Activity
        rvHist = binding.rvHist
        initData()
        return binding.root
    }

    private fun initData() {
        rvHist.layoutManager = LinearLayoutManager(con, LinearLayoutManager.VERTICAL, false)
        rvHist.setHasFixedSize(true)
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                con,
                R.color.white
            )
        )
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(con, R.color.primary_color))
        walletInfo = arguments?.getSerializable(WalletScanConstants.INTENT_PARAM_WALLET_INFO)!! as WalletInfo
        swipeRefreshLayout.setOnRefreshListener {
            retrieveHistory(walletInfo.id)
        }
        retrieveHistory(walletInfo.id)
    }

    private fun retrieveHistory(walletId: String) {
        swipeRefreshLayout.isRefreshing = false
        if (ConnectionManager.isNetworkAvailable(con)) {
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
                        val walletHistInfoTempList = setDataList(walletHistInfoList)
                        walletHistAdapter = WalletDetailHistoryAdapter(con, walletHistInfoTempList)
                        rvHist.adapter = walletHistAdapter
                        setVisible(2)
                    } else
                        setVisible(3)
                }
        } else
            setVisible(4)
    }

    private fun setDataList(walletHistInfoList: MutableList<WalletHistInfo>):
            MutableList<WalletHistInfoTemp> {
        val walletHistInfoTempList = mutableListOf<WalletHistInfoTemp>()
        var startingBalance = walletInfo.openingBalance
        walletHistInfoList.forEach {
            if (it.transactionType == WalletScanConstants.TRANSACTION_TYPE_ADD) {
                walletHistInfoTempList.add(
                    WalletHistInfoTemp(startingBalance, startingBalance+it.amtAdded!!, it)
                )
                startingBalance+=it.amtAdded!!
            } else {
                walletHistInfoTempList.add(
                    WalletHistInfoTemp(startingBalance, startingBalance-it.amtDeducted!!, it)
                )
                startingBalance-=it.amtDeducted!!
            }
        }
        walletHistInfoTempList.reverse()
        return walletHistInfoTempList
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


}