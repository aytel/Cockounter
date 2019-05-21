package com.example.cockounter.core;

import android.database.Cursor;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "deprecation"})
public final class PresetDao_Impl implements PresetDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter __insertionAdapterOfPreset;

  private final PresetConverter __presetConverter = new PresetConverter();

  private final EntityDeletionOrUpdateAdapter __deletionAdapterOfPreset;

  public PresetDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPreset = new EntityInsertionAdapter<Preset>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `Preset`(`name`,`globalParameters`,`roles`,`globalScripts`) VALUES (?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Preset value) {
        if (value.getName() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getName());
        }
        final String _tmp;
        _tmp = __presetConverter.fromGlobalParameters(value.getGlobalParameters());
        if (_tmp == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, _tmp);
        }
        final String _tmp_1;
        _tmp_1 = __presetConverter.fromRoles(value.getRoles());
        if (_tmp_1 == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, _tmp_1);
        }
        final String _tmp_2;
        _tmp_2 = __presetConverter.fromScripts(value.getGlobalScripts());
        if (_tmp_2 == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, _tmp_2);
        }
      }
    };
    this.__deletionAdapterOfPreset = new EntityDeletionOrUpdateAdapter<Preset>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `Preset` WHERE `name` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Preset value) {
        if (value.getName() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getName());
        }
      }
    };
  }

  @Override
  public void insert(final Preset preset) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfPreset.insert(preset);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final Preset preset) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfPreset.handle(preset);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<Preset> getAll() {
    final String _sql = "SELECT * from preset";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false);
    try {
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfGlobalParameters = CursorUtil.getColumnIndexOrThrow(_cursor, "globalParameters");
      final int _cursorIndexOfRoles = CursorUtil.getColumnIndexOrThrow(_cursor, "roles");
      final int _cursorIndexOfGlobalScripts = CursorUtil.getColumnIndexOrThrow(_cursor, "globalScripts");
      final List<Preset> _result = new ArrayList<Preset>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final Preset _item;
        final String _tmpName;
        _tmpName = _cursor.getString(_cursorIndexOfName);
        final Map<String, Parameter> _tmpGlobalParameters;
        final String _tmp;
        _tmp = _cursor.getString(_cursorIndexOfGlobalParameters);
        _tmpGlobalParameters = __presetConverter.toGlobalParameters(_tmp);
        final Map<String, Role> _tmpRoles;
        final String _tmp_1;
        _tmp_1 = _cursor.getString(_cursorIndexOfRoles);
        _tmpRoles = __presetConverter.toRoles(_tmp_1);
        final List<Script> _tmpGlobalScripts;
        final String _tmp_2;
        _tmp_2 = _cursor.getString(_cursorIndexOfGlobalScripts);
        _tmpGlobalScripts = __presetConverter.toScripts(_tmp_2);
        _item = new Preset(_tmpName,_tmpGlobalParameters,_tmpRoles,_tmpGlobalScripts);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }
}
