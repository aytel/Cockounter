package com.example.cockounter.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.cockounter.core.PresetScript
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout

class PresetScriptAdapter(private var items: MutableList<PresetScript>) : BaseAdapter() {
    override fun getItem(position: Int): Any = items[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = items.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View = with(parent!!.context) {
        val item = getItem(position)
        verticalLayout {
            val textView = textView {
                text = (item as PresetScript).visibleName
            }
        }
    }

    fun update(list: MutableList<PresetScript>) {
        items = list
        notifyDataSetChanged()
    }
}
