package com.example.cockounter.adapters

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.cockounter.core.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class GameStateAdapter(val getState: () -> GameState, val extract: (GameState) -> List<Pair<GameParameter, Parameter>>, val callback: (ActionButton) -> Unit) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View = with(parent!!.context) {
        linearLayout {
            val (gameParameter, parameter) = extract(getState())[position]
            verticalLayout {
                textView {
                    text = parameter.visibleName
                }

                textView {
                    text = gameParameter.valueString()
                }
            }
            Log.i("AdapterCnt", parameter.attachedActionButtons.size.toString())
            horizontalScrollView {
                linearLayout {
                    parameter.attachedActionButtons.forEach { script ->
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