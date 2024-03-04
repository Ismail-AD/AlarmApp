package com.appdev.alarmapp.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalTime

class Converters {

    @TypeConverter
    fun fromMissionsList(missionsList: List<Missions>): String {
        Log.d("CHKSJ", missionsList.toString())
        val gson = Gson()
        return gson.toJson(missionsList)
    }
    @TypeConverter
    fun fromBitmap(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    @TypeConverter
    fun toMissionsList(missionsListString: String): List<Missions> {
        Log.d("CHKSJ", missionsListString)
        val gson = Gson()
        val type = object : TypeToken<List<Missions>>() {}.type
        return gson.fromJson(missionsListString, type)
    }

    @TypeConverter
    fun fromLocalTime(localTime: LocalTime): String {
        return localTime.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toLocalTime(value: String): LocalTime {
        return LocalTime.parse(value)
    }

    @TypeConverter
    fun fromStringSet(value: Set<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringSet(value: String): Set<String> {
        return if (value.isEmpty()) {
            emptySet()
        } else {
            value.split(",").toSet()
        }
    }

    @TypeConverter
    fun fromUri(uri: Uri?): String? {
        return uri?.toString()
    }

    @TypeConverter
    fun toUri(uriString: String?): Uri? {
        return uriString?.let { Uri.parse(it) }
    }


    @TypeConverter
    fun fromFile(file: File?): String? {
        return file?.absolutePath
    }

    @TypeConverter
    fun toFile(filePath: String?): File? {
        return filePath?.let { File(it) }
    }
}