package com.be.hero.wordmoney

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.be.hero.wordmoney.quoteAdapter.QuotePagerAdapter
import com.be.hero.wordmoney.billionaireData.BillionaireViewModel
import com.be.hero.wordmoney.data.Billionaire
import com.be.hero.wordmoney.databinding.ActivityMainBinding
import com.be.hero.wordmoney.quoteData.QuoteViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var quotePagerAdapter: QuotePagerAdapter
    private val quoteViewModel: QuoteViewModel by viewModels() // 🔥 ViewModel 사용
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
        setViewPager()


    }

    private fun setViewPager() {
        quotePagerAdapter = QuotePagerAdapter(emptyList())
        binding.viewPager.adapter = quotePagerAdapter
        binding.viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
//        quoteViewModel.allQuotes.observe(this, Observer { quotes -> quotePagerAdapter.updateQuotes(quotes) })
    }

    private fun goToMenu() {
        Intent(this, MenuActivity::class.java).run { startActivity(this) }
    }

    private fun gotoRiches() {
        Intent(this, RichesActivity::class.java).run { startActivity(this) }
    }

    private fun insertElonMuskQuotesToFirestore() {
        val firestore = FirebaseFirestore.getInstance()

        // 🔥 Elon Musk의 UUID (이 값은 Firestore에서 확인 후 넣어주세요)
        val elonMuskUUID = "c30f4a76-307c-4bb3-aba6-e48c75cbe363" // 🔴 Firestore에서 가져온 UUID로 변경해야 함

        // 🔥 Elon Musk의 명언 데이터 리스트 (10개)
        val quoteList = listOf(
            mapOf(
                "id" to 1,
                "richId" to 1, // Elon Musk의 ID
                "uuid" to UUID.randomUUID().toString(),
                "quote" to "나는 결코 포기하지 않는다. 절대 아니다.",
                "author" to "Elon Musk",
                "isBookmarked" to false
            ),
            mapOf(
                "id" to 2,
                "richId" to 1,
                "uuid" to UUID.randomUUID().toString(),
                "quote" to "위험을 감수하지 않으면 평범한 삶을 살게 된다.",
                "author" to "Elon Musk",
                "isBookmarked" to false
            ),
            mapOf(
                "id" to 3,
                "richId" to 1,
                "uuid" to UUID.randomUUID().toString(),
                "quote" to "가장 큰 실수는 도전하지 않는 것이다.",
                "author" to "Elon Musk",
                "isBookmarked" to false
            ),
            mapOf(
                "id" to 4,
                "richId" to 1,
                "uuid" to UUID.randomUUID().toString(),
                "quote" to "지속적인 혁신이 없다면 도태될 것이다.",
                "author" to "Elon Musk",
                "isBookmarked" to false
            ),
            mapOf(
                "id" to 5,
                "richId" to 1,
                "uuid" to UUID.randomUUID().toString(),
                "quote" to "성공을 확신하지 못해도 시도해야 한다.",
                "author" to "Elon Musk",
                "isBookmarked" to false
            ),
            mapOf(
                "id" to 6,
                "richId" to 1,
                "uuid" to UUID.randomUUID().toString(),
                "quote" to "나의 목표는 단순한 것이 아니다. 나는 인류의 미래를 바꿀 것이다.",
                "author" to "Elon Musk",
                "isBookmarked" to false
            ),
            mapOf(
                "id" to 7,
                "richId" to 1,
                "uuid" to UUID.randomUUID().toString(),
                "quote" to "비판을 받아들이고, 더 나은 방향으로 나아가라.",
                "author" to "Elon Musk",
                "isBookmarked" to false
            ),
            mapOf(
                "id" to 8,
                "richId" to 1,
                "uuid" to UUID.randomUUID().toString(),
                "quote" to "대부분의 사람들이 실패하는 이유는 실행하지 않기 때문이다.",
                "author" to "Elon Musk",
                "isBookmarked" to false
            ),
            mapOf(
                "id" to 9,
                "richId" to 1,
                "uuid" to UUID.randomUUID().toString(),
                "quote" to "기술의 발전은 필수적이다. 우리는 멈출 수 없다.",
                "author" to "Elon Musk",
                "isBookmarked" to false
            ),
            mapOf(
                "id" to 10,
                "richId" to 1,
                "uuid" to UUID.randomUUID().toString(),
                "quote" to "불가능하다고 생각되는 일을 해내야만 혁신이 이루어진다.",
                "author" to "Elon Musk",
                "isBookmarked" to false
            )
        )

        val documentRef = firestore.collection("quotes").document(elonMuskUUID)

        // 데이터를 한 번에 저장 (배치)
        documentRef.set(mapOf("quotes" to quoteList))
            .addOnSuccessListener {
                println("🔥 Elon Musk 명언 10개 Firestore 저장 완료!")
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