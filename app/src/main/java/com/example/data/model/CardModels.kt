package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class CardCategory(
  val code: String,
  val labelBm: String,
  val subtitleBm: String,
  val defaultRsvpEnabled: Boolean,
) {
  INDIVIDU(
    code = "INDIVIDU",
    labelBm = "Ucapan Individu",
    subtitleBm = "Pautan khas peribadi tanpa RSVP",
    defaultRsvpEnabled = false,
  ),
  MAJLIS(
    code = "MAJLIS",
    labelBm = "Jemputan Majlis",
    subtitleBm = "Jemputan acara dengan penjejakan RSVP",
    defaultRsvpEnabled = true,
  ),
  PERNIAGAAN(
    code = "PERNIAGAAN",
    labelBm = "Kad Perniagaan",
    subtitleBm = "Profil profesional & pautan terus",
    defaultRsvpEnabled = false,
  ),
  PERAYAAN(
    code = "PERAYAAN",
    labelBm = "Kad Perayaan",
    subtitleBm = "Sambutan musim perayaan & rumah terbuka",
    defaultRsvpEnabled = true,
  );

  companion object {
    fun fromCode(code: String): CardCategory =
      entries.firstOrNull { it.code == code } ?: INDIVIDU
  }
}

enum class PaperStyle(val code: String, val labelBm: String) {
  IVORY_COTTON("IVORY_COTTON", "Kertas Kapas Gading"),
  BLUSH_CHAMPAGNE("BLUSH_CHAMPAGNE", "Cat Air Syampagne"),
  EMERALD_SONGKET("EMERALD_SONGKET", "Baldu Zamrud Songket"),
  OBSIDIAN_GOLD("OBSIDIAN_GOLD", "Obsidian Emas"),
  WARM_LINEN("WARM_LINEN", "Linen Krim Hangat");

  companion object {
    fun fromCode(code: String): PaperStyle =
      entries.firstOrNull { it.code == code } ?: IVORY_COTTON
  }
}

enum class FrameStyle(val code: String, val labelBm: String) {
  BOTANICAL_CORNER("BOTANICAL_CORNER", "Sudut Flora Emas"),
  DOUBLE_GOLD("DOUBLE_GOLD", "Bingkai Emas Berganda"),
  SONGKET_ARCH("SONGKET_ARCH", "Gerbang Songket"),
  MINIMAL_HAIRLINE("MINIMAL_HAIRLINE", "Garis Halus Moden"),
  NONE("NONE", "Tanpa Bingkai");

  companion object {
    fun fromCode(code: String): FrameStyle =
      entries.firstOrNull { it.code == code } ?: DOUBLE_GOLD
  }
}

enum class AnimationType(val code: String, val labelBm: String) {
  GOLD_PETALS("GOLD_PETALS", "Kelopak Emas"),
  STAR_SHIMMER("STAR_SHIMMER", "Kilauan Bintang"),
  FESTIVE_CONFETTI("FESTIVE_CONFETTI", "Konfeti Perayaan"),
  ENVELOPE_REVEAL("ENVELOPE_REVEAL", "Pembukaan Sampul"),
  NONE("NONE", "Tiada Animasi");

  companion object {
    fun fromCode(code: String): AnimationType =
      entries.firstOrNull { it.code == code } ?: GOLD_PETALS
  }
}

enum class MusicTrack(val code: String, val labelBm: String, val moodBm: String) {
  MELODI_TRADISI("MELODI_TRADISI", "Melodi Tradisi Harmoni", "Lembut & Klasik"),
  PIANO_MALAM("PIANO_MALAM", "Piano Malam Indah", "Romantis & Tenang"),
  GEMA_SYAWAL("GEMA_SYAWAL", "Gema Syawal Riang", "Ceria Perayaan"),
  KLASIK_KORPORAT("KLASIK_KORPORAT", "Klasik Korporat", "Elegan Profesional"),
  NONE("NONE", "Tiada Muzik", "Senyap");

  companion object {
    fun fromCode(code: String): MusicTrack =
      entries.firstOrNull { it.code == code } ?: NONE
  }
}

