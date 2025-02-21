package com.be.hero.wordmoney.widget

import android.app.Application
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.be.hero.wordmoney.MainActivity
import com.be.hero.wordmoney.R
import com.be.hero.wordmoney.quoteData.QuoteViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuoteWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val application = context.applicationContext as Application
        val viewModel = QuoteViewModel(application) // ✅ ViewModel 사용

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, viewModel)
        }
    }

    override fun onEnabled(context: Context) {
        // ✅ 위젯이 추가될 때 기본 데이터 설정
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, QuoteWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        val application = context.applicationContext as Application
        val viewModel = QuoteViewModel(application)
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, viewModel)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_REFRESH_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, QuoteWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            val application = context.applicationContext as Application
            val viewModel = QuoteViewModel(application)

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, viewModel)
            }
        }
    }

    companion object {
        private const val ACTION_REFRESH_WIDGET = "com.be.hero.wordmoney.ACTION_REFRESH_WIDGET"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            viewModel: QuoteViewModel
        ) {
            val remoteViews = RemoteViews(context.packageName, R.layout.widget_layout)

            CoroutineScope(Dispatchers.IO).launch {
                val randomQuote = viewModel.getRandomQuoteSync() // ✅ 즉시 데이터 가져오기

                withContext(Dispatchers.Main) {
                    remoteViews.setTextViewText(R.id.widget_text, randomQuote?.quote ?: "명언 없음")
                    remoteViews.setTextViewText(R.id.widget_author, randomQuote?.author ?: "")
                    appWidgetManager.updateAppWidget(appWidgetId, remoteViews)

                    // ✅ 위젯 클릭 시 MainActivity 실행
                    val intent = Intent(context, MainActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(
                        context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    remoteViews.setOnClickPendingIntent(R.id.widget_root, pendingIntent) // ✅ 위젯 전체 클릭 가능

                    // ✅ Refresh 버튼 클릭 이벤트 설정
                    val refreshIntent = Intent(context, QuoteWidgetProvider::class.java).apply {
                        action = ACTION_REFRESH_WIDGET
                    }
                    val refreshPendingIntent = PendingIntent.getBroadcast(
                        context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    remoteViews.setOnClickPendingIntent(R.id.widget_refresh_button, refreshPendingIntent)

                    appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
                }
            }
        }
    }
}

