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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val script = intent.getSerializableExtra("script") as Script?
        EditScriptUI(script).setContentView(this)
    }

    fun runScript(script: String) {
        performScriptUsingNothingWithContext(script, this)
    }

    fun save(scriptName: String, script: String) {
        val result = Intent()
        result.putExtra(
            "newScript",
            Script(
                scriptName,
                script
            )
        )
        result.putExtra("position", intent.getIntExtra("position", -1))
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
                val scriptSource = editText(script?.name ?: "") {
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
