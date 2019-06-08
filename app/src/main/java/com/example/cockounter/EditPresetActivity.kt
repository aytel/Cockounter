package com.example.cockounter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.BaseExpandableListAdapter
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.example.cockounter.adapters.*
import com.example.cockounter.adapters.simpleheader.listHeaderShow.listHeaderShow
import com.example.cockounter.core.*
import com.example.cockounter.storage.Storage
import com.example.cockounter.storage.loadLibrary
import com.google.android.material.appbar.AppBarLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.sdk27.coroutines.onChildClick
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.textChangedListener

class EditPresetViewModel() : ViewModel() {
    val globalParameters = EditableList<Parameter>()
    val roles = EditableList<Role>()
    val scripts = EditableList<PresetScript>()
    val libraries = EditableList<Library>()
    var name = ""
    var description = ""
    var id: Int = 0

    constructor(id: Int) : this() {
        this.id = id
        val presetInfo = doAsyncResult { Storage.getPresetInfoById(id) }.get()
        name = presetInfo.name
        description = presetInfo.description
        globalParameters.addAll(presetInfo.preset.globalParameters.values)
        roles.addAll(presetInfo.preset.roles.values)
        scripts.addAll(presetInfo.preset.actionsStubs)
        libraries.addAll(presetInfo.preset.libraries)
    }

    fun save() {
        val presetInfo = PresetInfo(
            id = id,
            name = name,
            description = description,
            preset = Preset(
                globalParameters = globalParameters.data.map { Pair(it.name, it) }.toMap(),
                roles = roles.data.map { Pair(it.name, it) }.toMap(),
                libraries = libraries.data.toList(),
                actionsStubs = scripts.data.toList()
            )
        )
        doAsyncResult {
            Storage.insertPreset(presetInfo)
            true
        }.get()
    }
}


class EditPresetActivity : AppCompatActivity() {
    private lateinit var viewModel: EditPresetViewModel
    private lateinit var adapter: EditPresetAdapter<HeaderViewer, ElementViewer>
    private var selectedPosition: Int = -1

    sealed class ElementViewer {
        data class Parameter(val parameter: com.example.cockounter.core.Parameter) : ElementViewer()
        data class Role(val role: com.example.cockounter.core.Role) : ElementViewer()
        data class PresetScript(val script: com.example.cockounter.core.PresetScript) : ElementViewer()
        data class Library(val library: com.example.cockounter.core.Library) : ElementViewer()
        companion object
    }

    sealed class HeaderViewer {
        object Parameter : HeaderViewer()
        object Role : HeaderViewer()
        object PresetScript : HeaderViewer()
        object Library : HeaderViewer()
        companion object
    }

    interface HeaderListHeaderShow : ListHeaderShow<HeaderViewer> {
        override fun HeaderViewer.buildView(context: Context, isSelected: Boolean): View = when(this) {
            HeaderViewer.Parameter -> SimpleHeader.listHeaderShow().run { SimpleHeader("Global parameters").buildView(context, isSelected) }
            HeaderViewer.Role -> SimpleHeader.listHeaderShow().run { SimpleHeader("Roles").buildView(context, isSelected) }
            HeaderViewer.PresetScript -> SimpleHeader.listHeaderShow().run { SimpleHeader("Actions").buildView(context, isSelected) }
            HeaderViewer.Library -> SimpleHeader.listHeaderShow().run { SimpleHeader("Libraries").buildView(context, isSelected) }
        }
    }
    private fun HeaderViewer.Companion.listHeaderShow() = object : HeaderListHeaderShow {}

    interface ViewerListElementShow : ListElementShow<ElementViewer> {
        override fun ElementViewer.buildView(context: Context): View = when(this) {
            is ElementViewer.Parameter -> Parameter.listElementShow().run { this@buildView.parameter.buildView(context) }
            is ElementViewer.Role -> Role.listElementShow().run { this@buildView.role.buildView(context) }
            is ElementViewer.PresetScript -> PresetScript.listElementShow().run { this@buildView.script.buildView(context) }
            is ElementViewer.Library -> Library.listElementShow().run { this@buildView.library.buildView(context) }
        }
    }
    private fun ElementViewer.Companion.listElementShow() = object : ViewerListElementShow {}

