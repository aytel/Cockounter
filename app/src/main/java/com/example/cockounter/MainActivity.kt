package com.example.cockounter

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.room.Room
import com.example.cockounter.network.StateUpdaterFirebaseMessagingService
import com.example.cockounter.storage.Storage
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.iid.FirebaseInstanceId
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.lang.Thread.sleep

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
            CODE_START_SINGLE_PLAYER_GAME -> if(resultCode == Activity.RESULT_OK){
                selectedId = data.getIntExtra(SelectPresetActivity.RETURN_PRESET_ID, 0)
                assert(selectedId != 0)
                doAsync {
                    val roles = Storage.getPresetInfoById(selectedId).preset.roles.keys.toTypedArray()
                    runOnUiThread {
                        startActivityForResult(intentFor<StartSinglePlayerGameActivity>(StartSinglePlayerGameActivity.ARG_ROLES to roles), CODE_RUN_SINGLE_PLAYER_GAME)
                    }
                }
            }
            CODE_RUN_SINGLE_PLAYER_GAME -> {
                val names = data.getStringArrayExtra(StartSinglePlayerGameActivity.RETURN_NAMES)!!
                val roles = data.getStringArrayExtra(StartSinglePlayerGameActivity.RETURN_ROLES)!!
                startActivity(
                    intentFor<SinglePlayerGameScreenActivity>(
                        SinglePlayerGameScreenActivity.MODE to SinglePlayerGameScreenActivity.MODE_BUILD_NEW_STATE,
                        SinglePlayerGameScreenActivity.ARG_PRESET_ID to selectedId,
                        SinglePlayerGameScreenActivity.ARG_PLAYER_NAMES to names,
                        SinglePlayerGameScreenActivity.ARG_PLAYER_ROLES to roles
                    )
                )
            }
            CODE_START_MULTI_PLAYER_GAME -> {
                selectedId = data.getIntExtra(SelectPresetActivity.RETURN_PRESET_ID, 0)
                assert(selectedId != 0)
                doAsync {
                    val roles = Storage.getPresetInfoById(selectedId).preset.roles.keys.toTypedArray()
                    runOnUiThread {
                        startActivityForResult(intentFor<StartSinglePlayerGameActivity>(StartSinglePlayerGameActivity.ARG_ROLES to roles), CODE_RUN_MULTI_PLAYER_GAME)
                    }
                }
            }
            CODE_RUN_MULTI_PLAYER_GAME -> {
                val names = data.getStringArrayExtra(StartSinglePlayerGameActivity.RETURN_NAMES)!!
                val roles = data.getStringArrayExtra(StartSinglePlayerGameActivity.RETURN_ROLES)!!
                startActivity(
                    intentFor<MultiplayerGameActivity>(
                        MultiplayerGameActivity.MODE to MultiplayerGameActivity.MODE_CREATE_GAME,
                        MultiplayerGameActivity.ARG_PRESET_ID to selectedId,
                        MultiplayerGameActivity.ARG_PLAYER_NAMES to names,
                        MultiplayerGameActivity.ARG_PLAYER_ROLES to roles
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

