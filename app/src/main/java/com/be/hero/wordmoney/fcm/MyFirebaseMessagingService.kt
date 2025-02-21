package com.be.hero.wordmoney.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.be.hero.wordmoney.MainActivity
import com.be.hero.wordmoney.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "✅ 새 FCM 토큰: $token")
        saveTokenToFirestore(token)  // Firestore에 저장
    }

    private fun saveTokenToFirestore(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val userRef = db.collection("users").document(userId)
        userRef.update("fcmToken", token)
            .addOnSuccessListener { Log.d("Firestore", "✅ FCM 토큰 저장 성공") }
            .addOnFailureListener { e -> Log.e("Firestore", "❌ FCM 토큰 저장 실패", e) }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "✅ 푸시 메시지 수신: ${remoteMessage.data}")

        val title = remoteMessage.notification?.title ?: "새로운 명언"
        val message = remoteMessage.notification?.body ?: "명언을 확인하세요!"

        showNotification(title, message)
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "wordmoney_fcm_channel"
        val notificationId = Random.nextInt()

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "WordMoney 알림", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_rich_mans)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}