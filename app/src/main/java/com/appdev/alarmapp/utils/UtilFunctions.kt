package com.appdev.alarmapp.utils

import android.net.Uri
import android.os.Build
import android.util.Patterns
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.appdev.alarmapp.R
import com.appdev.alarmapp.ui.inappbuyScreen.PurchaseOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.net.URLDecoder
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

val motivationalPhrases = listOf(
    CustomPhrase(
        phraseData = "Believe in yourself and all that you are. Know that there is something inside you that is greater than any obstacle.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Your only limit is you. Embrace the challenges and turn them into opportunities for growth.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Success is not final, failure is not fatal: It is the courage to continue that counts.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Don't watch the clock; do what it does. Keep going and never give up on your dreams.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "The only way to achieve the impossible is to believe it is possible. Dream big!",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Your attitude determines your direction. Stay positive, work hard, and make it happen.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Do it with passion or not at all. Passion is the fuel that drives success.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Challenges are what make life interesting. Overcoming them is what makes life meaningful.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Believe you can and you're halfway there. Confidence is the key to unlocking your potential.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Success is stumbling from failure to failure with no loss of enthusiasm. Keep moving forward.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "The future belongs to those who believe in the beauty of their dreams. Dream, strive, achieve.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "The only place where success comes before work is in the dictionary. Put in the effort.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Your time is limited, don't waste it living someone else's life. Follow your heart and intuition.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "It's not about the destination, it's about the journey. Enjoy the process of becoming the best version of yourself.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "The harder you work for something, the greater you'll feel when you achieve it. Work hard, dream big.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "You are never too old to set another goal or to dream a new dream. Keep evolving.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Your mindset determines your success. Cultivate a positive mindset and watch your life transform.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Make each day your masterpiece. Small daily improvements lead to long-term success.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Don't be afraid to give up the good to go for the great. Strive for excellence.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Success is not in what you have, but who you are. Be the best version of yourself.",
        phraseId = (0..19992).random().toLong()
    )
)

val basicPhrases = listOf(
    CustomPhrase(
        phraseData = "Embrace the uncertainty, and you'll discover endless possibilities.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "In the midst of chaos, find your inner peace and strength.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Celebrate small victories on the journey to your big goals.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Radiate positive energy; it's contagious and transforms the world around you.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Challenge yourself daily; that's where growth and magic happen.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Your uniqueness is your superpower; shine bright, don't dim your light.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Success is a journey, not a destination; enjoy every step.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Dance in the rain, and let every setback be a setup for a comeback.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Kindness is a language everyone understands; speak it often.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Find joy in the ordinary; life's beauty is in the details.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Be the reason someone believes in the goodness of people.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Success is not final; failure is not fatal—courage to continue matters.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Dream big, work hard, stay focused, and surround yourself with good people.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Your potential is limitless; don't underestimate the power within you.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "It's okay not to be perfect; progress is better than perfection.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Cultivate an attitude of gratitude; it transforms ordinary days into thanksgiving.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Create a life that feels good on the inside, not just one that looks good.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Every sunrise is an invitation to brighten someone's day.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Your attitude determines your direction; choose positivity.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Believe in the magic within you; you're capable of amazing things.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Celebrate your journey; each step is a victory worth acknowledging.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "The secret to getting ahead is getting started—take that first step.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Surround yourself with those who lift you higher and inspire your greatness.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Take chances, make mistakes, and embrace the beautiful mess of learning.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Radiate love; it's the invisible force that transforms everything it touches.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Find joy in the journey, for the destination is just a checkpoint.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Your potential is like a seed; nurture it, and watch it grow.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Create a life that reflects your values, and you'll find fulfillment.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Your story is unique; share it authentically and inspire others.",
        phraseId = (0..19992).random().toLong()
    ),
    CustomPhrase(
        phraseData = "Success is not about the destination but the person you become on the way.",
        phraseId = (0..19992).random().toLong()
    )
)

fun convertSetToString(set: Set<CustomPhrase>): String {
    val gson = Gson()
    return gson.toJson(
        if (set.isEmpty()) {
            setOf(motivationalPhrases[0])
        } else set
    ) ?: ""
}

// Convert String to Set<CustomPhrase>
fun convertStringToSet(string: String): Set<CustomPhrase> {
    val gson = Gson()
    val type = object : TypeToken<Set<CustomPhrase>>() {}.type
    return gson.fromJson(string, type) ?: emptySet()
}


val ListOfPurchaseOptions: List<PurchaseOptions> =
    listOf(
        PurchaseOptions("Monthly plan", 1700, planTitle = "Basic plan"),
        PurchaseOptions(
            "Annual plan",
            previousPrice = 1700,
            perMonth = 967,
            perYear = 11600,
            planTitle = "33% off"
        )
    )
