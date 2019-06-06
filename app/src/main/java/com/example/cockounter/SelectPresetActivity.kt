package com.example.cockounter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cockounter.adapters.PresetInfoAdapter
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
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk27.coroutines.onClick



class SelectPresetActivity : AppCompatActivity() {

    companion object {
        const val RETURN_PRESET_ID = "RETURN_PRESET_ID"
        private const val CODE_PRESET_ADDED = 0
        private const val CODE_PRESET_CHANGED = 1
        private const val CODE_START_GAME = 2
        private const val CODE_START_MULTI_PLAYER_GAME = 3
        private const val CODE_LOAD_FILE = 4;
        private const val CODE_SAVE_FILE = 5;
    }

    private val presetsList = mutableListOf<PresetInfo>()
    private val presetsAdapter: PresetInfoAdapter by lazy {
        PresetInfoAdapter({ presetsList }, ::returnPreset, { index ->
            selector(null, listOf("Edit", "Delete", "Export")) { _, i ->
                when (i) {
                    0 -> editPreset(index)
                    1 -> deletePreset(index)
                    2 -> loadPresetToFile(index)
                }
            }
        })
    }
    private var presetToSave: PresetInfo? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) {
            return
        }
        when (requestCode) {
            CODE_PRESET_ADDED -> if (resultCode == Activity.RESULT_OK) {
                presetsAdapter.notifyDataSetChanged()
            }
            CODE_PRESET_CHANGED -> if (resultCode == Activity.RESULT_OK) {
                presetsAdapter.notifyDataSetChanged()
            }
            /*
            CODE_START_GAME -> if (resultCode == Activity.RESULT_OK) {
                val position = data.getIntExtra("position", -1)
                val names = data.getStringArrayExtra("names")!!
                val roles = data.getStringArrayExtra("roles")!!
                startActivity(
                    intentFor<SinglePlayerGameScreenActivity>(
                        SinglePlayerGameScreenActivity.MODE to SinglePlayerGameScreenActivity.MODE_BUILD_NEW_STATE,
                        SinglePlayerGameScreenActivity.ARG_PLAYER_NAMES to names,
                        SinglePlayerGameScreenActivity.ARG_PLAYER_ROLES to roles,
                        SinglePlayerGameScreenActivity.ARG_PRESET to presetsList[position].preset
                    )
                )
                finish()
            }
            CODE_START_MULTI_PLAYER_GAME -> if (resultCode == Activity.RESULT_OK) {
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
                }.listElementShow()

            }
            */
            //FIXME
            CODE_LOAD_FILE -> if (resultCode == Activity.RESULT_OK) {
                val uri = data.data!!
                loadPreset(this, uri).fold({
                    alert(it.message!!).show()
                }, {
                    presetsList.add(it)
                    presetsAdapter.notifyDataSetChanged()
                    Storage.insertPreset(it)
                })
            }
            //FIXME
            CODE_SAVE_FILE -> if (resultCode == Activity.RESULT_OK) {
                val uri = data.data!!
                if (presetToSave != null) {
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
        val data = Storage.getAllPresetInfos()
        data.observe(this, Observer { list ->
            run {
                presetsList.clear()
                presetsList.addAll(list)
                presetsAdapter.notifyDataSetChanged()
            }
        })

        SelectPresetUI(presetsAdapter).setContentView(this@SelectPresetActivity)
    }

    fun editPreset(index: Int) {
        startActivityForResult(
            intentFor<EditPresetActivity>(
                EditPresetActivity.ARG_PRESET_ID to presetsList[index].id
            ), CODE_PRESET_CHANGED
        )
    }

    fun deletePreset(index: Int) {
        Storage.deletePreset(presetsList[index])
        presetsList.removeAt(index)
        presetsAdapter.notifyDataSetChanged()
    }

    fun returnPreset(index: Int) {
        val result = Intent()
        result.putExtra(RETURN_PRESET_ID, presetsList[index].id)
        setResult(Activity.RESULT_OK, result)
        finish()
    }

    fun createPreset() {
        startActivityForResult(intentFor<EditPresetActivity>(), CODE_PRESET_ADDED)
    }

    fun loadPresetFromFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        startActivityForResult(intent, CODE_LOAD_FILE)
    }

    fun loadPresetToFile(index: Int) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "*/*"
        presetToSave = presetsList[index]
        startActivityForResult(intent, CODE_SAVE_FILE)
    }
}

private class SelectPresetUI(val presetsAdapter: PresetInfoAdapter) : AnkoComponent<SelectPresetActivity> {
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
                            //setIcon(R.drawable.ic_folder_open_black_24dp)
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
            recyclerView {
                adapter = presetsAdapter
                layoutManager = LinearLayoutManager(ui.owner)
                addItemDecoration(DividerItemDecoration(owner, LinearLayoutManager.VERTICAL))
            }.lparams(width = matchParent, height = matchParent) {
                behavior = AppBarLayout.ScrollingViewBehavior()
            }
            floatingActionButton {
                onClick {
                    owner.createPreset()
                }
                //imageResource = R.drawable.ic_add_white_24dp
            }.lparams {
                width = wrapContent
                height = wrapContent
                margin = dip(16)
                gravity = Gravity.BOTTOM or Gravity.END
            }
        }
    }
}


