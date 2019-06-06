package com.example.cockounter

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.lifecycle.*
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.example.cockounter.adapters.PlayersAdapter
import com.example.cockounter.core.PlayerDescription
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import kotlin.properties.Delegates

private class StartSinglePlayerGameViewModel(roles: Array<String>) : ViewModel() {
    val players = EditableList<PlayerDescription>()
    val roles: List<String> = roles.toList()

    fun addNewPlayer(name: String, role: String): Option<String> {
        val usedNames = players.data.map { it.name }
        return when (name) {
            "" -> Some("Empty name")
            in usedNames -> Some("Name is already used")
            else -> {
                players.add(PlayerDescription(name, role))
                None
            }
        }
    }
}

open class StartSinglePlayerGameActivity : AppCompatActivity() {
    companion object {
        const val ARG_ROLES = "ARG_ROLES"
        const val RETURN_NAMES = "RETURN_NAMES"
        const val RETURN_ROLES = "RETURN_ROLES"
    }
    private lateinit var viewModel: StartSinglePlayerGameViewModel
    private val playersAdapter = PlayersAdapter(mutableListOf())
    private val roleAdapter by lazy { ArrayAdapter(this, android.R.layout.simple_list_item_1, viewModel.roles) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val roles = intent.getStringArrayExtra(ARG_ROLES)
                return StartSinglePlayerGameViewModel(roles) as T
            }

        }).get(StartSinglePlayerGameViewModel::class.java)
        viewModel.players.liveData.observe(this, Observer { list -> playersAdapter.update(list) })
        StartSinglePlayerGameUI(playersAdapter).setContentView(this)
    }

    fun addNewPlayer() {
        alert {
            customView {
                verticalLayout {
                    val name = editText {
                        hint = "Name"
                    }
                    val roleSpinner = spinner {
                        adapter = roleAdapter
                    }
                    yesButton {
                        when (val result = viewModel.addNewPlayer(name.text.toString(), roleSpinner.selectedItem as String)) {
                            is Some -> toast(result.t)
                        }
                    }
                    noButton { }
                }
            }
        }.show()
    }


    open fun startGame() {
        val result = Intent();
        result.putExtra(RETURN_NAMES, viewModel.players.data.map { it.name }.toTypedArray())
        result.putExtra(RETURN_ROLES, viewModel.players.data.map { it.role }.toTypedArray())
        setResult(Activity.RESULT_OK, result)
        finish()
    }
}

class StartSinglePlayerGameUI(private val playersAdapter: PlayersAdapter) : AnkoComponent<StartSinglePlayerGameActivity> {
    override fun createView(ui: AnkoContext<StartSinglePlayerGameActivity>): View = with(ui) {
        verticalLayout {
            listView {
                adapter = playersAdapter
            }
            button("Add new player") {
                onClick {
                    ui.owner.addNewPlayer()
                }
            }
            button("Start game") {
                onClick {
                    ui.owner.startGame()
                }
            }
        }
    }
}

