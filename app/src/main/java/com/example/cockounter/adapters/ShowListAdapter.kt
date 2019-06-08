package com.example.cockounter.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

class ShowListAdapter<E>(val listElementShow: ListElementShow<E>) : BaseAdapter() {
    private var list: List<E> = listOf()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View =
        listElementShow.run { list[position].buildView(parent!!.context) }

    override fun getItem(position: Int): Any = list[position] as Any

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = list.size

    fun update(list: List<E>) {
        this.list = list
        notifyDataSetChanged()
    }
}