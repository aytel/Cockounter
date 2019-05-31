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
        with(context) {
            runOnUiThread {
                context.toast(string)
            }
        }
        return LuaValue.NIL
    }
}

class LuaAskString(val context: Context) : OneArgFunction() {
    private var result: String? = null;
    private var isDone = false
    fun alert(title: String): String? {
        with(context) {
            val block = Object()
            synchronized(block) {
                isDone = false
            }
            runOnUiThread {
                alert(title) {
                    customView {
                        val text = editText()
                        yesButton {
                            synchronized(block) {
                                result = text.text.toString()
                                isDone = true
                                block.notify()
                            }
                        }
                        onCancelled {
                            synchronized(block) {
                                isDone = true
                                block.notify()
                            }
                        }
                    }
                }.show()
            }
            synchronized(block) {
                while(!isDone) {
                    block.wait()
                }
            }
        }
        return result
    }

    override fun call(arg: LuaValue?): LuaValue {
        val string = arg?.tojstring() ?: ""
        return LuaValue.valueOf(alert(string))
    }
}
