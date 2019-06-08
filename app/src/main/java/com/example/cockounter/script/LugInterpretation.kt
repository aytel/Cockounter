package com.example.cockounter.script

import arrow.Kind
import arrow.core.Try
import arrow.core.Tuple2
import arrow.data.extensions.list.foldable.foldM
import arrow.effects.ForIO
import arrow.effects.typeclasses.MonadDefer
import com.example.cockounter.core.*
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaFunction
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.JsePlatform
import arrow.effects.extensions.io.fx.fx
import arrow.effects.fix

interface LuaInterpret<F> {
    fun buildScriptEvaluation(preset: Preset, players: List<PlayerDescription>): Kind<F, ScriptEvaluation>
}

/*

class LuaInterpretImpl : LuaInterpret<ForIO> {

    private object Constants {
        const val GLOBAL_PARAMETERS = "gl"
        const val SHARED_PARAMETERS = "sh"
        const val SAVED_STATE = "saved"
        const val PLAYER = "pl"
        const val SINGLE_PARAMETER_NAME = "x"
        const val META_NAME = "__name"
        const val META_ROLE = "__role"
    }

    private fun mapFunctions(globals: Globals, functions: List<Tuple2<String, LuaFunction>>): Kind<ForIO, Globals> = fx {
        functions.forEach { globals[it.a] = it.b }
        globals
    }

    /*
    private fun mapFromGameState(context: ScriptContext, globals: Globals, state: GameState): Kind<ForIO, Globals> =
        when (context) {
            ScriptContext.None ->  fx {globals}.fix()
            is ScriptContext.SingleParameter -> mapSingleParameter(state, context.parameter, globals)
            is ScriptContext.PlayerOnly -> mapPlayerOnly(state, context.player, globals)
            is ScriptContext.Full -> mapAll(state, context.player, globals)
        }

*/
    private fun setValue(table: LuaTable, key: String, parameter: GameParameter): Kind<ForIO, LuaTable> =
        fx {
            when (parameter) {
                is IntegerGameParameter -> table.set(key, parameter.value)
                is StringGameParameter -> table.set(key, parameter.value)
                is DoubleGameParameter -> table.set(key, parameter.value)
                is BooleanGameParameter -> table.set(key, if (parameter.value) LuaValue.TRUE else LuaValue.FALSE)
            }
            table
        }

    private operator fun LuaTable.set(key: String, parameter: GameParameter): Kind<ForIO, Unit> = when (parameter) {
        is IntegerGameParameter -> fx { set(key, parameter.value) }
        is StringGameParameter -> fx { set(key, parameter.value) }
        is DoubleGameParameter -> fx { set(key, parameter.value) }
        is BooleanGameParameter -> fx { set(key, if (parameter.value) LuaValue.TRUE else LuaValue.FALSE) }
    }

    private operator fun LuaTable.set(key: String, value: Kind<ForIO, LuaTable>): Kind<ForIO, Unit> =
        fx { value.flatMap { this@set[key] = value }}

    private fun createTable(items: Map<String, GameParameter>): Kind<F, LuaTable> =
        MD.delay {
            items.entries.fold(LuaValue.tableOf()!!) { table, (key, value) ->
                setValue(table, key, value)
                table
            }
        }

    private fun LuaTable.addAll(items: Map<String, GameParameter>): Kind<F, Unit> =
        items.map { set(it.key, it.value) }.foldM(MD, Unit, {_, _ -> MD.delay { Unit }})

    private fun mapSingleParameter(state: GameState, parameter: GameParameterPointer, globals: Globals): Kind<F, Globals> =
        MD.delay {
            globals[Constants.SINGLE_PARAMETER_NAME] = state[parameter]
            globals
        }

    private fun mapPlayerOnly(state: GameState, player: PlayerDescription, globals: Globals): Kind<F, Globals> = MD.binding {
        globals[Constants.META_NAME] = player.name
        globals[Constants.META_ROLE] = player.role
        globals.addAll(state[player].privateParameters)
        val (table) = createTable(state[player.role].sharedParameters)
        globals[Constants.SHARED_PARAMETERS] = table
        globals
    }

    private fun mapRole(role: GameRole, player: PlayerDescription, globals: Globals): Kind<F, Unit> = fx {
        0
    }

    private fun mapAll(state: GameState, player: PlayerDescription, globals: Globals): Kind<F, Globals> = MD.binding {
        globals[Constants.GLOBAL_PARAMETERS] = createTable(state.globalParameters)
        state.roles.forEach {MD.binding {
            val roleName = it.key
            globals[roleName] = LuaValue.tableOf()!!
            globals[roleName].checktable()[Constants.SHARED_PARAMETERS] = createTable(it.value.sharedParameters)
            it.value.players.map { it.value }.forEachIndexed { index, currentPlayer ->
                MD.binding {
                    globals[roleName][index] = createTable(currentPlayer.privateParameters)
                    globals[roleName][index][Constants.SHARED_PARAMETERS] =
                        globals[roleName][Constants.SHARED_PARAMETERS]
                    if (player.name == currentPlayer.name) {
                        globals[Constants.PLAYER] = globals[roleName][index]
                    }
                }
            }
        }
        }
        globals
    }


    private fun unpackValue(value: LuaValue, old: GameParameter): GameParameter = when (old) {
        is IntegerGameParameter -> IntegerGameParameter(old.name, old.visibleName, value.toint())
        is StringGameParameter -> StringGameParameter(old.name, old.visibleName, value.tojstring()!!)
        is DoubleGameParameter -> DoubleGameParameter(old.name, old.visibleName, value.todouble())
        is BooleanGameParameter -> BooleanGameParameter(old.name, old.visibleName, value.toboolean())
    }

    private fun unpackTable(table: LuaTable, old: Map<String, GameParameter>) =
        old.mapValues { unpackValue(table[it.key], it.value) }.toImmutableMap()

    private fun unmapGameState(context: ScriptContext, globals: Globals, oldState: GameState): Try<GameState> =
        when (context) {
            ScriptContext.None -> Try { oldState }
            is ScriptContext.SingleParameter -> unmapSingleParameter(oldState, context.parameter, globals)
            is ScriptContext.PlayerOnly -> unmapPlayerOnly(oldState, context.player, globals)
            is ScriptContext.Full -> unmapAll(oldState, context.player, globals)
        }

    private fun unmapSingleParameter(
        oldState: GameState,
        parameter: GameParameterPointer,
        globals: Globals
    ): Try<GameState> = Try {
        oldState.set(parameter, unpackValue(globals[Constants.SINGLE_PARAMETER_NAME], oldState[parameter]))
    }

    private fun unmapPlayerOnly(oldState: GameState, player: PlayerDescription, globals: Globals): Try<GameState> = Try {
        val newSharedParameters = unpackTable(
            globals[Constants.GLOBAL_PARAMETERS] as LuaTable,
            oldState.roles.getValue(player.role).sharedParameters
        )
        val newPrivateParameters = unpackTable(globals, oldState[player].privateParameters)
        oldState.copy(roles = oldState.roles.toImmutableMap().modify(player.role) {
            it.copy(sharedParameters = newSharedParameters, players = it.players.toImmutableMap().modify(player.name) {
                it.copy(privateParameters = newPrivateParameters)
            })
        })
    }

    private fun unmapAll(oldState: GameState, player: PlayerDescription, globals: Globals): Try<GameState> = Try {
        val newGlobalParameters =
            unpackTable(globals[Constants.GLOBAL_PARAMETERS].checktable()!!, oldState.globalParameters)
        val newRoles = oldState.roles.mapValues { (roleName, role) ->
            role.copy(
                sharedParameters = unpackTable(
                    globals[roleName][Constants.SHARED_PARAMETERS].checktable()!!,
                    role.sharedParameters
                ),
                players = role.players.values.mapIndexed { index, player ->
                    val newPrivateParameters =
                        unpackTable(globals[roleName][index].checktable()!!, player.privateParameters)
                    Pair(player.name, player.copy(privateParameters = newPrivateParameters))
                }.toImmutableMap()
            )
        }.toImmutableMap()
        oldState.copy(globalParameters = newGlobalParameters, roles = newRoles)
    }
}

fun <F> buildSc(interpret: LuaInterpret<F>) {
    interpret.run {
        createGlobals()
    }

}

*/

