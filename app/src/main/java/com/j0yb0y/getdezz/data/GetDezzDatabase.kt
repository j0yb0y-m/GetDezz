package com.j0yb0y.getdezz.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [DownloadEntity::class], version = 1, exportSchema = false)
@TypeConverters(DownloadTypeConverters::class)
abstract class GetDezzDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
}
