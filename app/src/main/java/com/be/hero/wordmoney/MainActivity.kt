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
import com.be.hero.wordmoney.billionaireData.Billionaire
import com.be.hero.wordmoney.billionaireData.BillionaireViewModel
import com.be.hero.wordmoney.config.WordMoneyConfig
import com.be.hero.wordmoney.databinding.ActivityMainBinding
import com.be.hero.wordmoney.quoteAdapter.QuotePagerAdapter
import com.be.hero.wordmoney.quoteData.QuoteViewModel
import com.be.hero.wordmoney.userData.UserViewModel
import com.be.hero.wordmoney.widget.QuoteWidgetProvider
import com.be.hero.wordmoney.widget.WidgetUpdateWorker
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.util.UUID


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var quotePagerAdapter: QuotePagerAdapter
    private val quoteViewModel: QuoteViewModel by viewModels() // 🔥 ViewModel 사용
    private val billionaireViewModel: BillionaireViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()


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

        saveUserTokenToFirestore()

        //getToken 저장
        userViewModel.getToken()

        // ✅ WorkManager 실행 보장
        WidgetUpdateWorker.scheduleWidgetUpdate(this)
        updateAllWidgets()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // ✅ API 33 이상에서만 실행
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
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
            Log.d("guotes_size", quotes.size.toString())
            quotePagerAdapter.updateQuotes(quotes)
        })
    }

    private fun goToMenu() {
        Intent(this, MenuActivity::class.java).run { startActivity(this) }
    }

    private fun gotoRiches() {
        Intent(this, RichesActivity::class.java).run { startActivity(this) }
    }

    private fun insertJeffBezosQuotesToFirestore() {
        val firestore = FirebaseFirestore.getInstance()
        val authorUUID = "45ff6cab-e67a-4db1-b815-6e70d94d86ba" // 🔴 Firestore에서 가져온 UUID로 변경해야 함
        val author = "Jeff Bezos"
        val richId = 2

        val quotes = listOf(
            "미리 정답을 알고 있는 일만 한다면, 결국 회사는 사라질 것이다.",
            "우리는 비전에 대해서는 고집스럽지만, 세부 사항에는 유연하다.",
            "당신이 고객을 만족시키면, 그들은 다른 사람에게 그것을 이야기할 것이고, 아주 강력한 마케팅이 된다.",
            "당신이 장기적으로 생각한다면, 단기적으로 돈을 덜 벌게 될 수도 있지만, 궁극적으로 더 많은 돈을 벌게 된다.",
            "우리가 가진 자원의 크기에 의해 결정되는 것이 아니라, 우리가 가진 야망의 크기에 의해 결정된다.",
            "고객은 항상 더 나은 것을 원한다. 그리고 그들이 원하는 것을 제공하는 것이 우리가 돈을 버는 방법이다.",
            "우리는 항상 장기적인 관점에서 투자한다. 단기적인 이익에 집착하면 미래를 잃게 된다.",
            "위험을 감수하지 않으면, 큰 돈을 벌 기회도 얻지 못한다.",
            "기업의 가치는 돈이 아니라, 고객의 신뢰에서 나온다.",
            "작은 실험들을 많이 해라. 그중 몇 개는 크게 성공할 것이고, 그것이 돈이 된다.",
            "회사가 고객보다 자기 자신을 더 중요하게 생각하는 순간, 돈을 잃기 시작한다.",
            "비즈니스에서 돈을 벌고 싶다면, 남들보다 한 걸음 앞서 있어야 한다.",
            "돈은 결과이지 목표가 아니다. 좋은 회사를 만들면 돈은 자연스럽게 따라온다.",
            "기업은 규모가 아니라, 얼마나 고객 중심적인가에 따라 성공이 결정된다.",
            "우리가 오늘 혁신하지 않으면, 내일 경쟁자가 우리를 무너뜨릴 것이다.",
            "비즈니스에서는 실수도 자산이다. 하지만 같은 실수를 반복하면 돈을 잃는다.",
            "현명한 사람들은 위기를 기회로 만든다. 가장 큰 돈은 변화 속에서 벌린다.",
            "우리는 실패할 수 있다. 하지만 진짜 위험은 시도조차 하지 않는 것이다.",
            "고객이 불만을 가질 때, 그것은 우리가 개선하고 돈을 벌 수 있는 기회다.",
            "돈은 단순한 숫자가 아니라, 신뢰의 증거다.",
            "성공적인 기업은 돈을 어떻게 버는지가 아니라, 어떻게 가치를 창출하는지가 중요하다.",
            "오늘의 돈보다 내일의 가치를 생각하라.",
            "돈이 목표라면 오래가지 못한다. 하지만 고객의 문제를 해결하면 돈은 따라온다.",
            "효율적으로 일하지 않으면, 비용이 증가하고 결국 돈을 잃는다.",
            "사람들이 원하는 것을 제공하는 것이 가장 확실한 돈 버는 방법이다.",
            "단기적인 손해를 감수하더라도, 장기적인 성공을 위해 투자해야 한다.",
            "어려운 시기일수록 더 많은 기회가 있다. 위기를 활용하는 사람이 돈을 번다.",
            "돈을 벌기 위해서는 고객이 행복해야 한다. 고객이 행복하면 돈은 자연스럽게 따라온다.",
            "이익보다 중요한 것은 신뢰다. 신뢰를 쌓으면 돈은 따라온다.",
            "위대한 기업은 고객의 기대를 뛰어넘는다. 기대를 뛰어넘는 순간, 돈도 함께 따라온다."
        )

        val quoteList = quotes.mapIndexed { index, quote ->
            mapOf(
                "id" to index + 1,
                "richId" to richId,
                "uuid" to UUID.randomUUID().toString(),
                "quote" to quote,
                "author" to author,
                "isBookmarked" to false
            )
        }

        val documentRef = firestore.collection("quotes").document(authorUUID)

        // 데이터를 한 번에 저장 (배치)
        documentRef.set(mapOf("quotes" to quoteList))
            .addOnSuccessListener {
                println("🔥 명언 Firestore 저장 완료!")
            }
            .addOnFailureListener { e ->
                println("❌ Firestore 저장 실패: ${e.message}")
            }
    }


    private fun insertFireStoreRiches() {
        // 예제 데이터 생성
        val billionaireList = listOf(
            Billionaire(
                id = 1,
                uuid = UUID.randomUUID().toString(),
                name = "Elon Musk",
                netWorth = "320조원",
                description = listOf(
                    "혁신가", "테슬라 CEO", "스페이스X 창립자", "화성 탐사를 목표로 하는 기업가",
                    "트위터 인수 후 X로 브랜드 변경", "전기차 산업을 선도하는 인물",
                    "OpenAI 설립 초기 투자자", "Hyperloop 개념을 제안한 미래 기술가"
                ),
                quoteCount = 120,
                isSelected = false,
                category = 1,
                listPosition = 1
            ),
            Billionaire(
                id = 2,
                uuid = UUID.randomUUID().toString(),
                name = "Jeff Bezos",
                netWorth = "300조원",
                description = listOf(
                    "아마존 창립자", "세계 최대 전자상거래 기업 운영", "블루 오리진(Blue Origin) 설립",
                    "우주 탐사를 위한 로켓 개발", "고객 중심 경영 철학을 가진 인물",
                    "AI 및 클라우드 컴퓨팅 혁신을 주도한 경영자", "뉴욕타임스 등 언론 매체 인수"
                ),
                quoteCount = 95,
                isSelected = false,
                category = 1,
                listPosition = 2
            ),
            Billionaire(
                id = 3,
                uuid = UUID.randomUUID().toString(),
                name = "Bill Gates",
                netWorth = "250조원",
                description = listOf(
                    "마이크로소프트(Microsoft) 공동 창업자", "윈도우 운영체제 개발",
                    "세계 최초의 소프트웨어 산업 선도자", "빌 & 멜린다 게이츠 재단을 통한 자선사업가",
                    "소프트웨어 혁명가이자 미래 기술 비전 제시자", "코로나 백신 개발 및 기부 활동"
                ),
                quoteCount = 110,
                isSelected = false,
                category = 1,
                listPosition = 3
            ),
            Billionaire(
                id = 4,
                uuid = UUID.randomUUID().toString(),
                name = "Warren Buffett",
                netWorth = "240조원",
                description = listOf(
                    "버크셔 해서웨이 CEO", "세계적인 투자자", "장기 가치 투자 철학을 전파",
                    "기부 활동으로 자산의 99%를 사회에 환원하겠다고 선언",
                    "‘오마하의 현인’이라는 별명을 가짐", "매년 투자 서한을 통해 투자 철학을 공유"
                ),
                quoteCount = 105,
                isSelected = false,
                category = 1,
                listPosition = 4
            ),
            Billionaire(
                id = 5,
                uuid = UUID.randomUUID().toString(),
                name = "Mark Zuckerberg",
                netWorth = "210조원",
                description = listOf(
                    "페이스북(Facebook) 창립자", "메타(Meta) CEO", "SNS 산업 혁신가",
                    "메타버스 기술을 발전시키는 미래 전략가", "왓츠앱, 인스타그램 인수",
                    "기부 활동을 통한 사회적 환원", "디지털 광고 시장을 변화시킨 인물"
                ),
                quoteCount = 98,
                isSelected = false,
                category = 1,
                listPosition = 5
            ),
            Billionaire(
                id = 6,
                uuid = UUID.randomUUID().toString(),
                name = "Bernard Arnault",
                netWorth = "280조원",
                description = listOf(
                    "LVMH 회장", "럭셔리 브랜드 산업의 거물", "루이비통, 디올, 지방시, 티파니 운영",
                    "유럽에서 가장 부유한 기업가", "패션과 명품 시장을 주도하는 혁신가"
                ),
                quoteCount = 85,
                isSelected = false,
                category = 1,
                listPosition = 6
            ),
            Billionaire(
                id = 7,
                uuid = UUID.randomUUID().toString(),
                name = "Larry Page",
                netWorth = "200조원",
                description = listOf(
                    "구글(Google) 공동 창립자", "알파벳(Alphabet) CEO", "세계 최대 검색 엔진 운영",
                    "인공지능 기술 발전에 기여", "자율주행 자동차 웨이모(Waymo) 투자",
                    "유튜브, 안드로이드 등 글로벌 플랫폼 운영"
                ),
                quoteCount = 75,
                isSelected = false,
                category = 1,
                listPosition = 7
            ),
            Billionaire(
                id = 8,
                uuid = UUID.randomUUID().toString(),
                name = "Sergey Brin",
                netWorth = "195조원",
                description = listOf(
                    "구글 공동 창립자", "AI 기반 검색 시스템 개발", "알파벳 회장",
                    "신기술과 데이터 기반 혁신을 주도", "생명연장 프로젝트 투자"
                ),
                quoteCount = 72,
                isSelected = false,
                category = 1,
                listPosition = 8
            ),
            Billionaire(
                id = 9,
                uuid = UUID.randomUUID().toString(),
                name = "Steve Jobs",
                netWorth = "150조원 (사망 당시)",
                description = listOf(
                    "애플(Apple) 공동 창립자", "아이폰, 아이패드, 맥북 개발",
                    "디자인과 UX 철학을 완전히 바꾼 인물", "픽사(Pixar) 영화사 운영",
                    "기술 혁신과 감성적 제품 개발로 유명"
                ),
                quoteCount = 130,
                isSelected = false,
                category = 1,
                listPosition = 9
            ),
            Billionaire(
                id = 10,
                uuid = UUID.randomUUID().toString(),
                name = "Jack Ma",
                netWorth = "180조원",
                description = listOf(
                    "알리바바(Alibaba) 창립자", "중국 전자상거래 시장을 혁신",
                    "중소기업과 글로벌 시장을 연결", "알리페이, 클라우드 컴퓨팅 성장 주도",
                    "AI 기반의 비즈니스 모델 확장"
                ),
                quoteCount = 89,
                isSelected = false,
                category = 1,
                listPosition = 10
            )
        )

        billionaireViewModel.insertMultipleBillionairesToFirestore(billionaireList)
    }


}