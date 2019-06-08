package com.example.cockounter.script

import com.example.cockounter.core.ScriptContext

/**
 * Class that indicates an action to perform
 */
sealed class Action {
    /**
     * Simple Lua script
     *
     * @param context context with that script will be executed
     * @param script script source
     */
    data class PlayerScript(val context: ScriptContext, val script: String) : Action()
}
