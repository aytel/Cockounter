package com.example.cockounter.adapters

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.View
import arrow.extension
import com.example.cockounter.core.*
import com.example.cockounter.script.Action
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

interface ListElementShow<F> {
    fun F.buildView(context: Context): View
}

interface ParameterListElementShow : ListElementShow<Parameter> {
    override fun Parameter.buildView(context: Context): View = with(context) {
        verticalLayout {
            padding = dip(8)
            textView {
                setPadding(
                    dimenAttr(android.R.attr.expandableListPreferredChildPaddingLeft),
                    paddingTop,
                    paddingRight,
                    paddingBottom
                )
                textSize = 17f
                text = this@buildView.name
            }
        }
    }
}

fun Parameter.Companion.listElementShow(): ListElementShow<Parameter> = object : ParameterListElementShow {}

interface RoleListElementShow : ListElementShow<Role> {
    override fun Role.buildView(context: Context): View = with(context) {
        verticalLayout {
            padding = dip(8)
            textView {
                setPadding(
                    dimenAttr(android.R.attr.expandableListPreferredChildPaddingLeft),
                    paddingTop,
                    paddingRight,
                    paddingBottom
                )
                text = this@buildView.name
            }
        }
    }
}

fun Role.Companion.listElementShow(): ListElementShow<Role> = object : RoleListElementShow {}

interface PresetScriptListElementShow : ListElementShow<PresetScript> {
    override fun PresetScript.buildView(context: Context): View = with(context) {
        verticalLayout {
            padding = dip(8)
            textView {
                setPadding(
                    dimenAttr(android.R.attr.expandableListPreferredChildPaddingLeft),
                    paddingTop,
                    paddingRight,
                    paddingBottom
                )
                text = this@buildView.visibleName
            }
        }
    }
}

fun PresetScript.Companion.listElementShow(): ListElementShow<PresetScript> = object : PresetScriptListElementShow {}

interface LibraryElementShow : ListElementShow<Library> {
    override fun Library.buildView(context: Context): View = with(context) {
        verticalLayout {
            padding = dip(8)
            textView {
                setPadding(
                    dimenAttr(android.R.attr.expandableListPreferredChildPaddingLeft),
                    paddingTop,
                    paddingRight,
                    paddingBottom
                )
                text = this@buildView.name
            }
        }
    }
}

fun Library.Companion.listElementShow(): ListElementShow<Library> = object : LibraryElementShow {}

interface StateCaptureElementShow : ListElementShow<StateCapture> {
    override fun StateCapture.buildView(context: Context): View = with(context) {
        relativeLayout() {
            lparams(matchParent, wrapContent) {
            }
            textView {
                text = name
                textSize = 20f
            }.lparams {
                alignParentStart()
            }
            textView {
                text = date.toString()
            }.lparams() {
                alignParentEnd()
            }
        }
    }
}

fun StateCapture.Companion.listElementShow() = object : StateCaptureElementShow {}

interface GameElementShow<F> {
    fun F.buildView(context: Context, gameState: GameState, perform: (Action) -> Unit): View
}

interface ParameterRepresentationElementShow : GameElementShow<Model.Parameter> {
    override fun Model.Parameter.buildView(context: Context, gameState: GameState, perform: (Action) -> Unit): View =
        with(context) {
            linearLayout {
                verticalLayout {
                    textView(name)
                    textView(gameState[parameter].valueString())
                }
                horizontalScrollView {
                    linearLayout {
                        attachedButtons.forEach {
                            val action = it.action
                            button(it.text) {
                                onClick {
                                    perform(action)
                                }
                            }
                        }
                    }
                }.lparams() {
                    gravity = Gravity.END
                }
            }
        }
}

fun Model.Parameter.Companion.gameElementShow() = object : ParameterRepresentationElementShow {}

interface ActionElementShow : GameElementShow<Model.ActionButton> {
    override fun Model.ActionButton.buildView(context: Context, gameState: GameState, perform: (Action) -> Unit): View =
        with(context) {
            verticalLayout {
                padding = dip(2)
                button(text) {
                    onClick {
                        perform(action)
                        Log.d("Action", "Action: $text performed")
                    }
                }
            }
        }
}

fun Model.ActionButton.Companion.gameElementShow() = object : ActionElementShow {}


interface ListHeaderShow<F> {
    fun F.buildView(context: Context, isSelected: Boolean): View
}

data class SimpleHeader(val text: String) {
    companion object
}

@extension
interface SimpleHeaderListHeaderShow : ListHeaderShow<SimpleHeader> {
    override fun SimpleHeader.buildView(context: Context, isSelected: Boolean): View = with(context) {
        verticalLayout {
            padding = dip(8)
            textView {
                setPadding(
                    dimenAttr(android.R.attr.expandableListPreferredItemPaddingLeft),
                    paddingTop,
                    paddingRight,
                    paddingBottom
                )
                textSize = 17f

                text = this@buildView.text
            }
        }
    }
}


