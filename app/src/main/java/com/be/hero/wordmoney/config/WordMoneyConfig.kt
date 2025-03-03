package com.be.hero.wordmoney.config

import android.app.Application
import android.content.Context
import android.preference.PreferenceManager
import androidx.core.content.edit

class WordMoneyConfig private constructor(application: Application) {

    private val config by lazy {
        application.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE) // ✅ 대체 코드
    }

    var isPremium
        get() = config.getBoolean(PREMIUM_USER, false)
        set(value) {
            config.edit {
                putBoolean(PREMIUM_USER, value)
            }
        }

    var isToken
        get() = config.getString(FCM_TOKEN, null)
        set(value) {
            config.edit {
                putString(FCM_TOKEN, value)
            }
        }

    var openCount
        get() = config.getInt(OPEN_COUNT, 0)
        set(value) {
            config.edit {
                putInt(OPEN_COUNT, value)
            }
        }

    var isReviewed
        get() = config.getBoolean(IS_REVIEWED, false)
        set(value) {
            config.edit { putBoolean(IS_REVIEWED, value) }
        }


    companion object {
        private const val PREF_NAME = "WordMoneyPreferences" // ✅ SharedPreferences 이름 추가
        private const val PREMIUM_USER = "Premium.User"
        private const val FCM_TOKEN = "Fcm.Token"
        private const val OPEN_COUNT = "Open.Count"
        private const val IS_REVIEWED = "Is.Reviewed"


        private var instance: WordMoneyConfig? = null
        fun get(context: Context) = get(context.applicationContext as Application)
        fun get(application: Application) = instance ?: synchronized(this) {
            WordMoneyConfig(application).also {
                instance = it
            }
        }
    }
}
