package com.example.cockounter.core

import androidx.room.*
import com.github.andrewoma.dexx.kollection.ImmutableMap
import com.github.andrewoma.dexx.kollection.toImmutableList
import com.github.andrewoma.dexx.kollection.toImmutableMap
import java.io.Serializable

data class GameState(
    @PrimaryKey
    val sharedParameters: ImmutableMap<String, GameParameter>,
    val roles: ImmutableMap<String, GameRole>
) :
    Serializable

data class GameRole(
    val name: String,
    val sharedParameters: ImmutableMap<String, GameParameter>,
    val players: ImmutableMap<String, Player>
) : Serializable

data class Player(val name: String, val privateParameters: ImmutableMap<String, GameParameter>) : Serializable

sealed class GameParameter : Serializable {
    abstract val name: String
}

data class IntegerGameParameter(override val name: String, val value: Int) : GameParameter() {
    override fun toString(): String {
        return "Integer: $value"
    }
}

data class StringGameParameter(override val name: String, val value: String) : GameParameter() {
    override fun toString(): String {
        return "String: $value"
    }
}

operator fun GameState.get(role: String) = roles.getValue(role)

operator fun GameRole.get(player: String) = players.getValue(player)

data class PlayerDescription(val name: String, val role: String)

operator fun GameState.get(description: PlayerDescription) = get(description.role)[description.name]

fun buildGameParameter(parameter: Parameter) = when (parameter) {
    is IntegerParameter -> IntegerGameParameter(parameter.name, parameter.initialValue)
    is StringParameter -> StringGameParameter(parameter.name, parameter.initialValue)
}

fun buildPlayer(role: Role, playerName: PlayerDescription) =
    Player(playerName.name, role.privateParameters.mapValues { buildGameParameter(it.value) }.toImmutableMap())

fun buildState(preset: Preset, players: List<PlayerDescription>): GameState {
    val globalParameters = preset.globalParameters.mapValues { buildGameParameter(it.value) }.toImmutableMap()
    val byRole = players.map { Pair(it.role, buildPlayer(preset.roles.getValue(it.role), it))}.groupBy { it.first }.mapValues { it.value.map {it.second} }
    val roles = preset.roles.mapValues { (key, v) ->
        GameRole(key, v.sharedParameters.mapValues { buildGameParameter(it.value) }.toImmutableMap(), byRole.getValue(key).map{Pair(it.name, it)}.toImmutableMap())
    }
    return GameState(globalParameters, roles.toImmutableMap())
}
