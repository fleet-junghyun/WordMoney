package com.be.hero.wordmoney.userData

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.be.hero.wordmoney.config.WordMoneyConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository.get(application)

    private val _followingList = MutableLiveData<List<String>>()

    private val config by lazy {
        WordMoneyConfig.get(application)
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
        config.isToken = token
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