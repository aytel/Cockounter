package com.example.cockounter.core

import arrow.core.*
import java.io.Serializable

data class Preset(
    val name: String,
    val globalParameters: Map<String, Parameter>,
    val roles: Map<String, Role>,
    val globalScripts: List<Script>
) :
    Serializable

data class Role(
    val name: String,
    val sharedParameters: Map<String, Parameter>,
    val privateParameters: Map<String, Parameter>,
    val scripts: List<Script>
) : Serializable

sealed class Parameter : Serializable {
    abstract val name: String
    abstract val type: String

}

data class IntegerParameter(override val name: String, val initialValue: Int) : Parameter(), Serializable {
    override val type: String = "Integer"
}

data class StringParameter(override val name: String, val initialValue: String) : Parameter(), Serializable {
    override val type: String = "String"
}

data class Script(val name: String, val script: String) : Serializable

fun initialValueString(parameter: Parameter): String = when (parameter) {
    is IntegerParameter -> parameter.initialValue.toString()
    is StringParameter -> parameter.initialValue
}

fun toParameter(x: Any, name: String, defaultValue: String): Either<String, Parameter> = when (x.toString()) {
    "Integer" -> Try { IntegerParameter(name, defaultValue.trim().toInt()) }.fold(
        { Left("$defaultValue is not integer") },
        { Right(it) })
    "String" -> Try { StringParameter(name, defaultValue) }.fold({ Left("$defaultValue is not string") }, { Right(it) })
    else -> Left("Unknown type")
}

