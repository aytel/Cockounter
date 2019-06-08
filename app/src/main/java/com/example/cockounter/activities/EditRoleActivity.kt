package com.example.cockounter.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.BaseExpandableListAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.cockounter.EditableList
import com.example.cockounter.R
import com.example.cockounter.adapters.*
import com.example.cockounter.adapters.simpleheader.listHeaderShow.listHeaderShow
import com.example.cockounter.core.Parameter
import com.example.cockounter.core.PresetScript
import com.example.cockounter.core.Role
import com.google.android.material.appbar.AppBarLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.sdk27.coroutines.onChildClick
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.textChangedListener

class EditRoleViewModel() : ViewModel() {
    val sharedParameters = EditableList<Parameter>()
    val privateParameters = EditableList<Parameter>()
    val scripts = EditableList<PresetScript>()
    var name: String

    init {
        name = ""
    }

    constructor(role: Role) : this() {
        sharedParameters.addAll(role.sharedParameters.values)
        privateParameters.addAll(role.privateParameters.values)
        scripts.addAll(role.actionsStubs)
        name = role.name
    }
}

class EditRoleActivity : AppCompatActivity() {
    companion object {
        const val ARG_ROLE = "ARG_ROLE"
        const val ARG_POSITION = "ARG_POSITION"
        const val RETURN_POSITION = "RETURN_POSITION"
        const val RETURN_ROLE = "RETURN_ROLE"
        private const val CODE_SHARED_PARAMETER_ADDED = 0
        private const val CODE_PRIVATE_PARAMETER_ADDED = 1
        private const val CODE_SHARED_PARAMETER_CHANGED = 2
        private const val CODE_PRIVATE_PARAMETER_CHANGED = 3
        private const val CODE_SCRIPT_ADDED = 4
        private const val CODE_SCRIPT_CHANGED = 5
    }

    private sealed class HeaderViewer {
        object SharedParameter : HeaderViewer()
        object PrivateParameter : HeaderViewer()
        object PresetScript : HeaderViewer()
        companion object
    }

    private sealed class ElementViewer {
        data class SharedParameter(val parameter: Parameter) : ElementViewer()
        data class PrivateParameter(val parameter: Parameter) : ElementViewer()
        data class PresetScript(val script: com.example.cockounter.core.PresetScript) : ElementViewer()
        companion object
    }

    private interface HeaderShow : ListHeaderShow<HeaderViewer> {
        override fun HeaderViewer.buildView(context: Context, isSelected: Boolean): View = when (this) {
            HeaderViewer.SharedParameter -> SimpleHeader.listHeaderShow().run {
                SimpleHeader("Shared parameters").buildView(context, isSelected)
            }
            HeaderViewer.PrivateParameter -> SimpleHeader.listHeaderShow().run {
                SimpleHeader("Private parameters").buildView(context, isSelected)
            }
            HeaderViewer.PresetScript -> SimpleHeader.listHeaderShow().run {
                SimpleHeader("Actions").buildView(context, isSelected)
            }
        }
    }

    private fun HeaderViewer.Companion.listHeaderShow() = object :
        HeaderShow {}

    private interface ElementShow : ListElementShow<ElementViewer> {
        override fun ElementViewer.buildView(context: Context): View = when (this) {
            is ElementViewer.SharedParameter -> Parameter.listElementShow().run {
                this@buildView.parameter.buildView(context)
            }
            is ElementViewer.PrivateParameter -> Parameter.listElementShow().run {
                this@buildView.parameter.buildView(context)
            }
            is ElementViewer.PresetScript -> PresetScript.listElementShow().run {
                this@buildView.script.buildView(context)
            }
        }
    }

    private fun ElementViewer.Companion.listElementShow() = object :
        ElementShow {}

