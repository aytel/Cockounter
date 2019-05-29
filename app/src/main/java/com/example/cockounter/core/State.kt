package com.example.cockounter.core

import androidx.room.*
import com.github.andrewoma.dexx.kollection.ImmutableMap
import com.github.andrewoma.dexx.kollection.immutableMapOf
import com.github.andrewoma.dexx.kollection.toImmutableMap
import com.google.gson.GsonBuilder
import java.io.Serializable
import java.util.*

@Entity
@TypeConverters(StateCaptureConverter::class)
data class StateCapture(
    @PrimaryKey
    val name: String,
    val state: GameState,
    val preset: Preset,
    val players: List<PlayerDescription>,
    val date: Date,
    val uuid: UUID
)

data class GameState(
    val globalParameters: Map<String, GameParameter>,
    val roles: Map<String, GameRole>,
    val version: Int = 0
) :
    Serializable

@Dao
interface StateCaptureDao {
    @Query("SELECT * from stateCapture")
    fun getAll(): List<StateCapture>

    @Insert
    fun insert(stateCapture: StateCapture)

    @Delete
    fun delete(stateCapture: StateCapture)
}

class StateCaptureConverter {
    companion object {
        val gson = GsonBuilder().registerTypeAdapter(GameParameter::class.java, InterfaceAdapter<GameParameter>())
            .registerTypeAdapter(Parameter::class.java, InterfaceAdapter<Parameter>()).create()
        data class PlayersWrapper(val players: List<PlayerDescription>)
    }

    @TypeConverter
    fun fromGameState(gameState: GameState): String =
        gson.toJson(gameState)

    @TypeConverter
    fun fromPreset(preset: Preset): String =
        gson.toJson(preset)

    @TypeConverter
    fun fromPlayers(players: List<PlayerDescription>): String =
        gson.toJson(PlayersWrapper(players))

    @TypeConverter
    fun fromDate(date: Date): String =
        gson.toJson(date)

    @TypeConverter
    fun toGameState(data: String): GameState =
        gson.fromJson(data, GameState::class.java)

    @TypeConverter
    fun toPreset(data: String): Preset =
        gson.fromJson(data, Preset::class.java)

    @TypeConverter
    fun toPlayers(data: String): List<PlayerDescription> =
        gson.fromJson(data, PlayersWrapper::class.java).players

    @TypeConverter
    fun toDate(data: String): Date =
        gson.fromJson(data, Date::class.java)
}

val dummyState = GameState(immutableMapOf(), immutableMapOf())

data class GameRole(
    val name: String,
    val sharedParameters: Map<String, GameParameter>,
    val players: Map<String, Player>
) : Serializable

data class Player(val name: String, val privateParameters: Map<String, GameParameter>) : Serializable

sealed class GameParameter : Serializable {
    abstract val name: String
    abstract val visibleName: String
    abstract fun valueString(): String
}

data class IntegerGameParameter(override val name: String, override val visibleName: String, val value: Int) :
    GameParameter() {
    override fun valueString() = "Integer: $value"
}

data class StringGameParameter(override val name: String, override val visibleName: String, val value: String) :
    GameParameter() {
    override fun valueString() = "String: $value"
}

data class DoubleGameParameter(override val name: String, override val visibleName: String, val value: Double) :
    GameParameter() {
    override fun valueString() = "Double: $value"
}

data class BooleanGameParameter(override val name: String, override val visibleName: String, val value: Boolean) :
    GameParameter() {
    override fun valueString() = "Boolean: $value"
}

operator fun GameState.get(role: String) = roles.getValue(role)

operator fun GameRole.get(player: String) = players.getValue(player)

data class PlayerDescription(val name: String, val role: String)

operator fun GameState.get(description: PlayerDescription) = get(description.role)[description.name]


fun buildGameParameter(parameter: Parameter) = when (parameter) {
    is IntegerParameter -> IntegerGameParameter(parameter.name, parameter.visibleName, parameter.initialValue)
    is StringParameter -> StringGameParameter(parameter.name, parameter.visibleName, parameter.initialValue)
    is DoubleParameter -> DoubleGameParameter(parameter.name, parameter.visibleName, parameter.initialValue)
    is BooleanParameter -> BooleanGameParameter(parameter.name, parameter.visibleName, parameter.initialValue)
}

fun buildPlayer(role: Role, playerName: PlayerDescription) =
    Player(playerName.name, role.privateParameters.mapValues { buildGameParameter(it.value) }.toImmutableMap())

fun buildState(preset: Preset, players: List<PlayerDescription>): GameState {
    val globalParameters = preset.globalParameters.mapValues { buildGameParameter(it.value) }.toImmutableMap()
    val byRole = players.map { Pair(it.role, buildPlayer(preset.roles.getValue(it.role), it)) }.groupBy { it.first }
        .mapValues { it.value.map { it.second } }
    val roles = preset.roles.mapValues { (key, v) ->
        GameRole(
            key,
            v.sharedParameters.mapValues { buildGameParameter(it.value) }.toImmutableMap(),
            byRole.getValue(key).map { Pair(it.name, it) }.toImmutableMap()
        )
    }
    return GameState(globalParameters, roles.toImmutableMap())
}
