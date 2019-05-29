package com.example.cockounter.script

import android.content.Context
import arrow.core.*
import arrow.core.extensions.`try`.monad.binding
import com.example.cockounter.core.*
import com.github.andrewoma.dexx.kollection.toImmutableMap
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaFunction
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.JsePlatform
import kotlin.random.Random

private object Constants {
    const val GLOBAL_PARAMETERS = "gl"
    const val SHARED_PARAMETERS = "sh"
    const val SAVED_STATE = "saved"
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

private fun evalFunction(table: LuaTable, function: FunctionDescriptor): Try<LuaTable> = binding {
    if(function.args.size == 1) {
        table[function.args[0]].call()
        table
    } else {
        val (result) = evalFunction(table[function.args[0]].checktable(), FunctionDescriptor(function.args.drop(1)))
        result
    }
}

private fun putFunction(globals: Globals, table: LuaTable, descriptor: FunctionDescriptor, function: String): Try<Unit> = binding {
    if(descriptor.args.size == 1) {
        table[descriptor.args[0]] = globals.load(function)
        Unit
    } else {
        table[descriptor.begin] = LuaValue.tableOf()
        putFunction(globals, table[descriptor.begin].checktable(), descriptor.tail(), function)
        Unit
    }
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
        //FIXME load libraries
        preset.libraries.forEach {
            globals.load(it.script)
        };
        //FIXME init saved state
        //TODO initialization
        //TODO load actions
        preset.actionButtons.forEach {
            val descriptor = buildFunctionDescriptor(it)
            putFunction(globals, globals, descriptor, it.script.script)
        };
        { action: Action ->
            Try { performAction(globals, action) }
        }
    }
}

private fun toFunctionPrefix(parameter: ParameterPointer) = when (parameter) {
    is ParameterPointer.Global -> FunctionDescriptor(listOf("action", "global", parameter.name))
    is ParameterPointer.Shared -> FunctionDescriptor(listOf("action", parameter.rolePointer.role, "shared", parameter.name))
    is ParameterPointer.Private -> FunctionDescriptor(listOf("action", parameter.rolePointer.role, "private", parameter.name))
}

//FIXME remove random
fun buildAction(button: ActionButtonModel, context: ScriptContext): Action = when (button) {
    is ActionButtonModel.Attached -> Action.PlayerScript(context, buildFunctionDescriptor(button))
    is ActionButtonModel.Global -> Action.PlayerScript(context, buildFunctionDescriptor(button))
    is ActionButtonModel.Role -> Action.PlayerScript(context, buildFunctionDescriptor(button))
}

fun buildFunctionDescriptor(button: ActionButtonModel): FunctionDescriptor = when (button) {
    is ActionButtonModel.Attached -> FunctionDescriptor(toFunctionPrefix(button.parameterPointer).args + listOf(generateName(button.script.functionName, Random.nextInt())))
    is ActionButtonModel.Global -> FunctionDescriptor(listOf("action") + generateName(button.script.functionName, Random.nextInt()))
    is ActionButtonModel.Role -> FunctionDescriptor(listOf("action", button.rolePointer.role) + generateName(button.script.functionName, Random.nextInt()))
}

private fun generateName(string: String?, index: Int) = string.toOption().getOrElse { "__func$index" }

