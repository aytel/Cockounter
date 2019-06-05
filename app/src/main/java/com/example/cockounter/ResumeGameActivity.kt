package com.example.cockounter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.cockounter.adapters.StateCaptureAdapter
import com.example.cockounter.storage.Storage
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onItemClick

class ResumeGameActivity : AppCompatActivity() {

    val states by lazy { doAsyncResult { Storage.getAllGameStates().value!!.toMutableList() }.get()}
    val captureAdapter by lazy {StateCaptureAdapter(states)}
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ResumeGameUI(captureAdapter).setContentView(this)
    }

    fun resumeGame(index: Int) {
        startActivity(intentFor<SinglePlayerGameScreenActivity>(
            SinglePlayerGameScreenActivity.MODE to SinglePlayerGameScreenActivity.MODE_USE_STATE,
            SinglePlayerGameScreenActivity.ARG_STATE_ID to states[index].id
        ))
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
