package com.example.cockounter.adapters

import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cockounter.core.PresetInfo
import org.jetbrains.anko.*
import kotlin.properties.Delegates

class PresetInfoAdapter(val getData: () -> List<PresetInfo>, private val onItemClick: (Int) -> Unit, private val onLongItemClick: (Int) -> Unit) : RecyclerView.Adapter<PresetInfoAdapter.PresetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresetViewHolder {
        return PresetViewHolderUI().createView(AnkoContext.Companion.create(parent.context, parent)).tag as PresetViewHolder
    }

    override fun getItemCount(): Int = getData().size

    override fun onBindViewHolder(holder: PresetViewHolder, position: Int) {
        val presets = getData()
        if(position >= presets.size) {
            holder.itemView
        } else {
            holder.bind(presets[position].name, presets[position].description, position, onItemClick, onLongItemClick)
        }
    }

    class PresetViewHolderUI() : AnkoComponent<ViewGroup> {
        override fun createView(ui: AnkoContext<ViewGroup>): View {
            var name: TextView by Delegates.notNull<TextView>()
            var description: TextView by Delegates.notNull<TextView>()
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

    class PresetViewHolder(itemView: View, val nameTextView: TextView, val descriptionTextView: TextView) : RecyclerView.ViewHolder(itemView) {
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
}