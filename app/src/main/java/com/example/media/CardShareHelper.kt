package com.example.media

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.CardCategory
import com.example.data.model.ECard
import com.example.data.model.FrameStyle
import com.example.data.model.GuestEntry
import com.example.data.model.PaperStyle
import java.io.File
import java.io.FileOutputStream

object CardShareHelper {

  fun buildShareUrl(card: ECard, guestInviteCode: String? = null): String {
    val code = card.shareCode.ifBlank { "SERIKAD" }
    return if (card.category == CardCategory.INDIVIDU.code || !card.rsvpEnabled) {
      "https://serikad.app/kad/$code"
    } else if (!guestInviteCode.isNullOrBlank()) {
      "https://serikad.app/rsvp/$code?code=$guestInviteCode"
    } else {
      "https://serikad.app/rsvp/$code"
    }
  }

  fun buildShareMessage(card: ECard, guest: GuestEntry? = null): String {
    val link = buildShareUrl(card, guest?.inviteCode)
    val recipient = guest?.name?.takeIf { it.isNotBlank() } ?: card.recipientName
    return when (CardCategory.fromCode(card.category)) {
      CardCategory.INDIVIDU -> {
        val greetingTarget = if (recipient.isNotBlank()) "Hai $recipient,\n\n" else ""
        "${greetingTarget}Ada e-kad ucapan khas untuk anda daripada ${card.senderName.ifBlank { "rakan anda" }}!\n\n" +
          "\"${card.greetingTitle}\"\n\n" +
          "Buka kad beranimasi & muzik melalui pautan khas anda:\n$link\n\n" +
          "Kod Kad: ${card.shareCode}"
      }
      CardCategory.MAJLIS -> {
        val greetingTarget = if (recipient.isNotBlank()) "Kepada $recipient,\n\n" else ""
        "${greetingTarget}Jemputan Khas: ${card.greetingTitle.replace("\n", " ")}\n" +
          (if (card.eventDate.isNotBlank()) "Tarikh: ${card.eventDate}\n" else "") +
          (if (card.eventTime.isNotBlank()) "Masa: ${card.eventTime}\n" else "") +
          (if (card.eventVenue.isNotBlank()) "Lokasi: ${card.eventVenue}\n" else "") +
          "\nBuka e-kad & sahkan kehadiran (RSVP) anda di sini:\n$link\n" +
          "Kod Jemputan: ${card.shareCode}"
      }
      CardCategory.PERNIAGAAN -> {
        "Kad Perniagaan Digital — ${card.greetingTitle}\n" +
          (if (card.businessRole.isNotBlank()) "${card.businessRole}, ${card.businessCompany}\n" else "") +
          (if (card.businessPhone.isNotBlank()) "Tel: ${card.businessPhone}\n" else "") +
          (if (card.businessEmail.isNotBlank()) "E-mel: ${card.businessEmail}\n" else "") +
          "\nLihat kad interaktif:\n$link\nKod: ${card.shareCode}"
      }
      CardCategory.PERAYAAN -> {
        val greetingTarget = if (recipient.isNotBlank()) "Kepada $recipient,\n\n" else ""
        val rsvpLine =
          if (card.rsvpEnabled) "Buka kad & sahkan kehadiran anda di:\n$link"
          else "Buka e-kad ucapan perayaan anda di:\n$link"
        "${greetingTarget}${card.greetingTitle}\n${card.greetingBody}\n\n$rsvpLine\nKod: ${card.shareCode}"
      }
    }
  }

  fun copyShareLink(context: Context, card: ECard, guest: GuestEntry? = null): String {
    val url = buildShareUrl(card, guest?.inviteCode)
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Pautan SeriKad", url))
    return url
  }

