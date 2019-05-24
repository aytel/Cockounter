package com.example.cockounter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.example.cockounter.core.Parameter
import com.example.cockounter.core.toParameter
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class EditParameterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val parameter = intent.getSerializableExtra("parameter") as Parameter?
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
            button("Save") {
                onClick {
                    val result = Intent()
                    toParameter(
                        x = typeSpinner.selectedItem,
                        name = parameterInternalName.text.toString(),
                        visibleName = parameterVisibleName.text.toString(),
                        defaultValue = defaultValue.text.toString()
                    ).fold(
                        { alert(it).show() },
                        {
                            result.putExtra("newParameter", it)
                            result.putExtra("position", intent.getIntExtra("position", -1))
                            setResult(0, result); finish()
                        })
                }
            }
        }
    }
}

