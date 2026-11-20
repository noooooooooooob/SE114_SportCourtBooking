const { test } = require("node:test");
const assert = require("node:assert/strict");
const { findAvailableFacilities, weekdayFor } = require("./availability");

const date = "2026-11-24"; // Tuesday (T3).
const facility = { id: "fac_01", status: "ACTIVE", openTime: "18:00", closeTime: "19:00" };
const court = { id: "court_01", facilityId: facility.id, status: "ACTIVE" };
const booking = { courtId: court.id, bookingDate: date, status: "CONFIRMED", startTime: "18:00", endTime: "18:30" };
const weekly = {
  courtId: court.id, daysOfWeek: ["T3", "T5"], startDate: "2026-11-01", endDate: date,
  status: "PENDING", startTime: "18:30", endTime: "19:00",
};

function available(overrides = {}) {
  return findAvailableFacilities({
    facilities: [facility], courts: [court], bookings: [], recurring: [],
    date, earliestMinute: 0, ...overrides,
  });
}

test("a half hour beginning at the end of a booking is free", () => {
  assert.deepEqual(available({ bookings: [booking] }), [facility.id]);
});

test("a pending recurring session on the last applicable day can fill the remaining slot", () => {
  assert.deepEqual(available({ bookings: [booking], recurring: [weekly] }), []);
});

test("cancelled, expired and different-weekday bookings leave the last slot free", () => {
  const recurring = [
    { ...weekly, status: "CANCELLED" },
    { ...weekly, endDate: "2026-11-23" },
    { ...weekly, daysOfWeek: ["T5"] },
  ];
  assert.deepEqual(available({ bookings: [booking], recurring }), [facility.id]);
});

test("one active court is sufficient even when another court is full", () => {
  const courts = [court, { ...court, id: "court_02" }];
  assert.deepEqual(available({ courts, bookings: [booking], recurring: [weekly] }), [facility.id]);
});

test("a closed facility, a maintenance court and a past slot are each excluded", () => {
  assert.deepEqual(available({ facilities: [{ ...facility, status: "CLOSED" }] }), []);
  assert.deepEqual(available({ courts: [{ ...court, status: "MAINTENANCE" }] }), []);
  assert.deepEqual(available({ earliestMinute: 18 * 60 + 31 }), []);
});

test("calendar input rejects impossible dates and uses the Sunday code CN", () => {
  assert.equal(weekdayFor("2026-02-30"), null);
  assert.equal(weekdayFor("2026-11-22"), "CN");
});
