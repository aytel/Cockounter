package com.example.cockounter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.cockounter.adapters.PresetAdapter
import com.example.cockounter.core.PlayerDescription
import com.example.cockounter.core.Preset
import com.example.cockounter.core.PresetInfo
import com.example.cockounter.core.buildState
import com.example.cockounter.storage.Storage
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onItemClick
import org.jetbrains.anko.sdk27.coroutines.onItemLongClick

private const val PRESET_ADDED = 0
private const val PRESET_CHANGED = 1
private const val START_GAME = 2

class SelectPresetActivity : AppCompatActivity() {

    private val presetsList = mutableListOf<PresetInfo>()
    private val presetsAdapter: PresetAdapter by lazy { PresetAdapter(this, 0, presetsList) }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(data == null) {
            return
        }
        when(requestCode) {
            PRESET_ADDED -> if(resultCode == 0) {
                val preset = data.getSerializableExtra("newPreset") as PresetInfo
                presetsList.add(preset)
                presetsAdapter.notifyDataSetChanged()
                Storage.insertPreset(preset)
            }
            PRESET_CHANGED -> if(resultCode == 0) {
                val position = data.getIntExtra("position", -1)
                val preset = data.getSerializableExtra("newPreset") as PresetInfo
                Storage.deletePreset(presetsList[position])
                presetsList[position] = preset
                presetsAdapter.notifyDataSetChanged()
                Storage.insertPreset(preset)
            }
            START_GAME -> if(resultCode == 0) {
                val position = data.getIntExtra("position", -1)
                val names = data.getStringArrayExtra("names")!!
                val roles = data.getStringArrayExtra("roles")!!
                startActivity(intentFor<AdminGameScreenActivity>(
                    AdminGameScreenActivity.INIT_FLAG to AdminGameScreenActivity.FLAG_BUILD_NEW_STATE,
                    AdminGameScreenActivity.ARG_PLAYER_NAMES to names,
                    AdminGameScreenActivity.ARG_PLAYER_ROLES to roles,
                    AdminGameScreenActivity.ARG_PRESET to presetsList[position])
                )
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presetsList.addAll(Storage.getAllPresetInfos().get())
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
                    onItemClick { p0, p1, index, p3 ->
                        val roleNames = presetsList[index].preset.roles.keys.toTypedArray()
                        startActivityForResult(intentFor<StartSinglePlayerGameActivity>("roles" to roleNames, "position" to index), START_GAME)
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



