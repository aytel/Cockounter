package com.example.cockounter.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.cockounter.core.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors


@Database(entities = [StateCapture::class, PresetInfo::class], version = 10)
abstract class Storage : RoomDatabase() {
    companion object {
        lateinit var database: Storage
        private val databaseWorker = Executors.newSingleThreadExecutor()

        fun getAllPresetInfos() = databaseWorker.submit(Callable<List<PresetInfo>> { database.presetInfoDao().getAll() })
        fun insertPreset(presetInfo: PresetInfo) = databaseWorker.submit { database.presetInfoDao().insert(presetInfo) }
        fun deletePreset(presetInfo: PresetInfo) = databaseWorker.submit { database.presetInfoDao().delete(presetInfo) }
        fun nukePresets() = databaseWorker.submit { database.presetInfoDao().nukeTable() }

        fun getAllGameStates() = databaseWorker.submit(Callable<List<StateCapture>> { database.stateCaptureDao().getAll() })
        fun insertGameState(stateCapture: StateCapture) = databaseWorker.submit { database.stateCaptureDao().insert(stateCapture) }
        fun deleteGameState(stateCapture: StateCapture) = databaseWorker.submit { database.stateCaptureDao().delete(stateCapture) }
    }

    abstract fun stateCaptureDao(): StateCaptureDao
    abstract fun presetInfoDao(): PresetInfoDao
}
