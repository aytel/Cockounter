package com.example.cockounter.core

import androidx.room.*
import arrow.core.Either
import arrow.core.Left
import arrow.core.Right
import arrow.core.toOption
import com.google.gson.*
import java.io.Serializable
import java.lang.reflect.Type
import kotlin.reflect.KClass

@Entity
@TypeConverters(PresetConverter::class)
data class PresetInfo(
    @PrimaryKey
    val name: String,
    val description: String,
    val preset: Preset
) : Serializable

data class Preset(
    val globalParameters: Map<String, Parameter>,
    val roles: Map<String, Role>,
    val globalScripts: List<Script>
) :
    Serializable

@Dao
interface PresetInfoDao {
    @Query("SELECT * from presetInfo")
    fun getAll(): List<PresetInfo>

    @Insert
    fun insert(preset: PresetInfo)

    @Delete
    fun delete(preset: PresetInfo)

    @Query("DELETE FROM presetInfo")
    fun nukeTable()
}

class InterfaceAdapter<T: Any>: JsonSerializer<T>, JsonDeserializer<T> {
    override fun serialize(src: T, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val member = JsonObject()
        member.addProperty("type", src.javaClass.name)
        member.add("data", context!!.serialize(src))
        return member
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): T {
        val member = json as JsonObject
        val actualType = Class.forName(member.get("type").asString)
        return context!!.deserialize(member.get("data"), actualType)
    }

}

class PresetConverter {
    companion object {
        val gson = GsonBuilder().registerTypeAdapter(Parameter::class.java, InterfaceAdapter<Parameter>()).create()
    }

    @TypeConverter
    fun fromPreset(preset: Preset): String =
        gson.toJson(preset)

    fun fromPresetInfo(presetInfo: PresetInfo): String =
        gson.toJson(presetInfo)

    @TypeConverter
    fun toPreset(data: String): Preset =
        gson.fromJson(data, Preset::class.java)

    fun toPresetInfo(data: String): PresetInfo =
        gson.fromJson(data, PresetInfo::class.java)

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
    abstract val attachedScripts: List<Script>
}

data class IntegerParameter(
    override val name: String, override val visibleName: String, val initialValue: Int,
    override val attachedScripts: List<Script>
) :
    Parameter(), Serializable {
    override fun typeString(): String = typeName

    companion object {
        const val typeName: String = "Integer"
    }

    override fun initialValueString(): String = initialValue.toString()
}

data class DoubleParameter(
    override val name: String,
    override val visibleName: String,
    val initialValue: Double,
    override val attachedScripts: List<Script>
) :
    Parameter(), Serializable {
    override fun typeString(): String = typeName

    companion object {
        const val typeName: String = "Double"
    }

    override fun initialValueString(): String = initialValue.toString()
}

data class StringParameter(
    override val name: String,
    override val visibleName: String,
    val initialValue: String,
    override val attachedScripts: List<Script>
) :
    Parameter(), Serializable {
    override fun typeString(): String = typeName

    companion object {
        const val typeName: String = "String"
    }

    override fun initialValueString(): String = initialValue
}

data class BooleanParameter(
    override val name: String,
    override val visibleName: String,
    val initialValue: Boolean,
    override val attachedScripts: List<Script>
) :
    Parameter(), Serializable {
    override fun typeString(): String = typeName

    companion object {
        const val typeName: String = "Boolean"
    }

    override fun initialValueString(): String = initialValue.toString()
}

data class Script(val name: String, val script: String, val context: ScriptContext) : Serializable

enum class ScriptContext {
    NONE, X, PLAYER, FULL
}

fun toParameter(x: Any, name: String, visibleName: String, defaultValue: String, attachedScripts: List<Script>): Either<String, Parameter> =
    when (x.toString()) {
        IntegerParameter.typeName -> defaultValue.toIntOrNull().toOption().fold(
            { Left("$defaultValue is not an integer") },
            { Right(IntegerParameter(name, visibleName, it, attachedScripts)) })
        StringParameter.typeName -> Right(StringParameter(name, visibleName, defaultValue, attachedScripts))
        DoubleParameter.typeName -> defaultValue.toDoubleOrNull().toOption().fold(
            { Left("$defaultValue is not a double") },
            { Right(DoubleParameter(name, visibleName, it, attachedScripts)) })
        BooleanParameter.typeName -> Right(BooleanParameter(name, visibleName, defaultValue.toBoolean(), attachedScripts))
        else -> Left("Unknown type")
    }

