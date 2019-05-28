package com.example.cockounter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.cockounter.adapters.PlayerRepresentationAdapter
import com.example.cockounter.core.*
import com.example.cockounter.script.Action
import com.example.cockounter.script.buildScriptEvaluation
import com.example.cockounter.storage.Storage
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.themedTabLayout
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.find
import org.jetbrains.anko.support.v4.viewPager
import java.util.*


class AdminGameScreenActivity : AppCompatActivity(), GameHolder, ActionPerformer {

    companion object {
        const val MODE = "MODE"
        const val MODE_ERROR = -1
        const val MODE_BUILD_NEW_STATE = 0
        const val MODE_USE_STATE = 1

        const val ARG_PRESET = "ARG_PRESET"
        const val ARG_PLAYER_NAMES = "ARG_PLAYER_NAMES"
        const val ARG_PLAYER_ROLES = "ARG_PLAYER_ROLES"
        const val ARG_STATE = "ARG_STATE"
    }

    private fun scriptFailure(message: String) {
        alert(message)
    }

    override fun performAction(action: Action) {
        evaluator(action).flatMap { it(state) }.fold({
            scriptFailure(it.message ?: "Failed when performing actionButton")
        }, {
            stack.push(state)
            state = it
        })
        pagerAdapter.notifyDataSetChanged()
    }

    fun saveState() {
        alert {
            customView {
                val stateName = editText() {
                    hint = "Name"
                }
                yesButton {
                    Storage.insertGameState(StateCapture(stateName.text.toString(), state, preset, players, Calendar.getInstance().time))
                }
                noButton {

                }
            }
        }.show()
    }

    fun undo() {
        if(stack.empty()) {
            toast("At the beginning")
        } else {
            state = stack.pop()
            pagerAdapter.notifyDataSetChanged()
        }
    }


    private lateinit var state: GameState
    private lateinit var preset: Preset
    private lateinit var players: List<PlayerDescription>
    private val pagerAdapter by lazy { PlayerGameScreenAdapter(supportFragmentManager, getState, representation) }
    private val stack: Stack<GameState> = Stack()
    private val evaluator by lazy { buildScriptEvaluation(preset, players)(this) }
    override val representation by lazy { buildByPlayerRepresentation(preset, players) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mode = intent.getIntExtra(MODE, MODE_ERROR)
        when (mode) {
            MODE_ERROR -> {
                toast("Error while loading preset")
                finish()
            }
            MODE_BUILD_NEW_STATE -> {
                preset = intent.getSerializableExtra(ARG_PRESET) as Preset
                val names = intent.getStringArrayExtra(ARG_PLAYER_NAMES)
                val roles = intent.getStringArrayExtra(ARG_PLAYER_ROLES)
                players = names.zip(roles, ::PlayerDescription)
                state = buildState(preset, players)
            }
            MODE_USE_STATE -> {
                preset = intent.getSerializableExtra(ARG_PRESET) as Preset
                val names = intent.getStringArrayExtra(ARG_PLAYER_NAMES)
                val roles = intent.getStringArrayExtra(ARG_PLAYER_ROLES)
                players = names.zip(roles, ::PlayerDescription)
                state = intent.getSerializableExtra(ARG_STATE) as GameState
            }
        }
        lateinit var myTabLayout: TabLayout
        lateinit var myViewPager: ViewPager
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
                        add("Save state").apply {
                            setOnMenuItemClickListener {
                                saveState()
                                true
                            }
                        }
                        add("Undo").apply {
                            setOnMenuItemClickListener {
                                undo()
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

    override val getState = { state }
}

class PlayerGameScreenFragment : Fragment(), ActionPerformer {
    companion object {
        const val ARG_INDEX = "ARG_INDEX"
        fun newInstance(index: Int): PlayerGameScreenFragment {
            val args = bundleOf(ARG_INDEX to index)
            val fragment = PlayerGameScreenFragment()
            fragment.arguments = args
            return fragment
        }
    }

    var index: Int = -1
    lateinit var playerAdapter: PlayerRepresentationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        index = arguments?.getInt(ARG_INDEX) ?: -1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(index != -1) {
            playerAdapter = PlayerRepresentationAdapter((act as GameHolder).representation.players[index], ::getState)
            return PlayerGameScreenUI(playerAdapter).createView(AnkoContext.Companion.create(ctx, this))
        } else {
            //FIXME
            return find(0)
        }
    }

    fun getState() = (act as AdminGameScreenActivity).getState()

    override fun performAction(action: Action) {
        (act as ActionPerformer).performAction(action)
        /*
        alert {
            message = getState().toString()
        }.show()
        */
        playerAdapter.notifyDataSetChanged()
    }
}

class PlayerGameScreenUI(val playerAdapter: PlayerRepresentationAdapter) : AnkoComponent<PlayerGameScreenFragment> {
    override fun createView(ui: AnkoContext<PlayerGameScreenFragment>): View = with(ui) {
        verticalLayout {
            listView {
                adapter = playerAdapter
            }
        }
        /*
        scrollView {
            verticalLayout {
                textView("Global counters")
                listView {
                    adapter = globalParametersAdapter
                }.lparams(matchParent, matchParent)
                textView("Shared counters")
                listView {
                    adapter = sharedParametersAdapter
                }.lparams(matchParent, matchParent)
                textView("Private counters")
                listView {
                    adapter = privateParametersAdapter
                }.lparams(matchParent, matchParent)
                globalScripts.forEachIndexed { index, script ->
                    button(script) {
                        onClick {
                            ui.owner.performGlobalScript(index)
                        }
                    }
                }
                scripts.forEachIndexed { index, script ->
                    button(script) {
                        onClick {
                            ui.owner.performScript(index)
                        }
                    }
                }
            }
        }
        */

    }

}

private class RoleGameScreenFragment : Fragment() {
    //TODO
}

private class RoleGameScreenUI : AnkoComponent<RoleGameScreenFragment> {
    override fun createView(ui: AnkoContext<RoleGameScreenFragment>): View {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

class PlayerGameScreenAdapter(fm: FragmentManager, val getState: () -> GameState, val representation: ByPlayerRepresentation) :
    FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment =
        PlayerGameScreenFragment.newInstance(position)

    override fun getCount(): Int = representation.players.size

    override fun getPageTitle(position: Int): CharSequence? = representation.players[position].name

    override fun getItemPosition(`object`: Any): Int = PagerAdapter.POSITION_NONE
}

interface GameHolder {
    val getState: () -> GameState
    val representation: ByPlayerRepresentation
}

interface ActionPerformer {
    fun performAction(action: Action): Unit
}