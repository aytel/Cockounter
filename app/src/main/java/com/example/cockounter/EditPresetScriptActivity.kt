package com.example.cockounter

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import arrow.core.getOrElse
import com.example.cockounter.core.ActionButton
import com.example.cockounter.core.PresetScript
import com.example.cockounter.core.ScriptContextDescription
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class EditPresetScriptActivity : AppCompatActivity() {

    companion object {
        const val ARG_PRESET_SCRIPT = "ARG_PRESET_SCRIPT"
        const val ARG_POSITION = "ARG_POSITION"
        const val RETURN_POSITION = "RETURN_POSITION"
        const val RETURN_PRESET_SCRIPT = "RETURN_PRESET_SCRIPT"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val actionButton = intent.getSerializableExtra(ARG_PRESET_SCRIPT) as PresetScript?
        val contextAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ScriptContextDescription.values())
        EditButtonDescriptionUI(actionButton, contextAdapter).setContentView(this)
    }

    fun runScript(script: String) {
        //TODO run actionButton without building
        //performScript(dummyState, PlayerDescription("None", "None"), actionButton, ScriptContext.NONE, this)
    }

    fun save(visibleName: String, functionName: String, script: String, context: ScriptContextDescription) {
        val result = Intent()
        result.run {
            putExtra(RETURN_PRESET_SCRIPT, PresetScript(visibleName = visibleName,
                functionName = if (functionName.isNotBlank()) functionName else null,
                script = script,
                context = context))
            putExtra(RETURN_POSITION, intent.getIntExtra(ARG_POSITION, -1))
        }
        setResult(Activity.RESULT_OK, result)
        finish()
    }
}

class EditButtonDescriptionUI(val presetScript: PresetScript?, val contextAdapter: ArrayAdapter<ScriptContextDescription>) : AnkoComponent<EditPresetScriptActivity> {
    override fun createView(ui: AnkoContext<EditPresetScriptActivity>): View = with(ui) {
         scrollView {
            verticalLayout {
                val scriptName = editText(presetScript?.visibleName ?: "") {
                    hint = "Name"
                }
                val functionName = editText(presetScript?.functionName ?: "") {
                    hint = "Function name"
                }
                val spinner = spinner {
                    adapter = contextAdapter
                }
                val scriptSource = editText(presetScript?.script ?: "") {
                    hint = "script"
                }
                button("Run") {
                    onClick {
                        //TODO
                        //owner.runScript(scriptSource.text.toString())
                    }
                }
                button("Save") {
                    onClick {
                        owner.save(scriptName.text.toString(), functionName.text.toString(), scriptSource.text.toString(),
                            spinner.selectedItem as ScriptContextDescription
                        )
                    }
                }
            }
        }
    }
}
