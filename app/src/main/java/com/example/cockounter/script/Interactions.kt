package com.example.cockounter.script

import android.content.Context
import arrow.core.toT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.async
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import kotlin.coroutines.Continuation
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
    private var result: String? = null;
    fun alert(title: String) {
        with(context) {
            alert(title) {
                customView {
                    val text = editText()
                    yesButton {
                        result = text.text.toString()
                    }
                }
            }.show()
        }
    }

    //FIXME
    override fun call(arg: LuaValue?): LuaValue {
        val string = arg?.tojstring() ?: ""
        var answer = LuaValue.NIL
        answer = LuaValue.valueOf(result)
        return answer
    }
}
