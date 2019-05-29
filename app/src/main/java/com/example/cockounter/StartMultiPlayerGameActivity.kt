package com.example.cockounter

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.cockounter.adapters.PlayersAdapter
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class StartMultiPlayerGameActivity : StartSinglePlayerGameActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun startGame() {
        alert {
            customView {
                val name = editText {
                    hint = "Your name"
                }
                yesButton {
                    if(name.text.toString() in players.map { it.name }) {
                        val result = Intent();
                        result.putExtra("names", players.map { it.name }.toTypedArray())
                        result.putExtra("roles", players.map { it.role }.toTypedArray())
                        result.putExtra("position", intent.getIntExtra("position", -1))
                        setResult(Activity.RESULT_OK, result)
                        finish()
                    }
                }
            }
        }
    }
}

private class StartMultiPlayerGameUI(val playersAdapter: PlayersAdapter) :
    AnkoComponent<StartSinglePlayerGameActivity> {
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
