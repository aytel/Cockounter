package com.example.cockounter.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import com.example.cockounter.adapters.simpleheader.listHeaderShow.listHeaderShow
import com.example.cockounter.core.GameState
import com.example.cockounter.core.Model
import com.example.cockounter.script.Action


class RoleAdapter(
    private val representation: Model.Role,
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
            is ElementViewer.GroupParameter -> Model.GroupPrivateParameter.gameElementShow().run { parameter.buildView(context, gameState, perform) }
        }
    }
    fun ElementViewer.Companion.gameElementShow() = object : ElementViewerGameElementShow {}

    sealed class HeaderViewer {
        object GlobalParameters : HeaderViewer()
        object SharedParameters : HeaderViewer()
        data class PrivateParameters(val name: String) : HeaderViewer()
        data class Buttons(val player: String) : HeaderViewer()
        companion object
    }

    interface HeaderViewerListHeaderShow : ListHeaderShow<HeaderViewer> {
        override fun HeaderViewer.buildView(context: Context, isSelected: Boolean): View = when(this){
            HeaderViewer.GlobalParameters -> SimpleHeader.listHeaderShow().run { SimpleHeader("Global parameters").buildView(context, isSelected) }
            HeaderViewer.SharedParameters -> SimpleHeader.listHeaderShow().run { SimpleHeader("Shared parameters").buildView(context, isSelected) }
            is HeaderViewer.PrivateParameters -> SimpleHeader.listHeaderShow().run { SimpleHeader(name).buildView(context, isSelected) }
            is HeaderViewer.Buttons -> SimpleHeader.listHeaderShow().run { SimpleHeader(player).buildView(context, isSelected) }
        }
    }
    fun HeaderViewer.Companion.listHeaderShow() = object : HeaderViewerListHeaderShow {}

    private val headers = listOf(HeaderViewer.GlobalParameters, HeaderViewer.SharedParameters) +
            representation.privateParameterBlocks.map { HeaderViewer.PrivateParameters(it.name) } +
            representation.freeButtons.map { HeaderViewer.Buttons(it.player) }
    private val groups = headers.map { header ->  when(header) {
            HeaderViewer.GlobalParameters -> representation.globalParameters.map { ElementViewer.Parameter(it) }
            HeaderViewer.SharedParameters -> representation.sharedParameters.map { ElementViewer.Parameter(it) }
            is HeaderViewer.PrivateParameters -> representation.privateParameterBlocks.find { it.name == header.name }!!.parameters.map { ElementViewer.GroupParameter(it) }
            is HeaderViewer.Buttons -> representation.freeButtons.find { it.player == header.player }!!.buttons.map { ElementViewer.Button(it) }
        } }.toMutableList()
    private lateinit var state: GameState


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
