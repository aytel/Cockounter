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
import arrow.core.Failure
import arrow.core.Try
import com.example.cockounter.adapters.GameStateAdapter
import com.example.cockounter.core.*
import com.example.cockounter.storage.Storage
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.themedTabLayout
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onMenuItemClick
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.viewPager
import java.util.*


class AdminGameScreenActivity : AppCompatActivity(), GameHolder {

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

    override fun performGlobalScript(player: String, role: String, index: Int) {
        com.example.cockounter.script.performScript(
            state = state,
            player = PlayerDescription(player, role),
            script = preset.globalScripts[index].script,
            scriptContext = preset.globalScripts[index].context,
            context = this
        ).fold({
            scriptFailure(it.message ?: "Failed when performing script")
        }, {
            state = it
        })
        pagerAdapter.notifyDataSetChanged()
    }

    override fun performScript(player: String, role: String, index: Int) {
        com.example.cockounter.script.performScript(
            state = state,
            player = PlayerDescription(player, role),
            script = preset.roles.getValue(role).scripts[index].script,
            scriptContext = preset.roles.getValue(role).scripts[index].context,
            context = this
        ).fold({
            scriptFailure(it.message ?: "Failed when performing script")
        }, {
            state = it
        })
        pagerAdapter.notifyDataSetChanged()
    }

    override fun performScript(player: String, role: String, script: Script) {
        com.example.cockounter.script.performScript(state, PlayerDescription(player, role), script.script, script.context, this).fold({
            scriptFailure(it.message ?: "Failed when performing script")
        }, {
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


    private lateinit var state: GameState
    private lateinit var preset: Preset
    private lateinit var players: List<PlayerDescription>
    private val pagerAdapter by lazy { PlayerGameScreenAdapter(supportFragmentManager, getState, preset) }

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
                        onMenuItemClick {
                            toast("click")
                            saveState()
                        }
                        add("Save state").apply {
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
    override val getPreset = { preset }
}

class PlayerGameScreenFragment : Fragment() {
    companion object {
        const val ARG_NAME = "ARG_NAME"
        const val ARG_ROLE = "ARG_ROLE"
        fun newInstance(playerName: String, playerRole: String): PlayerGameScreenFragment {
            val args = bundleOf(ARG_NAME to playerName, ARG_ROLE to playerRole)
            val fragment = PlayerGameScreenFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var playerName = ""
    private var playerRole = ""

    lateinit var globalParametersAdapter: GameStateAdapter
    lateinit var sharedParametersAdapter: GameStateAdapter
    lateinit var privateParametersAdapter: GameStateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playerName = arguments!!.getString(ARG_NAME) ?: ""
        playerRole = arguments!!.getString(ARG_ROLE) ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val state = getState()
        val preset = getPreset()
        globalParametersAdapter = GameStateAdapter(
            getState = { state },
            extract = { it.globalParameters.values.toList().zip(preset.globalParameters.values) },
            callback = this::performScript
        )
        sharedParametersAdapter = GameStateAdapter(
            getState = { state },
            extract = { it[playerRole].sharedParameters.values.toList().zip(preset.roles.getValue(playerRole).sharedParameters.values) },
            callback = this::performScript
        )
        privateParametersAdapter = GameStateAdapter(
                getState = { state },
                extract = { it[playerRole][playerName].privateParameters.values.toList().zip(preset.roles.getValue(playerRole).privateParameters.values) },
                callback = this::performScript
        )
        return PlayerGameScreenUI(
            globalParametersAdapter,
            sharedParametersAdapter,
            privateParametersAdapter,
            preset.globalScripts.map { it.name },
            preset.roles.getValue(playerRole).scripts.map { it.name }
        ).createView(AnkoContext.Companion.create(ctx, this))
    }

    fun getState() = (act as AdminGameScreenActivity).getState()
    fun getPreset() = (act as AdminGameScreenActivity).getPreset()

    fun performScript(index: Int) {
        (act as GameHolder).performScript(playerName, playerRole, index)
        /*
        alert {
            message = getState().toString()
        }.show()
        */
        globalParametersAdapter.notifyDataSetChanged()
        sharedParametersAdapter.notifyDataSetChanged()
        privateParametersAdapter.notifyDataSetChanged()
    }

    fun performGlobalScript(index: Int) {
        (act as GameHolder).performGlobalScript(playerName, playerRole, index)
        /*
        alert {
            message = getState().toString()
        }.show()
        */
        globalParametersAdapter.notifyDataSetChanged()
        sharedParametersAdapter.notifyDataSetChanged()
        privateParametersAdapter.notifyDataSetChanged()
    }

    fun performScript(script: Script) {
        (act as GameHolder).performScript(playerName, playerRole, script)
        globalParametersAdapter.notifyDataSetChanged()
        sharedParametersAdapter.notifyDataSetChanged()
        privateParametersAdapter.notifyDataSetChanged()
    }
}

class PlayerGameScreenUI(
    val globalParametersAdapter: GameStateAdapter,
    val sharedParametersAdapter: GameStateAdapter,
    val privateParametersAdapter: GameStateAdapter,
    val globalScripts: List<String>,
    val scripts: List<String>
) : AnkoComponent<PlayerGameScreenFragment> {
    override fun createView(ui: AnkoContext<PlayerGameScreenFragment>): View = with(ui) {
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

    }

}

class PlayerGameScreenAdapter(fm: FragmentManager, val getState: () -> GameState, val preset: Preset) :
    FragmentPagerAdapter(fm) {
    private val playerNames by lazy { getState().roles.values.flatMap { it.players.values.map { it.name } } }
    private val playerRoles by lazy { getState().roles.values.flatMap { role -> role.players.values.map { role.name } } }

    override fun getItem(position: Int): Fragment =
        PlayerGameScreenFragment.newInstance(playerNames[position], playerRoles[position])

    override fun getCount(): Int = playerNames.size

    override fun getPageTitle(position: Int): CharSequence? = playerNames[position]

    override fun getItemPosition(`object`: Any): Int = PagerAdapter.POSITION_NONE
}

interface GameHolder {
    fun performGlobalScript(player: String, role: String, index: Int): Unit
    fun performScript(player: String, role: String, index: Int): Unit
    fun performScript(player: String, role: String, script: Script): Unit
    val getState: () -> GameState
    val getPreset: () -> Preset
}