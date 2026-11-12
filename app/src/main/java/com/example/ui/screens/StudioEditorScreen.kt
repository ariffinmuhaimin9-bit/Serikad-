package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AnimationType
import com.example.data.model.CanvasElement
import com.example.data.model.CardCategory
import com.example.data.model.ECard
import com.example.data.model.FrameStyle
import com.example.data.model.MusicTrack
import com.example.data.model.PaperStyle
import com.example.data.templates.BilingualGreetingPreset
import com.example.data.templates.CardTemplatesCatalog
import com.example.data.templates.StickerOption
import com.example.media.CardShareHelper
import com.example.ui.components.CardPreviewCanvas
import com.example.ui.components.stickerIconFor
import com.example.ui.theme.CardFontOption

@Composable
fun StudioEditorScreen(
  card: ECard,
  selectedElementId: String?,
  isSaving: Boolean,
  isMusicPlaying: Boolean,
  playingTrack: MusicTrack,
  onUpdateCard: ((ECard) -> ECard) -> Unit,
  onSelectElement: (String?) -> Unit,
  onMoveElement: (String, Float, Float) -> Unit,
  onAddText: (String) -> Unit,
  onAddSticker: (StickerOption) -> Unit,
  onAddPhoto: (String) -> Unit,
  onUpdateSelectedElement: ((CanvasElement) -> CanvasElement) -> Unit,
  onDeleteSelectedElement: () -> Unit,
  onToggleMusic: (MusicTrack) -> Unit,
  onSaveCard: ((ECard) -> Unit) -> Unit,
  onPreviewRecipient: (ECard) -> Unit,
  onShowMessage: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  var activeToolTab by remember { mutableIntStateOf(0) }
  var showAddTextDialog by remember { mutableStateOf(false) }
  var newTextInput by remember { mutableStateOf("") }
  var showBilingualDialog by remember { mutableStateOf(false) }

  val photoPickerLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
      if (uri != null) {
        onAddPhoto(uri.toString())
      }
    }

  val selectedElement =
    remember(card.elements, selectedElementId) {
      card.elements.firstOrNull { it.id == selectedElementId }
    }

  Column(
    modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 20.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    // Top Header Bar
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "STUDIO KANVAS BEBAS",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
        )
        Text(
          text = card.title.ifBlank { "Kad Tanpa Tajuk" },
          style = MaterialTheme.typography.titleLarge,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        val currentTrack = MusicTrack.fromCode(card.musicTrack)
        if (currentTrack != MusicTrack.NONE) {
          val playingThis = isMusicPlaying && playingTrack == currentTrack
          IconButton(onClick = { onToggleMusic(currentTrack) }) {
            Icon(
              imageVector = if (playingThis) Icons.Filled.Pause else Icons.Filled.MusicNote,
              contentDescription = "Mainkan muzik latar",
              tint = MaterialTheme.colorScheme.primary,
            )
          }
        }
        IconButton(onClick = { onPreviewRecipient(card) }) {
          Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "Pratonton penuh",
            tint = MaterialTheme.colorScheme.primary,
          )
        }
        Button(
          onClick = { onSaveCard {} },
          enabled = !isSaving,
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
          modifier = Modifier.testTag("studio_save_btn"),
        ) {
          if (isSaving) {
            CircularProgressIndicator(
              modifier = Modifier.size(16.dp),
              strokeWidth = 2.dp,
              color = MaterialTheme.colorScheme.onPrimary,
            )
          } else {
            Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Simpan")
          }
        }
      }
    }

    // Live Interactive Card Preview Canvas
    Box(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
      contentAlignment = Alignment.Center,
    ) {
      CardPreviewCanvas(
        card = card,
        editable = true,
        selectedElementId = selectedElementId,
        onSelectElement = onSelectElement,
        onMoveElement = onMoveElement,
        modifier = Modifier.widthIn(max = 290.dp).fillMaxWidth(),
      )
    }

    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "Tip: Sentuh & seret teks, stiker atau foto di atas kanvas untuk ubah posisi.",
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Tool Tabs
    val tabs =
      listOf(
        "Kandungan" to Icons.Filled.EditNote,
        "Kanvas" to Icons.Filled.Layers,
        "Kertas" to Icons.Filled.Palette,
        "Animasi" to Icons.Filled.AutoAwesome,
        "Kongsi" to Icons.Filled.Share,
      )

    TabRow(selectedTabIndex = activeToolTab) {
      tabs.forEachIndexed { idx, (label, icon) ->
        Tab(
          selected = activeToolTab == idx,
          onClick = { activeToolTab = idx },
          text = { Text(label, maxLines = 1, style = MaterialTheme.typography.labelSmall) },
          icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp)) },
        )
      }
    }

    // Tool Panel Body
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      when (activeToolTab) {
        0 -> ContentToolPanel(
          card = card,
          onUpdateCard = onUpdateCard,
          onOpenBilingualPicker = { showBilingualDialog = true },
        )
        1 -> CanvasToolPanel(
          selectedElement = selectedElement,
          onOpenAddText = {
            newTextInput = ""
            showAddTextDialog = true
          },
          onAddSticker = onAddSticker,
          onPickPhoto = {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          },
          onUpdateSelectedElement = onUpdateSelectedElement,
          onDeleteSelectedElement = onDeleteSelectedElement,
        )
        2 -> PaperAndFrameToolPanel(card = card, onUpdateCard = onUpdateCard)
        3 -> AnimationAndMusicToolPanel(
          card = card,
          isMusicPlaying = isMusicPlaying,
          playingTrack = playingTrack,
          onUpdateCard = onUpdateCard,
          onToggleMusic = onToggleMusic,
        )
        4 -> ShareToolPanel(
          card = card,
          isSaving = isSaving,
          onSaveAndRun = { action ->
            if (card.id.isBlank()) {
              onSaveCard { saved -> action(saved) }
            } else {
              action(card)
            }
          },
          onShowMessage = onShowMessage,
          context = context,
        )
      }
    }
  }

  if (showAddTextDialog) {
    AlertDialog(
      onDismissRequest = { showAddTextDialog = false },
      title = { Text("Tambah Teks Bebas") },
      text = {
        OutlinedTextField(
          value = newTextInput,
          onValueChange = { newTextInput = it },
          label = { Text("Teks / Nota Hiasan") },
          modifier = Modifier.fillMaxWidth(),
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onAddText(newTextInput)
            showAddTextDialog = false
          }
        ) {
          Text("Tambah")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddTextDialog = false }) { Text("Batal") }
      },
    )
  }

  if (showBilingualDialog) {
    AlertDialog(
      onDismissRequest = { showBilingualDialog = false },
      title = { Text("Ucapan Sedia Ada (Dwibahasa)") },
      text = {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          CardTemplatesCatalog.bilingualGreetings.forEach { preset: BilingualGreetingPreset ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text(preset.labelBm, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  OutlinedButton(
                    onClick = {
                      onUpdateCard {
                        it.copy(greetingTitle = preset.titleBm, greetingBody = preset.bodyBm)
                      }
                      showBilingualDialog = false
                    },
                    modifier = Modifier.weight(1f),
                  ) {
                    Text("Bahasa Melayu")
                  }
                  OutlinedButton(
                    onClick = {
                      onUpdateCard {
                        it.copy(greetingTitle = preset.titleEn, greetingBody = preset.bodyEn)
                      }
                      showBilingualDialog = false
                    },
                    modifier = Modifier.weight(1f),
                  ) {
                    Text("English")
                  }
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showBilingualDialog = false }) { Text("Tutup") }
      },
    )
  }
}

