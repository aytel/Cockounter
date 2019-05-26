package com.example.cockounter

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import com.example.cockounter.adapters.ParameterAdapter
import com.example.cockounter.adapters.ScriptAdapter
import com.example.cockounter.core.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onItemLongClick


class EditPresetActivity : AppCompatActivity() {
    val globalParametersList = mutableListOf<Parameter>()
    val rolesList = mutableListOf<Role>()
    val scriptsList = mutableListOf<Script>()
    val globalParametersAdapter by lazy { ParameterAdapter(this, android.R.layout.simple_list_item_1, globalParametersList) }
    val rolesAdapter by lazy { ArrayAdapter<Role>(this, android.R.layout.simple_list_item_1, rolesList) }
    val scriptsAdapter by lazy { ScriptAdapter(this, android.R.layout.simple_list_item_1, scriptsList) }

    companion object {
        private const val CODE_SHARED_PARAMETER_ADDED = 0
        private const val CODE_ROLE_ADDED = 1
        private const val CODE_SHARED_PARAMETER_CHANGED = 2
        private const val CODE_ROLE_CHANGED = 3
        private const val CODE_SCRIPT_ADDED = 4
        private const val CODE_SCRIPT_CHANGED = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val presetInfo = intent.getSerializableExtra("preset") as PresetInfo?
        if (presetInfo != null) {
            globalParametersList.addAll(presetInfo.preset.globalParameters.values)
            rolesList.addAll(presetInfo.preset.roles.values)
            scriptsList.addAll(presetInfo.preset.globalScripts)
            globalParametersAdapter.notifyDataSetChanged()
            rolesAdapter.notifyDataSetChanged()
            scriptsAdapter.notifyDataSetChanged()
        }
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
                                0 -> startActivityForResult(
                                    intentFor<EditParameterActivity>(
                                        "parameter" to globalParametersList[index],
                                        "position" to index
                                    ), CODE_SHARED_PARAMETER_CHANGED
                                )
                                1 -> {
                                    globalParametersList.removeAt(index)
                                    globalParametersAdapter.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                }
                button("Add new counter") {
                    onClick {
                        startActivityForResult(
                            intentFor<EditParameterActivity>("parameter" to null),
                            CODE_SHARED_PARAMETER_ADDED
                        )
                    }
                }
                listView {
                    adapter = rolesAdapter
                    onItemLongClick { _, _, index, _ ->
                        selector(null, listOf("Edit", "Delete")) { _, i ->
                            when (i) {
                                0 -> startActivityForResult(
                                    intentFor<EditRoleActivity>(
                                        "role" to rolesList[index],
                                        "position" to index
                                    ), CODE_ROLE_CHANGED
                                )
                                1 -> {
                                    rolesList.removeAt(index)
                                    rolesAdapter.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                }
                button("Add new role") {
                    onClick {
                        startActivityForResult(intentFor<EditRoleActivity>("role" to null), CODE_ROLE_ADDED)
                    }
                }
                listView {
                    adapter = scriptsAdapter
                    onItemLongClick { _, _, index, _ ->
                        selector(null, listOf("Edit", "Delete")) { _, i ->
                            when (i) {
                                0 -> startActivityForResult(
                                    intentFor<EditScriptActivity>(
                                        "script" to scriptsList[index],
                                        "position" to index
                                    ), CODE_SCRIPT_CHANGED
                                )
                                1 -> {
                                    scriptsList.removeAt(index)
                                    scriptsAdapter.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                }
                button("Add new script") {
                    onClick {
                        startActivityForResult(intentFor<EditScriptActivity>("role" to null), CODE_SCRIPT_ADDED)
                    }
                }
                listView {

                }
                button("Add library") {

                }
                button("Save") {
                    onClick {
                        val result = Intent()
                        result.putExtra(
                            "newPreset",
                            PresetInfo(
                                name = presetName.text.toString(),
                                description = presetDescription.text.toString(),
                                preset = Preset(
                                    globalParameters = globalParametersList.map { Pair(it.name, it) }.toMap(),
                                    roles = rolesList.map { Pair(it.name, it) }.toMap(),
                                    globalScripts = scriptsList
                                )
                            )
                        )
                        result.putExtra("position", intent.getIntExtra("position", -1))
                        setResult(Activity.RESULT_OK, result)
                        finish()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) {
            return
        }
        when (requestCode) {
            CODE_SHARED_PARAMETER_ADDED -> if (resultCode == Activity.RESULT_OK) {
                globalParametersList.add(data.getSerializableExtra("newParameter") as Parameter)
                globalParametersAdapter.notifyDataSetChanged()
            }
            CODE_ROLE_ADDED -> if (resultCode == Activity.RESULT_OK) {
                rolesList.add(data.getSerializableExtra("newRole") as Role)
                rolesAdapter.notifyDataSetChanged()
            }
            CODE_SHARED_PARAMETER_CHANGED -> if(resultCode == Activity.RESULT_OK) {
                val index = data.getIntExtra("position", -1)
                globalParametersList[index] = data.getSerializableExtra("newParameter") as Parameter
                globalParametersAdapter.notifyDataSetChanged()
            }
            CODE_ROLE_CHANGED -> if(resultCode == Activity.RESULT_OK) {
                val index = data.getIntExtra("position", -1)
                rolesList[index] = data.getSerializableExtra("newRole") as Role
                rolesAdapter.notifyDataSetChanged()
            }
            CODE_SCRIPT_ADDED -> if(resultCode == Activity.RESULT_OK) {
                scriptsList.add(data.getSerializableExtra("newScript") as Script)
                scriptsAdapter.notifyDataSetChanged()
            }
            CODE_SCRIPT_CHANGED -> if(resultCode == Activity.RESULT_OK) {
                val index = data.getIntExtra("position", -1)
                scriptsList[index] = data.getSerializableExtra("newScript") as Script
                scriptsAdapter.notifyDataSetChanged()
            }
        }
    }
}

private class EditPresetUI : AnkoComponent<EditPresetActivity> {
    override fun createView(ui: AnkoContext<EditPresetActivity>): View = with(ui) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

