package com.example.cockounter.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cockounter.R
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
import java.lang.Exception


class SelectPresetActivity : AppCompatActivity() {

    companion object {
        /**
         * Key for returned to the previous activity id of the selected preset
         */
        const val RETURN_PRESET_ID = "RETURN_PRESET_ID"
        private const val CODE_PRESET_ADDED = 0
        private const val CODE_PRESET_CHANGED = 1
        private const val CODE_LOAD_FILE = 4;
        private const val CODE_SAVE_FILE = 5;
    }

    private val presetsAdapter: PresetInfoAdapter by lazy {
        PresetInfoAdapter(::returnPreset) { index ->
            selector(null, listOf("Edit", "Delete", "Export")) { _, i ->
                when (i) {
                    0 -> editPreset(index)
                    1 -> deletePreset(index)
                    2 -> loadPresetToFile(index)
                }
            }
        }
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
            CODE_LOAD_FILE -> if (resultCode == Activity.RESULT_OK) {
                val uri = data.data!!
                try {
                    val presetInfo = loadPreset(this, uri)
                    doAsync {
                        Storage.insertPreset(presetInfo)
                    }
                } catch (e: Exception) {
                    alert(e.message!!).show()
                }
            }
            CODE_SAVE_FILE -> if (resultCode == Activity.RESULT_OK) {
                val uri = data.data!!
                if (presetToSave != null) {
                    try {
                        savePreset(this, uri, presetToSave!!)
                        toast("Saved")
                    } catch (e: Exception) {
                        alert(e.message!!).show()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = Storage.getAllPresetInfos()
        data.observe(this, Observer { presetsAdapter.update(it)})
        SelectPresetUI(presetsAdapter).setContentView(this@SelectPresetActivity)
    }

    private fun editPreset(index: Int) {
        startActivityForResult(
            intentFor<EditPresetActivity>(
                EditPresetActivity.ARG_PRESET_ID to presetsAdapter[index].id
            ), CODE_PRESET_CHANGED
        )
    }

    private fun deletePreset(index: Int) {
        doAsync {
            Storage.deletePreset(presetsAdapter[index])
        }
    }

    private fun returnPreset(index: Int) {
        val result = Intent()
        result.putExtra(RETURN_PRESET_ID, presetsAdapter[index].id)
        setResult(Activity.RESULT_OK, result)
        finish()
    }

    private fun createPreset() {
        startActivityForResult(intentFor<EditPresetActivity>(),
            CODE_PRESET_ADDED
        )
    }

    private fun loadPresetFromFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        startActivityForResult(intent, CODE_LOAD_FILE)
    }

    private fun loadPresetToFile(index: Int) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.type = "*/*"
        presetToSave = presetsAdapter[index]
        startActivityForResult(intent, CODE_SAVE_FILE)
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
                            add("Import").apply {
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
}



