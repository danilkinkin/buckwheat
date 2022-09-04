package com.danilkinkin.buckwheat.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideYourDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app.applicationContext,
        DatabaseModule::class.java,
        "buckwheat-db",
    )
        .fallbackToDestructiveMigration()
        .allowMainThreadQueries()
        .build()

    @Singleton
    @Provides
    fun provideSpentDao(db: DatabaseModule) = db.spentDao()

    @Singleton
    @Provides
    fun provideStorageDao(db: DatabaseModule) = db.storageDao()

}