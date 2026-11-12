const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const tsNow = () => new Date(Date.now() - 1000);

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validCardPayload(uid, shareCode = "SK123456", category = "MAJLIS") {
  return {
    userId: uid,
    shareCode,
    title: "Majlis Kesyukuran Keluarga",
    category,
    templateId: "majlis_orchid_ivory",
    recipientName: "Semua Tetamu",
    greetingTitle: "Jemputan Majlis Kesyukuran",
    greetingBody: "Dengan penuh kesyukuran kami menjemput anda sekeluarga.",
    senderName: "Keluarga Ahmad",
    eventDate: "24 Disember 2026",
    eventTime: "11:30 Pagi - 4:00 Petang",
    eventVenue: "Dewan Seri Melati, Putrajaya",
    paperStyle: "IVORY_COTTON",
    frameStyle: "BOTANICAL_CORNER",
    animationType: "GOLD_PETALS",
    musicTrack: "MELODI_TRADISI",
    rsvpEnabled: category !== "INDIVIDU",
    elements: [
      {
        id: "el1",
        type: "TEXT",
        content: "Bismillah",
        x: 0.5,
        y: 0.2,
        scale: 1.0,
        rotation: 0.0,
        fontKey: "PLAYFAIR",
        colorHex: "#B8860B",
        fontSizeSp: 18.0,
        zIndex: 1,
      },
    ],
    viewCount: 0,
    createdAt: tsNow(),
    updatedAt: tsNow(),
  };
}

function validSharedCardPayload(ownerId, cardId, shareCode = "SK123456", category = "MAJLIS") {
  return {
    ownerId,
    cardId,
    shareCode,
    title: "Majlis Kesyukuran Keluarga",
    category,
    templateId: "majlis_orchid_ivory",
    recipientName: "Semua Tetamu",
    greetingTitle: "Jemputan Majlis Kesyukuran",
    greetingBody: "Dengan penuh kesyukuran kami menjemput anda sekeluarga.",
    senderName: "Keluarga Ahmad",
    eventDate: "24 Disember 2026",
    eventTime: "11:30 Pagi - 4:00 Petang",
    eventVenue: "Dewan Seri Melati, Putrajaya",
    paperStyle: "IVORY_COTTON",
    frameStyle: "BOTANICAL_CORNER",
    animationType: "GOLD_PETALS",
    musicTrack: "MELODI_TRADISI",
    rsvpEnabled: category !== "INDIVIDU",
    elements: [],
    createdAt: tsNow(),
    updatedAt: tsNow(),
  };
}

function validGuestPayload(uid, cardId, inviteCode = "INV1001") {
  return {
    userId: uid,
    cardId,
    name: "Nurul Huda",
    phone: "0123456789",
    group: "KELUARGA",
    status: "DIHANTAR",
    pax: 2,
    note: "InshaAllah hadir",
    inviteCode,
    createdAt: tsNow(),
    updatedAt: tsNow(),
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("cards").get());
  await assertFails(unauthDb.collection("sharedCards").doc("SK123456").get());
});

test("Owner can create, update, and list their own user profile and cards", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      createdAt: tsNow(),
      updatedAt: tsNow(),
    })
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("cards")
      .doc("card_1")
      .set(validCardPayload(ALICE_UID, "SK123456"))
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("cards").get()
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("cards")
      .doc("card_1")
      .update({
        title: "Majlis Dikemas Kini",
        updatedAt: tsNow(),
      })
  );
});

test("Cross-user access to private cards and guest PII is denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("cards")
      .doc("card_1")
      .set(validCardPayload(ALICE_UID, "SK123456"))
  );
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("cards")
      .doc("card_1")
      .collection("guests")
      .doc("guest_1")
      .set(validGuestPayload(ALICE_UID, "card_1"))
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("cards").get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("cards")
      .doc("card_1")
      .collection("guests")
      .doc("guest_1")
      .get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("cards")
      .doc("card_1")
      .collection("guests")
      .doc("guest_2")
      .set(validGuestPayload(BOB_UID, "card_1"))
  );
});

test("Shadow update with ghost field and invalid category are rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const cardPayload = validCardPayload(ALICE_UID, "SK123456");
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("cards")
      .doc("card_bad")
      .set({ ...cardPayload, ghostAdmin: true })
  );
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("cards")
      .doc("card_bad2")
      .set({ ...cardPayload, category: "UNKNOWN" })
  );
});

test("SharedCard allows exact get by code, blocks blanket list, and accepts authenticated RSVP", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb
      .collection("sharedCards")
      .doc("SK123456")
      .set(validSharedCardPayload(ALICE_UID, "card_1", "SK123456"))
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("sharedCards").get());
  await assertSucceeds(bobDb.collection("sharedCards").doc("SK123456").get());
  await assertSucceeds(
    bobDb
      .collection("sharedCards")
      .doc("SK123456")
      .collection("rsvps")
      .doc("rsvp_bob")
      .set({
        ownerId: ALICE_UID,
        responderUid: BOB_UID,
        shareCode: "SK123456",
        cardId: "card_1",
        guestName: "Bob",
        status: "HADIR",
        pax: 2,
        wishes: "Tahniah!",
        createdAt: tsNow(),
        updatedAt: tsNow(),
      })
  );
  // Bob cannot spoof ownerId or read all rsvps without filter
  await assertFails(
    bobDb.collection("sharedCards").doc("SK123456").collection("rsvps").get()
  );
  // Alice CAN list rsvps for her shared card using ownerId filter
  await assertSucceeds(
    aliceDb
      .collection("sharedCards")
      .doc("SK123456")
      .collection("rsvps")
      .where("ownerId", "==", ALICE_UID)
      .get()
  );
});
