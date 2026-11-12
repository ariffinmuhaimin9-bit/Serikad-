package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CardCategory
import com.example.data.model.ECard
import com.example.data.model.GuestEntry
import com.example.data.model.GuestGroup
import com.example.data.model.GuestStatus
import com.example.data.model.SharedRsvp
import com.example.media.CardShareHelper
import com.example.ui.theme.AmberStatus
import com.example.ui.theme.EmeraldStatus
import com.example.ui.theme.RoseTerracotta
import com.example.viewmodel.UiState

@Composable
fun GuestManagementScreen(
  cardsState: UiState<List<ECard>>,
  selectedCardId: String?,
  guestsByCard: Map<String, List<GuestEntry>>,
  rsvpsByShareCode: Map<String, List<SharedRsvp>>,
  onSelectCard: (String) -> Unit,
  onAddGuest: (cardId: String, name: String, phone: String, group: GuestGroup, pax: Long) -> Unit,
  onMarkSent: (cardId: String, GuestEntry) -> Unit,
  onUpdateStatus: (cardId: String, guestId: String, GuestStatus) -> Unit,
  onDeleteGuest: (cardId: String, guestId: String) -> Unit,
  onShowMessage: (String) -> Unit,
  onGoToTemplates: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val cards = (cardsState as? UiState.Success)?.data.orEmpty()
  val activeCard = cards.firstOrNull { it.id == selectedCardId } ?: cards.firstOrNull()

  var statusFilter by remember { mutableStateOf<GuestStatus?>(null) }
  var showAddGuestDialog by remember { mutableStateOf(false) }
  var guestName by remember { mutableStateOf("") }
  var guestPhone by remember { mutableStateOf("") }
  var guestGroup by remember { mutableStateOf(GuestGroup.RAKAN) }
  var guestPax by remember { mutableLongStateOf(1L) }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      Column {
        Text(
          text = "PENGURUSAN JEMPUTAN",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
        )
        Text(
          text = "Senarai Tetamu & Maklum Balas",
          style = MaterialTheme.typography.headlineMedium,
        )
      }
    }

    if (cards.isEmpty() || activeCard == null) {
      item {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text("Tiada Kad Tersimpan", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Cipta dan simpan satu kad terlebih dahulu untuk mengurus senarai tetamu atau menghantar pautan khas.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onGoToTemplates) { Text("Pergi ke Templat") }
          }
        }
      }
      return@LazyColumn
    }

    // Card selector chips
    item {
      Text("Pilih Kad Aktif", style = MaterialTheme.typography.labelLarge)
      Spacer(modifier = Modifier.height(6.dp))
      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(cards, key = { it.id }) { card ->
          FilterChip(
            selected = card.id == activeCard.id,
            onClick = { onSelectCard(card.id) },
            label = { Text(card.title, maxLines = 1) },
          )
        }
      }
    }

    val isIndividu = activeCard.category == CardCategory.INDIVIDU.code
    val isNoRsvp = isIndividu || !activeCard.rsvpEnabled

    if (isNoRsvp) {
      item {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
              text =
                if (isIndividu) "Kad Ucapan Individu — Pautan Khas Penerima"
                else "Kad Tanpa RSVP — Perkongsian Pautan Terus",
              style = MaterialTheme.typography.titleMedium,
            )
            Text(
              text =
                if (isIndividu) {
                  "Tiada borang RSVP untuk kad individu seperti ucapan hari lahir. Kongsi pautan khas terus kepada ${activeCard.recipientName.ifBlank { "penerima" }}:"
                } else {
                  "Kongsi pautan kad ini kepada kenalan atau pelanggan anda."
                },
              style = MaterialTheme.typography.bodyMedium,
            )
            Text(
              text = CardShareHelper.buildShareUrl(activeCard),
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.primary,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                onClick = { CardShareHelper.shareToWhatsApp(context, activeCard) },
                modifier = Modifier.weight(1f),
              ) {
                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("WhatsApp")
              }
              OutlinedButton(
                onClick = {
                  val url = CardShareHelper.copyShareLink(context, activeCard)
                  onShowMessage("Pautan khas disalin: $url")
                },
                modifier = Modifier.weight(1f),
              ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Salin Pautan")
              }
            }
          }
        }
      }
    }

    // Add guest + status filter
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = if (isIndividu) "Rekod Penghantaran Penerima" else "Senarai Tetamu",
          style = MaterialTheme.typography.titleLarge,
        )
        Button(
          onClick = { showAddGuestDialog = true },
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
          modifier = Modifier.testTag("add_guest_btn"),
        ) {
          Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(if (isIndividu) "Tambah Penerima" else "Tambah Tetamu")
        }
      }
    }

    if (!isNoRsvp) {
      item {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          item {
            FilterChip(
              selected = statusFilter == null,
              onClick = { statusFilter = null },
              label = { Text("Semua") },
            )
          }
          items(GuestStatus.entries) { st ->
            FilterChip(
              selected = statusFilter == st,
              onClick = { statusFilter = st },
              label = { Text(st.labelBm) },
            )
          }
        }
      }
    }

    val guests = guestsByCard[activeCard.id].orEmpty()
    val filteredGuests =
      if (statusFilter == null) guests
      else guests.filter { it.status == statusFilter!!.code }

    if (filteredGuests.isEmpty()) {
      item {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text =
              if (isIndividu) "Tambah penerima khas jika ingin menjejak penghantaran kad individu ini."
              else "Belum ada tetamu ditambah. Ketik 'Tambah Tetamu' untuk mula menjemput via WhatsApp.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp),
          )
        }
      }
    } else {
      items(filteredGuests, key = { it.id }) { guest ->
        GuestRowCard(
          guest = guest,
          isNoRsvp = isNoRsvp,
          onSendWhatsApp = {
            onMarkSent(activeCard.id, guest)
            CardShareHelper.shareToWhatsApp(context, activeCard, guest)
          },
          onUpdateStatus = { newStatus -> onUpdateStatus(activeCard.id, guest.id, newStatus) },
          onDelete = { onDeleteGuest(activeCard.id, guest.id) },
        )
      }
    }

    // Incoming Live Shared Responses / Wishes
    val incomingRsvps = rsvpsByShareCode[activeCard.shareCode].orEmpty()
    if (incomingRsvps.isNotEmpty()) {
      item {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text =
            if (isNoRsvp) "Ucapan Balas & Pengesahan Dibuka (${incomingRsvps.size})"
            else "Maklum Balas RSVP Masuk Secara Langsung (${incomingRsvps.size})",
          style = MaterialTheme.typography.titleLarge,
        )
      }
      items(incomingRsvps, key = { it.id }) { rsvp ->
        val st = GuestStatus.fromCode(rsvp.status)
        val badgeColor =
          when (st) {
            GuestStatus.HADIR -> EmeraldStatus
            GuestStatus.TIDAK_HADIR -> RoseTerracotta
            GuestStatus.DIBUKA -> MaterialTheme.colorScheme.primary
            else -> AmberStatus
          }
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(rsvp.guestName, style = MaterialTheme.typography.titleMedium)
              Surface(shape = RoundedCornerShape(50), color = badgeColor.copy(alpha = 0.14f)) {
                val label =
                  if (isNoRsvp) "Kad Dibuka"
                  else if (st == GuestStatus.HADIR) "Hadir (${rsvp.pax} pax)"
                  else st.labelBm
                Text(
                  text = label,
                  style = MaterialTheme.typography.labelSmall,
                  color = badgeColor,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
              }
            }
            if (rsvp.wishes.isNotBlank()) {
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "\"${rsvp.wishes}\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }
    }
  }

  if (showAddGuestDialog && activeCard != null) {
    AlertDialog(
      onDismissRequest = { showAddGuestDialog = false },
      title = { Text("Tambah Tetamu Baharu") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = guestName,
            onValueChange = { guestName = it },
            label = { Text("Nama Penuh") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("guest_name_input"),
          )
          OutlinedTextField(
            value = guestPhone,
            onValueChange = { guestPhone = it },
            label = { Text("No. Telefon / WhatsApp (Pilihan)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
          Text("Kumpulan", style = MaterialTheme.typography.labelLarge)
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(GuestGroup.entries) { grp ->
              FilterChip(
                selected = guestGroup == grp,
                onClick = { guestGroup = grp },
                label = { Text(grp.labelBm) },
              )
            }
          }
          if (!activeCard.category.equals(CardCategory.INDIVIDU.code)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text("Bilangan Pax: $guestPax", style = MaterialTheme.typography.labelLarge)
              Row {
                OutlinedButton(
                  onClick = { if (guestPax > 1L) guestPax-- },
                  contentPadding = PaddingValues(horizontal = 10.dp),
                ) {
                  Text("-")
                }
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedButton(
                  onClick = { if (guestPax < 20L) guestPax++ },
                  contentPadding = PaddingValues(horizontal = 10.dp),
                ) {
                  Icon(Icons.Filled.Add, contentDescription = "Tambah pax", modifier = Modifier.size(16.dp))
                }
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onAddGuest(activeCard.id, guestName, guestPhone, guestGroup, guestPax)
            guestName = ""
            guestPhone = ""
            guestPax = 1L
            showAddGuestDialog = false
          },
          modifier = Modifier.testTag("confirm_add_guest_btn"),
        ) {
          Text("Simpan")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddGuestDialog = false }) { Text("Batal") }
      },
    )
  }
}

