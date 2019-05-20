package com.example.cockounter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.cockounter.core.GameState
import com.example.cockounter.core.Preset

class AdminGameScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}

class PlayerGameScreenFragment : Fragment() {
    companion object {

    }

}

class PlayerGameScreenAdapter(fm: FragmentManager, val getState: () -> GameState, val preset: Preset) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}