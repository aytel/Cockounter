package com.example.cockounter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.cockounter.adapters.ListShowAdapter
import com.example.cockounter.adapters.listElementShow
import com.example.cockounter.core.*
import com.google.android.material.appbar.AppBarLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onItemLongClick
import org.jetbrains.anko.sdk27.coroutines.onItemSelectedListener
import org.jetbrains.anko.sdk27.coroutines.textChangedListener
import java.text.FieldPosition

class EditParameterViewModel() : ViewModel() {
    var visibleName: String = ""
    var name: String = ""
    var initialValue: String = ""
    var scripts = EditableList<PresetScript>()
    var type: ParameterType = ParameterType.INTEGER
    var typePosition = 0

    constructor(parameter: Parameter) : this() {
        visibleName = parameter.visibleName
        name = parameter.name
        initialValue = parameter.initialValueString()
        scripts.addAll(parameter.actionsStubs)
    }
}

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

    private lateinit var viewModel: EditParameterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val parameter = intent.getSerializableExtra(ARG_PARAMETER) as Parameter?
        if(parameter == null) {
            viewModel = ViewModelProviders.of(this).get(EditParameterViewModel::class.java)
        } else {
            viewModel = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return EditParameterViewModel(parameter) as T
                }

            }).get(EditParameterViewModel::class.java)
        }
        val adapter = ListShowAdapter(PresetScript.listElementShow())
        viewModel.scripts.liveData.observe(this, Observer { list -> adapter.update(list) })
        EditParameterUI(viewModel.visibleName, viewModel.name, viewModel.typePosition, viewModel.initialValue, adapter).setContentView(this)
    }

    fun save() {
        toParameter(viewModel.type, viewModel.name, viewModel.visibleName, viewModel.initialValue, viewModel.scripts.data).fold(
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

    fun updateVisibleName(visibleName: String) {
        viewModel.visibleName = visibleName
    }

    fun updateName(name: String) {
        viewModel.name = name
    }

    fun updateInitialValue(initialValue: String) {
        viewModel.initialValue = initialValue
    }

    fun updateType(type: ParameterType, position: Int) {
        viewModel.type = type
        viewModel.typePosition = position
    }

    fun addScript() {
        startActivityForResult(intentFor<EditPresetScriptActivity>(), CODE_NEW_SCRIPT_ADDED)
    }

    fun deleteScript(index: Int) {
        viewModel.scripts.removeAt(index)
    }

    fun changeScript(index: Int) {
        startActivityForResult(
            intentFor<EditPresetScriptActivity>(
                EditPresetScriptActivity.ARG_POSITION to index,
                EditPresetScriptActivity.ARG_PRESET_SCRIPT to viewModel.scripts[index]
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
                viewModel.scripts.add(script)
            }
            CODE_SCRIPT_CHANGED -> if (resultCode == Activity.RESULT_OK) {
                val script = data.getSerializableExtra(EditPresetScriptActivity.RETURN_PRESET_SCRIPT) as PresetScript
                val position = data.getIntExtra(EditPresetScriptActivity.RETURN_POSITION, -1)
                viewModel.scripts[position] = script
            }
        }
    }
}

private class EditParameterUI(val visibleName: String, val name: String, val typePosition: Int, val initialValue: String, private val scriptsAdapter: ListAdapter) :
    AnkoComponent<EditParameterActivity> {
    override fun createView(ui: AnkoContext<EditParameterActivity>): View = with(ui) {
        coordinatorLayout {
            appBarLayout {
                lparams(matchParent, wrapContent) {

                }
                toolbar {
                    title = "Edit parameter"
                    menu.apply {
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
                val parameterVisibleName = editText(visibleName) {
                    hint = "Name"
                    textChangedListener {
                        onTextChanged { chars, _, _, _ ->
                            owner.updateVisibleName(chars.toString())
                        }
                    }
                }
                val parameterInternalName = editText(name) {
                    hint = "Scripting name"
                    textChangedListener {
                        onTextChanged { chars, _, _, _ ->
                            owner.updateName(chars.toString())
                        }
                    }
                }
                val typeAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, ParameterType.values())
                val typeSpinner = spinner {
                    adapter = typeAdapter
                    setSelection(typePosition)
                    onItemSelectedListener {
                        onItemSelected { _, _, i, _ ->
                            owner.updateType(selectedItem as ParameterType, i)
                        }
                    }
                }
                val defaultValue = editText(initialValue) {
                    hint = "Initial value"
                    textChangedListener {
                        onTextChanged { chars, _, _, _ ->
                            owner.updateInitialValue(chars.toString())
                        }
                    }
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
            }.lparams(width = matchParent, height = matchParent) {
                behavior = AppBarLayout.ScrollingViewBehavior()
            }
            floatingActionButton {
                onClick {
                    owner.addScript()
                }
                imageResource = R.drawable.ic_add_white_24dp
            }.lparams(width = wrapContent, height = wrapContent) {
                gravity = Gravity.BOTTOM + Gravity.END
                margin = dip(16)
            }
        }
    }
}

