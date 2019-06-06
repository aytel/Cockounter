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
import com.example.cockounter.adapters.EditPresetAdapter
import com.example.cockounter.adapters.ListElementShow
import com.example.cockounter.adapters.ListHeaderShow
import com.example.cockounter.adapters.SimpleHeader
import com.example.cockounter.adapters.library.listElementShow.listElementShow
import com.example.cockounter.adapters.parameter.listElementShow.listElementShow
import com.example.cockounter.adapters.presetscript.listElementShow.listElementShow
import com.example.cockounter.adapters.role.listElementShow.listElementShow
import com.example.cockounter.adapters.simpleheader.listHeaderShow.listHeaderShow
import com.example.cockounter.core.*
import com.example.cockounter.storage.Storage
import com.example.cockounter.storage.loadLibrary
import com.google.android.material.appbar.AppBarLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.textChangedListener

fun <T> MutableLiveData<T>.notify() {
    this.postValue(this.value)
}

private class EditPresetViewModel() : ViewModel() {
    val globalParametersList = MutableLiveData<MutableList<Parameter>>()
    val rolesList = MutableLiveData<MutableList<Role>>()
    val scriptsList = MutableLiveData<MutableList<PresetScript>>()
    val librariesList = MutableLiveData<MutableList<Library>>()
    var name = ""
    var description = ""
    var id: Int

    init {
        id = 0
        globalParametersList.value = mutableListOf()
        rolesList.value = mutableListOf()
        scriptsList.value = mutableListOf()
        librariesList.value = mutableListOf()
    }

    constructor(id: Int) : this() {
        this.id = id
        val presetInfo = doAsyncResult { Storage.getPresetInfoById(id) }.get()
        name = presetInfo.name
        description = presetInfo.description
        globalParametersList.value = presetInfo.preset.globalParameters.values.toMutableList()
        rolesList.value = presetInfo.preset.roles.values.toMutableList()
        scriptsList.value = presetInfo.preset.actionsStubs.toMutableList()
        librariesList.value = presetInfo.preset.libraries.toMutableList()
    }

    fun addGlobalParameter(parameter: Parameter) {
        globalParametersList.value?.add(parameter)
        globalParametersList.notify()
    }

    fun addRole(role: Role) {
        rolesList.value!!.add(role)
    }

    fun addScript(script: PresetScript) {
        scriptsList.value!!.add(script)
    }

    fun addLibrary(library: Library) {
        librariesList.value!!.add(library)
    }

    fun changeGlobalParameter(index: Int, parameter: Parameter) {
        globalParametersList.value!![index] = parameter
    }

    fun changeRole(index: Int, role: Role) {
        rolesList.value!![index] = role
    }

    fun changeScript(index: Int, script: PresetScript) {
        scriptsList.value!![index] = script
    }

    fun changeLibrary(index: Int, library: Library) {
        librariesList.value!![index] = library
    }

    fun deleteGlobalParameter(index: Int) {
        globalParametersList.value!!.removeAt(index)
    }

    fun deleteRole(index: Int) {
        rolesList.value!!.removeAt(index)
    }

    fun deleteScript(index: Int) {
        scriptsList.value!!.removeAt(index)
    }

    fun deleteLibrary(index: Int) {
        librariesList.value!!.removeAt(index)
    }

    fun save(name: String, description: String) {
        val presetInfo = PresetInfo(
            id = id,
            name = name,
            description = description,
            preset = Preset(
                globalParameters = globalParametersList.value!!.map { Pair(it.name, it) }.toMap(),
                roles = rolesList.value!!.map { Pair(it.name, it) }.toMap(),
                libraries = librariesList.value!!.toList(),
                actionsStubs = scriptsList.value!!.toList()
            )
        )
        doAsync {
            Storage.insertPreset(presetInfo)
        }
    }
}


