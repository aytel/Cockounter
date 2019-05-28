package com.example.cockounter.script

import com.example.cockounter.core.GameRole
import com.example.cockounter.core.GameState
import com.example.cockounter.core.IntegerGameParameter
import com.example.cockounter.core.Player
import org.junit.Test

class ActionButtonKtTest {

    @Test
    fun evaluateScript() {
    }

    @Test
    fun checkId() {
        val state = GameState(
            mapOf("x" to IntegerGameParameter("x", 0)),
            mapOf("role" to GameRole("role", mapOf(), mapOf("player" to Player("player", mapOf()))))
        )
        val res = performScriptUsingGameState(state, "player", "global.x = global.x + 1")
        val newState = mapToGameState(mapFromGameState(state, "player"), state)

    }

}