package com.example.cockounter.core;

import android.database.Cursor;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.github.andrewoma.dexx.kollection.ImmutableMap;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class GameStateDao_Impl implements GameStateDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter __insertionAdapterOfGameState;

  private final GameStateConverter __gameStateConverter = new GameStateConverter();

  private final EntityDeletionOrUpdateAdapter __deletionAdapterOfGameState;

  public GameStateDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfGameState = new EntityInsertionAdapter<GameState>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `GameState`(`sharedParameters`,`roles`) VALUES (?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, GameState value) {
        final String _tmp;
        _tmp = __gameStateConverter.fromSharedParameters(value.getSharedParameters());
        if (_tmp == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, _tmp);
        }
        final String _tmp_1;
        _tmp_1 = __gameStateConverter.fromRoles(value.getRoles());
        if (_tmp_1 == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, _tmp_1);
        }
      }
    };
    this.__deletionAdapterOfGameState = new EntityDeletionOrUpdateAdapter<GameState>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `GameState` WHERE `sharedParameters` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, GameState value) {
        final String _tmp;
        _tmp = __gameStateConverter.fromSharedParameters(value.getSharedParameters());
        if (_tmp == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, _tmp);
        }
      }
    };
  }

  @Override
  public void insert(final GameState gameState) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfGameState.insert(gameState);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final GameState gameState) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfGameState.handle(gameState);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<GameState> getAll() {
    final String _sql = "SELECT * from gameState";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false);
    try {
      final int _cursorIndexOfSharedParameters = CursorUtil.getColumnIndexOrThrow(_cursor, "sharedParameters");
      final int _cursorIndexOfRoles = CursorUtil.getColumnIndexOrThrow(_cursor, "roles");
      final List<GameState> _result = new ArrayList<GameState>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final GameState _item;
        final ImmutableMap<String, GameParameter> _tmpSharedParameters;
        final String _tmp;
        _tmp = _cursor.getString(_cursorIndexOfSharedParameters);
        _tmpSharedParameters = __gameStateConverter.toSharedParameters(_tmp);
        final ImmutableMap<String, GameRole> _tmpRoles;
        final String _tmp_1;
        _tmp_1 = _cursor.getString(_cursorIndexOfRoles);
        _tmpRoles = __gameStateConverter.toRoles(_tmp_1);
        _item = new GameState(_tmpSharedParameters,_tmpRoles);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }
}