class EditPresetActivity : AppCompatActivity() {
    private lateinit var viewModel: EditPresetViewModel
    private lateinit var adapter: EditPresetAdapter<HeaderViewer, ElementViewer>

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
        viewModel.globalParametersList.observe(this, Observer { list -> Log.i("s", "ss"); adapter.update(0, list.map{ElementViewer.Parameter(it)}) })
        viewModel.rolesList.observe(this, Observer { list -> adapter.update(1, list.map { ElementViewer.Role(it) }) })
        viewModel.scriptsList.observe(this, Observer { list -> adapter.update(2, list.map { ElementViewer.PresetScript(it) }) })
        viewModel.librariesList.observe(this, Observer { list -> adapter.update(3, list.map { ElementViewer.Library(it) }) })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) {
            return
        }
        when (requestCode) {
            CODE_SHARED_PARAMETER_ADDED -> if (resultCode == Activity.RESULT_OK) {
                viewModel.addGlobalParameter(data.getSerializableExtra(EditParameterActivity.RETURN_PARAMETER) as Parameter)
            }
            CODE_ROLE_ADDED -> if (resultCode == Activity.RESULT_OK) {
                viewModel.addRole(data.getSerializableExtra(EditRoleActivity.RETURN_ROLE) as Role)
            }
            CODE_SHARED_PARAMETER_CHANGED -> if (resultCode == Activity.RESULT_OK) {
                val index = data.getIntExtra(EditParameterActivity.RETURN_POSITION, -1)
                viewModel.changeGlobalParameter(index, data.getSerializableExtra(EditParameterActivity.RETURN_PARAMETER) as Parameter)
            }
            CODE_ROLE_CHANGED -> if (resultCode == Activity.RESULT_OK) {
                val index = data.getIntExtra(EditRoleActivity.RETURN_POSITION, -1)
                viewModel.changeRole(index, data.getSerializableExtra(EditRoleActivity.RETURN_ROLE) as Role)
            }
            CODE_SCRIPT_ADDED -> if (resultCode == Activity.RESULT_OK) {
                viewModel.addScript(data.getSerializableExtra(EditPresetScriptActivity.RETURN_PRESET_SCRIPT) as PresetScript)
            }
            CODE_SCRIPT_CHANGED -> if (resultCode == Activity.RESULT_OK) {
                val index = data.getIntExtra(EditPresetScriptActivity.RETURN_POSITION, -1)
                viewModel.changeScript(index, data.getSerializableExtra(EditPresetScriptActivity.RETURN_PRESET_SCRIPT) as PresetScript)
            }
            CODE_LOAD_LIBRARY -> if (resultCode == Activity.RESULT_OK) {
                val uri = data.data!!
                loadLibrary(this, uri).fold({
                    alert(it.message!!).show()
                }, {
                    viewModel.addLibrary(Library("lib", it))
                })
            }
            CODE_ADD_LIBRARY -> if (resultCode == Activity.RESULT_OK) {
                val name = data.getStringExtra(EditLibraryActivity.RETURN_NAME)
                val source = data.getStringExtra(EditLibraryActivity.RETURN_SOURCE)
                viewModel.addLibrary(Library(name, source))
            }
        }
    }

    fun editParameter(index: Int) {
        startActivityForResult(
            intentFor<EditParameterActivity>(
                EditParameterActivity.ARG_PARAMETER to viewModel.globalParametersList.value!![index],
                EditParameterActivity.ARG_POSITION to index
            ), CODE_SHARED_PARAMETER_CHANGED
        )
    }

    fun deleteParameter(index: Int) {
        viewModel.deleteGlobalParameter(index)
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
                EditRoleActivity.ARG_ROLE to viewModel.rolesList.value!![index],
                EditRoleActivity.ARG_POSITION to index
            ), CODE_ROLE_CHANGED
        )
    }

    fun deleteRole(index: Int) {
        viewModel.deleteRole(index)
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
                EditPresetScriptActivity.ARG_PRESET_SCRIPT to viewModel.scriptsList.value!![index],
                EditPresetScriptActivity.ARG_POSITION to index
            ), CODE_SCRIPT_CHANGED
        )
    }

    fun deleteScript(index: Int) {
        viewModel.deleteScript(index)
    }

    fun addScript() {
        startActivityForResult(
            intentFor<EditPresetScriptActivity>(EditPresetScriptActivity.ARG_PRESET_SCRIPT to null),
            CODE_SCRIPT_ADDED
        )
    }

    fun save(name: String, description: String) {
        viewModel.save(name, description)
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
}

private class EditPresetUI(
    val name: String,
    val description: String,
    /*
    val globalParametersAdapter: ParameterAdapter,
    val rolesAdapter: ArrayAdapter<Role>,
    val scriptsAdapter: PresetScriptAdapter,
    val librariesAdapter: ArrayAdapter<Library>
    */
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
                            //setIcon(R.drawable.ic_done_black_24dp)
                            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                            setOnMenuItemClickListener {
                                owner.save(presetName.text.toString(), presetDescription.text.toString())
                                true
                            }
                        }
                    }
                }.lparams(width = matchParent, height = wrapContent) {
                    scrollFlags = 0
                }
            /*
            scrollView {
                verticalLayout {
                    presetName = editText(presetInfo?.name ?: "") {
                        hint = "Name"
                    }
                    presetDescription = editText(presetInfo?.description ?: "") {
                        hint = "Description"
                    }
                    textView("Global counters")
                    listView {
                        adapter = globalParametersAdapter
                        onItemLongClick { _, _, index, _ ->
                            selector(null, listOf("Edit", "Delete")) { _, i ->
                                when (i) {
                                    0 -> owner.editParameter(index)
                                    1 -> owner.deleteParameter(index)
                                }
                            }
                        }
                    }
                    listView {
                        adapter = rolesAdapter
                        onItemLongClick { _, _, index, _ ->
                            selector(null, listOf("Edit", "Delete")) { _, i ->
                                when (i) {
                                    0 -> owner.editRole(index)
                                    1 -> owner.deleteRole(index)
                                }
                            }
                        }
                    }
                    listView {
                        adapter = scriptsAdapter
                        onItemLongClick { _, _, index, _ ->
                            selector(null, listOf("Edit", "Delete")) { _, i ->
                                when (i) {
                                    0 -> owner.editScript(index)
                                    1 -> owner.deleteScript(index)
                                }
                            }
                        }
                    }
                    listView {
                        adapter = librariesAdapter
                    }
                }
                //expandableListView {
                //
                //}
            }.lparams() {
                behavior = AppBarLayout.ScrollingViewBehavior()
            }
            */
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
                //imageResource = R.drawable.ic_add_white_24dp
            }.lparams(width = wrapContent, height = wrapContent) {
                gravity = Gravity.BOTTOM + Gravity.END
                margin = dip(16)
            }
        }
    }
}