@Composable
private fun GuestRowCard(
  guest: GuestEntry,
  isNoRsvp: Boolean,
  onSendWhatsApp: () -> Unit,
  onUpdateStatus: (GuestStatus) -> Unit,
  onDelete: () -> Unit,
) {
  var showStatusMenu by remember { mutableStateOf(false) }
  val status = GuestStatus.fromCode(guest.status)
  val group = GuestGroup.fromCode(guest.group)
  val statusColor =
    when (status) {
      GuestStatus.HADIR -> EmeraldStatus
      GuestStatus.TIDAK_HADIR -> RoseTerracotta
      GuestStatus.DIBUKA -> MaterialTheme.colorScheme.primary
      else -> AmberStatus
    }

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = guest.name,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text =
              if (isNoRsvp) "${group.labelBm} • Kod ${guest.inviteCode}"
              else "${group.labelBm} • ${guest.pax} pax • ${guest.phone.ifBlank { "Tiada no." }}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Surface(shape = RoundedCornerShape(50), color = statusColor.copy(alpha = 0.14f)) {
          Text(
            text = status.labelBm,
            style = MaterialTheme.typography.labelSmall,
            color = statusColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Button(
          onClick = onSendWhatsApp,
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier.height(36.dp),
        ) {
          Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Hantar WhatsApp", style = MaterialTheme.typography.labelLarge)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Box {
            IconButton(onClick = { showStatusMenu = true }) {
              Icon(Icons.Filled.MoreVert, contentDescription = "Tukar status tetamu")
            }
            DropdownMenu(
              expanded = showStatusMenu,
              onDismissRequest = { showStatusMenu = false },
            ) {
              val options =
                if (isNoRsvp) listOf(GuestStatus.DRAF, GuestStatus.DIHANTAR, GuestStatus.DIBUKA)
                else GuestStatus.entries
              options.forEach { opt ->
                DropdownMenuItem(
                  text = { Text("Status: ${opt.labelBm}") },
                  onClick = {
                    showStatusMenu = false
                    onUpdateStatus(opt)
                  },
                )
              }
            }
          }
          IconButton(onClick = onDelete) {
            Icon(
              Icons.Filled.DeleteOutline,
              contentDescription = "Padam tetamu",
              tint = MaterialTheme.colorScheme.error,
            )
          }
        }
      }
    }
  }
}
