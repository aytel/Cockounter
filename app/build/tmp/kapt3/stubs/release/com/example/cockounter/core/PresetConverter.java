package com.example.cockounter.core;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 13}, bv = {1, 0, 3}, k = 1, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010$\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0006\u0018\u0000 \u00132\u00020\u0001:\u0001\u0013B\u0005\u00a2\u0006\u0002\u0010\u0002J\u001c\u0010\u0003\u001a\u00020\u00042\u0012\u0010\u0005\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00070\u0006H\u0007J\u001c\u0010\b\u001a\u00020\u00042\u0012\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\n0\u0006H\u0007J\u0016\u0010\u000b\u001a\u00020\u00042\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\rH\u0007J\u001c\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\u0010\u001a\u00020\u0004H\u0007J\u001c\u0010\u0011\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\n0\u00062\u0006\u0010\u0010\u001a\u00020\u0004H\u0007J\u0016\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u000e0\r2\u0006\u0010\u0010\u001a\u00020\u0004H\u0007\u00a8\u0006\u0014"}, d2 = {"Lcom/example/cockounter/core/PresetConverter;", "", "()V", "fromGlobalParameters", "", "globalParameters", "", "Lcom/example/cockounter/core/Parameter;", "fromRoles", "roles", "Lcom/example/cockounter/core/Role;", "fromScripts", "scripts", "", "Lcom/example/cockounter/core/Script;", "toGlobalParameters", "data", "toRoles", "toScripts", "Companion", "app_release"})
public final class PresetConverter {
    @org.jetbrains.annotations.NotNull()
    private static final com.google.gson.Gson gson = null;
    public static final com.example.cockounter.core.PresetConverter.Companion Companion = null;
    
    @org.jetbrains.annotations.NotNull()
    @androidx.room.TypeConverter()
    public final java.lang.String fromGlobalParameters(@org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, ? extends com.example.cockounter.core.Parameter> globalParameters) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @androidx.room.TypeConverter()
    public final java.lang.String fromRoles(@org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, com.example.cockounter.core.Role> roles) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @androidx.room.TypeConverter()
    public final java.lang.String fromScripts(@org.jetbrains.annotations.NotNull()
    java.util.List<com.example.cockounter.core.Script> scripts) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @androidx.room.TypeConverter()
    public final java.util.Map<java.lang.String, com.example.cockounter.core.Parameter> toGlobalParameters(@org.jetbrains.annotations.NotNull()
    java.lang.String data) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @androidx.room.TypeConverter()
    public final java.util.Map<java.lang.String, com.example.cockounter.core.Role> toRoles(@org.jetbrains.annotations.NotNull()
    java.lang.String data) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @androidx.room.TypeConverter()
    public final java.util.List<com.example.cockounter.core.Script> toScripts(@org.jetbrains.annotations.NotNull()
    java.lang.String data) {
        return null;
    }
    
