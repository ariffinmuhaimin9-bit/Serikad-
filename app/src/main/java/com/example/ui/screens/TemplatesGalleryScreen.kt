package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.templates.CardTemplatePreset
import com.example.data.templates.CardTemplatesCatalog
import com.example.media.CardShareHelper
import com.example.ui.components.CardPreviewCanvas
import com.example.viewmodel.UiState

@Composable
fun TemplatesGalleryScreen(
  cardsState: UiState<List<ECard>>,
  onUseTemplate: (CardTemplatePreset) -> Unit,
  onStartBlank: (CardCategory) -> Unit,
  onEditCard: (ECard) -> Unit,
  onPreviewCard: (ECard) -> Unit,
  onDeleteCard: (ECard) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  var selectedCategory by remember { mutableStateOf<CardCategory?>(null) }

  val filteredTemplates =
    remember(selectedCategory) {
      if (selectedCategory == null) CardTemplatesCatalog.templates
      else CardTemplatesCatalog.templates.filter { it.category == selectedCategory }
    }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Column {
        Text(
          text = "KOLEKSI ATELIER",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
        )
        Text(
          text = "Templat Premium & Kad Saya",
          style = MaterialTheme.typography.headlineMedium,
        )
      }
    }

    // Category Filter Chips
    item {
      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
          FilterChip(
            selected = selectedCategory == null,
            onClick = { selectedCategory = null },
            label = { Text("Semua") },
            colors =
              FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
              ),
          )
        }
        items(CardCategory.entries) { cat ->
          FilterChip(
            selected = selectedCategory == cat,
            onClick = { selectedCategory = cat },
            label = { Text(cat.labelBm) },
            colors =
              FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
              ),
          )
        }
      }
    }

    // Blank Creative Canvas Launcher
    item {
      val targetCat = selectedCategory ?: CardCategory.INDIVIDU
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth().clickable { onStartBlank(targetCat) },
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Reka Secara Bebas (${targetCat.labelBm})",
              style = MaterialTheme.typography.titleMedium,
            )
            Text(
              text = targetCat.subtitleBm,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Button(
            onClick = { onStartBlank(targetCat) },
            modifier = Modifier.testTag("start_blank_card_btn"),
          ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Kosong")
          }
        }
      }
    }

    // Curated Templates Showcase
    item {
      Text(
        text = "Templat Sedia Guna",
        style = MaterialTheme.typography.titleLarge,
      )
    }

    item {
      LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(filteredTemplates, key = { it.id }) { preset ->
          val previewCard = remember(preset) { CardTemplatesCatalog.createCardFromPreset(preset) }
          Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
            modifier = Modifier.width(196.dp).clickable { onUseTemplate(preset) },
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              CardPreviewCanvas(
                card = previewCard,
                compact = true,
                modifier = Modifier.fillMaxWidth(),
              )
              Spacer(modifier = Modifier.height(10.dp))
              Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer,
              ) {
                Text(
                  text = preset.category.labelBm,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = preset.nameBm,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = preset.subtitleBm,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
              )
              Spacer(modifier = Modifier.height(10.dp))
              Button(
                onClick = { onUseTemplate(preset) },
                modifier = Modifier.fillMaxWidth().testTag("use_template_${preset.id}"),
              ) {
                Text("Guna Templat")
              }
            }
          }
        }
      }
    }

    // Saved Cards List
    item {
      Text(
        text = "Kad Tersimpan Saya",
        style = MaterialTheme.typography.titleLarge,
      )
    }

    when (cardsState) {
      is UiState.Success -> {
        val saved =
          if (selectedCategory == null) cardsState.data
          else cardsState.data.filter { it.category == selectedCategory!!.code }
        if (saved.isEmpty()) {
          item {
            Text(
              text = "Tiada kad tersimpan bagi kategori ini.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        } else {
          items(saved, key = { it.id }) { card ->
            val cat = CardCategory.fromCode(card.category)
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                CardPreviewCanvas(
                  card = card,
                  compact = true,
                  modifier = Modifier.width(74.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Text(
                    text =
                      if (cat == CardCategory.INDIVIDU) {
                        "Individu • Pautan Khas (Tanpa RSVP)"
                      } else if (card.rsvpEnabled) {
                        "${cat.labelBm} • RSVP Aktif"
                      } else {
                        "${cat.labelBm} • Pautan Sahaja"
                      },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(
                      onClick = { onEditCard(card) },
                      contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                      modifier = Modifier.height(34.dp),
                    ) {
                      Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("Ubah", style = MaterialTheme.typography.labelLarge)
                    }
                    IconButton(
                      onClick = { onPreviewCard(card) },
                      modifier = Modifier.size(34.dp),
                    ) {
                      Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = "Pratonton kad",
                        tint = MaterialTheme.colorScheme.primary,
                      )
                    }
                    IconButton(
                      onClick = { CardShareHelper.shareToWhatsApp(context, card) },
                      modifier = Modifier.size(34.dp),
                    ) {
                      Icon(
                        Icons.Filled.Share,
                        contentDescription = "Kongsi WhatsApp",
                        tint = MaterialTheme.colorScheme.primary,
                      )
                    }
                    IconButton(
                      onClick = { onDeleteCard(card) },
                      modifier = Modifier.size(34.dp),
                    ) {
                      Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = "Padam kad",
                        tint = MaterialTheme.colorScheme.error,
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
      else -> Unit
    }
  }
}
