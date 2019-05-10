package com.example.test2.core


/*
fun buildGlobalStateFromPreset(preset: Preset, role: Role): Pair<Player, GameState> {
    val state = HashMap<Parameter<*>, Any>()
    val player = Player(HashMap())
    for(parameter in preset.parameters) {
        state[parameter] = parameter.initialValue!!
        player.accessFunctions[parameter] = { it[parameter]!! }
    }
    for(parameter in role.privateParameters) {
        state[parameter] = parameter.initialValue!!
        player.accessFunctions[parameter] = { it[parameter]!! }
    }
    return Pair(player, MapK(state))
}
*/