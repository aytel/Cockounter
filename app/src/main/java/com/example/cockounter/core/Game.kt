package com.example.cockounter.core

import com.example.cockounter.script.Action
import com.example.cockounter.script.buildAction

object Model {
    sealed class Game {
        data class ByPlayer(val players: List<Player>) : Game()
        data class ByRole(val roles: List<Role>) : Game()
    }

    data class Player(
        val name: String,
        val role: String,
        val globalParameters: List<Parameter>,
        val sharedParameters: List<Parameter>,
        val privateParameters: List<Parameter>,
        val freeButtons: List<ActionButton>
    ) {
        companion object
    }

    data class Role(
        val role: String,
        val globalParameters: List<Parameter>,
        val sharedParameters: List<Parameter>,
        val privateParameterBlocks: List<PrivateParameterBlock>,
        val freeButtons: List<ActionButtonsBlock>
    ) {
        companion object
    }

    data class PrivateParameterBlock(
        val name: String,
        val parameters: List<GroupPrivateParameter>
    ) {
        companion object
    }

    data class GroupPrivateParameter(val playerName: String, val parameter: Parameter) {
        companion object
    }

    data class ActionButtonsBlock(val player: String, val buttons: List<ActionButton>) {
        companion object
    }

    data class Parameter(
        val name: String,
        val parameter: GameParameterPointer,
        val attachedButtons: List<ActionButton>
    ) {
        companion object
    }

    data class ActionButton(val text: String, val action: Action) {
        companion object
    }

    private fun buildContext(
        context: ScriptContextDescription,
        parameter: GameParameterPointer,
        player: PlayerDescription
    ): ScriptContext = when (context) {
        ScriptContextDescription.NONE -> ScriptContext.None
        ScriptContextDescription.PLAYER_ONLY -> ScriptContext.PlayerOnly(player)
        ScriptContextDescription.ALL -> ScriptContext.Full(player)
    }

    private fun buildContext(context: ScriptContextDescription, player: PlayerDescription): ScriptContext =
        when (context) {
            ScriptContextDescription.NONE -> ScriptContext.None
            ScriptContextDescription.PLAYER_ONLY -> ScriptContext.PlayerOnly(player)
            ScriptContextDescription.ALL -> ScriptContext.Full(player)
        }

    private fun buildGameParameterPointer(
        parameterPointer: ParameterPointer,
        player: PlayerDescription
    ): GameParameterPointer =
        when (parameterPointer) {
            is ParameterPointer.Global -> GameParameterPointer.Global(parameterPointer.name)
            is ParameterPointer.Shared -> GameParameterPointer.Shared(player.role, parameterPointer.name)
            is ParameterPointer.Private -> GameParameterPointer.Private(player, parameterPointer.name)
        }

    private fun buildAttachedButtons(
        parameterPointer: ParameterPointer,
        presetScripts: List<PresetScript>,
        player: PlayerDescription
    ): List<ActionButton> =
        presetScripts.map {
            val gameParameterPointer = buildGameParameterPointer(parameterPointer, player)
            val context = buildContext(it.context, gameParameterPointer, player)
            ActionButton(
                it.visibleName,
                buildAction(ActionButtonModel.Attached(parameterPointer, it), context)
            )
        }

    private fun buildFreeButtons(
        buttons: List<ActionButtonModel>,
        player: PlayerDescription
    ): List<ActionButton> =
        buttons.flatMap {
            when (it) {
                is ActionButtonModel.Global -> listOf(
                    ActionButton(
                        it.script.visibleName,
                        buildAction(it, buildContext(it.script.context, player))
                    )
                )
                is ActionButtonModel.Role -> listOf(
                    ActionButton(
                        it.script.visibleName,
                        buildAction(it, buildContext(it.script.context, player))
                    )
                )
                is ActionButtonModel.Attached -> listOf()
            }
        }

    fun buildPlayer(preset: Preset, player: PlayerDescription): Player {
        val globalParameters = preset.globalParameterPointers().map {
            Parameter(
                preset[it].visibleName,
                buildGameParameterPointer(it, player),
                buildAttachedButtons(it, preset[it].actionsStubs, player)
            )
        }
        val rolePointer = RolePointer(player.role)
        val sharedParameters = preset[rolePointer].sharedParameterPointers().map {
            Parameter(
                preset[it].visibleName,
                buildGameParameterPointer(it, player),
                buildAttachedButtons(it, preset[it].actionsStubs, player)
            )
        }
        val privateParameters = preset[rolePointer].privateParameterPointers().map {
            Parameter(
                preset[it].visibleName,
                buildGameParameterPointer(it, player),
                buildAttachedButtons(it, preset[it].actionsStubs, player)
            )
        }
        val freeButtons = buildFreeButtons(preset.actionButtons, player)
        return Player(
            player.name,
            player.role,
            globalParameters,
            sharedParameters,
            privateParameters,
            freeButtons
        )
    }

    fun buildByPlayer(preset: Preset, players: List<PlayerDescription>): Game.ByPlayer =
        Model.Game.ByPlayer(players.map { buildPlayer(preset, it) })

    //FIXME
    fun buildRole(
        preset: Preset,
        roleName: String,
        players: List<PlayerDescription>
    ): Role {
        val globalParameters = preset.globalParameterPointers().map {
            Parameter(
                preset[it].visibleName,
                buildGameParameterPointer(it, players[0]),
                buildAttachedButtons(it, preset[it].actionsStubs, players[0])
            )
        }
        //TODO make role pointer creation
        val rolePointer = RolePointer(roleName)
        val sharedParameters = preset[rolePointer].sharedParameterPointers().map {
            Parameter(
                preset[it].visibleName,
                buildGameParameterPointer(it, players[0]),
                buildAttachedButtons(it, preset[it].actionsStubs, players[0])
            )
        }
        val groupedPrivateParameters = players.flatMap { player ->
            preset[rolePointer].privateParameterPointers().map {
                Pair(
                    player.name, Parameter(
                        preset[it].visibleName,
                        buildGameParameterPointer(it, player),
                        buildAttachedButtons(it, preset[it].actionsStubs, player)
                    )
                )
            }
        }.groupBy { it.second.name }
        val blocks = groupedPrivateParameters.map {
            PrivateParameterBlock(it.key,
                it.value.map { GroupPrivateParameter(it.first, it.second) })
        }
        val freeButtons = players.filter { it.role == roleName }.map { ActionButtonsBlock(it.name, buildFreeButtons(preset.actionButtons, it)) }
        return Role(roleName, globalParameters, sharedParameters, blocks, freeButtons)
    }

    fun buildByRole(preset: Preset, players: List<PlayerDescription>): Game.ByRole =
        Model.Game.ByRole(players.groupBy { it.role }.map { buildRole(preset, it.key, it.value) })
}
