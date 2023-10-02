package com.android.walletscan.activities

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.android.walletscan.R
import com.android.walletscan.databinding.ActivityWalletDetailHistoryBinding
import com.android.walletscan.documents.WalletInfo
import com.android.walletscan.fragments.TransactionHistoryFragment
import com.android.walletscan.fragments.VisualFragment
import com.android.walletscan.supporting.WalletScanConstants
import com.google.android.material.tabs.TabLayout

class WalletDetailHistoryActivity : AppCompatActivity() {
    lateinit var binding: ActivityWalletDetailHistoryBinding
    private lateinit var toolbar: Toolbar
    private lateinit var txtWalletName: TextView
    lateinit var tabLayout: TabLayout
    lateinit var walletInfo: WalletInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletDetailHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toolbar = binding.toolbar
        txtWalletName = binding.txtWalletName
        tabLayout = binding.tabLayout
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (intent != null) {
            walletInfo = intent.getSerializableExtra((WalletScanConstants.INTENT_PARAM_WALLET_INFO)) as WalletInfo
            txtWalletName.text=walletInfo.name
        }
        initAction()
    }

    private fun initAction() {
        try {
            tabLayout.selectTab(tabLayout.getTabAt(0))
            showHistFragment()
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (tab?.position == 0) {
                        showHistFragment()
                    } else {
                        showVisualFragment()
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {

                }

                override fun onTabReselected(tab: TabLayout.Tab?) {

                }

            })
        } catch (e: Exception) {
            Log.e("initAction", "${e.message}")
        }
    }

    private fun showVisualFragment() {
        val fragment = VisualFragment()
        val args = Bundle()
        args.putSerializable(WalletScanConstants.INTENT_PARAM_WALLET_INFO, walletInfo)
        fragment.arguments = args
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment)
            .commit()
    }

    private fun showHistFragment() {
        val fragment = TransactionHistoryFragment()
        val args = Bundle()
        args.putSerializable(WalletScanConstants.INTENT_PARAM_WALLET_INFO, walletInfo)
        fragment.arguments = args
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment)
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}