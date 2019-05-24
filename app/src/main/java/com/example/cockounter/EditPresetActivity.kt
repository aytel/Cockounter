package com.example.cockounter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.example.cockounter.adapters.ParameterAdapter
import com.example.cockounter.adapters.ScriptAdapter
import com.example.cockounter.core.Parameter
import com.example.cockounter.core.Preset
import com.example.cockounter.core.Role
import com.example.cockounter.core.Script
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onItemLongClick

private const val SHARED_PARAMETER_ADDED = 0
private const val ROLE_ADDED = 1
private const val SHARED_PARAMETER_CHANGED = 2
private const val ROLE_CHANGED = 3
private const val SCRIPT_ADDED = 4
private const val SCRIPT_CHANGED = 5

class EditPresetActivity : AppCompatActivity() {
    val globalParametersList = mutableListOf<Parameter>()
    val rolesList = mutableListOf<Role>()
    val scriptsList = mutableListOf<Script>()
    val globalParametersAdapter by lazy { ParameterAdapter(this, android.R.layout.simple_list_item_1, globalParametersList) }
    val rolesAdapter by lazy { ArrayAdapter<Role>(this, android.R.layout.simple_list_item_1, rolesList) }
    val scriptsAdapter by lazy { ScriptAdapter(this, android.R.layout.simple_list_item_1, scriptsList) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preset = intent.getSerializableExtra("preset") as Preset?
        if (preset != null) {
            globalParametersList.addAll(preset.globalParameters.values)
            rolesList.addAll(preset.roles.values)
            scriptsList.addAll(preset.globalScripts)
            globalParametersAdapter.notifyDataSetChanged()
            rolesAdapter.notifyDataSetChanged()
            scriptsAdapter.notifyDataSetChanged()
        }
        scrollView {
            verticalLayout {
                val presetName = editText(preset?.name ?: "") {
                    hint = "Name"
                }
                val presetDescription = editText(preset?.description ?: "") {
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
                                    ), SHARED_PARAMETER_CHANGED
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
                            SHARED_PARAMETER_ADDED
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
                                    ), ROLE_CHANGED
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
                        startActivityForResult(intentFor<EditRoleActivity>("role" to null), ROLE_ADDED)
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
                                    ), SCRIPT_CHANGED
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
                        startActivityForResult(intentFor<EditScriptActivity>("role" to null), SCRIPT_ADDED)
                    }
                }
                button("Save") {
                    onClick {
                        val result = Intent()
                        result.putExtra(
                            "newPreset",
                            Preset(
                                name = presetName.text.toString(),
                                description = presetDescription.text.toString(),
                                globalParameters = globalParametersList.map { Pair(it.name, it) }.toMap(),
                                roles = rolesList.map { Pair(it.name, it) }.toMap(),
                                globalScripts = scriptsList
                            )
                        )
                        result.putExtra("position", intent.getIntExtra("position", -1))
                        setResult(0, result)
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
            SHARED_PARAMETER_ADDED -> if (resultCode == 0) {
                globalParametersList.add(data.getSerializableExtra("newParameter") as Parameter)
                globalParametersAdapter.notifyDataSetChanged()
            }
            ROLE_ADDED -> if (resultCode == 0) {
                rolesList.add(data.getSerializableExtra("newRole") as Role)
                rolesAdapter.notifyDataSetChanged()
            }
            SHARED_PARAMETER_CHANGED -> if(resultCode == 0) {
                val index = data.getIntExtra("position", -1)
                globalParametersList[index] = data.getSerializableExtra("newParameter") as Parameter
                globalParametersAdapter.notifyDataSetChanged()
            }
            ROLE_CHANGED -> if(resultCode == 0) {
                val index = data.getIntExtra("position", -1)
                rolesList[index] = data.getSerializableExtra("newRole") as Role
                rolesAdapter.notifyDataSetChanged()
            }
            SCRIPT_ADDED -> if(resultCode == 0) {
                scriptsList.add(data.getSerializableExtra("newScript") as Script)
                scriptsAdapter.notifyDataSetChanged()
            }
            SCRIPT_CHANGED -> if(requestCode == 0) {
                val index = data.getIntExtra("position", -1)
                scriptsList[index] = data.getSerializableExtra("newScript") as Script
                scriptsAdapter.notifyDataSetChanged()
            }
        }
    }
}

