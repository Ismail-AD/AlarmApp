package com.appdev.alarmapp.ui.PreivewScreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appdev.alarmapp.Repository.RingtoneRepository
import com.appdev.alarmapp.utils.EventHandlerSearchBar
import com.appdev.alarmapp.utils.Ringtone
import com.appdev.alarmapp.utils.ringtoneList
import com.appdev.alarmapp.utils.toRingtoneEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreviewViewModel @Inject constructor() :
    ViewModel() {

    var searchBarState by mutableStateOf(TopBarState())
    var chatMatesUiState by mutableStateOf(DataUi())


    fun eventHandlerForSB(eventHandlerSearchBar: EventHandlerSearchBar) {
        when (eventHandlerSearchBar) {
            is EventHandlerSearchBar.SearchBarOpen -> {
                searchBarState =
                    searchBarState.copy(isSearchBarVisible = eventHandlerSearchBar.openSB)
                searchBarState =
                    searchBarState.copy(listOfChatMates = ringtoneList.toMutableList())
            }

            is EventHandlerSearchBar.SearchedData -> {
                searchBarState =
                    searchBarState.copy(searchedText = eventHandlerSearchBar.textToSearch)
                filterTheList(searchBarState.listOfChatMates, eventHandlerSearchBar.textToSearch)
            }

            EventHandlerSearchBar.getOriginalList -> chatMatesUiState =
                chatMatesUiState.copy(listData = searchBarState.listOfChatMates)

            is EventHandlerSearchBar.setNewList -> TODO()
        }
    }

    data class TopBarState(
        val isSearchBarVisible: Boolean = false,
        val searchedText: String = "",
        val listOfChatMates: MutableList<Ringtone> = mutableListOf(),
    )

    data class DataUi(
        val listData: MutableList<Ringtone> = mutableListOf(),
    )

    private fun filterTheList(listOfChatMates: MutableList<Ringtone>, textToSearch: String) {
        val filteredList = listOfChatMates.filter {
            it.name.contains(textToSearch, ignoreCase = true)
        }
        chatMatesUiState = chatMatesUiState.copy(listData = filteredList.toMutableList())
    }
}