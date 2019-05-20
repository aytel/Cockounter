package com.example.cockounter.core

import androidx.room.*
import com.github.andrewoma.dexx.kollection.ImmutableList
import com.github.andrewoma.dexx.kollection.ImmutableMap
import java.io.Serializable

@Entity
data class GameState(@PrimaryKey
    val sharedParameters: ImmutableMap<String, GameParameter>,
    val roles: ImmutableMap<String, GameRole>
) :
    Serializable

@Dao
interface GameStateDao {
    @Query("SELECT * from gameState")
    fun getAll(): List<GameState>

    @Insert
    fun insert(gameState: GameState)

    @Delete
    fun delete(gameState: GameState)
}

data class GameRole(val name: String, val sharedParameters: ImmutableMap<String, GameParameter>, val players: ImmutableMap<String, Player>) : Serializable

data class Player(val name: String, val privateParameters: ImmutableMap<String, GameParameter>) : Serializable

sealed class GameParameter {
    abstract val name: String
    abstract val valueString: String
}
data class IntegerGameParameter(override val name: String, val value: Int) : GameParameter() {
    override val valueString = "Integer: $value"
}
data class StringGameParameter(override val name: String, val value: String) : GameParameter() {
    override val valueString = "String: $value"
}

operator fun GameState.get(role: String) = roles.getValue(role)

operator fun GameRole.get(player: String) = players.getValue(player)
