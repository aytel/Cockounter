package com.example.cockounter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.room.Room
import com.example.cockounter.storage.Storage
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class MainActivity : AppCompatActivity() {
    fun initDatabase() {
        Storage.database = Room.databaseBuilder(this, Storage::class.java, "storage").build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        initDatabase()
        Storage.nukePresets()
        verticalLayout {
            button("Create game") {
                onClick {
                    startActivity(intentFor<SelectPresetActivity>())
                }
            }
            button("Join game") {
                onClick {
                    toast("Work in progress")
                }
            }
            button("Edit presets") {
                onClick {
                    toast("Work in progress")
                    //startActivity(intentFor<PlayerGameScreenActivity>())
                }
            }
        }
    }
}

