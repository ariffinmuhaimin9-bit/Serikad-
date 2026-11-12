package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CardCategory
import com.example.data.model.ECard
import com.example.data.model.GuestStatus
import com.example.ui.components.CardPreviewCanvas
import com.example.ui.theme.AmberStatus
import com.example.ui.theme.EmeraldStatus
import com.example.ui.theme.RoseTerracotta
import com.example.viewmodel.DashboardActivityItem
import com.example.viewmodel.DashboardSummary
import com.example.viewmodel.UiState

@Composable
fun DashboardScreen(
  userName: String,
  summary: DashboardSummary,
  cardsState: UiState<List<ECard>>,
  onCreateNewCard: () -> Unit,
  onOpenCode: (String) -> Unit,
  onSelectCardToManage: (ECard) -> Unit,
  onPreviewCard: (ECard) -> Unit,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showOpenCodeDialog by remember { mutableStateOf(false) }
  var codeInput by remember { mutableStateOf("") }

  LazyColumn(
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // 1. Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "PAPARAN LANGSUNG",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
          )
          Text(
            text = "Selamat Datang, ${userName.ifBlank { "Pengirim" }}",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
        IconButton(
          onClick = onSignOut,
          modifier = Modifier.testTag("sign_out_button"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            contentDescription = "Log keluar",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }

    // 2. Quick Actions Row
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Button(
          onClick = onCreateNewCard,
          shape = RoundedCornerShape(14.dp),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
          modifier = Modifier.weight(1f).height(48.dp).testTag("dashboard_create_card_btn"),
        ) {
          Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Cipta Kad", maxLines = 1)
        }
        OutlinedButton(
          onClick = { showOpenCodeDialog = true },
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
          modifier = Modifier.weight(1f).height(48.dp).testTag("dashboard_open_code_btn"),
        ) {
          Icon(
            Icons.Filled.QrCodeScanner,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Buka Kod", color = MaterialTheme.colorScheme.primary, maxLines = 1)
        }
      }
    }

    // 3. Live 4-Metric KPI Grid
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          KpiCard(
            title = "Hadir (RSVP)",
            value = "${summary.totalAttending}",
            subtitle = "${summary.totalAttendingPax} pax disahkan",
            icon = Icons.Filled.CheckCircle,
            accent = EmeraldStatus,
            modifier = Modifier.weight(1f),
          )
          KpiCard(
            title = "Menunggu",
            value = "${summary.totalPending + summary.totalMaybe}",
            subtitle = "${summary.totalSent} telah dihantar",
            icon = Icons.Filled.Schedule,
            accent = AmberStatus,
            modifier = Modifier.weight(1f),
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          KpiCard(
            title = "Dibuka Penerima",
            value = "${summary.totalOpened}",
            subtitle = "Daripada ${summary.totalCards} kad aktif",
            icon = Icons.Filled.MarkEmailRead,
            accent = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
          )
          KpiCard(
            title = "Tidak Hadir",
            value = "${summary.totalDeclined}",
            subtitle = "${summary.totalMaybe} mungkin hadir",
            icon = Icons.Filled.Close,
            accent = RoseTerracotta,
            modifier = Modifier.weight(1f),
          )
        }
      }
    }

    // 4. Visual Distribution Bar
    item {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Status Jemputan Langsung",
              style = MaterialTheme.typography.titleMedium,
            )
            Text(
              text = "${summary.totalGuests} rekod",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.primary,
            )
          }
          Spacer(modifier = Modifier.height(12.dp))
          val total = maxOf(1, summary.totalAttending + summary.totalPending + summary.totalMaybe + summary.totalDeclined)
          Row(
            modifier =
              Modifier.fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant)
          ) {
            if (summary.totalAttending > 0) {
              Box(
                modifier =
                  Modifier.weight(summary.totalAttending.toFloat() / total)
                    .fillMaxSize()
                    .background(EmeraldStatus)
              )
            }
            if (summary.totalPending + summary.totalMaybe > 0) {
              Box(
                modifier =
                  Modifier.weight((summary.totalPending + summary.totalMaybe).toFloat() / total)
                    .fillMaxSize()
                    .background(AmberStatus)
              )
            }
            if (summary.totalDeclined > 0) {
              Box(
                modifier =
                  Modifier.weight(summary.totalDeclined.toFloat() / total)
                    .fillMaxSize()
                    .background(RoseTerracotta)
              )
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            LegendDot("Hadir", EmeraldStatus)
            LegendDot("Menunggu", AmberStatus)
            LegendDot("Tidak Hadir", RoseTerracotta)
          }
        }
      }
    }

    // 5. Active Cards Strip
    item {
      Text(
        text = "Kad Aktif Anda",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
      )
    }

    when (cardsState) {
      UiState.Loading -> {
        item {
          Box(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            contentAlignment = Alignment.Center,
          ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
          }
        }
      }
      is UiState.Error -> {
        item {
          Surface(
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = cardsState.message,
              color = MaterialTheme.colorScheme.error,
              modifier = Modifier.padding(16.dp),
            )
          }
        }
      }
      is UiState.Success -> {
        val cards = cardsState.data
        if (cards.isEmpty()) {
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
                Icon(
                  imageVector = Icons.Filled.Style,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(36.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "Belum Ada Kad Dicipta",
                  style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Pilih templat premium atau reka kad baharu di Studio Kreatif.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onCreateNewCard) { Text("Mula Cipta Kad") }
              }
            }
          }
        } else {
          item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              items(cards, key = { it.id }) { card ->
                val cat = CardCategory.fromCode(card.category)
                Surface(
                  shape = RoundedCornerShape(16.dp),
                  color = MaterialTheme.colorScheme.surface,
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                  modifier =
                    Modifier.width(168.dp).clickable { onSelectCardToManage(card) },
                ) {
                  Column(modifier = Modifier.padding(10.dp)) {
                    CardPreviewCanvas(
                      card = card,
                      compact = true,
                      modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                      text = card.title,
                      style = MaterialTheme.typography.titleMedium,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                      text =
                        if (cat == CardCategory.INDIVIDU) "Pautan Khas • ${card.shareCode}"
                        else "${cat.labelBm} • ${card.shareCode}",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.primary,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(
                      onClick = { onPreviewCard(card) },
                      contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                      modifier = Modifier.height(32.dp),
                    ) {
                      Text("Pratonton Kad", style = MaterialTheme.typography.labelLarge)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // 6. Live Feed of Responses & Wishes
    item {
      Text(
        text = "Maklum Balas & Ucapan Terkini",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
      )
    }

    if (summary.recentActivity.isEmpty()) {
      item {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.surface,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Filled.Drafts,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "Maklum balas tetamu dan ucapan penerima akan muncul secara langsung di sini.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    } else {
      items(summary.recentActivity, key = { it.id }) { item ->
        ActivityFeedRow(item = item)
      }
    }
  }

  if (showOpenCodeDialog) {
    AlertDialog(
      onDismissRequest = { showOpenCodeDialog = false },
      title = { Text("Buka Kad Melalui Kod / Pautan") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Masukkan kod kad (cth. SK8A2B1C) atau tampal pautan seriKad untuk melihat kad penerima.",
            style = MaterialTheme.typography.bodyMedium,
          )
          OutlinedTextField(
            value = codeInput,
            onValueChange = { codeInput = it },
            label = { Text("Kod atau Pautan Kad") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("open_code_input"),
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            showOpenCodeDialog = false
            onOpenCode(codeInput)
            codeInput = ""
          }
        ) {
          Text("Buka Kad")
        }
      },
      dismissButton = {
        TextButton(onClick = { showOpenCodeDialog = false }) { Text("Batal") }
      },
    )
  }
}

@Composable
private fun KpiCard(
  title: String,
  value: String,
  subtitle: String,
  icon: ImageVector,
  accent: Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
    modifier = modifier,
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
          modifier =
            Modifier.size(30.dp).clip(CircleShape).background(accent.copy(alpha = 0.14f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(16.dp),
          )
        }
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun LegendDot(label: String, color: Color) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun ActivityFeedRow(item: DashboardActivityItem) {
  val statusColor =
    when (item.status) {
      GuestStatus.HADIR -> EmeraldStatus
      GuestStatus.TIDAK_HADIR -> RoseTerracotta
      GuestStatus.DIBUKA -> MaterialTheme.colorScheme.primary
      else -> AmberStatus
    }
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier.size(10.dp).clip(CircleShape).background(statusColor)
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = item.guestName,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
          )
          Surface(
            shape = RoundedCornerShape(50),
            color = statusColor.copy(alpha = 0.14f),
          ) {
            val badgeText =
              if (item.cardCategory == CardCategory.INDIVIDU && item.status == GuestStatus.DIBUKA) {
                "Ucapan Dibuka"
              } else if (item.status == GuestStatus.HADIR) {
                "Hadir (${item.pax} pax)"
              } else {
                item.status.labelBm
              }
            Text(
              text = badgeText,
              style = MaterialTheme.typography.labelSmall,
              color = statusColor,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            )
          }
        }
        Text(
          text = item.cardTitle,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (item.note.isNotBlank()) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "\"${item.note}\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
      }
    }
  }
}
