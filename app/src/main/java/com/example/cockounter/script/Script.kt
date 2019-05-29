package com.example.cockounter.script

import android.content.Context
import arrow.core.Option
import arrow.core.Try
import arrow.core.Tuple2
import arrow.core.getOrElse
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
    const val SINGLE_PARAMETER_NAME = "x"
    const val META_NAME = "__name"
    const val META_ROLE = "__role"
}

//private data class Interpreter(val globals: Globals)

private fun mapFunctions(globals: Globals, functions: List<Tuple2<String, LuaFunction>>): Globals {
    functions.forEach { globals[it.a] = it.b }
    return globals
}

private fun mapFromGameState(context: ScriptContext, globals: Globals, state: GameState): Try<Globals> =
    when (context) {
        ScriptContext.None -> Try { globals }
        is ScriptContext.SingleParameter -> mapSingleParameter(state, context.parameter, globals)
        is ScriptContext.PlayerOnly -> mapPlayerOnly(state, context.player, globals)
        is ScriptContext.Full -> mapAll(state, context.player, globals)
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

private operator fun LuaTable.set(key: String, parameter: GameParameter) = when (parameter) {
    is IntegerGameParameter -> set(key, parameter.value)
    is StringGameParameter -> set(key, parameter.value)
    is DoubleGameParameter -> set(key, parameter.value)
    is BooleanGameParameter -> set(key, if (parameter.value) LuaValue.TRUE else LuaValue.FALSE)
}

private fun createTable(items: Map<String, GameParameter>) =
    items.entries.fold(LuaValue.tableOf()!!) { table, (key, value) ->
        setValue(table, key, value)
    }

private fun LuaTable.addAll(items: Map<String, GameParameter>) {
    items.forEach { set(it.key, it.value) }
}

private fun addToTable(table: LuaTable, items: Map<String, GameParameter>) {
    items.forEach {
        setValue(table, it.key, it.value)
    }
}

private fun mapSingleParameter(state: GameState, parameter: GameParameterPointer, globals: Globals): Try<Globals> =
    Try {
        globals[Constants.SINGLE_PARAMETER_NAME] = state[parameter]
        globals
    }

private fun mapPlayerOnly(state: GameState, player: PlayerDescription, globals: Globals): Try<Globals> = Try {
    globals[Constants.META_NAME] = player.name
    globals[Constants.META_ROLE] = player.role
    globals.addAll(state[player].privateParameters)
    globals[Constants.SHARED_PARAMETERS] = createTable(state[player.role].sharedParameters)
    globals
}

private fun mapAll(state: GameState, player: PlayerDescription, globals: Globals): Try<Globals> = Try {
    globals[Constants.GLOBAL_PARAMETERS] = createTable(state.globalParameters)
    state.roles.forEach {
        val roleName = it.key
        globals[roleName] = LuaValue.tableOf()!!
        globals[roleName][Constants.SHARED_PARAMETERS] = createTable(it.value.sharedParameters)
        it.value.players.map { it.value }.forEachIndexed { index, currentPlayer ->
            globals[roleName][index] = createTable(currentPlayer.privateParameters)
            globals[roleName][index][Constants.SHARED_PARAMETERS] = globals[roleName][Constants.SHARED_PARAMETERS]
            if (player.name == currentPlayer.name) {
                globals[Constants.PLAYER] = globals[roleName][index]
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

typealias ScriptEvaluation = (Context) -> ((Action) -> Try<Evaluation>)
typealias Evaluation = (GameState) -> Try<GameState>

private fun evalFunction(globals: Globals, function: String): Try<Globals> = Try {
    globals[function].call()
    globals
}

private fun performAction(globals: Globals, action: Action): (GameState) -> Try<GameState> = { state ->
    when (action) {
        is Action.PlayerScript -> mapFromGameState(action.context, globals, state)
            .flatMap { evalFunction(it, action.function) }
            .flatMap { unmapGameState(action.context, globals, state) }
    }
}

fun buildScriptEvaluation(preset: Preset, players: List<PlayerDescription>): ScriptEvaluation {
    val globals = JsePlatform.standardGlobals()
    return { context: Context ->
        mapFunctions(globals, buildInteractionFunctionsWithContext(context));
        //TODO load libraries
        //TODO init saved state
        //TODO load actions
        { action: Action ->
            Try { performAction(globals, action) }
        }
    }
}

private fun toFunctionPrefix(parameter: ParameterPointer) = when (parameter) {
    is ParameterPointer.Global -> "action.global.${parameter.name}."
    is ParameterPointer.Shared -> "action.${parameter.rolePointer.role}.shared.${parameter.name}."
    is ParameterPointer.Private -> "action.${parameter.rolePointer.role}.private.${parameter.name}"
}

fun buildAction(button: ActionButtonModel, context: ScriptContext): Action = when (button) {
    is ActionButtonModel.Attached -> Action.PlayerScript(
        context,
        toFunctionPrefix(button.parameterPointer) + generateName(button.script.functionName, TODO("index")))
    is ActionButtonModel.Global -> Action.PlayerScript(context, "action." + generateName(button.script.functionName, TODO("index")))
    is ActionButtonModel.Role -> Action.PlayerScript(context, "action.${button.rolePointer.role}." + generateName(button.script.functionName, TODO("index")))
}

private fun generateName(string: Option<String>, index: Int) = string.getOrElse { "__func$index" }
