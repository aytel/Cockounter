package com.example.cockounter.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import arrow.effects.IO
import com.example.cockounter.core.PresetInfo
import org.jetbrains.anko.*
import kotlin.properties.Delegates

class PresetInfoAdapter(val getData: () -> IO<List<PresetInfo>>) : RecyclerView.Adapter<PresetInfoAdapter.PresetViewHolder>() {
    private val presets: List<PresetInfo> = listOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresetViewHolder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemCount(): Int = presets.size

    override fun onBindViewHolder(holder: PresetViewHolder, position: Int) {
        if(position >= presets.size) {
            holder.itemView
        } else {
            holder.bind(presets[position].name)
        }
    }

    class PresetViewHolderUI() : AnkoComponent<ViewGroup> {
        override fun createView(ui: AnkoContext<ViewGroup>): View {
            var name: TextView by Delegates.notNull<TextView>()
            val itemView = with(ui) {
                textView() {
                    name = this
                }
            }
            itemView.tag = PresetViewHolder(itemView, name)
            return itemView
        }
    }

    class PresetViewHolder(itemView: View, val textView: TextView) : RecyclerView.ViewHolder(itemView) {
        fun bind(text: String) {
            textView.text = text
        }
    }
}