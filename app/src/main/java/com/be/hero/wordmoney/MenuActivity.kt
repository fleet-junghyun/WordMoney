package com.be.hero.wordmoney

import android.content.Intent
import android.net.Uri
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
            review.setOnClickListener { openReviewPage() }
            recommend.setOnClickListener { share() }
        }
    }

    // Google Play 스토어 리뷰 페이지 열기 함수
    private fun openReviewPage() {
        val uri = Uri.parse("https://play.google.com/store/apps/details?id=com.nhn.android.search&hl=ko")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(goToMarket)
        } catch (e: Exception) {
            val webUri = Uri.parse("https://play.google.com/store/apps/details?id=com.nhn.android.search&hl=ko")
            startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }

    private fun share() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "돈머리 - 부자들의 마인드셋 \n \"https://play.google.com/store/apps/details?id=com.nhn.android.search&hl=ko\"" // 🔥 줄바꿈 추가
            )
            type = "text/plain"
        }
        val chooser = Intent.createChooser(shareIntent, "공유하기")
        startActivity(chooser)
    }

}