package com.example.cockounter.core;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 13}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u0000 \u000e2\u00020\u0001:\u0001\u000eB\u0005\u00a2\u0006\u0002\u0010\u0002J\u001c\u0010\u0003\u001a\u00020\u00042\u0012\u0010\u0005\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00070\u0006H\u0007J\u001c\u0010\b\u001a\u00020\u00042\u0012\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\n0\u0006H\u0007J\u001c\u0010\u000b\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\f\u001a\u00020\u0004H\u0007J\u001c\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\n0\u00062\u0006\u0010\f\u001a\u00020\u0004H\u0007\u00a8\u0006\u000f"}, d2 = {"Lcom/example/cockounter/core/GameStateConverter;", "", "()V", "fromRoles", "", "roles", "Lcom/github/andrewoma/dexx/kollection/ImmutableMap;", "Lcom/example/cockounter/core/GameRole;", "fromSharedParameters", "sharedParameters", "Lcom/example/cockounter/core/GameParameter;", "toRoles", "data", "toSharedParameters", "Companion", "app_debug"})
public final class GameStateConverter {
    @org.jetbrains.annotations.NotNull()
    private static final com.google.gson.Gson gson = null;
    public static final com.example.cockounter.core.GameStateConverter.Companion Companion = null;
    
    @org.jetbrains.annotations.NotNull()
    @androidx.room.TypeConverter()
    public final java.lang.String fromSharedParameters(@org.jetbrains.annotations.NotNull()
    com.github.andrewoma.dexx.kollection.ImmutableMap<java.lang.String, ? extends com.example.cockounter.core.GameParameter> sharedParameters) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @androidx.room.TypeConverter()
    public final java.lang.String fromRoles(@org.jetbrains.annotations.NotNull()
    com.github.andrewoma.dexx.kollection.ImmutableMap<java.lang.String, com.example.cockounter.core.GameRole> roles) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @androidx.room.TypeConverter()
    public final com.github.andrewoma.dexx.kollection.ImmutableMap<java.lang.String, com.example.cockounter.core.GameParameter> toSharedParameters(@org.jetbrains.annotations.NotNull()
    java.lang.String data) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @androidx.room.TypeConverter()
    public final com.github.andrewoma.dexx.kollection.ImmutableMap<java.lang.String, com.example.cockounter.core.GameRole> toRoles(@org.jetbrains.annotations.NotNull()
    java.lang.String data) {
        return null;
    }
    
    public GameStateConverter() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 1, 13}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0086\u0003\u0018\u00002\u00020\u0001:\u0002\u0007\bB\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0011\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\t"}, d2 = {"Lcom/example/cockounter/core/GameStateConverter$Companion;", "", "()V", "gson", "Lcom/google/gson/Gson;", "getGson", "()Lcom/google/gson/Gson;", "Parameters", "Roles", "app_debug"})
    public static final class Companion {
        
        @org.jetbrains.annotations.NotNull()
        public final com.google.gson.Gson getGson() {
            return null;
        }
        
        private Companion() {
            super();
        }
        
        @kotlin.Metadata(mv = {1, 1, 13}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u0019\u0012\u0012\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u0003\u00a2\u0006\u0002\u0010\u0006J\u0015\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u0003H\u00c6\u0003J\u001f\u0010\n\u001a\u00020\u00002\u0014\b\u0002\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u0003H\u00c6\u0001J\u0013\u0010\u000b\u001a\u00020\f2\b\u0010\r\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u000e\u001a\u00020\u000fH\u00d6\u0001J\t\u0010\u0010\u001a\u00020\u0004H\u00d6\u0001R\u001d\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\b\u00a8\u0006\u0011"}, d2 = {"Lcom/example/cockounter/core/GameStateConverter$Companion$Parameters;", "", "parameters", "Lcom/github/andrewoma/dexx/kollection/ImmutableMap;", "", "Lcom/example/cockounter/core/GameParameter;", "(Lcom/github/andrewoma/dexx/kollection/ImmutableMap;)V", "getParameters", "()Lcom/github/andrewoma/dexx/kollection/ImmutableMap;", "component1", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
        public static final class Parameters {
            @org.jetbrains.annotations.NotNull()
            private final com.github.andrewoma.dexx.kollection.ImmutableMap<java.lang.String, com.example.cockounter.core.GameParameter> parameters = null;
            
            @org.jetbrains.annotations.NotNull()
            public final com.github.andrewoma.dexx.kollection.ImmutableMap<java.lang.String, com.example.cockounter.core.GameParameter> getParameters() {
                return null;
            }
            
            public Parameters(@org.jetbrains.annotations.NotNull()
            com.github.andrewoma.dexx.kollection.ImmutableMap<java.lang.String, ? extends com.example.cockounter.core.GameParameter> parameters) {
                super();
            }
            
            @org.jetbrains.annotations.NotNull()
            public final com.github.andrewoma.dexx.kollection.ImmutableMap<java.lang.String, com.example.cockounter.core.GameParameter> component1() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull()
            public final com.example.cockounter.core.GameStateConverter.Companion.Parameters copy(@org.jetbrains.annotations.NotNull()
            com.github.andrewoma.dexx.kollection.ImmutableMap<java.lang.String, ? extends com.example.cockounter.core.GameParameter> parameters) {
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
        
        @kotlin.Metadata(mv = {1, 1, 13}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u0019\u0012\u0012\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u0003\u00a2\u0006\u0002\u0010\u0006J\u0015\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u0003H\u00c6\u0003J\u001f\u0010\n\u001a\u00020\u00002\u0014\b\u0002\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u0003H\u00c6\u0001J\u0013\u0010\u000b\u001a\u00020\f2\b\u0010\r\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u000e\u001a\u00020\u000fH\u00d6\u0001J\t\u0010\u0010\u001a\u00020\u0004H\u00d6\u0001R\u001d\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\b\u00a8\u0006\u0011"}, d2 = {"Lcom/example/cockounter/core/GameStateConverter$Companion$Roles;", "", "roles", "Lcom/github/andrewoma/dexx/kollection/ImmutableMap;", "", "Lcom/example/cockounter/core/GameRole;", "(Lcom/github/andrewoma/dexx/kollection/ImmutableMap;)V", "getRoles", "()Lcom/github/andrewoma/dexx/kollection/ImmutableMap;", "component1", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
        public static final class Roles {
            @org.jetbrains.annotations.NotNull()
            private final com.github.andrewoma.dexx.kollection.ImmutableMap<java.lang.String, com.example.cockounter.core.GameRole> roles = null;
            
            @org.jetbrains.annotations.NotNull()
            public final com.github.andrewoma.dexx.kollection.ImmutableMap<java.lang.String, com.example.cockounter.core.GameRole> getRoles() {
                return null;
            }
            
            public Roles(@org.jetbrains.annotations.NotNull()
            com.github.andrewoma.dexx.kollection.ImmutableMap<java.lang.String, com.example.cockounter.core.GameRole> roles) {
                super();
            }
            
            @org.jetbrains.annotations.NotNull()
            public final com.github.andrewoma.dexx.kollection.ImmutableMap<java.lang.String, com.example.cockounter.core.GameRole> component1() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull()
            public final com.example.cockounter.core.GameStateConverter.Companion.Roles copy(@org.jetbrains.annotations.NotNull()
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
    }
}