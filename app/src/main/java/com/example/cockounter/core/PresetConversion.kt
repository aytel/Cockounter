package com.example.cockounter.core

import arrow.extension
import arrow.typeclasses.Show
import com.google.gson.Gson

private data class RoleWrapper(
    val name: String,
    val sharedParameters: Map<String, String>,
    val privateParameters: Map<String, String>,
    val scripts: List<Script>
)

@extension
interface PresetShow: Show<Preset> {
    override fun Preset.show(): String
}

@extension
interface ParameterShow: Show<Parameter> {
    override fun Parameter.show(): String = when(this) {
        is IntegerParameter -> Gson().toJson(this)
        is DoubleParameter -> Gson().toJson(this)
        is StringParameter -> Gson().toJson(this)
        is BooleanParameter -> Gson().toJson(this)
    }
}


