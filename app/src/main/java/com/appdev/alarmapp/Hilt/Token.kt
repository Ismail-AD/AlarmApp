package com.appdev.alarmapp.Hilt

import android.content.Context
import com.appdev.alarmapp.utils.ObjectsGlobal.Companion.DAYS
import com.appdev.alarmapp.utils.ObjectsGlobal.Companion.Newly_Selected_Puzzle
import com.appdev.alarmapp.utils.ObjectsGlobal.Companion.Selected_Puzzle
import com.appdev.alarmapp.utils.ObjectsGlobal.Companion.TOKEN_FILE_TO_STORE_TOKEN
import com.appdev.alarmapp.utils.ObjectsGlobal.Companion.USER_TOKEN
import dagger.hilt.android.qualifiers.ApplicationContext
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

    fun removeToken() {
        return _prefs.edit().remove(USER_TOKEN).apply()
    }


    fun saveDays(token: Set<String>) {
        val editorObject = _prefs.edit() //to edit values to be stored
        editorObject.putStringSet(DAYS, token) //key-value format
        editorObject.apply() //call to save key-value data edited
    }

    fun getDays(): Set<String>? {
        return _prefs.getStringSet(
            DAYS,
            null
        ) //default val to be passed we will pass null if token not present return default value
    }

    fun removeDays() {
        return _prefs.edit().remove(DAYS).apply()
    }

    fun saveSelected(listOfSelect: Set<String>) {
        val editorObject = _prefs.edit() //to edit values to be stored
        editorObject.putStringSet(Selected_Puzzle, listOfSelect) //key-value format
        editorObject.apply() //call to save key-value data edited
    }

    fun getSelected(): Set<String>? {
        return _prefs.getStringSet(
            Selected_Puzzle,null
        ) //default val to be passed we will pass null if token not present return default value
    }

    fun removeSelected() {
        return _prefs.edit().remove(Selected_Puzzle).apply()
    }


    fun saveNewlySelected(listOfSelect: Set<String>) {
        val editorObject = _prefs.edit() //to edit values to be stored
        editorObject.putStringSet(Newly_Selected_Puzzle, listOfSelect) //key-value format
        editorObject.apply() //call to save key-value data edited
    }

    fun getAllSelected(): Set<String>? {
        return _prefs.getStringSet(
            Newly_Selected_Puzzle,
            null
        ) //default val to be passed we will pass null if token not present return default value
    }

    fun removeAllSelected() {
        return _prefs.edit().remove(Newly_Selected_Puzzle).apply()
    }

}