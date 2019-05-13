package com.example.cockounter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.cockounter.core.Script
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class EditScriptActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val script = intent.getSerializableExtra("script") as Script?
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
                        toast("Work in progress")
                    }
                }
                button("Save") {
                    onClick {
                        val result = Intent()
                        result.putExtra(
                            "newScript",
                            Script(
                                scriptName.text.toString(),
                                scriptSource.text.toString()
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
}
