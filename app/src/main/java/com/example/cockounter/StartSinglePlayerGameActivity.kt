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

open class StartSinglePlayerGameActivity : AppCompatActivity() {
    companion object {
        const val ARG_PRESET_ID = "ARG_PRESET_ID"
        const val RETURN_NAMES = "RETURN_NAMES"
        const val RETURN_ROLES = "RETURN_ROLES"
        const val RETURN_PRESET_ID = "RETURN_PRESET_ID"
    }
    private val players = mutableListOf<PlayerDescription>()
    private val playersAdapter by lazy { PlayersAdapter(players) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StartSinglePlayerGameUI(playersAdapter).setContentView(this)
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
                                 playersAdapter.notifyDataSetChanged()
                             }
                         }
                     }
                     noButton { }
                 }
             }
        }.show()
    }

    open fun startGame() {
        val result = Intent();
        result.putExtra(RETURN_NAMES, players.map { it.name }.toTypedArray())
        result.putExtra(RETURN_ROLES, players.map { it.role }.toTypedArray())
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

