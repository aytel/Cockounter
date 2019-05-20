package com.example.cockounter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.room.Room
import com.example.cockounter.core.Preset
//import com.example.cockounter.storage.Storage
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        //val database = Room.databaseBuilder(this, Storage::class.java, "storage").build()
        //database.presetDao().insert(Preset("kek", emptyMap(), emptyMap(), emptyList()));
        //print(database.presetDao().getAll().size)

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
                    //toast("Work in progress")
                    startActivity(intentFor<PlayerGameScreenActivity>())
                }
            }
        }
    }
}