    public PresetConverter() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 1, 13}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\b\u0086\u0003\u0018\u00002\u00020\u0001:\u0003\u0007\b\tB\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u0011\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\n"}, d2 = {"Lcom/example/cockounter/core/PresetConverter$Companion;", "", "()V", "gson", "Lcom/google/gson/Gson;", "getGson", "()Lcom/google/gson/Gson;", "Parameters", "Roles", "Scripts", "app_release"})
    public static final class Companion {
        
        @org.jetbrains.annotations.NotNull()
        public final com.google.gson.Gson getGson() {
            return null;
        }
        
        private Companion() {
            super();
        }
        
        @kotlin.Metadata(mv = {1, 1, 13}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u0019\u0012\u0012\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u0003\u00a2\u0006\u0002\u0010\u0006J\u0015\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u0003H\u00c6\u0003J\u001f\u0010\n\u001a\u00020\u00002\u0014\b\u0002\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u0003H\u00c6\u0001J\u0013\u0010\u000b\u001a\u00020\f2\b\u0010\r\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u000e\u001a\u00020\u000fH\u00d6\u0001J\t\u0010\u0010\u001a\u00020\u0004H\u00d6\u0001R\u001d\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\b\u00a8\u0006\u0011"}, d2 = {"Lcom/example/cockounter/core/PresetConverter$Companion$Parameters;", "", "parameters", "", "", "Lcom/example/cockounter/core/Parameter;", "(Ljava/util/Map;)V", "getParameters", "()Ljava/util/Map;", "component1", "copy", "equals", "", "other", "hashCode", "", "toString", "app_release"})
        public static final class Parameters {
            @org.jetbrains.annotations.NotNull()
            private final java.util.Map<java.lang.String, com.example.cockounter.core.Parameter> parameters = null;
            
            @org.jetbrains.annotations.NotNull()
            public final java.util.Map<java.lang.String, com.example.cockounter.core.Parameter> getParameters() {
                return null;
            }
            
            public Parameters(@org.jetbrains.annotations.NotNull()
            java.util.Map<java.lang.String, ? extends com.example.cockounter.core.Parameter> parameters) {
                super();
            }
            
            @org.jetbrains.annotations.NotNull()
            public final java.util.Map<java.lang.String, com.example.cockounter.core.Parameter> component1() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull()
            public final com.example.cockounter.core.PresetConverter.Companion.Parameters copy(@org.jetbrains.annotations.NotNull()
            java.util.Map<java.lang.String, ? extends com.example.cockounter.core.Parameter> parameters) {
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
        
        @kotlin.Metadata(mv = {1, 1, 13}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u0019\u0012\u0012\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u0003\u00a2\u0006\u0002\u0010\u0006J\u0015\u0010\t\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u0003H\u00c6\u0003J\u001f\u0010\n\u001a\u00020\u00002\u0014\b\u0002\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u0003H\u00c6\u0001J\u0013\u0010\u000b\u001a\u00020\f2\b\u0010\r\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u000e\u001a\u00020\u000fH\u00d6\u0001J\t\u0010\u0010\u001a\u00020\u0004H\u00d6\u0001R\u001d\u0010\u0002\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00050\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\b\u00a8\u0006\u0011"}, d2 = {"Lcom/example/cockounter/core/PresetConverter$Companion$Roles;", "", "roles", "", "", "Lcom/example/cockounter/core/Role;", "(Ljava/util/Map;)V", "getRoles", "()Ljava/util/Map;", "component1", "copy", "equals", "", "other", "hashCode", "", "toString", "app_release"})
        public static final class Roles {
            @org.jetbrains.annotations.NotNull()
            private final java.util.Map<java.lang.String, com.example.cockounter.core.Role> roles = null;
            
            @org.jetbrains.annotations.NotNull()
            public final java.util.Map<java.lang.String, com.example.cockounter.core.Role> getRoles() {
                return null;
            }
            
            public Roles(@org.jetbrains.annotations.NotNull()
            java.util.Map<java.lang.String, com.example.cockounter.core.Role> roles) {
                super();
            }
            
            @org.jetbrains.annotations.NotNull()
            public final java.util.Map<java.lang.String, com.example.cockounter.core.Role> component1() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull()
            public final com.example.cockounter.core.PresetConverter.Companion.Roles copy(@org.jetbrains.annotations.NotNull()
            java.util.Map<java.lang.String, com.example.cockounter.core.Role> roles) {
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
        
        @kotlin.Metadata(mv = {1, 1, 13}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\u0013\u0012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\u0002\u0010\u0005J\u000f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00c6\u0003J\u0019\u0010\t\u001a\u00020\u00002\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00c6\u0001J\u0013\u0010\n\u001a\u00020\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001R\u0017\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\u0011"}, d2 = {"Lcom/example/cockounter/core/PresetConverter$Companion$Scripts;", "", "scripts", "", "Lcom/example/cockounter/core/Script;", "(Ljava/util/List;)V", "getScripts", "()Ljava/util/List;", "component1", "copy", "equals", "", "other", "hashCode", "", "toString", "", "app_release"})
        public static final class Scripts {
            @org.jetbrains.annotations.NotNull()
            private final java.util.List<com.example.cockounter.core.Script> scripts = null;
            
            @org.jetbrains.annotations.NotNull()
            public final java.util.List<com.example.cockounter.core.Script> getScripts() {
                return null;
            }
            
            public Scripts(@org.jetbrains.annotations.NotNull()
            java.util.List<com.example.cockounter.core.Script> scripts) {
                super();
            }
            
            @org.jetbrains.annotations.NotNull()
            public final java.util.List<com.example.cockounter.core.Script> component1() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull()
            public final com.example.cockounter.core.PresetConverter.Companion.Scripts copy(@org.jetbrains.annotations.NotNull()
            java.util.List<com.example.cockounter.core.Script> scripts) {
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