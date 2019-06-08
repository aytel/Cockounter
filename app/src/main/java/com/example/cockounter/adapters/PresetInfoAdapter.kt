package com.example.cockounter.adapters

import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cockounter.core.PresetInfo
import org.jetbrains.anko.*
import kotlin.properties.Delegates

class PresetInfoAdapter(private val onItemClick: (Int) -> Unit, private val onLongItemClick: (Int) -> Unit) : RecyclerView.Adapter<PresetInfoAdapter.PresetViewHolder>() {
    private var data = listOf<PresetInfo>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresetViewHolder {
        return PresetViewHolderUI().createView(AnkoContext.Companion.create(parent.context, parent)).tag as PresetViewHolder
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: PresetViewHolder, position: Int) {
        val presets = data
        if(position >= presets.size) {
            holder.itemView
        } else {
            holder.bind(presets[position].name, presets[position].description, position, onItemClick, onLongItemClick)
        }
    }

    class PresetViewHolderUI() : AnkoComponent<ViewGroup> {
        override fun createView(ui: AnkoContext<ViewGroup>): View {
            var name: TextView by Delegates.notNull()
            var description: TextView by Delegates.notNull()
            val itemView = with(ui) {
                verticalLayout {
                    lparams(matchParent, wrapContent)
                    textView {
                        name = this
                        textSize = 32f
                    }
                    textView {
                        description = this
                    }
                    with(TypedValue()) {
                        context.theme.resolveAttribute(
                            android.R.attr.selectableItemBackground,
                            this,
                            true
                        )
                        setBackgroundResource(resourceId)
                    }
                }
            }
            itemView.tag = PresetViewHolder(itemView, name, description)
            return itemView
        }
    }

    class PresetViewHolder(itemView: View, private val nameTextView: TextView, private val descriptionTextView: TextView) : RecyclerView.ViewHolder(itemView) {
        fun bind(name: String, description: String, position: Int, onItemClick: (Int) -> Unit, onLongItemClick: (Int) -> Unit) {
            nameTextView.text = name
            descriptionTextView.text = description
            itemView.setOnClickListener {
                onItemClick(position)
            }
            itemView.setOnLongClickListener {
                onLongItemClick(position)
                true
            }
        }
    }

    fun update(list: List<PresetInfo>) {
        data = list
        notifyDataSetChanged()
    }

    operator fun get(index: Int) = data[index]
}