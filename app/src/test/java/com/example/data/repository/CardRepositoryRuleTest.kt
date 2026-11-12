package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.CanvasElement
import com.example.data.model.CardCategory
import com.example.data.model.ECard
import com.example.data.model.GuestEntry
import com.example.data.model.GuestGroup
import com.example.data.model.GuestStatus
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CardRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun saveCard_individu_forcesRsvpDisabledAndCreatesSharedCard() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repo = CardRepository(firestore, auth)
    val shareCode = "SKIND" + UUID.randomUUID().toString().replace("-", "").take(6).uppercase()

    val saved =
      withTimeout(TIMEOUT_MS) {
        repo.saveCard(
          ECard(
            shareCode = shareCode,
            title = "Hari Lahir Aiman",
            category = CardCategory.INDIVIDU.code,
            templateId = "ind_birthday",
            recipientName = "Aiman Hakim",
            greetingTitle = "Selamat Hari Lahir!",
            greetingBody = "Semoga panjang umur dan dimurahkan rezeki.",
            senderName = "Amirah",
            rsvpEnabled = true, // Should be forced false for INDIVIDU
            elements = listOf(CanvasElement(id = "e1", type = "TEXT", content = "Tahniah")),
          )
        )
      }

    assertFalse(saved.rsvpEnabled)
    assertTrue(saved.id.isNotBlank())

    val cards = withTimeout(FLOW_TIMEOUT_MS) { repo.observeUserCards(uid).first { it.any { c -> c.id == saved.id } } }
    assertTrue(cards.any { it.shareCode == shareCode && !it.rsvpEnabled })

    val shared = withTimeout(TIMEOUT_MS) { repo.getSharedCardByCode(shareCode) }
    assertNotNull(shared)
    assertEquals("Aiman Hakim", shared?.recipientName)
    assertFalse(shared?.rsvpEnabled ?: true)
  }

  @Test
  fun guestsAndSharedRsvp_authenticatedFlow_worksAndEnforcesIsolation() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = CardRepository(firestore, auth)
    val shareCode = "SKMAJ" + UUID.randomUUID().toString().replace("-", "").take(6).uppercase()

    val card =
      withTimeout(TIMEOUT_MS) {
        aliceRepo.saveCard(
          ECard(
            shareCode = shareCode,
            title = "Walimatulurus",
            category = CardCategory.MAJLIS.code,
            templateId = "majlis_ivory",
            greetingTitle = "Jemputan Walimatulurus",
            greetingBody = "Menjemput tuan/puan ke majlis kami.",
            senderName = "Keluarga Ahmad",
            rsvpEnabled = true,
          )
        )
      }

    val guest =
      withTimeout(TIMEOUT_MS) {
        aliceRepo.addOrUpdateGuest(
          cardId = card.id,
          guest =
            GuestEntry(
              name = "Farah",
              phone = "0198887766",
              group = GuestGroup.RAKAN.code,
              status = GuestStatus.DIHANTAR.code,
              pax = 2,
            ),
        )
      }
    assertTrue(guest.id.isNotBlank())

    // Bob opens the shared card by code and submits RSVP
    signInTestUser(BOB_EMAIL)
    val bobRepo = CardRepository(firestore, auth)
    val shared = withTimeout(TIMEOUT_MS) { bobRepo.getSharedCardByCode(shareCode) }
    assertNotNull(shared)

    withTimeout(TIMEOUT_MS) {
      bobRepo.submitSharedFeedback(
        sharedCard = shared!!,
        guestName = "Bob",
        status = GuestStatus.HADIR,
        pax = 3,
        wishes = "Selamat Pengantin Baru!",
      )
    }

    // Bob cannot read Alice's private cards or guest list
    val crossUserError =
      try {
        withTimeout(FLOW_TIMEOUT_MS) { bobRepo.observeUserCards(aliceUid).first() }
        null
      } catch (e: Exception) {
        e.unwrapFirestoreException()
      }
    assertNotNull(crossUserError)
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, crossUserError?.code)

    // Alice observes Bob's RSVP live
    signInTestUser(ALICE_EMAIL)
    val rsvps =
      withTimeout(FLOW_TIMEOUT_MS) {
        aliceRepo.observeSharedRsvps(aliceUid, shareCode).first { it.isNotEmpty() }
      }
    assertEquals("HADIR", rsvps.first().status)
    assertEquals(3L, rsvps.first().pax)
  }

  @Test
  fun observeUserCards_unauthenticated_failsWithPermissionDenied() = runBlocking {
    auth.signOut()
    val repo = CardRepository(firestore, auth)
    val error =
      try {
        withTimeout(FLOW_TIMEOUT_MS) { repo.observeUserCards("some_uid").first() }
        null
      } catch (e: Exception) {
        e.unwrapFirestoreException()
      }
    assertNotNull(error)
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, error?.code)
  }

  private companion object {
    const val ALICE_EMAIL = "alice_serikad@test.com"
    const val BOB_EMAIL = "bob_serikad@test.com"
    const val TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}
