package com.example.cockounter.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.cockounter.core.Script
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout

class ScriptAdapter(val ctx: Context, val resoure: Int, val items: MutableList<Script>) : ArrayAdapter<Script>(ctx, resoure, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View = with(parent!!.context) {
        val item = getItem(position)!!
        verticalLayout {
            val textView = textView {
                text = item.name
            }
        }
    }
}
