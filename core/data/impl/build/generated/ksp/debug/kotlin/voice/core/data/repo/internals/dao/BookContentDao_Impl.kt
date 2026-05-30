package voice.core.`data`.repo.internals.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import java.io.File
import java.time.Instant
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import voice.core.`data`.BookContent
import voice.core.`data`.BookId
import voice.core.`data`.ChapterId
import voice.core.`data`.repo.internals.Converters

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class BookContentDao_Impl(
  __db: RoomDatabase,
) : BookContentDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfBookContent: EntityInsertAdapter<BookContent>

  private val __converters: Converters = Converters()
  init {
    this.__db = __db
    this.__insertAdapterOfBookContent = object : EntityInsertAdapter<BookContent>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `content2` (`id`,`playbackSpeed`,`skipSilence`,`isActive`,`lastPlayedAt`,`author`,`name`,`addedAt`,`chapters`,`currentChapter`,`positionInChapter`,`cover`,`gain`,`genre`,`narrator`,`series`,`part`,`description`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: BookContent) {
        val _tmp: String = __converters.fromBookId(entity.id)
        statement.bindText(1, _tmp)
        statement.bindDouble(2, entity.playbackSpeed.toDouble())
        val _tmp_1: Int = if (entity.skipSilence) 1 else 0
        statement.bindLong(3, _tmp_1.toLong())
        val _tmp_2: Int = if (entity.isActive) 1 else 0
        statement.bindLong(4, _tmp_2.toLong())
        val _tmp_3: String = __converters.fromInstant(entity.lastPlayedAt)
        statement.bindText(5, _tmp_3)
        val _tmpAuthor: String? = entity.author
        if (_tmpAuthor == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpAuthor)
        }
        statement.bindText(7, entity.name)
        val _tmp_4: String = __converters.fromInstant(entity.addedAt)
        statement.bindText(8, _tmp_4)
        val _tmp_5: String = __converters.fromChapterList(entity.chapters)
        statement.bindText(9, _tmp_5)
        val _tmp_6: String = __converters.fromChapterId(entity.currentChapter)
        statement.bindText(10, _tmp_6)
        statement.bindLong(11, entity.positionInChapter)
        val _tmpCover: File? = entity.cover
        val _tmp_7: String?
        if (_tmpCover == null) {
          _tmp_7 = null
        } else {
          _tmp_7 = __converters.fromFile(_tmpCover)
        }
        if (_tmp_7 == null) {
          statement.bindNull(12)
        } else {
          statement.bindText(12, _tmp_7)
        }
        statement.bindDouble(13, entity.gain.toDouble())
        val _tmpGenre: String? = entity.genre
        if (_tmpGenre == null) {
          statement.bindNull(14)
        } else {
          statement.bindText(14, _tmpGenre)
        }
        val _tmpNarrator: String? = entity.narrator
        if (_tmpNarrator == null) {
          statement.bindNull(15)
        } else {
          statement.bindText(15, _tmpNarrator)
        }
        val _tmpSeries: String? = entity.series
        if (_tmpSeries == null) {
          statement.bindNull(16)
        } else {
          statement.bindText(16, _tmpSeries)
        }
        val _tmpPart: String? = entity.part
        if (_tmpPart == null) {
          statement.bindNull(17)
        } else {
          statement.bindText(17, _tmpPart)
        }
        val _tmpDescription: String? = entity.description
        if (_tmpDescription == null) {
          statement.bindNull(18)
        } else {
          statement.bindText(18, _tmpDescription)
        }
      }
    }
  }

  public override suspend fun insert(content: BookContent): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfBookContent.insert(_connection, content)
  }

  public override suspend fun all(): List<BookContent> {
    val _sql: String = "SELECT * FROM content2"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPlaybackSpeed: Int = getColumnIndexOrThrow(_stmt, "playbackSpeed")
        val _columnIndexOfSkipSilence: Int = getColumnIndexOrThrow(_stmt, "skipSilence")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfLastPlayedAt: Int = getColumnIndexOrThrow(_stmt, "lastPlayedAt")
        val _columnIndexOfAuthor: Int = getColumnIndexOrThrow(_stmt, "author")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfAddedAt: Int = getColumnIndexOrThrow(_stmt, "addedAt")
        val _columnIndexOfChapters: Int = getColumnIndexOrThrow(_stmt, "chapters")
        val _columnIndexOfCurrentChapter: Int = getColumnIndexOrThrow(_stmt, "currentChapter")
        val _columnIndexOfPositionInChapter: Int = getColumnIndexOrThrow(_stmt, "positionInChapter")
        val _columnIndexOfCover: Int = getColumnIndexOrThrow(_stmt, "cover")
        val _columnIndexOfGain: Int = getColumnIndexOrThrow(_stmt, "gain")
        val _columnIndexOfGenre: Int = getColumnIndexOrThrow(_stmt, "genre")
        val _columnIndexOfNarrator: Int = getColumnIndexOrThrow(_stmt, "narrator")
        val _columnIndexOfSeries: Int = getColumnIndexOrThrow(_stmt, "series")
        val _columnIndexOfPart: Int = getColumnIndexOrThrow(_stmt, "part")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _result: MutableList<BookContent> = mutableListOf()
        while (_stmt.step()) {
          val _item: BookContent
          val _tmpId: BookId
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfId)
          _tmpId = __converters.toBookId(_tmp)
          val _tmpPlaybackSpeed: Float
          _tmpPlaybackSpeed = _stmt.getDouble(_columnIndexOfPlaybackSpeed).toFloat()
          val _tmpSkipSilence: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfSkipSilence).toInt()
          _tmpSkipSilence = _tmp_1 != 0
          val _tmpIsActive: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp_2 != 0
          val _tmpLastPlayedAt: Instant
          val _tmp_3: String
          _tmp_3 = _stmt.getText(_columnIndexOfLastPlayedAt)
          _tmpLastPlayedAt = __converters.toInstant(_tmp_3)
          val _tmpAuthor: String?
          if (_stmt.isNull(_columnIndexOfAuthor)) {
            _tmpAuthor = null
          } else {
            _tmpAuthor = _stmt.getText(_columnIndexOfAuthor)
          }
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpAddedAt: Instant
          val _tmp_4: String
          _tmp_4 = _stmt.getText(_columnIndexOfAddedAt)
          _tmpAddedAt = __converters.toInstant(_tmp_4)
          val _tmpChapters: List<ChapterId>
          val _tmp_5: String
          _tmp_5 = _stmt.getText(_columnIndexOfChapters)
          _tmpChapters = __converters.toChapterList(_tmp_5)
          val _tmpCurrentChapter: ChapterId
          val _tmp_6: String
          _tmp_6 = _stmt.getText(_columnIndexOfCurrentChapter)
          _tmpCurrentChapter = __converters.toChapterId(_tmp_6)
          val _tmpPositionInChapter: Long
          _tmpPositionInChapter = _stmt.getLong(_columnIndexOfPositionInChapter)
          val _tmpCover: File?
          val _tmp_7: String?
          if (_stmt.isNull(_columnIndexOfCover)) {
            _tmp_7 = null
          } else {
            _tmp_7 = _stmt.getText(_columnIndexOfCover)
          }
          if (_tmp_7 == null) {
            _tmpCover = null
          } else {
            _tmpCover = __converters.toFile(_tmp_7)
          }
          val _tmpGain: Float
          _tmpGain = _stmt.getDouble(_columnIndexOfGain).toFloat()
          val _tmpGenre: String?
          if (_stmt.isNull(_columnIndexOfGenre)) {
            _tmpGenre = null
          } else {
            _tmpGenre = _stmt.getText(_columnIndexOfGenre)
          }
          val _tmpNarrator: String?
          if (_stmt.isNull(_columnIndexOfNarrator)) {
            _tmpNarrator = null
          } else {
            _tmpNarrator = _stmt.getText(_columnIndexOfNarrator)
          }
          val _tmpSeries: String?
          if (_stmt.isNull(_columnIndexOfSeries)) {
            _tmpSeries = null
          } else {
            _tmpSeries = _stmt.getText(_columnIndexOfSeries)
          }
          val _tmpPart: String?
          if (_stmt.isNull(_columnIndexOfPart)) {
            _tmpPart = null
          } else {
            _tmpPart = _stmt.getText(_columnIndexOfPart)
          }
          val _tmpDescription: String?
          if (_stmt.isNull(_columnIndexOfDescription)) {
            _tmpDescription = null
          } else {
            _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          }
          _item = BookContent(_tmpId,_tmpPlaybackSpeed,_tmpSkipSilence,_tmpIsActive,_tmpLastPlayedAt,_tmpAuthor,_tmpName,_tmpAddedAt,_tmpChapters,_tmpCurrentChapter,_tmpPositionInChapter,_tmpCover,_tmpGain,_tmpGenre,_tmpNarrator,_tmpSeries,_tmpPart,_tmpDescription)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun search(query: String): List<BookId> {
    val _sql: String = """
        |
        |  SELECT id
        |  FROM bookSearchFts
        |  WHERE bookSearchFts MATCH ?
        |  AND isActive = 1
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, query)
        val _result: MutableList<BookId> = mutableListOf()
        while (_stmt.step()) {
          val _item: BookId
          val _tmp: String
          _tmp = _stmt.getText(0)
          _item = __converters.toBookId(_tmp)
          _result.add(_item)
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
