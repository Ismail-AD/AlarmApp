package com.appdev.alarmapp.Hilt

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.room.Room
import com.appdev.alarmapp.AlarmManagement.AlarmScheduler
import com.appdev.alarmapp.utils.DaoClasses.AlarmBasicSettingDao
import com.appdev.alarmapp.utils.DaoClasses.AlarmDao
import com.appdev.alarmapp.utils.DaoClasses.DefaultSettingsDao
import com.appdev.alarmapp.utils.DaoClasses.DismissDao
import com.appdev.alarmapp.utils.DaoClasses.ImageStoreDao
import com.appdev.alarmapp.utils.ObjectsGlobal.Companion.DatabaseName
import com.appdev.alarmapp.utils.DaoClasses.PhraseDao
import com.appdev.alarmapp.utils.DaoClasses.QrCodeDao
import com.appdev.alarmapp.utils.DaoClasses.RecordingsDao
import com.appdev.alarmapp.utils.RoomDB
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {
    @Singleton
    @Provides
    fun provideTextToSpeech(context: Context): TextToSpeech {
        return TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {

            } else {
                // Handle TextToSpeech initialization failure
            }
        }
    }
    @Singleton
    @Provides
    fun fireBaseDBInstance(): FirebaseDatabase = Firebase.database

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

    @Singleton
    @Provides
    fun provideDatabaseTheQrCodeClass(roomDatabase: RoomDB): QrCodeDao {
        return roomDatabase.qrCodeDao()
    }
    @Singleton
    @Provides
    fun provideDatabaseTheSettingsDefaultClass(roomDatabase: RoomDB): DefaultSettingsDao {
        return roomDatabase.dSettingsDao()
    }

    @Singleton
    @Provides
    fun provideDatabaseTheSettingsBasicClass(roomDatabase: RoomDB): AlarmBasicSettingDao {
        return roomDatabase.basicSettingsDao()
    }
    @Singleton
    @Provides
    fun provideDatabaseTheSettingsDismissClass(roomDatabase: RoomDB): DismissDao {
        return roomDatabase.dismissSettingsDao()
    }

}