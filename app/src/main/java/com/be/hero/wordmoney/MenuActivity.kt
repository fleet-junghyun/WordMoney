package com.be.hero.wordmoney

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.be.hero.wordmoney.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            ivExit.setOnClickListener { finish() }
        }
    }

}