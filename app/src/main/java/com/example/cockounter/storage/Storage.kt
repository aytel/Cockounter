package com.example.cockounter.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.cockounter.core.GameState
import com.example.cockounter.core.GameStateDao
import com.example.cockounter.core.Preset
import com.example.cockounter.core.PresetDao
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future


@Database(entities = [GameState::class, Preset::class], version = 1)
abstract class Storage: RoomDatabase() {
    companion object {
        private lateinit var database: Storage
        private val databaseWorker = Executors.newSingleThreadExecutor()

        fun getAllPresets() = databaseWorker.submit ( Callable<List<Preset>> { database.presetDao().getAll() } )
        fun insertPreset(preset: Preset) = databaseWorker.submit { database.presetDao().insert(preset) }
        fun deletePreset(preset: Preset) = databaseWorker.submit { database.presetDao().delete(preset) }

        fun getAllGameStates() = databaseWorker.submit ( Callable<List<GameState>> { database.gameStateDao().getAll() } )
        fun insertGameState(gameState: GameState) = databaseWorker.submit { database.gameStateDao().insert(gameState) }
        fun deleteGameState(gameState: GameState) = databaseWorker.submit { database.gameStateDao().delete(gameState) }
    }

    abstract fun gameStateDao(): GameStateDao
    abstract fun presetDao(): PresetDao
}
