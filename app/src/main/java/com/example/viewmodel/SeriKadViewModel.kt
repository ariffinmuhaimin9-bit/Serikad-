package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AnimationType
import com.example.data.model.CanvasElement
import com.example.data.model.CardCategory
import com.example.data.model.ECard
import com.example.data.model.FrameStyle
import com.example.data.model.GuestEntry
import com.example.data.model.GuestGroup
import com.example.data.model.GuestStatus
import com.example.data.model.MusicTrack
import com.example.data.model.PaperStyle
import com.example.data.model.SharedCard
import com.example.data.model.SharedRsvp
import com.example.data.repository.CardRepository
import com.example.data.templates.CardTemplatePreset
import com.example.data.templates.CardTemplatesCatalog
import com.example.data.templates.StickerOption
import com.example.media.AmbientMusicPlayer
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>
  data class Success<T>(val data: T) : UiState<T>
  data class Error(val message: String) : UiState<Nothing>
}

data class DashboardActivityItem(
  val id: String,
  val cardTitle: String,
  val cardCategory: CardCategory,
  val guestName: String,
  val status: GuestStatus,
  val pax: Long,
  val note: String,
  val timestampSeconds: Long,
)

data class DashboardSummary(
  val totalCards: Int = 0,
  val totalGuests: Int = 0,
  val totalSent: Int = 0,
  val totalOpened: Int = 0,
  val totalAttending: Int = 0,
  val totalAttendingPax: Int = 0,
  val totalMaybe: Int = 0,
  val totalDeclined: Int = 0,
  val totalPending: Int = 0,
  val recentActivity: List<DashboardActivityItem> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class SeriKadViewModel(
  private val repository: CardRepository,
  private val currentUserId: String,
  private val initialDisplayName: String,
) : ViewModel() {

  private val musicPlayer = AmbientMusicPlayer()

  private val _snackbarMessage = MutableStateFlow<String?>(null)
  val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

  private val _editorCard =
    MutableStateFlow(
      CardTemplatesCatalog.createCardFromPreset(CardTemplatesCatalog.templates.first())
        .copy(senderName = initialDisplayName.ifBlank { "Pengirim SeriKad" })
    )
  val editorCard: StateFlow<ECard> = _editorCard.asStateFlow()

  private val _selectedElementId = MutableStateFlow<String?>(null)
  val selectedElementId: StateFlow<String?> = _selectedElementId.asStateFlow()

  private val _isSavingCard = MutableStateFlow(false)
  val isSavingCard: StateFlow<Boolean> = _isSavingCard.asStateFlow()

  private val _selectedManageCardId = MutableStateFlow<String?>(null)
  val selectedManageCardId: StateFlow<String?> = _selectedManageCardId.asStateFlow()

  private val _isMusicPlaying = MutableStateFlow(false)
  val isMusicPlaying: StateFlow<Boolean> = _isMusicPlaying.asStateFlow()

  private val _playingTrack = MutableStateFlow(MusicTrack.NONE)
  val playingTrack: StateFlow<MusicTrack> = _playingTrack.asStateFlow()

  private val _recipientLookup = MutableStateFlow<UiState<SharedCard?>?>(null)
  val recipientLookup: StateFlow<UiState<SharedCard?>?> = _recipientLookup.asStateFlow()

  private val _isSubmittingRsvp = MutableStateFlow(false)
  val isSubmittingRsvp: StateFlow<Boolean> = _isSubmittingRsvp.asStateFlow()

  val cardsState: StateFlow<UiState<List<ECard>>> =
    repository
      .observeUserCards(currentUserId)
      .map<List<ECard>, UiState<List<ECard>>> { cards ->
        val sorted =
          cards.sortedByDescending { (it.updatedAt ?: it.createdAt ?: Timestamp(0, 0)).seconds }
        UiState.Success(sorted)
      }
      .catch { error ->
        val firestoreError =
          generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w(TAG, "Error observing user cards", rootError)
        emit(UiState.Error(rootError.message ?: "Gagal memuatkan senarai kad."))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = UiState.Loading,
      )

  val guestsByCard: StateFlow<Map<String, List<GuestEntry>>> =
    cardsState
      .flatMapLatest { state ->
        val cards = (state as? UiState.Success)?.data.orEmpty()
        if (cards.isEmpty()) {
          flowOf(emptyMap())
        } else {
          val flows: List<Flow<Pair<String, List<GuestEntry>>>> =
            cards.map { card ->
              repository
                .observeCardGuests(currentUserId, card.id)
                .map { guests ->
                  card.id to
                    guests.sortedByDescending {
                      (it.updatedAt ?: it.createdAt ?: Timestamp(0, 0)).seconds
                    }
                }
                .catch { emit(card.id to emptyList()) }
            }
          combine(flows) { pairs -> pairs.toMap() }
        }
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = emptyMap(),
      )

  val rsvpsByShareCode: StateFlow<Map<String, List<SharedRsvp>>> =
    cardsState
      .flatMapLatest { state ->
        val cards = (state as? UiState.Success)?.data.orEmpty().filter { it.shareCode.isNotBlank() }
        if (cards.isEmpty()) {
          flowOf(emptyMap())
        } else {
          val flows: List<Flow<Pair<String, List<SharedRsvp>>>> =
            cards.map { card ->
              repository
                .observeSharedRsvps(currentUserId, card.shareCode)
                .map { list ->
                  card.shareCode to
                    list.sortedByDescending {
                      (it.updatedAt ?: it.createdAt ?: Timestamp(0, 0)).seconds
                    }
                }
                .catch { emit(card.shareCode to emptyList()) }
            }
          combine(flows) { pairs -> pairs.toMap() }
        }
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = emptyMap(),
      )

  val dashboardSummary: StateFlow<DashboardSummary> =
    combine(cardsState, guestsByCard, rsvpsByShareCode) { cardsUi, guestsMap, rsvpsMap ->
      val cards = (cardsUi as? UiState.Success)?.data.orEmpty()
      val cardMap = cards.associateBy { it.id }
      val allGuests = guestsMap.values.flatten()
      val allRsvps = rsvpsMap.values.flatten()

      var sent = 0
      var opened = 0
      var attending = 0
      var attendingPax = 0
      var maybe = 0
      var declined = 0
      var pending = 0

      allGuests.forEach { guest ->
        when (GuestStatus.fromCode(guest.status)) {
          GuestStatus.DRAF -> pending++
          GuestStatus.DIHANTAR -> {
            sent++
            pending++
          }
          GuestStatus.DIBUKA -> {
            opened++
            pending++
          }
          GuestStatus.HADIR -> {
            attending++
            attendingPax += guest.pax.toInt()
          }
          GuestStatus.MUNGKIN -> maybe++
          GuestStatus.TIDAK_HADIR -> declined++
        }
      }

      // Include live incoming SharedRsvp responses not yet manually duplicated
      allRsvps.forEach { rsvp ->
        when (GuestStatus.fromCode(rsvp.status)) {
          GuestStatus.DIBUKA -> opened++
          GuestStatus.HADIR -> {
            attending++
            attendingPax += rsvp.pax.toInt()
          }
          GuestStatus.MUNGKIN -> maybe++
          GuestStatus.TIDAK_HADIR -> declined++
          else -> Unit
        }
      }

      val guestActivity =
        allGuests.map { g ->
          val parentCard = cardMap[g.cardId]
          DashboardActivityItem(
            id = "g_${g.id}",
            cardTitle = parentCard?.title ?: "Kad",
            cardCategory = CardCategory.fromCode(parentCard?.category ?: CardCategory.INDIVIDU.code),
            guestName = g.name,
            status = GuestStatus.fromCode(g.status),
            pax = g.pax,
            note = g.note,
            timestampSeconds = (g.updatedAt ?: g.createdAt ?: Timestamp(0, 0)).seconds,
          )
        }

      val rsvpActivity =
        allRsvps.map { r ->
          val parentCard = cardMap[r.cardId]
          DashboardActivityItem(
            id = "r_${r.id}",
            cardTitle = parentCard?.title ?: "Kad Kongsi",
            cardCategory = CardCategory.fromCode(parentCard?.category ?: CardCategory.MAJLIS.code),
            guestName = r.guestName,
            status = GuestStatus.fromCode(r.status),
            pax = r.pax,
            note = r.wishes,
            timestampSeconds = (r.updatedAt ?: r.createdAt ?: Timestamp(0, 0)).seconds,
          )
        }

      DashboardSummary(
        totalCards = cards.size,
        totalGuests = allGuests.size + allRsvps.size,
        totalSent = sent,
        totalOpened = opened,
        totalAttending = attending,
        totalAttendingPax = attendingPax,
        totalMaybe = maybe,
        totalDeclined = declined,
        totalPending = pending,
        recentActivity =
          (rsvpActivity + guestActivity).sortedByDescending { it.timestampSeconds }.take(15),
      )
    }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = DashboardSummary(),
      )

  init {
    viewModelScope.launch {
      try {
        repository.ensureUserProfile(initialDisplayName)
      } catch (e: Exception) {
        Log.w(TAG, "Profile sync warning", e)
      }
    }
  }

  fun clearSnackbar() {
    _snackbarMessage.value = null
  }

  fun showSnackbar(message: String) {
    _snackbarMessage.value = message
  }

  fun selectManageCard(cardId: String) {
    _selectedManageCardId.value = cardId
  }

  fun loadTemplateIntoEditor(preset: CardTemplatePreset) {
    _editorCard.value =
      CardTemplatesCatalog.createCardFromPreset(preset)
        .copy(
          senderName =
            preset.defaultSender.ifBlank { initialDisplayName.ifBlank { "Pengirim SeriKad" } }
        )
    _selectedElementId.value = null
  }

  fun startBlankCard(category: CardCategory) {
    _editorCard.value =
      ECard(
        title = "Kad ${category.labelBm}",
        category = category.code,
        templateId = "custom_${category.code.lowercase()}",
        recipientName = if (category == CardCategory.INDIVIDU) "Sahabat Istimewa" else "",
        greetingTitle = "Ucapan Istimewa",
        greetingBody = "Tulis ucapan atau butiran jemputan anda di sini.",
        senderName = initialDisplayName.ifBlank { "Pengirim SeriKad" },
        paperStyle = PaperStyle.IVORY_COTTON.code,
        frameStyle = FrameStyle.DOUBLE_GOLD.code,
        animationType = AnimationType.GOLD_PETALS.code,
        musicTrack = MusicTrack.PIANO_MALAM.code,
        rsvpEnabled = category.defaultRsvpEnabled,
      )
    _selectedElementId.value = null
  }

  fun editExistingCard(card: ECard) {
    _editorCard.value = card
    _selectedElementId.value = null
  }

  fun updateEditorCard(transform: (ECard) -> ECard) {
    val updated = transform(_editorCard.value)
    // Enforce user's rule: Individual greeting cards never have RSVP!
    val enforced =
      if (updated.category == CardCategory.INDIVIDU.code) {
        updated.copy(rsvpEnabled = false)
      } else {
        updated
      }
    _editorCard.value = enforced
  }

  fun selectCanvasElement(elementId: String?) {
    _selectedElementId.value = elementId
  }

  fun addTextElement(text: String) {
    val current = _editorCard.value
    if (current.elements.size >= 25) {
      showSnackbar("Maksimum 25 elemen kanvas dicapai.")
      return
    }
    val newId = "el_${UUID.randomUUID().toString().take(6)}"
    val isDark =
      current.paperStyle == PaperStyle.OBSIDIAN_GOLD.code ||
        current.paperStyle == PaperStyle.EMERALD_SONGKET.code
    val newEl =
      CanvasElement(
        id = newId,
        type = "TEXT",
        content = text.ifBlank { "Teks Baharu" },
        x = 0.5,
        y = 0.25,
        scale = 1.0,
        rotation = 0.0,
        fontKey = "DANCING",
        colorHex = if (isDark) "#E5C158" else "#B8860B",
        fontSizeSp = 18.0,
        zIndex = (current.elements.maxOfOrNull { it.zIndex } ?: 0L) + 1L,
      )
    _editorCard.value = current.copy(elements = current.elements + newEl)
    _selectedElementId.value = newId
  }

  fun addStickerElement(sticker: StickerOption) {
    val current = _editorCard.value
    if (current.elements.size >= 25) {
      showSnackbar("Maksimum 25 elemen kanvas dicapai.")
      return
    }
    val newId = "st_${UUID.randomUUID().toString().take(6)}"
    val isDark =
      current.paperStyle == PaperStyle.OBSIDIAN_GOLD.code ||
        current.paperStyle == PaperStyle.EMERALD_SONGKET.code
    val newEl =
      CanvasElement(
        id = newId,
        type = "STICKER",
        content = sticker.key,
        x = 0.5,
        y = 0.2,
        scale = 1.0,
        rotation = 0.0,
        colorHex = if (isDark) "#E5C158" else "#B8860B",
        zIndex = (current.elements.maxOfOrNull { it.zIndex } ?: 0L) + 1L,
      )
    _editorCard.value = current.copy(elements = current.elements + newEl)
    _selectedElementId.value = newId
  }

  fun addPhotoElement(uriString: String) {
    val current = _editorCard.value
    if (current.elements.size >= 25) {
      showSnackbar("Maksimum 25 elemen kanvas dicapai.")
      return
    }
    val newId = "ph_${UUID.randomUUID().toString().take(6)}"
    val newEl =
      CanvasElement(
        id = newId,
        type = "PHOTO",
        content = uriString,
        x = 0.5,
        y = 0.28,
        scale = 1.0,
        rotation = 0.0,
        zIndex = (current.elements.maxOfOrNull { it.zIndex } ?: 0L) + 1L,
      )
    _editorCard.value = current.copy(elements = current.elements + newEl)
    _selectedElementId.value = newId
  }

  fun moveElement(elementId: String, deltaNormX: Float, deltaNormY: Float) {
    val current = _editorCard.value
    val updated =
      current.elements.map { el ->
        if (el.id == elementId) {
          el.copy(
            x = (el.x + deltaNormX).coerceIn(0.08, 0.92),
            y = (el.y + deltaNormY).coerceIn(0.08, 0.92),
          )
        } else {
          el
        }
      }
    _editorCard.value = current.copy(elements = updated)
  }

  fun updateSelectedElement(transform: (CanvasElement) -> CanvasElement) {
    val targetId = _selectedElementId.value ?: return
    val current = _editorCard.value
    _editorCard.value =
      current.copy(
        elements =
          current.elements.map { if (it.id == targetId) transform(it) else it }
      )
  }

  fun deleteSelectedElement() {
    val targetId = _selectedElementId.value ?: return
    val current = _editorCard.value
    _editorCard.value =
      current.copy(elements = current.elements.filterNot { it.id == targetId })
    _selectedElementId.value = null
  }

  fun saveCurrentCard(onSaved: (ECard) -> Unit = {}) {
    if (_isSavingCard.value) return
    val draft = _editorCard.value
    if (draft.title.isBlank()) {
      showSnackbar("Sila masukkan tajuk kad sebelum menyimpan.")
      return
    }
    _isSavingCard.value = true
    viewModelScope.launch {
      try {
        val saved = repository.saveCard(draft)
        _editorCard.value = saved
        _selectedManageCardId.value = saved.id
        showSnackbar("Kad berjaya disimpan ke awan!")
        onSaved(saved)
      } catch (e: Exception) {
        showSnackbar("Gagal menyimpan kad: ${e.localizedMessage ?: "Ralat rangkaian"}")
      } finally {
        _isSavingCard.value = false
      }
    }
  }

  fun deleteCard(card: ECard) {
    viewModelScope.launch {
      try {
        repository.deleteCard(card)
        if (_selectedManageCardId.value == card.id) {
          _selectedManageCardId.value = null
        }
        showSnackbar("Kad '${card.title}' dipadam.")
      } catch (e: Exception) {
        showSnackbar("Gagal memadam kad: ${e.localizedMessage ?: "Ralat"}")
      }
    }
  }

  fun addGuest(
    cardId: String,
    name: String,
    phone: String,
    group: GuestGroup,
    pax: Long,
  ) {
    if (name.isBlank()) {
      showSnackbar("Sila masukkan nama tetamu/penerima.")
      return
    }
    viewModelScope.launch {
      try {
        repository.addOrUpdateGuest(
          cardId = cardId,
          guest =
            GuestEntry(
              name = name.trim(),
              phone = phone.trim(),
              group = group.code,
              status = GuestStatus.DRAF.code,
              pax = pax.coerceIn(1L, 20L),
            ),
        )
        showSnackbar("$name ditambah ke senarai.")
      } catch (e: Exception) {
        showSnackbar("Gagal menambah tetamu: ${e.localizedMessage ?: "Ralat"}")
      }
    }
  }

  fun markGuestSent(cardId: String, guest: GuestEntry) {
    if (guest.status == GuestStatus.DRAF.code) {
      updateGuestStatus(cardId, guest.id, GuestStatus.DIHANTAR)
    }
  }

  fun updateGuestStatus(cardId: String, guestId: String, status: GuestStatus, pax: Long? = null) {
    viewModelScope.launch {
      try {
        repository.updateGuestStatus(cardId, guestId, status, pax)
      } catch (e: Exception) {
        showSnackbar("Gagal mengemas kini status: ${e.localizedMessage ?: "Ralat"}")
      }
    }
  }

  fun deleteGuest(cardId: String, guestId: String) {
    viewModelScope.launch {
      try {
        repository.deleteGuest(cardId, guestId)
        showSnackbar("Tetamu dipadam.")
      } catch (e: Exception) {
        showSnackbar("Gagal memadam tetamu.")
      }
    }
  }

  fun toggleMusic(track: MusicTrack) {
    if (track == MusicTrack.NONE) {
      stopMusic()
      return
    }
    if (_isMusicPlaying.value && _playingTrack.value == track) {
      stopMusic()
    } else {
      musicPlayer.play(track)
      _playingTrack.value = track
      _isMusicPlaying.value = true
    }
  }

  fun stopMusic() {
    musicPlayer.stop()
    _isMusicPlaying.value = false
    _playingTrack.value = MusicTrack.NONE
  }

  fun previewLocalCardAsRecipient(card: ECard) {
    _recipientLookup.value =
      UiState.Success(
        SharedCard(
          id = card.shareCode,
          ownerId = card.userId.ifBlank { currentUserId },
          cardId = card.id,
          shareCode = card.shareCode.ifBlank { "PRATONTON" },
          title = card.title,
          category = card.category,
          templateId = card.templateId,
          recipientName = card.recipientName,
          greetingTitle = card.greetingTitle,
          greetingBody = card.greetingBody,
          senderName = card.senderName,
          eventDate = card.eventDate,
          eventTime = card.eventTime,
          eventVenue = card.eventVenue,
          businessRole = card.businessRole,
          businessCompany = card.businessCompany,
          businessPhone = card.businessPhone,
          businessEmail = card.businessEmail,
          businessWebsite = card.businessWebsite,
          paperStyle = card.paperStyle,
          frameStyle = card.frameStyle,
          animationType = card.animationType,
          musicTrack = card.musicTrack,
          rsvpEnabled = if (card.category == CardCategory.INDIVIDU.code) false else card.rsvpEnabled,
          elements = card.elements,
        )
      )
  }

  fun openSharedCardByCode(rawCodeOrUrl: String) {
    val cleanCode = extractShareCode(rawCodeOrUrl)
    if (cleanCode.isBlank()) {
      showSnackbar("Sila masukkan kod kad yang sah.")
      return
    }
    _recipientLookup.value = UiState.Loading
    viewModelScope.launch {
      try {
        val shared = repository.getSharedCardByCode(cleanCode)
        if (shared == null) {
          _recipientLookup.value = UiState.Error("Kad dengan kod '$cleanCode' tidak ditemui.")
        } else {
          _recipientLookup.value = UiState.Success(shared)
          val track = MusicTrack.fromCode(shared.musicTrack)
          if (track != MusicTrack.NONE) {
            musicPlayer.play(track)
            _playingTrack.value = track
            _isMusicPlaying.value = true
          }
        }
      } catch (e: Exception) {
        _recipientLookup.value =
          UiState.Error(e.localizedMessage ?: "Gagal membuka kad kongsi.")
      }
    }
  }

  fun closeRecipientView() {
    stopMusic()
    _recipientLookup.value = null
  }

  fun submitRecipientFeedback(
    sharedCard: SharedCard,
    guestName: String,
    status: GuestStatus,
    pax: Long,
    wishes: String,
    onDone: () -> Unit,
  ) {
    if (sharedCard.cardId.isBlank() || sharedCard.shareCode == "PRATONTON") {
      showSnackbar("Sila simpan kad terlebih dahulu untuk menguji penghantaran RSVP awan.")
      onDone()
      return
    }
    _isSubmittingRsvp.value = true
    viewModelScope.launch {
      try {
        repository.submitSharedFeedback(
          sharedCard = sharedCard,
          guestName = guestName,
          status = status,
          pax = pax,
          wishes = wishes,
        )
        val msg =
          if (sharedCard.category == CardCategory.INDIVIDU.code || !sharedCard.rsvpEnabled) {
            "Ucapan balas anda telah dihantar kepada ${sharedCard.senderName}!"
          } else {
            "Maklum balas RSVP anda telah direkodkan secara langsung!"
          }
        showSnackbar(msg)
        onDone()
      } catch (e: Exception) {
        showSnackbar("Gagal menghantar maklum balas: ${e.localizedMessage ?: "Ralat"}")
      } finally {
        _isSubmittingRsvp.value = false
      }
    }
  }

  private fun extractShareCode(raw: String): String {
    val trimmed = raw.trim()
    val withoutQuery = trimmed.substringBefore("?")
    val lastSegment = withoutQuery.substringAfterLast("/").trim()
    return lastSegment.ifBlank { trimmed }.filter { it.isLetterOrDigit() || it == '_' || it == '-' }
  }

  override fun onCleared() {
    super.onCleared()
    musicPlayer.stop()
  }

  private companion object {
    const val TAG = "SeriKadVM"
    const val STOP_TIMEOUT_MS = 5000L
  }
}
