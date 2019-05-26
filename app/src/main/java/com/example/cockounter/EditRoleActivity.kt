package com.example.cockounter

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.cockounter.adapters.ParameterAdapter
import com.example.cockounter.adapters.ScriptAdapter
import com.example.cockounter.core.Parameter
import com.example.cockounter.core.Role
import com.example.cockounter.core.Script
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onItemLongClick


class EditRoleActivity : AppCompatActivity() {
    private val sharedParameterList = mutableListOf<Parameter>()
    private val privateParameterList = mutableListOf<Parameter>()
    private val scriptsList = mutableListOf<Script>()
    private val sharedParameterAdapter by lazy { ParameterAdapter(this, 0, sharedParameterList) }
    private val privateParameterAdapter by lazy { ParameterAdapter(this, 0, privateParameterList) }
    private val scriptsAdapter by lazy { ScriptAdapter(this, 0, scriptsList) }

    companion object {
        private const val CODE_SHARED_PARAMETER_ADDED = 0
        private const val CODE_PRIVATE_PARAMETER_ADDED = 1
        private const val CODE_SHARED_PARAMETER_CHANGED = 2
        private const val CODE_PRIVATE_PARAMETER_CHANGED = 3
        private const val CODE_SCRIPT_ADDED = 4
        private const val CODE_SCRIPT_CHANGED = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        fun parameterListToMap(list: List<Parameter>) = list.map { Pair(it.name, it) }.toMap()
        super.onCreate(savedInstanceState)
        val role = intent.getSerializableExtra("role") as Role?
        if (role != null) {
            sharedParameterList.addAll(role.sharedParameters.values)
            privateParameterList.addAll(role.privateParameters.values)
            scriptsList.addAll(role.scripts)
            sharedParameterAdapter.notifyDataSetChanged()
            privateParameterAdapter.notifyDataSetChanged()
            scriptsAdapter.notifyDataSetChanged()
        }
        scrollView {
            verticalLayout {
                val roleName = editText(role?.name ?: "") {
                    hint = "Name"
                }
                textView("Shared parameters")
                listView {
                    adapter = sharedParameterAdapter
                    onItemLongClick { _, _, index, _ ->
                        selector(null, listOf("Edit", "Delete")) { _, i ->
                            when (i) {
                                0 -> startActivityForResult(
                                    intentFor<EditRoleActivity>(
                                        "parameter" to sharedParameterList[index],
                                        "position" to index
                                    ), CODE_PRIVATE_PARAMETER_CHANGED
                                )
                                1 -> {
                                    sharedParameterList.removeAt(index)
                                    sharedParameterAdapter.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                }
                button("Add shared parameter") {
                    onClick {
                        startActivityForResult(
                            intentFor<EditParameterActivity>("parameter" to null),
                            CODE_SHARED_PARAMETER_ADDED
                        )
                    }
                }
                listView {
                    adapter = privateParameterAdapter
                    onItemLongClick { _, _, index, _ ->
                        selector(null, listOf("Edit", "Delete")) { _, i ->
                            when (i) {
                                0 -> startActivityForResult(
                                    intentFor<EditRoleActivity>(
                                        "parameter" to privateParameterList[index],
                                        "position" to index
                                    ), CODE_PRIVATE_PARAMETER_CHANGED
                                )
                                1 -> {
                                    privateParameterList.removeAt(index)
                                    privateParameterAdapter.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                }
                button("Add private parameter") {
                    onClick {
                        startActivityForResult(
                            intentFor<EditParameterActivity>("parameter" to null),
                            CODE_PRIVATE_PARAMETER_ADDED
                        )
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
                button("Save") {
                    onClick {
                        val result = Intent()
                        result.putExtra(
                            "newRole",
                            Role(
                                roleName.text.toString(),
                                parameterListToMap(sharedParameterList),
                                parameterListToMap(privateParameterList),
                                scriptsList
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
                sharedParameterList.add(data.getSerializableExtra("newParameter") as Parameter)
                sharedParameterAdapter.notifyDataSetChanged()
            }
            CODE_PRIVATE_PARAMETER_ADDED -> if (resultCode == Activity.RESULT_OK) {
                privateParameterList.add(data.getSerializableExtra("newParameter") as Parameter)
                privateParameterAdapter.notifyDataSetChanged()
            }
            CODE_SHARED_PARAMETER_CHANGED -> if(resultCode == Activity.RESULT_OK) {
                val index = data.getIntExtra("position", -1)
                sharedParameterList[index] = data.getSerializableExtra("newParameter") as Parameter
                sharedParameterAdapter.notifyDataSetChanged()
            }
            CODE_PRIVATE_PARAMETER_CHANGED -> if(resultCode == Activity.RESULT_OK) {
                val index = data.getIntExtra("position", -1)
                privateParameterList[index] = data.getSerializableExtra("newParameter") as Parameter
                privateParameterAdapter.notifyDataSetChanged()
            }
            CODE_SCRIPT_ADDED -> if(resultCode == Activity.RESULT_OK) {
                scriptsList.add(data.getSerializableExtra("newScript") as Script)
                scriptsAdapter.notifyDataSetChanged()
            }
            CODE_SCRIPT_CHANGED -> if(requestCode == Activity.RESULT_OK) {
                val index = data.getIntExtra("position", -1)
                scriptsList[index] = data.getSerializableExtra("newScript") as Script
                scriptsAdapter.notifyDataSetChanged()
            }
        }
    }
}

