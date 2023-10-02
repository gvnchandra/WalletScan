package com.android.walletscan.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.walletscan.databinding.ActivityRoutingBinding
import com.android.walletscan.supporting.PreferenceManager
import com.android.walletscan.util.AppUtil

class RoutingActivity : AppCompatActivity() {
    lateinit var binding: ActivityRoutingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.window.setFlags(1024, 1024)
        PreferenceManager.init(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            initRouting()
        } else {
            binding = ActivityRoutingBinding.inflate(layoutInflater)
            setContentView(binding.root)
            Thread {
                Thread.sleep(1000)
                initRouting()
            }.start()
        }
    }

    private fun initRouting() {
        if (!PreferenceManager.getLoggedIn()) {
            startActivity(Intent(this, PhoneNumberLogin::class.java))
        } else {
            AppUtil.setLoggedInfo(
                PreferenceManager.getName(),
                PreferenceManager.getBio(),
                PreferenceManager.getPhone(),
                PreferenceManager.getUId()
            )
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }
}