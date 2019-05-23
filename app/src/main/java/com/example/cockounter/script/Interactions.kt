package com.example.cockounter.script

import android.content.Context
import arrow.core.toT
import org.jetbrains.anko.*
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction

val interactionFunctionsWithoutContext = listOf("toast" toT ::LuaToastShort, "askstring" toT ::LuaAskString)

fun buildInteractionFunctionsWithContext(context: Context) = interactionFunctionsWithoutContext.map{ it.map { it(context) }}

class LuaToastShort(val context: Context) : OneArgFunction() {
    override fun call(arg: LuaValue?): LuaValue {
        val string = arg?.tojstring() ?: ""
        context.toast(string)
        return LuaValue.NIL
    }
}

class LuaAskString(val context: Context) : OneArgFunction() {
    override fun call(arg: LuaValue?): LuaValue {
        val string = arg?.tojstring() ?: ""
        var result = LuaValue.NIL;
        with(context) {
            alert {
                customView {
                    val text = editText()
                    yesButton {
                        result = LuaValue.valueOf(text.text.toString())
                    }
                }
            }
        }
        return result
    }
}
