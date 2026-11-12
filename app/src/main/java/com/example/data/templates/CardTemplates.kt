package com.example.data.templates

import com.example.data.model.AnimationType
import com.example.data.model.CanvasElement
import com.example.data.model.CardCategory
import com.example.data.model.ECard
import com.example.data.model.FrameStyle
import com.example.data.model.MusicTrack
import com.example.data.model.PaperStyle

data class CardTemplatePreset(
  val id: String,
  val nameBm: String,
  val subtitleBm: String,
  val category: CardCategory,
  val paperStyle: PaperStyle,
  val frameStyle: FrameStyle,
  val animationType: AnimationType,
  val musicTrack: MusicTrack,
  val defaultRecipient: String,
  val defaultGreetingTitle: String,
  val defaultGreetingBody: String,
  val defaultSender: String,
  val defaultDate: String = "",
  val defaultTime: String = "",
  val defaultVenue: String = "",
  val defaultRole: String = "",
  val defaultCompany: String = "",
  val defaultPhone: String = "",
  val defaultEmail: String = "",
  val defaultWebsite: String = "",
  val defaultElements: List<CanvasElement> = emptyList(),
)

data class BilingualGreetingPreset(
  val id: String,
  val labelBm: String,
  val category: CardCategory,
  val titleBm: String,
  val bodyBm: String,
  val titleEn: String,
  val bodyEn: String,
)

enum class StickerOption(val key: String, val labelBm: String) {
  WAX_SEAL("WAX_SEAL", "Mohor Lilin Emas"),
  FLORAL_CREST("FLORAL_CREST", "Mahkota Bunga"),
  BIRTHDAY_CAKE("BIRTHDAY_CAKE", "Kek Ulang Tahun"),
  DIAMOND_RINGS("DIAMOND_RINGS", "Cincin Permata"),
  CRESCENT_STAR("CRESCENT_STAR", "Bulan & Bintang"),
  KETUPAT_WEAVE("KETUPAT_WEAVE", "Anyaman Ketupat"),
  LANTERN_GLOW("LANTERN_GLOW", "Tanglung Cahaya"),
  GOLD_STAR("GOLD_STAR", "Bintang Kilau"),
  HEART_LACE("HEART_LACE", "Hati Renda"),
  BRIEFCASE_BADGE("BRIEFCASE_BADGE", "Lencana Korporat");

  companion object {
    fun fromKey(key: String): StickerOption =
      entries.firstOrNull { it.key == key } ?: WAX_SEAL
  }
}

object CardTemplatesCatalog {

