package com.example.cockounter.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.cockounter.core.*
import com.example.cockounter.script.Action
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class RoleRepresentationAdapter(private val representation: RoleRepresentation, private val getState: () -> GameState, private val perform: (Action) -> Unit) : BaseAdapter() {
    val all = listOf(Border("Global parameters")) +
                representation.globalParameters +
                listOf(Border("Shared parametes")) +
                representation.sharedParameters +
                listOf(Border("Private parameters")) +
                representation.privateParameterBlocks +
                listOf(Border("Actions")) +
                representation.freeButtons

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View = with(parent!!.context) {
        val item = getItem(position)
        val state = getState()
        when(item) {
            is Border -> {
                verticalLayout {
                    textView(item.text) {
                        onClick {
                            //toast("kek")
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
            is PrivateParameterBlockRepresentation -> {
                verticalLayout {
                    item.parameters.forEach {
                        scrollView {
                            linearLayout {
                                verticalLayout {
                                    textView(item.name)
                                    textView(state[it.parameter.parameter].valueString())
                                }
                                it.parameter.attachedButtons.forEach {
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
                }
            }
            else -> {
                verticalLayout {
                    textView("TODO")
                }
            }
        }
    }

    override fun getItem(position: Int): Any = all[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = all.size

    private data class Border(val text: String)

}

