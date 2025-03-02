package com.be.hero.wordmoney

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.be.hero.wordmoney.config.WordMoneyConfig
import com.be.hero.wordmoney.databinding.ActivityMainBinding
import com.be.hero.wordmoney.quoteAdapter.QuotePagerAdapter
import com.be.hero.wordmoney.quoteData.Quote
import com.be.hero.wordmoney.quoteData.QuoteViewModel
import com.be.hero.wordmoney.userData.UserViewModel
import com.be.hero.wordmoney.widget.QuoteWidgetProvider
import com.be.hero.wordmoney.widget.WidgetUpdateWorker
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var quotePagerAdapter: QuotePagerAdapter
    private val quoteViewModel: QuoteViewModel by viewModels() // 🔥 ViewModel 사용
    private val userViewModel: UserViewModel by viewModels()

    private val config by lazy {
        WordMoneyConfig.get(application)
    }


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

        setViewPager()
        if (config.isToken.isNullOrEmpty()) {
            saveUserTokenToFirestore()
        }
        setWidget()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // ✅ API 33 이상에서만 실행
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun setWidget() {
        // ✅ WorkManager 실행 보장
        WidgetUpdateWorker.scheduleWidgetUpdate(this)
        updateAllWidgets()
    }


    private fun saveUserTokenToFirestore() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            val token = task.result ?: return@addOnCompleteListener
            userViewModel.saveUserToken(token)
            userViewModel.fetchFollowingList(token) // ✅ Firestore에서 팔로우 리스트 불러오기
        }
    }


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("FCM", "✅ 푸시 알림 권한 허용됨")
        } else {
            Log.e("FCM", "❌ 푸시 알림 권한 거부됨")
        }
    }

    private fun updateAllWidgets() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, QuoteWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        val viewModel = QuoteViewModel(application)

        for (appWidgetId in appWidgetIds) {
            QuoteWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId, viewModel)
        }

        // ✅ WorkManager가 설정되지 않았다면 실행 (앱 실행 시 주기적 업데이트 보장)
        WidgetUpdateWorker.scheduleWidgetUpdate(this)
    }


    private fun setViewPager() {
        quotePagerAdapter = QuotePagerAdapter(emptyList())
        binding.viewPager.adapter = quotePagerAdapter
        binding.viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        quoteViewModel.quotes.observe(this, Observer { quotes ->
            quotePagerAdapter.updateQuotes(quotes)
        })
        quotePagerAdapter.setShareClickListener(object : QuotePagerAdapter.ShareClickListener {
            override fun shareClick(quote: Quote) {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "\"${quote.quote}\"\n\n- ${quote.author}" // 🔥 줄바꿈 추가
                    )
                    type = "text/plain"
                }
                val chooser = Intent.createChooser(shareIntent, "공유하기")
                startActivity(chooser)
            }
        }
        )
    }

    private fun goToMenu() {
        Intent(this, MenuActivity::class.java).run { startActivity(this) }
    }

    private fun gotoRiches() {
        Intent(this, RichesActivity::class.java).run { startActivity(this) }
    }

