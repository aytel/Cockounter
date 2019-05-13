package com.example.cockounter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.example.cockounter.core.*
import com.example.cockounter.script.performScript
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk27.coroutines.onClick

class PlayerGameScreenActivity : AppCompatActivity() {
    private val globalParametersList = mutableListOf<GameParameter>()
    private val sharedParametersList = mutableListOf<GameParameter>()
    private val privateParametersList = mutableListOf<GameParameter>()
    private val globalParametersAdapter by lazy {
        ArrayAdapter<Any>(
            this, android.R.layout.simple_list_item_1,
            globalParametersList as List<Any>
        )
    }
    private val sharedParametersAdapter by lazy {
        ArrayAdapter<Any>(
            this, android.R.layout.simple_list_item_1,
            sharedParametersList as List<Any>
        )
    }
    private val privateParametersAdapter by lazy {
        ArrayAdapter<Any>(
            this, android.R.layout.simple_list_item_1,
            privateParametersList as List<Any>
        )
    }
    private lateinit var state: GameState

    private fun updateAdapters(role: String, player: String) {
        globalParametersList.clear()
        globalParametersList.addAll(state.sharedParameters.values)
        sharedParametersList.clear()
        sharedParametersList.addAll(state[role].sharedParameters.values)
        privateParametersList.clear()
        privateParametersList.addAll(state[role][player].privateParameters.values)
        globalParametersAdapter.notifyDataSetChanged()
        sharedParametersAdapter.notifyDataSetChanged()
        privateParametersAdapter.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state = intent.getSerializableExtra("state") as GameState
        val player = intent.getStringExtra("player")!!
        val role = intent.getStringExtra("role")!!
        val scripts = intent.getSerializableExtra("scripts") as List<Script>
        updateAdapters(role, player)

        scrollView {
            verticalLayout {
                textView("Global counters")
                listView {
                    adapter = globalParametersAdapter
                }
                textView("Shared counters")
                listView {
                    adapter = sharedParametersAdapter
                }
                textView("Private counters")
                listView {
                    adapter = privateParametersAdapter
                }
            }
            scripts.forEachIndexed { index, script ->
                button(script.name) {
                    onClick {
                        state = performScript(state, player, script.script)
                        updateAdapters(role, player)
                    }
                }
            }
        }
    }
}
