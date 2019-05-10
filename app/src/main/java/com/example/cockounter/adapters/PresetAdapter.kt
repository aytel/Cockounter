package com.example.cockounter.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.cockounter.core.Preset
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout

class PresetAdapter(val ctx: Context, val resoure: Int, val items: MutableList<Preset>) : ArrayAdapter<Preset>(ctx, resoure, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View = with(parent!!.context) {
        val item = getItem(position)!!
        verticalLayout {
            val textView = textView {
                text = item.name
            }
        }
    }
}