//    private fun insertQuotesToFirestore() {
//        val firestore = FirebaseFirestore.getInstance()
//        val authorUUID = "5de69aac-bdb3-4348-8c33-41b1149a0028" // 🔴 Firestore에서 가져온 UUID로 변경해야 함
//        val author = "빌 게이츠"
//        val richId = 10
//
//        val quotes = listOf(
//            "문제 해결에 집중하면 부는 자연스럽게 따라온다.",
//            "배움에 투자하는 것이 최고의 자산이다.",
//            "자신이 이해하는 분야에만 투자하라.",
//            "장기적 안목이 단기 변동보다 훨씬 중요하다.",
//            "끊임없는 혁신이 지속 가능한 성공의 열쇠다.",
//            "실패를 두려워하지 말고, 배움의 기회로 삼아라.",
//            "효율성과 단순함은 비즈니스의 근간이다.",
//            "팀워크와 협업이 위대한 성과를 낳는다.",
//            "기술 발전은 사회 문제 해결의 도구다.",
//            "현실적인 목표를 세우고 꾸준히 실행하라.",
//            "최신 기술을 활용해 새로운 기회를 창출하라.",
//            "끊임없이 질문하고 답을 찾는 자세가 필요하다.",
//            "자신의 실수를 인정하고 개선하면 성장한다.",
//            "혁신은 항상 도전과 실험에서 시작된다.",
//            "장기적인 비전을 가지고 움직여야 한다.",
//            "고객의 목소리에 귀 기울이면 성공으로 이어진다.",
//            "자신을 꾸준히 계발하는 것이 경쟁력이다.",
//            "사회에 기여하는 기업이 결국 지속된다.",
//            "데이터 기반의 의사결정이 실패를 줄인다.",
//            "열정과 끈기가 모든 어려움을 극복하게 한다.",
//            "단기 이익보다 장기적 가치를 우선하라.",
//            "기술 혁신은 새로운 시장을 열어준다.",
//            "자원의 효율적 배분이 부의 증식을 이끈다.",
//            "창의력과 논리적 사고가 결합되어 혁신을 만든다.",
//            "끊임없이 변화하는 세상에 유연하게 대응하라.",
//            "투자는 철저한 분석과 준비에서 시작된다.",
//            "도전하는 정신이 곧 성공의 밑바탕이다.",
//            "매일의 작은 개선이 큰 변화를 이끈다.",
//            "책임감 있는 리더십이 팀을 단합시킨다.",
//            "혁신적인 아이디어는 실행으로 증명된다.",
//            "모든 사업은 사회적 가치를 동반해야 한다.",
//            "문제 해결 능력이 부의 기초를 다진다.",
//            "끊임없이 학습하는 자세가 미래를 만든다.",
//            "고객 만족은 비즈니스 성공의 시작이다.",
//            "기술은 인류 발전의 가장 강력한 도구다.",
//            "실패를 경험삼아 다시 도전하면 기회가 온다.",
//            "자신의 한계를 뛰어넘는 도전을 계속하라.",
//            "긍정적 사고가 어려움을 극복하게 한다.",
//            "혁신은 기존 틀을 깨뜨리는 용기에서 나온다.",
//            "끊임없이 질문하고 개선하는 것이 성장의 비결이다.",
//            "효율적인 시스템 구축이 경쟁력을 강화한다.",
//            "자신의 아이디어에 확신을 가지고 추진하라.",
//            "기술과 사회의 발전은 서로 보완적이다.",
//            "모든 결정에는 철저한 분석이 따라야 한다.",
//            "시장 변동 속에서도 침착함을 잃지 마라.",
//            "창의적 해결책은 다양한 경험에서 나온다.",
//            "실패를 두려워하지 않는 도전이 기회를 만든다.",
//            "끊임없이 목표를 재설정하며 나아가라.",
//            "혁신적인 방법이 기존 문제를 해결한다.",
//            "지속 가능한 발전을 위해 꾸준히 투자하라.",
//            "배움에 대한 열정이 곧 부의 기반이다.",
//            "자신의 실수를 기록하고 분석하라.",
//            "현실에 안주하지 말고 계속 도전하라.",
//            "최신 기술 동향에 민감하게 반응하라.",
//            "효과적인 시간 관리가 성공을 좌우한다.",
//            "작은 아이디어도 꾸준히 발전시키면 큰 성과를 낸다.",
//            "세상을 바꾸려면 먼저 자신부터 변화해야 한다.",
//            "긍정적인 마인드와 겸손함이 리더를 만든다.",
//            "경험에서 배우고 그것을 공유하라.",
//            "변화를 기회로 삼는 유연한 사고가 필요하다.",
//            "창의력은 실패를 극복하는 힘이다.",
//            "끊임없는 연구개발이 새로운 시장을 개척한다.",
//            "실행력이 곧 혁신의 가치를 증명한다.",
//            "정보와 지식을 공유하면 모두가 성장한다.",
//            "사회 문제 해결이 장기적인 부의 원천이다.",
//            "투자에 있어 감정보다는 분석이 우선되어야 한다.",
//            "항상 미래를 예측하고 대비하는 자세가 중요하다.",
//            "리더는 비전을 명확히 제시하고 실행해야 한다.",
//            "모든 결정은 철저한 데이터 분석에 기반한다.",
//            "지속적인 자기 개선이 경쟁력을 유지시킨다.",
//            "실패를 경험삼아 더 강하게 다시 일어서라.",
//            "높은 목표를 세우고 그것을 향해 꾸준히 나아가라.",
//            "협업과 소통이 복잡한 문제를 해결한다.",
//            "혁신은 도전과 실천에서 비롯된다.",
//            "현실적인 문제에 집중하면 기회가 보인다.",
//            "끊임없이 스스로를 계발하며 배우라.",
//            "세상의 변화에 적극적으로 참여하라.",
//            "긍정적인 변화를 이끌어내는 것이 진정한 성공이다.",
//            "기술은 모든 산업의 발전을 견인한다.",
//            "도전과 실패는 결국 큰 성공을 낳는다.",
//            "사회적 책임을 다하는 기업이 장기적 성장을 이끈다.",
//            "지식과 실천의 결합이 부를 만든다.",
//            "미래를 내다보는 통찰력이 모든 결정의 기준이다.",
//            "효율성과 혁신은 서로를 보완하는 힘이다.",
//            "작은 성공들이 모여 큰 성과를 만든다.",
//            "시장과 기술의 변화를 예의주시하라.",
//            "성공은 끊임없는 도전과 개선에서 나온다.",
//            "현실에 안주하지 않고 늘 새로운 것을 모색하라.",
//            "위대한 아이디어는 실행으로 완성된다.",
//            "자신의 경험을 바탕으로 지속적으로 성장하라.",
//            "모든 투자는 철저한 분석과 준비로부터 시작된다.",
//            "긍정적인 에너지와 열정이 팀을 하나로 만든다.",
//            "효과적인 자원 배분이 성공을 극대화한다.",
//            "도전하는 자세가 미래 부의 문을 연다.",
//            "끊임없이 질문하고 답을 찾아내는 과정이 중요하다.",
//            "현실적인 문제를 해결하는 것이 곧 시장의 요구다.",
//            "미래 기술에 대한 투자로 오늘의 한계를 극복하라.",
//            "겸손과 열정이 함께할 때 진정한 리더십이 발현된다.",
//            "자신의 목표를 확고히 하고 꾸준히 추진하라.",
//            "빌 게이츠의 철학은 ‘지식과 실천이 부를 창출한다’는 데 있다."
//        )
//
//
//
//
//
//        val quoteList = quotes.mapIndexed { index, quote ->
//            mapOf(
//                "id" to index + 1,
//                "richId" to richId,
//                "uuid" to authorUUID,
//                "quote" to quote,
//                "author" to author,
//                "isBookmarked" to false
//            )
//        }
//
//        val documentRef = firestore.collection("quotes").document(authorUUID)
//
//        // 데이터를 한 번에 저장 (배치)
//        documentRef.set(mapOf("quotes" to quoteList))
//            .addOnSuccessListener {
//                println("🔥 명언 Firestore 저장 완료!")
//            }
//            .addOnFailureListener { e ->
//                println("❌ Firestore 저장 실패: ${e.message}")
//            }
//    }


