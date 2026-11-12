package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.CanvasElement
import com.example.data.model.CardCategory
import com.example.data.model.ECard
import com.example.data.model.GuestEntry
import com.example.data.model.GuestGroup
import com.example.data.model.GuestStatus
import com.example.data.model.SharedCard
import com.example.data.model.SharedRsvp
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CardRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth,
) {
  constructor(context: Context) : this(
    db =
      FirebaseFirestore.getInstance(
        context.applicationContext.getString(R.string.firestore_database_id)
      ),
    auth = Firebase.auth,
  )

  private fun requireUserId(): String =
    auth.currentUser?.uid
      ?: throw IllegalStateException("Pengguna mesti log masuk Google sebelum mengakses Firestore.")

  suspend fun ensureUserProfile(displayName: String) {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid)
    try {
      val existing = docRef.get().await()
      val safeName = displayName.ifBlank { "Pengguna SeriKad" }.take(80)
      if (!existing.exists()) {
        val payload =
          mapOf(
            "userId" to uid,
            "displayName" to safeName,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        docRef.set(payload).await()
      } else {
        docRef
          .update(
            mapOf(
              "displayName" to safeName,
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          )
          .await()
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  fun observeUserCards(userId: String): Flow<List<ECard>> = flow {
    val path = "users/$userId/cards"
    emitAll(
      db.collection("users")
        .document(userId)
        .collection("cards")
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(ECard::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        }
        .catch { error ->
          val firestoreError =
            generateSequence(error) { it.cause }
              .filterIsInstance<FirebaseFirestoreException>()
              .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser?.uid == userId) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, path)
          }
          throw error
        }
    )
  }

  fun observeCardGuests(userId: String, cardId: String): Flow<List<GuestEntry>> = flow {
    val path = "users/$userId/cards/$cardId/guests"
    emitAll(
      db.collection("users")
        .document(userId)
        .collection("cards")
        .document(cardId)
        .collection("guests")
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(
            GuestEntry::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
          )
        }
        .catch { error ->
          val firestoreError =
            generateSequence(error) { it.cause }
              .filterIsInstance<FirebaseFirestoreException>()
              .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser?.uid == userId) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, path)
          }
          throw error
        }
    )
  }

  fun observeSharedRsvps(userId: String, shareCode: String): Flow<List<SharedRsvp>> = flow {
    val path = "sharedCards/$shareCode/rsvps"
    emitAll(
      db.collection("sharedCards")
        .document(shareCode)
        .collection("rsvps")
        .whereEqualTo("ownerId", userId)
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(
            SharedRsvp::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
          )
        }
        .catch { error ->
          val firestoreError =
            generateSequence(error) { it.cause }
              .filterIsInstance<FirebaseFirestoreException>()
              .firstOrNull()
          if (firestoreError == null && error is CancellationException) throw error
          val exceptionToLog = firestoreError ?: (error as? Exception)
          if (exceptionToLog != null && auth.currentUser?.uid == userId) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, path)
          }
          throw error
        }
    )
  }

  suspend fun saveCard(card: ECard): ECard {
    val uid = requireUserId()
    val isNew = card.id.isBlank()
    val cardsCol = db.collection("users").document(uid).collection("cards")
    val cardRef = if (isNew) cardsCol.document() else cardsCol.document(card.id)
    val shareCode =
      card.shareCode.ifBlank {
        "SK" + UUID.randomUUID().toString().replace("-", "").take(8).uppercase()
      }
    val effectiveRsvpEnabled =
      if (card.category == CardCategory.INDIVIDU.code) false else card.rsvpEnabled
    val boundedElements = card.elements.take(25).map { it.toMap() }

    try {
      if (isNew) {
        val cardPayload =
          buildCardMap(
            userId = uid,
            shareCode = shareCode,
            card = card.copy(rsvpEnabled = effectiveRsvpEnabled),
            elementsMap = boundedElements,
            includeCreatedAt = true,
          )
        cardRef.set(cardPayload).await()
      } else {
        val updatePayload =
          buildCardMap(
            userId = uid,
            shareCode = shareCode,
            card = card.copy(rsvpEnabled = effectiveRsvpEnabled),
            elementsMap = boundedElements,
            includeCreatedAt = false,
          ).filterKeys { it != "userId" && it != "shareCode" && it != "createdAt" }
        cardRef.update(updatePayload).await()
      }

      val sharedRef = db.collection("sharedCards").document(shareCode)
      val existingShared = sharedRef.get().await()
      if (!existingShared.exists()) {
        val sharedPayload =
          buildSharedCardMap(
            ownerId = uid,
            cardId = cardRef.id,
            shareCode = shareCode,
            card = card.copy(rsvpEnabled = effectiveRsvpEnabled),
            elementsMap = boundedElements,
            includeCreatedAt = true,
          )
        sharedRef.set(sharedPayload).await()
      } else {
        val sharedUpdate =
          buildSharedCardMap(
            ownerId = uid,
            cardId = cardRef.id,
            shareCode = shareCode,
            card = card.copy(rsvpEnabled = effectiveRsvpEnabled),
            elementsMap = boundedElements,
            includeCreatedAt = false,
          ).filterKeys {
            it != "ownerId" && it != "cardId" && it != "shareCode" && it != "createdAt"
          }
        sharedRef.update(sharedUpdate).await()
      }

      return card.copy(
        id = cardRef.id,
        userId = uid,
        shareCode = shareCode,
        rsvpEnabled = effectiveRsvpEnabled,
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, if (isNew) OperationType.CREATE else OperationType.UPDATE, cardRef.path)
      throw e
    }
  }

  suspend fun deleteCard(card: ECard) {
    val uid = requireUserId()
    require(card.id.isNotBlank()) { "ID kad diperlukan untuk memadam kad." }
    val cardRef = db.collection("users").document(uid).collection("cards").document(card.id)
    try {
      // Delete guests subcollection entries first
      val guests = cardRef.collection("guests").get().await()
      for (guestDoc in guests.documents) {
        guestDoc.reference.delete().await()
      }
      cardRef.delete().await()
      if (card.shareCode.isNotBlank()) {
        val sharedRef = db.collection("sharedCards").document(card.shareCode)
        sharedRef.delete().await()
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, cardRef.path)
      throw e
    }
  }

  suspend fun addOrUpdateGuest(
    cardId: String,
    guest: GuestEntry,
  ): GuestEntry {
    val uid = requireUserId()
    require(cardId.isNotBlank()) { "ID kad tidak boleh kosong." }
    val guestsCol =
      db.collection("users").document(uid).collection("cards").document(cardId).collection("guests")
    val isNew = guest.id.isBlank()
    val docRef = if (isNew) guestsCol.document() else guestsCol.document(guest.id)
    val inviteCode =
      guest.inviteCode.ifBlank {
        "G" + UUID.randomUUID().toString().replace("-", "").take(7).uppercase()
      }
    val safeGroup =
      if (GuestGroup.entries.any { it.code == guest.group }) guest.group else GuestGroup.RAKAN.code
    val safeStatus =
      if (GuestStatus.entries.any { it.code == guest.status }) guest.status else GuestStatus.DRAF.code
    val safePax = guest.pax.coerceIn(1L, 20L)

    try {
      if (isNew) {
        val payload =
          mapOf(
            "userId" to uid,
            "cardId" to cardId,
            "name" to guest.name.trim().take(100),
            "phone" to guest.phone.trim().take(30),
            "group" to safeGroup,
            "status" to safeStatus,
            "pax" to safePax,
            "note" to guest.note.trim().take(500),
            "inviteCode" to inviteCode,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        docRef.set(payload).await()
      } else {
        val updatePayload =
          mapOf(
            "name" to guest.name.trim().take(100),
            "phone" to guest.phone.trim().take(30),
            "group" to safeGroup,
            "status" to safeStatus,
            "pax" to safePax,
            "note" to guest.note.trim().take(500),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        docRef.update(updatePayload).await()
      }
      return guest.copy(
        id = docRef.id,
        userId = uid,
        cardId = cardId,
        group = safeGroup,
        status = safeStatus,
        pax = safePax,
        inviteCode = inviteCode,
      )
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, if (isNew) OperationType.CREATE else OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun updateGuestStatus(
    cardId: String,
    guestId: String,
    status: GuestStatus,
    pax: Long? = null,
    note: String? = null,
  ) {
    val uid = requireUserId()
    val docRef =
      db.collection("users")
        .document(uid)
        .collection("cards")
        .document(cardId)
        .collection("guests")
        .document(guestId)
    val updates =
      mutableMapOf<String, Any>(
        "status" to status.code,
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    if (pax != null) {
      updates["pax"] = pax.coerceIn(1L, 20L)
    }
    if (note != null) {
      updates["note"] = note.trim().take(500)
    }
    try {
      docRef.update(updates).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun deleteGuest(cardId: String, guestId: String) {
    val uid = requireUserId()
    val docRef =
      db.collection("users")
        .document(uid)
        .collection("cards")
        .document(cardId)
        .collection("guests")
        .document(guestId)
    try {
      docRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  suspend fun getSharedCardByCode(shareCode: String): SharedCard? {
    requireUserId()
    val cleanCode = shareCode.trim()
    require(cleanCode.isNotBlank()) { "Kod kad tidak boleh kosong." }
    val docRef = db.collection("sharedCards").document(cleanCode)
    return try {
      val snapshot = docRef.get().await()
      if (snapshot.exists()) {
        snapshot.toObject(
          SharedCard::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
        )
      } else {
        null
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, docRef.path)
      throw e
    }
  }

  suspend fun submitSharedFeedback(
    sharedCard: SharedCard,
    guestName: String,
    status: GuestStatus,
    pax: Long,
    wishes: String,
    inviteCode: String = "",
  ) {
    val responderUid = requireUserId()
    val allowedStatus =
      when (status) {
        GuestStatus.HADIR,
        GuestStatus.MUNGKIN,
        GuestStatus.TIDAK_HADIR,
        GuestStatus.DIBUKA -> status.code
        else -> GuestStatus.DIBUKA.code
      }
    val docRef =
      db.collection("sharedCards")
        .document(sharedCard.shareCode)
        .collection("rsvps")
        .document()
    val payload =
      mapOf(
        "ownerId" to sharedCard.ownerId,
        "responderUid" to responderUid,
        "shareCode" to sharedCard.shareCode,
        "cardId" to sharedCard.cardId,
        "guestName" to guestName.trim().ifBlank { "Tetamu" }.take(100),
        "status" to allowedStatus,
        "pax" to pax.coerceIn(1L, 20L),
        "wishes" to wishes.trim().take(500),
        "inviteCode" to inviteCode.trim().take(64),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    try {
      docRef.set(payload).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
  }

  private fun CanvasElement.toMap(): Map<String, Any> =
    mapOf(
      "id" to id,
      "type" to type,
      "content" to content,
      "x" to x,
      "y" to y,
      "scale" to scale,
      "rotation" to rotation,
      "fontKey" to fontKey,
      "colorHex" to colorHex,
      "fontSizeSp" to fontSizeSp,
      "zIndex" to zIndex,
    )

  private fun buildCardMap(
    userId: String,
    shareCode: String,
    card: ECard,
    elementsMap: List<Map<String, Any>>,
    includeCreatedAt: Boolean,
  ): Map<String, Any> {
    val map =
      mutableMapOf<String, Any>(
        "userId" to userId,
        "shareCode" to shareCode,
        "title" to card.title.trim().ifBlank { "Kad Baharu" }.take(120),
        "category" to card.category,
        "templateId" to card.templateId.ifBlank { "custom" }.take(64),
        "recipientName" to card.recipientName.trim().take(100),
        "greetingTitle" to card.greetingTitle.trim().take(160),
        "greetingBody" to card.greetingBody.trim().take(1500),
        "senderName" to card.senderName.trim().take(100),
        "eventDate" to card.eventDate.trim().take(60),
        "eventTime" to card.eventTime.trim().take(60),
        "eventVenue" to card.eventVenue.trim().take(200),
        "businessRole" to card.businessRole.trim().take(100),
        "businessCompany" to card.businessCompany.trim().take(120),
        "businessPhone" to card.businessPhone.trim().take(40),
        "businessEmail" to card.businessEmail.trim().take(120),
        "businessWebsite" to card.businessWebsite.trim().take(200),
        "paperStyle" to card.paperStyle,
        "frameStyle" to card.frameStyle,
        "animationType" to card.animationType,
        "musicTrack" to card.musicTrack,
        "rsvpEnabled" to card.rsvpEnabled,
        "elements" to elementsMap,
        "viewCount" to card.viewCount.coerceAtLeast(0L),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    if (includeCreatedAt) {
      map["createdAt"] = FieldValue.serverTimestamp()
    }
    return map
  }

  private fun buildSharedCardMap(
    ownerId: String,
    cardId: String,
    shareCode: String,
    card: ECard,
    elementsMap: List<Map<String, Any>>,
    includeCreatedAt: Boolean,
  ): Map<String, Any> {
    val map =
      mutableMapOf<String, Any>(
        "ownerId" to ownerId,
        "cardId" to cardId,
        "shareCode" to shareCode,
        "title" to card.title.trim().ifBlank { "Kad Baharu" }.take(120),
        "category" to card.category,
        "templateId" to card.templateId.ifBlank { "custom" }.take(64),
        "recipientName" to card.recipientName.trim().take(100),
        "greetingTitle" to card.greetingTitle.trim().take(160),
        "greetingBody" to card.greetingBody.trim().take(1500),
        "senderName" to card.senderName.trim().take(100),
        "eventDate" to card.eventDate.trim().take(60),
        "eventTime" to card.eventTime.trim().take(60),
        "eventVenue" to card.eventVenue.trim().take(200),
        "businessRole" to card.businessRole.trim().take(100),
        "businessCompany" to card.businessCompany.trim().take(120),
        "businessPhone" to card.businessPhone.trim().take(40),
        "businessEmail" to card.businessEmail.trim().take(120),
        "businessWebsite" to card.businessWebsite.trim().take(200),
        "paperStyle" to card.paperStyle,
        "frameStyle" to card.frameStyle,
        "animationType" to card.animationType,
        "musicTrack" to card.musicTrack,
        "rsvpEnabled" to card.rsvpEnabled,
        "elements" to elementsMap,
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    if (includeCreatedAt) {
      map["createdAt"] = FieldValue.serverTimestamp()
    }
    return map
  }
}
