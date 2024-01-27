package com.appdev.alarmapp.utils

import androidx.annotation.DrawableRes
import com.appdev.alarmapp.R

sealed class BottomNavItems(
    val id: Int,
    @DrawableRes val selectedId: Int,
    @DrawableRes val darkSelectedId: Int,
    @DrawableRes val unSelectedId: Int
) {
    object Home : BottomNavItems(0, R.drawable.mainset, R.drawable.darkclock, R.drawable.mainunset)
    object Settings : BottomNavItems(4, R.drawable.sett, R.drawable.darksettings, R.drawable.unsett)
}