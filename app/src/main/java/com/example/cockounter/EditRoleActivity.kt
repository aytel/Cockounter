package com.example.cockounter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.cockounter.adapters.ParameterAdapter
import com.example.cockounter.adapters.PresetActionButtonAdapter
import com.example.cockounter.adapters.PresetScriptAdapter
import com.example.cockounter.core.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onItemLongClick
import org.jetbrains.anko.sdk27.coroutines.textChangedListener
import java.io.Serializable

//TODO FIX EVERYTHING

class EditRoleActivity : AppCompatActivity() {
    private val sharedParameterList = mutableListOf<Parameter>()
    private val privateParameterList = mutableListOf<Parameter>()
    private val scriptsList = mutableListOf<PresetScript>()
    private val sharedParameterAdapter by lazy { ParameterAdapter(this, 0, sharedParameterList) }
    private val privateParameterAdapter by lazy { ParameterAdapter(this, 0, privateParameterList) }
    private val scriptsAdapter by lazy { PresetScriptAdapter(scriptsList) }
    private val actionButtons by lazy { (intent.getSerializableExtra(ARG_ACTIONS) as List<ActionButtonModel>).toMutableList() }

    companion object {
        const val ARG_ROLE = "ARG_ROLE"
        const val ARG_ACTIONS = "ARG_ACTIONS"
        const val ARG_POSITION = "ARG_POSITION"
        const val RETURN_POSITION = "RETURN_POSITION"
        const val RETURN_ROLE = "RETURN_ROLE"
        const val RETURN_ACTIONS = "RETURN_ACTIONS"
        private const val CODE_SHARED_PARAMETER_ADDED = 0
        private const val CODE_PRIVATE_PARAMETER_ADDED = 1
        private const val CODE_SHARED_PARAMETER_CHANGED = 2
        private const val CODE_PRIVATE_PARAMETER_CHANGED = 3
        private const val CODE_SCRIPT_ADDED = 4
        private const val CODE_SCRIPT_CHANGED = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        fun parameterListToMap(list: List<Parameter>) = list.map { Pair(it.name, it) }.toMap()
        super.onCreate(savedInstanceState)
        val role = intent.getSerializableExtra(ARG_ROLE) as Role?
        if (role != null) {
            sharedParameterList.addAll(role.sharedParameters.values)
            privateParameterList.addAll(role.privateParameters.values)
            scriptsList.addAll(actionButtons.flatMap { when(it) {
                is ActionButtonModel.Role -> if(it.rolePointer.role == role.name) listOf(it.script) else listOf()
                else -> listOf()
            } })
            actionButtons.removeAll {  when(it) {
                is ActionButtonModel.Role -> it.rolePointer.role == role.name
                else -> false
            }}
            sharedParameterAdapter.notifyDataSetChanged()
            privateParameterAdapter.notifyDataSetChanged()
            scriptsAdapter.notifyDataSetChanged()
        }
        EditRoleUI(role, sharedParameterAdapter, privateParameterAdapter, scriptsAdapter).setContentView(this)
    }

    fun editSharedParameter(index: Int) {
        startActivityForResult(
            intentFor<EditParameterActivity>(
                EditParameterActivity.REQUEST to EditParameterActivity.REQUEST_NEW_PARAMETER,
                EditParameterActivity.ARG_PARAMETER to sharedParameterList[index],
                EditParameterActivity.ARG_ACTIONS to actionButtons.toList(),
                EditParameterActivity.ARG_POSITION to index
            ), CODE_SHARED_PARAMETER_CHANGED
        )
    }

    fun deleteSharedParameter(index: Int) {
        sharedParameterList.removeAt(index)
        sharedParameterAdapter.notifyDataSetChanged()
    }

