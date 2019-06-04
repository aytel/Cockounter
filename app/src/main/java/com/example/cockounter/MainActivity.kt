package com.example.cockounter

import android.Manifest
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

    fun initDatabase() {
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
        //Storage.nukePresets()
    }

    fun createSinglePlayerGame() {
        startActivityForResult(intentFor<SelectPresetActivity>(), CODE_START_SINGLE_PLAYER_GAME)
    }

    fun createMultiPlayerGame() {
        startActivityForResult(intentFor<SelectPresetActivity>(), CODE_START_MULTI_PLAYER_GAME)
    }

    fun resumeGame() {
        startActivity(intentFor<ResumeGameActivity>())
    }

    fun joinGame() {
        startActivity(intentFor<JoinGameActivity>())
    }

    fun editPresets() {
        startActivity(intentFor<SelectPresetActivity>())
    }

    private var selectedId: Int = 0

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) {
            return
        }
        when (requestCode) {
            CODE_START_SINGLE_PLAYER_GAME -> {
                selectedId = data.getIntExtra(SelectPresetActivity.RETURN_PRESET_ID, 0)
                startActivityForResult(intentFor<StartSinglePlayerGameActivity>(), CODE_RUN_SINGLE_PLAYER_GAME)
            }
            CODE_RUN_SINGLE_PLAYER_GAME -> {
                val names = data.getStringArrayExtra(StartSinglePlayerGameActivity.RETURN_NAMES)!!
                val roles = data.getStringArrayExtra(StartSinglePlayerGameActivity.RETURN_ROLES)!!
                startActivity(
                    intentFor<AdminGameScreenActivity>(
                        AdminGameScreenActivity.ARG_PRESET_ID to selectedId,
                        AdminGameScreenActivity.ARG_PLAYER_NAMES to names,
                        AdminGameScreenActivity.ARG_PLAYER_ROLES to roles
                    )
                )
            }
        }
    }
}

private class MainUI : AnkoComponent<MainActivity> {
    override fun createView(ui: AnkoContext<MainActivity>): View = with(ui) {
        verticalLayout {
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
            button("Edit presets") {
                onClick {
                    owner.editPresets()
                }
            }
        }
    }
}

