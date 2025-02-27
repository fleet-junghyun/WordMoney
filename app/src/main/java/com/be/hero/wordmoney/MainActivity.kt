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
//        insertQuotesToFirestore()

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
    }

    private fun goToMenu() {
        Intent(this, MenuActivity::class.java).run { startActivity(this) }
    }

    private fun gotoRiches() {
        Intent(this, RichesActivity::class.java).run { startActivity(this) }
    }

    private fun insertQuotesToFirestore() {
        val firestore = FirebaseFirestore.getInstance()
        val authorUUID = "d2ebbeec-35d3-4754-9445-1d17be4560a5" // 🔴 Firestore에서 가져온 UUID로 변경해야 함
        val author = "일론 머스크"
        val richId = 1

        val quotes = listOf(
            "나는 돈을 벌려고 창업한 게 아니다. 인류가 직면한 중요한 문제를 해결하고 싶었다. 부는 그 결과물이다.",
            "집중해야 한다고 느끼면, 주 80~100시간씩 일해라. 결국 그것이 성공과 실패를 가른다.",
            "여러분이 만들고자 하는 제품(서비스)이 정말로 최고라면, 사람들은 기꺼이 지갑을 연다.",
            "난 돈 자체엔 흥미가 없다. 더 나은 미래를 만드는 데 필요한 자원으로 본다.",
            "회사를 시작할 땐 돈을 벌겠다보다 이 문제를 해결하겠다가 더 중요하다.",
            "나는 실패를 두려워하지 않는다. 실패는 새로운 것을 시도한다는 증거이기 때문이다.",
            "실용적인 가치가 있는 혁신에 집중해야 한다. 그래야 시장이 만들어지고, 부가 창출된다.",
            "정말 중요한 목표라면, 확률이 낮아도 도전해야 한다. 그게 성공의 씨앗이다.",
            "창업가는 미래 가치를 보고 움직인다. 현실에만 안주하면 큰 돈을 벌기는 어렵다.",
            "한 번 정한 목표에 집착하고, 그 목표가 달성될 때까지 파고들어라.",
            "나는 모든 사업에 재투자한다. 그 돈은 다시 혁신으로 이어지고, 그러면 결국 더 큰 가치를 낳는다.",
            "매일 더 나아지려면, 어제보다 얼마나 나아졌는가?를 스스로에게 물어봐야 한다.",
            "내가 말하는 위험 감수란 무작정 뛰어드는 게 아니다. 계산된 위험을 기꺼이 감수하는 태도다.",
            "고객이 원하는 것을 먼저 파악하고, 그들이 반드시 필요로 하는 수준으로 끌어올려야 한다.",
            "기업가 정신이란 불확실성을 견디고, 아이디어를 현실로 만들어가는 과정이다. 그 속에서 부는 따른다.",
            "나 자신을 포함해, 모두가 멍청한 실수를 한다. 하지만 그 실수를 빠르게 인정하고 고쳐야 자산도 늘어난다.",
            "새로운 시장을 개척하거나, 기존 시장을 획기적으로 바꿔라. 남들이 못 본 길에 진짜 기회가 있다.",
            "가장 힘든 문제부터 직접 뛰어들어 해결하라. 그러면 누구도 쉽게 따라올 수 없는 가치를 만든다.",
            "큰 목표와 작은 목표는 에너지 소모가 비슷하다. 그렇다면 큰 목표를 택해야 한다.",
            "나는 공학적으로 불가능하다고 여겼던 일에 도전한다. 불가능을 깬 곳에 가장 큰 성장이 존재한다.",
            "직원과 동료가 존경할 만한 리더가 되면, 좋은 인재가 모이고 그게 결국 더 큰 성과와 부로 이어진다.",
            "집요하게, 그리고 집착하듯이 문제를 파고들면 돈을 벌어야지라는 생각보다 빠르게 부에 도달할 수 있다.",
            "당신의 아이디어가 말도 안 된다고 비웃음을 사더라도, 확신이 있다면 끝까지 밀어붙여라.",
            "언제나 소비자 입장에서 생각하라. 사람들이 정말 환호할 물건이면, 돈은 뒤따라온다.",
            "기술적 비전, 실행력, 그리고 인내심이 함께하면 불가능해 보이던 부도 현실이 된다.",
            "처음으로 시장에 나오는 혁신이 가장 강력하다. 그 파급력이 결국 큰 부를 창출한다.",
            "나는 자산 대부분을 새로운 프로젝트에 재투자한다. 정체되지 않고 끊임없이 움직여야 성장한다.",
            "계속해서 왜?라는 질문을 던져라. 그렇게 하면 불필요한 과정을 줄이고 효율이 극대화된다.",
            "가장 큰 리스크는 리스크를 전혀 감수하지 않는 것이다. 세상은 끊임없이 변화하니까.",
            "부유함은 스스로가 만든 가치에 대한 시장의 평가다. 먼저 가치를 만들어내는 데 집중하라.",
            "당신이 도전하는 분야가 정말 중요한지부터 확인하라. 의미 있는 분야일수록 더 큰 경제적 성취가 뒤따른다.",
            "문제 해결 속도가 경쟁우위다. 빨리 해결하면 시장에서 먼저 가치를 인정받는다.",
            "주변 사람들에게 내 프로젝트에 왜 투자할 가치가 있는가?를 설명할 수 있다면, 이미 반은 성공이다.",
            "나는 편안함을 유지하는 사람보다 시도하다가 실패하는 사람과 일하고 싶다.",
            "성공 여부를 가르는 건 운보다는 끈질긴 실행력이다. 운은 그 다음 이야기다.",
            "위대한 팀은 재능 있는 개인보다 훨씬 큰 가치를 만들어낸다. 부도 그 결과물이다.",
            "중요한 건 얼마나 빨리 이루는가보다 얼마나 크게 변화시킬 것인가이다.",
            "시장에 뛰어들기 전, 스스로에게 이게 진짜 가치 있는 일인가?를 끊임없이 물어봐라.",
            "타인을 설득하기 전, 먼저 자신이 확신해야 한다. 자신이 믿지 않으면 아무도 믿지 않는다.",
            "좋은 아이디어가 떠오르면 즉시 실행 계획을 짜라. 시간을 끌수록 경쟁자만 늘어난다.",
            "나는 끊임없이 핵심 가정들을 깨부순다. 그 과정을 통해 더 나은 해법을 찾고, 시장을 선도한다.",
            "사람들이 말도 안 된다고 할수록, 그 안에 큰 기회가 숨어 있을 가능성이 높다.",
            "부자가 되고 싶다면, 더 큰 문제를 더 효율적으로 해결하라. 시장은 그 가치를 절대 그냥 두지 않는다.",
            "작은 성취에 안주하면 대담한 혁신은 일어나지 않는다. 위험을 감수하되, 합리적으로 움직여라.",
            "창업가는 이 문제는 어떻게 풀지?라는 질문을 끊임없이 던지는 사람이다. 그 답이 곧 사업 기회다.",
            "업계를 뒤집어놓을 결정적 한 방을 노려라. 그게 만들어지면 부의 규모가 달라진다.",
            "가치 있는 일이라면, 실패 후에도 끊임없이 재도전하라. 실제로 실패를 통해 배우는 게 가장 빠르다.",
            "단기 이익이 아닌 장기적 비전을 추구해야 한다. 그 방향이 결과적으로 더 큰 부를 가져다준다.",
            "최신 기술의 흐름을 이해하고 적극적으로 활용하라. 오늘날 대규모 자산은 기술과 함께 움직인다.",
            "고객이 진정으로 원하는 것을 더 빠르고 정확하게 파악할수록, 시장에서의 영향력과 부도 커진다.",
            "미친 아이디어처럼 보이는 분야일수록 깊이 파고들어라. 남들이 안 한 영역에 진짜 기회가 숨어 있다.",
            "일에 투자하는 시간을 진심으로 즐긴다면, 재정적 보상은 자연스럽게 따라온다. 그것이 차별점이다."
        )




        val quoteList = quotes.mapIndexed { index, quote ->
            mapOf(
                "id" to index + 1,
                "richId" to richId,
                "uuid" to "d2ebbeec-35d3-4754-9445-1d17be4560a5",
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
                name = "일론 머스크",
                netWorth = "약 601조원",
                description = listOf("TESLA CEO", "SPACE X", "PayPal"),
                isSelected = false,
                category = 1,
                listPosition = 1
            ),
            Billionaire(
                id = 2,
                uuid = UUID.randomUUID().toString(),
                name = "제프 베이조스",
                netWorth = "약 378조원",
                description = listOf("Amazon", "e-Commerce 혁명", "고객 집착"),
                isSelected = false,
                category = 1,
                listPosition = 2
            ),
            Billionaire(
                id = 3,
                uuid = UUID.randomUUID().toString(),
                name = "베르나르 아르노",
                netWorth = "약 342조원",
                description = listOf("LVMH 회장", "명품 제국 운영", "유럽 최고 부호"),
                isSelected = false,
                category = 1,
                listPosition = 3
            ),
            Billionaire(
                id = 4,
                uuid = UUID.randomUUID().toString(),
                name = "래리 엘리슨",
                netWorth = "약 308조원",
                description = listOf("Oracle 창업자", "DB·Cloud 사업"),
                isSelected = false,
                category = 1,
                listPosition = 4
            ),
            Billionaire(
                id = 5,
                uuid = UUID.randomUUID().toString(),
                name = "마크 저커버그",
                netWorth = "약 287조원",
                description = listOf("Meta CEO", "Instagram"),
                isSelected = false,
                category = 1,
                listPosition = 5
            ),
            Billionaire(
                id = 6,
                uuid = UUID.randomUUID().toString(),
                name = "세르게이 브린",
                netWorth = "약 209조원",
                description = listOf("Google 공동 창업자", "검색·AI 혁신", "연구·미래기술 투자"),
                isSelected = false,
                category = 1,
                listPosition = 6
            ),
            Billionaire(
                id = 7,
                uuid = UUID.randomUUID().toString(),
                name = "스티브 발머",
                netWorth = "약 205조원",
                description = listOf("MS 전 CEO", "공격적 경영", "NBA LA 클리퍼스 구단주"),
                isSelected = false,
                category = 1,
                listPosition = 7
            ),
            Billionaire(
                id = 8,
                uuid = UUID.randomUUID().toString(),
                name = "워런 버핏",
                netWorth = "약 200조원",
                description = listOf("Berkshire Hathaway 회장", "가치투자 전설", "오마하의 현인"),
                isSelected = false,
                category = 1,
                listPosition = 8
            ),
            Billionaire(
                id = 9,
                uuid = UUID.randomUUID().toString(),
                name = "젠슨 황",
                netWorth = "약 141조원",
                description = listOf("NVIDIA 창업자·CEO", "GPU·AI 혁신 주도", "대만계 미국인 성공 스토리"),
                isSelected = false,
                category = 1,
                listPosition = 9
            ),
            Billionaire(
                id = 10,
                uuid = UUID.randomUUID().toString(),
                name = "빌 게이츠",
                netWorth = "약 138조원",
                description = listOf("MS 공동 창업자", "세계 최대 자선재단 운영", "글로벌 보건·교육 기여"),
                isSelected = false,
                category = 1,
                listPosition = 10
            )
        )
        billionaireViewModel.insertMultipleBillionairesToFirestore(billionaireList)
    }


}