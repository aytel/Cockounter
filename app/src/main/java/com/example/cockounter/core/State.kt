package com.example.cockounter.core

import com.github.andrewoma.dexx.kollection.ImmutableList
import com.github.andrewoma.dexx.kollection.ImmutableMap

data class GameState(val sharedParameters: ImmutableMap<String, Any>, val roles: ImmutableMap<String, GameRole>, val playersByRole: ImmutableMap<String, ImmutableList<Player>>)

data class Player(val name: String, val role: GameRole, val privateParameters: ImmutableMap<String, Any>)

data class GameRole(val name: String, val sharedParameters: ImmutableMap<String, Any>)
