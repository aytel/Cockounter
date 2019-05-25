package com.example.cockounter.adapters

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.cockounter.core.StateCapture
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout

class StateCaptureAdapter(val list: MutableList<StateCapture>) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View = with(parent!!.context) {
        linearLayout() {
            textView(list[position].name) {
                gravity = Gravity.START
            }
            textView(list[position].date.toString()) {
                gravity = Gravity.END
            }
        }
    }

    override fun getItem(position: Int): Any = list[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = list.size
}