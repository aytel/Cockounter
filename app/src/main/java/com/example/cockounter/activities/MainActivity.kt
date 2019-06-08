package com.example.cockounter.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.room.Room
import com.example.cockounter.storage.Storage
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class MainActivity : AppCompatActivity() {
    companion object {
        private const val CODE_START_SINGLE_PLAYER_GAME = 0
        private const val CODE_START_MULTI_PLAYER_GAME = 1
        private const val CODE_RUN_SINGLE_PLAYER_GAME = 2
        private const val CODE_RUN_MULTI_PLAYER_GAME = 3
    }

    private fun initDatabase() {
        Storage.database =
            Room.databaseBuilder(this, Storage::class.java, "storage").fallbackToDestructiveMigration().build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainUI().setContentView(this)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 1)
        doAsync {
            initDatabase()
        }
    }

    private fun createSinglePlayerGame() {
        startActivityForResult(intentFor<SelectPresetActivity>(),
            CODE_START_SINGLE_PLAYER_GAME
        )
    }

    private fun createMultiPlayerGame() {
        startActivityForResult(intentFor<SelectPresetActivity>(),
            CODE_START_MULTI_PLAYER_GAME
        )
    }

    private fun resumeGame() {
        startActivity(intentFor<ResumeGameActivity>())
    }

    private fun joinGame() {
        startActivity(intentFor<JoinGameActivity>())
    }

    private var selectedId: Int = 0

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) {
            return
        }
        when (requestCode) {
            CODE_START_SINGLE_PLAYER_GAME -> if(resultCode == Activity.RESULT_OK){
                selectedId = data.getIntExtra(SelectPresetActivity.RETURN_PRESET_ID, 0)
                assert(selectedId != 0)
                doAsync {
                    val roles = Storage.getPresetInfoById(selectedId).preset.roles.keys.toTypedArray()
                    runOnUiThread {
                        startActivityForResult(intentFor<SelectPlayersActivity>(SelectPlayersActivity.ARG_ROLES to roles),
                            CODE_RUN_SINGLE_PLAYER_GAME
                        )
                    }
                }
            }
            CODE_RUN_SINGLE_PLAYER_GAME -> {
                val names = data.getStringArrayExtra(SelectPlayersActivity.RETURN_NAMES)!!
                val roles = data.getStringArrayExtra(SelectPlayersActivity.RETURN_ROLES)!!
                startActivity(
                    intentFor<SinglePlayerGameActivity>(
                        SinglePlayerGameActivity.MODE to SinglePlayerGameActivity.MODE_BUILD_NEW_STATE,
                        SinglePlayerGameActivity.ARG_PRESET_ID to selectedId,
                        SinglePlayerGameActivity.ARG_PLAYER_NAMES to names,
                        SinglePlayerGameActivity.ARG_PLAYER_ROLES to roles
                    )
                )
            }
            CODE_START_MULTI_PLAYER_GAME -> {
                selectedId = data.getIntExtra(SelectPresetActivity.RETURN_PRESET_ID, 0)
                assert(selectedId != 0)
                doAsync {
                    val roles = Storage.getPresetInfoById(selectedId).preset.roles.keys.toTypedArray()
                    runOnUiThread {
                        startActivityForResult(intentFor<SelectPlayersActivity>(SelectPlayersActivity.ARG_ROLES to roles),
                            CODE_RUN_MULTI_PLAYER_GAME
                        )
                    }
                }
            }
            CODE_RUN_MULTI_PLAYER_GAME -> {
                val names = data.getStringArrayExtra(SelectPlayersActivity.RETURN_NAMES)!!
                val roles = data.getStringArrayExtra(SelectPlayersActivity.RETURN_ROLES)!!
                startActivity(
                    intentFor<MultiPlayerGameActivity>(
                        MultiPlayerGameActivity.MODE to MultiPlayerGameActivity.MODE_CREATE_GAME,
                        MultiPlayerGameActivity.ARG_PRESET_ID to selectedId,
                        MultiPlayerGameActivity.ARG_PLAYER_NAMES to names,
                        MultiPlayerGameActivity.ARG_PLAYER_ROLES to roles
                    )
                )
            }
        }
    }

    private class MainUI : AnkoComponent<MainActivity> {
        override fun createView(ui: AnkoContext<MainActivity>): View = with(ui) {
            verticalLayout {
                lparams(matchParent, wrapContent) {
                    margin = dip(10)
                }
                button("Create game") {
                    onClick {
                        owner.createSinglePlayerGame()
                    }
                }
                button("Create multiplayer game") {
                    onClick {
                        owner.createMultiPlayerGame()
                    }
                }
                button("Resume game") {
                    onClick {
                        owner.resumeGame()
                    }
                }
                button("Join game") {
                    onClick {
                        owner.joinGame()
                    }
                }
            }
        }
    }
}


