package com.example.cockounter

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.cockounter.core.PresetScript
import com.example.cockounter.core.ScriptContextDescription
import com.google.android.material.appbar.AppBarLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.textChangedListener
import java.text.FieldPosition

private class EditPresetScriptViewModel() : ViewModel() {
    var visibleName: String = ""
    var functionName: String = ""
    var script: String = ""
    var context: ScriptContextDescription = ScriptContextDescription.NONE
    var contextPosition = 0

    constructor(presetScript: PresetScript) : this() {
        visibleName = presetScript.visibleName
        functionName = presetScript.functionName ?: ""
        script = presetScript.script
        context = presetScript.context
    }

}

class EditPresetScriptActivity : AppCompatActivity() {

    companion object {
        const val ARG_PRESET_SCRIPT = "ARG_PRESET_SCRIPT"
        const val ARG_POSITION = "ARG_POSITION"
        const val RETURN_POSITION = "RETURN_POSITION"
        const val RETURN_PRESET_SCRIPT = "RETURN_PRESET_SCRIPT"
    }

    private lateinit var viewModel: EditPresetScriptViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val presetScript = intent.getSerializableExtra(ARG_PRESET_SCRIPT) as PresetScript?
        viewModel = if(presetScript == null) {
            ViewModelProviders.of(this).get(EditPresetScriptViewModel::class.java)
        } else {
            ViewModelProviders.of(this, object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return EditPresetScriptViewModel(presetScript) as T
                }

            }).get(EditPresetScriptViewModel::class.java)
        }
        val contextAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ScriptContextDescription.values())
        with(viewModel) {
            EditButtonDescriptionUI(visibleName, functionName, script, contextPosition)
        }.setContentView(this)
    }

    fun runScript() {
        //TODO run actionButton without building
        //performScript(dummyState, PlayerDescription("None", "None"), actionButton, ScriptContext.NONE, this)
    }

    fun save() {
        val result = Intent()
        result.run {
            putExtra(RETURN_PRESET_SCRIPT, PresetScript(
                visibleName = viewModel.visibleName,
                functionName = if (viewModel.functionName.isNotBlank()) viewModel.functionName else null,
                script = viewModel.script,
                context = viewModel.context
            ))
            putExtra(RETURN_POSITION, intent.getIntExtra(ARG_POSITION, -1))
        }
        setResult(Activity.RESULT_OK, result)
        finish()
    }

    fun updateName(name: String) {
        viewModel.visibleName = name
    }

    fun updateFunctionName(functionName: String) {
        viewModel.functionName = functionName
    }

    fun updateScript(script: String) {
        viewModel.script = script
    }

    fun updateContext(context: ScriptContextDescription, position: Int) {
        viewModel.context = context
        viewModel.contextPosition = position
    }
}

class EditButtonDescriptionUI(val name: String, val functionName: String, val script: String, val typePosition: Int) : AnkoComponent<EditPresetScriptActivity> {
    override fun createView(ui: AnkoContext<EditPresetScriptActivity>): View = with(ui) {
         coordinatorLayout {
             appBarLayout {
                 lparams(matchParent, wrapContent) {

                 }
                 toolbar {
                     //owner.setSupportActionBar(this.toolbar())
                     title = "Edit preset"
                     menu.apply {
                         add("Run").apply {
                             setIcon(R.drawable.ic_play_arrow_black_24dp)
                             setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                             setOnMenuItemClickListener {
                                 owner.runScript()
                                 true
                             }
                         }
                         add("Save").apply {
                             setIcon(R.drawable.ic_done_black_24dp)
                             setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                             setOnMenuItemClickListener {
                                 owner.save()
                                 true
                             }
                         }
                     }
                 }.lparams(width = matchParent, height = wrapContent) {
                     scrollFlags = 0
                 }
             }
             verticalLayout {
                 editText(name) {
                     hint = "Name"
                     textChangedListener {
                         onTextChanged { chars, _, _, _ ->
                             owner.updateName(text.toString())
                         }
                     }
                 }
                 editText(functionName) {
                     hint = "Function name"
                     textChangedListener {
                         onTextChanged { chars, _, _, _ ->
                             owner.updateFunctionName(text.toString())
                         }
                     }
                 }
                 val spinner = spinner {
                     adapter = ArrayAdapter(owner, android.R.layout.simple_list_item_1, ScriptContextDescription.values())
                 }
                 editText(script) {
                     hint = "script"
                     textChangedListener {
                         onTextChanged { chars, _, _, _ ->
                             owner.updateScript(text.toString())
                         }
                     }
                 }
             }.lparams(width = matchParent, height = matchParent) {
                behavior = AppBarLayout.ScrollingViewBehavior()
            }
         }
    }
}
