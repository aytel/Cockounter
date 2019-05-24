package com.example.cockounter.script

import android.content.Context
import arrow.core.*
import com.example.cockounter.core.*
import com.github.andrewoma.dexx.kollection.toImmutableMap
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaFunction
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.JsePlatform

fun evaluateScript(interpreter: Interpreter, script: String): Interpreter {
    loadScript(interpreter, script).call()
    return interpreter
}

fun mapFunctions(interpreter: Interpreter, functions: List<Tuple2<String, LuaFunction>>): Interpreter {
    functions.forEach { interpreter.globals[it.a] = it.b }
    return interpreter
}

fun mapFromGameState(state: GameState, currentPlayerName: String): Interpreter {
    fun setValue(table: LuaTable, key: String, parameter: GameParameter): LuaTable {
        when (parameter) {
            is IntegerGameParameter -> table.set(key, parameter.value)
            is StringGameParameter -> table.set(key, parameter.value)
        }
        return table
    }

    fun createTable(items: Map<String, GameParameter>) =
        items.entries.fold(LuaValue.tableOf()!!) { table, (key, value) ->
            setValue(table, key, value)
        }

    val globals = JsePlatform.standardGlobals()!!;
    globals["global"] = createTable(state.globalParameters)
    state.roles.forEach {
        val roleName = it.key
        globals[roleName] = LuaValue.tableOf()!!
        globals[roleName]["shared"] = createTable(it.value.sharedParameters)
        it.value.players.map { it.value }.forEachIndexed { index, player ->
            globals[roleName][index] = createTable(player.privateParameters)
            globals[roleName][index]["shared"] = globals[roleName]["shared"]
            if (player.name == currentPlayerName) {
                globals["player"] = globals[roleName][index]
            }
        }
    }
    return Interpreter(globals)
}

fun mapToGameState(interpreter: Interpreter, oldState: GameState): GameState {
    fun unpackValue(value: LuaValue, old: GameParameter): GameParameter = when (old) {
        is IntegerGameParameter -> IntegerGameParameter(old.name, value.toint())
        is StringGameParameter -> StringGameParameter(old.name, value.tojstring()!!)
    }

    fun unpackTable(table: LuaTable, old: Map<String, GameParameter>) =
        old.mapValues { unpackValue(table[it.key], it.value) }.toImmutableMap()

    val globals = interpreter.globals
    val sharedParameters = unpackTable(globals["global"].checktable()!!, oldState.globalParameters)
    val roles = oldState.roles.mapValues { (roleName, role) ->
        GameRole(
            role.name,
            unpackTable(globals[roleName]["shared"].checktable()!!, role.sharedParameters),
            role.players.values.mapIndexed { index, player ->
                val privateParameters = unpackTable(globals[roleName][index].checktable()!!, player.privateParameters)
                Pair(player.name, Player(player.name, privateParameters))
            }.toImmutableMap()
        )
    }.toImmutableMap()
    return GameState(sharedParameters, roles)
}

fun loadScript(interpreter: Interpreter, script: String) =
    JsePlatform.standardGlobals().load(script, "script", interpreter.globals)!!

fun performScriptUsingGameState(state: GameState, playerName: String, script: String): GameState =
    mapToGameState(evaluateScript(mapFromGameState(state, playerName), script), state)

fun performScriptWithContext(state: GameState, playerName: String, script: String, context: Context): GameState =
    mapToGameState(
        evaluateScript(
            mapFunctions(
                mapFromGameState(state, playerName),
                buildInteractionFunctionsWithContext(context)
            ), script
        ), state
    )

inline fun <reified T> performScript(map: (T) -> Interpreter, unmap: (Interpreter) -> T, functions: List<Tuple2<String, LuaFunction>>, script: String, value: T): T {
    val interpreter = mapFunctions(map(value), functions)
    return unmap(evaluateScript(interpreter, script))
}

fun mapFromNothing(unit: Unit) = Interpreter(JsePlatform.standardGlobals()!!)

fun mapToNothing(interpreter: Interpreter) = Unit

fun performScriptUsingNothingWithContext(script: String, context: Context) = performScript(::mapFromNothing, ::mapToNothing, buildInteractionFunctionsWithContext(context), script, Unit)


data class Interpreter(val globals: Globals)