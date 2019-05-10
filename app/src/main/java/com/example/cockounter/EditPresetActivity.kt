package com.example.cockounter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.example.cockounter.adapters.ParameterAdapter
import com.example.cockounter.core.Parameter
import com.example.cockounter.core.Preset
import com.example.cockounter.core.Role
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onItemLongClick

private const val SHARED_PARAMETER_ADDED = 0
private const val ROLE_ADDED = 1
private const val SHARED_PARAMETER_CHANGED = 2
private const val ROLE_CHANGED = 3

class EditPresetActivity : AppCompatActivity() {
    val globalParametersList = mutableListOf<Parameter>()
    val rolesList = mutableListOf<Role>()
    val globalParametersAdapter by lazy { ParameterAdapter(this, android.R.layout.simple_list_item_1, globalParametersList) }
    val rolesAdapter by lazy { ArrayAdapter<Role>(this, android.R.layout.simple_list_item_1, rolesList) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preset = intent.getSerializableExtra("preset") as Preset?
        if (preset != null) {
            globalParametersList.addAll(preset.globalParameters.values)
            rolesList.addAll(preset.roles.values)
            globalParametersAdapter.notifyDataSetChanged()
            rolesAdapter.notifyDataSetChanged()
        }

        verticalLayout {
            val presetName = editText(preset?.name ?: "")
            textView("Global counters")
            listView {
                adapter = globalParametersAdapter
                onItemLongClick { _, _, index, _ ->
                    selector(null, listOf("Edit", "Delete")) { _, i ->
                        when (i) {
                            0 -> startActivityForResult(intentFor<EditParameterActivity>("parameter" to globalParametersList[index], "position" to index), SHARED_PARAMETER_CHANGED)
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
                            0 -> startActivityForResult(intentFor<EditRoleActivity>("role" to rolesList[index], "position" to index), ROLE_CHANGED)
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
            button("Save") {
                onClick {
                    val result = Intent()
                    result.putExtra(
                        "newPreset",
                        Preset(
                            presetName.text.toString(),
                            globalParametersList.map { Pair(it.name, it) }.toMap(),
                            rolesList.map { Pair(it.name, it) }.toMap()
                        )
                    )
                    result.putExtra("position", intent.getIntExtra("position", -1))
                    setResult(0, result)
                    finish()
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
        }
    }
}

