package com.example.cockounter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.viewpager.widget.ViewPager
import com.example.cockounter.core.*
import com.example.cockounter.network.NetworkHandler
import com.example.cockounter.script.Action
import com.example.cockounter.script.buildScriptEvaluation
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.themedTabLayout
import org.jetbrains.anko.support.v4.viewPager
import java.util.*

class MultiplayerGameActivity : AppCompatActivity(), GameHolder, ActionPerformer {
    override val getState: () -> GameState = {state}

    companion object {
        const val MODE = "MODE"
        const val MODE_ERROR = -1
        const val MODE_CONNECT = 0
        const val MODE_CONTINUE_GAME = 1

        const val ARG_NAME = "ARG_NAME"
        const val ARG_UUID = "ARG_UUID"

        private enum class LayoutType {
            BY_PLAYER, BY_ROLE
        }
    }

    private lateinit var state: GameState
    private lateinit var preset: Preset
    private lateinit var players: List<PlayerDescription>
    private val pagerAdapter by lazy { PlayerGameScreenAdapter(supportFragmentManager, getState, {representation}) }
    private val evaluator by lazy { buildScriptEvaluation(preset, players)(this) }
    override lateinit var representation: GameRepresentation
    private lateinit var myTabLayout: TabLayout
    private lateinit var myViewPager: ViewPager
    private var currentLayout = LayoutType.BY_PLAYER
    private lateinit var player: PlayerDescription
    private lateinit var name: String
    private lateinit var uuid: UUID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val name = intent.getStringExtra(ARG_NAME)
        val uuid = intent.getStringExtra(ARG_UUID)
        if(name == null || uuid == null) {
            finish()
        }

        val stateCapture = NetworkHandler.connectToGame(UUID.fromString(uuid))
        state = stateCapture.state
        preset = stateCapture.preset
        players = stateCapture.players
        val temp = players.find { it.name == name }
        if(temp == null) {
            finish()
        }
        player = temp!!
        this.name = name
        this.uuid = UUID.fromString(uuid)

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
        try {
            state = NetworkHandler.getGameState(uuid)
            evaluator(action).flatMap { it(state) }.fold({
                scriptFailure(it.message ?: "Failed when performing actionButton")
            }, {
                state = NetworkHandler.updateGameState(uuid, it.copy(version = it.version + 1))
            })
            pagerAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
            scriptFailure(e.message.toString())
        }
    }

    private fun changeLayout() {
        when(currentLayout) {
            Companion.LayoutType.BY_PLAYER -> {
                representation = buildByRoleRepresentation(preset, players)
                currentLayout = Companion.LayoutType.BY_ROLE
                pagerAdapter.notifyDataSetChanged()
            }
            Companion.LayoutType.BY_ROLE -> {
                representation = buildByPlayerRepresentation(preset, players)
                currentLayout = Companion.LayoutType.BY_PLAYER
                pagerAdapter.notifyDataSetChanged()
            }
        }

    }

}
