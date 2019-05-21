package com.example.cockounter.storage;

import java.lang.System;

@androidx.room.Database(entities = {com.example.cockounter.core.GameState.class, com.example.cockounter.core.Preset.class}, version = 1)
@kotlin.Metadata(mv = {1, 1, 13}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\'\u0018\u0000 \u00072\u00020\u0001:\u0001\u0007B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&\u00a8\u0006\b"}, d2 = {"Lcom/example/cockounter/storage/Storage;", "Landroidx/room/RoomDatabase;", "()V", "gameStateDao", "Lcom/example/cockounter/core/GameStateDao;", "presetDao", "Lcom/example/cockounter/core/PresetDao;", "Companion", "app_debug"})
public abstract class Storage extends androidx.room.RoomDatabase {
    @org.jetbrains.annotations.NotNull()
    private static final com.example.cockounter.MainActivity.DatabaseDelegate database$delegate = null;
    public static final com.example.cockounter.storage.Storage.Companion Companion = null;
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.example.cockounter.core.GameStateDao gameStateDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.example.cockounter.core.PresetDao presetDao();
    
    public Storage() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 1, 13}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u001b\u0010\u0003\u001a\u00020\u00048FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0007\u0010\b\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\t"}, d2 = {"Lcom/example/cockounter/storage/Storage$Companion;", "", "()V", "database", "Lcom/example/cockounter/storage/Storage;", "getDatabase", "()Lcom/example/cockounter/storage/Storage;", "database$delegate", "Lcom/example/cockounter/MainActivity$DatabaseDelegate;", "app_debug"})
    public static final class Companion {
        
        @org.jetbrains.annotations.NotNull()
        public final com.example.cockounter.storage.Storage getDatabase() {
            return null;
        }
        
        private Companion() {
            super();
        }
    }
}