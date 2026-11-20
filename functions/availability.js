const BLOCKING_STATUSES = new Set(["PENDING", "CONFIRMED"]);
const SLOT_MINUTES = 30;
const WEEKDAYS = ["CN", "T2", "T3", "T4", "T5", "T6", "T7"];

function weekdayFor(date) {
  if (typeof date !== "string" || !/^\d{4}-\d{2}-\d{2}$/.test(date)) return null;
  const utcDate = new Date(date + "T00:00:00Z");
  if (Number.isNaN(utcDate.getTime()) || utcDate.toISOString().slice(0, 10) !== date) return null;
  return WEEKDAYS[utcDate.getUTCDay()];
}

function minutesOf(time) {
  if (time === "24:00") return 24 * 60;
  if (typeof time !== "string" || !/^(?:[01]\d|2[0-3]):[0-5]\d$/.test(time)) return NaN;
  return Number(time.slice(0, 2)) * 60 + Number(time.slice(3));
}

function findAvailableFacilities({ facilities, courts, bookings, recurring, date, earliestMinute }) {
  const weekday = weekdayFor(date);
  const busyByCourt = new Map();
  const occurrences = recurring.filter((booking) => (
    booking.startDate <= date && date <= booking.endDate && booking.daysOfWeek?.includes(weekday)
  ));

  for (const booking of [...bookings.filter((booking) => booking.bookingDate === date), ...occurrences]) {
    if (!BLOCKING_STATUSES.has(booking.status)) continue;
    const start = minutesOf(booking.startTime);
    const end = minutesOf(booking.endTime);
    if (!(start < end)) continue;

    const busy = busyByCourt.get(booking.courtId) ?? [];
    busy.push({ start, end });
    busyByCourt.set(booking.courtId, busy);
  }

  const hoursByFacility = new Map(facilities
    .filter((facility) => facility.status === "ACTIVE")
    .map((facility) => [facility.id, {
      open: minutesOf(facility.openTime),
      close: minutesOf(facility.closeTime),
    }]));

  const available = new Set();
  for (const court of courts) {
    if (court.status !== "ACTIVE" || available.has(court.facilityId)) continue;
    const hours = hoursByFacility.get(court.facilityId);
    if (!hours) continue;

    const busy = busyByCourt.get(court.id) ?? [];
    const firstSlot = Math.ceil(Math.max(hours.open, earliestMinute) / SLOT_MINUTES) * SLOT_MINUTES;
    for (let start = firstSlot; start + SLOT_MINUTES <= hours.close; start += SLOT_MINUTES) {
      const end = start + SLOT_MINUTES;
      if (busy.some((booking) => start < booking.end && booking.start < end)) continue;
      available.add(court.facilityId);
      break;
    }
  }
  return [...available];
}

module.exports = { findAvailableFacilities, weekdayFor };
