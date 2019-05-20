package com.example.cockounter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.ListAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.cockounter.adapters.GameStateAdapter
import com.example.cockounter.adapters.ParameterAdapter
import com.example.cockounter.core.GameState
import com.example.cockounter.core.Preset
import com.example.cockounter.core.get
import org.jetbrains.anko.*
import org.jetbrains.anko.design.tabLayout
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.viewPager

class AdminGameScreenActivity : AppCompatActivity(), GameHolder {
    override fun performScript(player: String, role: String, index: Int) {
        state = com.example.cockounter.script.performScript(state, player, preset.roles.getValue(role).scripts[index].script)
    }

    private lateinit var state: GameState
    private lateinit var preset: Preset

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state = intent.getSerializableExtra("state") as GameState
        preset = intent.getSerializableExtra("preset") as Preset
        tabLayout {
            val pager = viewPager {
                adapter = PlayerGameScreenAdapter(supportFragmentManager, getState, preset)
            }
            setupWithViewPager(pager)
        }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playerName = arguments!!.getString(ARG_NAME) ?: ""
        playerRole = arguments!!.getString(ARG_ROLE) ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val state = getState()
        val preset = getPreset()
        return PlayerGameScreenUI(
            GameStateAdapter({ state }, {it.sharedParameters.values.toList()}),
            GameStateAdapter({ state }, {it[playerRole].sharedParameters.values.toList()}),
            GameStateAdapter({ state }, {it[playerRole][playerName].privateParameters.values.toList()}),
            preset.roles.getValue(playerRole).scripts.map{it.name}
        ).createView(AnkoContext.Companion.create(ctx, this))
    }

    fun getState() = (act as AdminGameScreenActivity).getState()
    fun getPreset() = (act as AdminGameScreenActivity).getPreset()

    fun performScript(index: Int) {
        (act as GameHolder).performScript(playerName, playerRole, index)
    }
}

class PlayerGameScreenUI(val globalParametersAdapter: ListAdapter,
                         val sharedParametersAdapter: ListAdapter,
                         val privateParametersAdapter: ListAdapter,
                         val scripts: List<String>) : AnkoComponent<PlayerGameScreenFragment> {
    override fun createView(ui: AnkoContext<PlayerGameScreenFragment>): View = with(ui) {
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
                button(script) {
                    onClick {
                        ui.owner.performScript(index)
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

    override fun getItem(position: Int): Fragment {
        TODO()
    }

    override fun getCount(): Int = playerNames.size

    override fun getPageTitle(position: Int): CharSequence? = playerNames[position]
}

interface GameHolder {
    fun performScript(player: String, role: String, index: Int): Unit
    val getState: () -> GameState
    val getPreset: () -> Preset
}