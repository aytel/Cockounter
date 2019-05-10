package com.example.cockounter.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.cockounter.core.Parameter
import com.example.cockounter.core.initialValueString
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout

class ParameterAdapter(val ctx: Context, val resource: Int, val items: MutableList<Parameter>) :
    ArrayAdapter<Parameter>(ctx, resource, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = getItem(position)!!
        val view = if (convertView == null) {
            with(parent.context!!) {
                verticalLayout {
                    textView(item.name)
                    textView(item.type)
                    textView(initialValueString(item))
                }
            }
        } else {
            //TODO use convertView
            with(parent.context!!) {
                verticalLayout {
                    textView(item.name)
                    textView(item.type)
                    textView(initialValueString(item))
                }
            }
        }
        return view
    }
}
