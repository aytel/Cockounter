package com.example.cockounter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.cockounter.adapters.ParameterAdapter
import com.example.cockounter.adapters.PresetScriptAdapter
import com.example.cockounter.core.*
import com.example.cockounter.storage.loadLibrary
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onItemLongClick


class EditPresetActivity : AppCompatActivity() {
    val globalParametersList = mutableListOf<Parameter>()
    val rolesList = mutableListOf<Role>()
    val scriptsList = mutableListOf<PresetScript>()
    val librariesList = mutableListOf<Library>()
    val globalParametersAdapter by lazy {
        ParameterAdapter(
            this,
            android.R.layout.simple_list_item_1,
            globalParametersList
        )
    }
    //TODO make adapter
    val rolesAdapter by lazy { ArrayAdapter<Role>(this, android.R.layout.simple_list_item_1, rolesList) }
    val scriptsAdapter by lazy { PresetScriptAdapter(scriptsList) }
    val actionButtons = mutableListOf<ActionButtonModel>()
    val librariesAdapter by lazy { ArrayAdapter<Library>(this, android.R.layout.simple_list_item_1, librariesList) }

    companion object {
        const val ARG_POSITION = "ARG_POSITION"
        const val ARG_PRESET_INFO = "ARG_PRESET_INFO"
        const val RETURN_PRESET_INFO = "RETURN_PRESET_INFO"
        const val RETURN_POSITION = "RETURN_POSITION"
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
        val presetInfo = intent.getSerializableExtra(ARG_PRESET_INFO) as PresetInfo?
        if (presetInfo != null) {
            globalParametersList.addAll(presetInfo.preset.globalParameters.values)
            rolesList.addAll(presetInfo.preset.roles.values)
            actionButtons.addAll(presetInfo.preset.actionButtons)
            scriptsList.addAll(actionButtons.flatMap {
                when (it) {
                    is ActionButtonModel.Global -> listOf(it.script)
                    else -> listOf()
                }
            })
            actionButtons.removeAll {
                when (it) {
                    is ActionButtonModel.Global -> true
                    else -> false
                }
            }
            globalParametersAdapter.notifyDataSetChanged()
            rolesAdapter.notifyDataSetChanged()
            scriptsAdapter.notifyDataSetChanged()
        }
        EditPresetUI(presetInfo, globalParametersAdapter, rolesAdapter, scriptsAdapter, librariesAdapter).setContentView(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) {
            return
        }
        when (requestCode) {
            CODE_SHARED_PARAMETER_ADDED -> if (resultCode == Activity.RESULT_OK) {
                globalParametersList.add(data.getSerializableExtra(EditParameterActivity.RETURN_PARAMETER) as Parameter)
                actionButtons.clear()
                actionButtons.addAll(data.getSerializableExtra(EditParameterActivity.RETURN_ATTACHED_ACTIONS) as List<ActionButtonModel>)
                globalParametersAdapter.notifyDataSetChanged()
            }
            CODE_ROLE_ADDED -> if (resultCode == Activity.RESULT_OK) {
                rolesList.add(data.getSerializableExtra(EditRoleActivity.RETURN_ROLE) as Role)
                actionButtons.clear()
                actionButtons.addAll(data.getSerializableExtra(EditRoleActivity.RETURN_ACTIONS) as List<ActionButtonModel>)
                rolesAdapter.notifyDataSetChanged()
            }
            CODE_SHARED_PARAMETER_CHANGED -> if (resultCode == Activity.RESULT_OK) {
                val index = data.getIntExtra(EditParameterActivity.RETURN_POSITION, -1)
                actionButtons.clear()
                actionButtons.addAll(data.getSerializableExtra(EditParameterActivity.RETURN_ATTACHED_ACTIONS) as List<ActionButtonModel>)
                globalParametersList[index] = data.getSerializableExtra(EditParameterActivity.RETURN_PARAMETER) as Parameter
                globalParametersAdapter.notifyDataSetChanged()
            }
            CODE_ROLE_CHANGED -> if (resultCode == Activity.RESULT_OK) {
                val index = data.getIntExtra(EditRoleActivity.RETURN_POSITION, -1)
                actionButtons.clear()
                actionButtons.addAll(data.getSerializableExtra(EditRoleActivity.RETURN_ACTIONS) as List<ActionButtonModel>)
                rolesList[index] = data.getSerializableExtra(EditRoleActivity.RETURN_ROLE) as Role
                rolesAdapter.notifyDataSetChanged()
            }
            CODE_SCRIPT_ADDED -> if (resultCode == Activity.RESULT_OK) {
                scriptsList.add(data.getSerializableExtra(EditButtonDescriptionActivity.RETURN_PRESET_SCRIPT) as PresetScript)
                scriptsAdapter.notifyDataSetChanged()
            }
            CODE_SCRIPT_CHANGED -> if (resultCode == Activity.RESULT_OK) {
                val index = data.getIntExtra(EditButtonDescriptionActivity.RETURN_POSITION, -1)
                scriptsList[index] = data.getSerializableExtra(EditButtonDescriptionActivity.RETURN_PRESET_SCRIPT) as PresetScript
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
                EditParameterActivity.ARG_ACTIONS to actionButtons.toList(),
                EditParameterActivity.ARG_PARAMETER_POINTER to ParameterPointer.Global(globalParametersList[index].name),
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
                EditParameterActivity.ARG_PARAMETER to null,
                EditParameterActivity.ARG_ACTIONS to actionButtons.toList()
            ),
            CODE_SHARED_PARAMETER_ADDED
        )
    }

