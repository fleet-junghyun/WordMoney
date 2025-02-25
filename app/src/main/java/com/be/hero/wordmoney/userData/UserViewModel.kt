package com.be.hero.wordmoney.userData

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.be.hero.wordmoney.config.WordMoneyConfig
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel (application: Application) : AndroidViewModel(application)  {

    private val repository = UserRepository.get(application)

    private val _followingList = MutableLiveData<List<String>>()
    val followingList: LiveData<List<String>> get() = _followingList

    private val config by lazy {
        WordMoneyConfig.get(application)
    }


    init {
        getToken()
    }

    // ✅ FCM 토큰 가져오기 (SharedPreferences에서 로드)
    fun getToken() {
        val savedToken = config.isToken
        // ✅ 저장된 토큰이 없으면 새로운 토큰을 가져와 저장
        if (savedToken == null) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val newToken = task.result
                    if (newToken != null) {
                        config.isToken = newToken
                    }
                }
            }
        }
    }


    // ✅ Firestore에서 특정 사용자의 팔로우 리스트 가져오기
    fun fetchFollowingList(token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = repository.getFollowingList(token)
            _followingList.postValue(list)
        }
    }

    // ✅ Firestore에 FCM 토큰 저장
    fun saveUserToken(token: String) {
        repository.saveUserToken(token)
    }

    // ✅ 팔로우/언팔로우 실행 후 UI 업데이트
    fun followBillionaire(billionaireUUID: String, isFollowing: Boolean) {
        repository.followBillionaire(config.isToken.toString(), billionaireUUID, isFollowing) { success ->
            if (success) {
                fetchFollowingList(config.isToken.toString()) // ✅ Firestore 업데이트 후 UI 반영
            }
        }
    }
}