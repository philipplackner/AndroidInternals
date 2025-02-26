package com.plcoding.androidinternals

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import androidx.room.Room
import com.plcoding.androidinternals.db.DictionaryDao
import com.plcoding.androidinternals.db.DictionaryDatabase
import com.plcoding.androidinternals.db.DictionaryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.csv.CSVFormat

class DictionaryProvider: ContentProvider() {

    companion object {
        private const val AUTHORITY = "com.plcoding.androidinternals"
        private const val WORDS = 100

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "words", WORDS)
        }
    }

    private lateinit var dictionaryDao: DictionaryDao
    private val applicationScope by lazy {
        (context?.applicationContext as? DictionaryApp)?.applicationScope!!
    }

    override fun onCreate(): Boolean {
        dictionaryDao = Room.databaseBuilder(
            context?.applicationContext!!,
            DictionaryDatabase::class.java,
            "dictionary.db"
        ).build().dao

        applicationScope.launch {
            prepopulateDb()
        }

        return true
    }

    private suspend fun parseCsvFile() = withContext(Dispatchers.IO) {
        try {
            context
                ?.applicationContext!!
                .assets
                .open("english-dict.csv")
                .use { inputStream ->
                    val records = CSVFormat.DEFAULT.parse(inputStream.bufferedReader())

                    records
                        .toList()
                        .drop(1)
                        .mapNotNull { record ->
                            val word = record.get(0)
                            val definition = record.get(2)
                            DictionaryEntity(
                                word = word ?: return@mapNotNull null,
                                definition = definition ?: return@mapNotNull null
                            )
                        }
                }
        } catch (e: Exception) {
            coroutineContext.ensureActive()
            e.printStackTrace()
            emptyList<DictionaryEntity>()
        }
    }

    private suspend fun prepopulateDb() {
        if(dictionaryDao.getCount() == 0L) {
            val words = parseCsvFile()
            dictionaryDao.insertAll(words)
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        return when(uriMatcher.match(uri)) {
            WORDS -> {
                selectionArgs?.getOrNull(0)?.let { query ->
                    dictionaryDao.findByWord(query)
                } ?: dictionaryDao.getAll()
            }
            else -> throw IllegalArgumentException("Invalid Uri.")
        }
    }

    override fun getType(uri: Uri): String {
        return "vnd.android.cursor.dir/vnd.$AUTHORITY"
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return 0
    }
}