    companion object {
        const val ARG_PRESET_ID = "ARG_PRESET_ID"
        private const val CODE_SHARED_PARAMETER_ADDED = 0
        private const val CODE_ROLE_ADDED = 1
        private const val CODE_SHARED_PARAMETER_CHANGED = 2
        private const val CODE_ROLE_CHANGED = 3
        private const val CODE_SCRIPT_ADDED = 4
        private const val CODE_SCRIPT_CHANGED = 5
        private const val CODE_LOAD_LIBRARY = 6
        private const val CODE_ADD_LIBRARY = 7
        private const val CODE_LIBRARY_CHANGED = 8
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getIntExtra(ARG_PRESET_ID, 0)
        adapter = EditPresetAdapter(listOf(HeaderViewer.Parameter, HeaderViewer.Role, HeaderViewer.PresetScript, HeaderViewer.Library),
            ElementViewer.listElementShow(), HeaderViewer.listHeaderShow())
        val ui: EditPresetUI
        if (id != 0) {
            viewModel = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return EditPresetViewModel(id) as T
                }
            }).get(EditPresetViewModel::class.java)
            //EditPresetUI(presetInfo, globalParametersAdapter, rolesAdapter, scriptsAdapter, librariesAdapter).setContentView(this@EditPresetActivity)
            ui = EditPresetUI(viewModel.name, viewModel.description, adapter)
        } else {
            viewModel = ViewModelProviders.of(this).get(EditPresetViewModel::class.java)
            //EditPresetUI(null, globalParametersAdapter, rolesAdapter, scriptsAdapter, librariesAdapter).setContentView(this@EditPresetActivity)
            ui = EditPresetUI(viewModel.name, viewModel.description, adapter)
        }
        ui.setContentView(this)
        viewModel.globalParameters.liveData.observe(this, Observer { list -> Log.i("s", "ss"); adapter.update(0, list.map{ElementViewer.Parameter(it)}) })
        viewModel.roles.liveData.observe(this, Observer { list -> adapter.update(1, list.map { ElementViewer.Role(it) }) })
        viewModel.scripts.liveData.observe(this, Observer { list -> adapter.update(2, list.map { ElementViewer.PresetScript(it) }) })
        viewModel.libraries.liveData.observe(this, Observer { list -> adapter.update(3, list.map { ElementViewer.Library(it) }) })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) {
            return
        }
        when (requestCode) {
            CODE_SHARED_PARAMETER_ADDED -> if (resultCode == Activity.RESULT_OK) {
                viewModel.globalParameters.add(data.getSerializableExtra(EditParameterActivity.RETURN_PARAMETER) as Parameter)
            }
            CODE_ROLE_ADDED -> if (resultCode == Activity.RESULT_OK) {
                viewModel.roles.add(data.getSerializableExtra(EditRoleActivity.RETURN_ROLE) as Role)
            }
            CODE_SHARED_PARAMETER_CHANGED -> if (resultCode == Activity.RESULT_OK) {
                val index = data.getIntExtra(EditParameterActivity.RETURN_POSITION, -1)
                viewModel.globalParameters[index] = data.getSerializableExtra(EditParameterActivity.RETURN_PARAMETER) as Parameter
            }
            CODE_ROLE_CHANGED -> if (resultCode == Activity.RESULT_OK) {
                val index = data.getIntExtra(EditRoleActivity.RETURN_POSITION, -1)
                viewModel.roles[index] = data.getSerializableExtra(EditRoleActivity.RETURN_ROLE) as Role
            }
            CODE_SCRIPT_ADDED -> if (resultCode == Activity.RESULT_OK) {
                viewModel.scripts.add(data.getSerializableExtra(EditPresetScriptActivity.RETURN_PRESET_SCRIPT) as PresetScript)
            }
            CODE_SCRIPT_CHANGED -> if (resultCode == Activity.RESULT_OK) {
                val index = data.getIntExtra(EditPresetScriptActivity.RETURN_POSITION, -1)
                viewModel.scripts[index] = data.getSerializableExtra(EditPresetScriptActivity.RETURN_PRESET_SCRIPT) as PresetScript
            }
            CODE_LOAD_LIBRARY -> if (resultCode == Activity.RESULT_OK) {
                val uri = data.data!!
                loadLibrary(this, uri).fold({
                    alert(it.message!!).show()
                }, {
                    viewModel.libraries.add(Library("lib", it))
                })
            }
            CODE_ADD_LIBRARY -> if (resultCode == Activity.RESULT_OK) {
                val name = data.getStringExtra(EditLibraryActivity.RETURN_NAME)
                val source = data.getStringExtra(EditLibraryActivity.RETURN_SOURCE)
                viewModel.libraries.add(Library(name, source))
            }
            CODE_LIBRARY_CHANGED -> if(resultCode == Activity.RESULT_OK) {
                val name = data.getStringExtra(EditLibraryActivity.RETURN_NAME)
                val source = data.getStringExtra(EditLibraryActivity.RETURN_SOURCE)
                viewModel.libraries[selectedPosition] = Library(name, source)
            }
        }
    }

    fun editParameter(index: Int) {
        startActivityForResult(
            intentFor<EditParameterActivity>(
                EditParameterActivity.ARG_PARAMETER to viewModel.globalParameters[index],
                EditParameterActivity.ARG_POSITION to index
            ), CODE_SHARED_PARAMETER_CHANGED
        )
    }

    fun deleteParameter(index: Int) {
        viewModel.globalParameters.removeAt(index)
    }

    fun addParameter() {
        startActivityForResult(
            intentFor<EditParameterActivity>(
                EditParameterActivity.ARG_PARAMETER to null
            ),
            CODE_SHARED_PARAMETER_ADDED
        )
    }

    fun editRole(index: Int) {
        startActivityForResult(
            intentFor<EditRoleActivity>(
                EditRoleActivity.ARG_ROLE to viewModel.roles[index],
                EditRoleActivity.ARG_POSITION to index
            ), CODE_ROLE_CHANGED
        )
    }

    fun deleteRole(index: Int) {
        viewModel.roles.removeAt(index)
    }

    fun addRole() {
        startActivityForResult(
            intentFor<EditRoleActivity>(
                EditRoleActivity.ARG_ROLE to null
            ), CODE_ROLE_ADDED
        )
    }

    fun editScript(index: Int) {
        startActivityForResult(
            intentFor<EditPresetScriptActivity>(
                EditPresetScriptActivity.ARG_PRESET_SCRIPT to viewModel.scripts[index],
                EditPresetScriptActivity.ARG_POSITION to index
            ), CODE_SCRIPT_CHANGED
        )
    }

    fun deleteScript(index: Int) {
        viewModel.scripts.removeAt(index)
    }

    fun addScript() {
        startActivityForResult(
            intentFor<EditPresetScriptActivity>(EditPresetScriptActivity.ARG_PRESET_SCRIPT to null),
            CODE_SCRIPT_ADDED
        )
    }

    fun editLibrary(index: Int) {
        selectedPosition = index
        startActivityForResult(intentFor<EditLibraryActivity>(EditLibraryActivity.ARG_NAME to viewModel.libraries[index].name,
            EditLibraryActivity.ARG_SOURCE to viewModel.libraries[index].script), CODE_LIBRARY_CHANGED)

    }

    fun deleteLibrary(index: Int) {
        viewModel.libraries.removeAt(index)
    }

    fun save() {
        viewModel.save()
        setResult(Activity.RESULT_OK)
        finish()
    }

    fun loadLibraryFromFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        startActivityForResult(intent, CODE_LOAD_LIBRARY)
    }

    fun createLibrary() {
        startActivityForResult(intentFor<EditLibraryActivity>(), CODE_ADD_LIBRARY)
    }

    fun updateName(name: String) {
        viewModel.name = name
    }

    fun updateDescription(description: String) {
        viewModel.description = description
    }

    fun processParameter(index: Int) {
        selector(null, listOf("Edit", "Delete")) { _, i ->
            when (i) {
                0 -> editParameter(index)
                1 -> deleteParameter(index)
            }
        }
    }

    fun processRole(index: Int) {
        selector(null, listOf("Edit", "Delete")) { _, i ->
            when (i) {
                0 -> editRole(index)
                1 -> deleteRole(index)
            }
        }
    }

    fun processScript(index: Int) {
        selector(null, listOf("Edit", "Delete")) { _, i ->
            when (i) {
                0 -> editScript(index)
                1 -> deleteScript(index)
            }
        }
    }

    fun processLibrary(index: Int) {
        selector(null, listOf("Edit", "Delete")) { _, i ->
            when (i) {
                0 -> editLibrary(index)
                1 -> deleteLibrary(index)
            }
        }
    }
}

