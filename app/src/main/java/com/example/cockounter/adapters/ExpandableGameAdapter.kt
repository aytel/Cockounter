package com.example.cockounter.adapters

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import com.example.cockounter.core.GameState
import com.example.cockounter.script.Action

class ExpandableGameAdapter<H, E>(val headers: List<H>, val listHeaderShow: ListHeaderShow<H>, val gameElementShow: GameElementShow<E>, val state: GameState, val perform: (Action) -> Unit) : BaseExpandableListAdapter(){
    private val groups: MutableList<List<E>> = headers.map { listOf<E>() }.toMutableList()


    override fun getGroup(groupPosition: Int): Any = groups[groupPosition]

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

    override fun hasStableIds(): Boolean = true


    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View =
        listHeaderShow.run { headers[groupPosition].buildView(parent!!.context, isExpanded) }

    override fun getChildrenCount(groupPosition: Int): Int = groups[groupPosition].size

    override fun getChild(groupPosition: Int, childPosition: Int): Any = groups[groupPosition][childPosition] as Any

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View = gameElementShow.run { groups[groupPosition][childPosition].buildView(parent!!.context, state, perform) }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

    override fun getGroupCount(): Int = headers.size
}