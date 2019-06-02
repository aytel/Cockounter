package com.example.cockounter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import arrow.core.None
import arrow.core.Some
import com.example.cockounter.adapters.PresetAdapter
import com.example.cockounter.core.PresetInfo
import com.example.cockounter.storage.Storage
import com.example.cockounter.storage.loadPreset
import com.example.cockounter.storage.savePreset
import com.google.android.material.appbar.AppBarLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.design.themedAppBarLayout
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onItemClick
import org.jetbrains.anko.sdk27.coroutines.onItemLongClick

private const val PRESET_ADDED = 0
private const val PRESET_CHANGED = 1
private const val START_GAME = 2
private const val START_MULTI_PLAYER_GAME = 3
private const val LOAD_FILE = 4;
private const val SAVE_FILE = 5;


class SelectPresetActivity : AppCompatActivity() {

    companion object {
        const val REQUEST = "REQUEST"
        const val REQUEST_SINGLE_PLAYER_GAME = 0
        const val REQUEST_MULTI_PLAYER_GAME = 1
        private enum class GameType {
            SINGLE, MULTI
        }
    }
    private val presetsList = mutableListOf<PresetInfo>()
    private val presetsAdapter: PresetAdapter by lazy { PresetAdapter(this, 0, presetsList) }
    private var presetToSave: PresetInfo? = null
    private val gameType by lazy { when(intent.getIntExtra(REQUEST, -1)) {
        REQUEST_SINGLE_PLAYER_GAME -> Some(GameType.SINGLE)
        REQUEST_MULTI_PLAYER_GAME -> Some(GameType.MULTI)
        else -> None
    }}

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
            START_MULTI_PLAYER_GAME -> if(resultCode == Activity.RESULT_OK) {
                val position = data.getIntExtra("position", -1)
                val names = data.getStringArrayExtra("names")!!
                val roles = data.getStringArrayExtra("roles")!!
                Log.i("Multi", "Start game")
                alert {
                    customView {
                        val name = editText {
                            hint = "Your in-game name"
                        }
                        yesButton {
                            startActivity(
                                intentFor<MultiplayerGameActivity>(
                                    MultiplayerGameActivity.MODE to MultiplayerGameActivity.MODE_CREATE_GAME,
                                    MultiplayerGameActivity.ARG_NAME to name.text.toString(),
                                    MultiplayerGameActivity.ARG_PRESET to presetsList[position].preset,
                                    MultiplayerGameActivity.ARG_PLAYER_NAMES to names,
                                    MultiplayerGameActivity.ARG_PLAYER_ROLES to roles
                                    //MultiplayerGameActivity.ARG_UUID to uuid
                                )
                            )
                        }
                    }
                }.show()

            }
            LOAD_FILE -> if (resultCode == Activity.RESULT_OK){
                val uri = data.data!!
                loadPreset(this, uri).fold({
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
        doAsync {
            presetsList.addAll(Storage.getAllPresetInfos().get())
            runOnUiThread {
                SelectPresetUI(presetsAdapter).setContentView(this@SelectPresetActivity)
            }
        }
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
        when(gameType) {
            None -> toast("Error")
            is Some -> when((gameType as Some<GameType>).t) {
                Companion.GameType.SINGLE -> {
                    startActivityForResult(
                        intentFor<StartSinglePlayerGameActivity>(
                            "roles" to roleNames,
                            "position" to index
                        ), START_GAME
                    )
                }
                Companion.GameType.MULTI -> {
                    startActivityForResult(intentFor<StartMultiPlayerGameActivity>("roles" to roleNames, "position" to index), START_MULTI_PLAYER_GAME)
                }
            }
        }
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
        coordinatorLayout {
            appBarLayout {
                lparams(matchParent, wrapContent) {

                }
                toolbar {
                    //owner.setSupportActionBar(this.toolbar())
                    title = "Select preset"
                    menu.apply {
                        add("Import preset").apply {
                            setIcon(R.drawable.ic_folder_open_black_24dp)
                            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                            setOnMenuItemClickListener {
                                owner.loadPresetFromFile()
                                true
                            }
                        }
                    }
                }.lparams(width = matchParent, height = wrapContent) {
                    scrollFlags = 0
                }

            }
            //verticalLayout {
                listView {
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
                }.lparams(width = matchParent, height = wrapContent) {
                    behavior = AppBarLayout.ScrollingViewBehavior()
                }
            //}
            floatingActionButton {
                onClick {
                    selector(null, listOf("Create preset", "Import preset")) { _, i ->
                        when(i) {
                            0 -> owner.createPreset()
                            1 -> owner.loadPresetFromFile()
                        }
                    }
                }
                imageResource = R.drawable.ic_add_white_24dp
            }.lparams {
                width = wrapContent
                height = wrapContent
                margin = dip(16)
                gravity = Gravity.BOTTOM or Gravity.END
            }
        }
    }
}


