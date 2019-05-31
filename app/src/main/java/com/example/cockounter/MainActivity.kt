package com.example.cockounter

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.room.Room
import com.example.cockounter.storage.Storage
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class MainActivity : AppCompatActivity() {
    fun initDatabase() {
        Storage.database =
            Room.databaseBuilder(this, Storage::class.java, "storage").fallbackToDestructiveMigration().build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainUI().setContentView(this)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 1)

        initDatabase()
        //Storage.nukePresets()
    }

    fun createGame() {
        startActivity(intentFor<SelectPresetActivity>(SelectPresetActivity.REQUEST to SelectPresetActivity.REQUEST_SINGLE_PLAYER_GAME))
    }

    fun createMultiplayerGame() {
        startActivity(intentFor<SelectPresetActivity>(SelectPresetActivity.REQUEST to SelectPresetActivity.REQUEST_MULTI_PLAYER_GAME))
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
}

private class MainUI : AnkoComponent<MainActivity> {
    override fun createView(ui: AnkoContext<MainActivity>): View = with(ui) {
        verticalLayout {
            button("Create game") {
                onClick {
                    owner.createGame()
                }
            }
            button("Create multiplayer game") {
                onClick {
                    owner.createMultiplayerGame()
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