    fun editPrivateParameter(index: Int) {
        startActivityForResult(
            intentFor<EditParameterActivity>(
                EditParameterActivity.REQUEST to EditParameterActivity.REQUEST_NEW_PARAMETER,
                EditParameterActivity.ARG_PARAMETER to privateParameterList[index],
                EditParameterActivity.ARG_ACTIONS to actionButtons.toList(),
                EditParameterActivity.ARG_POSITION to index
            ), CODE_PRIVATE_PARAMETER_CHANGED
        )
    }

    fun deletePrivateParameter(index: Int) {
        privateParameterList.removeAt(index)
        privateParameterAdapter.notifyDataSetChanged()
    }

    fun addSharedParameter() {
        startActivityForResult(
            intentFor<EditParameterActivity>(
                EditParameterActivity.REQUEST to EditParameterActivity.REQUEST_NEW_PARAMETER,
                EditParameterActivity.ARG_ACTIONS to actionButtons.toList()
            ),
            CODE_SHARED_PARAMETER_ADDED
        )
    }

    fun addPrivateParameter() {
        startActivityForResult(
            intentFor<EditParameterActivity>(
                EditParameterActivity.REQUEST to EditParameterActivity.REQUEST_NEW_PARAMETER,
                EditParameterActivity.ARG_ACTIONS to actionButtons.toList()
            ),
            CODE_PRIVATE_PARAMETER_ADDED
        )
    }

    fun editAction(index: Int) {
        startActivityForResult(
            intentFor<EditButtonDescriptionActivity>(
                EditButtonDescriptionActivity.ARG_ACTION_BUTTON to scriptsList[index],
                EditButtonDescriptionActivity.ARG_POSITION to index
            ), CODE_SCRIPT_CHANGED
        )
    }

    fun addAction() {
        startActivityForResult(
            intentFor<EditButtonDescriptionActivity>(EditButtonDescriptionActivity.ARG_ACTION_BUTTON to null),
            CODE_SCRIPT_ADDED
        )
    }

    fun deleteAction(index: Int) {
        scriptsList.removeAt(index)
        scriptsAdapter.notifyDataSetChanged()
    }

