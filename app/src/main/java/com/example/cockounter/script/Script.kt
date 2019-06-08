package com.example.cockounter.script

import android.content.Context
import arrow.core.Tuple2
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
    const val META_NAME = "__name"
    const val META_ROLE = "__role"
}

private fun mapFunctions(globals: Globals, functions: List<Tuple2<String, LuaFunction>>): Globals {
    functions.forEach { globals[it.a] = it.b }
    return globals
}

private fun mapFromGameState(context: ScriptContext, globals: Globals, state: GameState): Globals =
    when (context) {
        ScriptContext.None ->  globals
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

private fun mapPlayerOnly(state: GameState, player: PlayerDescription, globals: Globals): Globals {
    globals[Constants.META_NAME] = player.name
    globals[Constants.META_ROLE] = player.role
    globals.addAll(state[player].privateParameters)
    globals[Constants.SHARED_PARAMETERS] = createTable(state[player.role].sharedParameters)
    globals[Constants.GLOBAL_PARAMETERS] = createTable(state.globalParameters)
    return globals
}

private fun mapAll(state: GameState, player: PlayerDescription, globals: Globals): Globals {
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
    return globals
}

private fun unpackValue(value: LuaValue, old: GameParameter): GameParameter = when (old) {
    is IntegerGameParameter -> IntegerGameParameter(old.name, old.visibleName, value.toint())
    is StringGameParameter -> StringGameParameter(old.name, old.visibleName, value.tojstring()!!)
    is DoubleGameParameter -> DoubleGameParameter(old.name, old.visibleName, value.todouble())
    is BooleanGameParameter -> BooleanGameParameter(old.name, old.visibleName, value.toboolean())
}

private fun unpackTable(table: LuaTable, old: Map<String, GameParameter>) =
    old.mapValues { unpackValue(table[it.key], it.value) }.toImmutableMap()

private fun unmapGameState(context: ScriptContext, globals: Globals, oldState: GameState): GameState =
    when (context) {
        ScriptContext.None -> oldState
        is ScriptContext.PlayerOnly -> unmapPlayerOnly(oldState, context.player, globals)
        is ScriptContext.Full -> unmapAll(oldState, globals)
    }

private fun unmapPlayerOnly(oldState: GameState, player: PlayerDescription, globals: Globals): GameState {
    val newSharedParameters = unpackTable(
        globals[Constants.SHARED_PARAMETERS] as LuaTable,
        oldState.roles.getValue(player.role).sharedParameters
    )
    val newGlobalParameters = unpackTable(
        globals[Constants.GLOBAL_PARAMETERS] as LuaTable,
        oldState.globalParameters
    )
    val newPrivateParameters = unpackTable(globals, oldState[player].privateParameters)
    return oldState.copy(globalParameters = newGlobalParameters, roles = oldState.roles.toImmutableMap().modify(player.role) {
        it.copy(sharedParameters = newSharedParameters, players = it.players.toImmutableMap().modify(player.name) {
            it.copy(privateParameters = newPrivateParameters)
        })
    })
}

private fun unmapAll(oldState: GameState, globals: Globals): GameState {
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
    return oldState.copy(globalParameters = newGlobalParameters, roles = newRoles)
}

typealias ScriptEvaluation = (Context) -> ((Action) -> Evaluation)
typealias Evaluation = (GameState) -> GameState

private fun evalAction(globals: Globals, action: Action): Globals {
    when(action) {
        is Action.PlayerScript -> {
            globals.load(action.script).call()
            return globals
        }
    }
}

private fun performAction(globals: Globals, action: Action): (GameState) -> GameState = { state ->
    when (action) {
        is Action.PlayerScript -> {
            unmapGameState(action.context, evalAction(mapFromGameState(action.context, globals, state), action), state)
        }
    }
}

/**
 * Builds [ScriptEvaluation] from [Preset]
 */
fun buildScriptEvaluation(preset: Preset): ScriptEvaluation {
    val globals = JsePlatform.standardGlobals()
    return { context: Context ->
        mapFunctions(globals, buildInteractionFunctionsWithContext(context));
        preset.libraries.forEach {
            globals.load(it.script).call()
        };
        { action: Action ->
            performAction(globals, action)
        }
    }
}

/**
 * Runs script without any mapped parameters
 *
 * @param context context to use
 * @param script script to run
 */
fun runScript(context: Context, script: String): Unit {
    val globals = JsePlatform.standardGlobals()
    mapFunctions(globals, buildInteractionFunctionsWithContext(context))
    val action = Action.PlayerScript(ScriptContext.None, script)
    performAction(globals, action)(dummyState)
}

/**
 * Builds an [Action] from [ActionButtonModel] and [ScriptContext]
 */
fun buildAction(button: ActionButtonModel, context: ScriptContext): Action = when (button) {
    is ActionButtonModel.Attached -> Action.PlayerScript(context, button.script.script)
    is ActionButtonModel.Global -> Action.PlayerScript(context, button.script.script)
    is ActionButtonModel.Role -> Action.PlayerScript(context, button.script.script)
}
