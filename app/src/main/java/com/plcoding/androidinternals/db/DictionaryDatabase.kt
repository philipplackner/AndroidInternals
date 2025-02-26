package com.plcoding.androidinternals.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DictionaryEntity::class],
    version = 1
)
abstract class DictionaryDatabase: RoomDatabase() {
    abstract val dao: DictionaryDao
}