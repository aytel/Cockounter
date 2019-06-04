package com.example.cockounter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.cockounter.adapters.ParameterAdapter
import com.example.cockounter.adapters.PresetScriptAdapter
import com.example.cockounter.core.*
import com.example.cockounter.storage.Storage
import com.example.cockounter.storage.loadLibrary
import com.google.android.material.appbar.AppBarLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onItemLongClick


class EditPresetActivity : AppCompatActivity() {
    private val globalParametersList = mutableListOf<Parameter>()
    private val rolesList = mutableListOf<Role>()
    private val scriptsList = mutableListOf<PresetScript>()
    private val librariesList = mutableListOf<Library>()
    private val globalParametersAdapter by lazy {
        ParameterAdapter(
            this,
            android.R.layout.simple_list_item_1,
            globalParametersList
        )
    }
    //TODO make adapter
    private val rolesAdapter by lazy { ArrayAdapter<Role>(this, android.R.layout.simple_list_item_1, rolesList) }
    private val scriptsAdapter by lazy { PresetScriptAdapter(scriptsList) }
    private val librariesAdapter by lazy { ArrayAdapter<Library>(this, android.R.layout.simple_list_item_1, librariesList) }
    private val id by lazy { intent.getIntExtra(ARG_PRESET_ID, 0) }

    companion object {
        const val ARG_PRESET_ID = "ARG_PRESET_ID"
        private const val CODE_SHARED_PARAMETER_ADDED = 0
        private const val CODE_ROLE_ADDED = 1
        private const val CODE_SHARED_PARAMETER_CHANGED = 2
        private const val CODE_ROLE_CHANGED = 3
        private const val CODE_SCRIPT_ADDED = 4
        private const val CODE_SCRIPT_CHANGED = 5
        private const val CODE_LOAD_LIBRARY = 6
        private const val CODE_ADD_LIBRARY = 7
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (id != 0) {
            doAsync {
                val presetInfo = Storage.getPresetInfoById(id).get()
                globalParametersList.addAll(presetInfo.preset.globalParameters.values)
                rolesList.addAll(presetInfo.preset.roles.values)
                scriptsList.addAll(presetInfo.preset.actionsStubs)
                runOnUiThread {
                    globalParametersAdapter.notifyDataSetChanged()
                    rolesAdapter.notifyDataSetChanged()
                    scriptsAdapter.notifyDataSetChanged()
                    EditPresetUI(presetInfo, globalParametersAdapter, rolesAdapter, scriptsAdapter, librariesAdapter).setContentView(this@EditPresetActivity)
                }
            }
        } else {
            EditPresetUI(null, globalParametersAdapter, rolesAdapter, scriptsAdapter, librariesAdapter).setContentView(this@EditPresetActivity)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) {
            return
        }
        when (requestCode) {
            CODE_SHARED_PARAMETER_ADDED -> if (resultCode == Activity.RESULT_OK) {
                globalParametersList.add(data.getSerializableExtra(EditParameterActivity.RETURN_PARAMETER) as Parameter)
                globalParametersAdapter.notifyDataSetChanged()
            }
            CODE_ROLE_ADDED -> if (resultCode == Activity.RESULT_OK) {
                rolesList.add(data.getSerializableExtra(EditRoleActivity.RETURN_ROLE) as Role)
                rolesAdapter.notifyDataSetChanged()
            }
            CODE_SHARED_PARAMETER_CHANGED -> if (resultCode == Activity.RESULT_OK) {
                val index = data.getIntExtra(EditParameterActivity.RETURN_POSITION, -1)
                globalParametersList[index] = data.getSerializableExtra(EditParameterActivity.RETURN_PARAMETER) as Parameter
                globalParametersAdapter.notifyDataSetChanged()
            }
            CODE_ROLE_CHANGED -> if (resultCode == Activity.RESULT_OK) {
                val index = data.getIntExtra(EditRoleActivity.RETURN_POSITION, -1)
                rolesList[index] = data.getSerializableExtra(EditRoleActivity.RETURN_ROLE) as Role
                rolesAdapter.notifyDataSetChanged()
            }
            CODE_SCRIPT_ADDED -> if (resultCode == Activity.RESULT_OK) {
                scriptsList.add(data.getSerializableExtra(EditPresetScriptActivity.RETURN_PRESET_SCRIPT) as PresetScript)
                scriptsAdapter.notifyDataSetChanged()
            }
            CODE_SCRIPT_CHANGED -> if (resultCode == Activity.RESULT_OK) {
                val index = data.getIntExtra(EditPresetScriptActivity.RETURN_POSITION, -1)
                scriptsList[index] = data.getSerializableExtra(EditPresetScriptActivity.RETURN_PRESET_SCRIPT) as PresetScript
                scriptsAdapter.notifyDataSetChanged()
            }
            CODE_LOAD_LIBRARY -> if (resultCode == Activity.RESULT_OK) {
                val uri = data.data!!
                loadLibrary(this, uri).fold({
                    alert(it.message!!).show()
                }, {
                    librariesList.add(Library("lib", it))
                    librariesAdapter.notifyDataSetChanged()
                })
            }
            CODE_ADD_LIBRARY -> if (resultCode == Activity.RESULT_OK) {
                val name = data.getStringExtra(EditLibraryActivity.RETURN_NAME)
                val source = data.getStringExtra(EditLibraryActivity.RETURN_SOURCE)
                librariesList.add(Library(name, source))
                librariesAdapter.notifyDataSetChanged()
            }
        }
    }

    fun editParameter(index: Int) {
        startActivityForResult(
            intentFor<EditParameterActivity>(
                EditParameterActivity.ARG_PARAMETER to globalParametersList[index],
                EditParameterActivity.ARG_POSITION to index
            ), CODE_SHARED_PARAMETER_CHANGED
        )
    }

