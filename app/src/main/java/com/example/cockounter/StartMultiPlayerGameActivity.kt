package com.example.cockounter

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import com.example.cockounter.adapters.PlayersAdapter
import com.example.cockounter.core.PlayerDescription
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class StartMultiPlayerGameActivity : AppCompatActivity() {
    private val players = mutableListOf<PlayerDescription>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StartMultiPlayerGameUI(PlayersAdapter(players)).setContentView(this)
    }

    fun startGame() {
        alert {
            customView {
                val name = editText {
                    hint = "Your name"
                }
                yesButton {
                    if(name.text.toString() in players.map { it.name }) {
                        val result = Intent()
                        result.putExtra("names", players.map { it.name }.toTypedArray())
                        result.putExtra("roles", players.map { it.role }.toTypedArray())
                        result.putExtra("position", intent.getIntExtra("position", -1))
                        setResult(Activity.RESULT_OK, result)
                        finish()
                    }
                }
            }
        }.show()
    }

    fun addNewPlayer() {
        val roles = intent.getStringArrayExtra("roles")!!
        val usedNames = players.map { it.name }
        val roleAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, roles)
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
                        when (name.text.toString()) {
                            "" -> toast("Enter name")
                            in usedNames -> toast("This name is already taken")
                            else -> {
                                players.add(
                                    PlayerDescription(
                                        name.text.toString(),
                                        roleSpinner.selectedItem.toString()
                                    )
                                )
                                roleAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                    noButton { }
                }
            }
        }.show()
    }
}

private class StartMultiPlayerGameUI(val playersAdapter: PlayersAdapter) :
    AnkoComponent<StartMultiPlayerGameActivity> {
    override fun createView(ui: AnkoContext<StartMultiPlayerGameActivity>): View = with(ui) {
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
