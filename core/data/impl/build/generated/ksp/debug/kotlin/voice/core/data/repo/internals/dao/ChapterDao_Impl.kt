package voice.core.`data`.repo.internals.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.appendPlaceholders
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
import kotlin.text.StringBuilder
import voice.core.`data`.Chapter
import voice.core.`data`.ChapterId
import voice.core.`data`.MarkData
import voice.core.`data`.repo.internals.Converters

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ChapterDao_Impl(
  __db: RoomDatabase,
) : ChapterDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfChapter: EntityInsertAdapter<Chapter>

  private val __converters: Converters = Converters()
  init {
    this.__db = __db
    this.__insertAdapterOfChapter = object : EntityInsertAdapter<Chapter>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `chapters2` (`id`,`name`,`duration`,`fileLastModified`,`markData`) VALUES (?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Chapter) {
        val _tmp: String = __converters.fromChapterId(entity.id)
        statement.bindText(1, _tmp)
        val _tmpName: String? = entity.name
        if (_tmpName == null) {
          statement.bindNull(2)
        } else {
          statement.bindText(2, _tmpName)
        }
        statement.bindLong(3, entity.duration)
        val _tmp_1: String = __converters.fromInstant(entity.fileLastModified)
        statement.bindText(4, _tmp_1)
        val _tmp_2: String = __converters.fromMarks(entity.markData)
        statement.bindText(5, _tmp_2)
      }
    }
  }

  public override suspend fun insert(chapter: Chapter): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfChapter.insert(_connection, chapter)
  }

  public override suspend fun chapter(id: ChapterId): Chapter? {
    val _sql: String = "SELECT * FROM chapters2 WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: String = __converters.fromChapterId(id)
        _stmt.bindText(_argIndex, _tmp)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfFileLastModified: Int = getColumnIndexOrThrow(_stmt, "fileLastModified")
        val _columnIndexOfMarkData: Int = getColumnIndexOrThrow(_stmt, "markData")
        val _result: Chapter?
        if (_stmt.step()) {
          val _tmpId: ChapterId
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfId)
          _tmpId = __converters.toChapterId(_tmp_1)
          val _tmpName: String?
          if (_stmt.isNull(_columnIndexOfName)) {
            _tmpName = null
          } else {
            _tmpName = _stmt.getText(_columnIndexOfName)
          }
          val _tmpDuration: Long
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration)
          val _tmpFileLastModified: Instant
          val _tmp_2: String
          _tmp_2 = _stmt.getText(_columnIndexOfFileLastModified)
          _tmpFileLastModified = __converters.toInstant(_tmp_2)
          val _tmpMarkData: List<MarkData>
          val _tmp_3: String
          _tmp_3 = _stmt.getText(_columnIndexOfMarkData)
          _tmpMarkData = __converters.toMarks(_tmp_3)
          _result = Chapter(_tmpId,_tmpName,_tmpDuration,_tmpFileLastModified,_tmpMarkData)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun chapters(ids: List<ChapterId>): List<Chapter> {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT * FROM chapters2 WHERE id IN (")
    val _inputSize: Int = ids.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: ChapterId in ids) {
          val _tmp: String = __converters.fromChapterId(_item)
          _stmt.bindText(_argIndex, _tmp)
          _argIndex++
        }
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfFileLastModified: Int = getColumnIndexOrThrow(_stmt, "fileLastModified")
        val _columnIndexOfMarkData: Int = getColumnIndexOrThrow(_stmt, "markData")
        val _result: MutableList<Chapter> = mutableListOf()
        while (_stmt.step()) {
          val _item_1: Chapter
          val _tmpId: ChapterId
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfId)
          _tmpId = __converters.toChapterId(_tmp_1)
          val _tmpName: String?
          if (_stmt.isNull(_columnIndexOfName)) {
            _tmpName = null
          } else {
            _tmpName = _stmt.getText(_columnIndexOfName)
          }
          val _tmpDuration: Long
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration)
          val _tmpFileLastModified: Instant
          val _tmp_2: String
          _tmp_2 = _stmt.getText(_columnIndexOfFileLastModified)
          _tmpFileLastModified = __converters.toInstant(_tmp_2)
          val _tmpMarkData: List<MarkData>
          val _tmp_3: String
          _tmp_3 = _stmt.getText(_columnIndexOfMarkData)
          _tmpMarkData = __converters.toMarks(_tmp_3)
          _item_1 = Chapter(_tmpId,_tmpName,_tmpDuration,_tmpFileLastModified,_tmpMarkData)
          _result.add(_item_1)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
