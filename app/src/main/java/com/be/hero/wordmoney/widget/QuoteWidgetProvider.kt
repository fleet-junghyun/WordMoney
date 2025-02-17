package com.be.hero.wordmoney.widget

import android.app.Application
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.be.hero.wordmoney.R
import com.be.hero.wordmoney.billionaireData.AppDatabase
import com.be.hero.wordmoney.quoteData.Quote
import com.be.hero.wordmoney.quoteData.QuoteRepository
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

    companion object {
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
                }
            }
        }
    }
}

