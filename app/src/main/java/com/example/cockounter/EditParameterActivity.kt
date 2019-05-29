package com.example.cockounter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.cockounter.adapters.PresetScriptAdapter
import com.example.cockounter.core.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onItemLongClick

class EditParameterActivity : AppCompatActivity() {

    companion object {
        const val REQUEST = "INIT_FLAG"
        const val REQUEST_ERROR = -1
        const val REQUEST_NEW_PARAMETER = 0
        const val ARG_PARAMETER = "ARG_PARAMETER"
        const val ARG_POSITION = "ARG_POSITION"
        const val RETURN_PARAMETER = "RETURN_PARAMETER"
        const val RETURN_POSITION = "RETURN_POSITION"
        const val CODE_NEW_SCRIPT_ADDED = 0
        const val CODE_SCRIPT_CHANGED = 1
    }

    private val scriptsList: MutableList<PresetScript> = mutableListOf()
    private val scriptsAdapter: PresetScriptAdapter by lazy { PresetScriptAdapter(scriptsList)}
    //val parameterPointer by lazy { intent.getSerializableExtra(ARG_PARAMETER_POINTER) as ParameterPointer? }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val parameter = intent.getSerializableExtra(ARG_PARAMETER) as Parameter?
        //val attachedActions = intent.getSerializableExtra(ARG_ACTIONS) as List<ActionButtonModel>?
        //if (attachedActions != null) {
        //    scriptsList.addAll(attachedActions)
        //}
        EditParameterUI(parameter, scriptsAdapter).setContentView(this)
    }

    fun save(type: String, name: String, visibleName: String, defaultValue: String) {
        toParameter(type, name, visibleName, defaultValue, scriptsList).fold(
            { errorMessage ->
                alert(errorMessage).show()
            },
            { parameter ->
                val result = Intent()
                result.apply {
                    putExtra(RETURN_PARAMETER, parameter)
                    putExtra(RETURN_POSITION, intent.getIntExtra(ARG_POSITION, -1))
                }
                setResult(Activity.RESULT_OK, result)
                finish()
            }
        )
    }

    fun addScript() {
        startActivityForResult(intentFor<EditPresetScriptActivity>(), CODE_NEW_SCRIPT_ADDED)
    }

    fun deleteScript(index: Int) {
        scriptsList.removeAt(index)
        scriptsAdapter.notifyDataSetChanged()
    }

    fun changeScript(index: Int) {
        startActivityForResult(
            intentFor<EditPresetScriptActivity>(
                EditPresetScriptActivity.ARG_POSITION to index,
                EditPresetScriptActivity.ARG_PRESET_SCRIPT to scriptsList[index]
            ), CODE_SCRIPT_CHANGED
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) {
            return
        }
        when (requestCode) {
            CODE_NEW_SCRIPT_ADDED -> if (resultCode == Activity.RESULT_OK) {
                val script = data.getSerializableExtra(EditPresetScriptActivity.RETURN_PRESET_SCRIPT) as PresetScript
                //TODO total function
                scriptsList.add(script)
                scriptsAdapter.notifyDataSetChanged()
            }
            CODE_SCRIPT_CHANGED -> if (resultCode == Activity.RESULT_OK) {
                val script = data.getSerializableExtra(EditPresetScriptActivity.RETURN_PRESET_SCRIPT) as PresetScript
                val position = data.getIntExtra(EditPresetScriptActivity.RETURN_POSITION, -1)
                //TODO total function
                scriptsList[position] = script
                scriptsAdapter.notifyDataSetChanged()
            }
        }
    }
}

private class EditParameterUI(val parameter: Parameter?, private val scriptsAdapter: PresetScriptAdapter) :
    AnkoComponent<EditParameterActivity> {
    override fun createView(ui: AnkoContext<EditParameterActivity>): View = with(ui) {
        verticalLayout {
            val parameterVisibleName = editText(parameter?.visibleName ?: "") {
                hint = "Name"
            }
            val parameterInternalName = editText(parameter?.name ?: "") {
                hint = "Scripting name"
            }
            val typeAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, listOf("Integer", "String"))
            val typeSpinner = spinner {
                adapter = typeAdapter
            }
            val defaultValue = editText(if (parameter == null) "" else parameter.initialValueString()) {
                hint = "Initial value"
            }
            listView {
                adapter = scriptsAdapter
                onItemLongClick { _, _, index, _ ->
                    selector(null, listOf("Edit", "Delete")) { _, i ->
                        when (i) {
                            0 -> owner.changeScript(index)
                            1 -> owner.deleteScript(index)
                        }
                    }
                }
            }
            button("Add actionButton") {
                onClick {
                    owner.addScript()
                }
            }
            button("Save") {
                onClick {
                    owner.save(
                        type = typeSpinner.selectedItem.toString(),
                        name = parameterInternalName.text.toString(),
                        visibleName = parameterVisibleName.text.toString(),
                        defaultValue = defaultValue.text.toString()
                    )
                }
            }
        }
    }
}

