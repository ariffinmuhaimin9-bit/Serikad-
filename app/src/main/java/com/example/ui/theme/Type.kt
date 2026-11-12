package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val PlayfairDisplayFamily = FontFamily(Font(R.font.playfair_display, FontWeight.Normal))
val PlusJakartaSansFamily = FontFamily(Font(R.font.plus_jakarta_sans, FontWeight.Normal))
val PacificoFamily = FontFamily(Font(R.font.pacifico, FontWeight.Normal))
val DancingScriptFamily = FontFamily(Font(R.font.dancing_script, FontWeight.Normal))
val LoraFamily = FontFamily(Font(R.font.lora, FontWeight.Normal))
val RobotoMonoFamily = FontFamily(Font(R.font.roboto_mono, FontWeight.Normal))

enum class CardFontOption(val key: String, val labelBm: String, val family: FontFamily) {
  PLAYFAIR("PLAYFAIR", "Klasik Editorial", PlayfairDisplayFamily),
  DANCING("DANCING", "Kaligrafi Halus", DancingScriptFamily),
  PACIFICO("PACIFICO", "Santai Artistik", PacificoFamily),
  LORA("LORA", "Sastera Elegan", LoraFamily),
  JAKARTA("JAKARTA", "Moden Bersih", PlusJakartaSansFamily),
  MONO("MONO", "Monospasi Kod", RobotoMonoFamily);

  companion object {
    fun fromKey(key: String): CardFontOption =
      entries.firstOrNull { it.key == key } ?: PLAYFAIR
  }
}

val Typography =
  Typography(
    displayLarge =
      TextStyle(
        fontFamily = PlayfairDisplayFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
      ),
    headlineLarge =
      TextStyle(
        fontFamily = PlayfairDisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
      ),
    headlineMedium =
      TextStyle(
        fontFamily = PlayfairDisplayFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
      ),
    titleLarge =
      TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 26.sp,
      ),
    titleMedium =
      TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
      ),
    bodyLarge =
      TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
      ),
    bodyMedium =
      TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
      ),
    labelLarge =
      TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
      ),
    labelSmall =
      TextStyle(
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
      ),
  )
