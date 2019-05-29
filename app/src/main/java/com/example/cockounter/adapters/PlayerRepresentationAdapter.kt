package com.example.cockounter.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.cockounter.core.*
import com.example.cockounter.script.Action
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class PlayerRepresentationAdapter(private val representation: PlayerRepresentation, private val getState: () -> GameState, private val perform: (Action) -> Unit) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View = with(parent!!.context) {
        val item = getItem(position)
        val state = getState()
        when(item) {
            is Border -> {
                verticalLayout {
                    textView(item.text) {
                        onClick {
                            toast("kek")
                        }
                    }
                }
            }
            is ParameterRepresentation -> {
                scrollView {
                    linearLayout {
                        verticalLayout {
                            textView(item.name)
                            textView(state[item.parameter].valueString())
                        }
                        item.attachedButtons.forEach {
                            val action = it.action
                            button(it.text) {
                                onClick {
                                    perform(action)
                                }
                            }
                        }
                        //button("test")
                    }
                }
            }
            is ActionButtonRepresentation -> {
                verticalLayout {
                    button(item.text) {
                        onClick {
                            perform(item.action)
                            toast("click")
                        }
                    }
                }
            }
            else -> {
                verticalLayout {
                    textView("TODO")
                }
            }
        }
    }

    override fun getItem(position: Int): Any {
        val list = listOf(Border("Global parameters")) +
                representation.globalParameters +
                listOf(Border("Shared parameters")) +
                representation.sharedParameters +
                listOf(Border("Private parameters")) +
                representation.privateParameters +
                listOf(Border("Actions")) +
                representation.freeButtons
        return list[position]
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = 4 + representation.globalParameters.size + representation.sharedParameters.size + representation.privateParameters.size + representation.freeButtons.size

    private data class Border(val text: String)
}