enum class GuestGroup(val code: String, val labelBm: String) {
  KELUARGA("KELUARGA", "Keluarga"),
  RAKAN("RAKAN", "Rakan"),
  VIP("VIP", "VIP"),
  PERNIAGAAN("PERNIAGAAN", "Rakan Niaga"),
  UMUM("UMUM", "Umum");

  companion object {
    fun fromCode(code: String): GuestGroup =
      entries.firstOrNull { it.code == code } ?: UMUM
  }
}

enum class GuestStatus(val code: String, val labelBm: String) {
  DRAF("DRAF", "Belum Hantar"),
  DIHANTAR("DIHANTAR", "Dihantar"),
  DIBUKA("DIBUKA", "Dibuka"),
  HADIR("HADIR", "Hadir"),
  MUNGKIN("MUNGKIN", "Mungkin"),
  TIDAK_HADIR("TIDAK_HADIR", "Tidak Hadir");

  companion object {
    fun fromCode(code: String): GuestStatus =
      entries.firstOrNull { it.code == code } ?: DRAF
  }
}

@IgnoreExtraProperties
data class CanvasElement(
  val id: String = "",
  val type: String = "TEXT", // TEXT | STICKER | PHOTO
  val content: String = "",
  val x: Double = 0.5,
  val y: Double = 0.5,
  val scale: Double = 1.0,
  val rotation: Double = 0.0,
  val fontKey: String = "PLAYFAIR",
  val colorHex: String = "#1E1A16",
  val fontSizeSp: Double = 18.0,
  val zIndex: Long = 1L,
)

@IgnoreExtraProperties
data class ECard(
  @DocumentId val id: String = "",
  val userId: String = "",
  val shareCode: String = "",
  val title: String = "",
  val category: String = CardCategory.INDIVIDU.code,
  val templateId: String = "ind_birthday_champagne",
  val recipientName: String = "",
  val greetingTitle: String = "",
  val greetingBody: String = "",
  val senderName: String = "",
  val eventDate: String = "",
  val eventTime: String = "",
  val eventVenue: String = "",
  val businessRole: String = "",
  val businessCompany: String = "",
  val businessPhone: String = "",
  val businessEmail: String = "",
  val businessWebsite: String = "",
  val paperStyle: String = PaperStyle.IVORY_COTTON.code,
  val frameStyle: String = FrameStyle.DOUBLE_GOLD.code,
  val animationType: String = AnimationType.GOLD_PETALS.code,
  val musicTrack: String = MusicTrack.PIANO_MALAM.code,
  val rsvpEnabled: Boolean = false,
  val elements: List<CanvasElement> = emptyList(),
  val viewCount: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class GuestEntry(
  @DocumentId val id: String = "",
  val userId: String = "",
  val cardId: String = "",
  val name: String = "",
  val phone: String = "",
  val group: String = GuestGroup.RAKAN.code,
  val status: String = GuestStatus.DRAF.code,
  val pax: Long = 1L,
  val note: String = "",
  val inviteCode: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class SharedCard(
  @DocumentId val id: String = "",
  val ownerId: String = "",
  val cardId: String = "",
  val shareCode: String = "",
  val title: String = "",
  val category: String = CardCategory.INDIVIDU.code,
  val templateId: String = "",
  val recipientName: String = "",
  val greetingTitle: String = "",
  val greetingBody: String = "",
  val senderName: String = "",
  val eventDate: String = "",
  val eventTime: String = "",
  val eventVenue: String = "",
  val businessRole: String = "",
  val businessCompany: String = "",
  val businessPhone: String = "",
  val businessEmail: String = "",
  val businessWebsite: String = "",
  val paperStyle: String = PaperStyle.IVORY_COTTON.code,
  val frameStyle: String = FrameStyle.DOUBLE_GOLD.code,
  val animationType: String = AnimationType.GOLD_PETALS.code,
  val musicTrack: String = MusicTrack.PIANO_MALAM.code,
  val rsvpEnabled: Boolean = false,
  val elements: List<CanvasElement> = emptyList(),
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

@IgnoreExtraProperties
data class SharedRsvp(
  @DocumentId val id: String = "",
  val ownerId: String = "",
  val responderUid: String = "",
  val shareCode: String = "",
  val cardId: String = "",
  val guestName: String = "",
  val status: String = GuestStatus.HADIR.code,
  val pax: Long = 1L,
  val wishes: String = "",
  val inviteCode: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)
