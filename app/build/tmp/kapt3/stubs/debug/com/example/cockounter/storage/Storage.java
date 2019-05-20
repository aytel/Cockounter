package com.example.cockounter.storage;

import java.lang.System;

@androidx.room.Database(entities = {com.example.cockounter.core.GameState.class, com.example.cockounter.core.Preset.class}, version = 1)
@kotlin.Metadata(mv = {1, 1, 13}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&\u00a8\u0006\u0007"}, d2 = {"Lcom/example/cockounter/storage/Storage;", "Landroidx/room/RoomDatabase;", "()V", "gameStateDao", "Lcom/example/cockounter/core/GameStateDao;", "presetDao", "Lcom/example/cockounter/core/PresetDao;", "app_debug"})
public abstract class Storage extends androidx.room.RoomDatabase {
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.example.cockounter.core.GameStateDao gameStateDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.example.cockounter.core.PresetDao presetDao();
    
    public Storage() {
        super();
    }
}