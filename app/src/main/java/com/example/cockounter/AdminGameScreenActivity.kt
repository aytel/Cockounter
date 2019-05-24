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
import com.example.cockounter.adapters.GameStateAdapter
import com.example.cockounter.core.*
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.tabItem
import org.jetbrains.anko.design.themedTabLayout
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.sdk27.coroutines.onMenuItemClick
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.viewPager


class AdminGameScreenActivity : AppCompatActivity(), GameHolder {

    companion object {
        const val INIT_FLAG = "INIT_FLAG"

        const val FLAG_ERROR = -1
        const val FLAG_BUILD_NEW_STATE = 0

        const val ARG_PRESET = "preset"
        const val ARG_PLAYER_NAMES = "names"
        const val ARG_PLAYER_ROLES = "roles"
    }

    override fun performGlobalScript(player: String, role: String, index: Int) {
        state = com.example.cockounter.script.performScriptWithContext(
            state,
            player,
            preset.globalScripts[index].script,
            this
        )
        pagerAdapter.notifyDataSetChanged()
    }

    override fun performScript(player: String, role: String, index: Int) {
        state = com.example.cockounter.script.performScriptWithContext(
            state,
            player,
            preset.roles.getValue(role).scripts[index].script,
            this
        )
        pagerAdapter.notifyDataSetChanged()
    }

    private lateinit var state: GameState
    private lateinit var preset: Preset
    private val pagerAdapter by lazy { PlayerGameScreenAdapter(supportFragmentManager, getState, preset) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initFlag = intent.getIntExtra(INIT_FLAG, FLAG_ERROR)
        when (initFlag) {
            FLAG_ERROR -> {
                toast("Error while loading preset")
                finish()
            }
            FLAG_BUILD_NEW_STATE -> {
                preset = intent.getSerializableExtra(ARG_PRESET) as Preset
                val names = intent.getStringArrayExtra(ARG_PLAYER_NAMES)
                val roles = intent.getStringArrayExtra(ARG_PLAYER_ROLES)
                val players = names.zip(roles, ::PlayerDescription)
                state = buildState(preset, players)
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
                            toast("Wow")
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
        globalParametersAdapter = GameStateAdapter({ state }, { it.globalParameters.values.toList() })
        sharedParametersAdapter = GameStateAdapter({ state }, { it[playerRole].sharedParameters.values.toList() })
        privateParametersAdapter =
            GameStateAdapter({ state }, { it[playerRole][playerName].privateParameters.values.toList() })
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
    val getState: () -> GameState
    val getPreset: () -> Preset
}