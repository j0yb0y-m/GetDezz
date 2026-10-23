package com.j0yb0y.getdezz.di

import android.content.Context
import androidx.room.Room
import com.j0yb0y.getdezz.data.GetDezzDatabase
import com.j0yb0y.getdezz.data.repository.DownloadRepository
import com.j0yb0y.getdezz.data.settings.SettingsRepository
import com.j0yb0y.getdezz.engine.EngineRepository

interface AppContainer {
    val database: GetDezzDatabase
    val downloadRepository: DownloadRepository
    val settingsRepository: SettingsRepository
    val engineRepository: EngineRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val database: GetDezzDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            GetDezzDatabase::class.java,
            "getdezz.db"
        ).build()
    }

    override val downloadRepository: DownloadRepository by lazy {
        DownloadRepository(database.downloadDao())
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context.applicationContext)
    }

    override val engineRepository: EngineRepository by lazy {
        EngineRepository(context.applicationContext)
    }
}
