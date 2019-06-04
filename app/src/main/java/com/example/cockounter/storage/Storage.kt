package com.example.cockounter.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.cockounter.core.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors


@Database(entities = [StateCapture::class, PresetInfo::class], version = 13)
abstract class Storage : RoomDatabase() {
    companion object {
        lateinit var database: Storage

        fun getAllPresetInfos() = database.presetInfoDao().getAll()
        fun getPresetInfoById(id: Int) = database.presetInfoDao().getById(id)
        fun insertPreset(presetInfo: PresetInfo) = database.presetInfoDao().insert(presetInfo)
        fun deletePreset(presetInfo: PresetInfo) = database.presetInfoDao().delete(presetInfo)
        fun nukePresets() = database.presetInfoDao().nukeTable()

        fun getGameStateById(id: Int) = database.stateCaptureDao().getById(id)
        fun getAllGameStates() = database.stateCaptureDao().getAll()
        fun insertGameState(stateCapture: StateCapture) = database.stateCaptureDao().insert(stateCapture)
        fun deleteGameState(stateCapture: StateCapture) = database.stateCaptureDao().delete(stateCapture)
    }

    abstract fun stateCaptureDao(): StateCaptureDao
    abstract fun presetInfoDao(): PresetInfoDao
}
