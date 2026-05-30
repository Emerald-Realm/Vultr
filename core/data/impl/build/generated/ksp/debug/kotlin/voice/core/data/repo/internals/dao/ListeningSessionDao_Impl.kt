package voice.core.`data`.repo.internals.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import java.time.Instant
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import voice.core.`data`.BookId
import voice.core.`data`.ChapterId
import voice.core.`data`.ListeningSession
import voice.core.`data`.repo.internals.Converters

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ListeningSessionDao_Impl(
  __db: RoomDatabase,
) : ListeningSessionDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfListeningSession: EntityInsertAdapter<ListeningSession>

  private val __converters: Converters = Converters()
  init {
    this.__db = __db
    this.__insertAdapterOfListeningSession = object : EntityInsertAdapter<ListeningSession>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `listening_session` (`id`,`bookId`,`chapterId`,`action`,`positionInChapter`,`createdAt`,`listenedMs`) VALUES (?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ListeningSession) {
        val _tmp: String = __converters.fromListeningSessionId(entity.id)
        statement.bindText(1, _tmp)
        val _tmp_1: String = __converters.fromBookId(entity.bookId)
        statement.bindText(2, _tmp_1)
        val _tmp_2: String = __converters.fromChapterId(entity.chapterId)
        statement.bindText(3, _tmp_2)
        statement.bindText(4, entity.action)
        statement.bindLong(5, entity.positionInChapter)
        val _tmp_3: String = __converters.fromInstant(entity.createdAt)
        statement.bindText(6, _tmp_3)
        statement.bindLong(7, entity.listenedMs)
      }
    }
  }

  public override suspend fun insert(session: ListeningSession): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfListeningSession.insert(_connection, session)
  }

  public override suspend fun forBook(bookId: BookId): List<ListeningSession> {
    val _sql: String = "SELECT * FROM listening_session WHERE bookId = ? ORDER BY createdAt DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: String = __converters.fromBookId(bookId)
        _stmt.bindText(_argIndex, _tmp)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfBookId: Int = getColumnIndexOrThrow(_stmt, "bookId")
        val _columnIndexOfChapterId: Int = getColumnIndexOrThrow(_stmt, "chapterId")
        val _columnIndexOfAction: Int = getColumnIndexOrThrow(_stmt, "action")
        val _columnIndexOfPositionInChapter: Int = getColumnIndexOrThrow(_stmt, "positionInChapter")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfListenedMs: Int = getColumnIndexOrThrow(_stmt, "listenedMs")
        val _result: MutableList<ListeningSession> = mutableListOf()
        while (_stmt.step()) {
          val _item: ListeningSession
          val _tmpId: ListeningSession.Id
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfId)
          _tmpId = __converters.toListeningSessionId(_tmp_1)
          val _tmpBookId: BookId
          val _tmp_2: String
          _tmp_2 = _stmt.getText(_columnIndexOfBookId)
          _tmpBookId = __converters.toBookId(_tmp_2)
          val _tmpChapterId: ChapterId
          val _tmp_3: String
          _tmp_3 = _stmt.getText(_columnIndexOfChapterId)
          _tmpChapterId = __converters.toChapterId(_tmp_3)
          val _tmpAction: String
          _tmpAction = _stmt.getText(_columnIndexOfAction)
          val _tmpPositionInChapter: Long
          _tmpPositionInChapter = _stmt.getLong(_columnIndexOfPositionInChapter)
          val _tmpCreatedAt: Instant
          val _tmp_4: String
          _tmp_4 = _stmt.getText(_columnIndexOfCreatedAt)
          _tmpCreatedAt = __converters.toInstant(_tmp_4)
          val _tmpListenedMs: Long
          _tmpListenedMs = _stmt.getLong(_columnIndexOfListenedMs)
          _item = ListeningSession(_tmpId,_tmpBookId,_tmpChapterId,_tmpAction,_tmpPositionInChapter,_tmpCreatedAt,_tmpListenedMs)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun all(): List<ListeningSession> {
    val _sql: String = "SELECT * FROM listening_session ORDER BY createdAt DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfBookId: Int = getColumnIndexOrThrow(_stmt, "bookId")
        val _columnIndexOfChapterId: Int = getColumnIndexOrThrow(_stmt, "chapterId")
        val _columnIndexOfAction: Int = getColumnIndexOrThrow(_stmt, "action")
        val _columnIndexOfPositionInChapter: Int = getColumnIndexOrThrow(_stmt, "positionInChapter")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfListenedMs: Int = getColumnIndexOrThrow(_stmt, "listenedMs")
        val _result: MutableList<ListeningSession> = mutableListOf()
        while (_stmt.step()) {
          val _item: ListeningSession
          val _tmpId: ListeningSession.Id
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfId)
          _tmpId = __converters.toListeningSessionId(_tmp)
          val _tmpBookId: BookId
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfBookId)
          _tmpBookId = __converters.toBookId(_tmp_1)
          val _tmpChapterId: ChapterId
          val _tmp_2: String
          _tmp_2 = _stmt.getText(_columnIndexOfChapterId)
          _tmpChapterId = __converters.toChapterId(_tmp_2)
          val _tmpAction: String
          _tmpAction = _stmt.getText(_columnIndexOfAction)
          val _tmpPositionInChapter: Long
          _tmpPositionInChapter = _stmt.getLong(_columnIndexOfPositionInChapter)
          val _tmpCreatedAt: Instant
          val _tmp_3: String
          _tmp_3 = _stmt.getText(_columnIndexOfCreatedAt)
          _tmpCreatedAt = __converters.toInstant(_tmp_3)
          val _tmpListenedMs: Long
          _tmpListenedMs = _stmt.getLong(_columnIndexOfListenedMs)
          _item = ListeningSession(_tmpId,_tmpBookId,_tmpChapterId,_tmpAction,_tmpPositionInChapter,_tmpCreatedAt,_tmpListenedMs)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: ListeningSession.Id) {
    val _sql: String = "DELETE FROM listening_session WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: String = __converters.fromListeningSessionId(id)
        _stmt.bindText(_argIndex, _tmp)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
