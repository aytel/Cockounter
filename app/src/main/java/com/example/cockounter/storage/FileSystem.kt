package com.example.cockounter.storage

import android.content.Context
import android.net.Uri
import com.example.cockounter.core.PresetConverter
import com.example.cockounter.core.PresetInfo
import arrow.effects.IO
import java.nio.charset.Charset


fun loadPreset(context: Context, uri: Uri): PresetInfo =
    PresetConverter().toPresetInfo(context.contentResolver.openInputStream(uri)!!.readBytes().toString(Charset.defaultCharset()))

fun savePreset(context: Context, uri: Uri, presetInfo: PresetInfo) =
    context.contentResolver.openOutputStream(uri)!!.write(PresetConverter().fromPresetInfo(presetInfo).toByteArray())

fun loadLibrary(context: Context, uri: Uri): String =
    context.contentResolver.openInputStream(uri)!!.readBytes().toString(Charset.defaultCharset())

