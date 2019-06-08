package com.example.cockounter.network

import android.util.Log
import com.example.cockounter.MultiPlayerGameViewModel
import com.example.cockounter.core.GameState
import com.example.cockounter.core.StateCaptureConverter
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class StateUpdaterFirebaseMessagingService: FirebaseMessagingService() {
    companion object {
        var token: String? = null
        set(value) {
            NetworkHandler.changeToken(multiPlayerGameViewModel?.uuid, field, value)
            field = value
        }

        var multiPlayerGameViewModel: MultiPlayerGameViewModel? = null
    }

    override fun onNewToken(token: String) {
        Log.d("", "token = $token")
        super.onNewToken(token)
        StateUpdaterFirebaseMessagingService.token = token
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.w("", "data = ${remoteMessage.data}")
        if (remoteMessage.data.isNotEmpty()) {
            multiPlayerGameViewModel?.updateGameState(StateCaptureConverter.gson.fromJson(
                remoteMessage.data["state"],
                GameState::class.java
            ))
        }
    }

    fun setToken() {
        val tokenGetter = FirebaseInstanceId.getInstance().instanceId
            .addOnSuccessListener { result ->

                // Get new Instance ID token
                token = result.token

                // Log and toast
                //val msg = getString(R.string.msg_token_fmt, token)
                Log.d("", "token = ${result.token}")
            }

        while (!tokenGetter.isSuccessful) {
            Thread.sleep(100)
        }
        token = tokenGetter.result?.token
    }
}