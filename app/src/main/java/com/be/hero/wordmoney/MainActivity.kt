package com.be.hero.wordmoney

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.be.hero.wordmoney.billionaireData.Billionaire
import com.be.hero.wordmoney.billionaireData.BillionaireViewModel
import com.be.hero.wordmoney.config.WordMoneyConfig
import com.be.hero.wordmoney.databinding.ActivityMainBinding
import com.be.hero.wordmoney.quoteAdapter.QuotePagerAdapter
import com.be.hero.wordmoney.quoteData.Quote
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
    private val userViewModel: UserViewModel by viewModels()
    private val billionaireViewModel: BillionaireViewModel by viewModels()

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
        setOpenCount()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // ✅ API 33 이상에서만 실행
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun setOpenCount() {
        config.openCount = config.openCount.plus(1)
        if (config.openCount == 30 || config.openCount == 50 || config.openCount == 100) {
            if (!config.isReviewed) {
                AlertDialog.Builder(this).setTitle("리뷰를 부탁드립니다.").setMessage("앱을 사용하며 느낀 감동을 리뷰로 공유해 주세요. 여러분의 한 마디가 큰 힘이 됩니다 !!\uD83D\uDE47\u200D♂\uFE0F ").setPositiveButton("네") { dialog, _ ->
                    config.isReviewed = true
                    openReviewPage()
                    dialog.dismiss()
                }
                    .setNeutralButton("괜찮아요.") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
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
//        val authorUUID = "144a4d14-8aac-467c-928f-71616c0fb1c4" // 🔴 Firestore에서 가져온 UUID로 변경해야 함
//        val author = "필 나이트"
//        val richId = 17
//
//        val quotes = listOf(
//
//            // (1~20) 필 나이트의 ‘성과·행동’에서 배우는 돈 버는 자세
//            "신발 수입을 시작점으로 작은 시장도 열정적으로 개척하라",
//            "차별화된 브랜드 스토리를 입혀 소비자에게 감동을 주어라",
//            "유명 선수와 협업해 브랜드 이미지를 빠르게 끌어올려라",
//            "작은 자본이라도 과감히 투자해 제품 개발을 선도해보라",
//            "가격 경쟁보다 독특한 디자인·기능으로 승부하라",
//            "판권·로열티 등 계약을 꼼꼼히 챙겨 지적재산 가치를 지켜라",
//            "공급망을 안정화해 원가 관리와 품질을 동시에 잡아라",
//            "신발 외 의류·액세서리로 제품군을 넓히며 시너지를 높여라",
//            "주요 시장별 마케팅 전략을 달리해 글로벌 확장을 노려라",
//            "아이디어가 있다면 초기 시행착오를 두려워 말고 부딪쳐라",
//            "처음엔 자전거처럼, 소량 유통부터 시험해보고 확신을 얻어라",
//            "고객 피드백을 바로 반영해 기능적 가치와 감성 가치를 결합하라",
//            "현지 공장·해외 협력 등 다양한 조달방식으로 생산비를 통제하라",
//            "수익이 나면 R&D와 브랜드 구축에 다시 투자하라",
//            "직원과 함께 목표를 공유해 열정을 불러일으켜라",
//            "주변 반대에도 스스로 옳다고 믿는 길이면 밀고 나가라",
//            "작은 성공은 자부심으로, 실패는 학습으로 삼아 전진하라",
//            "스포츠를 매개로 스토리텔링을 강화해 고객 충성도를 끌어올려라",
//            "거점 매장을 통해 브랜드 철학을 체험시키고 팬덤을 만들라",
//            "IPO와 투자금을 활용해 공격적 확장을 시도하라",
//
//            // (21~65) 필 나이트의 마인드로 흔들리는 의지를 붙잡아줄 용기와 동기부여 (45개)
//            "시작이 미약해도 꾸준히 걸으면 예측 못 한 지점에 도착할 수 있다",
//            "옳은 일이라면 주변 의심보다 본인의 확신을 우선시하라",
//            "겁나는 상황에서도 발을 떼야 ‘할 수 있다’는 증거가 생긴다",
//            "실패가 두렵다면 작게 시도하면서 배움을 극대화해보자",
//            "결과가 더딜 때도 시장 반응을 확인하면 성장 방향이 보인다",
//            "열정을 느끼는 분야에서 개선점을 찾으면 얼마든지 수익으로 연결할 수 있다",
//            "큰 기회만 바라보지 말고, 자잘한 시도도 돈이 될 수 있다",
//            "사람 눈치보다가 시간을 낭비하기보다 내 실행에 집중하라",
//            "과감히 뛰어들되 불필요한 부분은 철저히 통제하라",
//            "해보고 싶은 게 있다면 최소 한 번은 실행해봐라",
//            "행동하는 사람이 우연한 기회도 잡기 쉽다",
//            "초심을 잃지 않으려면 왜 시작했는지 항상 떠올려라",
//            "멘탈이 무너질 땐 일단 멈추고, 다시 ‘할 수 있다’고 주문하라",
//            "경쟁자가 많아도, 나만의 아이디어를 꾸준히 밀면 결이 달라진다",
//            "다른 길을 추구하면 생기는 외로움도 혁신의 증거다",
//            "작게 벌어도 적립하듯, 미래 자금을 모아 안정성을 높여라",
//            "단축마라톤식 스퍼트보다 꾸준한 페이스를 유지하라",
//            "아이디어 하나가 막히면 다른 관점으로 재검토하라",
//            "잘하는 걸 더 확장해 수익 창출 기회를 만들어보라",
//            "부딪히면서 시장 반응을 살피면 실행력이 생긴다",
//            "지출을 줄이고 핵심 역량에 집중하면 의지가 오른다",
//            "빠르게 오류를 발견하면 더 빠른 개선이 가능하다",
//            "한두 번 실패는 성장을 위한 마중물일 뿐이다",
//            "꾸준함이 하루아침에 큰 도약을 이뤄낼 날이 온다",
//            "생각보다 실제 실행이 쉬운 경우도 많다",
//            "작은 달성감이 불씨를 살린다",
//            "최악을 가정해도 다시 일어설 자신만 있으면 시도하라",
//            "불안정해도 내 무기(기술·재능)가 있으면 버틸 힘이 생긴다",
//            "주변이 다 말려도 스스로 믿으면 도전해볼 만하다",
//            "두려워도 뛰어들 때 시장은 반응한다",
//            "생각만 하면 시계는 계속 돈다, 움직여 교훈을 얻어라",
//            "잔 성공들이 모여 큰 결과를 만든다",
//            "해결책을 고민하면 의심이 줄어든다",
//            "조금씩 움직이면 어제보다 나아지는 오늘이 있다",
//            "고민만으론 답이 안 나온다, 행동으로 근거를 쌓아라",
//            "작은 리스크로 빠르게 습득하면 큰 도전에 대비할 수 있다",
//            "방황하더라도 목표를 조정해 다시 앞을 향해라",
//            "과정에 몰입하면 부담도 줄고 재미도 커진다",
//            "늦은 출발이라도 실행에 박차를 가하면 충분히 만회된다",
//            "지금 할 수 있는 작은 일을 꾸준히 하면 다음 단계가 열린다",
//            "부업이나 사이드 프로젝트로 전문성을 쌓아 비상구를 마련하라",
//            "중간에 포기하면 그간의 노력이 무의미해진다",
//            "모두 어렵다고 해도 반대로 통하는 길을 찾을 수 있다",
//
//            // (66~100) 힘든 세상 속에 부자가 되고 싶은 사람들을 위해 ‘필 나이트’가 해줄 수 있는 위로 (35개)
//            "작은 시도로도 의외의 시장을 열 수 있다고 믿어라",
//            "퇴근 후·주말 시간에 틈새 시장을 온라인으로 공략해보라",
//            "앞이 안 보여도 한 발씩 나가면 길이 생긴다",
//            "이럴 때 더 철저히 준비하면 생존율이 올라간다",
//            "꾸준히 행동하면 그 자체가 경쟁력이 된다",
//            "대다수가 시행착오를 거쳐 성공한다는 걸 기억하라",
//            "처음부터 완벽한 사람은 없다, 배움을 멈추지 마라",
//            "불황이어도 핵심가치를 지키면 회복기에 훅 치고 나갈 수 있다",
//            "똑같은 방식이 아니어도 나만의 운영법을 만들면 된다",
//            "매출이 잠시 줄어도 본질이 탄탄하면 되살아날 여력이 있다",
//            "보잘것없어 보여도 진심을 다하면 고객은 알아준다",
//            "현장과 직접 연결해 문제를 빨리 확인하고 해결하라",
//            "경제가 안 좋아도 기초 역량을 갈고닦으면 회복 후 큰 성장 가능",
//            "눈에 띄는 성과가 없다고 흐트러지지 말고 작은 걸음에 집중하라",
//            "환경 탓 대신 내 이익을 창출할 한두 가지를 고민해보라",
//            "소규모라도 해보면 부담이 덜하고 실행이 빨라진다",
//            "사람들이 귀찮아하거나 복잡해하는 문제를 푸는 게 돈이 된다",
//            "회사 생활에서도 해법을 제시하면 추가 보상을 받을 수 있다",
//            "한두 번 부서지더라도 재도전하면 그만큼 빨라진다",
//            "작은 규모에서도 이윤이 나면 마음이 훨씬 편해진다",
//            "인간의 욕구는 사라지지 않는다, 본질적 가치를 찾아라",
//            "아주 조금씩이라도 개선된다면 계속할 가치가 있다",
//            "늦었다고 생각해도 집중하면 단숨에 따라잡을 수 있다",
//            "의심이 커도 실천하면 배움이 자산으로 남는다",
//            "직장에서 부가 아이디어를 내거나 사이드 잡으로 확장하라",
//            "어려운 시장일수록 경쟁자가 주춤해 틈새가 생길 수 있다",
//            "고객에게 다가가면 문제 해결이 훨씬 빨라진다",
//            "부정적 뉴스 속에서도 활용할 수 있는 정보를 찾을 수 있다",
//            "협업으로 부담을 나누면 한계를 넘을 수 있다",
//            "시간이 없다고 미루지 말고 조금씩이라도 시도하라",
//            "행동해야 운이 머물 자리가 생긴다",
//            "지식에 투자하면 언제든 써먹을 수 있는 무기가 생긴다",
//            "세상이 삭막해도 남이 못 보는 시장을 보면 기회가 온다",
//            "주변 없이도 할 수 있는 것부터 하면 자립 기반이 만들어진다",
//            "조금만 올라서도 상상 못 한 풍경이 보일 수 있다",
//
//            // (101~120) ‘필 나이트’가 말했거나 그의 행동에 기반한 부자가 되고 싶은 사람들에게 영감을 줄 말들 (20개)
//            "작은 수입원이라도 꾸준히 늘려가면 의외의 대박이 올 수 있다",
//            "운동선수와 협업해 스토리를 입히면 브랜드가 폭발한다고 강조했다",
//            "독특한 디자인과 기능이 가격 경쟁보다 강력하다고 믿었다",
//            "처음엔 일본 신발을 소량 수입했으나 대담히 ‘자체 브랜드’를 구축하라고 했다",
//            "우연 같아 보이지만 기회는 늘 실행 중에 찾아온다고 말해왔다",
//            "거래 계약을 꼼꼼히 챙겨야 지적재산 가치가 보호된다고 했다",
//            "브랜드에 생명을 불어넣는 건 스토리텔링이며, 그게 가치라고 밝혔다",
//            "공장 선정·해외 생산 등 다양한 루트로 원가를 낮추며 품질을 높이자고 했다",
//            "직원들도 회사의 비전에 열정을 느껴야 진짜 성과가 나온다고 말했다",
//            "주저하지 말고 부딪히면 실패도 장기적으로 큰 무기가 된다고 말했다",
//            "경쟁자의 시선보다, 소비자가 원하는 ‘이야기’에 집중하라고 했다",
//            "회사가 커져도 마음만은 창업자 시절의 ‘도전 정신’을 유지하라고 강조했다",
//            "신발만 고집하지 말고 의류·액세서리로 제품군을 확장해보라고 했다",
//            "주식 상장을 통해 자금을 확보한 뒤 브랜드 인지도에 재투자하라고 했다",
//            "현지화 전략을 철저히 세우면 해외 시장도 빠르게 개척할 수 있다",
//            "제품뿐 아니라 매장 경험을 차별화해 고객에게 감동을 주라고 강조했다",
//            "스포츠 이벤트 후원으로 브랜드 스토리를 키우는 전략이 효과적이라고 봤다",
//            "장기 비전을 세우되, 오늘 할 수 있는 일부터 바로 실행하라고 했다",
//            "‘남이 말리는 길’일수록 성공 시 보상이 크다고 여러 번 언급했다",
//            "결국 중요한 건 ‘행동 속에서 배움이 생긴다’는 점을 늘 잊지 말라고 했다"
//        )
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
//                id = 11,
//                uuid = UUID.randomUUID().toString(),
//                name = "프랑수아즈 베탕쿠르 메이예",
//                netWorth = "약 1조원",
//                description = listOf("L’Oréal", "P&G", "프랑스 시총 2위"),
//                isSelected = false,
//                category = 1,
//                listPosition = 11
//            ),
//            Billionaire(
//                id = 12,
//                uuid = UUID.randomUUID().toString(),
//                name = "래리 페이지",
//                netWorth = "약 103조원",
//                description = listOf("Google"),
//                isSelected = false,
//                category = 1,
//                listPosition = 12
//            ),
//            Billionaire(
//                id = 13,
//                uuid = UUID.randomUUID().toString(),
//                name = "아만시오 오르테가",
//                netWorth = "약 101조원",
//                description = listOf("Zara", "Inditex"),
//                isSelected = false,
//                category = 1,
//                listPosition = 13
//            ),
//            Billionaire(
//                id = 14,
//                uuid = UUID.randomUUID().toString(),
//                name = "가우탐 아다니",
//                netWorth = "약 78조원",
//                description = listOf("Adani Group"),
//                isSelected = false,
//                category = 1,
//                listPosition = 14
//            ),
//            Billionaire(
//                id = 15,
//                uuid = UUID.randomUUID().toString(),
//                name = "데이비드 톰슨",
//                netWorth = "약 70조원",
//                description = listOf("톰슨 로이터"),
//                isSelected = false,
//                category = 1,
//                listPosition = 15
//            ),
//            Billionaire(
//                id = 16,
//                uuid = UUID.randomUUID().toString(),
//                name = "마이클 델",
//                netWorth = "약 65조원",
//                description = listOf("Dell"),
//                isSelected = false,
//                category = 1,
//                listPosition = 16
//            ),
//            Billionaire(
//                id = 17,
//                uuid = UUID.randomUUID().toString(),
//                name = "필 나이트",
//                netWorth = "약 56조원",
//                description = listOf("NIKE"),
//                isSelected = false,
//                category = 1,
//                listPosition = 17
//            ),
//            Billionaire(
//                id = 18,
//                uuid = UUID.randomUUID().toString(),
//                name = "장 이밍",
//                netWorth = "약 55조원",
//                description = listOf("TIK TOK", "ByteDance"),
//                isSelected = false,
//                category = 1,
//                listPosition = 18
//            ),
//            Billionaire(
//                id = 19,
//                uuid = UUID.randomUUID().toString(),
//                name = "마 후아텡",
//                netWorth = "약 45조원",
//                description = listOf("NVIDIA 창업자·CEO", "GPU·AI 혁신 주도", "대만계 미국인 성공 스토리"),
//                isSelected = false,
//                category = 1,
//                listPosition = 19
//            ),
//            Billionaire(
//                id = 20,
//                uuid = UUID.randomUUID().toString(),
//                name = "프랑수아 피노",
//                netWorth = "약 48조원",
//                description = listOf("GUCCI", "BOTTEGA VENETA", "Yves Saint Laurent"),
//                isSelected = false,
//                category = 1,
//                listPosition = 20
//            )
//        )
//        billionaireViewModel.insertMultipleBillionairesToFirestore(billionaireList)
//    }


}