private class EditPresetUI(
    val name: String,
    val description: String,
    val expandableAdapter: BaseExpandableListAdapter
) : AnkoComponent<EditPresetActivity> {
    private lateinit var presetName: EditText
    private lateinit var presetDescription: EditText
    override fun createView(ui: AnkoContext<EditPresetActivity>): View = with(ui) {
        coordinatorLayout {
            appBarLayout {
                lparams(matchParent, wrapContent) {

                }
                toolbar {
                    //owner.setSupportActionBar(this.toolbar())
                    title = "Edit preset"
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
                presetName = editText(name) {
                    hint = "name"
                    textChangedListener {
                        onTextChanged { chars, _, _, _ ->
                            owner.updateName(chars.toString())
                        }
                    }
                }
                presetDescription = editText(description) {
                    hint = "Description"
                    textChangedListener {
                        onTextChanged { chars, _, _, _ ->
                            owner.updateDescription(chars.toString())
                        }
                    }
                }
                expandableListView {
                    setAdapter(expandableAdapter)
                    onChildClick { _, _, groupPosition, childPosition, _ ->
                        when(groupPosition) {
                            0 -> owner.processParameter(childPosition)
                            1 -> owner.processRole(childPosition)
                            2 -> owner.processScript(childPosition)
                            3 -> owner.processLibrary(childPosition)
                        }
                    }
                }
            }.lparams(width = matchParent, height = matchParent) {
                behavior = AppBarLayout.ScrollingViewBehavior()
            }
            floatingActionButton {
                onClick {
                    selector("", listOf("Counter", "Role", "Action", "Library", "Import library")) { _, i ->
                        when(i) {
                            0 -> owner.addParameter()
                            1 -> owner.addRole()
                            2 -> owner.addScript()
                            3 -> owner.createLibrary()
                            4 -> owner.loadLibraryFromFile()
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

