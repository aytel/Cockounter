package com.example.cockounter.script

import org.junit.Test

import org.junit.Assert.*

class ScriptKtTest {

    @Test
    fun evaluateScript() {
    }

    @Test
    fun mapGameState() {
    }

    @Test
    fun loadScript() {
    }

    @Test
    fun testMap() {
        val m = mutableMapOf(1 to 1, 2 to 2);
        assertNotEquals(m, m.mapValues { it.value + 1 })
    }
}