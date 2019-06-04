package com.example.cockounter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.Try
import com.example.cockounter.core.*
import com.example.cockounter.network.NetworkHandler
import com.example.cockounter.script.Action
import com.example.cockounter.script.Evaluation
import com.example.cockounter.script.ScriptEvaluation
import com.example.cockounter.script.buildScriptEvaluation
import com.example.cockounter.storage.Storage
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import io.ktor.client.HttpClient
import org.jetbrains.anko.*
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.themedTabLayout
import org.jetbrains.anko.support.v4.viewPager
import java.util.*

private class MultiPlayerGameViewModel() : ViewModel() {
    lateinit var state: MutableLiveData<GameState>
    lateinit var preset: Preset
    lateinit var players: List<PlayerDescription>
    lateinit var representation: MutableLiveData<GameRepresentation>
    lateinit var evaluator: ScriptEvaluation
    private lateinit var name: String
    private lateinit var uuid: UUID
    private var currentLayout = LayoutType.BY_PLAYER

    constructor(id: Int, names: Array<String>, roles: Array<String>) : this() {
        val presetInfo = doAsyncResult { Storage.getPresetInfoById(id)}.get()
        uuid = UUID.randomUUID()
        players = names.zip(roles, ::PlayerDescription)
        preset = presetInfo.preset
        state = MutableLiveData()
        state.value = buildState(preset, players)
        representation = MutableLiveData()
        representation.value = buildByPlayerRepresentation(preset, players)
        val stateCapture = StateCapture(0, "", state.value!!, preset, players, Date(0), uuid)
        NetworkHandler.createGame(stateCapture)
        evaluator = buildScriptEvaluation(preset, players)
    }

    constructor(uuid: UUID) : this() {
        val stateCapture = NetworkHandler.connectToGame(uuid)
        preset = stateCapture.preset
        players = stateCapture.players
        state = MutableLiveData()
        state.value = stateCapture.state
        representation = MutableLiveData()
        representation.value = buildByPlayerRepresentation(preset, players)
        evaluator = buildScriptEvaluation(preset, players)
    }

    enum class LayoutType {
        BY_PLAYER, BY_ROLE
    }

    fun performAction(action: Action, evaluator: (Action) -> Try<Evaluation>): Option<String> {
        try {
            evaluator(action).flatMap { it(state.value!!) }.fold({
                return Some(it.message ?: "")
            }, {
                state.value = NetworkHandler.updateGameState(uuid, it)
                return None
            })
        } catch (e: Exception) {
            return Some(e.toString())
        }
    }

    fun changeLayout() {
        when(currentLayout) {
            LayoutType.BY_PLAYER -> {
                representation.value = buildByRoleRepresentation(preset, players)
                currentLayout = LayoutType.BY_ROLE
            }
            LayoutType.BY_ROLE -> {
                representation.value = buildByPlayerRepresentation(preset, players)
                currentLayout = LayoutType.BY_PLAYER
            }
        }

    }
}

class MultiplayerGameActivity : AppCompatActivity(), GameHolder, ActionPerformer {
    override val getRepresentation: () -> GameRepresentation = { viewModel.representation.value!! }
    override val getState: () -> GameState = { viewModel.state.value!! }

    companion object {
        const val MODE = "MODE"
        const val MODE_ERROR = -1
        const val MODE_JOIN_GAME = 0
        const val MODE_CREATE_GAME = 1

        const val ARG_PRESET_ID = "ARG_PRESET_ID"
        const val ARG_PLAYER_NAMES = "ARG_PLAYER_NAMES"
        const val ARG_PLAYER_ROLES = "ARG_PLAYER_ROLES"
        const val ARG_UUID = "ARG_UUID"

        private enum class LayoutType {
            BY_PLAYER, BY_ROLE
        }
    }

    private lateinit var pagerAdapter: PlayerGameScreenAdapter
    private lateinit var evaluator: (Action) -> Try<Evaluation>
    private lateinit var myTabLayout: TabLayout
    private lateinit var myViewPager: ViewPager
    private lateinit var viewModel: MultiPlayerGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when(intent.getIntExtra(MODE, MODE_ERROR)) {
            MODE_CREATE_GAME -> {
                val id = intent.getIntExtra(ARG_PRESET_ID, 0)
                assert(id != 0)
                val names = intent.getStringArrayExtra(ARG_PLAYER_NAMES)
                val roles = intent.getStringArrayExtra(ARG_PLAYER_ROLES)
                viewModel = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        return MultiPlayerGameViewModel(id, names, roles) as T
                    }
                }).get(MultiPlayerGameViewModel::class.java)
            }
            MODE_JOIN_GAME -> {
                viewModel = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        return MultiPlayerGameViewModel(UUID.fromString(intent.getStringExtra(ARG_UUID))) as T
                    }
                }).get(MultiPlayerGameViewModel::class.java)
            }
        }
        viewModel.state.observe(this, androidx.lifecycle.Observer { _ -> pagerAdapter.notifyDataSetChanged() })
        viewModel.representation.observe(this, androidx.lifecycle.Observer { _ -> pagerAdapter.notifyDataSetChanged() })
        evaluator = viewModel.evaluator(this)
        pagerAdapter = PlayerGameScreenAdapter(supportFragmentManager, getState, getRepresentation)

        coordinatorLayout {
            lparams(matchParent, matchParent)

            myViewPager = viewPager {
                id = R.id.container
            }.lparams(matchParent, matchParent)
            (myViewPager.layoutParams as CoordinatorLayout.LayoutParams).behavior = AppBarLayout.ScrollingViewBehavior()
            appBarLayout {
                lparams(matchParent, wrapContent)

                toolbar {
                    menu.apply {
                        add("Change layout").apply {
                            setOnMenuItemClickListener {
                                changeLayout()
                                true
                            }
                        }
                    }

                }

                myTabLayout = themedTabLayout(R.style.ThemeOverlay_AppCompat_Dark) {
                    lparams(matchParent, wrapContent)
                    {
                        tabGravity = TabLayout.GRAVITY_FILL
                        tabMode = TabLayout.MODE_FIXED
                    }
                }
            }
        }
        myViewPager.adapter = pagerAdapter
        myTabLayout.setupWithViewPager(myViewPager)
    }

    private fun scriptFailure(message: String) {
        alert(message).show()
    }

    override fun performAction(action: Action) {
        when(val result = viewModel.performAction(action, evaluator)) {
            is Some -> scriptFailure(result.t)
        }
    }

    private fun changeLayout() {
        viewModel.changeLayout()
    }
}