    private lateinit var viewModel: EditRoleViewModel
    private lateinit var adapter: EditPresetAdapter<HeaderViewer, ElementViewer>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val role = intent.getSerializableExtra(ARG_ROLE) as Role?
        if (role != null) {
            viewModel = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return EditRoleViewModel(role) as T
                }
            }).get(EditRoleViewModel::class.java)
        } else {
            viewModel = ViewModelProviders.of(this).get(EditRoleViewModel::class.java)
        }
        adapter = EditPresetAdapter(
            listOf(
                HeaderViewer.SharedParameter,
                HeaderViewer.PrivateParameter,
                HeaderViewer.PresetScript
            ), ElementViewer.listElementShow(), HeaderViewer.listHeaderShow()
        )
        viewModel.sharedParameters.liveData.observe(this, Observer { list -> adapter.update(0, list.map {
            ElementViewer.PrivateParameter(
                it
            )
        }) })
        viewModel.privateParameters.liveData.observe(this, Observer { list -> adapter.update(1, list.map {
            ElementViewer.SharedParameter(
                it
            )
        }) })
        viewModel.scripts.liveData.observe(this, Observer { list -> adapter.update(2, list.map {
            ElementViewer.PresetScript(
                it
            )
        }) })
        EditRoleUI(viewModel.name, adapter).setContentView(this)
    }

    private fun editSharedParameter(index: Int) {
        startActivityForResult(
            intentFor<EditParameterActivity>(
                EditParameterActivity.REQUEST to EditParameterActivity.REQUEST_NEW_PARAMETER,
                EditParameterActivity.ARG_PARAMETER to viewModel.sharedParameters[index],
                EditParameterActivity.ARG_POSITION to index
            ), CODE_SHARED_PARAMETER_CHANGED
        )
    }

    private fun deleteSharedParameter(index: Int) {
        viewModel.sharedParameters.removeAt(index)
    }

    private fun editPrivateParameter(index: Int) {
        startActivityForResult(
            intentFor<EditParameterActivity>(
                EditParameterActivity.REQUEST to EditParameterActivity.REQUEST_NEW_PARAMETER,
                EditParameterActivity.ARG_PARAMETER to viewModel.privateParameters[index],
                EditParameterActivity.ARG_POSITION to index
            ), CODE_PRIVATE_PARAMETER_CHANGED
        )
    }

    private fun deletePrivateParameter(index: Int) {
        viewModel.privateParameters.removeAt(index)
    }

    private fun addSharedParameter() {
        startActivityForResult(
            intentFor<EditParameterActivity>(
                EditParameterActivity.REQUEST to EditParameterActivity.REQUEST_NEW_PARAMETER
            ),
            CODE_SHARED_PARAMETER_ADDED
        )
    }

    private fun addPrivateParameter() {
        startActivityForResult(
            intentFor<EditParameterActivity>(
                EditParameterActivity.REQUEST to EditParameterActivity.REQUEST_NEW_PARAMETER
            ),
            CODE_PRIVATE_PARAMETER_ADDED
        )
    }

    private fun editAction(index: Int) {
        startActivityForResult(
            intentFor<EditPresetScriptActivity>(
                EditPresetScriptActivity.ARG_PRESET_SCRIPT to viewModel.scripts[index],
                EditPresetScriptActivity.ARG_POSITION to index
            ), CODE_SCRIPT_CHANGED
        )
    }

    private fun addAction() {
        startActivityForResult(
            intentFor<EditPresetScriptActivity>(EditPresetScriptActivity.ARG_PRESET_SCRIPT to null),
            CODE_SCRIPT_ADDED
        )
    }

    private fun deleteAction(index: Int) {
        viewModel.scripts.removeAt(index)
    }

    private fun processSharedParameter(index: Int) {
        selector(null, listOf("Edit", "Delete")) { _, i ->
            when (i) {
                0 -> editSharedParameter(index)
                1 -> deleteSharedParameter(index)
            }
        }
    }

    private fun processPrivateParameter(index: Int) {
        selector(null, listOf("Edit", "Delete")) { _, i ->
            when (i) {
                0 -> editPrivateParameter(index)
                1 -> deletePrivateParameter(index)
            }
        }
    }

    private fun processScript(index: Int) {
        selector(null, listOf("Edit", "Delete")) { _, i ->
            when (i) {
                0 -> editAction(index)
                1 -> deleteAction(index)
            }
        }
    }

    private fun save() {
        val result = Intent()
        val sharedParameters = viewModel.sharedParameters.data
        val privateParameters = viewModel.privateParameters.data
        val scriptsList = viewModel.scripts.data
        result.run {
            putExtra(
                RETURN_ROLE,
                Role(
                    name = viewModel.name,
                    sharedParameters = sharedParameters.map { it.name }.zip(sharedParameters).toMap(),
                    privateParameters = privateParameters.map { it.name }.zip(privateParameters).toMap(),
                    actionsStubs = scriptsList.toList()
                )
            )
            //TODO add new scripts
            putExtra(
                RETURN_POSITION, intent.getIntExtra(
                    ARG_POSITION, -1))
        }
        setResult(Activity.RESULT_OK, result)
        finish()
    }

    private fun updateName(name: String) {
        viewModel.name = name
    }

    override fun onBackPressed() {
        alert {
            message = "Save changes?"
            yesButton {
                save()
            }
            noButton {
                finish()
            }
        }.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) {
            return
        }
        when (requestCode) {
            CODE_SHARED_PARAMETER_ADDED -> if (resultCode == Activity.RESULT_OK) {
                viewModel.sharedParameters.add(data.getSerializableExtra(EditParameterActivity.RETURN_PARAMETER) as Parameter)
            }
            CODE_PRIVATE_PARAMETER_ADDED -> if (resultCode == Activity.RESULT_OK) {
                viewModel.privateParameters.add(data.getSerializableExtra(EditParameterActivity.RETURN_PARAMETER) as Parameter)
            }
            CODE_SHARED_PARAMETER_CHANGED -> if (resultCode == Activity.RESULT_OK) {
                val index = data.getIntExtra(EditParameterActivity.RETURN_POSITION, -1)
                viewModel.sharedParameters[index] =
                    data.getSerializableExtra(EditParameterActivity.RETURN_PARAMETER) as Parameter
            }
            CODE_PRIVATE_PARAMETER_CHANGED -> if (resultCode == Activity.RESULT_OK) {
                val index = data.getIntExtra(EditParameterActivity.RETURN_POSITION, -1)
                viewModel.privateParameters[index] =
                    data.getSerializableExtra(EditParameterActivity.RETURN_PARAMETER) as Parameter
            }
            CODE_SCRIPT_ADDED -> if (resultCode == Activity.RESULT_OK) {
                val presetScript =
                    data.getSerializableExtra(EditPresetScriptActivity.RETURN_PRESET_SCRIPT) as PresetScript
                viewModel.scripts.add(presetScript)
            }
            CODE_SCRIPT_CHANGED -> if (requestCode == Activity.RESULT_OK) {
                val index = data.getIntExtra(EditPresetScriptActivity.RETURN_POSITION, -1)
                val presetScript =
                    data.getSerializableExtra(EditPresetScriptActivity.RETURN_PRESET_SCRIPT) as PresetScript
                viewModel.scripts[index] = presetScript
            }
        }
    }

    private class EditRoleUI(val name: String, val expandableAdapter: BaseExpandableListAdapter) :
        AnkoComponent<EditRoleActivity> {
        override fun createView(ui: AnkoContext<EditRoleActivity>): View = with(ui) {
            coordinatorLayout {
                appBarLayout {
                    lparams(matchParent, wrapContent) {

                    }
                    toolbar {
                        //owner.setSupportActionBar(this.toolbar())
                        title = "Edit role"
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
                    editText(name) {
                        hint = "name"
                        textChangedListener {
                            onTextChanged { chars, _, _, _ ->
                                owner.updateName(chars.toString())
                            }
                        }
                    }
                    expandableListView {
                        setAdapter(expandableAdapter)
                        onChildClick { _, _, groupPosition, childPosition, _ ->
                            when(groupPosition) {
                                0 -> owner.processSharedParameter(childPosition)
                                1 -> owner.processPrivateParameter(childPosition)
                                2 -> owner.processScript(childPosition)
                            }
                        }
                    }
                }.lparams(width = matchParent, height = matchParent) {
                    behavior = AppBarLayout.ScrollingViewBehavior()
                }
                floatingActionButton {
                    onClick {
                        selector("", listOf("Shared parameter", "Private parameter", "Action")) { _, i ->
                            when (i) {
                                0 -> owner.addSharedParameter()
                                1 -> owner.addPrivateParameter()
                                2 -> owner.addAction()
                            }
                        }
                    }
                    imageResource = R.drawable.ic_add_white_24dp
                }.lparams(width = wrapContent, height = wrapContent) {
                    gravity = Gravity.BOTTOM + Gravity.END
                    margin = dip(16)
                }
            }
        }
    }
}


