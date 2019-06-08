package com.example.cockounter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.RequiresApi
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
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import org.jetbrains.anko.*
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.themedTabLayout
import org.jetbrains.anko.support.v4.viewPager
import java.net.NetworkInterface
import java.util.*

class MultiPlayerGameViewModel() : ViewModel() {
    lateinit var state: MutableLiveData<GameState>
    lateinit var preset: Preset
    lateinit var players: List<PlayerDescription>
    lateinit var representation: MutableLiveData<GameRepresentation>
    lateinit var evaluator: ScriptEvaluation
    private lateinit var name: String
    lateinit var uuid: UUID
    private var currentLayout = LayoutType.BY_PLAYER

    @RequiresApi(Build.VERSION_CODES.N)
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
        this.uuid = uuid
    }

    enum class LayoutType {
        BY_PLAYER, BY_ROLE
    }

    fun performAction(action: Action, evaluator: (Action) -> Try<Evaluation>, context: Context): Option<String> {
        try {
            evaluator(action).flatMap { it(state.value!!) }.fold({
                return Some(it.message ?: "")
            }, {
                context.runOnUiThread {
                    state.value = NetworkHandler.updateGameState(uuid, it)
                }
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

    fun updateState(context: Context) {
        val data = NetworkHandler.getGameState(uuid)
        context.runOnUiThread {
            state.value = data
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
        val ints = NetworkInterface.getNetworkInterfaces()
        ints.asSequence().forEach {
            System.err.println(it.name + " " + it.displayName)
            if (!it.isLoopback) {
                it.inetAddresses.asSequence().forEach {
                    if (!it.isSiteLocalAddress)
                        System.err.println(it.toString())
                }
            }
        }
        when(intent.getIntExtra(MODE, MODE_ERROR)) {
            MODE_CREATE_GAME -> {
                val id = intent.getIntExtra(ARG_PRESET_ID, 0)
                assert(id != 0)
                val names = intent.getStringArrayExtra(ARG_PLAYER_NAMES)
                val roles = intent.getStringArrayExtra(ARG_PLAYER_ROLES)
                viewModel = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
                    @RequiresApi(Build.VERSION_CODES.N)
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
        //viewModel.state.observe(this, androidx.lifecycle.Observer { _ -> pagerAdapter.notifyDataSetChanged() })
        viewModel.representation.observe(this, androidx.lifecycle.Observer { _ -> pagerAdapter.notifyDataSetChanged() })
        evaluator = viewModel.evaluator(this)
        pagerAdapter = PlayerGameScreenAdapter(supportFragmentManager, getRepresentation)

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
                        add("Show uuid").apply {
                            setOnMenuItemClickListener {
                                showUUID()
                                true
                            }
                        }
                        add("Update").apply {
                            setIcon(R.drawable.ic_refresh_black_24dp)
                            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                            setOnMenuItemClickListener {
                                updateState()
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
        when(val result = doAsyncResult { viewModel.performAction(action, evaluator, this@MultiplayerGameActivity) }.get()) {
            is Some -> scriptFailure(result.t)
        }
    }

    private fun changeLayout() {
        viewModel.changeLayout()
    }

    private fun showUUID() {
        alert {
            val qr = QRCodeWriter().encode(viewModel.uuid.toString(), BarcodeFormat.QR_CODE, 512, 512)
            val bitmap = Bitmap.createBitmap(qr.width, qr.height, Bitmap.Config.RGB_565)
            for(i in 0..(qr.width - 1)) {
                for(j in 0..(qr.height - 1)) {
                    bitmap.setPixel(i, j, if(qr.get(i, j)) Color.BLACK else Color.WHITE)
                }
            }
            customView {
                imageView {
                    setImageBitmap(bitmap)
                }
            }

        }.show()
    }

    private fun updateState() {
        viewModel.updateState(this)
    }
}
