package com.appdev.alarmapp.utils

import androidx.annotation.DrawableRes
import com.appdev.alarmapp.R

sealed class BottomNavItems(
    val id: Int,
    @DrawableRes val selectedId: Int,
    @DrawableRes val unSelectedId: Int
) {
    object Home : BottomNavItems(0, R.drawable.mainset, R.drawable.mainunset)
    object Sleep : BottomNavItems(1, R.drawable.setsleep, R.drawable.unsetsleep)
    object Morning : BottomNavItems(2, R.drawable.setsun, R.drawable.unsetsun)
    object Analysis : BottomNavItems(3, R.drawable.analysis, R.drawable.unsettanalysis)
    object Settings : BottomNavItems(4, R.drawable.sett, R.drawable.unsett)
}