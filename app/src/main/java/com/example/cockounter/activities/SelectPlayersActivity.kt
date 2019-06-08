package com.example.cockounter.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.lifecycle.*
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.example.cockounter.EditableList
import com.example.cockounter.R
import com.example.cockounter.adapters.PlayersAdapter
import com.example.cockounter.core.PlayerDescription
import com.google.android.material.appbar.AppBarLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.sdk27.coroutines.onClick

class SelectPlayersViewModel(roles: Array<String>) : ViewModel() {
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

class SelectPlayersActivity : AppCompatActivity() {
    companion object {
        const val ARG_ROLES = "ARG_ROLES"
        const val RETURN_NAMES = "RETURN_NAMES"
        const val RETURN_ROLES = "RETURN_ROLES"
    }
    private lateinit var viewModel: SelectPlayersViewModel
    private val playersAdapter = PlayersAdapter(mutableListOf())
    private val roleAdapter by lazy { ArrayAdapter(this, android.R.layout.simple_list_item_1, viewModel.roles) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val roles = intent.getStringArrayExtra(ARG_ROLES)
                return SelectPlayersViewModel(roles) as T
            }

        }).get(SelectPlayersViewModel::class.java)
        viewModel.players.liveData.observe(this, Observer { list -> playersAdapter.update(list) })
        if(viewModel.roles.isEmpty()) {
            toast("This preset contains no roles!")
            finish()
        }
        SelectPlayersGameUI(playersAdapter).setContentView(this)
    }

    private fun addNewPlayer() {
        alert {
            customView {
                verticalLayout {
                    val name = editText {
                        hint = "Name"
                    }
                    linearLayout {
                        lparams {
                            margin = dip(8)
                        }
                        textView("Role")
                        val roleSpinner = spinner {
                            adapter = roleAdapter
                        }
                        yesButton {
                            when (val result =
                                viewModel.addNewPlayer(name.text.toString(), roleSpinner.selectedItem as String)) {
                                is Some -> toast(result.t)
                            }
                        }
                        noButton { }
                    }
                }
            }
        }.show()
    }


    private fun startGame() {
        val result = Intent();
        result.putExtra(RETURN_NAMES, viewModel.players.data.map { it.name }.toTypedArray())
        result.putExtra(RETURN_ROLES, viewModel.players.data.map { it.role }.toTypedArray())
        setResult(Activity.RESULT_OK, result)
        finish()
    }

    private class SelectPlayersGameUI(private val playersAdapter: PlayersAdapter) : AnkoComponent<SelectPlayersActivity> {
        override fun createView(ui: AnkoContext<SelectPlayersActivity>): View = with(ui) {
            coordinatorLayout {
                appBarLayout {
                    lparams(matchParent, wrapContent) {
                    }
                    toolbar {
                        title = "Add players"
                        menu.apply {
                            add("Save").apply {
                                setIcon(R.drawable.ic_done_black_24dp)
                                setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                                setOnMenuItemClickListener {
                                    owner.startGame()
                                    true
                                }
                            }
                        }
                    }.lparams(width = matchParent, height = wrapContent) {
                        scrollFlags = 0
                    }
                }
                verticalLayout {
                    listView {
                        adapter = playersAdapter
                    }
                }.lparams(width = matchParent, height = matchParent) {
                    behavior = AppBarLayout.ScrollingViewBehavior()
                }
                floatingActionButton {
                    onClick {
                        owner.addNewPlayer()
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


