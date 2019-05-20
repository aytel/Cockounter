package com.example.cockounter.script

import com.example.cockounter.core.*
import com.github.andrewoma.dexx.kollection.toImmutableMap
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.JsePlatform

fun evaluateScript(interpreter: Interpreter, script: String): Interpreter {
    loadScript(interpreter, script).call()
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
    globals["global"] = createTable(state.sharedParameters)
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
    val sharedParameters = unpackTable(globals["global"].checktable()!!, oldState.sharedParameters)
    val roles = oldState.roles.mapValues { (roleName, role) ->
        GameRole(role.name, unpackTable(globals[roleName]["shared"].checktable()!!, role.sharedParameters), role.players.values.mapIndexed { index, player ->
            val privateParameters = unpackTable(globals[roleName][index].checktable()!!, player.privateParameters)
            Pair(player.name, Player(player.name, privateParameters))
        }.toImmutableMap())
    }.toImmutableMap()
    return GameState(sharedParameters, roles)
}

fun loadScript(interpreter: Interpreter, script: String) =
    JsePlatform.standardGlobals().load(script, "script", interpreter.globals)!!

fun performScript(state: GameState, playerName: String, script: String): GameState =
    mapToGameState(evaluateScript(mapFromGameState(state, playerName), script), state)

data class Interpreter(val globals: Globals)