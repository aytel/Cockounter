package com.example.cockounter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.cockounter.core.Script
import com.example.cockounter.script.performScriptUsingNothingWithContext
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class EditScriptActivity : AppCompatActivity() {

    companion object {
        const val ARG_SCRIPT = "script"
        const val ARG_POSITION = "position"
        const val RESULT_POSITION = "position"
        const val RESULT_SCRIPT = "newScript"
        const val RESULT_OK = 0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val script = intent.getSerializableExtra(ARG_SCRIPT) as Script?
        EditScriptUI(script).setContentView(this)
    }

    fun runScript(script: String) {
        performScriptUsingNothingWithContext(script, this)
    }

    fun save(scriptName: String, script: String) {
        val result = Intent()
        result.putExtra(
            RESULT_SCRIPT,
            Script(
                scriptName,
                script
            )
        )
        result.putExtra(RESULT_POSITION, intent.getIntExtra(ARG_POSITION, -1))
        setResult(0, result)
        finish()
    }
}

class EditScriptUI(val script: Script?) : AnkoComponent<EditScriptActivity> {
    override fun createView(ui: AnkoContext<EditScriptActivity>): View = with(ui) {
         scrollView {
            verticalLayout {
                val scriptName = editText(script?.name ?: "") {
                    hint = "Name"
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
                        owner.save(scriptName.text.toString(), scriptSource.text.toString())
                    }
                }
            }
        }
    }

}
