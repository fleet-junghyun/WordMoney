package com.be.hero.wordmoney.userData

import android.app.Application
import android.content.Context
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(application: Application) {

    private val db = FirebaseFirestore.getInstance()


    // ✅ Firestore에서 특정 사용자의 팔로우한 부자 리스트 가져오기
    suspend fun getFollowingList(token: String): List<String> {
        return try {
            val document = db.collection("users").document(token).get().await()
            document.get("following") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }


    // ✅ Firestore에 특정 사용자의 FCM 토큰 저장 (신규 or 업데이트)
    fun saveUserToken(token: String) {
        val userRef = db.collection("users").document(token)

        userRef.get().addOnSuccessListener { document ->
            val existingFollowing = document.get("following") as? MutableList<String> ?: mutableListOf()

            val userData = hashMapOf(
                "token" to token,
                "following" to existingFollowing, // ✅ 기존 팔로우 리스트 유지
                "timestamp" to System.currentTimeMillis() // ✅ 최신 업데이트 시간 저장
            )

            userRef.set(userData).addOnSuccessListener {
                println("✅ Firestore에 Token 저장 완료: $token")
            }.addOnFailureListener { e ->
                println("❌ Firestore 저장 실패: ${e.message}")
            }
        }
    }

    // ✅ 특정 부자 팔로우/언팔로우
    fun followBillionaire(token: String, billionaireUUID: String, isFollowing: Boolean, onComplete: (Boolean) -> Unit) {
        val userRef = db.collection("users").document(token)

        val updateData = if (isFollowing) {
            mapOf("following" to FieldValue.arrayUnion(billionaireUUID)) // ✅ 추가
        } else {
            mapOf("following" to FieldValue.arrayRemove(billionaireUUID)) // ❌ 제거
        }

        userRef.update(updateData)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }



    companion object {
        private var INSTANCE: UserRepository? = null
        fun get(context: Context) = get(context.applicationContext as Application)
        fun get(application: Application) = INSTANCE ?: synchronized(this) {
            UserRepository(application).also {
                INSTANCE = it
            }
        }
    }
}