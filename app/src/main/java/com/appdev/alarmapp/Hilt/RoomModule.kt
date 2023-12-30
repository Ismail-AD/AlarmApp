package com.appdev.alarmapp.Hilt

import android.content.Context
import androidx.room.Room
import com.appdev.alarmapp.utils.AlarmDao
import com.appdev.alarmapp.utils.ImageStoreDao
import com.appdev.alarmapp.utils.ObjectsGlobal.Companion.DatabaseName
import com.appdev.alarmapp.utils.PhraseDao
import com.appdev.alarmapp.utils.RecordingsDao
import com.appdev.alarmapp.utils.RoomDB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Provides
    @Singleton
    fun roomBuilder(@ApplicationContext context: Context): RoomDB {
        return Room.databaseBuilder(context, RoomDB::class.java, DatabaseName)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideDatabaseTheDaoClass(roomDatabase: RoomDB): RecordingsDao {
        return roomDatabase.recordingDao()
    }

    @Singleton
    @Provides
    fun bringTheContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Singleton
    @Provides
    fun provideDatabaseTheAlarmClass(roomDatabase: RoomDB): AlarmDao {
        return roomDatabase.alarmDao()
    }

    @Singleton
    @Provides
    fun provideDatabaseThePhraseClass(roomDatabase: RoomDB): PhraseDao {
        return roomDatabase.phraseDao()
    }

    @Singleton
    @Provides
    fun provideDatabaseTheImageClass(roomDatabase: RoomDB): ImageStoreDao {
        return roomDatabase.imageDao()
    }


}