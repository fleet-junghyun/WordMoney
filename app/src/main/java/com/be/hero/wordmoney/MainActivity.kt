package com.be.hero.wordmoney

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.be.hero.wordmoney.adapter.QuotePagerAdapter
import com.be.hero.wordmoney.billionaireData.BillionaireViewModel
import com.be.hero.wordmoney.billionaireData.BillionaireViewModelFactory
import com.be.hero.wordmoney.databinding.ActivityMainBinding
import androidx.lifecycle.Observer


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val billionaireViewModel: BillionaireViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.apply {
            menu.setOnClickListener {
                goToMenu()
            }
            riches.setOnClickListener {
                gotoRiches()
            }

        }

        // ViewModel에서 데이터를 가져와 Room에 저장
        billionaireViewModel.fetchAndSaveBillionaires()

        // Room에서 저장된 데이터를 불러와서 출력
        billionaireViewModel.billionaires.observe(this, Observer { list ->
            list.forEach { billionaire ->
                Log.d("Billionaire", "🔥 ${billionaire.name} - ${billionaire.netWorth}")
            }
        })


        val quotes = listOf(
            "돈을 버는 것보다 더 중요한 것은 세계를 더 나은 곳으로 만드는 것이다.",
            "이영란.",
            "털보",
            "바보",
            "꿀꿀"
        )

        val adapter = QuotePagerAdapter(quotes)
        binding.apply {
            viewPager.adapter = adapter
            viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        }

    }

    private fun goToMenu() {
        Intent(this, MenuActivity::class.java).run { startActivity(this) }
    }

    private fun gotoRiches() {
        Intent(this, RichesActivity::class.java).run { startActivity(this) }
    }



}