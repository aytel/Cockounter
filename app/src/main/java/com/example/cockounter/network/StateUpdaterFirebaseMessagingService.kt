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

class StateUpdaterFirebaseMessagingService: FirebaseMessagingService() {K
    companion object {
        private var token: String? = null
        set(value) {
            if (multiPlayerGameViewModel != null) {
                multiPlayerGameViewModel.changeToken(field, value)
            }
            field = value
        }

        val tokenHandler = { uuid: UUID ->
            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w("", "getInstanceId failed", task.exception)
                        return@OnCompleteListener
                    }

                    // Get new Instance ID token
                    token = task.result?.token

                    // Log and toast
                    //val msg = getString(R.string.msg_token_fmt, token)
                    Log.d("", "token = $token")
                })
        }

        var multiPlayerGameViewModel: MultiPlayerGameViewModel? = null
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.w("", "token = $token")
        StateUpdaterFirebaseMessagingService.token = token
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.w("", "data = ${remoteMessage.data}")
        if (remoteMessage.data.isNotEmpty() && multiPlayerGameViewModel != null) {
            multiPlayerGameViewModel.updateGameState(StateCaptureConverter.gson.fromJson(
                remoteMessage.data["state"],
                GameState::class.java
            ))
        }
    }
}