package com.se114.courtbooking.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.se114.courtbooking.data.model.Facility

class FacilityRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance("asia-southeast1")
) {
    fun getActiveFacilities(): Task<List<Facility>> =
        firestore.collection("facilities")
            .whereEqualTo("status", "ACTIVE")
            .get()
            .continueWith { task ->
                task.result.documents.mapNotNull { document ->
                    document.toObject(Facility::class.java)?.apply {
                        id = id.ifBlank { document.id }
                    }
                }
            }

    fun getAvailableFacilityIds(date: String): Task<Set<String>> =
        functions.getHttpsCallable("getAvailableFacilities")
            .call(mapOf("date" to date))
            .continueWith { task ->
                val payload = task.result.getData() as? Map<*, *>
                val ids = payload?.get("facilityIds") as? List<*>
                    ?: error("Missing facilityIds in availability response")
                ids.filterIsInstance<String>().toSet()
            }
}
