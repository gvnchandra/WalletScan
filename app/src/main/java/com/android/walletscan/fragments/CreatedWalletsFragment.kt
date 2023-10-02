package com.android.walletscan.fragments

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.airbnb.lottie.LottieAnimationView
import com.android.walletscan.R
import com.android.walletscan.adapters.CreatedWalletsAdapter
import com.android.walletscan.databinding.FragmentCreatedWalletsBinding
import com.android.walletscan.documents.WalletInfo
import com.android.walletscan.network.ConnectionManager
import com.android.walletscan.supporting.LoggedInUserDetails
import com.android.walletscan.util.FirebaseUtil
import com.google.firebase.firestore.ktx.toObject
import java.util.Collections

class CreatedWalletsFragment : Fragment() {
    lateinit var binding: FragmentCreatedWalletsBinding
    lateinit var con: Activity
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var txtNoWallets: TextView
    lateinit var noDataAnim: LottieAnimationView
    lateinit var rvMyWallets: RecyclerView
    var myWalletsAdapter: CreatedWalletsAdapter? = null
    lateinit var noInternetAnim: LottieAnimationView
    lateinit var loadingAnim: LottieAnimationView
    lateinit var myWalletsList: MutableList<WalletInfo>

    var dateComparator = Comparator<WalletInfo> { one, two ->
        if (one.modifiedOn != null && two.modifiedOn != null)
            two.modifiedOn!!.compareTo(one.modifiedOn)
        else if (one.modifiedOn != null)
            two.createdOn!!.compareTo(one.modifiedOn)
        else
            two.modifiedOn!!.compareTo(one.createdOn)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreatedWalletsBinding.inflate(inflater, container, false)
        con = activity as Activity
        txtNoWallets = binding.txtCreated
        noDataAnim = binding.noDataAnim
        loadingAnim = binding.loadingAnim
        noInternetAnim = binding.noInternetAnim
        rvMyWallets = binding.rvWallets
        swipeRefreshLayout = binding.swipeRefreshLayout
        initData()
        return binding.root
    }

    private fun initData() {
        rvMyWallets.layoutManager = LinearLayoutManager(con, LinearLayoutManager.VERTICAL, false)
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                con,
                R.color.white
            )
        )
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(con, R.color.primary_color))
        rvMyWallets.setHasFixedSize(true)
        swipeRefreshLayout.setOnRefreshListener {
            retrieveWallets()
        }
        retrieveWallets()
    }

    private fun retrieveWallets() {
        swipeRefreshLayout.isRefreshing=false
        if (ConnectionManager.isNetworkAvailable(con)) {
            setVisible(1)
            FirebaseUtil.getWalletsReference().whereEqualTo("ownerId", LoggedInUserDetails.uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.size() != 0) {
                        myWalletsList = mutableListOf()
                        snapshot.documents.forEach {
                            if (it.exists()) {
                                val walletInfo = it.toObject<WalletInfo>()!!
                                myWalletsList.add(walletInfo)
                            }
                        }
                        Collections.sort(myWalletsList, dateComparator)
                        myWalletsAdapter = CreatedWalletsAdapter(con, myWalletsList)
                        rvMyWallets.adapter = myWalletsAdapter
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
                rvMyWallets.visibility = View.GONE
                noDataAnim.visibility = View.GONE
                noInternetAnim.visibility = View.GONE
                loadingAnim.visibility = View.VISIBLE
                txtNoWallets.visibility = View.GONE
            }

            2 -> {
                loadingAnim.visibility = View.GONE
                noDataAnim.visibility = View.GONE
                noInternetAnim.visibility = View.GONE
                rvMyWallets.visibility = View.VISIBLE
                txtNoWallets.visibility = View.GONE
            }

            3 -> {
                loadingAnim.visibility = View.GONE
                rvMyWallets.visibility = View.GONE
                noInternetAnim.visibility = View.GONE
                noDataAnim.visibility = View.VISIBLE
                txtNoWallets.visibility = View.VISIBLE
            }

            4 -> {
                loadingAnim.visibility = View.GONE
                rvMyWallets.visibility = View.GONE
                noDataAnim.visibility = View.GONE
                noInternetAnim.visibility = View.VISIBLE
                txtNoWallets.visibility = View.GONE
            }
        }
    }
}