  val templates: List<CardTemplatePreset> =
    listOf(
      // --- 1. KAD UCAPAN INDIVIDU (NO RSVP, Dedicated Personal Link) ---
      CardTemplatePreset(
        id = "ind_birthday_champagne",
        nameBm = "Hari Lahir Syampagne",
        subtitleBm = "Ucapan peribadi eksklusif dengan pautan khas",
        category = CardCategory.INDIVIDU,
        paperStyle = PaperStyle.BLUSH_CHAMPAGNE,
        frameStyle = FrameStyle.DOUBLE_GOLD,
        animationType = AnimationType.GOLD_PETALS,
        musicTrack = MusicTrack.PIANO_MALAM,
        defaultRecipient = "Aiman Hakim",
        defaultGreetingTitle = "Selamat Hari Lahir!",
        defaultGreetingBody =
          "Semoga hari istimewamu dipenuhi kebahagiaan, kesihatan yang berpanjangan, serta segala impian tercapai dengan jayanya.",
        defaultSender = "Daripada Amirah",
        defaultElements =
          listOf(
            CanvasElement(
              id = "st_cake",
              type = "STICKER",
              content = StickerOption.BIRTHDAY_CAKE.key,
              x = 0.5,
              y = 0.14,
              scale = 1.0,
              colorHex = "#B8860B",
              zIndex = 1L,
            ),
            CanvasElement(
              id = "txt_bday_en",
              type = "TEXT",
              content = "Wishing You Endless Joy",
              x = 0.5,
              y = 0.84,
              scale = 1.0,
              fontKey = "DANCING",
              colorHex = "#7A6348",
              fontSizeSp = 16.0,
              zIndex = 2L,
            ),
          ),
      ),
      CardTemplatePreset(
        id = "ind_thankyou_ivory",
        nameBm = "Terima Kasih Orkid",
        subtitleBm = "Penghargaan tulus di atas kertas kapas gading",
        category = CardCategory.INDIVIDU,
        paperStyle = PaperStyle.IVORY_COTTON,
        frameStyle = FrameStyle.BOTANICAL_CORNER,
        animationType = AnimationType.STAR_SHIMMER,
        musicTrack = MusicTrack.MELODI_TRADISI,
        defaultRecipient = "Puan Salma",
        defaultGreetingTitle = "Terima Kasih Tidak Terhingga",
        defaultGreetingBody =
          "Setulus hati kami ucapkan terima kasih atas budi baik, sokongan dan keikhlasan anda selama ini.",
        defaultSender = "Ikhlas, Nadia",
        defaultElements =
          listOf(
            CanvasElement(
              id = "st_wax",
              type = "STICKER",
              content = StickerOption.WAX_SEAL.key,
              x = 0.5,
              y = 0.15,
              scale = 1.0,
              colorHex = "#B8860B",
              zIndex = 1L,
            )
          ),
      ),
      CardTemplatePreset(
        id = "ind_congrats_obsidian",
        nameBm = "Tahniah Diraja",
        subtitleBm = "Raikan kejayaan istimewa dengan aksen emas",
        category = CardCategory.INDIVIDU,
        paperStyle = PaperStyle.OBSIDIAN_GOLD,
        frameStyle = FrameStyle.DOUBLE_GOLD,
        animationType = AnimationType.STAR_SHIMMER,
        musicTrack = MusicTrack.KLASIK_KORPORAT,
        defaultRecipient = "Dr. Zaki",
        defaultGreetingTitle = "Setinggi-Tinggi Tahniah",
        defaultGreetingBody =
          "Kejayaan ini adalah hasil ketekunan dan usaha gigih anda. Teruskan melangkah ke puncak kecemerlangan!",
        defaultSender = "Rakan Seperjuangan",
        defaultElements =
          listOf(
            CanvasElement(
              id = "st_star",
              type = "STICKER",
              content = StickerOption.GOLD_STAR.key,
              x = 0.5,
              y = 0.14,
              scale = 1.0,
              colorHex = "#E5C158",
              zIndex = 1L,
            )
          ),
      ),

      // --- 2. KAD JEMPUTAN MAJLIS (RSVP Active) ---
      CardTemplatePreset(
        id = "majlis_walimah_orchid",
        nameBm = "Walimatulurus Orkid Gading",
        subtitleBm = "Jemputan perkahwinan mewah berserta RSVP langsung",
        category = CardCategory.MAJLIS,
        paperStyle = PaperStyle.IVORY_COTTON,
        frameStyle = FrameStyle.BOTANICAL_CORNER,
        animationType = AnimationType.GOLD_PETALS,
        musicTrack = MusicTrack.MELODI_TRADISI,
        defaultRecipient = "Tuan / Puan & Keluarga",
        defaultGreetingTitle = "Walimatulurus\nAmirah & Danial",
        defaultGreetingBody =
          "Dengan penuh kesyukuran ke hadrat Ilahi, kami mengundang anda ke majlis penyatuan dua hati kami.",
        defaultSender = "Hj. Razak & Hjh. Aminah",
        defaultDate = "Sabtu, 19 Disember 2026",
        defaultTime = "11:30 Pagi – 4:00 Petang",
        defaultVenue = "Glasshouse Seputeh, Kuala Lumpur",
        defaultElements =
          listOf(
            CanvasElement(
              id = "st_rings",
              type = "STICKER",
              content = StickerOption.DIAMOND_RINGS.key,
              x = 0.5,
              y = 0.14,
              scale = 1.0,
              colorHex = "#B8860B",
              zIndex = 1L,
            )
          ),
      ),
      CardTemplatePreset(
        id = "majlis_kesyukuran_linen",
        nameBm = "Majlis Kesyukuran & Akikah",
        subtitleBm = "Linen krim hangat dengan bingkai klasik",
        category = CardCategory.MAJLIS,
        paperStyle = PaperStyle.WARM_LINEN,
        frameStyle = FrameStyle.DOUBLE_GOLD,
        animationType = AnimationType.ENVELOPE_REVEAL,
        musicTrack = MusicTrack.MELODI_TRADISI,
        defaultRecipient = "Sahabat & Saudara",
        defaultGreetingTitle = "Majlis Kesyukuran & Akikah",
        defaultGreetingBody =
          "Semoga kehadiran anda memeriahkan majlis doa selamat dan kesyukuran keluarga kami.",
        defaultSender = "Keluarga Iskandar",
        defaultDate = "Ahad, 27 Disember 2026",
        defaultTime = "12:00 Tengah Hari – 3:30 Petang",
        defaultVenue = "Presint 10, Putrajaya",
        defaultElements =
          listOf(
            CanvasElement(
              id = "st_floral",
              type = "STICKER",
              content = StickerOption.FLORAL_CREST.key,
              x = 0.5,
              y = 0.14,
              scale = 1.0,
              colorHex = "#B8860B",
              zIndex = 1L,
            )
          ),
      ),
      CardTemplatePreset(
        id = "majlis_gala_obsidian",
        nameBm = "Malam Gala Korporat",
        subtitleBm = "Tema obsidian emas untuk majlis rasmi & anugerah",
        category = CardCategory.MAJLIS,
        paperStyle = PaperStyle.OBSIDIAN_GOLD,
        frameStyle = FrameStyle.MINIMAL_HAIRLINE,
        animationType = AnimationType.STAR_SHIMMER,
        musicTrack = MusicTrack.KLASIK_KORPORAT,
        defaultRecipient = "Tetamu Kehormat",
        defaultGreetingTitle = "Malam Gala Tahunan 2026",
        defaultGreetingBody =
          "Anda dijemput khas menghadiri Majlis Makan Malam & Penyampaian Anugerah Kecemerlangan.",
        defaultSender = "Jawatankuasa Penganjur",
        defaultDate = "Jumaat, 15 Januari 2027",
        defaultTime = "7:45 Malam – 11:00 Malam",
        defaultVenue = "Grand Ballroom, KLCC",
        defaultElements = emptyList(),
      ),

      // --- 3. KAD PERNIAGAAN ---
      CardTemplatePreset(
        id = "biz_exec_obsidian",
        nameBm = "Eksekutif Emas Hitam",
        subtitleBm = "Profil perniagaan berprestij & pautan terus",
        category = CardCategory.PERNIAGAAN,
        paperStyle = PaperStyle.OBSIDIAN_GOLD,
        frameStyle = FrameStyle.DOUBLE_GOLD,
        animationType = AnimationType.STAR_SHIMMER,
        musicTrack = MusicTrack.KLASIK_KORPORAT,
        defaultRecipient = "Rakan Niaga",
        defaultGreetingTitle = "Ariffin Muhaimin",
        defaultGreetingBody =
          "Membantu jenama membina pengalaman digital dan strategi produk bertaraf antarabangsa.",
        defaultSender = "Nusantara Studio Sdn. Bhd.",
        defaultRole = "Pengarah Kreatif & Strategi",
        defaultCompany = "Nusantara Studio",
        defaultPhone = "+60 12-345 6789",
        defaultEmail = "salam@nusantarastudio.my",
        defaultWebsite = "www.nusantarastudio.my",
        defaultElements =
          listOf(
            CanvasElement(
              id = "st_biz",
              type = "STICKER",
              content = StickerOption.BRIEFCASE_BADGE.key,
              x = 0.5,
              y = 0.14,
              scale = 0.9,
              colorHex = "#E5C158",
              zIndex = 1L,
            )
          ),
      ),
      CardTemplatePreset(
        id = "biz_creative_ivory",
        nameBm = "Atelier Minimalis Gading",
        subtitleBm = "Rekaan bersih untuk pengasas kreatif & konsultan",
        category = CardCategory.PERNIAGAAN,
        paperStyle = PaperStyle.WARM_LINEN,
        frameStyle = FrameStyle.MINIMAL_HAIRLINE,
        animationType = AnimationType.NONE,
        musicTrack = MusicTrack.NONE,
        defaultRecipient = "Pelanggan Dihargai",
        defaultGreetingTitle = "Siti Nurhalis",
        defaultGreetingBody =
          "Perunding Reka Bentuk Dalaman & Kraf Kontemporari di Lembah Klang.",
        defaultSender = "Atelier Seri",
        defaultRole = "Prinsipal Reka Bentuk",
        defaultCompany = "Atelier Seri",
        defaultPhone = "+60 19-778 4421",
        defaultEmail = "hello@atelierseri.my",
        defaultWebsite = "www.atelierseri.my",
        defaultElements = emptyList(),
      ),

      // --- 4. KAD PERAYAAN ---
      CardTemplatePreset(
        id = "fest_raya_emerald",
        nameBm = "Aidilfitri Zamrud Songket",
        subtitleBm = "Tanglung emas dan corak songket tradisi",
        category = CardCategory.PERAYAAN,
        paperStyle = PaperStyle.EMERALD_SONGKET,
        frameStyle = FrameStyle.SONGKET_ARCH,
        animationType = AnimationType.STAR_SHIMMER,
        musicTrack = MusicTrack.GEMA_SYAWAL,
        defaultRecipient = "Seisi Keluarga & Sahabat",
        defaultGreetingTitle = "Selamat Hari Raya Aidilfitri",
        defaultGreetingBody =
          "Maaf Zahir & Batin. Semoga Syawal kali ini membawa keberkatan, kegembiraan dan keampunan buat kita semua.",
        defaultSender = "Keluarga Hj. Yusof",
        defaultDate = "Sabtu, 28 Mac 2027",
        defaultTime = "12:00 Tengah Hari – 6:00 Petang",
        defaultVenue = "No. 18, Jalan Harmoni, Bangi",
        defaultElements =
          listOf(
            CanvasElement(
              id = "st_ketupat",
              type = "STICKER",
              content = StickerOption.KETUPAT_WEAVE.key,
              x = 0.5,
              y = 0.14,
              scale = 1.0,
              colorHex = "#E5C158",
              zIndex = 1L,
            )
          ),
      ),
      CardTemplatePreset(
        id = "fest_newyear_blush",
        nameBm = "Tahun Baru & Muhibah",
        subtitleBm = "Sambutan ceria dengan konfeti emas",
        category = CardCategory.PERAYAAN,
        paperStyle = PaperStyle.BLUSH_CHAMPAGNE,
        frameStyle = FrameStyle.DOUBLE_GOLD,
        animationType = AnimationType.FESTIVE_CONFETTI,
        musicTrack = MusicTrack.GEMA_SYAWAL,
        defaultRecipient = "Rakan & Kenalan",
        defaultGreetingTitle = "Selamat Menyambut Tahun Baharu",
        defaultGreetingBody =
          "Semoga tahun baharu membuka lembaran penuh harapan, kesihatan dan kejayaan berterusan.",
        defaultSender = "Salam Mesra, Faris",
        defaultDate = "1 Januari 2027",
        defaultTime = "8:00 Malam",
        defaultVenue = "Rumah Terbuka Muhibah",
        defaultElements =
          listOf(
            CanvasElement(
              id = "st_lantern",
              type = "STICKER",
              content = StickerOption.LANTERN_GLOW.key,
              x = 0.5,
              y = 0.14,
              scale = 1.0,
              colorHex = "#B8860B",
              zIndex = 1L,
            )
          ),
      ),
    )

