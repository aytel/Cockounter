package com.example.cockounter.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.cockounter.core.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors


@Database(entities = [GameState::class, PresetInfo::class], version = 2)
abstract class Storage : RoomDatabase() {
    companion object {
        lateinit var database: Storage
        private val databaseWorker = Executors.newSingleThreadExecutor()

        fun getAllPresetInfos() = databaseWorker.submit(Callable<List<PresetInfo>> { database.presetInfoDao().getAll() })
        fun insertPreset(presetInfo: PresetInfo) = databaseWorker.submit { database.presetInfoDao().insert(presetInfo) }
        fun deletePreset(presetInfo: PresetInfo) = databaseWorker.submit { database.presetInfoDao().delete(presetInfo) }
        fun nukePresets() = databaseWorker.submit { database.presetInfoDao().nukeTable() }

        fun getAllGameStates() = databaseWorker.submit(Callable<List<GameState>> { database.gameStateDao().getAll() })
        fun insertGameState(gameState: GameState) = databaseWorker.submit { database.gameStateDao().insert(gameState) }
        fun deleteGameState(gameState: GameState) = databaseWorker.submit { database.gameStateDao().delete(gameState) }
    }

    abstract fun gameStateDao(): GameStateDao
    abstract fun presetInfoDao(): PresetInfoDao
}
