package com.example.cockounter.core

import androidx.room.*
import arrow.core.Either
import arrow.core.Left
import arrow.core.Right
import arrow.core.toOption
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
    fun fromParameter(parameter: Parameter): String =
        gson.toJson(parameter)

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
    fun toParameter(data: String): Parameter =
        gson.fromJson(data, Parameter::class.java)

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
    abstract val visibleName: String
    abstract fun initialValueString(): String
    abstract fun typeString(): String
}

data class IntegerParameter(override val name: String, override val visibleName: String, val initialValue: Int) :
    Parameter(), Serializable {
    override fun typeString(): String = typeName

    companion object {
        const val typeName: String = "Integer"
    }

    override fun initialValueString(): String = initialValue.toString()
}

data class DoubleParameter(override val name: String, override val visibleName: String, val initialValue: Double) :
    Parameter(), Serializable {
    override fun typeString(): String = typeName

    companion object {
        const val typeName: String = "Double"
    }

    override fun initialValueString(): String = initialValue.toString()
}

data class StringParameter(override val name: String, override val visibleName: String, val initialValue: String) :
    Parameter(), Serializable {
    override fun typeString(): String = typeName

    companion object {
        const val typeName: String = "String"
    }

    override fun initialValueString(): String = initialValue
}

data class BooleanParameter(override val name: String, override val visibleName: String, val initialValue: Boolean) :
    Parameter(), Serializable {
    override fun typeString(): String = typeName

    companion object {
        const val typeName: String = "Boolean"
    }

    override fun initialValueString(): String = initialValue.toString()
}

data class Script(val name: String, val script: String) : Serializable

fun toParameter(x: Any, name: String, visibleName: String, defaultValue: String): Either<String, Parameter> =
    when (x.toString()) {
        IntegerParameter.typeName -> defaultValue.toIntOrNull().toOption().fold(
            { Left("$defaultValue is not an integer") },
            { Right(IntegerParameter(name, visibleName, it)) })
        StringParameter.typeName -> Right(StringParameter(name, visibleName, defaultValue))
        DoubleParameter.typeName -> defaultValue.toDoubleOrNull().toOption().fold(
            { Left("$defaultValue is not a double") },
            { Right(DoubleParameter(name, visibleName, it)) })
        BooleanParameter.typeName -> Right(BooleanParameter(name, visibleName, defaultValue.toBoolean()))
        else -> Left("Unknown type")
    }