    fun editRole(index: Int) {
        startActivityForResult(
            intentFor<EditRoleActivity>(
                EditRoleActivity.ARG_ROLE to rolesList[index],
                EditRoleActivity.ARG_ACTIONS to actionButtons.toList(),
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
                EditRoleActivity.ARG_ROLE to null,
                EditRoleActivity.ARG_ACTIONS to actionButtons.toList()
            ), CODE_ROLE_ADDED
        )
    }

    fun editScript(index: Int) {
        startActivityForResult(
            intentFor<EditButtonDescriptionActivity>(
                EditButtonDescriptionActivity.ARG_ACTION_BUTTON to ActionButtonModel.Global(scriptsList[index]),
                EditButtonDescriptionActivity.ARG_POSITION to index
            ), CODE_SCRIPT_CHANGED
        )
    }

    fun deleteScript(index: Int) {
        scriptsList.removeAt(index)
        scriptsAdapter.notifyDataSetChanged()
    }

    fun addScript() {
        startActivityForResult(
            intentFor<EditButtonDescriptionActivity>(EditButtonDescriptionActivity.ARG_ACTION_BUTTON to null),
            CODE_SCRIPT_ADDED
        )
    }

    fun save(name: String, description: String) {
        val result = Intent()
        result.run {
            putExtra(
                RETURN_PRESET_INFO,
                PresetInfo(
                    name = name,
                    description = description,
                    preset = Preset(
                        globalParameters = globalParametersList.map { Pair(it.name, it) }.toMap(),
                        roles = rolesList.map { Pair(it.name, it) }.toMap(),
                        actionButtons = actionButtons + scriptsList.map{ ActionButtonModel.Global(it) },
                        libraries = librariesList.toList()
                    )
                )
            )
            putExtra(RETURN_POSITION, intent.getIntExtra(ARG_POSITION, -1))
        }
        setResult(Activity.RESULT_OK, result)
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
    override fun createView(ui: AnkoContext<EditPresetActivity>): View = with(ui) {
        scrollView {
            verticalLayout {
                val presetName = editText(presetInfo?.name ?: "") {
                    hint = "Name"
                }
                val presetDescription = editText(presetInfo?.description ?: "") {
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
                button("Add new counter") {
                    onClick {
                        owner.addParameter()
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
                button("Add new role") {
                    onClick {
                        owner.addRole()
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
                button("Add new actionButton") {
                    onClick {
                        owner.addScript()
                    }
                }
                listView {
                    adapter = librariesAdapter
                }
                button("Create library") {
                    onClick {
                        owner.createLibrary()
                    }
                }
                button("Load library") {
                    onClick {
                        owner.loadLibraryFromFile()
                    }
                }
                button("Save") {
                    onClick {
                        owner.save(presetName.text.toString(), presetDescription.text.toString())
                    }
                }
            }
        }
    }
}

