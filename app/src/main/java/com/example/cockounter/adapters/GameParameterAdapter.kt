package com.example.cockounter.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.cockounter.core.GameParameter
import com.example.cockounter.core.IntegerGameParameter

class GameParameterAdapter(val ctx: Context, val resource: Int, val items: MutableList<GameParameter>) :
    ArrayAdapter<GameParameter>(ctx, resource, items) {
}
