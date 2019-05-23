package com.example.cockounter.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import com.example.cockounter.core.Player
import com.example.cockounter.core.PlayerDescription
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout

class PlayersAdapter(val players: MutableList<PlayerDescription>) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View = with(parent!!.context) {
        verticalLayout {
            textView(players[position].name)
            textView(players[position].role)
        }
    }

    override fun getItem(position: Int): Any = players[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = players.size
}
