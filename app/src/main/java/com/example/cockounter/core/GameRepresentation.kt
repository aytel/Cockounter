package com.example.cockounter.core

import com.example.cockounter.script.Action
import com.example.cockounter.script.buildAction

data class ByPlayerRepresentation(val players: List<PlayerRepresentation>) {
}

data class PlayerRepresentation(
    val name: String,
    val role: String,
    val globalParameters: List<ParameterRepresentation>,
    val sharedParameters: List<ParameterRepresentation>,
    val privateParameters: List<ParameterRepresentation>,
    val freeButtons: List<ActionButtonRepresentation>
)

data class ParameterRepresentation(
    val parameter: GameParameterPointer,
    val attachedButtons: List<ActionButtonRepresentation>
)

data class ActionButtonRepresentation(val text: String, val action: Action)

fun buildContext(
    context: ScriptContextDescription,
    parameter: GameParameterPointer,
    player: PlayerDescription
): ScriptContext = when (context) {
    ScriptContextDescription.NONE -> ScriptContext.None
    ScriptContextDescription.SINGLE_PARAMETER -> ScriptContext.SingleParameter(parameter)
    ScriptContextDescription.PLAYER_ONLY -> ScriptContext.PlayerOnly(player)
    ScriptContextDescription.ALL -> ScriptContext.Full(player)
}

fun buildContext(context: ScriptContextDescription, player: PlayerDescription): ScriptContext = when(context) {
    ScriptContextDescription.NONE -> ScriptContext.None
    ScriptContextDescription.SINGLE_PARAMETER -> ScriptContext.None //Todo None
    ScriptContextDescription.PLAYER_ONLY -> ScriptContext.PlayerOnly(player)
    ScriptContextDescription.ALL -> ScriptContext.Full(player)
}

fun buildGameParameterPointer(parameterPointer: ParameterPointer, player: PlayerDescription): GameParameterPointer =
    when (parameterPointer) {
        is ParameterPointer.Global -> GameParameterPointer.Global(parameterPointer.name)
        is ParameterPointer.Shared -> GameParameterPointer.Shared(player.role, parameterPointer.name)
        is ParameterPointer.Private -> GameParameterPointer.Private(player, parameterPointer.name)
    }

fun buildAttachedButtons(
    parameterPointer: ParameterPointer,
    actionButtons: List<ActionButtonModel>,
    player: PlayerDescription
): List<ActionButtonRepresentation> =
    actionButtons.flatMap {
        when (it) {
            is ActionButtonModel.Attached -> if (it == parameterPointer) {
                val gameParameterPointer = buildGameParameterPointer(it.parameterPointer, player)
                val context = buildContext(it.script.context, gameParameterPointer, player)
                listOf(ActionButtonRepresentation(it.script.visibleName, buildAction(it, context)))
            } else listOf()
            else -> listOf()
        }
    }

fun buildFreeButtons(buttons: List<ActionButtonModel>, player: PlayerDescription): List<ActionButtonRepresentation> =
    buttons.flatMap {
        when(it) {
            is ActionButtonModel.Global -> listOf(ActionButtonRepresentation(it.script.visibleName, buildAction(it, buildContext(it.script.context, player))))
            is ActionButtonModel.Role -> listOf(ActionButtonRepresentation(it.script.visibleName, buildAction(it, buildContext(it.script.context, player))))
            is ActionButtonModel.Attached -> listOf()
        }
    }

fun buildPlayerRepresentation(preset: Preset, player: PlayerDescription): PlayerRepresentation {
    val globalParameters = preset.globalParameterPointers().map {
        ParameterRepresentation(
            buildGameParameterPointer(it, player),
            buildAttachedButtons(it, preset.actionButtons, player)
        )
    }
    //TODO make role pointer creation
    val rolePointer = RolePointer(player.role)
    val sharedParameters = preset[rolePointer].sharedParameterPointers().map {
        ParameterRepresentation(
            buildGameParameterPointer(it, player),
            buildAttachedButtons(it, preset.actionButtons, player)
        )
    }
    val privateParameters = preset[rolePointer].privateParameterPointers().map {
        ParameterRepresentation(
            buildGameParameterPointer(it, player),
            buildAttachedButtons(it, preset.actionButtons, player)
        )
    }
    val freeButtons = buildFreeButtons(preset.actionButtons, player)
    return PlayerRepresentation(player.name, player.role, globalParameters, sharedParameters, privateParameters, freeButtons)
}

fun buildByPlayerRepresentation(preset: Preset, players: List<PlayerDescription>): ByPlayerRepresentation =
        ByPlayerRepresentation(players.map { buildPlayerRepresentation(preset, it) })
