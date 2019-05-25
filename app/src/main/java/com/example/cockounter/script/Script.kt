package com.example.cockounter.script

import android.content.Context
import arrow.core.Try
import arrow.core.Tuple2
import arrow.core.extensions.`try`.monad.binding
import com.example.cockounter.core.*
import com.github.andrewoma.dexx.kollection.toImmutableMap
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaFunction
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.JsePlatform

private object Constants {
    const val GLOBAL_PARAMETERS = "gl"
    const val SHARED_PARAMETERS = "sh"
    const val PLAYER = "pl"
}

fun evaluateScript(interpreter: Interpreter, script: String): Interpreter {
    loadScript(interpreter, script).call()
    return interpreter
}

fun mapFunctions(interpreter: Interpreter, functions: List<Tuple2<String, LuaFunction>>): Interpreter {
    functions.forEach { interpreter.globals[it.a] = it.b }
    return interpreter
}

fun mapFromGameState(context: ScriptContext, state: GameState, player: PlayerDescription): Try<Interpreter> =
    when (context) {
        ScriptContext.X -> TODO()
        ScriptContext.PLAYER -> mapPlayerFromGameState(state, player)
        ScriptContext.FULL -> mapAllFromGameState(state, player.name)
    }

private fun setValue(table: LuaTable, key: String, parameter: GameParameter): LuaTable {
    when (parameter) {
        is IntegerGameParameter -> table.set(key, parameter.value)
        is StringGameParameter -> table.set(key, parameter.value)
        is DoubleGameParameter -> table.set(key, parameter.value)
        is BooleanGameParameter -> table.set(key, if (parameter.value) LuaValue.TRUE else LuaValue.FALSE)
    }
    return table
}

private fun createTable(items: Map<String, GameParameter>) =
    items.entries.fold(LuaValue.tableOf()!!) { table, (key, value) ->
        setValue(table, key, value)
    }

private fun addToTable(table: LuaTable, items: Map<String, GameParameter>) {
    items.forEach {
        setValue(table, it.key, it.value)
    }
}

fun mapAllFromGameState(state: GameState, currentPlayerName: String): Try<Interpreter> = Try {
    val globals = JsePlatform.standardGlobals()!!;
    globals[Constants.GLOBAL_PARAMETERS] = createTable(state.globalParameters)
    state.roles.forEach {
        val roleName = it.key
        globals[roleName] = LuaValue.tableOf()!!
        globals[roleName][Constants.SHARED_PARAMETERS] = createTable(it.value.sharedParameters)
        it.value.players.map { it.value }.forEachIndexed { index, player ->
            globals[roleName][index] = createTable(player.privateParameters)
            globals[roleName][index][Constants.SHARED_PARAMETERS] = globals[roleName][Constants.SHARED_PARAMETERS]
            if (player.name == currentPlayerName) {
                globals[Constants.PLAYER] = globals[roleName][index]
            }
        }
    }
    Interpreter(globals)
}

fun mapPlayerFromGameState(state: GameState, player: PlayerDescription): Try<Interpreter> = Try {
    val globals = JsePlatform.standardGlobals()!!
    globals[Constants.GLOBAL_PARAMETERS] = createTable(state.globalParameters)
    globals[Constants.SHARED_PARAMETERS] = createTable(state[player.role].sharedParameters)
    addToTable(globals, state[player].privateParameters)
    Interpreter(globals)
}

fun mapXFromGameState(state: GameState, player: PlayerDescription, parameterName: String) = Try {
    val globals = JsePlatform.standardGlobals()!!
    TODO()
}

private fun unpackValue(value: LuaValue, old: GameParameter): GameParameter = when (old) {
    is IntegerGameParameter -> IntegerGameParameter(old.name, old.visibleName, value.toint())
    is StringGameParameter -> StringGameParameter(old.name, old.visibleName, value.tojstring()!!)
    is DoubleGameParameter -> DoubleGameParameter(old.name, old.visibleName, value.todouble())
    is BooleanGameParameter -> BooleanGameParameter(old.name, old.visibleName, value.toboolean())
}

private fun unpackTable(table: LuaTable, old: Map<String, GameParameter>) =
    old.mapValues { unpackValue(table[it.key], it.value) }.toImmutableMap()

fun mapToGameState(interpreter: Interpreter, oldState: GameState, player: PlayerDescription, context: ScriptContext): Try<GameState> = when(context) {
    ScriptContext.X -> TODO()
    ScriptContext.PLAYER -> mapPlayerToGameState(interpreter, oldState, player)
    ScriptContext.FULL -> mapAllToGameState(interpreter, oldState)
}

fun mapAllToGameState(interpreter: Interpreter, oldState: GameState): Try<GameState> = Try {
    val globals = interpreter.globals
    val globalParameters = unpackTable(globals[Constants.GLOBAL_PARAMETERS].checktable()!!, oldState.globalParameters)
    val roles = oldState.roles.mapValues { (roleName, role) ->
        GameRole(
            role.name,
            unpackTable(globals[roleName][Constants.SHARED_PARAMETERS].checktable()!!, role.sharedParameters),
            role.players.values.mapIndexed { index, player ->
                val privateParameters = unpackTable(globals[roleName][index].checktable()!!, player.privateParameters)
                Pair(player.name, Player(player.name, privateParameters))
            }.toImmutableMap()
        )
    }.toImmutableMap()
    GameState(globalParameters, roles)
}

fun mapPlayerToGameState(interpreter: Interpreter, oldState: GameState, player: PlayerDescription): Try<GameState> = Try {
    val globals = interpreter.globals
    val globalParameters = unpackTable(globals[Constants.GLOBAL_PARAMETERS].checktable()!!, oldState.globalParameters)
    val sharedParameters = unpackTable(globals[Constants.SHARED_PARAMETERS].checktable()!!, oldState[player.role].sharedParameters)
    val privateParameters = unpackTable(globals, oldState[player].privateParameters)
    TODO()
}

fun loadScript(interpreter: Interpreter, script: String) =
    JsePlatform.standardGlobals().load(script, "script", interpreter.globals)!!

fun performScriptUsingGameState(state: GameState, player: PlayerDescription, script: String, context: ScriptContext): Try<GameState> = binding {
    val (interpreter) = mapFromGameState(context, state, player)
    val interperter2 = evaluateScript(interpreter, script)
    val (result) = mapToGameState(interperter2, state, player, context)
    result
}

fun performScriptWithContext(state: GameState, playerName: String, script: String, context: Context): GameState = TODO()
/*
    mapToGameState(
        evaluateScript(
            mapFunctions(
                mapFromGameState(state, playerName),
                buildInteractionFunctionsWithContext(context)
            ), script
        ), state
    )
*/

inline fun <reified T> performScript(
    map: (T) -> Interpreter,
    unmap: (Interpreter) -> T,
    functions: List<Tuple2<String, LuaFunction>>,
    script: String,
    value: T
): T {
    val interpreter = mapFunctions(map(value), functions)
    return unmap(evaluateScript(interpreter, script))
}

fun mapFromNothing(unit: Unit) = Interpreter(JsePlatform.standardGlobals()!!)

fun mapToNothing(interpreter: Interpreter) = Unit

fun performScriptUsingNothingWithContext(script: String, context: Context) =
    performScript(::mapFromNothing, ::mapToNothing, buildInteractionFunctionsWithContext(context), script, Unit)


data class Interpreter(val globals: Globals)