//    private fun insertFireStoreRiches() {
//        // 예제 데이터 생성
//        val billionaireList = listOf(
//            Billionaire(
//                id = 1,
//                uuid = UUID.randomUUID().toString(),
//                name = "일론 머스크",
//                netWorth = "약 601조원",
//                description = listOf("TESLA CEO", "SPACE X", "PayPal"),
//                isSelected = false,
//                category = 1,
//                listPosition = 1
//            ),
//            Billionaire(
//                id = 2,
//                uuid = UUID.randomUUID().toString(),
//                name = "제프 베이조스",
//                netWorth = "약 378조원",
//                description = listOf("Amazon", "e-Commerce 혁명", "고객 집착"),
//                isSelected = false,
//                category = 1,
//                listPosition = 2
//            ),
//            Billionaire(
//                id = 3,
//                uuid = UUID.randomUUID().toString(),
//                name = "베르나르 아르노",
//                netWorth = "약 342조원",
//                description = listOf("LVMH 회장", "명품 제국 운영", "유럽 최고 부호"),
//                isSelected = false,
//                category = 1,
//                listPosition = 3
//            ),
//            Billionaire(
//                id = 4,
//                uuid = UUID.randomUUID().toString(),
//                name = "래리 엘리슨",
//                netWorth = "약 308조원",
//                description = listOf("Oracle 창업자", "DB·Cloud 사업"),
//                isSelected = false,
//                category = 1,
//                listPosition = 4
//            ),
//            Billionaire(
//                id = 5,
//                uuid = UUID.randomUUID().toString(),
//                name = "마크 저커버그",
//                netWorth = "약 287조원",
//                description = listOf("Meta CEO", "Instagram"),
//                isSelected = false,
//                category = 1,
//                listPosition = 5
//            ),
//            Billionaire(
//                id = 6,
//                uuid = UUID.randomUUID().toString(),
//                name = "세르게이 브린",
//                netWorth = "약 209조원",
//                description = listOf("Google 공동 창업자", "검색·AI 혁신", "연구·미래기술 투자"),
//                isSelected = false,
//                category = 1,
//                listPosition = 6
//            ),
//            Billionaire(
//                id = 7,
//                uuid = UUID.randomUUID().toString(),
//                name = "스티브 발머",
//                netWorth = "약 205조원",
//                description = listOf("MS 전 CEO", "공격적 경영", "NBA LA 클리퍼스 구단주"),
//                isSelected = false,
//                category = 1,
//                listPosition = 7
//            ),
//            Billionaire(
//                id = 8,
//                uuid = UUID.randomUUID().toString(),
//                name = "워런 버핏",
//                netWorth = "약 200조원",
//                description = listOf("Berkshire Hathaway 회장", "가치투자 전설", "오마하의 현인"),
//                isSelected = false,
//                category = 1,
//                listPosition = 8
//            ),
//            Billionaire(
//                id = 9,
//                uuid = UUID.randomUUID().toString(),
//                name = "젠슨 황",
//                netWorth = "약 141조원",
//                description = listOf("NVIDIA 창업자·CEO", "GPU·AI 혁신 주도", "대만계 미국인 성공 스토리"),
//                isSelected = false,
//                category = 1,
//                listPosition = 9
//            ),
//            Billionaire(
//                id = 10,
//                uuid = UUID.randomUUID().toString(),
//                name = "빌 게이츠",
//                netWorth = "약 138조원",
//                description = listOf("MS 공동 창업자", "세계 최대 자선재단 운영", "글로벌 보건·교육 기여"),
//                isSelected = false,
//                category = 1,
//                listPosition = 10
//            )
//        )
//        billionaireViewModel.insertMultipleBillionairesToFirestore(billionaireList)
//    }


}