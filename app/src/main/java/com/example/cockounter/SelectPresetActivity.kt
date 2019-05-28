package com.example.cockounter

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.room.TypeConverter
import com.example.cockounter.adapters.PresetAdapter
import com.example.cockounter.core.PresetConverter
import com.example.cockounter.core.PresetInfo
import com.example.cockounter.storage.Storage
import com.example.cockounter.storage.loadPreset
import com.example.cockounter.storage.savePreset
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onItemClick
import org.jetbrains.anko.sdk27.coroutines.onItemLongClick
import java.io.File
import java.lang.Exception
import java.nio.charset.Charset

private const val PRESET_ADDED = 0
private const val PRESET_CHANGED = 1
private const val START_GAME = 2
private const val LOAD_FILE = 3;
private const val SAVE_FILE = 4;


class SelectPresetActivity : AppCompatActivity() {

    private val presetsList = mutableListOf<PresetInfo>()
    private val presetsAdapter: PresetAdapter by lazy { PresetAdapter(this, 0, presetsList) }
    private var presetToSave: PresetInfo? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(data == null) {
            return
        }
        when(requestCode) {
            PRESET_ADDED -> if(resultCode == Activity.RESULT_OK) {
                val preset = data.getSerializableExtra(EditPresetActivity.RETURN_PRESET_INFO) as PresetInfo
                presetsList.add(preset)
                presetsAdapter.notifyDataSetChanged()
                Storage.insertPreset(preset)
            }
            PRESET_CHANGED -> if(resultCode == Activity.RESULT_OK) {
                val position = data.getIntExtra(EditPresetActivity.RETURN_POSITION, -1)
                val preset = data.getSerializableExtra(EditPresetActivity.RETURN_PRESET_INFO) as PresetInfo
                Storage.deletePreset(presetsList[position])
                presetsList[position] = preset
                presetsAdapter.notifyDataSetChanged()
                Storage.insertPreset(preset)
            }
            START_GAME -> if(resultCode == Activity.RESULT_OK) {
                val position = data.getIntExtra("position", -1)
                val names = data.getStringArrayExtra("names")!!
                val roles = data.getStringArrayExtra("roles")!!
                startActivity(intentFor<AdminGameScreenActivity>(
                    AdminGameScreenActivity.MODE to AdminGameScreenActivity.MODE_BUILD_NEW_STATE,
                    AdminGameScreenActivity.ARG_PLAYER_NAMES to names,
                    AdminGameScreenActivity.ARG_PLAYER_ROLES to roles,
                    AdminGameScreenActivity.ARG_PRESET to presetsList[position].preset)
                )
                finish()
            }
            LOAD_FILE -> if (resultCode == Activity.RESULT_OK){
                val uri = data.data!!
                val preset = loadPreset(this, uri).fold({
                    alert(it.message!!).show()
                }, {
                    presetsList.add(it)
                    presetsAdapter.notifyDataSetChanged()
                    Storage.insertPreset(it)
                })
            }
            SAVE_FILE -> if (resultCode == Activity.RESULT_OK){
                val uri = data.data!!
                if(presetToSave != null) {
                    savePreset(this, uri, presetToSave!!).fold({
                        alert(it.message!!).show()
                    }, {
                        toast("Saved")
                    })
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presetsList.addAll(Storage.getAllPresetInfos().get())
        SelectPresetUI(presetsAdapter).setContentView(this)
    }

    fun editPreset(index: Int) {
        startActivityForResult(
            intentFor<EditPresetActivity>(
                EditPresetActivity.ARG_PRESET_INFO to presetsList[index],
                EditPresetActivity.ARG_POSITION to index
            ), PRESET_CHANGED
        )
    }

    fun deletePreset(index: Int) {
        Storage.deletePreset(presetsList[index])
        presetsList.removeAt(index)
        presetsAdapter.notifyDataSetChanged()
    }

    fun startGame(index: Int) {
        val roleNames = presetsList[index].preset.roles.keys.toTypedArray()
        startActivityForResult(intentFor<StartSinglePlayerGameActivity>("roles" to roleNames, "position" to index), START_GAME)
    }

    fun createPreset() {
        startActivityForResult(intentFor<EditPresetActivity>("preset" to null), PRESET_ADDED)
    }

    fun loadPresetFromFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        startActivityForResult(intent, LOAD_FILE)
    }

    fun loadPresetToFile(index: Int) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "*/*"
        presetToSave = presetsList[index]
        startActivityForResult(intent, SAVE_FILE)
    }
}

private class SelectPresetUI(val presetsAdapter: PresetAdapter) : AnkoComponent<SelectPresetActivity> {
    override fun createView(ui: AnkoContext<SelectPresetActivity>): View = with(ui) {
        scrollView {
            verticalLayout {
                val listView = listView {
                    adapter = presetsAdapter
                    onItemLongClick { _, _, index, _ ->
                        selector(null, listOf("Edit", "Delete", "Export")) { _, i ->
                            when (i) {
                                0 -> owner.editPreset(index)
                                1 -> owner.deletePreset(index)
                                2 -> owner.loadPresetToFile(index)
                            }
                        }
                    }
                    onItemClick { p0, p1, index, p3 ->
                        owner.startGame(index)
                    }
                }
                button("New preset") {
                    onClick {
                        owner.createPreset()
                    }
                }
                button("Load preset") {
                    onClick {
                        owner.loadPresetFromFile()
                    }
                }
            }
        }
    }
}


