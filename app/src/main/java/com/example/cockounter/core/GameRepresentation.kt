package com.example.cockounter.core

import com.example.cockounter.script.Action
import com.example.cockounter.script.buildAction

sealed class GameRepresentation

data class ByPlayerRepresentation(val players: List<PlayerRepresentation>) : GameRepresentation()

data class ByRoleRepresentation(val roles: List<RoleRepresentation>) : GameRepresentation()

data class PlayerRepresentation(
    val name: String,
    val role: String,
    val globalParameters: List<ParameterRepresentation>,
    val sharedParameters: List<ParameterRepresentation>,
    val privateParameters: List<ParameterRepresentation>,
    val freeButtons: List<ActionButtonRepresentation>
)

data class RoleRepresentation(
    val role: String,
    val globalParameters: List<ParameterRepresentation>,
    val sharedParameters: List<ParameterRepresentation>,
    val privateParameterBlocks: List<PrivateParameterBlockRepresentation>,
    val freeButtons: List<ActionButtonRepresentation>
)

data class PrivateParameterBlockRepresentation(
    val name: String,
    val parameters: List<GroupPrivateParameterRepresentation>
)

data class GroupPrivateParameterRepresentation(
    val playerName: String,
    val parameter: ParameterRepresentation
)

data class ParameterRepresentation(
    val name: String,
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

fun buildContext(context: ScriptContextDescription, player: PlayerDescription): ScriptContext = when (context) {
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
        when (it) {
            is ActionButtonModel.Global -> listOf(
                ActionButtonRepresentation(
                    it.script.visibleName,
                    buildAction(it, buildContext(it.script.context, player))
                )
            )
            is ActionButtonModel.Role -> listOf(
                ActionButtonRepresentation(
                    it.script.visibleName,
                    buildAction(it, buildContext(it.script.context, player))
                )
            )
            is ActionButtonModel.Attached -> listOf()
        }
    }

fun buildPlayerRepresentation(preset: Preset, player: PlayerDescription): PlayerRepresentation {
    val globalParameters = preset.globalParameterPointers().map {
        ParameterRepresentation(
            preset[it].visibleName,
            buildGameParameterPointer(it, player),
            buildAttachedButtons(it, preset.actionButtons, player)
        )
    }
    //TODO make role pointer creation
    val rolePointer = RolePointer(player.role)
    val sharedParameters = preset[rolePointer].sharedParameterPointers().map {
        ParameterRepresentation(
            preset[it].visibleName,
            buildGameParameterPointer(it, player),
            buildAttachedButtons(it, preset.actionButtons, player)
        )
    }
    val privateParameters = preset[rolePointer].privateParameterPointers().map {
        ParameterRepresentation(
            preset[it].visibleName,
            buildGameParameterPointer(it, player),
            buildAttachedButtons(it, preset.actionButtons, player)
        )
    }
    val freeButtons = buildFreeButtons(preset.actionButtons, player)
    return PlayerRepresentation(
        player.name,
        player.role,
        globalParameters,
        sharedParameters,
        privateParameters,
        freeButtons
    )
}

fun buildByPlayerRepresentation(preset: Preset, players: List<PlayerDescription>): ByPlayerRepresentation =
    ByPlayerRepresentation(players.map { buildPlayerRepresentation(preset, it) })

//FIXME
fun buildRoleRepresentation(preset: Preset, roleName: String, players: List<PlayerDescription>): RoleRepresentation {
    val globalParameters = preset.globalParameterPointers().map {
        ParameterRepresentation(
            preset[it].visibleName,
            buildGameParameterPointer(it, players[0]),
            buildAttachedButtons(it, preset.actionButtons, players[0])
        )
    }
    //TODO make role pointer creation
    val rolePointer = RolePointer(roleName)
    val sharedParameters = preset[rolePointer].sharedParameterPointers().map {
        ParameterRepresentation(
            preset[it].visibleName,
            buildGameParameterPointer(it, players[0]),
            buildAttachedButtons(it, preset.actionButtons, players[0])
        )
    }
    val groupedPrivateParameters = players.flatMap { player ->
        preset[rolePointer].privateParameterPointers().map {
            Pair(
                player.name, ParameterRepresentation(
                    preset[it].visibleName,
                    buildGameParameterPointer(it, player),
                    buildAttachedButtons(it, preset.actionButtons, player)
                )
            )
        }
    }.groupBy { it.second.name }
    val blocks = groupedPrivateParameters.map {
        PrivateParameterBlockRepresentation(it.key,
            it.value.map { GroupPrivateParameterRepresentation(it.first, it.second) })
    }
    val freeButtons = buildFreeButtons(preset.actionButtons, players[0])
    return RoleRepresentation(roleName, globalParameters, sharedParameters, blocks, freeButtons)
}

fun buildByRoleRepresentation(preset: Preset, players: List<PlayerDescription>) =
        ByRoleRepresentation(players.groupBy { it.role }.map { buildRoleRepresentation(preset, it.key, it.value) })
