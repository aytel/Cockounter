package com.example.cockounter.script

import com.example.cockounter.core.GameRole
import com.example.cockounter.core.GameState
import com.example.cockounter.core.Player
import com.github.andrewoma.dexx.kollection.toImmutableList
import com.github.andrewoma.dexx.kollection.toImmutableMap
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.JsePlatform

fun evaluateScript(interpreter: Interpreter, script: String): Interpreter {
    loadScript(interpreter, script).call()
    return interpreter
}

fun mapFromGameState(state: GameState, currentPlayer: Player): Interpreter {
    val globals = JsePlatform.standardGlobals()!!;
    fun setValue(table: LuaTable, key: String, value: Any) {
        when (value) {
            is Int -> table.set(key, value as Int)
            is String -> table.set(key, value as String)
            else -> table.set(key, LuaValue.NIL)
        }
    }
    globals["shared"] = LuaValue.tableOf()
    state.sharedParameters.forEach {
        setValue(globals["shared"].checktable()!!, it.key, it.value)
    }
    globals["player"] = LuaValue.tableOf()
    state.playersByRole.forEach {
        val roleName = it.key;
        val sharedParameters = state.roles[roleName]!!.sharedParameters
        val sharedParametersTable = LuaValue.tableOf()
        sharedParameters.forEach {
            setValue(sharedParametersTable, it.key, it.value);
        }
        globals.set(roleName, LuaValue.tableOf())
        val roleTable = globals[roleName].checktable()!!
        it.value.forEachIndexed { index, player ->
            roleTable.set(index, LuaValue.tableOf())
            val playerTable = roleTable[index].checktable()!!
            playerTable["__name"] = player.name
            playerTable["shared"] = sharedParametersTable
            val privateParametersTable = playerTable["private"].checktable()!!
            player.privateParameters.forEach {
                setValue(privateParametersTable, it.key, it.value)
            }
            playerTable["private"] = privateParametersTable
            if (player.name == currentPlayer.name) {
                globals["player"].checktable()!!["shared"] = sharedParametersTable
                globals["player"].checktable()!!["private"] = privateParametersTable
            }
        }
    }
    return Interpreter(globals)
}

fun mapToGameState(interpreter: Interpreter, oldState: GameState): GameState {
    fun unpackValue(value: LuaValue, old: Any): Any = when (old) {
        is Int -> value.toint()
        is String -> value.tojstring()!!
        else -> 0
    }

    val globals = interpreter.globals
    val sharedParametersTable = globals["shared"].checktable()!!
    val sharedParameters = oldState.sharedParameters.mapValues { unpackValue(sharedParametersTable[it.key], it.value) }
    val rolesTable = globals["roles"].checktable()!!
    val roles = oldState.roles.mapValues { role ->
        GameRole(role.value.name, role.value.sharedParameters.mapValues {
            unpackValue(rolesTable[role.value.name]["shared"][it.key]!!, it.value)
        }.toImmutableMap())
    }
    val playersByRole = oldState.playersByRole.mapValues {
        it.value.mapIndexed { index, player ->
            Player(
                player.name,
                roles.getValue(player.role.name),
                player.privateParameters.mapValues {
                    unpackValue(rolesTable[player.role.name][index]["private"][it.key]!!, it.value)
                }.toImmutableMap()
            )
        }.toImmutableList()
    }
    return GameState(
        sharedParameters.toImmutableMap(),
        roles.toImmutableMap(),
        playersByRole.toImmutableMap()
    )
}

fun loadScript(interpreter: Interpreter, script: String) =
    JsePlatform.standardGlobals().load(script, "script", interpreter.globals)!!

fun performScript(state: GameState, player: Player, script: String): GameState =
    mapToGameState(evaluateScript(mapFromGameState(state, player), script), state)

data class Interpreter(val globals: Globals)