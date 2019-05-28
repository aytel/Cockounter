package com.example.cockounter.script

import com.example.cockounter.core.ScriptContext

sealed class Action {
    data class PlayerScript(val context: ScriptContext, val function: String) : Action()
}