  fun shareToWhatsApp(context: Context, card: ECard, guest: GuestEntry? = null) {
    val message = buildShareMessage(card, guest)
    val imageUri = exportCardImageUri(context, card)
    val pm = context.packageManager
    val targetPkg =
      listOf("com.whatsapp", "com.whatsapp.w4b").firstOrNull { pkg ->
        try {
          pm.getPackageInfo(pkg, 0)
          true
        } catch (_: Exception) {
          false
        }
      }

    val intent =
      Intent(Intent.ACTION_SEND).apply {
        type = if (imageUri != null) "image/png" else "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
        if (imageUri != null) {
          putExtra(Intent.EXTRA_STREAM, imageUri)
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        if (targetPkg != null) {
          setPackage(targetPkg)
        }
        // If guest has a phone number and WhatsApp is installed, hint JID if pure text or use standard send
      }

    try {
      if (targetPkg != null) {
        context.startActivity(intent)
      } else {
        context.startActivity(Intent.createChooser(intent, "Kongsi Kad Melalui WhatsApp / Sosial"))
      }
    } catch (_: Exception) {
      shareToSocial(context, card, guest)
    }
  }

  fun shareToSocial(context: Context, card: ECard, guest: GuestEntry? = null) {
    val message = buildShareMessage(card, guest)
    val imageUri = exportCardImageUri(context, card)
    val intent =
      Intent(Intent.ACTION_SEND).apply {
        type = if (imageUri != null) "image/png" else "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
        putExtra(Intent.EXTRA_SUBJECT, card.title)
        if (imageUri != null) {
          putExtra(Intent.EXTRA_STREAM, imageUri)
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
      }
    context.startActivity(Intent.createChooser(intent, "Kongsi E-Kad SeriKad"))
  }

  fun exportCardImageUri(context: Context, card: ECard): Uri? {
    return try {
      val bitmap = renderCardBitmap(context, card)
      val dir = File(context.cacheDir, "shared_cards").apply { mkdirs() }
      val fileName = "serikad_${card.shareCode.ifBlank { "preview" }}.png"
      val file = File(dir, fileName)
      FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
      }
      bitmap.recycle()
      FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    } catch (_: Exception) {
      null
    }
  }

  private fun renderCardBitmap(context: Context, card: ECard): Bitmap {
    val width = 1080
    val height = 1440
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paper = PaperStyle.fromCode(card.paperStyle)
    val isDarkPaper =
      paper == PaperStyle.EMERALD_SONGKET || paper == PaperStyle.OBSIDIAN_GOLD

    // 1. Background texture / color
    val bgDrawableRes =
      when (paper) {
        PaperStyle.IVORY_COTTON -> R.drawable.img_card_floral_ivory
        PaperStyle.BLUSH_CHAMPAGNE -> R.drawable.img_card_celebration_blush
        PaperStyle.EMERALD_SONGKET -> R.drawable.img_card_festive_emerald
        else -> null
      }

    if (bgDrawableRes != null) {
      val bgBmp = BitmapFactory.decodeResource(context.resources, bgDrawableRes)
      if (bgBmp != null) {
        canvas.drawBitmap(bgBmp, null, Rect(0, 0, width, height), Paint(Paint.FILTER_BITMAP_FLAG))
        bgBmp.recycle()
      }
    } else {
      val topColor = if (paper == PaperStyle.OBSIDIAN_GOLD) Color.parseColor("#1D1814") else Color.parseColor("#FAF5EB")
      val botColor = if (paper == PaperStyle.OBSIDIAN_GOLD) Color.parseColor("#2B221A") else Color.parseColor("#EFE5D4")
      val bgPaint =
        Paint().apply {
          shader = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), topColor, botColor, Shader.TileMode.CLAMP)
        }
      canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
    }

