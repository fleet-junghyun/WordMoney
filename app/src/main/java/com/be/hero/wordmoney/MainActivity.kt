package com.be.hero.wordmoney

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.be.hero.wordmoney.adapter.QuotePagerAdapter
import com.be.hero.wordmoney.billionaireData.BillionaireViewModel
import com.be.hero.wordmoney.data.Billionaire
import com.be.hero.wordmoney.databinding.ActivityMainBinding
import java.util.UUID


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

//        // 예제 데이터 생성
//        val sampleBillionaire = Billionaire(
//            id = 1,
//            uuid = UUID.randomUUID().toString(), // UUID 자동 생성
//            name = "Elon Musk",
//            netWorth = "320조원",
//            description = listOf("혁신가", "테슬라 CEO", "스페이스X 창립자"),
//            quoteCount = 120,
//            isSelected = false,
//            category = 1,
//            listPosition = 1
//        )
//
//        // 여러 명 삽입 예제
//        val billionaireList = listOf(
//            sampleBillionaire,
//            Billionaire(
//                id = 2,
//                uuid = UUID.randomUUID().toString(),
//                name = "Jeff Bezos",
//                netWorth = "300조원",
//                description = listOf("아마존 창립자", "전 세계 부자 순위 2위"),
//                quoteCount = 95,
//                isSelected = false,
//                category = 1,
//                listPosition = 2
//            )
//        )
//
//        billionaireViewModel.insertMultipleBillionairesToFirestore(billionaireList)


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