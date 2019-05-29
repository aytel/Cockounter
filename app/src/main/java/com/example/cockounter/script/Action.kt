package com.example.cockounter.script

import com.example.cockounter.core.ScriptContext

sealed class Action {
    data class PlayerScript(val context: ScriptContext, val function: FunctionDescriptor) : Action()
}

data class FunctionDescriptor(val args: List<String>) {
    fun tail(): FunctionDescriptor = FunctionDescriptor(args.drop(1))
    fun isRoot() = args.size == 1
    val begin: String
    get() = args[0]
}
