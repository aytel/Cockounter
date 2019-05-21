package com.example.cockounter.core;

import java.lang.System;

@androidx.room.TypeConverters(value = {com.example.cockounter.core.GameStateConverter.class})
@androidx.room.Entity()
@kotlin.Metadata(mv = {1, 1, 13}, bv = {1, 0, 3}, k = 1, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001B-\u0012\u0012\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u0003\u0012\u0012\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00070\u0003\u00a2\u0006\u0002\u0010\bJ\u0015\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u0003H\u00c6\u0003J\u0015\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00070\u0003H\u00c6\u0003J5\u0010\u000e\u001a\u00020\u00002\u0014\b\u0002\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u00032\u0014\b\u0002\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00070\u0003H\u00c6\u0001J\u0013\u0010\u000f\u001a\u00020\u00102\b\u0010\u0011\u001a\u0004\u0018\u00010\u0012H\u00d6\u0003J\t\u0010\u0013\u001a\u00020\u0014H\u00d6\u0001J\t\u0010\u0015\u001a\u00020\u0004H\u00d6\u0001R\u001d\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00070\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\"\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\n\u00a8\u0006\u0016"}, d2 = {"Lcom/example/cockounter/core/GameState;", "Ljava/io/Serializable;", "sharedParameters", "Lcom/github/andrewoma/dexx/kollection/ImmutableMap;", "", "Lcom/example/cockounter/core/GameParameter;", "roles", "Lcom/example/cockounter/core/GameRole;", "(Lcom/github/andrewoma/dexx/kollection/ImmutableMap;Lcom/github/andrewoma/dexx/kollection/ImmutableMap;)V", "getRoles", "()Lcom/github/andrewoma/dexx/kollection/ImmutableMap;", "getSharedParameters", "component1", "component2", "copy", "equals", "", "other", "", "hashCode", "", "toString", "app_release"})
public final class GameState implements java.io.Serializable {
    @org.jetbrains.annotations.NotNull()
    @androidx.room.PrimaryKey()
    private final com.github.andrewoma.dexx.kollection.ImmutableMap<java.lang.String, com.example.cockounter.core.GameParameter> sharedParameters = null;
    @org.jetbrains.annotations.NotNull()
    private final com.github.andrewoma.dexx.kollection.ImmutableMap<java.lang.String, com.example.cockounter.core.GameRole> roles = null;
    
    @org.jetbrains.annotations.NotNull()
    public final com.github.andrewoma.dexx.kollection.ImmutableMap<java.lang.String, com.example.cockounter.core.GameParameter> getSharedParameters() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.github.andrewoma.dexx.kollection.ImmutableMap<java.lang.String, com.example.cockounter.core.GameRole> getRoles() {
        return null;
    }
    
    public GameState(@org.jetbrains.annotations.NotNull()
    com.github.andrewoma.dexx.kollection.ImmutableMap<java.lang.String, ? extends com.example.cockounter.core.GameParameter> sharedParameters, @org.jetbrains.annotations.NotNull()
    com.github.andrewoma.dexx.kollection.ImmutableMap<java.lang.String, com.example.cockounter.core.GameRole> roles) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.github.andrewoma.dexx.kollection.ImmutableMap<java.lang.String, com.example.cockounter.core.GameParameter> component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.github.andrewoma.dexx.kollection.ImmutableMap<java.lang.String, com.example.cockounter.core.GameRole> component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.cockounter.core.GameState copy(@org.jetbrains.annotations.NotNull()
    com.github.andrewoma.dexx.kollection.ImmutableMap<java.lang.String, ? extends com.example.cockounter.core.GameParameter> sharedParameters, @org.jetbrains.annotations.NotNull()
    com.github.andrewoma.dexx.kollection.ImmutableMap<java.lang.String, com.example.cockounter.core.GameRole> roles) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String toString() {
        return null;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object p0) {
        return false;
    }
}