package voice.core.`data`.repo.internals.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import java.time.Instant
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlin.text.StringBuilder
import voice.core.`data`.BookId
import voice.core.`data`.Bookmark
import voice.core.`data`.ChapterId
import voice.core.`data`.repo.internals.Converters

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class BookmarkDao_Impl(
  __db: RoomDatabase,
) : BookmarkDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfBookmark: EntityInsertAdapter<Bookmark>

  private val __converters: Converters = Converters()
  init {
    this.__db = __db
    this.__insertAdapterOfBookmark = object : EntityInsertAdapter<Bookmark>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `bookmark2` (`bookId`,`chapterId`,`title`,`time`,`addedAt`,`setBySleepTimer`,`id`) VALUES (?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Bookmark) {
        val _tmp: String = __converters.fromBookId(entity.bookId)
        statement.bindText(1, _tmp)
        val _tmp_1: String = __converters.fromChapterId(entity.chapterId)
        statement.bindText(2, _tmp_1)
        val _tmpTitle: String? = entity.title
        if (_tmpTitle == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpTitle)
        }
        statement.bindLong(4, entity.time)
        val _tmp_2: String = __converters.fromInstant(entity.addedAt)
        statement.bindText(5, _tmp_2)
        val _tmp_3: Int = if (entity.setBySleepTimer) 1 else 0
        statement.bindLong(6, _tmp_3.toLong())
        val _tmp_4: String = __converters.fromBookmarkId(entity.id)
        statement.bindText(7, _tmp_4)
      }
    }
  }

  public override suspend fun addBookmark(bookmark: Bookmark): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfBookmark.insert(_connection, bookmark)
  }

  public override suspend fun allForChapters(chapters: List<ChapterId>): List<Bookmark> {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT * FROM bookmark2 WHERE chapterId IN(")
    val _inputSize: Int = chapters.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: ChapterId in chapters) {
          val _tmp: String = __converters.fromChapterId(_item)
          _stmt.bindText(_argIndex, _tmp)
          _argIndex++
        }
        val _columnIndexOfBookId: Int = getColumnIndexOrThrow(_stmt, "bookId")
        val _columnIndexOfChapterId: Int = getColumnIndexOrThrow(_stmt, "chapterId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfTime: Int = getColumnIndexOrThrow(_stmt, "time")
        val _columnIndexOfAddedAt: Int = getColumnIndexOrThrow(_stmt, "addedAt")
        val _columnIndexOfSetBySleepTimer: Int = getColumnIndexOrThrow(_stmt, "setBySleepTimer")
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _result: MutableList<Bookmark> = mutableListOf()
        while (_stmt.step()) {
          val _item_1: Bookmark
          val _tmpBookId: BookId
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfBookId)
          _tmpBookId = __converters.toBookId(_tmp_1)
          val _tmpChapterId: ChapterId
          val _tmp_2: String
          _tmp_2 = _stmt.getText(_columnIndexOfChapterId)
          _tmpChapterId = __converters.toChapterId(_tmp_2)
          val _tmpTitle: String?
          if (_stmt.isNull(_columnIndexOfTitle)) {
            _tmpTitle = null
          } else {
            _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          }
          val _tmpTime: Long
          _tmpTime = _stmt.getLong(_columnIndexOfTime)
          val _tmpAddedAt: Instant
          val _tmp_3: String
          _tmp_3 = _stmt.getText(_columnIndexOfAddedAt)
          _tmpAddedAt = __converters.toInstant(_tmp_3)
          val _tmpSetBySleepTimer: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfSetBySleepTimer).toInt()
          _tmpSetBySleepTimer = _tmp_4 != 0
          val _tmpId: Bookmark.Id
          val _tmp_5: String
          _tmp_5 = _stmt.getText(_columnIndexOfId)
          _tmpId = __converters.toBookmarkId(_tmp_5)
          _item_1 = Bookmark(_tmpBookId,_tmpChapterId,_tmpTitle,_tmpTime,_tmpAddedAt,_tmpSetBySleepTimer,_tmpId)
          _result.add(_item_1)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteBookmark(id: Bookmark.Id) {
    val _sql: String = "DELETE FROM bookmark2 WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: String = __converters.fromBookmarkId(id)
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
