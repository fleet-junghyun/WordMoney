package com.be.hero.wordmoney.widget

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.be.hero.wordmoney.R
import com.be.hero.wordmoney.quoteData.Quote
import com.be.hero.wordmoney.quoteData.QuoteRepository
import com.be.hero.wordmoney.quoteData.QuoteViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class WidgetUpdateWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val componentName = ComponentName(applicationContext, QuoteWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        val application = applicationContext.applicationContext as Application
        val repository = QuoteRepository.get(application) // ✅ Repository 직접 사용

        runBlocking {
            val quote = repository.getRandomQuote() // ✅ 즉시 데이터 가져오기
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(application, appWidgetManager, appWidgetId, quote)
            }
        }

        return Result.success()
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        quote: Quote?
    ) {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_layout)

        remoteViews.setTextViewText(R.id.widget_text, quote?.quote ?: "명언 없음")
        remoteViews.setTextViewText(R.id.widget_author, quote?.author ?: "")

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }

    companion object {
        fun scheduleWidgetUpdate(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(3, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .setRequiresDeviceIdle(false)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "widget_update_work",
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
    }
}