    fun deleteParameter(index: Int) {
        globalParametersList.removeAt(index)
        globalParametersAdapter.notifyDataSetChanged()
    }

    fun addParameter() {
        startActivityForResult(
            intentFor<EditParameterActivity>(
                EditParameterActivity.ARG_PARAMETER to null
            ),
            CODE_SHARED_PARAMETER_ADDED
        )
    }

    fun editRole(index: Int) {
        startActivityForResult(
            intentFor<EditRoleActivity>(
                EditRoleActivity.ARG_ROLE to rolesList[index],
                EditRoleActivity.ARG_POSITION to index
            ), CODE_ROLE_CHANGED
        )
    }

    fun deleteRole(index: Int) {
        rolesList.removeAt(index)
        rolesAdapter.notifyDataSetChanged()
    }

    fun addRole() {
        startActivityForResult(
            intentFor<EditRoleActivity>(
                EditRoleActivity.ARG_ROLE to null
            ), CODE_ROLE_ADDED
        )
    }

    fun editScript(index: Int) {
        startActivityForResult(
            intentFor<EditPresetScriptActivity>(
                EditPresetScriptActivity.ARG_PRESET_SCRIPT to scriptsList[index],
                EditPresetScriptActivity.ARG_POSITION to index
            ), CODE_SCRIPT_CHANGED
        )
    }

    fun deleteScript(index: Int) {
        scriptsList.removeAt(index)
        scriptsAdapter.notifyDataSetChanged()
    }

    fun addScript() {
        startActivityForResult(
            intentFor<EditPresetScriptActivity>(EditPresetScriptActivity.ARG_PRESET_SCRIPT to null),
            CODE_SCRIPT_ADDED
        )
    }

    fun save(name: String, description: String) {
        val presetInfo = PresetInfo(
                id = id,
                name = name,
                description = description,
                preset = Preset(
                    globalParameters = globalParametersList.map { Pair(it.name, it) }.toMap(),
                    roles = rolesList.map { Pair(it.name, it) }.toMap(),
                    libraries = librariesList.toList(),
                    actionsStubs = scriptsList.toList()
                )
            )
        doAsync {
            Storage.insertPreset(presetInfo);
        }
        setResult(Activity.RESULT_OK)
        finish()
    }

    fun loadLibraryFromFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        startActivityForResult(intent, CODE_LOAD_LIBRARY)
    }

    fun createLibrary() {
        startActivityForResult(intentFor<EditLibraryActivity>(), CODE_ADD_LIBRARY)
    }
}

private class EditPresetUI(
    val presetInfo: PresetInfo?,
    val globalParametersAdapter: ParameterAdapter,
    val rolesAdapter: ArrayAdapter<Role>,
    val scriptsAdapter: PresetScriptAdapter,
    val librariesAdapter: ArrayAdapter<Library>
) : AnkoComponent<EditPresetActivity> {
    private lateinit var presetName: EditText
    private lateinit var presetDescription: EditText
    override fun createView(ui: AnkoContext<EditPresetActivity>): View = with(ui) {
        coordinatorLayout {
            appBarLayout {
                lparams(matchParent, wrapContent) {

                }
                toolbar {
                    //owner.setSupportActionBar(this.toolbar())
                    title = "Edit preset"
                    menu.apply {
                        add("Save").apply {
                            setIcon(R.drawable.ic_done_black_24dp)
                            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                            setOnMenuItemClickListener {
                                owner.save(presetName.text.toString(), presetDescription.text.toString())
                                true
                            }
                        }
                    }
                }.lparams(width = matchParent, height = wrapContent) {
                    scrollFlags = 0
                }

            }
            scrollView {
                verticalLayout {
                    presetName = editText(presetInfo?.name ?: "") {
                        hint = "Name"
                    }
                    presetDescription = editText(presetInfo?.description ?: "") {
                        hint = "Description"
                    }
                    textView("Global counters")
                    listView {
                        adapter = globalParametersAdapter
                        onItemLongClick { _, _, index, _ ->
                            selector(null, listOf("Edit", "Delete")) { _, i ->
                                when (i) {
                                    0 -> owner.editParameter(index)
                                    1 -> owner.deleteParameter(index)
                                }
                            }
                        }
                    }
                    listView {
                        adapter = rolesAdapter
                        onItemLongClick { _, _, index, _ ->
                            selector(null, listOf("Edit", "Delete")) { _, i ->
                                when (i) {
                                    0 -> owner.editRole(index)
                                    1 -> owner.deleteRole(index)
                                }
                            }
                        }
                    }
                    listView {
                        adapter = scriptsAdapter
                        onItemLongClick { _, _, index, _ ->
                            selector(null, listOf("Edit", "Delete")) { _, i ->
                                when (i) {
                                    0 -> owner.editScript(index)
                                    1 -> owner.deleteScript(index)
                                }
                            }
                        }
                    }
                    listView {
                        adapter = librariesAdapter
                    }
                }
                //expandableListView {
                //
                //}
            }.lparams() {
                behavior = AppBarLayout.ScrollingViewBehavior()
            }
            floatingActionButton {
                onClick {
                    selector("", listOf("Counter", "Role", "Action", "Library", "Import library")) { _, i ->
                        when(i) {
                            0 -> owner.addParameter()
                            1 -> owner.addRole()
                            2 -> owner.addScript()
                            3 -> owner.createLibrary()
                            4 -> owner.loadLibraryFromFile()
                        }
                    }
                }
                imageResource = R.drawable.ic_add_white_24dp
            }.lparams(width = wrapContent, height = wrapContent) {
                gravity = Gravity.BOTTOM + Gravity.END
                margin = dip(16)
            }
        }
    }
}