val listOfIntervals: List<String> = listOf(
    "1",
    "5",
    "10",
    "15",
    "20",
    "25",
    "30"
)
val listOfMissionTime: List<String> = listOf(
    "10",
    "20",
    "30",
    "40",
    "50",
    "60",
)
val gentleWakeup: List<String> = listOf(
    "15",
    "30",
    "1",
    "5",
    "10"
)

val listOfSensi: List<String> = listOf(
    "High(hard to turn off)",
    "Normal",
    "Low(easy to turn off)",
)

val listOfOptions: List<String> = listOf(
    "Inquiry",
    "Bug",
    "Suggestion",
    "Compliment",
)

fun isEmailValid(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

data class TimeUntil(val days: Long, val hours: Long, val minutes: Long, val seconds: Long)

fun calculateTimeUntil(timestamp: Long): TimeUntil {
        val currentTime = System.currentTimeMillis()
        val timeDifference = timestamp - currentTime
        val days = TimeUnit.MILLISECONDS.toDays(timeDifference)
        val hours = TimeUnit.MILLISECONDS.toHours(timeDifference) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifference) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeDifference) % 60
        return TimeUntil(days, hours, minutes, seconds)
}

val weekDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

val ringtoneList = listOf(
    Ringtone("Silent", R.raw.silenceplease),
    Ringtone("Alarm Bell", R.raw.alarmsound),
    Ringtone("Peaceful Sound", R.raw.peacefulsound),
    Ringtone("Cheerful Sound", R.raw.cheerfulsound),
    Ringtone("Loud Sound", R.raw.loudsound),
)

val tabs = listOf(
    tabData("Alarmy ringtone"), tabData("Device ringtone"),
    tabData("Recorded sound")
)

val sentenceTabs = listOf(
    tabData("Motivational phrases"), tabData("Basic phrases"),
    tabData("My phrases")
)

fun getRepeatText(selectedDays: Set<String>): String {
    return when {
        selectedDays.isEmpty() -> "Never"
        selectedDays.size <= 3 -> selectedDays.joinToString(", ") { it.take(3) }
        else -> selectedDays.take(3).joinToString(", ") { it.take(3) } + "..."
    }
}


fun Ringtone.toRingtoneEntity(): RingtoneEntity {
    val encodedUri = this.file?.toUri()?.let { Uri.encode(it.toString()) }
    return RingtoneEntity(name = this.name, filePath = encodedUri ?: "")
}

fun RingtoneEntity.toRingtone(): Ringtone {
    val decodedUriString = this.filePath.let { URLDecoder.decode(it, "UTF-8") }
    val decodedUri = Uri.parse(decodedUriString)
    return Ringtone(name = this.name, file = File(decodedUri.path), ringId = this.id)
}


fun Ringtone.toSystemRingtoneEntity(): SystemRingtone {
    val encodedUri = this.uri?.let { Uri.encode(it.toString()) }
    return SystemRingtone(name = this.name, ringUri = encodedUri ?: "")
}

fun SystemRingtone.toRingtoneFromSystem(): Ringtone {
    val decodedUriString = this.ringUri.let { URLDecoder.decode(it, "UTF-8") }
    val decodedUri = Uri.parse(decodedUriString)
    return Ringtone(name = this.name, uri = decodedUri)
}

fun Ringtone.isSameAs(other: Ringtone): Boolean {
    return this.name == other.name
}

fun fromLocalTime(localTime: LocalTime?): String {
    return localTime.toString()
}

@RequiresApi(Build.VERSION_CODES.O)
fun toLocalTime(value: String?): LocalTime {
    return LocalTime.parse(value) ?: LocalTime.now()
}

fun fromStringSet(value: Set<String>?): String? {
    return value?.joinToString(",")
}

fun toStringSet(value: String?): Set<String> {
    return value?.split(",")?.toSet() ?: emptySet()
}

fun convertMillisToLocalTime(millis: Long): String {
    // Convert milliseconds to Instant
    val instant = Instant.ofEpochMilli(millis)

    // Define the desired time zone (e.g., "America/New_York")
    val zoneId = ZoneId.systemDefault()

    // Convert Instant to LocalDateTime in the given time zone
    val localDateTime = LocalDateTime.ofInstant(instant, zoneId)

    // Format the LocalDateTime as a string (optional)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    return localDateTime.format(formatter)
}

fun convertMillisToHoursAndMinutes(millis: Long): String {
    // Convert milliseconds to Instant
    val instant = Instant.ofEpochMilli(millis)

    // Define the desired time zone (e.g., "America/New_York")
    val zoneId = ZoneId.systemDefault()

    // Convert Instant to LocalDateTime in the given time zone
    val localDateTime = LocalDateTime.ofInstant(instant, zoneId)

    // Format the LocalDateTime as a string with only hours and minutes
    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    return localDateTime.format(formatter)
}

fun getFormattedToday(): String {
    // Get the current date
    val currentDate = LocalDate.now()

    // Define the desired format for day name and date
    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)

    // Format the current date as a string
    return currentDate.format(formatter)
}