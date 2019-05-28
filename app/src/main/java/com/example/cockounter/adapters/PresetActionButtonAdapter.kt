package com.example.cockounter.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import com.example.cockounter.core.ActionButton
import com.example.cockounter.core.ActionButtonModel
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout


class PresetActionButtonAdapter(private val items: MutableList<ActionButtonModel>, private var actionFilter: (ActionButtonModel) -> Boolean = {false}) : BaseAdapter() {


    override fun getItem(position: Int): Any = items.filter(actionFilter)[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = items.filter(actionFilter).size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View = with(parent!!.context) {
        val item = getItem(position)
        verticalLayout {
            val textView = textView {
                //TODO
                text = item.toString()
            }
        }
    }

    fun setFilter(filter: (ActionButtonModel) -> Boolean) {
        actionFilter = filter
    }

    //TODO remove
    fun removeAt(position: Int) {
        items.remove(getItem(position))
    }

    //TODO remove
    fun setAt(position: Int, item: ActionButtonModel) {
        TODO()
    }
}
