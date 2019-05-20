package com.example.cockounter.storage;

import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomOpenHelper;
import androidx.room.RoomOpenHelper.Delegate;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.room.util.TableInfo.Column;
import androidx.room.util.TableInfo.ForeignKey;
import androidx.room.util.TableInfo.Index;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Callback;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration;
import com.example.cockounter.core.GameStateDao;
import com.example.cockounter.core.GameStateDao_Impl;
import com.example.cockounter.core.PresetDao;
import com.example.cockounter.core.PresetDao_Impl;
import java.lang.IllegalStateException;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings({"unchecked", "deprecation"})
public final class Storage_Impl extends Storage {
  private volatile GameStateDao _gameStateDao;

  private volatile PresetDao _presetDao;

  @Override
  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `GameState` (`sharedParameters` TEXT NOT NULL, `roles` TEXT NOT NULL, PRIMARY KEY(`sharedParameters`))");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `Preset` (`name` TEXT NOT NULL, `globalParameters` TEXT NOT NULL, `roles` TEXT NOT NULL, `globalScripts` TEXT NOT NULL)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, \"cdfdf11dcf8cb64cee142ccacbf92d34\")");
      }

      @Override
      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `GameState`");
        _db.execSQL("DROP TABLE IF EXISTS `Preset`");
      }

      @Override
      protected void onCreate(SupportSQLiteDatabase _db) {
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onCreate(_db);
          }
        }
      }

      @Override
      public void onOpen(SupportSQLiteDatabase _db) {
        mDatabase = _db;
        internalInitInvalidationTracker(_db);
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onOpen(_db);
          }
        }
      }

      @Override
      public void onPreMigrate(SupportSQLiteDatabase _db) {
        DBUtil.dropFtsSyncTriggers(_db);
      }

      @Override
      public void onPostMigrate(SupportSQLiteDatabase _db) {
      }

      @Override
      protected void validateMigration(SupportSQLiteDatabase _db) {
        final HashMap<String, TableInfo.Column> _columnsGameState = new HashMap<String, TableInfo.Column>(2);
        _columnsGameState.put("sharedParameters", new TableInfo.Column("sharedParameters", "TEXT", true, 1));
        _columnsGameState.put("roles", new TableInfo.Column("roles", "TEXT", true, 0));
        final HashSet<TableInfo.ForeignKey> _foreignKeysGameState = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesGameState = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoGameState = new TableInfo("GameState", _columnsGameState, _foreignKeysGameState, _indicesGameState);
        final TableInfo _existingGameState = TableInfo.read(_db, "GameState");
        if (! _infoGameState.equals(_existingGameState)) {
          throw new IllegalStateException("Migration didn't properly handle GameState(com.example.cockounter.core.GameState).\n"
                  + " Expected:\n" + _infoGameState + "\n"
                  + " Found:\n" + _existingGameState);
        }
        final HashMap<String, TableInfo.Column> _columnsPreset = new HashMap<String, TableInfo.Column>(4);
        _columnsPreset.put("name", new TableInfo.Column("name", "TEXT", true, 0));
        _columnsPreset.put("globalParameters", new TableInfo.Column("globalParameters", "TEXT", true, 0));
        _columnsPreset.put("roles", new TableInfo.Column("roles", "TEXT", true, 0));
        _columnsPreset.put("globalScripts", new TableInfo.Column("globalScripts", "TEXT", true, 0));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPreset = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPreset = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPreset = new TableInfo("Preset", _columnsPreset, _foreignKeysPreset, _indicesPreset);
        final TableInfo _existingPreset = TableInfo.read(_db, "Preset");
        if (! _infoPreset.equals(_existingPreset)) {
          throw new IllegalStateException("Migration didn't properly handle Preset(com.example.cockounter.core.Preset).\n"
                  + " Expected:\n" + _infoPreset + "\n"
                  + " Found:\n" + _existingPreset);
        }
      }
    }, "cdfdf11dcf8cb64cee142ccacbf92d34", "595f61154929e674578a1a0a28ab7aad");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
        .name(configuration.name)
        .callback(_openCallback)
        .build();
    final SupportSQLiteOpenHelper _helper = configuration.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "GameState","Preset");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `GameState`");
      _db.execSQL("DELETE FROM `Preset`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  public GameStateDao gameStateDao() {
    if (_gameStateDao != null) {
      return _gameStateDao;
    } else {
      synchronized(this) {
        if(_gameStateDao == null) {
          _gameStateDao = new GameStateDao_Impl(this);
        }
        return _gameStateDao;
      }
    }
  }

  @Override
  public PresetDao presetDao() {
    if (_presetDao != null) {
      return _presetDao;
    } else {
      synchronized(this) {
        if(_presetDao == null) {
          _presetDao = new PresetDao_Impl(this);
        }
        return _presetDao;
      }
    }
  }
}
