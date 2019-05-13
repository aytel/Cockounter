package com.example.cockounter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.cockounter.adapters.PresetAdapter
import com.example.cockounter.core.Preset
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onItemClick
import org.jetbrains.anko.sdk27.coroutines.onItemLongClick

private const val PRESET_ADDED = 0
private const val PRESET_CHANGED = 1

class SelectPresetActivity : AppCompatActivity() {

    private val presetsList = mutableListOf<Preset>()
    private val presetsAdapter: PresetAdapter by lazy { PresetAdapter(this, 0, presetsList) }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(data == null) {
            return
        }
        when(requestCode) {
            PRESET_ADDED -> if(resultCode == 0) {
                presetsList.add(data.getSerializableExtra("newPreset") as Preset)
                presetsAdapter.notifyDataSetChanged()
            }
            PRESET_CHANGED -> if(resultCode == 0) {
                val position = data.getIntExtra("position", -1)
                presetsList[position] = data.getSerializableExtra("newPreset") as Preset
                presetsAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scrollView {
            verticalLayout {
                val listView = listView {
                    adapter = presetsAdapter
                    onItemLongClick { _, _, index, _ ->
                        selector(null, listOf("Edit", "Delete")) { _, i ->
                            if (i == 0) {
                                startActivityForResult(
                                    intentFor<EditPresetActivity>(
                                        "preset" to presetsList[index],
                                        "position" to index
                                    ), PRESET_CHANGED
                                )
                            } else if (i == 1) {
                                presetsList.removeAt(index)
                                presetsAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                    onItemClick { p0, p1, p2, p3 ->
                        startActivity(intentFor<PlayerGameScreenActivity>())
                    }
                }
                button("New preset") {
                    onClick {
                        startActivityForResult(intentFor<EditPresetActivity>("preset" to null), PRESET_ADDED)
                    }
                }
            }
        }
    }
}