    fun save(roleName: String) {
        val result = Intent()
        result.run {
            putExtra(
                RETURN_ROLE,
                Role(
                    name = roleName,
                    sharedParameters = sharedParameterList.map { it.name }.zip(sharedParameterList).toMap(),
                    privateParameters = privateParameterList.map { it.name }.zip(privateParameterList).toMap()
                    )
            )
            //TODO add new scripts
            putExtra(RETURN_ACTIONS, scriptsList.toList() as Serializable)
            putExtra(RETURN_POSITION, intent.getIntExtra(ARG_POSITION, -1))
        }
        setResult(Activity.RESULT_OK, result)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) {
            return
        }
        when (requestCode) {
            CODE_SHARED_PARAMETER_ADDED -> if (resultCode == Activity.RESULT_OK) {
                sharedParameterList.add(data.getSerializableExtra(EditParameterActivity.RETURN_PARAMETER) as Parameter)
                actionButtons.clear()
                actionButtons.addAll(data.getSerializableExtra(EditParameterActivity.RETURN_ATTACHED_ACTIONS) as List<ActionButtonModel>)
                scriptsAdapter.notifyDataSetChanged()
                sharedParameterAdapter.notifyDataSetChanged()
            }
            CODE_PRIVATE_PARAMETER_ADDED -> if (resultCode == Activity.RESULT_OK) {
                privateParameterList.add(data.getSerializableExtra(EditParameterActivity.RETURN_PARAMETER) as Parameter)
                actionButtons.clear()
                actionButtons.addAll(data.getSerializableExtra(EditParameterActivity.RETURN_ATTACHED_ACTIONS) as List<ActionButtonModel>)
                scriptsAdapter.notifyDataSetChanged()
                privateParameterAdapter.notifyDataSetChanged()
            }
            CODE_SHARED_PARAMETER_CHANGED -> if (resultCode == Activity.RESULT_OK) {
                val index = data.getIntExtra(EditParameterActivity.RETURN_POSITION, -1)
                sharedParameterList[index] = data.getSerializableExtra(EditParameterActivity.RETURN_PARAMETER) as Parameter
                sharedParameterAdapter.notifyDataSetChanged()
                actionButtons.clear()
                actionButtons.addAll(data.getSerializableExtra(EditParameterActivity.RETURN_ATTACHED_ACTIONS) as List<ActionButtonModel>)
                scriptsAdapter.notifyDataSetChanged()
            }
            CODE_PRIVATE_PARAMETER_CHANGED -> if (resultCode == Activity.RESULT_OK) {
                val index = data.getIntExtra(EditParameterActivity.RETURN_POSITION, -1)
                privateParameterList[index] = data.getSerializableExtra(EditParameterActivity.RETURN_PARAMETER) as Parameter
                privateParameterAdapter.notifyDataSetChanged()
                actionButtons.clear()
                actionButtons.addAll(data.getSerializableExtra(EditParameterActivity.RETURN_ATTACHED_ACTIONS) as List<ActionButtonModel>)
                scriptsAdapter.notifyDataSetChanged()
            }
            CODE_SCRIPT_ADDED -> if (resultCode == Activity.RESULT_OK) {
                val presetScript = data.getSerializableExtra(EditButtonDescriptionActivity.RETURN_PRESET_SCRIPT) as PresetScript
                scriptsList.add(presetScript)
                scriptsAdapter.notifyDataSetChanged()
            }
            CODE_SCRIPT_CHANGED -> if (requestCode == Activity.RESULT_OK) {
                val index = data.getIntExtra(EditButtonDescriptionActivity.RETURN_POSITION, -1)
                val presetScript = data.getSerializableExtra(EditButtonDescriptionActivity.RETURN_PRESET_SCRIPT) as PresetScript
                scriptsList[index] = presetScript
                scriptsAdapter.notifyDataSetChanged()
            }
        }
    }
}

private class EditRoleUI(
    val role: Role?,
    val sharedParameterAdapter: ParameterAdapter,
    val privateParameterAdapter: ParameterAdapter,
    val scriptsAdapter: PresetScriptAdapter
) : AnkoComponent<EditRoleActivity> {
    override fun createView(ui: AnkoContext<EditRoleActivity>): View = with(ui) {
        scrollView {
            verticalLayout {
                val roleName = editText(role?.name ?: "") {
                    hint = "Name"
                }
                textView("Shared parameters")
                listView {
                    adapter = sharedParameterAdapter
                    onItemLongClick { _, _, index, _ ->
                        selector(null, listOf("Edit", "Delete")) { _, i ->
                            when (i) {
                                0 -> owner.editSharedParameter(index)
                                1 -> owner.deleteSharedParameter(index)
                            }
                        }
                    }
                }
                button("Add shared parameter") {
                    onClick {
                        owner.addSharedParameter()
                    }
                }
                listView {
                    adapter = privateParameterAdapter
                    onItemLongClick { _, _, index, _ ->
                        selector(null, listOf("Edit", "Delete")) { _, i ->
                            when (i) {
                                0 -> owner.editPrivateParameter(index)
                                1 -> owner.deletePrivateParameter(index)
                            }
                        }
                    }
                }
                button("Add private parameter") {
                    onClick {
                        owner.addPrivateParameter()
                    }
                }
                listView {
                    adapter = scriptsAdapter
                    onItemLongClick { _, _, index, _ ->
                        selector(null, listOf("Edit", "Delete")) { _, i ->
                            when (i) {
                                0 -> owner.editAction(index)
                                1 -> owner.deleteAction(index)
                            }
                        }
                    }
                }
                button("Add new action") {
                    onClick {
                        owner.addAction()
                    }
                }
                button("Save") {
                    onClick {
                        owner.save(roleName.text.toString())
                    }
                }
            }
        }
    }

}

