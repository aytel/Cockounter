package com.example.cockounter

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.cockounter.adapters.ScriptAdapter
import com.example.cockounter.core.Parameter
import com.example.cockounter.core.Script
import com.example.cockounter.core.toParameter
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onItemLongClick

class EditParameterActivity : AppCompatActivity() {

    companion object {
        const val INIT_FLAG = "INIT_FLAG"
        const val FLAG_ERROR = -1
        const val FLAG_NEW_PARAMETER = 0
        const val ARG_PARAMETER = "parameter"
        const val ARG_POSITION = "position"
        const val RESULT_PARAMETER = "newParameter"
        const val RESULT_POSITION = "position"
        const val RESULT_OK = 0
        const val CODE_NEW_SCRIPT_ADDED = 0
        const val CODE_SCRIPT_CHANGED = 1
    }

    val scripts: MutableList<Script> = mutableListOf()
    val scriptsAdapter: ScriptAdapter by lazy { ScriptAdapter(this, 0, scripts) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val parameter = intent.getSerializableExtra(ARG_PARAMETER) as Parameter?
        if (parameter != null) {
            scripts.addAll(parameter.attachedScripts)
        }
        EditParameterUI(parameter, scriptsAdapter).setContentView(this)
    }

    fun save(type: String, name: String, visibleName: String, defaultValue: String) {
        toParameter(type, name, visibleName, defaultValue, scripts).fold(
            { errorMessage ->
                alert(errorMessage).show()
            },
            { parameter ->
                val result = Intent()
                result.apply {
                    putExtra(RESULT_PARAMETER, parameter)
                    putExtra(RESULT_POSITION, intent.getIntExtra(ARG_POSITION, -1))
                }
                setResult(RESULT_OK, result)
                finish()
            }
        )
    }

    fun addScript() {
        startActivityForResult(intentFor<EditScriptActivity>(), CODE_NEW_SCRIPT_ADDED)
    }

    fun deleteScript(index: Int) {
        scripts.removeAt(index)
        scriptsAdapter.notifyDataSetChanged()
    }

    fun changeScript(index: Int) {
        startActivityForResult(
            intentFor<EditScriptActivity>(
                EditScriptActivity.ARG_POSITION to index,
                EditScriptActivity.ARG_SCRIPT to scripts[index]
            ), CODE_SCRIPT_CHANGED
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) {
            return
        }
        when (requestCode) {
            CODE_NEW_SCRIPT_ADDED -> if (resultCode == EditScriptActivity.RESULT_OK) {
                val script = data.getSerializableExtra(EditScriptActivity.RESULT_SCRIPT) as Script
                scripts.add(script)
                scriptsAdapter.notifyDataSetChanged()
            }
            CODE_SCRIPT_CHANGED -> if (resultCode == EditScriptActivity.RESULT_OK) {
                val script = data.getSerializableExtra(EditScriptActivity.RESULT_SCRIPT) as Script
                val position = data.getIntExtra(EditScriptActivity.RESULT_POSITION, -1)
                scripts[position] = script
                scriptsAdapter.notifyDataSetChanged()
            }
        }
    }
}

class EditParameterUI(val parameter: Parameter?, val scriptsAdapter: ScriptAdapter) :
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
            button("Add script") {
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

