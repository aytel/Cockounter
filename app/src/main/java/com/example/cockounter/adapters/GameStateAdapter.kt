package com.example.cockounter.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.LinearLayout
import arrow.core.Id
import arrow.core.compose
import arrow.syntax.function.pipe
import com.example.cockounter.core.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class GameStateAdapter(val getState: () -> GameState, val extract: (GameState) -> List<GameParameter>, val parameters: Map<String, Parameter>, val callback: (Script) -> Unit) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View = with(parent!!.context) {
        linearLayout {
            var parameter = getItem(position) as GameParameter
            verticalLayout {
                textView {
                    text = parameter.visibleName
                }

                textView {
                    text = parameter.valueString()
                }
            }
            horizontalScrollView {
                linearLayout {
                    parameters.getValue(parameter.name).attachedScripts.forEach { script ->
                        button(script.name) {
                            onClick {
                                callback(script)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getItem(position: Int): Any = extract(getState())[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = extract(getState()).size
}