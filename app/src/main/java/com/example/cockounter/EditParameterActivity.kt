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
            val parameterName = editText(parameter?.name ?: " ") {}
            val typeAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, listOf("Integer", "String"))
            val typeSpinner = spinner {
                adapter = typeAdapter
            }
            val defaultValue = editText()
            button("Save") {
                onClick {
                    val result = Intent()
                    toParameter(
                        typeSpinner.selectedItem,
                        parameterName.text.toString(),
                        defaultValue.text.toString()
                    ).fold(
                        { alert(it.message ?: "Failure").show() },
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

