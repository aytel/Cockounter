package com.example.cockounter.network

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.cockounter.EditPresetActivity
import com.example.cockounter.core.GameState
import com.example.cockounter.core.Preset
import com.example.cockounter.core.StateCapture
import com.example.cockounter.core.StateCaptureConverter
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.Parameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.anko.doAsync
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection

class NetworkHandler {
    companion object {
        private const val BASE_URL = "https://aytel-cockounterserver.herokuapp.com"
        private const val CREATE_SESSION = "$BASE_URL/create"
        private const val UPDATE_GAME_STATE = "$BASE_URL/update_gs"
        private const val CONNECT_TO_SESSION = "$BASE_URL/connect/%s"
        private const val GET_GAME_SESSION = "$BASE_URL/get/%s"

        private val client = HttpClient(Android)

        @RequiresApi(Build.VERSION_CODES.N)
        fun createGame(stateCapture: StateCapture): Boolean {
            /*doAsync {
                with(URL(CREATE_SESSION).openConnection() as HttpsURLConnection) {
                    requestMethod = "GET"  // optional default is GET

                    println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")

                    inputStream.bufferedReader().use {
                        it.lines().forEach { line ->
                            println(line)
                        }
                    }
                }
            }*/
            val json = StateCaptureConverter.gson.toJson(stateCapture)
            val params = Parameters.build {
                append("capture", json)
            }
            return runBlocking (Dispatchers.IO) {
                client.submitForm<String>(CREATE_SESSION, params, encodeInQuery = false)
            }.toBoolean()
        }

        fun connectToGame(uuid: UUID): StateCapture {
            return StateCaptureConverter.gson.fromJson(runBlocking(Dispatchers.IO) {
                client.get<String>(CONNECT_TO_SESSION.format(uuid.toString()))
            }, StateCapture::class.java)
        }

        fun getGameState(uuid: UUID): GameState {
            return StateCaptureConverter.gson.fromJson(runBlocking(Dispatchers.IO) {
                client.get<String>(GET_GAME_SESSION.format(uuid.toString()))
            }, GameState::class.java )
        }

        fun updateGameState(uuid: UUID, gameState: GameState): GameState {
            val json = StateCaptureConverter.gson.toJson(gameState)
            val params = Parameters.build {
                append("state", json)
                append("uuid", uuid.toString())
            }
            return StateCaptureConverter.gson.fromJson(runBlocking(Dispatchers.IO) {
                client.submitForm<String> (UPDATE_GAME_STATE, params, encodeInQuery = false)
            }, GameState::class.java)
        }

    }
}