package com.example.cockounter.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import arrow.core.Id
import arrow.core.compose
import arrow.syntax.function.pipe
import com.example.cockounter.core.GameParameter
import com.example.cockounter.core.GameState
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout

class GameStateAdapter(val getState: () -> GameState, val extract: (GameState) -> List<GameParameter>) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View = with(parent!!.context) {
        verticalLayout {
            textView {
                text = (getItem(position) as GameParameter).name
            }
            textView {
                text = (getItem(position) as GameParameter).toString()
            }
        }
    }

    override fun getItem(position: Int): Any = extract(getState())[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = extract(getState()).size
}