  val bilingualGreetings: List<BilingualGreetingPreset> =
    listOf(
      BilingualGreetingPreset(
        id = "g_bday",
        labelBm = "Hari Lahir (BM / EN)",
        category = CardCategory.INDIVIDU,
        titleBm = "Selamat Hari Lahir!",
        bodyBm = "Semoga dikurniakan umur yang berkat, rezeki melimpah dan kebahagiaan berpanjangan.",
        titleEn = "Happy Birthday!",
        bodyEn = "Wishing you a year filled with warmth, laughter, good health, and cherished moments.",
      ),
      BilingualGreetingPreset(
        id = "g_tq",
        labelBm = "Terima Kasih (BM / EN)",
        category = CardCategory.INDIVIDU,
        titleBm = "Terima Kasih",
        bodyBm = "Penghargaan setinggi-tingginya atas bantuan dan keikhlasan hati anda.",
        titleEn = "With Heartfelt Gratitude",
        bodyEn = "Thank you sincerely for your kindness, generosity, and thoughtful support.",
      ),
      BilingualGreetingPreset(
        id = "g_wedding",
        labelBm = "Walimatulurus (BM / EN)",
        category = CardCategory.MAJLIS,
        titleBm = "Jemputan Walimatulurus",
        bodyBm = "Dengan penuh takzim kami menjemput tuan/puan ke majlis perkahwinan kami bagi menyerikan hari bahagia ini.",
        titleEn = "Wedding Invitation",
        bodyEn = "Together with our families, we joyfully invite you to celebrate our union and share in our happiness.",
      ),
      BilingualGreetingPreset(
        id = "g_raya",
        labelBm = "Aidilfitri (BM / EN)",
        category = CardCategory.PERAYAAN,
        titleBm = "Selamat Hari Raya Aidilfitri",
        bodyBm = "Maaf Zahir & Batin. Jemput hadir ke rumah terbuka kami untuk mengeratkan silaturahim.",
        titleEn = "Eid Mubarak",
        bodyEn = "Wishing you peace, joy, and blessed moments with loved ones this festive season.",
      ),
      BilingualGreetingPreset(
        id = "g_biz",
        labelBm = "Profil Perniagaan (BM / EN)",
        category = CardCategory.PERNIAGAAN,
        titleBm = "Mari Bekerjasama",
        bodyBm = "Sedia membantu projek anda dengan penyelesaian kreatif, teliti dan berimpak tinggi.",
        titleEn = "Let's Collaborate",
        bodyEn = "Partnering with ambitious teams to craft memorable digital and brand experiences.",
      ),
    )

  fun createCardFromPreset(preset: CardTemplatePreset): ECard =
    ECard(
      title = preset.nameBm,
      category = preset.category.code,
      templateId = preset.id,
      recipientName = preset.defaultRecipient,
      greetingTitle = preset.defaultGreetingTitle,
      greetingBody = preset.defaultGreetingBody,
      senderName = preset.defaultSender,
      eventDate = preset.defaultDate,
      eventTime = preset.defaultTime,
      eventVenue = preset.defaultVenue,
      businessRole = preset.defaultRole,
      businessCompany = preset.defaultCompany,
      businessPhone = preset.defaultPhone,
      businessEmail = preset.defaultEmail,
      businessWebsite = preset.defaultWebsite,
      paperStyle = preset.paperStyle.code,
      frameStyle = preset.frameStyle.code,
      animationType = preset.animationType.code,
      musicTrack = preset.musicTrack.code,
      rsvpEnabled = preset.category.defaultRsvpEnabled,
      elements = preset.defaultElements,
    )
}
