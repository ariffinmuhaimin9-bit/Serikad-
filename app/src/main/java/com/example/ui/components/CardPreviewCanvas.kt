package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.AnimationType
import com.example.data.model.CanvasElement
import com.example.data.model.CardCategory
import com.example.data.model.ECard
import com.example.data.model.FrameStyle
import com.example.data.model.PaperStyle
import com.example.data.templates.StickerOption
import com.example.ui.theme.CardFontOption
import com.example.ui.theme.PlayfairDisplayFamily
import com.example.ui.theme.PlusJakartaSansFamily
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun CardPreviewCanvas(
  card: ECard,
  modifier: Modifier = Modifier,
  editable: Boolean = false,
  selectedElementId: String? = null,
  onSelectElement: (String?) -> Unit = {},
  onMoveElement: (String, Float, Float) -> Unit = { _, _, _ -> },
  compact: Boolean = false,
) {
  val paper = PaperStyle.fromCode(card.paperStyle)
  val frame = FrameStyle.fromCode(card.frameStyle)
  val animType = AnimationType.fromCode(card.animationType)
  val isDarkPaper =
    paper == PaperStyle.EMERALD_SONGKET || paper == PaperStyle.OBSIDIAN_GOLD
  val primaryTextColor = if (isDarkPaper) Color(0xFFFAF6EF) else Color(0xFF1E1A16)
  val secondaryTextColor = if (isDarkPaper) Color(0xFFD9C7A3) else Color(0xFF5C5044)
  val goldAccent = if (isDarkPaper) Color(0xFFE5C158) else Color(0xFFB8860B)

  val infiniteTransition = rememberInfiniteTransition(label = "card_particles")
  val animProgress by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 5200, easing = LinearEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "particle_progress",
    )

  Surface(
    modifier = modifier.aspectRatio(3f / 4f),
    shape = RoundedCornerShape(if (compact) 14.dp else 20.dp),
    shadowElevation = if (compact) 4.dp else 10.dp,
  ) {
    BoxWithConstraints(
      modifier =
        Modifier.fillMaxSize().clickable(enabled = editable) { onSelectElement(null) }
    ) {
      val boxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
      val boxHeightPx = with(LocalDensity.current) { maxHeight.toPx() }

      // 1. Paper Background Layer
      when (paper) {
        PaperStyle.IVORY_COTTON ->
          Image(
            painter = painterResource(id = R.drawable.img_card_floral_ivory),
            contentDescription = "Kertas kapas gading",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
          )
        PaperStyle.BLUSH_CHAMPAGNE ->
          Image(
            painter = painterResource(id = R.drawable.img_card_celebration_blush),
            contentDescription = "Kertas cat air syampagne",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
          )
        PaperStyle.EMERALD_SONGKET ->
          Image(
            painter = painterResource(id = R.drawable.img_card_festive_emerald),
            contentDescription = "Latar baldu zamrud songket",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
          )
        PaperStyle.OBSIDIAN_GOLD ->
          Box(
            modifier =
              Modifier.fillMaxSize()
                .background(
                  Brush.linearGradient(
                    colors = listOf(Color(0xFF171310), Color(0xFF282019), Color(0xFF1A1511))
                  )
                )
          )
        PaperStyle.WARM_LINEN ->
          Box(
            modifier =
              Modifier.fillMaxSize()
                .background(
                  Brush.verticalGradient(
                    colors = listOf(Color(0xFFFBF7EF), Color(0xFFF1E8D8), Color(0xFFF8F2E7))
                  )
                )
          )
      }

      // Subtle readability scrim over image textures
      if (paper == PaperStyle.IVORY_COTTON || paper == PaperStyle.BLUSH_CHAMPAGNE) {
        Box(
          modifier =
            Modifier.fillMaxSize().background(Color(0xFFFAF6EF).copy(alpha = 0.24f))
        )
      } else if (paper == PaperStyle.EMERALD_SONGKET) {
        Box(
          modifier =
            Modifier.fillMaxSize().background(Color(0xFF0E2920).copy(alpha = 0.25f))
        )
      }

      // 2. Gold Foil Frame & Paper Grain Canvas
      Canvas(modifier = Modifier.fillMaxSize()) {
        val padOuter = size.minDimension * 0.045f
        val padInner = size.minDimension * 0.068f
        when (frame) {
          FrameStyle.DOUBLE_GOLD, FrameStyle.BOTANICAL_CORNER -> {
            drawRect(
              color = goldAccent.copy(alpha = 0.85f),
              topLeft = Offset(padOuter, padOuter),
              size = Size(size.width - padOuter * 2f, size.height - padOuter * 2f),
              style = Stroke(width = 2.5f),
            )
            drawRect(
              color = goldAccent.copy(alpha = 0.45f),
              topLeft = Offset(padInner, padInner),
              size = Size(size.width - padInner * 2f, size.height - padInner * 2f),
              style = Stroke(width = 1.2f),
            )
            if (frame == FrameStyle.BOTANICAL_CORNER) {
              val cornerR = size.minDimension * 0.05f
              listOf(
                Offset(padInner, padInner),
                Offset(size.width - padInner, padInner),
                Offset(padInner, size.height - padInner),
                Offset(size.width - padInner, size.height - padInner),
              ).forEach { center ->
                drawCircle(
                  color = goldAccent.copy(alpha = 0.65f),
                  radius = cornerR * 0.28f,
                  center = center,
                )
              }
            }
          }
          FrameStyle.SONGKET_ARCH -> {
            drawRoundRect(
              color = goldAccent.copy(alpha = 0.8f),
              topLeft = Offset(padOuter, padOuter),
              size = Size(size.width - padOuter * 2f, size.height - padOuter * 2f),
              cornerRadius = CornerRadius(size.minDimension * 0.08f, size.minDimension * 0.08f),
              style = Stroke(width = 2.5f),
            )
          }
          FrameStyle.MINIMAL_HAIRLINE -> {
            drawRect(
              color = goldAccent.copy(alpha = 0.65f),
              topLeft = Offset(padOuter, padOuter),
              size = Size(size.width - padOuter * 2f, size.height - padOuter * 2f),
              style = Stroke(width = 1.5f),
            )
          }
          FrameStyle.NONE -> Unit
        }
      }

      // 3. Particle Animation Layer
      if (animType != AnimationType.NONE && !compact) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val count = 14
          for (i in 0 until count) {
            val seed = (i * 0.6180339f) % 1f
            val fall = (animProgress + seed) % 1f
            val x =
              size.width * ((seed * 0.86f + 0.07f) + 0.04f * sin((fall * 2f * PI).toFloat() + i))
            val y =
              when (animType) {
                AnimationType.STAR_SHIMMER -> size.height * ((seed * 1.37f) % 0.88f + 0.06f)
                else -> size.height * fall
              }
            val alpha =
              when (animType) {
                AnimationType.STAR_SHIMMER ->
                  (0.25f + 0.65f * kotlin.math.abs(sin(((animProgress + seed) * PI).toFloat())))
                else -> (1f - kotlin.math.abs(fall - 0.5f) * 1.7f).coerceIn(0.15f, 0.9f)
              }

            when (animType) {
              AnimationType.GOLD_PETALS -> {
                val path =
                  Path().apply {
                    moveTo(x, y - 6f)
                    quadraticTo(x + 8f, y, x, y + 8f)
                    quadraticTo(x - 8f, y, x, y - 6f)
                    close()
                  }
                drawPath(path, color = goldAccent.copy(alpha = alpha * 0.75f))
              }
              AnimationType.STAR_SHIMMER -> {
                val r = 5f + (i % 3) * 2.5f
                drawLine(
                  color = goldAccent.copy(alpha = alpha),
                  start = Offset(x - r, y),
                  end = Offset(x + r, y),
                  strokeWidth = 2f,
                )
                drawLine(
                  color = goldAccent.copy(alpha = alpha),
                  start = Offset(x, y - r),
                  end = Offset(x, y + r),
                  strokeWidth = 2f,
                )
              }
              AnimationType.FESTIVE_CONFETTI -> {
                val confettiColors =
                  listOf(goldAccent, Color(0xFF2E7D5B), Color(0xFFD47A7A), Color(0xFFE8C56B))
                drawCircle(
                  color = confettiColors[i % confettiColors.size].copy(alpha = alpha * 0.85f),
                  radius = 4.5f + (i % 3) * 1.5f,
                  center = Offset(x, y),
                )
              }
              AnimationType.ENVELOPE_REVEAL -> {
                val pulse = 0.4f + 0.4f * cos((animProgress * 2f * PI + i).toFloat())
                drawCircle(
                  color = goldAccent.copy(alpha = pulse * 0.45f),
                  radius = 3.5f,
                  center = Offset(x, y),
                )
              }
              AnimationType.NONE -> Unit
            }
          }
        }
      }

      // 4. Core Structured Card Typography Content
      Column(
        modifier =
          Modifier.fillMaxSize()
            .padding(horizontal = if (compact) 14.dp else 28.dp, vertical = if (compact) 16.dp else 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
      ) {
        if (card.recipientName.isNotBlank() && !compact) {
          Text(
            text = "KEPADA ${card.recipientName.uppercase()}",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = PlusJakartaSansFamily,
            letterSpacing = 1.6.sp,
            color = goldAccent,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
          text = card.greetingTitle.ifBlank { card.title },
          fontFamily = PlayfairDisplayFamily,
          fontWeight = FontWeight.SemiBold,
          fontSize = if (compact) 15.sp else 23.sp,
          lineHeight = if (compact) 19.sp else 29.sp,
          color = primaryTextColor,
          textAlign = TextAlign.Center,
          maxLines = if (compact) 2 else 3,
          overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(if (compact) 6.dp else 12.dp))

        Text(
          text = card.greetingBody,
          fontFamily = PlusJakartaSansFamily,
          fontSize = if (compact) 10.sp else 13.sp,
          lineHeight = if (compact) 13.sp else 19.sp,
          color = secondaryTextColor,
          textAlign = TextAlign.Center,
          maxLines = if (compact) 3 else 6,
          overflow = TextOverflow.Ellipsis,
        )

        if (!compact) {
          val cat = CardCategory.fromCode(card.category)
          if (cat == CardCategory.MAJLIS || cat == CardCategory.PERAYAAN) {
            val hasEventInfo =
              card.eventDate.isNotBlank() || card.eventTime.isNotBlank() || card.eventVenue.isNotBlank()
            if (hasEventInfo) {
              Spacer(modifier = Modifier.height(14.dp))
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = goldAccent.copy(alpha = 0.12f),
              ) {
                Column(
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  if (card.eventDate.isNotBlank()) {
                    Text(
                      text = card.eventDate,
                      fontFamily = PlusJakartaSansFamily,
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 12.sp,
                      color = primaryTextColor,
                      textAlign = TextAlign.Center,
                    )
                  }
                  if (card.eventTime.isNotBlank()) {
                    Text(
                      text = card.eventTime,
                      fontFamily = PlusJakartaSansFamily,
                      fontSize = 11.sp,
                      color = secondaryTextColor,
                      textAlign = TextAlign.Center,
                    )
                  }
                  if (card.eventVenue.isNotBlank()) {
                    Text(
                      text = card.eventVenue,
                      fontFamily = PlusJakartaSansFamily,
                      fontSize = 11.sp,
                      color = secondaryTextColor,
                      textAlign = TextAlign.Center,
                    )
                  }
                }
              }
            }
          } else if (cat == CardCategory.PERNIAGAAN) {
            Spacer(modifier = Modifier.height(14.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              if (card.businessRole.isNotBlank() || card.businessCompany.isNotBlank()) {
                Text(
                  text = listOf(card.businessRole, card.businessCompany).filter { it.isNotBlank() }.joinToString(" • "),
                  fontFamily = PlusJakartaSansFamily,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 12.sp,
                  color = goldAccent,
                  textAlign = TextAlign.Center,
                )
              }
              if (card.businessPhone.isNotBlank()) {
                Text(
                  text = card.businessPhone,
                  fontFamily = PlusJakartaSansFamily,
                  fontSize = 11.sp,
                  color = primaryTextColor,
                )
              }
              if (card.businessEmail.isNotBlank()) {
                Text(
                  text = card.businessEmail,
                  fontFamily = PlusJakartaSansFamily,
                  fontSize = 11.sp,
                  color = secondaryTextColor,
                )
              }
              if (card.businessWebsite.isNotBlank()) {
                Text(
                  text = card.businessWebsite,
                  fontFamily = PlusJakartaSansFamily,
                  fontSize = 11.sp,
                  color = goldAccent,
                )
              }
            }
          }
        }

        if (card.senderName.isNotBlank()) {
          Spacer(modifier = Modifier.height(if (compact) 8.dp else 16.dp))
          Text(
            text = card.senderName,
            fontFamily = PlayfairDisplayFamily,
            fontSize = if (compact) 10.sp else 14.sp,
            fontWeight = FontWeight.Medium,
            color = goldAccent,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      // 5. Freeform Draggable Elements Layer
      if (!compact) {
        card.elements.sortedBy { it.zIndex }.forEach { element ->
          val isSelected = editable && selectedElementId == element.id
          val centerX = (element.x.toFloat() * boxWidthPx).roundToInt()
          val centerY = (element.y.toFloat() * boxHeightPx).roundToInt()

          Box(
            modifier =
              Modifier.offset { IntOffset(centerX - 60, centerY - 28) }
                .scale(element.scale.toFloat().coerceIn(0.4f, 2.5f))
                .rotate(element.rotation.toFloat())
                .then(
                  if (isSelected) {
                    Modifier.border(1.5.dp, goldAccent, RoundedCornerShape(8.dp))
                      .background(goldAccent.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                  } else {
                    Modifier
                  }
                )
                .then(
                  if (editable) {
                    Modifier.pointerInput(element.id, boxWidthPx, boxHeightPx) {
                      detectDragGestures(
                        onDragStart = { onSelectElement(element.id) },
                        onDrag = { change, dragAmount ->
                          change.consume()
                          if (boxWidthPx > 0f && boxHeightPx > 0f) {
                            onMoveElement(
                              element.id,
                              dragAmount.x / boxWidthPx,
                              dragAmount.y / boxHeightPx,
                            )
                          }
                        },
                      )
                    }.clickable { onSelectElement(element.id) }
                  } else {
                    Modifier
                  }
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
          ) {
            FreeformElementContent(element = element, fallbackColor = goldAccent)
          }
        }
      }
    }
  }
}

@Composable
private fun FreeformElementContent(element: CanvasElement, fallbackColor: Color) {
  val parsedColor =
    try {
      Color(android.graphics.Color.parseColor(element.colorHex))
    } catch (_: Exception) {
      fallbackColor
    }

  when (element.type) {
    "TEXT" -> {
      val font = CardFontOption.fromKey(element.fontKey).family
      Text(
        text = element.content,
        fontFamily = font,
        fontSize = element.fontSizeSp.toFloat().coerceIn(10f, 38f).sp,
        color = parsedColor,
        textAlign = TextAlign.Center,
      )
    }
    "STICKER" -> {
      val icon = stickerIconFor(StickerOption.fromKey(element.content))
      Surface(
        shape = CircleShape,
        color = parsedColor.copy(alpha = 0.14f),
        modifier = Modifier.size(44.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = StickerOption.fromKey(element.content).labelBm,
            tint = parsedColor,
            modifier = Modifier.size(26.dp),
          )
        }
      }
    }
    "PHOTO" -> {
      val uri = runCatching { Uri.parse(element.content) }.getOrNull()
      if (uri != null) {
        AsyncImage(
          model = uri,
          contentDescription = "Foto hiasan kad",
          contentScale = ContentScale.Crop,
          modifier =
            Modifier.size(84.dp)
              .clip(RoundedCornerShape(12.dp))
              .border(2.dp, fallbackColor, RoundedCornerShape(12.dp)),
        )
      }
    }
  }
}

fun stickerIconFor(sticker: StickerOption): ImageVector =
  when (sticker) {
    StickerOption.WAX_SEAL -> Icons.Filled.Verified
    StickerOption.FLORAL_CREST -> Icons.Filled.LocalFlorist
    StickerOption.BIRTHDAY_CAKE -> Icons.Filled.Cake
    StickerOption.DIAMOND_RINGS -> Icons.Filled.Diamond
    StickerOption.CRESCENT_STAR -> Icons.Filled.Brightness3
    StickerOption.KETUPAT_WEAVE -> Icons.Filled.GridView
    StickerOption.LANTERN_GLOW -> Icons.Filled.LightMode
    StickerOption.GOLD_STAR -> Icons.Filled.Star
    StickerOption.HEART_LACE -> Icons.Filled.Favorite
    StickerOption.BRIEFCASE_BADGE -> Icons.Filled.Work
  }
