package com.appdev.alarmapp.ui.StartingScreens

import androidx.annotation.DrawableRes
import com.appdev.alarmapp.R

sealed class OnBoardingPage(
    @DrawableRes
    val image: Int,
    val title: String,
    val description: String,
    val description2: String
) {
    object First : OnBoardingPage(
        image = R.drawable.alarmlogo,
        title = "Based on behavioral science",
        description = "The only alarm listed in",
        description2 = "medical journals"
    )

    object Second : OnBoardingPage(
        image = R.drawable.alarmlogo,
        title = "Wake up instantly",
        description = "Scientific sounds to",
        description2 = "activate your brainwave"
    )

    object Third : OnBoardingPage(
        image = R.drawable.alarmlogo,
        title = "No more snoozing",
        description = "Missions that break your",
        description2 = "sleep inertia"
    )

    object Forth : OnBoardingPage(
        image = R.drawable.alarmlogo,
        title = "Time to wake up scientifically",
        description = "Top Choice for a reason",
        description2 = "See for yourself"
    )
}
