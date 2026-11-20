const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { HttpsError, onCall } = require("firebase-functions/v2/https");
const { findAvailableFacilities, weekdayFor } = require("./availability");

initializeApp();
const db = getFirestore();
const vietnamClock = new Intl.DateTimeFormat("sv-SE", {
  timeZone: "Asia/Ho_Chi_Minh",
  dateStyle: "short",
  timeStyle: "medium",
});

exports.getAvailableFacilities = onCall({ region: "asia-southeast1" }, async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in to find available courts.");

  const date = request.data?.date;
  const weekday = weekdayFor(date);
  const [today, time] = vietnamClock.format(new Date()).split(" ");
  if (!weekday || date < today) {
    throw new HttpsError("invalid-argument", "Choose today or a future date (yyyy-MM-dd).");
  }
  const [hour, minute, second] = time.split(":").map(Number);
  const earliestMinute = date === today ? hour * 60 + minute + (second > 0 ? 1 : 0) : 0;

  const [facilities, courts, bookings, recurring] = await Promise.all([
    db.collection("facilities").where("status", "==", "ACTIVE")
      .select("id", "status", "openTime", "closeTime").get(),
    db.collection("courts").where("status", "==", "ACTIVE")
      .select("id", "status", "facilityId").get(),
    db.collection("bookings").where("bookingDate", "==", date)
      .select("courtId", "bookingDate", "status", "startTime", "endTime").get(),
    db.collection("recurring_bookings").where("daysOfWeek", "array-contains", weekday)
      .select("courtId", "daysOfWeek", "startDate", "endDate", "status", "startTime", "endTime").get(),
  ]);

  // Use the ERD's id field for joins. Also support documents that use their Firestore key as id.
  const rows = (snapshot) => snapshot.docs.map((doc) => ({ ...doc.data(), id: doc.get("id") || doc.id }));
  return {
    facilityIds: findAvailableFacilities({
      facilities: rows(facilities),
      courts: rows(courts),
      bookings: rows(bookings),
      recurring: rows(recurring),
      date,
      earliestMinute,
    }),
  };
});
