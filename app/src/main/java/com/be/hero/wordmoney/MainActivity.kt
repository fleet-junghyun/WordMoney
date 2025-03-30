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
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
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

        initTapTargetView()

//        val uuid = UUID.randomUUID().toString()
//        Log.d("uuid", uuid.toString())
//        insertFireStoreRiches()
//        insertQuotesToFirestore()


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

    private fun initTapTargetView() {
        if (!config.isFirst) {
            TapTargetView.showFor(this, TapTarget.forView(binding.riches, "", "").outerCircleAlpha(0.2f).outerCircleColor(R.color.white)
                .targetCircleColor(R.color.black).drawShadow(true).cancelable(false).tintTarget(false).targetRadius(80).dimColor(R.color.black),
                object : TapTargetView.Listener() {
                    override fun onTargetClick(view: TapTargetView) {
                        super.onTargetClick(view)
                        config.isFirst = true
                        gotoRiches()
                    }
                })
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

    private fun insertQuotesToFirestore() {
        val firestore = FirebaseFirestore.getInstance()
        val authorUUID = "819b698c-b6c6-4730-9df8-ca3bd5dae684" // 🔴 Firestore에서 가져온 UUID로 변경해야 함
        val author = "야나이 타다시"
        val richId = 18

        val quotes = listOf(
            "실패하더라도 회사가 망하지 않으면 됩니다. 실패할 거라면 빨리 경험하는 편이 낫습니다. 빨리 실패하고, 빨리 깨닫고, 빨리 수습하는 것이 성공 비결입니다.",
            "옷은 멋스럽게 잘 입어야 한다는 상식을 깨고 싶었다. 옷은 패션이 아니다. 그저 생필품일 뿐이다.",
            "꿈은 천국에 가깝고, 현실은 지옥에 가깝다",
            "사람이 상상할 수 있는 것이라면 사람이 확실히 이룰 수 있다.",
            "옷을 바꾸고, 상식을 바꾸고, 세상을 바꾼다.",
            "**CEO를 꿈꾸는 것만으로 눈빛이 달라지고, 행동이 달라진다.**",
            "가장 많은 것들을 가르쳐주는 것은 고객이다.",
            "사장이 지시한 것을 현장의 직원들이 곧이곧대로 실행하는 회사는 틀림없이 망한다.직원들이 \"사장님 그건 틀렸습니다.\"라고 편하게 이야기 할 수 있는 회사가 되어야 한다. 안 그러면 회사가 어느 새 잘못된 방향으로 나아간다.",
            "지금 보다 더 좋은 방법은 없을까 하고 끝없이 자문해야 진보와 성장이 있다.",
            "과거에 성취한 작은 성공에 안주한다면 거기서 끝나고 만다.",
            "승리는 단 한 번이면 충분하다. 아홉 번의 실패가 나를 더 강하게 만든다.",
            "우리는 언제나 변화의 한가운데 있어야 한다. 멈추는 순간 퇴보가 시작된다.",
            "사장이든 알바생이든, 모두가 ‘점원’이라는 자세로 고객과 마주해야 한다.",
            "고객이 원한다면, 기존 상식을 모두 부숴서라도 해결책을 찾아라.",
            "인재에게 아끼지 말아야 한다. 사람에 대한 투자는 절대 헛되지 않는다.",
            "반복되는 실패가 있었기에 혁신이 가능했다.",
            "초심을 잃지 않는 비결은, 매일 매장의 변화를 직접 확인하는 것이다.",
            "전문지식이 부족하면 더 많이 배우면 된다. 중요한 것은 배우려는 자세다.”",
            "궁극적으로는 옷이 아니라 생활을 창조해야 한다.",
            "성공의 반대말은 실패가 아니라 ‘도전하지 않음’이다.",
            "유니클로가 세계를 바꾼다기보다, 세계의 변화를 유니클로가 앞서 체감해야 한다.",
            "늘 현장부터 살펴라. 현장에 모든 답이 있다.",
            "빠른 결정과 느긋한 실행이 아니라, 빠른 결정과 빠른 실행이야말로 성공의 열쇠다.",
            "직원이 성장하지 않으면 회사도 성장할 수 없다.",
            "세계 제일이 되고 싶다면, 세계 어디에서도 통할 수 있는 서비스를 해야 한다.",
            "새로운 가치는 보통사람의 작은 불편함에서 탄생한다.",
            "어제 잘됐다’고 해서 오늘도 잘된다는 보장은 전혀 없다.",
            "나는 언제나 내가 틀릴 수 있다고 생각한다. 그렇기에 끊임없이 검증하고 변화를 시도한다.",
            "가장 큰 리스크는 리스크를 무서워하는 마음 그 자체다.",
            "항상 이제부터 시작이다 라는 마음으로 다시 출발해야 한다.",
            "회사 경영은 장기전이다. 한 번의 성공에 자만해선 안 된다.",
            "사람은 옷을 입고, 옷으로 표현된다. 우리가 파는 것은 옷 이상의 무엇이다.",
            "시장의 흐름을 좇기보다, 시장이 따르게끔 만들어야 한다.",
            "혼자 할 수 있는 일은 거의 없다. 좋은 동료가 모였을 때 비로소 가치가 배가된다.",
            "나는 고객 클레임을 가르침으로 받아들인다.",
            "새로운 계획은 작게 시작하되, 빠르게 확대할 기회를 항상 노려라.",
            "현지에 맞추기보다는 전 세계가 공감할 수 있는 표준을 만들고자 한다.",
            "정체된 순간이 찾아올수록 더 많이 배워야 한다. 책상 앞이 아니라 현장에서.",
            "직장인이라면 누구나 사장이 될 각오로 일해야 한다.",
            "옷은 생활을 윤택하게 만드는 도구다. 더 나아가 삶의 방식을 제안해야 한다.",
            "불가능에 대한 선입견이야말로 가장 큰 걸림돌이다.",
            "문제가 생겼을 때 한 단계 더 파고들면, 그 안에 성장의 기회가 보인다.",
            "나는 우리가 할 수 있는 최고를 계속 갱신해야 한다고 생각한다.",
            "아무리 훌륭한 전략도 실행이 뒤따르지 않으면 무의미하다.",
            "직급이나 나이에 상관없이, 가장 좋은 아이디어가 최우선이 되는 조직을 원한다.",
            "성공이란 내가 가진 것으로 할 수 있는 최선을 끊임없이 추구하는 과정이다.",
            "늘 고객에게 배우는 태도를 유지해야, 조직도 개인도 성장할 수 있다.",
            "회사를 경영한다는 것은 매일이 실험의 연속이다. 실패를 두려워하지 말라.",
            "글로벌 기업이 되려면, 먼저 사고방식부터 글로벌해야 한다.",
            "혁신은 누군가가 하겠지, 하고 기다리면 절대 오지 않는다. 직접 만들어내야 한다.",
            "완벽함을 추구하기보다는, 빠른 실행으로 개선과 실패를 반복하는 편이 낫다.",
            "침체기에는 허리띠를 졸라매는 것만이 능사가 아니다. 새로운 기회를 찾아 과감히 투자해야 한다.",
            "기업은 수익만 추구해선 안 된다. 사회 전체에 대한 기여가 곧 경쟁력이다.",
            "직원들의 다양한 배경과 재능을 모을 수 있을 때, 비로소 다이내믹한 혁신이 가능해진다.",
            "제품을 팔지 말고, ‘가치’를 팔아라. 제품은 시간이 지나면 낡지만, 가치는 지속된다.",
            "잘못된 결정을 했다면 빨리 수정하라. 고집하다가 더 큰 실패를 부르게 된다.",
            "혁신을 원한다면 불편하고 비효율적인 부분을 먼저 파악하라.",
            "나는 오너가 아니라, 가장 앞에 서 있는 점장이라고 생각한다.",
            "매일 ‘내가 고객이라면 무엇이 불편할까’를 되묻는 것이 기본이다.",
            "결국은 사람이다. 사람을 성장시키고, 사람과 함께 가는 회사가 오래 살아남는다.",
            "기업이란 사람이 모이는 장소지만, 목표와 철학이 없으면 단지 모임에 그칠 뿐이다.",
            "고객이 원하는 것은 결국 옷이 아니라, 그 옷으로부터 얻는 새로운 생활경험이다.",
            "세계에는 우리가 시도조차 하지 않은 시장이 너무 많다. 항상 시야를 넓혀야 한다.",
            "일은 곧 자기 자신을 표현하는 방식이다. 본인이 믿는 바를 행동으로 옮겨야 한다.",
            "사람을 키우지 않는 회사가 성장한다는 건 어불성설이다.",
            "제품을 잘 만들어도, 판매 전략이 없다면 결국 팔리지 않는다.",
            "유통(流通)은 ‘흘려보내는 것’이 아니라, 고객과의 소통이 핵심이라는 사실을 간과해선 안 된다.",
            "위기는 기업을 망치기도 하고, 도약시키기도 한다. 결국 어떤 태도로 맞서느냐가 관건이다.",
            "특정 책 한 권이 인생을 바꿨다기보단, 수많은 책에서 한두 문장씩 배워왔다.",
            "전 세계가 격변을 겪는 시대일수록, ‘단순함’이 가장 강력한 무기가 될 수 있다.",
            "과감한 의사결정을 할 수 있도록, 구성원 각자가 책임질 수 있는 문화를 만드는 게 중요하다.",
            "어느 순간 ‘내 일이 아니다’라고 선을 긋는 순간, 성장의 기회를 스스로 차단하게 된다.",
            "기업 문화는 구호가 아니라, 최고경영자가 행동으로 본보기를 보이는 데서 시작한다.",
            "타인과 경쟁하기보다 어제의 나와 경쟁해야 한다. 조금이라도 나아지면 그것이 성장이다.",
            "내적 동기가 없는 사람에게 일은 숙제일 뿐이다. 하지만 내적 동기가 있는 사람에겐 도전이다.",
            "작더라도 ‘새로운 무언가’를 매일 시도하는 것이 혁신으로 가는 길이다.",
            "옷을 만드는 회사지만, 궁극적으로는 고객의 생활 전반을 고민해야 한다.",
            "선진시장이라고 방심하지 말고, 신흥시장이라고 기죽지 말라. 경쟁자는 결국 스스로가 만들기 마련이다.",
            "우리 비전은 항상 ‘너무 크지 않나?’ 싶을 정도여야 한다.",
            "남들이 이미 하는 것을 따라가기보다, 아무도 안 하는 것에서 길을 찾아야 한다.",
            "가장 큰 경쟁자는 바깥이 아니라, 내부의 나태함이다.",
            "조직이 전체 방향을 공유하지 못하면, 아무리 훌륭한 전략도 무용지물이다.",
            "마케팅이란 단순히 ‘판촉’이 아니라, 기업 철학을 시장에 전달하는 과정이라고 믿는다.",
            "현장에서 하루라도 멀어지면, 고객이 무슨 생각을 하는지 더 이상 알 수 없게 된다.",
            "열등감은 잘 활용하면 창의력을 높이는 연료가 된다. 무엇이 부족한지 깨닫게 해주니까.",
            "매장 수가 늘어나는 것이 목표가 아니다. 매장 하나하나가 고객에게 ‘사랑받는지’가 중요하다.",
            "정직하게 일하는 사람은 실패하더라도 다시 일어날 힘을 얻게 된다.",
            "창업자가 앞장서는 이유는 누구보다 많은 실패와 시행착오를 겪어봤기 때문이다.",
            "세계화(글로벌화)란, 단순히 언어만 바꾸는 게 아니라, 문화와 감성을 깊이 이해하는 것이다.",
            "오랫동안 한 자리에 머문 직원일수록 더 배우려는 마음가짐이 필요하다.",
            "기업이 망하는 건 경쟁사의 압박보다도 자기 혁신에 실패해서다.",
            "회사는 군중이 아니라 조직이다. 각자의 목표가 회사 전체 목표와 어우러져야 한다.",
            "문제 발생 시 책임을 회피하면 그 순간부터 내부 신뢰가 무너진다.",
            "유니클로는 누군가의 뒤를 좇기보다, 직접 새로운 길을 내는 회사가 되고자 한다.",
            "안고 있는 문제일수록 제대로 들여다봐야 해결책이 나온다. 슬쩍 덮어두면 더 커질 뿐이다.",
            "옥석(玉石)을 구분해내는 유일한 방법은, 현장의 목소리를 직접 듣는 것이다.",
            "기업을 키우는 것은 결국 주주가 아니라 고객이다.",
            "가장 효율적인 홍보는, 상품이 좋아서 고객이 알아서 소문내도록 만드는 것이다.",
            "당장의 이익을 위해 신뢰를 저버리면, 훗날 10배, 100배로 잃게 된다.",
            "시장 점유율보다 중요한 것은 고객의 마음에서 차지하는 점유율이다.",
            "어떤 일에든 꼭 A-B-C 순서대로 할 필요는 없다. 때론 과감히 순서를 바꿔야 혁신이 일어난다.",
            "과거의 성공을 고집하면, 다음 세대의 변화 속도를 따라갈 수 없다.",
            "한 분야에서 세계적 수준이 되려면, 최소한 10년 이상의 집중이 필요하다고 믿는다.",
            "어제 성공했던 전략이 오늘 또 통한다는 보장은 없다. 매일 새로워져야 한다.",
            "초심을 잃었다면, 그건 우리가 고객보다 우리 자신을 우선하기 시작했기 때문이다.",
            "직원들이 ‘회사 가치’를 몸으로 체감할 기회를 꾸준히 만들어야 한다.",
            "작은 불만이나 사소한 문제를 빠르게 해결하는 것이 결국 브랜드 이미지가 된다.",
            "절망하고 있을 시간에, 실패 원인을 분석하고 다음 스텝을 준비하는 편이 훨씬 이익이다.",
            "회사를 하나의 ‘생명체’처럼 본다면, 계속 진화해야 생존할 수 있다.",
            "열심히 하는 것은 기본이고, 제대로 하는 것이 중요하며, 새롭게 하는 것이 더 중요하다.",
            "자기를 이겨내지 못하면, 결국 남들과도 제대로 경쟁하기 어렵다.",
            "같은 실패를 세 번 반복한다면, 그건 학습이 아니라 방치다.",
            "내가 바라는 진정한 글로벌화는, 어디서든 고객이 유니클로를 자신들의 브랜드로 느끼는 것이다.",
            "완벽한 준비가 되기를 기다리다 보면, 평생 시작할 수 없다."
        )





        val quoteList = quotes.mapIndexed { index, quote ->
            mapOf(
                "id" to index + 1,
                "richId" to richId,
                "uuid" to authorUUID,
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
                id = 18,
                uuid = "819b698c-b6c6-4730-9df8-ca3bd5dae684",
                name = "야나이 타다시",
                netWorth = "65조원",
                property = 65165300000000,
                description = listOf("유니클로", "GU"),
                isSelected = false,
                category = 1,
                listPosition = 18
            )
            )

        billionaireViewModel.insertMultipleBillionairesToFirestore(billionaireList)
    }


}