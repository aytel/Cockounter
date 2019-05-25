package com.example.cockounter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.cockounter.adapters.StateCaptureAdapter
import com.example.cockounter.core.StateCapture
import com.example.cockounter.storage.Storage
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onItemClick

class ResumeGameActivity : AppCompatActivity() {

    val states by lazy { Storage.getAllGameStates().get().toMutableList() }
    val captureAdapter by lazy {StateCaptureAdapter(states)}
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ResumeGameUI(captureAdapter).setContentView(this)
    }

    fun resumeGame(index: Int) {
        startActivity(intentFor<AdminGameScreenActivity>(
            AdminGameScreenActivity.MODE to AdminGameScreenActivity.MODE_USE_STATE,
            AdminGameScreenActivity.ARG_PRESET to states[index].preset,
            AdminGameScreenActivity.ARG_STATE to states[index].state,
            AdminGameScreenActivity.ARG_PLAYER_ROLES to (states[index].players.map { it.role }.toTypedArray()),
            AdminGameScreenActivity.ARG_PLAYER_NAMES to states[index].players.map { it.name }.toTypedArray()))
        finish()
    }
}

private class ResumeGameUI(val captureAdapter: StateCaptureAdapter) : AnkoComponent<ResumeGameActivity> {
    override fun createView(ui: AnkoContext<ResumeGameActivity>): View = with(ui) {
        verticalLayout {
            listView {
                adapter = captureAdapter
                onItemClick { _, _, index, _ ->
                    owner.resumeGame(index)
                }
            }
        }
    }

}
