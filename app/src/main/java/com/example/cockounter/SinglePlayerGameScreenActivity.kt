package com.example.cockounter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.Try
import com.example.cockounter.adapters.ExpandablePlayerRepresentationAdapter
import com.example.cockounter.core.*
import com.example.cockounter.script.Action
import com.example.cockounter.script.Evaluation
import com.example.cockounter.script.buildScriptEvaluation
import com.example.cockounter.storage.Storage
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.tabItem
import org.jetbrains.anko.design.themedTabLayout
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.find
import org.jetbrains.anko.support.v4.viewPager
import java.util.*

private class SinglePlayerGameScreenViewModel() : ViewModel() {
    lateinit var state: MutableLiveData<GameState>
    lateinit var preset: Preset
    lateinit var players: List<PlayerDescription>
    val stack: Stack<GameState> = Stack()
    lateinit var representation: MutableLiveData<GameRepresentation>
    private var currentLayout = LayoutType.BY_PLAYER

    constructor(id: Int, names: Array<String>, roles: Array<String>) : this() {
        val presetInfo = doAsyncResult {Storage.getPresetInfoById(id)}.get()
        players = names.zip(roles, ::PlayerDescription)
        preset = presetInfo.preset
        state = MutableLiveData()
        state.value = buildState(preset, players)
        representation = MutableLiveData()
        representation.value = buildByPlayerRepresentation(preset, players)
    }

    constructor(id: Int) : this() {
        val stateCapture = doAsyncResult { Storage.getGameStateById(id) }.get()
        preset = stateCapture.preset
        players = stateCapture.players
        state = MutableLiveData()
        state.value = stateCapture.state
        representation = MutableLiveData()
        representation.value = buildByPlayerRepresentation(preset, players)
    }

    enum class LayoutType {
        BY_PLAYER, BY_ROLE
    }

    fun undo(): Boolean {
        if(stack.empty()) {
            return false
        } else {
            state.value = stack.pop()
            return true
        }
    }

    fun performAction(action: Action, evaluator: (Action) -> Try<Evaluation>, context: Context): Option<String> {
        try {
            evaluator(action).flatMap { it(state.value!!) }.fold({
                return Some(it.message ?: "")
            }, {
                stack.push(state.value!!)
                with(context) {
                    runOnUiThread {
                        state.value = it
                    }
                }
                return None
            })
        } catch (e: Exception) {
            return Some(e.toString())
        }
    }