    // 2. Gold foil frame
    val goldColor = if (isDarkPaper) Color.parseColor("#E5C158") else Color.parseColor("#B8860B")
    val borderPaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = goldColor
        style = Paint.Style.STROKE
        strokeWidth = 4f
      }
    when (FrameStyle.fromCode(card.frameStyle)) {
      FrameStyle.DOUBLE_GOLD, FrameStyle.BOTANICAL_CORNER -> {
        canvas.drawRect(44f, 44f, width - 44f, height - 44f, borderPaint)
        borderPaint.strokeWidth = 2f
        canvas.drawRect(60f, 60f, width - 60f, height - 60f, borderPaint)
      }
      FrameStyle.SONGKET_ARCH -> {
        canvas.drawRoundRect(RectF(48f, 48f, width - 48f, height - 48f), 40f, 40f, borderPaint)
      }
      FrameStyle.MINIMAL_HAIRLINE -> {
        borderPaint.strokeWidth = 2f
        canvas.drawRect(48f, 48f, width - 48f, height - 48f, borderPaint)
      }
      FrameStyle.NONE -> Unit
    }

    val serifTypeface =
      try {
        ResourcesCompat.getFont(context, R.font.playfair_display) ?: Typeface.SERIF
      } catch (_: Exception) {
        Typeface.SERIF
      }
    val sansTypeface =
      try {
        ResourcesCompat.getFont(context, R.font.plus_jakarta_sans) ?: Typeface.SANS_SERIF
      } catch (_: Exception) {
        Typeface.SANS_SERIF
      }

    val primaryText = if (isDarkPaper) Color.parseColor("#FAF6EF") else Color.parseColor("#1E1A16")
    val secondaryText = if (isDarkPaper) Color.parseColor("#D9C7A3") else Color.parseColor("#5C5044")

    // Recipient label
    var cursorY = 280f
    if (card.recipientName.isNotBlank()) {
      val recPaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
          color = goldColor
          textSize = 32f
          textAlign = Paint.Align.CENTER
          typeface = sansTypeface
        }
      canvas.drawText("Kepada: ${card.recipientName}", width / 2f, cursorY, recPaint)
      cursorY += 74f
    }

    // Greeting Title
    val titlePaint =
      TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = primaryText
        textSize = 66f
        typeface = serifTypeface
      }
    val titleLayout =
      StaticLayout.Builder.obtain(card.greetingTitle, 0, card.greetingTitle.length, titlePaint, width - 220)
        .setAlignment(Layout.Alignment.ALIGN_CENTER)
        .setLineSpacing(8f, 1.0f)
        .build()
    canvas.save()
    canvas.translate(110f, cursorY)
    titleLayout.draw(canvas)
    canvas.restore()
    cursorY += titleLayout.height + 56f

    // Greeting Body
    val bodyPaint =
      TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = secondaryText
        textSize = 36f
        typeface = sansTypeface
      }
    val bodyLayout =
      StaticLayout.Builder.obtain(card.greetingBody, 0, card.greetingBody.length, bodyPaint, width - 240)
        .setAlignment(Layout.Alignment.ALIGN_CENTER)
        .setLineSpacing(10f, 1.1f)
        .build()
    canvas.save()
    canvas.translate(120f, cursorY)
    bodyLayout.draw(canvas)
    canvas.restore()
    cursorY += bodyLayout.height + 64f

    // Event or Business details
    val details = buildList {
      if (card.eventDate.isNotBlank()) add(card.eventDate)
      if (card.eventTime.isNotBlank()) add(card.eventTime)
      if (card.eventVenue.isNotBlank()) add(card.eventVenue)
      if (card.businessRole.isNotBlank()) add("${card.businessRole} • ${card.businessCompany}")
      if (card.businessPhone.isNotBlank()) add(card.businessPhone)
      if (card.businessEmail.isNotBlank()) add(card.businessEmail)
    }
    if (details.isNotEmpty()) {
      val detailPaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
          color = primaryText
          textSize = 30f
          textAlign = Paint.Align.CENTER
          typeface = sansTypeface
        }
      for (line in details) {
        canvas.drawText(line, width / 2f, cursorY, detailPaint)
        cursorY += 46f
      }
    }

    // Sender footer
    val senderPaint =
      TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = goldColor
        textSize = 34f
        textAlign = Paint.Align.CENTER
        typeface = serifTypeface
      }
    canvas.drawText(card.senderName, width / 2f, height - 180f, senderPaint)

    val brandPaint =
      TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = secondaryText
        textSize = 24f
        textAlign = Paint.Align.CENTER
        typeface = sansTypeface
      }
    canvas.drawText("SeriKad • ${card.shareCode}", width / 2f, height - 110f, brandPaint)

    return bitmap
  }
}
