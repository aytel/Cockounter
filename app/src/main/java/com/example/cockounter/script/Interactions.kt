package com.example.cockounter.script

import android.content.Context
import arrow.core.toT
import org.jetbrains.anko.*
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.VarArgFunction

val interactionFunctionsWithoutContext = listOf("toast" toT ::LuaToastShort, "askstr" toT ::LuaAskString, "select" toT ::LuaSelect, "confirm" toT ::LuaConfirm)

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

//FIXME
class LuaSelect(val context: Context) : VarArgFunction() {
    private var isDone = false
    private fun selector(args: Array<String>): Int {
        var result = -1
        val block = Object()
        synchronized(block) {
            isDone = false
        }
        with(context) {
            runOnUiThread {
                selector(args[0], args.drop(1)) { _, i ->
                    synchronized(block) {
                        isDone = true
                        result = i
                        block.notify()
                    }
                }
            }
        }
        synchronized(block) {
            while(!isDone) {
                block.wait()
            }
        }
        return result
    }
    override fun invoke(args: Array<out LuaValue>?): Varargs {
        return LuaValue.valueOf(selector(args?.map { it.tojstring()!! }?.toTypedArray() ?: arrayOf()))
    }
}

class LuaConfirm(val context: Context) : OneArgFunction() {
    private fun ask(text: String): Int {
        var isDone = false
        var result = -1
        val block = Object()
        synchronized(block) {
            isDone = false
        }
        with(context) {
            runOnUiThread {
                alert {
                    message = text
                    yesButton {
                        synchronized(block) {
                            isDone = true
                            result = 0
                            block.notify()
                        }
                    }
                    noButton {
                        synchronized(block) {
                            isDone = true
                            result = 1
                            block.notify()
                        }
                    }
                    onCancelled {
                        synchronized(block) {
                            isDone = true
                            result = -1
                            block.notify()
                        }
                    }
                }
            }
        }
        synchronized(block) {
            while (!isDone) {
                block.wait()
            }
        }
        return result
    }

    override fun call(arg: LuaValue?): LuaValue {
        return LuaValue.valueOf(ask(arg?.tojstring() ?: ""))
    }

}
