package com.be.hero.wordmoney

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.be.hero.wordmoney.databinding.ActivityPremiumBinding

class PremiumActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPremiumBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPremiumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            ivExit.setOnClickListener { finish() }
        }
    }

}