package com.example.cockounter.storage

import android.content.Context
import android.net.Uri
import arrow.core.Try
import com.example.cockounter.core.PresetConverter
import com.example.cockounter.core.PresetInfo
import java.io.File
import android.provider.MediaStore
import androidx.loader.content.CursorLoader
import java.nio.charset.Charset


fun loadPreset(context: Context, uri: Uri): Try<PresetInfo> = Try {
    PresetConverter().toPresetInfo(context.contentResolver.openInputStream(uri)!!.readBytes().toString(Charset.defaultCharset()))
}

fun savePreset(context: Context, uri: Uri, presetInfo: PresetInfo): Try<Unit> = Try {
    context.contentResolver.openOutputStream(uri)!!.write(PresetConverter().fromPresetInfo(presetInfo).toByteArray())
}
