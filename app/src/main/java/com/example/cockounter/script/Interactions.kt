package com.example.cockounter.script

import android.content.Context
import arrow.core.toT
import org.jetbrains.anko.*
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.VarArgFunction

private val interactionFunctionsWithoutContext = listOf(
    "toast" toT ::LuaToastShort,
    "askstr" toT ::LuaAskString,
    "select" toT ::LuaSelect,
    "confirm" toT ::LuaConfirm
)

/**
 * Builds interaction functions using context
 */
fun buildInteractionFunctionsWithContext(context: Context) = interactionFunctionsWithoutContext.map{ it.map { it(context) }}

/**
 * Lua function that shows the toast
 */
private class LuaToastShort(val context: Context) : OneArgFunction() {
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

/**
 * Lua function that asks user to enter a string
 * Returns lua string
 */
private class LuaAskString(val context: Context) : OneArgFunction() {
    private var result: String? = null
    private var isDone = false
    private fun alert(title: String): String? {
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
private class LuaSelect(val context: Context) : VarArgFunction() {
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

/**
 * Ask user to confirm some action
 * Returns 0 if user answered positively
 * Returns 1 if user answered negatively
 * Returns -1 if user dismissed
 */
private class LuaConfirm(val context: Context) : OneArgFunction() {
    private fun ask(text: String): Int {
        var isDone: Boolean
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
