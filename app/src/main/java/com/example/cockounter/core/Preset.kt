package com.example.cockounter.core

import arrow.core.Failure
import arrow.core.None
import arrow.core.Some
import arrow.core.Try
import arrow.data.ListK
import arrow.optics.optics
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

data class Preset(val name: String, val globalParameters: Map<String, Parameter>, val roles: Map<String, Role>) : Serializable

data class Role(val name: String, val sharedParameters: Map<String, Parameter>, val privateParameters: Map<String, Parameter>) : Serializable

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

fun initialValueString(parameter: Parameter): String = when(parameter) {
    is IntegerParameter -> parameter.initialValue.toString()
    is StringParameter -> parameter.initialValue
    else -> "Error type"
}

fun toParameter(x: Any, name: String, defaultValue: String): Try<Parameter> = when (x.toString()) {
    "Integer" -> Try { IntegerParameter(name, defaultValue.toInt()) }
    "String" -> Try { StringParameter(name, defaultValue) }
    else -> Failure(IllegalArgumentException("Can't convert x to Parameter"))
}
