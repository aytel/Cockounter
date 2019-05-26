package com.example.cockounter

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import com.example.cockounter.core.PlayerDescription
import com.example.cockounter.core.Script
import com.example.cockounter.core.ScriptContext
import com.example.cockounter.core.dummyState
import com.example.cockounter.script.performScript
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class EditScriptActivity : AppCompatActivity() {

    companion object {
        const val ARG_SCRIPT = "script"
        const val ARG_POSITION = "position"
        const val RETURN_POSITION = "position"
        const val RETURN_SCRIPT = "newScript"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val script = intent.getSerializableExtra(ARG_SCRIPT) as Script?
        val contextAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ScriptContext.values())
        EditScriptUI(script, contextAdapter).setContentView(this)
    }

    fun runScript(script: String) {
        performScript(dummyState, PlayerDescription("None", "None"), script, ScriptContext.NONE, this)
    }

    fun save(scriptName: String, script: String, context: ScriptContext) {
        val result = Intent()
        result.putExtra(
            RETURN_SCRIPT,
            Script(
                scriptName,
                script,
                context
            )
        )
        result.putExtra(RETURN_POSITION, intent.getIntExtra(ARG_POSITION, -1))
        setResult(Activity.RESULT_OK, result)
        finish()
    }
}

class EditScriptUI(val script: Script?, val contextAdapter: ArrayAdapter<ScriptContext>) : AnkoComponent<EditScriptActivity> {
    override fun createView(ui: AnkoContext<EditScriptActivity>): View = with(ui) {
         scrollView {
            verticalLayout {
                val scriptName = editText(script?.name ?: "") {
                    hint = "Name"
                }
                val spinner = spinner {
                    adapter = contextAdapter
                }
                val scriptSource = editText(script?.script ?: "") {
                    hint = "Script"
                }
                button("Run script") {
                    onClick {
                        owner.runScript(scriptSource.text.toString())
                    }
                }
                button("Save") {
                    onClick {
                        owner.save(scriptName.text.toString(), scriptSource.text.toString(), spinner.selectedItem as ScriptContext)
                    }
                }
            }
        }
    }
}