    fun saveState(name: String) {
        //Log.i("Kek", StateCaptureConverter.gson.toJson(StateCapture(0, stateName.text.toString(), state, preset, players, Calendar.getInstance().time, UUID(1, 1))))
        //FIXME
        Storage.insertGameState(StateCapture(0, name, state.value!!, preset, players, Calendar.getInstance().time, UUID(1, 1)))
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


class SinglePlayerGameScreenActivity : AppCompatActivity(), GameHolder, ActionPerformer {

    companion object {
        const val MODE = "MODE"
        const val MODE_ERROR = -1
        const val MODE_BUILD_NEW_STATE = 0
        const val MODE_USE_STATE = 1

        const val ARG_PRESET_ID = "ARG_PRESET_ID"
        const val ARG_PLAYER_NAMES = "ARG_PLAYER_NAMES"
        const val ARG_PLAYER_ROLES = "ARG_PLAYER_ROLES"
        const val ARG_STATE_ID = "ARG_STATE_ID"
    }

    private val evaluator by lazy { buildScriptEvaluation(viewModel.preset, viewModel.players)(this) }

    private fun scriptFailure(message: String) {
        alert(message).show()
    }

    override fun performAction(action: Action) {
        doAsync {
            when (val result = viewModel.performAction(action, evaluator, this@SinglePlayerGameScreenActivity)) {
                is Some -> {
                    runOnUiThread {
                        scriptFailure(result.t)
                    }
                }
            }
        }
    }

    fun saveState() {
        alert {
            customView {
                val stateName = editText() {
                    hint = "Name"
                }
                yesButton {
                    viewModel.saveState(stateName.text.toString())
                }
                noButton {}
            }
        }.show()
    }

    fun undo() {
        if(!viewModel.undo()) {
            toast("At the beginning")
        }
    }

    fun changeLayout() {
        viewModel.changeLayout()
    }


    private val pagerAdapter by lazy { PlayerGameScreenAdapter(
        supportFragmentManager,
        {viewModel.representation.value!!}) }
    private lateinit var myTabLayout: TabLayout
    private lateinit var myViewPager: ViewPager
    private lateinit var viewModel: SinglePlayerGameScreenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (intent.getIntExtra(MODE, MODE_ERROR)) {
            MODE_ERROR -> {
                toast("Error while loading preset")
                finish()
            }
            MODE_BUILD_NEW_STATE -> {
                viewModel = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        val id = intent.getIntExtra(ARG_PRESET_ID, 0)
                        val names = intent.getStringArrayExtra(ARG_PLAYER_NAMES)
                        val roles = intent.getStringArrayExtra(ARG_PLAYER_ROLES)
                        return SinglePlayerGameScreenViewModel(id, names, roles) as T
                    }

                }).get(SinglePlayerGameScreenViewModel::class.java)
            }
            MODE_USE_STATE -> {
                viewModel = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        val id = intent.getIntExtra(ARG_STATE_ID, 0)
                        return SinglePlayerGameScreenViewModel(id) as T
                    }

                }).get(SinglePlayerGameScreenViewModel::class.java)
            }
        }
        //viewModel.state.observe(this, androidx.lifecycle.Observer { _ -> pagerAdapter.notifyDataSetChanged() })
        viewModel.representation.observe(this, androidx.lifecycle.Observer { _ -> pagerAdapter.notifyDataSetChanged() })
        coordinatorLayout {
            lparams(matchParent, matchParent)

            myViewPager = viewPager {
                id = R.id.container
            }.lparams(matchParent, matchParent)
            (myViewPager.layoutParams as CoordinatorLayout.LayoutParams).behavior = AppBarLayout.ScrollingViewBehavior()
            appBarLayout {
                lparams(matchParent, wrapContent)

                toolbar {
                    tabItem {

                    }
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
                        add("Change layout").apply {
                            setOnMenuItemClickListener {
                                changeLayout()
                                true
                            }
                        }
                    }

                }
                //viewPager {
                //    adapter = pagerAdapter
                //    id = R.id.container
                //}

                myTabLayout = themedTabLayout(R.style.ThemeOverlay_AppCompat_Dark) {
                    tabMode = TabLayout.MODE_SCROLLABLE
                    lparams(matchParent, wrapContent) {
                        tabGravity = TabLayout.GRAVITY_FILL
                        tabMode = TabLayout.MODE_FIXED
                    }
                }
            }
        }
        myViewPager.adapter = pagerAdapter
        myTabLayout.setupWithViewPager(myViewPager)
    }

    override val getState = { viewModel.state.value!! }
    override val getRepresentation = { viewModel.representation.value!! }
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
    lateinit var gameAdapter: BaseExpandableListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        index = arguments?.getInt(ARG_INDEX) ?: -1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(index != -1) {
            val representation = (act as GameHolder).getRepresentation()
            when(representation) {
                is ByPlayerRepresentation -> {
                    gameAdapter = ExpandablePlayerRepresentationAdapter(representation.players[index], ::performAction)
                    if(act is SinglePlayerGameScreenActivity) {
                        val viewModel = ViewModelProviders.of(act).get(SinglePlayerGameScreenViewModel::class.java)
                        viewModel.state.observe(this, androidx.lifecycle.Observer {
                            (gameAdapter as ExpandablePlayerRepresentationAdapter).update(it)
                        })

                    } else if(act is MultiplayerGameActivity) {
                        val viewModel = ViewModelProviders.of(act).get(MultiPlayerGameViewModel::class.java)
                        viewModel.state.observe(this, androidx.lifecycle.Observer {
                            (gameAdapter as ExpandablePlayerRepresentationAdapter).update(it)
                        })
                    }
                }
                is ByRoleRepresentation -> {
                    //gameAdapter = RoleRepresentationAdapter(representation.roles[index], ::getState, ::performAction)
                }
            }

            return PlayerGameScreenUI(gameAdapter).createView(AnkoContext.Companion.create(ctx, this))
        } else {
            //FIXME
            return find(0)
        }
    }

    fun getState() = (act as GameHolder).getState()

    override fun performAction(action: Action) {
        (act as ActionPerformer).performAction(action)
        /*
        alert {
            message = getState().toString()
        }.listElementShow()
        */
    }
}

class PlayerGameScreenUI(val playerAdapter: ExpandableListAdapter) : AnkoComponent<PlayerGameScreenFragment> {
    override fun createView(ui: AnkoContext<PlayerGameScreenFragment>): View = with(ui) {
        verticalLayout {
            expandableListView {
                setAdapter(playerAdapter)
            }
        }
    }

}

class PlayerGameScreenAdapter(
    fm: FragmentManager,
    val getRepresentation: () -> GameRepresentation
) :
    FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment =
        PlayerGameScreenFragment.newInstance(position)

    override fun getCount(): Int {
        val representation = getRepresentation()
        return when(representation) {
            is ByPlayerRepresentation -> representation.players.size
            is ByRoleRepresentation -> representation.roles.size
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        val representation = getRepresentation()
        return when(representation) {
            is ByPlayerRepresentation -> representation.players[position].name
            is ByRoleRepresentation -> representation.roles[position].role
        }
    }

    override fun getItemPosition(`object`: Any): Int = PagerAdapter.POSITION_NONE
}

interface GameHolder {
    val getState: () -> GameState
    val getRepresentation: () -> GameRepresentation
}

interface ActionPerformer {
    fun performAction(action: Action): Unit
}