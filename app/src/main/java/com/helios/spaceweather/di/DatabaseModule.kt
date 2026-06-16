package com.helios.spaceweather.di

import android.content.Context
import androidx.room.Room
import com.helios.spaceweather.data.local.HeliosDatabase
import com.helios.spaceweather.data.local.KpDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Provides the Room database and its DAO. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HeliosDatabase =
        Room.databaseBuilder(context, HeliosDatabase::class.java, HeliosDatabase.NAME)
            // The cache is fully rebuildable from NOAA, so a destructive upgrade is acceptable.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideKpDao(database: HeliosDatabase): KpDao = database.kpDao()
}
