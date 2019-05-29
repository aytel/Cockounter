package com.example.cockounter.network

import com.example.cockounter.core.GameState
import com.example.cockounter.core.Preset
import com.example.cockounter.core.StateCapture
import com.example.cockounter.core.StateCaptureConverter
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.*

class NetworkHandler {
    companion object {
        private const val BASE_URL = "aytel-cockounterserver.herokuapp.com"
        private const val CREATE_SESSION = "$BASE_URL/create/%s"
        private const val UPDATE_GAME_STATE = "$BASE_URL/update_gs/%s"
        private const val CONNECT_TO_SESSION = "$BASE_URL/connect/%s"
        private const val GET_GAME_SESSION = "$BASE_URL/get/%s"

        val client = HttpClient()

        fun createGame(stateCapture: StateCapture): Boolean {
            return runBlocking(Dispatchers.IO) {
                client.get<String>(CREATE_SESSION.format(StateCaptureConverter.gson.toJson(stateCapture)))
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

        fun updateGameState(uuid: UUID, version: Int, gameState: GameState): GameState {
            return StateCaptureConverter.gson.fromJson(runBlocking(Dispatchers.IO) {
                client.post<String>(UPDATE_GAME_STATE.format(uuid.toString())) {
                    parameter("version", version.toString())
                    parameter("data", StateCaptureConverter.gson.toJson(gameState))
                }
            }, GameState::class.java)
        }

    }
}