package com.example.cockounter.adapters

import android.widget.BaseExpandableListAdapter
import com.example.cockounter.core.Model
import com.example.cockounter.script.Action


/*
class RoleAdapter(
    private val representation: Model.Player,
    private val perform: (Action) -> Unit
) : BaseExpandableListAdapter() {

    sealed class ElementViewer {
        data class Parameter(val parameter: Model.Parameter) : ElementViewer()
        data class Button(val button: Model.ActionButton) : ElementViewer()
        data class GroupParameter(val parameter: Model.GroupPrivateParameter) : ElementViewer()
        companion object
    }

    interface ElementViewerGameElementShow : GameElementShow<ElementViewer> {
        override fun ElementViewer.buildView(context: Context, gameState: GameState, perform: (Action) -> Unit): View = when(this) {
            is ElementViewer.Parameter -> Model.Parameter.gameElementShow().run { parameter.buildView(context, gameState, perform) }
            is ElementViewer.Button -> Model.ActionButton.gameElementShow().run { button.buildView(context, gameState, perform) }
        }
    }
    fun ElementViewer.Companion.gameElementShow() = object : ElementViewerGameElementShow {}

    sealed class HeaderViewer {
        object GlobalParameters : HeaderViewer()
        object SharedParameters : HeaderViewer()
        object PrivateParameters : HeaderViewer()
        object Buttons : HeaderViewer()
        companion object
    }

    interface HeaderViewerListHeaderShow : ListHeaderShow<HeaderViewer> {
        override fun HeaderViewer.buildView(context: Context, isSelected: Boolean): View = when(this){
            HeaderViewer.GlobalParameters -> SimpleHeader.listHeaderShow().run { SimpleHeader("Global parameters").buildView(context, isSelected) }
            HeaderViewer.SharedParameters -> SimpleHeader.listHeaderShow().run { SimpleHeader("Shared parameters").buildView(context, isSelected) }
            HeaderViewer.PrivateParameters -> SimpleHeader.listHeaderShow().run { SimpleHeader("Private parameters").buildView(context, isSelected) }
            HeaderViewer.Buttons -> SimpleHeader.listHeaderShow().run { SimpleHeader("Actions").buildView(context, isSelected) }
        }
    }
    fun HeaderViewer.Companion.listHeaderShow() = object : HeaderViewerListHeaderShow {}

    private val headers = listOf(HeaderViewer.GlobalParameters, HeaderViewer.SharedParameters, HeaderViewer.PrivateParameters, HeaderViewer.Buttons)
    private val groups = headers.map { listOf<ElementViewer>() }.toMutableList()
    private lateinit var state: GameState

    init {
        groups[0] = representation.globalParameters.map { ElementViewer.Parameter(it) }
        groups[1] = representation.sharedParameters.map { ElementViewer.Parameter(it) }
        groups[2] = representation.privateParameters.map { ElementViewer.Parameter(it) }
        groups[3] = representation.freeButtons.map { ElementViewer.Button(it) }
    }

    override fun getGroup(groupPosition: Int): Any = groups[groupPosition]

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

    override fun hasStableIds(): Boolean = true

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View =
        HeaderViewer.listHeaderShow().run { headers[groupPosition].buildView(parent!!.context, isExpanded) }


    override fun getChildrenCount(groupPosition: Int): Int = groups[groupPosition].size

    override fun getChild(groupPosition: Int, childPosition: Int): Any = groups[groupPosition][childPosition]

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View =
        ElementViewer.gameElementShow().run { groups[groupPosition][childPosition].buildView(parent!!.context, state, perform) }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

    override fun getGroupCount(): Int = headers.size;

    fun update(state: GameState) {
        this.state = state
        notifyDataSetChanged()
    }
}
*/