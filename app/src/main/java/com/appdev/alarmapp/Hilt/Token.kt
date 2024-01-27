package com.appdev.alarmapp.Hilt

import android.content.Context
import com.appdev.alarmapp.utils.ObjectsGlobal.Companion.Newly_Selected_Puzzle
import com.appdev.alarmapp.utils.ObjectsGlobal.Companion.Selected_Puzzle
import com.appdev.alarmapp.utils.ObjectsGlobal.Companion.THEME_SET
import com.appdev.alarmapp.utils.ObjectsGlobal.Companion.TOKEN_FILE_TO_STORE_TOKEN
import com.appdev.alarmapp.utils.ObjectsGlobal.Companion.USER_TOKEN
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TokenManagement @Inject constructor(@ApplicationContext context: Context) //Qualifier to denote the type of context to tell hilt
{
    private var _prefs = context.getSharedPreferences(
        TOKEN_FILE_TO_STORE_TOKEN,
        Context.MODE_PRIVATE
    ) //mode private to be used by application only

    fun saveToken(token: String) {
        val editorObject = _prefs.edit() //to edit values to be stored
        editorObject.putString(USER_TOKEN, token) //key-value format
        editorObject.apply() //call to save key-value data edited
    }

    fun getToken(): String? {
        return _prefs.getString(
            USER_TOKEN,
            null
        ) //default val to be passed we will pass null if token not present return default value
    }

    fun saveTheme(darkTheme: Boolean) {
        val editorObject = _prefs.edit() //to edit values to be stored
        editorObject.putBoolean(THEME_SET, darkTheme) //key-value format
        editorObject.apply() //call to save key-value data edited
    }
    fun getTheme(): Flow<Boolean> = flow {
        emit(_prefs.getBoolean(THEME_SET, false))
    }

}