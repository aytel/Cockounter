package com.example.cockounter.activities

import android.os.Bundle
import android.view.View
import android.widget.ListAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.cockounter.adapters.ShowListAdapter
import com.example.cockounter.adapters.listElementShow
import com.example.cockounter.core.StateCapture
import com.example.cockounter.storage.Storage
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onItemClick
import org.jetbrains.anko.sdk27.coroutines.onItemLongClick


class ResumeGameActivity : AppCompatActivity() {
    private val captureAdapter by lazy { ShowListAdapter<StateCapture>(StateCapture.listElementShow()) }
    private val states = Storage.getAllGameStates()

    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        states.observe(this, Observer { list -> captureAdapter.update(list) })
        ResumeGameUI(captureAdapter).setContentView(this)
    }

    fun resumeGame(index: Int) {
        startActivity(intentFor<SinglePlayerGameActivity>(
            SinglePlayerGameActivity.MODE to SinglePlayerGameActivity.MODE_USE_STATE,
            SinglePlayerGameActivity.ARG_STATE_ID to states.value!![index].id
        ))
        finish()
    }
}

private class ResumeGameUI(val captureAdapter: ListAdapter) : AnkoComponent<ResumeGameActivity> {
    override fun createView(ui: AnkoContext<ResumeGameActivity>): View = with(ui) {
        verticalLayout {
            listView {
                adapter = captureAdapter
                onItemClick { _, _, index, _ ->
                    owner.resumeGame(index)
                }
                onItemLongClick { p0, p1, p2, p3 ->  }
            }
        }
    }

}
