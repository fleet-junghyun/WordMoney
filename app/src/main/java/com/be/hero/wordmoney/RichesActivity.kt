package com.be.hero.wordmoney

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.be.hero.wordmoney.databinding.ActivityRichesBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class RichesActivity: AppCompatActivity() {

    private lateinit var binding: ActivityRichesBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRichesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            ivExit.setOnClickListener { finish() }
        }

        // 첫 진입 시 "세계" Fragment 호출
        if (savedInstanceState == null) {
            replaceFragment(WorldFragment())
        }

        // "세계" 버튼 클릭 시 Fragment 호출
        binding.categoryWorld.setOnClickListener {
            replaceFragment(WorldFragment())
        }

    }

    // Fragment 교체 메서드
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

}