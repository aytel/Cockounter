package com.example.cockounter.adapters

import android.content.Context
import android.view.View
import arrow.core.extensions.eq
import arrow.extension
import com.example.cockounter.core.Library
import com.example.cockounter.core.Parameter
import com.example.cockounter.core.PresetScript
import com.example.cockounter.core.Role
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout

interface ListElementShow<F> {
    fun F.buildView(context: Context): View
}

@extension
interface ParameterListElementShow : ListElementShow<Parameter> {
    override fun Parameter.buildView(context: Context): View = with(context) {
        verticalLayout {
            textView {
                text = this@buildView.name
            }
        }
    }
}
//fun Parameter.Companion.listElementShow(): ListElementShow<Parameter> = object : ParameterListElementShow {}

@extension
interface RoleListElementShow : ListElementShow<Role> {
    override fun Role.buildView(context: Context): View = with(context) {
        verticalLayout {
            textView {
                text = this@buildView.name
            }
        }
    }
}

//fun Role.Companion.listElementShow(): ListElementShow<Role> = object : RoleListElementShow {}

@extension
interface PresetScriptListElementShow : ListElementShow<PresetScript> {
    override fun PresetScript.buildView(context: Context): View = with(context) {
        verticalLayout {
            textView {
                text = this@buildView.visibleName
            }
        }
    }
}

//fun PresetScript.Companion.listElementShow(): ListElementShow<PresetScript> = object : PresetScriptListElementShow {}

@extension
interface LibraryElementShow : ListElementShow<Library> {
    override fun Library.buildView(context: Context): View = with(context) {
        verticalLayout {
            textView {
                text = this@buildView.name
            }
        }
    }
}

//fun Library.Companion.listElementShow(): ListElementShow<Library> = object : LibraryElementShow {}


interface ListHeaderShow<F> {
    fun F.buildView(context: Context, isSelected: Boolean): View
}

data class SimpleHeader(val text: String) {
    companion object
}

@extension
interface SimpleHeaderListHeaderShow : ListHeaderShow<SimpleHeader> {
    override fun SimpleHeader.buildView(context: Context, isSelected: Boolean): View  = with(context) {
        verticalLayout {
            textView {
                text = this@buildView.text
            }
        }
    }
}