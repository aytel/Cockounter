package com.example.cockounter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.room.Room
import com.example.cockounter.storage.Storage
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class MainActivity : AppCompatActivity() {
    fun initDatabase() {
        Storage.database = Room.databaseBuilder(this, Storage::class.java, "storage").fallbackToDestructiveMigration().build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainUI().setContentView(this)

        initDatabase()
        //Storage.nukePresets()
    }

    fun createGame() {
        startActivity(intentFor<SelectPresetActivity>())
    }

    fun resumeGame() {
        startActivity(intentFor<ResumeGameActivity>())
    }

    fun joinGame() {
        toast("Work in progress")
    }

    fun editPresets() {
        toast("Work in progress")
        //startActivity(intentFor<PlayerGameScreenActivity>())
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

