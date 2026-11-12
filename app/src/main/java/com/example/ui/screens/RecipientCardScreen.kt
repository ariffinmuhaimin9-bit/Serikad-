package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.CardCategory
import com.example.data.model.ECard
import com.example.data.model.GuestStatus
import com.example.data.model.MusicTrack
import com.example.data.model.SharedCard
import com.example.ui.components.CardPreviewCanvas
import com.example.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipientCardScreen(
  lookupState: UiState<SharedCard?>,
  isMusicPlaying: Boolean,
  playingTrack: MusicTrack,
  isSubmitting: Boolean,
  onToggleMusic: (MusicTrack) -> Unit,
  onSubmitFeedback: (
    sharedCard: SharedCard,
    guestName: String,
    status: GuestStatus,
    pax: Long,
    wishes: String,
    onDone: () -> Unit,
  ) -> Unit,
  onClose: () -> Unit,
) {
  BackHandler { onClose() }

  var isEnvelopeOpened by remember { mutableStateOf(false) }
  var responderName by remember { mutableStateOf("") }
  var selectedStatus by remember { mutableStateOf(GuestStatus.HADIR) }
  var paxCount by remember { mutableLongStateOf(1L) }
  var wishesText by remember { mutableStateOf("") }
  var feedbackSubmitted by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Paparan Penerima") },
        navigationIcon = {
          IconButton(onClick = onClose, modifier = Modifier.testTag("recipient_back_btn")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
          }
        },
        actions = {
          val card = (lookupState as? UiState.Success)?.data
          if (card != null) {
            val track = MusicTrack.fromCode(card.musicTrack)
            if (track != MusicTrack.NONE) {
              val active = isMusicPlaying && playingTrack == track
              IconButton(onClick = { onToggleMusic(track) }) {
                Icon(
                  imageVector = if (active) Icons.Filled.Pause else Icons.Filled.MusicNote,
                  contentDescription = "Kawal muzik",
                  tint = MaterialTheme.colorScheme.primary,
                )
              }
            }
          }
        },
      )
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier.fillMaxSize().padding(innerPadding),
      contentAlignment = Alignment.Center,
    ) {
      when (lookupState) {
        UiState.Loading -> {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Membuka e-kad anda…", style = MaterialTheme.typography.bodyMedium)
          }
        }
        is UiState.Error -> {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(lookupState.message, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onClose) { Text("Kembali") }
          }
        }
        is UiState.Success -> {
          val sharedCard = lookupState.data
          if (sharedCard == null) {
            Text("Kad tidak ditemui.")
          } else {
            LaunchedEffect(sharedCard.shareCode) {
              isEnvelopeOpened = true
            }

            val previewECard =
              remember(sharedCard) {
                ECard(
                  id = sharedCard.cardId,
                  userId = sharedCard.ownerId,
                  shareCode = sharedCard.shareCode,
                  title = sharedCard.title,
                  category = sharedCard.category,
                  templateId = sharedCard.templateId,
                  recipientName = sharedCard.recipientName,
                  greetingTitle = sharedCard.greetingTitle,
                  greetingBody = sharedCard.greetingBody,
                  senderName = sharedCard.senderName,
                  eventDate = sharedCard.eventDate,
                  eventTime = sharedCard.eventTime,
                  eventVenue = sharedCard.eventVenue,
                  businessRole = sharedCard.businessRole,
                  businessCompany = sharedCard.businessCompany,
                  businessPhone = sharedCard.businessPhone,
                  businessEmail = sharedCard.businessEmail,
                  businessWebsite = sharedCard.businessWebsite,
                  paperStyle = sharedCard.paperStyle,
                  frameStyle = sharedCard.frameStyle,
                  animationType = sharedCard.animationType,
                  musicTrack = sharedCard.musicTrack,
                  rsvpEnabled = sharedCard.rsvpEnabled,
                  elements = sharedCard.elements,
                )
              }

            val isIndividu = sharedCard.category == CardCategory.INDIVIDU.code
            val showRsvpForm = !isIndividu && sharedCard.rsvpEnabled

            Column(
              modifier =
                Modifier.fillMaxSize()
                  .widthIn(max = 560.dp)
                  .verticalScroll(rememberScrollState())
                  .padding(horizontal = 24.dp, vertical = 16.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
              AnimatedVisibility(
                visible = isEnvelopeOpened,
                enter = fadeIn() + scaleIn(initialScale = 0.92f),
              ) {
                CardPreviewCanvas(
                  card = previewECard,
                  editable = false,
                  modifier = Modifier.widthIn(max = 330.dp).fillMaxWidth(),
                )
              }

              if (feedbackSubmitted) {
                Surface(
                  shape = RoundedCornerShape(16.dp),
                  color = MaterialTheme.colorScheme.primaryContainer,
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Icon(
                      Icons.Filled.CheckCircle,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                      text =
                        if (showRsvpForm) "Terima kasih! Pengesahan RSVP anda telah direkodkan secara langsung."
                        else "Terima kasih! Ucapan balas anda telah dihantar kepada ${sharedCard.senderName}.",
                      style = MaterialTheme.typography.bodyMedium,
                    )
                  }
                }
              } else if (!showRsvpForm) {
                // Dedicated Personal Card View (NO RSVP!)
                Surface(
                  shape = RoundedCornerShape(18.dp),
                  color = MaterialTheme.colorScheme.surface,
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(
                        Icons.Filled.MailOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                      )
                      Spacer(modifier = Modifier.width(8.dp))
                      Text(
                        text =
                          if (isIndividu) "Kad Ucapan Khas Untuk ${sharedCard.recipientName.ifBlank { "Anda" }}"
                          else "Kad Digital ${sharedCard.senderName}",
                        style = MaterialTheme.typography.titleMedium,
                      )
                    }
                    Text(
                      text =
                        if (isIndividu) "Tiada borang RSVP diperlukan. Anda boleh meninggalkan nota terima kasih ringkas kepada pengirim."
                        else "Hantar nota ringkas kepada pengirim jika mahu.",
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                      value = responderName,
                      onValueChange = { responderName = it },
                      label = { Text("Nama Anda") },
                      singleLine = true,
                      modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                      value = wishesText,
                      onValueChange = { wishesText = it },
                      label = { Text("Ucapan Terima Kasih (Pilihan)") },
                      minLines = 2,
                      modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                      onClick = {
                        onSubmitFeedback(
                          sharedCard,
                          responderName.ifBlank { sharedCard.recipientName.ifBlank { "Penerima" } },
                          GuestStatus.DIBUKA,
                          1L,
                          wishesText,
                        ) { feedbackSubmitted = true }
                      },
                      enabled = !isSubmitting,
                      modifier = Modifier.fillMaxWidth().testTag("send_personal_reply_btn"),
                    ) {
                      Text("Hantar Ucapan Balas")
                    }
                  }
                }
              } else {
                // Event / Festive RSVP Form
                Surface(
                  shape = RoundedCornerShape(18.dp),
                  color = MaterialTheme.colorScheme.surface,
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                  ) {
                    Text(
                      text = "Pengesahan Kehadiran (RSVP)",
                      style = MaterialTheme.typography.titleLarge,
                    )
                    OutlinedTextField(
                      value = responderName,
                      onValueChange = { responderName = it },
                      label = { Text("Nama Tetamu") },
                      singleLine = true,
                      modifier = Modifier.fillMaxWidth().testTag("rsvp_name_input"),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                      listOf(GuestStatus.HADIR, GuestStatus.MUNGKIN, GuestStatus.TIDAK_HADIR).forEach { st ->
                        FilterChip(
                          selected = selectedStatus == st,
                          onClick = { selectedStatus = st },
                          label = { Text(st.labelBm) },
                        )
                      }
                    }
                    if (selectedStatus != GuestStatus.TIDAK_HADIR) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                      ) {
                        Text("Bilangan Tetamu (Pax): $paxCount", style = MaterialTheme.typography.labelLarge)
                        Row {
                          OutlinedButton(
                            onClick = { if (paxCount > 1L) paxCount-- },
                            contentPadding = PaddingValues(horizontal = 10.dp),
                          ) {
                            Text("-")
                          }
                          Spacer(modifier = Modifier.width(6.dp))
                          OutlinedButton(
                            onClick = { if (paxCount < 20L) paxCount++ },
                            contentPadding = PaddingValues(horizontal = 10.dp),
                          ) {
                            Icon(Icons.Filled.Add, contentDescription = "Tambah pax", modifier = Modifier.size(16.dp))
                          }
                        }
                      }
                    }
                    OutlinedTextField(
                      value = wishesText,
                      onValueChange = { wishesText = it },
                      label = { Text("Ucapan & Doa Kepada Tuan Rumah") },
                      minLines = 2,
                      modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                      onClick = {
                        onSubmitFeedback(
                          sharedCard,
                          responderName.ifBlank { "Tetamu" },
                          selectedStatus,
                          paxCount,
                          wishesText,
                        ) { feedbackSubmitted = true }
                      },
                      enabled = !isSubmitting,
                      modifier = Modifier.fillMaxWidth().testTag("submit_rsvp_btn"),
                    ) {
                      Text("Hantar Pengesahan RSVP")
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
