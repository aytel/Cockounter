package com.example.cockounter.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import arrow.core.None
import com.example.cockounter.core.*
import com.example.cockounter.script.Action
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class ExpandablePlayerRepresentationAdapter(
    private val representation: PlayerRepresentation,
    private val getState: () -> GameState,
    private val perform: (Action) -> Unit
) : BaseExpandableListAdapter() {

    override fun getGroup(groupPosition: Int): Any =when(groupPosition) {
            0 -> representation.globalParameters
            1 -> representation.sharedParameters
            2 -> representation.privateParameters
            3 -> representation.freeButtons
            else -> None
        }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

    override fun hasStableIds(): Boolean = true

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View = with(parent!!.context) {
        verticalLayout {
            textView {
                text = when(groupPosition) {
                    0 -> "Global parameters"
                    1 -> "Shared parameters"
                    2 -> "Private parameters"
                    3 -> "Actions"
                    else -> "Nothing"
                }
            }
        }
    }

    override fun getChildrenCount(groupPosition: Int): Int = (getGroup(groupPosition) as List<*>).size

    override fun getChild(groupPosition: Int, childPosition: Int): Any = (getGroup(groupPosition) as List<*>)[childPosition]!!

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View = with(parent!!.context) {
        when(groupPosition) {
            in 0..2 -> {
                val item = getChild(groupPosition, childPosition) as ParameterRepresentation
                val state = getState()
                scrollView {
                    linearLayout {
                        verticalLayout {
                            textView(item.name)
                            textView(state[item.parameter].valueString())
                        }
                        item.attachedButtons.forEach {
                            val action = it.action
                            button(it.text) {
                                onClick {
                                    perform(action)
                                }
                            }
                        }
                        //button("test")
                    }
                }
            }
            3 -> {
                val item = getChild(groupPosition, childPosition) as ActionButtonRepresentation
                verticalLayout {
                    button(item.text) {
                        onClick {
                            perform(item.action)
                            toast("click")
                        }
                    }
                }
            }
            else -> {
                verticalLayout {
                    textView("TODO")
                }
            }
        }
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

    override fun getGroupCount(): Int = 4;
}