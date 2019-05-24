package com.example.cockounter.core

import androidx.room.*
import arrow.core.*
import com.google.gson.Gson
import java.io.Serializable

@Entity
@TypeConverters(PresetConverter::class)
data class Preset(
    @PrimaryKey val name: String,
    val globalParameters: Map<String, Parameter>,
    val roles: Map<String, Role>,
    val globalScripts: List<Script>
) :
    Serializable

@Dao
interface PresetDao {
    @Query("SELECT * from preset")
    fun getAll(): List<Preset>

    @Insert
    fun insert(preset: Preset)

    @Delete
    fun delete(preset: Preset)

    @Query("DELETE FROM preset")
    fun nukeTable()
}

class PresetConverter {
    companion object {
        val gson = Gson()

        data class Parameters(val parameters: Map<String, Parameter>)
        data class Roles(val roles: Map<String, Role>)
        data class Scripts(val scripts: List<Script>)
    }

    @TypeConverter
    fun fromGlobalParameters(globalParameters: Map<String, Parameter>): String =
        gson.toJson(Parameters(globalParameters))

    @TypeConverter
    fun fromRoles(roles: Map<String, Role>): String =
        gson.toJson(Roles(roles))

    @TypeConverter
    fun fromScripts(scripts: List<Script>): String =
        gson.toJson(Scripts(scripts))

    @TypeConverter
    fun toGlobalParameters(data: String): Map<String, Parameter> =
        gson.fromJson(data, Parameters::class.java).parameters

    @TypeConverter
    fun toRoles(data: String): Map<String, Role> =
        gson.fromJson(data, Roles::class.java).roles

    @TypeConverter
    fun toScripts(data: String): List<Script> =
        gson.fromJson(data, Scripts::class.java).scripts
}

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