@Composable
private fun ContentToolPanel(
  card: ECard,
  onUpdateCard: ((ECard) -> ECard) -> Unit,
  onOpenBilingualPicker: () -> Unit,
) {
  val cat = CardCategory.fromCode(card.category)

  Text("Kategori Kad", style = MaterialTheme.typography.titleMedium)
  LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    items(CardCategory.entries) { option ->
      FilterChip(
        selected = cat == option,
        onClick = {
          onUpdateCard {
            it.copy(
              category = option.code,
              rsvpEnabled = option.defaultRsvpEnabled,
            )
          }
        },
        label = { Text(option.labelBm) },
      )
    }
  }

  if (cat == CardCategory.INDIVIDU) {
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(
        text =
          "Mod Ucapan Individu: Tiada borang RSVP. Hanya pautan khas peribadi dijana untuk penerima kad ini.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.padding(12.dp),
      )
    }
  } else if (cat == CardCategory.MAJLIS || cat == CardCategory.PERAYAAN) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text("Aktifkan Borang RSVP Tetamu", style = MaterialTheme.typography.titleMedium)
        Text(
          "Penerima boleh mengesahkan kehadiran secara langsung.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Switch(
        checked = card.rsvpEnabled,
        onCheckedChange = { enabled -> onUpdateCard { it.copy(rsvpEnabled = enabled) } },
      )
    }
  }

  OutlinedButton(
    onClick = onOpenBilingualPicker,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Text("Pilih Templat Ucapan Dwibahasa (BM / EN)")
  }

  OutlinedTextField(
    value = card.title,
    onValueChange = { v -> onUpdateCard { it.copy(title = v) } },
    label = { Text("Tajuk Projek Kad") },
    singleLine = true,
    modifier = Modifier.fillMaxWidth(),
  )
  OutlinedTextField(
    value = card.recipientName,
    onValueChange = { v -> onUpdateCard { it.copy(recipientName = v) } },
    label = { Text(if (cat == CardCategory.INDIVIDU) "Nama Penerima Khas" else "Kepada / Sasaran") },
    singleLine = true,
    modifier = Modifier.fillMaxWidth(),
  )
  OutlinedTextField(
    value = card.greetingTitle,
    onValueChange = { v -> onUpdateCard { it.copy(greetingTitle = v) } },
    label = { Text("Tajuk Utama Pada Kad") },
    modifier = Modifier.fillMaxWidth(),
  )
  OutlinedTextField(
    value = card.greetingBody,
    onValueChange = { v -> onUpdateCard { it.copy(greetingBody = v) } },
    label = { Text("Mesej Ucapan / Butiran") },
    minLines = 3,
    modifier = Modifier.fillMaxWidth(),
  )
  OutlinedTextField(
    value = card.senderName,
    onValueChange = { v -> onUpdateCard { it.copy(senderName = v) } },
    label = { Text("Nama Pengirim / Tuan Rumah") },
    singleLine = true,
    modifier = Modifier.fillMaxWidth(),
  )

  if (cat == CardCategory.MAJLIS || cat == CardCategory.PERAYAAN) {
    OutlinedTextField(
      value = card.eventDate,
      onValueChange = { v -> onUpdateCard { it.copy(eventDate = v) } },
      label = { Text("Tarikh Majlis") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
      value = card.eventTime,
      onValueChange = { v -> onUpdateCard { it.copy(eventTime = v) } },
      label = { Text("Masa Majlis") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
      value = card.eventVenue,
      onValueChange = { v -> onUpdateCard { it.copy(eventVenue = v) } },
      label = { Text("Lokasi Majlis") },
      modifier = Modifier.fillMaxWidth(),
    )
  } else if (cat == CardCategory.PERNIAGAAN) {
    OutlinedTextField(
      value = card.businessRole,
      onValueChange = { v -> onUpdateCard { it.copy(businessRole = v) } },
      label = { Text("Jawatan") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
      value = card.businessCompany,
      onValueChange = { v -> onUpdateCard { it.copy(businessCompany = v) } },
      label = { Text("Nama Syarikat") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
      value = card.businessPhone,
      onValueChange = { v -> onUpdateCard { it.copy(businessPhone = v) } },
      label = { Text("No. Telefon Perniagaan") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
      value = card.businessEmail,
      onValueChange = { v -> onUpdateCard { it.copy(businessEmail = v) } },
      label = { Text("E-mel Perniagaan") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
      value = card.businessWebsite,
      onValueChange = { v -> onUpdateCard { it.copy(businessWebsite = v) } },
      label = { Text("Laman Web") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
private fun CanvasToolPanel(
  selectedElement: CanvasElement?,
  onOpenAddText: () -> Unit,
  onAddSticker: (StickerOption) -> Unit,
  onPickPhoto: () -> Unit,
  onUpdateSelectedElement: ((CanvasElement) -> CanvasElement) -> Unit,
  onDeleteSelectedElement: () -> Unit,
) {
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
    OutlinedButton(onClick = onOpenAddText, modifier = Modifier.weight(1f)) {
      Icon(Icons.Filled.TextFields, contentDescription = null, modifier = Modifier.size(16.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text("Teks Bebas")
    }
    OutlinedButton(onClick = onPickPhoto, modifier = Modifier.weight(1f)) {
      Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text("Tambah Foto")
    }
  }

  Text("Tambah Stiker & Mohor", style = MaterialTheme.typography.titleMedium)
  LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    items(StickerOption.entries) { sticker ->
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier.clickable { onAddSticker(sticker) },
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Icon(
            imageVector = stickerIconFor(sticker),
            contentDescription = sticker.labelBm,
            tint = MaterialTheme.colorScheme.primary,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(sticker.labelBm, style = MaterialTheme.typography.labelSmall)
        }
      }
    }
  }

  if (selectedElement != null) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surface,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("Sunting Elemen Dipilih", style = MaterialTheme.typography.titleMedium)
          IconButton(onClick = onDeleteSelectedElement) {
            Icon(
              Icons.Filled.Delete,
              contentDescription = "Padam elemen",
              tint = MaterialTheme.colorScheme.error,
            )
          }
        }

        if (selectedElement.type == "TEXT") {
          OutlinedTextField(
            value = selectedElement.content,
            onValueChange = { v -> onUpdateSelectedElement { it.copy(content = v) } },
            label = { Text("Teks Elemen") },
            modifier = Modifier.fillMaxWidth(),
          )
          Text("Pilih Fon", style = MaterialTheme.typography.labelLarge)
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(CardFontOption.entries) { fontOpt ->
              FilterChip(
                selected = selectedElement.fontKey == fontOpt.key,
                onClick = { onUpdateSelectedElement { it.copy(fontKey = fontOpt.key) } },
                label = { Text(fontOpt.labelBm, fontFamily = fontOpt.family) },
              )
            }
          }
        }

        if (selectedElement.type != "PHOTO") {
          Text("Warna Aksen", style = MaterialTheme.typography.labelLarge)
          val colorSwatches =
            listOf("#B8860B", "#E5C158", "#1E1A16", "#FAF6EF", "#1B4D3E", "#8B2635", "#7A6348")
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            colorSwatches.forEach { hex ->
              val parsed = Color(android.graphics.Color.parseColor(hex))
              Box(
                modifier =
                  Modifier.size(30.dp)
                    .clip(CircleShape)
                    .background(parsed)
                    .border(
                      width = if (selectedElement.colorHex == hex) 3.dp else 1.dp,
                      color =
                        if (selectedElement.colorHex == hex) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                      shape = CircleShape,
                    )
                    .clickable { onUpdateSelectedElement { it.copy(colorHex = hex) } }
              )
            }
          }
        }

        Text("Saiz (${(selectedElement.scale * 100).toInt()}%)", style = MaterialTheme.typography.labelLarge)
        Slider(
          value = selectedElement.scale.toFloat(),
          onValueChange = { v -> onUpdateSelectedElement { it.copy(scale = v.toDouble()) } },
          valueRange = 0.5f..2.2f,
        )

        Text("Putaran (${selectedElement.rotation.toInt()}°)", style = MaterialTheme.typography.labelLarge)
        Slider(
          value = selectedElement.rotation.toFloat(),
          onValueChange = { v -> onUpdateSelectedElement { it.copy(rotation = v.toDouble()) } },
          valueRange = -45f..45f,
        )
      }
    }
  } else {
    Text(
      text = "Sentuh mana-mana elemen di kanvas untuk mengubah fon, warna, saiz atau putaran.",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun PaperAndFrameToolPanel(
  card: ECard,
  onUpdateCard: ((ECard) -> ECard) -> Unit,
) {
  Text("Tekstur Kertas Kad", style = MaterialTheme.typography.titleMedium)
  LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    items(PaperStyle.entries) { paper ->
      FilterChip(
        selected = card.paperStyle == paper.code,
        onClick = { onUpdateCard { it.copy(paperStyle = paper.code) } },
        label = { Text(paper.labelBm) },
      )
    }
  }

  Text("Gaya Bingkai Emas", style = MaterialTheme.typography.titleMedium)
  LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    items(FrameStyle.entries) { frame ->
      FilterChip(
        selected = card.frameStyle == frame.code,
        onClick = { onUpdateCard { it.copy(frameStyle = frame.code) } },
        label = { Text(frame.labelBm) },
      )
    }
  }
}

@Composable
private fun AnimationAndMusicToolPanel(
  card: ECard,
  isMusicPlaying: Boolean,
  playingTrack: MusicTrack,
  onUpdateCard: ((ECard) -> ECard) -> Unit,
  onToggleMusic: (MusicTrack) -> Unit,
) {
  Text("Animasi Kad", style = MaterialTheme.typography.titleMedium)
  LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    items(AnimationType.entries) { anim ->
      FilterChip(
        selected = card.animationType == anim.code,
        onClick = { onUpdateCard { it.copy(animationType = anim.code) } },
        label = { Text(anim.labelBm) },
      )
    }
  }

  Text("Muzik Latar Terbina", style = MaterialTheme.typography.titleMedium)
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    MusicTrack.entries.forEach { track ->
      val selected = card.musicTrack == track.code
      val playingThis = isMusicPlaying && playingTrack == track
      Surface(
        shape = RoundedCornerShape(14.dp),
        color =
          if (selected) MaterialTheme.colorScheme.primaryContainer
          else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier =
          Modifier.fillMaxWidth().clickable {
            onUpdateCard { it.copy(musicTrack = track.code) }
          },
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(track.labelBm, style = MaterialTheme.typography.titleMedium)
            Text(
              track.moodBm,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          if (track != MusicTrack.NONE) {
            IconButton(
              onClick = {
                onUpdateCard { it.copy(musicTrack = track.code) }
                onToggleMusic(track)
              }
            ) {
              Icon(
                imageVector = if (playingThis) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = "Uji muzik",
                tint = MaterialTheme.colorScheme.primary,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ShareToolPanel(
  card: ECard,
  isSaving: Boolean,
  onSaveAndRun: ((ECard) -> Unit) -> Unit,
  onShowMessage: (String) -> Unit,
  context: android.content.Context,
) {
  val cat = CardCategory.fromCode(card.category)
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Text(
        text =
          if (cat == CardCategory.INDIVIDU) "Pautan Khas Penerima (Tanpa RSVP)"
          else if (card.rsvpEnabled) "Pautan Jemputan & RSVP Langsung"
          else "Pautan E-Kad Digital",
        style = MaterialTheme.typography.titleMedium,
      )
      Text(
        text = CardShareHelper.buildShareUrl(card),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
      )
      if (card.shareCode.isBlank()) {
        Text(
          text = "Kad akan disimpan dahulu sebelum dikongsi supaya pautan berfungsi secara langsung.",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }

  Button(
    onClick = { onSaveAndRun { saved -> CardShareHelper.shareToWhatsApp(context, saved) } },
    enabled = !isSaving,
    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("share_whatsapp_btn"),
  ) {
    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
    Spacer(modifier = Modifier.width(8.dp))
    Text("Kongsi Terus ke WhatsApp")
  }

  OutlinedButton(
    onClick = { onSaveAndRun { saved -> CardShareHelper.shareToSocial(context, saved) } },
    enabled = !isSaving,
    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("share_social_btn"),
  ) {
    Text("Kongsi ke Media Sosial Lain")
  }

  OutlinedButton(
    onClick = {
      onSaveAndRun { saved ->
        val url = CardShareHelper.copyShareLink(context, saved)
        onShowMessage("Pautan disalin: $url")
      }
    },
    enabled = !isSaving,
    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("copy_link_btn"),
  ) {
    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
    Spacer(modifier = Modifier.width(8.dp))
    Text(if (cat == CardCategory.INDIVIDU) "Salin Pautan Khas Individu" else "Salin Pautan Kad")
  }
}
